"use strict";

const modal = document.querySelector("#modalRegistro");
const form = document.querySelector("#formRegistro");
const estado = document.querySelector("#estadoFormulario");
const campoCurso = document.querySelector("#curso");
const campoNombre = document.querySelector("#nombre");
const anio = document.querySelector("#anio");
const menu = document.querySelector("#menuPrincipal");
const enlaces = document.querySelectorAll("#menuPrincipal .nav-link");

if (anio) {
  anio.textContent = new Date().getFullYear();
}

const marcarEnlace = (id) => {
  enlaces.forEach((enlace) => {
    const activo = enlace.getAttribute("href") === `#${id}`;
    enlace.classList.toggle("active", activo);
    if (activo) {
      enlace.setAttribute("aria-current", "location");
    } else {
      enlace.removeAttribute("aria-current");
    }
  });
};

enlaces.forEach((enlace) => {
  enlace.addEventListener("click", () => {
    marcarEnlace(enlace.getAttribute("href").slice(1));
    if (menu) {
      bootstrap.Collapse.getInstance(menu)?.hide();
    }
  });
});

const secciones = document.querySelectorAll("main > section[id]");

if ("IntersectionObserver" in window) {
  const observador = new IntersectionObserver((entradas) => {
    entradas.forEach((entrada) => {
      if (entrada.isIntersecting) {
        marcarEnlace(entrada.target.id);
      }
    });
  }, { rootMargin: "-40% 0px -55% 0px" });

  secciones.forEach((seccion) => observador.observe(seccion));
}

if (modal && form && estado && campoCurso) {
  modal.addEventListener("show.bs.modal", (event) => {
    campoCurso.value = event.relatedTarget?.dataset.curso ?? "";
  });

  modal.addEventListener("hidden.bs.modal", () => {
    form.reset();
    form.classList.remove("was-validated");
    estado.textContent = "";
    estado.classList.add("d-none");
  });

  form.addEventListener("submit", (event) => {
    event.preventDefault();

    if (campoNombre) {
      campoNombre.value = campoNombre.value.trim();
    }
    form.classList.add("was-validated");

    if (!form.checkValidity()) {
      form.querySelector(":invalid")?.focus();
      return;
    }

    const nombre = new FormData(form).get("nombre");
    estado.textContent = `Registro de demostración completado. Gracias, ${nombre}. Tu interés en «${campoCurso.value}» no se envió ni se almacenó.`;
    estado.classList.remove("d-none");
  });
}
