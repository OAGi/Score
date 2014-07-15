package org.oagi.srt.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XPathHandler {

	private Document xmlDocument;
	private DocumentBuilder builder;
	private XPath xPath;
    
	public XPathHandler(String filePath) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException {
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
	
	public static void main(String args[]) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		XPathHandler xh = new XPathHandler("D:/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/Common/Components/Fields.xsd");
		
		try {
			NodeList result = xh.getNodeList("//xsd:complexType[@name = 'AmountType']");
			for(int i = 0; i < result.getLength(); i++) {
			    Element e = (Element)result.item(i);
			    System.out.println(e.getAttribute("name"));
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
}
