package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Whether a global type is declared once in a schema: the same code list has a manifest in each release, and the
 * type is named after the code list, not after the manifest.
 */
class BieXMLGenerateExpressionGlobalTypeDeclarationTest {

    private static final Namespace XSD = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    private final GenerationContext generationContext = mock(GenerationContext.class);
    private final Element schemaNode = new Element("schema", XSD);
    private BieXMLGenerateExpression generator;

    @BeforeEach
    void setUp() {
        generator = new BieXMLGenerateExpression();
        generator.afterPropertiesSet();
        ReflectionTestUtils.setField(generator, "generationContext", generationContext);
        ReflectionTestUtils.setField(generator, "option", new GenerateExpressionOption());
        ReflectionTestUtils.setField(generator, "schemaNode", schemaNode);
    }

    private CodeListSummaryRecord codeList(long manifestId, String name, List<String> values) {
        CodeListSummaryRecord codeList = new CodeListSummaryRecord(
                new CodeListManifestId(BigInteger.valueOf(manifestId)), null, null, null, null, null,
                name, "list-id", "1", null, null, false, CcState.Published, null, null, null, List.of());
        when(generationContext.getCodeListValues(codeList)).thenReturn(values.stream()
                .map(v -> new CodeListValueSummaryRecord(null, null, null, null, v, null, null)).toList());
        return codeList;
    }

    private AgencyIdListSummaryRecord agencyIdList(long manifestId, String name, List<String> values) {
        AgencyIdListSummaryRecord agencyIdList = new AgencyIdListSummaryRecord(
                new AgencyIdListManifestId(BigInteger.valueOf(manifestId)), null, null, null, null,
                name, "list-id", "1", null, null, null, null, false, CcState.Published, null, null, null, List.of());
        List<AgencyIdListValueSummaryRecord> agencyIdListValues = values.stream().map(v -> {
            AgencyIdListValueSummaryRecord value = mock(AgencyIdListValueSummaryRecord.class);
            when(value.value()).thenReturn(v);
            return value;
        }).toList();
        when(generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.agencyIdListManifestId()))
                .thenReturn(agencyIdListValues);
        return agencyIdList;
    }

    private long simpleTypes() {
        return schemaNode.getChildren("simpleType", XSD).size();
    }

    @Test
    void theCodeListOfTwoManifestsMakingTheSameTypeIsDeclaredOnce() {
        Element first = generator.generateCodeList(codeList(1, "ConditionType", List.of("PRE", "POST", "INV")), "xsd:token");
        Element second = generator.generateCodeList(codeList(79, "ConditionType", List.of("PRE", "POST", "INV")), "xsd:token");

        assertSame(first, second);
        assertEquals(1, simpleTypes());
    }

    @Test
    void aCodeListOfTheSameNameWithOtherValuesGetsAnotherName() {
        CodeListSummaryRecord first = codeList(1, "ConditionType", List.of("PRE", "POST"));
        CodeListSummaryRecord second = codeList(79, "ConditionType", List.of("PRE", "POST", "INV"));

        String firstName = generator.getCodeListTypeName(first, null);
        String secondName = generator.getCodeListTypeName(second, null);
        generator.generateCodeList(first, "xsd:token");
        generator.generateCodeList(second, "xsd:token");

        assertNotEquals(firstName, secondName);
        assertEquals(firstName + "_2", secondName);
        assertEquals(List.of(firstName, secondName),
                schemaNode.getChildren("simpleType", XSD).stream().map(e -> e.getAttributeValue("name")).toList());
        // a type is named the same each time it is asked
        assertEquals(secondName, generator.getCodeListTypeName(second, null));
        assertEquals(firstName, generator.getCodeListTypeName(codeList(1, "ConditionType", List.of("PRE", "POST")), null));
    }

    @Test
    void theFilesOfOneRequestDoNotMakeTheSameNameForOtherContents() {
        SharedComponentRegistry registry = new SharedComponentRegistry(name -> name);
        when(generationContext.getSharedComponentRegistry()).thenReturn(registry);
        CodeListSummaryRecord first = codeList(1, "ConditionType", List.of("PRE", "POST"));
        CodeListSummaryRecord second = codeList(79, "ConditionType", List.of("PRE", "POST", "INV"));

        String nameInFirstFile = generator.getCodeListTypeName(first, null);

        // another file of the request, generated by an expression of its own
        BieXMLGenerateExpression otherGenerator = new BieXMLGenerateExpression();
        otherGenerator.afterPropertiesSet();
        ReflectionTestUtils.setField(otherGenerator, "generationContext", generationContext);
        ReflectionTestUtils.setField(otherGenerator, "option", new GenerateExpressionOption());
        ReflectionTestUtils.setField(otherGenerator, "schemaNode", new Element("schema", XSD));

        assertEquals(nameInFirstFile, otherGenerator.getCodeListTypeName(first, null));
        assertEquals(nameInFirstFile + "_2", otherGenerator.getCodeListTypeName(second, null));
    }

    @Test
    void codeListsOfOtherNamesAreDeclaredSeparately() {
        generator.generateCodeList(codeList(1, "ConditionType", List.of("PRE")), "xsd:token");
        generator.generateCodeList(codeList(2, "StatusType", List.of("PRE")), "xsd:token");

        assertEquals(2, simpleTypes());
    }

    @Test
    void theAgencyIdListOfTwoManifestsMakingTheSameTypeIsDeclaredOnce() {
        AgencyIdListSummaryRecord first = agencyIdList(1, "Agency", List.of("6", "9"));
        AgencyIdListSummaryRecord second = agencyIdList(79, "Agency", List.of("6", "9"));
        String typeName = Helper.getAgencyListTypeName(first, null);

        generator.generateAgencyList(first, typeName);
        generator.generateAgencyList(second, typeName);

        assertEquals(1, simpleTypes());
    }
}
