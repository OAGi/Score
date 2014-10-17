package org.oagi.srt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ASCCPBCCPInvestigator {

	public static void main(String args[]) throws IOException {
		File f1 = new File("/Users/yslee/Work/Project/OAG/Development/asccp_bccp_serm.txt");
		File f2 = new File("/Users/yslee/Work/Project/OAG/Development/asccp.txt");
		File f3 = new File("/Users/yslee/Work/Project/OAG/Development/bccp.txt");
		
		ArrayList<String> al1 = new ArrayList<String>();
		BufferedReader br1 = new BufferedReader(new FileReader(f1));
		String line1;
		while ((line1 = br1.readLine()) != null) {
		   al1.add(line1);
		}
		br1.close();
		
		ArrayList<String> al2 = new ArrayList<String>();
		BufferedReader br2 = new BufferedReader(new FileReader(f2));
		String line2;
		while ((line2 = br2.readLine()) != null) {
			al2.add(line2);
		}
		br2.close();
		
		BufferedReader br3 = new BufferedReader(new FileReader(f3));
		String line3;
		while ((line3 = br3.readLine()) != null) {
			al2.add(line3);
		}
		br3.close();
		
		System.out.println("### serm size: " + al1.size());
		System.out.println("### my size: " + al2.size());
		
		ArrayList<String> r1 = new ArrayList<String>();
		ArrayList<String> r2 = new ArrayList<String>();
		ArrayList<String> r3 = new ArrayList<String>();
		ArrayList<String> r4 = new ArrayList<String>();
		
		for(String s1 : al1) {
			if(!al2.contains(s1)) {
				r1.add(s1);
			} else {
				r2.add(s1);
			}
		}
		
		//for(String s : r1)
			//System.out.println(s);
		
		System.out.println("----------------------------------");
		
		for(String s1 : al2) {
			if(!al1.contains(s1)) {
				r3.add(s1);
			} else {
				r4.add(s1);
			}
		}
		
		System.out.println(r1.size());
		System.out.println(r2.size());
		System.out.println(r3.size());
		System.out.println(r4.size());
		
		System.out.println(r1.size());
		for(String s : r1)
			System.out.println(s);
	}
}
