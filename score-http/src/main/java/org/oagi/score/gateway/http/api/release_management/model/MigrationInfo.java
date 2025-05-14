package org.oagi.score.gateway.http.api.release_management.model;

import com.google.gson.stream.JsonWriter;
import lombok.Data;
import org.oagi.score.gateway.http.common.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Data
public class MigrationInfo {

    private String version = "1.0";

    private String maintainer;

    private String description;

    private Migration migration = new Migration();

    @Data
    public static class Migration {

        private Release release = new Release();

        private List<Executable> processes = Collections.emptyList();

        private Metadata metadata = new Metadata();

        public Migration setRelease(ReleaseId releaseId, String releaseNum) {
            this.release.id = releaseId;
            this.release.releaseNum = releaseNum;
            return this;
        }

    }

    @Data
    public static class Release {

        private ReleaseId id;

        private String releaseNum;

    }

    @Data
    public static class Executable {

        private int order;
        private String validationQuery;
        private String script;
        private String description;

    }

    @Data
    public static class Metadata {

        private List<Release> releases = Collections.emptyList();

        private List<Table> tables = Collections.emptyList();

    }

    @Data
    public static class Table {

        private String tableName;

        private BigInteger maxId;

    }

    public void write(File file) throws IOException {
        try (JsonWriter writer = new JsonWriter(new FileWriter(file))) {
            writer.setIndent("  ");
            writer.setLenient(true);

            writer.beginObject();
            {
                writer.name("version").value(this.version)
                        .name("maintainer").value(this.maintainer)
                        .name("description").value(this.description);

                writer.name("migration").beginObject();
                {
                    writer.name("release").beginObject()
                            .name("id").value(this.migration.release.id.value())
                            .name("release_num").value(this.migration.release.releaseNum)
                            .endObject();

                    writer.name("processes").beginArray();
                    {
                        for (Executable executable : this.migration.processes) {
                            writer.beginObject()
                                    .name("order").value(executable.order)
                                    .name("executable").beginObject();
                            {
                                if (StringUtils.hasLength(executable.getValidationQuery())) {
                                    writer.name("validation").beginObject()
                                            .name("query").value(executable.getValidationQuery())
                                            .endObject();
                                }
                                if (StringUtils.hasLength(executable.getScript())) {
                                    writer.name("script").value(executable.getScript());
                                }
                                if (StringUtils.hasLength(executable.getDescription())) {
                                    writer.name("description").value(executable.getDescription());
                                }
                            }
                            writer.endObject().endObject();
                        }
                    }
                    writer.endArray();

                    writer.name("metadata").beginObject();
                    {
                        writer.name("releases").beginArray();
                        {
                            for (Release release : this.migration.metadata.releases) {
                                writer.beginObject()
                                        .name("id").value(release.id.value())
                                        .name("release_num").value(release.releaseNum)
                                        .endObject();
                            }
                        }
                        writer.endArray();

                        writer.name("tables").beginArray();
                        {
                            for (Table table : this.migration.metadata.tables) {
                                writer.beginObject()
                                        .name("table_name").value(table.tableName)
                                        .name("max_id").value(table.maxId)
                                        .endObject();
                            }
                        }
                        writer.endArray();
                    }
                    writer.endObject();
                }
                writer.endObject();
            }
            writer.endObject();

            writer.flush();
        }
    }
}
