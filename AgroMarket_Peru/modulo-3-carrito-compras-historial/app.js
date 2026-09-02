// Integrante 3: carrito, checkout, historial y perfil del cliente.
const Q = (selector) => document.querySelector(selector);
const cart = () => JSON.parse(localStorage.getItem('ampCart') || '[]');
const saveCart = (carrito) => {
  localStorage.setItem('ampCart', JSON.stringify(carrito));
  updateCommon();
  renderCart();
};
const products = () => window.AgroMarketDataProvider?.obtenerProductos?.() || [];
const logged = () => !!localStorage.getItem('ampSession');

function ensure() {
  if (!logged()) {
    location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=required');
  }

  return logged();
}

function renderCart() {
  const box = Q('#cartItems');

  if (!box) return;
  if (!logged()) return ensure();

  const rows = cart()
    .map((item) => ({ ...item, p: products().find((producto) => producto.id == item.id) }))
    .filter((item) => item.p);

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

  Q('#summary').classList.remove('d-none');
  Q('#units').textContent = rows.reduce((suma, item) => suma + item.cantidad, 0);

  const total = rows.reduce((suma, item) => suma + item.p.precio * item.cantidad, 0);
  Q('#total').textContent = `S/ ${total.toFixed(2)}`;

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

function addToCart(id) {
  if (!ensure()) return;

  const carrito = cart();
  const item = carrito.find((producto) => producto.id == id);

  if (item) {
    item.cantidad++;
  } else {
    carrito.push({ id: +id, cantidad: 1 });
  }

  saveCart(carrito);
}

window.addToCart = addToCart;

Q('#cartItems')?.addEventListener('click', (event) => {
  const boton = event.target.closest('button');

  if (!boton) return;

  const id = +(boton.dataset.p || boton.dataset.m || boton.dataset.x);
  const carrito = cart();
  const item = carrito.find((producto) => producto.id === id);

  if (!item) return;

  if (boton.dataset.p) item.cantidad++;
  if (boton.dataset.m) item.cantidad--;
  if (boton.dataset.x) item.cantidad = 0;

  saveCart(carrito.filter((producto) => producto.cantidad > 0));
});

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

Q('#logout')?.addEventListener('click', logout);

renderCart();
history();
perfil();
cargarCuenta();
cargarResumenCompras();