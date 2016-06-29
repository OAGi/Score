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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

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

    @Before
    public void setUp() {
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
        List<ExpectedOtherQBDT> expectedOtherQBDTs = Arrays.asList(
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
