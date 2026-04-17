package org.oagi.score.gateway.http.api.export.model;

import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.common.util.Utility;
import org.oagi.score.gateway.http.common.util.string.LiteralCaseStringConverter;
import org.oagi.score.gateway.http.common.util.string.PascalCaseStringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.oagi.score.gateway.http.common.ScoreConstants.OAGIS_VERSION;

public class XmlSchemaNamingStrategy implements SchemaNamingStrategy {

    @Override
    public String accName(AccSummaryRecord acc) {
        return Utility.toCamelCase(acc.objectClassTerm());
    }

    @Override
    public String asccpName(AsccpSummaryRecord asccp) {
        return Utility.toCamelCase(asccp.propertyTerm());
    }

    @Override
    public String asccpTypeName(AsccpSummaryRecord asccp, AccSummaryRecord roleOfAcc) {
        return Utility.toCamelCase(asccp.den().substring((asccp.propertyTerm() + ". ").length())) + "Type";
    }

    @Override
    public String bccpName(BccpSummaryRecord bccp) {
        return bccp.propertyTerm().replaceAll(" ", "").replace("Identifier", "ID");
    }

    @Override
    public String bdtScName(DtScSummaryRecord dtSc, DtSummaryRecord ownerDt, CcDocument ccDocument) {
        List<String> objectWords = normalizePhrase(dtSc.objectClassTerm());
        List<String> propertyWords = normalizePhrase(dtSc.propertyTerm());
        String representationTerm = dtSc.representationTerm();

        objectWords = applyRuleR125AndR142(objectWords, ownerDt);
        objectWords = applyRuleR126AndR143(objectWords);
        objectWords = applyGenericWrapperCompatibility(objectWords, ownerDt, ccDocument);

        propertyWords = applyRepresentationDeduplication(propertyWords, representationTerm);
        if ("Text".equals(representationTerm)) {
            propertyWords = applyRuleR127AndR144(propertyWords, representationTerm);
        }

        propertyWords = applyApprovedAbbreviations(propertyWords);
        objectWords = applyApprovedAbbreviations(objectWords);

        List<String> semanticWords = new ArrayList<>();
        semanticWords.addAll(objectWords);
        semanticWords.addAll(propertyWords);

        if (semanticWords.isEmpty()) {
            semanticWords = new ArrayList<>(applyApprovedAbbreviations(normalizePhrase(dtSc.propertyTerm())));
            if (semanticWords.isEmpty()) {
                semanticWords = new ArrayList<>(applyApprovedAbbreviations(normalizePhrase(dtSc.objectClassTerm())));
            }
        }

        String representationSuffix = representationSuffix(representationTerm, semanticWords);
        if (OAGIS_VERSION < 10.3D && "9bb9add40b5b415c8489b08bd4484907".equals(dtSc.getId().value())) {
            representationSuffix = "";
        }

        return lowerCamel(semanticWords) + representationSuffix;
    }

    @Override
    public String dtName(DtSummaryRecord dt) {
        String dtName = pascalCase(dt.den(), "(\\. |_ |\\s)");
        if (dtName.endsWith("ContentCodeType")) {
            dtName = dtName.substring(0, dtName.length() - "CodeType".length()) + "Type";
        } else if (dtName.endsWith("ContentIDType")) {
            dtName = dtName.substring(0, dtName.length() - "IDType".length()) + "Type";
        }
        return dtName + ((dt.sixDigitId() != null) ? "_" + dt.sixDigitId() : "");
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
        return connectSpecLiteralCase(value);
    }

    private String pascalCase(String value, String separatorRegex) {
        return connectSpecPascalCase(value, separatorRegex);
    }

    private String connectSpecLiteralCase(String value) {
        return new LiteralCaseStringConverter().convert(value.replaceAll("Identifier", "ID"));
    }

    private String connectSpecPascalCase(String value, String separatorRegex) {
        return new PascalCaseStringConverter(separatorRegex, true).convert(value.replaceAll("Identifier", "ID"));
    }

    private List<String> normalizePhrase(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.trim().split("\\s+"))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> applyRepresentationDeduplication(List<String> propertyWords, String representationTerm) {
        if (propertyWords.isEmpty()) {
            return new ArrayList<>(propertyWords);
        }
        List<String> representationWords = normalizePhrase(representationTerm);
        if (representationWords.isEmpty() || representationWords.size() > propertyWords.size()) {
            return new ArrayList<>(propertyWords);
        }

        int offset = propertyWords.size() - representationWords.size();
        for (int i = 0; i < representationWords.size(); i++) {
            if (!isEquivalent(propertyWords.get(offset + i), representationWords.get(i))) {
                return new ArrayList<>(propertyWords);
            }
        }
        return new ArrayList<>(propertyWords.subList(0, offset));
    }

    private List<String> applyRuleR125AndR142(List<String> objectWords, DtSummaryRecord ownerDt) {
        if (ownerDt == null) {
            return new ArrayList<>(objectWords);
        }
        List<String> parentRepresentationWords = normalizePhrase(
                (ownerDt.representationTerm() != null && !ownerDt.representationTerm().isBlank())
                        ? ownerDt.representationTerm()
                        : ownerDt.dataTypeTerm());
        if (parentRepresentationWords.isEmpty()) {
            return new ArrayList<>(objectWords);
        }
        return trimPrefix(objectWords, parentRepresentationWords);
    }

