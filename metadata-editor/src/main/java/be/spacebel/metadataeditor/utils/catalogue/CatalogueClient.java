/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.catalogue;

import be.spacebel.metadataeditor.business.AuthenticationException;
import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.business.SearchException;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchUrl;
import be.spacebel.metadataeditor.models.catalogue.SearchResultItem;
import be.spacebel.metadataeditor.models.catalogue.SearchResultSet;
import be.spacebel.metadataeditor.models.configuration.CatalogueInterface;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.utils.parser.XMLParser;
import be.spacebel.metadataeditor.utils.HttpInvoker;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.MetadataParser;
import be.spacebel.metadataeditor.utils.parser.XPathUtils;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Catalogue client utilities
 *
 * @author mng
 */
public class CatalogueClient {

    public static final String XSLPARAM_PART = "part";
    public static final String XSLPARAM_DISPLAYFILTER = "displayFilter";
    public static final String OS_URL_TAG = "Url";
    public static final String OS_RESPONSE_XSL_FILE = "os-response.xsl";
    public static final String OS_ATOM_RESPONSE_FORMAT = "application/atom+xml";
    public static final String PARAM_TOKEN_OPEN = "{";
    public static final String PARAM_TOKEN_CLOSE = "}";
    public static final String OS_TEMPLATE_URL = "osTemplateUrl";
    public static final String OS_INDEX_OFFSET = "osIndexOffset";
    public static final String XSL_GETSEARCHOUTPUT_HTML = "getSearchOutputHTML";
    public static final String XSLPARAM_DISPLAYFILTER_ALLRESULTS = "ALLRESULTS";

    private final Catalogue catalogue;
    private final XMLParser xmlParser;
    //private final IsoHandler isoHandler;
    private final MetadataParser metadataParser;

    private final Logger log = Logger.getLogger(getClass());

    public CatalogueClient(Configuration config, Catalogue catalogue) {
        this.catalogue = catalogue;
        this.xmlParser = new XMLParser();
        //this.isoHandler = new IsoHandler(config);
        this.metadataParser = new MetadataParser(config);
    }

    /**
     * Replace OpenSearch parameters of the template URL by the corresponding
     * values and then invoke the URL
     *
     * @param parameters
     * @param serviceSearch
     * @return
     * @throws java.io.IOException
     * @throws be.spacebel.metadataeditor.business.SearchException
     */
    public Map<String, String> search(Map<String, String> parameters, boolean serviceSearch)
            throws IOException, SearchException {
        OpenSearchUrl searchUrl;
        if (serviceSearch) {
            searchUrl = getServiceInterface().getSearchUrl();
        } else {
            searchUrl = getSeriesInterface().getSearchUrl();
        }
        log.debug("Enter search(templateUrl = " + searchUrl.getTemplateUrl() + ")");

        Map<String, String> result = null;

        if (log.isDebugEnabled()) {
            log.debug("OS params:");
            parameters.entrySet().forEach((entry) -> {
                log.debug(entry.getKey() + "=" + entry.getValue());
            });
        }
        String osUrl = fillParamValuesToOpenSearchUrl(parameters, serviceSearch);
        log.debug("osUrl = " + osUrl);

        if (StringUtils.isNotEmpty(osUrl)) {
            String strResultHML = search(osUrl);
            result = new HashMap<>();
            result.put(Constants.OS_RESPONSE, strResultHML);

            if (StringUtils.isNotEmpty(searchUrl.getTemplateUrl())) {
                result.put(OS_TEMPLATE_URL, searchUrl.getTemplateUrl());
            }
            if (searchUrl.getIndexOffset() != -1) {
                result.put(OS_INDEX_OFFSET, "" + searchUrl.getIndexOffset());
            }
        }
        return result;
    }

