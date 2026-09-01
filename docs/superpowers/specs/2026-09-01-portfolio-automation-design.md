# Portfolio de automation: Selenium / Playwright / Cypress

**Fecha:** 2026-09-01
**Estado:** aprobado, pendiente de plan de implementación

## Objetivo

Un portfolio de tres repositorios de test automation que resuelven **el mismo
conjunto de casos con tres herramientas distintas**. El valor no está en
demostrar una herramienta, sino en el contraste: qué resuelve mejor cada una y
por qué. Eso es material propio y da tema de conversación en una entrevista.

Audiencia: quien revisa un perfil de QA Automation. El filtro real es que pueda
clonar el repo, correr dos comandos y ver los tests pasar.

## Alcance y orden

Tres repos separados (no monorepo: cada uno con su README, su badge de CI y su
stack visible en GitHub).

1. **selenium-cucumber-automation** — implementación de referencia. Los casos se
   definen acá en Gherkin.
2. **playwright-automation** — porta los mismos casos.
3. **cypress-automation** — porta los mismos casos.

Se termina cada repo antes de empezar el siguiente. Un repo terminado vale más
que tres a medias.

## Aplicación bajo test

- **SauceDemo** (`saucedemo.com`) — flujo E2E: login, inventario, carrito,
  checkout. Incluye usuarios especiales (`locked_out_user`,
  `performance_glitch_user`) útiles para casos concretos.
- **the-internet** (Herokuapp) — widgets difíciles: dynamic loading, iframes,
  alerts, tablas.

Ambos llevan años estables y son públicos: el CI corre verde sin infraestructura
propia y cualquiera reproduce los resultados.

## Los casos (idénticos en los tres repos)

| # | Caso | Qué demuestra | App |
|---|------|---------------|-----|
| 1 | Login exitoso | Flujo base | SauceDemo |
| 2 | Login bloqueado | Assert sobre mensaje de error | SauceDemo |
| 3 | Login data-driven | Tabla de datos, un caso por fila | SauceDemo |
| 4 | Carrito agregar/quitar | Estado, contador del badge | SauceDemo |
| 5 | Checkout E2E | El flujo largo completo | SauceDemo |
| 6 | Ordenar productos | Validar el orden real, no solo que exista | SauceDemo |
| 7 | `performance_glitch_user` | Esperas correctas sin sleeps | SauceDemo |
| 8 | Dynamic loading | Elemento que aparece tarde | the-internet |
| 9 | Iframe | Cambio de contexto | the-internet |
| 10 | JS alerts | Accept / dismiss / prompt | the-internet |
| 11 | Tabla ordenable | Leer y validar datos tabulares | the-internet |

## Repo 1: selenium-cucumber-automation

### Stack

Java 21 · Maven · Selenium 4 · Cucumber 7 sobre JUnit 5 Platform · AssertJ ·
slf4j + logback.

Una sola librería de asserts, un solo runner, un solo framework de logging.

### Estructura

```
selenium-cucumber-automation/
├── .github/workflows/ci.yml
├── pom.xml
├── src/main/java/
│   ├── config/Env.java
│   ├── driver/DriverFactory.java
│   └── pages/
│       ├── BasePage.java
│       ├── LoginPage.java
│       ├── InventoryPage.java
│       ├── CartPage.java
│       ├── CheckoutPage.java
│       └── WidgetsPage.java
└── src/test/
    ├── java/
    │   ├── runners/RunCucumberTest.java
    │   ├── context/TestContext.java
    │   ├── hooks/Hooks.java
    │   └── steps/{Login,Cart,Checkout,Widget}Steps.java
    └── resources/
        ├── features/*.feature
        ├── config/{local,ci}.properties
        └── junit-platform.properties
```

Ocho clases en `main/`.

### Decisiones de diseño

**Sin capa de wrappers sobre Selenium.** No existe un paquete `methods/` con una
clase por tipo de control. `ButtonMethod.click(locator)` no le gana a
`element.click()`: es la API de Selenium reexpuesta con otro nombre. Lo que sí
vale encapsular —esperas y búsqueda de elementos— vive como métodos protegidos
en `BasePage`, que las cinco páginas extienden.

