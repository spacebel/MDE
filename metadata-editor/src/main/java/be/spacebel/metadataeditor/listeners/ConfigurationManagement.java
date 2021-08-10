/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.listeners;

import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchParameter;
import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.models.configuration.Thesaurus;
import be.spacebel.metadataeditor.models.configuration.Offering;
import be.spacebel.metadataeditor.models.configuration.OfferingContent;
import be.spacebel.metadataeditor.models.configuration.OfferingOperation;
import be.spacebel.metadataeditor.models.configuration.Organisation;
import be.spacebel.metadataeditor.models.configuration.VoidDataset;
import be.spacebel.metadataeditor.models.user.UserPreferences;
import be.spacebel.metadataeditor.tasks.ThesauriRefreshTask;
import be.spacebel.metadataeditor.utils.parser.CSVUtils;
import be.spacebel.metadataeditor.utils.parser.XMLParser;
import be.spacebel.metadataeditor.utils.parser.XPathUtils;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.catalogue.CatalogueDocumentHandler;
import be.spacebel.metadataeditor.utils.parser.ConceptUtils;
import be.spacebel.metadataeditor.utils.parser.UserUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class implements the ServletContextListener interface to load all
 * necessary configurations when the application initialization process is
 * starting
 *
 * @author mng
 */
@WebListener
public class ConfigurationManagement implements ServletContextListener {

    private final Logger log = Logger.getLogger(getClass());
    private final XMLParser xmlParser = new XMLParser();

    private final String EARTHTOPICS_URI = "https://earth.esa.int/concepts/concept_scheme/earth-topics";
    private final String PLATFORMS_URI = "https://earth.esa.int/concepts/concept_scheme/platforms";
    private final String INSTRUMENTS_URI = "https://earth.esa.int/concepts/concept_scheme/instruments";
    private final String SCIENCE_KW_URI = "https://gcmd.earthdata.nasa.gov/kms/concepts/concept_scheme/sciencekeywords";
    private final String OLD_SCIENCE_KW_URI = "https://gcmdservices.gsfc.nasa.gov/kms/concepts/concept_scheme/sciencekeywords";
    private final String GCMD_PLATFORMS_URI = "https://gcmd.earthdata.nasa.gov/kms/concepts/concept_scheme/platforms";
    private final String GCMD_INSTRUMENTS_URI = "https://gcmd.earthdata.nasa.gov/kms/concepts/concept_scheme/instruments";
    private final String SPATIAL_DATA_SERVICE_CATEGORY_URI = "http://inspire.ec.europa.eu/metadata-codelist/SpatialDataServiceCategory";
    private final String GCMD_CONCEPT_URI = "https://gcmd.earthdata.nasa.gov/kms/concept/";
    private final String OLD_GCMD_CONCEPT_URI = "https://gcmdservices.gsfc.nasa.gov/kms/concept/";
    private final String GCMD_CONCEPT_SCHEME_URI = "https://gcmd.earthdata.nasa.gov/kms/concepts/";
    private final String OLD_GCMD_CONCEPT_SCHEME_URI = "https://gcmdservices.gsfc.nasa.gov/kms/concepts/";

    private Properties applicationProperties;
    private Properties thesaurusSchemeProperties;

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        String configDir = System.getenv("ME_CONFIG_DIR");
        if (StringUtils.isNotEmpty(configDir)) {
            if (!Files.exists(Paths.get(configDir))) {
                configDir = null;
            }
        }

        if (StringUtils.isEmpty(configDir)) {
            configDir = "/config";
            //configDir = "D:/apps/metadata-editor/config";
        }

        String workspaceDir = System.getenv("ME_WORKSPACE_DIR");
        if (StringUtils.isEmpty(workspaceDir)) {
            workspaceDir = "/data";
            //workspaceDir = "D:/apps/metadata-editor/data";
        }
        log.debug("workspaceDir = " + workspaceDir);

        /*
             create working directory if it doesn't exist
         */
        File file = new File(workspaceDir);
        if (!file.exists()) {
            if (file.mkdirs()) {
                log.debug("Created workspace dir = " + workspaceDir);
            } else {
                log.debug("Could not create workspace dir = " + workspaceDir);
            }
        }

        String tempDir = "/editor/zip-files-temp";

        //String tempDir = "D:/apps/metadata-editor/zip-files-temp";
        List<String> errors = new ArrayList<>();
        Configuration configuration = new Configuration(configDir, workspaceDir, tempDir);
        configuration.setErrors(errors);

