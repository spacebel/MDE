/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.catalogue;

import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.utils.HttpInvoker;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class handling OpenAPI Document
 * 
 * @author mng
 */
public class OpenAPIHandler {

    private static final Logger log = Logger.getLogger(OpenAPIHandler.class);
    
//    public static void main(String[] args) throws Exception {
//        
//        Catalogue catalogue = new Catalogue("https://spb-kube-ergo-master.spb.spacebel.be/eo-catalogue/api", "", "", true);
//        loadCatalogueInfo(catalogue);
//    }

    public static void loadCatalogueInfo(Catalogue catalogue) throws IOException {
//        log.debug("getCatalogueInfo(openAPIUrl = " + catalogue.getOpenApiUrl() + ")");
//        Map<String, String> details = new HashMap<>();
//        String openAPIContent = HttpInvoker.httpGET(catalogue.getOpenApiUrl(), details);
//
//        if (details.get(Constants.HTTP_GET_DETAILS_ERROR_CODE) != null) {
//            throw new IOException("The OpenAPI URL " + catalogue.getOpenApiUrl() + " is inaccessible!");
//        } else {
//            if (StringUtils.isNotEmpty(openAPIContent)) {
//                JsonElement jsonElement = new JsonParser().parse(openAPIContent);
//                if (jsonElement.isJsonObject()) {
//                    JsonObject jsonObject = jsonElement.getAsJsonObject();
//                    JsonElement serversElem = jsonObject.get("servers");
//                    if (serversElem != null && serversElem.isJsonArray()) {
//                        JsonArray serversArray = serversElem.getAsJsonArray();
//                        for (JsonElement childElem : serversArray) {
//                            if (childElem.isJsonObject()) {
//                                String url = getAsString(childElem.getAsJsonObject(), "url");
//                                catalogue.setServerUrl(url);
//                            }
//                            break;
//                        }
//                    }
//
//                    JsonElement pathsElem = jsonObject.get("paths");
//                    if (pathsElem != null && pathsElem.isJsonObject()) {
//                        JsonObject pathsObject = pathsElem.getAsJsonObject();
//                        Set<Map.Entry<String, JsonElement>> entries = pathsObject.entrySet();
//                        for (Map.Entry<String, JsonElement> entry : entries) {
//                            String path = entry.getKey();
//                            if (entry.getValue().isJsonObject()) {
//                                JsonObject pathObject = entry.getValue().getAsJsonObject();
//
//                                JsonElement httpPostElem = pathObject.get("post");
//                                if (isExpectedTag(httpPostElem, "Series")) {
//                                    catalogue.setInsertUrl(catalogue.getServerUrl() + path);
//                                }
//
//                                JsonElement httpGetElem = pathObject.get("get");
//                                // get OSDD URL
//                                if (isExpectedTag(httpGetElem, "APIDefinition")) {
//                                    List<OpenAPIOperationParameter> parameters = getOperationParameters(httpGetElem);
//                                    if (!parameters.isEmpty()) {
//                                        for (OpenAPIOperationParameter param : parameters) {
//                                            if (param.getPossibleValues() != null
//                                                    && param.getPossibleValues().contains("application/opensearchdescription+xml")) {
//                                                String osddUrl = catalogue.getServerUrl() + path + "?" + param.getName() + "=application%2Fopensearchdescription%2Bxml";
//                                                catalogue.setOsddUrl(osddUrl);
//                                            }
//                                        }
//                                    }
//                                }
//
//                                // get Present URL
//                                if (isExpectedTag(httpGetElem, "aSeries")) {
//                                    String presentUrl = catalogue.getServerUrl() + path;
//                                    List<OpenAPIOperationParameter> parameters = getOperationParameters(httpGetElem);
//                                    if (!parameters.isEmpty()) {
//                                        String queryString = "";
//                                        for (OpenAPIOperationParameter param : parameters) {
//                                            if (param.isRequired()) {
//                                                if (param.getPosition().equalsIgnoreCase("path")) {
//                                                    presentUrl = presentUrl.replaceAll("\\{" + param.getName() + "\\}", param.getOsToken());
//                                                } else {
//                                                    if (queryString.isEmpty()) {
//                                                        queryString += "?";
//                                                    } else {
//                                                        queryString += "&";
//                                                    }
//                                                    queryString += param.getName() + "=" + param.getOsToken();
//                                                }
//                                            }
//                                        }
//                                        presentUrl += queryString;
//                                    }
//                                    catalogue.setPresentUrl(presentUrl);
//                                }
//
//                                JsonElement httpDeleteElem = pathObject.get("delete");
//                                if (isExpectedTag(httpDeleteElem, "aSeries")) {
//                                    String deleteUrl = catalogue.getServerUrl() + path;
//                                    List<OpenAPIOperationParameter> parameters = getOperationParameters(httpGetElem);
//                                    if (!parameters.isEmpty()) {
//                                        String queryString = "";
//                                        for (OpenAPIOperationParameter param : parameters) {
//                                            if (param.isRequired()) {
//                                                if (param.getPosition().equalsIgnoreCase("path")) {
//                                                    deleteUrl = deleteUrl.replaceAll("\\{" + param.getName() + "\\}", param.getOsToken());
//                                                } else {
//                                                    if (queryString.isEmpty()) {
//                                                        queryString += "?";
//                                                    } else {
//                                                        queryString += "&";
//                                                    }
//                                                    queryString += param.getName() + "=" + param.getOsToken();
//                                                }
//                                            }
//                                        }
//                                        deleteUrl += queryString;
//                                    }
//                                    catalogue.setDeleteUrl(deleteUrl);
//                                }
//                            }
//                        }
//                    }
//
//                    JsonElement infoElem = jsonObject.get("info");
//                    if (infoElem != null && infoElem.isJsonObject()) {
//                        JsonObject infoObj = infoElem.getAsJsonObject();
//                        catalogue.setProviderName(getAsString(infoObj, "x-providerName"));
//                        catalogue.setTitle(getAsString(infoObj, "title"));
//                        catalogue.setDescription(getAsString(infoObj, "description"));
//                    }
//                }
//                log.debug(catalogue.debug());
//            } else {
//                throw new IOException("The OpenAPI document from the URL " + catalogue.getOpenApiUrl() + " is empty !");
//            }
//        }
    }

