package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.oagi.score.gateway.http.common.util.StringUtils.trim;

@Component
public class FilenameExpressionValidationService {

    public record PreviewResult(String sampleFilename, String sampleDuplicateFilename,
                                FilenameSampleValues sampleValues) {
    }

    private static final Pattern BUSINESS_CONTEXT_NAME_INDEXED_PATTERN =
            Pattern.compile("^Business Context Name\\[(\\d+)]$");

    private static final Set<String> COMMON_PLACEHOLDERS = FilenameExpressionCatalog.COMMON_PLACEHOLDERS.keySet();

    private static final Set<String> PACKAGE_PLACEHOLDERS = FilenameExpressionCatalog.PACKAGE_PLACEHOLDERS.keySet();

    private static final Set<String> ALLOWED_FLAGS = FilenameExpressionCatalog.FLAGS.keySet();

    private static final Set<String> ALLOWED_DUPLICATE_PLACEHOLDERS = FilenameExpressionCatalog.DUPLICATE_PLACEHOLDERS.keySet();

    private static final Map<String, Boolean> DEFAULT_FLAGS = Map.of(
            ExpressionBasedFilenameStrategy.FLAG_INCLUDE_BUSINESS_CONTEXT, true,
            ExpressionBasedFilenameStrategy.FLAG_INCLUDE_VERSION, true
    );

    public void validateBieSchemaExpression(String expression, String duplicateHandlerExpression) {
        preview(expression, duplicateHandlerExpression, false, FilenameSampleValues.defaults());
    }

    public void validateBiePackageSchemaExpression(String expression, String duplicateHandlerExpression) {
        preview(expression, duplicateHandlerExpression, true, FilenameSampleValues.defaults());
    }

    public PreviewResult previewBieSchemaExpression(String expression, String duplicateHandlerExpression,
                                                    FilenameSampleValues sampleValues) {
        return preview(expression, duplicateHandlerExpression, false, sampleValues);
    }

    public PreviewResult previewBiePackageSchemaExpression(String expression, String duplicateHandlerExpression,
                                                           FilenameSampleValues sampleValues) {
        return preview(expression, duplicateHandlerExpression, true, sampleValues);
    }

    private PreviewResult preview(String expression,
                                  String duplicateHandlerExpression,
                                  boolean packageExpression,
                                  FilenameSampleValues sampleValues) {
        String normalizedExpression = trim(expression);
        if (!hasLength(normalizedExpression)) {
            throw new IllegalArgumentException("Filename expression must not be empty.");
        }
        String normalizedDuplicateHandlerExpression = trim(duplicateHandlerExpression);

        String baseFilename = evaluateFilenameExpression(normalizedExpression, packageExpression, sampleValues);
        String duplicateFilename = applyDuplicateHandlerExpression(
                baseFilename, normalizedDuplicateHandlerExpression, 1, 2, sampleValues);
        return new PreviewResult(baseFilename, duplicateFilename, sampleValues);
    }

    private String evaluateFilenameExpression(String expression, boolean packageExpression,
                                              FilenameSampleValues sampleValues) {
        FilenameExpressionParser.ParsedExpression parsedExpression = FilenameExpressionParser.parse(expression);
        String evaluated = FilenameExpressionEvaluator.evaluate(parsedExpression,
                (placeholderName, flagName) -> {
                    validateFlag(flagName);
                    if (hasLength(flagName) && !Boolean.TRUE.equals(DEFAULT_FLAGS.get(flagName))) {
                        return FilenameExpressionEvaluator.PlaceholderValues.single("");
                    }
                    return resolveFilenamePlaceholderValues(placeholderName, packageExpression, sampleValues);
                });
        return FilenameTokenUtils.sanitizeFileName(evaluated);
    }

