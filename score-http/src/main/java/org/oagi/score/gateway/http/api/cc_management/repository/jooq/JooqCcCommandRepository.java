package org.oagi.score.gateway.http.api.cc_management.repository.jooq;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.repository.CcCommandRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.JooqBaseRepository;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.*;
import static org.oagi.score.gateway.http.common.repository.jooq.entity.Tables.*;

public class JooqCcCommandRepository extends JooqBaseRepository implements CcCommandRepository {

    public JooqCcCommandRepository(DSLContext dslContext, ScoreUser requester, RepositoryFactory repositoryFactory) {
        super(dslContext, requester, repositoryFactory);
    }

    @Override
    public void clearReplacement(ReleaseId releaseId) {

        dslContext().update(ACC_MANIFEST)
                .setNull(ACC_MANIFEST.REPLACEMENT_ACC_MANIFEST_ID)
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ASCC_MANIFEST)
                .setNull(ASCC_MANIFEST.REPLACEMENT_ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCC_MANIFEST)
                .setNull(BCC_MANIFEST.REPLACEMENT_BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ASCCP_MANIFEST)
                .setNull(ASCCP_MANIFEST.REPLACEMENT_ASCCP_MANIFEST_ID)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCCP_MANIFEST)
                .setNull(BCCP_MANIFEST.REPLACEMENT_BCCP_MANIFEST_ID)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT_MANIFEST)
                .setNull(DT_MANIFEST.REPLACEMENT_DT_MANIFEST_ID)
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT_SC_MANIFEST)
                .setNull(DT_SC_MANIFEST.REPLACEMENT_DT_SC_MANIFEST_ID)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(CODE_LIST_MANIFEST)
                .setNull(CODE_LIST_MANIFEST.REPLACEMENT_CODE_LIST_MANIFEST_ID)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(CODE_LIST_VALUE_MANIFEST)
                .setNull(CODE_LIST_VALUE_MANIFEST.REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(AGENCY_ID_LIST_MANIFEST)
                .setNull(AGENCY_ID_LIST_MANIFEST.REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(AGENCY_ID_LIST_VALUE_MANIFEST)
                .setNull(AGENCY_ID_LIST_VALUE_MANIFEST.REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
    }

    @Override
    public void delete(ReleaseId releaseId) {
        dslContext().update(ASCC_MANIFEST).setNull(ASCC_MANIFEST.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCC_MANIFEST).setNull(BCC_MANIFEST.SEQ_KEY_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(SEQ_KEY.join(ASCC_MANIFEST).on(SEQ_KEY.ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.ASCC_MANIFEST_ID)))
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(SEQ_KEY.join(BCC_MANIFEST).on(SEQ_KEY.BCC_MANIFEST_ID.eq(BCC_MANIFEST.BCC_MANIFEST_ID)))
                .setNull(SEQ_KEY.PREV_SEQ_KEY_ID)
                .setNull(SEQ_KEY.NEXT_SEQ_KEY_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(SEQ_KEY).where(SEQ_KEY.ASCC_MANIFEST_ID.in(
                        select(ASCC_MANIFEST.ASCC_MANIFEST_ID)
                                .from(ASCC_MANIFEST)
                                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();
        dslContext().deleteFrom(SEQ_KEY).where(SEQ_KEY.BCC_MANIFEST_ID.in(
                        select(BCC_MANIFEST.BCC_MANIFEST_ID)
                                .from(BCC_MANIFEST)
                                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))))
                .execute();
        dslContext().deleteFrom(BCC_MANIFEST)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(BCCP_MANIFEST)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(DT_SC_AWD_PRI)
                .where(DT_SC_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(DT_AWD_PRI)
                .where(DT_AWD_PRI.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(XBT_MANIFEST)
                .where(XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT_SC_MANIFEST)
                .setNull(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(DT_SC_MANIFEST)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT_MANIFEST)
                .setNull(DT_MANIFEST.BASED_DT_MANIFEST_ID)
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(DT_MANIFEST)
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(ASCC_MANIFEST)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(ASCCP_MANIFEST)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ACC_MANIFEST)
                .setNull(ACC_MANIFEST.BASED_ACC_MANIFEST_ID)
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(ACC_MANIFEST)
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(CODE_LIST_VALUE_MANIFEST)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(CODE_LIST_MANIFEST)
                .setNull(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(CODE_LIST_MANIFEST)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(AGENCY_ID_LIST_MANIFEST)
                .setNull(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .setNull(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(AGENCY_ID_LIST_VALUE_MANIFEST)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().deleteFrom(AGENCY_ID_LIST_MANIFEST)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
    }

    @Override
    public void copyWorkingManifests(ReleaseId releaseId, ReleaseId workingReleaseId,
                                     List<AccManifestId> accManifestIds,
                                     List<AsccpManifestId> asccpManifestIds,
                                     List<BccpManifestId> bccpManifestIds,
                                     List<DtManifestId> dtManifestIds,
                                     List<CodeListManifestId> codeListManifestIds,
                                     List<AgencyIdListManifestId> agencyIdListManifestIds) {

        copyDtManifestRecordsFromWorking(releaseId, workingReleaseId, dtManifestIds);

        copyDtScManifestRecordsFromWorking(releaseId, workingReleaseId, dtManifestIds);
        updateDtScDependencies(releaseId, workingReleaseId);

        copyAccManifestRecordsFromWorking(releaseId, workingReleaseId, accManifestIds);

        copyAsccpManifestRecordsFromWorking(releaseId, workingReleaseId, asccpManifestIds);
        updateAsccpDependencies(releaseId, workingReleaseId);

        copyBccpManifestRecordsFromWorking(releaseId, workingReleaseId, bccpManifestIds);
        updateBccpDependencies(releaseId, workingReleaseId);

        copyAsccManifestRecordsFromWorking(releaseId, workingReleaseId, accManifestIds);
        updateAsccDependencies(releaseId, workingReleaseId);

        copyBccManifestRecordsFromWorking(releaseId, workingReleaseId, accManifestIds);
        updateBccDependencies(releaseId, workingReleaseId);

        // Run after ASCC/BCC dependency resolved b/c ASCC/BCC use ACC's state.
        updateAccDependencies(releaseId, workingReleaseId);
        updateDtDependencies(releaseId, workingReleaseId);

        copySeqKeyRecordsFromWorking(releaseId);
        updateSeqKeyPrevNext(releaseId);

        copyXbtManifestRecordsFromWorking(releaseId, workingReleaseId, Collections.emptyList());
        updateXbtDependencies(releaseId);

        copyCodeListManifestRecordsFromWorking(releaseId, workingReleaseId, codeListManifestIds);
        updateCodeListDependencies(releaseId, workingReleaseId);

        copyCodeListValueManifestRecordsFromWorking(releaseId, workingReleaseId, codeListManifestIds);
        updateCodeListValueDependencies(releaseId, workingReleaseId);

        copyAgencyIdListManifestRecordsFromWorking(releaseId, workingReleaseId, agencyIdListManifestIds);
        updateAgencyIdListDependencies(releaseId, workingReleaseId);

        copyAgencyIdListValueManifestRecordsFromWorking(releaseId, workingReleaseId, agencyIdListManifestIds);
        updateAgencyIdListValueDependencies(releaseId, workingReleaseId);

        copyDtAwdPriFromWorking(releaseId, workingReleaseId, dtManifestIds);
        copyDtScAwdPriFromWorking(releaseId, workingReleaseId, dtManifestIds);
    }

    @Override
    public void cleanUp(ReleaseId releaseId) {
        ULong requesterId = valueOf(requester().userId());

        // ACCs
        dslContext().update(ACC_MANIFEST
                        .join(ACC_MANIFEST.as("prev"))
                        .on(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.as("prev").NEXT_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ACC_MANIFEST
                        .join(ACC_MANIFEST.as("next"))
                        .on(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("next").ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.as("next").PREV_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ACC
                        .join(ACC_MANIFEST)
                        .on(ACC.ACC_ID.eq(ACC_MANIFEST.ACC_ID)))
                .set(ACC.OWNER_USER_ID, requesterId)
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // ASCCs
        dslContext().update(ASCC_MANIFEST
                        .join(ASCC_MANIFEST.as("prev"))
                        .on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID)))
                .set(ASCC_MANIFEST.as("prev").NEXT_ASCC_MANIFEST_ID, ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ASCC_MANIFEST
                        .join(ASCC_MANIFEST.as("next"))
                        .on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("next").ASCC_MANIFEST_ID)))
                .set(ASCC_MANIFEST.as("next").PREV_ASCC_MANIFEST_ID, ASCC_MANIFEST.ASCC_MANIFEST_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ASCC
                        .join(ASCC_MANIFEST)
                        .on(ASCC.ASCC_ID.eq(ASCC_MANIFEST.ASCC_ID)))
                .set(ASCC.OWNER_USER_ID, requesterId)
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // BCCs
        dslContext().update(BCC_MANIFEST
                        .join(BCC_MANIFEST.as("prev"))
                        .on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID)))
                .set(BCC_MANIFEST.as("prev").NEXT_BCC_MANIFEST_ID, BCC_MANIFEST.BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCC_MANIFEST
                        .join(BCC_MANIFEST.as("next"))
                        .on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("next").BCC_MANIFEST_ID)))
                .set(BCC_MANIFEST.as("next").PREV_BCC_MANIFEST_ID, BCC_MANIFEST.BCC_MANIFEST_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCC
                        .join(BCC_MANIFEST)
                        .on(BCC.BCC_ID.eq(BCC_MANIFEST.BCC_ID)))
                .set(BCC.OWNER_USER_ID, requesterId)
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // ASCCPs
        dslContext().update(ASCCP_MANIFEST
                        .join(ASCCP_MANIFEST.as("prev"))
                        .on(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.as("prev").NEXT_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ASCCP_MANIFEST
                        .join(ASCCP_MANIFEST.as("next"))
                        .on(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.as("next").PREV_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(ASCCP
                        .join(ASCCP_MANIFEST)
                        .on(ASCCP.ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID)))
                .set(ASCCP.OWNER_USER_ID, requesterId)
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCCP.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // BCCPs
        dslContext().update(BCCP_MANIFEST
                        .join(BCCP_MANIFEST.as("prev"))
                        .on(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID)))
                .set(BCCP_MANIFEST.as("prev").NEXT_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCCP_MANIFEST
                        .join(BCCP_MANIFEST.as("next"))
                        .on(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID)))
                .set(BCCP_MANIFEST.as("next").PREV_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(BCCP
                        .join(BCCP_MANIFEST)
                        .on(BCCP.BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID)))
                .set(BCCP.OWNER_USER_ID, requesterId)
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCCP.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // CODE_LISTs
        dslContext().update(CODE_LIST_MANIFEST
                        .join(CODE_LIST_MANIFEST.as("prev"))
                        .on(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("prev").CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.as("prev").NEXT_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(CODE_LIST_MANIFEST
                        .join(CODE_LIST_MANIFEST.as("next"))
                        .on(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("next").CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.as("next").PREV_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(CODE_LIST
                        .join(CODE_LIST_MANIFEST)
                        .on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID)))
                .set(CODE_LIST.OWNER_USER_ID, requesterId)
                .where(and(
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // CODE_LIST_VALUEs
        dslContext().update(CODE_LIST_VALUE_MANIFEST
                        .join(CODE_LIST_VALUE_MANIFEST.as("prev"))
                        .on(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("prev").CODE_LIST_VALUE_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.as("prev").NEXT_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(CODE_LIST_VALUE_MANIFEST
                        .join(CODE_LIST_VALUE_MANIFEST.as("next"))
                        .on(CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("next").CODE_LIST_VALUE_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.as("next").PREV_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                .where(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(CODE_LIST_VALUE
                        .join(CODE_LIST_VALUE_MANIFEST)
                        .on(CODE_LIST_VALUE.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID)))
                .set(CODE_LIST_VALUE.OWNER_USER_ID, requesterId)
                .where(and(
                        CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST_VALUE.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // DTs
        dslContext().update(DT_MANIFEST
                        .join(DT_MANIFEST.as("prev"))
                        .on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID)))
                .set(DT_MANIFEST.as("prev").NEXT_DT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT_MANIFEST
                        .join(DT_MANIFEST.as("next"))
                        .on(DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("next").DT_MANIFEST_ID)))
                .set(DT_MANIFEST.as("next").PREV_DT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT
                        .join(DT_MANIFEST)
                        .on(DT.DT_ID.eq(DT_MANIFEST.DT_ID)))
                .set(DT.OWNER_USER_ID, requesterId)
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        //DT_SCs
        dslContext().update(DT_SC_MANIFEST
                        .join(DT_SC_MANIFEST.as("prev"))
                        .on(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.as("prev").NEXT_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT_SC_MANIFEST
                        .join(DT_SC_MANIFEST.as("next"))
                        .on(DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("next").DT_SC_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.as("next").PREV_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                .where(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(DT_SC
                        .join(DT_SC_MANIFEST)
                        .on(DT_SC.DT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID)))
                .set(DT_SC.OWNER_USER_ID, requesterId)
                .where(and(
                        DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_SC.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        //XBTs
        dslContext().update(XBT_MANIFEST
                        .join(XBT_MANIFEST.as("prev"))
                        .on(XBT_MANIFEST.PREV_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("prev").XBT_MANIFEST_ID)))
                .set(XBT_MANIFEST.as("prev").NEXT_XBT_MANIFEST_ID, XBT_MANIFEST.XBT_MANIFEST_ID)
                .where(XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(XBT_MANIFEST
                        .join(XBT_MANIFEST.as("next"))
                        .on(XBT_MANIFEST.NEXT_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("next").XBT_MANIFEST_ID)))
                .set(XBT_MANIFEST.as("next").PREV_XBT_MANIFEST_ID, XBT_MANIFEST.XBT_MANIFEST_ID)
                .where(XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(XBT
                        .join(XBT_MANIFEST)
                        .on(XBT.XBT_ID.eq(XBT_MANIFEST.XBT_ID)))
                .set(XBT.OWNER_USER_ID, requesterId)
                .where(and(
                        XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        XBT.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // AGENCY_ID_LIST
        dslContext().update(AGENCY_ID_LIST_MANIFEST
                        .join(AGENCY_ID_LIST_MANIFEST.as("prev"))
                        .on(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("prev").AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.as("prev").NEXT_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(AGENCY_ID_LIST_MANIFEST
                        .join(AGENCY_ID_LIST_MANIFEST.as("next"))
                        .on(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("next").AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.as("next").PREV_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(AGENCY_ID_LIST
                        .join(AGENCY_ID_LIST_MANIFEST)
                        .on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID)))
                .set(AGENCY_ID_LIST.OWNER_USER_ID, requesterId)
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();

        // AGENCY_ID_LIST_VALUE
        dslContext().update(AGENCY_ID_LIST_VALUE_MANIFEST
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev"))
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(AGENCY_ID_LIST_VALUE_MANIFEST
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("next"))
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("next").AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("next").PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))
                .execute();
        dslContext().update(AGENCY_ID_LIST_VALUE
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID)))
                .set(AGENCY_ID_LIST_VALUE.OWNER_USER_ID, requesterId)
                .where(and(
                        AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST_VALUE.OWNER_USER_ID.notEqual(requesterId)
                ))
                .execute();
    }

    private void copyAccManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                   List<AccManifestId> accManifestIds) {
        dslContext().insertInto(ACC_MANIFEST,
                        ACC_MANIFEST.RELEASE_ID,
                        ACC_MANIFEST.ACC_ID,
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                        ACC_MANIFEST.DEN,
                        ACC_MANIFEST.CONFLICT,
                        ACC_MANIFEST.LOG_ID,
                        ACC_MANIFEST.PREV_ACC_MANIFEST_ID,
                        ACC_MANIFEST.NEXT_ACC_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                ACC_MANIFEST.ACC_ID,
                                ACC_MANIFEST.BASED_ACC_MANIFEST_ID,
                                ACC_MANIFEST.DEN,
                                ACC_MANIFEST.CONFLICT,
                                ACC_MANIFEST.LOG_ID,
                                ACC_MANIFEST.PREV_ACC_MANIFEST_ID,
                                ACC_MANIFEST.ACC_MANIFEST_ID)
                        .from(ACC_MANIFEST)
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .where(and(ACC_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.isNotNull(),
                                        ACC_MANIFEST.ACC_MANIFEST_ID.in(valueOf(accManifestIds))))))).execute();

        // Copy tags
        dslContext().insertInto(ACC_MANIFEST_TAG,
                        ACC_MANIFEST_TAG.ACC_MANIFEST_ID,
                        ACC_MANIFEST_TAG.TAG_ID,
                        ACC_MANIFEST_TAG.CREATED_BY,
                        ACC_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext().select(
                                ACC_MANIFEST.ACC_MANIFEST_ID,
                                ACC_MANIFEST_TAG.TAG_ID,
                                ACC_MANIFEST_TAG.CREATED_BY,
                                ACC_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(ACC_MANIFEST)
                        .join(ACC_MANIFEST.as("next")).on(and(
                                ACC_MANIFEST.NEXT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("next").ACC_MANIFEST_ID),
                                ACC_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(ACC_MANIFEST_TAG).on(ACC_MANIFEST.as("next").ACC_MANIFEST_ID.eq(ACC_MANIFEST_TAG.ACC_MANIFEST_ID))
                        .where(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))).execute();
    }

    private void copyDtAwdPriFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                         List<DtManifestId> dtManifestIds) {
        dslContext().insertInto(DT_AWD_PRI,
                        DT_AWD_PRI.RELEASE_ID,
                        DT_AWD_PRI.DT_ID,
                        DT_AWD_PRI.CDT_PRI_ID,
                        DT_AWD_PRI.XBT_MANIFEST_ID,
                        DT_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                DT_MANIFEST.as("target").RELEASE_ID,
                                DT_MANIFEST.as("target").DT_ID,
                                DT_AWD_PRI.CDT_PRI_ID,
                                XBT_MANIFEST.as("target_xbt").XBT_MANIFEST_ID,
                                DT_AWD_PRI.IS_DEFAULT)
                        .from(DT_MANIFEST.as("working"))
                        .join(DT_MANIFEST.as("target")).on(and(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(DT_MANIFEST.as("target").NEXT_DT_MANIFEST_ID),
                                DT_MANIFEST.as("target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(DT_AWD_PRI).on(and(
                                DT_MANIFEST.as("working").RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID),
                                DT_MANIFEST.as("working").DT_ID.eq(DT_AWD_PRI.DT_ID)))
                        .join(XBT_MANIFEST.as("working_xbt")).on(DT_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("working_xbt").XBT_MANIFEST_ID))
                        .join(XBT_MANIFEST.as("target_xbt")).on(and(
                                XBT_MANIFEST.as("target_xbt").XBT_ID.eq(XBT_MANIFEST.as("working_xbt").XBT_ID),
                                XBT_MANIFEST.as("target_xbt").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .where(and(DT_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_AWD_PRI.XBT_MANIFEST_ID.isNotNull(),
                                (or(DT_MANIFEST.as("working").PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.as("working").DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();

        dslContext().insertInto(DT_AWD_PRI,
                        DT_AWD_PRI.RELEASE_ID,
                        DT_AWD_PRI.DT_ID,
                        DT_AWD_PRI.CDT_PRI_ID,
                        DT_AWD_PRI.CODE_LIST_MANIFEST_ID,
                        DT_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                DT_MANIFEST.as("target").RELEASE_ID,
                                DT_MANIFEST.as("target").DT_ID,
                                DT_AWD_PRI.CDT_PRI_ID,
                                CODE_LIST_MANIFEST.as("target_code_list").CODE_LIST_MANIFEST_ID,
                                DT_AWD_PRI.IS_DEFAULT)
                        .from(DT_MANIFEST.as("working"))
                        .join(DT_MANIFEST.as("target")).on(and(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(DT_MANIFEST.as("target").NEXT_DT_MANIFEST_ID),
                                DT_MANIFEST.as("target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(DT_AWD_PRI).on(and(
                                DT_MANIFEST.as("working").RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID),
                                DT_MANIFEST.as("working").DT_ID.eq(DT_AWD_PRI.DT_ID)))
                        .join(CODE_LIST_MANIFEST.as("working_code_list")).on(DT_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("working_code_list").CODE_LIST_MANIFEST_ID))
                        .join(CODE_LIST_MANIFEST.as("target_code_list")).on(and(
                                CODE_LIST_MANIFEST.as("target_code_list").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("working_code_list").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("target_code_list").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .where(and(DT_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_AWD_PRI.CODE_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_MANIFEST.as("working").PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.as("working").DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();

        dslContext().insertInto(DT_AWD_PRI,
                        DT_AWD_PRI.RELEASE_ID,
                        DT_AWD_PRI.DT_ID,
                        DT_AWD_PRI.CDT_PRI_ID,
                        DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                        DT_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                DT_MANIFEST.as("target").RELEASE_ID,
                                DT_MANIFEST.as("target").DT_ID,
                                DT_AWD_PRI.CDT_PRI_ID,
                                AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list").AGENCY_ID_LIST_MANIFEST_ID,
                                DT_AWD_PRI.IS_DEFAULT)
                        .from(DT_MANIFEST.as("working"))
                        .join(DT_MANIFEST.as("target")).on(and(
                                DT_MANIFEST.as("working").DT_MANIFEST_ID.eq(DT_MANIFEST.as("target").NEXT_DT_MANIFEST_ID),
                                DT_MANIFEST.as("target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(DT_AWD_PRI).on(and(
                                DT_MANIFEST.as("working").RELEASE_ID.eq(DT_AWD_PRI.RELEASE_ID),
                                DT_MANIFEST.as("working").DT_ID.eq(DT_AWD_PRI.DT_ID)))
                        .join(AGENCY_ID_LIST_MANIFEST.as("working_agency_id_list")).on(DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("working_agency_id_list").AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("working_agency_id_list").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .where(and(DT_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_MANIFEST.as("working").PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.as("working").DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();
    }

    private void copyDtScAwdPriFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                           List<DtManifestId> dtManifestIds) {
        dslContext().insertInto(DT_SC_AWD_PRI,
                        DT_SC_AWD_PRI.RELEASE_ID,
                        DT_SC_AWD_PRI.DT_SC_ID,
                        DT_SC_AWD_PRI.CDT_PRI_ID,
                        DT_SC_AWD_PRI.XBT_MANIFEST_ID,
                        DT_SC_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                DT_SC_MANIFEST.as("target").RELEASE_ID,
                                DT_SC_MANIFEST.as("target").DT_SC_ID,
                                DT_SC_AWD_PRI.CDT_PRI_ID,
                                XBT_MANIFEST.as("target_xbt").XBT_MANIFEST_ID,
                                DT_SC_AWD_PRI.IS_DEFAULT)
                        .from(DT_SC_MANIFEST.as("working"))
                        .join(DT_SC_MANIFEST.as("target")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("target").NEXT_DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(DT_SC_AWD_PRI).on(and(
                                DT_SC_MANIFEST.as("working").RELEASE_ID.eq(DT_SC_AWD_PRI.RELEASE_ID),
                                DT_SC_MANIFEST.as("working").DT_SC_ID.eq(DT_SC_AWD_PRI.DT_SC_ID)))
                        .join(XBT_MANIFEST.as("working_xbt")).on(DT_SC_AWD_PRI.XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("working_xbt").XBT_MANIFEST_ID))
                        .join(XBT_MANIFEST.as("target_xbt")).on(and(
                                XBT_MANIFEST.as("target_xbt").XBT_ID.eq(XBT_MANIFEST.as("working_xbt").XBT_ID),
                                XBT_MANIFEST.as("target_xbt").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .where(and(DT_SC_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_SC_AWD_PRI.XBT_MANIFEST_ID.isNotNull(),
                                (or(DT_SC_MANIFEST.as("working").PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.as("working").OWNER_DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();

        dslContext().insertInto(DT_SC_AWD_PRI,
                        DT_SC_AWD_PRI.RELEASE_ID,
                        DT_SC_AWD_PRI.DT_SC_ID,
                        DT_SC_AWD_PRI.CDT_PRI_ID,
                        DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID,
                        DT_SC_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                DT_SC_MANIFEST.as("target").RELEASE_ID,
                                DT_SC_MANIFEST.as("target").DT_SC_ID,
                                DT_SC_AWD_PRI.CDT_PRI_ID,
                                CODE_LIST_MANIFEST.as("target_code_list").CODE_LIST_MANIFEST_ID,
                                DT_SC_AWD_PRI.IS_DEFAULT)
                        .from(DT_SC_MANIFEST.as("working"))
                        .join(DT_SC_MANIFEST.as("target")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("target").NEXT_DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(DT_SC_AWD_PRI).on(and(
                                DT_SC_MANIFEST.as("working").RELEASE_ID.eq(DT_SC_AWD_PRI.RELEASE_ID),
                                DT_SC_MANIFEST.as("working").DT_SC_ID.eq(DT_SC_AWD_PRI.DT_SC_ID)))
                        .join(CODE_LIST_MANIFEST.as("working_code_list")).on(DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("working_code_list").CODE_LIST_MANIFEST_ID))
                        .join(CODE_LIST_MANIFEST.as("target_code_list")).on(and(
                                CODE_LIST_MANIFEST.as("target_code_list").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("working_code_list").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("target_code_list").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .where(and(DT_SC_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_SC_AWD_PRI.CODE_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_SC_MANIFEST.as("working").PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.as("working").OWNER_DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();

        dslContext().insertInto(DT_SC_AWD_PRI,
                        DT_SC_AWD_PRI.RELEASE_ID,
                        DT_SC_AWD_PRI.DT_SC_ID,
                        DT_SC_AWD_PRI.CDT_PRI_ID,
                        DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID,
                        DT_SC_AWD_PRI.IS_DEFAULT)
                .select(dslContext().select(
                                DT_SC_MANIFEST.as("target").RELEASE_ID,
                                DT_SC_MANIFEST.as("target").DT_SC_ID,
                                DT_SC_AWD_PRI.CDT_PRI_ID,
                                AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list").AGENCY_ID_LIST_MANIFEST_ID,
                                DT_SC_AWD_PRI.IS_DEFAULT)
                        .from(DT_SC_MANIFEST.as("working"))
                        .join(DT_SC_MANIFEST.as("target")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("target").NEXT_DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("target").RELEASE_ID.eq(valueOf(releaseId))))
                        .join(DT_SC_AWD_PRI).on(and(
                                DT_SC_MANIFEST.as("working").RELEASE_ID.eq(DT_SC_AWD_PRI.RELEASE_ID),
                                DT_SC_MANIFEST.as("working").DT_SC_ID.eq(DT_SC_AWD_PRI.DT_SC_ID)))
                        .join(AGENCY_ID_LIST_MANIFEST.as("working_agency_id_list")).on(DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("working_agency_id_list").AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("working_agency_id_list").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("target_agency_id_list").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .where(and(DT_SC_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_SC_AWD_PRI.AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                (or(DT_SC_MANIFEST.as("working").PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.as("working").OWNER_DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();
    }

    private void copyDtManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                  List<DtManifestId> dtManifestIds) {
        dslContext().insertInto(DT_MANIFEST,
                        DT_MANIFEST.RELEASE_ID,
                        DT_MANIFEST.DT_ID,
                        DT_MANIFEST.BASED_DT_MANIFEST_ID,
                        DT_MANIFEST.DEN,
                        DT_MANIFEST.CONFLICT,
                        DT_MANIFEST.LOG_ID,
                        DT_MANIFEST.PREV_DT_MANIFEST_ID,
                        DT_MANIFEST.NEXT_DT_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                DT_MANIFEST.DT_ID,
                                DT_MANIFEST.BASED_DT_MANIFEST_ID,
                                DT_MANIFEST.DEN,
                                DT_MANIFEST.CONFLICT,
                                DT_MANIFEST.LOG_ID,
                                DT_MANIFEST.PREV_DT_MANIFEST_ID,
                                DT_MANIFEST.DT_MANIFEST_ID)
                        .from(DT_MANIFEST)
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .where(and(DT_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(DT_MANIFEST.PREV_DT_MANIFEST_ID.isNotNull(),
                                        DT_MANIFEST.DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();

        // Copy tags
        dslContext().insertInto(DT_MANIFEST_TAG,
                        DT_MANIFEST_TAG.DT_MANIFEST_ID,
                        DT_MANIFEST_TAG.TAG_ID,
                        DT_MANIFEST_TAG.CREATED_BY,
                        DT_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext().select(
                                DT_MANIFEST.DT_MANIFEST_ID,
                                DT_MANIFEST_TAG.TAG_ID,
                                DT_MANIFEST_TAG.CREATED_BY,
                                DT_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(DT_MANIFEST)
                        .join(DT_MANIFEST.as("next")).on(and(
                                DT_MANIFEST.NEXT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("next").DT_MANIFEST_ID),
                                DT_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(DT_MANIFEST_TAG).on(DT_MANIFEST.as("next").DT_MANIFEST_ID.eq(DT_MANIFEST_TAG.DT_MANIFEST_ID))
                        .where(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))).execute();
    }

    private void copyAsccpManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                     List<AsccpManifestId> asccpManifestIds) {
        dslContext().insertInto(ASCCP_MANIFEST,
                        ASCCP_MANIFEST.RELEASE_ID,
                        ASCCP_MANIFEST.ASCCP_ID,
                        ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                        ASCCP_MANIFEST.DEN,
                        ASCCP_MANIFEST.CONFLICT,
                        ASCCP_MANIFEST.LOG_ID,
                        ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                        ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                ASCCP_MANIFEST.ASCCP_ID,
                                ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID,
                                ASCCP_MANIFEST.DEN,
                                ASCCP_MANIFEST.CONFLICT,
                                ASCCP_MANIFEST.LOG_ID,
                                ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID,
                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .where(and(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.isNotNull(),
                                        ASCCP_MANIFEST.ASCCP_MANIFEST_ID.in(valueOf(asccpManifestIds))))))).execute();

        // Copy tags
        dslContext().insertInto(ASCCP_MANIFEST_TAG,
                        ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID,
                        ASCCP_MANIFEST_TAG.TAG_ID,
                        ASCCP_MANIFEST_TAG.CREATED_BY,
                        ASCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext().select(
                                ASCCP_MANIFEST.ASCCP_MANIFEST_ID,
                                ASCCP_MANIFEST_TAG.TAG_ID,
                                ASCCP_MANIFEST_TAG.CREATED_BY,
                                ASCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(ASCCP_MANIFEST)
                        .join(ASCCP_MANIFEST.as("next")).on(and(
                                ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID),
                                ASCCP_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(ASCCP_MANIFEST_TAG).on(ASCCP_MANIFEST.as("next").ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST_TAG.ASCCP_MANIFEST_ID))
                        .where(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))).execute();
    }

    private void copyBccpManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                    List<BccpManifestId> bccpManifestIds) {
        dslContext().insertInto(BCCP_MANIFEST,
                        BCCP_MANIFEST.RELEASE_ID,
                        BCCP_MANIFEST.BCCP_ID,
                        BCCP_MANIFEST.BDT_MANIFEST_ID,
                        BCCP_MANIFEST.DEN,
                        BCCP_MANIFEST.CONFLICT,
                        BCCP_MANIFEST.LOG_ID,
                        BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID,
                        BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                BCCP_MANIFEST.BCCP_ID,
                                BCCP_MANIFEST.BDT_MANIFEST_ID,
                                BCCP_MANIFEST.DEN,
                                BCCP_MANIFEST.CONFLICT,
                                BCCP_MANIFEST.LOG_ID,
                                BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID,
                                BCCP_MANIFEST.BCCP_MANIFEST_ID)
                        .from(BCCP_MANIFEST)
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .where(and(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.isNotNull(),
                                        BCCP_MANIFEST.BCCP_MANIFEST_ID.in(valueOf(bccpManifestIds))))))).execute();

        // Copy tags
        dslContext().insertInto(BCCP_MANIFEST_TAG,
                        BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID,
                        BCCP_MANIFEST_TAG.TAG_ID,
                        BCCP_MANIFEST_TAG.CREATED_BY,
                        BCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                .select(dslContext().select(
                                BCCP_MANIFEST.BCCP_MANIFEST_ID,
                                BCCP_MANIFEST_TAG.TAG_ID,
                                BCCP_MANIFEST_TAG.CREATED_BY,
                                BCCP_MANIFEST_TAG.CREATION_TIMESTAMP)
                        .from(BCCP_MANIFEST)
                        .join(BCCP_MANIFEST.as("next")).on(and(
                                BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID),
                                BCCP_MANIFEST.as("next").RELEASE_ID.eq(valueOf(workingReleaseId))))
                        .join(BCCP_MANIFEST_TAG).on(BCCP_MANIFEST.as("next").BCCP_MANIFEST_ID.eq(BCCP_MANIFEST_TAG.BCCP_MANIFEST_ID))
                        .where(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))).execute();
    }

    private void copyAsccManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                    List<AccManifestId> accManifestIds) {
        dslContext().insertInto(ASCC_MANIFEST,
                        ASCC_MANIFEST.RELEASE_ID,
                        ASCC_MANIFEST.ASCC_ID,
                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                        ASCC_MANIFEST.DEN,
                        ASCC_MANIFEST.CONFLICT,
                        ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID,
                        ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                ASCC_MANIFEST.ASCC_ID,
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                                ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID,
                                ASCC_MANIFEST.DEN,
                                ASCC_MANIFEST.CONFLICT,
                                ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID,
                                ASCC_MANIFEST.ASCC_MANIFEST_ID)
                        .from(ASCC_MANIFEST)
                        .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                        .where(and(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.isNotNull(),
                                        ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(valueOf(accManifestIds))))))).execute();
    }

    private void copyBccManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                   List<AccManifestId> accManifestIds) {
        dslContext().insertInto(BCC_MANIFEST,
                        BCC_MANIFEST.RELEASE_ID,
                        BCC_MANIFEST.BCC_ID,
                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                        BCC_MANIFEST.DEN,
                        BCC_MANIFEST.CONFLICT,
                        BCC_MANIFEST.PREV_BCC_MANIFEST_ID,
                        BCC_MANIFEST.NEXT_BCC_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                BCC_MANIFEST.BCC_ID,
                                BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                                BCC_MANIFEST.TO_BCCP_MANIFEST_ID,
                                BCC_MANIFEST.DEN,
                                BCC_MANIFEST.CONFLICT,
                                BCC_MANIFEST.PREV_BCC_MANIFEST_ID,
                                BCC_MANIFEST.BCC_MANIFEST_ID)
                        .from(BCC_MANIFEST)
                        .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                        .where(and(BCC_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.isNotNull(),
                                        BCC_MANIFEST.FROM_ACC_MANIFEST_ID.in(valueOf(accManifestIds))))))).execute();
    }

    private void copySeqKeyRecordsFromWorking(ReleaseId releaseId) {
        // insert ASCC SEQ_KEY Records
        dslContext().insertInto(SEQ_KEY,
                        SEQ_KEY.FROM_ACC_MANIFEST_ID,
                        SEQ_KEY.ASCC_MANIFEST_ID)
                .select(dslContext().select(
                                ASCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                                ASCC_MANIFEST.ASCC_MANIFEST_ID)
                        .from(ASCC_MANIFEST)
                        .join(SEQ_KEY).on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(SEQ_KEY.ASCC_MANIFEST_ID))
                        .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))).execute();

        // insert BCC SEQ_KEY Records
        dslContext().insertInto(SEQ_KEY,
                        SEQ_KEY.FROM_ACC_MANIFEST_ID,
                        SEQ_KEY.BCC_MANIFEST_ID)
                .select(dslContext().select(
                                BCC_MANIFEST.FROM_ACC_MANIFEST_ID,
                                BCC_MANIFEST.BCC_MANIFEST_ID)
                        .from(BCC_MANIFEST)
                        .join(SEQ_KEY).on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(SEQ_KEY.BCC_MANIFEST_ID))
                        .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)))).execute();

        // Link SEQ_KEY to Manifest
        dslContext().update(ASCC_MANIFEST
                        .join(SEQ_KEY).on(ASCC_MANIFEST.ASCC_MANIFEST_ID.eq(SEQ_KEY.ASCC_MANIFEST_ID)))
                .set(ASCC_MANIFEST.SEQ_KEY_ID, SEQ_KEY.SEQ_KEY_ID)
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))).execute();

        dslContext().update(BCC_MANIFEST
                        .join(SEQ_KEY).on(BCC_MANIFEST.BCC_MANIFEST_ID.eq(SEQ_KEY.BCC_MANIFEST_ID)))
                .set(BCC_MANIFEST.SEQ_KEY_ID, SEQ_KEY.SEQ_KEY_ID)
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))).execute();
    }

    private void updateSeqKeyPrevNext(ReleaseId releaseId) {
        // Update prev/next seq_key for ASCC
        dslContext().update(SEQ_KEY
                        .join(ASCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(ASCC_MANIFEST.SEQ_KEY_ID))
                        .join(SEQ_KEY.as("working_seq")).on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(SEQ_KEY.as("working_seq").ASCC_MANIFEST_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("working_prev_ascc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_prev_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                                and(ASCC_MANIFEST.as("working_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").NEXT_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("working_prev_bcc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_prev_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                                and(BCC_MANIFEST.as("working_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").NEXT_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(ASCC_MANIFEST.as("working_next_ascc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_next_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                                and(ASCC_MANIFEST.as("working_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").NEXT_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("working_next_bcc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_next_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                                and(BCC_MANIFEST.as("working_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").NEXT_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(valueOf(releaseId))))
                )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))).execute();

        // Update prev/next seq_key for BCC
        dslContext().update(SEQ_KEY
                        .join(BCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(BCC_MANIFEST.SEQ_KEY_ID))
                        .join(SEQ_KEY.as("working_seq")).on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(SEQ_KEY.as("working_seq").BCC_MANIFEST_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("working_prev_ascc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_prev_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                                and(ASCC_MANIFEST.as("working_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").NEXT_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("working_prev_bcc")).on(SEQ_KEY.as("working_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_prev_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                                and(BCC_MANIFEST.as("working_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").NEXT_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(ASCC_MANIFEST.as("working_next_ascc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("working_next_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                                and(ASCC_MANIFEST.as("working_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").NEXT_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("working_next_bcc")).on(SEQ_KEY.as("working_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("working_next_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                                and(BCC_MANIFEST.as("working_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").NEXT_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(valueOf(releaseId))))
                )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))).execute();

        // Update prev/next seq_key for unassigned ASCC.
        dslContext().update(SEQ_KEY
                        .join(ASCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(ASCC_MANIFEST.SEQ_KEY_ID))
                        .join(ASCC_MANIFEST.as("working_ascc")).on(ASCC_MANIFEST.NEXT_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("working_ascc").ASCC_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(ASCC_MANIFEST.as("working_ascc").FROM_ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(SEQ_KEY.as("last_seq")).on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(SEQ_KEY.as("last_seq").ASCC_MANIFEST_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("last_prev_ascc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_prev_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                                and(ASCC_MANIFEST.as("last_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").PREV_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("last_prev_bcc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_prev_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                                and(BCC_MANIFEST.as("last_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").PREV_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(ASCC_MANIFEST.as("last_next_ascc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_next_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                                and(ASCC_MANIFEST.as("last_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").PREV_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("last_next_bcc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_next_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                                and(BCC_MANIFEST.as("last_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").PREV_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(valueOf(releaseId))))
                )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(and(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published)))).execute();

        // Update prev/next seq_key for unassigned BCC.
        dslContext().update(SEQ_KEY
                        .join(BCC_MANIFEST).on(SEQ_KEY.SEQ_KEY_ID.eq(BCC_MANIFEST.SEQ_KEY_ID))
                        .join(BCC_MANIFEST.as("working_bcc")).on(BCC_MANIFEST.NEXT_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("working_bcc").BCC_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ACC_MANIFEST.ACC_MANIFEST_ID.eq(BCC_MANIFEST.as("working_bcc").FROM_ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(SEQ_KEY.as("last_seq")).on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(SEQ_KEY.as("last_seq").BCC_MANIFEST_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("last_prev_ascc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_prev_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_prev_ascc")).on(
                                and(ASCC_MANIFEST.as("last_prev_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_prev_ascc").PREV_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_prev_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("last_prev_bcc")).on(SEQ_KEY.as("last_seq").PREV_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_prev_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_prev_bcc")).on(
                                and(BCC_MANIFEST.as("last_prev_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_prev_bcc").PREV_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_prev_bcc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(ASCC_MANIFEST.as("last_next_ascc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(ASCC_MANIFEST.as("last_next_ascc").SEQ_KEY_ID))
                        .leftOuterJoin(ASCC_MANIFEST.as("cur_next_ascc")).on(
                                and(ASCC_MANIFEST.as("last_next_ascc").ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("cur_next_ascc").PREV_ASCC_MANIFEST_ID),
                                        ASCC_MANIFEST.as("cur_next_ascc").RELEASE_ID.eq(valueOf(releaseId))))

                        .leftOuterJoin(BCC_MANIFEST.as("last_next_bcc")).on(SEQ_KEY.as("last_seq").NEXT_SEQ_KEY_ID.eq(BCC_MANIFEST.as("last_next_bcc").SEQ_KEY_ID))
                        .leftOuterJoin(BCC_MANIFEST.as("cur_next_bcc")).on(
                                and(BCC_MANIFEST.as("last_next_bcc").BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("cur_next_bcc").PREV_BCC_MANIFEST_ID),
                                        BCC_MANIFEST.as("cur_next_bcc").RELEASE_ID.eq(valueOf(releaseId))))
                )
                .set(SEQ_KEY.PREV_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_prev_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_prev_bcc").SEQ_KEY_ID))
                .set(SEQ_KEY.NEXT_SEQ_KEY_ID, ifnull(ASCC_MANIFEST.as("cur_next_ascc").SEQ_KEY_ID, BCC_MANIFEST.as("cur_next_bcc").SEQ_KEY_ID))
                .where(and(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published)))).execute();
    }

    private void copyDtScManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                    List<DtManifestId> dtManifestIds) {
        dslContext().insertInto(DT_SC_MANIFEST,
                        DT_SC_MANIFEST.RELEASE_ID,
                        DT_SC_MANIFEST.DT_SC_ID,
                        DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                        DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID,
                        DT_SC_MANIFEST.CONFLICT,
                        DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID,
                        DT_SC_MANIFEST.NEXT_DT_SC_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                DT_SC_MANIFEST.DT_SC_ID,
                                DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID,
                                DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID,
                                DT_SC_MANIFEST.CONFLICT,
                                DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID,
                                DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                        .from(DT_SC_MANIFEST)
                        .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.isNotNull(),
                                        DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.in(valueOf(dtManifestIds))))))).execute();
    }

    private void copyXbtManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                   List<BigInteger> xbtManifestIds) {
        dslContext().insertInto(XBT_MANIFEST,
                        XBT_MANIFEST.RELEASE_ID,
                        XBT_MANIFEST.XBT_ID,
                        XBT_MANIFEST.CONFLICT,
                        XBT_MANIFEST.LOG_ID,
                        XBT_MANIFEST.PREV_XBT_MANIFEST_ID,
                        XBT_MANIFEST.NEXT_XBT_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                XBT_MANIFEST.XBT_ID,
                                XBT_MANIFEST.CONFLICT,
                                XBT_MANIFEST.LOG_ID,
                                XBT_MANIFEST.PREV_XBT_MANIFEST_ID,
                                XBT_MANIFEST.XBT_MANIFEST_ID)
                        .from(XBT_MANIFEST)
                        .where(XBT_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId))))
                .execute();
    }

    private void copyCodeListManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                        List<CodeListManifestId> codeListManifestIds) {
        dslContext().insertInto(CODE_LIST_MANIFEST,
                        CODE_LIST_MANIFEST.RELEASE_ID,
                        CODE_LIST_MANIFEST.CODE_LIST_ID,
                        CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                        CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        CODE_LIST_MANIFEST.CONFLICT,
                        CODE_LIST_MANIFEST.LOG_ID,
                        CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                        CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                CODE_LIST_MANIFEST.CODE_LIST_ID,
                                CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID,
                                CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                                CODE_LIST_MANIFEST.CONFLICT,
                                CODE_LIST_MANIFEST.LOG_ID,
                                CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID,
                                CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                        .from(CODE_LIST_MANIFEST)
                        .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .where(and(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNotNull(),
                                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(valueOf(codeListManifestIds))))))).execute();
    }

    private void copyCodeListValueManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                             List<CodeListManifestId> codeListManifestIds) {
        dslContext().insertInto(CODE_LIST_VALUE_MANIFEST,
                        CODE_LIST_VALUE_MANIFEST.RELEASE_ID,
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID,
                        CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID,
                        CODE_LIST_VALUE_MANIFEST.CONFLICT,
                        CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID,
                        CODE_LIST_VALUE_MANIFEST.NEXT_CODE_LIST_VALUE_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID,
                                CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID,
                                CODE_LIST_VALUE_MANIFEST.CONFLICT,
                                CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID,
                                CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_MANIFEST_ID)
                        .from(CODE_LIST_VALUE_MANIFEST)
                        .join(CODE_LIST_MANIFEST).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID))
                        .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .where(and(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.isNotNull(),
                                        CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID.in(valueOf(codeListManifestIds))))))).execute();
    }

    private void copyAgencyIdListManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                            List<AgencyIdListManifestId> agencyIdListManifestIds) {
        dslContext().insertInto(AGENCY_ID_LIST_MANIFEST,
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID,
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID,
                        AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        AGENCY_ID_LIST_MANIFEST.CONFLICT,
                        AGENCY_ID_LIST_MANIFEST.LOG_ID,
                        AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID,
                                AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID,
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                                AGENCY_ID_LIST_MANIFEST.CONFLICT,
                                AGENCY_ID_LIST_MANIFEST.LOG_ID,
                                AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID,
                                AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_MANIFEST)
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(valueOf(agencyIdListManifestIds))))))).execute();
    }

    private void copyAgencyIdListValueManifestRecordsFromWorking(ReleaseId releaseId, ReleaseId workingReleaseId,
                                                                 List<AgencyIdListManifestId> agencyIdListManifestIds) {
        dslContext().insertInto(AGENCY_ID_LIST_VALUE_MANIFEST,
                        AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.CONFLICT,
                        AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                        AGENCY_ID_LIST_VALUE_MANIFEST.NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .select(dslContext().select(
                                inline(valueOf(releaseId)),
                                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID,
                                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID,
                                AGENCY_ID_LIST_VALUE_MANIFEST.CONFLICT,
                                AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID,
                                AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                        .from(AGENCY_ID_LIST_VALUE_MANIFEST)
                        .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID))
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(workingReleaseId)),
                                (or(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.isNotNull(),
                                        AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.in(valueOf(agencyIdListManifestIds))))))).execute();
    }

    private void updateAccDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(ACC_MANIFEST.join(ACC_MANIFEST.as("based"))
                        .on(ACC_MANIFEST.BASED_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("based").NEXT_ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ACC_MANIFEST.as("based").ACC_MANIFEST_ID)
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC_MANIFEST.as("based").RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // Set ACC_MANIFEST.ACC_ID to PREV.ACC_ID if the ACC in Working branch is not in ReleaseDraft nor Published states.
        dslContext().update(ACC_MANIFEST
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(ACC_MANIFEST.as("prev")).on(ACC_MANIFEST.PREV_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("prev").ACC_MANIFEST_ID)))
                .set(ACC_MANIFEST.ACC_ID, ACC_MANIFEST.as("prev").ACC_ID)
                .set(ACC_MANIFEST.LOG_ID, ACC_MANIFEST.as("prev").LOG_ID)
                .where(and(ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update ACC_MANIFEST.BASED_ACC_MANIFEST_ID if ACC_MANIFEST.BASED_MANIFEST.ACC_ID is not equal to ACC.BASED_ACC_ID
        dslContext().update(ACC_MANIFEST
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID))
                        .join(ACC.as("based")).on(ACC.BASED_ACC_ID.eq(ACC.as("based").ACC_ID))
                        .join(ACC.as("revised")).on(ACC.as("based").GUID.eq(ACC.as("revised").GUID)) // for the case that the associated component has revised
                        .join(ACC_MANIFEST.as("based_manifest")).on(and(
                                ACC.as("revised").ACC_ID.eq(ACC_MANIFEST.as("based_manifest").ACC_ID),
                                ACC_MANIFEST.as("based_manifest").RELEASE_ID.eq(ACC_MANIFEST.RELEASE_ID))))
                .set(ACC_MANIFEST.BASED_ACC_MANIFEST_ID, ACC_MANIFEST.as("based_manifest").ACC_MANIFEST_ID)
                .where(and(
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC_MANIFEST.BASED_ACC_MANIFEST_ID.notEqual(ACC_MANIFEST.as("based_manifest").ACC_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext().update(ACC_MANIFEST.as("working")
                        .join(ACC_MANIFEST.as("release")).on(and(
                                ACC_MANIFEST.as("working").ACC_ID.eq(ACC_MANIFEST.as("release").ACC_ID),
                                ACC_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                ACC_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(ACC_MANIFEST.as("replacement_in_working")).on(and(
                                ACC_MANIFEST.as("working").REPLACEMENT_ACC_MANIFEST_ID.eq(ACC_MANIFEST.as("replacement_in_working").ACC_MANIFEST_ID),
                                ACC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(ACC_MANIFEST.as("replacement_in_release")).on(and(
                                ACC_MANIFEST.as("replacement_in_working").ACC_ID.eq(ACC_MANIFEST.as("replacement_in_release").ACC_ID),
                                ACC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(ACC_MANIFEST.as("release").REPLACEMENT_ACC_MANIFEST_ID, ACC_MANIFEST.as("replacement_in_release").ACC_MANIFEST_ID)
                .where(and(
                        ACC_MANIFEST.as("working").REPLACEMENT_ACC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAsccpDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(ASCCP_MANIFEST.join(ACC_MANIFEST)
                        .on(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.eq(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(and(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // Set ASCCP_MANIFEST.ASCCP_ID to PREV.ASCCP_ID if the ASCCP in Working branch is not in ReleaseDraft nor Published states.
        dslContext().update(ASCCP_MANIFEST
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(ASCCP_MANIFEST.as("prev")).on(ASCCP_MANIFEST.PREV_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("prev").ASCCP_MANIFEST_ID)))
                .set(ASCCP_MANIFEST.ASCCP_ID, ASCCP_MANIFEST.as("prev").ASCCP_ID)
                .set(ASCCP_MANIFEST.LOG_ID, ASCCP_MANIFEST.as("prev").LOG_ID)
                .where(and(ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCCP.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID if ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST.ACC_ID is not equal to ASCCP.ROLE_OF_ACC_ID
        dslContext().update(ASCCP_MANIFEST
                        .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(ACC).on(ASCCP.ROLE_OF_ACC_ID.eq(ACC.ACC_ID))
                        .join(ACC.as("revised")).on(ACC.GUID.eq(ACC.as("revised").GUID)) // for the case that the associated component has revised
                        .join(ACC_MANIFEST).on(and(
                                ACC.as("revised").ACC_ID.eq(ACC_MANIFEST.ACC_ID),
                                ACC_MANIFEST.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))))
                .set(ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCCP_MANIFEST.ROLE_OF_ACC_MANIFEST_ID.notEqual(ACC_MANIFEST.ACC_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext().update(ASCCP_MANIFEST.as("working")
                        .join(ASCCP_MANIFEST.as("release")).on(and(
                                ASCCP_MANIFEST.as("working").ASCCP_ID.eq(ASCCP_MANIFEST.as("release").ASCCP_ID),
                                ASCCP_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                ASCCP_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(ASCCP_MANIFEST.as("replacement_in_working")).on(and(
                                ASCCP_MANIFEST.as("working").REPLACEMENT_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.as("replacement_in_working").ASCCP_MANIFEST_ID),
                                ASCCP_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(ASCCP_MANIFEST.as("replacement_in_release")).on(and(
                                ASCCP_MANIFEST.as("replacement_in_working").ASCCP_ID.eq(ASCCP_MANIFEST.as("replacement_in_release").ASCCP_ID),
                                ASCCP_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(ASCCP_MANIFEST.as("release").REPLACEMENT_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.as("replacement_in_release").ASCCP_MANIFEST_ID)
                .where(and(
                        ASCCP_MANIFEST.as("working").REPLACEMENT_ASCCP_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateBccpDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {

        dslContext().update(BCCP_MANIFEST.join(DT_MANIFEST)
                        .on(BCCP_MANIFEST.BDT_MANIFEST_ID.eq(DT_MANIFEST.NEXT_DT_MANIFEST_ID)))
                .set(BCCP_MANIFEST.BDT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(and(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // Set BCCP_MANIFEST.BCCP_ID to PREV.BCCP_ID if the BCCP in Working branch is not in ReleaseDraft nor Published states.
        dslContext().update(BCCP_MANIFEST
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(BCCP_MANIFEST.as("prev")).on(BCCP_MANIFEST.PREV_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("prev").BCCP_MANIFEST_ID)))
                .set(BCCP_MANIFEST.BCCP_ID, BCCP_MANIFEST.as("prev").BCCP_ID)
                .set(BCCP_MANIFEST.LOG_ID, BCCP_MANIFEST.as("prev").LOG_ID)
                .where(and(BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCCP.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update BCCP_MANIFEST.BDT_MANIFEST_ID if BCCP_MANIFEST.BDT_MANIFEST.BDT_ID is not equal to BCCP.BDT_ID
        dslContext().update(BCCP_MANIFEST
                        .join(BCCP).on(BCCP_MANIFEST.BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(DT).on(BCCP.BDT_ID.eq(DT.DT_ID))
                        .join(DT.as("revised")).on(DT.GUID.eq(DT.as("revised").GUID)) // for the case that the associated component has revised
                        .join(DT_MANIFEST).on(and(
                                DT.as("revised").DT_ID.eq(DT_MANIFEST.DT_ID),
                                DT_MANIFEST.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))))
                .set(BCCP_MANIFEST.BDT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(and(
                        BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCCP_MANIFEST.BDT_MANIFEST_ID.notEqual(DT_MANIFEST.DT_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext().update(BCCP_MANIFEST.as("working")
                        .join(BCCP_MANIFEST.as("release")).on(and(
                                BCCP_MANIFEST.as("working").BCCP_ID.eq(BCCP_MANIFEST.as("release").BCCP_ID),
                                BCCP_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                BCCP_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(BCCP_MANIFEST.as("replacement_in_working")).on(and(
                                BCCP_MANIFEST.as("working").REPLACEMENT_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.as("replacement_in_working").BCCP_MANIFEST_ID),
                                BCCP_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(BCCP_MANIFEST.as("replacement_in_release")).on(and(
                                BCCP_MANIFEST.as("replacement_in_working").BCCP_ID.eq(BCCP_MANIFEST.as("replacement_in_release").BCCP_ID),
                                BCCP_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(BCCP_MANIFEST.as("release").REPLACEMENT_BCCP_MANIFEST_ID, BCCP_MANIFEST.as("replacement_in_release").BCCP_MANIFEST_ID)
                .where(and(
                        BCCP_MANIFEST.as("working").REPLACEMENT_BCCP_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAsccDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(ASCC_MANIFEST
                        .join(ACC_MANIFEST)
                        .on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID))
                        .join(ASCCP_MANIFEST)
                        .on(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.NEXT_ASCCP_MANIFEST_ID)))
                .set(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .set(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(and(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // Set ASCC_MANIFEST.ASCC_ID to PREV.ASCC_ID if the DT in Working branch is not in ReleaseDraft nor Published states.
        dslContext().update(ASCC_MANIFEST
                        .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                        .join(ASCC_MANIFEST.as("prev")).on(ASCC_MANIFEST.PREV_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("prev").ASCC_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(ASCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID)))
                .set(ASCC_MANIFEST.ASCC_ID, ASCC_MANIFEST.as("prev").ASCC_ID)
                .where(and(ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID if ASCCP_MANIFEST.ASCCP_MANIFEST.ASCCP_ID is not equal to ASCC.TO_ASCCP_ID
        dslContext().update(ASCC_MANIFEST
                        .join(ASCC).on(ASCC_MANIFEST.ASCC_ID.eq(ASCC.ASCC_ID))
                        .join(ASCCP).on(ASCC.TO_ASCCP_ID.eq(ASCCP.ASCCP_ID))
                        .join(ASCCP.as("revised")).on(ASCCP.GUID.eq(ASCCP.as("revised").GUID)) // for the case that the associated component has revised
                        .join(ASCCP_MANIFEST).on(and(
                                ASCCP.as("revised").ASCCP_ID.eq(ASCCP_MANIFEST.ASCCP_ID),
                                ASCC_MANIFEST.RELEASE_ID.eq(ASCCP_MANIFEST.RELEASE_ID))))
                .set(ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID, ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                .where(and(
                        ASCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ASCC_MANIFEST.TO_ASCCP_MANIFEST_ID.notEqual(ASCCP_MANIFEST.ASCCP_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext().update(ASCC_MANIFEST.as("working")
                        .join(ASCC_MANIFEST.as("release")).on(and(
                                ASCC_MANIFEST.as("working").ASCC_ID.eq(ASCC_MANIFEST.as("release").ASCC_ID),
                                ASCC_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                ASCC_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(ASCC_MANIFEST.as("replacement_in_working")).on(and(
                                ASCC_MANIFEST.as("working").REPLACEMENT_ASCC_MANIFEST_ID.eq(ASCC_MANIFEST.as("replacement_in_working").ASCC_MANIFEST_ID),
                                ASCC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(ASCC_MANIFEST.as("replacement_in_release")).on(and(
                                ASCC_MANIFEST.as("replacement_in_working").ASCC_ID.eq(ASCC_MANIFEST.as("replacement_in_release").ASCC_ID),
                                ASCC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(ASCC_MANIFEST.as("release").REPLACEMENT_ASCC_MANIFEST_ID, ASCC_MANIFEST.as("replacement_in_release").ASCC_MANIFEST_ID)
                .where(and(
                        ASCC_MANIFEST.as("working").REPLACEMENT_ASCC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateBccDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(BCC_MANIFEST.join(ACC_MANIFEST)
                        .on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.NEXT_ACC_MANIFEST_ID))
                        .join(BCCP_MANIFEST)
                        .on(BCC_MANIFEST.TO_BCCP_MANIFEST_ID.eq(BCCP_MANIFEST.NEXT_BCCP_MANIFEST_ID)))
                .set(BCC_MANIFEST.FROM_ACC_MANIFEST_ID, ACC_MANIFEST.ACC_MANIFEST_ID)
                .set(BCC_MANIFEST.TO_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(and(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCCP_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // Set BCC_MANIFEST.BCC_ID to PREV.BCC_ID if the DT in Working branch is not in ReleaseDraft nor Published states.
        dslContext().update(BCC_MANIFEST
                        .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                        .join(BCC_MANIFEST.as("prev")).on(BCC_MANIFEST.PREV_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("prev").BCC_MANIFEST_ID))
                        .join(ACC_MANIFEST).on(BCC_MANIFEST.FROM_ACC_MANIFEST_ID.eq(ACC_MANIFEST.ACC_MANIFEST_ID))
                        .join(ACC).on(ACC_MANIFEST.ACC_ID.eq(ACC.ACC_ID)))
                .set(BCC_MANIFEST.BCC_ID, BCC_MANIFEST.as("prev").BCC_ID)
                .where(and(BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        ACC.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update BCC_MANIFEST.TO_BCCP_MANIFEST_ID if BCCP_MANIFEST.BCCP_MANIFEST.BCCP_ID is not equal to BCC.TO_BCCP_ID
        dslContext().update(BCC_MANIFEST
                        .join(BCC).on(BCC_MANIFEST.BCC_ID.eq(BCC.BCC_ID))
                        .join(BCCP).on(BCC.TO_BCCP_ID.eq(BCCP.BCCP_ID))
                        .join(BCCP.as("revised")).on(BCCP.GUID.eq(BCCP.as("revised").GUID)) // for the case that the associated component has revised
                        .join(BCCP_MANIFEST).on(and(
                                BCCP.as("revised").BCCP_ID.eq(BCCP_MANIFEST.BCCP_ID),
                                BCC_MANIFEST.RELEASE_ID.eq(BCCP_MANIFEST.RELEASE_ID))))
                .set(BCC_MANIFEST.TO_BCCP_MANIFEST_ID, BCCP_MANIFEST.BCCP_MANIFEST_ID)
                .where(and(
                        BCC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        BCC_MANIFEST.TO_BCCP_MANIFEST_ID.notEqual(BCCP_MANIFEST.BCCP_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext().update(BCC_MANIFEST.as("working")
                        .join(BCC_MANIFEST.as("release")).on(and(
                                BCC_MANIFEST.as("working").BCC_ID.eq(BCC_MANIFEST.as("release").BCC_ID),
                                BCC_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                BCC_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(BCC_MANIFEST.as("replacement_in_working")).on(and(
                                BCC_MANIFEST.as("working").REPLACEMENT_BCC_MANIFEST_ID.eq(BCC_MANIFEST.as("replacement_in_working").BCC_MANIFEST_ID),
                                BCC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(BCC_MANIFEST.as("replacement_in_release")).on(and(
                                BCC_MANIFEST.as("replacement_in_working").BCC_ID.eq(BCC_MANIFEST.as("replacement_in_release").BCC_ID),
                                BCC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(BCC_MANIFEST.as("release").REPLACEMENT_BCC_MANIFEST_ID, BCC_MANIFEST.as("replacement_in_release").BCC_MANIFEST_ID)
                .where(and(
                        BCC_MANIFEST.as("working").REPLACEMENT_BCC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateDtDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(DT_MANIFEST.join(DT_MANIFEST.as("based"))
                        .on(DT_MANIFEST.BASED_DT_MANIFEST_ID.eq(DT_MANIFEST.as("based").NEXT_DT_MANIFEST_ID)))
                .set(DT_MANIFEST.BASED_DT_MANIFEST_ID, DT_MANIFEST.as("based").DT_MANIFEST_ID)
                .where(and(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_MANIFEST.as("based").RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // Set DT_MANIFEST.DT_ID to PREV.DT_ID if the DT in Working branch is not in ReleaseDraft nor Published states.
        dslContext().update(DT_MANIFEST
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .join(DT_MANIFEST.as("prev")).on(DT_MANIFEST.PREV_DT_MANIFEST_ID.eq(DT_MANIFEST.as("prev").DT_MANIFEST_ID)))
                .set(DT_MANIFEST.DT_ID, DT_MANIFEST.as("prev").DT_ID)
                .set(DT_MANIFEST.LOG_ID, DT_MANIFEST.as("prev").LOG_ID)
                .where(and(DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Issue #1552
        // Update DT_MANIFEST.BASED_DT_MANIFEST_ID if DT_MANIFEST.BASED_MANIFEST.DT_ID is not equal to DT.BASED_DT_ID
        dslContext().update(DT_MANIFEST
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                        .join(DT.as("based")).on(DT.BASED_DT_ID.eq(DT.as("based").DT_ID))
                        .join(DT.as("revised")).on(DT.as("based").GUID.eq(DT.as("revised").GUID)) // for the case that the associated component has revised
                        .join(DT_MANIFEST.as("based_manifest")).on(and(
                                DT.as("revised").DT_ID.eq(DT_MANIFEST.as("based_manifest").DT_ID),
                                DT_MANIFEST.as("based_manifest").RELEASE_ID.eq(DT_MANIFEST.RELEASE_ID))))
                .set(DT_MANIFEST.BASED_DT_MANIFEST_ID, DT_MANIFEST.as("based_manifest").DT_MANIFEST_ID)
                .where(and(
                        DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_MANIFEST.BASED_DT_MANIFEST_ID.notEqual(DT_MANIFEST.as("based_manifest").DT_MANIFEST_ID)
                ))
                .execute();

        // Update replacement
        dslContext().update(DT_MANIFEST.as("working")
                        .join(DT_MANIFEST.as("release")).on(and(
                                DT_MANIFEST.as("working").DT_ID.eq(DT_MANIFEST.as("release").DT_ID),
                                DT_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(DT_MANIFEST.as("replacement_in_working")).on(and(
                                DT_MANIFEST.as("working").REPLACEMENT_DT_MANIFEST_ID.eq(DT_MANIFEST.as("replacement_in_working").DT_MANIFEST_ID),
                                DT_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(DT_MANIFEST.as("replacement_in_release")).on(and(
                                DT_MANIFEST.as("replacement_in_working").DT_ID.eq(DT_MANIFEST.as("replacement_in_release").DT_ID),
                                DT_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(DT_MANIFEST.as("release").REPLACEMENT_DT_MANIFEST_ID, DT_MANIFEST.as("replacement_in_release").DT_MANIFEST_ID)
                .where(and(
                        DT_MANIFEST.as("working").REPLACEMENT_DT_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateDtScDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(DT_SC_MANIFEST.join(DT_MANIFEST)
                        .on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.NEXT_DT_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID, DT_MANIFEST.DT_MANIFEST_ID)
                .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        dslContext().update(DT_SC_MANIFEST.join(DT_SC_MANIFEST.as("based"))
                        .on(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("based").NEXT_DT_SC_MANIFEST_ID)))
                .set(DT_SC_MANIFEST.BASED_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.as("based").DT_SC_MANIFEST_ID)
                .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT_SC_MANIFEST.as("based").RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        dslContext().update(DT_SC_MANIFEST
                        .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                        .join(DT_SC_MANIFEST.as("prev")).on(DT_SC_MANIFEST.PREV_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("prev").DT_SC_MANIFEST_ID))
                        .join(DT_MANIFEST).on(DT_SC_MANIFEST.OWNER_DT_MANIFEST_ID.eq(DT_MANIFEST.DT_MANIFEST_ID))
                        .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID)))
                .set(DT_SC_MANIFEST.DT_SC_ID, DT_SC_MANIFEST.as("prev").DT_SC_ID)
                .where(and(DT_SC_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        DT.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext().update(DT_SC_MANIFEST.as("working")
                        .join(DT_SC_MANIFEST.as("release")).on(and(
                                DT_SC_MANIFEST.as("working").DT_SC_ID.eq(DT_SC_MANIFEST.as("release").DT_SC_ID),
                                DT_SC_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                DT_SC_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(DT_SC_MANIFEST.as("replacement_in_working")).on(and(
                                DT_SC_MANIFEST.as("working").REPLACEMENT_DT_SC_MANIFEST_ID.eq(DT_SC_MANIFEST.as("replacement_in_working").DT_SC_MANIFEST_ID),
                                DT_SC_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(DT_SC_MANIFEST.as("replacement_in_release")).on(and(
                                DT_SC_MANIFEST.as("replacement_in_working").DT_SC_ID.eq(DT_SC_MANIFEST.as("replacement_in_release").DT_SC_ID),
                                DT_SC_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(DT_SC_MANIFEST.as("release").REPLACEMENT_DT_SC_MANIFEST_ID, DT_SC_MANIFEST.as("replacement_in_release").DT_SC_MANIFEST_ID)
                .where(and(
                        DT_SC_MANIFEST.as("working").REPLACEMENT_DT_SC_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateXbtDependencies(ReleaseId releaseId) {
        dslContext().update(XBT_MANIFEST
                        .join(XBT).on(XBT_MANIFEST.XBT_ID.eq(XBT.XBT_ID))
                        .join(XBT_MANIFEST.as("prev")).on(XBT_MANIFEST.PREV_XBT_MANIFEST_ID.eq(XBT_MANIFEST.as("prev").XBT_MANIFEST_ID)))
                .set(XBT_MANIFEST.XBT_ID, XBT_MANIFEST.as("prev").XBT_ID)
                .set(XBT_MANIFEST.LOG_ID, XBT_MANIFEST.as("prev").LOG_ID)
                .where(and(XBT_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        XBT.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();
    }

    private void updateCodeListDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(CODE_LIST_MANIFEST.join(CODE_LIST_MANIFEST.as("based"))
                        .on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based").NEXT_CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.as("based").CODE_LIST_MANIFEST_ID)
                .where(and(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST_MANIFEST.as("based").RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        dslContext().update(CODE_LIST_MANIFEST
                        .join(CODE_LIST).on(CODE_LIST_MANIFEST.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .join(CODE_LIST_MANIFEST.as("prev")).on(CODE_LIST_MANIFEST.PREV_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("prev").CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_MANIFEST.CODE_LIST_ID, CODE_LIST_MANIFEST.as("prev").CODE_LIST_ID)
                .set(CODE_LIST_MANIFEST.LOG_ID, CODE_LIST_MANIFEST.as("prev").LOG_ID)
                .where(and(CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext().update(CODE_LIST_MANIFEST.as("working")
                        .join(CODE_LIST_MANIFEST.as("release")).on(and(
                                CODE_LIST_MANIFEST.as("working").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("release").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                CODE_LIST_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(CODE_LIST_MANIFEST.as("replacement_in_working")).on(and(
                                CODE_LIST_MANIFEST.as("working").REPLACEMENT_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("replacement_in_working").CODE_LIST_MANIFEST_ID),
                                CODE_LIST_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(CODE_LIST_MANIFEST.as("replacement_in_release")).on(and(
                                CODE_LIST_MANIFEST.as("replacement_in_working").CODE_LIST_ID.eq(CODE_LIST_MANIFEST.as("replacement_in_release").CODE_LIST_ID),
                                CODE_LIST_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(CODE_LIST_MANIFEST.as("release").REPLACEMENT_CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.as("replacement_in_release").CODE_LIST_MANIFEST_ID)
                .where(and(
                        CODE_LIST_MANIFEST.as("working").REPLACEMENT_CODE_LIST_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateCodeListValueDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(CODE_LIST_VALUE_MANIFEST.join(CODE_LIST_MANIFEST)
                        .on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.NEXT_CODE_LIST_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.CODE_LIST_MANIFEST_ID, CODE_LIST_MANIFEST.CODE_LIST_MANIFEST_ID)
                .where(and(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        dslContext().update(CODE_LIST_VALUE_MANIFEST
                        .join(CODE_LIST_VALUE).on(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE.CODE_LIST_VALUE_ID))
                        .join(CODE_LIST).on(CODE_LIST_VALUE.CODE_LIST_ID.eq(CODE_LIST.CODE_LIST_ID))
                        .join(CODE_LIST_VALUE_MANIFEST.as("prev")).on(CODE_LIST_VALUE_MANIFEST.PREV_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("prev").CODE_LIST_VALUE_MANIFEST_ID)))
                .set(CODE_LIST_VALUE_MANIFEST.CODE_LIST_VALUE_ID, CODE_LIST_VALUE_MANIFEST.as("prev").CODE_LIST_VALUE_ID)
                .where(and(CODE_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext().update(CODE_LIST_VALUE_MANIFEST.as("working")
                        .join(CODE_LIST_VALUE_MANIFEST.as("release")).on(and(
                                CODE_LIST_VALUE_MANIFEST.as("working").CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.as("release").CODE_LIST_VALUE_ID),
                                CODE_LIST_VALUE_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                CODE_LIST_VALUE_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(CODE_LIST_VALUE_MANIFEST.as("replacement_in_working")).on(and(
                                CODE_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID.eq(CODE_LIST_VALUE_MANIFEST.as("replacement_in_working").CODE_LIST_VALUE_MANIFEST_ID),
                                CODE_LIST_VALUE_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(CODE_LIST_VALUE_MANIFEST.as("replacement_in_release")).on(and(
                                CODE_LIST_VALUE_MANIFEST.as("replacement_in_working").CODE_LIST_VALUE_ID.eq(CODE_LIST_VALUE_MANIFEST.as("replacement_in_release").CODE_LIST_VALUE_ID),
                                CODE_LIST_VALUE_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(CODE_LIST_VALUE_MANIFEST.as("release").REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID, CODE_LIST_VALUE_MANIFEST.as("replacement_in_release").CODE_LIST_VALUE_MANIFEST_ID)
                .where(and(
                        CODE_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_CODE_LIST_VALUE_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAgencyIdListDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(AGENCY_ID_LIST_MANIFEST.join(AGENCY_ID_LIST_MANIFEST.as("based"))
                        .on(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("based").NEXT_AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.as("based").AGENCY_ID_LIST_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST_MANIFEST.as("based").RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        dslContext().update(AGENCY_ID_LIST_MANIFEST
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .join(AGENCY_ID_LIST_MANIFEST.as("prev")).on(AGENCY_ID_LIST_MANIFEST.PREV_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("prev").AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID, AGENCY_ID_LIST_MANIFEST.as("prev").AGENCY_ID_LIST_ID)
                .set(AGENCY_ID_LIST_MANIFEST.LOG_ID, AGENCY_ID_LIST_MANIFEST.as("prev").LOG_ID)
                .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // Update replacement
        dslContext().update(AGENCY_ID_LIST_MANIFEST.as("working")
                        .join(AGENCY_ID_LIST_MANIFEST.as("release")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("working").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("release").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                AGENCY_ID_LIST_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(AGENCY_ID_LIST_MANIFEST.as("replacement_in_working")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_MANIFEST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(AGENCY_ID_LIST_MANIFEST.as("replacement_in_release")).on(and(
                                AGENCY_ID_LIST_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_ID),
                                AGENCY_ID_LIST_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(AGENCY_ID_LIST_MANIFEST.as("release").REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_MANIFEST_ID)
                .where(and(
                        AGENCY_ID_LIST_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

    private void updateAgencyIdListValueDependencies(ReleaseId releaseId, ReleaseId workingReleaseId) {
        dslContext().update(AGENCY_ID_LIST_VALUE_MANIFEST.join(AGENCY_ID_LIST_MANIFEST)
                        .on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.NEXT_AGENCY_ID_LIST_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID, AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        dslContext().update(AGENCY_ID_LIST_VALUE_MANIFEST
                        .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .join(AGENCY_ID_LIST).on(AGENCY_ID_LIST_VALUE.OWNER_LIST_ID.eq(AGENCY_ID_LIST.AGENCY_ID_LIST_ID))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev")).on(AGENCY_ID_LIST_VALUE_MANIFEST.PREV_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID, AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_ID)
                .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST.STATE.notIn(Arrays.asList(CcState.ReleaseDraft, CcState.Published))))
                .execute();

        // for update agency id list value manifest id
        dslContext().update(AGENCY_ID_LIST_MANIFEST.join(AGENCY_ID_LIST_VALUE_MANIFEST.as("value"))
                        .on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("value").NEXT_AGENCY_ID_LIST_VALUE_MANIFEST_ID)))
                .set(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.as("value").AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        AGENCY_ID_LIST_VALUE_MANIFEST.as("value").RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // To update `code_list_manifest`.`agency_id_list_value_manifest_id`
        dslContext().update(CODE_LIST_MANIFEST
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev")).on(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                        .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.as("prev").AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID)))
                .set(CODE_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(and(AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(valueOf(releaseId)),
                        CODE_LIST_MANIFEST.RELEASE_ID.eq(valueOf(releaseId))))
                .execute();

        // Update replacement
        dslContext().update(AGENCY_ID_LIST_VALUE_MANIFEST.as("working")
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("release")).on(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("working").AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("release").AGENCY_ID_LIST_VALUE_ID),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("working").RELEASE_ID.eq(valueOf(workingReleaseId)),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("release").RELEASE_ID.eq(valueOf(releaseId))
                        ))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working")).on(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_VALUE_MANIFEST_ID),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working").RELEASE_ID.eq(valueOf(workingReleaseId))
                        ))
                        .join(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release")).on(and(
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_working").AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_VALUE_ID),
                                AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release").RELEASE_ID.eq(valueOf(releaseId))
                        )))
                .set(AGENCY_ID_LIST_VALUE_MANIFEST.as("release").REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID, AGENCY_ID_LIST_VALUE_MANIFEST.as("replacement_in_release").AGENCY_ID_LIST_VALUE_MANIFEST_ID)
                .where(and(
                        AGENCY_ID_LIST_VALUE_MANIFEST.as("working").REPLACEMENT_AGENCY_ID_LIST_VALUE_MANIFEST_ID.isNotNull()
                ))
                .execute();
    }

}
