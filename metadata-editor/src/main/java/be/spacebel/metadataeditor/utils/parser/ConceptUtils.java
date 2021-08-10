/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.configuration.VoidDataset;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.HttpInvoker;
import be.spacebel.metadataeditor.utils.jsf.TreeNodeComparator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.util.TreeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class provides utilities to extract skos:Concept (or rdf:Description)
 * from RDF files.
 *
 * @author mng
 */
public class ConceptUtils {

    private final static Logger LOG = Logger.getLogger(ConceptUtils.class);

    public static void loadConcepts(Configuration config, boolean localLoad) throws IOException, SAXException {
        XMLParser xmlParser = new XMLParser();
        boolean reload = false;

        if (config.getVoidDatasets() != null) {
            String earthtopicTopUri = "";
            String earthtopicRootUri = "";
            String platformTopUri = "";
            String platformRootUri = "";

            String dateFormat = "yyyy_MM_dd_hh'h'mm";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            String fileNamePrefix = sdf.format(new Date());
            LOG.debug("File name prefix: " + fileNamePrefix);

            /*
                create backup directory
             */
            Path backupDir = Paths.get(config.getThesaurusDir() + "/backup");
            Files.createDirectories(backupDir);

            for (VoidDataset dataset : config.getVoidDatasets()) {
                List<Concept> conceptList = new ArrayList<>();
                boolean isEsaThesaurus = false;
                boolean isGcmdThesaurus = false;

                boolean isSpatialDataServiceCategoryThesaurus = false;
                if (config.getSpatialDataServiceCategoryThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                    isSpatialDataServiceCategoryThesaurus = true;
                }

                if (config.getEarthtopicsThesaurusUri().equalsIgnoreCase(dataset.getUri())
                        || config.getPlatformThesaurusUri().equalsIgnoreCase(dataset.getUri())
                        || config.getInstrumentThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                    isEsaThesaurus = true;
                }

                if (config.getSckwThesaurusUri().equalsIgnoreCase(dataset.getUri())
                        || config.getGcmdPlatformThesaurusUri().equalsIgnoreCase(dataset.getUri())
                        || config.getGcmdInstrumentThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                    isGcmdThesaurus = true;
                }

                if (!localLoad
                        && (((isEsaThesaurus || isSpatialDataServiceCategoryThesaurus) && config.isRefreshEsaThesaurus()) || (isGcmdThesaurus && config.isRefreshGcmdThesaurus()))
                        && StringUtils.isNotEmpty(dataset.getRemoteDataFile())) {
                    try {
                        /**
                         * Load remote file
                         */
                        String mimeType = null;
                        if (isEsaThesaurus) {
                            mimeType = "application/rdf+xml";
                        }

                        String data = HttpInvoker.readFile(dataset.getRemoteDataFile(), mimeType);
                        if (StringUtils.isNotEmpty(data)) {
                            if (config.getEarthtopicsThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                                LOG.debug("New thesaurus: " + data);
                            }

                            boolean ok = false;
                            if (Constants.RDF_FORMAT_NS.equalsIgnoreCase(dataset.getFeature())) {
                                // parse RDF
                                Document rdfDoc = xmlParser.stream2Document(data);
                                ok = parseRDF(conceptList, rdfDoc, dataset, isSpatialDataServiceCategoryThesaurus);
                            } else {
                                if (Constants.CSV_FORMAT_NS.equalsIgnoreCase(dataset.getFeature())) {
                                    // parse CSV
                                    ok = CSVUtils.parseCsvString(conceptList, data, dataset);
                                }
                            }

                            if (ok && !conceptList.isEmpty()) {
                                /*
                                    backup the current local thesaurus file
                                 */
                                String dataFileFullPath = config.getThesaurusDir() + "/" + dataset.getDataFileName();
                                Path source = Paths.get(dataFileFullPath);

                                Path dest = Paths.get(fileNamePrefix + "_" + dataset.getDataFileName());
                                Files.move(source, backupDir.resolve(dest.getFileName()),
                                        StandardCopyOption.REPLACE_EXISTING);
                                /*
                                    save thesaurus data to local
                                 */
                                FileUtils.writeStringToFile(new File(dataFileFullPath), data, StandardCharsets.UTF_8);
                                LOG.debug("Saved thesaurus data to local " + dataFileFullPath);
                                reload = true;
                            }
                        }
                    } catch (IOException | SAXException e) {
                        LOG.debug(String.format("Error while loading remote data of thesaurus %s: %s", dataset.getUri(), e));
                    }
                }

                if (localLoad) {
                    /**
                     * Load local file
                     */
                    String dataFileFullPath = config.getThesaurusDir() + "/" + dataset.getDataFileName();

                    if (Constants.RDF_FORMAT_NS.equalsIgnoreCase(dataset.getFeature())) {
                        // parse RDF
                        Document rdfDoc = xmlParser.fileToDom(dataFileFullPath);
                        parseRDF(conceptList, rdfDoc, dataset, isSpatialDataServiceCategoryThesaurus);
                    } else {
                        if (Constants.CSV_FORMAT_NS.equalsIgnoreCase(dataset.getFeature())) {
                            // parse CSV
                            CSVUtils.parseCsvFile(conceptList, dataFileFullPath, dataset);
                        }
                    }
                }

                if (!conceptList.isEmpty()) {
                    reload = true;

                    if (config.getEarthtopicsThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                        LOG.debug("Set default EarthTopics");
                        config.setEarthTopics(new ConcurrentHashMap<>());
                        config.setDefaultEarthTopics(new ArrayList<>());

                        for (Concept concept : conceptList) {
                            correctExactMatchUri(concept, config.getSckwThesaurus().getUriSpace());

                            if (config.getEarthTopics().containsKey(concept.getUri())) {
                                Concept existingConcept = config.getEarthTopics().get(concept.getUri());
                                existingConcept.mergeConcept(concept);
                            } else {
                                config.getEarthTopics().put(concept.getUri(), concept);
                            }

                            if (concept.isTopConcept() && StringUtils.isEmpty(earthtopicTopUri)) {
                                earthtopicTopUri = concept.getUri();
                            }

                            if (concept.isRoot() && StringUtils.isEmpty(earthtopicRootUri)) {
                                earthtopicRootUri = concept.getUri();
                            }

                            //LOG.debug("Size = " + config.getDefaultEarthTopics().size());
                            // LOG.debug("Top search records: " + config.getLuceneTopSearchRecords());
//                            if (config.getDefaultEarthTopics().size() < config.getLuceneTopSearchRecords()) {
//                                String value = concept.getUri() + Constants.SEPARATOR + concept.getLabel();
//                                //   LOG.debug("Default EarthTopic value " + value);
//                                config.getDefaultEarthTopics().add(new ParameterOption(value, concept.getLabel()));
//                            }
                        }
                    }

                    if (config.getPlatformThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                        config.setPlatforms(new ConcurrentHashMap<>());
                        addConcepts(config.getPlatforms(), conceptList);
                    }

                    if (config.getInstrumentThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                        config.setInstruments(new ConcurrentHashMap<>());
                        addConcepts(config.getInstruments(), conceptList);
                    }

                    if (config.getSckwThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                        config.setGcmdSciencekeywords(new ConcurrentHashMap<>());
                        addConcepts(config.getGcmdSciencekeywords(), conceptList);
                    }

                    if (config.getGcmdPlatformThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                        config.setGcmdPlatforms(new ConcurrentHashMap<>());
                        addConcepts(config.getGcmdPlatforms(), conceptList);
                    }

                    if (config.getGcmdInstrumentThesaurusUri().equalsIgnoreCase(dataset.getUri())) {
                        config.setGcmdInstruments(new ConcurrentHashMap<>());
                        addConcepts(config.getGcmdInstruments(), conceptList);
                    }

                    if (config.getSpatialDataServiceCategoryThesaurusUri()
                            .equalsIgnoreCase(dataset.getUri())) {
                        config.setSpatialDataServiceCategories(new ConcurrentHashMap<>());
                        conceptList.forEach((concept) -> {
                            config.getSpatialDataServiceCategories().putIfAbsent(concept.getUri(), concept.getLabel());
                        });
                    }

                }
            }

            /**
             * Load all relations
             */
            if (reload) {
                LOG.debug("Reloading concepts relations...........");
                if (config.getInstruments() != null && !config.getInstruments().isEmpty()) {
                    LOG.debug("Instrument relations");
                    for (Concept concept : config.getInstruments().values()) {
                        correctExactMatchUri(concept, config.getGcmdInstrumentThesaurus().getUriSpace());

                        List<String> hostUris = concept.getPropertyValues(Constants.SOSA_ISHOSTEDBY);
                        if (hostUris != null && !hostUris.isEmpty()) {
                            hostUris.stream().map((hostUri) -> config.getPlatforms().get(hostUri)).filter((hostConcept) -> (hostConcept != null)).forEachOrdered((hostConcept) -> {
                                hostConcept.addProperty(Constants.SOSA_HOSTS, concept.getUri());
                                //LOG.debug("Host concept " + hostConcept.toStr());
                            });
                        }
                    }
                }

                if (config.getPlatforms() != null && !config.getPlatforms().isEmpty()) {
                    LOG.debug("Platform relations");
                    config.setDefaultPlatforms(new ArrayList<>());

                    for (Concept concept : config.getPlatforms().values()) {
                        //LOG.debug("Platform concept: " + concept.toStr());

                        if (concept != null && concept.isTopConcept()
                                && StringUtils.isEmpty(platformTopUri)) {
                            platformTopUri = concept.getUri();
                        }

                        if (concept != null && concept.isRoot()
                                && StringUtils.isEmpty(platformRootUri)) {
                            platformRootUri = concept.getUri();
                        }

                        if (concept != null && !concept.isTopConcept()) {
                            correctExactMatchUri(concept, config.getGcmdPlatformThesaurus().getUriSpace());

                            List<String> hostsUris = concept.getPropertyValues(Constants.SOSA_HOSTS);
                            if (hostsUris != null && !hostsUris.isEmpty()) {
                                //LOG.debug("Host URIs: " + hostsUris);
//                                if (config.getDefaultPlatforms().size() < config.getLuceneTopSearchRecords()) {
//                                    String value = concept.getUri() + Constants.SEPARATOR + concept.getLabel();
//                                    config.getDefaultPlatforms().add(new ParameterOption(value, concept.getLabel()));
//                                }
                            } else {
                                // if platform hosts no instrument, get all hosted instrument of its parent

                                List<String> broaderUris = concept.getPropertyValues(Constants.SKOS_BROADER);
                                if (broaderUris != null && !broaderUris.isEmpty()) {
                                    LOG.debug("Broader of " + concept.getUri() + ": " + broaderUris);
                                    for (String parentUri : broaderUris) {
                                        Concept parentConcept = config.getPlatform(parentUri);
                                        if (parentConcept != null) {
                                            List<String> parentHostUris = parentConcept.getPropertyValues(Constants.SOSA_HOSTS);
                                            LOG.debug("Broader hosts: " + parentHostUris);
                                            if (parentHostUris != null && !parentHostUris.isEmpty()) {
                                                concept.addProperty(Constants.SOSA_HOSTS, parentHostUris);
                                            }
                                        }
                                    }
                                }
                            }
//                        } else {
//                            // if the platform hosts no instrument, get all hosted instrument of its children
//                            List<String> narrowerUris = concept.getPropertyValues(Constants.SKOS_NARROWER);
//                            log.debug("Narrowers of " + plfUri + ": " + narrowerUris);
//                            if (narrowerUris != null && !narrowerUris.isEmpty()) {
//                                for (String childUri : narrowerUris) {
//                                    log.debug("Child uri: " + childUri);
//                                    Concept childConcept = concepts.get(childUri);
//                                    if (childConcept != null) {
//                                        List<String> childHostUris = childConcept.getPropertyValues(Constants.SOSA_HOSTS);
//                                        log.debug("Narrower hosts: " + childHostUris);
//                                        if (childHostUris != null && !childHostUris.isEmpty()) {
//                                            concept.addProperty(Constants.SOSA_HOSTS, childHostUris);
//                                        }
//                                    }
//                                }
//                            } else {
//                                // if platform has no children, get all hosted instrument of its parent
//                                List<String> broaderUris = concept.getPropertyValues(Constants.SKOS_BROADER);
//                                if (broaderUris != null && !broaderUris.isEmpty()) {
//                                    log.debug("Broader of " + plfUri + ": " + broaderUris);
//                                    for (String parentUri : broaderUris) {
//                                        Concept parentConcept = concepts.get(parentUri);
//                                        if (parentConcept != null) {
//                                            List<String> parentHostUris = parentConcept.getPropertyValues(Constants.SOSA_HOSTS);
//                                            log.debug("Broader hosts: " + parentHostUris);
//                                            if (parentHostUris != null && !parentHostUris.isEmpty()) {
//                                                concept.addProperty(Constants.SOSA_HOSTS, parentHostUris);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
                        }
                    }
                }

                /*
                index platforms and earth topics
                 */
                //new LuceneUtils(config.getLuceneIndexDir(), config.getLuceneTopSearchRecords()).writeIndex(config.getEarthTopics(), config.getPlatforms());
            }

            /**
             * Build TreeNode
             */
            if (StringUtils.isNotEmpty(earthtopicRootUri)) {
                LOG.debug("Earth Topic root concept URI " + earthtopicRootUri);
                config.setEarthtopicsTreeNode(buildTreeNode(earthtopicTopUri, earthtopicRootUri, config.getEarthTopics()));
            } else {
                LOG.debug("Earth Topic has no root concept");
            }

            if (StringUtils.isNotEmpty(platformRootUri)) {
                LOG.debug("Platform root concept URI " + platformRootUri);
                config.setPlatformTreeNode(buildTreeNode(platformTopUri, platformRootUri, config.getPlatforms()));
            } else {
                LOG.debug("Platform has no root concept");
            }
        }
    }

