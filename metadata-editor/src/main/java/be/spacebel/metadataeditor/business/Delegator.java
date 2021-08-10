/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.business;

import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchParameter;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchParameterComparator;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchUrl;
import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import be.spacebel.metadataeditor.models.catalogue.SearchResultSet;
import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.workspace.Metadata;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.utils.parser.IsoHandler;
import be.spacebel.metadataeditor.utils.parser.XMLParser;
import be.spacebel.metadataeditor.utils.catalogue.CatalogueClient;
import be.spacebel.metadataeditor.utils.catalogue.CatalogueDocumentHandler;
import be.spacebel.metadataeditor.models.user.UserPreferences;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.HttpInvoker;
import be.spacebel.metadataeditor.utils.parser.MetadataParser;
import be.spacebel.metadataeditor.utils.parser.MetadataUtils;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;
import org.xml.sax.SAXException;

/**
 * This class acts as an interface between layout beans and business layer
 *
 * @author mng
 */
public class Delegator {

    private final Logger log = Logger.getLogger(getClass());

    private final Configuration config;
    private final UserPreferences userPreferences;

    private final MetadataParser metadataParser;
    private final IsoHandler isoHandler;
    private CatalogueClient catalogueClient;
    private CatalogueDocumentHandler osddHandler;

    public Delegator(Configuration config, UserPreferences userPreferences)
            throws IOException {
        log.debug("Init Delegator");
        this.config = config;
        this.userPreferences = userPreferences;
        this.metadataParser = new MetadataParser(config);
        this.isoHandler = new IsoHandler(config);

        //paginator = null;
        log.debug("Finished");
    }

    public void loadCatalogueInfo() throws IOException {
        log.debug("Loading catalogue info...");
        this.osddHandler = new CatalogueDocumentHandler();
        if (this.userPreferences.getCatalogue() != null) {
            Catalogue cat = this.userPreferences.getCatalogue();
            if (cat.getSeriesInterface() == null
                    || (cat.getSeriesInterface() != null && cat.getSeriesInterface().getSearchUrl() == null)
                    || cat.getServiceInterface() == null
                    || (cat.getServiceInterface() != null && cat.getServiceInterface().getSearchUrl() == null)) {
                cat = osddHandler.getCatalogue(cat.getServerUrl(), config.getResourceParameters());
                this.userPreferences.setCatalogue(cat);
            }

            if (cat.getSeriesInterface() != null
                    || (cat.getSeriesInterface() != null && cat.getSeriesInterface().getSearchUrl() == null)) {
                checkDisplayParams(cat.getSeriesInterface().getSearchUrl(), null);
            }

            if (cat.getServiceInterface() != null
                    || (cat.getServiceInterface() != null && cat.getServiceInterface().getSearchUrl() == null)) {
                checkDisplayParams(cat.getServiceInterface().getSearchUrl(), null);
            }

        } else {
            log.debug("UserPreferences has no catalogue");
        }
        catalogueClient = new CatalogueClient(config, userPreferences.getCatalogue());
    }

    public DynamicPaginator doSearch(boolean serviceSearch) throws SearchException, IOException {
        log.debug("do a search");

        Map<String, String> inputParams = getCommonParamsValue();

        Map<String, String> dateParams = new HashMap<>();
        OpenSearchUrl searchUrl;
        if (serviceSearch) {
            searchUrl = userPreferences.getCatalogue().getServiceInterface().getSearchUrl();
        } else {
            searchUrl = userPreferences.getCatalogue().getSeriesInterface().getSearchUrl();
        }
        getFormParametersValues(inputParams, searchUrl, dateParams);

        validateStarEndDate(dateParams);

        Map<String, String> result = catalogueClient.search(inputParams, serviceSearch);

        if (result != null) {
            log.debug("handle search results");
            return handleSearchResult(result, inputParams, serviceSearch);
        } else {
            log.debug("Search result is null");
            return null;
        }
    }

