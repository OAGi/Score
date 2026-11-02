package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.ExpressionBasedFilenameStrategy.*;

/**
 * The placeholders and flags a filename expression may use, with the description of each. The validator accepts
 * exactly these and the suggester offers exactly these, so the grammar is kept in one place.
 */
final class FilenameExpressionCatalog {

    private FilenameExpressionCatalog() {
    }

    static final Map<String, String> COMMON_PLACEHOLDERS = ordered(
            PLACEHOLDER_BIE_PROPERTY_TERM, "the property term of the BIE",
            PLACEHOLDER_BIE_DISPLAY_NAME, "the display name of the BIE",
            PLACEHOLDER_BIE_VERSION, "the version of the BIE, e.g. 1.0",
            PLACEHOLDER_BIE_DEN, "the dictionary entry name of the BIE",
            PLACEHOLDER_BIE_STATUS, "the state of the BIE",
            PLACEHOLDER_BIE_REMARK, "the remark of the BIE",
            PLACEHOLDER_BIE_ID, "the id of the BIE",
            PLACEHOLDER_BUSINESS_CONTEXT_NAMES, "the names of all business contexts, joined by the separator");

    static final Map<String, String> PACKAGE_PLACEHOLDERS = ordered(
            PLACEHOLDER_BIE_PACKAGE_NAME, "the name of the BIE package",
            PLACEHOLDER_BIE_PACKAGE_VERSION_ID, "the version id of the BIE package",
            PLACEHOLDER_BIE_PACKAGE_VERSION_NAME, "the version name of the BIE package");

    static final Map<String, String> DUPLICATE_PLACEHOLDERS = ordered(
            PLACEHOLDER_INCREMENTAL, "the number of the duplicate: 1, 2, ...",
            PLACEHOLDER_BIE_ID, "the id of the BIE");

    static final Map<String, String> FLAGS = ordered(
            FLAG_INCLUDE_BUSINESS_CONTEXT, "leave the value out when the setting \"include business context\" is off",
            FLAG_INCLUDE_VERSION, "leave the value out when the setting \"include version\" is off");

    /** The indexed form of the business context placeholder, e.g. {@code Business Context Name[0]}. */
    static final String INDEXED_BUSINESS_CONTEXT_EXAMPLE = String.format(PLACEHOLDER_BUSINESS_CONTEXT_NAME_INDEXED_PATTERN, 0);
    static final String INDEXED_BUSINESS_CONTEXT_DETAIL =
            "the name of the business context at the index (from 0); empty when there is none";

    private static Map<String, String> ordered(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }
}
