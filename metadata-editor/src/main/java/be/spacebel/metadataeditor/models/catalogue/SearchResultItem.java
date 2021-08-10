/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.catalogue;

import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import java.util.UUID;

/**
 * This class represents a search result item
 *
 * @author mng
 */
public class SearchResultItem {

    private String uuid;
    private boolean selected;
    private boolean download;
    private MetadataFile metadataFile;
    private String geoJsonLink;

    private int index;

    public SearchResultItem() {
        uuid = UUID.randomUUID().toString();

    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof SearchResultItem) {
            SearchResultItem sri = (SearchResultItem) obj;
            if (this.uuid.equals(sri.getUuid())) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
        return hash;
    }

    /* GETTERS AND SETTERS */
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public MetadataFile getMetadataFile() {
        return metadataFile;
    }

    public void setMetadataFile(MetadataFile metadataFile) {
        this.metadataFile = metadataFile;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getGeoJsonLink() {
        return geoJsonLink;
    }

    public void setGeoJsonLink(String geoJsonLink) {
        this.geoJsonLink = geoJsonLink;
    }

}
