/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.business;

import be.spacebel.metadataeditor.utils.catalogue.CatalogueClient;
import be.spacebel.metadataeditor.models.catalogue.SearchResultItem;
import be.spacebel.metadataeditor.models.catalogue.SearchResultModel;
import be.spacebel.metadataeditor.models.catalogue.SearchResultSet;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Dynamic data paginator
 *
 * @author mng
 */
public class DynamicPaginator implements Serializable {

    private static final String START_INDEX = "os_startIndex";

    private String first;
    private String previous;
    private String next;
    private String last;
    private int page;
    private int backupPage;
    private int pageCount;
    private int pageSize;
    private int itemCount;
    private SearchResultModel data;
    private final CatalogueClient catalogueClient;
    /*
     the variables are used only when total of results is returned
     */
    private boolean showPageInfo;

    private Map<String, String> inputParams;

    private final Logger log = Logger.getLogger(getClass());
    private final boolean serviceSearch;

    public DynamicPaginator(CatalogueClient catalogueClient, boolean serviceSearch, boolean reset) {
        this.catalogueClient = catalogueClient;
        if (reset) {
            reset();
        }
        this.serviceSearch = serviceSearch;
    }

    public DynamicPaginator(CatalogueClient catalogueClient,
            String first, String previous, String next, String last, int itemCount,
            List<SearchResultItem> items, int pageSize, boolean serviceSearch) {
        this.catalogueClient = catalogueClient;
        this.first = first;
        this.previous = previous;
        this.next = next;
        this.last = last;
        this.data = new SearchResultModel(items);
        this.page = 1;
        this.backupPage = this.page;
        this.pageSize = pageSize;
        this.itemCount = itemCount;
        //this.processManager = new ProcessManager();
        this.showPageInfo = itemCount > 0;

        if (itemCount > -1) {
            this.pageCount = (int) Math.ceil(itemCount * 1d / pageSize);
        } else {
            this.pageCount = 0;
        }
        this.serviceSearch = serviceSearch;
    }

    public void pageNavigate(String where) {
        log.debug("pageNavigate : " + where);
        try {
            if ("first".equals(where)) {
                if (this.showPageInfo) {
                    this.page = 1;
                    navigate();
                } else {
                    if (StringUtils.isNotEmpty(this.first)) {
                        navigate(this.first);
                        this.page = 1;
                    } else {
                        FacesMessageUtil.addErrorMessage("Could not find the URL of first page in Atom response.");
                    }
                }
            }

            if ("previous".equals(where)) {
                if (this.showPageInfo) {
                    this.page = this.page - 1;
                    navigate();
                } else {
                    if (StringUtils.isNotEmpty(this.previous)) {
                        navigate(this.previous);
                        this.page -= 1;
                    } else {
                        FacesMessageUtil.addErrorMessage("Could not find the URL of previous page in Atom response.");
                    }
                }
            }

            if ("next".equals(where)) {
                if (this.showPageInfo) {
                    this.page = this.page + 1;
                    navigate();
                } else {
                    if (StringUtils.isNotEmpty(this.next)) {
                        navigate(this.next);
                        this.page += 1;
                    } else {
                        FacesMessageUtil.addErrorMessage("Could not find the URL of next page in Atom response.");
                    }
                }
            }

            if ("last".equals(where)) {
                if (this.showPageInfo) {
                    this.page = this.pageCount;
                    navigate();
                } else {
                    if (StringUtils.isNotEmpty(this.last)) {
                        navigate(this.last);
                        if (this.itemCount > -1) {
                            this.page = this.pageCount;
                        } else {
                            this.page += 1;
                        }
                    } else {
                        FacesMessageUtil.addErrorMessage("Could not find the URL of previous page in Atom response.");
                    }
                }
            }

            this.backupPage = this.page;
        } catch (SearchException e) {
            log.debug(e);
            reset();
            FacesMessageUtil.addErrorMessage(e.getTitle());
        } catch (IOException e) {
            log.error(e);
            FacesMessageUtil.addErrorMessage(e);
        }
    }

    public void jumpToPage(int targetPage) {
        this.page = targetPage;
        log.debug("Jump to page: " + this.page);
        try {
            navigate();
            this.backupPage = this.page;
        } catch (SearchException e) {
            log.debug(e);
            reset();
            FacesMessageUtil.addErrorMessage(e.getTitle());

        } catch (IOException e) {
            log.error(e);
            FacesMessageUtil.addErrorMessage(e);
        }
    }

