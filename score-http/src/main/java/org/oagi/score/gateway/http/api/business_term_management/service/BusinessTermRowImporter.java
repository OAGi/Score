package org.oagi.score.gateway.http.api.business_term_management.service;

import org.oagi.score.gateway.http.api.business_term_management.controller.payload.BusinessTermImportRow;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermUpsertResult;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Imports a single business-term row in its OWN transaction so a best-effort batch import can
 * commit the good rows and skip the bad ones independently.
 *
 * <p>This is intentionally a SEPARATE Spring bean from {@link BusinessTermCommandService}: a
 * {@code REQUIRES_NEW} boundary declared on a method that is called from within the same bean
 * would be a no-op, because Spring's transactional proxy is bypassed on self-invocation. Injecting
 * this bean and calling it through the proxy is what makes per-row commit boundaries real.</p>
 */
@Service
public class BusinessTermRowImporter {

    public enum Outcome {
        CREATED, UPDATED
    }

    @Autowired
    private RepositoryFactory repositoryFactory;

    /**
     * Validates and upserts one row. The access gate ({@code assertBusinessTermEnabled}) is applied
     * once by the caller before the loop, so it is deliberately not repeated per row here.
     *
     * @return {@link Outcome#UPDATED} when an existing row with the same external reference URI was
     * overwritten, otherwise {@link Outcome#CREATED}.
     * @throws IllegalArgumentException when the row fails validation (caught per row by the caller).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Outcome importOne(ScoreUser requester, BusinessTermImportRow row) {
        BusinessTermInputValidator.validate(
                row.businessTerm(), row.externalReferenceId(), row.externalReferenceUri());

        // The repository upsert already probes the external reference URI to decide insert-vs-update, so
        // it reports the created/updated outcome directly — no separate existence query per row is needed.
        var command = repositoryFactory.businessTermCommandRepository(requester);
        BusinessTermUpsertResult result = command.upsertByExternalReferenceUri(
                row.businessTerm(),
                row.externalReferenceId(),
                row.externalReferenceUri(),
                row.definition(),
                row.comment());

        return result.created() ? Outcome.CREATED : Outcome.UPDATED;
    }
}
