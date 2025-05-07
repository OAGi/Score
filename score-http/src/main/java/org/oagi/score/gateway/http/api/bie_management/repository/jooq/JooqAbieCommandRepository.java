package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertAbieRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieId;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieNode;
import org.oagi.score.gateway.http.api.bie_management.repository.AbieCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertAbieArguments;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AbieRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.ABIE;
import static org.oagi.score.gateway.http.common.util.ScoreDigestUtils.sha256;
import static org.oagi.score.gateway.http.common.util.Utility.emptyToNull;

public class JooqAbieCommandRepository extends JooqBaseRepository implements AbieCommandRepository {

    public JooqAbieCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AbieId insertAbie(InsertAbieArguments arguments) {
        return new AbieId(dslContext().insertInto(ABIE)
                .set(ABIE.GUID, ScoreGuidUtils.randomGuid())
                .set(ABIE.BASED_ACC_MANIFEST_ID, arguments.getAccManifestId())
                .set(ABIE.PATH, arguments.getPath())
                .set(ABIE.HASH_PATH, sha256(arguments.getPath()))
                .set(ABIE.CREATED_BY, arguments.getUserId())
                .set(ABIE.LAST_UPDATED_BY, arguments.getUserId())
                .set(ABIE.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(ABIE.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                .returningResult(ABIE.ABIE_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public AbieNode.Abie upsertAbie(UpsertAbieRequest request) {
        AbieNode.Abie abie = request.getAbie();
        ULong topLevelAsbiepId = valueOf(request.getTopLevelAsbiepId());
        String hashPath = abie.getHashPath();
        AbieRecord abieRecord = dslContext().selectFrom(ABIE)
                .where(and(
                        ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        ABIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        ScoreUser requester = requester();
        ULong requesterId = valueOf(requester.userId());

        LocalDateTime timestamp = LocalDateTime.now();
        if (abieRecord == null) {
            abieRecord = new AbieRecord();
            abieRecord.setGuid(ScoreGuidUtils.randomGuid());
            abieRecord.setBasedAccManifestId(valueOf(abie.getBasedAccManifestId()));
            abieRecord.setPath(abie.getPath());
            abieRecord.setHashPath(hashPath);

            abieRecord.setDefinition(abie.getDefinition());
            abieRecord.setRemark(abie.getRemark());
            abieRecord.setBizTerm(abie.getBizTerm());

            abieRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            abieRecord.setCreatedBy(requesterId);
            abieRecord.setLastUpdatedBy(requesterId);
            abieRecord.setCreationTimestamp(timestamp);
            abieRecord.setLastUpdateTimestamp(timestamp);

            abieRecord.setAbieId(
                    dslContext().insertInto(ABIE)
                            .set(abieRecord)
                            .returning(ABIE.ABIE_ID)
                            .fetchOne().getAbieId()
            );
        } else {
            if (abie.getDefinition() != null) {
                abieRecord.setDefinition(emptyToNull(abie.getDefinition()));
            }

            if (abie.getRemark() != null) {
                abieRecord.setRemark(emptyToNull(abie.getRemark()));
            }

            if (abie.getBizTerm() != null) {
                abieRecord.setBizTerm(emptyToNull(abie.getBizTerm()));
            }

            if (abieRecord.changed()) {
                abieRecord.setLastUpdatedBy(requesterId);
                abieRecord.setLastUpdateTimestamp(timestamp);
                abieRecord.update(
                        ABIE.DEFINITION,
                        ABIE.REMARK,
                        ABIE.BIZ_TERM,
                        ABIE.LAST_UPDATED_BY,
                        ABIE.LAST_UPDATE_TIMESTAMP
                );
            }
        }

        return getAbie(request.getTopLevelAsbiepId(), hashPath);
    }

    private AbieNode.Abie getAbie(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {

        AbieNode.Abie abie = new AbieNode.Abie();
        abie.setUsed(true);
        abie.setHashPath(hashPath);

        var query = repositoryFactory().abieQueryRepository(requester());
        AbieDetailsRecord abieDetails = query.getAbieDetails(topLevelAsbiepId, hashPath);
        if (abieDetails != null) {
            abie.setOwnerTopLevelAsbiepId(abieDetails.ownerTopLevelAsbiep().topLevelAsbiepId());
            abie.setAbieId(abieDetails.abieId().value());
            abie.setGuid(abieDetails.guid().toString());
            abie.setBasedAccManifestId(abieDetails.basedAcc().accManifestId());
            abie.setRemark(abieDetails.remark());
            abie.setBizTerm(abieDetails.bizTerm());
            abie.setDefinition(abieDetails.definition());
        }

        return abie;
    }

}
