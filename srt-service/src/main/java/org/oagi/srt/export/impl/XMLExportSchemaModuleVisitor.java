package org.oagi.srt.export.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.export.model.*;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
public class XMLExportSchemaModuleVisitor implements SchemaModuleVisitor {

    private File baseDir;

    private Document document;
    private Element rootElement;
    private File moduleFile;

    private final Namespace OAGI_NS = Namespace.getNamespace("", SRTConstants.OAGI_NS);
    private final Namespace XSD_NS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private ImportedDataProvider importedDataProvider;

    public void setBaseDirectory(File baseDirectory) throws IOException {
        this.baseDir = baseDirectory.getCanonicalFile();
    }

    @Override
    public void startSchemaModule(SchemaModule schemaModule) throws Exception {
        this.document = createDocument();

        Element schemaElement = new Element("schema", XSD_NS);
        schemaElement.addNamespaceDeclaration(OAGI_NS);
        schemaElement.setAttribute("targetNamespace", SRTConstants.OAGI_NS);
        schemaElement.setAttribute("elementFormDefault", "qualified");
        schemaElement.setAttribute("attributeFormDefault", "unqualified");

        this.document.addContent(schemaElement);
        this.rootElement = schemaElement;

        moduleFile = new File(baseDir, schemaModule.getPath()).getCanonicalFile();
    }

    private Document createDocument() {
        return createDocument(null);
    }

