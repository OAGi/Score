package org.oagi.score.gateway.http.api.release_management.service.package_load;

import org.oagi.score.gateway.http.api.release_management.model.package_info.Executable;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Info;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Metadata;
import org.oagi.score.gateway.http.api.release_management.model.package_info.Migration;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.ScriptSource;
import org.oagi.score.gateway.http.api.release_management.service.package_load.script.SqlScriptExecutor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Loads the data of a release package (the zip the migration script generator writes) into the schema the given
 * connection is of.
 *
 * <p>It is the loader of the standalone OAGIS data-loader, made to run inside the application. It names no schema: every
 * statement is run against the default schema of the connection, so the schema it works in is the one the caller has chosen
 * (the staging schema, never the one the application serves), and the caller owns the connection.</p>
 *
 * <p>It does what the data-loader does: the records of the end users are moved above the data of the package, the records of
 * the developers in the Working release are removed, and the scripts of the package are run. It differs in this: nothing is
 * printed (progress goes to a {@link ReleasePackageLoadProgress}), a package that does not fit the database is refused with an exception,
 * and a script that is not SQL is refused, since a package is data and not code to run in the server.</p>
 *
 * <p>The load is one transaction, but the scripts change the structure of tables, which MariaDB commits as it goes, so a
 * failure is not rolled back as a whole. A caller must load into a schema it can throw away.</p>
 */
public class ReleasePackageLoader {

    private long DELTA = 100000000L;

    private ScriptSource scriptSource;

    private ReleasePackageLoadProgress progress;

    private UnaryOperator<String> statements;

    /** @param statements what each statement of the scripts is changed by before it is run, or {@code null} for none */
    public ReleasePackageLoader(long delta, ScriptSource scriptSource, ReleasePackageLoadProgress progress, UnaryOperator<String> statements) {
        this.statements = statements != null ? statements : UnaryOperator.identity();
        this.DELTA = delta;
        this.scriptSource = scriptSource;
        this.progress = progress != null ? progress : ReleasePackageLoadProgress.NONE;
    }

    /**
     * @throws IllegalStateException when the database has a release the package does not know of
     */
    public void run(Connection con, Info info) throws Exception {
        boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            Metadata metadata = info.getMigration().getMetadata();
            List<String> unknownReleases = new ArrayList<>();
            try (Statement statement = con.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT `release_id`, `release_num` FROM `release`")) {
                while (rs.next()) {
                    if (!metadata.hasRelease(rs.getLong("release_id"))) {
                        unknownReleases.add(rs.getString("release_num"));
                    }
                }
            }
            if (!unknownReleases.isEmpty()) {
                throw new IllegalStateException("The database has release(s) that the package does not know of: "
                        + String.join(", ", unknownReleases));
            }

            // The triggers writing `record_change_log` have nothing to record while migrating, and they fail inside
            // the scripts' `LOCK TABLES` blocks. They write only while this session variable is NULL.
            try (Statement statement = con.createStatement()) {
                statement.execute("SET @disable_record_change_log = 1");
            }
            // A script that drops and recreates a table (e.g. `xbt`) takes the triggers of that table with it.
            Map<String, String> triggers = readTriggers(con);

            List<Executable> entrypoints = info.getMigration().getEntrypoints();
            if (entrypoints != null && !entrypoints.isEmpty()) {
                progress.report("Execute entrypoint tasks...");
                evaluateExecutables(con, entrypoints);
            }

            progress.report("Reposition end-user data...");
            relocatingEndUserData(con, info);

            progress.report("Remove developer components in Working release...");
            removeDeveloperData(con, info);

            List<Executable> processes = info.getMigration().getProcesses();
            if (processes != null && !processes.isEmpty()) {
                progress.report("Execute processes...");
                evaluateExecutables(con, processes);
            }

            restoreMissingTriggers(con, triggers);

            con.commit();
            progress.report("Completed.");
        } catch (Exception e) {
            con.rollback();
            throw e;
        } finally {
            try (Statement statement = con.createStatement()) {
                statement.execute("SET @disable_record_change_log = NULL");
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (SQLException ignored) {
                // The connection is the caller's to close; a session that cannot be reset is not used again.
            }
            con.setAutoCommit(autoCommit);
        }
    }

    private Map<String, String> readTriggers(Connection conn) throws SQLException {
        Map<String, String> triggers = new LinkedHashMap<>();
        try (Statement statement = conn.createStatement()) {
            for (String name : triggerNames(statement)) {
                // The 3rd column of SHOW CREATE TRIGGER is the 'SQL Original Statement'.
                try (ResultSet rs = statement.executeQuery("SHOW CREATE TRIGGER `" + name + "`")) {
                    if (rs.next()) {
                        triggers.put(name, rs.getString(3));
                    }
                }
            }
        }
        return triggers;
    }