    /**
     * Get GeoJson format of the record from the given URL
     *
     * @param geoJsonUrl
     * @return
     */
    public String getGeoJson(String geoJsonUrl) {
        try {

            Map<String, String> errorDetails = new HashMap<>();
            log.debug("Get GeoJson metadata from Url :" + geoJsonUrl);
            String strResponse = HttpInvoker.httpGET(geoJsonUrl, errorDetails);

            if (errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_CODE) != null) {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append(errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_CODE));
                String msg = errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_MSG);
                if (StringUtils.isNotEmpty(msg)) {
                    errorMsg.append(": ").append(msg);
                }
                throw new SearchException(errorMsg.toString(), "");
            }

            if (StringUtils.isNotEmpty(strResponse)) {
                return strResponse;
            }
        } catch (IOException | SearchException e) {
            String errorMsg = "500: " + CommonUtils.getErrorMessage(e);
            log.debug("Errors while collecting offerings from GeoJson Url " + errorMsg);

        }
        return null;
    }

    private DynamicPaginator handleSearchResult(Map<String, String> result,
            Map<String, String> inputParams, boolean serviceSearch) throws SearchException {
        log.debug("handle search result");

        DynamicPaginator paginator = null;
        String osResponse = result.get(Constants.OS_RESPONSE);
        if (StringUtils.isNotEmpty(osResponse)) {

            SearchResultSet searchResults = this.catalogueClient.parseSearchResults(osResponse);

            log.debug("check content of result search");

            if (searchResults.getItems().isEmpty()) {
                log.debug("NO RESULTS");
                FacesMessageUtil.addInfoMessage("search.noresult");
                paginator = new DynamicPaginator(catalogueClient, serviceSearch, true);
            } else {
                log.debug("HAS RESULTS");

                paginator = new DynamicPaginator(catalogueClient,
                        searchResults.getFirstPageLink(),
                        searchResults.getPreviousPageLink(),
                        searchResults.getNextPageLink(),
                        searchResults.getLastPageLink(),
                        searchResults.getTotalResults(),
                        searchResults.getItems(), config.getRowsPerPage(), serviceSearch);
            }

        } else {
            log.debug("No search result");
            FacesMessageUtil.addInfoMessage("search.noresult");
            paginator = new DynamicPaginator(catalogueClient, serviceSearch, true);
        }

        if (paginator != null) {
            paginator.setInputParams(inputParams);
        }

        return paginator;

    }

    private void validateStarEndDate(Map<String, String> params)
            throws SearchException {
        String timeStart = params.get(Constants.TIME_START);
        String timeEnd = params.get(Constants.TIME_END);
        if (StringUtils.isNotEmpty(timeStart)
                && StringUtils.isNotEmpty(timeEnd)) {
            SimpleDateFormat formatter = new SimpleDateFormat(
                    Constants.DATEFORMAT);
            try {
                Date startDate = formatter.parse(timeStart);
                Date endDate = formatter.parse(timeEnd);
                if (startDate.after(endDate)) {
                    throw new SearchException("Validation Error", "Start Date should be less than End Date.");
                }
            } catch (ParseException e) {
                throw new SearchException("Validation Error", "The date is not valid. It should follow the format: " + Constants.DATEFORMAT);
            }
        }
    }

    private void getFormParametersValues(Map<String, String> params, OpenSearchUrl osUrl,
            Map<String, String> dateParams) throws SearchException {
        if (osUrl.getParameters() != null) {
            log.debug("MNG FreeText = " + osUrl.getFreeTextParameter().getFormValue());

            for (OpenSearchParameter osParam : osUrl.getParameters()) {
                osParam.validate();
                log.debug(osParam.getName() + "=" + osParam.getFormValue());
                if (StringUtils.isNotEmpty(osParam.getFormValue())) {
                    if (Constants.DATE_TYPE.equals(osParam.getType())) {
                        String strDate = StringUtils.trim(osParam.getFormValue());
                        if (strDate.length() == "yyyy-MM-dd HH:mm:ss".length() && strDate.contains(" ")) {
                            strDate = StringUtils.replace(strDate, " ", "T") + "Z";
                            params.put(osParam.getName(), strDate);
                        } else {
                            if (strDate.length() == "yyyy-MM-dd".length()) {
                                if (CommonUtils.matchParameter(osParam.getNamespace(), osParam.getValue(), Constants.TIME_NAMESPACE, Constants.TIME_END)) {
                                    strDate += "T23:59:59Z";
                                    dateParams.put(Constants.TIME_END, strDate);
                                } else {
                                    strDate += "T00:00:00Z";
                                    if (CommonUtils.matchParameter(osParam.getNamespace(), osParam.getValue(), Constants.TIME_NAMESPACE, Constants.TIME_START)) {
                                        dateParams.put(Constants.TIME_START, strDate);
                                    }
                                }
                                params.put(osParam.getName(), strDate);
                            } else {
                                log.debug("Value of " + osParam.getLabel() + " is invalid.");
                            }
                        }
                        log.debug(osParam.getName() + " = " + strDate);
                    } else {
                        params.put(osParam.getName(), osParam.getFormValue());
                    }
                }
            }
        }
    }

    private Map<String, String> getCommonParamsValue() {
        Map<String, String> params = new HashMap<>();
        /*
         * set default values for common parameters
         */
        params.put("os_count", "" + config.getRowsPerPage());
        params.put("sru_recordSchema", "server-choice");
        params.put("startIndex", "1");
        return params;
    }

    private void checkDisplayParams(OpenSearchUrl openSearchUrl, Map<String, String> defaultParamValues) {
        if (openSearchUrl.getParameters() != null) {
            OpenSearchParameter pStartDate = null;
            OpenSearchParameter pEndDate = null;
            String startDate = null;
            String endDate = null;

            for (OpenSearchParameter osParam : openSearchUrl.getParameters()) {
                if (CommonUtils.matchParameter(osParam.getNamespace(), osParam.getValue(), Constants.TIME_NAMESPACE, Constants.TIME_START)) {
                    //if ("time_start".equals(osParam.getName())) {
                    pStartDate = osParam;
                }

                if (CommonUtils.matchParameter(osParam.getNamespace(), osParam.getValue(), Constants.TIME_NAMESPACE, Constants.TIME_END) && StringUtils.isNotEmpty(osParam.getMaxDate())) {
                    pEndDate = osParam;
                }

                String paramKey;
                if (osParam.getValue().indexOf(":") > 0) {
                    String localName = StringUtils.substringAfter(osParam.getValue(), ":");
                    paramKey = localName + "#" + osParam.getNamespace();
                } else {
                    paramKey = osParam.getValue() + "#" + Constants.OS_NAMESPACE;
                }

                log.debug("paramKey = " + paramKey);

                if (config.getQueryableBlacklist() != null
                        && config.getQueryableBlacklist().contains(paramKey)) {
                    log.debug("This param is in the blacklist. Do not show it: " + osParam.getName());
                    osParam.setShow(false);
                }

                if (osParam.isShow() && defaultParamValues != null && defaultParamValues.containsKey(osParam.getName())) {
                    String value = defaultParamValues.get(osParam.getName());
                    if (Constants.AUTO_COMPLETE_LIST.equals(osParam.getType())) {
                        String label = "";
                        if (osParam.getOptions() != null) {
                            label = osParam.getOptions().get(value);
                        }
                        osParam.setSelectedOption(new ParameterOption(value, label));
                        log.debug("auto complete option: " + value + "=" + label);
                    } else {
                        if (Constants.DATE_TYPE.equals(osParam.getType())) {
                            if (value.contains("T")) {
                                value = StringUtils.substringBefore(value, "T");
                            }
                            log.debug("date param : " + osParam.getName() + "=" + value);
                            if (CommonUtils.matchParameter(osParam.getNamespace(), osParam.getValue(), Constants.TIME_NAMESPACE, Constants.TIME_START)) {
                                startDate = value;
                            }

                            if (CommonUtils.matchParameter(osParam.getNamespace(), osParam.getValue(), Constants.TIME_NAMESPACE, Constants.TIME_END)) {
                                endDate = value;
                            }

                            //osParam.setFormValue(value);
                        } else {
                            log.debug("another param : " + osParam.getName() + "=" + value);
                            //osParam.setFormValue(value);
                        }
                    }
                }
            }

            checkDate(pStartDate, pEndDate);

            if (pStartDate != null && StringUtils.isNotEmpty(startDate)) {
                //pStartDate.setFormValue(startDate);
            }

            if (pEndDate != null && StringUtils.isNotEmpty(endDate)) {
                //pEndDate.setFormValue(endDate);
            }

            OpenSearchParameterComparator.sort(openSearchUrl.getParameters());
        }
    }

    private void checkDate(OpenSearchParameter pStartDate, OpenSearchParameter pEndDate) {
        String currentDate = CommonUtils.dateToStr(new Date());

        /**
         * validate the max/min date
         */
        Date today = CommonUtils.strToDate(currentDate);
        if (pEndDate != null && StringUtils.isNotEmpty(pEndDate.getMaxDate())) {
            Date maxDate = CommonUtils.strToDate(pEndDate.getMaxDate());
            if (maxDate.after(today)) {
                pEndDate.setMaxDate(currentDate);
            }
        }

        if (pStartDate != null && StringUtils.isNotEmpty(pStartDate.getMinDate())) {
            Date minDate = CommonUtils.strToDate(pStartDate.getMinDate());
            if (minDate.after(today)) {
                pStartDate.setMinDate(currentDate);
            }
        }

        if (pStartDate != null && pEndDate != null) {
            log.debug("Start & end date are not null");

            if (StringUtils.isNotEmpty(pEndDate.getMaxDate())) {
                log.debug("End max date: " + pEndDate.getMaxDate());
                //pEndDate.setFormValue(pEndDate.getMaxDate());

                //pStartDate.setFormValue(pEndDate.getMaxDate());
                pStartDate.setMaxDate(pEndDate.getMaxDate());
            } else {
                log.debug("No end max date.");
                pEndDate.setMaxDate(currentDate);
                //pEndDate.setFormValue(currentDate);

                pStartDate.setMaxDate(currentDate);
                //pStartDate.setFormValue(currentDate);
            }

            if (StringUtils.isNotEmpty(pStartDate.getMinDate())) {
                log.debug("Start min date: " + pStartDate.getMinDate());
                pEndDate.setMinDate(pStartDate.getMinDate());
            }

            /*
             if (series) {
               
             //  remove the default value for start/end dates of series
                 
             pEndDate.setFormValue(null);
             pStartDate.setFormValue(null);
             }
             */
        } else {
            log.debug("Either start or end date is null");

            if (pStartDate != null) {
                log.debug("Start date is not null");
                pStartDate.setMaxDate(currentDate);
                /*
                 if (!series) {
                 pStartDate.setFormValue(currentDate);
                 }
                 */
            }

            if (pEndDate != null) {
                log.debug("End date is not null");
                pEndDate.setMinDate(currentDate);
                /*
                 if (!series) {
                 pEndDate.setFormValue(currentDate);
                 }
                 */
            }
        }

        if (pStartDate != null) {
            log.debug("pStartDate.getMinDate(): " + pStartDate.getMinDate());
            log.debug("pStartDate.getMaxDate(): " + pStartDate.getMaxDate());
            log.debug("pStartDate.getFormValue(): " + pStartDate.getFormValue());
        }

        if (pEndDate != null) {
            log.debug("pEndDate.getMinDate(): " + pEndDate.getMinDate());
            log.debug("pEndDate.getMaxDate(): " + pEndDate.getMaxDate());
            log.debug("pEndDate.getFormValue(): " + pEndDate.getFormValue());
        }
    }

    public Document parseMetadata(String xmlMetadata, Metadata metadata) throws IOException, ParseException {
        return isoHandler.buildMetadata(xmlMetadata, metadata);
    }

