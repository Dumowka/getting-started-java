package pages;

import pages.components.SearchInputComponent;
import pages.components.SearchResultsComponent;
import pages.components.SearchTypeNavigationComponent;

public class SearchPage extends BasePage {
    private final SearchInputComponent searchInput = new SearchInputComponent();
    private final SearchResultsComponent searchResults = new SearchResultsComponent();
    private final SearchTypeNavigationComponent searchTypeNavigation = new SearchTypeNavigationComponent();

    public SearchPage openPage() {
        openPath("/search");
        return this;
    }

    public SearchPage search(String query) {
        searchInput.search(query);
        return this;
    }

    public SearchPage shouldHaveResultsWithText(String text) {
        searchResults.shouldHaveText(text);
        return this;
    }

    public SearchPage shouldHaveOnlyResultsFromRepository(String repository) {
        searchResults.shouldHaveOnlyRepository(repository);
        return this;
    }

    public SearchPage shouldShowNoMatchingRepositoriesMessage() {
        searchResults.shouldShowNoMatchingRepositoriesMessage();
        return this;
    }

    public SearchPage shouldHaveRepositoriesCount(String count) {
        searchTypeNavigation.shouldHaveRepositoriesCount(count);
        return this;
    }

    public SearchPage shouldNotHaveResultsList() {
        searchResults.shouldNotExist();
        return this;
    }
}
