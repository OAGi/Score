package org.oagi.srt.generate.samplexml;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.web.handler.TopLevelABIEHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class Experiment implements Runnable {
	
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
			if(!file.getName().substring(0, file.getName().indexOf(".")).endsWith("_standlone")) {
				String oagxsdfilename = file.getName().substring(0, file.getName().indexOf("."));
				String generatedxsdfilename = oagxsdfilename+"_standalone";
				String oagxsdfilename_remove_any = oagxsdfilename+"_replace_any";
				String generatedxsdfilename_remove_any = generatedxsdfilename+"_replace_any";
				for(File file2 : listOfF1){
					if(file2.getName().substring(0, file2.getName().indexOf(".")).equals(generatedxsdfilename)) {
						String oagxmlfilename = "XML_From_"+oagxsdfilename;
						String generatedxmlfilename = "XML_From_"+generatedxsdfilename;
						String rootElementname = oagxsdfilename;
						String prefix = "xs";
						XmlTest.xmltest_exp_replace_any(oagxsdfilename, oagxmlfilename, rootElementname, prefix);
						XmlTest.xmltest_exp_replace_any(generatedxsdfilename, generatedxmlfilename, rootElementname, prefix);
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
	
	private Thread t;
	String EXP_BOD_FILE_PATH;
	File f1 ;
	
	public void start() {
		if( t == null) {
			t = new Thread(this, "New Thread");
			t.run();
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Thread = "+Thread.currentThread().getName());
		try {
			macrotest_create(f1);
			macrotest_validate(f1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String args[]) throws FileNotFoundException, Exception {
		
//		Experiment a = new Experiment();
//		a.macrotest_create(f1);
//		a.macrotest_validate(f1);
		
		Experiment R1 = new Experiment();
		R1.EXP_BOD_FILE_PATH = args[0];
		R1.f1 = new File(R1.EXP_BOD_FILE_PATH);
		R1.start();
		
//		Experiment R2 = new Experiment();
//		R2.f1 = new File(R1.EXP_BOD_FILE_PATH);
//		R2.start();

		
	}

}