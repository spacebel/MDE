/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.configuration.Organisation;
import be.spacebel.metadataeditor.models.configuration.VoidDataset;
import be.spacebel.metadataeditor.utils.CommonUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * CSV parser utilities
 *
 * @author mng
 */
public class CSVUtils {

    private final static Logger LOG = Logger.getLogger(CSVUtils.class);

    public static boolean parseCsvString(List<Concept> concepts,
            String csvStr, VoidDataset dataset) throws IOException {
        return parseCSV(concepts, new ByteArrayInputStream(csvStr.getBytes(StandardCharsets.UTF_8.name())), dataset);
    }

    public static boolean parseCsvFile(List<Concept> concepts,
            String csvFile, VoidDataset dataset) throws IOException {
        File file = new File(csvFile);
        if (file.exists()) {
            return parseCSV(concepts, new FileInputStream(file), dataset);
        }
        return false;
    }

    private static boolean parseCSV(List<Concept> concepts,
            InputStream input, VoidDataset dataset) throws IOException {

        try (
                //Reader reader = Files.newBufferedReader(Paths.get(filePath));
                Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8.name()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        //.withFirstRecordAsHeader()
                        //.withIgnoreHeaderCase()
                        .withIgnoreEmptyLines()
                        .withTrim());) {
            /*
                    skip the lines until find “Category” in the first column
             */
            int categoryIndex = -1;
            int topicIndex = -1;
            int termIndex = -1;
            int varLevel1Index = -1;
            int varLevel2Index = -1;
            int varLevel3Index = -1;
            int detailedVarIndex = -1;
            int uuidIndex = -1;

            String version = "";
            String modifiedDate = "";

            for (CSVRecord csvRecord : csvParser) {

                for (int i = 0; i < csvRecord.size(); i++) {
                    String colContent = getValue(csvRecord, i);
                    if (StringUtils.isNotEmpty(colContent)) {
                        if (StringUtils.startsWithIgnoreCase(colContent, "Keyword Version:")) {
                            version = StringUtils.substringAfter(colContent, "Keyword Version:");
                            if (StringUtils.isNotEmpty(version)) {
                                version = StringUtils.trimToEmpty(version);
                            }
                            //dataset.setVersion(version);
                        }

                        if (StringUtils.startsWithIgnoreCase(colContent, "Revision:")) {
                            modifiedDate = StringUtils.substringAfter(colContent, "Revision:");
                            if (StringUtils.isNotEmpty(modifiedDate)) {
                                modifiedDate = StringUtils.trimToEmpty(modifiedDate);
                            }

                            if (StringUtils.isNotEmpty(modifiedDate) && modifiedDate.length() >= 10) {
                                modifiedDate = modifiedDate.substring(0, 10);
                            }
                            //dataset.setModified(modifiedDate);
                        }

                        if ("Category".equalsIgnoreCase(colContent)) {
                            categoryIndex = i;
                        }
                        if ("Topic".equalsIgnoreCase(colContent)) {
                            topicIndex = i;
                        }
                        if ("Term".equalsIgnoreCase(colContent)) {
                            termIndex = i;
                        }
                        if ("Variable_Level_1".equalsIgnoreCase(colContent)) {
                            varLevel1Index = i;
                        }
                        if ("Variable_Level_2".equalsIgnoreCase(colContent)) {
                            varLevel2Index = i;
                        }
                        if ("Variable_Level_3".equalsIgnoreCase(colContent)) {
                            varLevel3Index = i;
                        }
                        if ("Detailed_Variable".equalsIgnoreCase(colContent)) {
                            detailedVarIndex = i;
                        }
                        if ("UUID".equalsIgnoreCase(colContent)) {
                            uuidIndex = i;
                        }
                    }
                }
                if (uuidIndex > -1) {
                    break;
                }
            }

            LOG.debug(String.format("Old version of thesaurus %s is: %s", dataset.getUri(), dataset.getVersion()));
            LOG.debug(String.format("New version of thesaurus %s is: %s", dataset.getUri(), version));

            if (StringUtils.isNotEmpty(dataset.getVersion())
                    && dataset.getVersion().equalsIgnoreCase(version)) {
                LOG.debug(String.format("The thesaurus %s has no update", dataset.getUri()));
            } else {
                LOG.debug(String.format("The thesaurus %s has modification", dataset.getUri()));
            }

            for (CSVRecord csvRecord : csvParser) {
                String uuid = getValue(csvRecord, uuidIndex);
                if (StringUtils.isNotEmpty(uuid)) {

                    Concept concept = new Concept();

                    String conceptUri = CommonUtils.removeLastSlash(dataset.getUriSpace()) + "/" + uuid;
                    concept.setUri(conceptUri);

                    concept.setCsv(true);

                    concept.addProperty(Constants.CSV_CATEGORY, getValue(csvRecord, categoryIndex));
                    concept.addProperty(Constants.CSV_TOPIC, getValue(csvRecord, topicIndex));
                    concept.addProperty(Constants.CSV_TERM, getValue(csvRecord, termIndex));

                    concept.addProperty(Constants.CSV_VAR_LEVEL1, getValue(csvRecord, varLevel1Index));
                    concept.addProperty(Constants.CSV_VAR_LEVEL2, getValue(csvRecord, varLevel2Index));
                    concept.addProperty(Constants.CSV_VAR_LEVEL3, getValue(csvRecord, varLevel3Index));
                    concept.addProperty(Constants.CSV_DETAILED_VAR, getValue(csvRecord, detailedVarIndex));
                    concept.setLabel(MetadataUtils.getGcmdLabel(concept));

                    concepts.add(concept);
                }
            }

            if (!concepts.isEmpty()) {
                if (StringUtils.isNotEmpty(version)) {
                    dataset.setVersion(version);
                }
                if (StringUtils.isNotEmpty(modifiedDate)) {
                    dataset.setModified(modifiedDate);
                }
                return true;
            } else {
                LOG.debug(String.format("The new CSV file of thesaurus %s contains no concept", dataset.getUri()));
            }

//                String title = dataset.getTitle();
//                if (StringUtils.isNotEmpty(dataset.getVersion())) {
//                    title = title.replace("#{VERSION}", "Version " + dataset.getVersion());
//                } else {
//                    title = title.replace("#{VERSION}", "");
//                }
//                dataset.setTitle(title);
        }

//        try (
//                //Reader reader = Files.newBufferedReader(Paths.get(filePath));
//                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"));
//                org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(reader, CSVFormat.DEFAULT
//                        .withFirstRecordAsHeader()
//                        .withIgnoreHeaderCase()
//                        .withIgnoreEmptyLines()
//                        .withTrim());) {
//            for (CSVRecord csvRecord : csvParser) {
//                
//                String uuid = getValue(csvRecord, "UUID");
//                if (StringUtils.isNotEmpty(uuid)) {
//                    
//                    Concept concept = new Concept();
//                    
//                    String conceptUri = CommonUtils.removeLastSlash(dataset.getUriSpace()) + "/" + uuid;
//                    concept.setUri(conceptUri);
//                    
//                    concept.setCsv(true);
//                    
//                    concept.addProperty(Constants.CSV_CATEGORY, getValue(csvRecord, "Category"));
//                    concept.addProperty(Constants.CSV_TOPIC, getValue(csvRecord, "Topic"));
//                    concept.addProperty(Constants.CSV_TERM, getValue(csvRecord, "Term"));
//                    
//                    concept.addProperty(Constants.CSV_VAR_LEVEL1, getValue(csvRecord, "Variable_Level_1"));
//                    concept.addProperty(Constants.CSV_VAR_LEVEL2, getValue(csvRecord, "Variable_Level_2"));
//                    concept.addProperty(Constants.CSV_VAR_LEVEL3, getValue(csvRecord, "Variable_Level_3"));
//                    concept.addProperty(Constants.CSV_DETAILED_VAR, getValue(csvRecord, "Detailed_Variable"));
//                    concept.setLabel(MetadataUtils.getGcmdLabel(concept));
//
//                    /*
//                    Insert or merge the concept into the concept list
//                     */
//                    if (concepts.containsKey(concept.getUri())) {
//                        Concept existingConcept = concepts.get(concept.getUri());
//                        existingConcept.mergeConcept(concept);
//                    } else {
//                        concepts.put(concept.getUri(), concept);
//                    }
//                    
//                    datasetConceptUris.add(conceptUri);
//
//                    /**
//                     * MNG: these code lines aim to solve the issue: ESA
//                     * thesaurus is using old GCMD URI (i.e.
//                     * https://gcmdservices.gsfc.nasa.gov/kms/concept) instead
//                     * of new GCMD URI
//                     * (i.e.https://gcmd.earthdata.nasa.gov/kms/concept)
//                     */
//                    if (concepts.containsKey(uuid)) {
//                        Concept existingConcept = concepts.get(uuid);
//                        existingConcept.mergeConcept(concept);
//                    } else {
//                        concepts.put(uuid, concept);
//                    }
//                }
//            }
//        }
        return false;
    }

    public static ConcurrentHashMap<String, Organisation> loadOrganisationMapping(String orgDictFilePath,
            String mappingFilePath) throws IOException {
        ConcurrentHashMap<String, Organisation> organisations = loadProviders(orgDictFilePath);
        ConcurrentHashMap<String, Organisation> mappings = new ConcurrentHashMap<>();

        if (new File(mappingFilePath).exists()) {
            try (
                    Reader reader = new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream(mappingFilePath), StandardCharsets.UTF_8.name()));
                    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                            .withTrim());) {
                /*
                    skip the lines until find “URI” in the first column
                 */
                int matchColumn = -1;
                int labelColumn = -1;

                for (CSVRecord csvRecord : csvParser) {

                    if (csvRecord.get(0).equals("URI")) {
                        for (int i = 1; i < csvRecord.size(); i++) {
                            String header = getValue(csvRecord, i);
                            if (StringUtils.isEmpty(header) && matchColumn == -1) {
                                matchColumn = i;
                            }

                            if ("skos:hiddenLabel".equals(header) && labelColumn == -1) {
                                labelColumn = i;
                            }
                        }
                        break;
                    }
                }

                for (CSVRecord csvRecord : csvParser) {
                    if (matchColumn >= 0 && labelColumn >= 0) {
                        String uuid = getValue(csvRecord, matchColumn);
                        String label = getValue(csvRecord, labelColumn);
                        if (StringUtils.isNotEmpty(uuid) && StringUtils.isNotEmpty(label)) {
                            Organisation org = organisations.get(uuid);
                            if (org != null) {
                                org.setLabel(label);
                                mappings.putIfAbsent(label, org);
                            }
                        }
                    }
                }
            }
        }
        return mappings;

    }

    public static ConcurrentHashMap<String, String> loadTopics(String filePath)
            throws IOException {
        ConcurrentHashMap<String, String> topics = new ConcurrentHashMap<>();
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withTrim());) {

            /*
                    skip the lines until find “URI” in the first column
             */
            int topicColumn = -1;
            int dif10Column = -1;

            for (CSVRecord csvRecord : csvParser) {

                if (csvRecord.get(0).equals("URI")) {
                    for (int i = 1; i < csvRecord.size(); i++) {
                        String header = getValue(csvRecord, i);
                        if ("skos:hiddenLabel".equals(header)) {
                            topicColumn = i;
                        } else {
                            if ("skos:altLabel".equals(header)) {
                                dif10Column = i;
                            }
                        }
                    }
                    break;
                }
            }

            for (CSVRecord csvRecord : csvParser) {
                if (topicColumn >= 0 && dif10Column >= 0) {
                    String topic = getValue(csvRecord, topicColumn);
                    String dif10Topic = getValue(csvRecord, dif10Column);
                    if (StringUtils.isNotEmpty(topic) && StringUtils.isNotEmpty(dif10Topic)) {
                        topics.putIfAbsent(topic, dif10Topic);
                    }
                }
            }
        }
        return topics;
    }

    private static ConcurrentHashMap<String, Organisation> loadProviders(String filePath)
            throws IOException {
        ConcurrentHashMap<String, Organisation> organisations = new ConcurrentHashMap<>();
        try (
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim());) {
            for (CSVRecord csvRecord : csvParser) {
                Organisation org = new Organisation();

                org.setShortName(getValue(csvRecord, "Short_Name"));
                org.setLongName(getValue(csvRecord, "Long_Name"));
                org.setDataCenterUrl(getValue(csvRecord, "Data_Center_URL"));
                String uuid = getValue(csvRecord, "UUID");
                org.setUuid(uuid);
                if (StringUtils.isNotEmpty(uuid)) {
                    organisations.putIfAbsent(uuid, org);
                }
            }
        }
        return organisations;
    }

    private static String getValue(CSVRecord csvRecord, String name) {
        try {
            String value = csvRecord.get(name);
            if (value != null && !value.isEmpty()) {
                value = value.trim();
            }
            return value;
        } catch (IllegalStateException | IllegalArgumentException e) {
            return "";
        }
    }

    private static String getValue(CSVRecord csvRecord, int index) {
        try {
            String value = csvRecord.get(index);
            if (value != null && !value.isEmpty()) {
                value = value.trim();
            }
            return value;
        } catch (IllegalStateException | IllegalArgumentException e) {
            return "";
        }
    }
}
