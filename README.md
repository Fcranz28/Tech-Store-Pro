# TechStore Pro

Mini e-commerce fullstack de accesorios tecnológicos. Implementa autenticación JWT, catálogo, carrito persistente, confirmación de pedidos con control de stock y administración de productos y pedidos. El alcance original está en [docs/ENUNCIADO.md](docs/ENUNCIADO.md).

## Requisitos

- Java 17 o superior, disponible en `PATH` como `java` y `javac`.
- Node.js compatible con Angular 21 (el proyecto se preparó con Node 24.14.1).
- pnpm 11 o compatible.
- PostgreSQL 17 o superior (se comprobó con PostgreSQL 18 local).

El repositorio incluye Maven Wrapper (`mvnw.cmd` en Windows; `./mvnw` en Unix), por lo que no hace falta instalar Maven por separado. Node.js y pnpm se instalan en la máquina; las dependencias Angular se instalan localmente en `frontend/`.

En este equipo se encontró Java 17 en `C:\Users\Franz\.jdks\corretto-17.0.20.1`, aunque no está en el `PATH`. Para usarlo en una sesión de PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Users\Franz\.jdks\corretto-17.0.20.1'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

## Instalación

```powershell
cd frontend
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

Las imágenes del catálogo y del encabezado se sirven desde Cloudinary; el logo y el favicon se conservan en `frontend/public/assets`. La migración `V5__cloudinary_product_images.sql` guarda las URLs recortadas de cada producto en `productos.imagen_url`, tanto para instalaciones nuevas como para productos existentes que aún usen la imagen local. El administrador puede indicar una URL de imagen al crear o editar otros productos.

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

Copia el resultado en `JWT_SECRET` del `.env`. Para otra URL del frontend, ajusta `APP_FRONTEND_ORIGIN` (por defecto, `http://localhost:4200`). Usa exactamente `localhost` al abrir Angular para que coincida con el origen CORS configurado.

Para acceder al panel admin, establece `ADMIN_EMAIL` y `ADMIN_PASSWORD` en `.env` **antes del primer arranque**. El backend crea esa cuenta con rol `ADMIN` si no existe. En este equipo ya hay una cuenta de administrador local; consulta los valores en tu `.env`. El arranque no cambia la contraseña de una cuenta ya existente.

```powershell
cd frontend
pnpm start
```

En otra terminal, desde la raíz:

```powershell
.\mvnw.cmd spring-boot:run
```

Abre `http://localhost:4200/`. La tienda permite filtrar productos y agregarlos al carrito. Registro y login están en `/registro` y `/login`; carrito en `/carrito`, historial en `/pedidos` y panel de administración en `/admin`. Las cuentas nuevas reciben el rol `USER`.

Los visitantes pueden añadir productos y revisar el carrito lateral o la página `/carrito` sin iniciar sesión. El backend firma su selección en un JWT de invitado que se conserva en `sessionStorage` durante la sesión. Tras el login o registro, el backend valida ese JWT y transfiere las cantidades al carrito persistente de la cuenta; si el producto ya estaba allí, conserva la mayor cantidad. El login se solicita al confirmar el pedido. El JWT de invitado no autoriza pedidos ni acceso al carrito de usuarios.

## API

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

La contraseña se almacena con BCrypt. El JWT vence tras 60 minutos. El frontend guarda el token en `sessionStorage`, lo envía sólo al backend local y lo elimina al cerrar sesión. El servidor valida el rol para endpoints admin. La confirmación del pedido verifica stock bajo bloqueo de filas y guarda total y precios unitarios en una transacción; si falla, no descuenta inventario ni vacía el carrito. Ocultar un producto mantiene el historial de pedidos. **No hay pasarela de pago**, porque el enunciado solicita confirmar pedidos, no cobrar. Para producción se necesitarían HTTPS, configuración de despliegue y revisión de la estrategia de almacenamiento del token.

## Estructura prevista

```text
src/main/java/com/techstorepro/techstorepro/
  auth/         registro, login y tokens
  usuario/      cuentas y roles
  producto/     catálogo, CRUD y stock
  carrito/      carrito persistente
  pedido/       confirmación y consulta de pedidos
src/main/resources/db/migration/  migraciones Flyway
frontend/src/app/
  core/         autenticación, interceptores y API
  features/     auth, catalogo, carrito, pedidos, admin
  shared/       vista reutilizable de producto
docs/ENUNCIADO.md
```

El backend separa controladores, servicios y repositorios por funcionalidad. El frontend organiza cada pantalla por flujo. Los productos y la identidad visual inicial están en migraciones y `frontend/public/assets/`.

## Dependencias preparadas

- **Backend:** Spring Boot 4.1.1, Web MVC, Validation, Data JPA, Security, OAuth2 Resource Server, PostgreSQL y Flyway.
- **Frontend:** Angular 21, Router, Forms, RxJS, Tailwind CSS 4, Manrope local y herramientas de compilación de Angular.

## Verificación

```powershell
.\mvnw.cmd test
cd frontend
pnpm build
```

Se verificó además el flujo real con PostgreSQL local: permiso admin, creación de producto, carrito, confirmación de pedido, descuento de stock y vaciado del carrito. Los datos temporales de esa prueba se eliminaron.

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

Las imágenes nuevas se guardan en `uploads/productos/` (fuera de Git), junto a la aplicación, y se sirven desde la API. Las imágenes existentes de Cloudinary siguen disponibles. Configura `UPLOAD_DIR` para usar un directorio persistente y `APP_PUBLIC_URL` con la URL pública de la API cuando despliegues la aplicación. Cambiar o quitar una imagen del producto no elimina automáticamente el archivo anterior.
