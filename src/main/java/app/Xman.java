package app;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Xman {
    private String xml;

    private boolean automaticRetry = true;

    private boolean isNamespaceAware = false;

    private boolean isValidating = false;

    private String xpath;

    public static Xman newInstance() {
        return new Xman();
    }

    DocumentBuilderFactory getDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(isNamespaceAware);
        builderFactory.setValidating(isValidating);
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
                false);
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        return builderFactory;
    }

  
    DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder builder = getDocumentBuilderFactory().newDocumentBuilder();
        builder.setErrorHandler(null);
        return builder;
    }

    Document getDocument() throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilder builder = getDocumentBuilder();
        org.w3c.dom.Document document = builder.parse(asInputSource());
        document.getDocumentElement()
                .normalize();
        return document;
    }

    public Document asDocument() {
        try {
            return getDocument();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return null;
        }
    }

    public InputSource asInputSource() {
        return new InputSource(new StringReader(xml));

    }

    public String asPrettyString() throws IOException, SAXException, ParserConfigurationException {

        Document document = getDocument();
        StringWriter stringOut = new StringWriter();
        DOMImplementationLS domImpl = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImpl.createLSSerializer();
        LSOutput lsOut = domImpl.createLSOutput();
        lsOut.setEncoding("UTF-8");
        lsOut.setCharacterStream(stringOut);
        serializer.write(document, lsOut);
        return stringOut.toString();

    }

    NamespaceContext getNamespaceResolver()
            throws IOException, SAXException, ParserConfigurationException {
        return new UniversalNamespaceResolver(getDocument());
    }

    XPath getXpath() throws ParserConfigurationException, SAXException, IOException {

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(getNamespaceResolver());
        return xpath;
    }

    public Xman setXpathText(String xpath) {
        this.xpath = xpath;
        return this;
    }

    public String getXmlText() {
        return xml;
    }

    public Xman setXmlText(String xml) {
        this.xml = xml;
        return this;
    }

    public String evaluateToString() {
        boolean useNamespaces = isNamespaceAware;
        String string = (String) evaluateAs(XPathConstants.STRING, useNamespaces);
        if (string == null || string.isEmpty()) {
            string = (String) evaluateAs(XPathConstants.STRING, !useNamespaces);
        }
        return string;
    }

    public List<Node> evaluateToNodes() {
        boolean namespaceAware = isNamespaceAware;
        NodeList nodes = (NodeList) evaluateAs(XPathConstants.NODESET, namespaceAware);
        if (automaticRetry && nodes.getLength() == 0) {
            nodes = (NodeList) evaluateAs(XPathConstants.NODESET, !namespaceAware);
        }
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i));
        }
        return list;
    }

    private Object evaluateAs(QName xpathConstant, boolean useNamespaces) {
        boolean originalValue = isNamespaceAware;
        try {
            isNamespaceAware = useNamespaces;
            return getXpath().evaluate(xpath, asDocument(), xpathConstant);

        } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        } finally {
            isNamespaceAware = originalValue;
        }
    }



/*
Source: https://www.ibm.com/developerworks/library/x-nmspccontext/

<books:booklist
  xmlns:books="http://univNaSpResolver/booklist"
  xmlns="http://univNaSpResolver/book"
  xmlns:fiction="http://univNaSpResolver/fictionbook">
  <science:book xmlns:science="http://univNaSpResolver/sciencebook">
    <title>Learning XPath</title>
    <author>Michael Schmidt</author>
  </science:book>
  <fiction:book>
    <title>Faust I</title>
    <author>Johann Wolfgang von Goethe</author>
  </fiction:book>
  <fiction:book>
    <title>Faust II</title>
    <author>Johann Wolfgang von Goethe</author>
  </fiction:book>
</books:booklist>

This XML example has three namespaces declared in the root element and one declared
on an element deeper in the structure.

The element booklist has three children, all named book.
But the first child has the namespace science, while the following children
have the namespace fiction. This means that these elements are completely different to XPath.

A namespace is a part of the identifier for an element or attribute. You can have elements or
attributes with the same local name, but different namespaces. They are completely different.
See the example above (science:book and fiction:book). You need namespaces to resolve naming
conflicts if you combine XML files from different sources.

The namespace is defined by a URI (in this example, http://univNaSpResolver/booklist).
To avoid the use of this long string, you define a prefix that is associated with
this URI (in the example, books).
Please remember that the prefix is like a variable: its name does not matter.

An XPath expression uses prefixes (for example, books:booklist/science:book) and, you have to
provide the URI associated with each prefix. This is what the NamespaceContext comes in.
*/

}