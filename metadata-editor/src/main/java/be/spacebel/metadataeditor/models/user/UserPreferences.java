/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.user;

import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.utils.CommonUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents user preferences
 *
 * @author mng
 */
public class UserPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private transient String passwordForm;
    private String role;
    private boolean autoMatchGcmdPlatform;
    private boolean autoMatchGcmdInstrument;
    private boolean autoMatchGcmdScienceKeyword;
    private boolean autoUpdateId;
    private transient List<Catalogue> catalogues;
    private String selectedCatalogueUrl;
    private Catalogue catalogue;

    private String[] catalogueUrls;

    private boolean seriesIsoFormat;
    private boolean seriesJsonFormat;
    private boolean seriesDif10Format;

    private boolean serviceIsoFormat;
    private boolean serviceJsonFormat;

    public UserPreferences(List<Catalogue> catalogues) {
        this.catalogues = catalogues;
        if (catalogues != null) {
            _updateCatalogueUrls();
        }
        this.autoMatchGcmdPlatform = true;
        this.autoMatchGcmdInstrument = true;
        this.autoMatchGcmdScienceKeyword = true;
        this.autoUpdateId = true;

        this.seriesIsoFormat = true;
        this.seriesJsonFormat = true;
        this.seriesDif10Format = true;

        this.serviceIsoFormat = true;
        this.serviceJsonFormat = true;
        findCatalogue();
    }

    // clone 
    public UserPreferences(UserPreferences user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.autoMatchGcmdPlatform = user.isAutoMatchGcmdPlatform();
        this.autoMatchGcmdInstrument = user.isAutoMatchGcmdInstrument();
        this.autoMatchGcmdScienceKeyword = user.isAutoMatchGcmdScienceKeyword();
        this.autoUpdateId = user.isAutoUpdateId();
        this.catalogues = new ArrayList<>();
        if (user.getCatalogues() != null) {
            user.getCatalogues().forEach((cat) -> {
                this.catalogues.add(new Catalogue(cat));
            });
        }
        this.selectedCatalogueUrl = user.getSelectedCatalogueUrl();
        this.catalogueUrls = user.getCatalogueUrls();

        this.seriesIsoFormat = user.isSeriesIsoFormat();
        this.seriesJsonFormat = user.isSeriesJsonFormat();
        this.seriesDif10Format = user.isSeriesDif10Format();

        this.serviceIsoFormat = user.isServiceIsoFormat();
        this.serviceJsonFormat = user.isServiceJsonFormat();
        findCatalogue();
    }

    public UserPreferences() {
        this.catalogues = new ArrayList<>();
        this.autoMatchGcmdPlatform = true;
        this.autoMatchGcmdInstrument = true;
        this.autoMatchGcmdScienceKeyword = true;
        this.autoUpdateId = true;

        this.seriesIsoFormat = true;
        this.seriesJsonFormat = true;
        this.seriesDif10Format = true;

        this.serviceIsoFormat = true;
        this.serviceJsonFormat = true;

        findCatalogue();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAutoMatchGcmdPlatform() {
        return autoMatchGcmdPlatform;
    }

    public void setAutoMatchGcmdPlatform(boolean autoMatchGcmdPlatform) {
        this.autoMatchGcmdPlatform = autoMatchGcmdPlatform;
    }

    public boolean isAutoMatchGcmdInstrument() {
        return autoMatchGcmdInstrument;
    }

    public void setAutoMatchGcmdInstrument(boolean autoMatchGcmdInstrument) {
        this.autoMatchGcmdInstrument = autoMatchGcmdInstrument;
    }

    public boolean isAutoMatchGcmdScienceKeyword() {
        return autoMatchGcmdScienceKeyword;
    }

    public void setAutoMatchGcmdScienceKeyword(boolean autoMatchGcmdScienceKeyword) {
        this.autoMatchGcmdScienceKeyword = autoMatchGcmdScienceKeyword;
    }

    public boolean isAutoUpdateId() {
        return autoUpdateId;
    }

    public void setAutoUpdateId(boolean autoUpdateId) {
        this.autoUpdateId = autoUpdateId;
    }

    public String getSelectedCatalogueUrl() {
        return selectedCatalogueUrl;
    }

    public void setSelectedCatalogueUrl(String selectedCatalogueUrl) {
        this.selectedCatalogueUrl = selectedCatalogueUrl;
        findCatalogue();
    }

    public void addCatalogue(Catalogue cat) {
        if (catalogues == null) {
            catalogues = new ArrayList<>();
        }
        catalogues.add(cat);
    }

    public boolean hasMultipleCatalogues() {
        return (catalogues != null && catalogues.size() > 1);
    }

    public List<Catalogue> getCatalogues() {
        return catalogues;
    }

    public String[] getCatalogueUrls() {
        return catalogueUrls;
    }

    public void setCatalogueUrls(String[] catalogueUrls) {
        this.catalogueUrls = catalogueUrls;
    }

    public String getPasswordForm() {
        return passwordForm;
    }

    public void setPasswordForm(String passwordForm) {
        this.passwordForm = passwordForm;
    }

    public String getCataloguesName() {
        if (catalogues != null && !catalogues.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            catalogues.forEach((cat) -> {
                sb.append(cat.getServerUrl()).append(",");
            });
            String names = sb.toString();

            return names.substring(0, names.length() - 1);
        } else {
            return "";
        }

    }

    public String toCSVRecord() {
        String separator = ",";

        StringBuilder sb = new StringBuilder();
        sb.append(CommonUtils.encloseCSVField(username)).append(separator);
        sb.append(CommonUtils.encloseCSVField(password)).append(separator);
        sb.append(CommonUtils.encloseCSVField(role)).append(separator);
        String catUrls = "";
        for (Catalogue cat : catalogues) {
            catUrls += cat.getServerUrl() + ",";
        }
        catUrls = catUrls.substring(0, catUrls.length() - 1);
        sb.append(CommonUtils.encloseCSVField(catUrls)).append(separator);
        return sb.toString();
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("UserPreferences[");
        sb.append(" username = ").append(username);
        sb.append("; role = ").append(role);
        if (catalogueUrls != null) {
            for (String catUrl : catalogueUrls) {
                sb.append("; catalogueUrl = ").append(catUrl);
            }
        }
        sb.append("; autoMatchGcmdPlatform = ").append(autoMatchGcmdPlatform);
        sb.append("; autoMatchGcmdInstrument = ").append(autoMatchGcmdInstrument);
        sb.append("; autoMatchGcmdScienceKeyword = ").append(autoMatchGcmdScienceKeyword);
        sb.append("; autoUpdateId = ").append(autoUpdateId);
        sb.append("]");
        return sb.toString();
    }

    private void _updateCatalogueUrls() {
        if (catalogues != null && !catalogues.isEmpty()) {
            catalogueUrls = new String[catalogues.size()];
            int i = 0;
            for (Catalogue cat : catalogues) {
                catalogueUrls[i] = cat.getServerUrl();
                i++;
            }
        } else {
            catalogueUrls = null;
        }

    }

    public void setSeriesIsoFormat(boolean seriesIsoFormat) {
        this.seriesIsoFormat = seriesIsoFormat;
    }

    public void setSeriesJsonFormat(boolean seriesJsonFormat) {
        this.seriesJsonFormat = seriesJsonFormat;
    }

    public void setSeriesDif10Format(boolean seriesDif10Format) {
        this.seriesDif10Format = seriesDif10Format;
    }

    public void setServiceIsoFormat(boolean serviceIsoFormat) {
        this.serviceIsoFormat = serviceIsoFormat;
    }

    public void setServiceJsonFormat(boolean serviceJsonFormat) {
        this.serviceJsonFormat = serviceJsonFormat;
    }

    public boolean isSeriesIsoFormat() {
        return seriesIsoFormat;
    }

    public boolean isSeriesJsonFormat() {
        return seriesJsonFormat;
    }

    public boolean isSeriesDif10Format() {
        return seriesDif10Format;
    }

    public boolean isServiceIsoFormat() {
        return serviceIsoFormat;
    }

    public boolean isServiceJsonFormat() {
        return serviceJsonFormat;
    }

    public void updateCatalogueUrls() {
        _updateCatalogueUrls();
    }

    public List<SelectItem> getAvailableCatalogues() {
        List<SelectItem> availableCatalogues = new ArrayList<>();
        if (catalogues != null
                && !catalogues.isEmpty()) {
            catalogues.forEach((cat) -> {
                //System.out.println("Cat " + cat.getServerUrl());
                String catName;
                if(StringUtils.isNotEmpty(cat.getTitle())){
                    catName = cat.getTitle() + " (" + cat.getServerUrl() + ")";
                }else{
                    catName = cat.getServerUrl();
                }                
                availableCatalogues.add(new SelectItem(cat.getServerUrl(), catName));
            });
        }
        return availableCatalogues;
    }

    private void findCatalogue() {
        if (catalogues != null && !catalogues.isEmpty()) {
            if (StringUtils.isNotEmpty(selectedCatalogueUrl)) {
                for (Catalogue cat : catalogues) {
                    if (selectedCatalogueUrl.equalsIgnoreCase(cat.getServerUrl())) {
                        catalogue = new Catalogue(cat);
                        break;
                    }
                }
            } else {
                catalogue = new Catalogue(catalogues.get(0));
            }
        }
    }

    public Catalogue getCatalogue() {
        return catalogue;
    }

    public void setCatalogue(Catalogue catalogue) {
        this.catalogue = catalogue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UserPreferences)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUsername().equals(((UserPreferences) obj).getUsername());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUsername());
        return hash;
    }
}
