"use strict";

const botonConsejo = document.querySelector("#boton-consejo");
const consejo = document.querySelector("#consejo");

botonConsejo.addEventListener("click", () => {
    const mostrar = consejo.hidden;

    consejo.hidden = !mostrar;
    botonConsejo.setAttribute("aria-expanded", mostrar);
    botonConsejo.textContent = mostrar
        ? "Ocultar consejo"
        : "Mostrar consejo";
});


const botonTema = document.querySelector("#boton-tema");

botonTema.addEventListener("click", () => {
    const oscuro = document.body.classList.toggle("tema-oscuro");

    botonTema.setAttribute("aria-pressed", oscuro);
    botonTema.textContent = oscuro
        ? "Tema claro"
        : "Cambiar tema";
});