/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers.layout;

import be.spacebel.metadataeditor.business.Delegator;
import be.spacebel.metadataeditor.business.SearchException;
import be.spacebel.metadataeditor.business.StaticPaginator;
import be.spacebel.metadataeditor.models.configuration.VoidDataset;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.validation.AutoCorrectionWarning;
import be.spacebel.metadataeditor.utils.validation.AutoCorrectionWarningReport;
import be.spacebel.metadataeditor.utils.validation.ManualCorrectionWarning;
import be.spacebel.metadataeditor.utils.validation.ManualCorrectionWarningReport;
import be.spacebel.metadataeditor.utils.validation.ThesaurusChangeWarning;
import be.spacebel.metadataeditor.utils.validation.ValidationReport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.faces.event.ActionEvent;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
//import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * A model class provides data and functionalities of workspace page.
 *
 * @author mng
 */
public class WorkspaceLayout extends Layout {

    private final Logger log = Logger.getLogger(getClass());

    private List<MetadataFile> metadataFiles;
    private MetadataFile selectedMetadataFile;
    private List<MetadataFile> overwriteMetadataFiles;
    private StaticPaginator paginator;
    private ValidationReport validationReport;
    //private List<MetadataFile> warnMetadataFiles;
    private AutoCorrectionWarningReport thesaurusAutoCorrectionWarnReport;
    private List<MetadataFile> autoCorrectionWarnFiles;

    private ManualCorrectionWarningReport thesaurusManualCorrectionWarnReport;
    private List<MetadataFile> manualCorrectionWarnFiles;

    private List<ThesaurusChangeWarning> thesaurusChangeWarns;
    private List<MetadataFile> thesaurusChangeWarnFiles;

    private int numOfSeriesRecords;
    private int numOfServiceRecords;

    private int numOfWarnRecords;

    private final Delegator delegator;

    public WorkspaceLayout(Delegator newDelegator, String newUuidWorkingDir) {
        super();
        paginator = null;
        delegator = newDelegator;
        numOfSeriesRecords = 0;
        numOfServiceRecords = 0;
    }

    public void toListView(ActionEvent actionEvent) {
        log.debug("Back to list files screen");
        getView().toList();
    }

    public void toThumbnailView(ActionEvent actionEvent) {
        log.debug("Back to thumbnail view");
        getView().toThumbnail();
    }

    public void toDetailsView(ActionEvent actionEvent) {
        log.debug("To details view");
        if (selectedMetadataFile == null) {
            if (metadataFiles != null && !metadataFiles.isEmpty()) {
                // get the first item
                setSelectedMetadataFile(metadataFiles.get(0));
                getView().toDetails();
            } else {
                log.debug("There is no metadata records in the workspace");
            }
        } else {
            getView().toDetails();
        }
    }

    public void onViewDetails(MetadataFile metadataFile) {
        log.debug("View metadata record details");
        setSelectedMetadataFile(metadataFile);
        getView().toDetails();
    }

    public void onEdit(MetadataFile metadataFile) {
        log.debug("on edit metadata record " + metadataFile.getFlatList().getId());
        setSelectedMetadataFile(metadataFile);
        getView().toEdit();
    }

    public void onEdit(String recordId, int tabIndex) {
        if (metadataFiles != null && metadataFiles.size() > 0) {
            for (MetadataFile mFile : metadataFiles) {
                if (recordId.equals(mFile.getFlatList().getId())) {
                    mFile.setActiveTabIndex(tabIndex);
                    onEdit(mFile);
                    break;
                }
            }
        }
    }

    public void onViewValidationReport(MetadataFile metadataFile) {
        log.debug("on view validation report of metadata record " + metadataFile.getFlatList().getId());
        List<MetadataFile> sFiles = new ArrayList<>();
        sFiles.add(metadataFile);
        buildValidationReport(sFiles);
        getView().toReport();
    }

    /**
     * View validation report of either selected metadata records or all
     * metadata records (if there is no record is selected)
     */
    public void onViewValidationReport() {
        if (metadataFiles != null && !metadataFiles.isEmpty()) {
            int selectedCount = getSelectedCount();
            if (selectedCount > 0) {
                List<MetadataFile> sFiles = new ArrayList<>();
                for (MetadataFile item : metadataFiles) {
                    if (item.isSelected()) {
                        sFiles.add(item);
                    }
                    if (sFiles.size() >= selectedCount) {
                        break;
                    }
                }
                unSelectMetadataFiles();
                buildValidationReport(sFiles);
            } else {
                buildValidationReport(metadataFiles);
            }

            if (validationReport != null
                    && (validationReport.getErrors() > 0 || validationReport.getWarnings() > 0)) {
                getView().toReport();
            } else {
                FacesMessageUtil.addInfoMessage("All metadata records are valid");
            }
        } else {
            FacesMessageUtil.addErrorMessage("There is no metadata record in the workspace");
        }
    }

