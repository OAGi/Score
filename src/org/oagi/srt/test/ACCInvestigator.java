package org.oagi.srt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ACCInvestigator {

	public static void main(String args[]) throws IOException {
		File f1 = new File("/Users/yslee/Work/Project/OAG/Development/acc_serm.txt");
		File f2 = new File("/Users/yslee/Work/Project/OAG/Development/acc.txt");
		
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
		
		System.out.println("### serm size: " + al1.size());
		System.out.println("### my size: " + al2.size());
		
		ArrayList<String> r = new ArrayList<String>();
		
		for(String s1 : al1) {
			if(!al2.contains(s1)) {
				r.add(s1);
			}
		}
		
		for(String s : r)
			System.out.println(s);
	}
}
