package be.spacebel.metadataeditor.utils.jsf;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;

/**
 * This utility implements custom JSF functions
 *
 * @author mng
 */
public class FaceletFunctions implements Serializable {

    private static final Logger log = Logger.getLogger(FaceletFunctions.class);

    public static String substring(String orig, int start, int length) {
        if (orig == null) {
            return null;
        }
        if (start < 0) {
            start = 0;
        }
        if (length > orig.length()) {
            length = orig.length();
        }
        orig = orig.substring(start, length);
        return orig;
    }

    public static String shortStr(String orig, int length) {
        if (orig == null) {
            return null;
        }

        String subStr = orig;
        if (length < (orig.length() - 3)) {
            subStr = orig.substring(0, length) + "...";
        }
        return subStr;
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return StringUtils.endsWithIgnoreCase(str, suffix);
    }

    public static String shortenEOPCollectionNames(String collectionName) {
        String result = null;
        String delim = ":EOP:";
        if (collectionName == null) {
            return null;
        }
        // toUpperCase in order to handle :eop: and variations
        int index = collectionName.toUpperCase().indexOf(delim);
        if (index == -1) {
            // delimiter not found, returning the whole name
            result = collectionName;
        } else {
            // returning everything past the delimiter
            result = collectionName.substring(index + delim.length());
        }

        return result;
    }

    public static String nullIfNotValidURL(String url) {
        String result = url;
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            result = null;
        }
        return result;
    }

    public static String removeIdPrefix(String id) {
        //return StringUtils.removeStartIgnoreCase(id, LoadConfigurations.config.getIdentifierPrefix());
        return id;
    }

    public static String validateUrl(final String url) {
        String validUrl = "";
        if (StringUtils.isNotEmpty(url)) {
            String[] schemes = {"http", "https", "ftp", "ftps"};
            UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_2_SLASHES + UrlValidator.ALLOW_LOCAL_URLS + UrlValidator.ALLOW_ALL_SCHEMES);
            if (urlValidator.isValid(url)) {
                validUrl = url;
            } else {
                for (String scheme : schemes) {
                    if (StringUtils.startsWithIgnoreCase(url, (scheme + "://"))) {
                        validUrl = url;
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(validUrl)) {
                log.debug("The URL " + url + " is invalid.");
            }
        } else {
            log.debug("The URL is empty.");
        }

        return validUrl;
    }

    public static String humanReadableFormatDate(String strDate) {
        try {
            if (StringUtils.isNotEmpty(strDate) && strDate.trim().length() >= 0) {
                String inputDateFormat = "yyyy-MM-dd'T'HH:mm:ss";
                SimpleDateFormat inputSdf = new SimpleDateFormat(inputDateFormat);
                Date inputDate = inputSdf.parse(strDate);

                String outputTimeFormat = "HH:mm:ss";
                SimpleDateFormat outputSdf = new SimpleDateFormat(outputTimeFormat);
                String outputTime = outputSdf.format(inputDate);

                if (isToday(inputDate)) {
                    return "Today - " + outputTime;
                }

                if (isYesterday(inputDate)) {
                    return "Yesterday - " + outputTime;
                }

                Calendar specifiedCal = Calendar.getInstance();
                specifiedCal.setTime(inputDate);

                String firstFormat = "MMMM dd'st' yyyy";
                String secondFormat = "MMMM dd'nd' yyyy";
                String thirdFormat = "MMMM dd'rd' yyyy";

                String outputDateFormat = "MMMM dd'th' yyyy";

                switch (specifiedCal.get(Calendar.DAY_OF_MONTH)) {
                    case 1:
                    case 21:
                    case 31:
                        outputDateFormat = firstFormat;
                        break;
                    case 2:
                    case 22:
                        outputDateFormat = secondFormat;
                        break;
                    case 3:
                    case 23:
                        outputDateFormat = thirdFormat;
                        break;
                }
                outputSdf = new SimpleDateFormat(outputDateFormat, Locale.ENGLISH);

                return outputSdf.format(inputDate) + " - " + outputTime;
            } else {
                log.debug("Date format is illegal !");
            }

        } catch (ParseException e) {
            log.debug(e);
        }

        return strDate;
    }

    private static boolean isToday(Date inputDate) {
        Calendar nowCal = Calendar.getInstance();
        Calendar specifiedCal = Calendar.getInstance();
        specifiedCal.setTime(inputDate);

        return (nowCal.get(Calendar.ERA) == specifiedCal.get(Calendar.ERA)
                && nowCal.get(Calendar.YEAR) == specifiedCal.get(Calendar.YEAR)
                && nowCal.get(Calendar.DAY_OF_YEAR) == specifiedCal.get(Calendar.DAY_OF_YEAR));
    }

    private static boolean isYesterday(Date inputDate) {

        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);

        Calendar specifiedCal = Calendar.getInstance();
        specifiedCal.setTime(inputDate);

        return (yesterdayCal.get(Calendar.YEAR) == specifiedCal.get(Calendar.YEAR)
                && yesterdayCal.get(Calendar.DAY_OF_YEAR) == specifiedCal.get(Calendar.DAY_OF_YEAR));

    }

    public static String removeSpaces(String str) {
        return StringUtils.deleteWhitespace(str);
    }
    
}
