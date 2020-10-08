package org.oagi.score.gateway.http.api.code_list_management.service;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.entity.jooq.Tables;
import org.oagi.score.gateway.http.api.DataAccessForbiddenException;
import org.oagi.score.gateway.http.api.code_list_management.data.*;
import org.oagi.score.gateway.http.api.common.data.PageRequest;
import org.oagi.score.gateway.http.api.common.data.PageResponse;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.entity.jooq.Tables.*;
import static org.oagi.score.gateway.http.api.code_list_management.data.CodeListState.Editing;
import static org.oagi.score.gateway.http.helper.ScoreJdbcTemplate.newSqlParameterSource;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;

@Service
@Transactional(readOnly = true)
public class CodeListService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private SessionService sessionService;
    private String GET_CODE_LIST_VALUES_STATEMENT =
            "SELECT code_list_value_id, value, name, definition, definition_source, " +
                    "used_indicator as used, locked_indicator as locked, extension_Indicator as extension " +
                    "FROM code_list_value WHERE code_list_id = :code_list_id";

    private SelectOnConditionStep<Record13<
            ULong, String, String, ULong, String,
            String, ULong, String, String, Timestamp,
            String, Byte, String>> getSelectOnConditionStep() {
        return dslContext.select(
                Tables.CODE_LIST.CODE_LIST_ID,
                Tables.CODE_LIST.GUID,
                Tables.CODE_LIST.NAME.as("code_list_name"),
                Tables.CODE_LIST.BASED_CODE_LIST_ID,
                Tables.CODE_LIST.as("based").NAME.as("based_code_list_name"),
                Tables.CODE_LIST.LIST_ID,
                Tables.CODE_LIST.AGENCY_ID,
                Tables.AGENCY_ID_LIST_VALUE.NAME.as("agency_id_name"),
                Tables.CODE_LIST.VERSION_ID,
                Tables.CODE_LIST.LAST_UPDATE_TIMESTAMP,
                APP_USER.LOGIN_ID.as("last_update_user"),
                Tables.CODE_LIST.EXTENSIBLE_INDICATOR.as("extensible"),
                Tables.CODE_LIST.STATE)
                .from(Tables.CODE_LIST)
                .join(APP_USER).on(CODE_LIST.LAST_UPDATED_BY.eq(APP_USER.APP_USER_ID))
                .leftJoin(Tables.CODE_LIST.as("based")).on(Tables.CODE_LIST.BASED_CODE_LIST_ID.eq(Tables.CODE_LIST.as("based").CODE_LIST_ID))
                .leftJoin(Tables.AGENCY_ID_LIST_VALUE).on(Tables.CODE_LIST.AGENCY_ID.eq(Tables.AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID));
    }

    public PageResponse<CodeListForList> getCodeLists(CodeListForListRequest request) {

        SelectOnConditionStep<Record13<
                ULong, String, String, ULong, String,
                String, ULong, String, String, Timestamp,
                String, Byte, String>> step = getSelectOnConditionStep();

        List<Condition> conditions = new ArrayList();
        if (!StringUtils.isEmpty(request.getName())) {
            conditions.addAll(contains(request.getName(), CODE_LIST.NAME));
        }
        if (!request.getStates().isEmpty()) {
            conditions.add(Tables.CODE_LIST.STATE.in(request.getStates()));
        }
        if (request.getExtensible() != null) {
            conditions.add(CODE_LIST.EXTENSIBLE_INDICATOR.eq((byte) (request.getExtensible() ? 1 : 0)));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(Tables.CODE_LIST.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime())));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(Tables.CODE_LIST.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime())));
        }

        SelectConnectByStep<Record13<
                ULong, String, String, ULong, String,
                String, ULong, String, String, Timestamp,
                String, Byte, String>> conditionStep = step;
        if (!conditions.isEmpty()) {
            conditionStep = step.where(conditions);
        }

        PageRequest pageRequest = request.getPageRequest();
        String sortDirection = pageRequest.getSortDirection();
        SortField sortField = null;
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

        SelectWithTiesAfterOffsetStep<Record13<
                ULong, String, String, ULong, String,
                String, ULong, String, String, Timestamp,
                String, Byte, String>> offsetStep = null;
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

        PageResponse<CodeListForList> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(dslContext.selectCount()
                .from(Tables.CODE_LIST)
                .join(APP_USER).on(CODE_LIST.LAST_UPDATED_BY.eq(APP_USER.APP_USER_ID))
                .where(conditions)
                .fetchOptionalInto(Integer.class).orElse(0));

        return response;
    }

    public CodeList getCodeList(long id) {
        MapSqlParameterSource parameterSource = newSqlParameterSource()
                .addValue("code_list_id", id);

        CodeList codeList = dslContext.select(
                CODE_LIST.CODE_LIST_ID,
                CODE_LIST.NAME.as("code_list_name"),
                CODE_LIST.BASED_CODE_LIST_ID,
                CODE_LIST.as("base").NAME.as("based_code_list_name"),
                CODE_LIST.AGENCY_ID,
                AGENCY_ID_LIST_VALUE.NAME.as("agency_id_name"),
                CODE_LIST.VERSION_ID,
                CODE_LIST.GUID,
                CODE_LIST.LIST_ID,
                CODE_LIST.DEFINITION,
                CODE_LIST.DEFINITION_SOURCE,
                CODE_LIST.REMARK,
                CODE_LIST.EXTENSIBLE_INDICATOR.as("extensible"),
                CODE_LIST.STATE)
                .from(CODE_LIST)
                .leftJoin(CODE_LIST.as("base")).on(CODE_LIST.BASED_CODE_LIST_ID.eq(CODE_LIST.as("base").CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_VALUE).on(CODE_LIST.AGENCY_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(id)))
                .fetchOptionalInto(CodeList.class).orElse(null);

        boolean isPublished = CodeListState.Published.name().equals(codeList.getState());
        List<Condition> conditions = new ArrayList();
        conditions.add(CODE_LIST_VALUE.CODE_LIST_ID.eq(ULong.valueOf(id)));
        if (isPublished) {
            conditions.add(CODE_LIST_VALUE.LOCKED_INDICATOR.eq((byte) 0));
        }

        List<CodeListValue> codeListValues = dslContext.select(
                CODE_LIST_VALUE.CODE_LIST_VALUE_ID,
                CODE_LIST_VALUE.VALUE,
                CODE_LIST_VALUE.NAME,
                CODE_LIST_VALUE.DEFINITION,
                CODE_LIST_VALUE.DEFINITION_SOURCE,
                CODE_LIST_VALUE.USED_INDICATOR.as("used"),
                CODE_LIST_VALUE.LOCKED_INDICATOR.as("locked"),
                CODE_LIST_VALUE.EXTENSION_INDICATOR.as("extension"))
                .from(CODE_LIST_VALUE)
                .where(conditions)
                .fetchInto(CodeListValue.class);
        codeList.setCodeListValues(codeListValues);

        return codeList;
    }

    @Transactional
    public void insert(AuthenticatedPrincipal user, CodeList codeList) {
        ULong userId = ULong.valueOf(sessionService.userId(user));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        ULong codeListId = dslContext.insertInto(CODE_LIST,
                CODE_LIST.GUID,
                CODE_LIST.NAME,
                CODE_LIST.LIST_ID,
                CODE_LIST.AGENCY_ID,
                CODE_LIST.VERSION_ID,
                CODE_LIST.REMARK,
                CODE_LIST.DEFINITION,
                CODE_LIST.DEFINITION_SOURCE,
                CODE_LIST.BASED_CODE_LIST_ID,
                CODE_LIST.EXTENSIBLE_INDICATOR,
                CODE_LIST.STATE,
                CODE_LIST.CREATED_BY,
                CODE_LIST.LAST_UPDATED_BY,
                CODE_LIST.CREATION_TIMESTAMP,
                CODE_LIST.LAST_UPDATE_TIMESTAMP).values(
                ScoreGuid.randomGuid(),
                codeList.getCodeListName(),
                codeList.getListId(),
                ULong.valueOf(codeList.getAgencyId()),
                codeList.getVersionId(),
                codeList.getRemark(),
                codeList.getDefinition(),
                codeList.getDefinitionSource(),
                (codeList.getBasedCodeListId() != null) ? ULong.valueOf(codeList.getBasedCodeListId()) : null,
                (byte) ((codeList.isExtensible()) ? 1 : 0),
                Editing.name(),
                userId, userId, timestamp, timestamp)
                .returning(CODE_LIST.CODE_LIST_ID).fetchOne().getValue(CODE_LIST.CODE_LIST_ID);
        for (CodeListValue codeListValue : codeList.getCodeListValues()) {
            insert(codeListId.longValue(), codeListValue);
        }
    }

    private void insert(long codeListId, CodeListValue codeListValue) {

        boolean locked = codeListValue.isLocked();
        boolean used = codeListValue.isUsed();
        boolean extension = codeListValue.isExtension();
        if (locked) {
            used = false;
            extension = false;
        }

        dslContext.insertInto(CODE_LIST_VALUE,
                CODE_LIST_VALUE.CODE_LIST_ID,
                CODE_LIST_VALUE.VALUE,
                CODE_LIST_VALUE.NAME,
                CODE_LIST_VALUE.DEFINITION,
                CODE_LIST_VALUE.DEFINITION_SOURCE,
                CODE_LIST_VALUE.USED_INDICATOR,
                CODE_LIST_VALUE.LOCKED_INDICATOR,
                CODE_LIST_VALUE.EXTENSION_INDICATOR).values(
                ULong.valueOf(codeListId),
                codeListValue.getValue(),
                codeListValue.getName(),
                codeListValue.getDefinition(),
                codeListValue.getDefinitionSource(),
                (byte) ((used) ? 1 : 0),
                (byte) ((locked) ? 1 : 0),
                (byte) ((extension) ? 1 : 0))
                .execute();
    }

    @Transactional
    public void update(AuthenticatedPrincipal user, CodeList codeList) {
        dslContext.update(CODE_LIST)
                .set(CODE_LIST.NAME, codeList.getCodeListName())
                .set(CODE_LIST.LIST_ID, codeList.getListId())
                .set(CODE_LIST.AGENCY_ID, ULong.valueOf(codeList.getAgencyId()))
                .set(CODE_LIST.VERSION_ID, codeList.getVersionId())
                .set(CODE_LIST.DEFINITION, codeList.getDefinition())
                .set(CODE_LIST.REMARK, codeList.getRemark())
                .set(CODE_LIST.DEFINITION_SOURCE, codeList.getDefinitionSource())
                .set(CODE_LIST.EXTENSIBLE_INDICATOR, (byte) ((codeList.isExtensible()) ? 1 : 0))
                .set(CODE_LIST.LAST_UPDATED_BY, ULong.valueOf(sessionService.userId(user)))
                .set(CODE_LIST.LAST_UPDATE_TIMESTAMP, new Timestamp(System.currentTimeMillis()))
                .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(codeList.getCodeListId())))
                .execute();

        String state = codeList.getState();
        List<CodeListValue> codeListValues = codeList.getCodeListValues();
        if (CodeListState.Published.name().equals(state)) {
            codeListValues.stream().forEach(e -> {
                if (!e.isUsed()) {
                    e.setLocked(true);
                }
            });
        }

        update(codeList.getCodeListId(), codeListValues);

        if (!StringUtils.isEmpty(state)) {
            dslContext.update(CODE_LIST)
                    .set(CODE_LIST.STATE, state)
                    .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(codeList.getCodeListId())))
                    .execute();
        }
    }

    @Transactional
    public void update(long codeListId, List<CodeListValue> codeListValues) {
        List<Long> oldCodeListValueIds = dslContext.select(CODE_LIST_VALUE.CODE_LIST_VALUE_ID)
                .from(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_ID.eq(ULong.valueOf(codeListId)))
                .fetchInto(Long.class);

        Map<Long, CodeListValue> newCodeListValues = codeListValues.stream()
                .filter(e -> e.getCodeListValueId() > 0L)
                .collect(Collectors.toMap(CodeListValue::getCodeListValueId, Function.identity()));

        oldCodeListValueIds.removeAll(newCodeListValues.keySet());
        for (long deleteCodeListValueId : oldCodeListValueIds) {
            delete(codeListId, deleteCodeListValueId);
        }

        for (CodeListValue CodeListValue : newCodeListValues.values()) {
            update(codeListId, CodeListValue);
        }

        for (CodeListValue CodeListValue : codeListValues.stream()
                .filter(e -> e.getCodeListValueId() == 0L)
                .collect(Collectors.toList())) {
            insert(codeListId, CodeListValue);
        }
    }

    @Transactional
    public void update(long codeListId, CodeListValue codeListValue) {
        boolean locked = codeListValue.isLocked();
        boolean used = codeListValue.isUsed();
        boolean extension = codeListValue.isExtension();
        if (locked) {
            used = false;
            extension = false;
        }

        dslContext.update(CODE_LIST_VALUE)
                .set(CODE_LIST_VALUE.VALUE, codeListValue.getValue())
                .set(CODE_LIST_VALUE.NAME, codeListValue.getName())
                .set(CODE_LIST_VALUE.DEFINITION, codeListValue.getDefinition())
                .set(CODE_LIST_VALUE.DEFINITION_SOURCE, codeListValue.getDefinitionSource())
                .set(CODE_LIST_VALUE.USED_INDICATOR, (byte) ((used) ? 1 : 0))
                .set(CODE_LIST_VALUE.LOCKED_INDICATOR, (byte) ((locked) ? 1 : 0))
                .set(CODE_LIST_VALUE.EXTENSION_INDICATOR, (byte) ((extension) ? 1 : 0))
                .where(and(
                        CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(ULong.valueOf(codeListValue.getCodeListValueId())),
                        CODE_LIST_VALUE.CODE_LIST_ID.eq(ULong.valueOf(codeListId))
                ))
                .execute();
    }

    @Transactional
    public void delete(long codeListId, long codeListValueId) {
        ensureProperDeleteCodeListRequest(ULong.valueOf(codeListId));

        dslContext.deleteFrom(CODE_LIST_VALUE)
                .where(and(
                        CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(ULong.valueOf(codeListValueId)),
                        CODE_LIST_VALUE.CODE_LIST_ID.eq(ULong.valueOf(codeListId))
                ))
                .execute();
    }

    @Transactional
    public void delete(long codeListId) {
        ensureProperDeleteCodeListRequest(ULong.valueOf(codeListId));

        dslContext.deleteFrom(CODE_LIST_VALUE)
                .where(CODE_LIST_VALUE.CODE_LIST_ID.eq(ULong.valueOf(codeListId)))
                .execute();

        dslContext.deleteFrom(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(ULong.valueOf(codeListId)))
                .execute();
    }

    @Transactional
    public void delete(List<Long> codeListIds) {
        codeListIds.stream().forEach(e -> delete(e));
    }

    private void ensureProperDeleteCodeListRequest(ULong codeListId) {
        String state = dslContext.select(CODE_LIST.STATE)
                .from(CODE_LIST)
                .where(CODE_LIST.CODE_LIST_ID.eq(codeListId))
                .fetchOptionalInto(String.class).orElse(null);

        if (state == null) {
            throw new IllegalArgumentException();
        }
        CodeListState codeListState = CodeListState.valueOf(state);
        if (Editing != codeListState) {
            throw new DataAccessForbiddenException("Not allowed to delete the code list in '" + codeListState + "' state.");
        }
    }
}
