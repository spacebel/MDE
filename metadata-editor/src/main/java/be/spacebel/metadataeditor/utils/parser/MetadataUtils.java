/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.configuration.VoidLinkset;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.workspace.Metadata;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.validation.AutoCorrectionWarning;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * This class implements methods that are used to build ISO-19139 XML metadata
 *
 * @author mng
 */
public class MetadataUtils {

    private final static Logger LOG = Logger.getLogger(MetadataUtils.class);

    public static EarthTopic createEarthTopic(Concept concept, final Configuration config) {
        EarthTopic earthTopic = new EarthTopic();
        earthTopic.setLabel(concept.getLabel());
        earthTopic.setUri(concept.getUri());

        // Find exact match
        if (concept.getProperties() != null) {
            List<String> exactMatchUris = concept
                    .getProperties().get(Constants.SKOS_EXACTMATCH);
            if (exactMatchUris != null && !exactMatchUris.isEmpty()) {
                //List<Concept> exactMatchConcepts = new ArrayList<>();
                exactMatchUris.stream().map((uri)
                        -> config.getGcmdScienceKeyword(uri)).filter((exactMatchConcept)
                        -> (exactMatchConcept != null)).forEachOrdered((exactMatchConcept) -> {
                    earthTopic.addScienceKeyword(exactMatchConcept);
                });
            }

//            List<String> relatedUris = concept
//                    .getProperties().get(Constants.SKOS_RELATED);
//            if (relatedUris != null && !relatedUris.isEmpty()) {
//                List<Concept> relatedConcepts = new ArrayList<>();
//                relatedUris.stream().map((uri)
//                        -> config.getInstrument(uri)).filter((relatedConcept)
//                        -> (relatedConcept != null)).forEachOrdered((relatedConcept) -> {
//                    relatedConcepts.add(relatedConcept);
//                });
//                if (earthTopic.getRelations() == null) {
//                    earthTopic.setRelations(new ConcurrentHashMap<>());
//                }
//                earthTopic.getRelations().putIfAbsent(Constants.SKOS_RELATED, relatedConcepts);
//            }
        }

        return earthTopic;
    }

    public static EarthTopic createEarthTopic(String kwUri, String kwLabel,
            String recordId, Map<String, String> scienceKeywords, final Configuration config, int recordType) {
        EarthTopic earthTopic = new EarthTopic();
        earthTopic.setLabel(kwLabel);
        earthTopic.setUri(kwUri);

        Concept concept = config.getEarthTopics().get(kwUri);
        if (concept != null) {
            if (!concept.getLabel().equals(kwLabel)) {
                // Earth Topic label changed
                earthTopic.setWarning(new AutoCorrectionWarning(recordId,
                        kwUri, kwLabel, concept.getLabel(), 1, config.getEarthtopicThesaurus().getLabel(), 1, recordType));
            }

            if (!scienceKeywords.isEmpty()) {
                List<String> scKwUris = concept
                        .getProperties().get(Constants.SKOS_EXACTMATCH);
                if (scKwUris != null && !scKwUris.isEmpty()) {
                    for (String sckwUri : scKwUris) {
                        Concept sckwConcept = config.getGcmdScienceKeyword(sckwUri);
                        if (sckwConcept != null) {
                            String newLabel = MetadataUtils.buildScKwLabel(sckwConcept);
                            if (scienceKeywords.containsKey(sckwUri)) {
                                String oldLabel = scienceKeywords.get(sckwUri);

                                scienceKeywords.remove(sckwUri);
                                if (MetadataUtils.compareSckwLabels(oldLabel, newLabel)) {
                                    earthTopic.addScienceKeyword(sckwConcept);
                                } else {
                                    earthTopic.addSckWarning(new AutoCorrectionWarning(recordId,
                                            sckwUri, oldLabel, newLabel, 4, config.getSckwThesaurus().getLabel(), 1, recordType));
                                    LOG.debug(String.format("Label of GCMD science keyword %s (exact match of earth topic %s) has been changed from %s to %s", sckwUri, concept.getUri(), oldLabel, newLabel));
                                }
                            }
//                            } else {
//                                earthTopic.addSckWarning(new AutoCorrectionWarning(recordId, sckwUri, "", newLabel, 4, 3));
//                                LOG.debug(String.format("GCMD science keyword %s (exact match of earth topic %s) is not found in the metadata record", sckwUri, concept.getUri()));
//                            }
                        }
                    }
                }
            }
        } else {
            earthTopic.setEsaEarthTopic(false);
            LOG.debug(String.format("Earth Topics keyword %s(%s) does not exist in ESA Earth Topics thesaurus", kwLabel, kwUri));
        }

        return earthTopic;
    }

