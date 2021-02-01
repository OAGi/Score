package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.data.*;
import org.oagi.score.gateway.http.helper.Utility;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.util.StringUtils;

import java.math.BigInteger;

class Helper {

    private static final String CODE_LIST_NAME_PREFIX = "cl";

    private static final String AGENCY_ID_LIST_NAME_PREFIX = "il";

    private Helper() {
    }

    static boolean isAnyProperty(ASBIE asbie,
                                 GenerationContext generationContext) {
        ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);
        BigInteger asccpManifestId = asbiep.getBasedAsccpManifestId();
        ASCCP asccp = generationContext.findASCCP(asccpManifestId);
        if (!"AnyProperty".equals(Utility.first(asccp.getDen(), true))) {
            return false;
        }

        ABIE abie = generationContext.queryTargetABIE2(asbiep);
        ACC acc = generationContext.queryBasedACC(abie);
        return OagisComponentType.Embedded.getValue() == acc.getOagisComponentType();
    }

    static CodeList getCodeList(GenerationContext generationContext, BBIE bbie, DT bdt) {
        CodeList codeList = null;

        if (bbie.getCodeListId() != null) {
            codeList = generationContext.findCodeList(bbie.getCodeListId());
        }

        if (codeList == null) {
            BdtPriRestri bdtPriRestri =
                    generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
            if (bdtPriRestri != null && bdtPriRestri.getCodeListId() != null) {
                codeList = generationContext.findCodeList(bdtPriRestri.getCodeListId());
            }
        }

        if (codeList == null) {
            BdtPriRestri bdtPriRestri =
                    generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
            if (bdtPriRestri != null && bdtPriRestri.getCodeListId() != null) {
                codeList = generationContext.findCodeList(bdtPriRestri.getCodeListId());
            }
        }
        return codeList;
    }

    static Xbt getXbt(GenerationContext generationContext,
                      BdtPriRestri bdtPriRestri) {
        CdtAwdPriXpsTypeMap cdtAwdPriXpsTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
        Xbt xbt = generationContext.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
        return xbt;
    }

    static String getCodeListTypeName(CodeList codeList) {
        StringBuilder sb = new StringBuilder();

        sb.append(CODE_LIST_NAME_PREFIX);
        sb.append(codeList.getAgencyId()).append('_');
        sb.append(codeList.getVersionId()).append('_');
        String name = codeList.getName();
        if (StringUtils.hasLength(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(codeList.getListId());

        return sb.toString();
    }

    static String getAgencyListTypeName(AgencyIdList agencyIdList, AgencyIdListValue agencyIdListValue) {
        StringBuilder sb = new StringBuilder();

        sb.append(AGENCY_ID_LIST_NAME_PREFIX);
        /*
         * Issue #589
         */
        sb.append(agencyIdListValue.getValue()).append('_');
        sb.append(agencyIdList.getVersionId()).append('_');
        String name = agencyIdList.getName();
        if (StringUtils.hasLength(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(agencyIdList.getListId());

        return sb.toString();
    }
}
