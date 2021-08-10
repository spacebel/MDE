/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import be.spacebel.metadataeditor.models.catalogue.OpenSearchParameter;
import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import be.spacebel.metadataeditor.models.user.UserPreferences;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.TreeNode;

/**
 * This class represents the application configurations
 *
 * @author mng
 */
public class Configuration implements Serializable {

    private final String workspaceDir;
    private final String xmlDir;
    private final String resourcesDir;
    private final String luceneIndexDir;
    private final String thesaurusDir;
    private final String tempDir;

    private String usersFile;
    private String cataloguesFile;
    private String isoSeriesSchemaLocation;
    private String isoServiceSchemaLocation;
    private String jsonSeriesSchemaLocation;
    private String jsonServiceSchemaLocation;

    // <VOID_DATASET_URI,VOID_DATASET>
    private List<VoidDataset> voidDatasets;
    //<CONCEPT_KEY,CONCEPT>
    //private Map<String, Concept> concepts;

    //<CONCEPT_URI,CONCEPT>
    private Map<String, Concept> earthTopics;
    //<CONCEPT_URI,CONCEPT>
    private Map<String, Concept> platforms;
    //<CONCEPT_URI,CONCEPT>
    private Map<String, Concept> instruments;

    //<CONCEPT_URI,CONCEPT>
    private Map<String, Concept> gcmdSciencekeywords;
    //<CONCEPT_URI,CONCEPT>
    private Map<String, Concept> gcmdPlatforms;
    //<CONCEPT_URI,CONCEPT>
    private Map<String, Concept> gcmdInstruments;

    private Map<String, UserPreferences> users;
    private Map<String, Catalogue> catalogues;
    private Map<String, OpenSearchParameter> resourceParameters;
    private Map<String, String> resourceNamespaces;
    private Map<String, String> restrictionCodes;

    //private List<VoidLinkset> voidLinksets;
    private List<ParameterOption> defaultPlatforms;
    private List<ParameterOption> defaultEarthTopics;

    private List<String> dateTypeValues;
    private List<String> queryableBlacklist;
    private List<String> useLimitations;
    private List<String> onlineRsAppProfiles;
    private List<String> onlineRsProtocols;
    private List<String> onlineRsFunctions;
    private List<String> onlineRSRelatedFields;
    private List<String> roleCodes;
    private List<String> progressCodes;
    private List<String> isoTopicCategories;
    private List<String> processingLevels;
    private List<String> wavelengthInformation;
    private List<String> orbitType;
    private List<String> resolution;

    private String platformThesaurusUri;    

    private VoidDataset platformThesaurus;

    private String earthtopicsThesaurusUri;
    private VoidDataset earthtopicThesaurus;   

    private String sckwThesaurusUri;
    private String oldSckwThesaurusUri;
    private VoidDataset sckwThesaurus;

    private String instrumentThesaurusUri;
    private VoidDataset instrumentThesaurus;

    private String gcmdPlatformThesaurusUri;
    private VoidDataset gcmdPlatformThesaurus;

    private String gcmdInstrumentThesaurusUri;
    private VoidDataset gcmdInstrumentThesaurus;

    private String spatialDataServiceCategoryThesaurusUri;
    private VoidDataset spatialDataServiceCategoryThesaurus;
    private Map<String, String> spatialDataServiceCategories;

    private String gcmdConceptUri;
    private List<String> oldGcmdConceptUris;
    private String gcmdConceptSchemeUri;
    private List<String> oldGcmdConceptSchemeUris;

    private String dateTypeCode;
    private Thesaurus eopThesaurus;
    private Thesaurus eopExtThesaurus;
    private Map<String, Thesaurus> otherThesaurus;

    private int rowsPerPage;
    private int uploadSizeLimit;
    private int uploadFileLimit;
    private boolean omitXmlComments;
    private boolean reportCatalogMsg;

    private List<String> errors;

    //<offering_code,offering>
    private Map<String, Offering> offerings;
    // <protocol,offering_code>
    private Map<String, String> protocolOfferings;
    private List<String> serviceTypes;

    private String dif10ValidatorUrl;

    private int thesauriRefreshInterval;
    // Time of day (in format HH:mm) the refresh execution will start
    private String thesauriRefreshTime;

    private Map<String, Organisation> orgMappings = null;
    private Map<String, String> topicMappings = null;

    private TreeNode earthtopicsTreeNode;
    private TreeNode platformTreeNode;

