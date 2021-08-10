/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.controllers;

import be.spacebel.metadataeditor.controllers.layout.WorkspaceLayout;
import be.spacebel.metadataeditor.controllers.layout.CatalogueLayout;
import be.spacebel.metadataeditor.utils.jsf.CustomFunctions;
import be.spacebel.metadataeditor.business.AuthenticationException;
import be.spacebel.metadataeditor.business.Delegator;
import be.spacebel.metadataeditor.business.SearchException;
import be.spacebel.metadataeditor.business.StaticPaginator;
import be.spacebel.metadataeditor.business.ValidationException;
import be.spacebel.metadataeditor.models.workspace.EditorForm;
import be.spacebel.metadataeditor.models.catalogue.OpenSearchUrl;
import be.spacebel.metadataeditor.models.catalogue.SearchResultItem;
import be.spacebel.metadataeditor.models.workspace.MetadataFile;
import be.spacebel.metadataeditor.models.workspace.identification.FreeKeyword;
import be.spacebel.metadataeditor.models.workspace.identification.ThesaurusKeyword;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.jsf.FacesMessageUtil;
import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.MetadataParser;
import be.spacebel.metadataeditor.utils.parser.MetadataUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A managed bean handles all user interactions on Workspace and Catalogue
 * pages.
 *
 * @author mng
 */
@ManagedBean(name = "mainBean")
//@SessionScoped
@ViewScoped
public class MainBean {

    private final Logger log = Logger.getLogger(getClass());
    private static final String SERIES_TEMPLATE = "seriesTemplate.xml";
    private static final String SERVICE_TEMPLATE = "serviceTemplate.xml";

    @ManagedProperty(value = "#{userAuthBean}")
    private UserAuthenticationBean userAuthBean;

    private Delegator delegator;
    private MetadataParser metadataParser;
    private EditorForm editor;

    //private String uuidWorkingDir;
    private WorkspaceLayout workspace;
    private CatalogueLayout catalog;
    private CustomFunctions customFunctions;
    private boolean advancedSearch;

    private enum ACTION {
        REMOVE, REMOVE_SELECTIONS, COPY, MOVE, REPLACE, MOVE_REPLACE, COPY_SELECTIONS, REPLACE_SELECTIONS, MOVE_SELECTIONS, MOVE_REPLACE_SELECTIONS
    };
    private ACTION catalogueAction;
    private String catalogueAuthenticationMsg;

