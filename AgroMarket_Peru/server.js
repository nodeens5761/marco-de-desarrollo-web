require("dotenv").config();
const express = require("express");
const mysql = require("mysql2/promise");
const crypto = require("crypto");
const path = require("path");
const app = express();
const cfg = {
  host: process.env.DB_HOST || "127.0.0.1",
  port: +(process.env.DB_PORT || 3306),
  user: process.env.DB_USER || "root",
  password: process.env.DB_PASSWORD || "",
  database: process.env.DB_NAME || "agromarketperu",
};
let db;
const hash = (s, salt = crypto.randomBytes(16).toString("hex")) =>
  `${salt}:${crypto.scryptSync(s, salt, 64).toString("hex")}`;
const verify = (s, stored) => {
  const [salt, key] = stored.split(":");
  return crypto.timingSafeEqual(
    Buffer.from(key, "hex"),
    crypto.scryptSync(s, salt, 64),
  );
};
const token = () => crypto.randomBytes(32).toString("hex");
async function initDb() {
  const root = await mysql.createConnection({
    host: cfg.host,
    port: cfg.port,
    user: cfg.user,
    password: cfg.password,
  });
  await root.query(
    `CREATE DATABASE IF NOT EXISTS \`${cfg.database}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`,
  );
  await root.end();
  db = await mysql.createPool({ ...cfg, connectionLimit: 10 });
  await db.query(
    `CREATE TABLE IF NOT EXISTS usuarios (id INT AUTO_INCREMENT PRIMARY KEY,nombre VARCHAR(120) NOT NULL,correo VARCHAR(160) NOT NULL UNIQUE,password_hash VARCHAR(255) NOT NULL,rol ENUM('user','admin') NOT NULL DEFAULT 'user',telefono VARCHAR(30),dni VARCHAR(20),direccion VARCHAR(255),distrito VARCHAR(100),created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)`,
  );
  await db.query(
    `CREATE TABLE IF NOT EXISTS sesiones (token CHAR(64) PRIMARY KEY,usuario_id INT NOT NULL,expires_at DATETIME NOT NULL,FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE)`,
  );
  await db.query(
    `CREATE TABLE IF NOT EXISTS pedidos (id INT AUTO_INCREMENT PRIMARY KEY,numero VARCHAR(30) NOT NULL UNIQUE,usuario_id INT NOT NULL,nombre_cliente VARCHAR(120) NOT NULL,correo VARCHAR(160) NOT NULL,telefono VARCHAR(30) NOT NULL,dni VARCHAR(20) NOT NULL,direccion VARCHAR(255) NOT NULL,distrito VARCHAR(100) NOT NULL,referencia VARCHAR(255),metodo_pago VARCHAR(60) NOT NULL,total DECIMAL(10,2) NOT NULL,estado VARCHAR(40) NOT NULL DEFAULT 'Confirmado',fecha_entrega VARCHAR(80) NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE)`,
  );
  await db.query(
    `CREATE TABLE IF NOT EXISTS detalle_pedido (id INT AUTO_INCREMENT PRIMARY KEY,pedido_id INT NOT NULL,producto_id INT NOT NULL,producto_nombre VARCHAR(180) NOT NULL,tienda VARCHAR(100) NOT NULL,precio DECIMAL(10,2) NOT NULL,cantidad INT NOT NULL,subtotal DECIMAL(10,2) NOT NULL,FOREIGN KEY(pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE)`,
  );
  await db.query(
    `CREATE TABLE IF NOT EXISTS alertas (id INT AUTO_INCREMENT PRIMARY KEY,usuario_id INT NOT NULL,producto_id INT NOT NULL,producto_nombre VARCHAR(180) NOT NULL,precio_objetivo DECIMAL(10,2) NOT NULL,activo TINYINT(1) NOT NULL DEFAULT 1,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE)`,
  );
  const [a] = await db.query(
    'SELECT id FROM usuarios WHERE rol="admin" LIMIT 1',
  );
  if (!a.length)
    await db.query(
      'INSERT INTO usuarios(nombre,correo,password_hash,rol) VALUES(?,?,?,"admin")',
      ["Administrador", "admin@agromarketperu.local", hash("admin123")],
    );
}
app.use(express.json());
app.use(express.static(__dirname));
async function auth(req, res, next) {
  try {
    const t = (req.headers.authorization || "").replace("Bearer ", "");
    if (!t) return res.status(401).json({ error: "Sesión requerida" });
    const [rows] = await db.query(
      "SELECT u.* FROM sesiones s JOIN usuarios u ON u.id=s.usuario_id WHERE s.token=? AND s.expires_at>NOW()",
      [t],
    );
    if (!rows.length) return res.status(401).json({ error: "Sesión expirada" });
    req.user = rows[0];
    next();
  } catch (e) {
    res.status(500).json({ error: "Error de autenticación" });
  }
}
app.post("/api/auth/register", async (req, res) => {
  try {
    const { nombre, correo, password } = req.body;
    if (!nombre || !correo || !password)
      return res.status(400).json({ error: "Completa los campos" });
    const [r] = await db.query(
      'INSERT INTO usuarios(nombre,correo,password_hash,rol) VALUES(?,?,?,"user")',
      [nombre.trim(), correo.trim().toLowerCase(), hash(password)],
    );
    res.json({ ok: true, id: r.insertId });
  } catch (e) {
    res
      .status(400)
      .json({
        error:
          e.code === "ER_DUP_ENTRY"
            ? "El correo ya está registrado"
            : "No se pudo registrar",
      });
  }
});
app.post("/api/auth/login", async (req, res) => {
  try {
    const { correo, password } = req.body;
    const [rows] = await db.query("SELECT * FROM usuarios WHERE correo=?", [
      String(correo || "")
        .trim()
        .toLowerCase(),
    ]);
    if (!rows.length || !verify(password, rows[0].password_hash))
      return res.status(401).json({ error: "Correo o contraseña incorrectos" });
    const t = token();
    await db.query(
      "INSERT INTO sesiones(token,usuario_id,expires_at) VALUES(?,?,DATE_ADD(NOW(),INTERVAL 8 HOUR))",
      [t, rows[0].id],
    );
    res.json({
      token: t,
      user: {
        id: rows[0].id,
        nombre: rows[0].nombre,
        correo: rows[0].correo,
        rol: rows[0].rol,
      },
    });
  } catch (e) {
    res.status(500).json({ error: "No se pudo iniciar sesión" });
  }
});
app.post("/api/auth/admin", async (req, res) => {
  try {
    const { usuario, password } = req.body;
    if (usuario !== "ADMIN" || password !== "admin123")
      return res
        .status(401)
        .json({ error: "Credenciales administrativas incorrectas" });
    const [rows] = await db.query(
      'SELECT * FROM usuarios WHERE rol="admin" LIMIT 1',
    );
    const t = token();
    await db.query(
      "INSERT INTO sesiones(token,usuario_id,expires_at) VALUES(?,?,DATE_ADD(NOW(),INTERVAL 8 HOUR))",
      [t, rows[0].id],
    );
    res.json({
      token: t,
      user: {
        id: rows[0].id,
        nombre: rows[0].nombre,
        correo: rows[0].correo,
        rol: "admin",
      },
    });
  } catch (e) {
    res.status(500).json({ error: "No se pudo validar al administrador" });
  }
});
app.post("/api/auth/logout", auth, async (req, res) => {
  const t = (req.headers.authorization || "").replace("Bearer ", "");
  await db.query("DELETE FROM sesiones WHERE token=?", [t]);
  res.json({ ok: true });
});
app.get("/api/usuarios/perfil", auth, (req, res) =>
  res.json({
    id: req.user.id,
    nombre: req.user.nombre,
    correo: req.user.correo,
    telefono: req.user.telefono,
    dni: req.user.dni,
    direccion: req.user.direccion,
    distrito: req.user.distrito,
  }),
);
app.put("/api/usuarios/perfil", auth, async (req, res) => {
  const { nombre, telefono, dni, direccion, distrito } = req.body;
  await db.query(
    "UPDATE usuarios SET nombre=?,telefono=?,dni=?,direccion=?,distrito=? WHERE id=?",
    [nombre, telefono, dni, direccion, distrito, req.user.id],
  );
  res.json({ ok: true });
});
app.post("/api/alertas", auth, async (req, res) => {
  const { productoId, productoNombre, precioObjetivo } = req.body;
  if (req.user.rol !== "user")
    return res
      .status(403)
      .json({ error: "Solo clientes pueden crear alertas" });
  await db.query(
    "INSERT INTO alertas(usuario_id,producto_id,producto_nombre,precio_objetivo) VALUES(?,?,?,?)",
    [req.user.id, productoId, productoNombre, precioObjetivo],
  );
  res.json({ ok: true });
});
app.get("/api/alertas", auth, async (req, res) => {
  const [rows] = await db.query(
    "SELECT * FROM alertas WHERE usuario_id=? ORDER BY created_at DESC",
    [req.user.id],
  );
  res.json(rows);
});
app.post("/api/pedidos", auth, async (req, res) => {
  try {
    const { cliente, items, total } = req.body;
    if (
      req.user.rol !== "user" ||
      !cliente ||
      !Array.isArray(items) ||
      !items.length
    )
      return res.status(400).json({ error: "Datos de compra incompletos" });
    const numero = "AMP-" + Date.now().toString().slice(-8);
    const fecha = "Entrega estimada entre hoy y mañana";
    const con = await db.getConnection();
    try {
      await con.beginTransaction();
      const [r] = await con.query(
        "INSERT INTO pedidos(numero,usuario_id,nombre_cliente,correo,telefono,dni,direccion,distrito,referencia,metodo_pago,total,estado,fecha_entrega) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
        [
          numero,
          req.user.id,
          cliente.nombre,
          cliente.correo,
          cliente.telefono,
          cliente.dni,
          cliente.direccion,
          cliente.distrito,
          cliente.referencia || "",
          cliente.metodoPago,
          total,
          "Confirmado",
          fecha,
        ],
      );
      for (const i of items)
        await con.query(
          "INSERT INTO detalle_pedido(pedido_id,producto_id,producto_nombre,tienda,precio,cantidad,subtotal) VALUES(?,?,?,?,?,?,?)",
          [
            r.insertId,
            i.id,
            i.nombre,
            i.tienda,
            i.precio,
            i.cantidad,
            i.precio * i.cantidad,
          ],
        );
      await con.commit();
      res.json({ ok: true, numero, fechaEntrega: fecha, total });
    } catch (e) {
      await con.rollback();
      throw e;
    } finally {
      con.release();
    }
  } catch (e) {
    res.status(500).json({ error: "No se pudo registrar la compra" });
  }
});
app.get("/api/pedidos", auth, async (req, res) => {
  const [orders] = await db.query(
    "SELECT * FROM pedidos WHERE usuario_id=? ORDER BY created_at DESC",
    [req.user.id],
  );
  for (const o of orders) {
    const [d] = await db.query(
      "SELECT * FROM detalle_pedido WHERE pedido_id=?",
      [o.id],
    );
    o.items = d;
  }
  res.json(orders);
});
app.get("/api/admin/pedidos", auth, async (req, res) => {
  if (req.user.rol !== "admin")
    return res.status(403).json({ error: "Acceso denegado" });
  const [rows] = await db.query(
    "SELECT numero,nombre_cliente,correo,total,estado,fecha_entrega,created_at FROM pedidos ORDER BY created_at DESC",
  );
  res.json(rows);
});
initDb()
  .then(() =>
    app.listen(process.env.PORT || 3000, () =>
      console.log(
        `AgroMarketPeru activo en http://localhost:${process.env.PORT || 3000}`,
      ),
    ),
  )
  .catch((e) => {
    console.error(
      "No se pudo iniciar. Verifica Laragon/MySQL y .env",
      e.message,
    );
    process.exit(1);
  });
