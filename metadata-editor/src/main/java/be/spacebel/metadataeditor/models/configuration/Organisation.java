/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents GCMD Service Provider
 * @author mng
 */
public class Organisation implements Serializable {

    private String shortName;
    private String longName;
    private String dataCenterUrl;
    private String uuid;
    private String label;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getDataCenterUrl() {
        return dataCenterUrl;
    }

    public void setDataCenterUrl(String dataCenterUrl) {
        this.dataCenterUrl = dataCenterUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String debug() {
        return "Organisation[shortName=" + shortName + ", longName=" + longName + ", dataCenterUrl=" + dataCenterUrl + ", uuid=" + uuid + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Organisation)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUuid().equals(((Organisation) obj).getUuid());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.uuid);
        return hash;
    }
}
