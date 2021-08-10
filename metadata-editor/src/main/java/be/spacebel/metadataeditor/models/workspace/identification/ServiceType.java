/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import java.io.Serializable;
import org.w3c.dom.Node;

/**
 * A representation of srv:serviceType element of service metadata
 *
 * @author mng
 */
public class ServiceType implements Serializable {

    private Node self;
    private String type;
    private String code;
    private String value;

    public ServiceType() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }    
}
