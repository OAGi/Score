package org.oagi.score.gateway.http.api.release_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.exception.DataAccessException;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.blob_content.BlobContentManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseState;
import org.oagi.score.gateway.http.api.release_management.repository.ReleaseProgressRepository;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.jooq.impl.DSL.selectCount;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqReleaseProgressRepository extends JooqBaseRepository implements ReleaseProgressRepository {

    /** The table of the manifests of a kind of component, and the columns that tell which manifest, and of which release. */
    private record ManifestTable(Table<?> table, TableField<?, ULong> id, TableField<?, ULong> release) {
    }

    /** What the manifest of each kind of component is held in: a manifest tells the release it is of, and the release the library. */
    private static final Map<Class<? extends ManifestId>, ManifestTable> MANIFEST_TABLES = Map.ofEntries(
            Map.entry(AccManifestId.class, new ManifestTable(ACC_MANIFEST, ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.RELEASE_ID)),
            Map.entry(AsccManifestId.class, new ManifestTable(ASCC_MANIFEST, ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.RELEASE_ID)),
            Map.entry(AsccpManifestId.class, new ManifestTable(ASCCP_MANIFEST, ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.RELEASE_ID)),
            Map.entry(BccManifestId.class, new ManifestTable(BCC_MANIFEST, BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.RELEASE_ID)),
            Map.entry(BccpManifestId.class, new ManifestTable(BCCP_MANIFEST, BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.RELEASE_ID)),
            Map.entry(DtManifestId.class, new ManifestTable(DT_MANIFEST, DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.RELEASE_ID)),
            Map.entry(DtScManifestId.class, new ManifestTable(DT_SC_MANIFEST, DT_SC_MANIFEST.DT_SC_MANIFEST_ID, DT_SC_MANIFEST.RELEASE_ID)),
            Map.entry(CodeListManifestId.class, new ManifestTable(CODE_LIST_MANIFEST, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.RELEASE_ID)),
            Map.entry(CodeListValueManifestId.class, new ManifestTable(CODE_LIST_VALUE_MANIFEST, CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.RELEASE_ID)),
            Map.entry(AgencyIdListManifestId.class, new ManifestTable(AGENCY_ID_LIST_MANIFEST, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.RELEASE_ID)),
            Map.entry(AgencyIdListValueManifestId.class, new ManifestTable(AGENCY_ID_LIST_VALUE_MANIFEST, AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID)),
            Map.entry(XbtManifestId.class, new ManifestTable(XBT_MANIFEST, XBT_MANIFEST.XBT_MANIFEST_ID, XBT_MANIFEST.RELEASE_ID)),
            Map.entry(BlobContentManifestId.class, new ManifestTable(BLOB_CONTENT_MANIFEST, BLOB_CONTENT_MANIFEST.BLOB_CONTENT_MANIFEST_ID, BLOB_CONTENT_MANIFEST.RELEASE_ID)));

    /** The states a release is in while it is being drafted or published. */
    private static final List<String> IN_PROGRESS = List.of(ReleaseState.Drafting.name(), ReleaseState.Publishing.name());

    public JooqReleaseProgressRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    private static LibraryId library(ULong value) {
        return new LibraryId(value.toBigInteger());
    }

    @Override
    public Optional<LibraryId> libraryOfManifest(ManifestId manifestId) {
        ManifestTable manifest = MANIFEST_TABLES.get(manifestId.getClass());
        if (manifest == null) {
            return Optional.empty();
        }
        return dslContext().select(RELEASE.LIBRARY_ID)
                .from(manifest.table())
                .join(RELEASE).on(manifest.release().eq(RELEASE.RELEASE_ID))
                .where(manifest.id().eq(valueOf(manifestId)))
                .fetchOptional(RELEASE.LIBRARY_ID)
                .map(JooqReleaseProgressRepository::library);
    }

    @Override
    public Optional<LibraryId> libraryOfRelease(ReleaseId releaseId) {
        return dslContext().select(RELEASE.LIBRARY_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchOptional(RELEASE.LIBRARY_ID)
                .map(JooqReleaseProgressRepository::library);
    }

    /** The error of a lock that is not given at once: the one of a lock wait that is not waited for. */
    private static boolean isLockConflict(DataAccessException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException sql && (sql.getErrorCode() == 1205 || sql.getErrorCode() == 1020
                    || sql.getErrorCode() == 1213)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean libraryIsBusy(LibraryId libraryId) {
        try {
            dslContext().select(LIBRARY.LIBRARY_ID)
                    .from(LIBRARY)
                    .where(LIBRARY.LIBRARY_ID.eq(valueOf(libraryId)))
                    .forShare().noWait()
                    .fetch();
            return dslContext().selectCount()
                    .from(RELEASE)
                    .where(RELEASE.LIBRARY_ID.eq(valueOf(libraryId)), RELEASE.STATE.in(IN_PROGRESS))
                    .forShare().noWait()
                    .fetchOne(0, int.class) > 0;
        } catch (DataAccessException e) {
            if (isLockConflict(e)) {
                return true;
            }
            throw e;
        }
    }

    @Override
    public boolean anyLibraryIsBusy() {
        try {
            return dslContext().selectCount()
                    .from(RELEASE)
                    .where(RELEASE.STATE.in(IN_PROGRESS))
                    .forShare().noWait()
                    .fetchOne(0, int.class) > 0;
        } catch (DataAccessException e) {
            if (isLockConflict(e)) {
                return true;
            }
            throw e;
        }
    }

    @Override
    public void lockLibraryOfRelease(ReleaseId releaseId) {
        dslContext().select(LIBRARY.LIBRARY_ID)
                .from(LIBRARY)
                .join(RELEASE).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .forUpdate()
                .fetch();
    }

    @Override
    public boolean moveState(ReleaseId releaseId, ReleaseState from, ReleaseState to) {
        return dslContext().update(RELEASE)
                .set(RELEASE.STATE, to.name())
                .set(RELEASE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)), RELEASE.STATE.eq(from.name()))
                .execute() == 1;
    }

    @Override
    public boolean takeHold(ReleaseId releaseId, ReleaseState from, ReleaseState to, UserId requester) {
        return dslContext().update(RELEASE)
                .set(RELEASE.STATE, to.name())
                .set(RELEASE.LAST_UPDATED_BY, valueOf(requester))
                .set(RELEASE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)), RELEASE.STATE.eq(from.name()))
                .execute() == 1;
    }

    @Override
    public Optional<String> releaseNumOf(ReleaseId releaseId) {
        return dslContext().select(RELEASE.RELEASE_NUM)
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .fetchOptional(RELEASE.RELEASE_NUM);
    }

    @Override
    public Optional<ReleaseState> lockState(ReleaseId releaseId) {
        return dslContext().select(RELEASE.STATE)
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(valueOf(releaseId)))
                .forUpdate()
                .fetchOptional(RELEASE.STATE)
                .map(ReleaseState::valueOf);
    }

    @Override
    public List<InProgress> lockInProgressSince(LocalDateTime time) {
        return dslContext().select(RELEASE.RELEASE_ID, RELEASE.STATE, RELEASE.LAST_UPDATED_BY)
                .from(RELEASE)
                .where(RELEASE.STATE.in(IN_PROGRESS), RELEASE.LAST_UPDATE_TIMESTAMP.lt(time))
                .forUpdate().skipLocked()
                .fetch(r -> new InProgress(new ReleaseId(r.get(RELEASE.RELEASE_ID).toBigInteger()),
                        ReleaseState.valueOf(r.get(RELEASE.STATE)),
                        new UserId(r.get(RELEASE.LAST_UPDATED_BY).toBigInteger())));
    }

    @Override
    public boolean hasManifests(ReleaseId releaseId) {
        return dslContext().fetchExists(ACC_MANIFEST, ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                || dslContext().fetchExists(DT_MANIFEST, DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)));
    }
}
