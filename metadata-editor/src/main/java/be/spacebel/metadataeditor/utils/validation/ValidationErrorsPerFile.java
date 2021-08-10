/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents schema validation exception messages of an ISO 19139-2
 * XML metadata file
 *
 * @author mng
 */
public class ValidationErrorsPerFile implements Serializable {

    private List<ValidationError> errors;
    private String recordId;

    public ValidationErrorsPerFile(List<ValidationError> errors, String recordId) {
        this.errors = errors;
        this.recordId = recordId;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

}
