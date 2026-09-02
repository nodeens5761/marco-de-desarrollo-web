// Integrante 3: carrito, checkout, historial y perfil del cliente.

// ==========================================
// 1. HELPERS Y UTILIDADES BASE
// ==========================================

// Función corta (selector) para no escribir document.querySelector en todo el código
const Q = (selector) => document.querySelector(selector);

// Obtiene los productos guardados en LocalStorage (si no hay nada, devuelve un arreglo vacío)
const cart = () => JSON.parse(localStorage.getItem('ampCart') || '[]');

// Guarda el carrito en LocalStorage y actualiza la vista de la página en tiempo real
const saveCart = (carrito) => {
  localStorage.setItem('ampCart', JSON.stringify(carrito));
  updateCommon();
  renderCart();
};

// Obtiene la lista completa de productos desde el proveedor de datos global
const products = () => window.AgroMarketDataProvider?.obtenerProductos?.() || [];

// Verifica si existe una sesión activa guardada en el navegador
const logged = () => !!localStorage.getItem('ampSession');

// Control de seguridad: Si el usuario NO ha iniciado sesión, lo redirige a la pantalla de Login
function ensure() {
  if (!logged()) {
    location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=required');
  }

  return logged();
}

// ==========================================
// 2. RENDERIZADO DEL CARRITO
// ==========================================

// Dibuja dinámicamente los productos del carrito en la interfaz y calcula los totales
function renderCart() {
  const box = Q('#cartItems');

  if (!box) return;
  if (!logged()) return ensure();

  // Une el ID del carrito con la información completa del producto (nombre, precio, foto)
  const rows = cart()
    .map((item) => ({ ...item, p: products().find((producto) => producto.id == item.id) }))
    .filter((item) => item.p);

  // Si el carrito está vacío, muestra un mensaje informativo
  if (!rows.length) {
    box.innerHTML = `
      <div class="alert alert-info">
        <i class="bi bi-cart-x me-2"></i>Tu carrito está vacío.
        <a class="alert-link ms-1" href="/modulo-2-catalogo/index.html">Ir al catálogo</a>
      </div>
    `;
    Q('#summary')?.classList.add('d-none');
    return;
  }

  // Muestra el resumen de compra, calcula la cantidad de unidades y el monto total en Soles
  Q('#summary').classList.remove('d-none');
  Q('#units').textContent = rows.reduce((suma, item) => suma + item.cantidad, 0);

  const total = rows.reduce((suma, item) => suma + item.p.precio * item.cantidad, 0);
  Q('#total').textContent = `S/ ${total.toFixed(2)}`;

  // Genera el HTML para cada fila de producto con botones de suma, resta y eliminar
  box.innerHTML = rows
    .map(
      (item) => `
        <div class="d-flex gap-3 align-items-center border-bottom py-3 flex-wrap">
          <img class="cart-img rounded-3" src="${item.p.imagen}" alt="${escapeHtml(item.p.nombre)}">
          <div class="flex-grow-1">
            <h2 class="h6 mb-1">${escapeHtml(item.p.nombre)}</h2>
            <small class="text-secondary">${escapeHtml(item.p.tienda)} · S/ ${item.p.precio.toFixed(2)}</small>
          </div>
          <div class="btn-group btn-group-sm">
            <button class="btn btn-outline-secondary" data-m="${item.id}">−</button>
            <span class="btn btn-light">${item.cantidad}</span>
            <button class="btn btn-outline-secondary" data-p="${item.id}">+</button>
          </div>
          <strong>S/ ${(item.p.precio * item.cantidad).toFixed(2)}</strong>
          <button class="btn btn-outline-danger btn-sm" data-x="${item.id}">
            <i class="bi bi-trash"></i>
          </button>
        </div>
      `
    )
    .join('');
}

// ==========================================
// 3. INTERACCIÓN Y GESTIÓN DEL CARRITO
// ==========================================

// Función global para añadir productos al carrito desde el catálogo
function addToCart(id) {
  if (!ensure()) return;

  const carrito = cart();
  const item = carrito.find((producto) => producto.id == id);

  if (item) {
    item.cantidad++; // Si existe, incrementa la cantidad
  } else {
    carrito.push({ id: +id, cantidad: 1 }); // Si es nuevo, lo agrega
  }

  saveCart(carrito);
}

window.addToCart = addToCart;

// Escuchador de eventos en los botones del carrito (+, -, o eliminar)
Q('#cartItems')?.addEventListener('click', (event) => {
  const boton = event.target.closest('button');

  if (!boton) return;

  const id = +(boton.dataset.p || boton.dataset.m || boton.dataset.x);
  const carrito = cart();
  const item = carrito.find((producto) => producto.id === id);

  if (!item) return;

  if (boton.dataset.p) item.cantidad++; // Botón sumar (+)
  if (boton.dataset.m) item.cantidad--; // Botón restar (-)
  if (boton.dataset.x) item.cantidad = 0; // Botón eliminar

  // Guarda únicamente los productos cuya cantidad sea mayor a 0
  saveCart(carrito.filter((producto) => producto.cantidad > 0));
});

