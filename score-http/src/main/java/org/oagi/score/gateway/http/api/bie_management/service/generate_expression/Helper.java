package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.util.Utility;
import org.springframework.util.StringUtils;

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

    public static boolean isAnyProperty(AsbieSummaryRecord asbie,
                                        GenerationContext generationContext) {
        AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);
        AsccpManifestId asccpManifestId = asbiep.basedAsccpManifestId();
        AsccpSummaryRecord asccp = generationContext.getAsccp(asccpManifestId);
        if (!"Any Property. Any Structured Content".equals(asccp.den())) {
            return false;
        }

        AbieSummaryRecord abie = generationContext.queryTargetABIE(asbiep);
        AccSummaryRecord acc = generationContext.queryBasedACC(abie);
        return OagisComponentType.Embedded == acc.componentType();
    }

    public static CodeListSummaryRecord getCodeList(GenerationContext generationContext, BbieSummaryRecord bbie, DtSummaryRecord bdt) {
        if (bbie == null) {
            return null;
        }
        return generationContext.getCodeList(bbie.primitiveRestriction().codeListManifestId());
    }

    public static XbtSummaryRecord getXbt(GenerationContext generationContext,
                                          DtAwdPriSummaryRecord dtAwdPri) {
        XbtSummaryRecord xbt = generationContext.getXbt(dtAwdPri.xbtManifestId());
        return xbt;
    }

    public static String getCodeListTypeName(CodeListSummaryRecord codeList, AgencyIdListValueSummaryRecord agencyIdListValue) {
        StringBuilder sb = new StringBuilder();

        sb.append(CODE_LIST_NAME_PREFIX);
        if (agencyIdListValue != null) {
            sb.append(agencyIdListValue.value()).append('_');
        } else {
            sb.append('_');
        }
        sb.append(codeList.versionId()).append('_');
        String name = codeList.name();
        if (StringUtils.hasLength(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(codeList.listId());

        return sb.toString().replaceAll(" ", "_");
    }

    public static String getAgencyListTypeName(AgencyIdListSummaryRecord agencyIdList, AgencyIdListValueSummaryRecord agencyIdListValue) {
        StringBuilder sb = new StringBuilder();

        sb.append(AGENCY_ID_LIST_NAME_PREFIX);
        /*
         * Issue #589
         */
        sb.append(agencyIdListValue.value()).append('_');
        sb.append(agencyIdList.versionId()).append('_');
        String name = agencyIdList.name();
        if (StringUtils.hasLength(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(agencyIdList.listId());

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

    public static boolean hasAnyValuesInFacets(Facet facet) {
        if (facet == null) {
            return false;
        }
        return facet.minLength() != null ||
                facet.maxLength() != null ||
                StringUtils.hasLength(facet.pattern());
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
