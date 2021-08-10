/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import be.spacebel.metadataeditor.models.catalogue.OpenSearchUrl;
import java.io.Serializable;

/**
 * This class represents a Catalogue interface
 *
 * @author mng
 */
public class CatalogueInterface implements Serializable {

    private String name;
    private OpenSearchUrl searchUrl;
    private String searchTemplateUrl;
    private String insertUrl;
    private String deleteUrl;
    private String presentUrl;

    public CatalogueInterface() {
    }

    // clone
    public CatalogueInterface(CatalogueInterface catInterface) {
        this.name = catInterface.getName();
        this.searchUrl = catInterface.getSearchUrl();
        this.searchTemplateUrl = catInterface.getSearchTemplateUrl();
        this.insertUrl = catInterface.getInsertUrl();
        this.deleteUrl = catInterface.getDeleteUrl();
        this.presentUrl = catInterface.getPresentUrl();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OpenSearchUrl getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(OpenSearchUrl searchUrl) {
        this.searchUrl = searchUrl;
    }

    public String getSearchTemplateUrl() {
        return searchTemplateUrl;
    }

    public void setSearchTemplateUrl(String searchTemplateUrl) {
        this.searchTemplateUrl = searchTemplateUrl;
    }

    public String getInsertUrl() {
        return insertUrl;
    }

    public void setInsertUrl(String insertUrl) {
        this.insertUrl = insertUrl;
    }

    public String getDeleteUrl() {
        return deleteUrl;
    }

    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }

    public String getPresentUrl() {
        return presentUrl;
    }

    public void setPresentUrl(String presentUrl) {
        this.presentUrl = presentUrl;
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Interface[");
        sb.append("Name = ").append(name);        
        sb.append("; searchTemplateUrl = ").append(searchTemplateUrl);
        sb.append("; insertUrl = ").append(insertUrl);
        sb.append("; deleteUrl = ").append(deleteUrl);
        sb.append("; presentUrl = ").append(presentUrl);
        sb.append("]");
        return sb.toString();
    }
}
