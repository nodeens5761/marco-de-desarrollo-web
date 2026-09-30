const $=s=>document.querySelector(s);
const admin=()=>currentUser()?.rol==='admin';
if(document.body.dataset.admin==='true'&&!admin())location.replace('/modulo-4-auth-alertas-perfil/index.html?auth=admin');
async function render(){
 const tabla=$('#tabla');if(!tabla)return;
 const productos=await api('/productos');
 $('#count').textContent=`${productos.length} productos`;
 tabla.innerHTML=productos.map(p=>`<tr><td>${escapeHtml(p.nombre)}</td><td><span class="badge text-bg-light border">${escapeHtml(p.categoria)}</span></td><td>${escapeHtml(p.tienda)}</td><td>S/ ${Number(p.precio).toFixed(2)}</td></tr>`).join('');
}
$('#formProducto')?.addEventListener('submit',async e=>{
 e.preventDefault();if(!admin()||!e.target.checkValidity())return;
 const v=s=>$(s).value.trim();
 try{await api('/productos',{method:'POST',body:JSON.stringify({nombre:v('#nombre'),categoria:v('#categoria'),tienda:v('#tienda'),precio:+v('#precio'),imagen:v('#imagen'),url:v('#url')})});e.target.reset();$('#msg').innerHTML='<div class="alert alert-success">Producto guardado en MySQL.</div>';await render();await window.ampProductsReady}catch(err){$('#msg').innerHTML=`<div class="alert alert-danger">${escapeHtml(err.message)}</div>`}
});
async function pedidos(){
 const box=$('#orders');if(!box)return;
 try{const rows=await api('/admin/pedidos');box.innerHTML=rows.length?rows.map(p=>`<div class="border-bottom py-2"><b>${escapeHtml(p.numero)}</b> · ${escapeHtml(p.nombre_cliente)} · S/ ${Number(p.total).toFixed(2)} <span class="badge text-bg-success">${escapeHtml(p.estado)}</span><div class="small text-secondary">${escapeHtml(p.fecha_entrega)}</div></div>`).join(''):'<div class="text-secondary">Aún no hay pedidos.</div>'}catch{box.innerHTML='<div class="alert alert-warning">No se pudo consultar la API.</div>'}
}
if(document.body.dataset.admin==='true')Promise.all([render(),pedidos()]);
