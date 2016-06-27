package org.oagi.srt.persistence.populate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImportApplication.class)
public class P_1_6_1_to_2_PopulateDTFromMetaXSDTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReleaseRepository releaseRepository;

    private DataType defaultTextBDT;

    private List<String> targetGuids;

    private DataTypeSupplementaryComponent dtScOfTextDefaultBDT;

    private DataType targetTextCDT;
    private List<CoreDataTypeAllowedPrimitive> targetTextCDTAwdPriList;
    private List<CoreDataTypePrimitive> targetTextCDTPriList;
    private List<BusinessDataTypePrimitiveRestriction> defaultTextBDTPriList;
    private CoreDataTypePrimitive defaultTextBDTCDTPrimitive;
    private CoreDataTypeAllowedPrimitive defaultTextBDTCDTAwdPri;
    private CoreDataTypeAllowedPrimitiveExpressionTypeMap defaultTextBDTCDTAwdPriXpsTypeMap;

    @Before
    public void setUp() {
        defaultTextBDT = dtRepository.findOneByTypeAndDen(1, "Text_0F0ZX1. Type");
        assertNotNull(defaultTextBDT);

        targetTextCDT = dtRepository.findOneByTypeAndDen(0, "Text. Type");
        assertNotNull(targetTextCDT);

        assertEquals(defaultTextBDT.getBasedDtId(), targetTextCDT.getDtId());

        targetTextCDTAwdPriList = cdtAwdPriRepository.findByCdtId(targetTextCDT.getDtId());
        assertEquals(3, targetTextCDTAwdPriList.size());

        targetTextCDTPriList = cdtPriRepository.findByCdtPriIdIn(
                targetTextCDTAwdPriList.stream()
                        .mapToInt(CoreDataTypeAllowedPrimitive::getCdtPriId)
                        .boxed()
                        .collect(Collectors.toList())
        );
        
        defaultTextBDTPriList = bdtPriRestriRepository.findByBdtId(defaultTextBDT.getDtId());
        assertEquals(1, defaultTextBDTPriList.size());
        
        defaultTextBDTCDTAwdPriXpsTypeMap = cdtAwdPriXpsTypeMapRepository.findOne(defaultTextBDTPriList.get(0).getCdtAwdPriXpsTypeMapId());
        
        defaultTextBDTCDTAwdPri = cdtAwdPriRepository.findOne(defaultTextBDTCDTAwdPriXpsTypeMap.getCdtAwdPriId());
        
        defaultTextBDTCDTPrimitive = cdtPriRepository.findOne(defaultTextBDTCDTAwdPri.getCdtPriId());
        
        assertEquals("Token",defaultTextBDTCDTPrimitive.getName());

        List<String> targetTextCDTPriNameList =
                targetTextCDTPriList.stream().map(CoreDataTypePrimitive::getName).collect(Collectors.toList());

        assertTrue(targetTextCDTPriNameList.contains("NormalizedString"));
        assertTrue(targetTextCDTPriNameList.contains("String"));
        assertTrue(targetTextCDTPriNameList.contains("Token"));

        targetGuids = Arrays.asList(
                "oagis-id-0b8c80e1ef33491eb13c2a2de2db5d77",
                "oagis-id-c7fd61d128ab4eb5b44e1607359e6510",
                "oagis-id-9d98ebc34cf543d68b438e5321d21696");

        dtScOfTextDefaultBDT = dtScRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(
                defaultTextBDT.getDtId(), "Language", "Code");
        assertNotNull(dtScOfTextDefaultBDT);
    }

    private class ExpectedDataType {
        private String guid;
        private String den;
        private String definition;

        public ExpectedDataType(String guid, String den, String definition) {
            this.guid = guid;
            this.den = den;
            this.definition = definition;
        }

        public String getGuid() {
            return guid;
        }

        public String getDen() {
            return den;
        }

        public String getDefinition() {
            return definition;
        }
    }

    @Test
    public void test_PopulateThe_dt_Table() {
        List<ExpectedDataType> expectedDataTypeList = Arrays.asList(
                new ExpectedDataType("oagis-id-0b8c80e1ef33491eb13c2a2de2db5d77", "Expression. Type", "ReturnCriteria identifies the content that is to be returned, given query success. In essence, the expression here has the effect of filtering the part(s) of the found element(s) that are to be returned.\n" +
                        "\n" +
                        "ReturnCriteria plays no role in the query itself. That is handled as a match against the request BOD's noun exemplar. \n" +
                        "\n" +
                        "ReturnCriteria allows the sender of the BOD to indicate which information (down to the field level) is requested to be returned, given that the query has been successful in matching the exemplar to existing nouns. \n" +
                        "\n" +
                        "That is, in a GetListPurchaseOrder, if one or more PurchaseOrders with a TotalPrice = $1M were found, ReturnCriteria tells the BOD recipient which parts of the PurchaseOrder should be populated with content when the response (ShowPurchaseOrder) is formulated.\n" +
                        "\n" +
                        "The expressionLanguage indicates the expression language being used. In order for the ReturnCriteria expression to be evaluable by the BOD recipient, the recipient must be capable of processing and interpreting the specified expression language. XPath is the default, due to its ubiquity among XML processing technologies. "),
                new ExpectedDataType("oagis-id-c7fd61d128ab4eb5b44e1607359e6510", "Action Expression. Type", "ReturnCriteria identifies the content that is to be returned, given query success. In essence, the expression here has the effect of filtering the part(s) of the found element(s) that are to be returned.\n" +
                        "\n" +
                        "ReturnCriteria plays no role in the query itself. That is handled as a match against the request BOD's noun exemplar. \n" +
                        "\n" +
                        "ReturnCriteria allows the sender of the BOD to indicate which information (down to the field level) is requested to be returned, given that the query has been successful in matching the exemplar to existing nouns. \n" +
                        "\n" +
                        "That is, in a GetListPurchaseOrder, if one or more PurchaseOrders with a TotalPrice = $1M were found, ReturnCriteria tells the BOD recipient which parts of the PurchaseOrder should be populated with content when the response (ShowPurchaseOrder) is formulated.\n" +
                        "\n" +
                        "The expressionLanguage indicates the expression language being used. In order for the ReturnCriteria expression to be evaluable by the BOD recipient, the recipient must be capable of processing and interpreting the specified expression language. XPath is the default, due to its ubiquity among XML processing technologies. "),
                new ExpectedDataType("oagis-id-9d98ebc34cf543d68b438e5321d21696", "Response Expression. Type", "ReturnCriteria identifies the content that is to be returned, given query success. In essence, the expression here has the effect of filtering the part(s) of the found element(s) that are to be returned.\n" +
                        "\n" +
                        "ReturnCriteria plays no role in the query itself. That is handled as a match against the request BOD's noun exemplar. \n" +
                        "\n" +
                        "ReturnCriteria allows the sender of the BOD to indicate which information (down to the field level) is requested to be returned, given that the query has been successful in matching the exemplar to existing nouns. \n" +
                        "\n" +
                        "That is, in a GetListPurchaseOrder, if one or more PurchaseOrders with a TotalPrice = $1M were found, ReturnCriteria tells the BOD recipient which parts of the PurchaseOrder should be populated with content when the response (ShowPurchaseOrder) is formulated.\n" +
                        "\n" +
                        "The expressionLanguage indicates the expression language being used. In order for the ReturnCriteria expression to be evaluable by the BOD recipient, the recipient must be capable of processing and interpreting the specified expression language. XPath is the default, due to its ubiquity among XML processing technologies. ")
        );

        expectedDataTypeList.forEach(expectedDataType -> {
            DataType actualDataType = dtRepository.findOneByGuid(expectedDataType.getGuid());
            assertNotNull(actualDataType);

            assertEquals(expectedDataType.getDen(), actualDataType.getDen());
            assertEquals(expectedDataType.getDefinition(), actualDataType.getDefinition());
            assertEquals(expectedDataType.getDen().replace(". Type", "") + ". Content",
                    actualDataType.getContentComponentDen());
            assertEquals(1, actualDataType.getType());
            assertEquals("1.0", actualDataType.getVersionNum());
            assertEquals(null, actualDataType.getQualifier());
            assertEquals(defaultTextBDT.getDtId(), actualDataType.getBasedDtId());
            assertEquals(null, actualDataType.getContentComponentDefinition());
            assertEquals(null, actualDataType.getRevisionDoc());
            assertEquals(3, actualDataType.getState());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getCreatedBy());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getLastUpdatedBy());
            assertEquals(0, actualDataType.getRevisionNum());
            assertEquals(0, actualDataType.getRevisionTrackingNum());
            assertEquals(0, actualDataType.getRevisionAction());
            assertEquals(releaseRepository.findReleaseIdByReleaseNum("10.1"), actualDataType.getReleaseId());
            assertEquals(0, actualDataType.getCurrentBdtId());
            assertEquals(false, actualDataType.isDeprecated());
        });
    }

    private class ExpectedBusinessDataTypePrimitiveRestriction {
        private int bdtId;
        private int cdtAwdPriXpsTypeMapId;
        private boolean isDefault;

        public ExpectedBusinessDataTypePrimitiveRestriction(
                int bdtId, int cdtAwdPriXpsTypeMapId, boolean isDefault) {
            this.bdtId = bdtId;
            this.cdtAwdPriXpsTypeMapId = cdtAwdPriXpsTypeMapId;
            this.isDefault = isDefault;
        }

        public int getBdtId() {
            return bdtId;
        }

        public int getCdtAwdPriXpsTypeMapId() {
            return cdtAwdPriXpsTypeMapId;
        }

        public boolean isDefault() {
            return isDefault;
        }
    }

    @Test
    public void test_Populate_bdt_pri_restri_Table() {
        List<BusinessDataTypePrimitiveRestriction> basedBdtPriRestri =
                bdtPriRestriRepository.findByBdtId(defaultTextBDT.getDtId());
        assertEquals(1, basedBdtPriRestri.size());

        Map<Integer, BusinessDataTypePrimitiveRestriction> bdtPriRestriListMap = basedBdtPriRestri.stream()
                .collect(Collectors.toMap(
                        BusinessDataTypePrimitiveRestriction::getCdtAwdPriXpsTypeMapId,
                        Function.identity()));

        List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> targetCdtAwdPriXpsTypeMaps =
                cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriXpsTypeMapIdIn(bdtPriRestriListMap.keySet());
        assertEquals(1, targetCdtAwdPriXpsTypeMaps.size());

        List<ExpectedBusinessDataTypePrimitiveRestriction> expectedBdtPriRestriList =
                targetGuids.stream().map(guid ->
                        targetCdtAwdPriXpsTypeMaps.stream().map(cdtAwdPriXpsTypeMap ->
                                new ExpectedBusinessDataTypePrimitiveRestriction(
                                        dtRepository.findOneByGuid(guid).getDtId(),
                                        cdtAwdPriXpsTypeMap.getCdtAwdPriXpsTypeMapId(),
                                        bdtPriRestriListMap.get(cdtAwdPriXpsTypeMap.getCdtAwdPriXpsTypeMapId()).isDefault()
                                )).collect(Collectors.toList())
                ).flatMap(e -> e.stream()).collect(Collectors.toList());
        assertEquals(targetGuids.size() * defaultTextBDTPriList.size(), expectedBdtPriRestriList.size());

        expectedBdtPriRestriList.forEach(expectedBdtPriRestri -> {
            BusinessDataTypePrimitiveRestriction actualBdtPriRestri =
                    bdtPriRestriRepository.findOneByBdtIdAndCdtAwdPriXpsTypeMapId(
                            expectedBdtPriRestri.getBdtId(),
                            expectedBdtPriRestri.getCdtAwdPriXpsTypeMapId()
                    );
            assertNotNull(actualBdtPriRestri);
            assertEquals(expectedBdtPriRestri.isDefault(), actualBdtPriRestri.isDefault());

            CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                    cdtAwdPriXpsTypeMapRepository.findOne(actualBdtPriRestri.getCdtAwdPriXpsTypeMapId());
            XSDBuiltInType xbt = xbtRepository.findOne(cdtAwdPriXpsTypeMap.getXbtId());

            if (actualBdtPriRestri.isDefault()) {
                assertEquals("xsd:token", xbt.getBuiltInType());
            } else {
                assertTrue(Arrays.asList("xsd:normalizedString", "xsd:string").contains(xbt.getBuiltInType()));
            }
        });
    }

    @Test
    public void test_PopulateSCinThe_dt_sc_Table() {
        List<DataType> targetDataTypes = dtRepository.findByGuidIn(targetGuids);
        assertEquals(3, targetDataTypes.size());

        targetDataTypes.forEach(dataType -> {
            DataTypeSupplementaryComponent actualLanguageCodeDtSc =
                    dtScRepository.findOneByOwnerDtIdAndBasedDtScId(dataType.getDtId(), dtScOfTextDefaultBDT.getDtScId());
            assertNotNull(actualLanguageCodeDtSc);

            assertEquals(dtScOfTextDefaultBDT.getPropertyTerm(), actualLanguageCodeDtSc.getPropertyTerm());
            assertEquals(dtScOfTextDefaultBDT.getRepresentationTerm(), actualLanguageCodeDtSc.getRepresentationTerm());
            assertEquals(dtScOfTextDefaultBDT.getDefinition(), actualLanguageCodeDtSc.getDefinition());
            assertEquals(dtScOfTextDefaultBDT.getMinCardinality(), actualLanguageCodeDtSc.getMinCardinality());
            assertEquals(0, actualLanguageCodeDtSc.getMaxCardinality());

            List<DataTypeSupplementaryComponent> actualDtScList = dtScRepository.findByOwnerDtId(dataType.getDtId());
            switch (dataType.getDen()) {
                case "Expression. Type":
                    assertEquals(2, actualDtScList.size());
                    actualDtScList.forEach(actualDtSc -> {
                        if (dtScOfTextDefaultBDT.getPropertyTerm().equals(actualDtSc.getPropertyTerm())) {
                            return;
                        }

                        assertEquals("oagis-id-99f7c6c49493417191e281586fbe9223", actualDtSc.getGuid());
                        test_PopulateSCinThe_dt_sc_Table_for_details(actualDtSc, "expressionLanguage", "optional");
                    });
                    break;
                case "Action Expression. Type":
                    assertEquals(3, actualDtScList.size());
                    actualDtScList.forEach(actualDtSc -> {
                        if (dtScOfTextDefaultBDT.getPropertyTerm().equals(actualDtSc.getPropertyTerm())) {
                            return;
                        }

                        switch(actualDtSc.getGuid()) {
                            case "oagis-id-c05ff56ed80d42d59fdeabddb5891126":
                                test_PopulateSCinThe_dt_sc_Table_for_details(actualDtSc, "actionCode", "required");
                                break;
                            case "oagis-id-314bd79fb54147c993bafd54d37101aa":
                                test_PopulateSCinThe_dt_sc_Table_for_details(actualDtSc, "expressionLanguage", "optional");
                                break;
                            default:
                                throw new AssertionError();
                        }
                    });
                    break;
                case "Response Expression. Type":
                    assertEquals(3, actualDtScList.size());
                    actualDtScList.forEach(actualDtSc -> {
                        if (dtScOfTextDefaultBDT.getPropertyTerm().equals(actualDtSc.getPropertyTerm())) {
                            return;
                        }

                        switch(actualDtSc.getGuid()) {
                            case "oagis-id-1c020ca70a2440a0b490fabaed6100a5":
                                test_PopulateSCinThe_dt_sc_Table_for_details(actualDtSc, "actionCode", "required");
                                break;
                            case "oagis-id-d3f9ec6ec1974ffd93168e4d39262ed1":
                                test_PopulateSCinThe_dt_sc_Table_for_details(actualDtSc, "expressionLanguage", "optional");
                                break;
                            default:
                                throw new AssertionError();
                        }
                    });
                    break;
                default:
                    throw new AssertionError();
            }
        });
    }

    private void test_PopulateSCinThe_dt_sc_Table_for_details(DataTypeSupplementaryComponent actualDtSc,
                                                              String expectedName, String expectedUse) {
        switch (expectedName) {
            case "expressionLanguage":
                assertEquals("Expression Language", actualDtSc.getPropertyTerm());
                assertEquals("Text", actualDtSc.getRepresentationTerm());
                break;
            case "actionCode":
                assertEquals("Action", actualDtSc.getPropertyTerm());
                assertEquals("Code", actualDtSc.getRepresentationTerm());
                break;
        }

        switch (expectedUse) {
            case "optional":
                assertEquals(0, actualDtSc.getMinCardinality());
                assertEquals(1, actualDtSc.getMaxCardinality());
                break;
            case "required":
                assertEquals(1, actualDtSc.getMinCardinality());
                assertEquals(1, actualDtSc.getMaxCardinality());
                break;
            case "prohibited":
                assertEquals(0, actualDtSc.getMinCardinality());
                assertEquals(0, actualDtSc.getMaxCardinality());
                break;
        }

        assertEquals(0, actualDtSc.getBasedDtScId());
    }

    @Test
    public void test_Populate_cdt_sc_awd_pri_Table() {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> actualCdtScAwdPriList = retrieveActualCdtScAwdPriList();
        actualCdtScAwdPriList.forEach(cdtScAwdPri -> {
        	
        	DataTypeSupplementaryComponent dtsc = dtScRepository.findOne(cdtScAwdPri.getCdtScId());
        	if(dtsc.getPropertyTerm().equals("Language") || dtsc.getPropertyTerm().equals("Action")){
        		if (cdtScAwdPri.isDefault()) {
                    assertEquals("Token", cdtPriRepository.findOne(cdtScAwdPri.getCdtPriId()).getName());
                } else {
                    assertTrue(Arrays.asList("NormalizedString", "String")
                            .contains(cdtPriRepository.findOne(cdtScAwdPri.getCdtPriId()).getName()));
                }
        	}
        	else if(dtsc.getPropertyTerm().equals("Expression Language")){
        		if (cdtScAwdPri.isDefault()) {
                    assertEquals("NormalizedString", cdtPriRepository.findOne(cdtScAwdPri.getCdtPriId()).getName());
                } else {
                    assertTrue(Arrays.asList("Token", "String")
                            .contains(cdtPriRepository.findOne(cdtScAwdPri.getCdtPriId()).getName()));
                }
        	}
        	else {
        		assertTrue(false);
        	}
            
        });
    }

    private List<CoreDataTypeSupplementaryComponentAllowedPrimitive> retrieveActualCdtScAwdPriList() {
        int expectedDtScSize = 8; // it determines from the above testing, 'test_PopulateSCinThe_dt_sc_Table'
        List<Integer> targetDtIds =
                dtRepository.findByGuidIn(targetGuids).stream()
                        .mapToInt(DataType::getDtId).boxed().collect(Collectors.toList());
        List<DataTypeSupplementaryComponent> actualDtScList = dtScRepository.findByOwnerDtIdIn(targetDtIds);
        assertEquals(expectedDtScSize, actualDtScList.size());

        /*
         * If the DataType has a based_dt_sc_id, it omits for insertion in cdt_sc_awd_pri
         */
        List<DataTypeSupplementaryComponent> targetDtScList =
                actualDtScList.stream().filter(dt -> dt.getBasedDtScId() == 0).collect(Collectors.toList());

        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> actualCdtScAwdPriList =
                cdtScAwdPriRepository.findByCdtScIdIn(
                        targetDtScList.stream()
                                .mapToInt(DataTypeSupplementaryComponent::getDtScId)
                                .boxed()
                                .collect(Collectors.toList())
                );

        int expectedCdtScAwdPriSize = targetDtScList.size() * targetTextCDTAwdPriList.size();
        assertEquals(expectedCdtScAwdPriSize, actualCdtScAwdPriList.size());

        return actualCdtScAwdPriList;
    }

    @Test
    public void test_Populate_cdt_sc_awd_pri_xps_type_map_Table() {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> actualCdtScAwdPriList = retrieveActualCdtScAwdPriList();
        List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> actualCdtScAwdPriXpsTypeMapList =
                cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriIn(
                        actualCdtScAwdPriList.stream()
                                .mapToInt(CoreDataTypeSupplementaryComponentAllowedPrimitive::getCdtScAwdPriId)
                                .boxed()
                                .collect(Collectors.toList())
                );

        List<XSDBuiltInType> actualXbtList = xbtRepository.findByXbtIdIn(
                actualCdtScAwdPriXpsTypeMapList.stream()
                        .mapToInt(CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap::getXbtId)
                        .boxed()
                        .collect(Collectors.toList())
        );
        List<String> expectedXbtBuiltInTypes = Arrays.asList("xsd:string", "xsd:normalizedString", "xsd:token");
        actualXbtList.forEach(xbt -> {
            assertTrue(expectedXbtBuiltInTypes.contains(xbt.getBuiltInType()));
        });
    }

    @Test
    public void test_PopulateThe_bdt_sc_pri_restri_Table() {
        List<DataType> targetDtList = dtRepository.findByGuidIn(targetGuids);
        List<DataTypeSupplementaryComponent> targetDtScList = dtScRepository.findByOwnerDtIdIn(
                targetDtList.stream()
                        .mapToInt(DataType::getDtId)
                        .boxed()
                        .collect(Collectors.toList())
        );
        Map<Integer, DataTypeSupplementaryComponent> targetDtScMap =
                targetDtScList.stream().collect(Collectors.toMap(DataTypeSupplementaryComponent::getDtScId, Function.identity()));

        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> actualBdtScPriRestriList =
                bdtScPriRestriRepository.findByBdtScIdIn(
                        targetDtScList.stream()
                                .mapToInt(DataTypeSupplementaryComponent::getDtScId)
                                .boxed()
                                .collect(Collectors.toList())
                );

        /*
         * 'Language Code' Part
         */
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> expectedInheritedBdtScPriRestriListForLanguageCode =
                bdtScPriRestriRepository.findByBdtScId(dtScOfTextDefaultBDT.getDtScId());

        CodeList expectedCodeListOfBdtScPriRestriForLanguageCode =
                codeListRepository.findOneByName("clm56392A20081107_LanguageCode");

        // check rows of count
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> actualBdtScPriRestriListForLanguageCode =
                actualBdtScPriRestriList.stream().filter(bdtScPriRestri -> {
                    DataTypeSupplementaryComponent actualDtSc = targetDtScMap.get(bdtScPriRestri.getBdtScId());
                    return "Language".equals(actualDtSc.getPropertyTerm());
                }).collect(Collectors.toList());
        assertEquals(
                (expectedInheritedBdtScPriRestriListForLanguageCode.size() + 1) * targetGuids.size(),
                actualBdtScPriRestriListForLanguageCode.size());

        // check rows of value
        int expectedSumValueForLanguageCode = (expectedInheritedBdtScPriRestriListForLanguageCode.stream()
                .mapToInt(bdtScPriRestri ->
                        (bdtScPriRestri.getCdtScAwdPriXpsTypeMapId() +
                                bdtScPriRestri.getCodeListId() + (bdtScPriRestri.isDefault() ? 1 : 0))
                )
                .sum() + expectedCodeListOfBdtScPriRestriForLanguageCode.getCodeListId()) * targetGuids.size();

        int actualSumValueForLanguageCode = actualBdtScPriRestriListForLanguageCode
                .stream()
                .mapToInt(bdtScPriRestri ->
                        (bdtScPriRestri.getCdtScAwdPriXpsTypeMapId() +
                                bdtScPriRestri.getCodeListId() + (bdtScPriRestri.isDefault() ? 1 : 0))
                )
                .sum();

        assertEquals(expectedSumValueForLanguageCode, actualSumValueForLanguageCode);

        expectedInheritedBdtScPriRestriListForLanguageCode.forEach(bdtScPriRestri -> {
            if (bdtScPriRestri.isDefault()) {
                assertEquals("xsd:token",
                        xbtRepository.findOne(
                                cdtScAwdPriXpsTypeMapRepository.findOne(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId()).getXbtId()
                        ).getBuiltInType());
            } else {
                assertTrue(Arrays.asList("xsd:string", "xsd:normalizedString").contains(
                        xbtRepository.findOne(
                                cdtScAwdPriXpsTypeMapRepository.findOne(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId()).getXbtId()
                        ).getBuiltInType())
                );
            }
        });

        
        /*
         * 'Expression Language' Part
         */
        
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> actualBdtScPriRestriListForExpressionLanguage =
                actualBdtScPriRestriList.stream().filter(bdtScPriRestri -> {
                    DataTypeSupplementaryComponent actualDtSc = targetDtScMap.get(bdtScPriRestri.getBdtScId());
                    return "Expression Language".equals(actualDtSc.getPropertyTerm());
                }).collect(Collectors.toList());

        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> targetCdtScAwdPriListForExpressionLanguage =
                cdtScAwdPriRepository.findByCdtScIdIn(
                        actualBdtScPriRestriListForExpressionLanguage.stream()
                                .mapToInt(BusinessDataTypeSupplementaryComponentPrimitiveRestriction::getBdtScId)
                                .boxed()
                                .collect(Collectors.toList())
                );
        List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> targetCdtScAwdPriXpsTypeMapListForExpressionLanguage =
                cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriIn(
                targetCdtScAwdPriListForExpressionLanguage.stream()
                        .mapToInt(CoreDataTypeSupplementaryComponentAllowedPrimitive::getCdtScAwdPriId)
                        .boxed()
                        .collect(Collectors.toList())
        );
        // check rows of count
        assertEquals(targetCdtScAwdPriXpsTypeMapListForExpressionLanguage.size(), actualBdtScPriRestriListForExpressionLanguage.size());

        // check rows of value
        int actualSumValueForExpressionLanguage = actualBdtScPriRestriListForExpressionLanguage.stream()
                .mapToInt(bdtScPriRestri -> (bdtScPriRestri.getCdtScAwdPriXpsTypeMapId() + bdtScPriRestri.getCodeListId()))
                .sum();
       
        int expectedSumValueForExpressionLanguage = targetCdtScAwdPriXpsTypeMapListForExpressionLanguage.stream()
                .mapToInt(cdtScAwdPriXpsTypeMap -> cdtScAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId())
                .sum() ;
        assertEquals(expectedSumValueForExpressionLanguage, actualSumValueForExpressionLanguage);
        
        actualBdtScPriRestriListForExpressionLanguage.forEach(bdtScPriRestri -> {
            if (bdtScPriRestri.isDefault()) {
                assertEquals("xsd:normalizedString",
                        xbtRepository.findOne(
                                cdtScAwdPriXpsTypeMapRepository.findOne(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId()).getXbtId()
                        ).getBuiltInType());
            } else {
                assertTrue(Arrays.asList("xsd:string", "xsd:token").contains(
                        xbtRepository.findOne(
                                cdtScAwdPriXpsTypeMapRepository.findOne(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId()).getXbtId()
                        ).getBuiltInType())
                );
            }
        });
        
        /*
         * 'Action Code' Part
         */
        CodeList expectedCodeListOfBdtScPriRestriForActionCodeForActionExpression =
                codeListRepository.findOneByName("oacl_ActionCode");
        
        CodeList expectedCodeListOfBdtScPriRestriForActionCodeForResponseActionExpression =
                codeListRepository.findOneByName("oacl_ResponseActionCode");

        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> actualBdtScPriRestriListForActionCode =
                actualBdtScPriRestriList.stream().filter(bdtScPriRestri -> {
                    DataTypeSupplementaryComponent actualDtSc = targetDtScMap.get(bdtScPriRestri.getBdtScId());
                    return "Action".equals(actualDtSc.getPropertyTerm());
                }).collect(Collectors.toList());

        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> targetCdtScAwdPriListForActionCode =
                cdtScAwdPriRepository.findByCdtScIdIn(
                        actualBdtScPriRestriListForActionCode.stream()
                                .mapToInt(BusinessDataTypeSupplementaryComponentPrimitiveRestriction::getBdtScId)
                                .boxed()
                                .collect(Collectors.toList())
                );
        List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> targetCdtScAwdPriXpsTypeMapListForActionCode =
                cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriIn(
                targetCdtScAwdPriListForActionCode.stream()
                        .mapToInt(CoreDataTypeSupplementaryComponentAllowedPrimitive::getCdtScAwdPriId)
                        .boxed()
                        .collect(Collectors.toList())
                );
        
        // check rows of count
        assertEquals(targetCdtScAwdPriXpsTypeMapListForActionCode.size()+2, actualBdtScPriRestriListForActionCode.size());

        // check rows of value
        int actualSumValueForActionCode = actualBdtScPriRestriListForActionCode.stream()
                .mapToInt(bdtScPriRestri -> (bdtScPriRestri.getCdtScAwdPriXpsTypeMapId() + bdtScPriRestri.getCodeListId()))
                .sum();
       
        int expectedSumValueForActionCode = targetCdtScAwdPriXpsTypeMapListForActionCode.stream()
                .mapToInt(cdtScAwdPriXpsTypeMap -> cdtScAwdPriXpsTypeMap.getCdtScAwdPriXpsTypeMapId())
                .sum() 
                + expectedCodeListOfBdtScPriRestriForActionCodeForActionExpression.getCodeListId() 
                + expectedCodeListOfBdtScPriRestriForActionCodeForResponseActionExpression.getCodeListId();
        
        assertEquals(expectedSumValueForActionCode, actualSumValueForActionCode);

        int expectedCodeListSum=0;
        actualBdtScPriRestriListForActionCode.forEach(bdtScPriRestri -> {
            if (bdtScPriRestri.isDefault()) {
                assertEquals("xsd:token",
                        xbtRepository.findOne(
                                cdtScAwdPriXpsTypeMapRepository.findOne(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId()).getXbtId()
                        ).getBuiltInType());
            } else if(bdtScPriRestri.getCodeListId()==0){
                assertTrue(Arrays.asList("xsd:string", "xsd:normalizedString").contains(
                        xbtRepository.findOne(
                                cdtScAwdPriXpsTypeMapRepository.findOne(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId()).getXbtId()
                        ).getBuiltInType())
                );
            } 
        });
        
    }
        
        
}
