package org.oagi.score.repo;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.business_term_management.data.AssignedBusinessTermListRecord;
import org.oagi.score.gateway.http.api.business_term_management.data.AssignedBusinessTermListRequest;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.businessterm.model.AssignBusinessTermRequest;
import org.oagi.score.repo.api.businessterm.model.AssignedBusinessTerm;
import org.oagi.score.repo.api.businessterm.model.BusinessTerm;
import org.oagi.score.repo.api.businessterm.model.GetAssignedBusinessTermRequest;
import org.oagi.score.repo.api.security.AccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.helper.filter.ContainsFilterBuilder.contains;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

@Repository
public class BusinessTermRepository {

    @Autowired
    private DSLContext dslContext;

    private SelectConditionStep<Record14<
            String, ULong, ULong, Byte, String,
            String, ULong, String, String, ULong,
            String, String, String, LocalDateTime>> getAsbieBiztermAssignmentList(
            AssignedBusinessTermListRequest request) {
        List<Condition> conditions = setConditions(request);
        if (request.getAssignedBizTermId() != null) {
            conditions.add(ASBIE_BIZTERM.ASBIE_BIZTERM_ID.eq(ULong.valueOf(request.getAssignedBizTermId())));
        }
        if (request.getBieId() != null) {
            conditions.add(ASBIE.ASBIE_ID.eq(ULong.valueOf(request.getBieId())));
        }
        if (StringUtils.hasLength(request.getBieDen())) {
            conditions.add(ASCC_MANIFEST.DEN.contains(request.getBieDen()));
        }
        if (request.isPrimary()) {
            conditions.add(ASBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) 1));
        }
        if (StringUtils.hasLength(request.getTypeCode())) {
            conditions.add(ASBIE_BIZTERM.TYPE_CODE.contains(request.getTypeCode()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        return dslContext.select(
                        inline("ASBIE").as("bieType"),
                        ASBIE_BIZTERM.ASBIE_BIZTERM_ID.as("assignedBizTermId"),
                        ASBIE_BIZTERM.ASBIE_ID.as("bieId"),
                        ASBIE_BIZTERM.PRIMARY_INDICATOR.as("primary"),
                        ASBIE_BIZTERM.TYPE_CODE.as("typeCode"),
                        ASCC_MANIFEST.DEN.as("den"),
                        BUSINESS_TERM.BUSINESS_TERM_ID,
                        BUSINESS_TERM.BUSINESS_TERM_,
                        BUSINESS_TERM.EXTERNAL_REF_URI.as("externalReferenceUri"),
                        RELEASE.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        APP_USER.as("appUserUpdater").LOGIN_ID.as("lastUpdatedBy"),
                        APP_USER.LOGIN_ID,
                        ASBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.as("lastUpdateTimestamp"))
                .from(ASBIE_BIZTERM)
                .join(ASCC_BIZTERM).on(ASBIE_BIZTERM.ASCC_BIZTERM_ID.eq(ASCC_BIZTERM.ASCC_BIZTERM_ID))
                .join(ASCC).on(ASCC_BIZTERM.ASCC_ID.eq(ASCC.ASCC_ID))
                .join(BUSINESS_TERM).on(and(
                        ASCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID)
                ))
//               next 3 joins to get release information
                .join(ASBIE).on(ASBIE_BIZTERM.ASBIE_ID.eq(ASBIE.ASBIE_ID))
                .join(ASCC_MANIFEST).on(ASBIE.BASED_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID))
                .join(RELEASE).on(ASCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
//              next joins to get user information
                .join(APP_USER.as("appUserUpdater"))
                .on(ASBIE_BIZTERM.LAST_UPDATED_BY.eq(APP_USER.as("appUserUpdater").APP_USER_ID))
                .join(APP_USER)
                .on(ASBIE_BIZTERM.CREATED_BY.eq(APP_USER.APP_USER_ID))
                .where(conditions);
    }

    private SelectConditionStep<Record14<
            String, ULong, ULong, Byte, String,
            String, ULong, String, String, ULong,
            String, String, String, LocalDateTime>> getBbieBiztermAssignmentList(AssignedBusinessTermListRequest request) {
        List<Condition> conditions = setConditions(request);
        if (request.getAssignedBizTermId() != null) {
            conditions.add(BBIE_BIZTERM.BBIE_BIZTERM_ID.eq(ULong.valueOf(request.getAssignedBizTermId())));
        }
        if (request.getBieId() != null) {
            conditions.add(BBIE.BBIE_ID.eq(ULong.valueOf(request.getBieId())));
        }
        if (StringUtils.hasLength(request.getBieDen())) {
            if (StringUtils.hasLength(request.getBieDen())) {
                conditions.add(BCC_MANIFEST.DEN.contains(request.getBieDen()));
            }
        }
        if (request.isPrimary()) {
            conditions.add(BBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) 1));
        }
        if (StringUtils.hasLength(request.getTypeCode())) {
            conditions.add(BBIE_BIZTERM.TYPE_CODE.contains(request.getTypeCode()));
        }
        if (request.getUpdateStartDate() != null) {
            conditions.add(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.greaterOrEqual(new Timestamp(request.getUpdateStartDate().getTime()).toLocalDateTime()));
        }
        if (request.getUpdateEndDate() != null) {
            conditions.add(BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.lessThan(new Timestamp(request.getUpdateEndDate().getTime()).toLocalDateTime()));
        }

        return dslContext.select(
                        inline("BBIE").as("bieType"),
                        BBIE_BIZTERM.BBIE_BIZTERM_ID.as("assignedBizTermId"),
                        BBIE_BIZTERM.BBIE_ID.as("bieId"),
                        BBIE_BIZTERM.PRIMARY_INDICATOR.as("primary"),
                        BBIE_BIZTERM.TYPE_CODE.as("typeCode"),
                        BCC_MANIFEST.DEN.as("den"),
                        BUSINESS_TERM.BUSINESS_TERM_ID,
                        BUSINESS_TERM.BUSINESS_TERM_,
                        BUSINESS_TERM.EXTERNAL_REF_URI.as("externalReferenceUri"),
                        RELEASE.RELEASE_ID,
                        RELEASE.RELEASE_NUM,
                        APP_USER.as("appUserUpdater").LOGIN_ID.as("lastUpdatedBy"),
                        APP_USER.LOGIN_ID,
                        BBIE_BIZTERM.LAST_UPDATE_TIMESTAMP.as("lastUpdateTimestamp"))
                .from(BBIE_BIZTERM)
                .join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                .join(BCC).on(BCC_BIZTERM.BCC_ID.eq(BCC.BCC_ID))
                .join(BUSINESS_TERM).on(and(
                        BCC_BIZTERM.BUSINESS_TERM_ID.eq(BUSINESS_TERM.BUSINESS_TERM_ID)
                ))
