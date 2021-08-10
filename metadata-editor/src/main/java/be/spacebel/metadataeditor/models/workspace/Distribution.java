/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace;

import be.spacebel.metadataeditor.models.workspace.distribution.TransferOption;
import java.io.Serializable;
import java.util.List;
import org.w3c.dom.Node;

/**
 * A representation of gmd:distributionInfo element of the Internal Metadata Model
 * 
 * @author mng
 */
public class Distribution implements Serializable {

    private List<TransferOption> transferOptions;
    private Node self;

    public Distribution() {
    }

    public List<TransferOption> getTransferOptions() {
        return transferOptions;
    }

    public void setTransferOptions(List<TransferOption> transferOptions) {
        this.transferOptions = transferOptions;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

}
