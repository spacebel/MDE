/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers.layout;

import be.spacebel.metadataeditor.business.DynamicPaginator;
import be.spacebel.metadataeditor.models.catalogue.SearchResultItem;
import be.spacebel.metadataeditor.utils.parser.XMLParser;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * This is a model class provides data and functionalities of catalogue page.
 *
 * @author mng
 */
public class CatalogueLayout extends Layout {

    private final Logger log = Logger.getLogger(getClass());

    private SearchResultItem selectedItem;

    private DynamicPaginator paginator;

    private String searchType;

    public CatalogueLayout() {
        super();
        paginator = null;
        searchType = "series";
    }

    public void onSelectItem(boolean selected) {
        log.debug("On select item: " + selected);
        if (selected) {
            increaseSelectedCount();
        } else {
            decreaseSelectedCount();
        }
    }

    public void onSelectAllItems() {
        if (isSelectedAll()) {
            log.debug("Select all items");
            if (paginator != null && paginator.getData() != null && paginator.getData().getItems() != null) {
                for (SearchResultItem item : paginator.getData().getItems()) {
                    item.setSelected(true);
                }
                setSelectedCount(paginator.getData().getItems().size());
            }
        } else {
            log.debug("Unselect all items");
            if (paginator != null && paginator.getData() != null && paginator.getData().getItems() != null) {
                for (SearchResultItem item : paginator.getData().getItems()) {
                    item.setSelected(false);
                }
                setSelectedCount(0);
            }

        }
    }

    public boolean highlight(SearchResultItem item) {
        return (selectedItem != null && selectedItem.getUuid().equals(item.getUuid()));
    }

    public void onViewItemDetails(SearchResultItem item) {
        log.debug("On view item details" + item.getMetadataFile().getFlatList().getId());

        setSelectedItem(item);
        getView().toDetails();
    }

    public void onViewSelectedItemDetails(SelectEvent event) {
        SearchResultItem item = (SearchResultItem) event.getObject();
        onViewItemDetails(item);
    }

    public void onEditItem(SearchResultItem item) {
        log.debug("On edit item");
        setSelectedItem(item);
        getView().toEdit();
    }

    public void toListView(ActionEvent actionEvent) {
        log.debug("To list view");
        getView().toList();
    }

    public void toDetailsView() {
        log.debug("To details view");
        if (selectedItem == null) {
            if (paginator != null
                    && paginator.getData() != null
                    && paginator.getData().getItems() != null
                    && !paginator.getData().getItems().isEmpty()) {

                // get the first item
                setSelectedItem(paginator.getData().getItems().get(0));
                getView().toDetails();
            } else {
                log.debug("Search result has no metadata record.");
            }
        } else {
            getView().toDetails();
        }
    }

    public void toThumbnailView(ActionEvent actionEvent) {
        log.debug("To thumbnail view");
        getView().toThumbnail();
    }

    public void toXmlView(ActionEvent actionEvent) {
        getView().toXml();
    }

    public void navigateDetails(String target) {
        log.debug("Navigate to the " + target + " details screen ");
        navigate(target);
    }

    public void navigateEdit(String target) {
        log.debug("Navigate to the " + target + " editor screen ");
        navigate(target);
    }

    public void jumpToPage(final Integer targetPage) {
        log.debug("Jump to page: " + targetPage);
        if (targetPage < 1) {
            paginator.toBackupPage();
            FacesMessageUtil.addErrorMessage("Page number should be greater or equal 1.");
        } else {
            if (paginator != null && paginator.getData() != null && paginator.getData().getItems() != null) {
                paginator.jumpToPage(targetPage);
            } else {
                log.debug("Don't jump.");
            }
        }

    }

    public String getXml() {
        if (selectedItem != null && selectedItem.getMetadataFile() != null) {
            XMLParser xmlParser = new XMLParser();
            return xmlParser.format(selectedItem.getMetadataFile().getXmlDoc());
        } else {
            log.debug("No selected metadata.");
            return "No selected metadata.";
        }

    }

    public StreamedContent download(SearchResultItem item) {
        log.debug("download ");
        XMLParser xmlParser = new XMLParser();
        String metadata = xmlParser.format(item.getMetadataFile().getXmlDoc());

        if (metadata != null) {
            try {
                InputStream stream = new ByteArrayInputStream(metadata.getBytes(Charsets.UTF_8.name()));
                String mimetype = "application/xml";
                String filename = item.getMetadataFile().getFlatList().getId() + ".xml";
                log.debug("file name: " + filename);

                return new DefaultStreamedContent(stream, mimetype, filename);
            } catch (UnsupportedEncodingException e) {
                log.debug("download.error: " + e.getMessage());
                FacesMessageUtil.addErrorMessage("Couldn't obtain metadata of this collection.");
                return null;
            }
        } else {
            FacesMessageUtil.addErrorMessage("Couldn't obtain metadata of this collection.");
            return null;
        }
    }