    /**
     * Get results from the given page URL and then parse the results into
     * SearchResultSet object; This method is executed to navigate the pages of
     * search results
     *
     * @param pageUrl
     * @return
     * @throws be.spacebel.metadataeditor.business.SearchException
     */
    public SearchResultSet navigatePage(String pageUrl) throws SearchException {
        String result = search(pageUrl);
        if (StringUtils.isNotEmpty(result)) {
            return parseSearchResults(result);
        }
        return null;
    }

    /**
     * Replace OpenSearch parameters of the template URL by the corresponding
     * values; invoke the URL and then parse the results into SearchResultSet
     * object; This method is executed to navigate the pages of search results
     *
     * @param parameters
     * @param serviceSearch
     * @return
     * @throws be.spacebel.metadataeditor.business.SearchException
     * @throws java.io.IOException
     */
    public SearchResultSet navigatePage(Map<String, String> parameters, boolean serviceSearch)
            throws SearchException, IOException {
        String osUrl = fillParamValuesToOpenSearchUrl(parameters, serviceSearch);
        log.debug("osUrl = " + osUrl);

        if (StringUtils.isNotEmpty(osUrl)) {
            String result = search(osUrl);
            if (StringUtils.isNotEmpty(result)) {
                return parseSearchResults(result);
            }
        }
        return null;
    }

    /**
     * Parse XML based text search results into SearchResultSet
     *
     * @param xmlSearchResults
     * @return a SearchResultSet object
     */
    public SearchResultSet parseSearchResults(String xmlSearchResults) {

        SearchResultSet set = new SearchResultSet();

        Document searchResultsDoc = xmlParser.stream2Document(xmlSearchResults);
        if (searchResultsDoc != null) {
            String total = XPathUtils.getNodeValue(searchResultsDoc, "./atom:feed/os:totalResults");
            try {
                set.setTotalResults(Integer.parseInt(total));
            } catch (NumberFormatException e) {
                log.debug(e);
            }

            String firstLink = XPathUtils.getAttributeValue(searchResultsDoc,
                    "./atom:feed/atom:link[@rel='first' and @type='application/atom+xml']", "href");
            if (StringUtils.isNotEmpty(firstLink)) {
                firstLink = StringUtils.trimToEmpty(firstLink);
                if (StringUtils.isNotEmpty(firstLink)) {
                    set.setFirstPageLink(firstLink);
                }
            }

            String prevLink = XPathUtils.getAttributeValue(searchResultsDoc,
                    "./atom:feed/atom:link[(@rel='prev' or @rel='previous') and @type='application/atom+xml']", "href");
            if (StringUtils.isNotEmpty(prevLink)) {
                prevLink = StringUtils.trimToEmpty(prevLink);
                if (StringUtils.isNotEmpty(prevLink)) {
                    set.setPreviousPageLink(prevLink);
                }
            }

            String nextLink = XPathUtils.getAttributeValue(searchResultsDoc,
                    "./atom:feed/atom:link[@rel='next' and @type='application/atom+xml']", "href");
            if (StringUtils.isNotEmpty(nextLink)) {
                nextLink = StringUtils.trimToEmpty(nextLink);
                if (StringUtils.isNotEmpty(nextLink)) {
                    set.setNextPageLink(nextLink);
                }
            }

            String lastLink = XPathUtils.getAttributeValue(searchResultsDoc,
                    "./atom:feed/atom:link[@rel='last' and @type='application/atom+xml']", "href");
            if (StringUtils.isNotEmpty(lastLink)) {
                lastLink = StringUtils.trimToEmpty(lastLink);
                if (StringUtils.isNotEmpty(lastLink)) {
                    set.setLastPageLink(lastLink);
                }
            }

            NodeList entryNodes = XPathUtils.getNodes(searchResultsDoc, "./atom:feed/atom:entry");
            if (entryNodes != null) {
                for (int i = 0; i < entryNodes.getLength(); i++) {
                    Node entryNode = entryNodes.item(i);
                    Node metadataNode = XPathUtils.getNode(entryNode, "./gmi:MI_Metadata");
                    if (metadataNode == null) {
                        metadataNode = XPathUtils.getNode(entryNode, "./gmd:MD_Metadata");
                    }

                    if (metadataNode != null) {
                        SearchResultItem item = new SearchResultItem();
                        log.debug("Looking for GeoJson link....");
//                        String geoJsonLink = null;
//                        NodeList links = XPathUtils.getNodes(entryNode, "./atom:link");
//                        if (links != null && links.getLength() > 0) {
//                            for (int l = 0; l < links.getLength(); l++) {
//                                String type = XmlUtils.getNodeAttValue(links.item(l), "type");
//                                log.debug(type);
//                                if (StringUtils.isNotEmpty(type)
//                                        && (StringUtils.equalsIgnoreCase(type, "application/geo+json;profile=\"http://www.opengis.net/spec/eoc-geojson/1.0\"") 
//                                        || StringUtils.equalsIgnoreCase(type, "application/geo+json;profile=\"http://www.opengis.net/spec/eopad-geojson/1.0\""))) {
//                                    geoJsonLink = XmlUtils.getNodeAttValue(links.item(l), "href");
//                                }
//                                
//                                if (StringUtils.isNotEmpty(geoJsonLink)){
//                                    break;
//                                }
//                            }
//                        }

                        String geoJsonLink = XPathUtils.getNodeValue(entryNode, "./atom:link[@type='application/geo+json;profile=\"http://www.opengis.net/spec/eoc-geojson/1.0\"']/@href");
                        if (StringUtils.isEmpty(geoJsonLink)) {
                            log.debug("GeoJson link is empty, continue to find....");
                            geoJsonLink = XPathUtils.getNodeValue(entryNode, "./atom:link[@type='application/geo+json;profile=\"http://www.opengis.net/spec/eopad-geojson/1.0\"']/@href");
                        }

                        log.debug("GeoJson Link " + geoJsonLink);
                        item.setGeoJsonLink(geoJsonLink);

                        Document metadataDoc = xmlParser.stream2Document(XmlUtils.getNodeContent(metadataNode));
                        try {
                            MetadataFile metadataFile = metadataParser.buildMetadataFile(metadataDoc, false);
                            //       isoHandler.buildMetadataFile(metadataDoc);
                            //metadataFile.setXmlSrc(xmlParser.format(metadataDoc));
                            //isoHandler.setXmlSrc(metadataFile);
                            item.setMetadataFile(metadataFile);
                            set.addItem(item);
                        } catch (IOException | ParseException | XPathExpressionException e) {
                            log.debug("Error while parsing metadata XML " + e);
                        }
                    }
                }
            }

        }
        return set;

        //return xmlSearchResultParser.buildResultSetFromXML(xmlSource);
    }

