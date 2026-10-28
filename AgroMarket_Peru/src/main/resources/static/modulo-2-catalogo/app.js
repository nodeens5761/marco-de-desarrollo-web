const $=s=>document.querySelector(s);
const cards=[...document.querySelectorAll('[data-product-card]')],buscar=$('#buscar'),tienda=$('#tienda'),categoria=$('#categoria');
const tiendas=[...new Set(cards.flatMap(c=>(c.dataset.tiendas||'').split('|').filter(Boolean)))].sort();
tiendas.forEach(t=>tienda.insertAdjacentHTML('beforeend',`<option value="${escapeHtml(t)}">${escapeHtml(t)}</option>`));
function render(){
 const q=buscar.value.trim().toLowerCase(),cat=categoria.value,t=tienda.value;
 let shown=0;
 cards.forEach(c=>{const stores=(c.dataset.tiendas||'').split('|');const ok=(!q||`${c.dataset.nombre} ${stores.join(' ')}`.toLowerCase().includes(q))&&(!cat||c.dataset.categoria===cat)&&(!t||stores.includes(t));c.classList.toggle('d-none',!ok);if(ok)shown++});
 $('#resultado').textContent=`${shown} productos comparables`;
}
document.querySelector('#catalogo').addEventListener('click',e=>{const b=e.target.closest('[data-action]');if(!b)return;if(b.dataset.action==='cart'){if(!session())return location.href=`/modulo-4-auth-alertas-perfil/index.html?auth=required&action=cart&id=${b.dataset.id}`;return window.addToCart(b.dataset.id)}if(b.dataset.action==='alert'){if(!session())return location.href=`/modulo-4-auth-alertas-perfil/index.html?auth=required&action=alert&id=${b.dataset.id}`;location.href=`/modulo-4-auth-alertas-perfil/index.html?action=alert&id=${b.dataset.id}`}});
[buscar,categoria,tienda].forEach(e=>e.addEventListener('input',render));$('#limpiar').addEventListener('click',()=>{buscar.value='';categoria.value='';tienda.value='';render()});
const q=new URLSearchParams(location.search);buscar.value=q.get('q')||'';categoria.value=q.get('categoria')||'';render();
