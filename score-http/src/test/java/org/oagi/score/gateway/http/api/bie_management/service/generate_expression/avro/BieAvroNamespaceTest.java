package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.avro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The Avro namespace comes from the namespace URI, also when the URI has no host, as a URN. */
class BieAvroNamespaceTest {

    @Test
    void reversesTheHostOfAHierarchicalUri() {
        assertEquals("org.oagi.example", BieAvroGenerateExpression.avroNamespace("http://example.oagi.org/ns"));
    }

    @Test
    void stripsOnlyALeadingWww() {
        assertEquals("com.example", BieAvroGenerateExpression.avroNamespace("https://www.example.com/x"));
        assertEquals("com.example.awww", BieAvroGenerateExpression.avroNamespace("https://awww.example.com"));
    }

    @Test
    void makesEachHostTokenAValidAvroName() {
        assertEquals("com.example.my_corp", BieAvroGenerateExpression.avroNamespace("https://my-corp.example.com/ns"));
        assertEquals("_1._0._168._192", BieAvroGenerateExpression.avroNamespace("http://192.168.0.1/ns"));
    }

    @Test
    void doesNotFailForAUrn() {
        assertEquals("cidx.names.specification.ces.schema.all._7_1",
                BieAvroGenerateExpression.avroNamespace("urn:cidx:names:specification:ces:schema:all:7.1"));
        assertEquals("un.unece.uncefact.data.standard.ReusableAggregateBusinessInformationEntity._100",
                BieAvroGenerateExpression.avroNamespace("urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"));
    }

    @Test
    void givesNoNamespaceWhenNothingUsableRemains() {
        assertNull(BieAvroGenerateExpression.avroNamespace("urn:::"));
    }
}
