package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.jooq.types.ULong;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;
import static org.oagi.score.e2e.obj.ObjectHelper.sha256;

class DSLContextBusinessInformationEntityAPIImplTest {

    @Test
    void includesOnlyTheBaseChainThroughTheDeclaringAcc() {
        List<ULong> chain = List.of(id(261633), id(261531), id(261530));
        String root = "ASCCP-223067>ACC-261633";
        assertEquals(root + ">BCC-1", DSLContextBusinessInformationEntityAPIImpl
                .elementBbiePath(root, chain, id(261633), id(1)));
        assertEquals(root + ">ACC-261531>BCC-2", DSLContextBusinessInformationEntityAPIImpl
                .elementBbiePath(root, chain, id(261531), id(2)));
        assertEquals(root + ">ACC-261531>ACC-261530>BCC-131600", DSLContextBusinessInformationEntityAPIImpl
                .elementBbiePath(root, chain, id(261530), id(131600)));
        assertThrows(IllegalArgumentException.class, () -> DSLContextBusinessInformationEntityAPIImpl
                .elementBbiePath(root, chain, id(999), id(1)));
    }

    @Test
    void findsBbieScPrimitiveUsingSeparatePropertyAndRepresentationTerms() {
        DSLContext records = DSL.using(SQLDialect.MARIADB);
        MockConnection connection = new MockConnection(context -> {
            assertTrue(Arrays.asList(context.bindings()).contains("List Version"));
            assertTrue(Arrays.asList(context.bindings()).contains("Identifier"));

            var result = records.newResult(XBT_MANIFEST.XBT_ID);
            result.add(records.newRecord(XBT_MANIFEST.XBT_ID).values(id(777)));
            return new MockResult[]{new MockResult(1, result)};
        });

        var api = new DSLContextBusinessInformationEntityAPIImpl(DSL.using(connection, SQLDialect.MARIADB), null);

        assertEquals(BigInteger.valueOf(777), api
                .getBbieScXbtIdByBbiePathAndPropertyAndRepresentationTerm(
                        BigInteger.ONE,
                        "ASCCP-1>ACC-1>BCC-1",
                        "List Version",
                        "Identifier"));
    }