//               next 3 joins to get release information
                .join(BBIE).on(BBIE_BIZTERM.BBIE_ID.eq(BBIE.BBIE_ID))
                .join(BCC_MANIFEST).on(BBIE.BASED_BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID))
                .join(RELEASE).on(BCC_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
//              next joins to get user information
                .join(APP_USER.as("appUserUpdater"))
                .on(BBIE_BIZTERM.LAST_UPDATED_BY.eq(APP_USER.as("appUserUpdater").APP_USER_ID))
                .join(APP_USER)
                .on(BBIE_BIZTERM.CREATED_BY.eq(APP_USER.APP_USER_ID))
                .where(conditions);
    }

    private List<Condition> setConditions(AssignedBusinessTermListRequest request) {
        List<Condition> conditions = new ArrayList<>();
        if (StringUtils.hasLength(request.getBusinessTerm())) {
            conditions.addAll(contains(request.getBusinessTerm(), BUSINESS_TERM.BUSINESS_TERM_));
        }
        if (StringUtils.hasLength(request.getBusinessContext())) {
            conditions.addAll(contains(request.getBusinessContext(), BIZ_CTX.NAME));
        }
        if (StringUtils.hasLength(request.getExternalReferenceUri())) {
            conditions.addAll(contains(request.getExternalReferenceUri(), BUSINESS_TERM.EXTERNAL_REF_URI));
        }
        if (request.getOwnerLoginIds() != null && !request.getOwnerLoginIds().isEmpty()) {
            conditions.add(APP_USER.LOGIN_ID.in(request.getOwnerLoginIds()));
        }
        if (request.getOwnerLoginIds() != null && !request.getUpdaterLoginIds().isEmpty()) {
            conditions.add(APP_USER.as("appUserUpdater").LOGIN_ID.in(request.getUpdaterLoginIds()));
        }
        return conditions;
    }

    public <E> PaginationResponse<E> getBieBiztermList(AssignedBusinessTermListRequest request,
                                                       Class<? extends E> type) {

        SelectOrderByStep select = null;
        if (request.getBieTypes().contains("ASBIE")) {
            select = getAsbieBiztermAssignmentList(request);
        }
        if (request.getBieTypes().contains("BBIE")) {
            select = (select != null) ? select.union(getBbieBiztermAssignmentList(request)) :
                    getBbieBiztermAssignmentList(request);
        }

        int pageCount = dslContext.fetchCount(select);
        Optional<SortField> sortField = setSort(request.getPageRequest().getSortActive(),
                request.getPageRequest().getSortDirection());
        SelectWithTiesAfterOffsetStep offsetStep = null;
        if (sortField.isPresent()) {
            if (request.getPageRequest().getOffset() >= 0 && request.getPageRequest().getPageSize() >= 0) {
                offsetStep = select.orderBy(sortField.get()).limit(request.getPageRequest().getOffset(),
                        request.getPageRequest().getPageSize());
            }
        } else {
            if (request.getPageRequest().getOffset() >= 0 && request.getPageRequest().getPageSize() >= 0) {
                offsetStep = select.limit(request.getPageRequest().getOffset(), request.getPageRequest().getPageSize());
            }
        }

        return new PaginationResponse<>(pageCount,
                (offsetStep != null) ? offsetStep.fetchInto(type) : select.fetchInto(type));
    }

    public Optional<SortField> setSort(String field, String direction) {
        Optional<Field> sortField = Optional.empty();
        if (StringUtils.hasLength(field)) {
            switch (field) {
                case "bieType":
                    sortField = Optional.of(field("bieType"));
                    break;

                case "bieDen":
                    sortField = Optional.of(field("den"));
                    break;

                case "businessTerm":
                    sortField = Optional.of(BUSINESS_TERM.BUSINESS_TERM_);
                    break;

                case "primary":
                    sortField = Optional.of(field("primary"));
                    break;

                case "lastUpdateTimestamp":
                    sortField = Optional.of(field("lastUpdateTimestamp"));
                    break;

                default:
                    return Optional.empty();
            }
        }
        return (direction.equals("asc")) ? Optional.of(sortField.get().asc()) : Optional.of(sortField.get().desc());

    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public AssignedBusinessTerm getBusinessTermAssignment(
            GetAssignedBusinessTermRequest request) throws ScoreDataAccessException {

        AssignedBusinessTermListRecord assignedBusinessTermRecord = null;

        AssignedBusinessTermListRequest listRequest = new AssignedBusinessTermListRequest();
        listRequest.setAssignedBizTermId(request.getAssignedBizTermId());
        listRequest.setBieTypes(Collections.singletonList(request.getBieType()));
        if (listRequest.getAssignedBizTermId() != null && request.getBieType() != null) {
            if (request.getBieType().equals("ASBIE")) {
                assignedBusinessTermRecord = getAsbieBiztermAssignmentList(listRequest)
                        .fetchInto(AssignedBusinessTermListRecord.class)
                        .get(0);
            } else if (request.getBieType().equals("BBIE")) {
                assignedBusinessTermRecord = getBbieBiztermAssignmentList(listRequest)
                        .fetchInto(AssignedBusinessTermListRecord.class)
                        .get(0);
            } else throw new ScoreDataAccessException("Wrong BIE Type: " + request.getBieType());
        }
        AssignedBusinessTerm assignedBusinessTerm = new AssignedBusinessTerm(
                assignedBusinessTermRecord.getAssignedBizTermId(),
                assignedBusinessTermRecord.getBieId(),
                assignedBusinessTermRecord.getBieType(),
                assignedBusinessTermRecord.isPrimary(),
                assignedBusinessTermRecord.getTypeCode(),
                assignedBusinessTermRecord.getDen(),
                assignedBusinessTermRecord.getBusinessTermId(),
                assignedBusinessTermRecord.getBusinessTerm(),
                assignedBusinessTermRecord.getExternalReferenceUri(),
                null,
                assignedBusinessTermRecord.getOwner(),
                assignedBusinessTermRecord.getLastUpdatedBy());
        return assignedBusinessTerm;
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public boolean checkAssignmentUniqueness(AssignBusinessTermRequest assignBusinessTermRequest)
            throws ScoreDataAccessException {

        if (assignBusinessTermRequest.getBusinessTermId() != null
                && assignBusinessTermRequest.getBiesToAssign() != null
                && assignBusinessTermRequest.getBiesToAssign().size() == 1) {
            boolean isUnique = assignBusinessTermRequest.getBiesToAssign().stream().map(bieToAssign -> {
                if (bieToAssign.getBieType().equals("ASBIE")) {
                    List<Condition> conditions = new ArrayList<>();
                    conditions.add(and(ASBIE_BIZTERM.ASBIE_ID.eq(ULong.valueOf(bieToAssign.getBieId())),
                            ASCC_BIZTERM.BUSINESS_TERM_ID.eq(ULong.valueOf(assignBusinessTermRequest.getBusinessTermId())),
                            ((StringUtils.hasLength(assignBusinessTermRequest.getTypeCode())) ?
                                    ASBIE_BIZTERM.TYPE_CODE.eq(assignBusinessTermRequest.getTypeCode()) :
                                    or(ASBIE_BIZTERM.TYPE_CODE.isNull(), ASBIE_BIZTERM.TYPE_CODE.eq(""))),
                            ASBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) (assignBusinessTermRequest.isPrimary() ? 1 : 0))));
                    return dslContext.selectCount()
                            .from(ASBIE_BIZTERM)
                            .join(ASCC_BIZTERM).on(ASBIE_BIZTERM.ASCC_BIZTERM_ID.eq(ASCC_BIZTERM.ASCC_BIZTERM_ID))
                            .where(conditions)
                            .fetchOneInto(Integer.class) == 0;
                } else if (bieToAssign.getBieType().equals("BBIE")) {
                    List<Condition> conditions = new ArrayList<>();
                    conditions.add(and(BBIE_BIZTERM.BBIE_ID.eq(ULong.valueOf(bieToAssign.getBieId())),
                            BCC_BIZTERM.BUSINESS_TERM_ID.eq(ULong.valueOf(assignBusinessTermRequest.getBusinessTermId())),
                            ((StringUtils.hasLength(assignBusinessTermRequest.getTypeCode())) ?
                                    BBIE_BIZTERM.TYPE_CODE.eq(assignBusinessTermRequest.getTypeCode()) :
                                    or(BBIE_BIZTERM.TYPE_CODE.isNull(), BBIE_BIZTERM.TYPE_CODE.eq(""))),
                            BBIE_BIZTERM.PRIMARY_INDICATOR.eq((byte) (assignBusinessTermRequest.isPrimary() ? 1 : 0))));
                    return dslContext.selectCount()
                            .from(BBIE_BIZTERM)
                            .join(BCC_BIZTERM).on(BBIE_BIZTERM.BCC_BIZTERM_ID.eq(BCC_BIZTERM.BCC_BIZTERM_ID))
                            .where(conditions)
                            .fetchOneInto(Integer.class) == 0;
                } else throw new ScoreDataAccessException("Wrong BIE type: " + bieToAssign.getBieType());
            }).allMatch(isUniqueRecord -> isUniqueRecord);
            return isUnique;
        } else {
            throw new ScoreDataAccessException("Wrong input data");
        }
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public boolean checkBusinessTermUniqueness(BusinessTerm businessTerm)
            throws ScoreDataAccessException {

        if (businessTerm.getBusinessTerm() != null && businessTerm.getExternalReferenceUri() != null) {
            List<Condition> conditions = new ArrayList<>();
            if (businessTerm.getDefinition() != null && businessTerm.getDefinition() != "") {
                conditions.add(and(BUSINESS_TERM.BUSINESS_TERM_.eq(businessTerm.getBusinessTerm()),
                        BUSINESS_TERM.EXTERNAL_REF_URI.eq(businessTerm.getExternalReferenceUri()),
                        BUSINESS_TERM.DEFINITION.eq(businessTerm.getDefinition())));
            } else {
                conditions.add(and(BUSINESS_TERM.BUSINESS_TERM_.eq(businessTerm.getBusinessTerm()),
                        BUSINESS_TERM.EXTERNAL_REF_URI.eq(businessTerm.getExternalReferenceUri())));
            }
            if (businessTerm.getBusinessTermId() != null) {
                conditions.add(BUSINESS_TERM.BUSINESS_TERM_ID.ne(ULong.valueOf(businessTerm.getBusinessTermId())));
            }
            return dslContext.selectCount()
                    .from(BUSINESS_TERM)
                    .where(conditions)
                    .fetchOneInto(Integer.class) == 0;
        } else
            throw new ScoreDataAccessException("Wrong input data");
    }

    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public boolean checkBusinessTermNameUniqueness(BusinessTerm businessTerm)
            throws ScoreDataAccessException {

        List<Condition> conditions = new ArrayList<>();
        conditions.add(BUSINESS_TERM.BUSINESS_TERM_.eq(businessTerm.getBusinessTerm()));
        if (businessTerm.getBusinessTermId() != null) {
            conditions.add(BUSINESS_TERM.BUSINESS_TERM_ID.ne(ULong.valueOf(businessTerm.getBusinessTermId())));
        }

        if (businessTerm != null) {
            return dslContext.selectCount()
                    .from(BUSINESS_TERM)
                    .where(conditions)
                    .fetchOneInto(Integer.class) == 0;
        } else
            throw new ScoreDataAccessException("Wrong input data");
    }

}
