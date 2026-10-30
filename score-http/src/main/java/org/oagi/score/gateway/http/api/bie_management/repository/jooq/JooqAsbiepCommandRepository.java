package org.oagi.score.gateway.http.api.bie_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpsertAsbiepRequest;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepId;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepNode;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSupportDocId;
import org.oagi.score.gateway.http.api.bie_management.repository.AsbiepCommandRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.criteria.InsertAsbiepArguments;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AsbiepRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.AsbiepSupportDocRecord;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.ScoreDigestUtils.sha256;
import static org.oagi.score.gateway.http.common.util.Utility.emptyToNull;
import static org.springframework.util.StringUtils.hasLength;

public class JooqAsbiepCommandRepository extends JooqBaseRepository implements AsbiepCommandRepository {

    public JooqAsbiepCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public AsbiepId insertAsbiep(InsertAsbiepArguments arguments) {
        return new AsbiepId(dslContext().insertInto(ASBIEP)
                .set(ASBIEP.GUID, ScoreGuidUtils.randomGuid())
                .set(ASBIEP.BASED_ASCCP_MANIFEST_ID, arguments.getAsccpManifestId())
                .set(ASBIEP.PATH, arguments.getPath())
                .set(ASBIEP.HASH_PATH, sha256(arguments.getPath()))
                .set(ASBIEP.ROLE_OF_ABIE_ID, arguments.getRoleOfAbieId())
                .set(ASBIEP.CREATED_BY, arguments.getUserId())
                .set(ASBIEP.LAST_UPDATED_BY, arguments.getUserId())
                .set(ASBIEP.CREATION_TIMESTAMP, arguments.getTimestamp())
                .set(ASBIEP.LAST_UPDATE_TIMESTAMP, arguments.getTimestamp())
                .set(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID, arguments.getTopLevelAsbiepId())
                .returningResult(ASBIEP.ASBIEP_ID)
                .fetchOne().value1().toBigInteger());
    }

