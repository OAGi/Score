package org.oagi.score.gateway.http.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    private static final Logger logger = LoggerFactory.getLogger(Zip.class);
    private static final int BUFFER = 2048;

    /**
     * Zips the files into {@code filename}.zip, which is a file of its own in a temporary directory of its own,
     * so another export with the same name cannot replace it. The {@code targetFiles} are left as they are.
     */
    public static File compression(Collection<File> targetFiles, String filename) throws IOException {
        File file = TempFiles.newFile(filename + ".zip");
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

        return file;
    }

    public static File compressionHierarchy(File baseDirectory, Collection<File> targetFiles) throws IOException {
        // If it's running on Mac OS X, target files are generated under '/private/var/folders'.
        // But, the base directory is '/var/folders'. Since '/var' is a symlink of '/private/var' on Mac OS X,
        // renaming the base directory impacts on nothing.
        if (baseDirectory.getAbsolutePath().startsWith("/var/folders")) {
            baseDirectory = new File("/private/" + baseDirectory.getAbsolutePath());
        }
        File file = TempFiles.newFile("oagis.zip");
        URI currentPath = baseDirectory.toURI();
        FileOutputStream dest = new FileOutputStream(file);
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest))) {
            logger.info("Compressing files in Zip format... " + file);
            for (File targetFile : targetFiles) {
                logger.info("Adding: " + targetFile);
                URI relativePath = currentPath.relativize(targetFile.toURI());
                zipFile(targetFile, relativePath.toString(), out);
            }
        }
        return file;
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[BUFFER];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

}
