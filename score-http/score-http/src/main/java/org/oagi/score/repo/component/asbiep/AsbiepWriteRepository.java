package org.oagi.score.repo.component.asbiep;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.AsbiepRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.gateway.http.helper.Utility.emptyToNull;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ABIE;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.ASBIEP;

@Repository
public class AsbiepWriteRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private AsbiepReadRepository readRepository;

    public AsbiepNode.Asbiep upsertAsbiep(UpsertAsbiepRequest request) {
        AsbiepNode.Asbiep asbiep = request.getAsbiep();
        ULong topLevelAsbiepId = ULong.valueOf(request.getTopLevelAsbiepId());
        ULong refTopLevelAsbiepId = (request.getRefTopLevelAsbiepId() != null) ? ULong.valueOf(request.getRefTopLevelAsbiepId()) : null;
        String hashPath = asbiep.getHashPath();
        AsbiepRecord asbiepRecord = dslContext.selectFrom(ASBIEP)
                .where(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        ASBIEP.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        AppUser user = sessionService.getAppUserByUsername(request.getUser());
        ULong requesterId = ULong.valueOf(user.getAppUserId());

        if (asbiepRecord == null) {
            asbiepRecord = new AsbiepRecord();
            asbiepRecord.setGuid(ScoreGuid.randomGuid());
            asbiepRecord.setBasedAsccpManifestId(ULong.valueOf(asbiep.getBasedAsccpManifestId()));
            asbiepRecord.setPath(asbiep.getPath());
            asbiepRecord.setHashPath(hashPath);
            if (request.getRoleOfAbieId() != null) {
                asbiepRecord.setRoleOfAbieId(ULong.valueOf(request.getRoleOfAbieId()));
            } else {
                asbiepRecord.setRoleOfAbieId(dslContext.select(ABIE.ABIE_ID)
                        .from(ABIE)
                        .where(and(
                                ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq((refTopLevelAsbiepId != null) ? refTopLevelAsbiepId : topLevelAsbiepId),
                                ABIE.HASH_PATH.eq(asbiep.getRoleOfAbieHashPath())
                        ))
                        .fetchOneInto(ULong.class));
            }

            asbiepRecord.setDefinition(asbiep.getDefinition());
            asbiepRecord.setRemark(asbiep.getRemark());
            asbiepRecord.setBizTerm(asbiep.getBizTerm());

            asbiepRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            asbiepRecord.setCreatedBy(requesterId);
            asbiepRecord.setLastUpdatedBy(requesterId);
            asbiepRecord.setCreationTimestamp(request.getLocalDateTime());
            asbiepRecord.setLastUpdateTimestamp(request.getLocalDateTime());

            asbiepRecord.setAsbiepId(
                    dslContext.insertInto(ASBIEP)
                            .set(asbiepRecord)
                            .returning(ASBIEP.ASBIEP_ID)
                            .fetchOne().getAsbiepId()
            );
        } else {
            if (asbiep.getDefinition() != null) {
                asbiepRecord.setDefinition(emptyToNull(asbiep.getDefinition()));
            }

            if (asbiep.getRemark() != null) {
                asbiepRecord.setRemark(emptyToNull(asbiep.getRemark()));
            }

            if (asbiep.getBizTerm() != null) {
                asbiepRecord.setBizTerm(emptyToNull(asbiep.getBizTerm()));
            }

            if (asbiepRecord.changed()) {
                asbiepRecord.setLastUpdatedBy(requesterId);
                asbiepRecord.setLastUpdateTimestamp(request.getLocalDateTime());
                asbiepRecord.update(ASBIEP.DEFINITION,
                        ASBIEP.REMARK,
                        ASBIEP.BIZ_TERM,
                        ASBIEP.LAST_UPDATED_BY,
                        ASBIEP.LAST_UPDATE_TIMESTAMP);
            }

        }

        return readRepository.getAsbiep(request.getTopLevelAsbiepId(), hashPath);
    }
    
}