//    public MetadataFile loadInternalMetadataModelFile(String filePath,
//            UserPreferences userPreferences, boolean beValidated)
//            throws IOException, ParseException, SAXException {
//        MetadataFile metadataFile = metadataParser.loadInternalMetedataFile(filePath);
//        if (beValidated) {
//            validate(metadataFile, userPreferences);
//        }
//        return metadataFile;
//    }    
    public Metadata buildMetadata(Document metadataDoc) throws IOException, ParseException {
        return isoHandler.buildMetadata(metadataDoc);
    }

//    public void update(String userWorkspaceDir, MetadataFile metadataFile, UserPreferences userPreferences)
//            throws IOException, SAXException, XPathExpressionException {
//        metadataParser.update(userWorkspaceDir, metadataFile);
//        validate(metadataFile, userPreferences);
//    }
    public void applyThesaurusConceptChange(String userWorkspaceDir,
            List<MetadataFile> autoCorrectionWarnFiles, UserPreferences userPreferences)
            throws IOException, SAXException, XPathExpressionException {
        for (MetadataFile mFile : autoCorrectionWarnFiles) {
            MetadataUtils.applyThesaurusConceptChange(mFile.getMetadata(), config);
            metadataParser.update(userWorkspaceDir, mFile, userPreferences);
        }
    }

    public void applyThesaurusVersionChange(String userWorkspaceDir,
            List<MetadataFile> warnMetadataFiles, UserPreferences userPreferences)
            throws DOMException, IOException, SAXException, XPathExpressionException {
        for (MetadataFile mFile : warnMetadataFiles) {
            if (mFile.getMetadata().isEarthtopicChanged()
                    || mFile.getMetadata().isScienceKwChanged()
                    || mFile.getMetadata().isEsaInstrumentChanged()) {
                MetadataUtils.applyThesaurusVersionChange(mFile.getMetadata(), config);
                //isoHandler.updateAndSaveMetadataSources(userWorkspaceDir, mFile);
                metadataParser.update(userWorkspaceDir, mFile, userPreferences);

                mFile.getMetadata().setEarthtopicChanged(false);
                mFile.getMetadata().setScienceKwChanged(false);
                mFile.getMetadata().setEsaInstrumentChanged(false);
            }
        }
    }

