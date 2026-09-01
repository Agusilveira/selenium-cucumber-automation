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
