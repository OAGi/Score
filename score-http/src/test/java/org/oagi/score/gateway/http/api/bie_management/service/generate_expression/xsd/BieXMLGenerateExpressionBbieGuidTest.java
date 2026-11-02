package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.xsd;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.PrimitiveRestriction;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.GenerationContext;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The id of a BBIE in a schema made with the BIE GUID option is the GUID of the BBIE, also when the BBIE is
 * restricted by a code list or an agency ID list.
 */
class BieXMLGenerateExpressionBbieGuidTest {

    private static final Namespace XSD = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
    private static final String BBIE_GUID = "0123456789abcdef0123456789abcdef";

    private final GenerationContext generationContext = mock(GenerationContext.class, RETURNS_DEEP_STUBS);
    private BieXMLGenerateExpression generator;

    @BeforeEach
    void setUp() {
        when(generationContext.getSharedComponentRegistry()).thenReturn(null);
        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setBieGuid(true);
        generator = new BieXMLGenerateExpression();
        generator.afterPropertiesSet();
        ReflectionTestUtils.setField(generator, "generationContext", generationContext);
        ReflectionTestUtils.setField(generator, "option", option);
        ReflectionTestUtils.setField(generator, "schemaNode", new Element("schema", XSD));
    }

    private BbieSummaryRecord bbie(PrimitiveRestriction restriction, EntityType entityType) {
        BbieSummaryRecord bbie = mock(BbieSummaryRecord.class, RETURNS_DEEP_STUBS);
        when(bbie.getGuid()).thenReturn(new Guid(BBIE_GUID));
        when(bbie.cardinality()).thenReturn(new Cardinality(0, 1));
        when(bbie.primitiveRestriction()).thenReturn(restriction);
        BccSummaryRecord bcc = mock(BccSummaryRecord.class, RETURNS_DEEP_STUBS);
        when(bcc.den()).thenReturn("Parent. Thing");
        when(bcc.entityType()).thenReturn(entityType);
        when(generationContext.queryBasedBCC(bbie)).thenReturn(bcc);
        return bbie;
    }

    private Element generateWithCodeList(EntityType entityType) {
        CodeListSummaryRecord codeList = mock(CodeListSummaryRecord.class, RETURNS_DEEP_STUBS);
        when(generationContext.getCodeList(any(org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId.class)))
                .thenReturn(codeList);
        PrimitiveRestriction restriction = new PrimitiveRestriction(null,
                mock(org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId.class), null);
        return generate(bbie(restriction, entityType), entityType);
    }

    private Element generateWithAgencyIdList(EntityType entityType) {
        AgencyIdListSummaryRecord agencyIdList = mock(AgencyIdListSummaryRecord.class, RETURNS_DEEP_STUBS);
        when(generationContext.getCodeList(any(org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId.class)))
                .thenReturn(null);
        when(generationContext.getCodeList((org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId) null))
                .thenReturn(null);
        when(generationContext.getAgencyIdList(any(org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId.class)))
                .thenReturn(agencyIdList);
        PrimitiveRestriction restriction = new PrimitiveRestriction(null, null,
                mock(org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId.class));
        BbieSummaryRecord bbie = bbie(restriction, entityType);
        when(generationContext.getAgencyIdList(bbie)).thenReturn(agencyIdList);
        return generate(bbie, entityType);
    }

    private Element generate(BbieSummaryRecord bbie, EntityType entityType) {
        Element parent = new Element(entityType == EntityType.Element ? "sequence" : "complexType", XSD);
        generator.generateBBIE(bbie, mock(org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord.class, RETURNS_DEEP_STUBS), parent);
        return parent.getChildren().get(0);
    }

    @Test
    void anElementBbieOfACodeListKeepsTheBbieGuid() {
        assertEquals("_" + BBIE_GUID, generateWithCodeList(EntityType.Element).getAttributeValue("id"));
    }

    @Test
    void anElementBbieOfAnAgencyIdListKeepsTheBbieGuid() {
        assertEquals("_" + BBIE_GUID, generateWithAgencyIdList(EntityType.Element).getAttributeValue("id"));
    }

    @Test
    void anAttributeBbieOfACodeListKeepsTheBbieGuid() {
        assertEquals("_" + BBIE_GUID, generateWithCodeList(EntityType.Attribute).getAttributeValue("id"));
    }

    @Test
    void anAttributeBbieOfAnAgencyIdListKeepsTheBbieGuid() {
        assertEquals("_" + BBIE_GUID, generateWithAgencyIdList(EntityType.Attribute).getAttributeValue("id"));
    }
}
