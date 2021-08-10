/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers;

import be.spacebel.metadataeditor.models.configuration.Catalogue;
import be.spacebel.metadataeditor.models.configuration.Configuration;
import be.spacebel.metadataeditor.business.Constants;
import be.spacebel.metadataeditor.models.user.UserPreferences;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.UserUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;

/**
 * A session managed bean handles all user authentication actions
 *
 * @author mng
 */
@ManagedBean(name = "userAuthBean")
@SessionScoped
public class UserAuthenticationBean {

    private Configuration config;
    private UserPreferences userPreferences;
    private String username;
    private String password;
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
    private String userWorkspaceDir;
    private boolean editUser;

    private int activeTabIndex;
    private List<String> identifierList;
    private List<String> titleList;
    private String[] seriesFormats;
    private String[] serviceFormats;

    private final Logger log = Logger.getLogger(getClass());

    @PostConstruct
    public void init() {
        config = (Configuration) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getApplicationMap()
                .get(Constants.ME_CONFIG_ATTR);

        if (!config.getErrors().isEmpty()) {
            FacesMessageUtil.addErrors(config.getErrors());
        }

        getAnonymousUserPreferences();
    }

    @PreDestroy
    public void destroy() {
        log.debug("Destroy user authentication bean");
        String userWkDirName = StringUtils
                .substringAfterLast(userWorkspaceDir, "/");
        if (userWkDirName != null
                && userWkDirName.startsWith(Constants.ME_ANONYMOUS_WSP_DIR_PREFIX)) {
            log.debug("Delete anonymous user workspace " + userWorkspaceDir);
            FileUtils.deleteQuietly(new File(userWorkspaceDir));
        }
    }

    public boolean login() {

        UserPreferences tmpUser = findUser();

        if (tmpUser != null) {
            reset();

            userPreferences = new UserPreferences(tmpUser);
            List<String> formats = new ArrayList<>();
            if (userPreferences.isSeriesIsoFormat()) {
                formats.add("xml");
            }
            if (userPreferences.isSeriesJsonFormat()) {
                formats.add("json");
            }
            if (userPreferences.isSeriesDif10Format()) {
                formats.add("dif10");
            }

            if (!formats.isEmpty()) {
                seriesFormats = formats.toArray(new String[0]);
            }

            formats = new ArrayList<>();
            if (userPreferences.isServiceIsoFormat()) {
                formats.add("xml");
            }
            if (userPreferences.isServiceJsonFormat()) {
                formats.add("json");
            }
            if (!formats.isEmpty()) {
                serviceFormats = formats.toArray(new String[0]);
            }

            UIComponent ui = CommonUtils.getUIComponent("loginForm");
            if (ui != null) {
                log.debug("Found component: " + ui.getClientId());
                FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(ui.getClientId());
            }

            log.debug("hide login form dialog");
            PrimeFaces.current().executeScript("PF('wsLoginWv').hide();");

            /*
                Delete anonymous working dir            
             */
            String userWkDirName = StringUtils
                    .substringAfterLast(userWorkspaceDir, "/");
            if (userWkDirName != null
                    && userWkDirName.startsWith(Constants.ME_ANONYMOUS_WSP_DIR_PREFIX)) {
                log.debug("Delete anonymous user workspace " + userWorkspaceDir);
                FileUtils.deleteQuietly(new File(userWorkspaceDir));
            }

            userWorkspaceDir = config.getWorkspaceDir() + "/" + userPreferences.getUsername();
            log.debug("current workspace dir " + userWorkspaceDir);

            return true;
        } else {
            FacesMessageUtil.addErrorMessage("Wrong username and password");
        }
        return false;
    }

    private void reset() {
        this.identifierList = new ArrayList<>();
        this.titleList = new ArrayList<>();
        editUser = false;
        oldPassword = null;
        newPassword = null;
        confirmNewPassword = null;
        seriesFormats = null;
        serviceFormats = null;
    }

