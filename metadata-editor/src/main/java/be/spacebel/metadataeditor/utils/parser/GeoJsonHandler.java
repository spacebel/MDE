/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.business.ValidationException;
import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.configuration.Offering;
import be.spacebel.metadataeditor.models.configuration.OfferingContent;
import be.spacebel.metadataeditor.models.configuration.OfferingOperation;
import be.spacebel.metadataeditor.models.configuration.Thesaurus;
import be.spacebel.metadataeditor.models.configuration.VoidDataset;
import be.spacebel.metadataeditor.models.workspace.Acquisition;
import be.spacebel.metadataeditor.models.workspace.Contact;
import be.spacebel.metadataeditor.models.workspace.Metadata;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.models.workspace.distribution.OnlineResource;
import be.spacebel.metadataeditor.models.workspace.distribution.TransferOption;
import be.spacebel.metadataeditor.models.workspace.identification.Constraints;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.models.workspace.identification.FreeKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.GmxAnchor;
import be.spacebel.metadataeditor.models.workspace.identification.Keyword;
import be.spacebel.metadataeditor.models.workspace.identification.ThesaurusKeyword;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.validation.AutoCorrectionWarning;
import be.spacebel.metadataeditor.utils.validation.ValidationError;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import javax.faces.application.FacesMessage;
import org.apache.commons.io.FileUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implements methods that are used to handle GeoJson (OGC 17-084)
 * metadata
 *
 * @author mng
 */
public class GeoJsonHandler {

    private static final String JSON_V4_SCHEMA_IDENTIFIER = "http://json-schema.org/draft-04/schema#";
    private static final String JSON_SCHEMA_IDENTIFIER_ELEMENT = "$schema";

    private final Logger LOG = Logger.getLogger(getClass());
    private final XMLParser xmlParser;
    private final Configuration config;

    public GeoJsonHandler(Configuration config) {
        this.config = config;
        xmlParser = new XMLParser();
        xmlParser.setIsNamespaceAware(true);
    }

//    /**
//     * Transform GeoJson metadata to Internal Metadata Model
//     *
//     * @param geoJsonMetadata GeoJson metadata
//     * @return an Internal Metadata Model
//     * @throws java.io.IOException
//     * @throws org.xml.sax.SAXException
//     * @throws java.text.ParseException
//     */
//    public Metadata toIso(String geoJsonMetadata) throws IOException, SAXException, ParseException {
//        JSONObject metadataObj = new JSONObject(geoJsonMetadata);
//
//        JSONObject properties = getGeoJSONObjectProperty(metadataObj, "properties");
//        String identifier = getGeoJSONStringProperty(properties, "identifier");
//
//        Document isoDoc = xmlParser.fileToDom(config.getEmptyMdTemplateFile());
//        Element root = isoDoc.getDocumentElement();
//
//        /* Create gmd:fileIdentifier element */
//        Element fileId = isoDoc.createElementNS(Constants.GMD_NS, "gmd:fileIdentifier");
//        Element fileIdValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
//        fileIdValue.setTextContent(identifier);
//        fileId.appendChild(fileIdValue);
//        root.appendChild(fileId);
//
//        /* Create gmd:language element */
//        Element language = isoDoc.createElementNS(Constants.GMD_NS, "gmd:language");
//        Element languageValue = isoDoc.createElementNS(Constants.GMD_NS, "gmd:LanguageCode");
//        languageValue.setTextContent("eng");
//        languageValue.setAttribute("codeList", Constants.STANDARDS_ISO_ORG_CODELIST_BASE + "LanguageCode");
//        languageValue.setAttribute("codeListValue", "eng");
//        language.appendChild(languageValue);
//        root.appendChild(createlanguageElement(isoDoc));
//
//        /* Create gmd:hierarchyLevel element */
//        Element hierarchyLevel = isoDoc.createElementNS(Constants.GMD_NS, "gmd:hierarchyLevel");
//        Element hierarchyLevelValue = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_ScopeCode");
//        hierarchyLevelValue.setTextContent("series");
//        hierarchyLevelValue.setAttribute("codeList", Constants.STANDARDS_ISO_ORG_CODELIST_BASE + "MD_ScopeCode");
//        hierarchyLevelValue.setAttribute("codeListValue", "series");
//
//        hierarchyLevel.appendChild(hierarchyLevelValue);
//        root.appendChild(hierarchyLevel);
//
//        /* Create gmd:contact element */
//        Element contactEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:contact");
//        root.appendChild(contactEle);
//        JSONArray contactPoints = getGeoJSONArrayProperty(properties, "contactPoint");
//        if (contactPoints != null && contactPoints.length() > 0) {
//            JSONObject contactPoint = contactPoints.getJSONObject(0);
//            Element resParty = createCI_ResponsibleParty(isoDoc, contactPoint, "pointOfContact");
//            if (resParty != null) {
//                contactEle.appendChild(resParty);
//            }
//        }
//
//        /* Create gmd:dateStamp element */
//        Element dateStampEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:dateStamp");
//        root.appendChild(dateStampEle);
//
//        /* Create metadataStandard elements */
//        appendMetadataStandards(isoDoc, root, properties);
//
//        /* Create gmd:identificationInfo elements */
//        Element identificationInfoEle = createIdentificationInfo(isoDoc, metadataObj);
//        root.appendChild(identificationInfoEle);
//
//        JSONArray acqInfos = getGeoJSONArrayProperty(properties, "acquisitionInformation");
//        if (acqInfos != null) {
//            if (acqInfos.length() > 0) {
//                Element acqInformationEle = createIsoAcquisitionInformation(isoDoc, acqInfos);
//                root.appendChild(acqInformationEle);
//            }
//        }
//
//        Metadata series = metadataParser.buildSeries(isoDoc);
//        /**
//         * transform offerings from GeoJson to Metadata
//         */
//        JSONArray offerings = getGeoJSONArrayProperty(properties, "offerings");
//        if (offerings != null && offerings.length() > 0) {
//            for (int i = 0; i < offerings.length(); i++) {
//                JSONObject offObj = offerings.getJSONObject(i);
//                String offCode = getGeoJSONStringProperty(offObj, "code");
//                if (StringUtils.isNotEmpty(offCode)) {
//                    Offering offering = config.getOffering(offCode);
//                    offering.setCode(offCode);
//                    series.addOffering(offering);
//
//                    JSONArray operations = getGeoJSONArrayProperty(offObj, "operations");
//                    if (operations != null && operations.length() > 0) {
//                        for (int operIdx = 0; operIdx < operations.length(); operIdx++) {
//                            JSONObject operObj = operations.getJSONObject(operIdx);
//                            String operCode = getGeoJSONStringProperty(operObj, "code");
//                            if (StringUtils.isNotEmpty(operCode)) {
//                                OfferingOperation operation = offering.getAvailableOperation(operCode);
//                                operation.setCode(operCode);
//
//                                operation.setMethod(getGeoJSONStringProperty(operObj, "method"));
//                                operation.setMimeType(getGeoJSONStringProperty(operObj, "type"));
//
//                                if (operation.getRequiredExtFields() != null) {
//                                    operation.getRequiredExtFields().forEach((option) -> {
//                                        option.setValue(getGeoJSONStringProperty(operObj, option.getLabel()));
//                                    });
//                                }
//                                if (operation.getOptionalExtFields() != null) {
//                                    operation.getOptionalExtFields().forEach((option) -> {
//                                        option.setValue(getGeoJSONStringProperty(operObj, option.getLabel()));
//                                    });
//                                }
//                                offering.addOperation(operation);
//                            }
//                        }
//                    }
//
//                    JSONArray contents = getGeoJSONArrayProperty(offObj, "contents");
//                    if (contents != null && contents.length() > 0) {
//                        for (int contentIdx = 0; contentIdx < contents.length(); contentIdx++) {
//                            JSONObject contentObj = contents.getJSONObject(contentIdx);
//                            String key = String.format("%03d", (contentIdx + 1));
//
//                            OfferingContent content = offering.getAvailableContent(key);
//                            content.setMimeType(getGeoJSONStringProperty(contentObj, "type"));
//
//                            if (content.getRequiredExtFields() != null) {
//                                content.getRequiredExtFields().forEach((option) -> {
//                                    option.setValue(getGeoJSONStringProperty(contentObj, option.getLabel()));
//                                });
//                            }
//                            if (content.getOptionalExtFields() != null) {
//                                content.getOptionalExtFields().forEach((option) -> {
//                                    option.setValue(getGeoJSONStringProperty(contentObj, option.getLabel()));
//                                });
//                            }
//
//                            offering.addContent(content);
//                        }
//                    }
//                }
//            }
//        }
//
//        return series;
//    }
    /**
     * Transform ISO-19139 XML metadata to GeoJson metadata
     *
     * @param seriesDoc
     * @return a JSONObject representing GeoJson metadata
     */
    public JSONObject toGeoJSON(Document seriesDoc) {

        JSONObject feature = new JSONObject();

        Node isoDoc = XPathUtils.getNode(seriesDoc, "./gmi:MI_Metadata");
        if (isoDoc == null) {
            isoDoc = XPathUtils.getNode(seriesDoc, "./gmd:MD_Metadata");
        }

        if (isoDoc == null) {
            return feature;
        }

        String identifier = XPathUtils.getNodeValue(isoDoc, "./gmd:fileIdentifier/gco:CharacterString");

        feature.put("id", identifier);
        feature.put("type", "Feature");

        String west = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*//gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
        String east = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*//gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
        String south = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*//gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
        String north = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*//gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
        if (StringUtils.isNotEmpty(west) && StringUtils.isNotEmpty(east) && StringUtils.isNotEmpty(south)
                && StringUtils.isNotEmpty(north)) {

            // String coordinates = "[ [ [" + west + "," + south + "], [ " + east + "," +
            // south + " ], [ " + east + "," + north +" ], [ " + west + "," + north +" ], [
            // " + west + "," + south +" ] ] ]";
            JSONArray point1 = new JSONArray();
            point1.put(Double.parseDouble(west));
            point1.put(Double.parseDouble(south));

            JSONArray point2 = new JSONArray();
            point2.put(Double.parseDouble(east));
            point2.put(Double.parseDouble(south));

            JSONArray point3 = new JSONArray();
            point3.put(Double.parseDouble(east));
            point3.put(Double.parseDouble(north));

            JSONArray point4 = new JSONArray();
            point4.put(Double.parseDouble(west));
            point4.put(Double.parseDouble(north));

            JSONArray point5 = new JSONArray();
            point5.put(Double.parseDouble(west));
            point5.put(Double.parseDouble(south));

            JSONArray coordinates = new JSONArray();
            coordinates.put(point1);
            coordinates.put(point2);
            coordinates.put(point3);
            coordinates.put(point4);
            coordinates.put(point5);
            JSONObject geometry = new JSONObject();
            geometry.put("type", "Polygon");
            geometry.put("coordinates", new JSONArray().put(coordinates));
            feature.put("geometry", geometry);
        } else {
            feature.put("geometry", JSONObject.NULL);
        }

        JSONObject properties = new JSONObject();

        String type = XPathUtils.getNodeValue(isoDoc, "..//gmd:hierarchyLevel/gmd:MD_ScopeCode");
        boolean serviceMetadata = false;
        if (type.equals("series")) {
            properties.put("kind", Constants.GEO_JSON_SERIES_TYPE);
        } else {
            properties.put("kind", Constants.GEO_JSON_SERVICE_TYPE);
            serviceMetadata = true;
        }

        String title = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        if (StringUtils.isNotEmpty(title)) {
            properties.put("title", title);
        }

        String edition = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:edition/gco:CharacterString");
        if (StringUtils.isNotEmpty(edition)) {
            properties.put("versionInfo", edition);
        }

        String doi = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier[gmd:description/gco:CharacterString = 'DOI']");
        if (StringUtils.isNotEmpty(doi)) {
            properties.put("doi", doi);
        }

        String otherCitationDetails = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:otherCitationDetails/gco:CharacterString");
        if (StringUtils.isNotEmpty(otherCitationDetails)) {
            properties.put("bibliographicCitation", otherCitationDetails);
        }

        properties.put("identifier", identifier);

        String abstracts = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*/gmd:abstract/gco:CharacterString");
        if (StringUtils.isNotEmpty(abstracts)) {
            properties.put("abstract", abstracts);
        }

        /* get startDate */
        String startDate = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition");

        if (StringUtils.isEmpty(startDate)) {
            startDate = XPathUtils.getNodeValue(isoDoc,
                    "./gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition");
        }

        String dcDate = StringUtils.EMPTY;

        if (StringUtils.isNotEmpty(startDate)) {

            if (!startDate.contains("T")) {
                startDate = startDate + "T00:00:00.000Z";
            } else {
                if (!startDate.endsWith("Z")) {
                    startDate = startDate + "Z";
                }
            }
        }

        /* get endDate */
        String endDate = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition");
        if (StringUtils.isEmpty(endDate)) {
            endDate = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/*/*/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition");
        }

        if (StringUtils.isNotEmpty(endDate)) {
            if (!endDate.contains("T")) {
                endDate = endDate + "T23:59:59.999Z";
            } else {
                if (!endDate.endsWith("Z")) {
                    endDate = endDate + "Z";
                }
            }
        }

        if (StringUtils.isNotEmpty(startDate)) {
            dcDate = startDate;
        }

        if (StringUtils.isNotEmpty(endDate)) {
            if (StringUtils.isNotEmpty(dcDate)) {
                dcDate = dcDate + "/" + endDate;
            } else {
                dcDate = endDate;
            }

        }

        if (StringUtils.isNotEmpty(dcDate)) {
            properties.put("date", dcDate);
        }

        /* update date */
        String updatedDate = XPathUtils.getNodeValue(isoDoc,
                "//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date");

        if (StringUtils.isEmpty(updatedDate)) {
            updatedDate = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date");
        }

        if (StringUtils.isEmpty(updatedDate)) {
            updatedDate = XPathUtils.getNodeValue(isoDoc,
                    "//gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition");
        }

        if (StringUtils.isNotEmpty(updatedDate)) {
            if (!updatedDate.contains("T")) {
                updatedDate = updatedDate + "T00:00:00.000Z";
            } else {
                if (!updatedDate.endsWith("Z")) {
                    updatedDate = updatedDate + "Z";
                }
            }
        }

        if (StringUtils.isNotEmpty(updatedDate)) {
            properties.put("updated", updatedDate);
        }

        /* Add category and kewyord */
        addCategoryAndKeywordAndSubject(isoDoc, properties, serviceMetadata);

        /* Add license and accessRight */
        addLicenseAndRightInfo(isoDoc, properties);

        /* create contactPoint array */
        NodeList responsibleParties = XPathUtils.getNodes(isoDoc,
                "./gmd:contact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue = 'pointOfContact']");
        if (responsibleParties.getLength() == 0) {
            responsibleParties = XPathUtils.getNodes(isoDoc,
                    "./gmd:identificationInfo//gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue = 'pointOfContact']");
        }
        addAgentArray(properties, "contactPoint", responsibleParties);

        /* create authors array */
        responsibleParties = XPathUtils.getNodes(isoDoc,
                "./gmd:contact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue = 'author']");
        if (responsibleParties.getLength() == 0) {
            responsibleParties = XPathUtils.getNodes(isoDoc,
                    "./gmd:identificationInfo//gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue = 'author']");
        }
        addAgentArray(properties, "authors", responsibleParties);

        /* create attribute publisher */
        String publisher = XPathUtils.getNodeValue(isoDoc,
                "./gmd:contact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue = 'publisher']/gmd:organisationName/gco:CharacterString");
        if (StringUtils.isNotEmpty(publisher)) {
            properties.put("publisher", publisher);
        }

        /* create qualifiedAttributions */
        responsibleParties = XPathUtils.getNodes(isoDoc,
                "./gmd:contact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue != 'publisher' and gmd:role/gmd:CI_RoleCode/@codeListValue != 'pointOfContact' and gmd:role/gmd:CI_RoleCode/@codeListValue != 'author']");
        if (responsibleParties.getLength() == 0) {
            responsibleParties = XPathUtils.getNodes(isoDoc,
                    "./gmd:identificationInfo//gmd:pointOfContact/gmd:CI_ResponsibleParty[gmd:role/gmd:CI_RoleCode/@codeListValue != 'publisher' and gmd:role/gmd:CI_RoleCode/@codeListValue != 'pointOfContact' and gmd:role/gmd:CI_RoleCode/@codeListValue != 'author']");
        }

        JSONArray qualifiedAttributions = createQualifiedAttribution(responsibleParties);
        if (qualifiedAttributions.length() > 0) {
            properties.put("qualifiedAttribution", qualifiedAttributions);
        }

        /* Add provenance */
        addProvenance(isoDoc, properties);

        /* Add wasUsedBy */
        addWasUsedBy(isoDoc, properties);

        JSONObject isPrimaryTopicOf = createIsPrimaryTopicOf(isoDoc);
        properties.put("isPrimaryTopicOf", isPrimaryTopicOf);

        /* Add acquisitionInformation */
        addAcquisitionInformation(isoDoc, properties);

        /* Add links */
        JSONObject links = new JSONObject();
        addLinks(links, isoDoc);
        createDefaultProfiles(links, serviceMetadata);
        properties.put("links", links);

        feature.put("properties", properties);

        return feature;
    }

    public void addIso(JSONObject geoJsonObj, String isoXml) {
        /*
            Add whole ISO xml to GeoJson        
         */
        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(geoJsonObj, "properties");
        properties.remove("supplementalInformation");
        properties.put("supplementalInformation", xmlParser
                .removeNewLinesAndXmlDec(isoXml)
                .replaceAll("\\r|\\n", ""));
    }

