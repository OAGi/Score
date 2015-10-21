package org.oagi.srt.generate.samplexml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;


public class Result {
	
	public static void main(String args[]){
		
		FileReader fr = null;
		FileWriter fw = null;
		FileWriter fw2 = null;
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		BufferedWriter bw2 = null;
		
		Date d = null;
		
		try{
			
			fr = new FileReader(Result.class.getResource("").getPath()+"Result.txt");
			br = new BufferedReader(fr);
			
			fw = new FileWriter(Result.class.getResource("").getPath()+"Report_raw.txt", false);
			bw = new BufferedWriter(fw);
			
			fw2 = new FileWriter(Result.class.getResource("").getPath()+"Report.txt", false);
			bw2 = new BufferedWriter(fw2);
			
			String s = null;
			d = new Date();

			long start = d.getTime();
			Vector elements = new Vector<String>();
			
			int bod = 0, fail_bod = 0;
			boolean success_check = true;
			while((s=br.readLine())!=null){

				String ss = null;
				if(s.startsWith("Processing")){
					
					if(!success_check)
						fail_bod++;
					
					bw.newLine(); bw.newLine();
					ss = "###" + s.substring(s.indexOf("Processing")+11, s.indexOf("_"));
					bw.write(ss); bw.newLine();
					
					bw2.newLine(); bw2.newLine();
					bw2.write(ss); bw2.newLine();
					elements.clear();
					success_check = true;
					bod++;
				}
				if(s.startsWith("cvc-complex-type.2.4.a:")){
					ss = s.substring(s.indexOf("cvc-complex-type.2.4.a:") + 24);
					bw.write(ss); bw.newLine();
					
					ss = s.substring(s.indexOf("cvc-complex-type.2.4.a: Invalid content was found starting with element 'xs:") + "cvc-complex-type.2.4.a: Invalid content was found starting with element 'xs:".length(), s.indexOf("'. One of '"));
					boolean dup_check = false;
					for(int i = 0; i < elements.size() ; i++)
						if(elements.get(i).toString().equals(ss))
							dup_check = true;					
					if(!dup_check){
						elements.add(ss);
						bw2.write(ss+", ");
					}
					success_check = false;
				}
			}
			
			d = new Date();
			long end = d.getTime();
			int success_bod = bod - fail_bod;
			bw2.write("\n\n#Summary#\n"+"Number of validated BODs = " +bod+"\nSucceeded in validating BODs = "+ success_bod +"\nFailed to validate BODs = " + fail_bod);
			System.out.println("Processing Time : " + (end-start));
			
			
		}catch(Exception e){
			
			e.printStackTrace();
		
		}finally{
			
			if(br != null) try{br.close();}catch(IOException e){}
			if(fr != null) try{fr.close();}catch(IOException e){}
			
			if(bw != null) try{bw.close();}catch(IOException e){}
			if(fw != null) try{fw.close();}catch(IOException e){}
			if(bw2 != null) try{bw2.close();}catch(IOException e){}
			if(fw2 != null) try{fw2.close();}catch(IOException e){}
		}
		
	}

}