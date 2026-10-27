package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The writer must give the bytes {@link XMLOutputter} gives for the tree the generator would have built whole.
 * A random tree of the shape the XML generator builds is built twice, whole and through the writer.
 */
class StreamingSchemaWriterTest {

    private static final Namespace XSD = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
    private static final Namespace CCTS = Namespace.getNamespace("ccts", "urn:example:ccts");
    private static final Namespace TARGET = Namespace.getNamespace("oagis", "urn:example:target");

    private static Format format() {
        return Format.getPrettyFormat().setIndent("\t");
    }

    private static Element xsd(String name) {
        return new Element(name, XSD);
    }

    private static Element schema() {
        Element schema = xsd("schema");
        schema.addNamespaceDeclaration(TARGET);
        schema.setAttribute("targetNamespace", TARGET.getURI());
        schema.setAttribute("elementFormDefault", "qualified");
        return schema;
    }

    private static Element annotated(Element element, String text, Random random) {
        Element annotation = xsd("annotation");
        Element documentation = xsd("documentation");
        documentation.setAttribute("source", "urn:example:" + text);
        Element name = new Element("ccts_Name", CCTS);
        name.setText(text + (random.nextBoolean() ? " & <more>" : " café 😀"));
        documentation.addContent(name);
        if (random.nextBoolean()) {
            documentation.addContent(new Element("srt_Version", TARGET).setText("1"));
        }
        annotation.addContent(documentation);
        element.addContent(annotation);
        return element;
    }

    private static final class Builder {
        final Random random;
        final Element schema;
        final StreamingSchemaWriter writer;
        int serial;

        Builder(long seed, Element schema, StreamingSchemaWriter writer) {
            this.random = new Random(seed);
            this.schema = schema;
            this.writer = writer;
        }

        void root(int depth) {
            Element root = xsd("element");
            root.setAttribute("name", "Root" + serial++);
            schema.addContent(root);
            if (random.nextInt(4) > 0) {
                annotated(root, "root", random);
            }
            if (writer != null) {
                writer.enter(root);
            }
            abie(root, depth);
            if (writer != null) {
                writer.exit(root);
            }
        }

        /** Mimics generateABIE and generateBIEs. */
        void abie(Element parent, int depth) {
            Element complexType = xsd("complexType");
            parent.addContent(complexType);
            annotated(complexType, "abie", random);
            Element compositor = xsd(random.nextInt(5) == 0 ? "choice" : "sequence");
            complexType.addContent(compositor);
            if (writer != null) {
                writer.enter(complexType);
                writer.enter(compositor);
            }
            int count = depth <= 0 ? random.nextInt(3) : random.nextInt(5);
            for (int i = 0; i < count; i++) {
                if (writer != null) {
                    writer.flushChildren();
                }
                switch (random.nextInt(depth <= 0 ? 4 : 6)) {
                    case 0, 1 -> {
                        Element leaf = xsd("element");
                        leaf.setAttribute("name", "Leaf" + serial++);
                        leaf.setAttribute("type", "oagis:Type");
                        compositor.addContent(leaf);
                        if (random.nextBoolean()) {
                            annotated(leaf, "leaf", random);
                        }
                        if (random.nextInt(3) == 0) {
                            Element ct = xsd("complexType");
                            Element sc = xsd("simpleContent");
                            Element ext = xsd("extension");
                            ext.setAttribute("base", "xsd:token");
                            ext.addContent(xsd("attribute").setAttribute("name", "a" + serial++));
                            sc.addContent(ext);
                            ct.addContent(sc);
                            leaf.addContent(ct);
                        }
                    }
                    case 2 -> complexType.addContent(xsd("attribute").setAttribute("name", "attr" + serial++));
                    case 3 -> {
                        Element global = xsd("simpleType");
                        global.setAttribute("name", "Global" + serial++);
                        global.addContent(xsd("restriction").setAttribute("base", "xsd:token"));
                        schema.addContent(global);
                    }
                    default -> {
                        Element nested = xsd("element");
                        nested.setAttribute("name", "Nested" + serial++);
                        nested.setAttribute("minOccurs", "0");
                        compositor.addContent(nested);
                        annotated(nested, "nested", random);
                        if (writer != null) {
                            writer.enter(nested);
                        }
                        abie(nested, depth - 1);
                        if (writer != null) {
                            writer.exit(nested);
                        }
                    }
                }
            }
            if (writer != null) {
                writer.flushChildren();
                if (!writer.isStarted(compositor) && compositor.getChildren().isEmpty()) {
                    writer.discard(compositor);
                } else {
                    writer.exit(compositor);
                }
                writer.exit(complexType);
            } else if (compositor.getChildren().isEmpty()) {
                compositor.detach();
            }
        }
    }

