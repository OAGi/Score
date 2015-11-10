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
	
	private File[] getBODs(File f) {
		return f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(".*.xsd");
			}
		});
	}
	
	public void macrotest_create(File f1) throws Exception {
		TopLevelABIEHandler a = new TopLevelABIEHandler();
		File[] listOfF1 = getBODs(f1);

		for (File file : listOfF1) {
			String bodname = Utility.spaceSeparator(file.getName().substring(0, file.getName().indexOf(".")));
			a.macro(bodname);
		}

	}
	
	public void macrotest_validate(File f1) throws Exception {
		
		File[] listOfF1 = getBODs(f1);
		
		for (File file : listOfF1) {
			if(!file.getName().substring(0, file.getName().indexOf(".")).endsWith("_created")) {
				String oagxsdfilename = file.getName().substring(0, file.getName().indexOf("."));
				String generatedxsdfilename = oagxsdfilename+"_created";
				String oagxsdfilename_remove_any = oagxsdfilename+"_remove_any";
				String generatedxsdfilename_remove_any = generatedxsdfilename+"_remove_any";
				for(File file2 : listOfF1){
					if(file2.getName().substring(0, file2.getName().indexOf(".")).equals(generatedxsdfilename)) {
						String oagxmlfilename = "XML_From_"+oagxsdfilename;
						String generatedxmlfilename = "XML_From_"+generatedxsdfilename;
						String rootElementname = oagxsdfilename;
						String prefix = "xs";
						XmlTest.xmltest_exp_remove_any(oagxsdfilename, oagxmlfilename, rootElementname, prefix);
						XmlTest.xmltest_exp_remove_any(generatedxsdfilename, generatedxmlfilename, rootElementname, prefix);
						System.out.println("### Start to validate "+oagxsdfilename);
						//ValidateXML.validate_exp(oagxsdfilename_remove_any, oagxmlfilename);
						//ValidateXML.validate_exp(generatedxsdfilename_remove_any, generatedxmlfilename);	
						ValidateXML.validate_exp(generatedxsdfilename_remove_any, oagxmlfilename);	
						ValidateXML.validate_exp(oagxsdfilename_remove_any, generatedxmlfilename);
						System.out.println("### Finish validating "+oagxsdfilename);
					}		
				}
			}
		System.gc();
		}
	}
	
	public static void main(String args[]) throws FileNotFoundException, Exception {
		String EXP_BOD_FILE_PATH = args[0];
		File f1 = new File(EXP_BOD_FILE_PATH);
		System.out.println(EXP_BOD_FILE_PATH);
		Experiment a = new Experiment();
		a.macrotest_create(f1);
		Thread.sleep(150);
		a.macrotest_validate(f1);
	}
}
