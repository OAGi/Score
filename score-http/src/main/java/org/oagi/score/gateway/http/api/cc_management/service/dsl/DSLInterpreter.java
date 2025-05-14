package org.oagi.score.gateway.http.api.cc_management.service.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

public class DSLInterpreter {

    public static void main(String[] args) {
        String dsl1 = "contains(\"Given Name\" or \"Family Name\")";
        String dsl2 = "not contains(\"Given Name\" and \"Family Name\")";
        String dsl3 = "count(children) = 0";
        String dsl4 = "count(children) > 2";
        String dsl5 = "count(children) != 3";
        String dsl6 = "count(children) > 2 and contains(\"Given Name\" and \"Family Name\")";
        String dsl7 = "count(children) > 2 or not contains(\"Nothing\")";
        String dsl8 = "child-of(\"Mary\")";

        DSLRecord record1 = new StringDSLRecord("Mary", "My given name is Alice.", 0);
        DSLRecord record2 = new StringDSLRecord("John", "My family name is Smith.", 3);
        DSLRecord record3 = new StringDSLRecord("Sara", "My given name is Alice and my family name is Smith.", 5);
        DSLRecord record4 = new StringDSLRecord("Tim", "Nothing here.", 3);

//        System.out.println(parseExpression(dsl1).test(record1)); // true
//        System.out.println(parseExpression(dsl2).test(record1)); // false
//        System.out.println(parseExpression(dsl3).test(record1)); // true
//        System.out.println(parseExpression(dsl4).test(record2)); // true
//        System.out.println(parseExpression(dsl5).test(record3)); // true
//        System.out.println(parseExpression(dsl5).test(record4)); // false
//        System.out.println(parseExpression(dsl6).test(record3)); // true
//        System.out.println(parseExpression(dsl7).test(record4)); // true
        System.out.println(parseExpression(dsl8).test(record1)); // true
        System.out.println(parseExpression(dsl8).test(record2)); // false
    }

    public static Predicate<DSLRecord> parseExpression(String dsl) {
        dsl = dsl.trim();
        List<String> tokens = tokenize(dsl);
        return parseExpressionTokens(tokens);
    }

    // Parses top-level expression tokens supporting AND/OR between expressions
    static Predicate<DSLRecord> parseExpressionTokens(List<String> tokens) {
        Stack<Predicate<DSLRecord>> predicates = new Stack<>();
        Stack<String> operators = new Stack<>();
        Stack<Integer> precedence = new Stack<>();

        for (int i = 0; i < tokens.size(); ) {
            String token = tokens.get(i);

            if (token.equalsIgnoreCase("and") || token.equalsIgnoreCase("or")) {
                // Handle logical operators (and, or)
                int currentPrecedence = (token.equalsIgnoreCase("and")) ? 1 : 0; // 'and' has higher precedence than 'or'

                while (!operators.isEmpty() && precedence.peek() >= currentPrecedence) {
                    applyOperator(operators.pop(), predicates.pop(), predicates.pop());
                    precedence.pop();
                }

                operators.push(token.toLowerCase());
                precedence.push(currentPrecedence);
                i++;
            } else {
                // Parse sub-expression (e.g., "contains(...)", "count(children)", etc.)
                StringBuilder subExpr = new StringBuilder();
                int parenDepth = 0;

                while (i < tokens.size()) {
                    String t = tokens.get(i);
                    if ((t.equalsIgnoreCase("and") || t.equalsIgnoreCase("or")) && parenDepth == 0) break;
                    if (t.equals("(")) parenDepth++;
                    if (t.equals(")")) parenDepth--;
                    subExpr.append(t).append(" ");
                    i++;
                }

                // Remove trailing space and parse the sub-expression
                Predicate<DSLRecord> pred = parseSingleExpression(subExpr.toString().trim());
                predicates.push(pred);
            }
        }

        // Apply remaining operators
        while (!operators.isEmpty()) {
            Predicate<DSLRecord> right = predicates.pop();
            Predicate<DSLRecord> left = predicates.pop();
            predicates.push(applyOperator(operators.pop(), right, left));
        }

        return predicates.pop();
    }

    private static Predicate<DSLRecord> applyOperator(String operator, Predicate<DSLRecord> right, Predicate<DSLRecord> left) {
        return operator.equals("and") ? left.and(right) : left.or(right);
    }

    // Parses individual expressions: count, contains, or not contains
    static Predicate<DSLRecord> parseSingleExpression(String expr) {
        expr = expr.trim();
        if (expr.startsWith("contains(")) {
            String inner = expr.substring("contains(".length(), expr.length() - 1).trim();
            return parseLogicalExpression(inner, false);
        } else if (expr.startsWith("not contains(")) {
            String inner = expr.substring("not contains(".length(), expr.length() - 1).trim();
            return parseLogicalExpression(inner, true);
        } else if (expr.startsWith("count(children)")) {
            return parseCountExpression(expr);
        } else if (expr.startsWith("name(")) {
            String inner = expr.substring("name(".length(), expr.length() - 1).trim();
            return parseNameExpression(inner);
        } else if (expr.startsWith("child-of(")) {
            String inner = expr.substring("child-of(".length(), expr.length() - 1).trim();
            return parseChildOfExpression(inner);
        } else {
            throw new IllegalArgumentException("Invalid expression: " + expr);
        }
    }

