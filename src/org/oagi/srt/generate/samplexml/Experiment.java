package org.oagi.srt.generate.samplexml;

import java.io.File;
import java.io.FileNotFoundException;

import org.oagi.srt.common.SRTConstants;

import java.io.FilenameFilter;


public class Experiment {
	
	private static File f1 = new File(SRTConstants.TEST_BOD_FILE_PATH);

	private File[] getBODs(File f) {
		return f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*.xsd");
			}
		});
	}
	
	public void test() throws FileNotFoundException, Exception {

		System.out.println("Please, press the test_type (1 : create sample instances and validation, 2 : validation");
		
		int test_type = System.in.read();
		
		File[] listOfF1 = getBODs(f1);

		for (File file : listOfF1) {
			if(!file.getName().substring(0, file.getName().indexOf(".")).endsWith("_created")) {
				String oagxsdfilename = file.getName().substring(0, file.getName().indexOf("."));
				String generatedxsdfilename = oagxsdfilename+"_created";
				for(File file2 : listOfF1){
					if(file2.getName().substring(0, file2.getName().indexOf(".")).equals(generatedxsdfilename)) {
						System.out.println("Processing "+file2.getName().substring(0, file2.getName().indexOf(".")));
						String oagxmlfilename = "XML_From_"+oagxsdfilename;
						String generatedxmlfilename = "XML_From_"+generatedxsdfilename;
						String rootElementname = oagxsdfilename;
						String prefix = "xs";
						if(test_type == 49) {
							XmlTest.xmltest_exp(oagxsdfilename, oagxmlfilename, rootElementname, prefix);
							XmlTest.xmltest_exp(generatedxsdfilename, generatedxmlfilename, rootElementname, prefix);
	
						}
						ValidateXML.validate_exp(oagxsdfilename, oagxmlfilename);
						ValidateXML.validate_exp(generatedxsdfilename, generatedxmlfilename);	
						ValidateXML.validate_exp(oagxsdfilename, generatedxmlfilename);
						ValidateXML.validate_exp(generatedxsdfilename, oagxmlfilename);		
					}		
				}
			}
		}
	}
	
	public static void main(String args[]) throws FileNotFoundException, Exception  {
		Experiment a = new Experiment();
		a.test();
	}
}
