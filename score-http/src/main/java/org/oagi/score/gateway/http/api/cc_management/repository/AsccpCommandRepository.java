package org.oagi.score.gateway.http.api.cc_management.repository;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpType;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

public interface AsccpCommandRepository {

    AsccpManifestId create(ReleaseId releaseId,
                           AccManifestId roleOfAccManifestId,
                           @Nullable String initialPropertyTerm,
                           @Nullable AsccpType asccpType,
                           @Nullable Boolean reusable,
                           @Nullable CcState initialState,
                           @Nullable NamespaceId namespaceId,
                           @Nullable Definition definition);

    boolean update(AsccpManifestId asccpManifestId,
                   @Nullable String propertyTerm,
                   @Nullable Boolean reusable,
                   @Nullable Boolean deprecated,
                   @Nullable Boolean nillable,
                   @Nullable NamespaceId namespaceId,
                   @Nullable Definition definition);

    boolean updateNamespace(AsccpManifestId asccpManifestId,
                            NamespaceId namespaceId);

    boolean updateRoleOfAcc(AsccpManifestId asccpManifestId,
                            AccManifestId roleOfAccManifestId);

    boolean updateState(AsccpManifestId asccpManifestId, CcState state);

    boolean updateLogId(AsccpManifestId asccpManifestId, LogId logId);

    boolean delete(AsccpManifestId asccpManifestId);

    void revise(AsccpManifestId asccpManifestId);

    void cancel(AsccpManifestId asccpManifestId);

    boolean updateOwnership(ScoreUser targetUser, AsccpManifestId asccpManifestId);

}
