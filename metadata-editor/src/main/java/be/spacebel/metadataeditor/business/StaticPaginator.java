/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.business;

import be.spacebel.metadataeditor.models.workspace.DataTableModel;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.utils.CommonUtils;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.data.SortEvent;

/**
 * Static data paginator
 *
 * @author mng
 */
public class StaticPaginator implements Serializable {

    private int currentPage;
    private int totalPages;
    private final int rowsPerPage;
    private int totalRecords;
    private DataTableModel data;
    private List<MetadataFile> items;
    

    private int backupPage;

    private final Logger log = Logger.getLogger(getClass());

    public StaticPaginator(List<MetadataFile> newItems, int pageSize) {
        log.debug("Init StaticPaginator");
        this.items = newItems;

        log.debug("newItems = " + items.size());
        log.debug("pageSize = " + pageSize);

        this.rowsPerPage = pageSize;
        if (items != null && items.size() > 0) {
            this.totalRecords = items.size();
            this.totalPages = (int) Math.ceil(totalRecords * 1d / rowsPerPage);
            this.currentPage = 1;
            this.backupPage = this.currentPage;
            loadData();           
        }
    }

    public void onSort(SortEvent event) {

        String columnHeader = StringUtils.trimToEmpty(event.getSortColumn().getHeaderText());
        log.debug("Sort " + (event.isAscending() ? "Acending" : "Descending") + " column " + columnHeader);

        switch (columnHeader) {
            case "Identifier":
                if (event.isAscending()) {
                    items.sort(Comparator.comparing(CommonUtils::getMetadataId, Comparator.nullsFirst(Comparator.naturalOrder())));
//                    Collections.sort(items,
//                            (o1, o2) -> o1.getFlatList().getId().compareTo(o2.getFlatList().getId()));
                } else {
//                    Collections.sort(items,
//                            (o1, o2) -> o2.getFlatList().getId().compareTo(o1.getFlatList().getId()));
                    items.sort(Comparator.comparing(CommonUtils::getMetadataId, Comparator.nullsFirst(Comparator.reverseOrder())));
                }
                loadData();
                break;
            case "Title":
                if (event.isAscending()) {
//                    Collections.sort(items,
//                            (o1, o2) -> o1.getFlatList().getTitle().compareTo(o2.getFlatList().getTitle()));
                    items.sort(Comparator.comparing(CommonUtils::getMetadataTitle, Comparator.nullsFirst(Comparator.naturalOrder())));
                } else {
//                    Collections.sort(items,
//                            (o1, o2) -> o2.getFlatList().getTitle().compareTo(o1.getFlatList().getTitle()));
                    items.sort(Comparator.comparing(CommonUtils::getMetadataTitle, Comparator.nullsFirst(Comparator.reverseOrder())));
                }
                loadData();
                break;

            case "Start":
                if (event.isAscending()) {
//                    Collections.sort(items,
//                            (o1, o2) -> o1.getFlatList().getStartDate().compareTo(o2.getFlatList().getStartDate()));
                    items.sort(Comparator.comparing(CommonUtils::getMetadataStartDate, Comparator.nullsFirst(Comparator.naturalOrder())));
                } else {
//                    Collections.sort(items,
//                            (o1, o2) -> o2.getFlatList().getStartDate().compareTo(o1.getFlatList().getStartDate()));
                    items.sort(Comparator.comparing(CommonUtils::getMetadataStartDate, Comparator.nullsFirst(Comparator.reverseOrder())));
                }
                loadData();
                break;

            case "End":
                if (event.isAscending()) {
                    items.sort(Comparator.comparing(CommonUtils::getMetadataEndDate, Comparator.nullsFirst(Comparator.naturalOrder())));
//                    Collections.sort(items,
//                            (o1, o2) -> o1.getFlatList().getEndDate().compareTo(o2.getFlatList().getEndDate()));
                } else {
                    items.sort(Comparator.comparing(CommonUtils::getMetadataEndDate, Comparator.nullsFirst(Comparator.reverseOrder())));
//                    Collections.sort(items,
//                            (o1, o2) -> o2.getFlatList().getEndDate().compareTo(o1.getFlatList().getEndDate()));
                }
                loadData();
                break;

            case "Modified":
                if (event.isAscending()) {
                    items.sort(Comparator.comparing(CommonUtils::getMetadataModifiedDate, Comparator.nullsFirst(Comparator.naturalOrder())));
//                    Collections.sort(items,
//                            (o1, o2) -> o1.getFlatList().getModifiedDate().compareTo(o2.getFlatList().getModifiedDate()));
                } else {
                    items.sort(Comparator.comparing(CommonUtils::getMetadataModifiedDate, Comparator.nullsFirst(Comparator.reverseOrder())));
//                    Collections.sort(items,
//                            (o1, o2) -> o2.getFlatList().getModifiedDate().compareTo(o1.getFlatList().getModifiedDate()));
                }
                loadData();
                break;

            case "Organisation name":
                if (event.isAscending()) {
                    items.sort(Comparator.comparing(CommonUtils::getMetadataOrgName, Comparator.nullsFirst(Comparator.naturalOrder())));
//                    Collections.sort(items,
//                            (o1, o2) -> o1.getFlatList().getOrganisationName().compareTo(o2.getFlatList().getOrganisationName()));
                } else {
                    items.sort(Comparator.comparing(CommonUtils::getMetadataOrgName, Comparator.nullsFirst(Comparator.reverseOrder())));
//                    Collections.sort(items,
//                            (o1, o2) -> o2.getFlatList().getOrganisationName().compareTo(o1.getFlatList().getOrganisationName()));
                }
                loadData();
                break;
        }

    }

