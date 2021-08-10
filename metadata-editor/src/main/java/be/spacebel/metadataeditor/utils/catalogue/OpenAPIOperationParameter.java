/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.catalogue;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents a parameter of an operation in OpenAPI document
 *
 * @author mng
 */
public class OpenAPIOperationParameter {

    private String name;
    private String position;
    private boolean required;
    private String osToken;
    private String defaultValue;
    private List<String> possibleValues;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getOsToken() {
        return osToken;
    }

    public void setOsToken(String osToken) {
        this.osToken = osToken;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parameter[");
        sb.append("name = ").append(name);
        sb.append("; position = ").append(position);
        sb.append("; required = ").append(required);
        sb.append("; osToken = ").append(osToken);
        sb.append("; defaultValue = ").append(defaultValue);
        if (possibleValues != null && !possibleValues.isEmpty()) {
            sb.append("; possibleValues = ").append(StringUtils.join(possibleValues, ","));
        }
        sb.append("]");
        return sb.toString();
    }

}
