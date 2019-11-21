package org.oagi.srt.gateway.http.helper;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    private static final Logger logger = LoggerFactory.getLogger(Zip.class);
    private static final int BUFFER = 2048;

    public static File compression(Collection<File> targetFiles, String filename) throws IOException {
        File file = File.createTempFile("oagis-", null);
        FileOutputStream dest = new FileOutputStream(file);
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest))) {
            // out.setMethod(ZipOutputStream.DEFLATED);
            byte[] data = new byte[BUFFER];

            logger.info("Compressing files in Zip format...");
            for (File targetFile : targetFiles) {
                logger.info("Adding: " + targetFile);
                FileInputStream fi = new FileInputStream(targetFile);
                try (BufferedInputStream origin = new BufferedInputStream(fi, BUFFER)) {
                    ZipEntry entry = new ZipEntry(targetFile.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.flush();
                }
            }
        }

        File renamedFile = new File(file.getParentFile(), filename + ".zip");
        if (file.renameTo(renamedFile)) {
            FileUtils.deleteQuietly(file);
            file = renamedFile;
        }

        return file;
    }

}
