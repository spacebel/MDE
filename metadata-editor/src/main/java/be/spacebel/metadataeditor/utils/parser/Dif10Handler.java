/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.business.ValidationException;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.configuration.Organisation;
import be.spacebel.metadataeditor.utils.HttpInvoker;
import be.spacebel.metadataeditor.utils.validation.ValidationError;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class implements methods that are used to generate DIF-10 format
 *
 * @author mng
 */
public class Dif10Handler {

    public static String DIF10_NS = "http://gcmd.gsfc.nasa.gov/Aboutus/xml/dif/";

    private final Logger LOG = Logger.getLogger(getClass());

    private final XMLParser xmlParser;
    private final Configuration config;

    public Dif10Handler(Configuration config) {
        this.config = config;
        this.xmlParser = new XMLParser();
    }

    /**
     * Transform ISO metadata to DIF-10
     *
     * @param isoDoc
     * @return
     */
    public String toDif10(Document isoDoc) {
        String dif10Metadata = StringUtils.EMPTY;
        try {

            Document dif10Doc = xmlParser.fileToDom(config.getDif10TemplateFile());

            String identifier = XPathUtils.getNodeValue(isoDoc, "//gmd:fileIdentifier/gco:CharacterString");

            Element root = dif10Doc.getDocumentElement();

            /* Entry_ID */
            if (StringUtils.isNotEmpty(identifier)) {
                Element entryIdEle = dif10Doc.createElementNS(DIF10_NS, "Entry_ID");
                Element shortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
                Element versionEle = dif10Doc.createElementNS(DIF10_NS, "Version");

                String version = XPathUtils.getNodeValue(isoDoc, "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:edition/gco:CharacterString");
                if (StringUtils.isNotEmpty(version)) {
                    versionEle.setTextContent(version);
                } else {
                    versionEle.setTextContent("NA");
                }

                entryIdEle.appendChild(shortNameEle);
                shortNameEle.setTextContent(identifier);
                entryIdEle.appendChild(versionEle);
                root.appendChild(entryIdEle);
            }

            /* get title */
            String title = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
            if (StringUtils.isNotEmpty(title)) {
                Element entryTitleEle = dif10Doc.createElementNS(DIF10_NS, "Entry_Title");
                entryTitleEle.setTextContent(title);
                root.appendChild(entryTitleEle);
            }

            NodeList gcmdMDKeywords = XPathUtils.getNodes(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[contains(gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor, 'GCMD') or contains(gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString, 'GCMD')]");

            if (gcmdMDKeywords != null) {
                boolean hasScKeyword = false;
                for (int i = 0; i < gcmdMDKeywords.getLength(); i++) {

                    Element gcmdMdKeyword = (Element) gcmdMDKeywords.item(i);
                    Node alternateTitle = XPathUtils.getNode(gcmdMdKeyword,
                            "gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle");

                    if (alternateTitle == null) {

                        List<String> gcoStrings = XPathUtils.getNodesValues(gcmdMdKeyword, ".//gco:CharacterString");
                        List<String> anchors = XPathUtils.getNodesValues(gcmdMdKeyword, ".//gmx:Anchor");

                        for (String keyword : anchors) {
                            Element scKeywordsEle = createDif10GCMDKeywords(keyword, dif10Doc);
                            if (scKeywordsEle != null) {
                                root.appendChild(scKeywordsEle);
                                hasScKeyword = true;
                            }
                        }

                        for (String keyword : gcoStrings) {
                            Element scKeywordsEle = createDif10GCMDKeywords(keyword, dif10Doc);
                            if (scKeywordsEle != null) {
                                root.appendChild(scKeywordsEle);
                                hasScKeyword = true;
                            }
                        }

                    }
                }

                if (!hasScKeyword) {
                    Element scKeywordsEle = dif10Doc.createElementNS(DIF10_NS, "Science_Keywords");
                    Element categoryEle = dif10Doc.createElementNS(DIF10_NS, "Category");
                    categoryEle.setTextContent("EARTH SCIENCE");
                    scKeywordsEle.appendChild(categoryEle);
                    Element topicEle = dif10Doc.createElementNS(DIF10_NS, "Topic");
                    topicEle.setTextContent("SPECTRAL/ENGINEERING");
                    scKeywordsEle.appendChild(topicEle);
                    Element termEle = dif10Doc.createElementNS(DIF10_NS, "Term");
                    termEle.setTextContent("VISIBLE WAVELENGTHS");
                    scKeywordsEle.appendChild(termEle);
                    root.appendChild(scKeywordsEle);
                }
            }

            /* ISO_Topic_Category */
            String topicCategoryCode = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode");
            if (StringUtils.isNotEmpty(topicCategoryCode)) {
                String gcmdTopic = config.getTopic(topicCategoryCode);
                if (StringUtils.isNotEmpty(gcmdTopic)) {
                    topicCategoryCode = gcmdTopic;
                }
                Element topicCategoryCodeEle = dif10Doc.createElementNS(DIF10_NS, "ISO_Topic_Category");
                topicCategoryCodeEle.setTextContent(topicCategoryCode);
                root.appendChild(topicCategoryCodeEle);
            }

            NodeList mDKeywords = XPathUtils.getNodes(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords");
            if (mDKeywords != null) {
                for (int i = 0; i < mDKeywords.getLength(); i++) {
                    Element mDKeyword = (Element) mDKeywords.item(i);
                    Node thesaurusName = XPathUtils.getNode(mDKeyword, "gmd:thesaurusName");
                    if (thesaurusName == null) {
                        List<String> gcoStrings = XPathUtils.getNodesValues(mDKeyword, ".//gco:CharacterString");
                        gcoStrings.stream().filter((keyword) -> (!StringUtils.equals(keyword, "DIF10"))).map((keyword) -> {
                            Element ancillaryKeywordEle = dif10Doc.createElementNS(DIF10_NS,
                                    "Ancillary_Keyword");
                            ancillaryKeywordEle.setTextContent(keyword);
                            return ancillaryKeywordEle;
                        }).forEachOrdered((ancillaryKeywordEle) -> {
                            root.appendChild(ancillaryKeywordEle);
                        });
                        List<String> anchors = XPathUtils.getNodesValues(mDKeyword, ".//gmx:Anchor");
                        anchors.stream().filter((keyword) -> (!StringUtils.equals(keyword, "DIF10"))).map((keyword) -> {
                            Element ancillaryKeywordEle = dif10Doc.createElementNS(DIF10_NS, "Ancillary_Keyword");
                            ancillaryKeywordEle.setTextContent(keyword);
                            return ancillaryKeywordEle;
                        }).forEachOrdered((ancillaryKeywordEle) -> {
                            root.appendChild(ancillaryKeywordEle);
                        });
                    }
                }
            }

            NodeList miPlatforms = XPathUtils.getNodes(isoDoc,
                    "//gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform");
            if (miPlatforms != null && miPlatforms.getLength() > 0) {
                for (int i = 0; i < miPlatforms.getLength(); i++) {
                    Node miPlatform = miPlatforms.item(i);
                    Element platformEle = createDif10Platform(isoDoc, miPlatform, dif10Doc);

                    if (platformEle != null) {
                        root.appendChild(platformEle);
                    }
                }
            } else {
                Element platformEle = dif10Doc.createElementNS(DIF10_NS, "Platform");
                root.appendChild(platformEle);
                Element typeEle = dif10Doc.createElementNS(DIF10_NS, "Type");
                platformEle.appendChild(typeEle);
                typeEle.setTextContent("Not provided");

                Element platformShortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
                platformEle.appendChild(platformShortNameEle);
                platformShortNameEle.setTextContent("Not provided");

                Element instrumentEle = dif10Doc.createElementNS(DIF10_NS, "Instrument");
                platformEle.appendChild(instrumentEle);

                Element insShortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
                instrumentEle.appendChild(insShortNameEle);
                insShortNameEle.setTextContent("Not provided");

            }

            Node temporalExtent = XPathUtils.getNode(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent//gmd:EX_TemporalExtent");

            if (temporalExtent != null) {
                Element temporalCoverageEle = createDif10TemporalCoverage(temporalExtent, dif10Doc);
                root.appendChild(temporalCoverageEle);
            }

            String beginPosition = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent//gmd:EX_TemporalExtent//gml:beginPosition");
            if (StringUtils.isNotEmpty(beginPosition)) {
                Element datasetProgressEle = createDif10DatasetProgress(temporalExtent, dif10Doc);
                root.appendChild(datasetProgressEle);
            }

            Element spatialCoverageEle = createDif10SpatialCoverage(isoDoc, dif10Doc);
            root.appendChild(spatialCoverageEle);

            Element projectEle = dif10Doc.createElementNS(DIF10_NS, "Project");
            root.appendChild(projectEle);
            Element projectShortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
            projectShortNameEle.setTextContent("FedEO");
            projectEle.appendChild(projectShortNameEle);

            Element projectLongNameEle = dif10Doc.createElementNS(DIF10_NS, "Long_Name");
            projectLongNameEle.setTextContent("FedEO: Federated EO Gateway");
            projectEle.appendChild(projectLongNameEle);

            Element organizationEle = createDif10Organization(isoDoc, dif10Doc);
            root.appendChild(organizationEle);

            Element summaryEle = dif10Doc.createElementNS(DIF10_NS, "Summary");
            root.appendChild(summaryEle);

            Element abstractEle = dif10Doc.createElementNS(DIF10_NS, "Abstract");
            summaryEle.appendChild(abstractEle);
            String abstractSt = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString");
            abstractEle.setTextContent(abstractSt);

            NodeList onlineResources = XPathUtils.getNodes(isoDoc,
                    "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='information']");

            if (onlineResources != null && onlineResources.getLength() > 0) {
                for (int j = 0; j < onlineResources.getLength(); ++j) {
                    Element onlineResource = (Element) onlineResources.item(j);
                    Element relatedURLEle = createDif10RelatedURL(onlineResource, dif10Doc);
                    root.appendChild(relatedURLEle);
                }
            } else {
                // Add an empty Related_URL
                Element relatedURLEle = dif10Doc.createElementNS(DIF10_NS, "Related_URL");
                Element urlEle = dif10Doc.createElementNS(DIF10_NS, "URL");
                relatedURLEle.appendChild(urlEle);
                root.appendChild(relatedURLEle);
            }

//            String portalClient = BundleUtils.getResource("portal.client");
//            if (StringUtils.isNotEmpty(portalClient)) {
//                Element portalClientRelatedURLEle = createDif10IsoPortalClientsRelatedURL(identifier, portalClient,
//                        dif10Doc);
//                root.appendChild(portalClientRelatedURLEle);
//            }
//            Element isoRelatedURLEle = createDif10IsoXmlRelatedURL(identifier, dif10Doc);
//            root.appendChild(isoRelatedURLEle);
            Element metadataNameEle = dif10Doc.createElementNS(DIF10_NS, "Metadata_Name");
            root.appendChild(metadataNameEle);
            metadataNameEle.setTextContent("CEOS IDN DIF");

            Element metadataVersionEle = dif10Doc.createElementNS(DIF10_NS, "Metadata_Version");
            root.appendChild(metadataVersionEle);
            metadataVersionEle.setTextContent("VERSION 10.2");

            Element metadataDatesEle = createDif10MetadataDates(isoDoc, dif10Doc);
            root.appendChild(metadataDatesEle);

            Element productLevelIdEle = dif10Doc.createElementNS(DIF10_NS, "Product_Level_Id");
            root.appendChild(productLevelIdEle);
            productLevelIdEle.setTextContent("NA");

            mDKeywords = XPathUtils.getNodes(isoDoc, "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords");

            if (mDKeywords != null) {
                for (int i = 0; i < mDKeywords.getLength(); i++) {
                    Element mDKeyword = (Element) mDKeywords.item(i);
                    Element anchorEle = (Element) XPathUtils.getNode(mDKeyword, "gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor");
                    if (anchorEle != null) {
                        String thesaurusName = anchorEle.getTextContent();
                        String thesaurusUri = anchorEle.getAttributeNS(Constants.XLINK_NS, "href");
                        if (thesaurusName != null && thesaurusUri != null) {
                            if (!StringUtils.contains(thesaurusName, "GCMD")
                                    && StringUtils.equals(thesaurusUri, config.getEarthtopicsThesaurusUri())) {
                                String type = XPathUtils.getAttributeValue(mDKeyword, "gmd:type/gmd:MD_KeywordTypeCode", "codeListValue");
                                Element extendedMetadataEle = createDif10ExtendedMetadata(type, thesaurusName, mDKeyword, dif10Doc);
                                root.appendChild(extendedMetadataEle);
                            }
                        }
                    } else {
                        Element gcoStringEle = (Element) XPathUtils.getNode(mDKeyword, "gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString");
                        if (gcoStringEle != null) {

                            String thesaurusName = gcoStringEle.getTextContent();
                            if (thesaurusName != null) {
                                if (!StringUtils.contains(thesaurusName, "GCMD")
                                        && StringUtils.equals(thesaurusName, config.getEarthtopicThesaurus().getFullTitle())) {
                                    String type = XPathUtils.getAttributeValue(mDKeyword, "gmd:type/gmd:MD_KeywordTypeCode", "codeListValue");
                                    Element extendedMetadataEle = createDif10ExtendedMetadata(type, thesaurusName, mDKeyword, dif10Doc);
                                    dif10Doc.appendChild(extendedMetadataEle);
                                }
                            }

                        }
                    }

                }
            }

            dif10Metadata = xmlParser.format(dif10Doc);

        } catch (IOException | ParseException | DOMException | SAXException e) {
            LOG.debug("", e);
        }
        return dif10Metadata;
    }

    public void validateDif10(String dif10Data) throws ValidationException, IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {

        LOG.debug("Validate DIF 10 ....");
        Map<String, String> headers = new HashMap<>();
        headers.put("Cmr-Validate-Keywords", "true");

        Map<String, String> errorDetails = new HashMap<>();

        String response = HttpInvoker.httpPostWithXMLRequest(config.getDif10ValidatorUrl(),
                dif10Data, "application/dif10+xml", headers, errorDetails);
        String errorCode = errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_CODE);

        if (errorCode != null
                && ("404".equalsIgnoreCase(errorCode) || "500".equalsIgnoreCase(errorCode))) {
            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append(errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_CODE))
                    .append(": ");
            errorMsg.append(errorDetails.get(Constants.HTTP_GET_DETAILS_ERROR_MSG));

            LOG.debug("Error " + errorMsg.toString());
        } else {
            LOG.debug("Validation result: " + response);

            if (StringUtils.isNotEmpty(response)) {
                Document validationResultDoc = xmlParser.stream2Document(response);

                ValidationException vEx = new ValidationException();
                NodeList warnings = XPathUtils.getNodes(validationResultDoc, "result/warnings");
                NodeList errors = XPathUtils.getNodes(validationResultDoc, "errors/error");
                if (errors != null && errors.getLength() > 0) {
                    LOG.debug("DIF-10 is not valid.");
                    List<ValidationError> vErrors = new ArrayList<>();
                    for (int i = 0; i < errors.getLength(); i++) {
                        Node e = errors.item(i);

                        String path = XPathUtils.getNodeValue(e, "path");
                        String msg = "";
                        if (StringUtils.isNotEmpty(path)) {
                            msg = msg + path + " : ";
                        }

                        NodeList subErrors = XPathUtils.getNodes(e, "errors/error");
                        if (subErrors != null) {
                            for (int j = 0; j < subErrors.getLength(); j++) {
                                Node subError = subErrors.item(j);
                                msg = msg + subError.getTextContent();
                            }
                        }
                        LOG.debug(msg);
                        ValidationError vError = new ValidationError(FacesMessage.SEVERITY_ERROR, msg, 3, 1);
                        vErrors.add(vError);
                    }

                    vEx.setValidationErrors(vErrors);
                    throw vEx;
                } else {
                    if (warnings != null && warnings.getLength() > 0) {
                        LOG.debug("DIF-10 is valid with warnings.");
                        List<ValidationError> vErrors = new ArrayList<>();

                        for (int i = 0; i < warnings.getLength(); i++) {
                            Node w = warnings.item(i);
                            String warn = XmlUtils.getNodeValue(w);

                            if (StringUtils.isNotEmpty(warn)) {
                                LOG.debug(warn);
                                ValidationError vError = new ValidationError(FacesMessage.SEVERITY_WARN, warn, 3, 1);
                                vErrors.add(vError);
                            }
                        }
                        vEx.setValidationErrors(vErrors);
                        throw vEx;
                    } else {
                        LOG.debug("DIF-10 is valid.");
                    }
                }

            } else {
                LOG.debug("DIF 10 validator returned an empty response");
            }
        }

    }

    private Element createDif10GCMDKeywords(String gcmdKeyWord, Document dif10Doc) {

        Element scKeywordsEle = null;
        String fullKeyword = gcmdKeyWord;
        if (StringUtils.startsWith(gcmdKeyWord, "Science Keywords")
                || StringUtils.startsWith(gcmdKeyWord, "Science Keywords")) {
            fullKeyword = StringUtils.substringAfter(fullKeyword, ">");
        }

        String category = StringUtils.substringBefore(fullKeyword, ">");

        String tmpStr1 = StringUtils.substringAfter(fullKeyword, ">");
        String topic = StringUtils.substringBefore(tmpStr1, ">");

        String tmpStr2 = StringUtils.substringAfter(tmpStr1, ">");
        String term;
        String rest = StringUtils.EMPTY;
        if (StringUtils.contains(tmpStr2, ">")) {
            term = StringUtils.substringBefore(tmpStr2, ">");
            rest = StringUtils.substringAfter(tmpStr2, ">");
        } else {
            term = tmpStr2;
        }

        if (StringUtils.isNotEmpty(category) && StringUtils.isNotEmpty(topic) && StringUtils.isNotEmpty(term)) {
            scKeywordsEle = dif10Doc.createElementNS(DIF10_NS, "Science_Keywords");

            Element categoryEle = dif10Doc.createElementNS(DIF10_NS, "Category");
            categoryEle.setTextContent(StringUtils.upperCase(category.trim()));
            scKeywordsEle.appendChild(categoryEle);
            Element topicEle = dif10Doc.createElementNS(DIF10_NS, "Topic");
            topicEle.setTextContent(StringUtils.upperCase(topic.trim()));
            scKeywordsEle.appendChild(topicEle);
            Element termEle = dif10Doc.createElementNS(DIF10_NS, "Term");
            termEle.setTextContent(StringUtils.upperCase(term.trim()));
            scKeywordsEle.appendChild(termEle);
            int idx = 1;
            while (StringUtils.isNotEmpty(rest)) {
                Element variableLevelEle = dif10Doc.createElementNS(DIF10_NS,
                        "Variable_Level_" + String.valueOf(idx));
                if (StringUtils.contains(rest, ">")) {
                    variableLevelEle
                            .setTextContent(StringUtils.upperCase(StringUtils.substringBefore(rest, ">").trim()));
                } else {
                    variableLevelEle.setTextContent(StringUtils.upperCase(rest.trim()));
                }
                rest = StringUtils.substringAfter(rest, ">");
                scKeywordsEle.appendChild(variableLevelEle);
                idx++;
            }

        }

        return scKeywordsEle;

    }

    private Element createDif10Platform(Document isoDoc, Node miPlatform, Document dif10Doc) {
        Element platformEle = null;

        platformEle = dif10Doc.createElementNS(DIF10_NS, "Platform");
        Element typeEle = dif10Doc.createElementNS(DIF10_NS, "Type");
        platformEle.appendChild(typeEle);

        String platformShortName = XPathUtils.getNodeValue(miPlatform,
                "gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString");

        if (StringUtils.isEmpty(platformShortName)) {
            platformShortName = XPathUtils.getNodeValue(miPlatform,
                    "gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor");
        }

        if (StringUtils.isEmpty(platformShortName)) {
            platformShortName = XPathUtils.getNodeValue(miPlatform,
                    "gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
        }

        if (StringUtils.isEmpty(platformShortName)) {
            platformShortName = XPathUtils.getNodeValue(miPlatform,
                    "gmi:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");
        }

        if (StringUtils.isEmpty(platformShortName)) {
            platformShortName = "Not provided";
        }

        String platformType = "Earth Observation Satellites";
        NodeList platformKeyWords = XPathUtils.getNodes(isoDoc,
                ".//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString='Platforms']");

        if (platformKeyWords != null) {
            for (int i = 0; i < platformKeyWords.getLength(); i++) {

                Node platformKeyWord = platformKeyWords.item(i);

                String keywordValue = XPathUtils.getNodeValue(platformKeyWord, "gmd:keyword/gmx:Anchor");
                if (StringUtils.isEmpty(keywordValue)) {
                    keywordValue = XPathUtils.getNodeValue(platformKeyWord, "gmd:keyword/gco:CharacterString");
                }

                if (StringUtils.equals(platformShortName, keywordValue)) {
                    String keywordTypeCode = XPathUtils.getNodeValue(platformKeyWord, "gmd:type/gmd:MD_KeywordTypeCode");
                    if (StringUtils.isNotEmpty(keywordTypeCode)) {
                        platformType = keywordTypeCode;
                    }
                }
            }
        }

        typeEle.setTextContent(platformType);

        Element platformShortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
        platformEle.appendChild(platformShortNameEle);
        platformShortNameEle.setTextContent(platformShortName);

        String platformLongName = XPathUtils.getNodeValue(miPlatform, "gmi:description/gco:CharacterString");
        if (StringUtils.isEmpty(platformLongName)) {
            XPathUtils.getNodeValue(miPlatform, "gmi:description/gmx:Anchor");
        }

        Element platformLongNameEle = dif10Doc.createElementNS(DIF10_NS, "Long_Name");
        platformEle.appendChild(platformLongNameEle);
        platformLongNameEle.setTextContent(platformLongName);

        NodeList miInstruments = XPathUtils.getNodes(miPlatform, "gmi:instrument/gmi:MI_Instrument");
        if (miInstruments != null && miInstruments.getLength() > 0) {
            for (int i = 0; i < miInstruments.getLength(); i++) {
                Node miInstrument = miInstruments.item(i);

                Element instrumentEle = dif10Doc.createElementNS(DIF10_NS, "Instrument");
                platformEle.appendChild(instrumentEle);

                String instrumentShortName = XPathUtils.getNodeValue(miInstrument,
                        "gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString");

                if (StringUtils.isEmpty(instrumentShortName)) {
                    instrumentShortName = XPathUtils.getNodeValue(miInstrument,
                            "gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor");
                }

                if (StringUtils.isEmpty(instrumentShortName)) {
                    instrumentShortName = XPathUtils.getNodeValue(miInstrument,
                            "gmi:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
                }

                if (StringUtils.isEmpty(instrumentShortName)) {
                    instrumentShortName = XPathUtils.getNodeValue(miInstrument,
                            "gmi:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");
                }

                if (StringUtils.isEmpty(instrumentShortName)) {
                    instrumentShortName = "Not provided";
                }

                Element insShortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
                instrumentEle.appendChild(insShortNameEle);
                insShortNameEle.setTextContent(instrumentShortName);

                String instrumentLongName = XPathUtils.getNodeValue(miInstrument,
                        "gmi:description/gco:CharacterString");
                if (StringUtils.isEmpty(instrumentLongName)) {
                    instrumentLongName = XPathUtils.getNodeValue(miInstrument,
                            "gmi:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
                }

                if (StringUtils.isEmpty(instrumentLongName)) {
                    instrumentLongName = XPathUtils.getNodeValue(miInstrument,
                            "gmi:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");
                }

                Element insLongNameEle = dif10Doc.createElementNS(DIF10_NS, "Long_Name");
                instrumentEle.appendChild(insLongNameEle);
                insLongNameEle.setTextContent(instrumentLongName);
            }
        } else {
            Element instrumentEle = dif10Doc.createElementNS(DIF10_NS, "Instrument");
            platformEle.appendChild(instrumentEle);

            Element insShortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
            instrumentEle.appendChild(insShortNameEle);
            insShortNameEle.setTextContent("Not provided");

        }
        return platformEle;
    }

    private Element createDif10TemporalCoverage(Node temporalExtent, Document dif10Doc) {

        String beginPosition = "Not provided";
        if (temporalExtent != null) {
            beginPosition = XPathUtils.getNodeValue(temporalExtent, "//gml:beginPosition");
            if (StringUtils.isEmpty(beginPosition)) {
                beginPosition = "Not provided";

            }
        }

        Element temporalCoverageEle = dif10Doc.createElementNS(DIF10_NS, "Temporal_Coverage");
        Element rangeDateTimeEle = dif10Doc.createElementNS(DIF10_NS, "Range_DateTime");
        temporalCoverageEle.appendChild(rangeDateTimeEle);
        Element beginningDateTimeEle = dif10Doc.createElementNS(DIF10_NS, "Beginning_Date_Time");
        rangeDateTimeEle.appendChild(beginningDateTimeEle);
        beginningDateTimeEle.setTextContent(beginPosition);

        if (temporalExtent != null) {
            String endPosition = XPathUtils.getNodeValue(temporalExtent, "//gml:endPosition");
            if (StringUtils.isNotEmpty(endPosition)) {
                Element endingDateTimeEle = dif10Doc.createElementNS(DIF10_NS, "Ending_Date_Time");
                rangeDateTimeEle.appendChild(endingDateTimeEle);
                endingDateTimeEle.setTextContent(endPosition);
            }
        }

        return temporalCoverageEle;
    }

    private Element createDif10DatasetProgress(Node temporalExtent, Document dif10Doc) throws ParseException {

        Date currentDate = new Date();

        Element datasetProgressEle = dif10Doc.createElementNS(DIF10_NS, "Dataset_Progress");
        String beginPosition = XPathUtils.getNodeValue(temporalExtent, "//gml:beginPosition");
        String endPosition = XPathUtils.getNodeValue(temporalExtent, "//gml:endPosition");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date beginDate = formatter.parse(StringUtils.substring(beginPosition, 0, 10));

        if (beginDate.after(currentDate)) {
            datasetProgressEle.setTextContent("PLANNED");
        } else {
            if (StringUtils.isEmpty(endPosition)) {
                datasetProgressEle.setTextContent("IN WORK");
            } else {
                Date endDate = formatter.parse(StringUtils.substring(endPosition, 0, 10));
                if (endDate.before(currentDate)) {
                    datasetProgressEle.setTextContent("COMPLETE");
                } else {
                    datasetProgressEle.setTextContent("IN WORK");
                }
            }
        }

        return datasetProgressEle;
    }

    private Element createDif10SpatialCoverage(Document isoDoc, Document dif10Doc) throws ParseException {
        Element spatialCoverageEle = dif10Doc.createElementNS(DIF10_NS, "Spatial_Coverage");
        Element spatialRepEle = dif10Doc.createElementNS(DIF10_NS, "Granule_Spatial_Representation");
        spatialRepEle.setTextContent("NO_SPATIAL");
        spatialCoverageEle.appendChild(spatialRepEle);

        Element geometryEle = dif10Doc.createElementNS(DIF10_NS, "Geometry");
        spatialCoverageEle.appendChild(geometryEle);

        Element coorSystemEle = dif10Doc.createElementNS(DIF10_NS, "Coordinate_System");
        coorSystemEle.setTextContent("CARTESIAN");
        geometryEle.appendChild(coorSystemEle);

        Element boundingRectangleEle = dif10Doc.createElementNS(DIF10_NS, "Bounding_Rectangle");
        geometryEle.appendChild(boundingRectangleEle);

        String southLat = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent//gmd:EX_GeographicBoundingBox//gmd:southBoundLatitude/gco:Decimal");
        Element southLatEle = dif10Doc.createElementNS(DIF10_NS, "Southernmost_Latitude");
        southLatEle.setTextContent(southLat);
        boundingRectangleEle.appendChild(southLatEle);

        String northLat = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent//gmd:EX_GeographicBoundingBox//gmd:northBoundLatitude/gco:Decimal");
        Element northLatEle = dif10Doc.createElementNS(DIF10_NS, "Northernmost_Latitude");
        northLatEle.setTextContent(northLat);
        boundingRectangleEle.appendChild(northLatEle);

        String westLon = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent//gmd:EX_GeographicBoundingBox//gmd:westBoundLongitude/gco:Decimal");
        Element westLonEle = dif10Doc.createElementNS(DIF10_NS, "Westernmost_Longitude");
        westLonEle.setTextContent(westLon);
        boundingRectangleEle.appendChild(westLonEle);

        String eastLon = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent//gmd:EX_GeographicBoundingBox//gmd:eastBoundLongitude/gco:Decimal");
        Element eastLonEle = dif10Doc.createElementNS(DIF10_NS, "Easternmost_Longitude");
        eastLonEle.setTextContent(eastLon);
        boundingRectangleEle.appendChild(eastLonEle);

        return spatialCoverageEle;
    }

    private Element createDif10Organization(Document isoDoc, Document dif10Doc) {

        Node responsibleParty = XPathUtils.getNode(isoDoc, ".//gmd:contact/gmd:CI_ResponsibleParty");

        Element organizationEle = dif10Doc.createElementNS(DIF10_NS, "Organization");
        Element organizationTypeEle = dif10Doc.createElementNS(DIF10_NS, "Organization_Type");
        organizationTypeEle.setTextContent("DISTRIBUTOR");
        organizationEle.appendChild(organizationTypeEle);

        Element organizationNameEle = dif10Doc.createElementNS(DIF10_NS, "Organization_Name");
        organizationEle.appendChild(organizationNameEle);

        String orgShortName = XPathUtils.getNodeValue(responsibleParty, "gmd:organisationName/gco:CharacterString");

        Element organizationShortNameEle = dif10Doc.createElementNS(DIF10_NS, "Short_Name");
        organizationShortNameEle.setTextContent(orgShortName);
        organizationNameEle.appendChild(organizationShortNameEle);

        Element organizationLongNameEle = dif10Doc.createElementNS(DIF10_NS, "Long_Name");
        organizationLongNameEle.setTextContent("ESRIN Earth Observation, European Space Agency");
        organizationNameEle.appendChild(organizationLongNameEle);

        String url = XPathUtils.getNodeValue(responsibleParty,
                ".//gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
        if (StringUtils.isEmpty(url)) {
            url = XPathUtils.getNodeValue(isoDoc,
                    ".//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty//gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
        }

        if (StringUtils.isEmpty(url)) {
            url = "Not provided";
        }

        Element organizationUrlEle = dif10Doc.createElementNS(DIF10_NS, "Organization_URL");
        organizationUrlEle.setTextContent(url);
        organizationEle.appendChild(organizationUrlEle);

        if (StringUtils.isNotEmpty(orgShortName)) {
            orgShortName = StringUtils.trimToEmpty(orgShortName);
            Organisation org = config.getOrganisation(orgShortName);
            if (org != null) {
                organizationLongNameEle.setTextContent(org.getLongName());
                String shortName = org.getShortName();
                if (StringUtils.isEmpty(shortName)) {
                    shortName = StringUtils.EMPTY;
                }
                organizationShortNameEle.setTextContent(shortName);

                if (StringUtils.isEmpty(org.getDataCenterUrl())) {
                    // url = "Not provided";
                    organizationEle.removeChild(organizationUrlEle);
                }
                // orgUrl.setTextContent(org.getDataCenterUrl());
            }
        }

        Element personnelEle = dif10Doc.createElementNS(DIF10_NS, "Personnel");
        organizationEle.appendChild(personnelEle);
        Element roleEle = dif10Doc.createElementNS(DIF10_NS, "Role");
        roleEle.setTextContent("DATA CENTER CONTACT");
        personnelEle.appendChild(roleEle);

        String positionName = XPathUtils.getNodeValue(responsibleParty,
                ".//gmd:positionName/gco:CharacterString");
        String email = XPathUtils.getNodeValue(responsibleParty,
                ".//gmd:contactInfo/gmd:CI_Contact//gmd:electronicMailAddress/gco:CharacterString");
        Element contactGroupEle = dif10Doc.createElementNS(DIF10_NS, "Contact_Group");
        personnelEle.appendChild(contactGroupEle);

        if (StringUtils.isNotEmpty(positionName)) {
            Element nameEle = dif10Doc.createElementNS(DIF10_NS, "Name");
            nameEle.setTextContent(positionName);
            contactGroupEle.appendChild(nameEle);
        }

        if (StringUtils.isNotEmpty(email)) {
            Element emailEle = dif10Doc.createElementNS(DIF10_NS, "Email");
            emailEle.setTextContent(email);
            contactGroupEle.appendChild(emailEle);
        }

        return organizationEle;

    }

    private Element createDif10RelatedURL(Element onlineResource, Document dif10Doc) {

        Element relatedURLEle = dif10Doc.createElementNS(DIF10_NS, "Related_URL");
        Element urlContentTypeEle = dif10Doc.createElementNS(DIF10_NS, "URL_Content_Type");
        relatedURLEle.appendChild(urlContentTypeEle);

        Element typeEle = dif10Doc.createElementNS(DIF10_NS, "Type");
        typeEle.setTextContent("GET DATA");
        urlContentTypeEle.appendChild(typeEle);

        Node urlNode = XPathUtils.getNode(onlineResource, "gmd:linkage/gmd:URL");
        Node applicationProfile = XPathUtils.getNode(onlineResource,
                "gmd:applicationProfile/gco:CharacterString");
        Node name = XPathUtils.getNode(onlineResource, "gmd:name/gco:CharacterString");
        Node description = XPathUtils.getNode(onlineResource, "gmd:description/gco:CharacterString");

        String url = urlNode.getTextContent();
        String type = StringUtils.EMPTY;

        if (url.endsWith(".pdf")) {
            type = Constants.APPLICATION_PDF_MIME_TYPE;
        } else if ((url.endsWith(".docx")) || (url.endsWith(".doc"))) {
            type = Constants.APPLICATION_WORD_MIME_TYPE;
        } else if (url.endsWith(".zip")) {
            type = Constants.APPLICATION_ZIP_MIME_TYPE;
        } else if (applicationProfile != null) {
            String applicationProfileValue = applicationProfile.getTextContent();
            switch (applicationProfileValue) {
                case "PDF":
                    type = Constants.APPLICATION_PDF_MIME_TYPE;
                    break;
                case "WORD":
                    type = Constants.APPLICATION_WORD_MIME_TYPE;
                    break;
                default:
                    type = Constants.TEXT_HTML_MIME_TYPE;
                    break;
            }
        } else {
            type = Constants.TEXT_HTML_MIME_TYPE;
        }

        String nameValue = StringUtils.EMPTY;
        String desc = StringUtils.EMPTY;

        if (name != null) {
            nameValue = name.getTextContent();
        }
        if (description != null) {
            desc = description.getTextContent();
        }

        Element urlEle = dif10Doc.createElementNS(DIF10_NS, "URL");
        relatedURLEle.appendChild(urlEle);
        urlEle.setTextContent(url);

        Element descriptionEle = dif10Doc.createElementNS(DIF10_NS, "Description");
        relatedURLEle.appendChild(descriptionEle);
        descriptionEle.setTextContent("DescribedBy");

        Element mimeTypeEle = dif10Doc.createElementNS(DIF10_NS, "Mime_Type");
        relatedURLEle.appendChild(mimeTypeEle);
        mimeTypeEle.setTextContent(type);

        return relatedURLEle;

    }

    private Element createDif10MetadataDates(Document isoDoc, Document dif10Doc) {
        Element metadataDatesEle = dif10Doc.createElementNS(DIF10_NS, "Metadata_Dates");

        String metadataCreation = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode='creation' or gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date");
        if (StringUtils.isEmpty(metadataCreation)) {
            metadataCreation = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode='creation' or gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:DateTime");
        }
        if (StringUtils.isEmpty(metadataCreation)) {
            metadataCreation = "unknown";
        }

        Element metaDataCreationEle = dif10Doc.createElementNS(DIF10_NS, "Metadata_Creation");
        metaDataCreationEle.setTextContent(metadataCreation);
        metadataDatesEle.appendChild(metaDataCreationEle);

        String metadataLastRevision = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode='creation' or gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date");
        if (StringUtils.isEmpty(metadataLastRevision)) {
            metadataLastRevision = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode='creation' or gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:DateTime");
        }
        if (StringUtils.isEmpty(metadataLastRevision)) {
            metadataLastRevision = "unknown";
        }

        Element metadataLastRevisionEle = dif10Doc.createElementNS(DIF10_NS, "Metadata_Last_Revision");
        metadataLastRevisionEle.setTextContent(metadataLastRevision);
        metadataDatesEle.appendChild(metadataLastRevisionEle);

        Element dataCreationEle = dif10Doc.createElementNS(DIF10_NS, "Data_Creation");
        dataCreationEle.setTextContent("unknown");
        metadataDatesEle.appendChild(dataCreationEle);
        Element dataLastRevisionEle = dif10Doc.createElementNS(DIF10_NS, "Data_Last_Revision");
        dataLastRevisionEle.setTextContent("unknown");
        metadataDatesEle.appendChild(dataLastRevisionEle);
        return metadataDatesEle;
    }

    private Element createDif10ExtendedMetadata(String type, String name, Element mDKeyword, Document dif10Doc) {

        //System.out.println("++++++++++++AAAAAAAAAAAAA++++++++++++++++++++++");

        Element extendedMetadataEle = dif10Doc.createElementNS(DIF10_NS, "Extended_Metadata");
        Element metadataEle = dif10Doc.createElementNS(DIF10_NS, "Metadata");
        extendedMetadataEle.appendChild(metadataEle);

        Element groupEle = dif10Doc.createElementNS(DIF10_NS, "Group");
        metadataEle.appendChild(groupEle);
        groupEle.setTextContent("int.esa.fedeo");

        Element nameEle = dif10Doc.createElementNS(DIF10_NS, "Name");
        metadataEle.appendChild(nameEle);
        nameEle.setTextContent(name);

        if (type != null && StringUtils.isNotEmpty(type)) {
            Element typeEle = dif10Doc.createElementNS(DIF10_NS, "Type");
            metadataEle.appendChild(typeEle);
            typeEle.setTextContent(type);
        }

        List<String> keywordValues = XPathUtils.getNodesValues(mDKeyword, ".//gmd:keyword/gmx:Anchor");

        for (String keyword : keywordValues) {
            if (StringUtils.isNotEmpty(keyword)) {
                Element valueEle = dif10Doc.createElementNS(DIF10_NS, "Value");
                metadataEle.appendChild(valueEle);
                valueEle.setTextContent(keyword);

            }
        }

        keywordValues = XPathUtils.getNodesValues(mDKeyword, ".//gmd:keyword/gco:CharacterString");

        for (String keyword : keywordValues) {
            if (StringUtils.isNotEmpty(keyword)) {
                Element valueEle = dif10Doc.createElementNS(DIF10_NS, "Value");
                metadataEle.appendChild(valueEle);
                valueEle.setTextContent(keyword);
            }
        }

        return extendedMetadataEle;
    }

}