    public boolean remove(String metadataId, boolean serviceMetadata)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        if (serviceMetadata) {
            log.debug("Removing service metadata: " + metadataId);
        } else {
            log.debug("Removing series metadata: " + metadataId);
        }

        if (!catalogue.isAuthenticated()) {
            throw new AuthenticationException(401, "401 Unauthorized");
        }

        boolean removed;
        Map<String, String> errorDetails = new HashMap<>();
        String encodedMetadataId = URLEncoder.encode(metadataId, StandardCharsets.UTF_8.toString());
        String recordId;
        if (serviceMetadata) {
            recordId = getServiceInterface().getDeleteUrl().replaceAll("\\{geo:uid\\}", encodedMetadataId);
        } else {
            recordId = getSeriesInterface().getDeleteUrl().replaceAll("\\{geo:uid\\}", encodedMetadataId);
        }
        log.debug("Record Id: " + recordId);

        /*
         if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
         removed = HttpInvoker.httpDelete(recordId, username, password, errorDetails);
         } else {
         removed = HttpInvoker.httpDelete(recordId, errorDetails);
         }
         */
        removed = HttpInvoker.httpDelete(recordId,
                catalogue.getUsername(), catalogue.getPassword(), errorDetails);

        String errorCode = errorDetails.get(HttpInvoker.DETAILS_ERROR_CODE);
        String errorMsg = errorDetails.get(HttpInvoker.DETAILS_ERROR_MSG);

