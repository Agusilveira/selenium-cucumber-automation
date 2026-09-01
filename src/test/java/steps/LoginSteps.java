package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps {

    private final TestContext context;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Dado("que estoy en la página de login")
    public void que_estoy_en_la_pagina_de_login() {
        context.loginPage().open();
    }

    @Cuando("ingreso con el usuario {string}")
    public void ingreso_con_el_usuario(String usuario) {
        context.loginPage().loginAs(usuario, context.env().password());
    }

    @Entonces("veo la lista de productos")
    public void veo_la_lista_de_productos() {
        assertThat(context.inventoryPage().isLoaded())
                .as("la lista de productos debería estar visible")
                .isTrue();
    }

    @Cuando("ingreso con el usuario {string} y la contraseña {string}")
    public void ingreso_con_usuario_y_contrasenia(String usuario, String contrasenia) {
        context.loginPage().loginAs(usuario, contrasenia);
    }

    @Entonces("veo el mensaje de error {string}")
    public void veo_el_mensaje_de_error(String mensaje) {
        assertThat(context.loginPage().errorMessage())
                .as("el mensaje de error mostrado")
                .contains(mensaje);
    }

    @Entonces("el resultado del login es {string}")
    public void el_resultado_del_login_es(String resultado) {
        switch (resultado) {
            case "exito" -> assertThat(context.inventoryPage().isLoaded())
                    .as("esperaba entrar al inventario")
                    .isTrue();
            case "error" -> assertThat(context.loginPage().errorMessage())
                    .as("esperaba un mensaje de error")
                    .startsWith("Epic sadface");
            default -> throw new IllegalArgumentException(
                    "Resultado no reconocido: '" + resultado + "'. Usá exito o error.");
        }
    }
}
