package org.oagi.srt.common.util;

import org.oagi.srt.persistence.populate.helper.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;

public class XPathHandler {

    private Document xmlDocument;

    public XPathHandler(String filePath) throws Exception {
        this(new File(filePath));
    }

    public XPathHandler(File file) throws Exception {
        xmlDocument = Context.loadDocument(file);
    }

    public XPathExpression compile(String expression) throws XPathExpressionException {
        return Context.xPath.compile(expression);
    }

    public NodeList getNodeList(String xPathExpression) throws XPathExpressionException {
        return getNodeList(xmlDocument, xPathExpression);
    }

    public NodeList getNodeList(Object item, String xPathExpression) throws XPathExpressionException {
        return (NodeList) Context.xPath.compile(xPathExpression).evaluate(item, XPathConstants.NODESET);
    }

    public Node getNode(String xPathExpression) throws XPathExpressionException {
        return getNode(xmlDocument, xPathExpression);
    }

    public Node getNode(Object item, String xPathExpression) throws XPathExpressionException {
        return getNode(Context.xPath.compile(xPathExpression), item);
    }

    public Node getNode(XPathExpression expression, Object item) throws XPathExpressionException {
        return (Node) expression.evaluate(item, XPathConstants.NODE);
    }

}
