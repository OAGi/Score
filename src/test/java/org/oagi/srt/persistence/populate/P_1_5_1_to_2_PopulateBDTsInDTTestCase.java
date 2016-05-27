package org.oagi.srt.persistence.populate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class P_1_5_1_to_2_PopulateBDTsInDTTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    private static class ExpectedDataType {
        private String guid;
        private String den;
        private int basedDtType;
        private String basedDtDen;
        private String definition;
        private String defaultXSDBuiltInType;

        public ExpectedDataType(String guid, String den, int basedDtType, String basedDtDen) {
            this.guid = guid;
            this.den = den;
            this.basedDtType = basedDtType;
            this.basedDtDen = basedDtDen;
        }

        public ExpectedDataType(String guid, String den, int basedDtType, String basedDtDen, String definition, String defaultXSDBuiltInType) {
            this.guid = guid;
            this.den = den;
            this.basedDtType = basedDtType;
            this.basedDtDen = basedDtDen;
            this.definition = definition;
            this.defaultXSDBuiltInType = defaultXSDBuiltInType;
        }

        public String getGuid() {
            return guid;
        }

        public String getDen() {
            return den;
        }

        public String getContentComponentDen() {
            return den.replace(". Type", ". Content");
        }

        public int getBasedDtType() {
            return basedDtType;
        }

        public String getBasedDtDen() {
            return basedDtDen;
        }

        public String getDefinition() {
            return definition;
        }

        public String getDefaultXSDBuiltInType() {
            return defaultXSDBuiltInType;
        }
    }

    private static List<ExpectedDataType> expectedDefaultDataTypes;
    private static List<ExpectedDataType> expectedUnqualifiedDataTypes;
    private static List<ExpectedDataType> expectedExceptionalDefaultBDTs;
    public static List<String> EXPECTED_GUID_OF_BDT_LIST;

    static {
        expectedUnqualifiedDataTypes = new ArrayList();
        expectedDefaultDataTypes = new ArrayList();
        expectedExceptionalDefaultBDTs = new ArrayList();

        // Unqualified DataTypes mapped with Core Data Type (20)
        List<ExpectedDataType> unqualifiedDataTypes = Arrays.asList(
                new ExpectedDataType("oagis-id-109055a967bd4cf19ee3320755b01f8d", "Amount. Type", 1, "Amount_0723C8. Type"),
                new ExpectedDataType("oagis-id-f2c5dcba0088440d866ea23a81876280", "Binary Object. Type", 1, "Binary Object_4277E5. Type"),
                new ExpectedDataType("oagis-id-3318aed9165847e3afb907724db2b65c", "Code. Type", 1, "Code_1DEB05. Type"),
                new ExpectedDataType("oagis-id-3ecb2fb750c144c58c1b68d2fa2a36ae", "Date. Type", 1, "Date_238C51. Type"),
                new ExpectedDataType("oagis-id-dd0c8f86b160428da3a82d2866a5b48d", "Date Time. Type", 1, "Date Time_AD9DD9. Type"),
                new ExpectedDataType("oagis-id-ee2b3bf53bd44b21960ff8575891c638", "Duration. Type", 1, "Duration_JJ5401. Type"),
                new ExpectedDataType("oagis-id-9ec6be30dabf45d5b53b765634be2412", "Graphic. Type", 1, "Graphic_3FDF3D. Type"),
                new ExpectedDataType("oagis-id-bea4dcd433d54aa698db2176cab33c19", "Identifier. Type", 1, "Identifier_D995CD. Type"),
                new ExpectedDataType("oagis-id-ef32205ede95407f981064a45ffa652c", "Indicator. Type", 1, "Indicator_CVW231. Type"),
                new ExpectedDataType("oagis-id-3cbb2f0b87254ff696e9315cd863f613", "Measure. Type", 1, "Measure_671290. Type"),
                new ExpectedDataType("oagis-id-bf66e0afea2c4c2da7bc69af14ca23c9", "Name. Type", 1, "Name_02FC2Z. Type"),
                new ExpectedDataType("oagis-id-06083bfba01d4213a852830000e939b9", "Number. Type", 1, "Number_BE4776. Type"),
                new ExpectedDataType("oagis-id-06083bfba01d4213a852830000e12051", "Ordinal. Type", 1, "Ordinal_PQALZM. Type"),
                new ExpectedDataType("oagis-id-57efecfe17e64e20b83adccce3159a9e", "Percent. Type", 1, "Percent_481002. Type"),
                new ExpectedDataType("oagis-id-5212437eb6f045f98b072db0ce971409", "Quantity. Type", 1, "Quantity_201330. Type"),
                new ExpectedDataType("oagis-id-dc994532fe464847acf84a54548276ff", "Sound. Type", 1, "Sound_697AE6. Type"),
                new ExpectedDataType("oagis-id-d97b8cf6a26f408db148163485796d15", "Text. Type", 1, "Text_62S0B4. Type"),
                new ExpectedDataType("oagis-id-83ea28a4218e447ebe99113901b2c70f", "Time. Type", 1, "Time_100DCA. Type"),
                new ExpectedDataType("oagis-id-d006eb3550364c61bc10cac70763e677", "Value. Type", 1, "Value_D19E7B. Type"),
                new ExpectedDataType("oagis-id-83d4cc94be8249a3b8cbe7e1c2ecb417", "Video. Type", 1, "Video_539B44. Type")
        );
        expectedUnqualifiedDataTypes.addAll(unqualifiedDataTypes);

        // Default DataTypes which are base of previous 20 unqualified DataTypes (20)
        List<ExpectedDataType> defaultBDTs = Arrays.asList(
                new ExpectedDataType("oagis-id-e6f93bd0dc934ab2af11bd46888c1233", "Amount_0723C8. Type", 0, "Amount. Type", "An amount is a number of monetary units specified in a currency", "xsd:decimal"),
                new ExpectedDataType("oagis-id-6eae247688734e6da1dfdadb89c3e43a", "Binary Object_4277E5. Type", 0, "Binary Object. Type", "A binary object is a sequence of binary digits (bits)", "xsd:base64Binary"),
                new ExpectedDataType("oagis-id-0c482df00c2343cd99b1b5c1637a199d", "Code_1DEB05. Type", 0, "Code. Type", "A code is a character string of letters, numbers, special characters (except escape sequences), and symbols. It represents a definitive value, a method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values", "xsd:normalizedString"),
                new ExpectedDataType("oagis-id-f074a322acff4705bbef417505f9bf11", "Date_238C51. Type", 0, "Date. Type", "A date is a gregorian calendar representation in various common resolutions: year, week, day", "xsd:token"),
                new ExpectedDataType("oagis-id-a5cfd20385314a63afc1ffcf6357a08b", "Date Time_AD9DD9. Type", 0, "Date Time. Type", "A date time identifies a date and time of day to various common resolutions: year, month, day, hour, minute, second, and fraction of second", "xsd:token"),
                new ExpectedDataType("oagis-id-f16cdbda66d2441cac9e99615c99e70e", "Duration_JJ5401. Type", 0, "Duration. Type", "A duration is the specification of a length of time without a fixed start or end time, expressed in Gregorian calendar time units (Year, Month, Week, Day) and Hours, Minutes or Second", "xsd:token"),
                new ExpectedDataType("oagis-id-a3101ac1f4734f408d0b01e2ba182648", "Graphic_3FDF3D. Type", 0, "Graphic. Type", "A Graphic. Type is a finite data stream of diagram, graph, mathematical curves, or similar vector based representation in a specific notation", "xsd:base64Binary"),
                new ExpectedDataType("oagis-id-6e141689c22944a083798b9dbba8b47f", "Identifier_D995CD. Type", 0, "Identifier. Type", "An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an agency", "xsd:normalizedString"),
                new ExpectedDataType("oagis-id-c62bf0f41c964349b874b8f397f673ec", "Indicator_CVW231. Type", 0, "Indicator. Type", "An indicator is a list of two mutually exclusive Boolean values that express the only possible states of a property", "xsd:boolean"),
                new ExpectedDataType("oagis-id-bf1305892fcb4e2e9eccec5ae693d73d", "Measure_671290. Type", 0, "Measure. Type", "A measure is a numeric value determined by measuring an object along with the specified unit of measure", "xsd:decimal"),
                new ExpectedDataType("oagis-id-8ef2aeaecfa645088c4bf4b424905596", "Name_02FC2Z. Type", 0, "Name. Type", "A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept", "xsd:string"),
                new ExpectedDataType("oagis-id-e57e1a2b7be44356a2256bc46f61ec36", "Number_BE4776. Type", 0, "Number. Type", "A mathematical number that is assigned or is determined by calculation.", "xsd:decimal"),
                new ExpectedDataType("oagis-id-23a9c386c4304400a8f1dd620b26035b", "Ordinal_PQALZM. Type", 0, "Ordinal. Type", "An ordinal number is an assigned mathematical number that represents order or sequence", "xsd:integer"),
                new ExpectedDataType("oagis-id-af8d69bed6454c0eb9cc494977993b03", "Percent_481002. Type", 0, "Percent. Type", "A percent is a value representing a fraction of one hundred, expressed as a quotient", "xsd:decimal"),
                new ExpectedDataType("oagis-id-6d24dd8e55414acc915b1a2c7e358552", "Quantity_201330. Type", 0, "Quantity. Type", "A quantity is a counted number of non-monetary units, possibly including fractions", "xsd:decimal"),
                new ExpectedDataType("oagis-id-7242ea809d804f9ab4e08766a6117710", "Sound_697AE6. Type", 0, "Sound. Type", "A sound is any form of an audio file such as audio recordings in binary notation (octets)", "xsd:base64Binary"),
                new ExpectedDataType("oagis-id-89be97039be04d6f9cfda107d75926b4", "Text_62S0B4. Type", 0, "Text. Type", "Text is a character string such as a finite set of characters generally in  the form of words of a language", "xsd:string"),
                new ExpectedDataType("oagis-id-6659e405ac8d43268d2a10d451eea261", "Time_100DCA. Type", 0, "Time. Type", "Time is a time of day to various common resolutions - hour, minute, second, and fractions thereof", "xsd:token"),
                new ExpectedDataType("oagis-id-641fdee1d3114629a15902311d895ca2", "Value_D19E7B. Type", 0, "Value. Type", "A value is the concept of worth in general that is assigned or is determined by measurement, assessment or calculation.", "xsd:string"),
                new ExpectedDataType("oagis-id-3292eaa5630b48ecb7c4249b0ddc760e", "Video_539B44. Type", 0, "Video. Type", "A video is a recording, reproducing or broadcasting of visual images on magnetic tape or digitally in binary notation (octets)", "xsd:base64Binary")
        );
        expectedDefaultDataTypes.addAll(defaultBDTs);

        // 3.1.1.8.1.1 Exceptional Unqualified BDTs (13)
        List<ExpectedDataType> exceptionalUnqualifiedBDTs = Arrays.asList(
                new ExpectedDataType("oagis-id-5a2ed18041c04f3e995c773480b0076d", "Day Date. Type", 1, "Date_DB95C8. Type"),
                new ExpectedDataType("oagis-id-af5bfda6510443a5a468bd1df713ff4c", "Month Date. Type", 1, "Date_0C267D. Type"),
                new ExpectedDataType("oagis-id-86ce40c683224461a3d4747410c551c3", "Month Day Date. Type", 1, "Date_5B057B. Type"),
                new ExpectedDataType("oagis-id-a83c14386c7547669c8c9a516ff4c54e", "Year Date. Type", 1, "Date_57D5E1. Type"),
                new ExpectedDataType("oagis-id-004a7da25119417ba44a1f43a2585d0d", "Year Month Date. Type", 1, "Date_BBCC14. Type"),
                new ExpectedDataType("oagis-id-d26b22f9103744edb0a4d3728aefc26e", "Normalized String. Type", 1, "Text_0VCBZ5. Type"),
                new ExpectedDataType("oagis-id-e28a3e09aa6e42339e11a0c740362ca9", "Token. Type", 1, "Text_0F0ZX1. Type"),
                new ExpectedDataType("oagis-id-310d5bf351c143ed80c16ee9ef837271", "String. Type", 1, "Text_62S0C1. Type"),
                new ExpectedDataType("oagis-id-3d47ac271e344e6094791b6e93fbce26", "URI. Type", 1, "Identifier_B3F14E. Type"),
                new ExpectedDataType("oagis-id-10ef9f34e0504a71880c967c82ac039f", "Duration Measure. Type", 1, "Duration_JJ5401. Type"),
                new ExpectedDataType("oagis-id-9b8fed621a7148a5b7fb2f04b80381be", "Integer Number. Type", 1, "Number_B98233. Type"),
                new ExpectedDataType("oagis-id-ec9821a975e84ad9804265b0f082a36b", "Positive Integer Number. Type", 1, "Number_201301. Type"),
                new ExpectedDataType("oagis-id-5a2ed18041c04f3e995c7734386ae380", "Day Of Week Hour Minute UTC. Type", 1, "Time_100DCA. Type")
        );
        expectedUnqualifiedDataTypes.addAll(exceptionalUnqualifiedBDTs);

        // 3.1.1.8.1.1 Default BDTs of Exceptional Unqualified BDTs (11 - 1)
        List<ExpectedDataType> exceptionalDefaultBDTs = Arrays.asList(
                new ExpectedDataType("oagis-id-3049eed90b924d699f1102b946843725", "Date_DB95C8. Type", 0, "Date. Type", "A date is a gregorian calendar representation in various common resolutions: day of month", "xsd:gDay"),
                new ExpectedDataType("oagis-id-3bc40b222d994d9b9fb5d4a33319a146", "Date_5B057B. Type", 0, "Date. Type", "A date is a gregorian calendar representation in various common resolutions: month, day", "xsd:gMonthDay"),
                new ExpectedDataType("oagis-id-52df32ab374440f0ac456a1abe66cb94", "Date_0C267D. Type", 0, "Date. Type", "A date is a gregorian calendar representation in various common resolutions: month", "xsd:gMonth"),
                new ExpectedDataType("oagis-id-6712fbca652e49ac9739396377b090cb", "Date_BBCC14. Type", 0, "Date. Type", "A date is a gregorian calendar representation in various common resolutions: year, month", "xsd:gYearMonth"),
                new ExpectedDataType("oagis-id-0a7f3544ea954099aa06afe488417136", "Date_57D5E1. Type", 0, "Date. Type", "A date is a gregorian calendar representation in various common resolutions: year", "xsd:gYear"),
                new ExpectedDataType("oagis-id-d614ed8726ff482c9c5a8183d735d9ed", "Number_B98233. Type", 0, "Number. Type", "A mathematical number that is assigned or is determined by calculation.", "xsd:integer"),
                new ExpectedDataType("oagis-id-6b81b03c96cc47f08ccb26838853012d", "Number_201301. Type", 0, "Number. Type", "A mathematical number that is assigned or is determined by calculation.", "xsd:positiveInteger"),
                new ExpectedDataType("oagis-id-89be97039be04d6f9cfda107d75926b5", "Text_62S0C1. Type", 0, "Text. Type", "Text is a character string such as a finite set of characters generally in  the form of words of a language", "xsd:string"),
                new ExpectedDataType("oagis-id-42a03ed19450453da6c87fe8eadabfa4", "Text_0VCBZ5. Type", 0, "Text. Type", "A text is a character string such as a finite set of characters generally in the form of words of a language", "xsd:normalizedString"),
                new ExpectedDataType("oagis-id-d5cb8551edf041389893fee25a496395", "Text_0F0ZX1. Type", 0, "Text. Type", "A name is a word or phrase that constitutes the distinctive designation of a person, place, thing or concept", "xsd:token")
        );
        expectedDefaultDataTypes.addAll(exceptionalDefaultBDTs);
        expectedExceptionalDefaultBDTs.addAll(exceptionalDefaultBDTs);

        // 3.1.1.8.1.2 Additional Default BDTs (2 + 1)
        List<ExpectedDataType> additionalDefaultBDTs = Arrays.asList(
                new ExpectedDataType("oagis-id-d2f721a297684b538e7dbb88cf5526bc", "Code_1E7368. Type", 0, "Code. Type", "A code is a character string of letters, numbers, special characters (except escape sequences), and symbols. It represents a definitive value, a method, or a property description in an abbreviated or language-independent form that is part of a finite list of allowed values", "xsd:token"),
                new ExpectedDataType("oagis-id-0fb76e8565244977b1239327ca436f76", "Value_039C44. Type", 0, "Value. Type", "A value is the concept of worth in general that is assigned or is determined by measurement, assessment or calculation.", "xsd:integer"),
                new ExpectedDataType("oagis-id-ff84535456d44233b6f0976d993b442d", "Identifier_B3F14E. Type", 0, "Identifier. Type", "An identifier is a character string used to uniquely identify one instance of an object within an identification scheme that is managed by an agency", "xsd:normalizedString")
        );
        expectedDefaultDataTypes.addAll(additionalDefaultBDTs);

        // 3.1.1.8.1.3 CodeContentType (1)
        expectedUnqualifiedDataTypes.add(
                new ExpectedDataType("oagis-id-5646bf52a97b48adb50ded6ff8c38354", "Code Content. Type", 1, "Code_1E7368. Type")
        );

        // 3.1.1.8.1.4 IdentifierContentType (1)
        expectedUnqualifiedDataTypes.add(
                new ExpectedDataType("oagis-id-08d6ade226fd42488b53c0815664e246", "Identifier Content. Type", 1, "Identifier_B3F14E. Type")
        );
        // number of total tested DT case is 68

        EXPECTED_GUID_OF_BDT_LIST = new ArrayList();
        EXPECTED_GUID_OF_BDT_LIST.addAll(
                expectedDefaultDataTypes.stream().map(ExpectedDataType::getGuid).collect(Collectors.toList())
        );
        EXPECTED_GUID_OF_BDT_LIST.addAll(
                expectedUnqualifiedDataTypes.stream().map(ExpectedDataType::getGuid).collect(Collectors.toList())
        );
        assertEquals(68, EXPECTED_GUID_OF_BDT_LIST.size());
    }


    @Test
    public void testPopulateDTTableForDefaultBDTs() {
        expectedDefaultDataTypes.forEach(expectedDataType -> testPopulateDataType(expectedDataType));
    }

    @Test
    public void testPopulateDTTableForUnqualifiedBDTs() {
        expectedUnqualifiedDataTypes.forEach(expectedDataType -> testPopulateDataType(expectedDataType));
    }

    private void testPopulateDataType(ExpectedDataType expectedDataType) {
        DataType actualDataType = dtRepository.findOneByGuid(expectedDataType.getGuid());
        assertNotNull(actualDataType);

        assertEquals(1, actualDataType.getType());
        assertEquals("Data Type ID[" + actualDataType.getDtId() + "]'s den is different",
                expectedDataType.getDen(), actualDataType.getDen());
        assertEquals("Data Type ID[" + actualDataType.getDtId() + "]'s content component den is different",
                expectedDataType.getContentComponentDen(), actualDataType.getContentComponentDen());
        assertEquals("Data Type ID[" + actualDataType.getDtId() + "]'s definition is different",
                expectedDataType.getDefinition(), actualDataType.getDefinition());

        DataType actualBasedDataType =
                dtRepository.findOneByTypeAndDen(expectedDataType.getBasedDtType(),
                        expectedDataType.getBasedDtDen());
        assertNotNull("Data Type ID[" + actualDataType.getDtId() + "]'s based data type can't found. " +
                        "Based Data Type's type[" + expectedDataType.getBasedDtType() +
                        "] and den[" + expectedDataType.getBasedDtDen() + "]",
                actualBasedDataType);

        assertEquals(actualBasedDataType.getDtId(), actualDataType.getBasedDtId());
    }

    @Test
    public void testPopulateBDTPriRestriTableForDefaultBDTs() {
        // for Default BDTs
        expectedDefaultDataTypes.forEach(expectedDataType -> {
            DataType basedDataType =
                    dtRepository.findOneByTypeAndDen(expectedDataType.getBasedDtType(),
                            expectedDataType.getBasedDtDen());
            DataType cdt;
            if (basedDataType.getType() == 0) {
                cdt = basedDataType;
            } else {
                cdt = dtRepository.findOne(basedDataType.getBasedDtId());
            }
            assertEquals(0, cdt.getType());

            List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> expectedCdtAwdPriXpsTypeMapList =
                    cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriIdIn(
                            cdtAwdPriRepository.findByCdtId(cdt.getDtId()).stream()
                                    .mapToInt(CoreDataTypeAllowedPrimitive::getCdtAwdPriId)
                                    .boxed()
                                    .collect(Collectors.toList())
                    );

            if (expectedExceptionalDefaultBDTs.contains(expectedDataType)) {
                boolean isTimePoint = false;
                if (expectedDataType.getDen().contains("Date")) {
                    isTimePoint = true;
                }
                String defaultXSDBuiltInType = expectedDataType.getDefaultXSDBuiltInType();
                int xsdTokenId = xbtRepository.findOneByBuiltInType("xsd:token").getXbtId();
                int defaultXBTId = xbtRepository.findOneByBuiltInType(defaultXSDBuiltInType).getXbtId();

                for (int i = expectedCdtAwdPriXpsTypeMapList.size() - 1; i > -1; i--) {
                    boolean checkRemove = true;
                    if (isTimePoint) {
                        if (expectedCdtAwdPriXpsTypeMapList.get(i).getXbtId() == xsdTokenId) {
                            checkRemove = false;
                        }
                    }

                    if (expectedCdtAwdPriXpsTypeMapList.get(i).getXbtId() == defaultXBTId) {
                        checkRemove = false;
                    }

                    if (checkRemove) {
                        expectedCdtAwdPriXpsTypeMapList.remove(i);
                    }
                }
            }

            DataType bdt = dtRepository.findOneByGuid(expectedDataType.getGuid());

            List<BusinessDataTypePrimitiveRestriction> actualBdtPriRestriList =
                    bdtPriRestriRepository.findByBdtId(bdt.getDtId());
            assertEquals(expectedCdtAwdPriXpsTypeMapList.size(), actualBdtPriRestriList.size());
            assertEquals(expectedCdtAwdPriXpsTypeMapList.stream()
                            .mapToInt(CoreDataTypeAllowedPrimitiveExpressionTypeMap::getCdtAwdPriXpsTypeMapId)
                            .sum(),
                    actualBdtPriRestriList.stream()
                            .mapToInt(BusinessDataTypePrimitiveRestriction::getCdtAwdPriXpsTypeMapId)
                            .sum());

            String defaultXSDBuiltInType = expectedDataType.getDefaultXSDBuiltInType();
            if (defaultXSDBuiltInType == null) { // if it is unqualified BDT, try to find from its base.
                defaultXSDBuiltInType = expectedDefaultDataTypes.stream()
                        .filter(e -> e.getDen().equalsIgnoreCase(expectedDataType.getBasedDtDen()))
                        .findFirst()
                        .get().getDefaultXSDBuiltInType();
            }
            XSDBuiltInType expectedDefaultXbt =
                    xbtRepository.findOneByBuiltInType(defaultXSDBuiltInType);

            List<BusinessDataTypePrimitiveRestriction> defaultOfActualBdtPriRestriList =
                    actualBdtPriRestriList.stream()
                            .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                            .collect(Collectors.toList());
            assertEquals(1, defaultOfActualBdtPriRestriList.size());

            XSDBuiltInType actualDefaultXbt = xbtRepository.findOne(
                    cdtAwdPriXpsTypeMapRepository.findOne(
                            defaultOfActualBdtPriRestriList.get(0).getCdtAwdPriXpsTypeMapId())
                            .getXbtId());

            assertNotNull(actualDefaultXbt);
            assertEquals(expectedDefaultXbt.getXbtId(), actualDefaultXbt.getXbtId());
        });
    }

    @Test
    public void testPopulateBDTPriRestriTableForUnqualifiedBDTs() {
        // for Unqualified BDTs
        expectedUnqualifiedDataTypes.forEach(expectedDataType -> {
            DataType bdt = dtRepository.findOneByGuid(expectedDataType.getGuid());

            List<BusinessDataTypePrimitiveRestriction> actualBdtPriRestriList =
                    bdtPriRestriRepository.findByBdtId(bdt.getDtId());

            List<BusinessDataTypePrimitiveRestriction> expectedBdtPriRestriList =
                    bdtPriRestriRepository.findByBdtId(bdt.getBasedDtId());

            assertEquals(expectedBdtPriRestriList.size(), actualBdtPriRestriList.size());
            assertEquals(expectedBdtPriRestriList.stream()
                            .mapToInt(BusinessDataTypePrimitiveRestriction::getCdtAwdPriXpsTypeMapId)
                            .sum(),
                    actualBdtPriRestriList.stream()
                            .mapToInt(BusinessDataTypePrimitiveRestriction::getCdtAwdPriXpsTypeMapId)
                            .sum());

            String defaultXSDBuiltInType = expectedDataType.getDefaultXSDBuiltInType();


            if (defaultXSDBuiltInType == null) { // if it is unqualified BDT, try to find from its base.
                defaultXSDBuiltInType = expectedDefaultDataTypes.stream()
                        .filter(e -> e.getDen().equalsIgnoreCase(expectedDataType.getBasedDtDen()))
                        .findFirst()
                        .get().getDefaultXSDBuiltInType();
            }

            XSDBuiltInType expectedDefaultXbt =
                    xbtRepository.findOneByBuiltInType(defaultXSDBuiltInType);

            List<BusinessDataTypePrimitiveRestriction> defaultOfActualBdtPriRestriList =
                    actualBdtPriRestriList.stream()
                            .filter(bdtPriRestri -> bdtPriRestri.isDefault())
                            .collect(Collectors.toList());
            assertEquals(1, defaultOfActualBdtPriRestriList.size());

            XSDBuiltInType actualDefaultXbt = xbtRepository.findOne(
                    cdtAwdPriXpsTypeMapRepository.findOne(
                            defaultOfActualBdtPriRestriList.get(0).getCdtAwdPriXpsTypeMapId())
                            .getXbtId());

            assertNotNull(actualDefaultXbt);
            assertEquals(expectedDefaultXbt.getXbtId(), actualDefaultXbt.getXbtId());
        });
    }

}
