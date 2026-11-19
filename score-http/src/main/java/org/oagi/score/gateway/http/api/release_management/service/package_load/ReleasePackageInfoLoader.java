package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.package_info.Executable;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Metadata;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Migration;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Release;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Table;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the information file (mig_info_*.json) of a release package. The file is read by key, so keys it does not know of
 * (those a newer format adds) are left alone. Which versions of the format are loaded is not for it to say: that is for the format
 * the version of the package is handled by (see ReleasePackageFormats).
 */
public final class ReleasePackageInfoLoader {

    private ReleasePackageInfoLoader() {
    }

    /**
     * @throws IllegalArgumentException when the content is not an information file, or is of a format that is not loaded
     */
    public static Info loadFromJson(InputStream inputStream) throws IOException {
        JsonObject json;
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            throw new IllegalArgumentException("The information file is not valid JSON object: " + e.getMessage(), e);
        }
        try {
            return load(json);
        } catch (JsonParseException | IllegalStateException | NullPointerException | ClassCastException e) {
            throw new IllegalArgumentException("The information file is not complete: " + e.getMessage(), e);
        }
    }

    private static Info load(JsonObject json) {
        String version = string(json, "version");
        if (!StringUtils.hasLength(version)) {
            throw new IllegalArgumentException("No found 'version' property. Please check the information definition statements.");
        }
        if (!version.matches("\\d+(\\.\\d+){1,2}")) {
            throw new IllegalArgumentException("Unknown 'version' property: " + version);
        }

        Info info = new Info();
        info.setVersion(version);
        info.setDescription(string(json, "description"));
        info.setMaintainer(string(json, "maintainer"));
        info.setSchemaVersion(string(json, "schema_version"));

        Migration migration = new Migration();
        info.setMigration(migration);
        JsonObject migrationObj = json.getAsJsonObject("migration");

        JsonObject releaseObj = migrationObj.getAsJsonObject("release");
        Release release = new Release();
        release.setId(releaseObj.get("id").getAsLong());
        release.setReleaseNum(releaseObj.get("release_num").getAsString());
        migration.setRelease(release);

        if (migrationObj.has("entrypoints")) {
            migration.setEntrypoints(executables(migrationObj.getAsJsonArray("entrypoints")));
        }
        migration.setProcesses(executables(migrationObj.getAsJsonArray("processes")));

        JsonObject metadataObj = migrationObj.getAsJsonObject("metadata");
        Metadata metadata = new Metadata();
        migration.setMetadata(metadata);

        List<Release> releases = new ArrayList<>();
        for (JsonElement element : metadataObj.getAsJsonArray("releases")) {
            JsonObject obj = element.getAsJsonObject();
            Release other = new Release();
            other.setId(obj.get("id").getAsLong());
            other.setReleaseNum(obj.get("release_num").getAsString());
            releases.add(other);
        }
        metadata.setReleases(releases);

        List<Table> tables = new ArrayList<>();
        for (JsonElement element : metadataObj.getAsJsonArray("tables")) {
            JsonObject obj = element.getAsJsonObject();
            Table table = new Table();
            table.setTableName(obj.get("table_name").getAsString());
            // A table with no record has no maximum: the dump writes null.
            table.setMaxId(obj.get("max_id").isJsonNull() ? 0L : obj.get("max_id").getAsLong());
            tables.add(table);
        }
        metadata.setTables(tables);

        return info;
    }

    private static List<Executable> executables(JsonArray array) {
        List<Executable> executables = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            JsonObject exe = obj.getAsJsonObject("executable");
            Executable executable = new Executable();
            executable.setOrder(obj.get("order").getAsInt());
            // The generator writes the validation and the description inside 'executable'; the loader that came before
            // read them beside it. Both are read, the one inside first.
            JsonObject validation = exe.has("validation") ? exe.getAsJsonObject("validation")
                    : obj.has("validation") ? obj.getAsJsonObject("validation") : null;
            if (validation != null) {
                executable.setValidationQuery(validation.get("query").getAsString());
            }
            executable.setScript(exe.get("script").getAsString());
            String description = string(exe, "description");
            executable.setDescription(description != null ? description : string(obj, "description"));
            executables.add(executable);
        }
        return executables;
    }

    private static String string(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return (element == null || element.isJsonNull()) ? null : element.getAsString();
    }
}
