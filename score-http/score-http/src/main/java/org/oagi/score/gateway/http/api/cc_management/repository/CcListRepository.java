package org.oagi.score.gateway.http.api.cc_management.repository;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.data.Release;
import org.oagi.score.gateway.http.api.cc_management.data.CcId;
import org.oagi.score.gateway.http.api.cc_management.data.CcList;
import org.oagi.score.gateway.http.api.cc_management.data.CcListRequest;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder;
import org.oagi.score.repo.api.impl.jooq.entity.tables.AppUser;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetReleaseRecord;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.service.common.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.stripToNull;
import static org.jooq.impl.DSL.*;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.service.common.data.OagisComponentType.*;

@Repository
public class CcListRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ReleaseRepository releaseRepository;

    public PageResponse<CcList> getCcList(CcListRequest request) {
        Release release = releaseRepository.findById(request.getReleaseId());

        ULong defaultModuleSetReleaseId = null;
        ModuleSetReleaseRecord defaultModuleSetRelease = dslContext.selectFrom(MODULE_SET_RELEASE)
                .where(and(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1), MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .fetchOne();
        if (defaultModuleSetRelease != null) {
            defaultModuleSetReleaseId = defaultModuleSetRelease.getModuleSetReleaseId();
        }

        SelectOrderByStep select = null;
        if (request.getTypes().isAcc()) {
            select = (select != null) ? select.union(getAccList(request, release, defaultModuleSetReleaseId)) : getAccList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isAsccp()) {
            select = (select != null) ? select.union(getAsccpList(request, release, defaultModuleSetReleaseId)) : getAsccpList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isBccp()) {
            select = (select != null) ? select.union(getBccpList(request, release, defaultModuleSetReleaseId)) : getBccpList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isAscc()) {
            select = (select != null) ? select.union(getAsccList(request, release, defaultModuleSetReleaseId)) : getAsccList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isBcc()) {
            select = (select != null) ? select.union(getBccList(request, release, defaultModuleSetReleaseId)) : getBccList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isBdt()) {
            select = (select != null) ? select.union(getBdtList(request, release, defaultModuleSetReleaseId)) : getBdtList(request, release, defaultModuleSetReleaseId);
        }

        if (select == null) {
            PageResponse response = new PageResponse();
            response.setList(Collections.emptyList());
            response.setPage(request.getPageRequest().getPageIndex());
            response.setSize(request.getPageRequest().getPageSize());
            response.setLength(0);
            return response;
        }

        PageRequest pageRequest = request.getPageRequest();
        Field field = null;
        switch (pageRequest.getSortActive()) {
            case "type":
                field = field("type");
                break;
            case "state":
                field = field("state");
                break;
            case "den":
                field = field("den");
                break;
            case "revision":
                field = field(LOG.REVISION_NUM);
                break;
            case "owner":
                field = field("owner");
                break;
            case "module":
                field = field(MODULE.PATH);
                break;
            case "lastUpdateTimestamp":
                field = field("last_update_timestamp");
                break;
        }

        SortField sortField = null;
        if (field != null && pageRequest.getSortDirection() != null) {
            switch (pageRequest.getSortDirection()) {
                case "asc":
                    sortField = field.asc();
                    break;
                case "desc":
                    sortField = field.desc();
                    break;
            }
        }

        int count = dslContext.fetchCount(select);

        SelectWithTiesAfterOffsetStep offsetStep = null;
        if (sortField != null) {
            offsetStep = select.orderBy(sortField)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = select
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<CcList> result = ((offsetStep != null) ? offsetStep.fetch() : select.fetch()).map(row -> {
            CcList ccList = new CcList();
            ccList.setType(CcType.valueOf(row.getValue("type", String.class)));
            ccList.setManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
            ccList.setId(row.getValue("id", ULong.class).toBigInteger());
            ccList.setGuid(row.getValue("guid", String.class));
            ccList.setDen(row.getValue("den", String.class));
            ccList.setDefinition(stripToNull(row.getValue("definition", String.class)));
            ccList.setDefinitionSource(stripToNull(row.getValue("definition_source", String.class)));
            ccList.setModule(row.getValue("module_path", String.class));
            ccList.setName(row.getValue("term", String.class));
            Integer componentType = row.getValue("oagis_component_type", Integer.class);
            if (componentType != null) {
                ccList.setOagisComponentType(OagisComponentType.valueOf(componentType));
            }
            ccList.setDtType(row.getValue("dt_type", String.class));
            ccList.setState(CcState.valueOf(row.getValue("state", String.class)));
            ccList.setDeprecated(row.getValue("is_deprecated", Byte.class) == 1);
            ccList.setLastUpdateTimestamp(Date.from(row.getValue("last_update_timestamp", LocalDateTime.class)
                    .atZone(ZoneId.systemDefault()).toInstant()));
            ccList.setOwner((String) row.getValue("owner"));
            ccList.setOwnedByDeveloper(row.getValue("owned_by_developer", Byte.class) == 1);
            ccList.setLastUpdateUser((String) row.getValue("last_update_user"));
            ccList.setRevision(row.getValue(LOG.REVISION_NUM).toString());
            ccList.setReleaseNum(row.getValue(RELEASE.RELEASE_NUM));
            return ccList;
        });

        PageResponse<CcList> response = new PageResponse();
        response.setList(result);
        response.setPage(pageRequest.getPageIndex());
        response.setSize(pageRequest.getPageSize());
        response.setLength(count);

        return response;
    }

    private BigInteger getManifestIdByObjectClassTermAndReleaseId(String objectClassTerm, BigInteger releaseId) {
        return dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(
                        ACC.OBJECT_CLASS_TERM.eq(objectClassTerm),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))
                ))
                .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
    }

    private List<BigInteger> getManifestIdsByBasedAccManifestIdAndReleaseId(
            List<BigInteger> basedManifestIds, BigInteger releaseId) {
        return dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .where(and(
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID.in(basedManifestIds),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))
                ))
                .fetchInto(BigInteger.class);
    }

    private SelectOrderByStep getAccList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        if ("Working".equals(release.getReleaseNum())) {
            conditions.add(ACC.OAGIS_COMPONENT_TYPE.notEqual(OagisComponentType.UserExtensionGroup.getValue()));
        }
        if (request.getDeprecated() != null) {
            conditions.add(ACC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getStates() != null) {
            conditions.add(ACC.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(ACC_MANIFEST.ACC_MANIFEST_ID.notIn(request.getExcludes()));
        }
        if (StringUtils.hasLength(request.getDen())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), ACC.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), ACC.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(ACC.LAST_UPDATE_TIMESTAMP.greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(ACC.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }
        if (request.getComponentTypes() != null && !request.getComponentTypes().isEmpty()) {
            List<OagisComponentType> componentTypes = Arrays.asList(request.getComponentTypes().split(","))
                    .stream()
                    .map(e -> OagisComponentType.valueOf(Integer.parseInt(e)))
                    .collect(Collectors.toList());

            List<OagisComponentType> usualComponentTypes = componentTypes.stream()
                    .filter(e -> !Arrays.asList(BOD, Verb, Noun).contains(e))
                    .collect(Collectors.toList());

            if (!usualComponentTypes.isEmpty()) {
                conditions.add(ACC.OAGIS_COMPONENT_TYPE.in(usualComponentTypes.stream()
                        .map(e -> e.getValue()).collect(Collectors.toList())));
            }

            List<OagisComponentType> unusualComponentTypes = componentTypes.stream()
                    .filter(e -> Arrays.asList(BOD, Verb, Noun).contains(e))
                    .collect(Collectors.toList());

            if (!unusualComponentTypes.isEmpty()) {
                for (OagisComponentType unusualComponentType : unusualComponentTypes) {
                    switch (unusualComponentType) {
                        case BOD:
                            BigInteger bodBasedAccManifestId = getManifestIdByObjectClassTermAndReleaseId(
                                    "Business Object Document", request.getReleaseId());
                            conditions.add(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(ULong.valueOf(bodBasedAccManifestId)));
                            break;

                        case Verb:
                            BigInteger verbAccManifestId = getManifestIdByObjectClassTermAndReleaseId(
                                    "Verb", request.getReleaseId());

                            Set<BigInteger> verbManifestIds = new HashSet();
                            verbManifestIds.add(verbAccManifestId);

                            List<BigInteger> basedAccManifestIds = new ArrayList();
                            basedAccManifestIds.add(verbAccManifestId);

                            while (!basedAccManifestIds.isEmpty()) {
                                basedAccManifestIds = getManifestIdsByBasedAccManifestIdAndReleaseId(
                                        basedAccManifestIds, request.getReleaseId());
                                verbManifestIds.addAll(basedAccManifestIds);
                            }

                            conditions.add(ACC_MANIFEST.ACC_MANIFEST_ID.in(verbManifestIds.stream()
                                    .map(e -> ULong.valueOf(e)).collect(Collectors.toList())));

                            break;

                        case Noun:
                            // TODO:

                            break;
                    }
                }
            }
        }

        return dslContext.select(
                inline("ACC").as("type"),
                ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
                ACC.ACC_ID.as("id"),
                ACC.GUID,
                ACC.DEN,
                ACC.DEFINITION,
                ACC.DEFINITION_SOURCE,
                ACC.OBJECT_CLASS_TERM.as("term"),
                ACC.OAGIS_COMPONENT_TYPE.as("oagis_component_type"),
                val((String) null).as("dt_type"),
                ACC.STATE,
                ACC.IS_DEPRECATED,
                ACC.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM)
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID).and(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(ACC.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(ACC.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(MODULE_ACC_MANIFEST)
                .on(and(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID), MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getAsccList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(ASCC.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(ASCC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getStates() != null) {
            conditions.add(ASCC.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(ASCC_MANIFEST.ASCC_MANIFEST_ID.notIn(request.getExcludes()));
        }
        if (StringUtils.hasLength(request.getDen())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), ASCC.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), ASCC.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(ASCC.LAST_UPDATE_TIMESTAMP.greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(ASCC.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        return dslContext.select(
                inline("ASCC").as("type"),
                ASCC_MANIFEST.ASCC_MANIFEST_ID.as("manifest_id"),
                ASCC.ASCC_ID.as("id"),
                ASCC.GUID,
                ASCC.DEN,
                ASCC.DEFINITION,
                ASCC.DEFINITION_SOURCE,
                val((String) null).as("term"),
                val((Integer) null).as("oagis_component_type"),
                val((String) null).as("dt_type"),
                ACC.STATE,
                ASCC.IS_DEPRECATED,
                ASCC.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM)
                .from(ASCC)
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID).and(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(ACC_MANIFEST)
                .on(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)
                ))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(ACC.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(ASCC.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(MODULE_ACC_MANIFEST)
                .on(and(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID), MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getBccList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(BCC.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(BCC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getStates() != null) {
            conditions.add(BCC.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(BCC_MANIFEST.BCC_MANIFEST_ID.notIn(request.getExcludes()));
        }
        if (StringUtils.hasLength(request.getDen())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), BCC.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), BCC.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BCC.LAST_UPDATE_TIMESTAMP.greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BCC.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        return dslContext.select(
                inline("BCC").as("type"),
                BCC_MANIFEST.BCC_MANIFEST_ID.as("manifest_id"),
                BCC.BCC_ID.as("id"),
                BCC.GUID,
                BCC.DEN,
                BCC.DEFINITION,
                BCC.DEFINITION_SOURCE,
                val((String) null).as("term"),
                val((Integer) null).as("oagis_component_type"),
                val((String) null).as("dt_type"),
                ACC.STATE,
                BCC.IS_DEPRECATED,
                BCC.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM)
                .from(BCC)
                .join(BCC_MANIFEST)
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID).and(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(ACC_MANIFEST)
                .on(and(
                        BCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)
                ))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(ACC.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(BCC.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(MODULE_ACC_MANIFEST)
                .on(and(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID), MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getAsccpList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(ASCCP.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(ASCCP.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getStates() != null) {
            conditions.add(ASCCP.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.notIn(request.getExcludes()));
        }
        if (StringUtils.hasLength(request.getDen())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), ASCCP.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), ASCCP.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(ASCCP.LAST_UPDATE_TIMESTAMP.greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(ASCCP.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }
        if (request.getAsccpTypes().size() != 0) {
            conditions.add(ASCCP.TYPE.in(request.getAsccpTypes()));
        }
        if (request.getIsBIEUsable() != null && request.getIsBIEUsable()) {
            conditions.add(ACC.OAGIS_COMPONENT_TYPE.notIn(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue())));
        }

        return dslContext.select(
                inline("ASCCP").as("type"),
                ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifest_id"),
                ASCCP.ASCCP_ID.as("id"),
                ASCCP.GUID,
                ASCCP.DEN,
                ASCCP.DEFINITION,
                ASCCP.DEFINITION_SOURCE,
                ASCCP.PROPERTY_TERM.as("term"),
                val((Integer) null).as("oagis_component_type"),
                val((String) null).as("dt_type"),
                ASCCP.STATE,
                ASCCP.IS_DEPRECATED,
                ASCCP.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM)
                .from(ASCCP)
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID).and(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(ACC_MANIFEST)
                .on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID))
                .join(ACC)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(LOG)
                .on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(ASCCP.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(ASCCP.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(MODULE_ASCCP_MANIFEST)
                .on(and(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID), MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getBccpList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(BCCP.DEN.notContains("User Extension Group"));
        if (request.getDeprecated() != null) {
            conditions.add(BCCP.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getStates() != null) {
            conditions.add(BCCP.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(BCCP_MANIFEST.BCCP_MANIFEST_ID.notIn(request.getExcludes()));
        }
        if (StringUtils.hasLength(request.getDen())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), BCCP.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), BCCP.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BCCP.LAST_UPDATE_TIMESTAMP.greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BCCP.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        return dslContext.select(
                inline("BCCP").as("type"),
                BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifest_id"),
                BCCP.BCCP_ID.as("id"),
                BCCP.GUID,
                BCCP.DEN,
                BCCP.DEFINITION,
                BCCP.DEFINITION_SOURCE,
                BCCP.PROPERTY_TERM.as("term"),
                val((Integer) null).as("oagis_component_type"),
                val((String) null).as("dt_type"),
                BCCP.STATE,
                BCCP.IS_DEPRECATED,
                BCCP.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM)
                .from(BCCP)
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID).and(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(LOG)
                .on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(BCCP.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(BCCP.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(MODULE_BCCP_MANIFEST)
                .on(and(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID), MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    public SelectOrderByStep getBdtList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        if (request.getDeprecated() != null) {
            conditions.add(DT.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getStates() != null) {
            conditions.add(DT.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIds().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (!request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        if (!request.getExcludes().isEmpty()) {
            conditions.add(DT.DT_ID.notIn(request.getExcludes()));
        }
        if (StringUtils.hasLength(request.getDen())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), DT.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), DT.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (!request.getDtTypes().isEmpty()) {
            if (request.getDtTypes().size() == 1) {
                conditions.add(DT.TYPE.eq(request.getDtTypes().get(0)));
            } else {
                conditions.add(DT.TYPE.in(request.getDtTypes()));
            }
        } else {
            conditions.add(DT.TYPE.notEqual(DTType.Core.toString()));
        }
        if (request.getCommonlyUsed() != null) {
            conditions.add(DT.COMMONLY_USED.eq((byte) (request.getCommonlyUsed() ? 1 : 0)));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(DT.LAST_UPDATE_TIMESTAMP.greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(DT.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        return dslContext.select(
                inline("BDT").as("type"),
                DT_MANIFEST.DT_MANIFEST_ID.as("manifest_id"),
                DT.DT_ID.as("id"),
                DT.GUID,
                DT.DEN,
                DT.DEFINITION,
                DT.DEFINITION_SOURCE,
                DT.DATA_TYPE_TERM.as("term"),
                val((Integer) null).as("oagis_component_type"),
                DT.TYPE.as("dt_type"),
                DT.STATE,
                DT.IS_DEPRECATED,
                DT.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM)
                .from(DT)
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID).and(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(DT.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(DT.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(MODULE_DT_MANIFEST)
                .on(and(DT_MANIFEST.DT_MANIFEST_ID.eq(MODULE_DT_MANIFEST.DT_MANIFEST_ID), MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }
}
