package org.oagi.score.gateway.http.api.release_management.service;

import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.release_management.model.MigrationInfo;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.util.Zip;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.RELEASE;

public class MigrationScriptGenerator {

    private DSLContext dslContext;

    private ResourceLoader resourceLoader;

    private BigInteger delta;

    private int defaultMaximumRowsInStatement = 1000;

    public MigrationScriptGenerator(DSLContext dslContext, ResourceLoader resourceLoader, BigInteger delta) {
        this.dslContext = dslContext;
        this.resourceLoader = resourceLoader;
        this.delta = delta;
    }

    public File generate(ScoreUser user, ReleaseSummaryRecord release) throws IOException {
        File baseDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        FileUtils.forceMkdir(baseDirectory);

        File scriptFile = generateScriptFile(baseDirectory, user, release);
        File infoFile = generateInfoFile(scriptFile, user, release);
        try {
            return Zip.compression(Arrays.asList(scriptFile, infoFile), UUID.randomUUID().toString());
        } finally {
            FileUtils.deleteDirectory(baseDirectory);
        }
    }

    private File generateScriptFile(File baseDirectory, ScoreUser user, ReleaseSummaryRecord release) throws IOException {
        File scriptFile = new File(baseDirectory, "mig_" + release.releaseNum() + ".sql");

        try (PrintWriter writer = new PrintWriter(new FileWriter(scriptFile))) {
            writeBeginHeaders(writer, user, release);
            writeData(writer, release);
            writeEndHeaders(writer, release);

            writer.flush();
        }

        return scriptFile;
    }

    private void writeBeginHeaders(PrintWriter writer, ScoreUser user, ReleaseSummaryRecord release) {
        String headerLine = "----------------------------------------------------";
        writer.println("-- " + headerLine);
        writer.println("-- Migration script for " + release.releaseNum());
        writer.println("-- ");
        writer.println("-- Author: " + user.username());
        writer.println("-- " + headerLine);
        writer.println("");
        writer.println("SET FOREIGN_KEY_CHECKS = 0;");
        writer.println("");
        writer.println("-- Clean developer data --");
        writer.println("");
        writer.println("DELETE FROM `acc` WHERE `acc_id` < " + delta + ";");
        writer.println("DELETE FROM `acc_manifest` WHERE `acc_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `ascc` WHERE `ascc_id` < " + delta + ";");
        writer.println("DELETE FROM `ascc_manifest` WHERE `ascc_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `bcc` WHERE `bcc_id` < " + delta + ";");
        writer.println("DELETE FROM `bcc_manifest` WHERE `bcc_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `asccp` WHERE `asccp_id` < " + delta + ";");
        writer.println("DELETE FROM `asccp_manifest` WHERE `asccp_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `bccp` WHERE `bccp_id` < " + delta + ";");
        writer.println("DELETE FROM `bccp_manifest` WHERE `bccp_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `app_user` WHERE `app_user_id` >= 2 AND `app_user_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `agency_id_list` WHERE `agency_id_list_id` < " + delta + ";");
        writer.println("DELETE FROM `agency_id_list_manifest` WHERE `agency_id_list_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `agency_id_list_value` WHERE `agency_id_list_value_id` < " + delta + ";");
        writer.println("DELETE FROM `agency_id_list_value_manifest` WHERE `agency_id_list_value_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `code_list` WHERE `code_list_id` < " + delta + ";");
        writer.println("DELETE FROM `code_list_manifest` WHERE `code_list_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `code_list_value` WHERE `code_list_value_id` < " + delta + ";");
        writer.println("DELETE FROM `code_list_value_manifest` WHERE `code_list_value_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `dt` WHERE `dt_id` < " + delta + ";");
        writer.println("DELETE FROM `dt_manifest` WHERE `dt_manifest_id` < " + delta + ";");
        writer.println("DELETE FROM `dt_awd_pri` WHERE `dt_awd_pri_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `dt_sc` WHERE `dt_sc_id` < " + delta + ";");
        writer.println("DELETE FROM `dt_sc_manifest` WHERE `dt_sc_manifest_id`< " + delta + ";");
        writer.println("DELETE FROM `dt_sc_awd_pri` WHERE `dt_sc_awd_pri_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `log` WHERE `log_id` < " + delta + ";");
        writer.println("DELETE FROM `seq_key` WHERE `seq_key_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `namespace` WHERE `namespace_id` < " + delta + ";");
        writer.println("DELETE FROM `release` WHERE `release_id` < " + delta + ";");
        writer.println("DELETE FROM `release_dep` WHERE `release_id` < " + delta + ";");
        writer.println("DELETE FROM `library` WHERE `library_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `xbt` WHERE `xbt_id` < " + delta + ";");
        writer.println("DELETE FROM `xbt_manifest` WHERE `xbt_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `acc_manifest_tag` WHERE `acc_manifest_id` < " + delta + ";");
        writer.println("DELETE FROM `asccp_manifest_tag` WHERE `asccp_manifest_id` < " + delta + ";");
        writer.println("DELETE FROM `bccp_manifest_tag` WHERE `bccp_manifest_id` < " + delta + ";");
        writer.println("DELETE FROM `dt_manifest_tag` WHERE `dt_manifest_id` < " + delta + ";");
        writer.println("");
        writer.println("DROP TABLE IF EXISTS `module`;");
        writer.println("DROP TABLE IF EXISTS `module_acc_manifest`;");
        writer.println("DROP TABLE IF EXISTS `module_agency_id_list_manifest`;");
        writer.println("DROP TABLE IF EXISTS `module_asccp_manifest`;");
        writer.println("DROP TABLE IF EXISTS `module_bccp_manifest`;");
        writer.println("DROP TABLE IF EXISTS `module_blob_content_manifest`;");
        writer.println("DROP TABLE IF EXISTS `module_code_list_manifest`;");
        writer.println("DROP TABLE IF EXISTS `module_dt_manifest`;");
        writer.println("DROP TABLE IF EXISTS `module_set`;");
        writer.println("DROP TABLE IF EXISTS `module_set_release`;");
        writer.println("DROP TABLE IF EXISTS `module_xbt_manifest`;");
        writer.println("");
        writer.println("-- End of 'Clean developer data' --");
        writer.println("");
    }