    private static boolean parseRDF(List<Concept> concepts,
            Document rdfDoc, VoidDataset dataset, boolean isSpatialDataServiceCategoryThesaurus)
            throws IOException, SAXException {
        if (rdfDoc != null) {
            /*
                Find version info
             */
            String version = XPathUtils.getNodeValue(rdfDoc, "./rdf:RDF/gcmd:gcmd/gcmd:keywordVersion");
            if (StringUtils.isEmpty(version)) {
                version = XPathUtils.getNodeValue(rdfDoc, "./rdf:RDF/gcmd_old:gcmd/gcmd_old:keywordVersion");
            }

            String commonXPath = "./rdf:RDF/rdf:Description[@rdf:about='" + dataset.getUri() + "']";
            if (StringUtils.isEmpty(version)) {
                version = XPathUtils.getNodeValue(rdfDoc, commonXPath + "/owl:versionInfo");
            }

            if (StringUtils.isNotEmpty(version)) {
                version = StringUtils.trimToEmpty(version);
            }

            boolean keepGoing = true;
            LOG.debug(String.format("Old version of thesaurus %s is: %s", dataset.getUri(), dataset.getVersion()));
            LOG.debug(String.format("New version of thesaurus %s is: %s", dataset.getUri(), version));
//            if (StringUtils.isNotEmpty(dataset.getVersion())
//                    && dataset.getVersion().equalsIgnoreCase(version)) {
//                keepGoing = false;
//                LOG.debug(String.format("The thesaurus %s has no update", dataset.getUri()));
//            }

            if (keepGoing) {
                LOG.debug("Keep loading concepts of thesaurus " + dataset.getUri());
                if (isSpatialDataServiceCategoryThesaurus) {
                    NodeList descNodes = XPathUtils
                            .getNodes(rdfDoc, "./rdf:RDF/rdf:Description[dcterms:identifier and @rdf:about != '" + dataset.getUri() + "']");
                    if (descNodes != null && descNodes.getLength() > 0) {
                        LOG.debug("Number of Spatial Data Service categories: " + descNodes.getLength());
                        for (int i = 0; i < descNodes.getLength(); i++) {
                            Node descNode = descNodes.item(i);
                            String uri = XmlUtils
                                    .getNodeAttValue(descNode, Constants.RDF_NS, "about");
                            String label = XPathUtils
                                    .getNodeValue(descNode, "./dcterms:identifier");
                            Concept concept = new Concept();
                            concept.setUri(uri);

                            if (StringUtils.isNotEmpty(label)) {
                                concept.setLabel(label);
                                concept.addProperty(Constants.SKOS_PREFLABEL, label);
                            }
                            concepts.add(concept);
                        }
                    }
                } else {
                    /*
                        Find all rdf:Description elements that has skos:prefLabel child
                     */
                    NodeList descNodes = XPathUtils
                            .getNodes(rdfDoc, "./rdf:RDF/rdf:Description");
                    if (descNodes != null && descNodes.getLength() > 0) {
                        LOG.debug("Number of Descriptions: " + descNodes.getLength());
                        extractConceptInfo(concepts, descNodes, dataset.getUri());
                    }

                    /*
                        Find all skos:Concept elements that has skos:prefLabel child
                     */
                    NodeList conceptNodes = XPathUtils
                            .getNodes(rdfDoc, "./rdf:RDF/skos:Concept[skos:prefLabel]");

                    if (conceptNodes != null && conceptNodes.getLength() > 0) {
                        LOG.debug("Number of Concepts: " + conceptNodes.getLength());
                        extractConceptInfo(concepts, conceptNodes, dataset.getUri());
                    }
                }

                if (!concepts.isEmpty()) {
                    if (StringUtils.isNotEmpty(version)) {
                        dataset.setVersion(version);
                    }

                    if (!isSpatialDataServiceCategoryThesaurus) {
                        String title = XPathUtils.getNodeValue(rdfDoc, commonXPath + "/dcterms:title");
                        if (StringUtils.isNotEmpty(title)) {
                            dataset.setTitle(title);
                        }
                    }

                    String modifiedDate = XPathUtils.getNodeValue(rdfDoc, "./rdf:RDF/gcmd:gcmd/gcmd:schemeVersion");
                    if (StringUtils.isEmpty(modifiedDate)) {
                        modifiedDate = XPathUtils.getNodeValue(rdfDoc, "./rdf:RDF/gcmd_old:gcmd/gcmd_old:schemeVersion");
                    }

                    if (StringUtils.isEmpty(modifiedDate)) {
                        modifiedDate = XPathUtils.getNodeValue(rdfDoc, commonXPath + "/dcterms:modified");
                    }

                    if (StringUtils.isEmpty(modifiedDate)) {
                        modifiedDate = XPathUtils.getNodeValue(rdfDoc, commonXPath + "/dcterms:issued");
                    }

                    if (StringUtils.isNotEmpty(modifiedDate)) {
                        if (modifiedDate.length() > 10) {
                            modifiedDate = modifiedDate.substring(0, 10);
                        }
                        dataset.setModified(modifiedDate);
                    }

//                    String label = XPathUtils.getNodeValue(rdfDoc, commonXPath + "/rdfs:label");
//                    if (StringUtils.isNotEmpty(label)) {
//                        dataset.setLabel(label);
//                    }
                    return true;
                } else {
                    LOG.debug(String.format("The new RDF file of thesaurus %s contains no concept", dataset.getUri()));
                }
            }
        }
        return false;
    }

