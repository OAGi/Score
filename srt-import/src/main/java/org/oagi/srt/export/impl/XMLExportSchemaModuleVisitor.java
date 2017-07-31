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
import org.oagi.srt.persistence.populate.helper.Context;
import org.oagi.srt.provider.CoreComponentProvider;
import org.oagi.srt.provider.ImportedDataProvider;
import org.oagi.srt.repository.entity.*;
import org.oagi.srt.service.CoreComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.oagi.srt.repository.entity.OagisComponentType.Extension;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
@Component
public class XMLExportSchemaModuleVisitor implements SchemaModuleVisitor {

    private SchemaModule schemaModule;
    private File baseDir;

    private Document document;
    private Element rootElement;
    private File moduleFile;

    private final Namespace OAGI_NS = Namespace.getNamespace("", SRTConstants.OAGI_NS);
    private final Namespace XSD_NS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    @Autowired
    @Qualifier("defaultCoreComponentProvider")
    private CoreComponentProvider coreComponentProvider;

    @Autowired
    private CoreComponentService coreComponentService;

    @Autowired
    private ImportedDataProvider importedDataProvider;

    public void setBaseDirectory(File baseDirectory) throws IOException {
        this.baseDir = baseDirectory.getCanonicalFile();
    }

    @Override
    public void startSchemaModule(SchemaModule schemaModule) throws Exception {
        this.schemaModule = schemaModule;
        this.document = createDocument();

        Element schemaElement = new Element("schema", XSD_NS);
        schemaElement.addNamespaceDeclaration(OAGI_NS);
        schemaElement.setAttribute("targetNamespace", SRTConstants.OAGI_NS);
        schemaElement.setAttribute("elementFormDefault", "qualified");
        schemaElement.setAttribute("attributeFormDefault", "unqualified");
        String versionNum = schemaModule.getVersionNum();
        if (!StringUtils.isEmpty(versionNum)) {
            schemaElement.setAttribute("version", versionNum);
        }

        this.document.addContent(schemaElement);
        this.rootElement = schemaElement;

        moduleFile = new File(baseDir, schemaModule.getPath() + ".xsd").getCanonicalFile();
    }

    private Document createDocument() {
        return createDocument(null);
    }

    private Document createDocument(byte[] content) {
        return createDocument(content, true);
    }

