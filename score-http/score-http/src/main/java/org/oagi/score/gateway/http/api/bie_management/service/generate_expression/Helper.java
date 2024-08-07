package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.data.*;
import org.oagi.score.gateway.http.helper.Utility;
import org.oagi.score.service.common.data.OagisComponentType;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Helper {

    private static final String CODE_LIST_NAME_PREFIX = "cl";

    private static final String AGENCY_ID_LIST_NAME_PREFIX = "il";

    private Helper() {
    }

    public static boolean isAnyProperty(ASBIE asbie,
                                 GenerationContext generationContext) {
        ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);
        BigInteger asccpManifestId = asbiep.getBasedAsccpManifestId();
        ASCCP asccp = generationContext.findASCCP(asccpManifestId);
        if (!"AnyProperty".equals(Utility.first(asccp.getDen(), true))) {
            return false;
        }

        ABIE abie = generationContext.queryTargetABIE(asbiep);
        ACC acc = generationContext.queryBasedACC(abie);
        return OagisComponentType.Embedded.getValue() == acc.getOagisComponentType();
    }

    public static CodeList getCodeList(GenerationContext generationContext, BBIE bbie, DT bdt) {
        if (bbie == null) {
            return null;
        }
        return generationContext.findCodeList(bbie.getCodeListManifestId());
    }

    public static Xbt getXbt(GenerationContext generationContext,
                      BdtPriRestri bdtPriRestri) {
        CdtAwdPriXpsTypeMap cdtAwdPriXpsTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
        Xbt xbt = generationContext.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
        return xbt;
    }

    public static String getCodeListTypeName(CodeList codeList, AgencyIdListValue agencyIdListValue) {
        StringBuilder sb = new StringBuilder();

        sb.append(CODE_LIST_NAME_PREFIX);
        if (agencyIdListValue != null) {
            sb.append(agencyIdListValue.getValue()).append('_');
        } else {
            sb.append('_');
        }
        sb.append(codeList.getVersionId()).append('_');
        String name = codeList.getName();
        if (StringUtils.hasLength(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(codeList.getListId());

        return sb.toString().replaceAll(" ", "_");
    }

    public static String getAgencyListTypeName(AgencyIdList agencyIdList, AgencyIdListValue agencyIdListValue) {
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

        return sb.toString().replaceAll(" ", "_");
    }

    public static String convertIdentifierToId(String str) {
        if (!StringUtils.hasLength(str)) {
            return str;
        }
        return str.replaceAll("Identifier", "Id")
                .replaceAll("identifier", "id");
    }

    public static String camelCase(String... terms) {
        String term = Arrays.stream(terms).collect(Collectors.joining());
        if (terms.length == 1) {
            term = _camelCase(terms[0]);
        } else if (term.contains(" ")) {
            term = Arrays.stream(terms).map(e -> _camelCase(e)).collect(Collectors.joining());
        }

        if (!StringUtils.hasLength(term)) {
            throw new IllegalArgumentException();
        }

        return Character.toLowerCase(term.charAt(0)) + term.substring(1);
    }

    private static String _camelCase(String term) {
        return Arrays.stream(term.split(" ")).filter(e -> StringUtils.hasLength(e))
                .map(e -> {
                    if (e.length() > 1) {
                        return Character.toUpperCase(e.charAt(0)) + e.substring(1).toLowerCase();
                    } else {
                        return e.toUpperCase();
                    }
                }).collect(Collectors.joining());
    }

    public static boolean hasAnyValuesInFacets(FacetRestrictionsAware facetRestrictionsAware) {
        return facetRestrictionsAware.getFacetMinLength() != null ||
                facetRestrictionsAware.getFacetMaxLength() != null ||
                StringUtils.hasLength(facetRestrictionsAware.getFacetPattern());
    }

    public static String toName(String propertyTerm, String representationTerm,
                                Function<String, String> representationTermMapper,
                                boolean includedAbbr) {
        if (!StringUtils.hasLength(propertyTerm) || !StringUtils.hasLength(representationTerm)) {
            throw new IllegalArgumentException();
        }

        representationTerm = representationTermMapper.apply(representationTerm);

        List<String> s = Stream.concat(
                Stream.of(propertyTerm.split(" ")),
                Stream.of(representationTerm.split(" "))
        ).distinct().collect(Collectors.toList());
        s.set(0, s.get(0).toLowerCase());
        if (s.size() > 1) {
            for (int i = 1, len = s.size(); i < len; ++i) {
                s.set(i, Utility.camelCase(s.get(i), includedAbbr));
            }
        }
        return String.join("", s);
    }

}
