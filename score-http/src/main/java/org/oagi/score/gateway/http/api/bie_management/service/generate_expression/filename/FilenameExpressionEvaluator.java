package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameTokenUtils.sanitizeFileName;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

final class FilenameExpressionEvaluator {

    private FilenameExpressionEvaluator() {
    }

    @FunctionalInterface
    interface PlaceholderResolver {
        PlaceholderValues resolve(String placeholderName, String flagName);
    }

    static String evaluate(FilenameExpressionParser.ParsedExpression expression,
                           PlaceholderResolver placeholderResolver) {
        if (expression == null || expression.nodes().isEmpty()) {
            return "";
        }
        return evaluateNodes(expression.nodes(), placeholderResolver).text();
    }

    private static EvaluationResult evaluateNodes(List<FilenameExpressionParser.Node> nodes,
                                                  PlaceholderResolver placeholderResolver) {
        StringBuilder output = new StringBuilder();
        boolean hasResolvedPlaceholder = false;

        for (FilenameExpressionParser.Node node : nodes) {
            if (node instanceof FilenameExpressionParser.LiteralNode literalNode) {
                output.append(literalNode.text());
                continue;
            }

            if (node instanceof FilenameExpressionParser.PlaceholderNode placeholderNode) {
                PlaceholderValues placeholderValues = placeholderResolver.resolve(
                        placeholderNode.placeholderName(), placeholderNode.flagName());
                if (placeholderValues == null) {
                    placeholderValues = PlaceholderValues.single("");
                }

                String value = placeholderValues.render(placeholderNode.modifiers());
                if (hasLength(value)) {
                    output.append(value);
                    hasResolvedPlaceholder = true;
                }
                continue;
            }

            if (node instanceof FilenameExpressionParser.OptionalGroupNode optionalGroupNode) {
                EvaluationResult nested = evaluateNodes(optionalGroupNode.nodes(), placeholderResolver);
                if (nested.hasResolvedPlaceholder()) {
                    output.append(nested.text());
                    hasResolvedPlaceholder = true;
                }
            }
        }

        return new EvaluationResult(output.toString(), hasResolvedPlaceholder);
    }

    static final class PlaceholderValues {
        private final List<String> values;

        private PlaceholderValues(List<String> values) {
            this.values = (values == null) ? List.of() : Collections.unmodifiableList(new ArrayList<>(values));
        }

        static PlaceholderValues single(String value) {
            if (value == null) {
                return new PlaceholderValues(List.of());
            }
            return new PlaceholderValues(List.of(value));
        }

        static PlaceholderValues multiple(List<String> values) {
            return new PlaceholderValues(values);
        }

        String render(List<FilenameExpressionParser.Modifier> modifiers) {
            if (values.isEmpty()) {
                return "";
            }
            ModifierPlan plan = ModifierPlan.from(modifiers);
            return values.stream()
                    .map(value -> normalizeSingle(value, plan))
                    .filter(value -> hasLength(value))
                    .reduce((left, right) -> left + plan.separator() + right)
                    .orElse("");
        }

        private static String normalizeSingle(String value, ModifierPlan plan) {
            if (!hasLength(value)) {
                return "";
            }
            String normalized = value.trim();
            for (FilenameExpressionParser.ReplaceModifier replaceModifier : plan.replaceModifiers()) {
                normalized = applyReplace(normalized, replaceModifier.pattern(), replaceModifier.replacement());
            }
            normalized = normalized.replaceAll("\\s+", Matcher.quoteReplacement(plan.separator()));
            return sanitizeFileName(normalized);
        }

        private static String applyReplace(String value, String pattern, String replacement) {
            try {
                return value.replaceAll(pattern, replacement);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regex in replace(): " + pattern, e);
            }
        }
    }

    private static final class ModifierPlan {
        private final String separator;
        private final List<FilenameExpressionParser.ReplaceModifier> replaceModifiers;

        private ModifierPlan(String separator, List<FilenameExpressionParser.ReplaceModifier> replaceModifiers) {
            this.separator = separator;
            this.replaceModifiers = replaceModifiers;
        }

        static ModifierPlan from(List<FilenameExpressionParser.Modifier> modifiers) {
            ModifierPlanBuilder builder = new ModifierPlanBuilder();
            if (modifiers != null) {
                for (FilenameExpressionParser.Modifier modifier : modifiers) {
                    modifier.accept(builder);
                }
            }
            return builder.build();
        }

        String separator() {
            return separator;
        }

        List<FilenameExpressionParser.ReplaceModifier> replaceModifiers() {
            return replaceModifiers;
        }
    }

    private static final class ModifierPlanBuilder
            implements FilenameExpressionParser.ModifierVisitor<Void> {

        private String separator = "";
        private final List<FilenameExpressionParser.ReplaceModifier> replaceModifiers = new ArrayList<>();

        @Override
        public Void visitSeparator(FilenameExpressionParser.SeparatorModifier modifier) {
            this.separator = modifier.separator();
            return null;
        }

        @Override
        public Void visitReplace(FilenameExpressionParser.ReplaceModifier modifier) {
            this.replaceModifiers.add(modifier);
            return null;
        }

        ModifierPlan build() {
            return new ModifierPlan(separator, Collections.unmodifiableList(new ArrayList<>(replaceModifiers)));
        }
    }

    private record EvaluationResult(String text, boolean hasResolvedPlaceholder) {
    }
}
