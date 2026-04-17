package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.common.util.Utility;
import org.oagi.score.gateway.http.common.util.string.LiteralCaseStringConverter;
import org.oagi.score.gateway.http.common.util.string.PascalCaseStringConverter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.camelCase;
import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.convertIdentifierToId;

public class JsonSchemaNamingStrategy implements SchemaNamingStrategy {

    @Override
    public String accName(AccSummaryRecord acc) {
        return typeName(acc.objectClassTerm());
    }

    @Override
    public String asccpName(AsccpSummaryRecord asccp) {
        return convertIdentifierToId(camelCase(asccp.propertyTerm()));
    }

    @Override
    public String asccpTypeName(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc) {
        return typeName(roleOfAcc.objectClassTerm()) + "Type";
    }

    @Override
    public String bccpName(BccpSummaryRecord bccp) {
        return convertIdentifierToId(camelCase(bccp.propertyTerm()));
    }

    @Override
    public String bdtScName(DtScSummaryRecord dtSc, DtSummaryRecord ownerDt, CcDocument ccDocument) {
        return convertIdentifierToId(new XmlSchemaNamingStrategy().bdtScName(dtSc, ownerDt, ccDocument));
    }

    @Override
    public String dtName(DtSummaryRecord dt) {
        return dtTypeName(dt);
    }

    @Override
    public String agencyIdListName(AgencyIdListSummaryRecord agencyIdList) {
        return literalCase(agencyIdList.name());
    }

    @Override
    public String codeListName(CodeListSummaryRecord codeList) {
        return literalCase(codeList.name());
    }

    private String literalCase(String value) {
        return convertIdentifierToId(new LiteralCaseStringConverter().convert(value));
    }

    private String typeName(String value) {
        List<String> words = Arrays.stream(value.trim().split("\\s+"))
                .filter(StringUtils::hasLength)
                .toList();
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            String normalizedWord = normalizeTypeWord(word);
            sb.append(Character.toUpperCase(normalizedWord.charAt(0)));
            if (normalizedWord.length() > 1) {
                sb.append(normalizedWord.substring(1));
            }
        }
        return sb.toString();
    }

    private String dtTypeName(DtSummaryRecord dt) {
        StringBuilder baseName = new StringBuilder();
        String den = dt.den();
        String body = den.endsWith(". Type") ? den.substring(0, den.length() - ". Type".length()) : den;
        String[] parts = body.split("_ ");
        if (parts.length > 1) {
            baseName.append(typeName(String.join(" ", Arrays.copyOf(parts, parts.length - 1))));
        } else {
            baseName.append(typeName(body));
        }

        String representationName = (parts.length > 1)
                ? typeName(parts[parts.length - 1])
                : typeName(dt.representationTerm());
        if (StringUtils.hasLength(representationName) && !baseName.toString().endsWith(representationName)) {
            baseName.append(representationName);
        }

        return baseName + "Type" + ((dt.sixDigitId() != null) ? "_" + dt.sixDigitId() : "");
    }

    private String normalizeTypeWord(String word) {
        String normalizedWord = word;
        if (word.length() > 1 && word.chars().anyMatch(Character::isLetter) && word.equals(word.toUpperCase())) {
            normalizedWord = Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
        }
        return convertIdentifierToId(normalizedWord);
    }
}
