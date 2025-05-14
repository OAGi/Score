package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.UpdateSetMoreStep;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpdateTopLevelAsbiepRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.repository.TopLevelAsbiepCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertBizCtxAssignmentArguments;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertTopLevelAsbiepArguments;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.TopLevelAsbiepRecord;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BIZ_CTX_ASSIGNMENT;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.TOP_LEVEL_ASBIEP;
import static org.oagi.score.gateway.http.common.util.Utility.emptyToNull;

public class JooqTopLevelAsbiepCommandRepository extends JooqBaseRepository implements TopLevelAsbiepCommandRepository {

    public JooqTopLevelAsbiepCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public TopLevelAsbiepId insertTopLevelAsbiep(InsertTopLevelAsbiepArguments arguments) {
        TopLevelAsbiepRecord record = new TopLevelAsbiepRecord();
        record.setOwnerUserId(arguments.getUserId());
        record.setReleaseId(arguments.getReleaseId());
        record.setState(arguments.getBieState().name());
        record.setVersion(arguments.getVersion());
        record.setStatus(arguments.getStatus());
        record.setInverseMode((byte) (arguments.isInverseMode() ? 1 : 0));
        record.setLastUpdatedBy(arguments.getUserId());
        record.setLastUpdateTimestamp(arguments.getTimestamp());

        if (arguments.getBasedTopLevelAsbiepId() != null) {
            record.setBasedTopLevelAsbiepId(arguments.getBasedTopLevelAsbiepId());
        }

        if (arguments.getSourceTopLevelAsbiepId() != null) {
            record.setSourceTopLevelAsbiepId(arguments.getSourceTopLevelAsbiepId());
            record.setSourceAction(arguments.getSourceAction());
            record.setSourceTimestamp(arguments.getSourceTimestamp());
        }

        return new TopLevelAsbiepId(
                dslContext().insertInto(TOP_LEVEL_ASBIEP)
                        .set(record)
                        .returningResult(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                        .fetchOne().value1().toBigInteger()
        );
    }

    @Override
    public void insertBizCtxAssignments(InsertBizCtxAssignmentArguments arguments) {
        dslContext().batch(
                arguments.getBizCtxIds().stream().map(bizCtxId -> dslContext().insertInto(BIZ_CTX_ASSIGNMENT)
                        .set(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                        .set(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID, bizCtxId)).collect(Collectors.toList())
        ).execute();
    }

    @Override
    public void updateTopLevelAsbiep(UpdateTopLevelAsbiepRequest request) {

        ULong requesterId = valueOf(requester().userId());

        TopLevelAsbiepRecord record = dslContext().selectFrom(TOP_LEVEL_ASBIEP)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(request.getTopLevelAsbiepId())))
                .fetchOptional().orElse(null);

        if (record == null) {
            throw new IllegalArgumentException("Unknown Top Level BIE.");
        }

        if (!requesterId.equals(record.getOwnerUserId())) {
            throw new IllegalArgumentException("Only the owner can modify it.");
        }

        if (request.getStatus() != null) {
            record.setStatus(emptyToNull(request.getStatus()));
        }

        if (request.getVersion() != null) {
            record.setVersion(emptyToNull(request.getVersion()));
        }

        UpdateSetMoreStep moreStep = dslContext().update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.STATUS, record.getStatus())
                .set(TOP_LEVEL_ASBIEP.VERSION, record.getVersion());
        if (request.getInverseMode() != null) {
            moreStep = moreStep.set(TOP_LEVEL_ASBIEP.INVERSE_MODE, (byte) (request.getInverseMode() ? 1 : 0));
        }

        moreStep.set(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP, LocalDateTime.now())
                .set(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY, requesterId)
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(record.getTopLevelAsbiepId()))
                .execute();
    }

    @Override
    public void updateAsbiepId(AsbiepId asbiepId, TopLevelAsbiepId topLevelAsbiepId) {
        dslContext().update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.ASBIEP_ID, valueOf(asbiepId))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)))
                .execute();
    }

}