**Sin binarios de driver en el repo.** Selenium Manager (incluido desde 4.6)
resuelve el driver en runtime. No hay `.exe` versionados ni
`System.setProperty("webdriver.*")` ni dependencia de gestión de drivers. Es lo
que hace que el repo siga corriendo dentro de tres años.

**`ThreadLocal<WebDriver>` con paralelismo activado.** `cucumber.execution.parallel.enabled=true`
en `junit-platform.properties`. El `ThreadLocal` existe porque hay ejecución
paralela real que lo necesita, no por si acaso. `remove()` en el teardown.

**Implicit wait en 0; solo esperas explícitas.** Mezclar ambos mecanismos produce
tiempos de espera impredecibles y difíciles de diagnosticar.

**Inyección de dependencias real.** `cucumber-picocontainer` en la versión que
corresponde al Cucumber usado. `TestContext` se inyecta por constructor en las
clases de steps.

**Localizadores estables.** `data-test` y selectores por rol o texto. Nunca XPath
absoluto sobre la estructura del DOM.

**Evidencia en las fallas.** Un hook `@After` adjunta screenshot y page source al
reporte cuando el escenario falla.

## Configuración y ambientes

Perfiles de ejecución, no ambientes decorativos. Con una sola app bajo test,
tres archivos apuntando a la misma URL sería maqueta; los perfiles difieren en
parámetros que sí cambian de verdad:

| | `local` | `ci` |
|---|---|---|
| headless | no | sí |
| paralelismo | 1 hilo | 4 hilos |
| timeouts | largos (estás debuggeando) | cortos |
| navegador | el que quieras | fijado por la matrix |

Sin reintentos automáticos. Cucumber 7 no los trae de fábrica y montarlos para 21
casos contra sitios estables sería maquinaria para un problema que todavía no
existe. Además, reintentar enmascara intermitencias en lugar de exponerlas. Si
más adelante aparece inestabilidad real, se agrega el archivo de rerun con el
motivo documentado.

**Precedencia explícita y uniforme:** variable de entorno > archivo del perfil >
default. Todas las claves aceptan override, sin excepciones arbitrarias.

**Credenciales por variable de entorno / GitHub Secrets**, nunca versionadas,
aunque las de SauceDemo sean públicas: lo que se muestra es el patrón.

Queda un perfil `staging` preparado como hueco para apuntar a una URL propia.

## CI y reportes

- **GitHub Actions** en push y PR. Matrix Chrome + Firefox. Badge de estado en el
  README.
- **Sin ignorar fallas de test.** Un badge que no puede ponerse rojo no informa
  nada.
- **Reporte HTML publicado en GitHub Pages**, linkeado desde el README. Un
  reporte navegable con evidencia rinde más que cualquier descripción.
- Sin ejecución programada por cron: contra sitios de terceros solo garantiza
  encontrar el badge en rojo por causas ajenas.

## Capa de portfolio

Cada README abre con qué es el repo, cómo correrlo **en dos comandos**, y qué
demuestra cada caso. Los tres repos se enlazan entre sí.

**Tabla comparativa** en los tres READMEs: los mismos 11 casos contra las tres
herramientas, comparando líneas de código, tiempo de ejecución, calidad del
debugging y manejo de esperas. Escrita a partir de la experiencia de
implementarlos, no de documentación.

## Fuera de alcance

- Docker y ambientes levantados localmente (`staging` queda preparado).
- Tests de API.
- Visual regression y accesibilidad.
- Ejecución programada.

## Criterios de éxito

1. `git clone && mvn test` pasa en verde a la primera, sin pasos manuales.
2. El CI corre en GitHub Actions y el badge refleja el estado real.
3. El reporte HTML es accesible desde una URL pública.
4. Los 11 casos pasan en ejecución paralela sin intermitencias.
5. El README explica las decisiones de diseño, incluido qué se dejó afuera.