    private Document createDocument(byte[] content) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);

        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
        org.w3c.dom.Document document;
        if (content == null) {
            document = documentBuilder.newDocument();
        } else {
            try (InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(content))) {
                document = documentBuilder.parse(inputStream);
            } catch (IOException e) {
                throw new IllegalArgumentException("I/O error", e);
            } catch (SAXException e) {
                throw new IllegalArgumentException("Invalid XML content", e);
            }
        }

        DOMBuilder jdomBuilder = new DOMBuilder();
        return jdomBuilder.build(document);
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
    public void visitAgencyId(AgencyId agencyId) throws Exception {
        // ContentType part
        Element simpleTypeElement = new Element("simpleType", XSD_NS);
        rootElement.addContent(simpleTypeElement);

        simpleTypeElement.setAttribute("name", agencyId.getTypeName());
        simpleTypeElement.setAttribute("id", agencyId.getGuid());

        Element unionElement = new Element("union", XSD_NS);
        unionElement.setAttribute("memberTypes", "xsd:token " + agencyId.getName() + "ContentEnumerationType");
        simpleTypeElement.addContent(unionElement);

        // ContentEnumerationType part
        Element enumerationTypeElement = new Element("simpleType", XSD_NS);
        rootElement.addContent(enumerationTypeElement);

        enumerationTypeElement.setAttribute("name", agencyId.getName() + "ContentEnumerationType");
        enumerationTypeElement.setAttribute("id", agencyId.getEnumGuid());

        Element restrictionElement = new Element("restriction", XSD_NS);
        enumerationTypeElement.addContent(restrictionElement);

        restrictionElement.setAttribute("base", "xsd:token");

        Element minLengthElement = new Element("minLength", XSD_NS);
        restrictionElement.addContent(minLengthElement);

        minLengthElement.setAttribute("value", "" + agencyId.getMinLengthOfValues());

        Element maxLengthElement = new Element("maxLength", XSD_NS);
        restrictionElement.addContent(maxLengthElement);

        maxLengthElement.setAttribute("value", "" + agencyId.getMaxLengthOfValues());

        for (AgencyIdValue value : agencyId.getValues()) {
            Element enumerationElement = new Element("enumeration", XSD_NS);
            restrictionElement.addContent(enumerationElement);

            enumerationElement.setAttribute("value", value.getValue());

            Element annotationElement = new Element("annotation", XSD_NS);
            enumerationElement.addContent(annotationElement);

            Element documentationElement = new Element("documentation", XSD_NS);
            annotationElement.addContent(documentationElement);

            documentationElement.setAttribute("source", SRTConstants.OAGI_NS);

            Element cctsNameElement = new Element("ccts_Name", OAGI_NS);
            documentationElement.addContent(cctsNameElement);

            cctsNameElement.setText(value.getName());

            Element cctsDefinitionElement = new Element("ccts_Name", OAGI_NS);
            documentationElement.addContent(cctsDefinitionElement);

            cctsDefinitionElement.setText(value.getDefinition());
        }
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
                baseName = getCodeListName(bdtSimpleType);
            } else {
                baseName = getAgencyIdName(bdtSimpleType);
            }

            restrictionElement.setAttribute("base", baseName + "ContentType");
        } else {
            restrictionElement.setAttribute("base", bdtSimpleType.getBaseDTName());
        }

        rootElement.addContent(simpleTypeElement);
    }

    private String getCodeListName(BDTSimpleType bdtSimpleType) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                importedDataProvider.findBdtPriRestriListByDtId(bdtSimpleType.getBdtId()).stream()
                        .filter(e -> e.getCodeListId() > 0).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }
        CodeList codeList = importedDataProvider.findCodeList(bdtPriRestriList.get(0).getCodeListId());
        return codeList.getName();
    }

    public String getAgencyIdName(BDTSimpleType bdtSimpleType) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                importedDataProvider.findBdtPriRestriListByDtId(bdtSimpleType.getBdtId()).stream()
                        .filter(e -> e.getAgencyIdListId() > 0).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }

        AgencyIdList agencyIdList = importedDataProvider.findAgencyIdList(bdtPriRestriList.get(0).getAgencyIdListId());
        if ("oagis-id-f1df540ef0db48318f3a423b3057955f".equals(agencyIdList.getGuid())) {
            return "clm63055D08B_AgencyIdentification";
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void visitBDTSimpleContent(BDTSimpleContent bdtSimpleContent) throws Exception {
        Element complexTypeElement = new Element("complexType", XSD_NS);
        complexTypeElement.setAttribute("name", bdtSimpleContent.getName());
        complexTypeElement.setAttribute("id", bdtSimpleContent.getGuid());

        Element simpleContentElement = new Element("simpleContent", XSD_NS);
        complexTypeElement.addContent(simpleContentElement);

        Element extensionElement = new Element("extension", XSD_NS);

        String baseName = bdtSimpleContent.getBaseDTName();
        String name = bdtSimpleContent.getName();

        if ("CodeType".equals(baseName) && !"OpenCodeType".equals(name)) {
            baseName = name + "ContentType";
        }
        // b/c of 'CodeType'
        if (baseName.endsWith("CodeTypeContentType")) {
            baseName = baseName.replaceAll("CodeTypeContentType", "CodeContentType");
        }
        extensionElement.setAttribute("base", baseName);

        List<BDTSC> dtScList;
        if (baseName.endsWith("CodeContentType")) {
            dtScList = bdtSimpleContent.getDtScList();
        } else {
            dtScList = new ArrayList();
            for (BDTSC dtSc : bdtSimpleContent.getDtScList()) {
                if (!dtSc.hasBasedBDTSC()) {
                    dtScList.add(dtSc);
                }
            }
        }

        for (BDTSC dtSc : dtScList) {
            Element attributeElement = new Element("attribute", XSD_NS);

            String attrName = dtSc.getName();
            attributeElement.setAttribute("name", attrName);

            String typeName = dtSc.getTypeName();
            attributeElement.setAttribute("type", typeName);

            int useInt = dtSc.getMinCardinality() * 2 + dtSc.getMaxCardinality();
            String useVal = getUseAttributeValue(useInt);
            if (useVal != null) {
                attributeElement.setAttribute("use", useVal);
            }

            attributeElement.setAttribute("id", dtSc.getGuid());

            extensionElement.addContent(attributeElement);
        }

        simpleContentElement.addContent(extensionElement);

        rootElement.addContent(complexTypeElement);
    }

    @Override
    public void visitBCCP(BCCP bccp) throws Exception {
        addSimpleElement(bccp);
    }

    private Element addSimpleElement(org.oagi.srt.export.model.Component component) {
        Element element = new Element("element", XSD_NS);

        element.setAttribute("name", component.getName());
        element.setAttribute("type", component.getTypeName());
        element.setAttribute("id", component.getGuid());

        rootElement.addContent(element);

        return element;
    }

    @Override
    public void visitACCComplexType(ACCComplexType accComplexType) throws Exception {
        Element complexTypeElement;
        if (accComplexType.isGroup()) {
            complexTypeElement = new Element("group", XSD_NS);
            complexTypeElement.setAttribute("name", accComplexType.getName());
        } else {
            complexTypeElement = new Element("complexType", XSD_NS);
            complexTypeElement.setAttribute("name", accComplexType.getName() + "Type");
        }

        if (accComplexType.isAbstract()) {
            complexTypeElement.setAttribute("abstract", "true");
        }
        complexTypeElement.setAttribute("id", accComplexType.getGuid());

        Element sequenceElement = new Element("sequence", XSD_NS);
        ACC basedACC = accComplexType.getBasedACC();
        if (basedACC != null) {
            Element complexContentElement = new Element("complexContent", XSD_NS);
            complexTypeElement.addContent(complexContentElement);

            Element extensionElement = new Element("extension", XSD_NS);
            extensionElement.setAttribute("base", basedACC.getTypeName());
            complexContentElement.addContent(extensionElement);

            extensionElement.addContent(sequenceElement);
        } else {
            complexTypeElement.addContent(sequenceElement);
        }

        List<CoreComponent> coreComponents = coreComponentService.getCoreComponents(accComplexType.getRawId());
        // for ASCC or BCC (Sequence Key != 0)
        for (CoreComponent coreComponent : coreComponents) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent bcc = (BasicCoreComponent) coreComponent;
                BasicCoreComponentProperty bccp = importedDataProvider.findBCCP(bcc.getToBccpId());

                if (bcc.getSeqKey() > 0) {
                    Element element = new Element("element", XSD_NS);

                    element.setAttribute("ref", Utility.toCamelCase(bccp.getPropertyTerm()));
                    element.setAttribute("id", bcc.getGuid());
                    setCardinalities(element, bcc);

                    sequenceElement.addContent(element);
                }
            } else if (coreComponent instanceof AssociationCoreComponent) {
                AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
                ASCCP asccp = ASCCP.newInstance(importedDataProvider.findASCCP(ascc.getToAsccpId()), importedDataProvider);

                if (asccp.isGroup()) {
                    Element groupElement = new Element("group", XSD_NS);

                    groupElement.setAttribute("ref", asccp.getName());
                    groupElement.setAttribute("id", ascc.getGuid());
                    setCardinalities(groupElement, ascc);

                    sequenceElement.addContent(groupElement);
                } else {
                    Element element = new Element("element", XSD_NS);

                    if (asccp.isReusableIndicator()) {
                        element.setAttribute("ref", asccp.getName());
                    } else {
                        element.setAttribute("name", asccp.getName());
                        element.setAttribute("type", asccp.getTypeName());
                    }

                    element.setAttribute("id", ascc.getGuid());
                    setCardinalities(element, ascc);

                    sequenceElement.addContent(element);
                }
            }
        }

        // for BCCP (Sequence Key == 0)
        for (CoreComponent coreComponent : coreComponents) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent bcc = (BasicCoreComponent) coreComponent;
                BasicCoreComponentProperty bccp = importedDataProvider.findBCCP(bcc.getToBccpId());
                DataType bdt = importedDataProvider.findDT(bccp.getBdtId());

                if (bcc.getSeqKey() == 0) {
                    Element attributeElement = new Element("attribute", XSD_NS);

                    attributeElement.setAttribute("name", Utility.toLowerCamelCase(bccp.getPropertyTerm()));
                    attributeElement.setAttribute("type", Utility.denToName(bdt.getDen()));

                    int useInt = bcc.getCardinalityMin() * 2 + bcc.getCardinalityMax();
                    String useVal = getUseAttributeValue(useInt);
                    if (useVal != null) {
                        attributeElement.setAttribute("use", useVal);
                    }

                    attributeElement.setAttribute("id", bcc.getGuid());

                    if (basedACC != null) {
                        Element complexContentElement = complexTypeElement.getChild("complexContent", XSD_NS);
                        Element extensionElement = complexContentElement.getChild("extension", XSD_NS);

                        extensionElement.addContent(attributeElement);
                    } else {
                        complexTypeElement.addContent(attributeElement);
                    }
                }
            }
        }

        rootElement.addContent(complexTypeElement);
    }

    private void setCardinalities(Element element, CoreComponent ascc) {
        element.setAttribute("minOccurs", Integer.toString(ascc.getCardinalityMin()));
        switch (ascc.getCardinalityMax()) {
            case -1:
                element.setAttribute("maxOccurs", "unbounded");
                break;
            default:
                element.setAttribute("maxOccurs", Integer.toString(ascc.getCardinalityMax()));
                break;
        }
    }

    private String getUseAttributeValue(int useInt) {
        switch (useInt) {
            case 0:
                return "prohibited";
            case 3:
                return "required";
            case 2:
                throw new IllegalStateException();
        }
        return null;
    }

    @Override
    public void visitACCGroup(ACCGroup accGroup) throws Exception {
        // not implemented yet
    }

    @Override
    public void visitASCCPComplexType(ASCCPComplexType asccpComplexType) throws Exception {
        /*
         * <xsd:group> element doesn't need to be created here.
         * it already created on #visitACCComplexType(ACCComplexType)
         */
        if (asccpComplexType.isGroup()) {
            return;
        }
        addSimpleElement(asccpComplexType);
    }

    @Override
    public void visitASCCPGroup(ASCCPGroup asccpGroup) throws Exception {
        // not implemented yet
    }

    @Override
    public void visitBlobContent(byte[] content) throws Exception {
        this.document = createDocument(content);
        this.rootElement = this.document.getRootElement();
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
        if (this.rootElement.getContent().isEmpty()) {
            return;
        }

        FileUtils.forceMkdir(this.moduleFile.getParentFile());

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.moduleFile))) {
            outputter.output(this.document, outputStream);
            outputStream.flush();
        }
    }
}
