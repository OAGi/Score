package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchPredicate;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.TextSearchOperator;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/** The statements a core component tree search sends: which components and columns each attribute reads. */
class JooqCcSearchQueryRepositoryTest {

    private static final ReleaseId RELEASE = new ReleaseId(BigInteger.valueOf(10));

    private final List<String> sql = new ArrayList<>();

    private JooqCcSearchQueryRepository repository() {
        sql.clear();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        ScoreUser user = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester", "tester@example.com",
                false, List.of(ScoreRole.END_USER));
        return new JooqCcSearchQueryRepository(DSL.using(new MockConnection(provider), SQLDialect.MYSQL),
                user, mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));
    }

    private static CcSearchPredicate predicate(String attribute, TextSearchOperator operator, String value) {
        return new CcSearchPredicate("p", attribute, operator, value, false);
    }

    @Test
    void definitionReadsEveryKindOfComponentOfTheRelease() {
        repository().searchNodes(RELEASE, predicate("definition", TextSearchOperator.CONTAINS, "price"));

        assertEquals(7, sql.size());
        assertTrue(sql.stream().allMatch(s -> s.contains("`release_id` = ?") && s.contains("`definition` like")));
    }

    @Test
    void theOtherDefinitionsReadOnlyTheKindsThatHaveThem() {
        repository().searchNodes(RELEASE, predicate("association_definition", TextSearchOperator.EQUALS, "x"));
        assertEquals(2, sql.size());
        assertTrue(sql.stream().anyMatch(s -> s.contains("`ascc_manifest`")) && sql.stream().anyMatch(s -> s.contains("`bcc_manifest`")));

        repository().searchNodes(RELEASE, predicate("component_definition", TextSearchOperator.EQUALS, "x"));
        assertEquals(3, sql.size());

        repository().searchNodes(RELEASE, predicate("type_definition", TextSearchOperator.EQUALS, "x"));
        assertEquals(2, sql.size());
        assertTrue(sql.stream().anyMatch(s -> s.contains("`acc_manifest`")) && sql.stream().anyMatch(s -> s.contains("`dt_manifest`")));
    }

    @Test
    void definitionSourceReadsTheSourceColumn() {
        repository().searchNodes(RELEASE, predicate("definition_source", TextSearchOperator.CONTAINS, "x"));

        assertEquals(7, sql.size());
        assertTrue(sql.stream().allMatch(s -> s.contains("`definition_source` like")));
    }

    @Test
    void theValuesAreReadFromTheKindsThatHaveThem() {
        repository().searchNodes(RELEASE, predicate("default_value", TextSearchOperator.EQUALS, "x"));
        assertEquals(3, sql.size());
        assertTrue(sql.stream().anyMatch(s -> s.contains("`bcc`.`default_value`")) && sql.stream().anyMatch(s -> s.contains("`bccp`.`default_value`"))
                && sql.stream().anyMatch(s -> s.contains("`dt_sc`.`default_value`")));

        repository().searchNodes(RELEASE, predicate("fixed_value", TextSearchOperator.EQUALS, "x"));
        assertEquals(3, sql.size());
        assertTrue(sql.stream().allMatch(s -> s.contains("`fixed_value` like")));
    }

    @Test
    void theContentComponentDefinitionIsThatOfADataType() {
        repository().searchNodes(RELEASE, predicate("content_component_definition", TextSearchOperator.CONTAINS, "x"));

        assertEquals(1, sql.size());
        assertTrue(sql.get(0).contains("from `dt_manifest`") && sql.get(0).contains("`dt`.`content_component_definition` like"));
    }

    @Test
    void aFourByteCharacterMatchesNothingAndIsNotSent() {
        var result = repository().searchNodes(RELEASE, predicate("definition", TextSearchOperator.CONTAINS, "a😀"));

        assertTrue(result.accs().isEmpty());
        assertTrue(sql.isEmpty());
    }

    @Test
    void anUnknownAttributeIsRefused() {
        assertThrows(IllegalArgumentException.class, () ->
                repository().searchNodes(RELEASE, predicate("password", TextSearchOperator.EQUALS, "x")));
    }

    @Test
    void theReleaseIsThatOfTheComponentTheTreeIsRootedAt() {
        var asccp = new org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId(BigInteger.valueOf(7));
        var dt = new org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId(BigInteger.valueOf(8));

        assertEquals(null, repository().getReleaseOfRoot(
                new org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest(null, asccp, null, null, List.of())));
        assertTrue(sql.get(0).contains("from `asccp_manifest`"));

        repository().getReleaseOfRoot(
                new org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest(null, null, null, dt, List.of()));
        assertTrue(sql.get(0).contains("from `dt_manifest`"));
    }

    @Test
    void theDefaultValueDomainIsComparedOnlyForTheAllowedPrimitivesTheRulePicked() {
        var dtQuery = mock(org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository.class);
        var picked = new org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord(
                new org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId(BigInteger.valueOf(7)), RELEASE,
                new org.oagi.score.gateway.http.api.cc_management.model.dt.DtId(BigInteger.valueOf(5)), "Token", null, "token", null, null, null, null, true);
        var other = new org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord(
                new org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId(BigInteger.valueOf(8)), RELEASE,
                new org.oagi.score.gateway.http.api.cc_management.model.dt.DtId(BigInteger.valueOf(5)), "String", null, "string", null, null, null, null, false);
        org.mockito.Mockito.when(dtQuery.getDtAwdPriSummaryList(RELEASE)).thenReturn(List.of(other, picked));
        var factory = mock(RepositoryFactory.class);
        org.mockito.Mockito.when(factory.dtQueryRepository(org.mockito.ArgumentMatchers.any())).thenReturn(dtQuery);
        sql.clear();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT) + " " + java.util.Arrays.toString(ctx.bindings()));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        ScoreUser user = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester", "tester@example.com",
                false, List.of(ScoreRole.END_USER));
        var repository = new JooqCcSearchQueryRepository(DSL.using(new MockConnection(provider), SQLDialect.MYSQL), user, factory);

        repository.searchNodes(RELEASE, predicate("value_domain", TextSearchOperator.EQUALS, "token"));

        String compared = sql.stream().filter(s -> s.contains("from `score`.`dt_awd_pri`") || s.contains("from `dt_awd_pri`")).findFirst().orElse("");
        assertTrue(compared.contains("coalesce(`score`.`xbt`.`name`") || compared.contains("coalesce(`xbt`.`name`"), compared);
        assertTrue(compared.contains("`dt_awd_pri_id` in (?)") && compared.contains("[7,") , compared);
        assertTrue(compared.contains(" like "), compared);

        repository.searchNodes(RELEASE, predicate("value_domain_restriction", TextSearchOperator.EQUALS, "Code List"));
        assertTrue(sql.stream().anyMatch(s -> s.contains("case when") && s.contains("'primitive'") && s.contains("'code list'")));
    }
}
