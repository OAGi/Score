package org.oagi.srt.export.model;

import org.oagi.srt.common.util.OAGiNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class BdtsBlob {

    private XPath xPath;
    private Document document;

    public BdtsBlob(byte[] content) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            try (InputStream inputStream = new ByteArrayInputStream(content)) {
                this.document = documentBuilder.parse(inputStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new OAGiNamespaceContext());
    }

    public boolean exists(String guid) {
        try {
            String expression = "//xsd:simpleType[@id='" + guid + "'] | //xsd:complexType[@id='" + guid + "']";
            Node node = (Node) xPath.evaluate(expression, this.document, XPathConstants.NODE);
            return (node != null) ? true : false;
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

}
