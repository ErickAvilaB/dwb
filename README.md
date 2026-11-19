# Product API · Práctica 7

Product registra cuentas en MySQL, almacena las contraseñas con BCrypt y emite JWT de una hora. Cada petición protegida envía el JWT en el encabezado `Authorization: Bearer <token>`.

## Arrancar Product

Con MySQL activo y el esquema de la práctica 6 en `product_db`, ejecuta desde la raíz del repositorio:

```bash
./mvnw spring-boot:run
```

Al arrancar, Product crea la tabla `product_account` si hace falta. La cuenta de MySQL debe poder crear esa tabla. Si un administrador la crea ejecutando [schema-auth.sql](src/main/resources/schema-auth.sql), puedes arrancar con `SPRING_SQL_INIT_MODE=never`.

Con `JWT_SECRET` sin configurar, Product genera una clave aleatoria para ese arranque. Los tokens dejan de ser válidos al reiniciar. Para conservarlos, configura `JWT_SECRET` con una clave fija de al menos 32 bytes UTF-8. Para validar JWT de la API Auth del curso, usa la misma clave en ambos servicios.

## Registrar una cuenta y probar con curl

En otra terminal, registra un usuario. Product asigna el rol `User`, que tiene los permisos de Customer. Las contraseñas nuevas requieren al menos 8 caracteres y aceptan hasta 72 bytes UTF-8.

```bash
API=http://localhost:8080
CREDENCIALES='{"username":"cliente.demo","password":"Cliente-demo-123!"}'

# 201 al crear la cuenta. Si ya existe, devuelve 409.
curl -i "$API/user" -H 'Content-Type: application/json' -d "$CREDENCIALES"

# 200 con el token. Credenciales incorrectas devuelven 401.
TOKEN=$(curl -fsS "$API/login" -H 'Content-Type: application/json' -d "$CREDENCIALES" |
  python3 -c 'import json, sys; print(json.load(sys.stdin)["token"])')

# 200 para Customer.
curl -i "$API/category/active" -H "Authorization: Bearer $TOKEN"

# 401 sin token.
curl -i "$API/category/active"

# 403 con el token de Customer en una ruta de Admin.
curl -i "$API/category" -H "Authorization: Bearer $TOKEN"
```

| Autoridad | Permisos |
| --- | --- |
| `Customer` / `User` | `GET /category/active`, `GET /product/{id}` y `GET /product/{id}/image` |
| `Admin` / `Administrator` | Todos los endpoints del catálogo |

## Crear un administrador local

Antes de arrancar Product, configura `AUTH_ADMIN_USERNAME` y `AUTH_ADMIN_PASSWORD` con las credenciales elegidas. En el primer arranque con esas variables, Product guarda esa cuenta con rol `Admin`. Después puedes retirar las variables y seguir iniciando sesión con las mismas credenciales. El nombre de usuario debe estar libre para crear la cuenta. Usa `POST /login` con esas credenciales para obtener el token de Admin y probar `GET /category`.

Para un frontend separado, configura `CORS_ALLOWED_ORIGINS` con los orígenes permitidos separados por comas, por ejemplo `http://localhost:4200`. Ejecuta las pruebas con `./mvnw test`.
