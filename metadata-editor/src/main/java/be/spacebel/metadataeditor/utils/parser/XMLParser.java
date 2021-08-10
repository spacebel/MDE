package be.spacebel.metadataeditor.utils.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSException;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class represents an XML parser kit
 *
 * @author mng
 */
public class XMLParser implements Serializable {

    private static final Logger log = Logger.getLogger(XMLParser.class);

    private ThreadLocal<DocumentBuilder> documentBuilder = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            DocumentBuilderFactory builderFactory;
            builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);

            try {
                return builderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final ThreadLocal<TransformerFactory> transformerFactory = new ThreadLocal<TransformerFactory>() {
        @Override
        protected TransformerFactory initialValue() {
            return TransformerFactory.newInstance();
        }
    };

    public Document createDOM() {
        return documentBuilder.get().newDocument();
    }

    /**
     * Indicates whether or not the factory is configured to produce parsers
     * which are namespace aware.
     */
    private boolean isNamespaceAware = true;

    /**
     * Indicates whether or not the factory is configured to produce parsers
     * which validate the XML content during parse.
     */
    private boolean isValidating = false;

    /**
     * Set the isNamespaceAware to new value.
     *
     * @param newIsNamespaceAware - new value of isNamespaceAware
     */
    public void setIsNamespaceAware(boolean newIsNamespaceAware) {
        isNamespaceAware = newIsNamespaceAware;
    }

    /**
     * Set the isValidating to new value.
     *
     * @param newIsValidating - new value of isValidating
     */
    public void setIsValidating(boolean newIsValidating) {
        isValidating = newIsValidating;
    }

    /**
     * Obtain a new instance of a DOM Document object to build a DOM tree with.
     *
     * @param newIsValidating - new isValidating flag
     * @param newIsNamespaceAware - new IsNamespaceAware flag
     * @return an instance of DOM document
     */
    public Document createDOM(boolean newIsValidating, boolean newIsNamespaceAware) {
        log.debug("createDOM invoked");

        Document doc = null;
        try {
            // Get the current factory properties
            boolean currentIsValidating = isValidating;
            boolean currentIsNamespaceAware = isNamespaceAware;

            // Set the new factory properties
            setIsValidating(newIsValidating);
            setIsNamespaceAware(newIsNamespaceAware);

            // Create a new document instance
            doc = getDocumentBuilder().newDocument();

            // Reset the new factory properties
            setIsValidating(currentIsValidating);
            setIsNamespaceAware(currentIsNamespaceAware);

            // Return the new document
            return doc;
        } catch (ParserConfigurationException ex) {
            String errorMsg = "Error happens when creating a new XML file: " + ex.getMessage();
            log.error(errorMsg);
            return null;
        }
    }

    /**
     * Parse input XML file to an XML document.
     *
     * @param xmlStream - an XML-based text
     * @return an XML document
     */
    public Document stream2Document(String xmlStream) {
        log.debug("stream2Document invoked");
        Document doc = null;
        try {
            DocumentBuilder builder = documentBuilder.get();
            if ((xmlStream != null) && (xmlStream.length() > 0)) {
                StringReader stringReader = new StringReader(xmlStream);
                doc = builder.parse(new InputSource(stringReader));
            } else { // null or empty string
                doc = builder.newDocument();
            }
        } catch (IOException | SAXException e) {
            log.debug("stream2Document(xmlStream = " + xmlStream
                    + "): exception occurred while parsing:" + e.getMessage());
        }
        return doc;
    }

    /**
     * parse a file into an xml document.
     *
     * @param filename filename of the file storing xml document
     * @return an XML document
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public Document fileToDom(String filename) throws IOException, SAXException {
        log.debug("load XML file to java DOM");
        Document doc = null;

        File f = new File(filename);
        if (f.exists()) {
            doc = documentBuilder.get().parse(f);
        } else {
            throw new IOException("File " + filename + " doesn't exist");
        }

        return doc;
    }

    /**
     * Serialize an XML document to an XML-based string.
     *
     * @param xmlDoc - XML document used as source of serialization
     * @return an XML string stream result and return the output
     */
    public String serializeDOM(Document xmlDoc) {
        log.debug("Serialize DOM");

        try {
            DOMImplementationLS domImplementation = (DOMImplementationLS) xmlDoc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(xmlDoc);
        } catch (DOMException | LSException ex) {
            String errorMsg = "Error happens when serializing the XML document.";
            log.error(errorMsg + ":" + ex.getMessage());
            return null;
        }
    }

    /**
     * Serialize an XML document to an XML-based string without xml declaration
     *
     * @param xmlDoc
     * @return
     */
    public String toStr(Document xmlDoc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");            
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException | IllegalArgumentException | TransformerFactoryConfigurationError e) {
            return null;
        }
    }

    public String removeNewLinesAndXmlDec(String xmlSrc) {
        Document xmlDoc = stream2Document(xmlSrc);
        return toStr(xmlDoc);
    }

    public String format(String unformattedXml) {
        log.debug("Format XML string");
        if (unformattedXml != null && !unformattedXml.isEmpty()) {

            Document xmlDoc = stream2Document(unformattedXml);
            return format(xmlDoc, 4);
            /*
             try {
             Document xmlDoc = stream2Document(unformattedXml);

             DOMImplementationLS domImplementation = (DOMImplementationLS) xmlDoc.getImplementation();
             LSSerializer lsSerializer = domImplementation.createLSSerializer();
             lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
             lsSerializer.getDomConfig().setParameter("xml-declaration", Boolean.TRUE);

             LSOutput lsOutput = domImplementation.createLSOutput();
             lsOutput.setEncoding("UTF-8");
             Writer stringWriter = new StringWriter();
             lsOutput.setCharacterStream(stringWriter);
             lsSerializer.write(xmlDoc, lsOutput);
             return stringWriter.toString();

             //return lsSerializer.writeToString(xmlDoc);
             } catch (DOMException ex) {
             String errorMsg = "Error happens when serializing the XML document.";
             log.error(errorMsg + ":" + ex.getMessage());

             } catch (LSException ex) {
             String errorMsg = "Error happens when serializing the XML document.";
             log.error(errorMsg + ":" + ex.getMessage());
             }
             */
        }
        return null;

    }

    public String format(Document xmlDoc) {
        log.debug("Format XML DOM");

        return format(xmlDoc, 4);
        /*
         try {
         DOMImplementationLS domImplementation = (DOMImplementationLS) xmlDoc.getImplementation();
         LSSerializer lsSerializer = domImplementation.createLSSerializer();
         lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
         lsSerializer.getDomConfig().setParameter("xml-declaration", Boolean.TRUE);

         LSOutput lsOutput = domImplementation.createLSOutput();
         lsOutput.setEncoding("UTF-8");
         Writer stringWriter = new StringWriter();
         lsOutput.setCharacterStream(stringWriter);
         lsSerializer.write(xmlDoc, lsOutput);
         return stringWriter.toString();

         //return lsSerializer.writeToString(xmlDoc);
         } catch (DOMException ex) {
         String errorMsg = "Error happens when serializing the XML document.";
         log.error(errorMsg + ":" + ex.getMessage());

         } catch (LSException ex) {
         String errorMsg = "Error happens when serializing the XML document.";
         log.error(errorMsg + ":" + ex.getMessage());
         }

         return null;
         */

    }

    public String format(Document xmlDoc, int indent) {
        log.debug("Format XML document with inden = " + indent);
        try {
            /* 
             Remove whitespaces outside tags
             */

            xmlDoc.normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", xmlDoc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            Transformer transformer = transformerFactory.get().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));

            // Return pretty print xml string
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(xmlDoc), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (XPathExpressionException | DOMException | IllegalArgumentException | TransformerException e) {
            String errorMsg = "Error happens when formatting the XML document.";
            log.error(errorMsg + ":" + e.getMessage());
            return null;
        }
    }

    public void domToFile(Document xmlDoc, String filePath) throws DOMException, LSException, IOException {
        log.debug("Saving DOM to XML file " + filePath);
        try {
            DOMImplementationLS domImplementation = (DOMImplementationLS) xmlDoc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            lsSerializer.getDomConfig().setParameter("xml-declaration", Boolean.TRUE);

            LSOutput lsOutput = domImplementation.createLSOutput();
            lsOutput.setEncoding("UTF-8");

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));

            //FileWriter fileWriter = new FileWriter(filePath);
            lsOutput.setCharacterStream(out);
            lsSerializer.write(xmlDoc, lsOutput);

            log.debug("Saved DOM to XML file " + filePath);
        } catch (DOMException | LSException | IOException ex) {
            String errorMsg = "Error happens when serializing the XML document.";
            log.error(errorMsg + ":" + ex.getMessage());
            throw ex;
        }
    }

    /*
     public String getPrettyPrint(String unformattedXml) {
     Document doc = stream2Document(unformattedXml);
     DOMImplementation domImplementation = doc.getImplementation();
     if (domImplementation.hasFeature("LS", "3.0") && domImplementation.hasFeature("Core", "2.0")) {
     DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
     LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
     DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
     if (domConfiguration.canSetParameter("format-pretty-print", Boolean.TRUE)) {
     lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
     LSOutput lsOutput = domImplementationLS.createLSOutput();
     lsOutput.setEncoding(StandardCharsets.UTF_8.name());
     StringWriter stringWriter = new StringWriter();
     lsOutput.setCharacterStream(stringWriter);
     lsSerializer.write(doc, lsOutput);
     return stringWriter.toString();
     } else {
     throw new UnsupportedOperationException("DOMConfiguration 'format-pretty-print' parameter isn't settable.");
     }
     } else {
     throw new UnsupportedOperationException("DOM 3.0 LS and/or DOM 2.0 Core not supported.");
     }
     }
     */
    /**
     * Creates a new instance of a DocumentBuilderFactory using the currently
     * configured parameters.
     *
     * @return a factory of document builder
     * @exception RemoteException
     */
    private DocumentBuilderFactory getDocumentBuilderFactory() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(isNamespaceAware);
            factory.setValidating(isValidating);
            return factory;
        } catch (Exception ex) {
            log.error("XMLParser.getDocumentBuilderFactory().error:" + ex.getMessage());
            return null;
        }
    }

    /**
     * Creates a new instance of DocumentBuilder using the currently configured
     * parameters.
     *
     * @return a document builder
     * @exception RemoteException Description of Exception.
     * @exception ParserConfigurationException Description of Exception.
     */
    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return getDocumentBuilderFactory().newDocumentBuilder();
    }
}
