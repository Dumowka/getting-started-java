package pages;

import static com.codeborne.selenide.Selenide.open;

public abstract class BasePage {
    protected void openPath(String path) {
        open(path);
    }
}
