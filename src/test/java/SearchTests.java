import org.junit.jupiter.api.Test;

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
import static com.codeborne.selenide.Selenide.open;

public class SearchTests extends TestBase {
    @Test
    void successfulSearchTest() {
        open("/search");
        $("[aria-label='Search GitHub']").setValue("qa.guru").pressEnter();
        $("[data-testid='results-list']").shouldHave(text("QA.GURU"));
    }

    @Test
    void repoQualifierSearchTest() {
        open("/search");
        $("[aria-label='Search GitHub']").setValue("repo:selenide/selenide").pressEnter();
        $$("[data-testid='results-list'] a[data-hovercard-type='repository']")
                .shouldHave(sizeGreaterThan(0))
                .filterBy(not(exactText("selenide/selenide")))
                .shouldHave(size(0));
    }

    @Test
    void searchWithoutResultsTest() {
        open("/search");
        $("[aria-label='Search GitHub']").setValue("qaguruphase2nomatchzx7q9w").pressEnter();
        $(byRole("heading", "Your search did not match any repositories")).shouldBe(visible);
        $("[data-testid='nav-item-repositories'] [data-testid='resolved-count-label']").shouldHave(exactText("0"));
        $("[data-testid='results-list']").shouldNot(exist);
    }
}