    static Predicate<DSLRecord> parseNameExpression(String expr) {
        // Example: name("Given Name")
        int start = expr.indexOf('"');
        int end = expr.lastIndexOf('"');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("Invalid name() expression: " + expr);
        }

        String keyword = expr.substring(start + 1, end).trim();
        return record -> record.name().equals(keyword);
    }

    static Predicate<DSLRecord> parseChildOfExpression(String expr) {
        // Example: parent("Given Name")
        int start = expr.indexOf('"');
        int end = expr.lastIndexOf('"');
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("Invalid child-of() expression: " + expr);
        }

        String keyword = expr.substring(start + 1, end).trim();
        return record -> record.isChildOf(keyword);
    }

    static Predicate<DSLRecord> parseCountExpression(String dsl) {
        dsl = dsl.trim();

        // Example expression: count(children) > 2
        String[] parts = dsl.split("\\(");
        if (!"count".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid count expression: " + dsl);
        }

        parts = parts[1].split("\\)");
        if (!"children".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid count expression: " + dsl);
        }

        // Split the expression into the count part and the comparison part (e.g., "> 2")
        String comparisonExpression = dsl.substring(parts[1].length() + 1).trim(); // Get the part after "count(children)"

        // Find the operator (it can be >, <, =, >=, <=, !=)
        String operator;
        String numberString = "";

        // Operators to check
        if (comparisonExpression.contains(">=")) {
            operator = ">=";
            numberString = comparisonExpression.split(">=")[1].trim();
        } else if (comparisonExpression.contains("<=")) {
            operator = "<=";
            numberString = comparisonExpression.split("<=")[1].trim();
        } else if (comparisonExpression.contains("!=")) {
            operator = "!=";
            numberString = comparisonExpression.split("!=")[1].trim();
        } else if (comparisonExpression.contains(">")) {
            operator = ">";
            numberString = comparisonExpression.split(">")[1].trim();
        } else if (comparisonExpression.contains("<")) {
            operator = "<";
            numberString = comparisonExpression.split("<")[1].trim();
        } else if (comparisonExpression.contains("=")) {
            operator = "=";
            numberString = comparisonExpression.split("=")[1].trim();
        } else {
            operator = "";
        }

        // Parse the number after the operator
        int comparisonValue;
        try {
            comparisonValue = Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in count expression: " + numberString);
        }

        // Create a predicate that checks for the count of children and the comparison
        return record -> {
            // Assuming DSLRecord has a method `getChildrenCount()` that returns the number of children
            int count = record.getChildrenCount();  // Replace with actual logic to get the children count

            // Apply the comparison based on the operator
            switch (operator) {
                case ">":
                    return count > comparisonValue;
                case "<":
                    return count < comparisonValue;
                case "=":
                    return count == comparisonValue;
                case ">=":
                    return count >= comparisonValue;
                case "<=":
                    return count <= comparisonValue;
                case "!=":
                    return count != comparisonValue;
                default:
                    throw new IllegalArgumentException("Invalid operator in count expression: " + operator);
            }
        };
    }

    static Predicate<DSLRecord> parseLogicalExpression(String input, boolean isNot) {
        input = input.trim();
        List<String> tokens = tokenize(input);
        return parseTokens(tokens, isNot);
    }

    static Predicate<DSLRecord> parseTokens(List<String> tokens, boolean isNot) {
        Stack<Predicate<DSLRecord>> predicates = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.equalsIgnoreCase("and") || token.equalsIgnoreCase("or")) {
                operators.push(token.toLowerCase());
            } else if (token.startsWith("\"") && token.endsWith("\"")) {
                String value = token.substring(1, token.length() - 1);
                Predicate<DSLRecord> pred = record -> record.contains(value);
                if (isNot) {
                    pred = pred.negate();
                }
                predicates.push(pred);
            }
        }

        while (!operators.isEmpty()) {
            Predicate<DSLRecord> right = predicates.pop();
            Predicate<DSLRecord> left = predicates.pop();
            String op = operators.remove(0);
            Predicate<DSLRecord> combined = op.equals("and") ? left.and(right) : left.or(right);
            predicates.push(combined);
        }

        return predicates.pop();
    }

    static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char ch = input.charAt(i);

            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            // Handle parentheses
            if (ch == '(' || ch == ')') {
                tokens.add(String.valueOf(ch));
                i++;
                continue;
            }

            // Handle quoted strings
            if (ch == '"') {
                int start = i;
                i++;
                StringBuilder quoted = new StringBuilder();
                while (i < input.length()) {
                    if (input.charAt(i) == '"') {
                        i++;
                        break;
                    }
                    quoted.append(input.charAt(i));
                    i++;
                }
                if (i > input.length()) {
                    throw new RuntimeException("Unclosed quote starting at index " + start);
                }
                tokens.add("\"" + quoted + "\"");
                continue;
            }

            // Handle operators "and", "or", "not"
            if (input.regionMatches(true, i, "and", 0, 3) && isDelimiter(input, i + 3)) {
                tokens.add("and");
                i += 3;
                continue;
            }
            if (input.regionMatches(true, i, "or", 0, 2) && isDelimiter(input, i + 2)) {
                tokens.add("or");
                i += 2;
                continue;
            }
            if (input.regionMatches(true, i, "not", 0, 3) && isDelimiter(input, i + 3)) {
                tokens.add("not");
                i += 3;
                continue;
            }

            // Handle "count(children)" as a single token
            if (input.regionMatches(true, i, "count(children)", 0, 15) && isDelimiter(input, i + 15)) {
                tokens.add("count(children)");
                i += 15;
                continue;
            }

            // Handle function calls like "contains(...)"
            if (input.regionMatches(true, i, "contains(", 0, 9)) {
                tokens.add("contains(");
                i += 9;  // Skip "contains("
                int parenCount = 1;  // Count parentheses to correctly balance them
                StringBuilder content = new StringBuilder();
                while (parenCount > 0 && i < input.length()) {
                    char c = input.charAt(i);
                    if (c == '(') parenCount++;
                    if (c == ')') parenCount--;
                    content.append(c);
                    i++;
                }
                tokens.add(content.toString().trim());
                continue;
            }

            // Handle name("...")
            if (input.regionMatches(true, i, "name(", 0, 5)) {
                int start = i;
                i += 5;
                if (i >= input.length() || input.charAt(i) != '"') {
                    throw new RuntimeException("Expected opening quote after 'name(' at index " + start);
                }
                i++; // Skip opening quote
                StringBuilder keyword = new StringBuilder();
                while (i < input.length()) {
                    if (input.charAt(i) == '"') {
                        i++; // Skip closing quote
                        break;
                    }
                    keyword.append(input.charAt(i));
                    i++;
                }
                if (i >= input.length() || input.charAt(i) != ')') {
                    throw new RuntimeException("Expected closing ')' for name() starting at index " + start);
                }
                i++; // Skip closing paren
                tokens.add("name(\"" + keyword + "\")");
                continue;
            }

            // Handle child-of("...")
            if (input.regionMatches(true, i, "child-of(", 0, 9)) {
                int start = i;
                i += 9;
                if (i >= input.length() || input.charAt(i) != '"') {
                    throw new RuntimeException("Expected opening quote after 'child-of(' at index " + start);
                }
                i++; // Skip opening quote
                StringBuilder keyword = new StringBuilder();
                while (i < input.length()) {
                    if (input.charAt(i) == '"') {
                        i++; // Skip closing quote
                        break;
                    }
                    keyword.append(input.charAt(i));
                    i++;
                }
                if (i >= input.length() || input.charAt(i) != ')') {
                    throw new RuntimeException("Expected closing ')' for child-of() starting at index " + start);
                }
                i++; // Skip closing paren
                tokens.add("child-of(\"" + keyword + "\")");
                continue;
            }

            // Handle operators like '=', '<', '>', '<=', '>=', '!='
            if (i + 1 < input.length()) {
                // Handle two-character operators first (<=, >=, !=)
                if (input.charAt(i) == '<' && input.charAt(i + 1) == '=') {
                    tokens.add("<=");
                    i += 2;
                    continue;
                } else if (input.charAt(i) == '>' && input.charAt(i + 1) == '=') {
                    tokens.add(">=");
                    i += 2;
                    continue;
                } else if (input.charAt(i) == '!' && input.charAt(i + 1) == '=') {
                    tokens.add("!=");
                    i += 2;
                    continue;
                }
            }

            // Handle single-character operators
            if (input.charAt(i) == '=' || input.charAt(i) == '<' || input.charAt(i) == '>' || input.charAt(i) == '!') {
                tokens.add(String.valueOf(input.charAt(i)));
                i++;
                continue;
            }

            // Handle numeric values
            if (Character.isDigit(ch)) {
                StringBuilder number = new StringBuilder();
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    number.append(input.charAt(i));
                    i++;
                }
                tokens.add(number.toString());
                continue;
            }

            // Handle unexpected characters
            throw new RuntimeException("Unexpected character at index " + i + ": '" + ch + "'");
        }
        return tokens;
    }

    private static boolean isDelimiter(String input, int index) {
        return index >= input.length() || !Character.isLetterOrDigit(input.charAt(index));
    }

    static class StringDSLRecord implements DSLRecord {
        private final String parent;
        private final String content;
        private final int childrenCount;

        StringDSLRecord(String parent, String content, int childrenCount) {
            this.parent = parent;
            this.content = content.toLowerCase();
            this.childrenCount = childrenCount;
        }

        @Override
        public boolean contains(String keyword) {
            return content.contains(keyword.toLowerCase());
        }

        @Override
        public boolean isChildOf(String keyword) {
            return parent.contains(keyword);
        }

        @Override
        public int getChildrenCount() {
            return childrenCount;
        }

        @Override
        public String name() {
            return this.content;
        }
    }
}
