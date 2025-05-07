package org.oagi.score.gateway.http.api.cc_management.repository;

import jakarta.annotation.Nullable;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.ValueConstraint;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccType;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.log_management.model.LogId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;

public interface AccCommandRepository {

    AccManifestId create(
            ReleaseId releaseId,
            @Nullable AccManifestId basedAccManifestId,
            @Nullable String initialObjectClassTerm,
            @Nullable OagisComponentType initialComponentType,
            @Nullable AccType initialType,
            @Nullable String initialDefinition,
            @Nullable NamespaceId namespaceId);

    boolean update(AccManifestId accManifestId,
                   @Nullable String objectClassTerm,
                   @Nullable OagisComponentType componentType,
                   @Nullable Boolean isAbstract,
                   @Nullable Boolean deprecated,
                   @Nullable NamespaceId namespaceId,
                   @Nullable Definition definition);

    boolean updateState(AccManifestId accManifestId, CcState state);

    boolean updateBasedAccManifestId(AccManifestId accManifestId, AccManifestId basedAccManifestId);

    boolean updateLogId(AccManifestId accManifestId, LogId logId);

    AsccManifestId createAscc(AccManifestId accManifestId,
                              AsccpManifestId asccpManifestId,
                              @Nullable Integer pos,
                              @Nullable Cardinality cardinality,
                              boolean skipDependencyCheck);

    BccManifestId createBcc(AccManifestId accManifestId,
                            BccpManifestId bccpManifestId,
                            @Nullable Integer pos,
                            @Nullable Cardinality cardinality,
                            boolean skipDependencyCheck);

    boolean update(AsccManifestId asccManifestId,
                   @Nullable Cardinality cardinality,
                   @Nullable Boolean deprecated,
                   @Nullable Definition definition);

    boolean syncDen(AsccManifestId asccManifestId);

    boolean update(BccManifestId bccManifestId,
                   @Nullable EntityType entityType,
                   @Nullable Cardinality cardinality,
                   @Nullable Boolean deprecated,
                   @Nullable Boolean nillable,
                   @Nullable ValueConstraint valueConstraint,
                   @Nullable Definition definition);

    boolean syncDen(BccManifestId bccManifestId);

    boolean delete(AccManifestId accManifestId);

    boolean delete(AsccManifestId asccManifestId);

    boolean delete(BccManifestId bccManifestId);

    void revise(AccManifestId accManifestId);

    void cancel(AccManifestId accManifestId);

    boolean updateOwnership(ScoreUser targetUser, AccManifestId accManifestId);

}