    public StreamedContent downloadSelections() {
        if (paginator != null && paginator.getData() != null && paginator.getData().getItems() != null) {
            try {
                byte[] zipBytes = zipSelectedItems();
                if (zipBytes != null) {
                    ByteArrayInputStream bis = new ByteArrayInputStream(zipBytes);
                    InputStream stream = bis;
                    return new DefaultStreamedContent(stream, "application/zip", "collections.zip", Charsets.UTF_8.name());
                } else {
                    FacesMessageUtil.addErrorMessage("No metadata is able to download.");
                }
            } catch (IOException e) {
                log.debug("downloadZip.error: " + e.getMessage());
                FacesMessageUtil.addErrorMessage(e);
            }
        } else {
            FacesMessageUtil.addErrorMessage("No metadata is able to download.");
        }
        return null;
    }

    private byte[] zipSelectedItems() throws IOException {
        log.debug("Zip selected items");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count;
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            XMLParser xmlParser = new XMLParser();
            count = 0;
            for (SearchResultItem item : paginator.getData().getItems()) {
                if (item.isSelected()) {
                    log.debug("selected item: " + item.getMetadataFile().getFlatList().getId());
                    String metadata = xmlParser.format(item.getMetadataFile().getXmlDoc());
                    //log.debug("metadata: " + metadata);
                    if (StringUtils.isNotEmpty(metadata)) {
                        ZipEntry entry = new ZipEntry(item.getMetadataFile().getFlatList().getId() + ".xml");
                        zos.putNextEntry(entry);
                        zos.write(metadata.getBytes(Charsets.UTF_8.name()));
                        zos.closeEntry();
                        count++;
                    }
                }
                if (count > getSelectedCount()) {
                    break;
                }
            }
        }
        log.debug("count = " + count);

        if (count > 0) {
            return baos.toByteArray();
        } else {
            return null;
        }

    }

    private void navigate(String target) {
        int index = selectedItem.getIndex();

        log.debug("current index: " + index);
        int itemCount = paginator.getData().getItemCount();
        log.debug("Number of records of the current page: " + itemCount);

        if ("next".equals(target)) {
            log.debug("go next");
            if ((index + 1) >= itemCount) {
                log.debug("approach the last record of the current page");
                if (StringUtils.isNotEmpty(paginator.getNext())) {
                    log.debug("Has the next page, continue.");
                    paginator.pageNavigate("next");
                    index = 0;
                } else {
                    log.debug("Has no next page, stop.");
                }
            } else {
                log.debug("continue normal");
                index += 1;
            }
        }
        if ("prev".equals(target)) {
            log.debug("back previous");
            if (index <= 0) {
                log.debug("approach the first record of the current page");
                if (StringUtils.isNotEmpty(paginator.getPrevious())) {
                    log.debug("Has the previous page, continue.");
                    paginator.pageNavigate("previous");
                    index = paginator.getData().getItemCount() - 1;
                } else {
                    log.debug("Has no previous page, stop.");
                }
            } else {
                index -= 1;
                log.debug("back normal");
            }
        }
        log.debug("navigated index: " + index);

        if (paginator.getData() != null && paginator.getData().getItems() != null) {
            try {
                setSelectedItem(paginator.getData().getItems().get(index));
            } catch (IndexOutOfBoundsException e) {
                log.debug("Navigate item details error: " + e.getMessage());
            }
        }
    }    
    
    ///////////////////////////////////////////////////////////////////////////
    public SearchResultItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(SearchResultItem selectedItem) {
        this.selectedItem = selectedItem;
//        if (selectedItem != null && selectedItem.getSeriesFile() != null
//                && selectedItem.getSeriesFile().getXmlDoc() != null) {
//            selectedItem.getSeriesFile().updateSources();
//        }
    }

    public DynamicPaginator getPaginator() {
        return paginator;
    }

    public void setPaginator(DynamicPaginator paginator) {
        this.paginator = paginator;
    }

    public String getConfirmMetadataId() {
        if (this.selectedItem != null) {
            return selectedItem.getMetadataFile().getFlatList().getId();
        }
        return "";
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public boolean isServiceSearch() {
        return "service".equalsIgnoreCase(searchType);
    }
}
