package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
        clickUntil(CHECKOUT, ExpectedConditions.urlContains("checkout-step-one.html"));
    }
}
