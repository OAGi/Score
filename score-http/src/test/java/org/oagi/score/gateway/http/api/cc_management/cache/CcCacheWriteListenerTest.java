package org.oagi.score.gateway.http.api.cc_management.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CcCacheWriteListenerTest {

    @Test
    void writesToWatchedTablesAreDetected() {
        assertTrue(CcCacheWriteListener.writesToWatchedTable("insert into `oagi`.`acc_manifest` (`a`) values (?)"));
        assertTrue(CcCacheWriteListener.writesToWatchedTable("update `oagi`.`acc` set `state` = ? where `acc_id` = ?"));
        assertTrue(CcCacheWriteListener.writesToWatchedTable("delete from `oagi`.`seq_key` where `seq_key_id` = ?"));
        assertTrue(CcCacheWriteListener.writesToWatchedTable("  UPDATE acc SET state = ?"));
        assertTrue(CcCacheWriteListener.writesToWatchedTable("insert ignore into `oagi`.`release` values (?)"));
        assertTrue(CcCacheWriteListener.writesToWatchedTable("replace into `oagi`.`log` values (?)"));
        assertTrue(CcCacheWriteListener.writesToWatchedTable("update \"oagi\".\"dt_sc_manifest\" set a = ?"));
    }

    @Test
    void otherStatementsAreIgnored() {
        assertFalse(CcCacheWriteListener.writesToWatchedTable("select * from `oagi`.`acc`"));
        assertFalse(CcCacheWriteListener.writesToWatchedTable("update `oagi`.`business_term` set a = ?"));
        assertFalse(CcCacheWriteListener.writesToWatchedTable("insert into `oagi`.`access_log` values (?)"));
        assertFalse(CcCacheWriteListener.writesToWatchedTable("update `oagi`.`accounts` set a = ?"));
        assertFalse(CcCacheWriteListener.writesToWatchedTable(null));
        assertFalse(CcCacheWriteListener.writesToWatchedTable(""));
    }

}