    private boolean refreshEsaThesaurus;
    private boolean refreshGcmdThesaurus;

    private List<String> collectionProfiles;
    private List<String> serviceProfiles;

    public Configuration(String configDir, String workspaceDir, String tempDir) {
        this.workspaceDir = workspaceDir;
        this.tempDir = tempDir;
        this.resourcesDir = configDir + "/resources";
        this.xmlDir = configDir + "/xml";
        this.thesaurusDir = configDir + "/thesaurus";
        this.luceneIndexDir = configDir + "/luceneIndex";
    }

//    public Configuration(String workspaceDir, String tempDir, String xslDir, String luceneIndexDir,
//            Map<String, VoidDataset> voidDatasets, List<VoidLinkset> voidLinksets,
//            Map<String, Concept> concepts, String defaultNamespaces, List<ParameterOption> defaultPlatforms,
//            Map<String, Concept> platforms, List<ParameterOption> defaultEarthTopics,
//            Map<String, Concept> earthTopics) {
//
//        this.luceneIndexDir = luceneIndexDir;
//        this.workspaceDir = workspaceDir;
//        this.tempDir = tempDir;
//        this.xslDir = xslDir;
//
//        this.voidDatasets = voidDatasets;
//        this.voidLinksets = voidLinksets;
//        this.concepts = concepts;
//        this.defaultNamespaces = defaultNamespaces;
//        this.defaultPlatforms = defaultPlatforms;
//        this.platforms = platforms;
//        this.defaultEarthTopics = defaultEarthTopics;
//        this.earthTopics = earthTopics;
//    }
    public void storeSettings() {
        try {
            Properties settingProps = new Properties();

            settingProps.put("refresh.esa.thesaurus", Boolean.toString(refreshEsaThesaurus));
            settingProps.put("refresh.gcmd.thesaurus", Boolean.toString(refreshGcmdThesaurus));

            FileOutputStream outputStream = new FileOutputStream(resourcesDir + "/settings.properties");
            //Storing the properties to settings.xml file
            settingProps.store(outputStream, "The file is used to store the application settings");
        } catch (IOException e) {
            FacesMessageUtil.addErrorMessage(CommonUtils.getErrorMessage(e));
        }
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }

    public String getXmlDir() {
        return xmlDir;
    }

    public String getThesaurusDir() {
        return thesaurusDir;
    }

    public void setUsers(Map<String, UserPreferences> users) {
        this.users = users;
    }

    public Map<String, UserPreferences> getUsers() {
        return users;
    }

    public Map<String, Concept> getInstruments() {
        return instruments;
    }

    public void setInstruments(Map<String, Concept> instruments) {
        this.instruments = instruments;
    }

    public Map<String, Concept> getGcmdSciencekeywords() {
        return gcmdSciencekeywords;
    }

    public void setGcmdSciencekeywords(Map<String, Concept> gcmdSciencekeywords) {
        this.gcmdSciencekeywords = gcmdSciencekeywords;
    }

    public Map<String, Concept> getGcmdPlatforms() {
        return gcmdPlatforms;
    }

    public void setGcmdPlatforms(Map<String, Concept> gcmdPlatforms) {
        this.gcmdPlatforms = gcmdPlatforms;
    }

    public Map<String, Concept> getGcmdInstruments() {
        return gcmdInstruments;
    }

    public void setGcmdInstruments(Map<String, Concept> gcmdInstruments) {
        this.gcmdInstruments = gcmdInstruments;
    }

    public Concept getEarthTopics(String uri) {
        if (earthTopics != null && earthTopics.containsKey(uri)) {
            return new Concept(earthTopics.get(uri));
        }
        return null;
    }

    public Concept getPlatform(String uri) {
        if (platforms != null && platforms.containsKey(uri)) {
            return new Concept(platforms.get(uri));
        }
        return null;
    }

    public Concept getInstrument(String uri) {
        if (instruments != null && instruments.containsKey(uri)) {
            return new Concept(instruments.get(uri));
        }
        return null;
    }

    public Concept getGcmdScienceKeyword(String uri) {
        if (gcmdSciencekeywords != null && gcmdSciencekeywords.containsKey(uri)) {
            return new Concept(gcmdSciencekeywords.get(uri));
        } else {
            String newUri = sckwThesaurus.getUriSpace() + "/" + StringUtils.substringAfterLast(uri, "/");
            //System.out.println("New SCKW Uri: " + newUri);
            if (gcmdSciencekeywords != null && gcmdSciencekeywords.containsKey(newUri)) {
                return new Concept(gcmdSciencekeywords.get(newUri));
            }
        }
        return null;
    }

