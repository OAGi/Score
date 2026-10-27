package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieXMLGenerateExpression.CommonComponents;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieXMLGenerateExpression.CommonComponents.Declaration;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieXMLGenerateExpression.CommonComponents.GeneratedFile;
import org.oagi.score.gateway.http.api.bie_management.service.generate_expression.BieXMLGenerateExpression.CommonComponents.Kind;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BieXMLGenerateExpressionCommonComponentsTest {

    private static final Namespace XSD = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    private static Element simpleType(String name, String base) {
        Element simpleType = new Element("simpleType", XSD);
        simpleType.setAttribute("name", name);
        Element restriction = new Element("restriction", XSD);
        restriction.setAttribute("base", base);
        simpleType.addContent(restriction);
        return simpleType;
    }

    private static void write(Document document, File file) throws IOException {
        try (var out = new FileOutputStream(file)) {
            new XMLOutputter(Format.getPrettyFormat()).output(document, out);
        }
    }

    /** A generated file of the namespace {@code urn:example}, as the generator leaves it: the types are declared in it. */
    private static GeneratedFile generatedFile(Path dir, String name, String prefix, List<String> includes,
                                               Map<String, Element> components, Map<String, Kind> kinds,
                                               Element... elements) throws IOException {
        return generatedFile(dir, name, "urn:example", prefix, includes, components, kinds, elements);
    }

    private static GeneratedFile generatedFile(Path dir, String name, String namespaceUri, String prefix,
                                               List<String> includes, Map<String, Element> components,
                                               Map<String, Kind> kinds, Element... elements) throws IOException {
        Element schema = new Element("schema", XSD);
        schema.addNamespaceDeclaration(XSD);
        schema.addNamespaceDeclaration(Namespace.getNamespace(prefix, namespaceUri));
        schema.setAttribute("targetNamespace", namespaceUri);
        schema.setAttribute("elementFormDefault", "qualified");
        for (String include : includes) {
            Element e = new Element("include", XSD);
            e.setAttribute("schemaLocation", include);
            schema.addContent(e);
        }
        List<Declaration> declarations = new ArrayList<>();
        components.forEach((key, element) -> {
            schema.addContent(element);
            declarations.add(new Declaration(kinds.get(key), key, element));
        });
        for (Element element : elements) {
            schema.addContent(element);
        }
        Document document = new Document(schema);
        File file = dir.resolve(name).toFile();
        write(document, file);
        return new GeneratedFile(file, document, schema, namespaceUri, prefix, new LinkedHashSet<>(includes), declarations);
    }

    private static void assertValid(Path dir, String name) {
        assertDoesNotThrow(() -> SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new StreamSource(dir.resolve(name).toFile())));
    }

    private static CommonComponents commonComponents(Path dir) {
        return new CommonComponents((identity, name) -> name);
    }

    @Test
    void filesThatDeclareNothingTwiceAreLeftAsTheyAre(@TempDir Path dir) throws Exception {
        CommonComponents common = commonComponents(dir);
        common.register(generatedFile(dir, "A.xsd", "", List.of(), Map.of("XBT_1", simpleType("xbt_BooleanType", "xsd:boolean")),
                Map.of("XBT_1", Kind.DATA_TYPE)));
        common.register(generatedFile(dir, "B.xsd", "", List.of("A.xsd"), Map.of("XBT_2", simpleType("xbt_DateType", "xsd:date")),
                Map.of("XBT_2", Kind.DATA_TYPE)));
        String before = Files.readString(dir.resolve("B.xsd"));

        assertTrue(common.asFiles().isEmpty());
        assertEquals(before, Files.readString(dir.resolve("B.xsd")));
    }

    @Test
    void filesThatAreNotJoinedKeepTheirOwnDeclarations(@TempDir Path dir) throws Exception {
        CommonComponents common = commonComponents(dir);
        for (String name : new String[]{"A.xsd", "B.xsd"}) {
            common.register(generatedFile(dir, name, "", List.of(),
                    Map.of("XBT_1", simpleType("xbt_BooleanType", "xsd:boolean")), Map.of("XBT_1", Kind.DATA_TYPE)));
        }

        assertTrue(common.asFiles().isEmpty());
        assertTrue(Files.readString(dir.resolve("A.xsd")).contains("xbt_BooleanType"));
    }

    @Test
    void aComponentThatJoinedFilesDeclareMovesToACommonFile(@TempDir Path dir) throws Exception {
        CommonComponents common = commonComponents(dir);
        for (String name : new String[]{"A.xsd", "B.xsd"}) {
            common.register(generatedFile(dir, name, "", List.of(),
                    Map.of("XBT_1", simpleType("xbt_BooleanType", "xsd:boolean")), Map.of("XBT_1", Kind.DATA_TYPE)));
        }
        common.register(generatedFile(dir, "Root.xsd", "", List.of("A.xsd", "B.xsd"), Map.of(), Map.of()));
        assertThrows(org.xml.sax.SAXException.class, () -> SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(new StreamSource(dir.resolve("Root.xsd").toFile())), "the files declare the type twice");

        List<File> commonFiles = common.asFiles();

        assertEquals(List.of("CommonDataType.xsd"), commonFiles.stream().map(File::getName).toList());
        for (File file : commonFiles) {
            Files.copy(file.toPath(), dir.resolve(file.getName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        assertTrue(Files.readString(dir.resolve("CommonDataType.xsd")).contains("targetNamespace=\"urn:example\""));
        for (String name : new String[]{"A.xsd", "B.xsd"}) {
            String content = Files.readString(dir.resolve(name));
            assertFalse(content.contains("<xsd:simpleType"), name + " must not declare the type");
            assertTrue(content.contains("schemaLocation=\"CommonDataType.xsd\""));
        }
        assertValid(dir, "Root.xsd");
    }

    @Test
    void aCommonFileOfAPrefixedNamespaceEndsWithThePrefix(@TempDir Path dir) throws Exception {
        CommonComponents common = commonComponents(dir);
        for (String name : new String[]{"A.xsd", "B.xsd"}) {
            Map<String, Element> components = new LinkedHashMap<>();
            components.put("XBT_1", simpleType("xbt_BooleanType", "xsd:boolean"));
            // a code list whose base is the XBT, qualified as in a document with the prefixed namespace
            components.put("CodeList_1", simpleType("cl_Code", "ex:xbt_BooleanType"));
            Element element = new Element("element", XSD);
            element.setAttribute("name", "Flag" + name.charAt(0));
            element.setAttribute("type", "ex:cl_Code");
            common.register(generatedFile(dir, name, "ex", List.of(), components,
                    Map.of("XBT_1", Kind.DATA_TYPE, "CodeList_1", Kind.CODE_LIST), element));
        }
        common.register(generatedFile(dir, "Root.xsd", "ex", List.of("A.xsd", "B.xsd"), Map.of(), Map.of()));

        List<File> commonFiles = common.asFiles();
        for (File file : commonFiles) {
            Files.copy(file.toPath(), dir.resolve(file.getName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        assertEquals(List.of("CommonDataType_ex.xsd", "CommonCodeList_ex.xsd"), commonFiles.stream().map(File::getName).toList());
        assertTrue(Files.readString(dir.resolve("CommonCodeList_ex.xsd")).contains("schemaLocation=\"CommonDataType_ex.xsd\""));
        assertTrue(Files.readString(dir.resolve("CommonCodeList_ex.xsd")).contains("base=\"ex:xbt_BooleanType\""));
        assertValid(dir, "Root.xsd");
    }

    @Test
    void eachNamespaceGetsItsOwnCommonFile(@TempDir Path dir) throws Exception {
        CommonComponents common = commonComponents(dir);
        for (String[] ns : new String[][]{{"urn:one", ""}, {"urn:two", "t"}}) {
            String suffix = ns[1].isEmpty() ? "1" : "2";
            for (String name : new String[]{"A" + suffix + ".xsd", "B" + suffix + ".xsd"}) {
                common.register(generatedFile(dir, name, ns[0], ns[1], List.of(),
                        Map.of("XBT_1", simpleType("xbt_BooleanType", "xsd:boolean")), Map.of("XBT_1", Kind.DATA_TYPE)));
            }
            common.register(generatedFile(dir, "Root" + suffix + ".xsd", ns[0], ns[1],
                    List.of("A" + suffix + ".xsd", "B" + suffix + ".xsd"), Map.of(), Map.of()));
        }

        List<File> commonFiles = common.asFiles();
        for (File file : commonFiles) {
            Files.copy(file.toPath(), dir.resolve(file.getName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        assertEquals(List.of("CommonDataType.xsd", "CommonDataType_t.xsd"), commonFiles.stream().map(File::getName).toList());
        assertValid(dir, "Root1.xsd");
        assertValid(dir, "Root2.xsd");
    }
}
