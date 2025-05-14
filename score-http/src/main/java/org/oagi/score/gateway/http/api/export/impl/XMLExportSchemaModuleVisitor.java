package org.oagi.score.gateway.http.api.export.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.seq_key.SeqKeySupportable;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.export.model.*;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.ScoreConstants;
import org.oagi.score.gateway.http.common.helper.Context;
import org.oagi.score.gateway.http.common.util.Utility;
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

import static org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType.Extension;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Scope(SCOPE_PROTOTYPE)
public class XMLExportSchemaModuleVisitor {

    private static final String ID_ATTIBUTE_PREFIX = "_";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SchemaModule schemaModule;
    private File baseDir;

    private Document document;
    private Element rootElement;
    private org.jdom2.Namespace targetNamespace;
    private File moduleFile;
    private final org.jdom2.Namespace OAGI_NS = org.jdom2.Namespace.getNamespace("", ScoreConstants.OAGI_NS);
    private final org.jdom2.Namespace XSD_NS = org.jdom2.Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    private CcDocument ccDocument;

    public XMLExportSchemaModuleVisitor(CcDocument ccDocument) {
        this.ccDocument = ccDocument;
    }

    public void setBaseDirectory(File baseDirectory) throws IOException {
        this.baseDir = baseDirectory.getCanonicalFile();
    }

    private org.jdom2.Namespace getNamespace(SchemaModule schemaModule) {
        return schemaModule.getNamespace().asJdom2Namespace();
    }

    public void startSchemaModule(SchemaModule schemaModule) throws Exception {
        this.schemaModule = schemaModule;
        this.document = createDocument();

        Element schemaElement = new Element("schema", XSD_NS);
        this.targetNamespace = getNamespace(schemaModule);
        schemaElement.addNamespaceDeclaration(targetNamespace);
        schemaModule.getAdditionalNamespaces().stream().forEach(e -> {
            schemaElement.addNamespaceDeclaration(e.asJdom2Namespace());
        });
        schemaElement.setAttribute("targetNamespace", targetNamespace.getURI());
        schemaElement.setAttribute("elementFormDefault", "qualified");
        schemaElement.setAttribute("attributeFormDefault", "unqualified");
        String versionNum = schemaModule.getVersionNum();
        if (StringUtils.hasLength(versionNum)) {
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

        org.jdom2.Namespace namespace = getNamespace(includeSchemaModule);
        this.rootElement.addNamespaceDeclaration(namespace);
    }

    public void visitImportModule(SchemaModule importSchemaModule) throws Exception {
        Element importElement = new Element("import", XSD_NS);
        String schemaLocation = getRelativeSchemaLocation(importSchemaModule);
        importElement.setAttribute("schemaLocation", schemaLocation);
        importElement.setAttribute("namespace", importSchemaModule.getNamespace().getNamespaceUri());
        rootElement.addContent(importElement);

        org.jdom2.Namespace namespace = getNamespace(importSchemaModule);
        this.rootElement.addNamespaceDeclaration(namespace);
    }

    public void visitAgencyId(AgencyId agencyId) throws Exception {
        // ContentType part
        Element simpleTypeElement = new Element("simpleType", XSD_NS);
        rootElement.addContent(simpleTypeElement);

        simpleTypeElement.setAttribute("name", agencyId.getTypeName());
        simpleTypeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + agencyId.getGuid());

        String agencyIdName = agencyId.getName().replaceAll(" ", "").replace("Identifier", "ID");
        Element unionElement = new Element("union", XSD_NS);
        String agencyIdTypeName = attachNamespacePrefixIfExists(
                agencyIdName + "ContentEnumerationType", agencyId.getNamespaceId());
        unionElement.setAttribute("memberTypes", "xsd:token " + agencyIdTypeName);
        simpleTypeElement.addContent(unionElement);

        // ContentEnumerationType part
        Element enumerationTypeElement = new Element("simpleType", XSD_NS);
        rootElement.addContent(enumerationTypeElement);

        enumerationTypeElement.setAttribute("name", agencyIdName + "ContentEnumerationType");
        enumerationTypeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + agencyId.getEnumGuid());

