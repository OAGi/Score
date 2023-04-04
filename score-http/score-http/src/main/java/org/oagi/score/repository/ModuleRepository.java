package org.oagi.score.repository;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.export.model.Namespace;
import org.oagi.score.export.model.ScoreModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;


@Repository
public class ModuleRepository {

    @Autowired
    private DSLContext dslContext;

    public List<ScoreModule> findAll(ULong moduleSetReleaseId) {
        return dslContext.select(
                        MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                        MODULE_SET_RELEASE.MODULE_SET_ID,
                        MODULE_SET_RELEASE.RELEASE_ID,
                        MODULE.MODULE_ID, MODULE.NAME,
                        MODULE.NAMESPACE_ID.as("module_namespace_id"),
                        NAMESPACE.as("module_namespace").URI.as("module_namespace_uri"),
                        NAMESPACE.as("module_namespace").PREFIX.as("module_namespace_prefix"),
                        RELEASE.NAMESPACE_ID.as("release_namespace_id"),
                        NAMESPACE.as("release_namespace").URI.as("release_namespace_uri"),
                        NAMESPACE.as("release_namespace").PREFIX.as("release_namespace_prefix"),
                        MODULE.VERSION_NUM, MODULE.PATH)
                .from(MODULE_SET_RELEASE)
                .join(RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(MODULE).on(MODULE_SET_RELEASE.MODULE_SET_ID.eq(MODULE.MODULE_SET_ID))
                .leftJoin(NAMESPACE.as("module_namespace")).on(MODULE.NAMESPACE_ID.eq(NAMESPACE.as("module_namespace").NAMESPACE_ID))
                .leftJoin(NAMESPACE.as("release_namespace")).on(RELEASE.NAMESPACE_ID.eq(NAMESPACE.as("release_namespace").NAMESPACE_ID))
                .where(and(MODULE.TYPE.eq("FILE"), MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId)))
                .fetch(record -> {
                    ScoreModule scoreModule = new ScoreModule();
                    scoreModule.setModuleSetReleaseId(record.get(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID));
                    scoreModule.setModuleSetId(record.get(MODULE_SET_RELEASE.MODULE_SET_ID));
                    scoreModule.setReleaseId(record.get(MODULE_SET_RELEASE.RELEASE_ID));
                    scoreModule.setModuleId(record.get(MODULE.MODULE_ID));
                    scoreModule.setName(record.get(MODULE.NAME));

                    ULong moduleNamespaceId = record.get(
                            MODULE.NAMESPACE_ID.as("module_namespace_id"));
                    String moduleNamespaceUri = record.get(
                            NAMESPACE.as("module_namespace").URI.as("module_namespace_uri"));
                    String moduleNamespacePrefix = record.get(
                            NAMESPACE.as("module_namespace").PREFIX.as("module_namespace_prefix"));
                    if (moduleNamespaceId != null) {
                        scoreModule.setModuleNamespace(new Namespace(
                                moduleNamespaceId, moduleNamespaceUri, moduleNamespacePrefix));
                    }

                    ULong releaseNamespaceId = record.get(
                            RELEASE.NAMESPACE_ID.as("release_namespace_id"));
                    String releaseNamespaceUri = record.get(
                            NAMESPACE.as("release_namespace").URI.as("release_namespace_uri"));
                    String releaseNamespacePrefix = record.get(
                            NAMESPACE.as("release_namespace").PREFIX.as("release_namespace_prefix"));
                    if (releaseNamespaceId != null) {
                        scoreModule.setReleaseNamespace(new Namespace(
                                releaseNamespaceId, releaseNamespaceUri, releaseNamespacePrefix));
                    }

                    scoreModule.setVersionNum(record.get(MODULE.VERSION_NUM));
                    scoreModule.setPath(record.get(MODULE.PATH));
                    return scoreModule;
                });
    }

    public ScoreModule findByModuleSetReleaseIdAndAsccpManifestId(
            ULong moduleSetReleaseId, ULong asccpManifestId) {
        return dslContext.select(
                        MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID,
                        MODULE_SET_RELEASE.MODULE_SET_ID,
                        MODULE_SET_RELEASE.RELEASE_ID,
                        MODULE.MODULE_ID, MODULE.NAME,
                        MODULE.NAMESPACE_ID.as("module_namespace_id"),
                        NAMESPACE.as("module_namespace").URI.as("module_namespace_uri"),
                        NAMESPACE.as("module_namespace").PREFIX.as("module_namespace_prefix"),
                        RELEASE.NAMESPACE_ID.as("release_namespace_id"),
                        NAMESPACE.as("release_namespace").URI.as("release_namespace_uri"),
                        NAMESPACE.as("release_namespace").PREFIX.as("release_namespace_prefix"),
                        MODULE.VERSION_NUM, MODULE.PATH)
                .from(MODULE_SET_RELEASE)
                .join(MODULE_ASCCP_MANIFEST).on(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID))
                .join(RELEASE).on(MODULE_SET_RELEASE.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(MODULE).on(and(
                        MODULE_SET_RELEASE.MODULE_SET_ID.eq(MODULE.MODULE_SET_ID),
                        MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID)
                ))
                .leftJoin(NAMESPACE.as("module_namespace")).on(MODULE.NAMESPACE_ID.eq(NAMESPACE.as("module_namespace").NAMESPACE_ID))
                .leftJoin(NAMESPACE.as("release_namespace")).on(RELEASE.NAMESPACE_ID.eq(NAMESPACE.as("release_namespace").NAMESPACE_ID))
                .where(and(
                        MODULE.TYPE.eq("FILE"),
                        MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId),
                        MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestId)
                ))
                .fetchOneInto(ScoreModule.class);
    }

    public BigInteger getModuleSetReleaseIdByAsccpManifestId(ULong asccpManifestId) {
        return dslContext.select(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID)
                .from(MODULE_SET_RELEASE)
                .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST.RELEASE_ID.eq(MODULE_SET_RELEASE.RELEASE_ID))
                .where(and(
                        MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1),
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(asccpManifestId)
                ))
                .fetchOneInto(BigInteger.class);
    }

    public String getModulePathByDtManifestId(ULong moduleSetReleaseId, ULong dtManifestId) {
        return dslContext.select(MODULE.PATH)
                .from(MODULE)
                .join(MODULE_DT_MANIFEST).on(MODULE.MODULE_ID.eq(MODULE_DT_MANIFEST.MODULE_ID))
                .join(MODULE_SET_RELEASE).on(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID))
                .where(and(
                        MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID.eq(moduleSetReleaseId),
                        MODULE_DT_MANIFEST.DT_MANIFEST_ID.eq(dtManifestId)
                ))
                .fetchOneInto(String.class);
    }

}
