/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.models.workspace.identification.GmxAnchor;
import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.configuration.Organisation;
import be.spacebel.metadataeditor.models.configuration.Thesaurus;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.identification.Bbox;
import be.spacebel.metadataeditor.models.workspace.identification.Constraints;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.models.workspace.identification.ThesaurusKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.FreeKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.ServiceOperation;
import be.spacebel.metadataeditor.models.workspace.identification.ServiceType;
import be.spacebel.metadataeditor.models.workspace.identification.Temporal;
import be.spacebel.metadataeditor.utils.parser.MetadataUtils;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.parser.XPathUtils;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import be.spacebel.metadataeditor.utils.CommonUtils;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of gmd:identificationInfo element of ISO 19139-2 XML
 * metadata
 *
 * @author mng
 */
public class Identification implements Serializable {

    private final Logger log = Logger.getLogger(getClass());

    private String title;
    private Node titleNode;
    private String altTitle;
    private Node altTitleNode;
    private String plainTextAbstract;
    private Node plainTextAbstractNode;

    private Date creationDate;
    private Node creationDateNode;
    private Date revisionDate;
    private Node revisionDateNode;
    private Date publicationDate;
    private Node publicationDateNode;

    private String doi;
    //private Node doiNode;

    private String edition;
    //private Node editionNode;

    private String purpose;

    private String statusList;
    private String statusListValue;

    private Bbox bbox;
    private Temporal temporal;
    private List<Constraints> constraints;
    private Node dataId;
    private Node ciCitation;

    private String otherCitationDetails;
    //private Node otherCitationDetailsNode;

    private Node purposeNode;
    private Node statusNode;

    private Node resourceSpecificUsageNode;
    //private Node resourceConstraintsNode;
    private Node aggregationInfoNode;
    private Node spatialRepresentationTypeNode;
    private Node spatialResolutionNode;

    private Node language;
    private Node supplementalNodeForRef;
    //private CharacterData jsonSupplementalNode;

    private List<EarthTopic> earthTopics;
    //private List<EopKeyword> eopKeywords;    
    //private List<FreeKeyword> freeKeywords;
    private FreeKeyword freeKeyword;
    private FreeKeyword placeKeyword;
    private FreeKeyword spatialDataServiceCategoryKeywords;

    private ThesaurusKeyword orbitType;
    private ThesaurusKeyword waveLength;
    private ThesaurusKeyword processorVersion;
    private ThesaurusKeyword resolution;
    private ThesaurusKeyword productType;

    private ThesaurusKeyword orbitHeight;
    private ThesaurusKeyword swathWidth;

    //private List<ThesaurusKeyword> thesaurusKeywords;
    //private final Map<String, SelectItem> eopKeywordMap;
    //private List<String> corrections;
    private String topicCategory;
    private Node topicCategoryNode;
    private final List<SelectItem> availableTopicCategories;
    private final List<SelectItem> onlineRSRelatedFields;
    private final List<SelectItem> onlineRSFunctions;
    private final List<SelectItem> onlineRsProtocols;
    private final List<SelectItem> onlineRsAppProfiles;
    private final List<SelectItem> useLimitations;
    private final List<SelectItem> organisations;

    private List<Contact> pointOfContacts;

    private ServiceType serviceType;
    private List<String> serviceTypeVersion;

    private List<ServiceOperation> serviceOperations;

    private final Configuration config;
    private final int type;

    /*
        Store the GCMD Science Keyword that have no equivalent Earth Topics
     */
    private Map<String, String> noMappingScienceKeywords;
    private Map<String, String> instrumentTypeKeywords;

