/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.configuration.Offering;
import be.spacebel.metadataeditor.models.configuration.OfferingOperation;
import be.spacebel.metadataeditor.models.workspace.Acquisition;
import be.spacebel.metadataeditor.models.workspace.Contact;
import be.spacebel.metadataeditor.models.workspace.Distribution;
import be.spacebel.metadataeditor.models.workspace.Identification;
import be.spacebel.metadataeditor.models.workspace.Others;
import be.spacebel.metadataeditor.models.workspace.Metadata;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.models.workspace.identification.GmxAnchor;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.models.workspace.distribution.OnlineResource;
import be.spacebel.metadataeditor.models.workspace.distribution.TransferOption;
import be.spacebel.metadataeditor.models.workspace.identification.Bbox;
import be.spacebel.metadataeditor.models.workspace.identification.Constraints;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.models.workspace.identification.FreeKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.Keyword;
import be.spacebel.metadataeditor.models.configuration.Thesaurus;
import be.spacebel.metadataeditor.models.workspace.identification.ServiceType;
import be.spacebel.metadataeditor.models.workspace.mission.Sponsor;
import be.spacebel.metadataeditor.models.workspace.identification.Temporal;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.validation.AutoCorrectionWarning;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSException;
import org.xml.sax.SAXException;

/**
 * This class implements methods that is used to transform ISO-19139 XML
 * metadata into the Internal Metadata Model
 *
 * @author mng
 */
public class IsoHandler {

    private final Logger log = Logger.getLogger(getClass());
    private final XMLParser xmlParser;
    private final Configuration config;
    private final Dif10Handler dif10Handler;

    public IsoHandler(Configuration config) {
        this.config = config;
        this.xmlParser = new XMLParser();
        this.xmlParser.setIsNamespaceAware(true);
        this.dif10Handler = new Dif10Handler(config);
    }

    public Document getIdAndTitle(String source, List<String> values, boolean isFile) throws IOException, SAXException {
        if (StringUtils.isNotEmpty(source)) {
            Document isoDoc;
            if (isFile) {
                isoDoc = xmlParser.fileToDom(source);
            } else {
                isoDoc = xmlParser.stream2Document(source);
            }

            String metadataType = XPathUtils.getAttributeValue(isoDoc,
                    "./*/gmd:hierarchyLevel/gmd:MD_ScopeCode", "codeListValue");

            boolean expectedType = false;
            if (StringUtils.isNotEmpty(metadataType)) {
                if (metadataType.equals("series")) {
                    expectedType = true;
                }
                if (metadataType.equals("service")) {
                    expectedType = true;
                }
            }

            if (!expectedType) {
                throw new IOException("The metadata record is neither collection nor service");
            }

            String id = XPathUtils.getNodeValue(isoDoc,
                    "./*/gmd:fileIdentifier/gco:CharacterString");
            String title = XPathUtils.getNodeValue(isoDoc,
                    "./*/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");

            if (StringUtils.isNotEmpty(id)
                    && StringUtils.isNotEmpty(title)) {
                values.add(id);
                values.add(title);
            }
            return isoDoc;
        } else {
            return null;
        }
    }

    public Document getId(String isoFile, List<String> values) throws IOException, SAXException {
        Document isoDoc = xmlParser.fileToDom(isoFile);
        String id = XPathUtils.getNodeValue(isoDoc, "./*/gmd:fileIdentifier/gco:CharacterString");

        if (StringUtils.isNotEmpty(id)) {
            values.add(id);
        }
        return isoDoc;
    }

    public void updateToClone(Document isoDoc, String identifier) {
        Node node = XPathUtils.getNode(isoDoc, "./*/gmd:fileIdentifier/gco:CharacterString");
        if (node != null) {
            XmlUtils.setTextContent(node, identifier);
        }

        node = XPathUtils.getNode(isoDoc, "./*/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        if (node != null) {
            String newTitle = "Title of metadata record " + identifier;
            XmlUtils.setTextContent(node, newTitle);
        }

        // set current date to creation and last update dates
        node = XPathUtils.getNode(isoDoc, "./*/gmd:dateStamp");
        if (node != null) {
            log.debug("MNG: Update last update date");
            Date currentDate = new Date();
            String lastUpdateDate = CommonUtils.dateTimeToStr(currentDate);
            MetadataUtils.updateLastUpdateDate(node, lastUpdateDate);
        }
    }

    public Metadata buildMetadata(Document metadataDoc) throws IOException, ParseException {
        Metadata metadata = new Metadata(config);
        buildMetadata(metadataDoc, metadata);
        return metadata;
    }

    public Document buildMetadata(String xmlSource, Metadata metadata) throws IOException, ParseException {
        if (xmlSource != null && !xmlSource.isEmpty()) {
            Document metadataDoc = xmlParser.stream2Document(xmlSource);
            buildMetadata(metadataDoc, metadata);
            return metadataDoc;
        } else {
            return null;
        }
    }

    public MetadataFile buildMetadataFile(Document metadataDoc) throws IOException, ParseException {
        log.debug("Metadata document  --> MetadataFile");
        Metadata metadata = new Metadata(config);
        buildMetadata(metadataDoc, metadata);

        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setMetadata(metadata);
        metadataFile.setXmlDoc(metadataDoc);
        return metadataFile;
    }

    public MetadataFile buildMetadataFile(String xmlSource, String metadataId) throws IOException, ParseException {
        log.debug("MNG --> buildMetadataFile of metadata " + metadataId);
        //log.debug(xmlSource);
        if (xmlSource != null && !xmlSource.isEmpty()) {
            Document metadataDoc = xmlParser.stream2Document(xmlSource);

            Metadata metadata = new Metadata(config);
            buildMetadata(metadataDoc, metadata);

            MetadataFile metadataFile = new MetadataFile();
            metadataFile.setMetadata(metadata);
            metadataFile.setXmlDoc(metadataDoc);
            metadataFile.setFileName(metadataId + ".xml");

            return metadataFile;
        } else {
            return null;
        }
    }

//    public void updateToBeRemoved(String userWorkspaceDir, MetadataFile metadataFile) throws IOException, SAXException, XPathExpressionException {
//        if (metadataFile.getMetadata() != null && metadataFile.getXmlDoc() != null) {
//            metadataFile.getMetadata().update();
//
//            String newFileName = CommonUtils.getFileName(metadataFile.getMetadata().getOthers().getFileIdentifier()) + ".xml";
//            String newFilePath = userWorkspaceDir + "/" + newFileName;
//            log.debug("New File name: " + newFileName);
//            log.debug("File name: " + metadataFile.getFileName());
//
//            updateMetadataSources(metadataFile, newFilePath);
//
//            String fileName = metadataFile.getFileName();
//            log.debug("File name1: " + fileName);
//            if (fileName != null && !newFileName.equalsIgnoreCase(fileName)) {
//                String oldFilePath = userWorkspaceDir + "/" + metadataFile.getFileName();
//                log.debug("Deleting old metadata record file " + oldFilePath);
//                if (FileUtils.deleteQuietly(new File(oldFilePath))) {
//                    log.debug("Deleted");
//                }
//            }
//
//            metadataFile.setFileName(newFileName);
//            resetUnsaved(metadataFile);
//        }
//    }
    public void updateAndSaveMetadataSources(String userWorkspaceDir, MetadataFile metadataFile)
            throws DOMException, LSException, IOException {
        String filePath = userWorkspaceDir + "/" + metadataFile.getFileName();
        // serialize to the file
        xmlParser.domToFile(metadataFile.getXmlDoc(), filePath);

        // update XML src
        //metadataFile.setXmlSrc(xmlParser.format(metadataFile.getXmlDoc()));
        setXmlSrc(metadataFile);

        // update dif10
        if (metadataFile.getMetadata().isSeries()) {
            metadataFile.setDif10(dif10Handler.toDif10(metadataFile.getXmlDoc()));
        }
    }

    public void removeOfferingsfromDistribution(Document isoDoc, Metadata metadata) {
        if (metadata != null && metadata.hasOfferingOperation()) {
            metadata.getOfferings().stream().filter((offering) -> (offering.getOperations() != null)).forEachOrdered((offering) -> {
                offering.getOperations().stream().map((operation) -> {
                    OnlineResource onlineRs = new OnlineResource();
                    onlineRs.setLinkage(operation.getUrl());
                    onlineRs.setProtocol(operation.getProtocol());
                    onlineRs.setFunction(operation.getFunction());
                    onlineRs.setName(operation.getCode());
                    return onlineRs;
                }).forEachOrdered((onlineRs) -> {

                    StringBuilder sb = new StringBuilder();
                    sb.append("./*/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[gmd:CI_OnlineResource[");
                    sb.append("(gmd:linkage/gmd:URL='").append(onlineRs.getLinkage()).append("')");
                    if (StringUtils.isNotEmpty(onlineRs.getProtocol())) {
                        sb.append("and (gmd:protocol/gco:CharacterString='").append(onlineRs.getProtocol()).append("')");
                    }
                    if (StringUtils.isNotEmpty(onlineRs.getName())) {
                        sb.append("and (gmd:name/gco:CharacterString='").append(onlineRs.getName()).append("')");
                    }
                    if (StringUtils.isNotEmpty(onlineRs.getFunction())) {
                        sb.append("and (gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='").append(onlineRs.getFunction()).append("')");
                    }
                    sb.append("]]");

                    log.debug("Remove offerings from distribution XPATH: " + sb.toString());

                    /*
                        remove the dedicated online resource of the offering if it does already exist
                     */
                    XPathUtils.removeNodes(isoDoc, sb.toString());

                });
            });
        }

    }

