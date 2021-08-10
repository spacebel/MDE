/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import java.io.Serializable;
import org.w3c.dom.Node;

/**
 * A representation of srv:couplingType element of service metadata
 *
 * @author mng
 */
public class CouplingType implements Serializable {

    private Node self;
    private String codeList;
    private String codeListValue;
    private String codeSpace;
    private String value;

    public CouplingType() {
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public String getCodeList() {
        return codeList;
    }

    public void setCodeList(String codeList) {
        this.codeList = codeList;
    }

    public String getCodeListValue() {
        return codeListValue;
    }

    public void setCodeListValue(String codeListValue) {
        this.codeListValue = codeListValue;
    }

    public String getCodeSpace() {
        return codeSpace;
    }

    public void setCodeSpace(String codeSpace) {
        this.codeSpace = codeSpace;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
