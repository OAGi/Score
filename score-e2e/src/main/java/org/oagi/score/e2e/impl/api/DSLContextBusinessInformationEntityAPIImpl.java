package org.oagi.score.e2e.impl.api;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.oagi.score.e2e.api.APIFactory;
import org.oagi.score.e2e.api.BusinessInformationEntityAPI;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AbieRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.AsbiepRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.BizCtxAssignmentRecord;
import org.oagi.score.e2e.impl.api.jooq.entity.tables.records.TopLevelAsbiepRecord;
import org.oagi.score.e2e.obj.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.e2e.impl.api.jooq.entity.Tables.*;

public class DSLContextBusinessInformationEntityAPIImpl implements BusinessInformationEntityAPI {

    private final DSLContext dslContext;

    private final APIFactory apiFactory;

    public DSLContextBusinessInformationEntityAPIImpl(DSLContext dslContext, APIFactory apiFactory) {
        this.dslContext = dslContext;
        this.apiFactory = apiFactory;
    }

    @Override
    public TopLevelASBIEPObject getTopLevelASBIEPByID(BigInteger topLevelAsbiepId) {
        List<Field<?>> fields = new ArrayList();
        fields.addAll(Arrays.asList(TOP_LEVEL_ASBIEP.fields()));
        fields.add(ASBIEP.BASED_ASCCP_MANIFEST_ID);
        fields.add(ASCCP_MANIFEST.ASCCP_ID);
        fields.add(ASCCP.PROPERTY_TERM);
        fields.add(ASCCP.DEN);
        fields.add(RELEASE.RELEASE_NUM);
        return dslContext.select(fields)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiepId)))
                .fetchOne(record -> topLevelASBIEPMapper(record));
    }

    @Override
    public TopLevelASBIEPObject getTopLevelASBIEPByDENAndReleaseNum(String den, String releaseNum) {
        ULong releaseId = getReleaseIdByReleaseNum(releaseNum);
        List<Field<?>> fields = new ArrayList();
        fields.addAll(Arrays.asList(TOP_LEVEL_ASBIEP.fields()));
        fields.add(ASBIEP.BASED_ASCCP_MANIFEST_ID);
        fields.add(ASCCP_MANIFEST.ASCCP_ID);
        fields.add(ASCCP.PROPERTY_TERM);
        fields.add(ASCCP.DEN);
        fields.add(RELEASE.RELEASE_NUM);
        return dslContext.select(fields)
                .from(TOP_LEVEL_ASBIEP)
                .join(ASBIEP).on(TOP_LEVEL_ASBIEP.ASBIEP_ID.eq(ASBIEP.ASBIEP_ID))
                .join(ASCCP_MANIFEST).on(ASBIEP.BASED_ASCCP_MANIFEST_ID.eq(ASCCP_MANIFEST.ASCCP_MANIFEST_ID))
                .join(ASCCP).on(ASCCP_MANIFEST.ASCCP_ID.eq(ASCCP.ASCCP_ID))
                .join(RELEASE).on(ASCCP_MANIFEST.RELEASE_ID.eq(RELEASE.RELEASE_ID))
                .where(and(
                        ASCCP_MANIFEST.RELEASE_ID.eq(releaseId),
                        ASCCP.DEN.eq(den)))
                .fetchOne(record -> topLevelASBIEPMapper(record));
    }

    @Override
    public void updateTopLevelASBIEP(TopLevelASBIEPObject topLevelASBIEP) {
        dslContext.update(TOP_LEVEL_ASBIEP)
                .set(TOP_LEVEL_ASBIEP.VERSION, topLevelASBIEP.getVersion())
                .set(TOP_LEVEL_ASBIEP.STATUS, topLevelASBIEP.getStatus())
                .set(TOP_LEVEL_ASBIEP.STATE, topLevelASBIEP.getState())
                .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelASBIEP.getTopLevelAsbiepId())))
                .execute();
    }

    private TopLevelASBIEPObject topLevelASBIEPMapper(Record record) {
        TopLevelASBIEPObject topLevelASBIEPObject = new TopLevelASBIEPObject();
        topLevelASBIEPObject.setTopLevelAsbiepId(record.get(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID).toBigInteger());
        topLevelASBIEPObject.setAsbiepId(record.get(TOP_LEVEL_ASBIEP.ASBIEP_ID).toBigInteger());
        topLevelASBIEPObject.setVersion(record.get(TOP_LEVEL_ASBIEP.VERSION));
        topLevelASBIEPObject.setStatus(record.get(TOP_LEVEL_ASBIEP.STATUS));
        topLevelASBIEPObject.setState(record.get(TOP_LEVEL_ASBIEP.STATE));
        topLevelASBIEPObject.setLastUpdatedBy(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATED_BY).toBigInteger());
        topLevelASBIEPObject.setReleaseId(record.get(TOP_LEVEL_ASBIEP.RELEASE_ID).toBigInteger());
        topLevelASBIEPObject.setPropertyTerm(record.get(ASCCP.PROPERTY_TERM));
        topLevelASBIEPObject.setOwnerUserId(record.get(TOP_LEVEL_ASBIEP.OWNER_USER_ID).toBigInteger());
        topLevelASBIEPObject.setLastUpdateTimestamp(record.get(TOP_LEVEL_ASBIEP.LAST_UPDATE_TIMESTAMP));
        topLevelASBIEPObject.setDen(record.get(ASCCP.DEN));
        topLevelASBIEPObject.setReleaseNumber(record.get(RELEASE.RELEASE_NUM));
        return topLevelASBIEPObject;
    }

    private ULong getReleaseIdByReleaseNum(String releaseNum) {
        return dslContext.select(RELEASE.RELEASE_ID)
                .from(RELEASE)
                .where(RELEASE.RELEASE_NUM.eq(releaseNum))
                .fetchOneInto(ULong.class);
    }

    @Override
    public TopLevelASBIEPObject generateRandomTopLevelASBIEP(List<BusinessContextObject> businessContexts,
                                                             ASCCPObject asccp, AppUserObject creator, String state) {
        ReleaseObject release = apiFactory.getReleaseAPI().getReleaseById(asccp.getReleaseId());
        TopLevelASBIEPObject topLevelAsbiep = createRandomTopLevelAsbiep(creator, release, state);

        ACCObject acc = this.apiFactory.getCoreComponentAPI().getACCByManifestId(asccp.getRoleOfAccManifestId());
        ABIEObject roleOfAbie = createRandomABIE(asccp, acc, creator, topLevelAsbiep);
        ASBIEPObject asbiep = createRandomASBIEP(asccp, roleOfAbie, creator, topLevelAsbiep);
        updateTopLevelAsbiepWithAsbiepID(topLevelAsbiep, asbiep);
        for (BusinessContextObject businessContext : businessContexts) {
            addBusinessContextAssignment(topLevelAsbiep, businessContext);
        }
        topLevelAsbiep.setPropertyTerm(asccp.getPropertyTerm());
        topLevelAsbiep.setDen(asccp.getDen());
        topLevelAsbiep.setReleaseNumber(release.getReleaseNumber());
        return topLevelAsbiep;
    }

    private TopLevelASBIEPObject createRandomTopLevelAsbiep(AppUserObject user, ReleaseObject release, String state) {
        TopLevelASBIEPObject topLevelAsbiep = TopLevelASBIEPObject.createRandomTopLevelAsbiepInRelease(release, user, state);

        TopLevelAsbiepRecord topLevelAsbiepRecord = new TopLevelAsbiepRecord();

        topLevelAsbiepRecord.setOwnerUserId(ULong.valueOf(user.getAppUserId()));
        topLevelAsbiepRecord.setLastUpdateTimestamp(topLevelAsbiep.getLastUpdateTimestamp());
        topLevelAsbiepRecord.setLastUpdatedBy(ULong.valueOf(user.getAppUserId()));
        topLevelAsbiepRecord.setReleaseId(ULong.valueOf(topLevelAsbiep.getReleaseId()));
        topLevelAsbiepRecord.setVersion(topLevelAsbiep.getVersion());
        topLevelAsbiepRecord.setState(topLevelAsbiep.getState());
        topLevelAsbiepRecord.setStatus(topLevelAsbiep.getStatus());
        topLevelAsbiepRecord.setInverseMode((byte) (topLevelAsbiep.isInverseMode() ? 1 : 0));

        BigInteger topLevelAsbiepId = dslContext.insertInto(TOP_LEVEL_ASBIEP)
                .set(topLevelAsbiepRecord)
                .returning(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID)
                .fetchOne().getTopLevelAsbiepId().toBigInteger();
        topLevelAsbiep.setTopLevelAsbiepId(topLevelAsbiepId);
        return topLevelAsbiep;
    }

    private ABIEObject createRandomABIE(ASCCPObject asccp, ACCObject acc,
                                        AppUserObject user, TopLevelASBIEPObject topLevelAsbiep) {
        ABIEObject abie = ABIEObject.createRandomABIE(acc, user, topLevelAsbiep);
        abie.setPath("ASCCP-" + asccp.getAsccpManifestId() + ">" + abie.getPath());

        AbieRecord abieRecord = new AbieRecord();
        abieRecord.setGuid(abie.getGuid());
        abieRecord.setBasedAccManifestId(ULong.valueOf(abie.getBasedACCManifestId()));
        abieRecord.setPath(abie.getPath());
        abieRecord.setHashPath(abie.getHashPath());
        abieRecord.setDefinition(abie.getDefinition());
        abieRecord.setCreatedBy(ULong.valueOf(abie.getCreatedBy()));
        abieRecord.setLastUpdatedBy(ULong.valueOf(abie.getLastUpdatedBy()));
        abieRecord.setCreationTimestamp(abie.getCreationTimestamp());
        abieRecord.setLastUpdateTimestamp(abie.getLastUpdateTimestamp());
        abieRecord.setRemark(abie.getRemark());
        abieRecord.setBizTerm(abie.getBizTerm());
        abieRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));

        BigInteger abieId = dslContext.insertInto(ABIE)
                .set(abieRecord)
                .returning(ABIE.ABIE_ID)
                .fetchOne().getAbieId().toBigInteger();
        abie.setAbieId(abieId);
        return abie;
    }

    private ASBIEPObject createRandomASBIEP(ASCCPObject asccp, ABIEObject roleOfAbie,
                                            AppUserObject user, TopLevelASBIEPObject topLevelAsbiep) {
        ASBIEPObject asbiep = ASBIEPObject.createRandomAsbiep(asccp, roleOfAbie, user, topLevelAsbiep);
        AsbiepRecord asbiepRecord = new AsbiepRecord();

        asbiepRecord.setGuid(asbiep.getGuid());
        asbiepRecord.setBasedAsccpManifestId(ULong.valueOf(asccp.getAsccpManifestId()));
        asbiepRecord.setPath(asbiep.getPath());
        asbiepRecord.setHashPath(asbiep.getHashPath());
        asbiepRecord.setRoleOfAbieId(ULong.valueOf(asbiep.getRoleOfAbieId()));
        asbiepRecord.setDefinition(asbiep.getDefinition());
        asbiepRecord.setRemark(asbiep.getRemark());
        asbiepRecord.setBizTerm(asbiep.getBizTerm());
        asbiepRecord.setCreatedBy(ULong.valueOf(asbiep.getCreatedBy()));
        asbiepRecord.setLastUpdatedBy(ULong.valueOf(asbiep.getLastUpdatedBy()));
        asbiepRecord.setCreationTimestamp(asbiep.getCreationTimestamp());
        asbiepRecord.setLastUpdateTimestamp(asbiep.getLastUpdateTimestamp());
        asbiepRecord.setOwnerTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));

        BigInteger asbiepId = dslContext.insertInto(ASBIEP)
                .set(asbiepRecord)
                .returning(ASBIEP.ASBIEP_ID)
                .fetchOne().getAsbiepId().toBigInteger();
        asbiep.setAsbiepId(asbiepId);

        return asbiep;
    }

    private void updateTopLevelAsbiepWithAsbiepID(TopLevelASBIEPObject topLevelAsbiep, ASBIEPObject asbiep) {
        if (asbiep != null) {
            dslContext.update(TOP_LEVEL_ASBIEP)
                    .set(TOP_LEVEL_ASBIEP.ASBIEP_ID, ULong.valueOf(asbiep.getAsbiepId()))
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))
                    .execute();
        } else {
            ULong asbiepId = null;
            dslContext.update(TOP_LEVEL_ASBIEP)
                    .set(TOP_LEVEL_ASBIEP.ASBIEP_ID, asbiepId)
                    .where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId())))
                    .execute();
        }

    }

    private void addBusinessContextAssignment(TopLevelASBIEPObject topLevelAsbiep,
                                              BusinessContextObject businessContext) {
        BizCtxAssignmentRecord bizCtxAssignment = new BizCtxAssignmentRecord();
        bizCtxAssignment.setTopLevelAsbiepId(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()));
        bizCtxAssignment.setBizCtxId(ULong.valueOf(businessContext.getBusinessContextId()));
        dslContext.insertInto(BIZ_CTX_ASSIGNMENT)
                .set(bizCtxAssignment)
                .execute();
    }

    @Override
    public void deleteTopLevelASBIEPByTopLevelASBIEPId(TopLevelASBIEPObject topLevelAsbiep) {

        //Delete all BBIEs associated to the provided Top-level ASBIEP
        dslContext.deleteFrom(BBIE).where(BBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();

        //Delene all BBIEPs associated to the provided Top-level ASBIEP
        dslContext.deleteFrom(BBIEP).where(BBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();

        //Delete all BBIE_SC associated to the provided Top-level ASBIEP
        dslContext.deleteFrom(BBIE_SC).where(BBIE_SC.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();

        //Delete all ASBIEs associated to the provided Top-level ASBIEP
        updateTopLevelAsbiepWithAsbiepID(topLevelAsbiep, null);
        dslContext.deleteFrom(ASBIE).where(ASBIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();

        //Delete all ASBIEPs associated to the provided Top-level ASBIEP
        dslContext.deleteFrom(ASBIEP).where(ASBIEP.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();

        //Delete all ABIEs associated to the provided Top-level ASBIEP
        dslContext.deleteFrom(ABIE).where(ABIE.OWNER_TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();

        //Delete Biz ctx assignment
        dslContext.deleteFrom(BIZ_CTX_ASSIGNMENT).where(BIZ_CTX_ASSIGNMENT.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();

        //Delete Top-level ASBIEP
        dslContext.deleteFrom(TOP_LEVEL_ASBIEP).where(TOP_LEVEL_ASBIEP.TOP_LEVEL_ASBIEP_ID.eq(ULong.valueOf(topLevelAsbiep.getTopLevelAsbiepId()))).execute();
    }

}
