window.AMP_PRODUCTS=[];
window.ampProductsReady=fetch('/api/productos').then(r=>r.json()).then(data=>{
 window.AMP_PRODUCTS=Array.isArray(data)?data:[];
 return window.AMP_PRODUCTS;
});
window.AgroMarketDataProvider={
 obtenerProductos:()=>window.AMP_PRODUCTS.slice(),
 cargar:()=>window.ampProductsReady
};
