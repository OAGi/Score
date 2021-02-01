package org.oagi.score.repo.component.module;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.service.common.data.AppUser;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
import org.oagi.score.gateway.http.api.module_management.data.ModuleSet;
import org.oagi.score.gateway.http.api.module_management.data.ModuleSetListRequest;
import org.oagi.score.gateway.http.api.module_management.data.ModuleSetModule;
import org.oagi.score.gateway.http.api.module_management.data.ModuleSetModuleListRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Repository
public class ModuleSetReadRepository {

    @Autowired
    private DSLContext dslContext;

    private SelectOnConditionStep<Record6<
            ULong, String, String, String, LocalDateTime,
            String>> getSelectOnConditionStepForModuleSetList() {
        return dslContext.select(MODULE_SET.MODULE_SET_ID, MODULE_SET.GUID, MODULE_SET.NAME,
                MODULE_SET.DESCRIPTION, MODULE_SET.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("updater").LOGIN_ID.as("last_update_user"))
                .from(MODULE_SET)
                .join(APP_USER.as("updater"))
                .on(APP_USER.as("updater").APP_USER_ID.eq(MODULE_SET.LAST_UPDATED_BY));
    }

    public PageResponse<ModuleSet> fetch(AppUser requester, ModuleSetListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        SelectOnConditionStep<Record6<
                ULong, String, String, String, LocalDateTime,
                String>> selectOnConditionStep = getSelectOnConditionStepForModuleSetList();

        List<Condition> conditions = new ArrayList();
        if (StringUtils.hasLength(request.getName())) {
            conditions.add(MODULE_SET.NAME.containsIgnoreCase(request.getName()));
        }
        if (StringUtils.hasLength(request.getDescription())) {
            conditions.add(MODULE_SET.DESCRIPTION.containsIgnoreCase(request.getDescription()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                    new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(MODULE_SET.LAST_UPDATE_TIMESTAMP.lessThan(
                    new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        SelectConditionStep<Record6<
                ULong, String, String, String, LocalDateTime,
                String>> conditionStep = selectOnConditionStep.where(conditions);

        int length = dslContext.fetchCount(conditionStep);

        SortField sortField = null;
        if (StringUtils.hasLength(pageRequest.getSortActive())) {
            Field field = null;
            switch (pageRequest.getSortActive()) {
                case "name":
                    field = MODULE_SET.NAME;
                    break;

                case "lastUpdateTimestamp":
                    field = MODULE_SET.LAST_UPDATE_TIMESTAMP;
                    break;
            }

            if (field != null) {
                if ("asc".equalsIgnoreCase(pageRequest.getSortDirection())) {
                    sortField = field.asc();
                } else if ("desc".equalsIgnoreCase(pageRequest.getSortDirection())) {
                    sortField = field.desc();
                }
            }
        }

        ResultQuery<Record6<
                ULong, String, String, String, LocalDateTime,
                String>> query;
        if (sortField != null) {
            if (pageRequest.getOffset() >= 0 && pageRequest.getPageSize() >= 0) {
                query = conditionStep.orderBy(sortField)
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            } else {
                query = conditionStep.orderBy(sortField);
            }
        } else {
            if (pageRequest.getOffset() >= 0 && pageRequest.getPageSize() >= 0) {
                query = conditionStep.limit(pageRequest.getOffset(), pageRequest.getPageSize());
            } else {
                query = conditionStep;
            }
        }

        List<ModuleSet> results = query.fetchStream().map(record -> {
            ModuleSet moduleSet = new ModuleSet();
            moduleSet.setModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger());
            moduleSet.setGuid(record.get(MODULE_SET.GUID));
            moduleSet.setName(record.get(MODULE_SET.NAME));
            moduleSet.setDescription(record.get(MODULE_SET.DESCRIPTION));
            moduleSet.setLastUpdateTimestamp(Date.from(record.get(MODULE_SET.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            moduleSet.setLastUpdateUser(record.get(APP_USER.as("updater").LOGIN_ID.as("last_update_user")));
            return moduleSet;
        }).collect(Collectors.toList());

        PageResponse<ModuleSet> response = new PageResponse();
        response.setList(results);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(length);

        return response;
    }

    public ModuleSet getModuleSet(BigInteger moduleSetId) {
        Record record = getSelectOnConditionStepForModuleSetList()
                .where(MODULE_SET.MODULE_SET_ID.eq(ULong.valueOf(moduleSetId)))
                .fetchOptional().orElse(null);
        if (record == null) {
            return null;
        }

        ModuleSet moduleSet = new ModuleSet();
        moduleSet.setModuleSetId(record.get(MODULE_SET.MODULE_SET_ID).toBigInteger());
        moduleSet.setGuid(record.get(MODULE_SET.GUID));
        moduleSet.setName(record.get(MODULE_SET.NAME));
        moduleSet.setDescription(record.get(MODULE_SET.DESCRIPTION));
        moduleSet.setLastUpdateTimestamp(Date.from(record.get(MODULE_SET.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
        moduleSet.setLastUpdateUser(record.get(APP_USER.as("updater").LOGIN_ID.as("last_update_user")));
        return moduleSet;
    }

    private SelectOnConditionStep<Record7<
            ULong, String, ULong, String, LocalDateTime,
            String, ULong>> getSelectOnConditionStepForModuleSetModuleList() {
        return dslContext.select(
                MODULE.MODULE_ID,
                concat(MODULE_DIR.PATH, inline("/"), MODULE.NAME).as("path"),
                NAMESPACE.NAMESPACE_ID,
                NAMESPACE.URI,
                MODULE.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID)
                .from(MODULE)
                .join(MODULE_DIR)
                .on(MODULE.MODULE_DIR_ID.eq(MODULE_DIR.MODULE_DIR_ID))
                .join(NAMESPACE)
                .on(MODULE.NAMESPACE_ID.eq(NAMESPACE.NAMESPACE_ID))
                .join(APP_USER.as("updater"))
                .on(APP_USER.as("updater").APP_USER_ID.eq(MODULE.LAST_UPDATED_BY))
                .leftJoin(MODULE_SET_ASSIGNMENT)
                .on(MODULE.MODULE_ID.eq(MODULE_SET_ASSIGNMENT.MODULE_ID));
    }

    public PageResponse<ModuleSetModule> fetch(AppUser requester, ModuleSetModuleListRequest request) {
        PageRequest pageRequest = request.getPageRequest();
        SelectOnConditionStep<Record7<
                ULong, String, ULong, String, LocalDateTime,
                String, ULong>> selectOnConditionStep = getSelectOnConditionStepForModuleSetModuleList();

        List<Condition> conditions = new ArrayList();
        conditions.add(MODULE_SET_ASSIGNMENT.MODULE_SET_ID.eq(ULong.valueOf(request.getModuleSetId())));
        if (StringUtils.hasLength(request.getPath())) {
            conditions.add(field("path").containsIgnoreCase(request.getPath()));
        }
        if (StringUtils.hasLength(request.getNamespaceUri())) {
            conditions.add(NAMESPACE.URI.containsIgnoreCase(request.getNamespaceUri()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(MODULE.LAST_UPDATE_TIMESTAMP.greaterOrEqual(
                    new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(MODULE.LAST_UPDATE_TIMESTAMP.lessThan(
                    new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        SelectConditionStep<Record7<
                ULong, String, ULong, String, LocalDateTime,
                String, ULong>> conditionStep = selectOnConditionStep.where(conditions);

        int length = dslContext.fetchCount(conditionStep);

        SortField sortField = null;
        if (StringUtils.hasLength(pageRequest.getSortActive())) {
            Field field = null;
            switch (pageRequest.getSortActive()) {
                case "path":
                    field = field("path");
                    break;

                case "namespaceUri":
                    field = NAMESPACE.URI;
                    break;

                case "assigned":
                    field = MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID;
                    break;

                case "lastUpdateTimestamp":
                    field = MODULE.LAST_UPDATE_TIMESTAMP;
                    break;
            }

            if (field != null) {
                if ("asc".equalsIgnoreCase(pageRequest.getSortDirection())) {
                    sortField = field.asc();
                } else if ("desc".equalsIgnoreCase(pageRequest.getSortDirection())) {
                    sortField = field.desc();
                }
            }
        }

        ResultQuery<Record7<
                ULong, String, ULong, String, LocalDateTime,
                String, ULong>> query;
        if (sortField != null) {
            if (pageRequest.getOffset() >= 0 && pageRequest.getPageSize() >= 0) {
                query = conditionStep.orderBy(sortField)
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            } else {
                query = conditionStep.orderBy(sortField);
            }
        } else {
            if (pageRequest.getOffset() >= 0 && pageRequest.getPageSize() >= 0) {
                query = conditionStep.limit(pageRequest.getOffset(), pageRequest.getPageSize());
            } else {
                query = conditionStep;
            }
        }

        List<ModuleSetModule> results = query.fetchStream().map(record -> {
            ModuleSetModule moduleSetModule = new ModuleSetModule();
            moduleSetModule.setModuleId(record.get(MODULE.MODULE_ID).toBigInteger());
            moduleSetModule.setPath(record.value2());
            moduleSetModule.setNamespaceId(record.get(NAMESPACE.NAMESPACE_ID).toBigInteger());
            moduleSetModule.setNamespaceUri(record.get(NAMESPACE.URI));
            moduleSetModule.setLastUpdateTimestamp(Date.from(record.get(MODULE_SET.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            moduleSetModule.setLastUpdateUser(record.get(APP_USER.as("updater").LOGIN_ID.as("last_update_user")));
            moduleSetModule.setAssigned(record.get(MODULE_SET_ASSIGNMENT.MODULE_SET_ASSIGNMENT_ID) != null);
            return moduleSetModule;
        }).collect(Collectors.toList());

        PageResponse<ModuleSetModule> response = new PageResponse();
        response.setList(results);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(length);

        return response;
    }
}
