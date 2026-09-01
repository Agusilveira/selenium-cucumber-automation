package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CartSteps {

    private final TestContext context;

    public CartSteps(TestContext context) {
        this.context = context;
    }

    @Cuando("agrego al carrito el producto {string}")
    public void agrego_al_carrito(String producto) {
        context.inventoryPage().addToCart(producto);
    }

    @Cuando("quito del carrito el producto {string}")
    public void quito_del_carrito(String producto) {
        context.inventoryPage().removeFromCart(producto);
    }

    @Cuando("abro el carrito")
    public void abro_el_carrito() {
        context.inventoryPage().openCart();
    }

    @Entonces("el contador del carrito muestra {int}")
    public void el_contador_del_carrito_muestra(int esperado) {
        assertThat(context.inventoryPage().cartCount())
                .as("cantidad de productos en el badge del carrito")
                .isEqualTo(esperado);
    }

    @Entonces("el carrito contiene {string}")
    public void el_carrito_contiene(String producto) {
        assertThat(context.cartPage().items())
                .as("productos listados en el carrito")
                .contains(producto);
    }

    @Cuando("ordeno los productos por {string}")
    public void ordeno_los_productos_por(String criterio) {
        context.inventoryPage().sortBy(criterio);
    }

    @Entonces("los productos quedan ordenados por {string}")
    public void los_productos_quedan_ordenados_por(String criterio) {
        switch (criterio) {
            case "nombre ascendente" -> {
                List<String> nombres = context.inventoryPage().productNames();
                assertThat(nombres).as("nombres de productos").isSorted();
            }
            case "nombre descendente" -> {
                List<String> nombres = context.inventoryPage().productNames();
                assertThat(nombres).as("nombres de productos")
                        .isSortedAccordingTo(Comparator.reverseOrder());
            }
            case "precio ascendente" -> {
                List<BigDecimal> precios = context.inventoryPage().productPrices();
                assertThat(precios).as("precios de productos").isSorted();
            }
            case "precio descendente" -> {
                List<BigDecimal> precios = context.inventoryPage().productPrices();
                assertThat(precios).as("precios de productos")
                        .isSortedAccordingTo(Comparator.reverseOrder());
            }
            default -> throw new IllegalArgumentException("Criterio desconocido: " + criterio);
        }
    }
}
