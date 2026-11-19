package org.oagi.score.gateway.http.api.release_management.service.migrationload.format;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Executable;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Info;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Metadata;
import org.oagi.score.gateway.http.api.release_management.dataload.info.Migration;
import org.oagi.score.gateway.http.api.release_management.dataload.script.ScriptSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReleasePackageFormatV1Test {

    private final ReleasePackageFormatV1 format = new ReleasePackageFormatV1();

    private static Executable script(String name) {
        Executable executable = new Executable();
        executable.setScript(name);
        return executable;
    }

    private static Info info(String schemaVersion, String... scripts) {
        Info info = new Info();
        info.setVersion("1.1");
        info.setSchemaVersion(schemaVersion);
        Migration migration = new Migration();
        migration.setProcesses(java.util.Arrays.stream(scripts).map(ReleasePackageFormatV1Test::script).toList());
        migration.setMetadata(new Metadata());
        info.setMigration(migration);
        return info;
    }

    private static ScriptSource scriptsOf(String... present) {
        Set<String> names = Set.of(present);
        return new ScriptSource() {
            @Override
            public boolean exists(String script) {
                return names.contains(script);
            }

            @Override
            public InputStream open(String script) {
                return new ByteArrayInputStream(new byte[0]);
            }
        };
    }

    @Test
    void aPackageWhoseScriptsAreSqlAndAreThereHasNoProblem() {
        assertEquals(List.of(), format.problemsOf(info(null, "mig.sql"), scriptsOf("mig.sql")));
    }

    @Test
    void aScriptThatIsMissingOrIsNotSqlIsAProblemOfThePackage() {
        List<String> problems = format.problemsOf(info(null, "gone.sql", "code.kts", "ok.sql"), scriptsOf("code.kts", "ok.sql"));

        assertEquals(2, problems.size(), problems.toString());
        assertTrue(problems.get(0).contains("'gone.sql'") && problems.get(0).contains("missing"));
        assertTrue(problems.get(1).contains("'code.kts'") && problems.get(1).contains("not SQL"));
    }

    @Test
    void theVersionOfTheSchemaIsTheOneThePackageSaysAndTheLatestWhenItSaysNone() {
        assertEquals(java.util.Optional.of("3.4.0"), format.schemaVersionOf(info("3.4.0", "m.sql")));
        assertEquals(java.util.Optional.empty(), format.schemaVersionOf(info(null, "m.sql")));
    }

    @Test
    void theStatementsLeaveTheUserOfTheInstanceAndTheStructureOfTheTablesOut() {
        var statements = format.statements();

        assertEquals("CREATE TRIGGER `t` AFTER INSERT ON `a` FOR EACH ROW BEGIN SET @x = 1; END",
                statements.apply("CREATE DEFINER=`oagi`@`%` TRIGGER `t` AFTER INSERT ON `a` FOR EACH ROW BEGIN SET @x = 1; END"));
        assertEquals("DELETE FROM `xbt`", statements.apply("DROP TABLE IF EXISTS `xbt`"));
        assertEquals("DO 0", statements.apply("CREATE TABLE `xbt` (a int)"));
        assertEquals("INSERT INTO `xbt` VALUES (1)", statements.apply("INSERT INTO `xbt` VALUES (1)"));
    }

    @Test
    void theTablesTheUsersRecordsAreNumberedFromAreThoseTheGeneratorPins() {
        assertEquals(31, format.numberedFromDelta().size());
        assertTrue(format.numberedFromDelta().containsAll(List.of("acc", "app_user", "library", "release")));
    }

    @Test
    void whatIsCopiedIsThePlanOfTheFormat() {
        List<CopyStep> plan = format.copyPlan(List.of("acc", "abie", "xbt", "migration_load_job"), List.of("acc", "abie", "xbt", "migration_load_job"));

        assertEquals(CopyKind.ALL, plan.stream().filter(s -> s.table().equals("abie")).findFirst().orElseThrow().kind());
        assertEquals(CopyKind.FROM_DELTA, plan.stream().filter(s -> s.table().equals("acc")).findFirst().orElseThrow().kind());
        assertEquals(CopyKind.OF_PACKAGE, plan.stream().filter(s -> s.table().equals("xbt")).findFirst().orElseThrow().kind());
        assertEquals(CopyKind.STAYS_IN_PLACE, plan.stream().filter(s -> s.table().equals("migration_load_job")).findFirst().orElseThrow().kind());
    }
}