    public boolean highlight(MetadataFile item) {
        return (selectedMetadataFile != null && selectedMetadataFile.getUuid().equals(item.getUuid()));
    }

    public void onEditSelectedItem(SelectEvent event) {
        MetadataFile item = (MetadataFile) event.getObject();
        onEdit(item);
    }

    public void onXmlView() {
        getView().toXml();
    }

    public void onGeoJsonView() {
        getView().toGeoJson();
    }

    public void onInternalModelView() {
        getView().toInternalModel();
    }

    public void onDif10View() {
        getView().toDif10();
    }

    public void onViewThesaurusWarns() {
        getView().toThesaurusWarn();
    }

    public void onSelect(SelectEvent event) {
        onViewDetails((MetadataFile) event.getObject());
    }

    public void onSelect(boolean selected) {
        log.debug("On select item: " + selected);
        if (selected) {
            increaseSelectedCount();
        } else {
            decreaseSelectedCount();
        }
    }

    public void resetExistingSelections() {
        setOverwriteMetadataFiles(null);
    }

    public void resetThesaurusWarn() {
        // this.warnMetadataFiles = null;
        // this.thesaurusAutoCorrectionWarnReport = null;

        this.thesaurusAutoCorrectionWarnReport = null;
        this.autoCorrectionWarnFiles = null;

        this.thesaurusManualCorrectionWarnReport = null;
        this.manualCorrectionWarnFiles = null;

        this.thesaurusChangeWarns = null;
        this.thesaurusChangeWarnFiles = null;
        this.numOfWarnRecords = 0;
    }

    public void onSelectAll() {
        if (isSelectedAll()) {
            log.debug("Select all metadata");
            if (metadataFiles != null) {
                metadataFiles.forEach((item) -> {
                    item.setSelected(true);
                });
                setSelectedCount(metadataFiles.size());
            }
        } else {
            log.debug("Unselect all metadata");
            if (metadataFiles != null) {
                metadataFiles.forEach((item) -> {
                    item.setSelected(false);
                });
                setSelectedCount(0);
            }
        }
    }

//    public void catalogueLogin(ActionEvent actionEvent) {
//        delegator.catalogueLogin(catalogueUser, cataloguePass);
//    }
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

    public StreamedContent download(String format) {
        return download(selectedMetadataFile, format);
    }

    public StreamedContent download(MetadataFile metadataFile, String format) {
        String metadata = null;
        String mimetype = null;
        String filename = CommonUtils.getFileName(metadataFile.getFlatList().getId());
        switch (format) {
            case "xml":
                metadata = metadataFile.getXmlSrc();
                mimetype = "application/xml";
                filename = filename + ".xml";
                break;
            case "geojson":
                metadata = metadataFile.getGeoJsonSrc();
                mimetype = "application/json";
                filename = filename + ".json";
                break;
            case "internal":
                metadata = metadataFile.getInternalModelSrc();
                mimetype = "application/json";
                filename = filename + ".json";
                break;
            case "dif10":
                metadata = metadataFile.getDif10();
                mimetype = "application/dif10+xml";
                break;
        }

        log.debug("file name: " + filename);

        if (metadata != null) {
            try {
                InputStream stream = new ByteArrayInputStream(metadata.getBytes(Charsets.UTF_8.name()));
                return new DefaultStreamedContent(stream, mimetype, filename);
            } catch (UnsupportedEncodingException e) {
                log.debug("download.error: " + e.getMessage());
                FacesMessageUtil.addErrorMessage("Error while handling the download: " + e.getMessage());
                return null;
            }
        } else {
            FacesMessageUtil.addErrorMessage("The format " + format + " is not supported");
            return null;
        }

    }

    /**
     *
     * Download either selected metadata records or all metadata records (if
     * there is no record is selected)
     *
     * @param format
     * @return
     */
    public StreamedContent downloadMultiple(String format) {
        log.debug("Download metadata records");
        if (metadataFiles != null && !metadataFiles.isEmpty()) {
            try {
                byte[] zipBytes = null;
                if (getSelectedCount() > 0) {
                    List<MetadataFile> sFiles = new ArrayList<>();
                    for (MetadataFile item : metadataFiles) {
                        if (item.isSelected()) {
                            sFiles.add(item);
                        }
                        if (sFiles.size() >= getSelectedCount()) {
                            break;
                        }
                    }
                    zipBytes = zipToDownload(sFiles, format);
                    //setSelectedAll(false);
                } else {
                    zipBytes = zipToDownload(metadataFiles, format);
                }
                if (zipBytes != null) {
                    InputStream stream = new ByteArrayInputStream(zipBytes);
                    return new DefaultStreamedContent(stream, "application/zip", "collections.zip", Charsets.UTF_8.name());
                } else {
                    FacesMessageUtil.addErrorMessage("No metadata record is able to download.");
                }
            } catch (IOException e) {
                log.debug("downloadSelections --> error: " + e.getMessage());
                FacesMessageUtil.addErrorMessage(e);
            }
        } else {
            FacesMessageUtil.addErrorMessage("There is no metadata record in the workspace");
        }

        return null;
    }

