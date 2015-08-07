package org.oagi.srt.generate.samplexml;

import jlibs.xml.*;
import jlibs.xml.dom.*;
import jlibs.xml.sax.*;
import jlibs.xml.xsd.*;

import org.apache.xerces.xs.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;

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
    	XSModel xsModel = new XSParser().parse(SRTConstants.XML_TEST_FILE_PATH+xsdfilename+".xsd");
        XMLDocument sampleXML = new XMLDocument(new StreamResult(new FileOutputStream(SRTConstants.XML_TEST_FILE_PATH+xmlfilename+".xml")), false, 4, null);
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
    
    public static void main(String[] args) throws Exception{
    	String xsdfilename = "AcknowledgeField";
    	String xmlfilename= "AcknowledgeField1";
    	String rootElementname = "AcknowledgeField";
    	String prefix = "xs";
    	String oagis = "http://www.openapplications.org/oagis/10";
    	XSModel xsModel = new XSParser().parse(SRTConstants.XML_TEST_FILE_PATH+xsdfilename+".xsd");
        XMLDocument sampleXML = new XMLDocument(new StreamResult(new FileOutputStream(SRTConstants.XML_TEST_FILE_PATH+xmlfilename+".xml")), false, 4, null);
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