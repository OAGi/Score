package org.oagi.srt.persistence.populate;

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
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImportApplication.class)
@Transactional(readOnly = true)
public class P_1_5_6_PopulateBDTSCPrimitiveRestrictionTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private DataTypeSupplementaryComponentRepository dataTypeSupplementaryComponentRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtSCPrimitiveRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtSCAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtSCAwdPrimitiveRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository coreDataTypePrimitiveRepository;

    @Autowired
    private XSDBuiltInTypeRepository xsdBuiltInTypeRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    private class ExpectedBDTSCPrimitive {
        private List<String> maps;
        private String codeListName;
        private String agencyIDListName;
        private int defaultIndex;

        public ExpectedBDTSCPrimitive(List<String> maps, String codeListName, String agencyIDListName, int defaultIndex) {
            this.maps = maps;
            this.codeListName = codeListName;
            this.agencyIDListName = agencyIDListName;
            this.defaultIndex = defaultIndex;
        }
    }

    @Test
    public void testOnlyOneDefaultForBDTSCPrimitive(){

        List<DataType> dtList = dataTypeRepository.findByType(1);
        for(int i=0; i<dtList.size(); i++){
            List<DataTypeSupplementaryComponent> dtscList = dataTypeSupplementaryComponentRepository.findByOwnerDtId(dtList.get(i).getDtId());
            for(int j=0; j<dtscList.size(); j++){
                int defaultCount=0;
                List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtSCPriList = bdtSCPrimitiveRepository.findByBdtScId(dtscList.get(j).getDtScId());
                for(int m=0; m<bdtSCPriList.size(); m++){
                    if(bdtSCPriList.get(m).isDefault()){
                        defaultCount++;
                    }
                }
                assertEquals(1, defaultCount);
            }
        }
    }


    @Test
    public void testPopulateBDTSCPrimitive() {

        Map<String, ExpectedBDTSCPrimitive> expectedBDTSCPrimitive = new HashMap();
        expectedBDTSCPrimitive.put("Amount_0723C8. Type^Currency^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "oacl_CurrencyCode", "", 0));
        expectedBDTSCPrimitive.put("Binary Object_4277E5. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Binary Object_4277E5. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Binary Object_4277E5. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Graphic_3FDF3D. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Graphic_3FDF3D. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Graphic_3FDF3D. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Sound_697AE6. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Sound_697AE6. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Sound_697AE6. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Video_539B44. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Video_539B44. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Video_539B44. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Code_1DEB05. Type^List^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Code_1DEB05. Type^List Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "", "clm63055D08B_AgencyIdentification", 0));
        expectedBDTSCPrimitive.put("Code_1DEB05. Type^List Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Date Time_AD9DD9. Type^Time Zone^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Date Time_AD9DD9. Type^Daylight Saving^Indicator", new ExpectedBDTSCPrimitive(Arrays.asList("Boolean^xbt_BooleanTrueFalseType"), "", "", 0));
        expectedBDTSCPrimitive.put("Identifier_D995CD. Type^Scheme^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Identifier_D995CD. Type^Scheme Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Identifier_D995CD. Type^Scheme Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "", "clm63055D08B_AgencyIdentification", 0));
        expectedBDTSCPrimitive.put("Measure_671290. Type^Unit^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clm6Recommendation205_MeasurementUnitCommonCode", "", 0));
        expectedBDTSCPrimitive.put("Name_02FC2Z. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clm56392A20081107_LanguageCode", "", 0));
        expectedBDTSCPrimitive.put("Quantity_201330. Type^Unit^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "oacl_UnitCode", "", 0));
        expectedBDTSCPrimitive.put("Text_62S0B4. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clm56392A20081107_LanguageCode", "", 0));
        expectedBDTSCPrimitive.put("Text_0VCBZ5. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Text_0F0ZX1. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Text_62S0C1. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Identifier_B3F14E. Type^Scheme^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Identifier_B3F14E. Type^Scheme Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Identifier_B3F14E. Type^Scheme Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Code_1E7368. Type^List^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Code_1E7368. Type^List Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Code_1E7368. Type^List Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));

        expectedBDTSCPrimitive.put("Amount. Type^Currency^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "oacl_CurrencyCode", "", 0));
        expectedBDTSCPrimitive.put("Binary Object. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Binary Object. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Binary Object. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Graphic. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Graphic. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Graphic. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Sound. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Sound. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Sound. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Video. Type^MIME^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "", 0));
        expectedBDTSCPrimitive.put("Video. Type^Character Set^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clmIANACharacterSetCode20070514_CharacterSetCode", "", 0));
        expectedBDTSCPrimitive.put("Video. Type^Filename^Name", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Code. Type^List^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Code. Type^List Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "", "clm63055D08B_AgencyIdentification", 0));
        expectedBDTSCPrimitive.put("Code. Type^List Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Date Time. Type^Time Zone^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Date Time. Type^Daylight Saving^Indicator", new ExpectedBDTSCPrimitive(Arrays.asList("Boolean^xbt_BooleanTrueFalseType"), "", "", 0));
        expectedBDTSCPrimitive.put("Identifier. Type^Scheme^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Identifier. Type^Scheme Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 0));
        expectedBDTSCPrimitive.put("Identifier. Type^Scheme Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "", "clm63055D08B_AgencyIdentification", 0));
        expectedBDTSCPrimitive.put("Measure. Type^Unit^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clm6Recommendation205_MeasurementUnitCommonCode", "", 0));
        expectedBDTSCPrimitive.put("Name. Type^Sequence Number^Number", new ExpectedBDTSCPrimitive(Arrays.asList("Decimal^xsd:decimal", "Double^xsd:double", "Double^xsd:float", "Float^xsd:float", "Integer^xsd:integer", "Integer^xsd:positiveInteger", "Integer^xsd:nonNegativeInteger"), "", "", 0));
        expectedBDTSCPrimitive.put("Name. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clm56392A20081107_LanguageCode", "", 0));
        expectedBDTSCPrimitive.put("Quantity. Type^Unit^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "oacl_UnitCode", "", 0));
        expectedBDTSCPrimitive.put("Text. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("Token^xsd:token"), "clm56392A20081107_LanguageCode", "", 0));
        expectedBDTSCPrimitive.put("Normalized String. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Token. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("String. Type^Language^Code", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("URI. Type^Scheme^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("URI. Type^Scheme Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("URI. Type^Scheme Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Code Content. Type^List^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Code Content. Type^List Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Code Content. Type^List Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Identifier Content. Type^Scheme^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Identifier Content. Type^Scheme Version^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));
        expectedBDTSCPrimitive.put("Identifier Content. Type^Scheme Agency^Identifier", new ExpectedBDTSCPrimitive(Arrays.asList("NormalizedString^xsd:normalizedString", "String^xsd:string", "Token^xsd:token"), "", "", 2));


        for (Entry<String, ExpectedBDTSCPrimitive> entry : expectedBDTSCPrimitive.entrySet()) {
            String key = entry.getKey();
            ExpectedBDTSCPrimitive value = entry.getValue();

            DataType dt = dataTypeRepository.findOneByTypeAndDen(1, getBDTDen(key));
            assertTrue(dt != null);
            DataTypeSupplementaryComponent dtsc = dataTypeSupplementaryComponentRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(dt.getDtId(), getPropertyTerm(key), getRepresentationTerm(key));
            assertTrue(dtsc != null);
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtSCPris = bdtSCPrimitiveRepository.findByBdtScId(dtsc.getDtScId());

            long expectedMapIdSum = 0L;
            long expectedCodeListId = 0L;
            long expectedAgencyIdListId = 0L;
            long expectedDefaultMapId = 0L;

            expectedCodeListId = getCodeListId(value.codeListName);
            expectedAgencyIdListId = getAgencyIdListId(value.agencyIDListName);

            for (int i = 0; i < value.maps.size(); i++) {
                String map = value.maps.get(i);
                long cdtSCAwdPriXpsTypeId = getCDTSCAwdPriXpsTypeMapId(dtsc, map);
                expectedMapIdSum += cdtSCAwdPriXpsTypeId;

                if (i == value.defaultIndex) {
                    expectedDefaultMapId = cdtSCAwdPriXpsTypeId;
                }
            }


            long actualMapIdSum = 0L;
            long actualCodeListId = 0L;
            long actualAgencyIdListId = 0L;
            long actualDefaultMapId = 0L;

            for (int i = 0; i < bdtSCPris.size(); i++) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri = bdtSCPris.get(i);

                actualMapIdSum += bdtSCPri.getCdtScAwdPriXpsTypeMapId();
                actualCodeListId += bdtSCPri.getCodeListId();
                actualAgencyIdListId += bdtSCPri.getAgencyIdListId();
                if (bdtSCPri.isDefault()) {
                    actualDefaultMapId = bdtSCPri.getCdtScAwdPriXpsTypeMapId();
                }
            }
            assertEquals(expectedMapIdSum, actualMapIdSum);
            assertEquals(expectedCodeListId, actualCodeListId);
            assertEquals(expectedAgencyIdListId, actualAgencyIdListId);
            assertEquals(expectedDefaultMapId, actualDefaultMapId);
        }
    }

    public long getCodeListId(String codeListName) {
        CodeList cdl = codeListRepository.findOneByName(codeListName);
        if (cdl != null) {
            return cdl.getCodeListId();
        } else {
            return 0L;
        }
    }

    public long getAgencyIdListId(String agencyIdListName) {
        AgencyIdList agl = agencyIdListRepository.findOneByName(agencyIdListName);
        if (agl != null) {
            return agl.getAgencyIdListId();
        } else {
            return 0L;
        }
    }

    public String getBDTDen(String key) {
        String den;
        int posSeparator = -1;
        posSeparator = key.indexOf("^");
        den = key.substring(0, posSeparator);
        return den;
    }

    public String getPropertyTerm(String key) {
        String propertyTerm;
        int posSeparator = -1;
        int posSeparator2 = -1;
        posSeparator = key.indexOf("^");
        posSeparator2 = key.lastIndexOf("^");
        propertyTerm = key.substring(posSeparator + 1, posSeparator2);
        return propertyTerm;
    }

    public String getRepresentationTerm(String key) {
        String representationTerm;
        int posSeparator = -1;
        posSeparator = key.lastIndexOf("^");
        representationTerm = key.substring(posSeparator + 1);
        return representationTerm;
    }

    public long getCDTSCAwdPriXpsTypeMapId(DataTypeSupplementaryComponent dtsc, String map) {
        long cdtSCId = getCDTSCId(dtsc.getDtScId());
        String cdtPriName;
        String xsdBuiltInType;

        int posSeparator = -1;
        posSeparator = map.indexOf("^");
        cdtPriName = map.substring(0, posSeparator);
        xsdBuiltInType = map.substring(posSeparator + 1);

        long xbtId = xsdBuiltInTypeRepository.findOneByBuiltInType(xsdBuiltInType).getXbtId();
        long cdtPriId = coreDataTypePrimitiveRepository.findOneByName(cdtPriName).getCdtPriId();

        long cdtSCAwdPriId = cdtSCAwdPrimitiveRepository.findOneByCdtScIdAndCdtPriId(cdtSCId, cdtPriId).getCdtScAwdPriId();

        return cdtSCAwdPriXpsTypeMapRepository.findOneByCdtScAwdPriIdAndXbtId(cdtSCAwdPriId, xbtId).getCdtScAwdPriXpsTypeMapId();
    }

    public long getCDTSCId(long dtSCId) {
        DataTypeSupplementaryComponent dtsc = dataTypeSupplementaryComponentRepository.findOne(dtSCId);

        if (dtsc.getBasedDtScId() == 0L) {
            return dtSCId;
        } else {
            return getCDTSCId(dtsc.getBasedDtScId());
        }

    }
}