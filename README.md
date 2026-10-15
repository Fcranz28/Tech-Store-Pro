# TechStore Pro

La configuración del contenedor y la conexión PostgreSQL de Supabase están documentadas en [Despliegue del backend con Podman](docs/despliegue-backend.md).
El backend está desplegado en Render; su servicio Docker está definido en [render.yaml](render.yaml) y los pasos están en [docs/render.md](docs/render.md).

## Repositorios y despliegues

- Backend: [Tech-Store-Pro, rama codex/render-backend](https://github.com/Fcranz28/Tech-Store-Pro/tree/codex/render-backend).
- Frontend independiente: [Front_end_Tech_Store_Pro, rama main](https://github.com/Fcranz28/Front_end_Tech_Store_Pro).
- Aplicación: [techstorepro.vercel.app](https://techstorepro.vercel.app/).
- API: [techstore-pro-backend.onrender.com](https://techstore-pro-backend.onrender.com).
- Disponibilidad y conexión a BD: [readiness](https://techstore-pro-backend.onrender.com/actuator/health/readiness).
- [Informe técnico del 15/10/2026](output/pdf/TechStore_Pro_Informe_Despliegue.pdf), [anexos](output/pdf/TechStore_Pro_Informe_y_Anexos.zip) y [capturas originales](output/pdf/evidencias/).

El código Angular se mantiene en su propio repositorio. La carpeta local `Tech_Store_Frontend` puede estar en cualquier directorio; sustituye las rutas de ejemplo por las de tu equipo.

Mini e-commerce fullstack de accesorios tecnológicos. Implementa autenticación JWT, catálogo, carrito persistente, confirmación de pedidos con control de stock y administración de productos y pedidos. El alcance original está en [docs/ENUNCIADO.md](docs/ENUNCIADO.md).

## Requisitos

- Java 17 o superior, disponible en `PATH` como `java` y `javac`.
- Node.js compatible con Angular 21 (el proyecto se preparó con Node 24.14.1).
- pnpm 10.28.0, usado por el workflow del frontend, o compatible con su lockfile.
- PostgreSQL 17 o superior (se comprobó con PostgreSQL 18 local).

El repositorio incluye Maven Wrapper (`mvnw.cmd` en Windows; `./mvnw` en Unix), por lo que no hace falta instalar Maven por separado. Node.js y pnpm se instalan en la máquina; las dependencias Angular se instalan localmente en `Tech_Store_Frontend/`.

En este equipo se encontró Java 17 en `C:\Users\Franz\.jdks\corretto-17.0.20.1`, aunque no está en el `PATH`. Para usarlo en una sesión de PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Users\Franz\.jdks\corretto-17.0.20.1'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

## Instalación

```powershell
Set-Location 'C:\Users\Franz\Documents\Tech_Store_Frontend'
pnpm install --frozen-lockfile
```

Para descargar las dependencias Java desde la raíz:

```powershell
.\mvnw.cmd dependency:go-offline
```

## Ejecución

El backend carga automáticamente el archivo `.env` de la raíz. Copia [`.env.example`](.env.example) a `.env` y configura `DB_URL`, `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`. `.env` está ignorado por Git; nunca subas credenciales reales. En este equipo ya se creó la base local `techstore_pro` y el `.env` correspondiente.

Si no tienes PostgreSQL local, puedes iniciar el contenedor opcional con Docker Compose. Expone el puerto **5433** para evitar conflictos con una instalación local; en ese caso usa `DB_URL=jdbc:postgresql://localhost:5433/techstore_pro` en `.env`:

```powershell
docker compose up -d postgres
```

Si usas tu propia instalación, crea la base `techstore_pro` y ajusta los valores del `.env`. Flyway creará las tablas y cuatro productos de demostración al arrancar. Para generar una clave JWT aleatoria de al menos 32 bytes en PowerShell:

Las imágenes del catálogo y del encabezado se sirven desde Cloudinary; el logo y el favicon se conservan en `Tech_Store_Frontend/public/assets`. La migración `V5__cloudinary_product_images.sql` guarda las URLs recortadas de cada producto en `productos.imagen_url`, tanto para instalaciones nuevas como para productos existentes que aún usen la imagen local. El administrador puede indicar una URL de imagen al crear o editar otros productos.

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

Copia el resultado en `JWT_SECRET` del `.env`. Para otra URL del frontend, ajusta `APP_FRONTEND_ORIGIN` (por defecto, `http://localhost:4200`). Usa exactamente `localhost` al abrir Angular para que coincida con el origen CORS configurado.

Para acceder al panel admin, establece `ADMIN_EMAIL` y `ADMIN_PASSWORD` en `.env` **antes del primer arranque**. El backend crea esa cuenta con rol `ADMIN` si no existe. En este equipo ya hay una cuenta de administrador local; consulta los valores en tu `.env`. El arranque no cambia la contraseña de una cuenta ya existente.

```powershell
Set-Location 'C:\Users\Franz\Documents\Tech_Store_Frontend'
pnpm start
```

En otra terminal, desde la raíz:

```powershell
.\mvnw.cmd spring-boot:run
```

Abre `http://localhost:4200/`. La tienda permite filtrar productos y agregarlos al carrito. Registro y login están en `/registro` y `/login`; carrito en `/carrito`, historial en `/pedidos` y panel de administración en `/admin`. Las cuentas nuevas reciben el rol `USER`.

Los visitantes pueden añadir productos y revisar el carrito lateral o la página `/carrito` sin iniciar sesión. El backend firma su selección en un JWT de invitado que se conserva en `localStorage`, con migración de selecciones antiguas de `sessionStorage`. Tras el login o registro, el backend valida ese JWT y transfiere las cantidades al carrito persistente de la cuenta; si el producto ya estaba allí, conserva la mayor cantidad. El login se solicita al continuar al pago simulado. El JWT de invitado no autoriza pedidos ni acceso al carrito de usuarios.

## API

La [colección de Postman y su entorno local](docs/postman/README.md) incluyen solicitudes, pruebas de respuesta y captura automática de tokens e IDs para probar los flujos de la API.

| Método y ruta | Cuerpo o cabecera | Resultado |
| --- | --- | --- |
| `POST /auth/register` | `{ "nombre": "Ana", "email": "ana@example.com", "password": "una-clave-larga" }` | `201` y token JWT |
| `POST /auth/login` | `{ "email": "ana@example.com", "password": "una-clave-larga" }` | `200` y token JWT |
| `GET /auth/me` | `Authorization: Bearer <token>` | `200` y datos públicos del usuario |
| `GET /productos` | Público | Productos visibles |
| `GET /productos/{id}` | Público | Detalle visible |
| `GET /productos/admin` | JWT de `ADMIN` | Todos los productos, incluso ocultos |
| `POST /productos`, `PUT /productos/{id}`, `DELETE /productos/{id}` | JWT de `ADMIN` | Crear, editar u ocultar producto |
| `GET /carrito` | JWT | Carrito persistido del usuario |
| `POST /carrito/items` | `{ "productoId": 1, "cantidad": 2 }` + JWT | Añadir al carrito |
| `PUT /carrito/items/{id}` | `{ "cantidad": 3 }` + JWT | Cambiar cantidad |
| `DELETE /carrito/items/{id}` | JWT | Quitar artículo |
| `POST /pedidos` | JWT | Confirmar carrito y descontar stock |
| `GET /pedidos` | JWT | Historial propio |
| `GET /pedidos/admin` | JWT de `ADMIN` | Todos los pedidos |

La contraseña se almacena con BCrypt. El JWT de autenticación vence tras 60 minutos; el frontend lo guarda en `sessionStorage`, lo envía solo a la API configurada y lo elimina al cerrar sesión. El servidor valida los roles de acceso. La confirmación del pedido verifica stock bajo bloqueo de filas y guarda total y precios unitarios en una transacción; si falla, no descuenta inventario ni vacía el carrito. Ocultar un producto mantiene el historial de pedidos. `POST /pedidos/checkout` implementa el pago simulado del proyecto académico, sin cobros reales. Producción utiliza HTTPS y un origen CORS explícito.

## Devoluciones

El módulo reutiliza compras entregadas: USER consulta y solicita devoluciones propias; OPERADOR y ADMIN gestionan solicitudes. El flujo es `SOLICITADA → EN_REVISION → APROBADA → COMPLETADA`, con alternativa `EN_REVISION → RECHAZADA`. El rechazo exige observación. El servidor valida pertenencia, unidades disponibles e importe con el precio histórico; cada creación/transición válida registra actor, fecha y observación en el historial.

Consulta [la documentación del módulo](docs/devoluciones.md), [la colección original](docs/postman/Devoluciones.postman_collection.json) y [la colección ampliada de evaluación](output/pdf/TechStore_Pro_Postman.json). La guía de los anexos explica cómo preparar IDs, usuarios y recorridos alternativos sin ejecutar modificaciones involuntarias.

## Estructura

```text
src/main/java/com/techstorepro/techstorepro/
  auth/         registro, login y tokens
  usuario/      cuentas y roles
  producto/     catálogo, CRUD y stock
  carrito/      carrito persistente
  pedido/       confirmación y consulta de pedidos
  devolucion/   solicitudes, workflow e historial
src/main/resources/db/migration/  migraciones Flyway
Tech_Store_Frontend/src/app/
  core/         autenticación, interceptores y API
  features/     auth, catalogo, carrito, pedidos, admin
  shared/       vista reutilizable de producto
docs/ENUNCIADO.md
```

El backend separa controladores, servicios y repositorios por funcionalidad. El frontend organiza cada pantalla por flujo. Los productos y la identidad visual inicial están en migraciones y `Tech_Store_Frontend/public/assets/`.

## Dependencias preparadas

- **Backend:** Spring Boot 4.1.1, Web MVC, Validation, Data JPA, Security, OAuth2 Resource Server, PostgreSQL y Flyway.
- **Frontend:** Angular 21, Router, Forms, RxJS, Tailwind CSS 4, Manrope local y herramientas de compilación de Angular.

## Verificación

```powershell
.\mvnw.cmd verify
Set-Location 'C:\Users\Franz\Documents\Tech_Store_Frontend'
pnpm run verify
```

Se verificó además el flujo real con PostgreSQL local: permiso admin, creación de producto, carrito, confirmación de pedido, descuento de stock y vaciado del carrito. Los datos temporales de esa prueba se eliminaron.

El 15/10/2026 pasaron 35 pruebas del backend y 4 del carrito del frontend, además del build Angular. Las pruebas web usan MockMvc y servicios simulados; no constituyen una suite contra PostgreSQL real. Los workflows están publicados, pero la ejecución de GitHub Actions revisada quedó bloqueada por facturación de la cuenta. El informe detalla este límite y la construcción local pendiente con Podman.

### Seguimiento y actualización de pedidos

Los pedidos avanzan por `CONFIRMADO → EN_PREPARACION → ENVIADO → ENTREGADO`.
En Administración > Pedidos, el administrador pulsa el botón del siguiente estado. El stepper distingue pasos completados, estado actual y pendientes. Un pedido entregado es final; no admite retrocesos ni saltos. Los clientes consultan su progreso en Mis pedidos y pueden pulsar Actualizar estados.

`PATCH /pedidos/admin/{id}/estado` requiere rol ADMIN y cuerpo JSON como:
```json
{"estadoActual":"CONFIRMADO","estado":"EN_PREPARACION"}
```
Devuelve el pedido actualizado. Respuestas: 400 para datos inválidos, 403 para usuarios sin rol ADMIN, 404 si no existe y 409 para una transición inválida o un estado desactualizado. La fila se bloquea durante la transacción para serializar actualizaciones concurrentes. Flyway V4 limita los estados válidos en PostgreSQL. Cambiar estado no altera el total ni vuelve a descontar stock.
## Imágenes de productos

En Administración, selecciona o arrastra una imagen JPEG, PNG o WebP de hasta 5 MB. La vista previa aparece antes de guardar; al pulsar Guardar producto, el servidor sube el archivo y asocia su URL al producto. Solo el rol ADMIN puede subir archivos.

Las imágenes nuevas se suben a Cloudinary desde el backend y su URL HTTPS se guarda en el producto. Configura `CLOUDINARY_URL` como secreto del servicio; nunca lo publiques en Git ni en Angular. `UPLOAD_DIR` conserva únicamente la compatibilidad de lectura con archivos locales anteriores. El logo y el favicon permanecen en `public` del frontend. Cambiar o quitar una imagen del producto no elimina automáticamente el recurso anterior de Cloudinary.
