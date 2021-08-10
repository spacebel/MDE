/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of {@link org.w3c.dom.NodeList}
 * 
 * @author mng
 */
public class NodeListAdapter implements NodeList {

    private final List<Node> nodes;

    public NodeListAdapter(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Node item(int index) {
        return nodes.get(index);
    }

    @Override
    public int getLength() {
        return nodes.size();
    }
}