    public Identification(Configuration config, int mType) {
        this.config = config;
        orbitType = createEmptyEopKeyword(Constants.ORBITTYPE_KEY, false);
        waveLength = createEmptyEopKeyword(Constants.WAVELENGTH_KEY, false);
        processorVersion = createEmptyEopKeyword(Constants.PROCESSORVER_KEY, false);
        resolution = createEmptyEopKeyword(Constants.RESOLUTION_KEY, false);
        productType = createEmptyEopKeyword(Constants.PRODUCTTYPE_KEY, false);

        orbitHeight = createEmptyEopKeyword(Constants.ORBITHEIGHT_KEY, true);
        swathWidth = createEmptyEopKeyword(Constants.SWATHWIDTH_KEY, true);

//        freeKeyword = createEmptyFreeKeyword();
//        placeKeyword = createEmptyFreeKeyword();
//        placeKeyword.setCodeListValue("place");
        spatialDataServiceCategoryKeywords = new FreeKeyword();

        availableTopicCategories = new ArrayList<>();
        if (config.getIsoTopicCategories() != null) {
            config.getIsoTopicCategories().forEach((iso) -> {
                availableTopicCategories.add(new SelectItem(iso, iso));
            });
        }

        onlineRSRelatedFields = new ArrayList<>();
        if (config.getOnlineRSRelatedFields() != null) {
            config.getOnlineRSRelatedFields().forEach((field) -> {
                onlineRSRelatedFields.add(new SelectItem(field, field));
            });
        }

        onlineRSFunctions = new ArrayList<>();
        if (config.getOnlineRsFunctions() != null) {
            config.getOnlineRsFunctions().forEach((func) -> {
                onlineRSFunctions.add(new SelectItem(func, func));
            });
        }

        onlineRsProtocols = new ArrayList<>();
        if (config.getOnlineRsProtocols() != null) {
            config.getOnlineRsProtocols().forEach((protocol) -> {
                onlineRsProtocols.add(new SelectItem(protocol, protocol));
            });
        }

        onlineRsAppProfiles = new ArrayList<>();
        if (config.getOnlineRsAppProfiles() != null) {
            config.getOnlineRsAppProfiles().forEach((profile) -> {
                onlineRsAppProfiles.add(new SelectItem(profile, profile));
            });
        }

        // create required UseLimitation
        Constraints mConst = getConstraint();

        useLimitations = new ArrayList<>();
        if (config.getUseLimitations() != null) {
            config.getUseLimitations().forEach((useLimit) -> {
                useLimitations.add(new SelectItem(useLimit, useLimit));
            });
        }

        organisations = new ArrayList<>();
        Map<String, Organisation> orgMap = config.getOrgMappings();
        if (orgMap != null) {
            ArrayList<String> list = new ArrayList<>();
            orgMap.forEach((k, v) -> {
                list.add(k);
            });
            Collections.sort(list);
            list.forEach((org) -> {
                organisations.add(new SelectItem(org, org));
            });
        }
//
//        GmxAnchor requiredUseLimit = new GmxAnchor();
//        mConst.setRequiredUseLimitations(requiredUseLimit);

        temporal = new Temporal();

        this.type = mType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAltTitle() {
        return altTitle;
    }

    public void setAltTitle(String altTitle) {
        this.altTitle = altTitle;
    }

    public Node getTitleNode() {
        return titleNode;
    }

    public void setTitleNode(Node titleNode) {
        this.titleNode = titleNode;
    }

    public Node getAltTitleNode() {
        return altTitleNode;
    }

    public void setAltTitleNode(Node altTitleNode) {
        this.altTitleNode = altTitleNode;
    }

    public String getPlainTextAbstract() {
        return plainTextAbstract;
    }

    public void setPlainTextAbstract(String plainTextAbstract) {
        this.plainTextAbstract = plainTextAbstract;
    }

    public Node getPlainTextAbstractNode() {
        return plainTextAbstractNode;
    }

    public void setPlainTextAbstractNode(Node plainTextAbstractNode) {
        this.plainTextAbstractNode = plainTextAbstractNode;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

//    public Node getDoiNode() {
//        return doiNode;
//    }
//
//    public void setDoiNode(Node doiNode) {
//        this.doiNode = doiNode;
//    }

    public Node getDataId() {
        return dataId;
    }

    public void setDataId(Node dataId) {
        this.dataId = dataId;
    }

    public Node getCiCitation() {
        return ciCitation;
    }

    public void setCiCitation(Node ciCitation) {
        this.ciCitation = ciCitation;
    }

    public Node getLanguage() {
        return language;
    }

    public void setLanguage(Node language) {
        this.language = language;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Node getPurposeNode() {
        return purposeNode;
    }

    public void setPurposeNode(Node purposeNode) {
        this.purposeNode = purposeNode;
    }

    public String getStatusList() {
        return statusList;
    }

    public void setStatusList(String statusList) {
        this.statusList = statusList;
    }

    public String getStatusListValue() {
        return statusListValue;
    }

    public void setStatusListValue(String statusListValue) {
        this.statusListValue = statusListValue;
    }

    public Node getStatusNode() {
        return statusNode;
    }

    public void setStatusNode(Node statusNode) {
        this.statusNode = statusNode;
    }

    public List<Constraints> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraints> constraints) {
        this.constraints = constraints;
    }

    public Bbox getBbox() {
        return bbox;
    }

    public void setBbox(Bbox bbox) {
        this.bbox = bbox;
    }

    public Temporal getTemporal() {
        if (temporal == null) {
            temporal = new Temporal();
        }
        return temporal;
    }

    public void setTemporal(Temporal temporal) {
        this.temporal = temporal;
    }

    public Node getResourceSpecificUsageNode() {
        return resourceSpecificUsageNode;
    }

    public void setResourceSpecificUsageNode(Node resourceSpecificUsageNode) {
        this.resourceSpecificUsageNode = resourceSpecificUsageNode;
    }

//    public Node getResourceConstraintsNode() {
//        return resourceConstraintsNode;
//    }
//
//    public void setResourceConstraintsNode(Node resourceConstraintsNode) {
//        this.resourceConstraintsNode = resourceConstraintsNode;
//    }
    public Node getAggregationInfoNode() {
        return aggregationInfoNode;
    }

    public void setAggregationInfoNode(Node aggregationInfoNode) {
        this.aggregationInfoNode = aggregationInfoNode;
    }

    public Node getSpatialRepresentationTypeNode() {
        return spatialRepresentationTypeNode;
    }

    public void setSpatialRepresentationTypeNode(Node spatialRepresentationTypeNode) {
        this.spatialRepresentationTypeNode = spatialRepresentationTypeNode;
    }

    public Node getSpatialResolutionNode() {
        return spatialResolutionNode;
    }

    public void setSpatialResolutionNode(Node spatialResolutionNode) {
        this.spatialResolutionNode = spatialResolutionNode;
    }

    public Node getSupplementalNodeForRef() {
        return supplementalNodeForRef;
    }

    public void setSupplementalNodeForRef(Node supplementalNodeForRef) {
        this.supplementalNodeForRef = supplementalNodeForRef;
    }

//    public CharacterData getJsonSupplementalNode() {
//        return jsonSupplementalNode;
//    }
//
//    public void setJsonSupplementalNode(CharacterData jsonSupplementalNode) {
//        this.jsonSupplementalNode = jsonSupplementalNode;
//    }
    public List<EarthTopic> getEarthTopics() {
        return earthTopics;
    }

    public void setEarthTopics(List<EarthTopic> earthTopics) {
        this.earthTopics = earthTopics;
    }

//    public List<EopKeyword> getEopKeywords() {
//        return eopKeywords;
//    }
//
//    public void setEopKeywords(List<EopKeyword> eopKeywords) {
//        this.eopKeywords = eopKeywords;
//    }
//    public List<FreeKeyword> getFreeKeywords() {
//        return freeKeywords;
//    }
//
//    public void setFreeKeywords(List<FreeKeyword> freeKeywords) {
//        this.freeKeywords = freeKeywords;
//    }
    public FreeKeyword getFreeKeyword() {
        return freeKeyword;
    }

    public void setFreeKeyword(FreeKeyword freeKeyword) {
        this.freeKeyword = freeKeyword;
    }

    public FreeKeyword getPlaceKeyword() {
        return placeKeyword;
    }

    public void setPlaceKeyword(FreeKeyword placeKeyword) {
        this.placeKeyword = placeKeyword;
    }

    public FreeKeyword getSpatialDataServiceCategoryKeywords() {
        return spatialDataServiceCategoryKeywords;
    }

    public void setSpatialDataServiceCategoryKeywords(FreeKeyword spatialDataServiceCategoryKeywords) {
        this.spatialDataServiceCategoryKeywords = spatialDataServiceCategoryKeywords;
    }

//    public List<String> getCorrections() {
//        return corrections;
//    }
//
//    public void setCorrections(List<String> corrections) {
//        this.corrections = corrections;
//    }
//
//    public void addCorrection(String info) {
//        if (this.corrections == null) {
//            this.corrections = new ArrayList<>();
//        }
//        this.corrections.add(info);
//    }

    /*
     * ************************************
     */
    private String selectedEOField;

    public String getSelectedEOField() {
        return selectedEOField;
    }

    public void setSelectedEOField(String selectedEOField) {
        this.selectedEOField = selectedEOField;
    }

//    private List<SelectItem> eoList;
//
//    public void setEoList(List<SelectItem> eoList) {
//        this.eoList = eoList;
//    }
//
//    public List<SelectItem> getEoList() {
//        eoList = new ArrayList<>();
//        List<String> eopKws = new ArrayList<>();
//        if (eopKeywords != null) {
//            for (ThesaurusKeyword kw : eopKeywords) {
//                eopKws.add(kw.getCodeListValue());
//            }
//        }
//
//        for (Map.Entry<String, SelectItem> entry : eopKeywordMap.entrySet()) {
//            if (!eopKws.contains(entry.getKey())) {
//                eoList.add(entry.getValue());
//            }
//        }
//
//        return eoList;
//    }
//    public void eoSelectionChanged(final AjaxBehaviorEvent event) {
//        log.debug("EO selection changed.");
//        log.debug("selection: " + selectedEOField);
//
//        if (StringUtils.isNotEmpty(selectedEOField)) {
//            addEopKeyword(createEmptyEopKeyword(selectedEOField));
//        }
//    }
    public void addBbox() {
        bbox = new Bbox();
        bbox.setWest(new Double(-180));
        bbox.setEast(new Double(180));
        bbox.setSouth(new Double(-90));
        bbox.setNorth(new Double(90));
    }

    public void addTemporal() {
        temporal = new Temporal();
        Date currentDate = CommonUtils.getCurrentDate();
        temporal.setStartDate(currentDate);
        temporal.setEndDate(currentDate);
    }

    public void addUseLimitation() {
        Constraints mConst = getConstraint();
        GmxAnchor newUseLimit = new GmxAnchor();
        if (mConst.getUseLimitations() == null) {
            mConst.setUseLimitations(new ArrayList<>());
        }
        mConst.getUseLimitations().add(newUseLimit);
    }

    public void onChangeRichText(final String uuid, final String value) {
//        log.debug("On change rich text");
//        log.debug("uuid = " + uuid);
//        log.debug("value = " + value);
        if (constraints != null && constraints.size() > 0) {
            boolean stop = false;
            for (Constraints mConst : constraints) {
                if (mConst.getUseLimitations() != null
                        && mConst.getUseLimitations().size() > 0) {
                    for (GmxAnchor ul : mConst.getUseLimitations()) {
                        if (ul.getUuid().equals(uuid)) {
                            ul.setRichText(value);
                            stop = true;
                            break;
                        }
                    }
                }
                if (!stop) {
                    if (mConst.getOthers() != null
                            && mConst.getOthers().size() > 0) {
                        for (GmxAnchor ul : mConst.getOthers()) {
                            if (ul.getUuid().equals(uuid)) {
                                ul.setRichText(value);
                                stop = true;
                                break;
                            }
                        }
                    }
                }
                if (stop) {
                    break;
                }
            }
        }
    }

    public void removeUseLimitation(GmxAnchor limit) {
        if (constraints != null && constraints.size() > 0) {
            for (Constraints mConst : constraints) {
                if (mConst.getUseLimitations() != null
                        && mConst.getUseLimitations().size() > 0
                        && mConst.getUseLimitations().contains(limit)) {
                    mConst.getUseLimitations().remove(limit);
                    break;
                }
            }

        }
    }

    public void addAccess() {
        Constraints mConst = getConstraint();
        if (mConst.getAccesses() == null) {
            mConst.setAccesses(new ArrayList<>());
        }

        GmxAnchor newAccess = new GmxAnchor();
        mConst.getAccesses().add(newAccess);
    }

    public void removeAccess(GmxAnchor constraint) {
        if (constraints != null && constraints.size() > 0) {
            for (Constraints mConst : constraints) {
                if (mConst.getAccesses() != null
                        && mConst.getAccesses().size() > 0
                        && mConst.getAccesses().contains(constraint)) {
                    mConst.getAccesses().remove(constraint);
                    break;
                }
            }
        }
    }

    public void addUse() {
        Constraints mConst = getConstraint();
        if (mConst.getUses() == null) {
            mConst.setUses(new ArrayList<>());
        }

        GmxAnchor newUse = new GmxAnchor();
        mConst.getUses().add(newUse);
    }

    public void removeUse(GmxAnchor constraint) {
        if (constraints != null && constraints.size() > 0) {
            for (Constraints mConst : constraints) {
                if (mConst.getUses() != null
                        && mConst.getUses().size() > 0
                        && mConst.getUses().contains(constraint)) {
                    mConst.getUses().remove(constraint);
                    break;
                }
            }

        }
    }

    public void addOther() {
        Constraints mConst = getConstraint();
        if (mConst.getOthers() == null) {
            mConst.setOthers(new ArrayList<>());
        }

        GmxAnchor newOther = new GmxAnchor();
        mConst.getOthers().add(newOther);
    }

    public void removeOther(GmxAnchor constraint) {
        if (constraints != null && constraints.size() > 0) {
            for (Constraints mConst : constraints) {
                if (mConst.getOthers() != null
                        && mConst.getOthers().size() > 0
                        && mConst.getOthers().contains(constraint)) {
                    mConst.getOthers().remove(constraint);
                    break;
                }
            }

        }
    }

    public boolean update(Acquisition acquisition) throws XPathExpressionException {
        if (StringUtils.isEmpty(title)) {
            FacesMessageUtil.addErrorMessage("The title should not be empty.");
            return false;
        }

        if (StringUtils.isEmpty(plainTextAbstract)) {
            FacesMessageUtil.addErrorMessage("The abstract should not be empty.");
            return false;
        }

        if (titleNode != null) {
            XmlUtils.setTextContent(titleNode, title);
        }

        if (plainTextAbstractNode != null) {
            XmlUtils.setTextContent(plainTextAbstractNode, plainTextAbstract);
        }

        if (altTitleNode != null) {
            XmlUtils.setTextContent(altTitleNode, altTitle);
        } else {
            if (StringUtils.isNotEmpty(altTitle)) {
                // insert node

                Document ownerDoc = dataId.getOwnerDocument();
                Node parent = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:alternateTitle");
                altTitleNode = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
                Node value = ownerDoc.createTextNode(altTitle);
                altTitleNode.appendChild(value);
                parent.appendChild(altTitleNode);

                if (titleNode != null && titleNode.getParentNode() != null
                        && titleNode.getParentNode().getNextSibling() != null) {
                    log.debug("Insert altTitle after title");
                    ciCitation.insertBefore(parent, titleNode.getParentNode().getNextSibling());
                } else {
                    if (creationDateNode != null
                            && creationDateNode.getParentNode() != null
                            && creationDateNode.getParentNode().getParentNode() != null
                            && creationDateNode.getParentNode().getParentNode().getParentNode() != null) {
                        log.debug("Insert altTitle before date");
                        ciCitation.insertBefore(parent, creationDateNode.getParentNode().getParentNode().getParentNode());
                    } else {
                        Node doiRefNode = XmlUtils.getDoiNodeRef(ciCitation);
                        if (doiRefNode != null) {
                            ciCitation.insertBefore(parent, doiRefNode);
                        } else {
                            ciCitation.appendChild(parent);
                        }
                    }
                }
            }
        }

        Node editionNode = XPathUtils
                    .getNode(ciCitation, "./gmd:edition/gco:CharacterString");
        
        if (editionNode != null) {
            XmlUtils.setTextContent(editionNode, edition);
        } else {
            if (StringUtils.isNotEmpty(edition)) {
                // insert node                 
                Node newEdition = dataId.getOwnerDocument().importNode(XmlUtils.createEdition(edition), true);
                XmlUtils.cleanNamespaces(newEdition);
                Node editionRefNode = XmlUtils.getEditionNodeRef(ciCitation);
                if (editionRefNode != null) {
                    ciCitation.insertBefore(newEdition, editionRefNode);
                } else {
                    ciCitation.appendChild(newEdition);
                }
            }
        }

        Node doiNode = XPathUtils
                    .getNode(ciCitation, "./gmd:identifier/gmd:RS_Identifier[gmd:codeSpace/gco:CharacterString='http://doi.org']/gmd:code/gco:CharacterString");
        
        if (doiNode != null) {
            XmlUtils.setTextContent(doiNode, doi);
        } else {
            if (StringUtils.isNotEmpty(doi)) {
                // insert node                 
                Node newDOI = dataId.getOwnerDocument().importNode(XmlUtils.createDoi(doi), true);
                XmlUtils.cleanNamespaces(newDOI);
                Node doiRefNode = XmlUtils.getDoiNodeRef(ciCitation);
                if (doiRefNode != null) {
                    ciCitation.insertBefore(newDOI, doiRefNode);
                } else {
                    ciCitation.appendChild(newDOI);
                }
            }
        }
        
        Node otherCitationDetailsNode = XPathUtils
                    .getNode(ciCitation, "./gmd:otherCitationDetails/gco:CharacterString");
        if (otherCitationDetailsNode != null) {
            XmlUtils.setTextContent(otherCitationDetailsNode, otherCitationDetails);
        } else {
            if (StringUtils.isNotEmpty(otherCitationDetails)) {
                // insert node                 
                Node newOtherCitationDetailsNode = dataId.getOwnerDocument()
                        .importNode(XmlUtils
                                .createOtherCitationDetails(otherCitationDetails), true);
                XmlUtils.cleanNamespaces(newOtherCitationDetailsNode);
                Node otherCitationDetailsRefNode = XmlUtils.getOtherCitationDetailsRefNode(ciCitation);
                if (otherCitationDetailsRefNode != null) {
                    ciCitation.insertBefore(newOtherCitationDetailsNode, otherCitationDetailsRefNode);
                } else {
                    ciCitation.appendChild(newOtherCitationDetailsNode);
                }

//                setOtherCitationDetailsNode(XPathUtils
//                        .getNode(ciCitation, "./gmd:otherCitationDetails/gco:CharacterString"));
            }
        }

        if (creationDateNode != null) {
            XmlUtils.setTextContent(creationDateNode,
                    CommonUtils.dateToStr(creationDate));
        }

        if (topicCategoryNode != null) {
            if (StringUtils.isNotEmpty(topicCategory)) {
                XmlUtils.setTextContent(topicCategoryNode, topicCategory);
            } else {
                // remove gmd:topicCategory node
                log.debug("Remove node gmd:topicCategory");
                dataId.removeChild(topicCategoryNode.getParentNode());
            }
        } else {
            if (StringUtils.isNotEmpty(topicCategory)) {
                // create gmd:topicCategory node
                log.debug("Create a new gmd:topicCategory node");
                // insert node                 
                topicCategoryNode = dataId.getOwnerDocument().importNode(XmlUtils.createTopicCategory(topicCategory), true);
                XmlUtils.cleanNamespaces(topicCategoryNode);
                Node topicCategoryRefNode = XmlUtils.getTopicCategoryNodeRef(dataId);
                if (topicCategoryRefNode != null) {
                    dataId.insertBefore(topicCategoryNode, topicCategoryRefNode);
                } else {
                    dataId.appendChild(topicCategoryNode);
                }
            }
        }

        if (temporal != null) {
            try {
                if (temporal.getSelf() != null) {
                    log.debug("Update existing temporal");
                    temporal.update();
                } else {
                    log.debug("Created a new temporal");
                    temporal.validate();
                    Node importedNode = dataId.getOwnerDocument().importNode(XmlUtils.createTemporal(temporal), true);
                    XmlUtils.cleanNamespaces(importedNode);

                    if (supplementalNodeForRef != null) {
                        dataId.insertBefore(importedNode, supplementalNodeForRef);
                    } else {
                        dataId.appendChild(importedNode);
                    }
                }
            } catch (IOException e) {
                log.debug("Error while updating temporal: " + CommonUtils.getErrorMessage(e));
                FacesMessageUtil.addErrorMessage("Error while updating temporal: " + CommonUtils.getErrorMessage(e));
                return false;
            }
        }

        if (bbox != null) {
            try {
                if (bbox.getSelf() != null) {
                    log.debug("Update BBOX");
                    bbox.update();
                } else {
                    log.debug("Create BBOX");
                    bbox.validate();
                    Node bboxNode = dataId.getOwnerDocument().importNode(XmlUtils.createBbox(bbox), true);
                    if (supplementalNodeForRef != null) {
                        dataId.insertBefore(bboxNode, supplementalNodeForRef);
                    } else {
                        dataId.appendChild(bboxNode);
                    }
                }
            } catch (IOException e) {
                log.debug("Error while updating bounding box: " + CommonUtils.getErrorMessage(e));
                FacesMessageUtil.addErrorMessage("Error while updating bounding box: " + CommonUtils.getErrorMessage(e));
                return false;
            }
        }

        /**
         * *************************
         * Update/create keywords ************************
         */
        /*
            1. EarthTopics keywords
         */
        // first remove all existing EarthTopics keywords
        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='"
                + config.getEarthtopicsThesaurusUri() + "']");

        // second remove all existing GCMD science keywords
        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='"
                + config.getSckwThesaurusUri() + "']");

        // second remove all existing GCMD science keywords (old thesaurus URI)
        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='"
                + config.getOldSckwThesaurusUri() + "']");