    @PostConstruct
    public void init() {
        log.debug("Initialize main bean.");
        try {
            log.debug(userAuthBean.getConfig().getErrors());
            if (userAuthBean.getConfig().getErrors().isEmpty()) {
                delegator = new Delegator(userAuthBean.getConfig(), userAuthBean.getUserPreferences());
                metadataParser = new MetadataParser(userAuthBean.getConfig());

//                File file = new File(userAuthBean.getUserWorkspaceDir());
//                String userWkDirName = StringUtils
//                        .substringAfterLast(userAuthBean.getUserWorkspaceDir(), "/");
//                if (!file.exists()
//                        && userWkDirName.startsWith(Constants.ME_ANONYMOUS_WSP_DIR_PREFIX)) {
//                    if (file.mkdirs()) {
//                        log.debug("Created user workspace dir = " + userAuthBean.getUserWorkspaceDir());
//                    } else {
//                        log.debug("Could not create user workspace dir = " + userAuthBean.getUserWorkspaceDir());
//                    }
//                }
                workspace = new WorkspaceLayout(delegator, userAuthBean.getUserWorkspaceDir());

                loadLocalMetadataFiles();

                customFunctions = new CustomFunctions();

                editor = new EditorForm(userAuthBean.getConfig());

                delegator.loadCatalogueInfo();
                catalog = new CatalogueLayout();

            } else {
                log.debug("Errors: " + userAuthBean.getConfig().getErrors());
                FacesMessageUtil.addErrors(userAuthBean.getConfig().getErrors());
            }
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
//        } catch (ValidationException e) {
//            CommonUtils.handleValidationException(e);
//        }
    }

    @PreDestroy
    public void destroy() {
        log.debug("Destroy main bean.");
//        /*
//         delete temporary directories where temporary zip file have been stored
//         */
//
//        try {
//            File file = new File(userAuthBean.getConfig().getTempDir());
//            if (file.exists()) {
//                CommonUtils.delete(file);
//            }
//        } catch (IOException e) {
//            log.debug("Error while deleting working directory: " + e.getMessage());
//        }

    }

    public void authenticateCatalogue() {
        if (catalogueAction != null) {
            log.debug("Catalogue action: " + catalogueAction);

            switch (catalogueAction) {
                // remove selected item from the catalogue
                case REMOVE:
                    // Signal temporarily that the catalogue is authenticated to send the account info to the catalogue
                    // The real authentication will be performed by the catalogue
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    removeFromCatalogue();
                    break;

                // remove all selected items from the catalogue
                case REMOVE_SELECTIONS:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    removeSelectionsFromCatalogue();
                    break;

                // Copy selected metadata record to the catalogue
                case COPY:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    copyToCatalogue(false);
                    break;

                // Move selected metadata record to the catalogue
                case MOVE:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    copyToCatalogue(true);
                    break;

                // Replace an existing metadata record in the catalogue by the selected metadata record
                case REPLACE:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    copyToCatalogue(false);
                    break;

                // Replace an existing metadata record in the catalogue by the selected metadata record and then remove the selected metadata record from the workspace
                case MOVE_REPLACE:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    copyToCatalogue(true);
                    break;

                // Copy selected metadata records to the catalogue
                case COPY_SELECTIONS:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    copyMultipleToCatalogue(false);
                    break;

                // Move selected metadata records to the catalogue
                case MOVE_SELECTIONS:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    copyMultipleToCatalogue(true);
                    break;

                // Replace existing metadata records in the catalogue by selected metadata records
                case REPLACE_SELECTIONS:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    replaceSelections(false);
                    break;

                // Replace existing metadata records in the catalogue by the selected metadata records and then remove the selected metadata records from the workspace
                case MOVE_REPLACE_SELECTIONS:
                    userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(true);
                    replaceSelections(true);
                    break;
            }
        }
    }

    public void doLogin() {
        boolean ok = userAuthBean.login();
        if (ok) {
            log.debug("User signed");
            try {
                log.debug("User working dir: " + userAuthBean.getUserWorkspaceDir());
                File userDir = new File(userAuthBean.getUserWorkspaceDir());
                if (!userDir.exists()) {
                    userDir.mkdirs();
                }

                delegator = new Delegator(userAuthBean.getConfig(), userAuthBean.getUserPreferences());
                workspace = new WorkspaceLayout(delegator, userAuthBean.getUserWorkspaceDir());

                catalog = new CatalogueLayout();

                loadLocalMetadataFiles();

                deleteZipFiles();

                delegator.loadCatalogueInfo();

                userAuthBean.setActiveTabIndex(0);

            } catch (IOException ex) {
                String errorMsg = CommonUtils.getErrorMessage(ex);
                log.debug("Error: " + errorMsg);
                FacesMessageUtil.addErrorMessage(errorMsg);
            }
//            } catch (ValidationException e) {
//                CommonUtils.handleValidationException(e);
//            }
        }
    }

    public void doLogout() {
        try {
            userAuthBean.logout();
            log.debug("User signed out");
            log.debug("Anonymous working dir: " + userAuthBean.getUserWorkspaceDir());
            File userDir = new File(userAuthBean.getUserWorkspaceDir());
            if (!userDir.exists()) {
                userDir.mkdirs();
            }

            delegator = new Delegator(userAuthBean.getConfig(), userAuthBean.getUserPreferences());
            workspace = new WorkspaceLayout(delegator, userAuthBean.getUserWorkspaceDir());
            catalog = new CatalogueLayout();

            loadLocalMetadataFiles();
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
//        } catch (ValidationException e) {
//            CommonUtils.handleValidationException(e);
//        }
    }

    public void search() {
        doSearch();
    }

    public void onToggleSearchMode(final AjaxBehaviorEvent event) {
        doSearch();
    }

    public void copyToWorkspace() {
        if (catalog.getSelectedItem() != null) {
            copyToWorkspace(catalog.getSelectedItem());
        } else {
            FacesMessageUtil.addErrorMessage("No selected metadata record .");
        }
    }

    public void copyToWorkspace(SearchResultItem item) {
        log.debug("Copy to workspace");
        String itemId = item.getMetadataFile().getFlatList().getId();
        try {
            if (userAuthBean.existsId(itemId)) {
                FacesMessageUtil.addErrorMessage("The metadata record {" + itemId + "} does already exist in the workspace");
            } else {
                if (userAuthBean.existsTitle(item.getMetadataFile().getFlatList().getTitle())) {
                    FacesMessageUtil.addErrorMessage("The metadata record title {" + item.getMetadataFile().getFlatList().getTitle() + "} does already exist in the workspace");
                } else {
                    String geoJson = null;
                    if (StringUtils.isNotEmpty(item.getGeoJsonLink())) {
                        // Invoke the URL to obtain GeoJson format of the metadata record
                        geoJson = delegator.getGeoJson(item.getGeoJsonLink());
                    }

                    MetadataFile metadataFile = metadataParser.buildImportedRecord(item.getMetadataFile().getXmlDoc(), geoJson);
                    metadataParser.validate(metadataFile, userAuthBean.getUserPreferences());

                    workspace.addMetadataFile(metadataFile);
                    String id = metadataFile.getFlatList().getId();
                    userAuthBean.addId(id);
                    if (StringUtils.isNotEmpty(metadataFile.getFlatList().getTitle())) {
                        userAuthBean.addTitle(metadataFile.getFlatList().getTitle());
                    }

                    //save record to local file system
                    saveMetadataRecordToFile(id, metadataFile);

                    if (workspace.getMetadataFiles() != null
                            && workspace.getMetadataFiles().size() > 0) {
                        log.debug("Set paginator for local layout");
                        if (workspace.getPaginator() == null) {
                            log.debug("Set a new paginator");
                            workspace.setPaginator(new StaticPaginator(workspace.getMetadataFiles(), userAuthBean.getConfig().getRowsPerPage()));
                        } else {
                            /*
                                Reload paginator
                             */
                            workspace.getPaginator().reload(workspace.getMetadataFiles());
                        }
                    }
                    FacesMessageUtil.addInfoMessage("Metadata record " + itemId + " has been copied to Workspace.");
                }
            }
        } catch (IOException | ParseException | XPathExpressionException e) {
            String errorMsg = "Error while adding metadata record " + itemId + " to workspace: " + CommonUtils.getErrorMessage(e);
            log.debug(errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    /**
     * Check existing of the current selected metadata record before copying it
     * to the catalogue
     *
     * @param removeSource Remove the dedicated metadata record from the
     * workspace?
     */
    public void checkExistingAndCopyToCatalogue(boolean removeSource) {
        checkExistingAndCopyToCatalogue(workspace.getSelectedMetadataFile(), removeSource);
    }

    /**
     * Check existing of the given metadata record before copying it to the
     * catalogue
     *
     * @param metadataFile
     * @param removeSource Remove the dedicated metadata record from the
     * workspace?
     */
    public void checkExistingAndCopyToCatalogue(MetadataFile metadataFile, boolean removeSource) {
        if (removeSource) {
            log.debug("Move the metadata record " + metadataFile.getFlatList().getId() + " from workspace to catalogue");
            catalogueAction = ACTION.MOVE;
        } else {
            log.debug("Copy the metadata record " + metadataFile.getFlatList().getId() + " from workspace to catalogue");
            catalogueAction = ACTION.COPY;
        }

        workspace.setSelectedMetadataFile(metadataFile);
        try {
            /*
                Firstly check if the metadata record exists in the catalogue
             */
            String metaId = metadataFile.getMetadata().getOthers().getFileIdentifier();
            boolean exist = delegator.checkExist(metaId, metadataFile.getMetadata().isService());
            if (exist) {
                log.debug("Metadata record " + metaId + " already exists in the catalogue");
                /*
                 show confirmation dialog
                 */

                //RequestContext context = RequestContext.getCurrentInstance();
                log.debug("Update confirmation dialog");

                UIComponent ui = CommonUtils.getUIComponent("wsCommonForm");
                if (ui != null) {
                    log.debug("Found component: " + ui.getClientId());
                    FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(ui.getClientId());
                }

                if (removeSource) {
                    // move                    
                    // show dialog
                    PrimeFaces.current().executeScript("PF('wsMoveOverwriteOneConfirmWv').show();");

                } else {
                    // copy
                    // show dialog
                    PrimeFaces.current().executeScript("PF('wsCopyOverwriteOneConfirmWv').show();");
                }

            } else {
                copyToCatalogue(removeSource);
            }

        } catch (IOException e) {
            FacesMessageUtil.addErrorMessage("Errors: " + e.getMessage());
        }
    }

    /**
     * Copy the current selected metadata record to the catalogue
     *
     * @param removeSource Remove also the metadata from workspace?
     */
    public void copyToCatalogue(boolean removeSource) {
        if (removeSource) {
            log.debug("Move the current selected metadata record " + workspace.getSelectedMetadataFile().getFlatList().getId() + " from workspace to catalogue");
        } else {
            log.debug("Copy the current selected metadata record " + workspace.getSelectedMetadataFile().getFlatList().getId() + " from workspace to catalogue");
        }

        try {
            boolean copyFile = false;
            if (StringUtils.isNotEmpty(workspace.getSelectedMetadataFile().getFileName())) {
                File f = new File(userAuthBean.getUserWorkspaceDir() + "/" + workspace.getSelectedMetadataFile().getFileName());
                copyFile = f.exists();
            }

            if (copyFile) {
                log.debug("case: copy file");
                if (userAuthBean.getUserPreferences().getCatalogue().isAuthenticated()) {
                    log.debug("Authenticated");

                    String recordFile = userAuthBean.getUserWorkspaceDir() + "/" + workspace.getSelectedMetadataFile().getFileName();

                    // insert file
                    String recordId = delegator.harvestFile(recordFile, workspace.getSelectedMetadataFile().getMetadata().isService());

                    workspace.getSelectedMetadataFile().setInserted(true);

                    if (removeSource) {
                        // remove copied file from workspace
                        removeFromWorkspace(workspace.getSelectedMetadataFile());

                        // reload paginator
                        workspace.getPaginator().reload(workspace.getMetadataFiles());

                        FacesMessageUtil.addInfoMessage("The metadata record has been moved to catalogue. It can be found at: " + recordId);

                    } else {
                        FacesMessageUtil.addInfoMessage("The metadata record has been copied into catalogue. It can be found at: " + recordId);
                    }

                    // hide the catalogue authentication dlg
                    toogleCatalogueAuthenticationForm(false);
                } else {
                    log.debug("Not authenticated yet");
                    if (removeSource) {
                        catalogueAction = ACTION.MOVE;
                    } else {
                        catalogueAction = ACTION.COPY;
                    }

                    // show the catalogue authentication dlg
                    toogleCatalogueAuthenticationForm(true);
                }

            } else {
                log.debug("case: copy XML message");
                if (workspace.getSelectedMetadataFile().getXmlDoc() != null) {
                    if (userAuthBean.getUserPreferences().getCatalogue().isAuthenticated()) {
                        log.debug("Authenticated");
                        // instert XML metadata
                        String recordId = delegator.harvestDOM(workspace.getSelectedMetadataFile().getXmlDoc(),
                                workspace.getSelectedMetadataFile().getMetadata().isService());

                        if (removeSource) {
                            // remove copied file from workspace
                            removeFromWorkspace(workspace.getSelectedMetadataFile());

                            // reload paginator
                            workspace.getPaginator().reload(workspace.getMetadataFiles());

                            FacesMessageUtil.addInfoMessage("The metadata record has been moved to catalogue. It can be found at: " + recordId);
                        } else {
                            FacesMessageUtil.addInfoMessage("The metadata record has been copied into catalogue. It can be found at: " + recordId);
                        }
                        // hide the catalogue authentication dlg
                        toogleCatalogueAuthenticationForm(false);
                    } else {
                        log.debug("Not authenticated yet");
                        if (removeSource) {
                            catalogueAction = ACTION.MOVE;
                        } else {
                            catalogueAction = ACTION.COPY;
                        }

                        // show the catalogue authentication dlg
                        toogleCatalogueAuthenticationForm(true);
                    }

                } else {
                    FacesMessageUtil.addErrorMessage("The selected metadata record is null.");
                }
            }
        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            FacesMessageUtil.addErrorMessage("Errors: " + e.getMessage());
        } catch (AuthenticationException e) {
            handleAuthenticationException(e);
        }
    }

    /**
     * Copy all selected metadata records to the catalogue
     *
     * @param removeSources Remove the metadata records from the workspace?
     */
    public void copyMultipleToCatalogue(boolean removeSources) {
        if (removeSources) {
            log.debug("Move all selected metadata to catalogue ");
        } else {
            log.debug("Copy all selected metadata to catalogue ");
        }

        try {
            if (userAuthBean.getUserPreferences().getCatalogue().isAuthenticated()) {
                if (workspace.getMetadataFiles() != null
                        && !workspace.getMetadataFiles().isEmpty()) {

                    List<MetadataFile> seriesFilesList = new ArrayList<>();
                    List<MetadataFile> serviceFilesList = new ArrayList<>();

                    if (workspace.getSelectedCount() > 0) {
                        int count = 0;
                        for (MetadataFile item : workspace.getMetadataFiles()) {
                            if (item.isSelected()) {
                                if (item.getMetadata().isService()) {
                                    serviceFilesList.add(item);
                                } else {
                                    seriesFilesList.add(item);
                                }
                                count++;
                            }
                            if (count >= workspace.getSelectedCount()) {
                                break;
                            }
                        }
                        workspace.unSelectMetadataFiles();
                    } else {
                        workspace.getMetadataFiles().forEach((item) -> {
                            if (item.getMetadata().isService()) {
                                serviceFilesList.add(item);
                            } else {
                                seriesFilesList.add(item);
                            }
                        });
                    }

                    List<MetadataFile> nonExistingSelectedItems = null;
                    if (removeSources) {
                        nonExistingSelectedItems = new ArrayList<>();
                    }

                    boolean ok = false;
                    if (!seriesFilesList.isEmpty() || !serviceFilesList.isEmpty()) {
                        workspace.setOverwriteMetadataFiles(new ArrayList<>());
                    }

                    List<String> details = new ArrayList<>();
                    if (!seriesFilesList.isEmpty()) {
                        ok = copy(seriesFilesList, nonExistingSelectedItems, false, details);
                    }

                    if (!serviceFilesList.isEmpty()) {
                        ok = copy(serviceFilesList, nonExistingSelectedItems, true, details);
                    }

                    /**
                     * remove inserted items
                     */
                    removeFromWorkspace(nonExistingSelectedItems);

                    if (workspace.getOverwriteMetadataFiles() != null
                            && workspace.getOverwriteMetadataFiles().size() > 0) {

                        // show confirmation dialog
                        //RequestContext context = RequestContext.getCurrentInstance();
                        log.debug("Update confirmation dialog");

                        UIComponent ui = CommonUtils.getUIComponent("wsCommonForm");
                        if (ui != null) {
                            log.debug("Found component: " + ui.getClientId());
                            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(ui.getClientId());
                        }

                        // show dialog
                        if (removeSources) {
                            PrimeFaces.current().executeScript("PF('wsMoveOverwriteMultipleConfirmWv').show();");
                        } else {
                            PrimeFaces.current().executeScript("PF('wsCopyOverwriteMultipleConfirmWv').show();");
                        }
                    } else {

                        toogleCatalogueAuthenticationForm(false);
                        if (ok) {
                            if (!details.isEmpty()
                                    && delegator.getConfig().isReportCatalogMsg()) {
                                for (String str : details) {
                                    FacesMessageUtil.addInfoMessage(str);
                                }
                            } else {
                                if (removeSources) {
                                    FacesMessageUtil.addInfoMessage("All selected metadata records have been moved to catalogue.");
                                } else {
                                    FacesMessageUtil.addInfoMessage("All selected metadata records have been copied to catalogue.");
                                }
                            }
                        }
                    }
                } else {
                    FacesMessageUtil.addErrorMessage("There is no metadata record in the workspace");
                }

            } else {
                if (removeSources) {
                    catalogueAction = ACTION.MOVE_SELECTIONS;
                } else {
                    catalogueAction = ACTION.COPY_SELECTIONS;
                }
                toogleCatalogueAuthenticationForm(true);
            }

        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            FacesMessageUtil.addErrorMessage("Error while inserting selected metadata records into catalogue: " + e.getMessage());
        } catch (AuthenticationException e) {
            handleAuthenticationException(e);
        }
    }

    private boolean copy(List<MetadataFile> metadataFiles,
            List<MetadataFile> nonExistingSelectedItems, boolean serviceMetadata, List<String> details)
            throws IOException, AuthenticationException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
        //zip selected metadata                
        byte[] zipBytes = workspace.zipToCopy(metadataFiles, nonExistingSelectedItems, serviceMetadata);

        if (zipBytes != null) {
            // write zip bytes to a temporary zip file                 
            String zipFile = delegator.getConfig().getTempDir() + "/" + CommonUtils.generateFileName() + ".zip";
            log.debug("zip file: " + zipFile);
            String result = null;
            try {
                FileUtils.writeByteArrayToFile(new File(zipFile), zipBytes);

                // post the zip file to the catalogue                 
                result = delegator.harvestZipFile(zipFile, serviceMetadata);
                log.debug(result);
            } catch (AuthenticationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
                FileUtils.deleteQuietly(new File(zipFile));
                throw e;
            }

            if (StringUtils.isNotEmpty(result) && details != null) {
                details.add(result);
            }
            return true;
        } else {
            return false;
        }
    }

    public void replaceSelections(boolean removeSources) {
        log.debug("Replace dedicated metadata records in the catalogue");

        try {
            if (workspace.getOverwriteMetadataFiles() != null
                    && workspace.getOverwriteMetadataFiles().size() > 0) {
                if (userAuthBean.getUserPreferences().getCatalogue().isAuthenticated()) {
                    List<MetadataFile> seriesFiles = new ArrayList<>();
                    List<MetadataFile> serviceFiles = new ArrayList<>();

                    for (MetadataFile mFile : workspace.getOverwriteMetadataFiles()) {
                        if (mFile.getMetadata().isService()) {
                            serviceFiles.add(mFile);
                        } else {
                            seriesFiles.add(mFile);
                        }
                    }

                    List<String> details = new ArrayList<>();
                    if (!seriesFiles.isEmpty()) {
                        replace(seriesFiles, false, details);
                    }

                    if (!serviceFiles.isEmpty()) {
                        replace(serviceFiles, true, details);
                    }

                    if (!details.isEmpty() && delegator.getConfig().isReportCatalogMsg()) {
                        for (String msg : details) {
                            FacesMessageUtil.addInfoMessage(msg);
                        }

                    } else {
                        FacesMessageUtil.addInfoMessage("The metadata records have been replaced");
                    }

                    toogleCatalogueAuthenticationForm(false);

                    if (removeSources) {
                        /**
                         * remove inserted items from the workspace
                         */
                        removeFromWorkspace(workspace.getOverwriteMetadataFiles());
                    }

                    workspace.setOverwriteMetadataFiles(null);
                } else {
                    if (removeSources) {
                        catalogueAction = ACTION.MOVE_REPLACE_SELECTIONS;
                    } else {
                        catalogueAction = ACTION.REPLACE_SELECTIONS;
                    }
                    toogleCatalogueAuthenticationForm(true);
                }
            }
        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            FacesMessageUtil.addErrorMessage("Error while inserting metadata records into catalogue: " + e.getMessage());
        } catch (AuthenticationException e) {
            handleAuthenticationException(e);
        }
    }

    private void replace(List<MetadataFile> files, boolean serviceMetadata, List<String> details) throws IOException, AuthenticationException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
        // zip the metadata records                    
        byte[] zipBytes = workspace.zipToReplace(files);

        if (zipBytes != null) {
            // write zip bytes to a temporary zip file                    
            String zipFile = delegator.getConfig().getTempDir() + "/" + CommonUtils.generateFileName() + ".zip";
            log.debug("zip file: " + zipFile);

            try {
                FileUtils.writeByteArrayToFile(new File(zipFile), zipBytes);

                // post the zip file to the catalogue                     
                String result = delegator.harvestZipFile(zipFile, serviceMetadata);

                if (StringUtils.isNotEmpty(result) && details != null) {
                    details.add(result);
                }
            } catch (AuthenticationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
                FileUtils.deleteQuietly(new File(zipFile));
                throw e;
            }
            /*
                        if (StringUtils.isNotEmpty(result)) {
                            FacesMessageUtil.addInfoMessage(result);
                        }
             */
        }
    }

    public void removeFromCatalogue(SearchResultItem item) {
        log.debug("Remove item " + item.getMetadataFile().getFlatList().getId() + " from the catalogue");
        catalog.setSelectedItem(item);
        catalogueAction = ACTION.REMOVE;
        removeFromCatalogue();
    }

    public void removeCurrentItemFromCatalogue() {
        log.debug("Remove current item from the catalogue");
        catalogueAction = ACTION.REMOVE;
        removeFromCatalogue();
    }

    private void removeFromCatalogue() {
        log.debug("Remove metadata from catalogue");
        if (catalog.getSelectedItem() != null) {
            String itemId = catalog.getSelectedItem().getMetadataFile().getFlatList().getId();
            if (StringUtils.isNotEmpty(itemId)) {
                try {
                    if (userAuthBean.getUserPreferences().getCatalogue().isAuthenticated()) {
                        delegator.removeMetadata(itemId,
                                catalog.getSelectedItem().getMetadataFile().getMetadata().isService());

                        toogleCatalogueAuthenticationForm(false);

                        /*
                         refresh the search page
                         */
                        //doSearch();
                        nextAction("refreshSearch");

                    } else {
                        toogleCatalogueAuthenticationForm(true);
                    }

                } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
                    String errorMsg = CommonUtils.getErrorMessage(e);
                    log.debug("Error while removing metadata record " + itemId + " from catalogue: " + errorMsg);
                    FacesMessageUtil.addErrorMessage(errorMsg);
                } catch (AuthenticationException e) {
                    handleAuthenticationException(e);
                }
            } else {
                FacesMessageUtil.addErrorMessage("The metadata record hasn't been removed from catalogue because of identifier missing.");
            }
        } else {
            log.debug("No selected metadata.");
            FacesMessageUtil.addErrorMessage("No selected metadata record.");
        }
    }

    public void copySelectionsToWorkspace() {
        log.debug("Copy all selected metadata records from catalogue search results to workspace");
        if (catalog.getSelectedCount() > 0) {
            if (catalog.getPaginator() != null
                    && catalog.getPaginator().getData() != null
                    && catalog.getPaginator().getData().getItems() != null) {

                List<SearchResultItem> copyingItems = new ArrayList<>();
                List<SearchResultItem> existingIdItems = new ArrayList<>();
                List<SearchResultItem> existingTitleItems = new ArrayList<>();
                for (SearchResultItem item : catalog.getPaginator().getData().getItems()) {
                    if (item.isSelected()) {
                        if (userAuthBean.existsId(item.getMetadataFile().getFlatList().getId())) {
                            existingIdItems.add(item);
                        } else {
                            if (userAuthBean.existsTitle(item.getMetadataFile().getFlatList().getTitle())) {
                                existingTitleItems.add(item);
                            } else {
                                copyingItems.add(item);
                            }
                        }
                        item.setSelected(false);
                        catalog.decreaseSelectedCount();
                    }
                    if (catalog.getSelectedCount() <= 0) {
                        break;
                    }
                }
                catalog.setSelectedAll(false);

                List<String> errorItems = new ArrayList<>();
                if (!copyingItems.isEmpty()) {
                    for (SearchResultItem item : copyingItems) {
                        try {
                            String geoJson = null;
                            if (StringUtils.isNotEmpty(item.getGeoJsonLink())) {
                                // Invoke the URL to obtain GeoJson format of the metadata record
                                geoJson = delegator.getGeoJson(item.getGeoJsonLink());
                            }

                            MetadataFile metadataFile = metadataParser
                                    .buildImportedRecord(item.getMetadataFile().getXmlDoc(), geoJson);
                            metadataParser.validate(metadataFile, userAuthBean.getUserPreferences());

                            workspace.addMetadataFile(metadataFile);
                            String id = metadataFile.getFlatList().getId();
                            userAuthBean.addId(id);
                            if (StringUtils.isNotEmpty(metadataFile.getFlatList().getTitle())) {
                                userAuthBean.addTitle(metadataFile.getFlatList().getTitle());
                            }

                            //save record to local file system
                            saveMetadataRecordToFile(id, metadataFile);
                        } catch (IOException | ParseException | XPathExpressionException e) {
                            String error = item.getMetadataFile().getFlatList().getTitle()
                                    + " (" + item.getMetadataFile().getFlatList().getId() + ")"
                                    + " :" + CommonUtils.getErrorMessage(e);
                            errorItems.add(error);
                        }
                    }

                    if (workspace.getMetadataFiles() != null
                            && workspace.getMetadataFiles().size() > 0) {
                        log.debug("Set paginator for local layout");
                        if (workspace.getPaginator() == null) {
                            log.debug("Set a new paginator");
                            workspace.setPaginator(new StaticPaginator(workspace.getMetadataFiles(),
                                    userAuthBean.getConfig().getRowsPerPage()));
                        } else {
                            /*
                                Reload paginator
                             */
                            workspace.getPaginator().reload(workspace.getMetadataFiles());
                        }
                    }
                }

                if (existingIdItems.isEmpty()
                        && existingTitleItems.isEmpty()
                        && errorItems.isEmpty()) {
                    FacesMessageUtil.addInfoMessage(copyingItems.size() + " metadata records have been copied to Workspace");
                } else {
                    StringBuilder sb = new StringBuilder();
                    int copiedNum = copyingItems.size() - (existingIdItems.size() + existingTitleItems.size() + errorItems.size());
                    sb.append(copiedNum > 0 ? copiedNum : 0).append(" metadata records have been copied to Workspace");

                    if (!existingIdItems.isEmpty()) {
                        sb.append("<br/>");
                        sb.append(existingIdItems.size()).append(" existing identifier records: ");
                        existingIdItems.forEach((item) -> {
                            sb.append("<br/>");
                            sb.append(" - ").append(item.getMetadataFile().getFlatList().getId());
                        });
                    }

                    if (!existingTitleItems.isEmpty()) {
                        sb.append("<br/>");
                        sb.append(existingTitleItems.size()).append(" existing title records: ");
                        existingTitleItems.forEach((item) -> {
                            sb.append("<br/>");
                            sb.append(" - ").append(item.getMetadataFile().getFlatList().getTitle());
                        });
                    }

                    if (!errorItems.isEmpty()) {
                        sb.append("<br/>");
                        sb.append(errorItems.size()).append(" error records: ");
                        errorItems.forEach((str) -> {
                            sb.append("<br/>");
                            sb.append(" - ").append(str);
                        });
                    }

                    FacesMessageUtil.addErrorMessage(sb.toString());
                }
            } else {
                FacesMessageUtil.addErrorMessage("No metadata record to be copied.");
            }
        } else {
            FacesMessageUtil.addErrorMessage("No metadata record to be copied.");
        }
    }

    public void removeSelectionsFromCatalogue() {
        log.debug("Remove all selected metadata from catalogue");
        if (catalog.getSelectedCount() > 0) {
            if (catalog.getPaginator() != null
                    && catalog.getPaginator().getData() != null
                    && catalog.getPaginator().getData().getItems() != null) {
                int count = 0;
                List<String> removingSeriesItems = new ArrayList<>();
                List<String> removingServiceItems = new ArrayList<>();
                List<String> removedItems = new ArrayList<>();

                try {
                    for (SearchResultItem item : catalog.getPaginator().getData().getItems()) {
                        if (item.isSelected()) {
                            //delegator.removeMetadata(item.getProductId());
                            count++;
                            if (item.getMetadataFile().getMetadata().isService()) {
                                removingServiceItems.add(item.getMetadataFile().getFlatList().getId());
                            } else {
                                removingSeriesItems.add(item.getMetadataFile().getFlatList().getId());
                            }
                        }
                        if (count > catalog.getSelectedCount()) {
                            break;
                        }
                    }

                    if (removingSeriesItems.size() > 0 || removingServiceItems.size() > 0) {
                        if (userAuthBean.getUserPreferences().getCatalogue().isAuthenticated()) {
                            count = 0;
                            for (String itemId : removingSeriesItems) {
                                delegator.removeMetadata(itemId, false);
                                removedItems.add(itemId);
                                count++;
                            }
                            for (String itemId : removingServiceItems) {
                                delegator.removeMetadata(itemId, true);
                                removedItems.add(itemId);
                                count++;
                            }

                            /*
                             refresh the search page
                             */
                            //doSearch();
                            nextAction("refreshSearch");

                            FacesMessageUtil.addInfoMessage(count + " metadata records have been removed from catalogue.");

                            toogleCatalogueAuthenticationForm(false);
                        } else {
                            catalogueAction = ACTION.REMOVE_SELECTIONS;
                            toogleCatalogueAuthenticationForm(true);
                        }
                    } else {
                        FacesMessageUtil.addErrorMessage("No metadata record to be removed.");
                    }
                } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
                    String errorMsg = CommonUtils.getErrorMessage(e);
                    FacesMessageUtil.addErrorMessage(errorMsg + ". Only " + count + " following metadata records have been remove from catalogue: " + StringUtils.join(removedItems, ";"));
                } catch (AuthenticationException e) {
                    handleAuthenticationException(e);
                }
            } else {
                FacesMessageUtil.addErrorMessage("No metadata record to be removed.");
            }
        } else {
            FacesMessageUtil.addErrorMessage("No metadata record to be removed.");
        }
    }

    public void refreshSearch() {
        try {
            catalog.setPaginator(delegator.doSearch(catalog.isServiceSearch()));
            if (catalog.getView().isDetails() || catalog.getView().isXml()) {
                if (catalog.getPaginator() != null
                        && catalog.getPaginator().getData() != null
                        && catalog.getPaginator().getData().getItems() != null
                        && !catalog.getPaginator().getData().getItems().isEmpty()) {

                    // get the first item
                    catalog.setSelectedItem(catalog.getPaginator().getData().getItems().get(0));
                } else {
                    log.debug("Search result has no metadata record.");
                    catalog.getView().toList();
                }
            }
        } catch (SearchException e) {
            String errorMsg = CommonUtils.getErrorMessage(e);
            log.debug("Search exception: " + errorMsg);
            FacesMessageUtil.addErrorMessage(e.getTitle());
        } catch (IOException e) {
            String errorMsg = CommonUtils.getErrorMessage(e);
            log.debug("IO exception: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    public void createNewSeries() {
        log.debug("Create a new collection metadata record");
        cloneIsoTemplate(SERIES_TEMPLATE);
    }

    public void createNewService() {
        log.debug("Create a new service metadata record");
        cloneIsoTemplate(SERVICE_TEMPLATE);
    }

    public void cloneSelectedMetadataRecord() {
        log.debug("Clone the current selected metadata record");
        if (workspace.getSelectedMetadataFile() != null) {
            clone(workspace.getSelectedMetadataFile());
        }
    }

    public void cloneMetadataRecord(MetadataFile metadataFile) {
        log.debug("Clone the metadata record");
        clone(metadataFile);
    }

    private void cloneIsoTemplate(String template) {
        try {
            List<String> values = new ArrayList<>();
            Document isoDoc = delegator.getId(userAuthBean.getConfig().getXmlDir() + "/" + template, values);
            if (!values.isEmpty() && values.size() == 1) {
                String identifier = values.get(0);
                identifier = getAvailableId(identifier);

                MetadataFile metadataFile = metadataParser.cloneTemplateFile(isoDoc, identifier);

                postClone(identifier, metadataFile);
            }
        } catch (IOException | ParseException | SAXException e) {
            FacesMessageUtil.addErrorMessage(e);
        }
    }

    private void clone(MetadataFile metadataFile) {
        try {
            String identifier = metadataFile.getMetadata().getOthers().getFileIdentifier();
            identifier = getAvailableId(identifier);

            String filePath = userAuthBean.getUserWorkspaceDir() + "/" + metadataFile.getFileName();
            MetadataFile newMetadataFile = metadataParser.cloneJsonFile(filePath, identifier);
            postClone(identifier, newMetadataFile);
        } catch (IOException | ParseException | SAXException e) {
            FacesMessageUtil.addErrorMessage(e);
        }
    }

    private void postClone(String identifier, MetadataFile metadataFile) throws IOException {
        metadataParser.validate(metadataFile, userAuthBean.getUserPreferences());

        saveMetadataRecordToFile(identifier, metadataFile);

        workspace.addMetadataFile(metadataFile);
        if (workspace.getPaginator() == null) {
            workspace.setPaginator(new StaticPaginator(workspace.getMetadataFiles(), userAuthBean.getConfig().getRowsPerPage()));
        } else {
            workspace.getPaginator().reload(workspace.getMetadataFiles());
        }

        // add metadata record identifier to the list to be used for checking existing
        userAuthBean.addId(identifier);
        userAuthBean.addTitle(metadataFile.getFlatList().getTitle());

        workspace.onEdit(metadataFile);
    }

    private String getAvailableId(String id) {
        while (true) {
            id = CommonUtils.addIncrementalCounter(id);
            if (!userAuthBean.existsId(id)) {
                return id;
            }
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        log.debug("Handle multiple upload files");
        try {
            handleFileUpload(event.getFile());
        } catch (ValidationException e) {
            CommonUtils.handleValidationException(e);
        }
    }

    public void keepSessionAlive() {
        if (log.isDebugEnabled()) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.debug("Poll to keep session alive at " + formatter.format(new Date(System.currentTimeMillis())));
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getSession(true);
    }

    private void handleFileUpload(UploadedFile upFile) throws ValidationException {
        if (upFile != null) {
            String fileName = upFile.getFileName();
            log.debug("File name: " + fileName);

            if (StringUtils.endsWithIgnoreCase(fileName, ".xml")
                    || StringUtils.endsWithIgnoreCase(fileName, ".json")
                    || StringUtils.endsWithIgnoreCase(fileName, ".zip")) {
                /*
                    Handle XML/ISO file
                 */
                if (StringUtils.endsWithIgnoreCase(fileName, ".xml")
                        || StringUtils.endsWithIgnoreCase(fileName, ".json")) {

                    String fileContent = new String(upFile.getContents(), StandardCharsets.UTF_8);
                    List<String> existingIdFiles = new ArrayList<>();
                    List<String> existingTitleFiles = new ArrayList<>();
                    List<String> exceptions = new ArrayList<>();

                    boolean success = handleUploadFile(StringUtils.endsWithIgnoreCase(fileName, ".json"),
                            fileContent, false, fileName, existingIdFiles, existingTitleFiles, exceptions);

                    if (success) {
                        refreshPaginator();
                    } else {
                        if (!existingIdFiles.isEmpty()) {
                            FacesMessageUtil.addErrorMessage("The identifier " + existingIdFiles.get(0) + " does already exist");
                        }
                        if (!existingTitleFiles.isEmpty()) {
                            FacesMessageUtil.addErrorMessage("The title " + existingTitleFiles.get(0) + " does already exist");
                        }

                        if (!exceptions.isEmpty()) {
                            FacesMessageUtil.addErrorMessage("Error while handling upload file " + exceptions.get(0));
                        }
                    }
                }

                /*
                    Handle zip file
                 */
                if (StringUtils.endsWithIgnoreCase(fileName, ".zip")) {
                    try {
                        handleUploadedZipFile(upFile);
                    } catch (IOException e) {
                        log.debug("Error while handling upload file: " + e);
                        FacesMessageUtil.addErrorMessage(e);
                    }
                }
            } else {
                String errorMsg = "Upload file should be either XML or ZIP file.";
                log.debug(errorMsg);
                FacesMessageUtil.addErrorMessage(errorMsg);
            }
        }
    }

    private void handleUploadedZipFile(UploadedFile upFile) throws IOException, ValidationException {
        try {
            String unzipDir = userAuthBean.getUserWorkspaceDir()
                    + "/" + CommonUtils.generateFileName() + "/"
                    + FilenameUtils.removeExtension(upFile.getFileName());
            log.debug("Unzip dir: " + unzipDir);

            /*
             Unzip the uploaded zip file
             */
            CommonUtils.unzip(upFile.getInputstream(), unzipDir);

            /*
             Search for all XML and JSON files                       
             */
            Collection<File> recordFiles = CommonUtils.getXmlAndJsonFiles(unzipDir);
            List<String> existingIdFiles = new ArrayList<>();
            List<String> existingTitleFiles = new ArrayList<>();
            List<String> exceptions = new ArrayList<>();
            int count = 0;

            for (File rFile : recordFiles) {
                boolean ok = false;

                if (StringUtils.endsWithIgnoreCase(rFile.getName(), ".json")) {
                    String json = FileUtils.readFileToString(new File(rFile.getAbsolutePath()), StandardCharsets.UTF_8.name());
                    if (StringUtils.isNotEmpty(json)) {
                        ok = handleUploadFile(true, json,
                                false, rFile.getName(), existingIdFiles, existingTitleFiles, exceptions);
                    } else {
                        exceptions.add(rFile.getName() + ": empty");
                    }
                } else {
                    ok = handleUploadFile(false, rFile.getAbsolutePath(),
                            true, rFile.getName(), existingIdFiles, existingTitleFiles, exceptions);
                }

                if (ok) {
                    count++;
                }
            }

            /*
                Delete unzip directory
             */
            FileUtils.deleteQuietly(new File(unzipDir));

//            if (errorFiles != null && errorFiles.size() > 0) {
//                throw new ValidationException(errorFiles);
//            }            
            if (existingIdFiles.isEmpty() && existingTitleFiles.isEmpty() && exceptions.isEmpty()) {
                FacesMessageUtil.addInfoMessage(count + " metadata record files have been uploaded");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(count).append(" metadata record files have been uploaded");

                if (!existingIdFiles.isEmpty()) {
                    sb.append("<br/>");
                    sb.append(existingIdFiles.size()).append(" existing identifier files: ");
                    existingIdFiles.forEach((str) -> {
                        sb.append("<br/>");
                        sb.append(" - ").append(str);
                    });
                }

                if (!existingTitleFiles.isEmpty()) {
                    sb.append("<br/>");
                    sb.append(existingTitleFiles.size()).append(" existing title files: ");
                    existingTitleFiles.forEach((str) -> {
                        sb.append("<br/>");
                        sb.append(" - ").append(str);
                    });
                }

                if (!exceptions.isEmpty()) {
                    sb.append("<br/>");
                    sb.append(exceptions.size()).append(" error files: ");
                    exceptions.forEach((str) -> {
                        sb.append("<br/>");
                        sb.append(" - ").append(str);
                    });
                }

                FacesMessageUtil.addErrorMessage(sb.toString());
            }

            if (count > 0) {
                refreshPaginator();
            }
        } catch (IOException e) {
            String errorMsg = "Error while unzipping the uploaded file " + upFile.getFileName() + ": " + CommonUtils.getErrorMessage(e);
            log.debug(errorMsg);
            throw e;
        }
    }

    private boolean handleUploadFile(boolean isJson, String source, boolean isFile, String sourceName,
            List<String> existingIdFiles, List<String> existingTitleFiles, List<String> exceptions) {

        try {
            String xmlSource;
            if (isJson) {
                xmlSource = metadataParser.getSupplementalInformation(source);
                if (StringUtils.isEmpty(xmlSource)) {
                    exceptions.add(sourceName + " should be in the internal metadata format");
                    return false;
                }
            } else {
                xmlSource = source;
            }

            List<String> values = new ArrayList<>();
            Document uploadedDoc = delegator.getIdAndTitle(xmlSource, values, isFile);
            if (uploadedDoc != null) {
                boolean ok = true;
                if (!values.isEmpty() && values.size() == 2) {
                    String id = values.get(0);
                    log.debug("Id = " + id);
                    if (StringUtils.isNotEmpty(id)
                            && userAuthBean.existsId(id)) {
                        ok = false;
                        existingIdFiles.add(sourceName + ": " + id);
                        log.debug("The metadata record {" + sourceName + "} does already exist in the workspace");
                    }

                    String title = values.get(1);
                    log.debug("Title = " + title);
                    if (StringUtils.isNotEmpty(title)
                            && userAuthBean.existsTitle(title)) {
                        ok = false;
                        existingTitleFiles.add(sourceName + ": " + title);
                        log.debug("The metadata record title {" + title + "} does already exist in the workspace");
                    }

                    if (ok) {
                        MetadataFile metadataFile;
                        if (isJson) {
                            metadataFile = metadataParser.buildImportedRecord(uploadedDoc, source);
                        } else {
                            metadataFile = metadataParser.buildMetadataFile(uploadedDoc, true);
                        }

                        metadataParser.validate(metadataFile, userAuthBean.getUserPreferences());

                        workspace.addMetadataFile(metadataFile);
                        id = metadataFile.getFlatList().getId();
                        userAuthBean.addId(id);
                        if (StringUtils.isNotEmpty(title)) {
                            userAuthBean.addTitle(title);
                        }

                        //save record to local file system
                        saveMetadataRecordToFile(id, metadataFile);
                        return true;
                    }
                }
            } else {
                exceptions.add(sourceName + ": empty");
            }
        } catch (IOException | ParseException | SAXException | JSONException | XPathExpressionException e) {
            exceptions.add(sourceName + ": " + e.getMessage());
        }
        return false;
    }

    private void refreshPaginator() {
        if (workspace.getMetadataFiles() != null
                && workspace.getMetadataFiles().size() > 0) {
            log.debug("Set paginator for local layout; num of metadata records: " + workspace.getMetadataFiles().size());
            if (workspace.getPaginator() == null) {
                log.debug("Set a new paginator");
                workspace.setPaginator(new StaticPaginator(workspace.getMetadataFiles(), userAuthBean.getConfig().getRowsPerPage()));
            } else {
                // Reload paginator                                           
                workspace.getPaginator().reload(workspace.getMetadataFiles());
            }
        }
    }

    private void loadLocalMetadataFiles() {

        workspace.resetThesaurusWarn();

        //List<ValidationErrorsPerFile> errorFiles = null;
        //Collection<File> xmlFiles = CommonUtils.getXMLFiles(userAuthBean.getUserWorkspaceDir());
        Collection<File> internalMetadataRecordFiles = CommonUtils.getInternalMetadataFiles(userAuthBean.getUserWorkspaceDir());
        if (internalMetadataRecordFiles != null && internalMetadataRecordFiles.size() > 0) {
            if (workspace.getMetadataFiles() == null) {
                workspace.setMetadataFiles(new ArrayList<>());
            }

            internalMetadataRecordFiles.forEach((jsonFile) -> {
                try {
                    MetadataFile metadataFile = metadataParser
                            .loadInternalMetedataFile(jsonFile.getAbsolutePath(), userAuthBean.getUserPreferences());

                    workspace.addMetadataFile(metadataFile);
//                    if (metadataFile.getMetadata() != null) {
//                        workspace.addToWarnReports(metadataFile);
//                    }

                    // add metadata record identifier to the list to be used for checking existing
                    userAuthBean.addId(metadataFile.getMetadata().getOthers().getFileIdentifier());
                    if (StringUtils.isNotEmpty(metadataFile.getFlatList().getTitle())) {
                        userAuthBean.addTitle(metadataFile.getFlatList().getTitle());
                    }
                } catch (IOException | SAXException | ParseException e) {
                    log.debug("Error while loading XML metadata files on workspace directory: " + e.getMessage());
                }
            });
        }

        if (workspace.getMetadataFiles() != null
                && workspace.getMetadataFiles().size() > 0) {

            log.debug("Set paginator for local layout; num of metadata records: " + workspace.getMetadataFiles().size());
            if (workspace.getPaginator() == null) {
                log.debug("Set a new paginator");
                workspace.setPaginator(new StaticPaginator(workspace.getMetadataFiles(), userAuthBean.getConfig().getRowsPerPage()));
            } else {
                /*
                     Reload paginator
                 */
                workspace.getPaginator().reload(workspace.getMetadataFiles());
            }
        }

//            if (errorFiles != null && errorFiles.size() > 0) {
//                throw new ValidationException(errorFiles);
//            }
    }

    private void deleteZipFiles() {
        Collection<File> zipFiles = CommonUtils.getZipFiles(userAuthBean.getUserWorkspaceDir());
        if (zipFiles != null && zipFiles.size() > 0) {
            zipFiles.forEach((file) -> {
                log.debug("Deleting file " + file.getAbsolutePath());
                FileUtils.deleteQuietly(file);
            });
        }
    }

    private void doSearch() {
        try {
            String searchType = catalog.getSearchType();
            catalog = new CatalogueLayout();
            catalog.setSearchType(searchType);

            catalog.setPaginator(delegator.doSearch(catalog.isServiceSearch()));
        } catch (SearchException e) {
            String errorMsg = CommonUtils.getErrorMessage(e);
            log.debug("Search exception: " + errorMsg);
            FacesMessageUtil.addErrorMessage(e.getTitle());
        } catch (IOException e) {
            String errorMsg = CommonUtils.getErrorMessage(e);
            log.debug("IO exception: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    public void onChangeCatalogue(final AjaxBehaviorEvent event) {
        userAuthBean.changeCatalogue();
        refreshUserPreferences();
        try {
            delegator.loadCatalogueInfo();
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
        catalog = new CatalogueLayout();
    }

    public void refreshUserPreferences() {
        try {
            delegator = new Delegator(userAuthBean.getConfig(), userAuthBean.getUserPreferences());
        } catch (IOException ex) {
            String errorMsg = CommonUtils.getErrorMessage(ex);
            log.debug("Error: " + errorMsg);
            FacesMessageUtil.addErrorMessage(errorMsg);
        }
    }

    private void handleAuthenticationException(AuthenticationException e) {
        log.debug("handle authentication exception");

        this.catalogueAuthenticationMsg = "The user ("
                + userAuthBean.getUserPreferences().getCatalogue().getUsername()
                + ") does not have write access to the catalogue ("
                + userAuthBean.getUserPreferences().getCatalogue().getTitle()
                + "): " + e.getErrorMsg();

        userAuthBean.getUserPreferences().getCatalogue().setAuthenticated(false);

        // show the catalogue authentication dlg
        toogleCatalogueAuthenticationForm(true);
    }

    private void nextAction(String action) {
        PrimeFaces.current().ajax().addCallbackParam("nextAction", action);
    }

    private void toogleCatalogueAuthenticationForm(boolean show) {
        PrimeFaces.current().ajax().addCallbackParam("catAuthForm", show);
    }

    public void removeFromWorkspace() {
        log.debug("Remove metadata from workspace");
        if (workspace.getSelectedMetadataFile() != null) {
            int index = workspace.getSelectedMetadataFile().getIndex();

            removeFromWorkspace(workspace.getSelectedMetadataFile());

            /*
             Reload paginator
             */
            workspace.getPaginator().reload(workspace.getMetadataFiles());

            FacesMessageUtil.addInfoMessage("Metadata file " + workspace.getSelectedMetadataFile().getFlatList().getId() + " has been removed from Workspace.");

            workspace.reArrangedItems(index);

        } else {
            log.debug("No selected metadata");
        }
    }

    public void removeFromWorkspace(MetadataFile sFile) {
        log.debug("Remove metadata file " + sFile.getFlatList().getId() + " from workspace");

        String fileName = sFile.getFileName();
        if (StringUtils.isEmpty(fileName)) {
            fileName = CommonUtils.getFileName(sFile.getFlatList().getId());
        }

        if (StringUtils.isNotEmpty(sFile.getFileName())) {
            String xmlFile = userAuthBean.getUserWorkspaceDir() + "/" + fileName;
            if (!StringUtils.endsWithIgnoreCase(xmlFile, ".json")) {
                xmlFile = xmlFile + ".json";
            }

            if (FileUtils.deleteQuietly(new File(xmlFile))) {
                log.debug("Removed file " + xmlFile);
            } else {
                log.debug("Couldn't remove file " + xmlFile);
            }
        }

        workspace.removeMetadataFile(sFile);

        // remove the metadata record warnings
        workspace.removeWarnMetadataFile(sFile);

//        if (workspace.getMetadataFiles().remove(sFile)) {
//            log.debug("Removed metadata too. ");
//        } else {
//            log.debug("Couldn't remove metadata. ");
//        }

        /*
            Reload paginator
         */
        workspace.getPaginator().reload(workspace.getMetadataFiles());

        // Remove the metadata record identifier from the list
        userAuthBean.removeId(sFile.getMetadata().getOthers().getFileIdentifier());
        if (sFile.getMetadata().getIdentification() != null
                && StringUtils.isNotEmpty(sFile.getMetadata().getIdentification().getTitle())) {
            userAuthBean.removeTitle(sFile.getMetadata().getIdentification().getTitle());
        }
    }

    public void removeMultipleFromWorkspace() {
        log.debug("Remove selected/all metadata records from workspace");

        if (workspace.getMetadataFiles() != null
                && !workspace.getMetadataFiles().isEmpty()) {
            List<MetadataFile> removeList = new ArrayList<>();
            if (workspace.getSelectedCount() > 0) {
                for (MetadataFile item : workspace.getMetadataFiles()) {
                    if (item.isSelected()) {
                        removeList.add(item);
                    }
                    if (removeList.size() >= workspace.getSelectedCount()) {
                        break;
                    }
                }
                removeFromWorkspace(removeList);
                workspace.unSelectMetadataFiles();
            } else {
                workspace.getMetadataFiles().forEach((item) -> {
                    removeList.add(item);
                });
                removeFromWorkspace(removeList);
            }
        } else {
            FacesMessageUtil.addErrorMessage("There is no metadata record in the workspace");
        }
    }

    public void removeFromWorkspace(List<MetadataFile> removingItems) {
        if (removingItems != null && !removingItems.isEmpty()) {
            removingItems.forEach((item) -> {
                removeFromWorkspace(item);
            });

            /*
                Reload paginator
             */
            workspace.getPaginator().reload(workspace.getMetadataFiles());
        }
    }

    public void save(ActionEvent actionEvent) {
        log.debug("Save the modifications of selected metadata record");
        try {
            if (workspace.getSelectedMetadataFile() != null) {
                boolean valid = true;
//                if (workspace.getSelectedMetadataFile().getMetadata().getAcquisition() != null
//                        && workspace.getSelectedMetadataFile().getMetadata().getAcquisition().getPlatforms() != null) {
//                    for (Platform plf : workspace.getSelectedMetadataFile().getMetadata().getAcquisition().getPlatforms()) {
//                        if (plf.getAvailableInstruments() != null
//                                && plf.getAvailableInstruments().size() > 0
//                                && (plf.getInstruments() == null || plf.getInstruments().isEmpty())) {
//                            FacesMessageUtil.addErrorMessage(String
//                                    .format("The platform %s should have at least one instrument", plf.getLabel()));
//                            valid = false;
//                        }
//                    }
//                }
                if (valid) {
                    String oldId = XmlUtils.getNodeValue(workspace
                            .getSelectedMetadataFile()
                            .getMetadata().getOthers().getFileIdentifierNode());
                    String oldTitle = "";
                    if (workspace.getSelectedMetadataFile().getMetadata().getIdentification() != null
                            && workspace.getSelectedMetadataFile().getMetadata().getIdentification().getTitleNode() != null) {
                        oldTitle = XmlUtils.getNodeValue(workspace
                                .getSelectedMetadataFile().getMetadata().getIdentification().getTitleNode());
                    }

                    log.debug("Old identifier: " + oldId);
                    log.debug("Old title: " + oldTitle);

                    String id = workspace
                            .getSelectedMetadataFile().getMetadata().getOthers().getFileIdentifier();
                    log.debug("Current identifier " + id);

                    if (!oldId.equalsIgnoreCase(id) && userAuthBean.existsId(id)) {
                        FacesMessageUtil.addErrorMessage("The metadata record {" + id + "} does already exist in the workspace");
                    } else {
                        String title = "";
                        if (workspace.getSelectedMetadataFile().getMetadata().getIdentification() != null
                                && StringUtils.isNotEmpty(workspace.getSelectedMetadataFile().getMetadata().getIdentification().getTitle())) {
                            title = workspace.getSelectedMetadataFile().getMetadata().getIdentification().getTitle();
                        }

                        if (StringUtils.isNotEmpty(title) && !oldTitle.equalsIgnoreCase(title)
                                && userAuthBean.existsTitle(title)) {
                            workspace.getSelectedMetadataFile()
                                    .getMetadata().getIdentification().setTitle(oldTitle);
                            workspace.getSelectedMetadataFile()
                                    .getFlatList().setTitle(oldTitle);
                            FacesMessageUtil.addErrorMessage("The metadata record title {" + title + "} does already exist in the workspace");
                        } else {
                            // update MetadataFile
                            metadataParser.update(userAuthBean.getUserWorkspaceDir(),
                                    workspace.getSelectedMetadataFile(), userAuthBean.getUserPreferences());

                            // update id to the list
                            if (!oldId.equalsIgnoreCase(id)) {
                                userAuthBean.removeId(oldId);
                                userAuthBean.addId(id);
                            }

                            if (!oldTitle.equalsIgnoreCase(title)) {
                                userAuthBean.removeTitle(oldTitle);
                                userAuthBean.addTitle(title);
                            }

                            // refresh Metadata object
                            //workspace.getSelectedMetadataFile().setMetadata(null);
//                            workspace.getSelectedMetadataFile().setMetadata(delegator
//                                    .buildMetadata(workspace.getSelectedMetadataFile().getXmlDoc()));
                            workspace.getSelectedMetadataFile().setUpdated(true);

                            workspace.updateWarnMetadataFile();

                            //workspace.updateWarnMetadataFiles();
                            FacesMessageUtil.addInfoMessage("The metadata record has been saved");
                        }
                    }
                }
            } else {
                log.debug("No selected metadata.");
                FacesMessageUtil.addInfoMessage("No selected metadata record");
            }
        } catch (IOException | SAXException | XPathExpressionException ex) {
            FacesMessageUtil.addErrorMessage(CommonUtils.getErrorMessage(ex));
        }
    }

    public void applyThesaurusVersionChange() {
        try {
            delegator.applyThesaurusVersionChange(userAuthBean.getUserWorkspaceDir(),
                    workspace.getThesaurusChangeWarnFiles(), userAuthBean.getUserPreferences());

            workspace.setThesaurusChangeWarns(null);

            if (workspace.getThesaurusChangeWarnFiles() != null) {
                workspace.getThesaurusChangeWarnFiles().forEach((item) -> {
                    boolean existing = false;
                    if (workspace.getManualCorrectionWarnFiles() != null
                            && workspace.getManualCorrectionWarnFiles().contains(item)) {
                        existing = true;
                    }

                    if (!existing) {
                        if (workspace.getAutoCorrectionWarnFiles() != null
                                && workspace.getAutoCorrectionWarnFiles().contains(item)) {
                            existing = true;
                        }
                    }

                    if (!existing) {
                        workspace.decreaseNumOfWarnRecords();
                    }
                });
                workspace.setThesaurusChangeWarnFiles(null);
            }

            if ((workspace.getAutoCorrectionWarnFiles() == null || workspace.getAutoCorrectionWarnFiles().isEmpty())
                    && (workspace.getManualCorrectionWarnFiles() == null || workspace.getManualCorrectionWarnFiles().isEmpty())) {
                workspace.getView().toList();
            }
        } catch (IOException | DOMException | SAXException | XPathExpressionException ex) {
            FacesMessageUtil.addErrorMessage(CommonUtils.getErrorMessage(ex));
        }
    }

    public void applyThesaurusConceptChange() {
        try {
            //System.out.println("NQM: " + workspace.getAutoCorrectionWarnFiles());
            delegator.applyThesaurusConceptChange(userAuthBean.getUserWorkspaceDir(),
                    workspace.getAutoCorrectionWarnFiles(), userAuthBean.getUserPreferences());

            //System.out.println("NQM1: " + workspace.getAutoCorrectionWarnFiles());

            if (workspace.getThesaurusAutoCorrectionWarnReport() != null) {
                workspace.getThesaurusAutoCorrectionWarnReport().reset();
            }

            if (workspace.getAutoCorrectionWarnFiles() != null) {
                workspace.getAutoCorrectionWarnFiles().forEach((item) -> {
                    boolean existing = false;
                    if (workspace.getManualCorrectionWarnFiles() != null
                            && workspace.getManualCorrectionWarnFiles().contains(item)) {
                        existing = true;
                    }

                    if (!existing) {
                        if (workspace.getThesaurusChangeWarnFiles() != null
                                && workspace.getThesaurusChangeWarnFiles().contains(item)) {
                            existing = true;
                        }
                    }

                    if (!existing) {
                        workspace.decreaseNumOfWarnRecords();
                    }
                });
                workspace.setAutoCorrectionWarnFiles(null);
            }

            if ((workspace.getManualCorrectionWarnFiles() == null || workspace.getManualCorrectionWarnFiles().isEmpty())
                    && (workspace.getThesaurusChangeWarns() == null || workspace.getThesaurusChangeWarns().isEmpty())) {
                workspace.getView().toList();
            }
        } catch (IOException | SAXException | XPathExpressionException ex) {
            FacesMessageUtil.addErrorMessage(CommonUtils.getErrorMessage(ex));
        }
    }

    public void saveMetadataFormatChange() {
        userAuthBean.saveMetadataFormatChange();
        metadataParser.validate(workspace.getMetadataFiles(), userAuthBean.getUserPreferences());
        /*
            if XML format is unselected
         */
        if (workspace.getSelectedMetadataFile() != null
                && workspace.getSelectedMetadataFile().getMetadata() != null) {
            if (workspace.getSelectedMetadataFile().getMetadata().isSeries()) {
                if (workspace.getView().isXml()
                        && !userAuthBean.getUserPreferences().isSeriesIsoFormat()) {
                    /*
                        change to other view if the current view is XML view
                     */
                    if (userAuthBean.getUserPreferences().isSeriesJsonFormat()) {
                        workspace.getView().toGeoJson();
                    } else {
                        workspace.getView().toInternalModel();
                    }
                }

                if (workspace.getView().isGeojson()
                        && !userAuthBean.getUserPreferences().isSeriesJsonFormat()) {
                    /*
                        change to other view if the current view is JSON view
                     */
                    if (userAuthBean.getUserPreferences().isSeriesIsoFormat()) {
                        workspace.getView().toXml();
                    } else {
                        workspace.getView().toInternalModel();
                    }
                }

                if (workspace.getView().isDif10()
                        && !userAuthBean.getUserPreferences().isSeriesDif10Format()) {
                    /*
                        change to other view if the current view is DIF-10 view
                     */
                    if (userAuthBean.getUserPreferences().isSeriesIsoFormat()) {
                        workspace.getView().toXml();
                    } else {
                        workspace.getView().toGeoJson();
                    }
                }
            }

            if (workspace.getSelectedMetadataFile().getMetadata().isService()) {
                if (workspace.getView().isXml()
                        && !userAuthBean.getUserPreferences().isServiceIsoFormat()) {
                    /*
                        change to JSON view if the current view is XML view
                     */
                    workspace.getView().toGeoJson();
                }

                if (workspace.getView().isGeojson()
                        && !userAuthBean.getUserPreferences().isServiceJsonFormat()) {
                    /*
                        change to XML view if the current view is JSON view
                     */
                    workspace.getView().toXml();
                }
            }
        }

        FacesMessageUtil.addInfoMessage("Saved");
    }

    public void onChangeSearchType(final AjaxBehaviorEvent event) {
        String searchType = catalog.getSearchType();
        catalog = new CatalogueLayout();
        catalog.setSearchType(searchType);
    }

    private void saveMetadataRecordToFile(String id, MetadataFile mFile) throws IOException {
        String generatedFileName = CommonUtils.generateFileName(id);
        String filePath = userAuthBean.getUserWorkspaceDir() + "/" + generatedFileName;

        File file = new File(filePath);
        // create parent directories if nonexistent
        file.getParentFile().mkdirs();

        mFile.setFileName(generatedFileName);
        MetadataUtils.saveFile(mFile.getInternalModelSrc(), filePath);
    }

    ///////////////////////////////////////////////
    public CustomFunctions getCustomFunctions() {
        return customFunctions;
    }

    public EditorForm getEditor() {
        return editor;
    }

    public boolean hasCatalogue() {
        if (userAuthBean.getUserPreferences().getCatalogue() != null) {
            if (userAuthBean.getUserPreferences().getCatalogue().getSeriesInterface() != null
                    && userAuthBean.getUserPreferences().getCatalogue().getSeriesInterface().getSearchUrl() != null) {
                return true;
            }

            if (userAuthBean.getUserPreferences().getCatalogue().getServiceInterface() != null
                    && userAuthBean.getUserPreferences().getCatalogue().getServiceInterface().getSearchUrl() != null) {
                return true;
            }
        }
        return false;
    }

    public OpenSearchUrl getOpenSearchUrl() {
        if (catalog.isServiceSearch()) {
            return userAuthBean.getUserPreferences().getCatalogue().getServiceInterface().getSearchUrl();
        } else {
            return userAuthBean.getUserPreferences().getCatalogue().getSeriesInterface().getSearchUrl();
        }
    }

    public boolean showMergedMenuItem(String format) {
        if (workspace.getNumOfSeriesRecords() > 0) {
            switch (format) {
                case "xml":
                    return userAuthBean.getUserPreferences().isSeriesIsoFormat();
                case "geojson":
                    return userAuthBean.getUserPreferences().isSeriesJsonFormat();
                case "dif10":
                    return userAuthBean.getUserPreferences().isSeriesDif10Format();
            }
        }

        if (workspace.getNumOfServiceRecords() > 0) {
            switch (format) {
                case "xml":
                    return userAuthBean.getUserPreferences().isServiceIsoFormat();
                case "geojson":
                    return userAuthBean.getUserPreferences().isServiceJsonFormat();
            }
        }
        return false;
    }

    public boolean showMenuItem(String format) {
        //log.debug("Show menu item " + format);
        return showMenuItem(workspace.getSelectedMetadataFile(), format);
    }

    public boolean showMenuItem(MetadataFile mFile, String format) {
        if (mFile != null && mFile.getMetadata() != null) {
            //log.debug("Show menu item " + format + " of metadata record " + mFile.getFlatList().getId());
            if (mFile.getMetadata().isSeries()) {
                switch (format) {
                    case "xml":
                        return userAuthBean.getUserPreferences().isSeriesIsoFormat();
                    case "geojson":
                        return userAuthBean.getUserPreferences().isSeriesJsonFormat();
                    case "dif10":
                        return userAuthBean.getUserPreferences().isSeriesDif10Format();
                }
            }
            if (mFile.getMetadata().isService()) {
                switch (format) {
                    case "xml":
                        return userAuthBean.getUserPreferences().isServiceIsoFormat();
                    case "geojson":
                        return userAuthBean.getUserPreferences().isServiceJsonFormat();
                }
            }
        }
        return false;
    }

    public String getSelectedMenuItemCssClass(String format) {
        if (workspace.getSelectedMetadataFile() != null
                && workspace.getSelectedMetadataFile().getMetadata() != null) {
            if (workspace.getSelectedMetadataFile().getMetadata().isSeries()) {

                if ("internal".equals(format)
                        && workspace.getView().isInternalModel()) {
                    return "spb-selected-menuitem";
                }

                switch (format) {
                    case "xml":
                        if (workspace.getView().isXml()
                                && userAuthBean.getUserPreferences().isSeriesIsoFormat()) {
                            return "spb-selected-menuitem";
                        }
                        break;
                    case "geojson":
                        if (workspace.getView().isGeojson()
                                && userAuthBean.getUserPreferences().isSeriesJsonFormat()) {
                            return "spb-selected-menuitem";
                        }
                        break;
                    case "dif10":
                        if (workspace.getView().isDif10()
                                && userAuthBean.getUserPreferences().isSeriesDif10Format()) {
                            return "spb-selected-menuitem";
                        }
                        break;
                }
            }

            if (workspace.getSelectedMetadataFile().getMetadata().isService()) {
                switch (format) {
                    case "xml":
                        if (workspace.getView().isXml()
                                && userAuthBean.getUserPreferences().isServiceIsoFormat()) {
                            return "spb-selected-menuitem";
                        }
                        break;
                    case "geojson":
                        if (workspace.getView().isGeojson()
                                && userAuthBean.getUserPreferences().isServiceJsonFormat()) {
                            return "spb-selected-menuitem";
                        }
                        break;
                }
            }
        }
        return "";
    }

    public boolean hasKeyword(FreeKeyword kw) {
        return XmlUtils.hasKeyword(kw);
    }

    public boolean hasKeyword(ThesaurusKeyword kw) {
        return XmlUtils.hasKeyword(kw);
    }

    public CatalogueLayout getCatalog() {
        return catalog;
    }

    public void setCatalog(CatalogueLayout catalog) {
        this.catalog = catalog;
    }

    public WorkspaceLayout getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceLayout workspace) {
        this.workspace = workspace;
    }

    public UserAuthenticationBean getUserAuthBean() {
        return userAuthBean;
    }

    public void setUserAuthBean(UserAuthenticationBean userAuthBean) {
        this.userAuthBean = userAuthBean;
    }

    public boolean isAdvancedSearch() {
        return advancedSearch;
    }

    public void setAdvancedSearch(boolean advancedSearch) {
        this.advancedSearch = advancedSearch;
    }

    public String getCatalogueAuthenticationMsg() {
        return catalogueAuthenticationMsg;
    }

    public void setCatalogueAuthenticationMsg(String catalogueAuthenticationMsg) {
        this.catalogueAuthenticationMsg = catalogueAuthenticationMsg;
    }
}
