package org.oagi.score.gateway.http.common.helper;

import com.sun.xml.xsom.*;
import net.sf.saxon.lib.NamespaceConstant;
import org.oagi.score.gateway.http.common.util.OAGiNamespaceContext;
import org.oagi.score.gateway.http.common.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Locator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Context {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private static Map<String, Document> documentMap = new HashMap();
    public static XPath xPath;

    static {
        System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
        XPathFactory xPathFactory = null;
        try {
            xPathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        } catch (XPathFactoryConfigurationException e) {
            throw new IllegalStateException(e);
        }
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new OAGiNamespaceContext());
    }

    public static Document loadDocument(File file) {
        return loadDocument(file.toURI());
    }

    public static Document loadDocument(URI uri) {
        return loadDocument(uri.toString());
    }

    public static Document loadDocument(String uri) {
        String module = Utility.extractModuleName(uri);

        Document xmlDocument;
        if (!documentMap.containsKey(module)) {
            try {
                DocumentBuilder builder = documentBuilder();
                try (InputStream inputStream = new URI(uri).toURL().openStream()) {
                    xmlDocument = builder.parse(inputStream);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            documentMap.put(module, xmlDocument);
        } else {
            xmlDocument = documentMap.get(module);
        }

        return xmlDocument;
    }

    public static DocumentBuilder documentBuilder() {
        return documentBuilder(true);
    }

    public static DocumentBuilder documentBuilder(boolean namespaceAware) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(namespaceAware);
        try {
            return builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private XSSchemaSet xsSchemaSet;
    private File file;

    private Document loadDocument(Locator locator) {
        if (locator == null) {
            return null;
        }
        String systemId = locator.getSystemId();
        return Context.loadDocument(systemId);
    }

    public String evaluate(String expression, Object item) {
        try {
            return xPath.evaluate(expression, item);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Element evaluateElement(String expression, Node item) {
        try {
            return (Element) xPath.evaluate(expression, item, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Element evaluateElement(String expression, XSComponent xsComponent) {
        Document document = loadDocument(xsComponent.getLocator());
        return evaluateElement(expression, document);
    }

    public NodeList evaluateNodeList(String expression, Node item) {
        try {
            return (NodeList) xPath.evaluate(expression, item, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public XSComplexType getXSComplexType(String ns, String localName) {
        return xsSchemaSet.getComplexType(ns, localName);
    }

    public XSElementDecl getXSElementDecl(String ns, String localName) {
        return xsSchemaSet.getElementDecl(ns, localName);
    }

    public XSModelGroupDecl getXSModelGroupDecl(String ns, String localName) {
        return xsSchemaSet.getModelGroupDecl(ns, localName);
    }

    public Iterable<XSElementDecl> iterateXSElementDecls() {
        final Iterator<XSElementDecl> xsElementDeclIterator = xsSchemaSet.iterateElementDecls();
        return new Iterable<XSElementDecl>() {
            @Override
            public Iterator<XSElementDecl> iterator() {
                return xsElementDeclIterator;
            }
            @Override
            public void forEach(Consumer<? super XSElementDecl> action) {
                if (xsElementDeclIterator.hasNext()) {
                    action.accept(xsElementDeclIterator.next());
                }
            }
            @Override
            public Spliterator<XSElementDecl> spliterator() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
