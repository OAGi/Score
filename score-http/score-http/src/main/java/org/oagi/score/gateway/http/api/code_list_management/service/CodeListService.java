package org.oagi.score.gateway.http.api.code_list_management.service;

import org.jooq.*;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.code_list_management.data.*;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.redis.event.EventHandler;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.component.code_list.*;
import org.oagi.score.service.common.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.jooq.impl.DSL.or;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.bie.model.BieState.Production;
import static org.oagi.score.repo.api.bie.model.BieState.QA;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Service
@Transactional(readOnly = true)
public class CodeListService extends EventHandler {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CodeListWriteRepository codeListWriteRepository;

    private SelectOnConditionStep<Record22<
            ULong, ULong, String, String, ULong,
            String, String, ULong, String, String,
            String, LocalDateTime, ULong, String, String,
            Byte, String, Byte, String, String,
            String, UInteger>> getSelectOnConditionStep(ULong defaultModuleSetReleaseId) {
        return dslContext.select(
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                CODE_LIST.CODE_LIST_ID,
                CODE_LIST.GUID,
                CODE_LIST.NAME.as("code_list_name"),
                CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                CODE_LIST.as("based_code_list").NAME.as("based_code_list_name"),
                CODE_LIST.LIST_ID,
                CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                AGENCY_ID_LIST_VALUE.VALUE.as("agency_id_list_value_value"),
                AGENCY_ID_LIST_VALUE.NAME.as("agency_id_list_value_name"),
                CODE_LIST.VERSION_ID,
                CODE_LIST.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("owner").APP_USER_ID.as("owner_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner"),
                APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                CODE_LIST.EXTENSIBLE_INDICATOR.as("extensible"),
                CODE_LIST.STATE,
                CODE_LIST.IS_DEPRECATED.as("deprecated"),
                CODE_LIST.DEFINITION,
                CODE_LIST.DEFINITION_SOURCE,
                MODULE.PATH.as("module_path"),
                LOG.REVISION_NUM.as("revision"))
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(APP_USER.as("owner")).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("updater")).on(CODE_LIST.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(CODE_LIST_MANIFEST.as("based")).on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based").CODE_LIST_MANIFEST_ID))
                .leftJoin(CODE_LIST.as("based_code_list")).on(CODE_LIST_MANIFEST.as("based").CODE_LIST_ID.eq(CODE_LIST.as("based_code_list").CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                .leftJoin(MODULE_CODE_LIST_MANIFEST)
                .on(and(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID), MODULE_CODE_LIST_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID));
    }

    public PageResponse<CodeListForList> getCodeLists(AuthenticatedPrincipal user, CodeListForListRequest request) {

        ULong defaultModuleSetReleaseId = null;
        ModuleSetReleaseRecord defaultModuleSetRelease = dslContext.selectFrom(MODULE_SET_RELEASE)
                .where(and(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1), MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId()))))
                .fetchOne();

        if (defaultModuleSetRelease != null) {
            defaultModuleSetReleaseId = defaultModuleSetRelease.getModuleSetReleaseId();
        }

        SelectOnConditionStep<Record22<
                ULong, ULong, String, String, ULong,
                String, String, ULong, String, String,
                String, LocalDateTime, ULong, String, String,
                Byte, String, Byte, String, String,
                String, UInteger>> step = getSelectOnConditionStep(defaultModuleSetReleaseId);

        List<Condition> conditions = new ArrayList();
        conditions.add(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));

        if (StringUtils.hasLength(request.getName())) {
            conditions.addAll(contains(request.getName(), CODE_LIST.NAME));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(contains(request.getDefinition(), CODE_LIST.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getAccess() != null) {
            AppUser requester = sessionService.getAppUserByUsername(user);
            switch (request.getAccess()) {
                case CanEdit:
                    conditions.add(CODE_LIST.OWNER_USER_ID.eq(ULong.valueOf(requester.getAppUserId())));
                    break;

                case CanView:
                    conditions.add(
                            or(
                                    CODE_LIST.STATE.in(QA.name(), Production.name()),
                                    CODE_LIST.OWNER_USER_ID.eq(ULong.valueOf(requester.getAppUserId()))
                            )
                    );
                    break;
            }
        }
        if (!request.getStates().isEmpty()) {
            conditions.add(CODE_LIST.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (request.getDeprecated() != null) {
            conditions.add(CODE_LIST.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getExtensible() != null) {
            conditions.add(CODE_LIST.EXTENSIBLE_INDICATOR.eq((byte) (request.getExtensible() ? 1 : 0)));
        }
        if (request.getNamespaces() != null && !request.getNamespaces().isEmpty()) {
            conditions.add(CODE_LIST.NAMESPACE_ID.in(request.getNamespaces()));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("owner").LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(CODE_LIST.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(CODE_LIST.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }
        if (request.getOwnedByDeveloper() != null) {
            conditions.add(APP_USER.as("owner").IS_DEVELOPER.eq(request.getOwnedByDeveloper() ? (byte) 1 : 0));
        }

        SelectConnectByStep<Record22<
                ULong, ULong, String, String, ULong,
                String, String, ULong, String, String,
                String, LocalDateTime, ULong, String, String,
                Byte, String, Byte, String, String,
                String, UInteger>> conditionStep = step;
        if (!conditions.isEmpty()) {
            conditionStep = step.where(conditions);
        }

        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
        if (StringUtils.hasLength(pageRequest.getSortActive())) {
            switch (pageRequest.getSortActive()) {
                case "codeListName":
                    if ("asc".equals(sortDirection)) {
                        sortField = CODE_LIST.NAME.asc();
                    } else if ("desc".equals(sortDirection)) {
                        sortField = CODE_LIST.NAME.desc();
                    }

                    break;

                case "lastUpdateTimestamp":
                    if ("asc".equals(sortDirection)) {
                        sortField = CODE_LIST.LAST_UPDATE_TIMESTAMP.asc();
                    } else if ("desc".equals(sortDirection)) {
                        sortField = CODE_LIST.LAST_UPDATE_TIMESTAMP.desc();
                    }

                    break;
            }
        }


        SelectWithTiesAfterOffsetStep<Record22<
                ULong, ULong, String, String, ULong,
                String, String, ULong, String, String,
                String, LocalDateTime, ULong, String, String,
                Byte, String, Byte, String, String,
                String, UInteger>> offsetStep = null;
        if (sortField != null) {
            offsetStep = conditionStep.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = conditionStep
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<CodeListForList> result = (offsetStep != null) ?
                offsetStep.fetchInto(CodeListForList.class) : conditionStep.fetchInto(CodeListForList.class);

        String releaseNum = dslContext.select(RELEASE.RELEASE_NUM).from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId()))).fetchOneInto(String.class);
        boolean isWorkingRelease = releaseNum.equals("Working");

        AppUser requester = sessionService.getAppUserByUsername(user);
        result.stream().forEach(e -> {
            e.setAccess(
                    AccessPrivilege.toAccessPrivilege(requester, sessionService.getAppUserByUsername(e.getOwnerId()),
                            CcState.valueOf(e.getState()), isWorkingRelease)
            );
            e.setOwnerId(null); // hide sensitive information
        });

        PageResponse<CodeListForList> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(dslContext.selectCount()
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(APP_USER.as("owner")).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("updater")).on(CODE_LIST.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID))
                .join(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .leftJoin(CODE_LIST_MANIFEST.as("based")).on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based").CODE_LIST_MANIFEST_ID))
                .leftJoin(CODE_LIST.as("based_code_list")).on(CODE_LIST_MANIFEST.as("based").CODE_LIST_ID.eq(CODE_LIST.as("based_code_list").CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                .leftJoin(MODULE_CODE_LIST_MANIFEST)
                .on(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(MODULE_CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .leftJoin(MODULE)
                .on(MODULE_CODE_LIST_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions)
                .fetchOptionalInto(Integer.class).orElse(0));

        return response;
    }

    public CodeList getCodeList(AuthenticatedPrincipal user, BigInteger manifestId) {
        CodeList codeList = dslContext.select(
                CODE_LIST_MANIFEST.RELEASE_ID,
                RELEASE.STATE.as("release_state"),
                RELEASE.RELEASE_NUM,
                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID,
                CODE_LIST.NAME.as("code_list_name"),
                CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                CODE_LIST.as("based_code_list").NAME.as("based_code_list_name"),
                NAMESPACE.NAMESPACE_ID,
                NAMESPACE.URI.as("namespace_uri"),
                CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                AGENCY_ID_LIST_VALUE.VALUE.as("agency_id_list_value_value"),
                AGENCY_ID_LIST_VALUE.NAME.as("agency_id_list_value_name"),
                CODE_LIST.VERSION_ID,
                CODE_LIST.GUID,
                CODE_LIST.LIST_ID,
                CODE_LIST.DEFINITION,
                CODE_LIST.DEFINITION_SOURCE,
                CODE_LIST.REMARK,
                CODE_LIST.EXTENSIBLE_INDICATOR.as("extensible"),
                APP_USER.as("owner").APP_USER_ID.as("owner_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner"),
                CODE_LIST.STATE,
                CODE_LIST.IS_DEPRECATED.as("deprecated"),
                LOG.LOG_ID,
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM)
                .from(CODE_LIST_MANIFEST)
                .join(RELEASE).on(CODE_LIST_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .join(LOG).on(CODE_LIST_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(APP_USER.as("owner")).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .leftJoin(CODE_LIST_MANIFEST.as("based")).on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based").CODE_LIST_MANIFEST_ID))
                .leftJoin(CODE_LIST.as("based_code_list")).on(CODE_LIST_MANIFEST.as("based").CODE_LIST_ID.eq(CODE_LIST.as("based_code_list").CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE_MANIFEST).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                .leftJoin(NAMESPACE).on(CODE_LIST.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(manifestId)))
                .fetchOptionalInto(CodeList.class).orElse(null);

        if (codeList == null) {
            throw new EmptyResultDataAccessException(1);
        }

        String releaseNum = dslContext.select(RELEASE.RELEASE_NUM).from(RELEASE)
                .where(RELEASE.RELEASE_ID.eq(ULong.valueOf(codeList.getReleaseId()))).fetchOneInto(String.class);
        boolean isWorkingRelease = releaseNum.equals("Working");

        AppUser requester = sessionService.getAppUserByUsername(user);
        codeList.setAccess(
                AccessPrivilege.toAccessPrivilege(requester, sessionService.getAppUserByUsername(codeList.getOwnerId()),
                        CcState.valueOf(codeList.getState()), isWorkingRelease)
        );
        codeList.setOwnerId(null); // hide sensitive information

        boolean isPublished = CodeListState.Published.name().equals(codeList.getState());

        List<Condition> conditions = new ArrayList();
        conditions.add(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(manifestId)));

        List<CodeListValue> codeListValues = dslContext.select(
                CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID,
                CODE_LIST_VALUE_MANIFEST.BASED_CODE_LIST_VALUE_MANIFEST_ID,
                CODE_LIST_VALUE.VALUE,
                CODE_LIST_VALUE.MEANING,
                CODE_LIST_VALUE.GUID,
                CODE_LIST_VALUE.DEFINITION,
                CODE_LIST_VALUE.DEFINITION_SOURCE,
                CODE_LIST_VALUE.IS_DEPRECATED.as("deprecated"))
                .from(CODE_LIST_VALUE)
                .join(CODE_LIST_VALUE_MANIFEST).on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID))
                .join(CODE_LIST_MANIFEST)
                    .on(and(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID),
                            CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID)))
                .where(conditions)
                .fetchInto(CodeListValue.class);
        codeList.setCodeListValues(codeListValues);

        if (codeList.getBasedCodeListManifestId() != null) {
            List<String> basedCodeListValueList = dslContext.select(CODE_LIST_VALUE.VALUE)
                    .from(CODE_LIST_VALUE)
                    .join(CODE_LIST_VALUE_MANIFEST).on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID))
                    .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeList.getBasedCodeListManifestId())))
                    .fetchInto(String.class);
            codeListValues.stream().forEach(e -> e.setDerived(basedCodeListValueList.contains(e.getValue())));
        }

        return codeList;
    }

    @Transactional
    public BigInteger createCodeList(AuthenticatedPrincipal user, CodeList codeList) {
        LocalDateTime timestamp = LocalDateTime.now();
        CreateCodeListRepositoryRequest repositoryRequest =
                new CreateCodeListRepositoryRequest(user, timestamp, codeList.getBasedCodeListManifestId(),
                        codeList.getReleaseId());

        CreateCodeListRepositoryResponse repositoryResponse =
                codeListWriteRepository.createCodeList(repositoryRequest);

        fireEvent(new CreatedCodeListEvent());

        return repositoryResponse.getCodeListManifestId();
    }

    private void createCodeList(CodeListRecord codeListRecord, CodeListManifestRecord manifestRecord,
                                CodeListValue codeListValue) {

        ULong codeListValueId = dslContext.insertInto(CODE_LIST_VALUE,
                CODE_LIST_VALUE.CODE_LIST_ID,
                CODE_LIST_VALUE.VALUE,
                CODE_LIST_VALUE.MEANING,
                CODE_LIST_VALUE.DEFINITION,
                CODE_LIST_VALUE.DEFINITION_SOURCE,
                CODE_LIST_VALUE.CREATED_BY,
                CODE_LIST_VALUE.OWNER_USER_ID,
                CODE_LIST_VALUE.LAST_UPDATED_BY,
                CODE_LIST_VALUE.CREATION_TIMESTAMP,
                CODE_LIST_VALUE.LAST_UPDATE_TIMESTAMP).values(
                manifestRecord.getCodeListId(),
                codeListValue.getValue(),
                codeListValue.getMeaning(),
                codeListValue.getDefinition(),
                codeListValue.getDefinitionSource(),
                codeListRecord.getCreatedBy(),
                codeListRecord.getOwnerUserId(),
                codeListRecord.getLastUpdatedBy(),
                codeListRecord.getCreationTimestamp(),
                codeListRecord.getLastUpdateTimestamp())
                .returning(CODE_LIST_VALUE.CODE_LIST_VALUE_ID).fetchOne().getCodeListValueId();

        dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                .set(CODE_LIST_VALUE_MANIFEST.RELEASE_ID, manifestRecord.getReleaseId())
                .set(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID, codeListValueId)
                .set(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID, manifestRecord.getCodeListManifestId())
                .execute();
    }

    @Transactional
    public void update(AuthenticatedPrincipal user, CodeList codeList) {
        LocalDateTime timestamp = LocalDateTime.now();
        if (StringUtils.hasLength(codeList.getState())) {
            updateCodeListState(user, timestamp, codeList.getCodeListManifestId(), CcState.valueOf(codeList.getState()));
        } else {
            updateCodeListValues(user, timestamp, codeList);
            updateCodeListProperties(user, timestamp, codeList);
        }
    }

    @Transactional
    public BigInteger makeNewRevision(AuthenticatedPrincipal user, BigInteger codeListManifestId) {
        LocalDateTime timestamp = LocalDateTime.now();
        ReviseCodeListRepositoryRequest reviseCodeListRepositoryRequest
                = new ReviseCodeListRepositoryRequest(user, codeListManifestId, timestamp);

        ReviseCodeListRepositoryResponse reviseCodeListRepositoryResponse =
                codeListWriteRepository.reviseCodeList(reviseCodeListRepositoryRequest);

        fireEvent(new ReviseCodeListEvent());

        return reviseCodeListRepositoryResponse.getCodeListManifestId();
    }

    @Transactional
    public BigInteger cancelRevision(AuthenticatedPrincipal user, BigInteger codeListManifestId) {
        CancelRevisionCodeListRepositoryRequest request
                = new CancelRevisionCodeListRepositoryRequest(user, codeListManifestId);

        CancelRevisionCodeListRepositoryResponse response =
                codeListWriteRepository.cancelRevisionCodeList(request);

        fireEvent(new CancelRevisionCodeListEvent());

        return response.getCodeListManifestId();
    }

    public CodeList getCodeListRevision(AuthenticatedPrincipal user, BigInteger codeListManifestId) {
        CodeListManifestRecord codeListManifestRecord = dslContext.selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(codeListManifestId)))
                .fetchOne();
        if (codeListManifestRecord == null) {
            throw new IllegalArgumentException("Unknown CodeList: " + codeListManifestId);
        }

        CodeListRecord codeListRecord = dslContext.selectFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListManifestRecord.getCodeListId()))
                .fetchOne();

        ULong lastPublishedCodeListId = codeListRecord.getPrevCodeListId();
        if (lastPublishedCodeListId == null) {
            // rev = 1 return null
            return null;
        }
        CodeList codeList = dslContext.select(
                CODE_LIST.CODE_LIST_ID,
                CODE_LIST.NAME.as("code_list_name"),
                CODE_LIST.VERSION_ID,
                CODE_LIST.GUID,
                CODE_LIST.LIST_ID,
                CODE_LIST.DEFINITION,
                CODE_LIST.DEFINITION_SOURCE,
                CODE_LIST.REMARK,
                CODE_LIST.EXTENSIBLE_INDICATOR.as("extensible"),
                APP_USER.as("owner").APP_USER_ID.as("owner_id"),
                CODE_LIST.STATE,
                CODE_LIST.IS_DEPRECATED.as("deprecated"))
                .from(CODE_LIST)
                .join(APP_USER.as("owner")).on(CODE_LIST.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .where(CODE_LIST.CODE_LIST_ID.eq(lastPublishedCodeListId))
                .fetchOptionalInto(CodeList.class).orElse(null);

        if (codeList == null) {
            throw new EmptyResultDataAccessException(1);
        }

        List<CodeListValue> codeListValues = dslContext.select(
                CODE_LIST_VALUE.VALUE,
                CODE_LIST_VALUE.MEANING,
                CODE_LIST_VALUE.GUID,
                CODE_LIST_VALUE.DEFINITION,
                CODE_LIST_VALUE.DEFINITION_SOURCE,
                CODE_LIST_VALUE.IS_DEPRECATED.as("deprecated"))
                .from(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_ID.eq(lastPublishedCodeListId))
                .fetchInto(CodeListValue.class);
        codeList.setCodeListValues(codeListValues);

        return codeList;
    }

    public void updateCodeListState(AuthenticatedPrincipal user, LocalDateTime timestamp, BigInteger codeListManifestId, CcState state) {
        UpdateCodeListStateRepositoryRequest request =
                new UpdateCodeListStateRepositoryRequest(user, timestamp, codeListManifestId, state);

        UpdateCodeListStateRepositoryResponse response =
                codeListWriteRepository.updateCodeListState(request);

        fireEvent(new UpdatedCodeListStateEvent());
    }

    private void updateCodeListProperties(AuthenticatedPrincipal user, LocalDateTime timestamp, CodeList codeList) {
        UpdateCodeListPropertiesRepositoryRequest request =
                new UpdateCodeListPropertiesRepositoryRequest(user, timestamp, codeList.getCodeListManifestId());

        request.setCodeListName(codeList.getCodeListName());
        request.setAgencyIdListValueManifestId(codeList.getAgencyIdListValueManifestId());
        request.setVersionId(codeList.getVersionId());
        request.setListId(codeList.getListId());
        request.setNamespaceId(codeList.getNamespaceId());
        request.setDefinition(codeList.getDefinition());
        request.setDefinitionSource(codeList.getDefinitionSource());
        request.setRemark(codeList.getRemark());
        request.setExtensible(codeList.isExtensible());
        request.setDeprecated(codeList.isDeprecated());

        UpdateCodeListPropertiesRepositoryResponse response =
                codeListWriteRepository.updateCodeListProperties(request);

        fireEvent(new UpdatedCodeListPropertiesEvent());
    }

    private void updateCodeListValues(AuthenticatedPrincipal user, LocalDateTime timestamp, CodeList codeList) {
        ModifyCodeListValuesRepositoryRequest request =
                new ModifyCodeListValuesRepositoryRequest(user, timestamp,
                        codeList.getCodeListManifestId());

        request.setCodeListValueList(
                codeList.getCodeListValues().stream().map(e -> {
                    ModifyCodeListValuesRepositoryRequest.CodeListValue codeListValue =
                            new ModifyCodeListValuesRepositoryRequest.CodeListValue();

                    codeListValue.setMeaning(e.getMeaning());
                    codeListValue.setValue(e.getValue());
                    codeListValue.setDefinition(e.getDefinition());
                    codeListValue.setDefinitionSource(e.getDefinitionSource());
                    codeListValue.setDeprecated(e.isDeprecated());

                    return codeListValue;
                }).collect(Collectors.toList())
        );

        ModifyCodeListValuesRepositoryResponse response =
                codeListWriteRepository.modifyCodeListValues(request);
    }

    @Transactional
    public void update(AuthenticatedPrincipal user,
                       CodeListRecord codeListRecord, CodeListManifestRecord manifestRecord,
                       List<CodeListValue> codeListValues) {

        Map<ULong, CodeListValueManifestRecord> codeListValueManifestRecordMap = dslContext.selectFrom(CODE_LIST_VALUE_MANIFEST)
                .where(and(
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(manifestRecord.getCodeListManifestId()),
                        CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(manifestRecord.getReleaseId()))
                ).fetchStreamInto(CodeListValueManifestRecord.class)
                .collect(Collectors.toMap(CodeListValueManifestRecord::getCodeListValueManifestId, Function.identity()));

        Map<ULong, CodeListValueRecord> codeListValueRecordMap = dslContext.selectFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.in(
                        codeListValueManifestRecordMap.values().stream().map(e -> e.getCodeListValueId()).collect(Collectors.toList())
                )).fetchStreamInto(CodeListValueRecord.class)
                .collect(Collectors.toMap(CodeListValueRecord::getCodeListValueId, Function.identity()));

        // deletion begins
        Set<ULong> codeListValueManifestIds = codeListValues.stream()
                .filter(e -> e.getCodeListValueManifestId() != null && e.getCodeListValueManifestId().compareTo(BigInteger.ZERO) > 0)
                .map(e -> ULong.valueOf(e.getCodeListValueManifestId()))
                .collect(Collectors.toSet());

        Set<ULong> existingCodeListValueManifestIds = new HashSet(codeListValueManifestRecordMap.keySet()); // deep copy
        existingCodeListValueManifestIds.removeAll(codeListValueManifestIds);

        if (!existingCodeListValueManifestIds.isEmpty()) {
            for (ULong codeListValueManifestId : existingCodeListValueManifestIds) {
                CodeListValueManifestRecord codeListValueManifestRecord =
                        codeListValueManifestRecordMap.get(codeListValueManifestId);
                CodeListValueRecord codeListValueRecord =
                        codeListValueRecordMap.get(codeListValueManifestRecord.getCodeListValueId());

                ULong prevCodeListValueId = codeListValueRecord.getCodeListValueId();

                codeListValueRecord.setCodeListValueId(null);
                codeListValueRecord.setPrevCodeListValueId(prevCodeListValueId);

                ULong requesterId = codeListRecord.getOwnerUserId();
                LocalDateTime timestamp = codeListRecord.getLastUpdateTimestamp();

                if (!codeListValueRecord.getOwnerUserId().equals(requesterId)) {
                    throw new DataAccessForbiddenException("'" + user.getName() +
                            "' doesn't have an access privilege.");
                }

                codeListValueRecord.setLastUpdatedBy(requesterId);
                codeListValueRecord.setLastUpdateTimestamp(timestamp);

                codeListValueRecord = dslContext.insertInto(CODE_LIST_VALUE)
                        .set(codeListValueRecord)
                        .returning().fetchOne();

                dslContext.update(CODE_LIST_VALUE)
                        .set(CODE_LIST_VALUE.NEXT_CODE_LIST_VALUE_ID, codeListValueRecord.getCodeListValueId())
                        .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(prevCodeListValueId))
                        .execute();

                dslContext.deleteFrom(CODE_LIST_VALUE_MANIFEST)
                        .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.eq(codeListValueManifestId))
                        .execute();
            }
        }
        // deletion ends

        // insertion / updating begins
        for (CodeListValue codeListValue : codeListValues) {
            CodeListValueRecord codeListValueRecord;
            BigInteger codeListValueManifestId = codeListValue.getCodeListValueManifestId();
            if (codeListValueManifestId == null || codeListValueManifestId.compareTo(BigInteger.ZERO) <= 0) {
                codeListValueRecord = new CodeListValueRecord();
            } else {
                CodeListValueManifestRecord codeListValueManifestRecord =
                        codeListValueManifestRecordMap.get(ULong.valueOf(codeListValueManifestId));
                if (codeListValueManifestRecord == null) {
                    throw new IllegalArgumentException();
                }
                codeListValueRecord = codeListValueRecordMap.get(
                        codeListValueManifestRecord.getCodeListValueId()
                );
            }

            ULong prevCodeListValueId = codeListValueRecord.getCodeListValueId();
            boolean isUpdate = prevCodeListValueId != null;

            codeListValueRecord.setCodeListValueId(null);
            codeListValueRecord.setPrevCodeListValueId(prevCodeListValueId);

            codeListValueRecord.setCodeListId(codeListRecord.getCodeListId());
            codeListValueRecord.setValue(codeListValue.getValue());
            codeListValueRecord.setMeaning(codeListValue.getMeaning());

            codeListValueRecord.setDefinition(codeListValue.getDefinition());
            codeListValueRecord.setDefinitionSource(codeListValue.getDefinitionSource());

            ULong requesterId = codeListRecord.getOwnerUserId();
            LocalDateTime timestamp = codeListRecord.getLastUpdateTimestamp();

            if (!isUpdate) {
                codeListValueRecord.setCreatedBy(requesterId);
                codeListValueRecord.setCreationTimestamp(timestamp);
            }

            if (codeListValueRecord.getOwnerUserId() != null &&
                    !codeListValueRecord.getOwnerUserId().equals(requesterId)) {
                throw new DataAccessForbiddenException("'" + user.getName() +
                        "' doesn't have an access privilege.");
            } else {
                codeListValueRecord.setOwnerUserId(requesterId);
            }

            codeListValueRecord.setLastUpdatedBy(requesterId);
            codeListValueRecord.setLastUpdateTimestamp(timestamp);

            codeListValueRecord = dslContext.insertInto(CODE_LIST_VALUE)
                    .set(codeListValueRecord)
                    .returning().fetchOne();

            if (isUpdate) {
                dslContext.update(CODE_LIST_VALUE)
                        .set(CODE_LIST_VALUE.NEXT_CODE_LIST_VALUE_ID, codeListValueRecord.getCodeListValueId())
                        .where(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(prevCodeListValueId))
                        .execute();

                dslContext.update(CODE_LIST_VALUE_MANIFEST)
                        .set(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID, codeListValueRecord.getCodeListValueId())
                        .where(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID.eq(ULong.valueOf(codeListValueManifestId)))
                        .execute();
            } else {
                CodeListValueManifestRecord codeListValueManifestRecord = new CodeListValueManifestRecord();

                codeListValueManifestRecord.setCodeListManifestId(manifestRecord.getCodeListManifestId());
                codeListValueManifestRecord.setCodeListValueId(codeListValueRecord.getCodeListValueId());
                codeListValueManifestRecord.setReleaseId(manifestRecord.getReleaseId());

                dslContext.insertInto(CODE_LIST_VALUE_MANIFEST)
                        .set(codeListValueManifestRecord)
                        .execute();
            }
        }
        // insertion / updating ends
    }

    @Transactional
    public void deleteCodeList(AuthenticatedPrincipal user, BigInteger codeListManifestIds) {
        DeleteCodeListRepositoryRequest repositoryRequest =
                new DeleteCodeListRepositoryRequest(user, codeListManifestIds);

        DeleteCodeListRepositoryResponse repositoryResponse =
                codeListWriteRepository.deleteCodeList(repositoryRequest);

        fireEvent(new DeletedCodeListEvent());
    }

    @Transactional
    public void restoreCodeList(AuthenticatedPrincipal user, BigInteger codeListManifestIds) {
        RestoreCodeListRepositoryRequest repositoryRequest =
                new RestoreCodeListRepositoryRequest(user, codeListManifestIds);

        RestoreCodeListRepositoryResponse repositoryResponse =
                codeListWriteRepository.restoreCodeList(repositoryRequest);

        fireEvent(new RestoredCodeListEvent());
    }

    @Transactional
    public void deleteCodeList(AuthenticatedPrincipal user, List<BigInteger> codeListManifestIds) {
        codeListManifestIds.stream().forEach(e -> deleteCodeList(user, e));
    }

    @Transactional
    public void restoreCodeList(AuthenticatedPrincipal user, List<BigInteger> codeListManifestIds) {
        codeListManifestIds.stream().forEach(e -> restoreCodeList(user, e));
    }

    public boolean hasSameCodeList(SameCodeListParams params) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(params.getReleaseId())),
                CODE_LIST.STATE.notEqual(CcState.Deleted.name())
        ));

        if (params.getCodeListManifestId() != null) {
            conditions.add(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.ne(ULong.valueOf(params.getCodeListManifestId())));
        }
        conditions.add(and(
                CODE_LIST.LIST_ID.eq(params.getListId()),
                CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(ULong.valueOf(params.getAgencyIdListValueManifestId())),
                CODE_LIST.VERSION_ID.eq(params.getVersionId())
        ));

        return dslContext.selectCount()
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(conditions).fetchOneInto(Integer.class) > 0;
    }

    public boolean hasSameNameCodeList(SameNameCodeListParams params) {
        List<Condition> conditions = new ArrayList();
        conditions.add(and(
                CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(params.getReleaseId())),
                CODE_LIST.STATE.notEqual(CcState.Deleted.name())
        ));

        if (params.getCodeListManifestId() != null) {
            conditions.add(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.ne(ULong.valueOf(params.getCodeListManifestId())));
        }
        conditions.add(CODE_LIST.NAME.eq(params.getCodeListName()));

        return dslContext.selectCount()
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(conditions).fetchOneInto(Integer.class) > 0;
    }

    @Transactional
    public void transferOwnership(AuthenticatedPrincipal user, BigInteger manifestId, String targetLoginId) {
        AppUser targetUser = sessionService.getAppUserByUsername(targetLoginId);
        if (targetUser == null) {
            throw new IllegalArgumentException("Not found a target user.");
        }

        CodeListManifestRecord codeListManifestRecord = dslContext.selectFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.eq(ULong.valueOf(manifestId))).fetchOne();
        if (codeListManifestRecord == null) {
            throw new IllegalArgumentException("Not found a target CodeList.");
        }
        UpdateCodeListOwnerRepositoryRequest request = new UpdateCodeListOwnerRepositoryRequest(user,
                codeListManifestRecord.getCodeListManifestId().toBigInteger(), targetUser.getAppUserId());
        codeListWriteRepository.updateCodeListOwner(request);
        fireEvent(new UpdateCodeListOwnerEvent());
    }
}
