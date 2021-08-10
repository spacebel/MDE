/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents either rdf:Description or skos:Concept
 *
 * @author mng
 */
public class Concept implements Serializable {
    
    boolean csv;
    //private String key;
    private String uri;
    private String label;
    private boolean topConcept;
    private boolean root;
    private List<String> children;
    
    private final Map<String, List<String>> properties;
    //private final Map<String, List<String>> relations;

    public Concept() {
        this.properties = new ConcurrentHashMap<>();
        //  this.relations = new ConcurrentHashMap<>();
    }

    // clone method
    public Concept(Concept srcConcept) {
        this.csv = srcConcept.isCsv();
        //this.key = srcConcept.getKey();
        this.uri = srcConcept.getUri();
        this.label = srcConcept.getLabel();
        this.properties = new ConcurrentHashMap<>(srcConcept.getProperties());
        // this.relations = new ConcurrentHashMap<>(srcConcept.getRelations());
    }
    
    public void addProperty(String proKey, String proValue) {
        if (properties.containsKey(proKey)) {
            List<String> values = properties.get(proKey);
            if (!values.contains(proValue)) {
                values.add(proValue);
            }
        } else {
            List<String> values = new ArrayList<>();
            values.add(proValue);
            properties.put(proKey, values);
        }
    }
    
    public void addProperty(String proKey, List<String> proValues) {
        if (proValues != null && !proValues.isEmpty()) {
            proValues.forEach((pValue) -> {
                addProperty(proKey, pValue);
            });
        }
    }
    
    public void addOverWriteProperty(String proKey, List<String> proValues) {
        if (properties.containsKey(proKey)) {
            properties.remove(proKey);
        }
        addProperty(proKey, proValues);
    }

//    public void addRelation(String relKey, String relValue) {        
//        if (relations.containsKey(relKey)) {
//            List<String> values = relations.get(relKey);
//            if (!values.contains(relValue)) {
//                values.add(relValue);
//            }
//        } else {
//            List<String> values = new ArrayList<>();
//            values.add(relValue);
//            relations.put(relKey, values);
//        }
//    }
//    public void addRelation(String relKey, List<String> relValues) {
//        
//        if (relations.containsKey(relKey)) {
//            List<String> values = relations.get(relKey);
//            for (String newValue : relValues) {               
//                if (!values.contains(newValue)) {
//                    values.add(newValue);
//                }
//            }
//        } else {
//            relations.put(relKey, relValues);
//        }
//    }
    public void mergeConcept(Concept newConcept) {
        if (StringUtils.isNotEmpty(newConcept.getLabel())) {
            setLabel(newConcept.getLabel());
        }
        
        if (newConcept.isCsv()) {
            setCsv(true);
        }
        
        if (newConcept.isTopConcept()) {
            setTopConcept(true);
        }
        
        if (newConcept.isRoot()) {
            setRoot(true);
        }
        newConcept.getProperties().entrySet().forEach((entry) -> {
            addProperty(entry.getKey(), entry.getValue());
        });
    }

//    public void setKey(String key) {
//        this.key = key;
//    }
//
//    public void setKey(String datasetUri, String conceptUri) {
//        this.key = datasetUri + Constants.SEPARATOR + conceptUri;
//        this.uri = conceptUri;
//    }
    public void setCsv(boolean csv) {
        this.csv = csv;
    }
    
    public boolean isCsv() {
        return csv;
    }

//    public String getKey() {
//        return key;
//    }
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
    
    public Map<String, List<String>> getProperties() {
        return properties;
    }

//    public Map<String, List<String>> getRelations() {
//        return relations;
//    }
    public List<String> getPropertyValues(String proKey) {
        if (properties.containsKey(proKey)) {
            return properties.get(proKey);
        }
        return new ArrayList<>();
    }
    
    public String getPropertyValue(String proKey) {
        if (properties.containsKey(proKey)) {
            return StringUtils.join(properties.get(proKey), ",");
        }
        return "";
    }
    
    public boolean isTopConcept() {
        return topConcept;
    }
    
    public void setTopConcept(boolean topConcept) {
        this.topConcept = topConcept;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }       
    
    public String toStr() {
        StringBuilder sb = new StringBuilder();
        sb.append("Concept[");
        //sb.append("key = ").append(key);
        sb.append(";uri = ").append(uri);
        sb.append(";label = ").append(label);
        sb.append("; Properties[");
        properties.entrySet().forEach((entry) -> {
            sb.append(entry.getKey())
                    .append(" = ")
                    .append(String.join(",", entry.getValue()))
                    .append(";");
        });
        sb.append("]");
        sb.append("; Relations[");
//        relations.entrySet().forEach((entry) -> {
//            sb.append(entry.getKey())
//                    .append(" = ")
//                    .append(String.join(",", entry.getValue()))
//                    .append(";");
//        });
//        sb.append("]");
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Concept)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Concept otherConcept = (Concept) obj;
        if (this.getUri().equalsIgnoreCase(otherConcept.getUri())) {
            return true;
        } else {
            String uuid = StringUtils.substringAfterLast(this.getUri(), "/");
            String otherUuid = StringUtils.substringAfterLast(otherConcept.getUri(), "/");
            if (StringUtils.isNotEmpty(uuid) && StringUtils.isNotEmpty(otherUuid)) {
                return uuid.equalsIgnoreCase(otherUuid);
            }
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUri());
        return hash;
    }
}
