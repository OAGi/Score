package org.oagi.srt.generate.samplexml;

import jlibs.xml.*;
import jlibs.xml.dom.*;
import jlibs.xml.sax.*;
import jlibs.xml.xsd.*;

import org.apache.xerces.xs.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringReader;

import org.oagi.srt.common.SRTConstants;

public class XmlTest implements XSInstance.SampleValueGenerator{
    public String generateSampleValue(XSElementDeclaration elem, XSSimpleTypeDefinition xsSimpleTypeDefinition){
        return generateSampleValue(elem.getAnnotation());
    }

    public String generateSampleValue(XSAttributeDeclaration attr, XSSimpleTypeDefinition xsSimpleTypeDefinition){
        return generateSampleValue(attr.getAnnotation());
    }

    private String generateSampleValue(XSAnnotation annotation){
        if(annotation!=null){
            String string = annotation.getAnnotationString();
            try{
                Document dom = DOMUtil.newDocumentBuilder(true, false).parse(new InputSource(new StringReader(string)));
                NodeList nodeList = dom.getDocumentElement().getElementsByTagNameNS(Namespaces.URI_XSD, "documentation");
                if(nodeList.getLength()>0)
                    return nodeList.item(0).getTextContent();
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }
        return null;
    }
    
    public static void xmltest_exp(String xsdfilename, String xmlfilename, String rootElementname, String prefix) throws Exception, FileNotFoundException {
    	String oagis = "http://www.openapplications.org/oagis/10";
    	XSModel xsModel = new XSParser().parse(SRTConstants.TEST_BOD_FILE_PATH+xsdfilename+".xsd");
        XMLDocument sampleXML = new XMLDocument(new StreamResult(new FileOutputStream(SRTConstants.TEST_XML_FILE_PATH+xmlfilename+".xml")), false, 4, null);
        XSInstance xsInstance = new XSInstance();
        QName rootElement = new QName(oagis, rootElementname, prefix);
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumElementsGenerated = 1;
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumListItemsGenerated = 1;
        xsInstance.generateAllChoices = true;
        xsInstance.generateOptionalElements = true;
        xsInstance.generateDefaultAttributes = true;
        xsInstance.generateOptionalAttributes = false;
        //xsInstance.sampleValueGenerator = new XmlTest();
        
        xsInstance.showContentModel = false;
        String schemalocation = oagis + " "+ xsdfilename + ".xsd";
        xsInstance.generate(xsModel, rootElement, sampleXML, schemalocation, null);
        
        System.out.println(xmlfilename+".xsd from "+ xsdfilename+".xsd is generated...");
    }
    
    public static void xmltest_exp_remove_any(String xsdfilename, String xmlfilename, String rootElementname, String prefix) throws Exception, FileNotFoundException {
    	String oagis = "http://www.openapplications.org/oagis/10";
    	String newxsdfilename = remove_any(xsdfilename);
    	XSModel xsModel = new XSParser().parse(SRTConstants.TEST_BOD_FILE_PATH+newxsdfilename+".xsd");
        XMLDocument sampleXML = new XMLDocument(new StreamResult(new FileOutputStream(SRTConstants.TEST_XML_FILE_PATH+xmlfilename+".xml")), false, 4, null);
        XSInstance xsInstance = new XSInstance();
        QName rootElement = new QName(oagis, rootElementname, prefix);
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumElementsGenerated = 1;
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumListItemsGenerated = 1;
        xsInstance.generateAllChoices = true;
        xsInstance.generateOptionalElements = true;
        xsInstance.generateDefaultAttributes = true;
        xsInstance.generateOptionalAttributes = false;
        xsInstance.showContentModel = false;
        String schemalocation = oagis + " "+ newxsdfilename + ".xsd";
        xsInstance.generate(xsModel, rootElement, sampleXML, schemalocation, null);
       
        System.out.println(xmlfilename+".xsd from "+ xsdfilename+".xsd is generated...");
    }
    
    public static String remove_any(String xsdfilename) throws Exception {
    	DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); 
    	DocumentBuilder db = f.newDocumentBuilder();
    	File XSD = new File(SRTConstants.TEST_BOD_FILE_PATH+xsdfilename+".xsd");
        Document doc = db.parse(XSD);
        NodeList any = doc.getElementsByTagName("xsd:any");
        for(int i = any.getLength()-1; i >= 0 ; i--){
        	Node bb = any.item(i);
        	bb.getParentNode().removeChild(any.item(i));
        }
        NodeList includenodelist = doc.getElementsByTagName("xsd:include");
        if(includenodelist.getLength() > 0){
        	Node include = includenodelist.item(0);
        	((Element)include).setAttribute("schemaLocation", SRTConstants.COMPONENTS_XSD_FILE_PATH);
        }
//        NodeList union = doc.getElementsByTagName("xsd:union");
//        for(int i = union.getLength()-1; i >= 0 ; i--){
//        	Node bb = union.item(i);
//        	String unionvalue = ((Element)bb).getAttribute("memberTypes");
//        	String[] splits = unionvalue.split(" ");
//        	String split = splits[0];
//        	for(String tmp : splits) {
//        		if(tmp.length()>split.length())
//        			split = tmp;
//        	}
//        	((Element)bb).setAttribute("memberTypes", split);
//        }
        
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        
        DOMSource source = new DOMSource(doc);
        String newxsdfilname = xsdfilename+"_remove_any";
        StreamResult result = new StreamResult(SRTConstants.TEST_BOD_FILE_PATH+newxsdfilname+".xsd");
        t.transform(source, result);
        return newxsdfilname;
    }
    
    
    public static void main(String[] args) throws Exception{
//    	remove_any("AcknowledgeField");
    	String xsdfilename = "AcknowledgeField_created";
    	String xmlfilename= "AcknowledgeField_test";
    	String rootElementname = "AcknowledgeField";
    	String prefix = "xs";
    	String oagis = "http://www.openapplications.org/oagis/10";
    	XSModel xsModel = new XSParser().parse(SRTConstants.TEST_XML_FILE_PATH+xsdfilename+".xsd");
    	XMLDocument sampleXML = new XMLDocument(new StreamResult(new FileOutputStream(SRTConstants.TEST_XML_FILE_PATH+xmlfilename+".xml")), false, 4, null);
        XSInstance xsInstance = new XSInstance();
        QName rootElement = new QName(oagis, rootElementname, prefix);
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumElementsGenerated = 1;
        xsInstance.minimumElementsGenerated = 1;
        xsInstance.maximumListItemsGenerated = 1;
        xsInstance.generateAllChoices = true;
        xsInstance.generateOptionalElements = true;
        xsInstance.generateDefaultAttributes = true;
        xsInstance.generateOptionalAttributes = true;
        //xsInstance.sampleValueGenerator = new XmlTest();
        
        xsInstance.showContentModel = false;
        String schemalocation = oagis + " "+ xsdfilename + ".xsd";
        xsInstance.generate(xsModel, rootElement, sampleXML, schemalocation, null);
        
        System.out.println(xmlfilename+".xsd from "+ xsdfilename+".xsd is generated...");
    }
}