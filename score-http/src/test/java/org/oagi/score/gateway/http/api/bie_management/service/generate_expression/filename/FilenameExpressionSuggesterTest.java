package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameExpressionSuggester.Kind;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename.FilenameExpressionSuggester.Suggestions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilenameExpressionSuggesterTest {

    private final FilenameExpressionSuggester suggester = new FilenameExpressionSuggester();

    private List<String> labels(String text, Kind kind) {
        return suggester.suggest(text, text.length(), kind).items().stream()
                .map(FilenameExpressionSuggester.Suggestion::label).toList();
    }

    private List<String> labels(String text) {
        return labels(text, Kind.SCHEMA);
    }

    @Test
    void offersExamplesWhileEmpty() {
        assertFalse(labels("").isEmpty());
        assertEquals(List.of("-{Incremental}", "~{BIE ID}"), labels("", Kind.DUPLICATE));
    }

    @Test
    void offersAPlaceholderOrGroupWhereOneMayStart() {
        assertEquals(List.of("{", "("), labels("{BIE ID}"));
        assertEquals(List.of("{", "(", ")"), labels("(-{BIE ID}"));
        assertEquals(List.of(), labels("abc"));
    }

    @Test
    void aDuplicateHandlerOffersOnlyItsOwnPlaceholders() {
        assertEquals(List.of("{"), labels("-", Kind.DUPLICATE));
        assertEquals(List.of("Incremental", "BIE ID"), labels("-{", Kind.DUPLICATE));
        assertEquals(List.of("}"), labels("{BIE ID", Kind.DUPLICATE));
    }

    @Test
    void filtersNamesByThePrefixAndReplacesTheWord() {
        Suggestions s = suggester.suggest("{BIE Pro", 8, Kind.SCHEMA);
        assertEquals(List.of("BIE Property Term"), s.items().stream().map(i -> i.label()).toList());
        assertEquals(1, s.from());
        assertEquals(8, s.to());
    }

    @Test
    void packagePlaceholdersAreOfferedOnlyForAPackage() {
        assertEquals(List.of(), labels("{BIE Package"));
        assertEquals(List.of("BIE Package Name", "BIE Package Version ID", "BIE Package Version Name"),
                labels("{BIE Package", Kind.PACKAGE));
    }

    @Test
    void afterACompleteNameOffersToCloseFlagOrModify() {
        assertEquals(List.of("}", "?", ":"), labels("{BIE Version"));
        assertEquals(List.of("}", "?", ":"), labels("{Business Context Name[3]"));
    }

    @Test
    void offersFlagsAfterAQuestionMark() {
        assertEquals(List.of("includeBusinessContext", "includeVersion"), labels("{BIE Version?"));
        assertEquals(List.of("includeVersion"), labels("{BIE Version?includeV"));
        assertEquals(List.of(":", "}"), labels("{BIE Version?includeVersion"));
    }

    @Test
    void offersModifiersAfterAColonWithTheCaretBetweenTheQuotes() {
        Suggestions s = suggester.suggest("{BIE Version:", 13, Kind.SCHEMA);
        assertEquals(List.of("separator('')", "replace('', '')"), s.items().stream().map(i -> i.label()).toList());
        assertEquals(2, s.items().get(0).caretBack());
        assertEquals(List.of("separator('')"), labels("{BIE Version:sep"));
    }

    @Test
    void offersTheNextStepAfterACompleteModifier() {
        assertEquals(List.of(":", "}"), labels("{BIE Version:separator('-')"));
        assertEquals(List.of(":", "}"), labels("{BIE Version:replace('\\\\.', '_')"));
    }

    @Test
    void offersNothingInsideAQuotedArgument() {
        assertEquals(List.of(), labels("{BIE Version:separator('"));
        assertEquals(List.of(), labels("{BIE Version:replace('a', '"));
    }

    @Test
    void offersPlaceholdersAgainAfterThePlaceholderIsClosed() {
        assertEquals(List.of("{", "("), labels("{BIE Version:separator('-')}-"));
    }

    /** Whatever the suggester offers as a name, the validator must accept: they share one catalog. */
    @Test
    void everyOfferedNameIsAcceptedByTheValidator() {
        FilenameExpressionValidationService validator = new FilenameExpressionValidationService();
        for (String name : labels("{", Kind.PACKAGE)) {
            validator.validateBiePackageSchemaExpression("{" + name + "}", "");
        }
        for (String name : labels("{", Kind.DUPLICATE)) {
            validator.validateBieSchemaExpression("{BIE ID}", "{" + name + "}");
        }
    }
}
