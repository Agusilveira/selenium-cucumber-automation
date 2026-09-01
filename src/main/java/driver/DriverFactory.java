package driver;

import config.Env;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Crea un WebDriver ya configurado. Selenium Manager (incluido desde 4.6)
 * resuelve y descarga el binario del driver: no hay ejecutables versionados
 * en el repo ni propiedades webdriver.* que setear.
 *
 * El implicit wait queda en cero a propósito: mezclarlo con esperas explícitas
 * produce tiempos impredecibles. Toda la espera vive en BasePage.
 */
public final class DriverFactory {

    /**
     * Selenium busca una versión de CDP que coincida con el Chrome instalado y avisa
     * por cada driver cuando no la encuentra. Este framework no usa DevTools, así que
     * el aviso solo ensucia la salida.
     *
     * Las referencias son campos y no variables locales a propósito: java.util.logging
     * mantiene los loggers con referencias débiles, y uno sin referencia fuerte se
     * recolecta y vuelve al nivel por defecto.
     */
    private static final Logger[] SILENCIADOS = {
            Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder"),
            Logger.getLogger("org.openqa.selenium.devtools"),
            Logger.getLogger("org.openqa.selenium.chromium.ChromiumDriver"),
            Logger.getLogger("org.openqa.selenium.chromium")
    };

    static {
        for (Logger logger : SILENCIADOS) logger.setLevel(Level.SEVERE);
    }

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
