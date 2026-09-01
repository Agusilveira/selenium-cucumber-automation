package pages;

import config.Env;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WidgetsPage extends BasePage {

    private static final By START_BUTTON = By.cssSelector("#start button");
    private static final By FINISH_TEXT  = By.id("finish");
    private static final By ALERT_RESULT = By.id("result");

    public WidgetsPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public void open(String path) {
        driver.get(env.widgetsUrl() + path);
    }

    // --- Dynamic loading ---

    public void startDeferredLoad() {
        clickable(START_BUTTON).click();
    }

    /** Espera a que el elemento exista y sea visible. Sin sleeps. */
    public String loadedText() {
        return visible(FINISH_TEXT).getText();
    }

    // --- Frames anidados ---

    /**
     * Entra por una cadena de frames anidados, buscándolos por nombre, y devuelve
     * el texto del último. Siempre parte del documento raíz y vuelve a él, así el
     * contexto queda limpio para el paso siguiente: un switchTo() olvidado es de
     * las causas más difíciles de diagnosticar cuando falla un test más adelante.
     */
    public String textInsideFrames(String... nombres) {
        driver.switchTo().defaultContent();
        try {
            for (String nombre : nombres) {
                driver.switchTo().frame(nombre);
            }
            return visible(By.tagName("body")).getText().trim();
        } finally {
            driver.switchTo().defaultContent();
        }
    }

    // --- Alertas ---

    public void triggerAlert(String tipo) {
        String selector = switch (tipo) {
            case "alert"   -> "button[onclick='jsAlert()']";
            case "confirm" -> "button[onclick='jsConfirm()']";
            case "prompt"  -> "button[onclick='jsPrompt()']";
            default -> throw new IllegalArgumentException("Tipo de alerta desconocido: " + tipo);
        };
        clickable(By.cssSelector(selector)).click();
    }

    public void handleAlert(String accion) {
        Alert alert = new WebDriverWait(driver, Duration.ofSeconds(env.explicitTimeout()))
                .until(ExpectedConditions.alertIsPresent());
        if ("acepto".equals(accion)) {
            alert.accept();
        } else if ("descarto".equals(accion)) {
            alert.dismiss();
        } else {
            throw new IllegalArgumentException("Acción desconocida: " + accion);
        }
    }

    public String alertResult() {
        return visible(ALERT_RESULT).getText();
    }

    // --- Tabla ---

    /** allVisible y no findElements: leer sin esperar devuelve 0 si la tabla no renderizo todavia. */
    public int tableRowCount() {
        return allVisible(By.cssSelector("#table1 tbody tr")).size();
    }

    /** Devuelve los valores de la columna con el encabezado dado. */
    public List<String> columnValues(String encabezado) {
        List<WebElement> headers = allVisible(By.cssSelector("#table1 thead th"));
        int indice = -1;
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().trim().equalsIgnoreCase(encabezado)) {
                indice = i + 1;
                break;
            }
        }
        if (indice < 0) {
            throw new IllegalArgumentException("La tabla no tiene la columna '" + encabezado + "'");
        }
        return driver.findElements(By.cssSelector("#table1 tbody tr td:nth-child(" + indice + ")"))
                .stream().map(WebElement::getText).map(String::trim).toList();
    }
}