    private void writeData(PrintWriter writer, ReleaseSummaryRecord release) throws IOException {
        writer.println("-- Add developer data --");
        writer.println("");
        writer.println("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;");
        writer.println("/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;");
        writer.println("/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;");
        writer.println("/*!50503 SET NAMES utf8 */;");
        writer.println("/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;");
        writer.println("/*!40103 SET TIME_ZONE='+00:00' */;");
        writer.println("/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;");
        writer.println("/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;");
        writer.println("/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");
        writer.println("/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;");
        writer.println("");

        dumpData(writer, "acc", false);
        dumpData(writer, "acc_manifest", false);
        dumpData(writer, "agency_id_list", false);
        dumpData(writer, "agency_id_list_manifest", false);
        dumpData(writer, "agency_id_list_value", false);
        dumpData(writer, "agency_id_list_value_manifest", false);
        dumpData(writer, "app_user", false,
                () -> dslContext.resultQuery("SELECT `app_user`.* FROM `app_user` " +
                        "WHERE `app_user`.`app_user_id` < " + delta + " AND `app_user`.`app_user_id` > 1"));
        dumpData(writer, "ascc", false);
        dumpData(writer, "ascc_manifest", false);
        dumpData(writer, "asccp", false);
        dumpData(writer, "asccp_manifest", false);
        dumpData(writer, "bcc", false);
        dumpData(writer, "bcc_manifest", false);
        dumpData(writer, "bccp", false);
        dumpData(writer, "bccp_manifest", false);
        dumpData(writer, "blob_content", true);
        dumpData(writer, "blob_content_manifest", true);
        dumpData(writer, "cdt_pri", true);
        dumpData(writer, "code_list", false);
        dumpData(writer, "code_list_manifest", false);
        dumpData(writer, "code_list_value", false);
        dumpData(writer, "code_list_value_manifest", false);
        dumpData(writer, "dt", false);
        dumpData(writer, "dt_manifest", false);
        dumpData(writer, "dt_awd_pri", false);
        dumpData(writer, "dt_sc", false);
        dumpData(writer, "dt_sc_manifest", false);
        dumpData(writer, "dt_sc_awd_pri", false);
        dumpData(writer, "library", false);
        dumpData(writer, "log", false, null, 50);
        dumpData(writer, "module", true);
        dumpData(writer, "module_acc_manifest", true);
        dumpData(writer, "module_agency_id_list_manifest", true);
        dumpData(writer, "module_asccp_manifest", true);
        dumpData(writer, "module_bccp_manifest", true);
        dumpData(writer, "module_blob_content_manifest", true);
        dumpData(writer, "module_code_list_manifest", true);
        dumpData(writer, "module_dt_manifest", true);
        dumpData(writer, "module_set", true);
        dumpData(writer, "module_set_release", true);
        dumpData(writer, "module_xbt_manifest", true);
        dumpData(writer, "namespace");
        dumpData(writer, "release");
        dumpData(writer, "release_dep");
        dumpData(writer, "seq_key", false);
        dumpData(writer, "xbt", true);
        dumpData(writer, "xbt_manifest", true);
        dumpData(writer, "acc_manifest_tag", false, () -> dslContext.resultQuery(
                "SELECT `acc_manifest_tag`.* FROM `acc_manifest_tag` WHERE `acc_manifest_id` < " + delta));
        dumpData(writer, "asccp_manifest_tag", false, () -> dslContext.resultQuery(
                "SELECT `asccp_manifest_tag`.* FROM `asccp_manifest_tag` WHERE `asccp_manifest_id` < " + delta));
        dumpData(writer, "bccp_manifest_tag", false, () -> dslContext.resultQuery(
                "SELECT `bccp_manifest_tag`.* FROM `bccp_manifest_tag` WHERE `bccp_manifest_id` < " + delta));
        dumpData(writer, "dt_manifest_tag", false, () -> dslContext.resultQuery(
                "SELECT `dt_manifest_tag`.* FROM `dt_manifest_tag` WHERE `dt_manifest_id` < " + delta));

        writer.println("/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;");
        writer.println("");
        writer.println("/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;");
        writer.println("/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;");
        writer.println("/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;");
        writer.println("/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");
        writer.println("/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;");
        writer.println("/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;");
        writer.println("/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;");
        writer.println("");
        writer.println("-- End of 'Add developer data' --");
        writer.println("");
    }