    @Override
    public AsbiepNode.Asbiep upsertAsbiep(UpsertAsbiepRequest request) {
        AsbiepNode.Asbiep asbiep = request.getAsbiep();
        ULong topLevelAsbiepId = valueOf(request.getTopLevelAsbiepId());
        ULong refTopLevelAsbiepId = (request.getRefTopLevelAsbiepId() != null) ? valueOf(request.getRefTopLevelAsbiepId()) : null;
        String hashPath = asbiep.getHashPath();
        AsbiepRecord asbiepRecord = dslContext().selectFrom(ASBIEP)
                .where(and(
                        ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                        ASBIEP.HASH_PATH.eq(hashPath)
                ))
                .fetchOptional().orElse(null);

        ScoreUser requester = requester();
        ULong requesterId = valueOf(requester.userId());
        LocalDateTime timestamp = LocalDateTime.now();

        if (asbiepRecord == null) {
            asbiepRecord = new AsbiepRecord();
            asbiepRecord.setGuid(ScoreGuidUtils.randomGuid());
            asbiepRecord.setBasedAsccpManifestId(valueOf(asbiep.getBasedAsccpManifestId()));
            asbiepRecord.setPath(asbiep.getPath());
            asbiepRecord.setHashPath(hashPath);
            if (request.getRoleOfAbieId() != null) {
                asbiepRecord.setRoleOfAbieId(valueOf(request.getRoleOfAbieId()));
            } else {
                asbiepRecord.setRoleOfAbieId(dslContext().select(ABIE.ABIE_ID)
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
            asbiepRecord.setDisplayName(asbiep.getDisplayName());

            asbiepRecord.setOwnerTopLevelAsbiepId(topLevelAsbiepId);

            asbiepRecord.setCreatedBy(requesterId);
            asbiepRecord.setLastUpdatedBy(requesterId);
            asbiepRecord.setCreationTimestamp(timestamp);
            asbiepRecord.setLastUpdateTimestamp(timestamp);

            asbiepRecord.setAsbiepId(
                    dslContext().insertInto(ASBIEP)
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

            if (asbiep.getDisplayName() != null) {
                asbiepRecord.setDisplayName(emptyToNull(asbiep.getDisplayName()));
            }

            if (asbiepRecord.changed()) {
                asbiepRecord.setLastUpdatedBy(requesterId);
                asbiepRecord.setLastUpdateTimestamp(timestamp);
                asbiepRecord.update(ASBIEP.DEFINITION,
                        ASBIEP.REMARK,
                        ASBIEP.BIZ_TERM,
                        ASBIEP.DISPLAY_NAME,
                        ASBIEP.LAST_UPDATED_BY,
                        ASBIEP.LAST_UPDATE_TIMESTAMP);
            }
        }

        applyAsbiepSupportDocListChanges(new AsbiepId(asbiepRecord.getAsbiepId().toBigInteger()), asbiep.getSupportDocList());

        return getAsbiep(request.getTopLevelAsbiepId(), hashPath);
    }

    private AsbiepNode.Asbiep getAsbiep(TopLevelAsbiepId topLevelAsbiepId, String hashPath) {
        AsbiepNode.Asbiep asbiep = new AsbiepNode.Asbiep();
        asbiep.setUsed(true);
        asbiep.setHashPath(hashPath);

        var query = repositoryFactory().asbiepQueryRepository(requester());
        AsbiepDetailsRecord asbiepDetails = query.getAsbiepDetails(topLevelAsbiepId, hashPath);
        if (asbiepDetails != null) {
            asbiep.setOwnerTopLevelAsbiepId(asbiepDetails.ownerTopLevelAsbiep().topLevelAsbiepId());
            asbiep.setAsbiepId(asbiepDetails.asbiepId());
            asbiep.setRoleOfAbieId(asbiepDetails.roleOfAbieId());
            if (asbiepDetails.roleOfAbieId() != null) {
                asbiep.setRoleOfAbieHashPath(dslContext().select(ABIE.HASH_PATH)
                        .from(ABIE)
                        .where(ABIE.ABIE_ID.eq(valueOf(asbiepDetails.roleOfAbieId())))
                        .fetchOneInto(String.class));
            } else {
                String path = asbiepDetails.path() + '>' + "ACC-" + asbiepDetails.basedAsccp().roleOfAccManifestId();
                asbiep.setRoleOfAbieHashPath(sha256(path));
            }
            asbiep.setBasedAsccpManifestId(asbiepDetails.basedAsccp().asccpManifestId());
            asbiep.setDerived(false); // TODO
            asbiep.setGuid(asbiepDetails.guid().value());
            asbiep.setRemark(asbiepDetails.remark());
            asbiep.setBizTerm(asbiepDetails.bizTerm());
            asbiep.setDefinition(asbiepDetails.definition());
            asbiep.setDisplayName(asbiepDetails.displayName());
            asbiep.setSupportDocList(repositoryFactory().asbiepQueryRepository(requester())
                    .getAsbiepSupportingDocumentationList(asbiepDetails.asbiepId()));
        }

        return asbiep;
    }

    void applyAsbiepSupportDocListChanges(
            AsbiepId asbiepId, List<org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSupportDocRecord> supportDocList) {

        List<AsbiepSupportDocId> existingSupportDocIdList = dslContext().select(ASBIEP_SUPPORT_DOC.ASBIEP_SUPPORT_DOC_ID)
                .from(ASBIEP_SUPPORT_DOC)
                .where(ASBIEP_SUPPORT_DOC.ASBIEP_ID.eq(valueOf(asbiepId)))
                .fetchStreamInto(BigInteger.class).map(e -> new AsbiepSupportDocId(e)).collect(Collectors.toList());

        // A support document is updated only through the ASBIEP that owns it.
        for (org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSupportDocRecord supportDoc : supportDocList) {
            if (supportDoc.asbiepSupportDocId() != null && !existingSupportDocIdList.contains(supportDoc.asbiepSupportDocId())) {
                throw new IllegalArgumentException("The support document '" + supportDoc.asbiepSupportDocId() +
                        "' does not belong to the ASBIEP '" + asbiepId + "'.");
            }
        }

        Set<AsbiepSupportDocId> currentIds = supportDocList.stream()
                .map(org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSupportDocRecord::asbiepSupportDocId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (AsbiepSupportDocId existingSupportDocId : existingSupportDocIdList) {
            if (!currentIds.contains(existingSupportDocId)) {
                deleteAsbiepSupportDoc(existingSupportDocId);
            }
        }

        for (org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSupportDocRecord supportDoc : supportDocList) {
            if (supportDoc.asbiepSupportDocId() == null) {
                if (hasLength(supportDoc.content()) || hasLength(supportDoc.description())) {
                    insertAsbiepSupportDoc(asbiepId, supportDoc.content(), supportDoc.description());
                }
            } else {
                updateAsbiepSupportDoc(supportDoc.asbiepSupportDocId(), supportDoc.content(), supportDoc.description());
            }
        }
    }

    @Override
    public AsbiepSupportDocId insertAsbiepSupportDoc(AsbiepId asbiepId, String content, String description) {

        return new AsbiepSupportDocId(
                dslContext().insertInto(ASBIEP_SUPPORT_DOC)
                        .set(ASBIEP_SUPPORT_DOC.ASBIEP_ID, valueOf(asbiepId))
                        .set(ASBIEP_SUPPORT_DOC.CONTENT, content)
                        .set(ASBIEP_SUPPORT_DOC.DESCRIPTION, description)
                        .returningResult(ASBIEP_SUPPORT_DOC.ASBIEP_SUPPORT_DOC_ID)
                        .fetchOne().value1().toBigInteger());
    }

    @Override
    public boolean updateAsbiepSupportDoc(AsbiepSupportDocId asbiepSupportDocId, String content, String description) {

        AsbiepSupportDocRecord asbiepSupportDocRecord = dslContext().selectFrom(ASBIEP_SUPPORT_DOC)
                .where(ASBIEP_SUPPORT_DOC.ASBIEP_SUPPORT_DOC_ID.eq(valueOf(asbiepSupportDocId)))
                .fetchOptional().orElse(null);
        if (asbiepSupportDocRecord == null) {
            return false;
        }

        if (content != null) {
            asbiepSupportDocRecord.setContent(emptyToNull(content));
        }
        if (description != null) {
            asbiepSupportDocRecord.setDescription(emptyToNull(description));
        }

        if (asbiepSupportDocRecord.changed()) {
            return asbiepSupportDocRecord.update(
                    ASBIEP_SUPPORT_DOC.CONTENT,
                    ASBIEP_SUPPORT_DOC.DESCRIPTION) == 1;
        }

        return false;
    }

    @Override
    public boolean deleteAsbiepSupportDoc(AsbiepSupportDocId asbiepSupportDocId) {

        return dslContext().deleteFrom(ASBIEP_SUPPORT_DOC)
                .where(ASBIEP_SUPPORT_DOC.ASBIEP_SUPPORT_DOC_ID.eq(valueOf(asbiepSupportDocId)))
                .execute() == 1;
    }

    @Override
    public void copyAsbiepSupportingDocumentation(AsbiepId previousAsbiepId, AsbiepId nextAsbiepId) {
        dslContext().insertInto(ASBIEP_SUPPORT_DOC,
                        ASBIEP_SUPPORT_DOC.ASBIEP_ID,
                        ASBIEP_SUPPORT_DOC.CONTENT,
                        ASBIEP_SUPPORT_DOC.DESCRIPTION)
                .select(
                        dslContext().select(
                                        inline(valueOf(nextAsbiepId)).as(ASBIEP_SUPPORT_DOC.ASBIEP_ID),
                                        ASBIEP_SUPPORT_DOC.CONTENT,
                                        ASBIEP_SUPPORT_DOC.DESCRIPTION)
                                .from(ASBIEP_SUPPORT_DOC)
                                .where(ASBIEP_SUPPORT_DOC.ASBIEP_ID.eq(valueOf(previousAsbiepId)))
                )
                .execute();
    }

}
