package org.oagi.srt.generate.samplexml;

import java.io.FileNotFoundException;


public class Experiment {
	public static void main(String args[]) throws FileNotFoundException, Exception {
		//Constant setting
		String oagxsdfilename = "AcknowledgeField";
		String generatedxsdfilename = "AcknowledgeField_modified";
		String oagxmlfilename = "SampleXMLfrom"+oagxsdfilename;
		String generatedxmlfilename = "SampleXMLfrom"+generatedxsdfilename;
		String rootElementname = "AcknowledgeField";
		String prefix = "xs";
		
		//xml file generation
		//XmlTest.xmltest_exp(oagxsdfilename, oagxmlfilename, rootElementname, prefix);
		//XmlTest.xmltest_exp(generatedxsdfilename, generatedxmlfilename, rootElementname, prefix);
		//ValidateXML.validate_exp(oagxsdfilename, oagxmlfilename);
		//ValidateXML.validate_exp(generatedxsdfilename, generatedxmlfilename);
		ValidateXML.validate_exp(oagxsdfilename, generatedxmlfilename);
		ValidateXML.validate_exp(generatedxsdfilename, oagxmlfilename);
		
	}
}
