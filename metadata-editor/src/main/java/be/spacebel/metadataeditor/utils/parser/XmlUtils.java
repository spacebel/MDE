package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.utils.validation.ValidatorErrorHandler;
import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.business.SearchException;
import be.spacebel.metadataeditor.business.ValidationException;
import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.configuration.Offering;
import be.spacebel.metadataeditor.models.configuration.OfferingOperation;
import be.spacebel.metadataeditor.models.workspace.Acquisition;
import be.spacebel.metadataeditor.models.workspace.Contact;
import be.spacebel.metadataeditor.models.workspace.Distribution;
import be.spacebel.metadataeditor.models.workspace.identification.GmxAnchor;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.models.workspace.distribution.OnlineResource;
import be.spacebel.metadataeditor.models.workspace.distribution.TransferOption;
import be.spacebel.metadataeditor.models.workspace.identification.Bbox;
import be.spacebel.metadataeditor.models.workspace.identification.Constraints;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.models.workspace.identification.ThesaurusKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.FreeKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.Keyword;
import be.spacebel.metadataeditor.models.configuration.VoidDataset;
import be.spacebel.metadataeditor.models.workspace.ContentInfo;
import be.spacebel.metadataeditor.models.workspace.Identification;
import be.spacebel.metadataeditor.models.workspace.identification.ServiceType;
import be.spacebel.metadataeditor.models.workspace.mission.Sponsor;
import be.spacebel.metadataeditor.models.workspace.identification.Temporal;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.HttpInvoker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class implements XML utilities
 *
 * @author mng
 */
public class XmlUtils implements Serializable {

    private final static Logger log = Logger.getLogger(XmlUtils.class);

    public static String getNodeContent(Node n) {
        String returnString = "";
        TransformerFactory transfac = TransformerFactory.newInstance();
        try {
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(n);
            trans.transform(source, result);
            returnString = sw.toString();
        } catch (IllegalArgumentException | TransformerException e) {
            log.error("XMLParserUtils.getNodeContent().error:" + e.getMessage());
        }
        return returnString;
    }

    public static String getNodeAttValue(Node node, String xpathStr, String attNs, String attName) {
        String value = null;
        Node n = XPathUtils.getNode(node, xpathStr);
        if (n != null) {
            value = getNodeAttValue(n, attNs, attName);
        }
        return value;
    }

    public static String getNodeAttValue(Node node, String attName) {
        String value = null;
        if (node.getAttributes() != null && node.getAttributes().getNamedItem(attName) != null) {
            value = node.getAttributes().getNamedItem(attName).getNodeValue();
        }
        return value;
    }

    public static String getNodeAttValue(Node node, String attNs, String attName) {
        String value = null;
        if (node.getAttributes() != null
                && node.getAttributes().getNamedItemNS(attNs, attName) != null) {
            value = node.getAttributes().getNamedItemNS(attNs, attName).getNodeValue();
        }
        return value;
    }

