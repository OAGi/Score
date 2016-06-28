package org.oagi.srt.export.impl;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.export.model.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class XMLExportSchemaModuleVisitor implements SchemaModuleVisitor {

    private final File baseDir;

    private Document document;
    private Element rootElement;
    private File moduleFile;

    private final Namespace XSD_NS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    public XMLExportSchemaModuleVisitor(File baseDir) throws IOException {
        this.baseDir = baseDir.getCanonicalFile();
    }

    @Override
    public void startSchemaModule(SchemaModule schemaModule) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        org.w3c.dom.Document document = documentBuilder.newDocument();

        DOMBuilder jdomBuilder = new DOMBuilder();
        this.document = jdomBuilder.build(document);

        Element schemaElement = new Element("schema", XSD_NS);
        schemaElement.addNamespaceDeclaration(Namespace.getNamespace("", SRTConstants.OAGI_NS));
        schemaElement.setAttribute("targetNamespace", SRTConstants.OAGI_NS);
        schemaElement.setAttribute("elementFormDefault", "qualified");
        schemaElement.setAttribute("attributeFormDefault", "unqualified");

        this.document.addContent(schemaElement);
        this.rootElement = schemaElement;

        moduleFile = new File(baseDir, schemaModule.getPath()).getCanonicalFile();
    }

    @Override
    public void visitIncludeModule(SchemaModule includeSchemaModule) throws Exception {
        Element includeElement = new Element("include", XSD_NS);
        String schemaLocation = getRelativeSchemaLocation(includeSchemaModule);
        includeElement.setAttribute("schemaLocation", schemaLocation);
        rootElement.addContent(includeElement);
    }

    @Override
    public void visitImportModule(SchemaModule importSchemaModule) throws Exception {
        Element importElement = new Element("import", XSD_NS);
        String schemaLocation = getRelativeSchemaLocation(importSchemaModule);
        importElement.setAttribute("schemaLocation", schemaLocation);
        rootElement.addContent(importElement);
    }

    @Override
    public void visitCodeList(SchemaCodeList schemaCodeList) throws Exception {
        String name = schemaCodeList.getName();
        if (schemaCodeList.getEnumTypeGuid() != null) {
            Element codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "EnumerationType");
            codeListElement.setAttribute("id", schemaCodeList.getEnumTypeGuid());

            addRestriction(codeListElement, schemaCodeList.getValues());
            rootElement.addContent(codeListElement);
        }

        Element codeListElement = new Element("simpleType", XSD_NS);
        codeListElement.setAttribute("name", name + "ContentType");
        codeListElement.setAttribute("id", schemaCodeList.getGuid());

        if (name.startsWith("clm")) {
            Collection<String> values = schemaCodeList.getValues();
            if (values.isEmpty()) {
                Element restrictionElement = new Element("restriction", XSD_NS);
                restrictionElement.setAttribute("base", "xsd:normalizedString");
                codeListElement.addContent(restrictionElement);
            } else {
                addRestriction(codeListElement, values);
            }
        } else {
            Element unionElement = new Element("union", XSD_NS);
            SchemaCodeList baseCodeList = schemaCodeList.getBaseCodeList();
            if (baseCodeList == null) {
                unionElement.setAttribute("memberTypes", name + "EnumerationType" + " xsd:token");
            } else {
                unionElement.setAttribute("memberTypes", baseCodeList.getName() + "ContentType" + " xsd:token");
            }
            codeListElement.addContent(unionElement);
        }

        rootElement.addContent(codeListElement);
    }

    @Override
    public void visitBDTSimpleType(BDTSimpleType bdtSimpleType) throws Exception {
        Element simpleTypeElement = new Element("simpleType", XSD_NS);
        String name = bdtSimpleType.getName();
        simpleTypeElement.setAttribute("name", name);
        simpleTypeElement.setAttribute("id", bdtSimpleType.getGuid());

        Element restrictionElement = new Element("restriction", XSD_NS);
        simpleTypeElement.addContent(restrictionElement);

        if ( (name.endsWith("CodeContentType") && !name.equals("CodeContentType")) ||
             (name.endsWith("IDContentType") && !name.equals("IDContentType")) ) {
            String baseName;
            if ((name.endsWith("CodeContentType"))) {
                baseName = bdtSimpleType.getCodeListName();
            } else {
                baseName = bdtSimpleType.getAgencyIdName();
            }

            restrictionElement.setAttribute("base", baseName + "ContentType");
        } else {
            restrictionElement.setAttribute("base", bdtSimpleType.getBaseDTName());
        }

        rootElement.addContent(simpleTypeElement);
    }

    @Override
    public void visitBDTSimpleContent(BDTSimpleContent bdtSimpleContent) throws Exception {
        Element complexTypeElement = new Element("complexType", XSD_NS);
        complexTypeElement.setAttribute("name", bdtSimpleContent.getName());
        complexTypeElement.setAttribute("id", bdtSimpleContent.getGuid());

        Element simpleContentElement = new Element("simpleContent", XSD_NS);
        complexTypeElement.addContent(simpleContentElement);

        Element extensionElement = new Element("extension", XSD_NS);
        simpleContentElement.addContent(extensionElement);

        rootElement.addContent(complexTypeElement);
    }

    private void addRestriction(Element codeListElement, Collection<String> values) {
        Element restrictionElement = new Element("restriction", XSD_NS);
        restrictionElement.setAttribute("base", "xsd:token");
        codeListElement.addContent(restrictionElement);

        for (String value : values) {
            Element enumerationElement = new Element("enumeration", XSD_NS);
            enumerationElement.setAttribute("value", value);
            restrictionElement.addContent(enumerationElement);
        }
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
        if (this.moduleFile.getName().endsWith("Fields.xsd")) {
            System.out.println("<" + this.moduleFile.getCanonicalPath() + ">");

            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(this.document, System.out);

            System.out.println();
        }
    }
}