        /**
         * Load properties files
         */
        try {
            // application.properties
            applicationProperties = CommonUtils.readPropertiesFile(configDir + "/resources/application.properties");
            try {
                thesaurusSchemeProperties = CommonUtils.readPropertiesFile(configDir + "/resources/thesaurusSchemes.properties");
            } catch (IOException e) {
                thesaurusSchemeProperties = null;
            }

            // load settings.properties
            loadSettings(configDir, configuration);

            // eopThesaurus.properties
            setEopThesaurus(configuration, configDir + "/resources/eopThesaurus.properties");

            // restrictionCodes.properties
            Map<String, String> restrictionCodes = new ConcurrentHashMap<>();
            Properties restrictionCodesProps = CommonUtils
                    .readPropertiesFile(configDir + "/resources/restrictionCodes.properties");

            restrictionCodesProps.keySet().stream().map((objKey) -> (String) objKey).forEachOrdered((key) -> {
                restrictionCodes.put(key, restrictionCodesProps.getProperty(key));
            });
            configuration.setRestrictionCodes(restrictionCodes);

            // profiles.properties
            Properties profilesProps = CommonUtils
                    .readPropertiesFile(configDir + "/resources/profiles.properties");
            configuration.setCollectionProfiles(CommonUtils
                    .strToList(profilesProps.getProperty("collection")));
            configuration.setServiceProfiles(CommonUtils
                    .strToList(profilesProps.getProperty("service")));
        } catch (IOException e) {
            log.debug("Error while loading properties file: " + e);
            errors.add(e.getMessage());
        }

        /*
            Load offerings.xml
         */
        try {
            String offeringsFile = configDir + "/xml/offerings.xml";
            loadOfferings(offeringsFile, configuration);
        } catch (IOException | SAXException e) {
            String errorMsg = "Error while parsing offerings.xml: " + e;
            log.debug(errorMsg);
            errors.add(e.getMessage());
        }

        /*
            Load thesaurus.xml
         */
//        try {
//            String thesaurusFile = configDir + "/xml/thesaurus.xml";
//            configuration.setOtherThesaurus(getThesauri(thesaurusFile));
//        } catch (IOException | SAXException e) {
//            String errorMsg = "Error while parsing thesaurus.xml: " + e;
//            log.debug(errorMsg);
//            //errors.add(e.getMessage());
//        }
        /**
         * Load catalogues.xml
         */
        try {
            String cataloguesFile = configDir + "/xml/catalogues.xml";
            configuration.setCataloguesFile(cataloguesFile);
            configuration.setCatalogues(getCatalogues(cataloguesFile));
        } catch (IOException | SAXException e) {
            String errorMsg = "Error while parsing catalogues.xml: " + e;
            log.debug(errorMsg);
            errors.add(e.getMessage());
        }

        /**
         * Load Users Preferences
         */
        String usersFile = configDir + "/users";
        configuration.setUsersFile(usersFile);
        try {
            Map<String, UserPreferences> usersMap = new ConcurrentHashMap<>();
            List<UserPreferences> users = UserUtils.loadUsers(usersFile);
            if (users != null && !users.isEmpty()) {
                users.forEach((user) -> {
                    /**
                     * Load catalogue info
                     */
                    if (user.getCatalogueUrls() != null
                            && user.getCatalogueUrls().length > 0) {
                        for (String catUrl : user.getCatalogueUrls()) {
                            Catalogue cat = configuration.getCatalogue(catUrl);
                            if (cat != null) {
                                user.addCatalogue(cat);
                            }
                        }
                        user.updateCatalogueUrls();
                    }
                    log.debug("user = " + user.debug());
                    usersMap.put(user.getUsername(), user);
                });
            } else {
                log.debug("Use default admin user");
                String adminPass = System.getenv("ME_ADMIN_PASSWORD");
                if (StringUtils.isEmpty(adminPass)) {
                    adminPass = "admin";
                }

                UserPreferences adminUser = new UserPreferences();
                adminUser.setUsername("administrator");
                adminUser.setRole("Administrator");
                String hashingPassword = DigestUtils.md5Hex(adminPass);
                adminUser.setPassword(hashingPassword);

                // add catalogues to the admin user
                if (configuration.getCatalogues() != null) {
                    configuration.getCatalogues().entrySet().forEach((entry) -> {
                        Catalogue cat = new Catalogue(entry.getValue());
                        if (cat.isPublish()) {
                            adminUser.addCatalogue(cat);
                        }
                    });
                    adminUser.updateCatalogueUrls();
                }

                usersMap.put(adminUser.getUsername(), adminUser);

                try {
                    UserUtils.saveUsers(usersFile, new ArrayList<>(usersMap.values()));
                } catch (IOException ex) {
                    String errorMsg = "Error while saving admin user to file " + usersFile + ": " + ex;
                    log.debug("Error: " + errorMsg);
                }
            }
            configuration.setUsers(usersMap);
        } catch (Exception e) {
            String errorMsg = "Error while parsing file " + usersFile + ": " + e;
            log.debug(errorMsg);
            errors.add(e.getMessage());
        }

