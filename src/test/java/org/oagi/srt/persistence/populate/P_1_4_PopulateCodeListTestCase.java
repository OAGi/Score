package org.oagi.srt.persistence.populate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class P_1_4_PopulateCodeListTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    private class ExpectedCodeList {
        private String module;
        private String name;
        private String guid;

        public ExpectedCodeList(String module, String name, String guid) {
            this.module = module;
            this.name = name;
            this.guid = guid;
        }

        public String getModule() {
            return module;
        }

        public String getName() {
            return name;
        }

        public String getGuid() {
            return guid;
        }
    }

    @Test
    public void test_PopulateCodeListTable() {
        List<ExpectedCodeList> expectedCodeLists = new ArrayList();
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_CharacterSetCode_IANA_20070514.xsd", "clmIANACharacterSetCode20070514_CharacterSetCode", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_ConditionTypeCode_1.xsd", "clm6ConditionTypeCode1_ConditionTypeCode", "oagis-id-b7e16bb124eb4ddbb86d4861383fe372"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_ConstraintTypeCode_1.xsd", "clm6ConstraintTypeCode1_ConstraintTypeCode", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_CurrencyCode_ISO_7_04.xsd","clm542173A20090305_ISO3AlphaCurrencyCode", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_DateFormatCode_1.xsd", "clm6DateFormatCode1_DateFormatCode", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_DateTimeFormatCode_1.xsd","clm6DateTimeFormatCode1_DateTimeFormatCode", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_LanguageCode_ISO_7_04.xsd", "clm56392A20081107_LanguageCode", "oagis-id-c5e8ac8c44894e54a147a870136da686"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_MIMEMediaTypeCode_IANA_7_04.xsd", "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode", "oagis-id-efcfccd8fff24243a86b8f0d432db37a"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_TimeFormatCode_1.xsd", "clm6TimeFormatCode1_TimeFormatCode", "oagis-id-98f48609a5154608bd5673aea57784da"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_TimeZoneCode_1.xsd", "clm6TimeZoneCode1_TimeZoneCode", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_UnitCode_UNECE_7_04.xsd", "clm6Recommendation205_MeasurementUnitCommonCode", "oagis-id-cbb8c3e6d3bb40428f8efd8639c8e222"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ActionCode", "oagis-id-49c1788460864e99b0872d0a6e58bddb"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_AgencyRoleCode", "oagis-id-a9ee4e537bde4e2ba96fa1d0aa04dba7"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CategoryCode", "oagis-id-ec3ae69ddf6046f09b906caf951f4f35"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ChargeBearerCode", "oagis-id-c6716c40af4d4f0c926b975dd5918270"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ChargeCode", "oagis-id-f0dfec16cc4d44a198e4bfbf72a37b81"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ClassificationCode", "oagis-id-18a04d1b4f124693bc4272463e701755"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CharacterSetCode", "oagis-id-8274b2e56a8941a6a235bb1faf227e74"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CheckDeliveryMethodCode", "oagis-id-08040659d66c4591a1353cc431c50c4a"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CheckInstructionCode", "oagis-id-25f1e4ef171045f0be4e9e6785dbbb70"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ConfirmationCode", "oagis-id-a19867f6ebe5426d99fe4808b11e23c4"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ContactLocationCode", "oagis-id-6233008ff5874c15a76000e186e0130d"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ControlAssertionCode", "oagis-id-46dded6e75894d85a7e772090cb42c5b"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ControlCode", "oagis-id-f01cb1632cbd4ae89cf1b9841c40e2f0"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ControlComponentCode", "oagis-id-be54304dfda44246aa554217f5be5124"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CountryCode", "oagis-id-bac4c638149847478d35fa6c377b9f13"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CountrySubdivisionCode", "oagis-id-d954fa8d2aef4e6d93ff9f1841bad5f6"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CreditTransferCode", "oagis-id-e64591fa9a2a46009b0e1142710d8793"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_CurrencyCode", "oagis-id-c4c7892ab835480895b142ca41930292"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_DayOfWeekCode", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_DebitCreditCode", "oagis-id-69e1317dc2f24edcb1847f34f66be977"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_DeliveryPointCode", "oagis-id-da0b3fdfca0d48a0aa4017c5e71eaecf"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_EMailFormatCode", "oagis-id-428bb44db16241faa4813f779ffff26e"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_EngineeringActivityCode", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_EngineeringWorkOrderCode", "oagis-id-97ffe643788648c096ea981c32c2a3dd"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_EngineeringWorkRequestCode", "oagis-id-cc80f54e299048859c47c0046f7c7e36"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_EngineeringWorkRequestStatusCode", "oagis-id-0073c51b321e42e795d1b48116a7620f"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_FinalAgentInstructionCode", "oagis-id-1dd922a4b7e348e7af93870e7ab75d4c"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_FreightTermCode", "oagis-id-0da859f6357b4769aab010637c2724ad"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_GenderCode", "oagis-id-58bcc34f61284f4580e706506453fc3d"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_IncotermsCode", "oagis-id-c9060a9151244e2d883bfabbb779a165"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_LanguageCode", "oagis-id-7f23d96ea25c443fbdcd5411159ff377"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_LicenseTypeCode", "oagis-id-608c4f0fe09d45529dfa723d5406c5a0"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_MaritalStatusCode", "oagis-id-33c77e39087143f088565de1a0ebee77"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_MatchCode", "oagis-id-0a1384322190497499cf2ca6b26d8934"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_MatchDocumentCode", "oagis-id-f516abfd2be844d3acf481ed8b1399c3"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_PartyCategoryCode", "oagis-id-dbcb006c92754ed7a064ea0c8dc5940c"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_MIMECode", "oagis-id-51ed08216606417b9b9266b464c204f7"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_PartyRoleCode", "oagis-id-9f78cba9c3b04b60877b2e0217f8c2fa"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_PaymentBasisCode", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_PaymentPurposeCode", "oagis-id-f6ae1fdc206843468def6e636ce34384"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_PaymentMethodCode", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_PaymentSchemeCode", "oagis-id-985a704ce00b4f8791f436f638d85ea1"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_PaymentTermCode", "oagis-id-b9d13739f3fc4fbfa790ebd62972bcfd"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ProcessCategoryCode", "oagis-id-1bb1a4c4d6784b558be0f40d43404424"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ProcessConfirmationCode", "oagis-id-bb1cc4dc481a477fa2674e52e3a90cfb"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ReasonCode", "oagis-id-60dde524708b4b7db304686fd159d2cb"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_RecurrencePatternCode", "oagis-id-472dbebea6be484b9a9e3a90612b96ce"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_RemitLocationMethodCode", "oagis-id-c04686cc6c254869b51ea7ff39d860e5"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ResourceTypeCode", "oagis-id-02d26533df40477583a3fea53f90e0e8"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ResponseActionCode", "oagis-id-ecd22496e9d245e09a3851e71fe3ee18"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_ResponseCode", "oagis-id-73c93d58425945a0a0a6abfe4cf53274"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_RiskCode", "oagis-id-ba592aa0ac6e4b75a4079327b9e988c4"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_SalesActivityCode", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_SalesTaskCode", "oagis-id-902eea339f974afb94840f4aef4e1d8f"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_StateCode", "oagis-id-5d83970fc7a14264960da8e6c9fca3e4"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_SystemEnvironmentCode", "oagis-id-9aa6992a44c349b5b9a2ab7dbc51e939"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_TimeZoneCode", "oagis-id-9aa6992a44c349b5b9a2ab7dbc51e945"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_TaxCode", "oagis-id-19df69be3a5b4080a693730044647de3"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_TransferCode", "oagis-id-a1aef967e9644d8f92024d518b2adc09"));
        expectedCodeLists.add(new ExpectedCodeList("Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd", "oacl_UnitCode", "oagis-id-832d7dc7e6fb4020afd7edc99bb16742"));

        assertEquals(71, expectedCodeLists.size());
        assertEquals(71, codeListRepository.count());

        Map<String, ExpectedCodeList> expectedCodeListMap =
                expectedCodeLists.stream().collect(Collectors.toMap(ExpectedCodeList::getGuid, Function.identity()));

        codeListRepository.findAll().forEach(codeList -> {
            String guid = codeList.getGuid();
            assertTrue(expectedCodeListMap.containsKey(guid));

            ExpectedCodeList expectedCodeList = expectedCodeListMap.get(guid);
            assertEquals(expectedCodeList.getName(), codeList.getName());
            assertEquals(expectedCodeList.getModule(), codeList.getModule());
        });
    }
}
