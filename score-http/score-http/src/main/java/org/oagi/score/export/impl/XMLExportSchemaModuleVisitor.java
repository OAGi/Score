package org.oagi.score.export.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.*;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jooq.types.ULong;
import org.oagi.score.common.ScoreConstants;
import org.oagi.score.common.util.Utility;
import org.oagi.score.export.model.*;
import org.oagi.score.export.service.CoreComponentService;
import org.oagi.score.populate.helper.Context;
import org.oagi.score.repo.api.corecomponent.model.OagisComponentType;
import org.oagi.score.repo.api.impl.jooq.entity.tables.records.*;
import org.oagi.score.repository.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.oagi.score.common.util.OagisComponentType.Extension;
import static org.oagi.score.repo.api.corecomponent.model.OagisComponentType.SemanticGroup;
import static org.oagi.score.repo.api.corecomponent.model.OagisComponentType.UserExtensionGroup;
import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
public class XMLExportSchemaModuleVisitor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SchemaModule schemaModule;
    private File baseDir;

    private Document document;
    private Element rootElement;
    private Namespace targetNamespace;
    private File moduleFile;

    private final Namespace OAGI_NS = Namespace.getNamespace("", ScoreConstants.OAGI_NS);
    private final Namespace XSD_NS = Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    private CoreComponentService coreComponentService;

    private DataProvider dataProvider;

    public XMLExportSchemaModuleVisitor(CoreComponentService coreComponentService, DataProvider dataProvider) {
        this.coreComponentService = coreComponentService;
        this.dataProvider = dataProvider;
    }

    public void setBaseDirectory(File baseDirectory) throws IOException {
        this.baseDir = baseDirectory.getCanonicalFile();
    }

    private Namespace getNamespace(SchemaModule schemaModule) {
        return Namespace.getNamespace(schemaModule.getNamespacePrefix(), schemaModule.getNamespaceUri());
    }

    public void startSchemaModule(SchemaModule schemaModule) throws Exception {
        this.schemaModule = schemaModule;
        this.document = createDocument();

        Element schemaElement = new Element("schema", XSD_NS);
        this.targetNamespace = getNamespace(schemaModule);
        schemaElement.addNamespaceDeclaration(targetNamespace);
        schemaElement.setAttribute("targetNamespace", targetNamespace.getURI());
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

    private Element text2Element(String text) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        return builder.build(new StringReader(text)).getRootElement();
    }

    public void visitIncludeModule(SchemaModule includeSchemaModule) throws Exception {
        Element includeElement = new Element("include", XSD_NS);
        String schemaLocation = getRelativeSchemaLocation(includeSchemaModule);
        includeElement.setAttribute("schemaLocation", schemaLocation);
        rootElement.addContent(includeElement);

        Namespace namespace = getNamespace(includeSchemaModule);
        this.rootElement.addNamespaceDeclaration(namespace);
    }

    public void visitImportModule(SchemaModule importSchemaModule) throws Exception {
        Element importElement = new Element("import", XSD_NS);
        String schemaLocation = getRelativeSchemaLocation(importSchemaModule);
        importElement.setAttribute("schemaLocation", schemaLocation);
        importElement.setAttribute("namespace", importSchemaModule.getNamespaceUri());
        rootElement.addContent(importElement);

        Namespace namespace = getNamespace(importSchemaModule);
        this.rootElement.addNamespaceDeclaration(namespace);
    }

    public void visitAgencyId(AgencyId agencyId) throws Exception {
        // ContentType part
        Element simpleTypeElement = new Element("simpleType", XSD_NS);
        rootElement.addContent(simpleTypeElement);

        simpleTypeElement.setAttribute("name", agencyId.getTypeName());
        simpleTypeElement.setAttribute("id", agencyId.getGuid());

        Element unionElement = new Element("union", XSD_NS);
        String agencyIdTypeName = attachNamespacePrefixIfExists(agencyId.getName() + "ContentEnumerationType", agencyId.getNamespaceId());
        unionElement.setAttribute("memberTypes", "xsd:token " + agencyIdTypeName);
        simpleTypeElement.addContent(unionElement);

        // ContentEnumerationType part
        Element enumerationTypeElement = new Element("simpleType", XSD_NS);
        rootElement.addContent(enumerationTypeElement);

        enumerationTypeElement.setAttribute("name", agencyId.getName() + "ContentEnumerationType");
        enumerationTypeElement.setAttribute("id", agencyId.getEnumGuid());

        Element restrictionElement = new Element("restriction", XSD_NS);
        enumerationTypeElement.addContent(restrictionElement);

        restrictionElement.setAttribute("base", "xsd:token");

        for (AgencyIdListValueRecord value : agencyId.getValues()) {
            Element enumerationElement = new Element("enumeration", XSD_NS);
            restrictionElement.addContent(enumerationElement);

            enumerationElement.setAttribute("value", value.getValue());

            Element annotationElement = new Element("annotation", XSD_NS);
            enumerationElement.addContent(annotationElement);

            Element documentationElement = new Element("documentation", XSD_NS);
            annotationElement.addContent(documentationElement);

            documentationElement.setAttribute("source", ScoreConstants.OAGI_NS);

            Element cctsNameElement = new Element("ccts_Name", OAGI_NS);
            documentationElement.addContent(cctsNameElement);

            cctsNameElement.setText(value.getName());

            Element cctsDefinitionElement = new Element("ccts_Definition", OAGI_NS);
            documentationElement.addContent(cctsDefinitionElement);

            cctsDefinitionElement.setText(value.getDefinition());
        }
    }

    public void visitCodeList(SchemaCodeList schemaCodeList) throws Exception {
        String name = schemaCodeList.getName();
        if (schemaCodeList.getEnumTypeGuid() != null) {
            Element codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "EnumerationType");
            codeListElement.setAttribute("id", schemaCodeList.GUID_PREFIX + schemaCodeList.getEnumTypeGuid());

            addRestriction(codeListElement, schemaCodeList.getValues());
            rootElement.addContent(codeListElement);

            codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "ContentType");
            codeListElement.setAttribute("id", schemaCodeList.getGuid());
            Element unionElement = new Element("union", XSD_NS);
            SchemaCodeList baseCodeList = schemaCodeList.getBaseCodeList();
            if (baseCodeList == null) {
                unionElement.setAttribute("memberTypes", attachNamespacePrefixIfExists(name + "EnumerationType", schemaCodeList.getNamespaceId()) + " xsd:token");
            } else {
                unionElement.setAttribute("memberTypes", attachNamespacePrefixIfExists(baseCodeList.getName() + "ContentType", baseCodeList.getNamespaceId()) + " xsd:token");
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
        DtRecord dataType = bdtSimple.getDataType();

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

        String qualifier = dataType.getQualifier_();
        if (StringUtils.hasLength(qualifier)) {
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
                            .map(e -> {
                                if (e.startsWith(XSD_NS.getPrefix())) {
                                    return e;
                                }
                                if (!hasLength(targetNamespace.getPrefix())) {
                                    return e;
                                }
                                return targetNamespace.getPrefix() + ":" + e;
                            })
                            .collect(Collectors.toList())
            );
            unionElement.setAttribute("memberTypes", memberTypes);
            simpleTypeElement.setAttribute("final", "union");

            targetElement = unionElement;
        } else {
            Element restrictionElement = new Element("restriction", XSD_NS);
            simpleTypeElement.addContent(restrictionElement);
            String name = bdtSimpleType.getName();

            if ((name.endsWith("CodeContentType") && !name.equals("CodeContentType")) ||
                    (name.endsWith("IDContentType") && !name.equals("IDContentType"))) {
                String baseName;
                if (name.endsWith("CodeContentType")) {
                    baseName = getCodeListName(bdtSimpleType);
                } else {
                    baseName = getAgencyIdName(bdtSimpleType);
                }

                restrictionElement.setAttribute("base", baseName + "ContentType");
            } else {
                if (bdtSimpleType.isDefaultBDT()) {
                    String xbtName = bdtSimpleType.getXbtName();
                    // TODO:
                    // xbtName = attachNamespacePrefixIfExists(xbtName, xbt.getNamespaceId());
                    if (!xbtName.startsWith(XSD_NS.getPrefix()) && hasLength(targetNamespace.getPrefix())) {
                        xbtName = targetNamespace.getPrefix() + ":" + xbtName;
                    }
                    restrictionElement.setAttribute("base", xbtName);
                } else {
                    restrictionElement.setAttribute("base", attachNamespacePrefixIfExists(bdtSimpleType.getBaseDTName(), bdtSimpleType.getNamespaceId()));
                }
            }

            targetElement = restrictionElement;
        }

        setDocumentationForRestrictionOrUnionOrExtension(targetElement, bdtSimpleType);
    }

    private void setDocumentationForRestrictionOrUnionOrExtension(Element extensionElement, BDTSimple bdtSimple) {
        DtRecord dataType = bdtSimple.getDataType();
        String definition = dataType.getContentComponentDefinition();

        Element annotationElement = new Element("annotation", XSD_NS);
        extensionElement.addContent(annotationElement);

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        String definitionSource = dataType.getDefinitionSource();
        if (StringUtils.hasLength(definitionSource)) {
            documentationElement.setAttribute("source", definitionSource);
        }

        Element ccts_ContentComponentValueDomain = new Element("ccts_ContentComponentValueDomain", OAGI_NS);
        documentationElement.addContent(ccts_ContentComponentValueDomain);

        Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
        ccts_ContentComponentValueDomain.addContent(ccts_Definition);
        ccts_Definition.setText(definition);

        CdtAwdPriRecord cdtAwdPri = bdtSimple.getDefaultCdtAwdPri();
        Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
        ccts_ContentComponentValueDomain.addContent(ccts_DefaultIndicator);
        ccts_DefaultIndicator.setText((cdtAwdPri.getIsDefault() == 1) ? "True" : "False");

        Element ccts_PrimitiveTypeName = new Element("ccts_PrimitiveTypeName", OAGI_NS);
        ccts_ContentComponentValueDomain.addContent(ccts_PrimitiveTypeName);
        String primitiveTypeName = bdtSimple.getCdtPriName();
        ccts_PrimitiveTypeName.setText(primitiveTypeName);

    }

    private String getCodeListName(BDTSimpleType bdtSimpleType) {
        List<BdtPriRestriRecord> bdtPriRestriList =
                dataProvider.findBdtPriRestriListByDtManifestId(bdtSimpleType.getBdtId()).stream()
                        .filter(e -> e.getCodeListManifestId() != null).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }
        CodeListManifestRecord codeListManifest = dataProvider.findCodeListManifest(bdtPriRestriList.get(0).getCodeListManifestId());
        CodeListRecord codeList = dataProvider.findCodeList(codeListManifest.getCodeListId());
        return attachNamespacePrefixIfExists(codeList.getName(), codeList.getNamespaceId());
    }

    public String getAgencyIdName(BDTSimpleType bdtSimpleType) {
        List<BdtPriRestriRecord> bdtPriRestriList =
                dataProvider.findBdtPriRestriListByDtManifestId(bdtSimpleType.getBdtId()).stream()
                        .filter(e -> e.getAgencyIdListManifestId() != null).collect(Collectors.toList());
        if (bdtPriRestriList.isEmpty() || bdtPriRestriList.size() > 1) {
            throw new IllegalStateException();
        }

        AgencyIdListManifestRecord agencyIdListManifest = dataProvider.findAgencyIdListManifest(bdtPriRestriList.get(0).getAgencyIdListManifestId());
        AgencyIdListRecord agencyIdList = dataProvider.findAgencyIdList(agencyIdListManifest.getAgencyIdListId());
        return attachNamespacePrefixIfExists(agencyIdList.getName(), agencyIdList.getNamespaceId());
    }


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
            // TODO:
            // xbtName = attachNamespacePrefixIfExists(xbtName, xbt.getNamespaceId());
            if (!xbtName.startsWith(XSD_NS.getPrefix()) && hasLength(targetNamespace.getPrefix())) {
                xbtName = targetNamespace.getPrefix() + ":" + xbtName;
            }
            extensionElement.setAttribute("base", xbtName);
        } else {
            extensionElement.setAttribute("base", attachNamespacePrefixIfExists(baseName, bdtSimpleContent.getNamespaceId()));
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
            attributeElement.setAttribute("type", attachNamespacePrefixIfExists(typeName, dtSc.getNamespaceId()));

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

        DtScRecord bdtSc = dtSc.getBdtSc();

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        documentationElement.setAttribute("lang", "en", Namespace.XML_NAMESPACE);

        String definitionSource = bdtSc.getDefinitionSource();
        if (StringUtils.hasLength(definitionSource)) {
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
        String dictionaryEntryName = dataTypeTerm + ". " + propertyTerm + ". " + representationTerm;
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

        XbtRecord xbt = dtSc.getXbt();
        if (xbt != null) {
            Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_DefaultIndicator);
            CdtScAwdPriRecord cdtScAwdPri = dtSc.getCdtScAwdPri();
            ccts_DefaultIndicator.setText((cdtScAwdPri.getIsDefault() == 1) ? "True" : "False");

            Element ccts_PrimitiveTypeName = new Element("ccts_PrimitiveTypeName", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_PrimitiveTypeName);
            CdtPriRecord cdtPri = dtSc.getCdtPri();
            ccts_PrimitiveTypeName.setText(cdtPri.getName());
        } else {
            AgencyIdListRecord agencyIdList = dtSc.getAgencyIdList();
            CodeListRecord codeList = dtSc.getCodeList();

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
                    codeList = dataProvider.findCodeList(codeList.getBasedCodeListId());
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

    private void setDocumentation(Element element, org.oagi.score.export.model.Component component) {
        String definition = component.getDefinition();
        String definitionSource = component.getDefinitionSource();

        setDocumentation(element, definition, definitionSource);
    }

    private void setDocumentation(Element element, String definition, String definitionSource) {
        if (!StringUtils.hasLength(definition)) {
            return;
        }

        Element annotationElement = new Element("annotation", XSD_NS);
        element.addContent(annotationElement);

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        documentationElement.setAttribute("lang", "en", Namespace.XML_NAMESPACE);

        if (StringUtils.hasLength(definitionSource)) {
            documentationElement.setAttribute("source", definitionSource);
        }

        // @TODO:
        // Currently, there are an assumption that the `definition` property must be
        // either a 'XML' formatted text or a plain text.
        // In order to have a flexibility of this,
        // The `definition` property should have a 'Content-Type',
        // so that can be used for finding an appropriate parsing logic.
        if (definition.startsWith("<") && definition.endsWith(">")) {
            try {
                Element doc = text2Element("<doc>" + definition + "</doc>");
                doc.getChildren().forEach(e -> {
                    Element clone = e.clone();
                    setNamespaceRecursively(clone, Namespace.getNamespace(ScoreConstants.OAGI_NS));
                    documentationElement.addContent(clone);
                });
            } catch (IOException | JDOMException e) {
                logger.warn("Can't parse a given text: " + definition, e);
            }
        } else {
            if (Pattern.compile("[<>&'\"]").matcher(definition).find()) {
                documentationElement.setContent(new CDATA(definition));
            } else {
                documentationElement.setText(definition);
            }
        }
    }

    private void setNamespaceRecursively(Element element, Namespace namespace) {
        element.setNamespace(namespace);
        for (Element child : element.getChildren()) {
            setNamespaceRecursively(child, namespace);
        }
    }

    public void visitBCCP(BCCP bccp) throws Exception {
        Element element = addSimpleElement(bccp);
        if (bccp.isNillable()) {
            element.setAttribute("nillable", "true");
        }
        String defaultValue = bccp.getDefaultValue();
        if (StringUtils.hasLength(defaultValue)) {
            element.setAttribute("default", defaultValue);
        }
    }

    private Element addSimpleElement(org.oagi.score.export.model.Component component) {
        Element element = new Element("element", XSD_NS);

        element.setAttribute("name", component.getName());
        element.setAttribute("type", attachNamespacePrefixIfExists(component.getTypeName(), component.getNamespaceId()));
        element.setAttribute("id", component.getGuid());

        setDocumentation(element, component);

        rootElement.addContent(element);

        return element;
    }

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
                element.setAttribute("ref", attachNamespacePrefixIfExists(bodName, dependedModule.getNamespaceId()));
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

        List<SeqKeyRecord> seqKeys = coreComponentService.getCoreComponents(
                accComplexType.getAccManifest().getAccManifestId(), dataProvider);

        String guidPrefix = "oagis-id-";
        // for ASCC or BCC (Sequence Key != 0)
        for (SeqKeyRecord seqKey : seqKeys) {
            if (seqKey.getAsccManifestId() != null) {
                AsccManifestRecord asccManifest = dataProvider.findASCCManifest(seqKey.getAsccManifestId());
                AsccRecord ascc = dataProvider.findASCC(asccManifest.getAsccId());
                if (ascc.getDen().endsWith("Any Structured Content")) {
                    Element anyElement = new Element("any", XSD_NS);

                    anyElement.setAttribute("namespace", "##any");
                    anyElement.setAttribute("processContents", "strict");
                    setCardinalities(anyElement, ascc.getCardinalityMin(), ascc.getCardinalityMax());
                    anyElement.setAttribute("id", guidPrefix + ascc.getGuid());

                    sequenceElement.addContent(anyElement);

                } else {
                    AsccpManifestRecord asccpManifest = dataProvider.findASCCPManifest(asccManifest.getToAsccpManifestId());
                    AccManifestRecord accManifest = dataProvider.findACCManifest(asccpManifest.getRoleOfAccManifestId());

                    AsccpRecord asccp = dataProvider.findASCCP(asccpManifest.getAsccpId());
                    AccRecord acc = dataProvider.findACC(accManifest.getAccId());

                    if (asccp.getGuid().equals(acc.getGuid())) {
                        Element groupElement = new Element("group", XSD_NS);

                        String ref = Utility.toCamelCase(asccp.getPropertyTerm());
                        groupElement.setAttribute("ref", attachNamespacePrefixIfExists(ref, asccp.getNamespaceId()));
                        groupElement.setAttribute("id", guidPrefix + ascc.getGuid());
                        setCardinalities(groupElement, ascc.getCardinalityMin(), ascc.getCardinalityMax());

                        sequenceElement.addContent(groupElement);
                        setDocumentation(groupElement, ascc.getDefinition(), ascc.getDefinitionSource());
                    } else {
                        Element element = new Element("element", XSD_NS);

                        if (asccp.getReusableIndicator() == 1) {
                            String ref = Utility.toCamelCase(asccp.getPropertyTerm());
                            element.setAttribute("ref", attachNamespacePrefixIfExists(ref, asccp.getNamespaceId()));
                        } else {
                            element.setAttribute("name", Utility.toCamelCase(asccp.getPropertyTerm()));
                            String typeName = Utility.toCamelCase(asccp.getDen().substring((asccp.getPropertyTerm() + ". ").length())) + "Type";
                            element.setAttribute("type", attachNamespacePrefixIfExists(typeName, asccp.getNamespaceId()));
                        }

                        element.setAttribute("id", guidPrefix + ascc.getGuid());
                        setCardinalities(element, ascc.getCardinalityMin(), ascc.getCardinalityMax());

                        sequenceElement.addContent(element);
                        setDocumentation(element, ascc.getDefinition(), ascc.getDefinitionSource());
                    }
                }
            } else {
                BccManifestRecord bccManifest = dataProvider.findBCCManifest(seqKey.getBccManifestId());
                BccRecord bcc = dataProvider.findBCC(bccManifest.getBccId());

                if (bcc.getEntityType() == 1) {
                    BccpManifestRecord bccpManifest = dataProvider.findBCCPManifest(bccManifest.getToBccpManifestId());
                    BccpRecord bccp = dataProvider.findBCCP(bccpManifest.getBccpId());
                    Element element = new Element("element", XSD_NS);

                    String ref = Utility.toCamelCase(bccp.getPropertyTerm());
                    element.setAttribute("ref", attachNamespacePrefixIfExists(ref, bccp.getNamespaceId()));
                    element.setAttribute("id", guidPrefix + bcc.getGuid());
                    setCardinalities(element, bcc.getCardinalityMin(), bcc.getCardinalityMax());

                    sequenceElement.addContent(element);
                    setDocumentation(element, bcc.getDefinition(), bcc.getDefinitionSource());
                }
            }
        }

        ACC basedACC = accComplexType.getBasedACC();
        if (basedACC != null) {
            Element complexContentElement = new Element("complexContent", XSD_NS);
            complexTypeElement.addContent(complexContentElement);

            Element extensionElement = new Element("extension", XSD_NS);
            extensionElement.setAttribute("base", attachNamespacePrefixIfExists(basedACC.getTypeName(), basedACC.getNamespaceId()));
            complexContentElement.addContent(extensionElement);

            if (!sequenceElement.getContent().isEmpty() || accComplexType.getOagisComponentType() == Extension) {
                extensionElement.addContent(sequenceElement);
            }
        } else {
            if (!sequenceElement.getContent().isEmpty()) {
                complexTypeElement.addContent(sequenceElement);
            }
        }

        for (SeqKeyRecord seqKey : seqKeys) {
            if (seqKey.getBccManifestId() != null) {
                BccManifestRecord bccManifest = dataProvider.findBCCManifest(seqKey.getBccManifestId());
                BccRecord bcc = dataProvider.findBCC(bccManifest.getBccId());
                BccpManifestRecord bccpManifest = dataProvider.findBCCPManifest(bccManifest.getToBccpManifestId());
                BccpRecord bccp = dataProvider.findBCCP(bccpManifest.getBccpId());
                DtRecord bdt = dataProvider.findDT(bccp.getBdtId());

                if (bcc.getEntityType() == 0) {
                    Element attributeElement = new Element("attribute", XSD_NS);

                    attributeElement.setAttribute("name", Utility.toLowerCamelCase(bccp.getPropertyTerm()));
                    attributeElement.setAttribute("type", attachNamespacePrefixIfExists(ModelUtils.getTypeName(bdt), bdt.getNamespaceId()));

                    int useInt = bcc.getCardinalityMin() * 2 + bcc.getCardinalityMax();
                    String useVal = getUseAttributeValue(useInt);
                    if (useVal != null) {
                        attributeElement.setAttribute("use", useVal);
                    }

                    attributeElement.setAttribute("id", guidPrefix + bcc.getGuid());
                    if (bcc.getIsNillable() == 1) {
                        attributeElement.setAttribute("nillable", "true");
                    }
                    String defaultValue = bcc.getDefaultValue();
                    if (StringUtils.hasLength(defaultValue)) {
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

    private void setCardinalities(Element element, Integer min, Integer max) {
        element.setAttribute("minOccurs", Integer.toString(min));
        switch (max) {
            case -1:
                element.setAttribute("maxOccurs", "unbounded");
                break;
            default:
                element.setAttribute("maxOccurs", Integer.toString(max));
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

    public void visitACCGroup(ACCGroup accGroup) throws Exception {
        // not implemented yet
    }

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

    public void visitASCCPGroup(ASCCPGroup asccpGroup) throws Exception {
        // not implemented yet
    }

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

    public File endSchemaModule(SchemaModule schemaModule) throws Exception {
        FileUtils.forceMkdir(this.moduleFile.getParentFile());

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.moduleFile))) {
            outputter.output(this.document, outputStream);
            outputStream.flush();
        }

        return this.moduleFile;
    }

    private boolean isGroup(AccRecord acc) {
        OagisComponentType componentType = OagisComponentType.valueOf(acc.getOagisComponentType());
        return componentType == SemanticGroup || componentType == UserExtensionGroup;
    }

    private String attachNamespacePrefixIfExists(String str, ULong namespaceId) {
        if (namespaceId == null) {
            return str;
        }
        NamespaceRecord namespace = dataProvider.findNamespace(namespaceId);
        if (StringUtils.hasLength(namespace.getPrefix())) {
            return namespace.getPrefix() + ":" + str;
        }
        return str;
    }
}
