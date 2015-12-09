package org.oagi.srt.generate.samplexml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.oagi.srt.common.SRTConstants;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class ValidateXML {

	public static void validate(File xml, InputStream xsd, String xsdfilename) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			factory.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);
			factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
			
			Schema schema = factory.newSchema(new StreamSource(xsd));
			
			Validator validator = schema.newValidator();
		    final List<SAXParseException> exceptions = new LinkedList<SAXParseException>();
		    validator.setErrorHandler(new ErrorHandler()
			  {
			    public void warning(SAXParseException exception) throws SAXException
			    {
			    	exceptions.add(exception);
			    }

			    public void fatalError(SAXParseException exception) throws SAXException
			    {
			    	if(exception.getMessage().startsWith("cvc-complex-type.2.4.a") && !exception.getMessage().contains("ns:anyElement"))
			    		System.out.println(exception);
			    	exceptions.add(exception);
			    }

			    public void error(SAXParseException exception) throws SAXException
			    {
			    	if(exception.getMessage().startsWith("cvc-complex-type.2.4.a") && !exception.getMessage().contains("ns:anyElement"))
			    		System.out.println(exception);
			    	exceptions.add(exception);
			    }
			  });
			StreamSource xmlFile = new StreamSource(xml);
			validator.validate(xmlFile);
			System.out.println(xml.getName()+" is valid against given"+ xsdfilename);
			System.out.println("");
		} catch (SAXException e) {
            e.printStackTrace();
   		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void validate_exp(String xsdfilename, String xmlfilename) throws FileNotFoundException {
		File xml = new File(SRTConstants.TEST_XML_FILE_PATH+xmlfilename+".xml");
		InputStream xsd = new FileInputStream(SRTConstants.TEST_BOD_FILE_PATH+xsdfilename+".xsd");
		validate(xml, xsd, xsdfilename);
	}

	public static void main(String args[]) throws FileNotFoundException {
		File xml = new File(SRTConstants.TEST_XML_FILE_PATH+"AcknowledgeField1.xml");
		InputStream xsd = new FileInputStream(SRTConstants.TEST_BOD_FILE_PATH+"AcknowledgeField_modified.xsd");
		//InputStream xsd = new FileInputStream("/Temp/test/AcknowledgeField.xsd");
		validate(xml, xsd, "AcknowledgeField_modified");
	}
}