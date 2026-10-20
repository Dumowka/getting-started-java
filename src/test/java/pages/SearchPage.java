package pages;

import io.qameta.allure.Step;
import pages.components.SearchInputComponent;
import pages.components.SearchResultsComponent;
import pages.components.SearchTypeNavigationComponent;

public class SearchPage extends BasePage {
    private final SearchInputComponent searchInput = new SearchInputComponent();
    private final SearchResultsComponent searchResults = new SearchResultsComponent();
    private final SearchTypeNavigationComponent searchTypeNavigation = new SearchTypeNavigationComponent();

    @Step("Open search page")
    public SearchPage openPage() {
        openPath("/search");
        return this;
    }

    @Step("Search for \"{query}\"")
    public SearchPage search(String query) {
        searchInput.search(query);
        return this;
    }

    @Step("Check that results contain text \"{text}\"")
    public SearchPage shouldHaveResultsWithText(String text) {
        searchResults.shouldHaveText(text);
        return this;
    }

    @Step("Check that all results are from repository \"{repository}\"")
    public SearchPage shouldHaveOnlyResultsFromRepository(String repository) {
        searchResults.shouldHaveOnlyRepository(repository);
        return this;
    }

    @Step("Check that no matching repositories message is shown")
    public SearchPage shouldShowNoMatchingRepositoriesMessage() {
        searchResults.shouldShowNoMatchingRepositoriesMessage();
        return this;
    }

    @Step("Check that repositories count is \"{count}\"")
    public SearchPage shouldHaveRepositoriesCount(String count) {
        searchTypeNavigation.shouldHaveRepositoriesCount(count);
        return this;
    }

    @Step("Check that results list is absent")
    public SearchPage shouldNotHaveResultsList() {
        searchResults.shouldNotExist();
        return this;
    }
}
