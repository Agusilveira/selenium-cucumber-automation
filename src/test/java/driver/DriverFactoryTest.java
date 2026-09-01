package driver;

import config.Env;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

class DriverFactoryTest {

    @Test
    void crea_un_chrome_headless_y_navega() {
        System.setProperty("HEADLESS", "true");
        WebDriver driver = DriverFactory.create(Env.load());
        try {
            driver.get("https://www.saucedemo.com");
            assertThat(driver.getTitle()).isEqualTo("Swag Labs");
        } finally {
            driver.quit();
            System.clearProperty("HEADLESS");
        }
    }
}
