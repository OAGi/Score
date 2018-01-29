package org.oagi.srt.persistence.populate.helper;

import com.sun.xml.internal.xsom.*;
import com.sun.xml.internal.xsom.parser.XSOMParser;
import net.sf.saxon.lib.NamespaceConstant;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.OAGiNamespaceContext;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.ModuleRepository;
import org.oagi.srt.repository.entity.Module;
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
import javax.xml.parsers.SAXParserFactory;
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
    private ModuleRepository moduleRepository;

    public Context(File file, ModuleRepository moduleRepository) throws Exception {
        XSOMParser xsomParser = new XSOMParser(SAXParserFactory.newInstance());
        xsomParser.parse(file);

        xsSchemaSet = xsomParser.getResult();
        if (xsSchemaSet == null) {
            throw new IllegalStateException();
        }

        this.file = file;
        this.moduleRepository = moduleRepository;
    }

    private Document loadDocument(Locator locator) {
        if (locator == null) {
            return null;
        }
        String systemId = locator.getSystemId();
        return Context.loadDocument(systemId);
    }

    public ElementDecl getRootElementDecl() throws Exception {
        String fileName = file.getName();
        String rootElementName = fileName.substring(0, fileName.indexOf(".xsd"));
        XSElementDecl xsElementDecl =
                xsSchemaSet.getElementDecl(SRTConstants.OAGI_NS, rootElementName);

        Document document = loadDocument(xsElementDecl.getLocator());
        Element element = (Element)
                xPath.evaluate("//xsd:element[@name='" + rootElementName + "']", document, XPathConstants.NODE);

        return new ElementDecl(this, xsElementDecl, element);
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

    public Module findByModule(String module) {
        return moduleRepository.findByModule(module);
    }
}
