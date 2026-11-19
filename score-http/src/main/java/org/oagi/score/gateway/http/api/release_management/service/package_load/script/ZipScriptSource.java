package org.oagi.score.gateway.http.api.release_management.service.package_load.script;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads the information file and the scripts directly from a zip archive, without extracting it.
 * The archive must contain exactly one 'mig_info_*.json' and every script that file refers to.
 */
public class ZipScriptSource implements ScriptSource, Closeable {

    private final ZipFile zipFile;

    /** The most entries a package may have, and the most bytes its information file may have once it is unpacked. */
    static final int MAX_ENTRIES = 10_000;
    public static final long MAX_INFO_BYTES = 16L << 20;

    public ZipScriptSource(String path) throws IOException {
        requireZipSignature(Path.of(path));
        this.zipFile = new ZipFile(path);
        try {
            requireSafeEntries();
        } catch (IOException | RuntimeException e) {
            zipFile.close();
            throw e;
        }
    }

    /** A zip archive begins with the signature of its first entry; whatever else begins differently is not read as one. */
    private static void requireZipSignature(Path file) throws IOException {
        byte[] head = new byte[4];
        try (RandomAccessFile in = new RandomAccessFile(file.toFile(), "r")) {
            in.readFully(head);
        } catch (java.io.EOFException e) {
            throw new IOException("The file is not a zip archive", e);
        }
        if (head[0] != 'P' || head[1] != 'K' || head[2] != 3 || head[3] != 4) {
            throw new IOException("The file is not a zip archive");
        }
    }

    /** Nothing is ever unpacked to the disk; still, an entry that names a place outside, or too many entries, make the package not one. */
    private void requireSafeEntries() throws IOException {
        int count = 0;
        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
            String name = entries.nextElement().getName();
            if (++count > MAX_ENTRIES) {
                throw new IOException("The package has too many entries");
            }
            if (name.isEmpty() || name.startsWith("/") || name.indexOf('\\') >= 0 || name.indexOf('\0') >= 0
                    || name.matches("^[A-Za-z]:.*") || java.util.Arrays.asList(name.split("/")).contains("..")) {
                throw new IOException("The package has an entry with an unsafe name");
            }
        }
    }

    public ZipEntry findInfoEntry() throws IOException {
        List<ZipEntry> found = new ArrayList<>();
        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = entries.nextElement();
            String name = fileName(entry.getName());
            if (!entry.isDirectory() && name.startsWith("mig_info_") && name.endsWith(".json")) {
                found.add(entry);
            }
        }
        if (found.isEmpty()) {
            throw new FileNotFoundException("No 'mig_info_*.json' found in the package");
        }
        if (found.size() > 1) {
            throw new IllegalStateException("More than one 'mig_info_*.json' found in the package: " + found);
        }
        return found.get(0);
    }

    public InputStream open(ZipEntry entry) throws IOException {
        return zipFile.getInputStream(entry);
    }

    /** The information file, which is read for no more than it may have, whatever the archive says of its size. */
    public InputStream openInfo(ZipEntry entry) throws IOException {
        if (entry.getSize() > MAX_INFO_BYTES) {
            throw new IOException("The information file is too large");
        }
        InputStream in = zipFile.getInputStream(entry);
        return new java.io.FilterInputStream(in) {
            private long left = MAX_INFO_BYTES;

            @Override
            public int read() throws IOException {
                int b = super.read();
                if (b >= 0 && --left < 0) {
                    throw new IOException("The information file is too large");
                }
                return b;
            }

            @Override
            public int read(byte[] buf, int off, int len) throws IOException {
                int n = super.read(buf, off, len);
                if (n > 0 && (left -= n) < 0) {
                    throw new IOException("The information file is too large");
                }
                return n;
            }
        };
    }

    @Override
    public boolean exists(String script) {
        return find(script) != null;
    }

    @Override
    public InputStream open(String script) throws IOException {
        ZipEntry entry = find(script);
        if (entry == null) {
            throw new FileNotFoundException("'" + script + "' not found in the package");
        }
        return zipFile.getInputStream(entry);
    }

    /**
     * An exact entry name wins; otherwise the entry with the same file name, as long as it is unique.
     */
    private ZipEntry find(String script) {
        ZipEntry entry = zipFile.getEntry(script);
        if (entry != null && !entry.isDirectory()) {
            return entry;
        }
        String name = fileName(script);
        ZipEntry match = null;
        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
            ZipEntry candidate = entries.nextElement();
            if (!candidate.isDirectory() && fileName(candidate.getName()).equals(name)) {
                if (match != null) {
                    return null;
                }
                match = candidate;
            }
        }
        return match;
    }

    private static String fileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

}
