# ms-seguridad

Microservicio central de la **Entrega 3** (puerto `8083`). Gestiona usuarios con
múltiples roles, contraseñas **BCrypt** y la generación/validación de **JWT**.
Es el único servicio del grupo que autentica; compras y alquiler validan el
mismo token por su cuenta.

## Endpoints

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| POST | `/api/usuarios` | ADMIN |
| GET | `/api/usuarios/{id}` | ADMIN |
| PUT | `/api/usuarios/{id}` | ADMIN |
| PUT | `/api/usuarios/{id}/roles` | ADMIN |
| DELETE | `/api/usuarios/{id}` | ADMIN |
| GET | `/api/roles` | ADMIN |

`@PreAuthorize` (seguridad por método) + reglas por URL (`SecurityConfig`). Los
401/403 se devuelven como `ApiError` sin trazas (`SecurityAccessErrorHandler`).

## JWT

- HS256 por defecto (configurable a HS384/HS512), issuer
  `alquiler-eventos-seguridad`, TTL externo.
- Payload: `sub`, `uid`, `roles`, `iss`, `iat`, `exp`, `jti`. **Nunca** se
  guardan contraseñas ni datos privados.

## Caché Redis

- `seguridad:usuarios` → usuario sin password por ID, TTL 10 min.
- `seguridad:roles` → rol por ID, **sin TTL** (invalidación explícita).
- Claves: `ae:local:seguridad:usuario:v1:{id}` y `ae:local:seguridad:rol:v1:{id}`.

## Variables de entorno

| Variable | Defecto | Descripción |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | — | PostgreSQL/Supabase (obligatorias) |
| `SECURITY_JWT_SECRET` | — | Base64 de 32+ bytes (**obligatoria**) |
| `SECURITY_JWT_ISSUER` | `alquiler-eventos-seguridad` | Emisor del token |
| `SECURITY_JWT_ALGORITHM` | `HS256` | `HS256`, `HS384` o `HS512` |
| `SECURITY_JWT_TTL_SECONDS` | `1800` | Vigencia del token |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis compartido |
| `CACHE_ENABLED` | `true` | Habilita/deshabilita caché |

## Pruebas

`mvn clean test` → `JwtServiceTest` (claims/algoritmo) y
`UsuarioSeguridadServiceTest` (autenticación, BCrypt, roles, duplicados).

La configuración y el flujo completo están en `../README-ENTREGA-3.md`; el paso
a paso de la demo, en `../GUIA-PRUEBAS-ENTREGA-3.md`.
