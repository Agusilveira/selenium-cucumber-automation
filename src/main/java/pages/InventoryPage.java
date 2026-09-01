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
    private String slug(String producto) {
        return producto.toLowerCase().replace("(", "").replace(")", "").replace(" ", "-");
    }

    private By addButtonFor(String producto) {
        return By.cssSelector("[data-test='add-to-cart-" + slug(producto) + "']");
    }

    private By removeButtonFor(String producto) {
        return By.cssSelector("[data-test='remove-" + slug(producto) + "']");
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
