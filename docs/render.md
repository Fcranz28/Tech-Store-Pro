# TechStore Pro en Render

Backend: Render con Docker. Frontend: Vercel. Base de datos: el proyecto Supabase `inknldqrcpgwivyrjcfh`, con sus migraciones ya aplicadas al esquema privado `techstore`.

## Desplegar desde Git

`render.yaml` define un Web Service Docker gratuito en Virginia, próximo a la región `us-east-1` de Supabase. Render construye el Dockerfile desde el repositorio. Para este flujo no hace falta publicar una imagen en un registro.

1. Publica los archivos de este backend, incluido `Dockerfile` y `render.yaml`, en el repositorio `Fcranz28/Tech-Store-Pro`.
2. En Render, crea un Blueprint conectado a ese repositorio y revisa el servicio `techstore-pro-backend`.
3. Completa las variables que Render solicita como `sync: false`.
4. Despliega y comprueba el endpoint de salud y el catálogo.

| Variable | Valor |
| --- | --- |
| `DB_URL` | URL JDBC PostgreSQL de Supabase con `sslmode=require&currentSchema=techstore` |
| `DB_USER` | Usuario exacto correspondiente a la conexión elegida |
| `DB_PASSWORD` | Contraseña PostgreSQL del proyecto, guardada como secreto en Render |
| `APP_FRONTEND_ORIGIN` | Origen HTTPS exacto del frontend Vercel |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Credenciales iniciales del administrador; contraseña de al menos 12 caracteres |
| `CLOUDINARY_URL` | Credenciales de Cloudinary, solo en el backend; las nuevas subidas devuelven su URL HTTPS |

Render genera `JWT_SECRET`. El Blueprint activa el perfil `supabase`, configura el puerto 10000 y limita el heap Java al 60% de la memoria del contenedor. La API escucha en `0.0.0.0` y la comprobación de salud usa `/actuator/health/readiness`, que incluye PostgreSQL. La URL pública se toma de `RENDER_EXTERNAL_URL`; opcionalmente se puede sobrescribir con `APP_PUBLIC_URL`.

La conexión Supabase se elige según la red del servicio desplegado. La conexión directa requiere IPv6 o el complemento IPv4 del proyecto; el Session pooler en puerto 5432 funciona sobre IPv4 y conserva sentencias preparadas. Copia el host real desde **Connect → Session pooler** y usa `postgres.inknldqrcpgwivyrjcfh` si es el usuario mostrado por Supabase. El host del pooler no se deduce del nombre ni de la región del proyecto. El pooler de transacciones en 6543 no es la configuración prevista para este backend.

El perfil remoto desactiva Flyway para evitar volver a ejecutar las migraciones aplicadas con Supabase. No se crean bases de datos adicionales en Render.

## Construir con Podman y desplegar la imagen

Después de reiniciar Windows e iniciar una máquina Podman:

```powershell
podman machine init --cpus 2 --memory 2048 --disk-size 20
podman machine start
.\scripts\build-backend-image.ps1
```

Ejecuta `machine init` solo si aún no existe la máquina. La imagen resultante es Docker/OCI para Linux amd64 y se etiqueta `localhost/techstore-backend:latest`.

Si deseas que Render use exactamente la imagen construida con Podman, inicia sesión en tu registro, publica una etiqueta versionada con `-RegistryImage` y crea en Render un Web Service desde esa URL de imagen. Por ejemplo, sustituye `REGISTRO/USUARIO` por tu destino real:

```powershell
podman login REGISTRO
.\scripts\build-backend-image.ps1 -RegistryImage REGISTRO/USUARIO/techstore-backend:1.0.0
```

Render requiere una imagen disponible en un registro; una etiqueta que solo existe en Podman local no se puede descargar desde Render. Configura las mismas variables y la comprobación de salud que declara el Blueprint. No copies `.env.supabase` dentro de la imagen ni del repositorio.

## Verificar el despliegue

```powershell
Invoke-RestMethod https://TU-SERVICIO.onrender.com/actuator/health/readiness
Invoke-RestMethod https://TU-SERVICIO.onrender.com/productos
```

Después apunta Angular a esa URL HTTPS y verifica login, carrito, pedidos y devoluciones con los permisos correspondientes.

El plan gratuito puede suspender el servicio por inactividad y no conserva archivos locales después de cambios de instancia o despliegues. Las nuevas subidas del administrador se guardan en Cloudinary y su URL HTTPS se persiste en `productos.imagen_url` al guardar el producto. El endpoint mantiene el formato `{ "imagenUrl": "https://..." }`; el frontend puede reintentar la subida sin cambiar el formulario. El logo y favicon siguen siendo recursos del frontend. La lectura de archivos locales antiguos se conserva por compatibilidad, pero las nuevas subidas no escriben en el contenedor.

Referencias: [Docker en Render](https://render.com/docs/docker), [Blueprints](https://render.com/docs/blueprint-spec), [Plan gratuito](https://render.com/docs/free), [Conexiones Supabase](https://supabase.com/docs/guides/database/connecting-to-postgres).