    public static Platform createPlatform(Concept concept, final Configuration config) {
        Platform platform = new Platform();
        platform.setLabel(concept.getLabel());
        platform.setUri(concept.getUri());

        if (concept.getProperties() != null) {
            List<String> exactMatchUris = concept.getProperties().get(Constants.SKOS_EXACTMATCH);
            if (exactMatchUris != null && !exactMatchUris.isEmpty()) {
                for (String uri : exactMatchUris) {
                    Concept exactMatchConcept = config.getGcmdPlatform(uri);
                    if (exactMatchConcept != null) {
                        platform.setGcmd(exactMatchConcept);
                        break;
                    }
                }
            }

            List<String> hostsUris = concept.getProperties().get(Constants.SOSA_HOSTS);
            if (hostsUris != null && !hostsUris.isEmpty()) {
                hostsUris.stream().map((uri)
                        -> config.getInstrument(uri)).filter((hostsConcept)
                        -> (hostsConcept != null)).forEachOrdered((hostsConcept) -> {
                    if (platform.getAvailableInstruments() == null) {
                        platform.setAvailableInstruments(new ArrayList<>());
                    }
                    platform.getAvailableInstruments().add(createInstrument(hostsConcept, config));
                });
            }
        }
        platform.setLaunchDate(CommonUtils.strToDate(config.getPlatformThesaurus().getModified()));

        return platform;
    }

    public static Instrument createInstrument(Concept concept, final Configuration config) {
        Instrument instrument = new Instrument();
        instrument.setLabel(concept.getLabel());
        instrument.setUri(concept.getUri());

        if (concept.getProperties() != null) {
            List<String> values = concept.getProperties().get(Constants.SKOS_EXACTMATCH);
            if (values != null && !values.isEmpty()) {
                for (String conceptUri : values) {
                    Concept relatedConcept = config.getGcmdInstrument(conceptUri);
                    if (relatedConcept != null) {
                        instrument.setGcmd(relatedConcept);
                        break;
                    }
                }
            }
        }

        LOG.debug("createInstrument " + concept.toStr());

        findInstrumentType(instrument, concept, config);

        return instrument;
    }

    public static void findInstrumentType(Instrument instrument,
            Concept concept, final Configuration config) {
        List<String> broaderURIs = concept.getPropertyValues(Constants.SKOS_BROADER);

        //String conceptKeyPrefix = StringUtils.substringBefore(concept.getKey(), Constants.SEPARATOR) + Constants.SEPARATOR;
        if (!broaderURIs.isEmpty()) {
            instrument.setBroaders(new ArrayList<>());
            broaderURIs.stream().map((broaderUri)
                    -> config.getInstrument(broaderUri)).filter((broaderConcept)
                    -> (broaderConcept != null)).forEachOrdered((broaderConcept) -> {
                instrument.getBroaders().add(broaderConcept);
            });
        }
    }

    public static String getGcmdLabel(Concept concept) {
        String value = concept.getPropertyValue(Constants.CSV_DETAILED_VAR);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }

