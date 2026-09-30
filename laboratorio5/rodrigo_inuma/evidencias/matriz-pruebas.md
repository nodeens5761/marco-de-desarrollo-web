# Matriz de pruebas APF1

| Caso | Prueba | Resultado esperado | Resultado |
|---|---|---|---|
| CP-01 | Recargar y revisar Console y Network | Sin errores críticos y recursos cargados | Pasa |
| CP-02 | Abrir y cerrar navbar con teclado | Navegación operable y `aria-expanded` actualizado | Pasa |
| CP-03 | Activar enlaces internos | La vista llega a la sección correcta | Pasa |
| CP-04 | Abrir modal desde cada card | El campo Ruta muestra la ruta seleccionada | Pasa |
| CP-05 | Cerrar modal con Escape | Modal cierra y foco vuelve al activador | Pasa |
| CP-06 | Enviar formulario vacío | Se muestran errores y se enfoca el primer inválido | Pasa |
| CP-07 | Escribir correo inválido | Se muestra error de formato | Pasa |
| CP-08 | Completar datos válidos | Aparece confirmación anunciable | Pasa |
| CP-09 | Cerrar y reabrir modal | Formulario y mensajes vuelven al estado inicial | Pasa |
| CP-10 | Recorrer con teclado | Orden lógico y foco visible | Pasa |
| 360 px | Vista móvil | 1 card por fila y sin desplazamiento horizontal | Pasa |
| 768 px | Vista tablet | 2 cards por fila | Pasa |
| 1024 px | Vista intermedia | Distribución equilibrada | Pasa |
| 1440 px | Vista escritorio | 3 cards por fila | Pasa |
| 200 % | Zoom | Controles y modal siguen utilizables | Pasa |
