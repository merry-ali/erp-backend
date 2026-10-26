# Avance integrado — LP2, Unidad II

Entrega parcial solicitada el 19 de octubre de 2026. Este paquete reúne el código de los tres ZIP y añade las pantallas e integraciones descritas abajo. No debe presentarse todavía como una entrega final verificada con Oracle y Keycloak.

## Qué incluye

- Backend único: Spring Boot 4.0.7, Java 21, Maven y paquetes por módulo.
- Frontend único: Angular 22.2, navegación, estructura core/shared/features y formularios reactivos.
- Producción: fórmulas con insumos, planificación, verificación de existencias, consumo real, productos terminados, mermas y cierre.
- Administración: empresas, sucursales, almacenes, tiendas, roles, permisos y vinculación de perfiles institucionales.
- Inventario: recepción de compras, pesaje, humedad, impurezas, aceptación/rechazo, lotes, existencias y despacho de pedidos.
- Catálogo: artículos, unidades de medida y categorías. La categoría es opcional; la unidad es obligatoria.
- Compras: órdenes con detalle y cálculo del total; conserva y adapta el trabajo del compañero.
- Personas/proveedores: registro compartido con roles de cliente y proveedor.
- Ventas: pedidos con descuentos, aprobación según stock, entrega y anulación de pedidos no entregados. Este módulo se añadió porque no estaba en los ZIP.
- Portal: catálogo público sin costos ni stock y solicitudes de contacto visibles en Ventas.
- Reporte de producción: consumo planificado, consumo real y desviación por artículo/unidad, filtros y exportación CSV.
- Configuración de Keycloak: realm propio, usuarios de demostración, Authorization Code + PKCE, tokens en memoria, interceptor y permisos del backend.

## Qué se comprobó

- Compilación Java del backend.
- 8 pruebas de integración Java aprobadas con H2: flujo compra → recepción → producción → venta; falta de stock; rechazo de recepción; prevención de doble recepción; consumo insuficiente; 401/403; relación inexistente; serialización de cabecera-detalle.
- 9 pruebas del frontend aprobadas: formulario de compras e interceptor de autenticación, incluyendo un solo reintento ante 401 y ningún reintento ante 403.
- Compilación Angular de producción aprobada después de los ajustes finales; el backend también compila con Maven sin errores de compilación.
- Evidencias guardadas en `evidencias/`.

Las pruebas Java usan JWT simulados de Spring Security Test. No constituyen una prueba de login real con Keycloak. H2 tampoco sustituye la comprobación en Oracle exigida por el curso.

## Cómo abrirlo

1. Extrae el ZIP en una carpeta nueva. No reemplaces los proyectos originales aún.
2. Abre Docker Desktop y espera a que el motor esté disponible.
3. En la carpeta que contiene `compose.yml`, ejecuta:

```powershell
docker compose up -d
docker compose logs -f oracle
```

Espera a que Oracle indique que está listo. `Ctrl+C` termina la visualización del log, no el contenedor.

