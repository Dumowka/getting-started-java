package pages.components;

import static com.codeborne.selenide.Selenide.$;

public class SearchInputComponent {
    public void search(String query) {
        $("[aria-label='Search GitHub']").setValue(query).pressEnter();
    }
}
