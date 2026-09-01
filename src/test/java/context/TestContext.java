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
