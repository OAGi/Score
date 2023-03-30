package org.oagi.score.repo.component.abie;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AbieRecord;
import org.oagi.score.service.common.data.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.helper.Utility.emptyToNull;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ABIE;

@Repository
public class AbieWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AbieReadRepository readRepository;

    public AbieNode.Abie upsertAbie(UpsertAbieRequest request) {
        AbieNode.Abie abie = request.getAbie();
        ULong topLevelAsbiepId = ULong.valueOf(request.getTopLevelAsbiepId());
        String hashPath = abie.getHashPath();
        AbieRecord abieRecord = dslContext.selectFrom(ABIE)
                .where(and(
                        ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        ABIE.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong requesterId = ULong.valueOf(user.getAppUserId());

        if (abieRecord == null) {
            abieRecord = new AbieRecord();
            abieRecord.setGuid(ScoreGuid.randomGuid());
            abieRecord.setBasedAccManifestId(ULong.valueOf(abie.getBasedAccManifestId()));
            abieRecord.setPath(abie.getPath());
            abieRecord.setHashPath(hashPath);

            abieRecord.setDefinition(abie.getDefinition());
            abieRecord.setRemark(abie.getRemark());
            abieRecord.setBizTerm(abie.getBizTerm());

            abieRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            abieRecord.setCreatedBy(requesterId);
            abieRecord.setLastUpdatedBy(requesterId);
            abieRecord.setCreationTimestamp(request.getLocalDateTime());
            abieRecord.setLastUpdateTimestamp(request.getLocalDateTime());

            abieRecord.setAbieId(
                    dslContext.insertInto(ABIE)
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
                abieRecord.setLastUpdateTimestamp(request.getLocalDateTime());
                abieRecord.update(
                        ABIE.DEFINITION,
                        ABIE.REMARK,
                        ABIE.BIZ_TERM,
                        ABIE.LAST_UPDATED_BY,
                        ABIE.LAST_UPDATE_TIMESTAMP
                );
            }
        }

        return readRepository.getAbie(request.getTopLevelAsbiepId(), hashPath);
    }

}