    private static boolean isExpectedTag(JsonElement element, String tagName) {
        //System.out.println("isExpectedTag = " + tagName);

        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonElement tagsElem = object.get("tags");
            if (tagsElem != null && tagsElem.isJsonArray()) {
                for (JsonElement tag : tagsElem.getAsJsonArray()) {
                    if (tag.getAsString().equals(tagName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static List<OpenAPIOperationParameter> getOperationParameters(JsonElement element) {
        List<OpenAPIOperationParameter> parameters = new ArrayList<>();

        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonElement parametersElem = object.get("parameters");
            if (parametersElem != null && parametersElem.isJsonArray()) {
                for (JsonElement param : parametersElem.getAsJsonArray()) {
                    if (param.isJsonObject()) {
                        JsonObject paramObj = param.getAsJsonObject();
                        OpenAPIOperationParameter parameter = new OpenAPIOperationParameter();

                        parameter.setRequired(getAsBoolean(paramObj, "required"));
                        parameter.setName(getAsString(paramObj, "name"));
                        parameter.setPosition(getAsString(paramObj, "in"));
                        parameter.setOsToken(getAsString(paramObj, "x-value"));
                        JsonElement schemaElem = paramObj.get("schema");

                        if (schemaElem.isJsonObject()) {

                            JsonObject schemaObj = schemaElem.getAsJsonObject();
                            parameter.setDefaultValue(getAsString(schemaObj, "default"));
                            JsonElement enumElem = schemaObj.get("enum");
                            if (enumElem != null && enumElem.isJsonArray()) {
                                for (JsonElement value : enumElem.getAsJsonArray()) {
                                    if (parameter.getPossibleValues() == null) {
                                        parameter.setPossibleValues(new ArrayList<>());
                                    }
                                    parameter.getPossibleValues().add(value.getAsString());
                                }
                            }
                        }

                        System.out.println(parameter.debug());

                        parameters.add(parameter);

                    }
                }
            }
        }
        return parameters;
    }

    private static String getAsString(JsonObject jsonObject, String name) {
        JsonElement element = jsonObject.get(name);
        if (element != null) {
            return element.getAsString();
        }
        return null;
    }

    private static boolean getAsBoolean(JsonObject jsonObject, String name) {
        JsonElement element = jsonObject.get(name);
        if (element != null) {
            return element.getAsBoolean();
        }
        return false;
    }

}
