package org.oagi.score.gateway.http.api.context_management.context_scheme.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategoryId;
import org.oagi.score.gateway.http.api.context_management.context_scheme.controller.payload.CreateContextSchemeRequest;
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
 * Guards added for the Context Management audit (#1744 sibling class): server-side triplet/name
 * uniqueness on create/update, in-use guards on discard, and a value-removal guard on update.
 * These tests mock the repositories (no DB) and assert each guard throws a 4xx-mapped
 * {@link IllegalArgumentException} and does not mutate.
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

    // ----- create: server-side uniqueness (B7) -----

    @Test
    void create_rejects_a_duplicate_triplet() {
        when(query.hasDuplicate(any(), any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(requester, createRequest()));
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_rejects_a_duplicate_name() {
        when(query.hasDuplicate(any(), any(), any())).thenReturn(false);
        when(query.hasDuplicateName(any(), any(), any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(requester, createRequest()));
        verify(command, never()).create(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void create_persists_a_unique_scheme() {
        when(query.hasDuplicate(any(), any(), any())).thenReturn(false);
        when(query.hasDuplicateName(any(), any(), any(), any())).thenReturn(false);
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
}
