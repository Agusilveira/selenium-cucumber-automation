package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckoutSteps {

    private final TestContext context;

    public CheckoutSteps(TestContext context) {
        this.context = context;
    }

    @Cuando("inicio el checkout")
    public void inicio_el_checkout() {
        context.cartPage().checkout();
    }

    @Cuando("completo mis datos {string}, {string} y {string}")
    public void completo_mis_datos(String nombre, String apellido, String codigoPostal) {
        context.checkoutPage().fillDetails(nombre, apellido, codigoPostal);
    }

    @Cuando("confirmo la compra")
    public void confirmo_la_compra() {
        context.checkoutPage().finish();
    }

    @Entonces("veo la confirmación {string}")
    public void veo_la_confirmacion(String mensaje) {
        assertThat(context.checkoutPage().confirmationMessage())
                .as("mensaje de confirmación de la compra")
                .isEqualTo(mensaje);
    }

    @Entonces("el total incluye impuestos")
    public void el_total_incluye_impuestos() {
        var page = context.checkoutPage();
        assertThat(page.total())
                .as("el total debería ser subtotal más impuestos")
                .isEqualByComparingTo(page.subtotal().add(page.tax()));
    }
}
