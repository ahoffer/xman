package com.github.ahoffer;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Xman {

    private String xmlText;

    private boolean automaticRetry = true;

    private boolean isNamespaceAware = false;

    private boolean isValidating = false;

    private String resultSeparator = " <!----------Next Result---------->";

    private String currentQuery;

    private List<Node> results = new ArrayList<>();

    public static Xman newInstance() {
        return new Xman();
    }

    public static Xman from(String xml) {
        Xman xman = Xman.newInstance();
        xman.xmlText = xml;
        return xman;
    }

    public static Xman from(InputStream xmlStream) throws IOException {
        return Xman.from(IOUtils.toString(xmlStream, UTF_8));
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

    public InputSource asInputSource() {
        return new InputSource(new ByteArrayInputStream(xmlText.getBytes(UTF_8)));

    }

    public Optional<String> asPrettyStringInput() {
        Document document = null;
        try {
            document = getDocument();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            //Throw away the exception
            return Optional.empty();
        }
        return Optional.of(getPrettyString(document));
    }

    public String asPrettyStringResults() {
        return asResults().map(list -> list.stream()
                .map(Node::getOwnerDocument)
                .map(this::getPrettyString)
                .collect(Collectors.joining(resultSeparator)))
                .get();
    }

    protected String getPrettyString(Document document) {
        String output = "";
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            output = stringWriter.toString();
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    NamespaceContext getNamespaceResolver()
            throws IOException, SAXException, ParserConfigurationException {
        return new UniversalNamespaceResolver(getDocument());
    }

    XPath getXpathQuueryString() throws ParserConfigurationException, SAXException, IOException {

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(getNamespaceResolver());
        return xpath;
    }

    public Xman evaluate(String queryString) {
        currentQuery = queryString;
        boolean namespaceAware = isNamespaceAware;
        NodeList nodes = (NodeList) evaluate();
        if (automaticRetry && nodes.getLength() == 0) {
            toggleNamespaceAwareness();
            nodes = (NodeList) evaluate();
        }
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i));
        }
        setResults(list);
        return this;
    }

    protected void toggleNamespaceAwareness() {
        isNamespaceAware = !isNamespaceAware;
    }

    private Object evaluate() {
        boolean originalValue = isNamespaceAware;
        try {
            return getXpathQuueryString().evaluate(currentQuery,
                    getDocument(),
                    XPathConstants.NODESET);

        } catch (XPathExpressionException | ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        } finally {
            isNamespaceAware = originalValue;
        }
    }

    public Optional<Node> asFirstResult() {
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<List<Node>> asResults() {
        return results.isEmpty() ? Optional.empty() : Optional.of(results);
    }

    protected void setResults(List<Node> results) {
        this.results = results;
    }

    public Xman mutateFirstResult(Consumer<Node> consumer) {
        asFirstResult().ifPresent(n -> consumer.accept(n));
        return this;
    }

    public Xman mutateResults(Consumer<List<Node>> consumer) {
        asResults().ifPresent(ns -> consumer.accept(ns));
        return this;
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