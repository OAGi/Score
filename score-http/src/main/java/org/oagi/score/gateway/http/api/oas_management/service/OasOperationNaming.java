package org.oagi.score.gateway.http.api.oas_management.service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * How the Operation ID and the Resource Name of an operation of an OpenAPI Document are made from the verb, the BIE and
 * whether it is an array, and when a Resource Name is a path at all. The clients only say what the user did (the verb or
 * the array indicator was changed) and show what this makes of it.
 *
 * The array marker ({@code List} of an operationId, {@code -list} of a path) is driven by the array indicator and the
 * property term only, never by looking at the string, so a term that itself ends in "List" keeps its own and still gets
 * the marker: the single and the array form stay distinct (queryPriceList, queryPriceListList).
 */
public final class OasOperationNaming {

    private static final Map<String, String> VERB_WORDS = Map.of(
            "GET", "query", "POST", "create", "PUT", "replace", "PATCH", "update", "DELETE", "delete",
            "OPTIONS", "options", "HEAD", "head", "TRACE", "trace");
    /** The words that may already lead an operationId, the legacy {@code get} included. */
    private static final String[] KNOWN_VERB_WORDS =
            {"query", "create", "replace", "update", "delete", "options", "head", "trace", "get"};
    private static final String LIST = "List";

    private OasOperationNaming() {
    }

    /** The word an operationId starts with for the verb; empty for a verb that has none. */
    public static String verbWord(String verb) {
        return verb == null ? "" : VERB_WORDS.getOrDefault(verb.trim().toUpperCase(Locale.ROOT), "");
    }

    /** The BIE segment of an operationId: the property term without spaces and with its first letter capitalized. */
    public static String operationName(String bieName) {
        String name = bieName == null ? "" : bieName.replaceAll("\\s", "");
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /** {@code <verb word><BIE name>[List]}; empty for a verb that has no word. */
    public static String buildOperationId(String verb, String bieName, boolean isArray) {
        String word = verbWord(verb);
        return word.isEmpty() ? "" : word + operationName(bieName) + (isArray ? LIST : "");
    }

    /**
     * The BIE segment of an existing operationId, for an operation that has no property term (one without a BIE): the verb word
     * (and a legacy {@code <business context>_} prefix) is dropped, and the array marker only when the array indicator says
     * there is one, so a name that really ends in "List" is not cut.
     */
    public static String extractBieName(String operationId, boolean isArray) {
        String name = operationId == null ? "" : operationId.trim();
        int underscore = name.lastIndexOf('_');
        if (underscore >= 0) {
            name = name.substring(underscore + 1);
        }
        for (String word : KNOWN_VERB_WORDS) {
            if (name.length() > word.length() && name.startsWith(word) && Character.isUpperCase(name.charAt(word.length()))) {
                name = name.substring(word.length());
                break;
            }
        }
        if (isArray && name.endsWith(LIST)) {
            name = name.substring(0, name.length() - LIST.length());
        }
        return name;
    }

    /**
     * The operationId after the verb or the array indicator was changed: from the property term when there is one, else from
     * the id as it was. A verb that has no word leaves the id as it was.
     */
    public static String recomputeOperationId(String verb, String oldOperationId, boolean isArray, String propertyTerm) {
        if (verbWord(verb).isEmpty()) {
            return oldOperationId;
        }
        String name = (propertyTerm != null && !propertyTerm.isBlank()) ? propertyTerm : extractBieName(oldOperationId, isArray);
        return buildOperationId(verb, name, isArray);
    }

    /**
     * The Operation ID of an operation that has no BIE, from its verb and the last segment of its path that is not a path
     * parameter: {@code PATCH /production-order/{id}} is {@code updateProductionOrder}. Empty when there is nothing to make it of.
     */
    public static String operationIdFromPath(String verb, String path) {
        if (verb == null || verb.isBlank() || path == null || path.isBlank()) {
            return "";
        }
        String last = "";
        for (String segment : path.split("/")) {
            String trimmed = segment.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("{")) {
                last = trimmed;
            }
        }
        StringBuilder resource = new StringBuilder();
        for (String part : last.split("[-_\\s]+")) {
            if (!part.isEmpty()) {
                resource.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        String upper = verb.trim().toUpperCase(Locale.ROOT);
        String action = "DELETE".equals(upper) ? "delete" : "PATCH".equals(upper) ? "update" : verb.trim().toLowerCase(Locale.ROOT);
        return resource.length() == 0 ? "" : action + resource;
    }

    /** The last segment of a Resource Name: the property term in lower case with dashes for spaces, and {@code -list} for an array. */
    public static String resourcePathLeaf(String propertyTerm, boolean isArray) {
        String term = (propertyTerm == null ? "" : propertyTerm).replaceAll("\\s", "-").toLowerCase(Locale.ROOT);
        return isArray ? term + "-list" : term;
    }

    /** {@code /<business context>/<version>/<leaf>}; without the version segment when the document has none. */
    public static String buildResourcePath(String businessContextName, String version, String propertyTerm, boolean isArray) {
        String context = (businessContextName == null ? "" : businessContextName).toLowerCase(Locale.ROOT).replace(' ', '-');
        String leaf = resourcePathLeaf(propertyTerm, isArray);
        return (version == null || version.isEmpty()) ? "/" + context + "/" + leaf : "/" + context + "/" + version + "/" + leaf;
    }

    /**
     * Make the array marker of a Resource Name follow the array indicator by making its last segment again from the property
     * term; the rest of the path is as the user has it. The last segment of a path that ends in a path parameter
     * ({@code /containers/{id}}) is not the BIE's, and is left as it is.
     */
    public static String applyResourceArrayMarker(String resourceName, String propertyTerm, boolean isArray) {
        String path = resourceName == null ? "" : resourceName;
        int slash = path.lastIndexOf('/');
        String leaf = path.substring(slash + 1);
        if (leaf.startsWith("{") && leaf.endsWith("}")) {
            return path;
        }
        return path.substring(0, slash + 1) + resourcePathLeaf(propertyTerm, isArray);
    }

    /**
     * Why the Resource Name is not a path, or empty when it is. The path is a key of {@code paths} of the generated document:
     * it starts with a slash, has no whitespace, query string or fragment, and its braces are balanced and not empty. An empty
     * path is for the check that it is given.
     */
    public static Optional<String> resourcePathError(String path) {
        String value = path == null ? "" : path;
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (!value.startsWith("/")) {
            return Optional.of("Path must start with \"/\".");
        }
        if (value.codePoints().anyMatch(Character::isWhitespace)) {
            return Optional.of("Path must not contain whitespace.");
        }
        if (value.contains("?") || value.contains("#")) {
            return Optional.of("Path must not contain a query string (\"?\") or fragment (\"#\").");
        }
        boolean open = false;
        int length = 0;
        for (char c : value.toCharArray()) {
            if (c == '{') {
                if (open) {
                    return Optional.of("Path has unbalanced \"{\" and \"}\".");
                }
                open = true;
                length = 0;
            } else if (c == '}') {
                if (!open) {
                    return Optional.of("Path has unbalanced \"{\" and \"}\".");
                }
                if (length == 0) {
                    return Optional.of("Path parameter \"{}\" must not be empty.");
                }
                open = false;
            } else if (open) {
                length++;
            }
        }
        return open ? Optional.of("Path has unbalanced \"{\" and \"}\".") : Optional.empty();
    }
}
