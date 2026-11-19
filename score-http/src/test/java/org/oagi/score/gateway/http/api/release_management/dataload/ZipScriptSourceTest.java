package org.oagi.score.gateway.http.api.release_management.dataload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.oagi.score.gateway.http.api.release_management.dataload.script.ZipScriptSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ZipScriptSourceTest {

    @TempDir
    File dir;

    private String zip(Map<String, String> entries) throws Exception {
        File file = new File(dir, "package.zip");
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return file.getPath();
    }

    @Test
    void findsTheInformationFileAndReadsAScriptByItsName() throws Exception {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("mig_10.13.3.sql", "SELECT 1;");
        entries.put("mig_info_10.13.3.json", "{}");
        try (ZipScriptSource source = new ZipScriptSource(zip(entries))) {
            assertEquals("mig_info_10.13.3.json", source.findInfoEntry().getName());
            assertTrue(source.exists("mig_10.13.3.sql"));
            assertFalse(source.exists("other.sql"));
            assertEquals("SELECT 1;", new String(source.open("mig_10.13.3.sql").readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void aScriptInAFolderIsFoundByItsFileNameWhenThatIsUnique() throws Exception {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("a/mig_x.sql", "1");
        entries.put("mig_info_x.json", "{}");
        try (ZipScriptSource source = new ZipScriptSource(zip(entries))) {
            assertTrue(source.exists("mig_x.sql"));
        }
    }

    @Test
    void aFileNameThatIsNotUniqueIsNotFound() throws Exception {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("a/mig_x.sql", "1");
        entries.put("b/mig_x.sql", "2");
        entries.put("mig_info_x.json", "{}");
        try (ZipScriptSource source = new ZipScriptSource(zip(entries))) {
            assertFalse(source.exists("mig_x.sql"));
            assertThrows(FileNotFoundException.class, () -> source.open("mig_x.sql"));
        }
    }

    @Test
    void anArchiveNeedsExactlyOneInformationFile() throws Exception {
        try (ZipScriptSource none = new ZipScriptSource(zip(Map.of("mig_x.sql", "1")))) {
            assertThrows(FileNotFoundException.class, none::findInfoEntry);
        }
        Map<String, String> two = new LinkedHashMap<>();
        two.put("mig_info_a.json", "{}");
        two.put("mig_info_b.json", "{}");
        try (ZipScriptSource source = new ZipScriptSource(zip(two))) {
            assertThrows(IllegalStateException.class, source::findInfoEntry);
        }
    }
}
