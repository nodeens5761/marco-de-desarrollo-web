# Matriz de pruebas

## Por ancho

| Ancho | Qué observar | Resultado |
| --- | --- | --- |
| 360 x 800 px | Navbar colapsada; una card por fila; modal dentro de la pantalla; sin desplazamiento horizontal. | |
| 768 x 1024 px | Dos cards por fila; tabla operable; espacios consistentes; campos utilizables. | |
| 1024 x 768 px | Distribución equilibrada; navegación visible según breakpoint; texto sin líneas excesivamente largas. | |
| 1440 x 900 px | Contenido limitado por container; tres cards por fila; alineación y jerarquía estables. | |
| Zoom 200 % | Sin solapamiento; controles y modal visibles y operables. | |

## Casos funcionales

| Caso | Procedimiento | Resultado esperado | Resultado |
| --- | --- | --- | --- |
| CP-01 | Recargar y revisar Console y Network. | Cero errores; recursos esenciales con respuesta correcta. | |
| CP-02 | Abrir y cerrar la navbar con teclado. | Estado visible y `aria-expanded` actualizado. | |
| CP-03 | Activar cada enlace interno. | La vista llega a la sección correcta. | |
| CP-04 | Abrir el modal desde cada tarjeta. | La ruta cambia según `data-curso`. | |
| CP-05 | Cerrar el modal con Escape. | El modal cierra y el foco regresa al activador. | |
| CP-06 | Enviar el formulario vacío. | No continúa; muestra errores y enfoca el primer campo inválido. | |
| CP-07 | Ingresar un correo inválido. | El mensaje identifica el problema de formato. | |
| CP-08 | Completar datos válidos. | Aparece confirmación anunciable sin simular almacenamiento real. | |
| CP-09 | Cerrar y reabrir el modal. | Campos, feedback y mensaje vuelven al estado inicial. | |
| CP-10 | Recorrer toda la página con teclado. | Orden lógico, foco visible y ninguna trampa de teclado. | |
