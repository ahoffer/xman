package app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
This code adapted from:
    Source: https://www.ibm.com/developerworks/library/x-nmspccontext/
    Author: Holger Kraus
    `License: Unknown
*/

public class UniversalNamespaceResolver implements NamespaceContext {

    public static final String xmlns = XMLConstants.XMLNS_ATTRIBUTE;

    public static final String w3cUri = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

    public static final String PREFIX_DELIMITER = ":";

    private static final String UNKNOWN_NS = "UNKNOWN_NAME_SPACE";

    private Map<String, String> prefix2Uri = new HashMap<String, String>();

    private Map<String, String> uri2Prefix = new HashMap<String, String>();

    /**
     * This constructor parses the document and stores all namespaces it can
     * find. If toplevelOnly is true, only namespaces in the root are used.
     *
     * @param document source document
     */
    public UniversalNamespaceResolver(Document document) {
        examineNode(document.getDocumentElement());
        for (String key : prefix2Uri.keySet()) {
            System.out.println("prefix " + key + ": uri " + prefix2Uri.get(key));
        }
    }

    /**
     * A single node is read, the namespace attributes are extracted and stored.
     *
     * @param node to examine
     */
    private void examineNode(Node node) {
        NamedNodeMap attributes = node.getAttributes();

        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            storeAttribute((Attr) attribute);

        }
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                examineNode(child);
            }
        }
    }

    /**
     * This method looks at an attribute and stores it, if it is a namespace
     * attribute.
     *
     * @param attribute to examine
     */
    private void storeAttribute(Attr attribute) {
        // examine the attributes in namespace xmlns

        String name = attribute.getName();
        String value = attribute.getValue();
        if (name.equals(xmlns)) {
            putInCache(xmlns, value);
        } else if (name.startsWith(xmlns + PREFIX_DELIMITER)) {
            String prefix = name.split(":")[1];
            putInCache(prefix, value);
        }
    }

    private void putInCache(String prefix, String uri) {
        prefix2Uri.put(prefix, uri);
        uri2Prefix.put(uri, prefix);
    }

    /**
     * This method is called by XPath. It returns the default namespace, if the
     * prefix is null or "".
     *
     * @param prefix to search for
     * @return uri
     */
    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return prefix2Uri.get(UNKNOWN_NS);
        } else {
            return prefix2Uri.get(prefix);
        }
    }

    /**
     * This method is not needed in this context, but can be implemented in a
     * similar way.
     */
    public String getPrefix(String namespaceURI) {
        return uri2Prefix.get(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        // Not implemented
        return null;
    }

}