        if (isService()) {
            // first remove all existing EarthTopics keywords
            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getEarthtopicThesaurus().getFullTitle() + "']");

            // second remove all existing GCMD science keywords
            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getSckwThesaurus().getFullTitle() + "']");
        }

        // then insert new EarthTopics and GCMD science keywords
        //Node nodeRef = getKeywordNodeRef();
        Node nodeRef = XmlUtils.getDescriptiveKeywordNodeRef(this);
        //boolean insertAfter = false;
        if (earthTopics != null && earthTopics.size() > 0) {
            Node earthTopicsNode = dataId.getOwnerDocument()
                    .importNode(XmlUtils.createEarthTopicsNode(earthTopics, config.getEarthtopicThesaurus(), isService()), true);
            XmlUtils.cleanNamespaces(earthTopicsNode);

            dataId.insertBefore(earthTopicsNode, nodeRef);

            List<Concept> scKeywords = new ArrayList<>();

            for (EarthTopic eTopic : earthTopics) {
                List<Concept> sckwConcepts = eTopic.getScienceKeywords();
                if (sckwConcepts != null && sckwConcepts.size() > 0) {
                    scKeywords.addAll(sckwConcepts);
                }
            }

            if (scKeywords.size() > 0) {
                // insert GCMD keywords right after EarthTopics keywords
                Node sckwNode = dataId.getOwnerDocument()
                        .importNode(XmlUtils.createScienceKeywordNode(scKeywords, config.getSckwThesaurus(), isService()), true);
                XmlUtils.cleanNamespaces(sckwNode);

                dataId.insertBefore(sckwNode, nodeRef);
            }
        }

        /*
            2. EOP keywords
         */
        // first remove all existing EOP 2.1 keywords
        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='"
                + config.getEopThesaurus().getTitleUri() + "']");

        // second remove all existing EOP extension keywords
        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='"
                + config.getEopExtThesaurus().getTitleUri() + "']");
        if (isService()) {
            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getEopThesaurus().getTitle() + "']");

            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getEopExtThesaurus().getTitle() + "']");
        }

        // then insert new EOP 2.1 and EOP Ext keywords
        //log.debug(XmlUtils.nodeToString(nodeRef));

        insertEOPKeyWord(waveLength, nodeRef);
        insertEOPKeyWord(orbitType, nodeRef);
        insertEOPKeyWord(processorVersion, nodeRef);
        insertEOPKeyWord(resolution, nodeRef);
        insertEOPKeyWord(productType, nodeRef);

        insertEOPKeyWord(orbitHeight, nodeRef);
        insertEOPKeyWord(swathWidth, nodeRef);

        /*
             3. Instrument type keywords
         */
        // first remove existing Platforms keyword
