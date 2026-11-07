package org.oagi.score.gateway.http.api.oas_management.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What the generator and the document editing agree on about a Resource Path: the {@code {version}} is the version of
 * the document, and every other {@code {name}} is a path parameter.
 */
public final class OasResourcePaths {

    public static final String VERSION_PATH_PARAMETER = "{version}";
    private static final Pattern PATH_PARAMETER_PATTERN = Pattern.compile("\\{([^{}]+)}");

    private OasResourcePaths() {
    }

    /** The path as the generated document has it: {@code {version}} replaced by the version of the document, if it has one. */
    public static String resolve(String resourceName, String documentVersion) {
        if (resourceName == null || resourceName.isEmpty() || documentVersion == null || documentVersion.isEmpty()) {
            return resourceName;
        }
        return resourceName.replace(VERSION_PATH_PARAMETER, documentVersion);
    }

    /** The names of the path parameters of a (resolved) path, in the order they appear, each once. */
    public static List<String> parameterNames(String resolvedResourceName) {
        if (resolvedResourceName == null || resolvedResourceName.isEmpty()) {
            return List.of();
        }
        // Issue #1710: only valid `{...}` placeholders; malformed fragments are ignored.
        Set<String> names = new LinkedHashSet<>();
        Matcher matcher = PATH_PARAMETER_PATTERN.matcher(resolvedResourceName);
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (!candidate.isEmpty()) {
                names.add(candidate);
            }
        }
        return new ArrayList<>(names);
    }
}
