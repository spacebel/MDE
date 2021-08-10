/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.distribution;

import java.io.Serializable;
import java.util.UUID;
import org.w3c.dom.Node;

/**
 * A representation of gmd:onLine element of ISO 19139-2 metadata
 * 
 * @author mng
 */
public class OnlineResource implements Serializable {

    private final String uuid;
    private String linkage;
    private String protocol;
    private String appProfile;
    private String name;
    private String description;
    private String function;
    private String relatedField;
    

    private Node self;

    public OnlineResource() {
        uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public String getLinkage() {
        return linkage;
    }

    public void setLinkage(String linkage) {
        this.linkage = linkage;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAppProfile() {
        return appProfile;
    }

    public void setAppProfile(String appProfile) {
        this.appProfile = appProfile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }    

    public String getRelatedField() {
        return relatedField;
    }

    public void setRelatedField(String relatedField) {
        this.relatedField = relatedField;
    }    

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OnlineResource)) {
            return false;
        }
        OnlineResource other = (OnlineResource) obj;
        return this.getUuid().equals(other.getUuid());
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }

}
