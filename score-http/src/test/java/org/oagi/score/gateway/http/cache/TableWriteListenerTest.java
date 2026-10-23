package org.oagi.score.gateway.http.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableWriteListenerTest {

    private static final java.util.Set<String> TABLES = new org.oagi.score.gateway.http.api.cc_management.model.CcRecordKeys(
            (org.oagi.score.gateway.http.api.cc_management.repository.AffectedRecordsRepository) null).tables();


    @Test
    void writesToWatchedTablesAreDetected() {
        assertTrue(TableWriteListener.writesTo(TABLES, "insert into `oagi`.`acc_manifest` (`a`) values (?)"));
        assertTrue(TableWriteListener.writesTo(TABLES, "update `oagi`.`acc` set `state` = ? where `acc_id` = ?"));
        assertTrue(TableWriteListener.writesTo(TABLES, "delete from `oagi`.`seq_key` where `seq_key_id` = ?"));
        assertTrue(TableWriteListener.writesTo(TABLES, "  UPDATE acc SET state = ?"));
        assertTrue(TableWriteListener.writesTo(TABLES, "insert ignore into `oagi`.`release` values (?)"));
        assertTrue(TableWriteListener.writesTo(TABLES, "replace into `oagi`.`log` values (?)"));
        assertTrue(TableWriteListener.writesTo(TABLES, "update \"oagi\".\"dt_sc_manifest\" set a = ?"));
    }

    @Test
    void otherStatementsAreIgnored() {
        assertFalse(TableWriteListener.writesTo(TABLES, "select * from `oagi`.`acc`"));
        assertFalse(TableWriteListener.writesTo(TABLES, "update `oagi`.`business_term` set a = ?"));
        assertFalse(TableWriteListener.writesTo(TABLES, "insert into `oagi`.`access_log` values (?)"));
        assertFalse(TableWriteListener.writesTo(TABLES, "update `oagi`.`accounts` set a = ?"));
        assertFalse(TableWriteListener.writesTo(TABLES, null));
        assertFalse(TableWriteListener.writesTo(TABLES, ""));
    }

}
