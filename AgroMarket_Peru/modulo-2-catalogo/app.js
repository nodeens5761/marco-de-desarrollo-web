// Módulo 2: catálogo, filtros y acciones de compra/alerta del cliente.
// Se reutiliza la fuente de datos del módulo 1 para mostrar productos comparables.
const $ = (selector) => document.querySelector(selector);
const catalogo = $('#catalogo');
const buscar = $('#buscar');
const tienda = $('#tienda');
const categoria = $('#categoria');

// Comprueba si existe una sesión activa del usuario para permitir acciones.
function hasSession() {
  return !!localStorage.getItem('ampSession');
}

// Redirige al usuario al módulo de autenticación si intenta ejecutar una acción protegida.
function goAuth(action, id) {
  location.href = `/modulo-4-auth-alertas-perfil/index.html?auth=required&action=${action}&id=${id}`;
}

// Obtiene la lista actual de productos desde el proveedor compartido.
const productos = () => AgroMarketDataProvider.obtenerProductos();

// Carga las tiendas disponibles para filtrar el catálogo.
function tiendas() {
  [...new Set(productos().map((producto) => producto.tienda))]
    .sort()
    .forEach((tiendaActual) => {
      tienda.insertAdjacentHTML('beforeend', `<option>${escapeHtml(tiendaActual)}</option>`);
    });
}

// Genera las tarjetas del catálogo según los filtros actuales.
function render() {
  const textoBusqueda = buscar.value.toLowerCase();
  const categoriaSeleccionada = categoria.value;
  const tiendaSeleccionada = tienda.value;

  const productosFiltrados = productos().filter(
    (producto) =>
      (producto.nombre.toLowerCase().includes(textoBusqueda) ||
        producto.tienda.toLowerCase().includes(textoBusqueda)) &&
      (!categoriaSeleccionada || producto.categoria === categoriaSeleccionada) &&
      (!tiendaSeleccionada || producto.tienda === tiendaSeleccionada)
  );

  $('#resultado').textContent = `${productosFiltrados.length} resultados`;

  catalogo.innerHTML = productosFiltrados.length
    ? productosFiltrados
        .map(
          (producto) => `
            <div class="col">
              <article class="card h-100 border-0 soft-shadow product-card">
                <img src="${producto.imagen}" class="card-img-top" alt="${escapeHtml(producto.nombre)}">
                <div class="card-body d-flex flex-column">
                  <span class="badge text-bg-light border align-self-start mb-2">
                    ${escapeHtml(producto.categoria)} · ${escapeHtml(producto.tienda)}
                  </span>
                  <h3 class="h6">${escapeHtml(producto.nombre)}</h3>
                  <strong class="fs-4 mb-3">S/ ${producto.precio.toFixed(2)}</strong>
                  <div class="mt-auto d-grid gap-2">
                    <button class="btn btn-success" data-action="cart" data-id="${producto.id}">
                      <i class="bi bi-cart-plus me-1"></i>Agregar al carrito
                    </button>
                    <a class="btn btn-outline-success" href="${producto.url}" target="_blank" rel="noopener noreferrer">
                      <i class="bi bi-shop-window me-1"></i>Ir a ${escapeHtml(producto.tienda)}
                    </a>
                    <button class="btn btn-warning" data-action="alert" data-id="${producto.id}">
                      <i class="bi bi-bell me-1"></i>Crear Alerta
                    </button>
                  </div>
                </div>
              </article>
            </div>
          `
        )
        .join('')
    : '<div class="col-12"><div class="alert alert-warning">No encontramos coincidencias.</div></div>';
}

// Maneja los eventos de los botones de cada tarjeta: carrito y alerta.
catalogo.addEventListener('click', (e) => {
  const boton = e.target.closest('[data-action]');

  if (!boton) return;

  const id = boton.dataset.id;

  if (boton.dataset.action === 'cart') {
    if (!hasSession()) return goAuth('cart', id);

    window.addToCart(id);
    boton.textContent = 'Añadido';
    return;
  }

  if (!hasSession()) return goAuth(boton.dataset.action, id);

  if (boton.dataset.action === 'alert') {
    location.href = `/modulo-4-auth-alertas-perfil/index.html?action=alert&id=${id}`;
  }
});

// Actualiza el catálogo cada vez que cambian los filtros.
[buscar, categoria, tienda].forEach((elemento) => {
  elemento.addEventListener('input', render);
});

// Botón para limpiar los filtros y volver al estado inicial.
$('#limpiar').addEventListener('click', () => {
  buscar.value = '';
  categoria.value = '';
  tienda.value = '';
  render();
});

// Carga parámetros de búsqueda desde la URL y renderiza el catálogo inicial.
const url = new URLSearchParams(location.search);
buscar.value = url.get('q') || '';
categoria.value = url.get('categoria') || '';
tiendas();
render();
