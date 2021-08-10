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
 * This class represents an operation of an offering element in the
 * offerings.xml configuration file
 *
 * @author mng
 */
public class OfferingOperation implements Serializable {

    private final String uuid;
    private String code;
    private String protocol;
    private String function;
    private String serviceType;
    private String method;
    private String mimeType;

    private List<ParameterOption> requiredExtFields;
    private List<ParameterOption> optionalExtFields;

    public OfferingOperation() {
        this.uuid = UUID.randomUUID().toString();

    }

    public OfferingOperation(String code, String method, String type, String protocol, String function, String serviceType) {
        this.code = code;
        this.method = method;
        this.mimeType = type;
        this.protocol = protocol;
        this.function = function;
        this.serviceType = serviceType;
        this.uuid = UUID.randomUUID().toString();

    }

    public OfferingOperation(OfferingOperation newOfferingOperation) {
        this.code = newOfferingOperation.getCode();
        this.method = newOfferingOperation.getMethod();
        this.mimeType = newOfferingOperation.getMimeType();

        this.protocol = newOfferingOperation.getProtocol();
        this.function = newOfferingOperation.getFunction();
        this.serviceType = newOfferingOperation.getServiceType();
        this.uuid = UUID.randomUUID().toString();

        if (newOfferingOperation.getRequiredExtFields() != null) {
            requiredExtFields = new ArrayList<>();
            newOfferingOperation.getRequiredExtFields().forEach((newOption) -> {
                requiredExtFields.add(new ParameterOption("", newOption.getLabel()));
            });
        }

        if (newOfferingOperation.getOptionalExtFields() != null) {
            optionalExtFields = new ArrayList<>();
            newOfferingOperation.getOptionalExtFields().forEach((newOption) -> {
                optionalExtFields.add(new ParameterOption("", newOption.getLabel()));
            });
        }
    }

    public String getUuid() {
        return uuid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public String getUrl() {
        if (requiredExtFields != null) {
            for (ParameterOption option : requiredExtFields) {
                if ("href".equalsIgnoreCase(option.getLabel())
                        || "url".equalsIgnoreCase(option.getLabel())) {
                    return option.getValue();
                }
            }
        }
        return "";
    }

    public ParameterOption findUrl() {
        for (ParameterOption option : requiredExtFields) {
            if ("href".equalsIgnoreCase(option.getLabel())
                    || "url".equalsIgnoreCase(option.getLabel())) {
                return option;
            }
        }
        return null;
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<operation>");
        sb.append("<code>").append(code).append("</code>");
        sb.append("<method>").append(method).append("</method>");
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
        sb.append("</operation>");

        return sb.toString();
    }

    public JSONObject toJsonObject() {
        JSONObject operation = new JSONObject();
        operation.put("code", code);
        operation.put("method", method);
        if (StringUtils.isNotEmpty(mimeType)) {
            operation.put("type", mimeType);
        }
        if (requiredExtFields != null) {
            requiredExtFields.stream().filter((option) -> (StringUtils.isNotEmpty(option.getValue()))).forEachOrdered((option) -> {
                operation.put(option.getLabel(), option.getValue());
            });
        }
        if (optionalExtFields != null) {
            optionalExtFields.stream().filter((option) -> (StringUtils.isNotEmpty(option.getValue()))).forEachOrdered((option) -> {
                operation.put(option.getLabel(), option.getValue());
            });
        }

        return operation;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OfferingOperation)) {
            return false;
        }
        OfferingOperation other = (OfferingOperation) obj;
        return this.getUuid().equals(other.getUuid());
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }

}
