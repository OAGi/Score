package org.oagi.srt.common.util;

import org.oagi.srt.common.SRTConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	
	public NodeList getNodeList(String xPathExpression) throws XPathExpressionException {
		return (NodeList)xPath.compile(xPathExpression).evaluate(xmlDocument, XPathConstants.NODESET);
	}
	
	public Node getNode(String xPathExpression) throws XPathExpressionException {
		return (Node)xPath.compile(xPathExpression).evaluate(xmlDocument, XPathConstants.NODE);
	}
	
	public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException {
	
		XPathHandler xh = new XPathHandler(SRTConstants.BOD_FILE_PATH_02 + "LoadPayable.xsd");
		
		try {
			System.out.println("### " + ((Element)xh.getNode("//xsd:complexType[@name = 'LoadPayableDataAreaType' and count(xsd:simpleContent) = 0] ")).getAttribute("name"));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
}
