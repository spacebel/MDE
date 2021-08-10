/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers;

import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.user.UserPreferences;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.UserUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

/**
 * A managed bean handles all user interactions on the User Management page
 *
 * @author mng
 */
@ManagedBean(name = "userBean")
@ViewScoped
public class UserManagementBean {

    @ManagedProperty(value = "#{userAuthBean}")
    private UserAuthenticationBean userAuthBean;

    @ManagedProperty(value = "#{mainBean}")
    private MainBean mainBean;

    private List<UserPreferences> users;
    private UserPreferences selectionUser;
    private UserPreferences creatingUser;

    private List<SelectItem> availableRoles;

    private int view;

    private final Logger log = Logger.getLogger(getClass());

    @PostConstruct
    public void init() {
        log.debug("Initialize admin bean.");
        users = new ArrayList<>(userAuthBean.getConfig().getUsers().values());
        for (UserPreferences u : users) {
            log.debug(u.debug());
        }

        availableRoles = new ArrayList<>();
        availableRoles.add(new SelectItem("Registered User", "Registered User"));
        availableRoles.add(new SelectItem("Administrator", "Administrator"));

        view = 1;
    }

    public void prepareCreatingUser() {
        creatingUser = new UserPreferences();
        this.view = 3;
    }

    public void createUser() {
        if (creatingUser.getCatalogueUrls() != null) {
            for (String catId : creatingUser.getCatalogueUrls()) {
                creatingUser.getCatalogues()
                        .add(userAuthBean.getConfig().getCatalogue(catId));
            }
        }

        UserPreferences newUser = new UserPreferences(creatingUser);
        String hashingPassword = DigestUtils.md5Hex(newUser.getPassword());
        newUser.setPassword(hashingPassword);
        users.add(newUser);
        userAuthBean.getConfig().getUsers().putIfAbsent(newUser.getUsername(), newUser);
        this.creatingUser = null;
        this.view = 1;

        try {
            UserUtils.saveUsers(userAuthBean.getConfig().getUsersFile(), users);
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    public void updateUser() {
        if (selectionUser.getCatalogueUrls() != null
                && selectionUser.getCatalogueUrls().length > 0) {
            log.debug("Has catalogue URL ");
            if (selectionUser.getCatalogues() != null) {
                selectionUser.getCatalogues().clear();
            }
            for (String catUrl : selectionUser.getCatalogueUrls()) {
                log.debug("catUrl = " + catUrl);
                selectionUser
                        .addCatalogue(userAuthBean.getConfig().getCatalogue(catUrl));
            }
        } else {
            log.debug("Has no catalogue URL ");
            if (selectionUser.getCatalogues() != null) {
                selectionUser.getCatalogues().clear();
            }
            if (userAuthBean.getActiveTabIndex() > 0) {
                userAuthBean.setActiveTabIndex(userAuthBean.getActiveTabIndex() - 1);
            }
        }

        log.debug("selectionUser " + selectionUser.getCatalogue());
        UserPreferences newUser = new UserPreferences(selectionUser);
        log.debug("Old password: " + selectionUser.getPassword());

        if (StringUtils.isNotEmpty(selectionUser.getPasswordForm())) {
            log.debug("Update user password" + selectionUser.getPasswordForm());
            String hashingPassword = DigestUtils.md5Hex(selectionUser.getPasswordForm());
            log.debug("New pass: " + hashingPassword);
            newUser.setPassword(hashingPassword);
        }

        if (userAuthBean.getConfig().getUsers().containsKey(selectionUser.getUsername())) {
            userAuthBean.getConfig().getUsers().remove(selectionUser.getUsername());
            userAuthBean.getConfig().getUsers().put(newUser.getUsername(), newUser);
        }

        users = new ArrayList<>(userAuthBean.getConfig().getUsers().values());

        // if updating user is the current user
        if (userAuthBean.getUserPreferences().getUsername().equals(newUser.getUsername())) {
            if (userAuthBean.getUserPreferences().getCatalogues() != null
                    && !userAuthBean.getUserPreferences().getCatalogues().isEmpty()) {
                if (selectionUser.getCatalogues() != null
                        && !selectionUser.getCatalogues().isEmpty()) {

                    selectionUser.getCatalogues().stream().filter((cat) -> (!userAuthBean.getUserPreferences().getCatalogues().contains(cat))).forEachOrdered((cat) -> {
                        userAuthBean.getUserPreferences().addCatalogue(cat);
                    });

                    // remove all catalogues from the user preferences that don't exist in the selectionUser catalogues list
                    userAuthBean.getUserPreferences().getCatalogues().retainAll(selectionUser.getCatalogues());

                } else {
                    userAuthBean.getUserPreferences().getCatalogues().clear();
                }
            } else {
                if (selectionUser.getCatalogues() != null
                        && !selectionUser.getCatalogues().isEmpty()) {
                    log.debug("Case 1");
                    selectionUser.getCatalogues().forEach((cat) -> {
                        userAuthBean.getUserPreferences().addCatalogue(cat);
                    });
                } else {
                    log.debug("Case 2");
                }
            }
            userAuthBean.getUserPreferences().updateCatalogueUrls();
            if (userAuthBean.getUserPreferences().getCatalogues() != null) {
                for (Catalogue cat : userAuthBean.getUserPreferences().getCatalogues()) {
                    log.debug(cat.debug());
                }
            }
            mainBean.refreshUserPreferences();
        }

        log.debug("User catalogue: " + userAuthBean.getUserPreferences().getCatalogue());

        this.view = 1;
        try {
            UserUtils.saveUsers(userAuthBean.getConfig().getUsersFile(), users);
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    public void removeUser(UserPreferences user) {
        log.debug("Remove user" + user.getUsername());
        if (userAuthBean.getConfig().getUsers().containsKey(user.getUsername())) {
            String userWorkspace = userAuthBean.getConfig().getWorkspaceDir() + "/" + user.getUsername();
            userAuthBean.getConfig().getUsers().remove(user.getUsername());

            users = new ArrayList<>(userAuthBean.getConfig().getUsers().values());

            try {
                UserUtils.saveUsers(userAuthBean.getConfig().getUsersFile(), users);

                // remove user workspace
                FileUtils.deleteQuietly(new File(userWorkspace));
            } catch (IOException ex) {
                String errorMsg = CommonUtils.getErrorMessage(ex);
                log.debug("Error: " + errorMsg);
                FacesMessageUtil.addErrorMessage(errorMsg);
            }
        }
    }

    public List<UserPreferences> getUsers() {
        return users;
    }

    public void setUsers(List<UserPreferences> users) {
        this.users = users;
    }

    public void onUserSelect(SelectEvent event) {
        log.debug("On user select ");
        selectionUser = (UserPreferences) event.getObject();
        this.view = 2;
    }

    public UserPreferences getSelectionUser() {
        return selectionUser;
    }

    public void setSelectionUser(UserPreferences selectionUser) {
        this.selectionUser = selectionUser;
    }

    public UserPreferences getCreatingUser() {
        return creatingUser;
    }

    public void setCreatingUser(UserPreferences creatingUser) {
        this.creatingUser = creatingUser;
    }

    public List<SelectItem> getAvailableRoles() {
        return availableRoles;
    }

    public void setAvailableRoles(List<SelectItem> availableRoles) {
        this.availableRoles = availableRoles;
    }

    public List<SelectItem> getAvailableCatalogues() {
        List<SelectItem> availableCatalogues = new ArrayList<>();
        if (userAuthBean.getConfig().getCatalogues() != null
                && !userAuthBean.getConfig().getCatalogues().isEmpty()) {
            userAuthBean.getConfig().getCatalogues().entrySet().forEach((entry) -> {
                log.debug("Catalogue: " + entry.getValue().getServerUrl());
                availableCatalogues.add(new SelectItem(entry.getKey(), entry.getValue().getServerUrl()));
            });
        }
        return availableCatalogues;
    }

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public MainBean getMainBean() {
        return mainBean;
    }

    public void setMainBean(MainBean mainBean) {
        this.mainBean = mainBean;
    }

    public UserAuthenticationBean getUserAuthBean() {
        return userAuthBean;
    }

    public void setUserAuthBean(UserAuthenticationBean userAuthBean) {
        this.userAuthBean = userAuthBean;
    }

}
