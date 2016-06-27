package org.oagi.srt.export.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.export.model.SchemaModule;
import org.oagi.srt.export.model.SchemaModuleVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class XMLExportSchemaModuleVisitor implements SchemaModuleVisitor {

    private final File baseDir;

    private Document document;
    private Element rootElement;
    private File moduleFile;

    public XMLExportSchemaModuleVisitor(File baseDir) throws IOException {
        this.baseDir = baseDir.getCanonicalFile();
        FileUtils.forceMkdir(baseDir);
    }

    @Override
    public void startSchemaModule(SchemaModule schemaModule) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        document = documentBuilder.newDocument();

        Element schemaNode = document.createElementNS(SRTConstants.NS_XSD, "xsd:schema");
        schemaNode.setAttribute("xmlns:xsd", SRTConstants.NS_XSD);
        schemaNode.setAttribute("xmlns", SRTConstants.OAGI_NS);
        schemaNode.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
        schemaNode.setAttribute("targetNamespace", SRTConstants.OAGI_NS);
        schemaNode.setAttribute("elementFormDefault", "qualified");
        schemaNode.setAttribute("attributeFormDefault", "unqualified");

        document.appendChild(schemaNode);
        rootElement = schemaNode;

        moduleFile = new File(baseDir, schemaModule.getPath()).getCanonicalFile();
        FileUtils.forceMkdir(moduleFile.getParentFile());
    }


    @Override
    public void visitIncludeModule(SchemaModule includeSchemaModule) throws Exception {
        Element includeElement = document.createElement("xsd:include");
        String schemaLocation = getRelativeSchemaLocation(includeSchemaModule);
        includeElement.setAttribute("schemaLocation", schemaLocation);
        rootElement.appendChild(includeElement);
    }

    @Override
    public void visitImportModule(SchemaModule importSchemaModule) throws Exception {
        Element importElement = document.createElement("xsd:import");
        String schemaLocation = getRelativeSchemaLocation(importSchemaModule);
        importElement.setAttribute("schemaLocation", schemaLocation);
        rootElement.appendChild(importElement);
    }

    private String getRelativeSchemaLocation(SchemaModule schemaModule) throws IOException {
        File moduleFile = new File(baseDir, schemaModule.getPath());

        Path pathAbsolute = Paths.get(moduleFile.getCanonicalPath());
        Path pathBase = Paths.get(this.moduleFile.getParentFile().getCanonicalPath());
        Path pathRelative = pathBase.relativize(pathAbsolute);

        return FilenameUtils.separatorsToUnix(pathRelative.toString());
    }

    @Override
    public void endSchemaModule(SchemaModule schemaModule) throws Exception {
        System.out.println("<" + this.moduleFile.getCanonicalPath() + ">");

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);

        StreamResult result = new StreamResult(System.out);
        transformer.transform(source, result);

        System.out.println();
    }
}
