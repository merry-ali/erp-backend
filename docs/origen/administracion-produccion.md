# BomERP backend — módulos Administración y Producción

Este paquete contiene el código de **2 de los 6 módulos** del ERP descrito en
tu documento (Figura 1 / Figura 3): **Administración** y **Producción**,
construidos como paquetes de un solo monolito modular (Spring Modulith),
organizados por capas (Controller → Service → Repository), tal como exige
la arquitectura del curso.

## 1. Cómo integrarlo con tu proyecto de Spring Initializr

Sigue generando el proyecto base exactamente como en tus capturas
(Tabla 5 del curso: Maven, Spring Boot 4.0.7, Java 21, group `pe.edu.upeu.bomerp`,
artifact `bomerp-backend`, con las 9 dependencias). Luego:

1. Reemplaza el `pom.xml` generado por el de este paquete (ya trae las 9
   dependencias de la Tabla 5, más `ojdbc11` y el módulo de docs de
   Spring Modulith usado por los tests).
2. Copia la carpeta `src/main/java/pe/edu/upeu/bomerp/administracion` y
   `src/main/java/pe/edu/upeu/bomerp/produccion` dentro de tu
   `src/main/java/pe/edu/upeu/bomerp/`.
3. Reemplaza `BomerpBackendApplication.java` y `application.yml`.
4. Copia `src/test/java/pe/edu/upeu/bomerp/ModularityTests.java`.
5. Copia `docker-compose.yml` a la raíz del proyecto.

## 2. Levantar Oracle con Docker

```bash
docker compose up -d
docker compose logs -f oracle-db   # espera a "DATABASE IS READY TO USE!"
```

Esto crea el usuario `bomerp` (mismo nombre que el schema usado en
`@Table(schema = "bomerp")` de las entidades — en Oracle, un usuario **es**
su propio schema, así que no necesitas crear el schema aparte).

Si prefieres usar tu propia instancia de Oracle en vez de Docker, solo
ajusta `spring.datasource.url/username/password` en `application.yml`.

## 3. Ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

Con `ddl-auto: update`, Hibernate crea las tablas automáticamente en el
schema `bomerp` la primera vez (para un proyecto de curso está bien; en
un proyecto real se reemplaza por Flyway/Liquibase con scripts versionados).

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

## 4. Verificar los límites entre módulos (Spring Modulith)

```bash
./mvnw test -Dtest=ModularityTests
```

`ApplicationModules.verify()` falla la compilación si, por ejemplo,
`produccion` termina dependiendo de una clase interna de `administracion`
que no debería ser pública. El segundo test genera diagramas en
`target/spring-modulith-docs/` — útil para tu sustentación.

## 5. Flujo de prueba end-to-end sugerido

```bash
# 1. Crear empresa → sucursal → almacenes
curl -X POST localhost:8080/api/administracion/empresas -H "Content-Type: application/json" \
  -d '{"ruc":"20123456789","razonSocial":"Molinos XYZ S.A.C."}'
curl -X POST localhost:8080/api/administracion/empresas/1/sucursales -H "Content-Type: application/json" \
  -d '{"nombre":"Planta de producción"}'
curl -X POST localhost:8080/api/administracion/sucursales/1/almacenes -H "Content-Type: application/json" \
  -d '{"nombre":"Almacén MP","tipo":"MATERIA_PRIMA"}'
curl -X POST localhost:8080/api/administracion/sucursales/1/almacenes -H "Content-Type: application/json" \
  -d '{"nombre":"Almacén PT","tipo":"PRODUCTO_TERMINADO"}'

# 2. Crear roles y permisos (para el menú del Portal Web)
curl -X POST localhost:8080/api/administracion/roles -H "Content-Type: application/json" \
  -d '{"nombre":"OPERARIO_PLANTA","descripcion":"Acceso a Producción"}'

# 3. Crear una fórmula con su detalle (insumos)
curl -X POST localhost:8080/api/produccion/formulas -H "Content-Type: application/json" \
  -d '{"codigoProductoTerminado":"PUR-001","nombre":"Purina engorde","version":1,
       "detalles":[{"codigoArticulo":"MAIZ-01","cantidadPorUnidad":0.6,"unidadMedida":"kg"}]}'

# 4. Planificar → iniciar → registrar consumo/resultado → cerrar la orden
curl -X POST localhost:8080/api/produccion/ordenes -H "Content-Type: application/json" \
  -d '{"formulaId":1,"almacenOrigenId":1,"almacenDestinoId":2,"cantidadPlanificada":100}'
curl -X POST localhost:8080/api/produccion/ordenes/1/iniciar
curl -X POST localhost:8080/api/produccion/ordenes/1/consumos -H "Content-Type: application/json" \
  -d '{"codigoArticulo":"MAIZ-01","cantidadConsumida":61.5,"unidadMedida":"kg"}'
curl -X POST localhost:8080/api/produccion/ordenes/1/resultados -H "Content-Type: application/json" \
  -d '{"tipo":"TERMINADO","codigoArticulo":"PUR-001","cantidad":99,"unidadMedida":"kg"}'
curl -X POST localhost:8080/api/produccion/ordenes/1/cerrar
```

## 6. Decisiones de diseño (para tu sustentación)

- **Administración no depende de ningún otro módulo** (igual que en la
  Figura 1): es quien sostiene a los demás de forma transversal.
- **Persona es el registro único de identidad**; `Usuario` es un rol sobre
  `Persona`. `Proveedor`/`Cliente` (que no construiste en este encargo)
  seguirían el mismo patrón cuando construyas Compras/Ventas.
- **Producción no tiene una relación JPA directa hacia Inventario** (que
  no existe todavía en el proyecto). En su lugar expone el puerto
  `DisponibilidadInventarioPort`, con un adaptador de prueba
  (`DisponibilidadInventarioStub`) que se reemplaza el día que construyas
  Inventario — sin tocar una línea de `OrdenProduccionService`.
- **`OrdenProduccion` nunca reserva stock de forma optimista**: `iniciar()`
  consulta disponibilidad real antes de pasar a `EN_PROCESO`; si no
  alcanza, queda `EN_ESPERA`.
- Los `almacenId` que usa Producción son enteros sueltos, no relaciones
  JPA hacia la entidad `Almacen` de Administración — mismo criterio: los
  módulos no comparten tablas entre sí directamente.

## 7. Lo que falta si quieres completar el sistema

Compras, Inventario, Ventas y Finanzas, siguiendo el mismo patrón
(paquete propio, capas, `package-info.java` con `@ApplicationModule`), y
finalmente el Portal Web como **proyecto Spring Boot aparte** (no un
paquete más de este monolito) que le hable a este backend solo por HTTP.
