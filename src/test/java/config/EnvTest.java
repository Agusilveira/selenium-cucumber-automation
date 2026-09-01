package config;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvTest {

    private Env envWith(Properties props, Map<String, String> overrides) {
        return new Env(props, overrides::get);
    }

    private Properties props(String... kv) {
        Properties p = new Properties();
        for (int i = 0; i < kv.length; i += 2) p.setProperty(kv[i], kv[i + 1]);
        return p;
    }

    @Test
    void usa_el_valor_del_archivo_cuando_no_hay_override() {
        Env env = envWith(props("base.url", "https://del-archivo"), Map.of());
        assertThat(env.get("base.url")).isEqualTo("https://del-archivo");
    }

    @Test
    void la_variable_de_entorno_le_gana_al_archivo() {
        Env env = envWith(props("base.url", "https://del-archivo"),
                          Map.of("BASE_URL", "https://del-entorno"));
        assertThat(env.get("base.url")).isEqualTo("https://del-entorno");
    }

    @Test
    void convierte_la_clave_a_formato_de_variable_de_entorno() {
        Env env = envWith(props("page.load.timeout", "30"),
                          Map.of("PAGE_LOAD_TIMEOUT", "5"));
        assertThat(env.getInt("page.load.timeout")).isEqualTo(5);
    }

    @Test
    void un_override_vacio_no_pisa_el_archivo() {
        Env env = envWith(props("base.url", "https://del-archivo"),
                          Map.of("BASE_URL", "   "));
        assertThat(env.get("base.url")).isEqualTo("https://del-archivo");
    }

    @Test
    void falla_con_mensaje_claro_si_falta_la_clave() {
        Env env = envWith(props(), Map.of());
        assertThatThrownBy(() -> env.get("base.url"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("base.url");
    }

    @Test
    void lee_booleanos() {
        Env env = envWith(props("headless", "true"), Map.of());
        assertThat(env.getBool("headless")).isTrue();
    }

    @Test
    void falla_con_mensaje_claro_si_el_numero_no_es_valido() {
        Env env = envWith(props("page.load.timeout", "treinta"), Map.of());
        assertThatThrownBy(() -> env.getInt("page.load.timeout"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("page.load.timeout");
    }
}
