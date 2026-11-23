package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.common.model.TextSearchOperator;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchAttribute;
import org.oagi.score.gateway.http.api.bie_management.model.bie_search.BieSearchPredicate;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
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

/**
 * The statements a BIE tree search sends: which rows and columns each attribute reads, and how the operator, the case
 * flag and the special characters of the text reach the database.
 */
class JooqBieSearchQueryRepositoryTest {

    private static final TopLevelAsbiepId BIE = new TopLevelAsbiepId(BigInteger.valueOf(1015));

    private final List<String> sql = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();

    private JooqBieSearchQueryRepository repository() {
        sql.clear();
        bindings.clear();
        MockDataProvider provider = (MockExecuteContext ctx) -> {
            sql.add(ctx.sql().toLowerCase(Locale.ROOT));
            bindings.add(List.of(ctx.bindings()));
            return new MockResult[]{new MockResult(0, DSL.using(SQLDialect.MYSQL).newResult())};
        };
        ScoreUser user = new ScoreUser(new UserId(BigInteger.TEN), "tester", "Tester", "tester@example.com",
                false, List.of(ScoreRole.END_USER));
        return new JooqBieSearchQueryRepository(DSL.using(new MockConnection(provider), SQLDialect.MYSQL),
                user, mock(RepositoryFactory.class, RETURNS_DEEP_STUBS));
    }

    private static BieSearchPredicate predicate(String attribute, TextSearchOperator operator, String value, boolean caseSensitive) {
        return new BieSearchPredicate("p", BieSearchAttribute.from(attribute), operator, value, caseSensitive);
    }

    private String from(String table) {
        return sql.stream().filter(s -> s.contains("from `" + table + "`")).findFirst().orElse("");
    }

    @Test
    void definitionReadsTheRowsThatHoldItAndOnlyTheRootAsbiep() {
        repository().searchNodes(BIE, predicate("definition", TextSearchOperator.CONTAINS, "price", false));

        assertEquals(4, sql.size());
        assertTrue(from("asbiep").contains("`top_level_asbiep`.`asbiep_id` = `score`.`asbiep`.`asbiep_id`")
                || from("asbiep").contains("join `score`.`top_level_asbiep`") || from("asbiep").contains("join `top_level_asbiep`"));
        assertTrue(from("asbie").contains("`asbie`.`definition` like"));
        assertTrue(from("bbie").contains("`bbie`.`definition` like"));
        assertTrue(from("bbie_sc").contains("`bbie_sc`.`definition` like"));
        assertTrue(sql.stream().noneMatch(s -> s.contains("`bbiep`.`definition`")));
    }

    @Test
    void remarkIsOnTheAsbiepTheBbiepAndTheSupplementaryComponent() {
        repository().searchNodes(BIE, predicate("remark", TextSearchOperator.EQUALS, "x", false));

        assertEquals(3, sql.size());
        assertTrue(from("asbiep").contains("`asbiep`.`remark`"));
        assertTrue(from("bbiep").contains("`bbiep`.`remark`"));
        assertTrue(from("bbie_sc").contains("`bbie_sc`.`remark`"));
    }

    @Test
    void businessTermIsTheAssignedTermsAndTheLegacyTextTogether() {
        repository().searchNodes(BIE, predicate("business_term", TextSearchOperator.CONTAINS, "x", false));

        assertEquals(6, sql.size());
        assertTrue(from("asbie").contains("asbie_bizterm") && from("asbie").contains("`business_term`.`business_term` like"));
        assertTrue(from("bbie").contains("bbie_bizterm") && from("bbie").contains("`business_term`.`business_term` like"));
        assertTrue(from("abie").contains("`abie`.`biz_term` like"));
        assertTrue(from("asbiep").contains("`asbiep`.`biz_term` like"));
        assertTrue(from("bbiep").contains("`bbiep`.`biz_term` like"));
        assertTrue(from("bbie_sc").contains("`bbie_sc`.`biz_term` like"));
    }

    @Test
    void valueDomainWithoutARestrictionLooksInThePrimitiveTheCodeListAndTheAgencyIdListOfBothRows() {
        repository().searchNodes(BIE, predicate("value_domain", TextSearchOperator.CONTAINS, "token", false));

        assertEquals(6, sql.size());
        assertEquals(3, sql.stream().filter(s -> s.contains("from `score`.`bbie` ") || s.contains("from `bbie` ")).count());
        assertEquals(3, sql.stream().filter(s -> s.contains("from `score`.`bbie_sc` ") || s.contains("from `bbie_sc` ")).count());
        assertEquals(2, sql.stream().filter(s -> s.contains("`xbt`.`name` like")).count());
        assertEquals(2, sql.stream().filter(s -> s.contains("`code_list`.`name` like")).count());
        assertEquals(2, sql.stream().filter(s -> s.contains("`agency_id_list`.`name` like")).count());
    }

