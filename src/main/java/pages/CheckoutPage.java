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
