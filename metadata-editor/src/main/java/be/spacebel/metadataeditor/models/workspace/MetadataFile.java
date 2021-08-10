/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.validation.ValidationStatus;
import java.io.Serializable;
import java.util.UUID;
import org.json.JSONObject;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.w3c.dom.Document;

/**
 * This class represents the serialized file of Internal Metadata Model
 *
 * @author mng
 */
public class MetadataFile implements Serializable {

    //private final Logger log = Logger.getLogger(getClass());
    private String fileName;
    private Metadata metadata;
    private Document xmlDoc;
    private String xmlSrc;
    private String geoJsonSrc;
    private JSONObject jsonObject;
    private String dif10;
    private boolean selected;
    private boolean updated;
    private boolean inserted;
    private int index;

    /////////////////////////////////////
    private boolean unsavedOrgTab;
    private boolean unsavedIdTab;
    private boolean unsavedGeoTab;
    private boolean unsavedTempTab;
    private boolean unsavedConstTab;
    private boolean unsavedKwTab;
    private boolean unsavedOfferingTab;
    private boolean unsavedDistTab;
    private boolean unsavedAcquiTab;
    private boolean unsavedOthersTab;
    private final String uuid;

    private ValidationStatus validationStatus;

    private int activeTabIndex;

    public MetadataFile() {
        uuid = UUID.randomUUID().toString();
        activeTabIndex = 0;
    }

    public MetadataFile(Metadata metadata, Document metadataDoc) {
        // this.filePath = filePath;
        this.metadata = metadata;
        if (metadata != null) {
//            if (metadata.getAcquisition() != null
//                    && metadata.getAcquisition().getNoInstrumentPlatforms() != null
//                    && metadata.getAcquisition().getNoInstrumentPlatforms().size() > 0) {
//                this.unsavedAcquiTab = true;
//            }

//            if (metadata.getIdentification() != null
//                    && metadata.getIdentification().getCorrections() != null
//                    && metadata.getIdentification().getCorrections().size() > 0) {
//                this.unsavedKwTab = true;
//            }
        }
        this.xmlDoc = metadataDoc;
        uuid = UUID.randomUUID().toString();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
        if (metadata != null) {
//            if (metadata.getAcquisition() != null
//                    && metadata.getAcquisition().getNoInstrumentPlatforms() != null
//                    && metadata.getAcquisition().getNoInstrumentPlatforms().size() > 0) {
//                this.unsavedAcquiTab = true;
//            }

//            if (metadata.getIdentification() != null
//                    && metadata.getIdentification().getCorrections() != null
//                    && metadata.getIdentification().getCorrections().size() > 0) {
//                this.unsavedKwTab = true;
//            }
        }
    }

    public Document getXmlDoc() {
        return xmlDoc;
    }

