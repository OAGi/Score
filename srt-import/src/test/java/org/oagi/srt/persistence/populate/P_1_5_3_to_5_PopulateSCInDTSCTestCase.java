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
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.oagi.srt.persistence.populate.P_1_5_1_to_2_PopulateBDTsInDTTestCase.EXPECTED_GUID_OF_BDT_LIST;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImportApplication.class)
@Transactional(readOnly = true)
public class P_1_5_3_to_5_PopulateSCInDTSCTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    private class ExpectedDataTypeSupplementaryComponent {
        private String guid;
        private String use;
        private String attributeName;
        private String definition;

        public ExpectedDataTypeSupplementaryComponent(String guid, String use, String attributeName, String definition) {
            this.guid = guid;
            this.use = use;
            this.attributeName = attributeName;
            this.definition = definition;
        }

        public String getGuid() {
            return guid;
        }

        public String getUse() {
            return use;
        }

        public String getPropertyTerm() {
            return this.attributeName.substring(0, this.attributeName.indexOf('.'));
        }

        public String getRepresentationTerm() {
            return this.attributeName.substring(this.attributeName.indexOf('.') + 1, this.attributeName.length()).trim();
        }

        public String getDefinition() {
            return definition;
        }
    }

    private Map<String, List<ExpectedDataTypeSupplementaryComponent>> predefinedDtScListForDefaultBDT;
    private Map<String, List<ExpectedDataTypeSupplementaryComponent>> additionalDtScMapForUnqualifiedBDT;

    @Before
    public void setUp() {
        predefinedDtScListForDefaultBDT = new HashMap();
        predefinedDtScListForDefaultBDT.put("Amount_0723C8. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-0dd62519460d4a91bfdbc1f7778befac", "optional", "Currency. Code", "The currency of the amount")));
        predefinedDtScListForDefaultBDT.put("Binary Object_4277E5. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-4b87790af2ea49a7a5d1f92dec29335f", "optional", "MIME. Code", "The Multipurpose Internet Mail Extensions (MIME) media type of the binary object"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-88d1b6e5f2d94dbda4d08793249cd878", "optional", "Character Set. Code", "The character set of the binary object if the Multipurpose Internet Mail Extensions (MIME) type is text"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-8ca03283b2ec469598af6bb115cf0bf2", "optional", "Filename. Name", "The filename of the binary object")));
        predefinedDtScListForDefaultBDT.put("Graphic_3FDF3D. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-deb5c8ba87004cd49e57010ad3ece3af", "optional", "MIME. Code", "The Multipurpose Internet Mail Extensions (MIME) media type of the graphic"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-318dae29eff94cbc9c172448ce54fb34", "optional", "Character Set. Code", "The character set of the graphic if the Multipurpose Internet Mail Extensions (MIME) type is text"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-600fb9b394aa44c2ad17be7ce8aa2396", "optional", "Filename. Name", "The filename of the graphic")));
        predefinedDtScListForDefaultBDT.put("Sound_697AE6. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-7b304c647a924ce7973753eecdcb5d79", "optional", "MIME. Code", "The Multipurpose Internet Mail Extensions (MIME) media type of the sound"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-8bbefdeaf55d4daab08a48f32e0f4796", "optional", "Character Set. Code", "The character set of the sound if the Multipurpose Internet Mail Extensions (MIME) type is text"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-d6688838cedb4fd7aae20626c2ef27b0", "optional", "Filename. Name", "The filename of the binary object")));
        predefinedDtScListForDefaultBDT.put("Video_539B44. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-2c6f9ff650c24bfdbd0c49ac67de13bd", "optional", "MIME. Code", "The Multipurpose Internet Mail Extensions (MIME) media type for videos"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-b558001026164520b5ddda71d6360b09", "optional", "Character Set. Code", "The character set of the video if the Multipurpose Internet Mail Extensions (MIME) type is text"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-3a765939c131448da1858fd6f1c339db", "optional", "Filename. Name", "The filename of the video")));
        predefinedDtScListForDefaultBDT.put("Code_1DEB05. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-a269304987de4f3a845be02a78df41ae", "optional", "List. Identifier", "The identification of a list of codes"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-6521de84253e4428adfd742cc8cd4603", "optional", "List Agency. Identifier", "The identification of the agency that manages the code list"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-118b994c3a7b45f3b0f4f37d184cf0a6", "optional", "List Version. Identifier", "The identification of the version of the list of codes")));
        predefinedDtScListForDefaultBDT.put("Identifier_D995CD. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-3233f3fb57c9482fb39255be78c495af", "optional", "Scheme. Identifier", "The identification of the identifier scheme"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-59ee9c5aa80641d7a0d78f5418ebcfa4", "optional", "Scheme Version. Identifier", "The identifier scheme version identifier"),
                new ExpectedDataTypeSupplementaryComponent("oagis-id-cb2ac5b98b0847e785de08756c29de85", "optional", "Scheme Agency. Identifier", "The identifier scheme Agency identifier")));
        predefinedDtScListForDefaultBDT.put("Measure_671290. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-d83591f0ee35430f95172883718499ff", "optional", "Unit. Code", "The unit of measure")));
        predefinedDtScListForDefaultBDT.put("Name_02FC2Z. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-42e59d799de147b8ab49c8a27ec85ff1", "optional", "Language. Code", "The language used in the corresponding text string")));
        predefinedDtScListForDefaultBDT.put("Quantity_201330. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-9b0fbfcf9ac244b29dc8c7281607dc90", "optional", "Unit. Code", "The unit of measure")));
        predefinedDtScListForDefaultBDT.put("Text_62S0B4. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-c8d0c7094d7d4fbeb7e50fd20a17c1b3", "optional", "Language. Code", "The language used in the corresponding text string")));

        additionalDtScMapForUnqualifiedBDT = new HashMap();
        additionalDtScMapForUnqualifiedBDT.put("Name. Type", Arrays.asList(
                new ExpectedDataTypeSupplementaryComponent("oagis-id-84fa20db74b942449e1885cff79b24df", "optional", "Sequence. Number", "When an object occurs multiple times, this sequence number can be used to provide the order.")));
    }

    @Test
    public void test_PopulateSCin_dt_sc_table() {
        List<DataType> targetDataTypes = dtRepository.findByGuidIn(EXPECTED_GUID_OF_BDT_LIST);

        targetDataTypes.forEach(dt -> {
            DataType basedDt = dtRepository.findOne(dt.getBasedDtId());
            List<DataTypeSupplementaryComponent> expectedDtScList;
            boolean isDefaultBDT;
            if (basedDt.getType() == 0) {
                isDefaultBDT = true;
                expectedDtScList = dtScRepository.findByOwnerDtId(basedDt.getDtId());
                /*
                 * Min Cardinality:
                 * For the default BDTs, take the value from //xsd:attribute/@use.
                 * 'optional' = 0. 'required' = 1, 'prohibited' = 0.
                 * If the //xsd:attribute/@use does not exist, it means 0.
                 *
                 * Max Cardinality:
                 * For the default BDTs, take the value from //xsd:attribute/@use.
                 * 'optional' = 1. 'required' = 1, 'prohibited' = 0.
                 * If the //xsd:attribute/@use does not exist, it means 1.
                 */
                if (predefinedDtScListForDefaultBDT.containsKey(dt.getDen())) {
                    predefinedDtScListForDefaultBDT.get(dt.getDen()).forEach(predefinedDtSc -> {
                        expectedDtScList.forEach(expectedDtSc -> {
                            if (expectedDtSc.getPropertyTerm().equals(predefinedDtSc.getPropertyTerm())) {
                                // Set expected GUID and Definition to inherited DT SC
                                expectedDtSc.setGuid(predefinedDtSc.getGuid());
                                expectedDtSc.setDefinition(predefinedDtSc.getDefinition());

                                switch (predefinedDtSc.getUse()) {
                                    case "optional":
                                        expectedDtSc.setCardinalityMin(0);
                                        expectedDtSc.setCardinalityMax(1);
                                        break;
                                    case "required":
                                        expectedDtSc.setCardinalityMin(1);
                                        expectedDtSc.setCardinalityMax(1);
                                        break;
                                    case "prohibited":
                                        expectedDtSc.setCardinalityMin(0);
                                        expectedDtSc.setCardinalityMax(0);
                                        break;
                                    default:
                                        expectedDtSc.setCardinalityMin(0);
                                        expectedDtSc.setCardinalityMax(1);
                                        break;
                                }
                            }
                        });
                    });
                }
                /*
                 * Min/Max Cardinality:
                 * For the SC (inherited from the base CDT) that has no corresponding xsd:attribute,
                 * the value is 0.
                 */
                else {
                    expectedDtScList.forEach(expectedDtSc -> {
                        expectedDtSc.setCardinalityMin(0);
                        expectedDtSc.setCardinalityMax(0);
                    });
                }
            } else {
                isDefaultBDT = false;
                expectedDtScList = dtScRepository.findByOwnerDtId(basedDt.getDtId());
                if (additionalDtScMapForUnqualifiedBDT.containsKey(dt.getDen())) {
                    expectedDtScList.addAll(dtScRepository.findByGuidIn(
                            additionalDtScMapForUnqualifiedBDT.get(dt.getDen())
                                    .stream()
                                    .map(ExpectedDataTypeSupplementaryComponent::getGuid)
                                    .collect(Collectors.toList())));
                }
            }

            List<DataTypeSupplementaryComponent> actualDtScList = dtScRepository.findByOwnerDtId(dt.getDtId());
            Map<String, DataTypeSupplementaryComponent> actualDtScMap = actualDtScList.stream()
                    .collect(Collectors.toMap(DataTypeSupplementaryComponent::getPropertyTerm, Function.identity()));

            assertEquals("Data Type[id: " + dt.getDtId() + "]'s Supplementary Component size is different.",
                    expectedDtScList.size(), actualDtScList.size());

            assertEquals(
                    expectedDtScList.stream()
                            .map(DataTypeSupplementaryComponent::getPropertyTerm)
                            .mapToInt(String::hashCode)
                            .sum(),
                    actualDtScList.stream()
                            .map(DataTypeSupplementaryComponent::getPropertyTerm)
                            .mapToInt(String::hashCode)
                            .sum()
            );
            assertEquals(
                    expectedDtScList.stream()
                            .map(DataTypeSupplementaryComponent::getRepresentationTerm)
                            .mapToInt(String::hashCode)
                            .sum(),
                    actualDtScList.stream()
                            .map(DataTypeSupplementaryComponent::getRepresentationTerm)
                            .mapToInt(String::hashCode)
                            .sum()
            );

            expectedDtScList.forEach(expectedDtSc -> {
                assertTrue(actualDtScMap.containsKey(expectedDtSc.getPropertyTerm()));
                DataTypeSupplementaryComponent actualDtSc = actualDtScMap.get(expectedDtSc.getPropertyTerm());

                assertEquals("Data Type[id: " + dt.getDtId() + "]'s min cardinality is different.",
                        expectedDtSc.getCardinalityMin(), actualDtSc.getCardinalityMin());
                assertEquals("Data Type[id: " + dt.getDtId() + "]'s max cardinality is different.",
                        expectedDtSc.getCardinalityMax(), actualDtSc.getCardinalityMax());

                if (isDefaultBDT) {
                    if (predefinedDtScListForDefaultBDT.containsKey(dt.getDen())) {
                        assertEquals(expectedDtSc.getGuid(), actualDtSc.getGuid());
                    }
                } else {
                    if (additionalDtScMapForUnqualifiedBDT.containsKey(dt.getDen())) {
                        additionalDtScMapForUnqualifiedBDT.get(dt.getDen()).forEach(additionalDtSc -> {
                            if (additionalDtSc.getPropertyTerm().equals(actualDtSc.getPropertyTerm())) {
                                assertEquals(expectedDtSc.getGuid(), actualDtSc.getGuid());
                            }
                        });
                    }
                }
            });
        });
    }

    @Test
    public void test_Populate_cdt_sc_awd_pri_And_cdt_sc_awd_pri_xps_type_map_Table() {
        // In OAGIS 10.1, there is only one unqualified BDT that has an additional SC to its based default BDT – the NameType.
        additionalDtScMapForUnqualifiedBDT.values().forEach(expectedDtScList -> {
            expectedDtScList.forEach(expectedDtSc -> {
                DataType targetCDT = dtRepository.findOneByDataTypeTermAndType(expectedDtSc.getRepresentationTerm(), 0);

                // cdt_sc_awd_pri
                List<CoreDataTypeAllowedPrimitive> expectedCdtAwdPriList =
                        cdtAwdPriRepository.findByCdtId(targetCDT.getDtId());

                List<CoreDataTypeSupplementaryComponentAllowedPrimitive> actualCdtScAwdPriList =
                        cdtScAwdPriRepository.findByCdtScId(
                                dtScRepository.findOneByGuid(expectedDtSc.getGuid()).getDtScId());

                assertEquals(expectedCdtAwdPriList.size(), actualCdtScAwdPriList.size());
                assertEquals(expectedCdtAwdPriList.stream()
                                .mapToInt(CoreDataTypeAllowedPrimitive::getCdtPriId)
                                .sum(),
                        actualCdtScAwdPriList.stream()
                                .mapToInt(CoreDataTypeSupplementaryComponentAllowedPrimitive::getCdtPriId)
                                .sum());

                // cdt_sc_awd_pri_xps_type_map
                List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> expectedCdtAwdPriXpsTypeMapList =
                        cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriIdIn(
                                expectedCdtAwdPriList.stream()
                                        .mapToInt(CoreDataTypeAllowedPrimitive::getCdtAwdPriId)
                                        .boxed()
                                        .collect(Collectors.toList())
                        );

                List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> actualCdtScAwdPriXpsTypeMapList =
                        cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriIdIn(
                                actualCdtScAwdPriList.stream()
                                        .mapToInt(CoreDataTypeSupplementaryComponentAllowedPrimitive::getCdtScAwdPriId)
                                        .boxed()
                                        .collect(Collectors.toList())
                        );

                assertEquals(expectedCdtAwdPriXpsTypeMapList.size(), actualCdtScAwdPriXpsTypeMapList.size());
                assertEquals(expectedCdtAwdPriXpsTypeMapList.stream()
                                .mapToInt(CoreDataTypeAllowedPrimitiveExpressionTypeMap::getXbtId)
                                .sum(),
                        actualCdtScAwdPriXpsTypeMapList.stream()
                                .mapToInt(CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap::getXbtId)
                                .sum());
            });
        });
    }
}
