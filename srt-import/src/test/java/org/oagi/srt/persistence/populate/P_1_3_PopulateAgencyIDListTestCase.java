package org.oagi.srt.persistence.populate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.config.ImportConfig;
import org.oagi.srt.config.TestRepositoryConfig;
import org.oagi.srt.repository.AgencyIdListRepository;
import org.oagi.srt.repository.AgencyIdListValueRepository;
import org.oagi.srt.repository.entity.AgencyIdList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        TestRepositoryConfig.class,
        ImportConfig.class
})
public class P_1_3_PopulateAgencyIDListTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private P_1_3_PopulateAgencyIDList populateAgencyIDList;

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        populateAgencyIDList.run(applicationContext);
    }

    @Test
    public void test_PopulateAgencyIdListTable() {
        assertEquals(1, agencyIdListRepository.count());

        String expectedGuid = "oagis-id-f1df540ef0db48318f3a423b3057955f";
        AgencyIdList agencyIdList = agencyIdListRepository.findOneByGuid(expectedGuid);

        assertNotNull(agencyIdList);
        assertEquals("oagis-id-68a3c03a4ea84562bd783fe2dc8f5487", agencyIdList.getEnumTypeGuid());
        assertEquals(agencyIdListValueRepository.findOneByValue("6").getAgencyIdListValueId(), agencyIdList.getAgencyId());
        assertEquals("Agency Identification", agencyIdList.getName());
        assertEquals("3055", agencyIdList.getListId());
        assertEquals("D13A", agencyIdList.getVersionId());
        assertEquals("Schema agency:  UN/CEFACT\n" +
                "Schema version: 4.5\n" +
                "Schema date:    02 February 2014\n" +
                "\n" +
                "Code list name:     Agency Identification Code\n" +
                "Code list agency:   UNECE\n" +
                "Code list version:  D13A", agencyIdList.getDefinition());
    }

    private class ExpectedAgencyIdListValue {
        private String name;
        private String description;

        public ExpectedAgencyIdListValue(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    @Test
    public void testPopulateAgencyIdListValueTable() {
        assertEquals(395, agencyIdListValueRepository.count());

        Map<String, ExpectedAgencyIdListValue> expectedAgencyIdListValues = new HashMap();
        expectedAgencyIdListValues.put("1", new ExpectedAgencyIdListValue("CCC (Customs Co-operation Council)", "Customs Co-operation Council (now World Customs Organization)."));
        expectedAgencyIdListValues.put("2", new ExpectedAgencyIdListValue("CEC (Commission of the European Communities)", "Generic: see also 140, 141, 142, 162."));
        expectedAgencyIdListValues.put("3", new ExpectedAgencyIdListValue("IATA (International Air Transport Association)", "The airline industry's international organisation."));
        expectedAgencyIdListValues.put("4", new ExpectedAgencyIdListValue("ICC (International Chamber of Commerce)", "International Chamber of Commerce."));
        expectedAgencyIdListValues.put("5", new ExpectedAgencyIdListValue("ISO (International Organization for Standardization)", "International Organization of Standardization."));
        expectedAgencyIdListValues.put("6", new ExpectedAgencyIdListValue("UN/ECE (United Nations - Economic Commission for Europe)", "United Nations Economic Commission for Europe."));
        expectedAgencyIdListValues.put("7", new ExpectedAgencyIdListValue("CEFIC (Conseil Europeen des Federations de l'Industrie Chimique)", "EDI project for chemical industry."));
        expectedAgencyIdListValues.put("8", new ExpectedAgencyIdListValue("EDIFICE", "Standardised electronic commerce forum for companies with interests in computing, electronics and telecommunications."));
        expectedAgencyIdListValues.put("9", new ExpectedAgencyIdListValue("GS1", "GS1 (formerly EAN International), an organisation of GS1 Member Organisations, which manages the GS1 System."));
        expectedAgencyIdListValues.put("10", new ExpectedAgencyIdListValue("ODETTE", "Organization for Data Exchange through Tele-Transmission in Europe (European automotive industry project)."));
        expectedAgencyIdListValues.put("11", new ExpectedAgencyIdListValue("Lloyd's register of shipping", "A register of ocean going vessels maintained by Lloyd's of London."));
        expectedAgencyIdListValues.put("12", new ExpectedAgencyIdListValue("UIC (International union of railways)", "International Union of Railways."));
        expectedAgencyIdListValues.put("13", new ExpectedAgencyIdListValue("ICAO (International Civil Aviation Organization)", "International Civil Aviation Organization."));
        expectedAgencyIdListValues.put("14", new ExpectedAgencyIdListValue("ICS (International Chamber of Shipping)", "International Chamber of Shipping."));
        expectedAgencyIdListValues.put("15", new ExpectedAgencyIdListValue("RINET (Reinsurance and Insurance Network)", "Reinsurance and Insurance Network."));
        expectedAgencyIdListValues.put("16", new ExpectedAgencyIdListValue("US, D&B (Dun & Bradstreet Corporation)", "Identifies the Dun & Bradstreet Corporation, United States."));
        expectedAgencyIdListValues.put("17", new ExpectedAgencyIdListValue("S.W.I.F.T.", "Society for Worldwide Interbank Financial Telecommunications s.c."));
        expectedAgencyIdListValues.put("18", new ExpectedAgencyIdListValue("Conventions on SAD and transit (EC and EFTA)", "SAD = Single Administrative Document."));
        expectedAgencyIdListValues.put("19", new ExpectedAgencyIdListValue("FRRC (Federal Reserve Routing Code)", "Federal Reserve Routing Code."));
        expectedAgencyIdListValues.put("20", new ExpectedAgencyIdListValue("BIC (Bureau International des Containeurs)", "The container industry's international organisation responsible for the issuance of container-related codes."));
        expectedAgencyIdListValues.put("21", new ExpectedAgencyIdListValue("Assigned by transport company", "Codes assigned by a transport company."));
        expectedAgencyIdListValues.put("22", new ExpectedAgencyIdListValue("US, ISA (Information Systems Agreement)", "Codes assigned by the ISA for use by its members."));
        expectedAgencyIdListValues.put("23", new ExpectedAgencyIdListValue("FR, EDITRANSPORT", "French association developing EDI in transport logistics."));
        expectedAgencyIdListValues.put("24", new ExpectedAgencyIdListValue("AU, ROA (Railways of Australia)", "Maintains code lists which are accepted by Australian government railways."));
        expectedAgencyIdListValues.put("25", new ExpectedAgencyIdListValue("EDITEX (Europe)", "EDI group for the textile and clothing industry."));
        expectedAgencyIdListValues.put("26", new ExpectedAgencyIdListValue("NL, Foundation Uniform Transport Code", "Foundation Uniform Transport Code is the EDI organisation for shippers, carriers and other logistic service providers in the Netherlands."));
        expectedAgencyIdListValues.put("27", new ExpectedAgencyIdListValue("US, FDA (Food and Drug Administration)", "U.S. food and drug administration."));
        expectedAgencyIdListValues.put("28", new ExpectedAgencyIdListValue("EDITEUR (European book sector electronic data interchange group)", "Code identifying the pan European user group for the book industry as an organisation responsible for code values in the book industry."));
        expectedAgencyIdListValues.put("29", new ExpectedAgencyIdListValue("GB, FLEETNET", "Association of fleet vehicle hiring and leasing companies in the UK."));
        expectedAgencyIdListValues.put("30", new ExpectedAgencyIdListValue("GB, ABTA (Association of British Travel Agencies)", "ABTA, Association of British Travel Agencies."));
        expectedAgencyIdListValues.put("31", new ExpectedAgencyIdListValue("FI, Finish State Railway", "Finish State Railway."));
        expectedAgencyIdListValues.put("32", new ExpectedAgencyIdListValue("PL, Polish State Railway", "Polish State Railway."));
        expectedAgencyIdListValues.put("33", new ExpectedAgencyIdListValue("BG, Bulgaria State Railway", "Bulgaria State Railway."));
        expectedAgencyIdListValues.put("34", new ExpectedAgencyIdListValue("RO, Rumanian State Railway", "Rumanian State Railway."));
        expectedAgencyIdListValues.put("35", new ExpectedAgencyIdListValue("CZ, Tchechian State Railway", "Tchechian State Railway."));
        expectedAgencyIdListValues.put("36", new ExpectedAgencyIdListValue("HU, Hungarian State Railway", "Hungarian State Railway."));
        expectedAgencyIdListValues.put("37", new ExpectedAgencyIdListValue("GB, British Railways", "British Railways."));
        expectedAgencyIdListValues.put("38", new ExpectedAgencyIdListValue("ES, Spanish National Railway", "Spanish National Railway."));
        expectedAgencyIdListValues.put("39", new ExpectedAgencyIdListValue("SE, Swedish State Railway", "Swedish State Railway."));
        expectedAgencyIdListValues.put("40", new ExpectedAgencyIdListValue("NO, Norwegian State Railway", "Norwegian State Railway."));
        expectedAgencyIdListValues.put("41", new ExpectedAgencyIdListValue("DE, German Railway", "German Railway."));
        expectedAgencyIdListValues.put("42", new ExpectedAgencyIdListValue("AT, Austrian Federal Railways", "Austrian Federal Railways."));
        expectedAgencyIdListValues.put("43", new ExpectedAgencyIdListValue("LU, Luxembourg National Railway Company", "Luxembourg National Railway Company."));
        expectedAgencyIdListValues.put("44", new ExpectedAgencyIdListValue("IT, Italian State Railways", "Italian State Railways."));
        expectedAgencyIdListValues.put("45", new ExpectedAgencyIdListValue("NL, Netherlands Railways", "Netherlands Railways."));
        expectedAgencyIdListValues.put("46", new ExpectedAgencyIdListValue("CH, Swiss Federal Railways", "Swiss Federal Railways."));
        expectedAgencyIdListValues.put("47", new ExpectedAgencyIdListValue("DK, Danish State Railways", "Danish State Railways."));
        expectedAgencyIdListValues.put("48", new ExpectedAgencyIdListValue("FR, French National Railway Company", "French National Railway Company."));
        expectedAgencyIdListValues.put("49", new ExpectedAgencyIdListValue("BE, Belgian National Railway Company", "Belgian National Railway Company."));
        expectedAgencyIdListValues.put("50", new ExpectedAgencyIdListValue("PT, Portuguese Railways", "Portuguese Railways."));
        expectedAgencyIdListValues.put("51", new ExpectedAgencyIdListValue("SK, Slovakian State Railways", "Slovakian State Railways."));
        expectedAgencyIdListValues.put("52", new ExpectedAgencyIdListValue("IE, Irish Transport Company", "Irish Transport Company."));
        expectedAgencyIdListValues.put("53", new ExpectedAgencyIdListValue("FIATA (International Federation of Freight Forwarders Associations)", "International Federation of Freight Forwarders Associations."));
        expectedAgencyIdListValues.put("54", new ExpectedAgencyIdListValue("IMO (International Maritime Organisation)", "International Maritime Organisation."));
        expectedAgencyIdListValues.put("55", new ExpectedAgencyIdListValue("US, DOT (United States Department of Transportation)", "United States Department of Transportation."));
        expectedAgencyIdListValues.put("56", new ExpectedAgencyIdListValue("TW, Trade-van", "Trade-van is an EDI/VAN service centre for customs, transport, and insurance in national and international trade."));
        expectedAgencyIdListValues.put("57", new ExpectedAgencyIdListValue("TW, Chinese Taipei Customs", "Customs authorities of Chinese Taipei responsible for collecting import duties and preventing smuggling."));
        expectedAgencyIdListValues.put("58", new ExpectedAgencyIdListValue("EUROFER", "European steel organisation - EDI project for the European steel industry."));
        expectedAgencyIdListValues.put("59", new ExpectedAgencyIdListValue("DE, EDIBAU", "National body responsible for the German codification in the construction area."));
        expectedAgencyIdListValues.put("60", new ExpectedAgencyIdListValue("Assigned by national trade agency", "The code list is from a national agency."));
        expectedAgencyIdListValues.put("61", new ExpectedAgencyIdListValue("Association Europeenne des Constructeurs de Materiel Aerospatial (AECMA)", "A code to identify the Association Europeenne des Constructeurs de Materiel Aeropsatial (European Association of Aerospace Products Manufacturers) as an authorizing agency for code lists."));
        expectedAgencyIdListValues.put("62", new ExpectedAgencyIdListValue("US, DIstilled Spirits Council of the United States (DISCUS)", "United States DIstilled Spirits Council of the United States (DISCUS)."));
        expectedAgencyIdListValues.put("63", new ExpectedAgencyIdListValue("North Atlantic Treaty Organization (NATO)", "A code to identify the North Atlantic Treaty Organization (NATO) as an authorizing agency for code lists."));
        expectedAgencyIdListValues.put("64", new ExpectedAgencyIdListValue("FR, CLEEP", "French association responsible for standardization, coordination and promotion of EDI and Data Exchange applications."));
        expectedAgencyIdListValues.put("65", new ExpectedAgencyIdListValue("GS1 France", "Organisation responsible for the GS1 System in France."));
        expectedAgencyIdListValues.put("66", new ExpectedAgencyIdListValue("MY, Malaysian Customs and Excise", "Malaysia Royal Customs and Excise."));
        expectedAgencyIdListValues.put("67", new ExpectedAgencyIdListValue("MY, Malaysia Central Bank", "Malaysia Central Bank is a regulatory body set up by the government to charge with promoting economic monetary and credit condition favourable to commercial and industrial activity."));
        expectedAgencyIdListValues.put("68", new ExpectedAgencyIdListValue("GS1 Italy", "Organisation responsible for the GS1 System in Italy."));
        expectedAgencyIdListValues.put("69", new ExpectedAgencyIdListValue("US, National Alcohol Beverage Control Association (NABCA)", "United States National Alcohol Beverage Control Association (NABCA)."));
        expectedAgencyIdListValues.put("70", new ExpectedAgencyIdListValue("MY, Dagang.Net", "Malaysia, Dagang.Net is a national clearing house which provide EDI/VAN service for customs, transport, retail and financial and other industries in the national and international trade."));
        expectedAgencyIdListValues.put("71", new ExpectedAgencyIdListValue("US, FCC (Federal Communications Commission)", "A code representing the United States Federal Communication Commission (FCC)."));
        expectedAgencyIdListValues.put("72", new ExpectedAgencyIdListValue("US, MARAD (Maritime Administration)", "A code representing the United States Maritime Administration (MARAD) under the Department of Transportation (DOT)."));
        expectedAgencyIdListValues.put("73", new ExpectedAgencyIdListValue("US, DSAA (Defense Security Assistance Agency)", "A code representing the United States Defense Security Assistance Agency (DSAA) under the Department of Defense (DOD)."));
        expectedAgencyIdListValues.put("74", new ExpectedAgencyIdListValue("US, NRC (Nuclear Regulatory Commission)", "A code representing the United States Nuclear Regulatory Commission (NRC)."));
        expectedAgencyIdListValues.put("75", new ExpectedAgencyIdListValue("US, ODTC (Office of Defense Trade Controls)", "A code representing the United States Office of Defense Trade Controls (ODTC) under the Department of State."));
        expectedAgencyIdListValues.put("76", new ExpectedAgencyIdListValue("US, ATF (Bureau of Alcohol, Tobacco and Firearms)", "A code representing the United States Bureau of Alcohol, Tobacco and Firearms, Department of Treasury (ATF)."));
        expectedAgencyIdListValues.put("77", new ExpectedAgencyIdListValue("US, BXA (Bureau of Export Administration)", "A code representing the United States Bureau of Export Administration (BXA) under the Department of Commerce (DOC) ."));
        expectedAgencyIdListValues.put("78", new ExpectedAgencyIdListValue("US, FWS (Fish and Wildlife Service)", "A code depicting the United States Fish and Wildlife Service (FWS)."));
        expectedAgencyIdListValues.put("79", new ExpectedAgencyIdListValue("US, OFAC (Office of Foreign Assets Control)", "A code representing the United States Office of Foreign Assets Controls (OFAC)."));
        expectedAgencyIdListValues.put("80", new ExpectedAgencyIdListValue("BRMA/RAA - LIMNET - RINET Joint Venture", "Joint venture between BRMA (Brokers & Reinsurance Markets Association) / RAA (Reinsurance Association of America) - LIMNET (London Insurance Market Network) - RINET (Reinsurance and Insurance Network)."));
        expectedAgencyIdListValues.put("81", new ExpectedAgencyIdListValue("RU, (SFT) Society for Financial Telecommunications", "Russian company representing the users of the Global Financial Telecommunication Network (GFTN)."));
        expectedAgencyIdListValues.put("82", new ExpectedAgencyIdListValue("NO, Enhetsregisteret ved Bronnoysundregisterne", "The co-ordinating register for companies and business units of companies at the Bronnoysund register centre."));
        expectedAgencyIdListValues.put("83", new ExpectedAgencyIdListValue("US, National Retail Federation", "The National Retail Federation is the trade association for the general merchandise retailing industry. In addition to providing support and education services, they also maintain and publish standard colour and size codes for the retail industry."));
        expectedAgencyIdListValues.put("84", new ExpectedAgencyIdListValue("DE, BRD (Gesetzgeber der Bundesrepublik Deutschland)", "German legislature."));
        expectedAgencyIdListValues.put("85", new ExpectedAgencyIdListValue("North America, Telecommunications Industry Forum", "Trade association representing telecommunications service providers, equipment manufacturers, suppliers to the industry and customers."));
        expectedAgencyIdListValues.put("86", new ExpectedAgencyIdListValue("Assigned by party originating the message", "Codes assigned by the party originating the message."));
        expectedAgencyIdListValues.put("87", new ExpectedAgencyIdListValue("Assigned by carrier", "Codes assigned by the carrier."));
        expectedAgencyIdListValues.put("88", new ExpectedAgencyIdListValue("Assigned by owner of operation", "Assigned by owner of operation (e.g. used in construction)."));
        expectedAgencyIdListValues.put("89", new ExpectedAgencyIdListValue("Assigned by distributor", "Codes assigned by a distributor."));
        expectedAgencyIdListValues.put("90", new ExpectedAgencyIdListValue("Assigned by manufacturer", "Codes assigned by a manufacturer."));
        expectedAgencyIdListValues.put("91", new ExpectedAgencyIdListValue("Assigned by seller or seller's agent", "Codes assigned by a seller or seller's agent."));
        expectedAgencyIdListValues.put("92", new ExpectedAgencyIdListValue("Assigned by buyer or buyer's agent", "Codes assigned by a buyer or buyer's agent."));
        expectedAgencyIdListValues.put("93", new ExpectedAgencyIdListValue("AT, Austrian Customs", "Austrian customs organization."));
        expectedAgencyIdListValues.put("94", new ExpectedAgencyIdListValue("AT, Austrian PTT", "The Austrian organization responsible for assigning telephone (voice/data) + telex numbers, postcodes, and postal account numbers."));
        expectedAgencyIdListValues.put("95", new ExpectedAgencyIdListValue("AU, Australian Customs Service", "Australian Customs Service."));
        expectedAgencyIdListValues.put("96", new ExpectedAgencyIdListValue("CA, Revenue Canada, Customs and Excise", "Canada Customs and Revenue Agency."));
        expectedAgencyIdListValues.put("97", new ExpectedAgencyIdListValue("CH, Administration federale des contributions", "Indirect taxation (e.g. turn-over/sales taxes)."));
        expectedAgencyIdListValues.put("98", new ExpectedAgencyIdListValue("CH, Direction generale des douanes", "Customs (incl. ISO alpha 2 country code)."));
        expectedAgencyIdListValues.put("99", new ExpectedAgencyIdListValue("CH, Division des importations et exportations, OFAEE", "Import and export licences."));
        expectedAgencyIdListValues.put("100", new ExpectedAgencyIdListValue("CH, Entreprise des PTT", "Telephone (voice/data) + telex numbers, postcodes, postal account numbers."));
        expectedAgencyIdListValues.put("101", new ExpectedAgencyIdListValue("CH, Carbura", "Centrale suisse pour l'importation de carburants et combustibles liquides (Oil products)."));
        expectedAgencyIdListValues.put("102", new ExpectedAgencyIdListValue("CH, Centrale suisse pour l'importation du charbon", "Coal."));
        expectedAgencyIdListValues.put("103", new ExpectedAgencyIdListValue("CH, Office fiduciaire des importateurs de denrees alimentaires", "Foodstuff."));
        expectedAgencyIdListValues.put("104", new ExpectedAgencyIdListValue("CH, Association suisse code des articles", "Swiss article numbering association."));
        expectedAgencyIdListValues.put("105", new ExpectedAgencyIdListValue("DK, Ministry of taxation, Central Customs and Tax Administration", "Danish Customs administration."));
        expectedAgencyIdListValues.put("106", new ExpectedAgencyIdListValue("FR, Direction generale des douanes et droits indirects", "French Customs."));
        expectedAgencyIdListValues.put("107", new ExpectedAgencyIdListValue("FR, INSEE", "Institut National de la Statistique et des Etudes Economiques."));
        expectedAgencyIdListValues.put("108", new ExpectedAgencyIdListValue("FR, Banque de France", "Banque de France."));
        expectedAgencyIdListValues.put("109", new ExpectedAgencyIdListValue("GB, H.M. Customs & Excise", "United Kingdom H.M. Customs and Excise."));
        expectedAgencyIdListValues.put("110", new ExpectedAgencyIdListValue("IE, Revenue Commissioners, Customs AEP project", "Ireland Revenue Commissioners Customs Automated Entry Processing project."));
        expectedAgencyIdListValues.put("111", new ExpectedAgencyIdListValue("US, U.S. Customs Service", "United States Customs Service."));
        expectedAgencyIdListValues.put("112", new ExpectedAgencyIdListValue("US, U.S. Census Bureau", "The Bureau of the Census of the U.S. Dept. of Commerce."));
        expectedAgencyIdListValues.put("113", new ExpectedAgencyIdListValue("GS1 US", "Organisation responsible for the GS1 System in the USA."));
        expectedAgencyIdListValues.put("114", new ExpectedAgencyIdListValue("US, ABA (American Bankers Association)", "United States American Bankers Association."));
        expectedAgencyIdListValues.put("116", new ExpectedAgencyIdListValue("US, ANSI ASC X12", "American National Standards Institute ASC X12."));
        expectedAgencyIdListValues.put("117", new ExpectedAgencyIdListValue("AT, Geldausgabeautomaten-Service Gesellschaft m.b.H.", "Austrian Geldausgabeautomaten-Service Gesellschaft m.b.H."));
        expectedAgencyIdListValues.put("118", new ExpectedAgencyIdListValue("SE, Svenska Bankfoereningen", "Swedish bankers association."));
        expectedAgencyIdListValues.put("119", new ExpectedAgencyIdListValue("IT, Associazione Bancaria Italiana", "Italian Associazione Bancaria Italiana."));
        expectedAgencyIdListValues.put("120", new ExpectedAgencyIdListValue("IT, Socieata' Interbancaria per l'Automazione", "Italian Socieata' Interbancaria per l'Automazione."));
        expectedAgencyIdListValues.put("121", new ExpectedAgencyIdListValue("CH, Telekurs AG", "Swiss Telekurs AG."));
        expectedAgencyIdListValues.put("122", new ExpectedAgencyIdListValue("CH, Swiss Securities Clearing Corporation", "Swiss Securities Clearing Corporation."));
        expectedAgencyIdListValues.put("123", new ExpectedAgencyIdListValue("NO, Norwegian Interbank Research Organization", "Norwegian Interbank Research Organization."));
        expectedAgencyIdListValues.put("124", new ExpectedAgencyIdListValue("NO, Norwegian Bankers' Association", "Norwegian Bankers' Association."));
        expectedAgencyIdListValues.put("125", new ExpectedAgencyIdListValue("FI, The Finnish Bankers' Association", "Finnish Bankers' Association."));
        expectedAgencyIdListValues.put("126", new ExpectedAgencyIdListValue("US, NCCMA (Account Analysis Codes)", "The United States organization responsible for issuing account analysis codes."));
        expectedAgencyIdListValues.put("127", new ExpectedAgencyIdListValue("DE, ARE (AbRechnungs Einheit)", "A German code for subsidiary unit number."));
        expectedAgencyIdListValues.put("128", new ExpectedAgencyIdListValue("BE, Belgian Bankers' Association", "Belgian Bankers' Association."));
        expectedAgencyIdListValues.put("129", new ExpectedAgencyIdListValue("BE, Belgian Ministry of Finance", "VAT numbers."));
        expectedAgencyIdListValues.put("130", new ExpectedAgencyIdListValue("DK, Danish Bankers Association", "Code identifying the organisation responsible for the issuance of bank related codes in Denmark."));
        expectedAgencyIdListValues.put("131", new ExpectedAgencyIdListValue("DE, German Bankers Association", "German Bankers' Association."));
        expectedAgencyIdListValues.put("132", new ExpectedAgencyIdListValue("GB, BACS Limited", "An organization that operates the United Kingdom's electronic fund transfer service on behalf of the major Banks and Building Societies."));
        expectedAgencyIdListValues.put("133", new ExpectedAgencyIdListValue("GB, Association for Payment Clearing Services", "British Association for Payment Clearing Services."));
        expectedAgencyIdListValues.put("134", new ExpectedAgencyIdListValue("GB, APACS (Association of payment clearing services)", "The association which manages the United Kingdom payment clearing system, and among other thing manages the UK bank sort code numbering system."));
        expectedAgencyIdListValues.put("135", new ExpectedAgencyIdListValue("GB, The Clearing House", "British financial transaction clearing house."));
        expectedAgencyIdListValues.put("136", new ExpectedAgencyIdListValue("GS1 UK", "Organisation responsible for the GS1 System in the United Kingdom."));
        expectedAgencyIdListValues.put("137", new ExpectedAgencyIdListValue("AT, Verband oesterreichischer Banken und Bankiers", "Austrian bankers association."));
        expectedAgencyIdListValues.put("138", new ExpectedAgencyIdListValue("FR, CFONB (Comite francais d'organ. et de normalisation bancaires)", "National body responsible for the French codification in banking activity."));
        expectedAgencyIdListValues.put("139", new ExpectedAgencyIdListValue("Universal Postal Union (UPU)", "The Universal Postal Union (UPU) is an international organization that coordinates postal policies between member nations, and hence the world-wide postal system."));
        expectedAgencyIdListValues.put("140", new ExpectedAgencyIdListValue("CEC (Commission of the European Communities), DG/XXI-01", "Responsible for computerization within Customs area."));
        expectedAgencyIdListValues.put("141", new ExpectedAgencyIdListValue("CEC (Commission of the European Communities), DG/XXI-B-1", "International Commission of the European Communities."));
        expectedAgencyIdListValues.put("142", new ExpectedAgencyIdListValue("CEC (Commission of the European Communities), DG/XXXIV", "Statistical Office of the European Communities: e.g. Geonomenclature."));
        expectedAgencyIdListValues.put("143", new ExpectedAgencyIdListValue("NZ, New Zealand Customs", "New Zealand Customs."));
        expectedAgencyIdListValues.put("144", new ExpectedAgencyIdListValue("NL, Netherlands Customs", "Netherlands Customs."));
        expectedAgencyIdListValues.put("145", new ExpectedAgencyIdListValue("SE, Swedish Customs", "Swedish Customs."));
        expectedAgencyIdListValues.put("146", new ExpectedAgencyIdListValue("DE, German Customs", "German Customs."));
        expectedAgencyIdListValues.put("147", new ExpectedAgencyIdListValue("BE, Belgian Customs", "Belgian Customs."));
        expectedAgencyIdListValues.put("148", new ExpectedAgencyIdListValue("ES, Spanish Customs", "Spanish Customs."));
        expectedAgencyIdListValues.put("149", new ExpectedAgencyIdListValue("IL, Israel Customs", "Israeli Customs."));
        expectedAgencyIdListValues.put("150", new ExpectedAgencyIdListValue("HK, Hong Kong Customs", "Hong Kong Customs."));
        expectedAgencyIdListValues.put("151", new ExpectedAgencyIdListValue("JP, Japan Customs", "Japan Customs."));
        expectedAgencyIdListValues.put("152", new ExpectedAgencyIdListValue("SA, Saudi Arabia Customs", "Saudi Arabia Customs."));
        expectedAgencyIdListValues.put("153", new ExpectedAgencyIdListValue("IT, Italian Customs", "Italian Customs."));
        expectedAgencyIdListValues.put("154", new ExpectedAgencyIdListValue("GR, Greek Customs", "Greek Customs."));
        expectedAgencyIdListValues.put("155", new ExpectedAgencyIdListValue("PT, Portuguese Customs", "Portuguese Customs."));
        expectedAgencyIdListValues.put("156", new ExpectedAgencyIdListValue("LU, Luxembourg Customs", "Luxembourg Customs."));
        expectedAgencyIdListValues.put("157", new ExpectedAgencyIdListValue("NO, Norwegian Customs", "Norwegian Customs."));
        expectedAgencyIdListValues.put("158", new ExpectedAgencyIdListValue("FI, Finnish Customs", "Finnish Customs."));
        expectedAgencyIdListValues.put("159", new ExpectedAgencyIdListValue("IS, Iceland Customs", "Iceland Customs."));
        expectedAgencyIdListValues.put("160", new ExpectedAgencyIdListValue("LI, Liechtenstein authority", "Identification of relevant responsible agency for e.g. banking/financial matters still pending. For e.g. Customs, currency, post/telephone: see relevant CH entry."));
        expectedAgencyIdListValues.put("161", new ExpectedAgencyIdListValue("UNCTAD (United Nations - Conference on Trade And Development)", "United Nations - Conference on Trade And Development."));
        expectedAgencyIdListValues.put("162", new ExpectedAgencyIdListValue("CEC (Commission of the European Communities), DG/XIII-D-5", "Responsible for TEDIS - incl. CEBIS -, INSIS and CADDIA projects."));
        expectedAgencyIdListValues.put("163", new ExpectedAgencyIdListValue("US, FMC (Federal Maritime Commission)", "United States Federal Maritime Commission."));
        expectedAgencyIdListValues.put("164", new ExpectedAgencyIdListValue("US, DEA (Drug Enforcement Agency)", "United States Drug Enforcement Agency."));
        expectedAgencyIdListValues.put("165", new ExpectedAgencyIdListValue("US, DCI (Distribution Codes, INC.)", "United States Distribution Codes, Inc. organization."));
        expectedAgencyIdListValues.put("166", new ExpectedAgencyIdListValue("US, National Motor Freight Classification Association", "The organisation in the USA which is responsible for code maintenance in the trucking industry."));
        expectedAgencyIdListValues.put("167", new ExpectedAgencyIdListValue("US, AIAG (Automotive Industry Action Group)", "United States Automotive Industry Action Group."));
        expectedAgencyIdListValues.put("168", new ExpectedAgencyIdListValue("US, FIPS (Federal Information Publishing Standard)", "A code issued by the United States National Institute for Science and Technology (NIST) to identify a Federal Information Publishing Standard."));
        expectedAgencyIdListValues.put("169", new ExpectedAgencyIdListValue("CA, SCC (Standards Council of Canada)", "Standards Council of Canada."));
        expectedAgencyIdListValues.put("170", new ExpectedAgencyIdListValue("CA, CPA (Canadian Payment Association)", "Canadian Payment Association."));
        expectedAgencyIdListValues.put("171", new ExpectedAgencyIdListValue("NL, Interpay Girale Services", "Interpay Girale Services."));
        expectedAgencyIdListValues.put("172", new ExpectedAgencyIdListValue("NL, Interpay Debit Card Services", "Interpay Debit Card Services."));
        expectedAgencyIdListValues.put("173", new ExpectedAgencyIdListValue("NO, NORPRO", "Norwegian electronic data interchange standards organization."));
        expectedAgencyIdListValues.put("174", new ExpectedAgencyIdListValue("DE, DIN (Deutsches Institut fuer Normung)", "German standardization institute."));
        expectedAgencyIdListValues.put("175", new ExpectedAgencyIdListValue("FCI (Factors Chain International)", "Factors Chain International."));
        expectedAgencyIdListValues.put("176", new ExpectedAgencyIdListValue("BR, Banco Central do Brazil", "Brazilian central bank."));
        expectedAgencyIdListValues.put("177", new ExpectedAgencyIdListValue("AU, LIFA (Life Insurance Federation of Australia)", "Life Insurance Federation of Australia."));
        expectedAgencyIdListValues.put("178", new ExpectedAgencyIdListValue("AU, SAA (Standards Association of Australia)", "Standards Association of Australia."));
        expectedAgencyIdListValues.put("179", new ExpectedAgencyIdListValue("US, Air transport association of America", "U.S. -based trade association representing the major North American scheduled airlines."));
        expectedAgencyIdListValues.put("180", new ExpectedAgencyIdListValue("DE, BIA (Berufsgenossenschaftliches Institut fuer Arbeitssicherheit)", "German institute of the workmen's compensation board."));
        expectedAgencyIdListValues.put("181", new ExpectedAgencyIdListValue("Edibuild", "EDI organization for companies in the construction industry."));
        expectedAgencyIdListValues.put("182", new ExpectedAgencyIdListValue("US, Standard Carrier Alpha Code (Motor)", "Organisation maintaining the SCAC lists and transportation operating in North America."));
        expectedAgencyIdListValues.put("183", new ExpectedAgencyIdListValue("US, American Petroleum Institute", "US-based trade association representing oil and natural gas producers, shippers, refineries, marketers, and major suppliers to the industry."));
        expectedAgencyIdListValues.put("184", new ExpectedAgencyIdListValue("AU, ACOS (Australian Chamber of Shipping)", "The national organisation for the maritime industry in Australia."));
        expectedAgencyIdListValues.put("185", new ExpectedAgencyIdListValue("DE, BDI (Bundesverband der Deutschen Industrie e.V.)", "German industry association."));
        expectedAgencyIdListValues.put("186", new ExpectedAgencyIdListValue("US, GSA (General Services Administration)", "The US General Services Administration."));
        expectedAgencyIdListValues.put("187", new ExpectedAgencyIdListValue("US, DLMSO (Defense Logistics Management Standards Office)", "The Defense Logistics Management Standards Office."));
        expectedAgencyIdListValues.put("188", new ExpectedAgencyIdListValue("US, NIST (National Institute of Standards and Technology)", "The US National Institute of Standards and Technology."));
        expectedAgencyIdListValues.put("189", new ExpectedAgencyIdListValue("US, DoD (Department of Defense)", "The US Department of Defense."));
        expectedAgencyIdListValues.put("190", new ExpectedAgencyIdListValue("US, VA (Department of Veterans Affairs)", "The Department of Veterans Affairs."));
        expectedAgencyIdListValues.put("191", new ExpectedAgencyIdListValue("IAPSO (United Nations Inter-Agency Procurement Services Office)", "United Nations organization responsible for maintaining the United Nations Common Coding System (UNCCS) which is used extensively by UN agencies in procurement and statistical analysis."));
        expectedAgencyIdListValues.put("192", new ExpectedAgencyIdListValue("Shipper's association", "Code assigned by a shipper's association."));
        expectedAgencyIdListValues.put("193", new ExpectedAgencyIdListValue("EU, European Telecommunications Informatics Services (ETIS)", "European Telecommunications Informatics Services is a non-profit cooperative organisation owned by European public network operators, working in the field of information technology."));
        expectedAgencyIdListValues.put("194", new ExpectedAgencyIdListValue("AU, AQIS (Australian Quarantine and Inspection Service)", "Australian Quarantine and Inspection Service."));
        expectedAgencyIdListValues.put("195", new ExpectedAgencyIdListValue("CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)", "The Colombian customs organization."));
        expectedAgencyIdListValues.put("196", new ExpectedAgencyIdListValue("US, COPAS (Council of Petroleum Accounting Society)", "Organization supplying codes of oil field equipment and tubular goods used by joint operators in the petroleum industry."));
        expectedAgencyIdListValues.put("197", new ExpectedAgencyIdListValue("US, DISA (Data Interchange Standards Association)", "The organization maintaining code lists under the administration of the data interchange standards association."));
        expectedAgencyIdListValues.put("198", new ExpectedAgencyIdListValue("CO, Superintendencia Bancaria De Colombia", "The organization which assigns identification numbers to financial institutions conducting business in Colombia."));
        expectedAgencyIdListValues.put("199", new ExpectedAgencyIdListValue("FR, Direction de la Comptabilite Publique", "The French public accounting office."));
        expectedAgencyIdListValues.put("200", new ExpectedAgencyIdListValue("GS1 Netherlands", "Organisation responsible for the GS1 System in The Netherlands."));
        expectedAgencyIdListValues.put("201", new ExpectedAgencyIdListValue("US, WSSA(Wine and Spirits Shippers Association)", "United States based Wine and Spirits Shippers association."));
        expectedAgencyIdListValues.put("202", new ExpectedAgencyIdListValue("PT, Banco de Portugal", "Portuguese Central Bank."));
        expectedAgencyIdListValues.put("203", new ExpectedAgencyIdListValue("FR, GALIA (Groupement pour l'Amelioration des Liaisons dans l'Industrie Automobile)", "The national organisation representing France in ODETTE (Organisation for Data Exchanges through Tele-Transmission in Europe)."));
        expectedAgencyIdListValues.put("204", new ExpectedAgencyIdListValue("DE, VDA (Verband der Automobilindustrie E.V.)", "The national organisation representing Germany in ODETTE (Organisation for Data Exchange through Tele-Transmission in Europe)."));
        expectedAgencyIdListValues.put("205", new ExpectedAgencyIdListValue("IT, ODETTE Italy", "The national organisation representing Italy in ODETTE (Organisation for Data Exchange through Tele-Transmission in Europe)."));
        expectedAgencyIdListValues.put("206", new ExpectedAgencyIdListValue("NL, ODETTE Netherlands", "The national organisation representing Netherlands in ODETTE (Organisation for Data Exchange through Tele-Transmission in Europe)."));
        expectedAgencyIdListValues.put("207", new ExpectedAgencyIdListValue("ES, ODETTE Spain", "The national organisation representing Spain in ODETTE (Organisation for Data Exchange through Tele-Transmission in Europe)."));
        expectedAgencyIdListValues.put("208", new ExpectedAgencyIdListValue("SE, ODETTE Sweden", "The national organisation representing Scandinavian countries in ODETTE (Organisation for Data Exchange through Tele-Transmission in Europe)."));
        expectedAgencyIdListValues.put("209", new ExpectedAgencyIdListValue("GB, ODETTE United Kingdom", "The national organisation representing UK in ODETTE (Organisation for Data Exchange through Tele-Transmission in Europe)."));
        expectedAgencyIdListValues.put("210", new ExpectedAgencyIdListValue("EU, EDI for financial, informational, cost, accounting, auditing and social areas (EDIFICAS) - Europe", "European association dealing with accounting and auditing."));
        expectedAgencyIdListValues.put("211", new ExpectedAgencyIdListValue("FR, EDI for financial, informational, cost, accounting, auditing and social areas (EDIFICAS) - France", "French association dealing with accounting and auditing."));
        expectedAgencyIdListValues.put("212", new ExpectedAgencyIdListValue("DE, Deutsch Telekom AG", "German telecommunication services agency."));
        expectedAgencyIdListValues.put("213", new ExpectedAgencyIdListValue("JP, NACCS Center (Nippon Automated Cargo Clearance System Operations Organization)", "NACCS (Nippon Automated Cargo Clearance System Operation Organization) Center is the operations organization of the automated cargo clearance system in Japan."));
        expectedAgencyIdListValues.put("214", new ExpectedAgencyIdListValue("US, AISI (American Iron and Steel Institute)", "American iron and steel institute."));
        expectedAgencyIdListValues.put("215", new ExpectedAgencyIdListValue("AU, APCA (Australian Payments Clearing Association)", "Australian association responsible for the management of payment clearing."));
        expectedAgencyIdListValues.put("216", new ExpectedAgencyIdListValue("US, Department of Labor", "To identify the United States department of labour."));
        expectedAgencyIdListValues.put("217", new ExpectedAgencyIdListValue("US, N.A.I.C. (National Association of Insurance Commissioners)", "To identify the United States, National Association of Insurance Commissioners."));
        expectedAgencyIdListValues.put("218", new ExpectedAgencyIdListValue("GB, The Association of British Insurers", "An association that administers code lists on behalf of the UK insurance community."));
        expectedAgencyIdListValues.put("219", new ExpectedAgencyIdListValue("FR, d'ArvA", "Value added network administering insurance code lists on behalf of the French insurance community."));
        expectedAgencyIdListValues.put("220", new ExpectedAgencyIdListValue("FI, Finnish tax board", "Finnish tax board."));
        expectedAgencyIdListValues.put("221", new ExpectedAgencyIdListValue("FR, CNAMTS (Caisse Nationale de l'Assurance Maladie des Travailleurs Salaries)", "The French public institution funding health-care for salaried workers."));
        expectedAgencyIdListValues.put("222", new ExpectedAgencyIdListValue("DK, Danish National Board of Health", "The national authority responsible for the supervision of health activities in Denmark."));
        expectedAgencyIdListValues.put("223", new ExpectedAgencyIdListValue("DK, Danish Ministry of Home Affairs", "The ministry responsible for all interior affairs concerning the Danish people."));
        expectedAgencyIdListValues.put("224", new ExpectedAgencyIdListValue("US, Aluminum Association", "Organization that assigns identification numbers for the aluminum industry."));
        expectedAgencyIdListValues.put("225", new ExpectedAgencyIdListValue("US, CIDX (Chemical Industry Data Exchange)", "Organization that assigns identification numbers for the chemical Industry."));
        expectedAgencyIdListValues.put("226", new ExpectedAgencyIdListValue("US, Carbide Manufacturers", "Organization that assigns identification numbers for the iron and carbide manufacturing industry."));
        expectedAgencyIdListValues.put("227", new ExpectedAgencyIdListValue("US, NWDA (National Wholesale Druggist Association)", "Organization that assigns identification numbers for the wholesale drug industry."));
        expectedAgencyIdListValues.put("228", new ExpectedAgencyIdListValue("US, EIA (Electronic Industry Association)", "Organization that assigns identification numbers for the electronic industry."));
        expectedAgencyIdListValues.put("229", new ExpectedAgencyIdListValue("US, American Paper Institute", "Organization that assigns identification numbers for the American paper industry."));
        expectedAgencyIdListValues.put("230", new ExpectedAgencyIdListValue("US, VICS (Voluntary Inter-Industry Commerce Standards)", "Organization that assigns identification numbers for the retail industry."));
        expectedAgencyIdListValues.put("231", new ExpectedAgencyIdListValue("Copper and Brass Fabricators Council", "Organization that assigns identification numbers for the copper and brass fabricators industry."));
        expectedAgencyIdListValues.put("232", new ExpectedAgencyIdListValue("GB, Inland Revenue", "Code identifying the government department responsible for assessing and collecting revenue consisting of taxes and inland duties in Great Britain."));
        expectedAgencyIdListValues.put("233", new ExpectedAgencyIdListValue("US, OMB (Office of Management and Budget)", "Codes are assigned by the United States Office of Management and Budget."));
        expectedAgencyIdListValues.put("234", new ExpectedAgencyIdListValue("DE, Siemens AG", "Siemens AG, Germany."));
        expectedAgencyIdListValues.put("235", new ExpectedAgencyIdListValue("AU, Tradegate (Electronic Commerce Australia)", "Australian industry body coordinating codes for use in local and international commerce and trade."));
        expectedAgencyIdListValues.put("236", new ExpectedAgencyIdListValue("US, United States Postal Service (USPS)", "Code specifying the official postal service of the United States."));
        expectedAgencyIdListValues.put("237", new ExpectedAgencyIdListValue("US, United States health industry", "Code assigned by the United States health industry."));
        expectedAgencyIdListValues.put("238", new ExpectedAgencyIdListValue("US, TDCC (Transportation Data Coordinating Committee)", "United States Transportation Data Coordinating Committee."));
        expectedAgencyIdListValues.put("239", new ExpectedAgencyIdListValue("US, HL7 (Health Level 7)", "United States, electronic data interchange standards-making organization, Health Level 7."));
        expectedAgencyIdListValues.put("240", new ExpectedAgencyIdListValue("US, CHIPS (Clearing House Interbank Payment Systems)", "United States financial clearing house."));
        expectedAgencyIdListValues.put("241", new ExpectedAgencyIdListValue("PT, SIBS (Sociedade Interbancaria de Servicos)", "Portuguese automated clearing house."));
        expectedAgencyIdListValues.put("244", new ExpectedAgencyIdListValue("US, Department of Health and Human Services", "United States Department of Health and Human Services."));
        expectedAgencyIdListValues.put("245", new ExpectedAgencyIdListValue("GS1 Denmark", "Organisation responsible for the GS1 System in Denmark."));
        expectedAgencyIdListValues.put("246", new ExpectedAgencyIdListValue("GS1 Germany", "Organisation responsible for the GS1 System in Germany."));
        expectedAgencyIdListValues.put("247", new ExpectedAgencyIdListValue("US, HBICC (Health Industry Business Communication Council)", "Code identifying the United States HIBCC (Health Industry Business Communication Council)."));
        expectedAgencyIdListValues.put("248", new ExpectedAgencyIdListValue("US, ASTM (American Society of Testing and Materials)", "A not-for-profit organization that provides a forum for producers, users, ultimate consumers, and those having a general interest (representatives of government and academia) to meet on common ground and write standards for materials, products, systems, and services."));
        expectedAgencyIdListValues.put("249", new ExpectedAgencyIdListValue("IP (Institute of Petroleum)", "An independent European centre for the advancement and dissemination of technical, economic and professional knowledge relating to the international oil and gas industry."));
        expectedAgencyIdListValues.put("250", new ExpectedAgencyIdListValue("US, UOP (Universal Oil Products)", "An United States based organization that provides products, services and technology primarily in the areas of petroleum refining, olefins, aromatics, and gas processing."));
        expectedAgencyIdListValues.put("251", new ExpectedAgencyIdListValue("AU, HIC (Health Insurance Commission)", "Australian agency responsible for administering the Health Insurance Act."));
        expectedAgencyIdListValues.put("252", new ExpectedAgencyIdListValue("AU, AIHW (Australian Institute of Health and Welfare)", "Australian statutory authority responsible for the national collection of health related statistics and health related data definitions."));
        expectedAgencyIdListValues.put("253", new ExpectedAgencyIdListValue("AU, NCCH (National Centre for Classification in Health)", "Australian national authority responsible for healthcare classifications."));
        expectedAgencyIdListValues.put("254", new ExpectedAgencyIdListValue("AU, DOH (Australian Department of Health)", "Australian government department responsible for administration of health policy."));
        expectedAgencyIdListValues.put("255", new ExpectedAgencyIdListValue("AU, ADA (Australian Dental Association)", "Industry association responsible for the classification of dental services in Australia."));
        expectedAgencyIdListValues.put("256", new ExpectedAgencyIdListValue("US, AAR (Association of American Railroads)", "The official United States organization of the railroads in North America."));
        expectedAgencyIdListValues.put("257", new ExpectedAgencyIdListValue("ECCMA (Electronic Commerce Code Management Association)", "The Electronic Commerce Code Management Association, a not for profit membership organization, which manages codes used in electronic commerce."));
        expectedAgencyIdListValues.put("258", new ExpectedAgencyIdListValue("JP, Japanese Ministry of Transport", "Japanese Ministry of Transport."));
        expectedAgencyIdListValues.put("259", new ExpectedAgencyIdListValue("JP, Japanese Maritime Safety Agency", "Japanese Maritime Safety Agency."));
        expectedAgencyIdListValues.put("260", new ExpectedAgencyIdListValue("ebIX (European forum for energy Business Information eXchange)", "A code to identify European forum for energy Business Information eXchange, which is an organization standardizing the use of EDI between the participants in the European energy market."));
        expectedAgencyIdListValues.put("261", new ExpectedAgencyIdListValue("EEG7, European Expert Group 7 (Insurance)", "European Expert Group 7 for Insurance."));
        expectedAgencyIdListValues.put("262", new ExpectedAgencyIdListValue("DE, GDV (Gesamtverband der Deutschen Versicherungswirtschaft e.V.)", "Gesamtverband der Deutschen Versicherungswirtschaft e.V. (German Insurance Association)."));
        expectedAgencyIdListValues.put("263", new ExpectedAgencyIdListValue("CA, CSIO (Centre for Study of Insurance Operations)", "The Centre for Study of Insurance Operations (CSIO) in Canada."));
        expectedAgencyIdListValues.put("264", new ExpectedAgencyIdListValue("FR, AGF (Assurances Generales de France)", "Code lists are administered by Assurances Generales de France (AGF)."));
        expectedAgencyIdListValues.put("265", new ExpectedAgencyIdListValue("SE, Central bank", "Swedish central bank."));
        expectedAgencyIdListValues.put("266", new ExpectedAgencyIdListValue("US, DoA (Department of Agriculture)", "Department of Agriculture, United States federal agency."));
        expectedAgencyIdListValues.put("267", new ExpectedAgencyIdListValue("RU, Central Bank of Russia", "Central bank of Russia."));
        expectedAgencyIdListValues.put("268", new ExpectedAgencyIdListValue("FR, DGI (Direction Generale des Impots)", "French taxation authority."));
        expectedAgencyIdListValues.put("269", new ExpectedAgencyIdListValue("GRE (Reference Group of Experts)", "An international association that administers code lists on behalf of business credit information users and providers."));
        expectedAgencyIdListValues.put("270", new ExpectedAgencyIdListValue("Concord EDI group", "An organisation of international transport equipment leasing companies and transport equipment repair providers responsible for promoting the use of EDI standards and standard business terms."));
        expectedAgencyIdListValues.put("271", new ExpectedAgencyIdListValue("InterContainer InterFrigo", "European railway associated organisation involved in the transport of containers by rail."));
        expectedAgencyIdListValues.put("272", new ExpectedAgencyIdListValue("Joint Automotive Industry agency", "The Joint Automotive Industry (JAI) agency is in charge of code lists that are common to automotive industry groups."));
        expectedAgencyIdListValues.put("273", new ExpectedAgencyIdListValue("CH, SCC (Swiss Chambers of Commerce)", "Swiss Chambers of Commerce."));
        expectedAgencyIdListValues.put("274", new ExpectedAgencyIdListValue("ITIGG (International Transport Implementation Guidelines Group)", "ITIGG is the UN/EDIFACT transport message development group's organisation responsible for the issuance of globally harmonised transport-related codes."));
        expectedAgencyIdListValues.put("275", new ExpectedAgencyIdListValue("ES, Banco de España", "The Spanish central bank."));
        expectedAgencyIdListValues.put("276", new ExpectedAgencyIdListValue("Assigned by Port Community", "Codes assigned by the Port Community."));
        expectedAgencyIdListValues.put("277", new ExpectedAgencyIdListValue("BIGNet (Business Information Group Network)", "Identifies the Business Information Group Network, an international trade alliance that administers code lists on behalf of business information users and providers."));
        expectedAgencyIdListValues.put("278", new ExpectedAgencyIdListValue("Eurogate", "An international trade alliance that administers code lists on behalf of business information users and providers."));
        expectedAgencyIdListValues.put("279", new ExpectedAgencyIdListValue("NL, Graydon", "Identifies the Graydon Corporation in the Netherlands."));
        expectedAgencyIdListValues.put("280", new ExpectedAgencyIdListValue("FR, Euler", "A company in France responsible for assigning codes in the credit insurance industry."));
        expectedAgencyIdListValues.put("281", new ExpectedAgencyIdListValue("GS1 Belgium and Luxembourg", "Organisation responsible for the GS1 System in Belgium and Luxembourg."));
        expectedAgencyIdListValues.put("282", new ExpectedAgencyIdListValue("DE, Creditreform International e.V.", "Identifies the Creditreform International e.V. in Germany (e.V.: eingetragener Verein)."));
        expectedAgencyIdListValues.put("283", new ExpectedAgencyIdListValue("DE, Hermes Kreditversicherungs AG", "Identifies the Hermes Kreditversicherungs AG in Germany (AG: Aktiengesellschaft)."));
        expectedAgencyIdListValues.put("284", new ExpectedAgencyIdListValue("TW, Taiwanese Bankers' Association", "Code identifying the organization responsible for the issuance of bank related codes in Taiwan."));
        expectedAgencyIdListValues.put("285", new ExpectedAgencyIdListValue("ES, Asociación Española de Banca", "Code identifying the organization responsible for the issuance of bank related codes in Spain."));
        expectedAgencyIdListValues.put("286", new ExpectedAgencyIdListValue("SE, TCO (Tjänstemännes Central Organisation)", "The Swedish Confederation of Professional Employees."));
        expectedAgencyIdListValues.put("287", new ExpectedAgencyIdListValue("DE, FORTRAS (Forschungs- und Entwicklungsgesellschaft für Transportwesen GMBH)", "German research and development institute for transport matters."));
        expectedAgencyIdListValues.put("288", new ExpectedAgencyIdListValue("OSJD (Organizacija Sotrudnichestva Zeleznih Dorog)", "Code identifying OSJD, Organisation for Co-operation of Railways."));
        expectedAgencyIdListValues.put("289", new ExpectedAgencyIdListValue("JP,JIPDEC/ECPC  (Japan Information Processing Development Center / Electronic Commerce Promotion Center)", "JIPDEC/ECPC is a nonprofit foundation for promoting electronic commerce, and is the registration agency of the standard company code in Japan."));
        expectedAgencyIdListValues.put("290", new ExpectedAgencyIdListValue("JP, JAMA", "Japan Automobile Manufacturers Association, Inc."));
        expectedAgencyIdListValues.put("291", new ExpectedAgencyIdListValue("JP, JAPIA", "Japan Auto Parts Industries Association."));
        expectedAgencyIdListValues.put("292", new ExpectedAgencyIdListValue("FI, TIEKE The Information Technology Development Centre of Finland", "The national organization representing Finland in electronic data interchange for trade and industry."));
        expectedAgencyIdListValues.put("293", new ExpectedAgencyIdListValue("DE, BDEW (Bundesverband der Energie- und Wasserwirtschaft e.V.)", "The Federal Association of Energy and Water (BDEW) is the trade association of the German industries for natural gas, electricity supply, long-distance heating and water supply."));
        expectedAgencyIdListValues.put("294", new ExpectedAgencyIdListValue("GS1 Austria", "Organisation responsible for the GS1 System in Austria."));
        expectedAgencyIdListValues.put("295", new ExpectedAgencyIdListValue("AU, Australian Therapeutic Goods Administration", "Austrialian administration responsible for the regulation of therapeutic goods in Australia."));
        expectedAgencyIdListValues.put("296", new ExpectedAgencyIdListValue("ITU (International Telecommunication Union)", "International Telecommunication Union."));
        expectedAgencyIdListValues.put("297", new ExpectedAgencyIdListValue("IT, Ufficio IVA", "Ufficio responsabile gestione partite IVA is the Italian Institute issuing VAT registration numbers."));
        expectedAgencyIdListValues.put("298", new ExpectedAgencyIdListValue("GS1 Spain", "Organisation responsible for the GS1 System in Spain."));
        expectedAgencyIdListValues.put("299", new ExpectedAgencyIdListValue("BE, Seagha", "Organisation responsible for assigning maritime related identification numbers in Belgian ports."));
        expectedAgencyIdListValues.put("300", new ExpectedAgencyIdListValue("SE, Swedish International Freight Association", "Swedish International Freight Association (SIFA)."));
        expectedAgencyIdListValues.put("301", new ExpectedAgencyIdListValue("DE, BauDatenbank GmbH", "The organisation responsible for issuing and maintaining commodity codes for use in the German construction industry."));
        expectedAgencyIdListValues.put("302", new ExpectedAgencyIdListValue("DE, Bundesverband des Deutschen Textileinzelhandels e.V.", "The organisation responsible for issuing and maintaining commodity codes for use in the German textile industry."));
        expectedAgencyIdListValues.put("303", new ExpectedAgencyIdListValue("GB, Trade Service Information Ltd (TSI)", "A United Kingdom authority responsible for the allocation of identification codes to products in the building sector."));
        expectedAgencyIdListValues.put("304", new ExpectedAgencyIdListValue("DE, Bundesverband Deutscher Heimwerker-, Bau- und Gartenfachmaerkte e.V.", "The organisation responsible for issuing and maintaining commodity codes for use in German stores for do-it-yourself, construction and garden articles."));
        expectedAgencyIdListValues.put("305", new ExpectedAgencyIdListValue("ETSO (European Transmission System Operator)", "The European organisation representing the electrical industry transmission system operators."));
        expectedAgencyIdListValues.put("306", new ExpectedAgencyIdListValue("SMDG (Ship-planning Message Design Group)", "User Group for Shipping Lines and Container Terminals."));
        expectedAgencyIdListValues.put("307", new ExpectedAgencyIdListValue("JP, Ministry of Justice", "Japanese Ministry of Justice."));
        expectedAgencyIdListValues.put("309", new ExpectedAgencyIdListValue("JP, JASTPRO (Japan Association for Simplification of International Trade Procedures)", "JASTPRO is a nonprofit organization for simplifying international trade procedures and is the responsible agency for the registration of the \"Japan Exporters and Importers Standard Code\"."));
        expectedAgencyIdListValues.put("310", new ExpectedAgencyIdListValue("DE, SAP AG (Systeme, Anwendungen und Produkte)", "Systeme, Anwendungen und Produkte (German software company)."));
        expectedAgencyIdListValues.put("311", new ExpectedAgencyIdListValue("JP, TDB (Teikoku Databank, Ltd.)", "TDB (Teikoku Databank, Ltd.) conducts corporate credit research and maintains company codes based on research activities for supporting business transactions in Japan."));
        expectedAgencyIdListValues.put("312", new ExpectedAgencyIdListValue("FR, AGRO EDI EUROPE", "French association developing EDI in the agricultural and food processing sectors."));
        expectedAgencyIdListValues.put("313", new ExpectedAgencyIdListValue("FR, Groupement National Interprofessionnel des Semences et Plants", "French organization of seed and plant professionals responsible for assigning codes in the botanical seed sectors."));
        expectedAgencyIdListValues.put("314", new ExpectedAgencyIdListValue("OAGi (Open Applications Group, Incorporated)", "Open Applications Group, Incorporated."));
        expectedAgencyIdListValues.put("315", new ExpectedAgencyIdListValue("US, STAR (Standards for Technology in Automotive Retail)", "Standards for Technology in Automotive Retail."));
        expectedAgencyIdListValues.put("316", new ExpectedAgencyIdListValue("GS1 Finland", "Organisation responsible for the GS1 system in Finland."));
        expectedAgencyIdListValues.put("317", new ExpectedAgencyIdListValue("GS1 Brazil", "Organisation responsible for the GS1 system in Brazil."));
        expectedAgencyIdListValues.put("318", new ExpectedAgencyIdListValue("IETF (Internet Engineering Task Force)", "IETF - Internet Engineering Task Force."));
        expectedAgencyIdListValues.put("319", new ExpectedAgencyIdListValue("FR, GTF", "Group of Terrestrial Freight Forwarders."));
        expectedAgencyIdListValues.put("320", new ExpectedAgencyIdListValue("DK, Danish National IT and Telcom Agency (ITA)", "Code specifying the Danish National IT and Telcom Agency as codelist responsible Agency."));
        expectedAgencyIdListValues.put("321", new ExpectedAgencyIdListValue("EASEE-Gas (European Association for the Streamlining of Energy Exchange for gas)", "The European organization whose working group Edig@s is responsible for the management of gas related codes and messages."));
        expectedAgencyIdListValues.put("322", new ExpectedAgencyIdListValue("IS, ICEPRO", "Icelandic committee of e-commerce and trade procedures."));
        expectedAgencyIdListValues.put("323", new ExpectedAgencyIdListValue("PROTECT", "A group of European ports, national competent authorities and port community systems exchanging information about dangerous goods and vessel movements."));
        expectedAgencyIdListValues.put("324", new ExpectedAgencyIdListValue("GS1 Ireland", "Organisation responsible for the GS1 system in Ireland."));
        expectedAgencyIdListValues.put("325", new ExpectedAgencyIdListValue("GS1 Russia", "Organisation responsible for the GS1 system in Russia."));
        expectedAgencyIdListValues.put("326", new ExpectedAgencyIdListValue("GS1 Poland", "Organisation responsible for the GS1 system in Poland."));
        expectedAgencyIdListValues.put("327", new ExpectedAgencyIdListValue("GS1 Estonia", "Organisation responsible for the GS1 system in Estonia."));
        expectedAgencyIdListValues.put("328", new ExpectedAgencyIdListValue("Assigned by ultimate recipient of the message", "A code assigned by the party who is the ultimate recipient of the message."));
        expectedAgencyIdListValues.put("329", new ExpectedAgencyIdListValue("Assigned by loading dock operator", "The code is assigned by the operator of a loading dock."));
        expectedAgencyIdListValues.put("330", new ExpectedAgencyIdListValue("EXIS (Exis Technologies Ltd.)", "Exis Technologies Ltd. is the system developer of the electronic International Maritime Dangerous Goods Code (IMDG Code) for the International Maritime Organization."));
        expectedAgencyIdListValues.put("331", new ExpectedAgencyIdListValue("US, Agricultural Marketing Service (AMS)", "A division of the US Department of Agriculture. AMS inspects and grades agricultural products and gathers import and export statistics for farm products."));
        expectedAgencyIdListValues.put("332", new ExpectedAgencyIdListValue("DE, DVGW Service & Consult GmbH", "Subsidiary of DVGW German Technical and Scientific Association for Gas and Water. As a technical standardization organization, the DVGW promotes technical standards for the production, transportation, distribution and use of gas and drinking water in the Federal Republic of Germany."));
        expectedAgencyIdListValues.put("333", new ExpectedAgencyIdListValue("US, Animal and Plant Health Inspection Service (APHIS)", "A division of the US Department of Agriculture. APHIS regulates the import and export of plants, animals and their products."));
        expectedAgencyIdListValues.put("334", new ExpectedAgencyIdListValue("US, Bureau of Labor Statistics (BLS)", "A division of the US Department of Labor. BLS is the principal fact-finding agency for the Federal Government in the broad field of labor economics and statistics."));
        expectedAgencyIdListValues.put("335", new ExpectedAgencyIdListValue("US, Bureau of Transportation Statistics (BTS)", "A division of the US Department of Transportation. BTS administers data collection, analysis, and reporting and to ensure the most cost-effective use of transportation-monitoring resources."));
        expectedAgencyIdListValues.put("336", new ExpectedAgencyIdListValue("US, Customs and Border Protection (CBP)", "A division of US Department of Homeland Security."));
        expectedAgencyIdListValues.put("337", new ExpectedAgencyIdListValue("US, Center for Disease Control (CDC)", "A division of the US Department of Health and Human Services. CDC promotes health and quality of life by preventing and controlling disease, injury, and disability."));
        expectedAgencyIdListValues.put("338", new ExpectedAgencyIdListValue("US, Consumer Product Safety Commission (CPSC)", "An independent commission within the US federal government. CPSC works to protect consumers from products that pose a fire, electrical, chemical, or mechanical hazard or can injure children."));
        expectedAgencyIdListValues.put("339", new ExpectedAgencyIdListValue("US, Directorate of Defense Trade Controls (DDTC)", "A division of the US Department of State. DDTC is charged with controlling the export and temporary import of defense articles and defense services covered by the United States."));
        expectedAgencyIdListValues.put("340", new ExpectedAgencyIdListValue("US, Environmental Protection Agency (EPA)", "An independent agency within the US federal government. EPA registers and controls imports of pesticides, Ozone Depleting Substances, and bulk chemicals; controls conformity of vehicles and engines to the Clean Air Act, and tracks hazardous wastes."));
        expectedAgencyIdListValues.put("341", new ExpectedAgencyIdListValue("US, Federal Aviation Administration (FAA)", "A division of the US Department of Transportation. FAA is responsible for the safety of hazardous materials shipments when flown into and from US air space."));
        expectedAgencyIdListValues.put("342", new ExpectedAgencyIdListValue("US, Foreign Agriculture Service (FAS)", "A division of the US Department of Agriculture. FAS works to improve foreign market access for US products, build new markets, improve the competitive position of US agriculture in the global marketplace, and provide food aid and technical assistance to foreign countries."));
        expectedAgencyIdListValues.put("343", new ExpectedAgencyIdListValue("US, Federal Motor Carrier Safety Administration (FMCSA)", "A division of the US Department of Transportation. FMCSA is focused on reducing crashes, injuries, and fatalities involving large trucks and buses."));
        expectedAgencyIdListValues.put("344", new ExpectedAgencyIdListValue("US, Food Safety Inspection Service (FSIS)", "A division of the US Department of Agriculture. FSIS is responsible for ensuring that the nation's commercial supply of meat, poultry, and egg products is safe, wholesome, and correctly labeled and packaged."));
        expectedAgencyIdListValues.put("345", new ExpectedAgencyIdListValue("US, Foreign Trade Zones Board (FTZB)", "FTZB licenses, regulates, and monitors the activity of foreign-trade zones (FTZ) in the United States."));
        expectedAgencyIdListValues.put("346", new ExpectedAgencyIdListValue("US, The Grain Inspection, Packers and Stockyards Administration (GIPSA)", "A division of the US Department of Agriculture. GIPSA facilitates the marketing of livestock, poultry, meat, cereals, oilseeds, and related agricultural products, and promotes fair and competitive trading practices for the overall benefit of consumers and American agriculture."));
        expectedAgencyIdListValues.put("347", new ExpectedAgencyIdListValue("US, Import Administration (IA)", "A division of the US Department of Commerce. IA enforces the US unfair trade laws (i.e., the anti-dumping and countervailing duty laws) and develops and implements other policies and programs aimed at countering foreign unfair trade practices."));
        expectedAgencyIdListValues.put("348", new ExpectedAgencyIdListValue("US, Internal Revenue Service (IRS)", "A division of the US Department of the Treasury. IRS collects excise taxes on imports and on conveyances involved in international traffic."));
        expectedAgencyIdListValues.put("349", new ExpectedAgencyIdListValue("US, International Trade Commission (ITC)", "An independent commission. ITC investigates the effects of dumped and subsidized imports on domestic industries and conducts global safeguard investigations."));
        expectedAgencyIdListValues.put("350", new ExpectedAgencyIdListValue("US, National Highway Traffic Safety Administration (NHTSA)", "A division of the US Department of Transportation. NHTSA monitors the importation of motor vehicles and motor vehicle equipment to ensure compliance with applicable Federal motor vehicle safety standards."));
        expectedAgencyIdListValues.put("351", new ExpectedAgencyIdListValue("US, National Marine Fisheries Service (NMFS)", "A division of the US Department of Commerce. NMFS issues permits to support their effort to regulate commercial and recreational international transactions involving resources such as such as swordfish, tooth fish, and tuna while overseeing the management, conservation and protection of living marine resources within the areas of ocean surrounding the United States."));
        expectedAgencyIdListValues.put("352", new ExpectedAgencyIdListValue("US, Office of Fossil Energy (OFE)", "A division of the US Department of Energy."));
        expectedAgencyIdListValues.put("353", new ExpectedAgencyIdListValue("US, Office of Foreign Missions (OFM)", "A division of the US Department of State. OFM provides the legal foundation to facilitate secure and efficient operations of US missions abroad, and of foreign missions and international organizations in the United States."));
        expectedAgencyIdListValues.put("354", new ExpectedAgencyIdListValue("US, Bureau of Oceans and International Environmental and Scientific Affairs (OES)", "A division of the US Department of State. OES works with issues surrounding foreign policy formulation and implementation in global environment, science, and technology."));
        expectedAgencyIdListValues.put("355", new ExpectedAgencyIdListValue("US, Office of Naval Intelligence (ONI)", "A division of the US Department of Defense (Navy)."));
        expectedAgencyIdListValues.put("356", new ExpectedAgencyIdListValue("US, Pipeline and Hazardous Materials Safety Administration (PHMSA)", "A division of the US Department of Transportation. PHMSA works to protect the American public and the environment by ensuring the safe and secure movement of hazardous materials to industry and consumers by all transportation modes, including the nation's pipelines."));
        expectedAgencyIdListValues.put("357", new ExpectedAgencyIdListValue("US, Alcohol and Tobacco Tax and Trade Bureau (TTB)", "A division of the US Department of the Treasury. TTB collects alcohol, tobacco, firearms, and ammunition excise taxes; to ensure that these products are labeled, advertised, and marketed in accordance with the law; and administers the laws and regulations."));
        expectedAgencyIdListValues.put("358", new ExpectedAgencyIdListValue("US, Army Corp of Engineers (USACE)", "A division of the US Department of Defense (Army). USACE protects the US aquatic resources, while allowing reasonable development through fair, flexible and balanced permit decisions."));
        expectedAgencyIdListValues.put("359", new ExpectedAgencyIdListValue("US, Agency for International Development (USAID)", "USAID is an independent US federal government agency. USAID supports long-term and equitable economic growth and advances US foreign policy objectives."));
        expectedAgencyIdListValues.put("360", new ExpectedAgencyIdListValue("US, Coast Guard (USCG)", "A division of US Department of Homeland Security. USCG is a military branch of the United States involved in maritime law, mariner assistance, and search and rescue."));
        expectedAgencyIdListValues.put("361", new ExpectedAgencyIdListValue("US, Office of the United States Trade Representative (USTR)", "An office within the US Executive Branch, Office of the President. USTR negotiates directly with foreign governments to create trade agreements; resolves disputes and participates in global trade policy organizations; meets with governments, business groups, legislators and public interest groups to gather input on trade issues; and explains the president’s trade policy positions."));
        expectedAgencyIdListValues.put("362", new ExpectedAgencyIdListValue("International Commission for the Conservation of Atlantic Tunas (ICCAT)", "An inter-governmental fishery organization responsible for the conservation of tunas and tuna-like species in the Atlantic Ocean and its adjacent seas."));
        expectedAgencyIdListValues.put("363", new ExpectedAgencyIdListValue("Inter-American Tropical Tuna Commission (IATTC)", "An organization responsible for the conservation and management of fisheries for tunas and other species taken by tuna-fishing vessels in the eastern Pacific Ocean."));
        expectedAgencyIdListValues.put("364", new ExpectedAgencyIdListValue("Commission for the Conservation of Southern Bluefin Tuna (CCSBT)", "An organization which oversees the management and conservation of southern blue fin tuna."));
        expectedAgencyIdListValues.put("365", new ExpectedAgencyIdListValue("Indian Ocean Tuna Commission (IOTC)", "An intergovernmental organization mandated to manage tuna and tuna-like species in the Indian Ocean and adjacent seas."));
        expectedAgencyIdListValues.put("366", new ExpectedAgencyIdListValue("International Botanical Congress", "International organization responsible for the international identification of botanical species."));
        expectedAgencyIdListValues.put("367", new ExpectedAgencyIdListValue("International Commission on Zoological Nomenclature", "International organization responsible for the International Code of Zoological Nomenclature (ICZN)."));
        expectedAgencyIdListValues.put("368", new ExpectedAgencyIdListValue("International Society for Horticulture Science", "International organization responsible for the International Code of Nomenclature for Cultivated Plants (ICNCP)."));
        expectedAgencyIdListValues.put("369", new ExpectedAgencyIdListValue("Chemical Abstract Service (CAS)", "Chemical Abstract Service (CAS) is a division of the American Chemical Society (ACS) responsible for the CAS registry."));
        expectedAgencyIdListValues.put("370", new ExpectedAgencyIdListValue("Social Security Administration (SSA)", "The Social Security Administration (SSA) is an independent agency of the US government. SSA issues Social Security Numbers (SSN) used to identify parties/entities in a trade transaction."));
        expectedAgencyIdListValues.put("371", new ExpectedAgencyIdListValue("INMARSAT", "Satellite communications organization that provides vessel call sign used by ships as an identifier."));
        expectedAgencyIdListValues.put("372", new ExpectedAgencyIdListValue("Agent of ship at the intended port of arrival", "Name and contact details of the agent of the ship at the intended port of arrival."));
        expectedAgencyIdListValues.put("373", new ExpectedAgencyIdListValue("US Air Force", "The aerial warfare division of the U.S. Department of Defense."));
        expectedAgencyIdListValues.put("374", new ExpectedAgencyIdListValue("US, Bureau of Explosives", "A self-policing agency to promote the safe transportation of explosives and other hazardous materials on roads and railways."));
        expectedAgencyIdListValues.put("375", new ExpectedAgencyIdListValue("Basel Convention Secretariat", "Agency which maintains the list and specifications of goods for control of transboundary movements of hazardous wastes and their disposal."));
        expectedAgencyIdListValues.put("376", new ExpectedAgencyIdListValue("PANTONE", "Color code controlling organisation."));
        expectedAgencyIdListValues.put("377", new ExpectedAgencyIdListValue("IS, National Registry of Iceland", "The national registry of Iceland, responsible for legal registration ID for persons."));
        expectedAgencyIdListValues.put("378", new ExpectedAgencyIdListValue("IS, Internal Revenue Directorate of Iceland", "Tax authorities of Iceland, responsible for VAT codes and registration ID for legal entities."));
        expectedAgencyIdListValues.put("379", new ExpectedAgencyIdListValue("IANA (Internet Assigned Numbers Authority)", "The Internet Assigned Numbers Authority (IANA) is responsible for the global coordination of the DNS Root, IP addressing, and other Internet protocol resources."));
        expectedAgencyIdListValues.put("380", new ExpectedAgencyIdListValue("Korea Customs Service", "The customs services department of the Government of the Republic of Korea."));
        expectedAgencyIdListValues.put("381", new ExpectedAgencyIdListValue("Israel Tax Authority", "The customs and taxation services department of the State of Israel."));
        expectedAgencyIdListValues.put("382", new ExpectedAgencyIdListValue("Israeli Ministry of Interior", "The Ministry of the State of Israel that is responsible for interior affairs including the issue of passports / travel documents."));
        expectedAgencyIdListValues.put("383", new ExpectedAgencyIdListValue("FR, LUMD (Logistique Urbaine Mutualisée Durable)", "Logistique Urbaine Mutualisée Durable, French organisation in charge of Urban Logistics."));
        expectedAgencyIdListValues.put("384", new ExpectedAgencyIdListValue("DE, BiPRO (Brancheninitiative Prozessoptimierung)", "BiPRO is the German association of Insurance Companies, Brokers and Software Suppliers responsible for the development of standards for use by the insurance industry."));
        expectedAgencyIdListValues.put("385", new ExpectedAgencyIdListValue("JO, Jordan Ministry of Agriculture", "The Authority for agriculture in the Hashemite Kingdom of Jordan."));
        expectedAgencyIdListValues.put("386", new ExpectedAgencyIdListValue("JO, Jordan Customs", "The Customs Authority of the Hashemite Kingdom of Jordan."));
        expectedAgencyIdListValues.put("387", new ExpectedAgencyIdListValue("JO, Jordan Food & Drug Administration", "The Food & Drug Administration of the Hashemite Kingdom of Jordan."));
        expectedAgencyIdListValues.put("388", new ExpectedAgencyIdListValue("JO, Jordan Institution for Standards and Metrology", "The Institution for Standards and Metrology of the Hashemite Kingdom of Jordan."));
        expectedAgencyIdListValues.put("389", new ExpectedAgencyIdListValue("JO, Jordan Telecommunication Regulatory Commission", "The national authority of the Hashemite Kingdom of Jordan concerned with the regulation of telecommunications."));
        expectedAgencyIdListValues.put("390", new ExpectedAgencyIdListValue("JO, Jordan Nuclear Regulatory Commission", "The Nuclear Regulatory Commission of the Hashemite Kingdom of Jordan."));
        expectedAgencyIdListValues.put("391", new ExpectedAgencyIdListValue("JO, Jordan Ministry of Environment", "The government department of the Hashemite Kingdom of Jordan responsible for the protection of the environment."));
        expectedAgencyIdListValues.put("392", new ExpectedAgencyIdListValue("Hazardous waste collector", "Party collecting hazardous waste."));
        expectedAgencyIdListValues.put("393", new ExpectedAgencyIdListValue("Hazardous waste generator", "Party generating hazardous waste."));
        expectedAgencyIdListValues.put("394", new ExpectedAgencyIdListValue("Marketing agent", "Party authorized to market a product."));
        expectedAgencyIdListValues.put("395", new ExpectedAgencyIdListValue("BE, TELEBIB Centre", "The TELEBIB Centre is the Belgian association of Insurance Companies, Brokers and Software Suppliers responsible for the development of standards for use by the insurance industry."));
        expectedAgencyIdListValues.put("396", new ExpectedAgencyIdListValue("BE, BNB", "The Belgian National Bank, which delivers identifiers for, amongst others, Insurers and Re-insurers."));
        expectedAgencyIdListValues.put("397", new ExpectedAgencyIdListValue("BE, FSMA", "The Belgian Financial Services Market Authority, which delivers identifiers for, amongst others, Intermediaries in insurance business."));
        expectedAgencyIdListValues.put("398", new ExpectedAgencyIdListValue("FR, PHAST", "French association responsible for standardization, coordination and promotion of EDI and Data Exchange applications in Health and Hospital sector."));
        expectedAgencyIdListValues.put("ZZZ", new ExpectedAgencyIdListValue("Mutually defined", "A code assigned within a code list to be used on an interim basis and as defined among trading partners until a precise code can be assigned to the code list."));

        agencyIdListValueRepository.findAll()
                .forEach(agencyIdListValue -> {
                    assertTrue(expectedAgencyIdListValues.containsKey(agencyIdListValue.getValue()));

                    ExpectedAgencyIdListValue expectedAgencyIdListValue = expectedAgencyIdListValues.get(agencyIdListValue.getValue());
                    assertEquals(expectedAgencyIdListValue.getName(), agencyIdListValue.getName());
                    assertEquals(expectedAgencyIdListValue.getDescription(), agencyIdListValue.getDefinition());
        });
    }
}
