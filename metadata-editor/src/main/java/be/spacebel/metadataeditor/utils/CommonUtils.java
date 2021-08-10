/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils;

import be.spacebel.metadataeditor.business.ValidationException;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.models.workspace.identification.FreeKeyword;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Common utilities
 *
 * @author mng
 */
public class CommonUtils {

    private static final String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final Logger log = Logger.getLogger(CommonUtils.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(Constants.DATEFORMAT);
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(Constants.DATETIMEFORMAT);
    private static final SimpleDateFormat DATETIME_ZONE_FORMAT = new SimpleDateFormat(Constants.DATETIMEZONEFORMAT);
    private static final SimpleDateFormat DATETIME_ZONE_FULL_FORMAT = new SimpleDateFormat(Constants.DATETIMEZONEFULLFORMAT);

    public static String dateToStr(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);

            if (hour == 0 && minute == 0 && second == 0) {
                return DATE_FORMAT.format(date);
            } else {
                return DATETIME_FORMAT.format(date);
            }
        }
        return "";
    }

    public static String dateTimeToStr(Date date) {
        if (date != null) {
            return DATETIME_FORMAT.format(date);
        }
        return "";
    }

    public static Date strToDateTime(String dateStr) {
        try {
            return DATETIME_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            log.debug(String.format("Date time %s is invalid. Its format should be %s", dateStr, Constants.DATETIMEFORMAT));
            return null;
        }
    }

    public static Date toDate(String dateStr) {
        try {
            if (StringUtils.isNotEmpty(dateStr)) {
                if (dateStr.contains("T")) {
                    if (dateStr.endsWith("Z")) {
                        if (dateStr.length() > 20) {
                            return DATETIME_ZONE_FULL_FORMAT.parse(dateStr);
                        } else {
                            return DATETIME_ZONE_FORMAT.parse(dateStr);
                        }
                    } else {
                        return DATETIME_FORMAT.parse(dateStr);
                    }
                } else {
                    return DATE_FORMAT.parse(dateStr);
                }
            }
        } catch (ParseException e) {
            log.debug(String.format("Date time %s is invalid", dateStr));
            return null;
        }
        return null;
    }

    public static String toDateTimeZoneStr(Date date) {
        if (date != null) {
            return DATETIME_ZONE_FORMAT.format(date);
        }
        return "";
    }

    public static String toDateTimeZoneFullStr(Date date) {
        if (date != null) {
            return DATETIME_ZONE_FULL_FORMAT.format(date);
        }
        return "";
    }

    public static Date strToDate(String dateStr) {
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            log.debug(String.format("Date %s is invalid. Its format should be %s", dateStr, Constants.DATEFORMAT));
            return null;
        }
    }

    public static String getInclusiveDate(String inclusiveDate) {
        String dateStr = inclusiveDate.substring(0, 10);
        Date date = strToDate(dateStr);
        if (date == null) {
            log.error("Error: The format of date parameters should start with yyyy-MM-dd (e.g. 2008-12-10 or 2008-12-10T00:00:00...)");
            return null;
        }
        log.debug("inclusiveDate = " + dateStr);
        return dateStr;
    }

    public static String getErrorMessage(Throwable e) {
        if (StringUtils.isNotEmpty(e.getMessage())) {
            return StringEscapeUtils.escapeHtml4(e.getMessage());
        }

        if (StringUtils.isNotEmpty(e.getLocalizedMessage())) {
            return StringEscapeUtils.escapeHtml4(e.getLocalizedMessage());
        }

        return StringEscapeUtils.escapeHtml4(getStackTrace(e));
    }

    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static String removeXMLDeclaration(String inputString) {
        String result = inputString;
        if (result != null) {
            if (result.startsWith(xmlDeclaration)) {
                result = result.substring(xmlDeclaration.length());
            }
            if (result.startsWith(System.getProperty("line.separator"))) {
                result = result.substring(System.getProperty("line.separator")
                        .length());
            }
            if (result.startsWith("\r\n")) {
                result = result.substring("\r\n".length());
            }
        }
        return result;
    }

    public static String hoursToDays(String hoursStr) {
        log.debug("hoursToDays(" + hoursStr + ")");
        double doubleHours = Double.parseDouble(hoursStr);
        if (doubleHours >= 24) {
            StringBuilder sb = new StringBuilder();
            long hours = (long) doubleHours;
            long days = hours / 24;
            long months = 0;
            long years = 0;

            if (days >= 30) {
                months = days / 30;
                days = days % 30;
                if (months >= 12) {
                    years = months / 12;
                    months = months % 12;
                }
            }

            if (years == 1) {
                sb.append(years).append(" year ");
            }
            if (years > 1) {
                sb.append(years).append(" years ");
            }

            if (months == 1) {
                sb.append(months).append(" month ");
            }
            if (months > 1) {
                sb.append(months).append(" months ");
            }

            if (days == 1) {
                sb.append(days).append(" day ");
            }
            if (days > 1) {
                sb.append(days).append(" days ");
            }
            return sb.toString().trim();
        } else {
            String hourText = (doubleHours > 1) ? " hours" : " hour";
            return (hoursStr + hourText);
        }
    }

    public static String getAbsolutePath() {
        String path = "";
        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            path = ctx.getRealPath("/");
        } catch (Exception e) {
            log.error(e);
        }
        return path;
    }

    /**
     * Delete a file or a directory and its children.
     *
     * @param file The directory to delete.
     * @throws IOException Exception when problem occurs during deleting the
     * directory.
     */
    public static void delete(File file) throws IOException {

        for (File childFile : file.listFiles()) {

            if (childFile.isDirectory()) {
                delete(childFile);
            } else {
                if (!childFile.delete()) {
                    throw new IOException();
                }
            }
        }

        if (!file.delete()) {
            throw new IOException();
        }
    }

    public static boolean matchParameter(String paramNS, String paramToken, String comparedNS, String comparedToken) {
        log.debug("matchParameter(paramNS = " + paramNS + ", paramToken = " + paramToken + ", comparedNS = " + comparedNS + ", comparedToken = " + comparedToken + ")");
        if (StringUtils.isEmpty(paramNS) || StringUtils.isEmpty(paramToken) || StringUtils.isEmpty(comparedNS) || StringUtils.isEmpty(comparedToken)) {
            return false;
        }

        if (paramNS.equalsIgnoreCase(comparedNS)) {
            String pValue = paramToken;
            if (StringUtils.contains(pValue, ":")) {
                pValue = StringUtils.substringAfterLast(pValue, ":");
            }
            log.debug("pValue = " + pValue);
            if (comparedToken.equalsIgnoreCase(pValue)) {
                log.debug("matchParameter = true");
                return true;
            }
        }
        return false;
    }

    public static String validateCoordinates(String coordinatesStr) {
        log.debug("validateCoordinates(" + coordinatesStr + ")");
        List<Double> coordinates = strToDoubles(coordinatesStr);
        StringBuilder sb = new StringBuilder();
        log.debug("Number of points: " + coordinates.size());
        if (coordinates.size() % 2 == 0) {
            for (int i = 0; i < coordinates.size(); i++) {
                double latlon = coordinates.get(i);
                if (i == 0 || (i % 2 == 0)) {
                    /*
                     in case of lat
                     */
                    if (latlon > 85.06) {
                        latlon = 85.06;
                    }

                    if (latlon < -85.06) {
                        latlon = -85.06;
                    }
                }

                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(latlon);
            }
        } else {
            log.debug("Number of points of should be even.");
        }
        return sb.toString();
    }

    private static List<Double> strToDoubles(String str) {
        String[] strArr = str.split(" ");
        List<Double> doubleList = new ArrayList<>();
        if (strArr != null && strArr.length >= 2) {
            for (String xy : strArr) {
                if (StringUtils.isNotEmpty(xy)) {
                    xy = StringUtils.trimToEmpty(xy);
                    if (StringUtils.isNotEmpty(xy)) {
                        try {
                            doubleList.add(Double.parseDouble(xy));
                        } catch (NumberFormatException e) {

                        }
                    }
                }
            }
        }
        return doubleList;
    }

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        try {
            File destDir = new File(destDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            byte[] buffer = new byte[1024];
            try (FileInputStream inputStream = new FileInputStream(zipFilePath); ZipInputStream zis = new ZipInputStream(inputStream)) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = newFile(destDir, zipEntry);
                    if (zipEntry.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }

                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            log.debug("ERRORS " + e);
            throw e;
        }

    }

    public static void unzip(InputStream inputStream, String destDirectory) throws IOException {
        try {
            File destDir = new File(destDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            byte[] buffer = new byte[1024];
            try (ZipInputStream zis = new ZipInputStream(inputStream)) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = newFile(destDir, zipEntry);
                    if (zipEntry.isDirectory()) {
                        newFile.mkdirs();
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }

                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            log.debug("ERRORS " + e);
            throw e;
        }

    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public static Collection<File> getXmlAndJsonFiles(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            String[] extensions = {"xml","json"};
            return FileUtils.listFiles(new File(dirPath), extensions, true);
        } else {
            return new ArrayList<>();
        }
    }

    public static Collection<File> getInternalMetadataFiles(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            String[] extensions = {"json"};
            return FileUtils.listFiles(new File(dirPath), extensions, true);
        } else {
            return new ArrayList<>();
        }
    }

    public static Collection<File> getZipFiles(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            String[] extensions = {"zip"};
            return FileUtils.listFiles(new File(dirPath), extensions, true);
        } else {
            return new ArrayList<>();
        }
    }

    public static String generateFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmssSSS");
        return (sdf.format(new Date()));
    }

    public static String generateFileName(String recordId) {
        return (generateFileName() + "_____" + getFileName(recordId) + ".json");
    }

    public static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    public static UIComponent getUIComponent(String id) {

        FacesContext facesCtx = FacesContext.getCurrentInstance();
        return findComponent(facesCtx.getViewRoot(), id);
    }

    private static UIComponent findComponent(UIComponent base, String id) {

        if (id.equals(base.getClientId())) {
            return base;
        }

        UIComponent children = null;
        UIComponent result = null;
        Iterator childrens = base.getFacetsAndChildren();
        while (childrens.hasNext() && (result == null)) {
            children = (UIComponent) childrens.next();
            if (children.getClientId().endsWith(id)) {
                result = children;
                break;
            }
            result = findComponent(children, id);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public static void handleValidationException(ValidationException e) {
        //FacesMessageUtil.addErrorMessage("Metadata file " + e.getFile() + " is invalid");
        if (e.getErrorFiles() != null && e.getErrorFiles().size() > 0) {
            FacesMessageUtil.addErrorMessages(e.getErrorFiles());
        } else {
            if (e.getValidationErrors() != null) {
                FacesMessageUtil.addValidationErrorMessages(e.getValidationErrors());
            }
        }

    }

    public static String removeLastSlash(String url) {
        while (StringUtils.endsWith(url, "/")) {
            url = StringUtils.removeEnd(url, "/");
        }
        return url;
    }

    public static List<String> strToList(String str) {
        List<String> list = new ArrayList<>();

        if (StringUtils.isNotEmpty(str)) {
            if (str.contains("\"")) {
                int start = 0;
                boolean inQuotes = false;
                for (int current = 0; current < str.length(); current++) {
                    if (str.charAt(current) == '\"') {
                        inQuotes = !inQuotes; // toggle state
                    }
                    boolean atLastChar = (current == str.length() - 1);
                    if (atLastChar) {
                        list.add(trim(str.substring(start)));
                    } else if (str.charAt(current) == ',' && !inQuotes) {
                        list.add(trim(str.substring(start, current)));
                        start = current + 1;
                    }
                }
            } else {
                list.addAll(Arrays.asList(str.split(",")));
            }
        }
        return list;
    }

    private static String trim(String str) {
        /*
            remove leading and tailing double quotes
         */
        if (str.startsWith("\"")) {
            str = str.substring(1, str.length());
        }
        if (str.endsWith("\"")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static Properties readPropertiesFile(String fileName)
            throws FileNotFoundException, IOException {
        Properties properties;
        try (FileInputStream f = new FileInputStream(fileName)) {
            properties = new Properties();            
            properties.load(new InputStreamReader(f, Charset.forName("UTF-8")));
        }
        return properties;
    }

    public static String encloseCSVField(String value) {
        if (value == null || value.isEmpty()) {
            return "\"\"";
        }
        return "\"" + value + "\"";
    }

    public static String addIncrementalCounter(String identifier) {
        int index = identifier.length() - 1;
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = identifier.charAt(index);
            if (Character.isDigit(c)) {
                sb.append(c);
                index--;
            } else {
                break;
            }
        }
        String idIndex = sb.reverse().toString();
        if (idIndex != null && !idIndex.isEmpty()) {
            int counter = Integer.parseInt(idIndex);
            return identifier.substring(0, (identifier.length() - idIndex.length())) + String.format("%0" + idIndex.length() + "d", counter + 1);
        } else {
            return identifier + "-" + String.format("%03d", 1);
        }
    }

    public static String getMetadataId(MetadataFile metadataFile) {
        if (metadataFile != null && metadataFile.getFlatList() != null) {
            return metadataFile.getFlatList().getId();
        }
        return null;
    }

    public static String getMetadataTitle(MetadataFile metadataFile) {
        if (metadataFile != null && metadataFile.getFlatList() != null) {
            return metadataFile.getFlatList().getTitle();
        }
        return null;
    }

    public static String getMetadataStartDate(MetadataFile metadataFile) {
        if (metadataFile != null && metadataFile.getFlatList() != null) {
            return toDateTime(metadataFile.getFlatList().getStartDate());
        }
        return null;
    }

    public static String getMetadataEndDate(MetadataFile metadataFile) {
        if (metadataFile != null && metadataFile.getFlatList() != null) {
            return toDateTime(metadataFile.getFlatList().getEndDate());
        }
        return null;
    }

    public static String getMetadataModifiedDate(MetadataFile metadataFile) {
        if (metadataFile != null && metadataFile.getFlatList() != null) {
            return toDateTime(metadataFile.getFlatList().getModifiedDate());
        }
        return null;
    }

    public static String getMetadataOrgName(MetadataFile metadataFile) {
        if (metadataFile != null && metadataFile.getFlatList() != null) {
            return metadataFile.getFlatList().getOrganisationName();
        }
        return null;
    }

    private static String toDateTime(String date) {
        if (date != null && date.length() == 10) {
            return date + "T00:00:00";
        }
        return date;
    }

    public static boolean hasKeyword(FreeKeyword freeKw) {
        if (freeKw != null
                && freeKw.getKeywords() != null
                && !freeKw.getKeywords().isEmpty()) {
            if (freeKw.getKeywords().stream().anyMatch((kw) -> (StringUtils.isNotEmpty(kw.getLabel())))) {
                return true;
            }
        }
        return false;
    }

    public static String getFileName(String metaRecId) {
        return metaRecId.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public static void showMessage(String message) {
        FacesMessageUtil.addInfoMessage(message);
    }
}
