// Integrante 4: autenticación real contra API, registro de clientes y alertas.
const $ = (selector) => document.querySelector(selector);
const user = () => currentUser();
const logged = () => !!session();

function valid(form) {
  form.classList.add('was-validated');
  return form.checkValidity();
}

function toast(message) {
  $('#toastMsg').textContent = message;
  bootstrap.Toast.getOrCreateInstance($('#toast')).show();
}

function selectView(view) {
  ['login', 'alert', 'admin', 'register'].forEach((vista) => {
    $('#view' + vista.charAt(0).toUpperCase() + vista.slice(1))?.classList.toggle('d-none', vista !== view);
  });

  document.querySelectorAll('#authTabs .nav-link').forEach((boton) => {
    boton.classList.toggle('active', boton.dataset.view === view);
  });
}

function fillProducts() {
  const select = $('#alertProducto');

  if (!select) return;

  select.innerHTML =
    '<option value="">Selecciona</option>' +
    AgroMarketDataProvider.obtenerProductos()
      .map(
        (producto) =>
          `<option value="${producto.id}">${escapeHtml(producto.nombre)} · ${escapeHtml(producto.tienda)} · S/ ${producto.precio.toFixed(2)}</option>`
      )
      .join('');

  const id = new URLSearchParams(location.search).get('id');
  if (id) select.value = id;
}

function saveLogin(response) {
  localStorage.setItem('ampSession', response.token);
  localStorage.setItem('ampUser', JSON.stringify(response.user));
  updateCommon();
}

$('#authTabs')?.addEventListener('click', (event) => {
  const boton = event.target.closest('[data-view]');

  if (!boton) return;

  if (boton.dataset.view === 'alert' && !logged()) {
    return (location.href = '?auth=required&open=alert');
  }

  selectView(boton.dataset.view);
});

$('#showRegister')?.addEventListener('click', () => selectView('register'));
$('#backLogin')?.addEventListener('click', () => selectView('login'));

$('#loginForm')?.addEventListener('submit', async (event) => {
  event.preventDefault();

  if (!valid(event.target)) return;

  try {
    const respuesta = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        correo: $('#loginEmail').value,
        password: $('#loginPass').value
      })
    });

    saveLogin(respuesta);
    toast('Inicio de sesión correcto.');

    const query = new URLSearchParams(location.search);
    setTimeout(
      () =>
        (location.href =
          query.get('action') === 'cart'
            ? '/modulo-3-carrito-compras-historial/carrito.html'
            : query.get('action') === 'history'
              ? '/modulo-3-carrito-compras-historial/historial-compras.html'
              : query.get('action') === 'alert'
                ? `/modulo-4-auth-alertas-perfil/alertas.html?id=${query.get('id') || ''}`
                : '/modulo-3-carrito-compras-historial/hub.html'),
      400
    );
  } catch (error) {
    toast(error.message);
  }
});

$('#registerForm')?.addEventListener('submit', async (event) => {
  event.preventDefault();

  if ($('#regPass').value !== $('#regConfirm').value) {
    $('#regConfirm').setCustomValidity('Las contraseñas no coinciden');
  } else {
    $('#regConfirm').setCustomValidity('');
  }

  if (!valid(event.target)) return;

  try {
    await api('/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        nombre: $('#regNombre').value,
        correo: $('#regCorreo').value,
        password: $('#regPass').value
      })
    });

    toast('Cuenta creada. Ahora inicia sesión.');
    event.target.reset();
    selectView('login');
  } catch (error) {
    toast(error.message);
  }
});

$('#adminForm')?.addEventListener('submit', async (event) => {
  event.preventDefault();

  if (!valid(event.target)) return;

  try {
    const respuesta = await api('/auth/admin', {
      method: 'POST',
      body: JSON.stringify({
        usuario: $('#adminUser').value,
        password: $('#adminPass').value
      })
    });

    saveLogin(respuesta);
    localStorage.setItem('userRole', 'admin');
    location.href = '/modulo-1-datos-backend/index.html';
  } catch (error) {
    toast(error.message);
  }
});

$('#alertForm')?.addEventListener('submit', async (event) => {
  event.preventDefault();

  if (!logged()) return (location.href = '?auth=required&open=alert');
  if (!valid(event.target)) return;

  const producto = AgroMarketDataProvider.obtenerProductos().find((item) => item.id == $('#alertProducto').value);

  try {
    await api('/alertas', {
      method: 'POST',
      body: JSON.stringify({
        productoId: producto.id,
        productoNombre: producto.nombre,
        precioObjetivo: +$('#alertPrecio').value
      })
    });

    toast('Alerta registrada correctamente.');
    event.target.reset();
    loadAlerts();
  } catch (error) {
    toast(error.message);
  }
});

async function loadAlerts() {
  const box = $('#alerts') || $('#alertList');

  if (!box) return;

  if (!logged()) {
    location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=required');
    return;
  }

  try {
    const rows = await api('/alertas');

    box.innerHTML = rows.length
      ? rows
          .map(
            (alerta) => `
              <div class="${box.id === 'alerts' ? 'col-md-6' : ''}">
                <div class="card border-0 soft-shadow h-100">
                  <div class="card-body">
                    <span class="badge text-bg-success">Activa</span>
                    <h2 class="h6 mt-2">${escapeHtml(alerta.producto_nombre)}</h2>
                    <p class="mb-1">Avisar al llegar a <b>S/ ${Number(alerta.precio_objetivo).toFixed(2)}</b></p>
                    <small class="text-secondary">Creada el ${new Date(alerta.created_at).toLocaleDateString('es-PE')}</small>
                  </div>
                </div>
              </div>
            `
          )
          .join('')
      : '<div class="alert alert-info">No tienes alertas creadas.</div>';
  } catch (error) {
    box.innerHTML = '<div class="alert alert-danger">No se pudieron cargar las alertas.</div>';
  }
}

const queryString = new URLSearchParams(location.search);

if (queryString.get('auth') === 'required') {
  $('#urlAlert').innerHTML =
    '<div class="alert alert-danger fw-semibold">⚠️ Para acceder a los historiales, alertas o compras, primero debes iniciar sesión.</div>';
}

if (queryString.get('open') === 'alert') {
  selectView('alert');
}

if (queryString.get('auth') === 'admin') {
  selectView('admin');
}

fillProducts();

// Solo carga las alertas automáticamente dentro de alertas.html.
if ($('#alerts')) loadAlerts();