/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.faces.event.AjaxBehaviorEvent;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.extensions.model.fluidgrid.FluidGridItem;

/**
 * This class represents gmd:descriptiveKeywords element (that is not belonging
 * to a thesaurus) of ISO 19139-2 XML metadata
 *
 * @author mng
 */
public class FreeKeyword implements Serializable {

    private List<Keyword> keywords;
    private String codeListValue;
    private String selectedValue;
    private final String uuid;
    private List<FluidGridItem> fluidKeywords;

    public FreeKeyword() {
        uuid = UUID.randomUUID().toString();
        fluidKeywords = new ArrayList<>();
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public List<FluidGridItem> getFluidKeywords() {
        //fluidKeywords.clear();
//        if (keywords != null && keywords.size() > 0) {
//            keywords.forEach((kw) -> {
//                if(StringUtils.isNotEmpty(kw.getLabel())){
//                    fluidKeywords.add(new FluidGridItem(kw));
//                }                
//            });
//        }
        return fluidKeywords;
    }

    public void setFluidKeywords(List<FluidGridItem> fluidKeywords) {
        this.fluidKeywords = fluidKeywords;
    }

//    public List<FluidGridItem> getFluidKeywords() {
//        List<FluidGridItem> fluidKeywords = new ArrayList<>();
//        if (keywords != null) {
//            keywords.forEach((kw) -> {
//                if(StringUtils.isNotEmpty(kw.getLabel())){
//                    fluidKeywords.add(new FluidGridItem(kw));
//                }                
//            });
//        }
//        return fluidKeywords;
//    }
    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;

    }

    public String getCodeListValue() {
        return codeListValue;
    }

    public void setCodeListValue(String codeListValue) {
        this.codeListValue = codeListValue;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

//    public void addEmptyKeyword() {
//        addKeyword(new Keyword());
//    }

    public void addKeyword(Keyword kw) {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        keywords.add(kw);
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
    
    public void addEmptyKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<>();
        }
        Keyword kw1 = new Keyword();
        Keyword kw2 = new Keyword();
        
        keywords.add(kw1);
        keywords.add(kw2);
        
        fluidKeywords.add(new FluidGridItem(kw1));
        fluidKeywords.add(new FluidGridItem(kw2));               
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
                newKw.setLabel(selectedValue);
                addKeyword(newKw);
            }
        }
    }

    public void removeKeyword(Keyword kw) {        
        keywords.remove(kw);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FreeKeyword)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUuid().equals(((FreeKeyword) obj).getUuid());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUuid());
        return hash;
    }
}
