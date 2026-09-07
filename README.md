# ERP Molinera - Backend API (LP2 - Unidad 1)

Backend REST empresarial con arquitectura de **Monolito Modular** para el Sistema Integral de Gestión de Producción y Comercialización de Maíz. 

Este proyecto cumple con los requerimientos de la Unidad 1 del curso LP2, implementando un CRUD maestro, una operación transaccional cabecera-detalle, consultas, filtros y configuración CORS, conectados a una base de datos Oracle.

## Stack Tecnológico
* **Lenguaje:** 21
* **Framework:** Spring Boot 3.x
* **Persistencia:** Spring Data JPA / Hibernate
* **Base de Datos:** Oracle DB (Proporcionada por BD2)
* **Arquitectura:** Monolito Modular (validado con Spring Modulith)

## Estructura de Módulos (Alcance Unidad 1)
Solo los módulos priorizados para la U1 están funcionales. Los demás existen como límites preparados para la Unidad 2.

* `pe.edu.upeu.erp.administracion/` ➔ **Funcional** (Maestros, Personas, Categorías planas).
* `pe.edu.upeu.erp.ventas/` ➔ **Funcional** (Operación atómica Cabecera-Detalle con `@Transactional`).
* `pe.edu.upeu.erp.inventario/` ➔ **Mock U1** (Servicio provisional de lectura de stock).
* `compras/`, `produccion/`, `finanzas/` ➔ Cascarones para U2.

## Cómo ejecutar el proyecto localmente

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/TU-USUARIO/erp-molinera-backend.git](https://github.com/TU-USUARIO/erp-molinera-backend.git)
