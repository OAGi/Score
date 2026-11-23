package org.oagi.score.gateway.http.api.oas_management.repository.jooq;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocChangeSet.HeaderKey;
import org.oagi.score.gateway.http.api.oas_management.model.OasDocHttpHeader;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** What is left of the entries of a document when what the user deleted is taken out and what was added or changed is put in. */
class OasDocHttpHeaderStoreMergeTest {

    private static OasDocHttpHeader entry(Integer operation, OasDocHttpHeader.Direction direction, String in, String name,
                                          String description) {
        OasDocHttpHeader header = new OasDocHttpHeader();
        header.setOasOperationId(operation == null ? null : BigInteger.valueOf(operation));
        header.setDirection(direction);
        header.setIn(in);
        header.setName(name);
        header.setDescription(description);
        return header;
    }

    private static OasDocHttpHeader query(int operation, String name, String description) {
        return entry(operation, OasDocHttpHeader.Direction.REQUEST, "query", name, description);
    }

    private static String names(List<OasDocHttpHeader> headers) {
        return headers.stream().map(h -> h.getOasOperationId() + ":" + h.getName() + "=" + h.getDescription())
                .reduce((a, b) -> a + " " + b).orElse("");
    }

    @Test
    void aDeletedEntryGoesAndTheOthersStay() {
        var current = List.of(query(1, "a", "x"), query(1, "b", "x"));

        var merged = OasDocHttpHeaderStore.merge(current, List.of(), List.of(HeaderKey.of(current.get(0))));

        assertEquals("1:b=x", names(merged));
    }

    @Test
    void aChangedEntryTakesThePlaceOfTheOneWithItsKey() {
        var current = List.of(query(1, "a", "x"), query(1, "b", "x"));

        var merged = OasDocHttpHeaderStore.merge(current, List.of(query(1, "a", "changed")), List.of());

        assertEquals("1:a=changed 1:b=x", names(merged));
    }

    @Test
    void anAddedEntryIsAtTheEnd() {
        var merged = OasDocHttpHeaderStore.merge(List.of(query(1, "a", "x")), List.of(query(2, "a", "y")), List.of());

        assertEquals("1:a=x 2:a=y", names(merged));
    }

    @Test
    void theSameNameOfAnotherOperationOrLocationIsAnotherEntry() {
        var current = List.of(query(1, "a", "x"),
                entry(1, OasDocHttpHeader.Direction.REQUEST, "header", "a", "in a header"),
                entry(1, OasDocHttpHeader.Direction.RESPONSE, null, "a", "of the response"));

        var merged = OasDocHttpHeaderStore.merge(current, List.of(), List.of(HeaderKey.of(current.get(0))));

        assertEquals("1:a=in a header 1:a=of the response", names(merged));
    }

    @Test
    void aRenamedEntryIsARemovalAndAnAddition() {
        var current = List.of(query(1, "a", "x"));

        var merged = OasDocHttpHeaderStore.merge(current, List.of(query(1, "renamed", "x")), List.of(HeaderKey.of(current.get(0))));

        assertEquals("1:renamed=x", names(merged));
    }

    @Test
    void anEntryOfTheWholeDocumentIsNotOneOfAnOperation() {
        var current = List.of(entry(null, OasDocHttpHeader.Direction.REQUEST, "header", "traceparent", "all"));

        var merged = OasDocHttpHeaderStore.merge(current, List.of(), List.of(
                new HeaderKey(BigInteger.valueOf(1), OasDocHttpHeader.Direction.REQUEST, "header", "traceparent")));

        assertEquals("null:traceparent=all", names(merged));
    }
}