//    public void applyThesaurusChange(String userWorkspaceDir,
//            MetadataFile metadataFile, UserPreferences userPreferences)
//            throws IOException, SAXException, XPathExpressionException {
//
//        if (metadataFile.getMetadata().isEarthtopicChanged()
//                || metadataFile.getMetadata().isScienceKwChanged()
//                || metadataFile.getMetadata().isEsaInstrumentChanged()) {
//            MetadataUtils.applyThesaurusVersionChange(metadataFile.getMetadata(), config);
//
//            metadataFile.getMetadata().setEarthtopicChanged(false);
//            metadataFile.getMetadata().setScienceKwChanged(false);
//            metadataFile.getMetadata().setEsaInstrumentChanged(false);
//        }
//
////        if (metadataFile.getMetadata().isHasEarthtopicChangeWarn()
////                || metadataFile.getMetadata().isHasScienceKwChangeWarn()
////                || metadataFile.getMetadata().isHasEsaInstrumentChangeWarn()) {
////            MetadataUtils.applyThesaurusConceptChange(metadataFile.getMetadata(), config);
////        }
//        metadataParser.update(userWorkspaceDir, metadataFile, userPreferences);
//
//    }
//    public void refreshSeriesSources(String userWorkspaceDir, MetadataFile metadataFile, String filePath) throws IOException, SAXException {
//        isoHandler.updateSeriesSources(userWorkspaceDir, metadataFile, filePath);
//    }
    public boolean checkExist(String metadataId, boolean serviceMetadata) throws IOException {
        return catalogueClient.checkExist(metadataId, serviceMetadata);
    }

    public String harvestFile(String metadataFile, boolean serviceMetadata)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        return catalogueClient.harvestFile(metadataFile, serviceMetadata);
    }

    public String harvestZipFile(String zipFile, boolean serviceMetadata)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        return catalogueClient.harvestZipFile(zipFile, serviceMetadata, config.isReportCatalogMsg());
    }

    public String harvestDOM(Document metaDoc, boolean serviceMetadata)
            throws IOException, AuthenticationException {
        return catalogueClient.harvestXML(isoHandler.getXmlParser().serializeDOM(metaDoc), serviceMetadata);
    }

    public boolean removeMetadata(String metadataId, boolean serviceMetadata)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        return catalogueClient.remove(metadataId, serviceMetadata);
    }

    public void saveMetadataToLocal(Document metaDoc, String filePath) throws IOException {
        File file = new File(filePath);
        // create parent directories if nonexistent
        file.getParentFile().mkdirs();
        isoHandler.getXmlParser().domToFile(metaDoc, filePath);
    }

    public Document getIdAndTitle(String xmlSource, List<String> values, boolean isFile) throws IOException, SAXException {
        return isoHandler.getIdAndTitle(xmlSource, values, isFile);
    }

    public Document getId(String isoFile, List<String> values) throws IOException, SAXException {
        return isoHandler.getId(isoFile, values);
    }

    //////////////////////////////////////////////////////    
    public Configuration getConfig() {
        return config;
    }

    public XMLParser getXmlParser() {
        return isoHandler.getXmlParser();
    }

}
