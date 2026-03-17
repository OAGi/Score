package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;

/**
 * Parses filename expression strings into a typed AST.
 * <p>
 * EBNF:
 * <pre>
 * expression          = { element } ;
 * element             = literal | optional-group | placeholder ;
 *
 * optional-group      = "(" , expression , ")" ;
 * placeholder         = "{" , placeholder-spec , "}" ;
 *
 * placeholder-spec    = placeholder-head , [ ":" , modifier-chain ] ;
 * placeholder-head    = placeholder-name , [ "?" , flag-name ] ;
 *
 * modifier-chain      = modifier-token , { ":" , modifier-token } ;
 * modifier-token      = separator-modifier | replace-modifier ;
 *
 * separator-modifier  = "separator" , "(" , quoted-arg , ")" | raw-separator ;
 * replace-modifier    = "replace" , "(" , quoted-arg , "," , quoted-arg , ")" ;
 *
 * quoted-arg          = "'" , { escaped-char | normal-char } , "'" ;
 * escaped-char        = "\" , any-char ;
 * normal-char         = any-char - "'" - "}" ;
 *
 * raw-separator       = non-empty-text-without-colon-parentheses ;
 * placeholder-name    = non-empty-text ;
 * flag-name           = non-empty-text ;
 * literal             = any-char - "{" - "}" - "(" - ")" ;
 * </pre>
 *
 * Notes:
 * <ul>
 *     <li>Parser enforces balanced parentheses and braces.</li>
 *     <li>Placeholder closing brace is the first {@code }}, so {@code }} is not allowed inside placeholder modifiers.</li>
 *     <li>{@code raw-separator} is backward-compatible shorthand for {@code separator('raw-separator')}.</li>
 * </ul>
 */
final class FilenameExpressionParser {

    private FilenameExpressionParser() {
    }

    static ParsedExpression parse(String expression) {
        if (!hasLength(expression)) {
            return new ParsedExpression(List.of());
        }
        ParseResult result = parseNodes(expression, 0, '\0');
        return new ParsedExpression(result.nodes());
    }

    private static ParseResult parseNodes(String expression, int startIndex, char endChar) {
        List<Node> nodes = new ArrayList<>();
        StringBuilder literal = new StringBuilder();
        int index = startIndex;

        while (index < expression.length()) {
            char ch = expression.charAt(index);
            if (endChar != '\0' && ch == endChar) {
                flushLiteral(nodes, literal);
                return new ParseResult(nodes, index + 1);
            }
            if (endChar == '\0' && ch == ')') {
                throw new IllegalArgumentException(
                        "Invalid filename expression: unmatched ')' at index " + index);
            }
            if (ch == '}') {
                throw new IllegalArgumentException(
                        "Invalid filename expression: unmatched '}' at index " + index);
            }

            if (ch == '{') {
                flushLiteral(nodes, literal);

                int closeIndex = expression.indexOf('}', index + 1);
                if (closeIndex < 0) {
                    throw new IllegalArgumentException(
                            "Invalid filename expression: unmatched '{' at index " + index);
                }

                String placeholderSpec = expression.substring(index + 1, closeIndex).trim();
                nodes.add(parsePlaceholderToken(placeholderSpec, index));
                index = closeIndex + 1;
                continue;
            }

            if (ch == '(') {
                flushLiteral(nodes, literal);
                ParseResult nested = parseNodes(expression, index + 1, ')');
                nodes.add(new OptionalGroupNode(nested.nodes()));
                index = nested.nextIndex();
                continue;
            }

            literal.append(ch);
            index++;
        }

        if (endChar != '\0') {
            throw new IllegalArgumentException("Invalid filename expression: missing closing '" + endChar + "'");
        }

        flushLiteral(nodes, literal);
        return new ParseResult(nodes, index);
    }

    private static void flushLiteral(List<Node> nodes, StringBuilder literal) {
        if (literal.isEmpty()) {
            return;
        }
        nodes.add(new LiteralNode(literal.toString()));
        literal.setLength(0);
    }

    private static PlaceholderNode parsePlaceholderToken(String placeholderSpec, int openBraceIndex) {
        if (!hasLength(placeholderSpec)) {
            throw new IllegalArgumentException(
                    "Invalid filename expression: empty placeholder at index " + openBraceIndex);
        }

        int separatorIndex = placeholderSpec.indexOf(':');
        String leftPart;
        List<Modifier> modifiers;
        if (separatorIndex < 0) {
            leftPart = placeholderSpec.trim();
            modifiers = List.of();
        } else {
            leftPart = placeholderSpec.substring(0, separatorIndex).trim();
            String modifierExpression = placeholderSpec.substring(separatorIndex + 1).trim();
            modifiers = parseModifierTokens(modifierExpression, openBraceIndex);
        }

        int flagIndex = leftPart.indexOf('?');
        String placeholderName;
        String flagName;
        if (flagIndex < 0) {
            placeholderName = leftPart.trim();
            flagName = "";
        } else {
            placeholderName = leftPart.substring(0, flagIndex).trim();
            flagName = leftPart.substring(flagIndex + 1).trim();
            if (!hasLength(flagName)) {
                throw new IllegalArgumentException(
                        "Invalid filename expression: empty flag name at index " + openBraceIndex);
            }
        }
        if (!hasLength(placeholderName)) {
            throw new IllegalArgumentException(
                    "Invalid filename expression: empty placeholder name at index " + openBraceIndex);
        }

        return new PlaceholderNode(placeholderName, flagName, modifiers);
    }

