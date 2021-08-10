/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers.layout;

import java.io.Serializable;

/**
 * This class represents a current view of a web page
 *
 * @author mng
 */
public class View implements Serializable {

    private boolean list;
    private boolean thumbnail;
    private boolean details;
    private boolean edit;
    private boolean xml;
    private boolean geojson;
    private boolean internalModel;
    private boolean dif10;
    private boolean report;
    private boolean thesaurusWarn;

    public View() {
    }

    public boolean isDetails() {
        return details;
    }

    public void setDetails(boolean details) {
        this.details = details;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isXml() {
        return xml;
    }

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }

    public boolean isGeojson() {
        return geojson;
    }

    public void setGeojson(boolean geojson) {
        this.geojson = geojson;
    }

    public boolean isInternalModel() {
        return internalModel;
    }

    public void setInternalModel(boolean internalModel) {
        this.internalModel = internalModel;
    }

    public boolean isDif10() {
        return dif10;
    }

    public void setDif10(boolean dif10) {
        this.dif10 = dif10;
    }

    public boolean isReport() {
        return report;
    }

    public void setReport(boolean report) {
        this.report = report;
    }

    public boolean isThesaurusWarn() {
        return thesaurusWarn;
    }

    public void setThesaurusWarn(boolean thesaurusWarn) {
        this.thesaurusWarn = thesaurusWarn;
    }

    public void toList() {
        this.list = true;
        this.thumbnail = false;
        this.edit = false;
        this.details = false;
        this.xml = false;
        this.geojson = false;
        this.internalModel = false;
        this.dif10 = false;
        this.report = false;
        this.thesaurusWarn = false;
    }

    public void toThumbnail() {
        this.thumbnail = true;
        this.list = false;
        this.edit = false;
        this.details = false;
        this.xml = false;
        this.geojson = false;
        this.internalModel = false;
        this.dif10 = false;
        this.report = false;
        this.thesaurusWarn = false;
    }

    public void toEdit() {
        this.edit = true;
        this.list = false;
        this.thumbnail = false;
        this.details = false;
        this.xml = false;
        this.geojson = false;
        this.internalModel = false;
        this.dif10 = false;
        this.report = false;
        this.thesaurusWarn = false;
    }

    public void toDetails() {
        this.details = true;
        this.list = false;
        this.thumbnail = false;
        this.edit = false;
        this.xml = false;
        this.geojson = false;
        this.internalModel = false;
        this.dif10 = false;
        this.report = false;
        this.thesaurusWarn = false;
    }

    public void toXml() {
        this.xml = true;
        this.list = false;
        this.thumbnail = false;
        this.edit = false;
        this.details = false;
        this.geojson = false;
        this.internalModel = false;
        this.dif10 = false;
        this.report = false;
        this.thesaurusWarn = false;
    }

    public void toGeoJson() {
        this.geojson = true;
        this.internalModel = false;
        this.xml = false;
        this.list = false;
        this.thumbnail = false;
        this.edit = false;
        this.details = false;
        this.dif10 = false;
        this.report = false;
        this.thesaurusWarn = false;
    }
    
    public void toInternalModel() {
        this.internalModel = true;
        this.geojson = false;        
        this.xml = false;
        this.list = false;
        this.thumbnail = false;
        this.edit = false;
        this.details = false;
        this.dif10 = false;
        this.report = false;
        this.thesaurusWarn = false;
    }

    public void toDif10() {
        this.dif10 = true;
        this.geojson = false;
        this.internalModel = false;
        this.xml = false;
        this.list = false;
        this.thumbnail = false;
        this.edit = false;
        this.details = false;
        this.report = false;
        this.thesaurusWarn = false;
    }

    public void toReport() {
        this.report = true;
        this.dif10 = false;
        this.geojson = false;
        this.internalModel = false;
        this.xml = false;
        this.list = false;
        this.thumbnail = false;
        this.edit = false;
        this.details = false;
        this.thesaurusWarn = false;
    }

    public void toThesaurusWarn() {
        this.thesaurusWarn = true;
        this.report = false;
        this.dif10 = false;
        this.geojson = false;
        this.internalModel = false;
        this.xml = false;
        this.list = false;
        this.thumbnail = false;
        this.edit = false;
        this.details = false;
    }
}
