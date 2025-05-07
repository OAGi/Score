package org.oagi.score.gateway.http.api.business_term_management.repository;

import org.oagi.score.gateway.http.api.business_term_management.controller.payload.AssignBusinessTermRequest;
import org.oagi.score.gateway.http.api.business_term_management.model.AsbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BbieBusinessTermId;
import org.oagi.score.gateway.http.api.business_term_management.model.BusinessTermId;

import java.math.BigInteger;
import java.util.List;

public interface BusinessTermCommandRepository {

    BusinessTermId create(String businessTerm,
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
