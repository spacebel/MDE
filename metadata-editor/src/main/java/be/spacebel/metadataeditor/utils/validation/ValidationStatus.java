/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;

/**
 * This is a data model to be used to store validation status of a metadata
 * record
 *
 * @author mng
 */
public class ValidationStatus implements Serializable {

    private final String recordId;

    private List<ValidationError> messages;
    private boolean error;

    public ValidationStatus(String recordId) {
        this.recordId = recordId;
    }

    public void addMessage(ValidationError message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        if (message.getErrorLevel() == FacesMessage.SEVERITY_ERROR) {
            error = true;
        }
    }

    public String getRecordId() {
        return recordId;
    }

    public List<ValidationError> getMessages() {
        return messages;
    }

    public boolean isValid() {
        return (messages == null || messages.isEmpty());
    }

    public boolean isError() {
        return error;
    }

    public boolean isWarn() {
        return (!error && messages != null && messages.size() > 0);
    }

    public String getStatus() {
        if (isValid()) {
            return "Valid";
        } else {
            if (isError()) {
                return "Error";
            } else {
                return "Warn";
            }
        }

    }
}