// ==========================================
// 4. PROCESO DE CHECKOUT Y PROCESAMIENTO DE COMPRA
// ==========================================

// Al pulsar "Continuar compra", obtiene los datos del usuario desde la API para autocompletar el modal
Q('#checkout')?.addEventListener('click', async () => {
  if (!ensure() || !cart().length) return;

  try {
    const perfil = await api('/usuarios/perfil');
    ['Nombre', 'Correo', 'Telefono', 'Dni', 'Direccion', 'Distrito'].forEach((campo) => {
      const elemento = Q('#c' + campo);
      if (elemento) {
        elemento.value = perfil[campo.toLowerCase()] || '';
      }
    });

    bootstrap.Modal.getOrCreateInstance(Q('#checkoutModal')).show();
  } catch (error) {
    location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=required');
  }
});

// Procesa el formulario de envío: crea el pedido via POST en el backend y limpia el carrito
Q('#checkoutForm')?.addEventListener('submit', async (event) => {
  event.preventDefault();

  if (!event.target.checkValidity()) {
    event.target.classList.add('was-validated');
    return;
  }

  const productosDisponibles = products();
  const items = cart().map((item) => {
    const producto = productosDisponibles.find((actual) => actual.id == item.id);
    return {
      id: producto.id,
      nombre: producto.nombre,
      tienda: producto.tienda,
      precio: producto.precio,
      cantidad: item.cantidad
    };
  });

  const total = items.reduce((suma, item) => suma + item.precio * item.cantidad, 0);

  try {
    // Envío del pedido a la API backend[cite: 2]
    const respuesta = await api('/pedidos', {
      method: 'POST',
      body: JSON.stringify({
        cliente: {
          nombre: Q('#cNombre').value,
          correo: Q('#cCorreo').value,
          telefono: Q('#cTelefono').value,
          dni: Q('#cDni').value,
          direccion: Q('#cDireccion').value,
          distrito: Q('#cDistrito').value,
          referencia: Q('#cReferencia').value,
          metodoPago: Q('#cPago').value
        },
        items,
        total
      })
    });

    // Limpia el carrito local y muestra pantalla de éxito
    localStorage.removeItem('ampCart');
    bootstrap.Modal.getInstance(Q('#checkoutModal')).hide();

    Q('#cartItems').innerHTML = `
      <div class="alert alert-success p-4">
        <h2 class="h4"><i class="bi bi-check-circle-fill me-2"></i>¡Compra exitosa!</h2>
        <p class="mb-2">Pedido <b>${respuesta.numero}</b> confirmado por <b>S/ ${Number(respuesta.total).toFixed(2)}</b>.</p>
        <p class="mb-2">Tu pedido llegará <b>entre hoy y mañana</b>.</p>
        <p class="mb-0">Puedes revisar el estado y el detalle en <a href="historial-compras.html" class="alert-link">Historial de compras</a>.</p>
      </div>
    `;
    Q('#summary').classList.add('d-none');
    updateCommon();
  } catch (error) {
    Q('#checkoutError').innerHTML = `<div class="alert alert-danger">${escapeHtml(error.message)}</div>`;
  }
});

// ==========================================
// 5. HISTORIAL DE COMPRAS
// ==========================================

// Consulta la API (/pedidos) y renderiza las tarjetas con el historial de pedidos del cliente
async function history() {
  if (!Q('#orders') || !ensure()) return;

  try {
    const rows = await api('/pedidos');

    Q('#orders').innerHTML = rows.length
      ? rows
          .map(
            (pedido) => `
              <div class="col-lg-6">
                <article class="card border-0 soft-shadow h-100">
                  <div class="card-body">
                    <div class="d-flex justify-content-between">
                      <h2 class="h5">${pedido.numero}</h2>
                      <span class="badge text-bg-success">${escapeHtml(pedido.estado)}</span>
                    </div>
                    <p class="text-secondary small">${new Date(pedido.created_at).toLocaleString('es-PE')}</p>
                    <ul class="list-group list-group-flush mb-3">
                      ${pedido.items
                        .map(
                          (item) => `
                            <li class="list-group-item px-0 d-flex justify-content-between">
                              <span>${escapeHtml(item.producto_nombre)} × ${item.cantidad}</span>
                              <b>S/ ${Number(item.subtotal).toFixed(2)}</b>
                            </li>
                          `
                        )
                        .join('')}
                    </ul>
                    <div class="d-flex justify-content-between fw-bold">
                      <span>Total</span>
                      <span>S/ ${Number(pedido.total).toFixed(2)}</span>
                    </div>
                    <div class="alert alert-light border mt-3 mb-0">
                      <i class="bi bi-truck me-1"></i>${escapeHtml(pedido.fecha_entrega)}
                    </div>
                  </div>
                </article>
              </div>
            `
          )
          .join('')
      : '<div class="col-12"><div class="alert alert-info">Todavía no tienes compras.</div></div>';
  } catch (error) {
    Q('#orders').innerHTML = '<div class="col-12"><div class="alert alert-danger">No se pudo cargar tu historial.</div></div>';
  }
}

