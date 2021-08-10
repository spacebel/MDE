/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.mission;

import be.spacebel.metadataeditor.models.workspace.identification.Keyword;
import java.io.Serializable;
import java.util.Objects;
import org.w3c.dom.Node;

/**
 * This class represents gmi:sponsor element of ISO 19139-2 XML metadata
 * 
 * @author mng
 */
public class Sponsor implements Serializable {

    private Keyword operator;
    private Node self;

    public Keyword getOperator() {
        return operator;
    }

    public void setOperator(Keyword operator) {
        this.operator = operator;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public void addOperator(String name) {
        operator = new Keyword();
        operator.setLabel(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Sponsor)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.getOperator().equals(((Sponsor) obj).getOperator());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(getOperator().getUuid());
        return hash;
    }

}