    @Test
    void persistsEditorCompatiblePathsAndHashesForInheritedBbieAndItsSc() {
        DSLContext records = DSL.using(SQLDialect.MARIADB);
        String rootPath = "ASCCP-223067>ACC-261633";
        String bbiePath = rootPath + ">ACC-261531>ACC-261530>BCC-131600";
        String bbiepPath = bbiePath + ">BCCP-100";
        String scPath = bbiepPath + ">DT-200>DT_SC-300";
        String identifierBbiePath = rootPath + ">ACC-261531>ACC-261530>BCC-131601";
        String identifierScPath = identifierBbiePath + ">BCCP-101>DT-201>DT_SC-301";
        List<List<Object>> inserts = new ArrayList<>();
        List<String> insertSql = new ArrayList<>();

        MockConnection connection = new MockConnection(context -> {
            String sql = context.sql().replace("`oagi`.", "");
            if (sql.startsWith("insert")) {
                inserts.add(Arrays.asList(context.bindings()));
                insertSql.add(sql);
                if (sql.contains("insert into `bbiep`")) {
                    var result = records.newResult(BBIEP.BBIEP_ID);
                    result.add(records.newRecord(BBIEP.BBIEP_ID).values(id(10)));
                    return new MockResult[]{new MockResult(1, result)};
                }
                return new MockResult[]{new MockResult(1, null)};
            }
            if (sql.contains("from `abie`")) {
                var result = records.newResult(ABIE);
                var root = records.newRecord(ABIE);
                root.setAbieId(id(1));
                root.setPath(rootPath);
                root.setBasedAccManifestId(id(261633));
                result.add(root);
                return new MockResult[]{new MockResult(1, result)};
            }
            if (sql.contains("from `acc_manifest`")) {
                long current = Long.parseLong(context.bindings()[0].toString());
                ULong base = current == 261633 ? id(261531) : current == 261531 ? id(261530) : null;
                var result = records.newResult(ACC_MANIFEST.BASED_ACC_MANIFEST_ID);
                result.add(records.newRecord(ACC_MANIFEST.BASED_ACC_MANIFEST_ID).values(base));
                return new MockResult[]{new MockResult(1, result)};
            }
            if (sql.contains("join `bccp`")) {
                List<Object> bindings = Arrays.asList(context.bindings());
                assertTrue(bindings.contains("Identifier"));
                var result = records.newResult(BBIE.BBIE_ID);
                result.add(records.newRecord(BBIE.BBIE_ID).values(id(21)));
                return new MockResult[]{new MockResult(1, result)};
            }
            if (sql.contains("from `bbie`")) {
                boolean identifierBbie = Arrays.stream(context.bindings())
                        .anyMatch(value -> "21".equals(value.toString()));
                var result = records.newResult(BBIE);
                var bbie = records.newRecord(BBIE);
                bbie.setBbieId(identifierBbie ? id(21) : id(20));
                bbie.setBasedBccManifestId(identifierBbie ? id(131601) : id(131600));
                bbie.setPath(identifierBbie ? identifierBbiePath : bbiePath);
                result.add(bbie);
                return new MockResult[]{new MockResult(1, result)};
            }
            if (sql.contains("join `dt_sc_manifest`")) {
                List<Object> bindings = Arrays.asList(context.bindings());
                boolean schemeAgencyIdentifier = bindings.contains("Scheme Agency");
                if (schemeAgencyIdentifier) {
                    assertTrue(bindings.contains("Identifier"));
                }
                var result = records.newResult(DT_SC_MANIFEST.DT_SC_MANIFEST_ID,
                        BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.BDT_MANIFEST_ID);
                result.add(records.newRecord(DT_SC_MANIFEST.DT_SC_MANIFEST_ID,
                        BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.BDT_MANIFEST_ID)
                        .values(schemeAgencyIdentifier ? id(301) : id(300),
                                schemeAgencyIdentifier ? id(101) : id(100),
                                schemeAgencyIdentifier ? id(201) : id(200)));
                return new MockResult[]{new MockResult(1, result)};
            }
            if (sql.contains("from `bcc_manifest`")) {
                var result = records.newResult(BCC_MANIFEST.BCC_MANIFEST_ID,
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID, BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        BCC.CARDINALITY_MIN, BCC.CARDINALITY_MAX);
                result.add(records.newRecord(BCC_MANIFEST.BCC_MANIFEST_ID,
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID, BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        BCC.CARDINALITY_MIN, BCC.CARDINALITY_MAX)
                        .values(id(131600), id(100), id(261530), 0, 1));
                return new MockResult[]{new MockResult(1, result)};
            }
            throw new AssertionError("Unexpected SQL: " + sql);
        });

        var api = new DSLContextBusinessInformationEntityAPIImpl(DSL.using(connection, SQLDialect.MARIADB), null);
        api.createBbieNodesForUsedElements(BigInteger.valueOf(861), BigInteger.ONE);
        api.createBbieScForFirstBbie(BigInteger.valueOf(861), BigInteger.ONE);

        assertEquals(3, inserts.size());
        for (String path : List.of(bbiePath, bbiepPath, scPath)) {
            assertTrue(inserts.stream().anyMatch(values -> values.contains(path) && values.contains(sha256(path))), path);
        }
        assertUsedBbieScInsert(insertSql, inserts, scPath);

        api.createBbieScForBbieAndDtSc(BigInteger.valueOf(861), BigInteger.ONE,
                "Identifier", "Scheme Agency", "Identifier");
        assertEquals(4, inserts.size());
        assertTrue(inserts.stream().anyMatch(values -> values.contains(identifierScPath)
                && values.contains(sha256(identifierScPath))), inserts::toString);
        assertUsedBbieScInsert(insertSql, inserts, identifierScPath);

    }

    private static void assertUsedBbieScInsert(List<String> insertSql, List<List<Object>> inserts, String path) {
        for (int i = 0; i < insertSql.size(); i++) {
            if (!insertSql.get(i).contains("insert into `bbie_sc`") || !inserts.get(i).contains(path)) {
                continue;
            }
            String columns = insertSql.get(i).substring(insertSql.get(i).indexOf('(') + 1,
                    insertSql.get(i).indexOf(')'));
            String[] columnNames = columns.split(",");
            for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++) {
                if (columnNames[columnIndex].contains("is_used")) {
                    assertEquals((byte) 1, inserts.get(i).get(columnIndex));
                    return;
                }
            }
            fail("The BBIE_SC insert does not contain is_used: " + insertSql.get(i));
        }
        fail("No BBIE_SC insert found for path: " + path);
    }

    private static ULong id(long value) {
        return ULong.valueOf(value);
    }
}
