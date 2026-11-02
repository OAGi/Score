package org.oagi.score.gateway.http.common.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generated files of the same name must not share a path, so concurrent exports cannot overwrite each other.
 */
class TempFilesTest {

    @Test
    void sameNameGivesDistinctPaths() throws Exception {
        File first = TempFiles.newFile("manifest.json");
        File second = TempFiles.newFile("manifest.json");
        try {
            assertEquals("manifest.json", first.getName());
            assertEquals("manifest.json", second.getName());
            assertNotEquals(first.getParentFile(), second.getParentFile());

            Files.writeString(first.toPath(), "package one");
            Files.writeString(second.toPath(), "package two");
            assertEquals("package one", Files.readString(first.toPath()));
        } finally {
            TempFiles.deleteQuietly(first);
            TempFiles.deleteQuietly(second);
        }
    }

    @Test
    void deleteRemovesTheFileAndItsDirectory() throws Exception {
        File file = TempFiles.newFile("a.xsd");
        Files.writeString(file.toPath(), "x");

        TempFiles.deleteQuietly(file);

        assertFalse(file.exists());
        assertFalse(file.getParentFile().exists());
    }

    @Test
    void zipIsAFileOfItsOwn() throws Exception {
        File input = TempFiles.newFile("a.xsd");
        Files.writeString(input.toPath(), "x");
        File zip1 = Zip.compression(List.of(input), "same");
        File zip2 = Zip.compression(List.of(input), "same");
        try {
            assertEquals("same.zip", zip1.getName());
            assertNotEquals(zip1, zip2);
            assertTrue(zip1.length() > 0 && input.exists());
        } finally {
            TempFiles.deleteQuietly(input);
            TempFiles.deleteQuietly(zip1);
            TempFiles.deleteQuietly(zip2);
        }
    }

    @Test
    void concurrentRequestsWithTheSameNameNeitherShareNorOverwriteAFile() throws Exception {
        int requests = 32;
        ExecutorService executor = Executors.newFixedThreadPool(requests);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<File>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < requests; i++) {
                String content = "request " + i;
                Callable<File> request = () -> {
                    start.await();
                    File file = TempFiles.newFile("oagis.zip");
                    Files.writeString(file.toPath(), content);
                    return file;
                };
                futures.add(executor.submit(request));
            }
            start.countDown();

            Set<File> paths = new HashSet<>();
            for (int i = 0; i < requests; i++) {
                File file = futures.get(i).get();
                paths.add(file);
                assertEquals("oagis.zip", file.getName());
                assertEquals("request " + i, Files.readString(file.toPath()));
            }
            assertEquals(requests, paths.size());
            paths.forEach(TempFiles::deleteQuietly);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void newDirectoryIsASweepableDirectoryOfItsOwn() throws Exception {
        File first = TempFiles.newDirectory();
        File second = TempFiles.newDirectory();
        try {
            assertTrue(first.isDirectory() && first.getName().startsWith(TempFiles.DIRECTORY_PREFIX));
            assertNotEquals(first, second);
        } finally {
            first.delete();
            second.delete();
        }
    }

    @Test
    void otherFilesLeaveTheirDirectoryAlone() throws Exception {
        File dir = Files.createTempDirectory("not-ours-").toFile();
        File file = new File(dir, "keep.txt");
        Files.writeString(file.toPath(), "x");
        try {
            TempFiles.deleteQuietly(file);
            assertFalse(file.exists());
            assertTrue(dir.exists());
        } finally {
            dir.delete();
        }
    }
}
