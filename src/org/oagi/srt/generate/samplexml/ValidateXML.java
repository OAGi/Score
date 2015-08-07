package org.oagi.srt.generate.samplexml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.oagi.srt.common.SRTConstants;
import org.xml.sax.SAXException;

public class ValidateXML {

	public static void validate(File xml, InputStream xsd) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(new StreamSource(xsd));
			Validator validator = schema.newValidator();
			StreamSource xmlFile = new StreamSource(xml);
			validator.validate(xmlFile);
			System.out.println("Given xml file is valid against given schema");
		} catch (SAXException e) {
            System.out.print(xml.getName() + " is not valid, because ");
            System.out.println(e.getMessage());
            e.printStackTrace();
   		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void validate_exp(String xsdfilename, String xmlfilename) throws FileNotFoundException {
		File xml = new File(SRTConstants.XML_TEST_FILE_PATH+xmlfilename+".xml");
		InputStream xsd = new FileInputStream(SRTConstants.XML_TEST_FILE_PATH+xsdfilename+".xsd");
		validate(xml, xsd);
	}

	public static void main(String args[]) throws FileNotFoundException {
		File xml = new File(SRTConstants.XML_TEST_FILE_PATH+"AcknowledgeField1.xml");
		InputStream xsd = new FileInputStream(SRTConstants.XML_TEST_FILE_PATH+"AcknowledgeField_modified.xsd");
		//InputStream xsd = new FileInputStream("/Temp/test/AcknowledgeField.xsd");
		validate(xml, xsd);
	}
}
