package pages.components;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byRole;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class SearchResultsComponent {
    private final SelenideElement resultsList = $("[data-testid='results-list']");

    public void shouldHaveText(String text) {
        resultsList.shouldHave(text(text));
    }

    public void shouldHaveOnlyRepository(String repository) {
        $$("[data-testid='results-list'] a[data-hovercard-type='repository']")
                .shouldHave(sizeGreaterThan(0))
                .filterBy(not(exactText(repository)))
                .shouldHave(size(0));
    }

    public void shouldShowNoMatchingRepositoriesMessage() {
        $(byRole("heading", "Your search did not match any repositories")).shouldBe(visible);
    }

    public void shouldNotExist() {
        resultsList.shouldNot(exist);
    }
}
