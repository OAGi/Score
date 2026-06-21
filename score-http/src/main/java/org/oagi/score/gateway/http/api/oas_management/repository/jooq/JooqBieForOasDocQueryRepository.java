package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.SQLDataType;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagResponse;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocRequest;
import org.oagi.score.gateway.http.api.oas_management.controller.payload.GetBieForOasDocResponse;
import org.oagi.score.gateway.http.api.oas_management.model.*;
import org.oagi.score.gateway.http.api.oas_management.repository.BieForOasDocQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessControl;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.SortDirection;
import org.oagi.score.gateway.http.common.model.base.ScoreDataAccessException;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasResourceTagRecord;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.OasTagRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.castNull;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.oagi.score.gateway.http.api.oas_management.controller.payload.GetAssignedOasTagResponse.EMPTY_INSTANCE;
import static org.oagi.score.gateway.http.common.model.ScoreRole.DEVELOPER;
import static org.oagi.score.gateway.http.common.model.ScoreRole.END_USER;
import static org.oagi.score.gateway.http.common.model.SortDirection.ASC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqBieForOasDocQueryRepository extends JooqBaseRepository
        implements BieForOasDocQueryRepository {

    public JooqBieForOasDocQueryRepository(DSLContext dslContext,
                                           ScoreUser requester,
                                           RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    private SelectOnConditionStep selectForRequest() {
        return dslContext().select(concat(fields(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.RELEASE_ID,
                        ASBIEP.ASBIEP_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP.ASCCP_ID,
                        ASBIEP.GUID,
                        ASCCP_MANIFEST.DEN,
                        RELEASE.RELEASE_NUM,
                        ASBIEP.REMARK,
                        inline("Request").as("oas_doc_message_body_type"),
                        // Issue #1730: typed NULL placeholder so the unionAll column set stays aligned with the
                        // response select (requests carry no HTTP status code). Renders as a NULL literal.
                        castNull(SQLDataType.INTEGER).as("http_status_code"),
                        OAS_DOC.as("oas_doc").OAS_DOC_ID.as("oas_doc_id"),
                        OAS_OPERATION.as("oas_operation").VERB.as("verb"),
                        OAS_TAG.as("oas_tag").NAME.as("tag_name"),
                        OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("array_indicator"),
                        OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("suppress_root_indicator"),
                        OAS_RESOURCE.as("oas_resource").PATH.as("resource_name"),
                        OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID.as("oas_resource_id"),
                        OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id"),
                        OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.as("oas_operation_id"),
                        OAS_OPERATION.as("oas_operation").SECURITY_OVERRIDDEN.as("security_overridden"),
                        OAS_MESSAGE_BODY.CREATION_TIMESTAMP,
                        OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP
                ), ownerFields(), creatorFields(), updaterFields()))
                .from(OAS_MESSAGE_BODY)
                .leftJoin(OAS_REQUEST).on(OAS_REQUEST.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("oas_operation")).on(OAS_REQUEST.OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("oas_resource")).on(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.eq(OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("oas_doc")).on(OAS_RESOURCE.as("oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_RESOURCE_TAG.as("oas_resource_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_TAG.as("oas_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_TAG_ID.eq(OAS_TAG.as("oas_tag").OAS_TAG_ID))
                // Issue #1730: left-join so BIE-less (bodyless) operations are still returned.
                .leftJoin(TOP_LEVEL_ASBIEP).on(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .leftJoin(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .leftJoin(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .leftJoin(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                .join(creatorTable()).on(creatorTablePk().eq(OAS_MESSAGE_BODY.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(OAS_MESSAGE_BODY.LAST_UPDATED_BY));
    }

    private SelectOnConditionStep selectForResponse() {
        return dslContext().select(concat(fields(
                        TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                        TOP_LEVEL_ASBIEP.STATE,
                        TOP_LEVEL_ASBIEP.VERSION,
                        TOP_LEVEL_ASBIEP.RELEASE_ID,
                        ASBIEP.ASBIEP_ID,
                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                        ASCCP.ASCCP_ID,
                        ASBIEP.GUID,
                        ASCCP_MANIFEST.DEN,
                        RELEASE.RELEASE_NUM,
                        ASBIEP.REMARK,
                        inline("Response").as("oas_doc_message_body_type"),
                        // Issue #1730: HTTP status code drives bodyless (202/204) response generation.
                        OAS_RESPONSE.HTTP_STATUS_CODE.as("http_status_code"),
                        OAS_DOC.as("oas_doc").OAS_DOC_ID.as("oas_doc_id"),
                        OAS_OPERATION.as("oas_operation").VERB.as("verb"),
                        OAS_TAG.as("oas_tag").NAME.as("tag_name"),
                        OAS_RESPONSE.MAKE_ARRAY_INDICATOR.as("array_indicator"),
                        OAS_RESPONSE.SUPPRESS_ROOT_INDICATOR.as("suppress_root_indicator"),
                        OAS_RESOURCE.as("oas_resource").PATH.as("resource_name"),
                        OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID.as("oas_resource_id"),
                        OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id"),
                        OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.as("oas_operation_id"),
                        OAS_OPERATION.as("oas_operation").SECURITY_OVERRIDDEN.as("security_overridden"),
                        OAS_MESSAGE_BODY.CREATION_TIMESTAMP,
                        OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP
                ), ownerFields(), creatorFields(), updaterFields()))
                .from(OAS_MESSAGE_BODY)
                .leftJoin(OAS_RESPONSE).on(OAS_RESPONSE.OAS_MESSAGE_BODY_ID.eq(OAS_MESSAGE_BODY.OAS_MESSAGE_BODY_ID))
                .leftJoin(OAS_OPERATION.as("oas_operation")).on(OAS_RESPONSE.OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_RESOURCE.as("oas_resource")).on(OAS_OPERATION.as("oas_operation").OAS_RESOURCE_ID.eq(OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID))
                .leftJoin(OAS_DOC.as("oas_doc")).on(OAS_RESOURCE.as("oas_resource").OAS_DOC_ID.eq(OAS_DOC.as("oas_doc").OAS_DOC_ID))
                .leftJoin(OAS_RESOURCE_TAG.as("oas_resource_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_OPERATION_ID.eq(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID))
                .leftJoin(OAS_TAG.as("oas_tag")).on(OAS_RESOURCE_TAG.as("oas_resource_tag").OAS_TAG_ID.eq(OAS_TAG.as("oas_tag").OAS_TAG_ID))
                // Issue #1730: left-join so BIE-less (bodyless) operations are still returned.
                .leftJoin(TOP_LEVEL_ASBIEP).on(OAS_MESSAGE_BODY.TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                .leftJoin(RELEASE).on(RELEASE.RELEASE_ID.eq(TOP_LEVEL_ASBIEP.RELEASE_ID))
                .leftJoin(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .leftJoin(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .leftJoin(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .leftJoin(ownerTable()).on(ownerTablePk().eq(TOP_LEVEL_ASBIEP.OWNER_USER_ID))
                .join(creatorTable()).on(creatorTablePk().eq(OAS_MESSAGE_BODY.CREATED_BY))
                .join(updaterTable()).on(updaterTablePk().eq(OAS_MESSAGE_BODY.LAST_UPDATED_BY));
    }

    private RecordMapper<Record, BieForOasDoc> mapper() {
        return record -> {
            BieForOasDoc bieForOasDoc = new BieForOasDoc();
            // Issue #1730: TOP_LEVEL_ASBIEP is left-joined; bodyless operations have no BIE.
            ULong topLevelAsbiepId = record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID);
            if (topLevelAsbiepId != null) {
                bieForOasDoc.setTopLevelAsbiepId(new TopLevelAsbiepId(topLevelAsbiepId.toBigInteger()));
                bieForOasDoc.setState(BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)));
                bieForOasDoc.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
                bieForOasDoc.setDen(record.get(ASCCP_MANIFEST.DEN));
                bieForOasDoc.setReleaseNum(record.get(RELEASE.RELEASE_NUM));
                bieForOasDoc.setRemark(record.get(ASBIEP.REMARK));
                bieForOasDoc.setGuid(record.get(ASCCP.GUID));
            } else {
                // Issue #1730: a bodyless operation has no BIE state; treat it as editable (WIP)
                // so downstream access checks (which switch on state) do not NPE.
                bieForOasDoc.setState(BieState.WIP);
            }
            bieForOasDoc.setMessageBody(record.get(field("oas_doc_message_body_type", String.class)));
            ULong oasDocId = record.get(OAS_DOC.as("oas_doc").OAS_DOC_ID.as("oas_doc_id"));
            if (oasDocId != null) {
                bieForOasDoc.setOasDocId(new OasDocId(oasDocId.toBigInteger()));
            }
            bieForOasDoc.setVerb(record.get(OAS_OPERATION.as("oas_operation").VERB.as("verb")));
            Byte arrayIndicator = record.get(OAS_REQUEST.MAKE_ARRAY_INDICATOR.as("array_indicator"));
            if (arrayIndicator != null) {
                bieForOasDoc.setArrayIndicator(arrayIndicator == (byte) 1);
            }
            Byte suppressRootIndicator = record.get(OAS_REQUEST.SUPPRESS_ROOT_INDICATOR.as("suppress_root_indicator"));
            if (suppressRootIndicator != null) {
                bieForOasDoc.setSuppressRootIndicator(suppressRootIndicator == (byte) 1);
            }
            bieForOasDoc.setResourceName(record.get(OAS_RESOURCE.as("oas_resource").PATH.as("resource_name")));
            bieForOasDoc.setOperationId(record.get(OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id")));
            bieForOasDoc.setTagName(record.get(OAS_TAG.as("oas_tag").NAME.as("tag_name")));
            ULong oasResourceId = record.get(OAS_RESOURCE.as("oas_resource").OAS_RESOURCE_ID.as("oas_resource_id"));
            if (oasResourceId != null) {
                bieForOasDoc.setOasResourceId(new OasResourceId(oasResourceId.toBigInteger()));
            }
            ULong oasOperationId = record.get(OAS_OPERATION.as("oas_operation").OAS_OPERATION_ID.as("oas_operation_id"));
            if (oasOperationId != null) {
                bieForOasDoc.setOasOperationId(new OasOperationId(oasOperationId.toBigInteger()));
                List<OasSecurityRequirement> securityRequirements = loadOperationSecurityRequirements(oasOperationId);
                Byte securityOverridden = record.get(field("security_overridden", Byte.class));
                bieForOasDoc.setSecurityOverridden(securityOverridden != null && securityOverridden == (byte) 1);
                bieForOasDoc.setSecurityRequirements(securityRequirements);
            }
            if (topLevelAsbiepId != null) {
                bieForOasDoc.setReleaseId(new ReleaseId(record.get(TOP_LEVEL_ASBIEP.RELEASE_ID).toBigInteger()));
                bieForOasDoc.setOwner(fetchOwnerSummary(record));
            } else {
                // Issue #1730: bodyless operation has no BIE owner; fall back to the creator so that
                // access evaluation (getOwner().userId()) works and the creator can edit/remove it.
                bieForOasDoc.setOwner(fetchCreatorSummary(record));
            }
            // Issue #1730: surface the persisted HTTP status code for bodyless responses.
            Integer httpStatusCode = record.get(field("http_status_code", Integer.class));
            if (httpStatusCode != null) {
                bieForOasDoc.setHttpStatusCode(httpStatusCode);
            }
            bieForOasDoc.setCreatedBy(fetchCreatorSummary(record));
            bieForOasDoc.setLastUpdatedBy(fetchUpdaterSummary(record));
            bieForOasDoc.setCreationTimestamp(toDate(record.get(OAS_MESSAGE_BODY.CREATION_TIMESTAMP)));
            bieForOasDoc.setLastUpdateTimestamp(toDate(record.get(OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP)));
            return bieForOasDoc;
        };
    }

    private List<OasSecurityRequirement> loadOperationSecurityRequirements(ULong oasOperationId) {
        Map<Integer, OasSecurityRequirement> requirements = new LinkedHashMap<>();
        // Issue #1729: oas_operation_security references the scheme by oas_security_scheme_id; join to
        // resolve the components.securitySchemes key (a NULL id is an anonymous {} requirement).
        dslContext().select(
                        OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID,
                        OAS_OPERATION_SECURITY.REQUIREMENT_GROUP,
                        OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID,
                        OAS_SECURITY_SCHEME.SCHEME_NAME)
                .from(OAS_OPERATION_SECURITY)
                .leftJoin(OAS_SECURITY_SCHEME)
                .on(OAS_SECURITY_SCHEME.OAS_SECURITY_SCHEME_ID.eq(OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID))
                .where(OAS_OPERATION_SECURITY.OAS_OPERATION_ID.eq(oasOperationId))
                .orderBy(OAS_OPERATION_SECURITY.REQUIREMENT_GROUP.asc(), OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID.asc())
                .fetch()
                .forEach(record -> {
                    Integer group = record.get(OAS_OPERATION_SECURITY.REQUIREMENT_GROUP);
                    OasSecurityRequirement requirement = requirements.computeIfAbsent(group, k -> {
                        OasSecurityRequirement r = new OasSecurityRequirement();
                        r.setSchemes(new ArrayList<>());
                        return r;
                    });
                    String schemeName = record.get(OAS_SECURITY_SCHEME.SCHEME_NAME);
                    if (record.get(OAS_OPERATION_SECURITY.OAS_SECURITY_SCHEME_ID) == null
                            || schemeName == null || schemeName.isBlank()) {
                        requirement.setAnonymous(true);
                    } else if (!requirement.isAnonymous()) {
                        OasSecurityRequirementScheme scheme = new OasSecurityRequirementScheme();
                        scheme.setSchemeName(schemeName);
                        scheme.setScopes(loadOperationSecurityScopes(record.get(OAS_OPERATION_SECURITY.OAS_OPERATION_SECURITY_ID)));
                        requirement.getSchemes().add(scheme);
                    }
                });
        return new ArrayList<>(requirements.values());
    }

    private List<String> loadOperationSecurityScopes(ULong oasOperationSecurityId) {
        return dslContext().select(OAS_OPERATION_SECURITY_SCOPE.SCOPE_NAME)
                .from(OAS_OPERATION_SECURITY_SCOPE)
                .where(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_ID.eq(oasOperationSecurityId))
                .orderBy(OAS_OPERATION_SECURITY_SCOPE.OAS_OPERATION_SECURITY_SCOPE_ID.asc())
                .fetchInto(String.class);
    }

    private Collection<Condition> getConditions(GetBieForOasDocRequest request) {
        List<Condition> conditions = new ArrayList();
        OasDocId oasDocId = request.getOasDocId();
        if (oasDocId != null) {
            conditions.add(OAS_DOC.as("oas_doc").OAS_DOC_ID.eq(valueOf(oasDocId)));
        }
        TopLevelAsbiepId topLevelAsbiepId = request.getTopLevelAsbiepId();
        if (topLevelAsbiepId != null) {
            conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(valueOf(topLevelAsbiepId)));
        }
        return conditions;
    }

    private List<SortField<?>> getSortFields(GetBieForOasDocRequest request) {
        List<SortField<?>> sortFields = new ArrayList<>();

        for (int i = 0, len = request.getSortActives().size(); i < len; ++i) {
            String sortActive = request.getSortActives().get(i);
            SortDirection sortDirection = request.getSortDirections().get(i);

            Field field;
            switch (sortActive.toLowerCase()) {
                case "den":
                    field = ASCCP_MANIFEST.DEN;
                    break;
                case "remark":
                    field = ASBIEP.REMARK;
                    break;
                case "verb":
                    field = OAS_OPERATION.as("oas_operation").VERB.as("verb");
                    break;
                case "array":
                case "arrayindicator":
                    field = field("array_indicator");
                    break;
                case "suppressroot":
                case "suppressrootindicator":
                    field = field("suppress_root_indicator");
                    break;
                case "messagebody":
                    field = field("oas_doc_message_body_type");
                    break;
                case "resourcename":
                    field = OAS_RESOURCE.as("oas_resource").PATH.as("resource_name");
                    break;
                case "operationid":
                    field = OAS_OPERATION.as("oas_operation").OPERATION_ID.as("operation_id");
                    break;
                case "tagname":
                    field = OAS_TAG.NAME.as("tag_name");
                    break;
                case "lastupdatetimestamp":
                    field = OAS_MESSAGE_BODY.LAST_UPDATE_TIMESTAMP;
                    break;
                default:
                    continue;
            }

            if (sortDirection == ASC) {
                sortFields.add(field.asc());
            } else {
                sortFields.add(field.desc());
            }
        }
        return sortFields;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetBieForOasDocResponse getBieForOasDoc(GetBieForOasDocRequest request) throws ScoreDataAccessException {
        SelectOrderByStep orderByStep = selectForRequest()
                .where(getConditions(request))
                .unionAll(selectForResponse()
                        .where(getConditions(request)));

        List<SortField<?>> sortFields = getSortFields(request);
        int length = dslContext().fetchCount(orderByStep);
        SelectFinalStep finalStep;
        if (sortFields == null || sortFields.isEmpty()) {
            if (request.isPagination()) {
                finalStep = orderByStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = orderByStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = orderByStep.orderBy(sortFields)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = orderByStep.orderBy(sortFields);
            }
        }
        return new GetBieForOasDocResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    @Override
    public GetAssignedOasTagResponse getAssignedOasTag(GetAssignedOasTagRequest request) throws ScoreDataAccessException {
        OasTagRecord oasTagRecord = null;
        if (request.getMessageBodyType().equals("Request")) {
            //Get oasTag
            OasResourceTagRecord req_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("req_oas_resource_tag"))
                    .where(OAS_RESOURCE_TAG.as("req_oas_resource_tag").OAS_OPERATION_ID.eq(valueOf(request.getOasOperationId())))
                    .fetchOptional().orElse(null);
            if (req_oasResourceTagRecord == null) {
                return EMPTY_INSTANCE;
            } else {
                ULong oasTagId = req_oasResourceTagRecord.getOasTagId();
                oasTagRecord = dslContext().selectFrom(OAS_TAG.as("req_oas_tag"))
                        .where(OAS_TAG.as("req_oas_tag").OAS_TAG_ID.eq(oasTagId)).fetchOptional().orElse(null);
            }
        } else if (request.getMessageBodyType().equals("Response")) {
            //Get oasTag
            OasResourceTagRecord res_oasResourceTagRecord = dslContext().selectFrom(OAS_RESOURCE_TAG.as("res_oas_resource_tag"))
                    .where(OAS_RESOURCE_TAG.as("res_oas_resource_tag").OAS_OPERATION_ID.eq(valueOf(request.getOasOperationId())))
                    .fetchOptional().orElse(null);
            if (res_oasResourceTagRecord == null) {
                return EMPTY_INSTANCE;
            } else {
                ULong oasTagId = res_oasResourceTagRecord.getOasTagId();
                oasTagRecord = dslContext().selectFrom(OAS_TAG.as("res_oas_tag"))
                        .where(OAS_TAG.as("res_oas_tag").OAS_TAG_ID.eq(oasTagId)).fetchOptional().orElse(null);
            }
        } else {
            throw new ScoreDataAccessException("Wrong MessageBody Type: " + request.getMessageBodyType());
        }

        GetAssignedOasTagResponse response;
        if (oasTagRecord != null) {
            OasTag oasTag = new OasTag(
                    oasTagRecord.getOasTagId().toBigInteger(),
                    oasTagRecord.getGuid(),
                    oasTagRecord.getName(),
                    oasTagRecord.getDescription(),
                    null,
                    null);
            response = new GetAssignedOasTagResponse(oasTag);
        } else {
            return EMPTY_INSTANCE;
        }

        return response;
    }
}
