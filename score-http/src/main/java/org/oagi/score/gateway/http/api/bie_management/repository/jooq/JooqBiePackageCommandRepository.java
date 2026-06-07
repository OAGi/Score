package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import jakarta.annotation.Nullable;
import org.jooq.DSLContext;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageId;
import org.oagi.score.gateway.http.api.bie_management.repository.BiePackageCommandRepository;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.common.model.Guid;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BiePackageRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BiePackageTopLevelAsbiepRecord;
import org.springframework.dao.EmptyResultDataAccessException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BIE_PACKAGE;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BIE_PACKAGE_TOP_LEVEL_ASBIEP;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

public class JooqBiePackageCommandRepository extends JooqBaseRepository implements BiePackageCommandRepository {

    public JooqBiePackageCommandRepository(DSLContext dslContext, ScoreUser requester,
                                           RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BiePackageId create(LibraryId libraryId, String name, String versionId, String versionName, String description) {

        BiePackageRecord biePackageRecord = new BiePackageRecord();

        biePackageRecord.setGuid(Guid.create().value());
        biePackageRecord.setLibraryId(valueOf(libraryId));
        biePackageRecord.setName(name);
        biePackageRecord.setVersionId(versionId);
        biePackageRecord.setVersionName(versionName);
        biePackageRecord.setDescription(description);
        biePackageRecord.setState(BieState.WIP.name());
        biePackageRecord.setOwnerUserId(valueOf(requester().userId()));
        biePackageRecord.setCreatedBy(valueOf(requester().userId()));
        biePackageRecord.setLastUpdatedBy(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        biePackageRecord.setCreationTimestamp(timestamp);
        biePackageRecord.setLastUpdateTimestamp(timestamp);

        return new BiePackageId(
                dslContext().insertInto(BIE_PACKAGE)
                        .set(biePackageRecord)
                        .returning(BIE_PACKAGE.BIE_PACKAGE_ID)
                        .fetchOne().getBiePackageId().toBigInteger()
        );
    }

    @Override
    public BiePackageId revise(BiePackageId biePackageId, String versionId) {

        var query = repositoryFactory().biePackageQueryRepository(requester());

        BiePackageDetailsRecord biePackageDetails = query.getBiePackageDetails(biePackageId);
        if (biePackageDetails == null) {
            throw new EmptyResultDataAccessException(1);
        }

        BiePackageRecord biePackageRecord = new BiePackageRecord();

        biePackageRecord.setGuid(Guid.create().value());
        biePackageRecord.setLibraryId(valueOf(biePackageDetails.libraryId()));
        biePackageRecord.setName(biePackageDetails.name());
        biePackageRecord.setVersionId(versionId);
        biePackageRecord.setVersionName(biePackageDetails.versionName());
        biePackageRecord.setDescription(biePackageDetails.description());
        biePackageRecord.setState(BieState.WIP.name());
        biePackageRecord.setPrevBiePackageId(valueOf(biePackageId));
        biePackageRecord.setOwnerUserId(valueOf(requester().userId()));
        biePackageRecord.setCreatedBy(valueOf(requester().userId()));
        biePackageRecord.setLastUpdatedBy(valueOf(requester().userId()));
        LocalDateTime timestamp = LocalDateTime.now();
        biePackageRecord.setCreationTimestamp(timestamp);
        biePackageRecord.setLastUpdateTimestamp(timestamp);

        BiePackageId revisedBiePackageId = new BiePackageId(
                dslContext().insertInto(BIE_PACKAGE)
                        .set(biePackageRecord)
                        .returning(BIE_PACKAGE.BIE_PACKAGE_ID)
                        .fetchOne().getBiePackageId().toBigInteger()
        );

        addBieToBiePackage(revisedBiePackageId,
                query.getTopLevelAsbiepIdListInBiePackage(biePackageId));

        return revisedBiePackageId;
    }

    @Override
    public boolean update(BiePackageId biePackageId,
                          String name, String versionId, String versionName, String description) {

        UpdateSetFirstStep<BiePackageRecord> firstStep = dslContext().update(BIE_PACKAGE);
        UpdateSetMoreStep<BiePackageRecord> step;
        if (hasLength(name)) {
            step = firstStep.set(BIE_PACKAGE.NAME, name);
        } else {
            step = firstStep.setNull(BIE_PACKAGE.NAME);
        }
        if (hasLength(versionId)) {
            step = step.set(BIE_PACKAGE.VERSION_ID, versionId);
        } else {
            step = step.setNull(BIE_PACKAGE.VERSION_ID);
        }
        if (hasLength(versionName)) {
            step = step.set(BIE_PACKAGE.VERSION_NAME, versionName);
        } else {
            step = step.setNull(BIE_PACKAGE.VERSION_NAME);
        }
        if (hasLength(description)) {
            step = step.set(BIE_PACKAGE.DESCRIPTION, description);
        } else {
            step = step.setNull(BIE_PACKAGE.DESCRIPTION);
        }

        int numOfUpdatedRecords = step.set(BIE_PACKAGE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateState(BiePackageId biePackageId, BieState state) {

        int numOfUpdatedRecords = dslContext().update(BIE_PACKAGE)
                .set(BIE_PACKAGE.STATE, state.name())
                .set(BIE_PACKAGE.LAST_UPDATED_BY, valueOf(requester().userId()))
                .set(BIE_PACKAGE.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public boolean updateOwnerUserId(BiePackageId biePackageId, UserId userId) {

        int numOfUpdatedRecords = dslContext().update(BIE_PACKAGE)
                .set(BIE_PACKAGE.OWNER_USER_ID, valueOf(userId))
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                .execute();
        return numOfUpdatedRecords == 1;
    }

    @Override
    public BiePackageId copy(BiePackageId biePackageId) {
        BiePackageRecord biePackageRecord = dslContext().selectFrom(BIE_PACKAGE)
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.eq(valueOf(biePackageId)))
                .fetchOne();

        List<BiePackageTopLevelAsbiepRecord> biePackageTopLevelAsbiepRecords = dslContext().selectFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(biePackageRecord.getBiePackageId()))
                .fetch();

        BiePackageRecord copiedBiePackageRecord = biePackageRecord.copy();
        copiedBiePackageRecord.setBiePackageId(null);
        copiedBiePackageRecord.setGuid(Guid.create().value());
        copiedBiePackageRecord.setState(BieState.WIP.name());
        ULong requesterUserId = valueOf(requester().userId());
        copiedBiePackageRecord.setOwnerUserId(requesterUserId);
        copiedBiePackageRecord.setCreatedBy(requesterUserId);
        copiedBiePackageRecord.setLastUpdatedBy(requesterUserId);
        LocalDateTime now = LocalDateTime.now();
        copiedBiePackageRecord.setCreationTimestamp(now);
        copiedBiePackageRecord.setLastUpdateTimestamp(now);
        copiedBiePackageRecord.setSourceBiePackageId(biePackageRecord.getBiePackageId());
        copiedBiePackageRecord.setSourceAction("Copy");
        copiedBiePackageRecord.setSourceTimestamp(now);
        BiePackageId copiedBiePackageId = new BiePackageId(
                dslContext().insertInto(BIE_PACKAGE)
                        .set(copiedBiePackageRecord)
                        .returning(BIE_PACKAGE.BIE_PACKAGE_ID)
                        .fetchOne().getBiePackageId().toBigInteger());

        for (BiePackageTopLevelAsbiepRecord biePackageTopLevelAsbiepRecord : biePackageTopLevelAsbiepRecords) {
            BiePackageTopLevelAsbiepRecord copiedBiePackageTopLevelAsbiepRecord = biePackageTopLevelAsbiepRecord.copy();
            copiedBiePackageTopLevelAsbiepRecord.setBiePackageTopLevelAsbiepId(null);
            copiedBiePackageTopLevelAsbiepRecord.setBiePackageId(valueOf(copiedBiePackageId));
            copiedBiePackageTopLevelAsbiepRecord.setBiePackageTopLevelAsbiepId(
                    dslContext().insertInto(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                            .set(copiedBiePackageTopLevelAsbiepRecord)
                            .returning(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_TOP_LEVEL_ASBIEP_ID)
                            .fetchOne().getBiePackageTopLevelAsbiepId());
        }

        return copiedBiePackageId;
    }

    @Override
    public int delete(Collection<BiePackageId> biePackageIdList) {

        if (biePackageIdList == null || biePackageIdList.isEmpty()) {
            return 0;
        }

        // source_bie_package_id is only copy/uplift provenance ("where this was copied from"); breaking it does not
        // harm the referencing package. So detach it from EVERY package pointing at a discarded one — inside or
        // outside this set — letting surviving copies keep working with a null source.
        dslContext().update(BIE_PACKAGE)
                .setNull(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID)
                .setNull(BIE_PACKAGE.SOURCE_ACTION)
                .setNull(BIE_PACKAGE.SOURCE_TIMESTAMP)
                .where(BIE_PACKAGE.SOURCE_BIE_PACKAGE_ID.in(valueOf(biePackageIdList)))
                .execute();

        // prev_bie_package_id is the revision-history dependency: the service rejects the request when a package
        // OUTSIDE this set references one of these as its previous revision, so at this point only rows that are all
        // about to be deleted can still carry the link. Null it among them so the single multi-row delete does not
        // trip the row-by-row FK enforcement.
        dslContext().update(BIE_PACKAGE)
                .setNull(BIE_PACKAGE.PREV_BIE_PACKAGE_ID)
                .where(BIE_PACKAGE.PREV_BIE_PACKAGE_ID.in(valueOf(biePackageIdList)))
                .execute();

        dslContext().deleteFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.in(valueOf(biePackageIdList)))
                .execute();

        return dslContext().deleteFrom(BIE_PACKAGE)
                .where(BIE_PACKAGE.BIE_PACKAGE_ID.in(valueOf(biePackageIdList)))
                .execute();
    }

    @Override
    public void deleteAssignedTopLevelAsbiepIdList(Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }
        dslContext().deleteFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList)))
                .execute();
    }