    public void setXmlDoc(Document xmlDoc) {
        this.xmlDoc = xmlDoc;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isInserted() {
        return inserted;
    }

    public void setInserted(boolean inserted) {
        this.inserted = inserted;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

//    public void update(String userWorkspaceDir) throws ValidationException, DOMException, LSException, IOException, SAXException, XPathExpressionException {
//        if (metadata != null && xmlDoc != null) {
//            metadata.update();
//            //new GeoJsonParser(config).updateGeoJson(jsonObject, metadata);
//
//            String newFileName = CommonUtils.getFileName(metadata.getOthers().getFileIdentifier());
//            String newFilePath = userWorkspaceDir + "/" + newFileName + ".xml";
//            //new XMLParser().domToFile(xmlDoc, newFilePath);
//
//            if (fileName != null && !newFilePath.equalsIgnoreCase(fileName)) {
//                String oldFilePath = userWorkspaceDir + "/" + fileName + ".xml";
//                log.debug("Deleting old metadata record file " + oldFilePath);
//                if (FileUtils.deleteQuietly(new File(oldFilePath))) {
//                    log.debug("Deleted");
//                }
//                setFileName(newFileName);
//            }
//
//            ValidationErrorsPerFile errorFile = XmlUtils.validateMetadata(newFilePath, config.getSchemaLocation());
//            if (errorFile != null) {
//                List<ValidationErrorsPerFile> list = new ArrayList<>(1);
//                list.add(errorFile);
//                throw new ValidationException(list);
//            }
//
//            resetUnsaved();
//        }
//    }
    //////////////////////////////////////////////
//    private void resetUnsaved() {
//        unsavedOrgTab = false;
//        unsavedIdTab = false;
//        unsavedGeoTab = false;
//        unsavedTempTab = false;
//        unsavedConstTab = false;
//        unsavedKwTab = false;
//        unsavedOfferingTab = false;
//        unsavedDistTab = false;
//        unsavedAcquiTab = false;
//        unsavedOthersTab = false;
//    }
    public boolean isUnsavedOrgTab() {
        return unsavedOrgTab;
    }

    public void setUnsavedOrgTab(boolean unsavedOrgTab) {
        this.unsavedOrgTab = unsavedOrgTab;
    }

    public boolean isUnsavedIdTab() {
        return unsavedIdTab;
    }

    public void setUnsavedIdTab(boolean unsavedIdTab) {
        this.unsavedIdTab = unsavedIdTab;
    }

    public boolean isUnsavedGeoTab() {
        return unsavedGeoTab;
    }

    public void setUnsavedGeoTab(boolean unsavedGeoTab) {
        this.unsavedGeoTab = unsavedGeoTab;
    }

    public boolean isUnsavedTempTab() {
        return unsavedTempTab;
    }

    public void setUnsavedTempTab(boolean unsavedTempTab) {
        this.unsavedTempTab = unsavedTempTab;
    }

    public boolean isUnsavedConstTab() {
        return unsavedConstTab;
    }

    public void setUnsavedConstTab(boolean unsavedConstTab) {
        this.unsavedConstTab = unsavedConstTab;
    }

    public boolean isUnsavedKwTab() {
        return unsavedKwTab;
    }

    public void setUnsavedKwTab(boolean unsavedKwTab) {
        this.unsavedKwTab = unsavedKwTab;
    }

    public boolean isUnsavedOfferingTab() {
        return unsavedOfferingTab;
    }

    public void setUnsavedOfferingTab(boolean unsavedOfferingTab) {
        this.unsavedOfferingTab = unsavedOfferingTab;
    }

    public boolean isUnsavedDistTab() {
        return unsavedDistTab;
    }

    public void setUnsavedDistTab(boolean unsavedDistTab) {
        this.unsavedDistTab = unsavedDistTab;
    }

    public boolean isUnsavedAcquiTab() {
        return unsavedAcquiTab;
    }

    public void setUnsavedAcquiTab(boolean unsavedAcquiTab) {
        this.unsavedAcquiTab = unsavedAcquiTab;
    }

    public boolean isUnsavedOthersTab() {
        return unsavedOthersTab;
    }

    public void setUnsavedOthersTab(boolean unsavedOthersTab) {
        this.unsavedOthersTab = unsavedOthersTab;
    }

    public FlatList getFlatList() {
        FlatList flatList = new FlatList();
        if (metadata != null) {
            if (metadata.getOthers() != null) {
                flatList.setId(metadata.getOthers().getFileIdentifier());
                flatList.setModifiedDate(metadata.getOthers().getLastUpdateDate());
                if (metadata.getOthers().getContacts() != null
                        && !metadata.getOthers().getContacts().isEmpty()) {
                    flatList.setOrganisationName(metadata.getOthers().getContacts().get(0).getOrgName());
                }
            }

            if (metadata.getIdentification() != null) {
                flatList.setTitle(metadata.getIdentification().getTitle());
                flatList.setAbst(metadata.getIdentification().getPlainTextAbstract());

                if (metadata.getIdentification().getTemporal() != null) {
                    flatList.setStartDate(CommonUtils.dateToStr(metadata.getIdentification().getTemporal().getStartDate()));
                    flatList.setEndDate(CommonUtils.dateToStr(metadata.getIdentification().getTemporal().getEndDate()));
                }
            }
        }
        return flatList;
    }

    public String getUuid() {
        return uuid;
    }

    public String getXmlSrc() {
        return xmlSrc;
    }

    public void setXmlSrc(String xmlSrc) {
        this.xmlSrc = xmlSrc;
    }

    public String getInternalModelSrc() {
        if (jsonObject != null) {
            return jsonObject.toString(2);
        }
        return "";
    }

    public String getGeoJsonSrc() {
        return geoJsonSrc;
    }

    public void setGeoJsonSrc(String geoJsonSrc) {
        this.geoJsonSrc = geoJsonSrc;
    }

    public String getDif10() {
        return dif10;
    }

    public void setDif10(String dif10) {
        this.dif10 = dif10;
    }

    public ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public void onTabChange(TabChangeEvent event) {
        Tab activeTab = event.getTab();
        this.activeTabIndex = ((TabView) event.getSource()).getChildren().indexOf(activeTab);
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof MetadataFile) {
            MetadataFile sri = (MetadataFile) obj;
            if (this.uuid.equals(sri.getUuid())) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
        return hash;
    }
}
