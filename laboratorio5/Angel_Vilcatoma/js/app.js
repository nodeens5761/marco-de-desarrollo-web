"use strict";

const form = document.querySelector("#formRegistro");
const estado = document.querySelector("#estadoFormulario");
const modal = document.querySelector("#modalRegistro");
const curso = document.querySelector("#curso");
const anio = document.querySelector("#anio");

if (modal && curso) {
  modal.addEventListener("show.bs.modal", (event) => {
    const boton = event.relatedTarget;
    curso.value = boton?.dataset.curso ?? "";
  });

  modal.addEventListener("hidden.bs.modal", () => {
    form?.reset();
    form?.classList.remove("was-validated");
    estado?.classList.add("d-none");
    if (estado) estado.textContent = "";
  });
}

if (form && estado) {
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    form.classList.add("was-validated");
    if (!form.checkValidity()) {
      form.querySelector(":invalid")?.focus();
      return;
    }
    estado.textContent = "Registro de demostración completado correctamente.";
    estado.classList.remove("d-none");
  });
}

if (anio) anio.textContent = new Date().getFullYear();
