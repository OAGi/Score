package org.oagi.score.gateway.http.api.cc_management.model.dt;

import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Picks the default value domain (a primitive, a code list, or an agency ID list) that a BBIE or a BBIE
 * supplementary component starts with, out of the allowed primitives of its Data Type.
 * <p>
 * This is the only place that holds the rule. Everything that needs the default of a Data Type asks here.
 * <p>
 * Issue #808: a Data Type whose term is {@code Date Time}, {@code Date} or {@code Time} starts with the
 * primitive of that name ({@code date time}, {@code date} or {@code time}), not with the default of the
 * Core Component, which is a {@code token} for many of them. Any other Data Type starts with its default.
 * A Data Type that does not allow the primitive of that name breaks the rule.
 */
public final class DefaultAwdPriRule {

    private DefaultAwdPriRule() {
    }

    /**
     * @param dataTypeTerm the data type term of the Data Type
     * @throws IllegalStateException if the Data Type has no allowed primitive to start with
     */
    public static DtAwdPriSummaryRecord select(String dataTypeTerm, List<DtAwdPriSummaryRecord> awdPriList) {
        return select("Data Type", dataTypeTerm, awdPriList,
                DtAwdPriSummaryRecord::xbtName, DtAwdPriSummaryRecord::isDefault);
    }

    /**
     * @param representationTerm the representation term of the supplementary component
     * @throws IllegalStateException if the supplementary component has no allowed primitive to start with
     */
    public static DtScAwdPriSummaryRecord selectForSc(String representationTerm, List<DtScAwdPriSummaryRecord> awdPriList) {
        return select("Supplementary component", representationTerm, awdPriList,
                DtScAwdPriSummaryRecord::xbtName, DtScAwdPriSummaryRecord::isDefault);
    }

    private static <T> T select(String subject, String term, List<T> awdPriList,
                                Function<T, String> xbtName, Predicate<T> isDefault) {
        String primitiveName = primitiveNameOf(term);
        if (primitiveName != null) {
            return awdPriList.stream()
                    .filter(e -> primitiveName.equalsIgnoreCase(xbtName.apply(e)))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            subject + " of the term '" + term + "' does not allow the primitive '" + primitiveName + "'."));
        }
        return awdPriList.stream()
                .filter(isDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        subject + " of the term '" + term + "' has no default primitive."));
    }

    private static String primitiveNameOf(String term) {
        if ("Date Time".equals(term)) {
            return "date time";
        } else if ("Date".equals(term)) {
            return "date";
        } else if ("Time".equals(term)) {
            return "time";
        }
        return null;
    }
}
