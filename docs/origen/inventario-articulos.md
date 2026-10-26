# Diego — proyecto standalone (Inventario + Catálogo de Artículos)

Este es un proyecto Spring Boot **completo y aislado**, solo con tus dos
módulos. No depende de Administración ni de Producción — lo abres, lo
corres y lo pruebas sin esperar a nadie del equipo.

## Qué contiene

```
diego-standalone/
├── pom.xml                  ← igual al que usa el equipo (Spring Boot + Oracle)
├── docker-compose.yml       ← Oracle en Docker
├── src/main/resources/application.yml
├── src/main/java/pe/edu/upeu/bomerp/
│   ├── BomerpBackendApplication.java   ← clase principal temporal (solo tuya)
│   ├── articulo/            ← Módulo 6: Catálogo de Artículos
│   └── inventario/          ← Módulo 5: Inventario
└── para-integrar-despues/
    └── adapter/DisponibilidadInventarioAdapter.java
```

La carpeta `para-integrar-despues/` **no se copia a `src/`** — es la pieza
que conecta tu Inventario con el módulo de Producción de Alexander
(`DisponibilidadInventarioPort`). Como ese módulo no existe todavía en
este proyecto aislado, ese archivo no compilaría. Guárdalo aparte y
recién lo agregas cuando el equipo junte todo (junto con el
`produccion/service/package-info.java` que te expliqué antes).

## 1. Abrir el proyecto

Abre la **carpeta `diego-standalone` completa** (donde está el
`pom.xml`) con IntelliJ o VS Code — así el IDE reconoce que es un
proyecto Maven y descarga las dependencias solo.

## 2. Levantar Oracle

```bash
cd diego-standalone
docker compose up -d
docker compose logs -f oracle-db   # espera "DATABASE IS READY TO USE!"
```

Puerto host: **1522** → contenedor **1521**, service `FREEPDB1`,
usuario `bomerp` / password `bomerp_pwd` (ya configurado en
`application.yml`, no toques nada).

## 3. Correr

```bash
./mvnw spring-boot:run
```

Swagger: `http://localhost:8080/swagger-ui.html`

## 4. Probar tus endpoints

Usa los mismos `curl` del README anterior (unidad de medida → artículo →
recepción → confirmar → stock → despacho → catálogo público). Todos
funcionan igual aquí, porque tu módulo no necesita nada de
Administración ni Producción para operar solo.

## 5. Cuando llegue el momento de integrar (al final)

1. Copian tus carpetas `articulo` e `inventario` dentro del repo grande
   (el que ya tiene `administracion` y `produccion`).
2. Agregan `produccion/service/package-info.java` (archivo nuevo).
3. Agregan `para-integrar-despues/adapter/DisponibilidadInventarioAdapter.java`
   dentro de `inventario/service/adapter/`.
4. Borran `produccion/service/DisponibilidadInventarioStub.java`.
5. Usan el `BomerpBackendApplication.java` y el `application.yml`
   definitivos del repo del equipo (no los de este proyecto standalone).
