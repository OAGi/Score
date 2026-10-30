package org.oagi.score.gateway.http.common.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Temporary files that keep a chosen name without being shared: each file gets a directory of its own, so
 * two requests that generate a file with the same name cannot overwrite each other.
 */
public final class TempFiles {

    static final String DIRECTORY_PREFIX = "score-gen-";

    private TempFiles() {
    }

    /**
     * Returns the not yet existing file {@code name} in a new temporary directory.
     */
    public static File newFile(String name) throws IOException {
        return new File(Files.createTempDirectory(DIRECTORY_PREFIX).toFile(), name);
    }

    /**
     * Returns a new temporary directory, for an export that needs several files in a layout of its own. It is removed
     * by its user, or by {@link TempFileSweeper} when that fails.
     */
    public static File newDirectory() throws IOException {
        return Files.createTempDirectory(DIRECTORY_PREFIX).toFile();
    }

    /**
     * Deletes a file made by {@link #newFile(String)} together with its directory; any other file is only deleted itself.
     */
    public static void deleteQuietly(File file) {
        if (file == null) {
            return;
        }
        File parent = file.getParentFile();
        FileUtils.deleteQuietly(file);
        if (parent != null && parent.getName().startsWith(DIRECTORY_PREFIX)) {
            // only removes the directory when it is empty: the files of another export are never in it
            parent.delete();
        }
    }
}
