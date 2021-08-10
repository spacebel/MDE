package be.spacebel.metadataeditor.models.catalogue;

import java.io.Serializable;

/**
 * This class represents a search exception message
 * 
 * @author mng
 */
public class SearchResultError implements Serializable {

    private static final long serialVersionUID = 1L;

    private String errorCode;
    private String errorMessage;
    private String exceptionCode;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(String exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

}
