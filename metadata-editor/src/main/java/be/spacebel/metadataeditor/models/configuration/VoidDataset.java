/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents void:Dataset
 *
 * @author mng
 */
public class VoidDataset implements Serializable {

    private String uri;
    private String label;
    private String title;
    private String modified;
    private String dataFileName;
    private String remoteDataFile;
    private String version;
    private String feature;
    private String uriSpace;

    public VoidDataset() {
    }

    public VoidDataset(String uri, String label, String title,
            String modified, String dataFileName, String remoteDataFile, String feature, String uriSpace) {
        this.uri = uri;
        this.label = label;
        this.title = title;
        this.modified = modified;
        this.dataFileName = dataFileName;
        this.remoteDataFile = remoteDataFile;
        this.feature = feature;
        this.uriSpace = uriSpace;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullTitle() {
        if (title != null
                && (title.contains("#{VERSION}") || title.contains("{#YEAR}"))) {
            String fullTitle = title;
            
            String year = "";
            if (StringUtils.isNotEmpty(modified)) {
                if (modified.length() >= 10) {
                    year = modified.substring(0, 4);
                }
            }            
            fullTitle = fullTitle.replace("{#YEAR}", year);

            if (StringUtils.isNotEmpty(version)) {
                fullTitle = fullTitle.replace("#{VERSION}", "Version " + version);
            } else {
                fullTitle = fullTitle.replace("#{VERSION}", "");
            }
            return fullTitle;
        }
        return title;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getRemoteDataFile() {
        return remoteDataFile;
    }

    public void setRemoteDataFile(String remoteDataFile) {
        this.remoteDataFile = remoteDataFile;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getUriSpace() {
        return uriSpace;
    }

    public void setUriSpace(String uriSpace) {
        this.uriSpace = uriSpace;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("VoidDataset[");
        sb.append("uri = ").append(uri);
        sb.append("; label = ").append(label);
        sb.append("; title = ").append(title);
        sb.append("; modified = ").append(modified);
        sb.append("; version = ").append(version);
        sb.append("; dataFileName = ").append(dataFileName);
        sb.append("; remoteDataFile = ").append(remoteDataFile);
        sb.append("; feature = ").append(feature);
        sb.append("; uriSpace = ").append(uriSpace);
        sb.append("]");
        return sb.toString();
    }

}
