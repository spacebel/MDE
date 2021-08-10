/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.jsf;

import be.spacebel.metadataeditor.models.configuration.Concept;
import be.spacebel.metadataeditor.models.workspace.Identification;
import be.spacebel.metadataeditor.models.workspace.identification.ThesaurusKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.FreeKeyword;
import be.spacebel.metadataeditor.utils.parser.MetadataUtils;
import be.spacebel.metadataeditor.utils.CommonUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.faces.context.FacesContext;

/**
 * This class implements custom JSF functions.
 *
 * @author mng
 */
public class CustomFunctions {

    private final TimeZone timeZone;

    public CustomFunctions() {
        FacesContext context = FacesContext.getCurrentInstance();
        Locale locale = context.getExternalContext().getRequestLocale();
        Calendar calendar = Calendar.getInstance(locale);
        timeZone = calendar.getTimeZone();
    }

//    public boolean hasProperties(SearchResultItem item, String properties) {
//        if (item == null || item.getProperties() == null || StringUtils.isEmpty(properties)) {
//            return false;
//        }
//        String[] listProps = properties.split(",");
//        boolean has = false;
//        for (String prop : listProps) {
//            SearchResultProperty srProp = item.getProperties().get(prop);
//            if (srProp != null) {
//                String value = srProp.getValue();
//                if (StringUtils.isNotEmpty(value)) {
//                    has = true;
//                    break;
//                }
//            }
//        }
//        return has;
//    }
//
//    public List<String> iteratePropertyValues(SearchResultItem item, String propName) {
//        List<String> values = new ArrayList<>();
//
//        if (item == null || item.getProperties() == null) {
//            return values;
//        }
//
//        int count = 1;
//        while (true) {
//            String name = propName + count;
//            SearchResultProperty srProp = item.getProperties().get(name);
//            if (srProp != null) {
//                String value = srProp.getValue();
//                if (StringUtils.isNotEmpty(value)) {
//                    values.add(value);
//                }
//                count++;
//            } else {
//                break;
//            }
//        }
//        return values;
//    }
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public String getProperty(Concept concept, String propName) {
        if (concept != null) {
            return concept.getPropertyValue(propName);
        }
        return "";
    }

    public String getGcmdVariable(Concept concept) {
        return MetadataUtils.getGcmdLabel(concept);
    }

    public String getScKwLabel(Concept concept) {
        return MetadataUtils.buildScKwLabel(concept);
    }

    public boolean hasKeyword(Identification identification) {
        if (identification.getOrbitType().getKeywords() != null
                && !identification.getOrbitType().getKeywords().isEmpty()) {
            return true;
        }

        if (identification.getWaveLength().getKeywords() != null
                && !identification.getWaveLength().getKeywords().isEmpty()) {
            return true;
        }

        if (identification.getProcessorVersion().getKeywords() != null
                && !identification.getProcessorVersion().getKeywords().isEmpty()) {
            return true;
        }

        if (identification.getResolution().getKeywords() != null
                && !identification.getResolution().getKeywords().isEmpty()) {
            return true;
        }

        if (identification.getProductType().getKeywords() != null
                && !identification.getProductType().getKeywords().isEmpty()) {
            return true;
        }

        if (identification.getOrbitHeight().getKeywords() != null
                && !identification.getOrbitHeight().getKeywords().isEmpty()) {
            return true;
        }

        if (identification.getSwathWidth().getKeywords() != null
                && !identification.getSwathWidth().getKeywords().isEmpty()) {
            return true;
        }

        if (identification.getEarthTopics() != null
                && !identification.getEarthTopics().isEmpty()) {
            return true;
        }

        if (identification.getFreeKeyword() != null
                && identification.getFreeKeyword().getKeywords() != null
                && !identification.getFreeKeyword().getKeywords().isEmpty()) {
            return true;
        }

//        if ((identification.getFreeKeywords() != null && !identification.getFreeKeywords().isEmpty())
//                || (identification.getFreeKeyword() != null && identification.getFreeKeyword().getKeywords() != null && !identification.getFreeKeyword().getKeywords().isEmpty())) {
//            return true;
//        }
        return (identification.getPlaceKeyword() != null
                && identification.getPlaceKeyword().getKeywords() != null
                && !identification.getPlaceKeyword().getKeywords().isEmpty());

    }

    public boolean hasEopKeyword(ThesaurusKeyword eopKw) {
        return (eopKw.getKeywords() != null
                && !eopKw.getKeywords().isEmpty());
    }

    public boolean hasKeyword(FreeKeyword freeKw) {
        return CommonUtils.hasKeyword(freeKw);
    }

    public String dateToStr(Date date) {
        return CommonUtils.dateToStr(date);
    }
}