    public byte[] zipToCopy(List<MetadataFile> sFiles,
            List<MetadataFile> nonExistingSelectedItems, boolean serviceMetadata)
            throws IOException {
        log.debug("Zip metadata records are being to insert to the catalogue");

        List<MetadataFile> zippingItems = new ArrayList<>();
        //overwriteMetadataFiles = new ArrayList<>();

        /**
         * Firstly check if the metadata already exists in the catalogue
         */
        sFiles.forEach((item) -> {
            boolean existing = false;
            try {
                existing = delegator.checkExist(item.getFlatList().getId(), serviceMetadata);
                if (existing) {
                    log.debug(String.format("Record %s already exists ", item.getFlatList().getId()));
                    overwriteMetadataFiles.add(item);
                }
            } catch (IOException ioe) {
                log.debug(String
                        .format("Error while checking existing of metadata record %s : %s", item.getFlatList().getId(), ioe));
            }
            if (!existing) {
                if (nonExistingSelectedItems != null) {
                    nonExistingSelectedItems.add(item);
                }
                zippingItems.add(item);
            }
        });

        if (!zippingItems.isEmpty()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (MetadataFile item : zippingItems) {
                    String filename = CommonUtils.getFileName(item.getFlatList().getId()) + ".json";
                    ZipEntry entry = new ZipEntry(filename);
                    zos.putNextEntry(entry);
                    zos.write(item.getInternalModelSrc().getBytes(Charsets.UTF_8.name()));
                    zos.closeEntry();
                }
            }

            return baos.toByteArray();
        }

