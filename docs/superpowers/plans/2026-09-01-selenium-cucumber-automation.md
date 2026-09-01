# selenium-cucumber-automation — Plan de implementación

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir el repo de referencia del portfolio: 11 casos E2E contra SauceDemo y the-internet, con Selenium 4 + Cucumber 7, ejecución paralela, perfiles de configuración y CI en GitHub Actions.

**Architecture:** Page Objects que extienden un `BasePage` con las esperas explícitas. Cucumber sobre JUnit 5 Platform; `TestContext` inyectado por picocontainer le da a cada escenario su propio `WebDriver` (aislamiento por escenario, sin `ThreadLocal`). La configuración se resuelve con precedencia variable de entorno > archivo del perfil > error.

**Tech Stack:** Java 21 · Maven · Selenium 4 · Cucumber 7 · JUnit 5 Platform · AssertJ · logback

**Ubicación:** `C:\Users\agusi\personal\selenium-cucumber-automation` (repo nuevo, separado del viejo)

**Spec:** `docs/superpowers/specs/2026-09-01-portfolio-automation-design.md`

---

## Notas para quien ejecuta

**Sobre los localizadores.** Los selectores CSS de SauceDemo y the-internet están escritos de memoria. La primera ejecución de cada tarea los confirma o los corrige: si un test falla con `NoSuchElementException`, abrir la página, inspeccionar y ajustar el selector. Es parte esperada del trabajo, no un error del plan.

**Sobre las versiones.** Las del `pom.xml` son versiones conocidas que funcionan juntas. Si se quieren las últimas, actualizarlas en la Tarea 1 y verificar que `mvn test-compile` siga pasando antes de avanzar.

**Orden.** Cada tarea termina en verde y con un commit. No pasar a la siguiente con algo en rojo.

---

## Estructura de archivos

```
selenium-cucumber-automation/
├── .github/workflows/ci.yml            CI: matrix Chrome + Firefox
├── .gitignore
├── pom.xml
├── README.md
├── src/main/java/
│   ├── config/Env.java                 configuración con precedencia
│   ├── driver/DriverFactory.java       crea el WebDriver según el perfil
│   └── pages/
│       ├── BasePage.java               driver + esperas explícitas
│       ├── LoginPage.java
│       ├── InventoryPage.java
│       ├── CartPage.java
│       ├── CheckoutPage.java
│       └── WidgetsPage.java            the-internet
├── src/main/resources/logback.xml
└── src/test/
    ├── java/
    │   ├── runners/RunCucumberTest.java
    │   ├── context/TestContext.java    inyectado por picocontainer
    │   ├── hooks/Hooks.java            ciclo de vida + evidencia en fallas
    │   ├── config/EnvTest.java
    │   ├── driver/DriverFactoryTest.java
    │   └── steps/{Login,Cart,Checkout,Widget}Steps.java
    └── resources/
        ├── features/*.feature
        ├── config/{local,ci}.properties
        └── junit-platform.properties
```

---

## Tarea 1: Proyecto Maven que compila

**Files:**
- Create: `pom.xml`, `.gitignore`, `src/main/resources/logback.xml`

- [ ] **Step 1: Crear el repo e inicializar git**

```bash
mkdir -p "C:/Users/agusi/personal/selenium-cucumber-automation" && cd "C:/Users/agusi/personal/selenium-cucumber-automation" && git init && mkdir -p src/main/java/config src/main/java/driver src/main/java/pages src/main/resources src/test/java/runners src/test/java/context src/test/java/hooks src/test/java/steps src/test/java/config src/test/java/driver src/test/resources/features src/test/resources/config
```

- [ ] **Step 2: Escribir `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>silveira</groupId>
  <artifactId>selenium-cucumber-automation</artifactId>
  <version>1.0.0</version>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <selenium.version>4.25.0</selenium.version>
    <cucumber.version>7.20.1</cucumber.version>
    <junit.version>5.11.3</junit.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.seleniumhq.selenium</groupId>
      <artifactId>selenium-java</artifactId>
      <version>${selenium.version}</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.5.12</version>
    </dependency>

    <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-java</artifactId>
      <version>${cucumber.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-junit-platform-engine</artifactId>
      <version>${cucumber.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-picocontainer</artifactId>
      <version>${cucumber.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.platform</groupId>
      <artifactId>junit-platform-suite</artifactId>
      <version>1.11.3</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>${junit.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.assertj</groupId>
      <artifactId>assertj-core</artifactId>
      <version>3.26.3</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.5.2</version>
      </plugin>
    </plugins>
  </build>
</project>
```

Sin `testFailureIgnore`: si un test falla, el build falla.

- [ ] **Step 3: Escribir `.gitignore`**

```gitignore
target/
.idea/
*.iml
.vscode/
*.log
screenshots/
.env
```

