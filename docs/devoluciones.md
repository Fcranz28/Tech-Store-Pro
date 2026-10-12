# Módulo de devoluciones

Este módulo usa las compras existentes (`pedidos` y `pedido_detalles`) de TechStore Pro. El backend está aislado en `src/main/java/com/techstorepro/techstorepro/devolucion`, la migración en `V7__devoluciones.sql` y Angular en `C:/Users/Franz/Documents/Tech_Store_Frontend/src/app/features/devoluciones`.

## Acceso y roles

- `USER` representa al **CLIENTE** del enunciado. Puede consultar sus compras, enviar solicitudes y ver únicamente sus devoluciones.
- `OPERADOR` revisa y resuelve solicitudes. `ADMIN` también puede gestionarlas.
- El registro público siempre crea `USER`. Para crear el primer operador, configura `OPERATOR_EMAIL` y `OPERATOR_PASSWORD` (mínimo 12 caracteres) en `.env` antes de iniciar el backend. No se sobrescribe una cuenta existente.
- Las rutas API usan el JWT de `/auth/login`. La pertenencia de compra y devolución se valida en el servidor; el guard Angular solo mejora la navegación.

## Flujo

1. El cliente abre **Mis pedidos** o **Mis devoluciones**, elige una compra `ENTREGADO` y selecciona una o más líneas con cantidades positivas.
2. El backend calcula `importe = suma(precio_unitario original × cantidad)`. Solo acepta líneas del pedido y cantidades que no superen las unidades compradas menos las ya aprobadas o completadas.
3. El operador aplica `SOLICITADA → EN_REVISION → APROBADA → COMPLETADA`, o `EN_REVISION → RECHAZADA`. El rechazo exige observación.
4. Al aprobar se vuelve a comprobar la disponibilidad bajo bloqueo de la compra, de modo que solicitudes pendientes concurrentes no puedan aprobarse por encima de las unidades compradas.

La solicitud no ejecuta un reembolso financiero: registra y gestiona la devolución. Las compras y el pago permanecen en el módulo actual.

## API

| Método | Ruta | Rol | Uso |
| --- | --- | --- | --- |
| GET | `/devoluciones/compras` | USER/ADMIN | Compras propias y unidades disponibles |
| GET | `/devoluciones/compras/{pedidoId}` | USER/ADMIN | Detalle de compra propia |
| POST | `/devoluciones` | USER/ADMIN | Crear solicitud |
| GET | `/devoluciones` | USER/ADMIN | Solicitudes propias |
| GET | `/devoluciones/{id}` | USER/ADMIN | Solicitud propia |
| GET | `/devoluciones/gestion?estado=&motivo=&desde=&hasta=&pagina=&tamanio=` | OPERADOR/ADMIN | Consulta filtrada y paginada |
| GET | `/devoluciones/gestion/{id}` | OPERADOR/ADMIN | Detalle para revisión |
| PATCH | `/devoluciones/gestion/{id}/estado` | OPERADOR/ADMIN | Transición de estado |

Ejemplo de solicitud:

```json
{
  "pedidoId": 12,
  "motivo": "PRODUCTO_DEFECTUOSO",
  "comentario": "El mouse no enciende",
  "items": [{"pedidoDetalleId": 34, "cantidad": 1}]
}
```

Ejemplo de transición:

```json
{"estadoActual":"EN_REVISION","estado":"RECHAZADA","observacionOperador":"No corresponde a la compra"}
```

Los errores de validación usan HTTP 400, compras ajenas o inexistentes 404, estado o cantidad en conflicto 409, sin sesión 401 y sin rol 403. Las fechas del filtro son instantes ISO 8601; `hasta` es exclusivo.

## Ejecución

Con PostgreSQL y las variables habituales `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` configuradas:

```powershell
./mvnw spring-boot:run
Set-Location 'C:\Users\Franz\Documents\Tech_Store_Frontend'
pnpm install
pnpm start
```

Flyway aplica `V7` al iniciar el backend. Angular muestra `/devoluciones`, `/devoluciones/nueva/:pedidoId` y `/gestion-devoluciones`. La colección `docs/postman/Devoluciones.postman_collection.json` permite probar los endpoints con tokens separados para cliente y operador.

## Verificación

`mvn test` comprueba cantidades, propiedad de la compra, transiciones y permisos. `pnpm run build` verifica Angular. Para una demostración completa, crea una compra, avánzala a `ENTREGADO` desde administración y luego envía la solicitud.
