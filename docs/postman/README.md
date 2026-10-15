# Pruebas de la API con Postman

Importa `TechStore-Pro.postman_collection.json` y `TechStore-Pro.local.postman_environment.json` en Postman y selecciona el entorno **TechStore Pro — Local**. El backend debe estar activo en `http://localhost:8080`.

## Variables

- `baseUrl`: dirección del backend, sin barra final.
- `userEmail`: si está vacío, la solicitud de registro genera un correo único de prueba. Para crear otro usuario, vacía esta variable antes de registrar. Repetir el registro del mismo correo devuelve conflicto.
- `userPassword`: contraseña de la cuenta de prueba; el valor incluido es solo de demostración.
- `adminEmail` y `adminPassword`: introduce localmente las credenciales de tu administrador. No se incluyen credenciales reales.
- Tokens, IDs y estado del pedido se guardan automáticamente al ejecutar las solicitudes anteriores. Usa un entorno seleccionado; estos valores no se guardan en la colección.

## Orden de ejecución

1. **Catálogo público**: lista productos para obtener `productId`, consulta el detalle y prueba filtros y paginación. Se selecciona un producto con stock.
2. **Autenticación de usuario**: registra la cuenta de prueba, inicia sesión y consulta `/auth/me`. Se guarda `userToken`.
3. **Carrito invitado**: obtiene el token de invitado, añade un producto y cambia su cantidad. La transferencia requiere haber iniciado sesión como usuario.
4. **Carrito de usuario y pedido**: consulta o añade productos, cambia cantidades y confirma un pedido. La confirmación descuenta stock y vacía el carrito. Se guarda `orderId` y `orderState`.
5. **Administración**: configura las credenciales y realiza el login. Crea y edita un producto independiente (`adminProductId`), consulta el resumen y los pedidos, y avanza el pedido de prueba hasta entregado. La última solicitud oculta el producto de prueba.
6. **Validaciones y permisos**: comprueba ausencia de autenticación, rol insuficiente, cantidades y filtros inválidos, y producto inexistente. La cuenta de usuario debe tener rol USER.

Ejecuta las solicitudes manualmente. Las solicitudes **Opcional** y **Subir imagen** no forman parte de una ejecución automática completa. Para subir imagen, selecciona un archivo en **Body → form-data → file** (JPEG, PNG o WebP, máximo 5 MB); después puedes consultar la imagen guardada. Postman establece el Content-Type de multipart automáticamente.

Cada solicitud incluye pruebas de estado HTTP. Las respuestas correctas también validan JSON o tipo de imagen; algunas comprueban estructuras y capturan variables. Los errores de seguridad no presuponen un cuerpo JSON.

## Datos que modifican las pruebas

El registro crea cuentas; carrito y pedidos modifican la base de datos local. Confirmar pedido descuenta stock, y avanzar sus estados es definitivo en el flujo de la aplicación. Un producto oculto permanece en la base; una imagen subida permanece en Cloudinary. Usa una base de pruebas para ejecutar repetidamente estos flujos. No se ejecutó ninguna solicitud de escritura durante la creación de esta colección.

## Devoluciones e informe

`Devoluciones.postman_collection.json` cubre las rutas del módulo. La [colección ampliada de evaluación](../../output/pdf/TechStore_Pro_Postman.json) añade autenticación por rol y casos negativos; sus instrucciones están en [la guía de anexos](../../output/pdf/LEEME_Anexos.txt). Sus variables pertenecen a la colección, a diferencia del entorno local descrito arriba. No ejecutes aprobación y rechazo sobre la misma solicitud: son caminos alternativos y los estados finales no se reabren.

No exportes ni subas un entorno que contenga tokens o credenciales reales. El entorno incluido en el proyecto mantiene vacíos los secretos de administrador y los tokens.