    private void offeringsToDistribution(Document isoDoc, Metadata metadata) {
        log.debug("Offering to distribution");
//        Document newMetadataDoc = xmlParser.createDOM(false, true);
//        Node importedNode = newMetadataDoc.importNode(isoDoc.getDocumentElement(), true);
//        newMetadataDoc.appendChild(importedNode);
        List<Offering> offerings = null;
        if (metadata.getOfferings() != null
                && metadata.getOfferings().size() > 0) {
            offerings = new ArrayList<>();
            for (Offering offering : metadata.getOfferings()) {
                if (hasOnlineResource(offering)) {
                    offerings.add(offering);
                }
            }
        }

        if (offerings != null && offerings.size() > 0) {
            // add dedicated online resources of offerings to distributionInfo
            NodeList nodes = XPathUtils.getNodes(isoDoc,
                    "./*/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine");
            if (nodes != null && nodes.getLength() > 0) {
                log.debug("Have online resource");
                appendOnlineResourceNodes(nodes.item(0).getParentNode(), offerings);
            } else {
                log.debug("Have no online resource");
                nodes = XPathUtils.getNodes(isoDoc,
                        "./*/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions");
                if (nodes != null && nodes.getLength() > 0) {
                    log.debug("Have TransferOptions");
                    appendOnlineResourceNodes(nodes.item(0), offerings);
                } else {
                    log.debug("Have no TransferOptions");
                    Node node = XPathUtils.getNode(isoDoc,
                            "./*/gmd:distributionInfo/gmd:MD_Distribution");
                    if (node != null) {
                        log.debug("Have MD_Distribution");
                        // create new transferOptions
                        TransferOption transferOptions = new TransferOption();
                        transferOptions.setOnlineRses(toOnlineResources(offerings));

                        Node importedTransferOptions = node.getOwnerDocument()
                                .importNode(XmlUtils.createTransferOptions(transferOptions), true);
                        XmlUtils.cleanNamespaces(importedTransferOptions);
                        node.appendChild(importedTransferOptions);

                        Node transferNode = XPathUtils.getNode(importedTransferOptions, "./gmd:MD_DigitalTransferOptions");
                        if (transferNode == null) {
                            log.debug("Have no transfer options");
                        } else {
                            log.debug("Have transfer options");
                        }
                        transferOptions.setSelf(transferNode);
                    } else {
                        log.debug("Have no MD_Distribution");
                        // create new distributionInfo
                        Distribution distribution = new Distribution();
                        List<TransferOption> list = new ArrayList<>();
                        TransferOption transferOptions = new TransferOption();
                        transferOptions.setOnlineRses(toOnlineResources(offerings));
                        list.add(transferOptions);
                        distribution.setTransferOptions(list);

                        Node importedDistribution = isoDoc.importNode(XmlUtils
                                .createDistributionInfo(distribution), true);
                        Node nodeRef = getDistributionNodeRef(isoDoc);
                        if (nodeRef != null) {
                            nodeRef.getParentNode().insertBefore(importedDistribution, nodeRef);
                        } else {
                            nodeRef = XPathUtils.getNode(isoDoc, "./gmi:MI_Metadata");
                            if (nodeRef == null) {
                                XPathUtils.getNode(isoDoc, "./gmd:MD_Metadata");
                            }
                            if (nodeRef != null) {
                                nodeRef.appendChild(importedDistribution);
                            }
                        }
                    }
                }
            }
        }

        //return newMetadataDoc;
    }

    private List<OnlineResource> toOnlineResources(List<Offering> offerings) {
        List<OnlineResource> list = new ArrayList<>();

        offerings.stream().filter((offering) -> (offering.getOperations() != null)).forEachOrdered((offering) -> {
            offering.getOperations().stream().filter((operation) -> (StringUtils.isNotEmpty(operation.getUrl()))).map((operation) -> {
                OnlineResource onlineRs = new OnlineResource();
                onlineRs.setLinkage(operation.getUrl());
                onlineRs.setProtocol(operation.getProtocol());
                onlineRs.setFunction(operation.getFunction());
                onlineRs.setName(operation.getCode());
                return onlineRs;
            }).forEachOrdered((onlineRs) -> {
                list.add(onlineRs);
            });
        });
        return list;
    }

    private void appendOnlineResourceNodes(Node parent, List<Offering> offerings) {
        offerings.stream().filter((offering) -> (offering.getOperations() != null)).forEachOrdered((offering) -> {
            offering.getOperations().stream().filter((operation) -> (StringUtils.isNotEmpty(operation.getUrl()))).map((operation) -> {
                OnlineResource onlineRs = new OnlineResource();
                onlineRs.setLinkage(operation.getUrl());
                onlineRs.setProtocol(operation.getProtocol());
                onlineRs.setFunction(operation.getFunction());
                onlineRs.setName(operation.getCode());
                return onlineRs;
            }).forEachOrdered((onlineRs) -> {
                /*
                    check if the dedicated online resource does already exist
                 */
                StringBuilder sb = new StringBuilder();
                sb.append("./gmd:onLine[gmd:CI_OnlineResource[");
                sb.append("(gmd:linkage/gmd:URL='").append(onlineRs.getLinkage()).append("')");
                if (StringUtils.isNotEmpty(onlineRs.getProtocol())) {
                    sb.append("and (gmd:protocol/gco:CharacterString='").append(onlineRs.getProtocol()).append("')");
                }
                if (StringUtils.isNotEmpty(onlineRs.getName())) {
                    sb.append("and (gmd:name/gco:CharacterString='").append(onlineRs.getName()).append("')");
                }
                if (StringUtils.isNotEmpty(onlineRs.getFunction())) {
                    sb.append("and (gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='").append(onlineRs.getFunction()).append("')");
                }
                sb.append("]]");

                log.debug("Online resource XPATH: " + sb.toString());

                Node olRsNode = XPathUtils.getNode(parent, sb.toString());
                if (olRsNode == null) {
                    Node importedNode = parent.getOwnerDocument()
                            .importNode(XmlUtils.createOnlineResourceNode(onlineRs), true);
                    XmlUtils.cleanNamespaces(importedNode);
                    parent.appendChild(importedNode);
                } else {
                    log.debug("Online resource " + onlineRs.getLinkage() + " does already exist");
                }
            });
        });
    }

