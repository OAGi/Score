package org.oagi.score.gateway.http.api.cc_management.repository;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

public interface BccpCommandRepository {

    BccpManifestId create(ReleaseId releaseId,
                          DtManifestId dtManifestId,
                          @Nullable String initialPropertyTerm);

    boolean update(BccpManifestId bccpManifestId,
                   @Nullable String propertyTerm,
                   @Nullable Boolean nillable,
                   @Nullable Boolean deprecated,
                   @Nullable NamespaceId namespaceId,
                   @Nullable ValueConstraint valueConstraint,
                   @Nullable Definition definition);

    boolean updateDt(BccpManifestId bccpManifestId, DtManifestId dtManifestId);

    boolean updateState(BccpManifestId bccpManifestId, CcState state);

    boolean updateLogId(BccpManifestId bccpManifestId, LogId logId);

    boolean delete(BccpManifestId bccpManifestId);

    void revise(BccpManifestId bccpManifestId);

    void cancel(BccpManifestId bccpManifestId);

    boolean updateOwnership(ScoreUser targetUser, BccpManifestId bccpManifestId);

}
