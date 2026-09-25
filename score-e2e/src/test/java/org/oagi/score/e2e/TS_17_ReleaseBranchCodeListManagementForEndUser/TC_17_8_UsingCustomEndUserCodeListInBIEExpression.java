package org.oagi.score.e2e.TS_17_ReleaseBranchCodeListManagementForEndUser;

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
import org.oagi.score.e2e.page.code_list.EditCodeListPage;
import org.oagi.score.e2e.page.code_list.ViewEditCodeListPage;
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
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.oagi.score.e2e.impl.PageHelper.getText;

@Execution(ExecutionMode.CONCURRENT)
public class TC_17_8_UsingCustomEndUserCodeListInBIEExpression extends BaseTest {

    private final List<AppUserObject> randomAccounts = new ArrayList<>();

    @BeforeEach
    public void init() {
        super.init();
    }

    private void thisAccountWillBeDeletedAfterTests(AppUserObject appUser) {
        this.randomAccounts.add(appUser);
    }

    @Test
    @DisplayName("TC_17_8_TA_1_TA_2_TA_3_All_Expression_Formats")
    public void custom_code_lists_on_bbie_and_bbie_sc_generate_all_expression_formats() throws Exception {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        CodeListObject baseCodeList = getAPIFactory().getCodeListAPI().getCodeListByCodeListNameAndReleaseNum(
                "clm6TimeFormatCode1_TimeFormatCode", release.getReleaseNumber());
        List<CodeListValueObject> inheritedValues = getAPIFactory().getCodeListValueAPI()
                .getCodeListValuesByCodeListManifestId(baseCodeList.getCodeListManifestId());
        assertFalse(inheritedValues.isEmpty(), "The seeded base Code List must contain an inherited value.");

        List<CodeListObject> codeLists = new ArrayList<>();
        List<CodeListValueObject> customValues = new ArrayList<>();

        CodeListObject unbasedCodeList = getAPIFactory().getCodeListAPI().createRandomCodeList(
                endUser, namespace, release, "Production");
        CodeListValueObject unbasedValue = getAPIFactory().getCodeListValueAPI()
                .createRandomCodeListValue(unbasedCodeList, endUser);
        codeLists.add(unbasedCodeList);
        customValues.add(unbasedValue);

        CodeListObject basedCodeList = getAPIFactory().getCodeListAPI().createDerivedCodeList(
                baseCodeList, endUser, namespace, release, "Production");
        CodeListValueObject basedValue = getAPIFactory().getCodeListValueAPI()
                .createRandomCodeListValue(basedCodeList, endUser);
        codeLists.add(basedCodeList);
        customValues.add(basedValue);

        ASCCPObject bomASCCP = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        assertNotNull(bomASCCP, "The seeded BOM. BOM ASCCP is required for the BBIE and BBIE_SC fixtures.");

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        for (int i = 0; i < codeLists.size(); i++) {
            BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
            TopLevelASBIEPObject bie = getAPIFactory().getBusinessInformationEntityAPI()
                    .generateRandomTopLevelASBIEP(Arrays.asList(context), bomASCCP, endUser, "WIP");

            EditBIEPage editBIEPage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(bie);
            WebElement bbieNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Default Indicator");
            EditBIEPage.BBIEPanel bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
            if (!bbiePanel.getUsedCheckbox().isSelected()) {
                bbiePanel.toggleUsed();
            }
            bbiePanel.setValueDomainRestriction("Code");
            bbiePanel.setValueDomain(codeLists.get(i).getName());
            editBIEPage.hitUpdateButton();

            // Updating the BBIE re-renders the tree; reload before locating the BBIE_SC.
            editBIEPage.openPage();
            WebElement bbieSCNode = editBIEPage.getNodeByPath(
                    "/BOM/BOM Option/Description/Language Code", 3);
            EditBIEPage.BBIESCPanel bbieSCPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
            if (!bbieSCPanel.getUsedCheckbox().isSelected()) {
                bbieSCPanel.toggleUsed();
            }
            bbieSCPanel.setValueDomainRestriction("Code");
            bbieSCPanel.setValueDomain(codeLists.get(i).getName());
            editBIEPage.hitUpdateButton();

            editBIEPage.openPage();
            bbieNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Default Indicator");
            bbiePanel = editBIEPage.getBBIEPanel(bbieNode);
            bbieSCNode = editBIEPage.getNodeByPath("/BOM/BOM Option/Description/Language Code", 3);
            bbieSCPanel = editBIEPage.getBBIESCPanel(bbieSCNode);
            assertTrue(getText(bbiePanel.getValueDomainField()).startsWith(codeLists.get(i).getName()));
            assertTrue(getText(bbieSCPanel.getValueDomainField()).startsWith(codeLists.get(i).getName()));

            ExpressBIEPage expressBIEPage = homePage.getBIEMenu().openExpressBIESubMenu();
            expressBIEPage.selectBIEForExpression(bie);
            expressBIEPage.selectPutAllSchemasInTheSameFile();

            String expectedValue = customValues.get(i).getValue();
            String expectedInheritedValue = (codeLists.get(i) == basedCodeList) ? inheritedValues.get(0).getValue() : null;
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.XML, ".xsd",
                    () -> expressBIEPage.selectXMLSchemaExpression(), expectedValue, expectedInheritedValue,
                    codeLists.get(i).getName(), true);
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.JSON, ".json",
                    () -> expressBIEPage.selectJSONSchemaExpression(), expectedValue, expectedInheritedValue,
                    codeLists.get(i).getName(), false);
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.YML, ".yml",
                    () -> {
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
                    }, expectedValue, expectedInheritedValue, codeLists.get(i).getName(), false);
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.ODS, ".ods",
                    () -> expressBIEPage.selectODFExpression("ODS"), expectedValue, expectedInheritedValue,
                    codeLists.get(i).getName(), false);
            generateAndCheck(expressBIEPage, ExpressBIEPage.ExpressionFormat.AVRO, ".avsc",
                    () -> expressBIEPage.selectAvroExpression(), expectedValue, expectedInheritedValue,
                    codeLists.get(i).getName(), false);

            // The Express BIE table is searched by DEN; remove this BIE before
            // creating the next one so the same-DEN row cannot be selected instead.
            getAPIFactory().getBusinessInformationEntityAPI().deleteTopLevelASBIEPByTopLevelASBIEPId(bie);
        }
    }

    @Test
    @DisplayName("TC_17_8_TA_4_Custom_Code_List_Managed_By_Custom_Agency_ID_List_In_Inherited_BIE")
    public void custom_code_list_managed_by_custom_agency_id_list_is_preserved_by_bie_inheritance() throws Exception {
        AppUserObject endUser = getAPIFactory().getAppUserAPI().createRandomEndUserAccount(false);
        thisAccountWillBeDeletedAfterTests(endUser);

        LibraryObject library = getAPIFactory().getLibraryAPI().getLibraryByName("connectSpec");
        ReleaseObject release = getAPIFactory().getReleaseAPI().getReleaseByReleaseNumber(library, "10.8.7.1");
        NamespaceObject namespace = getAPIFactory().getNamespaceAPI().createRandomEndUserNamespace(endUser, library);
        AgencyIDListObject customAgencyIdList = getAPIFactory().getAgencyIDListAPI()
                .createRandomAgencyIDList(endUser, namespace, release, "Production");
        AgencyIDListValueObject customAgencyIdValue = getAPIFactory().getAgencyIDListValueAPI()
                .createRandomAgencyIDListValue(endUser, customAgencyIdList);
        CodeListObject customCodeList = getAPIFactory().getCodeListAPI()
                .createRandomCodeList(endUser, namespace, release, "WIP");
        CodeListValueObject customCodeValue = getAPIFactory().getCodeListValueAPI()
                .createRandomCodeListValue(customCodeList, endUser);

        HomePage homePage = loginPage().signIn(endUser.getLoginId(), endUser.getPassword());
        ViewEditCodeListPage viewEditCodeListPage = homePage.getCoreComponentMenu().openViewEditCodeListSubMenu();
        EditCodeListPage editCodeListPage = viewEditCodeListPage.openCodeListViewEditPage(customCodeList);
        editCodeListPage.setAgencyIDList(customAgencyIdList);
        editCodeListPage.setAgencyIDListValue(customAgencyIdValue);
        editCodeListPage.hitUpdateButton();
        CodeListObject savedCodeList = getAPIFactory().getCodeListAPI()
                .getCodeListByManifestId(customCodeList.getCodeListManifestId());
        assertEquals(customAgencyIdValue.getAgencyIDListValueManifestId(),
                savedCodeList.getAgencyIdListValueManifestId(),
                "The Code List must retain the selected custom Agency ID List Value.");
        editCodeListPage.moveToQA();
        editCodeListPage.moveToProduction();

        ASCCPObject bomASCCP = getAPIFactory().getCoreComponentAPI()
                .getASCCPByDENAndReleaseNum(library, "BOM. BOM", release.getReleaseNumber());
        BusinessContextObject context = getAPIFactory().getBusinessContextAPI().createRandomBusinessContext(endUser);
        TopLevelASBIEPObject baseBie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(List.of(context), bomASCCP, endUser, "WIP");
        TopLevelASBIEPObject inheritedBie = getAPIFactory().getBusinessInformationEntityAPI()
                .generateRandomTopLevelASBIEP(List.of(context), bomASCCP, endUser, "WIP");

        EditBIEPage editBiePage = homePage.getBIEMenu().openViewEditBIESubMenu().openEditBIEPage(baseBie);
        WebElement bbieNode = editBiePage.getNodeByPath("/BOM/BOM Option/Default Indicator");
        EditBIEPage.BBIEPanel bbiePanel = editBiePage.getBBIEPanel(bbieNode);
        if (!bbiePanel.getUsedCheckbox().isSelected()) {
            bbiePanel.toggleUsed();
        }
        bbiePanel.setValueDomainRestriction("Code");
        bbiePanel.setValueDomain(customCodeList.getName());
        editBiePage.hitUpdateButton();

        editBiePage.openPage();
        WebElement bbieScNode = editBiePage.getNodeByPath("/BOM/BOM Option/Description/Language Code", 3);
        EditBIEPage.BBIESCPanel bbieScPanel = editBiePage.getBBIESCPanel(bbieScNode);
        if (!bbieScPanel.getUsedCheckbox().isSelected()) {
            bbieScPanel.toggleUsed();
        }
        bbieScPanel.setValueDomainRestriction("Code");
        bbieScPanel.setValueDomain(customCodeList.getName());
        editBiePage.hitUpdateButton();

        editBiePage.openPage();
        editBiePage.openUseBaseBIEDialog().selectBaseBIE(baseBie);
        ExpressBIEPage expressBiePage = homePage.getBIEMenu().openExpressBIESubMenu();
        expressBiePage.selectBIEForExpression(inheritedBie);
        expressBiePage.selectPutAllSchemasInTheSameFile();

        String expectedCodeValue = customCodeValue.getValue();
        String expectedAgencyIdValue = customAgencyIdValue.getValue();
        String customListName = customCodeList.getName();
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.XML, ".xsd",
                () -> expressBiePage.selectXMLSchemaExpression(), expectedCodeValue, null, customListName, true);
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.JSON, ".json",
                () -> expressBiePage.selectJSONSchemaExpression(), expectedCodeValue, null, customListName, false);
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
        }, expectedCodeValue, null, customListName, false);
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.ODS, ".ods",
                () -> expressBiePage.selectODFExpression("ODS"), expectedCodeValue, null, customListName, false);
        generateAndCheck(expressBiePage, ExpressBIEPage.ExpressionFormat.AVRO, ".avsc",
                () -> expressBiePage.selectAvroExpression(), expectedCodeValue, null, customListName, false);

        expressBiePage.selectXMLSchemaExpression();
        File xsd = expressBiePage.hitGenerateButton(ExpressBIEPage.ExpressionFormat.XML,
                filename -> filename.endsWith(".xsd"));
        try {
            String schema = Files.readString(xsd.toPath(), StandardCharsets.UTF_8);
            assertTrue(schema.contains("cl" + expectedAgencyIdValue + "_"),
                    "Generated inherited BIE schema omitted custom Agency ID List value '" + expectedAgencyIdValue +
                            "' in its Code List type name. Types: " +
                            String.join(", ", extractTypeNames(schema)));
        } finally {
            if (xsd != null) xsd.delete();
        }
    }

    private List<String> extractTypeNames(String schema) {
        List<String> typeNames = new ArrayList<>();
        Matcher matcher = Pattern.compile("<xsd:simpleType name=\"([^\"]+)\"").matcher(schema);
        while (matcher.find()) {
            typeNames.add(matcher.group(1));
        }
        return typeNames;
    }

    private void generateAndCheck(ExpressBIEPage page, ExpressBIEPage.ExpressionFormat format,
                                 String extension, Runnable selectFormat,
                                 String expectedValue, String expectedInheritedValue,
                                 String codeListName, boolean checkXsdEnumeration) throws Exception {
        selectFormat.run();
        File generated = null;
        try {
            generated = page.hitGenerateButton(format,
                    filename -> filename.endsWith(extension));
            assertNotNull(generated, format + " generation did not produce a file.");
            assertTrue(generated.length() > 0L, format + " output is empty.");
            if (checkXsdEnumeration) {
                String content = Files.readString(generated.toPath(), StandardCharsets.UTF_8);
                assertCodeListValueUsedByBothNodes(content, expectedValue, expectedInheritedValue, codeListName);
            } else if (format == ExpressBIEPage.ExpressionFormat.JSON) {
                String content = Files.readString(generated.toPath(), StandardCharsets.UTF_8);
                assertTrue(content.contains("\"enum\"") && content.contains(expectedValue),
                        "Generated JSON Schema omitted the custom Code List value '" + expectedValue + "'.");
                if (expectedInheritedValue != null) {
                    assertTrue(content.contains(expectedInheritedValue),
                            "Generated JSON Schema omitted inherited Code List value '" + expectedInheritedValue + "'.");
                }
            } else if (format == ExpressBIEPage.ExpressionFormat.YML) {
                Map<?, ?> document = (Map<?, ?>) new Yaml().load(
                        Files.readString(generated.toPath(), StandardCharsets.UTF_8));
                assertOpenApiOperationsAndEnums(document, expectedValue, expectedInheritedValue);
            } else if (format == ExpressBIEPage.ExpressionFormat.ODS) {
                String content = readOdsContent(generated);
                assertTrue(content.toLowerCase().contains("defaultindicator"),
                        "Generated ODS omitted the custom Code List BBIE field.");
                assertTrue(content.toLowerCase().contains("languagecode"),
                        "Generated ODS omitted the custom Code List BBIE_SC field.");
                assertFalse(content.contains(expectedValue),
                        "ODS should describe BIE fields without serializing Code List values.");
                if (expectedInheritedValue != null) {
                    assertFalse(content.contains(expectedInheritedValue),
                            "ODS should describe BIE fields without serializing inherited Code List values.");
                }
            } else if (format == ExpressBIEPage.ExpressionFormat.AVRO) {
                JsonNode schema = new ObjectMapper().readTree(generated);
                List<String> fieldNames = schema.findValues("name").stream()
                        .map(JsonNode::asText).toList();
                assertTrue(fieldNames.contains("DefaultIndicator"),
                        "Generated Avro schema omitted the custom Code List BBIE field.");
                assertTrue(fieldNames.stream().anyMatch(name -> name.toLowerCase().contains("language")),
                        "Generated Avro schema omitted the custom Code List BBIE_SC field.");
                assertTrue(schema.findValues("enum").isEmpty(),
                        "Avro output should not serialize Code List values as enums.");
                assertFalse(schema.toString().contains(expectedValue),
                        "Avro output should describe BIE fields without serializing Code List values.");
                if (expectedInheritedValue != null) {
                    assertFalse(schema.toString().contains(expectedInheritedValue),
                            "Avro output should not serialize inherited Code List values.");
                }
            }
        } finally {
            if (generated != null) generated.delete();
        }
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

    private void assertOpenApiOperationsAndEnums(Map<?, ?> document, String expectedValue,
                                                 String expectedInheritedValue) {
        Map<?, ?> components = (Map<?, ?>) document.get("components");
        assertNotNull(components, "Generated OpenAPI document omitted components.");
        Map<?, ?> schemas = (Map<?, ?>) components.get("schemas");
        assertNotNull(schemas, "Generated OpenAPI document omitted component schemas.");
        assertFalse(schemas.isEmpty(), "Generated OpenAPI document has no schemas despite enabled GET and POST templates.");
        assertTrue(schemas.values().stream().filter(Map.class::isInstance).map(Map.class::cast)
                        .map(schema -> schema.get("enum"))
                        .filter(List.class::isInstance).map(List.class::cast)
                        .flatMap(List::stream).map(String::valueOf).anyMatch(expectedValue::equals),
                "Generated OpenAPI component schemas omitted custom Code List value '" + expectedValue + "'.");
        if (expectedInheritedValue != null) {
            assertTrue(schemas.values().stream().filter(Map.class::isInstance).map(Map.class::cast)
                            .map(schema -> schema.get("enum"))
                            .filter(List.class::isInstance).map(List.class::cast)
                            .flatMap(List::stream).map(String::valueOf).anyMatch(expectedInheritedValue::equals),
                    "Generated OpenAPI component schemas omitted inherited Code List value '" +
                            expectedInheritedValue + "'.");
        }
    }

    private void assertCodeListValueUsedByBothNodes(String schema, String expectedValue,
                                                    String expectedInheritedValue, String codeListName) {
        Matcher bbieType = Pattern.compile(
                "<xsd:element name=\"DefaultIndicator\"[^>]*type=\"([^\"]+)\"").matcher(schema);
        assertTrue(bbieType.find(), "Generated XML Schema omitted the BBIE code-list type for " + codeListName + ".");

        Matcher bbieScType = Pattern.compile(
                "<xsd:attribute name=\"languageCode\"[^>]*type=\"([^\"]+)\"").matcher(schema);
        assertTrue(bbieScType.find(), "Generated XML Schema omitted the BBIE_SC code-list type for " + codeListName + ".");
        assertEquals(bbieType.group(1), bbieScType.group(1),
                "BBIE and BBIE_SC must reference the selected custom Code List type.");
        assertTrue(bbieType.group(1).contains(codeListName),
                "Generated XML Schema used a different Code List type than the selected list '" + codeListName + "'.");

        String typeName = Pattern.quote(bbieType.group(1));
        Matcher enumerationType = Pattern.compile(
                "<xsd:simpleType name=\"" + typeName + "\">(.*?)</xsd:simpleType>", Pattern.DOTALL).matcher(schema);
        assertTrue(enumerationType.find(), "Generated XML Schema omitted the custom Code List enumeration type.");
        assertTrue(enumerationType.group(1).contains("<xsd:enumeration value=\"" + expectedValue + "\""),
                "Generated XML Schema omitted custom Code List value '" + expectedValue +
                        "' for list '" + codeListName + "'.");
        if (expectedInheritedValue != null) {
            assertTrue(enumerationType.group(1).contains("<xsd:enumeration value=\"" + expectedInheritedValue + "\""),
                    "Generated XML Schema omitted inherited Code List value '" + expectedInheritedValue +
                            "' for based list '" + codeListName + "'.");
        }
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        this.randomAccounts.forEach(user -> getAPIFactory().getAppUserAPI().deleteAppUserByLoginId(user.getLoginId()));
    }
}
