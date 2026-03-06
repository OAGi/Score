package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.oagi.score.gateway.http.common.util.StringUtils.trim;

@Component
public class FilenameExpressionValidationService {

    public record PreviewResult(String sampleFilename, String sampleDuplicateFilename) {
    }

    private static final Pattern BUSINESS_CONTEXT_NAME_INDEXED_PATTERN =
            Pattern.compile("^Business Context Name\\[(\\d+)]$");

    private static final Set<String> COMMON_PLACEHOLDERS = Set.of(
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BUSINESS_CONTEXT_NAMES,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_ID,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PROPERTY_TERM,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_DISPLAY_NAME,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_VERSION,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_DEN,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_STATUS,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_REMARK
    );

    private static final Set<String> PACKAGE_PLACEHOLDERS = Set.of(
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_NAME,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_VERSION_ID,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_VERSION_NAME
    );

    private static final Set<String> ALLOWED_FLAGS = Set.of(
            ExpressionBasedFilenameStrategy.FLAG_INCLUDE_BUSINESS_CONTEXT,
            ExpressionBasedFilenameStrategy.FLAG_INCLUDE_VERSION
    );

    private static final Set<String> ALLOWED_DUPLICATE_PLACEHOLDERS = Set.of(
            ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_ID,
            ExpressionBasedFilenameStrategy.PLACEHOLDER_INCREMENTAL
    );

    private static final Map<String, Boolean> DEFAULT_FLAGS = Map.of(
            ExpressionBasedFilenameStrategy.FLAG_INCLUDE_BUSINESS_CONTEXT, true,
            ExpressionBasedFilenameStrategy.FLAG_INCLUDE_VERSION, true
    );

    public void validateBieSchemaExpression(String expression, String duplicateHandlerExpression) {
        preview(expression, duplicateHandlerExpression, false);
    }

    public void validateBiePackageSchemaExpression(String expression, String duplicateHandlerExpression) {
        preview(expression, duplicateHandlerExpression, true);
    }

    public PreviewResult previewBieSchemaExpression(String expression, String duplicateHandlerExpression) {
        return preview(expression, duplicateHandlerExpression, false);
    }

    public PreviewResult previewBiePackageSchemaExpression(String expression, String duplicateHandlerExpression) {
        return preview(expression, duplicateHandlerExpression, true);
    }

    private PreviewResult preview(String expression,
                                  String duplicateHandlerExpression,
                                  boolean packageExpression) {
        String normalizedExpression = trim(expression);
        if (!hasLength(normalizedExpression)) {
            throw new IllegalArgumentException("Filename expression must not be empty.");
        }
        String normalizedDuplicateHandlerExpression = trim(duplicateHandlerExpression);

        String baseFilename = evaluateFilenameExpression(normalizedExpression, packageExpression);
        String duplicateFilename = applyDuplicateHandlerExpression(
                baseFilename, normalizedDuplicateHandlerExpression, 1, 2);
        return new PreviewResult(baseFilename, duplicateFilename);
    }

    private String evaluateFilenameExpression(String expression, boolean packageExpression) {
        FilenameExpressionParser.ParsedExpression parsedExpression = FilenameExpressionParser.parse(expression);
        String evaluated = FilenameExpressionEvaluator.evaluate(parsedExpression,
                (placeholderName, flagName) -> {
                    validateFlag(flagName);
                    if (hasLength(flagName) && !Boolean.TRUE.equals(DEFAULT_FLAGS.get(flagName))) {
                        return FilenameExpressionEvaluator.PlaceholderValues.single("");
                    }
                    return resolveFilenamePlaceholderValues(placeholderName, packageExpression);
                });
        return FilenameTokenUtils.sanitizeFileName(evaluated);
    }

    private String applyDuplicateHandlerExpression(String baseFilename,
                                                   String duplicateHandlerExpression,
                                                   int occurrence,
                                                   int totalOccurrences) {
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
                (placeholderName, flagName) -> resolveDuplicatePlaceholderValues(placeholderName, flagName, occurrence));
        if (!hasLength(suffix)) {
            return baseFilename;
        }
        return baseFilename + FilenameTokenUtils.sanitizeFileName(suffix);
    }

    private FilenameExpressionEvaluator.PlaceholderValues resolveFilenamePlaceholderValues(
            String placeholderName, boolean packageExpression) {
        if (BUSINESS_CONTEXT_NAME_INDEXED_PATTERN.matcher(placeholderName).matches()) {
            return FilenameExpressionEvaluator.PlaceholderValues.single("Default Context");
        }

        if (COMMON_PLACEHOLDERS.contains(placeholderName)) {
            return switch (placeholderName) {
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BUSINESS_CONTEXT_NAMES ->
                        FilenameExpressionEvaluator.PlaceholderValues.multiple(
                                List.of("Default Context", "Secondary Context"));
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_ID ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("1001");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PROPERTY_TERM ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("Invoice");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_DISPLAY_NAME ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("Invoice Header");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_VERSION ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("1.0");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_DEN ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("Invoice. Details");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_STATUS ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("Published");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_REMARK ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("Sample remark");
                default -> throw new IllegalArgumentException("Unknown filename placeholder: {" + placeholderName + "}");
            };
        }

        if (PACKAGE_PLACEHOLDERS.contains(placeholderName)) {
            if (!packageExpression) {
                throw new IllegalArgumentException(
                        "BIE package placeholders are only allowed in BIE Package Schema Expression.");
            }
            return switch (placeholderName) {
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_NAME ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("Sample Package");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_VERSION_ID ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("v1");
                case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_PACKAGE_VERSION_NAME ->
                        FilenameExpressionEvaluator.PlaceholderValues.single("1.0");
                default -> throw new IllegalArgumentException("Unknown filename placeholder: {" + placeholderName + "}");
            };
        }

        throw new IllegalArgumentException("Unknown filename placeholder: {" + placeholderName + "}");
    }

    private FilenameExpressionEvaluator.PlaceholderValues resolveDuplicatePlaceholderValues(
            String placeholderName, String flagName, int occurrence) {
        if (hasLength(flagName)) {
            throw new IllegalArgumentException("Duplicate handler expression does not support flags: ?" + flagName);
        }
        if (!ALLOWED_DUPLICATE_PLACEHOLDERS.contains(placeholderName)) {
            throw new IllegalArgumentException("Unknown duplicate placeholder: {" + placeholderName + "}");
        }

        return switch (placeholderName) {
            case ExpressionBasedFilenameStrategy.PLACEHOLDER_BIE_ID ->
                    FilenameExpressionEvaluator.PlaceholderValues.single("1001");
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
