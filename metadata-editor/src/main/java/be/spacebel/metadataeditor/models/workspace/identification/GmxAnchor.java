/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import be.spacebel.metadataeditor.models.catalogue.ParameterOption;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.w3c.dom.Node;

/**
 * This class represents gmx:Anchor element of ISO 19139-2 XML metadata
 *
 * @author mng
 */
public class GmxAnchor implements Serializable {

    private final String uuid;
    private String link;
    private String text;
    private String richText;

    private Node self;
    private ParameterOption option;

    public GmxAnchor() {
        uuid = UUID.randomUUID().toString();
        option = new ParameterOption();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
        option.setValue(link);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        option.setLabel(text);
    }

    public String getRichText() {
        return richText;
    }

    public void setRichText(String richText) {
        this.richText = richText;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public ParameterOption getOption() {
        return option;
    }

    public void setOption(ParameterOption option) {
        this.option = option;
    }

    /*
     public void update() {
     if (self != null) {
     XMLUtility.setTextContent(self, text);
     }
     }
     */
    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GmxAnchor)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getUuid().equals(((GmxAnchor) obj).getUuid());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getUuid());
        return hash;
    }

}
