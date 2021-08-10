/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.utils.CommonUtils;
import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import be.spacebel.metadataeditor.utils.parser.MetadataUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.w3c.dom.Node;

/**
 * This class represents elements: gmd:fileIdentifier, gmd:language,
 * gmd:dateStamp, gmd:metadataStandardName and gmd:metadataStandardVersion of
 * ISO 19139-2 XML metadata
 *
 * @author mng
 */
public class Others implements Serializable {

    private String fileIdentifier;
    private String backupId;
    private Node fileIdentifierNode;
    private String language;
    private Node languageNode;
    private String standardName;
    private Node standardNameNode;
    private String standardVersion;
    private Node standardVersionNode;
    private List<Contact> contacts;
    //private Date lastUpdateDate;
    private String lastUpdateDate;
    private Node lastUpdateDateNode;

    public Others() {
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public Node getFileIdentifierNode() {
        return fileIdentifierNode;
    }

    public void setFileIdentifierNode(Node fileIdentifierNode) {
        this.fileIdentifierNode = fileIdentifierNode;
    }

    public String getBackupId() {
        return backupId;
    }

    public void setBackupId(String backupId) {
        this.backupId = backupId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Node getLanguageNode() {
        return languageNode;
    }

    public void setLanguageNode(Node languageNode) {
        this.languageNode = languageNode;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public Node getStandardNameNode() {
        return standardNameNode;
    }

    public void setStandardNameNode(Node standardNameNode) {
        this.standardNameNode = standardNameNode;
    }

    public String getStandardVersion() {
        return standardVersion;
    }

    public void setStandardVersion(String standardVersion) {
        this.standardVersion = standardVersion;
    }

    public Node getStandardVersionNode() {
        return standardVersionNode;
    }

    public void setStandardVersionNode(Node standardVersionNode) {
        this.standardVersionNode = standardVersionNode;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

//    public Date getLastUpdateDate() {
//        return lastUpdateDate;
//    }
//    
//    public void setLastUpdateDate(Date lastUpdateDate) {
//        this.lastUpdateDate = lastUpdateDate;
//    }
    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Node getLastUpdateDateNode() {
        return lastUpdateDateNode;
    }

    public void setLastUpdateDateNode(Node lastUpdateDateNode) {
        this.lastUpdateDateNode = lastUpdateDateNode;
    }

    public void update() {
        if (contacts != null) {
            contacts.forEach((contact) -> {
                contact.update();
            });
        }
        if (lastUpdateDateNode != null) {
            this.lastUpdateDate = CommonUtils.dateTimeToStr(new Date());
            MetadataUtils.updateLastUpdateDate(lastUpdateDateNode, lastUpdateDate);
        }

        if (fileIdentifierNode != null) {
            XmlUtils.setTextContent(fileIdentifierNode, fileIdentifier);
        }
    }

}
