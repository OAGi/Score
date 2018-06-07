package org.oagi.srt.service.expression;

import org.oagi.srt.common.util.Utility;
import org.oagi.srt.repository.entity.*;
import org.springframework.util.StringUtils;

class Helper {

    private static final String CODE_LIST_NAME_PREFIX = "cl";

    private static final String AGENCY_ID_LIST_NAME_PREFIX = "il";

    private Helper() {}

    static boolean isAnyProperty(AssociationBusinessInformationEntity asbie,
                                  GenerationContext generationContext) {
        AssociationBusinessInformationEntityProperty asbiep = generationContext.queryAssocToASBIEP(asbie);
        AssociationCoreComponentProperty asccp = generationContext.findASCCP(asbiep.getBasedAsccpId());
        if (!"AnyProperty".equals(Utility.first(asccp.getDen(), true))) {
            return false;
        }

        AggregateBusinessInformationEntity abie = generationContext.queryTargetABIE2(asbiep);
        AggregateCoreComponent acc = generationContext.queryBasedACC(abie);
        return OagisComponentType.Embedded == acc.getOagisComponentType();
    }

    static CodeList getCodeList(GenerationContext generationContext, BasicBusinessInformationEntity bbie, DataType bdt) {
        CodeList codeList = null;

        if (bbie.getCodeListId() != 0) {
            codeList = generationContext.findCodeList(bbie.getCodeListId());
        }

        if (codeList == null) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
            if (bdtPriRestri != null && bdtPriRestri.getCodeListId() != 0) {
                codeList = generationContext.findCodeList(bdtPriRestri.getCodeListId());
            }
        }

        if (codeList == null) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
            if (bdtPriRestri != null && bdtPriRestri.getCodeListId() != 0) {
                codeList = generationContext.findCodeList(bdtPriRestri.getCodeListId());
            }
        }
        return codeList;
    }

    static XSDBuiltInType getXbt(GenerationContext generationContext,
                                 BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtAwdPriXpsTypeMap.getXbtId());
        return xbt;
    }

    static String getCodeListTypeName(CodeList codeList) {
        StringBuilder sb = new StringBuilder();

        sb.append(CODE_LIST_NAME_PREFIX);
        sb.append(codeList.getAgencyId()).append('_');
        sb.append(codeList.getVersionId()).append('_');
        String name = codeList.getName();
        if (!StringUtils.isEmpty(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(codeList.getListId());

        return sb.toString();
    }

    static String getAgencyListTypeName(AgencyIdList agencyIdList, AgencyIdListValue agencyIdListValue) {
        StringBuilder sb = new StringBuilder();

        sb.append(AGENCY_ID_LIST_NAME_PREFIX);
        sb.append(agencyIdListValue.getValue());
        sb.append(agencyIdList.getVersionId()).append('_');
        String name = agencyIdList.getName();
        if (!StringUtils.isEmpty(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(agencyIdList.getListId());

        return sb.toString();
    }


}