// ==========================================
// 6. GESTIÓN DEL PERFIL DE USUARIO
// ==========================================

// Obtiene los datos del perfil desde el backend y llena los campos del formulario
async function perfil() {
  if (!Q('#profileForm') || !ensure()) return;

  try {
    const perfilUsuario = await api('/usuarios/perfil');

    Q('#pNombre').value = perfilUsuario.nombre;
    Q('#pCorreo').value = perfilUsuario.correo;
    Q('#pTelefono').value = perfilUsuario.telefono || '';
    Q('#pDni').value = perfilUsuario.dni || '';
    Q('#pDireccion').value = perfilUsuario.direccion || '';
    Q('#pDistrito').value = perfilUsuario.distrito || '';

    if (Q('#nombrePerfil')) {
      Q('#nombrePerfil').textContent = perfilUsuario.nombre;
      Q('#correoPerfil').textContent = perfilUsuario.correo;
    }
  } catch (error) {
    location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=required');
  }
}

// Guarda las modificaciones del perfil mediante una petición PUT a la API
Q('#profileForm')?.addEventListener('submit', async (event) => {
  event.preventDefault();

  if (!event.target.checkValidity()) {
    event.target.classList.add('was-validated');
    return;
  }

  try {
    await api('/usuarios/perfil', {
      method: 'PUT',
      body: JSON.stringify({
        nombre: Q('#pNombre').value,
        telefono: Q('#pTelefono').value,
        dni: Q('#pDni').value,
        direccion: Q('#pDireccion').value,
        distrito: Q('#pDistrito').value
      })
    });

    const usuario = currentUser();
    if (usuario) {
      usuario.nombre = Q('#pNombre').value;
      localStorage.setItem('ampUser', JSON.stringify(usuario));
    }

    Q('#profileMsg').innerHTML = '<div class="alert alert-success">Perfil actualizado correctamente.</div>';
    updateCommon();
  } catch (error) {
    Q('#profileMsg').innerHTML = `<div class="alert alert-danger">${escapeHtml(error.message)}</div>`;
  }
});

// ==========================================
// 7. VISTA MI CUENTA / HUB
// ==========================================

// Carga el nombre y correo del usuario en el panel principal
async function cargarCuenta() {
  const nombre = Q('#nombrePerfil');
  const correo = Q('#correoPerfil');

  if (!nombre) return;
  if (!ensure()) return;

  try {
    const usuario = await api('/usuarios/perfil');
    nombre.textContent = usuario.nombre;
    correo.textContent = usuario.correo || '';
  } catch (error) {
    nombre.textContent = 'No disponible';
    correo.textContent = 'No se pudo cargar la cuenta';
  }
}

// Carga un resumen de las últimas 3 compras en el panel principal
async function cargarResumenCompras() {
  const box = Q('#resumenCompras');

  if (!box) return;
  if (!ensure()) return;

  try {
    const pedidos = await api('/pedidos');

    if (!pedidos.length) {
      box.innerHTML = `
        <div class="alert alert-info mb-0">
          Todavía no tienes compras registradas.
          <a href="/modulo-2-catalogo/index.html" class="alert-link">
            Ir al catálogo
          </a>
        </div>
      `;
      return;
    }

    box.innerHTML = pedidos
      .slice(0, 3)
      .map(
        (pedido) => `
          <div class="border-bottom py-3">
            <div class="d-flex justify-content-between align-items-center">
              <strong>${escapeHtml(pedido.numero)}</strong>
              <span class="badge text-bg-success">${escapeHtml(pedido.estado)}</span>
            </div>
            <div class="small text-secondary mt-1">${new Date(pedido.created_at).toLocaleString('es-PE')}</div>
            <div class="d-flex justify-content-between mt-2">
              <span>${pedido.items.length} producto(s)</span>
              <strong>S/ ${Number(pedido.total).toFixed(2)}</strong>
            </div>
          </div>
        `
      )
      .join('');
  } catch (error) {
    box.innerHTML = `
      <div class="alert alert-danger mb-0">
        No se pudieron cargar tus pedidos.
      </div>
    `;
  }
}

// ==========================================
// 8. EVENTOS DE CIERRE DE SESIÓN E INICIALIZACIÓN
// ==========================================

// Asigna la función de cerrar sesión al botón correspondiente
Q('#logout')?.addEventListener('click', logout);

// Ejecución automática al cargar la página
renderCart();
history();
perfil();
cargarCuenta();
cargarResumenCompras();