    private List<String> applyRuleR126AndR143(List<String> objectWords) {
        if (objectWords.isEmpty()) {
            return new ArrayList<>(objectWords);
        }
        List<String> filtered = objectWords.stream()
                .filter(word -> !isEquivalent(word, "identification"))
                .collect(Collectors.toCollection(ArrayList::new));
        return filtered.isEmpty() ? new ArrayList<>(objectWords) : filtered;
    }

    private List<String> applyRuleR127AndR144(List<String> propertyWords, String representationTerm) {
        return applyRepresentationDeduplication(propertyWords, representationTerm);
    }

    private List<String> applyGenericWrapperCompatibility(List<String> objectWords, DtSummaryRecord ownerDt, CcDocument ccDocument) {
        if (!isGenericWrapperTextType(ownerDt, ccDocument) || objectWords.isEmpty()) {
            return new ArrayList<>(objectWords);
        }

        List<List<String>> prefixes = List.of(
                List.of("Binary", "Object"),
                List.of("Amount"),
                List.of("Measure"),
                List.of("Quantity"),
                List.of("Code"),
                List.of("Text")
        );

        for (List<String> prefix : prefixes) {
            List<String> trimmed = trimPrefix(objectWords, prefix);
            if (trimmed.size() != objectWords.size()) {
                return trimmed;
            }
        }
        return new ArrayList<>(objectWords);
    }

    private boolean isGenericWrapperTextType(DtSummaryRecord ownerDt, CcDocument ccDocument) {
        if (ownerDt == null || ccDocument == null || !"Text".equals(ownerDt.dataTypeTerm())) {
            return false;
        }
        DtSummaryRecord current = ownerDt;
        while (current != null) {
            if ("Text".equals(current.dataTypeTerm()) && isEquivalent(current.qualifier(), "Any Generic Value")) {
                return true;
            }
            if (current.basedDtManifestId() == null) {
                return false;
            }
            current = ccDocument.getDt(current.basedDtManifestId());
        }
        return false;
    }

    private List<String> trimPrefix(List<String> words, List<String> prefix) {
        if (prefix.isEmpty() || prefix.size() > words.size()) {
            return new ArrayList<>(words);
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (!isEquivalent(words.get(i), prefix.get(i))) {
                return new ArrayList<>(words);
            }
        }
        return new ArrayList<>(words.subList(prefix.size(), words.size()));
    }

    private List<String> applyApprovedAbbreviations(List<String> words) {
        if (words.size() == 3 &&
                isEquivalent(words.get(0), "Uniform") &&
                isEquivalent(words.get(1), "Resource") &&
                isEquivalent(words.get(2), "Identifier")) {
            return List.of("URI");
        }
        if (words.size() == 2 &&
                isEquivalent(words.get(0), "Uniform") &&
                isEquivalent(words.get(1), "Resource")) {
            return List.of("URI");
        }
        return words.stream()
                .map(this::applyApprovedAbbreviation)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String applyApprovedAbbreviation(String word) {
        if (isEquivalent(word, "Identifier")) {
            return "ID";
        }
        if (isEquivalent(word, "MIME")) {
            return "MIME";
        }
        if (isEquivalent(word, "URI")) {
            return "URI";
        }
        return word;
    }

    private boolean isEquivalent(String term, String expected) {
        return canonical(term).equals(canonical(expected));
    }

    private String canonical(String value) {
        String normalized = value == null ? "" : value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "identifier", "identification", "id" -> "id";
            case "uniformresourceidentifier", "uniformresource", "uri" -> "uri";
            case "mime" -> "mime";
            default -> normalized;
        };
    }

    private String representationSuffix(String representationTerm, List<String> semanticWords) {
        if ("Text".equals(representationTerm)) {
            return "";
        }
        if (!semanticWords.isEmpty()) {
            String lastWord = semanticWords.get(semanticWords.size() - 1);
            if (isEquivalent(lastWord, representationTerm)) {
                return "";
            }
            if ("Identifier".equals(representationTerm) && isEquivalent(lastWord, "URI")) {
                return "";
            }
        }
        if ("Identifier".equals(representationTerm)) {
            return "ID";
        }
        if ("Code".equals(representationTerm)) {
            return "Code";
        }
        return pascalCase(representationTerm, "\\s");
    }

    private String lowerCamel(List<String> words) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if ("URI".equals(word)) {
                sb.append(i == 0 ? "uri" : "URI");
                continue;
            }
            if ("ID".equals(word)) {
                sb.append(i == 0 ? "id" : "ID");
                continue;
            }
            if ("MIME".equals(word)) {
                sb.append(i == 0 ? "mime" : "Mime");
                continue;
            }

            String lower = word.toLowerCase(Locale.ROOT);
            if (i == 0) {
                sb.append(lower);
            } else {
                sb.append(Character.toUpperCase(lower.charAt(0)));
                if (lower.length() > 1) {
                    sb.append(lower.substring(1));
                }
            }
        }
        return sb.toString();
    }
}
