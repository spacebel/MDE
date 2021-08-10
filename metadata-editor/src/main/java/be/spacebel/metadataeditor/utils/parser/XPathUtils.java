/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.spacebel.metadataeditor.utils.parser;

import be.spacebel.metadataeditor.business.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implements XPath utilities
 *
 * @author mng
 */
public class XPathUtils {

    private final static Logger log = Logger.getLogger(XPathUtils.class);

    private static final Map<String, String> namespaces;

    static {
        namespaces = new ConcurrentHashMap<>();
        namespaces.put(Constants.GMI_PREFIX, Constants.GMI_NS);
        namespaces.put(Constants.GCO_PREFIX, Constants.GCO_NS);
        namespaces.put(Constants.GMD_PREFIX, Constants.GMD_NS);
        namespaces.put(Constants.GML_PREFIX, Constants.GML_NS);
        namespaces.put(Constants.GMX_PREFIX, Constants.GMX_NS);
        namespaces.put(Constants.XLINK_PREFIX, Constants.XLINK_NS);

        namespaces.put(Constants.DCT_PREFIX, Constants.DCT_NS);
        namespaces.put(Constants.OWL_PREFIX, Constants.OWL_NS);
        namespaces.put(Constants.RDF_PREFIX, Constants.RDF_NS);
        namespaces.put(Constants.RDFS_PREFIX, Constants.RDFS_NS);
        namespaces.put(Constants.SKOS_PREFIX, Constants.SKOS_NS);
        namespaces.put(Constants.SOSA_PREFIX, Constants.SOSA_NS);
        namespaces.put(Constants.VOID_PREFIX, Constants.VOID_NS);

        namespaces.put(Constants.GCMD_OLD_PREFIX, Constants.GCMD_OLD_NS);
        namespaces.put(Constants.GCMD_PREFIX, Constants.GCMD_NS);
        namespaces.put(Constants.OS_PREFIX, Constants.OS_NAMESPACE);

        namespaces.put(Constants.OWS_PREFIX, Constants.OWS_NS);
        namespaces.put(Constants.ATOM_PREFIX, Constants.ATOM_NS);

        namespaces.put("wcs", "http://www.opengis.net/wcs/2.0");
        namespaces.put("wfs", "http://www.opengis.net/wfs/2.0");
        namespaces.put("wms", "http://www.opengis.net/wms");
        namespaces.put("wmts", "http://www.opengis.net/wmts/1.0");
        namespaces.put("ows1", "http://www.opengis.net/ows/1.1");

        namespaces.put(Constants.SERVICE_PREFIX, Constants.SERVICE_NS);

    }

    public static String getNodeValue(Node node, String strXPATH) {
        try {
            Node result = (Node) getDomXPath(strXPATH).selectSingleNode(node);
            if (result == null) {
                return null;
            }
            String value = result.getTextContent();
            if (StringUtils.isNotEmpty(value)) {
                return StringUtils.trimToEmpty(value);
            }
            return value;
        } catch (JaxenException | DOMException e) {
            log.warn("Error evaluating XPath expression", e);
            return null;
        }
    }

    public static List<String> getNodesValues(Node node, String strXPATH) {
        try {
            List<Node> nodes = getDomXPath(strXPATH).selectNodes(node);
            return nodes.stream().map(Node::getTextContent).collect(Collectors.toList());
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);
            return new ArrayList<>();
        }
    }

    public static Node getNode(Node node, String strXPATH) {
        try {
            return (Node) getDomXPath(strXPATH).selectSingleNode(node);
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);
            return null;
        }
    }

    public static NodeList getNodes(Node node, String strXPATH) {
        try {
            List<Node> nodes = getDomXPath(strXPATH).selectNodes(node);
            return new NodeListAdapter(nodes);
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);
            return null;
        }
    }

    public static String getAttributeValue(Node node, String strXPATH, String attribute) {
        try {
            Element result = (Element) getDomXPath(strXPATH).selectSingleNode(node);
            if (result != null) {
                return result.getAttribute(attribute);
            } else {
                return null;
            }
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);
            return null;
        }
    }

    public static String getAttributeValue(Node node, String strXPATH, String attrLocalName, String attrNs) {
        try {
            Element result = (Element) getDomXPath(strXPATH).selectSingleNode(node);
            if (result != null) {
                return result.getAttributeNS(attrNs, attrLocalName);
            } else {
                return null;
            }
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);
            return null;
        }
    }

    public static List<String> getAttributeValues(Node node, String strXPATH, String attrLocalName, String attrNs) {
        try {
            List<String> values = new ArrayList<>();

            List<Node> nodes = getDomXPath(strXPATH).selectNodes(node);
            if (nodes != null && !nodes.isEmpty()) {
                for (Node n : nodes) {
                    String value = ((Element) n).getAttributeNS(attrNs, attrLocalName);
                    if (StringUtils.isNotEmpty(value)) {
                        values.add(value);
                    }
                }
            }
            return values;
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);
            return null;
        }
    }

    public static void removeNodes(Node rootNode, String strXPATH) {
        NodeList nodeList = getNodes(rootNode, strXPATH);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String nodeName = node.getNodeName();
                node.getParentNode().removeChild(node);
                log.debug("Removed node " + nodeName);
            }
        }
    }

    public static void updateNodeValue(Node rootNode, String strXPATH, String value) {
        Node node = getNode(rootNode, strXPATH);
        if (node != null) {
            log.debug("Node is not null");
            if (value != null) {
                value = StringEscapeUtils.escapeXml10(value);
            }
            node.setTextContent(value);
        } else {
            log.debug("Node is null");
        }
    }

    public static void updateAttributeValue(Node rootNode, String strXPATH, String attrLocalName, String attrNs, String attrValue) {
        try {
            Element result = (Element) getDomXPath(strXPATH).selectSingleNode(rootNode);
            if (result != null) {
                Attr attr = result.getAttributeNodeNS(attrNs, attrLocalName);
                if (attr != null) {
                    if (attrValue != null) {
                        attrValue = StringEscapeUtils.escapeXml10(attrValue);
                    }
                    attr.setNodeValue(attrValue);
                }
            }
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);
        }
    }

    public static void updateAttributeValue(Node node, String strXPATH, String attrName, String attrValue) {
        try {
            Element result = (Element) getDomXPath(strXPATH).selectSingleNode(node);
            if (result != null) {
                Attr attr = result.getAttributeNode(attrName);
                if (attr != null) {
                    if (attrValue != null) {
                        attrValue = StringEscapeUtils.escapeXml10(attrValue);
                    }
                    attr.setNodeValue(attrValue);
                }
            }
        } catch (JaxenException e) {
            log.warn("Error evaluating XPath expression", e);

        }
    }

    private static DOMXPath getDomXPath(String strXPATH) throws JaxenException {
        DOMXPath xpath = new DOMXPath(strXPATH);
        xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));
        return xpath;
    }

    public static Map<String, String> getNamespaces() {
        return new HashMap<>(namespaces);
    }

    public static String getPrefixByNamespace(String ns) {
        for (Entry<String, String> entry : namespaces.entrySet()) {
            if (entry.getValue().equals(ns)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