//        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='"
//                + config.getPlatformThesaurusUri() + "']");
        // second remove existing Instruments keyword
        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gmx:Anchor/@xlink:href='"
                + config.getInstrumentThesaurusUri() + "']");
        if (isService()) {
            // first remove all existing ESA Platform keywords
            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getPlatformThesaurus().getFullTitle() + "']");

            // second remove all existing GCMD Platform keywords
            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getGcmdPlatformThesaurus().getFullTitle() + "']");

            // first remove all existing ESA Instrument keywords
            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getInstrumentThesaurus().getFullTitle() + "']");

            // second remove all existing GCMD Instrument keywords
            XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                    + config.getGcmdInstrumentThesaurus().getFullTitle() + "']");
        }
        // then insert new Platforms and Instruments keyword
        if (acquisition != null
                && acquisition.getPlatforms() != null
                && acquisition.getPlatforms().size() > 0) {

            if (isService()) {
                List<String> platformKeywords = new ArrayList<>();
                List<String> gcmdPlatformKeywords = new ArrayList<>();
                List<String> instrumentKeywords = new ArrayList<>();
                List<String> gcmdInstrumentKeywords = new ArrayList<>();
                acquisition.getPlatforms().forEach((platform) -> {
                    if (!platformKeywords.contains(platform.getLabel())) {
                        platformKeywords.add(platform.getLabel());
                    }
                    if (platform.getGcmd() != null) {
                        if (!gcmdPlatformKeywords.contains(platform.getGcmd().getLabel())) {
                            gcmdPlatformKeywords.add(platform.getGcmd().getLabel());
                        }
                    }
                    if (platform.getInstruments() != null) {
                        for (Instrument inst : platform.getInstruments()) {
                            if (!instrumentKeywords.contains(inst.getLabel())) {
                                instrumentKeywords.add(inst.getLabel());
                            }
                            if (inst.getGcmd() != null) {
                                if (!gcmdInstrumentKeywords.contains(inst.getGcmd().getLabel())) {
                                    gcmdInstrumentKeywords.add(inst.getGcmd().getLabel());
                                }
                            }
                        }
                    }
                });

                if (gcmdInstrumentKeywords.size() > 0) {
                    // insert GCMD Instrument keywords
                    Node node = dataId.getOwnerDocument()
                            .importNode(XmlUtils
                                    .createServiceKeywordNode(gcmdInstrumentKeywords,
                                            config.getGcmdInstrumentThesaurus()), true);
                    XmlUtils.cleanNamespaces(node);

                    dataId.insertBefore(node, nodeRef);
                }

                if (instrumentKeywords.size() > 0) {
                    // insert Instrument keywords
                    Node node = dataId.getOwnerDocument()
                            .importNode(XmlUtils
                                    .createServiceKeywordNode(instrumentKeywords,
                                            config.getInstrumentThesaurus()), true);
                    XmlUtils.cleanNamespaces(node);

                    dataId.insertBefore(node, nodeRef);
                }

                if (gcmdPlatformKeywords.size() > 0) {
                    // insert GCMD Platform keywords
                    Node node = dataId.getOwnerDocument()
                            .importNode(XmlUtils
                                    .createServiceKeywordNode(gcmdPlatformKeywords,
                                            config.getGcmdPlatformThesaurus()), true);
                    XmlUtils.cleanNamespaces(node);

                    dataId.insertBefore(node, nodeRef);
                }

                if (platformKeywords.size() > 0) {
                    // insert Platform keywords
                    Node node = dataId.getOwnerDocument()
                            .importNode(XmlUtils
                                    .createServiceKeywordNode(platformKeywords,
                                            config.getPlatformThesaurus()), true);
                    XmlUtils.cleanNamespaces(node);

                    dataId.insertBefore(node, nodeRef);
                }

            } else {
                /*
                    create equivalent descriptiveKeywords of instrument types
                 */
                List<Concept> instrumentTypes = new ArrayList<>();
                acquisition.getPlatforms().forEach((platform) -> {
                    if (platform.getInstruments() != null) {
                        for (Instrument inst : platform.getInstruments()) {
                            if (inst.getBroaders() != null) {
                                for (Concept instType : inst.getBroaders()) {
                                    if (!instrumentTypes.contains(instType)) {
                                        instrumentTypes.add(instType);
                                    }
                                }
                            }
                        }
                    }
                });

                if (!instrumentTypes.isEmpty()) {
                    Node instrumentKwNode = dataId.getOwnerDocument()
                            .importNode(XmlUtils
                                    .createInstrumentKeywordNode(instrumentTypes, config.getInstrumentThesaurus()), true);
                    XmlUtils.cleanNamespaces(instrumentKwNode);
                    dataId.insertBefore(instrumentKwNode, nodeRef);
                }
            }
        }

        /*
            4. Spatial Data Service Category keywords
         */
        // first remove all existing Spatial Data Service Category keywords
        XmlUtils.removeNodesByXpath(dataId, "./gmd:descriptiveKeywords[./gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='"
                + config.getSpatialDataServiceCategoryThesaurus().getFullTitle() + "']");
        if (spatialDataServiceCategoryKeywords != null
                && XmlUtils.hasKeyword(spatialDataServiceCategoryKeywords)) {
            log.debug("MNG has Spatial Data Service Category kw");
            Node sdsCatKwNode = dataId.getOwnerDocument()
                    .importNode(XmlUtils
                            .createSpatialDataServiceCategoryNode(spatialDataServiceCategoryKeywords,
                                    config.getSpatialDataServiceCategoryThesaurus()), true);
            XmlUtils.cleanNamespaces(sdsCatKwNode);
            dataId.insertBefore(sdsCatKwNode, nodeRef);
        }
        /*
             5. Place keywords
         */
        // first remove all existing place keywords (remove all descriptiveKeywords that have no thesaurusName and type/@codeListValue='place'
        XPathUtils.removeNodes(dataId, "./gmd:descriptiveKeywords[not(./gmd:MD_Keywords/gmd:thesaurusName) and (./gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='place')]");

        // then insert place keywords
        if (placeKeyword != null
                && XmlUtils.hasKeyword(placeKeyword)) {
            log.debug("MNG has place kw");
            Node placeKwNode = dataId.getOwnerDocument()
                    .importNode(XmlUtils.createFreeKeywordNode(placeKeyword), true);
            XmlUtils.cleanNamespaces(placeKwNode);
            dataId.insertBefore(placeKwNode, nodeRef);
        }

        /*
             6. Free keywords
         */
        // first remove all existing free keywords (remove all descriptiveKeywords that have no thesaurusName nor type
        XPathUtils.removeNodes(dataId, "./gmd:descriptiveKeywords[not(./gmd:MD_Keywords/gmd:thesaurusName) and not (./gmd:MD_Keywords/gmd:type)]");

        // then insert the free keyword
        if (XmlUtils.hasKeyword(freeKeyword)) {
            Node freeKwNode = dataId.getOwnerDocument()
                    .importNode(XmlUtils.createFreeKeywordNode(freeKeyword), true);
            XmlUtils.cleanNamespaces(freeKwNode);
            dataId.insertBefore(freeKwNode, nodeRef);
        }

