package org.oagi.score.e2e.TS_37_ReleaseBranchAgencyIDListManagementEndUser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.oagi.score.e2e.BaseTest;
import org.oagi.score.e2e.obj.*;
import org.oagi.score.e2e.page.HomePage;
import org.oagi.score.e2e.page.agency_id_list.EditAgencyIDListPage;
import org.oagi.score.e2e.page.agency_id_list.ViewEditAgencyIDListPage;
import org.oagi.score.e2e.page.bie.EditBIEPage;
import org.oagi.score.e2e.page.bie.ExpressBIEPage;
import org.openqa.selenium.WebElement;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.SAME_THREAD)
public class TC_37_8_UsingCustomEndUserAgencyIDListInBIEExpression extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_37_8_TA_1_TA_2_TA_3_Issue_1805_All_Expression_Formats")
    public void custom_agency_id_lists_on_bbie_sc_generate_all_expression_formats() throws Exception {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        List<AgencyIDListObject> agencyIDLists = new ArrayList<>();
        List<AgencyIDListValueObject> customValues = new ArrayList<>();

        AgencyIDListObject unbasedList = getAPIFactory().getAgencyIDListAPI()
                .createRandomAgencyIDList(endUser, namespace, release, "WIP");
        AgencyIDListValueObject unbasedValue = getAPIFactory().getAgencyIDListValueAPI()
                .createRandomAgencyIDListValue(endUser, unbasedList);
        agencyIDLists.add(unbasedList);
        customValues.add(unbasedValue);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditAgencyIDListPage viewEditAgencyIDListPage = homePage.getCoreComponentMenu()
                .openViewEditAgencyIDListSubMenu();
        EditAgencyIDListPage editAgencyIDListPage = viewEditAgencyIDListPage
                .openEditAgencyIDListPageByNameAndBranch(unbasedList.getName(), release.getReleaseNumber());
        // Agency ID List Value is optional. Leave the list-level selected value unset
        // to exercise issue #1805 while using the custom list's values in a BIE.
        editAgencyIDListPage.setRemark("Updated while list-level Agency ID List Value is unset.");
        editAgencyIDListPage.hitUpdateButton();
        editAgencyIDListPage.moveToQA();
        editAgencyIDListPage.moveToProduction();

        ASCCPObject bomASCCP = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        assertNotNull(bomASCCP, "The seeded BOM. BOM ASCCP is required for the BBIE_SC fixture.");

        for (int i = 0; i < agencyIDLists.size(); i++) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(context), bomASCCP, endUser, "WIP");

            EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
            editBIEPage.openPage();
            WebElement bbieSCNode = editBIEPage.getNodeByPath(
                    "/BOM/BOM Option/Identifier/Scheme Agency Identifier", 3);
            EditBIEPage.BBIESCPanel bbieSCPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
            if (!bbieSCPanel.getUsedCheckbox().isSelected()) {
                bbieSCPanel.toggleUsed();
            }
            bbieSCPanel.setValueDomainRestriction("Agency");
            bbieSCPanel.setValueDomain(agencyIDLists.get(i).getName());
            editBIEPage.hitUpdateButton();

            editBIEPage.openPage();
            bbieSCNode = editBIEPage.getNodeByPath(
                    "/BOM/BOM Option/Identifier/Scheme Agency Identifier", 3);
            bbieSCPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
            assertTrue(getText(bbieSCPanel.getValueDomainField()).startsWith(agencyIDLists.get(i).getName()));

            ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
            expressBIEPage.selectBIEForExpression(bie);
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.XML, ".xsd",
                    () -> expressBIEPage.selectXMLSchemaExpression(), customValues.get(i).getValue(),
                    agencyIDLists.get(i).getName());
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.JSON, ".json",
                    () -> expressBIEPage.selectJSONSchemaExpression(), customValues.get(i).getValue(),
                    agencyIDLists.get(i).getName());
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.YML, ".yml", () -> {
                ExpressBIEPage.OpenAPIExpressionOptions options = expressBIEPage.selectOpenAPIExpression();
                options.selectVersion("3.1");
                options.selectYAMLOpenAPIFormat();
                options.toggleGETOperationTemplate();
                assertTrue(options.getGETOperationTemplateCheckbox().getAttribute("class")
                                .contains("mat-mdc-checkbox-checked"),
                        "The GET operation template must be enabled.");
                options.togglePOSTOperationTemplate();
                assertTrue(options.getPOSTOperationTemplateCheckbox().getAttribute("class")
                                .contains("mat-mdc-checkbox-checked"),
                        "The POST operation template must be enabled.");
            }, customValues.get(i).getValue(), agencyIDLists.get(i).getName());
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.ODS, ".ods",
                    () -> expressBIEPage.selectODFExpression("ODS"),
                    customValues.get(i).getValue(), agencyIDLists.get(i).getName());
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.AVRO, ".avsc",
                    () -> expressBIEPage.selectAvroExpression(),
                    customValues.get(i).getValue(), agencyIDLists.get(i).getName());
        }
    }

    @Test
    @DisplayName("TC_37_8_TA_5_Custom_Agency_ID_List_In_Inherited_BIE")
    public void custom_agency_id_list_assigned_to_base_bbie_sc_is_expressed_by_inherited_bie() throws Exception {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        AgencyIDListObject agencyIdList = getAPIFactory().getAgencyIDListAPI()
                .createRandomAgencyIDList(endUser, namespace, release, "Production");
        AgencyIDListValueObject agencyIdListValue = getAPIFactory().getAgencyIDListValueAPI()
                .createRandomAgencyIDListValue(endUser, agencyIdList);
        ASCCPObject bomASCCP = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject baseBie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), bomASCCP, endUser, "WIP");
        TopLevelASBIEPObject inheritedBie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), bomASCCP, endUser, "WIP");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBiePage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBie);
        WebElement bbieScNode = editBiePage.getNodeByPath(
                "/BOM/BOM Option/Identifier/Scheme Agency Identifier", 3);
        EditBIEPage.BBIESCPanel bbieScPanel = editBiePage.getBBIESCPanel(bbieScNode);
        if (!bbieScPanel.getUsedCheckbox().isSelected()) {
            bbieScPanel.toggleUsed();
        }
        bbieScPanel.setValueDomainRestriction("Agency");
        bbieScPanel.setValueDomain(agencyIdList.getName());
        editBiePage.hitUpdateButton();
        editBiePage.openPage();
        editBiePage.openUseBaseBIEDialog().selectBaseBIE(baseBie);

        ExpressBIEPage expressBiePage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBiePage.selectBIEForExpression(inheritedBie);
        expressBiePage.selectPutAllSchemasInTheSameFile();
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.XML, ".xsd",
                () -> expressBiePage.selectXMLSchemaExpression(), agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.JSON, ".json",
                () -> expressBiePage.selectJSONSchemaExpression(), agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.YML, ".yml", () -> {
            ExpressBIEPage.OpenAPIExpressionOptions options = expressBiePage.selectOpenAPIExpression();
            options.selectVersion("3.1");
            options.selectYAMLOpenAPIFormat();
            options.toggleGETOperationTemplate();
            assertTrue(options.getGETOperationTemplateCheckbox().getAttribute("class")
                            .contains("mat-mdc-checkbox-checked"),
                    "The GET operation template must be enabled.");
            options.togglePOSTOperationTemplate();
            assertTrue(options.getPOSTOperationTemplateCheckbox().getAttribute("class")
                            .contains("mat-mdc-checkbox-checked"),
                    "The POST operation template must be enabled.");
        }, agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.ODS, ".ods",
                () -> expressBiePage.selectODFExpression("ODS"),
                agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.AVRO, ".avsc",
                () -> expressBiePage.selectAvroExpression(),
                agencyIdListValue.getValue(), agencyIdList.getName());
    }

    @Test
    @DisplayName("TC_37_8_TA_6_Custom_Agency_ID_List_In_Reused_BIE")
    public void custom_agency_id_list_in_a_reused_bie_is_expressed_by_the_reusing_bie() throws Exception {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        AgencyIDListObject agencyIdList = getAPIFactory().getAgencyIDListAPI()
                .createRandomAgencyIDList(endUser, namespace, release, "Production");
        AgencyIDListValueObject agencyIdListValue = getAPIFactory().getAgencyIDListValueAPI()
                .createRandomAgencyIDListValue(endUser, agencyIdList);
        ASCCPObject partyASCCP = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "Party. Party", release.getReleaseNumber());
        ASCCPObject bomItemDataASCCP = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM Item Data. BOM Item Data", release.getReleaseNumber());
        ASCCPObject bomASCCP = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject partyBie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), partyASCCP, endUser, "WIP");
        TopLevelASBIEPObject bomItemDataBie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), bomItemDataASCCP, endUser, "WIP");
        TopLevelASBIEPObject parentBie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(Arrays.asList(context), bomASCCP, endUser, "WIP");
        getAPIFactory().getBusinessInformationEntityAPI().createBbieNodesForUsedElements(
                bomItemDataBie.getTopLevelAsbiepId(), endUser.getAppUserId());
        getAPIFactory().getBusinessInformationEntityAPI().createBbieScForFirstBbie(
                bomItemDataBie.getTopLevelAsbiepId(), endUser.getAppUserId());

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        EditBIEPage editBiePage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(partyBie);
        WebElement bbieScNode = editBiePage.getNodeByPath("/Party/Identifier/Scheme Agency Identifier", 3);
        EditBIEPage.BBIESCPanel bbieScPanel = editBiePage.getBBIESCPanel(bbieScNode);
        if (!bbieScPanel.getUsedCheckbox().isSelected()) {
            bbieScPanel.toggleUsed();
        }
        bbieScPanel.setValueDomainRestriction("Agency");
        bbieScPanel.setValueDomain(agencyIdList.getName());
        editBiePage.hitUpdateButton();

        editBiePage.openPage();
        editBiePage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bomItemDataBie);
        editBiePage.reuseBIEOnNode("/BOM Item Data/Party").selectBIEToReuse(partyBie);
        editBiePage.openPage();
        editBiePage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(parentBie);
        editBiePage.reuseBIEOnNode("/BOM/BOM Item Data").selectBIEToReuse(bomItemDataBie);
        ExpressBIEPage expressBiePage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBiePage.selectBIEForExpression(parentBie);
        expressBiePage.selectPutAllSchemasInTheSameFile();
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.XML, ".xsd",
                () -> expressBiePage.selectXMLSchemaExpression(), agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.JSON, ".json",
                () -> expressBiePage.selectJSONSchemaExpression(), agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.YML, ".yml", () -> {
            ExpressBIEPage.OpenAPIExpressionOptions options = expressBiePage.selectOpenAPIExpression();
            options.selectVersion("3.1");
            options.selectYAMLOpenAPIFormat();
            options.toggleGETOperationTemplate();
            assertTrue(options.getGETOperationTemplateCheckbox().getAttribute("class")
                            .contains("mat-mdc-checkbox-checked"),
                    "The GET operation template must be enabled.");
            options.togglePOSTOperationTemplate();
            assertTrue(options.getPOSTOperationTemplateCheckbox().getAttribute("class")
                            .contains("mat-mdc-checkbox-checked"),
                    "The POST operation template must be enabled.");
        }, agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.ODS, ".ods",
                () -> expressBiePage.selectODFExpression("ODS"),
                agencyIdListValue.getValue(), agencyIdList.getName());
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.AVRO, ".avsc",
                () -> expressBiePage.selectAvroExpression(),
                agencyIdListValue.getValue(), agencyIdList.getName());
    }

    private void generateAndCheck(ExpressBIEPage page, ExpressBIEPage.ExpressionFormat format,
                                 String extension, Runnable selectFormat,
                                 String expectedAgencyIdValue, String selectedAgencyIdListName) throws Exception {
        selectFormat.run();
        File generated = null;
        try {
            generated = page.hitGenerateButton(format,
                    filename -> filename.endsWith(extension));
            assertNotNull(generated, format + " generation did not produce a file for an Agency ID List with no selected value.");
            assertTrue(generated.length() > 0L, format + " output is empty.");
            if (format == ExpressBIEPage.ExpressionFormat.XML) {
                String content = Files.readString(generated.toPath(), StandardCharsets.UTF_8);
                assertAgencyIdListValueUsedByBbieSc(content, expectedAgencyIdValue, selectedAgencyIdListName);
            } else if (format == ExpressBIEPage.ExpressionFormat.JSON) {
                String content = Files.readString(generated.toPath(), StandardCharsets.UTF_8);
                assertTrue(content.contains("\"enum\"") && content.contains(expectedAgencyIdValue),
                        "Generated JSON Schema omitted the custom Agency ID List value '" +
                                expectedAgencyIdValue + "'.");
                assertTrue(content.contains("il_"),
                        "Generated JSON Schema omitted the Agency ID List definition for '" +
                                selectedAgencyIdListName + "'.");
            } else if (format == ExpressBIEPage.ExpressionFormat.YML) {
                Map<?, ?> document = (Map<?, ?>) new Yaml().load(
                        Files.readString(generated.toPath(), StandardCharsets.UTF_8));
                assertOpenApiOperationsAndAgencyIdEnum(document, expectedAgencyIdValue, selectedAgencyIdListName);
            } else if (format == ExpressBIEPage.ExpressionFormat.ODS) {
                String content = readOdsContent(generated);
                assertTrue(content.toLowerCase().contains("schemeagencyidentifier"),
                        "Generated ODS omitted the Scheme Agency Identifier BBIE_SC field.");
                assertFalse(content.contains(expectedAgencyIdValue),
                        "ODS should describe BIE fields without serializing Agency ID List values.");
            } else if (format == ExpressBIEPage.ExpressionFormat.AVRO) {
                JsonNode schema = new ObjectMapper().readTree(generated);
                List<String> fieldNames = schema.findValues("name").stream()
                        .map(JsonNode::asText).toList();
                assertTrue(fieldNames.stream().anyMatch(name -> {
                    String normalized = name.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
                    return normalized.contains("schemeagency") &&
                            (normalized.endsWith("id") || normalized.endsWith("identifier"));
                }), "Generated Avro schema omitted the Scheme Agency Identifier BBIE_SC field.");
                assertTrue(schema.findValues("enum").isEmpty(),
                        "Avro output should not serialize Agency ID List values as enums.");
                assertFalse(schema.toString().contains(expectedAgencyIdValue),
                        "Avro output should describe BIE fields without serializing Agency ID List values.");
            }
        } finally {
            if (generated != null) generated.delete();
        }
    }

    private void assertOpenApiOperationsAndAgencyIdEnum(Map<?, ?> document, String expectedAgencyIdValue,
                                                          String agencyIdListName) {
        Map<?, ?> paths = (Map<?, ?>) document.get("paths");
        assertNotNull(paths, "Generated OpenAPI document omitted paths.");
        assertTrue(paths.values().stream().filter(Map.class::isInstance).map(Map.class::cast)
                        .anyMatch(path -> path.containsKey("get") && path.containsKey("post")),
                "Generated OpenAPI document must contain the enabled GET and POST operation templates.");

        Map<?, ?> components = (Map<?, ?>) document.get("components");
        assertNotNull(components, "Generated OpenAPI document omitted components.");
        Map<?, ?> schemas = (Map<?, ?>) components.get("schemas");
        assertNotNull(schemas, "Generated OpenAPI document omitted component schemas.");
        assertTrue(schemas.values().stream().filter(Map.class::isInstance).map(Map.class::cast)
                        .map(schema -> schema.get("enum"))
                        .filter(List.class::isInstance).map(List.class::cast)
                        .flatMap(List::stream).map(String::valueOf).anyMatch(expectedAgencyIdValue::equals),
                "Generated OpenAPI component schemas omitted custom Agency ID List value '" +
                        expectedAgencyIdValue + "' for list '" + agencyIdListName + "'.");
    }

    private void assertAgencyIdListValueUsedByBbieSc(String schema, String expectedValue, String agencyIdListName) {
        assertNotNull(expectedValue, "A custom Agency ID List value is required for the XML Schema assertion.");
        assertNotNull(agencyIdListName, "A selected Agency ID List name is required for the XML Schema assertion.");
        String attributeName = "schemeAgencyID";
        Matcher attribute = java.util.regex.Pattern.compile(
                "<xsd:attribute\\s+name=\\\"" + attributeName + "\\\"[^>]*type=\\\"([^\\\"]+)\\\"")
                .matcher(schema);
        boolean foundSelectedListValue = false;
        while (attribute.find()) {
            String typeName = attribute.group(1);
            if (!typeName.contains(agencyIdListName)) {
                continue;
            }
            Matcher simpleType = java.util.regex.Pattern.compile(
                    "<xsd:simpleType\\s+name=\\\"" + java.util.regex.Pattern.quote(typeName) +
                            "\\\">(.*?)</xsd:simpleType>", java.util.regex.Pattern.DOTALL)
                    .matcher(schema);
            if (simpleType.find() && simpleType.group(1).contains(
                    "<xsd:enumeration value=\"" + expectedValue + "\"")) {
                foundSelectedListValue = true;
                break;
            }
        }
        assertTrue(foundSelectedListValue,
                "Generated XML Schema must include a Scheme Agency Identifier attribute whose type references " +
                        "the selected Agency ID List '" + agencyIdListName + "' and enumerates custom value '" +
                        expectedValue + "'.");
    }

    private String readOdsContent(File odsFile) throws Exception {
        try (ZipFile zipFile = new ZipFile(odsFile)) {
            ZipEntry contentEntry = zipFile.getEntry("content.xml");
            assertNotNull(contentEntry, "Generated ODS file omitted content.xml.");
            try (var inputStream = zipFile.getInputStream(contentEntry)) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(user -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId()));
    }
}