    private static void extractConceptInfo(List<Concept> concepts, NodeList conceptNodes, String thesaurusUri) {
        for (int i = 0; i < conceptNodes.getLength(); i++) {
            Node conceptNode = conceptNodes.item(i);

            List<String> inSchemas = XPathUtils
                    .getAttributeValues(conceptNode, "./skos:inScheme", "resource", Constants.RDF_NS);
            if (inSchemas != null && !inSchemas.isEmpty()
                    && inSchemas.contains(thesaurusUri)) {
                String uri = XmlUtils
                        .getNodeAttValue(conceptNode, Constants.RDF_NS, "about");
                String base = XmlUtils
                        .getNodeAttValue(conceptNode, "xml:base");

                String label = XPathUtils
                        .getNodeValue(conceptNode, "./skos:prefLabel[@xml:lang='en']");
                if (StringUtils.isEmpty(label)) {
                    label = XPathUtils
                            .getNodeValue(conceptNode, "./skos:prefLabel");
                }

                if (StringUtils.isNotEmpty(uri)) {
                    if (StringUtils.isNotEmpty(base)
                            && !StringUtils.startsWithIgnoreCase(uri, base)) {
                        uri = base + uri;
                    }

                    Concept concept = new Concept();

                    //concept.setKey(datasetUri, uri);
                    concept.setUri(uri);

                    if (StringUtils.isNotEmpty(label)) {
                        concept.setLabel(label);
                        concept.addProperty(Constants.SKOS_PREFLABEL, label);
                    }

                    addConceptProperty(concept, conceptNode, Constants.SKOS_NARROWER);
                    addConceptProperty(concept, conceptNode, Constants.SKOS_BROADER);

                    addConceptProperty(concept, conceptNode, Constants.SKOS_EXACTMATCH);
                    addConceptProperty(concept, conceptNode, Constants.SKOS_RELATED);
                    addConceptProperty(concept, conceptNode, Constants.SKOS_RELATEDMATCH);

                    addConceptProperty(concept, conceptNode, Constants.RDFS_SEEALSO);
                    addConceptProperty(concept, conceptNode, Constants.SOSA_ISHOSTEDBY);
                    addConceptProperty(concept, conceptNode, Constants.SOSA_HOSTS);

                    addConceptProperty(concept, conceptNode, Constants.SKOS_INSCHEME);
                    addConceptProperty(concept, conceptNode, Constants.RDF_TYPE);

                    String altLabel = XPathUtils
                            .getNodeValue(conceptNode, "./skos:altLabel[@xml:lang='en']");
                    if (StringUtils.isEmpty(altLabel)) {
                        altLabel = XPathUtils
                                .getNodeValue(conceptNode, "./skos:altLabel");
                    }
                    if (StringUtils.isNotEmpty(altLabel)) {
                        concept.addProperty(Constants.SKOS_ALTLABEL, altLabel);
                    }

                    String gcmdLabel = XPathUtils
                            .getAttributeValue(conceptNode,
                                    "./gcmd:altLabel[@xml:lang='en']", "text", Constants.GCMD_NS);
                    if (StringUtils.isEmpty(gcmdLabel)) {
                        gcmdLabel = XPathUtils
                                .getAttributeValue(conceptNode,
                                        "./gcmd:altLabel", "text", Constants.GCMD_NS);
                    }
                    if (StringUtils.isEmpty(gcmdLabel)) {
                        gcmdLabel = XPathUtils
                                .getAttributeValue(conceptNode,
                                        "./gcmd_old:altLabel[@xml:lang='en']", "text", Constants.GCMD_OLD_NS);
                    }

                    if (StringUtils.isEmpty(gcmdLabel)) {
                        gcmdLabel = XPathUtils
                                .getAttributeValue(conceptNode,
                                        "./gcmd_old:altLabel", "text", Constants.GCMD_OLD_NS);
                    }

                    if (StringUtils.isNotEmpty(gcmdLabel)) {
                        concept.addProperty(Constants.GCMD_ALTLABEL, gcmdLabel);
                    }

                    String definition = XPathUtils
                            .getNodeValue(conceptNode, "./skos:definition[@xml:lang='en']");
                    if (StringUtils.isEmpty(definition)) {
                        definition = XPathUtils
                                .getNodeValue(conceptNode, "./skos:definition");
                    }
                    if (StringUtils.isNotEmpty(definition)) {
                        concept.addProperty(Constants.SKOS_DEFINITION, definition);
                    }

                    String topConcept = XPathUtils.getAttributeValue(conceptNode, "./skos:hasTopConcept", "resource", Constants.SKOS_NS);
                    if (StringUtils.isNotEmpty(topConcept)) {
                        concept.setTopConcept(true);
                    }
                    concepts.add(concept);
                }
            }
        }
    }

