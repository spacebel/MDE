/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.models.workspace.identification;

import be.spacebel.metadataeditor.utils.parser.XmlUtils;
import java.io.IOException;
import org.w3c.dom.Node;

/**
 * This class represents gmd:geographicElement element of ISO 19139-2 XML
 * metadata
 *
 * @author mng
 */
public class Bbox {

    private Double west;
    private Node nWest;
    private Double east;
    private Node nEast;
    private Double south;
    private Node nSouth;
    private Double north;
    private Node nNorth;
    private Node self;

    public Bbox() {
    }

    public Double getWest() {
        return west;
    }

    public void setWest(Double west) {
        this.west = west;
    }

    public Double getEast() {
        return east;
    }

    public void setEast(Double east) {
        this.east = east;
    }

    public Double getSouth() {
        return south;
    }

    public void setSouth(Double south) {
        this.south = south;
    }

    public Double getNorth() {
        return north;
    }

    public void setNorth(Double north) {
        this.north = north;
    }

    public Node getnWest() {
        return nWest;
    }

    public void setnWest(Node nWest) {
        this.nWest = nWest;
    }

    public Node getnEast() {
        return nEast;
    }

    public void setnEast(Node nEast) {
        this.nEast = nEast;
    }

    public Node getnSouth() {
        return nSouth;
    }

    public void setnSouth(Node nSouth) {
        this.nSouth = nSouth;
    }

    public Node getnNorth() {
        return nNorth;
    }

    public void setnNorth(Node nNorth) {
        this.nNorth = nNorth;
    }

    public Node getSelf() {
        return self;
    }

    public void setSelf(Node self) {
        this.self = self;
    }

    public void validate() throws IOException {
        StringBuilder errors = new StringBuilder();
        if (west < -180 || west > 180) {
            errors.append("The west value should be in rang (-180, 180).");
        }
        if (south < -90 || south > 90) {
            errors.append(" The south value should be in rang (-90, 90).");
        }
        if (east < -180 || east > 180) {
            errors.append(" The east value should be in rang (-180, 180).");
        }
        if (north < -90 || north > 90) {
            errors.append(" The north value should be in rang (-90, 90).");
        }

        if (errors.length() > 0) {
            throw new IOException(errors.toString());
        }
    }

    public void update() throws IOException {
        validate();
        if (nWest != null) {
            XmlUtils.setTextContent(nWest, Double.toString(west));
        }
        if (nEast != null) {
            XmlUtils.setTextContent(nEast, Double.toString(east));
        }
        if (nSouth != null) {
            XmlUtils.setTextContent(nSouth, Double.toString(south));
        }
        if (nNorth != null) {
            XmlUtils.setTextContent(nNorth, Double.toString(north));
        }
    }

}