4. En otra terminal:

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
```

El perfil `demo` agrega una empresa ficticia, dos almacenes, una unidad, tres artículos, un proveedor, un cliente y una fórmula cuando la base está vacía. No agrega stock: este se obtiene al recibir una compra.

5. En otra terminal:

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

6. Abre http://localhost:4200 y selecciona **Acceso del personal**.

El portal público y el acceso del personal tienen una presentación propia. La pantalla de credenciales conserva la autenticación PKCE de Keycloak y usa el tema `maiz` desde `infra/keycloak-themes`. Si el realm `maiz-erp` ya existía antes de este cambio, abre la consola de Keycloak en http://localhost:8180, entra al realm **maiz-erp**, ve a **Realm settings → Themes → Login theme**, selecciona **maiz** y guarda. En una instalación nueva, la opción viene definida en el archivo de importación del realm.

Requisitos: JDK 21, Maven, Node compatible con Angular 22 y Docker Desktop. La máquina utilizada para desarrollar tenía Node 26.1 y npm 11.13. No se incluyen `node_modules` ni archivos compilados; se reconstruyen con esos comandos.

## Accesos locales de demostración

Realm: `maiz-erp`. Cliente SPA: `maiz-spa`. Audiencia de API: `maiz-api`.

| Usuario | Contraseña | Rol |
|---|---|---|
| admin | Demo2026! | ADMIN |
| compras | Demo2026! | COMPRADOR |
| almacen | Demo2026! | ALMACENERO |
| produccion | Demo2026! | OPERARIO_PLANTA |
| ventas | Demo2026! | VENDEDOR |

Consola de Keycloak: http://localhost:8180 — administrador de infraestructura `admin`, contraseña `keycloak_local_2026`. Es una cuenta distinta del usuario `admin` del realm.

Oracle: puerto 1522, servicio FREEPDB1, usuario bomerp, contraseña local bomerp_local_2026. Las credenciales incluidas son solo para demostración local.

Los roles de identidad se asignan en Keycloak. Administración permite configurar los módulos autorizados para cada nombre de rol. Vincular un perfil institucional no crea una cuenta en Keycloak ni modifica sus roles.

## Autocompletar DNI/RUC en Personas

El formulario consulta API Perú al salir del campo del documento cuando se ingresan 8 dígitos para DNI o 11 para RUC. El backend completa nombre o razón social y dirección cuando el servicio los devuelve; revisa los datos y pulsa **Guardar persona**. Para guardar la credencial localmente, abre `backend/application-local.yml` y coloca tu token en `consulta-documento.token`. Ese archivo está excluido de Git. También se puede definir `API_PERU_TOKEN` como variable de entorno; esta tiene prioridad sobre el archivo local.

Obtén la credencial y revisa planes/límites en [API Perú](https://docs.apiperu.pe/). La consulta usa `POST /dni` o `POST /ruc` en `https://api.apiperu.pe`. La credencial se mantiene en el backend y no se envía al navegador. Si no está configurada, se puede seguir registrando personas manualmente.

## Recorrido sugerido

1. Entrar como admin y revisar empresa, sucursal y almacenes.
2. Crear una orden de compra con 60 KG de MAIZ-01 y 40 KG de SOYA-01.
3. En Inventario, registrar la recepción en el almacén de materias primas y aceptarla. Revisar los lotes.
4. En Producción, planificar 100 unidades de la fórmula 60/40, seleccionar almacenes e iniciar.
5. Registrar consumos: 60 KG de MAIZ-01 y 40 KG de SOYA-01. Registrar 98 KG de PUR-001 como terminado y 2 KG como merma. Cerrar.
6. Consultar el reporte y las existencias de PUR-001 en el almacén de destino.
7. Crear un pedido para el cliente, aprobarlo y despacharlo desde Inventario con una placa.
8. Entrar como produccion e intentar acceder a Administración: debe denegarse.

## Pendientes antes de la evaluación

- Ejecutar la integración completa en Oracle y Keycloak reales. Docker estuvo inaccesible por permisos del entorno; los contenedores no fueron verificados.
- Revisar visualmente todas las pantallas y probar el recorrido completo en navegador con los cinco usuarios.
- S03 queda cubierto con `Categoria` y la selección de categoría en el formulario de Artículo; falta hacer la captura en vivo de la validación de una categoría inactiva.
- Repetir build de producción y pruebas después de cualquier cambio final.
- Comprobar login, logout, expiración y refresco reales. Revisar el import del realm y la disponibilidad de las imágenes Docker en tu equipo.
- Coordinar los esquemas, usuarios y privilegios de Oracle con BD2. Este avance consolida las tablas en el esquema `bomerp`; no implementa la separación de esquemas del ZIP de logística.
- Completar las evidencias personales, capturas y documentación publicada para S12. El archivo `lp2-demo.md` es un borrador honesto de sustentación, no evidencia de ejecución real.
- Revisar los endpoints heredados, completar DTOs y pruebas de concurrencia, endurecer validaciones y verificar los límites de Spring Modulith.
- Revisar permisos de escritura de catálogos institucionales: el backend exige ADMIN; no debe asignarse ADMINISTRACION a otros roles sin ajustar también la interfaz.
- La recepción actual es completa, sin entregas parciales. Las cantidades de compra conservan la restricción entera del proyecto original.
- No se implementaron facturación electrónica, pagos ni un módulo financiero completo. Los pedidos conservan importes para una futura integración.

## Procedencia

- `bomerp-modules.zip`: Administración y Producción.
- `diego-standalone.zip`: Inventario y Artículos.
- `logistica-maiz-u2.zip`: Compras, Personas y base del frontend de compras.
- Capturas del brief: alcance de los ocho módulos del equipo.

Los archivos de `docs/origen/` son documentación histórica; algunas instrucciones ya no corresponden al proyecto integrado. Sigue esta guía para ejecutar el avance.
