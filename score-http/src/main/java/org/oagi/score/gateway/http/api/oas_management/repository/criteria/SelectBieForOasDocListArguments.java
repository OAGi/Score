package org.oagi.score.gateway.http.api.oas_management.repository.criteria;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.bie_management.model.BieState;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.library_management.model.LibraryId;
import org.oagi.score.gateway.http.api.oas_management.model.BieForOasDoc;
import org.oagi.score.gateway.http.api.oas_management.repository.OasDocQueryRepository;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.AccessPrivilege;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.api.bie_management.model.BieState.*;
import static org.oagi.score.gateway.http.common.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Routines.levenshtein;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class SelectBieForOasDocListArguments {

    private final OasDocQueryRepository repository;

    private final List<Field> selectFields = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();
    private List<SortField<?>> sortFields = new ArrayList<>();
    private int offset = -1;
    private int numberOfRows = -1;

    private String den;
    private String type;

    public SelectBieForOasDocListArguments(OasDocQueryRepository repository) {
        this.repository = repository;

        selectFields.addAll(Arrays.asList(
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                TOP_LEVEL_ASBIEP.VERSION,
                TOP_LEVEL_ASBIEP.STATUS,
                ASBIEP.GUID,
                ASCCP_MANIFEST.DEN,
                ASCCP.PROPERTY_TERM,
                ASBIEP.DISPLAY_NAME,
                ASBIEP.REMARK,
                RELEASE.RELEASE_NUM,
                TOP_LEVEL_ASBIEP.OWNER_USER_ID,
                APP_USER.LOGIN_ID.as("owner"),
                TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP,
                APP_USER.as("updater").LOGIN_ID.as("last_update_user"),
                TOP_LEVEL_ASBIEP.STATE));
    }

    public List<Field> selectFields() {
        return this.selectFields;
    }

    public SelectBieForOasDocListArguments setOasDocId(BigInteger oasDocId) {
        if (oasDocId != null && oasDocId.longValue() > 0L) {
            conditions.add(OAS_DOC.OAS_DOC_ID.eq(ULong.valueOf(oasDocId)));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setDen(String den) {
        if (StringUtils.hasLength(den)) {
            conditions.addAll(contains(den, ASCCP_MANIFEST.DEN, ASBIEP.DISPLAY_NAME));
            selectFields.add(
                    val(1).minus(levenshtein(lower(ASCCP.PROPERTY_TERM), val(den.toLowerCase()))
                                    .div(greatest(length(ASCCP.PROPERTY_TERM), length(den))))
                            .as("score")
            );
            sortFields.add(field("score").desc());
        }
        return this;
    }

    public SelectBieForOasDocListArguments setPropertyTerm(String propertyTerm) {
        if (StringUtils.hasLength(propertyTerm)) {
            conditions.addAll(contains(propertyTerm, ASCCP.PROPERTY_TERM));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setBusinessContext(String businessContext) {
        if (StringUtils.hasLength(businessContext)) {
            conditions.add(or(Arrays.asList(businessContext.split(",")).stream().map(e -> e.trim())
                    .filter(e -> StringUtils.hasLength(e))
                    .map(e -> and(contains(e, BIZ_CTX.NAME)))
                    .collect(Collectors.toList())));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setVersion(String version) {
        if (StringUtils.hasLength(version)) {
            conditions.addAll(contains(version, TOP_LEVEL_ASBIEP.VERSION));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setRemark(String remark) {
        if (StringUtils.hasLength(remark)) {
            conditions.addAll(contains(remark, ASBIEP.REMARK));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setAsccpManifestId(AsccpManifestId asccpManifestId) {
        if (asccpManifestId != null) {
            conditions.add(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId.value())));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setExcludePropertyTerms(List<String> excludePropertyTerms) {
        if (!excludePropertyTerms.isEmpty()) {
            conditions.add(ASCCP.PROPERTY_TERM.notIn(excludePropertyTerms));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setExcludeTopLevelAsbiepIds(List<TopLevelAsbiepId> excludeTopLevelAsbiepIds) {
        if (!excludeTopLevelAsbiepIds.isEmpty()) {
            conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.notIn(
                    excludeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e.value())).collect(Collectors.toList())
            ));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setIncludeTopLevelAsbiepIds(List<TopLevelAsbiepId> includeTopLevelAsbiepIds) {
        if (!includeTopLevelAsbiepIds.isEmpty()) {
            conditions.add(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(
                    includeTopLevelAsbiepIds.stream().map(e -> ULong.valueOf(e.value())).collect(Collectors.toList())
            ));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setStates(List<BieState> states) {
        if (!states.isEmpty()) {
            conditions.add(TOP_LEVEL_ASBIEP.STATE.in(states.stream().map(e -> e.name()).collect(Collectors.toList())));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setType(String type) {
        this.type = type;
        return this;
    }

    public SelectBieForOasDocListArguments setOwnerLoginIdList(List<String> ownerLoginIdList) {
        if (!ownerLoginIdList.isEmpty()) {
            conditions.add(APP_USER.LOGIN_ID.in(ownerLoginIdList));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setUpdaterLoginIdList(List<String> updaterLoginIdList) {
        if (!updaterLoginIdList.isEmpty()) {
            conditions.add(APP_USER.as("updater").LOGIN_ID.in(updaterLoginIdList));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setUpdateDate(Date from, Date to) {
        return setUpdateDate(
                (from != null) ? new Timestamp(from.getTime()).toLocalDateTime() : null,
                (to != null) ? new Timestamp(to.getTime()).toLocalDateTime() : null
        );
    }

    public SelectBieForOasDocListArguments setUpdateDate(LocalDateTime from, LocalDateTime to) {
        if (from != null) {
            conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.greaterOrEqual(from));
        }
        if (to != null) {
            conditions.add(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP.lessThan(to));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setAccess(ULong userId, AccessPrivilege access) {
        if (access != null) {
            switch (access) {
                case CanEdit:
                    conditions.add(
                            and(
                                    TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                    TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                            )
                    );
                    break;

                case CanView:
                    conditions.add(
                            or(
                                    TOP_LEVEL_ASBIEP.STATE.in(QA.name(), Production.name()),
                                    and(
                                            TOP_LEVEL_ASBIEP.STATE.notEqual(Initiating.name()),
                                            TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(userId)
                                    )
                            )
                    );
                    break;
            }
        }
        return this;
    }

    public SelectBieForOasDocListArguments setSort(String sortKey, String direction) {
        if (StringUtils.hasLength(sortKey)) {
            Field field = null;
            SortField<?> sortField = null;
            switch (sortKey) {
                case "state":
                    field = TOP_LEVEL_ASBIEP.STATE;
                    break;

                case "branch":
                case "releaseNum":
                    field = RELEASE.RELEASE_NUM;
                    break;

                case "den":
                    field = ASCCP_MANIFEST.DEN;
                    break;

                case "topLevelAsccpPropertyTerm":
                    field = ASCCP.PROPERTY_TERM;
                    break;

                case "owner":
                    field = APP_USER.LOGIN_ID;
                    break;

                case "businessContexts":
                    field = BIZ_CTX.NAME;
                    break;

                case "version":
                    field = TOP_LEVEL_ASBIEP.VERSION;
                    break;

                case "status":
                    field = TOP_LEVEL_ASBIEP.STATUS;
                    break;

                case "remark":
                    field = ASBIEP.REMARK;
                    break;

                case "lastUpdateTimestamp":
                    field = TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP;
                    break;
            }

            if (field != null) {
                if ("asc".equals(direction)) {
                    sortField = field.asc();
                } else if ("desc".equals(direction)) {
                    sortField = field.desc();
                }
            }

            if (sortField != null) {
                this.sortFields.add(0, sortField);
            }
        }

        return this;
    }

    public SelectBieForOasDocListArguments setOffset(int offset, int numberOfRows) {
        this.offset = offset;
        this.numberOfRows = numberOfRows;
        return this;
    }

    public SelectBieForOasDocListArguments setLibraryId(LibraryId libraryId) {
        if (libraryId != null && libraryId.value().longValue() > 0) {
            conditions.add(LIBRARY.LIBRARY_ID.eq(ULong.valueOf(libraryId.value())));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setReleaseId(ReleaseId releaseId) {
        if (releaseId != null && releaseId.value().longValue() > 0) {
            conditions.add(TOP_LEVEL_ASBIEP.RELEASE_ID.eq(ULong.valueOf(releaseId.value())));
        }
        return this;
    }

    public SelectBieForOasDocListArguments setOwnedByDeveloper(Boolean ownedByDeveloper) {
        if (ownedByDeveloper != null) {
            conditions.add(APP_USER.IS_DEVELOPER.eq(ownedByDeveloper ? (byte) 1 : 0));
        }
        return this;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<SortField<?>> getSortFields() {
        return this.sortFields;
    }

    public int getOffset() {
        return offset;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public String getDen() {
        return den;
    }

    public String getType() {
        return type;
    }

    public PageResponse<BieForOasDoc> fetch() {
        return repository.selectBieForOasDocList(this);
    }

}
