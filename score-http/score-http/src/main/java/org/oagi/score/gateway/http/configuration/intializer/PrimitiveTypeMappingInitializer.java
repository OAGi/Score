package org.oagi.score.gateway.http.configuration.intializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.DSLContext;
import org.jooq.types.ULong;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.and;
import static org.oagi.score.repo.api.impl.jooq.entity.Tables.*;

@Component
public class PrimitiveTypeMappingInitializer implements InitializingBean {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private DSLContext dslContext;


    @Override
    public void afterPropertiesSet() throws Exception {
        // Issue #1469
        // Add 'integer' primitive to all 'Identifier' DTs and DT_SCs
        issue_1469();
    }

    private void issue_1469() {
        /*
         * For DTs
         */
        DtRecord identifierCdt = dslContext.selectFrom(DT)
                .where(and(
                        DT.DATA_TYPE_TERM.eq("Identifier"),
                        DT.BASED_DT_ID.isNull()
                ))
                .fetchOne();

        CdtPriRecord integerCdtPri = dslContext.selectFrom(CDT_PRI)
                .where(CDT_PRI.NAME.eq("Integer"))
                .fetchOne();

        CdtAwdPriRecord identifierIntegerCdtAwdPri = dslContext.selectFrom(CDT_AWD_PRI)
                .where(and(
                        CDT_AWD_PRI.CDT_ID.eq(identifierCdt.getDtId()),
                        CDT_AWD_PRI.CDT_PRI_ID.eq(integerCdtPri.getCdtPriId())
                ))
                .fetchOptionalInto(CdtAwdPriRecord.class).orElse(null);
        if (identifierIntegerCdtAwdPri != null) { // if exists, skip it.
            return;
        }

        logger.info("[Issue #1469] 'Identifier' DT and DT_SC allows 'integer' primitives.");

        identifierIntegerCdtAwdPri = new CdtAwdPriRecord();
        identifierIntegerCdtAwdPri.setCdtId(identifierCdt.getDtId());
        identifierIntegerCdtAwdPri.setCdtPriId(integerCdtPri.getCdtPriId());
        identifierIntegerCdtAwdPri.setIsDefault((byte) 0);
        identifierIntegerCdtAwdPri.setCdtAwdPriId(
                dslContext.insertInto(CDT_AWD_PRI)
                        .set(identifierIntegerCdtAwdPri)
                        .returning(CDT_AWD_PRI.CDT_AWD_PRI_ID)
                        .fetchOne().getCdtAwdPriId());

        XbtRecord integerXbt = dslContext.selectFrom(XBT)
                .where(XBT.NAME.eq("integer"))
                .fetchOne();

        CdtAwdPriXpsTypeMapRecord identifierIntegerCdtAwdPriXpsTypeMap = dslContext.selectFrom(CDT_AWD_PRI_XPS_TYPE_MAP)
                .where(and(
                        CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_ID.eq(identifierIntegerCdtAwdPri.getCdtAwdPriId()),
                        CDT_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(integerXbt.getXbtId())))
                .fetchOptionalInto(CdtAwdPriXpsTypeMapRecord.class).orElse(null);
        if (identifierIntegerCdtAwdPriXpsTypeMap == null) {
            identifierIntegerCdtAwdPriXpsTypeMap = new CdtAwdPriXpsTypeMapRecord();
            identifierIntegerCdtAwdPriXpsTypeMap.setCdtAwdPriId(identifierIntegerCdtAwdPri.getCdtAwdPriId());
            identifierIntegerCdtAwdPriXpsTypeMap.setXbtId(integerXbt.getXbtId());
            identifierIntegerCdtAwdPriXpsTypeMap.setIsDefault((byte) 0);
            identifierIntegerCdtAwdPriXpsTypeMap.setCdtAwdPriXpsTypeMapId(
                    dslContext.insertInto(CDT_AWD_PRI_XPS_TYPE_MAP)
                            .set(identifierIntegerCdtAwdPriXpsTypeMap)
                            .returning(CDT_AWD_PRI_XPS_TYPE_MAP.CDT_AWD_PRI_XPS_TYPE_MAP_ID)
                            .fetchOne().getCdtAwdPriXpsTypeMapId()
            );
        }

        List<ULong> bdtManifestIdList = dslContext.select(DT_MANIFEST.DT_MANIFEST_ID)
                .from(DT_MANIFEST)
                .join(DT).on(DT_MANIFEST.DT_ID.eq(DT.DT_ID))
                .where(and(
                        DT.DATA_TYPE_TERM.eq("Identifier"),
                        DT.BASED_DT_ID.isNotNull()))
                .fetchInto(ULong.class);

        List<ULong> identifierBdtPriRestri = dslContext.select(BDT_PRI_RESTRI.BDT_MANIFEST_ID)
                .from(BDT_PRI_RESTRI)
                .where(and(
                        BDT_PRI_RESTRI.BDT_MANIFEST_ID.in(bdtManifestIdList),
                        BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID.eq(identifierIntegerCdtAwdPriXpsTypeMap.getCdtAwdPriXpsTypeMapId())))
                .fetchInto(ULong.class);

        // bdtManifestIdList - identifierBdtPriRestri
        for (ULong bdtManifestId : bdtManifestIdList.stream().filter(e -> !identifierBdtPriRestri.contains(e)).collect(Collectors.toList())) {
            dslContext.insertInto(BDT_PRI_RESTRI)
                    .set(BDT_PRI_RESTRI.BDT_MANIFEST_ID, bdtManifestId)
                    .set(BDT_PRI_RESTRI.CDT_AWD_PRI_XPS_TYPE_MAP_ID, identifierIntegerCdtAwdPriXpsTypeMap.getCdtAwdPriXpsTypeMapId())
                    .set(BDT_PRI_RESTRI.IS_DEFAULT, (byte) 0)
                    .execute();
        }

        /*
         * For DT_SCs
         */
        for (DtScRecord identifierDtSc : dslContext.selectFrom(DT_SC)
                .where(DT_SC.REPRESENTATION_TERM.eq("Identifier"))
                .fetch()) {
            CdtScAwdPriRecord identifierIntegerCdtScAwdPri = dslContext.selectFrom(CDT_SC_AWD_PRI)
                    .where(and(
                            CDT_SC_AWD_PRI.CDT_SC_ID.eq(identifierDtSc.getDtScId()),
                            CDT_SC_AWD_PRI.CDT_PRI_ID.eq(integerCdtPri.getCdtPriId())))
                    .fetchOptionalInto(CdtScAwdPriRecord.class).orElse(null);
            if (identifierIntegerCdtScAwdPri == null) {
                identifierIntegerCdtScAwdPri = new CdtScAwdPriRecord();
                identifierIntegerCdtScAwdPri.setCdtScId(identifierDtSc.getDtScId());
                identifierIntegerCdtScAwdPri.setCdtPriId(integerCdtPri.getCdtPriId());
                identifierIntegerCdtScAwdPri.setIsDefault((byte) 0);
                identifierIntegerCdtScAwdPri.setCdtScAwdPriId(
                        dslContext.insertInto(CDT_SC_AWD_PRI)
                                .set(identifierIntegerCdtScAwdPri)
                                .returning(CDT_SC_AWD_PRI.CDT_SC_AWD_PRI_ID)
                                .fetchOne().getCdtScAwdPriId());
            }

            CdtScAwdPriXpsTypeMapRecord identifierIntegerCdtScAwdPriXpsTypeMap = dslContext.selectFrom(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                    .where(and(
                            CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_ID.eq(identifierIntegerCdtScAwdPri.getCdtScAwdPriId()),
                            CDT_SC_AWD_PRI_XPS_TYPE_MAP.XBT_ID.eq(integerXbt.getXbtId())))
                    .fetchOptionalInto(CdtScAwdPriXpsTypeMapRecord.class).orElse(null);
            if (identifierIntegerCdtScAwdPriXpsTypeMap == null) {
                identifierIntegerCdtScAwdPriXpsTypeMap = new CdtScAwdPriXpsTypeMapRecord();
                identifierIntegerCdtScAwdPriXpsTypeMap.setCdtScAwdPriId(identifierIntegerCdtScAwdPri.getCdtScAwdPriId());
                identifierIntegerCdtScAwdPriXpsTypeMap.setXbtId(integerXbt.getXbtId());
                identifierIntegerCdtScAwdPriXpsTypeMap.setIsDefault((byte) 0);
                identifierIntegerCdtScAwdPriXpsTypeMap.setCdtScAwdPriXpsTypeMapId(
                        dslContext.insertInto(CDT_SC_AWD_PRI_XPS_TYPE_MAP)
                                .set(identifierIntegerCdtScAwdPriXpsTypeMap)
                                .returning(CDT_SC_AWD_PRI_XPS_TYPE_MAP.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID)
                                .fetchOne().getCdtScAwdPriXpsTypeMapId()
                );
            }

            List<ULong> bdtScManifestIdList = dslContext.select(DT_SC_MANIFEST.DT_SC_MANIFEST_ID)
                    .from(DT_SC_MANIFEST)
                    .join(DT_SC).on(DT_SC_MANIFEST.DT_SC_ID.eq(DT_SC.DT_SC_ID))
                    .where(DT_SC.DT_SC_ID.eq(identifierDtSc.getDtScId()))
                    .fetchInto(ULong.class);

            List<ULong> identifierBdtScPriRestri = dslContext.select(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID)
                    .from(BDT_SC_PRI_RESTRI)
                    .where(and(
                            BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID.in(bdtScManifestIdList),
                            BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID.eq(identifierIntegerCdtScAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId())))
                    .fetchInto(ULong.class);

            // bdtScManifestIdList - identifierBdtScPriRestri
            for (ULong bdtScManifestId : bdtScManifestIdList.stream().filter(e -> !identifierBdtScPriRestri.contains(e)).collect(Collectors.toList())) {
                dslContext.insertInto(BDT_SC_PRI_RESTRI)
                        .set(BDT_SC_PRI_RESTRI.BDT_SC_MANIFEST_ID, bdtScManifestId)
                        .set(BDT_SC_PRI_RESTRI.CDT_SC_AWD_PRI_XPS_TYPE_MAP_ID, identifierIntegerCdtScAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId())
                        .set(BDT_SC_PRI_RESTRI.IS_DEFAULT, (byte) 0)
                        .execute();
            }
        }
    }

}
