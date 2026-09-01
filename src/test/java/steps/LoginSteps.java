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
}