    public Concept getGcmdPlatform(String uri) {
        if (gcmdPlatforms != null && gcmdPlatforms.containsKey(uri)) {
            return new Concept(gcmdPlatforms.get(uri));
        } else {
            String newUri = gcmdPlatformThesaurus.getUriSpace() + "/" + StringUtils.substringAfterLast(uri, "/");
            if (gcmdPlatforms != null && gcmdPlatforms.containsKey(newUri)) {
                return new Concept(gcmdPlatforms.get(newUri));
            }
        }
        return null;
    }

    public Concept getGcmdInstrument(String uri) {
        if (gcmdInstruments != null && gcmdInstruments.containsKey(uri)) {
            return new Concept(gcmdInstruments.get(uri));
        } else {
            String newUri = gcmdInstrumentThesaurus.getUriSpace() + "/" + StringUtils.substringAfterLast(uri, "/");
            if (gcmdInstruments != null && gcmdInstruments.containsKey(newUri)) {
                return new Concept(gcmdInstruments.get(newUri));
            }
        }
        return null;
    }

    public boolean isSpatialDataServiceCategory(String kw) {
        if (StringUtils.startsWithIgnoreCase(kw, spatialDataServiceCategoryThesaurusUri)) {
            return true;
        }
        if (spatialDataServiceCategories != null
                && !spatialDataServiceCategories.isEmpty()) {
            kw = spatialDataServiceCategoryThesaurusUri + "/" + kw;
            return spatialDataServiceCategories.containsKey(kw);
        }
        return false;
    }

//    public Concept getConcept(String key) {
//        System.out.println("Get Concept: " + key);
//
//        if (concepts.containsKey(key)) {
//            System.out.println("Has key");
//            return new Concept(concepts.get(key));
//        } else {
//            key = StringUtils.substringAfterLast(key, "/");
//            System.out.println("ID key " + key);
//            if (concepts.containsKey(key)) {
//                System.out.println("Has ID key ");
//                return new Concept(concepts.get(key));
//            }
//        }
//        return null;
//    }
//    public void setVoidDatasets(Map<String, VoidDataset> voidDatasets) {
//        this.voidDatasets = voidDatasets;
//    }
//    public void setConcepts(Map<String, Concept> concepts) {
//        this.concepts = concepts;
//    }
    public void setPlatforms(Map<String, Concept> platforms) {
        this.platforms = platforms;
    }

    public void setEarthTopics(Map<String, Concept> earthTopics) {
        this.earthTopics = earthTopics;
    }

//    public void setVoidLinksets(List<VoidLinkset> voidLinksets) {
//        this.voidLinksets = voidLinksets;
//    }
    public void setDefaultPlatforms(List<ParameterOption> defaultPlatforms) {
        this.defaultPlatforms = defaultPlatforms;
    }

    public void setDefaultEarthTopics(List<ParameterOption> defaultEarthTopics) {
        this.defaultEarthTopics = defaultEarthTopics;
    }

    public Map<String, Concept> getPlatforms() {
        return platforms;
    }

    public void setCatalogues(Map<String, Catalogue> catalogues) {
        this.catalogues = catalogues;
    }

    public Map<String, Catalogue> getCatalogues() {
        return catalogues;
    }

    public Catalogue getCatalogue(String catalogueServerUrl) {
        if (catalogues != null
                && catalogues.containsKey(catalogueServerUrl)) {
            return new Catalogue(catalogues.get(catalogueServerUrl));
        }
        return null;
    }

    public Map<String, OpenSearchParameter> getResourceParameters() {
        return resourceParameters;
    }

    public void setResourceParameters(Map<String, OpenSearchParameter> resourceParameters) {
        this.resourceParameters = resourceParameters;
    }

    public Map<String, String> getResourceNamespaces() {
        return resourceNamespaces;
    }

    public void setResourceNamespaces(Map<String, String> resourceNamespaces) {
        this.resourceNamespaces = resourceNamespaces;
    }

    public String getPlatformThesaurusUri() {
        return platformThesaurusUri;
    }

    public void setPlatformThesaurusUri(String platformThesaurusUri) {
        this.platformThesaurusUri = platformThesaurusUri;
    }

