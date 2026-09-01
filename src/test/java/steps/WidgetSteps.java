package steps;

import context.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static org.assertj.core.api.Assertions.assertThat;

public class WidgetSteps {

    private final TestContext context;

    public WidgetSteps(TestContext context) {
        this.context = context;
    }

    @Dado("que abro la página de widgets {string}")
    public void que_abro_la_pagina_de_widgets(String path) {
        context.widgetsPage().open(path);
    }

    @Cuando("disparo la carga diferida")
    public void disparo_la_carga_diferida() {
        context.widgetsPage().startDeferredLoad();
    }

    @Entonces("el texto cargado es {string}")
    public void el_texto_cargado_es(String esperado) {
        assertThat(context.widgetsPage().loadedText())
                .as("texto que aparece después de la carga diferida")
                .isEqualTo(esperado);
    }

    @Entonces("el texto del frame {string} es {string}")
    public void el_texto_del_frame_es(String ruta, String esperado) {
        String[] nombres = ruta.split("\\s*>\\s*");
        assertThat(context.widgetsPage().textInsideFrames(nombres))
                .as("texto dentro de " + ruta)
                .isEqualTo(esperado);
    }

    @Cuando("disparo la alerta {string} y la {string}")
    public void disparo_la_alerta_y_la(String tipo, String accion) {
        context.widgetsPage().triggerAlert(tipo);
        context.widgetsPage().handleAlert(accion);
    }

    @Entonces("el resultado indica {string}")
    public void el_resultado_indica(String esperado) {
        assertThat(context.widgetsPage().alertResult())
                .as("texto de resultado tras manejar la alerta")
                .isEqualTo(esperado);
    }

    @Entonces("la tabla tiene {int} filas")
    public void la_tabla_tiene_filas(int esperadas) {
        assertThat(context.widgetsPage().tableRowCount())
                .as("cantidad de filas de la tabla")
                .isEqualTo(esperadas);
    }

    @Entonces("la columna {string} contiene {string}")
    public void la_columna_contiene(String columna, String valor) {
        assertThat(context.widgetsPage().columnValues(columna))
                .as("valores de la columna " + columna)
                .contains(valor);
    }
}
