package org.oagi.score.gateway.http.api.release_management.service;

import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.types.ULong;
import org.oagi.score.data.Release;
import org.oagi.score.gateway.http.api.release_management.data.MigrationInfo;
import org.oagi.score.gateway.http.helper.Zip;
import org.oagi.score.repo.api.user.model.ScoreUser;
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

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.RELEASE;

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

    public File generate(ScoreUser user, Release release) throws IOException {
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

    private File generateScriptFile(File baseDirectory, ScoreUser user, Release release) throws IOException {
        File scriptFile = new File(baseDirectory, "mig_" + release.getReleaseNum() + ".sql");

        try (PrintWriter writer = new PrintWriter(new FileWriter(scriptFile))) {
            writeBeginHeaders(writer, user, release);
            writeData(writer, release);
            writeEndHeaders(writer, release);

            writer.flush();
        }

        return scriptFile;
    }

    private void writeBeginHeaders(PrintWriter writer, ScoreUser user, Release release) {
        String headerLine = "----------------------------------------------------";
        writer.println("-- " + headerLine);
        writer.println("-- Migration script for " + release.getReleaseNum());
        writer.println("-- ");
        writer.println("-- Author: " + user.getUsername());
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
        writer.println("DELETE `cdt_awd_pri_xps_type_map` FROM `cdt_awd_pri_xps_type_map` JOIN `cdt_awd_pri` " +
                "ON `cdt_awd_pri_xps_type_map`.`cdt_awd_pri_id` = `cdt_awd_pri`.`cdt_awd_pri_id` " +
                "WHERE `cdt_awd_pri`.`cdt_id` < " + delta + ";");
        writer.println("DELETE FROM `cdt_ref_spec` WHERE `cdt_id` < " + delta + ";");
        writer.println("DELETE FROM `cdt_awd_pri` WHERE `cdt_id` < " + delta + ";");
        writer.println("DELETE `cdt_sc_awd_pri_xps_type_map` FROM `cdt_sc_awd_pri_xps_type_map` JOIN `cdt_sc_awd_pri` " +
                "ON `cdt_sc_awd_pri_xps_type_map`.`cdt_sc_awd_pri_id` = `cdt_sc_awd_pri`.`cdt_sc_awd_pri_id` " +
                "WHERE `cdt_sc_awd_pri`.`cdt_sc_id` < " + delta + ";");
        writer.println("DELETE FROM `cdt_sc_ref_spec` WHERE `cdt_sc_id` < " + delta + ";");
        writer.println("DELETE FROM `cdt_sc_awd_pri` WHERE `cdt_sc_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `dt` WHERE `dt_id` < " + delta + ";");
        writer.println("DELETE FROM `dt_manifest` WHERE `dt_manifest_id` < " + delta + ";");
        writer.println("DELETE FROM `bdt_pri_restri` WHERE `bdt_pri_restri_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `dt_sc` WHERE `dt_sc_id` < " + delta + ";");
        writer.println("DELETE FROM `dt_sc_manifest` WHERE `dt_sc_manifest_id`< " + delta + ";");
        writer.println("DELETE FROM `bdt_sc_pri_restri` WHERE `bdt_sc_pri_restri_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `log` WHERE `log_id` < " + delta + ";");
        writer.println("DELETE FROM `seq_key` WHERE `seq_key_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `namespace` WHERE `namespace_id` < " + delta + ";");
        writer.println("DELETE FROM `release` WHERE `release_id` < " + delta + ";");
        writer.println("");
        writer.println("DELETE FROM `xbt` WHERE `xbt_id` < " + delta + ";");
        writer.println("DELETE FROM `xbt_manifest` WHERE `xbt_manifest_id` < " + delta + ";");
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

    private void writeData(PrintWriter writer, Release release) throws IOException {
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

        dumpData(writer, "acc", false,
                () -> dslContext.resultQuery("SELECT `acc`.* FROM `acc` " +
                        "WHERE `acc`.`acc_id` < " + delta + " AND `acc`.`state` = 'Published'"));
        dumpData(writer, "acc_manifest", false,
                () -> dslContext.resultQuery("SELECT `acc_manifest`.* FROM `acc_manifest` " +
                        "JOIN `acc` ON `acc_manifest`.`acc_id` = `acc`.`acc_id` " +
                        "WHERE `acc_manifest`.`acc_manifest_id` < " + delta + " AND `acc`.`state` = 'Published'"));
        dumpData(writer, "agency_id_list", false,
                () -> dslContext.resultQuery("SELECT `agency_id_list`.* FROM `agency_id_list` " +
                        "WHERE `agency_id_list`.`agency_id_list_id` < " + delta + " AND `agency_id_list`.`state` = 'Published'"));
        dumpData(writer, "agency_id_list_manifest", false,
                () -> dslContext.resultQuery("SELECT `agency_id_list_manifest`.* FROM `agency_id_list_manifest` " +
                        "JOIN `agency_id_list` ON `agency_id_list_manifest`.`agency_id_list_id` = `agency_id_list`.`agency_id_list_id` " +
                        "WHERE `agency_id_list_manifest`.`agency_id_list_manifest_id` < " + delta + " AND `agency_id_list`.`state` = 'Published'"));
        dumpData(writer, "agency_id_list_value", false,
                () -> dslContext.resultQuery("SELECT `agency_id_list_value`.* FROM `agency_id_list_value` " +
                        "JOIN `agency_id_list` ON `agency_id_list_value`.`owner_list_id` = `agency_id_list`.`agency_id_list_id` " +
                        "WHERE `agency_id_list_value`.`agency_id_list_value_id` < " + delta + " AND `agency_id_list`.`state` = 'Published'"));
        dumpData(writer, "agency_id_list_value_manifest", false,
                () -> dslContext.resultQuery("SELECT `agency_id_list_value_manifest`.* FROM `agency_id_list_value_manifest` " +
                        "JOIN `agency_id_list_value` ON `agency_id_list_value_manifest`.`agency_id_list_value_id` = `agency_id_list_value`.`agency_id_list_value_id` " +
                        "JOIN `agency_id_list` ON `agency_id_list_value`.`owner_list_id` = `agency_id_list`.`agency_id_list_id` " +
                        "WHERE `agency_id_list_value_manifest`.`agency_id_list_value_manifest_id` < " + delta + " AND `agency_id_list`.`state` = 'Published'"));
        dumpData(writer, "app_user", false,
                () -> dslContext.resultQuery("SELECT `app_user`.* FROM `app_user` " +
                        "WHERE `app_user`.`app_user_id` < " + delta + " AND `app_user`.`app_user_id` > 1"));
        dumpData(writer, "ascc", false,
                () -> dslContext.resultQuery("SELECT `ascc`.* FROM `ascc` " +
                        "WHERE `ascc`.`ascc_id` < " + delta + " AND `ascc`.`state` = 'Published'"));
        dumpData(writer, "ascc_manifest", false,
                () -> dslContext.resultQuery("SELECT `ascc_manifest`.* FROM `ascc_manifest` " +
                        "JOIN `ascc` ON `ascc_manifest`.`ascc_id` = `ascc`.`ascc_id` " +
                        "WHERE `ascc_manifest`.`ascc_manifest_id` < " + delta + " AND `ascc`.`state` = 'Published'"));
        dumpData(writer, "asccp", false,
                () -> dslContext.resultQuery("SELECT `asccp`.* FROM `asccp` " +
                        "WHERE `asccp`.`asccp_id` < " + delta + " AND `asccp`.`state` = 'Published'"));
        dumpData(writer, "asccp_manifest", false,
                () -> dslContext.resultQuery("SELECT `asccp_manifest`.* FROM `asccp_manifest` " +
                        "JOIN `asccp` ON `asccp_manifest`.`asccp_id` = `asccp`.`asccp_id` " +
                        "WHERE `asccp_manifest`.`asccp_manifest_id` < " + delta + " AND `asccp`.`state` = 'Published'"));
        dumpData(writer, "bcc", false,
                () -> dslContext.resultQuery("SELECT `bcc`.* FROM `bcc` " +
                        "WHERE `bcc`.`bcc_id` < " + delta + " AND `bcc`.`state` = 'Published'"));
        dumpData(writer, "bcc_manifest", false,
                () -> dslContext.resultQuery("SELECT `bcc_manifest`.* FROM `bcc_manifest` " +
                        "JOIN `bcc` ON `bcc_manifest`.`bcc_id` = `bcc`.`bcc_id` " +
                        "WHERE `bcc_manifest`.`bcc_manifest_id` < " + delta + " AND `bcc`.`state` = 'Published'"));
        dumpData(writer, "bccp", false,
                () -> dslContext.resultQuery("SELECT `bccp`.* FROM `bccp` " +
                        "WHERE `bccp`.`bccp_id` < " + delta + " AND `bccp`.`state` = 'Published'"));
        dumpData(writer, "bccp_manifest", false,
                () -> dslContext.resultQuery("SELECT `bccp_manifest`.* FROM `bccp_manifest` " +
                        "JOIN `bccp` ON `bccp_manifest`.`bccp_id` = `bccp`.`bccp_id` " +
                        "WHERE `bccp_manifest`.`bccp_manifest_id` < " + delta + " AND `bccp`.`state` = 'Published'"));
        dumpData(writer, "bdt_pri_restri", false,
                () -> dslContext.resultQuery("SELECT `bdt_pri_restri`.* FROM `bdt_pri_restri` " +
                        "JOIN `dt_manifest` ON `bdt_pri_restri`.`bdt_manifest_id` = `dt_manifest`.`dt_manifest_id` " +
                        "JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id` " +
                        "WHERE `bdt_pri_restri`.`bdt_pri_restri_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "bdt_sc_pri_restri", false,
                () -> dslContext.resultQuery("SELECT `bdt_sc_pri_restri`.* FROM `bdt_sc_pri_restri` " +
                        "JOIN `dt_sc_manifest` ON `bdt_sc_pri_restri`.`bdt_sc_manifest_id` = `dt_sc_manifest`.`dt_sc_manifest_id` " +
                        "JOIN `dt_manifest` ON `dt_sc_manifest`.`owner_dt_manifest_id` = `dt_manifest`.`dt_manifest_id` " +
                        "JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id` " +
                        "WHERE `bdt_sc_pri_restri`.`bdt_sc_pri_restri_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "blob_content", true);
        dumpData(writer, "blob_content_manifest", true);
        dumpData(writer, "cdt_awd_pri", false,
                () -> dslContext.resultQuery("SELECT `cdt_awd_pri`.* FROM `cdt_awd_pri` " +
                        "JOIN `dt` ON `cdt_awd_pri`.`cdt_id` = `dt`.`dt_id` " +
                        "WHERE `cdt_awd_pri`.`cdt_awd_pri_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "cdt_awd_pri_xps_type_map", false,
                () -> dslContext.resultQuery("SELECT `cdt_awd_pri_xps_type_map`.* FROM `cdt_awd_pri_xps_type_map` " +
                        "JOIN `cdt_awd_pri` ON `cdt_awd_pri_xps_type_map`.`cdt_awd_pri_id` = `cdt_awd_pri`.`cdt_awd_pri_id` " +
                        "JOIN `dt` ON `cdt_awd_pri`.`cdt_id` = `dt`.`dt_id` " +
                        "WHERE `cdt_awd_pri_xps_type_map`.`cdt_awd_pri_xps_type_map_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "cdt_pri", true);
        dumpData(writer, "cdt_ref_spec", false,
                () -> dslContext.resultQuery("SELECT `cdt_ref_spec`.* FROM `cdt_ref_spec` " +
                        "JOIN `dt` ON `cdt_ref_spec`.`cdt_id` = `dt`.`dt_id` " +
                        "WHERE `cdt_ref_spec`.`cdt_ref_spec_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "cdt_sc_awd_pri", false,
                () -> dslContext.resultQuery("SELECT `cdt_sc_awd_pri`.* FROM `cdt_sc_awd_pri` " +
                        "JOIN `dt_sc` ON `cdt_sc_awd_pri`.`cdt_sc_id` = `dt_sc`.`dt_sc_id` " +
                        "JOIN `dt` ON `dt_sc`.`owner_dt_id` = `dt`.`dt_id` " +
                        "WHERE `cdt_sc_awd_pri`.`cdt_sc_awd_pri_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "cdt_sc_awd_pri_xps_type_map", false,
                () -> dslContext.resultQuery("SELECT `cdt_sc_awd_pri_xps_type_map`.* FROM `cdt_sc_awd_pri_xps_type_map` " +
                        "JOIN `cdt_sc_awd_pri` ON `cdt_sc_awd_pri_xps_type_map`.`cdt_sc_awd_pri_id` = `cdt_sc_awd_pri`.`cdt_sc_awd_pri_id` " +
                        "JOIN `dt_sc` ON `cdt_sc_awd_pri`.`cdt_sc_id` = `dt_sc`.`dt_sc_id` " +
                        "JOIN `dt` ON `dt_sc`.`owner_dt_id` = `dt`.`dt_id` " +
                        "WHERE `cdt_sc_awd_pri_xps_type_map`.`cdt_sc_awd_pri_xps_type_map_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "cdt_sc_ref_spec", false,
                () -> dslContext.resultQuery("SELECT `cdt_sc_ref_spec`.* FROM `cdt_sc_ref_spec` " +
                        "JOIN `dt_sc` ON `cdt_sc_ref_spec`.`cdt_sc_id` = `dt_sc`.`dt_sc_id` " +
                        "JOIN `dt` ON `dt_sc`.`owner_dt_id` = `dt`.`dt_id` " +
                        "WHERE `cdt_sc_ref_spec`.`cdt_sc_ref_spec_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "code_list", false,
                () -> dslContext.resultQuery("SELECT `code_list`.* FROM `code_list` " +
                        "WHERE `code_list`.`code_list_id` < " + delta + " AND `code_list`.`state` = 'Published'"));
        dumpData(writer, "code_list_manifest", false,
                () -> dslContext.resultQuery("SELECT `code_list_manifest`.* FROM `code_list_manifest` " +
                        "JOIN `code_list` ON `code_list_manifest`.`code_list_id` = `code_list`.`code_list_id` " +
                        "WHERE `code_list_manifest`.`code_list_manifest_id` < " + delta + " AND `code_list`.`state` = 'Published'"));
        dumpData(writer, "code_list_value", false,
                () -> dslContext.resultQuery("SELECT `code_list_value`.* FROM `code_list_value` " +
                        "JOIN `code_list` ON `code_list_value`.`code_list_id` = `code_list`.`code_list_id` " +
                        "WHERE `code_list_value`.`code_list_value_id` < " + delta + " AND `code_list`.`state` = 'Published'"));
        dumpData(writer, "code_list_value_manifest", false,
                () -> dslContext.resultQuery("SELECT `code_list_value_manifest`.* FROM `code_list_value_manifest` " +
                        "JOIN `code_list_value` ON `code_list_value_manifest`.`code_list_value_id` = `code_list_value`.`code_list_value_id` " +
                        "JOIN `code_list` ON `code_list_value`.`code_list_id` = `code_list`.`code_list_id` " +
                        "WHERE `code_list_value_manifest`.`code_list_value_manifest_id` < " + delta + " AND `code_list`.`state` = 'Published'"));
        dumpData(writer, "dt", false,
                () -> dslContext.resultQuery("SELECT `dt`.* FROM `dt` " +
                        "WHERE `dt`.`dt_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "dt_manifest", false,
                () -> dslContext.resultQuery("SELECT `dt_manifest`.* FROM `dt_manifest` " +
                        "JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id` " +
                        "WHERE `dt_manifest`.`dt_manifest_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "dt_sc", false,
                () -> dslContext.resultQuery("SELECT `dt_sc`.* FROM `dt_sc` " +
                        "JOIN `dt` ON `dt_sc`.`owner_dt_id` = `dt`.`dt_id` " +
                        "WHERE `dt_sc`.`dt_sc_id` < " + delta + " AND `dt`.`state` = 'Published'"));
        dumpData(writer, "dt_sc_manifest", false,
                () -> dslContext.resultQuery("SELECT `dt_sc_manifest`.* FROM `dt_sc_manifest` " +
                        "JOIN `dt_manifest` ON `dt_sc_manifest`.`owner_dt_manifest_id` = `dt_manifest`.`dt_manifest_id` " +
                        "JOIN `dt` ON `dt_manifest`.`dt_id` = `dt`.`dt_id` " +
                        "WHERE `dt_sc_manifest`.`dt_sc_manifest_id` < " + delta + " AND `dt`.`state` = 'Published'"));
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
        dumpData(writer, "ref_spec", true);
        dumpData(writer, "release");
        dumpData(writer, "seq_key", false,
                () -> dslContext.resultQuery("SELECT `seq_key`.* FROM `seq_key` " +
                        "JOIN `acc_manifest` ON `seq_key`.`from_acc_manifest_id` = `acc_manifest`.`acc_manifest_id` " +
                        "JOIN `acc` ON `acc_manifest`.`acc_id` = `acc`.`acc_id` " +
                        "WHERE `seq_key`.`seq_key_id` < " + delta + " AND `acc`.`state` = 'Published'"));
        dumpData(writer, "xbt", true);
        dumpData(writer, "xbt_manifest", true);

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

    private void writeEndHeaders(PrintWriter writer, Release release) {
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
        writer.println("ALTER TABLE `bdt_pri_restri` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `bdt_sc_pri_restri` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `cdt_awd_pri` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `cdt_awd_pri_xps_type_map` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `cdt_ref_spec` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `cdt_sc_awd_pri` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `cdt_sc_awd_pri_xps_type_map` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `cdt_sc_ref_spec` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list_value` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `code_list_value_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_sc` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `dt_sc_manifest` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `log` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `namespace` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("ALTER TABLE `seq_key` AUTO_INCREMENT = " + deltaPlusOne + ";");
        writer.println("");
        writer.println("-- End of 'Post processes' --");
        writer.println("");
    }

    private File generateInfoFile(File scriptFile, ScoreUser user, Release release) throws IOException {
        MigrationInfo migrationInfo = new MigrationInfo();
        migrationInfo.setVersion("1.0");
        migrationInfo.setMaintainer(user.getUsername());
        migrationInfo.setDescription("Add " + release.getReleaseNum() + " release");
        MigrationInfo.Migration migration = migrationInfo.getMigration();

        migration.setRelease(release.getReleaseId(), release.getReleaseNum());
        MigrationInfo.Executable executable = new MigrationInfo.Executable();
        executable.setOrder(1);
        executable.setScript(scriptFile.getName());
        migration.setProcesses(Arrays.asList(executable));
        MigrationInfo.Metadata metadata = new MigrationInfo.Metadata();
        migration.setMetadata(metadata);

        metadata.setReleases(dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.notEqual(ULong.valueOf(release.getReleaseId())))
                .orderBy(RELEASE.RELEASE_ID)
                .fetchStream().map(record -> {
                    MigrationInfo.Release rel = new MigrationInfo.Release();
                    rel.setId(record.getReleaseId().toBigInteger());
                    rel.setReleaseNum(record.getReleaseNum());
                    return rel;
                })
                .collect(Collectors.toList()));

        List<String> tableNames = Arrays.asList("acc", "acc_manifest", "ascc", "ascc_manifest", "bcc", "bcc_manifest",
                "asccp", "asccp_manifest",
                "bccp", "bccp_manifest", "dt", "dt_manifest", "dt_sc", "dt_sc_manifest",
                "code_list", "code_list_manifest", "code_list_value", "code_list_value_manifest",
                "agency_id_list", "agency_id_list_manifest", "agency_id_list_value", "agency_id_list_value_manifest", "log",
                "seq_key", "namespace");
        metadata.setTables(tableNames.stream().map(tableName -> {
            BigInteger maxId = dslContext.resultQuery("SELECT max(`" + tableName + "_id`) FROM `" + tableName + "`")
                    .fetchOneInto(BigInteger.class);
            MigrationInfo.Table table = new MigrationInfo.Table();
            table.setTableName(tableName);
            table.setMaxId(maxId);
            return table;
        }).collect(Collectors.toList()));

        File infoFile = new File(scriptFile.getParentFile(), "mig_info_" + release.getReleaseNum() + ".json");
        migrationInfo.write(infoFile);
        return infoFile;
    }

    private List<String> getDdl(String tableName) throws IOException {
        return FileUtils.readLines(
                this.resourceLoader.getResource("classpath:schemas/" + tableName + ".ddl").getFile(),
                StandardCharsets.UTF_8);
    }

}
