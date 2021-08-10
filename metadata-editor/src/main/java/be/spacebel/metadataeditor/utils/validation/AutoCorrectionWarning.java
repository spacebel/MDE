/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import java.io.Serializable;

/**
 * This is a data model class to be used to store change warnings caused by a
 * newer version of the thesauri. The warnings of multiple records can be
 * corrected automatically at once
 *
 * @author mng
 */
public class AutoCorrectionWarning implements Serializable {

    private String recordId;
    private String uri;
    private String oldLabel;
    private String newLabel;
    // 1: EarthTopic, 2: Platform, 3:Instrument
    // 4: GCMD science keyword
    // 5: GCMD Platform
    // 6: GCMD Instrument
    private int thesaurusType;
    // 1: Label changed
    // 2: URI not found
    // 3: No equivalent    
    private int warningType;
    private final int recordType;
    private String schemeName;

    public AutoCorrectionWarning() {
        this.recordType = 0;
    }

    public AutoCorrectionWarning(String recordId, String uri,
            String oldLabel, String newLabel, int thesaurusType,
            String schemeName, int warningType, int recordType) {
        this.recordId = recordId;
        this.uri = uri;
        this.oldLabel = oldLabel;
        this.newLabel = newLabel;
        this.thesaurusType = thesaurusType;
        this.warningType = warningType;
        this.recordType = recordType;
        this.schemeName = schemeName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getOldLabel() {
        return oldLabel;
    }

    public void setOldLabel(String oldLabel) {
        this.oldLabel = oldLabel;
    }

    public String getNewLabel() {
        return newLabel;
    }

    public void setNewLabel(String newLabel) {
        this.newLabel = newLabel;
    }

    public int getThesaurusType() {
        return thesaurusType;
    }

    public void setThesaurusType(int thesaurusType) {
        this.thesaurusType = thesaurusType;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public int getWarningType() {
        return warningType;
    }

    public void setWarningType(int warningType) {
        this.warningType = warningType;
    }

//    public String getThesaurusTypeStr() {
//        switch (thesaurusType) {
//            case 1:
//                return "Earth topic";
//            case 2:
//                return "ESA platform";
//            case 3:
//                return "ESA instrument";
//            case 4:
//                return "GCMD science keyword";
//            case 5:
//                return "GCMD platform";
//            case 6:
//                return "GCMD instrument";
//        }
//        //System.out.println("NQMinh: " + thesaurusType);
//        return "";
//    }
    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public int getTabIndex() {
        switch (thesaurusType) {
            case 1:
            case 4:
                return 5;
        }
        return 8;
    }

    public int getRecordType() {
        return recordType;
    }

    public String getKey() {
        return recordId + "#" + uri + "#" + thesaurusType + "#" + warningType;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof AutoCorrectionWarning) {
            AutoCorrectionWarning acw = (AutoCorrectionWarning) obj;
            if (this.getKey().equals(acw.getKey())) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.getKey() != null ? this.getKey().hashCode() : 0);
        return hash;
    }
}