    private static void addConcepts(Map<String, Concept> concepts, List<Concept> conceptList) {
        conceptList.forEach((concept) -> {
            if (concepts.containsKey(concept.getUri())) {
                Concept existingConcept = concepts.get(concept.getUri());
                existingConcept.mergeConcept(concept);
            } else {
                concepts.put(concept.getUri(), concept);
            }
        });

    }

    private static void addConceptProperty(Concept concept, Node conceptNode, String proKey) {
        String strXpath = "./" + proKey;
        List<String> values = XPathUtils
                .getAttributeValues(conceptNode, strXpath, "resource", Constants.RDF_NS);
        if (values != null && !values.isEmpty()) {
            concept.addProperty(proKey, values);
        } else {
            if (Constants.SKOS_BROADER.equals(proKey)) {
                concept.setRoot(true);
            }
        }
    }

    private static void correctExactMatchUri(Concept concept, String prefix) {
        prefix = CommonUtils.removeLastSlash(prefix);

        List<String> exactUris = concept.getPropertyValues(Constants.SKOS_EXACTMATCH);
        List<String> newExactUris = new ArrayList<>();
        if (exactUris != null && !exactUris.isEmpty()) {
            for (String exactUri : exactUris) {
                if (!StringUtils.startsWithIgnoreCase(exactUri, prefix)) {
                    exactUri = prefix + "/" + StringUtils.substringAfterLast(exactUri, "/");
                }
                newExactUris.add(exactUri);
            }
            concept.addOverWriteProperty(Constants.SKOS_EXACTMATCH, newExactUris);
        }
    }