    public void logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        getAnonymousUserPreferences();
    }

    public void toChangePassword() {
        editUser = true;
    }

    public void onChangeSeriesFormats(final AjaxBehaviorEvent event) {
        if (seriesFormats == null || seriesFormats.length < 1) {
            seriesFormats = new String[1];
            seriesFormats[0] = "xml";
            FacesMessageUtil.addErrorMessage("At least one metadata format must be selected");
        }
        log.debug("Series formats : " + Arrays.toString(seriesFormats));
    }

    public void onChangeServiceFormats(final AjaxBehaviorEvent event) {
        if (serviceFormats == null || serviceFormats.length < 1) {
            serviceFormats = new String[1];
            serviceFormats[0] = "xml";
            FacesMessageUtil.addErrorMessage("At least one metadata format must be selected");
        }
        log.debug("Service formats : " + Arrays.toString(serviceFormats));
    }

    public void saveMetadataFormatChange() {
        userPreferences.setSeriesIsoFormat(false);
        userPreferences.setSeriesJsonFormat(false);
        userPreferences.setSeriesDif10Format(false);
        if (seriesFormats != null) {
            for (String format : seriesFormats) {
                switch (format) {
                    case "xml":
                        userPreferences.setSeriesIsoFormat(true);
                        break;
                    case "json":
                        userPreferences.setSeriesJsonFormat(true);
                        break;
                    case "dif10":
                        userPreferences.setSeriesDif10Format(true);
                        break;
                }
            }
        }

        userPreferences.setServiceIsoFormat(false);
        userPreferences.setServiceJsonFormat(false);
        if (serviceFormats != null) {
            for (String format : serviceFormats) {
                switch (format) {
                    case "xml":
                        userPreferences.setServiceIsoFormat(true);
                        break;
                    case "json":
                        userPreferences.setServiceJsonFormat(true);
                        break;
                }
            }
        }

        try {
            if (config.getUsers().containsKey(userPreferences.getUsername())) {
                config.getUsers().remove(userPreferences.getUsername());
                config.getUsers().putIfAbsent(userPreferences.getUsername(), userPreferences);
            }
            UserUtils.saveUsers(config.getUsersFile(), new ArrayList<>(config.getUsers().values()));
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    public void changePassword() {
        if (StringUtils.isEmpty(oldPassword)) {
            FacesMessageUtil.addErrorMessage("Please provide the current password");
        } else {
            if (!password.equals(oldPassword)) {
                FacesMessageUtil.addErrorMessage("Wrong password. Please try again");
            } else {
                if (StringUtils.isEmpty(newPassword)) {
                    FacesMessageUtil.addErrorMessage("Please provide a new password");
                } else {
                    if (StringUtils.isEmpty(confirmNewPassword) || !newPassword.equals(confirmNewPassword)) {
                        FacesMessageUtil.addErrorMessage("Password doesn't match");
                    } else {
                        String hashingPassword = DigestUtils.md5Hex(newPassword);
                        userPreferences.setPassword(hashingPassword);

                        if (config.getUsers().containsKey(userPreferences.getUsername())) {
                            config.getUsers().remove(userPreferences.getUsername());
                            config.getUsers().putIfAbsent(userPreferences.getUsername(), userPreferences);
                        }

                        try {
                            UserUtils.saveUsers(config.getUsersFile(), new ArrayList<>(config.getUsers().values()));
                        } catch (IOException ex) {
                            String errorMsg = CommonUtils.getErrorMessage(ex);
                            log.debug("Error: " + errorMsg);
                            FacesMessageUtil.addErrorMessage(errorMsg);
                        }

                        editUser = false;
                        password = newPassword;
                        oldPassword = null;
                        newPassword = null;
                        confirmNewPassword = null;

                        FacesMessageUtil.addInfoMessage("Password has been changed");
                    }
                }
            }
        }
    }

    public void cancelChangePassword() {
        editUser = false;
        oldPassword = null;
        newPassword = null;
        confirmNewPassword = null;
    }

    public void changeCatalogue() {
        log.debug("Change catalogue");
        if (config.getUsers().containsKey(userPreferences.getUsername())) {
            config.getUsers().remove(userPreferences.getUsername());
            config.getUsers().putIfAbsent(userPreferences.getUsername(), userPreferences);
        }
        log.debug("Selected catalogue: " + userPreferences.getSelectedCatalogueUrl());

        try {
            UserUtils.saveUsers(config.getUsersFile(), new ArrayList<>(config.getUsers().values()));
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }

    }

    public void onTabChange(TabChangeEvent event) {
        Tab activeTab = event.getTab();
        this.activeTabIndex = ((TabView) event.getSource()).getChildren().indexOf(activeTab);
        log.debug("activeTabIndex = " + activeTabIndex);
    }

    public boolean isAuthenticated() {
        return (userPreferences != null
                && StringUtils.isNotEmpty(userPreferences.getUsername())
                && StringUtils.isNotEmpty(userPreferences.getPassword()));
    }

    public boolean hasCatalogueWriteAccess() {
        return isAuthenticated()
                && userPreferences.getCatalogue() != null
                && (userPreferences.getCatalogue().isSeriesWrite() || userPreferences.getCatalogue().isServiceWrite());
    }

    public boolean canWrite(MetadataFile mFile) {
        if (isAuthenticated() && userPreferences.getCatalogue() != null) {
            if (mFile != null && mFile.getMetadata() != null) {
                if (mFile.getMetadata().isSeries()
                        && userPreferences.getCatalogue().isSeriesWrite()) {
                    return true;
                } else {
                    if (mFile.getMetadata().isService()
                            && userPreferences.getCatalogue().isServiceWrite()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canWrite(String searchType) {
        if (isAuthenticated() && userPreferences.getCatalogue() != null) {
            if ("series".equals(searchType)
                    && userPreferences.getCatalogue().isSeriesWrite()) {
                return true;
            }

            if ("service".equals(searchType)
                    && userPreferences.getCatalogue().isServiceWrite()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return (userPreferences != null
                && userPreferences.getRole() != null
                && userPreferences.getRole().equalsIgnoreCase("Administrator"));
    }

    public boolean isRegistered() {
        return (userPreferences != null
                && userPreferences.getRole() != null
                && userPreferences.getRole().equalsIgnoreCase("Registered user"));
    }

    public List<SelectItem> getCatInterfaces() {
        List<SelectItem> catInterfaces = new ArrayList<>();

        if (userPreferences != null && userPreferences.getCatalogue() != null) {
            if (userPreferences.getCatalogue().getSeriesInterface() != null
                    && userPreferences.getCatalogue().getSeriesInterface().getSearchUrl() != null) {
                catInterfaces.add(new SelectItem("series", "Collection search"));
            }
            if (userPreferences.getCatalogue().getServiceInterface() != null
                    && userPreferences.getCatalogue().getServiceInterface().getSearchUrl() != null) {
                catInterfaces.add(new SelectItem("service", "Service search"));
            }
        }

        return catInterfaces;
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public Configuration getConfig() {
        return config;
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

    public void setUserWorkspaceDir(String userWorkspaceDir) {
        this.userWorkspaceDir = userWorkspaceDir;
    }

    public String getUserWorkspaceDir() {
        return userWorkspaceDir;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    public boolean isEditUser() {
        return editUser;
    }

    public void setEditUser(boolean editUser) {
        this.editUser = editUser;
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(int activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public String[] getSeriesFormats() {
        return seriesFormats;
    }

    public void setSeriesFormats(String[] seriesFormats) {
        this.seriesFormats = seriesFormats;
    }

    public String[] getServiceFormats() {
        return serviceFormats;
    }

    public void setServiceFormats(String[] serviceFormats) {
        this.serviceFormats = serviceFormats;
    }

    public boolean existsId(String id) {
        String newId = cleanStr(id);
        if (StringUtils.isNotEmpty(newId)) {
            return (identifierList != null && identifierList.contains(newId));
        }
        return false;
    }

    public void addId(String id) {
        String newId = cleanStr(id);
        if (StringUtils.isNotEmpty(newId)) {
            if (this.identifierList == null) {
                this.identifierList = new ArrayList<>();
            }

            if (!this.identifierList.contains(newId)) {
                this.identifierList.add(newId);
            }
        }
    }

    public void removeId(String id) {
        String newId = cleanStr(id);
        if (StringUtils.isNotEmpty(newId)
                && this.identifierList != null) {
            this.identifierList.remove(newId);
        }
    }

    public boolean existsTitle(String title) {
        String newTitle = cleanStr(title);
        if (StringUtils.isNotEmpty(newTitle)) {
            return (titleList != null && titleList.contains(newTitle));
        }
        return false;
    }

    public void addTitle(String title) {
        String newTitle = cleanStr(title);
        if (StringUtils.isNotEmpty(newTitle)) {
            if (this.titleList == null) {
                this.titleList = new ArrayList<>();
            }
            if (!this.titleList.contains(newTitle)) {
                this.titleList.add(newTitle);
            }
        }
    }

    public void removeTitle(String title) {
        String newTitle = cleanStr(title);
        if (StringUtils.isNotEmpty(newTitle) && this.titleList != null) {
            this.titleList.remove(newTitle);
        }
    }

    private UserPreferences findUser() {
        String hashingPassword = DigestUtils.md5Hex(password);
        Map<String, UserPreferences> users = config.getUsers();

        if (users != null && users.containsKey(username)) {
            log.debug("Found user " + username);
            log.debug("hashingPassword " + hashingPassword);

            UserPreferences tmpUser = users.get(username);
            //return tmpUser;
            //log.debug("user pass " + tmpUser.getPassword());
            if (tmpUser != null
                    && tmpUser.getPassword().equalsIgnoreCase(hashingPassword)) {
                return tmpUser;
            }
            //return tmpUser;
        }
        return null;
    }

    private void getAnonymousUserPreferences() {
        log.debug("Create an anonymous user preferences");
        List<Catalogue> userCatalogues = new ArrayList<>();
        if (config.getCatalogues() != null && !config.getCatalogues().isEmpty()) {
            config.getCatalogues().entrySet().forEach((entry) -> {
                Catalogue cat = new Catalogue(entry.getValue());
                if (cat.isPublish()) {
                    userCatalogues.add(cat);
                }
            });
        }

        userPreferences = new UserPreferences(userCatalogues);
        userPreferences.setSeriesDif10Format(false);
        FacesContext fCtx = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(true);
        userWorkspaceDir = (String) session.getAttribute(Constants.ME_ANONYMOUS_WSP_DIR);

        if (StringUtils.isNotEmpty(userWorkspaceDir)) {
            if (FileUtils.deleteQuietly(new File(userWorkspaceDir))) {
                log.debug("Delete previous anonymous workspace dir " + userWorkspaceDir);
            }
        }

        // generate a new anonymous user workspace
        userWorkspaceDir = config.getWorkspaceDir() + "/" + Constants.ME_ANONYMOUS_WSP_DIR_PREFIX + UUID.randomUUID().toString();
        log.debug("Anonymous user workspace dir: " + userWorkspaceDir);
        session.setAttribute(Constants.ME_ANONYMOUS_WSP_DIR, userWorkspaceDir);

        // create the anonymous user workspace 
//        File dir = new File(userWorkspaceDir);
//        dir.mkdirs();
        activeTabIndex = 0;
    }

    private String cleanStr(String str) {
        String newStr = StringUtils.trimToEmpty(str);
        if (newStr != null) {
            newStr = StringUtils.lowerCase(newStr);
        }
        return newStr;
    }
}
