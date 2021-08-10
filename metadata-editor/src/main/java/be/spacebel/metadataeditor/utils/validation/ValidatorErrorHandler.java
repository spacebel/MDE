/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class implements the {@link org.xml.sax.ErrorHandler} interface to
 * customize error handling
 *
 * @author mng
 */
public class ValidatorErrorHandler implements ErrorHandler {

    private List<ValidationError> errors;
    
    private final int recordType;

    public ValidatorErrorHandler(int recordType) {        
        this.recordType = recordType;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        handleError(FacesMessage.SEVERITY_WARN, exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        handleError(FacesMessage.SEVERITY_ERROR, exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        handleError(FacesMessage.SEVERITY_FATAL, exception);
    }

    private void handleError(FacesMessage.Severity errorLevel, SAXParseException exception) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Line ").append(exception.getLineNumber()).append(", column ").append(exception.getColumnNumber()).append(" : ").append(exception.getMessage());

        ValidationError vError = new ValidationError(errorLevel, sb.toString(), 1, recordType);
        errors.add(vError);
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}