    @Test
    void valueDomainRestrictionIsTheTypeThatTheRowHolds() {
        repository().searchNodes(BIE, predicate("value_domain_restriction", TextSearchOperator.EQUALS, "Code List", false));

        assertEquals(6, sql.size());
        assertTrue(sql.stream().allMatch(s -> s.contains(" like ")));
        assertEquals(2, sql.stream().filter(s -> s.contains("join `score`.`xbt_manifest`") || s.contains("join `xbt_manifest`")).count());
        assertEquals(2, sql.stream().filter(s -> s.contains("code_list_manifest")).count());
        assertEquals(2, sql.stream().filter(s -> s.contains("agency_id_list_manifest")).count());
        assertTrue(bindings.stream().allMatch(b -> b.contains("Code List")));
    }

    @Test
    void theTypesOfValueDomainAreOfferedWithoutReadingTheBie() {
        assertEquals(List.of("Primitive", "Code List", "Agency ID List"), repository().suggestValues(BIE, BieSearchAttribute.from("value_domain_restriction"), "", 30));
        assertEquals(List.of("Code List"), repository().suggestValues(BIE, BieSearchAttribute.from("value_domain_restriction"), "code", 30));
        assertTrue(sql.isEmpty());
    }

    @Test
    void businessTermValuesAreOfferedFromTheSamePlacesTheSearchReads() {
        assertEquals(List.of(), repository().suggestValues(BIE, BieSearchAttribute.from("business_term"), "pro", 30));

        assertEquals(6, sql.size());
        assertTrue(sql.stream().allMatch(s -> s.contains(" like ") && s.contains("limit")));
        assertTrue(from("asbie").contains("asbie_bizterm") && from("abie").contains("`abie`.`biz_term`"));
        assertTrue(bindings.stream().anyMatch(b -> b.stream().anyMatch(v -> String.valueOf(v).contains("pro"))));
    }

    @Test
    void anEmptyTextOffersEveryValueAndAFourByteCharacterOffersNone() {
        repository().suggestValues(BIE, BieSearchAttribute.from("business_term"), "", 30);
        assertEquals(6, sql.size());
        assertTrue(sql.stream().noneMatch(s -> s.contains(" like ")));

        assertEquals(List.of(), repository().suggestValues(BIE, BieSearchAttribute.from("business_term"), "\uD83D\uDE00", 30));
        assertTrue(sql.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repository().suggestValues(BIE, BieSearchAttribute.from("nope"), "", 30));
    }

    @Test
    void thePatternEscapesTheCharactersLikeGivesAMeaning() {
        repository().searchNodes(BIE, predicate("example", TextSearchOperator.STARTS_WITH, "50%_off!", false));

        assertTrue(bindings.get(0).contains("50!%!_off!!%"));
        assertTrue(sql.get(0).contains("like ? escape '!'"));
    }

    @Test
    void aCaseSensitiveSearchComparesBinary() {
        repository().searchNodes(BIE, predicate("example", TextSearchOperator.EQUALS, "ABC", true));

        assertTrue(sql.stream().allMatch(s -> s.contains("like binary ? escape '!'")));
    }

    @Test
    void notEqualsNeedsAValueSoAnAbsentPartIsNotDifferent() {
        repository().searchNodes(BIE, predicate("fixed_value", TextSearchOperator.NOT_EQUALS, "x", false));

        assertTrue(sql.stream().allMatch(s -> s.contains("is not null") && s.contains("not (")));
    }

    @Test
    void aFourByteCharacterMatchesNothingAndIsNotSent() {
        var result = repository().searchNodes(BIE, predicate("remark", TextSearchOperator.CONTAINS, "a😀", false));

        assertTrue(result.isEmpty());
        assertTrue(sql.isEmpty());
    }

    @Test
    void aFourByteCharacterIsDifferentFromEverythingThatIsStored() {
        repository().searchNodes(BIE, predicate("remark", TextSearchOperator.NOT_EQUALS, "a😀", false));

        assertEquals(3, sql.size());
        assertTrue(sql.stream().allMatch(s -> s.contains("is not null") && !s.contains("like")));
    }

    @Test
    void theDefaultValueDomainsOfTheReleaseAreOfferedToo() {
        repository().suggestValues(BIE, BieSearchAttribute.from("value_domain"), "", 30);
        // a place the BIE stores nothing for starts with the default of its data type
        assertTrue(sql.stream().anyMatch(s -> s.contains("from `top_level_asbiep`") && s.contains("`release_id`")));
    }

    @Test
    void anUnknownAttributeIsRefused() {
        assertThrows(IllegalArgumentException.class, () ->
                repository().searchNodes(BIE, predicate("password", TextSearchOperator.EQUALS, "x", false)));
    }
}
