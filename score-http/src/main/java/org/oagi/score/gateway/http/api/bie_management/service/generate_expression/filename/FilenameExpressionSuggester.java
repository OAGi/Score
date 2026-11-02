package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameExpressionCatalog.*;

/**
 * Completions for a filename expression at a caret: the start of a placeholder or an optional group, and inside
 * {@code {...}} the placeholder names, the {@code ?flag} and the {@code :modifier}s, each as the grammar allows.
 * Nothing is offered in a quoted modifier argument or in the literal text between placeholders.
 */
@Component
public class FilenameExpressionSuggester {

    public enum Kind {SCHEMA, PACKAGE, DUPLICATE}

    /** One completion: replace {@code [from, to)} with {@code insert}; the caret then goes {@code caretBack} before its end. */
    public record Suggestion(String label, String insert, String detail, int caretBack) {
        Suggestion(String label, String insert, String detail) {
            this(label, insert, detail, 0);
        }
    }

    public record Suggestions(int from, int to, List<Suggestion> items) {
    }

    private static final Pattern INDEXED_BUSINESS_CONTEXT = Pattern.compile("^Business Context Name\\[\\d+]$");
    private static final Suggestion CLOSE = new Suggestion("}", "}", "end of the placeholder");
    private static final Suggestion FLAG = new Suggestion("?", "?", "add a flag: includeBusinessContext or includeVersion");
    private static final Suggestion MODIFIER = new Suggestion(":", ":", "add a modifier: separator('-') or replace('a', 'b')");
    private static final List<Suggestion> MODIFIERS = List.of(
            new Suggestion("separator('')", "separator('')",
                    "join the words (and the values of Business Context Names) with the text, e.g. separator('-')", 2),
            new Suggestion("replace('', '')", "replace('', '')",
                    "replace what a regular expression matches, e.g. replace('\\\\.', '_')", 6));

    public Suggestions suggest(String text, int caret, Kind kind) {
        text = text == null ? "" : text;
        caret = Math.max(0, Math.min(caret, text.length()));
        if (text.isBlank()) {
            return new Suggestions(0, text.length(), hints(kind));
        }
        String before = text.substring(0, caret);
        int brace = unclosedBrace(before);
        return brace < 0 ? outside(before, caret, kind) : inside(before, caret, kind, brace);
    }

    private List<Suggestion> hints(Kind kind) {
        if (kind == Kind.DUPLICATE) {
            return List.of(
                    new Suggestion("-{Incremental}", "-{Incremental}", "a hyphen and the number of the duplicate"),
                    new Suggestion("~{BIE ID}", "~{BIE ID}", "a tilde and the id of the BIE"));
        }
        return List.of(
                new Suggestion("{BIE Property Term:separator('-')}", "{BIE Property Term:separator('-')}",
                        "the property term with its words joined by a hyphen"),
                new Suggestion("(-{Business Context Name[0]?includeBusinessContext})",
                        "(-{Business Context Name[0]?includeBusinessContext})",
                        "an optional group: left out when everything in it is empty"),
                new Suggestion("(-{BIE Version?includeVersion:replace('\\\\.', '_')})",
                        "(-{BIE Version?includeVersion:replace('\\\\.', '_')})",
                        "the version with its dots replaced by underscores"));
    }

