package org.oagi.score.gateway.http.api.business_term_management.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.application_management.service.ApplicationConfigurationQueryService;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermBatchImportResult;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermBatchImportRowResult;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermCreateRequest;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermImportRow;
import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermUpdateRequest;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.repository.BusinessTermCommandRepository;
import org.oagi.score.gateway.http.api.business_term_management.repository.BusinessTermQueryRepository;
import org.oagi.score.gateway.http.common.model.ScoreRole;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * #1754 coverage for {@link BusinessTermCommandService}. These mock the repository factory + config
 * query (no DB) and assert the rules the user cares about:
 * <ul>
 *   <li><b>Ownership</b> — every command endpoint (create/update/assign/batch) is 403 for a
 *       developer-role requester and when the tenant feature is off (closes the OWN-01 gap where
 *       only the catalog read/write endpoints were previously probed).</li>
 *   <li><b>Duplication</b> — uniqueness is the (name + URI) pair only, so both create and update
 *       hard-block a duplicate (name + URI) pair; a same-name/different-URI term is distinct and is
 *       NOT blocked (the server runs no name-only check).</li>
 *   <li><b>Import-duplication</b> — null rows / over-cap are 400, an intra-batch duplicate URI is
 *       FAILED (not re-imported), and one failing row does not abort the surviving rows.</li>
 * </ul>
 */
class BusinessTermCommandServiceTest {

    private BusinessTermCommandService service;
    private RepositoryFactory repositoryFactory;
    private ApplicationConfigurationQueryService applicationConfigurationQueryService;
    private BusinessTermRowImporter rowImporter;
    private BusinessTermCommandRepository command;
    private BusinessTermQueryRepository query;

    private ScoreUser endUser;
    private ScoreUser developer;

    private static final BusinessTermId CREATED_ID = new BusinessTermId(BigInteger.valueOf(777));

    @BeforeEach
    void setUp() {
        service = new BusinessTermCommandService();
        repositoryFactory = mock(RepositoryFactory.class);
        applicationConfigurationQueryService = mock(ApplicationConfigurationQueryService.class);
        rowImporter = mock(BusinessTermRowImporter.class);
        command = mock(BusinessTermCommandRepository.class);
        query = mock(BusinessTermQueryRepository.class);

        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        ReflectionTestUtils.setField(service, "applicationConfigurationQueryService", applicationConfigurationQueryService);
        ReflectionTestUtils.setField(service, "rowImporter", rowImporter);

        endUser = new ScoreUser(new UserId(BigInteger.valueOf(42)), "eu", "End User",
                "eu@example.com", true, List.of(ScoreRole.END_USER));
        developer = new ScoreUser(new UserId(BigInteger.valueOf(7)), "dev", "Developer",
                "dev@example.com", true, List.of(ScoreRole.DEVELOPER));

        // End-user + feature enabled is the happy default; the catalog is unique unless a test says otherwise.
        when(applicationConfigurationQueryService.isBusinessTermEnabled(any())).thenReturn(true);
        when(repositoryFactory.businessTermCommandRepository(any())).thenReturn(command);
        when(repositoryFactory.businessTermQueryRepository(any())).thenReturn(query);
        when(query.checkUniqueness(any(), any())).thenReturn(true);          // create: (name + URI)
        when(query.checkUniqueness(any(), any(), any())).thenReturn(true);   // update: (id + name + URI)
        when(command.insert(any(), any(), any(), any(), any())).thenReturn(CREATED_ID);
    }

    private static BusinessTermCreateRequest createRequest() {
        return new BusinessTermCreateRequest("Ship To", "id-1", "http://ref/1", "def", "comment");
    }

    private static BusinessTermUpdateRequest updateRequest() {
        return new BusinessTermUpdateRequest(new BusinessTermId(BigInteger.ONE),
                "Ship To", "id-1", "http://ref/1", "def", "comment");
    }

    private static BusinessTermImportRow row(int index, String uri) {
        return new BusinessTermImportRow(index, "Term " + index, "", uri, "", "");
    }

    // ---- Ownership (403) ------------------------------------------------------------------------