- [ ] **Step 4: Escribir `src/main/resources/logback.xml`**

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{20} - %msg%n</pattern>
    </encoder>
  </appender>
  <logger name="org.openqa.selenium" level="WARN"/>
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
</configuration>
```

- [ ] **Step 5: Verificar que compila**

Run: `mvn -B test-compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add pom.xml .gitignore src/main/resources/logback.xml && git commit -m "chore: proyecto maven con selenium 4, cucumber 7 y junit 5"
```

---

## Tarea 2: `Env` — resolución de configuración

Lógica pura, sin navegador: se testea de verdad y con TDD.

**Files:**
- Create: `src/main/java/config/Env.java`
- Test: `src/test/java/config/EnvTest.java`
- Create: `src/test/resources/config/local.properties`, `src/test/resources/config/ci.properties`

- [ ] **Step 1: Escribir los tests que fallan**

`src/test/java/config/EnvTest.java`:

```java
package config;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Properties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvTest {

    private Env envWith(Properties props, Map<String, String> overrides) {
        return new Env(props, overrides::get);
    }

    private Properties props(String... kv) {
        Properties p = new Properties();
        for (int i = 0; i < kv.length; i += 2) p.setProperty(kv[i], kv[i + 1]);
        return p;
    }

    @Test
    void usa_el_valor_del_archivo_cuando_no_hay_override() {
        Env env = envWith(props("base.url", "https://del-archivo"), Map.of());
        assertThat(env.get("base.url")).isEqualTo("https://del-archivo");
    }

    @Test
    void la_variable_de_entorno_le_gana_al_archivo() {
        Env env = envWith(props("base.url", "https://del-archivo"),
                          Map.of("BASE_URL", "https://del-entorno"));
        assertThat(env.get("base.url")).isEqualTo("https://del-entorno");
    }

    @Test
    void convierte_la_clave_a_formato_de_variable_de_entorno() {
        Env env = envWith(props("page.load.timeout", "30"),
                          Map.of("PAGE_LOAD_TIMEOUT", "5"));
        assertThat(env.getInt("page.load.timeout")).isEqualTo(5);
    }

    @Test
    void un_override_vacio_no_pisa_el_archivo() {
        Env env = envWith(props("base.url", "https://del-archivo"),
                          Map.of("BASE_URL", "   "));
        assertThat(env.get("base.url")).isEqualTo("https://del-archivo");
    }

    @Test
    void falla_con_mensaje_claro_si_falta_la_clave() {
        Env env = envWith(props(), Map.of());
        assertThatThrownBy(() -> env.get("base.url"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("base.url");
    }

    @Test
    void lee_booleanos() {
        Env env = envWith(props("headless", "true"), Map.of());
        assertThat(env.getBool("headless")).isTrue();
    }

    @Test
    void falla_con_mensaje_claro_si_el_numero_no_es_valido() {
        Env env = envWith(props("page.load.timeout", "treinta"), Map.of());
        assertThatThrownBy(() -> env.getInt("page.load.timeout"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("page.load.timeout");
    }
}
```

- [ ] **Step 2: Correr y verificar que falla**

Run: `mvn -B test -Dtest=EnvTest`
Expected: FALLA con error de compilación — la clase `Env` no existe.

- [ ] **Step 3: Implementar `Env`**

`src/main/java/config/Env.java`:

```java
package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

/**
 * Resuelve la configuración con precedencia explícita y uniforme:
 * variable de entorno > archivo del perfil > error.
 *
 * La clave "page.load.timeout" se busca en el entorno como PAGE_LOAD_TIMEOUT.
 * Todas las claves admiten override, sin excepciones arbitrarias.
 */
public final class Env {

    private final Properties props;
    private final Function<String, String> overrides;

    Env(Properties props, Function<String, String> overrides) {
        this.props = props;
        this.overrides = overrides;
    }

    /** Carga el perfil indicado por -Denv o TEST_ENV. Default: local. */
    public static Env load() {
        String profile = System.getProperty("env");
        if (profile == null || profile.isBlank()) profile = System.getenv("TEST_ENV");
        if (profile == null || profile.isBlank()) profile = "local";
        return new Env(readProfile(profile), Env::fromSystem);
    }

    private static String fromSystem(String envKey) {
        String v = System.getProperty(envKey);
        return v != null ? v : System.getenv(envKey);
    }

    private static Properties readProfile(String profile) {
        String resource = "/config/" + profile + ".properties";
        try (InputStream in = Env.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("No existe el perfil de configuración: " + resource);
            }
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el perfil " + resource, e);
        }
    }

    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    public String get(String key) {
        String override = overrides.apply(toEnvKey(key));
        if (override != null && !override.isBlank()) return override;

        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Falta la clave de configuración '" + key + "'. "
                    + "Definila en el archivo del perfil o exportá " + toEnvKey(key) + ".");
        }
        return value;
    }

    public String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (IllegalStateException e) {
            return defaultValue;
        }
    }

    public int getInt(String key) {
        String raw = get(key);
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La clave '" + key + "' debe ser un número entero, pero vale '" + raw + "'.", e);
        }
    }

    public boolean getBool(String key) {
        return Boolean.parseBoolean(get(key).trim());
    }

    public String baseUrl()      { return get("base.url"); }
    public String widgetsUrl()   { return get("widgets.url"); }
    public String browser()      { return get("browser"); }
    public boolean headless()    { return getBool("headless"); }
    public int pageLoadTimeout() { return getInt("page.load.timeout"); }
    public int explicitTimeout() { return getInt("explicit.timeout"); }
    public String user()         { return get("sauce.user"); }
    public String password()     { return get("sauce.password"); }
}
```

- [ ] **Step 4: Escribir los archivos de perfil**

`src/test/resources/config/local.properties`:

```properties
base.url=https://www.saucedemo.com
widgets.url=https://the-internet.herokuapp.com
browser=chrome
headless=false
page.load.timeout=30
explicit.timeout=15
sauce.user=standard_user
sauce.password=secret_sauce
```

`src/test/resources/config/ci.properties`:

```properties
base.url=https://www.saucedemo.com
widgets.url=https://the-internet.herokuapp.com
browser=chrome
headless=true
page.load.timeout=20
explicit.timeout=10
sauce.user=standard_user
sauce.password=secret_sauce
```

Las credenciales de SauceDemo son públicas y las publica el propio sitio en su home. Se dejan en el archivo para que el repo corra sin configuración previa; el override por `SAUCE_USER` / `SAUCE_PASSWORD` queda disponible y se usa desde el CI para demostrar el patrón.

- [ ] **Step 5: Correr y verificar que pasan**

Run: `mvn -B test -Dtest=EnvTest`
Expected: `Tests run: 7, Failures: 0, Errors: 0` y `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/config/Env.java src/test/java/config/EnvTest.java src/test/resources/config/ && git commit -m "feat: configuracion por perfiles con precedencia entorno > archivo"
```

---

## Tarea 3: `DriverFactory` — crear el navegador

**Files:**
- Create: `src/main/java/driver/DriverFactory.java`
- Test: `src/test/java/driver/DriverFactoryTest.java`

- [ ] **Step 1: Escribir el test que falla**

`src/test/java/driver/DriverFactoryTest.java`:

```java
package driver;

import config.Env;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import static org.assertj.core.api.Assertions.assertThat;

class DriverFactoryTest {

    @Test
    void crea_un_chrome_headless_y_navega() {
        System.setProperty("HEADLESS", "true");
        WebDriver driver = DriverFactory.create(Env.load());
        try {
            driver.get("https://www.saucedemo.com");
            assertThat(driver.getTitle()).isEqualTo("Swag Labs");
        } finally {
            driver.quit();
            System.clearProperty("HEADLESS");
        }
    }
}
```

Este test también verifica que el override por propiedad de sistema funciona de punta a punta: el perfil `local` dice `headless=false` y aun así corre headless.

- [ ] **Step 2: Correr y verificar que falla**

Run: `mvn -B test -Dtest=DriverFactoryTest`
Expected: FALLA con error de compilación — `DriverFactory` no existe.

- [ ] **Step 3: Implementar `DriverFactory`**

`src/main/java/driver/DriverFactory.java`:

```java
package driver;

import config.Env;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Crea un WebDriver ya configurado. Selenium Manager (incluido desde 4.6)
 * resuelve y descarga el binario del driver: no hay ejecutables versionados
 * en el repo ni propiedades webdriver.* que setear.
 *
 * El implicit wait queda en cero a propósito: mezclarlo con esperas explícitas
 * produce tiempos impredecibles. Toda la espera vive en BasePage.
 */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver create(Env env) {
        WebDriver driver = switch (env.browser().toLowerCase()) {
            case "chrome"  -> chrome(env);
            case "firefox" -> firefox(env);
            default -> throw new IllegalArgumentException(
                    "Navegador no soportado: '" + env.browser() + "'. Usá chrome o firefox.");
        };

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(env.pageLoadTimeout()));
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().window().setSize(new Dimension(1920, 1080));
        return driver;
    }

    private static WebDriver chrome(Env env) {
        ChromeOptions options = new ChromeOptions();
        if (env.headless()) options.addArguments("--headless=new");
        options.addArguments("--disable-notifications", "--disable-gpu", "--no-sandbox");
        return new ChromeDriver(options);
    }

    private static WebDriver firefox(Env env) {
        FirefoxOptions options = new FirefoxOptions();
        if (env.headless()) options.addArguments("-headless");
        return new FirefoxDriver(options);
    }
}
```

- [ ] **Step 4: Correr y verificar que pasa**

Run: `mvn -B test -Dtest=DriverFactoryTest`
Expected: `Tests run: 1, Failures: 0` y `BUILD SUCCESS`. La primera corrida tarda más porque Selenium Manager descarga el chromedriver.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/driver/DriverFactory.java src/test/java/driver/DriverFactoryTest.java && git commit -m "feat: DriverFactory con Selenium Manager, sin binarios en el repo"
```