        if (errorCode != null) {
            log.debug("Error when removing metadata " + metadataId + " from the catalogue: (" + errorCode + "): " + errorMsg);
            throw new IOException("Errors: (" + errorCode + "): " + StringEscapeUtils.escapeXml10(errorMsg));
        } else {
            log.debug("remove success: " + removed);
        }
        return removed;
    }

    public boolean checkExist(String metadataId, boolean serviceMetadata) throws IOException {
        if (serviceMetadata) {
            log.debug("Check existing of service metadata = " + metadataId);
        } else {
            log.debug("Check existing of series metadata = " + metadataId);
        }

        Map<String, String> errorDetails = new HashMap<>();
        String encodedMetadataId = URLEncoder.encode(metadataId, StandardCharsets.UTF_8.toString());
        String metadataUrl;
        if (serviceMetadata) {
            metadataUrl = getServiceInterface().getPresentUrl().replaceAll("\\{geo:uid\\}", encodedMetadataId);
        } else {
            metadataUrl = getSeriesInterface().getPresentUrl().replaceAll("\\{geo:uid\\}", encodedMetadataId);
        }
        log.debug("Metadata Url: " + metadataUrl);

        String result;
        if (StringUtils.isNotEmpty(catalogue.getUsername())
                && StringUtils.isNotEmpty(catalogue.getPassword())) {
            result = HttpInvoker.httpGET(metadataUrl,
                    catalogue.getUsername(), catalogue.getPassword(), errorDetails);
        } else {
            result = HttpInvoker.httpGET(metadataUrl, errorDetails);
        }
        log.debug("Result = " + result);

        String errorCode = errorDetails.get(HttpInvoker.DETAILS_ERROR_CODE);
        String errorMsg = errorDetails.get(HttpInvoker.DETAILS_ERROR_MSG);
        log.debug("errorMsg = " + errorMsg);

        boolean avail = true;
        if (errorCode != null && errorCode.equalsIgnoreCase("404")) {
            avail = false;
        }

        return avail;
    }

    public String harvestFile(String metadataFile, boolean serviceMetadata)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        log.debug("Inserting metadata file: " + metadataFile);

        if (!catalogue.isAuthenticated()) {
            throw new AuthenticationException(401, "401 Unauthorized");
        }

        Map<String, String> errorDetails = new HashMap<>();

        String insertUrl;
        if (serviceMetadata) {
            insertUrl = getServiceInterface().getInsertUrl();
        } else {
            insertUrl = getSeriesInterface().getInsertUrl();
        }

        String recordId = HttpInvoker.httpPostFile(insertUrl,
                metadataFile, "application/geo+json", catalogue.getUsername(), catalogue.getPassword(), errorDetails, false);

        String errorCode = errorDetails.get(HttpInvoker.DETAILS_ERROR_CODE);
        String errorMsg = errorDetails.get(HttpInvoker.DETAILS_ERROR_MSG);

        if (errorCode != null) {
            log.debug("Error when inserting metadata " + metadataFile
                    + " into the catalogue: (" + errorCode + "): " + errorMsg);
            throw new IOException("Errors: (" + errorCode + "): " + StringEscapeUtils.escapeXml10(errorMsg));
        } else {
            log.debug("harvest success: " + recordId);
            return recordId;
        }
    }

    public String harvestXML(String metadata, boolean serviceMetadata)
            throws IOException, AuthenticationException {
        log.debug("Inserting metadata ");

        if (!catalogue.isAuthenticated()) {
            throw new AuthenticationException(401, "401 Unauthorized");
        }

        Map<String, String> errorDetails = new HashMap<>();
        String insertUrl;
        if (serviceMetadata) {
            insertUrl = getServiceInterface().getInsertUrl();
        } else {
            insertUrl = getSeriesInterface().getInsertUrl();
        }

        String recordId = HttpInvoker.httpPostWithXMLRequest(insertUrl,
                metadata, "application/geo+json", catalogue.getUsername(), catalogue.getPassword(), errorDetails);

        String errorCode = errorDetails.get(HttpInvoker.DETAILS_ERROR_CODE);
        String errorMsg = errorDetails.get(HttpInvoker.DETAILS_ERROR_MSG);

        if (errorCode != null) {
            log.debug("Error when inserting XML metadata into the catalogue: (" + errorCode + "): " + errorMsg);
            throw new IOException("Errors: (" + errorCode + "): " + StringEscapeUtils.escapeXml10(errorMsg));
        } else {
            log.debug("harvest success: " + recordId);
            return recordId;
        }
    }

    public String harvestZipFile(String zipFile, boolean serviceMetadata, boolean reportCatalogMsg)
            throws IOException, AuthenticationException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        log.debug("Inserting zip file  " + zipFile);

        if (!catalogue.isAuthenticated()) {
            throw new AuthenticationException(401, "401 Unauthorized");
        }

        Map<String, String> errorDetails = new HashMap<>();
        String insertUrl;
        if (serviceMetadata) {
            insertUrl = getServiceInterface().getInsertUrl();
        } else {
            insertUrl = getSeriesInterface().getInsertUrl();
        }

        String recordId = HttpInvoker.httpPostFile(insertUrl,
                zipFile, "application/zip", catalogue.getUsername(), catalogue.getPassword(), errorDetails, reportCatalogMsg);

        String errorCode = errorDetails.get(HttpInvoker.DETAILS_ERROR_CODE);
        String errorMsg = errorDetails.get(HttpInvoker.DETAILS_ERROR_MSG);
        String respMsg = errorDetails.get(HttpInvoker.DETAILS_RESPONSE);

        if (errorCode != null) {
            log.debug("Error when inserting ZIP file " + zipFile + " into the catalogue: (" + errorCode + "): " + errorMsg);
            throw new IOException("(" + errorCode + "): " + StringEscapeUtils.escapeXml10(errorMsg));
        } else {
            log.debug("harvest success: " + recordId);

            /**
             * remove the ZIP file after harvesting
             */
            FileUtils.deleteQuietly(new File(zipFile));

            if (reportCatalogMsg) {
                String httpCode = errorDetails.get(HttpInvoker.HTTP_CODE);
                respMsg = "(" + httpCode + "): " + respMsg;
            } else {
                if (StringUtils.isEmpty(respMsg)) {
                    respMsg = recordId;
                }
            }
            return respMsg;
        }
    }

    private String fillParamValuesToOpenSearchUrl(Map<String, String> params, boolean serviceSearch)
            throws IOException {
        String strStartIndex = null;
        OpenSearchUrl searchUrl;
        if (serviceSearch) {
            searchUrl = getServiceInterface().getSearchUrl();
        } else {
            searchUrl = getSeriesInterface().getSearchUrl();
        }

        String opensearchTemplateURL = searchUrl.getTemplateUrl();

        if (params.containsKey("startIndex")
                && StringUtils.isNotEmpty(params.get("startIndex"))) {
            try {
                /*
                 * calculate startIndex to correspond to the back-end. Default
                 * start value of startIndex is 1.
                 */
                int startIndex = Integer.parseInt(params.get("startIndex"));
                if (searchUrl.getIndexOffset() < 1) {
                    strStartIndex = Integer.toString(startIndex - 1);
                }
                if (searchUrl.getIndexOffset() > 1) {
                    strStartIndex = Integer.toString(startIndex
                            + (searchUrl.getIndexOffset() - 1));
                }
            } catch (NumberFormatException e) {
            }

        }
        log.debug("Opensearch URL template:" + searchUrl.getTemplateUrl());
        String[] tokens = StringUtils.substringsBetween(opensearchTemplateURL,
                PARAM_TOKEN_OPEN, PARAM_TOKEN_CLOSE);
        // log.debug("List of opensearch parameters:");
        if (tokens != null) {
            for (String token : tokens) {
                String fullToken = PARAM_TOKEN_OPEN + token + PARAM_TOKEN_CLOSE;
                // log.debug("fullToken : " + fullToken);

                String cleanToken = token.replace("?", "");
                cleanToken = cleanToken.replace(":", "_");
                cleanToken = StringUtils.trim(cleanToken);
                // TODO review this hack (cnl, 06 may 2015):
                // to Lower case because the case Username and Password did not
                // work !
                if (cleanToken.equals("wsse_Username")) {
                    cleanToken = "wsse_username";
                }
                if (cleanToken.equals("wsse_Password")) {
                    cleanToken = "wsse_password";
                }

                // log.debug("cleanToken : " + cleanToken);

                /*
                 * replace the token with user input value if the value is not
                 * empty
                 */
                log.debug("replace " + fullToken + " - cleantoken " + cleanToken + " - "
                        + params.get(cleanToken));
                if (StringUtils.isNotEmpty(params.get(cleanToken))) {
                    // log.debug(cleanToken + "=" + params.get(cleanToken));
                    if ("startIndex".equals(cleanToken)
                            && StringUtils.isNotEmpty(strStartIndex)) {
                        /*
                         * replace startIndex with the value that was calculated
                         * above
                         */
                        opensearchTemplateURL = StringUtils.replace(opensearchTemplateURL,
                                fullToken, strStartIndex);
                    } else {
                        // log.debug("replace "+ fullToken +
                        // " - cleantoken "+cleanToken +
                        // " - "+params.get(cleanToken));
                        opensearchTemplateURL = StringUtils.replace(opensearchTemplateURL,
                                fullToken, params.get(cleanToken));
                    }
                } else {
                    /*
                     * leave empty value for the param
                     */
                    opensearchTemplateURL = StringUtils
                            .replace(opensearchTemplateURL, fullToken, "");
                }
            }
        }
        return opensearchTemplateURL;
    }

    private String search(String osUrl) throws SearchException {
        log.debug("search(osUrl = " + osUrl + ")");
        try {
            Map<String, String> errorDetails = new HashMap<>();
            log.debug("Searching URL :" + osUrl);
            String strResponse = HttpInvoker.httpGET(osUrl, errorDetails);
            log.debug("response -----------");
            // log.debug(strResponse);

            if (errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_CODE) != null) {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append(errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_CODE))
                        .append(": ");
                String msg = errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_MSG);
                if (StringUtils.isNotEmpty(msg)) {
                    Document msgDoc = xmlParser.stream2Document(msg);
                    if (msgDoc != null) {
                        String exText = XPathUtils.getNodeValue(msgDoc, "./ows:ExceptionReport/ows:Exception/ows:ExceptionText");
                        if (StringUtils.isNotEmpty(exText)) {
                            errorMsg.append(exText);
                            String exCode = XPathUtils.getAttributeValue(msgDoc, "./ows:ExceptionReport/ows:Exception", "exceptionCode");
                            if (StringUtils.isNotEmpty(exCode)) {
                                errorMsg.append(" (").append(exCode).append(")");
                            }
                        }
                    }
                }
                throw new SearchException(errorMsg.toString(), "");
            }

            return strResponse;
        } catch (IOException e) {
            String errorMsg = "500: " + CommonUtils.getErrorMessage(e);
            throw new SearchException(errorMsg, "");
        }
    }

    private CatalogueInterface getSeriesInterface() throws IOException {
        if (catalogue.getSeriesInterface() == null) {
            throw new IOException("The catalogue does not support collection interface");
        }
        return catalogue.getSeriesInterface();
    }

    private CatalogueInterface getServiceInterface() throws IOException {
        if (catalogue.getServiceInterface() == null) {
            throw new IOException("The catalogue does not support service interface");
        }
        return catalogue.getServiceInterface();
    }
}
