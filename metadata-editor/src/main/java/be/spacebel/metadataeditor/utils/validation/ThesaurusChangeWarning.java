/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import be.spacebel.metadataeditor.models.configuration.VoidDataset;
import java.io.Serializable;
import java.util.Objects;

/**
 * This is a data model class to be used to store list of metadata records per thesaurus impacted due to a newer version of the thesauri
 * @author mng
 */
public class ThesaurusChangeWarning implements Serializable {

    private String uri;
    private VoidDataset thesaurus;
    private int numOfFiles;

    public ThesaurusChangeWarning() {
    }

    public ThesaurusChangeWarning(String uri, VoidDataset thesaurus, int numOfFiles) {
        this.uri = uri;
        this.thesaurus = thesaurus;
        this.numOfFiles = numOfFiles;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public VoidDataset getThesaurus() {
        return thesaurus;
    }

    public void setThesaurus(VoidDataset thesaurus) {
        this.thesaurus = thesaurus;
    }

    public int getNumOfFiles() {
        return numOfFiles;
    }

    public void setNumOfFiles(int numOfFiles) {
        this.numOfFiles = numOfFiles;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ThesaurusChangeWarning)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUri().equals(((ThesaurusChangeWarning) obj).getUri());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUri());
        return hash;
    }
}
