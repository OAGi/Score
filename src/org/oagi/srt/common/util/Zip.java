package org.oagi.srt.common.util;

import java.io.*;
import java.util.zip.*;

public class Zip {
	static final int BUFFER = 2048;
	static public String filename;

	public static void main(String filename) {
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream("/Temp/test/"+filename+".zip");
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			// out.setMethod(ZipOutputStream.DEFLATED);
			byte data[] = new byte[BUFFER];

			File f = new File("/Temp/test/Package/.");
			String files[] = f.list();
			System.out.println("Compressing files in Zip format...");
			for (int i = 0; i < files.length; i++) {
				System.out.println("Adding: " + files[i]);
				FileInputStream fi = new FileInputStream("/Temp/test/Package/"+files[i]);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
