package org.oagi.score.gateway.http.api.bie_management.model.bie_search;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchAttribute;
import org.oagi.score.gateway.http.api.cc_management.model.cc_search.CcSearchRequest;
import org.oagi.score.gateway.http.common.model.TextSearchOperator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** What a search names by text on the wire (an attribute, a table) and what it becomes in the server. */
class BieSearchJsonTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void aRequestNamesTheAttributeAsTheSearchBarWritesIt() throws Exception {
        BieSearchRequest request = mapper.readValue("""
                {"predicates": [
                  {"id": "0", "attribute": "definition", "operator": "CONTAINS", "value": "x", "caseSensitive": false},
                  {"id": "1", "attribute": "Fixed_Value", "operator": "EQUALS", "value": "y", "caseSensitive": true}]}
                """, BieSearchRequest.class);

        assertEquals(List.of(BieSearchAttribute.DEFINITION, BieSearchAttribute.FIXED_VALUE),
                request.predicates().stream().map(BieSearchPredicate::attribute).toList());
        assertEquals(TextSearchOperator.EQUALS, request.predicates().get(1).operator());
    }

    @Test
    void aCoreComponentRequestNamesTheAttributeTheSameWay() throws Exception {
        CcSearchRequest request = mapper.readValue("""
                {"asccpManifestId": 7, "predicates": [
                  {"id": "0", "attribute": "component_definition", "operator": "STARTS_WITH", "value": "x", "caseSensitive": false}]}
                """, CcSearchRequest.class);

        assertEquals(CcSearchAttribute.COMPONENT_DEFINITION, request.predicates().get(0).attribute());
    }

    @Test
    void anAttributeThatIsNotSearchedIsRefusedWhenTheRequestIsRead() {
        assertThrows(JacksonException.class, () -> mapper.readValue("""
                {"predicates": [{"id": "0", "attribute": "password", "operator": "EQUALS", "value": "x", "caseSensitive": false}]}
                """, BieSearchRequest.class));
    }

    @Test
    void theAnswerNamesAPlaceByTheTableOfItsRowInLowerCase() throws Exception {
        String json = mapper.writeValueAsString(new BieSearchResponse(
                Map.of("0", List.of(new BieSearchNodeKey(BieSearchTable.BBIE_SC, "hash", "a>b")))));

        assertEquals("{\"matches\":{\"0\":[{\"table\":\"bbie_sc\",\"hashPath\":\"hash\",\"path\":\"a>b\"}]}}", json);
    }
}
