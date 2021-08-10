/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.faces.model.SelectItem;
import org.apache.log4j.Logger;
import org.primefaces.model.TreeNode;

/**
 * This is a model class provides data and functionalities of editor form
 *
 * @author mng
 */
public class EditorForm {

    private final Logger log = Logger.getLogger(getClass());

    private final Configuration config;
    

    private final List<SelectItem> restrictionCodes;
    private final List<SelectItem> dateTypeCodeValues;
    private final List<SelectItem> roleCodes;

    private String restrictionDesc;

    //private IndexSearcher earthtopicsSearcher;
    private ParameterOption selectedEarthtopic;

    //private IndexSearcher platformsSearcher;
    private ParameterOption selectedPlatform;

    private TreeNode selectedEarthtopicNode;
    private TreeNode selectedPlatformNode;

    private final List<SelectItem> spatialDataServiceCategories;

    public EditorForm(Configuration newConfig) throws IOException {
        this.config = newConfig;        

        /*
         create SelectItem list of restriction codes
         */
        Set<String> keys = config.getRestrictionCodes().keySet();
        restrictionCodes = new ArrayList<>();
        keys.forEach((key) -> {
            restrictionCodes.add(new SelectItem(key, key));
        });

        dateTypeCodeValues = new ArrayList<>();
        config.getDateTypeValues().forEach((value) -> {
            dateTypeCodeValues.add(new SelectItem(value, value));
        });

        roleCodes = new ArrayList<>();
        config.getRoleCodes().forEach((value) -> {
            roleCodes.add(new SelectItem(value, value));
        });

        spatialDataServiceCategories = new ArrayList<>();
        if (config.getSpatialDataServiceCategories() != null) {
            ArrayList<String> list = new ArrayList<>();
            config.getSpatialDataServiceCategories().forEach((k, v) -> {
                list.add(v);
            });
            Collections.sort(list);
            list.forEach((cat) -> {
                spatialDataServiceCategories.add(new SelectItem(cat, cat));
            });
        }
    }  

//    public List<String> useLimitationCompleteValue(String query) {
//        log.debug("useLimitationCompleteValue( query = " + query + ")");
//        return completeValue(config.getUseLimitations(), query);
//    }
//    public List<String> onlineRsProtocolCompleteValue(String query) {
//        log.debug("onlineRsProtocolCompleteValue( query = " + query + ")");
//        return completeValue(config.getOnlineRsProtocols(), query);
//    }
//    
//    public List<String> onlineRsAppProfilesCompleteValue(String query) {
//        log.debug("onlineRsAppProfilesCompleteValue( query = " + query + ")");
//        return completeValue(config.getOnlineRsAppProfiles(), query);
//    }
//    private List<String> completeValue(List<String> values, String query) {
//        if (StringUtils.isNotEmpty(query)) {
//            List<String> filteredValues = new ArrayList<>();
//            values.stream().filter((str) -> (str.toLowerCase().startsWith(query.toLowerCase())
//                    || StringUtils.getLevenshteinDistance(str.toLowerCase(), query.toLowerCase()) <= 2)).forEachOrdered((str) -> {
//                filteredValues.add(str);
//            });
//            return filteredValues;
//        } else {
//            return values;
//        }
//    }
    ////////////////////////////////////////////////////////////////////
    public List<SelectItem> getRestrictionCodes() {
        return restrictionCodes;
    }

    public List<SelectItem> getDateTypeCodeValues() {
        return dateTypeCodeValues;
    }

    public String getRestrictionDesc(String code) {
        restrictionDesc = config.getRestrictionCodes().get(code);
        return restrictionDesc;
    }

    public ParameterOption getSelectedEarthtopic() {
        return selectedEarthtopic;
    }

    public void setSelectedEarthtopic(ParameterOption selectedEarthtopic) {
        this.selectedEarthtopic = selectedEarthtopic;
    }

    public ParameterOption getSelectedPlatform() {
        return selectedPlatform;
    }

    public void setSelectedPlatform(ParameterOption selectedPlatform) {
        this.selectedPlatform = selectedPlatform;
    }

    public String getRestrictionDesc() {
        return restrictionDesc;
    }

    public void setRestrictionDesc(String restrictionDesc) {
        this.restrictionDesc = restrictionDesc;
    }

    public List<SelectItem> getRoleCodes() {
        return roleCodes;
    }

    public List<SelectItem> getSpatialDataServiceCategories() {
        return spatialDataServiceCategories;
    }

    public TreeNode getEarthtopicTreeNode() {
        return config.getEarthtopicsTreeNode();
    }

    public TreeNode getPlatformTreeNode() {
        return config.getPlatformTreeNode();
    }

    public TreeNode getSelectedEarthtopicNode() {
        return selectedEarthtopicNode;
    }

    public void setSelectedEarthtopicNode(TreeNode selectedEarthtopicNode) {
        this.selectedEarthtopicNode = selectedEarthtopicNode;
    }

    public TreeNode getSelectedPlatformNode() {
        return selectedPlatformNode;
    }

    public void setSelectedPlatformNode(TreeNode selectedPlatformNode) {
        this.selectedPlatformNode = selectedPlatformNode;
    }

}
