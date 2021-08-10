/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.utils.validation.AutoCorrectionWarning;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This class represents an Earth topic keyword of ISO 19139-2 XML metadata
 *
 * @author mng
 */
public class EarthTopic implements Serializable {

    private String uri;
    private String label;
    private final String uuid;
    private boolean esaEarthTopic;

    private List<Concept> scienceKeywords;
//    /*
//        <relation_id (e.g: skos:exactMatch),list_of_related_concepts>        
//     */
//    private Map<String, List<Concept>> relations;
    private List<AutoCorrectionWarning> sckWarnings;
    private AutoCorrectionWarning warning;
    private Map<String, String> changedScienceKeywords;

    public EarthTopic() {
        uuid = UUID.randomUUID().toString();
        esaEarthTopic = true;
    }

    public EarthTopic(Keyword kw) {
        uri = kw.getUri();
        label = kw.getLabel();
        uuid = UUID.randomUUID().toString();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isEsaEarthTopic() {
        return esaEarthTopic;
    }

    public void setEsaEarthTopic(boolean esaEarthTopic) {
        this.esaEarthTopic = esaEarthTopic;
    }

    public List<Concept> getScienceKeywords() {
        return scienceKeywords;
    }

    public void addScienceKeyword(Concept sckwConcept) {
        if (scienceKeywords == null) {
            scienceKeywords = new ArrayList<>();
        }

        if (!scienceKeywords.contains(sckwConcept)) {
            scienceKeywords.add(sckwConcept);
        }
    }

    public Map<String, String> getChangedScienceKeywords() {
        return changedScienceKeywords;
    }

    public void setChangedScienceKeywords(Map<String, String> changedScienceKeywords) {
        this.changedScienceKeywords = changedScienceKeywords;
    }

    public void addSckWarning(AutoCorrectionWarning warn) {
        if (sckWarnings == null) {
            sckWarnings = new ArrayList<>();
        }
        this.sckWarnings.add(warn);
    }

    public List<AutoCorrectionWarning> getSckWarnings() {
        return sckWarnings;
    }

    public void setSckWarnings(List<AutoCorrectionWarning> sckWarnings) {
        this.sckWarnings = sckWarnings;
    }

    public AutoCorrectionWarning getWarning() {
        return warning;
    }

    public void setWarning(AutoCorrectionWarning warning) {
        this.warning = warning;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EarthTopic)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUri().equals(((EarthTopic) obj).getUri());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUri());
        return hash;
    }
}