    public void setGeoJsonSrc(MetadataFile metadataFile) {
        JSONObject geoJsonObj = new JSONObject(metadataFile.getJsonObject().toString());
        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(geoJsonObj, "properties");
        properties.remove("supplementalInformation");
        metadataFile.setGeoJsonSrc(geoJsonObj.toString(2));
    }

    public void updateGeoJson(JSONObject geoJsonObj, Metadata metadata) {

        String identifier = metadata.getOthers().getFileIdentifier();
        geoJsonObj.put("id", identifier);

        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(geoJsonObj, "properties");
        properties.put("identifier", identifier);

        // update bbox
        if (metadata.getIdentification().getBbox() != null) {
            double west = metadata.getIdentification().getBbox().getWest();
            double east = metadata.getIdentification().getBbox().getEast();
            double south = metadata.getIdentification().getBbox().getSouth();
            double north = metadata.getIdentification().getBbox().getNorth();

            JSONArray point1 = new JSONArray();
            point1.put(west);
            point1.put(south);

            JSONArray point2 = new JSONArray();
            point2.put(east);
            point2.put(south);

            JSONArray point3 = new JSONArray();
            point3.put(east);
            point3.put(north);

            JSONArray point4 = new JSONArray();
            point4.put(west);
            point4.put(north);

            JSONArray point5 = new JSONArray();
            point5.put(west);
            point5.put(south);

            JSONArray coordinates = new JSONArray();
            coordinates.put(point1);
            coordinates.put(point2);
            coordinates.put(point3);
            coordinates.put(point4);
            coordinates.put(point5);
            JSONObject geometry = new JSONObject();
            geometry.put("type", "Polygon");
            geometry.put("coordinates", new JSONArray().put(coordinates));
            geoJsonObj.put("geometry", geometry);
        } else {
            geoJsonObj.remove("geometry");
        }

        if (metadata.getIdentification() != null) {
            // update title
            properties.put("title", metadata.getIdentification().getTitle());

            // update abstract
            if (StringUtils.isNotEmpty(metadata.getRichTextAbstract())) {
                JSONObject absObj = new JSONObject();
                if (StringUtils.isNotEmpty(metadata.getIdentification().getPlainTextAbstract())) {
                    absObj.put("text/plain", metadata.getIdentification().getPlainTextAbstract());
                }

                JSONArray absArray = new JSONArray();
                absArray.put(metadata.getRichTextAbstract());
                absObj.put("text/markdown", absArray);

                properties.put("abstract", absObj);
            } else {
                if (StringUtils.isNotEmpty(metadata.getIdentification().getPlainTextAbstract())) {
                    properties.put("abstract", metadata.getIdentification().getPlainTextAbstract());
                } else {
                    properties.remove("abstract");
                }
            }

            if (StringUtils.isNotEmpty(metadata.getIdentification().getDoi())) {
                properties.put("doi", metadata.getIdentification().getDoi());
            } else {
                properties.remove("doi");
            }

            if (StringUtils.isNotEmpty(metadata.getIdentification().getOtherCitationDetails())) {
                properties.put("bibliographicCitation", metadata.getIdentification().getOtherCitationDetails());
            } else {
                properties.remove("bibliographicCitation");
            }

            if (StringUtils.isNotEmpty(metadata.getIdentification().getEdition())) {
                properties.put("versionInfo", metadata.getIdentification().getEdition());
            } else {
                properties.remove("versionInfo");
            }

            // update temporal extent
            String dcDate = StringUtils.EMPTY;
            if (metadata.getIdentification().getTemporal() != null) {
                String startDate = CommonUtils.toDateTimeZoneFullStr(metadata.getIdentification().getTemporal().getStartDate());
                if (StringUtils.isNotEmpty(startDate)) {
                    dcDate = startDate;
                }

                String endDate = CommonUtils.toDateTimeZoneFullStr(metadata.getIdentification().getTemporal().getEndDate());
                if (StringUtils.isNotEmpty(endDate) && endDate.endsWith("T00:00:00.000Z")) {
                    endDate = endDate.replace("T00:00:00.000Z", "T23:59:59.999Z");
                }

                if (StringUtils.isNotEmpty(endDate)) {
                    if (StringUtils.isNotEmpty(dcDate)) {
                        dcDate = dcDate + "/" + endDate;
                    } else {
                        dcDate = endDate;
                    }
                }

                if (StringUtils.isNotEmpty(dcDate)) {
                    properties.put("date", dcDate);
                } else {
                    properties.remove("date");
                }
            } else {
                properties.remove("date");
            }

            // update subject
            if (StringUtils.isNotEmpty(metadata.getIdentification().getTopicCategory())) {
                updateSubject(properties, metadata.getIdentification().getTopicCategory());
            } else {
                properties.remove("subject");
            }

            /* Update license and accessRight */
            updateLicenseAndRightInfo(properties, metadata.getIdentification().getConstraints());
        }

        // update the last update date        
        String lastUpdateDate = metadata.getOthers().getLastUpdateDate();
        if (StringUtils.isNotEmpty(lastUpdateDate)) {
            if (!lastUpdateDate.contains("T")) {
                lastUpdateDate = lastUpdateDate + "T00:00:00.000Z";
            } else {
                if (!lastUpdateDate.endsWith("Z")) {
                    lastUpdateDate = lastUpdateDate + "Z";
                }
            }
            //lastUpdateDate = lastUpdateDate + ".000Z";
        } else {
            lastUpdateDate = CommonUtils.toDateTimeZoneFullStr(new Date());
        }
        properties.put("updated", lastUpdateDate);

        // update categories and keywords
        updateCategoriesAndKeywords(properties, metadata);

        JSONArray offeringsArray = metadata.getOfferingsArray();
        if (offeringsArray != null) {
            properties.put("offerings", offeringsArray);
        } else {
            properties.remove("offerings");
        }

        /* Update contactPoint array */
        List<Contact> pointOfContacts = new ArrayList<>();
        List<Contact> authorContacts = new ArrayList<>();
        List<Contact> qualifiedContacts = new ArrayList<>();

        distinguishContact(metadata.getOthers().getContacts(),
                pointOfContacts, authorContacts, qualifiedContacts);
        distinguishContact(metadata.getIdentification().getPointOfContacts(),
                pointOfContacts, authorContacts, qualifiedContacts);


        /* update contactPoint array */
        if (pointOfContacts.size() > 0) {
            createAgentArray(properties, "contactPoint", pointOfContacts);
        } else {
            properties.remove("contactPoint");
        }

        /* update authors array */
        if (authorContacts.size() > 0) {
            createAgentArray(properties, "authors", authorContacts);
        } else {
            properties.remove("authors");
        }

        /* update attribute publisher */
        boolean noPublisher = true;
        if (metadata.getOthers().getContacts() != null
                && metadata.getOthers().getContacts().size() > 0) {
            for (Contact contact : metadata.getOthers().getContacts()) {
                if (StringUtils.isNotEmpty(contact.getRole())
                        && "publisher".equalsIgnoreCase(contact.getRole())) {
                    if (StringUtils.isNotEmpty(contact.getOrgName())) {
                        properties.put("publisher", contact.getOrgName());
                        noPublisher = false;
                        break;
                    }
                }
            }
        }

        if (noPublisher) {
            properties.remove("publisher");
        }

        /* update qualifiedAttributions */
        if (qualifiedContacts.size() > 0) {
            JSONArray qualifiedAttributions = createQualifiedAttribution(qualifiedContacts);
            if (qualifiedAttributions.length() > 0) {
                properties.put("qualifiedAttribution", qualifiedAttributions);
            } else {
                properties.remove("qualifiedAttribution");
            }
        } else {
            properties.remove("qualifiedAttribution");
        }

        /* Add provenance */
        //addProvenance(isoDoc, properties);

        /* Add wasUsedBy */
        //addWasUsedBy(isoDoc, properties);
        JSONObject isPrimaryTopicOf = createIsPrimaryTopicOf(metadata);
        properties.put("isPrimaryTopicOf", isPrimaryTopicOf);

        /* Add acquisitionInformation */
        if (metadata.getAcquisition() != null) {
            properties.put("acquisitionInformation", createAcquisitionInformation(metadata.getAcquisition()));
        } else {
            properties.remove("acquisitionInformation");
        }


        /* update describedby links */
        JSONObject links = JsonUtils.getGeoJSONObjectProperty(properties, "links");
        if (links == null) {
            links = new JSONObject();
        }
        if (metadata.getDistribution() != null
                && metadata.getDistribution().getTransferOptions() != null) {
            createLinks(links, metadata.getDistribution().getTransferOptions());
        }

        createDefaultProfiles(links, metadata.isService());

        properties.put("links", links);
    }

    private void createDefaultProfiles(JSONObject links, boolean isService) {
        JSONArray profiles = JsonUtils.getGeoJSONArrayProperty(links, "profiles");
        if (profiles == null) {
            profiles = new JSONArray();
            if (isService) {
                for (String pf : config.getServiceProfiles()) {
                    JSONObject pObj = new JSONObject();
                    pObj.put("href", pf);
                    profiles.put(pObj);
                }
            } else {
                for (String pf : config.getCollectionProfiles()) {
                    JSONObject pObj = new JSONObject();
                    pObj.put("href", pf);
                    profiles.put(pObj);
                }
            }
            links.put("profiles", profiles);
        }
    }

//    public JSONObject toGeoJSON(String jsonFile, MetadataFile metadataFile) throws IOException {
//        String json = FileUtils.readFileToString(new File(jsonFile), StandardCharsets.UTF_8.name());
//        JSONObject jsonObject = new JSONObject(json);
//        metadataFile.setJsonObject(jsonObject);
//        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(jsonObject, "properties");
//
//        /*
//            get offerings
//         */
//        JSONArray offeringsArray = JsonUtils.getGeoJSONArrayProperty(properties, "offerings");
//        if (offeringsArray != null && offeringsArray.length() > 0) {
//            List<Offering> offerings = new ArrayList<>();
//            metadataFile.getMetadata().setOfferings(offerings);
//
//            for (int i = 0; i < offeringsArray.length(); i++) {
//                JSONObject offObj = offeringsArray.getJSONObject(i);
//                String offCode = JsonUtils.getGeoJSONStringProperty(offObj, "code");
//                Offering offering;
//
//                if (StringUtils.isNotEmpty(offCode)) {
//                    offering = config.getOffering(offCode);
//                    offering.setCode(offCode);
//                } else {
//                    offering = new Offering();
//                }
//                offerings.add(offering);
//
//                JSONArray operations = JsonUtils.getGeoJSONArrayProperty(offObj, "operations");
//                if (operations != null && operations.length() > 0) {
//                    for (int operIdx = 0; operIdx < operations.length(); operIdx++) {
//                        JSONObject operObj = operations.getJSONObject(operIdx);
//                        String operCode = JsonUtils.getGeoJSONStringProperty(operObj, "code");
//                        OfferingOperation operation;
//                        if (StringUtils.isNotEmpty(operCode)) {
//                            operation = offering.getAvailableOperation(operCode);
//                            operation.setCode(operCode);
//                        } else {
//                            operation = new OfferingOperation();
//                        }
//
//                        operation.setMethod(JsonUtils.getGeoJSONStringProperty(operObj, "method"));
//                        operation.setMimeType(JsonUtils.getGeoJSONStringProperty(operObj, "type"));
//
//                        if (operation.getRequiredExtFields() != null) {
//                            operation.getRequiredExtFields().forEach((option) -> {
//                                option.setValue(JsonUtils.getGeoJSONStringProperty(operObj, option.getLabel()));
//                            });
//                        }
//                        if (operation.getOptionalExtFields() != null) {
//                            operation.getOptionalExtFields().forEach((option) -> {
//                                option.setValue(JsonUtils.getGeoJSONStringProperty(operObj, option.getLabel()));
//                            });
//                        }
//                        offering.addOperation(operation);
//
//                    }
//                }
//
//                JSONArray contents = JsonUtils.getGeoJSONArrayProperty(offObj, "contents");
//                if (contents != null && contents.length() > 0) {
//                    for (int contentIdx = 0; contentIdx < contents.length(); contentIdx++) {
//                        JSONObject contentObj = contents.getJSONObject(contentIdx);
//                        String key = String.format("%03d", (contentIdx + 1));
//
//                        OfferingContent content = offering.getAvailableContent(key);
//                        content.setMimeType(JsonUtils.getGeoJSONStringProperty(contentObj, "type"));
//
//                        if (content.getRequiredExtFields() != null) {
//                            content.getRequiredExtFields().forEach((option) -> {
//                                option.setValue(JsonUtils.getGeoJSONStringProperty(contentObj, option.getLabel()));
//                            });
//                        }
//                        if (content.getOptionalExtFields() != null) {
//                            content.getOptionalExtFields().forEach((option) -> {
//                                option.setValue(JsonUtils.getGeoJSONStringProperty(contentObj, option.getLabel()));
//                            });
//                        }
//
//                        offering.addContent(content);
//                    }
//                }
//                LOG.debug(offering.toXml());
//
//            }
//        }
//
//        /*
//            get abstract text/markdown
//         */
//        JSONObject abstractObj = JsonUtils.getGeoJSONObjectProperty(properties, "abstract");
//        if (abstractObj != null) {
//            JSONArray richTextArray = JsonUtils.getGeoJSONArrayProperty(abstractObj, "text/markdown");
//
//            if (richTextArray != null && richTextArray.length() > 0) {
//                StringBuilder sb = new StringBuilder();
//                richTextArray.iterator().forEachRemaining(element -> {
//                    sb.append(element.toString());
//                });
//                String absRichText = sb.toString();
//                if (StringUtils.isNotEmpty(absRichText)) {
//                    //absRichText = StringEscapeUtils.unescapeJson(absRichText);
//                    absRichText = absRichText.replaceAll("\r\n", "\n");
//                }
//                metadataFile.getMetadata().setRichTextAbstract(absRichText);
//            }
//        }
//
//        return jsonObject;
//    }
    /**
     * get information such as: offerings, abstract (rich text) which is in
     * GeoJson format to Metadata model
     *
     * @param properties
     * @param metadata
     */
    public void collectOfferingsAndAbstract(JSONObject properties, Metadata metadata) {
        LOG.debug("Get information such as: offerings, abstract (rich text)");

        /*
            get offerings
         */
        JSONArray offeringsArray = JsonUtils.getGeoJSONArrayProperty(properties, "offerings");
        if (offeringsArray != null && offeringsArray.length() > 0) {
            List<Offering> offerings = new ArrayList<>();
            metadata.setOfferings(offerings);

            for (int i = 0; i < offeringsArray.length(); i++) {
                JSONObject offObj = offeringsArray.getJSONObject(i);
                String offCode = JsonUtils.getGeoJSONStringProperty(offObj, "code");
                Offering offering;

                if (StringUtils.isNotEmpty(offCode)) {
                    offering = config.getOffering(offCode);
                    offering.setCode(offCode);
                } else {
                    offering = new Offering();
                }
                offerings.add(offering);

                JSONArray operations = JsonUtils.getGeoJSONArrayProperty(offObj, "operations");
                if (operations != null && operations.length() > 0) {
                    for (int operIdx = 0; operIdx < operations.length(); operIdx++) {
                        JSONObject operObj = operations.getJSONObject(operIdx);
                        String operCode = JsonUtils.getGeoJSONStringProperty(operObj, "code");
                        OfferingOperation operation;
                        if (StringUtils.isNotEmpty(operCode)) {
                            operation = offering.getAvailableOperation(operCode);
                            operation.setCode(operCode);
                        } else {
                            operation = new OfferingOperation();
                        }

                        operation.setMethod(JsonUtils.getGeoJSONStringProperty(operObj, "method"));
                        operation.setMimeType(JsonUtils.getGeoJSONStringProperty(operObj, "type"));

                        if (operation.getRequiredExtFields() != null) {
                            operation.getRequiredExtFields().forEach((option) -> {
                                option.setValue(JsonUtils.getGeoJSONStringProperty(operObj, option.getLabel()));
                            });
                        }
                        if (operation.getOptionalExtFields() != null) {
                            operation.getOptionalExtFields().forEach((option) -> {
                                option.setValue(JsonUtils.getGeoJSONStringProperty(operObj, option.getLabel()));
                            });
                        }
                        offering.addOperation(operation);

                    }
                }

                JSONArray contents = JsonUtils.getGeoJSONArrayProperty(offObj, "contents");
                if (contents != null && contents.length() > 0) {
                    for (int contentIdx = 0; contentIdx < contents.length(); contentIdx++) {
                        JSONObject contentObj = contents.getJSONObject(contentIdx);
                        String key = String.format("%03d", (contentIdx + 1));

                        OfferingContent content = offering.getAvailableContent(key);
                        content.setMimeType(JsonUtils.getGeoJSONStringProperty(contentObj, "type"));

                        if (content.getRequiredExtFields() != null) {
                            content.getRequiredExtFields().forEach((option) -> {
                                option.setValue(JsonUtils.getGeoJSONStringProperty(contentObj, option.getLabel()));
                            });
                        }
                        if (content.getOptionalExtFields() != null) {
                            content.getOptionalExtFields().forEach((option) -> {
                                option.setValue(JsonUtils.getGeoJSONStringProperty(contentObj, option.getLabel()));
                            });
                        }

                        offering.addContent(content);
                    }
                }
                LOG.debug(offering.toXml());

            }
        }

        /*
            get abstract text/markdown
         */
        try {
            JSONObject abstractObj = JsonUtils.getGeoJSONObjectProperty(properties, "abstract");
            if (abstractObj != null) {
                JSONArray richTextArray = JsonUtils.getGeoJSONArrayProperty(abstractObj, "text/markdown");

                if (richTextArray != null && richTextArray.length() > 0) {
                    StringBuilder sb = new StringBuilder();
                    richTextArray.iterator().forEachRemaining(element -> {
                        sb.append(element.toString());
                    });
                    String absRichText = sb.toString();
                    if (StringUtils.isNotEmpty(absRichText)) {
                        //absRichText = StringEscapeUtils.unescapeJson(absRichText);
                        absRichText = absRichText.replaceAll("\r\n", "\n");
                    }
                    metadata.setRichTextAbstract(absRichText);
                }
            }
        } catch (JSONException e) {
            LOG.debug("get abstract text/markdown exception " + e);
        }
    }