    /**
     *
     * @param xmlSource
     * @param schemaLocation
     * @return
     */
    public static Node buildNode(String xmlSource, URL schemaLocation) {
        //log.debug("buildNode " + xmlSource);
        if (StringUtils.isNotEmpty(xmlSource)) {
            DOMParser parser = new DOMParser();
            try {
                if (schemaLocation != null) {

                    XMLReader validator = XMLReaderFactory.createXMLReader();
                    validator.setFeature("http://xml.org/sax/features/validation",
                            true);
                    validator.setFeature(
                            "http://apache.org/xml/features/validation/schema",
                            true);
                    validator
                            .setProperty(
                                    "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                                    schemaLocation.toExternalForm());
                    // parser.setContentHandler(new IndexationHandler(this));
                    validator.parse(new InputSource(new StringReader(xmlSource)));

                }

                parser.parse(new InputSource(new StringReader(xmlSource)));
            } catch (IOException | SAXException e) {
                log.error(e);
            }
            Document doc = parser.getDocument();
            Node root = doc.getChildNodes().item(0);
            log.debug("build OK");
            return root;
        } else {
            return null;
        }

    }

    public static Node buildNode(String xmlSource) {
        return buildNode(xmlSource, null);
    }

    public static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException e) {
            log.error(e);
        }
        return sw.toString();
    }

    public static String getNodeValue(Node node) {
        String value = null;
        try {
            if (node.getFirstChild() != null) {
                value = node.getFirstChild().getNodeValue();
            } else {
                value = node.getTextContent();
            }

            if (value != null) {
                value = StringUtils.trimToEmpty(value);
            }
        } catch (DOMException e) {

        }
        return value;
    }

    public static Map<String, String> getNamespaces(Document doc, boolean prefixIsKey)
            throws IOException {
        Map<String, String> namespaces = new ConcurrentHashMap<>();
        NamedNodeMap atts = doc.getDocumentElement().getAttributes();
        if (atts != null) {
            for (int i = 0; i < atts.getLength(); i++) {
                Node node = atts.item(i);
                String attName = node.getNodeName().trim();
                String ns = node.getNodeValue();
                String prefix = "";
                if ("xmlns".equalsIgnoreCase(attName)) {
                    // this is the default namespace
                    if (Constants.OS_NAMESPACE.equalsIgnoreCase(ns)) {
                        prefix = Constants.OS_PREFIX;
                    } else {
                        log.debug("Unknow prefix for this namespace: " + ns);
                    }
                } else {
                    if (StringUtils.startsWithIgnoreCase(attName, "xmlns:")) {
                        prefix = StringUtils.substringAfter(attName, ":");
                    }
                }
                log.debug(prefix + " = " + ns);
                if (StringUtils.isNotEmpty(ns) && StringUtils.isNotEmpty(prefix)) {
                    if (prefixIsKey) {
                        namespaces.put(prefix, ns);
                    } else {
                        namespaces.put(ns, prefix);
                    }
                }

            }
        }
        return namespaces;
    }

    public static void setTextContent(Node node, String value) {
        if (value != null) {
            value = StringEscapeUtils.escapeXml11(unnscapeXml(value));
        }
        node.setTextContent(value);
    }

    public static String escapeXml(String value) {
        if (value != null) {
            value = StringEscapeUtils.escapeXml10(value);
        }
        return value;
    }

    public static String unnscapeXml(String value) {
        if (value != null) {
            value = StringEscapeUtils.unescapeXml(value);
        }
        return value;
    }

    public static Node createDoi(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:identifier xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
        sb.append("<gmd:RS_Identifier>");
        sb.append("<gmd:code>");
        sb.append("<gco:CharacterString>");
        sb.append(escapeXml(value));
        sb.append("</gco:CharacterString>");
        sb.append("</gmd:code>");
        sb.append("<gmd:codeSpace>");
        sb.append("<gco:CharacterString>");
        sb.append("http://doi.org");
        sb.append("</gco:CharacterString>");
        sb.append("</gmd:codeSpace>");
        sb.append("</gmd:RS_Identifier>");
        sb.append("</gmd:identifier>");

        log.debug("DOI " + sb.toString());
        return buildNode(sb.toString());
    }

    public static Node getDoiNodeRef(Node ciCitationNode) {
        NodeList nodeList = XPathUtils.getNodes(ciCitationNode, "./gmd:identifier");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(ciCitationNode, "./gmd:citedResponsibleParty");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(ciCitationNode, "./gmd:presentationForm");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        Node node = XPathUtils.getNode(ciCitationNode, "./gmd:series");
        if (node != null) {
            return node;
        }

        node = XPathUtils.getNode(ciCitationNode, "./gmd:otherCitationDetails");
        if (node != null) {
            return node;
        }

        node = XPathUtils.getNode(ciCitationNode, "./gmd:collectiveTitle");
        if (node != null) {
            return node;
        }

        node = XPathUtils.getNode(ciCitationNode, "./gmd:ISBN");
        if (node != null) {
            return node;
        }

        node = XPathUtils.getNode(ciCitationNode, "./gmd:ISSN");
        return node;
    }

    public static Node createOtherCitationDetails(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:otherCitationDetails xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
        sb.append("<gco:CharacterString>");
        sb.append(escapeXml(value));
        sb.append("</gco:CharacterString>");
        sb.append("</gmd:otherCitationDetails>");

        log.debug("DOI " + sb.toString());
        return buildNode(sb.toString());
    }

    public static Node getOtherCitationDetailsRefNode(Node ciCitationNode) {

        Node node = XPathUtils.getNode(ciCitationNode, "./gmd:collectiveTitle");
        if (node != null) {
            return node;
        }

        node = XPathUtils.getNode(ciCitationNode, "./gmd:ISBN");
        if (node != null) {
            return node;
        }

        node = XPathUtils.getNode(ciCitationNode, "./gmd:ISSN");
        return node;
    }

    public static Node createEdition(String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:edition xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
        sb.append("<gco:CharacterString>");
        sb.append(escapeXml(value));
        sb.append("</gco:CharacterString>");
        sb.append("</gmd:edition>");

        log.debug("EDITION " + sb.toString());
        return buildNode(sb.toString());
    }

    public static Node getEditionNodeRef(Node ciCitationNode) {
        Node node = XPathUtils.getNode(ciCitationNode, "./gmd:editionDate");
        if (node != null) {
            return node;
        }
        return getDoiNodeRef(ciCitationNode);
    }

    public static Node createBbox(Bbox bbox) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:extent xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
        sb.append("<gmd:EX_Extent>");
        sb.append("<gmd:geographicElement>");
        sb.append("<gmd:EX_GeographicBoundingBox>");
        sb.append("<gmd:westBoundLongitude>");
        sb.append("<gco:Decimal>").append(bbox.getWest()).append("</gco:Decimal>");
        sb.append("</gmd:westBoundLongitude>");
        sb.append("<gmd:eastBoundLongitude>");
        sb.append("<gco:Decimal>").append(bbox.getEast()).append("</gco:Decimal>");
        sb.append("</gmd:eastBoundLongitude>");
        sb.append("<gmd:southBoundLatitude>");
        sb.append("<gco:Decimal>").append(bbox.getSouth()).append("</gco:Decimal>");
        sb.append("</gmd:southBoundLatitude>");
        sb.append("<gmd:northBoundLatitude>");
        sb.append("<gco:Decimal>").append(bbox.getNorth()).append("</gco:Decimal>");
        sb.append("</gmd:northBoundLatitude>");
        sb.append("</gmd:EX_GeographicBoundingBox>");
        sb.append("</gmd:geographicElement>");
        sb.append("</gmd:EX_Extent>");
        sb.append("</gmd:extent>");

        return buildNode(sb.toString());
    }

    public static Node createTemporal(Temporal temporal) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:extent xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gml=\"" + Constants.GML_NS + "\">");
        sb.append("<gmd:EX_Extent>");
        sb.append("<gmd:temporalElement>");
        sb.append("<gmd:EX_TemporalExtent>");
        sb.append("<gmd:extent>");
        sb.append("<gml:TimePeriod gml:id=\"timeperiod1\">");
        sb.append("<gml:beginPosition>").append(temporal.getStartDate()).append("</gml:beginPosition>");
        sb.append("<gml:endPosition>").append(temporal.getEndDate()).append("</gml:endPosition>");
        sb.append("</gml:TimePeriod>");
        sb.append("</gmd:extent>");
        sb.append("</gmd:EX_TemporalExtent>");
        sb.append("</gmd:temporalElement>");
        sb.append("</gmd:EX_Extent>");
        sb.append("</gmd:extent>");

        return buildNode(sb.toString());
    }

    public static Node createEarthTopicsNode(List<EarthTopic> earthTopics,
            VoidDataset earthTopicThesaurus, boolean serviceMetadata) {
        return buildNode(buildEarthTopics(earthTopics, earthTopicThesaurus, serviceMetadata));
    }

    private static String buildEarthTopics(List<EarthTopic> earthTopics,
            VoidDataset earthTopicDataset, boolean serviceMetadata) {
        StringBuilder sb = new StringBuilder();
        if (earthTopics != null && earthTopics.size() > 0) {
            if (serviceMetadata) {
                sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                        .append(Constants.GMD_NS)
                        .append("\" xmlns:gco=\"")
                        .append(Constants.GCO_NS)
                        .append("\">");
            } else {
                sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                        .append(Constants.GMD_NS)
                        .append("\" xmlns:gco=\"")
                        .append(Constants.GCO_NS)
                        .append("\" xmlns:gmx=\"")
                        .append(Constants.GMX_NS)
                        .append("\" xmlns:xlink=\"")
                        .append(Constants.XLINK_NS).append("\">");
            }

            sb.append("<gmd:MD_Keywords>");
            for (EarthTopic eTopic : earthTopics) {
                if (StringUtils.isNotEmpty(eTopic.getUri())
                        && StringUtils.isNotEmpty(eTopic.getLabel())) {
                    sb.append("<gmd:keyword>");
                    if (serviceMetadata) {
                        sb.append("<gco:CharacterString>")
                                .append(escapeXml(eTopic.getLabel()))
                                .append("</gco:CharacterString>");
                    } else {
                        sb.append("<gmx:Anchor xlink:href=\"")
                                .append(escapeXml(eTopic.getUri()))
                                .append("\">")
                                .append(escapeXml(eTopic.getLabel()))
                                .append("</gmx:Anchor>");
                    }
                    sb.append("</gmd:keyword>");
                }
            }
            sb.append("<gmd:thesaurusName>");
            sb.append("<gmd:CI_Citation>");
            sb.append("<gmd:title>");
            if (serviceMetadata) {
                sb.append("<gco:CharacterString>")
                        .append(escapeXml(earthTopicDataset.getFullTitle()))
                        .append("</gco:CharacterString>");
            } else {
                sb.append("<gmx:Anchor xlink:href=\"")
                        .append(earthTopicDataset.getUri())
                        .append("\">")
                        .append(earthTopicDataset.getFullTitle());
                sb.append("</gmx:Anchor>");
            }

            sb.append("</gmd:title>");
            sb.append("<gmd:date>");
            sb.append("<gmd:CI_Date>");
            sb.append("<gmd:date>");
            sb.append("<gco:Date>")
                    .append(earthTopicDataset.getModified())
                    .append("</gco:Date>");
            sb.append("</gmd:date>");
            sb.append("<gmd:dateType>");
            sb.append("<gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"publication\">publication</gmd:CI_DateTypeCode>");
            sb.append("</gmd:dateType>");
            sb.append("</gmd:CI_Date>");
            sb.append("</gmd:date>");
            sb.append("</gmd:CI_Citation>");
            sb.append("</gmd:thesaurusName>");
            sb.append("</gmd:MD_Keywords>");
            sb.append("</gmd:descriptiveKeywords>");
        }
        return sb.toString();
    }

    public static Node createScienceKeywordNode(List<Concept> scKeywords,
            VoidDataset scKeywordThesaurus, boolean serviceMetadata) {
        return buildNode(buildScienceKeywords(scKeywords, scKeywordThesaurus, serviceMetadata));
    }

    private static String buildScienceKeywords(List<Concept> scKeywords,
            VoidDataset scKeywordThesaurus, boolean serviceMetadata) {
        StringBuilder sb = new StringBuilder();
        if (scKeywords != null && scKeywords.size() > 0) {
            if (serviceMetadata) {
                sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                        .append(Constants.GMD_NS)
                        .append("\" xmlns:gco=\"")
                        .append(Constants.GCO_NS)
                        .append("\">");
            } else {
                sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                        .append(Constants.GMD_NS)
                        .append("\" xmlns:gco=\"")
                        .append(Constants.GCO_NS)
                        .append("\" xmlns:gmx=\"")
                        .append(Constants.GMX_NS)
                        .append("\" xmlns:xlink=\"")
                        .append(Constants.XLINK_NS).append("\">");
            }

            sb.append("<gmd:MD_Keywords>");
            scKeywords.forEach((concept) -> {
                String kwLabel = MetadataUtils.buildScKwLabel(concept);
                if (StringUtils.isNotEmpty(kwLabel)
                        && StringUtils.isNotEmpty(concept.getUri())) {
                    sb.append("<gmd:keyword>");
                    if (serviceMetadata) {
                        sb.append("<gco:CharacterString>")
                                .append(escapeXml(kwLabel))
                                .append("</gco:CharacterString>");
                    } else {
                        sb.append("<gmx:Anchor xlink:href=\"")
                                .append(concept.getUri())
                                .append("\">")
                                .append(escapeXml(kwLabel))
                                .append("</gmx:Anchor>");
                    }

                    sb.append("</gmd:keyword>");
                }
            });

            sb.append("<gmd:type>");
            sb.append("<gmd:MD_KeywordTypeCode codeList=\"theme\" codeListValue=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode\"/>");
            sb.append("</gmd:type>");

            sb.append("<gmd:thesaurusName>");
            sb.append("<gmd:CI_Citation>");
            sb.append("<gmd:title>");
            if (serviceMetadata) {
                sb.append("<gco:CharacterString>")
                        .append(escapeXml(scKeywordThesaurus.getFullTitle()))
                        .append("</gco:CharacterString>");
            } else {
                sb.append("<gmx:Anchor xlink:href=\"")
                        .append(scKeywordThesaurus.getUri())
                        .append("\">")
                        .append(scKeywordThesaurus.getFullTitle());
                sb.append("</gmx:Anchor>");
            }

            sb.append("</gmd:title>");

            sb.append("<gmd:date>");
            sb.append("<gmd:CI_Date>");
            sb.append("<gmd:date>");
            sb.append("<gco:Date>")
                    .append(scKeywordThesaurus.getModified())
                    .append("</gco:Date>");
            sb.append("</gmd:date>");
            sb.append("<gmd:dateType>");
            sb.append("<gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"publication\">publication</gmd:CI_DateTypeCode>");
            sb.append("</gmd:dateType>");
            sb.append("</gmd:CI_Date>");
            sb.append("</gmd:date>");
            sb.append("</gmd:CI_Citation>");
            sb.append("</gmd:thesaurusName>");

            sb.append("</gmd:MD_Keywords>");
            sb.append("</gmd:descriptiveKeywords>");
        }
        return sb.toString();
    }

    public static Node createServiceKeywordNode(List<String> keywords, VoidDataset thesaurus) {
        return buildNode(buildServiceKeywords(keywords, thesaurus));
    }

    private static String buildServiceKeywords(List<String> keywords, VoidDataset thesaurus) {
        StringBuilder sb = new StringBuilder();

        sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                .append(Constants.GMD_NS)
                .append("\" xmlns:gco=\"")
                .append(Constants.GCO_NS)
                .append("\">");

        sb.append("<gmd:MD_Keywords>");
        keywords.forEach((kwLabel) -> {
            sb.append("<gmd:keyword>");
            sb.append("<gco:CharacterString>")
                    .append(escapeXml(kwLabel))
                    .append("</gco:CharacterString>");

            sb.append("</gmd:keyword>");
        });

        sb.append("<gmd:type>");
        sb.append("<gmd:MD_KeywordTypeCode codeList=\"theme\" codeListValue=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode\"/>");
        sb.append("</gmd:type>");

        sb.append("<gmd:thesaurusName>");
        sb.append("<gmd:CI_Citation>");
        sb.append("<gmd:title>");

        sb.append("<gco:CharacterString>")
                .append(escapeXml(thesaurus.getFullTitle()))
                .append("</gco:CharacterString>");

        sb.append("</gmd:title>");

        sb.append("<gmd:date>");
        sb.append("<gmd:CI_Date>");
        sb.append("<gmd:date>");
        sb.append("<gco:Date>")
                .append(thesaurus.getModified())
                .append("</gco:Date>");
        sb.append("</gmd:date>");
        sb.append("<gmd:dateType>");
        sb.append("<gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"publication\">publication</gmd:CI_DateTypeCode>");
        sb.append("</gmd:dateType>");
        sb.append("</gmd:CI_Date>");
        sb.append("</gmd:date>");
        sb.append("</gmd:CI_Citation>");
        sb.append("</gmd:thesaurusName>");

        sb.append("</gmd:MD_Keywords>");
        sb.append("</gmd:descriptiveKeywords>");

        return sb.toString();
    }

    public static Node createEopKeywordNode(ThesaurusKeyword eopKw, boolean serviceMetedata) {
        return buildNode(buildEopKeyword(eopKw, serviceMetedata));
    }

    private static String buildEopKeyword(ThesaurusKeyword eopKw, boolean serviceMetedata) {
        StringBuilder sb = new StringBuilder();

        if (serviceMetedata) {
            sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                    .append(Constants.GMD_NS)
                    .append("\" xmlns:gco=\"")
                    .append(Constants.GCO_NS)
                    .append("\">");
        } else {
            sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                    .append(Constants.GMD_NS)
                    .append("\" xmlns:gco=\"")
                    .append(Constants.GCO_NS)
                    .append("\" xmlns:gmx=\"")
                    .append(Constants.GMX_NS)
                    .append("\" xmlns:xlink=\"")
                    .append(Constants.XLINK_NS).append("\">");
        }

        sb.append("<gmd:MD_Keywords>");
        for (Keyword kw : eopKw.getKeywords()) {
            if (StringUtils.isNotEmpty(kw.getLabel())) {
                sb.append("<gmd:keyword>");
                if (serviceMetedata) {
                    sb.append("<gco:CharacterString>")
                            .append(escapeXml(kw.getLabel()))
                            .append("</gco:CharacterString>");
                } else {
                    String uri = kw.getUri();
                    if (StringUtils.isEmpty(uri)) {
                        uri = eopKw.getThesaurus().getTitleUri() + eopKw.getCodeListValue();
                    }
                    sb.append("<gmx:Anchor xlink:href=\"")
                            .append(uri)
                            .append("\">")
                            .append(escapeXml(kw.getLabel()))
                            .append("</gmx:Anchor>");
                }
                sb.append("</gmd:keyword>");
            }
        }

        sb.append("<gmd:type>");
        sb.append("<gmd:MD_KeywordTypeCode codeList=\"")
                .append(eopKw.getThesaurus().getKeywordType())
                .append("\" codeListValue=\"")
                .append(eopKw.getCodeListValue())
                .append("\"/>");
        sb.append("</gmd:type>");

        sb.append("<gmd:thesaurusName>");
        sb.append("<gmd:CI_Citation>");
        sb.append("<gmd:title>");
        if (serviceMetedata) {
            sb.append("<gco:CharacterString>")
                    .append(escapeXml(eopKw.getThesaurus().getTitle()))
                    .append("</gco:CharacterString>");
        } else {
            sb.append("<gmx:Anchor xlink:href=\"")
                    .append(eopKw.getThesaurus().getTitleUri())
                    .append("\">")
                    .append(escapeXml(eopKw.getThesaurus().getTitle()));
            sb.append("</gmx:Anchor>");
        }

        sb.append("</gmd:title>");

        sb.append("<gmd:date>");
        sb.append("<gmd:CI_Date>");
        sb.append("<gmd:date>");
        sb.append("<gco:Date>")
                .append(eopKw.getThesaurus().getDate())
                .append("</gco:Date>");
        sb.append("</gmd:date>");
        sb.append("<gmd:dateType>");
        sb.append("<gmd:CI_DateTypeCode codeList=\"").append(eopKw.getThesaurus().getDateTypeCodeList())
                .append("\" codeListValue=\"").append(eopKw.getThesaurus().getDateTypeCodeListValue())
                .append("\">").append(eopKw.getThesaurus().getDateType()).append("</gmd:CI_DateTypeCode>");
        sb.append("</gmd:dateType>");
        sb.append("</gmd:CI_Date>");
        sb.append("</gmd:date>");

        sb.append("</gmd:CI_Citation>");
        sb.append("</gmd:thesaurusName>");

        sb.append("</gmd:MD_Keywords>");
        sb.append("</gmd:descriptiveKeywords>");
        return sb.toString();
    }

    public static Node createFreeKeywordNode(FreeKeyword freeKeyword) {
        //String freeKw = buildFreeKeyword(freeKeyword);
        //System.out.println(freeKw);
        return buildNode(buildFreeKeyword(freeKeyword));
    }

    private static String buildFreeKeyword(FreeKeyword freeKeyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                .append(Constants.GMD_NS)
                .append("\" xmlns:gco=\"")
                .append(Constants.GCO_NS).append("\">");
        sb.append("<gmd:MD_Keywords>");
        for (Keyword kw : freeKeyword.getKeywords()) {
            if (StringUtils.isNotEmpty(kw.getLabel())) {
                sb.append("<gmd:keyword>");
                sb.append("<gco:CharacterString>");
                sb.append(escapeXml(kw.getLabel()));
                sb.append("</gco:CharacterString>");
                sb.append("</gmd:keyword>");
            }
        }
        if (StringUtils.isNotEmpty(freeKeyword.getCodeListValue())) {
            sb.append("<gmd:type>");
            sb.append("<gmd:MD_KeywordTypeCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_KeywordTypeCode\" codeListValue=\"")
                    .append(freeKeyword.getCodeListValue())
                    .append("\">");
            sb.append(freeKeyword.getCodeListValue());
            sb.append("</gmd:MD_KeywordTypeCode>");
            sb.append("</gmd:type>");
        }
        sb.append("</gmd:MD_Keywords>");
        sb.append("</gmd:descriptiveKeywords>");
        return sb.toString();
    }

    public static Node createContentInfoNode(ContentInfo contentInfo, boolean serviceMetadata) {
        StringBuilder sb = new StringBuilder();
        if (serviceMetadata) {
            sb.append("<gmd:contentInfo xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">");
            sb.append("<gmd:MD_ImageDescription>");
        } else {
            sb.append("<gmd:contentInfo xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gmi=\"http://www.isotc211.org/2005/gmi\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">");
            sb.append("<gmi:MI_ImageDescription>");
        }

        sb.append("<gmd:attributeDescription/>");
        sb.append("<gmd:contentType/>");
        sb.append("<gmd:processingLevelCode>");
        sb.append("<gmd:RS_Identifier>");
        sb.append("<gmd:code>");
        sb.append("<gco:CharacterString>");
        sb.append(escapeXml(contentInfo.getProcessingLevel().getLabel()));
        sb.append("</gco:CharacterString>");
        sb.append("</gmd:code>");
        sb.append("</gmd:RS_Identifier>");
        sb.append("</gmd:processingLevelCode>");

        if (serviceMetadata) {
            sb.append("</gmd:MD_ImageDescription>");
        } else {
            sb.append("</gmi:MI_ImageDescription>");
        }

        sb.append("</gmd:contentInfo>");
        return buildNode(sb.toString());
    }

    public static Node createSpatialDataServiceCategoryNode(FreeKeyword sdsCategory, VoidDataset sdsCatDataset) {
        return buildNode(buildSpatialDataServiceCategory(sdsCategory, sdsCatDataset));
    }

    private static String buildSpatialDataServiceCategory(FreeKeyword sdsCategory, VoidDataset sdsCatDataset) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                .append(Constants.GMD_NS)
                .append("\" xmlns:gco=\"")
                .append(Constants.GCO_NS)
                .append("\">");
        sb.append("<gmd:MD_Keywords>");
        for (Keyword kw : sdsCategory.getKeywords()) {
            if (StringUtils.isNotEmpty(kw.getLabel())) {
                sb.append("<gmd:keyword>");
                sb.append("<gco:CharacterString>");
                sb.append(escapeXml(kw.getLabel()));
                sb.append("</gco:CharacterString>");
                sb.append("</gmd:keyword>");
            }
        }

        sb.append("<gmd:thesaurusName>");
        sb.append("<gmd:CI_Citation>");
        sb.append("<gmd:title>");
        sb.append("<gco:CharacterString>")
                .append(escapeXml(sdsCatDataset.getFullTitle()))
                .append("</gco:CharacterString>");
        sb.append(" </gmd:title>");

        sb.append("<gmd:date>");
        sb.append("<gmd:CI_Date>");
        sb.append("<gmd:date>");
        sb.append("<gco:Date>")
                .append(sdsCatDataset.getModified())
                .append("</gco:Date>");
        sb.append("</gmd:date>");

        sb.append("<gmd:dateType>");
        sb.append("<gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"publication\"/>");
        sb.append("</gmd:dateType>");

        sb.append("</gmd:CI_Date>");
        sb.append("</gmd:date>");

        sb.append("</gmd:CI_Citation>");
        sb.append("</gmd:thesaurusName>");
        sb.append("</gmd:MD_Keywords>");
        sb.append("</gmd:descriptiveKeywords>");
        //log.debug("buildSpatialDataServiceCategory = " + sb.toString());
        return sb.toString();
    }

    public static boolean hasKeyword(FreeKeyword freeKw) {
        boolean hasKw = false;
        if (freeKw != null
                && freeKw.getKeywords() != null
                && freeKw.getKeywords().size() > 0) {
            for (Keyword kw : freeKw.getKeywords()) {
                if (StringUtils.isNotEmpty(kw.getLabel())) {
                    hasKw = true;
                    break;
                }
            }
        }
        return hasKw;
    }

    public static boolean hasKeyword(ThesaurusKeyword eopKw) {
        if (eopKw != null
                && eopKw.getKeywords() != null
                && eopKw.getKeywords().size() > 0) {
            if (eopKw.getKeywords().stream().anyMatch((kw) -> (StringUtils.isNotEmpty(kw.getLabel())))) {
                return true;
            }
        }
        return false;
    }

    public static Node createMDLegalConstraints(Constraints constraint, boolean serviceMetadata) {
        String mdLegalConstraints = buildMDLegalConstraints(constraint, true, serviceMetadata);
        if (mdLegalConstraints != null) {
            return buildNode(mdLegalConstraints);
        } else {
            return null;
        }
    }

    public static Node createResourceConstraints(Constraints constraint, boolean serviceMetadata) {
        String mdLegalConstraints = buildMDLegalConstraints(constraint, false, serviceMetadata);
        if (mdLegalConstraints != null) {
            StringBuilder sb = new StringBuilder();
            if (serviceMetadata) {
                sb.append("<gmd:resourceConstraints xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
            } else {
                sb.append("<gmd:resourceConstraints xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\"  xmlns:gmx=\"" + Constants.GMX_NS + "\"  xmlns:xlink=\"" + Constants.XLINK_NS + "\">");
            }
            sb.append(mdLegalConstraints);
            sb.append("</gmd:resourceConstraints>");

            return buildNode(sb.toString());
        } else {
            return null;
        }

    }

    private static String buildMDLegalConstraints(Constraints constraint, boolean ns, boolean serviceMetadata) {
        if ((constraint.getUseLimitations() != null && constraint.getUseLimitations().size() > 0)
                || (constraint.getAccesses() != null && constraint.getAccesses().size() > 0)
                || (constraint.getUses() != null && constraint.getUses().size() > 0)
                || (constraint.getOthers() != null && constraint.getOthers().size() > 0)) {

            StringBuilder sb = new StringBuilder();
            if (ns) {
                if (serviceMetadata) {
                    sb.append("<gmd:MD_LegalConstraints xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
                } else {
                    sb.append("<gmd:MD_LegalConstraints xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\"  xmlns:gmx=\"" + Constants.GMX_NS + "\"  xmlns:xlink=\"" + Constants.XLINK_NS + "\">");
                }
            } else {
                sb.append("<gmd:MD_LegalConstraints>");
            }

            if (constraint.getUseLimitations() != null && constraint.getUseLimitations().size() > 0) {
                constraint.getUseLimitations().forEach((sp) -> {
                    if (serviceMetadata) {
                        String text = "";
                        if (StringUtils.isNotEmpty(sp.getLink())
                                && StringUtils.isNotEmpty(sp.getText())) {
                            text = sp.getText() + " " + sp.getLink();
                        } else {
                            text = StringUtils.isNotEmpty(sp.getText()) ? sp.getText() : sp.getLink();
                        }
                        if (StringUtils.isNotEmpty(text)) {
                            sb.append("<gmd:useLimitation>");
                            sb.append("<gco:CharacterString>").append(escapeXml(text)).append("</gco:CharacterString>");
                            sb.append("</gmd:useLimitation>");
                        }
                    } else {
                        if (StringUtils.isNotEmpty(sp.getLink()) && StringUtils.isNotEmpty(sp.getText())) {
                            sb.append("<gmd:useLimitation>");
                            sb.append("<gmx:Anchor xlink:href=\"").append(escapeXml(sp.getLink())).append("\">");
                            sb.append(escapeXml(sp.getText()));
                            sb.append("</gmx:Anchor>");
                            sb.append("</gmd:useLimitation>");
                        } else {
                            if (StringUtils.isNotEmpty(sp.getText())) {
                                sb.append("<gmd:useLimitation>");
                                sb.append("<gco:CharacterString>").append(escapeXml(sp.getText())).append("</gco:CharacterString>");
                                sb.append("</gmd:useLimitation>");
                            }
                        }
                    }
                });
            }

            if (constraint.getAccesses() != null
                    && constraint.getAccesses().size() > 0) {
                constraint.getAccesses().stream().map((sp) -> {
                    sb.append("<gmd:accessConstraints>");
                    sb.append("<gmd:MD_RestrictionCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode\" codeListValue=\"").append(escapeXml(sp.getText())).append("\"/>");
                    return sp;
                }).forEachOrdered((_item) -> {
                    sb.append("</gmd:accessConstraints>");
                });
            }

            if (constraint.getUses() != null && constraint.getUses().size() > 0) {
                constraint.getUses().stream().map((sp) -> {
                    sb.append("<gmd:useConstraints>");
                    sb.append("<gmd:MD_RestrictionCode codeList=\"http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_RestrictionCode\" codeListValue=\"").append(escapeXml(sp.getText())).append("\"/>");
                    return sp;
                }).forEachOrdered((_item) -> {
                    sb.append("</gmd:useConstraints>");
                });
            }

            if (constraint.getOthers() != null && constraint.getOthers().size() > 0) {
                constraint.getOthers().forEach((sp) -> {
                    if (serviceMetadata) {
                        String text = "";
                        if (StringUtils.isNotEmpty(sp.getLink())
                                && StringUtils.isNotEmpty(sp.getText())) {
                            text = sp.getText() + " " + sp.getLink();
                        } else {
                            text = StringUtils.isNotEmpty(sp.getText()) ? sp.getText() : sp.getLink();
                        }
                        if (StringUtils.isNotEmpty(text)) {
                            sb.append("<gmd:otherConstraints>");
                            sb.append("<gco:CharacterString>").append(escapeXml(text)).append("</gco:CharacterString>");
                            sb.append("</gmd:otherConstraints>");
                        }
                    } else {
                        if (StringUtils.isNotEmpty(sp.getLink()) && StringUtils.isNotEmpty(sp.getText())) {
                            sb.append("<gmd:otherConstraints>");
                            sb.append("<gmx:Anchor xlink:href=\"").append(escapeXml(sp.getLink())).append("\">");
                            sb.append(sp.getText());
                            sb.append("</gmx:Anchor>");
                            sb.append("</gmd:otherConstraints>");
                        } else {
                            if (StringUtils.isNotEmpty(sp.getText())) {
                                sb.append("<gmd:otherConstraints>");
                                sb.append("<gco:CharacterString>").append(escapeXml(sp.getText())).append("</gco:CharacterString>");
                                sb.append("</gmd:otherConstraints>");
                            }
                        }
                    }
                });
            }

            sb.append("</gmd:MD_LegalConstraints>");

            log.debug("MDLegalConstraints " + sb.toString());
            return sb.toString();
        } else {
            log.debug("No MDLegalConstraints ");
            return null;
        }
    }

    public static Node createDistributionInfo(Distribution distribution) {
        if (distribution.getTransferOptions() != null && distribution.getTransferOptions().size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("<gmd:distributionInfo xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
            sb.append("<gmd:MD_Distribution>");

            distribution.getTransferOptions().forEach((tfOption) -> {
                sb.append(buildTransferOptions(tfOption, false));
            });

            sb.append("</gmd:MD_Distribution>");
            sb.append("</gmd:distributionInfo>");

            log.debug("DistributionInfo: " + sb.toString());

            return buildNode(sb.toString());
        } else {
            return null;
        }

    }

    public static Node createTransferOptions(TransferOption tfOption) {
        return buildNode(buildTransferOptions(tfOption, true));
    }

    private static String buildTransferOptions(TransferOption tfOption, boolean ns) {
        StringBuilder sb = new StringBuilder();
        if (ns) {
            sb.append("<gmd:transferOptions xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\">");
        } else {
            sb.append("<gmd:transferOptions>");
        }
        sb.append("<gmd:MD_DigitalTransferOptions>");

        if (StringUtils.isNotEmpty(tfOption.getUnits())) {
            sb.append("<gmd:unitsOfDistribution>");
            sb.append("<gco:CharacterString>").append(escapeXml(tfOption.getUnits())).append("</gco:CharacterString>");
            sb.append("</gmd:unitsOfDistribution>");
        }

        if (StringUtils.isNotEmpty(tfOption.getSize())) {
            sb.append("<gmd:transferSize>");
            sb.append("<gco:Real>").append(escapeXml(tfOption.getSize())).append("</gco:Real>");
            sb.append("</gmd:transferSize>");
        }

        if (tfOption.getOnlineRses() != null
                && tfOption.getOnlineRses().size() > 0) {
            for (OnlineResource onlineRs : tfOption.getOnlineRses()) {
                sb.append(buildOnlineResource(onlineRs, false));
            }
        }

//        if (tfOption.getOfferingResources() != null 
//                && tfOption.getOfferingResources().size() > 0) {
//            for (OnlineResource onlineRs : tfOption.getOfferingResources()) {
//                sb.append(buildOnlineResource(onlineRs, false));
//            }
//        }
        sb.append("");
        sb.append("</gmd:MD_DigitalTransferOptions>");
        sb.append("</gmd:transferOptions>");
        return sb.toString();
    }

    public static Node createOnlineResourceNode(OnlineResource onlineRs) {
        return buildNode(buildOnlineResource(onlineRs, true));
    }

    private static String buildOnlineResource(OnlineResource onlineRs, boolean ns) {
        StringBuilder sb = new StringBuilder();
        String rsLink = "";
        if (StringUtils.isNotEmpty(onlineRs.getRelatedField())) {
            rsLink = "xlink:type=\"simple\" xlink:href=\"xpointer(" + onlineRs.getRelatedField() + ")\"";
        }

        if (ns) {
            sb.append("<gmd:onLine ").append(rsLink)
                    .append(" xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\" xmlns:xlink=\"")
                    .append(Constants.XLINK_NS).append("\">");
        } else {
            if (StringUtils.isNotEmpty(rsLink)) {
                sb.append("<gmd:onLine ").append(rsLink).append(">");
            } else {
                sb.append("<gmd:onLine>");
            }

        }

        sb.append("<gmd:CI_OnlineResource>");
        sb.append("<gmd:linkage>");
        sb.append("<gmd:URL>").append(escapeXml(onlineRs.getLinkage())).append("</gmd:URL>");
        sb.append("</gmd:linkage>");

        if (StringUtils.isNotEmpty(onlineRs.getProtocol())) {
            sb.append("<gmd:protocol>");
            sb.append("<gco:CharacterString>").append(escapeXml(onlineRs.getProtocol())).append("</gco:CharacterString>");
            sb.append("</gmd:protocol>");
        }

        if (StringUtils.isNotEmpty(onlineRs.getAppProfile())) {
            sb.append("<gmd:applicationProfile>");
            sb.append("<gco:CharacterString>").append(escapeXml(onlineRs.getAppProfile())).append("</gco:CharacterString>");
            sb.append("</gmd:applicationProfile>");
        }

        if (StringUtils.isNotEmpty(onlineRs.getName())) {
            sb.append("<gmd:name>");
            sb.append("<gco:CharacterString>").append(escapeXml(onlineRs.getName())).append("</gco:CharacterString>");
            sb.append("</gmd:name>");
        }

        if (StringUtils.isNotEmpty(onlineRs.getDescription())) {
            sb.append("<gmd:description>");
            sb.append("<gco:CharacterString>").append(escapeXml(onlineRs.getDescription())).append("</gco:CharacterString>");
            sb.append("</gmd:description>");
        }

        if (StringUtils.isNotEmpty(onlineRs.getFunction())) {
            sb.append("<gmd:function>");
            sb.append("<gmd:CI_OnLineFunctionCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_OnLineFunctionCode\" codeListValue=\"").append(escapeXml(onlineRs.getFunction())).append("\"/>");
            sb.append("</gmd:function>");
        }

        sb.append("</gmd:CI_OnlineResource>");
        sb.append("</gmd:onLine>");

        return sb.toString();
    }

    public static Node createAcquisitionNode(Acquisition acquisition) {
        return buildNode(buildAcquisitionInformation(acquisition));
    }

    private static String buildAcquisitionInformation(Acquisition acquisition) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmi:acquisitionInformation xmlns:gmi=\"" + Constants.GMI_NS + "\" xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\"  xmlns:gmx=\"" + Constants.GMX_NS + "\"  xmlns:xlink=\"" + Constants.XLINK_NS + "\">");
        sb.append("<gmi:MI_AcquisitionInformation>");

        if (acquisition.getPlatforms() != null) {
            for (Platform platform : acquisition.getPlatforms()) {
                sb.append(buildPlatform(platform, false));
            }
        }

        sb.append("</gmi:MI_AcquisitionInformation>");
        sb.append("</gmi:acquisitionInformation>");
        return sb.toString();
    }

    public static Node createPlatformNode(Platform platform) {
        return buildNode(buildPlatform(platform, true));
    }

    private static String buildPlatform(Platform platform, boolean ns) {
        StringBuilder sb = new StringBuilder();
        if (ns) {
            sb.append("<gmi:platform xmlns:gmi=\"" + Constants.GMI_NS + "\" xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\"  xmlns:gmx=\"" + Constants.GMX_NS + "\"  xmlns:xlink=\"" + Constants.XLINK_NS + "\">");
        } else {
            sb.append("<gmi:platform>");
        }
        sb.append("<gmi:MI_Platform>");

        sb.append("<gmi:citation>");
        sb.append("<gmd:CI_Citation>");

        /*
            gmd:title
         */
        sb.append(buildSP(platform.getUri(), platform.getLabel(), "gmd:title"));

        /*
            gmd:alternateTitle
         */
        if (platform.getGcmd() != null
                && StringUtils.isNotEmpty(platform.getGcmd().getLabel())) {
            sb.append(buildSP(platform.getGcmd().getUri(), platform.getGcmd().getLabel(), "gmd:alternateTitle"));
        } else {
            if (platform.getAltTitle() != null
                    && StringUtils.isNotEmpty(platform.getAltTitle().getText())) {
                sb.append(buildSP(platform.getAltTitle(), "gmd:alternateTitle"));
            }
        }

        /*
            Platform launch date
         */
        sb.append(buildLaunchDate(CommonUtils.dateToStr(platform.getLaunchDate()), false));

        sb.append("</gmd:CI_Citation>");
        sb.append("</gmi:citation>");

        /*
            gmi:identifier
         */
        sb.append("<gmi:identifier>");
        sb.append("<gmd:MD_Identifier>");
        sb.append(buildSP(platform.getUri(), platform.getLabel(), "gmd:code"));
        sb.append("</gmd:MD_Identifier>");
        sb.append("</gmi:identifier>");

        /*
            gmi:description
         */
        if (platform.getGcmd() != null) {
            sb.append(buildSP("", platform.getGcmd().getPropertyValue(Constants.GCMD_ALTLABEL), "gmi:description"));
        } else {
            if (platform.getDescription() != null
                    && StringUtils.isNotEmpty(platform.getDescription().getText())) {
                sb.append(buildSP(platform.getDescription(), "gmi:description"));
            } else {
                sb.append(buildSP("", "", "gmi:description"));
            }
        }

        /*
            gmi:sponsor
         */
        if (platform.getOperators() != null
                && !platform.getOperators().isEmpty()) {
            platform.getOperators().forEach((sps) -> {
                sb.append(buildSponsor(sps, false));
            });
        }

        if (platform.getInstruments() != null
                && !platform.getInstruments().isEmpty()) {
            platform.getInstruments().forEach((inst) -> {
                sb.append(buildInstrument(inst, false));
            });

//            platform.getInstruments().stream().map((instrument) -> {
//                sb.append("<gmi:instrument>");
//                sb.append("<gmi:MI_Instrument>");
//                sb.append("<gmi:citation>");
//                sb.append("<gmd:CI_Citation xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">");
//                /*
//                gmd:title
//                 */
//                sb.append(buildSP(instrument.getUri(), instrument.getLabel(), "gmd:title"));
//                return instrument;
//            }).map((instrument) -> {
//                /*
//                gmd:alternateTitle
//                 */
//                if (instrument.getGcmd() != null
//                        && StringUtils.isNotEmpty(instrument.getGcmd().getLabel())) {
//                    sb.append(buildSP(instrument.getGcmd().getUri(), instrument.getGcmd().getLabel(), "gmd:alternateTitle"));
//                }
//                return instrument;
//            }).map((instrument) -> {
//                /*
//                gmd:date
//                 */
//                sb.append("<gmd:date gco:nilReason=\"unknown\"/>");
//                /*
//                gmd:identifier
//                 */
//                sb.append("<gmd:identifier>");
//                sb.append("<gmd:MD_Identifier>");
//                sb.append(buildSP(instrument.getUri(), instrument.getLabel(), "gmd:code"));
//                return instrument;
//            }).map((instrument) -> {
//                sb.append("</gmd:MD_Identifier>");
//                sb.append("</gmd:identifier>");
//                sb.append("</gmd:CI_Citation>");
//                sb.append("</gmi:citation>");
//                sb.append("<gmi:type>");
//                sb.append("<gmi:MI_SensorTypeCode/>");
//                sb.append("</gmi:type>");
//                /*
//                gmi:description
//                 */
//                if (instrument.getGcmd() != null) {
//                    sb.append(buildSP("", instrument.getGcmd().getPropertyValue(Constants.GCMD_ALTLABEL), "gmi:description"));
//                }
//                return instrument;
//            }).map((_item) -> {
//                sb.append("</gmi:MI_Instrument>");
//                return _item;
//            }).forEachOrdered((_item) -> {
//                sb.append("</gmi:instrument>");
//            });
        } else {
            sb.append("<gmi:instrument/>");
        }
        sb.append("</gmi:MI_Platform>");
        sb.append("</gmi:platform>");

        return sb.toString();
    }

    public static Node createAlternateTitle(Concept gcmd) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:alternateTitle xmlns:gmd=\"" + Constants.GMD_NS + "\" xmlns:gmx=\"" + Constants.GMX_NS + "\"  xmlns:xlink=\"" + Constants.XLINK_NS + "\">");
        sb.append("<gmx:Anchor xlink:href=\"").append(escapeXml(gcmd.getUri())).append("\">");
        if (StringUtils.isNotEmpty(gcmd.getLabel())) {
            sb.append(escapeXml(gcmd.getLabel()));
        }
        sb.append("</gmx:Anchor>");
        sb.append("</gmd:alternateTitle>");
        //log.debug("MNG MNG MNG " + sb.toString());
        return buildNode(sb.toString());
    }

    public static Node createInstrumentNode(Instrument instrument) {
        return buildNode(buildInstrument(instrument, true));
    }

    private static String buildInstrument(Instrument instrument, boolean ns) {
        StringBuilder sb = new StringBuilder();
        if (ns) {
            sb.append("<gmi:instrument xmlns:gmi=\"" + Constants.GMI_NS + "\" xmlns:gmd=\"" + Constants.GMD_NS + "\"  xmlns:gco=\"" + Constants.GCO_NS + "\"  xmlns:gmx=\"" + Constants.GMX_NS + "\"  xmlns:xlink=\"" + Constants.XLINK_NS + "\">");
        } else {
            sb.append("<gmi:instrument>");
        }
        sb.append("<gmi:MI_Instrument>");

        sb.append("<gmi:citation>");
        sb.append("<gmd:CI_Citation xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">");
        /*
                gmd:title
         */
        sb.append(buildSP(instrument.getUri(), instrument.getLabel(), "gmd:title"));

        /*
                gmd:alternateTitle
         */
        if (instrument.getGcmd() != null
                && StringUtils.isNotEmpty(instrument.getGcmd().getLabel())) {
            sb.append(buildSP(instrument.getGcmd().getUri(),
                    instrument.getGcmd().getLabel(), "gmd:alternateTitle"));
        } else {
            if (instrument.getAltTitle() != null
                    && StringUtils.isNotEmpty(instrument.getAltTitle().getText())) {
                sb.append(buildSP(instrument.getAltTitle(), "gmd:alternateTitle"));
            }
        }

        /*
                gmd:date
         */
        sb.append("<gmd:date gco:nilReason=\"unknown\"/>");
        /*
                gmd:identifier
         */
        sb.append("<gmd:identifier>");
        sb.append("<gmd:MD_Identifier>");
        sb.append(buildSP(instrument.getUri(), instrument.getLabel(), "gmd:code"));
        sb.append("</gmd:MD_Identifier>");
        sb.append("</gmd:identifier>");

        sb.append("</gmd:CI_Citation>");
        sb.append("</gmi:citation>");

        sb.append("<gmi:type>");
        sb.append("<gmi:MI_SensorTypeCode/>");
        sb.append("</gmi:type>");
        /*
                gmi:description
         */
        if (instrument.getGcmd() != null) {
            sb.append(buildSP("", instrument.getGcmd().getPropertyValue(Constants.GCMD_ALTLABEL), "gmi:description"));
        } else {
            if (instrument.getDescription() != null
                    && StringUtils.isNotEmpty(instrument.getDescription().getText())) {
                sb.append(buildSP(instrument.getDescription(), "gmi:description"));
            } else {
                sb.append(buildSP("", "", "gmi:description"));
            }
        }
        sb.append("</gmi:MI_Instrument>");
        sb.append("</gmi:instrument>");
        return sb.toString();
    }

    public static Node createInstrumentKeywordNode(List<Concept> instrumentTypes, VoidDataset instrumentThesaurus) {
        return buildNode(buildInstrumentKeyword(instrumentTypes, instrumentThesaurus));
    }

    private static String buildInstrumentKeyword(List<Concept> instrumentTypes, VoidDataset instrumentThesaurus) {
        StringBuilder sb = new StringBuilder();

        sb.append("<gmd:descriptiveKeywords xmlns:gmd=\"")
                .append(Constants.GMD_NS)
                .append("\" xmlns:gco=\"")
                .append(Constants.GCO_NS)
                .append("\" xmlns:gmx=\"")
                .append(Constants.GMX_NS)
                .append("\" xmlns:xlink=\"")
                .append(Constants.XLINK_NS).append("\">");
        sb.append("<gmd:MD_Keywords>");

        for (Concept instType : instrumentTypes) {
            sb.append("<gmd:keyword>");
            sb.append("<gmx:Anchor xlink:href=\"")
                    .append(instType.getUri())
                    .append("\">")
                    .append(escapeXml(instType.getLabel()))
                    .append("</gmx:Anchor>");
            sb.append("</gmd:keyword>");
        }

        sb.append("<gmd:type>");
        sb.append("<gmd:MD_KeywordTypeCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode\" codeListValue=\"theme\"/>");
        sb.append("</gmd:type>");

        sb.append("<gmd:thesaurusName>");
        sb.append("<gmd:CI_Citation>");
        sb.append("<gmd:title>");
        sb.append("<gmx:Anchor xlink:href=\"")
                .append(instrumentThesaurus.getUri())
                .append("\">")
                .append(escapeXml(instrumentThesaurus.getFullTitle()))
                .append("</gmx:Anchor>");
        sb.append(" </gmd:title>");

        sb.append("<gmd:date>");
        sb.append("<gmd:CI_Date>");
        sb.append("<gmd:date>");
        sb.append("<gco:Date>")
                .append(instrumentThesaurus.getModified())
                .append("</gco:Date>");
        sb.append("</gmd:date>");

        sb.append("<gmd:dateType>");
        sb.append("<gmd:CI_DateTypeCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"publication\">publication</gmd:CI_DateTypeCode>");
        sb.append("</gmd:dateType>");

        sb.append("</gmd:CI_Date>");
        sb.append("</gmd:date>");

        sb.append("</gmd:CI_Citation>");
        sb.append("</gmd:thesaurusName>");

        sb.append("</gmd:MD_Keywords>");
        sb.append("</gmd:descriptiveKeywords>");
        return sb.toString();
    }

    private static String buildResponsibleParty(Contact contact) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:CI_ResponsibleParty>");

        if (StringUtils.isNotEmpty(contact.getIndividualName())) {
            sb.append("<gmd:individualName>");
            sb.append(" <gco:CharacterString>")
                    .append(escapeXml(contact.getIndividualName()))
                    .append("</gco:CharacterString>");
            sb.append("</gmd:individualName>");
        }

        if (StringUtils.isNotEmpty(contact.getOrgName())) {
            sb.append("<gmd:organisationName>");
            sb.append(" <gco:CharacterString>")
                    .append(escapeXml(contact.getOrgName()))
                    .append("</gco:CharacterString>");
            sb.append("</gmd:organisationName>");
        }

        if (StringUtils.isNotEmpty(contact.getPositionName())) {
            sb.append("<gmd:positionName>");
            sb.append(" <gco:CharacterString>")
                    .append(escapeXml(contact.getPositionName()))
                    .append("</gco:CharacterString>");
            sb.append("</gmd:positionName>");
        }

        if (StringUtils.isNotEmpty(contact.getPhone()) || StringUtils.isNotEmpty(contact.getFax())
                || StringUtils.isNotEmpty(contact.getAddress()) || StringUtils.isNotEmpty(contact.getCity())
                || StringUtils.isNotEmpty(contact.getPostal()) || StringUtils.isNotEmpty(contact.getCountry())
                || StringUtils.isNotEmpty(contact.getEmail()) || StringUtils.isNotEmpty(contact.getOnlineRs())) {

            sb.append("<gmd:contactInfo>");
            sb.append("<gmd:CI_Contact>");

            if (StringUtils.isNotEmpty(contact.getPhone()) || StringUtils.isNotEmpty(contact.getFax())) {
                sb.append("<gmd:phone>");
                sb.append("<gmd:CI_Telephone>");
                if (StringUtils.isNotEmpty(contact.getPhone())) {
                    sb.append("<gmd:voice>");
                    sb.append(" <gco:CharacterString>")
                            .append(escapeXml(contact.getPhone()))
                            .append("</gco:CharacterString>");
                    sb.append("</gmd:voice>");
                }
                if (StringUtils.isNotEmpty(contact.getFax())) {
                    sb.append("<gmd:facsimile>");
                    sb.append(" <gco:CharacterString>")
                            .append(escapeXml(contact.getFax()))
                            .append("</gco:CharacterString>");
                    sb.append("</gmd:facsimile>");
                }
                sb.append("</gmd:CI_Telephone>");
                sb.append("</gmd:phone>");
            }

            if (StringUtils.isNotEmpty(contact.getAddress()) || StringUtils.isNotEmpty(contact.getCity())
                    || StringUtils.isNotEmpty(contact.getPostal()) || StringUtils.isNotEmpty(contact.getCountry())
                    || StringUtils.isNotEmpty(contact.getEmail())) {
                sb.append("<gmd:address>");
                sb.append("<gmd:CI_Address>");

                if (StringUtils.isNotEmpty(contact.getAddress())) {
                    sb.append("<gmd:deliveryPoint>");
                    sb.append(" <gco:CharacterString>")
                            .append(escapeXml(contact.getAddress()))
                            .append("</gco:CharacterString>");
                    sb.append("</gmd:deliveryPoint>");
                }

                if (StringUtils.isNotEmpty(contact.getCity())) {
                    sb.append("<gmd:city>");
                    sb.append(" <gco:CharacterString>")
                            .append(escapeXml(contact.getCity()))
                            .append("</gco:CharacterString>");
                    sb.append("</gmd:city>");
                }

                if (StringUtils.isNotEmpty(contact.getPostal())) {
                    sb.append("<gmd:postalCode>");
                    sb.append(" <gco:CharacterString>").append(escapeXml(contact.getPostal())).append("</gco:CharacterString>");
                    sb.append("</gmd:postalCode>");
                }

                if (StringUtils.isNotEmpty(contact.getPostal())) {
                    sb.append("<gmd:postalCode>");
                    sb.append(" <gco:CharacterString>").append(escapeXml(contact.getPostal())).append("</gco:CharacterString>");
                    sb.append("</gmd:postalCode>");
                }

                if (StringUtils.isNotEmpty(contact.getEmail())) {
                    sb.append("<gmd:electronicMailAddress>");
                    sb.append(" <gco:CharacterString>").append(escapeXml(contact.getEmail())).append("</gco:CharacterString>");
                    sb.append("</gmd:electronicMailAddress>");
                }

                sb.append("</gmd:CI_Address>");
                sb.append("</gmd:address>");
            }

            if (StringUtils.isNotEmpty(contact.getOnlineRs())) {
                sb.append("<gmd:onlineResource>");
                sb.append("<gmd:CI_OnlineResource>");
                sb.append("<gmd:linkage>");
                sb.append("<gmd:URL>").append(escapeXml(contact.getOnlineRs())).append("</gmd:URL>");
                sb.append("</gmd:linkage>");
                sb.append("</gmd:CI_OnlineResource>");
                sb.append("</gmd:onlineResource>");
            }
            sb.append("</gmd:CI_Contact>");
            sb.append("</gmd:contactInfo>");
        }

        sb.append("<gmd:role>");
        sb.append("<gmd:CI_RoleCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_RoleCode\" codeListValue=\"").append(escapeXml(contact.getRole())).append("\">").append(escapeXml(contact.getRole())).append("</gmd:CI_RoleCode>");
        sb.append("</gmd:role>");

        sb.append("</gmd:CI_ResponsibleParty>");
        return sb.toString();
    }

    private static String buildSP(String uri, String label, String nodeName) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(uri)) {
            sb.append("<").append(nodeName).append(">");
            sb.append("<gmx:Anchor xlink:href=\"").append(escapeXml(uri)).append("\">");
            if (StringUtils.isNotEmpty(label)) {
                sb.append(escapeXml(label));
            }
            sb.append("</gmx:Anchor>");
            sb.append("</").append(nodeName).append(">");
        } else {
            sb.append("<").append(nodeName).append(">");
            sb.append("<gco:CharacterString>");
            if (StringUtils.isNotEmpty(label)) {
                sb.append(escapeXml(label));
            }
            sb.append("</gco:CharacterString>");
            sb.append("</").append(nodeName).append(">");

        }
        return sb.toString();
    }

    private static String buildSP(GmxAnchor sp, String nodeName) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(sp.getLink()) && StringUtils.isNotEmpty(sp.getText())) {
            sb.append("<").append(nodeName).append(">");
            sb.append("<gmx:Anchor xlink:href=\"").append(escapeXml(sp.getLink())).append("\">");
            sb.append(escapeXml(sp.getText()));
            sb.append("</gmx:Anchor>");
            sb.append("</").append(nodeName).append(">");
        } else {
            if (StringUtils.isNotEmpty(sp.getText())) {
                sb.append("<").append(nodeName).append(">");
                sb.append("<gco:CharacterString>").append(escapeXml(sp.getText())).append("</gco:CharacterString>");
                sb.append("</").append(nodeName).append(">");
            }
        }
        return sb.toString();
    }

    public static void updateSPNode(GmxAnchor sp, Node parentNode, Node refNode, String nodeName, String nodeNs) {
        if (sp.getSelf() != null) {
            Node parent = sp.getSelf().getParentNode();
            parent.getParentNode().removeChild(parent);
        }

        Node newNode = XmlUtils.createSPNode(parentNode, sp, nodeName, nodeNs);
        if (refNode != null) {
            parentNode.insertBefore(newNode, refNode);
        } else {
            parentNode.appendChild(newNode);
        }

    }

    public static Node createSPNode(Node parent, GmxAnchor sp, String nodeName, String nodeNs) {
        Document ownerDoc = parent.getOwnerDocument();
        Node node = ownerDoc.createElementNS(nodeNs, nodeName);

        if (StringUtils.isNotEmpty(sp.getLink()) && StringUtils.isNotEmpty(sp.getText())) {
            Node anchor = ownerDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
            node.appendChild(anchor);
            ((Element) anchor).setAttributeNS(Constants.XLINK_NS, "xlink:href", escapeXml(sp.getLink()));
            anchor.setTextContent(escapeXml(sp.getText()));
        } else {
            Node textNode = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
            node.appendChild(textNode);
            if (StringUtils.isNotEmpty(sp.getText())) {
                textNode.setTextContent(escapeXml(sp.getText()));
            }
        }

        return node;
    }

