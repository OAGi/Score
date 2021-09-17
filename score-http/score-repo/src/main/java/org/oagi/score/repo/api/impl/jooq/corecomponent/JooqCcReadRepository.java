package org.oagi.score.repo.api.impl.jooq.corecomponent;

import org.jooq.*;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.CcReadRepository;
import org.oagi.score.repo.api.corecomponent.model.*;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repo.api.security.AccessControl;
import org.oagi.score.repo.api.user.model.ScoreUser;

import javax.print.DocFlavor;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;
import static org.oagi.score.repo.api.user.model.ScoreRole.DEVELOPER;
import static org.oagi.score.repo.api.user.model.ScoreRole.END_USER;

public class JooqCcReadRepository
        extends JooqScoreRepository
        implements CcReadRepository {

    public JooqCcReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    private ULong getReleaseIdByAsccpManifestId(ULong asccpManifestId) {
        return dslContext().select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .join(ASCCP_MANIFEST).on(RELEASE.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))
                .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(asccpManifestId))
                .fetchOptionalInto(ULong.class).orElse(null);
    }

    private class CcManifestList {
        private Map<ULong, AccManifestRecord> accManifestRecordMap;
        private Map<ULong, AsccManifestRecord> asccManifestRecordMap;
        private Map<ULong, BccManifestRecord> bccManifestRecordMap;
        private Map<ULong, AsccpManifestRecord> asccpManifestRecordMap;
        private Map<ULong, BccpManifestRecord> bccpManifestRecordMap;
        private Map<ULong, DtManifestRecord> dtManifestRecordMap;
        private Map<ULong, DtScManifestRecord> dtScManifestRecordMap;

        void fillOut(CcMap ccMap, AsccpManifestRecord asccpManifestRecord) {
            if (asccpManifestRecord == null) {
                return;
            }

            if (asccpManifestRecordMap == null) {
                asccpManifestRecordMap = new HashMap();
            }
            if (asccpManifestRecordMap.containsKey(asccpManifestRecord.getAsccpManifestId())) {
                return;
            }
            asccpManifestRecordMap.put(asccpManifestRecord.getAsccpManifestId(), asccpManifestRecord);

            AccManifestRecord accManifestRecord =
                    ccMap.accManifestRecordMap.get(asccpManifestRecord.getRoleOfAccManifestId());
            fillOut(ccMap, accManifestRecord);
        }

        void fillOut(CcMap ccMap, BccpManifestRecord bccpManifestRecord) {
            if (bccpManifestRecord == null) {
                return;
            }

            if (bccpManifestRecordMap == null) {
                bccpManifestRecordMap = new HashMap();
            }
            if (bccpManifestRecordMap.containsKey(bccpManifestRecord.getBccpManifestId())) {
                return;
            }
            bccpManifestRecordMap.put(bccpManifestRecord.getBccpManifestId(), bccpManifestRecord);

            DtManifestRecord dtManifestRecord =
                    ccMap.dtManifestRecordMap.get(bccpManifestRecord.getBdtManifestId());
            fillOut(ccMap, dtManifestRecord);
        }

        void fillOut(CcMap ccMap, DtManifestRecord dtManifestRecord) {
            if (dtManifestRecord == null) {
                return;
            }

            if (dtManifestRecordMap == null) {
                dtManifestRecordMap = new HashMap();
            }
            if (dtManifestRecordMap.containsKey(dtManifestRecord.getDtManifestId())) {
                return;
            }
            dtManifestRecordMap.put(dtManifestRecord.getDtManifestId(), dtManifestRecord);

            List<DtScManifestRecord> dtScManifestRecordList =
                    ccMap.dtScManifestRecordMap.getOrDefault(dtManifestRecord.getDtManifestId(), Collections.emptyList());
            if (dtScManifestRecordMap == null) {
                dtScManifestRecordMap = new HashMap();
            }
            dtScManifestRecordList.forEach(dtScManifestRecord -> {
                dtScManifestRecordMap.putIfAbsent(dtScManifestRecord.getDtScManifestId(), dtScManifestRecord);
            });
        }

        void fillOut(CcMap ccMap, AccManifestRecord accManifestRecord) {
            if (accManifestRecord == null) {
                return;
            }

            if (accManifestRecordMap == null) {
                accManifestRecordMap = new HashMap();
            }
            if (accManifestRecordMap.containsKey(accManifestRecord.getAccManifestId())) {
                return;
            }
            accManifestRecordMap.put(accManifestRecord.getAccManifestId(), accManifestRecord);

            if (accManifestRecord.getBasedAccManifestId() != null) {
                AccManifestRecord basedAccManifestRecord =
                        ccMap.accManifestRecordMap.get(accManifestRecord.getBasedAccManifestId());
                fillOut(ccMap, basedAccManifestRecord);
            }

            List<AsccManifestRecord> asccManifestRecordList =
                    ccMap.asccManifestRecordMap.getOrDefault(accManifestRecord.getAccManifestId(), Collections.emptyList());
            if (asccManifestRecordMap == null) {
                asccManifestRecordMap = new HashMap();
            }
            asccManifestRecordList.forEach(asccManifestRecord -> {
                asccManifestRecordMap.putIfAbsent(asccManifestRecord.getAsccManifestId(), asccManifestRecord);

                AsccpManifestRecord asccpManifestRecord =
                        ccMap.asccpManifestRecordMap.get(asccManifestRecord.getToAsccpManifestId());
                fillOut(ccMap, asccpManifestRecord);
            });

            List<BccManifestRecord> bccManifestRecordList =
                    ccMap.bccManifestRecordMap.getOrDefault(accManifestRecord.getAccManifestId(), Collections.emptyList());
            if (bccManifestRecordMap == null) {
                bccManifestRecordMap = new HashMap();
            }
            bccManifestRecordList.forEach(bccManifestRecord -> {
                bccManifestRecordMap.putIfAbsent(bccManifestRecord.getBccManifestId(), bccManifestRecord);

                BccpManifestRecord bccpManifestRecord =
                        ccMap.bccpManifestRecordMap.get(bccManifestRecord.getToBccpManifestId());
                fillOut(ccMap, bccpManifestRecord);
            });
        }
    }

    private class CcMap {

        Map<ULong, AccManifestRecord> accManifestRecordMap;
        Map<ULong, List<AsccManifestRecord>> asccManifestRecordMap;
        Map<ULong, List<BccManifestRecord>> bccManifestRecordMap;
        Map<ULong, AsccpManifestRecord> asccpManifestRecordMap;
        Map<ULong, BccpManifestRecord> bccpManifestRecordMap;
        Map<ULong, DtManifestRecord> dtManifestRecordMap;
        Map<ULong, List<DtScManifestRecord>> dtScManifestRecordMap;

        CcMap(DSLContext dslContext, ULong releaseId) {
            accManifestRecordMap = dslContext.selectFrom(ACC_MANIFEST)
                    .where(ACC_MANIFEST.RELEASE_ID.eq(releaseId))
                    .fetchStream().collect(Collectors.toMap(AccManifestRecord::getAccManifestId, Function.identity()));

            asccManifestRecordMap = dslContext.selectFrom(ASCC_MANIFEST)
                    .where(ASCC_MANIFEST.RELEASE_ID.eq(releaseId))
                    .fetchStream().collect(Collectors.groupingBy(AsccManifestRecord::getFromAccManifestId));

            bccManifestRecordMap = dslContext.selectFrom(BCC_MANIFEST)
                    .where(BCC_MANIFEST.RELEASE_ID.eq(releaseId))
                    .fetchStream().collect(Collectors.groupingBy(BccManifestRecord::getFromAccManifestId));

            asccpManifestRecordMap = dslContext.selectFrom(ASCCP_MANIFEST)
                    .where(ASCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                    .fetchStream().collect(Collectors.toMap(AsccpManifestRecord::getAsccpManifestId, Function.identity()));

            bccpManifestRecordMap = dslContext.selectFrom(BCCP_MANIFEST)
                    .where(BCCP_MANIFEST.RELEASE_ID.eq(releaseId))
                    .fetchStream().collect(Collectors.toMap(BccpManifestRecord::getBccpManifestId, Function.identity()));

            dtManifestRecordMap = dslContext.selectFrom(DT_MANIFEST)
                    .where(DT_MANIFEST.RELEASE_ID.eq(releaseId))
                    .fetchStream().collect(Collectors.toMap(DtManifestRecord::getDtManifestId, Function.identity()));

            dtScManifestRecordMap = dslContext.selectFrom(DT_SC_MANIFEST)
                    .where(DT_SC_MANIFEST.RELEASE_ID.eq(releaseId))
                    .fetchStream().collect(Collectors.groupingBy(DtScManifestRecord::getOwnerDtManifestId));
        }
    }

    private RecordMapper<Record, AccManifest> mapperAccManifest() {
        return record -> {
            AccManifest accManifest = new AccManifest();
            accManifest.setAccManifestId(record.get(ACC_MANIFEST.ACC_MANIFEST_ID).toBigInteger());
            accManifest.setReleaseId(record.get(ACC_MANIFEST.RELEASE_ID).toBigInteger());
            accManifest.setAccId(record.get(ACC_MANIFEST.ACC_ID).toBigInteger());
            if (record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID) != null) {
                accManifest.setBasedAccManifestId(record.get(ACC_MANIFEST.BASED_ACC_MANIFEST_ID).toBigInteger());
            }
            accManifest.setConflict((byte) 1 == record.get(ACC_MANIFEST.CONFLICT));
            accManifest.setLogId(record.get(ACC_MANIFEST.LOG_ID).toBigInteger());
            if (record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID) != null) {
                accManifest.setPrevAccManifestId(record.get(ACC_MANIFEST.PREV_ACC_MANIFEST_ID).toBigInteger());
            }
            if (record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID) != null) {
                accManifest.setNextAccManifestId(record.get(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID).toBigInteger());
            }
            return accManifest;
        };
    }

    private RecordMapper<Record, AsccManifest> mapperAsccManifest() {
        return record -> {
            AsccManifest asccManifest = new AsccManifest();
            asccManifest.setAsccManifestId(record.get(ASCC_MANIFEST.ASCC_MANIFEST_ID).toBigInteger());
            asccManifest.setReleaseId(record.get(ASCC_MANIFEST.RELEASE_ID).toBigInteger());
            asccManifest.setAsccId(record.get(ASCC_MANIFEST.ASCC_ID).toBigInteger());
            asccManifest.setSeqKeyId(record.get(ASCC_MANIFEST.SEQ_KEY_ID).toBigInteger());
            asccManifest.setFromAccManifestId(record.get(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID).toBigInteger());
            asccManifest.setToAsccpManifestId(record.get(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID).toBigInteger());
            asccManifest.setConflict((byte) 1 == record.get(ASCC_MANIFEST.CONFLICT));
            if (record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID) != null) {
                asccManifest.setPrevAsccManifestId(record.get(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID).toBigInteger());
            }
            if (record.get(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID) != null) {
                asccManifest.setNextAsccManifestId(record.get(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID).toBigInteger());
            }
            return asccManifest;
        };
    }

    private RecordMapper<Record, BccManifest> mapperBccManifest() {
        return record -> {
            BccManifest bccManifest = new BccManifest();
            bccManifest.setBccManifestId(record.get(BCC_MANIFEST.BCC_MANIFEST_ID).toBigInteger());
            bccManifest.setReleaseId(record.get(BCC_MANIFEST.RELEASE_ID).toBigInteger());
            bccManifest.setBccId(record.get(BCC_MANIFEST.BCC_ID).toBigInteger());
            bccManifest.setSeqKeyId(record.get(BCC_MANIFEST.SEQ_KEY_ID).toBigInteger());
            bccManifest.setFromAccManifestId(record.get(BCC_MANIFEST.FROM_ACC_MANIFEST_ID).toBigInteger());
            bccManifest.setToBccpManifestId(record.get(BCC_MANIFEST.TO_BCCP_MANIFEST_ID).toBigInteger());
            bccManifest.setConflict((byte) 1 == record.get(BCC_MANIFEST.CONFLICT));
            if (record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID) != null) {
                bccManifest.setPrevBccManifestId(record.get(BCC_MANIFEST.PREV_BCC_MANIFEST_ID).toBigInteger());
            }
            if (record.get(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID) != null) {
                bccManifest.setNextBccManifestId(record.get(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID).toBigInteger());
            }
            return bccManifest;
        };
    }

    private RecordMapper<Record, CcAssociationSequence> mapperSequence() {
        return record -> {
            CcAssociationSequence sequence = new CcAssociationSequence();
            sequence.setSeqKeyId(record.get(SEQ_KEY.SEQ_KEY_ID).toBigInteger());
            sequence.setFromAccManifestId(record.get(SEQ_KEY.FROM_ACC_MANIFEST_ID).toBigInteger());
            if (record.get(SEQ_KEY.ASCC_MANIFEST_ID) != null) {
                sequence.setAsccManifestId(record.get(SEQ_KEY.ASCC_MANIFEST_ID).toBigInteger());
            }
            if (record.get(SEQ_KEY.BCC_MANIFEST_ID) != null) {
                sequence.setBccManifestId(record.get(SEQ_KEY.BCC_MANIFEST_ID).toBigInteger());
            }
            if (record.get(SEQ_KEY.PREV_SEQ_KEY_ID) != null) {
                sequence.setPrevSeqKeyId(record.get(SEQ_KEY.PREV_SEQ_KEY_ID).toBigInteger());
            }
            if (record.get(SEQ_KEY.NEXT_SEQ_KEY_ID) != null) {
                sequence.setNextSeqKeyId(record.get(SEQ_KEY.NEXT_SEQ_KEY_ID).toBigInteger());
            }
            return sequence;
        };
    }

    private RecordMapper<Record, AsccpManifest> mapperAsccpManifest() {
        return record -> {
            AsccpManifest asccpManifest = new AsccpManifest();
            asccpManifest.setAsccpManifestId(record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID).toBigInteger());
            asccpManifest.setReleaseId(record.get(ASCCP_MANIFEST.RELEASE_ID).toBigInteger());
            asccpManifest.setAsccpId(record.get(ASCCP_MANIFEST.ASCCP_ID).toBigInteger());
            asccpManifest.setRoleOfAccManifestId(record.get(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID).toBigInteger());
            asccpManifest.setConflict((byte) 1 == record.get(ASCCP_MANIFEST.CONFLICT));
            asccpManifest.setLogId(record.get(ASCCP_MANIFEST.LOG_ID).toBigInteger());
            if (record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID) != null) {
                asccpManifest.setPrevAsccpManifestId(record.get(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID).toBigInteger());
            }
            if (record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID) != null) {
                asccpManifest.setNextAsccpManifestId(record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID).toBigInteger());
            }
            return asccpManifest;
        };
    }

    private RecordMapper<Record, BccpManifest> mapperBccpManifest() {
        return record -> {
            BccpManifest bccpManifest = new BccpManifest();
            bccpManifest.setBccpManifestId(record.get(BCCP_MANIFEST.BCCP_MANIFEST_ID).toBigInteger());
            bccpManifest.setReleaseId(record.get(BCCP_MANIFEST.RELEASE_ID).toBigInteger());
            bccpManifest.setBccpId(record.get(BCCP_MANIFEST.BCCP_ID).toBigInteger());
            bccpManifest.setBdtManifestId(record.get(BCCP_MANIFEST.BDT_MANIFEST_ID).toBigInteger());
            bccpManifest.setConflict((byte) 1 == record.get(BCCP_MANIFEST.CONFLICT));
            bccpManifest.setLogId(record.get(BCCP_MANIFEST.LOG_ID).toBigInteger());
            if (record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID) != null) {
                bccpManifest.setPrevBccpManifestId(record.get(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID).toBigInteger());
            }
            if (record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID) != null) {
                bccpManifest.setNextBccpManifestId(record.get(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID).toBigInteger());
            }
            return bccpManifest;
        };
    }

    private RecordMapper<Record, DtManifest> mapperDtManifest() {
        return record -> {
            DtManifest dtManifest = new DtManifest();
            dtManifest.setDtManifestId(record.get(DT_MANIFEST.DT_MANIFEST_ID).toBigInteger());
            dtManifest.setReleaseId(record.get(DT_MANIFEST.RELEASE_ID).toBigInteger());
            dtManifest.setDtId(record.get(DT_MANIFEST.DT_ID).toBigInteger());
            if (record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID) != null) {
                dtManifest.setBasedDtManifestId(record.get(DT_MANIFEST.BASED_DT_MANIFEST_ID).toBigInteger());
            }
            dtManifest.setConflict((byte) 1 == record.get(DT_MANIFEST.CONFLICT));
            dtManifest.setLogId(record.get(DT_MANIFEST.LOG_ID).toBigInteger());
            if (record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID) != null) {
                dtManifest.setPrevDtManifestId(record.get(DT_MANIFEST.PREV_DT_MANIFEST_ID).toBigInteger());
            }
            if (record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID) != null) {
                dtManifest.setNextDtManifestId(record.get(DT_MANIFEST.NEXT_DT_MANIFEST_ID).toBigInteger());
            }
            return dtManifest;
        };
    }

    private RecordMapper<Record, DtScManifest> mapperDtScManifest() {
        return record -> {
            DtScManifest dtScManifest = new DtScManifest();
            dtScManifest.setDtScManifestId(record.get(DT_SC_MANIFEST.DT_SC_MANIFEST_ID).toBigInteger());
            dtScManifest.setReleaseId(record.get(DT_SC_MANIFEST.RELEASE_ID).toBigInteger());
            dtScManifest.setDtScId(record.get(DT_SC_MANIFEST.DT_SC_ID).toBigInteger());
            dtScManifest.setOwnerDtManifestId(record.get(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID).toBigInteger());
            dtScManifest.setConflict((byte) 1 == record.get(DT_SC_MANIFEST.CONFLICT));
            if (record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID) != null) {
                dtScManifest.setPrevDtScManifestId(record.get(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID).toBigInteger());
            }
            if (record.get(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID) != null) {
                dtScManifest.setNextDtScManifestId(record.get(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID).toBigInteger());
            }
            return dtScManifest;
        };
    }

    private SelectOnConditionStep selectAcc() {
        return dslContext().select(
                ACC.ACC_ID,
                ACC.GUID,
                ACC.TYPE,
                ACC.OBJECT_CLASS_TERM,
                ACC.DEN,
                ACC.DEFINITION,
                ACC.DEFINITION_SOURCE,
                ACC.OBJECT_CLASS_QUALIFIER,
                ACC.OAGIS_COMPONENT_TYPE,
                ACC.NAMESPACE_ID,
                ACC.STATE,
                ACC.IS_DEPRECATED,
                ACC.IS_ABSTRACT,
                ACC.PREV_ACC_ID,
                ACC.NEXT_ACC_ID,
                APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                ACC.CREATION_TIMESTAMP,
                ACC.LAST_UPDATE_TIMESTAMP)
                .from(ACC)
                .join(APP_USER.as("owner")).on(ACC.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(ACC.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(ACC.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, Acc> mapperAcc() {
        return record -> {
            Acc acc = new Acc();
            acc.setAccId(record.get(ACC.ACC_ID).toBigInteger());
            acc.setGuid(record.get(ACC.GUID));
            acc.setType(record.get(ACC.TYPE));
            acc.setObjectClassTerm(record.get(ACC.OBJECT_CLASS_TERM));
            acc.setDen(record.get(ACC.DEN));
            acc.setDefinition(record.get(ACC.DEFINITION));
            acc.setDefinitionSource(record.get(ACC.DEFINITION_SOURCE));
            acc.setObjectClassQualifier(record.get(ACC.OBJECT_CLASS_QUALIFIER));
            acc.setOagisComponentType(OagisComponentType.valueOf(record.get(ACC.OAGIS_COMPONENT_TYPE)));
            if (record.get(ACC.NAMESPACE_ID) != null) {
                acc.setNamespaceId(record.get(ACC.NAMESPACE_ID).toBigInteger());
            }
            acc.setState(CcState.valueOf(record.get(ACC.STATE)));
            acc.setDeprecated((byte) 1 == record.get(ACC.IS_DEPRECATED));
            acc.setAbstract((byte) 1 == record.get(ACC.IS_ABSTRACT));
            if (record.get(ACC.PREV_ACC_ID) != null) {
                acc.setPrevAccId(record.get(ACC.PREV_ACC_ID).toBigInteger());
            }
            if (record.get(ACC.NEXT_ACC_ID) != null) {
                acc.setNextAccId(record.get(ACC.NEXT_ACC_ID).toBigInteger());
            }
            acc.setOwner(new ScoreUser(
                    record.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(),
                    record.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")),
                    (byte) 1 == record.get(APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer")) ? DEVELOPER : END_USER
            ));
            acc.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            acc.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            acc.setCreationTimestamp(
                    Date.from(record.get(ACC.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            acc.setLastUpdateTimestamp(
                    Date.from(record.get(ACC.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return acc;
        };
    }

    private SelectJoinStep selectAscc() {
        return dslContext().select(
                ASCC.ASCC_ID,
                ASCC.GUID,
                ASCC.CARDINALITY_MIN,
                ASCC.CARDINALITY_MAX,
                ASCC.DEN,
                ASCC.DEFINITION,
                ASCC.DEFINITION_SOURCE,
                ASCC.IS_DEPRECATED,
                ASCC.PREV_ASCC_ID,
                ASCC.NEXT_ASCC_ID)
                .from(ASCC);
    }

    private RecordMapper<Record, Ascc> mapperAscc() {
        return record -> {
            Ascc ascc = new Ascc();
            ascc.setAsccId(record.get(ASCC.ASCC_ID).toBigInteger());
            ascc.setGuid(record.get(ASCC.GUID));
            ascc.setCardinalityMin(record.get(ASCC.CARDINALITY_MIN));
            ascc.setCardinalityMax(record.get(ASCC.CARDINALITY_MAX));
            ascc.setDen(record.get(ASCC.DEN));
            ascc.setDefinition(record.get(ASCC.DEFINITION));
            ascc.setDefinitionSource(record.get(ASCC.DEFINITION_SOURCE));
            ascc.setDeprecated((byte) 1 == record.get(ASCC.IS_DEPRECATED));
            if (record.get(ASCC.PREV_ASCC_ID) != null) {
                ascc.setPrevAsccId(record.get(ASCC.PREV_ASCC_ID).toBigInteger());
            }
            if (record.get(ASCC.NEXT_ASCC_ID) != null) {
                ascc.setNextAsccId(record.get(ASCC.NEXT_ASCC_ID).toBigInteger());
            }
            return ascc;
        };
    }

    private SelectJoinStep selectBcc() {
        return dslContext().select(
                BCC.BCC_ID,
                BCC.GUID,
                BCC.CARDINALITY_MIN,
                BCC.CARDINALITY_MAX,
                BCC.ENTITY_TYPE,
                BCC.DEN,
                BCC.DEFINITION,
                BCC.DEFINITION_SOURCE,
                BCC.IS_DEPRECATED,
                BCC.IS_NILLABLE,
                BCC.DEFAULT_VALUE,
                BCC.FIXED_VALUE,
                BCC.PREV_BCC_ID,
                BCC.NEXT_BCC_ID)
                .from(BCC);
    }

    private RecordMapper<Record, Bcc> mapperBcc() {
        return record -> {
            Bcc bcc = new Bcc();
            bcc.setBccId(record.get(BCC.BCC_ID).toBigInteger());
            bcc.setGuid(record.get(BCC.GUID));
            bcc.setCardinalityMin(record.get(BCC.CARDINALITY_MIN));
            bcc.setCardinalityMax(record.get(BCC.CARDINALITY_MAX));
            bcc.setEntityType(EntityType.valueOf(record.get(BCC.ENTITY_TYPE)));
            bcc.setDen(record.get(BCC.DEN));
            bcc.setDefinition(record.get(BCC.DEFINITION));
            bcc.setDefinitionSource(record.get(BCC.DEFINITION_SOURCE));
            bcc.setDeprecated((byte) 1 == record.get(BCC.IS_DEPRECATED));
            bcc.setNillable((byte) 1 == record.get(BCC.IS_NILLABLE));
            bcc.setDefaultValue(record.get(BCC.DEFAULT_VALUE));
            bcc.setFixedValue(record.get(BCC.FIXED_VALUE));
            if (record.get(BCC.PREV_BCC_ID) != null) {
                bcc.setPrevBccId(record.get(BCC.PREV_BCC_ID).toBigInteger());
            }
            if (record.get(BCC.NEXT_BCC_ID) != null) {
                bcc.setNextBccId(record.get(BCC.NEXT_BCC_ID).toBigInteger());
            }
            return bcc;
        };
    }

    private SelectOnConditionStep selectAsccp() {
        return dslContext().select(
                ASCCP.ASCCP_ID,
                ASCCP.GUID,
                ASCCP.TYPE,
                ASCCP.PROPERTY_TERM,
                ASCCP.DEN,
                ASCCP.DEFINITION,
                ASCCP.DEFINITION_SOURCE,
                ASCCP.NAMESPACE_ID,
                ASCCP.STATE,
                ASCCP.IS_DEPRECATED,
                ASCCP.REUSABLE_INDICATOR,
                ASCCP.IS_NILLABLE,
                ASCCP.PREV_ASCCP_ID,
                ASCCP.NEXT_ASCCP_ID,
                APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                ASCCP.CREATION_TIMESTAMP,
                ASCCP.LAST_UPDATE_TIMESTAMP)
                .from(ASCCP)
                .join(APP_USER.as("owner")).on(ASCCP.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(ASCCP.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(ASCCP.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, Asccp> mapperAsccp() {
        return record -> {
            Asccp asccp = new Asccp();
            asccp.setAsccpId(record.get(ASCCP.ASCCP_ID).toBigInteger());
            asccp.setGuid(record.get(ASCCP.GUID));
            asccp.setType(record.get(ASCCP.TYPE));
            asccp.setPropertyTerm(record.get(ASCCP.PROPERTY_TERM));
            asccp.setDen(record.get(ASCCP.DEN));
            asccp.setDefinition(record.get(ASCCP.DEFINITION));
            asccp.setDefinitionSource(record.get(ASCCP.DEFINITION_SOURCE));
            if (record.get(ASCCP.NAMESPACE_ID) != null) {
                asccp.setNamespaceId(record.get(ASCCP.NAMESPACE_ID).toBigInteger());
            }
            asccp.setState(CcState.valueOf(record.get(ASCCP.STATE)));
            asccp.setDeprecated((byte) 1 == record.get(ASCCP.IS_DEPRECATED));
            asccp.setReusable((byte) 1 == record.get(ASCCP.REUSABLE_INDICATOR));
            asccp.setNillable((byte) 1 == record.get(ASCCP.IS_NILLABLE));
            if (record.get(ASCCP.PREV_ASCCP_ID) != null) {
                asccp.setPrevAsccpId(record.get(ASCCP.PREV_ASCCP_ID).toBigInteger());
            }
            if (record.get(ASCCP.NEXT_ASCCP_ID) != null) {
                asccp.setNextAsccpId(record.get(ASCCP.NEXT_ASCCP_ID).toBigInteger());
            }
            asccp.setOwner(new ScoreUser(
                    record.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(),
                    record.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")),
                    (byte) 1 == record.get(APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer")) ? DEVELOPER : END_USER
            ));
            asccp.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            asccp.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            asccp.setCreationTimestamp(
                    Date.from(record.get(ASCCP.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            asccp.setLastUpdateTimestamp(
                    Date.from(record.get(ASCCP.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return asccp;
        };
    }

    private SelectOnConditionStep selectBccp() {
        return dslContext().select(
                BCCP.BCCP_ID,
                BCCP.GUID,
                BCCP.PROPERTY_TERM,
                BCCP.REPRESENTATION_TERM,
                BCCP.DEN,
                BCCP.DEFINITION,
                BCCP.DEFINITION_SOURCE,
                BCCP.NAMESPACE_ID,
                BCCP.STATE,
                BCCP.IS_DEPRECATED,
                BCCP.IS_NILLABLE,
                BCCP.DEFAULT_VALUE,
                BCCP.FIXED_VALUE,
                BCCP.PREV_BCCP_ID,
                BCCP.NEXT_BCCP_ID,
                APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                BCCP.CREATION_TIMESTAMP,
                BCCP.LAST_UPDATE_TIMESTAMP)
                .from(BCCP)
                .join(APP_USER.as("owner")).on(BCCP.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(BCCP.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(BCCP.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, Bccp> mapperBccp() {
        return record -> {
            Bccp bccp = new Bccp();
            bccp.setBccpId(record.get(BCCP.BCCP_ID).toBigInteger());
            bccp.setGuid(record.get(BCCP.GUID));
            bccp.setPropertyTerm(record.get(BCCP.PROPERTY_TERM));
            bccp.setRepresentationTerm(record.get(BCCP.REPRESENTATION_TERM));
            bccp.setDen(record.get(BCCP.DEN));
            bccp.setDefinition(record.get(BCCP.DEFINITION));
            bccp.setDefinitionSource(record.get(BCCP.DEFINITION_SOURCE));
            if (record.get(BCCP.NAMESPACE_ID) != null) {
                bccp.setNamespaceId(record.get(BCCP.NAMESPACE_ID).toBigInteger());
            }
            bccp.setState(CcState.valueOf(record.get(BCCP.STATE)));
            bccp.setDeprecated((byte) 1 == record.get(BCCP.IS_DEPRECATED));
            bccp.setNillable((byte) 1 == record.get(BCCP.IS_NILLABLE));
            bccp.setDefaultValue(record.get(BCCP.DEFAULT_VALUE));
            bccp.setFixedValue(record.get(BCCP.FIXED_VALUE));
            if (record.get(BCCP.PREV_BCCP_ID) != null) {
                bccp.setPrevBccpId(record.get(BCCP.PREV_BCCP_ID).toBigInteger());
            }
            if (record.get(BCCP.NEXT_BCCP_ID) != null) {
                bccp.setNextBccpId(record.get(BCCP.NEXT_BCCP_ID).toBigInteger());
            }
            bccp.setOwner(new ScoreUser(
                    record.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(),
                    record.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")),
                    (byte) 1 == record.get(APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer")) ? DEVELOPER : END_USER
            ));
            bccp.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            bccp.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            bccp.setCreationTimestamp(
                    Date.from(record.get(BCCP.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            bccp.setLastUpdateTimestamp(
                    Date.from(record.get(BCCP.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return bccp;
        };
    }

    private SelectOnConditionStep selectDt() {
        return dslContext().select(
                DT.DT_ID,
                DT.GUID,
                DT.TYPE,
                DT.VERSION_NUM,
                DT.DATA_TYPE_TERM,
                DT.QUALIFIER,
                DT.DEN,
                DT.DEFINITION,
                DT.DEFINITION_SOURCE,
                DT.CONTENT_COMPONENT_DEN,
                DT.CONTENT_COMPONENT_DEFINITION,
                DT.NAMESPACE_ID,
                DT.STATE,
                DT.IS_DEPRECATED,
                DT.COMMONLY_USED,
                DT.PREV_DT_ID,
                DT.NEXT_DT_ID,
                APP_USER.as("owner").APP_USER_ID.as("owner_user_id"),
                APP_USER.as("owner").LOGIN_ID.as("owner_login_id"),
                APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer"),
                APP_USER.as("creator").APP_USER_ID.as("creator_user_id"),
                APP_USER.as("creator").LOGIN_ID.as("creator_login_id"),
                APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer"),
                APP_USER.as("updater").APP_USER_ID.as("updater_user_id"),
                APP_USER.as("updater").LOGIN_ID.as("updater_login_id"),
                APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer"),
                DT.CREATION_TIMESTAMP,
                DT.LAST_UPDATE_TIMESTAMP)
                .from(DT)
                .join(APP_USER.as("owner")).on(DT.OWNER_USER_ID.eq(APP_USER.as("owner").APP_USER_ID))
                .join(APP_USER.as("creator")).on(DT.CREATED_BY.eq(APP_USER.as("creator").APP_USER_ID))
                .join(APP_USER.as("updater")).on(DT.LAST_UPDATED_BY.eq(APP_USER.as("updater").APP_USER_ID));
    }

    private RecordMapper<Record, Dt> mapperDt() {
        return record -> {
            Dt dt = new Dt();
            dt.setDtId(record.get(DT.DT_ID).toBigInteger());
            dt.setGuid(record.get(DT.GUID));
            dt.setType(DtType.valueOf(record.get(DT.TYPE)));
            dt.setVersionNum(record.get(DT.VERSION_NUM));
            dt.setDataTypeTerm(record.get(DT.DATA_TYPE_TERM));
            dt.setQualifier(record.get(DT.QUALIFIER));
            dt.setDen(record.get(DT.DEN));
            dt.setDefinition(record.get(DT.DEFINITION));
            dt.setDefinitionSource(record.get(DT.DEFINITION_SOURCE));
            dt.setContentComponentDen(record.get(DT.CONTENT_COMPONENT_DEN));
            dt.setContentComponentDefinition(record.get(DT.CONTENT_COMPONENT_DEFINITION));
            if (record.get(DT.NAMESPACE_ID) != null) {
                dt.setNamespaceId(record.get(DT.NAMESPACE_ID).toBigInteger());
            }
            dt.setState(CcState.valueOf(record.get(DT.STATE)));
            dt.setDeprecated((byte) 1 == record.get(DT.IS_DEPRECATED));
            dt.setCommonlyUsed((byte) 1 == record.get(DT.COMMONLY_USED));
            if (record.get(DT.PREV_DT_ID) != null) {
                dt.setPrevDtId(record.get(DT.PREV_DT_ID).toBigInteger());
            }
            if (record.get(DT.NEXT_DT_ID) != null) {
                dt.setNextDtId(record.get(DT.NEXT_DT_ID).toBigInteger());
            }
            dt.setOwner(new ScoreUser(
                    record.get(APP_USER.as("owner").APP_USER_ID.as("owner_user_id")).toBigInteger(),
                    record.get(APP_USER.as("owner").LOGIN_ID.as("owner_login_id")),
                    (byte) 1 == record.get(APP_USER.as("owner").IS_DEVELOPER.as("owner_is_developer")) ? DEVELOPER : END_USER
            ));
            dt.setCreatedBy(new ScoreUser(
                    record.get(APP_USER.as("creator").APP_USER_ID.as("creator_user_id")).toBigInteger(),
                    record.get(APP_USER.as("creator").LOGIN_ID.as("creator_login_id")),
                    (byte) 1 == record.get(APP_USER.as("creator").IS_DEVELOPER.as("creator_is_developer")) ? DEVELOPER : END_USER
            ));
            dt.setLastUpdatedBy(new ScoreUser(
                    record.get(APP_USER.as("updater").APP_USER_ID.as("updater_user_id")).toBigInteger(),
                    record.get(APP_USER.as("updater").LOGIN_ID.as("updater_login_id")),
                    (byte) 1 == record.get(APP_USER.as("updater").IS_DEVELOPER.as("updater_is_developer")) ? DEVELOPER : END_USER
            ));
            dt.setCreationTimestamp(
                    Date.from(record.get(DT.CREATION_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            dt.setLastUpdateTimestamp(
                    Date.from(record.get(DT.LAST_UPDATE_TIMESTAMP).atZone(ZoneId.systemDefault()).toInstant()));
            return dt;
        };
    }

    private SelectJoinStep selectDtSc() {
        return dslContext().select(
                DT_SC.DT_SC_ID,
                DT_SC.GUID,
                DT_SC.PROPERTY_TERM,
                DT_SC.REPRESENTATION_TERM,
                DT_SC.CARDINALITY_MIN,
                DT_SC.CARDINALITY_MAX,
                DT_SC.DEFINITION,
                DT_SC.DEFINITION_SOURCE,
                DT_SC.IS_DEPRECATED,
                DT_SC.DEFAULT_VALUE,
                DT_SC.FIXED_VALUE)
                .from(DT_SC);
    }

    private RecordMapper<Record, DtSc> mapperDtSc() {
        return record -> {
            DtSc dtSc = new DtSc();
            dtSc.setDtScId(record.get(DT_SC.DT_SC_ID).toBigInteger());
            dtSc.setGuid(record.get(DT_SC.GUID));
            dtSc.setPropertyTerm(record.get(DT_SC.PROPERTY_TERM));
            dtSc.setRepresentationTerm(record.get(DT_SC.REPRESENTATION_TERM));
            dtSc.setCardinalityMin(record.get(DT_SC.CARDINALITY_MIN));
            dtSc.setCardinalityMax(record.get(DT_SC.CARDINALITY_MAX));
            dtSc.setDefinition(record.get(DT_SC.DEFINITION));
            dtSc.setDefinitionSource(record.get(DT_SC.DEFINITION_SOURCE));
            dtSc.setDeprecated((byte) 1 == record.get(DT_SC.IS_DEPRECATED));
            dtSc.setDefaultValue(record.get(DT_SC.DEFAULT_VALUE));
            dtSc.setFixedValue(record.get(DT_SC.FIXED_VALUE));
            return dtSc;
        };
    }

    private SelectJoinStep selectBdtPriRestri() {
        return dslContext().select(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID,
                BDT_PRI_RESTRI.BDT_ID,
                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_PRI_RESTRI.CODE_LIST_ID,
                BDT_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_PRI_RESTRI.IS_DEFAULT,
                XBT.NAME, XBT.XBT_ID)
                .from(BDT_PRI_RESTRI)
                .leftJoin(CDT_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID));
    }

    private RecordMapper<Record, BdtPriRestri> mapperBdtPriRestri() {
        return record -> {
            BdtPriRestri bdtPriRestri = new BdtPriRestri();
            bdtPriRestri.setBdtPriRestriId(record.get(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID).toBigInteger());
            bdtPriRestri.setBdtId(record.get(BDT_PRI_RESTRI.BDT_ID).toBigInteger());
            if (record.get(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID) != null) {
                bdtPriRestri.setCdtAwdPriXpsTypeMapId(record.get(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID).toBigInteger());
                bdtPriRestri.setXbtId(record.get(XBT.XBT_ID).toBigInteger());
                bdtPriRestri.setXbtName(record.get(XBT.NAME));
            }
            if (record.get(BDT_PRI_RESTRI.CODE_LIST_ID) != null) {
                bdtPriRestri.setCodeListId(record.get(BDT_PRI_RESTRI.CODE_LIST_ID).toBigInteger());
            }
            if (record.get(BDT_PRI_RESTRI.AGENCY_ID_LIST_ID) != null) {
                bdtPriRestri.setAgencyIdListId(record.get(BDT_PRI_RESTRI.AGENCY_ID_LIST_ID).toBigInteger());
            }
            bdtPriRestri.setDefault(record.get(BDT_PRI_RESTRI.IS_DEFAULT) == (byte) 1);
            return bdtPriRestri;
        };
    }

    private SelectJoinStep selectBdtScPriRestri() {
        return dslContext().select(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID,
                BDT_SC_PRI_RESTRI.BDT_SC_ID,
                BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_SC_PRI_RESTRI.CODE_LIST_ID,
                BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_SC_PRI_RESTRI.IS_DEFAULT,
                XBT.XBT_ID, XBT.NAME)
                .from(BDT_SC_PRI_RESTRI)
                .leftJoin(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID));
    }

    private RecordMapper<Record, BdtScPriRestri> mapperBdtScPriRestri() {
        return record -> {
            BdtScPriRestri bdtScPriRestri = new BdtScPriRestri();
            bdtScPriRestri.setBdtScPriRestriId(record.get(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID).toBigInteger());
            bdtScPriRestri.setBdtScId(record.get(BDT_SC_PRI_RESTRI.BDT_SC_ID).toBigInteger());
            if (record.get(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID) != null) {
                bdtScPriRestri.setCdtScAwdPriXpsTypeMapId(record.get(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID).toBigInteger());
                bdtScPriRestri.setXbtId(record.get(XBT.XBT_ID).toBigInteger());
                bdtScPriRestri.setXbtName(record.get(XBT.NAME));
            }
            if (record.get(BDT_SC_PRI_RESTRI.CODE_LIST_ID) != null) {
                bdtScPriRestri.setCodeListId(record.get(BDT_SC_PRI_RESTRI.CODE_LIST_ID).toBigInteger());
            }
            if (record.get(BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID) != null) {
                bdtScPriRestri.setAgencyIdListId(record.get(BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID).toBigInteger());
            }
            bdtScPriRestri.setDefault(record.get(BDT_SC_PRI_RESTRI.IS_DEFAULT) == (byte) 1);
            return bdtScPriRestri;
        };
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public GetCcPackageResponse getCcPackage(GetCcPackageRequest request) throws ScoreDataAccessException {
        ScoreUser requester = request.getRequester();
        ULong asccpManifestId = ULong.valueOf(request.getAsccpManifestId());

        ULong releaseId = getReleaseIdByAsccpManifestId(asccpManifestId);
        if (releaseId == null) {
            throw new ScoreDataAccessException();
        }

        CcMap ccMap = new CcMap(dslContext(), releaseId);
        CcManifestList ccManifestList = new CcManifestList();
        ccManifestList.fillOut(ccMap, ccMap.asccpManifestRecordMap.get(asccpManifestId));

        CcPackage ccPackage = new CcPackage();
        ccPackage.setAccManifestList(new ArrayList(Optional.of(ccManifestList.accManifestRecordMap)
                .orElse(Collections.emptyMap()).values().stream()
                .map(record -> mapperAccManifest().map(record)).collect(Collectors.toList())));
        ccPackage.setAsccManifestList(new ArrayList(Optional.of(ccManifestList.asccManifestRecordMap)
                .orElse(Collections.emptyMap()).values().stream()
                .map(record -> mapperAsccManifest().map(record)).collect(Collectors.toList())));
        ccPackage.setBccManifestList(new ArrayList(Optional.of(ccManifestList.bccManifestRecordMap)
                .orElse(Collections.emptyMap()).values().stream()
                .map(record -> mapperBccManifest().map(record)).collect(Collectors.toList())));
        ccPackage.setSequenceList(dslContext().selectFrom(SEQ_KEY)
                .where(SEQ_KEY.SEQ_KEY_ID.in(
                        Stream.concat(ccPackage.getAsccManifestList().stream().map(e -> e.getSeqKeyId()),
                                ccPackage.getBccManifestList().stream().map(e -> e.getSeqKeyId()))
                                .collect(Collectors.toList())
                ))
                .fetch(mapperSequence()));
        ccPackage.setAsccpManifestList(new ArrayList(Optional.of(ccManifestList.asccpManifestRecordMap)
                .orElse(Collections.emptyMap()).values().stream()
                .map(record -> mapperAsccpManifest().map(record)).collect(Collectors.toList())));
        ccPackage.setBccpManifestList(new ArrayList(Optional.of(ccManifestList.bccpManifestRecordMap)
                .orElse(Collections.emptyMap()).values().stream()
                .map(record -> mapperBccpManifest().map(record)).collect(Collectors.toList())));
        ccPackage.setDtManifestList(new ArrayList(Optional.of(ccManifestList.dtManifestRecordMap)
                .orElse(Collections.emptyMap()).values().stream()
                .map(record -> mapperDtManifest().map(record)).collect(Collectors.toList())));
        ccPackage.setDtScManifestList(new ArrayList(Optional.of(ccManifestList.dtScManifestRecordMap)
                .orElse(Collections.emptyMap()).values().stream()
                .map(record -> mapperDtScManifest().map(record)).collect(Collectors.toList())));

        ccPackage.setAccList(selectAcc()
                .where(ACC.ACC_ID.in(ccPackage.getAccManifestList().stream()
                        .map(e -> ULong.valueOf(e.getAccId())).distinct()
                        .collect(Collectors.toList())))
                .fetch(mapperAcc()));
        ccPackage.setAsccList(selectAscc()
                .where(ASCC.ASCC_ID.in(ccPackage.getAsccManifestList().stream()
                        .map(e -> ULong.valueOf(e.getAsccId())).distinct()
                        .collect(Collectors.toList())))
                .fetch(mapperAscc()));
        ccPackage.setBccList(selectBcc()
                .where(BCC.BCC_ID.in(ccPackage.getBccManifestList().stream()
                        .map(e -> ULong.valueOf(e.getBccId())).distinct()
                        .collect(Collectors.toList())))
                .fetch(mapperBcc()));
        ccPackage.setAsccpList(selectAsccp()
                .where(ASCCP.ASCCP_ID.in(ccPackage.getAsccpManifestList().stream()
                        .map(e -> ULong.valueOf(e.getAsccpId())).distinct()
                        .collect(Collectors.toList())))
                .fetch(mapperAsccp()));
        ccPackage.setBccpList(selectBccp()
                .where(BCCP.BCCP_ID.in(ccPackage.getBccpManifestList().stream()
                        .map(e -> ULong.valueOf(e.getBccpId())).distinct()
                        .collect(Collectors.toList())))
                .fetch(mapperBccp()));
        ccPackage.setDtList(selectDt()
                .where(DT.DT_ID.in(ccPackage.getDtManifestList().stream()
                        .map(e -> ULong.valueOf(e.getDtId())).distinct()
                        .collect(Collectors.toList())))
                .fetch(mapperDt()));
        ccPackage.setDtScList(selectDtSc()
                .where(DT_SC.DT_SC_ID.in(ccPackage.getDtScManifestList().stream()
                        .map(e -> ULong.valueOf(e.getDtScId())).distinct()
                        .collect(Collectors.toList())))
                .fetch(mapperDtSc()));

        ccPackage.setBdtPriRestriList(selectBdtPriRestri().fetch(mapperBdtPriRestri()));
        ccPackage.setBdtScPriRestriList(selectBdtScPriRestri().fetch(mapperBdtScPriRestri()));

        return new GetCcPackageResponse(ccPackage);
    }

    @Override
    @AccessControl(requiredAnyRole = {DEVELOPER, END_USER})
    public FindNextAsccpManifestResponse findNextAsccpManifest(
            FindNextAsccpManifestRequest request) throws ScoreDataAccessException {
        BigInteger asccpManifestId = request.getAsccpManifestId();
        if (asccpManifestId == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }
        BigInteger nextReleaseId = request.getNextReleaseId();
        if (nextReleaseId == null) {
            throw new ScoreDataAccessException(new IllegalArgumentException());
        }

        ULong nextAsccpManifestId = null;
        String releaseNum = null;
        while (nextAsccpManifestId == null) {
            Record record = dslContext().select(
                    ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                    ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID,
                    ASCCP_MANIFEST.RELEASE_ID,
                    RELEASE.RELEASE_NUM)
                    .from(ASCCP_MANIFEST)
                    .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                    .where(ASCCP_MANIFEST.ASCCP_MANIFEST_ID.eq(ULong.valueOf(asccpManifestId)))
                    .fetchOptional().orElse(null);
            if (record == null) {
                break;
            }

            if (record.get(ASCCP_MANIFEST.RELEASE_ID).toBigInteger().equals(nextReleaseId)) {
                nextAsccpManifestId = record.get(ASCCP_MANIFEST.ASCCP_MANIFEST_ID);
            }

            asccpManifestId = record.get(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID).toBigInteger();
            releaseNum = record.get(RELEASE.RELEASE_NUM);
        }

        return new FindNextAsccpManifestResponse((nextAsccpManifestId != null) ? nextAsccpManifestId.toBigInteger() : null, releaseNum);
    }

}
