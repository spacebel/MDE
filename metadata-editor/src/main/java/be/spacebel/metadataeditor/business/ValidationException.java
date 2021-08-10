/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.business;

import be.spacebel.metadataeditor.utils.validation.ValidationError;
import be.spacebel.metadataeditor.utils.validation.ValidationErrorsPerFile;
import java.util.List;

/**
 * Signals that a schema validation exception has occurred
 *
 * @author mng
 */
public class ValidationException extends Exception {

    private List<ValidationErrorsPerFile> errorFiles;
    private List<ValidationError> validationErrors;

    public List<ValidationErrorsPerFile> getErrorFiles() {
        return errorFiles;
    }

    public void setErrorFiles(List<ValidationErrorsPerFile> errorFiles) {
        this.errorFiles = errorFiles;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

}
