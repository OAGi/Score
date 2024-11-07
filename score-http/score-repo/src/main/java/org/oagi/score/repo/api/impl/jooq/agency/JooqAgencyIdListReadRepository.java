package org.oagi.score.repo.api.impl.jooq.agency;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.agency.AgencyIdListReadRepository;
import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.agency.model.AgencyIdListValue;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListRequest;
import org.oagi.score.repo.api.agency.model.GetAgencyIdListListResponse;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.model.CcState;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetReleaseRecord;
import org.oagi.score.repo.api.impl.utils.StringUtils;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.base.SortDirection.ASC;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.jooq.utils.DSLUtils.contains;
import static org.oagi.score.repo.api.impl.utils.StringUtils.trim;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqAgencyIdListReadRepository
        extends JooqScoreRepository
        implements AgencyIdListReadRepository {

    public JooqAgencyIdListReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep select(ULong defaultModuleSetReleaseId) {
        return dslContext().select(AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID,
                AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST.GUID,
                AGENCY_ID_LIST.ENUM_TYPE_GUID,
                AGENCY_ID_LIST.NAME,
                AGENCY_ID_LIST.LIST_ID,
                AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID,
                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                AGENCY_ID_LIST.VERSION_ID,
                AGENCY_ID_LIST.BASED_AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST.as("base").NAME.as("base_name"),
                AGENCY_ID_LIST.DEFINITION,
                AGENCY_ID_LIST.DEFINITION_SOURCE,
                AGENCY_ID_LIST.REMARK,
                AGENCY_ID_LIST.NAMESPACE_ID,
                AGENCY_ID_LIST.IS_DEPRECATED,
                iif(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull(), true, false).as("new_component"),
                AGENCY_ID_LIST.STATE,
                AGENCY_ID_LIST.CREATION_TIMESTAMP,
                AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP,
                RELEASE.RELEASE_NUM,
                RELEASE.STATE.as("release_state"),
                RELEASE.RELEASE_ID,
                LOG.REVISION_NUM,
                MODULE.PATH,
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").NAME.as("creator_name"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                APP_USER.as("owner").NAME.as("owner_name"),
                APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").NAME.as("updater_name"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"))
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_MANIFEST.as("base_manifest")).on(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("base_manifest").AGENCY_ID_LIST_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST.as("base")).on(AGENCY_ID_LIST_MANIFEST.as("base_manifest").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.as("base").AGENCY_ID_LIST_ID))
                .join(RELEASE).on(RELEASE.RELEASE_ID.eq(AGENCY_ID_LIST_MANIFEST.RELEASE_ID))
                .join(LOG).on(AGENCY_ID_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(APP_USER.as("creator")).on(AGENCY_ID_LIST.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("owner")).on(AGENCY_ID_LIST.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("updater")).on(AGENCY_ID_LIST.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .leftJoin(MODULE_AGENCY_ID_LIST_MANIFEST)
                .on(and(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(MODULE_AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID), MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE).on(MODULE_AGENCY_ID_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID));
    }

    private RecordMapper<Record, AgencyIdList> mapper() {
        return e -> {
            AgencyIdList agencyIdList = new AgencyIdList();
            agencyIdList.setAgencyIdListManifestId(e.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
            agencyIdList.setAgencyIdListId(e.get(AGENCY_ID_LIST.AGENCY_ID_LIST_ID).toBigInteger());
            agencyIdList.setGuid(e.get(AGENCY_ID_LIST.GUID));
            agencyIdList.setEnumTypeGuid(e.get(AGENCY_ID_LIST.ENUM_TYPE_GUID));
            agencyIdList.setName(e.get(AGENCY_ID_LIST.NAME));
            if (e.get(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID) != null) {
                agencyIdList.setBasedAgencyIdListManifestId(e.get(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID).toBigInteger());
                agencyIdList.setBasedAgencyIdListId(e.get(AGENCY_ID_LIST.BASED_AGENCY_ID_LIST_ID).toBigInteger());
                agencyIdList.setBasedAgencyIdListName(e.get(AGENCY_ID_LIST.as("base").NAME.as("base_name")));
            }
            agencyIdList.setListId(e.get(AGENCY_ID_LIST.LIST_ID));
            if (e.get(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID) != null) {
                agencyIdList.setAgencyIdListValueId(e.get(AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID).toBigInteger());
            }
            agencyIdList.setVersionId(e.get(AGENCY_ID_LIST.VERSION_ID));
            agencyIdList.setDefinition(e.get(AGENCY_ID_LIST.DEFINITION));
            agencyIdList.setDefinitionSource(e.get(AGENCY_ID_LIST.DEFINITION_SOURCE));
            agencyIdList.setRemark(e.get(AGENCY_ID_LIST.REMARK));
            if (e.get(AGENCY_ID_LIST.NAMESPACE_ID) != null) {
                agencyIdList.setNamespaceId(e.get(AGENCY_ID_LIST.NAMESPACE_ID).toBigInteger());
            }
            agencyIdList.setOwner(new ScoreUser(
                    e.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(),
                    e.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")),
                    e.get(APP_USER.as("owner").NAME.as("owner_name")),
                    (byte) 1 == e.get(APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer")) ? DEVELOPER : END_USER
            ));
            agencyIdList.setCreatedBy(new ScoreUser(
                    e.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    e.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    e.get(APP_USER.as("creator").NAME.as("creator_name")),
                    (byte) 1 == e.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            agencyIdList.setLastUpdatedBy(new ScoreUser(
                    e.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    e.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    e.get(APP_USER.as("updater").NAME.as("updater_name")),
                    (byte) 1 == e.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            agencyIdList.setDeprecated(e.get(AGENCY_ID_LIST.IS_DEPRECATED) == 1);
            agencyIdList.setNewComponent(e.get(field("new_component", Boolean.class)));
            agencyIdList.setState(CcState.valueOf(e.get(AGENCY_ID_LIST.STATE)));
            agencyIdList.setReleaseState(e.get(RELEASE.STATE.as("release_state")));
            agencyIdList.setReleaseNum(e.get(RELEASE.RELEASE_NUM));
            agencyIdList.setReleaseId(e.get(RELEASE.RELEASE_ID).toBigInteger());
            agencyIdList.setRevisionNum(e.get(LOG.REVISION_NUM).toString());
            agencyIdList.setModulePath(e.get(MODULE.PATH));
            agencyIdList.setCreationTimestamp(Date.from(e.get(AGENCY_ID_LIST.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            agencyIdList.setLastUpdateTimestamp(Date.from(e.get(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            if (e.get(AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID) != null) {
                agencyIdList.setPrevAgencyIdListId(e.get(AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID).toBigInteger());
            }

            if (e.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID) != null) {
                agencyIdList.setAgencyIdListValueManifestId(e.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger());
            }

            return agencyIdList;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetAgencyIdListListResponse getAgencyIdListList(GetAgencyIdListListRequest request) throws ScoreDataAccessException {
        Collection<Condition> conditions = getConditions(request);

        SelectConditionStep conditionStep;

        ULong defaultModuleSetReleaseId = null;
        ModuleSetReleaseRecord defaultModuleSetRelease = dslContext().selectFrom(MODULE_SET_RELEASE)
                .where(and(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1), MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId()))))
                .fetchOne();

        if (defaultModuleSetRelease != null) {
            defaultModuleSetReleaseId = defaultModuleSetRelease.getModuleSetReleaseId();
        }

        conditionStep = select(defaultModuleSetReleaseId).where(conditions);

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

        return new GetAgencyIdListListResponse(
                finalStep.fetch(mapper()),
                request.getPageIndex(),
                request.getPageSize(),
                length
        );
    }

    private Collection<Condition> getConditions(GetAgencyIdListListRequest request) {
        List<Condition> conditions = new ArrayList();

        conditions.add(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));

        if (StringUtils.hasLength(request.getName())) {
            conditions.addAll(contains(request.getName(), AGENCY_ID_LIST.NAME));
        }

        if (StringUtils.hasLength(request.getModule())) {
            conditions.addAll(contains(request.getModule(), MODULE.PATH));
        }

        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(contains(request.getDefinition(), AGENCY_ID_LIST.DEFINITION));
        }

        if (request.getStates().size() > 0) {
            conditions.add(AGENCY_ID_LIST.STATE.in(request.getStates()));
        }

        if (request.getNamespaces() != null && !request.getNamespaces().isEmpty()) {
            conditions.add(AGENCY_ID_LIST.NAMESPACE_ID.in(request.getNamespaces()));
        }

        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("owner").LOGIN_ID.in(
                    new HashSet<>(request.getOwnerLoginIds()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }

        if (request.getDeprecated() != null) {
            conditions.add(AGENCY_ID_LIST.IS_DEPRECATED.eq(request.getDeprecated() ? (byte) 1 : 0));
        }

        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent() ?
                    AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull() :
                    AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNotNull());
        }

        if (!request.getUpdaterUsernameList().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(
                    new HashSet<>(request.getUpdaterUsernameList()).stream()
                            .filter(e -> StringUtils.hasLength(e)).map(e -> trim(e)).collect(Collectors.toList())
            ));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP.greaterOrEqual(request.getUpdateStartDate()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP.lessThan(request.getUpdateEndDate()));
        }

        return conditions;
    }

    private SortField getSortField(GetAgencyIdListListRequest request) {
        if (!StringUtils.hasLength(request.getSortActive())) {
            return null;
        }

        Field field;
        switch (trim(request.getSortActive()).toLowerCase()) {
            case "state":
                field = AGENCY_ID_LIST.STATE;
                break;

            case "name":
                field = AGENCY_ID_LIST.NAME;
                break;

            case "version":
            case "versionid":
                field = AGENCY_ID_LIST.VERSION_ID;
                break;

            case "revision":
                field = LOG.REVISION_NUM;
                break;

            case "owner":
                field = APP_USER.as("owner").LOGIN_ID;
                break;

            case "module":
                field = MODULE.PATH;
                break;

            case "lastupdatetimestamp":
                field = AGENCY_ID_LIST.LAST_UPDATE_TIMESTAMP;
                break;

            default:
                return null;
        }

        return (request.getSortDirection() == ASC) ? field.asc() : field.desc();
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public AgencyIdList getAgencyIdList(BigInteger agencyIdListManifestId) throws ScoreDataAccessException {
        AgencyIdList agencyIdList = (AgencyIdList) select(null)
                .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(agencyIdListManifestId)))
                .fetchOne(mapper());
        if (agencyIdList == null) {
            throw new IllegalArgumentException("Can not found agency id list.");
        }
        agencyIdList.setValues(getAgencyIdListValueList(agencyIdListManifestId));
        return agencyIdList;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public AgencyIdList getAgencyIdListById(BigInteger agencyIdListId) throws ScoreDataAccessException {
        AgencyIdList agencyIdList = dslContext().select(AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST.GUID,
                AGENCY_ID_LIST.ENUM_TYPE_GUID,
                AGENCY_ID_LIST.NAME,
                AGENCY_ID_LIST.LIST_ID,
                AGENCY_ID_LIST.AGENCY_ID_LIST_VALUE_ID,
                AGENCY_ID_LIST.VERSION_ID,
                AGENCY_ID_LIST.BASED_AGENCY_ID_LIST_ID,
                AGENCY_ID_LIST.DEFINITION,
                AGENCY_ID_LIST.DEFINITION_SOURCE,
                AGENCY_ID_LIST.REMARK,
                AGENCY_ID_LIST.NAMESPACE_ID,
                AGENCY_ID_LIST.IS_DEPRECATED.as("deprecated"),
                AGENCY_ID_LIST.STATE)
                .from(AGENCY_ID_LIST)
                .where(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(ULong.valueOf(agencyIdListId)))
                .fetchOneInto(AgencyIdList.class);

        agencyIdList.setValues(dslContext().select(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID,
                AGENCY_ID_LIST_VALUE.GUID,
                AGENCY_ID_LIST_VALUE.NAME,
                AGENCY_ID_LIST_VALUE.VALUE,
                AGENCY_ID_LIST_VALUE.DEFINITION,
                AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE,
                AGENCY_ID_LIST_VALUE.IS_DEPRECATED.as("deprecated"))
                .from(AGENCY_ID_LIST_VALUE)
                .where(AGENCY_ID_LIST_VALUE.OWNER_LIST_ID.eq(ULong.valueOf(agencyIdListId)))
                .fetchInto(AgencyIdListValue.class));
        return agencyIdList;
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public List<AgencyIdListValue> getAgencyIdListValueList(BigInteger agencyIdListManifestId) throws ScoreDataAccessException {
        List<AgencyIdListValue> agencyIdListValueList = dslContext().select(
                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                AGENCY_ID_LIST_VALUE_MANIFEST.BASED_AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                AGENCY_ID_LIST_VALUE.GUID,
                AGENCY_ID_LIST_VALUE.NAME,
                AGENCY_ID_LIST_VALUE.VALUE,
                AGENCY_ID_LIST_VALUE.DEFINITION,
                AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE,
                AGENCY_ID_LIST_VALUE.IS_DEPRECATED)
                .from(AGENCY_ID_LIST_VALUE)
                .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID))
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(ULong.valueOf(agencyIdListManifestId)))
        .fetch(e -> {
            AgencyIdListValue agencyIdListValue = new AgencyIdListValue();
            agencyIdListValue.setAgencyIdListValueManifestId(e.get(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).toBigInteger());
            ULong basedAgencyIdListValueManifestId = e.get(AGENCY_ID_LIST_VALUE_MANIFEST.BASED_AGENCY_ID_LIST_VALUE_MANIFEST_ID);
            if (basedAgencyIdListValueManifestId != null) {
                agencyIdListValue.setBasedAgencyIdListValueManifestId(basedAgencyIdListValueManifestId.toBigInteger());
            }
            agencyIdListValue.setDeprecated(e.get(AGENCY_ID_LIST_VALUE.IS_DEPRECATED) == 1);
            agencyIdListValue.setGuid(e.get(AGENCY_ID_LIST_VALUE.GUID));
            agencyIdListValue.setValue(e.get(AGENCY_ID_LIST_VALUE.VALUE));
            agencyIdListValue.setName(e.get(AGENCY_ID_LIST_VALUE.NAME));
            agencyIdListValue.setDefinition(e.get(AGENCY_ID_LIST_VALUE.DEFINITION));
            agencyIdListValue.setDefinitionSource(e.get(AGENCY_ID_LIST_VALUE.DEFINITION_SOURCE));
            return agencyIdListValue;
        });

        if (!agencyIdListValueList.isEmpty()) {
            List<ULong> agencyIdListValueIdList = agencyIdListValueList.stream()
                    .map(e -> ULong.valueOf(e.getAgencyIdListValueManifestId())).collect(Collectors.toList());
            Map<ULong, Integer> codeListRefCounter = dslContext().select(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            count(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).as("cnt"))
                    .from(CODE_LIST_MANIFEST)
                    .where(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.in(agencyIdListValueIdList))
                    .groupBy(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                    .fetchMap(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            count(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).as("cnt"));

            Map<ULong, Integer> agencyIdListRefCounter = dslContext().select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            count(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).as("cnt"))
                    .from(AGENCY_ID_LIST_MANIFEST)
                    .where(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.in(agencyIdListValueIdList))
                    .groupBy(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                    .fetchMap(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                            count(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID).as("cnt"));

            for (AgencyIdListValue agencyIdListValue : agencyIdListValueList) {
                ULong key = ULong.valueOf(agencyIdListValue.getAgencyIdListValueManifestId());
                if (codeListRefCounter.containsKey(key) || agencyIdListRefCounter.containsKey(key)) {
                    agencyIdListValue.setUsed(true);
                }
            }
        }

        return agencyIdListValueList;
    }
}
