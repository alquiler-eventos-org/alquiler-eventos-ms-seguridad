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
| `SECURITY_JWT_SECRET` | — | Base64 de 32+ bytes (**obligatoria**; igual en todos los servicios) |
| `SECURITY_JWT_ISSUER` | `alquiler-eventos-seguridad` | Emisor del token |
| `SECURITY_JWT_ALGORITHM` | `HS256` | `HS256`, `HS384` o `HS512` |
| `SECURITY_JWT_TTL_SECONDS` | `1800` | Vigencia del token |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis compartido |
| `CACHE_ENABLED` | `true` | Habilita/deshabilita caché |
| `CACHE_KEY_PREFIX`, `CACHE_ENVIRONMENT`, `CACHE_KEY_VERSION` | `ae`, `local`, `v1` | Formato de claves |
| `CACHE_USUARIOS_TTL_SECONDS` | `600` | TTL de la caché de usuarios (roles son sin TTL) |

## Cómo correr

```bash
# Requiere el JAR de common instalado: cd ../alquiler-eventos-common; mvn clean install
mvn spring-boot:run
```

Queda en `http://localhost:8083`; sus endpoints se exponen por el gateway en
`/api/auth/**`, `/api/usuarios/**` y `/api/roles/**`. El flujo completo de la
demo está en `../README-ENTREGA-3.md` y el paso a paso en
`../GUIA-PRUEBAS-ENTREGA-3.md`.

## Pruebas

`mvn clean test` → `JwtServiceTest` (claims/algoritmo) y
`UsuarioSeguridadServiceTest` (autenticación, BCrypt, roles, duplicados).

## Rol dentro del sistema

Es el **único servicio que autentica** y emite JWT (puerto `8083`). El Gateway
valida ese mismo token antes de rutear, y compras/alquiler lo revalidan por su
cuenta. La tabla de puertos y el flujo general están en el README de
`alquiler-eventos-common` y en `../README-ENTREGA-3.md`.