---

## Tarea 4: Primer escenario Cucumber end-to-end (caso 1)

Esta tarea arma el andamiaje de Cucumber completo con un solo escenario. Es la más larga; las siguientes solo agregan casos.

**Files:**
- Create: `src/main/java/pages/BasePage.java`, `src/main/java/pages/LoginPage.java`, `src/main/java/pages/InventoryPage.java`
- Create: `src/test/java/context/TestContext.java`, `src/test/java/hooks/Hooks.java`, `src/test/java/runners/RunCucumberTest.java`, `src/test/java/steps/LoginSteps.java`
- Create: `src/test/resources/features/login.feature`, `src/test/resources/junit-platform.properties`

- [ ] **Step 1: Escribir el feature (el test que falla)**

`src/test/resources/features/login.feature`:

```gherkin
# language: es
Característica: Login en SauceDemo

  Antecedentes:
    Dado que estoy en la página de login

  @smoke
  Escenario: Un usuario válido accede al inventario
    Cuando ingreso con el usuario "standard_user"
    Entonces veo la lista de productos
```

- [ ] **Step 2: Escribir `junit-platform.properties`**

`src/test/resources/junit-platform.properties`:

```properties
cucumber.glue=steps,hooks
cucumber.plugin=pretty, html:target/cucumber-report.html, summary
cucumber.publish.quiet=true
```

El paralelismo se agrega en la Tarea 10, una vez que todos los casos pasen en serie.

- [ ] **Step 3: Escribir el runner**

`src/test/java/runners/RunCucumberTest.java`:

```java
package runners;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class RunCucumberTest {
}
```

- [ ] **Step 4: Correr y verificar que falla**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: FALLA. Cucumber reporta pasos sin definir (`UndefinedStepException`) y sugiere los snippets de los steps faltantes.

- [ ] **Step 5: Escribir `BasePage`**

`src/main/java/pages/BasePage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Base de los Page Objects. Contiene solo lo que Selenium no da servido:
 * las esperas explícitas. No envuelve la API de Selenium: si un método de acá
 * se limitara a delegar en WebElement, no debería existir.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final Env env;
    private final WebDriverWait wait;

    protected BasePage(WebDriver driver, Env env) {
        this.driver = driver;
        this.env = env;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(env.explicitTimeout()));
    }

    /** Espera a que el elemento sea visible y lo devuelve. */
    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Espera a que el elemento sea clickeable y lo devuelve. */
    protected WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /** Espera a que haya al menos un elemento y los devuelve todos. */
    protected List<WebElement> allVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    /** True si el elemento aparece dentro del timeout; false si no. No lanza. */
    protected boolean isVisible(By locator) {
        try {
            visible(locator);
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}
```

- [ ] **Step 6: Escribir `LoginPage` e `InventoryPage`**

`src/main/java/pages/LoginPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By USERNAME = By.cssSelector("[data-test='username']");
    private static final By PASSWORD = By.cssSelector("[data-test='password']");
    private static final By LOGIN    = By.cssSelector("[data-test='login-button']");
    private static final By ERROR    = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public void open() {
        driver.get(env.baseUrl());
    }

    public void loginAs(String user, String password) {
        visible(USERNAME).sendKeys(user);
        visible(PASSWORD).sendKeys(password);
        clickable(LOGIN).click();
    }

    public String errorMessage() {
        return visible(ERROR).getText();
    }
}
```

`src/main/java/pages/InventoryPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InventoryPage extends BasePage {

    private static final By CONTAINER = By.cssSelector("[data-test='inventory-container']");

    public InventoryPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public boolean isLoaded() {
        return isVisible(CONTAINER);
    }
}
```

Si `[data-test='inventory-container']` no existe, el fallback es `By.id("inventory_container")`. Confirmar en el navegador en el Step 9.

- [ ] **Step 7: Escribir `TestContext`**

`src/test/java/context/TestContext.java`:

```java
package context;

import config.Env;
import driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import pages.CartPage;
import pages.CheckoutPage;
import pages.InventoryPage;
import pages.LoginPage;
import pages.WidgetsPage;

/**
 * Estado compartido por los steps de un escenario.
 *
 * Picocontainer crea una instancia nueva por escenario y se la inyecta a cada
 * clase de steps que la declare en el constructor. Como Cucumber corre un
 * escenario por hilo, cada escenario tiene su propio WebDriver y no hace falta
 * ThreadLocal. La Tarea 10 verifica ese aislamiento en ejecución paralela.
 */
public class TestContext {

    private final Env env = Env.load();
    private WebDriver driver;

    private LoginPage loginPage;
    private InventoryPage inventoryPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private WidgetsPage widgetsPage;

    public Env env() {
        return env;
    }

    public WebDriver driver() {
        if (driver == null) driver = DriverFactory.create(env);
        return driver;
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public LoginPage loginPage() {
        if (loginPage == null) loginPage = new LoginPage(driver(), env);
        return loginPage;
    }

    public InventoryPage inventoryPage() {
        if (inventoryPage == null) inventoryPage = new InventoryPage(driver(), env);
        return inventoryPage;
    }

    public CartPage cartPage() {
        if (cartPage == null) cartPage = new CartPage(driver(), env);
        return cartPage;
    }

    public CheckoutPage checkoutPage() {
        if (checkoutPage == null) checkoutPage = new CheckoutPage(driver(), env);
        return checkoutPage;
    }

    public WidgetsPage widgetsPage() {
        if (widgetsPage == null) widgetsPage = new WidgetsPage(driver(), env);
        return widgetsPage;
    }
}
```

`CartPage`, `CheckoutPage` y `WidgetsPage` todavía no existen: se crean en las Tareas 6, 7 y 9. Para que compile ahora, crear las tres como clases mínimas:

`src/main/java/pages/CartPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {
    public CartPage(WebDriver driver, Env env) {
        super(driver, env);
    }
}
```

`src/main/java/pages/CheckoutPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.WebDriver;

public class CheckoutPage extends BasePage {
    public CheckoutPage(WebDriver driver, Env env) {
        super(driver, env);
    }
}
```

`src/main/java/pages/WidgetsPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.WebDriver;

public class WidgetsPage extends BasePage {
    public WidgetsPage(WebDriver driver, Env env) {
        super(driver, env);
    }
}
```

- [ ] **Step 8: Escribir `Hooks` y `LoginSteps`**

`src/test/java/hooks/Hooks.java`:

```java
package hooks;

import context.TestContext;
import io.cucumber.java.After;

public class Hooks {

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @After(order = 0)
    public void cerrarNavegador() {
        context.quitDriver();
    }
}
```

La captura de evidencia en fallas se agrega en la Tarea 9.

`src/test/java/steps/LoginSteps.java`:

```java
package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private final TestContext context;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Dado("que estoy en la página de login")
    public void que_estoy_en_la_pagina_de_login() {
        context.loginPage().open();
    }

    @Cuando("ingreso con el usuario {string}")
    public void ingreso_con_el_usuario(String usuario) {
        context.loginPage().loginAs(usuario, context.env().password());
    }

    @Entonces("veo la lista de productos")
    public void veo_la_lista_de_productos() {
        assertThat(context.inventoryPage().isLoaded())
                .as("la lista de productos debería estar visible")
                .isTrue();
    }
}
```

Los asserts llevan siempre `.as(...)` y terminan en una aserción real (`isTrue()`, `isEqualTo()`). Un `assertThat(x)` sin aserción final no verifica nada.

- [ ] **Step 9: Correr y verificar que pasa**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: `1 Scenarios (1 passed)` y `BUILD SUCCESS`.

Si falla con `NoSuchElementException` o timeout, abrir https://www.saucedemo.com, inspeccionar el elemento y corregir el localizador en el Page Object correspondiente.

- [ ] **Step 10: Commit**

```bash
git add src/main src/test && git commit -m "feat: andamiaje cucumber + primer escenario de login end-to-end"
```

---

## Tarea 5: Casos 2 y 3 — login bloqueado y data-driven

**Files:**
- Modify: `src/test/resources/features/login.feature`
- Modify: `src/test/java/steps/LoginSteps.java`

- [ ] **Step 1: Agregar los escenarios al feature**

Agregar al final de `src/test/resources/features/login.feature`:

```gherkin
  Escenario: Un usuario bloqueado no puede entrar
    Cuando ingreso con el usuario "locked_out_user"
    Entonces veo el mensaje de error "Sorry, this user has been locked out."

  @regresion
  Esquema del escenario: Distintos usuarios obtienen distinto resultado
    Cuando ingreso con el usuario "<usuario>" y la contraseña "<contrasenia>"
    Entonces el resultado del login es "<resultado>"

    Ejemplos:
      | usuario                 | contrasenia  | resultado |
      | standard_user           | secret_sauce | exito     |
      | problem_user            | secret_sauce | exito     |
      | locked_out_user         | secret_sauce | error     |
      | standard_user           | mala_clave   | error     |
      | usuario_inexistente     | secret_sauce | error     |
```

- [ ] **Step 2: Correr y verificar que falla**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: FALLA con pasos sin definir para el mensaje de error y el esquema.

- [ ] **Step 3: Agregar los steps**

Agregar a `src/test/java/steps/LoginSteps.java`:

```java
    @Cuando("ingreso con el usuario {string} y la contraseña {string}")
    public void ingreso_con_usuario_y_contrasenia(String usuario, String contrasenia) {
        context.loginPage().loginAs(usuario, contrasenia);
    }

    @Entonces("veo el mensaje de error {string}")
    public void veo_el_mensaje_de_error(String mensaje) {
        assertThat(context.loginPage().errorMessage())
                .as("el mensaje de error mostrado")
                .contains(mensaje);
    }

    @Entonces("el resultado del login es {string}")
    public void el_resultado_del_login_es(String resultado) {
        switch (resultado) {
            case "exito" -> assertThat(context.inventoryPage().isLoaded())
                    .as("esperaba entrar al inventario")
                    .isTrue();
            case "error" -> assertThat(context.loginPage().errorMessage())
                    .as("esperaba un mensaje de error")
                    .startsWith("Epic sadface");
            default -> throw new IllegalArgumentException(
                    "Resultado no reconocido: '" + resultado + "'. Usá exito o error.");
        }
    }
```

- [ ] **Step 4: Correr y verificar que pasa**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: `7 Scenarios (7 passed)` — 2 sueltos más 5 del esquema.

- [ ] **Step 5: Commit**

```bash
git add src/test && git commit -m "test: login bloqueado y login data-driven con esquema de escenario"
```

---

## Tarea 6: Casos 4 y 6 — carrito y ordenamiento

**Files:**
- Modify: `src/main/java/pages/InventoryPage.java`
- Rewrite: `src/main/java/pages/CartPage.java`
- Create: `src/test/resources/features/cart.feature`, `src/test/java/steps/CartSteps.java`

- [ ] **Step 1: Escribir el feature**

`src/test/resources/features/cart.feature`:

```gherkin
# language: es
Característica: Carrito de compras

  Antecedentes:
    Dado que estoy en la página de login
    Cuando ingreso con el usuario "standard_user"

  @smoke
  Escenario: Agregar y quitar productos actualiza el contador
    Cuando agrego al carrito el producto "Sauce Labs Backpack"
    Y agrego al carrito el producto "Sauce Labs Bike Light"
    Entonces el contador del carrito muestra 2
    Cuando quito del carrito el producto "Sauce Labs Backpack"
    Entonces el contador del carrito muestra 1

  Escenario: El carrito lista los productos agregados
    Cuando agrego al carrito el producto "Sauce Labs Backpack"
    Y abro el carrito
    Entonces el carrito contiene "Sauce Labs Backpack"

  @regresion
  Esquema del escenario: Ordenar el inventario reordena los productos
    Cuando ordeno los productos por "<criterio>"
    Entonces los productos quedan ordenados por "<criterio>"

    Ejemplos:
      | criterio          |
      | nombre ascendente |
      | nombre descendente|
      | precio ascendente |
      | precio descendente|
```

- [ ] **Step 2: Correr y verificar que falla**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: FALLA con pasos sin definir en `cart.feature`.

- [ ] **Step 3: Ampliar `InventoryPage`**

Reemplazar `src/main/java/pages/InventoryPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.util.List;

public class InventoryPage extends BasePage {

    private static final By CONTAINER   = By.cssSelector("[data-test='inventory-container']");
    private static final By ITEM_NAMES  = By.cssSelector("[data-test='inventory-item-name']");
    private static final By ITEM_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By CART_BADGE  = By.cssSelector("[data-test='shopping-cart-badge']");
    private static final By CART_LINK   = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By SORT        = By.cssSelector("[data-test='product-sort-container']");

    public InventoryPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public boolean isLoaded() {
        return isVisible(CONTAINER);
    }

    /** Convierte "Sauce Labs Backpack" en el data-test del botón correspondiente. */
    private By addButtonFor(String producto) {
        return By.cssSelector("[data-test='add-to-cart-" + slug(producto) + "']");
    }

    private By removeButtonFor(String producto) {
        return By.cssSelector("[data-test='remove-" + slug(producto) + "']");
    }

    private String slug(String producto) {
        return producto.toLowerCase().replace("(", "").replace(")", "").replace(" ", "-");
    }

    public void addToCart(String producto) {
        clickable(addButtonFor(producto)).click();
    }

    public void removeFromCart(String producto) {
        clickable(removeButtonFor(producto)).click();
    }

    /** 0 cuando el badge no está presente: SauceDemo lo oculta con el carrito vacío. */
    public int cartCount() {
        List<WebElement> badge = driver.findElements(CART_BADGE);
        return badge.isEmpty() ? 0 : Integer.parseInt(badge.get(0).getText().trim());
    }

    public void openCart() {
        clickable(CART_LINK).click();
    }

    public void sortBy(String criterio) {
        String value = switch (criterio) {
            case "nombre ascendente"  -> "az";
            case "nombre descendente" -> "za";
            case "precio ascendente"  -> "lohi";
            case "precio descendente" -> "hilo";
            default -> throw new IllegalArgumentException("Criterio desconocido: " + criterio);
        };
        new Select(visible(SORT)).selectByValue(value);
    }

    public List<String> productNames() {
        return allVisible(ITEM_NAMES).stream().map(WebElement::getText).toList();
    }

    public List<BigDecimal> productPrices() {
        return allVisible(ITEM_PRICES).stream()
                .map(e -> new BigDecimal(e.getText().replace("$", "").trim()))
                .toList();
    }
}
```

