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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

/**
 * This class represents gmi:platform element of ISO 19139-2 XML metadata
 *
 * @author mng
 */
public class Platform implements Serializable {

    private final Logger log = Logger.getLogger(getClass());

    private String uri;
    private String label;
    private GmxAnchor altTitle;
    private boolean esaPlatform;
    private List<Instrument> availableInstruments;
    private List<Instrument> instruments;
    private String selectedInstrumentUri;
    private List<Sponsor> operators;
    private String currentOperator;
    private Date launchDate;
    private Node launchDateNode;
    private GmxAnchor identifier;
    private GmxAnchor description;

    private Concept gcmd;
    private Node self;

    private AutoCorrectionWarning warning;
    private AutoCorrectionWarning gcmdWarning;

    private final String uuid;

    public Platform() {
        uuid = UUID.randomUUID().toString();
        esaPlatform = true;
    }

//    public Platform(Concept concept) {
//        this.uri = concept.getUri();
//        this.label = concept.getLabel();
//        uuid = UUID.randomUUID().toString();
//    }
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public GmxAnchor getAltTitle() {
        return altTitle;
    }

    public void setAltTitle(GmxAnchor altTitle) {
        this.altTitle = altTitle;
    }

    public boolean isEsaPlatform() {
        return esaPlatform;
    }

    public void setEsaPlatform(boolean esaPlatform) {
        this.esaPlatform = esaPlatform;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Instrument> getAvailableInstruments() {
        return availableInstruments;
    }

    public void setAvailableInstruments(List<Instrument> availableInstruments) {
        this.availableInstruments = availableInstruments;
    }

    public List<Instrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public void addInstrument(Instrument inst) {
        if (this.instruments == null) {
            this.instruments = new ArrayList<>();
        }
        this.instruments.add(inst);
    }

    public void onBlurOperator(final AjaxBehaviorEvent event) {
        if (StringUtils.isNotEmpty(currentOperator)) {
            boolean exist = false;
            if (operators != null) {
                for (Sponsor oper : operators) {
                    if (currentOperator.equals(oper.getOperator().getLabel())) {
                        exist = true;
                        break;
                    }
                }
            }

            if (!exist) {
                if (operators == null) {
                    operators = new ArrayList<>();
                }
                Sponsor sps = new Sponsor();
                sps.addOperator(currentOperator);
                operators.add(sps);
            }
            currentOperator = StringUtils.EMPTY;
        }
    }

    public void removeOperator(Sponsor sps) {
        operators.remove(sps);
    }

    public String getUuid() {
        return uuid;
    }

    public List<Sponsor> getOperators() {
        return operators;
    }

    public void setOperators(List<Sponsor> operators) {
        this.operators = operators;
    }

    public String getCurrentOperator() {
        return currentOperator;
    }

    public void setCurrentOperator(String currentOperator) {
        this.currentOperator = currentOperator;
    }

    public Concept getGcmd() {
        return gcmd;
    }

    public void setGcmd(Concept gcmd) {
        this.gcmd = gcmd;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(Date launchDate) {
        this.launchDate = launchDate;
    }

    public Node getLaunchDateNode() {
        return launchDateNode;
    }

    public void setLaunchDateNode(Node launchDateNode) {
        this.launchDateNode = launchDateNode;
    }

    public GmxAnchor getIdentifier() {
        return identifier;
    }

    public void setIdentifier(GmxAnchor identifier) {
        this.identifier = identifier;
    }

    public GmxAnchor getDescription() {
        return description;
    }

    public void setDescription(GmxAnchor description) {
        this.description = description;
    }

    public String getSelectedInstrumentUri() {
        return selectedInstrumentUri;
    }

    public void setSelectedInstrumentUri(String selectedInstrumentUri) {
        this.selectedInstrumentUri = selectedInstrumentUri;
    }

    public List<SelectItem> getInstrumentOptions() {
        List<SelectItem> options = new ArrayList<>();
        if (availableInstruments != null) {
            availableInstruments.forEach((inst) -> {
                options.add(new SelectItem(inst.getUri(), inst.getLabel()));
            });
        }
        return options;
    }

    public void addInstrument(final AjaxBehaviorEvent event) {
        log.debug("Add instrument " + selectedInstrumentUri + " to the platform");
        if (selectedInstrumentUri != null
                && !selectedInstrumentUri.isEmpty()) {
            for (Instrument inst : availableInstruments) {
                log.debug("Inst URI " + inst.getUri());
                if (selectedInstrumentUri.equals(inst.getUri())) {
                    addInstrument(inst);

                    availableInstruments.remove(inst);
                    selectedInstrumentUri = null;
                    break;
                }
            }
        }
    }

    public void removeInstrument(Instrument inst) {
        if (availableInstruments != null) {
            availableInstruments.add(inst);
        }
        instruments.remove(inst);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Platform)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUri().equals(((Platform) obj).getUri());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUri());
        return hash;
    }
}
