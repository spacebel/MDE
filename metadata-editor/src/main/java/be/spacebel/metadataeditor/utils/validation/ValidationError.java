/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;

/**
 * This class represents a schema validation exception message
 *
 * @author mng
 */
public class ValidationError implements Serializable {

    private Severity errorLevel;
    private String message;
    private final int formatType;
    private final int recordType;
    private String recordId;

    public ValidationError() {
        this.formatType = 0;
        this.recordType = 0;
    }

    public ValidationError(Severity errorLevel, String message, int formatType, int recordType) {
        this.errorLevel = errorLevel;
        this.message = message;
        this.formatType = formatType;
        this.recordType = recordType;
    }

    public Severity getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(Severity errorLevel) {
        this.errorLevel = errorLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

//    public boolean isXml() {
//        return (this.formatType == 1);
//    }
//
//    public boolean isJson() {
//        return (this.formatType == 2);
//    }
//
//    public boolean isDif10() {
//        return (this.formatType == 3);
//    }
    public int getRecordType() {
        return recordType;
    }

    public String getStatus() {
        if (FacesMessage.SEVERITY_ERROR == errorLevel) {
            return "Error";
        } else {
            return "Warning";
        }
    }

    public String getFormat() {
        switch (formatType) {
            case 1:
                return "XML";
            case 2:
                return "GeoJSON";
            case 3:
                return "DIF-10";
        }
        return "";
    }
//    public String getStatus() {
//        StringBuilder sb = new StringBuilder();
//        if (FacesMessage.SEVERITY_ERROR == errorLevel) {
//            sb.append("Error");
//        } else {
//            sb.append("Warning");
//        }
//
//        if (isXml()) {
//            sb.append(" (XML)");
//        }
//        if (isJson()) {
//            sb.append(" (GeoJSON)");
//        }
//        if (isDif10()) {
//            sb.append(" (DIF-10)");
//        }
//
//        return sb.toString();
//    }
}
