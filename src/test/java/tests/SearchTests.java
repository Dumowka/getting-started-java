package tests;

import org.junit.jupiter.api.Test;
import pages.SearchPage;

public class SearchTests extends TestBase {
    private final SearchPage searchPage = new SearchPage();

    @Test
    void successfulSearchTest() {
        searchPage.openPage()
                .search("qa.guru")
                .shouldHaveResultsWithText("QA.GURU");
    }

    @Test
    void repoQualifierSearchTest() {
        searchPage.openPage()
                .search("repo:selenide/selenide")
                .shouldHaveOnlyResultsFromRepository("selenide/selenide");
    }

    @Test
    void searchWithoutResultsTest() {
        searchPage.openPage()
                .search("qaguruphase2nomatchzx7q9w")
                .shouldShowNoMatchingRepositoriesMessage()
                .shouldHaveRepositoriesCount("0")
                .shouldNotHaveResultsList();
    }
}