    public void buildMetadata(Document metadataDoc, Metadata metadata) throws IOException, ParseException {
        log.debug("MNG Build metadata model from DOM");
        if (config.isOmitXmlComments()) {
            cleanXmlComments(metadataDoc);
        }

        //try {
        Others others = new Others();
        metadata.setOthers(others);

//        Map<String, String> docNamespaces = XPathUtils.getNamespaces();
//        NamedNodeMap atts = metadataDoc.getDocumentElement().getAttributes();
//        for (int i = 0; i < atts.getLength(); i++) {
//            Node node = atts.item(i);
//            String name = node.getNodeName();
//            String value = node.getNodeValue();
//
//            if ((name != null && name.startsWith("xmlns:"))) {
//                String prefix = StringUtils.substringAfter(name, "xmlns:");
//                if (docNamespaces.containsKey(prefix)
//                        && value.equalsIgnoreCase(docNamespaces.get(prefix))) {
//                    docNamespaces.remove(prefix);
//                }
//            }
//        }
//
//        // add missing prefixes/namespaces
//        for (Map.Entry<String, String> entry : docNamespaces.entrySet()) {
//            log.debug("Add namespace " + entry.getKey() + ":" + entry.getValue());
//            metadataDoc.getDocumentElement()
//                    .setAttributeNS(Constants.XML_NS, "xmlns:" + entry.getKey(), entry.getValue());
//        }
        String metadataType = XPathUtils.getAttributeValue(metadataDoc, ".//gmd:hierarchyLevel/gmd:MD_ScopeCode", "codeListValue");

        boolean expectedType = false;
        if (StringUtils.isNotEmpty(metadataType)) {
            if (metadataType.equals("series")) {
                metadata.toSeries();
                expectedType = true;
            }
            if (metadataType.equals("service")) {
                metadata.toService();
                expectedType = true;
            }
        }

        if (!expectedType) {
            throw new IOException("The metadata record is neither collection nor service");
        }

        /*
             get gmi:MI_Metadata node
         */
        Node selfNode = XPathUtils.getNode(metadataDoc, "./gmi:MI_Metadata");
        if (selfNode == null) {
            selfNode = XPathUtils.getNode(metadataDoc, "./gmd:MD_Metadata");
        }

        if (selfNode == null) {
            throw new IOException("The metadata record is neither collection nor service");
        } else {
            metadata.setSelf(selfNode);
        }

        /*
             Parse fileIdentifier
         */
        Node fileIdNode = XPathUtils.getNode(selfNode,
                "./gmd:fileIdentifier/gco:CharacterString");
        if (fileIdNode != null) {
            others.setFileIdentifierNode(fileIdNode);
            others.setFileIdentifier(XmlUtils.getNodeValue(fileIdNode));
        }

        /*
             Parse language
         */
        Node langNode = XPathUtils.getNode(selfNode,
                "./gmd:language/gmd:LanguageCode");
        if (langNode != null) {
            others.setLanguageNode(langNode);
            others.setLanguage(XmlUtils.getNodeAttValue(langNode, "codeListValue"));
        }

        /*
             Parse gmd:metadataStandardName
         */
        Node standardNameNode = XPathUtils.getNode(selfNode,
                "./gmd:metadataStandardName/gco:CharacterString");
        if (standardNameNode != null) {
            others.setStandardNameNode(standardNameNode);
            others.setStandardName(XmlUtils.getNodeValue(standardNameNode));
        }

        /*
             Parse gmd:metadataStandardVersion
         */
        Node standardVersionNode = XPathUtils.getNode(selfNode,
                "./gmd:metadataStandardVersion/gco:CharacterString");
        if (standardVersionNode != null) {
            others.setStandardVersionNode(standardVersionNode);
            others.setStandardVersion(XmlUtils.getNodeValue(standardVersionNode));
        }

        /*
             Parse contact
         */
        NodeList contactNL = XPathUtils.getNodes(selfNode, "./gmd:contact");
        if (contactNL != null && contactNL.getLength() > 0) {
            if (others.getContacts() == null) {
                others.setContacts(new ArrayList<>());
            }

            for (int i = 0; i < contactNL.getLength(); i++) {
                Node contactNode = contactNL.item(i);
                Contact contact = buildContact(contactNode);
                if (contact != null) {
                    others.getContacts().add(contact);
                }
            }
        }

        Node mDateNode = XPathUtils.getNode(selfNode, "./gmd:dateStamp");
        if (mDateNode != null) {
            others.setLastUpdateDateNode(mDateNode);

            Node dateNode = XPathUtils.getNode(mDateNode, "./gco:Date");
            if (dateNode != null) {
                others.setLastUpdateDate(XmlUtils.getNodeValue(dateNode));
            } else {
                Node dateTimeNode = XPathUtils.getNode(mDateNode, "./gco:DateTime");
                if (dateTimeNode != null) {
                    others.setLastUpdateDate(XmlUtils.getNodeValue(dateTimeNode));
                }
            }
        }

        /*
             Parse gmd:identificationInfo
         */
        Node dataIdNode = XPathUtils.getNode(selfNode,
                "./gmd:identificationInfo/gmd:MD_DataIdentification");
        if (dataIdNode == null) {
            dataIdNode = XPathUtils.getNode(selfNode,
                    "./gmd:identificationInfo/srv:SV_ServiceIdentification");
        }

        if (dataIdNode != null) {
            buildIdentification(dataIdNode, metadata);
        }

        /*
        Parse gmd:contentInfo to obtain processing levels
         */
        NodeList contentInfoList = XPathUtils.getNodes(selfNode, "./gmd:contentInfo");
        if (contentInfoList != null) {
            for (int i = 0; i < contentInfoList.getLength(); i++) {
                Node contentInfoNode = contentInfoList.item(i);
                String pLevel = XPathUtils.getNodeValue(contentInfoNode,
                        "./gmi:MI_ImageDescription/gmd:processingLevelCode/gmd:RS_Identifier/gmd:code");
                if (StringUtils.isEmpty(pLevel)) {
                    pLevel = XPathUtils.getNodeValue(contentInfoNode,
                            "./gmd:MD_ImageDescription/gmd:processingLevelCode/gmd:RS_Identifier/gmd:code");
                }
                if (StringUtils.isNotEmpty(pLevel)) {
                    metadata.addProcessingLevel(pLevel, contentInfoNode);
                }
            }
        }

        /*
             Parse gmd:distributionInfo
         */
        Node distributionNode = XPathUtils.getNode(selfNode,
                "./gmd:distributionInfo/gmd:MD_Distribution");
        if (distributionNode != null) {
//            boolean hasOffering = false;
//            if (metadata.getIdentification() != null
//                    && metadata.getIdentification().getJsonSupplementalNode() != null) {
//                hasOffering = true;
//            }
            metadata.setDistribution(XmlUtils
                    .buildDistribution(distributionNode, metadata.hasOfferingOperation(), config));
            //buildDistribution(distributionNode, metadata);
        }

        /*
             * Parse gmi:acquisitionInformation
         */
        Node acquisitionNode = XPathUtils.getNode(selfNode,
                "./gmi:acquisitionInformation/gmi:MI_AcquisitionInformation");
        if (acquisitionNode != null) {
            buildAcquisition(metadata, acquisitionNode);
        }

        /*
             * Parse gmd:dataQualityInfo
         */
        Node dataQualityInfoNode = XPathUtils
                .getNode(selfNode, "./gmd:dataQualityInfo");
        if (dataQualityInfoNode != null) {
            metadata.setDataQualityInfo(dataQualityInfoNode);
        }

        /*
             * Parse gmd:portrayalCatalogueInfo
         */
        Node portrayalNode = XPathUtils
                .getNode(selfNode, "./gmd:portrayalCatalogueInfo");
        if (portrayalNode != null) {
            metadata.setPortrayalCatalogueInfo(portrayalNode);
        }

        /*
             * Parse gmd:metadataConstraints
         */
        Node metadataConstraintsNode = XPathUtils
                .getNode(selfNode, "./gmd:metadataConstraints");
        if (metadataConstraintsNode != null) {
            metadata.setMetadataConstraints(metadataConstraintsNode);
        }

        /*
             * Parse gmd:applicationSchemaInfo
         */
        Node applicationSchemaInfoNode = XPathUtils
                .getNode(selfNode, "./gmd:applicationSchemaInfo");
        if (applicationSchemaInfoNode != null) {
            metadata.setApplicationSchemaInfo(applicationSchemaInfoNode);
        }

        /*
             * Parse gmd:metadataMaintenance
         */
        Node metadataMaintenanceNode = XPathUtils
                .getNode(selfNode, "./gmd:metadataMaintenance");
        if (metadataMaintenanceNode != null) {
            metadata.setMetadataMaintenance(metadataMaintenanceNode);
        }

        /*
             * Parse gmd:metadata
         */
        Node seriesNode = XPathUtils
                .getNode(selfNode, "./gmd:series");
        if (seriesNode != null) {
            metadata.setSeries(seriesNode);
        }

        /*
             * Parse gmd:describes
         */
        Node describesNode = XPathUtils
                .getNode(selfNode, "./gmd:describes");
        if (describesNode != null) {
            metadata.setDescribes(describesNode);
        }

        /*
             * Parse gmd:propertyType
         */
        Node propertyTypeNode = XPathUtils
                .getNode(selfNode, "./gmd:propertyType");
        if (propertyTypeNode != null) {
            metadata.setPropertyType(propertyTypeNode);
        }

        /*
             * Parse gmd:featureType
         */
        Node featureTypeNode = XPathUtils
                .getNode(selfNode, "./gmd:featureType");
        if (featureTypeNode != null) {
            metadata.setFeatureType(featureTypeNode);
        }

        /*
             * Parse gmd:featureAttribute
         */
        Node featureAttributeNode = XPathUtils
                .getNode(selfNode, "./gmd:featureAttribute");
        if (featureAttributeNode != null) {
            metadata.setFeatureAttribute(featureAttributeNode);
        }

        /*
             * get gmi:acquisitionInformation Node
         */
        Node acquisitionInfoNode = XPathUtils
                .getNode(selfNode, "./gmi:acquisitionInformation");
        if (acquisitionInfoNode != null) {
            metadata.setAcquisitionNode(acquisitionInfoNode);
        }
//        } catch (XPathExpressionException e) {
//            log.debug("Error while building Metadata object from XML: " + e.getMessage());
//            throw new IOException(e);
//        }

        //series.offeringsToOnlineResources();
    }