Si los `data-test` no coinciden, los fallbacks clásicos son `.inventory_item_name`, `.inventory_item_price`, `.shopping_cart_badge`, `.shopping_cart_link` y `.product_sort_container`.

- [ ] **Step 4: Escribir `CartPage`**

Reemplazar `src/main/java/pages/CartPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CartPage extends BasePage {

    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT   = By.cssSelector("[data-test='checkout']");

    public CartPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public List<String> items() {
        return allVisible(ITEM_NAMES).stream().map(WebElement::getText).toList();
    }

    public void checkout() {
        clickable(CHECKOUT).click();
    }
}
```

- [ ] **Step 5: Escribir `CartSteps`**

`src/test/java/steps/CartSteps.java`:

```java
package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CartSteps {

    private final TestContext context;

    public CartSteps(TestContext context) {
        this.context = context;
    }

    @Cuando("agrego al carrito el producto {string}")
    public void agrego_al_carrito(String producto) {
        context.inventoryPage().addToCart(producto);
    }

    @Cuando("quito del carrito el producto {string}")
    public void quito_del_carrito(String producto) {
        context.inventoryPage().removeFromCart(producto);
    }

    @Cuando("abro el carrito")
    public void abro_el_carrito() {
        context.inventoryPage().openCart();
    }

    @Entonces("el contador del carrito muestra {int}")
    public void el_contador_del_carrito_muestra(int esperado) {
        assertThat(context.inventoryPage().cartCount())
                .as("cantidad de productos en el badge del carrito")
                .isEqualTo(esperado);
    }

    @Entonces("el carrito contiene {string}")
    public void el_carrito_contiene(String producto) {
        assertThat(context.cartPage().items())
                .as("productos listados en el carrito")
                .contains(producto);
    }

    @Cuando("ordeno los productos por {string}")
    public void ordeno_los_productos_por(String criterio) {
        context.inventoryPage().sortBy(criterio);
    }

    @Entonces("los productos quedan ordenados por {string}")
    public void los_productos_quedan_ordenados_por(String criterio) {
        switch (criterio) {
            case "nombre ascendente" -> {
                List<String> nombres = context.inventoryPage().productNames();
                assertThat(nombres).as("nombres de productos").isSorted();
            }
            case "nombre descendente" -> {
                List<String> nombres = context.inventoryPage().productNames();
                assertThat(nombres).as("nombres de productos")
                        .isSortedAccordingTo(Comparator.reverseOrder());
            }
            case "precio ascendente" -> {
                List<BigDecimal> precios = context.inventoryPage().productPrices();
                assertThat(precios).as("precios de productos").isSorted();
            }
            case "precio descendente" -> {
                List<BigDecimal> precios = context.inventoryPage().productPrices();
                assertThat(precios).as("precios de productos")
                        .isSortedAccordingTo(Comparator.reverseOrder());
            }
            default -> throw new IllegalArgumentException("Criterio desconocido: " + criterio);
        }
    }
}
```

El test valida el orden real de la lista, no que el dropdown tenga el valor seleccionado. Verificar que el control cambió no prueba que la app haya hecho algo.

- [ ] **Step 6: Correr y verificar que pasa**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: `13 Scenarios (13 passed)`.

- [ ] **Step 7: Commit**

```bash
git add src/main src/test && git commit -m "test: carrito y ordenamiento de inventario"
```

---

## Tarea 7: Caso 5 — checkout end-to-end

**Files:**
- Rewrite: `src/main/java/pages/CheckoutPage.java`
- Create: `src/test/resources/features/checkout.feature`, `src/test/java/steps/CheckoutSteps.java`

- [ ] **Step 1: Escribir el feature**

`src/test/resources/features/checkout.feature`:

```gherkin
# language: es
Característica: Checkout

  @smoke
  Escenario: Un usuario completa una compra de principio a fin
    Dado que estoy en la página de login
    Cuando ingreso con el usuario "standard_user"
    Y agrego al carrito el producto "Sauce Labs Backpack"
    Y abro el carrito
    Y inicio el checkout
    Y completo mis datos "Agustin", "Silveira" y "5000"
    Entonces el total incluye impuestos
    Cuando confirmo la compra
    Entonces veo la confirmación "Thank you for your order!"
```

El total se valida **antes** de confirmar: la página de resumen con subtotal,
impuestos y total solo existe en ese paso. Una vez confirmada la compra esos
elementos ya no están en el DOM.

- [ ] **Step 2: Correr y verificar que falla**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: FALLA con pasos sin definir en `checkout.feature`.

- [ ] **Step 3: Escribir `CheckoutPage`**

Reemplazar `src/main/java/pages/CheckoutPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.math.BigDecimal;

public class CheckoutPage extends BasePage {

    private static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
    private static final By LAST_NAME  = By.cssSelector("[data-test='lastName']");
    private static final By POSTAL     = By.cssSelector("[data-test='postalCode']");
    private static final By CONTINUE   = By.cssSelector("[data-test='continue']");
    private static final By FINISH     = By.cssSelector("[data-test='finish']");
    private static final By COMPLETE   = By.cssSelector("[data-test='complete-header']");
    private static final By SUBTOTAL   = By.cssSelector("[data-test='subtotal-label']");
    private static final By TAX        = By.cssSelector("[data-test='tax-label']");
    private static final By TOTAL      = By.cssSelector("[data-test='total-label']");

    public CheckoutPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public void fillDetails(String nombre, String apellido, String codigoPostal) {
        visible(FIRST_NAME).sendKeys(nombre);
        visible(LAST_NAME).sendKeys(apellido);
        visible(POSTAL).sendKeys(codigoPostal);
        clickable(CONTINUE).click();
    }

    public void finish() {
        clickable(FINISH).click();
    }

    public String confirmationMessage() {
        return visible(COMPLETE).getText();
    }

    public BigDecimal subtotal() { return amountFrom(SUBTOTAL); }
    public BigDecimal tax()      { return amountFrom(TAX); }
    public BigDecimal total()    { return amountFrom(TOTAL); }

    /** Extrae el número de textos como "Item total: $29.99" o "Tax: $2.40". */
    private BigDecimal amountFrom(By locator) {
        String texto = visible(locator).getText();
        int corte = texto.indexOf('$');
        if (corte < 0) {
            throw new IllegalStateException("No encontré un monto en el texto: '" + texto + "'");
        }
        return new BigDecimal(texto.substring(corte + 1).trim());
    }
}
```

- [ ] **Step 4: Escribir `CheckoutSteps`**

`src/test/java/steps/CheckoutSteps.java`:

```java
package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckoutSteps {

    private final TestContext context;

    public CheckoutSteps(TestContext context) {
        this.context = context;
    }

    @Cuando("inicio el checkout")
    public void inicio_el_checkout() {
        context.cartPage().checkout();
    }

    @Cuando("completo mis datos {string}, {string} y {string}")
    public void completo_mis_datos(String nombre, String apellido, String codigoPostal) {
        context.checkoutPage().fillDetails(nombre, apellido, codigoPostal);
    }

    @Cuando("confirmo la compra")
    public void confirmo_la_compra() {
        context.checkoutPage().finish();
    }

    @Entonces("veo la confirmación {string}")
    public void veo_la_confirmacion(String mensaje) {
        assertThat(context.checkoutPage().confirmationMessage())
                .as("mensaje de confirmación de la compra")
                .isEqualTo(mensaje);
    }

    @Entonces("el total incluye impuestos")
    public void el_total_incluye_impuestos() {
        var page = context.checkoutPage();
        assertThat(page.total())
                .as("el total debería ser subtotal más impuestos")
                .isEqualByComparingTo(page.subtotal().add(page.tax()));
    }
}
```

- [ ] **Step 5: Correr y verificar que pasa**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: `14 Scenarios (14 passed)`.

- [ ] **Step 6: Commit**

```bash
git add src/main src/test && git commit -m "test: checkout end-to-end con validacion de totales"
```

---

## Tarea 8: Caso 7 — `performance_glitch_user` sin sleeps

**Files:**
- Modify: `src/test/resources/features/login.feature`

- [ ] **Step 1: Agregar el escenario**

Agregar al final de `src/test/resources/features/login.feature`:

```gherkin
  @regresion
  Escenario: Un usuario con la app lenta igual llega al inventario
    Cuando ingreso con el usuario "performance_glitch_user"
    Entonces veo la lista de productos
```

- [ ] **Step 2: Correr y verificar que pasa**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: `15 Scenarios (15 passed)`. El escenario tarda visiblemente más que el resto: SauceDemo demora la respuesta a propósito para ese usuario.

No se agrega ningún `Thread.sleep` ni espera nueva. Si este escenario pasa, la espera explícita de `BasePage` está bien hecha. Si falla por timeout, subir `explicit.timeout` en el perfil, nunca meter un sleep.

- [ ] **Step 3: Commit**

```bash
git add src/test && git commit -m "test: usuario con degradacion de performance, sin esperas fijas"
```

---

## Tarea 9: Casos 8 a 11 — widgets de the-internet y evidencia en fallas

**Files:**
- Rewrite: `src/main/java/pages/WidgetsPage.java`
- Create: `src/test/resources/features/widgets.feature`, `src/test/java/steps/WidgetSteps.java`
- Modify: `src/test/java/hooks/Hooks.java`

- [ ] **Step 1: Escribir el feature**

`src/test/resources/features/widgets.feature`:

```gherkin
# language: es
Característica: Widgets difíciles

  @regresion
  Escenario: Un elemento que carga con retraso se detecta sin esperas fijas
    Dado que abro la página de widgets "/dynamic_loading/2"
    Cuando disparo la carga diferida
    Entonces el texto cargado es "Hello World!"

  @regresion
  Escenario: Escribir dentro de un iframe
    Dado que abro la página de widgets "/iframe"
    Cuando escribo "texto dentro del iframe" en el editor
    Entonces el editor contiene "texto dentro del iframe"

  @regresion
  Esquema del escenario: Manejo de alertas de JavaScript
    Dado que abro la página de widgets "/javascript_alerts"
    Cuando disparo la alerta "<tipo>" y la "<accion>"
    Entonces el resultado indica "<mensaje>"

    Ejemplos:
      | tipo    | accion   | mensaje                          |
      | alert   | acepto   | You successfully clicked an alert |
      | confirm | acepto   | You clicked: Ok                   |
      | confirm | descarto | You clicked: Cancel               |

  @regresion
  Escenario: Leer y validar los datos de una tabla
    Dado que abro la página de widgets "/tables"
    Entonces la tabla tiene 4 filas
    Y la columna "Email" contiene "jdoe@hotmail.com"
```

- [ ] **Step 2: Correr y verificar que falla**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: FALLA con pasos sin definir en `widgets.feature`.

- [ ] **Step 3: Escribir `WidgetsPage`**

Reemplazar `src/main/java/pages/WidgetsPage.java`:

```java
package pages;

import config.Env;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WidgetsPage extends BasePage {

    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_TEXT  = By.id("finish");
    private static final By IFRAME       = By.id("mce_0_ifr");
    private static final By EDITOR_BODY  = By.id("tinymce");
    private static final By ALERT_RESULT = By.id("result");
    private static final By TABLE1       = By.id("table1");

    public WidgetsPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public void open(String path) {
        driver.get(env.widgetsUrl() + path);
    }

    // --- Dynamic loading ---

    public void startDeferredLoad() {
        clickable(START_BUTTON).click();
    }

    /** Espera a que el elemento exista y sea visible. Sin sleeps. */
    public String loadedText() {
        return visible(FINISH_TEXT).getText();
    }

    // --- Iframe ---

    public void typeInEditor(String texto) {
        driver.switchTo().frame(visible(IFRAME));
        WebElement body = visible(EDITOR_BODY);
        body.clear();
        body.sendKeys(texto);
        driver.switchTo().defaultContent();
    }

    public String editorText() {
        driver.switchTo().frame(visible(IFRAME));
        String texto = visible(EDITOR_BODY).getText();
        driver.switchTo().defaultContent();
        return texto;
    }

    // --- Alertas ---

    public void triggerAlert(String tipo) {
        String selector = switch (tipo) {
            case "alert"   -> "button[onclick='jsAlert()']";
            case "confirm" -> "button[onclick='jsConfirm()']";
            case "prompt"  -> "button[onclick='jsPrompt()']";
            default -> throw new IllegalArgumentException("Tipo de alerta desconocido: " + tipo);
        };
        clickable(By.cssSelector(selector)).click();
    }

    public void handleAlert(String accion) {
        Alert alert = new WebDriverWait(driver, Duration.ofSeconds(env.explicitTimeout()))
                .until(ExpectedConditions.alertIsPresent());
        if ("acepto".equals(accion)) {
            alert.accept();
        } else if ("descarto".equals(accion)) {
            alert.dismiss();
        } else {
            throw new IllegalArgumentException("Acción desconocida: " + accion);
        }
    }

    public String alertResult() {
        return visible(ALERT_RESULT).getText();
    }

    // --- Tabla ---

    public int tableRowCount() {
        return driver.findElements(By.cssSelector("#table1 tbody tr")).size();
    }

    /** Devuelve los valores de la columna con el encabezado dado. */
    public List<String> columnValues(String encabezado) {
        List<WebElement> headers = allVisible(By.cssSelector("#table1 thead th"));
        int indice = -1;
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().trim().equalsIgnoreCase(encabezado)) {
                indice = i + 1;
                break;
            }
        }
        if (indice < 0) {
            throw new IllegalArgumentException("La tabla no tiene la columna '" + encabezado + "'");
        }
        return driver.findElements(By.cssSelector("#table1 tbody tr td:nth-child(" + indice + ")"))
                .stream().map(WebElement::getText).map(String::trim).toList();
    }
}
```

