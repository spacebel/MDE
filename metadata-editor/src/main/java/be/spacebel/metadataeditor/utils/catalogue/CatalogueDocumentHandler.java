/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.catalogue;

import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchParameter;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchUrl;
import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.configuration.CatalogueInterface;
import be.spacebel.metadataeditor.utils.parser.XMLParser;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.HttpInvoker;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.XPathUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class acts as a catalogue documents handler. It invokes either landing
 * page URL or OSDD URL and then extract the catalogue interfaces/information
 * from the returned documents
 *
 * @author mng
 */
public class CatalogueDocumentHandler {

    private final XMLParser xmlParser;
    //private final Map<String, OpenSearchParameter> resourceParameters;

    private static final Logger LOG = Logger.getLogger(CatalogueDocumentHandler.class);

    public CatalogueDocumentHandler() {
        this.xmlParser = new XMLParser();
    }

    /**
     * Invoke the given URL and then extract the catalogue
     * interfaces/information from the returned document
     *
     * @param url Either landing page URL or OSDD URL
     * @param resourceParameters
     * @return Catalogue object that contains the catalogue
     * information/interfaces
     * @throws java.io.IOException
     */
    public Catalogue getCatalogue(String url, Map<String, OpenSearchParameter> resourceParameters)
            throws IOException {
        Map<String, String> details = new HashMap<>();
        String catDoc = HttpInvoker.httpGET(url, details);

        if (details.get(Constants.HTTP_GET_DETAILS_ERROR_CODE) != null) {
            String errorCode = details.get(Constants.HTTP_GET_DETAILS_ERROR_CODE);
            String errorMsg = details.get(Constants.HTTP_GET_DETAILS_ERROR_MSG);
            throw new IOException("Errors while invoking the URL " + url + ": (" + errorCode + ") " + errorMsg);
        } else {
            if (StringUtils.isNotEmpty(catDoc)) {
                try {
                    JsonElement jsonElement = new JsonParser().parse(catDoc);

                    String osddUrl = null;
                    if (jsonElement.isJsonObject()) {
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        JsonElement linksElem = jsonObject.get("links");
                        if (linksElem != null && linksElem.isJsonArray()) {
                            JsonArray linksArray = linksElem.getAsJsonArray();
                            for (JsonElement linkElem : linksArray) {
                                if (linkElem.isJsonObject()) {
                                    String href = getAsString(linkElem.getAsJsonObject(), "href");
                                    String type = getAsString(linkElem.getAsJsonObject(), "type");
                                    if (type != null && "application/opensearchdescription+xml".equalsIgnoreCase(type)) {
                                        osddUrl = href;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (StringUtils.isNotEmpty(osddUrl)) {
                        Catalogue catalogue = new Catalogue();
                        catalogue.setLandingUrl(url);
                        loadCatalogueInfo(osddUrl, catalogue, resourceParameters, true);
                        //catalogue.setWriteAccess(true);
                        return catalogue;
                    } else {
                        throw new IOException("No ODDD URL was found in the landing page");
                    }
                } catch (JsonSyntaxException e) {
                    LOG.debug("The URL is not a landing page URL. It may be an OSDD URL");
                    /*
                        This is not a JSON document ==> The URL is not a landing page URL
                        ==> Continue to check if this is an OSDD URL
                     */
                    Document osddDoc = xmlParser.stream2Document(catDoc);
                    Catalogue catalogue = new Catalogue();
                    catalogue.setOsddUrl(url);
                    if (osddDoc != null) {
                        loadCatalogueInfo(osddDoc, catalogue, resourceParameters, false);
                        return catalogue;
                    }
                }
                throw new IOException("The URL " + url + " is neither a landing page URL nor an OSDD URL.");
            } else {
                throw new IOException("An empty value is returned by the catalogue.");
            }
        }
    }

    private void loadCatalogueInfo(String osddUrl, Catalogue catalogue,
            Map<String, OpenSearchParameter> resourceParameters, boolean accessWrite) throws IOException {
        Map<String, String> details = new HashMap<>();
        String osddContent = HttpInvoker.httpGET(osddUrl, details);
        if (details.get(Constants.HTTP_GET_DETAILS_ERROR_CODE) != null) {
            String errorCode = details.get(Constants.HTTP_GET_DETAILS_ERROR_CODE);
            String errorMsg = details.get(Constants.HTTP_GET_DETAILS_ERROR_MSG);
            throw new IOException("Errors while invoking the OSDD URL " + osddUrl + ": (" + errorCode + ") " + errorMsg);
        } else {
            if (StringUtils.isNotEmpty(osddContent)) {
                Document osddDoc = xmlParser.stream2Document(osddContent);
                if (osddDoc != null) {
                    catalogue.setOsddUrl(osddUrl);
                    loadCatalogueInfo(osddDoc, catalogue, resourceParameters, accessWrite);
                } else {
                    throw new IOException("The OSDD is not valid " + osddContent);
                }
            } else {
                throw new IOException("No OSDD was found from the URL " + osddUrl);
            }
        }
    }

    private void loadCatalogueInfo(Document osddDoc, Catalogue catalogue,
            Map<String, OpenSearchParameter> resourceParameters, boolean accessWrite) throws IOException {
        Map<String, String> osddNamespaces = null;
        if (resourceParameters != null) {
            osddNamespaces = XmlUtils.getNamespaces(osddDoc, true);
        }
        catalogue.setSeriesInterface(getCatalogueInterface(osddDoc,
                "collection", resourceParameters, osddNamespaces, accessWrite));
        catalogue.setServiceInterface(getCatalogueInterface(osddDoc,
                "service", resourceParameters, osddNamespaces, accessWrite));
        catalogue.setTitle(XPathUtils.getNodeValue(osddDoc, "./os:OpenSearchDescription/os:ShortName"));
        catalogue.setDescription(XPathUtils.getNodeValue(osddDoc, "./os:OpenSearchDescription/os:Description"));
        catalogue.setProviderName(XPathUtils.getNodeValue(osddDoc, "./os:OpenSearchDescription/os:Attribution"));
        LOG.debug(catalogue.debug());

//        if (catalogue.getSeriesInterface() == null
//                && catalogue.getServiceInterface() == null) {
//            throw new IOException("Neither collection search URL nor service search URL was found in the OSDD");
//        }
    }

    private CatalogueInterface getCatalogueInterface(Document osddDoc, String rel,
            Map<String, OpenSearchParameter> resourceParameters, Map<String, String> osddNamespaces, boolean accessWrite)
            throws IOException {
        Node urlNode = XPathUtils.getNode(osddDoc, "./os:OpenSearchDescription/os:Url[@type='" + CatalogueClient.OS_ATOM_RESPONSE_FORMAT + "' and @rel='" + rel + "']");
        if (urlNode != null) {
            String templateUrl = XmlUtils.getNodeAttValue(urlNode, "template");
            LOG.debug("Template URL " + templateUrl);
            if (StringUtils.isNotEmpty(templateUrl)) {
                CatalogueInterface catInterface = new CatalogueInterface();
                catInterface.setSearchTemplateUrl(templateUrl);

                if (accessWrite) {
                    String baseUrl = StringUtils.substringBefore(templateUrl, "?");
                    if (StringUtils.isNotEmpty(baseUrl)) {
                        baseUrl = CommonUtils.removeLastSlash(baseUrl);
                        catInterface.setInsertUrl(baseUrl);
                        catInterface.setDeleteUrl(baseUrl + "/{geo:uid}");
                        catInterface.setPresentUrl(baseUrl + "/{geo:uid}");
                    }
                }

                if (resourceParameters != null && osddNamespaces != null) {
                    catInterface.setSearchUrl(parseUrl(urlNode, osddNamespaces, resourceParameters));
                }
                return catInterface;
            }
        }
        return null;
    }

//    private OpenSearchUrl getOpenSearchUrl(String osddUrl, String rel, Map<String, OpenSearchParameter> resourceParameters) throws IOException {
//        LOG.debug("getOpenSearchUrl(osddUrl = " + osddUrl + ")");
//        Map<String, String> details = new HashMap<>();
//        String osddContent = HttpInvoker.httpGET(osddUrl, details);
//        OpenSearchUrl osUrl = null;
//        if (details.get(Constants.HTTP_GET_DETAILS_ERROR_CODE) != null) {
//            String errorCode = details.get(Constants.HTTP_GET_DETAILS_ERROR_CODE);
//            String errorMsg = details.get(Constants.HTTP_GET_DETAILS_ERROR_MSG);
//            throw new IOException("Errors while invoking the OSDD URL " + osddUrl + ": (" + errorCode + ") " + errorMsg);
//        } else {
//            if (StringUtils.isNotEmpty(osddContent)) {
//                Document osddDoc = xmlParser.stream2Document(osddContent);
//                Map<String, String> osddNamespaces = XmlUtils.getNamespaces(osddDoc, true);
//
//                Node urlNode = XPathUtils.getNode(osddDoc, "./os:Url[@rel='" + rel + "' and @type='" + CatalogueClient.OS_ATOM_RESPONSE_FORMAT + "']");
//                if (urlNode != null) {
//                    osUrl = parseUrl(urlNode, osddNamespaces, resourceParameters);
//                }
//            } else {
//                throw new IOException("The OpenSearch Description Document from the  " + osddUrl + " is empty !");
//            }
//        }
//        if (osUrl == null) {
//            throw new IOException("OpenSearch Description Document from " + osddUrl + " does not contain template URL for dataset series search (i.e. type='application/atom+xml',  rel='" + rel + "').");
//        }
//        return osUrl;
//    }
    private OpenSearchUrl parseUrl(Node urlNode, Map<String, String> osddNamespaces,
            Map<String, OpenSearchParameter> resourceParameters) throws IOException {

        LOG.debug("********************************************************************");
        LOG.debug("osddNamespaces");
        LOG.debug("********************************************************************");
        for (Map.Entry<String, String> entry : osddNamespaces.entrySet()) {
            LOG.debug(entry.getKey() + "===" + entry.getValue());
        }
        LOG.debug("********************************************************************");
        /*
         * get list of parameters: value (e.g: searchTerm)/OpenSearchParameter
         */
        Map<String, OpenSearchParameter> osddParameters = new HashMap<>();
        NodeList params = ((Element) urlNode).getElementsByTagNameNS(Constants.OS_PARAM_NAMESPACE, "Parameter");
        if (params.getLength() > 0) {
            for (int pIdx = 0; pIdx < params.getLength(); pIdx++) {
                Node paramNode = params.item(pIdx);
                String valueWithBracket = XmlUtils.getNodeAttValue(paramNode, "value");
                if (StringUtils.isNotEmpty(valueWithBracket)) {
                    String valueWithoutBracket = StringUtils.substringBetween(valueWithBracket,
                            CatalogueClient.PARAM_TOKEN_OPEN, CatalogueClient.PARAM_TOKEN_CLOSE);
                    if (StringUtils.isNotEmpty(valueWithoutBracket)) {
                        valueWithoutBracket = StringUtils.replace(valueWithoutBracket, "?", "");

                        OpenSearchParameter osParam = new OpenSearchParameter();
                        osParam.setIndex(Integer.toString(pIdx + 1));
                        osParam.setValue(valueWithoutBracket);
                        osParam.setName(XmlUtils.getNodeAttValue(paramNode, "name"), valueWithoutBracket);

                        if (valueWithoutBracket.indexOf(":") > 0) {
                            String prefix = StringUtils.substringBefore(valueWithoutBracket, ":");
                            String ns = osddNamespaces.get(prefix);
                            osParam.setNamespace(ns);
                        } else {
                            osParam.setNamespace(Constants.OS_NAMESPACE);
                        }

                        osParam.setLabel(valueWithoutBracket);

                        String tooltip = XmlUtils.getNodeAttValue(paramNode, "title");
                        if (StringUtils.isNotEmpty(tooltip)) {
                            osParam.setHelp(tooltip);
                        }
                        osParam.setType(XmlUtils.getNodeAttValue(paramNode, "type"));
                        String pattern = XmlUtils.getNodeAttValue(paramNode, "pattern");
                        if (StringUtils.isNotEmpty(pattern)) {
                            osParam.setPattern(pattern);
                        }
                        String minInclusive = XmlUtils.getNodeAttValue(paramNode, "minInclusive");
                        if (StringUtils.isNotEmpty(minInclusive)) {
                            osParam.setpMinInclusive(minInclusive);
                        }
                        String maxInclusive = XmlUtils.getNodeAttValue(paramNode, "maxInclusive");
                        if (StringUtils.isNotEmpty(maxInclusive)) {
                            osParam.setpMaxInclusive(maxInclusive);
                        }
                        /*
                         * get list of options
                         */
                        NodeList opChildren = ((Element) paramNode)
                                .getElementsByTagNameNS(Constants.OS_PARAM_NAMESPACE, "Option");
                        if (opChildren.getLength() > 0) {
                            osParam.setOptions(new HashMap<>());
                            for (int idx = 0; idx < opChildren.getLength(); idx++) {
                                Node opChild = opChildren.item(idx);
                                String key = XmlUtils.getNodeAttValue(opChild, "value");
                                String val = XmlUtils.getNodeAttValue(opChild, "label");
                                if (StringUtils.isNotEmpty(key)) {
                                    osParam.getOptions().put(key, StringUtils.isNotEmpty(val) ? val : key);
                                }
                            }
                        }

                        LOG.debug("osdd param key: " + valueWithoutBracket);
                        osddParameters.put(valueWithoutBracket, osParam);
                        LOG.debug(osParam.toString());
                    } else {
                        LOG.debug("Parameter value should be placed between {}:" + valueWithBracket);
                    }
                } else {
                    LOG.debug("Parameter value is empty.");
                }
            }
        }

        String templateUrl = XmlUtils.getNodeAttValue(urlNode, "template");
        LOG.debug("Original templateUrl: " + templateUrl);

        OpenSearchUrl openSearchUrl = new OpenSearchUrl();
        if (StringUtils.isNotEmpty(templateUrl)) {
            String[] tokens = StringUtils.substringsBetween(templateUrl,
                    CatalogueClient.PARAM_TOKEN_OPEN, CatalogueClient.PARAM_TOKEN_CLOSE);
            if (tokens != null) {
                int index = 1;
                for (final String token : tokens) {
                    boolean isRequired = !StringUtils.endsWith(token, "?");
                    String cleanToken = StringUtils.replace(token, "?", "");
                    LOG.debug("cleanToken = " + cleanToken);
                    OpenSearchParameter osddParam = osddParameters.get(cleanToken);

                    String resourceParamKey;
                    String paramNS;
                    if (cleanToken.indexOf(":") > 0) {
                        String prefix = StringUtils.substringBefore(cleanToken, ":");
                        paramNS = osddNamespaces.get(prefix);
                        String localName = StringUtils.substringAfter(cleanToken, ":");
                        resourceParamKey = localName + "#" + paramNS;
                    } else {
                        paramNS = Constants.OS_NAMESPACE;
                        resourceParamKey = cleanToken + "#" + paramNS;
                    }
                    LOG.debug("resourceParamKey = " + resourceParamKey);

                    OpenSearchParameter resourceParam = resourceParameters.get(resourceParamKey);

                    OpenSearchParameter osParam;
                    if (osddParam == null && resourceParam == null) {
                        LOG.debug("The param does not exist in OSDD nor in the resource: " + cleanToken);
                        osParam = new OpenSearchParameter();
                        osParam.setValue(cleanToken);
                        osParam.setName(RandomStringUtils.randomAlphabetic(20) + index);
                        osParam.setLabel(cleanToken);
                        osParam.setNamespace(paramNS);
                    } else {
                        osParam = mergeParameterDetails(osddParam, resourceParam);
                    }
                    osParam.setIndex("" + index);
                    osParam.setRequired(isRequired);

                    String replacement = CatalogueClient.PARAM_TOKEN_OPEN
                            + osParam.getName() + CatalogueClient.PARAM_TOKEN_CLOSE;

                    String searchString = CatalogueClient.PARAM_TOKEN_OPEN
                            + token + CatalogueClient.PARAM_TOKEN_CLOSE;

                    templateUrl = StringUtils.replace(templateUrl, searchString, replacement);
                    openSearchUrl.getParameters().add(osParam);
                    if (Constants.OS_NAMESPACE.equalsIgnoreCase(osParam.getNamespace())
                            && "os_searchTerms".equals(osParam.getName())) {
                        openSearchUrl.setSupportTextSearch(true);
                        openSearchUrl.setTextSearchTitle(osParam.getHelp());
                        openSearchUrl.setFreeTextParameter(osParam);
                    }
                    /*
                     if ("geo_box".equals(osParam.getName())) {
                     String placeNameKey = "placeName#" + Constants.OS_NAMESPACE;
                     LOG.debug("placeNameKey = " + placeNameKey);
                     OpenSearchParameter placeNameParam = resourceParameters.get(placeNameKey);
                     if (placeNameParam != null) {
                     LOG.debug("Place name param is not null.");
                     openSearchUrl.getParameters().add(placeNameParam);
                     }
                     }
                     */
                    index++;
                }
            }
            LOG.debug("Updated templateUrl: " + templateUrl);
            openSearchUrl.setTemplateUrl(templateUrl);

            /*
             * set indexOffset of the url if exist
             */
            String indexOffset = XmlUtils.getNodeAttValue(urlNode, "indexOffset");
            if (StringUtils.isNotEmpty(indexOffset)) {
                try {
                    openSearchUrl.setIndexOffset(Integer.parseInt(indexOffset));
                } catch (NumberFormatException e) {
                }
            }
            /*
             * set pageOffset of the url if exist
             */
            String pageOffset = XmlUtils.getNodeAttValue(urlNode, "pageOffset");
            if (StringUtils.isNotEmpty(pageOffset)) {
                try {
                    openSearchUrl.setPageOffset(Integer.parseInt(pageOffset));
                } catch (NumberFormatException e) {
                }
            }
        } else {
            throw new IOException("The template attribute of <Url> element which has type = application/atom+xml should not be empty.");
        }
        return openSearchUrl;
    }

    private OpenSearchParameter mergeParameterDetails(OpenSearchParameter osddParam, OpenSearchParameter resourceParam) {
        OpenSearchParameter osParam;
        if (resourceParam != null) {
            osParam = cloneOpenSearchParameter(resourceParam);
            LOG.debug("The parameter exists in the resource: " + osParam.getValue());
            if (osddParam != null) {
                LOG.debug("The parameter exists in the osdd too: " + osParam.getValue());

                if (osddParam.getHelp() != null && osddParam.getHelp().length() > 0) {
                    osParam.setHelp(osddParam.getHelp());
                }

                if (osddParam.getOptions() != null) {
                    osParam.setOptions(osddParam.getOptions());
                }
                if (StringUtils.isNotEmpty(osddParam.getPattern())) {
                    osParam.setPattern(osddParam.getPattern());
                }

                if (StringUtils.isNotEmpty(osddParam.getpMinInclusive())) {
                    LOG.debug("minInclusive: " + osddParam.getpMinInclusive());
                    if (Constants.DATE_TYPE.equals(osParam.getType())) {
                        String minInclusive = CommonUtils.getInclusiveDate(osddParam.getpMinInclusive());
                        LOG.debug("date minInclusive: " + minInclusive);
                        osParam.setMinDate(minInclusive);
                        //osParam.setFormValue(minInclusive);
                    } else {
                        try {
                            double num = Double.parseDouble(osddParam.getpMinInclusive());
                            osParam.setMinInclusive(num);
                            osParam.setHasMinInclusive(true);
                        } catch (NumberFormatException e) {
                        }
                    }
                    osParam.setpMinInclusive(osddParam.getpMinInclusive());
                }

                if (StringUtils.isNotEmpty(osddParam.getpMaxInclusive())) {
                    LOG.debug("maxInclusive: " + osddParam.getpMaxInclusive());
                    if (Constants.DATE_TYPE.equals(osParam.getType())) {
                        String maxInclusive = CommonUtils.getInclusiveDate(osddParam.getpMaxInclusive());
                        LOG.debug("date maxInclusive: " + maxInclusive);
                        osParam.setMaxDate(maxInclusive);
                        //osParam.setFormValue(maxInclusive);
                    } else {
                        try {
                            double num = Double.parseDouble(osddParam.getpMaxInclusive());
                            osParam.setMaxInclusive(num);
                            osParam.setHasMaxInclusive(true);
                        } catch (NumberFormatException e) {
                        }
                    }
                    osParam.setpMaxInclusive(osddParam.getpMaxInclusive());
                } else {
                    if (Constants.DATE_TYPE.equals(osParam.getType())) {
                        /*
                         * set the max date = current date
                         */
                        osParam.setMaxDate(CommonUtils.dateToStr(new Date()));
                    }
                }
            }
        } else {
            osParam = cloneOpenSearchParameter(osddParam);
            LOG.debug("The parameter does not exist in the resource but exists in the OSDD: " + osParam.getValue());
            if (StringUtils.isNotEmpty(osddParam.getpMinInclusive())) {
                LOG.debug("minInclusive: " + osddParam.getpMinInclusive());
                if (Constants.DATE_TYPE.equals(osParam.getType())) {
                    String minInclusive = CommonUtils.getInclusiveDate(osddParam.getpMinInclusive());
                    LOG.debug("date minInclusive: " + minInclusive);
                    osParam.setMinDate(minInclusive);
                    //osParam.setFormValue(minInclusive);
                } else {
                    try {
                        double num = Double.parseDouble(osddParam.getpMinInclusive());
                        osParam.setMinInclusive(num);
                        osParam.setHasMinInclusive(true);
                    } catch (NumberFormatException e) {
                    }
                }
                osParam.setpMinInclusive(osddParam.getpMinInclusive());
            }

            if (StringUtils.isNotEmpty(osddParam.getpMaxInclusive())) {
                LOG.debug("maxInclusive: " + osddParam.getpMaxInclusive());
                if (Constants.DATE_TYPE.equals(osParam.getType())) {
                    String maxInclusive = CommonUtils.getInclusiveDate(osddParam.getpMaxInclusive());
                    LOG.debug("date maxInclusive: " + maxInclusive);
                    osParam.setMaxDate(maxInclusive);
                    //osParam.setFormValue(maxInclusive);
                } else {
                    try {
                        double num = Double.parseDouble(osddParam.getpMaxInclusive());
                        osParam.setMaxInclusive(num);
                        osParam.setHasMaxInclusive(true);
                    } catch (NumberFormatException e) {
                    }
                }
                osParam.setpMaxInclusive(osddParam.getpMaxInclusive());
            } else {
                if (Constants.DATE_TYPE.equals(osParam.getType())) {
                    /*
                     * set the max date = current date
                     */
                    osParam.setMaxDate(CommonUtils.dateToStr(new Date()));
                }
            }
        }
        return osParam;
    }

    private OpenSearchParameter cloneOpenSearchParameter(OpenSearchParameter osParam) {
        OpenSearchParameter newOsParam = new OpenSearchParameter();
        newOsParam.setIndex(osParam.getIndex());
        newOsParam.setName(osParam.getName());
        newOsParam.setValue(osParam.getValue());
        newOsParam.setFormValue(osParam.getFormValue());
        newOsParam.setLabel(osParam.getLabel());
        newOsParam.setHelp(osParam.getHelp());
        newOsParam.setOrder(osParam.getOrder());
        newOsParam.setType(osParam.getType());
        newOsParam.setNamespace(osParam.getNamespace());
        newOsParam.setOptions(osParam.getOptions());
        newOsParam.setPattern(osParam.getPattern());
        newOsParam.setMinInclusive(osParam.getMinInclusive());
        newOsParam.setMaxInclusive(osParam.getMaxInclusive());
        newOsParam.setHasMinInclusive(osParam.isHasMinInclusive());
        newOsParam.setHasMaxInclusive(osParam.isHasMaxInclusive());
        newOsParam.setRequired(osParam.isRequired());
        newOsParam.setShow(osParam.isShow());
        newOsParam.setMaxDate(osParam.getMaxDate());
        newOsParam.setMinDate(osParam.getMinDate());
        return newOsParam;
    }

    private String getAsString(JsonObject jsonObject, String name) {
        JsonElement element = jsonObject.get(name);
        if (element != null) {
            return element.getAsString();
        }
        return null;
    }
}
