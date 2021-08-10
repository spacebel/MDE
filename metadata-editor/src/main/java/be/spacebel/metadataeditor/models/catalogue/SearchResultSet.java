package be.spacebel.metadataeditor.models.catalogue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a list of search results items
 *
 * @author mng
 */
public class SearchResultSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SearchResultItem> items;
    private String firstPageLink;
    private String previousPageLink;
    private String nextPageLink;
    private String lastPageLink;
    private int totalResults;

    public SearchResultSet() {
        items = new ArrayList<>();
        this.totalResults = -1;
    }

    public void addItem(SearchResultItem item) {
        item.setIndex(items.size());
        items.add(item);
    }

    /* GETTERS AND SETTERS */
    public List<SearchResultItem> getItems() {
        return items;
    }

    public void setItems(List<SearchResultItem> items) {
        this.items = items;
    }

    public String getFirstPageLink() {
        return firstPageLink;
    }

    public void setFirstPageLink(String firstPageLink) {
        this.firstPageLink = firstPageLink;
    }

    public String getPreviousPageLink() {
        return previousPageLink;
    }

    public void setPreviousPageLink(String previousPageLink) {
        this.previousPageLink = previousPageLink;
    }

    public String getNextPageLink() {
        return nextPageLink;
    }

    public void setNextPageLink(String nextPageLink) {
        this.nextPageLink = nextPageLink;
    }

    public String getLastPageLink() {
        return lastPageLink;
    }

    public void setLastPageLink(String lastPageLink) {
        this.lastPageLink = lastPageLink;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

}
