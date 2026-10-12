# Tech Store Pro en Supabase

Proyecto: [Tech Store Pro](https://supabase.com/dashboard/project/inknldqrcpgwivyrjcfh), referencia `inknldqrcpgwivyrjcfh`, región `us-east-1`.

Las migraciones de esta carpeta ya se aplicaron al proyecto el 12 de octubre de 2026 mediante el conector Supabase. Los nombres de archivo coinciden con las versiones registradas en `supabase_migrations.schema_migrations`:

- `20261012171510_techstore_bootstrap.sql`: crea el esquema `techstore` reutilizando Flyway V1-V8, siete tablas y quince productos de muestra con URLs Cloudinary.
- `20261012171659_techstore_foreign_key_indexes.sql`: incorpora los tres índices de claves foráneas equivalentes a Flyway V9.

No vuelvas a ejecutar la migración inicial en el SQL Editor. Las modificaciones futuras deben entrar como nuevas migraciones Supabase; Flyway permanece habilitado para la base local y desactivado en el perfil remoto `supabase`.

## Verificación realizada

Se comprobaron por SQL quince productos, quince imágenes Cloudinary, RLS activo en las siete tablas y ausencia de permisos `USAGE` sobre `techstore` para `anon` y `authenticated`. El historial remoto registra ambas migraciones.

Los asesores ya no detectan claves foráneas sin índice. Conservan avisos informativos de [índices todavía sin uso](https://supabase.com/docs/guides/database/database-linter?lint=0005_unused_index), porque la base recién creada aún no tiene carga de la aplicación.

El aviso [RLS activo sin políticas](https://supabase.com/docs/guides/database/database-linter?lint=0008_rls_enabled_no_policy) es intencional: el esquema es privado, el frontend accede únicamente a Spring Boot y los roles de la Data API no deben consultar estas tablas. El backend conecta como propietario de las tablas y aplica autorización mediante sus JWT y comprobaciones de pertenencia.

## Configuración del backend

`.env.supabase` es local y está ignorado por Git. Contiene las credenciales de PostgreSQL y un secreto JWT generado para esta instancia. Para una red sin IPv6, usa el host y el usuario exactos de **Connect → Session pooler**, puerto 5432; el host directo de este proyecto no es accesible desde la red usada en la comprobación.

Los pasos del contenedor están en [docs/despliegue-backend.md](../docs/despliegue-backend.md).
