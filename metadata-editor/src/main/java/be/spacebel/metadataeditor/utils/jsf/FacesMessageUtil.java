package be.spacebel.metadataeditor.utils.jsf;

import be.spacebel.metadataeditor.utils.validation.ValidationError;
import be.spacebel.metadataeditor.utils.validation.ValidationErrorsPerFile;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Utility class to display error and info messages on the client, or retrieve
 * localized strings from the bundle.
 *
 * @author mng
 *
 */
public class FacesMessageUtil implements Serializable {

    private ResourceBundle bundle;
    private final static Logger log = Logger.getLogger(FacesMessageUtil.class);

    private FacesMessageUtil(FacesContext fc, Locale locale) {
        bundle = ResourceBundle.getBundle(fc.getApplication().getMessageBundle(), locale);
    }

    public static void addInfoMessage(String bundleKey, String... params) {
        addInfoMessageWithDetails(bundleKey, "", params);
    }

    public static void addInfoMessageWithDetails(String bundleKey, String details, String... params) {
        getInstance().addMessage(bundleKey, details, FacesMessage.SEVERITY_INFO, params);
    }

    public static void addInfoMessage(Throwable e) {
        log.error(e.getMessage(), e);
        getInstance().addMessage(e.getMessage(), getStackTraceAsString(e),
                FacesMessage.SEVERITY_INFO);
    }

    public static void addWarningMessage(String bundleKey, String... params) {
        addWarningMessageWithDetails(bundleKey, "", params);
    }

    public static void addWarningMessageWithDetails(String bundleKey, String details,
            String... params) {
        getInstance().addMessage(bundleKey, details, FacesMessage.SEVERITY_WARN, params);
    }

    public static void addWarningMessage(Throwable e) {
        log.error(e.getMessage(), e);
        getInstance().addMessage(e.getMessage(), getStackTraceAsString(e),
                FacesMessage.SEVERITY_WARN);
    }

    public static void addErrorMessage(String bundleKey, String... params) {
        addErrorMessageWithDetails(bundleKey, "", params);
    }

