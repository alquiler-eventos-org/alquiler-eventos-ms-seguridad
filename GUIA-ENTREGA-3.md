# Guía de implementación — Entrega 3

Esta entrega incorpora seguridad JWT y Redis compartido al sistema de alquiler de eventos.

## Repositorios modificados

| Repositorio | Rama | Estado |
|---|---|---|
| `alquiler-eventos-common` | `feature/cast-busqueda-postgres` | Subido |
| `alquiler-eventos-ms-compras` | `main` | Subido |
| `alquiler-eventos-api-gateway` | `main` | Subido |
| `alquiler-eventos-ms-seguridad` | `main` | Servicio nuevo |

## Common

- Migración Liquibase para crear `usuario_roles` y migrar el antiguo `usuarios.rol_id`.
- `Usuario` y `Rol` pasan a relación muchos-a-muchos.
- Se agrega `usuarios.activo` para baja lógica.
- Se siembran los roles `ADMIN`, `ENCARGADO_COMPRAS` y `ENCARGADO_DEPOSITO`.
- `OrdenCompraCrearRequest` ya no recibe `usuarioId`: el actor procede del JWT.

## MS Seguridad — puerto 8083

- `POST /api/auth/login` valida contraseña BCrypt y emite JWT HS256.
- JWT: `sub`, `uid`, `roles`, `iss`, `iat`, `exp`, `jti`; no contiene password.
- Usuarios: crear, obtener, actualizar, asignar múltiples roles y baja lógica. Todo requiere `ADMIN` excepto login.
- Valida localmente firma, expiración e issuer del token.
- Responde 401/403 como `ApiError`, sin trazas internas.
- Redis compartido:
  - `seguridad:usuarios`: usuario seguro individual, TTL 10 minutos.
  - `seguridad:roles`: rol individual, sin TTL.

## MS Compras — puerto 8082

- Valida el JWT otra vez aunque la petición venga del Gateway.
- Solo `ADMIN` o `ENCARGADO_COMPRAS` acceden a proveedores y órdenes de compra.
- El `uid` del JWT se usa para crear, editar, cambiar estado y anular órdenes; no se confía en `usuarioId` del body.
- Redis cache-aside sobre DTOs individuales:
  - `compras:proveedores`: TTL 30 minutos.
  - `compras:ordenes-compra`: TTL 2 minutos.
- GET individual: Redis hit/miss; POST/PUT/estado actualiza; DELETE expulsa.
- Páginas y búsquedas no guardan colecciones; solo calientan elementos individuales.
- Logs `CACHE GET/SET/DELETE/ERROR`; si Redis cae, la API continúa contra PostgreSQL.
- `CACHE_ENABLED=false` desactiva Redis sin detener la API.

## API Gateway — puerto 8080

- Rutas hacia Seguridad: `/api/auth/**`, `/api/usuarios/**`, `/api/roles/**`.
- Login, Swagger, health y OPTIONS son públicos.
- El resto requiere Bearer JWT válido antes de rutear.
- Se valida firma HS256, expiración e issuer.

## Variables necesarias

```text
DB_URL
DB_USER
DB_PASSWORD
SECURITY_JWT_SECRET=<Base64 de 32 bytes o más>
SECURITY_JWT_ISSUER=alquiler-eventos-seguridad
SECURITY_JWT_TTL_SECONDS=1800
REDIS_HOST=localhost
REDIS_PORT=6379
CACHE_ENABLED=true
```

Todos los micros deben usar el mismo `SECURITY_JWT_SECRET`, issuer y Redis.

## Orden de arranque y demo

1. Redis.
2. Eureka (`8761`).
3. Seguridad (`8083`).
4. Compras (`8082`) y Alquiler (`8081`).
5. Gateway (`8080`).
6. Login: `POST http://localhost:8080/api/auth/login`.
7. Copiar `accessToken` en Swagger como `Bearer <token>`.
8. Probar Compras: sin token devuelve 401; token de depósito devuelve 403; token de compras permite proveedores/órdenes.
9. En Redis Insight, ejecutar dos veces un GET individual: primero miss/SET, luego hit. Actualizar o cambiar estado y verificar PUT/EVICT.

## Pendiente para MS Alquiler (Tobi)

- Replicar JWT local y `@PreAuthorize` con `ADMIN` o `ENCARGADO_DEPOSITO`.
- Obtener `uid` desde JWT en vez de aceptar `usuarioId` del body.
- Cachear únicamente cliente individual (15 min) y orden de alquiler individual (1 min).
- Usar nombres `alquiler:clientes` y `alquiler:ordenes-alquiler`; no cachear páginas/búsquedas.

## Commits

- Common: `c2d550c`.
- Compras: `7eac491`.
- Gateway: `e9ceeac`.
- Seguridad: `2114a80` (el siguiente commit añade esta guía).
