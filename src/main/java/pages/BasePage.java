package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
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
    private final WebDriverWait shortWait;

    protected BasePage(WebDriver driver, Env env) {
        this.driver = driver;
        this.env = env;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(env.explicitTimeout()));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
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

    /** Espera a que la URL contenga el fragmento indicado. */
    protected void urlContains(String fragmento) {
        wait.until(ExpectedConditions.urlContains(fragmento));
    }

    /**
     * Hace click y confirma que el efecto esperado ocurra; si no, reintenta.
     *
     * SauceDemo es una aplicacion JavaScript: entre que un boton pasa a ser
     * visible y clickeable y que su handler queda enlazado hay una ventana en la
     * que el click se dispara y la pagina no reacciona. Localmente dura
     * milisegundos y no se nota; en un runner de CI se abre lo suficiente como
     * para perder clicks en cualquier pagina del flujo.
     *
     * Techo conocido: 3 intentos con 4 segundos de espera cada uno. Si una accion
     * necesitara mas, el problema es otro y conviene diagnosticarlo, no subir el
     * numero.
     */
    protected void clickUntil(By boton, ExpectedCondition<?> efecto) {
        TimeoutException ultimoError = null;
        for (int intento = 1; intento <= 3; intento++) {
            clickable(boton).click();
            try {
                shortWait.until(efecto);
                if (intento > 1) {
                    System.out.println("Click reintentado " + intento + " veces en " + boton);
                }
                return;
            } catch (TimeoutException e) {
                ultimoError = e;
            }
        }
        // Ultimo recurso: click por JavaScript.
        //
        // Verificado en el CI que el elemento y su handler estan bien: un click por
        // JS sobre el mismo boton funciona cuando el nativo no. Instrumentando la
        // pagina con un listener propio, el evento del click nativo directamente no
        // llega (recibidos: []), sin que Selenium lance ninguna excepcion. Es un
        // problema de entrega de eventos de Chrome headless en el runner, no del
        // test ni de la aplicacion.
        //
        // Cuesta fidelidad: un click por JS no ejercita el mismo camino que el de
        // una persona. Por eso es el ultimo recurso y no el metodo por defecto, y
        // avisa cada vez que hace falta. Si empieza a aparecer seguido, el problema
        // de entrega volvio y hay que atacarlo de nuevo, no acostumbrarse a esto.
        System.out.println("Click nativo sin efecto en " + boton + ", usando JavaScript");
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", driver.findElement(boton));
        try {
            shortWait.until(efecto);
            return;
        } catch (TimeoutException e) {
            throw ultimoError;
        }
    }

    /** True si el elemento aparece dentro del timeout; false si no. No lanza. */
    protected boolean isVisible(By locator) {
        try {
            visible(locator);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