    private void dumpData(PrintWriter writer, String tableName) throws IOException {
        dumpData(writer, tableName, false, null, defaultMaximumRowsInStatement);
    }

    private void dumpData(PrintWriter writer, String tableName, boolean includeTableStructure) throws IOException {
        dumpData(writer, tableName, includeTableStructure, null, defaultMaximumRowsInStatement);
    }

    private void dumpData(PrintWriter writer, String tableName, boolean includeTableStructure,
                          Supplier<ResultQuery<Record>> resultQuerySupplier) throws IOException {
        dumpData(writer, tableName, includeTableStructure, resultQuerySupplier, defaultMaximumRowsInStatement);
    }

    private void dumpData(PrintWriter writer, String tableName, boolean includeTableStructure,
                          Supplier<ResultQuery<Record>> resultQuerySupplier, int maximumRowsInStatement) throws IOException {

        if (resultQuerySupplier == null) {
            resultQuerySupplier = () -> dslContext.resultQuery("SELECT `" + tableName + "`.* FROM `" + tableName +
                    "` WHERE `" + tableName + "_id` < " + delta);
        }

        if (includeTableStructure) {
            writer.println("--");
            writer.println("-- Table structure for table `" + tableName + "`");
            writer.println("--");
            writer.println("");
            writer.println("DROP TABLE IF EXISTS `" + tableName + "`;");
            writer.println("/*!40101 SET @saved_cs_client     = @@character_set_client */;");
            writer.println("/*!50503 SET character_set_client = utf8mb4 */;");
            for (String ddl : getDdl(tableName)) {
                writer.println(ddl);
            }
            writer.println("/*!40101 SET character_set_client = @saved_cs_client */;");
            writer.println("");
        }

        writer.println("--");
        writer.println("-- Dumping data for table `" + tableName + "`");
        writer.println("--");
        writer.println("");
        writer.println("LOCK TABLES `" + tableName + "` WRITE;");
        writer.println("/*!40000 ALTER TABLE `" + tableName + "` DISABLE KEYS */;");

        List<org.jooq.Record> columns = dslContext.resultQuery(
                "SELECT `column_name`, `data_type` FROM `information_schema`.`columns` " +
                        "WHERE `table_name` = '" + tableName + "'").fetch();

        String columnsStr = columns.stream()
                .map(e -> "`" + e.get("column_name", String.class) + "`")
                .collect(Collectors.joining(","));

        List<String> values = new ArrayList<>();

        resultQuerySupplier.get().fetchStream().forEach(record -> {
            List<String> list = new ArrayList<>();
            for (org.jooq.Record column : columns) {
                Object val = record.get(column.get("column_name", String.class));
                String dataType = column.get("data_type", String.class);
                list.add(toString(val, dataType));
            }

            values.add("(" + list.stream().collect(Collectors.joining(",")) + ")");
            if (values.size() == maximumRowsInStatement) {
                writer.println("INSERT INTO `" + tableName + "` (" + columnsStr + ") VALUES " +
                        values.stream().collect(Collectors.joining(",")) + ";");
                values.clear();
            }
        });
        if (!values.isEmpty()) {
            writer.println("INSERT INTO `" + tableName + "` (" + columnsStr + ") VALUES " +
                    values.stream().collect(Collectors.joining(",")) + ";");
            values.clear();
        }

        writer.println("/*!40000 ALTER TABLE `" + tableName + "` ENABLE KEYS */;");
        writer.println("UNLOCK TABLES;");
        writer.println("");
    }

