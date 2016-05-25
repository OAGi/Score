package org.oagi.srt.persistence.populate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.CodeList;
import org.oagi.srt.repository.entity.CodeListValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class P_1_4_PopulateCodeListTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    private class ExpectedCodeList {
        private String module;
        private String name;
        private String guid;
        private String enumTypeGuid;
        private String baseCodeListGuid;
        private int versionId;

        public ExpectedCodeList(String module, String name, String guid,
                                String enumTypeGuid, String baseCodeListGuid, int versionId) {
            this.module = module;
            this.name = name;
            this.guid = guid;
            this.enumTypeGuid = enumTypeGuid;
            this.baseCodeListGuid = baseCodeListGuid;
            this.versionId = versionId;
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

        public String getEnumTypeGuid() {
            return enumTypeGuid;
        }

        public String getBaseCodeListGuid() {
            return baseCodeListGuid;
        }

        public int getVersionId() {
            return versionId;
        }
    }

    @Test
    public void test_PopulateCodeListTable() {
        List<ExpectedCodeList> expectedCodeLists = new ArrayList();
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_CharacterSetCode_IANA_20070514.xsd",
                "clmIANACharacterSetCode20070514_CharacterSetCode",
                null,
                null,
                null,
                20070514));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_ConditionTypeCode_1.xsd",
                "clm6ConditionTypeCode1_ConditionTypeCode",
                "oagis-id-b7e16bb124eb4ddbb86d4861383fe372",
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_ConstraintTypeCode_1.xsd",
                "clm6ConstraintTypeCode1_ConstraintTypeCode",
                "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b",
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_CurrencyCode_ISO_7_04.xsd",
                "clm542173A20090305_ISO3AlphaCurrencyCode",
                "oagis-id-b219e03523e04d1fb3379d37001c8f0c",
                null,
                null,
                20090305));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_DateFormatCode_1.xsd",
                "clm6DateFormatCode1_DateFormatCode",
                "oagis-id-46fc137ae5b44dde9ab25724a2abec86",
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_DateTimeFormatCode_1.xsd",
                "clm6DateTimeFormatCode1_DateTimeFormatCode",
                null,
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_LanguageCode_ISO_7_04.xsd",
                "clm56392A20081107_LanguageCode",
                "oagis-id-c5e8ac8c44894e54a147a870136da686",
                null,
                null,
                20081107));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_MIMEMediaTypeCode_IANA_7_04.xsd",
                "clmIANAMIMEMediaType20090304_MIMEMediaTypeCode",
                "oagis-id-efcfccd8fff24243a86b8f0d432db37a",
                null,
                null,
                20090304));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_TimeFormatCode_1.xsd",
                "clm6TimeFormatCode1_TimeFormatCode",
                "oagis-id-98f48609a5154608bd5673aea57784da",
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_TimeZoneCode_1.xsd",
                "clm6TimeZoneCode1_TimeZoneCode",
                null,
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeList_UnitCode_UNECE_7_04.xsd",
                "clm6Recommendation205_MeasurementUnitCommonCode",
                "oagis-id-cbb8c3e6d3bb40428f8efd8639c8e222",
                null,
                null,
                5));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ActionCode",
                "oagis-id-49c1788460864e99b0872d0a6e58bddb",
                "oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_AgencyRoleCode",
                "oagis-id-a9ee4e537bde4e2ba96fa1d0aa04dba7",
                "oagis-id-1cfb7a2e0cc04586a6732f3871d670ec",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CategoryCode",
                "oagis-id-ec3ae69ddf6046f09b906caf951f4f35",
                "oagis-id-ae69554cc74b4fc6a57977bb382b5f5a",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ChargeBearerCode",
                "oagis-id-c6716c40af4d4f0c926b975dd5918270",
                "oagis-id-a355da7ff7e444aa8b27a4d3323eba08",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ChargeCode",
                "oagis-id-f0dfec16cc4d44a198e4bfbf72a37b81",
                "oagis-id-dbf83a334fa84d7992a7db76d70cf5a0",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ClassificationCode",
                "oagis-id-18a04d1b4f124693bc4272463e701755",
                "oagis-id-d260cb0d1ce44dadbb736059bb284e72",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CharacterSetCode",
                "oagis-id-8274b2e56a8941a6a235bb1faf227e74",
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CheckDeliveryMethodCode",
                "oagis-id-08040659d66c4591a1353cc431c50c4a",
                "oagis-id-e7eeecddd9544535a9a89fc1276bb494",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CheckInstructionCode",
                "oagis-id-25f1e4ef171045f0be4e9e6785dbbb70",
                "oagis-id-b8553ba514c64ad68b49bd5f83a6ef72",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ConfirmationCode",
                "oagis-id-a19867f6ebe5426d99fe4808b11e23c4",
                "oagis-id-7ec2195e70294a13bfb9414de9f14473",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ContactLocationCode",
                "oagis-id-6233008ff5874c15a76000e186e0130d",
                "oagis-id-fc8d4b561faa47ff94fffdeb12566aea",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ControlAssertionCode",
                "oagis-id-46dded6e75894d85a7e772090cb42c5b",
                "oagis-id-64ffe9653e934bee80d9051818bf3b94",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ControlCode",
                "oagis-id-f01cb1632cbd4ae89cf1b9841c40e2f0",
                "oagis-id-58a748fc51eb421a862db5fcfcf85c3b",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ControlComponentCode",
                "oagis-id-be54304dfda44246aa554217f5be5124",
                "oagis-id-af274f44caad4162804061f2342a3715",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CountryCode",
                "oagis-id-bac4c638149847478d35fa6c377b9f13",
                "oagis-id-e28b4633be8e480480706595baaa42f2",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CountrySubdivisionCode",
                "oagis-id-d954fa8d2aef4e6d93ff9f1841bad5f6",
                "oagis-id-b7e948384e9348a2b2e6ff50ddf84d5b",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CreditTransferCode",
                "oagis-id-e64591fa9a2a46009b0e1142710d8793",
                "oagis-id-5482a811ea5047d38ea6082e02b076b0",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_CurrencyCode",
                "oagis-id-c4c7892ab835480895b142ca41930292",
                null,
                "oagis-id-b219e03523e04d1fb3379d37001c8f0c",
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_DayOfWeekCode",
                "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4",
                "oagis-id-8314d4295c1b48c3864a47504e464952",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_DebitCreditCode",
                "oagis-id-69e1317dc2f24edcb1847f34f66be977",
                "oagis-id-ca797f68a80c4475a4e67713ca79db8d",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_DeliveryPointCode",
                "oagis-id-da0b3fdfca0d48a0aa4017c5e71eaecf",
                "oagis-id-b04bbef96ed34fd1a155323a38d74cfa",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_EMailFormatCode",
                "oagis-id-428bb44db16241faa4813f779ffff26e",
                "oagis-id-9d182f28e99840318c9757dab3177019",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_EngineeringActivityCode",
                "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1",
                "oagis-id-10eb52c5181f419ea5f52cf501d86708",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_EngineeringWorkOrderCode",
                "oagis-id-97ffe643788648c096ea981c32c2a3dd",
                "oagis-id-aa71419c1d7842819c3baaa28926393c",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_EngineeringWorkRequestCode",
                "oagis-id-cc80f54e299048859c47c0046f7c7e36",
                "oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_EngineeringWorkRequestStatusCode",
                "oagis-id-0073c51b321e42e795d1b48116a7620f",
                "oagis-id-a0d4ca19498b42a1af3219d8e6dd4213",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_FinalAgentInstructionCode",
                "oagis-id-1dd922a4b7e348e7af93870e7ab75d4c",
                "oagis-id-ff46417e01b640a78776bd03ec58fc8f",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_FreightTermCode",
                "oagis-id-0da859f6357b4769aab010637c2724ad",
                "oagis-id-67c5a58be44e43df854e397c0691d6d4",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_GenderCode",
                "oagis-id-58bcc34f61284f4580e706506453fc3d",
                "oagis-id-54bb202e31294a098faf49ba2767c4bc",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_IncotermsCode",
                "oagis-id-c9060a9151244e2d883bfabbb779a165",
                "oagis-id-610f796fb0e246809299f9c8ccd5e307",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_LanguageCode",
                "oagis-id-7f23d96ea25c443fbdcd5411159ff377",
                null,
                "oagis-id-c5e8ac8c44894e54a147a870136da686",
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_LicenseTypeCode",
                "oagis-id-608c4f0fe09d45529dfa723d5406c5a0",
                "oagis-id-7414ab2c016b40439184fcd587097a2f",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_MaritalStatusCode",
                "oagis-id-33c77e39087143f088565de1a0ebee77",
                "oagis-id-2e3438031b844929a9cf868e997a32ad",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_MatchCode",
                "oagis-id-0a1384322190497499cf2ca6b26d8934",
                "oagis-id-f35c6ed188224281a8073d355b94d44f",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_MatchDocumentCode",
                "oagis-id-f516abfd2be844d3acf481ed8b1399c3",
                "oagis-id-7c9d9573d51546f89f0b7f09566acc7a",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_PartyCategoryCode",
                "oagis-id-dbcb006c92754ed7a064ea0c8dc5940c",
                "oagis-id-f0eaec1e35124594afc96b1ed82559fa",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_MIMECode",
                "oagis-id-51ed08216606417b9b9266b464c204f7",
                null,
                "oagis-id-efcfccd8fff24243a86b8f0d432db37a",
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_PartyRoleCode",
                "oagis-id-9f78cba9c3b04b60877b2e0217f8c2fa",
                "oagis-id-3605aaeb18704d60bd2d3f57671bfaa1",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_PaymentBasisCode",
                "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855",
                "oagis-id-6ebb4957a88d403bb99dba8bc7a8018f",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_PaymentPurposeCode",
                "oagis-id-f6ae1fdc206843468def6e636ce34384",
                "oagis-id-13c5b880367d4bd3aa50abfa3ce32b27",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_PaymentMethodCode",
                "oagis-id-4c19009d16f248d9aad9deec2d4a075d",
                "oagis-id-ae52455e984342199e5c9dea82f2378a",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_PaymentSchemeCode",
                "oagis-id-985a704ce00b4f8791f436f638d85ea1",
                "oagis-id-6d8ec6bef55048bba69cfad67771a195",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_PaymentTermCode",
                "oagis-id-b9d13739f3fc4fbfa790ebd62972bcfd",
                "oagis-id-0400aa29d86742bc84546d30a6bc6f5a",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ProcessCategoryCode",
                "oagis-id-1bb1a4c4d6784b558be0f40d43404424",
                "oagis-id-87ad6a845d3349528d261df0517bf2aa",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ProcessConfirmationCode",
                "oagis-id-bb1cc4dc481a477fa2674e52e3a90cfb",
                "oagis-id-c398ee4caace4d398d80156f8362af00",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ReasonCode",
                "oagis-id-60dde524708b4b7db304686fd159d2cb",
                "oagis-id-a5e28051f66c4cd2849f3ef9288a57b4",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_RecurrencePatternCode",
                "oagis-id-472dbebea6be484b9a9e3a90612b96ce",
                "oagis-id-5b37f382148b4d64bffe7407d921ea9a",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_RemitLocationMethodCode",
                "oagis-id-c04686cc6c254869b51ea7ff39d860e5",
                "oagis-id-3bef37aca5e448d0a5dd2812c3be1b58",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ResourceTypeCode",
                "oagis-id-02d26533df40477583a3fea53f90e0e8",
                "oagis-id-00f33231374b490bbc0b4838209fa50d",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ResponseActionCode",
                "oagis-id-ecd22496e9d245e09a3851e71fe3ee18",
                "oagis-id-fd277fa2c7c74b2db8a98d6bacdd6cb5",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_ResponseCode",
                "oagis-id-73c93d58425945a0a0a6abfe4cf53274",
                "oagis-id-3be828e40dbb4386873ff1db0e38efc2",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_RiskCode",
                "oagis-id-ba592aa0ac6e4b75a4079327b9e988c4",
                "oagis-id-4f337a2efc5c409b9af300a8a359ce89",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_SalesActivityCode",
                "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6",
                "oagis-id-85c9478b650b454cb1b787b2f73c5bd3",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_SalesTaskCode",
                "oagis-id-902eea339f974afb94840f4aef4e1d8f",
                "oagis-id-ca9c8d90e3c44affa4d629e7f27b10cf",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_StateCode",
                "oagis-id-5d83970fc7a14264960da8e6c9fca3e4",
                "oagis-id-757cc95940474bdb8cd363e747481562",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_SystemEnvironmentCode",
                "oagis-id-9aa6992a44c349b5b9a2ab7dbc51e939",
                "oagis-id-ecd5459a46b64a18a8e231007a66c2ef",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_TimeZoneCode",
                "oagis-id-9aa6992a44c349b5b9a2ab7dbc51e945",
                null,
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_TaxCode",
                "oagis-id-19df69be3a5b4080a693730044647de3",
                "oagis-id-df190df7a6324876a806c1dba81abc17",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_TransferCode",
                "oagis-id-a1aef967e9644d8f92024d518b2adc09",
                "oagis-id-8fe94a19fa2f4efbab618ac7acd8f094",
                null,
                1));
        expectedCodeLists.add(new ExpectedCodeList(
                "Model\\Platform\\2_1\\Common\\CodeLists\\CodeLists_1.xsd",
                "oacl_UnitCode",
                "oagis-id-832d7dc7e6fb4020afd7edc99bb16742",
                null,
                "oagis-id-cbb8c3e6d3bb40428f8efd8639c8e222",
                1));

        assertEquals(71, expectedCodeLists.size());
        assertEquals(71, codeListRepository.count());

        Map<String, ExpectedCodeList> expectedCodeListMap =
                expectedCodeLists.stream()
                        .collect(Collectors.toMap(ExpectedCodeList::getGuid, Function.identity()));

        Map<String, CodeList> actualCodeListMap =
                StreamSupport.stream(codeListRepository.findAll().spliterator(), false)
                        .collect(Collectors.toMap(CodeList::getGuid, Function.identity()));

        actualCodeListMap.values().forEach(codeList -> {
            String guid = codeList.getGuid();
            assertTrue(expectedCodeListMap.containsKey(guid));

            ExpectedCodeList expectedCodeList = expectedCodeListMap.get(guid);
            assertEquals(expectedCodeList.getName(), codeList.getName());
            assertEquals(expectedCodeList.getModule(), codeList.getModule());
            assertEquals(expectedCodeList.getEnumTypeGuid(), codeList.getEnumTypeGuid());
            if (expectedCodeList.getBaseCodeListGuid() != null) {
                assertTrue(actualCodeListMap.containsKey(expectedCodeList.getBaseCodeListGuid()));
            }
            assertEquals(expectedCodeList.getVersionId(), Integer.parseInt(codeList.getVersionId()));

            assertNotNull(codeList.getModule());
            String module = codeList.getModule();
            String filename = module.substring(module.lastIndexOf('\\') + 1, module.length());
            switch (filename) {
                case "CodeLists_1.xsd":
                case "CodeList_ConditionTypeCode_1.xsd":
                case "CodeList_ConstraintTypeCode_1.xsd":
                case "CodeList_DateFormatCode_1.xsd":
                case "CodeList_DateTimeFormatCode_1.xsd":
                case "CodeList_TimeFormatCode_1.xsd":
                    assertEquals(agencyIdListValueRepository.findOneByValue("314").getAgencyIdListValueId(),
                            codeList.getAgencyId());
                    break;
                case "CodeList_CharacterSetCode_IANA_20070514.xsd":
                case "CodeList_MIMEMediaTypeCode_IANA_7_04.xsd":
                    assertEquals(agencyIdListValueRepository.findOneByValue("379").getAgencyIdListValueId(),
                            codeList.getAgencyId());
                    break;
                case "CodeList_CurrencyCode_ISO_7_04.xsd":
                case "CodeList_LanguageCode_ISO_7_04.xsd":
                case "CodeList_TimeZoneCode_1.xsd":
                    assertEquals(agencyIdListValueRepository.findOneByValue("5").getAgencyIdListValueId(),
                            codeList.getAgencyId());
                    break;
                case "CodeList_UnitCode_UNECE_7_04.xsd":
                    assertEquals(agencyIdListValueRepository.findOneByValue("6").getAgencyIdListValueId(),
                            codeList.getAgencyId());
                    break;
                default:
                    throw new AssertionError("Unknown module: " + filename);
            }

            assertNull(codeList.getRemark());
            assertTrue(codeList.isExtensibleIndicator());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), codeList.getCreatedBy());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), codeList.getLastUpdatedBy());
            assertEquals("Published", codeList.getState());
        });
    }

    private class ExpectedCodeListValue {
        private String enumTypeGuid;
        private String value;
        private String baseCodeListGuid;

        public ExpectedCodeListValue(String enumTypeGuid, String value, String baseCodeListGuid) {
            this.enumTypeGuid = enumTypeGuid;
            this.value = value;
            this.baseCodeListGuid = baseCodeListGuid;
        }

        public String getEnumTypeGuid() {
            return enumTypeGuid;
        }

        public String getValue() {
            return value;
        }

        public String getBaseCodeListGuid() {
            return baseCodeListGuid;
        }
    }

    @Test
    public void test_PopulateCodeListValueTable() {
        List<ExpectedCodeListValue> expectedCodeListValues = Arrays.asList(
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "Add", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "Change", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "Delete", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "Replace", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "UpSert", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "Accepted", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "Modified", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-f63f1d0aa3c34e7cab8efee204f61cf7", "Rejected", "oagis-id-49c1788460864e99b0872d0a6e58bddb"),
                new ExpectedCodeListValue("oagis-id-1cfb7a2e0cc04586a6732f3871d670ec", "Customer", "oagis-id-a9ee4e537bde4e2ba96fa1d0aa04dba7"),
                new ExpectedCodeListValue("oagis-id-1cfb7a2e0cc04586a6732f3871d670ec", "Supplier", "oagis-id-a9ee4e537bde4e2ba96fa1d0aa04dba7"),
                new ExpectedCodeListValue("oagis-id-1cfb7a2e0cc04586a6732f3871d670ec", "Manufacturer", "oagis-id-a9ee4e537bde4e2ba96fa1d0aa04dba7"),
                new ExpectedCodeListValue("oagis-id-1cfb7a2e0cc04586a6732f3871d670ec", "Broker", "oagis-id-a9ee4e537bde4e2ba96fa1d0aa04dba7"),
                new ExpectedCodeListValue("oagis-id-1cfb7a2e0cc04586a6732f3871d670ec", "Carrier", "oagis-id-a9ee4e537bde4e2ba96fa1d0aa04dba7"),
                new ExpectedCodeListValue("oagis-id-a355da7ff7e444aa8b27a4d3323eba08", "OUR", "oagis-id-c6716c40af4d4f0c926b975dd5918270"),
                new ExpectedCodeListValue("oagis-id-a355da7ff7e444aa8b27a4d3323eba08", "BEN", "oagis-id-c6716c40af4d4f0c926b975dd5918270"),
                new ExpectedCodeListValue("oagis-id-a355da7ff7e444aa8b27a4d3323eba08", "SHA", "oagis-id-c6716c40af4d4f0c926b975dd5918270"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "MLDB", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "MLCD", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "MLFA", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "CRDB", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "CRCD", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "CRFA", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "PUDB", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "PUCD", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "PUFA", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "RGDB", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "RGCD", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-e7eeecddd9544535a9a89fc1276bb494", "RGFA", "oagis-id-08040659d66c4591a1353cc431c50c4a"),
                new ExpectedCodeListValue("oagis-id-b8553ba514c64ad68b49bd5f83a6ef72", "CCHQ", "oagis-id-25f1e4ef171045f0be4e9e6785dbbb70"),
                new ExpectedCodeListValue("oagis-id-b8553ba514c64ad68b49bd5f83a6ef72", "CCCH", "oagis-id-25f1e4ef171045f0be4e9e6785dbbb70"),
                new ExpectedCodeListValue("oagis-id-b8553ba514c64ad68b49bd5f83a6ef72", "BCHQ", "oagis-id-25f1e4ef171045f0be4e9e6785dbbb70"),
                new ExpectedCodeListValue("oagis-id-b8553ba514c64ad68b49bd5f83a6ef72", "DFFT", "oagis-id-25f1e4ef171045f0be4e9e6785dbbb70"),
                new ExpectedCodeListValue("oagis-id-b8553ba514c64ad68b49bd5f83a6ef72", "ELDR", "oagis-id-25f1e4ef171045f0be4e9e6785dbbb70"),
                new ExpectedCodeListValue("oagis-id-7ec2195e70294a13bfb9414de9f14473", "Always", "oagis-id-a19867f6ebe5426d99fe4808b11e23c4"),
                new ExpectedCodeListValue("oagis-id-7ec2195e70294a13bfb9414de9f14473", "OnError", "oagis-id-a19867f6ebe5426d99fe4808b11e23c4"),
                new ExpectedCodeListValue("oagis-id-7ec2195e70294a13bfb9414de9f14473", "OnModification", "oagis-id-a19867f6ebe5426d99fe4808b11e23c4"),
                new ExpectedCodeListValue("oagis-id-7ec2195e70294a13bfb9414de9f14473", "OnRejection", "oagis-id-a19867f6ebe5426d99fe4808b11e23c4"),
                new ExpectedCodeListValue("oagis-id-7ec2195e70294a13bfb9414de9f14473", "Never", "oagis-id-a19867f6ebe5426d99fe4808b11e23c4"),
                new ExpectedCodeListValue("oagis-id-fc8d4b561faa47ff94fffdeb12566aea", "Home", "oagis-id-6233008ff5874c15a76000e186e0130d"),
                new ExpectedCodeListValue("oagis-id-fc8d4b561faa47ff94fffdeb12566aea", "Work", "oagis-id-6233008ff5874c15a76000e186e0130d"),
                new ExpectedCodeListValue("oagis-id-64ffe9653e934bee80d9051818bf3b94", "Completeness", "oagis-id-46dded6e75894d85a7e772090cb42c5b"),
                new ExpectedCodeListValue("oagis-id-64ffe9653e934bee80d9051818bf3b94", "Existence or Occurance", "oagis-id-46dded6e75894d85a7e772090cb42c5b"),
                new ExpectedCodeListValue("oagis-id-64ffe9653e934bee80d9051818bf3b94", "Presentation and Disclosure", "oagis-id-46dded6e75894d85a7e772090cb42c5b"),
                new ExpectedCodeListValue("oagis-id-64ffe9653e934bee80d9051818bf3b94", "Rights and Obligations", "oagis-id-46dded6e75894d85a7e772090cb42c5b"),
                new ExpectedCodeListValue("oagis-id-64ffe9653e934bee80d9051818bf3b94", "Valuation or Measurement", "oagis-id-46dded6e75894d85a7e772090cb42c5b"),
                new ExpectedCodeListValue("oagis-id-58a748fc51eb421a862db5fcfcf85c3b", "Trigger", "oagis-id-f01cb1632cbd4ae89cf1b9841c40e2f0"),
                new ExpectedCodeListValue("oagis-id-58a748fc51eb421a862db5fcfcf85c3b", "Workflow", "oagis-id-f01cb1632cbd4ae89cf1b9841c40e2f0"),
                new ExpectedCodeListValue("oagis-id-58a748fc51eb421a862db5fcfcf85c3b", "Measurement", "oagis-id-f01cb1632cbd4ae89cf1b9841c40e2f0"),
                new ExpectedCodeListValue("oagis-id-af274f44caad4162804061f2342a3715", "Risk Assessment", "oagis-id-be54304dfda44246aa554217f5be5124"),
                new ExpectedCodeListValue("oagis-id-af274f44caad4162804061f2342a3715", "Monitoring", "oagis-id-be54304dfda44246aa554217f5be5124"),
                new ExpectedCodeListValue("oagis-id-af274f44caad4162804061f2342a3715", "Control Environment", "oagis-id-be54304dfda44246aa554217f5be5124"),
                new ExpectedCodeListValue("oagis-id-af274f44caad4162804061f2342a3715", "Control Activities", "oagis-id-be54304dfda44246aa554217f5be5124"),
                new ExpectedCodeListValue("oagis-id-af274f44caad4162804061f2342a3715", "Information and Communication", "oagis-id-be54304dfda44246aa554217f5be5124"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "CASH", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "CORT", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "DIVI", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "GOVT", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "HEDG", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "INTC", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "INTE", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "LOAN", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "PENS", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "SALA", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "SECU", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "SSBE", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "SUPP", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "TAXS", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "TRAD", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "TREA", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-5482a811ea5047d38ea6082e02b076b0", "VATX", "oagis-id-e64591fa9a2a46009b0e1142710d8793"),
                new ExpectedCodeListValue("oagis-id-8314d4295c1b48c3864a47504e464952", "Sunday", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"),
                new ExpectedCodeListValue("oagis-id-8314d4295c1b48c3864a47504e464952", "Monday", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"),
                new ExpectedCodeListValue("oagis-id-8314d4295c1b48c3864a47504e464952", "Tuesday", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"),
                new ExpectedCodeListValue("oagis-id-8314d4295c1b48c3864a47504e464952", "Wednesday", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"),
                new ExpectedCodeListValue("oagis-id-8314d4295c1b48c3864a47504e464952", "Thursday", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"),
                new ExpectedCodeListValue("oagis-id-8314d4295c1b48c3864a47504e464952", "Friday", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"),
                new ExpectedCodeListValue("oagis-id-8314d4295c1b48c3864a47504e464952", "Saturday", "oagis-id-ed55866d4a3e4c9eb00d0928e14cbbf4"),
                new ExpectedCodeListValue("oagis-id-ca797f68a80c4475a4e67713ca79db8d", "Debit", "oagis-id-69e1317dc2f24edcb1847f34f66be977"),
                new ExpectedCodeListValue("oagis-id-ca797f68a80c4475a4e67713ca79db8d", "Credit", "oagis-id-69e1317dc2f24edcb1847f34f66be977"),
                new ExpectedCodeListValue("oagis-id-9d182f28e99840318c9757dab3177019", "HTML", "oagis-id-428bb44db16241faa4813f779ffff26e"),
                new ExpectedCodeListValue("oagis-id-9d182f28e99840318c9757dab3177019", "Rich Text", "oagis-id-428bb44db16241faa4813f779ffff26e"),
                new ExpectedCodeListValue("oagis-id-9d182f28e99840318c9757dab3177019", "Plain Text", "oagis-id-428bb44db16241faa4813f779ffff26e"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "amendment", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "analysis", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "cancellation", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "deliveryChange", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "designChange", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "design", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "mockUpCreation", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "protoypeBuilding", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "rectification", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "restructuring", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "sparePartCreation", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "stopNotice", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-10eb52c5181f419ea5f52cf501d86708", "testing", "oagis-id-8deb90f1405a4a7e98caa6817b9b2ef1"),
                new ExpectedCodeListValue("oagis-id-aa71419c1d7842819c3baaa28926393c", "designDeviationPermit", "oagis-id-97ffe643788648c096ea981c32c2a3dd"),
                new ExpectedCodeListValue("oagis-id-aa71419c1d7842819c3baaa28926393c", "designRelease", "oagis-id-97ffe643788648c096ea981c32c2a3dd"),
                new ExpectedCodeListValue("oagis-id-aa71419c1d7842819c3baaa28926393c", "managementResolution", "oagis-id-97ffe643788648c096ea981c32c2a3dd"),
                new ExpectedCodeListValue("oagis-id-aa71419c1d7842819c3baaa28926393c", "manufacturingRelease", "oagis-id-97ffe643788648c096ea981c32c2a3dd"),
                new ExpectedCodeListValue("oagis-id-aa71419c1d7842819c3baaa28926393c", "productionDeviationPermit", "oagis-id-97ffe643788648c096ea981c32c2a3dd"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "ChangeOfStandard", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "CostReduction", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "CustomerRejection", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "CustomerRequest", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "DurabilityImprovement", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "GovernmentRegulation", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "ProcurementAlignment", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "ProductionAlignment", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "ProductionRelief", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "ProductionRequirement", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "QualityImprovement", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "SecuriyReason", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "Standardization", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "SupplierRequest", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "TechnicalImprovement", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-482e2957e4bc4346ad1f94d2f3c56bb5", "ToolImprovement", "oagis-id-cc80f54e299048859c47c0046f7c7e36"),
                new ExpectedCodeListValue("oagis-id-a0d4ca19498b42a1af3219d8e6dd4213", "inWork", "oagis-id-0073c51b321e42e795d1b48116a7620f"),
                new ExpectedCodeListValue("oagis-id-a0d4ca19498b42a1af3219d8e6dd4213", "issued", "oagis-id-0073c51b321e42e795d1b48116a7620f"),
                new ExpectedCodeListValue("oagis-id-ff46417e01b640a78776bd03ec58fc8f", "CHQB", "oagis-id-1dd922a4b7e348e7af93870e7ab75d4c"),
                new ExpectedCodeListValue("oagis-id-ff46417e01b640a78776bd03ec58fc8f", "HOLD", "oagis-id-1dd922a4b7e348e7af93870e7ab75d4c"),
                new ExpectedCodeListValue("oagis-id-ff46417e01b640a78776bd03ec58fc8f", "PHOB", "oagis-id-1dd922a4b7e348e7af93870e7ab75d4c"),
                new ExpectedCodeListValue("oagis-id-ff46417e01b640a78776bd03ec58fc8f", "TELB", "oagis-id-1dd922a4b7e348e7af93870e7ab75d4c"),
                new ExpectedCodeListValue("oagis-id-54bb202e31294a098faf49ba2767c4bc", "Male", "oagis-id-58bcc34f61284f4580e706506453fc3d"),
                new ExpectedCodeListValue("oagis-id-54bb202e31294a098faf49ba2767c4bc", "Female", "oagis-id-58bcc34f61284f4580e706506453fc3d"),
                new ExpectedCodeListValue("oagis-id-54bb202e31294a098faf49ba2767c4bc", "Unknown", "oagis-id-58bcc34f61284f4580e706506453fc3d"),
                new ExpectedCodeListValue("oagis-id-7414ab2c016b40439184fcd587097a2f", "Import", "oagis-id-608c4f0fe09d45529dfa723d5406c5a0"),
                new ExpectedCodeListValue("oagis-id-7414ab2c016b40439184fcd587097a2f", "Export", "oagis-id-608c4f0fe09d45529dfa723d5406c5a0"),
                new ExpectedCodeListValue("oagis-id-2e3438031b844929a9cf868e997a32ad", "Divorced", "oagis-id-33c77e39087143f088565de1a0ebee77"),
                new ExpectedCodeListValue("oagis-id-2e3438031b844929a9cf868e997a32ad", "Married", "oagis-id-33c77e39087143f088565de1a0ebee77"),
                new ExpectedCodeListValue("oagis-id-2e3438031b844929a9cf868e997a32ad", "NeverMarried", "oagis-id-33c77e39087143f088565de1a0ebee77"),
                new ExpectedCodeListValue("oagis-id-2e3438031b844929a9cf868e997a32ad", "Separated", "oagis-id-33c77e39087143f088565de1a0ebee77"),
                new ExpectedCodeListValue("oagis-id-2e3438031b844929a9cf868e997a32ad", "SignificantOther", "oagis-id-33c77e39087143f088565de1a0ebee77"),
                new ExpectedCodeListValue("oagis-id-2e3438031b844929a9cf868e997a32ad", "Widowed", "oagis-id-33c77e39087143f088565de1a0ebee77"),
                new ExpectedCodeListValue("oagis-id-2e3438031b844929a9cf868e997a32ad", "Unknown", "oagis-id-33c77e39087143f088565de1a0ebee77"),
                new ExpectedCodeListValue("oagis-id-f35c6ed188224281a8073d355b94d44f", "2", "oagis-id-0a1384322190497499cf2ca6b26d8934"),
                new ExpectedCodeListValue("oagis-id-f35c6ed188224281a8073d355b94d44f", "3", "oagis-id-0a1384322190497499cf2ca6b26d8934"),
                new ExpectedCodeListValue("oagis-id-f35c6ed188224281a8073d355b94d44f", "4", "oagis-id-0a1384322190497499cf2ca6b26d8934"),
                new ExpectedCodeListValue("oagis-id-7c9d9573d51546f89f0b7f09566acc7a", "Invoice", "oagis-id-f516abfd2be844d3acf481ed8b1399c3"),
                new ExpectedCodeListValue("oagis-id-7c9d9573d51546f89f0b7f09566acc7a", "Purchase Order", "oagis-id-f516abfd2be844d3acf481ed8b1399c3"),
                new ExpectedCodeListValue("oagis-id-7c9d9573d51546f89f0b7f09566acc7a", "Receipt", "oagis-id-f516abfd2be844d3acf481ed8b1399c3"),
                new ExpectedCodeListValue("oagis-id-7c9d9573d51546f89f0b7f09566acc7a", "Inspection", "oagis-id-f516abfd2be844d3acf481ed8b1399c3"),
                new ExpectedCodeListValue("oagis-id-f0eaec1e35124594afc96b1ed82559fa", "Organization", "oagis-id-dbcb006c92754ed7a064ea0c8dc5940c"),
                new ExpectedCodeListValue("oagis-id-f0eaec1e35124594afc96b1ed82559fa", "Individual", "oagis-id-dbcb006c92754ed7a064ea0c8dc5940c"),
                new ExpectedCodeListValue("oagis-id-6ebb4957a88d403bb99dba8bc7a8018f", "InvoiceDate", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"),
                new ExpectedCodeListValue("oagis-id-6ebb4957a88d403bb99dba8bc7a8018f", "ShippingDate", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"),
                new ExpectedCodeListValue("oagis-id-6ebb4957a88d403bb99dba8bc7a8018f", "DeliveryDate", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"),
                new ExpectedCodeListValue("oagis-id-6ebb4957a88d403bb99dba8bc7a8018f", "PurchaseOrderDate", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"),
                new ExpectedCodeListValue("oagis-id-6ebb4957a88d403bb99dba8bc7a8018f", "ReceiptOfGoodsDate", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"),
                new ExpectedCodeListValue("oagis-id-6ebb4957a88d403bb99dba8bc7a8018f", "AcceptanceOfGoodsDate", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"),
                new ExpectedCodeListValue("oagis-id-6ebb4957a88d403bb99dba8bc7a8018f", "AcceptanceOfOrderDate", "oagis-id-7e62e27dbcc94a9d9667ddcf219e2855"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "ADVA", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "AGRT", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "ALMY", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "BECH", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "BENE", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "BONU", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "CASH", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "CBFF", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "CHAR", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "CMDT", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "COLL", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "COMC", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "COMM", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "CONS", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "COST", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "CPYR", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "DBTC", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "DIVI", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "FREX", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "GDDS", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "GOVT", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "HEDG", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "IHRP", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "INSU", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "INTC", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "INTE", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "LICF", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "LOAN", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "LOAR", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "NETT", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "PAYR", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "PENS", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "REFU", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "RENT", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "ROYA", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "SALA", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "SCVE", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "SECU", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "SSBE", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "SUBS", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "TAXS", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "TREA", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "VATX", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-13c5b880367d4bd3aa50abfa3ce32b27", "VENP", "oagis-id-f6ae1fdc206843468def6e636ce34384"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "Cash", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "Check", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "CreditCard", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "DebitCard", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "ElectronicFundsTransfer", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "ProcurementCard", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "BankDraft", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "PurchaseOrder", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-ae52455e984342199e5c9dea82f2378a", "CreditTransfer", "oagis-id-4c19009d16f248d9aad9deec2d4a075d"),
                new ExpectedCodeListValue("oagis-id-6d8ec6bef55048bba69cfad67771a195", "ACH", "oagis-id-985a704ce00b4f8791f436f638d85ea1"),
                new ExpectedCodeListValue("oagis-id-6d8ec6bef55048bba69cfad67771a195", "RTGS", "oagis-id-985a704ce00b4f8791f436f638d85ea1"),
                new ExpectedCodeListValue("oagis-id-6d8ec6bef55048bba69cfad67771a195", "Fednet", "oagis-id-985a704ce00b4f8791f436f638d85ea1"),
                new ExpectedCodeListValue("oagis-id-6d8ec6bef55048bba69cfad67771a195", "CHIPS", "oagis-id-985a704ce00b4f8791f436f638d85ea1"),
                new ExpectedCodeListValue("oagis-id-0400aa29d86742bc84546d30a6bc6f5a", "Net20", "oagis-id-b9d13739f3fc4fbfa790ebd62972bcfd"),
                new ExpectedCodeListValue("oagis-id-0400aa29d86742bc84546d30a6bc6f5a", "Net30", "oagis-id-b9d13739f3fc4fbfa790ebd62972bcfd"),
                new ExpectedCodeListValue("oagis-id-0400aa29d86742bc84546d30a6bc6f5a", "Net45", "oagis-id-b9d13739f3fc4fbfa790ebd62972bcfd"),
                new ExpectedCodeListValue("oagis-id-0400aa29d86742bc84546d30a6bc6f5a", "Net60", "oagis-id-b9d13739f3fc4fbfa790ebd62972bcfd"),
                new ExpectedCodeListValue("oagis-id-0400aa29d86742bc84546d30a6bc6f5a", "10Percent30", "oagis-id-b9d13739f3fc4fbfa790ebd62972bcfd"),
                new ExpectedCodeListValue("oagis-id-87ad6a845d3349528d261df0517bf2aa", "Routine", "oagis-id-1bb1a4c4d6784b558be0f40d43404424"),
                new ExpectedCodeListValue("oagis-id-87ad6a845d3349528d261df0517bf2aa", "Non-Routine", "oagis-id-1bb1a4c4d6784b558be0f40d43404424"),
                new ExpectedCodeListValue("oagis-id-87ad6a845d3349528d261df0517bf2aa", "Estimating", "oagis-id-1bb1a4c4d6784b558be0f40d43404424"),
                new ExpectedCodeListValue("oagis-id-c398ee4caace4d398d80156f8362af00", "OnReceipt", "oagis-id-bb1cc4dc481a477fa2674e52e3a90cfb"),
                new ExpectedCodeListValue("oagis-id-c398ee4caace4d398d80156f8362af00", "OnValidation", "oagis-id-bb1cc4dc481a477fa2674e52e3a90cfb"),
                new ExpectedCodeListValue("oagis-id-c398ee4caace4d398d80156f8362af00", "OnCompletion", "oagis-id-bb1cc4dc481a477fa2674e52e3a90cfb"),
                new ExpectedCodeListValue("oagis-id-c398ee4caace4d398d80156f8362af00", "OnAll", "oagis-id-bb1cc4dc481a477fa2674e52e3a90cfb"),
                new ExpectedCodeListValue("oagis-id-5b37f382148b4d64bffe7407d921ea9a", "Daily", "oagis-id-472dbebea6be484b9a9e3a90612b96ce"),
                new ExpectedCodeListValue("oagis-id-5b37f382148b4d64bffe7407d921ea9a", "Weekly", "oagis-id-472dbebea6be484b9a9e3a90612b96ce"),
                new ExpectedCodeListValue("oagis-id-5b37f382148b4d64bffe7407d921ea9a", "Monthly", "oagis-id-472dbebea6be484b9a9e3a90612b96ce"),
                new ExpectedCodeListValue("oagis-id-5b37f382148b4d64bffe7407d921ea9a", "Yearly", "oagis-id-472dbebea6be484b9a9e3a90612b96ce"),
                new ExpectedCodeListValue("oagis-id-3bef37aca5e448d0a5dd2812c3be1b58", "FAX", "oagis-id-c04686cc6c254869b51ea7ff39d860e5"),
                new ExpectedCodeListValue("oagis-id-3bef37aca5e448d0a5dd2812c3be1b58", "EDI", "oagis-id-c04686cc6c254869b51ea7ff39d860e5"),
                new ExpectedCodeListValue("oagis-id-3bef37aca5e448d0a5dd2812c3be1b58", "URI", "oagis-id-c04686cc6c254869b51ea7ff39d860e5"),
                new ExpectedCodeListValue("oagis-id-3bef37aca5e448d0a5dd2812c3be1b58", "EML", "oagis-id-c04686cc6c254869b51ea7ff39d860e5"),
                new ExpectedCodeListValue("oagis-id-3bef37aca5e448d0a5dd2812c3be1b58", "PST", "oagis-id-c04686cc6c254869b51ea7ff39d860e5"),
                new ExpectedCodeListValue("oagis-id-fd277fa2c7c74b2db8a98d6bacdd6cb5", "Accepted", "oagis-id-ecd22496e9d245e09a3851e71fe3ee18"),
                new ExpectedCodeListValue("oagis-id-fd277fa2c7c74b2db8a98d6bacdd6cb5", "Modified", "oagis-id-ecd22496e9d245e09a3851e71fe3ee18"),
                new ExpectedCodeListValue("oagis-id-fd277fa2c7c74b2db8a98d6bacdd6cb5", "Rejected", "oagis-id-ecd22496e9d245e09a3851e71fe3ee18"),
                new ExpectedCodeListValue("oagis-id-3be828e40dbb4386873ff1db0e38efc2", "Always", "oagis-id-73c93d58425945a0a0a6abfe4cf53274"),
                new ExpectedCodeListValue("oagis-id-3be828e40dbb4386873ff1db0e38efc2", "OnError", "oagis-id-73c93d58425945a0a0a6abfe4cf53274"),
                new ExpectedCodeListValue("oagis-id-3be828e40dbb4386873ff1db0e38efc2", "OnModification", "oagis-id-73c93d58425945a0a0a6abfe4cf53274"),
                new ExpectedCodeListValue("oagis-id-3be828e40dbb4386873ff1db0e38efc2", "OnRejection", "oagis-id-73c93d58425945a0a0a6abfe4cf53274"),
                new ExpectedCodeListValue("oagis-id-3be828e40dbb4386873ff1db0e38efc2", "Never", "oagis-id-73c93d58425945a0a0a6abfe4cf53274"),
                new ExpectedCodeListValue("oagis-id-4f337a2efc5c409b9af300a8a359ce89", "Compliance with applicable laws and regulations", "oagis-id-ba592aa0ac6e4b75a4079327b9e988c4"),
                new ExpectedCodeListValue("oagis-id-4f337a2efc5c409b9af300a8a359ce89", "Effectiveness and efficiency of operations", "oagis-id-ba592aa0ac6e4b75a4079327b9e988c4"),
                new ExpectedCodeListValue("oagis-id-4f337a2efc5c409b9af300a8a359ce89", "Reliability of Financial Statements", "oagis-id-ba592aa0ac6e4b75a4079327b9e988c4"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "LiteratureRequest", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "NewLead", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "DeadContent", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "TrafficReport", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "Sold", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "EMail", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "Letter", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-85c9478b650b454cb1b787b2f73c5bd3", "Fax", "oagis-id-4ecd86e940f44dc7bc53eee193cc2fb6"),
                new ExpectedCodeListValue("oagis-id-ca9c8d90e3c44affa4d629e7f27b10cf", "Meeting", "oagis-id-902eea339f974afb94840f4aef4e1d8f"),
                new ExpectedCodeListValue("oagis-id-ca9c8d90e3c44affa4d629e7f27b10cf", "ConferenceCall", "oagis-id-902eea339f974afb94840f4aef4e1d8f"),
                new ExpectedCodeListValue("oagis-id-ca9c8d90e3c44affa4d629e7f27b10cf", "FollowUp", "oagis-id-902eea339f974afb94840f4aef4e1d8f"),
                new ExpectedCodeListValue("oagis-id-ca9c8d90e3c44affa4d629e7f27b10cf", "EMail", "oagis-id-902eea339f974afb94840f4aef4e1d8f"),
                new ExpectedCodeListValue("oagis-id-ecd5459a46b64a18a8e231007a66c2ef", "Production", "oagis-id-9aa6992a44c349b5b9a2ab7dbc51e939"),
                new ExpectedCodeListValue("oagis-id-ecd5459a46b64a18a8e231007a66c2ef", "Test", "oagis-id-9aa6992a44c349b5b9a2ab7dbc51e939"),
                new ExpectedCodeListValue("oagis-id-8fe94a19fa2f4efbab618ac7acd8f094", "Complete", "oagis-id-a1aef967e9644d8f92024d518b2adc09"),
                new ExpectedCodeListValue("oagis-id-8fe94a19fa2f4efbab618ac7acd8f094", "Return", "oagis-id-a1aef967e9644d8f92024d518b2adc09"),

                new ExpectedCodeListValue(null, "M0100", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0100EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0200", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0300", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0300BR", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0300GL", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0300US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0330US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0400", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0400BR", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0400CL", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0400GL", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0400PY", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0400US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0500", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0500BR", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0500CU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0500US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0600", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0600CL", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0600US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0700", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0700US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0800", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0800US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0830", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0900", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M0900US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M1000", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M1000US", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M1100", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "M1200", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0000", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0000EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0100", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0100EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0100NA", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0200", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0200EG", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0200EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0200IL", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0200JO", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0200SY", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0300", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0300EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0300IQ", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0330IR", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0400", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0400EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0430", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0500", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0500EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0530", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0545", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0600", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0600EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0630", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0700", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0700EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0800", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0800EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0900", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0900EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0930", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P0930AUS", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1000", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1000AUS", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1030AUS", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1000EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1100", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1100EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1200", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1200EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1200NZ", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1245NZ", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1300", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1300EU", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),
                new ExpectedCodeListValue(null, "P1400", "oagis-id-44ec8e62b49c44a997637034a5be4f2e"),

                new ExpectedCodeListValue(null, "YYYY-MM-DDThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DDThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-MM-DD", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DDThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "--MM-DD", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DDThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "---DD", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDDThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-DDD", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDDThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-DDD", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-DThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "YYYY-Www-D", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-DThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-Www-D", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh:mm:ss", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh:mm:ss+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh:mm:ssZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh:mmZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh:mm+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThhZ", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-DThh+hh:mm", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),
                new ExpectedCodeListValue(null, "-W-D", "oagis-id-0b7cbca16b6741b58fb6920861ddd433"),

                new ExpectedCodeListValue(null, "hh:mm:ss", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hh:mm:ss+hh:mm", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hh:mm:ssZ", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hh:mm", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hh:mmZ", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hh:mm+hh:mm", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hh", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hhZ", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "hh+hh:mm", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "-mm:ss", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "-mm", "oagis-id-98f48609a5154608bd5673aea57784da"),
                new ExpectedCodeListValue(null, "--ss", "oagis-id-98f48609a5154608bd5673aea57784da"),
                
                new ExpectedCodeListValue(null, "YYYY-MM-DD", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "YYYY-MM", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "YYYY", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "YY", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "--MM-DD", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "--MM--", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "---DD", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "YYYY-DDD", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "-DDD", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "YYYY-Www-D", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "-Www-D", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "YYYY-Www", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "-Www", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),
                new ExpectedCodeListValue(null, "-W-D", "oagis-id-46fc137ae5b44dde9ab25724a2abec86"),

                new ExpectedCodeListValue(null, "AED", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "AFN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ALL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "AMD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ANG", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "AOA", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ARS", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "AUD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "AWG", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "AZM", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BAM", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BBD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BDT", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BGN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BHD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BIF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BMD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BND", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BOB", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BRL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BSD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BTN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BWP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BYR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "BZD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CAD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CDF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CHF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CLP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CNY", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "COP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CRC", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CUP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CVE", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CYP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "CZK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "DJF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "DKK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "DOP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "DZD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "EEK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "EGP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ERN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ETB", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "EUR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "FJD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "FKP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GBP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GEL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GHC", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GIP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GMD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GNF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GTQ", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "GYD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "HKD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "HNL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "HRK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "HTG", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "HUF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "IDR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ILS", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "INR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "IQD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "IRR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ISK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "JMD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "JOD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "JPY", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KES", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KGS", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KHR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KMF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KPW", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KRW", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KWD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KYD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "KZT", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LAK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LBP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LKR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LRD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LSL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LTL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LVL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "LYD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MAD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MDL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MGF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MKD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MMK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MNT", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MOP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MRO", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MTL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MUR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MVR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MWK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MXN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MYR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "MZM", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "NAD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "NGN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "NIO", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "NOK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "NPR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "NZD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "OMR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "PAB", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "PEN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "PGK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "PHP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "PKR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "PLN", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "PYG", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "QAR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ROL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "RON", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "RUB", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "RWF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SAR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SBD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SCR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SDD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SEK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SGD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SHP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SIT", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SKK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SLL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SOS", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SRG", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "STD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SVC", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SYP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "SZL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "THB", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TJS", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TMM", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TND", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TOP", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TRL", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TRY", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TTD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TWD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "TZS", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "UAH", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "UGX", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "USD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "UYU", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "UZS", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "VEB", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "VEF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "VND", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "VUV", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "WST", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XAF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XAG", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XAU", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XCD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XDR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XOF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XPD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XPF", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "XPT", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "YER", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "YUM", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ZAR", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ZMK", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),
                new ExpectedCodeListValue(null, "ZWD", "oagis-id-b219e03523e04d1fb3379d37001c8f0c"),

                new ExpectedCodeListValue(null, "PRE", "oagis-id-b7e16bb124eb4ddbb86d4861383fe372"),
                new ExpectedCodeListValue(null, "POST", "oagis-id-b7e16bb124eb4ddbb86d4861383fe372"),
                new ExpectedCodeListValue(null, "INV", "oagis-id-b7e16bb124eb4ddbb86d4861383fe372"),

                new ExpectedCodeListValue(null, "UNS", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "OCL", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "SBVR", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "XSRE", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "ECL", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "CHIP", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "CAT", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "BON", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "NCL", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "PERL", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),
                new ExpectedCodeListValue(null, "SCR", "oagis-id-e58cd86fc54b442a9b9bd10d20ebd79b"),

                new ExpectedCodeListValue(null, "3", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "4", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "5", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "6", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "7", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "8", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "9", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "10", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "11", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "12", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "13", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "14", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "15", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "16", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "17", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "18", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "19", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "20", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "21", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "22", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "23", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "24", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "25", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "26", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "27", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "28", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "29", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "30", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "31", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "32", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "33", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "34", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "35", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "36", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "37", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "38", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "39", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "40", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "41", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "42", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "43", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "44", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "45", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "46", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "47", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "48", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "49", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "50", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "51", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "52", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "53", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "54", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "55", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "56", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "57", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "58", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "59", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "60", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "61", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "62", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "63", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "64", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "65", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "66", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "67", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "68", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "69", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "70", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "71", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "72", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "73", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "74", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "75", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "76", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "77", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "78", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "79", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "80", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "81", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "82", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "83", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "84", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "85", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "86", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "87", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "88", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "89", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "90", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "91", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "92", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "93", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "94", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "95", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "96", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "97", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "98", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "99", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "100", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "101", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "102", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "103", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "104", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "105", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "106", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "109", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "110", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "111", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "112", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "113", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "114", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "115", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "116", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "117", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "118", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "119", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1000", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1001", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1002", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1003", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1004", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1005", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1006", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1007", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1008", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1009", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1010", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1011", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1012", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1013", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1014", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1015", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1016", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1017", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1018", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1019", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "1020", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2000", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2001", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2002", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2003", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2004", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2005", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2006", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2007", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2008", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2009", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2013", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2013", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2014", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2015", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2016", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2017", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2018", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2019", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2020", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2021", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2022", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2023", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2024", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2025", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2026", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2027", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2028", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2029", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2030", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2031", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2032", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2033", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2034", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2035", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2036", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2037", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2038", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2039", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2040", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2041", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2042", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2043", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2011", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2044", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2045", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2010", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2046", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2047", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2048", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2049", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2050", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2051", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2052", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2053", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2054", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2055", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2056", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2057", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2058", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2059", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2060", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2061", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2062", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2063", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2064", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2065", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2066", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2067", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2068", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2069", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2070", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2071", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2072", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2073", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2074", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2075", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2076", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2077", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2078", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2079", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2080", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2081", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2082", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2083", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2084", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2085", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2086", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2087", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2088", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2089", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2090", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2091", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2092", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2093", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2094", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2095", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2096", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2097", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2098", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2099", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2100", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2101", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2102", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2103", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2104", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2105", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2106", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2107", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2250", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2251", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2252", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2253", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2254", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2255", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2256", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2257", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2258", "oagis-id-369bb5cf4149492e84fb5b070a98a12b"),
                new ExpectedCodeListValue(null, "2259", "oagis-id-8274b2e56a8941a6a235bb1faf227e74")
        );

        assertEquals(877, expectedCodeListValues.size());
        assertEquals(877, codeListValueRepository.count());

        expectedCodeListValues.forEach(expectedCodeListValue -> {
            CodeList codeList = codeListRepository.findOneByGuid(expectedCodeListValue.getBaseCodeListGuid());
            assertNotNull(codeList);
            assertEquals(expectedCodeListValue.getEnumTypeGuid(), codeList.getEnumTypeGuid());

            CodeListValue codeListValue =
                    codeListValueRepository.findOneByCodeListIdAndValue(codeList.getCodeListId(), expectedCodeListValue.getValue());
            assertNotNull(codeListValue);
        });
    }
}