    public String getEarthtopicsThesaurusUri() {
        return earthtopicsThesaurusUri;
    }

    public void setEarthtopicsThesaurusUri(String earthtopicsThesaurusUri) {
        this.earthtopicsThesaurusUri = earthtopicsThesaurusUri;
    }    

    public String getDateTypeCode() {
        return dateTypeCode;
    }

    public void setDateTypeCode(String dateTypeCode) {
        this.dateTypeCode = dateTypeCode;
    }

    public List<String> getDateTypeValues() {
        return dateTypeValues;
    }

    public void setDateTypeValues(List<String> dateTypeValues) {
        this.dateTypeValues = dateTypeValues;
    }

    public Thesaurus getEopThesaurus() {
        return eopThesaurus;
    }

    public void setEopThesaurus(Thesaurus eopThesaurus) {
        this.eopThesaurus = eopThesaurus;
    }

    public Thesaurus getEopExtThesaurus() {
        return eopExtThesaurus;
    }

    public void setEopExtThesaurus(Thesaurus eopExtThesaurus) {
        this.eopExtThesaurus = eopExtThesaurus;
    }

    public Map<String, String> getRestrictionCodes() {
        return restrictionCodes;
    }

    public void setRestrictionCodes(Map<String, String> restrictionCodes) {
        this.restrictionCodes = restrictionCodes;
    }

    public List<String> getQueryableBlacklist() {
        return queryableBlacklist;
    }

    public void setQueryableBlacklist(List<String> queryableBlacklist) {
        this.queryableBlacklist = queryableBlacklist;
    }

    public List<String> getUseLimitations() {
        return useLimitations;
    }

    public void setUseLimitations(List<String> useLimitations) {
        this.useLimitations = useLimitations;
    }

    public List<String> getOnlineRsFunctions() {
        return onlineRsFunctions;
    }

