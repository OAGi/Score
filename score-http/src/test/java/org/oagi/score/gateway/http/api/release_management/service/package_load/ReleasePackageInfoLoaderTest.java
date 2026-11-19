package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ReleasePackageInfoLoaderTest {

    private static Info load(String json) throws Exception {
        return ReleasePackageInfoLoader.loadFromJson(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    private static String file(String version, String extra, String tableExtra, String maxId) {
        return """
                {
                  "version": "%s",
                  "maintainer": "oagis",
                  "description": "Add 10.13.3 release",
                  %s
                  "migration": {
                    "release": {"id": 77, "release_num": "10.13.3"},
                    "processes": [
                      {"order": 2, "executable": {"script": "mig_10.13.3.sql"}},
                      {"order": 1, "validation": {"query": "SELECT 1"}, "executable": {"script": "pre.sql", "description": "first"}}
                    ],
                    "metadata": {
                      "releases": [{"id": 1, "release_num": "10.6"}, {"id": 2, "release_num": "Working"}],
                      "tables": [{"table_name": "acc", "max_id": %s %s}]
                    }
                  }
                }
                """.formatted(version, extra, maxId, tableExtra);
    }

    @Test
    void readsWhatTheLoaderNeeds() throws Exception {
        Info info = load(file("1.1", "", "", "8361"));

        assertEquals("1.1", info.getVersion());
        assertEquals("oagis", info.getMaintainer());
        assertEquals(77, info.getMigration().getRelease().getId());
        assertEquals("10.13.3", info.getMigration().getRelease().getReleaseNum());
        assertEquals(2, info.getMigration().getProcesses().size());
        assertEquals("pre.sql", info.getMigration().getProcesses().get(1).getScript());
        assertEquals("SELECT 1", info.getMigration().getProcesses().get(1).getValidationQuery());
        assertEquals("first", info.getMigration().getProcesses().get(1).getDescription());
        assertTrue(info.getMigration().getEntrypoints().isEmpty());
        assertTrue(info.getMigration().getMetadata().hasRelease(2));
        assertFalse(info.getMigration().getMetadata().hasRelease(3));
        assertEquals(8361, info.getMigration().getMetadata().getTable("ACC").getMaxId());
    }

    @Test
    void theValidationAndTheDescriptionAreReadBesideTheExecutableToo() throws Exception {
        Info info = load("""
                {"version": "1.1", "migration": {
                  "release": {"id": 1, "release_num": "1"},
                  "entrypoints": [{"order": 1, "validation": {"query": "SELECT 2"}, "description": "beside",
                                   "executable": {"script": "e.sql"}}],
                  "processes": [],
                  "metadata": {"releases": [], "tables": []}}}
                """);

        assertEquals("SELECT 2", info.getMigration().getEntrypoints().get(0).getValidationQuery());
        assertEquals("beside", info.getMigration().getEntrypoints().get(0).getDescription());
    }

    @Test
    void keysOfANewerFormatAreLeftAlone() throws Exception {
        Info info = load(file("1.2",
                "\"schema_version\": \"3.6.1\", \"app_version\": \"3.6.2-dev\", \"schema_fingerprint\": \"abc\",",
                ", \"columns\": [{\"name\": \"acc_id\", \"data_type\": \"bigint\", \"nullable\": false, \"has_default\": false}]",
                "8361"));

        assertEquals("1.2", info.getVersion());
        assertEquals(8361, info.getMigration().getMetadata().getTable("acc").getMaxId());
    }

    @Test
    void aTableWithNoRecordHasMaximumZero() throws Exception {
        assertEquals(0, load(file("1.1", "", "", "null")).getMigration().getMetadata().getTable("acc").getMaxId());
    }

    @Test
    void whichVersionsOfTheFormatAreLoadedIsNotForTheReaderOfTheFileToSay() throws Exception {
        // The file of an old or a new format is read all the same: the format that handles its version says whether it is loaded.
        assertEquals("1.0", load(file("1.0", "", "", "1")).getVersion());
        assertEquals("3.2", load(file("3.2", "", "", "1")).getVersion());
    }

    @Test
    void aMissingOrMalformedVersionIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> load("{\"migration\": {}}"));
        assertThrows(IllegalArgumentException.class, () -> load(file("one", "", "", "1")));
    }

    @Test
    void aFileThatIsNotCompleteIsRefusedWithTheReason() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> load("{\"version\": \"1.1\"}"));
        assertTrue(e.getMessage().startsWith("The information file is not complete"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> load("[1, 2]"));
        assertThrows(IllegalArgumentException.class, () -> load("not json"));
    }
}
