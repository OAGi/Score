package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample data used to preview a filename expression: one value per scalar placeholder
 * (keyed by placeholder name) plus the ordered Business Context names.
 */
public record FilenameSampleValues(Map<String, String> values, List<String> businessContextNames) {

    public static FilenameSampleValues defaults() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_ID, "1001");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PROPERTY_TERM, "Item Master");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_DISPLAY_NAME, "Engineering Item Master");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_VERSION, "1.0");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_DEN, "Item Master. Item Master");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_STATUS, "Published");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_REMARK, "Sample remark");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_NAME, "Sample Package");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_VERSION_ID, "v1");
        values.put(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_VERSION_NAME, "1.0");
        return new FilenameSampleValues(values,
                List.of("Default Context", "Secondary Context", "Third Context"));
    }

    /** Overlays user-supplied values on the defaults; absent keys keep their default. */
    public static FilenameSampleValues merge(Map<String, String> overrides, List<String> businessContextNames) {
        FilenameSampleValues defaults = defaults();
        Map<String, String> merged = new LinkedHashMap<>(defaults.values());
        if (overrides != null) {
            overrides.forEach((key, value) -> {
                if (merged.containsKey(key)) {
                    merged.put(key, value == null ? "" : value);
                }
            });
        }
        return new FilenameSampleValues(merged,
                businessContextNames != null ? List.copyOf(businessContextNames) : defaults.businessContextNames());
    }
}
