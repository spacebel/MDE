/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.configuration.Offering;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.models.workspace.distribution.OnlineResource;
import be.spacebel.metadataeditor.models.workspace.distribution.TransferOption;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.utils.parser.MetadataUtils;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.parser.XPathUtils;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.extensions.model.fluidgrid.FluidGridItem;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents the Internal Metadata Model
 *
 * @author mng
 */
public class Metadata implements Serializable {

    private final Logger log = Logger.getLogger(getClass());
    private final Configuration config;

    private enum TYPE {
        SERIES, SERVICE
    }

    private TYPE metadataType;

    private Others others;
    private Identification identification;
    private Distribution distribution;
    private Acquisition acquisition;
    private Node self;

    private Node dataQualityInfo;
    private Node portrayalCatalogueInfo;
    private Node metadataConstraints;
    private Node applicationSchemaInfo;
    private Node metadataMaintenance;
    private Node series;
    private Node describes;
    private Node propertyType;
    private Node featureType;
    private Node featureAttribute;
    private Node acquisitionNode;

    private final List<SelectItem> progressCodes;

    private List<ContentInfo> processingLevels;
    private String selectedProcessingLevel;
    private List<SelectItem> availableProcessingLevels;
    private List<FluidGridItem> fluidProcessingLevels;

    private List<Offering> offerings;
    private String selectedOffering;
    private List<SelectItem> availableOfferings;
    private String richTextAbstract;

    private String capabilitiesServiceUrl;

    private boolean earthtopicChanged;
    private boolean scienceKwChanged;
    private boolean esaInstrumentChanged;

    public Metadata(Configuration config) {
        this.config = config;

        this.acquisition = new Acquisition();
        //createEmptyOnlineRS();

        progressCodes = new ArrayList<>();
        if (config.getProgressCodes() != null) {
            config.getProgressCodes().forEach((code) -> {
                progressCodes.add(new SelectItem(code, code));
            });
        }

        availableProcessingLevels = new ArrayList<>();
        if (config.getProcessingLevels() != null) {
            for (String iso : config.getProcessingLevels()) {
                availableProcessingLevels.add(new SelectItem(iso, iso));
            }
        }

        availableOfferings = new ArrayList<>();
        if (config.getOfferings() != null) {
            config.getOfferings().entrySet().forEach((entry) -> {
                availableOfferings.add(new SelectItem(entry.getKey(), entry.getKey()));
            });
        }

        fluidProcessingLevels = new ArrayList<>();
    }

    public Others getOthers() {
        return others;
    }

    public void setOthers(Others others) {
        this.others = others;
    }

