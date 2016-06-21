package org.oagi.srt.common.util;

import org.oagi.srt.common.SRTConstants;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
	static final int BUFFER = 2048;
	static public String filename;

	public static String compression (String filename) throws IOException {
		BufferedInputStream origin = null;
		String filepath = SRTConstants.BOD_FILE_PATH + filename + ".zip";
		FileOutputStream dest = new FileOutputStream(filepath);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		// out.setMethod(ZipOutputStream.DEFLATED);
		byte data[] = new byte[BUFFER];

		File f = new File(SRTConstants.BOD_FILE_PATH + "package/.");
		String files[] = f.list();
		System.out.println("Compressing files in Zip format...");
		for (int i = 0; i < files.length; i++) {
			System.out.println("Adding: " + files[i]);
			FileInputStream fi = new FileInputStream(SRTConstants.BOD_FILE_PATH + "package/" + files[i]);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(files[i]);
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
		}
		out.close();
		return filepath;
	}
	
	public static void main(String args[]) {
		
	}
}