        configuration.setEarthtopicsThesaurusUri(getThesaurusScheme("earthtopic", EARTHTOPICS_URI));
        configuration.setPlatformThesaurusUri(getThesaurusScheme("esa.platform", PLATFORMS_URI));
        configuration.setInstrumentThesaurusUri(getThesaurusScheme("esa.instrument", INSTRUMENTS_URI));

        configuration.setSckwThesaurusUri(getThesaurusScheme("gcmd.sciencekeyword", SCIENCE_KW_URI));
        configuration.setOldSckwThesaurusUri(getThesaurusScheme("gcmd.sciencekeyword.old", OLD_SCIENCE_KW_URI));

        configuration.setGcmdPlatformThesaurusUri(getThesaurusScheme("gcmd.platform", GCMD_PLATFORMS_URI));
        configuration.setGcmdInstrumentThesaurusUri(getThesaurusScheme("gcmd.instrument", GCMD_INSTRUMENTS_URI));

        configuration.setSpatialDataServiceCategoryThesaurusUri(getThesaurusScheme("spatial.data.service.category", SPATIAL_DATA_SERVICE_CATEGORY_URI));

        configuration.setGcmdConceptUri(getThesaurusScheme("gcmd.concept.uri", GCMD_CONCEPT_URI));
        configuration.setOldGcmdConceptUris(getThesaurusSchemes("gcmd.concept.uri.old", OLD_GCMD_CONCEPT_URI));
        configuration.setGcmdConceptSchemeUri(getThesaurusScheme("gcmd.concept.scheme.uri", GCMD_CONCEPT_SCHEME_URI));
        configuration.setOldGcmdConceptSchemeUris(getThesaurusSchemes("gcmd.concept.scheme.uri.old", OLD_GCMD_CONCEPT_SCHEME_URI));

        // load organisations and topic categories
        try {
            configuration.setOrgMappings(loadOrganisation(configDir));
            configuration.setTopicMappings(loadTopicCategories(configDir));
        } catch (IOException e) {
            String errorMsg = "Error while loading organisations and topic categories: " + e;
            log.error(errorMsg);
            errors.add(e.getMessage());
        }

        String defaultNamespaces = getDefaultNamespaces(configuration.getXmlDir());

        Map<String, OpenSearchParameter> resourceParameters = new ConcurrentHashMap<>();
        Map<String, String> resourceNamespaces = new ConcurrentHashMap<>();

        try {
            loadResourceParameters(configuration.getXmlDir(), resourceParameters, resourceNamespaces);
        } catch (IOException | SAXException e) {
            String errorMsg = "Error while loading resource parameters: " + e;
            log.debug(errorMsg);
            errors.add(errorMsg);
        }

        configuration.setIsoSeriesSchemaLocation(configDir + "/schemas/" + getResource("collection.iso.schema.location"));
        configuration.setJsonSeriesSchemaLocation(configDir + "/schemas/" + getResource("collection.geojson.schema.location"));
        configuration.setIsoServiceSchemaLocation(configDir + "/schemas/" + getResource("service.iso.schema.location"));
        configuration.setJsonServiceSchemaLocation(configDir + "/schemas/" + getResource("service.geojson.schema.location"));

        configuration.setResourceNamespaces(resourceNamespaces);
        configuration.setResourceParameters(resourceParameters);

        configuration.setDateTypeCode(getResource("date.type.codes"));
        configuration.setDateTypeValues(getResources("date.type.values"));

        //System.out.println("Here 4" + StringUtils.join(errors));
        configuration.setQueryableBlacklist(getBlacklist(defaultNamespaces));
        configuration.setUseLimitations(getResources("use.limitations"));
        configuration.setOnlineRsAppProfiles(getResources("online.resource.app.profiles"));
        configuration.setOnlineRsProtocols(getResources("online.resource.protocols"));
        configuration.setOnlineRsFunctions(getResources("online.resource.functions"));
        configuration.setOnlineRSRelatedFields(getResources("online.resource.related.fields"));
        configuration.setRoleCodes(getResources("role.codes"));
        configuration.setProgressCodes(getResources("progress.codes"));
        configuration.setIsoTopicCategories(getResources("iso.topic.categories"));
        configuration.setProcessingLevels(getResources("processing.levels"));
        configuration.setWavelengthInformation(getResources("wavelength.information"));
        configuration.setOrbitType(getResources("orbit.type"));
        configuration.setResolution(getResources("resolution"));

        configuration.setOmitXmlComments(getResourceBoolean("omit.xml.comments"));
        configuration.setReportCatalogMsg(getResourceBoolean("report.catalogue.message"));

        configuration.setDif10ValidatorUrl(getResource("dif10.validator.url"));

        int interval = getResourceInt("thesauri.refresh.interval", 1);
        if (interval <= 0) {
            interval = 1;
        }
        configuration.setThesauriRefreshInterval(interval);
        String refreshTimeStr = getResource("thesauri.refresh.time");
        if (StringUtils.isEmpty(refreshTimeStr)
                || refreshTimeStr.length() != 5 || !refreshTimeStr.contains(":")) {
            refreshTimeStr = "00:00";
        }
        configuration.setThesauriRefreshTime(refreshTimeStr);

