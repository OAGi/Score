package org.oagi.srt.common.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
	static final int BUFFER = 2048;

	public static String compression (String filename) throws IOException {
		BufferedInputStream origin = null;
		File filepath = File.createTempFile(filename, ".zip");
		FileOutputStream dest = new FileOutputStream(filepath);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		// out.setMethod(ZipOutputStream.DEFLATED);
		byte data[] = new byte[BUFFER];

		File f = new File(filepath.getParentFile(), "package");
		File files[] = f.listFiles();
		System.out.println("Compressing files in Zip format...");
		for (int i = 0; i < files.length; i++) {
			System.out.println("Adding: " + files[i]);
			FileInputStream fi = new FileInputStream(files[i]);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(files[i].getName());
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
		}
		out.close();
		return filepath.getCanonicalPath();
	}
}
