package org.oagi.srt.persistence.populate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.CodeListRepository;
import org.oagi.srt.repository.CodeListValueRepository;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.CodeList;
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
                "oagis-id-369bb5cf4149492e84fb5b070a98a12b",
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
                "oagis-id-0b7cbca16b6741b58fb6920861ddd433",
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
                "oagis-id-44ec8e62b49c44a997637034a5be4f2e",
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
                "oagis-id-369bb5cf4149492e84fb5b070a98a12b",
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
                "oagis-id-44ec8e62b49c44a997637034a5be4f2e",
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
            assertEquals(userRepository.findOneByLoginId("oagis").getAppUserId(), codeList.getCreatedBy());
            assertEquals(userRepository.findOneByLoginId("oagis").getAppUserId(), codeList.getLastUpdatedBy());
            assertEquals("Published", codeList.getState());
        });
    }

    @Test
    public void test_PopulateCodeListValueTable() {
        assertEquals(877, codeListValueRepository.count());
    }
}