    public String getPageInfo() {
        //log.debug("getPageInfo...........");

        // Page 1 of 3 or Page 1
        if (data != null && this.data.getItems() != null && !this.data.getItems().isEmpty()) {
            StringBuilder strBuf = new StringBuilder();
            strBuf.append("Page ").append(this.page);
            if (this.itemCount > -1) {
                strBuf.append(" of ").append(this.pageCount);
            }
            return strBuf.toString();
        } else {
            return null;
        }

    }

    public String getRecordInfo() {
        //log.debug("getPageInfo...........");

        // Results 1-10 of 23 or Results 1-10
        if (data != null && this.data.getItems() != null && !this.data.getItems().isEmpty()) {
            StringBuilder strBuf = new StringBuilder();
            int beginItemIndex = Math.max((this.page - 1) * pageSize, 0) + 1;
            int endItemIndex = (beginItemIndex - 1) + this.data.getItems().size();

            strBuf.append("Records ");

            strBuf.append(beginItemIndex);
            strBuf.append("-");
            strBuf.append(endItemIndex);
            if (this.itemCount > -1) {
                strBuf.append(" of ").append(this.itemCount);
            }
            return strBuf.toString();
        } else {
            return null;
        }
    }

    public String getRecordDetailsInfo(int index) {
        //log.debug("getRecordDetailsInfo..........." + index);
        if (data != null && this.data.getItems() != null && !this.data.getItems().isEmpty()) {
            StringBuilder strBuf = new StringBuilder();
            int beginItemIndex = Math.max((this.page - 1) * pageSize, 0) + 1;

            strBuf.append("Record ");

            strBuf.append(beginItemIndex + index);
            if (this.itemCount > -1) {
                strBuf.append(" of ").append(this.itemCount);
                //strBuf.append("/").append(this.itemCount);
            }
            return strBuf.toString();
        } else {
            return null;
        }
    }

    private void navigate(String url) throws SearchException, IOException {
        log.debug("Navigating to:" + url);
        SearchResultSet searchRS = catalogueClient.navigatePage(url);
        handleSearchResults(searchRS);
    }

    private void navigate() throws SearchException, IOException {
        log.debug("Navigate to page " + this.page);

        int startIndex = ((this.page - 1) * this.pageSize) + 1;
        log.debug("Navigate with start index: " + startIndex);
        this.inputParams.put(START_INDEX, Integer.toString(startIndex));

        SearchResultSet searchRS = catalogueClient.navigatePage(inputParams, serviceSearch);
        handleSearchResults(searchRS);
    }

    private void handleSearchResults(SearchResultSet searchRS) throws IOException {
        if (searchRS != null) {
            if (searchRS.getItems().isEmpty()) {
                log.debug("NO RESULTS");
                this.data = new SearchResultModel();
                this.first = null;
                this.previous = null;
                this.next = null;
                this.last = null;
                FacesMessageUtil.addInfoMessage("search.noresult");
            } else {
                log.debug("HAS RESULTS");
                this.data = new SearchResultModel(searchRS.getItems());
                this.first = searchRS.getFirstPageLink();
                this.previous = searchRS.getPreviousPageLink();
                this.next = searchRS.getNextPageLink();
                this.last = searchRS.getLastPageLink();

                this.itemCount = searchRS.getTotalResults();
                if (itemCount > 1) {
                    this.pageCount = (int) Math.ceil(itemCount * 1d / pageSize);
                } else {
                    this.pageCount = 0;
                }

                this.showPageInfo = itemCount > 0;
            }
        } else {
            log.debug("ERROR: SearchResultSet is null.");
            reset();
            throw new IOException("Search gave an empty response.");
        }
    }

    public void toBackupPage() {
        this.page = this.backupPage;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public boolean isShowPageInfo() {
        return showPageInfo;
    }

    public void setShowPageInfo(boolean showPageInfo) {
        this.showPageInfo = showPageInfo;
    }

    public Map<String, String> getInputParams() {
        return inputParams;
    }

    public void setInputParams(Map<String, String> inputParams) {
        this.inputParams = inputParams;
    }

    public int getBackupPage() {
        return backupPage;
    }

    public void setBackupPage(int backupPage) {
        this.backupPage = backupPage;
    }

    public SearchResultModel getData() {
        return data;
    }

    public void setData(SearchResultModel data) {
        this.data = data;
    }

    private void reset() {
        this.data = null;
        this.page = 0;
        this.pageCount = 0;
        this.itemCount = 0;
        this.first = null;
        this.previous = null;
        this.next = null;
        this.last = null;
        this.inputParams = null;
    }
}
