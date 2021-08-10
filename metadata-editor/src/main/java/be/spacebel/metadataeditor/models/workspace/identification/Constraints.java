/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import java.io.Serializable;
import java.util.List;
import org.w3c.dom.Node;

/**
 * This class represents gmd:resourceConstraints element of ISO 19139-2 XML metadata
 * 
 * @author mng
 */
public class Constraints implements Serializable {

    //private GmxAnchor requiredUseLimitations;
    private List<GmxAnchor> useLimitations;
    private List<GmxAnchor> accesses;
    private List<GmxAnchor> uses;
    private List<GmxAnchor> others;
    private Node self;

    public Constraints() {
    }

//    public GmxAnchor getRequiredUseLimitations() {
//        return requiredUseLimitations;
//    }
//
//    public void setRequiredUseLimitations(GmxAnchor requiredUseLimitations) {
//        this.requiredUseLimitations = requiredUseLimitations;
//    }

    public List<GmxAnchor> getUseLimitations() {
        return useLimitations;
    }

    public void setUseLimitations(List<GmxAnchor> useLimitations) {
        this.useLimitations = useLimitations;
    }

    public List<GmxAnchor> getAccesses() {
        return accesses;
    }

    public void setAccesses(List<GmxAnchor> accesses) {
        this.accesses = accesses;
    }

    public List<GmxAnchor> getUses() {
        return uses;
    }

    public void setUses(List<GmxAnchor> uses) {
        this.uses = uses;
    }

    public List<GmxAnchor> getOthers() {
        return others;
    }

    public void setOthers(List<GmxAnchor> others) {
        this.others = others;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    /* 
     public void update() {
     if(useLimitations != null){
     for(GmxAnchor sp: useLimitations){
     sp.update();
     }
     }
         
     if(accesses != null){
     for(GmxAnchor sp: accesses){
     sp.update();
     }
     }
         
     if(uses != null){
     for(GmxAnchor sp: uses){
     sp.update();
     }
     }
         
     if(others != null){
     for(GmxAnchor sp: others){
     sp.update();
     }
     }
     }
     */
}