`TABLE1` queda declarada para documentar el localizador de la tabla aunque los métodos usen selectores derivados; si el linter marca el campo como no usado, borrarlo.

- [ ] **Step 4: Escribir `WidgetSteps`**

`src/test/java/steps/WidgetSteps.java`:

```java
package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;

import static org.assertj.core.api.Assertions.assertThat;

public class WidgetSteps {

    private final TestContext context;

    public WidgetSteps(TestContext context) {
        this.context = context;
    }

    @Dado("que abro la página de widgets {string}")
    public void que_abro_la_pagina_de_widgets(String path) {
        context.widgetsPage().open(path);
    }

    @Cuando("disparo la carga diferida")
    public void disparo_la_carga_diferida() {
        context.widgetsPage().startDeferredLoad();
    }

    @Entonces("el texto cargado es {string}")
    public void el_texto_cargado_es(String esperado) {
        assertThat(context.widgetsPage().loadedText())
                .as("texto que aparece después de la carga diferida")
                .isEqualTo(esperado);
    }

    @Cuando("escribo {string} en el editor")
    public void escribo_en_el_editor(String texto) {
        context.widgetsPage().typeInEditor(texto);
    }

    @Entonces("el editor contiene {string}")
    public void el_editor_contiene(String esperado) {
        assertThat(context.widgetsPage().editorText())
                .as("contenido del editor dentro del iframe")
                .contains(esperado);
    }

    @Cuando("disparo la alerta {string} y la {string}")
    public void disparo_la_alerta_y_la(String tipo, String accion) {
        context.widgetsPage().triggerAlert(tipo);
        context.widgetsPage().handleAlert(accion);
    }

    @Entonces("el resultado indica {string}")
    public void el_resultado_indica(String esperado) {
        assertThat(context.widgetsPage().alertResult())
                .as("texto de resultado tras manejar la alerta")
                .isEqualTo(esperado);
    }

    @Entonces("la tabla tiene {int} filas")
    public void la_tabla_tiene_filas(int esperadas) {
        assertThat(context.widgetsPage().tableRowCount())
                .as("cantidad de filas de la tabla")
                .isEqualTo(esperadas);
    }

    @Y("la columna {string} contiene {string}")
    public void la_columna_contiene(String columna, String valor) {
        assertThat(context.widgetsPage().columnValues(columna))
                .as("valores de la columna " + columna)
                .contains(valor);
    }
}
```

- [ ] **Step 5: Agregar la captura de evidencia al hook**

Reemplazar `src/test/java/hooks/Hooks.java`:

```java
package hooks;

import context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class Hooks {

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    /**
     * Adjunta screenshot y HTML al reporte cuando el escenario falla.
     * Corre antes del cierre del navegador (order mayor = más temprano en @After).
     */
    @After(order = 1)
    public void capturarEvidenciaSiFalla(Scenario scenario) {
        if (!scenario.isFailed()) return;
        try {
            byte[] png = ((TakesScreenshot) context.driver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(png, "image/png", scenario.getName());
            scenario.attach(context.driver().getPageSource(), "text/html", "page-source");
        } catch (Exception e) {
            scenario.log("No se pudo capturar la evidencia: " + e.getMessage());
        }
    }

    @After(order = 0)
    public void cerrarNavegador() {
        context.quitDriver();
    }
}
```

En Cucumber, los `@After` con `order` más alto corren primero, así que la evidencia se captura con el navegador todavía abierto.

- [ ] **Step 6: Correr y verificar que pasa**

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: `21 Scenarios (21 passed)` — 15 previos más 6 de widgets.

- [ ] **Step 7: Verificar que la evidencia funciona**

Romper a propósito un assert (por ejemplo cambiar `"Hello World!"` por `"Chau"` en `widgets.feature`), correr, y confirmar que `target/cucumber-report.html` muestra el screenshot adjunto en el escenario fallido. Después revertir el cambio y volver a correr en verde.

Run: `mvn -B test -Dtest=RunCucumberTest`
Expected: primero 1 escenario fallido con screenshot en el reporte; tras revertir, `21 Scenarios (21 passed)`.

- [ ] **Step 8: Commit**

```bash
git add src/main src/test && git commit -m "test: widgets de the-internet y evidencia adjunta en fallas"
```

---

## Tarea 10: Ejecución paralela y verificación de aislamiento

Acá se comprueba que la decisión de no usar `ThreadLocal` es correcta.

**Files:**
- Modify: `src/test/resources/junit-platform.properties`

- [ ] **Step 1: Medir el tiempo en serie**

Run: `mvn -B test -Dtest=RunCucumberTest`
Anotar el tiempo total que reporta Maven (`Total time`).

- [ ] **Step 2: Activar el paralelismo**

Reemplazar `src/test/resources/junit-platform.properties`:

```properties
cucumber.glue=steps,hooks
cucumber.plugin=pretty, html:target/cucumber-report.html, summary
cucumber.publish.quiet=true

cucumber.execution.parallel.enabled=true
cucumber.execution.parallel.config.strategy=fixed
cucumber.execution.parallel.config.fixed.parallelism=4
```

- [ ] **Step 3: Correr en paralelo y verificar aislamiento**

Run: `mvn -B test -Dtest=RunCucumberTest -DHEADLESS=true`
Expected: `21 Scenarios (21 passed)` y un `Total time` bastante menor al del Step 1.

**Este es el chequeo importante.** Si aparecen fallas intermitentes, escenarios que ven el estado de otro, o `NoSuchSessionException`, significa que los escenarios están compartiendo driver y hay que revisar la inyección de `TestContext`. En ese caso, el remedio documentado es envolver el driver en `ThreadLocal<WebDriver>` dentro de `TestContext` y dejar un comentario explicando por qué hizo falta.

- [ ] **Step 4: Correr tres veces seguidas para descartar intermitencia**

```bash
mvn -B test -Dtest=RunCucumberTest -DHEADLESS=true && mvn -B test -Dtest=RunCucumberTest -DHEADLESS=true && mvn -B test -Dtest=RunCucumberTest -DHEADLESS=true
```

Expected: las tres corridas en verde con 21 escenarios.

- [ ] **Step 5: Commit**

```bash
git add src/test/resources/junit-platform.properties && git commit -m "perf: ejecucion paralela de escenarios en 4 hilos"
```

---

## Tarea 11: CI en GitHub Actions

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Escribir el workflow**

`.github/workflows/ci.yml`:

```yaml
name: tests

on:
  push:
    branches: [main]
  pull_request:
  workflow_dispatch:

jobs:
  e2e:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        browser: [chrome, firefox]

    steps:
      - uses: actions/checkout@v4

      - name: Configurar JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: temurin
          cache: maven

      - name: Correr los tests
        env:
          TEST_ENV: ci
          BROWSER: ${{ matrix.browser }}
          SAUCE_USER: ${{ secrets.SAUCE_USER || 'standard_user' }}
          SAUCE_PASSWORD: ${{ secrets.SAUCE_PASSWORD || 'secret_sauce' }}
        run: mvn -B test

      - name: Publicar el reporte
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: cucumber-report-${{ matrix.browser }}
          path: target/cucumber-report.html
          retention-days: 30
```