//    public static ValidationErrorsPerFile validateMetadataFile(String metadataFilePath, String recordId, String schemaLocation) throws SAXException, FileNotFoundException, IOException {
//        log.debug("Validating ISO 19139-2 (GMI) Metadata file " + metadataFilePath);
//        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        Schema schema = factory.newSchema(new File(schemaLocation));
//
//        javax.xml.validation.Validator validator = schema.newValidator();
//        ValidatorErrorHandler errorHandler = new ValidatorErrorHandler(metadataFilePath);
//        validator.setErrorHandler(errorHandler);
//
//        validator.validate(new StreamSource(new FileReader(metadataFilePath)));
//
//        if (errorHandler.getErrors() != null && errorHandler.getErrors().size() > 0) {
//            log.debug("Errors: ");
//            for (ValidationError error : errorHandler.getErrors()) {
//                log.debug(error.getMessage());
//            }
//            return new ValidationErrorsPerFile(errorHandler.getErrors(), errorHandler.getRecordId());
//        } else {
//            log.debug("Validation is successful");
//            return null;
//        }
//    }
    public static void validateMetadata(String metadata, String schemaLocation, int recordtype) throws ValidationException, SAXException, FileNotFoundException, IOException {
        log.debug("Validating XML Metadata");
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(schemaLocation));

        javax.xml.validation.Validator validator = schema.newValidator();
        ValidatorErrorHandler errorHandler = new ValidatorErrorHandler(recordtype);
        validator.setErrorHandler(errorHandler);

        try (Reader reader = new StringReader(metadata)) {
            validator.validate(new StreamSource(reader));
        }
        if (errorHandler.getErrors() != null && errorHandler.getErrors().size() > 0) {
            log.debug("Errors: ");
            errorHandler.getErrors().forEach((error) -> {
                log.debug(error.getMessage());
            });
            ValidationException vEx = new ValidationException();
            vEx.setValidationErrors(errorHandler.getErrors());
            throw vEx;
        } else {
            log.debug("Validation is successful");
        }
    }

    public static void removeNodesByXpath(Node rootNode, String xpathExpr) {
        NodeList nodeList = XPathUtils.getNodes(rootNode, xpathExpr);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String nodeName = node.getNodeName();
                node.getParentNode().removeChild(node);
                log.debug("Removed node " + nodeName);
            }
        }
    }

    public static Node getPlatformNodeRef(Node acquisitionNode) {

        NodeList nodeList = XPathUtils.getNodes(acquisitionNode, "./gmi:acquisitionPlan");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        } else {
            nodeList = XPathUtils.getNodes(acquisitionNode, "./gmi:objective");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            } else {
                nodeList = XPathUtils.getNodes(acquisitionNode, "./gmi:acquisitionRequirement");
                if (nodeList != null && nodeList.getLength() > 0) {
                    return nodeList.item(0);
                }
            }
        }
        return null;
    }

    public static void appendInstrumentNode(Node pfNode, Node instNode) {
        Node miPfNode = XPathUtils.getNode(pfNode, "./gmi:MI_Platform");
        if (miPfNode != null) {
            Node importedInst = pfNode.getOwnerDocument()
                    .importNode(instNode, true);
            miPfNode.appendChild(importedInst);
        }
    }

    public static void cleanNamespaces(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            element.removeAttributeNS(Constants.XML_NS, Constants.GCO_PREFIX);
            element.removeAttributeNS(Constants.XML_NS, Constants.GMD_PREFIX);
            element.removeAttributeNS(Constants.XML_NS, Constants.GMX_PREFIX);
            element.removeAttributeNS(Constants.XML_NS, Constants.GMI_PREFIX);
            element.removeAttributeNS(Constants.XML_NS, Constants.XLINK_PREFIX);
        }

    }

    public static Node getMissionNodeRef(Node acquisitionNode) {

        NodeList nodeList = XPathUtils.getNodes(acquisitionNode, "./gmi:platform");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        } else {
            return getPlatformNodeRef(acquisitionNode);
        }
    }

    public static Node createMissionNode(String missionStatus) {
        return buildNode(buildMission(missionStatus, true));
    }

    private static String buildMission(String missionStatus, boolean ns) {
        StringBuilder sb = new StringBuilder();
        if (ns) {
            sb.append("<gmi:operation xmlns:gmi=\"" + Constants.GMI_NS + "\" xmlns:gmd=\"" + Constants.GMD_NS + "\">");
        } else {
            sb.append("<gmi:operation>");
        }

        sb.append("<gmi:MI_Operation>");
        sb.append("<gmi:citation/>");
        sb.append("<gmi:status>");
        sb.append("<gmd:MD_ProgressCode codeList=\"http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/gmxCodelists.xml#MD_ProgressCode\" codeListValue=\"")
                .append(missionStatus)
                .append("\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"/>");
        sb.append("</gmi:status>");
        sb.append("<gmi:parentOperation/>");
        sb.append("</gmi:MI_Operation>");
        sb.append("</gmi:operation>");
        return sb.toString();
    }

    public static Node createSponsorNode(Sponsor sps) {
        return buildNode(buildSponsor(sps, true));
    }

    private static String buildSponsor(Sponsor sps, boolean ns) {
        StringBuilder sb = new StringBuilder();
        if (ns) {
            sb.append("<gmi:sponsor xmlns:gmi=\"" + Constants.GMI_NS + "\" xmlns:gmd=\"" + Constants.GMD_NS + "\" xmlns:gco=\"").append(Constants.GCO_NS).append("\" >");
        } else {
            sb.append("<gmi:sponsor>");
        }

        sb.append("<gmd:CI_ResponsibleParty xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">");
        sb.append("<gmd:organisationName>");
        sb.append("<gco:CharacterString>");
        sb.append(sps.getOperator().getLabel());
        sb.append("</gco:CharacterString>");
        sb.append("</gmd:organisationName>");
        sb.append("<gmd:role/>");
        sb.append("</gmd:CI_ResponsibleParty>");
        sb.append("</gmi:sponsor>");
        return sb.toString();
    }

    public static Node getSponsorNodeRef(Node platformNode) {
        NodeList nodeList = XPathUtils.getNodes(platformNode, "./gmi:MI_Platform/gmi:instrument");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        } else {
            return null;
        }
    }

    public static Node getLaunchDateNodeRef(Node platformNode) {

        NodeList nodeList = XPathUtils.getNodes(platformNode, "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:alternateTitle");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        } else {
            return XPathUtils.getNode(platformNode, "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:title");
        }
    }

    public static Node createLaunchDateNode(String launchDate) {
        return buildNode(buildLaunchDate(launchDate, true));
    }

    private static String buildLaunchDate(String launchDate, boolean ns) {
        StringBuilder sb = new StringBuilder();
        if (ns) {
            sb.append("<gmd:date xmlns:gmd=\"" + Constants.GMD_NS + "\" xmlns:gco=\"").append(Constants.GCO_NS).append("\" >");
        } else {
            sb.append("<gmd:date>");
        }
        sb.append("<gmd:CI_Date>");
        sb.append("<gmd:date>");
        sb.append("<gco:Date>");
        sb.append(launchDate);
        sb.append("</gco:Date>");
        sb.append("</gmd:date>");
        sb.append("<gmd:dateType>");
        sb.append("<gmd:CI_DateTypeCode codeList=\"http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"creation\"/>");
        sb.append("</gmd:dateType>");
        sb.append("</gmd:CI_Date>");
        sb.append("</gmd:date>");
        return sb.toString();
    }

    public static void updateNode(Node node, String value) {
        log.debug("Update node " + node.getLocalName() + " with value = " + value);
        if (value != null) {
            value = StringEscapeUtils.escapeXml10(value);
        }
        node.setTextContent(value);
    }

    public static Node createConstraintNode(Node root, String constraintName, GmxAnchor sp,
            Node nodeRef, boolean append, boolean serviceMetadata) {
        if (StringUtils.isNotEmpty(sp.getLink())
                || StringUtils.isNotEmpty(sp.getText())) {
            StringBuilder sb = new StringBuilder();
            if (serviceMetadata) {
                String text = "";
                if (StringUtils.isNotEmpty(sp.getLink())
                        && StringUtils.isNotEmpty(sp.getText())) {
                    text = sp.getText() + " " + sp.getLink();
                } else {
                    text = StringUtils.isNotEmpty(sp.getText()) ? sp.getText() : sp.getLink();
                }

                sb.append("<gmd:").append(constraintName).append(" xmlns:gmd=\"" + Constants.GMD_NS + "\" xmlns:gco=\"").append(Constants.GCO_NS).append("\">");
                sb.append("<gco:CharacterString>").append(escapeXml(text)).append("</gco:CharacterString>");
                sb.append("</gmd:").append(constraintName).append(">");
            } else {
                if (StringUtils.isNotEmpty(sp.getLink()) && StringUtils.isNotEmpty(sp.getText())) {
                    sb.append("<gmd:").append(constraintName)
                            .append(" xmlns:gmd=\"" + Constants.GMD_NS + "\" xmlns:gco=\"")
                            .append(Constants.GCO_NS)
                            .append("\" xmlns:gmx=\"")
                            .append(Constants.GMX_NS).append("\" xmlns:xlink=\"")
                            .append(Constants.XLINK_NS).append("\">");
                    sb.append("<gmx:Anchor xlink:href=\"").append(escapeXml(sp.getLink())).append("\">");
                    sb.append(escapeXml(sp.getText()));
                    sb.append("</gmx:Anchor>");
                    sb.append("</gmd:").append(constraintName).append(">");
                } else {
                    if (StringUtils.isNotEmpty(sp.getText())) {
                        sb.append("<gmd:").append(constraintName).append(" xmlns:gmd=\"" + Constants.GMD_NS + "\" xmlns:gco=\"").append(Constants.GCO_NS).append("\">");
                        sb.append("<gco:CharacterString>").append(escapeXml(sp.getText())).append("</gco:CharacterString>");
                        sb.append("</gmd:").append(constraintName).append(">");
                    }
                }
            }

            Node node = buildNode(sb.toString());

            if (node != null) {
                log.debug("Root node " + root.getLocalName());
                Node importedNode = root.getOwnerDocument()
                        .importNode(node, true);

                cleanNamespaces(importedNode);

                if (nodeRef != null) {
                    root.insertBefore(importedNode, nodeRef.getNextSibling());
                } else {
                    if (append) {
                        root.appendChild(importedNode);
                    } else {
                        log.debug("Root node first child" + root.getFirstChild());
                        root.insertBefore(importedNode, root.getFirstChild());
                    }
                }
                log.debug("created constraint node ");
                return importedNode;
            }
        }

        return null;
    }

    public static void cataloguesToFile(String cataloguesFile, List<Catalogue> catalogues) throws DOMException, LSException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<catalogues>");
        if (catalogues != null) {
            for (Catalogue cat : catalogues) {
                if (cat.isPublish()) {
                    sb.append("<catalogue public=\"true\">");
                } else {
                    sb.append("<catalogue>");
                }
                sb.append("<url>").append(escapeXml(cat.getServerUrl())).append("</url>");
                sb.append("</catalogue>");
            }
        }
        sb.append("</catalogues>");

        log.debug("List of catalogues " + sb.toString());
        XMLParser xmlParser = new XMLParser();
        xmlParser.domToFile(xmlParser.stream2Document(sb.toString()), cataloguesFile);
    }

    public static Node createTopicCategory(String topicCategory) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:topicCategory xmlns:gmd=\"" + Constants.GMD_NS + "\">");
        sb.append("<gmd:MD_TopicCategoryCode>");
        sb.append(topicCategory);
        sb.append("</gmd:MD_TopicCategoryCode>");
        sb.append("</gmd:topicCategory>");

        return buildNode(sb.toString());
    }

    public static Node getTopicCategoryNodeRef(Node dataId) {
        Node node = XPathUtils.getNode(dataId, "./gmd:environmentDescription");
        if (node != null) {
            return node;
        }

        NodeList nodeList = XPathUtils.getNodes(dataId, "./gmd:extent");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        node = XPathUtils.getNode(dataId, "./gmd:supplementalInformation");
        if (node != null) {
            return node;
        }
        return null;
    }

    public static Node createSuppInfo(String suppInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gmd:supplementalInformation xmlns:gmd=\"").append(Constants.GMD_NS).append("\" xmlns:gco=\"").append(Constants.GCO_NS).append("\" >");
        sb.append("<gco:CharacterString>");
        sb.append("<![CDATA[");
        sb.append(suppInfo);
        sb.append("]]>");
        sb.append("</gco:CharacterString>");
        sb.append("</gmd:supplementalInformation>");

        return buildNode(sb.toString());
    }

    public static Offering loadOfferingOperations(String serviceCapabilitiesUrl, Configuration config) throws SearchException, IOException {
        Map<String, String> errorDetails = new HashMap<>();

        String strResponse = HttpInvoker.httpGET(serviceCapabilitiesUrl, errorDetails);

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
            XMLParser xmlParser = new XMLParser();
            xmlParser.setIsNamespaceAware(true);

            Document capabilitiesDoc = xmlParser.stream2Document(strResponse);
            if (capabilitiesDoc != null) {
                Node capabilitiesNode = XPathUtils.getNode(capabilitiesDoc, "./wms:WMS_Capabilities");
                if (capabilitiesNode != null) {
                    // WMS capabilities
                    Offering offering = config.findOffering("/req/wms");
                    Node getCapabilitiesNode = XPathUtils.getNode(capabilitiesNode, "./wms:Capability/wms:Request/wms:GetCapabilities");
                    if (getCapabilitiesNode != null) {
                        OfferingOperation operation = offering.getAvailableOperation("GetCapabilities");
                        if (StringUtils.isEmpty(operation.getCode())) {
                            operation.setCode("GetCapabilities");
                        }

                        extractWmsOperationInfo(operation, getCapabilitiesNode);
                        offering.addOperation(operation);
                    }
                    Node getMapNode = XPathUtils.getNode(capabilitiesNode, "./wms:Capability/wms:Request/wms:GetMap");
                    if (getMapNode != null) {
                        OfferingOperation operation = offering.getAvailableOperation("GetMap");
                        if (StringUtils.isEmpty(operation.getCode())) {
                            operation.setCode("GetMap");
                        }
                        extractWmsOperationInfo(operation, getMapNode);
                        offering.addOperation(operation);
                    }
                    return offering;
                }

                capabilitiesNode = XPathUtils.getNode(capabilitiesDoc, "./wcs:Capabilities");
                if (capabilitiesNode != null) {
                    // WCS capabilities
                    Offering offering = config.findOffering("/req/wcs");
                    NodeList operationNodes = XPathUtils.getNodes(capabilitiesNode, "./ows:OperationsMetadata/ows:Operation");
                    if (operationNodes != null && operationNodes.getLength() > 0) {
                        for (int i = 0; i < operationNodes.getLength(); i++) {
                            Node operNode = operationNodes.item(i);
                            String operCode = getNodeAttValue(operNode, "name");
                            if (StringUtils.isNotEmpty(operCode)) {
                                OfferingOperation operation = offering.getAvailableOperation(operCode);
                                offering.addOperation(operation);
                                if (StringUtils.isEmpty(operation.getCode())) {
                                    operation.setCode(operCode);
                                }
                                String url = XPathUtils.getAttributeValue(operNode, "./ows:DCP/ows:HTTP/ows:Get", "href", Constants.XLINK_NS);
                                if (StringUtils.isNotEmpty(url)) {
                                    operation.setMethod("GET");
                                } else {
                                    url = XPathUtils.getAttributeValue(operNode, "./ows:DCP/ows:HTTP/ows:Post", "href", Constants.XLINK_NS);
                                    if (StringUtils.isNotEmpty(url)) {
                                        operation.setMethod("POST");
                                    }
                                }
                                setOperationUrl(operation, url);
                            }
                        }
                    }
                    return offering;
                }

                capabilitiesNode = XPathUtils.getNode(capabilitiesDoc, "./wfs:WFS_Capabilities");
                if (capabilitiesNode != null) {
                    // WFS capabilities
                    Offering offering = config.findOffering("/req/wfs");
                    Node getCapabilitiesNode = XPathUtils.getNode(capabilitiesNode, "./ows1:OperationsMetadata/ows1:Operation[@name='GetCapabilities']");
                    if (getCapabilitiesNode != null) {
                        OfferingOperation operation = offering.getAvailableOperation("GetCapabilities");
                        extractOperationInfo(operation, getCapabilitiesNode);
                        if (StringUtils.isEmpty(operation.getCode())) {
                            operation.setCode("GetCapabilities");
                        }
                        offering.addOperation(operation);
                    }

                    Node getFeatureNode = XPathUtils.getNode(capabilitiesNode, "./ows1:OperationsMetadata/ows1:Operation[@name='GetFeature']");
                    if (getFeatureNode != null) {
                        OfferingOperation operation = offering.getAvailableOperation("GetFeature");
                        if (StringUtils.isEmpty(operation.getCode())) {
                            operation.setCode("GetFeature");
                        }
                        extractOperationInfo(operation, getFeatureNode);
                        offering.addOperation(operation);
                    }
                    return offering;
                }

                capabilitiesNode = XPathUtils.getNode(capabilitiesDoc, "./wmts:Capabilities ");
                if (capabilitiesNode != null) {
                    // WMTS capabilities
                    Offering offering = config.findOffering("/req/wmts");

                    Node getCapabilitiesNode = XPathUtils.getNode(capabilitiesNode, "./ows1:OperationsMetadata/ows1:Operation[@name='GetCapabilities']");
                    if (getCapabilitiesNode != null) {
                        OfferingOperation operation = offering.getAvailableOperation("GetCapabilities");
                        if (StringUtils.isEmpty(operation.getCode())) {
                            operation.setCode("GetCapabilities");
                        }
                        extractOperationInfo(operation, getCapabilitiesNode);
                        offering.addOperation(operation);
                    }

                    Node getTileNode = XPathUtils.getNode(capabilitiesNode, "./ows1:OperationsMetadata/ows1:Operation[@name='GetTile']");
                    if (getTileNode != null) {
                        OfferingOperation operation = offering.getAvailableOperation("GetTile");
                        if (StringUtils.isEmpty(operation.getCode())) {
                            operation.setCode("GetTile");
                        }
                        extractOperationInfo(operation, getTileNode);
                        offering.addOperation(operation);
                    }

                    return offering;
                }

                throw new SearchException("Only WMS (1.3), WMTS(1.0), WCS(2.0) and WFS(2.0) Service Capabilities endpoints are supported", "");

            } else {
                throw new SearchException(String.format("Capabilities service endpoint %s return an invallid response", serviceCapabilitiesUrl), "");
            }
        }
        throw new SearchException(String.format("Capabilities service endpoint %s return an empty response", serviceCapabilitiesUrl), "");
    }

    private static void extractWmsOperationInfo(OfferingOperation operation, Node operNode) {
        String type = XPathUtils.getNodeValue(operNode, "./wms:Format");
        if (StringUtils.isNotEmpty(type)) {
            operation.setMimeType(type);
        }

        String url = XPathUtils.getAttributeValue(operNode, "./wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource", "href", Constants.XLINK_NS);
        if (StringUtils.isNotEmpty(url)) {
            operation.setMethod("GET");
        } else {
            url = XPathUtils.getAttributeValue(operNode, "./wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource", "href", Constants.XLINK_NS);
            if (StringUtils.isNotEmpty(url)) {
                operation.setMethod("POST");
            }
        }
        setOperationUrl(operation, url);
    }

    private static void extractOperationInfo(OfferingOperation operation, Node operNode) {
        String url = XPathUtils.getAttributeValue(operNode, "./ows1:DCP/ows1:HTTP/ows1:Get", "href", Constants.XLINK_NS);
        if (StringUtils.isNotEmpty(url)) {
            operation.setMethod("GET");
        } else {
            url = XPathUtils.getAttributeValue(operNode, "./ows1:DCP/ows1:HTTP/ows1:Post", "href", Constants.XLINK_NS);
            if (StringUtils.isNotEmpty(url)) {
                operation.setMethod("POST");
            }
        }

        setOperationUrl(operation, url);
    }

    private static void setOperationUrl(OfferingOperation operation, String url) {
        if (StringUtils.isNotEmpty(url)) {
            ParameterOption hrefOption = operation.findUrl();
            if (hrefOption != null) {
                hrefOption.setValue(url);
            } else {
                if (operation.getRequiredExtFields() == null) {
                    operation.setRequiredExtFields(new ArrayList<>());
                }
                operation.getRequiredExtFields().add(new ParameterOption(url, "href"));
            }
        }
    }

    public static Node getDescriptiveKeywordNodeRef(Identification identification) {

        if (identification.getResourceSpecificUsageNode() != null) {
            return identification.getResourceSpecificUsageNode();
        }

        Node node = XPathUtils.getNode(identification.getDataId(), "./gmd:resourceConstraints");
        if (node != null) {
            return node;
        }

        return getConstraintNodeRef(identification);
    }

    public static Node getConstraintNodeRef(Identification identification) {

        if (identification.getAggregationInfoNode() != null) {
            return identification.getAggregationInfoNode();
        }

        if (identification.isService()) {
            /*
                service metadata
             */
            Node node = XPathUtils.getNode(identification.getDataId(), "./srv:serviceType");
            if (node != null) {
                return node;
            }

            return getServiceTypeNodeRef(identification);
        } else {
            /*
                collection metadata
             */

            if (identification.getSpatialRepresentationTypeNode() != null) {
                return identification.getSpatialRepresentationTypeNode();
            }

            if (identification.getSpatialResolutionNode() != null) {
                return identification.getSpatialResolutionNode();
            }

            if (identification.getLanguage() != null) {
                return identification.getLanguage();
            }

            NodeList nodeList = XPathUtils.getNodes(identification.getDataId(), "./gmd:characterSet");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            }

            nodeList = XPathUtils.getNodes(identification.getDataId(), "./gmd:topicCategory");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            }

            Node node = XPathUtils.getNode(identification.getDataId(), "./gmd:environmentDescription");
            if (node != null) {
                return node;
            }

            nodeList = XPathUtils.getNodes(identification.getDataId(), "./gmd:extent");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            }

            return XPathUtils.getNode(identification.getDataId(), "./gmd:supplementalInformation");
        }
    }

    public static Node getServiceTypeNodeRef(Identification identification) {
        NodeList nodeList = XPathUtils.getNodes(identification.getDataId(), "./srv:serviceTypeVersion");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        Node node = XPathUtils.getNode(identification.getDataId(), "./srv:accessProperties");
        if (node != null) {
            return node;
        }

        nodeList = XPathUtils.getNodes(identification.getDataId(), "./srv:restrictions");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(identification.getDataId(), "./srv:keywords");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(identification.getDataId(), "./srv:extent");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(identification.getDataId(), "./srv:coupledResource");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        node = XPathUtils.getNode(identification.getDataId(), "./srv:couplingType");
        if (node != null) {
            return node;
        }

        nodeList = XPathUtils.getNodes(identification.getDataId(), "./srv:containsOperations");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(identification.getDataId(), "./srv:operatesOn");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }
        return null;
    }

    public static Node createServiceType(ServiceType serviceType) {
        StringBuilder sb = new StringBuilder();
        String type = serviceType.getType();
        if (StringUtils.isEmpty(type)) {
            type = "LocalName";
        }
        sb.append("<srv:serviceType xmlns:srv=\"" + Constants.SERVICE_NS
                + "\" xmlns:gco=\"" + Constants.GCO_NS + "\">");
        sb.append("<gco:").append(type)
                .append(" codeSpace=\"").append(serviceType.getCode()).append("\">")
                .append(serviceType.getValue());
        sb.append("</gco:").append(type).append(">");
        sb.append("</srv:serviceType>");

        return buildNode(sb.toString());
    }

    public static Node getDistributionNodeRef(Node metadataNode, boolean seriesMetadata) {

        NodeList nodes = XPathUtils.getNodes(metadataNode, "./gmd:dataQualityInfo");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:portrayalCatalogueInfo");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:metadataConstraints");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:applicationSchemaInfo");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        Node node = XPathUtils.getNode(metadataNode, "./gmd:metadataMaintenance");

        if (node != null) {
            return node;
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:series");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:describes");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:propertyType");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:featureType");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        nodes = XPathUtils.getNodes(metadataNode, "./gmd:featureAttribute");
        if (nodes != null && nodes.getLength() > 0) {
            return nodes.item(0);
        }

        if (seriesMetadata) {
            nodes = XPathUtils.getNodes(metadataNode, "./gmi:acquisitionInformation");
            if (nodes != null && nodes.getLength() > 0) {
                return nodes.item(0);
            }
        }
        return null;
    }

    public static Node getContentInfoNodeRef(Node metadataNode, boolean seriesMetadata) {
        Node node = XPathUtils.getNode(metadataNode, "./gmd:distributionInfo");

        if (node != null) {
            return node;
        }

        return getDistributionNodeRef(metadataNode, seriesMetadata);
    }

    public static Node getAlternateTitleNodeRef(Node parentNode) {

        Node ciCitationNode = XPathUtils.getNode(parentNode, "./*/gmi:citation/gmd:CI_Citation");
        //System.out.println("MNG " + ciCitationNode);
        if (ciCitationNode != null) {
            NodeList nodeList = XPathUtils.getNodes(ciCitationNode, "./gmd:date");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            }

            Node node = XPathUtils.getNode(ciCitationNode, "./gmd:edition");
            if (node != null) {
                return node;
            }

            node = XPathUtils.getNode(ciCitationNode, "./gmd:editionDate");
            if (node != null) {
                return node;
            }

            nodeList = XPathUtils.getNodes(ciCitationNode, "./gmd:identifier");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            }

            nodeList = XPathUtils.getNodes(ciCitationNode, "./gmd:citedResponsibleParty");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            }

            nodeList = XPathUtils.getNodes(ciCitationNode, "./gmd:presentationForm");
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0);
            }

            node = XPathUtils.getNode(ciCitationNode, "./gmd:series");
            if (node != null) {
                return node;
            }

            node = XPathUtils.getNode(ciCitationNode, "./gmd:otherCitationDetails");
            if (node != null) {
                return node;
            }

            node = XPathUtils.getNode(ciCitationNode, "./gmd:collectiveTitle");
            if (node != null) {
                return node;
            }

            node = XPathUtils.getNode(ciCitationNode, "./gmd:ISBN");
            if (node != null) {
                return node;
            }

            return XPathUtils.getNode(ciCitationNode, "./gmd:ISSN");
        }
        return null;
    }

    public static Distribution buildDistribution(Node distributionNode,
            boolean hasOffering, final Configuration config) {
        //log.debug("Building distribution..............." + nodeToString(distributionNode));
        log.debug("Building distribution...............");

        Distribution distribution = new Distribution();
        distribution.setSelf(distributionNode);

        NodeList transferNodes = XPathUtils
                .getNodes(distributionNode, "./gmd:transferOptions/gmd:MD_DigitalTransferOptions");
        if (transferNodes != null && transferNodes.getLength() > 0) {
            log.debug("Has transferNodes " + transferNodes.getLength());
            if (distribution.getTransferOptions() == null) {
                distribution.setTransferOptions(new ArrayList<>());
            }

            for (int i = 0; i < transferNodes.getLength(); i++) {
                log.debug("transfer node " + i);
                Node optionsNode = transferNodes.item(i);
                TransferOption transferOption = new TransferOption();
                distribution.getTransferOptions().add(transferOption);

                transferOption.setSelf(optionsNode);

                Node unitsNode = XPathUtils
                        .getNode(optionsNode, "./gmd:unitsOfDistribution/gco:CharacterString");
                if (unitsNode != null) {
                    transferOption.setUnitsNode(unitsNode);
                    transferOption.setUnits(XmlUtils.getNodeValue(unitsNode));
                }

                Node sizeNode = XPathUtils
                        .getNode(optionsNode, "./gmd:transferSize/gco:Real");
                if (sizeNode != null) {
                    transferOption.setSizeNode(sizeNode);
                    transferOption.setSize(XmlUtils.getNodeValue(sizeNode));
                }

                NodeList onlineNodes = XPathUtils
                        .getNodes(optionsNode, "./gmd:onLine");
                if (onlineNodes != null && onlineNodes.getLength() > 0) {
                    if (transferOption.getOnlineRses() == null) {
                        transferOption.setOnlineRses(new ArrayList<>());
                    }
                    for (int j = 0; j < onlineNodes.getLength(); j++) {
                        addOnlineResource(onlineNodes.item(j), transferOption, hasOffering, config);
                    }
                }
            }
        } else {
            log.debug("Have no transferOptions node");
        }
        return distribution;
    }

    private static void addOnlineResource(Node onlineNode,
            TransferOption transferOption, boolean hasOffering, final Configuration config) {
        log.debug("build OnlineResource");

        if (onlineNode != null) {
            OnlineResource onlineRs = new OnlineResource();
            Node protocolNode = XPathUtils
                    .getNode(onlineNode, "./gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString");
            String protocol = null;
            if (protocolNode != null) {
                protocol = XmlUtils.getNodeValue(protocolNode);
                //onlineRs.setProtocolNode(protocolNode);
                onlineRs.setProtocol(protocol);
            }

            if (hasOffering
                    && StringUtils.isNotEmpty(protocol)
                    && config.isOfferingProtocol(protocol)) {
                // remove the offering online resource
                onlineNode.getParentNode().removeChild(onlineNode);

            } else {
                //onlineRs.setSelf(onlineNode);
                transferOption.getOnlineRses().add(onlineRs);

                Node urlNode = XPathUtils
                        .getNode(onlineNode, "./gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
                if (urlNode != null) {
                    //onlineRs.setLinkageNode(urlNode);
                    onlineRs.setLinkage(XmlUtils.getNodeValue(urlNode));
                }

                Node appProfileNode = XPathUtils
                        .getNode(onlineNode, "./gmd:CI_OnlineResource/gmd:applicationProfile/gco:CharacterString");
                if (appProfileNode != null) {
                    //onlineRs.setAppProfileNode(appProfileNode);
                    onlineRs.setAppProfile(XmlUtils.getNodeValue(appProfileNode));
                }

                Node nameNode = XPathUtils
                        .getNode(onlineNode, "./gmd:CI_OnlineResource/gmd:name/gco:CharacterString");
                if (nameNode != null) {
                    // onlineRs.setNameNode(nameNode);
                    onlineRs.setName(XmlUtils.getNodeValue(nameNode));
                }

                Node descNode = XPathUtils
                        .getNode(onlineNode, "./gmd:CI_OnlineResource/gmd:description/gco:CharacterString");
                if (descNode != null) {
                    // onlineRs.setDescriptionNode(descNode);
                    onlineRs.setDescription(XmlUtils.getNodeValue(descNode));
                }

                Node functionNode = XPathUtils
                        .getNode(onlineNode, "./gmd:CI_OnlineResource/gmd:function/gmd:CI_OnLineFunctionCode");
                if (functionNode != null) {
                    // onlineRs.setFunctionNode(functionNode);
                    onlineRs.setFunction(XmlUtils.getNodeAttValue(functionNode, "codeListValue"));
                }

                String relatedField = XmlUtils.getNodeAttValue(onlineNode, Constants.XLINK_NS, "href");
                if (StringUtils.isNotEmpty(relatedField)) {
                    if (relatedField.contains("xpointer(")) {
                        relatedField = StringUtils.substringAfter(relatedField, "xpointer(");
                        relatedField = StringUtils.substringBefore(relatedField, ")");
                        log.debug("Related field " + relatedField);
                        if (config.getOnlineRSRelatedFields().contains(relatedField)) {
                            onlineRs.setRelatedField(relatedField);
                        }
                    }
                }
            }
        }
    }

    public static GmxAnchor buildStringProperty(Node node, String preXpath) {
        Node descNode = XPathUtils
                .getNode(node, preXpath + "/gco:CharacterString");
        if (descNode != null) {
            GmxAnchor strProp = new GmxAnchor();
            strProp.setSelf(descNode);
            strProp.setText(XmlUtils.getNodeValue(descNode));
            return strProp;
        } else {
            descNode = XPathUtils
                    .getNode(node, preXpath + "/gmx:Anchor");
            if (descNode != null) {
                GmxAnchor strProp = new GmxAnchor();
                strProp.setSelf(descNode);
                strProp.setText(XmlUtils.getNodeValue(descNode));
                strProp.setLink(XmlUtils.getNodeAttValue(descNode, Constants.XLINK_NS, "href"));
                return strProp;
            }
        }
        return null;
    }
}
