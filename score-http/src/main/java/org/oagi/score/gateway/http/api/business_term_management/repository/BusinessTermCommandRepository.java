package org.oagi.score.gateway.http.api.business_term_management.repository;

import org.oagi.score.gateway.http.api.business_term_management.controller.payload.AssignBusinessTermRequest;
import org.oagi.score.gateway.http.api.business_term_management.model.AsbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermUpsertResult;

import java.math.BigInteger;
import java.util.List;

public interface BusinessTermCommandRepository {

    /**
     * Upserts a catalog business term keyed on its (business term + external reference URI) pair, the
     * same identity the create and edit screens enforce: inserts a new row when no business term has
     * that pair, otherwise updates the lowest-id existing row with that pair. A term with the same URI
     * but a different name is a distinct term and is inserted, never renamed. The optional external
     * reference id / definition / comment are written only when non-blank, so a re-import whose source
     * lacks one of those columns cannot blank-clobber the existing value. Returns the written id plus
     * whether the row was newly {@code created} (vs. an existing same-pair row being updated), so
     * callers do not need a separate existence query to classify the outcome.
     */
    BusinessTermUpsertResult upsertByBusinessTermAndUri(String businessTerm,
                                                        String externalReferenceId,
                                                        String externalReferenceUri,
                                                        String definition,
                                                        String comment);

    /**
     * Inserts a new catalog business term and returns its id; never touches an existing row. This is
     * the interactive create: a term is identified by its (name + external reference URI) pair, so a
     * different name under an already-used URI is a distinct term, not an update of the existing one
     * (the batch import goes through {@link #upsertByBusinessTermAndUri}).
     */
    BusinessTermId insert(String businessTerm,
                          String externalReferenceId,
                          String externalReferenceUri,
                          String definition,
                          String comment);

    boolean update(BusinessTermId businessTermId,
                   String businessTerm,
                   String externalReferenceId,
                   String externalReferenceUri,
                   String definition,
                   String comment);

    boolean delete(BusinessTermId businessTermId);

    List<BigInteger> assignBusinessTerm(
            BusinessTermId businessTermId, AssignBusinessTermRequest request);

    boolean updateAssignment(AsbieBusinessTermId asbieBusinessTermId,
                             String typeCode, Boolean primaryIndicator);

    boolean updateAssignment(BbieBusinessTermId bbieBusinessTermId,
                             String typeCode, Boolean primaryIndicator);

    boolean delete(AsbieBusinessTermId asbieBusinessTermId);

    boolean delete(BbieBusinessTermId bbieBusinessTermId);
}