    private String toString(Object val, String dataType) {
        if (val == null) {
            return "NULL";
        }

        switch (dataType) {
            case "tinyint":
                return "true".equals(val.toString()) ? "1" : "0";
            case "char":
            case "varchar":
            case "tinytext":
            case "text":
            case "longtext":
                return "'" + wrap(val.toString()) + "'";
            case "json":
                return "'" + wrapForJson(val.toString()) + "'";
            case "datetime":
                return "'" + val + "'";
            case "mediumblob":
                byte[] bytes = (byte[]) val;
                return "'" + wrap(new String(bytes)) + "'";
        }
        return val.toString();
    }

    private String wrap(String str) {
        return str.replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\\n", "\\\\n")
                .replaceAll("\\r", "\\\\r")
                .replaceAll("'", "\\\\'")
                .replaceAll("\"", "\\\\\"");
    }

    private String wrapForJson(String str) {
        return str.replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("'", "\\\\'")
                .replaceAll("\"", "\\\\\"");
    }

    private void writeEndHeaders(PrintWriter writer, ReleaseSummaryRecord release) {
        BigInteger deltaPlusOne = delta.add(BigInteger.ONE);
        writer.println("-- Post processes --");
        writer.println("");
        writer.println("ALTER TABLE `acc` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `acc_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `agency_id_list` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `agency_id_list_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `agency_id_list_value` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `agency_id_list_value_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `app_user` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `ascc` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `ascc_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `bcc` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `bcc_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `asccp` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `asccp_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `bccp` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `bccp_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list_value` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list_value_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_awd_pri` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_sc` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_sc_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_sc_awd_pri` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `library` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `log` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `namespace` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `seq_key` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `release` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `release_dep` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("");
        writer.println("-- End of 'Post processes' --");
        writer.println("");
    }

    private File generateInfoFile(File scriptFile, ScoreUser user, ReleaseSummaryRecord release) throws IOException {
        MigrationInfo migrationInfo = new MigrationInfo();
        migrationInfo.setVersion("1.1");
        migrationInfo.setMaintainer(user.username());
        migrationInfo.setDescription("Add " + release.releaseNum() + " release");
        MigrationInfo.Migration migration = migrationInfo.getMigration();

        migration.setRelease(release.releaseId(), release.releaseNum());
        MigrationInfo.Executable executable = new MigrationInfo.Executable();
        executable.setOrder(1);
        executable.setScript(scriptFile.getName());
        migration.setProcesses(Arrays.asList(executable));
        MigrationInfo.Metadata metadata = new MigrationInfo.Metadata();
        migration.setMetadata(metadata);

        metadata.setReleases(dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.notEqual(ULong.valueOf(release.releaseId().value())))
                .orderBy(RELEASE.RELEASE_ID)
                .fetchStream().map(record -> {
                    MigrationInfo.Release rel = new MigrationInfo.Release();
                    rel.setId(new ReleaseId(record.getReleaseId().toBigInteger()));
                    rel.setReleaseNum(record.getReleaseNum());
                    return rel;
                })
                .collect(Collectors.toList()));

        List<String> tableNames = Arrays.asList("acc", "acc_manifest", "ascc", "ascc_manifest", "bcc", "bcc_manifest",
                "asccp", "asccp_manifest",
                "bccp", "bccp_manifest", "dt", "dt_manifest", "dt_awd_pri", "dt_sc", "dt_sc_manifest", "dt_sc_awd_pri",
                "code_list", "code_list_manifest", "code_list_value", "code_list_value_manifest",
                "agency_id_list", "agency_id_list_manifest", "agency_id_list_value", "agency_id_list_value_manifest", "log",
                "seq_key", "namespace", "library", "release_dep");
        metadata.setTables(tableNames.stream().map(tableName -> {
            BigInteger maxId = dslContext.resultQuery("SELECT max(`" + tableName + "_id`) FROM `" + tableName + "`")
                    .fetchOneInto(BigInteger.class);
            MigrationInfo.Table table = new MigrationInfo.Table();
            table.setTableName(tableName);
            table.setMaxId(maxId);
            return table;
        }).collect(Collectors.toList()));

        File infoFile = new File(scriptFile.getParentFile(), "mig_info_" + release.releaseNum() + ".json");
        migrationInfo.write(infoFile);
        return infoFile;
    }

    private List<String> getDdl(String tableName) throws IOException {
        return FileUtils.readLines(
                this.resourceLoader.getResource("classpath:schemas/" + tableName + ".ddl").getFile(),
                StandardCharsets.UTF_8);
    }

}
