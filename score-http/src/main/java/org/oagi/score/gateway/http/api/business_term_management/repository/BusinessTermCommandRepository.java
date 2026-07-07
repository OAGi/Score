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
     * Upserts a catalog business term keyed on its external reference URI: inserts a new row when no
     * business term with that URI exists, otherwise updates the lowest-id existing row with that URI.
     * The required {@code businessTerm} is always written; the optional external reference id /
     * definition / comment are written only when non-blank, so a re-import whose source lacks one of
     * those columns cannot blank-clobber the existing value. Returns the written id plus whether the
     * row was newly {@code created} (vs. an existing same-URI row being updated), so callers do not
     * need a separate existence query to classify the outcome.
     */
    BusinessTermUpsertResult upsertByExternalReferenceUri(String businessTerm,
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
