/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a data model to be used to store thesaurus change warnings to build
 * thesaurus change report of a metadata record
 *
 * @author mng
 */
public class AutoCorrectionWarningReport implements Serializable {

    private List<AutoCorrectionWarning> warnEnties;
    private List<String> recordIds;
    private List<String> thesaurusType;

    public AutoCorrectionWarningReport() {
    }

    public void addWarn(AutoCorrectionWarning warn) {
        if (this.warnEnties == null) {
            this.warnEnties = new ArrayList<>();
        }
        
        if (!this.warnEnties.contains(warn)) {
            if (recordIds == null) {
                recordIds = new ArrayList<>();
            }
            this.warnEnties.add(warn);
            if (!recordIds.contains(warn.getRecordId())) {
                recordIds.add(warn.getRecordId());
            }

            String thType = warn.getSchemeName();
            if (thesaurusType == null) {
                thesaurusType = new ArrayList<>();
            }
            if (!thesaurusType.contains(thType)) {
                thesaurusType.add(thType);
            }
        }
    }

    public List<AutoCorrectionWarning> getWarnEnties() {
        return warnEnties;
    }

    public List<String> getRecordIds() {
        return recordIds;
    }

    public void setRecordIds(List<String> recordIds) {
        this.recordIds = recordIds;
    }

    public List<String> getThesaurusType() {
        return thesaurusType;
    }

    public void setThesaurusType(List<String> thesaurusType) {
        this.thesaurusType = thesaurusType;
    }

    public void reset() {
        this.warnEnties = null;
        this.recordIds = null;
        this.thesaurusType = null;
    }

}
