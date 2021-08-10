/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.models.workspace.identification.GmxAnchor;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.models.workspace.mission.Sponsor;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.parser.XPathUtils;
import be.spacebel.metadataeditor.utils.CommonUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * A representation of AcquisitionInformation of the Internal Metadata Model
 *
 * @author mng
 */
public class Acquisition implements Serializable {

    private final Logger log = Logger.getLogger(getClass());
    private List<Platform> platforms;
    private Node self;
    private List<String> noInstrumentPlatforms;
    private Node operation;
    private String missionStatus;

    public Acquisition() {
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Platform> platforms) {
        this.platforms = platforms;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

//    public List<String> getNoInstrumentPlatforms() {
//        return noInstrumentPlatforms;
//    }
//
//    public void setNoInstrumentPlatforms(List<String> noInstrumentPlatforms) {
//        this.noInstrumentPlatforms = noInstrumentPlatforms;
//    }
    public Node getOperation() {
        return operation;
    }

    public void setOperation(Node operation) {
        this.operation = operation;
    }

    public String getMissionStatus() {
        return missionStatus;
    }

    public void setMissionStatus(String missionStatus) {
        this.missionStatus = missionStatus;
    }

    public String getRemovedPlatforms() {
        if (this.noInstrumentPlatforms == null || this.noInstrumentPlatforms.isEmpty()) {
            return "";
        } else {
            return StringUtils.join(this.noInstrumentPlatforms, " ; ");
        }
    }

    public void update() throws XPathExpressionException {
        if (self != null) {
            if (StringUtils.isNotEmpty(missionStatus)) {
                if (operation != null) {
                    // update status
                    log.debug("Update mission status to " + missionStatus);
                    XPathUtils.updateAttributeValue(operation, "./gmi:MI_Operation/gmi:status/gmd:MD_ProgressCode", "codeListValue", missionStatus);
                } else {
                    // create new operation node
                    log.debug("Create new mission status element " + missionStatus);

                    Node refNode = XmlUtils.getMissionNodeRef(self);
                    Node missionNode = XmlUtils.createMissionNode(missionStatus);
                    Node importedMission = self.getOwnerDocument()
                            .importNode(missionNode, true);
                    XmlUtils.cleanNamespaces(importedMission);
                    if (refNode != null) {
                        self.insertBefore(importedMission, refNode);
                    } else {
                        self.appendChild(importedMission);
                    }

                }
            }
            List<Node> platformNodes = new ArrayList<>();
            if (platforms != null) {
                for (Platform platform : platforms) {
                    if (platform.getSelf() != null) {
                        log.debug("Existing platform " + platform.getLabel());
                        //log.debug(XmlUtils.nodeToString(platform.getSelf()));
                        
                        GmxAnchor altTitle = XmlUtils.buildStringProperty(platform.getSelf(),
                                
                                "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:alternateTitle");
                        log.debug("altTitle: " + altTitle);

                        if (platform.getGcmd() != null && altTitle == null) {
                            // Add GCMD platform if non-existing
                            Node gcmdPlatformNode = XmlUtils.createAlternateTitle(platform.getGcmd());
                            Node importedGcmdPlatforNode = self.getOwnerDocument()
                                    .importNode(gcmdPlatformNode, true);
                            XmlUtils.cleanNamespaces(importedGcmdPlatforNode);
                            Node refNode = XmlUtils.getAlternateTitleNodeRef(platform.getSelf());
                            //System.out.println("MNG MNG " + refNode);
                            if (refNode != null) {
                                refNode.getParentNode().insertBefore(importedGcmdPlatforNode, refNode);
                            }
                        }

                        /*
                            insert instruments                       
                         */
                        if (platform.getInstruments() != null
                                && !platform.getInstruments().isEmpty()) {

                            Node instParentNode = XPathUtils.getNode(platform.getSelf(), "./gmi:MI_Platform");

                            for (Instrument inst : platform.getInstruments()) {
                                if (inst.getSelf() != null) {
                                    log.debug("Existing instrument: " + inst.getLabel());
                                    //log.debug(XmlUtils.nodeToString(inst.getSelf()));
                                    
                                    GmxAnchor instAltTitle = XmlUtils.buildStringProperty(inst.getSelf(),
                                            "./gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:alternateTitle");
                                    log.debug("altTitle: " + instAltTitle);

                                    if (inst.getGcmd() != null && instAltTitle == null) {
                                        // Add GCMD instrument if non-existing
                                        Node gcmdInstNode = XmlUtils.createAlternateTitle(inst.getGcmd());
                                        Node importedGcmdInstNode = self.getOwnerDocument()
                                                .importNode(gcmdInstNode, true);
                                        XmlUtils.cleanNamespaces(importedGcmdInstNode);
                                        Node refNode = XmlUtils.getAlternateTitleNodeRef(inst.getSelf());
                                        if (refNode != null) {
                                            refNode.getParentNode().insertBefore(importedGcmdInstNode, refNode);
                                        }
                                    }
                                } else {
                                    log.debug("Non-existing instrument: " + inst.getLabel());
                                    Node instNode = XmlUtils.createInstrumentNode(inst);
                                    Node importedInstNode = self.getOwnerDocument()
                                            .importNode(instNode, true);
                                    XmlUtils.cleanNamespaces(importedInstNode);
                                    instParentNode.appendChild(importedInstNode);
                                }
                            }
                        }
                        /*
                            update launch date
                         */
                        if (platform.getLaunchDateNode() != null) {
                            XPathUtils.updateNodeValue(platform.getLaunchDateNode(),
                                    "./gmd:CI_Date/gmd:date/gco:Date", CommonUtils.dateToStr(platform.getLaunchDate()));
                        } else {
                            Node launchDateNode = XmlUtils
                                    .createLaunchDateNode(CommonUtils.dateToStr(platform.getLaunchDate()));
                            Node importedLaunchDateNode = self.getOwnerDocument()
                                    .importNode(launchDateNode, true);
                            XmlUtils.cleanNamespaces(importedLaunchDateNode);

                            Node refNode = XmlUtils.getLaunchDateNodeRef(platform.getSelf());
                            if (refNode != null) {
                                // insert after the ref node
                                refNode.getParentNode().insertBefore(importedLaunchDateNode, refNode.getNextSibling());
                            } else {
                                // insert as a first child
                                refNode = XPathUtils.getNode(platform.getSelf(), "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation");
                                if (refNode != null) {
                                    refNode.insertBefore(importedLaunchDateNode, refNode.getFirstChild());
                                }
                            }
                        }

                        /*
                            update sponsors
                         */
                        // first remove all existing sponsors
                        XPathUtils.removeNodes(platform.getSelf(), "./gmi:MI_Platform/gmi:sponsor");

                        // and then add new sponsors if any
                        if (platform.getOperators() != null
                                && !platform.getOperators().isEmpty()) {
                            for (Sponsor sps : platform.getOperators()) {
                                if (sps.getSelf() != null) {
                                    // update sponsor
                                    XPathUtils.updateNodeValue(platform.getLaunchDateNode(),
                                            "./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString", sps.getOperator().getLabel());
                                } else {
                                    // create a new sponsor
                                    Node sponsorNode = XmlUtils.createSponsorNode(sps);
                                    Node importedSponsorNode = self.getOwnerDocument()
                                            .importNode(sponsorNode, true);
                                    XmlUtils.cleanNamespaces(importedSponsorNode);

                                    Node refNode = XmlUtils.getSponsorNodeRef(platform.getSelf());
                                    if (refNode != null) {
                                        refNode.getParentNode().insertBefore(importedSponsorNode, refNode);
                                    } else {
                                        XPathUtils.getNode(platform.getSelf(), "./gmi:MI_Platform").appendChild(importedSponsorNode);
                                    }
                                }
                            }
                        }
                        platformNodes.add(platform.getSelf().cloneNode(true));
                    } else {
                        platformNodes.add(XmlUtils.createPlatformNode(platform));
                        log.debug("Non-existing platform " + platform.getLabel());
                    }
                }

                // first remove all existing platforms
                XPathUtils.removeNodes(self, "./gmi:platform");

                // and then add new platforms if any
                if (platformNodes.size() > 0) {
                    Node refNode = XmlUtils.getPlatformNodeRef(self);
                    platformNodes.stream().map((pltf) -> self.getOwnerDocument()
                            .importNode(pltf, true)).map((importedPltf) -> {
                        XmlUtils.cleanNamespaces(importedPltf);
                        return importedPltf;
                    }).forEachOrdered((importedPltf) -> {
                        if (refNode != null) {
                            self.insertBefore(importedPltf, refNode);
                        } else {
                            self.appendChild(importedPltf);
                        }
                    });
                }
            }
        }
    }

    public void addPlatform(Platform platform) {
        if (platforms == null) {
            platforms = new ArrayList<>();
        }
        platforms.add(platform);
    }

//    public void addNoInstrumentPlatform(String plf) {
//        if (noInstrumentPlatforms == null) {
//            noInstrumentPlatforms = new ArrayList<>();
//        }
//        noInstrumentPlatforms.add(plf);
//    }
    public boolean exists(Platform plf) {
        if (platforms == null || platforms.isEmpty()) {
            return false;
        }
        return platforms.contains(plf);
    }
}
