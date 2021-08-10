/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class represents an offering element in the offerings.xml configuration
 * file
 *
 * @author mng
 */
public class Offering implements Serializable {

    private final String uuid;
    private String code;

    private Map<String, OfferingOperation> availableOperations;
    private List<SelectItem> availableOperationCodes;
    private String selectedOperationCode;
    private List<OfferingOperation> operations;

    private Map<String, OfferingContent> availableContents;
    private String selectedContent;
    private List<SelectItem> availableContentCodes;
    private List<OfferingContent> contents;

    public Offering() {
        uuid = UUID.randomUUID().toString();
    }

    public Offering(Offering newOffering) {
        this.code = newOffering.getCode();
        if (newOffering.getAvailableOperations() != null) {
            this.availableOperations = new ConcurrentHashMap<>();
            this.availableOperationCodes = new ArrayList<>();

            newOffering.getAvailableOperations().entrySet().forEach((entry) -> {
                this.availableOperations.putIfAbsent(entry.getKey(), new OfferingOperation(entry.getValue()));
                this.availableOperationCodes.add(new SelectItem(entry.getKey(), entry.getKey()));
            });
        }

        if (newOffering.getAvailableContents() != null) {
            this.availableContents = new ConcurrentHashMap<>();
            this.availableContentCodes = new ArrayList<>();

            newOffering.getAvailableContents().entrySet().forEach((entry) -> {
                this.availableContents.putIfAbsent(entry.getKey(), new OfferingContent(entry.getValue()));
                this.availableContentCodes.add(new SelectItem(entry.getKey(), entry.getKey()));
            });
        }
        uuid = UUID.randomUUID().toString();
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

    public boolean hasMultiContents() {
        return (availableContents != null && availableContents.size() > 1);
    }

    public boolean hasOneContent() {
        return (availableContents != null && availableContents.size() == 1);
    }

    public boolean hasAvailableContentCodes() {
        return (availableContentCodes != null && availableContentCodes.size() > 0);
    }

    public boolean hasAvailableOperationCodes() {
        return (availableOperationCodes != null && availableOperationCodes.size() > 0);
    }

    public void addAvailableOperation(OfferingOperation operation) {
        if (availableOperations == null) {
            availableOperations = new ConcurrentHashMap<>();
        }
        availableOperations.putIfAbsent(operation.getCode(), operation);
    }

    public void addOperation(final AjaxBehaviorEvent event) {
        if (StringUtils.isNotEmpty(selectedOperationCode)) {
            addOperation(getAvailableOperation(selectedOperationCode));
        }
    }

    public void addOperation(OfferingOperation operation) {
        if (operations == null) {
            operations = new ArrayList<>();
        }
        operations.add(operation);

        if (availableOperationCodes != null) {
            for (SelectItem item : availableOperationCodes) {
                if (item.getValue().equals(operation.getCode())) {
                    availableOperationCodes.remove(item);
                    break;
                }
            }
        }
    }

    public void removeOperation(OfferingOperation operation) {
        if (operations != null && operations.size() > 0) {
            operations.remove(operation);
            availableOperationCodes.add(new SelectItem(operation.getCode(), operation.getCode()));
        }
    }

    public void addAvailableContent(String contentId, OfferingContent content) {
        if (availableContents == null) {
            availableContents = new ConcurrentHashMap<>();
        }
        availableContents.putIfAbsent(contentId, content);
    }

    public void addFirstContent() {
        if (contents == null) {
            contents = new ArrayList<>();
        }
        contents.add(getAvailableContent("001"));
    }

    public void addContent(final AjaxBehaviorEvent event) {
        if (StringUtils.isNotEmpty(selectedContent)) {

            addContent(getAvailableContent(selectedContent));

            for (SelectItem item : availableContentCodes) {
                if (item.getValue().equals(selectedContent)) {
                    availableContentCodes.remove(item);
                    break;
                }
            }
        }
    }

    public void addContent(OfferingContent content) {
        if (contents == null) {
            contents = new ArrayList<>();
        }
        contents.add(content);
    }

    public void removeContent(OfferingContent content) {
        if (contents != null && contents.size() > 0) {
            contents.remove(content);
        }
    }

    public List<SelectItem> getOperationCodes() {
        List<SelectItem> items = new ArrayList<>();

        if (availableOperations != null) {
            availableOperations.entrySet().forEach((entry) -> {
                items.add(new SelectItem(entry.getKey(), entry.getKey()));
            });
        }
        return items;
    }

    public List<SelectItem> getContentIds() {
        List<SelectItem> items = new ArrayList<>();

        if (availableContents != null) {
            availableContents.entrySet().forEach((entry) -> {
                items.add(new SelectItem(entry.getKey(), entry.getKey()));
            });
        }
        return items;
    }

    public OfferingOperation getAvailableOperation(String operationCode) {
        if (availableOperations != null
                && availableOperations.containsKey(operationCode)) {
            return new OfferingOperation(availableOperations.get(operationCode));
        }
        return new OfferingOperation();
    }

    public OfferingContent getAvailableContent(String contentId) {
        if (availableContents != null && availableContents.containsKey(contentId)) {
            return new OfferingContent(availableContents.get(contentId));
        }
        return new OfferingContent();
    }

    public List<SelectItem> getAvailableOperationCodes() {
        return availableOperationCodes;
    }

    public void setAvailableOperationCodes(List<SelectItem> availableOperationCodes) {
        this.availableOperationCodes = availableOperationCodes;
    }

    public Map<String, OfferingOperation> getAvailableOperations() {
        return availableOperations;
    }

    public void setAvailableOperations(Map<String, OfferingOperation> availableOperations) {
        this.availableOperations = availableOperations;
    }

    public Map<String, OfferingContent> getAvailableContents() {
        return availableContents;
    }

    public void setAvailableContents(Map<String, OfferingContent> availableContents) {
        this.availableContents = availableContents;
    }

    public String getSelectedOperationCode() {
        return selectedOperationCode;
    }

    public void setSelectedOperationCode(String selectedOperationCode) {
        this.selectedOperationCode = selectedOperationCode;
    }

    public List<OfferingOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<OfferingOperation> operations) {
        this.operations = operations;
    }

