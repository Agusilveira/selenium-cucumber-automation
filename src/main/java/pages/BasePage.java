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
        diagnosticarClickPerdido(boton);
        throw ultimoError;
    }

    // DIAGNOSTICO TEMPORAL: que hay realmente en el punto donde aterriza el click
    private void diagnosticarClickPerdido(By boton) {
        try {
            WebElement el = driver.findElement(boton);
            Object info = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "const e = arguments[0];"
                    + "const r = e.getBoundingClientRect();"
                    + "const cx = r.left + r.width/2, cy = r.top + r.height/2;"
                    + "const enPunto = document.elementFromPoint(cx, cy);"
                    + "return JSON.stringify({"
                    + " boton: e.outerHTML.slice(0,150),"
                    + " rect: [Math.round(r.left), Math.round(r.top), Math.round(r.width), Math.round(r.height)],"
                    + " scroll: [Math.round(window.scrollX), Math.round(window.scrollY)],"
                    + " enPunto: enPunto ? enPunto.outerHTML.slice(0,150) : null,"
                    + " esElMismo: enPunto === e,"
                    + " readyState: document.readyState"
                    + "});", el);
            System.out.println("DIAGCLICK|" + boton + "|" + info);
        } catch (Exception e) {
            System.out.println("DIAGCLICK|" + boton + "|error=" + e.getMessage());
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
