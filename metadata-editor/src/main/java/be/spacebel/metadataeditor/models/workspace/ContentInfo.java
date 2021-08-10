/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.models.workspace.identification.Keyword;
import java.io.Serializable;
import org.w3c.dom.Node;
import java.util.Objects;
import java.util.UUID;

/**
 * A representation of gmd:contentInfo element of the Internal Metadata Model
 *
 * @author mng
 */
public class ContentInfo implements Serializable {

    private Keyword processingLevel;
    private Node self;
    private final String uuid;
    private boolean removed;

    public ContentInfo() {
        uuid = UUID.randomUUID().toString();
    }

    public Keyword getProcessingLevel() {
        return processingLevel;
    }

    public void setProcessingLevel(Keyword processingLevel) {
        this.processingLevel = processingLevel;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public void addProcessingLevel(String level) {
        processingLevel = new Keyword();
        processingLevel.setLabel(level);
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ContentInfo)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUuid().equals(((ContentInfo) obj).getUuid());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUuid());
        return hash;
    }
}
