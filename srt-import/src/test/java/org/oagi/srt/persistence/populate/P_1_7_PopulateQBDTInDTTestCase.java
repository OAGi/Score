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
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(ImportApplication.class)
@Transactional(readOnly = true)
public class P_1_7_PopulateQBDTInDTTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtSCAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtSCAwdXpsTypeMapRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    private class ExpectedContentType {
        private String qualifier;
        private String guid;
        private String codeListName;

        public ExpectedContentType(String qualifier, String guid, String codeListName) {
            this.qualifier = qualifier;
            this.guid = guid;
            this.codeListName = codeListName;
        }

        public String getQualifier() {
            return qualifier;
        }

        public String getGuid() {
            return guid;
        }

        public String getCodeListName() {
            return codeListName;
        }
    }

    private List<ExpectedContentType> expectedContentTypes;
    private List<ExpectedOtherQBDT> expectedOtherQBDTs;
    private Map<String, ExpectedQBDTSCPriRestri> qbdtScPriRestriMap;

    private int CDTPriBooleanId;
    private int xbtBooleanTrueFalseTypeId;

    private int CDTPriDecimalId;
    private int xsdDecimalId;

    private int CDTPriDoubleId;
    private int xsdDoubleId;
    private int CDTPriFloatId;
    private int xsdFloatId;

    private int CDTPriIntegerId;
    private int xsdIntegerId;
    private int xsdNonNegativeIntegerId;
    private int xsdPositiveInteger;

    private int CDTPriNormalizedStringId;
    private int xsdNormalizedStringId;

    private int CDTPriStringId;
    private int xsdStringId;

    private int CDTPriTokenId;
    private int xsdTokenId;

    private int CDTPriTimePointId;
    private int xsdDateTimeId;
    private int xsdDateId;
    private int xsdTimeId;
    private int xsdGYearMonthId;
    private int xsdGYearId;
    private int xsdGMonthDayId;
    private int xsdGDayId;
    private int xsdGMonthId;


    @Before
    public void setUp() {

        CDTPriBooleanId = cdtPriRepository.findOneByName("Boolean").getCdtPriId();
        CDTPriDecimalId = cdtPriRepository.findOneByName("Decimal").getCdtPriId();
        CDTPriDoubleId= cdtPriRepository.findOneByName("Double").getCdtPriId();
        CDTPriFloatId= cdtPriRepository.findOneByName("Float").getCdtPriId();
        CDTPriIntegerId= cdtPriRepository.findOneByName("Integer").getCdtPriId();
        CDTPriNormalizedStringId= cdtPriRepository.findOneByName("NormalizedString").getCdtPriId();
        CDTPriStringId= cdtPriRepository.findOneByName("String").getCdtPriId();
        CDTPriTokenId= cdtPriRepository.findOneByName("Token").getCdtPriId();
        CDTPriTimePointId = cdtPriRepository.findOneByName("TimePoint").getCdtPriId();

        xbtBooleanTrueFalseTypeId = xbtRepository.findOneByBuiltInType("xbt_BooleanTrueFalseType").getXbtId();
        xsdDecimalId = xbtRepository.findOneByBuiltInType("xsd:decimal").getXbtId();
        xsdDoubleId = xbtRepository.findOneByBuiltInType("xsd:double").getXbtId();
        xsdFloatId = xbtRepository.findOneByBuiltInType("xsd:float").getXbtId();
        xsdIntegerId = xbtRepository.findOneByBuiltInType("xsd:integer").getXbtId();
        xsdNonNegativeIntegerId = xbtRepository.findOneByBuiltInType("xsd:nonNegativeInteger").getXbtId();
        xsdPositiveInteger = xbtRepository.findOneByBuiltInType("xsd:positiveInteger").getXbtId();
        xsdNormalizedStringId = xbtRepository.findOneByBuiltInType("xsd:normalizedString").getXbtId();
        xsdStringId = xbtRepository.findOneByBuiltInType("xsd:string").getXbtId();
        xsdTokenId = xbtRepository.findOneByBuiltInType("xsd:token").getXbtId();
        xsdDateTimeId= xbtRepository.findOneByBuiltInType("xsd:dateTime").getXbtId();
        xsdDateId= xbtRepository.findOneByBuiltInType("xsd:date").getXbtId();
        xsdTimeId= xbtRepository.findOneByBuiltInType("xsd:time").getXbtId();
        xsdGYearMonthId= xbtRepository.findOneByBuiltInType("xsd:gYearMonth").getXbtId();
        xsdGYearId= xbtRepository.findOneByBuiltInType("xsd:gYear").getXbtId();
        xsdGMonthDayId= xbtRepository.findOneByBuiltInType("xsd:gMonthDay").getXbtId();
        xsdGDayId= xbtRepository.findOneByBuiltInType("xsd:gDay").getXbtId();
        xsdGMonthId= xbtRepository.findOneByBuiltInType("xsd:gMonth").getXbtId();

        expectedContentTypes = Arrays.asList(
                new ExpectedContentType("Action", "oagis-id-08d6ade226fd42488b53c0815664ccf3", "oacl_ActionCode"),
                new ExpectedContentType("Agency Role", "oagis-id-e1b0c9f2b79a41da91200c26f1786453", "oacl_AgencyRoleCode"),
                new ExpectedContentType("Category", "oagis-id-ad482ed644fb464485b6c2810c11981a", "oacl_CategoryCode"),
                new ExpectedContentType("Charge", "oagis-id-909624db85184160828dc419f741c612", "oacl_ChargeCode"),
                new ExpectedContentType("Classification", "oagis-id-c3d657318fe3405e95475794be694ada", "oacl_ClassificationCode"),
                new ExpectedContentType("Charge Bearer", "oagis-id-5ded5c9751534cbba37c1995c031a4cb", "oacl_ChargeBearerCode"),
                new ExpectedContentType("Check Delivery Method", "oagis-id-f1827291a63c4b179b4d4396e55d17d1", "oacl_CheckDeliveryMethodCode"),
                new ExpectedContentType("Check Instruction", "oagis-id-4a332cec0ad34a18889e1c7723d1635b", "oacl_CheckInstructionCode"),
                new ExpectedContentType("Confirmation", "oagis-id-d96bfeb7f20a40b48e5ef2f6fe7a870c", "oacl_ConfirmationCode"),
                new ExpectedContentType("Contact Location", "oagis-id-0c4a51622f0b40a49fe848b7af3ed85f", "oacl_ContactLocationCode"),
                new ExpectedContentType("Control Assertion", "oagis-id-785caa3c0a6e44809afd2edf2bf0cbd6", "oacl_ControlAssertionCode"),
                new ExpectedContentType("Control", "oagis-id-397b2c60a50b4a6bb8fd8c27e42997c7", "oacl_ControlCode"),
                new ExpectedContentType("Control Component", "oagis-id-64d632d770164bcab04f1c0540f51685", "oacl_ControlComponentCode"),
                new ExpectedContentType("Country", "oagis-id-466610673ec34b6c8c647d0f8b17fd1b", "oacl_CountryCode"),
                new ExpectedContentType("Country Subdivision", "oagis-id-9a66c23bf2704d80803f8c99c3796a41", "oacl_CountrySubdivisionCode"),
                new ExpectedContentType("Credit Transfer", "oagis-id-680932277c154e3281f0aedc1d5c0477", "oacl_CreditTransferCode"),
                new ExpectedContentType("Currency", "oagis-id-7b3457352b7e47609138ba10970e03aa", "oacl_CurrencyCode"),
                new ExpectedContentType("Day Of Week", "oagis-id-9d2b86fa536740649a3a7bd257d71c89", "oacl_DayOfWeekCode"),
                new ExpectedContentType("Debit Credit", "oagis-id-6073c7d7d8124c05ac5c4152e4036eea", "oacl_DebitCreditCode"),
                new ExpectedContentType("Delivery Point", "oagis-id-e5f3ed90350e43ee8168257121535b9e", "oacl_DeliveryPointCode"),
                new ExpectedContentType("E Mail Format", "oagis-id-dca015ffe62946dfafa7b45cae8f8b68", "oacl_EMailFormatCode"),
                new ExpectedContentType("Engineering Activity", "oagis-id-7f8fcf6fa5df4bc1ad4eada3f42eb43c", "oacl_EngineeringActivityCode"),
                new ExpectedContentType("Engineering Work Order", "oagis-id-18e0ee3656834b78b9399b325a644a6f", "oacl_EngineeringWorkOrderCode"),
                new ExpectedContentType("Engineering Work Request", "oagis-id-e29b443a7f8245699ab4e340e6f08d11", "oacl_EngineeringWorkRequestCode"),
                new ExpectedContentType("Engineering Work Request Status", "oagis-id-4e3bb9cefbf24c4e90d3b73b5b2176d8", "oacl_EngineeringWorkRequestStatusCode"),
                new ExpectedContentType("Freight Term", "oagis-id-4e9b5ddfe8874df38d07288a6d91ba15", "oacl_FreightTermCode"),
                new ExpectedContentType("Gender", "oagis-id-f080bdebd0be449c8f0563ceb3de483c", "oacl_GenderCode"),
                new ExpectedContentType("Incoterms", "oagis-id-a3ccabb5e06240f189d72bd1a451c09f", "oacl_IncotermsCode"),
                new ExpectedContentType("Language", "oagis-id-59a897077c3a4fdca89a19a3bf728e26", "oacl_LanguageCode"),
                new ExpectedContentType("License Type", "oagis-id-eb9859902ff846fa819a2e84a9764ec0", "oacl_LicenseTypeCode"),
                new ExpectedContentType("Marital Status", "oagis-id-8582f38cdf2c45448e3883d7dfdbf6a6", "oacl_MaritalStatusCode"),
                new ExpectedContentType("Match", "oagis-id-759137981ae74166984f50605929cb75", "oacl_MatchCode"),
                new ExpectedContentType("Match Document", "oagis-id-41b7326252df4817b948d13c2afef2b0", "oacl_MatchDocumentCode"),
                new ExpectedContentType("MIME", "oagis-id-6b6bdc9d48d247cd8f7b81d0db789409", "oacl_MIMECode"),
                new ExpectedContentType("Party Category", "oagis-id-c4ae47e8ac574b139ee12f4c0b4831ad", "oacl_PartyCategoryCode"),
                new ExpectedContentType("Party Role", "oagis-id-097adb9430434b958a2b334497b7c7b0", "oacl_PartyRoleCode"),
                new ExpectedContentType("Payment Basis", "oagis-id-affb19e98e8a4a9ea25e8fe37e91ba39", "oacl_PaymentBasisCode"),
                new ExpectedContentType("Payment Purpose", "oagis-id-f3cc3615adae4ef0a49fd91d02618e21", "oacl_PaymentPurposeCode"),
                new ExpectedContentType("Payment Method", "oagis-id-df92b34d49b142f9b01d33b5c658c191", "oacl_PaymentMethodCode"),
                new ExpectedContentType("Payment Scheme", "oagis-id-890b3b8b41ce4b3fbfc40cf4c6f269ae", "oacl_PaymentSchemeCode"),
                new ExpectedContentType("Payment Term", "oagis-id-e2ef95b8bc5144faa561093e65d8c9e4", "oacl_PaymentTermCode"),
                new ExpectedContentType("Process Category", "oagis-id-2100a1bfb8c74071b9fc22130140af6a", "oacl_ProcessCategoryCode"),
                new ExpectedContentType("Process Confirmation", "oagis-id-198877b27f75423f8bafd3f82803dd46", "oacl_ProcessConfirmationCode"),
                new ExpectedContentType("Reason", "oagis-id-49e616e3cf784a02b21a0a1f01b7f8d2", "oacl_ReasonCode"),
                new ExpectedContentType("Recurrence Pattern", "oagis-id-536bc88db9074cdd8c72b28443b85e56", "oacl_RecurrencePatternCode"),
                new ExpectedContentType("Remit Location Method", "oagis-id-de851d5c88494da6bd5ce986910d4efa", "oacl_RemitLocationMethodCode"),
                new ExpectedContentType("Resource Type", "oagis-id-d07f3789f8a84e26a900a061a23f0f2a", "oacl_ResourceTypeCode"),
                new ExpectedContentType("Response Action", "oagis-id-2d10e154dd7a4d96b5bc25e254833e0f", "oacl_ResponseActionCode"),
                new ExpectedContentType("Response", "oagis-id-d359dad68ee748b3b19c578ed9d006d8", "oacl_ResponseCode"),
                new ExpectedContentType("Risk", "oagis-id-60ddd70a4eaa41249eca417db6a15e17", "oacl_RiskCode"),
                new ExpectedContentType("Sales Activity", "oagis-id-a9ba17d97f1041e8a665ecd3e3e8cee9", "oacl_SalesActivityCode"),
                new ExpectedContentType("Sales Task", "oagis-id-12af5c4e0b4d4a45acfa61fdf9f739ab", "oacl_SalesTaskCode"),
                new ExpectedContentType("State", "oagis-id-2654ce58ee6d4da7b332a995a2cddf0d", "oacl_StateCode"),
                new ExpectedContentType("System Environment", "oagis-id-377aed87d8cf4a57895e04d302d76ce3", "oacl_SystemEnvironmentCode"),
                new ExpectedContentType("Tax", "oagis-id-062248c9951d44bfbe3b65a69cfa26de", "oacl_TaxCode"),
                new ExpectedContentType("Transfer", "oagis-id-c9dbc6a3ec254540907732a5c253e25e", "oacl_TransferCode"),
                new ExpectedContentType("Unit", "oagis-id-740dd2f4526741cc9f8532181c1c2630", "oacl_UnitCode"),
                new ExpectedContentType("Time Zone", "oagis-id-740dd2f4526741cc9f8532181c1c2635", "oacl_TimeZoneCode")
        );
        assertEquals(58, expectedContentTypes.size());

        expectedOtherQBDTs = Arrays.asList(
                new ExpectedOtherQBDT("Open", "Amount", "Amount. Type", "oagis-id-4b47b06600344fdb95bbe52f664c5a20"),
                new ExpectedOtherQBDT("Open", "Binary Object", "Binary Object. Type", "oagis-id-994553f41e0e48158d0a2516bc935511"),
                new ExpectedOtherQBDT("Open", "Code", "Code. Type", "oagis-id-506251b85278490a98d998763406f0b9"),
                new ExpectedOtherQBDT("Open", "Date", "Date. Type", "oagis-id-b033251fb58848eebffbabdd778aa940"),
                new ExpectedOtherQBDT("Open", "Date Time", "Date Time. Type", "oagis-id-38a0fb8fb7514a29b0597837faf2518d"),
                new ExpectedOtherQBDT("Open", "Duration", "Duration. Type", "oagis-id-f8166dd9674d4855a3b2da5001049590"),
                new ExpectedOtherQBDT("Open", "Identifier", "Identifier. Type", "oagis-id-1af3c0f64e8f4852b1303a583171b609"),
                new ExpectedOtherQBDT("Open", "Indicator", "Indicator. Type", "oagis-id-1443878322254f0da4f0bf36a3d468ab"),
                new ExpectedOtherQBDT("Open", "Measure", "Measure. Type", "oagis-id-87bd59221bbc47938599363bbba6142a"),
                new ExpectedOtherQBDT("Open", "Name", "Name. Type", "oagis-id-3cd82d4229984113976240713ed38906"),
                new ExpectedOtherQBDT("Open", "Number", "Number. Type", "oagis-id-dfabe79fde714c1daf6547d6e533a6ee"),
                new ExpectedOtherQBDT("Open", "Percent", "Percent. Type", "oagis-id-46626125da114dfabbfbb11800d1bc52"),
                new ExpectedOtherQBDT("Open", "Quantity", "Quantity. Type", "oagis-id-0b2f056d45304fe997e6b8df8d8ad33e"),
                new ExpectedOtherQBDT("Open", "Text", "Text. Type", "oagis-id-5840f7a57dd949ababcd1eb394b2840c"),
                new ExpectedOtherQBDT("Open", "Time", "Time. Type", "oagis-id-fd79b936c42944f0818865b89e092257"),
                new ExpectedOtherQBDT("Open Field", "Value", "Value. Type", "oagis-id-a443aea7aa634ae8aa57bfad38762ef1"),
                new ExpectedOtherQBDT("Score", "Value", "Value. Type", "oagis-id-f4c5cb13024345408522ce0757ce2f5c"),
                new ExpectedOtherQBDT("Record Field", "Value", "Value. Type", "oagis-id-de22296a3390465eb652dc43a7676bc0"),
                new ExpectedOtherQBDT("Description", "Text", "Text. Type", "oagis-id-9dc9109b384b43299ecc0d140817f47b"),
                new ExpectedOtherQBDT("Note", "Text", "Text. Type", "oagis-id-8d9b4b5c8b284fd0b59b246262bcafa2"),
                new ExpectedOtherQBDT("Temperature", "Measure", "Measure. Type", "oagis-id-4a641b3b6a074d5b909b425aeec9caaa"),
                new ExpectedOtherQBDT("Name Value Pair", "Text", "String. Type", "oagis-id-e298a36a331247c3a2a4a585d5754531"),
                new ExpectedOtherQBDT("Preferred", "Name", "Name. Type", "oagis-id-55e3dee204b449fab4dac08c5893c031"),
                new ExpectedOtherQBDT("Sequenced", "Code", "Code. Type", "oagis-id-dac3c62369c54a5e8b996496f0438c64"),
                new ExpectedOtherQBDT("Sequenced", "Identifier", "Identifier. Type", "oagis-id-0624ca38925d43929a6f7d8b7b7e0882"),
                new ExpectedOtherQBDT("Sequenced", "Text", "Text. Type", "oagis-id-51e010d7a1e24ebe89fcf58989fefd1b"),
                new ExpectedOtherQBDT("Typed", "Text", "Sequenced_ Text. Type", "oagis-id-c2bef83fa023429f96c2383d4304c7b2"),
                new ExpectedOtherQBDT("Telephone", "Value", "Value_D19E7B. Type", "oagis-id-30c24b6c473f48e396ad7b0845cf95d2"),
                new ExpectedOtherQBDT("License", "Indicator", "Indicator. Type", "oagis-id-de63b187aff44fedb887d12a9035309a"),
                new ExpectedOtherQBDT("Harmonized Tariff Schedule", "Code", "Code. Type", "oagis-id-3de49c6b04d2483d94e47b46e246aa4f"),
                new ExpectedOtherQBDT("Financial", "Code", "Match_ Code. Type", "oagis-id-31de26f234b749448cf9c474336ba55d"),
                new ExpectedOtherQBDT("Encrypted", "Binary Object", "Binary Object. Type", "oagis-id-f28d03fa05a94dc49dad44afb81f79e0"),
                new ExpectedOtherQBDT("Acknowledge", "Code", "Response_ Code. Type", "oagis-id-8637fad84f094b5f97a64c12c67b939c"),
                new ExpectedOtherQBDT("Action", "Code", "Code. Type", "oagis-id-480d156c3d50459aa6689fa74d01ac89"),
                new ExpectedOtherQBDT("Category", "Code", "Code. Type", "oagis-id-c4a4406c1cd74115a07f04b365ab9075"),
                new ExpectedOtherQBDT("Charge Bearer", "Code", "Code. Type", "oagis-id-a8d20a9602eb4353816e12cd17ea0343"),
                new ExpectedOtherQBDT("Charge", "Code", "Code. Type", "oagis-id-6131e6a3d8114c5ea36bd91044180bb1"),
                new ExpectedOtherQBDT("Check Delivery Method", "Code", "Code. Type", "oagis-id-f28cea919478487cbb2f95deb3b408e2"),
                new ExpectedOtherQBDT("Check Instruction", "Code", "Code. Type", "oagis-id-6fa8419852f6400abd5c255f55e38f40"),
                new ExpectedOtherQBDT("Classification", "Code", "Code. Type", "oagis-id-8f4d478090a746fc9ec0573373901793"),
                new ExpectedOtherQBDT("Confirmation", "Code", "Code. Type", "oagis-id-8311a261bc5348dda91cba90fa83429e"),
                new ExpectedOtherQBDT("Contact Location", "Code", "Code. Type", "oagis-id-250d8771469544d88b7bb7607432da93"),
                new ExpectedOtherQBDT("Control Assertion", "Code", "Code. Type", "oagis-id-34c5204633a54894846863a99618f567"),
                new ExpectedOtherQBDT("Control Component", "Code", "Code. Type", "oagis-id-31e2c152a1c8458e8d0ecda03db61206"),
                new ExpectedOtherQBDT("Control", "Code", "Code. Type", "oagis-id-4465b4b605184fcc8f46c44e5d153f37"),
                new ExpectedOtherQBDT("Country", "Code", "Code. Type", "oagis-id-7d121d2b83504aadb363832921313340"),
                new ExpectedOtherQBDT("Country Sub Division", "Code", "Code. Type", "oagis-id-3582718b267c47c1bb66f323026b6455"),
                new ExpectedOtherQBDT("Credit Transfer", "Code", "Code. Type", "oagis-id-08af8861f20b499cabd7d7c55875e2b2"),
                new ExpectedOtherQBDT("Currency", "Code", "Code. Type", "oagis-id-2800f421b6104d0bb3967395a7fca178"),
                new ExpectedOtherQBDT("Day Of Week", "Code", "Code. Type", "oagis-id-12732d4b096d418fa817d8c75dbe920b"),
                new ExpectedOtherQBDT("Debit Credit", "Code", "Code. Type", "oagis-id-0dde79e01ad647028e78e76337ad8796"),
                new ExpectedOtherQBDT("Delivery Point", "Code", "Code. Type", "oagis-id-cdca0b77d32e4bab80a2a4597d66d13e"),
                new ExpectedOtherQBDT("Email Format", "Code", "Code. Type", "oagis-id-55303803c2454c6bbc3b826f74ead936"),
                new ExpectedOtherQBDT("Engineering Activity", "Code", "Code. Type", "oagis-id-bc15ccf8c8a24bf189e9dd3f4084947c"),
                new ExpectedOtherQBDT("Engineering Work Order", "Code", "Code. Type", "oagis-id-355457cafb544cba89666c0b29ddd14b"),
                new ExpectedOtherQBDT("Engineering Work Request", "Code", "Code. Type", "oagis-id-799714a12ef0445fb6058f78edf38719"),
                new ExpectedOtherQBDT("Engineering Work Request Status", "Code", "Code. Type", "oagis-id-0c47b25537494bc196fb00db88be6eb3"),
                new ExpectedOtherQBDT("Final Agent Instruction", "Code", "Code. Type", "oagis-id-46435e4819e1496e8b4e3b7e9427c049"),
                new ExpectedOtherQBDT("Freight Term", "Code", "Code. Type", "oagis-id-0a6c463dae304b7e9323987c2414eda9"),
                new ExpectedOtherQBDT("Gender", "Code", "Code. Type", "oagis-id-3357e4f03b794607ba65ac06f0a7af8f"),
                new ExpectedOtherQBDT("Incoterms", "Code", "Code. Type", "oagis-id-bdbb3ceca38b4a3b960f07ce65dbdc14"),
                new ExpectedOtherQBDT("Language", "Code", "Code. Type", "oagis-id-d06b74f6326d4b3796472e484c8ee114"),
                new ExpectedOtherQBDT("License", "Code", "Code. Type", "oagis-id-05347183cec64cd0a418d3ebaa440d65"),
                new ExpectedOtherQBDT("Match", "Code", "Code. Type", "oagis-id-a95a79c088ed4b7b8ebf0ed30d948f92"),
                new ExpectedOtherQBDT("Match Document", "Code", "Code. Type", "oagis-id-a78f749cbd574e66bb7d2990c2f0f1e0"),
                new ExpectedOtherQBDT("Marital Status", "Code", "Code. Type", "oagis-id-9b3d5038a87c4096ac38b3d8b446468e"),
                new ExpectedOtherQBDT("MIME", "Code", "Code. Type", "oagis-id-481ad1c0f5e04ddaa1a9e6d124a81956"),
                new ExpectedOtherQBDT("Payment Basis", "Code", "Code. Type", "oagis-id-0f3c04d8c8954282a3946a2fef821f2d"),
                new ExpectedOtherQBDT("Payment Method", "Code", "Code. Type", "oagis-id-a6f7038124ff455187957149eb6b20db"),
                new ExpectedOtherQBDT("Payment Purpose", "Code", "Code. Type", "oagis-id-6726fb3e210a497e8b784ae82b39a88e"),
                new ExpectedOtherQBDT("Payment Scheme", "Code", "Code. Type", "oagis-id-375c95ec0fa74cdb98bc2523ddf86969"),
                new ExpectedOtherQBDT("Payment Term", "Code", "Code. Type", "oagis-id-a805bb8347dc4a1da34799751d2b91c1"),
                new ExpectedOtherQBDT("Process Category", "Code", "Code. Type", "oagis-id-653072097f904ab2a22386d5c4ad201b"),
                new ExpectedOtherQBDT("Process Confirmation", "Code", "Code. Type", "oagis-id-87c569b9b0a541caadccf1b2514539dd"),
                new ExpectedOtherQBDT("Reason", "Code", "Code. Type", "oagis-id-d558f96782dc4b7494790eb40bb98b2a"),
                new ExpectedOtherQBDT("Recurrence Pattern", "Code", "Code. Type", "oagis-id-bf00248210d14830992852cd73f02061"),
                new ExpectedOtherQBDT("Remit Location Method", "Code", "Code. Type", "oagis-id-38c236283326452c9bc2c392a1673152"),
                new ExpectedOtherQBDT("Resource Type", "Code", "Code. Type", "oagis-id-2abdd58302114d6a84af5d24d59e0313"),
                new ExpectedOtherQBDT("Response", "Code", "Code. Type", "oagis-id-dfb3832a38cb4ab68257c1a056de063d"),
                new ExpectedOtherQBDT("Risk", "Code", "Code. Type", "oagis-id-284fa310cd834d9cbd7e804ac2bdc7e5"),
                new ExpectedOtherQBDT("Sales Activity", "Code", "Code. Type", "oagis-id-8c211564adc3475b9f5bc96c3e90ab5e"),
                new ExpectedOtherQBDT("Sales Task", "Code", "Code. Type", "oagis-id-d709b8ba02f1464ea8dfcb297cf91e4b"),
                new ExpectedOtherQBDT("State", "Code", "Code. Type", "oagis-id-d1e442d072d84de89420ca400bf11533"),
                new ExpectedOtherQBDT("System Environment", "Code", "Code. Type", "oagis-id-f0a7a5c206ae42b699e9de8da860cfc3"),
                new ExpectedOtherQBDT("Tax", "Code", "Code. Type", "oagis-id-8954f848e8f448c0a72785acd5a3a805"),
                new ExpectedOtherQBDT("Transfer", "Code", "Code. Type", "oagis-id-f1bf224d9da94fbea2d8e98af95c7a0b"),
                new ExpectedOtherQBDT("Time Zone", "Code", "Code. Type", "oagis-id-f1bf224d9da94fbea2d8e98af95c7a19"),
                new ExpectedOtherQBDT("UOM", "Code", "Code. Type", "oagis-id-6b7fa72803d843d8a29c567788b90745")
        );
        assertEquals(88, expectedOtherQBDTs.size());


        qbdtScPriRestriMap = new HashMap<>();
        qbdtScPriRestriMap.put("oagis-id-63d46f46e4fa46ec8cc269212d7cfa77", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-b4436f4498bd47b188330661b29d596d", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-563f7c1348ca48328faf286aaa19473b", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-64b1ef4ceab44c06a3816c19a28b5fec", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-543b69ea686e44f4a470a332e18b1d91", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8d7acef53cda44a2874809fc08d2018b", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-c66d3280c8f5457096df33b593dcbddf", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-48dc2d60ac4d4235bf9a1f6706a81e3b", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-98d2757026ec48daacb73f7589300fa3", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-82d03758dfd844bea1676759edf0d653", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-81c76ad6b7914fbf980e0149c0b0cbb7", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-b00b3ba702944e048bf72e3edfc85c9b", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3afffa0da54c450f84aadbd33c0beac0", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-2780e69800934662a4782be31c2bacf6", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-fe67f89596b34ea387d6d97167e31778", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-9034f3521cc84e0e93b4b10e54bb46a3", new ExpectedQBDTSCPriRestri("formatCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-c736ced2c5d24d85a2fb47f608a85a95", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-0c8ef7c4cd374bffb8710c544b4f9de4", new ExpectedQBDTSCPriRestri("code", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-0710e25ea5cb4c118538c9dd002ce2f0", new ExpectedQBDTSCPriRestri("interval", "xsd:string", "", ""));
        qbdtScPriRestriMap.put("oagis-id-6f2da2a259f44984b0a479fe8eb0379e", new ExpectedQBDTSCPriRestri("minimum", "xsd:string", "", ""));
        qbdtScPriRestriMap.put("oagis-id-d53cf4ec2ae44af78ef15bdf266fb6a9", new ExpectedQBDTSCPriRestri("maximum", "xsd:string", "", ""));
        qbdtScPriRestriMap.put("oagis-id-9034f3521cc84e0e93b4b10e54aa46a3", new ExpectedQBDTSCPriRestri("formatCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-7c3c291f53984e90bed1749ac95a7f28", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-7c3c291f53984e90bed1749bc10a7f28", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-08c23257569347caa798f07620e38923", new ExpectedQBDTSCPriRestri("author", "xsd:string", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8b0a10d59d3647d89e0819516c1aeaca", new ExpectedQBDTSCPriRestri("entryDateTime", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-893aac10c6724803ad4e1496ce5d0027", new ExpectedQBDTSCPriRestri("status", "xsd:string", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8b03854b876742dc9c1d1bedddf2755d", new ExpectedQBDTSCPriRestri("name", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-086abcbc69a34794ad2520eb28080afc", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-ed2af44734be48f6be9936110eae4641", new ExpectedQBDTSCPriRestri("unitCode", "xsd:token", "oacl_UnitCode", ""));
        qbdtScPriRestriMap.put("oagis-id-d455ed06eb7a40d1ae1d3408cd3ea1c3", new ExpectedQBDTSCPriRestri("unitCodeListVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-9bb9add40b5b415c8489b08bd4484907", new ExpectedQBDTSCPriRestri("preferredIndicator", "xbt_BooleanTrueFalseType", "", ""));
        qbdtScPriRestriMap.put("oagis-id-f14c6ce3eae34517acb446241a38c629", new ExpectedQBDTSCPriRestri("sequenceNumber", "xsd:decimal", "", ""));
        qbdtScPriRestriMap.put("oagis-id-93d92483194641a3ac48a997ec4dbe5d", new ExpectedQBDTSCPriRestri("sequenceNumber", "xsd:decimal", "", ""));
        qbdtScPriRestriMap.put("oagis-id-39a5a53826024a65a2291f50d9feecd3", new ExpectedQBDTSCPriRestri("sequenceNumber", "xsd:decimal", "", ""));
        qbdtScPriRestriMap.put("oagis-id-4228f5e4d6294040a8d813736f872bae", new ExpectedQBDTSCPriRestri("type", "xsd:string", "", ""));
        qbdtScPriRestriMap.put("oagis-id-c13ef1ab9da949329b0b19895179bbd8", new ExpectedQBDTSCPriRestri("numberFormatValue", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-81f8b8994157436689db9229650af6c3", new ExpectedQBDTSCPriRestri("accessCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-da20be8066e1484da8ed8519b1457ba8", new ExpectedQBDTSCPriRestri("countryCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-fda27cba60354fe1b3ec82438aebb73f", new ExpectedQBDTSCPriRestri("areaCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-121bc81df6df4642b1390485dc65656e", new ExpectedQBDTSCPriRestri("exchangeValue", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-5a36cbe4e69a4c869e2425f1331c1d76", new ExpectedQBDTSCPriRestri("extensionValue", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-db502068711649c385a0bd1f41d3a63d", new ExpectedQBDTSCPriRestri("name", "xsd:token", "oacl_LicenseTypeCode", ""));
        qbdtScPriRestriMap.put("oagis-id-aac395f92b5b44e389a6ba3a2f189dcd", new ExpectedQBDTSCPriRestri("countryCode", "xsd:token", "oacl_CountryCode", ""));
        qbdtScPriRestriMap.put("oagis-id-3ed14d16c7334beea947a253ba4c5d31", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-a770cbdc4d8f42a38e136f91fe566873", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-fbfa9b1b4b914e009f17c4e486f5eaa6", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-ad83b12bc35f45189bb13fe32fdabb57", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-fe96e5569935475285bb02ca3977f0e5", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-24db8872409f48b8b92a2cdf07f00285", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-b724266afe58480f80c9c58287cd672e", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-9135ae93211a4470b4964eb19fd7dd11", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-a6db1960b3d1470783d2deb750f122c7", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-031ac869ab004b24baf568a18be2cba5", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3386e05f8b6f415b94bd10267da0da8a", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-c3a48632756946eab4151e3d51e6a62e", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-5150467da5004c4fac4dd1dae5f3559d", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e218062ce1b94d5aa719352380a7d83b", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-62e2f345ec4340a2b348798e90e234e8", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-913b18566a754ba9b94a050d399ea149", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-9ae5cd690e24415b85211f98b5e1bc7b", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-723f51b5f86b4753940f62ce46864aa5", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-01187c67b1834598b3e63b77634f8b0e", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e100b634bdf44e82adfc0551cc68a002", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-26acfd7fdbff40cb8af1062a513d4ce7", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-fe6422f9632e4e07addb64ad18f26d00", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-4c374723ecfb44b39a492504eb02e37f", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-86fcddc6c6dd4389b3024e7f664512b5", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-ef3ed7b7779145d8b49da60c24f5b6a0", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-04f8787edf8641b4a31cd2574fd9a6cb", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-a912b4737c6140cbab7820a8929db906", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e47690b640464528973b475a6764aee2", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-7916f409d25b40a4ac62927241f3410f", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-b63575497bf14cac9a6f08158a3b800f", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3737b52e961944fa9cca686229bfec86", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-fda2fa4f93c6415686f4be80cd0e7a61", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-4b132da72c244a9299d2b0de35164226", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-99e35ec4191f4f068a5de55f636beacd", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-f7cabe17f72f4cac95024974b4124046", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-d1b6cdd8c2dc48f5996c95b9b7387434", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-27d5ae9716904a6694e84200ee63d593", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-7a9c86259ac54c7686628c4d9ddf76fb", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-6a2a50be7142492f98ea30c541538eb0", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-d68e1a8121944c03a721773885265c71", new ExpectedQBDTSCPriRestri("typeCode", "xsd:token", "", ""));
        qbdtScPriRestriMap.put("oagis-id-bfa30d4b139849a190951d1dc77fbd28", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-31f5dc214d5045c0acb2a34701bf3403", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-e05522b3a3e54e54b36b11ed64f491aa", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-f851a6c9509548bf82a0a0ca5683249d", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e758cce7a51d44f887ed75cb07f8a8c6", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-328f0ddd012249618611ef731906e78c", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-96a0c4ca29ca42e298ced3e209cdfa1d", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-d578cb3477e945c59fa2d024855f8436", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-2ae14052b29c4b889b833dd52263e8f2", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-76087f82da27494c98586b51dbc0f847", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-11e2ef27228b423f8692c97293217575", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-6640631a9a604a819b905c33cafcdc84", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-ae6668f9a1194a12bbc6fb9bcfdc90f7", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8a94f5337a4945f9a0f4f39250a6cde5", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-ffd0139554494360a14d2d6093b12a32", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3488a0e0f0404c89869d07f8c03b2b7a", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8920d2880d624fbd928edfadba377657", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-1581f204ba05401f8ccf6e561534d469", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-cfcf704340274e5889bccd30b4423451", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3815fcd73c8d4de69c4ecc21e5e9176b", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-d1a7dd9d23314bc4838a421cd3591ef1", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-1479d9f3c21944beb53cd1605d5b7aec", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-83846bbbfc7244a79fc015982ce2ee59", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-b54005a1bf904cd6a2907270a9dcad8c", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-ffa646bcc39842c890057effa6c5975d", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3035a088949c4186804fd55e1796be08", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-12df0e4ddc4a4e489205c07aed956484", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-7cebe0943d9e4743809c6b3d0fc7f7c6", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3ab2bacf93714d35a4613710318e9f47", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-faa0f7006f724e1fa3bfe06a585891be", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-ad71fcecdb934fda86cad46c436d2e2f", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-cb9166872ba54f05a50bc2a3fbb42165", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-ed584a4b4c7e4328859b8688d91f78be", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-1dae1402e2ac498ea41e20afb69f8002", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-dfeaf700ece44979b022e6ddbfe6fcdf", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-2d288e5dac3140f19ca5d4d9cea77684", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-f4e2135be85b4c5ea195ae367d3973be", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-4bfbb27a12e148a5af5d93a7371a0e8e", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-aa7e6f7aad054bdca87fd71326d3b740", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-d6eddc4772dd489f8723449474e56a4c", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-1b17f61877d6474ea6e73ab07f584b16", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-76d7253c61b34cc2ad43ead3b3f1922f", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-391f8699cfce4a69b4dd276a83fb0a47", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-054beac32aed46c784752eeb81d10200", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-ead36c5f905a4ab09f5ec34a5b0c02a7", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-6050905d3ecf4308b65514f650eb2309", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-42e72842204b41bbba4ba402f548c0da", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-f708d760df624e79b201e51ba5630385", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-0c49b0be61ad4e63842c0e76425ddba0", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-61eeeb78bd8447e89d7d0a52f0501976", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-ab3c2e1abeff4872a7e60ce9ce95bde5", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-fe7175233094436586902d1790c8f601", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-2946363d2db24161b51780176ae21fd2", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-b743f9f23cb84470a8a1b6e2de5eea49", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-2e5a6564746c4acfbd768bafe0794997", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-1e5446c5e3d54f819ae59866ab836013", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-bc4443a7513b4a5e9768f085d91b0b08", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-2a0d8eac934f40eeaaaf904b9b52d1de", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3c3ce6fab37f4fdca102cb2180b944b0", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-3853abb5c9244483aab2cbc03fbaaaf8", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-286aa0554594455382547fd9e7d0572d", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-f9f1566704274d309481299474d84574", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-f94898b8c49b4d62889fa99217d88413", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-c95531ac8f4d422ab7ddc8a5dfc3fbcb", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-498e3b310a304e3d9b512d72f4ec7808", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-7bf801b6b6414c5985584638c9ab984c", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-13bb99499c0c4699b97cb12bedb4ca84", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-492977f9faf54cc8880ccb51a7e8671c", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-ec04e819931c43d5b0babb7d3cb63055", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-571b366b135144b28c35f96337c715bb", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-c1ac8d32c67644dc8c924bc88efe32ff", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-2fea6aa2bcd348ed9605454c4eb5e1df", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8bd83370a6de465da589eeda41067ec7", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-9a3aaf9381c84b569c5562c4b536f857", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-1ffc2c57129f4cc2b590f9af9f2f6cce", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-6b34b38215e14086b6daa9276407a646", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-0f3abdfb7a464cccb9fd3c04be53db7f", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-5b32eb0f737743b1a8c11555b3f2ab22", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-2cc8bdf28e9043309536c249f2eea9fc", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-cc70c786386b4b1e97e03d497c93ffcf", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-ad77283348584b798fea28b6a1e7993f", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-52b52cb89b2d493eae526a0eb8a2fbce", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-bda6a6cb91114b1eac169ad1151c9613", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-c4393a9d805140e6a2c019fd43cc13fb", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e5305a520c744ec49ebdd5e73d7cf474", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-f0da5d47af4d401ca9f1e041c16799bc", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-b1a04e2a0ccd4f368414fd06ceae9093", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-94aacd3290e54e55b32814303fde07e3", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-bc09af5c870441679eb7f0c684ce49ca", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-0bc83cc6e1c04a08bbed6308a1b8d732", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-9cf34eeb9a634fe4a0bc3f7b9714bd90", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-eaa90523e5284c40b6c61d7fd9aa4f5b", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-310f0d76a3064a28a47093996604bd67", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-b60b59e3f1b04bb6b3e024f0817bf9cf", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-4f3fae47c66846c695f1501966b2d343", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-8df48352f08148cda04dd769e74f517f", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8a0f01a897c34ed2929801c6b4632558", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e6048f5e8c0f4f1eb7cd0183ca19bd0c", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-664c5e29b37b401da802648e03f558cb", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-4ab1e9e4399048d08a46e57eef3ded04", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-8508fe4900ac4576b029c748d7a2a5da", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-05b1c539208a4192bef0ab97c5a09225", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e8c118d412ba440ba34179da2481b40f", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-e7f78b3e9b5b4ac7a69ddceb09ac5366", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-57d11f7f3618406ca906d79223a340d9", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-389a57988e504798a5869a92c6728273", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-d6ebc8c3016d4dc2b16658224ac088cf", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-2101e15920d842659f0696e8612eefbc", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-389a57988e504798a5869a92c6728354", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-d6ebc8c3016d4dc2b16658224ac08495", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-2101e15920d842659f0696e8612ee861", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-3d8eec0a38b94bc69d9d40d9d224d647", new ExpectedQBDTSCPriRestri("listID", "xsd:normalizedString", "", ""));
        qbdtScPriRestriMap.put("oagis-id-ac4eed3fe8c24f469cfc1f47ca8f680f", new ExpectedQBDTSCPriRestri("listAgencyID", "xsd:token", "", "Agency Identification"));
        qbdtScPriRestriMap.put("oagis-id-2d34df84eb69473592137596e3f611d4", new ExpectedQBDTSCPriRestri("listVersionID", "xsd:normalizedString", "", ""));
        assertEquals(198, qbdtScPriRestriMap.size());
    }

    @Test
    public void test_PopulateTheQualifiedCodeContentTypeInThe_dt_Table() {
        DataType expectedBasedDt = dtRepository.findOneByTypeAndDen(1, "Code Content. Type");
        assertNotNull(expectedBasedDt);

        test_dt("Code", expectedBasedDt, expectedContentTypes);
    }

    private void test_dt(String dataTerm, DataType expectedBasedDt, Collection<ExpectedContentType> expectedContentTypes) {
        expectedContentTypes.forEach(expectedContentType -> {
            DataType actualDataType = dtRepository.findOneByGuid(expectedContentType.getGuid());
            assertNotNull(actualDataType);

            assertEquals(expectedContentType.getQualifier(), actualDataType.getQualifier());
            assertEquals(expectedBasedDt.getDtId(), actualDataType.getBasedDtId());
            assertEquals(expectedBasedDt.getDataTypeTerm(), actualDataType.getDataTypeTerm());
            assertEquals(expectedContentType.getQualifier() + "_ " + dataTerm + " Content. Type",
                    actualDataType.getDen());
            assertEquals(expectedContentType.getQualifier() + "_ " + dataTerm + " Content. Content",
                    actualDataType.getContentComponentDen());
            assertEquals(3, actualDataType.getState());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getCreatedBy());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getOwnerUserId());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getLastUpdatedBy());
            assertEquals(0, actualDataType.getRevisionNum());
            assertEquals(0, actualDataType.getRevisionAction());
            assertEquals(false, actualDataType.isDeprecated());
        });
    }

    @Test
    public void test_PopulateThe_bdt_pri_restri_TableForTheQualifiedCodeContentType() {
        DataType expectedBasedDt = dtRepository.findOneByTypeAndDen(1, "Code Content. Type");
        assertNotNull(expectedBasedDt);

        test_bdt_pri_restri("Code", expectedBasedDt, expectedContentTypes);
    }

    private void test_bdt_pri_restri(String dataTerm, DataType expectedBasedDt, Collection<ExpectedContentType> expectedContentTypes) {
        List<BusinessDataTypePrimitiveRestriction> expectedBasedBdtPriRestri =
                bdtPriRestriRepository.findByBdtId(expectedBasedDt.getDtId());

        expectedContentTypes.forEach(expectedContentType -> {
            DataType actualDataType = dtRepository.findOneByGuid(expectedContentType.getGuid());
            assertNotNull(actualDataType);

            CodeList expectedCodeList = codeListRepository.findOneByName(expectedContentType.getCodeListName());
            AgencyIdList expectedAgencyIdList = agencyIdListRepository.findOneByName("Agency Identification");

            List<BusinessDataTypePrimitiveRestriction> actualBasedBdtPriRestri =
                    bdtPriRestriRepository.findByBdtId(actualDataType.getDtId());

            int expectedBdtPriRestriCount = expectedBasedBdtPriRestri.size() +
                    ((expectedCodeList != null) ? 1 : 0) +
                    ("Identifier".equals(dataTerm) ? 1 : 0);

            assertEquals(expectedBdtPriRestriCount, actualBasedBdtPriRestri.size());

            assertEquals(expectedBasedBdtPriRestri.stream()
                            .mapToInt(e -> e.getCdtAwdPriXpsTypeMapId())
                            .sum(),
                    actualBasedBdtPriRestri.stream()
                            .filter(e -> e.getCdtAwdPriXpsTypeMapId() != 0)
                            .mapToInt(e -> e.getCdtAwdPriXpsTypeMapId())
                            .sum());

            if (expectedCodeList != null) {
                assertEquals(expectedCodeList.getCodeListId(),
                        actualBasedBdtPriRestri.stream()
                                .filter(e -> e.getCodeListId() != 0)
                                .mapToInt(e -> e.getCodeListId())
                                .sum());
            }

            if ("Identifier".equals(dataTerm)) {
                assertEquals(expectedAgencyIdList.getAgencyIdListId(),
                        actualBasedBdtPriRestri.stream()
                                .filter(e -> e.getAgencyIdListId() != 0)
                                .mapToInt(e -> e.getAgencyIdListId())
                                .sum());
            }
        });
    }

    @Test
    public void test_PopulateThe_dt_sc_table_ForTheQualifiedCodeContentType() {
        DataType expectedBasedDt = dtRepository.findOneByTypeAndDen(1, "Code Content. Type");
        assertNotNull(expectedBasedDt);

        test_dt_sc(expectedBasedDt, expectedContentTypes);
    }

    private void test_dt_sc(DataType expectedBasedDt, Collection<ExpectedContentType> expectedContentTypes) {
        List<DataTypeSupplementaryComponent> expectedDtScList =
                dtScRepository.findByOwnerDtId(expectedBasedDt.getDtId());

        expectedContentTypes.forEach(expectedContentType -> {
            DataType actualDataType = dtRepository.findOneByGuid(expectedContentType.getGuid());
            assertNotNull(actualDataType);

            List<DataTypeSupplementaryComponent> actualDtScList =
                    dtScRepository.findByOwnerDtId(actualDataType.getDtId());

            assertEquals(expectedDtScList.size(), actualDtScList.size());
            assertEquals(expectedDtScList.stream()
                            .mapToInt(e ->
                                    e.getPropertyTerm().hashCode() +
                                            e.getRepresentationTerm().hashCode() +
                                            e.getDefinition().hashCode() +
                                            e.getMinCardinality() +
                                            e.getMaxCardinality() +
                                            e.getDtScId()
                            ).sum(),
                    actualDtScList.stream()
                            .mapToInt(e ->
                                    e.getPropertyTerm().hashCode() +
                                            e.getRepresentationTerm().hashCode() +
                                            e.getDefinition().hashCode() +
                                            e.getMinCardinality() +
                                            e.getMaxCardinality() +
                                            e.getBasedDtScId()
                            ).sum());
        });
    }

    @Test
    public void test_PopulateThe_bdt_sc_pri_restri_TableTheQualifiedCodeContentType() {
        DataType expectedBasedDt = dtRepository.findOneByTypeAndDen(1, "Code Content. Type");
        assertNotNull(expectedBasedDt);

        test_bdt_sc_pri_restri(expectedBasedDt, expectedContentTypes);
    }

    public class ExpectedQBDTSCPriRestri {
        private String attributeName;
        private String attributeType;
        private int codeListId;
        private int agencyIdListId;

        public ExpectedQBDTSCPriRestri(String attName, String attType, String codeListName, String agencyIdListName) {
            if (!StringUtils.isEmpty(codeListName)) {
                CodeList cdl = codeListRepository.findOneByName(codeListName);
                codeListId = cdl.getCodeListId();
            } else if (!StringUtils.isEmpty(agencyIdListName)) {
                AgencyIdList agencyIdList = agencyIdListRepository.findOneByName(agencyIdListName);
                agencyIdListId = agencyIdList.getAgencyIdListId();
            }

            this.attributeName = attName;
            this.attributeType = attType;
        }
    }

    private void test_bdt_sc_pri_restri(DataType expectedBasedDt, ExpectedOtherQBDT expectedQBDT) {
        DataType actualDT = dtRepository.findOneByGuid(expectedQBDT.getGuid());

        List<DataTypeSupplementaryComponent> actualBaseDtScList = dtScRepository.findByOwnerDtId(expectedBasedDt.getDtId());
        List<DataTypeSupplementaryComponent> actualDtScList = dtScRepository.findByOwnerDtId(actualDT.getDtId());

        for (int i = 0; i < actualDtScList.size(); i++) {
            int included = -1;
            for (int j = 0; j < actualBaseDtScList.size(); j++) {
                if (actualDtScList.get(i).getPropertyTerm().equals(actualBaseDtScList.get(j).getPropertyTerm()) &&
                        actualDtScList.get(i).getRepresentationTerm().equals(actualBaseDtScList.get(j).getRepresentationTerm())) {
                    included = j;
                    break;
                }
            }

            if (included != -1) {
                List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> expectedBdtScPriRestriList =
                        bdtScPriRestriRepository.findByBdtScId(actualBaseDtScList.get(included).getDtScId());

                List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> actualBdtScPriRestriList =
                        bdtScPriRestriRepository.findByBdtScId(actualDtScList.get(i).getDtScId());
                assertEquals(expectedBdtScPriRestriList.size(), actualBdtScPriRestriList.size());
                assertEquals(expectedBdtScPriRestriList.stream()
                                .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                                .sum(),
                        actualBdtScPriRestriList.stream()
                                .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                                .sum());

                // 'is_default' value check
                assertEquals(expectedBdtScPriRestriList.stream()
                                .filter(e -> e.isDefault())
                                .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                                .sum(),
                        actualBdtScPriRestriList.stream()
                                .filter(e -> e.isDefault())
                                .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                                .sum());

                actualBdtScPriRestriList.stream()
                        .collect(Collectors.groupingBy(
                                BusinessDataTypeSupplementaryComponentPrimitiveRestriction::getBdtScId))
                        .forEach(((bdtScId, bdtScPriRestriList) -> {
                            assertEquals("The summation of 'is_default' value is always be 1. bdt_sc_id: " + bdtScId,
                                    1,
                                    bdtScPriRestriList.stream()
                                            .mapToInt(e -> e.isDefault() ? 1 : 0)
                                            .sum());
                        }));

            } else {
                DataTypeSupplementaryComponent actualDtSc = actualDtScList.get(i);

                ExpectedQBDTSCPriRestri expectedQBDTScPri = qbdtScPriRestriMap.get(actualDtSc.getGuid());
                assertTrue(expectedQBDTScPri != null);

                int actualCodeListId = 0;
                int actualAgencyIdListId = 0;
                int actualMapIdSum = 0;
                int actualDefaultMapId = 0;
                int actualDefaultCount = 0;

                int expectedMapIdSum = 0;
                List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> actualBDTScPris =
                        bdtScPriRestriRepository.findByBdtScId(actualDtSc.getDtScId());

                for (int j = 0; j < actualBDTScPris.size(); j++) {
                    BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPri = actualBDTScPris.get(j);

                    actualCodeListId += bdtScPri.getCodeListId();
                    actualAgencyIdListId += bdtScPri.getAgencyIdListId();
                    actualMapIdSum += bdtScPri.getCdtScAwdPriXpsTypeMapId();
                    if (bdtScPri.isDefault()) {
                        actualDefaultMapId = bdtScPri.getCdtScAwdPriXpsTypeMapId();
                        actualDefaultCount++;
                    }
                }

                assertTrue("The following dt_sc has no default in bdt_sc_pri_restri: " + actualDtSc,
                        actualDefaultMapId != 0);
                assertTrue("The following dt_sc has 2 or more default in bdt_sc_pri_restri: " + actualDtSc,
                        actualDefaultMapId == 1);

                if (expectedQBDTScPri.codeListId + expectedQBDTScPri.agencyIdListId > 0) {
                    expectedMapIdSum = getMapIdSum(actualDtSc, expectedQBDTScPri.attributeType, true);
                } else {
                    expectedMapIdSum = getMapIdSum(actualDtSc, expectedQBDTScPri.attributeType, false);
                }

                int expectedDefaultMapId = getDefaultMapId(actualDtSc, expectedQBDTScPri.attributeType);

                assertEquals(expectedQBDTScPri.codeListId, actualCodeListId);
                assertEquals(expectedQBDTScPri.agencyIdListId, actualAgencyIdListId);
                assertEquals("The following dt_sc has an incorrect default in bdt_sc_pri_restri: " + actualDtSc,
                        expectedDefaultMapId, actualDefaultMapId);
                assertEquals(1, actualDefaultCount);
                assertTrue(actualDefaultMapId > 0);
                assertEquals(expectedMapIdSum, actualMapIdSum);
            }
        }
    }


    public int getMapIdSum(DataTypeSupplementaryComponent dtSc, String type, boolean hasCodeListOrAgencyIdList) {
        // Assume that we only take new SC from attribute
        if (dtSc.getRepresentationTerm().equals("Value")) {
            int sum = 0;

            List<Integer> cdtPris = Arrays.asList(CDTPriDecimalId, CDTPriDoubleId, CDTPriDoubleId, CDTPriFloatId, CDTPriIntegerId, CDTPriIntegerId, CDTPriIntegerId, CDTPriNormalizedStringId, CDTPriStringId, CDTPriTokenId);
            List<Integer> xbts = Arrays.asList(xsdDecimalId, xsdDoubleId, xsdFloatId, xsdFloatId, xsdIntegerId, xsdNonNegativeIntegerId, xsdPositiveInteger, xsdNormalizedStringId, xsdStringId, xsdTokenId);

            for (int i = 0; i < cdtPris.size(); i++) {
                sum += getCdtScAwdPriXpsTypeMapId(dtSc, cdtPris.get(i), xbts.get(i));
            }

            return sum;
        } else if (hasCodeListOrAgencyIdList && "xsd:token".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriTokenId, xsdTokenId);
        } else if (dtSc.getRepresentationTerm().equals("Date Time") && "xsd:token".equals(type)) {
            int sum = 0;

            List<Integer> cdtPris = Arrays.asList(CDTPriTimePointId, CDTPriTimePointId, CDTPriTimePointId, CDTPriTimePointId, CDTPriTimePointId, CDTPriTimePointId, CDTPriTimePointId, CDTPriTimePointId, CDTPriTimePointId);
            List<Integer> xbts = Arrays.asList(xsdTokenId, xsdDateTimeId, xsdDateId, xsdTimeId, xsdGYearMonthId, xsdGYearId, xsdGMonthDayId, xsdGDayId, xsdGMonthId);

            for (int i = 0; i < cdtPris.size(); i++) {
                sum += getCdtScAwdPriXpsTypeMapId(dtSc, cdtPris.get(i), xbts.get(i));
            }

            return sum;
        } else if ("xbt_BooleanTrueFalseType".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriBooleanId, xbtBooleanTrueFalseTypeId);
        } else if ("xsd:decimal".equals(type)) {
            int sum = 0;

            List<Integer> cdtPris = Arrays.asList(CDTPriDecimalId, CDTPriDoubleId, CDTPriDoubleId, CDTPriFloatId, CDTPriIntegerId, CDTPriIntegerId, CDTPriIntegerId);
            List<Integer> xbts = Arrays.asList(xsdDecimalId, xsdDoubleId, xsdFloatId, xsdFloatId, xsdIntegerId, xsdNonNegativeIntegerId, xsdPositiveInteger);

            for (int i = 0; i < cdtPris.size(); i++) {
                sum += getCdtScAwdPriXpsTypeMapId(dtSc, cdtPris.get(i), xbts.get(i));
            }

            return sum;
        } else if ("xsd:normalizedString".equals(type) || "xsd:token".equals(type) || "xsd:string".equals(type)) {
            int sum = 0;

            List<Integer> cdtPris = Arrays.asList(CDTPriNormalizedStringId, CDTPriStringId, CDTPriTokenId);
            List<Integer> xbts = Arrays.asList(xsdNormalizedStringId, xsdStringId, xsdTokenId);

            for (int i = 0; i < cdtPris.size(); i++) {
                sum += getCdtScAwdPriXpsTypeMapId(dtSc, cdtPris.get(i), xbts.get(i));
            }

            return sum;
        }

        return -1;
    }

    public int getDefaultMapId(DataTypeSupplementaryComponent dtSc, String type) {
        // Assume that we only take new SC from attribute
        if (dtSc.getRepresentationTerm().equals("Date Time") && "xsd:token".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriTimePointId, xsdTokenId);
        } else if ("xsd:token".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriTokenId, xsdTokenId);
        } else if ("xbt_BooleanTrueFalseType".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriBooleanId, xbtBooleanTrueFalseTypeId);
        } else if ("xsd:decimal".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriDecimalId, xsdDecimalId);
        } else if ("xsd:normalizedString".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriNormalizedStringId, xsdNormalizedStringId);
        } else if ("xsd:string".equals(type)) {
            return getCdtScAwdPriXpsTypeMapId(dtSc, CDTPriStringId, xsdStringId);
        }

        return -1;
    }

    private int getCdtScAwdPriXpsTypeMapId(DataTypeSupplementaryComponent dtSc, int cdtPriId, int xbtId) {
        CoreDataTypeSupplementaryComponentAllowedPrimitive a =
                cdtSCAwdPriRepository.findOneByCdtScIdAndCdtPriId(dtSc.getDtScId(), cdtPriId);
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap map =
                cdtSCAwdXpsTypeMapRepository.findOneByCdtScAwdPriAndXbtId(a.getCdtScAwdPriId(), xbtId);
        return map.getCdtScAwdPriXpsTypeMapId();
    }

    private void test_bdt_sc_pri_restri(DataType expectedBasedDt, Collection<ExpectedContentType> expectedContentTypes) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> expectedBdtScPriRestriList =
                bdtScPriRestriRepository.findByBdtScIdIn(
                        dtScRepository.findByOwnerDtId(expectedBasedDt.getDtId()).stream()
                                .mapToInt(e -> e.getDtScId())
                                .boxed()
                                .collect(Collectors.toList())
                );

        expectedContentTypes.forEach(expectedContentType -> {
            DataType actualDataType = dtRepository.findOneByGuid(expectedContentType.getGuid());
            assertNotNull(actualDataType);

            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> actualBdtScPriRestriList =
                    bdtScPriRestriRepository.findByBdtScIdIn(
                            dtScRepository.findByOwnerDtId(actualDataType.getDtId()).stream()
                                    .mapToInt(e -> e.getDtScId())
                                    .boxed()
                                    .collect(Collectors.toList())
                    );

            assertEquals(expectedBdtScPriRestriList.size(), actualBdtScPriRestriList.size());

            // 'cdt_sc_awd_pri_xps_type_map_id', 'code_list_id' and 'agency_id_list_id' values check
            assertEquals(expectedBdtScPriRestriList.stream()
                            .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                            .sum(),
                    actualBdtScPriRestriList.stream()
                            .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                            .sum());

            // 'is_default' value check
            assertEquals(expectedBdtScPriRestriList.stream()
                            .filter(e -> e.isDefault())
                            .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                            .sum(),
                    actualBdtScPriRestriList.stream()
                            .filter(e -> e.isDefault())
                            .mapToInt(e -> e.getCdtScAwdPriXpsTypeMapId() + e.getCodeListId() + e.getAgencyIdListId())
                            .sum());

            actualBdtScPriRestriList.stream()
                    .collect(Collectors.groupingBy(
                            BusinessDataTypeSupplementaryComponentPrimitiveRestriction::getBdtScId))
                    .forEach(((bdtScId, bdtScPriRestriList) -> {
                        assertEquals("The summation of 'is_default' value is always be 1. bdt_sc_id: " + bdtScId,
                                1,
                                bdtScPriRestriList.stream()
                                        .mapToInt(e -> e.isDefault() ? 1 : 0)
                                        .sum());
                    }));
        });
    }

    @Test
    public void test_ImportQualifiedIDContentType() {
        List<ExpectedContentType> expectedID_QBDT = Arrays.asList(
                new ExpectedContentType("Agency", "oagis-id-0624ca38925d43929a6f7d8b7b7e4304", "clm63055D08B_AgencyIdentification")
        );

        DataType expectedBasedDt = dtRepository.findOneByTypeAndDen(1, "Identifier Content. Type");
        assertNotNull(expectedBasedDt);

        // 'dt' table check
        test_dt("Identifier", expectedBasedDt, expectedID_QBDT);

        // 'bdt_pri_restri' table check
        test_bdt_pri_restri("Identifier", expectedBasedDt, expectedID_QBDT);

        // 'dt_sc' table check
        test_dt_sc(expectedBasedDt, expectedID_QBDT);

        // 'bdt_sc_pri_restri' table check
        test_bdt_sc_pri_restri(expectedBasedDt, expectedID_QBDT);
    }

    private class ExpectedOtherQBDT {

        private String qualifier;
        private String dataTypeTerm;
        private String basedDtDen;
        private String guid;

        public ExpectedOtherQBDT(String qualifier, String dataTypeTerm, String basedDtDen, String guid) {
            this.qualifier = qualifier;
            this.dataTypeTerm = dataTypeTerm;
            this.basedDtDen = basedDtDen;
            this.guid = guid;
        }

        public String getQualifier() {
            return qualifier;
        }

        public String getDataTypeTerm() {
            return dataTypeTerm;
        }

        public String getBasedDtDen() {
            return basedDtDen;
        }

        public String getGuid() {
            return guid;
        }
    }

    @Test
    public void test_Populate_QBDT_InThe_dt_Table() {
        List<DataType> actualDataTypes =
                dtRepository.findByGuidIn(expectedOtherQBDTs.stream()
                        .map(ExpectedOtherQBDT::getGuid).collect(Collectors.toList()));

        assertEquals(expectedOtherQBDTs.size(), actualDataTypes.size());

        expectedOtherQBDTs.forEach(expectedOtherQBDT -> {
            DataType actualDataType = dtRepository.findOneByGuid(expectedOtherQBDT.getGuid());
            assertNotNull("QBDT[guid: " + expectedOtherQBDT.getGuid() + "] doesn't exist.", actualDataType);

            assertEquals("QBDT[guid: " + expectedOtherQBDT.getGuid() + "] data_type_term is different.",
                    expectedOtherQBDT.getDataTypeTerm(), actualDataType.getDataTypeTerm());
            assertEquals("QBDT[guid: " + expectedOtherQBDT.getGuid() + "] qualifier is different.",
                    expectedOtherQBDT.getQualifier(), actualDataType.getQualifier());

            String basedDtDen = expectedOtherQBDT.getBasedDtDen();
            DataType expectedBasedDataType = dtRepository.findOneByTypeAndDen(1, basedDtDen);
            assertNotNull("QBDT[guid: " + expectedOtherQBDT.getGuid() + "] based data type doesn't exist.",
                    expectedBasedDataType);

            assertEquals("QBDT[guid: " + expectedOtherQBDT.getGuid() + "] based_dt_id is different.",
                    expectedBasedDataType.getDtId(), actualDataType.getBasedDtId());

            String expectedDen = new StringBuilder(expectedOtherQBDT.getQualifier())
                    .append("_ ")
                    .append(basedDtDen.replaceAll("_[\\w]{6}. ", ". ")) // DENxUUID
                    .toString();

            assertEquals(expectedDen, actualDataType.getDen());
            assertEquals(expectedDen.replace(". Type", ". Content"), actualDataType.getContentComponentDen());

            assertEquals(3, actualDataType.getState());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getCreatedBy());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getOwnerUserId());
            assertEquals(userRepository.findAppUserIdByLoginId("oagis"), actualDataType.getLastUpdatedBy());
            assertEquals(0, actualDataType.getRevisionNum());
            assertEquals(0, actualDataType.getRevisionAction());
            assertEquals(false, actualDataType.isDeprecated());
        });
    }

    @Test
    public void test_Populate_bdt_pri_restri_Table() {
        for (int i = 0; i < expectedOtherQBDTs.size(); i++) {
            DataType expectedBasedDt = dtRepository.findOneByTypeAndDen(1, expectedOtherQBDTs.get(i).getBasedDtDen());
            assertNotNull(expectedBasedDt);
            test_bdt_sc_pri_restri(expectedBasedDt, expectedOtherQBDTs.get(i));
        }
    }

    @Test
    public void test_PopulateSCInThe_dt_sc_Table() {

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

    @Test
    public void test_PopulateThe_bccp_table() {

    }

}
