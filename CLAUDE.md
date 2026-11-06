# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**Idioma:** responder siempre en español (mensajes, documentación y commits).

## 🇩🇴 Contexto del Negocio: Colmado (República Dominicana)
Este sistema gestiona las operaciones diarias de un comercio minorista local (Colmado) en Republeica Dominicana 
  . Conceptos clave a mantener:
- **POS / Ventas:** Debe ser extremadamente rápido. Soporta búsqueda por código de barra, teclado o selección rápida.
- **CxC (Cuentas por Cobrar / "El Fiado"):** Gestión de clientes locales con límites de crédito. Las ventas pueden ser a crédito y se registran en el balance del cliente.
- **CxP (Cuentas por Pagar):** Deudas con proveedores/distribuidores (ej: Cervecería, Brugal, Merca, etc.).
- **Inventario y Merma:** Registro estricto de productos que se dañan, rompen o vencen (mermas), restando stock sin generar venta.
- **Compras:** Abastecimiento de mercancía que actualiza costos y existencias.
- **delivery:** Gestion de las ventas por delivey y liquidaciion de delivery.


## Resumen

COLMADOM-WEB es un sistema web de punto de venta / ERP para un colmado, construido con **Vaadin Flow 24.6 (UI Java del lado del servidor) + Spring Boot 3.4 + Spring Data JPA sobre MySQL**, Java 21. El código, los identificadores y los textos de la UI están en español. La zona horaria de la app se fija a `America/Santo_Domingo` en `Application.main`.

## Comandos

```bash
./mvnw                                  # goal por defecto: spring-boot:run (modo dev, http://localhost:8082, abre el navegador)
./mvnw clean package -Pproduction       # JAR de producción (frontend incluido) -> target/COLMADOM-WEB-1.0-SNAPSHOT.jar
./mvnw spotless:apply                   # formatea Java con eclipse-formatter.xml
./mvnw dependency:tree > deps.txt       # buscar librerías duplicadas o en conflicto
./mvnw verify -Pit                      # pruebas de integración (levanta la app y corre clases *IT con failsafe)
./mvnw test -Dtest=NombreClase#metodo   # una sola prueba
```

Actualmente no hay pruebas en `src/test/java`. En Windows usar `mvnw.cmd`.

## Base de datos

- MySQL 8 en `localhost:3307/db_colmadom` (credenciales en `src/main/resources/application.properties`, y duplicadas como constantes en `configuracion/MysqlConnection.java`, que se usa para JDBC directo —p. ej. JasperReports— y se invoca una vez al arrancar).
- Hibernate **no** gestiona el esquema (`ddl-auto` no está en create/update). El volcado del esquema está en la raíz (`DB-Colmadom-AAAAMMDD.sql`); los cambios en entidades requieren DDL manual.
- Los repositorios usan mucho `nativeQuery = true` con SQL en strings, así que al renombrar columnas/tablas hay que actualizar también esas consultas.

## Arquitectura

Todo el código está en `src/main/java/com/maxsoft/application`:

- `modelo/` — entidades JPA (un solo paquete). `dto/` — proyecciones/DTOs para grids y reportes. `enums/`.
- `repo/` — repositorios Spring Data.
- `servicio/interfaces/<area>/` + `servicio/impl/<area>/` — cada servicio es una interfaz con su `*Impl`, agrupados por área: `compra`, `cxc` (cuentas por cobrar / facturas de venta, recibos de ingreso), `cxp` (cuentas por pagar), `delivery`, `inventario`, `venta`, `reporte`. Las vistas inyectan la interfaz.
- `evento/` — eventos de Spring (`VentaRealizadaEvent`, `VentaAnuladaEvent`, `VentaDevueltaEvent`, `AjusteInventarioCreadoEvent`, `SalidaInventarioCreadaEvent`). `InventarioServiceImpl` los maneja con `@EventListener @Transactional` y registra filas de `MovimientoInventario`. Ojo: las llamadas a `publishEvent` en `FacturaDeVentaServiceImpl` están comentadas — verificar cómo se actualiza realmente el inventario antes de depender de los eventos.
- `view/` — UI Vaadin (ver abajo). `view/componente/` contiene widgets reutilizables (barras del POS, diálogos de cobro, filtros); `view/dialogo/` diálogos compartidos.
- `reporte/` + `src/main/resources/reporte/` — JasperReports (archivos `.jasper`); la impresión también pasa por `servicio/impl/venta/ImpresionDirectaService`.
- `util/` — `CalculoFacturaUtil` (totales/impuestos de factura), `FiltroUtil`/`FiltroGenerico` (filtrado de grids), `SonidoUtil` (reproduce archivos de `META-INF/resources/sounds`), `NavigationContext` (ver abajo).

### Estructura de UI / navegación

- `MainLayout` (`@Route("/")`, `@Layout("modulo")`) es el contenedor principal; su menú lateral se arma con las rutas anotadas con `@Menu` vía `MenuConfiguration`.
- Cada módulo de negocio tiene su propio `AppLayout` anotado `@Route("mdlXxx") @Layout("<prefijo>") @Menu(order=N)` — p. ej. `view/delivery/ModuloDelivery` (`@Layout("delivery")`), `ModuloCompra`, `ModuloCuentaPorCobrar`, `ModuloCuentaPorPagar`. El módulo arma su drawer con `RouterLink`s explícitos a sus vistas y un enlace "Salir" a `ModuloPrincipal`.
- Las vistas de un módulo usan rutas con el prefijo del layout (p. ej. `@Route("delivery/liquidacionDelivery")`); así Vaadin las coloca dentro del AppLayout de ese módulo. Para agregar una vista a un módulo: darle el prefijo de ruta del módulo y añadir el enlace en `createDrawer()` del módulo.
- Patrón común: vista de lista/consulta (`XxxView` / `ConsultaXxxView`) + vista de formulario (`RegistroXxxView`). Los objetos se pasan entre vistas con `NavigationContext.store(obj)` → clave → `NavigationContext.retrieve(clave, Tipo)` (uso único, mapa estático en memoria).
- Server push habilitado (`@Push(AUTOMATIC, WEBSOCKET_XHR)` en `Application`) para vistas en tiempo real como el monitor de delivery. Tema: `src/main/frontend/themes/gestion-colmadom`.
- Hay archivos que son versiones históricas (p. ej. `PuntoDeVentaView17`, `PuntoDeVentaViewV10/V11`, `CobrarComponentV1`, `ResumenVentaPanel1`); editar la versión actual sin sufijo salvo que se indique lo contrario.

## Precauciones

- El índice de git contiene `repo/FacturaDeVentaRepo.java` y `repo/FacturaDeventaRepo.java` (solo difieren en mayúsculas). En el sistema de archivos de Windows, que no distingue mayúsculas, chocan; tener cuidado al editar o hacer stage de cualquiera de los dos.
