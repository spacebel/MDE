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
import be.spacebel.metadataeditor.models.user.UserPreferences;
import be.spacebel.metadataeditor.models.workspace.Metadata;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.models.workspace.identification.EarthTopic;
import be.spacebel.metadataeditor.models.workspace.mission.Instrument;
import be.spacebel.metadataeditor.models.workspace.mission.Platform;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.validation.ValidationError;
import be.spacebel.metadataeditor.utils.validation.ValidationStatus;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class acts as an interface to handle different formats of the metadata record by combining with the corresponding handlers
 * @author mng
 */
public class MetadataParser {

    private final Logger LOG = Logger.getLogger(getClass());
    private final XMLParser xmlParser;
    private final Configuration config;
    private final IsoHandler isoHandler;
    private final GeoJsonHandler geoJsonHandler;
    private final Dif10Handler dif10Handler;

    public MetadataParser(Configuration config) {
        this.config = config;
        this.xmlParser = new XMLParser();
        this.xmlParser.setIsNamespaceAware(true);
        this.geoJsonHandler = new GeoJsonHandler(config);
        this.dif10Handler = new Dif10Handler(config);
        this.isoHandler = new IsoHandler(config);
    }

    public MetadataFile buildMetadataFile(Document isoDoc, boolean autoCorrect) throws IOException, ParseException, XPathExpressionException {
        LOG.debug("Build metadata file object from a document");

        Metadata metadata = isoHandler.buildMetadata(isoDoc);
        if (autoCorrect) {
            autoCorrect(metadata);
        }

//        if (metadata.isEarthtopicChanged()
//                || metadata.isScienceKwChanged()
//                || metadata.isEsaInstrumentChanged()) {
//            MetadataUtils.applyThesaurusVersionChange(metadata, config);
//
//            metadata.setEarthtopicChanged(false);
//            metadata.setScienceKwChanged(false);
//            metadata.setEsaInstrumentChanged(false);
//        }
//
//        if (metadata.isHasEarthtopicChangeWarn()
//                || metadata.isHasScienceKwChangeWarn()
//                || metadata.isHasEsaInstrumentChangeWarn()) {
//            MetadataUtils.applyThesaurusConceptChange(metadata, config);
//        }
        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setMetadata(metadata);
        metadataFile.setXmlDoc(isoDoc);

        isoHandler.setXmlSrc(metadataFile);

        JSONObject geoJsonObj = geoJsonHandler.toGeoJSON(isoDoc);
        if (metadata.isService()) {
            JSONObject properties = JsonUtils.getGeoJSONObjectProperty(geoJsonObj, "properties");
            geoJsonHandler.collectMoreInfoFromJson(properties, metadata);
        }
        metadataFile.setJsonObject(geoJsonObj);
        geoJsonHandler.setGeoJsonSrc(metadataFile);
        geoJsonHandler.addIso(metadataFile.getJsonObject(), metadataFile.getXmlSrc());

        if (metadata.isSeries()) {
            metadataFile.setDif10(dif10Handler.toDif10(isoDoc));
        }

        return metadataFile;
    }

