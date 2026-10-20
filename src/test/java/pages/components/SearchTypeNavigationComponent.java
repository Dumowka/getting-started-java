package pages.components;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selenide.$;

public class SearchTypeNavigationComponent {
    public void shouldHaveRepositoriesCount(String count) {
        $("[data-testid='nav-item-repositories'] [data-testid='resolved-count-label']").shouldHave(exactText(count));
    }
}
