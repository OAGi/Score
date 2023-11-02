package org.oagi.score.gateway.http.api.cc_management.repository;

import org.jooq.Record;
import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.data.Release;
import org.oagi.score.gateway.http.api.cc_management.data.CcChangesResponse;
import org.oagi.score.gateway.http.api.cc_management.data.CcList;
import org.oagi.score.gateway.http.api.cc_management.data.CcListRequest;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.api.tag_management.data.ShortTag;
import org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder;
import org.oagi.score.repo.api.impl.jooq.entity.tables.AppUser;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.ModuleSetReleaseRecord;
import org.oagi.score.repo.api.user.model.ScoreUser;
import org.oagi.score.repo.component.release.ReleaseRepository;
import org.oagi.score.service.common.data.CcState;
import org.oagi.score.service.common.data.OagisComponentType;
import org.oagi.score.service.common.data.PageRequest;
import org.oagi.score.service.common.data.PageResponse;
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
import static org.oagi.score.repo.api.impl.jooq.entity.Routines.levenshtein;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;
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
                .where(and(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1),
                        MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .fetchOne();
        if (defaultModuleSetRelease != null) {
            defaultModuleSetReleaseId = defaultModuleSetRelease.getModuleSetReleaseId();
        }

        SelectOrderByStep select = null;
        if (request.getTypes().isAcc()) {
            select = (select != null) ? select.union(getAccList(request, release, defaultModuleSetReleaseId))
                    : getAccList(request, release, defaultModuleSetReleaseId);
        }
        // Component Types are only allowed by ACC.
        if (request.getTypes().isAsccp() && !hasLength(request.getComponentTypes())) {
            select = (select != null) ? select.union(getAsccpList(request, release, defaultModuleSetReleaseId))
                    : getAsccpList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isBccp() && !hasLength(request.getComponentTypes())) {
            select = (select != null) ? select.union(getBccpList(request, release, defaultModuleSetReleaseId))
                    : getBccpList(request, release, defaultModuleSetReleaseId);
        }
        // Tags/Namespaces are only allowed by ACC, ASCCP, BCCP, and DT.
        if (request.getTypes().isAscc() && !hasLength(request.getComponentTypes()) && request.getTags().isEmpty()
                && request.getNamespaces().isEmpty()) {
            select = (select != null) ? select.union(getAsccList(request, release, defaultModuleSetReleaseId))
                    : getAsccList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isBcc() && !hasLength(request.getComponentTypes()) && request.getTags().isEmpty()
                && request.getNamespaces().isEmpty()) {
            select = (select != null) ? select.union(getBccList(request, release, defaultModuleSetReleaseId))
                    : getBccList(request, release, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isDt() && !hasLength(request.getComponentTypes())) {
            select = (select != null) ? select.union(getDtList(request, release, defaultModuleSetReleaseId))
                    : getDtList(request, release, defaultModuleSetReleaseId);
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
                field = field("module_path");
                break;
            case "lastUpdateTimestamp":
                field = field("last_update_timestamp");
                break;
        }

        List<SortField> sortFields = new ArrayList<>();
        if (field != null && pageRequest.getSortDirection() != null) {
            switch (pageRequest.getSortDirection()) {
                case "asc":
                    sortFields.add(field.asc());
                    break;
                case "desc":
                    sortFields.add(field.desc());
                    break;
            }
        }
        if (StringUtils.hasLength(request.getDen())) {
            sortFields.add(field("score").desc());
        }

        int count = dslContext.fetchCount(select);

        SelectWithTiesAfterOffsetStep offsetStep = null;
        if (!sortFields.isEmpty()) {
            offsetStep = select.orderBy(sortFields)
                    .limit(pageRequest.getOffset(), pageRequest.getPageSize());
        } else {
            if (pageRequest.getPageIndex() >= 0 && pageRequest.getPageSize() > 0) {
                offsetStep = select
                        .limit(pageRequest.getOffset(), pageRequest.getPageSize());
            }
        }

        List<CcList> result = ((offsetStep != null) ? offsetStep.fetch() : select.fetch())
                .map((RecordMapper<Record, CcList>) row -> {
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
                        ccList.setOagisComponentType(valueOf(componentType));
                    }
                    ccList.setState(CcState.valueOf(row.getValue("state", String.class)));
                    ccList.setDeprecated(row.getValue("is_deprecated", Byte.class) == 1);
                    ccList.setLastUpdateTimestamp(Date.from(row.getValue("last_update_timestamp", LocalDateTime.class)
                            .atZone(ZoneId.systemDefault()).toInstant()));
                    ccList.setOwner((String) row.getValue("owner"));
                    ccList.setOwnedByDeveloper(row.getValue("owned_by_developer", Byte.class) == 1);
                    ccList.setLastUpdateUser((String) row.getValue("last_update_user"));
                    ccList.setRevision(row.getValue(LOG.REVISION_NUM).toString());
                    ccList.setReleaseNum(row.getValue(RELEASE.RELEASE_NUM));
                    ULong basedManifestId = row.getValue("based_manifest_id", ULong.class);
                    if (basedManifestId != null) {
                        ccList.setBasedManifestId(basedManifestId.toBigInteger());
                        ccList.setDtType(ccList.getType() == CcType.DT ? "BDT" : "");
                    } else {
                        ccList.setDtType(ccList.getType() == CcType.DT ? "CDT" : "");
                    }
                    ccList.setSixDigitId(row.getValue("six_digit_id", String.class));
                    ccList.setDefaultValueDomain(row.getValue("default_value_domain", String.class));
                    ccList.setNewComponent(row.getValue("new_component", Byte.class) == 1);
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
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
    }

    private List<BigInteger> getManifestIdsByBasedAccManifestIdAndReleaseId(
            List<BigInteger> basedManifestIds, BigInteger releaseId) {
        return dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .where(and(
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID.in(basedManifestIds),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
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
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent() ? ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull()
                    : ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNotNull());
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
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
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), ACC_MANIFEST.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), ACC.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(ACC.LAST_UPDATE_TIMESTAMP
                    .greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(ACC.LAST_UPDATE_TIMESTAMP
                    .lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            conditions.add(TAG.NAME.in(request.getTags()));
        }
        if (request.getNamespaces() != null && !request.getNamespaces().isEmpty()) {
            conditions.add(ACC.NAMESPACE_ID.in(request.getNamespaces()));
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

        List<Field> selectFields = new ArrayList<>();
        selectFields.addAll(Arrays.asList(
                inline("ACC").as("type"),
                ACC_MANIFEST.ACC_MANIFEST_ID.as("manifest_id"),
                ACC.ACC_ID.as("id"),
                ACC.GUID,
                ACC_MANIFEST.DEN,
                ACC.DEFINITION,
                ACC.DEFINITION_SOURCE,
                ACC.OBJECT_CLASS_TERM.as("term"),
                ACC.OAGIS_COMPONENT_TYPE.as("oagis_component_type"),
                ACC.STATE,
                ACC.IS_DEPRECATED,
                ACC.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM,
                ACC_MANIFEST.BASED_ACC_MANIFEST_ID.as("based_manifest_id"),
                val((String) null).as("six_digit_id"),
                val((String) null).as("default_value_domain"),
                iif(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull(), true, false).as("new_component")));
        if (StringUtils.hasLength(request.getDen())) {
            selectFields.add(
                    val(1).minus(levenshtein(lower(ACC_MANIFEST.DEN), val(request.getDen().toLowerCase()))
                                    .div(greatest(length(ACC_MANIFEST.DEN), length(request.getDen()))))
                            .as("score")
            );
        }

        return dslContext.select(selectFields)
                .from(ACC)
                .join(ACC_MANIFEST)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID)
                        .and(ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(ACC.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(ACC.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(ACC_MANIFEST_TAG)
                .on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST_TAG.ACC_MANIFEST_ID))
                .leftJoin(TAG)
                .on(ACC_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .leftJoin(MODULE_ACC_MANIFEST)
                .on(and(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID),
                        MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getAsccList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(ASCC_MANIFEST.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(ASCC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent() ? ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull()
                    : ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNotNull());
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
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
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), ASCC_MANIFEST.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), ASCC.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(ASCC.LAST_UPDATE_TIMESTAMP
                    .greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(ASCC.LAST_UPDATE_TIMESTAMP
                    .lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        List<Field> selectFields = new ArrayList<>();
        selectFields.addAll(Arrays.asList(
                inline("ASCC").as("type"),
                ASCC_MANIFEST.ASCC_MANIFEST_ID.as("manifest_id"),
                ASCC.ASCC_ID.as("id"),
                ASCC.GUID,
                ASCC_MANIFEST.DEN,
                ASCC.DEFINITION,
                ASCC.DEFINITION_SOURCE,
                val((String) null).as("term"),
                val((Integer) null).as("oagis_component_type"),
                ACC.STATE,
                ASCC.IS_DEPRECATED,
                ASCC.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM,
                val((Integer) null).as("based_manifest_id"),
                val((String) null).as("six_digit_id"),
                val((String) null).as("default_value_domain"),
                iif(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull(), true, false).as("new_component")));
        if (StringUtils.hasLength(request.getDen())) {
            selectFields.add(
                    val(1).minus(levenshtein(lower(ASCC_MANIFEST.DEN), val(request.getDen().toLowerCase()))
                                    .div(greatest(length(ASCC_MANIFEST.DEN), length(request.getDen()))))
                            .as("score")
            );
        }

        return dslContext.select(selectFields)
                .from(ASCC)
                .join(ASCC_MANIFEST)
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID)
                        .and(ASCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(ACC_MANIFEST)
                .on(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)))
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
                .on(and(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID),
                        MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getBccList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(BCC_MANIFEST.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(BCC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent() ? BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull()
                    : BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNotNull());
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
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
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), BCC_MANIFEST.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), BCC.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BCC.LAST_UPDATE_TIMESTAMP
                    .greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BCC.LAST_UPDATE_TIMESTAMP
                    .lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        List<Field> selectFields = new ArrayList<>();
        selectFields.addAll(Arrays.asList(
                inline("BCC").as("type"),
                BCC_MANIFEST.BCC_MANIFEST_ID.as("manifest_id"),
                BCC.BCC_ID.as("id"),
                BCC.GUID,
                BCC_MANIFEST.DEN,
                BCC.DEFINITION,
                BCC.DEFINITION_SOURCE,
                val((String) null).as("term"),
                val((Integer) null).as("oagis_component_type"),
                ACC.STATE,
                BCC.IS_DEPRECATED,
                BCC.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM,
                val((Integer) null).as("based_manifest_id"),
                val((String) null).as("six_digit_id"),
                val((String) null).as("default_value_domain"),
                iif(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull(), true, false).as("new_component")));
        if (StringUtils.hasLength(request.getDen())) {
            selectFields.add(
                    val(1).minus(levenshtein(lower(BCC_MANIFEST.DEN), val(request.getDen().toLowerCase()))
                                    .div(greatest(length(BCC_MANIFEST.DEN), length(request.getDen()))))
                            .as("score")
            );
        }

        return dslContext.select(selectFields)
                .from(BCC)
                .join(BCC_MANIFEST)
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID)
                        .and(BCC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(ACC_MANIFEST)
                .on(and(
                        BCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)))
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
                .on(and(ACC_MANIFEST.ACC_MANIFEST_ID.eq(MODULE_ACC_MANIFEST.ACC_MANIFEST_ID),
                        MODULE_ACC_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ACC_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getAsccpList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(ASCCP_MANIFEST.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(ASCCP.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent() ? ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull()
                    : ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNotNull());
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
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
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), ASCCP_MANIFEST.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), ASCCP.DEFINITION));
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            conditions.add(TAG.NAME.in(request.getTags()));
        }
        if (request.getNamespaces() != null && !request.getNamespaces().isEmpty()) {
            conditions.add(ASCCP.NAMESPACE_ID.in(request.getNamespaces()));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(ASCCP.LAST_UPDATE_TIMESTAMP
                    .greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(ASCCP.LAST_UPDATE_TIMESTAMP
                    .lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }
        if (request.getAsccpTypes().size() != 0) {
            conditions.add(ASCCP.TYPE.in(request.getAsccpTypes()));
        }
        if (request.getIsBIEUsable() != null && request.getIsBIEUsable()) {
            conditions.add(ACC.OAGIS_COMPONENT_TYPE
                    .notIn(Arrays.asList(SemanticGroup.getValue(), UserExtensionGroup.getValue())));
        }

        List<Field> selectFields = new ArrayList<>();
        selectFields.addAll(Arrays.asList(
                inline("ASCCP").as("type"),
                ASCCP_MANIFEST.ASCCP_MANIFEST_ID.as("manifest_id"),
                ASCCP.ASCCP_ID.as("id"),
                ASCCP.GUID,
                ASCCP_MANIFEST.DEN,
                ASCCP.DEFINITION,
                ASCCP.DEFINITION_SOURCE,
                ASCCP.PROPERTY_TERM.as("term"),
                val((Integer) null).as("oagis_component_type"),
                ASCCP.STATE,
                ASCCP.IS_DEPRECATED,
                ASCCP.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM,
                val((Integer) null).as("based_manifest_id"),
                val((String) null).as("six_digit_id"),
                val((String) null).as("default_value_domain"),
                iif(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull(), true, false).as("new_component")));
        if (StringUtils.hasLength(request.getDen())) {
            selectFields.add(
                    val(1).minus(levenshtein(lower(ASCCP_MANIFEST.DEN), val(request.getDen().toLowerCase()))
                                    .div(greatest(length(ASCCP_MANIFEST.DEN), length(request.getDen()))))
                            .as("score")
            );
        }

        return dslContext.select(selectFields)
                .from(ASCCP)
                .join(ASCCP_MANIFEST)
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID)
                        .and(ASCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
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
                .leftJoin(ASCCP_MANIFEST_TAG)
                .on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID))
                .leftJoin(TAG)
                .on(ASCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .leftJoin(MODULE_ASCCP_MANIFEST)
                .on(and(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(MODULE_ASCCP_MANIFEST.ASCCP_MANIFEST_ID),
                        MODULE_ASCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_ASCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    private SelectOrderByStep getBccpList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        conditions.add(BCCP_MANIFEST.DEN.notContains("User Extension Group"));
        if (request.getDeprecated() != null) {
            conditions.add(BCCP.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent() ? BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull()
                    : BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNotNull());
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
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
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), BCCP_MANIFEST.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), BCCP.DEFINITION));
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            conditions.add(TAG.NAME.in(request.getTags()));
        }
        if (request.getNamespaces() != null && !request.getNamespaces().isEmpty()) {
            conditions.add(BCCP.NAMESPACE_ID.in(request.getNamespaces()));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BCCP.LAST_UPDATE_TIMESTAMP
                    .greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BCCP.LAST_UPDATE_TIMESTAMP
                    .lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        List<Field> selectFields = new ArrayList<>();
        selectFields.addAll(Arrays.asList(
                inline("BCCP").as("type"),
                BCCP_MANIFEST.BCCP_MANIFEST_ID.as("manifest_id"),
                BCCP.BCCP_ID.as("id"),
                BCCP.GUID,
                BCCP_MANIFEST.DEN,
                BCCP.DEFINITION,
                BCCP.DEFINITION_SOURCE,
                BCCP.PROPERTY_TERM.as("term"),
                val((Integer) null).as("oagis_component_type"),
                BCCP.STATE,
                BCCP.IS_DEPRECATED,
                BCCP.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM,
                val((Integer) null).as("based_manifest_id"),
                val((String) null).as("six_digit_id"),
                val((String) null).as("default_value_domain"),
                iif(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull(), true, false).as("new_component")));
        if (StringUtils.hasLength(request.getDen())) {
            selectFields.add(
                    val(1).minus(levenshtein(lower(BCCP_MANIFEST.DEN), val(request.getDen().toLowerCase()))
                                    .div(greatest(length(BCCP_MANIFEST.DEN), length(request.getDen()))))
                            .as("score")
            );
        }

        return dslContext.select(selectFields)
                .from(BCCP)
                .join(BCCP_MANIFEST)
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID)
                        .and(BCCP_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(LOG)
                .on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(BCCP.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(BCCP.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(BCCP_MANIFEST_TAG)
                .on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID))
                .leftJoin(TAG)
                .on(BCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .leftJoin(MODULE_BCCP_MANIFEST)
                .on(and(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(MODULE_BCCP_MANIFEST.BCCP_MANIFEST_ID),
                        MODULE_BCCP_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_BCCP_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .where(conditions);
    }

    public SelectOrderByStep getDtList(CcListRequest request, Release release, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        conditions.add(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId())));
        if (request.getDtTypes() != null && !request.getDtTypes().isEmpty()) {
            List<String> dtTypes = request.getDtTypes().stream()
                    .filter(e -> "CDT".equals(e) || "BDT".equals(e))
                    .collect(Collectors.toList());

            if (dtTypes.size() == 1) {
                String dtType = dtTypes.get(0);
                switch (dtType) {
                    case "CDT":
                        conditions.add(DT.BASED_DT_ID.isNull());
                        break;
                    case "BDT":
                        conditions.add(DT.BASED_DT_ID.isNotNull());
                        break;
                }
            }
        }
        if (request.getDeprecated() != null) {
            conditions.add(DT.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent() ? DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull()
                    : DT_MANIFEST.PREV_DT_MANIFEST_ID.isNotNull());
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
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
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            conditions.add(TAG.NAME.in(request.getTags()));
        }
        if (request.getNamespaces() != null && !request.getNamespaces().isEmpty()) {
            conditions.add(DT.NAMESPACE_ID.in(request.getNamespaces()));
        }
        if (StringUtils.hasLength(request.getDen())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDen(), DT_MANIFEST.DEN));
        }
        if (StringUtils.hasLength(request.getDefinition())) {
            conditions.addAll(ContainsFilterBuilder.contains(request.getDefinition(), DT.DEFINITION));
        }
        if (StringUtils.hasLength(request.getModule())) {
            conditions.add(MODULE.PATH.containsIgnoreCase(request.getModule()));
        }
        if (request.getCommonlyUsed() != null) {
            conditions.add(DT.COMMONLY_USED.eq((byte) (request.getCommonlyUsed() ? 1 : 0)));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(DT.LAST_UPDATE_TIMESTAMP
                    .greaterThan(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(DT.LAST_UPDATE_TIMESTAMP
                    .lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        List<Field> selectFields = new ArrayList<>();
        selectFields.addAll(Arrays.asList(
                inline("DT").as("type"),
                DT_MANIFEST.DT_MANIFEST_ID.as("manifest_id"),
                DT.DT_ID.as("id"),
                DT.GUID,
                DT_MANIFEST.DEN,
                DT.DEFINITION,
                DT.DEFINITION_SOURCE,
                DT.DATA_TYPE_TERM.as("term"),
                val((Integer) null).as("oagis_component_type"),
                DT.STATE,
                DT.IS_DEPRECATED,
                DT.LAST_UPDATE_TIMESTAMP,
                MODULE.PATH.as("module_path"),
                appUserOwner.LOGIN_ID.as("owner"),
                appUserOwner.IS_DEVELOPER.as("owned_by_developer"),
                appUserUpdater.LOGIN_ID.as("last_update_user"),
                LOG.REVISION_NUM,
                LOG.REVISION_TRACKING_NUM,
                RELEASE.RELEASE_NUM,
                DT_MANIFEST.BASED_DT_MANIFEST_ID.as("based_manifest_id"),
                DT.SIX_DIGIT_ID,
                concat(ifnull(CDT_PRI.NAME, ""),
                        ifnull(CODE_LIST.NAME, ""),
                        ifnull(AGENCY_ID_LIST.NAME, ""),
                        ifnull(CDT_PRI.as("pri_for_cdt").NAME, "")).as("default_value_domain"),
                iif(DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull(), true, false).as("new_component")));
        if (StringUtils.hasLength(request.getDen())) {
            selectFields.add(
                    val(1).minus(levenshtein(lower(DT_MANIFEST.DEN), val(request.getDen().toLowerCase()))
                                    .div(greatest(length(DT_MANIFEST.DEN), length(request.getDen()))))
                            .as("score")
            );
        }

        return dslContext.select(selectFields)
                .from(DT)
                .join(DT_MANIFEST)
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID)
                        .and(DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(release.getReleaseId()))))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(appUserOwner)
                .on(DT.OWNER_USER_ID.eq(appUserOwner.APP_USER_ID))
                .join(appUserUpdater)
                .on(DT.LAST_UPDATED_BY.eq(appUserUpdater.APP_USER_ID))
                .leftJoin(DT_MANIFEST_TAG)
                .on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST_TAG.DT_MANIFEST_ID))
                .leftJoin(TAG)
                .on(DT_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .leftJoin(MODULE_DT_MANIFEST)
                .on(and(DT_MANIFEST.DT_MANIFEST_ID.eq(MODULE_DT_MANIFEST.DT_MANIFEST_ID),
                        MODULE_DT_MANIFEST.MODULE_SET_RELEASE_ID.eq(defaultModuleSetReleaseId)))
                .leftJoin(MODULE)
                .on(MODULE_DT_MANIFEST.MODULE_ID.eq(MODULE.MODULE_ID))
                .leftJoin(BDT_PRI_RESTRI)
                .on(and(DT_MANIFEST.DT_MANIFEST_ID.eq(BDT_PRI_RESTRI.BDT_MANIFEST_ID),
                        BDT_PRI_RESTRI.IS_DEFAULT.eq((byte) 1)))
                .leftJoin(CODE_LIST_MANIFEST)
                .on(BDT_PRI_RESTRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .leftJoin(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_MANIFEST)
                .on(BDT_PRI_RESTRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST)
                .on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .leftJoin(CDT_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(CDT_AWD_PRI).on(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.eq(CDT_AWD_PRI.CDT_AWD_PRI_ID))
                .leftJoin(CDT_PRI).on(CDT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                .leftJoin(CDT_AWD_PRI.as("awd_pri_for_cdt"))
                .on(and(DT.DT_ID.eq(CDT_AWD_PRI.as("awd_pri_for_cdt").CDT_ID),
                        CDT_AWD_PRI.as("awd_pri_for_cdt").IS_DEFAULT.eq((byte) 1)))
                .leftJoin(CDT_PRI.as("pri_for_cdt"))
                .on(CDT_AWD_PRI.as("awd_pri_for_cdt").CDT_PRI_ID.eq(CDT_PRI.as("pri_for_cdt").CDT_PRI_ID))
                .where(conditions);
    }

    public Collection<CcChangesResponse.CcChange> getCcChanges(ScoreUser requester, ULong releaseId) {
        List<CcChangesResponse.CcChange> response = new ArrayList<>();

        response.addAll(getNewAccList(requester, releaseId));
        response.addAll(getChangedAccList(requester, releaseId));

        response.addAll(getNewAsccpList(requester, releaseId));
        response.addAll(getChangedAsccpList(requester, releaseId));

        response.addAll(getNewBccpList(requester, releaseId));
        response.addAll(getChangedBccpList(requester, releaseId));

        response.addAll(getNewAsccList(requester, releaseId));
        response.addAll(getChangedAsccList(requester, releaseId));

        response.addAll(getNewBccList(requester, releaseId));
        response.addAll(getChangedBccList(requester, releaseId));

        response.addAll(getNewDtList(requester, releaseId));
        response.addAll(getChangedDtList(requester, releaseId));

        response.addAll(getNewCodeListList(requester, releaseId));
        response.addAll(getChangedCodeListList(requester, releaseId));

        response.addAll(getNewAgencyIdListList(requester, releaseId));
        response.addAll(getChangedAgencyIdListList(requester, releaseId));

        return response;
    }

    private Collection<CcChangesResponse.CcChange> getNewAccList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> accChangesMap = new HashMap<>();
        dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .leftJoin(ACC_MANIFEST_TAG).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST_TAG.ACC_MANIFEST_ID))
                .leftJoin(TAG).on(ACC_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(releaseId),
                        ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (accChangesMap.containsKey(manifestId)) {
                        ccChange = accChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("ACC", manifestId.toBigInteger(),
                                record.get(ACC_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        accChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return accChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAccList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> accChangesMap = new HashMap<>();
        dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID, ACC_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(ACC_MANIFEST)
                .join(ACC_MANIFEST.as("prev_manifest")).on(
                        and(
                                ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev_manifest").ACC_MANIFEST_ID),
                                ACC_MANIFEST.ACC_ID.notEqual(ACC_MANIFEST.as("prev_manifest").ACC_ID)))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .leftJoin(ACC_MANIFEST_TAG).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ACC_MANIFEST_TAG.ACC_MANIFEST_ID))
                .leftJoin(TAG).on(ACC_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(ACC_MANIFEST.ACC_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (accChangesMap.containsKey(manifestId)) {
                        ccChange = accChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("ACC", manifestId.toBigInteger(),
                                record.get(ACC_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        accChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return accChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewAsccpList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> asccpChangesMap = new HashMap<>();
        dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(ASCCP_MANIFEST)
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .leftJoin(ASCCP_MANIFEST_TAG)
                .on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID))
                .leftJoin(TAG).on(ASCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(releaseId),
                        ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (asccpChangesMap.containsKey(manifestId)) {
                        ccChange = asccpChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("ASCCP", manifestId.toBigInteger(),
                                record.get(ASCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        asccpChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return asccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAsccpList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> asccpChangesMap = new HashMap<>();
        dslContext.select(ASCCP_MANIFEST.ASCCP_MANIFEST_ID, ASCCP_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(ASCCP_MANIFEST)
                .join(ASCCP_MANIFEST.as("prev_manifest")).on(
                        and(
                                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID
                                        .eq(ASCCP_MANIFEST.as("prev_manifest").ASCCP_MANIFEST_ID),
                                ASCCP_MANIFEST.ASCCP_ID.notEqual(ASCCP_MANIFEST.as("prev_manifest").ASCCP_ID)))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .leftJoin(ASCCP_MANIFEST_TAG)
                .on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID))
                .leftJoin(TAG).on(ASCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (asccpChangesMap.containsKey(manifestId)) {
                        ccChange = asccpChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("ASCCP", manifestId.toBigInteger(),
                                record.get(ASCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        asccpChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return asccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewBccpList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> bccpChangesMap = new HashMap<>();
        dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(BCCP_MANIFEST)
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .leftJoin(BCCP_MANIFEST_TAG).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID))
                .leftJoin(TAG).on(BCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(releaseId),
                        BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (bccpChangesMap.containsKey(manifestId)) {
                        ccChange = bccpChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("BCCP", manifestId.toBigInteger(),
                                record.get(BCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        bccpChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return bccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedBccpList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> bccpChangesMap = new HashMap<>();
        dslContext.select(BCCP_MANIFEST.BCCP_MANIFEST_ID, BCCP_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(BCCP_MANIFEST)
                .join(BCCP_MANIFEST.as("prev_manifest")).on(
                        and(
                                BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID
                                        .eq(BCCP_MANIFEST.as("prev_manifest").BCCP_MANIFEST_ID),
                                BCCP_MANIFEST.BCCP_ID.notEqual(BCCP_MANIFEST.as("prev_manifest").BCCP_ID)))
                .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                .leftJoin(BCCP_MANIFEST_TAG).on(BCCP_MANIFEST.BCCP_MANIFEST_ID.eq(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID))
                .leftJoin(TAG).on(BCCP_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (bccpChangesMap.containsKey(manifestId)) {
                        ccChange = bccpChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("BCCP", manifestId.toBigInteger(),
                                record.get(BCCP_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        bccpChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return bccpChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewAsccList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> asccChangesMap = new HashMap<>();
        dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.DEN)
                .from(ASCC_MANIFEST)
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(releaseId),
                        ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (asccChangesMap.containsKey(manifestId)) {
                        ccChange = asccChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("ASCC", manifestId.toBigInteger(),
                                record.get(ASCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        asccChangesMap.put(manifestId, ccChange);
                    }
                });
        return asccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAsccList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> asccChangesMap = new HashMap<>();
        dslContext.select(ASCC_MANIFEST.ASCC_MANIFEST_ID, ASCC_MANIFEST.DEN)
                .from(ASCC_MANIFEST)
                .join(ASCC_MANIFEST.as("prev_manifest")).on(
                        and(
                                ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID
                                        .eq(ASCC_MANIFEST.as("prev_manifest").ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.ASCC_ID.notEqual(ASCC_MANIFEST.as("prev_manifest").ASCC_ID)))
                .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (asccChangesMap.containsKey(manifestId)) {
                        ccChange = asccChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("ASCC", manifestId.toBigInteger(),
                                record.get(ASCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        asccChangesMap.put(manifestId, ccChange);
                    }
                });
        return asccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewBccList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> bccChangesMap = new HashMap<>();
        dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.DEN)
                .from(BCC_MANIFEST)
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(releaseId),
                        BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(BCC_MANIFEST.BCC_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (bccChangesMap.containsKey(manifestId)) {
                        ccChange = bccChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("BCC", manifestId.toBigInteger(),
                                record.get(BCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        bccChangesMap.put(manifestId, ccChange);
                    }
                });
        return bccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedBccList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> bccChangesMap = new HashMap<>();
        dslContext.select(BCC_MANIFEST.BCC_MANIFEST_ID, BCC_MANIFEST.DEN)
                .from(BCC_MANIFEST)
                .join(BCC_MANIFEST.as("prev_manifest")).on(
                        and(
                                BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev_manifest").BCC_MANIFEST_ID),
                                BCC_MANIFEST.BCC_ID.notEqual(BCC_MANIFEST.as("prev_manifest").BCC_ID)))
                .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(BCC_MANIFEST.BCC_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (bccChangesMap.containsKey(manifestId)) {
                        ccChange = bccChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("BCC", manifestId.toBigInteger(),
                                record.get(BCC_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        bccChangesMap.put(manifestId, ccChange);
                    }
                });
        return bccChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewDtList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> dtChangesMap = new HashMap<>();
        dslContext.select(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .leftJoin(DT_MANIFEST_TAG).on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST_TAG.DT_MANIFEST_ID))
                .leftJoin(TAG).on(DT_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(releaseId),
                        DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(DT_MANIFEST.DT_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (dtChangesMap.containsKey(manifestId)) {
                        ccChange = dtChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("DT", manifestId.toBigInteger(),
                                record.get(DT_MANIFEST.DEN), CcChangesResponse.CcChangeType.NEW_COMPONENT, new ArrayList<>());
                        dtChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return dtChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedDtList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> dtChangesMap = new HashMap<>();
        dslContext.select(DT_MANIFEST.DT_MANIFEST_ID, DT_MANIFEST.DEN,
                        TAG.TAG_ID, TAG.NAME, TAG.TEXT_COLOR, TAG.BACKGROUND_COLOR)
                .from(DT_MANIFEST)
                .join(DT_MANIFEST.as("prev_manifest")).on(
                        and(
                                DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev_manifest").DT_MANIFEST_ID),
                                DT_MANIFEST.DT_ID.notEqual(DT_MANIFEST.as("prev_manifest").DT_ID)))
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .leftJoin(DT_MANIFEST_TAG).on(DT_MANIFEST.DT_MANIFEST_ID.eq(DT_MANIFEST_TAG.DT_MANIFEST_ID))
                .leftJoin(TAG).on(DT_MANIFEST_TAG.TAG_ID.eq(TAG.TAG_ID))
                .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(DT_MANIFEST.DT_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (dtChangesMap.containsKey(manifestId)) {
                        ccChange = dtChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("DT", manifestId.toBigInteger(),
                                record.get(DT_MANIFEST.DEN), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        dtChangesMap.put(manifestId, ccChange);
                    }

                    ULong tagId = record.get(TAG.TAG_ID);
                    if (tagId != null) {
                        ShortTag tag = new ShortTag();
                        tag.setTagId(tagId.toBigInteger());
                        tag.setName(record.get(TAG.NAME));
                        tag.setTextColor(record.get(TAG.TEXT_COLOR));
                        tag.setBackgroundColor(record.get(TAG.BACKGROUND_COLOR));
                        ccChange.addTag(tag);
                    }
                });
        return dtChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewCodeListList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> codeListChangesMap = new HashMap<>();
        dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(and(
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId),
                        CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (codeListChangesMap.containsKey(manifestId)) {
                        ccChange = codeListChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("CODE_LIST", manifestId.toBigInteger(),
                                record.get(CODE_LIST.NAME), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                new ArrayList<>());
                        codeListChangesMap.put(manifestId, ccChange);
                    }
                });
        return codeListChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedCodeListList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> codeListChangesMap = new HashMap<>();
        dslContext.select(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST.NAME)
                .from(CODE_LIST_MANIFEST)
                .join(CODE_LIST_MANIFEST.as("prev_manifest")).on(
                        and(
                                CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID
                                        .eq(CODE_LIST_MANIFEST.as("prev_manifest").CODE_LIST_MANIFEST_ID),
                                CODE_LIST_MANIFEST.CODE_LIST_ID
                                        .notEqual(CODE_LIST_MANIFEST.as("prev_manifest").CODE_LIST_ID)))
                .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (codeListChangesMap.containsKey(manifestId)) {
                        ccChange = codeListChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("CODE_LIST", manifestId.toBigInteger(),
                                record.get(CODE_LIST.NAME), CcChangesResponse.CcChangeType.REVISED, new ArrayList<>());
                        codeListChangesMap.put(manifestId, ccChange);
                    }
                });
        return codeListChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getNewAgencyIdListList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> agencyIdListChangesMap = new HashMap<>();
        dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId),
                        AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNull()))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (agencyIdListChangesMap.containsKey(manifestId)) {
                        ccChange = agencyIdListChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("AGENCY_ID_LIST", manifestId.toBigInteger(),
                                record.get(AGENCY_ID_LIST.NAME), CcChangesResponse.CcChangeType.NEW_COMPONENT,
                                new ArrayList<>());
                        agencyIdListChangesMap.put(manifestId, ccChange);
                    }
                });
        return agencyIdListChangesMap.values();
    }

    private Collection<CcChangesResponse.CcChange> getChangedAgencyIdListList(ScoreUser requester, ULong releaseId) {
        Map<ULong, CcChangesResponse.CcChange> agencyIdListChangesMap = new HashMap<>();
        dslContext.select(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST.NAME)
                .from(AGENCY_ID_LIST_MANIFEST)
                .join(AGENCY_ID_LIST_MANIFEST.as("prev_manifest")).on(
                        and(
                                AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID
                                        .eq(AGENCY_ID_LIST_MANIFEST.as("prev_manifest").AGENCY_ID_LIST_MANIFEST_ID),
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID
                                        .notEqual(AGENCY_ID_LIST_MANIFEST.as("prev_manifest").AGENCY_ID_LIST_ID)))
                .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(releaseId))
                .fetchStream().forEach(record -> {
                    ULong manifestId = record.get(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID);
                    CcChangesResponse.CcChange ccChange;
                    if (agencyIdListChangesMap.containsKey(manifestId)) {
                        ccChange = agencyIdListChangesMap.get(manifestId);
                    } else {
                        ccChange = new CcChangesResponse.CcChange("AGENCY_ID_LIST", manifestId.toBigInteger(),
                                record.get(AGENCY_ID_LIST.NAME), CcChangesResponse.CcChangeType.REVISED,
                                new ArrayList<>());
                        agencyIdListChangesMap.put(manifestId, ccChange);
                    }
                });
        return agencyIdListChangesMap.values();
    }

    public Map<String, String> getLastUpdatedRelease() {
        Map<String, String> lastUpdatedMap = new HashMap<>();
        CommonTableExpression<?> t = name("t").fields(
                "asccp_manifest_id",
                "release_id",
                "prev_asccp_manifest_id",
                "asccp_id").as(
                        select(
                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                                ASCCP_MANIFEST.RELEASE_ID,
                                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                                ASCCP_MANIFEST.ASCCP_ID)
                                .from(ASCCP_MANIFEST)
                                .where(ASCCP_MANIFEST.RELEASE_ID.eq(select(max(RELEASE.RELEASE_ID)).from(RELEASE)))
                                .unionAll(
                                        select(
                                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                                                ASCCP_MANIFEST.RELEASE_ID,
                                                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                                                ASCCP_MANIFEST.ASCCP_ID)
                                                .from(table(name("t")))
                                                .join(ASCCP_MANIFEST)
                                                .on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID
                                                        .eq(field(name("t", "prev_asccp_manifest_id"), ULong.class))
                                                        .and(ASCCP_MANIFEST.ASCCP_ID.eq(
                                                                field(name("t", "asccp_id"), ULong.class))))));

        dslContext
                .withRecursive(t)
                .select(ASCCP.DEN, min(RELEASE.RELEASE_ID).as("RELEASE_ID"), RELEASE.RELEASE_NUM)
                .from(t)
                .join(ASCCP)
                .on(ASCCP.ASCCP_ID.eq(field(name("t", "asccp_id"), ULong.class)))
                .join(RELEASE)
                .on(RELEASE.RELEASE_ID.eq(field(name("t", "release_id"), ULong.class)))
                .groupBy(ASCCP.DEN)
                .fetchStream().forEach(record -> {
                    String den = record.get(ASCCP.DEN);
                    String relNum = record.get(RELEASE.RELEASE_NUM);
                    lastUpdatedMap.put(den, relNum);
                });

        return lastUpdatedMap;

    }

    public Map<String, String> getSinceRelease() {
        Map<String, String> sinceMap = new HashMap<>();

        CommonTableExpression<?> t = name("t").fields(
                "asccp_manifest_id",
                "release_id",
                "prev_asccp_manifest_id",
                "asccp_id").as(
                        select(
                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                                ASCCP_MANIFEST.RELEASE_ID,
                                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                                ASCCP_MANIFEST.ASCCP_ID)
                                .from(ASCCP_MANIFEST)
                                .where(ASCCP_MANIFEST.RELEASE_ID.eq(select(max(RELEASE.RELEASE_ID)).from(RELEASE)))
                                .unionAll(
                                        select(
                                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                                                ASCCP_MANIFEST.RELEASE_ID,
                                                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                                                ASCCP_MANIFEST.ASCCP_ID)
                                                .from(table(name("t")))
                                                .join(ASCCP_MANIFEST)
                                                .on(ASCCP_MANIFEST.ASCCP_MANIFEST_ID
                                                        .eq(field(name("t", "prev_asccp_manifest_id"), ULong.class)))));

        dslContext
                .withRecursive(t)
                .select(ASCCP.DEN, min(RELEASE.RELEASE_ID).as("RELEASE_ID"), RELEASE.RELEASE_NUM)
                .from(t)
                .join(ASCCP)
                .on(ASCCP.ASCCP_ID.eq(field(name("t", "asccp_id"), ULong.class)))
                .join(RELEASE)
                .on(RELEASE.RELEASE_ID.eq(field(name("t", "release_id"), ULong.class)))
                .groupBy(ASCCP.DEN)
                .fetchStream().forEach(record -> {
                    String den = record.get(ASCCP.DEN);
                    String relNum = record.get(RELEASE.RELEASE_NUM);
                    sinceMap.put(den, relNum);
                });
        return sinceMap;

    }

}
