/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.distribution;

import java.io.Serializable;
import java.util.List;
import org.w3c.dom.Node;

/**
 * A representation of gmd:transferOptions element of ISO 19139-2 metadata
 *
 * @author mng
 */
public class TransferOption implements Serializable {

    private String units;
    private Node unitsNode;
    private String size;
    private Node sizeNode;
    private List<OnlineResource> onlineRses;    
    private Node self;

    public TransferOption() {
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<OnlineResource> getOnlineRses() {
        return onlineRses;
    }

    public void setOnlineRses(List<OnlineResource> onlineRses) {
        this.onlineRses = onlineRses;
    }   

    public Node getUnitsNode() {
        return unitsNode;
    }

    public void setUnitsNode(Node unitsNode) {
        this.unitsNode = unitsNode;
    }

    public Node getSizeNode() {
        return sizeNode;
    }

    public void setSizeNode(Node sizeNode) {
        this.sizeNode = sizeNode;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

}