El perfil `ci` se selecciona con `TEST_ENV`, y `BROWSER` demuestra que el override por variable de entorno funciona: el mismo archivo de perfil sirve para las dos corridas de la matrix.

- [ ] **Step 2: Crear el repo remoto y pushear**

```bash
gh repo create selenium-cucumber-automation --public --source=. --remote=origin --push
```

- [ ] **Step 3: Verificar que el CI corre en verde**

```bash
gh run watch
```

Expected: los dos jobs (chrome y firefox) en verde.

Si el job de Firefox falla por localizadores o timeouts, ajustar. Si resulta inestable de forma persistente contra los sitios públicos, es aceptable dejar solo Chrome en la matrix y documentar el motivo en el README — pero intentar arreglarlo primero.

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/ci.yml && git commit -m "ci: workflow de github actions con matrix chrome y firefox" && git push
```

---

## Tarea 12: Publicar el reporte en GitHub Pages

**Files:**
- Modify: `.github/workflows/ci.yml`

- [ ] **Step 1: Agregar el job de publicación**

Agregar al final de `.github/workflows/ci.yml`:

```yaml
  publicar-reporte:
    needs: e2e
    if: always() && github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    permissions:
      contents: read
      pages: write
      id-token: write
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}

    steps:
      - name: Descargar el reporte de Chrome
        uses: actions/download-artifact@v4
        with:
          name: cucumber-report-chrome
          path: site

      - name: Renombrar a index.html
        run: mv site/cucumber-report.html site/index.html

      - uses: actions/configure-pages@v5
      - uses: actions/upload-pages-artifact@v3
        with:
          path: site
      - id: deployment
        uses: actions/deploy-pages@v4
```

- [ ] **Step 2: Habilitar Pages en el repo**

En GitHub: Settings → Pages → Source → **GitHub Actions**. Es un paso manual en la interfaz web; sin él el job falla con un error de permisos.

- [ ] **Step 3: Pushear y verificar**

```bash
git add .github/workflows/ci.yml && git commit -m "ci: publicar el reporte html en github pages" && git push && gh run watch
```

Expected: el job `publicar-reporte` en verde y el reporte accesible en `https://<usuario>.github.io/selenium-cucumber-automation/`.

---

## Tarea 13: README

**Files:**
- Create: `README.md`

- [ ] **Step 1: Escribir el README**

`README.md`:

```markdown
# selenium-cucumber-automation

[![tests](https://github.com/<usuario>/selenium-cucumber-automation/actions/workflows/ci.yml/badge.svg)](https://github.com/<usuario>/selenium-cucumber-automation/actions/workflows/ci.yml)

Suite E2E con **Selenium 4 + Cucumber 7 + JUnit 5**, contra
[SauceDemo](https://www.saucedemo.com) y
[the-internet](https://the-internet.herokuapp.com).

Parte de un portfolio de tres repos que resuelven **los mismos 11 casos** con
tres herramientas distintas:

| Repo | Stack | Estado |
|------|-------|--------|
| **selenium-cucumber-automation** (este) | Java · Selenium 4 · Cucumber | ✅ |
| playwright-automation | TypeScript · Playwright | pendiente |
| cypress-automation | JavaScript · Cypress | pendiente |

📊 **[Ver el último reporte de ejecución](https://<usuario>.github.io/selenium-cucumber-automation/)**

## Correrlo

```bash
git clone https://github.com/<usuario>/selenium-cucumber-automation.git
cd selenium-cucumber-automation
mvn test
```

No hace falta descargar drivers: Selenium Manager los resuelve solo.

Perfiles y overrides:

```bash
mvn test -DTEST_ENV=ci          # headless, 2 reintentos, timeouts cortos
mvn test -DBROWSER=firefox      # cualquier clave admite override
mvn test -Dcucumber.filter.tags="@smoke"
```

## Qué cubre

| # | Caso | Qué demuestra |
|---|------|---------------|
| 1 | Login exitoso | Flujo base |
| 2 | Login bloqueado | Assert sobre mensaje de error |
| 3 | Login data-driven | Esquema de escenario, 5 combinaciones |
| 4 | Carrito agregar/quitar | Estado y contador |
| 5 | Checkout E2E | Flujo largo + validación de totales |
| 6 | Ordenar productos | Valida el orden real de la lista |
| 7 | `performance_glitch_user` | Esperas correctas, sin sleeps |
| 8 | Dynamic loading | Elemento que aparece tarde |
| 9 | Iframe | Cambio de contexto |
| 10 | JS alerts | Accept y dismiss |
| 11 | Tabla | Lectura y validación de datos tabulares |

## Decisiones de diseño

**Sin capa de wrappers sobre Selenium.** No hay clases tipo `ButtonMethod` o
`TextBoxMethod`: envolver `element.click()` en un método propio reexpone la API
de Selenium con otro nombre. Lo único que vale encapsular son las esperas, y
viven en `BasePage`.

**Sin binarios de driver versionados.** Selenium Manager resuelve el driver en
runtime. Un `chromedriver.exe` commiteado deja de servir en cuanto el navegador
se actualiza.

**Implicit wait en cero.** Mezclar esperas implícitas y explícitas produce
tiempos impredecibles. Toda la espera es explícita.

**Aislamiento por escenario, sin `ThreadLocal`.** Picocontainer crea un
`TestContext` por escenario y Cucumber corre un escenario por hilo, así que cada
uno tiene su propio driver. Verificado corriendo en paralelo con 4 hilos.

**Configuración con precedencia uniforme.** Variable de entorno > archivo del
perfil > error explícito. Todas las claves admiten override, sin excepciones.

**El build falla si falla un test.** Sin `testFailureIgnore`: un badge que no
puede ponerse en rojo no informa nada.

## Fuera de alcance

Tests de API, visual regression, accesibilidad, ejecución programada y
ambientes propios en Docker. El perfil `staging` queda preparado para apuntar a
una URL propia cuando haga falta.
```

- [ ] **Step 2: Reemplazar `<usuario>` por el usuario real de GitHub**

- [ ] **Step 3: Verificar el criterio de éxito principal**

Clonar el repo en un directorio limpio y correrlo sin ningún paso previo:

```bash
cd /tmp && rm -rf verificacion && git clone https://github.com/<usuario>/selenium-cucumber-automation.git verificacion && cd verificacion && mvn -B test -DHEADLESS=true
```

Expected: `21 Scenarios (21 passed)` y `BUILD SUCCESS`. Si esto no pasa a la primera, el repo no cumple su objetivo principal y hay que arreglarlo antes de darlo por terminado.

- [ ] **Step 4: Commit**

```bash
git add README.md && git commit -m "docs: readme con decisiones de diseno y guia de ejecucion" && git push
```

---

## Verificación final

- [ ] `git clone` + `mvn test` en un directorio limpio pasa en verde a la primera
- [ ] El badge del CI refleja el estado real (probar rompiendo un test en una rama)
- [ ] El reporte HTML es accesible en la URL pública de GitHub Pages
- [ ] Los 21 escenarios pasan en paralelo tres corridas seguidas sin intermitencias
- [ ] No hay binarios de driver ni `target/` versionados: `git ls-files | grep -E "\.exe$|^target/"` no devuelve nada
- [ ] El README explica las decisiones y lo que quedó fuera de alcance