    public String getSelectedContent() {
        return selectedContent;
    }

    public void setSelectedContent(String selectedContent) {
        this.selectedContent = selectedContent;
    }

    public List<SelectItem> getAvailableContentCodes() {
        return availableContentCodes;
    }

    public void setAvailableContentCodes(List<SelectItem> availableContentCodes) {
        this.availableContentCodes = availableContentCodes;
    }

    public List<OfferingContent> getContents() {
        return contents;
    }

    public void setContents(List<OfferingContent> contents) {
        this.contents = contents;
    }

    public String toXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<offering>");
        sb.append("<code>").append(code).append("</code>");
        if (operations != null && operations.size() > 0) {
            sb.append("<operations>");
            operations.forEach((oper) -> {
                sb.append(oper.toXml());
            });
            sb.append("</operations>");
        }

        if (contents != null && contents.size() > 0) {
            sb.append("<contents>");
            contents.forEach((content) -> {
                sb.append(content.toXml());
            });
            sb.append("</contents>");
        }
        sb.append("</offering>");
        return sb.toString();
    }

    public JSONObject toJsonObject() {
        JSONObject offeringObj = new JSONObject();
        offeringObj.put("type", "Offering");
        offeringObj.put("code", code);

        if (operations != null && operations.size() > 0) {
            JSONArray operationArray = new JSONArray();
            operations.forEach((oper) -> {
                operationArray.put(oper.toJsonObject());
            });
            offeringObj.put("operations", operationArray);
        }

        if (contents != null && contents.size() > 0) {
            JSONArray contentArray = new JSONArray();
            contents.forEach((content) -> {
                contentArray.put(content.toJsonObject());
            });
            offeringObj.put("contents", contentArray);
        }

        return offeringObj;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Offering)) {
            return false;
        }
        Offering other = (Offering) obj;
        return this.getUuid().equals(other.getUuid());
    }

    @Override
    public int hashCode() {
        return this.getUuid().hashCode();
    }
}
