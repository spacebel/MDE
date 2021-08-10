/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers;

import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.user.UserPreferences;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.catalogue.CatalogueDocumentHandler;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import be.spacebel.metadataeditor.utils.parser.UserUtils;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.ls.LSException;

/**
 * A managed bean handles all user interactions on the Catalogue Management page
 *
 * @author mng
 */
@ManagedBean(name = "catalogueBean")
@ViewScoped
public class CatalogueManagementBean {

    private final Logger log = Logger.getLogger(getClass());

    @ManagedProperty(value = "#{userAuthBean}")
    private UserAuthenticationBean userAuthBean;

    @ManagedProperty(value = "#{userBean}")
    private UserManagementBean userBean;

    private List<Catalogue> catalogues;
    private String currentCatalogueUrl;
    private CatalogueDocumentHandler catDocHandler;

    @PostConstruct
    public void init() {
        log.debug("Initialize catalogue management bean.");
        catDocHandler = new CatalogueDocumentHandler();
        catalogues = new ArrayList<>(userAuthBean.getConfig().getCatalogues().values());
    }

    public void addCatalogue() {
        if (StringUtils.isNotEmpty(currentCatalogueUrl)) {
            if (userAuthBean.getConfig().getCatalogues().containsKey(currentCatalogueUrl)) {
                FacesMessageUtil.addWarningMessage("The system already has a catalogue with the URL " + currentCatalogueUrl);
            } else {
                try {
                    Catalogue newCat = catDocHandler.getCatalogue(currentCatalogueUrl, null);
                    userAuthBean.getConfig().getCatalogues().putIfAbsent(newCat.getServerUrl(), newCat);
                    catalogues = new ArrayList<>(userAuthBean.getConfig().getCatalogues().values());

                    // update the catalogues.xml file
                    XmlUtils.cataloguesToFile(userAuthBean.getConfig().getCataloguesFile(), catalogues);
                } catch (IOException e) {
                    String errorMsg = CommonUtils.getErrorMessage(e);
                    log.debug("Error while adding catalogue: " + errorMsg);
                    FacesMessageUtil.addErrorMessage(errorMsg);
                }
            }
        } else {
            FacesMessageUtil.addWarningMessage("Please provide either landing page URL or OSDD URL");
        }
        currentCatalogueUrl = null;
    }

    public void removeCatalogue(Catalogue cat) {
        log.debug("Remove catalogue" + cat.getServerUrl());
        if (userAuthBean.getUserPreferences().getCatalogue() != null
                && userAuthBean.getUserPreferences().getCatalogue().getServerUrl().equals(cat.getServerUrl())) {
            FacesMessageUtil.addErrorMessage("The catalogue (" + cat.getTitle() + ") is currently in use");
        } else {
            if (userAuthBean.getConfig().getCatalogues().containsKey(cat.getServerUrl())) {
                userAuthBean.getConfig().getCatalogues().remove(cat.getServerUrl());

                catalogues = new ArrayList<>(userAuthBean.getConfig().getCatalogues().values());

                removeCatalogueFromUsers(cat.getServerUrl());

                try {
                    // update the catalogues.xml file
                    XmlUtils.cataloguesToFile(userAuthBean.getConfig().getCataloguesFile(), catalogues);
                } catch (IOException | DOMException | LSException ex) {
                    String errorMsg = CommonUtils.getErrorMessage(ex);
                    log.debug("Error: " + errorMsg);
                    FacesMessageUtil.addErrorMessage(errorMsg);
                }
            }
        }
    }

    private void removeCatalogueFromUsers(String catUrl) {
        if (userAuthBean.getConfig().getUsers() != null) {
            for (Map.Entry<String, UserPreferences> entry : userAuthBean.getConfig().getUsers().entrySet()) {
                UserPreferences userPref = entry.getValue();
                if (userPref.getCatalogues() != null) {
                    userPref.getCatalogues().clear();
                }
                if (userPref.getCatalogueUrls() != null 
                        && userPref.getCatalogueUrls().length > 0) {
                    for (String url : userPref.getCatalogueUrls()) {
                        if (!catUrl.equalsIgnoreCase(url)) {
                            userPref.addCatalogue(userAuthBean.getConfig().getCatalogue(url));
                        }
                    }
                }
                userPref.updateCatalogueUrls();
            }
        }
//        userAuthBean.getConfig().getUsers().entrySet().stream().map((entry) -> entry.getValue()).map((userPref) -> {
//            userPref.getCatalogues().clear();
//            return userPref;
//        }).map((userPref) -> {
//            if (userPref.getCatalogueUrls() != null && userPref.getCatalogueUrls().length > 0) {
//                for (String url : userPref.getCatalogueUrls()) {
//                    if (!catUrl.equalsIgnoreCase(url)) {
//                        userPref.addCatalogue(userAuthBean.getConfig().getCatalogue(url));
//                    }
//                }
//            }
//            return userPref;
//        }).forEachOrdered((userPref) -> {
//            userPref.updateCatalogueUrls();
//        });

        List<UserPreferences> users = new ArrayList<>(userAuthBean.getConfig().getUsers().values());
        try {
            UserUtils.saveUsers(userAuthBean.getConfig().getUsersFile(), users);
            userBean.setUsers(users);

            UserPreferences currentUser = userAuthBean.getConfig().getUsers().get(userAuthBean.getUsername());
            userAuthBean.setUserPreferences(new UserPreferences(currentUser));
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    public void publishCatalogue(Catalogue catalogue) {
        String url = catalogue.getServerUrl();
        log.debug("url = " + url);
        boolean published = catalogue.isPublish();
        log.debug("published = " + published);
        if (published) {
            catalogues.forEach((cat) -> {
                if (cat.getServerUrl().equals(url)) {
                    cat.setPublish(true);
                } else {
                    cat.setPublish(false);
                }
            });
        }
        try {
            XmlUtils.cataloguesToFile(userAuthBean.getConfig().getCataloguesFile(), catalogues);
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }

    }

    public UserAuthenticationBean getUserAuthBean() {
        return userAuthBean;
    }

    public void setUserAuthBean(UserAuthenticationBean userAuthBean) {
        this.userAuthBean = userAuthBean;
    }

    public UserManagementBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserManagementBean userBean) {
        this.userBean = userBean;
    }

    public List<Catalogue> getCatalogues() {
        return catalogues;
    }

    public void setCatalogues(List<Catalogue> catalogues) {
        this.catalogues = catalogues;
    }

    public String getCurrentCatalogueUrl() {
        return currentCatalogueUrl;
    }

    public void setCurrentCatalogueUrl(String currentCatalogueUrl) {
        this.currentCatalogueUrl = currentCatalogueUrl;
    }

}