    public static void addErrors(List<String> errors) {
        if (errors != null && errors.size() > 0) {
            FacesContext fc = FacesContext.getCurrentInstance();
            StringBuilder sb = new StringBuilder();
            String summaryText = "Configuration loading errors";
            sb.append("<table>");
            for (String error : errors) {
                sb.append("<tr>");
                sb.append("<td>").append(StringEscapeUtils.escapeXml10(error)).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            fc.addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, summaryText, sb.toString()));
        }
    }

    public static void addValidationErrorMessages(List<ValidationError> errors) {
        if (errors != null && errors.size() > 0) {
            FacesContext fc = FacesContext.getCurrentInstance();
            String summaryText = "Validation";

            int warnCount = 0;
            int errorCount = 0;
            int fatalCount = 0;

            StringBuilder sb = new StringBuilder();

            for (ValidationError error : errors) {
                if (error.getErrorLevel() == FacesMessage.SEVERITY_WARN) {
                    warnCount++;
                }

                if (error.getErrorLevel() == FacesMessage.SEVERITY_ERROR) {
                    errorCount++;
                }

                if (error.getErrorLevel() == FacesMessage.SEVERITY_FATAL) {
                    fatalCount++;
                }

                sb.append("<tr>");
                sb.append("<td>").append(StringEscapeUtils.escapeXml10(error.getMessage())).append("</td>");
                sb.append("</tr>");

                //fc.addMessage("", new FacesMessage(error.getErrorLevel(), summaryText, error.getMessage()));
            }

            if (errorCount > 0) {
                summaryText = summaryText + " errors: ";
                fc.addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, summaryText, buildErrorTable(sb, summaryText)));
            } else {
                if (warnCount > 0) {
                    summaryText = summaryText + " warnings: ";
                    fc.addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN, summaryText, buildErrorTable(sb, summaryText)));
                } else {
                    if (fatalCount > 0) {
                        summaryText = summaryText + " fatals:";
                        fc.addMessage("", new FacesMessage(FacesMessage.SEVERITY_FATAL, summaryText, buildErrorTable(sb, summaryText)));
                    }
                }
            }
        }

    }

    private static String buildErrorTable(StringBuilder sbRows, String header) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<th>").append(header).append("</th>");
        sb.append("</tr>");
        sb.append(sbRows);
        sb.append("</table>");
        return sb.toString();
    }

    public static void addErrorMessages(List<ValidationErrorsPerFile> errorFiles) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (errorFiles != null && errorFiles.size() > 0) {
            for (ValidationErrorsPerFile errorFile : errorFiles) {
                StringBuilder sb = new StringBuilder();
                String summaryText = "Validation";
                sb.append("<table>");
                sb.append("<tr>");
                sb.append("<th>Errors/warnings of metadata record ").append(errorFile.getRecordId()).append("</th>");
                sb.append("</tr>");

                int warnCount = 0;
                int errorCount = 0;
                int fatalCount = 0;

                for (ValidationError error : errorFile.getErrors()) {
                    if (error.getErrorLevel() == FacesMessage.SEVERITY_WARN) {
                        warnCount++;
                    }

                    if (error.getErrorLevel() == FacesMessage.SEVERITY_ERROR) {
                        errorCount++;
                    }

                    if (error.getErrorLevel() == FacesMessage.SEVERITY_FATAL) {
                        fatalCount++;
                    }

                    sb.append("<tr>");
                    sb.append("<td>").append(StringEscapeUtils.escapeXml10(error.getMessage())).append("</td>");
                    sb.append("</tr>");

                    //fc.addMessage("", new FacesMessage(error.getErrorLevel(), summaryText, error.getMessage()));
                }
                sb.append("</table>");

                if (errorCount > 0) {
                    summaryText = summaryText + " error";
                    fc.addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, summaryText, sb.toString()));
                } else {
                    if (warnCount > 0) {
                        summaryText = summaryText + " warning";
                        fc.addMessage("", new FacesMessage(FacesMessage.SEVERITY_WARN, summaryText, sb.toString()));
                    } else {
                        if (fatalCount > 0) {
                            summaryText = summaryText + " fatal";
                            fc.addMessage("", new FacesMessage(FacesMessage.SEVERITY_FATAL, summaryText, sb.toString()));
                        }
                    }
                }
            }

        }
    }

    public static void addErrorMessageWithDetails(String bundleKey, String details,
            String... params) {
        getInstance().addMessage(bundleKey, details, FacesMessage.SEVERITY_ERROR, params);
    }

    public static void addErrorMessage(Throwable e) {
        log.error(e.getMessage(), e);
        getInstance().addMessage(e.getMessage(), "", FacesMessage.SEVERITY_ERROR);
    }

    public static String getLocalizedString(String bundleKey, String... params) {
        return getInstance().getStringFromBundle(bundleKey, params);
    }

    private static FacesMessageUtil getInstance() {
        FacesContext fc = FacesContext.getCurrentInstance();
        Locale locale = null;
        try {
            locale = fc.getViewRoot().getLocale();

        } catch (NullPointerException npe) {
            locale = Locale.US;
        }
        return new FacesMessageUtil(fc, locale);
    }

    private void addMessage(String bundleKey, String details, FacesMessage.Severity severity,
            String... params) {
        String text = "null";
        if (bundleKey != null) {
            text = getStringFromBundle(bundleKey, params);
        }
        if (StringUtils.isNotEmpty(details)) {
            text += ": " + details;
        }
        String summaryText = "Info";
        if (severity == FacesMessage.SEVERITY_ERROR) {
            summaryText = "Error";
        }
        if (severity == FacesMessage.SEVERITY_WARN) {
            summaryText = "Warning";
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, "", text));
    }

    private String getStringFromBundle(String bundleKey, String... params) {
        String text = null;
        try {
            text = bundle.getString(bundleKey);
        } catch (MissingResourceException e) {
            // property not found, we leave the text as is
            text = bundleKey;
        }

        if (params != null) {
            try {
                MessageFormat mf = new MessageFormat(text);
                text = mf.format(params, new StringBuffer(), null).toString();
            } catch (Exception e) {
                // message parsing failed, leave the text untouched
            }
        }
        return text;
    }

    public static String getStackTraceAsString(Throwable t) {
        return t.getMessage();
    }

}
