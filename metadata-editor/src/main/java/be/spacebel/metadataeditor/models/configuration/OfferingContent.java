/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 * This class represents an content entry of an offering element in the
 * offerings.xml configuration file
 *
 * @author mng
 */
public class OfferingContent implements Serializable {

    private final String uuid;
    private String protocol;
    private String function;
    private String serviceType;

    private String mimeType;

    private List<ParameterOption> requiredExtFields;
    private List<ParameterOption> optionalExtFields;

    public OfferingContent() {
        this.uuid = UUID.randomUUID().toString();
    }

    public OfferingContent(String type, String protocol, String function, String serviceType) {
        this.mimeType = type;
        this.protocol = protocol;
        this.function = function;
        this.serviceType = serviceType;
        this.uuid = UUID.randomUUID().toString();
    }

    public OfferingContent(OfferingContent newOfferingContent) {
        this.protocol = newOfferingContent.getProtocol();
        this.function = newOfferingContent.getFunction();
        this.serviceType = newOfferingContent.getServiceType();
        this.mimeType = newOfferingContent.getMimeType();

        this.uuid = UUID.randomUUID().toString();

        if (newOfferingContent.getRequiredExtFields() != null) {
            requiredExtFields = new ArrayList<>();
            newOfferingContent.getRequiredExtFields().forEach((newOption) -> {
                requiredExtFields.add(new ParameterOption("", newOption.getLabel()));
            });
        }

        if (newOfferingContent.getOptionalExtFields() != null) {
            optionalExtFields = new ArrayList<>();
            newOfferingContent.getOptionalExtFields().forEach((newOption) -> {
                optionalExtFields.add(new ParameterOption("", newOption.getLabel()));
            });
        }
    }

    public String getUuid() {
        return uuid;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<ParameterOption> getRequiredExtFields() {
        return requiredExtFields;
    }

    public void setRequiredExtFields(List<ParameterOption> requiredExtFields) {
        this.requiredExtFields = requiredExtFields;
    }

    public List<ParameterOption> getOptionalExtFields() {
        return optionalExtFields;
    }

    public void setOptionalExtFields(List<ParameterOption> optionalExtFields) {
        this.optionalExtFields = optionalExtFields;
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<content>");
        if (StringUtils.isNotEmpty(mimeType)) {
            sb.append("<type>").append(XmlUtils.escapeXml(mimeType)).append("</type>");
        }

        if (requiredExtFields != null) {
            requiredExtFields.stream().filter((option) -> (StringUtils.isNotEmpty(option.getValue()))).forEachOrdered((option) -> {
                sb.append("<").append(option.getLabel()).append(">")
                        .append(XmlUtils.escapeXml(option.getValue()))
                        .append("</").append(option.getLabel()).append(">");
            });
        }
        if (optionalExtFields != null) {
            optionalExtFields.stream().filter((option) -> (StringUtils.isNotEmpty(option.getValue()))).forEachOrdered((option) -> {
                sb.append("<").append(option.getLabel()).append(">")
                        .append(XmlUtils.escapeXml(option.getValue()))
                        .append("</").append(option.getLabel()).append(">");
            });
        }

        sb.append("</content>");
        return sb.toString();
    }

    public JSONObject toJsonObject() {
        JSONObject contentObj = new JSONObject();
        if (StringUtils.isNotEmpty(mimeType)) {
            contentObj.put("type", mimeType);
        }
        if (requiredExtFields != null) {
            requiredExtFields.stream().filter((option) -> (StringUtils.isNotEmpty(option.getValue()))).forEachOrdered((option) -> {
                contentObj.put(option.getLabel(), option.getValue());
            });
        }
        if (optionalExtFields != null) {
            optionalExtFields.stream().filter((option) -> (StringUtils.isNotEmpty(option.getValue()))).forEachOrdered((option) -> {
                contentObj.put(option.getLabel(), option.getValue());
            });
        }
        return contentObj;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OfferingContent)) {
            return false;
        }
        OfferingContent other = (OfferingContent) obj;
        return this.getUuid().equals(other.getUuid());
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }
}
