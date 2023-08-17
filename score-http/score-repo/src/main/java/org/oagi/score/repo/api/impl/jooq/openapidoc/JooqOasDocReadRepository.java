package org.oagi.score.repo.api.impl.jooq.openapidoc;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.openapidoc.OasDocReadRepository;
import org.oagi.score.repo.api.openapidoc.model.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqOasDocReadRepository extends JooqScoreRepository
        implements OasDocReadRepository {

    public JooqOasDocReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select() {
        return dslContext().select(
                        OAS_DOC.OAS_DOC_ID,
                        OAS_DOC.GUID,
                        OAS_DOC.OPEN_API_VERSION,
                        OAS_DOC.TITLE,
                        OAS_DOC.DESCRIPTION,
                        OAS_DOC.TERMS_OF_SERVICE,
                        OAS_DOC.VERSION,
                        OAS_DOC.CONTACT_NAME,
                        OAS_DOC.CONTACT_URL,
                        OAS_DOC.CONTACT_EMAIL,
                        OAS_DOC.LICENSE_NAME,
                        OAS_DOC.LICENSE_URL,
                        APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                        APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                        APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                        APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                        APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                        APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                        OAS_DOC.CREATION_TIMESTAMP,
                        OAS_DOC.LAST_UPDATE_TIMESTAMP)
                .from(OAS_DOC)
                .join(APP_USER.as("creator")).on(OAS_DOC.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(OAS_DOC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, OasDoc> mapper() {
        return record -> {
            OasDoc oasDoc = new OasDoc();
            oasDoc.setOasDocId(record.get(OAS_DOC.OAS_DOC_ID).toBigInteger());
            oasDoc.setGuid(record.get(OAS_DOC.GUID));
            oasDoc.setOpenAPIVersion(record.get(OAS_DOC.OPEN_API_VERSION));
            oasDoc.setTitle(record.get(OAS_DOC.TITLE));
            oasDoc.setDescription(record.get(OAS_DOC.DESCRIPTION));
            oasDoc.setTermsOfService(record.get(OAS_DOC.TERMS_OF_SERVICE));
            oasDoc.setVersion(record.get(OAS_DOC.VERSION));
            oasDoc.setContactName(record.get(OAS_DOC.CONTACT_NAME));
            oasDoc.setContactUrl(record.get(OAS_DOC.CONTACT_URL));
            oasDoc.setContactEmail(record.get(OAS_DOC.CONTACT_EMAIL));
            oasDoc.setLicenseName(record.get(OAS_DOC.LICENSE_NAME));
            oasDoc.setLicenseUrl(record.get(OAS_DOC.LICENSE_URL));
            oasDoc.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            oasDoc.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            oasDoc.setCreationTimestamp(
                    Date.from(record.get(OAS_DOC.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            oasDoc.setLastUpdateTimestamp(
                    Date.from(record.get(OAS_DOC.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return oasDoc;
        };
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasDocResponse getOasDoc(
            GetOasDocRequest request) throws ScoreDataAccessException {
        OasDoc oasDoc = null;

        BigInteger oasDocId = request.getOasDocId();
        if (oasDocId != null) {
            oasDoc = (OasDoc) select()
                    .where(OAS_DOC.OAS_DOC_ID.eq(ULong.valueOf(oasDocId)))
                    .fetchOne(mapper());
        }

        return new GetOasDocResponse(oasDoc);
    }

    private Collection<Condition> getConditions(GetOasDocListRequest request) {
        List<Condition> conditions = new ArrayList();

        if (request.getOasDocIdList() != null && !request.getOasDocIdList().isEmpty()) {
            if (request.getOasDocIdList().size() == 1) {
                conditions.add(OAS_DOC.OAS_DOC_ID.eq(
                        ULong.valueOf(request.getOasDocIdList().iterator().next())
                ));
            } else {
                conditions.add(OAS_DOC.OAS_DOC_ID.in(
                        request.getOasDocIdList().stream()
                                .map(e -> ULong.valueOf(e)).collect(Collectors.toList())
                ));
            }
        }
        if (StringUtils.hasLength(request.getTitle())) {
            conditions.addAll(contains(request.getTitle(), OAS_DOC.TITLE));
        }
        if (StringUtils.hasLength(request.getOpenAPIVersion())) {
            conditions.addAll(contains(request.getOpenAPIVersion(), OAS_DOC.OPEN_API_VERSION));
        }
        if (StringUtils.hasLength(request.getVersion())) {
            conditions.addAll(contains(request.getVersion(), OAS_DOC.VERSION));
        }
        if (StringUtils.hasLength(request.getLicenseName())) {
            conditions.addAll(contains(request.getLicenseName(), OAS_DOC.LICENSE_NAME));
        }
        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(OAS_DOC.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetOasDocListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "title":
                field = OAS_DOC.TITLE;
                break;

            case "description":
                field = OAS_DOC.DESCRIPTION;
                break;

            case "lastupdatetimestamp":
                field = OAS_DOC.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasDocListResponse getOasDocList(
            GetOasDocListRequest request) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request);
        SelectConditionStep conditionStep = select().where(conditions);

        SortField sortField = getSortField(request);
        int length = dslContext().fetchCount(conditionStep);
        SelectFinalStep finalStep;
        if (sortField == null) {
            if (request.isPagination()) {
                finalStep = conditionStep.limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep;
            }
        } else {
            if (request.isPagination()) {
                finalStep = conditionStep.orderBy(sortField)
                        .limit(request.getPageOffset(), request.getPageSize());
            } else {
                finalStep = conditionStep.orderBy(sortField);
            }
        }

        return new GetOasDocListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetOasOperationResponse getOasOperation(GetOasOperationRequest request) throws ScoreDataAccessException {
        OasOperation oasOperation = null;
        BigInteger oasResourceId = request.getOasResourceId();
        if (oasResourceId != null){
            oasOperation = (OasOperation) selectForOasOperation()
                    .where(OAS_OPERATION.OAS_RESOURCE_ID.eq(ULong.valueOf(oasResourceId)))
                    .fetchOne(mapperForOasOperation());
        }
        return new GetOasOperationResponse(oasOperation);
    }

    private RecordMapper<Record, OasOperation> mapperForOasOperation() {
        return record -> {
            OasOperation oasOperation = new OasOperation();
            oasOperation.setOasOperationId(record.get(OAS_OPERATION.OAS_OPERATION_ID).toBigInteger());
            oasOperation.setOasResourceId(record.get(OAS_OPERATION.OAS_RESOURCE_ID).toBigInteger());
            oasOperation.setVerb(record.get(OAS_OPERATION.VERB));
            oasOperation.setDeprecated((byte) 1 == record.get(OAS_OPERATION.DEPRECATED));
            oasOperation.setOperationId(record.get(OAS_OPERATION.OPERATION_ID));
            oasOperation.setSummary(record.get(OAS_OPERATION.SUMMARY));
            oasOperation.setDescription(record.get(OAS_OPERATION.DESCRIPTION));
            oasOperation.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            oasOperation.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            oasOperation.setCreationTimestamp(
                    Date.from(record.get(OAS_DOC.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            oasOperation.setLastUpdateTimestamp(
                    Date.from(record.get(OAS_DOC.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return oasOperation;
        };
    }

    private SelectOnConditionStep selectForOasOperation() {
        return dslContext().select(
                        OAS_OPERATION.OAS_OPERATION_ID,
                        OAS_OPERATION.OAS_RESOURCE_ID,
                        OAS_OPERATION.DESCRIPTION,
                        OAS_OPERATION.OPERATION_ID,
                        OAS_OPERATION.VERB,
                        OAS_OPERATION.SUMMARY,
                        OAS_OPERATION.DEPRECATED,
                        APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                        APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                        APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                        APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                        APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                        APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                        OAS_OPERATION.CREATION_TIMESTAMP,
                        OAS_OPERATION.LAST_UPDATE_TIMESTAMP)
                .from(OAS_OPERATION)
                .join(APP_USER.as("creator")).on(OAS_OPERATION.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(OAS_OPERATION.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }
}
