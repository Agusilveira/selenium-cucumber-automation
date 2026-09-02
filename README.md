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

## Dos cosas que costaron encontrar

Las dos se resolvieron mirando evidencia, no deduciendo. Van acá porque el
razonamiento vale más que el diff.

### El editor que no se dejaba escribir

El caso de frames apuntaba a `/iframe` de the-internet, para escribir en el
editor TinyMCE. Fallaba con `InvalidElementStateException` y ninguna variante
—quitar el `clear()`, enfocar con click, limpiar por JS— lo resolvía.
Inspeccionando la página en vivo apareció el motivo: ese editor está en modo
`readonly` (`mode: "readonly"`), así que no admite escritura por ningún medio.
El caso era imposible tal como estaba planteado. Se movió a `/nested_frames`,
que además demuestra más: entrar a un frame, bajar a uno anidado, leer y volver
al documento raíz.

### Clicks que el navegador nunca recibía

En CI, tres escenarios fallaban solo en Chrome. Los síntomas eran engañosos y
llevaron a varias hipótesis equivocadas: condiciones de carrera, `/dev/shm`,
tamaño de ventana. Lo que finalmente lo resolvió fue instrumentar la página con
un listener de clicks propio y registrar qué llegaba:

```
esperado: [1368, 356]   recibidos: []   dpr: 1   viewport: [1920, 937]
```

El evento **no llegaba a la página**. Selenium no lanzaba ninguna excepción, el
elemento estaba visible, habilitado y sin nada encima (`elementFromPoint`
devolvía el mismo elemento), y la página en `readyState: complete`. Un click por
JavaScript sobre ese mismo elemento sí funcionaba. El mismo problema afectaba a
`sendKeys`: los campos del checkout quedaban vacíos y la aplicación no pasaba
del primer paso.

Es un problema de entrega de eventos de entrada de Chrome headless en el runner
—donde `window.screen` reporta 800x600 aunque la ventana sea mayor—, no del test
ni de la aplicación. La solución es explícita y ruidosa a propósito: el click y
el `sendKeys` verifican su propio efecto y, si no ocurrió, recurren a
JavaScript **avisando cada vez que lo hacen**. Un click por JS no ejercita el
mismo camino que el de una persona, así que es el último recurso y no el método
por defecto. Si esos avisos se vuelven frecuentes, el problema volvió y hay que
atacarlo, no acostumbrarse.

## Fuera de alcance

Tests de API, visual regression, accesibilidad, ejecución programada y ambientes
propios en Docker. El perfil `staging` queda preparado para apuntar a una URL
propia cuando haga falta.