    @Test
    void create_rejectsDeveloperWith403() {
        assertThrows(DataAccessForbiddenException.class, () -> service.create(developer, createRequest()));
        verify(command, never()).insert(any(), any(), any(), any(), any());
    }

    @Test
    void create_rejectsWhenFeatureDisabledWith403() {
        when(applicationConfigurationQueryService.isBusinessTermEnabled(any())).thenReturn(false);
        assertThrows(DataAccessForbiddenException.class, () -> service.create(endUser, createRequest()));
        verify(command, never()).insert(any(), any(), any(), any(), any());
    }

    @Test
    void assignBusinessTerm_rejectsDeveloperWith403() {
        assertThrows(DataAccessForbiddenException.class,
                () -> service.assignBusinessTerm(developer, new BusinessTermId(BigInteger.ONE), null));
        verify(command, never()).assignBusinessTerm(any(), any());
    }

    @Test
    void createBatch_rejectsDeveloperWith403() {
        assertThrows(DataAccessForbiddenException.class, () -> service.createBatch(developer, List.of()));
        verify(rowImporter, never()).importOne(any(), any());
    }

    // ---- Duplication (name / name+URI) ----------------------------------------------------------

    @Test
    void create_rejectsDuplicateCompositeWith400() {
        when(query.checkUniqueness(any(), any())).thenReturn(false);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(endUser, createRequest()));
        assertTrue(ex.getMessage().contains("external reference URI"));
        verify(command, never()).insert(any(), any(), any(), any(), any());
    }

    @Test
    void create_allowsDuplicateNameWhenCompositeUnique() {
        // Uniqueness is the (name + URI) pair; a same-name term with a different URI is distinct, so
        // create must succeed (there is no name-only check).
        BusinessTermId id = service.create(endUser, createRequest());
        assertEquals(CREATED_ID, id);
        verify(command).insert(any(), any(), any(), any(), any());
        verify(command, never()).upsertByBusinessTermAndUri(any(), any(), any(), any(), any());
    }

    @Test
    void create_andUpdate_rejectUriWithSchemeThatCanRunScript() {
        for (String uri : new String[]{"javascript:alert(1)", "JavaScript:alert(1)", "data:text/html,x", "/relative/path", "ref/1"}) {
            assertThrows(IllegalArgumentException.class, () -> service.create(endUser,
                    new BusinessTermCreateRequest("Ship To", "id-1", uri, "def", "comment")), uri);
            assertThrows(IllegalArgumentException.class, () -> service.update(endUser,
                    new BusinessTermUpdateRequest(new BusinessTermId(BigInteger.ONE),
                            "Ship To", "id-1", uri, "def", "comment")), uri);
        }
        verify(command, never()).insert(any(), any(), any(), any(), any());
    }

    @Test
    void create_acceptsHttpHttpsAndUrnUri() {
        for (String uri : new String[]{"http://ref/1", "HTTPS://ref/1", "urn:example:term:1"}) {
            service.create(endUser, new BusinessTermCreateRequest("Ship To", "id-1", uri, "def", "comment"));
        }
        verify(command, times(3)).insert(any(), any(), any(), any(), any());
    }

    @Test
    void create_withUriOwnedByAnotherName_insertsInsteadOfUpsertingOverIt() {
        // Issue #10: (Customer, U) exists; creating (Client, U) is a distinct (name + URI) pair. It must be
        // inserted, never routed through the URI-keyed upsert that would rename Customer.
        when(query.checkUniqueness("Client", "http://ref/1")).thenReturn(true);
        BusinessTermId id = service.create(endUser,
                new BusinessTermCreateRequest("Client", "id-1", "http://ref/1", "def", "comment"));
        assertEquals(CREATED_ID, id);
        verify(command).insert(eq("Client"), any(), eq("http://ref/1"), any(), any());
        verify(command, never()).upsertByBusinessTermAndUri(any(), any(), any(), any(), any());
    }

    @Test
    void create_succeedsWhenUnique() {
        BusinessTermId id = service.create(endUser, createRequest());
        assertEquals(CREATED_ID, id);
        verify(command).insert(
                eq("Ship To"), eq("id-1"), eq("http://ref/1"), eq("def"), eq("comment"));
    }

    @Test
    void update_rejectsDuplicateCompositeWith400() {
        when(query.checkUniqueness(any(), any(), any())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.update(endUser, updateRequest()));
        verify(command, never()).update(any(), any(), any(), any(), any(), any());
    }

    @Test
    void update_allowsDuplicateNameWhenCompositeUnique() {
        // Uniqueness is the (name + URI) pair only, so an update that collides on name but keeps a
        // distinct URI is allowed (there is no name-only check).
        when(command.update(any(), any(), any(), any(), any(), any())).thenReturn(true);

        boolean changed = service.update(endUser, updateRequest());

        assertTrue(changed);
        verify(command).update(any(), any(), any(), any(), any(), any());
    }

    // ---- Import-duplication / row cap / partial success -----------------------------------------

    @Test
    void createBatch_rejectsNullRowsWith400() {
        assertThrows(IllegalArgumentException.class, () -> service.createBatch(endUser, null));
    }

    @Test
    void createBatch_rejectsOverCapWith400() {
        List<BusinessTermImportRow> rows =
                Collections.nCopies(BusinessTermCommandService.MAX_IMPORT_ROWS + 1, row(1, "http://ref/x"));
        assertThrows(IllegalArgumentException.class, () -> service.createBatch(endUser, rows));
        verify(rowImporter, never()).importOne(any(), any());
    }

    @Test
    void createBatch_intraBatchDuplicatePairIsFailedNotReimported() {
        when(rowImporter.importOne(any(), any())).thenReturn(BusinessTermRowImporter.Outcome.CREATED);

        BusinessTermImportRow first = new BusinessTermImportRow(1, "A", "", "http://dup", "", "");
        BusinessTermImportRow second = new BusinessTermImportRow(2, "A", "", "http://dup", "", "");

        BusinessTermBatchImportResult result = service.createBatch(endUser, List.of(first, second));

        assertEquals(1, result.createdCount());
        assertEquals(0, result.updatedCount());
        assertEquals(1, result.failedCount());
        verify(rowImporter, times(1)).importOne(any(), any());

        BusinessTermBatchImportRowResult failed = result.results().stream()
                .filter(r -> BusinessTermBatchImportRowResult.OUTCOME_FAILED.equals(r.outcome()))
                .findFirst().orElseThrow();
        assertEquals(2, failed.rowIndex());
        assertTrue(failed.message().contains("Duplicate business term and external reference URI"));
    }

    @Test
    void createBatch_sameUriWithDifferentNamesAreBothImported() {
        // The identity is the (name + URI) pair, so two terms sharing a URI are distinct, not duplicates.
        when(rowImporter.importOne(any(), any())).thenReturn(BusinessTermRowImporter.Outcome.CREATED);

        BusinessTermImportRow first = new BusinessTermImportRow(1, "Customer", "", "http://shared", "", "");
        BusinessTermImportRow second = new BusinessTermImportRow(2, "Client", "", "http://shared", "", "");

        BusinessTermBatchImportResult result = service.createBatch(endUser, List.of(first, second));

        assertEquals(2, result.createdCount());
        assertEquals(0, result.failedCount());
        verify(rowImporter, times(2)).importOne(any(), any());
    }

    @Test
    void createBatch_oneFailingRowDoesNotAbortTheSurvivors() {
        BusinessTermImportRow good1 = row(1, "http://a");
        BusinessTermImportRow bad = row(2, "http://b");
        BusinessTermImportRow good2 = row(3, "http://c");

        when(rowImporter.importOne(eq(endUser), eq(good1))).thenReturn(BusinessTermRowImporter.Outcome.CREATED);
        when(rowImporter.importOne(eq(endUser), eq(bad))).thenThrow(new IllegalArgumentException("bad row"));
        when(rowImporter.importOne(eq(endUser), eq(good2))).thenReturn(BusinessTermRowImporter.Outcome.UPDATED);

        BusinessTermBatchImportResult result = service.createBatch(endUser, List.of(good1, bad, good2));

        assertEquals(1, result.createdCount());
        assertEquals(1, result.updatedCount());
        assertEquals(1, result.failedCount());
        verify(rowImporter, times(3)).importOne(any(), any());
    }
}