    private static TreeNode buildTreeNode(String topConceptUri, String rootConceptUri, Map<String, Concept> concepts) {

//        TreeNode topNode = new DefaultTreeNode();
//        topNode.setSelectable(false);
//
//        if (StringUtils.isNotEmpty(topConceptUri)) {
//            Concept topConcept = concepts.get(topConceptUri);
//            if (topConcept != null) {
//                topNode = new DefaultTreeNode(topConcept);
//                topNode.setSelectable(false);
//            }
//        }
        Concept rootConcept = concepts.get(rootConceptUri);
        if (rootConcept != null) {
            //TreeNode rootNode = new DefaultTreeNode(rootConcept, topNode);
            TreeNode rootNode = new DefaultTreeNode(rootConcept);
            rootNode.setSelectable(false);
            buildTreeNode(rootNode, rootConcept, concepts);
            TreeUtils.sortNode(rootNode, new TreeNodeComparator());
            return rootNode;
        } else {
            LOG.debug("No concept found for the URI " + rootConceptUri);
        }
        return null;
    }

    private static void buildTreeNode(TreeNode rootNode, Concept rootConcept, Map<String, Concept> concepts) {
        List<String> children = rootConcept.getPropertyValues(Constants.SKOS_NARROWER);
        if (children != null && !children.isEmpty()) {
            children.forEach((childUri) -> {
                Concept childConcept = concepts.get(childUri);
                if (childConcept != null) {
                    TreeNode childNode = new DefaultTreeNode(childConcept, rootNode);
                    childNode.setExpanded(false);
                    buildTreeNode(childNode, childConcept, concepts);
                } else {
                    LOG.debug("No concept found for the URI " + childUri);
                }
            });
        }

    }
}
