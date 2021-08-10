package be.spacebel.metadataeditor.business;

/**
 * Signals that a search exception has occurred
 *
 * @author mng
 */
public class SearchException extends Exception {

    private static final long serialVersionUID = 1L;

    private String title;
    private String detail;

    public SearchException(String title, String detail) {
        this.title = title;
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

}
