package org.oagi.srt;

import net.sf.saxon.lib.NamespaceConstant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class SKOSGenerator {

    private static Map<String, Document> documentMap = new HashMap();

    public static final String NS_CCTS_PREFIX = "ccts";
    public static final String NS_XSD_PREFIX = "xsd";
    public static final String NS_XS_PREFIX = "xs";
    public static final String NS_CCTS = "urn:un:unece:uncefact:documentation:1.1";
    public static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";

    public static final String OAGI_NS = "http://www.openapplications.org/oagis/10";

    public static XPath xPath;
    static {
        System.setProperty("javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
        XPathFactory xPathFactory = null;
        try {
            xPathFactory = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        } catch (XPathFactoryConfigurationException e) {
            throw new IllegalStateException(e);
        }
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case NS_CCTS_PREFIX:
                        return NS_CCTS;
                    case NS_XSD_PREFIX:
                        return NS_XSD;
                    case "":
                    default:
                        return OAGI_NS;
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                switch (namespaceURI) {
                    case NS_XSD:
                        return NS_XSD_PREFIX;
                    case NS_CCTS:
                        return NS_CCTS_PREFIX;
                    case OAGI_NS:
                    default:
                        return "";
                }
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                List<String> prefixes;
                switch (namespaceURI) {
                    case NS_XSD:
                        prefixes = Arrays.asList(NS_XSD_PREFIX, NS_XS_PREFIX);
                        break;
                    case NS_CCTS:
                        prefixes = Arrays.asList(NS_CCTS_PREFIX);
                        break;
                    case OAGI_NS:
                    case "":
                    default:
                        prefixes = Arrays.asList("");
                }
                return prefixes.iterator();
            }
        });
    }

    public Document loadDocument(File file) {
        return loadDocument(file.toURI());
    }

    public Document loadDocument(URI uri) {
        return loadDocument(uri.toString());
    }

    public Document loadDocument(String uri) {
        String module = extractModuleName(uri);

        Document xmlDocument;
        if (!documentMap.containsKey(module)) {
            try {
                DocumentBuilder builder = documentBuilder();
                try (InputStream inputStream = new URI(uri).toURL().openStream()) {
                    xmlDocument = builder.parse(inputStream);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            documentMap.put(module, xmlDocument);
        } else {
            xmlDocument = documentMap.get(module);
        }

        return xmlDocument;
    }

    public String extractModuleName(String path) {
        int idx = path.indexOf("Model");
        path = (idx != -1) ? separatorsToWindows(path.substring(idx)) : path;
        return path.replace(".xsd", "");
    }

    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    public static String separatorsToWindows(String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }

    public DocumentBuilder documentBuilder() {
        return documentBuilder(true);
    }

    public DocumentBuilder documentBuilder(boolean namespaceAware) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(namespaceAware);
        try {
            return builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private class Concept {
        private String name;
        private String definition;
        private String related;

        public Concept(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getRelated() {
            return related;
        }

        public void setRelated(String related) {
            this.related = related;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Concept concept = (Concept) o;

            return name != null ? name.equals(concept.name) : concept.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("oagiskos:").append(name).append(" ").append("rdf:type skos:Concept").append(" ").append(";").append("\n");
            sb.append("  ").append("skos:prefLabel").append(" ").append("\"").append(name).append("\"").append("@en").append(" ").append(";").append("\n");
            if (!isEmpty(related)) {
                sb.append("  ").append("skos:related").append(" ").append("oagiskos:").append(related).append(" ").append(";").append("\n");
            }
            sb.append("  ").append("skos:definition").append(" ").append("\"\"\"").append(definition).append("\"\"\"").append("@en").append(" ").append(".").append("\n");

            return sb.toString();
        }
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private class SKOSContext {

        private Set<Concept> conceptSet = new HashSet();

        public void addConcept(Concept concept) {
            conceptSet.add(concept);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .").append("\n");
            sb.append("@prefix skos: <http://www.w3.org/2004/02/skos/core#> .").append("\n");
            sb.append("@prefix oagiskos: <http://www.oagi.org/skos#> .").append("\n");
            sb.append("\n");
            sb.append("oagiskos:OAGISConceptScheme rdf:type skos:ConceptScheme ;").append("\n");
            sb.append("  skos:hasTopConceptOf oagiskos:BOD ,").append("\n");
            sb.append("                       oagiskos:Verb ,").append("\n");
            sb.append("                       oagiskos:Noun ,").append("\n");
            sb.append("                       oagiskos:Other .").append("\n");
            sb.append("\n");
            sb.append("oagiskos:BOD rdf:type skos:Concept ;").append("\n");
            sb.append("  skos:topConceptOf oagiskos:OAGISConceptScheme .").append("\n");
            sb.append("oagiskos:Noun rdf:type skos:Concept ;").append("\n");
            sb.append("  skos:topConceptOf oagiskos:OAGISConceptScheme .").append("\n");
            sb.append("oagiskos:Verb rdf:type skos:Concept ;").append("\n");
            sb.append("  skos:topConceptOf oagiskos:OAGISConceptScheme .").append("\n");
            sb.append("oagiskos:Other rdf:type skos:Concept ;").append("\n");
            sb.append("  skos:topConceptOf oagiskos:OAGISConceptScheme .").append("\n");
            sb.append("\n");

            for (Concept concept : conceptSet) {
                sb.append(concept.toString());
                sb.append("\n");
            }

            return sb.toString();
        }
    }


    public SKOSContext generate(File schemaFile) throws Exception {
        SKOSContext skosContext = new SKOSContext();
        generate(skosContext, schemaFile);
        return skosContext;
    }

    public void generate(SKOSContext skosContext, File schemaFile) throws Exception {
        if (schemaFile.isDirectory()) {
            for (File child : schemaFile.listFiles()) {
                generate(skosContext, child);
            }
        } else {
            doGenerate(skosContext, schemaFile);
        }
    }

    private void doGenerate(SKOSContext skosContext, File schemaFile) throws Exception {
        if (!schemaFile.getName().endsWith(".xsd")) {
            return;
        }

        Document document = loadDocument(schemaFile);
        Element rootElement = document.getDocumentElement();
        doGenerate(skosContext, schemaFile, rootElement);
    }

    private void doGenerate(SKOSContext skosContext, File schemaFile, Element element) throws Exception {

        String filename = schemaFile.getName();
        String bodName = filename.substring(0, filename.length() - 4);

        Element target = (Element) xPath.evaluate("//xsd:element[@name='" + bodName + "']", element, XPathConstants.NODE);
        if (target != null) {
            String conceptName = target.getAttribute("name");
            String definition = (String) xPath.evaluate("//xsd:annotation/xsd:documentation/text()", target, XPathConstants.STRING);

            Concept concept = new Concept(conceptName);
            concept.setDefinition(definition);
            concept.setRelated("BOD");
            skosContext.addConcept(concept);
        }

    }

    public static void main(String[] args) throws Exception {
        SKOSGenerator generator = new SKOSGenerator();
        File baseDirectory = new File("./data/OAGIS_10_3_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_3/Model/BODs");
        SKOSContext skosContext = generator.generate(baseDirectory);
        System.out.println(skosContext);
    }
}
