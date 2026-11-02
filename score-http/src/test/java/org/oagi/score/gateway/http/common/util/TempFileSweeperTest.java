package org.oagi.score.gateway.http.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempFileSweeperTest {

    @TempDir
    File root;

    private File directory(String name, Duration age) throws Exception {
        File directory = new File(root, name);
        Files.createDirectory(directory.toPath());
        Files.writeString(new File(directory, "a.xsd").toPath(), "x");
        assertTrue(directory.setLastModified(System.currentTimeMillis() - age.toMillis()));
        return directory;
    }

    @Test
    void removesOnlyOldDirectoriesOfTempFiles() throws Exception {
        File old = directory(TempFiles.DIRECTORY_PREFIX + "old", Duration.ofHours(3));
        File recent = directory(TempFiles.DIRECTORY_PREFIX + "recent", Duration.ofMinutes(5));
        File other = directory("other-old", Duration.ofHours(3));

        assertEquals(1, TempFileSweeper.sweep(root, TempFileSweeper.MAX_AGE));

        assertFalse(old.exists());
        assertTrue(recent.exists());
        assertTrue(other.exists());
    }
}
