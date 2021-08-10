/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class implements JSON utilities
 *
 * @author mng
 */
public class JsonUtils {

    public static JSONObject getGeoJSONObjectProperty(JSONObject object, String properties) {
        JSONObject value = null;
        if (object != null) {
            if (object.has(properties) && (object.get(properties) != JSONObject.NULL)) {
                value = object.getJSONObject(properties);
            }
        }
        return value;
    }

    public static String getGeoJSONStringProperty(JSONObject object, String properties) {
        String value = StringUtils.EMPTY;
        if (object != null) {
            if (object.has(properties)) {
                value = object.getString(properties);
            }
            if (StringUtils.isNotEmpty(value)) {
                value = StringUtils.trimToEmpty(value);
            }
        }
        return value;

    }

    public static JSONArray getGeoJSONArrayProperty(JSONObject object, String properties) {
        JSONArray value = null;
        if (object != null) {
            if (object.has(properties)) {
                value = object.getJSONArray(properties);
            }
        }
        return value;

    }
}