    private static void run(long seed, int roots, Builder builder) {
        Random directives = new Random(seed ^ 0x5DEECE66DL);
        for (int i = 0; i < roots; i++) {
            builder.root(3);
            if (directives.nextBoolean()) {
                Element include = xsd("include").setAttribute("schemaLocation", "f" + i + ".xsd");
                int index = 0;
                while (index < builder.schema.getContentSize()
                        && builder.schema.getContent(index) instanceof Element e && e.getName().equals("include")) {
                    index++;
                }
                builder.schema.addContent(index, include);
            }
        }
    }

    @Test
    void sameBytesAsTheWholeTree(@TempDir File dir) throws IOException {
        for (long seed = 0; seed < 300; seed++) {
            int roots = 1 + (int) (seed % 3);

            Element wholeSchema = schema();
            Document document = new Document(wholeSchema);
            run(seed, roots, new Builder(seed, wholeSchema, null));
            File expected = new File(dir, "expected-" + seed + ".xsd");
            try (var out = Files.newOutputStream(expected.toPath())) {
                new XMLOutputter(format()).output(document, out);
            }

            Element streamSchema = schema();
            StreamingSchemaWriter writer = new StreamingSchemaWriter(streamSchema, format());
            run(seed, roots, new Builder(seed, streamSchema, writer));
            File actual = new File(dir, "actual-" + seed + ".xsd");
            writer.assemble(actual);
            writer.dispose();

            assertArrayEquals(Files.readAllBytes(expected.toPath()), Files.readAllBytes(actual.toPath()),
                    "seed " + seed);
        }
    }

    @Test
    void detachedGlobalsAreLeftOutAndReassembleKeepsTheRest(@TempDir File dir) throws IOException {
        Element wholeSchema = schema();
        Document document = new Document(wholeSchema);
        Element streamSchema = schema();
        StreamingSchemaWriter writer = new StreamingSchemaWriter(streamSchema, format());
        Builder whole = new Builder(7, wholeSchema, null);
        Builder streamed = new Builder(7, streamSchema, writer);
        whole.root(3);
        streamed.root(3);

        Element wholeGlobal = wholeSchema.getChildren("simpleType", XSD).stream().findFirst().orElse(null);
        Element streamGlobal = streamSchema.getChildren("simpleType", XSD).stream().findFirst().orElse(null);
        assertEquals(wholeGlobal == null, streamGlobal == null);
        if (wholeGlobal != null) {
            wholeGlobal.detach();
            streamGlobal.detach();
        }
        wholeSchema.addContent(0, xsd("include").setAttribute("schemaLocation", "common.xsd"));
        streamSchema.addContent(0, xsd("include").setAttribute("schemaLocation", "common.xsd"));

        File expected = new File(dir, "expected.xsd");
        try (var out = Files.newOutputStream(expected.toPath())) {
            new XMLOutputter(format()).output(document, out);
        }
        File actual = new File(dir, "actual.xsd");
        writer.assemble(actual);
        writer.dispose();
        assertArrayEquals(Files.readAllBytes(expected.toPath()), Files.readAllBytes(actual.toPath()));
    }
}
