/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.configuration;

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents Catalogue information
 *
 * @author mng
 */
public class Catalogue implements Serializable {

    private String username;
    private String password;
    private boolean authenticated;
    private boolean seriesWrite;
    private boolean serviceWrite;
    private boolean publish;
    private String providerName;
    private String title;
    private String description;

    private String landingUrl;
    private String osddUrl;

    private CatalogueInterface seriesInterface;
    private CatalogueInterface serviceInterface;

    public Catalogue() {
    }

    // clone
    public Catalogue(Catalogue srcCatalogue) {
        this.publish = srcCatalogue.isPublish();
        this.seriesWrite = srcCatalogue.isSeriesWrite();
        this.serviceWrite = srcCatalogue.isServiceWrite();

        this.providerName = srcCatalogue.getProviderName();
        this.title = srcCatalogue.getTitle();
        this.description = srcCatalogue.getDescription();

        this.landingUrl = srcCatalogue.getLandingUrl();
        this.osddUrl = srcCatalogue.getOsddUrl();

        if (srcCatalogue.getSeriesInterface() != null) {
            this.seriesInterface = new CatalogueInterface(srcCatalogue.getSeriesInterface());
        }

        if (srcCatalogue.getServiceInterface() != null) {
            this.serviceInterface = new CatalogueInterface(srcCatalogue.getServiceInterface());
        }
    }

    public String getLandingUrl() {
        return landingUrl;
    }

    public void setLandingUrl(String landingUrl) {
        this.landingUrl = landingUrl;
    }

    public String getServerUrl() {
        if (landingUrl != null) {
            return landingUrl;
        }
        return osddUrl;
    }

    public String getOsddUrl() {
        return osddUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setOsddUrl(String osddUrl) {
        this.osddUrl = osddUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isPublish() {
        return publish;
    }

    public boolean isSeriesWrite() {
        return seriesWrite;
    }

    public void setSeriesWrite(boolean seriesWrite) {
        this.seriesWrite = seriesWrite;
    }

    public boolean isServiceWrite() {
        return serviceWrite;
    }

    public void setServiceWrite(boolean serviceWrite) {
        this.serviceWrite = serviceWrite;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return (authenticated
                && StringUtils.isNotEmpty(username)
                && StringUtils.isNotEmpty(password));
    }

    public CatalogueInterface getSeriesInterface() {
        return seriesInterface;
    }

    public void setSeriesInterface(CatalogueInterface seriesInterface) {
        this.seriesInterface = seriesInterface;
        if (this.seriesInterface != null
                && (StringUtils.isNotEmpty(this.seriesInterface.getInsertUrl()) || StringUtils.isNotEmpty(this.seriesInterface.getDeleteUrl()))) {
            this.seriesWrite = true;
        }
    }

    public CatalogueInterface getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(CatalogueInterface serviceInterface) {
        this.serviceInterface = serviceInterface;
        if (this.serviceInterface != null
                && (StringUtils.isNotEmpty(this.serviceInterface.getInsertUrl()) || StringUtils.isNotEmpty(this.serviceInterface.getDeleteUrl()))) {
            this.serviceWrite = true;
        }
    }    

    public boolean isReady() {
        return ((seriesInterface != null && seriesInterface.getSearchUrl() != null)
                || (serviceInterface != null && serviceInterface.getSearchUrl() != null));
    }

    public String debug() {
        StringBuilder sb = new StringBuilder();
        sb.append("Catalogue[");
        sb.append("providerName = ").append(providerName);
        sb.append("; title = ").append(title);
        sb.append("; description = ").append(description);
        sb.append("; landingUrl = ").append(landingUrl);
        sb.append("; osddUrl = ").append(osddUrl);
        sb.append("; Interfaces[");
        if (seriesInterface != null) {
            seriesInterface.debug();
        }
        sb.append(";");
        if (serviceInterface != null) {
            serviceInterface.debug();
        }
        sb.append("] ]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Catalogue)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getServerUrl().equals(((Catalogue) obj).getServerUrl());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getServerUrl());
        return hash;
    }
}