    public void collectMoreInfoFromJson(JSONObject properties, Metadata metadata) {
        Map<String, String> plfTerms = new HashMap<>();
        Map<String, String> gcmdPlfTerms = new HashMap<>();
        Map<String, String> instTerms = new HashMap<>();
        Map<String, String> gcmdInstTerms = new HashMap<>();
        int recordType = 1;

        /*
         *  get keywords 
         */
        if (metadata.isService()) {
            recordType = 2;
            LOG.debug("MNG 1111");
            JSONArray categories = JsonUtils.getGeoJSONArrayProperty(properties, "categories");
            if (categories != null && categories.length() > 0) {
                LOG.debug("MNG 2222: " + categories.length());
                Map<String, String> eTopicKeywords = new HashMap<>();
                Map<String, String> scienceKeywords = new HashMap<>();
                metadata.getIdentification().setNoMappingScienceKeywords(scienceKeywords);

                for (int i = 0; i < categories.length(); i++) {
                    JSONObject catObj = categories.getJSONObject(i);
                    String scheme = JsonUtils.getGeoJSONStringProperty(catObj, "scheme");
                    String term = JsonUtils.getGeoJSONStringProperty(catObj, "term");
                    String label = JsonUtils.getGeoJSONStringProperty(catObj, "label");

                    LOG.debug("MNG 333: " + scheme);
                    LOG.debug("MNG 333: " + term);
                    LOG.debug("MNG 333: " + label);
                    if (StringUtils.isNotEmpty(scheme)
                            && StringUtils.isNotEmpty(term)
                            && StringUtils.isNotEmpty(label)) {
                        if (scheme.equalsIgnoreCase(config.getEarthtopicsThesaurusUri())) {
                            eTopicKeywords.putIfAbsent(term, label);
                        } else {
                            if (scheme.equalsIgnoreCase(config.getSckwThesaurusUri())
                                    || scheme.equalsIgnoreCase(config.getOldSckwThesaurusUri())) {
                                scienceKeywords.putIfAbsent(term, label);
                            } else {
                                if (scheme.equalsIgnoreCase(config.getEopThesaurus().getTitleUri())
                                        || scheme.equalsIgnoreCase(config.getEopExtThesaurus().getTitleUri())) {
                                    if (term.endsWith(Constants.ORBITTYPE_KEY)) {
                                        addKeyword(metadata.getIdentification().getOrbitType(), term, label);
                                    } else {
                                        if (term.endsWith(Constants.WAVELENGTH_KEY)) {
                                            addKeyword(metadata.getIdentification().getWaveLength(), term, label);
                                        } else {
                                            if (term.endsWith(Constants.PROCESSORVER_KEY)) {
                                                addKeyword(metadata.getIdentification().getProcessorVersion(), term, label);
                                            } else {
                                                if (term.endsWith(Constants.RESOLUTION_KEY)) {
                                                    addKeyword(metadata.getIdentification().getResolution(), term, label);
                                                } else {
                                                    if (term.endsWith(Constants.PRODUCTTYPE_KEY)) {
                                                        addKeyword(metadata.getIdentification().getProductType(), term, label);
                                                    } else {
                                                        if (term.endsWith(Constants.ORBITHEIGHT_KEY)) {
                                                            addKeyword(metadata.getIdentification().getOrbitHeight(), term, label);
                                                        } else {
                                                            if (term.endsWith(Constants.SWATHWIDTH_KEY)) {
                                                                addKeyword(metadata.getIdentification().getSwathWidth(), term, label);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (scheme.equalsIgnoreCase(config.getSpatialDataServiceCategoryThesaurusUri())) {
                                        Keyword kw = new Keyword();
                                        kw.setUri(term);
                                        kw.setLabel(label);
                                        metadata.getIdentification().getSpatialDataServiceCategoryKeywords().addKeyword(kw);
                                    } else {
                                        if (scheme.equalsIgnoreCase(config.getInstrumentThesaurusUri())) {
                                            instTerms.putIfAbsent(term, label);
                                        }
                                        if (scheme.equalsIgnoreCase(config.getGcmdInstrumentThesaurusUri())) {
                                            gcmdInstTerms.putIfAbsent(term, label);
                                        }
                                        if (scheme.equalsIgnoreCase(config.getPlatformThesaurusUri())) {
                                            plfTerms.putIfAbsent(term, label);
                                        }
                                        if (scheme.equalsIgnoreCase(config.getGcmdPlatformThesaurusUri())) {
                                            gcmdPlfTerms.putIfAbsent(term, label);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (StringUtils.isNotEmpty(term) && StringUtils.isNotEmpty(label)) {
                            instTerms.putIfAbsent(term, label);
                        }
                    }
                }

                // handle Earth Topics
                if (!eTopicKeywords.isEmpty()) {
                    List<EarthTopic> earthTopics = new ArrayList<>();
                    metadata.getIdentification().setEarthTopics(earthTopics);

                    eTopicKeywords.entrySet().forEach((entry) -> {
                        earthTopics.add(MetadataUtils.createEarthTopic(entry.getKey(), entry.getValue(),
                                metadata.getOthers().getFileIdentifier(), scienceKeywords, config, metadata.isService() ? 2 : 1));
                    });
                }

            }

            metadata.getIdentification().getProcessorVersion().addEmptyKeywords();
            metadata.getIdentification().getProductType().addEmptyKeywords();
            metadata.getIdentification().getOrbitHeight().addEmptyKeywords();
            metadata.getIdentification().getSwathWidth().addEmptyKeywords();
        }

        /*
         *  get license and accessRights text/markdown
         */
        //System.out.println("MINH MINH MINH");
        if (metadata.getIdentification() != null
                && metadata.getIdentification().getConstraints() != null
                && metadata.getIdentification().getConstraints().size() > 0) {
            //System.out.println("MINH MINH MINH 111111111111");
            try {
                JSONArray licenseArray = JsonUtils.getGeoJSONArrayProperty(properties, "license");
                Map<String, String> licenses = getRichText(licenseArray);

                JSONArray accessRightsArray = JsonUtils.getGeoJSONArrayProperty(properties, "accessRights");
                Map<String, String> accessRights = getRichText(accessRightsArray);

                if (!licenses.isEmpty() || !accessRights.isEmpty()) {
                    metadata.getIdentification().getConstraints().stream().map((mConst) -> {
                        if (!licenses.isEmpty() && mConst.getUseLimitations() != null
                                && mConst.getUseLimitations().size() > 0) {
                            findRichText(mConst.getUseLimitations(), licenses);
                        }
                        return mConst;
                    }).filter((mConst) -> (!accessRights.isEmpty() && mConst.getOthers() != null
                            && mConst.getOthers().size() > 0)).forEachOrdered((mConst) -> {
                        findRichText(mConst.getOthers(), accessRights);
                    });
                }

            } catch (JSONException e) {
                LOG.debug("get license and accessRights text/markdown exception " + e);
            }
        }

        /**
         * Get acquisitionInformation
         */
        if (metadata.isService()) {
            JSONArray acquisitionInformation = JsonUtils
                    .getGeoJSONArrayProperty(properties, "acquisitionInformation");
            if (acquisitionInformation != null
                    && acquisitionInformation.length() > 0) {
                Acquisition acquisition = new Acquisition();
                metadata.setAcquisition(acquisition);

                for (int i = 0; i < acquisitionInformation.length(); i++) {
                    JSONObject jsonObj = acquisitionInformation.getJSONObject(i);
                    JSONObject platformObj = JsonUtils.getGeoJSONObjectProperty(jsonObj, "platform");
                    if (platformObj != null) {
                        String platformUri = JsonUtils
                                .getGeoJSONStringProperty(platformObj, "id");
                        String platformLabel = JsonUtils
                                .getGeoJSONStringProperty(platformObj, "platformShortName");
                        if (StringUtils.isNotEmpty(platformUri)
                                && StringUtils.isNotEmpty(platformLabel)) {
                            Platform platform = new Platform();
                            platform.setUri(platformUri);
                            platform.setLabel(platformLabel);

                            acquisition.addPlatform(platform);

                            JSONObject instObj = JsonUtils.getGeoJSONObjectProperty(jsonObj, "instrument");
                            if (instObj != null) {
                                String instUri = JsonUtils
                                        .getGeoJSONStringProperty(instObj, "id");
                                String instLabel = JsonUtils
                                        .getGeoJSONStringProperty(instObj, "instrumentShortName");
                                if (StringUtils.isNotEmpty(instUri)
                                        && StringUtils.isNotEmpty(instLabel)) {
                                    platform.addInstrument(createInstrument(instUri, instLabel,
                                            metadata.getOthers().getFileIdentifier(), gcmdInstTerms, recordType));
                                }

                            }

                            Concept plfConcept = config.getPlatform(platformUri);
                            if (plfConcept != null) {
                                if (!plfConcept.getLabel().equals(platformLabel)) {
                                    // Platform label changed
                                    platform.setWarning(new AutoCorrectionWarning(metadata.getOthers().getFileIdentifier(),
                                            platformUri, platformLabel, plfConcept.getLabel(), 2,
                                            config.getPlatformThesaurus().getLabel(), 1, recordType));
                                }

                                List<String> exactMatchUris = plfConcept.getProperties().get(Constants.SKOS_EXACTMATCH);
                                if (exactMatchUris != null && !exactMatchUris.isEmpty()) {
                                    for (String uri : exactMatchUris) {
                                        if (gcmdPlfTerms.containsKey(uri)) {
                                            Concept exactMatchConcept = config.getGcmdPlatform(uri);
                                            if (exactMatchConcept != null) {
                                                String gcmdLabel = gcmdPlfTerms.get(uri);
                                                platform.setGcmd(exactMatchConcept);
                                                if (!exactMatchConcept.getLabel().equals(gcmdLabel)) {
                                                    GmxAnchor altTitle = new GmxAnchor();
                                                    altTitle.setLink(uri);
                                                    altTitle.setText(gcmdLabel);
                                                    platform.setAltTitle(altTitle);
                                                    // GCMD Platform label changed
                                                    platform.setGcmdWarning(new AutoCorrectionWarning(metadata.getOthers().getFileIdentifier(),
                                                            uri, gcmdLabel, exactMatchConcept.getLabel(), 5,
                                                            config.getGcmdPlatformThesaurus().getLabel(), 1, recordType));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                List<String> hostsUris = plfConcept.getProperties().get(Constants.SOSA_HOSTS);
                                if (hostsUris != null && !hostsUris.isEmpty()) {
                                    hostsUris.stream().map((uri)
                                            -> config.getInstrument(uri)).filter((hostsConcept)
                                            -> (hostsConcept != null)).forEachOrdered((hostsConcept) -> {

                                        boolean avail = false;
                                        if (platform.getInstruments() != null) {
                                            for (Instrument inst : platform.getInstruments()) {
                                                if (inst.getUri().equals(hostsConcept.getUri())) {
                                                    inst.setHosted(true);
                                                    avail = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (!avail) {
                                            if (instTerms.containsKey(hostsConcept.getUri())) {
                                                String instLabel = instTerms.get(hostsConcept.getUri());
                                                Instrument inst = createInstrument(hostsConcept.getUri(), instLabel,
                                                        metadata.getOthers().getFileIdentifier(), gcmdInstTerms, metadata.isService() ? 2 : 1);
                                                inst.setHosted(true);
                                                platform.addInstrument(inst);
                                            } else {
                                                Instrument instrument = new Instrument();
                                                instrument.setUri(hostsConcept.getUri());
                                                instrument.setLabel(hostsConcept.getLabel());

                                                if (platform.getAvailableInstruments() == null) {
                                                    platform.setAvailableInstruments(new ArrayList<>());
                                                }
                                                platform.getAvailableInstruments().add(instrument);
                                            }
                                        }

                                    });
                                }
                            } else {
                                platform.setEsaPlatform(false);
                            }
                        }
                    }
                }
            }
        }
    }

    private Instrument createInstrument(String instUri, String instLabel,
            String metadataId, Map<String, String> gcmdInstTerms, int recordType) {
        Instrument instrument = new Instrument();
        instrument.setUri(instUri);
        instrument.setLabel(instLabel);
        Concept instrConcept = config.getInstrument(instUri);
        if (instrConcept != null) {
            if (!instrConcept.getLabel().equals(instLabel)) {
                // Instrument label changed
                instrument.setWarning(new AutoCorrectionWarning(metadataId,
                        instUri, instLabel, instrConcept.getLabel(), 3, config.getInstrumentThesaurus().getLabel(), 1, recordType));
            }
            List<String> exactMatchUris = instrConcept.getProperties().get(Constants.SKOS_EXACTMATCH);
            if (exactMatchUris != null && !exactMatchUris.isEmpty()) {
                for (String uri : exactMatchUris) {
                    if (gcmdInstTerms.containsKey(uri)) {
                        Concept exactMatchConcept = config.getGcmdInstrument(uri);
                        if (exactMatchConcept != null) {
                            String gcmdLabel = gcmdInstTerms.get(uri);
                            instrument.setGcmd(exactMatchConcept);
                            if (!exactMatchConcept.getLabel().equals(gcmdLabel)) {
                                GmxAnchor altTitle = new GmxAnchor();
                                altTitle.setLink(uri);
                                altTitle.setText(gcmdLabel);
                                instrument.setAltTitle(altTitle);
                                // GCMD Instrument label changed
                                instrument.setGcmdWarning(new AutoCorrectionWarning(metadataId,
                                        uri, gcmdLabel, exactMatchConcept.getLabel(), 6,
                                        config.getGcmdInstrumentThesaurus().getLabel(), 1, recordType));
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            instrument.setEsaInstrument(false);
        }

        return instrument;
    }

    private void addKeyword(ThesaurusKeyword theKw, String term, String label) {
        Keyword kw = new Keyword();
        kw.setUri(term);
        kw.setLabel(label);
        theKw.addKeyword(kw);
    }

    private Map<String, String> getRichText(JSONArray jsonArray) {
        Map<String, String> richTextValues = new HashMap<>();
        if (jsonArray != null && jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                JSONObject labelObj = JsonUtils.getGeoJSONObjectProperty(obj, "label");
                if (labelObj != null) {
                    //String richText = JsonUtils.getGeoJSONStringProperty(labelObj, "text/markdown");
                    JSONArray richTextArray = JsonUtils.getGeoJSONArrayProperty(labelObj, "text/markdown");
                    if (richTextArray != null && richTextArray.length() > 0) {
                        StringBuilder sb = new StringBuilder();
                        richTextArray.iterator().forEachRemaining(element -> {
                            sb.append(element.toString());
                        });
                        String richText = sb.toString();
                        if (StringUtils.isNotEmpty(richText)) {
                            //absRichText = StringEscapeUtils.unescapeJson(absRichText);
                            richText = richText.replaceAll("\r\n", "\n");
                        }
                        if (StringUtils.isNotEmpty(richText)) {
                            String plainText = JsonUtils.getGeoJSONStringProperty(labelObj, "text/plain");
                            if (StringUtils.isNotEmpty(plainText)) {
                                richTextValues.putIfAbsent(plainText, richText);
                            }
                        }
                    }
                }

            }
        }
        return richTextValues;
    }

    private void findRichText(List<GmxAnchor> limits, Map<String, String> richTextValues) {
        //System.out.println("MINH MINH MINH 3333333");
        limits.forEach((ul) -> {
            String plainText = ul.getText() != null ? ul.getText() : "";
            if (StringUtils.isNotEmpty(ul.getLink())) {
                plainText = plainText + " " + ul.getLink();
            }
            plainText = StringUtils.trimToEmpty(plainText);
            if (richTextValues.containsKey(plainText)) {
                String richText = richTextValues.get(plainText);
//                if (StringUtils.isNotEmpty(richText)) {
//                    richText = richText.replaceAll("\r\n", "\n");
//                }
                ul.setRichText(richText);
            }
        });
    }

    /**
     * Extract supplemental information such as: offerings, abstract
     * (text/markdown),... from the given GeoJson metadata
     *
     * @param geoJsonMetadata
     * @return A String representing a JSON object containing supplemental
     * information
     */
    public String getSupplementalInfo(String geoJsonMetadata) {
        JSONObject mdObject = new JSONObject(geoJsonMetadata);
        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(mdObject, "properties");

        JSONObject jsonObj = new JSONObject();

        boolean hasInfo = false;
        JSONArray offeringsArray = JsonUtils.getGeoJSONArrayProperty(properties, "offerings");
        if (offeringsArray != null
                && offeringsArray.length() > 0) {
            jsonObj.put("offerings", offeringsArray);
            hasInfo = true;
        }

        JSONObject abstractObj = JsonUtils.getGeoJSONObjectProperty(properties, "abstract");
        if (abstractObj != null) {
            JSONArray textMarkdownArray = JsonUtils.getGeoJSONArrayProperty(abstractObj, "text/markdown");
            if (textMarkdownArray != null
                    && textMarkdownArray.length() > 0) {
                JSONObject absObj = new JSONObject();
                absObj.put("text/markdown", textMarkdownArray);
                jsonObj.put("abstract", absObj);
                hasInfo = true;
            }
        }

        if (hasInfo) {
            return jsonObj.toString(2);
        }

        return "";
    }

    public void validate(String jsonData, boolean isService) throws IOException, JSONException, ValidationException {

        JSONObject jsonSchema;
        JSONObject jsonObject;
        String schemaLocation;
        int recordType;
        if (isService) {
            schemaLocation = config.getJsonServiceSchemaLocation();
            recordType = 2;
        } else {
            schemaLocation = config.getJsonSeriesSchemaLocation();
            recordType = 1;
        }

        try {
            try (FileInputStream fis = new FileInputStream(schemaLocation)) {
                jsonSchema = new JSONObject(new JSONTokener(fis));
                jsonObject = new JSONObject(new JSONTokener(jsonData));
            }
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonObject);
        } catch (org.everit.json.schema.ValidationException e) {
            if (e.getAllMessages() != null) {
                ValidationException vEx = new ValidationException();
                vEx.setValidationErrors(new ArrayList<>());
                e.getAllMessages().forEach((msg) -> {
                    vEx.getValidationErrors().add(new ValidationError(FacesMessage.SEVERITY_ERROR, msg, 2, recordType));
                });
                throw vEx;
            }
        }
    }

    private Element createlanguageElement(Document isoDoc) {
        Element language = isoDoc.createElementNS(Constants.GMD_NS, "gmd:language");
        Element languageValue = isoDoc.createElementNS(Constants.GMD_NS, "gmd:LanguageCode");
        languageValue.setTextContent("eng");
        languageValue.setAttribute("codeList", Constants.STANDARDS_ISO_ORG_CODELIST_BASE + "LanguageCode");
        languageValue.setAttribute("codeListValue", "eng");
        language.appendChild(languageValue);

        return language;
    }

    private Element createCI_ResponsibleParty(Document isoDoc, JSONObject contactObject, String role) {

        Element resParty = null;
        String name = JsonUtils.getGeoJSONStringProperty(contactObject, "name");
        if (StringUtils.isNotEmpty(name)) {
            resParty = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_ResponsibleParty");
            Element organisationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:organisationName");
            Element organisationValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
            organisationValue.setTextContent(name);
            organisationEle.appendChild(organisationValue);
            resParty.appendChild(organisationEle);

            String phone = JsonUtils.getGeoJSONStringProperty(contactObject, "phone");
            String uri = JsonUtils.getGeoJSONStringProperty(contactObject, "uri");
            String email = JsonUtils.getGeoJSONStringProperty(contactObject, "email");

            JSONObject hasAddress = JsonUtils.getGeoJSONObjectProperty(contactObject, "hasAddress");

            if (hasAddress != null || StringUtils.isNotEmpty(phone) || StringUtils.isNotEmpty(uri)) {
                Element contactInfoEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:contactInfo");
                resParty.appendChild(contactInfoEle);
                Element ciContactEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Contact");
                contactInfoEle.appendChild(ciContactEle);

                if (StringUtils.isNotEmpty(phone)) {
                    Element phoneEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:phone");
                    ciContactEle.appendChild(phoneEle);
                    Element ciTelephoneEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Telephone");
                    phoneEle.appendChild(ciTelephoneEle);
                    Element voiceEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:voice");
                    ciTelephoneEle.appendChild(voiceEle);
                    Element voiceValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                    voiceEle.appendChild(voiceValue);
                    voiceValue.setTextContent(phone);

                }

                if (hasAddress != null) {
                    Element addressEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:address");
                    ciContactEle.appendChild(addressEle);
                    Element ciAddressEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Address");
                    addressEle.appendChild(ciAddressEle);

                    String streetAddress = JsonUtils.getGeoJSONStringProperty(hasAddress, "street-address");
                    String locality = JsonUtils.getGeoJSONStringProperty(hasAddress, "locality");
                    String postalCode = JsonUtils.getGeoJSONStringProperty(hasAddress, "postal-code");
                    String countryName = JsonUtils.getGeoJSONStringProperty(hasAddress, "country-name");

                    if (StringUtils.isNotEmpty(streetAddress)) {
                        Element deliveryPointEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:deliveryPoint");
                        ciAddressEle.appendChild(deliveryPointEle);
                        Element deliveryPointValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                        deliveryPointEle.appendChild(deliveryPointValue);
                        deliveryPointValue.setTextContent(streetAddress);

                    }

                    if (StringUtils.isNotEmpty(locality)) {
                        Element cityEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:city");
                        ciAddressEle.appendChild(cityEle);
                        Element cityValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                        cityEle.appendChild(cityValue);
                        cityValue.setTextContent(locality);

                    }

                    if (StringUtils.isNotEmpty(postalCode)) {
                        Element postalCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:postalCode");
                        ciAddressEle.appendChild(postalCodeEle);
                        Element postalCodeValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                        postalCodeEle.appendChild(postalCodeValue);
                        postalCodeValue.setTextContent(postalCode);

                    }

                    if (StringUtils.isNotEmpty(postalCode)) {
                        Element countryEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:country");
                        ciAddressEle.appendChild(countryEle);
                        Element countryValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                        countryEle.appendChild(countryValue);
                        countryValue.setTextContent(countryName);

                    }

                    if (StringUtils.isNotEmpty(email)) {
                        Element emailEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:electronicMailAddress");
                        ciAddressEle.appendChild(emailEle);
                        Element emailValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                        emailEle.appendChild(emailValue);
                        emailValue.setTextContent(email);

                    }

                }

                if (StringUtils.isNotEmpty(uri)) {
                    Element onlineResourceEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:onlineResource");
                    ciContactEle.appendChild(onlineResourceEle);
                    Element ciOnlineResourceEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_OnlineResource");
                    onlineResourceEle.appendChild(ciOnlineResourceEle);
                    Element linkageEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:linkage");
                    ciOnlineResourceEle.appendChild(linkageEle);
                    Element urlEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:URL");
                    linkageEle.appendChild(urlEle);
                    urlEle.setTextContent(uri);

                }

            }

            if (role != null && StringUtils.isNotEmpty(role)) {
                Element roleEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:role");
                resParty.appendChild(roleEle);
                Element ciRoleCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_RoleCode");
                roleEle.appendChild(ciRoleCodeEle);
                ciRoleCodeEle.setAttribute("codeList", Constants.STANDARDS_ISO_ORG_CODELIST_BASE + "CI_RoleCode");
                ciRoleCodeEle.setAttribute("codeListValue", role);
                ciRoleCodeEle.setTextContent(role);
            }
        }

        return resParty;
    }

    private void appendMetadataStandards(Document isoDoc, Element root, JSONObject properties) {

        Element mdStandardNameEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:metadataStandardName");
        root.appendChild(mdStandardNameEle);
        Element mdStandardNameValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
        mdStandardNameEle.appendChild(mdStandardNameValue);
        mdStandardNameValue.setTextContent("ISO19115");

        Element mdStandardVersionEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:metadataStandardVersion");
        root.appendChild(mdStandardVersionEle);
        Element mdStandardVersionValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
        mdStandardVersionEle.appendChild(mdStandardVersionValue);
        mdStandardVersionValue.setTextContent("2005/Cor.1:2006");

        JSONObject isPrimaryTopicOf = JsonUtils.getGeoJSONObjectProperty(properties, "isPrimaryTopicOf");
        if (isPrimaryTopicOf != null) {
            JSONObject conformsTo = JsonUtils.getGeoJSONObjectProperty(isPrimaryTopicOf, "conformsTo");
            if (conformsTo != null) {
                String versionInfo = JsonUtils.getGeoJSONStringProperty(conformsTo, "versionInfo");
                if (StringUtils.isNotEmpty(versionInfo)) {
                    mdStandardVersionValue.setTextContent(versionInfo);
                }
                String title = JsonUtils.getGeoJSONStringProperty(conformsTo, "title");
                if (StringUtils.isNotEmpty(title)) {
                    mdStandardNameValue.setTextContent(title);
                }

            }
        }
    }

    private Element createIdentificationInfo(Document isoDoc, JSONObject metadataObj) {

        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(metadataObj, "properties");

        Element identificationInfoEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:identificationInfo");
        Element mdDataIdentificationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_DataIdentification");
        identificationInfoEle.appendChild(mdDataIdentificationEle);

        Element citationEle = createCitation(isoDoc, properties);
        mdDataIdentificationEle.appendChild(citationEle);

        /* create gmd:abstract element */
        Element abstractEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:abstract");
        mdDataIdentificationEle.appendChild(abstractEle);
        Element abstractValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
        abstractEle.appendChild(abstractValue);
        String abstractSt = JsonUtils.getGeoJSONStringProperty(properties, "abstract");
        String identifier = JsonUtils.getGeoJSONStringProperty(properties, "identifier");
        String title = JsonUtils.getGeoJSONStringProperty(properties, "title");

        if (StringUtils.isNotEmpty(abstractSt)) {
            abstractValue.setTextContent(abstractSt);
        } else if (StringUtils.isNotEmpty(title)) {
            abstractValue.setTextContent(title);
        } else {
            abstractValue.setTextContent(identifier);
        }

        /* create gmd:pointOfContact element */
        Element pointOfContactEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:pointOfContact");
        mdDataIdentificationEle.appendChild(pointOfContactEle);
        JSONArray qualifiedAttributions = JsonUtils.getGeoJSONArrayProperty(properties, "qualifiedAttribution");
        if (qualifiedAttributions != null && qualifiedAttributions.length() > 0) {
            JSONObject qualifiedAttribution = qualifiedAttributions.getJSONObject(0);
            JSONArray agents = JsonUtils.getGeoJSONArrayProperty(qualifiedAttribution, "agent");
            String role = JsonUtils.getGeoJSONStringProperty(qualifiedAttribution, "role");
            if (agents != null && agents.length() > 0) {
                JSONObject agent = agents.getJSONObject(0);
                Element resParty = createCI_ResponsibleParty(isoDoc, agent, role);
                if (resParty != null) {
                    pointOfContactEle.appendChild(resParty);
                }
            }
        }

        appendDescriptiveKeywords(isoDoc, mdDataIdentificationEle, properties);

        /* create resourceConstraints */
        appendResourceConstraints(isoDoc, mdDataIdentificationEle, properties);

        /* Create gmd:language element */
        mdDataIdentificationEle.appendChild(createlanguageElement(isoDoc));

        /* Create gmd:topicCategory element */
        Element topicCategoryEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:topicCategory");
        mdDataIdentificationEle.appendChild(topicCategoryEle);
        Element mdTopicCategoryCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_TopicCategoryCode");
        topicCategoryEle.appendChild(mdTopicCategoryCodeEle);
        // MNG: Set default iso Topic Category
        //mdTopicCategoryCodeEle.setTextContent(BundleUtils.getParam(BundleUtils.ISO_MD_TOPC_CATEGORY_CODE));

        /* Create temporal element */
        Element temporalExtentEle = createTemporalExtent(isoDoc, properties);
        if (temporalExtentEle != null) {
            mdDataIdentificationEle.appendChild(temporalExtentEle);
        }

        /* Create geographical element */
        JSONObject geometry = JsonUtils.getGeoJSONObjectProperty(metadataObj, "geometry");

        if (geometry != null) {
            JSONArray coordinates = JsonUtils.getGeoJSONArrayProperty(geometry, "coordinates");
            if (coordinates != null) {
                if (coordinates.length() > 0) {
                    Element geoExtentEle = createGeographicElement(isoDoc, coordinates);
                    if (geoExtentEle != null) {
                        mdDataIdentificationEle.appendChild(geoExtentEle);
                    }
                }

            }
        }

        return identificationInfoEle;

    }

    private Element createIsoAcquisitionInformation(Document isoDoc, JSONArray acqInfos) {
        Element acqInformationEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:acquisitionInformation");
        Element miAcqInformationEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:MI_AcquisitionInformation");
        acqInformationEle.appendChild(miAcqInformationEle);

        for (int i = 0; i < acqInfos.length(); i++) {
            JSONObject acqInfo = acqInfos.getJSONObject(i);
            JSONObject platform = JsonUtils.getGeoJSONObjectProperty(acqInfo, "platform");
            if (platform != null) {
                Element platformEle = createPlatform(isoDoc, acqInfo);
                miAcqInformationEle.appendChild(platformEle);
            }

        }
        return acqInformationEle;

    }

    private Element createCitation(Document isoDoc, JSONObject properties) {
        Element citationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:citation");
        Element ciCitationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Citation");
        citationEle.appendChild(ciCitationEle);

        /* create gmd:title element */
        Element titleEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:title");
        ciCitationEle.appendChild(titleEle);
        Element titleValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
        titleEle.appendChild(titleValue);
        String abstractSt = JsonUtils.getGeoJSONStringProperty(properties, "abstract");
        String identifier = JsonUtils.getGeoJSONStringProperty(properties, "identifier");
        String title = JsonUtils.getGeoJSONStringProperty(properties, "title");

        if (StringUtils.isNotEmpty(title)) {
            titleValue.setTextContent(title);
        } else if (StringUtils.isNotEmpty(abstractSt)) {
            titleValue.setTextContent(abstractSt);
        } else {
            titleValue.setTextContent(identifier);
        }

        /* Create gmd:date element */
        Element dateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
        ciCitationEle.appendChild(dateEle);
        JSONObject isPrimaryTopicOf = JsonUtils.getGeoJSONObjectProperty(properties, "isPrimaryTopicOf");
        if (isPrimaryTopicOf != null) {
            String created = JsonUtils.getGeoJSONStringProperty(isPrimaryTopicOf, "created");
            if (StringUtils.isNotEmpty(created)) {
                Element ciDateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Date");
                dateEle.appendChild(ciDateEle);
                Element innerDateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
                ciDateEle.appendChild(innerDateEle);
                Element dateValue;
                if (StringUtils.contains(created, "T")) {
                    dateValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:DateTime");
                } else {
                    dateValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:Date");
                }
                dateValue.setTextContent(created);
                innerDateEle.appendChild(dateValue);

                Element dateTypeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:dateType");
                ciDateEle.appendChild(dateTypeEle);
                Element ciDateTypeCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_DateTypeCode");
                dateTypeEle.appendChild(ciDateTypeCodeEle);
                ciDateTypeCodeEle.setAttribute("codeList", Constants.ISO_TC211_ORG_CODELIST_BASE + "CI_DateTypeCode");
                ciDateTypeCodeEle.setAttribute("codeListValue", "creation");
            }
        }

        /* Create gmd:identifier element */
        Element identifierEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:identifier");
        ciCitationEle.appendChild(identifierEle);
        Element rsIdentifierEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:RS_Identifier");
        identifierEle.appendChild(rsIdentifierEle);
        Element codeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:code");
        rsIdentifierEle.appendChild(codeEle);
        Element codeValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
        codeEle.appendChild(codeValue);
        codeValue.setTextContent(identifier);

        return citationEle;
    }

    private Element createPlatform(Document isoDoc, JSONObject acqInfo) {

        JSONObject platform = JsonUtils.getGeoJSONObjectProperty(acqInfo, "platform");
        String platformName = JsonUtils.getGeoJSONStringProperty(platform, "platformShortName");
        String platformId = JsonUtils.getGeoJSONStringProperty(platform, "id");

        Element platformEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:platform");
        Element miPlatformEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:MI_Platform");
        platformEle.appendChild(miPlatformEle);
        Element citationEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:citation");
        miPlatformEle.appendChild(citationEle);
        Element ciCitationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Citation");
        citationEle.appendChild(ciCitationEle);
        Element titleEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:title");
        ciCitationEle.appendChild(titleEle);
        if (StringUtils.isEmpty(platformId)) {
            Element titleValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
            titleEle.appendChild(titleValue);
            titleValue.setTextContent(platformName);
        } else {
            Element anchorEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
            titleEle.appendChild(anchorEle);
            anchorEle.setTextContent(platformName);
            anchorEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", platformId);
        }
        Element dateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
        dateEle.setAttributeNS(Constants.GCO_NS, "gco:nilReason", "unknown");
        ciCitationEle.appendChild(dateEle);

        Element idEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:identifier");
        miPlatformEle.appendChild(idEle);

        Element mdIdEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_Identifier");
        idEle.appendChild(mdIdEle);

        Element codeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:code");
        mdIdEle.appendChild(codeEle);
        if (StringUtils.isEmpty(platformId)) {
            Element codeValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
            codeEle.appendChild(codeValue);
            codeValue.setTextContent(platformName);
        } else {
            Element anchorEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
            codeEle.appendChild(anchorEle);
            anchorEle.setTextContent(platformName);
            anchorEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", platformId);
        }

        Element descEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:description");
        miPlatformEle.appendChild(descEle);
        Element descValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
        descEle.appendChild(descValue);
        descValue.setTextContent(platformName);

        JSONObject instrument = JsonUtils.getGeoJSONObjectProperty(acqInfo, "instrument");
        Element instrumentEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:instrument");
        if (instrument != null) {
            instrumentEle = createInstrument(isoDoc, instrument);
        }

        miPlatformEle.appendChild(instrumentEle);

        return platformEle;

    }

    private Element createInstrument(Document isoDoc, JSONObject instrument) {

        String insName = JsonUtils.getGeoJSONStringProperty(instrument, "instrumentShortName");
        String insId = JsonUtils.getGeoJSONStringProperty(instrument, "id");

        Element instrumentEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:instrument");
        Element miInstrumentEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:MI_Instrument");
        instrumentEle.appendChild(miInstrumentEle);

        Element citationEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:citation");
        miInstrumentEle.appendChild(citationEle);
        Element ciCitationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Citation");
        citationEle.appendChild(ciCitationEle);

        Element titleEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:title");

        ciCitationEle.appendChild(titleEle);
        if (StringUtils.isEmpty(insId)) {
            Element titleValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
            titleEle.appendChild(titleValue);
            titleValue.setTextContent(insName);
        } else {
            Element anchorEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
            titleEle.appendChild(anchorEle);
            anchorEle.setTextContent(insName);
            anchorEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", insId);
        }

        Element dateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
        dateEle.setAttributeNS(Constants.GCO_NS, "gco:nilReason", "unknown");
        ciCitationEle.appendChild(dateEle);

        Element idEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:identifier");
        ciCitationEle.appendChild(idEle);

        Element mdIdEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_Identifier");
        idEle.appendChild(mdIdEle);

        Element codeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:code");
        mdIdEle.appendChild(codeEle);
        if (StringUtils.isEmpty(insId)) {
            Element codeValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
            codeEle.appendChild(codeValue);
            codeValue.setTextContent(insName);
        } else {
            Element anchorEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
            codeEle.appendChild(anchorEle);
            anchorEle.setTextContent(insName);
            anchorEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", insId);
        }

        Element typeEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:type");
        miInstrumentEle.appendChild(typeEle);
        Element sensorTypeCodeEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:MI_SensorTypeCode");
        typeEle.appendChild(sensorTypeCodeEle);

        Element descEle = isoDoc.createElementNS(Constants.GMI_NS, "gmi:description");
        miInstrumentEle.appendChild(descEle);
        Element descValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
        descEle.appendChild(descValue);
        descValue.setTextContent(insName);

        return instrumentEle;

    }

    private Element createGeographicElement(Document isoDoc, JSONArray coordinates) {
        JSONArray coordinate = coordinates.getJSONArray(0);

        double west = 180;
        double east = -180;
        double south = 90;
        double north = -90;

        for (int i = 0; i < coordinate.length(); i++) {
            JSONArray points = coordinate.getJSONArray(i);
            double x = points.getDouble(0);
            double y = points.getDouble(1);

            if (west > x) {
                west = x;
            }

            if (east < x) {
                east = x;
            }

            if (south > y) {
                south = y;
            }

            if (north < y) {
                north = y;
            }
        }

        Element extentEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:extent");
        Element exExtentEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:EX_Extent");
        extentEle.appendChild(exExtentEle);
        Element geoEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:geographicElement");
        exExtentEle.appendChild(geoEle);
        Element exGeoBBoxEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:EX_GeographicBoundingBox");
        geoEle.appendChild(exGeoBBoxEle);

        Element westEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:westBoundLongitude");
        exGeoBBoxEle.appendChild(westEle);
        Element westValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:Decimal");
        westEle.appendChild(westValue);
        westValue.setTextContent(String.valueOf(west));

        Element eastEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:eastBoundLongitude");
        exGeoBBoxEle.appendChild(eastEle);
        Element eastValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:Decimal");
        eastEle.appendChild(eastValue);
        eastValue.setTextContent(String.valueOf(east));

        Element southEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:southBoundLatitude");
        exGeoBBoxEle.appendChild(southEle);
        Element southValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:Decimal");
        southEle.appendChild(southValue);
        southValue.setTextContent(String.valueOf(south));

        Element northEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:northBoundLatitude");
        exGeoBBoxEle.appendChild(northEle);
        Element northValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:Decimal");
        northEle.appendChild(northValue);
        northValue.setTextContent(String.valueOf(north));

        return extentEle;

    }

    private void appendDescriptiveKeywords(Document isoDoc, Element parent, JSONObject properties) {
        JSONArray categories = JsonUtils.getGeoJSONArrayProperty(properties, "categories");

        if (categories != null) {
            appendDescriptiveKeywordsByThesausrus(isoDoc, parent, categories, "GMET");
            appendDescriptiveKeywordsByThesausrus(isoDoc, parent, categories, "GCMD");
            appendDescriptiveKeywordsByThesausrus(isoDoc, parent, categories, "EARTHTOPICS");
            appendDescriptiveKeywordsByThesausrus(isoDoc, parent, categories, "OM");
        }

        JSONArray keywords = JsonUtils.getGeoJSONArrayProperty(properties, "keyword");

        if (keywords != null) {

            if (keywords.length() > 0) {

                Element descKwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:descriptiveKeywords");
                Element mdKwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_Keywords");
                descKwEle.appendChild(mdKwEle);
                parent.appendChild(descKwEle);
                for (int i = 0; i < keywords.length(); i++) {
                    String keyword = keywords.getString(i);
                    Element kwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:keyword");
                    mdKwEle.appendChild(kwEle);
                    Element kwValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                    kwEle.appendChild(kwValue);
                    kwValue.setTextContent(keyword);
                }
            }

        }

    }

    private void appendDescriptiveKeywordsByThesausrus(Document isoDoc, Element parent, JSONArray categories,
            String thesaurusName) {
        ArrayList<String> keywords = new ArrayList<>();

        String thesaurusBaseUrl = StringUtils.EMPTY;
        String thesaurusTitle = StringUtils.EMPTY;
        String thesaurusURL = StringUtils.EMPTY;
        String thesaurusDate = StringUtils.EMPTY;

//        if (StringUtils.equals(thesaurusName, "GMET")) {
//            thesaurusBaseUrl = BundleUtils.getParam(BundleUtils.GMET_THESAURUS_BASE_URL);
//            thesaurusTitle = BundleUtils.getParam(BundleUtils.GMET_THESAURUS_TITLE);
//            thesaurusURL = BundleUtils.getParam(BundleUtils.GMET_THESAURUS_URL);
//            thesaurusDate = BundleUtils.getParam(BundleUtils.GMET_THESAURUS_DATE);
//        } else if (StringUtils.equals(thesaurusName, "GCMD")) {
//            thesaurusBaseUrl = BundleUtils.getParam(BundleUtils.GCMD_THESAURUS_BASE_URL);
//            thesaurusTitle = BundleUtils.getParam(BundleUtils.GCMD_THESAURUS_TITLE);
//            thesaurusURL = BundleUtils.getParam(BundleUtils.GCMD_THESAURUS_URL);
//            thesaurusDate = BundleUtils.getParam(BundleUtils.GCMD_THESAURUS_DATE);
//        } else if (StringUtils.equals(thesaurusName, "EARTHTOPICS")) {
//            thesaurusBaseUrl = BundleUtils.getParam(BundleUtils.EARTH_TOPICS_THESAURUS_BASE_URL);
//            thesaurusTitle = BundleUtils.getParam(BundleUtils.EARTH_TOPICS_THESAURUS_TITLE);
//            thesaurusURL = BundleUtils.getParam(BundleUtils.EARTH_TOPICS_THESAURUS_URL);
//            thesaurusDate = BundleUtils.getParam(BundleUtils.EARTH_TOPICS_THESAURUS_DATE);
//        } else if (StringUtils.equals(thesaurusName, "OM")) {
//            thesaurusBaseUrl = BundleUtils.getParam(BundleUtils.OM_THESAURUS_BASE_URL);
//            thesaurusTitle = BundleUtils.getParam(BundleUtils.OM_THESAURUS_TITLE);
//            thesaurusURL = BundleUtils.getParam(BundleUtils.OM_THESAURUS_URL);
//            thesaurusDate = BundleUtils.getParam(BundleUtils.OM_THESAURUS_DATE);
//        }
        for (int i = 0; i < categories.length(); i++) {
            JSONObject category = categories.getJSONObject(i);
            String term = JsonUtils.getGeoJSONStringProperty(category, "term");
            String label = JsonUtils.getGeoJSONStringProperty(category, "label");
            if (StringUtils.isNotEmpty(term) && StringUtils.startsWith(term, "http")) {
                if (StringUtils.startsWith(term, thesaurusBaseUrl)) {
                    keywords.add(term + "#.#" + label);
                }
            }
        }

        if (keywords.size() > 0) {

            if (StringUtils.equals(thesaurusName, "OM")) {
                for (String s : keywords) {

                    Element descKwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:descriptiveKeywords");
                    Element mdKwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_Keywords");
                    descKwEle.appendChild(mdKwEle);
                    parent.appendChild(descKwEle);

                    String href = StringUtils.substringBefore(s, "#.#");
                    String label = StringUtils.substringAfter(s, "#.#");
                    Element kwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:keyword");
                    mdKwEle.appendChild(kwEle);
                    Element anchorEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
                    anchorEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", href);
                    anchorEle.setTextContent(label);
                    kwEle.appendChild(anchorEle);

                    Element typeNameEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:type");
                    mdKwEle.appendChild(typeNameEle);

                    Element mdKwTypeCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_KeywordTypeCode");
                    typeNameEle.appendChild(mdKwTypeCodeEle);
                    mdKwTypeCodeEle.setAttribute("codeListValue", StringUtils.substringAfterLast(href, "/"));
                    mdKwTypeCodeEle.setAttribute("codeList",
                            "https://earth.esa.int/2017/resources/codeList.xml#MD_KeywordTypeCode");

                    Element thesaurusNameEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:thesaurusName");
                    mdKwEle.appendChild(thesaurusNameEle);
                    Element ciCitationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Citation");
                    thesaurusNameEle.appendChild(ciCitationEle);

                    Element titleEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:title");
                    ciCitationEle.appendChild(titleEle);

                    Element anchor4TitleEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
                    anchor4TitleEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", thesaurusURL);
                    anchor4TitleEle.setTextContent(thesaurusTitle);
                    titleEle.appendChild(anchor4TitleEle);

                    Element dateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
                    ciCitationEle.appendChild(dateEle);
                    Element ciDateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Date");
                    dateEle.appendChild(ciDateEle);
                    Element innerDateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
                    ciDateEle.appendChild(innerDateEle);
                    Element dateValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:Date");
                    dateValue.setTextContent(thesaurusDate);
                    innerDateEle.appendChild(dateValue);

                    Element dateTypeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:dateType");
                    ciDateEle.appendChild(dateTypeEle);
                    Element ciDateTypeCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_DateTypeCode");
                    dateTypeEle.appendChild(ciDateTypeCodeEle);
                    ciDateTypeCodeEle.setAttribute("codeList",
                            Constants.STANDARDS_ISO_ORG_CODELIST_BASE + "CI_DateTypeCode");
                    ciDateTypeCodeEle.setAttribute("codeListValue", "publication");
                    ciDateTypeCodeEle.setTextContent("publication");
                }

            } else {
                Element descKwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:descriptiveKeywords");
                Element mdKwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_Keywords");
                descKwEle.appendChild(mdKwEle);
                parent.appendChild(descKwEle);
                for (String s : keywords) {
                    String href = StringUtils.substringBefore(s, "#.#");
                    String label = StringUtils.substringAfter(s, "#.#");
                    Element kwEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:keyword");
                    mdKwEle.appendChild(kwEle);
                    Element anchorEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
                    anchorEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", href);
                    anchorEle.setTextContent(label);
                    kwEle.appendChild(anchorEle);

                }

                Element typeNameEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:type");
                mdKwEle.appendChild(typeNameEle);

                Element mdKwTypeCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_KeywordTypeCode");
                typeNameEle.appendChild(mdKwTypeCodeEle);
                mdKwTypeCodeEle.setAttribute("codeList", Constants.ISO_TC211_ORG_CODELIST_BASE + "MD_KeywordTypeCode");
                mdKwTypeCodeEle.setAttribute("codeListValue", "theme");

                Element thesaurusNameEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:thesaurusName");
                mdKwEle.appendChild(thesaurusNameEle);
                Element ciCitationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Citation");
                thesaurusNameEle.appendChild(ciCitationEle);

                Element titleEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:title");
                ciCitationEle.appendChild(titleEle);

                Element anchorEle = isoDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
                anchorEle.setAttributeNS(Constants.XLINK_NS, "xlink:href", thesaurusURL);
                anchorEle.setTextContent(thesaurusTitle);
                titleEle.appendChild(anchorEle);

                Element dateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
                ciCitationEle.appendChild(dateEle);
                Element ciDateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_Date");
                dateEle.appendChild(ciDateEle);
                Element innerDateEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:date");
                ciDateEle.appendChild(innerDateEle);
                Element dateValue = isoDoc.createElementNS(Constants.GCO_NS, "gco:Date");
                dateValue.setTextContent(thesaurusDate);
                innerDateEle.appendChild(dateValue);

                Element dateTypeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:dateType");
                ciDateEle.appendChild(dateTypeEle);
                Element ciDateTypeCodeEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:CI_DateTypeCode");
                dateTypeEle.appendChild(ciDateTypeCodeEle);
                ciDateTypeCodeEle.setAttribute("codeList",
                        Constants.STANDARDS_ISO_ORG_CODELIST_BASE + "CI_DateTypeCode");
                ciDateTypeCodeEle.setAttribute("codeListValue", "publication");
                ciDateTypeCodeEle.setTextContent("publication");
            }

        }

    }

    private void appendResourceConstraints(Document isoDoc, Element parent, JSONObject properties) {

        JSONArray licenses = JsonUtils.getGeoJSONArrayProperty(properties, "license");
        JSONArray accessRights = JsonUtils.getGeoJSONArrayProperty(properties, "accessRights");

        if (licenses != null || accessRights != null) {
            Element resourceConstraintsEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:resourceConstraints");
            parent.appendChild(resourceConstraintsEle);
            Element mdLegalConstraintsEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:MD_LegalConstraints");
            resourceConstraintsEle.appendChild(mdLegalConstraintsEle);

            if (licenses != null) {
                for (int i = 0; i < licenses.length(); i++) {
                    JSONObject license = licenses.getJSONObject(i);
                    String label = JsonUtils.getGeoJSONStringProperty(license, "label");
                    if (StringUtils.isNotEmpty(label)) {
                        Element useLimitationEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:useLimitation");
                        mdLegalConstraintsEle.appendChild(useLimitationEle);
                        Element useLimitationValue = isoDoc.createElementNS(Constants.GCO_NS,
                                "gco:CharacterString");
                        useLimitationEle.appendChild(useLimitationValue);
                        useLimitationValue.setTextContent(label);
                    }
                }

            }

            if (accessRights != null) {
                for (int i = 0; i < accessRights.length(); i++) {
                    JSONObject accessRight = accessRights.getJSONObject(i);
                    String label = JsonUtils.getGeoJSONStringProperty(accessRight, "label");
                    if (StringUtils.isNotEmpty(label)) {
                        Element otherConstraintsEle = isoDoc.createElementNS(Constants.GMD_NS,
                                "gmd:otherConstraints");
                        mdLegalConstraintsEle.appendChild(otherConstraintsEle);
                        Element otherConstraintsValue = isoDoc.createElementNS(Constants.GCO_NS,
                                "gco:CharacterString");
                        otherConstraintsEle.appendChild(otherConstraintsValue);
                        otherConstraintsValue.setTextContent(label);

                    }
                }
            }
        }
    }

    private Element createTemporalExtent(Document isoDoc, JSONObject properties) {
        Element extentEle = null;

        String date = JsonUtils.getGeoJSONStringProperty(properties, "date");

        if (StringUtils.isNotEmpty(date)) {
            String startDate = StringUtils.substringBefore(date, "/");
            String endDate = StringUtils.substringAfter(date, "/");
            extentEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:extent");
            Element exExtentEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:EX_Extent");
            extentEle.appendChild(exExtentEle);
            Element temporalEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:temporalElement");
            exExtentEle.appendChild(temporalEle);
            Element exTemporalEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:EX_TemporalExtent");
            temporalEle.appendChild(exTemporalEle);
            Element innerExtentEle = isoDoc.createElementNS(Constants.GMD_NS, "gmd:extent");
            exTemporalEle.appendChild(innerExtentEle);

            Element timePeriodEle = isoDoc.createElementNS(Constants.GML_NS, "gml:TimePeriod");
            timePeriodEle.setAttributeNS(Constants.GML_NS, "gml:id", "timeperiod1");
            innerExtentEle.appendChild(timePeriodEle);

            if (StringUtils.isNotEmpty(startDate)) {
                Element beginPositionEle = isoDoc.createElementNS(Constants.GML_NS, "gml:beginPosition");
                timePeriodEle.appendChild(beginPositionEle);
                beginPositionEle.setTextContent(startDate);
            }

            Element endPositionEle = isoDoc.createElementNS(Constants.GML_NS, "gml:endPosition");
            timePeriodEle.appendChild(endPositionEle);
            if (StringUtils.isNotEmpty(endDate)) {
                endPositionEle.setTextContent(endDate);

            }
        }
        return extentEle;
    }

    private void addCategoryAndKeywordAndSubject(Node isoDoc, JSONObject properties, boolean serviceMetadata) {

        ArrayList<String> combinedKeywordURIs = getKeyWordURIs(isoDoc, true, serviceMetadata);

        ArrayList<String> combinedKeyword = new ArrayList<>();
        JSONArray keyword = new JSONArray();
        JSONArray categories = new JSONArray();
        if (combinedKeywordURIs.size() > 0) {
            for (String ckw : combinedKeywordURIs) {

                String scheme = StringUtils.substringAfterLast(ckw, "#;#");
                String tmp = StringUtils.substringBeforeLast(ckw, "#;#");
                String term = StringUtils.substringBefore(tmp, "#;#");
                String label = StringUtils.substringAfter(tmp, "#;#");
                JSONObject category = new JSONObject();
                if (StringUtils.isNotEmpty(term)) {
                    if (StringUtils.isNotEmpty(scheme)) {
                        category.put("scheme", scheme);
                    }
                    category.put("term", term);
                    category.put("label", label.trim());
                    categories.put(category);
                    combinedKeyword.add(label);
                }
            }
            if (categories.length() > 0) {
                properties.put("categories", categories);
            }
        }

        /* keyword */
        ArrayList<String> keywords = getKeyWords(isoDoc);

        if (keywords.size() > 0) {
            for (String kw : keywords) {
                if (!combinedKeyword.contains(kw)) {
                    keyword.put(kw.trim());
                }
            }
            if (keyword.length() > 0) {
                properties.put("keyword", keyword);
            }
        }

        String topicCategory = XPathUtils.getNodeValue(isoDoc,
                "./gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        Map<String, String> topics = new HashMap<>();

        if (StringUtils.isNotEmpty(topicCategory)) {
            topics.put("farming", "Farming");
            topics.put("biota", "Biota");
            topics.put("boundaries", "Boundaries");
            topics.put("climatologyMeteorologyAtmosphere", "Climatology Meteorology Atmosphere");
            topics.put("economy", "Economy");
            topics.put("elevation", "Elevation");
            topics.put("environment", "Environment");
            topics.put("geoscientificInformation", "Geoscientific Information");
            topics.put("health", "Health");
            topics.put("imageryBaseMapsEarthCover", "Imagery Base Maps Earth Cover");
            topics.put("intelligenceMilitary", "Intelligence Military");
            topics.put("inlandWaters", "Inland Waters");
            topics.put("location", "Location");
            topics.put("oceans", "Oceans");
            topics.put("planningCadastre", "Planning Cadastre");
            topics.put("society", "Society");
            topics.put("structure", "Structure");
            topics.put("transportation", "Transportation");
            topics.put("utilitiesCommunication", "Utilities Communication");

            JSONArray subjects = new JSONArray();
            JSONObject subject = new JSONObject();
            subject.put("term", "http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/" + topicCategory);
            String label = topicCategory;
            if (topics.containsKey(topicCategory)) {
                label = topics.get(topicCategory);
            }
            subject.put("label", label);
            subjects.put(subject);
            properties.put("subject", subjects);
        }
    }

    private void addLicenseAndRightInfo(Node entry, JSONObject properties) {
        NodeList resourceConstraints = XPathUtils.getNodes(entry,
                "./gmd:identificationInfo//gmd:resourceConstraints");
        if (resourceConstraints.getLength() > 0) {
            List<String> license1 = XPathUtils.getNodesValues(entry,
                    "./gmd:identificationInfo//gmd:resourceConstraints//gmd:useLimitation/gco:CharacterString");

            JSONArray licenses = new JSONArray();
            if (license1.size() > 0) {
                for (String s : license1) {
                    JSONObject license = new JSONObject();
                    license.put("type", "LicenseDocument");
                    license.put("label", s.trim());
                    licenses.put(license);
                }

            }

            NodeList anchors = XPathUtils.getNodes(entry,
                    "./gmd:identificationInfo//gmd:resourceConstraints//gmd:useLimitation/gmx:Anchor");

            for (int i = 0; i < anchors.getLength(); i++) {
                Element anchor = (Element) anchors.item(i);
                String text = anchor.getTextContent();
                String href = anchor.getAttributeNS(Constants.XLINK_NS, "href");
                String rights = text;
                if (StringUtils.isNotEmpty(href)) {
                    rights = rights.trim() + " " + href;
                }

                if (StringUtils.isNotEmpty(href)) {
                    JSONObject license = new JSONObject();
                    license.put("type", "LicenseDocument");
                    license.put("label", rights);
                    licenses.put(license);
                }
            }

            if (licenses.length() > 0) {
                properties.put("license", licenses);
            }

            List<String> accessRights1 = XPathUtils.getNodesValues(entry,
                    "./gmd:identificationInfo//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString");
            List<String> accessRights2 = XPathUtils.getNodesValues(entry,
                    "./gmd:identificationInfo//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor");

            JSONArray accessRights = new JSONArray();

            if (accessRights1.size() > 0) {

                for (String s : accessRights1) {
                    JSONObject accessRight = new JSONObject();
                    accessRight.put("type", "RightsStatement");
                    accessRight.put("label", s.trim());
                    accessRights.put(accessRight);
                }

            }

            anchors = XPathUtils.getNodes(entry,
                    "./gmd:identificationInfo//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gmx:Anchor");

            for (int i = 0; i < anchors.getLength(); i++) {
                Element anchor = (Element) anchors.item(i);
                String text = anchor.getTextContent();
                String href = anchor.getAttributeNS(Constants.XLINK_NS, "href");
                String rights = text;
                if (StringUtils.isNotEmpty(href)) {
                    rights = rights.trim() + " " + href;
                }

                if (StringUtils.isNotEmpty(href)) {
                    JSONObject accessRight = new JSONObject();
                    accessRight.put("type", "RightsStatement");
                    accessRight.put("label", rights);
                    accessRights.put(accessRight);
                }
            }

            if (accessRights.length() > 0) {
                properties.put("accessRights", accessRights);
            }

        }
    }

    private void addAgentArray(JSONObject parent, String agentName, NodeList responsibleParties) {

        JSONArray agents = new JSONArray();
        for (int i = 0; i < responsibleParties.getLength(); i++) {
            Node responsibleParty = responsibleParties.item(i);
            parent.put(agentName, agents);
            JSONObject agent = createAgentObject(responsibleParty);
            agents.put(agent);
        }
    }

    private JSONArray createQualifiedAttribution(NodeList responsibleParties) {
        JSONArray qualifiedAttributions = new JSONArray();

        for (int i = 0; i < responsibleParties.getLength(); i++) {
            Node responsibleParty = responsibleParties.item(i);

            String role = XPathUtils.getNodeValue(responsibleParty, "./gmd:role/gmd:CI_RoleCode/@codeListValue");

            if (StringUtils.equals(role, "resourceProvider") || StringUtils.equals(role, "custodian")
                    || StringUtils.equals(role, "owner") || StringUtils.equals(role, "user")
                    || StringUtils.equals(role, "distributor") || StringUtils.equals(role, "originator")
                    || StringUtils.equals(role, "pointOfContact") || StringUtils.equals(role, "principalInvestigator")
                    || StringUtils.equals(role, "processor") || StringUtils.equals(role, "publisher")
                    || StringUtils.equals(role, "author")) {

                JSONObject qualifiedAttribution = new JSONObject();
                qualifiedAttributions.put(qualifiedAttribution);
                qualifiedAttribution.put("type", "Attribution");

                JSONArray agents = new JSONArray();
                qualifiedAttribution.put("agent", agents);
                JSONObject agent = createAgentObject(responsibleParty);
                agents.put(agent);

                qualifiedAttribution.put("role", role);

            }
        }
        return qualifiedAttributions;
    }

    private void addProvenance(Node entry, JSONObject properties) {

        NodeList provenanceList = XPathUtils.getNodes(entry,
                "./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage");

        if (provenanceList.getLength() > 0) {
            JSONArray provenances = new JSONArray();
            for (int j = 0; j < provenanceList.getLength(); j++) {
                Element pro = (Element) provenanceList.item(j);
                String label = XPathUtils.getNodeValue(pro,
                        "./gmd:LI_Lineage/gmd:statement/gco:CharacterString");
                if (label != null) {
                    JSONObject provenance = new JSONObject();
                    provenance.put("type", "ProvenanceStatement");
                    provenance.put("label", label.trim());
                    provenances.put(provenance);
                }
            }
            if (provenances.length() > 0) {
                properties.put("provenance", provenances);
            }
        }
    }

    private void addWasUsedBy(Node entry, JSONObject properties) {

        Node conformanceResult = XPathUtils.getNode(entry,
                "./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult");

        if (conformanceResult != null) {
            JSONArray wasUsedByArray = new JSONArray();
            JSONObject wasUsedBy = new JSONObject();
            JSONObject generated = new JSONObject();
            generated.put("type", "Entity");

            String pass = XPathUtils.getNodeValue(conformanceResult, "./gmd:pass/gco:Boolean");
            String conformant = "http://inspire.ec.europa.eu/metadata-codelist/DegreeOfConformity/";
            if (StringUtils.isNotEmpty(pass)) {
                if (StringUtils.equalsIgnoreCase(pass, "true")) {
                    conformant = conformant + "conformant";
                } else {
                    conformant = conformant + "notConformant";
                }
            } else {
                conformant = conformant + "notEvaluated";
            }
            generated.put("degree", conformant);
            String description = XPathUtils.getNodeValue(conformanceResult,
                    "./gmd:explanation/gco:CharacterString");
            if (!StringUtils.isEmpty(description)) {
                generated.put("description", description);
            }
            wasUsedByArray.put(wasUsedBy);
            wasUsedBy.put("type", "Activity");
            wasUsedBy.put("generated", generated);
            properties.put("wasUsedBy", wasUsedByArray);

            Node specification = XPathUtils.getNode(conformanceResult, "./gmd:specification");
            if (specification != null) {
                JSONObject qualifiedAssociation = new JSONObject();
                wasUsedBy.put("qualifiedAssociation", qualifiedAssociation);
                qualifiedAssociation.put("type", "Association");
                JSONObject hadPlan = new JSONObject();
                qualifiedAssociation.put("hadPlan", hadPlan);
                hadPlan.put("type", "Plan");
                JSONObject wasDerivedFrom = new JSONObject();
                hadPlan.put("wasDerivedFrom", wasDerivedFrom);
                wasDerivedFrom.put("type", "Standard");
                String title = XPathUtils.getNodeValue(conformanceResult,
                        "./gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString");
                if (!StringUtils.isEmpty(title)) {
                    wasDerivedFrom.put("title", title.trim());
                }

                String issued = XPathUtils.getNodeValue(conformanceResult,
                        "./gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date//gco:Date");
                if (!StringUtils.isEmpty(issued)) {
                    if (!issued.contains("T")) {
                        issued = issued.trim() + "T00:00:00.00Z";
                    } else {
                        if (!issued.endsWith("Z")) {
                            issued = issued.trim() + "Z";
                        }
                    }
                    wasDerivedFrom.put("issued", issued.trim());
                }
            }

        }
    }

    private JSONObject createIsPrimaryTopicOf(Node entry) {
        JSONObject isPrimaryTopicOf = new JSONObject();
        isPrimaryTopicOf.put("type", "CatalogRecord");

        String lang = XPathUtils.getNodeValue(entry,
                "./gmd:identificationInfo/*/gmd:language/gmd:LanguageCode");

        String modified = XPathUtils.getNodeValue(entry,
                "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date/gco:Date");
        String created = XPathUtils.getNodeValue(entry,
                "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date/gco:Date");

        String published = XPathUtils.getNodeValue(entry,
                "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date/gco:Date");

        if (!StringUtils.isEmpty(created)) {
            if (!created.contains("T")) {
                created = created.trim() + "T00:00:00.00Z";
            } else {
                if (!created.endsWith("Z")) {
                    created = created.trim() + "Z";
                }
            }
            isPrimaryTopicOf.put("created", created);
        }

        if (!StringUtils.isEmpty(published)) {
            if (!published.contains("T")) {
                published = published.trim() + "T00:00:00.00Z";
            } else {
                if (!published.endsWith("Z")) {
                    published = published.trim() + "Z";
                }
            }
            isPrimaryTopicOf.put("created", created);
        }

        if (StringUtils.isNotEmpty(modified)) {
            if (!modified.contains("T")) {
                modified = modified.trim() + "T00:00:00.00Z";
            } else {
                if (!modified.endsWith("Z")) {
                    modified = modified.trim() + "Z";
                }
            }
        } else {
            if (!StringUtils.isEmpty(created)) {
                modified = created;
            }
        }

        if (StringUtils.isNotEmpty(modified)) {
            isPrimaryTopicOf.put("updated", modified);
        }

        if (StringUtils.isNotEmpty(published)) {
            isPrimaryTopicOf.put("published", published);
        }

        if (StringUtils.isNotEmpty(lang)) {
            if (lang.length() > 2) {
                lang = StringUtils.substring(lang, 0, 2);
            }
            isPrimaryTopicOf.put("lang", StringUtils.lowerCase(lang));
        }

        String standard = XPathUtils.getNodeValue(entry, "./gmd:metadataStandardName/gco:CharacterString");
        if (StringUtils.isNotEmpty(standard)) {
            JSONObject conformsTo = new JSONObject();
            conformsTo.put("type", "Standard");
            conformsTo.put("title", standard);
            isPrimaryTopicOf.put("conformsTo", conformsTo);
            String standardVersion = XPathUtils.getNodeValue(entry,
                    "./gmd:metadataStandardVersion/gco:CharacterString");

            if (StringUtils.isNotEmpty(standardVersion)) {
                conformsTo.put("versionInfo", standardVersion);

            }
        }

        return isPrimaryTopicOf;
    }

    private void addAcquisitionInformation(Node entry, JSONObject properties) {

        NodeList platforms = XPathUtils.getNodes(entry,
                "./gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform");
        if (platforms.getLength() > 0) {
            JSONArray acquisitionInfos = new JSONArray();
            properties.put("acquisitionInformation", acquisitionInfos);
            for (int i = 0; i < platforms.getLength(); i++) {
                Node platf = platforms.item(i);
                JSONObject acquisitionInfo = new JSONObject();
                Node identifier = XPathUtils.getNode(platf, "gmi:identifier/gmd:MD_Identifier");
                if (identifier != null) {
                    Element id = (Element) XPathUtils.getNode(platf,
                            "gmi:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");

                    JSONObject platform = new JSONObject();
                    acquisitionInfos.put(acquisitionInfo);
                    if (id != null) {
                        if (StringUtils.isNotEmpty(id.getAttribute("xlink:href"))) {
                            platform.put("id", id.getAttribute("xlink:href"));
                        }
                    }
                    String platformShortName = XPathUtils.getNodeValue(platf,
                            "gmi:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");

                    if (StringUtils.isEmpty(platformShortName)) {
                        platformShortName = XPathUtils.getNodeValue(platf,
                                "gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
                    }

                    if (StringUtils.isNotEmpty(platformShortName)) {
                        platform.put("platformShortName", platformShortName);
                    }

                    if (platform.length() > 0) {
                        acquisitionInfo.put("platform", platform);
                    }

                }

                NodeList instruments = XPathUtils.getNodes(platf, "gmi:instrument/gmi:MI_Instrument");
                if (instruments.getLength() > 0) {

                    JSONObject instrument = new JSONObject();
                    for (int j = 0; j < instruments.getLength(); j++) {
                        Node ins = instruments.item(j);
                        Node insId = XPathUtils.getNode(ins,
                                "gmi:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier");
                        if (insId == null) {
                            insId = XPathUtils.getNode(ins,
                                    "gmi:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier");
                        }

                        if (insId != null) {

                            Element anchor = (Element) XPathUtils.getNode(insId, "gmd:code/gmx:Anchor");
                            if (anchor != null) {
                                if (StringUtils.isNotEmpty(anchor.getAttribute("xlink:href"))) {
                                    instrument.put("id", anchor.getAttribute("xlink:href"));
                                }
                            }

                            String instrumentShortName = XPathUtils.getNodeValue(insId, "gmd:code/gmx:Anchor");
                            if (StringUtils.isEmpty(instrumentShortName)) {
                                instrumentShortName = XPathUtils.getNodeValue(insId,
                                        "gmd:code/gco:CharacterString");
                            }

                            if (StringUtils.isNotEmpty(instrumentShortName)) {
                                instrument.put("instrumentShortName", instrumentShortName);
                            }

                        } else {

                            insId = XPathUtils.getNode(ins, "gmi:citation/gmd:CI_Citation/gmd:title");

                            if (insId != null) {
                                Element anchor = (Element) XPathUtils.getNode(insId, "gmx:Anchor");
                                if (anchor != null) {
                                    if (StringUtils.isNotEmpty(anchor.getAttribute("xlink:href"))) {
                                        instrument.put("id", anchor.getAttribute("xlink:href"));
                                    }
                                }
                                String instrumentShortName = XPathUtils.getNodeValue(insId, "gmx:Anchor");
                                if (StringUtils.isEmpty(instrumentShortName)) {
                                    instrumentShortName = XPathUtils.getNodeValue(insId, "gco:CharacterString");
                                }

                                if (StringUtils.isNotEmpty(instrumentShortName)) {
                                    instrument.put("instrumentShortName", instrumentShortName);
                                }
                            }
                        }
                    }
                    acquisitionInfo.put("instrument", instrument);
                }
            }
        }
    }

    private void addLinks(JSONObject links, Node doc) {
        NodeList onlineResources = XPathUtils.getNodes(doc,
                "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='information']");
        JSONArray describedbys = new JSONArray();

        for (int j = 0; j < onlineResources.getLength(); ++j) {
            JSONObject describedby = new JSONObject();
            Element onlineResource = (Element) onlineResources.item(j);
            Node urlNode = XPathUtils.getNode(onlineResource, "gmd:linkage/gmd:URL");
            Node applicationProfile = XPathUtils.getNode(onlineResource,
                    "gmd:applicationProfile/gco:CharacterString");
            Node name = XPathUtils.getNode(onlineResource, "gmd:name/gco:CharacterString");
            Node description = XPathUtils.getNode(onlineResource, "gmd:description/gco:CharacterString");

            String url = urlNode.getTextContent();
            String title = StringUtils.EMPTY;
            String type = StringUtils.EMPTY;

            if (url.endsWith(".pdf")) {
                type = Constants.APPLICATION_PDF_MIME_TYPE;
            } else if ((url.endsWith(".docx")) || (url.endsWith(".doc"))) {
                type = Constants.APPLICATION_WORD_MIME_TYPE;
            } else if (url.endsWith(".zip")) {
                type = Constants.APPLICATION_ZIP_MIME_TYPE;
            } else if (applicationProfile != null) {
                String applicationProfileValue = applicationProfile.getTextContent();
                if (applicationProfileValue.equals("PDF")) {
                    type = Constants.APPLICATION_PDF_MIME_TYPE;
                } else if (applicationProfileValue.equals("WORD")) {
                    type = Constants.APPLICATION_WORD_MIME_TYPE;
                } else {
                    type = Constants.TEXT_HTML_MIME_TYPE;
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

            if (StringUtils.isNotEmpty(nameValue) && StringUtils.isNotEmpty(desc)) {
                title = nameValue + " - " + desc;
            } else {
                if (StringUtils.isNotEmpty(nameValue)) {
                    title = nameValue;
                }
                if (StringUtils.isNotEmpty(desc)) {
                    title = desc;
                }

            }

            describedby.put("href", url);

            if (StringUtils.isNotEmpty(title)) {
                describedby.put("title", title);
            }

            if (StringUtils.isNotEmpty(type)) {
                describedby.put("type", type);
            }

            describedbys.put(describedby);
        }

        if (describedbys.length() > 0) {
            links.put("describedby", describedbys);
        }

//        if (links.length() > 0) {
//            properties.put("links", links);
//        }
    }

    /**
     * Get list of keywoords from the metadata
     *
     * @param xmlDoc metadata
     */
    private ArrayList<String> getKeyWordURIs(Node xmlDoc, boolean combined, boolean serviceMetadata) {
        ArrayList<String> keywordURIList = new ArrayList<>();

        NodeList mdKeywords = XPathUtils.getNodes(xmlDoc,
                "//gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords");

        for (int j = 0; j < mdKeywords.getLength(); j++) {
            Element mdKeyword = (Element) mdKeywords.item(j);

            Element thesaurusNameAnchor = (Element) XPathUtils.getNode(mdKeyword,
                    "gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor");
            String href = StringUtils.EMPTY;

            if (thesaurusNameAnchor != null) {
                href = thesaurusNameAnchor.getAttributeNS(Constants.XLINK_NS, "href");
            } else {
                if (serviceMetadata) {
                    String thesaurusTitle = XPathUtils
                            .getNodeValue(mdKeyword, "./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString");
                    if (StringUtils.isNotEmpty(thesaurusTitle)) {
                        List<String> keywords = XPathUtils.getNodesValues(mdKeyword, "gmd:keyword/gco:CharacterString");
                        if (keywords != null && keywords.size() > 0) {
                            if (StringUtils.equalsIgnoreCase(thesaurusTitle,
                                    config.getSpatialDataServiceCategoryThesaurus().getFullTitle())) {
                                String theUri = config.getSpatialDataServiceCategoryThesaurus().getUri();
                                String baseUri = CommonUtils.removeLastSlash(config.getSpatialDataServiceCategoryThesaurus().getUriSpace());
                                keywords.forEach((kw) -> {
                                    String uri = baseUri + "/" + kw;
                                    if (combined) {
                                        if (!keywordURIList.contains(uri + "#;#" + kw + "#;#" + theUri)) {
                                            keywordURIList.add(uri + "#;#" + kw + "#;#" + theUri);
                                        }
                                    } else {
                                        if (!keywordURIList.contains(uri)) {
                                            keywordURIList.add(uri);
                                        }
                                    }
                                });
                            } else {
                                String type = XPathUtils
                                        .getNodeValue(mdKeyword, "./gmd:type/gmd:MD_KeywordTypeCode/@codeListValue");
                                if (StringUtils.isNotEmpty(type)) {
                                    String eopUri = null;
                                    switch (type) {
                                        case Constants.ORBITTYPE_KEY:
                                        case Constants.WAVELENGTH_KEY:
                                        case Constants.PROCESSORVER_KEY:
                                        case Constants.RESOLUTION_KEY:
                                        case Constants.PRODUCTTYPE_KEY:
                                            eopUri = config.getEopThesaurus().getTitleUri();
                                            break;
                                        case Constants.ORBITHEIGHT_KEY:
                                        case Constants.SWATHWIDTH_KEY:
                                            eopUri = config.getEopExtThesaurus().getTitleUri();
                                            break;
                                    }

                                    if (StringUtils.isNotEmpty(eopUri)) {
                                        String baseUri = CommonUtils.removeLastSlash(eopUri);
                                        for (String kw : keywords) {
                                            String uri = baseUri + "/" + type;
                                            if (combined) {
                                                if (!keywordURIList.contains(uri + "#;#" + kw + "#;#" + eopUri)) {
                                                    keywordURIList.add(uri + "#;#" + kw + "#;#" + eopUri);
                                                }
                                            } else {
                                                if (!keywordURIList.contains(uri)) {
                                                    keywordURIList.add(uri);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!serviceMetadata) {
                NodeList anchors = XPathUtils.getNodes(mdKeyword, "gmd:keyword/gmx:Anchor");

                for (int i = 0; i < anchors.getLength(); i++) {
                    Element anchor = (Element) anchors.item(i);
                    String uri = anchor.getAttributeNS(Constants.XLINK_NS, "href");
                    String keyword = anchor.getTextContent();
                    if (combined) {
                        if (!keywordURIList.contains(uri + "#;#" + keyword + "#;#" + href)) {
                            keywordURIList.add(uri + "#;#" + keyword + "#;#" + href);
                        }
                    } else {
                        if (!keywordURIList.contains(uri)) {
                            keywordURIList.add(uri);
                        }
                    }
                }
            }
        }

        NodeList anchors = XPathUtils.getNodes(xmlDoc,
                "//gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");

        for (int i = 0; i < anchors.getLength(); i++) {
            Element anchor = (Element) anchors.item(i);
            String uri = anchor.getAttributeNS(Constants.XLINK_NS, "href");
            String keyword = anchor.getTextContent();
            if (combined) {
                if (!keywordURIList.contains(uri + "#;#" + keyword + "#;#")) {
                    keywordURIList.add(uri + "#;#" + keyword + "#;#");
                }
            } else {
                if (!keywordURIList.contains(uri)) {
                    keywordURIList.add(uri);
                }
            }
        }

        anchors = XPathUtils.getNodes(xmlDoc,
                "//gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:instrument/gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:title/gmx:Anchor");
        for (int i = 0; i < anchors.getLength(); i++) {
            Element anchor = (Element) anchors.item(i);
            String uri = anchor.getAttributeNS(Constants.XLINK_NS, "href");
            String keyword = anchor.getTextContent();
            if (combined) {
                if (!keywordURIList.contains(uri + "#;#" + keyword + "#;#")) {
                    keywordURIList.add(uri + "#;#" + keyword + "#;#");
                }
            } else {
                if (!keywordURIList.contains(uri)) {
                    keywordURIList.add(uri);
                }
            }
        }

        anchors = XPathUtils.getNodes(xmlDoc,
                "//gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:instrument/gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor");
        for (int i = 0; i < anchors.getLength(); i++) {
            Element anchor = (Element) anchors.item(i);
            String uri = anchor.getAttributeNS(Constants.XLINK_NS, "href");
            String keyword = anchor.getTextContent();
            if (combined) {
                if (!keywordURIList.contains(uri + "#;#" + keyword + "#;#")) {
                    keywordURIList.add(uri + "#;#" + keyword + "#;#");
                }
            } else {
                if (!keywordURIList.contains(uri)) {
                    keywordURIList.add(uri);
                }
            }
        }

        return keywordURIList;

    }

    /**
     * Get list of keywords from the metadata
     *
     * @param xmlDoc metadata
     */
    private static ArrayList<String> getKeyWords(Node xmlDoc) {
        ArrayList<String> keywordsList = new ArrayList<>();
        List<String> keywords = XPathUtils.getNodesValues(xmlDoc,
                "//gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor");

        keywordsList.addAll(keywords);

        keywords = XPathUtils.getNodesValues(xmlDoc,
                "//gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");

        keywordsList.addAll(keywords);

        return keywordsList;

    }

    private JSONObject createAgentObject(Node responsibleParty) {

        JSONObject agent = new JSONObject();

        String email = XPathUtils.getNodeValue(responsibleParty,
                "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
        if (StringUtils.isNotEmpty(email)) {
            agent.put("email", email);
        }

        String organisationName = XPathUtils.getNodeValue(responsibleParty,
                "./gmd:organisationName/gco:CharacterString");
        String individualName = XPathUtils.getNodeValue(responsibleParty,
                "./gmd:individualName/gco:CharacterString");

        if (StringUtils.isNotEmpty(organisationName)) {
            agent.put("name", organisationName);
            agent.put("type", "Organization");

        } else if (StringUtils.isNotEmpty(individualName)) {
            agent.put("name", individualName);
            agent.put("type", "Individual");

        } else {
            agent.put("type", "Agent");
        }

        String phone = XPathUtils.getNodeValue(responsibleParty,
                "./gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString");
        if (StringUtils.isNotEmpty(phone)) {
            agent.put("phone", "tel:" + phone);
        }

        String uri = XPathUtils.getNodeValue(responsibleParty,
                "./gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
        if (StringUtils.isNotEmpty(uri)) {
            agent.put("uri", uri);
        }

        NodeList addresses = XPathUtils.getNodes(responsibleParty,
                "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address");
        for (int j = 0; j < addresses.getLength(); j++) {
            Node addresse = addresses.item(j);

            String countryName = XPathUtils.getNodeValue(addresse, "./gmd:country/gco:CharacterString");
            String postalCode = XPathUtils.getNodeValue(addresse, "./gmd:postalCode/gco:CharacterString");
            String locality = XPathUtils.getNodeValue(addresse, "./gmd:city/gco:CharacterString");
            String streetAddress = XPathUtils.getNodeValue(addresse, "./gmd:deliveryPoint/gco:CharacterString");

            if (StringUtils.isNotEmpty(countryName) || StringUtils.isNotEmpty(postalCode)
                    || StringUtils.isNotEmpty(locality) || StringUtils.isNotEmpty(streetAddress)) {

                JSONObject hasAddress = new JSONObject();

                if (StringUtils.isNotEmpty(countryName)) {
                    hasAddress.put("country-name", countryName);
                }

                if (StringUtils.isNotEmpty(postalCode)) {
                    hasAddress.put("postal-code", postalCode);
                }

                if (StringUtils.isNotEmpty(locality)) {
                    hasAddress.put("locality", locality);
                }

                if (StringUtils.isNotEmpty(streetAddress)) {
                    hasAddress.put("street-address", streetAddress);
                }
                agent.put("hasAddress", hasAddress);

            }
        }

        return agent;
    }

    private void updateCategoriesAndKeywords(JSONObject properties, Metadata metadata) {

        JSONArray categories = new JSONArray();
        JSONArray keywords = new JSONArray();

        if (metadata.getIdentification() != null) {
            /*
                transform EarthTopics to categories
             */
            if (metadata.getIdentification().getEarthTopics() != null
                    && metadata.getIdentification().getEarthTopics().size() > 0) {

                metadata.getIdentification().getEarthTopics().forEach((eTopic) -> {
                    JSONObject category = new JSONObject();
                    category.put("scheme", config.getEarthtopicsThesaurusUri());
                    category.put("term", eTopic.getUri());
                    category.put("label", StringUtils.trimToEmpty(eTopic.getLabel()));
                    categories.put(category);
                    if (eTopic.getScienceKeywords() != null
                            && eTopic.getScienceKeywords().size() > 0) {
                        eTopic.getScienceKeywords().stream().map((concept) -> {
                            JSONObject cat = new JSONObject();
                            cat.put("scheme", config.getSckwThesaurusUri());
                            cat.put("term", concept.getUri());
                            cat.put("label", StringUtils.trimToEmpty(MetadataUtils.buildScKwLabel(concept)));
                            return cat;
                        }).forEachOrdered((cat) -> {
                            categories.put(cat);
                        });
                    }
                });
            }

            /*
                transform Spatial Data Service Category to categories
             */
            if (metadata.getIdentification().getSpatialDataServiceCategoryKeywords() != null
                    && XmlUtils.hasKeyword(metadata.getIdentification().getSpatialDataServiceCategoryKeywords())) {

                VoidDataset ds = config.getSpatialDataServiceCategoryThesaurus();
                metadata.getIdentification().getSpatialDataServiceCategoryKeywords().getKeywords().stream().filter((kw) -> (StringUtils.isNotEmpty(kw.getLabel()))).map((kw) -> {
                    JSONObject category = new JSONObject();
                    category.put("scheme", ds.getUri());
                    category.put("term", ds.getUriSpace() + "/" + kw.getLabel());
                    category.put("label", StringUtils.trimToEmpty(kw.getLabel()));
                    return category;
                }).forEachOrdered((category) -> {
                    categories.put(category);
                });
            }

            /*
                transform EOP EXT keywords to categories
             */
            if (XmlUtils.hasKeyword(metadata.getIdentification().getOrbitType())) {
                addEopCategories(categories, metadata.getIdentification().getOrbitType());
            }
            if (XmlUtils.hasKeyword(metadata.getIdentification().getWaveLength())) {
                addEopCategories(categories, metadata.getIdentification().getWaveLength());
            }
            if (XmlUtils.hasKeyword(metadata.getIdentification().getProcessorVersion())) {
                addEopCategories(categories, metadata.getIdentification().getProcessorVersion());
            }
            if (XmlUtils.hasKeyword(metadata.getIdentification().getResolution())) {
                addEopCategories(categories, metadata.getIdentification().getResolution());
            }
            if (XmlUtils.hasKeyword(metadata.getIdentification().getProductType())) {
                addEopCategories(categories, metadata.getIdentification().getProductType());
            }
            if (XmlUtils.hasKeyword(metadata.getIdentification().getOrbitHeight())) {
                addEopCategories(categories, metadata.getIdentification().getOrbitHeight());
            }
            if (XmlUtils.hasKeyword(metadata.getIdentification().getSwathWidth())) {
                addEopCategories(categories, metadata.getIdentification().getSwathWidth());
            }

            List<String> freeKeywords = new ArrayList<>();
            getFreeKeywords(freeKeywords, metadata.getIdentification().getPlaceKeyword());
            getFreeKeywords(freeKeywords, metadata.getIdentification().getFreeKeyword());

//            if (metadata.getIdentification().getFreeKeywords() != null
//                    && metadata.getIdentification().getFreeKeywords().size() > 0) {
//                metadata.getIdentification().getFreeKeywords().forEach((freeKw) -> {
//                    getFreeKeywords(freeKeywords, freeKw);
//                });
//            }
            if (freeKeywords.size() > 0) {
                freeKeywords.forEach((kw) -> {
                    keywords.put(kw);
                });
            }
        }

        /*
                transform platforms and instrument to categories
         */
        if (metadata.getAcquisition() != null
                && metadata.getAcquisition().getPlatforms() != null
                && metadata.getAcquisition().getPlatforms().size() > 0) {

            Map<String, String> plfKeywords = new HashMap<>();
            Map<String, String> gcmdPlfKeywords = new HashMap<>();
            Map<String, String> instKeywords = new HashMap<>();
            Map<String, String> gcmdInstKeywords = new HashMap<>();
            Map<String, String> instTypeKeywords = new HashMap<>();

            metadata.getAcquisition().getPlatforms().stream().map((plf) -> {

                plfKeywords.putIfAbsent(plf.getUri(), StringUtils.trimToEmpty(plf.getLabel()));
                if (metadata.isService() && plf.getGcmd() != null) {
                    gcmdPlfKeywords.putIfAbsent(plf.getGcmd().getUri(), plf.getGcmd().getLabel());
                }

                return plf;
            }).filter((plf) -> (plf.getInstruments() != null
                    && plf.getInstruments().size() > 0)).forEachOrdered((plf) -> {
                plf.getInstruments().stream().map((inst) -> {
                    instKeywords.putIfAbsent(inst.getUri(), StringUtils.trimToEmpty(inst.getLabel()));
                    if (metadata.isService() && inst.getGcmd() != null) {
                        gcmdInstKeywords.putIfAbsent(inst.getGcmd().getUri(), inst.getGcmd().getLabel());
                    }
                    return inst;
                }).filter((inst) -> (inst.getBroaders() != null)).forEachOrdered((inst) -> {
                    if (metadata.isSeries()) {
                        inst.getBroaders().stream().filter((instType) -> (!instTypeKeywords.containsKey(instType.getUri()))).forEachOrdered((instType) -> {
                            instTypeKeywords.put(instType.getUri(), instType.getLabel());
                        });
                    }
                });
            });

            instTypeKeywords.entrySet().stream().map((entry) -> {
                JSONObject category = new JSONObject();
                category.put("scheme", config.getInstrumentThesaurusUri());
                category.put("term", entry.getKey());
                category.put("label", entry.getValue());
                return category;
            }).forEachOrdered((category) -> {
                categories.put(category);
            });

            plfKeywords.entrySet().stream().map((entry) -> {
                JSONObject category = new JSONObject();
                category.put("scheme", config.getPlatformThesaurusUri());
                category.put("term", entry.getKey());
                category.put("label", entry.getValue());
                return category;
            }).forEachOrdered((category) -> {
                categories.put(category);
            });

            gcmdPlfKeywords.entrySet().stream().map((entry) -> {
                JSONObject category = new JSONObject();
                category.put("scheme", config.getGcmdPlatformThesaurusUri());
                category.put("term", entry.getKey());
                category.put("label", entry.getValue());
                return category;
            }).forEachOrdered((category) -> {
                categories.put(category);
            });

            instKeywords.entrySet().stream().map((entry) -> {
                JSONObject category = new JSONObject();
                category.put("scheme", config.getInstrumentThesaurusUri());
                category.put("term", entry.getKey());
                category.put("label", entry.getValue());
                return category;
            }).forEachOrdered((category) -> {
                categories.put(category);
            });

            gcmdInstKeywords.entrySet().stream().map((entry) -> {
                JSONObject category = new JSONObject();
                category.put("scheme", config.getGcmdInstrumentThesaurusUri());
                category.put("term", entry.getKey());
                category.put("label", entry.getValue());
                return category;
            }).forEachOrdered((category) -> {
                categories.put(category);
            });
        }

        if (categories.length() > 0) {
            properties.put("categories", categories);
        } else {
            properties.remove("categories");
        }

        if (keywords.length() > 0) {
            properties.put("keyword", keywords);
        } else {
            properties.remove("keyword");
        }
    }

    private void addEopCategories(JSONArray categories, ThesaurusKeyword eopKw) {
        eopKw.getKeywords().stream().map((kw) -> StringUtils.trimToEmpty(kw.getLabel())).filter((label) -> (StringUtils.isNotEmpty(label))).map((label) -> {
            JSONObject category = new JSONObject();
            category.put("scheme", eopKw.getThesaurus().getTitleUri());
            category.put("term", eopKw.getThesaurus().getTitleUri() + eopKw.getCodeListValue());
            category.put("label", label);
            return category;
        }).forEachOrdered((category) -> {
            categories.put(category);
        });
    }

    private void getFreeKeywords(List<String> freeKeywords, FreeKeyword freeKw) {
        if (XmlUtils.hasKeyword(freeKw)) {
            freeKw.getKeywords().stream().map((kw) -> StringUtils.trimToEmpty(kw.getLabel())).filter((kwLabel) -> (StringUtils.isNotEmpty(kwLabel)
                    && !freeKeywords.contains(kwLabel))).forEachOrdered((kwLabel) -> {
                freeKeywords.add(kwLabel);
            });
        }
    }

    private void updateSubject(JSONObject properties, String topicCategory) {
        JSONArray subjects = new JSONArray();
        JSONObject subject = new JSONObject();
        subject.put("term", "http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/" + topicCategory);
        subject.put("label", topicCategory);
        subjects.put(subject);
        properties.put("subject", subjects);
    }

    private void updateLicenseAndRightInfo(JSONObject properties, List<Constraints> constraints) {
        if (constraints != null && constraints.size() > 0) {
            JSONArray licenses = new JSONArray();
            JSONArray accessRights = new JSONArray();
            constraints.stream().map((constr) -> {
                if (constr.getUseLimitations() != null
                        && constr.getUseLimitations().size() > 0) {
                    constr.getUseLimitations().stream().map((useLimit) -> {
                        JSONObject license = new JSONObject();
                        String label = useLimit.getText() != null ? useLimit.getText() : "";
                        if (StringUtils.isNotEmpty(useLimit.getLink())) {
                            label = label + " " + useLimit.getLink();
                        }
                        license.put("type", "LicenseDocument");
                        //license.put("label", StringUtils.trimToEmpty(label));
                        buildLabel(license, label, useLimit.getRichText());
                        return license;
                    }).forEachOrdered((license) -> {
                        licenses.put(license);
                    });
                }
                return constr;
            }).filter((constr) -> (constr.getOthers() != null
                    && constr.getOthers().size() > 0)).forEachOrdered((constr) -> {
                constr.getOthers().forEach((other) -> {
                    String label = other.getText() != null ? other.getText() : "";
                    if (StringUtils.isNotEmpty(other.getLink())) {
                        label = label + " " + other.getLink();
                    }
                    JSONObject accessRight = new JSONObject();
                    accessRight.put("type", "RightsStatement");
                    buildLabel(accessRight, label, other.getRichText());
                    //accessRight.put("label", StringUtils.trimToEmpty(label));
                    accessRights.put(accessRight);
                });
            });

            if (licenses.length() > 0) {
                properties.put("license", licenses);
            } else {
                properties.remove("license");
            }

            if (accessRights.length() > 0) {
                properties.put("accessRights", accessRights);
            } else {
                properties.remove("accessRights");
            }

        } else {
            properties.remove("license");
            properties.remove("accessRights");
        }
    }

    private void buildLabel(JSONObject parent, String plainText, String richText) {
        if (StringUtils.isNotEmpty(richText)) {
            JSONObject labelObj = new JSONObject();
            if (StringUtils.isNotEmpty(plainText)) {
                labelObj.put("text/plain", StringUtils.trimToEmpty(plainText));
            }

            JSONArray richTextArray = new JSONArray();
            richTextArray.put(richText);
            labelObj.put("text/markdown", richTextArray);

            parent.put("label", labelObj);
        } else {
            parent.put("label", StringUtils.trimToEmpty(plainText));
        }
    }

    private void distinguishContact(List<Contact> originalContacts,
            List<Contact> pointOfContacts, List<Contact> authorContacts, List<Contact> qualifiedContacts) {

        if (originalContacts != null && originalContacts.size() > 0) {
            originalContacts.forEach((contact) -> {
                String role = StringUtils.trimToEmpty(contact.getRole());
                if (StringUtils.isNotEmpty(role)) {
                    if ("pointOfContact".equalsIgnoreCase(role)) {
                        pointOfContacts.add(contact);
                    } else {
                        if ("author".equalsIgnoreCase(role)) {
                            authorContacts.add(contact);
                        } else {
                            if (!"publisher".equalsIgnoreCase(role)) {
                                qualifiedContacts.add(contact);
                            }
                        }
                    }
                }
            });
        }
    }

    private void createAgentArray(JSONObject parent, String agentName, List<Contact> contacts) {
        JSONArray agents = new JSONArray();
        contacts.forEach((contact) -> {
            parent.put(agentName, agents);
            agents.put(createAgentObject(contact));
        });
    }

    private JSONArray createQualifiedAttribution(List<Contact> contacts) {
        JSONArray qualifiedAttributions = new JSONArray();

        contacts.forEach((contact) -> {
            String role = contact.getRole();
            if (StringUtils.equals(role, "resourceProvider") || StringUtils.equals(role, "custodian")
                    || StringUtils.equals(role, "owner") || StringUtils.equals(role, "user")
                    || StringUtils.equals(role, "distributor") || StringUtils.equals(role, "originator")
                    || StringUtils.equals(role, "pointOfContact") || StringUtils.equals(role, "principalInvestigator")
                    || StringUtils.equals(role, "processor") || StringUtils.equals(role, "publisher")
                    || StringUtils.equals(role, "author")) {
                JSONObject qualifiedAttribution = new JSONObject();
                qualifiedAttributions.put(qualifiedAttribution);
                qualifiedAttribution.put("type", "Attribution");

                JSONArray agents = new JSONArray();
                qualifiedAttribution.put("agent", agents);
                JSONObject agent = createAgentObject(contact);
                agents.put(agent);

                qualifiedAttribution.put("role", role);
            }
        });
        return qualifiedAttributions;
    }

    private JSONObject createAgentObject(Contact contact) {
        JSONObject agent = new JSONObject();

        if (StringUtils.isNotEmpty(contact.getEmail())) {
            agent.put("email", contact.getEmail());
        }
        if (StringUtils.isNotEmpty(contact.getOrgName())) {
            agent.put("name", contact.getOrgName());
            agent.put("type", "Organization");
        } else if (StringUtils.isNotEmpty(contact.getIndividualName())) {
            agent.put("name", contact.getIndividualName());
            agent.put("type", "Individual");
        } else {
            agent.put("type", "Agent");
        }
        if (StringUtils.isNotEmpty(contact.getPhone())) {
            agent.put("phone", "tel:" + contact.getPhone());
        }
        if (StringUtils.isNotEmpty(contact.getOnlineRs())) {
            agent.put("uri", contact.getOnlineRs());
        }
        String countryName = contact.getCountry();
        String postalCode = contact.getPostal();
        String locality = contact.getCity();
        String streetAddress = contact.getAddress();
        if (StringUtils.isNotEmpty(countryName) || StringUtils.isNotEmpty(postalCode)
                || StringUtils.isNotEmpty(locality) || StringUtils.isNotEmpty(streetAddress)) {
            JSONObject hasAddress = new JSONObject();

            if (StringUtils.isNotEmpty(countryName)) {
                hasAddress.put("country-name", countryName);
            }

            if (StringUtils.isNotEmpty(postalCode)) {
                hasAddress.put("postal-code", postalCode);
            }

            if (StringUtils.isNotEmpty(locality)) {
                hasAddress.put("locality", locality);
            }

            if (StringUtils.isNotEmpty(streetAddress)) {
                hasAddress.put("street-address", streetAddress);
            }
            agent.put("hasAddress", hasAddress);
        }

        return agent;
    }

    private JSONObject createIsPrimaryTopicOf(Metadata metadata) {
        JSONObject isPrimaryTopicOf = new JSONObject();
        isPrimaryTopicOf.put("type", "CatalogRecord");

        String lang = metadata.getOthers().getLanguage();

        String modified = StringUtils.EMPTY;
        if (metadata.getOthers() != null) {
            modified = metadata.getOthers().getLastUpdateDate();
        }
        if (StringUtils.isEmpty(modified)) {
            modified = CommonUtils.toDateTimeZoneFullStr(metadata
                    .getIdentification().getRevisionDate());
        }

        String created = CommonUtils.toDateTimeZoneFullStr(metadata
                .getIdentification().getCreationDate());
        String published = CommonUtils.toDateTimeZoneFullStr(metadata
                .getIdentification().getPublicationDate());

        if (!StringUtils.isEmpty(created)) {
            if (!created.contains("T")) {
                created = created.trim() + "T00:00:00.00Z";
            } else {
                if (!created.endsWith("Z")) {
                    created = created.trim() + "Z";
                }
            }
            isPrimaryTopicOf.put("created", created);
        }

        if (!StringUtils.isEmpty(published)) {
            if (!published.contains("T")) {
                published = published.trim() + "T00:00:00.00Z";
            } else {
                if (!published.endsWith("Z")) {
                    published = published.trim() + "Z";
                }
            }
            isPrimaryTopicOf.put("created", created);
        }

        if (StringUtils.isNotEmpty(modified)) {
            if (!modified.contains("T")) {
                modified = modified.trim() + "T00:00:00.00Z";
            } else {
                if (!modified.endsWith("Z")) {
                    modified = modified.trim() + "Z";
                }
            }
        } else {
            if (!StringUtils.isEmpty(created)) {
                modified = created;
            }
        }

        if (StringUtils.isNotEmpty(modified)) {
            isPrimaryTopicOf.put("updated", modified);
        }

        if (StringUtils.isNotEmpty(published)) {
            isPrimaryTopicOf.put("published", published);
        }

        if (StringUtils.isNotEmpty(lang)) {
            if (lang.length() > 2) {
                lang = StringUtils.substring(lang, 0, 2);
            }
            isPrimaryTopicOf.put("lang", StringUtils.lowerCase(lang));
        }

        String standard = metadata.getOthers().getStandardName();
        if (StringUtils.isNotEmpty(standard)) {
            JSONObject conformsTo = new JSONObject();

            conformsTo.put("type", "Standard");
            conformsTo.put("title", standard);
            isPrimaryTopicOf.put("conformsTo", conformsTo);
            String standardVersion = metadata.getOthers().getStandardVersion();

            if (StringUtils.isNotEmpty(standardVersion)) {
                conformsTo.put("versionInfo", standardVersion);
            }
        }

        return isPrimaryTopicOf;
    }

    private JSONArray createAcquisitionInformation(Acquisition acquisition) {
        JSONArray acquisitionInfos = new JSONArray();
        if (acquisition.getPlatforms() != null && acquisition.getPlatforms().size() > 0) {
            for (Platform plf : acquisition.getPlatforms()) {
                JSONObject acquisitionInfo = new JSONObject();

                JSONObject platform = new JSONObject();
                acquisitionInfos.put(acquisitionInfo);

                platform.put("id", plf.getUri());
                platform.put("platformShortName", plf.getLabel());
                acquisitionInfo.put("platform", platform);

                if (plf.getInstruments() != null && plf.getInstruments().size() > 0) {
                    JSONObject instrument = new JSONObject();
                    for (Instrument inst : plf.getInstruments()) {
                        instrument.put("id", inst.getUri());
                        instrument.put("instrumentShortName", inst.getLabel());
                        break;
                    }
                    acquisitionInfo.put("instrument", instrument);
                }
            }
        }
        return acquisitionInfos;
    }

    private void createLinks(JSONObject links, List<TransferOption> transferOptions) {
        JSONArray describedbyArray = new JSONArray();
        JSONArray dataArray = new JSONArray();
        JSONArray relatedArray = new JSONArray();
        JSONArray searchArray = new JSONArray();

        for (TransferOption tfOption : transferOptions) {
            if (tfOption.getOnlineRses() != null
                    && tfOption.getOnlineRses().size() > 0) {
                for (OnlineResource onlineRs : tfOption.getOnlineRses()) {
                    JSONObject jsonObj = new JSONObject();
                    String applicationProfile = onlineRs.getAppProfile();
                    String url = onlineRs.getLinkage();
                    String title = StringUtils.EMPTY;
                    String type = null;
                    if (url.endsWith(".pdf")) {
                        type = Constants.APPLICATION_PDF_MIME_TYPE;
                    } else if ((url.endsWith(".docx")) || (url.endsWith(".doc"))) {
                        type = Constants.APPLICATION_WORD_MIME_TYPE;
                    } else if (url.endsWith(".zip")) {
                        type = Constants.APPLICATION_ZIP_MIME_TYPE;
                    } else if (StringUtils.isNotEmpty(applicationProfile)) {
                        switch (applicationProfile) {
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
                    }
//                    } else {
//                        type = Constants.TEXT_HTML_MIME_TYPE;
//                    }
                    String nameValue = onlineRs.getName();
                    String desc = onlineRs.getDescription();
                    if (StringUtils.isNotEmpty(nameValue) && StringUtils.isNotEmpty(desc)) {
                        title = nameValue + " - " + desc;
                    } else {
                        if (StringUtils.isNotEmpty(nameValue)) {
                            title = nameValue;
                        }
                        if (StringUtils.isNotEmpty(desc)) {
                            title = desc;
                        }
                    }
                    jsonObj.put("href", url);
                    if (StringUtils.isNotEmpty(title)) {
                        jsonObj.put("title", title);
                    }
                    if (StringUtils.isNotEmpty(type)) {
                        jsonObj.put("type", type);
                    }

                    if (StringUtils.isNotEmpty(onlineRs.getFunction())) {
                        if ("download".equals(onlineRs.getFunction())) {
                            dataArray.put(jsonObj);
                        } else {
                            if ("offlineAccess".equalsIgnoreCase(onlineRs.getFunction())
                                    || "order".equalsIgnoreCase(onlineRs.getFunction())) {
                                relatedArray.put(jsonObj);
                            } else {
                                if ("search".equals(onlineRs.getFunction())) {
                                    searchArray.put(jsonObj);
                                } else {
                                    describedbyArray.put(jsonObj);
                                }
                            }
                        }
                    } else {
                        describedbyArray.put(jsonObj);
                    }
                }
            }
        }

//        transferOptions.stream().filter((tfOption) -> (tfOption.getOnlineRses() != null && tfOption.getOnlineRses().size() > 0)).forEachOrdered((tfOption) -> {
//            tfOption.getOnlineRses().stream().map((onlineRs) -> {
//                JSONObject jsonObj = new JSONObject();
//                String applicationProfile = onlineRs.getAppProfile();
//                String url = onlineRs.getLinkage();
//                String title = StringUtils.EMPTY;
//                String type;
//                if (url.endsWith(".pdf")) {
//                    type = Constants.APPLICATION_PDF_MIME_TYPE;
//                } else if ((url.endsWith(".docx")) || (url.endsWith(".doc"))) {
//                    type = Constants.APPLICATION_WORD_MIME_TYPE;
//                } else if (url.endsWith(".zip")) {
//                    type = Constants.APPLICATION_ZIP_MIME_TYPE;
//                } else if (StringUtils.isNotEmpty(applicationProfile)) {
//                    switch (applicationProfile) {
//                        case "PDF":
//                            type = Constants.APPLICATION_PDF_MIME_TYPE;
//                            break;
//                        case "WORD":
//                            type = Constants.APPLICATION_WORD_MIME_TYPE;
//                            break;
//                        default:
//                            type = Constants.TEXT_HTML_MIME_TYPE;
//                            break;
//                    }
//                } else {
//                    type = Constants.TEXT_HTML_MIME_TYPE;
//                }
//                String nameValue = onlineRs.getName();
//                String desc = onlineRs.getDescription();
//                if (StringUtils.isNotEmpty(nameValue) && StringUtils.isNotEmpty(desc)) {
//                    title = nameValue + " - " + desc;
//                } else {
//                    if (StringUtils.isNotEmpty(nameValue)) {
//                        title = nameValue;
//                    }
//                    if (StringUtils.isNotEmpty(desc)) {
//                        title = desc;
//                    }
//                }
//                jsonObj.put("href", url);
//                if (StringUtils.isNotEmpty(title)) {
//                    jsonObj.put("title", title);
//                }
//                if (StringUtils.isNotEmpty(type)) {
//                    jsonObj.put("type", type);
//                }
//                return jsonObj;
//            }).forEachOrdered((jsonObj) -> {
//
//                describedbys.put(jsonObj);
//
//            });
//        });
        if (describedbyArray.length() > 0) {
            links.put("describedby", describedbyArray);
        } else {
            links.remove("describedby");
        }

        if (dataArray.length() > 0) {
            links.put("data", dataArray);
        } else {
            links.remove("data");
        }

        if (relatedArray.length() > 0) {
            links.put("related", relatedArray);
        } else {
            links.remove("related");
        }

        if (searchArray.length() > 0) {
            links.put("search", searchArray);
        } else {
            links.remove("search");
        }
    }
}
