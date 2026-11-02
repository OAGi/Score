package org.oagi.score.gateway.http.api.bie_management.service;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.objenesis.ObjenesisStd;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "Create Inherited BIE" and "Create BIE from existing BIE" carry the Facets, the Display Names and
 * the Business Term of the source BIE over to the new BIE, as the BIE copy does.
 */
class BieCreateFromExistingBieCopiedPropertiesTest {

    private static final TopLevelAsbiepId SOURCE = new TopLevelAsbiepId(BigInteger.TEN);

    private final List<String> sql = new ArrayList<>();
    private BieCreateFromExistingBieService service;
    private DSLContext dslContext;

    @BeforeEach
    void setUp() {
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            return new MockResult[]{new MockResult(1, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        dslContext = DSL.using(new MockConnection(provider), SQLDialect.MYSQL);
        service = new BieCreateFromExistingBieService();
        ReflectionTestUtils.setField(service, "dslContext", dslContext);
    }

    private void assertLastSqlMentions(String... columns) {
        String last = sql.get(sql.size() - 1);
        for (String column : columns) {
            assertTrue(last.contains(column), column + " is missing in: " + last);
        }
    }

    @Test
    void selectsTheCopiedColumnsOfTheSourceBie() throws Exception {
        invokeSelect("getAbieByOwnerTopLevelAsbiepId");
        assertLastSqlMentions("`biz_term`");

        invokeSelect("getAsbiepByOwnerTopLevelAsbiepId");
        assertLastSqlMentions("`biz_term`", "`display_name`");

        invokeSelect("getBbiepByOwnerTopLevelAsbiepId");
        assertLastSqlMentions("`biz_term`", "`display_name`");

        invokeSelect("getBbieByOwnerTopLevelAsbiepId");
        assertLastSqlMentions("`facet_min_length`", "`facet_max_length`", "`facet_pattern`");

        invokeSelect("getBbieScByOwnerTopLevelAsbiepId");
        assertLastSqlMentions("`facet_min_length`", "`facet_max_length`", "`facet_pattern`",
                "`biz_term`", "`display_name`");
    }

    @Test
    void insertsTheCopiedColumnsIntoTheNewBie() throws Exception {
        Object context = newContext();

        var abie = new BieCreateFromExistingBieService.BieCreateFromExistingBieAbie();
        abie.setPath("p");
        abie.setBasedAccManifestId(new org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId(BigInteger.ONE));
        abie.setBizTerm("abie term");
        insert(context, "insertAbie", abie);
        assertLastSqlMentions("`biz_term`");

        var asbiep = new BieCreateFromExistingBieService.BieCreateFromExistingBieAsbiep();
        asbiep.setPath("p");
        asbiep.setBasedAsccpManifestId(new org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId(BigInteger.ONE));
        asbiep.setRoleOfAbieId(new org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId(BigInteger.ONE));
        insert(context, "insertAsbiep", asbiep);
        assertLastSqlMentions("`biz_term`", "`display_name`");

        var bbiep = new BieCreateFromExistingBieService.BieCreateFromExistingBieBbiep();
        bbiep.setPath("p");
        bbiep.setBasedBccpManifestId(new org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId(BigInteger.ONE));
        insert(context, "insertBbiep", bbiep);
        assertLastSqlMentions("`biz_term`", "`display_name`");

        var bbie = new BieCreateFromExistingBieService.BieCreateFromExistingBieBbie();
        bbie.setPath("p");
        bbie.setFromAbieId(new org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId(BigInteger.ONE));
        bbie.setToBbiepId(new org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepId(BigInteger.ONE));
        bbie.setBasedBccManifestId(new org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId(BigInteger.ONE));
        bbie.setFacetMinLength(BigInteger.TWO);
        bbie.setFacetMaxLength(BigInteger.valueOf(5));
        bbie.setFacetPattern("[a-z]+");
        insert(context, "insertBbie", bbie);
        assertLastSqlMentions("`facet_min_length`", "`facet_max_length`", "`facet_pattern`");

        var bbieSc = new BieCreateFromExistingBieService.BieCreateFromExistingBieBbieSc();
        bbieSc.setPath("p");
        bbieSc.setBbieId(new org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId(BigInteger.ONE));
        bbieSc.setBasedDtScManifestId(new org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId(BigInteger.ONE));
        insert(context, "insertBbieSc", bbieSc);
        assertLastSqlMentions("`facet_min_length`", "`facet_max_length`", "`facet_pattern`",
                "`biz_term`", "`display_name`");
    }

    @Test
    void holdersKeepTheCopiedValues() {
        var bbie = new BieCreateFromExistingBieService.BieCreateFromExistingBieBbie();
        bbie.setFacetMinLength(BigInteger.TWO);
        bbie.setFacetPattern("[a-z]+");
        assertEquals(BigInteger.TWO, bbie.getFacetMinLength());
        assertEquals("[a-z]+", bbie.getFacetPattern());
    }

    private void invokeSelect(String name) throws Exception {
        Method method = BieCreateFromExistingBieService.class.getDeclaredMethod(name, TopLevelAsbiepId.class);
        method.setAccessible(true);
        method.invoke(service, SOURCE);
    }

    private Object newContext() throws Exception {
        Class<?> type = Class.forName(BieCreateFromExistingBieService.class.getName() + "$BieCreateFromExistingBieContext");
        Object context = new ObjenesisStd().newInstance(type);
        ReflectionTestUtils.setField(context, "this$0", service);
        ReflectionTestUtils.setField(context, "userId", new UserId(BigInteger.ONE));
        ReflectionTestUtils.setField(context, "timestamp", LocalDateTime.now());
        ReflectionTestUtils.setField(context, "targetTopLevelAsbiepId", new TopLevelAsbiepId(BigInteger.valueOf(11)));
        ReflectionTestUtils.setField(context, "sourceAsccpKey", "ASCCP-1");
        return context;
    }

    private void insert(Object context, String name, Object holder) throws Exception {
        Method method = null;
        for (Method m : context.getClass().getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                method = m;
            }
        }
        method.setAccessible(true);
        try {
            method.invoke(context, holder);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // the mocked database returns no generated key; the statement itself was already recorded
        }
    }
}
