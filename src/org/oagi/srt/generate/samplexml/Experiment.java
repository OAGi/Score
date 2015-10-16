package org.oagi.srt.generate.samplexml;

import java.io.File;
import java.io.FileNotFoundException;

import org.chanchan.common.persistence.db.BfPersistenceException;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.web.handler.TopLevelABIEHandler;

import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

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
							XmlTest.xmltest_exp_remove_any(oagxsdfilename, oagxmlfilename, rootElementname, prefix);
							XmlTest.xmltest_exp_remove_any(generatedxsdfilename, generatedxmlfilename, rootElementname, prefix);
	
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
	
	public void macrotest_create() throws Exception {
		TopLevelABIEHandler a = new TopLevelABIEHandler();
		//String filepath = a.macro("oagis-id-dedeb4e4be384d5282c33ee5533f5ff2");
		//String filepath = a.macro("oagis-id-9712b728b0d34677a367b2a3555bcdfa");
		File[] listOfF1 = getBODs(f1);

		for (File file : listOfF1) {
			String bodname = Utility.spaceSeparator(file.getName().substring(0, file.getName().indexOf(".")));
			long time = System.currentTimeMillis(); 
			SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
			String str = dayTime.format(new Date(time));
			System.out.println(str);
			long start = System.currentTimeMillis() ; 
			System.out.println("Before creating profie BOD : " + str); 
			a.macro(bodname);
			long end = System.currentTimeMillis(); 
			System.out.println("After creating profie BOD : " + str + "   computation time = " + (end-start)); 
			System.gc();
		}

	}
	
	public void macrotest_validate() throws Exception {
		
		File[] listOfF1 = getBODs(f1);
		
		for (File file : listOfF1) {
			if(!file.getName().substring(0, file.getName().indexOf(".")).endsWith("_created")) {
				String oagxsdfilename = file.getName().substring(0, file.getName().indexOf("."));
				String generatedxsdfilename = oagxsdfilename+"_created";
				String oagxsdfilename_remove_any = oagxsdfilename+"_remove_any";
				String generatedxsdfilename_remove_any = generatedxsdfilename+"_remove_any";
				for(File file2 : listOfF1){
					if(file2.getName().substring(0, file2.getName().indexOf(".")).equals(generatedxsdfilename)) {
						System.out.println("Processing "+file2.getName().substring(0, file2.getName().indexOf(".")));
						String oagxmlfilename = "XML_From_"+oagxsdfilename;
						String generatedxmlfilename = "XML_From_"+generatedxsdfilename;
						String rootElementname = oagxsdfilename;
						String prefix = "xs";
						XmlTest.xmltest_exp_remove_any(oagxsdfilename, oagxmlfilename, rootElementname, prefix);
						XmlTest.xmltest_exp_remove_any(generatedxsdfilename, generatedxmlfilename, rootElementname, prefix);

						ValidateXML.validate_exp(oagxsdfilename_remove_any, oagxmlfilename);
						ValidateXML.validate_exp(generatedxsdfilename_remove_any, generatedxmlfilename);	
						ValidateXML.validate_exp(oagxsdfilename_remove_any, generatedxmlfilename);
						ValidateXML.validate_exp(generatedxsdfilename_remove_any, oagxmlfilename);		
					}		
				}
			}
		System.gc();
		}
	}
	
	public static void main(String args[]) throws FileNotFoundException, Exception {
		Experiment a = new Experiment();
		//a.test();
		a.macrotest_create();
		a.macrotest_validate();
	}
}
