package org.oagi.score.gateway.http.api.release_management.service.migrationload.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeepSchemaStructureTest {

    private final KeepSchemaStructure keep = new KeepSchemaStructure();

    @Test
    void aTableThatIsRemovedIsEmptiedInstead() {
        assertEquals("DELETE FROM `xbt`", keep.apply("DROP TABLE IF EXISTS `xbt`"));
        assertEquals("DELETE FROM `xbt_manifest`", keep.apply("  drop table if exists `xbt_manifest` ;"));
        assertEquals("DELETE FROM `module`", keep.apply("DROP TABLE `module`"));
    }

    @Test
    void theStatementThatMakesATableIsNotRun() {
        assertEquals("DO 0", keep.apply("CREATE TABLE `xbt_manifest` (\n  `xbt_manifest_id` bigint NOT NULL,\n  PRIMARY KEY (`xbt_manifest_id`)\n) ENGINE=InnoDB"));
        assertEquals("DO 0", keep.apply("create table if not exists `x` (a int)"));
    }

    @Test
    void whatIsNotAboutTheStructureOfATableIsAsItIsWritten() {
        for (String statement : new String[]{
                "INSERT INTO `xbt` (`a`) VALUES (1)",
                "INSERT INTO `t` VALUES ('CREATE TABLE x (a int)')",
                "LOCK TABLES `xbt` WRITE",
                "ALTER TABLE `xbt` DISABLE KEYS",
                "DROP TRIGGER IF EXISTS `t`",
                "CREATE TRIGGER `t` AFTER INSERT ON `a` FOR EACH ROW BEGIN SET @x = 1; END",
                "DELETE FROM `acc` WHERE `acc_id` < 100000000",
                "SET FOREIGN_KEY_CHECKS=0"}) {
            assertEquals(statement, keep.apply(statement));
        }
    }
}
