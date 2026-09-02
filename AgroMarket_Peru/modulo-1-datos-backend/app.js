
//  se crea un proveedor local de productos
window.AgroMarketDataProvider = (() => {
  // Array base con productos  del negocio.
  const p = [
    {
      id: 1,
      nombre: 'Arroz Costeño Saco 5kg',
      categoria: 'Abarrotes',
      tienda: 'Plaza Vea',
      precio: 21.5,
      imagen: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.plazavea.com.pe/'
    },
    {
      id: 2,
      nombre: 'Arroz Costeño Saco 5kg',
      categoria: 'Abarrotes',
      tienda: 'Tottus',
      precio: 22.9,
      imagen: 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.tottus.com.pe/'
    },
    {
      id: 3,
      nombre: 'Aceite Primor 1L',
      categoria: 'Abarrotes',
      tienda: 'Tottus',
      precio: 9.7,
      imagen: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.tottus.com.pe/'
    },
    {
      id: 4,
      nombre: 'Aceite Primor 1L',
      categoria: 'Abarrotes',
      tienda: 'Wong',
      precio: 10.4,
      imagen: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.wong.pe/'
    },
    {
      id: 5,
      nombre: 'Leche Gloria Six Pack',
      categoria: 'Lácteos',
      tienda: 'Metro',
      precio: 23.5,
      imagen: 'https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.metro.pe/'
    },
    {
      id: 6,
      nombre: 'Leche Gloria Six Pack',
      categoria: 'Lácteos',
      tienda: 'Plaza Vea',
      precio: 24.2,
      imagen: 'https://images.unsplash.com/photo-1563636619-e9143da7973b?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.plazavea.com.pe/'
    },
    {
      id: 7,
      nombre: 'Papa Blanca 1kg',
      categoria: 'Frutas y Verduras',
      tienda: 'Metro',
      precio: 4.9,
      imagen: 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.metro.pe/'
    },
    {
      id: 8,
      nombre: 'Plátano de Seda 1kg',
      categoria: 'Frutas y Verduras',
      tienda: 'Wong',
      precio: 6.2,
      imagen: 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=700&q=80',
      url: 'https://www.wong.pe/'
    }
  ];

  // obtener lista y agregar nuevos productos.
  return {
    obtenerProductos: () => p.slice(),
    agregarProducto: (x) => p.push({ ...x, id: Date.now() })
  };
})();

// Verificampos si el usuario  tiene permisos de admi
const protectedAdmin = () => {
  try {
    return JSON.parse(localStorage.getItem('ampUser') || 'null')?.rol === 'admin';
  } catch {
    return false;
  }
};

// Si no es admi, redirige a  autenticación.
if (document.body.dataset.admin === 'true' && !protectedAdmin()) {
  location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=admin');
}

// Renderiza la tabla de productos del panel administrador.
function render() {
  const tabla = document.querySelector('#tabla');

  if (!tabla) return;

  const productos = AgroMarketDataProvider.obtenerProductos();
  const count = document.querySelector('#count');

  if (count) {
    count.textContent = `${productos.length} productos`;
  }

  tabla.innerHTML = productos
    .map(
      (producto) => `
        <tr>
          <td>${escapeHtml(producto.nombre)}</td>
          <td>
            <span class="badge text-bg-light border">${escapeHtml(producto.categoria)}</span>
          </td>
          <td>${escapeHtml(producto.tienda)}</td>
          <td>S/ ${producto.precio.toFixed(2)}</td>
        </tr>
      `
    )
    .join('');
}

// Captura el formulario para registrar nuevos productos en memoria.
document.querySelector('#formProducto')?.addEventListener('submit', (e) => {
  e.preventDefault();

  if (!e.target.checkValidity()) {
    e.target.classList.add('was-validated');
    return;
  }

  const leerValor = (selector) => document.querySelector(selector).value.trim();

  AgroMarketDataProvider.agregarProducto({
    nombre: leerValor('#nombre'),
    categoria: leerValor('#categoria'),
    tienda: leerValor('#tienda'),
    precio: +leerValor('#precio'),
    imagen: leerValor('#imagen'),
    url: leerValor('#url')
  });

  e.target.reset();
  document.querySelector('#msg').innerHTML = '<div class="alert alert-success">Producto agregado a la memoria.</div>';
  render();
});

// Consulta los pedidos recientes 
async function pedidos() {
  const contenedor = document.querySelector('#orders');

  if (!contenedor) return;

  try {
    const respuesta = await api('/admin/pedidos');

    contenedor.innerHTML = respuesta.length
      ? respuesta
          .map(
            (pedido) => `
              <div class="border-bottom py-2">
                <b>${pedido.numero}</b> · ${escapeHtml(pedido.nombre_cliente)} · S/ ${Number(pedido.total).toFixed(2)}
                <span class="badge text-bg-success">${escapeHtml(pedido.estado)}</span>
                <div class="small text-secondary">${escapeHtml(pedido.fecha_entrega)}</div>
              </div>
            `
          )
          .join('')
      : '<div class="text-secondary">Aún no hay pedidos.</div>';
  } catch (error) {
    contenedor.innerHTML = '<div class="alert alert-warning">No se pudo consultar la API.</div>';
  }
}

if (document.body.dataset.admin === 'true') {
  render();
  pedidos();
}
