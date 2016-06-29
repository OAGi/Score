package org.oagi.srt.export.impl;

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
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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

    private final Namespace XSD_NS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private DataTypeRepository dtRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    public void setBaseDirectory(File baseDirectory) throws IOException {
        this.baseDir = baseDirectory.getCanonicalFile();
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
                bdtPriRestriRepository.findByBdtId(bdtSimpleType.getBdtId()).stream()
                        .filter(e -> e.getCodeListId() > 0).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }
        CodeList codeList = codeListRepository.findOne(bdtPriRestriList.get(0).getCodeListId());
        return codeList.getName();
    }

    public String getAgencyIdName(BDTSimpleType bdtSimpleType) {
        List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                bdtPriRestriRepository.findByBdtId(bdtSimpleType.getBdtId()).stream()
                        .filter(e -> e.getAgencyIdListId() > 0).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }

        AgencyIdList agencyIdList = agencyIdListRepository.findOne(bdtPriRestriList.get(0).getAgencyIdListId());
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
            extensionElement.setAttribute("base", name + "ContentType");
        } else {
            extensionElement.setAttribute("base", baseName);
        }

        List<BDTSC> dtScList;
        if (baseName.endsWith("CodeContentType")) {
            dtScList = bdtSimpleContent.getDtScList();
        } else {
            dtScList = new ArrayList();
            List<String> baseDtScGuidList = bdtSimpleContent.getBaseDtScList().stream()
                    .map(e -> e.getGuid()).collect(Collectors.toList());
            for (BDTSC dtSc : bdtSimpleContent.getDtScList()) {
                if (!baseDtScGuidList.contains(dtSc.getGuid())) {
                    dtScList.add(dtSc);
                }
            }
        }

        for (BDTSC dtSc : dtScList) {
            Element attributeElement = new Element("attribute", XSD_NS);

            String attrName = dtSc.getName();
            attributeElement.setAttribute("name", attrName);

            String typeName = getTypeName(dtSc);
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
        Element element = new Element("element", XSD_NS);
        element.setAttribute("name", bccp.getName());
        element.setAttribute("type", bccp.getTypeName());
        element.setAttribute("id", bccp.getGuid());

        rootElement.addContent(element);
    }

    @Override
    public void visitACCComplexType(ACCComplexType accComplexType) throws Exception {
        Element complexTypeElement = new Element("complexType", XSD_NS);
        complexTypeElement.setAttribute("name", accComplexType.getName());
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
            extensionElement.setAttribute("type", basedACC.getName() + "Type");
            complexContentElement.addContent(extensionElement);

            extensionElement.addContent(sequenceElement);
        } else {
            complexTypeElement.addContent(sequenceElement);
        }

        List<CoreComponent> coreComponents = coreComponentService.getCoreComponents(accComplexType.getRawId());
        for (CoreComponent coreComponent : coreComponents) {
            if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent bcc = (BasicCoreComponent) coreComponent;
                BasicCoreComponentProperty bccp = bccpRepository.findOne(bcc.getToBccpId());
                DataType bdt = dtRepository.findOne(bccp.getBdtId());

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

                    sequenceElement.addContent(attributeElement);
                } else {

                }
            } else if (coreComponent instanceof AssociationCoreComponent) {
                AssociationCoreComponent ascc = (AssociationCoreComponent) coreComponent;
                AssociationCoreComponentProperty asccp = asccpRepository.findOne(ascc.getToAsccpId());

            }
        }

        rootElement.addContent(complexTypeElement);
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

    }

    private String getTypeName(BDTSC dtSc) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList =
                bdtScPriRestriRepository.findByBdtScId(dtSc.getDtScId());

        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> codeListBdtScPriRestri =
                bdtScPriRestriList.stream()
                        .filter(e -> e.getCodeListId() > 0)
                        .collect(Collectors.toList());
        if (codeListBdtScPriRestri.size() > 1) {
            throw new IllegalStateException();
        }

        if (codeListBdtScPriRestri.isEmpty()) {
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> agencyIdBdtScPriRestri =
                    bdtScPriRestriList.stream()
                            .filter(e -> e.getAgencyIdListId() > 0)
                            .collect(Collectors.toList());
            if (agencyIdBdtScPriRestri.size() > 1) {
                throw new IllegalStateException();
            }

            if (agencyIdBdtScPriRestri.isEmpty()) {
                List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> defaultBdtScPriRestri =
                        bdtScPriRestriList.stream()
                                .filter(e -> e.isDefault())
                                .collect(Collectors.toList());
                if (defaultBdtScPriRestri.isEmpty() || defaultBdtScPriRestri.size() > 1) {
                    throw new IllegalStateException();
                }

                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                        cdtScAwdPriXpsTypeMapRepository.findOne(defaultBdtScPriRestri.get(0).getCdtScAwdPriXpsTypeMapId());
                XSDBuiltInType xbt = xbtRepository.findOne(cdtScAwdPriXpsTypeMap.getXbtId());
                return xbt.getBuiltInType();
            } else {
                AgencyIdList agencyIdList = agencyIdListRepository.findOne(agencyIdBdtScPriRestri.get(0).getAgencyIdListId());
                if ("oagis-id-f1df540ef0db48318f3a423b3057955f".equals(agencyIdList.getGuid())) {
                    return "clm63055D08B_AgencyIdentificationContentType";
                } else {
                    throw new IllegalStateException();
                }
            }
        } else {
            CodeList codeList = codeListRepository.findOne(codeListBdtScPriRestri.get(0).getCodeListId());
            return codeList.getName() + "ContentType";
        }
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

        System.out.println("<< " + this.moduleFile + " >>");

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(this.document, System.out);

        System.out.println();

//        FileUtils.forceMkdir(this.moduleFile.getParentFile());
//
//        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.moduleFile))) {
//            outputter.output(this.document, outputStream);
//            outputStream.flush();
//        }
    }
}
