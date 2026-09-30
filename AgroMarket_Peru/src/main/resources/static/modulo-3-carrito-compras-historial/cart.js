async function renderCart(){
 const box=Q('#cartItems');if(!box||!ensure())return;const ps=await readyProducts();
 const rows=cart().map(i=>({...i,p:ps.find(p=>p.id==i.id)})).filter(i=>i.p);
 if(!rows.length){box.innerHTML='<div class="alert alert-info"><i class="bi bi-cart-x me-2"></i>Tu carrito está vacío. <a class="alert-link ms-1" href="/modulo-2-catalogo/index.html">Ir al catálogo</a></div>';Q('#summary')?.classList.add('d-none');return}
 Q('#summary')?.classList.remove('d-none');Q('#units').textContent=rows.reduce((s,i)=>s+i.cantidad,0);Q('#total').textContent=`S/ ${rows.reduce((s,i)=>s+i.p.precio*i.cantidad,0).toFixed(2)}`;
 box.innerHTML=rows.map(i=>`<div class="d-flex gap-3 align-items-center border-bottom py-3 flex-wrap"><img class="cart-img rounded-3" src="${i.p.imagen}" alt="${escapeHtml(i.p.nombre)}"><div class="flex-grow-1"><h2 class="h6 mb-1">${escapeHtml(i.p.nombre)}</h2><small class="text-secondary">${escapeHtml(i.p.tienda)} · S/ ${i.p.precio.toFixed(2)}</small></div><div class="btn-group btn-group-sm"><button class="btn btn-outline-secondary" data-m="${i.id}">−</button><span class="btn btn-light">${i.cantidad}</span><button class="btn btn-outline-secondary" data-p="${i.id}">+</button></div><strong>S/ ${(i.p.precio*i.cantidad).toFixed(2)}</strong><button class="btn btn-outline-danger btn-sm" data-x="${i.id}"><i class="bi bi-trash"></i></button></div>`).join('');
}
function saveCart(value){localStorage.setItem('ampCart',JSON.stringify(value));updateCommon();renderCart()}
function addToCart(id){if(!ensure())return;const c=cart(),item=c.find(i=>i.id==id);item?item.cantidad++:c.push({id:+id,cantidad:1});saveCart(c);}
window.addToCart=addToCart;
Q('#cartItems')?.addEventListener('click',e=>{const b=e.target.closest('button');if(!b)return;const id=+(b.dataset.p||b.dataset.m||b.dataset.x),c=cart(),i=c.find(x=>x.id===id);if(!i)return;if(b.dataset.p)i.cantidad++;if(b.dataset.m)i.cantidad--;if(b.dataset.x)i.cantidad=0;saveCart(c.filter(x=>x.cantidad>0))});
window.ampProductsReady.then(renderCart);
