# LP2 — Borrador de sustentación de la Unidad II

Proyecto: Sistema Integral de Gestión de Producción y Comercialización.

Estado: avance integrado. Completar capturas y evidencias reales antes de presentar.

## 1. Organización funcional

La SPA usa `core` para autenticación y layout, `shared` para CRUD reutilizable y `features` para las operaciones del negocio. Se conserva parte de la estructura del frontend del compañero durante la integración; `api.ts`, `models.ts` y `proveedores.ts` aún están en la raíz de app.

El backend es un único proceso Spring Boot con paquetes de administración, personas, artículos, compras, inventario, producción, ventas y portal. No son microservicios. Los módulos se declararon abiertos para integrar el código heredado; falta verificar y afinar los límites con Spring Modulith.

## 2. Aporte de producción y administración

Mi alcance asignado en el brief es Producción y Administración institucional/roles. Para explicarlo debo dominar:

- La relación Empresa → Sucursal → Almacén/Tienda.
- Los roles de Keycloak y la tabla de permisos que determina el acceso a módulos.
- La relación Fórmula → FormulaDetalle y OrdenProduccion → ConsumoInsumo/ProductoResultante.
- La consulta de disponibilidad antes de iniciar una orden.
- El descuento de insumos al registrar consumos y el ingreso de productos al cerrar.
- La comparación de cantidades planificadas y reales por insumo.

Este avance fue preparado con asistencia de IA. Cada integrante debe revisar, ejecutar y comprender su parte; no presentar como propia una implementación que no pueda explicar.

## 3. Correspondencia con las sesiones

| Sesiones | Aplicación en este avance |
|---|---|
| S02–S03 | REST, repositorios JPA y relaciones; CRUD de empresas, sucursales, artículos, unidades y categoría–artículo |
| S04 | Fórmulas, compras, pedidos y movimientos con cabecera y detalle |
| S05 | Reportes, filtros y configuración CORS |
| S07–S08 | Angular, rutas, listas, formularios separados y selectores relacionados |
| S09 | FormArray en compras, fórmulas y ventas; cálculos y mensajes del backend |
| S10b | Backend resource server, issuer, firma, expiración, audiencia y roles de Keycloak |
| S11 | PKCE, token en memoria, guard, interceptor, menú y cierre de sesión |

## 4. Evidencias disponibles y pendientes

| Evidencia | Estado |
|---|---|
| Código backend y frontend | Incluido |
| Pruebas Java de integración | 8 aprobadas con H2 y JWT simulado |
| Pruebas frontend | 9 aprobadas |
| Compilación | Java aprobada; Angular producción anterior y desarrollo actual aprobados |
| Login y refresh reales | Pendiente con Keycloak |
| Persistencia real administrada | Pendiente con Oracle/BD2 |
| Demo visual completa | Pendiente |
| Documentación publicada y capturas | Pendiente |

## 5. Guion para preparar la exposición

**Presentación técnica, 8 minutos:** explicar el problema, la organización del proyecto, las entidades de tus módulos, el flujo de producción y la diferencia entre autenticación y autorización.

**Demo, 5 minutos:** crear una sucursal con empresa válida; provocar una dependencia inválida; mostrar una fórmula con sus líneas; iniciar una orden sin stock; completar una orden con existencias; mostrar el reporte. Mostrar también login y una denegación 403 cuando se hayan comprobado con Keycloak.

**Preguntas, 5 minutos:** practicar las respuestas siguientes usando el código.

- ¿Por qué la unidad de medida se selecciona de la base? Para usar un registro existente y validar la relación en el servidor.
- ¿Por qué el frontend no calcula el stock definitivo? Porque inventario lo calcula a partir de lotes persistidos; el servidor valida nuevamente al consumir o entregar.
- ¿Qué impide guardar una fórmula vacía? La validación del formulario y la comprobación de detalles en FormulaService.
- ¿Por qué ocultar un botón no basta? Un cliente puede llamar la API directamente; Spring Security comprueba permisos en el backend.
- ¿Qué diferencia hay entre 401 y 403? 401 indica que debe revisarse la autenticación; el interceptor intenta refrescar una vez. 403 indica permiso insuficiente y no se reintenta.
- ¿Dónde está el token? En memoria. sessionStorage conserva temporalmente los valores del intercambio PKCE, no el token de acceso.
- ¿Qué sucede al recargar la página? Se pierde la sesión en memoria y se vuelve al flujo de Keycloak; la sesión SSO puede permitir volver sin escribir credenciales.
- ¿Qué falta comprobar? La integración real Oracle/Keycloak, la navegación completa y las evidencias de BD2.

## 6. Fuentes del curso

- [Evaluación S12](https://262ciclo4.github.io/bomerp/lp2/sesiones/S12_Evaluacion_Unidad_2/).
- [Producto y rúbrica de U2](https://262ciclo4.github.io/bomerp/proyecto-integrador/u2/lp2-demo/).
- [Migración a Keycloak](https://262ciclo4.github.io/bomerp/lp2/sesiones/S10b_Seguridad_Backend_Keycloak/).
- [Seguridad del frontend](https://262ciclo4.github.io/bomerp/lp2/sesiones/S11_Seguridad_Frontend_Keycloak/).

La evaluación exige demostración en vivo. La existencia del código y las pruebas aisladas no permite afirmar que todos los criterios estén cumplidos.