    /**
     * Transform the imported metadata record (either from local system or from
     * the catalogue) in ISO format and GeoJson format into MetadataFile object
     *
     * @param isoXmlDocument The metadata record in ISO format
     * @param geoJson The metadata record in GeoJson format
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    public MetadataFile buildImportedRecord(Document isoXmlDocument, String geoJson)
            throws IOException, ParseException, XPathExpressionException {
        Document isoDoc = isoHandler.correctGcmdConceptUri(isoXmlDocument);

        Metadata metadata = new Metadata(config);
        MetadataFile metadataFile = new MetadataFile();

        JSONObject jsonObject;
        JSONObject properties = null;
        if (StringUtils.isNotEmpty(geoJson)) {
            jsonObject = new JSONObject(geoJson);
            properties = JsonUtils.getGeoJSONObjectProperty(jsonObject, "properties");
            geoJsonHandler.collectOfferingsAndAbstract(properties, metadata);
            isoHandler.removeOfferingsfromDistribution(isoDoc, metadata);
        } else {
            jsonObject = geoJsonHandler.toGeoJSON(isoDoc);
        }

        isoHandler.buildMetadata(isoDoc, metadata);
        if (properties != null) {
            geoJsonHandler.collectMoreInfoFromJson(properties, metadata);
        }
        autoCorrect(metadata);
        metadataFile.setJsonObject(jsonObject);

//        if (metadata.isEarthtopicChanged()
//                || metadata.isScienceKwChanged()
//                || metadata.isEsaInstrumentChanged()) {
//            MetadataUtils.applyThesaurusVersionChange(metadata, config);
//
//            metadata.setEarthtopicChanged(false);
//            metadata.setScienceKwChanged(false);
//            metadata.setEsaInstrumentChanged(false);
//        }
//
//        if (metadata.isHasEarthtopicChangeWarn()
//                || metadata.isHasScienceKwChangeWarn()
//                || metadata.isHasEsaInstrumentChangeWarn()) {
//            MetadataUtils.applyThesaurusConceptChange(metadata, config);
//        }
        metadataFile.setMetadata(metadata);
        metadataFile.setXmlDoc(isoDoc);

        isoHandler.setXmlSrc(metadataFile);

        geoJsonHandler.setGeoJsonSrc(metadataFile);
        geoJsonHandler.addIso(metadataFile.getJsonObject(), metadataFile.getXmlSrc());

        if (metadata.isSeries()) {
            metadataFile.setDif10(dif10Handler.toDif10(isoDoc));
        }

        return metadataFile;
    }

    /**
     * Correct automatically the metadata record with controlled vocabulary
     * information
     *
     * @param metadata
     */
    private void autoCorrect(Metadata metadata) throws XPathExpressionException {
        /*
            Correct Earth Topic
         */
        if (metadata.getIdentification() != null
                && metadata.getIdentification().getEarthTopics() != null) {
            List<EarthTopic> newEarthTopics = new ArrayList<>();
            metadata.getIdentification().getEarthTopics().stream().filter((eTopic) -> (eTopic.isEsaEarthTopic())).forEachOrdered((eTopic) -> {
                Concept concept = config.getEarthTopics().get(eTopic.getUri());
                if (concept != null) {
                    newEarthTopics.add(eTopic);
                    eTopic.setLabel(concept.getLabel());
                    /**
                     * Find equivalent GCMD science keywords
                     */
                    List<String> scKwUris = concept
                            .getProperties().get(Constants.SKOS_EXACTMATCH);
                    if (scKwUris != null && !scKwUris.isEmpty()) {
                        scKwUris.forEach((sckwUri) -> {
                            Concept sckwConcept = config.getGcmdScienceKeyword(sckwUri);
                            if (sckwConcept != null) {
                                eTopic.addScienceKeyword(sckwConcept);
                            }
                        });
                    }
                }
            });

            metadata.getIdentification().setEarthTopics(newEarthTopics);
            metadata.getIdentification().setNoMappingScienceKeywords(null);
        }

        /*
            Correct instrument and platform
         */
        if (metadata.getAcquisition() != null
                && metadata.getAcquisition().getPlatforms() != null) {
            for (Platform platform : metadata.getAcquisition().getPlatforms()) {
                if (platform.isEsaPlatform()) {
                    Concept pConcept = config.getPlatform(platform.getUri());
                    if (pConcept != null) {
                        platform.setLabel(pConcept.getLabel());

                        // GCMD
                        if (pConcept.getProperties() != null) {
                            List<String> exactMatchUris = pConcept.getProperties().get(Constants.SKOS_EXACTMATCH);
                            if (exactMatchUris != null && !exactMatchUris.isEmpty()) {
                                for (String uri : exactMatchUris) {
                                    Concept exactMatchConcept = config.getGcmdPlatform(uri);
                                    if (exactMatchConcept != null) {
                                        platform.setGcmd(exactMatchConcept);
                                        break;
                                    }
                                }
                            }
                        }

                        // Instrument
                        if (platform.getInstruments() != null) {
                            for (Instrument inst : platform.getInstruments()) {
                                if (inst.isEsaInstrument()) {
                                    Concept instrConcept = config.getInstrument(inst.getUri());
                                    if (instrConcept != null) {
                                        inst.setLabel(instrConcept.getLabel());

                                        // GCMD
                                        if (instrConcept.getProperties() != null) {
                                            List<String> exactMatchUris = instrConcept.getProperties().get(Constants.SKOS_EXACTMATCH);
                                            if (exactMatchUris != null && !exactMatchUris.isEmpty()) {
                                                for (String uri : exactMatchUris) {
                                                    Concept exactMatchConcept = config.getGcmdInstrument(uri);
                                                    if (exactMatchConcept != null) {
                                                        inst.setGcmd(exactMatchConcept);
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
        metadata.update();
    }

    public MetadataFile loadInternalMetedataFile(String metadataJsonFilePath, UserPreferences userPreferences)
            throws IOException, ParseException, SAXException {
        LOG.debug("Load internal metadata record file " + metadataJsonFilePath);

        String json = FileUtils.readFileToString(new File(metadataJsonFilePath), StandardCharsets.UTF_8.name());
        JSONObject jsonObject = new JSONObject(json);
        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(jsonObject, "properties");

        String isoXml = JsonUtils.getGeoJSONStringProperty(properties, "supplementalInformation");
        if (StringUtils.isNotEmpty(isoXml)) {
            Metadata metadata = new Metadata(config);
            geoJsonHandler.collectOfferingsAndAbstract(properties, metadata);

            Document xmlDoc = isoHandler.buildMetadata(isoXml, metadata);
//            if (metadata.hasOfferingOperation()) {
//                isoHandler.offeringsToDistribution(xmlDoc, metadata);
//            }

            geoJsonHandler.collectMoreInfoFromJson(properties, metadata);

            MetadataFile metadataFile = new MetadataFile();
            metadataFile.setMetadata(metadata);
            metadataFile.setJsonObject(jsonObject);
            metadataFile.setXmlDoc(xmlDoc);
            isoHandler.setXmlSrc(metadataFile);
            geoJsonHandler.setGeoJsonSrc(metadataFile);
            if (metadata.isSeries()) {
                metadataFile.setDif10(dif10Handler.toDif10(xmlDoc));
            }
            metadataFile.setFileName(FilenameUtils.getName(metadataJsonFilePath));

            // check if version of thesauri are up to date 
            checkThesauriVersionChange(metadataFile);

            validate(metadataFile, userPreferences);
            return metadataFile;
        } else {
            String errorMsg = "No ISO XML found in the internal metadata record file " + metadataJsonFilePath;
            LOG.debug(errorMsg);
            throw new IOException();
        }
    }

    private void checkThesauriVersionChange(MetadataFile metadataFile) {
        Document xmlDoc = metadataFile.getXmlDoc();
        Metadata metadata = metadataFile.getMetadata();
        if (metadata.getIdentification() != null
                && metadata.getIdentification().getEarthTopics() != null
                && metadata.getIdentification().getEarthTopics().size() > 0) {
            if (metadata.isSeries()) {
                metadata.setEarthtopicChanged(isThesaurusVersionChanged(xmlDoc,
                        config.getEarthtopicsThesaurusUri(),
                        config.getEarthtopicThesaurus().getFullTitle(),
                        config.getEarthtopicThesaurus().getModified()));

                metadata.setScienceKwChanged(isThesaurusVersionChanged(xmlDoc,
                        config.getSckwThesaurusUri(),
                        config.getSckwThesaurus().getFullTitle(),
                        config.getSckwThesaurus().getModified()));
            } else {
                // don't know how to check because the service metadata record has no gmx:Anchor
            }
        }

        if (metadata.getAcquisition() != null
                && metadata.getAcquisition().getPlatforms() != null
                && metadata.getAcquisition().getPlatforms().size() > 0) {
            metadata.setEsaInstrumentChanged(isThesaurusVersionChanged(xmlDoc,
                    config.getInstrumentThesaurusUri(),
                    config.getInstrumentThesaurus().getFullTitle(),
                    config.getInstrumentThesaurus().getModified()));
        }

    }

    private boolean isThesaurusVersionChanged(Document xmlDoc,
            String thesaurusTitleUri, String thesaurusTitle, String thesaurusDate) {
        Node thesaurusCitation = XPathUtils.getNode(xmlDoc,
                "./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation[./gmd:title/gmx:Anchor/@xlink:href='"
                + thesaurusTitleUri + "']");
        if (thesaurusCitation != null) {
            String date = XPathUtils.getNodeValue(thesaurusCitation, "./gmd:date/gmd:CI_Date/gmd:date/gco:Date");
            if (StringUtils.isNotEmpty(date)
                    && !thesaurusDate.equals(date)) {
                return true;
            } else {
                String title = XPathUtils.getNodeValue(thesaurusCitation, "./gmd:title/gmx:Anchor");
                if (StringUtils.isNotEmpty(title)
                        && !thesaurusTitle.equalsIgnoreCase(title)) {
                    return true;
                }
            }
        }

        return false;
    }

//    private void checkConceptChange(Metadata metadata) {
//        if (metadata.getIdentification() != null
//                && metadata.getIdentification().getEarthTopics() != null
//                && metadata.getIdentification().getEarthTopics().size() > 0) {
//            metadata.getIdentification().getEarthTopics().forEach((eTopic) -> {
//                Concept eConcept = config.getEarthTopics().get(eTopic.getUri());
//                if (eConcept != null) {
//                    if (!eTopic.getLabel().equals(eConcept.getLabel())) {
//                        // Earth Topic label changed
//                        eTopic.setWarning(new AutoCorrectionWarning(metadata.getOthers().getFileIdentifier(),
//                                eTopic.getUri(), eTopic.getLabel(), eConcept.getLabel(), 1, 1));
//                    }
//                } else {
//                    // Earth Topic URI not found
//                    eTopic.setWarning(new AutoCorrectionWarning(metadata.getOthers().getFileIdentifier(),
//                            eTopic.getUri(), eTopic.getLabel(), "", 1, 2));
//                }
//            });
//        }
//
//        if (metadata.getAcquisition() != null
//                && metadata.getAcquisition().getPlatforms() != null
//                && metadata.getAcquisition().getPlatforms().size() > 0) {
//           
//
//        }
//    }
    public MetadataFile cloneTemplateFile(Document isoDoc, String identifier)
            throws IOException, ParseException, SAXException {
        LOG.debug("Clone ISO template with id  = " + identifier);

        isoHandler.updateToClone(isoDoc, identifier);
        Metadata metadata = isoHandler.buildMetadata(isoDoc);

        MetadataFile metadataFile = new MetadataFile();
        metadataFile.setMetadata(metadata);
        metadataFile.setXmlDoc(isoDoc);

        isoHandler.setXmlSrc(metadataFile);

        metadataFile.setJsonObject(geoJsonHandler.toGeoJSON(isoDoc));
        geoJsonHandler.setGeoJsonSrc(metadataFile);
        geoJsonHandler.addIso(metadataFile.getJsonObject(), metadataFile.getXmlSrc());

        if (metadata.isSeries()) {
            metadataFile.setDif10(dif10Handler.toDif10(isoDoc));
        }

        return metadataFile;
    }

    public MetadataFile cloneJsonFile(String metadataJsonFilePath, String identifier)
            throws IOException, ParseException, SAXException {
        LOG.debug("Clone internal metadata record file " + metadataJsonFilePath);

        String json = FileUtils.readFileToString(new File(metadataJsonFilePath), StandardCharsets.UTF_8.name());
        JSONObject jsonObject = new JSONObject(json);
        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(jsonObject, "properties");

        String isoXml = JsonUtils.getGeoJSONStringProperty(properties, "supplementalInformation");
        if (StringUtils.isNotEmpty(isoXml)) {
            Metadata metadata = new Metadata(config);
            geoJsonHandler.collectOfferingsAndAbstract(properties, metadata);

            Document isoDoc = xmlParser.stream2Document(isoXml);
            isoHandler.updateToClone(isoDoc, identifier);
            isoHandler.buildMetadata(isoDoc, metadata);
            geoJsonHandler.collectMoreInfoFromJson(properties, metadata);

            MetadataFile metadataFile = new MetadataFile();
            metadataFile.setMetadata(metadata);
            metadataFile.setJsonObject(jsonObject);
            metadataFile.setXmlDoc(isoDoc);
            isoHandler.setXmlSrc(metadataFile);
            geoJsonHandler.setGeoJsonSrc(metadataFile);

            if (metadata.isSeries()) {
                metadataFile.setDif10(dif10Handler.toDif10(isoDoc));
            }

            return metadataFile;
        } else {
            String errorMsg = "No ISO XML found in the internal metadata record file " + metadataJsonFilePath;
            LOG.debug(errorMsg);
            throw new IOException();
        }
    }

    public void update(String userWorkspaceDir, MetadataFile metadataFile,
            UserPreferences userPreferences)
            throws IOException, SAXException, XPathExpressionException {
        String oldId = XmlUtils.getNodeValue(metadataFile
                .getMetadata().getOthers().getFileIdentifierNode());
        String currentId = metadataFile.getMetadata().getOthers().getFileIdentifier();

        // update ISO      
        metadataFile.getMetadata().update();

        isoHandler.setXmlSrc(metadataFile);

        // update JSON
        geoJsonHandler.updateGeoJson(metadataFile.getJsonObject(), metadataFile.getMetadata());
        geoJsonHandler.setGeoJsonSrc(metadataFile);
        geoJsonHandler.addIso(metadataFile.getJsonObject(), metadataFile.getXmlSrc());

        // update dif10
        if (metadataFile.getMetadata().isSeries()) {
            metadataFile.setDif10(dif10Handler.toDif10(metadataFile.getXmlDoc()));
        }

        String fileName = metadataFile.getFileName();
        if (!oldId.equalsIgnoreCase(currentId)) {
            String oldFilePath = userWorkspaceDir + "/" + fileName;
            LOG.debug("Deleting old metadata record file " + oldFilePath);
            if (FileUtils.deleteQuietly(new File(oldFilePath))) {
                LOG.debug("Deleted");
            }
            fileName = CommonUtils.generateFileName(currentId);
        }

        // save metadata record to the dedicated file
        MetadataUtils.saveFile(metadataFile.getInternalModelSrc(), userWorkspaceDir + "/" + fileName);
        metadataFile.setFileName(fileName);

        resetUnsaved(metadataFile);

        validate(metadataFile, userPreferences);
    }

    public void validate(MetadataFile metadataFile, UserPreferences userPreferences) {
        LOG.debug("Validating the metadata record " + metadataFile.getFlatList().getId());
        ValidationStatus validationStatus = new ValidationStatus(metadataFile.getFlatList().getId());
        metadataFile.setValidationStatus(validationStatus);

        if (metadataFile.getMetadata().isService()) {
            LOG.debug("Validating a service metadata");
            if (userPreferences.isServiceIsoFormat()) {
                LOG.debug("Validating ISO...");
                try {
                    XmlUtils.validateMetadata(metadataFile.getXmlSrc(),
                            config.getIsoServiceSchemaLocation(), 2);
                } catch (ValidationException e) {
                    if (e.getValidationErrors() != null) {
                        e.getValidationErrors().forEach((vError) -> {
                            validationStatus.addMessage(vError);
                        });
                    }
                } catch (IOException | SAXException e) {
                    validationStatus.addMessage(new ValidationError(FacesMessage.SEVERITY_ERROR, e.getMessage(), 1, 2));
                }
            }

            if (userPreferences.isServiceJsonFormat()) {
                LOG.debug("Validating GeoJson...");
                try {
                    geoJsonHandler.validate(metadataFile.getGeoJsonSrc(), true);
                } catch (ValidationException e) {
                    e.getValidationErrors().forEach((vError) -> {
                        validationStatus.addMessage(vError);
                    });
                } catch (IOException | JSONException e) {
                    validationStatus.addMessage(new ValidationError(FacesMessage.SEVERITY_ERROR, e.getMessage(), 2, 2));
                }
            }

        } else {
            LOG.debug("Validating a collection metadata");
            if (userPreferences.isSeriesIsoFormat()) {
                LOG.debug("Validating ISO...");
                try {
                    XmlUtils.validateMetadata(metadataFile.getXmlSrc(),
                            config.getIsoSeriesSchemaLocation(), 1);
                } catch (ValidationException e) {
                    if (e.getValidationErrors() != null) {
                        e.getValidationErrors().forEach((vError) -> {
                            validationStatus.addMessage(vError);
                        });
                    }
                } catch (IOException | SAXException e) {
                    validationStatus.addMessage(new ValidationError(FacesMessage.SEVERITY_ERROR, e.getMessage(), 1, 1));
                }
            }

            if (userPreferences.isSeriesJsonFormat()) {
                LOG.debug("Validating GeoJson...");
                try {
                    geoJsonHandler.validate(metadataFile.getGeoJsonSrc(), false);
                } catch (ValidationException e) {
                    e.getValidationErrors().forEach((vError) -> {
                        validationStatus.addMessage(vError);
                    });
                } catch (IOException | JSONException e) {
                    validationStatus.addMessage(new ValidationError(FacesMessage.SEVERITY_ERROR, e.getMessage(), 2, 1));
                }
            }

            if (userPreferences.isSeriesDif10Format()
                    && metadataFile.getMetadata().isSeries()) {
                try {
                    isoHandler.getDif10Handler().validateDif10(metadataFile.getDif10());
                } catch (ValidationException e) {
                    e.getValidationErrors().forEach((vError) -> {
                        validationStatus.addMessage(vError);
                    });
                } catch (IOException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                    validationStatus.addMessage(new ValidationError(FacesMessage.SEVERITY_ERROR, e.getMessage(), 3, 1));
                } catch (Exception e) {
                    validationStatus.addMessage(new ValidationError(FacesMessage.SEVERITY_ERROR, e.getMessage(), 3, 1));
                }
            }
        }
    }

    public void validate(List<MetadataFile> metadataFiles, UserPreferences userPreferences) {
        if (metadataFiles != null) {
            metadataFiles.forEach((metadataFile) -> {
                validate(metadataFile, userPreferences);
            });
        }
    }

    private void resetUnsaved(MetadataFile metadataFile) {
        metadataFile.setUnsavedOrgTab(false);
        metadataFile.setUnsavedIdTab(false);
        metadataFile.setUnsavedGeoTab(false);
        metadataFile.setUnsavedTempTab(false);
        metadataFile.setUnsavedConstTab(false);
        metadataFile.setUnsavedKwTab(false);
        metadataFile.setUnsavedOfferingTab(false);
        metadataFile.setUnsavedDistTab(false);
        metadataFile.setUnsavedAcquiTab(false);
        metadataFile.setUnsavedOthersTab(false);
    }

    public String getSupplementalInformation(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject properties = JsonUtils.getGeoJSONObjectProperty(jsonObject, "properties");
        return JsonUtils.getGeoJSONStringProperty(properties, "supplementalInformation");
    }

}