    public Identification getIdentification() {
        return identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public Acquisition getAcquisition() {
        return acquisition;
    }

    public void setAcquisition(Acquisition acquisition) {
        this.acquisition = acquisition;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public Node getDataQualityInfo() {
        return dataQualityInfo;
    }

    public void setDataQualityInfo(Node dataQualityInfo) {
        this.dataQualityInfo = dataQualityInfo;
    }

    public Node getPortrayalCatalogueInfo() {
        return portrayalCatalogueInfo;
    }

    public void setPortrayalCatalogueInfo(Node portrayalCatalogueInfo) {
        this.portrayalCatalogueInfo = portrayalCatalogueInfo;
    }

    public Node getMetadataConstraints() {
        return metadataConstraints;
    }

    public void setMetadataConstraints(Node metadataConstraints) {
        this.metadataConstraints = metadataConstraints;
    }

    public Node getApplicationSchemaInfo() {
        return applicationSchemaInfo;
    }

    public void setApplicationSchemaInfo(Node applicationSchemaInfo) {
        this.applicationSchemaInfo = applicationSchemaInfo;
    }

    public Node getMetadataMaintenance() {
        return metadataMaintenance;
    }

    public void setMetadataMaintenance(Node metadataMaintenance) {
        this.metadataMaintenance = metadataMaintenance;
    }

    public Node getSeries() {
        return series;
    }

    public void setSeries(Node series) {
        this.series = series;
    }

    public Node getDescribes() {
        return describes;
    }

    public void setDescribes(Node describes) {
        this.describes = describes;
    }

    public Node getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Node propertyType) {
        this.propertyType = propertyType;
    }

    public Node getFeatureType() {
        return featureType;
    }

    public void setFeatureType(Node featureType) {
        this.featureType = featureType;
    }

    public Node getFeatureAttribute() {
        return featureAttribute;
    }

    public void setFeatureAttribute(Node featureAttribute) {
        this.featureAttribute = featureAttribute;
    }

    public Node getAcquisitionNode() {
        return acquisitionNode;
    }

    public void setAcquisitionNode(Node acquisitionNode) {
        this.acquisitionNode = acquisitionNode;
    }

    public List<SelectItem> getProgressCodes() {
        return progressCodes;
    }

    public void addOnlineRs() {
        log.debug("Add online resource");
        createEmptyOnlineRS();
    }

    private void createEmptyOnlineRS() {
        TransferOption option = getTransferOption();

        if (option.getOnlineRses() == null) {
            option.setOnlineRses(new ArrayList<>());
        }

        OnlineResource onlineRs = new OnlineResource();
        option.getOnlineRses().add(onlineRs);
    }

    public void removeOnlineRs(OnlineResource onlineRs) {
        log.debug("Remove online resource");
        TransferOption option = getTransferOption();
        if (option.getOnlineRses() != null && !option.getOnlineRses().isEmpty()) {
            log.debug("Remove OK");
            option.getOnlineRses().remove(onlineRs);
        } else {
            log.debug("Remove NOK");
        }
    }

    private TransferOption getTransferOption() {
        if (distribution == null) {
            distribution = new Distribution();
        }

        if (distribution.getTransferOptions() == null) {
            distribution.setTransferOptions(new ArrayList<>());
        }

        TransferOption option;
        if (distribution.getTransferOptions().isEmpty()) {
            option = new TransferOption();
            distribution.getTransferOptions().add(option);
        } else {
            option = distribution.getTransferOptions().get(0);
        }
        return option;
    }

    public boolean hasDistribution() {
        if (distribution != null
                && distribution.getTransferOptions() != null
                && distribution.getTransferOptions().size() > 0) {
            for (TransferOption tfo : distribution.getTransferOptions()) {
                if (StringUtils.isNotEmpty(tfo.getSize())
                        || StringUtils.isNotEmpty(tfo.getUnits())) {
                    return true;
                }
                if (tfo.getOnlineRses() != null
                        && tfo.getOnlineRses().size() > 0) {
                    if (tfo.getOnlineRses().stream().anyMatch((olRs) -> (StringUtils.isNotEmpty(olRs.getLinkage())
                            || StringUtils.isNotEmpty(olRs.getProtocol())
                            || StringUtils.isNotEmpty(olRs.getFunction())))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    public void onSelectPlatform(SelectEvent event) throws ParseException {
//        log.debug("On select a platform: " + event.getObject());
//        Object obj = event.getObject();
//        if (obj instanceof ParameterOption) {
//            log.debug("This is a ParameterOption");
//            ParameterOption option = (ParameterOption) obj;
//            String newUri = option.getValue();
//            if (newUri.contains(Constants.SEPARATOR)) {
//                newUri = StringUtils.substringBefore(newUri, Constants.SEPARATOR);
//            }
//            log.debug("Uri = " + newUri);
//            log.debug("Text = " + option.getLabel());
//            if (config.getPlatforms().containsKey(newUri)) {
//                addPlatform(config.getPlatforms().get(newUri));
//            } else {
//                log.debug("No platform found");
//            }
//        }
//    }
    public void onSelectPlatform(NodeSelectEvent event) throws ParseException {
        log.debug("On select a platform");
        Concept selectedConcept = (Concept) event.getTreeNode().getData();
        log.debug("Uri = " + selectedConcept.getUri());
        addPlatform(selectedConcept);
    }

//    public void onSelectEarthTopic(SelectEvent event) {
//        log.debug("On select an Earth Topic keyword: " + event.getObject());
//        Object obj = event.getObject();
//        if (obj instanceof ParameterOption) {
//            log.debug("This is a ParameterOption");
//            ParameterOption option = (ParameterOption) obj;
//            String newUri = option.getValue();
//            if (newUri.contains(Constants.SEPARATOR)) {
//                newUri = StringUtils.substringBefore(newUri, Constants.SEPARATOR);
//            }
//            log.debug("Uri = " + newUri);
//            log.debug("Text = " + option.getLabel());
//            if (config.getEarthTopics().containsKey(newUri)) {
//                identification.addEarthTopic(config.getEarthTopics().get(newUri));
//            } else {
//                log.debug("No Earth Topics keyword found");
//            }
//        }
//    }
    public void onSelectEarthTopic(NodeSelectEvent event) {
        log.debug("On select an Earth Topic");
        Concept selectedConcept = (Concept) event.getTreeNode().getData();
        log.debug("Uri = " + selectedConcept.getUri());
        identification.addEarthTopic(selectedConcept);
    }

    public void removeInstrument(Platform sPlatform, Instrument sInstrument) {
        if (sPlatform.getInstruments().size() > 1) {
            sPlatform.getInstruments().remove(sInstrument);
        } else {
            //FacesMessageUtil.addInfoMessage("The platform should have at least one instrument");
        }
    }

    private void addPlatform(Concept concept) throws ParseException {
        if (acquisition == null) {
            acquisition = new Acquisition();
        }
        Platform tempPlatform = new Platform();
        tempPlatform.setUri(concept.getUri());
        if (acquisition.exists(tempPlatform)) {
            FacesMessageUtil.addInfoMessage(String.format("The platform {%s} does already exist", concept.getLabel()));
        } else {
            acquisition.addPlatform(MetadataUtils.createPlatform(concept, config));
        }
    }

    public void removePlatform(Platform sPlatform) {
        acquisition.getPlatforms().remove(sPlatform);
    }

    public void removeEarthTopic(EarthTopic earthTopic) {
        identification.getEarthTopics().remove(earthTopic);
    }

    public void updateOrInsertDistribution() {
        log.debug("update or insert Distribution");
        if (distribution != null) {
            log.debug("distribution is not  null");
            Node distributionNode = XPathUtils.getNode(self,
                    "./gmd:distributionInfo/gmd:MD_Distribution");
            distribution.setSelf(distributionNode);

            if (distribution.getSelf() != null) {
                log.debug("have Distribution node ");
                /*
                 case update existing Distribution
                 */
                if (distribution.getTransferOptions() != null) {
                    log.debug("Number of transferOptions: " + distribution.getTransferOptions().size());

                    for (TransferOption transferOption : distribution.getTransferOptions()) {
                        List<Node> onlineRsNodes = new ArrayList<>();
                        if (notEmptyTransferOption(transferOption)) {
                            log.debug("not empty TransferOption");
                            if (transferOption.getOnlineRses() != null) {
                                transferOption.getOnlineRses().forEach((onlineRs) -> {
                                    onlineRsNodes.add(XmlUtils.createOnlineResourceNode(onlineRs));
                                });
                            }
                        }

                        if (transferOption.getSelf() != null) {
                            log.debug("Have transferOption node ");

                            // remove all existing gmd:onLine                            
                            NodeList existingOnlineRsNodes = XPathUtils
                                    .getNodes(transferOption.getSelf(), "./gmd:onLine");
                            if (existingOnlineRsNodes != null && existingOnlineRsNodes.getLength() > 0) {
                                for (int i = 0; i < existingOnlineRsNodes.getLength(); i++) {
                                    Node node = existingOnlineRsNodes.item(i);
                                    node.getParentNode().removeChild(node);
                                }
                            }

                            // add gmd:onLine nodes
                            for (Node node : onlineRsNodes) {
                                Node importedNode = transferOption.getSelf().getOwnerDocument()
                                        .importNode(node, true);
                                XmlUtils.cleanNamespaces(importedNode);
                                transferOption.getSelf().appendChild(importedNode);
                            }
                        } else {
                            log.debug("Have no transferOption node ");
                            Node node = XmlUtils.createTransferOptions(transferOption);
                            if (node != null) {
                                Node importedNode = distribution.getSelf().getOwnerDocument().importNode(node, true);
                                distribution.getSelf().appendChild(importedNode);

                                Node transferNode = XPathUtils.getNode(importedNode, "./gmd:MD_DigitalTransferOptions");
                                if (transferNode == null) {
                                    log.debug("MNG Have no transfer options");
                                } else {
                                    log.debug("MNG Have transfer options");
                                }
                                transferOption.setSelf(transferNode);
                            }
                        }
                    }
                } else {
                    log.debug("removing all online resources");
                    XPathUtils.removeNodes(distribution.getSelf(), "./gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine");
                    log.debug("removed all online resources");
                }

            } else {
                log.debug("have no Distribution node ");
                /*
                 case create new Distribution
                 */

                Node newDistribution = XmlUtils.createDistributionInfo(distribution);
                if (newDistribution != null) {
                    Node importedNode = self.getOwnerDocument().importNode(newDistribution, true);
                    XmlUtils.cleanNamespaces(importedNode);

                    Node nodeRef = XmlUtils.getDistributionNodeRef(self, isSeries());
                    if (nodeRef != null) {
                        self.insertBefore(importedNode, nodeRef);
                    } else {
                        self.appendChild(importedNode);
                    }

                    Node mdDistNode = XPathUtils.getNode(importedNode, "./gmd:MD_Distribution");
                    if (mdDistNode != null) {
                        setDistribution(XmlUtils
                                .buildDistribution(mdDistNode, hasOfferingOperation(), config));
                    }
                    //distribution.setSelf(importedNode);
                }

            }
        } else {
            log.debug("distribution is null");
        }

    }

    private boolean notEmptyTransferOption(TransferOption option) {
        if (option.getOnlineRses() != null
                && option.getOnlineRses().size() > 0) {
            return true;
        }

        if (StringUtils.isNotEmpty(option.getSize())) {
            return true;
        }

        return StringUtils.isNotEmpty(option.getUnits());
    }

    public void updateOrInsertAcquisition() throws XPathExpressionException {
        if (acquisition != null) {
            if (acquisition.getSelf() != null) {
                acquisition.update();
            } else {
                /*
                 case insert
                 */
                if ((acquisition.getPlatforms() != null
                        && acquisition.getPlatforms().size() > 0)) {

                    Node newAcquisition = self.getOwnerDocument()
                            .importNode(XmlUtils.createAcquisitionNode(acquisition), true);
                    if (newAcquisition != null) {
                        XmlUtils.cleanNamespaces(newAcquisition);
                        self.appendChild(newAcquisition);
                        acquisition.setSelf(XPathUtils.getNode(newAcquisition, "./gmi:MI_AcquisitionInformation"));
                    }
                }
            }
        }
    }

    public void update() throws XPathExpressionException {
        if (others != null) {
            others.update();
        }

        if (identification != null) {
            identification.update(acquisition);

//            if (isSeries()) {
//                /**
//                 * update offerings
//                 */
//                String offeringsJson = "";
//                JSONObject jsonObj = new JSONObject();
//                JSONArray array = getOfferingsArray();
//                boolean hasInfo = false;
//                if (array != null) {
//                    jsonObj.put("offerings", array);
//                    hasInfo = true;
//                }
//
//                if (StringUtils.isNotEmpty(identification.getRichTextAbstract())) {
//                    JSONObject absObj = new JSONObject();
//                    JSONArray absArray = new JSONArray();
//                    absArray.put(identification.getRichTextAbstract());
//                    absObj.put("text/markdown", absArray);
//                    jsonObj.put("abstract", absObj);
//                    hasInfo = true;
//                }
//
//                if (hasInfo) {
//                    offeringsJson = jsonObj.toString(2);
//                }
//                identification.updateSupplementalInfo(offeringsJson);
//            }
        }

        List<ContentInfo> removingContentInfos = new ArrayList<>();
        // update contentInfo
        if (processingLevels != null && processingLevels.size() > 0) {
            processingLevels.forEach((contentInfo) -> {
                if (!contentInfo.isRemoved() && contentInfo.getProcessingLevel() != null
                        && StringUtils.isNotEmpty(contentInfo.getProcessingLevel().getLabel())) {
                    if (contentInfo.getSelf() != null) {
                        //System.out.println("Update processingLevelCode");
                        // update processingLevelCode
                        XPathUtils.updateNodeValue(contentInfo.getSelf(),
                                "./gmd:processingLevelCode/gmd:RS_Identifier/gmd:code/gco:CharacterString",
                                contentInfo.getProcessingLevel().getLabel());
                    } else {
                        //System.out.println("Add a new contentInfo element");
                        // create a new contentInfo element
                        Node importedNode = self.getOwnerDocument()
                                .importNode(XmlUtils.createContentInfoNode(contentInfo, isService()), true);
                        XmlUtils.cleanNamespaces(importedNode);
                        Node nodeRef = XmlUtils.getContentInfoNodeRef(self, isSeries());
                        self.insertBefore(importedNode, nodeRef);
                        contentInfo.setSelf(importedNode);
                    }
                } else {
                    if (contentInfo.isRemoved()) {
                        removingContentInfos.add(contentInfo);
                    }
                }
            });
        }

        if (removingContentInfos.size() > 0) {
            removingContentInfos.stream().map((contentInfo) -> {
                if (contentInfo.getSelf() != null) {
                    //System.out.println("Remove contentInfo");
                    // remove contentInfo
                    contentInfo.getSelf().getParentNode().removeChild(contentInfo.getSelf());
                }
                return contentInfo;
            }).forEachOrdered((contentInfo) -> {
                processingLevels.remove(contentInfo);
            });
        }

        //offeringsToOnlineResources();
        updateOrInsertDistribution();

        if (isSeries()) {
            updateOrInsertAcquisition();
        }
    }

//    public void offeringsToOnlineResources() {
//        // Add the dedicated online resources of offerings
//        if (hasOfferingOperation()) {
//            TransferOption option = getTransferOption();
//
//            if (option.getOnlineRses() == null) {
//                option.setOnlineRses(new ArrayList<>());
//            }
//
//            // reset the offering resources
//            option.setOfferingResources(new ArrayList<>());
//
//            offerings.stream().filter((offering) -> (offering.getOperations() != null)).forEachOrdered((offering) -> {
//                offering.getOperations().stream().map((operation) -> {
//                    OnlineResource onlineRs = new OnlineResource();
//                    onlineRs.setLinkage(operation.getUrl());
//                    onlineRs.setProtocol(operation.getProtocol());
//                    onlineRs.setFunction(operation.getFunction());
//                    onlineRs.setName(operation.getCode());
//                    return onlineRs;
//                }).forEachOrdered((onlineRs) -> {
//                    option.getOfferingResources().add(onlineRs);
//                });
//            });
//        }
//    }   
    public void addProcessingLevel(final AjaxBehaviorEvent event) {
        addProcessingLevel(selectedProcessingLevel, null);
    }

    public void addProcessingLevel(String level, Node contentInfoNode) {
        //System.out.println("NQMINHHHHH:" + level);
        if (StringUtils.isNotEmpty(level)) {
            if (processingLevels == null) {
                processingLevels = new ArrayList<>();
            }

            ContentInfo contentInfo = new ContentInfo();
            contentInfo.addProcessingLevel(level);
            contentInfo.setSelf(contentInfoNode);

            processingLevels.add(contentInfo);

            fluidProcessingLevels.add(new FluidGridItem(contentInfo));

            SelectItem removingItem = null;
            for (SelectItem item : availableProcessingLevels) {
                //System.out.println(item.getLabel() + "=" + level);
                if (item.getLabel().equalsIgnoreCase(level)) {
                    removingItem = item;
                    break;
                }
            }
            if (removingItem != null) {
                //System.out.println("NQMINHHHHH: found" + level);
                availableProcessingLevels.remove(removingItem);
            } else {
                //System.out.println("NQMINHHHHH: not found" + level);
            }
        }
    }

    public void removeProcessingLevel(ContentInfo contentInfo) {
        String level = contentInfo.getProcessingLevel().getLabel();
        //this.processingLevels.remove(contentInfo);

        FluidGridItem existingContentInfo = null;
        for (FluidGridItem fluidKw : fluidProcessingLevels) {
            if (contentInfo.equals(fluidKw.getData())) {
                existingContentInfo = fluidKw;
                break;
            }
        }
        if (existingContentInfo != null) {
            fluidProcessingLevels.remove(existingContentInfo);
        }
        availableProcessingLevels.add(new SelectItem(level, level));
        contentInfo.setRemoved(true);
    }

    public List<ContentInfo> getProcessingLevels() {
        return processingLevels;
    }

    public void setProcessingLevels(List<ContentInfo> processingLevels) {
        this.processingLevels = processingLevels;
    }

    public List<FluidGridItem> getFluidProcessingLevels() {
        return fluidProcessingLevels;
    }

    public void setFluidProcessingLevels(List<FluidGridItem> fluidProcessingLevels) {
        this.fluidProcessingLevels = fluidProcessingLevels;
    }

    public String getSelectedProcessingLevel() {
        return selectedProcessingLevel;
    }

    public void setSelectedProcessingLevel(String selectedProcessingLevel) {
        this.selectedProcessingLevel = selectedProcessingLevel;
    }

    public List<SelectItem> getAvailableProcessingLevels() {
        return availableProcessingLevels;
    }

    public void setAvailableProcessingLevels(List<SelectItem> availableProcessingLevels) {
        this.availableProcessingLevels = availableProcessingLevels;
    }

    public void addOffering(final AjaxBehaviorEvent event) {
        //System.out.println("selectedOffering = " + selectedOffering);
        if (StringUtils.isNotEmpty(selectedOffering)) {
            addOffering(config.getOffering(selectedOffering));
        }
    }

    public void addOffering(Offering offering) {
        if (offerings == null) {
            offerings = new ArrayList<>();
        }

        offerings.add(offering);

        if (offering.hasOneContent()) {
            offering.addFirstContent();
        }
    }

    public void removeOffering(Offering offering) {
        log.debug("Remove offering");
        if (offerings != null && offerings.size() > 0) {
            offerings.remove(offering);
            log.debug("Removed");
        } else {
            log.debug("No offering");
        }
    }

    public boolean hasOfferingOperation() {
        if (offerings != null && offerings.size() > 0) {
            if (offerings.stream().anyMatch((offering) -> (offering.getOperations() != null
                    && offering.getOperations().size() > 0))) {
                return true;
            }
        }
        return false;
    }

    public List<Offering> getOfferings() {
        return offerings;
    }

    public void setOfferings(List<Offering> offerings) {
        this.offerings = offerings;
    }

    public String getSelectedOffering() {
        return selectedOffering;
    }

    public void setSelectedOffering(String selectedOffering) {
        this.selectedOffering = selectedOffering;
    }

    public List<SelectItem> getAvailableOfferings() {
        return availableOfferings;
    }

    public void setAvailableOfferings(List<SelectItem> availableOfferings) {
        this.availableOfferings = availableOfferings;
    }

    public JSONArray getOfferingsArray() {
        if (offerings != null && offerings.size() > 0) {
            JSONArray offeringsArray = new JSONArray();

            offerings.forEach((offer) -> {
                offeringsArray.put(offer.toJsonObject());
            });

            return offeringsArray;
        }
        return null;
    }

    public String getRichTextAbstract() {
        return richTextAbstract;
    }

    public void setRichTextAbstract(String richTextAbstract) {
        this.richTextAbstract = richTextAbstract;
    }

    public String getCapabilitiesServiceUrl() {
        return capabilitiesServiceUrl;
    }

    public void setCapabilitiesServiceUrl(String capabilitiesServiceUrl) {
        this.capabilitiesServiceUrl = capabilitiesServiceUrl;
    }

    public boolean isEarthtopicChanged() {
        return earthtopicChanged;
    }

    public void setEarthtopicChanged(boolean earthtopicChanged) {
        this.earthtopicChanged = earthtopicChanged;
    }

    public boolean isScienceKwChanged() {
        return scienceKwChanged;
    }

    public void setScienceKwChanged(boolean scienceKwChanged) {
        this.scienceKwChanged = scienceKwChanged;
    }

    public boolean isEsaInstrumentChanged() {
        return esaInstrumentChanged;
    }

    public void setEsaInstrumentChanged(boolean esaInstrumentChanged) {
        this.esaInstrumentChanged = esaInstrumentChanged;
    }
//
//    public boolean isHasEarthtopicChangeWarn() {
//        return hasEarthtopicChangeWarn;
//    }
//
//    public boolean isHasScienceKwChangeWarn() {
//        return hasScienceKwChangeWarn;
//    }
//
//    public boolean isHasEsaInstrumentChangeWarn() {
//        return hasEsaInstrumentChangeWarn;
//    }
//
//    public void setHasEarthtopicChangeWarn(boolean hasEarthtopicChangeWarn) {
//        this.hasEarthtopicChangeWarn = hasEarthtopicChangeWarn;
//    }
//
//    public void setHasScienceKwChangeWarn(boolean hasScienceKwChangeWarn) {
//        this.hasScienceKwChangeWarn = hasScienceKwChangeWarn;
//    }
//
//    public void setHasEsaInstrumentChangeWarn(boolean hasEsaInstrumentChangeWarn) {
//        this.hasEsaInstrumentChangeWarn = hasEsaInstrumentChangeWarn;
//    }
//
//    public boolean isHasThesaurusChangeWarning() {
//        return (earthtopicChanged || scienceKwChanged || esaInstrumentChanged
//                || hasEarthtopicChangeWarn
//                || hasScienceKwChangeWarn
//                || hasEsaInstrumentChangeWarn);
//    }

    public void toSeries() {
        this.metadataType = TYPE.SERIES;
    }

    public boolean isSeries() {
        return (metadataType == TYPE.SERIES);
    }

    public void toService() {
        this.metadataType = TYPE.SERVICE;
    }

    public boolean isService() {
        return (metadataType == TYPE.SERVICE);
    }
}
