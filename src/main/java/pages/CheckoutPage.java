package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

    /**
     * Los campos se buscan con clickable y no con visible: un input puede estar
     * visible pero todavia no aceptar texto, y un sendKeys que se pierde deja el
     * formulario incompleto. Ahi SauceDemo se queda en el paso uno y el fallo
     * aparece mas adelante, buscando elementos de una pagina a la que nunca llego.
     * Esperar la navegacion convierte eso en un error inmediato y claro.
     */
    public void fillDetails(String nombre, String apellido, String codigoPostal) {
        escribir(FIRST_NAME, nombre);
        escribir(LAST_NAME, apellido);
        escribir(POSTAL, codigoPostal);
        clickUntil(CONTINUE, ExpectedConditions.urlContains("checkout-step-two.html"));
    }

    /**
     * Escribe y verifica que el valor haya quedado, con el mismo ultimo recurso que
     * clickUntil: en el runner del CI, Chrome headless a veces no entrega los
     * eventos de entrada y el sendKeys se pierde sin lanzar ningun error. Un
     * formulario incompleto deja a SauceDemo en el paso uno, y el sintoma aparece
     * varios pasos despues buscando elementos de una pagina a la que nunca llego.
     *
     * El valor de reserva se asigna con el setter nativo de HTMLInputElement y
     * disparando el evento input: SauceDemo es React y una asignacion directa a
     * .value no actualiza su estado interno.
     */
    private void escribir(By campo, String valor) {
        WebElement input = clickable(campo);
        input.clear();
        input.sendKeys(valor);
        if (valor.equals(input.getDomProperty("value"))) return;

        System.out.println("sendKeys sin efecto en " + campo + ", asignando por JavaScript");
        ((JavascriptExecutor) driver).executeScript(
                "const setter = Object.getOwnPropertyDescriptor("
              + "    window.HTMLInputElement.prototype, 'value').set;"
              + "setter.call(arguments[0], arguments[1]);"
              + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));",
                input, valor);
    }

    public void finish() {
        clickUntil(FINISH, ExpectedConditions.urlContains("checkout-complete.html"));
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