//        // then insert new free keywords
//        if (freeKeywords != null && freeKeywords.size() > 0) {
//            for (FreeKeyword freeKw : freeKeywords) {
//                if (XmlUtils.hasKeyword(freeKw)) {
//                    Node freeKwNode = dataId.getOwnerDocument()
//                            .importNode(XmlUtils.createFreeKeywordNode(freeKw), true);
//                    XmlUtils.cleanNamespaces(freeKwNode);
//                    dataId.insertBefore(freeKwNode, nodeRef);
//                }
//            }
//        }
        /*
            Update constraints
         */
        if (constraints != null) {
            constraints.forEach((constraint) -> {
                updateConstraint(constraint);
            });
        }

        if (serviceType != null) {
            // Remove srv:serviceType element
            XPathUtils.removeNodes(dataId, "./srv:serviceType");

            // Re-add a new srv:serviceType element
            Node serviceTypeRefNode = XmlUtils.getServiceTypeNodeRef(this);
            Node serviceTypeNode = dataId.getOwnerDocument()
                    .importNode(XmlUtils.createServiceType(serviceType), true);
            XmlUtils.cleanNamespaces(serviceTypeNode);
            dataId.insertBefore(serviceTypeNode, serviceTypeRefNode);
        }

        return true;
    }

//    public void updateSupplementalInfo(String info) {
//        if (StringUtils.isNotEmpty(info)) {
//            // add offerings
//            if (jsonSupplementalNode != null) {
//                jsonSupplementalNode.setData(info);
//            } else {
//                Node importedNode = dataId.getOwnerDocument().importNode(XmlUtils.createSuppInfo(info), true);
//                XmlUtils.cleanNamespaces(importedNode);
//                dataId.appendChild(importedNode);
//                NodeList list = importedNode.getChildNodes();
//                for (int index = 0; index < list.getLength(); index++) {
//                    if (list.item(index) instanceof CharacterData) {
//                        jsonSupplementalNode = (CharacterData) list.item(index);
//                        break;
//                    }
//                }
//            }
//        } else {
//            // remove offerings
//            if (jsonSupplementalNode != null) {
//                jsonSupplementalNode.getParentNode().getParentNode().getParentNode()
//                        .removeChild(jsonSupplementalNode.getParentNode().getParentNode());
//            }
//        }
//
//    }
    private Constraints getConstraint() {
        if (constraints == null) {
            constraints = new ArrayList<>();
        }

        if (constraints.isEmpty()) {
            constraints.add(new Constraints());
        }

        return constraints.get(0);
    }

