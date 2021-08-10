/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * This class represents gmd:keyword element of ISO 19139-2 XML metadata
 * 
 * @author mng
 */
public class Keyword implements Serializable {

    private String uri;
    private String label;
    private final String uuid;    

    public Keyword() {
        uuid = UUID.randomUUID().toString();
        this.uri = "";
        this.label = "";
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUuid() {
        return uuid;
    }    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Keyword)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUuid().equals(((Keyword) obj).getUuid());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUuid());
        return hash;
    }
}