    private String applyDuplicateHandlerExpression(String baseFilename,
                                                   String duplicateHandlerExpression,
                                                   int occurrence,
                                                   int totalOccurrences,
                                                   FilenameSampleValues sampleValues) {
        if (totalOccurrences <= 1 || !hasLength(duplicateHandlerExpression)) {
            return baseFilename;
        }
        if (usesPlaceholder(duplicateHandlerExpression, ExpressionBasedFilenameStrategy.PLACEHOLDER_INCREMENTAL)
                && occurrence == 0) {
            return baseFilename;
        }

        FilenameExpressionParser.ParsedExpression parsedExpression =
                FilenameExpressionParser.parse(duplicateHandlerExpression);
        String suffix = FilenameExpressionEvaluator.evaluate(parsedExpression,
                (placeholderName, flagName) -> resolveDuplicatePlaceholderValues(placeholderName, flagName, occurrence, sampleValues));
        if (!hasLength(suffix)) {
            return baseFilename;
        }
        return baseFilename + FilenameTokenUtils.sanitizeFileName(suffix);
    }

    private FilenameExpressionEvaluator.PlaceholderValues resolveFilenamePlaceholderValues(
            String placeholderName, boolean packageExpression, FilenameSampleValues sampleValues) {
        Matcher indexedMatcher = BUSINESS_CONTEXT_NAME_INDEXED_PATTERN.matcher(placeholderName);
        if (indexedMatcher.matches()) {
            int index = Integer.parseInt(indexedMatcher.group(1));
            List<String> names = sampleValues.businessContextNames();
            return FilenameExpressionEvaluator.PlaceholderValues.single(index < names.size() ? names.get(index) : "");
        }

        if (ExpressionBasedFilenameStrategy.PLACEHOLDER_BUSINESS_CONTEXT_NAMES.equals(placeholderName)) {
            return FilenameExpressionEvaluator.PlaceholderValues.multiple(sampleValues.businessContextNames());
        }

        if (COMMON_PLACEHOLDERS.contains(placeholderName)) {
            return FilenameExpressionEvaluator.PlaceholderValues.single(sampleValues.values().get(placeholderName));
        }

        if (PACKAGE_PLACEHOLDERS.contains(placeholderName)) {
            if (!packageExpression) {
                throw new IllegalArgumentException(
                        "BIE package placeholders are only allowed in BIE Package Schema Expression.");
            }
            return FilenameExpressionEvaluator.PlaceholderValues.single(sampleValues.values().get(placeholderName));
        }

        throw new IllegalArgumentException("Unknown filename placeholder: {" + placeholderName + "}");
    }

    private FilenameExpressionEvaluator.PlaceholderValues resolveDuplicatePlaceholderValues(
            String placeholderName, String flagName, int occurrence, FilenameSampleValues sampleValues) {
        if (hasLength(flagName)) {
            throw new IllegalArgumentException("Duplicate handler expression does not support flags: ?" + flagName);
        }
        if (!ALLOWED_DUPLICATE_PLACEHOLDERS.contains(placeholderName)) {
            throw new IllegalArgumentException("Unknown duplicate placeholder: {" + placeholderName + "}");
        }

        return switch (placeholderName) {
            case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_ID ->
                    FilenameExpressionEvaluator.PlaceholderValues.single(
                            sampleValues.values().get(ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_ID));
            case ExpressionBasedFilenameStrategy.PLACEHOLDER_INCREMENTAL ->
                    FilenameExpressionEvaluator.PlaceholderValues.single(String.valueOf(occurrence));
            default -> throw new IllegalArgumentException("Unknown duplicate placeholder: {" + placeholderName + "}");
        };
    }

    private void validateFlag(String flagName) {
        if (hasLength(flagName) && !ALLOWED_FLAGS.contains(flagName)) {
            throw new IllegalArgumentException("Unknown filename flag: ?" + flagName);
        }
    }

    private boolean usesPlaceholder(String expression, String placeholderName) {
        if (!hasLength(expression)) {
            return false;
        }
        return Pattern.compile("\\{" + Pattern.quote(placeholderName) + "(?:\\?[^}:]+)?(?:\\:[^}]*)?}")
                .matcher(expression)
                .find();
    }

}