    private Document createDocument(byte[] content, boolean namespaceAware) {
        DocumentBuilder documentBuilder = Context.documentBuilder(namespaceAware);
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

            Element cctsDefinitionElement = new Element("ccts_Definition", OAGI_NS);
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

            codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "ContentType");
            codeListElement.setAttribute("id", schemaCodeList.getGuid());
            Element unionElement = new Element("union", XSD_NS);
            SchemaCodeList baseCodeList = schemaCodeList.getBaseCodeList();
            if (baseCodeList == null) {
                unionElement.setAttribute("memberTypes", name + "EnumerationType" + " xsd:token");
            } else {
                unionElement.setAttribute("memberTypes", baseCodeList.getName() + "ContentType" + " xsd:token");
            }
            codeListElement.addContent(unionElement);
            rootElement.addContent(codeListElement);

        } else {
            Element codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "ContentType");
            codeListElement.setAttribute("id", schemaCodeList.getGuid());
            Collection<String> values = schemaCodeList.getValues();
            if (values.isEmpty()) {
                Element restrictionElement = new Element("restriction", XSD_NS);
                restrictionElement.setAttribute("base", "xsd:normalizedString");
                codeListElement.addContent(restrictionElement);
            } else {
                addRestriction(codeListElement, values);
            }
            rootElement.addContent(codeListElement);
        }
    }

    @Override
    public void visitXBTSimpleType(XBTSimpleType xbtSimpleType) throws Exception {
        Element simpleTypeElement = new Element("simpleType", XSD_NS);
        String name = xbtSimpleType.getName();
        simpleTypeElement.setAttribute("name", name);
        simpleTypeElement.setAttribute("id", xbtSimpleType.getGuid());

        String schemaDefinition = xbtSimpleType.getSchemaDefinition();
        schemaDefinition =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
                schemaDefinition +
                "</xsd:schema>";

        Document document = createDocument(schemaDefinition.getBytes());
        for (Element child : document.getRootElement().getChildren()) {
            child = child.clone();
            if ("restriction".equals(child.getName())) {
                if (!Arrays.asList("xbt_DayOfWeekType", "xbt_DayOfYearType", "xbt_BooleanType").contains(name)) {
                    child.removeContent();
                }
            }
            simpleTypeElement.addContent(child);
        }

        rootElement.addContent(simpleTypeElement);
    }

    @Override
    public void visitBDTSimpleType(BDTSimpleType bdtSimpleType) throws Exception {
        Element simpleTypeElement = new Element("simpleType", XSD_NS);
        String name = bdtSimpleType.getName();
        simpleTypeElement.setAttribute("name", name);
        simpleTypeElement.setAttribute("id", bdtSimpleType.getGuid());

        if (bdtSimpleType.isDefaultBDT()) {
            setDocumentation(simpleTypeElement, bdtSimpleType);
        }
        setRestriction(simpleTypeElement, bdtSimpleType);

        rootElement.addContent(simpleTypeElement);
    }

    private void setDocumentation(Element typeElement, BDTSimple bdtSimple) {
        DataType dataType = bdtSimple.getDataType();

        Element annotationElement = new Element("annotation", XSD_NS);
        typeElement.addContent(annotationElement);

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        documentationElement.setAttribute("lang", "en", Namespace.XML_NAMESPACE);

        String den = dataType.getDen();

        if (bdtSimple.isDefaultBDT()) {
            Element ccts_UniqueID = new Element("ccts_UniqueID", OAGI_NS);
            documentationElement.addContent(ccts_UniqueID);
            String uniqueId = den.substring(den.indexOf('_') + 1, den.indexOf(". Type"));
            ccts_UniqueID.setText(uniqueId);

            Element ccts_VersionID = new Element("ccts_VersionID", OAGI_NS);
            documentationElement.addContent(ccts_VersionID);
            ccts_VersionID.setText("1.0");

            Element ccts_DictionaryEntryName = new Element("ccts_DictionaryEntryName", OAGI_NS);
            documentationElement.addContent(ccts_DictionaryEntryName);
            String dictionaryEntryName = den.replaceAll("_" + uniqueId, "");
            ccts_DictionaryEntryName.setText(dictionaryEntryName);
        } else {
            Element ccts_UniqueID = new Element("ccts_UniqueID", OAGI_NS);
            documentationElement.addContent(ccts_UniqueID);
            ccts_UniqueID.setText("BDT000000");

            Element ccts_CategoryCode = new Element("ccts_CategoryCode", OAGI_NS);
            documentationElement.addContent(ccts_CategoryCode);
            ccts_CategoryCode.setText("BDT");

            Element ccts_DictionaryEntryName = new Element("ccts_DictionaryEntryName", OAGI_NS);
            documentationElement.addContent(ccts_DictionaryEntryName);
            ccts_DictionaryEntryName.setText(den);

            Element ccts_VersionID = new Element("ccts_VersionID", OAGI_NS);
            documentationElement.addContent(ccts_VersionID);
            ccts_VersionID.setText("1.0");
        }

        Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
        documentationElement.addContent(ccts_Definition);
        String definition = dataType.getDefinition();
        ccts_Definition.setText(definition);

        Element ccts_DataTypeTermName = new Element("ccts_DataTypeTermName", OAGI_NS);
        documentationElement.addContent(ccts_DataTypeTermName);
        String dataTypeTerm = dataType.getDataTypeTerm();
        ccts_DataTypeTermName.setText(dataTypeTerm);

        String qualifier = dataType.getQualifier();
        if (!StringUtils.isEmpty(qualifier)) {
            Element ccts_QualifierTerm = new Element("ccts_QualifierTerm", OAGI_NS);
            documentationElement.addContent(ccts_QualifierTerm);
            ccts_QualifierTerm.setText(qualifier);
        }
    }

    private void setRestriction(Element simpleTypeElement, BDTSimpleType bdtSimpleType) {
        Element targetElement;
        if (bdtSimpleType.isTimepointCDT() && bdtSimpleType.isBaseDT_CDT() && bdtSimpleType.count_BDT_PRI_RESTRI() > 1) {
            Element unionElement = new Element("union", XSD_NS);
            simpleTypeElement.addContent(unionElement);

            String memberTypes = String.join(" ",
                    bdtSimpleType.getXbtBuiltInTypes().stream()
                            .filter(e -> !"xsd:token".equals(e))
                            .collect(Collectors.toList())
            );
            unionElement.setAttribute("memberTypes", memberTypes);
            simpleTypeElement.setAttribute("final", "union");

            targetElement = unionElement;
        } else {
            Element restrictionElement = new Element("restriction", XSD_NS);
            simpleTypeElement.addContent(restrictionElement);
            String name = bdtSimpleType.getName();

            if ( (name.endsWith("CodeContentType") && !name.equals("CodeContentType")) ||
                    (name.endsWith("IDContentType") && !name.equals("IDContentType")) ) {
                String baseName;
                if (name.endsWith("CodeContentType")) {
                    baseName = getCodeListName(bdtSimpleType);
                } else {
                    baseName = getAgencyIdName(bdtSimpleType);
                }

                restrictionElement.setAttribute("base", baseName + "ContentType");
            } else {
                if (bdtSimpleType.isDefaultBDT()) {
                    restrictionElement.setAttribute("base", bdtSimpleType.getXbtName());
                } else {
                    restrictionElement.setAttribute("base", bdtSimpleType.getBaseDTName());
                }
            }

            targetElement = restrictionElement;
        }

        setDocumentationForRestrictionOrUnionOrExtension(targetElement, bdtSimpleType);
    }

    private void setDocumentationForRestrictionOrUnionOrExtension(Element extensionElement, BDTSimple bdtSimple) {
        DataType dataType = bdtSimple.getDataType();
        String definition = dataType.getContentComponentDefinition();

        Element annotationElement = new Element("annotation", XSD_NS);
        extensionElement.addContent(annotationElement);

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        String definitionSource = dataType.getDefinitionSource();
        if (!StringUtils.isEmpty(definitionSource)) {
            documentationElement.setAttribute("source", definitionSource);
        }

        Element ccts_ContentComponentValueDomain = new Element("ccts_ContentComponentValueDomain", OAGI_NS);
        documentationElement.addContent(ccts_ContentComponentValueDomain);

        Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
        ccts_ContentComponentValueDomain.addContent(ccts_Definition);
        ccts_Definition.setText(definition);

        CoreDataTypeAllowedPrimitive cdtAwdPri = bdtSimple.getDefaultCdtAwdPri();
        Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
        ccts_ContentComponentValueDomain.addContent(ccts_DefaultIndicator);
        ccts_DefaultIndicator.setText((cdtAwdPri.isDefault()) ? "True" : "False");

        Element ccts_PrimitiveTypeName = new Element("ccts_PrimitiveTypeName", OAGI_NS);
        ccts_ContentComponentValueDomain.addContent(ccts_PrimitiveTypeName);
        String primitiveTypeName = bdtSimple.getCdtPriName();
        ccts_PrimitiveTypeName.setText(primitiveTypeName);

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
        return agencyIdList.getName();
    }

    @Override
    public void visitBDTSimpleContent(BDTSimpleContent bdtSimpleContent) throws Exception {
        Element complexTypeElement = new Element("complexType", XSD_NS);
        complexTypeElement.setAttribute("name", bdtSimpleContent.getName());
        complexTypeElement.setAttribute("id", bdtSimpleContent.getGuid());

        setDocumentation(complexTypeElement, bdtSimpleContent);
        setSimpleContent(complexTypeElement, bdtSimpleContent);

        rootElement.addContent(complexTypeElement);
    }

    private void setSimpleContent(Element complexTypeElement, BDTSimpleContent bdtSimpleContent) {
        Element simpleContentElement = new Element("simpleContent", XSD_NS);
        complexTypeElement.addContent(simpleContentElement);

        Element extensionElement = new Element("extension", XSD_NS);
        setDocumentationForRestrictionOrUnionOrExtension(extensionElement, bdtSimpleContent);

        String baseName = bdtSimpleContent.getBaseDTName();
        String name = bdtSimpleContent.getName();
        if (bdtSimpleContent.isDefaultBDT()) {
            String xbtName = bdtSimpleContent.getXbtName();
            extensionElement.setAttribute("base", xbtName);
        } else {
            extensionElement.setAttribute("base", baseName);
        }

        List<BDTSC> dtScList;
        if (baseName.endsWith("CodeContentType")) {
            dtScList = bdtSimpleContent.getDtScList();
        } else {
            dtScList = new ArrayList();
            for (BDTSC dtSc : bdtSimpleContent.getDtScList()) {
                if (bdtSimpleContent.isDefaultBDT() || !dtSc.hasBasedBDTSC()) {
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

            String useVal;
            if (bdtSimpleContent.isDefaultBDT() && "timeZoneCode".equals(name)) {
                useVal = "required";
            } else {
                int useInt = dtSc.getMinCardinality() * 2 + dtSc.getMaxCardinality();
                useVal = getUseAttributeValue(useInt);
            }
            if (bdtSimpleContent.isDefaultBDT() && useVal == null) {
                useVal = "optional";
            }

            if (useVal != null) {
                attributeElement.setAttribute("use", useVal);
            }

            attributeElement.setAttribute("id", dtSc.getGuid());

            if (!schemaModule.getPath().contains("Meta")) {
                setDocumentationForBdtSc(attributeElement, bdtSimpleContent, dtSc);
            }

            extensionElement.addContent(attributeElement);
        }

        simpleContentElement.addContent(extensionElement);
    }

    private void setDocumentationForBdtSc(Element attributeElement, BDTSimple bdtSimple, BDTSC dtSc) {
        Element annotationElement = new Element("annotation", XSD_NS);
        attributeElement.addContent(annotationElement);

        DataTypeSupplementaryComponent bdtSc = dtSc.getBdtSc();

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        documentationElement.setAttribute("lang", "en", Namespace.XML_NAMESPACE);

        String definitionSource = bdtSc.getDefinitionSource();
        if (!StringUtils.isEmpty(definitionSource)) {
            documentationElement.setAttribute("source", definitionSource);
        }

        Element ccts_Cardinality = new Element("ccts_Cardinality", OAGI_NS);
        documentationElement.addContent(ccts_Cardinality);

        if (dtSc.getCodeList() != null && dtSc.getCodeList().getName().equals("clm6DateTimeFormatCode1_DateTimeFormatCode")) {
            ccts_Cardinality.setText("1..1");
        } else {
            ccts_Cardinality.setText(bdtSc.getCardinalityMin() + ".." + bdtSc.getCardinalityMax());
        }

        Element ccts_DictionaryEntryName = new Element("ccts_DictionaryEntryName", OAGI_NS);
        documentationElement.addContent(ccts_DictionaryEntryName);

        String dataTypeTerm = bdtSimple.getDataType().getDataTypeTerm();
        String propertyTerm = bdtSc.getPropertyTerm();
        String representationTerm = bdtSc.getRepresentationTerm();
        String dictionaryEntryName = dataTypeTerm  + ". " + propertyTerm + ". " + representationTerm;
        ccts_DictionaryEntryName.setText(dictionaryEntryName);

        Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
        documentationElement.addContent(ccts_Definition);
        String definition = bdtSc.getDefinition();
        ccts_Definition.setText(definition);

        Element ccts_PropertyTermName = new Element("ccts_PropertyTermName", OAGI_NS);
        documentationElement.addContent(ccts_PropertyTermName);
        ccts_PropertyTermName.setText(propertyTerm);

        Element ccts_RepresentationTermName = new Element("ccts_RepresentationTermName", OAGI_NS);
        documentationElement.addContent(ccts_RepresentationTermName);
        ccts_RepresentationTermName.setText(representationTerm);

        Element ccts_DataTypeTermName = new Element("ccts_DataTypeTermName", OAGI_NS);
        documentationElement.addContent(ccts_DataTypeTermName);
        ccts_DataTypeTermName.setText(dataTypeTerm);

        Element ccts_SupplementaryComponentValueDomain = new Element("ccts_SupplementaryComponentValueDomain", OAGI_NS);
        documentationElement.addContent(ccts_SupplementaryComponentValueDomain);

        XSDBuiltInType xbt = dtSc.getXbt();
        if (xbt != null) {
            Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_DefaultIndicator);
            CoreDataTypeSupplementaryComponentAllowedPrimitive cdtScAwdPri = dtSc.getCdtScAwdPri();
            ccts_DefaultIndicator.setText((cdtScAwdPri.isDefault()) ? "True" : "False");

            Element ccts_PrimitiveTypeName = new Element("ccts_PrimitiveTypeName", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_PrimitiveTypeName);
            CoreDataTypePrimitive cdtPri = dtSc.getCdtPri();
            ccts_PrimitiveTypeName.setText(cdtPri.getName());
        } else {
            AgencyIdList agencyIdList = dtSc.getAgencyIdList();
            CodeList codeList = dtSc.getCodeList();

            Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_DefaultIndicator);
            ccts_DefaultIndicator.setText("True");

            String schemeOrListID = null;
            String schemeOrListVersionID = null;
            String schemeOrListAgencyID = null;
            String schemeOrListModificationAllowedIndicator = null;

            if (agencyIdList != null) {
                String name = agencyIdList.getName();
                name = name.substring("clm".length(), name.length());

                schemeOrListAgencyID = "" + name.charAt(0);
                name = name.substring(1);

                schemeOrListID = name.substring(0, name.indexOf('D'));

                name = name.substring(0, name.indexOf('_'));
                schemeOrListVersionID = name.substring(schemeOrListID.length(), name.length());

                schemeOrListModificationAllowedIndicator = "False";
            } else if (codeList != null) {
                String name = codeList.getName();

                while (name.startsWith("oacl")) {
                    codeList = importedDataProvider.findCodeList(codeList.getBasedCodeListId());
                    if (codeList == null) {
                        break;
                    }
                    name = codeList.getName();
                }

                if (!name.startsWith("clm")) {
                    return;
                }

                name = name.substring("clm".length(), name.length());

                if (Character.isDigit(name.charAt(0))) {
                    schemeOrListAgencyID = "" + name.charAt(0);
                    name = name.substring(1);
                } else {
                    schemeOrListAgencyID = "IANA";
                    name = name.substring(4);
                }

                name = name.substring(0, name.indexOf('_'));

                String lastEightChars = name.substring(name.length() - 8, name.length());
                if (lastEightChars.matches("[0-9]{8}")) {
                    schemeOrListVersionID =
                            lastEightChars.substring(0, 4) + "-" +
                            lastEightChars.substring(4, 6) + "-" +
                            lastEightChars.substring(6, 8);
                    name = name.substring(0, name.length() - 8);
                } else {
                    schemeOrListVersionID = "" + name.charAt(name.length() - 1);
                    name = name.substring(0, name.length() - 1);
                }

                schemeOrListID = name;

                switch (schemeOrListID) {
                    case "CharacterSetCode":
                    case "MIMEMediaType":
                        schemeOrListModificationAllowedIndicator = "True";
                        break;
                    case "42173A":
                        if (bdtSimple.getName().startsWith("Rate")) {
                            schemeOrListModificationAllowedIndicator = "False";
                        } else {
                            schemeOrListModificationAllowedIndicator = "True";
                        }
                        break;
                    default:
                        schemeOrListModificationAllowedIndicator = "False";
                }
            }

            Element ccts_SchemeOrListID = new Element("ccts_SchemeOrListID", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_SchemeOrListID);
            ccts_SchemeOrListID.setText(schemeOrListID);

            Element ccts_SchemeOrListVersionID = new Element("ccts_SchemeOrListVersionID", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_SchemeOrListVersionID);
            ccts_SchemeOrListVersionID.setText(schemeOrListVersionID);

            Element ccts_SchemeOrListAgencyID = new Element("ccts_SchemeOrListAgencyID", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_SchemeOrListAgencyID);
            ccts_SchemeOrListAgencyID.setText(schemeOrListAgencyID);

            Element ccts_SchemeOrListModificationAllowedIndicator = new Element("ccts_SchemeOrListModificationAllowedIndicator", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_SchemeOrListModificationAllowedIndicator);
            ccts_SchemeOrListModificationAllowedIndicator.setText(schemeOrListModificationAllowedIndicator);
        }
    }

    private void setDocumentation(Element element, org.oagi.srt.export.model.Component component) {
        String definition = component.getDefinition();
        String definitionSource = component.getDefinitionSource();

        setDocumentation(element, definition, definitionSource);
    }

    private void setDocumentation(Element element, String definition, String definitionSource) {
        if (definition == null) {
            return;
        }

        Element annotationElement = new Element("annotation", XSD_NS);
        element.addContent(annotationElement);

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        if (definitionSource != null) {
            documentationElement.setAttribute("source", definitionSource);
        }
        documentationElement.setText(definition);
    }

    @Override
    public void visitBCCP(BCCP bccp) throws Exception {
        Element element = addSimpleElement(bccp);
        if (bccp.isNillable()) {
            element.setAttribute("nillable", "true");
        }
        String defaultValue = bccp.getDefaultValue();
        if (!StringUtils.isEmpty(defaultValue)) {
            element.setAttribute("default", defaultValue);
        }
    }

    private Element addSimpleElement(org.oagi.srt.export.model.Component component) {
        Element element = new Element("element", XSD_NS);

        element.setAttribute("name", component.getName());
        element.setAttribute("type", component.getTypeName());
        element.setAttribute("id", component.getGuid());

        setDocumentation(element, component);

        rootElement.addContent(element);

        return element;
    }

    @Override
    public void visitACCComplexType(ACCComplexType accComplexType) throws Exception {
        switch (accComplexType.getOagisComponentType()) {
            case OAGIS10Nouns:
            case OAGIS10BODs:
                processOAGIS10(accComplexType);
                break;
            default:
                processACCComplexType(accComplexType);
        }
    }

    private void processOAGIS10(ACCComplexType accComplexType) throws Exception {
        Element complexTypeElement = new Element("complexType", XSD_NS);

        String name = accComplexType.getName();
        complexTypeElement.setAttribute("name", name + "Type");
        complexTypeElement.setAttribute("id", accComplexType.getGuid());

        Element sequenceElement = new Element("sequence", XSD_NS);
        complexTypeElement.addContent(sequenceElement);

        String delimiter;
        switch (accComplexType.getOagisComponentType()) {
            case OAGIS10Nouns:
                delimiter = "Nouns";
                break;
            case OAGIS10BODs:
                delimiter = "BODs";
                break;
            default:
                throw new IllegalStateException();
        }
        for (SchemaModule dependedModule : schemaModule.getDependedModules()) {
            String path = dependedModule.getPath();
            if (path.contains(delimiter + File.separator)) {
                Element element = new Element("element", XSD_NS);

                String bodName = path.substring(path.lastIndexOf(File.separator) + 1, path.length());
                element.setAttribute("ref", bodName);
                element.setAttribute("id", Utility.generateGUID((name + path).getBytes()));
                element.setAttribute("minOccurs", "0");

                sequenceElement.addContent(element);
            }
        }

        rootElement.addContent(complexTypeElement);
    }

    private void processACCComplexType(ACCComplexType accComplexType) throws Exception {
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
        List<CoreComponentRelation> coreComponents = coreComponentService.getCoreComponents(
                accComplexType.getRawId(), coreComponentProvider);
        // for ASCC or BCC (Sequence Key != 0)
        for (CoreComponentRelation coreComponent : coreComponents) {
            if (coreComponent.getDen().endsWith("Any Structured Content")) {
                Element anyElement = new Element("any", XSD_NS);

                anyElement.setAttribute("namespace", "##any");
                anyElement.setAttribute("processContents", "strict");
                setCardinalities(anyElement, coreComponent);
                anyElement.setAttribute("id", coreComponent.getGuid());

                sequenceElement.addContent(anyElement);
            } else if (coreComponent instanceof BasicCoreComponent) {
                BasicCoreComponent bcc = (BasicCoreComponent) coreComponent;
                BasicCoreComponentProperty bccp = importedDataProvider.findBCCP(bcc.getToBccpId());

                if (bcc.getSeqKey() > 0) {
                    Element element = new Element("element", XSD_NS);

                    element.setAttribute("ref", Utility.toCamelCase(bccp.getPropertyTerm()));
                    element.setAttribute("id", bcc.getGuid());
                    setCardinalities(element, bcc);

                    sequenceElement.addContent(element);
                    setDocumentation(element, bcc.getDefinition(), bcc.getDefinitionSource());
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
                    setDocumentation(groupElement, ascc.getDefinition(), ascc.getDefinitionSource());
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
                    setDocumentation(element, ascc.getDefinition(), ascc.getDefinitionSource());
                }
            }
        }

        ACC basedACC = accComplexType.getBasedACC();
        if (basedACC != null) {
            Element complexContentElement = new Element("complexContent", XSD_NS);
            complexTypeElement.addContent(complexContentElement);

            Element extensionElement = new Element("extension", XSD_NS);
            extensionElement.setAttribute("base", basedACC.getTypeName());
            complexContentElement.addContent(extensionElement);

            if (!sequenceElement.getContent().isEmpty() || accComplexType.getOagisComponentType() == Extension) {
                extensionElement.addContent(sequenceElement);
            }
        } else {
            if (!sequenceElement.getContent().isEmpty()) {
                complexTypeElement.addContent(sequenceElement);
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
                    if (bcc.isNillable()) {
                        attributeElement.setAttribute("nillable", "true");
                    }
                    String defaultValue = bcc.getDefaultValue();
                    if (!StringUtils.isEmpty(defaultValue)) {
                        attributeElement.setAttribute("default", defaultValue);
                    }

                    if (basedACC != null) {
                        Element complexContentElement = complexTypeElement.getChild("complexContent", XSD_NS);
                        Element extensionElement = complexContentElement.getChild("extension", XSD_NS);

                        extensionElement.addContent(attributeElement);
                    } else {
                        complexTypeElement.addContent(attributeElement);
                    }

                    setDocumentation(attributeElement, bcc.getDefinition(), bcc.getDefinitionSource());
                }
            }
        }

        rootElement.addContent(complexTypeElement);
    }

    private void setCardinalities(Element element, CoreComponentRelation ascc) {
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
        Element element = addSimpleElement(asccpComplexType);
        if (asccpComplexType.isNillable()) {
            element.setAttribute("nillable", "true");
        }
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

        return FilenameUtils.separatorsToUnix(pathRelative.toString()) + ".xsd";
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
