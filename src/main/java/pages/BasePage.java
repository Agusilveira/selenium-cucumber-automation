package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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

    /** Espera a que la URL contenga el fragmento indicado. */
    protected void urlContains(String fragmento) {
        wait.until(ExpectedConditions.urlContains(fragmento));
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
