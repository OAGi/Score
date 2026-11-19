package org.oagi.score.gateway.http.api.release_management.dataload.script;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    public ZipScriptSource(String path) throws IOException {
        this.zipFile = new ZipFile(path);
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
            throw new FileNotFoundException("No 'mig_info_*.json' found in " + zipFile.getName());
        }
        if (found.size() > 1) {
            throw new IllegalStateException("More than one 'mig_info_*.json' found in " + zipFile.getName() + ": " + found);
        }
        return found.get(0);
    }

    public InputStream open(ZipEntry entry) throws IOException {
        return zipFile.getInputStream(entry);
    }

    @Override
    public boolean exists(String script) {
        return find(script) != null;
    }

    @Override
    public InputStream open(String script) throws IOException {
        ZipEntry entry = find(script);
        if (entry == null) {
            throw new FileNotFoundException("'" + script + "' not found in " + zipFile.getName());
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