        //System.out.println("Here 5" + StringUtils.join(errors));
        configuration.setRowsPerPage(getResourceInt("rows.per.page", 20));
        configuration.setUploadFileLimit(getResourceInt("upload.file.limit", 3));
        configuration.setUploadSizeLimit(getResourceInt("upload.size.limit", 52428800)); // 50MB        

        /**
         * Load thesauri concepts
         */
        try {
            loadConcepts(configuration);
        } catch (IOException | SAXException e) {
            String errorMsg = "Error while loading thesaurus: " + e;
            log.error(errorMsg);
            errors.add(e.getMessage());
        }

        /*
            store the configuration in the Servlet Context
         */
        sce.getServletContext().setAttribute(Constants.ME_CONFIG_ATTR, configuration);
        sce.getServletContext().setAttribute(Constants.ME_WSP_DIR_ATTR, configuration.getWorkspaceDir());

        //System.out.println("Here 6" + StringUtils.join(errors));

        /*
            startup the scheduler
         */
        startupScheduler(configuration);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //System.out.println("contextDestroyed.");

        scheduler.shutdownNow();

        /**
         * Clean all anonymous generated workspace directories before shutting
         * down Tomcat
         */
        String wkDir = (String) sce.getServletContext().getAttribute(Constants.ME_WSP_DIR_ATTR);
        //System.out.println("wkDir" + wkDir);