    @Override
    public void addBieToBiePackage(BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        for (TopLevelAsbiepId topLevelAsbiepId : topLevelAsbiepIdList) {
            addBiePackageTopLevelAsbiep(biePackageId, topLevelAsbiepId, null);
        }
    }

    private void addBiePackageTopLevelAsbiep(BiePackageId biePackageId,
                                             TopLevelAsbiepId topLevelAsbiepId,
                                             @Nullable TopLevelAsbiepId prevTopLevelAsbiepId) {
        BiePackageTopLevelAsbiepRecord record = new BiePackageTopLevelAsbiepRecord();
        record.setBiePackageId(valueOf(biePackageId));
        record.setTopLevelAsbiepId(valueOf(topLevelAsbiepId));
        if (prevTopLevelAsbiepId != null) {
            record.setPrevTopLevelAsbiepId(valueOf(prevTopLevelAsbiepId));
        }
        record.setCreatedBy(valueOf(requester().userId()));
        record.setCreationTimestamp(LocalDateTime.now());

        var q = dslContext().insertInto(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .set(record)
                .onDuplicateKeyUpdate()
                .set(BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID, valueOf(biePackageId))
                .set(BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID, valueOf(topLevelAsbiepId));
        if (prevTopLevelAsbiepId != null) {
            q = q.set(BIE_PACKAGE_TOP_LEVEL_ASBIEP.PREV_TOP_LEVEL_ASBIEP_ID, valueOf(prevTopLevelAsbiepId));
        }
        q.execute();
    }

    @Override
    public void replaceBieInBiePackage(BiePackageId biePackageId, TopLevelAsbiepId prevTopLevelAsbiepId, TopLevelAsbiepId topLevelAsbiepId) {
        if (prevTopLevelAsbiepId == null || topLevelAsbiepId == null) {
            return;
        }

        addBiePackageTopLevelAsbiep(biePackageId, topLevelAsbiepId, prevTopLevelAsbiepId);
    }

    @Override
    public void deleteBieInBiePackage(BiePackageId biePackageId, Collection<TopLevelAsbiepId> topLevelAsbiepIdList) {
        if (topLevelAsbiepIdList == null || topLevelAsbiepIdList.isEmpty()) {
            return;
        }

        dslContext().deleteFrom(BIE_PACKAGE_TOP_LEVEL_ASBIEP)
                .where(and(
                        BIE_PACKAGE_TOP_LEVEL_ASBIEP.BIE_PACKAGE_ID.eq(valueOf(biePackageId)),
                        BIE_PACKAGE_TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(valueOf(topLevelAsbiepIdList))
                ))
                .execute();
    }

}