        value = concept.getPropertyValue(Constants.CSV_VAR_LEVEL3);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }

        value = concept.getPropertyValue(Constants.CSV_VAR_LEVEL2);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }

        value = concept.getPropertyValue(Constants.CSV_VAR_LEVEL1);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }

        value = concept.getPropertyValue(Constants.CSV_TERM);
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }
        return "";
    }

    public static String getCsvConceptLabel(Concept concept) {
        if (concept == null) {
            return "";
        }

        String label = concept.getPropertyValue(Constants.CSV_CATEGORY);

        String value = concept.getPropertyValue(Constants.CSV_TOPIC);
        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNotEmpty(label)) {
                label += " / " + value;
            } else {
                label = value;
            }
        }

        value = concept.getPropertyValue(Constants.CSV_TERM);
        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNotEmpty(label)) {
                label += " / " + value;
            } else {
                label = value;
            }
        }

        value = concept.getPropertyValue(Constants.CSV_VAR_LEVEL1);
        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNotEmpty(label)) {
                label += " / " + value;
            } else {
                label = value;
            }
        }

        value = concept.getPropertyValue(Constants.CSV_VAR_LEVEL2);
        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNotEmpty(label)) {
                label += " / " + value;
            } else {
                label = value;
            }
        }

        value = concept.getPropertyValue(Constants.CSV_VAR_LEVEL3);
        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNotEmpty(label)) {
                label += " / " + value;
            } else {
                label = value;
            }
        }

        value = concept.getPropertyValue(Constants.CSV_DETAILED_VAR);
        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNotEmpty(label)) {
                label += " / " + value;
            } else {
                label = value;
            }
        }

        return label;
    }

    public static String buildScKwLabel(Concept scKwConcept) {
        StringBuilder sb = new StringBuilder();

        String label = scKwConcept.getPropertyValue(Constants.CSV_CATEGORY);
        if (StringUtils.isNotEmpty(label)) {
            sb.append(label);
        } else {
            return sb.toString();
        }

        label = scKwConcept.getPropertyValue(Constants.CSV_TOPIC);
        if (StringUtils.isNotEmpty(label)) {
            sb.append(" > ").append(label);
        } else {
            return sb.toString();
        }

        label = scKwConcept.getPropertyValue(Constants.CSV_TERM);
        if (StringUtils.isNotEmpty(label)) {
            sb.append(" > ").append(label);
        } else {
            return sb.toString();
        }

        label = scKwConcept.getPropertyValue(Constants.CSV_VAR_LEVEL1);
        if (StringUtils.isNotEmpty(label)) {
            sb.append(" > ").append(label);
        } else {
            return sb.toString();
        }

        label = scKwConcept.getPropertyValue(Constants.CSV_VAR_LEVEL2);
        if (StringUtils.isNotEmpty(label)) {
            sb.append(" > ").append(label);
        } else {
            return sb.toString();
        }

        label = scKwConcept.getPropertyValue(Constants.CSV_VAR_LEVEL3);
        if (StringUtils.isNotEmpty(label)) {
            sb.append(" > ").append(label);
        } else {
            return sb.toString();
        }

        label = scKwConcept.getPropertyValue(Constants.CSV_DETAILED_VAR);
        if (StringUtils.isNotEmpty(label)) {
            sb.append(" > ").append(label);
        } else {
            return sb.toString();
        }

        return sb.toString();
    }

    public static boolean compareSckwLabels(String label1, String label2) {
        String[] labels1 = label1.split(">");
        String[] labels2 = label2.split(">");
        if (labels1.length == labels2.length) {
            for (int i = 0; i < labels1.length; i++) {
                String l1 = StringUtils.trimToEmpty(labels1[i]);
                String l2 = StringUtils.trimToEmpty(labels2[i]);
                if (!l1.equals(l2)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String getTargetThesaurusUri(String srcThesaurusUri, List<VoidLinkset> voidLinksets, String linkPredicate) {
        for (VoidLinkset ls : voidLinksets) {
            if (ls.getLinkPredicate().equalsIgnoreCase(linkPredicate)
                    && ls.getSubjectsTarget().equalsIgnoreCase(srcThesaurusUri)) {
                return ls.getObjectsTarget();
            }
            if (ls.getLinkPredicate().equalsIgnoreCase(linkPredicate)
                    && ls.getObjectsTarget().equalsIgnoreCase(srcThesaurusUri)) {
                return ls.getSubjectsTarget();
            }
        }
        return null;
    }

//    public static void mergePlatform(Platform platform, Platform esaPlatform) {
//        if (platform.getGcmd() == null) {
//            platform.setGcmd(esaPlatform.getGcmd());
//        }
//        if (esaPlatform.getAvailableInstruments() != null) {
//            platform.setAvailableInstruments(new ArrayList<>());
//            for (Instrument avInst : esaPlatform.getAvailableInstruments()) {
//                boolean adding = true;
//                if (platform.getInstruments() != null) {
//                    for (Instrument inst : platform.getInstruments()) {
//                        if (avInst.getUri().equals(inst.getUri())) {
//                            adding = false;
//                            break;
//                        }
//                    }
//                }
//                if (adding) {
//                    platform.getAvailableInstruments().add(avInst);
//                }
//            }
//        }
//    }
    public static void updateLastUpdateDate(Node lastUpdateDateNode, String dateStr) {
        // Remove /gco:Date element if it is existing
        XPathUtils.removeNodes(lastUpdateDateNode, "./gco:Date");

        // Remove /gco:DateTime element if it is existing
        XPathUtils.removeNodes(lastUpdateDateNode, "./gco:DateTime");

        Node importedNode = lastUpdateDateNode.getOwnerDocument()
                .importNode(buildDatetimeNode(dateStr), true);

        XmlUtils.cleanNamespaces(importedNode);

        lastUpdateDateNode.appendChild(importedNode);
    }

    public static void applyThesaurusVersionChange(Metadata metadata, final Configuration config) {
        if (metadata.isEarthtopicChanged()) {
            Node thesaurusCitNode = XPathUtils.getNode(metadata.getIdentification().getDataId(),
                    "./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation[gmd:title/gmx:Anchor/@xlink:href='" + config.getEarthtopicsThesaurusUri() + "']");
            if (thesaurusCitNode != null) {
                // update Earth topic title
                XPathUtils.updateNodeValue(thesaurusCitNode, "./gmd:title/gmx:Anchor", config.getEarthtopicThesaurus().getFullTitle());

                // update Earth topic modification date
                XPathUtils.updateNodeValue(thesaurusCitNode, "./gmd:date/gmd:CI_Date/gmd:date/gco:Date", config.getEarthtopicThesaurus().getModified());
            }
        }

        if (metadata.isScienceKwChanged()) {
            Node thesaurusCitNode = XPathUtils.getNode(metadata.getIdentification().getDataId(),
                    "./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation[gmd:title/gmx:Anchor/@xlink:href='" + config.getSckwThesaurusUri() + "' or gmd:title/gmx:Anchor/@xlink:href='" + config.getOldSckwThesaurusUri() + "']");
            if (thesaurusCitNode != null) {
                // update GCMD Science Keyword URI
                XPathUtils.updateAttributeValue(thesaurusCitNode, "./gmd:title/gmx:Anchor", "href", Constants.XLINK_NS, config.getSckwThesaurusUri());

                // update GCMD Science Keyword title
                XPathUtils.updateNodeValue(thesaurusCitNode, "./gmd:title/gmx:Anchor", config.getSckwThesaurus().getFullTitle());

                // update GCMD Science Keyword date
                XPathUtils.updateNodeValue(thesaurusCitNode, "./gmd:date/gmd:CI_Date/gmd:date/gco:Date", config.getSckwThesaurus().getModified());
            }
        }

        if (metadata.isEsaInstrumentChanged()) {
            Node thesaurusCitNode = XPathUtils.getNode(metadata.getIdentification().getDataId(),
                    "./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation[gmd:title/gmx:Anchor/@xlink:href='" + config.getInstrumentThesaurusUri() + "']");
            if (thesaurusCitNode != null) {
                // update Instrument title
                XPathUtils.updateNodeValue(thesaurusCitNode, "./gmd:title/gmx:Anchor", config.getInstrumentThesaurus().getFullTitle());

                // update Instrument modification date
                XPathUtils.updateNodeValue(thesaurusCitNode, "./gmd:date/gmd:CI_Date/gmd:date/gco:Date", config.getInstrumentThesaurus().getModified());
            }
        }
    }

    public static void applyThesaurusConceptChange(Metadata metadata, final Configuration config) {
        if (metadata != null) {
            if (metadata.getIdentification() != null
                    && metadata.getIdentification().getEarthTopics() != null) {
                List<EarthTopic> newEarthTopics = new ArrayList<>();
                for (EarthTopic eTopic : metadata.getIdentification().getEarthTopics()) {
                    LOG.debug("MNG Earth topic: " + eTopic.getUri());
                    Concept concept = config.getEarthTopics().get(eTopic.getUri());
                    if (concept != null) {
                        EarthTopic newETopic = createEarthTopic(concept, config);
                        if (newETopic.getScienceKeywords() != null
                                && !newETopic.getScienceKeywords().isEmpty()) {
                            newEarthTopics.add(createEarthTopic(concept, config));
                        }
                    }
                }
                metadata.getIdentification().setEarthTopics(newEarthTopics);
            }

            if (metadata.getAcquisition() != null
                    && metadata.getAcquisition().getPlatforms() != null) {

                for (Platform platform : metadata.getAcquisition().getPlatforms()) {
                    LOG.debug("Get Plt concept: " + platform.getUri());
                    Concept pConcept = config.getPlatform(platform.getUri());
                    if (pConcept != null) {
                        LOG.debug("Found Plt concept " + pConcept.getLabel());
                        platform.setLabel(pConcept.getLabel());
                        XPathUtils.updateNodeValue(platform.getSelf(),
                                "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:title/gmx:Anchor", pConcept.getLabel());
                        if (pConcept.getProperties() != null) {
                            List<String> exactMatchUris = pConcept.getProperties().get(Constants.SKOS_EXACTMATCH);
                            if (exactMatchUris != null && !exactMatchUris.isEmpty()) {
                                for (String uri : exactMatchUris) {
                                    Concept exactMatchConcept = config.getGcmdPlatform(uri);
                                    if (exactMatchConcept != null) {
                                        platform.setGcmd(exactMatchConcept);
                                        XPathUtils.updateAttributeValue(platform.getSelf(),
                                                "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor",
                                                "href", Constants.XLINK_NS, uri);
                                        XPathUtils.updateNodeValue(platform.getSelf(),
                                                "./gmi:MI_Platform/gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor",
                                                exactMatchConcept.getLabel());
                                        break;
                                    }
                                }
                            }
                        }

                        if (platform.getInstruments() != null) {
                            for (Instrument inst : platform.getInstruments()) {
                                LOG.debug("Get Inst concept: " + inst.getUri());
                                Concept instConcept = config.getInstrument(inst.getUri());
                                if (instConcept != null) {
                                    LOG.debug("Found Inst concept " + instConcept.getLabel());
                                    inst.setLabel(instConcept.getLabel());
                                    XPathUtils.updateNodeValue(inst.getSelf(),
                                            "./gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:title/gmx:Anchor", instConcept.getLabel());

                                    if (instConcept.getProperties() != null) {
                                        List<String> values = instConcept.getProperties().get(Constants.SKOS_EXACTMATCH);
                                        if (values != null && !values.isEmpty()) {
                                            for (String conceptUri : values) {
                                                Concept relatedConcept = config.getGcmdInstrument(conceptUri);
                                                if (relatedConcept != null) {
                                                    inst.setGcmd(relatedConcept);
                                                    XPathUtils.updateAttributeValue(inst.getSelf(),
                                                            "./gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor",
                                                            "href", Constants.XLINK_NS, conceptUri);
                                                    XPathUtils.updateNodeValue(inst.getSelf(),
                                                            "./gmi:MI_Instrument/gmi:citation/gmd:CI_Citation/gmd:alternateTitle/gmx:Anchor",
                                                            relatedConcept.getLabel());
                                                    break;
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
        }
    }

    private static Node buildDatetimeNode(String dateStr) {
        StringBuilder sb = new StringBuilder();
        sb.append("<gco:DateTime xmlns:")
                .append(Constants.GCO_PREFIX).append("=\"").append(Constants.GCO_NS).append("\">")
                .append(dateStr)
                .append("</gco:DateTime>");
        return XmlUtils.buildNode(sb.toString());
    }

    public static void saveFile(String content, String filePath) throws IOException {
        if (StringUtils.isNotEmpty(content)) {
            LOG.debug("Saving metadata to file " + filePath);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(content);
            }
        }
    }
}
