# Backend con Podman y PostgreSQL de Supabase

El destino elegido para alojar el backend es **Render**. La configuración del servicio y las dos formas de desplegar el contenedor se encuentran en [docs/render.md](render.md).

El proyecto remoto y las migraciones aplicadas se registran en [supabase/README.md](../supabase/README.md).

## Imagen

Desde la raíz de TechStore-Pro, con Podman instalado y su máquina Linux iniciada:

```powershell
podman machine init
podman machine start
podman build --format docker --tag localhost/techstore-backend:latest --file Dockerfile .
```

Ejecuta `machine init` solo si todavía no hay una máquina. En Windows se requiere WSL2 o un proveedor compatible con Podman. Tras habilitar `VirtualMachinePlatform` y `Microsoft-Windows-Subsystem-Linux`, reinicia Windows si el instalador devuelve 3010 antes de crear la máquina Podman. El Dockerfile compila el JAR con Maven y ejecuta Java 17 como usuario 10001. El contexto excluye `.env`, credenciales, repositorio Git, archivos locales y frontend.

## Supabase y migraciones

Las migraciones locales siguen en `src/main/resources/db/migration` y se ejecutan con Flyway. La migración inicial de `supabase/migrations` reutiliza las versiones V1-V8 para crear el esquema privado `techstore` en Supabase, con su historial propio de migraciones. En el perfil `supabase`, Flyway está desactivado para evitar ejecutar las mismas sentencias dos veces; Hibernate valida las tablas al iniciar el backend. Las futuras modificaciones remotas deben aplicarse mediante nuevas migraciones Supabase.

La V8 activa RLS en las tablas de aplicación. En Supabase, además, revoca acceso al esquema privado para `anon` y `authenticated`. El backend conecta con el usuario propietario de las tablas; la autorización continúa en Spring Security. Las cuentas de la tienda y los JWT existentes son independientes de Supabase Auth.

Copia `.env.supabase.example` a `.env.supabase` y configura los datos de **Connect → Session pooler** del proyecto:

- `DB_URL`: URL JDBC con host real del pooler, puerto 5432, base `postgres`, `sslmode=require` y `currentSchema=techstore`.
- `DB_USER`: usuario exacto mostrado por Supabase, normalmente `postgres.<project-ref>`.
- `DB_PASSWORD`: contraseña PostgreSQL del proyecto, no una API key.
- `JWT_SECRET`: secreto de firma propio del backend, de al menos 32 bytes UTF-8.
- `APP_FRONTEND_ORIGIN`: origen HTTPS exacto del frontend Vercel.
- `APP_PUBLIC_URL`: URL pública del backend.

La conexión directa también sirve si el host dispone de IPv6. Para este contenedor persistente se usa modo sesión; el pooler de transacciones en puerto 6543 tiene restricciones para sentencias preparadas. La conexión JVM usa un máximo inicial de cinco conexiones, ajustable con `DB_POOL_SIZE`.

```powershell
podman run --detach --name techstore-backend --env-file .env.supabase --publish 8080:8080 --volume techstore-uploads:/app/uploads/productos localhost/techstore-backend:latest
podman logs --follow techstore-backend
```

El volumen conserva los archivos de imagen subidos por la implementación actual. Las URLs del catálogo alojadas en Cloudinary siguen guardadas por las migraciones. Los secretos entran al ejecutar el contenedor, no se incluyen en la imagen.

## Comprobar

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health/readiness
Invoke-RestMethod http://localhost:8080/productos
podman inspect --format '{{.State.Health.Status}}' techstore-backend
```

La salud debe indicar `UP` y el catálogo debe devolver productos. Para revisar las migraciones desde el SQL Editor del proyecto:

```sql
select version, name
from supabase_migrations.schema_migrations
order by version;

select tablename, rowsecurity
from pg_tables
where schemaname = 'techstore';
```

El frontend publicado debe apuntar a la URL HTTPS pública del backend. Publicar la imagen en un registro y ejecutar el contenedor en un proveedor requiere elegir ese destino.

Fuentes: [Conexiones PostgreSQL de Supabase](https://supabase.com/docs/guides/database/connecting-to-postgres), [Instalación de Podman](https://podman.io/docs/installation).
