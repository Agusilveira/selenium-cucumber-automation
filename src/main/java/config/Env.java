package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

/**
 * Resuelve la configuración con precedencia explícita y uniforme:
 * variable de entorno > archivo del perfil > error.
 *
 * La clave "page.load.timeout" se busca en el entorno como PAGE_LOAD_TIMEOUT.
 * Todas las claves admiten override, sin excepciones arbitrarias.
 */
public final class Env {

    private final Properties props;
    private final Function<String, String> overrides;

    Env(Properties props, Function<String, String> overrides) {
        this.props = props;
        this.overrides = overrides;
    }

    /** Carga el perfil indicado por -Denv o TEST_ENV. Default: local. */
    public static Env load() {
        String profile = System.getProperty("env");
        if (profile == null || profile.isBlank()) profile = System.getenv("TEST_ENV");
        if (profile == null || profile.isBlank()) profile = "local";
        return new Env(readProfile(profile), Env::fromSystem);
    }

    private static String fromSystem(String envKey) {
        String v = System.getProperty(envKey);
        return v != null ? v : System.getenv(envKey);
    }

    private static Properties readProfile(String profile) {
        String resource = "/config/" + profile + ".properties";
        try (InputStream in = Env.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("No existe el perfil de configuración: " + resource);
            }
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el perfil " + resource, e);
        }
    }

    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    public String get(String key) {
        String override = overrides.apply(toEnvKey(key));
        if (override != null && !override.isBlank()) return override;

        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Falta la clave de configuración '" + key + "'. "
                    + "Definila en el archivo del perfil o exportá " + toEnvKey(key) + ".");
        }
        return value;
    }

    public String get(String key, String defaultValue) {
        try {
            return get(key);
        } catch (IllegalStateException e) {
            return defaultValue;
        }
    }

    public int getInt(String key) {
        String raw = get(key);
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La clave '" + key + "' debe ser un número entero, pero vale '" + raw + "'.", e);
        }
    }

    public boolean getBool(String key) {
        return Boolean.parseBoolean(get(key).trim());
    }

    public String baseUrl()      { return get("base.url"); }
    public String widgetsUrl()   { return get("widgets.url"); }
    public String browser()      { return get("browser"); }
    public boolean headless()    { return getBool("headless"); }
    public int pageLoadTimeout() { return getInt("page.load.timeout"); }
    public int explicitTimeout() { return getInt("explicit.timeout"); }
    public String user()         { return get("sauce.user"); }
    public String password()     { return get("sauce.password"); }
}
