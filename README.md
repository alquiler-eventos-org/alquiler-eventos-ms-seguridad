# ms-seguridad

Microservicio central de la Entrega 3. Gestiona usuarios con multiples roles,
contraseñas BCrypt y JWT. Las rutas de login son públicas; usuarios y roles
requieren `ADMIN`. La configuración y el flujo completo están en
`../README-ENTREGA-3.md`.

Variables obligatorias: `DB_URL`, `DB_USER`, `DB_PASSWORD` y
`SECURITY_JWT_SECRET` (Base64, mínimo 32 bytes).
