/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

/**
 * This is a data model class to be used to store warnings such as: concept URI
 * not found, an instrument is no longer hosted by a platform,... caused by a
 * newer version of the thesauri. The warnings can only be corrected by the user
 * using the edit form
 *
 * @author mng
 */
public class ManualCorrectionWarning implements Serializable {

    private String recordId;
    private String uri;
    private String label;
    // 1: Platform
    // 2: Instrument   
    private int thesaurusType;
    private String schemeName;

    /**
     * The two fields below are used in case of an instrument is not hosted by a
     * platform
     */
    private String hostedUri;
    private String hostedLabel;
    private final int recordType;

    public ManualCorrectionWarning() {
        recordType = 0;
    }

    public ManualCorrectionWarning(String recordId, String uri, String label, int thesaurusType, int recordType, String schemeName) {
        this.recordId = recordId;
        this.uri = uri;
        this.label = label;
        this.thesaurusType = thesaurusType;
        this.recordType = recordType;
        this.schemeName = schemeName;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
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

    public int getThesaurusType() {
        return thesaurusType;
    }

    public void setThesaurusType(int thesaurusType) {
        this.thesaurusType = thesaurusType;
    }

    public String getHostedUri() {
        return hostedUri;
    }

    public void setHostedUri(String hostedUri) {
        this.hostedUri = hostedUri;
    }

    public String getHostedLabel() {
        return hostedLabel;
    }

    public void setHostedLabel(String hostedLabel) {
        this.hostedLabel = hostedLabel;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

//    public String getThesaurusTypeStr() {
//        switch (thesaurusType) {
//            case 1:
//                return "Platform";
//            case 2:
//                return "Instrument";
//            case 3:
//                return "GCMD Platform";
//            case 4:
//                return "GCMD Instrument";
//        }
//        System.out.println("NQMinh: " + thesaurusType);
//        return "";
//    }
    public String getWarningMsg() {
        if (StringUtils.isNotEmpty(hostedUri) || StringUtils.isNotEmpty(hostedLabel)) {
            return "The instrument is not hosted by the platform " + hostedLabel + "(" + hostedUri + ")";
        } else {
            return "The URI does not exist in the " + this.schemeName + " thesaurus";
        }
    }

    public int getRecordType() {
        return recordType;
    }

    public String getKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(recordId);
        sb.append("#");
        sb.append(uri);
        sb.append("#");
        sb.append(thesaurusType);
        if (StringUtils.isNotEmpty(hostedUri)) {
            sb.append("#").append(hostedUri);
        }
        if (StringUtils.isNotEmpty(hostedLabel)) {
            sb.append("#").append(hostedLabel);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof ManualCorrectionWarning) {
            ManualCorrectionWarning mcw = (ManualCorrectionWarning) obj;
            if (this.getKey().equals(mcw.getKey())) {
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
