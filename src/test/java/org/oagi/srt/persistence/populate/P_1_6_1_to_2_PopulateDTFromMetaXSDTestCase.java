package org.oagi.srt.persistence.populate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.BusinessDataTypePrimitiveRestriction;
import org.oagi.srt.repository.entity.CoreDataTypeAllowedPrimitiveExpressionTypeMap;
import org.oagi.srt.repository.entity.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class P_1_6_1_to_2_PopulateDTFromMetaXSDTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private UserRepository userRepository;

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

        DataType defaultTextBDT = dtRepository.findOneByDen("Text_0F0ZX1. Type");
        assertNotNull(defaultTextBDT);

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
            assertEquals(0, actualDataType.getReleaseId());
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
        List<String> targetGuids = Arrays.asList(
                "oagis-id-0b8c80e1ef33491eb13c2a2de2db5d77",
                "oagis-id-c7fd61d128ab4eb5b44e1607359e6510",
                "oagis-id-9d98ebc34cf543d68b438e5321d21696");

        DataType defaultTextBDT = dtRepository.findOneByDen("Text_0F0ZX1. Type");
        assertNotNull(defaultTextBDT);
        List<BusinessDataTypePrimitiveRestriction> basedBdtPriRestri =
                bdtPriRestriRepository.findByBdtId(defaultTextBDT.getDtId());
        assertEquals(3, basedBdtPriRestri.size());

        Map<Integer, BusinessDataTypePrimitiveRestriction> bdtPriRestriListMap = basedBdtPriRestri.stream()
                .collect(Collectors.toMap(
                        BusinessDataTypePrimitiveRestriction::getCdtAwdPriXpsTypeMapId,
                        Function.identity()));

        List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> targetCdtAwdPriXpsTypeMaps =
                cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriXpsTypeMapIdIn(bdtPriRestriListMap.keySet());
        assertEquals(3, targetCdtAwdPriXpsTypeMaps.size());

        List<ExpectedBusinessDataTypePrimitiveRestriction> expectedBdtPriRestriList =
                targetGuids.stream().map(guid ->
                        targetCdtAwdPriXpsTypeMaps.stream().map(cdtAwdPriXpsTypeMap ->
                                new ExpectedBusinessDataTypePrimitiveRestriction(
                                        dtRepository.findOneByGuid(guid).getDtId(),
                                        cdtAwdPriXpsTypeMap.getCdtAwdPriXpsTypeMapId(),
                                        bdtPriRestriListMap.get(cdtAwdPriXpsTypeMap.getCdtAwdPriXpsTypeMapId()).isDefault()
                                )).collect(Collectors.toList())
                ).flatMap(e -> e.stream()).collect(Collectors.toList());
        assertEquals(9, expectedBdtPriRestriList.size());

        expectedBdtPriRestriList.forEach(expectedBdtPriRestri -> {
            BusinessDataTypePrimitiveRestriction actualBdtPriRestri =
                    bdtPriRestriRepository.findOneByBdtIdAndCdtAwdPriXpsTypeMapId(
                            expectedBdtPriRestri.getBdtId(),
                            expectedBdtPriRestri.getCdtAwdPriXpsTypeMapId()
                    );
            assertNotNull(actualBdtPriRestri);
            assertEquals(expectedBdtPriRestri.isDefault(), actualBdtPriRestri.isDefault());
        });
    }

    @Test
    public void test_PopulateSCinThe_dt_sc_Table() {

    }

    @Test
    public void test_Populate_cdt_sc_awd_pri_Table() {

    }

    @Test
    public void test_Populate_cdt_sc_awd_pri_xps_type_map_Table() {

    }

    @Test
    public void test_PopulateThe_bdt_sc_pri_restri_Table() {

    }
}
