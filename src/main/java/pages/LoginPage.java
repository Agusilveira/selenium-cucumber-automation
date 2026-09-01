package pages;

import config.Env;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private static final By USERNAME = By.cssSelector("[data-test='username']");
    private static final By PASSWORD = By.cssSelector("[data-test='password']");
    private static final By LOGIN    = By.cssSelector("[data-test='login-button']");
    private static final By ERROR    = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver, Env env) {
        super(driver, env);
    }

    public void open() {
        driver.get(env.baseUrl());
    }

    public void loginAs(String user, String password) {
        visible(USERNAME).sendKeys(user);
        visible(PASSWORD).sendKeys(password);
        clickable(LOGIN).click();
    }

    public String errorMessage() {
        return visible(ERROR).getText();
    }
}
