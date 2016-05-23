package org.oagi.srt.common.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.FileInputStream;
import java.io.IOException;

public class XPathHandler {

    private Document xmlDocument;
    private DocumentBuilder builder;
    private XPath xPath;

    public XPathHandler(String filePath) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);

        builder = builderFactory.newDocumentBuilder();
        xmlDocument = builder.parse(new FileInputStream(filePath));
        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new OAGiNamespaceContext());
    }

    public XPathExpression compile(String expression) throws XPathExpressionException {
        return xPath.compile(expression);
    }

    public NodeList getNodeList(String xPathExpression) throws XPathExpressionException {
        return (NodeList) xPath.compile(xPathExpression).evaluate(xmlDocument, XPathConstants.NODESET);
    }

    public Node getNode(String xPathExpression) throws XPathExpressionException {
        return getNode(xmlDocument, xPathExpression);
    }

    public Node getNode(Object item, String xPathExpression) throws XPathExpressionException {
        return getNode(xPath.compile(xPathExpression), item);
    }

    public Node getNode(XPathExpression expression, Object item) throws XPathExpressionException {
        return (Node) expression.evaluate(item, XPathConstants.NODE);
    }

}
