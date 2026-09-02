// Componentes comunes del sistema: navegación, pie, sesión y helpers de API.
const API='/api';
const session=()=>localStorage.getItem('ampSession');
const currentUser=()=>{try{return JSON.parse(localStorage.getItem('ampUser')||'null')}catch{return null}};
function escapeHtml(v=''){return String(v).replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]))}
function renderCommon(){
 if(document.body.dataset.admin==='true'){
    const nav=document.querySelector('#commonNav');

    if(nav){
        nav.innerHTML=`
            <nav class="navbar navbar-dark bg-dark fixed-top shadow-sm">
                <div class="container">
                    <span class="navbar-brand fw-bold">
                        <i class="bi bi-basket2-fill me-2"></i>
                        AgroMarketPeru
                    </span>

                    <button id="adminLogout"
                            class="btn btn-outline-light">
                        <i class="bi bi-box-arrow-right me-1"></i>
                        Cerrar sesión
                    </button>
                </div>
            </nav>`;

        document.querySelector('#adminLogout')
            ?.addEventListener('click',logout);
    }

    return;
 }
 document.querySelector('#commonNav')&&(document.querySelector('#commonNav').innerHTML=`<nav class="navbar navbar-expand-lg navbar-dark bg-dark fixed-top shadow-sm"><div class="container"><a class="navbar-brand fw-bold" href="/index.html"><i class="bi bi-basket2-fill me-2"></i>AgroMarketPeru</a><button class="navbar-toggler" data-bs-toggle="collapse" data-bs-target="#mainNav"><span class="navbar-toggler-icon"></span></button><div id="mainNav" class="collapse navbar-collapse"><ul class="navbar-nav ms-auto align-items-lg-center gap-lg-1"><li class="nav-item"><a class="nav-link" href="/index.html">Inicio</a></li><li class="nav-item"><a class="nav-link" href="/modulo-2-catalogo/index.html">Catálogo</a></li><li class="nav-item"><a class="nav-link position-relative" href="/modulo-3-carrito-compras-historial/carrito.html"><i class="bi bi-cart3 fs-5"></i><span id="commonCartBadge" class="badge rounded-pill text-bg-success position-absolute top-0 start-100 translate-middle d-none">0</span></a></li><li class="nav-item"><a id="commonUser" class="btn btn-success ms-lg-2" href="/modulo-4-auth-alertas-perfil/index.html">Iniciar Sesión / Registrarse</a></li></ul></div></div></nav>`);
 document.querySelector('#commonFooter')&&(document.querySelector('#commonFooter').innerHTML=`<footer class="bg-dark text-light mt-5"><div class="container py-5"><div class="row g-4"><div class="col-lg-4"><h2 class="h5 fw-bold"><i class="bi bi-basket2-fill me-2"></i>AgroMarketPeru</h2><p class="text-secondary">Compara precios de productos de primera necesidad y compra con confianza.</p></div><div class="col-6 col-lg-2"><h3 class="h6 fw-bold">Nosotros</h3><a class="footer-link d-block mb-2" href="/index.html#nosotros">Quiénes somos</a><a class="footer-link d-block" href="/modulo-4-auth-alertas-perfil/index.html">Contacto</a></div><div class="col-6 col-lg-3"><h3 class="h6 fw-bold">Categorías</h3><a class="footer-link d-block mb-2" href="/modulo-2-catalogo/index.html?categoria=Abarrotes">Abarrotes</a><a class="footer-link d-block mb-2" href="/modulo-2-catalogo/index.html?categoria=Frutas%20y%20Verduras">Frutas y Verduras</a><a class="footer-link d-block" href="/modulo-2-catalogo/index.html?categoria=Lácteos">Lácteos</a></div><div class="col-lg-3"><h3 class="h6 fw-bold">Síguenos</h3><div class="d-flex gap-3 fs-4"><a class="footer-link" href="#"> <i class="bi bi-facebook"></i></a><a class="footer-link" href="#"><i class="bi bi-instagram"></i></a><a class="footer-link" href="#"><i class="bi bi-tiktok"></i></a></div></div></div><hr class="border-secondary my-4"><div class="d-flex flex-column flex-md-row justify-content-between small text-secondary"><span>© 2026 AgroMarketPeru. Todos los derechos reservados.</span><span>Hecho en Perú 🇵🇪</span></div></div></footer>`);
 updateCommon();
}
function updateCommon(){
    const u=currentUser();
    const b=document.querySelector('#commonCartBadge');
    const n=JSON.parse(localStorage.getItem('ampCart')||'[]')
        .reduce((s,i)=>s+i.cantidad,0);

    if(b){
        b.textContent=n;
        b.classList.toggle('d-none',!n);
    }

    const a=document.querySelector('#commonUser');

    if(a){
        if(u){
            a.textContent=`${u.nombre} · Mi cuenta`;
            a.href='/modulo-3-carrito-compras-historial/hub.html';
        }else{
            a.textContent='Iniciar Sesión / Registrarse';
            a.href='/modulo-4-auth-alertas-perfil/index.html';
        }
    }
}
async function api(url,opts={}){const headers={'Content-Type':'application/json',...(opts.headers||{})};if(session())headers.Authorization='Bearer '+session();const r=await fetch(API+url,{...opts,headers});const d=await r.json().catch(()=>({}));if(!r.ok)throw new Error(d.error||'Error de servidor');return d}
function requireUser(dest='/modulo-4-auth-alertas-perfil/index.html?auth=required'){if(!session()){location.href=dest;return false}return true}
function logout(){localStorage.removeItem('ampSession');localStorage.removeItem('ampUser');location.href='/index.html'}
document.addEventListener('DOMContentLoaded',renderCommon);
