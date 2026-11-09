package org.oagi.score.gateway.http.api.context_management.context_scheme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.CreateContextSchemeRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.CreateContextSchemeValueRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.UpdateContextSchemeRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.UpdateContextSchemeValueRequest;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueDetailsRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeCommandRepository;
import org.oagi.score.gateway.http.api.context_management.context_scheme.repository.ContextSchemeQueryRepository;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Guards added for the Context Management audit (#1744 sibling class): server-side triplet
 * uniqueness on create/update, in-use guards on discard, and a value-removal guard on update.
 * These tests mock the repositories (no DB) and assert hard guards throw a 4xx-mapped
 * {@link IllegalArgumentException}, while the UI-confirmed soft warning remains writable.
 */
class ContextSchemeCommandServiceTest {

    private ContextSchemeCommandService service;
    private RepositoryFactory repositoryFactory;
    private ContextSchemeCommandRepository command;
    private ContextSchemeQueryRepository query;

    private static final ContextCategoryId CATEGORY_ID = new ContextCategoryId(BigInteger.valueOf(3));
    private static final ContextSchemeId SCHEME_ID = new ContextSchemeId(BigInteger.valueOf(10));
    private static final ContextSchemeValueId V1 = new ContextSchemeValueId(BigInteger.valueOf(101));
    private static final ContextSchemeValueId V2 = new ContextSchemeValueId(BigInteger.valueOf(102));

    private ScoreUser requester;

    @BeforeEach
    void setUp() {
        service = new ContextSchemeCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        command = mock(ContextSchemeCommandRepository.class);
        query = mock(ContextSchemeQueryRepository.class);
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);

        requester = new ScoreUser(new UserId(BigInteger.valueOf(42)), "tester", "Tester",
                "tester@example.com", true, List.of(ScoreRole.DEVELOPER));

        when(repositoryFactory.contextSchemeCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.contextSchemeQueryRepository(any())).thenReturn(query);
    }

    private CreateContextSchemeRequest createRequest() {
        return new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "Geopolitical",
                "desc", "OAGi", "1.0", List.of());
    }

    private UpdateContextSchemeRequest updateRequest(List<UpdateContextSchemeValueRequest> values) {
        return new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "GEO", "Geopolitical",
                "desc", "OAGi", "1.0", values);
    }

    // ----- blank name (#213) -----

    @Test
    void create_rejects_a_blank_name() {
        for (String name : new String[]{null, "", "   "}) {
            var request = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", name,
                    "desc", "OAGi", "1.0", List.of());
            assertThrows(IllegalArgumentException.class, () -> service.create(requester, request));
        }
        verifyNoInteractions(command);
    }

    @Test
    void update_rejects_a_blank_name() {
        for (String name : new String[]{null, "", "   "}) {
            var request = new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "GEO", name,
                    "desc", "OAGi", "1.0", List.of());
            assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        }
        verifyNoInteractions(command);
    }

    // ----- blank identifiers -----

    @Test
    void create_rejects_a_blank_scheme_id_agency_id_or_version() {
        for (String blank : new String[]{null, "", "   "}) {
            var noSchemeId = new CreateContextSchemeRequest(CATEGORY_ID, null, blank, "Geopolitical",
                    "desc", "OAGi", "1.0", List.of());
            var noAgency = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "Geopolitical",
                    "desc", blank, "1.0", List.of());
            var noVersion = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "Geopolitical",
                    "desc", "OAGi", blank, List.of());
            for (var request : List.of(noSchemeId, noAgency, noVersion)) {
                assertThrows(IllegalArgumentException.class, () -> service.create(requester, request));
            }
        }
        verifyNoInteractions(command);
    }

    @Test
    void update_rejects_a_blank_scheme_id_agency_id_or_version() {
        for (String blank : new String[]{null, "", "   "}) {
            var noSchemeId = new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, blank, "Geopolitical",
                    "desc", "OAGi", "1.0", List.of());
            var noAgency = new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "GEO", "Geopolitical",
                    "desc", blank, "1.0", List.of());
            var noVersion = new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "GEO", "Geopolitical",
                    "desc", "OAGi", blank, List.of());
            for (var request : List.of(noSchemeId, noAgency, noVersion)) {
                assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
            }
        }
        verifyNoInteractions(command);
    }

    // ----- blank value -----

    @Test
    void create_rejects_a_blank_value() {
        for (String blank : new String[]{"", "   "}) {
            var request = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "Geopolitical",
                    "desc", "OAGi", "1.0", List.of(new CreateContextSchemeValueRequest(blank, "meaning")));
            assertThrows(IllegalArgumentException.class, () -> service.create(requester, request));
        }
        verifyNoInteractions(command);
    }

    @Test
    void update_rejects_a_blank_value() {
        for (String blank : new String[]{"", "   "}) {
            var request = updateRequest(List.of(new UpdateContextSchemeValueRequest(null, blank, "meaning")));
            assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        }
        verifyNoInteractions(command);
    }

    // ----- surrounding spaces -----

    @Test
    void create_checks_and_saves_the_identifiers_without_the_surrounding_spaces() {
        var request = new CreateContextSchemeRequest(CATEGORY_ID, null, " GEO ", " Geopolitical ",
                "desc", " OAGi ", " 1.0 ", List.of());
        when(query.hasDuplicate("GEO", "OAGi", "1.0")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(requester, request));
        verify(query).hasDuplicate("GEO", "OAGi", "1.0");
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_checks_and_saves_the_identifiers_without_the_surrounding_spaces() {
        var request = new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, " GEO ", " Geopolitical ",
                "desc", " OAGi ", " 1.0 ", List.of());
        when(query.hasDuplicateExcludingCurrent(SCHEME_ID, "GEO", "OAGi", "1.0")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        verify(query).hasDuplicateExcludingCurrent(SCHEME_ID, "GEO", "OAGi", "1.0");
        verify(command, never()).update(any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ----- create: server-side uniqueness (B7) -----

    @Test
    void create_rejects_a_duplicate_triplet() {
        when(query.hasDuplicate(any(), any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(requester, createRequest()));
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_allows_a_name_warning_after_user_confirmation() {
        when(query.hasDuplicate(any(), any(), any())).thenReturn(false);
        when(query.hasDuplicateName(any(), any(), any(), any())).thenReturn(true);
        when(command.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(SCHEME_ID);

        assertEquals(SCHEME_ID, service.create(requester, createRequest()));
        verify(command).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_persists_a_unique_scheme() {
        when(query.hasDuplicate(any(), any(), any())).thenReturn(false);
        when(command.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(SCHEME_ID);

        assertEquals(SCHEME_ID, service.create(requester, createRequest()));
        verify(command).create(any(), any(), any(), any(), any(), any(), any());
    }

    // ----- update: self-excluding uniqueness (#1744) -----

    @Test
    void update_rejects_a_duplicate_triplet_of_another_scheme() {
        when(query.hasDuplicateExcludingCurrent(any(), any(), any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.update(requester, updateRequest(List.of())));
        verify(command, never()).update(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_allows_a_name_warning_after_user_confirmation() {
        when(query.hasDuplicateExcludingCurrent(any(), any(), any(), any())).thenReturn(false);
        when(query.hasDuplicateNameExcludingCurrent(any(), any(), any(), any(), any())).thenReturn(true);
        when(query.getContextSchemeValueList(any())).thenReturn(List.of());
        when(query.findUsedContextSchemeValueIds(any())).thenReturn(Set.of());

        service.update(requester, updateRequest(List.of()));

        verify(command).update(any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ----- update: value-removal guard (B3) -----

    @Test
    void update_rejects_removing_a_context_scheme_value_that_is_in_use() {
        when(query.hasDuplicateExcludingCurrent(any(), any(), any(), any())).thenReturn(false);
        when(query.hasDuplicateNameExcludingCurrent(any(), any(), any(), any(), any())).thenReturn(false);
        when(query.getContextSchemeValueList(any())).thenReturn(List.of(
                new ContextSchemeValueDetailsRecord(V1, SCHEME_ID, new Guid("0123456789abcdef0123456789abcdef"), "US", "United States", true),
                new ContextSchemeValueDetailsRecord(V2, SCHEME_ID, new Guid("abcdef0123456789abcdef0123456789"), "CA", "Canada", false)));
        // The request keeps only V2, i.e. it removes V1 — which is referenced by a business context.
        when(query.findUsedContextSchemeValueIds(any())).thenReturn(Set.of(V1));

        assertThrows(IllegalArgumentException.class, () -> service.update(requester,
                updateRequest(List.of(new UpdateContextSchemeValueRequest(V2, "CA", "Canada")))));

        verify(command, never()).update(any(), any(), any(), any(), any(), any(), any(), any());
        verify(command, never()).deleteValue(any());
    }

    @Test
    void update_removes_an_unused_value_and_persists() {
        when(query.hasDuplicateExcludingCurrent(any(), any(), any(), any())).thenReturn(false);
        when(query.hasDuplicateNameExcludingCurrent(any(), any(), any(), any(), any())).thenReturn(false);
        when(query.getContextSchemeValueList(any())).thenReturn(List.of(
                new ContextSchemeValueDetailsRecord(V1, SCHEME_ID, new Guid("0123456789abcdef0123456789abcdef"), "US", "United States", false),
                new ContextSchemeValueDetailsRecord(V2, SCHEME_ID, new Guid("abcdef0123456789abcdef0123456789"), "CA", "Canada", false)));
        when(query.findUsedContextSchemeValueIds(any())).thenReturn(Set.of());

        service.update(requester, updateRequest(List.of(new UpdateContextSchemeValueRequest(V2, "CA", "Canada"))));

        verify(command).update(any(), any(), any(), any(), any(), any(), any(), any());
        verify(command).deleteValue(V1);
    }

    // ----- discard: in-use guard (B2) -----

    @Test
    void discard_rejects_a_scheme_in_use() {
        when(query.isContextSchemeUsed(SCHEME_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.discard(requester, SCHEME_ID));
        verify(command, never()).delete(any());
    }

    @Test
    void discard_deletes_an_unused_scheme() {
        when(query.isContextSchemeUsed(SCHEME_ID)).thenReturn(false);
        when(command.delete(any())).thenReturn(1);

        assertTrue(service.discard(requester, SCHEME_ID));
        verify(command).delete(List.of(SCHEME_ID));
    }

    @Test
    void bulk_discard_skips_schemes_in_use_and_deletes_the_rest() {
        ContextSchemeId used = new ContextSchemeId(BigInteger.valueOf(20));
        when(query.isContextSchemeUsed(SCHEME_ID)).thenReturn(false);
        when(query.isContextSchemeUsed(used)).thenReturn(true);
        when(command.delete(any())).thenReturn(1);

        service.discard(requester, List.of(SCHEME_ID, used));

        verify(command).delete(List.of(SCHEME_ID));
    }

    @Test
    void bulk_discard_rejects_when_all_schemes_are_in_use() {
        when(query.isContextSchemeUsed(any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.discard(requester, List.of(SCHEME_ID)));
        verify(command, never()).delete(any());
    }

    // ----- column lengths (#329) -----

    @Test
    void create_rejects_text_longer_than_the_columns() {
        String over45 = "x".repeat(46);
        var longId = new CreateContextSchemeRequest(CATEGORY_ID, null, over45, "N", "d", "OAGi", "1.0", List.of());
        var longAgency = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", over45, "1.0", List.of());
        var longVersion = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", "OAGi", over45, List.of());
        var longName = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "x".repeat(256), "d", "OAGi", "1.0", List.of());
        var longValue = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", "OAGi", "1.0",
                List.of(new org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload
                        .CreateContextSchemeValueRequest("v".repeat(101), "m")));
        for (var request : List.of(longId, longAgency, longVersion, longName, longValue)) {
            assertThrows(IllegalArgumentException.class, () -> service.create(requester, request));
        }
        verifyNoInteractions(command);
    }

    @Test
    void create_accepts_text_at_the_column_limits() {
        when(command.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(SCHEME_ID);
        var request = new CreateContextSchemeRequest(CATEGORY_ID, null, "x".repeat(45), "n".repeat(255), "d",
                "a".repeat(45), "v".repeat(45),
                List.of(new org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload
                        .CreateContextSchemeValueRequest("v".repeat(100), "m")));
        assertEquals(SCHEME_ID, service.create(requester, request));
    }

    @Test
    void update_rejects_a_value_longer_than_the_column() {
        var request = updateRequest(List.of(new UpdateContextSchemeValueRequest(V1, "v".repeat(101), "m")));
        var e = assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        assertTrue(e.getMessage().contains("100"));
        verifyNoInteractions(command);
    }

    // ----- duplicate values (#329) -----

    private static ContextSchemeValueDetailsRecord existingValue(ContextSchemeValueId id, String value) {
        return new ContextSchemeValueDetailsRecord(id, SCHEME_ID, new Guid("0123456789abcdef0123456789abcdef"), value, "m", false);
    }

    private static org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload
            .CreateContextSchemeValueRequest cv(String value) {
        return new org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload
                .CreateContextSchemeValueRequest(value, "m");
    }

    private CreateContextSchemeRequest createWithValues(String... values) {
        return new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", "OAGi", "1.0",
                java.util.Arrays.stream(values).map(ContextSchemeCommandServiceTest::cv).toList());
    }

    @Test
    void create_rejects_a_value_given_twice() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> service.create(requester, createWithValues("US", "CA", "US")));
        assertTrue(e.getMessage().contains("'US'"));
        verifyNoInteractions(command);
        verify(query, never()).hasDuplicate(any(), any(), any());
    }

    @Test
    void create_rejects_a_value_given_twice_even_with_a_different_meaning() {
        var request = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", "OAGi", "1.0",
                List.of(cv("US"), new org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload
                        .CreateContextSchemeValueRequest("US", "United States")));
        assertThrows(IllegalArgumentException.class, () -> service.create(requester, request));
        verifyNoInteractions(command);
    }

    @Test
    void create_accepts_distinct_values_and_compares_them_exactly() {
        when(command.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(SCHEME_ID);
        // Like the screens, the comparison is exact: case and surrounding spaces make a different value.
        assertEquals(SCHEME_ID, service.create(requester, createWithValues("US", "us", "US ", "CA")));
        verify(command, times(4)).createValue(eq(SCHEME_ID), any(), any());
    }

    @Test
    void create_accepts_no_values_and_a_single_value() {
        when(command.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(SCHEME_ID);
        assertEquals(SCHEME_ID, service.create(requester, createWithValues()));
        assertEquals(SCHEME_ID, service.create(requester, createWithValues("US")));
    }

    @Test
    void update_rejects_a_value_given_twice() {
        var request = updateRequest(List.of(
                new UpdateContextSchemeValueRequest(V1, "US", "a"),
                new UpdateContextSchemeValueRequest(null, "US", "b")));
        var e = assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        assertTrue(e.getMessage().contains("'US'"));
        verifyNoInteractions(command);
    }

    @Test
    void update_rejects_a_new_value_equal_to_a_kept_one() {
        var request = updateRequest(List.of(
                new UpdateContextSchemeValueRequest(V1, "US", "a"),
                new UpdateContextSchemeValueRequest(V2, "US", "b")));
        assertThrows(IllegalArgumentException.class, () -> service.update(requester, request));
        verifyNoInteractions(command);
    }

    @Test
    void update_accepts_editing_a_value_in_place_and_distinct_values() {
        when(query.getContextSchemeValueList(SCHEME_ID)).thenReturn(List.of(existingValue(V1, "US"), existingValue(V2, "CA")));
        when(query.findUsedContextSchemeValueIds(any())).thenReturn(Set.of());
        // V1 keeps its value and only its meaning changes: it is not a duplicate of itself (#1744 class).
        service.update(requester, updateRequest(List.of(
                new UpdateContextSchemeValueRequest(V1, "US", "United States of America"),
                new UpdateContextSchemeValueRequest(V2, "CA", "Canada"))));
        verify(command).updateValue(V1, "US", "United States of America");
    }

    @Test
    void update_accepts_swapping_a_value_into_one_that_was_removed() {
        when(query.getContextSchemeValueList(SCHEME_ID)).thenReturn(List.of(existingValue(V1, "US"), existingValue(V2, "CA")));
        when(query.findUsedContextSchemeValueIds(any())).thenReturn(Set.of());
        // The request is the final state: V2 is dropped and a new value takes its text.
        service.update(requester, updateRequest(List.of(
                new UpdateContextSchemeValueRequest(V1, "US", "a"),
                new UpdateContextSchemeValueRequest(null, "CA", "again"))));
        verify(command).createValue(SCHEME_ID, "CA", "again");
    }

    // ----- column lengths and duplicate values: the remaining cases (#329) -----

    @Test
    void update_rejects_text_longer_than_the_columns_and_names_the_field() {
        String over45 = "x".repeat(46);
        var cases = new java.util.LinkedHashMap<String, UpdateContextSchemeRequest>();
        cases.put("Context scheme name", new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "GEO", "x".repeat(256),
                "desc", "OAGi", "1.0", List.of()));
        cases.put("Context scheme ID", new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, over45, "N",
                "desc", "OAGi", "1.0", List.of()));
        cases.put("Context scheme agency ID", new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "GEO", "N",
                "desc", over45, "1.0", List.of()));
        cases.put("Context scheme version", new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "GEO", "N",
                "desc", "OAGi", over45, List.of()));
        cases.forEach((field, request) -> {
            var e = assertThrows(IllegalArgumentException.class, () -> service.update(requester, request), field);
            assertTrue(e.getMessage().startsWith(field), e.getMessage());
        });
        verifyNoInteractions(command);
    }

    @Test
    void create_names_the_agency_id_and_version_when_they_are_too_long() {
        var longAgency = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", "a".repeat(46), "1.0", List.of());
        var longVersion = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", "OAGi", "v".repeat(46), List.of());
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.create(requester, longAgency))
                .getMessage().contains("agency ID"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.create(requester, longVersion))
                .getMessage().contains("version"));
    }

    @Test
    void update_accepts_text_at_the_column_limits() {
        when(query.getContextSchemeValueList(SCHEME_ID)).thenReturn(List.of());
        when(query.findUsedContextSchemeValueIds(any())).thenReturn(Set.of());
        service.update(requester, new UpdateContextSchemeRequest(SCHEME_ID, CATEGORY_ID, null, "x".repeat(45), "n".repeat(255),
                "desc", "a".repeat(45), "v".repeat(45),
                List.of(new UpdateContextSchemeValueRequest(null, "v".repeat(100), "m"))));
        verify(command).createValue(SCHEME_ID, "v".repeat(100), "m");
    }

    @Test
    void a_value_over_the_column_is_named_in_the_message() {
        var request = createWithValues("ok", "v".repeat(101));
        var e = assertThrows(IllegalArgumentException.class, () -> service.create(requester, request));
        assertTrue(e.getMessage().startsWith("Context scheme value 'vvv"), e.getMessage());
    }

    @Test
    void null_and_empty_values_do_not_count_as_duplicates_of_each_other() {
        when(command.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(SCHEME_ID);
        // A missing value is not a value given twice; the required-field checks of the screens decide on it.
        var request = new CreateContextSchemeRequest(CATEGORY_ID, null, "GEO", "N", "d", "OAGi", "1.0",
                java.util.Arrays.asList(cv(null), cv(null)));
        assertEquals(SCHEME_ID, service.create(requester, request));
    }

    @Test
    void two_empty_values_are_a_duplicate() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> service.create(requester, createWithValues("", "")));
        assertTrue(e.getMessage().contains("already exists"));
    }

    @Test
    void a_value_given_twice_is_rejected_before_the_triplet_uniqueness_is_checked() {
        when(query.hasDuplicate(any(), any(), any())).thenReturn(true);
        var e = assertThrows(IllegalArgumentException.class,
                () -> service.create(requester, createWithValues("US", "US")));
        assertTrue(e.getMessage().contains("'US'"));
    }
}
