/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

/**
 * This class represents Observations and Measurements thesaurus
 *
 * @author mng
 */
public class Thesaurus {

    private final String keywordType;
    private final String title;
    private final String titleUri;
    private String baseUrl;
    private String date;
    private String dateTypeCodeList;
    private String dateTypeCodeListValue;
    private String dateType;

    public Thesaurus(String titleUri, String title, String baseUrl, String date) {
        this.keywordType = null;
        this.title = title;
        this.titleUri = titleUri;
        this.baseUrl = baseUrl;
        this.date = date;
    }

    public Thesaurus(String keywordType, String title, String titleUri,
            String date, String dateTypeCodeList, String dateTypeCodeListValue, String dateType) {
        this.keywordType = keywordType;
        this.title = title;
        this.titleUri = titleUri;
        this.date = date;
        this.dateTypeCodeList = dateTypeCodeList;
        this.dateTypeCodeListValue = dateTypeCodeListValue;
        this.dateType = dateType;
    }

    public Thesaurus(Thesaurus eopthesaurus) {
        this.keywordType = eopthesaurus.getKeywordType();
        this.title = eopthesaurus.getTitle();
        this.titleUri = eopthesaurus.getTitleUri();
        this.date = eopthesaurus.getDate();
        this.dateTypeCodeList = eopthesaurus.getDateTypeCodeList();
        this.dateTypeCodeListValue = eopthesaurus.getDateTypeCodeListValue();
        this.dateType = eopthesaurus.getDateType();
        this.baseUrl = eopthesaurus.getBaseUrl();
    }

    public String getKeywordType() {
        return keywordType;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleUri() {
        return titleUri;
    }

    public String getDate() {
        return date;
    }

    public String getDateTypeCodeList() {
        return dateTypeCodeList;
    }

    public String getDateTypeCodeListValue() {
        return dateTypeCodeListValue;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDateTypeCodeList(String dateTypeCodeList) {
        this.dateTypeCodeList = dateTypeCodeList;
    }

    public void setDateTypeCodeListValue(String dateTypeCodeListValue) {
        this.dateTypeCodeListValue = dateTypeCodeListValue;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("EopThesaurus[");
        sb.append("keywordType = ").append(keywordType);
        sb.append("; title = ").append(title);
        sb.append("; titleUri = ").append(titleUri);
        sb.append("; baseUrl = ").append(baseUrl);
        sb.append("; date = ").append(date);
        sb.append("; dateTypeCodeList = ").append(dateTypeCodeList);
        sb.append("; dateTypeCodeListValue = ").append(dateTypeCodeListValue);
        sb.append("; dateType = ").append(dateType);
        sb.append("]");
        return sb.toString();
    }
}