        if (StringUtils.isNotEmpty(wkDir)) {
            File[] directories = new File(wkDir).listFiles((File file)
                    -> (file.isDirectory() && file.getName().startsWith(Constants.ME_ANONYMOUS_WSP_DIR_PREFIX)));
            if (directories != null) {
                for (File dir : directories) {
                    FileUtils.deleteQuietly(dir);
                    log.debug("Removed dir " + dir.getAbsolutePath());
                }
            }
        }

    }

    private void loadConcepts(Configuration config)
            throws IOException, SAXException {

        String voidFile = config.getThesaurusDir() + "/void.rdf";

        List<VoidDataset> voidDatasets = new ArrayList<>();
        config.setVoidDatasets(voidDatasets);

        log.debug("Load VOID file... " + voidFile);
        Document voidDoc = xmlParser.fileToDom(voidFile);

        if (voidDoc != null) {

            /*
                load all Datasets
             */
            NodeList datasetNodes = XPathUtils.getNodes(voidDoc, "./rdf:RDF/void:Dataset");

            if (datasetNodes != null && datasetNodes.getLength() > 0) {

                log.debug("Number of Datasets: " + datasetNodes.getLength());

                for (int i = 0; i < datasetNodes.getLength(); i++) {
                    Node datasetNode = datasetNodes.item(i);

                    VoidDataset voidDataset = new VoidDataset();

                    String datasetUri = XmlUtils
                            .getNodeAttValue(datasetNode, Constants.RDF_NS, "about");
                    voidDataset.setUri(datasetUri);

                    List<String> dataDumpFiles = XPathUtils.getAttributeValues(datasetNode, "./void:dataDump", "resource", Constants.RDF_NS);
                    if (dataDumpFiles != null && !dataDumpFiles.isEmpty()) {
                        for (String dataFile : dataDumpFiles) {
                            if (StringUtils.startsWithIgnoreCase(dataFile, "file")) {
                                if (dataFile.startsWith("file://")) {
                                    dataFile = StringUtils.substringAfter(dataFile, "file://");
                                }
                                voidDataset.setDataFileName(dataFile);
                            }
                            if (StringUtils.startsWithIgnoreCase(dataFile, "http")) {
                                voidDataset.setRemoteDataFile(dataFile);
                            }
                        }

                        if (StringUtils.isEmpty(voidDataset.getDataFileName())) {
                            if (config.getEarthtopicsThesaurusUri().equalsIgnoreCase(datasetUri)) {
                                voidDataset.setDataFileName("earth-topics.rdf");
                            }
                            if (config.getPlatformThesaurusUri().equalsIgnoreCase(datasetUri)) {
                                voidDataset.setDataFileName("platforms.rdf");
                            }
                            if (config.getInstrumentThesaurusUri().equalsIgnoreCase(datasetUri)) {
                                voidDataset.setDataFileName("instruments.rdf");
                            }
                            if (config.getSckwThesaurusUri().equalsIgnoreCase(datasetUri)) {
                                voidDataset.setDataFileName("sciencekeywords.csv");
                            }
                            if (config.getGcmdPlatformThesaurusUri().equalsIgnoreCase(datasetUri)) {
                                voidDataset.setDataFileName("gcmd_platforms.rdf");
                            }
                            if (config.getGcmdInstrumentThesaurusUri().equalsIgnoreCase(datasetUri)) {
                                voidDataset.setDataFileName("gcmd_instruments.rdf");
                            }
                            if (config.getSpatialDataServiceCategoryThesaurusUri().equalsIgnoreCase(datasetUri)) {
                                voidDataset.setDataFileName("spatial_data_service_category.rdf");
                            }
                        }
                    }

                    String feature = XPathUtils
                            .getAttributeValue(datasetNode, "./void:feature", "resource", Constants.RDF_NS);
                    voidDataset.setFeature(feature);

                    String uriSpace = XPathUtils
                            .getNodeValue(datasetNode, "./void:uriSpace");
                    voidDataset.setUriSpace(uriSpace);

                    voidDataset.setLabel(XPathUtils
                            .getNodeValue(datasetNode, "./rdfs:label"));
                    voidDataset.setTitle(XPathUtils
                            .getNodeValue(datasetNode, "./dcterms:title"));
                    voidDataset.setModified(XPathUtils
                            .getNodeValue(datasetNode, "./dcterms:modified"));

                    if (StringUtils.isNotEmpty(voidDataset.getDataFileName())
                            && StringUtils.isNotEmpty(datasetUri)) {
                        voidDatasets.add(voidDataset);

                        if (config.getEarthtopicsThesaurusUri().equalsIgnoreCase(datasetUri)) {
                            config.setEarthtopicThesaurus(voidDataset);
                        }
                        if (config.getPlatformThesaurusUri().equalsIgnoreCase(datasetUri)) {
                            config.setPlatformThesaurus(voidDataset);
                        }
                        if (config.getInstrumentThesaurusUri().equalsIgnoreCase(datasetUri)) {
                            config.setInstrumentThesaurus(voidDataset);
                        }
                        if (config.getSckwThesaurusUri().equalsIgnoreCase(datasetUri)) {
                            config.setSckwThesaurus(voidDataset);
                        }
                        if (config.getGcmdPlatformThesaurusUri().equalsIgnoreCase(datasetUri)) {
                            config.setGcmdPlatformThesaurus(voidDataset);
                        }
                        if (config.getGcmdInstrumentThesaurusUri().equalsIgnoreCase(datasetUri)) {
                            config.setGcmdInstrumentThesaurus(voidDataset);
                        }
                        if (config.getSpatialDataServiceCategoryThesaurusUri().equalsIgnoreCase(datasetUri)) {
                            config.setSpatialDataServiceCategoryThesaurus(voidDataset);
                        }
                    }

                    log.debug("Dataset = " + voidDataset.debug());
                }

                /**
                 * Load all concepts
                 */
                ConceptUtils.loadConcepts(config, true);
            }

        } else {
            log.error("VOID file is empty ");
        }
    }

    private String getDefaultNamespaces(String xmlDir) {
        StringBuilder sb = new StringBuilder();
        String xslFile = xmlDir + "/" + Constants.OS_PARAMETERS_XML_FILE;
        log.debug("xslFile = " + xmlDir);

        try {
            Document osParamsDoc = xmlParser.fileToDom(xslFile);

            NamedNodeMap atts = osParamsDoc.getDocumentElement().getAttributes();
            if (atts != null) {
                for (int i = 0; i < atts.getLength(); i++) {
                    Node node = atts.item(i);
                    String prefix = node.getNodeName().trim();
                    String ns = node.getNodeValue();
                    if (StringUtils.startsWithIgnoreCase(prefix, "xmlns:")) {
                        sb.append(StringUtils.substringAfter(prefix, ":"));
                        sb.append("=");
                        sb.append(ns);
                        if (i < (atts.getLength() - 1)) {
                            sb.append("\r\n");
                        }
                    }
                }
            }
        } catch (IOException | DOMException | SAXException e) {
            log.error("Error while loading default namespaces: " + e);
        }

        return sb.toString();
    }

    private List<String> getBlacklist(String namespaces) {
        String blacklistParams = getResource("queryable.blacklist");

        log.debug("namespaces: " + namespaces);
        log.debug("blacklist: " + blacklistParams);

        List<String> blacklist = new ArrayList<>();

        try {
            if (StringUtils.isNotEmpty(namespaces) && StringUtils.isNotEmpty(blacklistParams)) {
                /* parse namespaces */
                BufferedReader reader = new BufferedReader(new StringReader(namespaces));
                String line = null;
                Map<String, String> nsMap = new HashMap<>();
                while ((line = reader.readLine()) != null) {
                    log.debug("NS line: " + line);
                    if (line.indexOf("=") > 0) {
                        String prefix = StringUtils.substringBefore(line, "=");
                        log.debug("prefix: " + prefix);
                        String ns = StringUtils.substringAfter(line, "=");
                        log.debug("ns: " + ns);
                        nsMap.put(prefix, ns);
                    }
                }
                /* parse parameters */
                String[] paramList = blacklistParams.split(",");
                for (String param : paramList) {
                    String[] preParam = param.split(":");
                    if (preParam.length == 2) {
                        String blParam = preParam[1] + "#" + nsMap.get(preParam[0]);
                        log.debug("blParam = " + blParam);
                        blacklist.add(blParam);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
        return blacklist;
    }

    private String getResource(String key) {
        return applicationProperties.getProperty(key);
    }

    private List<String> getResources(String key) {
        //System.out.println(key + "=NQMINH=" + getResource(key));
        return CommonUtils.strToList(getResource(key));
    }

    private int getResourceInt(String key, int defaultValue) {
        String value = getResource(key);
        if (StringUtils.isNotEmpty(value)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private boolean getResourceBoolean(String key) {
        String value = getResource(key);
        return (StringUtils.isNotEmpty(value) && value.equalsIgnoreCase("true"));
    }

    private void loadResourceParameters(String xmlDir, Map<String, OpenSearchParameter> resourceParameters,
            Map<String, String> resourceNamespaces) throws IOException, SAXException {

        Document resourceDoc = xmlParser.fileToDom(xmlDir + "/" + Constants.OS_PARAMETERS_XML_FILE);
        resourceNamespaces = XmlUtils.getNamespaces(resourceDoc, true);

        log.debug("********************************************************************");
        log.debug("LOAD RESOURCE PARAMS");
        log.debug("********************************************************************");
        for (Map.Entry<String, String> entry : resourceNamespaces.entrySet()) {
            log.debug(entry.getKey() + "===" + entry.getValue());
        }

        /*
         * get list of parameters
         */
        NodeList params = resourceDoc.getElementsByTagNameNS(Constants.OS_PARAM_NAMESPACE, "Parameter");
        if (params.getLength() > 0) {
            for (int pIdx = 0; pIdx < params.getLength(); pIdx++) {
                Node paramNode = params.item(pIdx);

                String value = XmlUtils.getNodeAttValue(paramNode, "value");
                if (StringUtils.isNotEmpty(value)) {
                    OpenSearchParameter osParam = new OpenSearchParameter();
                    osParam.setValue(value);
                    String prefix = StringUtils.substringBefore(value, ":");
                    String ns = resourceNamespaces.get(prefix);
                    osParam.setNamespace(ns);

                    String token = StringUtils.substringAfter(value, ":");
                    log.debug("prefix = " + prefix + ", token = " + token);
                    osParam.setName(XmlUtils.getNodeAttValue(paramNode, "name"), value);
                    osParam.setLabel(XmlUtils.getNodeAttValue(paramNode, "label"), value);
                    osParam.setType(XmlUtils.getNodeAttValue(paramNode, "type"));
                    osParam.setHelp(XmlUtils.getNodeAttValue(paramNode, "title"));

                    osParam.setOrder(pIdx + 1);

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
                    log.debug("Resource parameters: " + osParam.toString());
                    resourceParameters.put((token + "#" + ns), osParam);
                } else {
                    log.debug("Parameter value of parameter " + pIdx + " is empty.");
                }
            }
        }
    }

    private void loadOfferings(String offeringsFile, Configuration config)
            throws IOException, SAXException {

        Document offeringsDoc = xmlParser.fileToDom(offeringsFile);
        NodeList offeringNodes = XPathUtils.getNodes(offeringsDoc, "./offerings/offering");
        if (offeringNodes != null && offeringNodes.getLength() > 0) {
            for (int i = 0; i < offeringNodes.getLength(); i++) {
                Node offeringNode = offeringNodes.item(i);
                String offeringCode = XPathUtils.getNodeValue(offeringNode, "./code");

                Offering offering = new Offering();
                offering.setCode(offeringCode);
                config.addOffering(offering);

                NodeList operationNodes = XPathUtils.getNodes(offeringNode, "./operations/operation");
                if (operationNodes != null && operationNodes.getLength() > 0) {
                    log.debug(offeringCode + " has operation");
                    for (int operIdx = 0; operIdx < operationNodes.getLength(); operIdx++) {
                        Node operationNode = operationNodes.item(operIdx);
                        String operationCode = XPathUtils.getNodeValue(operationNode, "./code");
                        log.debug("operationCode = " + operationCode);
                        String method = XPathUtils.getNodeValue(operationNode, "./method");
                        String type = XPathUtils.getNodeValue(operationNode, "./type");
                        String protocol = XPathUtils.getNodeValue(operationNode, "./protocol");
                        String function = XPathUtils.getNodeValue(operationNode, "./function");
                        String serviceType = XPathUtils.getNodeValue(operationNode, "./serviceType");
                        config.addServiceType(serviceType);

                        String additionalFields = XPathUtils.getNodeValue(operationNode, "./additionalFields");

                        if (StringUtils.isNotEmpty(operationCode)) {
                            OfferingOperation operation = new OfferingOperation(operationCode, method, type, protocol, function, serviceType);
                            if (StringUtils.isNotEmpty(additionalFields)) {
                                List<ParameterOption> requiredFields = new ArrayList<>();
                                List<ParameterOption> optionalFields = new ArrayList<>();
                                collectAdditionalFields(additionalFields, requiredFields, optionalFields);
                                operation.setRequiredExtFields(requiredFields);
                                operation.setOptionalExtFields(optionalFields);
                            }
                            offering.addAvailableOperation(operation);
                            config.addProtocolOffering(protocol, offeringCode);
                        }
                    }
                }

                NodeList contentNodes = XPathUtils.getNodes(offeringNode, "./contents/content");
                if (contentNodes != null && contentNodes.getLength() > 0) {
                    for (int contentIdx = 0; contentIdx < contentNodes.getLength(); contentIdx++) {
                        Node contentNode = contentNodes.item(contentIdx);
                        String type = XPathUtils.getNodeValue(contentNode, "./type");

                        String protocol = XPathUtils.getNodeValue(contentNode, "./protocol");
                        String function = XPathUtils.getNodeValue(contentNode, "./function");
                        String serviceType = XPathUtils.getNodeValue(contentNode, "./serviceType");

                        String additionalFields = XPathUtils.getNodeValue(contentNode, "./additionalFields");

                        OfferingContent content = new OfferingContent(type, protocol, function, serviceType);
                        if (StringUtils.isNotEmpty(additionalFields)) {
                            List<ParameterOption> requiredFields = new ArrayList<>();
                            List<ParameterOption> optionalFields = new ArrayList<>();
                            collectAdditionalFields(additionalFields, requiredFields, optionalFields);
                            content.setRequiredExtFields(requiredFields);
                            content.setOptionalExtFields(optionalFields);
                        }
                        String key = String.format("%03d", (contentIdx + 1));
                        offering.addAvailableContent(key, content);
                    }
                }
            }
        }
    }

    private void collectAdditionalFields(String additionalFields,
            List<ParameterOption> requiredFields, List<ParameterOption> optionalFields) {
        String[] fields = additionalFields.split(",");
        for (String field : fields) {
            ParameterOption addField = new ParameterOption();
            if (field.contains("*")) {
                field = field.replaceAll("\\*", "");
                requiredFields.add(addField);
            } else {
                optionalFields.add(addField);
            }
            addField.setLabel(field);
        }
    }

    private Map<String, Catalogue> getCatalogues(String cataloguesFile)
            throws IOException, SAXException {

        Map<String, Catalogue> catalogues = new ConcurrentHashMap<>();
        CatalogueDocumentHandler catDocHandler = new CatalogueDocumentHandler();

        File file = new File(cataloguesFile);
        if (file.exists()) {
            Document cataloguesDoc = xmlParser.fileToDom(cataloguesFile);
            NodeList catNodes = XPathUtils.getNodes(cataloguesDoc, "./catalogues/catalogue");
            if (catNodes != null && catNodes.getLength() > 0) {
                for (int i = 0; i < catNodes.getLength(); i++) {
                    Node catNode = catNodes.item(i);

                    String catalogueUrl = XPathUtils.getNodeValue(catNode, "./url");
                    boolean open = false;

                    String pub = XmlUtils.getNodeAttValue(catNode, "public");
                    if (StringUtils.isNotEmpty(pub)) {
                        open = Boolean.parseBoolean(pub);
                    }
                    try {
                        Catalogue catalogue = catDocHandler.getCatalogue(catalogueUrl, null);
                        catalogue.setPublish(open);
                        catalogues.putIfAbsent(catalogue.getServerUrl(), catalogue);
                    } catch (IOException e) {
                        Catalogue catalogue = new Catalogue();
                        catalogue.setLandingUrl(catalogueUrl);
                        catalogue.setPublish(open);
                        catalogues.putIfAbsent(catalogue.getServerUrl(), catalogue);
                        log.debug("Error while accessing to the catalogue " + catalogueUrl + ": " + e);
                    }
                }
            }
        }

        if (catalogues.isEmpty()) {
            String catalogueUrl = System.getenv("ME_CATALOGUE_URL");
            if (StringUtils.isNotEmpty(catalogueUrl)) {
                Catalogue catalogue = catDocHandler.getCatalogue(catalogueUrl, null);
                catalogue.setPublish(true);
                catalogues.putIfAbsent(catalogue.getServerUrl(), catalogue);

                List<Catalogue> catalogueList = new ArrayList<>();
                catalogueList.add(catalogue);

                // update the catalogues.xml file
                XmlUtils.cataloguesToFile(cataloguesFile, catalogueList);
            }
        }

        return catalogues;
    }

    private Map<String, Organisation> loadOrganisation(String configDir) throws IOException {
        String providersFilePath = configDir + "/resources/providers.csv";
        String organisationsFilePath = configDir + "/resources/organisations.csv";
        File providers = new File(providersFilePath);
        File organisations = new File(organisationsFilePath);

        if (providers.exists() && organisations.exists()) {
            return CSVUtils
                    .loadOrganisationMapping(providersFilePath,
                            organisationsFilePath);
        }
        return null;
    }

    private Map<String, String> loadTopicCategories(String configDir) throws IOException {
        String topicCategoriesFilePath = configDir + "/resources/topiccategories.csv";

        if (new File(topicCategoriesFilePath).exists()) {
            return CSVUtils.loadTopics(topicCategoriesFilePath);
        }
        return null;
    }

    private void loadSettings(String configDir, Configuration configuration)
            throws IOException {
        Properties settingProps = CommonUtils.readPropertiesFile(configDir + "/resources/settings.properties");

        String rfEsaThesaurus = settingProps.getProperty("refresh.esa.thesaurus", "true");
        configuration.setRefreshEsaThesaurus(Boolean.parseBoolean(rfEsaThesaurus));

        String rfGcmdThesaurus = settingProps.getProperty("refresh.gcmd.thesaurus", "true");
        configuration.setRefreshGcmdThesaurus(Boolean.parseBoolean(rfGcmdThesaurus));

    }

    private void setEopThesaurus(Configuration configuration, String eopThesaurusPropFile) throws IOException {

        Properties thesaurusProps = CommonUtils.readPropertiesFile(eopThesaurusPropFile);
        /*
                get EOP thesaurus
         */
        String keywordType = thesaurusProps.getProperty("eop.keywordType.codeList");
        String title = thesaurusProps.getProperty("eop.title");
        String titleUri = thesaurusProps.getProperty("eop.title.uri");
        String date = thesaurusProps.getProperty("eop.date");
        String dateTypeCodeList = thesaurusProps.getProperty("eop.date.type.codeList");
        String dateTypeCodeListValue = thesaurusProps.getProperty("eop.date.type.codeListValue");
        String dateType = thesaurusProps.getProperty("eop.date.type");

        Thesaurus eopThesaurus = new Thesaurus(keywordType, title, titleUri, date, dateTypeCodeList, dateTypeCodeListValue, dateType);
        configuration.setEopThesaurus(eopThesaurus);

        log.debug("EOP thesaurus: " + eopThesaurus.debug());

        /*
                get EOP Extension thesaurus
         */
        keywordType = thesaurusProps.getProperty("eopext.keywordType.codeList");
        title = thesaurusProps.getProperty("eopext.title");
        titleUri = thesaurusProps.getProperty("eopext.title.uri");
        date = thesaurusProps.getProperty("eopext.date");
        dateTypeCodeList = thesaurusProps.getProperty("eopext.date.type.codeList");
        dateTypeCodeListValue = thesaurusProps.getProperty("eopext.date.type.codeListValue");
        dateType = thesaurusProps.getProperty("eopext.date.type");

        eopThesaurus = new Thesaurus(keywordType, title, titleUri, date, dateTypeCodeList, dateTypeCodeListValue, dateType);

        log.debug("EOP Ext thesaurus: " + eopThesaurus.debug());
        configuration.setEopExtThesaurus(eopThesaurus);

    }

    private String getThesaurusScheme(String key, String defaultValue) {
        if (thesaurusSchemeProperties != null) {
            String value = thesaurusSchemeProperties.getProperty(key);
            if (StringUtils.isNotEmpty(value)) {
                return value;
            } else {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private List<String> getThesaurusSchemes(String key, String defaultValue) {
        List<String> defaultList = new ArrayList<>();
        defaultList.add(defaultValue);
        if (thesaurusSchemeProperties != null) {
            String value = thesaurusSchemeProperties.getProperty(key);
            if (StringUtils.isNotEmpty(value)) {
                return CommonUtils.strToList(value);
            } else {
                return defaultList;
            }
        }
        return defaultList;
    }

    private void startupScheduler(Configuration config) {
        log.debug("Start up thesauri refresh scheduler");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'");
        String refreshDateTime = formatter.format(new Date()) + config.getThesauriRefreshTime() + ":00";
        log.debug("Refresh date time = " + refreshDateTime);

        log.debug("Now = " + LocalDateTime.now());
        
        long delay = LocalDateTime.now().until(LocalDateTime.parse(refreshDateTime), ChronoUnit.MINUTES);
        log.debug("Delay = " + delay);

        if (delay < 0) {
            delay = LocalDateTime.now().until(LocalDateTime.parse(refreshDateTime).plusDays(1), ChronoUnit.MINUTES);
            log.debug("New delay = " + delay);
        }

        long period = TimeUnit.DAYS.toMinutes(config.getThesauriRefreshInterval());
        log.debug("period = " + period);
        ThesauriRefreshTask task = new ThesauriRefreshTask(config);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(task, delay, period, TimeUnit.MINUTES);
    }
}
