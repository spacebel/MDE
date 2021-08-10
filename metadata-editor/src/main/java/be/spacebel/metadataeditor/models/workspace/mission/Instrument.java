/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.mission;

import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.workspace.identification.GmxAnchor;
import be.spacebel.metadataeditor.utils.validation.AutoCorrectionWarning;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.w3c.dom.Node;

/**
 * This class represents gmi:instrument element of ISO 19139-2 XML metadata
 *
 * @author mng
 */
public class Instrument implements Serializable {

    private String uri;
    private String label;
    private GmxAnchor altTitle;
    private Concept gcmd;
    private List<Concept> broaders;
    private Node self;
    private GmxAnchor identifier;
    private GmxAnchor description;
    private boolean esaInstrument;
    private List<AutoCorrectionWarning> instrumentTypeWarnings;
    private AutoCorrectionWarning warning;
    private AutoCorrectionWarning gcmdWarning;
    private boolean hosted;

    public Instrument() {
        esaInstrument = true;
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

    public Concept getGcmd() {
        return gcmd;
    }

    public void setGcmd(Concept gcmd) {
        this.gcmd = gcmd;
    }

    public List<Concept> getBroaders() {
        return broaders;
    }

    public void setBroaders(List<Concept> broaders) {
        this.broaders = broaders;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public GmxAnchor getIdentifier() {
        return identifier;
    }

    public void setIdentifier(GmxAnchor identifier) {
        this.identifier = identifier;
    }

    public GmxAnchor getAltTitle() {
        return altTitle;
    }

    public void setAltTitle(GmxAnchor altTitle) {
        this.altTitle = altTitle;
    }

    public boolean isEsaInstrument() {
        return esaInstrument;
    }

    public void setEsaInstrument(boolean esaInstrument) {
        this.esaInstrument = esaInstrument;
    }

    public GmxAnchor getDescription() {
        return description;
    }

    public void setDescription(GmxAnchor description) {
        this.description = description;
    }

    public List<AutoCorrectionWarning> getInstrumentTypeWarnings() {
        return instrumentTypeWarnings;
    }

    public void setInstrumentTypeWarnings(List<AutoCorrectionWarning> instrumentTypeWarnings) {
        this.instrumentTypeWarnings = instrumentTypeWarnings;
    }

    public void addInstrumentTypeWarning(AutoCorrectionWarning warn) {
        if (instrumentTypeWarnings == null) {
            instrumentTypeWarnings = new ArrayList<>();
        }
        this.instrumentTypeWarnings.add(warn);
    }

    public AutoCorrectionWarning getWarning() {
        return warning;
    }

    public void setWarning(AutoCorrectionWarning warning) {
        this.warning = warning;
    }

    public AutoCorrectionWarning getGcmdWarning() {
        return gcmdWarning;
    }

    public void setGcmdWarning(AutoCorrectionWarning gcmdWarning) {
        this.gcmdWarning = gcmdWarning;
    }

    public boolean isHosted() {
        return hosted;
    }

    public void setHosted(boolean hosted) {
        this.hosted = hosted;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Instrument)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUri().equals(((Instrument) obj).getUri());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUri());
        return hash;
    }
}