    /** The index of the unclosed {@code {} before the caret, or -1. */
    private int unclosedBrace(String before) {
        int start = -1;
        boolean quote = false;
        for (int i = 0; i < before.length(); i++) {
            char c = before.charAt(i);
            if (start >= 0 && quote) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    quote = false;
                }
            } else if (c == '{') {
                start = i;
            } else if (c == '}') {
                start = -1;
            } else if (start >= 0 && c == '\'') {
                quote = true;
            }
        }
        return start;
    }

    private Suggestions outside(String before, int caret, Kind kind) {
        // literal text is the user's own: placeholders are offered where one can start, not in the middle of a word
        if (!before.isEmpty() && Character.isLetterOrDigit(before.charAt(before.length() - 1))) {
            return none();
        }
        List<Suggestion> items = new ArrayList<>();
        items.add(new Suggestion("{", "{", "a placeholder, e.g. {BIE Property Term}"));
        if (kind != Kind.DUPLICATE) {
            items.add(new Suggestion("(", "(", "an optional group: it is left out when every placeholder in it is empty"));
            if (openParentheses(before) > 0) {
                items.add(new Suggestion(")", ")", "end of the optional group"));
            }
        }
        return new Suggestions(caret, caret, items);
    }

    private int openParentheses(String before) {
        int depth = 0;
        boolean inBrace = false;
        boolean quote = false;
        for (int i = 0; i < before.length(); i++) {
            char c = before.charAt(i);
            if (quote) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    quote = false;
                }
            } else if (inBrace) {
                if (c == '}') {
                    inBrace = false;
                } else if (c == '\'') {
                    quote = true;
                }
            } else if (c == '{') {
                inBrace = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')' && depth > 0) {
                depth--;
            }
        }
        return depth;
    }

    private Suggestions inside(String before, int caret, Kind kind, int brace) {
        String body = before.substring(brace + 1);
        if (inQuote(body)) {
            return none();
        }
        boolean duplicate = kind == Kind.DUPLICATE;
        int flagStart = body.indexOf('?');
        int modifierStart = body.indexOf(':');

        // the modifiers: {name:separator('-'):replace('a', 'b')}
        if (modifierStart >= 0 && !duplicate) {
            String afterColon = body.substring(body.lastIndexOf(':') + 1);
            String token = afterColon.strip();
            if (token.endsWith(")") && balanced(token)) {
                return new Suggestions(caret, caret, List.of(MODIFIER, CLOSE));
            }
            int tokenStart = caret - afterColon.length() + (afterColon.length() - afterColon.stripLeading().length());
            return rank(token, tokenStart, caret, MODIFIERS);
        }

        // the flag: {name?includeVersion}
        if (flagStart >= 0 && !duplicate) {
            String word = body.substring(flagStart + 1);
            if (FLAGS.containsKey(word)) {
                return new Suggestions(caret, caret, List.of(MODIFIER, CLOSE));
            }
            List<Suggestion> flags = new ArrayList<>();
            FLAGS.forEach((name, detail) -> flags.add(new Suggestion(name, name, detail)));
            return rank(word, caret - word.length(), caret, flags);
        }

        // the name: {BIE Property Term}
        Map<String, String> names = new LinkedHashMap<>();
        if (duplicate) {
            names.putAll(DUPLICATE_PLACEHOLDERS);
        } else {
            names.putAll(COMMON_PLACEHOLDERS);
            names.put(INDEXED_BUSINESS_CONTEXT_EXAMPLE, INDEXED_BUSINESS_CONTEXT_DETAIL);
            if (kind == Kind.PACKAGE) {
                names.putAll(PACKAGE_PLACEHOLDERS);
            }
        }
        boolean complete = names.keySet().stream().anyMatch(n -> n.equalsIgnoreCase(body))
                || INDEXED_BUSINESS_CONTEXT.matcher(body).matches();
        if (complete) {
            return new Suggestions(caret, caret, duplicate ? List.of(CLOSE) : List.of(CLOSE, FLAG, MODIFIER));
        }
        List<Suggestion> items = new ArrayList<>();
        names.forEach((name, detail) -> items.add(new Suggestion(name, name, detail)));
        return rank(body, caret - body.length(), caret, items);
    }

    private boolean inQuote(String body) {
        boolean quote = false;
        for (int i = 0; i < body.length(); i++) {
            if (quote && body.charAt(i) == '\\') {
                i++;
            } else if (body.charAt(i) == '\'') {
                quote = !quote;
            }
        }
        return quote;
    }

    private boolean balanced(String token) {
        int depth = 0;
        boolean quote = false;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (quote) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    quote = false;
                }
            } else if (c == '\'') {
                quote = true;
            } else if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            }
        }
        return depth == 0 && !quote;
    }

    /** Keeps the suggestions that start with (then contain) what has been typed. */
    private Suggestions rank(String word, int wordStart, int caret, List<Suggestion> items) {
        String typed = word.strip().toLowerCase();
        List<Suggestion> ranked = new ArrayList<>();
        items.stream().filter(i -> i.label().toLowerCase().startsWith(typed)).forEach(ranked::add);
        if (!typed.isEmpty()) {
            items.stream().filter(i -> !i.label().toLowerCase().startsWith(typed) && i.label().toLowerCase().contains(typed))
                    .forEach(ranked::add);
        }
        return new Suggestions(wordStart, caret, ranked);
    }

    private Suggestions none() {
        return new Suggestions(0, 0, List.of());
    }
}
