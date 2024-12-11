package org.oagi.score.repo.component.release;

import org.jooq.*;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.data.Release;
import org.oagi.score.gateway.http.api.agency_id_management.service.AgencyIdService;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.api.cc_management.service.CcNodeService;
import org.oagi.score.gateway.http.api.code_list_management.service.CodeListService;
import org.oagi.score.gateway.http.api.release_management.data.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.repo.api.impl.jooq.entity.Keys;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ReleaseRecord;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repository.ScoreRepository;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.CcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.release_management.data.ReleaseState.*;
import static org.oagi.score.gateway.http.helper.ScoreGuid.randomGuid;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.service.common.data.CcState.Candidate;
import static org.oagi.score.service.common.data.CcState.ReleaseDraft;

@Repository
public class ReleaseRepository implements ScoreRepository<Release> {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CcNodeService ccNodeService;

    @Autowired
    private CodeListService codeListService;

    @Autowired
    private AgencyIdService agencyIdService;

    private SelectOnConditionStep<Record12<
            ULong, String, String, String, String,
            String, ULong, ULong, ULong, LocalDateTime,
            ULong, LocalDateTime>> getSelectReleaseStep() {
        return dslContext.select(RELEASE.RELEASE_ID, RELEASE.GUID, RELEASE.RELEASE_NUM,
                RELEASE.RELEASE_NOTE, RELEASE.RELEASE_LICENSE, RELEASE.STATE,
                LIBRARY.LIBRARY_ID, RELEASE.NAMESPACE_ID, RELEASE.CREATED_BY, RELEASE.CREATION_TIMESTAMP,
                RELEASE.LAST_UPDATED_BY, RELEASE.LAST_UPDATE_TIMESTAMP)
                .from(RELEASE)
                .join(LIBRARY).on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID));
    }

    @Override
    public List<Release> findAll() {
        return getSelectReleaseStep().fetchInto(Release.class);
    }

    @Override
    public Release findById(BigInteger id) {
        if (id == null || id.longValue() <= 0L) {
            return null;
        }
        return getSelectReleaseStep()
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(id)))
                .fetchOneInto(Release.class);
    }

    public List<Release> findByReleaseNum(BigInteger libraryId, String releaseNum) {
        return getSelectReleaseStep()
                .where(and(
                        RELEASE.LIBRARY_ID.eq(ULong.valueOf(libraryId)),
                        RELEASE.RELEASE_NUM.eq(releaseNum)
                ))
                .fetchInto(Release.class);
    }

    public Map<BigInteger, BigInteger> getReleaseIdMapByAsccpManifestIdList(List<BigInteger> asccpManifestIdList) {
        if (asccpManifestIdList == null || asccpManifestIdList.isEmpty()) {
            return Collections.emptyMap();
        }
        return dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.RELEASE_ID)
                .from(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(
                        asccpManifestIdList.stream().map(e -> ULong.valueOf(e)).collect(Collectors.toList())))
                .fetchStream().collect(Collectors.toMap(
                        e -> e.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger(),
                        e -> e.get(ASCCP_MANIFEST.RELEASE_ID).toBigInteger()));
    }

    public Release getWorkingRelease(BigInteger libraryId) {
        List<Release> releases = findByReleaseNum(libraryId, "Working");
        if (releases.size() != 1) {
            throw new IllegalStateException();
        }
        return releases.get(0);
    }

    public Release getLatestRelease(BigInteger libraryId) {
        return dslContext.select()
                .from(RELEASE)
                .where(and(
                        RELEASE.LIBRARY_ID.eq(ULong.valueOf(libraryId)),
                        RELEASE.RELEASE_NUM.notEqual("Working")
                ))
                .orderBy(RELEASE.RELEASE_ID.desc())
                .limit(1)
                .fetchOneInto(Release.class);
    }

    private void ensureUniqueReleaseNum(BigInteger releaseId, String releaseNum) {
        List<Condition> conditions = new ArrayList();
        conditions.add(RELEASE.RELEASE_NUM.eq(releaseNum));
        if (releaseId != null) {
            conditions.add(RELEASE.RELEASE_ID.ne(ULong.valueOf(releaseId)));
        }
        if (dslContext.selectCount()
                .from(RELEASE)
                .where(conditions)
                .fetchOptionalInto(Integer.class).orElse(0) > 0) {
            throw new IllegalArgumentException("'" + releaseNum + "' is already exist.");
        }
    }

    public ReleaseRecord create(BigInteger userId,
                                String releaseNum,
                                String releaseNote,
                                String releaseLicense,
                                BigInteger libraryId,
                                BigInteger namespaceId) {

        ensureUniqueReleaseNum(null, releaseNum);

        LocalDateTime timestamp = LocalDateTime.now();
        ReleaseRecord releaseRecord = dslContext.insertInto(RELEASE)
                .set(RELEASE.GUID, randomGuid())
                .set(RELEASE.RELEASE_NUM, releaseNum)
                .set(RELEASE.RELEASE_NOTE, releaseNote)
                .set(RELEASE.RELEASE_LICENSE, releaseLicense)
                .set(RELEASE.LIBRARY_ID, ULong.valueOf(libraryId))
                .set(RELEASE.NAMESPACE_ID, (namespaceId != null) ? ULong.valueOf(namespaceId) : null)
                .set(RELEASE.STATE, Initialized.name())
                .set(RELEASE.CREATED_BY, ULong.valueOf(userId))
                .set(RELEASE.CREATION_TIMESTAMP, timestamp)
                .set(RELEASE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .returning().fetchOne();

        return releaseRecord;
    }

    public void update(BigInteger userId,
                       BigInteger releaseId,
                       String releaseNum,
                       String releaseNote,
                       String releaseLicense,
                       BigInteger namespaceId) {

        ensureUniqueReleaseNum(releaseId, releaseNum);

        LocalDateTime timestamp = LocalDateTime.now();
        dslContext.update(RELEASE)
                .set(RELEASE.RELEASE_NUM, releaseNum)
                .set(RELEASE.RELEASE_NOTE, releaseNote)
                .set(RELEASE.RELEASE_LICENSE, releaseLicense)
                .set(RELEASE.NAMESPACE_ID, (namespaceId != null) ? ULong.valueOf(namespaceId) : null)
                .set(RELEASE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
    }

    public void discard(ReleaseRepositoryDiscardRequest request) {
        ReleaseRecord releaseRecord = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())))
                .fetchOne();

        if ("Working".equals(releaseRecord.getReleaseNum())) {
            throw new IllegalArgumentException("'Working' release cannot be discarded.");
        }

        if (Initialized != ReleaseState.valueOf(releaseRecord.getState())) {
            throw new IllegalArgumentException("Only the release in '" + Initialized + "' can discard.");
        }

        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        if (!user.isDeveloper()) {
            throw new IllegalArgumentException("It only allows to discard the release for developers.");
        }

        if (dslContext.selectFrom(MODULE_SET_RELEASE)
                .where(MODULE_SET_RELEASE.RELEASE_ID.eq(releaseRecord.getReleaseId())).fetch().size() > 0) {
            throw new IllegalArgumentException("It cannot be discarded because there are dependent module set releases.");
        }

        releaseRecord.delete();
    }

    public void updateState(BigInteger userId,
                            BigInteger releaseId,
                            ReleaseState releaseState) {
        AppUser appUser = sessionService.getAppUserByUserId(userId);
        if (!appUser.isDeveloper()) {
            throw new IllegalArgumentException("It only allows to update the state of the release for developers.");
        }

        LocalDateTime timestamp = LocalDateTime.now();
        dslContext.update(RELEASE)
                .set(RELEASE.STATE, releaseState.name())
                .set(RELEASE.LAST_UPDATED_BY, ULong.valueOf(userId))
                .set(RELEASE.LAST_UPDATE_TIMESTAMP, timestamp)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
    }

    public void copyWorkingManifestsTo(
            BigInteger releaseId,
            List<BigInteger> accManifestIds,
            List<BigInteger> asccpManifestIds,
            List<BigInteger> bccpManifestIds,
            List<BigInteger> bdtManifestIds,
            List<BigInteger> codeListManifestIds,
            List<BigInteger> agencyIdListManifestIds) {

        ULong libraryId = dslContext.select(RELEASE.LIBRARY_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchOneInto(ULong.class);

        ReleaseRecord releaseRecord = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchOne();
        ReleaseRecord workingReleaseRecord = dslContext.selectFrom(RELEASE)
                .where(and(RELEASE.RELEASE_NUM.eq("Working"), RELEASE.LIBRARY_ID.eq(libraryId)))
                .fetchOne();

        if (workingReleaseRecord == null) {
            throw new IllegalStateException("Cannot find 'Working' release");
        }

        List<BigInteger> xbtManifestIds = Collections.emptyList();

        try {
            // copying manifests from 'Working' release
            releaseId = releaseRecord.getReleaseId().toBigInteger();
            BigInteger workingReleaseId = workingReleaseRecord.getReleaseId().toBigInteger();

            copyDtManifestRecordsFromWorking(releaseId, workingReleaseId, bdtManifestIds);

            copyDtScManifestRecordsFromWorking(releaseId, workingReleaseId, bdtManifestIds);
            updateBdtScDependencies(releaseId, workingReleaseId);

            copyAccManifestRecordsFromWorking(releaseId, workingReleaseId, accManifestIds);

            copyAsccpManifestRecordsFromWorking(releaseId, workingReleaseId, asccpManifestIds);
            updateAsccpDependencies(releaseId, workingReleaseId);

            copyBccpManifestRecordsFromWorking(releaseId, workingReleaseId, bccpManifestIds);
            updateBccpDependencies(releaseId, workingReleaseId);

            copyAsccManifestRecordsFromWorking(releaseId, workingReleaseId, accManifestIds);
            updateAsccDependencies(releaseId, workingReleaseId);

            copyBccManifestRecordsFromWorking(releaseId, workingReleaseId, accManifestIds);
            updateBccDependencies(releaseId, workingReleaseId);

            // Run after ASCC/BCC dependency resolved b/c ASCC/BCC use ACC's state.
            updateAccDependencies(releaseId, workingReleaseId);
            updateDtDependencies(releaseId, workingReleaseId);

            copySeqKeyRecordsFromWorking(releaseId);
            updateSeqKeyPrevNext(releaseId);

            copyXbtManifestRecordsFromWorking(releaseId, workingReleaseId, xbtManifestIds);
            updateXbtDependencies(releaseId);

            copyCodeListManifestRecordsFromWorking(releaseId, workingReleaseId, codeListManifestIds);
            updateCodeListDependencies(releaseId, workingReleaseId);

            copyCodeListValueManifestRecordsFromWorking(releaseId, workingReleaseId, codeListManifestIds);
            updateCodeListValueDependencies(releaseId, workingReleaseId);

            copyAgencyIdListManifestRecordsFromWorking(releaseId, workingReleaseId, agencyIdListManifestIds);
            updateAgencyIdListDependencies(releaseId, workingReleaseId);

            copyAgencyIdListValueManifestRecordsFromWorking(releaseId, workingReleaseId, agencyIdListManifestIds);
            updateAgencyIdListValueDependencies(releaseId, workingReleaseId);

            copyBdtPriRestriFromWorking(releaseId, workingReleaseId, bdtManifestIds);
            copyBdtScPriRestriFromWorking(releaseId, workingReleaseId, bdtManifestIds);

        } catch (Exception e) {
            releaseRecord.setReleaseNote(e.getMessage());
            releaseRecord.update(RELEASE.RELEASE_NOTE);
        }
    }

    private void copyAccManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                   List<BigInteger> accManifestIds) {
        dslContext.insertInto(ACC_MANIFEST,
                ACC_MANIFEST.RELEASE_ID,
                ACC_MANIFEST.ACC_ID,
                ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                ACC_MANIFEST.DEN,
                ACC_MANIFEST.CONFLICT,
                ACC_MANIFEST.LOG_ID,
                ACC_MANIFEST.PREV_ACC_MANIFEST_ID,
                ACC_MANIFEST.NEXT_ACC_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        ACC_MANIFEST.ACC_ID,
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                        ACC_MANIFEST.DEN,
                        ACC_MANIFEST.CONFLICT,
                        ACC_MANIFEST.LOG_ID,
                        ACC_MANIFEST.PREV_ACC_MANIFEST_ID,
                        ACC_MANIFEST.ACC_MANIFEST_ID)
                        .from(ACC_MANIFEST)
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(and(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNotNull(),
                                        ACC_MANIFEST.ACC_MANIFEST_ID.in(accManifestIds)))))).execute();

        // Copy tags
        dslContext.insertInto(ACC_MANIFEST_TAG,
                        ACC_MANIFEST_TAG.ACC_MANIFEST_ID,
                        ACC_MANIFEST_TAG.TAG_ID,
                        ACC_MANIFEST_TAG.CREATED_BY,
                        ACC_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext.select(
                                ACC_MANIFEST.ACC_MANIFEST_ID,
                                ACC_MANIFEST_TAG.TAG_ID,
                                ACC_MANIFEST_TAG.CREATED_BY,
                                ACC_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(ACC_MANIFEST)
                        .join(ACC_MANIFEST.as("next")).on(and(
                                ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("next").ACC_MANIFEST_ID),
                                ACC_MANIFEST.as("next").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))))
                        .join(ACC_MANIFEST_TAG).on(ACC_MANIFEST.as("next").ACC_MANIFEST_ID.eq(ACC_MANIFEST_TAG.ACC_MANIFEST_ID))
                        .where(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))).execute();
    }

    private void copyBdtPriRestriFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                             List<BigInteger> dtManifestIds) {
        dslContext.insertInto(BDT_PRI_RESTRI,
                        BDT_PRI_RESTRI.BDT_MANIFEST_ID,
                        BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                        BDT_PRI_RESTRI.IS_DEFAULT)
                .select(dslContext.select(
                                DT_MANIFEST.as("target").DT_MANIFEST_ID,
                                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                                BDT_PRI_RESTRI.IS_DEFAULT)
                        .from(DT_MANIFEST.as("working"))
                        .join(DT_MANIFEST.as("target")).on(and(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(DT_MANIFEST.as("target").NEXT_DT_MANIFEST_ID),
                                DT_MANIFEST.as("target").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .join(BDT_PRI_RESTRI.forceIndexForJoin(Keys.BDT_PRI_RESTRI_BDT_MANIFEST_ID_FK.getName())).on(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(BDT_PRI_RESTRI.BDT_MANIFEST_ID))
                        .where(and(DT_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.isNotNull(),
                                (or(DT_MANIFEST.as("working").PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.as("working").DT_MANIFEST_ID.in(dtManifestIds)))))).execute();

        dslContext.insertInto(BDT_PRI_RESTRI,
                        BDT_PRI_RESTRI.BDT_MANIFEST_ID,
                        BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID,
                        BDT_PRI_RESTRI.IS_DEFAULT)
                .select(dslContext.select(
                                DT_MANIFEST.as("target").DT_MANIFEST_ID,
                                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                                BDT_PRI_RESTRI.IS_DEFAULT)
                        .from(DT_MANIFEST.as("working"))
                        .join(DT_MANIFEST.as("target")).on(and(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(DT_MANIFEST.as("target").NEXT_DT_MANIFEST_ID),
                                DT_MANIFEST.as("target").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .join(BDT_PRI_RESTRI.forceIndexForJoin(Keys.BDT_PRI_RESTRI_BDT_MANIFEST_ID_FK.getName())).on(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(BDT_PRI_RESTRI.BDT_MANIFEST_ID))
                        .join(CODE_LIST_MANIFEST).on(and(
                                CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID),
                                CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .where(and(DT_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_MANIFEST.as("working").PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.as("working").DT_MANIFEST_ID.in(dtManifestIds)))))).execute();

        dslContext.insertInto(BDT_PRI_RESTRI,
                        BDT_PRI_RESTRI.BDT_MANIFEST_ID,
                        BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID,
                        BDT_PRI_RESTRI.IS_DEFAULT)
                .select(dslContext.select(
                                DT_MANIFEST.as("target").DT_MANIFEST_ID,
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                                BDT_PRI_RESTRI.IS_DEFAULT)
                        .from(DT_MANIFEST.as("working"))
                        .join(DT_MANIFEST.as("target")).on(and(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(DT_MANIFEST.as("target").NEXT_DT_MANIFEST_ID),
                                DT_MANIFEST.as("target").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .join(BDT_PRI_RESTRI.forceIndexForJoin(Keys.BDT_PRI_RESTRI_BDT_MANIFEST_ID_FK.getName())).on(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(BDT_PRI_RESTRI.BDT_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_MANIFEST).on(and(
                                AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID),
                                AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .where(and(DT_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_MANIFEST.as("working").PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.as("working").DT_MANIFEST_ID.in(dtManifestIds)))))).execute();
    }

    private void copyBdtScPriRestriFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                               List<BigInteger> dtManifestIds) {
        dslContext.insertInto(BDT_SC_PRI_RESTRI,
                        BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID,
                        BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                        BDT_SC_PRI_RESTRI.IS_DEFAULT)
                .select(dslContext.select(
                                DT_SC_MANIFEST.as("target").DT_SC_MANIFEST_ID,
                                BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                                BDT_SC_PRI_RESTRI.IS_DEFAULT)
                        .from(DT_SC_MANIFEST.as("working"))
                        .join(DT_SC_MANIFEST.as("target")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("target").NEXT_DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("target").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .join(BDT_SC_PRI_RESTRI.forceIndexForJoin(Keys.BDT_SC_PRI_RESTRI_BDT_MANIFEST_ID_FK.getName())).on(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID))
                        .where(and(DT_SC_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.isNotNull(),
                                (or(DT_SC_MANIFEST.as("working").PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.as("working").OWNER_DT_MANIFEST_ID.in(dtManifestIds)))))).execute();

        dslContext.insertInto(BDT_SC_PRI_RESTRI,
                        BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID,
                        BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID,
                        BDT_SC_PRI_RESTRI.IS_DEFAULT)
                .select(dslContext.select(
                                DT_SC_MANIFEST.as("target").DT_SC_MANIFEST_ID,
                                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                                BDT_SC_PRI_RESTRI.IS_DEFAULT)
                        .from(DT_SC_MANIFEST.as("working"))
                        .join(DT_SC_MANIFEST.as("target")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("target").NEXT_DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("target").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .join(BDT_SC_PRI_RESTRI.forceIndexForJoin(Keys.BDT_SC_PRI_RESTRI_BDT_MANIFEST_ID_FK.getName())).on(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID))
                        .join(CODE_LIST_MANIFEST).on(and(
                                CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID),
                                CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .where(and(DT_SC_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BDT_SC_PRI_RESTRI.CODE_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_SC_MANIFEST.as("working").PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.as("working").OWNER_DT_MANIFEST_ID.in(dtManifestIds)))))).execute();

        dslContext.insertInto(BDT_SC_PRI_RESTRI,
                        BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID,
                        BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID,
                        BDT_SC_PRI_RESTRI.IS_DEFAULT)
                .select(dslContext.select(
                                DT_SC_MANIFEST.as("target").DT_SC_MANIFEST_ID,
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                                BDT_SC_PRI_RESTRI.IS_DEFAULT)
                        .from(DT_SC_MANIFEST.as("working"))
                        .join(DT_SC_MANIFEST.as("target")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("target").NEXT_DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("target").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .join(BDT_SC_PRI_RESTRI.forceIndexForJoin(Keys.BDT_SC_PRI_RESTRI_BDT_MANIFEST_ID_FK.getName())).on(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_MANIFEST).on(and(
                                AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID),
                                AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                        .where(and(DT_SC_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_SC_MANIFEST.as("working").PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.as("working").OWNER_DT_MANIFEST_ID.in(dtManifestIds)))))).execute();
    }

    private void copyDtManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                  List<BigInteger> dtManifestIds) {
        dslContext.insertInto(DT_MANIFEST,
                DT_MANIFEST.RELEASE_ID,
                DT_MANIFEST.DT_ID,
                DT_MANIFEST.BASED_DT_MANIFEST_ID,
                DT_MANIFEST.DEN,
                DT_MANIFEST.CONFLICT,
                DT_MANIFEST.LOG_ID,
                DT_MANIFEST.PREV_DT_MANIFEST_ID,
                DT_MANIFEST.NEXT_DT_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        DT_MANIFEST.DT_ID,
                        DT_MANIFEST.BASED_DT_MANIFEST_ID,
                        DT_MANIFEST.DEN,
                        DT_MANIFEST.CONFLICT,
                        DT_MANIFEST.LOG_ID,
                        DT_MANIFEST.PREV_DT_MANIFEST_ID,
                        DT_MANIFEST.DT_MANIFEST_ID)
                        .from(DT_MANIFEST)
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .where(and(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(DT_MANIFEST.PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.DT_MANIFEST_ID.in(dtManifestIds)))))).execute();

        // Copy tags
        dslContext.insertInto(DT_MANIFEST_TAG,
                DT_MANIFEST_TAG.DT_MANIFEST_ID,
                DT_MANIFEST_TAG.TAG_ID,
                DT_MANIFEST_TAG.CREATED_BY,
                DT_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext.select(
                        DT_MANIFEST.DT_MANIFEST_ID,
                        DT_MANIFEST_TAG.TAG_ID,
                        DT_MANIFEST_TAG.CREATED_BY,
                        DT_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(DT_MANIFEST)
                        .join(DT_MANIFEST.as("next")).on(and(
                                DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("next").DT_MANIFEST_ID),
                                DT_MANIFEST.as("next").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))))
                        .join(DT_MANIFEST_TAG).on(DT_MANIFEST.as("next").DT_MANIFEST_ID.eq(DT_MANIFEST_TAG.DT_MANIFEST_ID))
                        .where(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))).execute();
    }

    private void copyAsccpManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                     List<BigInteger> asccpManifestIds) {
        dslContext.insertInto(ASCCP_MANIFEST,
                ASCCP_MANIFEST.RELEASE_ID,
                ASCCP_MANIFEST.ASCCP_ID,
                ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                ASCCP_MANIFEST.DEN,
                ASCCP_MANIFEST.CONFLICT,
                ASCCP_MANIFEST.LOG_ID,
                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        ASCCP_MANIFEST.ASCCP_ID,
                        ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                        ASCCP_MANIFEST.DEN,
                        ASCCP_MANIFEST.CONFLICT,
                        ASCCP_MANIFEST.LOG_ID,
                        ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .where(and(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNotNull(),
                                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(asccpManifestIds)))))).execute();

        // Copy tags
        dslContext.insertInto(ASCCP_MANIFEST_TAG,
                        ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID,
                        ASCCP_MANIFEST_TAG.TAG_ID,
                        ASCCP_MANIFEST_TAG.CREATED_BY,
                        ASCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext.select(
                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                                ASCCP_MANIFEST_TAG.TAG_ID,
                                ASCCP_MANIFEST_TAG.CREATED_BY,
                                ASCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP_MANIFEST.as("next")).on(and(
                                ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID),
                                ASCCP_MANIFEST.as("next").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))))
                        .join(ASCCP_MANIFEST_TAG).on(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID))
                        .where(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))).execute();
    }

    private void copyBccpManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                    List<BigInteger> bccpManifestIds) {
        dslContext.insertInto(BCCP_MANIFEST,
                BCCP_MANIFEST.RELEASE_ID,
                BCCP_MANIFEST.BCCP_ID,
                BCCP_MANIFEST.BDT_MANIFEST_ID,
                BCCP_MANIFEST.DEN,
                BCCP_MANIFEST.CONFLICT,
                BCCP_MANIFEST.LOG_ID,
                BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID,
                BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        BCCP_MANIFEST.BCCP_ID,
                        BCCP_MANIFEST.BDT_MANIFEST_ID,
                        BCCP_MANIFEST.DEN,
                        BCCP_MANIFEST.CONFLICT,
                        BCCP_MANIFEST.LOG_ID,
                        BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID,
                        BCCP_MANIFEST.BCCP_MANIFEST_ID)
                        .from(BCCP_MANIFEST)
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .where(and(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNotNull(),
                                        BCCP_MANIFEST.BCCP_MANIFEST_ID.in(bccpManifestIds)))))).execute();

        // Copy tags
        dslContext.insertInto(BCCP_MANIFEST_TAG,
                        BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID,
                        BCCP_MANIFEST_TAG.TAG_ID,
                        BCCP_MANIFEST_TAG.CREATED_BY,
                        BCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext.select(
                                BCCP_MANIFEST.BCCP_MANIFEST_ID,
                                BCCP_MANIFEST_TAG.TAG_ID,
                                BCCP_MANIFEST_TAG.CREATED_BY,
                                BCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(BCCP_MANIFEST)
                        .join(BCCP_MANIFEST.as("next")).on(and(
                                BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID),
                                BCCP_MANIFEST.as("next").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))))
                        .join(BCCP_MANIFEST_TAG).on(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID.eq(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID))
                        .where(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))).execute();
    }

    private void copyAsccManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                    List<BigInteger> accManifestIds) {
        dslContext.insertInto(ASCC_MANIFEST,
                ASCC_MANIFEST.RELEASE_ID,
                ASCC_MANIFEST.ASCC_ID,
                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                ASCC_MANIFEST.DEN,
                ASCC_MANIFEST.CONFLICT,
                ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID,
                ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        ASCC_MANIFEST.ASCC_ID,
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                        ASCC_MANIFEST.DEN,
                        ASCC_MANIFEST.CONFLICT,
                        ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID,
                        ASCC_MANIFEST.ASCC_MANIFEST_ID)
                        .from(ASCC_MANIFEST)
                        .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                        .where(and(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNotNull(),
                                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIds)))))).execute();
    }

    private void copyBccManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                   List<BigInteger> accManifestIds) {
        dslContext.insertInto(BCC_MANIFEST,
                BCC_MANIFEST.RELEASE_ID,
                BCC_MANIFEST.BCC_ID,
                BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                BCC_MANIFEST.DEN,
                BCC_MANIFEST.CONFLICT,
                BCC_MANIFEST.PREV_BCC_MANIFEST_ID,
                BCC_MANIFEST.NEXT_BCC_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        BCC_MANIFEST.BCC_ID,
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                        BCC_MANIFEST.DEN,
                        BCC_MANIFEST.CONFLICT,
                        BCC_MANIFEST.PREV_BCC_MANIFEST_ID,
                        BCC_MANIFEST.BCC_MANIFEST_ID)
                        .from(BCC_MANIFEST)
                        .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                        .where(and(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNotNull(),
                                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIds)))))).execute();
    }

    private void copySeqKeyRecordsFromWorking(BigInteger releaseId) {
        // insert ASCC SEQ_KEY Records
        dslContext.insertInto(SEQ_KEY,
                SEQ_KEY.FROM_ACC_MANIFEST_ID,
                SEQ_KEY.ASCC_MANIFEST_ID)
                .select(dslContext.select(
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        ASCC_MANIFEST.ASCC_MANIFEST_ID)
                        .from(ASCC_MANIFEST)
                        .join(SEQ_KEY).on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(SEQ_KEY.ASCC_MANIFEST_ID))
                        .where(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))).execute();
        
        // insert BCC SEQ_KEY Records
        dslContext.insertInto(SEQ_KEY,
                SEQ_KEY.FROM_ACC_MANIFEST_ID,
                SEQ_KEY.BCC_MANIFEST_ID)
                .select(dslContext.select(
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        BCC_MANIFEST.BCC_MANIFEST_ID)
                        .from(BCC_MANIFEST)
                        .join(SEQ_KEY).on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(SEQ_KEY.BCC_MANIFEST_ID))
                        .where(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))).execute();

        // Link SEQ_KEY to Manifest
        dslContext.update(ASCC_MANIFEST
                .join(SEQ_KEY).on(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(SEQ_KEY.ASCC_MANIFEST_ID)))
                .set(ASCC_MANIFEST.SEQ_KEY_ID, SEQ_KEY.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))).execute();

        dslContext.update(BCC_MANIFEST
                .join(SEQ_KEY).on(BCC_MANIFEST.BCC_MANIFEST_ID.eq(SEQ_KEY.BCC_MANIFEST_ID)))
                .set(BCC_MANIFEST.SEQ_KEY_ID, SEQ_KEY.SEQ_KEY_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))).execute();
    }

    private void updateSeqKeyPrevNext(BigInteger releaseId) {
        // Update prev/next seq_key for ASCC
        dslContext.update(SEQ_KEY
                .join(ASCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(ASCC_MANIFEST.SEQ_KEY_ID))
                .join(SEQ_KEY.as("working_seq")).on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(SEQ_KEY.as("working_seq").ASCC_MANIFEST_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("working_prev_ascc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_prev_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                        and(ASCC_MANIFEST.as("working_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").NEXT_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("working_prev_bcc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_prev_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                        and(BCC_MANIFEST.as("working_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").NEXT_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(ASCC_MANIFEST.as("working_next_ascc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_next_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                        and(ASCC_MANIFEST.as("working_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").NEXT_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("working_next_bcc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_next_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                        and(BCC_MANIFEST.as("working_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").NEXT_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))
        )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))).execute();

        // Update prev/next seq_key for BCC
        dslContext.update(SEQ_KEY
                .join(BCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(BCC_MANIFEST.SEQ_KEY_ID))
                .join(SEQ_KEY.as("working_seq")).on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(SEQ_KEY.as("working_seq").BCC_MANIFEST_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("working_prev_ascc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_prev_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                        and(ASCC_MANIFEST.as("working_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").NEXT_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("working_prev_bcc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_prev_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                        and(BCC_MANIFEST.as("working_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").NEXT_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(ASCC_MANIFEST.as("working_next_ascc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_next_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                        and(ASCC_MANIFEST.as("working_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").NEXT_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("working_next_bcc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_next_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                        and(BCC_MANIFEST.as("working_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").NEXT_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))
        )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))).execute();

        // Update prev/next seq_key for unassigned ASCC.
        dslContext.update(SEQ_KEY
                .join(ASCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(ASCC_MANIFEST.SEQ_KEY_ID))
                .join(ASCC_MANIFEST.as("working_ascc")).on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("working_ascc").ASCC_MANIFEST_ID))
                .join(ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ASCC_MANIFEST.as("working_ascc").FROM_ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(SEQ_KEY.as("last_seq")).on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(SEQ_KEY.as("last_seq").ASCC_MANIFEST_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("last_prev_ascc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_prev_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                        and(ASCC_MANIFEST.as("last_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").PREV_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("last_prev_bcc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_prev_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                        and(BCC_MANIFEST.as("last_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").PREV_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(ASCC_MANIFEST.as("last_next_ascc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_next_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                        and(ASCC_MANIFEST.as("last_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").PREV_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("last_next_bcc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_next_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                        and(BCC_MANIFEST.as("last_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").PREV_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))
        )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(and(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published)))).execute();

        // Update prev/next seq_key for unassigned BCC.
        dslContext.update(SEQ_KEY
                .join(BCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(BCC_MANIFEST.SEQ_KEY_ID))
                .join(BCC_MANIFEST.as("working_bcc")).on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("working_bcc").BCC_MANIFEST_ID))
                .join(ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(BCC_MANIFEST.as("working_bcc").FROM_ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(SEQ_KEY.as("last_seq")).on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(SEQ_KEY.as("last_seq").BCC_MANIFEST_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("last_prev_ascc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_prev_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                        and(ASCC_MANIFEST.as("last_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").PREV_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("last_prev_bcc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_prev_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                        and(BCC_MANIFEST.as("last_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").PREV_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(ASCC_MANIFEST.as("last_next_ascc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_next_ascc").SEQ_KEY_ID))
                .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                        and(ASCC_MANIFEST.as("last_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").PREV_ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(ULong.valueOf(releaseId))))

                .leftOuterJoin(BCC_MANIFEST.as("last_next_bcc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_next_bcc").SEQ_KEY_ID))
                .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                        and(BCC_MANIFEST.as("last_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").PREV_BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(ULong.valueOf(releaseId))))
        )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(and(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published)))).execute();
    }

    private void copyDtScManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                    List<BigInteger> dtManifestIds) {
        dslContext.insertInto(DT_SC_MANIFEST,
                DT_SC_MANIFEST.RELEASE_ID,
                DT_SC_MANIFEST.DT_SC_ID,
                DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID,
                DT_SC_MANIFEST.CONFLICT,
                DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID,
                DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        DT_SC_MANIFEST.DT_SC_ID,
                        DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                        DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID,
                        DT_SC_MANIFEST.CONFLICT,
                        DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID,
                        DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                        .from(DT_SC_MANIFEST)
                        .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.in(dtManifestIds)))))).execute();
    }

    private void copyXbtManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                   List<BigInteger> xbtManifestIds) {
        dslContext.insertInto(XBT_MANIFEST,
                XBT_MANIFEST.RELEASE_ID,
                XBT_MANIFEST.XBT_ID,
                XBT_MANIFEST.CONFLICT,
                XBT_MANIFEST.LOG_ID,
                XBT_MANIFEST.PREV_XBT_MANIFEST_ID,
                XBT_MANIFEST.NEXT_XBT_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        XBT_MANIFEST.XBT_ID,
                        XBT_MANIFEST.CONFLICT,
                        XBT_MANIFEST.LOG_ID,
                        XBT_MANIFEST.PREV_XBT_MANIFEST_ID,
                        XBT_MANIFEST.XBT_MANIFEST_ID)
                        .from(XBT_MANIFEST)
                        .where(XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId))))
                .execute();
    }

    private void copyCodeListManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                        List<BigInteger> codeListManifestIds) {
        dslContext.insertInto(CODE_LIST_MANIFEST,
                CODE_LIST_MANIFEST.RELEASE_ID,
                CODE_LIST_MANIFEST.CODE_LIST_ID,
                CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                CODE_LIST_MANIFEST.CONFLICT,
                CODE_LIST_MANIFEST.LOG_ID,
                CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        CODE_LIST_MANIFEST.CODE_LIST_ID,
                        CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                        CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        CODE_LIST_MANIFEST.CONFLICT,
                        CODE_LIST_MANIFEST.LOG_ID,
                        CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                        .from(CODE_LIST_MANIFEST)
                        .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .where(and(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNotNull(),
                                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(codeListManifestIds)))))).execute();
    }

    private void copyCodeListValueManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                             List<BigInteger> codeListManifestIds) {
        dslContext.insertInto(CODE_LIST_VALUE_MANIFEST,
                CODE_LIST_VALUE_MANIFEST.RELEASE_ID,
                CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID,
                CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID,
                CODE_LIST_VALUE_MANIFEST.CONFLICT,
                CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID,
                CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID,
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID,
                        CODE_LIST_VALUE_MANIFEST.CONFLICT,
                        CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID,
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                        .from(CODE_LIST_VALUE_MANIFEST)
                        .join(CODE_LIST_MANIFEST).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                        .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .where(and(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNotNull(),
                                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(codeListManifestIds)))))).execute();
    }

    private void copyAgencyIdListManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                            List<BigInteger> agencyIdListManifestIds) {
        dslContext.insertInto(AGENCY_ID_LIST_MANIFEST,
                AGENCY_ID_LIST_MANIFEST.RELEASE_ID,
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                AGENCY_ID_LIST_MANIFEST.CONFLICT,
                AGENCY_ID_LIST_MANIFEST.LOG_ID,
                AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID,
                        AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        AGENCY_ID_LIST_MANIFEST.CONFLICT,
                        AGENCY_ID_LIST_MANIFEST.LOG_ID,
                        AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_MANIFEST)
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(agencyIdListManifestIds)))))).execute();
    }

    private void copyAgencyIdListValueManifestRecordsFromWorking(BigInteger releaseId, BigInteger workingReleaseId,
                                                                 List<BigInteger> agencyIdListManifestIds) {
        dslContext.insertInto(AGENCY_ID_LIST_VALUE_MANIFEST,
                AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID,
                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID,
                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST_VALUE_MANIFEST.CONFLICT,
                AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .select(dslContext.select(
                        inline(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.CONFLICT,
                        AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                (or(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(agencyIdListManifestIds)))))).execute();
    }

    private void updateAccDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(ACC_MANIFEST.join(ACC_MANIFEST.as("based"))
                .on(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("based").NEXT_ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ACC_MANIFEST.as("based").ACC_MANIFEST_ID)
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC_MANIFEST.as("based").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // Set ACC_MANIFEST.ACC_ID to PREV.ACC_ID if the ACC in Working branch is not in ReleaseDraft nor Published states.
        dslContext.update(ACC_MANIFEST
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(ACC_MANIFEST.as("prev")).on(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.ACC_ID, ACC_MANIFEST.as("prev").ACC_ID)
                .set(ACC_MANIFEST.LOG_ID, ACC_MANIFEST.as("prev").LOG_ID)
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update ACC_MANIFEST.BASED_ACC_MANIFEST_ID if ACC_MANIFEST.BASED_MANIFEST.ACC_ID is not equal to ACC.BASED_ACC_ID
        dslContext.update(ACC_MANIFEST
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(ACC.as("based")).on(ACC.BASED_ACC_ID.eq(ACC.as("based").ACC_ID))
                        .join(ACC.as("revised")).on(ACC.as("based").GUID.eq(ACC.as("revised").GUID)) // for the case that the associated component has revised
                        .join(ACC_MANIFEST.as("based_manifest")).on(and(
                                ACC.as("revised").ACC_ID.eq(ACC_MANIFEST.as("based_manifest").ACC_ID),
                                ACC_MANIFEST.as("based_manifest").RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID))))
                .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ACC_MANIFEST.as("based_manifest").ACC_MANIFEST_ID)
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID.notEqual(ACC_MANIFEST.as("based_manifest").ACC_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext.update(ACC_MANIFEST.as("working")
                        .join(ACC_MANIFEST.as("release")).on(and(
                                ACC_MANIFEST.as("working").ACC_ID.eq(ACC_MANIFEST.as("release").ACC_ID),
                                ACC_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                ACC_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(ACC_MANIFEST.as("replacement_in_working")).on(and(
                                ACC_MANIFEST.as("working").REPLACEMENT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("replacement_in_working").ACC_MANIFEST_ID),
                                ACC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(ACC_MANIFEST.as("replacement_in_release")).on(and(
                                ACC_MANIFEST.as("replacement_in_working").ACC_ID.eq(ACC_MANIFEST.as("replacement_in_release").ACC_ID),
                                ACC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(ACC_MANIFEST.as("release").REPLACEMENT_ACC_MANIFEST_ID, ACC_MANIFEST.as("replacement_in_release").ACC_MANIFEST_ID)
                .where(and(
                        ACC_MANIFEST.as("working").REPLACEMENT_ACC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAsccpDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(ASCCP_MANIFEST.join(ACC_MANIFEST)
                .on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(and(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // Set ASCCP_MANIFEST.ASCCP_ID to PREV.ASCCP_ID if the ASCCP in Working branch is not in ReleaseDraft nor Published states.
        dslContext.update(ASCCP_MANIFEST
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(ASCCP_MANIFEST.as("prev")).on(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.ASCCP_ID, ASCCP_MANIFEST.as("prev").ASCCP_ID)
                .set(ASCCP_MANIFEST.LOG_ID, ASCCP_MANIFEST.as("prev").LOG_ID)
                .where(and(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ASCCP.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID if ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST.ACC_ID is not equal to ASCCP.ROLE_OF_ACC_ID
        dslContext.update(ASCCP_MANIFEST
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(ACC).on(ASCCP.ROLE_OF_ACC_ID.eq(ACC.ACC_ID))
                .join(ACC.as("revised")).on(ACC.GUID.eq(ACC.as("revised").GUID)) // for the case that the associated component has revised
                .join(ACC_MANIFEST).on(and(
                        ACC.as("revised").ACC_ID.eq(ACC_MANIFEST.ACC_ID),
                        ACC_MANIFEST.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))))
                .set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.notEqual(ACC_MANIFEST.ACC_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext.update(ASCCP_MANIFEST.as("working")
                        .join(ASCCP_MANIFEST.as("release")).on(and(
                                ASCCP_MANIFEST.as("working").ASCCP_ID.eq(ASCCP_MANIFEST.as("release").ASCCP_ID),
                                ASCCP_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                ASCCP_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(ASCCP_MANIFEST.as("replacement_in_working")).on(and(
                                ASCCP_MANIFEST.as("working").REPLACEMENT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("replacement_in_working").ASCCP_MANIFEST_ID),
                                ASCCP_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(ASCCP_MANIFEST.as("replacement_in_release")).on(and(
                                ASCCP_MANIFEST.as("replacement_in_working").ASCCP_ID.eq(ASCCP_MANIFEST.as("replacement_in_release").ASCCP_ID),
                                ASCCP_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(ASCCP_MANIFEST.as("release").REPLACEMENT_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.as("replacement_in_release").ASCCP_MANIFEST_ID)
                .where(and(
                        ASCCP_MANIFEST.as("working").REPLACEMENT_ASCCP_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateBccpDependencies(BigInteger releaseId, BigInteger workingReleaseId) {

        dslContext.update(BCCP_MANIFEST.join(DT_MANIFEST)
                .on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.NEXT_DT_MANIFEST_ID)))
                .set(BCCP_MANIFEST.BDT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(and(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // Set BCCP_MANIFEST.BCCP_ID to PREV.BCCP_ID if the BCCP in Working branch is not in ReleaseDraft nor Published states.
        dslContext.update(BCCP_MANIFEST
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(BCCP_MANIFEST.as("prev")).on(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID)))
                .set(BCCP_MANIFEST.BCCP_ID, BCCP_MANIFEST.as("prev").BCCP_ID)
                .set(BCCP_MANIFEST.LOG_ID, BCCP_MANIFEST.as("prev").LOG_ID)
                .where(and(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        BCCP.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update BCCP_MANIFEST.BDT_MANIFEST_ID if BCCP_MANIFEST.BDT_MANIFEST.BDT_ID is not equal to BCCP.BDT_ID
        dslContext.update(BCCP_MANIFEST
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(DT).on(BCCP.BDT_ID.eq(DT.DT_ID))
                .join(DT.as("revised")).on(DT.GUID.eq(DT.as("revised").GUID)) // for the case that the associated component has revised
                .join(DT_MANIFEST).on(and(
                        DT.as("revised").DT_ID.eq(DT_MANIFEST.DT_ID),
                        DT_MANIFEST.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))))
                .set(BCCP_MANIFEST.BDT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        BCCP_MANIFEST.BDT_MANIFEST_ID.notEqual(DT_MANIFEST.DT_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext.update(BCCP_MANIFEST.as("working")
                        .join(BCCP_MANIFEST.as("release")).on(and(
                                BCCP_MANIFEST.as("working").BCCP_ID.eq(BCCP_MANIFEST.as("release").BCCP_ID),
                                BCCP_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BCCP_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(BCCP_MANIFEST.as("replacement_in_working")).on(and(
                                BCCP_MANIFEST.as("working").REPLACEMENT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("replacement_in_working").BCCP_MANIFEST_ID),
                                BCCP_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(BCCP_MANIFEST.as("replacement_in_release")).on(and(
                                BCCP_MANIFEST.as("replacement_in_working").BCCP_ID.eq(BCCP_MANIFEST.as("replacement_in_release").BCCP_ID),
                                BCCP_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(BCCP_MANIFEST.as("release").REPLACEMENT_BCCP_MANIFEST_ID, BCCP_MANIFEST.as("replacement_in_release").BCCP_MANIFEST_ID)
                .where(and(
                        BCCP_MANIFEST.as("working").REPLACEMENT_BCCP_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAsccDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(ASCC_MANIFEST
                .join(ACC_MANIFEST)
                .on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID))
                .join(ASCCP_MANIFEST)
                .on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID)))
                .set(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .set(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(and(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // Set ASCC_MANIFEST.ASCC_ID to PREV.ASCC_ID if the DT in Working branch is not in ReleaseDraft nor Published states.
        dslContext.update(ASCC_MANIFEST
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .join(ASCC_MANIFEST.as("prev")).on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID))
                .join(ACC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID)))
                .set(ASCC_MANIFEST.ASCC_ID, ASCC_MANIFEST.as("prev").ASCC_ID)
                .where(and(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID if ASCCP_MANIFEST.ASCCP_MANIFEST.ASCCP_ID is not equal to ASCC.TO_ASCCP_ID
        dslContext.update(ASCC_MANIFEST
                        .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                        .join(ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(ASCCP.as("revised")).on(ASCCP.GUID.eq(ASCCP.as("revised").GUID)) // for the case that the associated component has revised
                        .join(ASCCP_MANIFEST).on(and(
                                ASCCP.as("revised").ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID),
                                ASCC_MANIFEST.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))))
                .set(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.notEqual(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext.update(ASCC_MANIFEST.as("working")
                        .join(ASCC_MANIFEST.as("release")).on(and(
                                ASCC_MANIFEST.as("working").ASCC_ID.eq(ASCC_MANIFEST.as("release").ASCC_ID),
                                ASCC_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                ASCC_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(ASCC_MANIFEST.as("replacement_in_working")).on(and(
                                ASCC_MANIFEST.as("working").REPLACEMENT_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("replacement_in_working").ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(ASCC_MANIFEST.as("replacement_in_release")).on(and(
                                ASCC_MANIFEST.as("replacement_in_working").ASCC_ID.eq(ASCC_MANIFEST.as("replacement_in_release").ASCC_ID),
                                ASCC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(ASCC_MANIFEST.as("release").REPLACEMENT_ASCC_MANIFEST_ID, ASCC_MANIFEST.as("replacement_in_release").ASCC_MANIFEST_ID)
                .where(and(
                        ASCC_MANIFEST.as("working").REPLACEMENT_ASCC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateBccDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(BCC_MANIFEST.join(ACC_MANIFEST)
                .on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID))
                .join(BCCP_MANIFEST)
                .on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID)))
                .set(BCC_MANIFEST.FROM_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .set(BCC_MANIFEST.TO_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(and(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // Set BCC_MANIFEST.BCC_ID to PREV.BCC_ID if the DT in Working branch is not in ReleaseDraft nor Published states.
        dslContext.update(BCC_MANIFEST
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .join(BCC_MANIFEST.as("prev")).on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID))
                .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID)))
                .set(BCC_MANIFEST.BCC_ID, BCC_MANIFEST.as("prev").BCC_ID)
                .where(and(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update BCC_MANIFEST.TO_BCCP_MANIFEST_ID if BCCP_MANIFEST.BCCP_MANIFEST.BCCP_ID is not equal to BCC.TO_BCCP_ID
        dslContext.update(BCC_MANIFEST
                        .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                        .join(BCCP).on(BCC.TO_BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(BCCP.as("revised")).on(BCCP.GUID.eq(BCCP.as("revised").GUID)) // for the case that the associated component has revised
                        .join(BCCP_MANIFEST).on(and(
                                BCCP.as("revised").BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID),
                                BCC_MANIFEST.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))))
                .set(BCC_MANIFEST.TO_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID.notEqual(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext.update(BCC_MANIFEST.as("working")
                        .join(BCC_MANIFEST.as("release")).on(and(
                                BCC_MANIFEST.as("working").BCC_ID.eq(BCC_MANIFEST.as("release").BCC_ID),
                                BCC_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                BCC_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(BCC_MANIFEST.as("replacement_in_working")).on(and(
                                BCC_MANIFEST.as("working").REPLACEMENT_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("replacement_in_working").BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(BCC_MANIFEST.as("replacement_in_release")).on(and(
                                BCC_MANIFEST.as("replacement_in_working").BCC_ID.eq(BCC_MANIFEST.as("replacement_in_release").BCC_ID),
                                BCC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(BCC_MANIFEST.as("release").REPLACEMENT_BCC_MANIFEST_ID, BCC_MANIFEST.as("replacement_in_release").BCC_MANIFEST_ID)
                .where(and(
                        BCC_MANIFEST.as("working").REPLACEMENT_BCC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateDtDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(DT_MANIFEST.join(DT_MANIFEST.as("based"))
                .on(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(DT_MANIFEST.as("based").NEXT_DT_MANIFEST_ID)))
                .set(DT_MANIFEST.BASED_DT_MANIFEST_ID, DT_MANIFEST.as("based").DT_MANIFEST_ID)
                .where(and(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT_MANIFEST.as("based").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // Set DT_MANIFEST.DT_ID to PREV.DT_ID if the DT in Working branch is not in ReleaseDraft nor Published states.
        dslContext.update(DT_MANIFEST
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(DT_MANIFEST.as("prev")).on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID)))
                .set(DT_MANIFEST.DT_ID, DT_MANIFEST.as("prev").DT_ID)
                .set(DT_MANIFEST.LOG_ID, DT_MANIFEST.as("prev").LOG_ID)
                .where(and(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update DT_MANIFEST.BASED_DT_MANIFEST_ID if DT_MANIFEST.BASED_MANIFEST.DT_ID is not equal to DT.BASED_DT_ID
        dslContext.update(DT_MANIFEST
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .join(DT.as("based")).on(DT.BASED_DT_ID.eq(DT.as("based").DT_ID))
                        .join(DT.as("revised")).on(DT.as("based").GUID.eq(DT.as("revised").GUID)) // for the case that the associated component has revised
                        .join(DT_MANIFEST.as("based_manifest")).on(and(
                                DT.as("revised").DT_ID.eq(DT_MANIFEST.as("based_manifest").DT_ID),
                                DT_MANIFEST.as("based_manifest").RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))))
                .set(DT_MANIFEST.BASED_DT_MANIFEST_ID, DT_MANIFEST.as("based_manifest").DT_MANIFEST_ID)
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT_MANIFEST.BASED_DT_MANIFEST_ID.notEqual(DT_MANIFEST.as("based_manifest").DT_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext.update(DT_MANIFEST.as("working")
                        .join(DT_MANIFEST.as("release")).on(and(
                                DT_MANIFEST.as("working").DT_ID.eq(DT_MANIFEST.as("release").DT_ID),
                                DT_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                DT_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(DT_MANIFEST.as("replacement_in_working")).on(and(
                                DT_MANIFEST.as("working").REPLACEMENT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("replacement_in_working").DT_MANIFEST_ID),
                                DT_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(DT_MANIFEST.as("replacement_in_release")).on(and(
                                DT_MANIFEST.as("replacement_in_working").DT_ID.eq(DT_MANIFEST.as("replacement_in_release").DT_ID),
                                DT_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(DT_MANIFEST.as("release").REPLACEMENT_DT_MANIFEST_ID, DT_MANIFEST.as("replacement_in_release").DT_MANIFEST_ID)
                .where(and(
                        DT_MANIFEST.as("working").REPLACEMENT_DT_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateBdtScDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(DT_SC_MANIFEST.join(DT_MANIFEST)
                .on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.NEXT_DT_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        dslContext.update(DT_SC_MANIFEST.join(DT_SC_MANIFEST.as("based"))
                        .on(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("based").NEXT_DT_SC_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.as("based").DT_SC_MANIFEST_ID)
                .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT_SC_MANIFEST.as("based").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        dslContext.update(DT_SC_MANIFEST
                .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                .join(DT_SC_MANIFEST.as("prev")).on(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID))
                .join(DT_MANIFEST).on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID)))
                .set(DT_SC_MANIFEST.DT_SC_ID, DT_SC_MANIFEST.as("prev").DT_SC_ID)
                .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext.update(DT_SC_MANIFEST.as("working")
                        .join(DT_SC_MANIFEST.as("release")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_ID.eq(DT_SC_MANIFEST.as("release").DT_SC_ID),
                                DT_SC_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                DT_SC_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(DT_SC_MANIFEST.as("replacement_in_working")).on(and(
                                DT_SC_MANIFEST.as("working").REPLACEMENT_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("replacement_in_working").DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(DT_SC_MANIFEST.as("replacement_in_release")).on(and(
                                DT_SC_MANIFEST.as("replacement_in_working").DT_SC_ID.eq(DT_SC_MANIFEST.as("replacement_in_release").DT_SC_ID),
                                DT_SC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(DT_SC_MANIFEST.as("release").REPLACEMENT_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.as("replacement_in_release").DT_SC_MANIFEST_ID)
                .where(and(
                        DT_SC_MANIFEST.as("working").REPLACEMENT_DT_SC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateXbtDependencies(BigInteger releaseId) {
        dslContext.update(XBT_MANIFEST
                .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                .join(XBT_MANIFEST.as("prev")).on(XBT_MANIFEST.PREV_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("prev").XBT_MANIFEST_ID)))
                .set(XBT_MANIFEST.XBT_ID, XBT_MANIFEST.as("prev").XBT_ID)
                .set(XBT_MANIFEST.LOG_ID, XBT_MANIFEST.as("prev").LOG_ID)
                .where(and(XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        XBT.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();
    }

    private void updateCodeListDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(CODE_LIST_MANIFEST.join(CODE_LIST_MANIFEST.as("based"))
                .on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based").NEXT_CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.as("based").CODE_LIST_MANIFEST_ID)
                .where(and(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST_MANIFEST.as("based").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        dslContext.update(CODE_LIST_MANIFEST
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(CODE_LIST_MANIFEST.as("prev")).on(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("prev").CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.CODE_LIST_ID, CODE_LIST_MANIFEST.as("prev").CODE_LIST_ID)
                .set(CODE_LIST_MANIFEST.LOG_ID, CODE_LIST_MANIFEST.as("prev").LOG_ID)
                .where(and(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext.update(CODE_LIST_MANIFEST.as("working")
                        .join(CODE_LIST_MANIFEST.as("release")).on(and(
                                CODE_LIST_MANIFEST.as("working").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("release").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                CODE_LIST_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(CODE_LIST_MANIFEST.as("replacement_in_working")).on(and(
                                CODE_LIST_MANIFEST.as("working").REPLACEMENT_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("replacement_in_working").CODE_LIST_MANIFEST_ID),
                                CODE_LIST_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(CODE_LIST_MANIFEST.as("replacement_in_release")).on(and(
                                CODE_LIST_MANIFEST.as("replacement_in_working").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("replacement_in_release").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(CODE_LIST_MANIFEST.as("release").REPLACEMENT_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.as("replacement_in_release").CODE_LIST_MANIFEST_ID)
                .where(and(
                        CODE_LIST_MANIFEST.as("working").REPLACEMENT_CODE_LIST_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateCodeListValueDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(CODE_LIST_VALUE_MANIFEST.join(CODE_LIST_MANIFEST)
                .on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .where(and(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        dslContext.update(CODE_LIST_VALUE_MANIFEST
                .join(CODE_LIST_VALUE).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.CODE_LIST_VALUE_ID))
                .join(CODE_LIST).on(CODE_LIST_VALUE.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(CODE_LIST_VALUE_MANIFEST.as("prev")).on(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("prev").CODE_LIST_VALUE_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID, CODE_LIST_VALUE_MANIFEST.as("prev").CODE_LIST_VALUE_ID)
                .where(and(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext.update(CODE_LIST_VALUE_MANIFEST.as("working")
                        .join(CODE_LIST_VALUE_MANIFEST.as("release")).on(and(
                                CODE_LIST_VALUE_MANIFEST.as("working").CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.as("release").CODE_LIST_VALUE_ID),
                                CODE_LIST_VALUE_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                CODE_LIST_VALUE_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(CODE_LIST_VALUE_MANIFEST.as("replacement_in_working")).on(and(
                                CODE_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("replacement_in_working").CODE_LIST_VALUE_MANIFEST_ID),
                                CODE_LIST_VALUE_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(CODE_LIST_VALUE_MANIFEST.as("replacement_in_release")).on(and(
                                CODE_LIST_VALUE_MANIFEST.as("replacement_in_working").CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.as("replacement_in_release").CODE_LIST_VALUE_ID),
                                CODE_LIST_VALUE_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(CODE_LIST_VALUE_MANIFEST.as("release").REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.as("replacement_in_release").CODE_LIST_VALUE_MANIFEST_ID)
                .where(and(
                        CODE_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAgencyIdListDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(AGENCY_ID_LIST_MANIFEST.join(AGENCY_ID_LIST_MANIFEST.as("based"))
                .on(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("based").NEXT_AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.as("based").AGENCY_ID_LIST_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST_MANIFEST.as("based").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        dslContext.update(AGENCY_ID_LIST_MANIFEST
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(AGENCY_ID_LIST_MANIFEST.as("prev")).on(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("prev").AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID, AGENCY_ID_LIST_MANIFEST.as("prev").AGENCY_ID_LIST_ID)
                .set(AGENCY_ID_LIST_MANIFEST.LOG_ID, AGENCY_ID_LIST_MANIFEST.as("prev").LOG_ID)
                .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext.update(AGENCY_ID_LIST_MANIFEST.as("working")
                        .join(AGENCY_ID_LIST_MANIFEST.as("release")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("working").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("release").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                AGENCY_ID_LIST_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(AGENCY_ID_LIST_MANIFEST.as("replacement_in_working")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_MANIFEST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(AGENCY_ID_LIST_MANIFEST.as("replacement_in_release")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(AGENCY_ID_LIST_MANIFEST.as("release").REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_MANIFEST_ID)
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAgencyIdListValueDependencies(BigInteger releaseId, BigInteger workingReleaseId) {
        dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST.join(AGENCY_ID_LIST_MANIFEST)
                .on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST
                .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_VALUE.OWNER_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev")).on(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID, AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_ID)
                .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // for update agency id list value manifest id
        dslContext.update(AGENCY_ID_LIST_MANIFEST.join(AGENCY_ID_LIST_VALUE_MANIFEST.as("value"))
                .on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("value").NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.as("value").AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST_VALUE_MANIFEST.as("value").RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // To update `code_list_manifest`.`agency_id_list_value_manifest_id`
        dslContext.update(CODE_LIST_MANIFEST
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev")).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID)))
                .set(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .execute();

        // Update replacement
        dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST.as("working")
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("release")).on(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("working").AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("release").AGENCY_ID_LIST_VALUE_ID),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId)),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        ))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working")).on(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_VALUE_MANIFEST_ID),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(ULong.valueOf(workingReleaseId))
                        ))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release")).on(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_VALUE_ID),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(ULong.valueOf(releaseId))
                        )))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("release").REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(and(
                        AGENCY_ID_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    public void unassignManifests(
            BigInteger releaseId,
            List<BigInteger> accManifestIds,
            List<BigInteger> asccpManifestIds,
            List<BigInteger> bccpManifestIds) {

        // ensure all manifests in given request are in 'Candidate' state.
        // check ACCs
        dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC.STATE)
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC_MANIFEST.ACC_MANIFEST_ID.in(accManifestIds)
                ))
                .fetchStream().forEach(e -> {
            CcState ccState = CcState.valueOf(e.value2());
            if (ccState != Candidate) {
                throw new IllegalArgumentException(e.value1() + " is an invalid manifest ID.");
            }
        });

        // check ASCCPs
        dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP.STATE)
                .from(ASCCP)
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(asccpManifestIds)
                ))
                .fetchStream().forEach(e -> {
            CcState ccState = CcState.valueOf(e.value2());
            if (ccState != Candidate) {
                throw new IllegalArgumentException(e.value1() + " is an invalid manifest ID.");
            }
        });

        // check BCCPs
        dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP.STATE)
                .from(BCCP)
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        BCCP_MANIFEST.BCCP_MANIFEST_ID.in(bccpManifestIds)
                ))
                .fetchStream().forEach(e -> {
            CcState ccState = CcState.valueOf(e.value2());
            if (ccState != Candidate) {
                throw new IllegalArgumentException(e.value1() + " is an invalid manifest ID.");
            }
        });

        deleteManifests(releaseId, accManifestIds, asccpManifestIds, bccpManifestIds);
    }

    private void deleteManifests(
            BigInteger releaseId,
            List<BigInteger> accManifestIds,
            List<BigInteger> asccpManifestIds,
            List<BigInteger> bccpManifestIds) {

        if (accManifestIds != null && !accManifestIds.isEmpty()) {
            dslContext.deleteFrom(ASCC_MANIFEST)
                    .where(and(
                            RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIds)
                    ))
                    .execute();

            dslContext.deleteFrom(BCC_MANIFEST)
                    .where(and(
                            RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(accManifestIds)
                    ))
                    .execute();
        }

        if (bccpManifestIds != null && !bccpManifestIds.isEmpty()) {
            dslContext.deleteFrom(BCCP_MANIFEST)
                    .where(and(
                            RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            BCCP_MANIFEST.BCCP_MANIFEST_ID.in(bccpManifestIds)
                    ))
                    .execute();
        }

        if (asccpManifestIds != null && !asccpManifestIds.isEmpty()) {
            dslContext.deleteFrom(ASCCP_MANIFEST)
                    .where(and(
                            RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(asccpManifestIds)
                    ))
                    .execute();
        }

        if (accManifestIds != null && !accManifestIds.isEmpty()) {
            dslContext.deleteFrom(ACC_MANIFEST)
                    .where(and(
                            RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                            ACC_MANIFEST.ACC_MANIFEST_ID.in(accManifestIds)
                    ))
                    .execute();
        }
    }

    public AssignComponents getAssignComponents(BigInteger releaseId) {
        ULong libraryId = dslContext.select(RELEASE.LIBRARY_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchOneInto(ULong.class);

        AssignComponents assignComponents = new AssignComponents();

        // ACCs
        Map<ULong, List<Record8<
                ULong, String, String, LocalDateTime, String,
                String, UInteger, UInteger>>> map =
                dslContext.select(
                        ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        ACC.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ACC.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                        .from(ACC_MANIFEST)
                        .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(APP_USER).on(ACC.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                        .join(LOG).on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                        .where(and(
                                or(
                                        RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                                        and(RELEASE.RELEASE_NUM.eq("Working"), RELEASE.LIBRARY_ID.eq(libraryId))
                                ),
                                ACC.STATE.notEqual(CcState.Published.name())
                        ))
                        .fetchStream()
                        .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            node.setManifestId(e.get(0).value1().toBigInteger());
            node.setDen(e.get(0).value2());
            node.setTimestamp(e.get(0).value4());
            node.setOwnerUserId(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.ACC);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableAccManifest(
                        node.getManifestId(), node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableAccManifest(
                        node.getManifestId(), node);
            }
        });

        // ASCCPs
        map = dslContext.select(
                ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                ASCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, ASCCP.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(ASCCP_MANIFEST)
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER).on(ASCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                                and(RELEASE.RELEASE_NUM.eq("Working"), RELEASE.LIBRARY_ID.eq(libraryId))
                        ),
                        ASCCP.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            node.setManifestId(e.get(0).value1().toBigInteger());
            node.setDen(e.get(0).value2());
            node.setTimestamp(e.get(0).value4());
            node.setOwnerUserId(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.ASCCP);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableAsccpManifest(
                        node.getManifestId(), node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableAsccpManifest(
                        node.getManifestId(), node);
            }
        });

        // BCCPs
        map = dslContext.select(
                BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                BCCP.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, BCCP.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(BCCP_MANIFEST)
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(APP_USER).on(BCCP.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                                and(RELEASE.RELEASE_NUM.eq("Working"), RELEASE.LIBRARY_ID.eq(libraryId))
                        ),
                        BCCP.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            node.setManifestId(e.get(0).value1().toBigInteger());
            node.setDen(e.get(0).value2());
            node.setTimestamp(e.get(0).value4());
            node.setOwnerUserId(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.BCCP);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableBccpManifest(
                        node.getManifestId(), node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableBccpManifest(
                        node.getManifestId(), node);
            }
        });

        // CODE_LISTs
        map = dslContext.select(
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME, RELEASE.RELEASE_NUM,
                CODE_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, CODE_LIST.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(CODE_LIST_MANIFEST)
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(APP_USER).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                                and(RELEASE.RELEASE_NUM.eq("Working"), RELEASE.LIBRARY_ID.eq(libraryId))
                        ),
                        CODE_LIST.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            node.setManifestId(e.get(0).value1().toBigInteger());
            node.setDen(e.get(0).value2());
            node.setTimestamp(e.get(0).value4());
            node.setOwnerUserId(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.CODE_LIST);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableCodeListManifest(
                        node.getManifestId(), node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableCodeListManifest(
                        node.getManifestId(), node);
            }
        });

        // AGENCY_ID_LISTs
        map = dslContext.select(
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME, RELEASE.RELEASE_NUM,
                AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, AGENCY_ID_LIST.STATE,
                LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(APP_USER).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                                and(RELEASE.RELEASE_NUM.eq("Working"), RELEASE.LIBRARY_ID.eq(libraryId))
                        ),
                        AGENCY_ID_LIST.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            node.setManifestId(e.get(0).value1().toBigInteger());
            node.setDen(e.get(0).value2());
            node.setTimestamp(e.get(0).value4());
            node.setOwnerUserId(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.AGENCY_ID_LIST);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableAgencyIdListManifest(
                        node.getManifestId(), node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableAgencyIdListManifest(
                        node.getManifestId(), node);
            }
        });

        // DTs
        map = dslContext.select(
                        DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN, RELEASE.RELEASE_NUM,
                        DT.LAST_UPDATE_TIMESTAMP, APP_USER.LOGIN_ID, DT.STATE,
                        LOG.REVISION_NUM, LOG.REVISION_TRACKING_NUM)
                .from(DT_MANIFEST)
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(APP_USER).on(DT.OWNER_USER_ID.eq(APP_USER.APP_USER_ID))
                .join(LOG).on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .where(and(
                        or(
                                RELEASE.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                                and(RELEASE.RELEASE_NUM.eq("Working"), RELEASE.LIBRARY_ID.eq(libraryId))
                        ),
                        DT.STATE.notEqual(CcState.Published.name())
                ))
                .fetchStream()
                .collect(groupingBy(e -> e.value1()));

        map.values().forEach(e -> {
            AssignableNode node = new AssignableNode();
            node.setManifestId(e.get(0).value1().toBigInteger());
            node.setDen(e.get(0).value2());
            node.setTimestamp(e.get(0).value4());
            node.setOwnerUserId(e.get(0).value5());
            node.setState(CcState.valueOf(e.get(0).value6()));
            node.setRevision(e.get(0).value7().toBigInteger());
            node.setType(CcType.DT);
            if (e.size() == 2) { // manifest are located at both sides.
                assignComponents.addUnassignableDtManifest(
                        node.getManifestId(), node);
            }
            // manifest is only located at 'Working' release side.
            else if (e.size() == 1 && "Working".equals(e.get(0).value3())) {
                assignComponents.addAssignableDtManifest(
                        node.getManifestId(), node);
            }
        });

        return assignComponents;
    }

    public void transitState(AuthenticatedPrincipal user,
                             TransitStateRequest request) {

        ReleaseRecord releaseRecord = dslContext.selectFrom(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())))
                .fetchOne();

        ReleaseState requestState = ReleaseState.valueOf(request.getState());
        CcState fromCcState = null;
        CcState toCcState = null;

        switch (ReleaseState.valueOf(releaseRecord.getState())) {
            case Initialized:
                if (requestState != Draft) {
                    throw new IllegalArgumentException("The release in '" + releaseRecord.getState() + "' state cannot transit to '" + requestState + "' state.");
                }

                requestState = Processing;
                fromCcState = Candidate;
                toCcState = ReleaseDraft;
                break;

            case Draft:
                if (requestState != Initialized && requestState != Published) {
                    throw new IllegalArgumentException("The release in '" + releaseRecord.getState() + "' state cannot transit to '" + requestState + "' state.");
                }

                if (requestState == Initialized) {
                    fromCcState = ReleaseDraft;
                    toCcState = Candidate;
                } else if (requestState == Published) {
                    requestState = Processing;
                    fromCcState = ReleaseDraft;
                    toCcState = CcState.Published;
                }

                break;

            case Processing:
            case Published:
                throw new IllegalArgumentException("The release in '" + releaseRecord.getState() + "' state cannot be transited.");
        }

        AppUser appUser = sessionService.getAppUserByUsername(user);
        ULong userId = ULong.valueOf(appUser.getAppUserId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (!appUser.isDeveloper()) {
            throw new IllegalArgumentException("It only allows to modify the release by the developer.");
        }

        releaseRecord.setState(requestState.name());
        releaseRecord.setLastUpdatedBy(userId);
        releaseRecord.setLastUpdateTimestamp(timestamp);
        releaseRecord.update(RELEASE.STATE, RELEASE.LAST_UPDATED_BY, RELEASE.LAST_UPDATE_TIMESTAMP);

        // update CCs' states by transited release state.
        ULong releaseId = releaseRecord.getReleaseId();
        if (fromCcState != null && toCcState != null) {
            if (toCcState == ReleaseDraft) {
                ReleaseValidationRequest validationRequest = request.getValidationRequest();
                for (BigInteger accManifestId : validationRequest.getAssignedAccComponentManifestIds()) {
                    ccNodeService.updateAccState(user, accManifestId, fromCcState, toCcState);
                }
                for (BigInteger asccpManifestId : validationRequest.getAssignedAsccpComponentManifestIds()) {
                    ccNodeService.updateAsccpState(user, asccpManifestId, fromCcState, toCcState);
                }
                for (BigInteger bccpManifestId : validationRequest.getAssignedBccpComponentManifestIds()) {
                    ccNodeService.updateBccpState(user, bccpManifestId, fromCcState, toCcState);
                }
                for (BigInteger codeListManifestId : validationRequest.getAssignedCodeListComponentManifestIds()) {
                    codeListService.updateCodeListState(user, timestamp, codeListManifestId, toCcState);
                }
                for (BigInteger agencyIdListManifestId : validationRequest.getAssignedAgencyIdListComponentManifestIds()) {
                    agencyIdService.updateAgencyIdListState(user, timestamp, agencyIdListManifestId, toCcState.name());
                }
                for (BigInteger dtManifestId : validationRequest.getAssignedDtComponentManifestIds()) {
                    ccNodeService.updateDtState(user, dtManifestId, fromCcState, toCcState);
                }
            } else if (toCcState == Candidate) {
                updateCCStates(user, releaseRecord, fromCcState, toCcState, timestamp);

                // Remove module set releases
                List<ULong> moduleSetReleases = dslContext.select(MODULE_SET_RELEASE.MODULE_SET_RELEASE_ID)
                        .from(MODULE_SET_RELEASE)
                        .where(MODULE_SET_RELEASE.RELEASE_ID.eq(releaseId)).fetchInto(ULong.class);

                if (moduleSetReleases.size() > 0) {
                    dslContext.deleteFrom(MODULE_ACC_MANIFEST).where(MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                    dslContext.deleteFrom(MODULE_ASCCP_MANIFEST).where(MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                    dslContext.deleteFrom(MODULE_BCCP_MANIFEST).where(MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                    dslContext.deleteFrom(MODULE_DT_MANIFEST).where(MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                    dslContext.deleteFrom(MODULE_CODE_LIST_MANIFEST).where(MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                    dslContext.deleteFrom(MODULE_AGENCY_ID_LIST_MANIFEST).where(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                    dslContext.deleteFrom(MODULE_XBT_MANIFEST).where(MODULE_XBT_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                    dslContext.deleteFrom(MODULE_BLOB_CONTENT_MANIFEST).where(MODULE_BLOB_CONTENT_MANIFEST.MODULE_SET_RELEASE_ID.in(moduleSetReleases)).execute();
                }

                // Remove tags.
                {
                    List<ULong> accManifestTagIdList = dslContext.select(ACC_MANIFEST_TAG.ACC_MANIFEST_ID)
                            .from(ACC_MANIFEST_TAG)
                            .join(ACC_MANIFEST).on(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                            .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId)).fetchInto(ULong.class);
                    if (!accManifestTagIdList.isEmpty()) {
                        dslContext.deleteFrom(ACC_MANIFEST_TAG)
                                .where(ACC_MANIFEST_TAG.ACC_MANIFEST_ID.in(accManifestTagIdList))
                                .execute();
                    }

                    List<ULong> asccpManifestTagIdList = dslContext.select(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID)
                            .from(ASCCP_MANIFEST_TAG)
                            .join(ASCCP_MANIFEST).on(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                            .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId)).fetchInto(ULong.class);
                    if (!asccpManifestTagIdList.isEmpty()) {
                        dslContext.deleteFrom(ASCCP_MANIFEST_TAG)
                                .where(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID.in(asccpManifestTagIdList))
                                .execute();
                    }

                    List<ULong> bccpManifestTagIdList = dslContext.select(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID)
                            .from(BCCP_MANIFEST_TAG)
                            .join(BCCP_MANIFEST).on(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.BCCP_MANIFEST_ID))
                            .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId)).fetchInto(ULong.class);
                    if (!bccpManifestTagIdList.isEmpty()) {
                        dslContext.deleteFrom(BCCP_MANIFEST_TAG)
                                .where(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID.in(bccpManifestTagIdList))
                                .execute();
                    }

                    List<ULong> dtManifestTagIdList = dslContext.select(DT_MANIFEST_TAG.DT_MANIFEST_ID)
                            .from(DT_MANIFEST_TAG)
                            .join(DT_MANIFEST).on(DT_MANIFEST_TAG.DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                            .where(DT_MANIFEST.RELEASE_ID.eq(releaseId)).fetchInto(ULong.class);
                    if (!dtManifestTagIdList.isEmpty()) {
                        dslContext.deleteFrom(DT_MANIFEST_TAG)
                                .where(DT_MANIFEST_TAG.DT_MANIFEST_ID.in(dtManifestTagIdList))
                                .execute();
                    }
                }

                // Remove replacement
                {
                    dslContext.update(ACC_MANIFEST)
                            .setNull(ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID)
                            .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(ASCC_MANIFEST)
                            .setNull(ASCC_MANIFEST.REPLACEMENT_ASCC_MANIFEST_ID)
                            .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(BCC_MANIFEST)
                            .setNull(BCC_MANIFEST.REPLACEMENT_BCC_MANIFEST_ID)
                            .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(ASCCP_MANIFEST)
                            .setNull(ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID)
                            .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(BCCP_MANIFEST)
                            .setNull(BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID)
                            .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(DT_MANIFEST)
                            .setNull(DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID)
                            .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(DT_SC_MANIFEST)
                            .setNull(DT_SC_MANIFEST.REPLACEMENT_DT_SC_MANIFEST_ID)
                            .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(CODE_LIST_MANIFEST)
                            .setNull(CODE_LIST_MANIFEST.REPLACEMENT_CODE_LIST_MANIFEST_ID)
                            .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(CODE_LIST_VALUE_MANIFEST)
                            .setNull(CODE_LIST_VALUE_MANIFEST.REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID)
                            .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(AGENCY_ID_LIST_MANIFEST)
                            .setNull(AGENCY_ID_LIST_MANIFEST.REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID)
                            .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                    dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST)
                            .setNull(AGENCY_ID_LIST_VALUE_MANIFEST.REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                            .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                            .execute();
                }

                dslContext.update(ASCC_MANIFEST).setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                        .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.update(BCC_MANIFEST).setNull(BCC_MANIFEST.SEQ_KEY_ID)
                        .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.update(SEQ_KEY.join(ASCC_MANIFEST).on(SEQ_KEY.ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID)))
                        .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                        .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                        .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.update(SEQ_KEY.join(BCC_MANIFEST).on(SEQ_KEY.BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID)))
                        .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                        .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                        .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(SEQ_KEY).where(SEQ_KEY.ASCC_MANIFEST_ID.in(
                        select(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                        .from(ASCC_MANIFEST)
                        .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))))
                        .execute();
                dslContext.deleteFrom(SEQ_KEY).where(SEQ_KEY.BCC_MANIFEST_ID.in(
                        select(BCC_MANIFEST.BCC_MANIFEST_ID)
                                .from(BCC_MANIFEST)
                                .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))))
                        .execute();
                dslContext.deleteFrom(BCC_MANIFEST)
                        .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(BCCP_MANIFEST)
                        .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(XBT_MANIFEST)
                        .where(XBT_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(BDT_SC_PRI_RESTRI).where(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.in(
                        select(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID)
                                .from(BDT_SC_PRI_RESTRI)
                                .join(DT_SC_MANIFEST).on(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.DT_SC_MANIFEST_ID))
                                .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId)))).execute();
                dslContext.update(DT_SC_MANIFEST)
                        .setNull(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID)
                        .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(DT_SC_MANIFEST)
                        .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(BDT_PRI_RESTRI).where(BDT_PRI_RESTRI.BDT_MANIFEST_ID.in(
                        select(BDT_PRI_RESTRI.BDT_MANIFEST_ID)
                                .from(BDT_PRI_RESTRI)
                                .join(DT_MANIFEST).on(BDT_PRI_RESTRI.BDT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId)))).execute();
                dslContext.update(DT_MANIFEST)
                        .setNull(DT_MANIFEST.BASED_DT_MANIFEST_ID)
                        .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(DT_MANIFEST)
                        .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(ASCC_MANIFEST)
                        .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(ASCCP_MANIFEST)
                        .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.update(ACC_MANIFEST)
                        .setNull(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                        .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(ACC_MANIFEST)
                        .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(CODE_LIST_VALUE_MANIFEST)
                        .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.update(CODE_LIST_MANIFEST)
                        .setNull(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID)
                        .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(CODE_LIST_MANIFEST)
                        .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.update(AGENCY_ID_LIST_MANIFEST)
                        .setNull(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .setNull(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID)
                        .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();
                dslContext.deleteFrom(AGENCY_ID_LIST_MANIFEST)
                        .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                        .execute();

            } else if (toCcState == CcState.Published) {
                updateCCStates(user, releaseRecord, fromCcState, toCcState, timestamp);
            }
        }
    }

    private void updateCCStates(AuthenticatedPrincipal user, ReleaseRecord releaseRecord,
                                CcState fromCcState, CcState toCcState, LocalDateTime timestamp) {
        for (BigInteger accManifestId : dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(RELEASE).on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        ACC.STATE.eq(fromCcState.name()),
                        RELEASE.RELEASE_NUM.eq("Working"),
                        RELEASE.LIBRARY_ID.eq(releaseRecord.getLibraryId())
                ))
                .fetchInto(BigInteger.class)) {
            ccNodeService.updateAccState(user, accManifestId, fromCcState, toCcState);
        }
        for (BigInteger asccpManifestId : dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        ASCCP.STATE.eq(fromCcState.name()),
                        RELEASE.RELEASE_NUM.eq("Working"),
                        RELEASE.LIBRARY_ID.eq(releaseRecord.getLibraryId())
                ))
                .fetchInto(BigInteger.class)) {
            ccNodeService.updateAsccpState(user, asccpManifestId, fromCcState, toCcState);
        }
        for (BigInteger bccpManifestId : dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .join(RELEASE).on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        BCCP.STATE.eq(fromCcState.name()),
                        RELEASE.RELEASE_NUM.eq("Working"),
                        RELEASE.LIBRARY_ID.eq(releaseRecord.getLibraryId())
                ))
                .fetchInto(BigInteger.class)) {
            ccNodeService.updateBccpState(user, bccpManifestId, fromCcState, toCcState);
        }
        for (BigInteger codeListManifestId : dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        CODE_LIST.STATE.eq(fromCcState.name()),
                        RELEASE.RELEASE_NUM.eq("Working"),
                        RELEASE.LIBRARY_ID.eq(releaseRecord.getLibraryId())
                ))
                .fetchInto(BigInteger.class)) {
            codeListService.updateCodeListState(user, timestamp, codeListManifestId, toCcState);
        }

        for (BigInteger agencyIdListManifestId : dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .join(RELEASE).on(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        AGENCY_ID_LIST.STATE.eq(fromCcState.name()),
                        RELEASE.RELEASE_NUM.eq("Working"),
                        RELEASE.LIBRARY_ID.eq(releaseRecord.getLibraryId())
                ))
                .fetchInto(BigInteger.class)) {
            agencyIdService.updateAgencyIdListState(user, timestamp, agencyIdListManifestId, toCcState.toString());
        }

        for (BigInteger dtManifestId : dslContext.select(DT_MANIFEST.DT_MANIFEST_ID)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .join(RELEASE).on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        DT.STATE.eq(fromCcState.name()),
                        RELEASE.RELEASE_NUM.eq("Working"),
                        RELEASE.LIBRARY_ID.eq(releaseRecord.getLibraryId())
                ))
                .fetchInto(BigInteger.class)) {
            ccNodeService.updateDtState(user, dtManifestId, fromCcState, toCcState);
        }
    }

    public boolean isThereAnyDraftRelease(BigInteger releaseId) {
        return dslContext.selectCount()
                .from(RELEASE)
                .where(and(
                        RELEASE.STATE.in(Draft.name(), Processing.name()),
                        RELEASE.RELEASE_ID.ne(ULong.valueOf(releaseId))
                ))
                .fetchOptionalInto(Integer.class).orElse(0) > 0;
    }

    public boolean isLatestRelease(BigInteger releaseId) {
        return dslContext.resultQuery("SELECT max(`release_id`) FROM `release` WHERE `release_num` != 'Working'")
                .fetchOneInto(BigInteger.class).equals(releaseId);
    }

    public void cleanUp(ScoreUser requester, BigInteger releaseId) {
        ULong requesterId = ULong.valueOf(requester.getUserId());

        // ACCs
        dslContext.update(ACC_MANIFEST
                .join(ACC_MANIFEST.as("prev"))
                .on(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.as("prev").NEXT_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(ACC_MANIFEST
                .join(ACC_MANIFEST.as("next"))
                .on(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("next").ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.as("next").PREV_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(ACC
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID)))
                .set(ACC.OWNER_USER_ID, requesterId)
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ACC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // ASCCs
        dslContext.update(ASCC_MANIFEST
                .join(ASCC_MANIFEST.as("prev"))
                .on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID)))
                .set(ASCC_MANIFEST.as("prev").NEXT_ASCC_MANIFEST_ID, ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(ASCC_MANIFEST
                .join(ASCC_MANIFEST.as("next"))
                .on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("next").ASCC_MANIFEST_ID)))
                .set(ASCC_MANIFEST.as("next").PREV_ASCC_MANIFEST_ID, ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(ASCC
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID)))
                .set(ASCC.OWNER_USER_ID, requesterId)
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ASCC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // BCCs
        dslContext.update(BCC_MANIFEST
                .join(BCC_MANIFEST.as("prev"))
                .on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID)))
                .set(BCC_MANIFEST.as("prev").NEXT_BCC_MANIFEST_ID, BCC_MANIFEST.BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(BCC_MANIFEST
                .join(BCC_MANIFEST.as("next"))
                .on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("next").BCC_MANIFEST_ID)))
                .set(BCC_MANIFEST.as("next").PREV_BCC_MANIFEST_ID, BCC_MANIFEST.BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(BCC
                .join(BCC_MANIFEST)
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID)))
                .set(BCC.OWNER_USER_ID, requesterId)
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        BCC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // ASCCPs
        dslContext.update(ASCCP_MANIFEST
                .join(ASCCP_MANIFEST.as("prev"))
                .on(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.as("prev").NEXT_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(ASCCP_MANIFEST
                .join(ASCCP_MANIFEST.as("next"))
                .on(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.as("next").PREV_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(ASCCP
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID)))
                .set(ASCCP.OWNER_USER_ID, requesterId)
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        ASCCP.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // BCCPs
        dslContext.update(BCCP_MANIFEST
                .join(BCCP_MANIFEST.as("prev"))
                .on(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID)))
                .set(BCCP_MANIFEST.as("prev").NEXT_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(BCCP_MANIFEST
                .join(BCCP_MANIFEST.as("next"))
                .on(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID)))
                .set(BCCP_MANIFEST.as("next").PREV_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(BCCP
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID)))
                .set(BCCP.OWNER_USER_ID, requesterId)
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        BCCP.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // CODE_LISTs
        dslContext.update(CODE_LIST_MANIFEST
                .join(CODE_LIST_MANIFEST.as("prev"))
                .on(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("prev").CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.as("prev").NEXT_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(CODE_LIST_MANIFEST
                .join(CODE_LIST_MANIFEST.as("next"))
                .on(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("next").CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.as("next").PREV_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(CODE_LIST
                .join(CODE_LIST_MANIFEST)
                .on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID)))
                .set(CODE_LIST.OWNER_USER_ID, requesterId)
                .where(and(
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // CODE_LIST_VALUEs
        dslContext.update(CODE_LIST_VALUE_MANIFEST
                .join(CODE_LIST_VALUE_MANIFEST.as("prev"))
                .on(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("prev").CODE_LIST_VALUE_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.as("prev").NEXT_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(CODE_LIST_VALUE_MANIFEST
                .join(CODE_LIST_VALUE_MANIFEST.as("next"))
                .on(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("next").CODE_LIST_VALUE_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.as("next").PREV_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(CODE_LIST_VALUE
                .join(CODE_LIST_VALUE_MANIFEST)
                .on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID)))
                .set(CODE_LIST_VALUE.OWNER_USER_ID, requesterId)
                .where(and(
                        CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        CODE_LIST_VALUE.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // DTs
        dslContext.update(DT_MANIFEST
                .join(DT_MANIFEST.as("prev"))
                .on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID)))
                .set(DT_MANIFEST.as("prev").NEXT_DT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(DT_MANIFEST
                .join(DT_MANIFEST.as("next"))
                .on(DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("next").DT_MANIFEST_ID)))
                .set(DT_MANIFEST.as("next").PREV_DT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(DT
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID)))
                .set(DT.OWNER_USER_ID, requesterId)
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        //DT_SCs
        dslContext.update(DT_SC_MANIFEST
                .join(DT_SC_MANIFEST.as("prev"))
                .on(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.as("prev").NEXT_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(DT_SC_MANIFEST
                .join(DT_SC_MANIFEST.as("next"))
                .on(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("next").DT_SC_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.as("next").PREV_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(DT_SC
                .join(DT_SC_MANIFEST)
                .on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID)))
                .set(DT_SC.OWNER_USER_ID, requesterId)
                .where(and(
                        DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        DT_SC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        //XBTs
        dslContext.update(XBT_MANIFEST
                .join(XBT_MANIFEST.as("prev"))
                .on(XBT_MANIFEST.PREV_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("prev").XBT_MANIFEST_ID)))
                .set(XBT_MANIFEST.as("prev").NEXT_XBT_MANIFEST_ID, XBT_MANIFEST.XBT_MANIFEST_ID)
                .where(XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(XBT_MANIFEST
                .join(XBT_MANIFEST.as("next"))
                .on(XBT_MANIFEST.NEXT_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("next").XBT_MANIFEST_ID)))
                .set(XBT_MANIFEST.as("next").PREV_XBT_MANIFEST_ID, XBT_MANIFEST.XBT_MANIFEST_ID)
                .where(XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(XBT
                .join(XBT_MANIFEST)
                .on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID)))
                .set(XBT.OWNER_USER_ID, requesterId)
                .where(and(
                        XBT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        XBT.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // AGENCY_ID_LIST
        dslContext.update(AGENCY_ID_LIST_MANIFEST
                .join(AGENCY_ID_LIST_MANIFEST.as("prev"))
                .on(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("prev").AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.as("prev").NEXT_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(AGENCY_ID_LIST_MANIFEST
                .join(AGENCY_ID_LIST_MANIFEST.as("next"))
                .on(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("next").AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.as("next").PREV_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(AGENCY_ID_LIST
                .join(AGENCY_ID_LIST_MANIFEST)
                .on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID)))
                .set(AGENCY_ID_LIST.OWNER_USER_ID, requesterId)
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // AGENCY_ID_LIST_VALUE
        dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST
                .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev"))
                .on(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(AGENCY_ID_LIST_VALUE_MANIFEST
                .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("next"))
                .on(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("next").AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("next").PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .execute();
        dslContext.update(AGENCY_ID_LIST_VALUE
                .join(AGENCY_ID_LIST_VALUE_MANIFEST)
                .on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID)))
                .set(AGENCY_ID_LIST_VALUE.OWNER_USER_ID, requesterId)
                .where(and(
                        AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)),
                        AGENCY_ID_LIST_VALUE.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();
    }
}