    public void reload(List<MetadataFile> newItems) {
        this.totalRecords = items.size();
        this.totalPages = (int) Math.ceil(totalRecords * 1d / rowsPerPage);
        loadData();
    }

    public void pageNavigate(String where) {
        log.debug("pageNavigate : " + where);

        if ("first".equals(where)) {
            this.currentPage = 1;
        }

        if ("previous".equals(where)) {
            this.currentPage = this.currentPage - 1;
            if (this.currentPage < 1) {
                this.currentPage = 1;
            }
        }

        if ("next".equals(where)) {
            this.currentPage = this.currentPage + 1;
            if (this.currentPage > totalPages) {
                this.currentPage = totalPages;
            }
        }

        if ("last".equals(where)) {
            this.currentPage = this.totalPages;
        }
        this.backupPage = this.currentPage;

        loadData();

    }

    public void jumpToPage(int targetPage) {
        this.currentPage = targetPage;
        log.debug("Jump to page: " + this.currentPage);
        if (targetPage > totalPages) {
            this.currentPage = totalPages;
        }
        this.backupPage = this.currentPage;

        loadData();

    }

    public void toBackupPage() {
        this.currentPage = this.backupPage;
        loadData();
    }

    private void loadData() {
        List<MetadataFile> currentPageItems;
        if (items.size() > rowsPerPage) {
            int fromIndex = 0;
            if (currentPage > 1) {
                fromIndex = (currentPage - 1) * rowsPerPage;
            }

            int toIndex = fromIndex + rowsPerPage;
            if (toIndex > totalRecords) {
                toIndex = totalRecords;
            }
            log.debug("FromIndex = " + fromIndex + "; toIndex = " + toIndex);
            currentPageItems = items.subList(fromIndex, toIndex);
            //data = new DataTableModel(items.subList(fromIndex, toIndex));
        } else {
            currentPageItems = items;
            //data = new DataTableModel(items);
        }

        if (currentPageItems != null) {
            for (int i = 0; i < currentPageItems.size(); i++) {
                currentPageItems.get(i).setIndex(i);
            }
            data = new DataTableModel(currentPageItems);
        }

    }

    public String getPageInfo() {
        //log.debug("getPageInfo...........");

        // Page 1 of 3 or Page 1
        if (data != null && this.data.getItems() != null && !this.data.getItems().isEmpty()) {
            StringBuilder strBuf = new StringBuilder();
            strBuf.append("Page ").append(currentPage);
            strBuf.append(" of ").append(totalPages);

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
            int beginItemIndex = Math.max((currentPage - 1) * rowsPerPage, 0) + 1;
            int endItemIndex = (beginItemIndex - 1) + this.data.getItems().size();

            strBuf.append("Records ");

            strBuf.append(beginItemIndex);
            strBuf.append("-");
            strBuf.append(endItemIndex);

            strBuf.append(" of ").append(totalRecords);

            return strBuf.toString();
        } else {
            return null;
        }
    }

    public String getRecordDetailsInfo(int index) {
        //log.debug("getRecordDetailsInfo..........." + index);
        if (data != null && this.data.getItems() != null && !this.data.getItems().isEmpty()) {
            StringBuilder strBuf = new StringBuilder();
            int beginItemIndex = Math.max((currentPage - 1) * rowsPerPage, 0) + 1;

            strBuf.append("Record ");

            strBuf.append(beginItemIndex + index);
            //strBuf.append(index + 1);
            strBuf.append(" of ").append(totalRecords);

            return strBuf.toString();
        } else {
            return null;
        }
    }

    public List<MetadataFile> getItems() {
        return items;
    }

    public void setItems(List<MetadataFile> items) {
        this.items = items;
    }

    public DataTableModel getData() {
        return data;
    }

    public void setData(DataTableModel data) {
        this.data = data;
    }

    public boolean isShowPaginator() {
        return (items != null && items.size() > 0);        
    }

    public boolean isShowFirst() {
        return (isShowPaginator() && currentPage > 1);
    }

    public boolean isShowPrev() {
        return (isShowPaginator() && currentPage > 1);
    }

    public boolean isShowNext() {
        return (isShowPaginator() && currentPage < totalPages);
    }

    public boolean isShowLast() {
        return (isShowPaginator() && currentPage < totalPages);
    }

    public int getBackupPage() {
        return backupPage;
    }

    public void setBackupPage(int backupPage) {
        this.backupPage = backupPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

}