    private void buildIdentification(Node dataIdNode, Metadata metadata) throws ParseException {

        int type = 1;
        if (metadata.isService()) {
            type = 2;
        }
        Identification identification = new Identification(config, type);
        identification.setDataId(dataIdNode);
        metadata.setIdentification(identification);

        /*
             parse citation
         */
        Node ciCitationNode = XPathUtils
                .getNode(dataIdNode, "./gmd:citation/gmd:CI_Citation");
        if (ciCitationNode != null) {
            identification.setCiCitation(ciCitationNode);

            Node titleNode = XPathUtils
                    .getNode(ciCitationNode, "./gmd:title/gco:CharacterString");
            if (titleNode != null) {
                identification.setTitleNode(titleNode);
                identification.setTitle(XmlUtils.getNodeValue(titleNode));
            }

            Node altTitleNode = XPathUtils
                    .getNode(ciCitationNode, "./gmd:alternateTitle/gco:CharacterString");
            if (altTitleNode != null) {
                identification.setAltTitleNode(altTitleNode);
                identification.setAltTitle(XmlUtils.getNodeValue(altTitleNode));
            }

            Node editionNode = XPathUtils
                    .getNode(ciCitationNode, "./gmd:edition/gco:CharacterString");
            if (editionNode != null) {
                //identification.setEditionNode(editionNode);
                identification.setEdition(XmlUtils.getNodeValue(editionNode));
            }

            Node doiNode = XPathUtils
                    .getNode(ciCitationNode, "./gmd:identifier/gmd:RS_Identifier[gmd:codeSpace/gco:CharacterString='http://doi.org']/gmd:code/gco:CharacterString");
            if (doiNode != null) {
                //identification.setDoiNode(doiNode);
                identification.setDoi(XmlUtils.getNodeValue(doiNode));
            }

            NodeList dateNodes = XPathUtils.getNodes(ciCitationNode, "./gmd:date/gmd:CI_Date");
            if (dateNodes != null && dateNodes.getLength() > 0) {
                for (int i = 0; i < dateNodes.getLength(); i++) {
                    Node ciDateNode = dateNodes.item(i);

                    Node dateNode = XPathUtils.getNode(ciDateNode, "./gmd:date/gco:Date");
                    if (dateNode == null) {
                        dateNode = XPathUtils.getNode(ciDateNode, "./gmd:date/gco:DateTime");
                    }

                    if (dateNode != null) {
                        Date dateValue = CommonUtils.toDate(XmlUtils.getNodeValue(dateNode));
                        String dateType = XPathUtils.getAttributeValue(ciDateNode, "./gmd:dateType/gmd:CI_DateTypeCode", "codeListValue");

                        if (StringUtils.isNotEmpty(dateType)) {
                            if ("creation".equalsIgnoreCase(dateType)) {
                                identification.setCreationDateNode(dateNode);
                                identification.setCreationDate(dateValue);
                            }
                            if ("revision".equalsIgnoreCase(dateType)) {
                                identification.setRevisionDateNode(dateNode);
                                identification.setRevisionDate(dateValue);
                            }
                            if ("publication".equalsIgnoreCase(dateType)) {
                                identification.setPublicationDateNode(dateNode);
                                identification.setPublicationDate(dateValue);
                            }
                        }
                    }
                }
            }

            // gmd:otherCitationDetails
            Node otherCitationDetailsNode = XPathUtils
                    .getNode(ciCitationNode, "./gmd:otherCitationDetails/gco:CharacterString");
            if (otherCitationDetailsNode != null) {
                //identification.setOtherCitationDetailsNode(otherCitationDetailsNode);
                identification.setOtherCitationDetails(XmlUtils.getNodeValue(otherCitationDetailsNode));
            }
        }

        /*
             parse abstract
         */
        Node absNode = XPathUtils
                .getNode(dataIdNode, "./gmd:abstract/gco:CharacterString");
        if (absNode != null) {
            identification.setPlainTextAbstractNode(absNode);
            identification.setPlainTextAbstract(XmlUtils.getNodeValue(absNode));
        }

        /*
             parse purpose
         */
        Node purposeNode = XPathUtils
                .getNode(dataIdNode, "./gmd:purpose/gco:CharacterString");
        if (purposeNode != null) {
            identification.setPurposeNode(purposeNode);
            identification.setPurpose(XmlUtils.getNodeValue(purposeNode));
        }

        /*
             parse status
         */
        Node statusNode = XPathUtils.getNode(dataIdNode, "./gmd:status/gmd:MD_ProgressCode");
        if (statusNode != null) {
            identification.setStatusNode(statusNode);
            identification.setStatusList(XmlUtils.getNodeAttValue(statusNode, "codeList"));
            identification.setStatusListValue(XmlUtils.getNodeAttValue(statusNode, "codeListValue"));
        }

        /*
            parse gmd:pointOfContact
         */
        NodeList contactNL = XPathUtils.getNodes(dataIdNode, "./gmd:pointOfContact");
        if (contactNL != null && contactNL.getLength() > 0) {
            if (identification.getPointOfContacts() == null) {
                identification.setPointOfContacts(new ArrayList<>());
            }

            for (int i = 0; i < contactNL.getLength(); i++) {
                Node contactNode = contactNL.item(i);
                Contact contact = buildContact(contactNode);
                if (contact != null) {
                    identification.getPointOfContacts().add(contact);
                }
            }
        }

        /*
             parse topicCategory
         */
        Node topicCategoryNode = XPathUtils.getNode(dataIdNode, "./gmd:topicCategory/gmd:MD_TopicCategoryCode");
        if (topicCategoryNode != null) {
            identification.setTopicCategoryNode(topicCategoryNode);
            identification.setTopicCategory(XmlUtils.getNodeValue(topicCategoryNode));
        }

        /*
             parse extent
         */
        NodeList extentNL = XPathUtils
                .getNodes(dataIdNode, "./gmd:extent/gmd:EX_Extent");
        if (extentNL == null || extentNL.getLength() == 0) {
            log.debug("Looking for srv:extent");
            extentNL = XPathUtils
                    .getNodes(dataIdNode, "./srv:extent/gmd:EX_Extent");
        }

        log.debug("extentNL = " + extentNL);

        if (extentNL != null && extentNL.getLength() > 0) {
            for (int j = 0; j < extentNL.getLength(); j++) {
                Node extentNode = extentNL.item(j);

                /*
                     parse temporal
                 */
                NodeList temporalNL = XPathUtils
                        .getNodes(extentNode, "./gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent");
                if (temporalNL != null && temporalNL.getLength() > 0) {
                    /*
                         if (identification.getTemporal() == null) {
                         identification.setTemporal(new ArrayList<Temporal>());
                         }*/

                    GmxAnchor desc = XmlUtils.buildStringProperty(extentNode, "./gmd:description");
                    if (desc == null) {
                        desc = new GmxAnchor();
                    }

                    for (int k = 0; k < temporalNL.getLength(); k++) {
                        Node temporalNode = temporalNL.item(k);
                        Temporal temporal = new Temporal();
                        //identification.getTemporal().add(temporal);

                        Node startNode = XPathUtils
                                .getNode(temporalNode, "./gml:TimePeriod/gml:beginPosition");
                        log.debug(startNode);

                        if (startNode == null) {
                            startNode = XPathUtils
                                    .getNode(temporalNode, "./gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition");
                        }
                        log.debug(startNode);

                        if (startNode != null) {
                            temporal.setnStart(startNode);

                            String startDate = XmlUtils.getNodeValue(startNode);
                            if (StringUtils.isNotEmpty(startDate)) {
                                log.debug("startDate = " + startDate);
                                temporal.setStartDate(CommonUtils.toDate(startDate));

                                log.debug("startDate = " + temporal.getStartDate());
                            }
                        }

                        Node endNode = XPathUtils
                                .getNode(temporalNode, "./gml:TimePeriod/gml:endPosition");
                        log.debug(endNode);

                        if (endNode == null) {
                            endNode = XPathUtils
                                    .getNode(temporalNode, "./gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition");
                        }
                        log.debug(endNode);

                        if (endNode != null) {
                            temporal.setnEnd(endNode);
                            String endDate = XmlUtils.getNodeValue(endNode);
                            if (StringUtils.isNotEmpty(endDate)) {
                                temporal.setEndDate(CommonUtils.toDate(endDate));
                            }
                        }

                        if (startNode != null && endNode != null) {
                            temporal.setSelf(extentNode);
                            temporal.setDescription(desc);
                            identification.setTemporal(temporal);
                            break;
                        }
                    }
                }

                /*
                     parse geographic
                 */
                NodeList geoNL = XPathUtils
                        .getNodes(extentNode, "./gmd:geographicElement/gmd:EX_GeographicBoundingBox");
                if (geoNL != null && geoNL.getLength() > 0) {
                    /*
                         if (identification.getGeographic() == null) {
                         identification.setGeographic(new ArrayList<Bbox>());
                         }
                     */

                    for (int k = 0; k < geoNL.getLength(); k++) {
                        Node geoNode = geoNL.item(k);

                        Node westNode = XPathUtils
                                .getNode(geoNode, "./gmd:westBoundLongitude/gco:Decimal");
                        if (westNode != null) {
                            Bbox bbox = new Bbox();
                            bbox.setSelf(geoNode);

                            //identification.getGeographic().add(bbox);
                            bbox.setnWest(westNode);
                            bbox.setWest(Double.parseDouble(XmlUtils.getNodeValue(westNode)));

                            Node eastNode = XPathUtils
                                    .getNode(geoNode, "./gmd:eastBoundLongitude/gco:Decimal");
                            if (eastNode != null) {
                                bbox.setnEast(eastNode);
                                bbox.setEast(Double.parseDouble(XmlUtils.getNodeValue(eastNode)));
                            }

                            Node southNode = XPathUtils
                                    .getNode(geoNode, "./gmd:southBoundLatitude/gco:Decimal");
                            if (southNode != null) {
                                bbox.setnSouth(southNode);
                                bbox.setSouth(Double.parseDouble(XmlUtils.getNodeValue(southNode)));
                            }

                            Node northNode = XPathUtils
                                    .getNode(geoNode, "./gmd:northBoundLatitude/gco:Decimal");
                            if (northNode != null) {
                                bbox.setnNorth(northNode);
                                bbox.setNorth(Double.parseDouble(XmlUtils.getNodeValue(northNode)));
                            }

                            identification.setBbox(bbox);
                            break;
                        }
                    }
                }
            }
        }

        /*
            GCMD science keyword
         */
        Map<String, String> scienceKeywords = new HashMap<>();
        log.debug("MNG: CHECK VERSION OF GCMD SCKW of " + metadata.getOthers().getFileIdentifier());
        Node scKwNode = XPathUtils.getNode(dataIdNode, "./gmd:descriptiveKeywords/gmd:MD_Keywords[./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='" + config.getSckwThesaurusUri() + "' or ./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='" + config.getOldSckwThesaurusUri() + "']");

        if (scKwNode != null) {
            List<Keyword> keywords = getKeywords(scKwNode);
            if (keywords != null && keywords.size() > 0) {
                keywords.forEach((kw) -> {
                    if (StringUtils.isNotEmpty(kw.getUri())) {
                        scienceKeywords.put(kw.getUri(), kw.getLabel());
//                        String sckwConceptPrefix = config.getSckwThesaurus().getUriSpace();
//                        if (StringUtils.startsWithIgnoreCase(kw.getUri(), sckwConceptPrefix)) {
//                            scienceKeywords.put(kw.getUri(), kw.getLabel());
//                        } else {
//                            String newUri = sckwConceptPrefix + "/" + StringUtils.substringAfterLast(kw.getUri(), "/");
//                            scienceKeywords.put(newUri, kw.getLabel());
//                        }
                    }
                });
                identification.setNoMappingScienceKeywords(scienceKeywords);
            }
        } else {
            log.debug("NO GCMD SCKW");
        }

        /*
            Instrument type keywords 
         */
        Map<String, String> instTypeKeywords = new HashMap<>();
        Node instTypeKwNode = XPathUtils.getNode(dataIdNode, "./gmd:descriptiveKeywords/gmd:MD_Keywords[./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='" + config.getInstrumentThesaurusUri() + "']");
        if (instTypeKwNode != null) {
            List<Keyword> keywords = getKeywords(instTypeKwNode);
            if (keywords != null && keywords.size() > 0) {
                keywords.forEach((kw) -> {
                    if (StringUtils.isNotEmpty(kw.getUri())) {
                        instTypeKeywords.put(kw.getUri(), kw.getLabel());
//                        String instConceptPrefix = config.getInstrumentThesaurus().getUriSpace();
//                        if (StringUtils.startsWithIgnoreCase(kw.getUri(), instConceptPrefix)) {
//                            instTypeKeywords.put(kw.getUri(), kw.getLabel());
//                        } else {
//                            String newUri = instConceptPrefix + "/" + StringUtils.substringAfterLast(kw.getUri(), "/");
//                            instTypeKeywords.put(newUri, kw.getLabel());
//                        }
                    }
                });
                identification.setInstrumentTypeKeywords(instTypeKeywords);
            }
        } else {
            log.debug("NO Instrument type keyword");
        }

        /*
             Parse keyword
         */
        NodeList mdKeywords = XPathUtils
                .getNodes(dataIdNode, "./gmd:descriptiveKeywords/gmd:MD_Keywords");
        if (mdKeywords != null && mdKeywords.getLength() > 0) {
            List<EarthTopic> earthTopics = new ArrayList<>();
            identification.setEarthTopics(earthTopics);

            for (int i = 0; i < mdKeywords.getLength(); i++) {
                Node mdKeywordNode = mdKeywords.item(i);
                List<Keyword> keywords = getKeywords(mdKeywordNode);

                if (keywords != null && keywords.size() > 0) {
                    /*
                        Check the thesaurus
                     */
                    String codeListValue = null;
                    Node kwTypeNode = XPathUtils
                            .getNode(mdKeywordNode, "./gmd:type/gmd:MD_KeywordTypeCode");
                    if (kwTypeNode != null) {
                        codeListValue = XmlUtils.getNodeAttValue(kwTypeNode, "codeListValue");
                    }

                    Node thesaurusNode = XPathUtils
                            .getNode(mdKeywordNode, "./gmd:thesaurusName");
                    if (thesaurusNode != null) {
                        Node thesaurusTitleNode = XPathUtils
                                .getNode(mdKeywordNode, "./gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor");
                        if (thesaurusTitleNode != null) {
                            String thesaurusUri = XmlUtils.getNodeAttValue(thesaurusTitleNode, Constants.XLINK_NS, "href");
                            if (StringUtils.isNotEmpty(thesaurusUri)) {
                                if (config.getEarthtopicsThesaurusUri().equalsIgnoreCase(thesaurusUri)) {
                                    // Earth Topic keywords                                   

                                    keywords.forEach((kw) -> {
                                        if (StringUtils.isNotEmpty(kw.getUri())
                                                && kw.getUri().startsWith(config.getEarthtopicThesaurus().getUriSpace())) {
                                            earthTopics.add(MetadataUtils.createEarthTopic(kw.getUri(), kw.getLabel(),
                                                    metadata.getOthers().getFileIdentifier(), scienceKeywords, config, metadata.isService() ? 2 : 1));
                                        } else {
                                            //identification.addCorrection("Removed Earth Topics keyword " + kw.getLabel() + " because it has no URI");
                                            log.debug("Earth Topics keyword " + kw.getLabel() + " has no URI");
                                            // display warning
                                        }
                                    });
                                } else {
                                    if (config.getEopThesaurus().getTitleUri().equalsIgnoreCase(thesaurusUri)
                                            || config.getEopExtThesaurus().getTitleUri().equalsIgnoreCase(thesaurusUri)) {
                                        // EOP 2.1 or EOP Ext keywords

                                        Thesaurus eopThesaurus;
                                        if (config.getEopThesaurus().getTitleUri().equalsIgnoreCase(thesaurusUri)) {
                                            eopThesaurus = new Thesaurus(config.getEopThesaurus());
                                        } else {
                                            eopThesaurus = new Thesaurus(config.getEopExtThesaurus());
                                        }

                                        String date = XPathUtils.getNodeValue(mdKeywordNode,
                                                "./gmd:thesaurusName/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date");
                                        if (StringUtils.isNotEmpty(date)) {
                                            eopThesaurus.setDate(date);
                                        }

                                        String dateType = XPathUtils.getNodeValue(mdKeywordNode,
                                                "./gmd:thesaurusName/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode");
                                        if (StringUtils.isNotEmpty(dateType)) {
                                            eopThesaurus.setDateType(dateType);
                                        }

                                        String dateCodeList = XPathUtils.getAttributeValue(mdKeywordNode,
                                                "./gmd:thesaurusName/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode", "codeList");
                                        if (StringUtils.isNotEmpty(dateCodeList)) {
                                            eopThesaurus.setDateTypeCodeList(dateCodeList);
                                        }

                                        String dateCodeListValue = XPathUtils.getAttributeValue(mdKeywordNode,
                                                "./gmd:thesaurusName/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode", "codeListValue");
                                        if (StringUtils.isNotEmpty(dateCodeListValue)) {
                                            eopThesaurus.setDateTypeCodeListValue(dateCodeListValue);
                                        }

                                        if (StringUtils.isNotEmpty(codeListValue)) {
                                            switch (codeListValue) {
                                                case Constants.ORBITTYPE_KEY:
                                                    identification.getOrbitType().setThesaurus(eopThesaurus);
                                                    identification.getOrbitType().setKeywords(keywords);
                                                    break;
                                                case Constants.WAVELENGTH_KEY:
                                                    identification.getWaveLength().setThesaurus(eopThesaurus);
                                                    identification.getWaveLength().setKeywords(keywords);
                                                    break;
                                                case Constants.PROCESSORVER_KEY:
                                                    identification.getProcessorVersion().setKeywords(keywords);
                                                    identification.getProcessorVersion().setThesaurus(eopThesaurus);
                                                    break;
                                                case Constants.RESOLUTION_KEY:
                                                    identification.getResolution().setThesaurus(eopThesaurus);
                                                    identification.getResolution().setKeywords(keywords);
                                                    break;
                                                case Constants.PRODUCTTYPE_KEY:
                                                    identification.getProductType().setKeywords(keywords);
                                                    identification.getProductType().setThesaurus(eopThesaurus);
                                                    break;
                                                case Constants.ORBITHEIGHT_KEY:
                                                    identification.getOrbitHeight().setKeywords(keywords);
                                                    identification.getOrbitHeight().setThesaurus(eopThesaurus);
                                                    break;
                                                case Constants.SWATHWIDTH_KEY:
                                                    identification.getSwathWidth().setKeywords(keywords);
                                                    identification.getSwathWidth().setThesaurus(eopThesaurus);
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // free keyword
                        if ("place".equals(codeListValue)) {
                            if (identification.getPlaceKeyword() == null) {
                                FreeKeyword placeKw = new FreeKeyword();
                                placeKw.setCodeListValue(codeListValue);
                                identification.setPlaceKeyword(placeKw);
                            }
                            keywords.forEach((kw) -> {
                                identification.getPlaceKeyword().addKeyword(kw);
                            });
                        } else {
                            if (identification.getFreeKeyword() == null) {
                                FreeKeyword freeKw = new FreeKeyword();
                                identification.setFreeKeyword(freeKw);
                            }
                            keywords.forEach((kw) -> {
                                identification.getFreeKeyword().addKeyword(kw);
                            });
                        }
                    }

                }
            }
        }

        if (!identification.isService()) {
            identification.getProcessorVersion().addEmptyKeywords();
            identification.getProductType().addEmptyKeywords();
            identification.getOrbitHeight().addEmptyKeywords();
            identification.getSwathWidth().addEmptyKeywords();
        }

        if (identification.getFreeKeyword() == null) {
            FreeKeyword freeKw = new FreeKeyword();
            identification.setFreeKeyword(freeKw);
        }
        identification.getFreeKeyword().addEmptyKeywords();

        if (identification.getPlaceKeyword() == null) {
            FreeKeyword placeKw = new FreeKeyword();
            placeKw.setCodeListValue("place");
            identification.setPlaceKeyword(placeKw);
        }
        identification.getPlaceKeyword().addEmptyKeywords();
        /*
             Add configured keywords to identification
         */
        //addConfigThesaurus(identification);

        /*
             parse language gmd:resourceSpecificUsageNode
         */
        Node resourceSpecificUsageNode = XPathUtils
                .getNode(dataIdNode, "./gmd:resourceSpecificUsage");
        if (resourceSpecificUsageNode != null) {
            identification.setResourceSpecificUsageNode(resourceSpecificUsageNode);
        }

        /*
             parse language
         */
        Node langNode = XPathUtils.getNode(dataIdNode, "./gmd:language");
        if (langNode != null) {
            identification.setLanguage(langNode);
        }

        /*
             parse gmd:resourceConstraints
         */
//        Node resourceConstraintsNode = XPathUtils
//                .getNode(dataIdNode, "./gmd:resourceConstraints");
//        if (resourceConstraintsNode != null) {
//            identification.setResourceConstraintsNode(resourceConstraintsNode);
//        }

        /*
             parse gmd:resourceConstraints
         */
        NodeList constraintsNL = XPathUtils
                .getNodes(dataIdNode, "./gmd:resourceConstraints/gmd:MD_LegalConstraints");
        if (constraintsNL != null
                && constraintsNL.getLength() > 0) {
            identification.setConstraints(new ArrayList<>());
            log.debug("Number of constraints " + constraintsNL.getLength());
            for (int i = 0; i < constraintsNL.getLength(); i++) {
                identification.getConstraints().add(buildConstraints(constraintsNL.item(i), metadata.isService()));
            }
        }

        /*
             parse gmd:aggregationInfo
         */
        Node aggregationInfoNode = XPathUtils
                .getNode(dataIdNode, "./gmd:aggregationInfo");
        if (aggregationInfoNode != null) {
            identification.setAggregationInfoNode(aggregationInfoNode);
        }

        /*
             parse gmd:spatialRepresentationType
         */
        Node spatialRepresentationTypeNode = XPathUtils
                .getNode(dataIdNode, "./gmd:spatialRepresentationType");
        if (spatialRepresentationTypeNode != null) {
            identification.setSpatialRepresentationTypeNode(spatialRepresentationTypeNode);
        }

        /*
             parse gmd:spatialResolution
         */
        Node spatialResolutionNode = XPathUtils
                .getNode(dataIdNode, "./gmd:spatialResolution");
        if (spatialResolutionNode != null) {
            identification.setSpatialResolutionNode(spatialResolutionNode);
        }

        /*
             parse gmd:supplementalInformation
         */
        NodeList supplementalNodeForRefs = XPathUtils
                .getNodes(dataIdNode, "./gmd:supplementalInformation");
        if (supplementalNodeForRefs != null
                && supplementalNodeForRefs.getLength() > 0) {
            identification.setSupplementalNodeForRef(supplementalNodeForRefs.item(0));
        }

//        NodeList supplementalInfoNodes = XPathUtils
//                .getNodes(dataIdNode, "./gmd:supplementalInformation/gco:CharacterString");
//
//        if (supplementalInfoNodes != null
//                && supplementalInfoNodes.getLength() > 0) {
//            for (int i = 0; i < supplementalInfoNodes.getLength(); i++) {
//                Node suppNode = supplementalInfoNodes.item(i);
//                NodeList list = suppNode.getChildNodes();
//                log.debug("Number of nodes: " + list.getLength());
//                String cdata = "";
//                CharacterData cdataNode = null;
//                try {
//                    for (int index = 0; index < list.getLength(); index++) {
//                        if (list.item(index).getNodeType() == Node.CDATA_SECTION_NODE) {
//                            cdataNode = (CharacterData) list.item(index);
//                            cdata = StringUtils.trimToEmpty(cdataNode.getData());
//                            if (StringUtils.isNotEmpty(cdata)) {
//                                log.debug("This is a CDATA node");
//                                break;
//                            }
//                        }
//                    }
//                    if (cdataNode != null && StringUtils.isNotEmpty(cdata)) {
//                        geoJsonParser.obtainSupplementalInfo(cdata, metadata);
//                        identification.setJsonSupplementalNode(cdataNode);
//                        break;
//                    }
//                } catch (Exception e) {
//
//                }
//            }
//        }

        /*
            service information
         */
        Node serviceTypeNode = XPathUtils.getNode(dataIdNode, "./srv:serviceType");
        if (serviceTypeNode != null) {
            ServiceType serviceType = new ServiceType();
            serviceType.setSelf(serviceTypeNode);

            Node node = XPathUtils.getNode(serviceTypeNode, "./gco:LocalName");
            if (node == null) {
                node = XPathUtils.getNode(serviceTypeNode, "./gco:ScopedName");
            }

            if (node != null) {

                serviceType.setType(node.getLocalName());
                serviceType.setCode(XmlUtils.getNodeAttValue(node, "codeSpace"));
                serviceType.setValue(XmlUtils.getNodeValue(node));
            }
            log.debug("serviceType.getType() = " + serviceType.getType());
            log.debug("serviceType.getValue() = " + serviceType.getValue());
            log.debug("serviceType.getCode() = " + serviceType.getCode());
            identification.setServiceType(serviceType);
        } else {
            log.debug("No service type");
            if (metadata.isService()) {
                identification.setServiceType(new ServiceType());
            }
        }
    }

    private Contact buildContact(Node contactNode) {
        if (contactNode != null) {

            Contact contact = new Contact();

            Node responsiblePartyNode = XPathUtils
                    .getNode(contactNode, "./gmd:CI_ResponsibleParty");
            if (responsiblePartyNode != null) {
                contact.setResponsibleParty(responsiblePartyNode);

                Node indNode = XPathUtils
                        .getNode(responsiblePartyNode, "./gmd:individualName/gco:CharacterString");
                if (indNode != null) {
                    contact.setnIndividualName(indNode);
                    contact.setIndividualName(XmlUtils.getNodeValue(indNode));
                }

                Node orgNode = XPathUtils
                        .getNode(responsiblePartyNode, "./gmd:organisationName/gco:CharacterString");
                if (orgNode != null) {
                    contact.setnOrgName(orgNode);
                    contact.setOrgName(XmlUtils.getNodeValue(orgNode));
                }

                Node positionNode = XPathUtils
                        .getNode(responsiblePartyNode, "./gmd:positionName/gco:CharacterString");
                if (positionNode != null) {
                    contact.setnPositionName(positionNode);
                    contact.setPositionName(XmlUtils.getNodeValue(positionNode));
                }

                Node contactInfoNode = XPathUtils
                        .getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact");
                if (contactInfoNode != null) {
                    contact.setCiContact(contactInfoNode);

                    Node telephoneNode = XPathUtils
                            .getNode(contactInfoNode, "./gmd:phone/gmd:CI_Telephone");
                    if (telephoneNode != null) {
                        contact.setCiTelephone(telephoneNode);

                        Node phoneNode = XPathUtils
                                .getNode(telephoneNode, "./gmd:voice/gco:CharacterString");
                        if (phoneNode != null) {
                            contact.setnPhone(phoneNode);
                            contact.setPhone(XmlUtils.getNodeValue(phoneNode));
                        }

                        Node faxNode = XPathUtils
                                .getNode(telephoneNode, "./gmd:facsimile/gco:CharacterString");
                        if (faxNode != null) {
                            contact.setnFax(faxNode);
                            contact.setFax(XmlUtils.getNodeValue(faxNode));
                        }
                    }

                    Node ciAddNode = XPathUtils
                            .getNode(contactInfoNode, "./gmd:address/gmd:CI_Address");
                    if (ciAddNode != null) {
                        contact.setCiAddress(ciAddNode);

                        Node addNode = XPathUtils
                                .getNode(ciAddNode, "./gmd:deliveryPoint/gco:CharacterString");
                        if (addNode != null) {
                            contact.setnAdd(addNode);
                            contact.setAddress(XmlUtils.getNodeValue(addNode));
                        }

                        Node cityNode = XPathUtils
                                .getNode(ciAddNode, "./gmd:city/gco:CharacterString");
                        if (cityNode != null) {
                            contact.setnCity(cityNode);
                            contact.setCity(XmlUtils.getNodeValue(cityNode));
                        }

                        Node postalNode = XPathUtils
                                .getNode(ciAddNode, "./gmd:postalCode/gco:CharacterString");
                        if (postalNode != null) {
                            contact.setnPostal(postalNode);
                            contact.setPostal(XmlUtils.getNodeValue(postalNode));
                        }

                        Node countryNode = XPathUtils
                                .getNode(ciAddNode, "./gmd:country/gco:CharacterString");
                        if (countryNode != null) {
                            contact.setnCountry(countryNode);
                            contact.setCountry(XmlUtils.getNodeValue(countryNode));
                        }

                        Node emailNode = XPathUtils
                                .getNode(ciAddNode, "./gmd:electronicMailAddress/gco:CharacterString");
                        if (emailNode != null) {
                            contact.setnEmail(emailNode);
                            contact.setEmail(XmlUtils.getNodeValue(emailNode));
                        }
                    }

                    Node ciOnlineRsNode = XPathUtils
                            .getNode(contactInfoNode, "./gmd:onlineResource/gmd:CI_OnlineResource");
                    if (ciOnlineRsNode != null) {
                        contact.setCiOnlineRs(ciOnlineRsNode);

                        Node linkNode = XPathUtils
                                .getNode(ciOnlineRsNode, "./gmd:linkage/gmd:URL");
                        if (linkNode != null) {
                            contact.setnOnlineRs(linkNode);
                            contact.setOnlineRs(XmlUtils.getNodeValue(linkNode));
                        }
                    }
                }

                Node roleNode = XPathUtils
                        .getNode(responsiblePartyNode, "./gmd:role/gmd:CI_RoleCode/@codeListValue");
                if (roleNode != null) {
                    contact.setnRole(roleNode);
                    contact.setRole(XmlUtils.getNodeValue(roleNode));
                }
            }
            return contact;

        }
        return null;
    }

    private Acquisition buildAcquisition(Metadata metadata, Node acquisitionNode) throws ParseException {

        Acquisition acquisition = new Acquisition();
        acquisition.setSelf(acquisitionNode);
        metadata.setAcquisition(acquisition);

        Node operationNode = XPathUtils.getNode(acquisitionNode, "./gmi:operation");
        if (operationNode != null) {
            acquisition.setOperation(operationNode);
            String missionStatus = XPathUtils.getAttributeValue(operationNode, "./gmi:MI_Operation/gmi:status/gmd:MD_ProgressCode", "codeListValue");
            acquisition.setMissionStatus(missionStatus);
        }

        NodeList platformNodes = XPathUtils
                .getNodes(acquisitionNode, "./gmi:platform");
        if (platformNodes != null && platformNodes.getLength() > 0) {
            if (acquisition.getPlatforms() == null) {
                acquisition.setPlatforms(new ArrayList<>());
            }
            int recordType = 1;
            if (metadata.isService()) {
                recordType = 2;
            }
            for (int i = 0; i < platformNodes.getLength(); i++) {
                Platform platform = buildPlatform(metadata.getOthers().getFileIdentifier(), recordType, platformNodes.item(i));
                acquisition.addPlatform(platform);
//                if (platform.getInstruments() != null
//                        && platform.getInstruments().size() > 0) {
//                    acquisition.addPlatform(platform);
//                } else {
//                    String label = platform.getLabel();
//                    if (StringUtils.isNotEmpty(platform.getUri())) {
//                        label += " (" + platform.getUri() + ")";
//                    }
//                    acquisition.addNoInstrumentPlatform(label);
//                }
            }
        }

        return acquisition;
    }

    private Instrument buildInstrument(String metadataId, int recordType, Node instrumentNode) {
        Instrument instrument = new Instrument();
        instrument.setSelf(instrumentNode);

        Node citationNode = XPathUtils
                .getNode(instrumentNode, "./gmi:MI_Instrument/gmi:citation/gmd:CI_Citation");
        if (citationNode != null) {
            GmxAnchor title = XmlUtils.buildStringProperty(citationNode, "./gmd:title");
            if (title != null) {
                instrument.setUri(title.getLink());
                instrument.setLabel(title.getText());
            }

            GmxAnchor identifier = XmlUtils.buildStringProperty(citationNode, "./gmi:identifier/gmd:MD_Identifier/gmd:code");
            if (identifier != null) {
                instrument.setIdentifier(identifier);
                if (instrument.getUri() == null || instrument.getUri().isEmpty()) {
                    instrument.setUri(identifier.getLink());
                }

                if (instrument.getLabel() == null || instrument.getLabel().isEmpty()) {
                    instrument.setLabel(identifier.getText());
                }
            }

            //String conceptKey = config.getInstrumentThesaurusUri() + Constants.SEPARATOR + instrument.getUri();
            log.debug("Instrument concept uri: " + instrument.getUri());

            if (StringUtils.isNotEmpty(instrument.getUri())
                    && instrument.getUri().startsWith(config.getInstrumentThesaurus().getUriSpace())) {
                GmxAnchor altTitle = XmlUtils.buildStringProperty(citationNode, "./gmd:alternateTitle");

                if (altTitle != null) {
                    if (StringUtils.isNotEmpty(altTitle.getLink())) {
                        log.debug("Instrument alt concept uri: " + altTitle.getLink());
                        Concept gcmd = config.getGcmdInstrument(altTitle.getLink());
                        if (gcmd != null) {
                            instrument.setGcmd(gcmd);
                            if (!gcmd.getLabel().equals(altTitle.getText())) {
                                //instrument.setAltTitle(altTitle);
                                // GCMD instrument label changed
                                instrument.setGcmdWarning(new AutoCorrectionWarning(metadataId,
                                        altTitle.getLink(), altTitle.getText(), gcmd.getLabel(),
                                        6, config.getGcmdInstrumentThesaurus().getLabel(), 1, recordType));
                                log.debug(String.format("Label of GCMD instrument %s has been change from %s to %s",
                                        gcmd.getUri(), altTitle.getText(), gcmd.getLabel()));
                            }
                        } else {
                            log.debug(String.format("Instrument %s does not exist in the GCMD instrument thesaurus", altTitle.getLink()));
                        }
                        instrument.setAltTitle(altTitle);
                    }
                }

                Concept instrConcept = config.getInstrument(instrument.getUri());
                if (instrConcept != null) {
                    MetadataUtils.findInstrumentType(instrument, instrConcept, config);
                    if (!instrConcept.getLabel().equals(instrument.getLabel())) {
                        // Instrument label changed
                        instrument.setWarning(new AutoCorrectionWarning(metadataId,
                                instrument.getUri(), instrument.getLabel(), instrConcept.getLabel(), 3,
                                config.getInstrumentThesaurus().getLabel(), 1, recordType));
                    }
                } else {
                    instrument.setEsaInstrument(false);
                    log.debug(String.format("Instrument %s does not exist in the ESA instrument thesaurus", instrument.getUri()));
                }
            } else {
                instrument.setEsaInstrument(false);
                log.debug(String.format("Instrument %s is not an ESA instrument", instrument.getUri()));
            }
        }

        instrument.setDescription(XmlUtils
                .buildStringProperty(instrumentNode, "./gmi:MI_Platform/gmi:description"));

        return instrument;
    }

    private Platform buildPlatform(String metadataId, int recordType, Node platformNode) {

        Platform platform = new Platform();
        platform.setSelf(platformNode);

        Node citationNode = XPathUtils
                .getNode(platformNode, "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation");
        if (citationNode != null) {
            GmxAnchor title = XmlUtils.buildStringProperty(citationNode, "./gmd:title");
            if (title != null) {
                platform.setUri(title.getLink());
                platform.setLabel(title.getText());
            }

            Node launchDateNode = XPathUtils.getNode(citationNode, "./gmd:date");
            if (launchDateNode != null) {
                platform.setLaunchDateNode(launchDateNode);

                String launchDate = XPathUtils.getNodeValue(launchDateNode, "./gmd:CI_Date/gmd:date/gco:Date");
                if (StringUtils.isNotEmpty(launchDate)) {
                    platform.setLaunchDate(CommonUtils.strToDate(launchDate));
                } else {
                    platform.setLaunchDate(CommonUtils.strToDate(config.getPlatformThesaurus().getModified()));
                }
            }

            GmxAnchor altTitle = XmlUtils.buildStringProperty(citationNode, "./gmd:alternateTitle");

            if (altTitle != null) {
                if (StringUtils.isNotEmpty(altTitle.getLink())) {
                    log.debug("Platform alt concept Uri: " + altTitle.getLink());
                    Concept gcmd = config.getGcmdPlatform(altTitle.getLink());
                    if (gcmd != null) {
                        platform.setGcmd(gcmd);
                        if (!gcmd.getLabel().equals(altTitle.getText())) {
                            // GCMD Platform label changed
                            platform.setGcmdWarning(new AutoCorrectionWarning(metadataId,
                                    altTitle.getLink(), altTitle.getText(), gcmd.getLabel(), 5,
                                    config.getGcmdPlatformThesaurus().getLabel(), 1, recordType));
                            log.debug(String.format("Label of GCMD platform %s has been change from %s to %s",
                                    gcmd.getUri(), altTitle.getText(), gcmd.getLabel()));
                        }
                    } else {
                        log.debug(String.format("Platform %s does not exist in the GCMD platform thesaurus", altTitle.getLink()));
                    }
                    platform.setAltTitle(altTitle);
                }
            }
        }

        GmxAnchor identifier = XmlUtils.buildStringProperty(platformNode,
                "./gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code");
        if (identifier != null) {
            platform.setIdentifier(identifier);
            if (platform.getUri() == null || platform.getUri().isEmpty()) {
                platform.setUri(identifier.getLink());
            }

            if (platform.getLabel() == null || platform.getLabel().isEmpty()) {
                platform.setLabel(identifier.getText());
            }
        }

        platform.setDescription(XmlUtils
                .buildStringProperty(platformNode, "./gmi:MI_Platform/gmi:description"));

        NodeList sponsors = XPathUtils.getNodes(platformNode, "./gmi:MI_Platform/gmi:sponsor");
        if (sponsors != null) {
            List<Sponsor> operators = new ArrayList<>();
            for (int i = 0; i < sponsors.getLength(); i++) {
                Node operNode = sponsors.item(i);
                String orgName = XPathUtils.getNodeValue(operNode, "./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
                Sponsor sps = new Sponsor();
                sps.addOperator(orgName);
                operators.add(sps);
            }
            platform.setOperators(operators);
        }

        NodeList instrumentNodes = XPathUtils
                .getNodes(platformNode, "./gmi:MI_Platform/gmi:instrument");
        if (instrumentNodes != null && instrumentNodes.getLength() > 0) {
            for (int i = 0; i < instrumentNodes.getLength(); i++) {
                Instrument inst = buildInstrument(metadataId, recordType, instrumentNodes.item(i));
                if (StringUtils.isNotEmpty(inst.getLabel())) {
                    platform.addInstrument(inst);
                }
            }
        }

        /*
            check if this is an ESA platform                
         */
        if (StringUtils.isNotEmpty(platform.getUri())
                && platform.getUri().startsWith(config.getPlatformThesaurus().getUriSpace())) {
            log.debug(String.format("Platform %s is an ESA platform", platform.getUri()));

            //String plfConceptKey = config.getPlatformThesaurusUri() + Constants.SEPARATOR + platform.getUri();
            log.debug("Platform concept key: " + platform.getUri());
            Concept plfConcept = config.getPlatform(platform.getUri());
            if (plfConcept != null) {
                Platform esaPlatform = MetadataUtils.createPlatform(plfConcept, config);
                //MetadataUtils.mergePlatform(platform, esaPlatform);
                if (esaPlatform.getAvailableInstruments() != null) {
                    platform.setAvailableInstruments(new ArrayList<>());
                    for (Instrument avInst : esaPlatform.getAvailableInstruments()) {
                        boolean adding = true;
                        if (platform.getInstruments() != null) {
                            for (Instrument inst : platform.getInstruments()) {
                                if (avInst.getUri().equals(inst.getUri())) {
                                    inst.setHosted(true);
                                    adding = false;
                                    break;
                                }
                            }
                        }
                        if (adding) {
                            platform.getAvailableInstruments().add(avInst);
                        }
                    }
                }
                if (!esaPlatform.getLabel().equals(platform.getLabel())) {
                    // Platform label changed
                    platform.setWarning(new AutoCorrectionWarning(metadataId,
                            platform.getUri(), platform.getLabel(), esaPlatform.getLabel(), 2,
                            config.getPlatformThesaurus().getLabel(), 1, recordType));
                }
            } else {
                platform.setEsaPlatform(false);
                log.debug(String.format("Platform %s does not exist in the ESA platform thesaurus", platform.getUri()));
            }
        } else {
            platform.setEsaPlatform(false);
            log.debug(String.format("Platform %s is not an ESA platform", platform.getUri()));
        }

        return platform;
    }

    private Constraints buildConstraints(Node constraintsNode, boolean serviceMetadata) {
        Constraints constraints = new Constraints();
        constraints.setSelf(constraintsNode);

        NodeList useLimits = XPathUtils
                .getNodes(constraintsNode, "./gmd:useLimitation");
        if (useLimits != null && useLimits.getLength() > 0) {
            if (constraints.getUseLimitations() == null) {
                constraints.setUseLimitations(new ArrayList<>());
            }
            for (int i = 0; i < useLimits.getLength(); i++) {
                GmxAnchor useLimit = XmlUtils.buildStringProperty(useLimits.item(i), ".");
                if (useLimit != null) {
                    extractLink(useLimit, serviceMetadata);
                    constraints.getUseLimitations().add(useLimit);
                }
            }
        }

        NodeList accesses = XPathUtils
                .getNodes(constraintsNode, "./gmd:accessConstraints/gmd:MD_RestrictionCode");
        if (accesses != null && accesses.getLength() > 0) {
            if (constraints.getAccesses() == null) {
                constraints.setAccesses(new ArrayList<>());
            }
            for (int i = 0; i < accesses.getLength(); i++) {
                GmxAnchor accessConstraint = new GmxAnchor();
                Node accessNode = accesses.item(i);
                accessConstraint.setSelf(accessNode);
                accessConstraint.setLink(XmlUtils.getNodeAttValue(accessNode, "codeList"));
                accessConstraint.setText(XmlUtils.getNodeAttValue(accessNode, "codeListValue"));
                constraints.getAccesses().add(accessConstraint);
            }
        }

        NodeList uses = XPathUtils
                .getNodes(constraintsNode, "./gmd:useConstraints/gmd:MD_RestrictionCode");
        if (uses != null && uses.getLength() > 0) {
            if (constraints.getUses() == null) {
                constraints.setUses(new ArrayList<>());
            }
            for (int i = 0; i < uses.getLength(); i++) {
                GmxAnchor usesConstraint = new GmxAnchor();
                Node useNode = uses.item(i);
                usesConstraint.setSelf(useNode);
                usesConstraint.setLink(XmlUtils.getNodeAttValue(useNode, "codeList"));
                usesConstraint.setText(XmlUtils.getNodeAttValue(useNode, "codeListValue"));
                constraints.getUses().add(usesConstraint);
            }
        }

        NodeList others = XPathUtils
                .getNodes(constraintsNode, "./gmd:otherConstraints");
        if (others != null && others.getLength() > 0) {
            if (constraints.getOthers() == null) {
                constraints.setOthers(new ArrayList<>());
            }
            for (int i = 0; i < others.getLength(); i++) {
                GmxAnchor other = XmlUtils.buildStringProperty(others.item(i), ".");
                if (other != null) {
                    extractLink(other, serviceMetadata);
                    constraints.getOthers().add(other);
                }
            }
        }

        return constraints;
    }

    private void extractLink(GmxAnchor anchor, boolean serviceMetadata) {
        if (serviceMetadata
                && StringUtils.isEmpty(anchor.getLink())
                && StringUtils.isNotEmpty(anchor.getText())) {
            int index = anchor.getText().lastIndexOf(" ");
            if (index > -1) {
                String lastWord = anchor.getText().substring(index + 1);
                if (StringUtils.isNotEmpty(lastWord)
                        && (StringUtils.startsWithIgnoreCase(lastWord, "http://")
                        || StringUtils.startsWithIgnoreCase(lastWord, "https://")
                        || StringUtils.startsWithIgnoreCase(lastWord, "ftp://")
                        || StringUtils.startsWithIgnoreCase(lastWord, "ftps://"))) {
                    String newText = anchor.getText().substring(0, index);
                    anchor.setText(newText);
                    anchor.setLink(lastWord);
                }
            }
        }
    }

    private List<Keyword> getKeywords(Node mdKeywordNode) {
        List<Keyword> keywords = null;

        NodeList kwNodes = XPathUtils.getNodes(mdKeywordNode, "./gmd:keyword");
        if (kwNodes != null && kwNodes.getLength() > 0) {
            keywords = new ArrayList<>();
            for (int i = 0; i < kwNodes.getLength(); i++) {
                Node n = kwNodes.item(i);

                Node anchorNode = XPathUtils.getNode(n, "./gmx:Anchor");
                if (anchorNode != null) {
                    Keyword kw = new Keyword();
                    kw.setUri(XmlUtils.getNodeAttValue(anchorNode, Constants.XLINK_NS, "href"));
                    kw.setLabel(XmlUtils.getNodeValue(anchorNode));
                    keywords.add(kw);
                } else {
                    Node descNode = XPathUtils.getNode(n, "./gco:CharacterString");
                    if (descNode != null) {
                        Keyword kw = new Keyword();
                        kw.setLabel(XmlUtils.getNodeValue(descNode));
                        keywords.add(kw);
                    }
                }
            }
        }
        return keywords;
    }

    private void cleanXmlComments(Document xmlDoc) {
        log.debug("Clean XML comments");
        NodeList commentNodes = XPathUtils.getNodes(xmlDoc, "//comment()");
        if (commentNodes != null) {
            for (int i = 0; i < commentNodes.getLength(); i++) {
                Node cNode = commentNodes.item(i);
                if (cNode.getParentNode() != null) {
                    cNode.getParentNode().removeChild(cNode);
                } else {
                    log.debug("Could not remove the comment because its parent node is null");
                }
            }
        }
    }

    private Node getDistributionNodeRef(Document metadataDoc) {

        NodeList nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:dataQualityInfo");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:portrayalCatalogueInfo");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:metadataConstraints");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:applicationSchemaInfo");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        Node node = XPathUtils.getNode(metadataDoc, "./*/gmd:metadataMaintenance");
        if (node != null) {
            return node;
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:series");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:describes");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:propertyType");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:featureType");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmd:featureAttribute");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }

        nodeList = XPathUtils.getNodes(metadataDoc, "./*/gmi:acquisitionInformation");
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0);
        }
        return null;
    }

    public XMLParser getXmlParser() {
        return xmlParser;
    }

    public Dif10Handler getDif10Handler() {
        return dif10Handler;
    }

    public void setXmlSrc(MetadataFile metadataFile) {
        boolean updated = false;

        Metadata metadata = metadataFile.getMetadata();
        if (metadata != null
                && (metadata.isService() || metadata.hasOfferingOperation())) {

            String newDocStr = xmlParser.serializeDOM(metadataFile.getXmlDoc());
            Document newDoc = xmlParser.stream2Document(newDocStr);

//            if (metadataFile.getMetadata().isService()) {
//                if (metadata.getIdentification() != null) {
//                    Node dataIdNode = XPathUtils.getNode(newDoc,
//                            "./*/gmd:identificationInfo/srv:SV_ServiceIdentification");
//                    if (dataIdNode != null) {
//                        // Find all gmd:keyword/gmx:Anchor elements
//                        NodeList nodes = XPathUtils.getNodes(dataIdNode,
//                                "./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor");
//
//                        if (nodes != null && nodes.getLength() > 0) {
//                            replaceGmxByGco(nodes);
//                            updated = true;
//                        }
//
//                        // Find all gmd:title/gmx:Anchor elements
//                        nodes = XPathUtils.getNodes(dataIdNode,
//                                "./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor");
//                        if (nodes != null && nodes.getLength() > 0) {
//                            replaceGmxByGco(nodes);
//                            updated = true;
//                        }
//                    }
//                }
//            }
            // add offerings to distribution
            if (metadata.hasOfferingOperation()) {
                offeringsToDistribution(newDoc, metadata);
                updated = true;
            }

            if (updated) {
                metadataFile.setXmlSrc(xmlParser.format(newDoc));
            }
        }
        if (!updated) {
            metadataFile.setXmlSrc(xmlParser.format(metadataFile.getXmlDoc()));
        }
    }

    private void replaceGmxByGco(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String nodeText = XmlUtils.getNodeValue(node);

            Node parent = node.getParentNode();

            // remove gmx:Anchor node
            parent.removeChild(node);

            if (StringUtils.isNotEmpty(nodeText)) {

                // add gco:CharacterString node
                StringBuilder sb = new StringBuilder();
                sb.append("<gco:CharacterString xmlns:gco=\"")
                        .append(Constants.GCO_NS)
                        .append("\">");
                sb.append(nodeText);
                sb.append("</gco:CharacterString>");

                Node importedNode = parent.getOwnerDocument()
                        .importNode(XmlUtils.buildNode(sb.toString()), true);
                XmlUtils.cleanNamespaces(importedNode);
                parent.appendChild(importedNode);
            }
        }
    }

    public Document correctGcmdConceptUri(Document isoDoc) {
        String newIsoDoc = xmlParser.serializeDOM(isoDoc);
        if (config.getOldGcmdConceptSchemeUris() != null) {
            for (String uri : config.getOldGcmdConceptSchemeUris()) {
                newIsoDoc = newIsoDoc.replaceAll(uri, config.getGcmdConceptSchemeUri());
            }
        }
        if (config.getOldGcmdConceptUris() != null) {
            for (String uri : config.getOldGcmdConceptUris()) {
                newIsoDoc = newIsoDoc.replaceAll(uri, config.getGcmdConceptUri());
            }
        }
        return xmlParser.stream2Document(newIsoDoc);
    }

    private boolean hasOnlineResource(Offering offering) {

        if (offering.getOperations() != null
                && offering.getOperations().size() > 0) {
            for (OfferingOperation operation : offering.getOperations()) {
                if (StringUtils.isNotEmpty(operation.getUrl())) {
                    return true;
                }
            }
        }
        return false;
    }
}
