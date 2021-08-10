/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import be.spacebel.metadataeditor.models.configuration.Thesaurus;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.extensions.model.fluidgrid.FluidGridItem;

/**
 * This class represents gmd:descriptiveKeywords element that originate from a
 * controlled vocabulary described through the thesaurusName property of ISO
 * 19139-2 XML metadata
 *
 * @author mng
 */
public class ThesaurusKeyword implements Serializable {

    private List<Keyword> keywords;
    private List<String> availableValues;
    private String selectedValue;
    private String codeListValue;
//    private boolean ext;
    private Thesaurus thesaurus;
    private final String uuid;

    private List<FluidGridItem> fluidKeywords;

    public ThesaurusKeyword() {
        uuid = UUID.randomUUID().toString();
        fluidKeywords = new ArrayList<>();
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
        if (availableValues != null && !availableValues.isEmpty()) {
            keywords.stream().filter((kw) -> (availableValues.contains(kw.getLabel()))).forEachOrdered((kw) -> {
                availableValues.remove(kw.getLabel());
            });
        }

        keywords.forEach((kw) -> {
            fluidKeywords.add(new FluidGridItem(kw));
        });
    }

    public String getCodeListValue() {
        return codeListValue;
    }

    public void setCodeListValue(String codeListValue) {
        this.codeListValue = codeListValue;
    }

    public void onSelect(final AjaxBehaviorEvent event) {
        if (StringUtils.isNotEmpty(selectedValue)) {
            Keyword newKw = new Keyword();
            String uri = thesaurus.getTitleUri() + codeListValue;
            newKw.setUri(uri);
            newKw.setLabel(selectedValue);
            addKeyword(newKw);
            if (availableValues.contains(selectedValue)) {
                availableValues.remove(selectedValue);
            }
        }
    }

    public void onBlur(final AjaxBehaviorEvent event) {
        if (StringUtils.isNotEmpty(selectedValue)) {
            boolean exist = false;
            if (keywords != null) {
                for (Keyword kw : keywords) {
                    if (selectedValue.equals(kw.getLabel())) {
                        exist = true;
                        break;
                    }
                }
            }

            if (!exist) {
                Keyword newKw = new Keyword();
                String uri = thesaurus.getTitleUri() + codeListValue;
                newKw.setUri(uri);
                newKw.setLabel(selectedValue);
                addKeyword(newKw);
            }
        }
    }

    public void removeKeyword(Keyword kw) {
        if (keywords != null && !keywords.isEmpty()) {
            keywords.remove(kw);
            if (availableValues != null && !availableValues.contains(kw.getLabel())) {
                availableValues.add(kw.getLabel());
            }
        }

        FluidGridItem existingKw = null;
        for (FluidGridItem fluidKw : fluidKeywords) {
            if (kw.equals(fluidKw.getData())) {
                existingKw = fluidKw;
                break;
            }
        }
        if (existingKw != null) {
            fluidKeywords.remove(existingKw);
        }
    }

//    public boolean isExt() {
//        return ext;
//    }
//
//    public void setExt(boolean ext) {
//        this.ext = ext;
//    }
    public String getUuid() {
        return uuid;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getKeywordValues() {
        if (keywords != null) {
            List<String> values = new ArrayList<>();
            for (Keyword kw : keywords) {
                if (StringUtils.isNotEmpty(kw.getLabel())) {
                    values.add(kw.getLabel());
                }
            }
            return StringUtils.join(values, ",");
        }
        return "";
    }

    public List<String> getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(List<String> availableValues) {
        this.availableValues = availableValues;
    }

    public List<SelectItem> getOptions() {
        List<SelectItem> options = new ArrayList<>();
        if (availableValues != null) {
            for (String opt : availableValues) {
                options.add(new SelectItem(opt, opt));
            }
        }
        return options;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

//    public void addEmptyKeyword() {
//        Keyword newKw = new Keyword();
//        String uri = thesaurus.getTitleUri() + codeListValue;
//        newKw.setUri(uri);
//        addKeyword(newKw);
//    }
    
    public void addEmptyKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        String uri = thesaurus.getTitleUri() + codeListValue;
        
        Keyword kw1 = new Keyword();
        kw1.setUri(uri);
        Keyword kw2 = new Keyword();
        kw2.setUri(uri);
        
        keywords.add(kw1);
        keywords.add(kw2);
        
        fluidKeywords.add(new FluidGridItem(kw1));
        fluidKeywords.add(new FluidGridItem(kw2));               
    }

    public void addKeyword(Keyword kw) {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        keywords.add(kw);

        if (availableValues != null && availableValues.contains(kw.getLabel())) {
            availableValues.remove(kw.getLabel());

        }
        if (StringUtils.isNotEmpty(kw.getLabel())) {
            boolean existing = false;
            for (FluidGridItem fluidKw : fluidKeywords) {
                if (kw.equals(fluidKw.getData())) {
                    existing = true;
                    break;
                }
            }
            if (!existing) {
                fluidKeywords.add(new FluidGridItem(kw));
            }
        }
    }

    public List<FluidGridItem> getFluidKeywords() {
        return fluidKeywords;
    }

    public void setFluidKeywords(List<FluidGridItem> fluidKeywords) {
        this.fluidKeywords = fluidKeywords;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ThesaurusKeyword)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUuid().equals(((ThesaurusKeyword) obj).getUuid());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUuid());
        return hash;
    }
}
