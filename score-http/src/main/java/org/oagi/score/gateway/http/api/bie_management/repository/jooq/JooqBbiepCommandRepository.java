package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertBbiepRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepNode;
import org.oagi.score.gateway.http.api.bie_management.repository.BbiepCommandRepository;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.BbiepRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.BBIEP;
import static org.oagi.score.gateway.http.common.util.Utility.emptyToNull;

public class JooqBbiepCommandRepository extends JooqBaseRepository implements BbiepCommandRepository {

    public JooqBbiepCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public BbiepNode.Bbiep upsertBbiep(UpsertBbiepRequest request) {
        BbiepNode.Bbiep bbiep = request.getBbiep();
        ULong topLevelAsbiepId = valueOf(request.getTopLevelAsbiepId());
        String hashPath = bbiep.getHashPath();
        BbiepRecord bbiepRecord = dslContext().selectFrom(BBIEP)
                .where(and(
                        BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        BBIEP.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        ScoreUser requester = requester();
        ULong requesterId = valueOf(requester.userId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (bbiepRecord == null) {
            bbiepRecord = new BbiepRecord();
            bbiepRecord.setGuid(ScoreGuidUtils.randomGuid());
            bbiepRecord.setBasedBccpManifestId(valueOf(bbiep.getBasedBccpManifestId()));
            bbiepRecord.setPath(bbiep.getPath());
            bbiepRecord.setHashPath(hashPath);

            bbiepRecord.setDefinition(bbiep.getDefinition());
            bbiepRecord.setRemark(bbiep.getRemark());
            bbiepRecord.setBizTerm(bbiep.getBizTerm());
            bbiepRecord.setDisplayName(bbiep.getDisplayName());

            bbiepRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            bbiepRecord.setCreatedBy(requesterId);
            bbiepRecord.setLastUpdatedBy(requesterId);
            bbiepRecord.setCreationTimestamp(timestamp);
            bbiepRecord.setLastUpdateTimestamp(timestamp);

            bbiepRecord.setBbiepId(
                    dslContext().insertInto(BBIEP)
                            .set(bbiepRecord)
                            .returning(BBIEP.BBIEP_ID)
                            .fetchOne().getBbiepId()
            );
        } else {
            if (bbiep.getDefinition() != null) {
                bbiepRecord.setDefinition(emptyToNull(bbiep.getDefinition()));
            }

            if (bbiep.getRemark() != null) {
                bbiepRecord.setRemark(emptyToNull(bbiep.getRemark()));
            }

            if (bbiep.getBizTerm() != null) {
                bbiepRecord.setBizTerm(emptyToNull(bbiep.getBizTerm()));
            }

            if (bbiep.getDisplayName() != null) {
                bbiepRecord.setDisplayName(emptyToNull(bbiep.getDisplayName()));
            }

            if (bbiepRecord.changed()) {
                bbiepRecord.setLastUpdatedBy(requesterId);
                bbiepRecord.setLastUpdateTimestamp(timestamp);

                bbiepRecord.update(
                        BBIEP.DEFINITION,
                        BBIEP.REMARK,
                        BBIEP.BIZ_TERM,
                        BBIEP.DISPLAY_NAME,
                        BBIEP.LAST_UPDATED_BY,
                        BBIEP.LAST_UPDATE_TIMESTAMP
                );
            }
        }

        return getBbiep(request.getTopLevelAsbiepId(), hashPath);
    }

    private BbiepNode.Bbiep getBbiep(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        BbiepNode.Bbiep bbiep = new BbiepNode.Bbiep();
        bbiep.setUsed(true);
        bbiep.setHashPath(hashPath);

        var query = repositoryFactory().bbiepQueryRepository(requester());
        BbiepDetailsRecord bbiepDetails = query.getBbiepDetails(topLevelAsbiepId, hashPath);
        if (bbiepDetails != null) {
            bbiep.setOwnerTopLevelAsbiepId(bbiepDetails.ownerTopLevelAsbiep().topLevelAsbiepId());
            bbiep.setBbiepId(bbiepDetails.bbiepId());
            bbiep.setBasedBccpManifestId(bbiepDetails.basedBccp().bccpManifestId());
            bbiep.setGuid(bbiepDetails.guid().value());
            bbiep.setRemark(bbiepDetails.remark());
            bbiep.setBizTerm(bbiepDetails.bizTerm());
            bbiep.setDefinition(bbiepDetails.definition());
            bbiep.setDisplayName(bbiepDetails.displayName());
        }

        return bbiep;
    }
}