//    private void updateOrInsertMDKeyword(MDKeyword mdKw) throws XPathExpressionException {
//        if (mdKw != null) {
//            if (mdKw.getMdKeyword() != null) {
//                updateMDKeyword(mdKw);
//            } else {
//                if (mdKw.getKeywords() != null && mdKw.getKeywords().size() > 0) {
//                    Node newDescKw = dataId.getOwnerDocument().importNode(XmlUtils.createDescriptiveKeywordsNode(mdKw), true);
//                    Node nodeRef = getKeywordNodeRef();
//                    dataId.insertBefore(newDescKw, nodeRef);
//                }
//            }
//        }
//    }
//
//    private boolean updateMDKeyword(MDKeyword mdKw) throws XPathExpressionException {
//        if (mdKw.getKeywords() == null || mdKw.getKeywords().isEmpty()) {
//            /*
//             remove the descriptiveKeywords
//             */
//            Node parentNode = mdKw.getMdKeyword().getParentNode();
//            parentNode.removeChild(mdKw.getMdKeyword());
//            parentNode.getParentNode().removeChild(parentNode);
//            return true;
//        }
//
//        XPath xpath = XPathUtils.getXPath();
//
//        XPathExpression expr = xpath.compile("./gmd:keyword");
//        NodeList keywordNodes = (NodeList) expr.evaluate(mdKw.getMdKeyword(), XPathConstants.NODESET);
//        if (keywordNodes != null && keywordNodes.getLength() > 0) {
//            for (int i = 0; i < keywordNodes.getLength(); i++) {
//                Node kw = keywordNodes.item(i);
//                kw.getParentNode().removeChild(kw);
//            }
//        }
//
//        Node refNode = null;
//        if (mdKw.getKeywordTypeNode() != null) {
//            log.debug("Keyword tType ref node");
//            refNode = mdKw.getKeywordTypeNode();
//        } else {
//            if (mdKw.getThesaurus() != null && mdKw.getThesaurus().getSelf() != null) {
//                log.debug("Thesaurus ref node");
//                refNode = mdKw.getThesaurus().getSelf();
//            }
//        }
//
//        if (mdKw.getKeywords() != null && mdKw.getKeywords().size() > 0) {
//            for (Keyword kw : mdKw.getKeywords()) {
//                if ((StringUtils.isNotEmpty(kw.getLink()) && StringUtils.isNotEmpty(kw.getText())) || StringUtils.isNotEmpty(kw.getText())) {
//                    Node kwNode = createKeywordNode(kw);
//                    if (refNode != null) {
//                        mdKw.getMdKeyword().insertBefore(kwNode, refNode);
//                    } else {
//                        mdKw.getMdKeyword().appendChild(kwNode);
//                    }
//                }
//            }
//        }
//        return true;
//    }
//
//    private Node createKeywordNode(Keyword kw) {
//        Document ownerDoc = dataId.getOwnerDocument();
//        Node kwNode = ownerDoc.createElementNS(Constants.GMD_NS, "gmd:keyword");
//
//        if (StringUtils.isNotEmpty(kw.getLink()) && StringUtils.isNotEmpty(kw.getText())) {
//            Node anchor = ownerDoc.createElementNS(Constants.GMX_NS, "gmx:Anchor");
//            kwNode.appendChild(anchor);
//            ((Element) anchor).setAttributeNS(Constants.XLINK_NS, "xlink:href", kw.getLink());
//            anchor.setTextContent(kw.getText());
//        } else {
//            if (StringUtils.isNotEmpty(kw.getText())) {
//                Node textNode = ownerDoc.createElementNS(Constants.GCO_NS, "gco:CharacterString");
//                textNode.setTextContent(kw.getText());
//                kwNode.appendChild(textNode);
//            }
//        }
//
//        return kwNode;
//    }
//    private ThesaurusKeyword createEmptyEopKeyword(String codeListValue) {
//        ThesaurusKeyword kw = new ThesaurusKeyword();
//        kw.setCodeListValue(codeListValue);
//        switch (selectedEOField) {
//            case Constants.ORBITHEIGHT_KEY:
//            case Constants.SWATHWIDTH_KEY:
//                kw.setExt(true);
//                break;
//        }
//        kw.addEmptyKeyword();
//        return kw;
//    }
//    private Node getKeywordNodeRef() {
//
//        if (resourceSpecificUsageNode != null) {
//            return resourceSpecificUsageNode;
//        }
//
//        if (resourceConstraintsNode != null) {
//            return resourceConstraintsNode;
//        }
//
//        if (aggregationInfoNode != null) {
//            return aggregationInfoNode;
//        }
//
//        if (spatialRepresentationTypeNode != null) {
//            return spatialRepresentationTypeNode;
//        }
//
//        if (spatialResolutionNode != null) {
//            return spatialResolutionNode;
//        }
//
//        return language;
//    }
    private void updateConstraint(Constraints constraint) {
        if (constraint.getSelf() != null) {
            //System.out.println("MNGMNG Self Node: " + constraint.getSelf().getLocalName());
            // remove existing useLimitations
            XPathUtils.removeNodes(constraint.getSelf(), "./gmd:useLimitation");

            // remove existing otherConstraints
            XPathUtils.removeNodes(constraint.getSelf(), "./gmd:otherConstraints");

            boolean append = false;
            Node nodeRef = null;
            if (constraint.getUseLimitations() != null) {
                for (GmxAnchor sp : constraint.getUseLimitations()) {
                    nodeRef = XmlUtils.createConstraintNode(constraint.getSelf(),
                            "useLimitation", sp, nodeRef, append, isService());
                }
            }

            if (constraint.getOthers() != null) {
                constraint.getOthers().forEach((sp) -> {
                    XmlUtils.createConstraintNode(constraint.getSelf(),
                            "otherConstraints", sp, null, true, isService());
                });
            }

        } else {
            /*
             case: insert a new gmd:resourceConstraints
             */
            Node rsConstraintsNode = XmlUtils.createResourceConstraints(constraint, isService());
            if (rsConstraintsNode != null) {
                Node importedRsConstraintsNode = dataId.getOwnerDocument().importNode(rsConstraintsNode, true);
                XmlUtils.cleanNamespaces(importedRsConstraintsNode);
                //Node nodeRef = getConstraintNodeRef();
                Node nodeRef = XmlUtils.getConstraintNodeRef(this);
                //System.out.println("MNGMNG Ref Node: " + nodeRef.getLocalName());
                dataId.insertBefore(importedRsConstraintsNode, nodeRef);
                constraint.setSelf(XPathUtils.getNode(importedRsConstraintsNode, "./gmd:MD_LegalConstraints"));
            }
        }
    }