        Element restrictionElement = new Element("restriction", XSD_NS);
        enumerationTypeElement.addContent(restrictionElement);

        restrictionElement.setAttribute("base", "xsd:token");

        for (AgencyIdListValueSummaryRecord value : agencyId.getValues()) {
            Element enumerationElement = new Element("enumeration", XSD_NS);
            restrictionElement.addContent(enumerationElement);

            enumerationElement.setAttribute("value", value.value());

            Element annotationElement = new Element("annotation", XSD_NS);
            enumerationElement.addContent(annotationElement);

            Element documentationElement = new Element("documentation", XSD_NS);
            annotationElement.addContent(documentationElement);

            documentationElement.setAttribute("source", ScoreConstants.OAGI_NS);

            Element cctsNameElement = new Element("ccts_Name", OAGI_NS);
            documentationElement.addContent(cctsNameElement);

            cctsNameElement.setText(value.name());

            if (value.definition() != null) {
                String definition = value.definition().content();
                if (hasLength(definition)) {
                    Element cctsDefinitionElement = new Element("ccts_Definition", OAGI_NS);
                    documentationElement.addContent(cctsDefinitionElement);
                    cctsDefinitionElement.setText(definition);
                }
            }
        }
    }

    public void visitCodeList(SchemaCodeList schemaCodeList) throws Exception {
        String name = schemaCodeList.getName();
        if (schemaCodeList.getEnumTypeGuid() != null) {
            Element codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "EnumerationType");
            codeListElement.setAttribute("id", ID_ATTIBUTE_PREFIX + schemaCodeList.getEnumTypeGuid());

            addRestriction(codeListElement, schemaCodeList.getValues());
            rootElement.addContent(codeListElement);

            codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "ContentType");
            codeListElement.setAttribute("id", ID_ATTIBUTE_PREFIX + schemaCodeList.getGuid());
            Element unionElement = new Element("union", XSD_NS);
            SchemaCodeList baseCodeList = schemaCodeList.getBaseCodeList();
            if (baseCodeList == null) {
                unionElement.setAttribute("memberTypes", attachNamespacePrefixIfExists(
                        name + "EnumerationType", schemaCodeList.getNamespaceId()) + " xsd:token");
            } else {
                unionElement.setAttribute("memberTypes", attachNamespacePrefixIfExists(
                        baseCodeList.getName() + "ContentType", baseCodeList.getNamespaceId()) + " xsd:token");
            }
            codeListElement.addContent(unionElement);
            rootElement.addContent(codeListElement);

        } else {
            Element codeListElement = new Element("simpleType", XSD_NS);
            codeListElement.setAttribute("name", name + "ContentType");
            codeListElement.setAttribute("id", ID_ATTIBUTE_PREFIX + schemaCodeList.getGuid());
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
        simpleTypeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + xbtSimpleType.getGuid());

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
        simpleTypeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + bdtSimpleType.getGuid());

        if (bdtSimpleType.isDefaultBDT()) {
            setDocumentation(simpleTypeElement, bdtSimpleType);
        }
        setRestriction(simpleTypeElement, bdtSimpleType);

        rootElement.addContent(simpleTypeElement);
    }

    private void setDocumentation(Element typeElement, BDTSimple bdtSimple) {
        DtSummaryRecord dataType = bdtSimple.getDataType();

        Element annotationElement = new Element("annotation", XSD_NS);
        typeElement.addContent(annotationElement);

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        documentationElement.setAttribute("lang", "en", org.jdom2.Namespace.XML_NAMESPACE);

        String den = dataType.den();

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

        if (dataType.definition() != null) {
            String definition = dataType.definition().content();
            if (hasLength(definition)) {
                Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
                documentationElement.addContent(ccts_Definition);
                ccts_Definition.setText(definition);
            }
        }

        Element ccts_DataTypeTermName = new Element("ccts_DataTypeTermName", OAGI_NS);
        documentationElement.addContent(ccts_DataTypeTermName);
        String dataTypeTerm = dataType.dataTypeTerm();
        ccts_DataTypeTermName.setText(dataTypeTerm);

        String qualifier = dataType.qualifier();
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

            setDocumentationForRestrictionOrUnionOrExtension(targetElement, bdtSimpleType, true);
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
                    DtSummaryRecord baseDataType = bdtSimpleType.getBaseDataType();
                    restrictionElement.setAttribute("base", attachNamespacePrefixIfExists(
                            bdtSimpleType.getBaseDTName(), baseDataType.namespaceId()));
                }
            }

            targetElement = restrictionElement;

            setDocumentationForRestrictionOrUnionOrExtension(targetElement, bdtSimpleType, false);
        }
    }

    private void setDocumentationForRestrictionOrUnionOrExtension(Element extensionElement, BDTSimple bdtSimple, boolean union) {
        DtSummaryRecord dataType = bdtSimple.getDataType();

        Element annotationElement = new Element("annotation", XSD_NS);
        extensionElement.addContent(annotationElement);

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        if (dataType.definition() != null) {
            String definitionSource = dataType.definition().source();
            if (StringUtils.hasLength(definitionSource)) {
                documentationElement.setAttribute("source", definitionSource);
            }
        }

        Element ccts_ContentComponentValueDomain = new Element("ccts_ContentComponentValueDomain", OAGI_NS);
        documentationElement.addContent(ccts_ContentComponentValueDomain);

        String contentComponentDefinition = dataType.contentComponentDefinition();
        if (hasLength(contentComponentDefinition)) {
            Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
            ccts_ContentComponentValueDomain.addContent(ccts_Definition);
            ccts_Definition.setText(contentComponentDefinition);
        }

        DtAwdPriSummaryRecord defaultDtAwdPri = bdtSimple.getDefaultDtAwdPri();
        if (defaultDtAwdPri != null) {
            boolean defaultIndicator;
            DtSummaryRecord cdt = getCDT(bdtSimple.getBdtManifestId());
            if (cdt != null) {
                DtAwdPriSummaryRecord cdtAwdPri =
                        this.ccDocument.getDtAwdPriList(cdt.dtManifestId()).stream()
                                .filter(e -> e.isDefault())
                                .findAny().orElse(null);
                defaultIndicator = cdtAwdPri.cdtPriName().equals(defaultDtAwdPri.cdtPriName());
            } else {
                defaultIndicator = false;
            }

            Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
            ccts_ContentComponentValueDomain.addContent(ccts_DefaultIndicator);
            ccts_DefaultIndicator.setText(defaultIndicator ? "True" : "False");

            Element ccts_PrimitiveTypeName = new Element("ccts_PrimitiveTypeName", OAGI_NS);
            ccts_ContentComponentValueDomain.addContent(ccts_PrimitiveTypeName);
            String primitiveTypeName = bdtSimple.getCdtPriName();
            ccts_PrimitiveTypeName.setText(primitiveTypeName);
        }
    }

    private boolean isSamePrimitive(DtAwdPriSummaryRecord dtAwdPri, DtAwdPriSummaryRecord cdtAwdPri) {
        if (dtAwdPri.dtAwdPriId().equals(cdtAwdPri.dtAwdPriId())) {
            return true;
        }

        if (dtAwdPri.xbtManifestId() != null && cdtAwdPri.xbtManifestId() != null) {
            return dtAwdPri.xbtName().equals(cdtAwdPri.xbtName());
        } else if (dtAwdPri.codeListManifestId() != null && cdtAwdPri.codeListManifestId() != null) {
            return dtAwdPri.codeListName().equals(cdtAwdPri.codeListName());
        } else if (dtAwdPri.agencyIdListManifestId() != null && cdtAwdPri.agencyIdListManifestId() != null) {
            return dtAwdPri.agencyIdListName().equals(cdtAwdPri.agencyIdListName());
        }

        return false;
    }

    private DtSummaryRecord getCDT(DtManifestId dtManifestId) {
        DtSummaryRecord dt = ccDocument.getDt(dtManifestId);
        if (dt.basedDtManifestId() == null) {
            return dt;
        }
        return getCDT(dt.basedDtManifestId());
    }

    private String getCodeListName(BDTSimpleType bdtSimpleType) {
        DtSummaryRecord dt = ccDocument.getDt(bdtSimpleType.getBdtManifestId());
        List<DtAwdPriSummaryRecord> dtAwdPriList =
                ccDocument.getDtAwdPriList(dt.dtManifestId()).stream()
                        .filter(e -> e.codeListManifestId() != null && e.isDefault()).collect(Collectors.toList());
        if (dtAwdPriList.isEmpty() || dtAwdPriList.size() > 1) {
            throw new IllegalStateException();
        }
        CodeListSummaryRecord codeList = ccDocument.getCodeList(
                dtAwdPriList.get(0).codeListManifestId());
        String codeListName = codeList.name().replaceAll(" ", "").replace("Identifier", "ID");
        return attachNamespacePrefixIfExists(codeListName, codeList.namespaceId());
    }

    public String getAgencyIdName(BDTSimpleType bdtSimpleType) {
        DtSummaryRecord dt = ccDocument.getDt(bdtSimpleType.getBdtManifestId());
        List<DtAwdPriSummaryRecord> dtAwdPriList =
                ccDocument.getDtAwdPriList(dt.dtManifestId()).stream()
                        .filter(e -> e.agencyIdListManifestId() != null && e.isDefault()).collect(Collectors.toList());
        if (dtAwdPriList.isEmpty() || dtAwdPriList.size() > 1) {
            throw new IllegalStateException();
        }

        AgencyIdListSummaryRecord agencyIdList = ccDocument.getAgencyIdList(
                dtAwdPriList.get(0).agencyIdListManifestId());
        String agencyIdListName = agencyIdList.name().replaceAll(" ", "").replace("Identifier", "ID");
        return attachNamespacePrefixIfExists(agencyIdListName, agencyIdList.namespaceId());
    }


    public void visitBDTSimpleContent(BDTSimpleContent bdtSimpleContent) throws Exception {
        Element complexTypeElement = new Element("complexType", XSD_NS);
        complexTypeElement.setAttribute("name", bdtSimpleContent.getName());
        complexTypeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + bdtSimpleContent.getGuid());

        setDocumentation(complexTypeElement, bdtSimpleContent);
        setSimpleContent(complexTypeElement, bdtSimpleContent);

        rootElement.addContent(complexTypeElement);
    }

    private void setSimpleContent(Element complexTypeElement, BDTSimpleContent bdtSimpleContent) {
        Element simpleContentElement = new Element("simpleContent", XSD_NS);
        complexTypeElement.addContent(simpleContentElement);

        Element extensionElement = new Element("extension", XSD_NS);
        setDocumentationForRestrictionOrUnionOrExtension(extensionElement, bdtSimpleContent, false);

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
            DtSummaryRecord baseDataType = bdtSimpleContent.getBaseDataType();
            extensionElement.setAttribute("base", attachNamespacePrefixIfExists(
                    baseName, baseDataType.namespaceId()));
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
            attributeElement.setAttribute("type", attachNamespacePrefixIfExists(
                    typeName, dtSc.getNamespaceId()));

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

            attributeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + dtSc.getGuid());

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

        DtScSummaryRecord bdtSc = dtSc.getDtSc();

        Element documentationElement = new Element("documentation", XSD_NS);
        annotationElement.addContent(documentationElement);

        documentationElement.setAttribute("lang", "en", org.jdom2.Namespace.XML_NAMESPACE);

        if (bdtSc.definition() != null) {
            String definitionSource = bdtSc.definition().source();
            if (StringUtils.hasLength(definitionSource)) {
                documentationElement.setAttribute("source", definitionSource);
            }
        }

        Element ccts_Cardinality = new Element("ccts_Cardinality", OAGI_NS);
        documentationElement.addContent(ccts_Cardinality);

        if (dtSc.getCodeList() != null && dtSc.getCodeList().name().equals("clm6DateTimeFormatCode1_DateTimeFormatCode")) {
            ccts_Cardinality.setText("1..1");
        } else {
            ccts_Cardinality.setText(bdtSc.cardinality().min() + ".." + bdtSc.cardinality().max());
        }

        Element ccts_DictionaryEntryName = new Element("ccts_DictionaryEntryName", OAGI_NS);
        documentationElement.addContent(ccts_DictionaryEntryName);

        String dataTypeTerm = bdtSimple.getDataType().dataTypeTerm();
        String propertyTerm = bdtSc.propertyTerm();
        String representationTerm = bdtSc.representationTerm();
        String dictionaryEntryName = dataTypeTerm + ". " + propertyTerm + ". " + representationTerm;
        ccts_DictionaryEntryName.setText(dictionaryEntryName);

        if (bdtSc.definition() != null) {
            String definition = bdtSc.definition().content();
            if (hasLength(definition)) {
                Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
                documentationElement.addContent(ccts_Definition);
                ccts_Definition.setText(definition);
            }
        }

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

        DtScAwdPriSummaryRecord defaultDtScAwdPri = dtSc.getDtScAwdPri();
        if (defaultDtScAwdPri != null) {
            DtScSummaryRecord cdtSc = getCDTSC(dtSc.getDtSc().dtScManifestId());
            Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_DefaultIndicator);
            boolean defaultIndicator;
            if (cdtSc != null) {
                DtScAwdPriSummaryRecord cdtScAwdPri =
                        this.ccDocument.getDtScAwdPriList(cdtSc.dtScManifestId()).stream()
                                .filter(e -> e.isDefault())
                                .findAny().orElse(null);

                defaultIndicator = cdtScAwdPri.cdtPriName().equals(defaultDtScAwdPri.cdtPriName());
            } else {

                // For CDT_SCs not defined in CCTS, the corresponding CDT is identified through the Representation Term,
                // and the default status is determined based on the CDT_AWD_PRI.
                List<DtSummaryRecord> dtList = ccDocument.getDtList();
                DtSummaryRecord cdtByRepresentationTerm = dtList.stream()
                        .filter(e -> e.library().name().equals("CCTS Data Type Catalogue v3") &&
                                e.release().releaseNum().equals("3.1"))
                        .filter(e -> e.dataTypeTerm().equals(dtSc.getDtSc().representationTerm()))
                        .filter(e -> e.basedDtManifestId() == null)
                        .findAny().orElse(null);
                DtAwdPriSummaryRecord defaultCdtAwdPri = null;
                if (cdtByRepresentationTerm != null) {
                    defaultCdtAwdPri = this.ccDocument.getDtAwdPriList(cdtByRepresentationTerm.dtManifestId()).stream()
                            .filter(e -> e.isDefault())
                            .findAny().orElse(null);
                }

                // If the CDT for a CDT_SC cannot be found, the default is set to False.
                defaultIndicator = (defaultCdtAwdPri != null) ?
                        defaultCdtAwdPri.cdtPriName().equals(defaultDtScAwdPri.cdtPriName()) : false;
            }

            ccts_DefaultIndicator.setText(defaultIndicator ? "True" : "False");

            Element ccts_PrimitiveTypeName = new Element("ccts_PrimitiveTypeName", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_PrimitiveTypeName);
            String cdtPriName = dtSc.getCdtPriName();
            ccts_PrimitiveTypeName.setText(cdtPriName);
        } else {
            AgencyIdListSummaryRecord agencyIdList = dtSc.getAgencyIdList();
            CodeListSummaryRecord codeList = dtSc.getCodeList();

            Element ccts_DefaultIndicator = new Element("ccts_DefaultIndicator", OAGI_NS);
            ccts_SupplementaryComponentValueDomain.addContent(ccts_DefaultIndicator);
            ccts_DefaultIndicator.setText("True");

            String schemeOrListID = null;
            String schemeOrListVersionID = null;
            String schemeOrListAgencyID = null;
            String schemeOrListModificationAllowedIndicator = null;

            if (agencyIdList != null) {
                String name = agencyIdList.name();
                name = name.substring("clm".length(), name.length());

                schemeOrListAgencyID = "" + name.charAt(0);
                name = name.substring(1);

                schemeOrListID = name.substring(0, name.indexOf('D'));

                name = name.substring(0, name.indexOf('_'));
                schemeOrListVersionID = name.substring(schemeOrListID.length(), name.length());

                schemeOrListModificationAllowedIndicator = "False";
            } else if (codeList != null) {
                String name = codeList.name();

                while (name.startsWith("oacl")) {
                    codeList = ccDocument.getCodeList(codeList.basedCodeListManifestId());
                    if (codeList == null) {
                        break;
                    }
                    name = codeList.name();
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

    private DtScSummaryRecord getCDTSC(DtScManifestId dtScManifestId) {
        DtScSummaryRecord dtSc = ccDocument.getDtSc(dtScManifestId);
        if (dtSc.basedDtScManifestId() == null) {
            DtSummaryRecord ownerDt = ccDocument.getDt(dtSc.ownerDtManifestId());
            // If the ownerDt is not CDT, DT_SC is not a CDT_SC.
            return (ownerDt.basedDtManifestId() == null) ? dtSc : null;
        }
        return getCDTSC(dtSc.basedDtScManifestId());
    }

    private boolean isSamePrimitive(DtScAwdPriSummaryRecord dtScAwdPri, DtScAwdPriSummaryRecord cdtScAwdPri) {
        if (dtScAwdPri.dtScAwdPriId().equals(cdtScAwdPri.dtScAwdPriId())) {
            return true;
        }

        if (dtScAwdPri.xbtManifestId() != null && cdtScAwdPri.xbtManifestId() != null) {
            return dtScAwdPri.xbtName().equals(cdtScAwdPri.xbtName());
        } else if (dtScAwdPri.codeListManifestId() != null && cdtScAwdPri.codeListManifestId() != null) {
            return dtScAwdPri.codeListName().equals(cdtScAwdPri.codeListName());
        } else if (dtScAwdPri.agencyIdListManifestId() != null && cdtScAwdPri.agencyIdListManifestId() != null) {
            return dtScAwdPri.agencyIdListName().equals(cdtScAwdPri.agencyIdListName());
        }

        return false;
    }

    private void setDocumentation(Element element, Component component) {
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

        documentationElement.setAttribute("lang", "en", org.jdom2.Namespace.XML_NAMESPACE);

        if (StringUtils.hasLength(definitionSource)) {
            documentationElement.setAttribute("source", definitionSource);
        }

        // @TODO:
        // Currently, there are an assumption that the `content` property must be
        // either a 'XML' formatted text or a plain text.
        // In order to have a flexibility of this,
        // The `content` property should have a 'Content-Type',
        // so that can be used for finding an appropriate parsing logic.
        if (definition.startsWith("<") && definition.endsWith(">")) {
            try {
                Element doc = text2Element("<doc>" + definition + "</doc>");
                doc.getChildren().forEach(e -> {
                    Element clone = e.clone();
                    setNamespaceRecursively(clone, org.jdom2.Namespace.getNamespace(ScoreConstants.OAGI_NS));
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

    private void setNamespaceRecursively(Element element, org.jdom2.Namespace namespace) {
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

    private Element addSimpleElement(Component component) {
        Element element = new Element("element", XSD_NS);

        element.setAttribute("name", component.getName());
        element.setAttribute("type", attachNamespacePrefixIfExists(
                component.getTypeName(), component.getTypeNamespaceId()));
        element.setAttribute("id", ID_ATTIBUTE_PREFIX + component.getGuid());

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
        complexTypeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + accComplexType.getGuid());

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
                element.setAttribute("ref", attachNamespacePrefixIfExists(bodName, dependedModule.getNamespace().getNamespaceId()));
                element.setAttribute("id", ID_ATTIBUTE_PREFIX + Utility.generateGUID((name + path).getBytes()));
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
        complexTypeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + accComplexType.getGuid());

        Element sequenceElement = new Element("sequence", XSD_NS);

        List<SeqKeySupportable> associations = ccDocument.getAssociationListByFromAccManifestId(accComplexType.accManifestId());

        // for ASCC or BCC (Sequence Key != 0)
        for (SeqKeySupportable assoc : associations) {
            if (assoc instanceof AsccSummaryRecord) {
                AsccSummaryRecord ascc = (AsccSummaryRecord) assoc;
                if (ascc.den().endsWith("Any Structured Content")) {
                    Element anyElement = new Element("any", XSD_NS);

                    anyElement.setAttribute("namespace", "##any");
                    anyElement.setAttribute("processContents", "strict");
                    setCardinalities(anyElement, ascc.cardinality().min(), ascc.cardinality().max());
                    anyElement.setAttribute("id", ID_ATTIBUTE_PREFIX + ascc.guid());

                    sequenceElement.addContent(anyElement);

                } else {
                    AsccpSummaryRecord asccp = ccDocument.getAsccp(ascc.toAsccpManifestId());
                    AccSummaryRecord acc = ccDocument.getAcc(asccp.roleOfAccManifestId());

                    if (acc.isGroup()) {
                        Element groupElement = new Element("group", XSD_NS);

                        String ref = Utility.toCamelCase(asccp.propertyTerm());
                        groupElement.setAttribute("ref", attachNamespacePrefixIfExists(ref, asccp.namespaceId()));
                        groupElement.setAttribute("id", ID_ATTIBUTE_PREFIX + ascc.guid());
                        setCardinalities(groupElement, ascc.cardinality().min(), ascc.cardinality().max());

                        sequenceElement.addContent(groupElement);
                        if (ascc.definition() != null) {
                            setDocumentation(groupElement, ascc.definition().content(), ascc.definition().source());
                        }
                    } else {
                        Element element = new Element("element", XSD_NS);

                        if (asccp.reusable()) {
                            String ref = Utility.toCamelCase(asccp.propertyTerm());
                            element.setAttribute("ref", attachNamespacePrefixIfExists(ref, asccp.namespaceId()));
                        } else {
                            element.setAttribute("name", Utility.toCamelCase(asccp.propertyTerm()));
                            String typeName = Utility.toCamelCase(asccp.den().substring((asccp.propertyTerm() + ". ").length())) + "Type";
                            element.setAttribute("type", attachNamespacePrefixIfExists(typeName, asccp.namespaceId()));
                        }

                        element.setAttribute("id", ID_ATTIBUTE_PREFIX + ascc.guid());
                        setCardinalities(element, ascc.cardinality().min(), ascc.cardinality().max());

                        sequenceElement.addContent(element);
                        if (ascc.definition() != null) {
                            setDocumentation(element, ascc.definition().content(), ascc.definition().source());
                        }
                    }
                }
            } else {
                BccSummaryRecord bcc = (BccSummaryRecord) assoc;

                if (bcc.entityType() == EntityType.Element) {
                    BccpSummaryRecord bccp = ccDocument.getBccp(bcc.toBccpManifestId());
                    Element element = new Element("element", XSD_NS);

                    String ref = Utility.toCamelCase(bccp.propertyTerm());
                    element.setAttribute("ref", attachNamespacePrefixIfExists(ref, bccp.namespaceId()));
                    element.setAttribute("id", ID_ATTIBUTE_PREFIX + bcc.guid());
                    setCardinalities(element, bcc.cardinality().min(), bcc.cardinality().max());

                    sequenceElement.addContent(element);
                    if (bcc.definition() != null) {
                        setDocumentation(element, bcc.definition().content(), bcc.definition().source());
                    }
                }
            }
        }

        ACC basedACC = accComplexType.getBasedACC();
        if (basedACC != null) {
            Element complexContentElement = new Element("complexContent", XSD_NS);
            complexTypeElement.addContent(complexContentElement);

            Element extensionElement = new Element("extension", XSD_NS);
            extensionElement.setAttribute("base", attachNamespacePrefixIfExists(
                    basedACC.getTypeName(), basedACC.getNamespaceId()));
            complexContentElement.addContent(extensionElement);

            if (!sequenceElement.getContent().isEmpty() || accComplexType.getOagisComponentType() == Extension) {
                extensionElement.addContent(sequenceElement);
            }
        } else {
            if (!sequenceElement.getContent().isEmpty()) {
                complexTypeElement.addContent(sequenceElement);
            }
        }

        for (SeqKeySupportable assoc : associations) {
            if (assoc instanceof BccSummaryRecord) {
                BccSummaryRecord bcc = (BccSummaryRecord) assoc;

                if (bcc.entityType() == EntityType.Attribute) {
                    BccpSummaryRecord bccp = ccDocument.getBccp(bcc.toBccpManifestId());
                    DtSummaryRecord dt = ccDocument.getDt(bccp.dtManifestId());

                    Element attributeElement = new Element("attribute", XSD_NS);

                    attributeElement.setAttribute("name", Utility.toLowerCamelCase(bccp.propertyTerm()));
                    attributeElement.setAttribute("type", attachNamespacePrefixIfExists(
                            ModelUtils.getTypeName(dt), dt.namespaceId()));

                    int useInt = bcc.cardinality().min() * 2 + bcc.cardinality().max();
                    String useVal = getUseAttributeValue(useInt);
                    if (useVal != null) {
                        attributeElement.setAttribute("use", useVal);
                    }

                    attributeElement.setAttribute("id", ID_ATTIBUTE_PREFIX + bcc.guid());
                    // Attribute 'nillable' is not allowed in element <xsd:attribute>
//                    if (bcc.getIsNillable() == 1) {
//                        attributeElement.setAttribute("nillable", "true");
//                    }
                    if (bcc.valueConstraint() != null) {
                        String defaultValue = bcc.valueConstraint().defaultValue();
                        if (StringUtils.hasLength(defaultValue)) {
                            attributeElement.setAttribute("default", defaultValue);
                        }
                    }

                    if (basedACC != null) {
                        Element complexContentElement = complexTypeElement.getChild("complexContent", XSD_NS);
                        Element extensionElement = complexContentElement.getChild("extension", XSD_NS);

                        extensionElement.addContent(attributeElement);
                    } else {
                        complexTypeElement.addContent(attributeElement);
                    }

                    if (bcc.definition() != null) {
                        setDocumentation(attributeElement, bcc.definition().content(), bcc.definition().source());
                    }
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

    private String attachNamespacePrefixIfExists(String str, NamespaceId namespaceId) {
        if (namespaceId == null) {
            return str;
        }
        NamespaceSummaryRecord namespace = ccDocument.getNamespace(namespaceId);
        if (StringUtils.hasLength(namespace.prefix())) {
            return namespace.prefix() + ":" + str;
        }
        return str;
    }
}