    private void restoreMissingTriggers(Connection conn, Map<String, String> triggers) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            Set<String> existing = new HashSet<>(triggerNames(statement));
            for (Map.Entry<String, String> entry : triggers.entrySet()) {
                if (!existing.contains(entry.getKey())) {
                    progress.report("\tRestore the trigger `" + entry.getKey() + "` dropped with its table");
                    statement.execute(entry.getValue());
                }
            }
        }
    }

    private List<String> triggerNames(Statement statement) throws SQLException {
        List<String> names = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery("SELECT `trigger_name` FROM `information_schema`.`triggers` " +
                "WHERE `trigger_schema` = DATABASE()")) {
            while (rs.next()) {
                names.add(rs.getString(1));
            }
        }
        return names;
    }

    private void evaluateExecutables(Connection conn, List<Executable> executables) throws Exception {
        for (Executable executable : executables.stream().sorted(Comparator.comparingInt(Executable::getOrder)).collect(Collectors.toList())) {
            try (Statement statement = conn.createStatement()) {
                boolean valid = true;
                if (StringUtils.hasLength(executable.getValidationQuery())) {
                    try (ResultSet rs = statement.executeQuery(executable.getValidationQuery())) {
                        valid = rs.next();
                    }
                }
                if (valid) {
                    String scriptFilename = executable.getScript().substring(executable.getScript().lastIndexOf('/') + 1);
                    String message = "Executing " + scriptFilename;
                    if (StringUtils.hasLength(executable.getDescription())) {
                        message += " - " + executable.getDescription();
                    }
                    progress.report("\t" + message);
                    if (scriptFilename.endsWith(".sql")) {
                        this.executeScript(conn, executable.getScript());
                    } else {
                        throw new UnsupportedOperationException("Only SQL scripts are run; unsupported script: " + executable.getScript());
                    }
                }
            }
        }
    }

    private void executeScript(Connection con, String filename) throws SQLException, IOException {
        progress.report("\tExecuting " + filename + " as a SQL script");
        // Statements are executed while the script is being read; the script is never held in memory as a whole.
        try (Reader reader = new InputStreamReader(this.scriptSource.open(filename), StandardCharsets.UTF_8)) {
            int count = SqlScriptExecutor.execute(con, reader, this.statements);
            progress.report("\t" + count + " statement(s) executed");
        }
    }

    private void removeDeveloperAsccpData(Connection conn) throws SQLException {
        List<Long> asccpIdList = new ArrayList();
        List<Long> asccpManifestIdList = new ArrayList();
        List<Long> logIdList = new ArrayList();
        List<Long> asbiepIdList = new ArrayList();

        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `asccp`.`asccp_id` FROM `asccp` " +
                    "JOIN `app_user` ON `asccp`.`created_by` = `app_user`.`app_user_id` " +
                    "WHERE `asccp`.`asccp_id` >= " + DELTA + " AND " +
                    "`app_user`.`app_user_id` < " + DELTA + " AND `app_user`.`is_developer` = 1")) {
                while (rs.next()) {
                    long asccpId = rs.getLong("asccp_id");
                    asccpIdList.add(asccpId);
                }
            }
        }
        if (!asccpIdList.isEmpty()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT `asccp_manifest`.`asccp_manifest_id`, `asccp_manifest`.`log_id` " +
                        "FROM `asccp_manifest` " +
                        "WHERE `asccp_manifest`.`asccp_id` IN (" + asccpIdList.stream().map(e -> Long.toString(e))
                        .collect(Collectors.joining(", ")) + ")")) {
                    while (rs.next()) {
                        long asccpManifestId = rs.getLong("asccp_manifest_id");
                        asccpManifestIdList.add(asccpManifestId);

                        long logId = rs.getLong("log_id");
                        logIdList.add(logId);
                    }
                }
            }

            if (!asccpManifestIdList.isEmpty()) {
                try (Statement statement = conn.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SELECT `asbiep`.`asbiep_id` " +
                            "FROM `asbiep` " +
                            "WHERE `asbiep`.`based_asccp_manifest_id` IN (" + asccpManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")")) {
                        while (rs.next()) {
                            long asbiepId = rs.getLong("asbiep_id");
                            asbiepIdList.add(asbiepId);
                        }
                    }
                }
            }
        }

        progress.report("\t\tFound " + asccpIdList.size() + " target `asccp` record(s)");
        if (!asccpIdList.isEmpty()) {
            progress.report("\t\tFound " + asccpManifestIdList.size() + " target `asccp_manifest` record(s)");
            progress.report("\t\tFound " + logIdList.size() + " target `log` record(s)");
            progress.report("\t\tFound " + asbiepIdList.size() + " target `asbiep` record(s)");

            try (Statement statement = conn.createStatement()) {
                if (!asccpIdList.isEmpty()) {
                    statement.execute("DELETE FROM `asccp` WHERE `asccp_id` IN (" + asccpIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!asccpManifestIdList.isEmpty()) {
                    statement.execute("DELETE FROM `asccp_manifest` WHERE `asccp_manifest_id` IN (" + asccpManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!logIdList.isEmpty()) {
                    statement.execute("DELETE FROM `log` WHERE `log_id` IN (" + logIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!asbiepIdList.isEmpty()) {
                    statement.execute("DELETE FROM `asbiep` WHERE `asbiep_id` IN (" + asbiepIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
            }
        }
    }

    private void removeDeveloperBccpData(Connection conn) throws SQLException {
        List<Long> bccpIdList = new ArrayList();
        List<Long> bccpManifestIdList = new ArrayList();
        List<Long> logIdList = new ArrayList();
        List<Long> bbiepIdList = new ArrayList();

        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `bccp`.`bccp_id` FROM `bccp` " +
                    "JOIN `app_user` ON `bccp`.`created_by` = `app_user`.`app_user_id` " +
                    "WHERE `bccp`.`bccp_id` >= " + DELTA + " AND " +
                    "`app_user`.`app_user_id` < " + DELTA + " AND `app_user`.`is_developer` = 1")) {
                while (rs.next()) {
                    long bccpId = rs.getLong("bccp_id");
                    bccpIdList.add(bccpId);
                }
            }
        }
        if (!bccpIdList.isEmpty()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT `bccp_manifest`.`bccp_manifest_id`, `bccp_manifest`.`log_id` " +
                        "FROM `bccp_manifest` " +
                        "WHERE `bccp_manifest`.`bccp_id` IN (" + bccpIdList.stream().map(e -> Long.toString(e))
                        .collect(Collectors.joining(", ")) + ")")) {
                    while (rs.next()) {
                        long bccpManifestId = rs.getLong("bccp_manifest_id");
                        bccpManifestIdList.add(bccpManifestId);

                        long logId = rs.getLong("log_id");
                        logIdList.add(logId);
                    }
                }
            }

            if (!bccpManifestIdList.isEmpty()) {
                try (Statement statement = conn.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SELECT `bbiep`.`bbiep_id` " +
                            "FROM `bbiep` " +
                            "WHERE `bbiep`.`based_bccp_manifest_id` IN (" + bccpManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")")) {
                        while (rs.next()) {
                            long bbiepId = rs.getLong("bbiep_id");
                            bbiepIdList.add(bbiepId);
                        }
                    }
                }
            }
        }

        progress.report("\t\tFound " + bccpIdList.size() + " target `bccp` record(s)");
        if (!bccpIdList.isEmpty()) {
            progress.report("\t\tFound " + bccpManifestIdList.size() + " target `bccp_manifest` record(s)");
            progress.report("\t\tFound " + logIdList.size() + " target `log` record(s)");
            progress.report("\t\tFound " + bbiepIdList.size() + " target `bbiep` record(s)");

            try (Statement statement = conn.createStatement()) {
                if (!bccpIdList.isEmpty()) {
                    statement.execute("DELETE FROM `bccp` WHERE `bccp_id` IN (" + bccpIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!bccpManifestIdList.isEmpty()) {
                    statement.execute("DELETE FROM `bccp_manifest` WHERE `bccp_manifest_id` IN (" + bccpManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!logIdList.isEmpty()) {
                    statement.execute("DELETE FROM `log` WHERE `log_id` IN (" + logIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!bbiepIdList.isEmpty()) {
                    statement.execute("DELETE FROM `bbiep` WHERE `bbiep_id` IN (" + bbiepIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
            }
        }
    }

    private void removeDeveloperAsccData(Connection conn) throws SQLException {
        List<Long> asccIdList = new ArrayList();
        List<Long> asccManifestIdList = new ArrayList();
        List<Long> seqKeyIdList = new ArrayList();
        List<Long> asbieIdList = new ArrayList();

        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `ascc`.`ascc_id` FROM `ascc` " +
                    "JOIN `acc` ON `ascc`.`from_acc_id` = `acc`.`acc_id` " +
                    "JOIN `app_user` ON `acc`.`created_by` = `app_user`.`app_user_id` " +
                    "WHERE `acc`.`acc_id` >= " + DELTA + " AND " +
                    "`app_user`.`app_user_id` < " + DELTA + " AND `app_user`.`is_developer` = 1")) {
                while (rs.next()) {
                    long asccId = rs.getLong("ascc_id");
                    asccIdList.add(asccId);
                }
            }
        }

        if (!asccIdList.isEmpty()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT `ascc_manifest`.`ascc_manifest_id`, `ascc_manifest`.`seq_key_id` " +
                        "FROM `ascc_manifest` " +
                        "WHERE `ascc_manifest`.`ascc_id` IN (" + asccIdList.stream().map(e -> Long.toString(e))
                        .collect(Collectors.joining(", ")) + ")")) {
                    while (rs.next()) {
                        long asccManifestId = rs.getLong("ascc_manifest_id");
                        asccManifestIdList.add(asccManifestId);

                        long seqKeyId = rs.getLong("seq_key_id");
                        seqKeyIdList.add(seqKeyId);
                    }
                }
            }

            if (!asccManifestIdList.isEmpty()) {
                try (Statement statement = conn.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SELECT `asbie`.`asbie_id` " +
                            "FROM `asbie` " +
                            "WHERE `asbie`.`based_ascc_manifest_id` IN (" + asccManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")")) {
                        while (rs.next()) {
                            long asbieId = rs.getLong("asbie_id");
                            asbieIdList.add(asbieId);
                        }
                    }
                }
            }
        }

        progress.report("\t\tFound " + asccIdList.size() + " target `ascc` record(s)");
        if (!asccIdList.isEmpty()) {
            progress.report("\t\tFound " + asccManifestIdList.size() + " target `ascc_manifest` record(s)");
            progress.report("\t\tFound " + seqKeyIdList.size() + " target `seq_key` record(s)");
            progress.report("\t\tFound " + asbieIdList.size() + " target `asbie` record(s)");

            try (Statement statement = conn.createStatement()) {
                if (!asccIdList.isEmpty()) {
                    statement.execute("DELETE FROM `ascc` WHERE `ascc_id` IN (" + asccIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!asccManifestIdList.isEmpty()) {
                    statement.execute("DELETE FROM `ascc_manifest` WHERE `ascc_manifest_id` IN (" + asccManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!seqKeyIdList.isEmpty()) {
                    statement.execute("DELETE FROM `seq_key` WHERE `seq_key_id` IN (" + seqKeyIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!asbieIdList.isEmpty()) {
                    statement.execute("DELETE FROM `asbie` WHERE `asbie_id` IN (" + asbieIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
            }
        }
    }

    private void removeDeveloperBccData(Connection conn) throws SQLException {
        List<Long> bccIdList = new ArrayList();
        List<Long> bccManifestIdList = new ArrayList();
        List<Long> seqKeyIdList = new ArrayList();
        List<Long> bbieIdList = new ArrayList();

        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `bcc`.`bcc_id` FROM `bcc` " +
                    "JOIN `acc` ON `bcc`.`from_acc_id` = `acc`.`acc_id` " +
                    "JOIN `app_user` ON `acc`.`created_by` = `app_user`.`app_user_id` " +
                    "WHERE `acc`.`acc_id` >= " + DELTA + " AND " +
                    "`app_user`.`app_user_id` < " + DELTA + " AND `app_user`.`is_developer` = 1")) {
                while (rs.next()) {
                    long bccId = rs.getLong("bcc_id");
                    bccIdList.add(bccId);
                }
            }
        }

        if (!bccIdList.isEmpty()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT `bcc_manifest`.`bcc_manifest_id`, `bcc_manifest`.`seq_key_id` " +
                        "FROM `bcc_manifest` " +
                        "WHERE `bcc_manifest`.`bcc_id` IN (" + bccIdList.stream().map(e -> Long.toString(e))
                        .collect(Collectors.joining(", ")) + ")")) {
                    while (rs.next()) {
                        long bccManifestId = rs.getLong("bcc_manifest_id");
                        bccManifestIdList.add(bccManifestId);

                        long seqKeyId = rs.getLong("seq_key_id");
                        seqKeyIdList.add(seqKeyId);
                    }
                }
            }

            if (!bccManifestIdList.isEmpty()) {
                try (Statement statement = conn.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SELECT `bbie`.`bbie_id` " +
                            "FROM `bbie` " +
                            "WHERE `bbie`.`based_bcc_manifest_id` IN (" + bccManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")")) {
                        while (rs.next()) {
                            long bbieId = rs.getLong("bbie_id");
                            bbieIdList.add(bbieId);
                        }
                    }
                }
            }
        }

        progress.report("\t\tFound " + bccIdList.size() + " target `bcc` record(s)");
        if (!bccIdList.isEmpty()) {
            progress.report("\t\tFound " + bccManifestIdList.size() + " target `bcc_manifest` record(s)");
            progress.report("\t\tFound " + seqKeyIdList.size() + " target `seq_key` record(s)");
            progress.report("\t\tFound " + bbieIdList.size() + " target `bbie` record(s)");

            try (Statement statement = conn.createStatement()) {
                if (!bccIdList.isEmpty()) {
                    statement.execute("DELETE FROM `bcc` WHERE `bcc_id` IN (" + bccIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!bccManifestIdList.isEmpty()) {
                    statement.execute("DELETE FROM `bcc_manifest` WHERE `bcc_manifest_id` IN (" + bccManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!seqKeyIdList.isEmpty()) {
                    statement.execute("DELETE FROM `seq_key` WHERE `seq_key_id` IN (" + seqKeyIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!bbieIdList.isEmpty()) {
                    statement.execute("DELETE FROM `bbie` WHERE `bbie_id` IN (" + bbieIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
            }
        }
    }

    private void removeDeveloperAccData(Connection conn) throws SQLException {
        List<Long> accIdList = new ArrayList();
        List<Long> accManifestIdList = new ArrayList();
        List<Long> logIdList = new ArrayList();
        List<Long> abieIdList = new ArrayList();

        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `acc`.`acc_id` FROM `acc` " +
                    "JOIN `app_user` ON `acc`.`created_by` = `app_user`.`app_user_id` " +
                    "WHERE `acc`.`acc_id` >= " + DELTA + " AND " +
                    "`app_user`.`app_user_id` < " + DELTA + " AND `app_user`.`is_developer` = 1")) {
                while (rs.next()) {
                    long accId = rs.getLong("acc_id");
                    accIdList.add(accId);
                }
            }
        }

        if (!accIdList.isEmpty()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery("SELECT `acc_manifest`.`acc_manifest_id`, `acc_manifest`.`log_id` " +
                        "FROM `acc_manifest` " +
                        "WHERE `acc_manifest`.`acc_id` IN (" + accIdList.stream().map(e -> Long.toString(e))
                        .collect(Collectors.joining(", ")) + ")")) {
                    while (rs.next()) {
                        long accManifestId = rs.getLong("acc_manifest_id");
                        accManifestIdList.add(accManifestId);

                        long logId = rs.getLong("log_id");
                        logIdList.add(logId);
                    }
                }
            }

            if (!accManifestIdList.isEmpty()) {
                try (Statement statement = conn.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SELECT `abie`.`abie_id` " +
                            "FROM `abie` " +
                            "WHERE `abie`.`based_acc_manifest_id` IN (" + accManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")")) {
                        while (rs.next()) {
                            long abieId = rs.getLong("abie_id");
                            abieIdList.add(abieId);
                        }
                    }
                }
            }
        }

        progress.report("\t\tFound " + accIdList.size() + " target `acc` record(s)");
        if (!accIdList.isEmpty()) {
            progress.report("\t\tFound " + accManifestIdList.size() + " target `acc_manifest` record(s)");
            progress.report("\t\tFound " + logIdList.size() + " target `log` record(s)");
            progress.report("\t\tFound " + abieIdList.size() + " target `abie` record(s)");

            try (Statement statement = conn.createStatement()) {
                if (!accIdList.isEmpty()) {
                    statement.execute("DELETE FROM `acc` WHERE `acc_id` IN (" + accIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!accManifestIdList.isEmpty()) {
                    statement.execute("DELETE FROM `acc_manifest` WHERE `acc_manifest_id` IN (" + accManifestIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!logIdList.isEmpty()) {
                    statement.execute("DELETE FROM `log` WHERE `log_id` IN (" + logIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
                if (!abieIdList.isEmpty()) {
                    statement.execute("DELETE FROM `abie` WHERE `abie_id` IN (" + abieIdList.stream().map(e -> Long.toString(e))
                            .collect(Collectors.joining(", ")) + ")");
                }
            }
        }
    }

    private void removeDeveloperData(Connection conn, Info info) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        progress.report("\tRemove developer components in Working release for `asccp` table...");
        removeDeveloperAsccpData(conn);

        progress.report("\tRemove developer components in Working release for `bccp` table...");
        removeDeveloperBccpData(conn);

        progress.report("\tRemove developer components in Working release for `ascc` table...");
        removeDeveloperAsccData(conn);

        progress.report("\tRemove developer components in Working release for `bcc` table...");
        removeDeveloperBccData(conn);

        progress.report("\tRemove developer components in Working release for `acc` table...");
        removeDeveloperAccData(conn);

        try (Statement statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private void relocatingEndUserData(Connection conn, Info info) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
        }

        progress.report("\tReposition end-user records in `app_user` table...");
        relocatingAppUser(conn, info);

        progress.report("\tReposition end-user records in `asccp` table...");
        relocatingAsccp(conn, info);

        progress.report("\tReposition end-user records in `bccp` table...");
        relocatingBccp(conn, info);

        progress.report("\tReposition end-user records in `dt` table...");
        relocatingDt(conn, info);

        progress.report("\tReposition end-user records in `dt_sc` table...");
        relocatingDtSc(conn, info);

        progress.report("\tReposition end-user records in `ascc` table...");
        relocatingAscc(conn, info);

        progress.report("\tReposition end-user records in `bcc` table...");
        relocatingBcc(conn, info);

        progress.report("\tReposition end-user records in `acc` table...");
        relocatingAcc(conn, info);

        progress.report("\tReposition end-user records in `seq_key` table...");
        relocatingSeqKey(conn, info);

        progress.report("\tReposition end-user records in `code_list` table...");
        relocatingCodeList(conn, info);

        progress.report("\tReposition end-user records in `code_list_value` table...");
        relocatingCodeListValue(conn, info);

        progress.report("\tReposition end-user records in `log` table...");
        relocatingLog(conn, info);

        progress.report("\tReposition end-user records in `namespace` table...");
        relocatingNamespace(conn, info);

        progress.report("\tReposition end-user records in `library` table...");
        relocatingLibrary(conn, info);

        try (Statement statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private void relocatingAcc(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> accIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `acc`.`acc_id` FROM `acc` " +
                    "WHERE `acc`.`acc_id` > " + metadata.getTable("acc").getMaxId() + " AND `acc`.`acc_id` < " + DELTA)) {
                while (rs.next()) {
                    long accId = rs.getLong("acc_id");
                    accIdMap.put(accId, accId + DELTA);
                }
            }
        }

        Map<Long, Long> accManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `acc_manifest`.`acc_manifest_id` FROM `acc_manifest` " +
                    "WHERE `acc_manifest`.`acc_manifest_id` > " + metadata.getTable("acc_manifest").getMaxId() + " AND `acc_manifest`.`acc_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long accManifestId = rs.getLong("acc_manifest_id");
                    accManifestIdMap.put(accManifestId, accManifestId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + accIdMap.size() + " target `acc` record(s)");
        progress.report("\t\tFound " + accManifestIdMap.size() + " target `acc_manifest` record(s)");

        for (Map.Entry<Long, Long> entry : accIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `acc` SET `acc_id` = " + entry.getValue() + " WHERE `acc_id` = " + entry.getKey());
                statement.execute("UPDATE `acc` SET `based_acc_id` = " + entry.getValue() + " WHERE `based_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `acc` SET `replacement_acc_id` = " + entry.getValue() + " WHERE `replacement_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `acc` SET `prev_acc_id` = " + entry.getValue() + " WHERE `prev_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `acc` SET `next_acc_id` = " + entry.getValue() + " WHERE `next_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest` SET `acc_id` = " + entry.getValue() + " WHERE `acc_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc` SET `from_acc_id` = " + entry.getValue() + " WHERE `from_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `role_of_acc_id` = " + entry.getValue() + " WHERE `role_of_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc` SET `from_acc_id` = " + entry.getValue() + " WHERE `from_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `bie_user_ext_revision` SET `ext_acc_id` = " + entry.getValue() + " WHERE `ext_acc_id` = " + entry.getKey());
                statement.execute("UPDATE `bie_user_ext_revision` SET `user_ext_acc_id` = " + entry.getValue() + " WHERE `user_ext_acc_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"acc_id\": " + entry.getKey() + "', '\"acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_acc_id\": " + entry.getKey() + "', '\"based_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_acc_id\": " + entry.getKey() + "', '\"replacement_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_acc_id\": " + entry.getKey() + "', '\"prev_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_acc_id\": " + entry.getKey() + "', '\"next_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"from_acc_id\": " + entry.getKey() + "', '\"from_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"role_of_acc_id\": " + entry.getKey() + "', '\"role_of_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"ext_acc_id\": " + entry.getKey() + "', '\"ext_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"user_ext_acc_id\": " + entry.getKey() + "', '\"user_ext_acc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : accManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `based_acc_manifest_id` = " + entry.getValue() + " WHERE `based_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest` SET `acc_manifest_id` = " + entry.getValue() + " WHERE `acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest` SET `based_acc_manifest_id` = " + entry.getValue() + " WHERE `based_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest` SET `replacement_acc_manifest_id` = " + entry.getValue() + " WHERE `replacement_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest` SET `prev_acc_manifest_id` = " + entry.getValue() + " WHERE `prev_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest` SET `next_acc_manifest_id` = " + entry.getValue() + " WHERE `next_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest_tag` SET `acc_manifest_id` = " + entry.getValue() + " WHERE `acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_manifest` SET `from_acc_manifest_id` = " + entry.getValue() + " WHERE `from_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest` SET `role_of_acc_manifest_id` = " + entry.getValue() + " WHERE `role_of_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `from_acc_manifest_id` = " + entry.getValue() + " WHERE `from_acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `module_acc_manifest` SET `acc_manifest_id` = " + entry.getValue() + " WHERE `acc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `seq_key` SET `from_acc_manifest_id` = " + entry.getValue() + " WHERE `from_acc_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `path` = REPLACE(`path`, 'ACC-" + entry.getKey() + "', 'ACC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbie` SET `path` = REPLACE(`path`, 'ACC-" + entry.getKey() + "', 'ACC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie` SET `path` = REPLACE(`path`, 'ACC-" + entry.getKey() + "', 'ACC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbiep` SET `path` = REPLACE(`path`, 'ACC-" + entry.getKey() + "', 'ACC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbiep` SET `path` = REPLACE(`path`, 'ACC-" + entry.getKey() + "', 'ACC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie_sc` SET `path` = REPLACE(`path`, 'ACC-" + entry.getKey() + "', 'ACC-" + entry.getValue() + "')");

                statement.execute("UPDATE `abie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie_sc` SET `hash_path` = SHA2(`path`, 256)");
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"acc_manifest_id\": " + entry.getKey() + "', '\"acc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_acc_manifest_id\": " + entry.getKey() + "', '\"based_acc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_acc_manifest_id\": " + entry.getKey() + "', '\"replacement_acc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_acc_manifest_id\": " + entry.getKey() + "', '\"prev_acc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_acc_manifest_id\": " + entry.getKey() + "', '\"next_acc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"from_acc_manifest_id\": " + entry.getKey() + "', '\"from_acc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"role_of_acc_manifest_id\": " + entry.getKey() + "', '\"role_of_acc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingAscc(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> asccIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `ascc`.`ascc_id` FROM `ascc` " +
                    "WHERE `ascc`.`ascc_id` > " + metadata.getTable("ascc").getMaxId() + " AND `ascc`.`ascc_id` < " + DELTA)) {
                while (rs.next()) {
                    long asccId = rs.getLong("ascc_id");
                    asccIdMap.put(asccId, asccId + DELTA);
                }
            }
        }

        Map<Long, Long> asccManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `ascc_manifest`.`ascc_manifest_id` FROM `ascc_manifest` " +
                    "WHERE `ascc_manifest`.`ascc_manifest_id` > " + metadata.getTable("ascc_manifest").getMaxId() + " AND `ascc_manifest`.`ascc_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long asccManifestId = rs.getLong("ascc_manifest_id");
                    asccManifestIdMap.put(asccManifestId, asccManifestId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + asccIdMap.size() + " target `ascc` record(s)");
        progress.report("\t\tFound " + asccManifestIdMap.size() + " target `ascc_manifest` record(s)");

        for (Map.Entry<Long, Long> entry : asccIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `ascc` SET `ascc_id` = " + entry.getValue() + " WHERE `ascc_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc` SET `replacement_ascc_id` = " + entry.getValue() + " WHERE `replacement_ascc_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc` SET `prev_ascc_id` = " + entry.getValue() + " WHERE `prev_ascc_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc` SET `next_ascc_id` = " + entry.getValue() + " WHERE `next_ascc_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_bizterm` SET `ascc_id` = " + entry.getValue() + " WHERE `ascc_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_manifest` SET `ascc_id` = " + entry.getValue() + " WHERE `ascc_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"ascc_id\": " + entry.getKey() + "', '\"ascc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_ascc_id\": " + entry.getKey() + "', '\"replacement_ascc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_ascc_id\": " + entry.getKey() + "', '\"prev_ascc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_ascc_id\": " + entry.getKey() + "', '\"next_ascc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : asccManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `asbie` SET `based_ascc_manifest_id` = " + entry.getValue() + " WHERE `based_ascc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_manifest` SET `ascc_manifest_id` = " + entry.getValue() + " WHERE `ascc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_manifest` SET `replacement_ascc_manifest_id` = " + entry.getValue() + " WHERE `replacement_ascc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_manifest` SET `prev_ascc_manifest_id` = " + entry.getValue() + " WHERE `prev_ascc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_manifest` SET `next_ascc_manifest_id` = " + entry.getValue() + " WHERE `next_ascc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `seq_key` SET `ascc_manifest_id` = " + entry.getValue() + " WHERE `ascc_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `path` = REPLACE(`path`, 'ASCC-" + entry.getKey() + "', 'ASCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbie` SET `path` = REPLACE(`path`, 'ASCC-" + entry.getKey() + "', 'ASCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie` SET `path` = REPLACE(`path`, 'ASCC-" + entry.getKey() + "', 'ASCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbiep` SET `path` = REPLACE(`path`, 'ASCC-" + entry.getKey() + "', 'ASCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbiep` SET `path` = REPLACE(`path`, 'ASCC-" + entry.getKey() + "', 'ASCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie_sc` SET `path` = REPLACE(`path`, 'ASCC-" + entry.getKey() + "', 'ASCC-" + entry.getValue() + "')");

                statement.execute("UPDATE `abie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie_sc` SET `hash_path` = SHA2(`path`, 256)");
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"ascc_manifest_id\": " + entry.getKey() + "', '\"ascc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_ascc_manifest_id\": " + entry.getKey() + "', '\"based_ascc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_ascc_manifest_id\": " + entry.getKey() + "', '\"replacement_ascc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_ascc_manifest_id\": " + entry.getKey() + "', '\"prev_ascc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_ascc_manifest_id\": " + entry.getKey() + "', '\"next_ascc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingBcc(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> bccIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `bcc`.`bcc_id` FROM `bcc` " +
                    "WHERE `bcc`.`bcc_id` > " + metadata.getTable("bcc").getMaxId() + " AND `bcc`.`bcc_id` < " + DELTA)) {
                while (rs.next()) {
                    long bccId = rs.getLong("bcc_id");
                    bccIdMap.put(bccId, bccId + DELTA);
                }
            }
        }

        Map<Long, Long> bccManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `bcc_manifest`.`bcc_manifest_id` FROM `bcc_manifest` " +
                    "WHERE `bcc_manifest`.`bcc_manifest_id` > " + metadata.getTable("bcc_manifest").getMaxId() + " AND `bcc_manifest`.`bcc_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long bccManifestId = rs.getLong("bcc_manifest_id");
                    bccManifestIdMap.put(bccManifestId, bccManifestId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + bccIdMap.size() + " target `bcc` record(s)");
        progress.report("\t\tFound " + bccManifestIdMap.size() + " target `bcc_manifest` record(s)");

        for (Map.Entry<Long, Long> entry : bccIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bcc` SET `bcc_id` = " + entry.getValue() + " WHERE `bcc_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc` SET `replacement_bcc_id` = " + entry.getValue() + " WHERE `replacement_bcc_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc` SET `prev_bcc_id` = " + entry.getValue() + " WHERE `prev_bcc_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc` SET `next_bcc_id` = " + entry.getValue() + " WHERE `next_bcc_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_bizterm` SET `bcc_id` = " + entry.getValue() + " WHERE `bcc_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `bcc_id` = " + entry.getValue() + " WHERE `bcc_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"bcc_id\": " + entry.getKey() + "', '\"bcc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_bcc_id\": " + entry.getKey() + "', '\"replacement_bcc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_bcc_id\": " + entry.getKey() + "', '\"prev_bcc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_bcc_id\": " + entry.getKey() + "', '\"next_bcc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : bccManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bbie` SET `based_bcc_manifest_id` = " + entry.getValue() + " WHERE `based_bcc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `bcc_manifest_id` = " + entry.getValue() + " WHERE `bcc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `replacement_bcc_manifest_id` = " + entry.getValue() + " WHERE `replacement_bcc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `prev_bcc_manifest_id` = " + entry.getValue() + " WHERE `prev_bcc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `next_bcc_manifest_id` = " + entry.getValue() + " WHERE `next_bcc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `seq_key` SET `bcc_manifest_id` = " + entry.getValue() + " WHERE `bcc_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `path` = REPLACE(`path`, 'BCC-" + entry.getKey() + "', 'BCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbie` SET `path` = REPLACE(`path`, 'BCC-" + entry.getKey() + "', 'BCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie` SET `path` = REPLACE(`path`, 'BCC-" + entry.getKey() + "', 'BCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbiep` SET `path` = REPLACE(`path`, 'BCC-" + entry.getKey() + "', 'BCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbiep` SET `path` = REPLACE(`path`, 'BCC-" + entry.getKey() + "', 'BCC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie_sc` SET `path` = REPLACE(`path`, 'BCC-" + entry.getKey() + "', 'BCC-" + entry.getValue() + "')");

                statement.execute("UPDATE `abie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie_sc` SET `hash_path` = SHA2(`path`, 256)");
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"bcc_manifest_id\": " + entry.getKey() + "', '\"bcc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_bcc_manifest_id\": " + entry.getKey() + "', '\"based_bcc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_bcc_manifest_id\": " + entry.getKey() + "', '\"replacement_bcc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_bcc_manifest_id\": " + entry.getKey() + "', '\"prev_bcc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_bcc_manifest_id\": " + entry.getKey() + "', '\"next_bcc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingAsccp(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> asccpIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `asccp`.`asccp_id` FROM `asccp` " +
                    "WHERE `asccp`.`asccp_id` > " + metadata.getTable("asccp").getMaxId() + " AND `asccp`.`asccp_id` < " + DELTA)) {
                while (rs.next()) {
                    long asccpId = rs.getLong("asccp_id");
                    asccpIdMap.put(asccpId, asccpId + DELTA);
                }
            }
        }

        Map<Long, Long> asccpManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `asccp_manifest`.`asccp_manifest_id` FROM `asccp_manifest` " +
                    "WHERE `asccp_manifest`.`asccp_manifest_id` > " + metadata.getTable("asccp_manifest").getMaxId() + " AND `asccp_manifest`.`asccp_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long asccpManifestId = rs.getLong("asccp_manifest_id");
                    asccpManifestIdMap.put(asccpManifestId, asccpManifestId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + asccpIdMap.size() + " target `asccp` record(s)");
        progress.report("\t\tFound " + asccpManifestIdMap.size() + " target `asccp_manifest` record(s)");

        for (Map.Entry<Long, Long> entry : asccpIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `ascc` SET `to_asccp_id` = " + entry.getValue() + " WHERE `to_asccp_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `asccp_id` = " + entry.getValue() + " WHERE `asccp_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `replacement_asccp_id` = " + entry.getValue() + " WHERE `replacement_asccp_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `prev_asccp_id` = " + entry.getValue() + " WHERE `prev_asccp_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `next_asccp_id` = " + entry.getValue() + " WHERE `next_asccp_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest` SET `asccp_id` = " + entry.getValue() + " WHERE `asccp_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"asccp_id\": " + entry.getKey() + "', '\"asccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_asccp_id\": " + entry.getKey() + "', '\"replacement_asccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_asccp_id\": " + entry.getKey() + "', '\"prev_asccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_asccp_id\": " + entry.getKey() + "', '\"next_asccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"to_asccp_id\": " + entry.getKey() + "', '\"to_asccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : asccpManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `asbiep` SET `based_asccp_manifest_id` = " + entry.getValue() + " WHERE `based_asccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc_manifest` SET `to_asccp_manifest_id` = " + entry.getValue() + " WHERE `to_asccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest` SET `asccp_manifest_id` = " + entry.getValue() + " WHERE `asccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest` SET `replacement_asccp_manifest_id` = " + entry.getValue() + " WHERE `replacement_asccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest` SET `prev_asccp_manifest_id` = " + entry.getValue() + " WHERE `prev_asccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest` SET `next_asccp_manifest_id` = " + entry.getValue() + " WHERE `next_asccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest_tag` SET `asccp_manifest_id` = " + entry.getValue() + " WHERE `asccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `module_asccp_manifest` SET `asccp_manifest_id` = " + entry.getValue() + " WHERE `asccp_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `path` = REPLACE(`path`, 'ASCCP-" + entry.getKey() + "', 'ASCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbie` SET `path` = REPLACE(`path`, 'ASCCP-" + entry.getKey() + "', 'ASCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie` SET `path` = REPLACE(`path`, 'ASCCP-" + entry.getKey() + "', 'ASCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbiep` SET `path` = REPLACE(`path`, 'ASCCP-" + entry.getKey() + "', 'ASCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbiep` SET `path` = REPLACE(`path`, 'ASCCP-" + entry.getKey() + "', 'ASCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie_sc` SET `path` = REPLACE(`path`, 'ASCCP-" + entry.getKey() + "', 'ASCCP-" + entry.getValue() + "')");

                statement.execute("UPDATE `abie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie_sc` SET `hash_path` = SHA2(`path`, 256)");
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"asccp_manifest_id\": " + entry.getKey() + "', '\"asccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_asccp_manifest_id\": " + entry.getKey() + "', '\"based_asccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_asccp_manifest_id\": " + entry.getKey() + "', '\"replacement_asccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_asccp_manifest_id\": " + entry.getKey() + "', '\"prev_asccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_asccp_manifest_id\": " + entry.getKey() + "', '\"next_asccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"to_asccp_manifest_id\": " + entry.getKey() + "', '\"to_asccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingBccp(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> bccpIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `bccp`.`bccp_id` FROM `bccp` " +
                    "WHERE `bccp`.`bccp_id` > " + metadata.getTable("bccp").getMaxId() + " AND `bccp`.`bccp_id` < " + DELTA)) {
                while (rs.next()) {
                    long bccpId = rs.getLong("bccp_id");
                    bccpIdMap.put(bccpId, bccpId + DELTA);
                }
            }
        }

        Map<Long, Long> bccpManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `bccp_manifest`.`bccp_manifest_id` FROM `bccp_manifest` " +
                    "WHERE `bccp_manifest`.`bccp_manifest_id` > " + metadata.getTable("bccp_manifest").getMaxId() + " AND `bccp_manifest`.`bccp_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long bccpManifestId = rs.getLong("bccp_manifest_id");
                    bccpManifestIdMap.put(bccpManifestId, bccpManifestId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + bccpIdMap.size() + " target `bccp` record(s)");
        progress.report("\t\tFound " + bccpManifestIdMap.size() + " target `bccp_manifest` record(s)");

        for (Map.Entry<Long, Long> entry : bccpIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bcc` SET `to_bccp_id` = " + entry.getValue() + " WHERE `to_bccp_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `bccp_id` = " + entry.getValue() + " WHERE `bccp_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `replacement_bccp_id` = " + entry.getValue() + " WHERE `replacement_bccp_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `prev_bccp_id` = " + entry.getValue() + " WHERE `prev_bccp_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `next_bccp_id` = " + entry.getValue() + " WHERE `next_bccp_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest` SET `bccp_id` = " + entry.getValue() + " WHERE `bccp_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"bccp_id\": " + entry.getKey() + "', '\"bccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_bccp_id\": " + entry.getKey() + "', '\"replacement_bccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_bccp_id\": " + entry.getKey() + "', '\"prev_bccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_bccp_id\": " + entry.getKey() + "', '\"next_bccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"to_bccp_id\": " + entry.getKey() + "', '\"to_bccp_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : bccpManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bbiep` SET `based_bccp_manifest_id` = " + entry.getValue() + " WHERE `based_bccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `to_bccp_manifest_id` = " + entry.getValue() + " WHERE `to_bccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest` SET `bccp_manifest_id` = " + entry.getValue() + " WHERE `bccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest` SET `replacement_bccp_manifest_id` = " + entry.getValue() + " WHERE `replacement_bccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest` SET `prev_bccp_manifest_id` = " + entry.getValue() + " WHERE `prev_bccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest` SET `next_bccp_manifest_id` = " + entry.getValue() + " WHERE `next_bccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest_tag` SET `bccp_manifest_id` = " + entry.getValue() + " WHERE `bccp_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `module_bccp_manifest` SET `bccp_manifest_id` = " + entry.getValue() + " WHERE `bccp_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `path` = REPLACE(`path`, 'BCCP-" + entry.getKey() + "', 'BCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbie` SET `path` = REPLACE(`path`, 'BCCP-" + entry.getKey() + "', 'BCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie` SET `path` = REPLACE(`path`, 'BCCP-" + entry.getKey() + "', 'BCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbiep` SET `path` = REPLACE(`path`, 'BCCP-" + entry.getKey() + "', 'BCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbiep` SET `path` = REPLACE(`path`, 'BCCP-" + entry.getKey() + "', 'BCCP-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie_sc` SET `path` = REPLACE(`path`, 'BCCP-" + entry.getKey() + "', 'BCCP-" + entry.getValue() + "')");

                statement.execute("UPDATE `abie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie_sc` SET `hash_path` = SHA2(`path`, 256)");
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"bccp_manifest_id\": " + entry.getKey() + "', '\"bccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_bccp_manifest_id\": " + entry.getKey() + "', '\"based_bccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_bccp_manifest_id\": " + entry.getKey() + "', '\"replacement_bccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_bccp_manifest_id\": " + entry.getKey() + "', '\"prev_bccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_bccp_manifest_id\": " + entry.getKey() + "', '\"next_bccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"to_bccp_manifest_id\": " + entry.getKey() + "', '\"to_bccp_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingDt(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> dtIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `dt`.`dt_id` FROM `dt` " +
                    "WHERE `dt`.`dt_id` > " + metadata.getTable("dt").getMaxId() + " AND `dt`.`dt_id` < " + DELTA)) {
                while (rs.next()) {
                    long dtId = rs.getLong("dt_id");
                    dtIdMap.put(dtId, dtId + DELTA);
                }
            }
        }

        Map<Long, Long> dtManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `dt_manifest`.`dt_manifest_id` FROM `dt_manifest` " +
                    "WHERE `dt_manifest`.`dt_manifest_id` > " + metadata.getTable("dt_manifest").getMaxId() + " AND `dt_manifest`.`dt_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long dtManifestId = rs.getLong("dt_manifest_id");
                    dtManifestIdMap.put(dtManifestId, dtManifestId + DELTA);
                }
            }
        }

        Map<Long, Long> dtAwdPriIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `dt_awd_pri`.`dt_awd_pri_id` FROM `dt_awd_pri` " +
                    "WHERE `dt_awd_pri`.`dt_id` > " + metadata.getTable("dt").getMaxId() + " AND `dt_awd_pri`.`dt_awd_pri_id` < " + DELTA)) {
                while (rs.next()) {
                    long dtAwdPriId = rs.getLong("dt_awd_pri_id");
                    dtAwdPriIdMap.put(dtAwdPriId, dtAwdPriId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + dtIdMap.size() + " target `dt` record(s)");
        progress.report("\t\tFound " + dtManifestIdMap.size() + " target `dt_manifest` record(s)");
        progress.report("\t\tFound " + dtAwdPriIdMap.size() + " target `dt_awd_pri` record(s)");

        for (Map.Entry<Long, Long> entry : dtIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bccp` SET `bdt_id` = " + entry.getValue() + " WHERE `bdt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_awd_pri` SET `dt_id` = " + entry.getValue() + " WHERE `dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `dt_id` = " + entry.getValue() + " WHERE `dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `based_dt_id` = " + entry.getValue() + " WHERE `based_dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `replacement_dt_id` = " + entry.getValue() + " WHERE `replacement_dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `prev_dt_id` = " + entry.getValue() + " WHERE `prev_dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `next_dt_id` = " + entry.getValue() + " WHERE `next_dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest` SET `dt_id` = " + entry.getValue() + " WHERE `dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `owner_dt_id` = " + entry.getValue() + " WHERE `owner_dt_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_usage_rule` SET `target_dt_id` = " + entry.getValue() + " WHERE `target_dt_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"dt_id\": " + entry.getKey() + "', '\"dt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_dt_id\": " + entry.getKey() + "', '\"based_dt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_dt_id\": " + entry.getKey() + "', '\"replacement_dt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_dt_id\": " + entry.getKey() + "', '\"prev_dt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_dt_id\": " + entry.getKey() + "', '\"next_dt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"owner_dt_id\": " + entry.getKey() + "', '\"owner_dt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"bdt_id\": " + entry.getKey() + "', '\"bdt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"cdt_id\": " + entry.getKey() + "', '\"cdt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"target_dt_id\": " + entry.getKey() + "', '\"target_dt_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : dtManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bccp_manifest` SET `bdt_manifest_id` = " + entry.getValue() + " WHERE `bdt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest` SET `dt_manifest_id` = " + entry.getValue() + " WHERE `dt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest` SET `based_dt_manifest_id` = " + entry.getValue() + " WHERE `based_dt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest` SET `replacement_dt_manifest_id` = " + entry.getValue() + " WHERE `replacement_dt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest` SET `prev_dt_manifest_id` = " + entry.getValue() + " WHERE `prev_dt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest` SET `next_dt_manifest_id` = " + entry.getValue() + " WHERE `next_dt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest_tag` SET `dt_manifest_id` = " + entry.getValue() + " WHERE `dt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc_manifest` SET `owner_dt_manifest_id` = " + entry.getValue() + " WHERE `owner_dt_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `module_dt_manifest` SET `dt_manifest_id` = " + entry.getValue() + " WHERE `dt_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `path` = REPLACE(`path`, 'DT-" + entry.getKey() + "', 'DT-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbie` SET `path` = REPLACE(`path`, 'DT-" + entry.getKey() + "', 'DT-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie` SET `path` = REPLACE(`path`, 'DT-" + entry.getKey() + "', 'DT-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbiep` SET `path` = REPLACE(`path`, 'DT-" + entry.getKey() + "', 'DT-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbiep` SET `path` = REPLACE(`path`, 'DT-" + entry.getKey() + "', 'DT-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie_sc` SET `path` = REPLACE(`path`, 'DT-" + entry.getKey() + "', 'DT-" + entry.getValue() + "')");

                statement.execute("UPDATE `abie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie_sc` SET `hash_path` = SHA2(`path`, 256)");
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"dt_manifest_id\": " + entry.getKey() + "', '\"dt_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_dt_manifest_id\": " + entry.getKey() + "', '\"based_dt_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_dt_manifest_id\": " + entry.getKey() + "', '\"replacement_dt_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_dt_manifest_id\": " + entry.getKey() + "', '\"prev_dt_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_dt_manifest_id\": " + entry.getKey() + "', '\"next_dt_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"owner_dt_manifest_id\": " + entry.getKey() + "', '\"owner_dt_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"bdt_manifest_id\": " + entry.getKey() + "', '\"bdt_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : dtAwdPriIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `dt_awd_pri` SET `dt_awd_pri_id` = " + entry.getValue() + " WHERE `dt_awd_pri_id` = " + entry.getKey());
            }
        }
    }

    private void relocatingDtSc(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> dtScIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `dt_sc`.`dt_sc_id` FROM `dt_sc` " +
                    "WHERE `dt_sc`.`dt_sc_id` > " + metadata.getTable("dt_sc").getMaxId() + " AND `dt_sc`.`dt_sc_id` < " + DELTA)) {
                while (rs.next()) {
                    long dtScId = rs.getLong("dt_sc_id");
                    dtScIdMap.put(dtScId, dtScId + DELTA);
                }
            }
        }

        Map<Long, Long> dtScManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `dt_sc_manifest`.`dt_sc_manifest_id` FROM `dt_sc_manifest` " +
                    "WHERE `dt_sc_manifest`.`dt_sc_manifest_id` > " + metadata.getTable("dt_sc_manifest").getMaxId() + " AND `dt_sc_manifest`.`dt_sc_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long dtScManifestId = rs.getLong("dt_sc_manifest_id");
                    dtScManifestIdMap.put(dtScManifestId, dtScManifestId + DELTA);
                }
            }
        }

        Map<Long, Long> dtScAwdPriIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `dt_sc_awd_pri`.`dt_sc_awd_pri_id` FROM `dt_sc_awd_pri` " +
                    "WHERE `dt_sc_awd_pri`.`dt_sc_id` > " + metadata.getTable("dt_sc").getMaxId() + " AND `dt_sc_awd_pri`.`dt_sc_awd_pri_id` < " + DELTA)) {
                while (rs.next()) {
                    long dtScAwdPriId = rs.getLong("dt_sc_awd_pri_id");
                    dtScAwdPriIdMap.put(dtScAwdPriId, dtScAwdPriId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + dtScIdMap.size() + " target `dt_sc` record(s)");
        progress.report("\t\tFound " + dtScManifestIdMap.size() + " target `dt_sc_manifest` record(s)");
        progress.report("\t\tFound " + dtScAwdPriIdMap.size() + " target `dt_sc_awd_pri` record(s)");

        for (Map.Entry<Long, Long> entry : dtScIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `dt_sc_awd_pri` SET `dt_sc_id` = " + entry.getValue() + " WHERE `dt_sc_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `dt_sc_id` = " + entry.getValue() + " WHERE `dt_sc_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `based_dt_sc_id` = " + entry.getValue() + " WHERE `based_dt_sc_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `replacement_dt_sc_id` = " + entry.getValue() + " WHERE `replacement_dt_sc_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `prev_dt_sc_id` = " + entry.getValue() + " WHERE `prev_dt_sc_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `next_dt_sc_id` = " + entry.getValue() + " WHERE `next_dt_sc_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc_manifest` SET `dt_sc_id` = " + entry.getValue() + " WHERE `dt_sc_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_usage_rule` SET `target_dt_sc_id` = " + entry.getValue() + " WHERE `target_dt_sc_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"dt_sc_id\": " + entry.getKey() + "', '\"dt_sc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_dt_sc_id\": " + entry.getKey() + "', '\"based_dt_sc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_dt_sc_id\": " + entry.getKey() + "', '\"replacement_dt_sc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_dt_sc_id\": " + entry.getKey() + "', '\"prev_dt_sc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_dt_sc_id\": " + entry.getKey() + "', '\"next_dt_sc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"cdt_sc_id\": " + entry.getKey() + "', '\"cdt_sc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"target_dt_sc_id\": " + entry.getKey() + "', '\"target_dt_sc_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : dtScManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bbie_sc` SET `based_dt_sc_manifest_id` = " + entry.getValue() + " WHERE `based_dt_sc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc_manifest` SET `dt_sc_manifest_id` = " + entry.getValue() + " WHERE `dt_sc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc_manifest` SET `based_dt_sc_manifest_id` = " + entry.getValue() + " WHERE `based_dt_sc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc_manifest` SET `replacement_dt_sc_manifest_id` = " + entry.getValue() + " WHERE `replacement_dt_sc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc_manifest` SET `prev_dt_sc_manifest_id` = " + entry.getValue() + " WHERE `prev_dt_sc_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc_manifest` SET `next_dt_sc_manifest_id` = " + entry.getValue() + " WHERE `next_dt_sc_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `abie` SET `path` = REPLACE(`path`, 'DT_SC-" + entry.getKey() + "', 'DT_SC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbie` SET `path` = REPLACE(`path`, 'DT_SC-" + entry.getKey() + "', 'DT_SC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie` SET `path` = REPLACE(`path`, 'DT_SC-" + entry.getKey() + "', 'DT_SC-" + entry.getValue() + "')");
                statement.execute("UPDATE `asbiep` SET `path` = REPLACE(`path`, 'DT_SC-" + entry.getKey() + "', 'DT_SC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbiep` SET `path` = REPLACE(`path`, 'DT_SC-" + entry.getKey() + "', 'DT_SC-" + entry.getValue() + "')");
                statement.execute("UPDATE `bbie_sc` SET `path` = REPLACE(`path`, 'DT_SC-" + entry.getKey() + "', 'DT_SC-" + entry.getValue() + "')");

                statement.execute("UPDATE `abie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `asbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbiep` SET `hash_path` = SHA2(`path`, 256)");
                statement.execute("UPDATE `bbie_sc` SET `hash_path` = SHA2(`path`, 256)");
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"dt_sc_manifest_id\": " + entry.getKey() + "', '\"dt_sc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_dt_sc_manifest_id\": " + entry.getKey() + "', '\"based_dt_sc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_dt_sc_manifest_id\": " + entry.getKey() + "', '\"replacement_dt_sc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_dt_sc_manifest_id\": " + entry.getKey() + "', '\"prev_dt_sc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_dt_sc_manifest_id\": " + entry.getKey() + "', '\"next_dt_sc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"bdt_sc_manifest_id\": " + entry.getKey() + "', '\"bdt_sc_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : dtScAwdPriIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `dt_sc_awd_pri` SET `dt_sc_awd_pri_id` = " + entry.getValue() + " WHERE `dt_sc_awd_pri_id` = " + entry.getKey());
            }
        }
    }

    private void relocatingSeqKey(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> seqKeyIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `seq_key`.`seq_key_id` FROM `seq_key` " +
                    "WHERE `seq_key`.`seq_key_id` > " + metadata.getTable("seq_key").getMaxId() + " AND `seq_key`.`seq_key_id` < " + DELTA)) {
                while (rs.next()) {
                    long seqKeyId = rs.getLong("seq_key_id");
                    seqKeyIdMap.put(seqKeyId, seqKeyId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + seqKeyIdMap.size() + " target `seq_key` record(s)");

        for (Map.Entry<Long, Long> entry : seqKeyIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `ascc_manifest` SET `seq_key_id` = " + entry.getValue() + " WHERE `seq_key_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc_manifest` SET `seq_key_id` = " + entry.getValue() + " WHERE `seq_key_id` = " + entry.getKey());
                statement.execute("UPDATE `seq_key` SET `seq_key_id` = " + entry.getValue() + " WHERE `seq_key_id` = " + entry.getKey());
                statement.execute("UPDATE `seq_key` SET `prev_seq_key_id` = " + entry.getValue() + " WHERE `prev_seq_key_id` = " + entry.getKey());
                statement.execute("UPDATE `seq_key` SET `next_seq_key_id` = " + entry.getValue() + " WHERE `next_seq_key_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"seq_key_id\": " + entry.getKey() + "', '\"seq_key_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_seq_key_id\": " + entry.getKey() + "', '\"prev_seq_key_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_seq_key_id\": " + entry.getKey() + "', '\"next_seq_key_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingCodeList(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> codeListIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `code_list`.`code_list_id` FROM `code_list` " +
                    "WHERE `code_list`.`code_list_id` > " + metadata.getTable("code_list").getMaxId() + " AND `code_list`.`code_list_id` < " + DELTA)) {
                while (rs.next()) {
                    long codeListId = rs.getLong("code_list_id");
                    codeListIdMap.put(codeListId, codeListId + DELTA);
                }
            }
        }

        Map<Long, Long> codeListManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `code_list_manifest`.`code_list_manifest_id` FROM `code_list_manifest` " +
                    "WHERE `code_list_manifest`.`code_list_manifest_id` > " + metadata.getTable("code_list_manifest").getMaxId() + " AND `code_list_manifest`.`code_list_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long codeListManifestId = rs.getLong("code_list_manifest_id");
                    codeListManifestIdMap.put(codeListManifestId, codeListManifestId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + codeListIdMap.size() + " target `code_list` record(s)");
        progress.report("\t\tFound " + codeListManifestIdMap.size() + " target `code_list_manifest` record(s)");

        for (Map.Entry<Long, Long> entry : codeListIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `code_list` SET `code_list_id` = " + entry.getValue() + " WHERE `code_list_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `based_code_list_id` = " + entry.getValue() + " WHERE `based_code_list_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `replacement_code_list_id` = " + entry.getValue() + " WHERE `replacement_code_list_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `prev_code_list_id` = " + entry.getValue() + " WHERE `prev_code_list_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `next_code_list_id` = " + entry.getValue() + " WHERE `next_code_list_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_manifest` SET `code_list_id` = " + entry.getValue() + " WHERE `code_list_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `code_list_id` = " + entry.getValue() + " WHERE `code_list_id` = " + entry.getKey());
                statement.execute("UPDATE `ctx_scheme` SET `code_list_id` = " + entry.getValue() + " WHERE `code_list_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"code_list_id\": " + entry.getKey() + "', '\"code_list_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_code_list_id\": " + entry.getKey() + "', '\"based_code_list_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_code_list_id\": " + entry.getKey() + "', '\"replacement_code_list_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_code_list_id\": " + entry.getKey() + "', '\"prev_code_list_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_code_list_id\": " + entry.getKey() + "', '\"next_code_list_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : codeListManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `bbie` SET `code_list_manifest_id` = " + entry.getValue() + " WHERE `code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bbie_sc` SET `code_list_manifest_id` = " + entry.getValue() + " WHERE `code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bdt_pri_restri` SET `code_list_manifest_id` = " + entry.getValue() + " WHERE `code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `bdt_sc_pri_restri` SET `code_list_manifest_id` = " + entry.getValue() + " WHERE `code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_manifest` SET `code_list_manifest_id` = " + entry.getValue() + " WHERE `code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_manifest` SET `based_code_list_manifest_id` = " + entry.getValue() + " WHERE `based_code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_manifest` SET `replacement_code_list_manifest_id` = " + entry.getValue() + " WHERE `replacement_code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_manifest` SET `prev_code_list_manifest_id` = " + entry.getValue() + " WHERE `prev_code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_manifest` SET `next_code_list_manifest_id` = " + entry.getValue() + " WHERE `next_code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value_manifest` SET `code_list_manifest_id` = " + entry.getValue() + " WHERE `code_list_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `module_code_list_manifest` SET `code_list_manifest_id` = " + entry.getValue() + " WHERE `code_list_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"code_list_manifest_id\": " + entry.getKey() + "', '\"code_list_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_code_list_manifest_id\": " + entry.getKey() + "', '\"based_code_list_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_code_list_manifest_id\": " + entry.getKey() + "', '\"replacement_code_list_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_code_list_manifest_id\": " + entry.getKey() + "', '\"prev_code_list_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_code_list_manifest_id\": " + entry.getKey() + "', '\"next_code_list_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingCodeListValue(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> codeListValueIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `code_list_value`.`code_list_value_id` FROM `code_list_value` " +
                    "WHERE `code_list_value`.`code_list_value_id` > " + metadata.getTable("code_list_value").getMaxId() + " AND `code_list_value`.`code_list_value_id` < " + DELTA)) {
                while (rs.next()) {
                    long codeListValueId = rs.getLong("code_list_value_id");
                    codeListValueIdMap.put(codeListValueId, codeListValueId + DELTA);
                }
            }
        }

        Map<Long, Long> codeListValueManifestIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `code_list_value_manifest`.`code_list_value_manifest_id` FROM `code_list_value_manifest` " +
                    "WHERE `code_list_value_manifest`.`code_list_value_manifest_id` > " + metadata.getTable("code_list_value_manifest").getMaxId() + " AND `code_list_value_manifest`.`code_list_value_manifest_id` < " + DELTA)) {
                while (rs.next()) {
                    long codeListValueManifestId = rs.getLong("code_list_value_manifest_id");
                    codeListValueManifestIdMap.put(codeListValueManifestId, codeListValueManifestId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + codeListValueIdMap.size() + " target `code_list_value` record(s)");
        progress.report("\t\tFound " + codeListValueManifestIdMap.size() + " target `code_list_value_manifest` record(s)");

        for (Map.Entry<Long, Long> entry : codeListValueIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `code_list_value` SET `code_list_value_id` = " + entry.getValue() + " WHERE `code_list_value_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `based_code_list_value_id` = " + entry.getValue() + " WHERE `based_code_list_value_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `replacement_code_list_value_id` = " + entry.getValue() + " WHERE `replacement_code_list_value_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `prev_code_list_value_id` = " + entry.getValue() + " WHERE `prev_code_list_value_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `next_code_list_value_id` = " + entry.getValue() + " WHERE `next_code_list_value_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value_manifest` SET `code_list_value_id` = " + entry.getValue() + " WHERE `code_list_value_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"code_list_value_id\": " + entry.getKey() + "', '\"code_list_value_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_code_list_value_id\": " + entry.getKey() + "', '\"based_code_list_value_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_code_list_value_id\": " + entry.getKey() + "', '\"replacement_code_list_value_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_code_list_value_id\": " + entry.getKey() + "', '\"prev_code_list_value_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_code_list_value_id\": " + entry.getKey() + "', '\"next_code_list_value_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }

        for (Map.Entry<Long, Long> entry : codeListValueManifestIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `code_list_value_manifest` SET `code_list_value_manifest_id` = " + entry.getValue() + " WHERE `code_list_value_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value_manifest` SET `based_code_list_value_manifest_id` = " + entry.getValue() + " WHERE `based_code_list_value_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value_manifest` SET `replacement_code_list_value_manifest_id` = " + entry.getValue() + " WHERE `replacement_code_list_value_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value_manifest` SET `prev_code_list_value_manifest_id` = " + entry.getValue() + " WHERE `prev_code_list_value_manifest_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value_manifest` SET `next_code_list_value_manifest_id` = " + entry.getValue() + " WHERE `next_code_list_value_manifest_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"code_list_value_manifest_id\": " + entry.getKey() + "', '\"code_list_value_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"based_code_list_value_manifest_id\": " + entry.getKey() + "', '\"based_code_list_value_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"replacement_code_list_value_manifest_id\": " + entry.getKey() + "', '\"replacement_code_list_value_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_code_list_value_manifest_id\": " + entry.getKey() + "', '\"prev_code_list_value_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_code_list_value_manifest_id\": " + entry.getKey() + "', '\"next_code_list_value_manifest_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingLog(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> logIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `log`.`log_id` FROM `log` " +
                    "WHERE `log`.`log_id` > " + metadata.getTable("log").getMaxId() + " AND `log`.`log_id` < " + DELTA)) {
                while (rs.next()) {
                    long logId = rs.getLong("log_id");
                    logIdMap.put(logId, logId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + logIdMap.size() + " target `log` record(s)");

        for (Map.Entry<Long, Long> entry : logIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `acc_manifest` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list_manifest` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_manifest` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
                statement.execute("UPDATE `log` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
                statement.execute("UPDATE `log` SET `prev_log_id` = " + entry.getValue() + " WHERE `prev_log_id` = " + entry.getKey());
                statement.execute("UPDATE `log` SET `next_log_id` = " + entry.getValue() + " WHERE `next_log_id` = " + entry.getKey());
                statement.execute("UPDATE `xbt_manifest` SET `log_id` = " + entry.getValue() + " WHERE `log_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"log_id\": " + entry.getKey() + "', '\"log_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"prev_log_id\": " + entry.getKey() + "', '\"prev_log_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"next_log_id\": " + entry.getKey() + "', '\"next_log_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingNamespace(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> namespaceIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `namespace`.`namespace_id` FROM `namespace` " +
                    "WHERE `namespace`.`namespace_id` > " + metadata.getTable("namespace").getMaxId() + " AND `namespace`.`namespace_id` < " + DELTA)) {
                while (rs.next()) {
                    long namespaceId = rs.getLong("namespace_id");
                    namespaceIdMap.put(namespaceId, namespaceId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + namespaceIdMap.size() + " target `namespace` record(s)");

        for (Map.Entry<Long, Long> entry : namespaceIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `acc` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `module` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `namespace` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
                statement.execute("UPDATE `release` SET `namespace_id` = " + entry.getValue() + " WHERE `namespace_id` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"namespace_id\": " + entry.getKey() + "', '\"namespace_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

    private void relocatingLibrary(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> libraryIdMap = new HashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `library`.`library_id` FROM `library` " +
                    "WHERE `library`.`library_id` > " + metadata.getTable("library").getMaxId() + " AND `library`.`library_id` < " + DELTA)) {
                while (rs.next()) {
                    long libraryId = rs.getLong("library_id");
                    libraryIdMap.put(libraryId, libraryId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + libraryIdMap.size() + " target `library` record(s)");

        for (Map.Entry<Long, Long> entry : libraryIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `release` SET `library_id` = " + entry.getValue() + " WHERE `library_id` = " + entry.getKey());
            }
        }
    }

    private void relocatingAppUser(Connection conn, Info info) throws SQLException {
        Metadata metadata = info.getMigration().getMetadata();

        Map<Long, Long> endUserIdMap = new LinkedHashMap();
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT `app_user_id` FROM `app_user` WHERE `app_user_id` < " + DELTA + " AND `is_developer` = 0")) {
                while (rs.next()) {
                    long appUserId = rs.getLong("app_user_id");
                    endUserIdMap.put(appUserId, appUserId + DELTA);
                }
            }
        }

        progress.report("\t\tFound " + endUserIdMap.size() + " target `app_user` record(s)");

        for (Map.Entry<Long, Long> entry : endUserIdMap.entrySet()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `app_oauth2_user` SET `app_user_id` = " + entry.getValue() + " WHERE `app_user_id` = " + entry.getKey());
                statement.execute("UPDATE `app_user` SET `app_user_id` = " + entry.getValue() + " WHERE `app_user_id` = " + entry.getKey());
                statement.execute("UPDATE `user_tenant` SET `app_user_id` = " + entry.getValue() + " WHERE `app_user_id` = " + entry.getKey());

                statement.execute("UPDATE `abie` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `abie` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `acc` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `acc` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `acc` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `acc_manifest_tag` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list_value` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list_value` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `agency_id_list_value` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `asbie` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `asbie` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `asbie_bizterm` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `asbie_bizterm` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `asbiep` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `asbiep` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `ascc` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `ascc` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `ascc` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `ascc_bizterm` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `ascc_bizterm` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `asccp` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `asccp_manifest_tag` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bbie` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bbie` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `bbie_bizterm` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bbie_bizterm` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `bbie_sc` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bbie_sc` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `bbiep` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bbiep` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `bcc` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bcc` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `bcc` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `bcc_bizterm` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bcc_bizterm` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `bccp` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `bccp_manifest_tag` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `biz_ctx` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `biz_ctx` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `business_term` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `business_term` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `code_list_value` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `comment` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `ctx_category` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `ctx_category` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `ctx_scheme` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `ctx_scheme` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `dt` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_manifest_tag` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `dt_sc` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `exception` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `library` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `library` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `log` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `message` SET `sender_id` = " + entry.getValue() + " WHERE `sender_id` = " + entry.getKey());
                statement.execute("UPDATE `message` SET `recipient_id` = " + entry.getValue() + " WHERE `recipient_id` = " + entry.getKey());
                statement.execute("UPDATE `module` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `module_acc_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_acc_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_agency_id_list_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_agency_id_list_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_asccp_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_asccp_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_bccp_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_bccp_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_blob_content_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_blob_content_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_code_list_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_code_list_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_dt_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_dt_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_set` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_set` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_set_release` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_set_release` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `module_xbt_manifest` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `module_xbt_manifest` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `namespace` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `namespace` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `namespace` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `release` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `release` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `tag` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `tag` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `top_level_asbiep` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `top_level_asbiep` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
                statement.execute("UPDATE `xbt` SET `created_by` = " + entry.getValue() + " WHERE `created_by` = " + entry.getKey());
                statement.execute("UPDATE `xbt` SET `owner_user_id` = " + entry.getValue() + " WHERE `owner_user_id` = " + entry.getKey());
                statement.execute("UPDATE `xbt` SET `last_updated_by` = " + entry.getValue() + " WHERE `last_updated_by` = " + entry.getKey());
            }

            try (Statement statement = conn.createStatement()) {
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"app_user_id\": " + entry.getKey() + "', '\"app_user_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"created_by\": " + entry.getKey() + "', '\"created_by\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"last_updated_by\": " + entry.getKey() + "', '\"last_updated_by\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"owner_user_id\": " + entry.getKey() + "', '\"owner_user_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"sender_id\": " + entry.getKey() + "', '\"sender_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
                statement.execute("UPDATE `log` SET `snapshot` = REPLACE(`snapshot`, '\"recipient_id\": " + entry.getKey() + "', '\"recipient_id\": " + entry.getValue() + "') WHERE `log_id` > " + metadata.getTable("log").getMaxId());
            }
        }
    }

}
