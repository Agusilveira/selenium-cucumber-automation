# selenium-cucumber-automation

[![tests](https://github.com/Agusilveira/selenium-cucumber-automation/actions/workflows/ci.yml/badge.svg)](https://github.com/Agusilveira/selenium-cucumber-automation/actions/workflows/ci.yml)

Suite E2E con **Selenium 4 + Cucumber 7 + JUnit 5**, contra
[SauceDemo](https://www.saucedemo.com) y
[the-internet](https://the-internet.herokuapp.com).

24 escenarios en 4 hilos, en unos 19 segundos.

Parte de un portfolio de tres repos que resuelven **los mismos casos** con tres
herramientas distintas:

| Repo | Stack | Estado |
|------|-------|--------|
| **selenium-cucumber-automation** (este) | Java · Selenium 4 · Cucumber | ✅ |
| playwright-automation | TypeScript · Playwright | pendiente |
| cypress-automation | JavaScript · Cypress | pendiente |

## Correrlo

```bash
git clone https://github.com/Agusilveira/selenium-cucumber-automation.git
cd selenium-cucumber-automation
mvn test
```

No hay que descargar drivers: Selenium Manager los resuelve solo.

Perfiles y overrides:

```bash
mvn test -DTEST_ENV=ci                        # headless, timeouts cortos
mvn test -DBROWSER=firefox                    # cualquier clave admite override
mvn test -Dcucumber.filter.tags="@smoke"      # solo el subconjunto de humo
```

## Qué cubre

| Caso | Qué demuestra |
|------|---------------|
| Login exitoso | Flujo base |
| Login bloqueado | Assert sobre mensaje de error |
| Login data-driven | Esquema de escenario, 5 combinaciones |
| Carrito: agregar y quitar | Estado y contador |
| Carrito: listado | Verifica el contenido, no solo el contador |
| Checkout E2E | Flujo largo, valida que total = subtotal + impuestos |
| Ordenar productos | Comprueba el orden real de la lista, 4 criterios |
| `performance_glitch_user` | Esperas correctas, sin sleeps |
| Dynamic loading | Elemento que aparece tarde |
| Frames anidados | Cambio de contexto en profundidad y vuelta al raíz |
| Alertas de JavaScript | Accept y dismiss |
| Tabla | Lectura y validación de datos tabulares |

## Decisiones de diseño

**Sin capa de wrappers sobre Selenium.** No hay clases tipo `ButtonMethod` o
`TextBoxMethod`: envolver `element.click()` en un método propio reexpone la API
de Selenium con otro nombre. Lo único que vale encapsular son las esperas, y
viven en `BasePage`. Ocho clases en `main/`.

**Sin binarios de driver versionados.** Selenium Manager resuelve el driver en
runtime. Un `chromedriver.exe` commiteado deja de servir apenas el navegador se
actualiza, y convierte al repo en algo que hay que arreglar antes de poder
correrlo.

**Implicit wait en cero.** Mezclar esperas implícitas y explícitas produce
tiempos impredecibles y difíciles de diagnosticar. Toda la espera es explícita.

**Aislamiento por escenario, sin `ThreadLocal`.** Picocontainer crea un
`TestContext` por escenario, así que cada uno tiene su propio driver. Verificado
instrumentando las sesiones: 24 escenarios, 24 sesiones únicas de WebDriver.
`ThreadLocal` habría sido una abstracción sin nada que la justifique.

**`max-pool-size` además de `parallelism`.** Configurar solo `parallelism=4` no
limita nada: `ForkJoinPool` crea hilos de compensación cuando los suyos se
bloquean, y Selenium es I/O bloqueante de punta a punta. Sin `max-pool-size`, el
suite levantaba 24 navegadores en paralelo — más lento que con 4, y con fallas
intermitentes.

**Configuración con precedencia uniforme.** Variable de entorno > archivo del
perfil > error explícito. Todas las claves admiten override, sin excepciones
arbitrarias, y una clave faltante o mal tipada falla en el momento con un
mensaje que dice qué falta.

**El build falla si falla un test.** Sin `testFailureIgnore`: un badge que no
puede ponerse en rojo no informa nada.

**Evidencia en las fallas.** Un hook adjunta screenshot y HTML de la página al
reporte cuando un escenario falla, antes de cerrar el navegador.

## Una nota sobre el caso de frames

El caso empezó apuntando a `/iframe` de the-internet, para escribir dentro del
editor TinyMCE. Fallaba con `InvalidElementStateException` y ninguna variante lo
resolvía. Inspeccionando la página en vivo apareció el motivo: ese editor está
en modo `readonly`, así que no admite escritura por ningún medio. El caso se
movió a `/nested_frames`, que demuestra mejor lo que interesaba mostrar —
entrar a un frame, bajar a uno anidado, leer, y volver al documento raíz.

## Fuera de alcance

Tests de API, visual regression, accesibilidad, ejecución programada y ambientes
propios en Docker. El perfil `staging` queda preparado para apuntar a una URL
propia cuando haga falta.
