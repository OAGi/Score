package org.oagi.score.gateway.http.api.cc_management.repository;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.cc_management.model.*;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseSummaryRecord;
import org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.AppUser;
import org.oagi.score.gateway.http.common.repository.jooq.entity.tables.records.ModuleSetReleaseRecord;
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
import static org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType.*;
import static org.oagi.score.gateway.http.common.model.SortDirection.DESC;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Routines.levenshtein;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

@Deprecated
@Repository
public class CcListRepository {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private RepositoryFactory repositoryFactory;

    public PageResponse<CcList> getCcList(ScoreUser requester, CcListRequest request) {
        Set<ReleaseSummaryRecord> releases = repositoryFactory.releaseQueryRepository(requester)
                .getIncludedReleaseSummaryList(request.getReleaseId());

        ULong defaultModuleSetReleaseId = null;
        ModuleSetReleaseRecord defaultModuleSetRelease = dslContext.selectFrom(MODULE_SET_RELEASE)
                .where(and(MODULE_SET_RELEASE.IS_DEFAULT.eq((byte) 1),
                        MODULE_SET_RELEASE.RELEASE_ID.eq(ULong.valueOf(request.getReleaseId().value()))))
                .fetchOne();
        if (defaultModuleSetRelease != null) {
            defaultModuleSetReleaseId = defaultModuleSetRelease.getModuleSetReleaseId();
        }

        SelectOrderByStep select = null;
        if (request.getTypes().isAcc()) {
            select = (select != null) ? select.union(getAccList(request, releases, defaultModuleSetReleaseId))
                    : getAccList(request, releases, defaultModuleSetReleaseId);
        }
        // Component Types are only allowed by ACC.
        if (request.getTypes().isAsccp() && !hasLength(request.getComponentTypes())) {
            select = (select != null) ? select.union(getAsccpList(request, releases, defaultModuleSetReleaseId))
                    : getAsccpList(request, releases, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isBccp() && !hasLength(request.getComponentTypes())) {
            select = (select != null) ? select.union(getBccpList(request, releases, defaultModuleSetReleaseId))
                    : getBccpList(request, releases, defaultModuleSetReleaseId);
        }
        // Tags/Namespaces are only allowed by ACC, ASCCP, BCCP, and DT.
        if (request.getTypes().isAscc() && !hasLength(request.getComponentTypes()) && request.getTags().isEmpty()
                && request.getNamespaces().isEmpty()) {
            select = (select != null) ? select.union(getAsccList(request, releases, defaultModuleSetReleaseId))
                    : getAsccList(request, releases, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isBcc() && !hasLength(request.getComponentTypes()) && request.getTags().isEmpty()
                && request.getNamespaces().isEmpty()) {
            select = (select != null) ? select.union(getBccList(request, releases, defaultModuleSetReleaseId))
                    : getBccList(request, releases, defaultModuleSetReleaseId);
        }
        if (request.getTypes().isDt() && !hasLength(request.getComponentTypes())) {
            select = (select != null) ? select.union(getDtList(request, releases, defaultModuleSetReleaseId))
                    : getDtList(request, releases, defaultModuleSetReleaseId);
        }

        if (select == null) {
            PageResponse response = new PageResponse();
            response.setList(Collections.emptyList());
            response.setPage(request.getPageRequest().pageIndex());
            response.setSize(request.getPageRequest().pageSize());
            response.setLength(0);
            return response;
        }

        PageRequest pageRequest = request.getPageRequest();
        List<SortField<?>> sortFields = sortFields(pageRequest);
        if (StringUtils.hasLength(request.getDen())) {
            sortFields.add(field("score").desc());
        }

        int count = dslContext.fetchCount(select);

        SelectWithTiesAfterOffsetStep offsetStep = null;
        if (!sortFields.isEmpty()) {
            offsetStep = select.orderBy(sortFields)
                    .limit(pageRequest.pageOffset(), pageRequest.pageSize());
        } else {
            if (pageRequest.pageIndex() >= 0 && pageRequest.pageSize() > 0) {
                offsetStep = select
                        .limit(pageRequest.pageOffset(), pageRequest.pageSize());
            }
        }

        List<CcList> result = ((offsetStep != null) ? offsetStep.fetch() : select.fetch())
                .map((RecordMapper<Record, CcList>) row -> {
                    CcList ccList = new CcList();
                    CcType ccType = CcType.valueOf(row.getValue("type", String.class));
                    ccList.setType(ccType);
                    ccList.setLibraryId(new LibraryId(row.getValue("library_id", ULong.class).toBigInteger()));
                    ccList.setLibraryName(row.getValue("library_name", String.class));

                    ManifestId manifestId;
                    switch (ccType) {
                        case ACC:
                            manifestId = new AccManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
                            break;
                        case ASCC:
                            manifestId = new AsccManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
                            break;
                        case BCC:
                            manifestId = new BccManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
                            break;
                        case ASCCP:
                            manifestId = new AsccpManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
                            break;
                        case BCCP:
                            manifestId = new BccpManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
                            break;
                        case DT:
                        case CDT:
                        case BDT:
                            manifestId = new DtManifestId(row.getValue("manifest_id", ULong.class).toBigInteger());
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    ccList.setManifestId(manifestId);
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
        response.setPage(pageRequest.pageIndex());
        response.setSize(pageRequest.pageSize());
        response.setLength(count);

        return response;
    }

    public List<SortField<?>> sortFields(PageRequest pageRequest) {
        List<SortField<?>> sortFields = new ArrayList<>();

        for (Sort sort : pageRequest.sorts()) {
            Field field;
            switch (sort.field()) {
                case "type":
                    field = field("type");
                    break;
                case "state":
                    field = field("state");
                    break;
                case "den":
                    field = field("den");
                    break;
                case "valueDomain":
                    field = field("default_value_domain");
                    break;
                case "sixDigitId":
                    field = field("six_digit_id");
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
                default:
                    continue;
            }

            if (sort.direction() == DESC) {
                sortFields.add(field.desc());
            } else {
                sortFields.add(field.asc());
            }
        }

        return sortFields;
    }

    private BigInteger getManifestIdByObjectClassTermAndReleaseId(String objectClassTerm, ReleaseId releaseId) {
        return dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .where(and(
                        ACC.OBJECT_CLASS_TERM.eq(objectClassTerm),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId.value()))))
                .fetchOptionalInto(BigInteger.class).orElse(BigInteger.ZERO);
    }

    private List<BigInteger> getManifestIdsByBasedAccManifestIdAndReleaseId(
            List<BigInteger> basedManifestIds, ReleaseId releaseId) {
        return dslContext.select(ACC_MANIFEST.ACC_MANIFEST_ID)
                .from(ACC_MANIFEST)
                .where(and(
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID.in(basedManifestIds),
                        ACC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId.value()))))
                .fetchInto(BigInteger.class);
    }

    private SelectOrderByStep getAccList(CcListRequest request, Set<ReleaseSummaryRecord> releases, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        List<ULong> libraryIdList = new ArrayList();
        libraryIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.libraryId().value())).collect(Collectors.toList()));
        conditions.add(LIBRARY.LIBRARY_ID.in(libraryIdList));

        List<ULong> releaseIdList = new ArrayList();
        releaseIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.releaseId().value())).collect(Collectors.toList()));
        conditions.add(ACC_MANIFEST.RELEASE_ID.in(releaseIdList));

        ReleaseSummaryRecord requestedRelease = releases.stream()
                .filter(e -> e.releaseId().equals(request.getReleaseId())).findFirst().get();
        if (requestedRelease.isWorkingRelease()) {
            conditions.add(ACC.OAGIS_COMPONENT_TYPE.notEqual(UserExtensionGroup.getValue()));
        }
        if (request.getDeprecated() != null) {
            conditions.add(ACC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent()
                    ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull())
                    : or(RELEASE.PREV_RELEASE_ID.isNull(), ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNotNull()));
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
            conditions.add(ACC.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIdList().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIdList()));
        }
        if (!request.getUpdaterLoginIdList().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIdList()));
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
                LIBRARY.LIBRARY_ID,
                LIBRARY.NAME.as("library_name"),
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
                iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNull()), true, false).as("new_component")));
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
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ACC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY)
                .on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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

    private SelectOrderByStep getAsccList(CcListRequest request, Set<ReleaseSummaryRecord> releases, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        List<ULong> libraryIdList = new ArrayList();
        libraryIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.libraryId().value())).collect(Collectors.toList()));
        conditions.add(LIBRARY.LIBRARY_ID.in(libraryIdList));

        List<ULong> releaseIdList = new ArrayList();
        releaseIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.releaseId().value())).collect(Collectors.toList()));
        conditions.add(ASCC_MANIFEST.RELEASE_ID.in(releaseIdList));
        conditions.add(ASCC_MANIFEST.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(ASCC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent()
                    ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull())
                    : or(RELEASE.PREV_RELEASE_ID.isNull(), ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNotNull()));
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
            conditions.add(ASCC.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIdList().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIdList()));
        }
        if (!request.getUpdaterLoginIdList().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIdList()));
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
                LIBRARY.LIBRARY_ID,
                LIBRARY.NAME.as("library_name"),
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
                iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNull()), true, false).as("new_component")));
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
                .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID))
                .join(ACC_MANIFEST)
                .on(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY)
                .on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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

    private SelectOrderByStep getBccList(CcListRequest request, Set<ReleaseSummaryRecord> releases, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        List<ULong> libraryIdList = new ArrayList();
        libraryIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.libraryId().value())).collect(Collectors.toList()));
        conditions.add(LIBRARY.LIBRARY_ID.in(libraryIdList));

        List<ULong> releaseIdList = new ArrayList();
        releaseIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.releaseId().value())).collect(Collectors.toList()));
        conditions.add(BCC_MANIFEST.RELEASE_ID.in(releaseIdList));
        conditions.add(BCC_MANIFEST.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(BCC.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent()
                    ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull())
                    : or(RELEASE.PREV_RELEASE_ID.isNull(), BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNotNull()));
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
            conditions.add(BCC.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIdList().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIdList()));
        }
        if (!request.getUpdaterLoginIdList().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIdList()));
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
                LIBRARY.LIBRARY_ID,
                LIBRARY.NAME.as("library_name"),
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
                iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNull()), true, false).as("new_component")));
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
                .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID))
                .join(ACC_MANIFEST)
                .on(and(
                        BCC_MANIFEST.RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID),
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID)))
                .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                .join(LOG)
                .on(ACC_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY)
                .on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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

    private SelectOrderByStep getAsccpList(CcListRequest request, Set<ReleaseSummaryRecord> releases, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        List<ULong> libraryIdList = new ArrayList();
        libraryIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.libraryId().value())).collect(Collectors.toList()));
        conditions.add(LIBRARY.LIBRARY_ID.in(libraryIdList));

        List<ULong> releaseIdList = new ArrayList();
        releaseIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.releaseId().value())).collect(Collectors.toList()));
        conditions.add(ASCCP_MANIFEST.RELEASE_ID.in(releaseIdList));
        conditions.add(ASCCP_MANIFEST.DEN.notContains("User Extension Group"));

        if (request.getDeprecated() != null) {
            conditions.add(ASCCP.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent()
                    ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull())
                    : or(RELEASE.PREV_RELEASE_ID.isNull(), ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNotNull()));
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
            conditions.add(ASCCP.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIdList().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIdList()));
        }
        if (!request.getUpdaterLoginIdList().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIdList()));
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
                LIBRARY.LIBRARY_ID,
                LIBRARY.NAME.as("library_name"),
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
                iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNull()), true, false).as("new_component")));
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
                .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID))
                .join(ACC_MANIFEST)
                .on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID))
                .join(ACC)
                .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID))
                .join(LOG)
                .on(ASCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY)
                .on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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

    private SelectOrderByStep getBccpList(CcListRequest request, Set<ReleaseSummaryRecord> releases, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        List<ULong> libraryIdList = new ArrayList();
        libraryIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.libraryId().value())).collect(Collectors.toList()));
        conditions.add(LIBRARY.LIBRARY_ID.in(libraryIdList));

        List<ULong> releaseIdList = new ArrayList();
        releaseIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.releaseId().value())).collect(Collectors.toList()));
        conditions.add(BCCP_MANIFEST.RELEASE_ID.in(releaseIdList));
        conditions.add(BCCP_MANIFEST.DEN.notContains("User Extension Group"));
        if (request.getDeprecated() != null) {
            conditions.add(BCCP.IS_DEPRECATED.eq((byte) (request.getDeprecated() ? 1 : 0)));
        }
        if (request.getNewComponent() != null) {
            conditions.add(request.getNewComponent()
                    ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull())
                    : or(RELEASE.PREV_RELEASE_ID.isNull(), BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNotNull()));
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
            conditions.add(BCCP.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIdList().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIdList()));
        }
        if (!request.getUpdaterLoginIdList().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIdList()));
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
                LIBRARY.LIBRARY_ID,
                LIBRARY.NAME.as("library_name"),
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
                iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNull()), true, false).as("new_component")));
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
                .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID))
                .join(LOG)
                .on(BCCP_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(BCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY)
                .on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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

    public SelectOrderByStep getDtList(CcListRequest request, Set<ReleaseSummaryRecord> releases, ULong defaultModuleSetReleaseId) {
        AppUser appUserOwner = APP_USER.as("owner");
        AppUser appUserUpdater = APP_USER.as("updater");

        List<Condition> conditions = new ArrayList();
        List<ULong> libraryIdList = new ArrayList();
        libraryIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.libraryId().value())).collect(Collectors.toList()));
        conditions.add(LIBRARY.LIBRARY_ID.in(libraryIdList));

        List<ULong> releaseIdList = new ArrayList();
        releaseIdList.addAll(releases.stream().map(e -> ULong.valueOf(e.releaseId().value())).collect(Collectors.toList()));
        conditions.add(DT_MANIFEST.RELEASE_ID.in(releaseIdList));

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
            conditions.add(request.getNewComponent()
                    ? and(RELEASE.PREV_RELEASE_ID.isNotNull(), DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull())
                    : or(RELEASE.PREV_RELEASE_ID.isNull(), DT_MANIFEST.PREV_DT_MANIFEST_ID.isNotNull()));
        }
        if (request.getStates() != null && !request.getStates().isEmpty()) {
            conditions.add(DT.STATE.in(
                    request.getStates().stream().map(CcState::name).collect(Collectors.toList())));
        }
        if (!request.getOwnerLoginIdList().isEmpty()) {
            conditions.add(appUserOwner.LOGIN_ID.in(request.getOwnerLoginIdList()));
        }
        if (!request.getUpdaterLoginIdList().isEmpty()) {
            conditions.add(appUserUpdater.LOGIN_ID.in(request.getUpdaterLoginIdList()));
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
                LIBRARY.LIBRARY_ID,
                LIBRARY.NAME.as("library_name"),
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
                ifnull(CDT_PRI.NAME, "").as("default_value_domain"),
                iif(and(RELEASE.PREV_RELEASE_ID.isNotNull(), DT_MANIFEST.PREV_DT_MANIFEST_ID.isNull()), true, false).as("new_component")));
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
                .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID))
                .join(LOG)
                .on(DT_MANIFEST.LOG_ID.eq(LOG.LOG_ID))
                .join(RELEASE)
                .on(DT_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .join(LIBRARY)
                .on(RELEASE.LIBRARY_ID.eq(LIBRARY.LIBRARY_ID))
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
                .leftJoin(DT_AWD_PRI)
                .on(and(DT_MANIFEST.RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID),
                        DT_MANIFEST.DT_ID.eq(DT_AWD_PRI.DT_ID),
                        DT_AWD_PRI.IS_DEFAULT.eq((byte) 1)))
                .leftJoin(CDT_PRI)
                .on(DT_AWD_PRI.CDT_PRI_ID.eq(CDT_PRI.CDT_PRI_ID))
                .leftJoin(XBT_MANIFEST)
                .on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.XBT_MANIFEST_ID))
                .leftJoin(XBT)
                .on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                .leftJoin(CODE_LIST_MANIFEST)
                .on(DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                .leftJoin(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                .leftJoin(AGENCY_ID_LIST_MANIFEST)
                .on(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                .leftJoin(AGENCY_ID_LIST)
                .on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                .where(conditions);
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
                .select(ASCCP_MANIFEST.DEN, min(RELEASE.RELEASE_ID).as("RELEASE_ID"), RELEASE.RELEASE_NUM)
                .from(t)
                .join(RELEASE)
                .on(RELEASE.RELEASE_ID.eq(field(name("t", "release_id"), ULong.class)))
                .join(ASCCP_MANIFEST)
                .on(and(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(field(name("t", "asccp_manifest_id"), ULong.class))),
                        ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .groupBy(ASCCP_MANIFEST.DEN)
                .fetchStream().forEach(record -> {
                    String den = record.get(ASCCP_MANIFEST.DEN);
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
                .select(ASCCP_MANIFEST.DEN, min(RELEASE.RELEASE_ID).as("RELEASE_ID"), RELEASE.RELEASE_NUM)
                .from(t)
                .join(RELEASE)
                .on(RELEASE.RELEASE_ID.eq(field(name("t", "release_id"), ULong.class)))
                .join(ASCCP_MANIFEST)
                .on(and(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(field(name("t", "asccp_manifest_id"), ULong.class))),
                        ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .groupBy(ASCCP_MANIFEST.DEN)
                .fetchStream().forEach(record -> {
                    String den = record.get(ASCCP_MANIFEST.DEN);
                    String relNum = record.get(RELEASE.RELEASE_NUM);
                    sinceMap.put(den, relNum);
                });
        return sinceMap;

    }

}
