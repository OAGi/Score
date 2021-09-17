package org.oagi.score.repo.api.impl.jooq.corecomponent;

import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.agency.model.AgencyIdList;
import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.corecomponent.CodeListReadRepository;
import org.oagi.score.repo.api.corecomponent.ValueDomainReadRepository;
import org.oagi.score.repo.api.corecomponent.model.BdtPriRestri;
import org.oagi.score.repo.api.corecomponent.model.BdtScPriRestri;
import org.oagi.score.repo.api.corecomponent.model.CodeList;
import org.oagi.score.repo.api.impl.jooq.JooqScoreRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

public class JooqValueDomainReadRepository
        extends JooqScoreRepository
        implements ValueDomainReadRepository {

    public JooqValueDomainReadRepository(DSLContext dslContext) {
        super(dslContext);
    }

    @Override
    public List<CodeList> getCodeListList(BigInteger releaseId) throws ScoreDataAccessException {
        return dslContext()
                .select(CODE_LIST.CODE_LIST_ID,
                        CODE_LIST.GUID,
                        CODE_LIST.NAME,
                        CODE_LIST.LIST_ID,
                        CODE_LIST.VERSION_ID,
                        CODE_LIST_MANIFEST.as("based_clm").CODE_LIST_ID.as("based_code_list_id"),
                        CODE_LIST.AGENCY_ID,
                        AGENCY_ID_LIST_VALUE.NAME.as("agencyName"),
                        CODE_LIST.PREV_CODE_LIST_ID,
                        CODE_LIST.NEXT_CODE_LIST_ID)
                .from(CODE_LIST)
                .join(CODE_LIST_MANIFEST).on(CODE_LIST.CODE_LIST_ID.eq(CODE_LIST_MANIFEST.CODE_LIST_ID))
                .leftOuterJoin(CODE_LIST_MANIFEST.as("based_clm")).on(CODE_LIST_MANIFEST.BASED_CODE_LIST_MANIFEST_ID.eq(CODE_LIST_MANIFEST.as("based_clm").CODE_LIST_MANIFEST_ID))
                .join(AGENCY_ID_LIST_VALUE).on(CODE_LIST.AGENCY_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(
                        and(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID)),
                        AGENCY_ID_LIST_VALUE_MANIFEST.RELEASE_ID.eq(CODE_LIST_MANIFEST.RELEASE_ID))
                .where(CODE_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(CodeList.class);
    }

    @Override
    public Map<BigInteger, BdtPriRestri> getBdtPriRestriMap(BigInteger releaseId) throws ScoreDataAccessException {
        List<BdtPriRestri> bdtPriRestriRecords = dslContext().select(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID,
                BDT_PRI_RESTRI.BDT_ID,
                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_PRI_RESTRI.CODE_LIST_ID,
                BDT_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_PRI_RESTRI.IS_DEFAULT,
                XBT.NAME.as("XBT_NAME"), XBT.XBT_ID)
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST).on(and(BDT_PRI_RESTRI.BDT_ID.eq(DT_MANIFEST.DT_ID), DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .leftJoin(CDT_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .fetchInto(BdtPriRestri.class);

        return bdtPriRestriRecords.stream()
                .collect(Collectors.toMap(BdtPriRestri::getBdtPriRestriId, Function.identity()));
    }

    @Override
    public Map<BigInteger, BdtScPriRestri> getBdtScPriRestriMap(BigInteger releaseId) throws ScoreDataAccessException {
        List<BdtScPriRestri> bdtScPriRestriRecords = dslContext().select(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID,
                BDT_SC_PRI_RESTRI.BDT_SC_ID,
                BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_SC_PRI_RESTRI.CODE_LIST_ID,
                BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_SC_PRI_RESTRI.IS_DEFAULT,
                XBT.XBT_ID, XBT.NAME.as("XBT_NAME"))
                .from(BDT_SC_PRI_RESTRI)
                .join(DT_SC_MANIFEST).on(and(BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID), DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .leftJoin(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .fetchInto(BdtScPriRestri.class);

        return bdtScPriRestriRecords.stream()
                .collect(Collectors.toMap(BdtScPriRestri::getBdtScPriRestriId, Function.identity()));
    }

    @Override
    public Map<BigInteger, List<BdtPriRestri>> getBdtPriRestriBdtIdMap(BigInteger releaseId) throws ScoreDataAccessException {
        List<BdtPriRestri> bdtPriRestriRecords = dslContext().select(BDT_PRI_RESTRI.BDT_PRI_RESTRI_ID,
                BDT_PRI_RESTRI.BDT_ID,
                BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_PRI_RESTRI.CODE_LIST_ID,
                BDT_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_PRI_RESTRI.IS_DEFAULT,
                XBT.NAME.as("XBT_NAME"), XBT.XBT_ID)
                .from(BDT_PRI_RESTRI)
                .join(DT_MANIFEST).on(and(BDT_PRI_RESTRI.BDT_ID.eq(DT_MANIFEST.DT_ID), DT_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .leftJoin(CDT_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .fetchInto(BdtPriRestri.class);

        return bdtPriRestriRecords.stream()
                .collect(groupingBy(BdtPriRestri::getBdtId));
    }

    @Override
    public Map<BigInteger, List<BdtScPriRestri>> getBdtScPriRestriBdtScIdMap(BigInteger releaseId) throws ScoreDataAccessException {
        List<BdtScPriRestri> bdtScPriRestriRecords = dslContext().select(BDT_SC_PRI_RESTRI.BDT_SC_PRI_RESTRI_ID,
                BDT_SC_PRI_RESTRI.BDT_SC_ID,
                BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID,
                BDT_SC_PRI_RESTRI.CODE_LIST_ID,
                BDT_SC_PRI_RESTRI.AGENCY_ID_LIST_ID,
                BDT_SC_PRI_RESTRI.IS_DEFAULT,
                XBT.XBT_ID, XBT.NAME.as("XBT_NAME"))
                .from(BDT_SC_PRI_RESTRI)
                .join(DT_SC_MANIFEST).on(and(BDT_SC_PRI_RESTRI.BDT_SC_ID.eq(DT_SC_MANIFEST.DT_SC_ID), DT_SC_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId))))
                .leftJoin(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                .on(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID))
                .leftJoin(XBT).on(CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(XBT.XBT_ID))
                .fetchInto(BdtScPriRestri.class);

        return bdtScPriRestriRecords.stream()
                .collect(groupingBy(BdtScPriRestri::getBdtScId));
    }

    @Override
    public List<AgencyIdList> getAgencyIdListList(BigInteger releaseId) throws ScoreDataAccessException {
        return dslContext()
                .select(AGENCY_ID_LIST.AGENCY_ID_LIST_ID,
                        AGENCY_ID_LIST.GUID,
                        AGENCY_ID_LIST.NAME,
                        AGENCY_ID_LIST.LIST_ID,
                        AGENCY_ID_LIST.VERSION_ID,
                        AGENCY_ID_LIST_MANIFEST.as("based_ail").AGENCY_ID_LIST_ID.as("based_agency_id_list_id"),
                        AGENCY_ID_LIST_VALUE.NAME.as("agencyIdListValueName"),
                        AGENCY_ID_LIST.PREV_AGENCY_ID_LIST_ID,
                        AGENCY_ID_LIST.NEXT_AGENCY_ID_LIST_ID)
                .from(AGENCY_ID_LIST)
                .join(AGENCY_ID_LIST_MANIFEST).on(AGENCY_ID_LIST.AGENCY_ID_LIST_ID.eq(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_ID))
                .leftOuterJoin(AGENCY_ID_LIST_MANIFEST.as("based_ail")).on(AGENCY_ID_LIST_MANIFEST.BASED_AGENCY_ID_LIST_MANIFEST_ID.eq(AGENCY_ID_LIST_MANIFEST.as("based_ail").AGENCY_ID_LIST_MANIFEST_ID))
                .join(AGENCY_ID_LIST_VALUE_MANIFEST).on(AGENCY_ID_LIST_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID.eq(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_MANIFEST_ID))
                .join(AGENCY_ID_LIST_VALUE).on(AGENCY_ID_LIST_VALUE_MANIFEST.AGENCY_ID_LIST_VALUE_ID.eq(AGENCY_ID_LIST_VALUE.AGENCY_ID_LIST_VALUE_ID))
                .where(AGENCY_ID_LIST_MANIFEST.RELEASE_ID.eq(ULong.valueOf(releaseId)))
                .fetchInto(AgencyIdList.class);
    }
}
