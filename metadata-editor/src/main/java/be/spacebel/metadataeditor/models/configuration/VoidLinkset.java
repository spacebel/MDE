/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import java.io.Serializable;

/**
 * This class represents void:Linkset
 * 
 * @author mng
 */
public class VoidLinkset implements Serializable {

    private String subjectsTarget;
    private String objectsTarget;
    private String feature;
    private String linkPredicate;
    private String inverseLinkPredicate;
    private String dataFile;

    public VoidLinkset() {
    }

    public VoidLinkset(String subjectsTarget, String objectsTarget,
            String feature, String dataFile) {
        this.subjectsTarget = subjectsTarget;
        this.objectsTarget = objectsTarget;
        this.feature = feature;        
        this.dataFile = dataFile;
    }

    public String getSubjectsTarget() {
        return subjectsTarget;
    }

    public void setSubjectsTarget(String subjectsTarget) {
        this.subjectsTarget = subjectsTarget;
    }

    public String getObjectsTarget() {
        return objectsTarget;
    }

    public void setObjectsTarget(String objectsTarget) {
        this.objectsTarget = objectsTarget;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getLinkPredicate() {
        return linkPredicate;
    }

    public void setLinkPredicate(String linkPredicate) {
        this.linkPredicate = linkPredicate;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public String getInverseLinkPredicate() {
        return inverseLinkPredicate;
    }

    public void setInverseLinkPredicate(String inverseLinkPredicate) {
        this.inverseLinkPredicate = inverseLinkPredicate;
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("VoidLinkset[");
        sb.append("subjectsTarget").append(subjectsTarget);
        sb.append("; objectsTarget = ").append(objectsTarget);
        sb.append("; feature = ").append(feature);
        sb.append("; linkPredicate = ").append(linkPredicate);
        sb.append("; inverseLinkPredicate = ").append(inverseLinkPredicate);        
        sb.append("; dataFile = ").append(dataFile);
        sb.append("]");
        return sb.toString();
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (!(obj instanceof VoidLinkset)) {
//            return false;
//        }
//        if (obj == this) {
//            return true;
//        }
//        return this.getUuid().equals(((VoidLinkset) obj).getUuid());
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 83 * hash + Objects.hashCode(getUuid());
//        return hash;
//    }
}