    public void setOnlineRsFunctions(List<String> onlineRsFunctions) {
        this.onlineRsFunctions = onlineRsFunctions;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<String> getProgressCodes() {
        return progressCodes;
    }

    public void setProgressCodes(List<String> progressCodes) {
        this.progressCodes = progressCodes;
    }

    public List<String> getIsoTopicCategories() {
        return isoTopicCategories;
    }

    public void setIsoTopicCategories(List<String> isoTopicCategories) {
        this.isoTopicCategories = isoTopicCategories;
    }

    public List<String> getProcessingLevels() {
        return processingLevels;
    }

    public void setProcessingLevels(List<String> processingLevels) {
        this.processingLevels = processingLevels;
    }

    public List<String> getWavelengthInformation() {
        return wavelengthInformation;
    }

    public void setWavelengthInformation(List<String> wavelengthInformation) {
        this.wavelengthInformation = wavelengthInformation;
    }

    public List<String> getOrbitType() {
        return orbitType;
    }

    public void setOrbitType(List<String> orbitType) {
        this.orbitType = orbitType;
    }

    public List<String> getResolution() {
        return resolution;
    }

    public void setResolution(List<String> resolution) {
        this.resolution = resolution;
    }

    public List<ParameterOption> getDefaultPlatforms() {
        return defaultPlatforms;
    }

    public List<ParameterOption> getDefaultEarthTopics() {
        return defaultEarthTopics;
    }

    public Map<String, Concept> getEarthTopics() {
        return earthTopics;
    }    

    public String getTempDir() {
        return tempDir;
    }

    public String getIsoSeriesSchemaLocation() {
        return isoSeriesSchemaLocation;
    }

    public void setIsoSeriesSchemaLocation(String isoSeriesSchemaLocation) {
        this.isoSeriesSchemaLocation = isoSeriesSchemaLocation;
    }

    public String getIsoServiceSchemaLocation() {
        return isoServiceSchemaLocation;
    }

    public void setIsoServiceSchemaLocation(String isoServiceSchemaLocation) {
        this.isoServiceSchemaLocation = isoServiceSchemaLocation;
    }

    public String getJsonSeriesSchemaLocation() {
        return jsonSeriesSchemaLocation;
    }

    public void setJsonSeriesSchemaLocation(String jsonSeriesSchemaLocation) {
        this.jsonSeriesSchemaLocation = jsonSeriesSchemaLocation;
    }

    public String getJsonServiceSchemaLocation() {
        return jsonServiceSchemaLocation;
    }

    public void setJsonServiceSchemaLocation(String jsonServiceSchemaLocation) {
        this.jsonServiceSchemaLocation = jsonServiceSchemaLocation;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public int getUploadSizeLimit() {
        return uploadSizeLimit;
    }

    public void setUploadSizeLimit(int uploadSizeLimit) {
        this.uploadSizeLimit = uploadSizeLimit;
    }

    public int getUploadFileLimit() {
        return uploadFileLimit;
    }

    public void setUploadFileLimit(int uploadFileLimit) {
        this.uploadFileLimit = uploadFileLimit;
    }    

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getOnlineRsAppProfiles() {
        return onlineRsAppProfiles;
    }

    public void setOnlineRsAppProfiles(List<String> onlineRsAppProfiles) {
        this.onlineRsAppProfiles = onlineRsAppProfiles;
    }

    public List<String> getOnlineRsProtocols() {
        return onlineRsProtocols;
    }

    public void setOnlineRsProtocols(List<String> onlineRsProtocols) {
        this.onlineRsProtocols = onlineRsProtocols;
    }

    public List<String> getOnlineRSRelatedFields() {
        return onlineRSRelatedFields;
    }

    public void setOnlineRSRelatedFields(List<String> onlineRSRelatedFields) {
        this.onlineRSRelatedFields = onlineRSRelatedFields;
    }

    public VoidDataset getEarthtopicThesaurus() {
        return earthtopicThesaurus;
    }

    public void setEarthtopicThesaurus(VoidDataset earthtopicThesaurus) {
        this.earthtopicThesaurus = earthtopicThesaurus;
    }

    public VoidDataset getPlatformThesaurus() {
        return platformThesaurus;
    }

    public void setPlatformThesaurus(VoidDataset platformThesaurus) {
        this.platformThesaurus = platformThesaurus;
    }

    public String getSckwThesaurusUri() {
        return sckwThesaurusUri;
    }

    public void setSckwThesaurusUri(String sckwThesaurusUri) {
        this.sckwThesaurusUri = sckwThesaurusUri;
    }

    public VoidDataset getSckwThesaurus() {
        return sckwThesaurus;
    }

    public void setSckwThesaurus(VoidDataset sckwThesaurus) {
        this.sckwThesaurus = sckwThesaurus;
    }

    public String getInstrumentThesaurusUri() {
        return instrumentThesaurusUri;
    }

    public void setInstrumentThesaurusUri(String instrumentThesaurusUri) {
        this.instrumentThesaurusUri = instrumentThesaurusUri;
    }

    public VoidDataset getInstrumentThesaurus() {
        return instrumentThesaurus;
    }

    public void setInstrumentThesaurus(VoidDataset instrumentThesaurus) {
        this.instrumentThesaurus = instrumentThesaurus;
    }

    public String getGcmdPlatformThesaurusUri() {
        return gcmdPlatformThesaurusUri;
    }

    public void setGcmdPlatformThesaurusUri(String gcmdPlatformThesaurusUri) {
        this.gcmdPlatformThesaurusUri = gcmdPlatformThesaurusUri;
    }

    public VoidDataset getGcmdPlatformThesaurus() {
        return gcmdPlatformThesaurus;
    }

    public void setGcmdPlatformThesaurus(VoidDataset gcmdPlatformThesaurus) {
        this.gcmdPlatformThesaurus = gcmdPlatformThesaurus;
    }

    public VoidDataset getGcmdInstrumentThesaurus() {
        return gcmdInstrumentThesaurus;
    }

    public void setGcmdInstrumentThesaurus(VoidDataset gcmdInstrumentThesaurus) {
        this.gcmdInstrumentThesaurus = gcmdInstrumentThesaurus;
    }

    public String getGcmdInstrumentThesaurusUri() {
        return gcmdInstrumentThesaurusUri;
    }

//    public String getExactPlatformThesaurusUri() {
//        return MetadataUtils.getTargetThesaurusUri(platformThesaurusUri, voidLinksets, Constants.SKOS_EXACTMATCH);
//    }
//
//    public String getExactInstrumentThesaurusUri() {
//        return MetadataUtils.getTargetThesaurusUri(instrumentThesaurusUri, voidLinksets, Constants.SKOS_EXACTMATCH);
//    }
    public void setGcmdInstrumentThesaurusUri(String gcmdInstrumentThesaurusUri) {
        this.gcmdInstrumentThesaurusUri = gcmdInstrumentThesaurusUri;
    }

    public String getSpatialDataServiceCategoryThesaurusUri() {
        return spatialDataServiceCategoryThesaurusUri;
    }

    public void setSpatialDataServiceCategoryThesaurusUri(String spatialDataServiceCategoryThesaurusUri) {
        this.spatialDataServiceCategoryThesaurusUri = spatialDataServiceCategoryThesaurusUri;
    }

    public VoidDataset getSpatialDataServiceCategoryThesaurus() {
        return spatialDataServiceCategoryThesaurus;
    }

    public void setSpatialDataServiceCategoryThesaurus(VoidDataset spatialDataServiceCategoryThesaurus) {
        this.spatialDataServiceCategoryThesaurus = spatialDataServiceCategoryThesaurus;
    }

    public Map<String, String> getSpatialDataServiceCategories() {
        return spatialDataServiceCategories;
    }

    public void setSpatialDataServiceCategories(Map<String, String> spatialDataServiceCategories) {
        this.spatialDataServiceCategories = spatialDataServiceCategories;
    }

    public String getUsersFile() {
        return usersFile;
    }

    public void setUsersFile(String usersFile) {
        this.usersFile = usersFile;
    }

    public String getCataloguesFile() {
        return cataloguesFile;
    }

    public void setCataloguesFile(String cataloguesFile) {
        this.cataloguesFile = cataloguesFile;
    }

    public List<VoidDataset> getVoidDatasets() {
        return voidDatasets;
    }

    public void setVoidDatasets(List<VoidDataset> voidDatasets) {
        this.voidDatasets = voidDatasets;
    }

    public boolean isOmitXmlComments() {
        return omitXmlComments;
    }

    public void setOmitXmlComments(boolean omitXmlComments) {
        this.omitXmlComments = omitXmlComments;
    }

    public boolean isReportCatalogMsg() {
        return reportCatalogMsg;
    }

    public void setReportCatalogMsg(boolean reportCatalogMsg) {
        this.reportCatalogMsg = reportCatalogMsg;
    }

    public Map<String, Offering> getOfferings() {
        return offerings;
    }

    public void addOffering(Offering offering) {
        if (this.offerings == null) {
            this.offerings = new ConcurrentHashMap<>();
        }
        this.offerings.putIfAbsent(offering.getCode(), offering);
    }

    public boolean hasOffering() {
        return (offerings != null && offerings.size() > 0);
    }

    public Offering getOffering(String offeringCode) {
        //System.out.println("Get offering code " + offeringCode);

        if (offerings != null && offerings.containsKey(offeringCode)) {
            System.out.println("Found offering by code " + offeringCode);
            return new Offering(offerings.get(offeringCode));
        }
        return new Offering();
    }

    public Offering findOffering(String suffixCode) {
        if (offerings != null && !offerings.isEmpty()) {
            for (Map.Entry<String, Offering> entry : offerings.entrySet()) {
                if (entry.getKey().endsWith(suffixCode)) {
                    return new Offering(entry.getValue());
                }
            }
        }
        return new Offering();
    }

//    public String getEmptyMdTemplateFile() {
//        return xmlDir + "/emptyMetadataTemplate.xml";
//    }
    public String getDif10TemplateFile() {
        return xmlDir + "/dif10Template.xml";
    }

    public Map<String, Thesaurus> getOtherThesaurus() {
        return otherThesaurus;
    }

    public void setOtherThesaurus(Map<String, Thesaurus> otherThesaurus) {
        this.otherThesaurus = otherThesaurus;
    }

    public Thesaurus getThesaurus(String thesaurusUri) {
        if (otherThesaurus != null && otherThesaurus.containsKey(thesaurusUri)) {
            return otherThesaurus.get(thesaurusUri);
        }
        return null;
    }

    public void addProtocolOffering(String protocol, String offeringCode) {
        if (this.protocolOfferings == null) {
            this.protocolOfferings = new ConcurrentHashMap<>();
        }
        this.protocolOfferings.putIfAbsent(protocol, offeringCode);
    }

    public boolean isOfferingProtocol(String protocol) {
        return (StringUtils.isNotEmpty(protocol)
                && this.protocolOfferings != null
                && this.protocolOfferings.containsKey(protocol));
    }

    public String getDif10ValidatorUrl() {
        return dif10ValidatorUrl;
    }

    public void setDif10ValidatorUrl(String dif10ValidatorUrl) {
        this.dif10ValidatorUrl = dif10ValidatorUrl;
    }

    public int getThesauriRefreshInterval() {
        return thesauriRefreshInterval;
    }

    public void setThesauriRefreshInterval(int thesauriRefreshInterval) {
        this.thesauriRefreshInterval = thesauriRefreshInterval;
    }

    public String getThesauriRefreshTime() {
        return thesauriRefreshTime;
    }

    public void setThesauriRefreshTime(String thesauriRefreshTime) {
        this.thesauriRefreshTime = thesauriRefreshTime;
    }

    public Map<String, Organisation> getOrgMappings() {
        return orgMappings;
    }

    public void setOrgMappings(Map<String, Organisation> orgMappings) {
        this.orgMappings = orgMappings;
    }

    public Organisation getOrganisation(String orgShortName) {
        if (orgMappings != null && orgMappings.containsKey(orgShortName)) {
            return orgMappings.get(orgShortName);
        }
        return null;
    }

    public Map<String, String> getTopicMappings() {
        return topicMappings;
    }

    public void setTopicMappings(Map<String, String> topicMappings) {
        this.topicMappings = topicMappings;
    }

    public String getTopic(String key) {
        if (topicMappings != null && topicMappings.containsKey(key)) {
            return topicMappings.get(key);
        }
        return null;
    }

    public TreeNode getEarthtopicsTreeNode() {
        return earthtopicsTreeNode;
    }

    public void setEarthtopicsTreeNode(TreeNode earthtopicsTreeNode) {
        this.earthtopicsTreeNode = earthtopicsTreeNode;
    }

    public TreeNode getPlatformTreeNode() {
        return platformTreeNode;
    }

    public void setPlatformTreeNode(TreeNode platformTreeNode) {
        this.platformTreeNode = platformTreeNode;
    }

    public String getOldSckwThesaurusUri() {
        return oldSckwThesaurusUri;
    }

    public void setOldSckwThesaurusUri(String oldSckwThesaurusUri) {
        this.oldSckwThesaurusUri = oldSckwThesaurusUri;
    }

    public boolean isRefreshEsaThesaurus() {
        return refreshEsaThesaurus;
    }

    public void setRefreshEsaThesaurus(boolean refreshEsaThesaurus) {
        this.refreshEsaThesaurus = refreshEsaThesaurus;
    }

    public List<String> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void addServiceType(String serviceType) {
        if (this.serviceTypes == null) {
            this.serviceTypes = new ArrayList<>();
        }
        if (!this.serviceTypes.contains(serviceType)) {
            this.serviceTypes.add(serviceType);
        }
    }

    public boolean isRefreshGcmdThesaurus() {
        return refreshGcmdThesaurus;
    }

    public void setRefreshGcmdThesaurus(boolean refreshGcmdThesaurus) {
        this.refreshGcmdThesaurus = refreshGcmdThesaurus;
    }

    public List<String> getCollectionProfiles() {
        return collectionProfiles;
    }

    public void setCollectionProfiles(List<String> collectionProfiles) {
        this.collectionProfiles = collectionProfiles;
    }

    public List<String> getServiceProfiles() {
        return serviceProfiles;
    }

    public void setServiceProfiles(List<String> serviceProfiles) {
        this.serviceProfiles = serviceProfiles;
    }

    public String getGcmdConceptUri() {
        return gcmdConceptUri;
    }

    public void setGcmdConceptUri(String gcmdConceptUri) {
        this.gcmdConceptUri = gcmdConceptUri;
    }

    public List<String> getOldGcmdConceptUris() {
        return oldGcmdConceptUris;
    }

    public void setOldGcmdConceptUris(List<String> oldGcmdConceptUris) {
        this.oldGcmdConceptUris = oldGcmdConceptUris;
    }

    public String getGcmdConceptSchemeUri() {
        return gcmdConceptSchemeUri;
    }

    public void setGcmdConceptSchemeUri(String gcmdConceptSchemeUri) {
        this.gcmdConceptSchemeUri = gcmdConceptSchemeUri;
    }

    public List<String> getOldGcmdConceptSchemeUris() {
        return oldGcmdConceptSchemeUris;
    }

    public void setOldGcmdConceptSchemeUris(List<String> oldGcmdConceptSchemeUris) {
        this.oldGcmdConceptSchemeUris = oldGcmdConceptSchemeUris;
    }

}
