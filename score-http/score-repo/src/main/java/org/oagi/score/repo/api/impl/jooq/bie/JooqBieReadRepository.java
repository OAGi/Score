package org.oagi.score.repo.api.impl.jooq.bie;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.bie.BieReadRepository;
import org.oagi.score.repo.api.bie.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqBieReadRepository
        extends JooqScoreRepository
        implements BieReadRepository {

    public JooqBieReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private SelectOnConditionStep selectTopLevelAsbiep() {
        return dslContext().select(
                TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID,
                TOP_LEVEL_ASBIEP.ASBIEP_ID,
                TOP_LEVEL_ASBIEP.RELEASE_ID,
                ASCCP.PROPERTY_TERM,
                ASCCP.GUID,
                TOP_LEVEL_ASBIEP.STATE,
                TOP_LEVEL_ASBIEP.STATUS,
                TOP_LEVEL_ASBIEP.VERSION,
                APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                ASBIEP.CREATION_TIMESTAMP,
                TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(APP_USER.as("owner")).on(TOP_LEVEL_ASBIEP.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(ASBIEP.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, TopLevelAsbiep> mapperTopLevelAsbiep() {
        return record -> {
            TopLevelAsbiep topLevelAsbiep = new TopLevelAsbiep();
            topLevelAsbiep.setTopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
            topLevelAsbiep.setAsbiepId(record.get(TOP_LEVEL_ASBIEP.ASBIEP_ID).toBigInteger());
            topLevelAsbiep.setReleaseId(record.get(TOP_LEVEL_ASBIEP.RELEASE_ID).toBigInteger());
            topLevelAsbiep.setPropertyTerm(record.get(ASCCP.PROPERTY_TERM));
            topLevelAsbiep.setGuid(record.get(ASCCP.GUID));
            topLevelAsbiep.setState(BieState.valueOf(record.get(TOP_LEVEL_ASBIEP.STATE)));
            topLevelAsbiep.setStatus(record.get(TOP_LEVEL_ASBIEP.STATUS));
            topLevelAsbiep.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
            topLevelAsbiep.setOwner(new ScoreUser(
                    record.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(),
                    record.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")),
                    (byte) 1 == record.get(APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer")) ? DEVELOPER : END_USER
            ));
            topLevelAsbiep.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            topLevelAsbiep.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            topLevelAsbiep.setCreationTimestamp(
                    Date.from(record.get(ASBIEP.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            topLevelAsbiep.setLastUpdateTimestamp(
                    Date.from(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return topLevelAsbiep;
        };
    }

    private SelectJoinStep selectAbie() {
        return dslContext().select(ABIE.ABIE_ID,
                ABIE.GUID,
                ABIE.BASED_ACC_MANIFEST_ID,
                ABIE.PATH,
                ABIE.HASH_PATH,
                ABIE.DEFINITION,
                ABIE.REMARK,
                ABIE.BIZ_TERM,
                ABIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ABIE);
    }

    private RecordMapper<Record, Abie> mapperAbie() {
        return record -> {
            Abie abie = new Abie();
            abie.setAbieId(record.get(ABIE.ABIE_ID).toBigInteger());
            abie.setGuid(record.get(ABIE.GUID));
            abie.setBasedAccManifestId(record.get(ABIE.BASED_ACC_MANIFEST_ID).toBigInteger());
            abie.setPath(record.get(ABIE.PATH));
            abie.setHashPath(record.get(ABIE.HASH_PATH));
            abie.setDefinition(record.get(ABIE.DEFINITION));
            abie.setRemark(record.get(ABIE.REMARK));
            abie.setBizTerm(record.get(ABIE.BIZ_TERM));
            abie.setOwnerTopLevelAsbiepId(record.get(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
            return abie;
        };
    }

    private SelectJoinStep selectAsbie() {
        return dslContext().select(ASBIE.ASBIE_ID,
                ASBIE.GUID,
                ASBIE.BASED_ASCC_MANIFEST_ID,
                ASBIE.PATH,
                ASBIE.HASH_PATH,
                ASBIE.FROM_ABIE_ID,
                ASBIE.TO_ASBIEP_ID,
                ASBIE.CARDINALITY_MIN,
                ASBIE.CARDINALITY_MAX,
                ASBIE.IS_NILLABLE,
                ASBIE.IS_USED,
                ASBIE.DEFINITION,
                ASBIE.REMARK,
                ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ASBIE);
    }

    private RecordMapper<Record, Asbie> mapperAsbie() {
        return record -> {
            Asbie asbie = new Asbie();
            asbie.setAsbieId(record.get(ASBIE.ASBIE_ID).toBigInteger());
            asbie.setGuid(record.get(ASBIE.GUID));
            asbie.setBasedAsccManifestId(record.get(ASBIE.BASED_ASCC_MANIFEST_ID).toBigInteger());
            asbie.setPath(record.get(ASBIE.PATH));
            asbie.setHashPath(record.get(ASBIE.HASH_PATH));
            asbie.setFromAbieId(record.get(ASBIE.FROM_ABIE_ID).toBigInteger());
            asbie.setToAsbiepId(record.get(ASBIE.TO_ASBIEP_ID).toBigInteger());
            asbie.setCardinalityMin(record.get(ASBIE.CARDINALITY_MIN));
            asbie.setCardinalityMax(record.get(ASBIE.CARDINALITY_MAX));
            asbie.setNillable((byte) 1 == record.get(ASBIE.IS_NILLABLE));
            asbie.setUsed((byte) 1 == record.get(ASBIE.IS_USED));
            asbie.setDefinition(record.get(ASBIE.DEFINITION));
            asbie.setRemark(record.get(ASBIE.REMARK));
            asbie.setOwnerTopLevelAsbiepId(record.get(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
            return asbie;
        };
    }

    private SelectJoinStep selectBbie() {
        return dslContext().select(BBIE.BBIE_ID,
                BBIE.GUID,
                BBIE.BASED_BCC_MANIFEST_ID,
                BBIE.PATH,
                BBIE.HASH_PATH,
                BBIE.FROM_ABIE_ID,
                BBIE.TO_BBIEP_ID,
                BBIE.BDT_PRI_RESTRI_ID,
                BBIE.CODE_LIST_ID,
                BBIE.AGENCY_ID_LIST_ID,
                BBIE.CARDINALITY_MIN,
                BBIE.CARDINALITY_MAX,
                BBIE.DEFAULT_VALUE,
                BBIE.FIXED_VALUE,
                BBIE.IS_NILLABLE,
                BBIE.IS_NULL,
                BBIE.IS_USED,
                BBIE.DEFINITION,
                BBIE.REMARK,
                BBIE.EXAMPLE,
                BBIE.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(BBIE);
    }

    private RecordMapper<Record, Bbie> mapperBbie() {
        return record -> {
            Bbie bbie = new Bbie();
            bbie.setBbieId(record.get(BBIE.BBIE_ID).toBigInteger());
            bbie.setGuid(record.get(BBIE.GUID));
            bbie.setBasedBccManifestId(record.get(BBIE.BASED_BCC_MANIFEST_ID).toBigInteger());
            bbie.setPath(record.get(BBIE.PATH));
            bbie.setHashPath(record.get(BBIE.HASH_PATH));
            bbie.setFromAbieId(record.get(BBIE.FROM_ABIE_ID).toBigInteger());
            bbie.setToBbiepId(record.get(BBIE.TO_BBIEP_ID).toBigInteger());
            bbie.setBdtPriRestriId((record.get(BBIE.BDT_PRI_RESTRI_ID) != null) ?
                    record.get(BBIE.BDT_PRI_RESTRI_ID).toBigInteger() : null);
            bbie.setCodeListId((record.get(BBIE.CODE_LIST_ID) != null) ?
                    record.get(BBIE.CODE_LIST_ID).toBigInteger() : null);
            bbie.setAgencyIdListId((record.get(BBIE.AGENCY_ID_LIST_ID) != null) ?
                    record.get(BBIE.AGENCY_ID_LIST_ID).toBigInteger() : null);
            bbie.setCardinalityMin(record.get(BBIE.CARDINALITY_MIN));
            bbie.setCardinalityMax(record.get(BBIE.CARDINALITY_MAX));
            bbie.setDefaultValue(record.get(BBIE.DEFAULT_VALUE));
            bbie.setFixedValue(record.get(BBIE.FIXED_VALUE));
            bbie.setNillable((byte) 1 == record.get(BBIE.IS_NILLABLE));
            bbie.setUsed((byte) 1 == record.get(BBIE.IS_USED));
            bbie.setDefinition(record.get(BBIE.DEFINITION));
            bbie.setRemark(record.get(BBIE.REMARK));
            bbie.setExample(record.get(BBIE.EXAMPLE));
            bbie.setOwnerTopLevelAsbiepId(record.get(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
            return bbie;
        };
    }

    private SelectJoinStep selectAsbiep() {
        return dslContext().select(ASBIEP.ASBIEP_ID,
                ASBIEP.GUID,
                ASBIEP.BASED_ASCCP_MANIFEST_ID,
                ASBIEP.PATH,
                ASBIEP.HASH_PATH,
                ASBIEP.ROLE_OF_ABIE_ID,
                ASBIEP.DEFINITION,
                ASBIEP.REMARK,
                ASBIEP.BIZ_TERM,
                ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(ASBIEP);
    }

    private RecordMapper<Record, Asbiep> mapperAsbiep() {
        return record -> {
            Asbiep asbiep = new Asbiep();
            asbiep.setAsbiepId(record.get(ASBIEP.ASBIEP_ID).toBigInteger());
            asbiep.setGuid(record.get(ASBIEP.GUID));
            asbiep.setBasedAsccpManifestId(record.get(ASBIEP.BASED_ASCCP_MANIFEST_ID).toBigInteger());
            asbiep.setPath(record.get(ASBIEP.PATH));
            asbiep.setHashPath(record.get(ASBIEP.HASH_PATH));
            asbiep.setRoleOfAbieId(record.get(ASBIEP.ROLE_OF_ABIE_ID).toBigInteger());
            asbiep.setDefinition(record.get(ASBIEP.DEFINITION));
            asbiep.setRemark(record.get(ASBIEP.REMARK));
            asbiep.setBizTerm(record.get(ASBIEP.BIZ_TERM));
            asbiep.setOwnerTopLevelAsbiepId(record.get(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
            return asbiep;
        };
    }

    private SelectJoinStep selectBbiep() {
        return dslContext().select(BBIEP.BBIEP_ID,
                BBIEP.GUID,
                BBIEP.BASED_BCCP_MANIFEST_ID,
                BBIEP.PATH,
                BBIEP.HASH_PATH,
                BBIEP.DEFINITION,
                BBIEP.REMARK,
                BBIEP.BIZ_TERM,
                BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(BBIEP);
    }

    private RecordMapper<Record, Bbiep> mapperBbiep() {
        return record -> {
            Bbiep bbiep = new Bbiep();
            bbiep.setBbiepId(record.get(BBIEP.BBIEP_ID).toBigInteger());
            bbiep.setGuid(record.get(BBIEP.GUID));
            bbiep.setBasedBccpManifestId(record.get(BBIEP.BASED_BCCP_MANIFEST_ID).toBigInteger());
            bbiep.setPath(record.get(BBIEP.PATH));
            bbiep.setHashPath(record.get(BBIEP.HASH_PATH));
            bbiep.setDefinition(record.get(BBIEP.DEFINITION));
            bbiep.setRemark(record.get(BBIEP.REMARK));
            bbiep.setBizTerm(record.get(BBIEP.BIZ_TERM));
            bbiep.setOwnerTopLevelAsbiepId(record.get(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
            return bbiep;
        };
    }

    private SelectJoinStep selectBbieSc() {
        return dslContext().select(BBIE_SC.BBIE_SC_ID,
                BBIE_SC.GUID,
                BBIE_SC.BASED_DT_SC_MANIFEST_ID,
                BBIE_SC.PATH,
                BBIE_SC.HASH_PATH,
                BBIE_SC.BBIE_ID,
                BBIE_SC.DT_SC_PRI_RESTRI_ID,
                BBIE_SC.CODE_LIST_ID,
                BBIE_SC.AGENCY_ID_LIST_ID,
                BBIE_SC.CARDINALITY_MIN,
                BBIE_SC.CARDINALITY_MAX,
                BBIE_SC.DEFAULT_VALUE,
                BBIE_SC.FIXED_VALUE,
                BBIE_SC.IS_USED,
                BBIE_SC.DEFINITION,
                BBIE_SC.BIZ_TERM,
                BBIE_SC.REMARK,
                BBIE_SC.EXAMPLE,
                BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID)
                .from(BBIE_SC);
    }

    private RecordMapper<Record, BbieSc> mapperBbieSc() {
        return record -> {
            BbieSc bbieSc = new BbieSc();
            bbieSc.setBbieScId(record.get(BBIE_SC.BBIE_SC_ID).toBigInteger());
            bbieSc.setGuid(record.get(BBIE_SC.GUID));
            bbieSc.setBasedDtScManifestId(record.get(BBIE_SC.BASED_DT_SC_MANIFEST_ID).toBigInteger());
            bbieSc.setBbieId(record.get(BBIE_SC.BBIE_ID).toBigInteger());
            bbieSc.setPath(record.get(BBIE_SC.PATH));
            bbieSc.setHashPath(record.get(BBIE_SC.HASH_PATH));
            bbieSc.setDtScPriRestriId((record.get(BBIE_SC.DT_SC_PRI_RESTRI_ID) != null) ?
                    record.get(BBIE_SC.DT_SC_PRI_RESTRI_ID).toBigInteger() : null);
            bbieSc.setCodeListId((record.get(BBIE_SC.CODE_LIST_ID) != null) ?
                    record.get(BBIE_SC.CODE_LIST_ID).toBigInteger() : null);
            bbieSc.setAgencyIdListId((record.get(BBIE_SC.AGENCY_ID_LIST_ID) != null) ?
                    record.get(BBIE_SC.AGENCY_ID_LIST_ID).toBigInteger() : null);
            bbieSc.setCardinalityMin(record.get(BBIE_SC.CARDINALITY_MIN));
            bbieSc.setCardinalityMax(record.get(BBIE_SC.CARDINALITY_MAX));
            bbieSc.setDefaultValue(record.get(BBIE_SC.DEFAULT_VALUE));
            bbieSc.setFixedValue(record.get(BBIE_SC.FIXED_VALUE));
            bbieSc.setUsed((byte) 1 == record.get(BBIE_SC.IS_USED));
            bbieSc.setDefinition(record.get(BBIE_SC.DEFINITION));
            bbieSc.setBizTerm(record.get(BBIE_SC.BIZ_TERM));
            bbieSc.setRemark(record.get(BBIE_SC.REMARK));
            bbieSc.setExample(record.get(BBIE_SC.EXAMPLE));
            bbieSc.setOwnerTopLevelAsbiepId(record.get(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID).toBigInteger());
            return bbieSc;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetBiePackageResponse getBiePackage(GetBiePackageRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        BigInteger topLevelAsbiepId = request.getTopLevelAsbiepId();

        TopLevelAsbiep topLevelAsbiep = (TopLevelAsbiep) selectTopLevelAsbiep()
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .fetchOne(mapperTopLevelAsbiep());

        if (topLevelAsbiep.getState() == BieState.WIP) {
            if (!topLevelAsbiep.getOwner().equals(requester)) {
                throw new ScoreDataAccessException();
            }
        }

        BiePackage biePackage = new BiePackage();
        biePackage.setTopLevelAsbiep(topLevelAsbiep);

        if (topLevelAsbiep.getState() != BieState.Initiating) {
            List<Condition> conditions;

            biePackage.setAbieList(selectAbie()
                            .where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                            .fetch(mapperAbie())
            );

            conditions = new ArrayList();
            conditions.add(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)));
            if (request.isUsed()) {
                conditions.add(ASBIE.IS_USED.eq((byte) 1));
            }
            biePackage.setAsbieList(selectAsbie()
                    .where(conditions)
                    .fetch(mapperAsbie())
            );

            conditions = new ArrayList();
            conditions.add(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)));
            if (request.isUsed()) {
                conditions.add(BBIE.IS_USED.eq((byte) 1));
            }
            biePackage.setBbieList(selectBbie()
                    .where(conditions)
                    .fetch(mapperBbie())
            );

            biePackage.setAsbiepList(selectAsbiep()
                    .where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                    .fetch(mapperAsbiep())
            );

            biePackage.setBbiepList(selectBbiep()
                    .where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                    .fetch(mapperBbiep())
            );

            conditions = new ArrayList();
            conditions.add(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)));
            if (request.isUsed()) {
                conditions.add(BBIE_SC.IS_USED.eq((byte) 1));
            }
            biePackage.setBbieScList(selectBbieSc()
                    .where(conditions)
                    .fetch(mapperBbieSc())
            );
        }

        return new GetBiePackageResponse(biePackage);
    }

    @Override
    public GetReuseBieListResponse getReuseBieList(
            GetReuseBieListRequest request) throws ScoreDataAccessException {
        ULong topLevelAsbiepId = ULong.valueOf(request.getTopLevelAsbiepId());
        List<ULong> reuseTopLevelAsbiepIdList = getReuseTopLevelAsbiepList(topLevelAsbiepId, request.isReusedBie());
        List<TopLevelAsbiep> reuseTopLevelAsbiepList = (!reuseTopLevelAsbiepIdList.isEmpty()) ?
                selectTopLevelAsbiep()
                        .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.in(reuseTopLevelAsbiepIdList))
                        .fetch(mapperTopLevelAsbiep()) :
                Collections.emptyList();
        return new GetReuseBieListResponse(reuseTopLevelAsbiepList);
    }

    private List<ULong> getReuseTopLevelAsbiepList(ULong topLevelAsbiepId, boolean isReusedBie) {
        List<Condition> conds = new ArrayList();
        if (isReusedBie) {
            return dslContext().selectDistinct(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                    .from(ASBIE)
                    .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                    .join(TOP_LEVEL_ASBIEP).on(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .where(and(
                            ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(topLevelAsbiepId),
                            ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId)
                    ))
                    .fetchInto(ULong.class);
        } else {
            return dslContext().selectDistinct(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                    .from(ASBIE)
                    .join(ASBIEP).on(ASBIE.TO_ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                    .join(TOP_LEVEL_ASBIEP).on(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID))
                    .where(and(
                            ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(topLevelAsbiepId),
                            ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.notEqual(topLevelAsbiepId)
                    ))
                    .fetchInto(ULong.class);
        }
    }

    @Override
    public GetAssignedBusinessContextResponse getAssignedBusinessContext(
            GetAssignedBusinessContextRequest request) throws ScoreDataAccessException {

        BigInteger topLevelAsbiepId = request.getTopLevelAsbiepId();
        List<BigInteger> bizCtxIds = dslContext().select(BIZ_CTX_ASSIGNMENT.BIZ_CTX_ID)
                .from(BIZ_CTX_ASSIGNMENT)
                .where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .fetchInto(BigInteger.class);

        return new GetAssignedBusinessContextResponse(bizCtxIds);
    }

}