//    private Node getConstraintNodeRef() {
//        if (aggregationInfoNode != null) {
//            return aggregationInfoNode;
//        }
//
//        if (spatialRepresentationTypeNode != null) {
//            return spatialRepresentationTypeNode;
//        }
//
//        if (spatialResolutionNode != null) {
//            return spatialResolutionNode;
//        }
//        return language;
//    }
    public void addEarthTopic(EarthTopic eTopic) {
        if (earthTopics == null) {
            earthTopics = new ArrayList<>();
        }
        earthTopics.add(eTopic);
    }

    public void addEarthTopic(Concept concept) {
        if (earthTopics == null) {
            earthTopics = new ArrayList<>();
        }
        boolean existing = false;
        for (EarthTopic eTopic : earthTopics) {
            if (eTopic.getUri().equalsIgnoreCase(concept.getUri())) {
                existing = true;
                break;
            }
        }

        if (existing) {
            log.debug("Earth topic already exists");
            FacesMessageUtil.addErrorMessage(String.format("The Earth Topic %s already exists", concept.getLabel()));
        } else {
            earthTopics.add(MetadataUtils.createEarthTopic(concept, config));
        }
    }

    public void removeEarthTopic(EarthTopic eTopic) {
        this.earthTopics.remove(eTopic);
    }

//    public void addEopKeyword(ThesaurusKeyword eopKw) {
//        if (eopKeywords == null) {
//            eopKeywords = new ArrayList<>();
//        }
//        eopKeywords.add(eopKw);
//    }
//    public void removeEopKeyword(ThesaurusKeyword eopKw) {
//        eopKeywords.remove(eopKw);
//    }    
    private ThesaurusKeyword createEmptyEopKeyword(String codeListValue, boolean ext) {
        ThesaurusKeyword eopkw = new ThesaurusKeyword();
        eopkw.setCodeListValue(codeListValue);

        switch (codeListValue) {
            case Constants.ORBITTYPE_KEY:
                eopkw.setAvailableValues(new ArrayList<>(config.getOrbitType()));
                break;

            case Constants.WAVELENGTH_KEY:
                eopkw.setAvailableValues(new ArrayList<>(config.getWavelengthInformation()));
                break;

            case Constants.RESOLUTION_KEY:
                eopkw.setAvailableValues(new ArrayList<>(config.getResolution()));
                break;
        }

        if (ext) {
            eopkw.setThesaurus(new Thesaurus(config.getEopExtThesaurus()));
        } else {
            eopkw.setThesaurus(new Thesaurus(config.getEopThesaurus()));
        }

        return eopkw;
    }

    private void insertEOPKeyWord(ThesaurusKeyword eopKw, Node nodeRef) {
        if (XmlUtils.hasKeyword(eopKw)) {
            Node eopKwNode = dataId.getOwnerDocument()
                    .importNode(XmlUtils.createEopKeywordNode(eopKw, isService()), true);
            XmlUtils.cleanNamespaces(eopKwNode);

            dataId.insertBefore(eopKwNode, nodeRef);
        }
    }

