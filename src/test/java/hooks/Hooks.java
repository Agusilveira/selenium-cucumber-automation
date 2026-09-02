package hooks;

import context.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class Hooks {

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    /**
     * Adjunta screenshot y HTML al reporte cuando el escenario falla.
     * Corre antes del cierre del navegador: en Cucumber, los @After con order
     * más alto se ejecutan primero.
     */
    @After(order = 1)
    public void capturarEvidenciaSiFalla(Scenario scenario) {
        if (!scenario.isFailed()) return;
        try {
            byte[] png = ((TakesScreenshot) context.driver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(png, "image/png", scenario.getName());
            // Ademas del adjunto, guarda el PNG como archivo para poder mirarlo
            // directo desde los artefactos del CI.
            java.nio.file.Path dir = java.nio.file.Path.of("target", "screenshots");
            java.nio.file.Files.createDirectories(dir);
            String nombre = scenario.getName().replaceAll("[^a-zA-Z0-9]+", "-") + ".png";
            java.nio.file.Files.write(dir.resolve(nombre), png);
            scenario.attach(context.driver().getPageSource(), "text/html", "page-source");
        } catch (Exception e) {
            scenario.log("No se pudo capturar la evidencia: " + e.getMessage());
        }
    }

    @After(order = 0)
    public void cerrarNavegador() {
        context.quitDriver();
    }
}