        return null;
    }

    public byte[] zipToReplace(List<MetadataFile> sFiles) throws IOException {
        log.debug("Add given metadata records into a zip file to be inserted to the catalogue");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (MetadataFile item : sFiles) {
                String filename = CommonUtils.getFileName(item.getFlatList().getId()) + ".json";
                ZipEntry entry = new ZipEntry(filename);
                zos.putNextEntry(entry);
                zos.write(item.getInternalModelSrc().getBytes(Charsets.UTF_8.name()));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private byte[] zipToDownload(List<MetadataFile> sFiles, String format) throws IOException {
        log.debug("Add given metadata records into a zip file to be downloaded");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count = 0;
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (MetadataFile item : sFiles) {
                String filename = CommonUtils.getFileName(item.getFlatList().getId());
                String metadata = null;
                switch (format) {
                    case "xml":
                        metadata = item.getXmlSrc();
                        filename = filename + ".xml";
                        break;
                    case "geojson":
                        metadata = item.getGeoJsonSrc();
                        filename = filename + ".json";
                        break;
                    case "internal":
                        metadata = item.getInternalModelSrc();
                        filename = filename + ".json";
                        break;
                    case "dif10":
                        if (item.getMetadata().isSeries()) {
                            metadata = item.getDif10();
                        }
                        break;
                }

                if (metadata != null && !metadata.isEmpty()) {
                    ZipEntry entry = new ZipEntry(filename);
                    zos.putNextEntry(entry);
                    zos.write(metadata.getBytes(Charsets.UTF_8.name()));
                    zos.closeEntry();
                    count++;
                }
            }
        }
        if (count == 0) {
            throw new IOException("Format " + format.toUpperCase() + " is not supported by the selected metadata record(s)");
        }
        return baos.toByteArray();
    }

    public void extractOfferings() {
        if (selectedMetadataFile != null) {
            if (selectedMetadataFile.getMetadata() != null) {
                if (StringUtils.isNotEmpty(selectedMetadataFile.getMetadata().getCapabilitiesServiceUrl())) {
                    try {
                        URL url = new URL(selectedMetadataFile.getMetadata().getCapabilitiesServiceUrl());
                        selectedMetadataFile.getMetadata().addOffering(XmlUtils
                                .loadOfferingOperations(selectedMetadataFile
                                        .getMetadata().getCapabilitiesServiceUrl(), delegator.getConfig()));

                    } catch (MalformedURLException e) {
                        log.debug("Invalid URL: " + e);
                        FacesMessageUtil.addErrorMessage(String.format("The Capabilities service enpoint is invalid",
                                selectedMetadataFile.getMetadata().getCapabilitiesServiceUrl()));
                    } catch (SearchException e) {
                        String errorMsg = CommonUtils.getErrorMessage(e);
                        log.debug("Extract offering from Capabilities ==> SearchException: " + errorMsg);
                        FacesMessageUtil.addErrorMessage(e.getTitle());
                    } catch (IOException e) {
                        log.debug("Extract offering from Capabilities ==> IOException: " + e);
                        FacesMessageUtil.addErrorMessage(e);
                    }
                } else {
                    FacesMessageUtil.addErrorMessage("Please provide a Capabilities service endpoint");
                }
            } else {
                FacesMessageUtil.addErrorMessage("No selected metadata record");
            }
        } else {
            FacesMessageUtil.addErrorMessage("No selected metadata record");
        }
    }

//    public void validateMetadata() {
//        try {
//            if (getView().isDif10()) {
//                delegator.validateDif10(selectedMetadataFile.getDif10());
//                FacesMessageUtil.addInfoMessage("DIF-10 format of the metadata record is valid");
//            } else {
//                if (getView().isJson()) {
//                    delegator.validateJson(selectedMetadataFile.getJsonSrc());
//                    FacesMessageUtil.addInfoMessage("Geo JSON format of the metadata record is valid");
//                } else {
//                    delegator.validateIso(selectedMetadataFile.getXmlSrc());
//                    FacesMessageUtil.addInfoMessage("ISO 19139-2 (XML) format of the metadata record is valid");
//                }
//            }
//        } catch (ValidationException e) {
//            CommonUtils.handleValidationException(e);
//        } catch (IOException | SAXException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | JSONException ex) {
//            FacesMessageUtil.addErrorMessage(CommonUtils.getErrorMessage(ex));
//        }
//    }
    public void navigateDetails(String target) {
        log.debug("Navigate to the " + target + " details screen ");
        navigate(target);
    }

    public void navigateEdit(String target) {
        log.debug("Navigate to the " + target + " editor screen ");
        navigate(target);
    }

    public void onFormChange(final String tab) {
        //log.debug("Modifying fields on tab: " + tab);
        if (selectedMetadataFile != null) {
            switch (tab) {
                case "org":
                    selectedMetadataFile.setUnsavedOrgTab(true);
                    break;
                case "id":
                    selectedMetadataFile.setUnsavedIdTab(true);
                    break;
                case "geo":
                    selectedMetadataFile.setUnsavedGeoTab(true);
                    break;
                case "temp":
                    selectedMetadataFile.setUnsavedTempTab(true);
                    break;
                case "const":
                    selectedMetadataFile.setUnsavedConstTab(true);
                    break;
                case "kw":
                    selectedMetadataFile.setUnsavedKwTab(true);
                    break;
                case "offering":
                    selectedMetadataFile.setUnsavedOfferingTab(true);
                    break;
                case "dist":
                    selectedMetadataFile.setUnsavedDistTab(true);
                    break;
                case "acqui":
                    selectedMetadataFile.setUnsavedAcquiTab(true);
                    break;
                case "other":
                    selectedMetadataFile.setUnsavedOthersTab(true);
                    break;
            }
        }

    }

    public void checkGeoValue(final String newValue, final String direction) {
        log.debug("Check Geo value");
        log.debug("newValue = " + newValue + ", direction = " + direction);
        if (selectedMetadataFile != null) {
            if (selectedMetadataFile.getMetadata().getIdentification() != null && selectedMetadataFile.getMetadata().getIdentification().getBbox() != null) {
                double dNewValue = Double.parseDouble(newValue);

                switch (direction) {
                    case "west":
                        double west = selectedMetadataFile.getMetadata().getIdentification().getBbox().getWest();
                        log.debug("bbox.west = " + west);
                        if (dNewValue != west) {
                            selectedMetadataFile.setUnsavedGeoTab(true);
                        }
                        break;

                    case "east":
                        double east = selectedMetadataFile.getMetadata().getIdentification().getBbox().getEast();
                        log.debug("bbox.east = " + east);

                        if (dNewValue != east) {
                            selectedMetadataFile.setUnsavedGeoTab(true);
                        }
                        break;

                    case "south":
                        double south = selectedMetadataFile.getMetadata().getIdentification().getBbox().getSouth();
                        log.debug("bbox.south = " + south);

                        if (dNewValue != south) {
                            selectedMetadataFile.setUnsavedGeoTab(true);
                        }
                        break;

                    case "north":
                        double north = selectedMetadataFile.getMetadata().getIdentification().getBbox().getNorth();
                        log.debug("bbox.north = " + north);

                        if (dNewValue != north) {
                            selectedMetadataFile.setUnsavedGeoTab(true);
                        }
                        break;
                }
            }
        }
    }

    public void onChangeRichText(final String uuid, final String value) {
        if (selectedMetadataFile != null) {
            if (selectedMetadataFile.getMetadata().getIdentification() != null) {
                selectedMetadataFile.getMetadata().getIdentification().onChangeRichText(uuid, value);
            }
        }
    }

    private void navigate(String target) {
        int index = selectedMetadataFile.getIndex();

        log.debug("current index: " + index);
        int itemCount = paginator.getData().getItemCount();
        log.debug("Number of records of the current page: " + itemCount);

        if ("next".equals(target)) {
            log.debug("go next");
            if ((index + 1) >= itemCount) {
                log.debug("approach the last record of the current page");
                if (paginator.isShowNext()) {
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
                if (paginator.isShowPrev()) {
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
                setSelectedMetadataFile(paginator.getData().getItems().get(index));
            } catch (IndexOutOfBoundsException e) {
                log.debug("Navigate item details error: " + e.getMessage());
            }
        }
    }

//    public void updateSelectedMetadata() {
//        if (selectedMetadataFile != null
//                && selectedMetadataFile.getXmlDoc() != null) {
//            selectedMetadataFile.updateSources();
//        }
//    }
    public void reArrangedItems(int index) {
        if (paginator.getItems() != null
                && !paginator.getItems().isEmpty()) {
            int size = paginator.getItems().size();
            if (size > 0) {
                if (index >= size) {
                    index = size - 1;
                }
                setSelectedMetadataFile(paginator.getItems().get(index));
            }
        } else {
            setSelectedMetadataFile(null);
            getView().toList();
        }
    }

    private void buildValidationReport(List<MetadataFile> metadataFiles) {
        validationReport = new ValidationReport();
        if (metadataFiles.size() > 1) {
            validationReport.setAll(true);
        }
        metadataFiles.stream().filter((sFile) -> (sFile.getValidationStatus() != null)).forEachOrdered((sFile) -> {
            validationReport.addEntry(sFile.getValidationStatus());
        });

    }

    //******************************************/  
    public List<MetadataFile> getMetadataFiles() {
        return metadataFiles;
    }

    public void setMetadataFiles(List<MetadataFile> metadataFiles) {
        this.metadataFiles = metadataFiles;
    }

    public void addMetadataFile(MetadataFile item) {
        if (this.metadataFiles == null) {
            this.metadataFiles = new ArrayList<>();
        }
        item.setIndex(this.metadataFiles.size());
        this.metadataFiles.add(item);
        if (item.getMetadata() != null) {
            if (item.getMetadata().isSeries()) {
                numOfSeriesRecords++;
            }
            if (item.getMetadata().isService()) {
                numOfServiceRecords++;
            }
            addToWarnReports(item);
        }

    }

    public void removeMetadataFile(MetadataFile item) {
        if (this.metadataFiles != null) {
            if (this.metadataFiles.remove(item)) {
                log.debug("Removed metadata too. ");
                if (item.getMetadata().isSeries()) {
                    if (numOfSeriesRecords > 0) {
                        numOfSeriesRecords--;
                    }
                }
                if (item.getMetadata().isService()) {
                    if (numOfServiceRecords > 0) {
                        numOfServiceRecords--;
                    }
                }
            } else {
                log.debug("Couldn't remove metadata. ");
            }
        }
    }

    public MetadataFile getSelectedMetadataFile() {
        return selectedMetadataFile;
    }

    public void setSelectedMetadataFile(MetadataFile selectedMetadataFile) {
        this.selectedMetadataFile = selectedMetadataFile;
        //updateSelectedMetadata();
    }

    public StaticPaginator getPaginator() {
        return paginator;
    }

    public void setPaginator(StaticPaginator paginator) {
        this.paginator = paginator;
    }

    public int getUploadFileLimit() {
        return delegator.getConfig().getUploadFileLimit();
    }

    public int getUploadSizeLimit() {
        return delegator.getConfig().getUploadSizeLimit();
    }

    public List<MetadataFile> getOverwriteMetadataFiles() {
        return overwriteMetadataFiles;
    }

    public void setOverwriteMetadataFiles(List<MetadataFile> overwriteMetadataFiles) {
        this.overwriteMetadataFiles = overwriteMetadataFiles;
    }

    public String getNumOfOverwriteFiles() {
        if (this.overwriteMetadataFiles != null && this.overwriteMetadataFiles.size() > 0) {
            return ("(" + this.overwriteMetadataFiles.size() + ")");
        }
        return "";
    }

    public ValidationReport getValidationReport() {
        return validationReport;
    }

    public void setValidationReport(ValidationReport validationReport) {
        this.validationReport = validationReport;
    }

    public int getNumberOfHandlingMetadataFiles() {
        if (getSelectedCount() > 0) {
            return getSelectedCount();
        } else {
            if (metadataFiles != null) {
                return metadataFiles.size();
            }
        }
        return 0;
    }

    public void unSelectMetadataFiles() {
        metadataFiles.forEach((item) -> {
            item.setSelected(false);
        });
        setSelectedCount(0);
    }

    public AutoCorrectionWarningReport getThesaurusAutoCorrectionWarnReport() {
        return thesaurusAutoCorrectionWarnReport;
    }

    public void setThesaurusAutoCorrectionWarnReport(AutoCorrectionWarningReport thesaurusAutoCorrectionWarnReport) {
        this.thesaurusAutoCorrectionWarnReport = thesaurusAutoCorrectionWarnReport;
    }

    public void addToWarnReports(MetadataFile item) {
        log.debug("Add to warning reports ");
        String id = item.getMetadata().getOthers().getFileIdentifier();
        int recordType = 0;
        if (item.getMetadata().isSeries()) {
            recordType = 1;
        } else {
            recordType = 2;
        }
        boolean isVersionChanged = false;
        boolean isAutoWarn = false;
        boolean isManualWarn = false;

        if (item.getMetadata().isEarthtopicChanged()
                || item.getMetadata().isScienceKwChanged()
                || item.getMetadata().isEsaInstrumentChanged()) {

            if (item.getMetadata().isEarthtopicChanged()) {
                addThesaurusChangeWarn(1);
                isVersionChanged = true;
            }

            if (item.getMetadata().isScienceKwChanged()) {
                addThesaurusChangeWarn(2);
                isVersionChanged = true;
            }

            if (item.getMetadata().isEsaInstrumentChanged()) {
                addThesaurusChangeWarn(3);
                isVersionChanged = true;
            }

            if (thesaurusChangeWarnFiles == null) {
                thesaurusChangeWarnFiles = new ArrayList<>();
            }
            thesaurusChangeWarnFiles.add(item);
        }

        if (item.getMetadata().getIdentification() != null
                && item.getMetadata().getIdentification().getNoMappingScienceKeywords() != null
                && !item.getMetadata().getIdentification().getNoMappingScienceKeywords().isEmpty()) {
            for (Map.Entry<String, String> entry
                    : item.getMetadata().getIdentification().getNoMappingScienceKeywords().entrySet()) {
                addToAutoCorrectionWarnReport(new AutoCorrectionWarning(id, entry.getKey(), entry.getValue(), "", 4,
                        delegator.getConfig().getSckwThesaurus().getLabel(), 3, recordType));
            }
        }

        if (item.getMetadata().getIdentification() != null
                && item.getMetadata().getIdentification().getEarthTopics() != null
                && item.getMetadata().getIdentification().getEarthTopics().size() > 0) {
            for (EarthTopic eTopic : item.getMetadata().getIdentification().getEarthTopics()) {
                if (!eTopic.isEsaEarthTopic()) {
                    // not found URI
                    addToAutoCorrectionWarnReport(new AutoCorrectionWarning(id,
                            eTopic.getUri(), eTopic.getLabel(), "", 1,
                            delegator.getConfig().getEarthtopicThesaurus().getLabel(), 2, recordType));
                    isAutoWarn = true;
                }
                if (eTopic.getWarning() != null) {
                    addToAutoCorrectionWarnReport(eTopic.getWarning());
                    isAutoWarn = true;
                }

                if (eTopic.getScienceKeywords() == null
                        || eTopic.getScienceKeywords().isEmpty()) {
                    addToAutoCorrectionWarnReport(new AutoCorrectionWarning(id, eTopic.getUri(), eTopic.getLabel(), "", 1,
                            delegator.getConfig().getEarthtopicThesaurus().getLabel(), 3, recordType));
                    isAutoWarn = true;
                }

                if (eTopic.getSckWarnings() != null) {
                    isAutoWarn = true;
                    eTopic.getSckWarnings().forEach((warn) -> {
                        addToAutoCorrectionWarnReport(warn);
                    });
                }
            }
        }

        if (item.getMetadata().getAcquisition() != null
                && item.getMetadata().getAcquisition().getPlatforms() != null
                && item.getMetadata().getAcquisition().getPlatforms().size() > 0) {
            for (Platform platform : item.getMetadata().getAcquisition().getPlatforms()) {
                if (platform.isEsaPlatform()) {
                    if (platform.getWarning() != null) {
                        addToAutoCorrectionWarnReport(platform.getWarning());
                        isAutoWarn = true;
                    }
                    if (platform.getGcmdWarning() != null) {
                        addToAutoCorrectionWarnReport(platform.getGcmdWarning());
                        isAutoWarn = true;
                    }

                    if (platform.getGcmd() == null && platform.getAltTitle() != null) {
                        // GCMD Platform URI not found
                        addToAutoCorrectionWarnReport(new AutoCorrectionWarning(id,
                                platform.getAltTitle().getLink(), platform.getAltTitle().getText(), "", 5,
                                delegator.getConfig().getGcmdPlatformThesaurus().getLabel(), 2, recordType));
                        isAutoWarn = true;
                    }
                } else {
                    addToManualCorrectionWarnReport(new ManualCorrectionWarning(id,
                            platform.getUri(), platform.getLabel(), 1, recordType,
                            delegator.getConfig().getPlatformThesaurus().getLabel()));
                    isManualWarn = true;
                }

                if (platform.getInstruments() != null
                        && platform.getInstruments().size() > 0) {
                    for (Instrument inst : platform.getInstruments()) {
                        if (inst.isEsaInstrument()) {
                            if (inst.getWarning() != null) {
                                addToAutoCorrectionWarnReport(inst.getWarning());
                                isAutoWarn = true;
                            }
                            if (inst.getGcmdWarning() != null) {
                                addToAutoCorrectionWarnReport(inst.getGcmdWarning());
                                isAutoWarn = true;
                            }

                            if (!inst.isHosted()) {
                                ManualCorrectionWarning mcWarn = new ManualCorrectionWarning(id,
                                        inst.getUri(), inst.getLabel(), 2, recordType,
                                        delegator.getConfig().getInstrumentThesaurus().getLabel());
                                mcWarn.setHostedUri(platform.getUri());
                                mcWarn.setHostedLabel(platform.getLabel());
                                addToManualCorrectionWarnReport(mcWarn);
                                isManualWarn = true;
                            }

                            if (inst.getInstrumentTypeWarnings() != null) {
                                for (AutoCorrectionWarning warn : inst.getInstrumentTypeWarnings()) {
                                    addToAutoCorrectionWarnReport(warn);
                                    isAutoWarn = true;
                                }
                            }

                            if (inst.getGcmd() == null && inst.getAltTitle() != null) {
                                // GCMD Instrument URI not found
                                addToAutoCorrectionWarnReport(new AutoCorrectionWarning(id,
                                        inst.getAltTitle().getLink(), inst.getAltTitle().getText(), "", 6,
                                        delegator.getConfig().getGcmdInstrumentThesaurus().getLabel(), 2, recordType));
                                isAutoWarn = true;
                            }
                        } else {
                            addToManualCorrectionWarnReport(new ManualCorrectionWarning(id,
                                    inst.getUri(), inst.getLabel(), 2, recordType,
                                    delegator.getConfig().getInstrumentThesaurus().getLabel()));
                            isManualWarn = true;
                        }
                    }
                }
            }
        }

        if (isAutoWarn) {
            if (autoCorrectionWarnFiles == null) {
                autoCorrectionWarnFiles = new ArrayList<>();
            }
            autoCorrectionWarnFiles.add(item);
        }

        if (isManualWarn) {
            if (manualCorrectionWarnFiles == null) {
                manualCorrectionWarnFiles = new ArrayList<>();
            }
            manualCorrectionWarnFiles.add(item);
        }

        if (isVersionChanged || isAutoWarn || isManualWarn) {
            numOfWarnRecords++;
        }
    }

    public void updateWarnMetadataFile() {
        if (this.selectedMetadataFile != null) {
            if (autoCorrectionWarnFiles != null) {
                autoCorrectionWarnFiles.remove(this.selectedMetadataFile);
            }

            if (manualCorrectionWarnFiles != null) {
                manualCorrectionWarnFiles.remove(this.selectedMetadataFile);
            }

            if (thesaurusChangeWarnFiles != null) {
                thesaurusChangeWarnFiles.remove(this.selectedMetadataFile);
            }

            updateWarnMetadataFiles();
        }
    }

    public void updateWarnMetadataFiles() {
        if (thesaurusAutoCorrectionWarnReport != null) {
            thesaurusAutoCorrectionWarnReport.reset();
        }

        if (thesaurusManualCorrectionWarnReport != null) {
            thesaurusManualCorrectionWarnReport.reset();
        }

        thesaurusChangeWarns = null;

        List<MetadataFile> newWarnMetadataFile = new ArrayList<>();
        if (autoCorrectionWarnFiles != null) {
            autoCorrectionWarnFiles.forEach((item) -> {
                newWarnMetadataFile.add(item);
            });
            autoCorrectionWarnFiles = null;
        }

        if (manualCorrectionWarnFiles != null) {
            manualCorrectionWarnFiles.stream().filter((mf) -> (!newWarnMetadataFile.contains(mf))).forEachOrdered((mf) -> {
                newWarnMetadataFile.add(mf);
            });
            manualCorrectionWarnFiles = null;
        }

        if (thesaurusChangeWarnFiles != null) {
            thesaurusChangeWarnFiles.stream().filter((mf) -> (!newWarnMetadataFile.contains(mf))).forEachOrdered((mf) -> {
                newWarnMetadataFile.add(mf);
            });
            thesaurusChangeWarnFiles = null;
        }

        numOfWarnRecords = 0;
        newWarnMetadataFile.forEach((item) -> {
            addToWarnReports(item);
        });

    }

    public void removeWarnMetadataFile(MetadataFile item) {
        boolean update = false;
        if (autoCorrectionWarnFiles != null
                && autoCorrectionWarnFiles.contains(item)) {
            autoCorrectionWarnFiles.remove(item);
            update = true;
        }

        if (manualCorrectionWarnFiles != null
                && manualCorrectionWarnFiles.contains(item)) {
            manualCorrectionWarnFiles.remove(item);
            update = true;
        }

        if (thesaurusChangeWarnFiles != null
                && thesaurusChangeWarnFiles.contains(item)) {
            thesaurusChangeWarnFiles.remove(item);
            update = true;
        }

        if (update) {
            updateWarnMetadataFiles();
        }
    }

//    public List<MetadataFile> getWarnMetadataFiles() {
//        return warnMetadataFiles;
//    }
//
//    public int getNumOfWarnMetadata() {
//        if (warnMetadataFiles != null) {
//            return warnMetadataFiles.size();
//        }
//        return 0;
//    }
    public int getNumOfSeriesRecords() {
        return numOfSeriesRecords;
    }

    public int getNumOfServiceRecords() {
        return numOfServiceRecords;
    }

    private void addToAutoCorrectionWarnReport(AutoCorrectionWarning warn) {
        if (thesaurusAutoCorrectionWarnReport == null) {
            thesaurusAutoCorrectionWarnReport = new AutoCorrectionWarningReport();
        }
        thesaurusAutoCorrectionWarnReport.addWarn(warn);
    }

    private void addToManualCorrectionWarnReport(ManualCorrectionWarning warn) {
        if (thesaurusManualCorrectionWarnReport == null) {
            thesaurusManualCorrectionWarnReport = new ManualCorrectionWarningReport();
        }
        thesaurusManualCorrectionWarnReport.addWarn(warn);
    }

    private void addThesaurusChangeWarn(int type) {
        VoidDataset dataset = null;
        switch (type) {
            // Earth Topic
            case 1:
                dataset = delegator.getConfig().getEarthtopicThesaurus();
                break;
            // Science keywords
            case 2:
                dataset = delegator.getConfig().getSckwThesaurus();
                break;
            // Instrument
            case 3:
                dataset = delegator.getConfig().getInstrumentThesaurus();
                break;
        }
        if (dataset != null) {
            if (thesaurusChangeWarns == null) {
                thesaurusChangeWarns = new ArrayList<>();
                thesaurusChangeWarns.add(new ThesaurusChangeWarning(dataset.getUri(), dataset, 1));
            } else {
                ThesaurusChangeWarning tcWarn = new ThesaurusChangeWarning(dataset.getUri(), dataset, 1);
                if (thesaurusChangeWarns.contains(tcWarn)) {
                    ThesaurusChangeWarning oldTcWarn = thesaurusChangeWarns.get(thesaurusChangeWarns.indexOf(tcWarn));
                    int numOfFiles = oldTcWarn.getNumOfFiles() + 1;
                    oldTcWarn.setNumOfFiles(numOfFiles);
                } else {
                    thesaurusChangeWarns.add(tcWarn);
                }
            }
        }
    }

    public List<MetadataFile> getAutoCorrectionWarnFiles() {
        return autoCorrectionWarnFiles;
    }

    public void setAutoCorrectionWarnFiles(List<MetadataFile> autoCorrectionWarnFiles) {
        this.autoCorrectionWarnFiles = autoCorrectionWarnFiles;
    }

    public ManualCorrectionWarningReport getThesaurusManualCorrectionWarnReport() {
        return thesaurusManualCorrectionWarnReport;
    }

    public void setThesaurusManualCorrectionWarnReport(ManualCorrectionWarningReport thesaurusManualCorrectionWarnReport) {
        this.thesaurusManualCorrectionWarnReport = thesaurusManualCorrectionWarnReport;
    }

    public List<MetadataFile> getManualCorrectionWarnFiles() {
        return manualCorrectionWarnFiles;
    }

    public void setManualCorrectionWarnFiles(List<MetadataFile> manualCorrectionWarnFiles) {
        this.manualCorrectionWarnFiles = manualCorrectionWarnFiles;
    }

    public List<ThesaurusChangeWarning> getThesaurusChangeWarns() {
        return thesaurusChangeWarns;
    }

    public void setThesaurusChangeWarns(List<ThesaurusChangeWarning> thesaurusChangeWarns) {
        this.thesaurusChangeWarns = thesaurusChangeWarns;
    }

    public List<MetadataFile> getThesaurusChangeWarnFiles() {
        return thesaurusChangeWarnFiles;
    }

    public void setThesaurusChangeWarnFiles(List<MetadataFile> thesaurusChangeWarnFiles) {
        this.thesaurusChangeWarnFiles = thesaurusChangeWarnFiles;
    }

    public boolean isWarnMetadata() {
        if (selectedMetadataFile != null) {
            if (autoCorrectionWarnFiles != null
                    && autoCorrectionWarnFiles.contains(selectedMetadataFile)) {
                return true;
            }

            if (manualCorrectionWarnFiles != null
                    && manualCorrectionWarnFiles.contains(selectedMetadataFile)) {
                return true;
            }

            if (thesaurusChangeWarnFiles != null
                    && thesaurusChangeWarnFiles.contains(selectedMetadataFile)) {
                return true;
            }
        }
        return false;
    }

    public int getNumOfWarnRecords() {
        return numOfWarnRecords;
    }

    public void decreaseNumOfWarnRecords() {
        if (this.numOfWarnRecords > 0) {
            this.numOfWarnRecords--;
        }
    }

}