//    private FreeKeyword createEmptyFreeKeyword() {
//        FreeKeyword freeKw = new FreeKeyword();
//        freeKw.addEmptyKeywords();
//        return freeKw;
//    }
    public String getAllConstraintUuids() {
        if (constraints != null && !constraints.isEmpty()) {
            List<String> constUuids = new ArrayList<>();
            constraints.stream().map((constr) -> {
                if (constr.getUseLimitations() != null) {
                    constr.getUseLimitations().forEach((ul) -> {
                        constUuids.add(ul.getUuid());
                    });
                }
                return constr;
            }).filter((constr) -> (constr.getOthers() != null)).forEachOrdered((constr) -> {
                constr.getOthers().forEach((other) -> {
                    constUuids.add(other.getUuid());
                });
            });
            return String.join("#", constUuids);
        }
        return "";
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public Node getRevisionDateNode() {
        return revisionDateNode;
    }

    public void setRevisionDateNode(Node revisionDateNode) {
        this.revisionDateNode = revisionDateNode;
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Node getPublicationDateNode() {
        return publicationDateNode;
    }

    public void setPublicationDateNode(Node publicationDateNode) {
        this.publicationDateNode = publicationDateNode;
    }

    public Node getCreationDateNode() {
        return creationDateNode;
    }

    public void setCreationDateNode(Node creationDateNode) {
        this.creationDateNode = creationDateNode;
    }

    public void setOrbitType(ThesaurusKeyword orbitType) {
        this.orbitType = orbitType;
    }

    public void setWaveLength(ThesaurusKeyword waveLength) {
        this.waveLength = waveLength;
    }

    public void setProcessorVersion(ThesaurusKeyword processorVersion) {
        this.processorVersion = processorVersion;
    }

    public void setResolution(ThesaurusKeyword resolution) {
        this.resolution = resolution;
    }

    public void setProductType(ThesaurusKeyword productType) {
        this.productType = productType;
    }

    public void setOrbitHeight(ThesaurusKeyword orbitHeight) {
        this.orbitHeight = orbitHeight;
    }

    public void setSwathWidth(ThesaurusKeyword swathWidth) {
        this.swathWidth = swathWidth;
    }

    public ThesaurusKeyword getOrbitType() {
        return orbitType;
    }

    public ThesaurusKeyword getWaveLength() {
        return waveLength;
    }

    public ThesaurusKeyword getProcessorVersion() {
        return processorVersion;
    }

    public ThesaurusKeyword getResolution() {
        return resolution;
    }

    public ThesaurusKeyword getProductType() {
        return productType;
    }

    public ThesaurusKeyword getOrbitHeight() {
        return orbitHeight;
    }

    public ThesaurusKeyword getSwathWidth() {
        return swathWidth;
    }

    public String getTopicCategory() {
        return topicCategory;
    }

    public void setTopicCategory(String topicCategory) {
        this.topicCategory = topicCategory;
    }

    public Node getTopicCategoryNode() {
        return topicCategoryNode;
    }

    public void setTopicCategoryNode(Node topicCategoryNode) {
        this.topicCategoryNode = topicCategoryNode;
    }

    public List<SelectItem> getAvailableTopicCategories() {
        return availableTopicCategories;
    }

    public List<SelectItem> getOnlineRSRelatedFields() {
        return onlineRSRelatedFields;
    }

    public List<SelectItem> getOnlineRSFunctions() {
        return onlineRSFunctions;
    }

    public List<SelectItem> getOnlineRsProtocols() {
        return onlineRsProtocols;
    }

    public List<SelectItem> getOnlineRsAppProfiles() {
        return onlineRsAppProfiles;
    }

    public List<SelectItem> getUseLimitations() {
        return useLimitations;
    }

    public List<SelectItem> getOrganisations() {
        return organisations;
    }

//    public List<ThesaurusKeyword> getThesaurusKeywords() {
//        return thesaurusKeywords;
//    }
//
//    public void setThesaurusKeywords(List<ThesaurusKeyword> thesaurusKeywords) {
//        this.thesaurusKeywords = thesaurusKeywords;
//    }
//    public void addThesaurusKeyword(ThesaurusKeyword thesaurusKw) {
//        if (this.thesaurusKeywords == null) {
//            this.thesaurusKeywords = new ArrayList<>();
//        }
//        thesaurusKeywords.add(thesaurusKw);
//    }
    public List<Contact> getPointOfContacts() {
        return pointOfContacts;
    }

    public void setPointOfContacts(List<Contact> pointOfContacts) {
        this.pointOfContacts = pointOfContacts;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

//    public Node getEditionNode() {
//        return editionNode;
//    }
//
//    public void setEditionNode(Node editionNode) {
//        this.editionNode = editionNode;
//    }

    public String getOtherCitationDetails() {
        return otherCitationDetails;
    }

    public void setOtherCitationDetails(String otherCitationDetails) {
        this.otherCitationDetails = otherCitationDetails;
    }

//    public Node getOtherCitationDetailsNode() {
//        return otherCitationDetailsNode;
//    }
//
//    public void setOtherCitationDetailsNode(Node otherCitationDetailsNode) {
//        this.otherCitationDetailsNode = otherCitationDetailsNode;
//    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public List<String> getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    public void setServiceTypeVersion(List<String> serviceTypeVersion) {
        this.serviceTypeVersion = serviceTypeVersion;
    }

    public List<ServiceOperation> getServiceOperations() {
        return serviceOperations;
    }

    public void setServiceOperations(List<ServiceOperation> serviceOperations) {
        this.serviceOperations = serviceOperations;
    }

    public boolean isService() {
        return (type == 2);
    }

    public Map<String, String> getNoMappingScienceKeywords() {
        return noMappingScienceKeywords;
    }

    public void setNoMappingScienceKeywords(Map<String, String> noMappingScienceKeywords) {
        this.noMappingScienceKeywords = noMappingScienceKeywords;
    }

    public Map<String, String> getInstrumentTypeKeywords() {
        return instrumentTypeKeywords;
    }

    public void setInstrumentTypeKeywords(Map<String, String> instrumentTypeKeywords) {
        this.instrumentTypeKeywords = instrumentTypeKeywords;
    }

}
