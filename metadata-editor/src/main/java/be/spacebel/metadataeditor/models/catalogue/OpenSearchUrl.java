package be.spacebel.metadataeditor.models.catalogue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents an URL element of the OpenSearch Description Document
 *
 * @author mng
 */
public class OpenSearchUrl implements Serializable {

    private static final long serialVersionUID = 1L;
    private String templateUrl;
    private String responseFormat;
    private List<OpenSearchParameter> parameters;
    private int indexOffset;
    private int pageOffset;
    private boolean supportTextSearch;
    private String textSearchTitle;
    private OpenSearchParameter freeTextParameter;

    public OpenSearchUrl() {
        this.responseFormat = "application/atom+xml";
        this.indexOffset = 1;
        this.pageOffset = 1;
        this.parameters = new ArrayList<>();
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public void setTemplateUrl(String templateUrl) {
        this.templateUrl = templateUrl;
    }

    public List<OpenSearchParameter> getParameters() {
        return parameters;
    }

    public Map<String, OpenSearchParameter> getParameterMap() {
        Map<String, OpenSearchParameter> params = new HashMap<>();
        if (this.parameters != null) {
            for (OpenSearchParameter osParam : this.parameters) {
                if (osParam.getValue().contains(":")) {
                    params.put(StringUtils.substringAfter(osParam.getValue(), ":"), osParam);
                } else {
                    params.put(osParam.getValue(), osParam);
                }
            }
        }

        return params;
    }

    public void setParameters(List<OpenSearchParameter> parameters) {
        this.parameters = parameters;
    }

    public int getIndexOffset() {
        return indexOffset;
    }

    public void setIndexOffset(int indexOffset) {
        this.indexOffset = indexOffset;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public void setPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
    }

    public boolean isSupportTextSearch() {
        return supportTextSearch;
    }

    public void setSupportTextSearch(boolean supportTextSearch) {
        this.supportTextSearch = supportTextSearch;
    }

    public String getTextSearchTitle() {
        return textSearchTitle;
    }

    public void setTextSearchTitle(String textSearchTitle) {
        this.textSearchTitle = textSearchTitle;
    }

    public OpenSearchParameter getFreeTextParameter() {
        return freeTextParameter;
    }

    public void setFreeTextParameter(OpenSearchParameter freeTextParameter) {
        this.freeTextParameter = freeTextParameter;
    }
}
