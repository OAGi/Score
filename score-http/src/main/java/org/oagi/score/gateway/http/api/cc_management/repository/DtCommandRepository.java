package org.oagi.score.gateway.http.api.cc_management.repository;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

public interface DtCommandRepository {

    DtManifestId create(
            ReleaseId releaseId,
            DtManifestId basedDtManifestId);

    DtScManifestId appendDtSc(DtManifestId ownerDtManifestId);

    DtScManifestId createDtScFromBase(DtManifestId ownerDtManifestId, DtScManifestId basedDtScManifestId);

    boolean update(DtManifestId dtManifestId,
                   @Nullable String qualifier,
                   @Nullable String sixDigitId,
                   @Nullable Boolean deprecated,
                   @Nullable NamespaceId namespaceId,
                   @Nullable String contentComponentDefinition,
                   @Nullable Definition definition);

    boolean updateState(DtManifestId dtManifestId, CcState state);

    boolean updateDtSc(DtScManifestId dtScManifestId,
                       @Nullable String propertyTerm,
                       @Nullable String representationTerm,
                       @Nullable Cardinality cardinality,
                       @Nullable Boolean deprecated,
                       @Nullable ValueConstraint valueConstraint,
                       @Nullable Definition definition);

    boolean updateLogId(DtManifestId dtManifestId, LogId logId);

    boolean delete(DtManifestId dtManifestId);

    boolean delete(DtScManifestId dtScManifestId);

    void revise(DtManifestId dtManifestId);

    void cancel(DtManifestId dtManifestId);

    DtAwdPriId createDtAwdPri(ReleaseId releaseId, DtId dtId,
                              XbtManifestId xbtManifestId,
                              CodeListManifestId codeListManifestId,
                              AgencyIdListManifestId agencyIdListManifestId,
                              boolean isDefault);

    boolean updateDtAwdPri(DtAwdPriId dtAwdPriId,
                           XbtManifestId xbtManifestId,
                           CodeListManifestId codeListManifestId,
                           AgencyIdListManifestId agencyIdListManifestId,
                           boolean isDefault);

    boolean deleteDtAwdPri(DtAwdPriId dtAwdPriId);

    DtScAwdPriId createDtScAwdPri(ReleaseId releaseId, DtScId dtScId,
                                  XbtManifestId xbtManifestId,
                                  CodeListManifestId codeListManifestId,
                                  AgencyIdListManifestId agencyIdListManifestId,
                                  boolean isDefault);

    boolean updateDtScAwdPri(DtScAwdPriId dtScAwdPriId,
                             XbtManifestId xbtManifestId,
                             CodeListManifestId codeListManifestId,
                             AgencyIdListManifestId agencyIdListManifestId,
                             boolean isDefault);

    boolean deleteDtScAwdPri(DtScAwdPriId dtScAwdPriId);

    boolean updateOwnership(ScoreUser targetUser, DtManifestId dtManifestId);

}
