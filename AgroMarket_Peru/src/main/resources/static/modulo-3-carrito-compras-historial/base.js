const Q=s=>document.querySelector(s);
const cart=()=>JSON.parse(localStorage.getItem('ampCart')||'[]');
const products=()=>window.AgroMarketDataProvider?.obtenerProductos?.()||[];
const logged=()=>!!session();
function ensure(){if(!logged())location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=required');return logged()}
async function readyProducts(){await window.ampProductsReady;return products()}
