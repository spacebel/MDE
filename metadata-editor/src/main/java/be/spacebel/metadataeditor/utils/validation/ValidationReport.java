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
 * This is a data model to be used to store validation information to build
 * validation report of a metadata record
 *
 * @author mng
 */
public class ValidationReport implements Serializable {

    private int total;
    private int errors;
    private int warnings;
    private List<ValidationError> entries;
    private List<String> recordIds;
    private List<String> status;
    private List<String> formats;
    private boolean all;    

    public boolean isAll() {
        return all;
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public int getValid() {
        return (total - (errors + warnings));
    }

    public List<ValidationError> getEntries() {
        return entries;
    }

    public void addEntry(ValidationStatus vStatus) {
        if (entries == null) {
            entries = new ArrayList<>();
        }

        if (recordIds == null) {
            recordIds = new ArrayList<>();
        }

        if (status == null) {
            status = new ArrayList<>();
        }

        if (formats == null) {
            formats = new ArrayList<>();
        }

        this.total += 1;
        if (!vStatus.isValid()) {
            if (vStatus.isError()) {
                this.errors += 1;
            } else {

                this.warnings += 1;
            }
            vStatus.getMessages().forEach((message) -> {
                message.setRecordId(vStatus.getRecordId());
                entries.add(message);
                if (!recordIds.contains(vStatus.getRecordId())) {
                    recordIds.add(vStatus.getRecordId());
                }

                String s = message.getStatus();
                if (!status.contains(s)) {
                    status.add(s);
                }

                String f = message.getFormat();
                if (!formats.contains(f)) {
                    formats.add(f);
                }
            });
        }
    }

    public List<String> getRecordIds() {
        return recordIds;
    }

    public List<String> getStatus() {
        return status;
    }

    public List<String> getFormats() {
        return formats;
    }

}