    private static List<Modifier> parseModifierTokens(String modifierExpression, int openBraceIndex) {
        List<String> tokens = splitModifierTokens(modifierExpression, openBraceIndex);
        List<Modifier> modifiers = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            modifiers.add(parseModifierToken(token));
        }
        return Collections.unmodifiableList(modifiers);
    }

    private static List<String> splitModifierTokens(String modifierExpression, int openBraceIndex) {
        if (!hasLength(modifierExpression)) {
            throw new IllegalArgumentException(
                    "Invalid filename expression: empty modifier at index " + openBraceIndex);
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        boolean escaped = false;
        int parenthesisDepth = 0;

        for (int i = 0; i < modifierExpression.length(); i++) {
            char ch = modifierExpression.charAt(i);
            if (escaped) {
                current.append(ch);
                escaped = false;
                continue;
            }

            if (inQuote && ch == '\\') {
                current.append(ch);
                escaped = true;
                continue;
            }

            if (ch == '\'') {
                inQuote = !inQuote;
                current.append(ch);
                continue;
            }

            if (!inQuote) {
                if (ch == '(') {
                    parenthesisDepth++;
                } else if (ch == ')') {
                    parenthesisDepth--;
                    if (parenthesisDepth < 0) {
                        throw new IllegalArgumentException(
                                "Invalid filename expression: invalid modifier at index " + openBraceIndex);
                    }
                } else if (ch == ':' && parenthesisDepth == 0) {
                    String token = current.toString().trim();
                    if (!hasLength(token)) {
                        throw new IllegalArgumentException(
                                "Invalid filename expression: invalid modifier at index " + openBraceIndex);
                    }
                    tokens.add(token);
                    current.setLength(0);
                    continue;
                }
            }

            current.append(ch);
        }

        if (inQuote || parenthesisDepth != 0) {
            throw new IllegalArgumentException(
                    "Invalid filename expression: invalid modifier at index " + openBraceIndex);
        }
        String token = current.toString().trim();
        if (!hasLength(token)) {
            throw new IllegalArgumentException(
                    "Invalid filename expression: invalid modifier at index " + openBraceIndex);
        }
        tokens.add(token);
        return tokens;
    }

    private static Modifier parseModifierToken(String token) {
        if (token.startsWith("separator(") && token.endsWith(")")) {
            List<String> args = parseQuotedArguments(
                    token.substring("separator(".length(), token.length() - 1), 1, token);
            return new SeparatorModifier(args.get(0));
        }
        if (token.startsWith("replace(") && token.endsWith(")")) {
            List<String> args = parseQuotedArguments(
                    token.substring("replace(".length(), token.length() - 1), 2, token);
            return new ReplaceModifier(args.get(0), args.get(1));
        }

        // Backward compatibility: raw token means separator.
        if (!token.contains("(") && !token.contains(")")) {
            return new SeparatorModifier(token);
        }
        throw new IllegalArgumentException("Invalid filename modifier token: " + token);
    }

    private static List<String> parseQuotedArguments(String text, int expectedCount, String originalToken) {
        List<String> args = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            if (index >= text.length()) {
                break;
            }
            if (text.charAt(index) != '\'') {
                throw new IllegalArgumentException("Invalid filename modifier token: " + originalToken);
            }
            index++;
            StringBuilder arg = new StringBuilder();
            boolean closed = false;
            while (index < text.length()) {
                char ch = text.charAt(index++);
                if (ch == '\\') {
                    if (index >= text.length()) {
                        throw new IllegalArgumentException("Invalid filename modifier token: " + originalToken);
                    }
                    arg.append(text.charAt(index++));
                    continue;
                }
                if (ch == '\'') {
                    closed = true;
                    break;
                }
                arg.append(ch);
            }
            if (!closed) {
                throw new IllegalArgumentException("Invalid filename modifier token: " + originalToken);
            }
            args.add(arg.toString());

            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            if (index < text.length()) {
                if (text.charAt(index) != ',') {
                    throw new IllegalArgumentException("Invalid filename modifier token: " + originalToken);
                }
                index++;
            }
        }

        if (args.size() != expectedCount) {
            throw new IllegalArgumentException("Invalid filename modifier token: " + originalToken);
        }
        return args;
    }

    sealed interface Node permits LiteralNode, PlaceholderNode, OptionalGroupNode {
    }

    record LiteralNode(String text) implements Node {
    }

    record PlaceholderNode(String placeholderName, String flagName, List<Modifier> modifiers) implements Node {
        PlaceholderNode {
            modifiers = (modifiers == null) ? List.of() : Collections.unmodifiableList(new ArrayList<>(modifiers));
        }
    }

    record OptionalGroupNode(List<Node> nodes) implements Node {
        OptionalGroupNode {
            nodes = (nodes == null) ? List.of() : Collections.unmodifiableList(new ArrayList<>(nodes));
        }
    }

    interface ModifierVisitor<R> {
        R visitSeparator(SeparatorModifier modifier);

        R visitReplace(ReplaceModifier modifier);
    }

    sealed interface Modifier permits SeparatorModifier, ReplaceModifier {
        <R> R accept(ModifierVisitor<R> visitor);
    }

    record SeparatorModifier(String separator) implements Modifier {
        @Override
        public <R> R accept(ModifierVisitor<R> visitor) {
            return visitor.visitSeparator(this);
        }
    }

    record ReplaceModifier(String pattern, String replacement) implements Modifier {
        @Override
        public <R> R accept(ModifierVisitor<R> visitor) {
            return visitor.visitReplace(this);
        }
    }

    record ParsedExpression(List<Node> nodes) {
        ParsedExpression {
            nodes = (nodes == null) ? List.of() : Collections.unmodifiableList(new ArrayList<>(nodes));
        }
    }

    private record ParseResult(List<Node> nodes, int nextIndex) {
    }
}
