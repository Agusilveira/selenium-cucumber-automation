package hooks;

import context.TestContext;
import io.cucumber.java.After;

public class Hooks {

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @After(order = 0)
    public void cerrarNavegador() {
        context.quitDriver();
    }
}
