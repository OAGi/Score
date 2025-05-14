package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import jakarta.annotation.Nullable;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListSummaryRecord;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListValueSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.BIE;
import org.oagi.score.gateway.http.api.bie_management.model.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.Facet;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.abie.AbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbie.AsbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.asbiep.AsbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbiep.BbiepSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.cc_management.model.CcType;
import org.oagi.score.gateway.http.api.cc_management.model.CoreComponent;
import org.oagi.score.gateway.http.api.cc_management.model.Definition;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.acc.OagisComponentType;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.EntityType;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListValueSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_category.model.ContextCategorySummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeSummaryRecord;
import org.oagi.score.gateway.http.api.context_management.context_scheme.model.ContextSchemeValueSummaryRecord;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceId;
import org.oagi.score.gateway.http.api.namespace_management.model.NamespaceSummaryRecord;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtSummaryRecord;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.util.ScoreGuidUtils;
import org.oagi.score.gateway.http.common.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.hasAnyValuesInFacets;
import static org.oagi.score.gateway.http.api.bie_management.service.generate_expression.Helper.toName;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.oagi.score.gateway.http.common.util.Utility.toZuluTimeString;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class BieXMLGenerateExpression implements BieGenerateExpression, InitializingBean {

    private static final org.jdom2.Namespace XSD_NAMESPACE = org.jdom2.Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
    private static final String ID_ATTRIBUTE_PREFIX = "_";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Document document;
    private Element schemaNode;
    private Element rootElementNode;

    private GenerateExpressionOption option;

    private final Map<String, Element> processedElements = new HashMap();

    @Autowired
    private ApplicationContext applicationContext;

    private GenerationContext generationContext;

    @Override
    public void afterPropertiesSet() {
        this.processedElements.clear();
        this.document = new Document();
        this.schemaNode = null;
    }

    private Namespace getNamespace(GenerationContext generationContext, NamespaceId namespaceId) {
        NamespaceSummaryRecord namespaceSummary = generationContext.findNamespace(namespaceId);
        if (namespaceSummary == null) {
            return null;
        }
        return org.jdom2.Namespace.getNamespace(namespaceSummary.prefix(), namespaceSummary.uri());
    }

    private Element generateSchema(Namespace targetNamespace) {
        Element schemaNode = newElement("schema");
        schemaNode.addNamespaceDeclaration(targetNamespace);
        schemaNode.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace"));
        schemaNode.setAttribute("targetNamespace", targetNamespace.getURI());
        schemaNode.setAttribute("elementFormDefault", "qualified");
        schemaNode.setAttribute("attributeFormDefault", "unqualified");
        document.addContent(schemaNode);
        return schemaNode;
    }

    private Element newElement(String localName) {
        return new Element(localName, XSD_NAMESPACE);
    }

    @Override
    public GenerationContext generateContext(ScoreUser requester, List<TopLevelAsbiepSummaryRecord> topLevelAsbieps, GenerateExpressionOption option) {
        return applicationContext.getBean(GenerationContext.class, requester, topLevelAsbieps);
    }

    @Override
    public void reset() {
        this.afterPropertiesSet();
    }

    @Override
    public void generate(ScoreUser requester, TopLevelAsbiepSummaryRecord topLevelAsbiep,
                         GenerationContext generationContext,
                         GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    @Override
    public File asFile(String filename) throws IOException {
        File tempFile = File.createTempFile(ScoreGuidUtils.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".xsd");

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            outputter.output(this.document, outputStream);
            outputStream.flush();
        }

        logger.info("XML Schema is generated: " + tempFile);

        return tempFile;
    }

    private void generateTopLevelAsbiep(TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord asccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        Namespace targetNamespace = getNamespace(generationContext, asccp.namespaceId());

        if (this.schemaNode == null) {
            this.schemaNode = generateSchema(targetNamespace);
        } else {
            String schemaTargetNamespaceValue = this.schemaNode.getAttributeValue("targetNamespace");
            if (!targetNamespace.getURI().equals(schemaTargetNamespaceValue)) {
                throw new IllegalArgumentException("The schema has a different target namespace: " + schemaTargetNamespaceValue);
            }
        }

        generationContext.referenceCounter().increase(asbiep);
        try {
            logger.debug("Generating Top Level ABIE w/ given ASBIEP Id: " + asbiep.asbiepId());

            rootElementNode = generateTopLevelASBIEP(asbiep, topLevelAsbiep);

            AbieSummaryRecord abie = generationContext.queryTargetABIE(asbiep);
            Element rootSeqNode = generateABIE(abie, rootElementNode);
            generateBIEs(abie, rootSeqNode);
            if (rootSeqNode.getChildren().isEmpty()) {
                rootSeqNode.detach();
            }
            // Issue #1615 & #1617
            attachBiePackageAttributes(rootElementNode, topLevelAsbiep, this.option.getBiePackage());
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private String key(BIE bie) {
        return bie.getClass().getSimpleName() + ID_ATTRIBUTE_PREFIX + bie.getGuid();
    }

    private String key(XbtSummaryRecord xbt) {
        return "XBT" + xbt.name();
    }

    private String key(CodeListSummaryRecord codeList) {
        return "CodeList" + ID_ATTRIBUTE_PREFIX + codeList.guid();
    }

    private String key(AgencyIdListSummaryRecord agencyIdList) {
        return "AgencyIdList" + ID_ATTRIBUTE_PREFIX + agencyIdList.guid();
    }

    private boolean isProcessed(BIE bie) {
        return processedElements.containsKey(key(bie));
    }

    private boolean isProcessed(XbtSummaryRecord xbt) {
        return processedElements.containsKey(key(xbt));
    }

    private boolean isProcessed(CodeListSummaryRecord codeList) {
        return processedElements.containsKey(key(codeList));
    }

    private boolean isProcessed(AgencyIdListSummaryRecord agencyIdList) {
        return processedElements.containsKey(key(agencyIdList));
    }

    private Element getProcessedElement(BIE bie) {
        return processedElements.get(key(bie));
    }

    private Element getProcessedElement(CodeListSummaryRecord codeList) {
        return processedElements.get(key(codeList));
    }

    private Element getProcessedElement(AgencyIdListSummaryRecord agencyIdList) {
        return processedElements.get(key(agencyIdList));
    }

    private Element setProcessedElement(BIE bie, Element element) {
        processedElements.put(key(bie), element);
        return element;
    }

    private Element setProcessedElement(XbtSummaryRecord xbt, Element element) {
        processedElements.put(key(xbt), element);
        return element;
    }

    private Element setProcessedElement(CodeListSummaryRecord codeList, Element element) {
        processedElements.put(key(codeList), element);
        return element;
    }

    private Element setProcessedElement(AgencyIdListSummaryRecord agencyIdList, Element element) {
        processedElements.put(key(agencyIdList), element);
        return element;
    }

    private void attachBiePackageAttributes(Element node, TopLevelAsbiepSummaryRecord topLevelAsbiep, BiePackageSummaryRecord biePackage) {
        List<Element> appinfoList = new ArrayList<>();
        if (biePackage != null) {
            if (hasLength(biePackage.versionName())) {
                appinfoList.add(makeAppInfo("Package Version Name", true, biePackage.versionName(), "token"));
            }
            if (hasLength(biePackage.versionId())) {
                appinfoList.add(makeAppInfo("Package Version ID", true, biePackage.versionId(), "token"));
            }
            if (hasLength(biePackage.description())) {
                appinfoList.add(makeAppInfo("Package Description", true, biePackage.description(), "token"));
            }
        }
        if (hasLength(topLevelAsbiep.version())) {
            appinfoList.add(makeAppInfo("Version ID", true, topLevelAsbiep.version(), "normalizedString"));
        }

        if (!appinfoList.isEmpty()) {
            Element prevAnnotation = node.getChild("annotation", XSD_NAMESPACE);
            if (prevAnnotation != null) {
                prevAnnotation.detach();
            }

            Element annotation = newElement("annotation");
            node.addContent(0, annotation);

            Element documentation = newElement("documentation");
            if (biePackage != null) {
                documentation.setText("Below are 'appinfo' elements containing BIE package information in beta format.");
            } else {
                documentation.setText("Below is the 'appinfo' element containing BIE version information in beta format.");
            }
            annotation.addContent(documentation);

            for (Element appinfo : appinfoList) {
                annotation.addContent(appinfo);
            }

            if (prevAnnotation != null) {
                for (Element child : prevAnnotation.getChildren()) {
                    child.detach();
                    annotation.addContent(child);
                }
            }
        }
    }

    private Element makeAppInfo(String name, boolean isRequired, String fixedValue, String type) {
        Element appinfo = newElement("appinfo");
        appinfo.setText(name + " = " + fixedValue);
        return appinfo;
    }

    private void setDefinition(Element node, String contextDefinition, @Nullable Namespace namespace) {
        if (!option.isBieDefinition()) {
            return;
        }

        String definition = contextDefinition;
        if (!StringUtils.hasLength(definition)) {
            return;
        }

        Element annotation = node.getChild("annotation", XSD_NAMESPACE);
        if (annotation == null) {
            annotation = newElement("annotation");
            node.addContent(0, annotation);
        }

        Element documentation = newElement("documentation");
        if (namespace != null) {
            documentation.setAttribute("source", namespace.getURI());
        }
        documentation.setText(definition);

        annotation.addContent(documentation);
    }

    private void setBusinessContext(Element node, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (!option.isBusinessContext()) {
            return;
        }

        AsbiepSummaryRecord asbiep = generationContext.findASBIEP(topLevelAsbiep.asbiepId(), topLevelAsbiep);
        AsccpSummaryRecord asccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        Namespace namespace = getNamespace(generationContext, asccp.namespaceId());

        Element annotation = newElement("annotation");
        node.addContent(annotation);

        Element documentation = newElement("documentation");
        annotation.addContent(documentation);
        List<BusinessContextSummaryRecord> bizCtxList = generationContext.findBusinessContexts(topLevelAsbiep);

        for (BusinessContextSummaryRecord bizCtx : bizCtxList) {
            Element ccts_BusinessContext = new Element("ccts_BusinessContext", namespace);
            documentation.addContent(ccts_BusinessContext);

            Element ccts_BusinessContextGUID = new Element("ccts_GUID", namespace);
            ccts_BusinessContext.addContent(ccts_BusinessContextGUID);
            ccts_BusinessContextGUID.setText(bizCtx.guid().value());

            Element ccts_BusinessContextName = new Element("ccts_Name", namespace);
            ccts_BusinessContext.addContent(ccts_BusinessContextName);
            ccts_BusinessContextName.setText(bizCtx.name());

            List<ContextSchemeValueSummaryRecord> contextSchemeValues = generationContext.findContextSchemeValue(bizCtx);
            for (ContextSchemeValueSummaryRecord contextSchemeValue : contextSchemeValues) {
                Element ccts_ContextValue = new Element("ccts_ContextValue", namespace);
                ccts_BusinessContext.addContent(ccts_ContextValue);

                Element ccts_ContextSchemeValueGUID = new Element("ccts_GUID", namespace);
                ccts_ContextValue.addContent(ccts_ContextSchemeValueGUID);
                ccts_ContextSchemeValueGUID.setText(contextSchemeValue.guid().value());

                Element ccts_ContextSchemeValue = new Element("ccts_Value", namespace);
                ccts_ContextValue.addContent(ccts_ContextSchemeValue);
                ccts_ContextSchemeValue.setText(contextSchemeValue.value());

                Element ccts_ContextSchemeValueMeaning = new Element("ccts_Meaning", namespace);
                ccts_ContextValue.addContent(ccts_ContextSchemeValueMeaning);
                ccts_ContextSchemeValueMeaning.setText(contextSchemeValue.meaning());

                ContextSchemeSummaryRecord contextScheme = generationContext.findContextScheme(contextSchemeValue.contextSchemeId());

                Element ccts_ClassificationScheme = new Element("ccts_ClassificationScheme", namespace);
                ccts_ContextValue.addContent(ccts_ClassificationScheme);

                Element ccts_ClassificationSchemeGUID = new Element("ccts_GUID", namespace);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeGUID);
                ccts_ClassificationSchemeGUID.setText(contextScheme.guid().value());

                {
                    ContextCategorySummaryRecord contextCategory = generationContext.findContextCategory(contextScheme.contextCategory().contextCategoryId());

                    Element ccts_ContextCategory = new Element("ccts_ContextCategory", namespace);
                    ccts_ClassificationScheme.addContent(ccts_ContextCategory);

                    Element ccts_ContextCategoryGUID = new Element("ccts_GUID", namespace);
                    ccts_ContextCategory.addContent(ccts_ContextCategoryGUID);
                    ccts_ContextCategoryGUID.setText(contextCategory.guid().value());

                    Element ccts_ContextCategoryName = new Element("ccts_Name", namespace);
                    ccts_ContextCategory.addContent(ccts_ContextCategoryName);
                    ccts_ContextCategoryName.setText(contextCategory.name());

                    Element ccts_ContextCategoryDefinition = new Element("ccts_Definition", namespace);
                    ccts_ContextCategory.addContent(ccts_ContextCategoryDefinition);
                    ccts_ContextCategoryDefinition.setText(contextCategory.description());
                }

                Element ccts_ClassificationSchemeId = new Element("ccts_ID", namespace);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeId);
                ccts_ClassificationSchemeId.setText(contextScheme.schemeId());

                Element ccts_ClassificationSchemeName = new Element("ccts_Name", namespace);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeName);
                ccts_ClassificationSchemeName.setText(contextScheme.schemeName());

                Element ccts_ClassificationSchemeAgencyID = new Element("ccts_AgencyID", namespace);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeAgencyID);
                ccts_ClassificationSchemeAgencyID.setText(contextScheme.schemeAgencyId());

                Element ccts_ClassificationSchemeVersionID = new Element("ccts_VersionID", namespace);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeVersionID);
                ccts_ClassificationSchemeVersionID.setText(contextScheme.schemeVersionId());

                Element ccts_ClassificationSchemeDefinition = new Element("ccts_Definition", namespace);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeDefinition);
                ccts_ClassificationSchemeDefinition.setText(contextScheme.description());
            }
        }
    }

    private AbstractBIEDocumentation getBIEDocumentation(BIE bie, CoreComponent cc) {
        if (bie instanceof AbieSummaryRecord && cc instanceof AccSummaryRecord) {
            AbieSummaryRecord abie = (AbieSummaryRecord) bie;
            TopLevelAsbiepSummaryRecord topLevelAsbiep = generationContext.findTopLevelAsbiep(abie.ownerTopLevelAsbiepId());
            return new ABIEDocumentation(
                    topLevelAsbiep, abie,
                    (AccSummaryRecord) cc);
        }

        if (bie instanceof AsbieSummaryRecord && cc instanceof AsccSummaryRecord) {
            return new ASBIEDocumentation(
                    (AsbieSummaryRecord) bie, (AsccSummaryRecord) cc,
                    generationContext.getAcc(((AsccSummaryRecord) cc).fromAccManifestId()));
        }

        if (bie instanceof AsbiepSummaryRecord && cc instanceof AsccpSummaryRecord) {
            return new ASBIEPDocumentation(
                    (AsbiepSummaryRecord) bie,
                    (AsccpSummaryRecord) cc);
        }

        if (bie instanceof BbieSummaryRecord && cc instanceof BccSummaryRecord) {
            return new BBIEDocumentation(
                    (BbieSummaryRecord) bie, (BccSummaryRecord) cc,
                    generationContext.getAcc(((BccSummaryRecord) cc).fromAccManifestId()));
        }

        if (bie instanceof BbiepSummaryRecord && cc instanceof BccpSummaryRecord) {
            return new BBIEPDocumentation(
                    (BbiepSummaryRecord) bie,
                    (BccpSummaryRecord) cc);
        }

        throw new IllegalArgumentException();
    }

    private void setOptionalDocumentation(Element node, BIE bie, CoreComponent cc) {
        AbstractBIEDocumentation bieDocumentation = getBIEDocumentation(bie, cc);
        setOptionalDocumentation(node, bieDocumentation);
    }

    private void setOptionalDocumentation(Element node, AbstractBIEDocumentation bieDocumentation) {
        boolean bieCctsMetaData = option.isBieCctsMetaData();
        boolean bieOagiScoreMetaData = option.isBieOagiScoreMetaData();
        boolean basedCcMetaData = option.isBasedCcMetaData();

        if (!basedCcMetaData && !bieOagiScoreMetaData) {
            return;
        }

        Element documentation = null;
        for (Element child : node.getChildren("documentation", XSD_NAMESPACE)) {
            if (child.getAttribute("source") == null) {
                documentation = child;
                break;
            }
        }

        if (documentation == null) {
            Element annotation = newElement("annotation");
            node.addContent(annotation);

            documentation = newElement("documentation");
            annotation.addContent(documentation);
        }

        Namespace namespace = getNamespace(generationContext, bieDocumentation.getNamespaceId());

        if (bieCctsMetaData) {
            String entityTypeCode = bieDocumentation.getEntityTypeCode();
            Element ccts_BIEEntityTypeCode = new Element("ccts_BIEEntityTypeCode", namespace.getURI());
            documentation.addContent(ccts_BIEEntityTypeCode);
            ccts_BIEEntityTypeCode.setText(entityTypeCode);

            String dictionaryEntryName = bieDocumentation.getDictionaryEntryName();
            if (StringUtils.hasLength(dictionaryEntryName)) {
                Element ccts_DictionaryEntryName = new Element("ccts_DictionaryEntryName", namespace.getURI());
                documentation.addContent(ccts_DictionaryEntryName);
                ccts_DictionaryEntryName.setText(dictionaryEntryName);
            }

            if (option.isIncludeCctsDefinitionTag()) {
                for (Definition definition : bieDocumentation.getDefinitions()) {
                    if (StringUtils.hasLength(definition.content())) {
                        Element ccts_Definition = new Element("ccts_Definition", namespace.getURI());
                        documentation.addContent(ccts_Definition);
                        ccts_Definition.setText(definition.content());

                        if (StringUtils.hasLength(definition.source())) {
                            ccts_Definition.setAttribute("source", definition.source());
                        }
                    }
                }
            }

            String objectClassTermName = bieDocumentation.getObjectClassTermName();
            if (StringUtils.hasLength(objectClassTermName)) {
                Element ccts_ObjectClassTermName = new Element("ccts_ObjectClassTermName", namespace.getURI());
                documentation.addContent(ccts_ObjectClassTermName);
                ccts_ObjectClassTermName.setText(objectClassTermName);
            }

            String propertyTermName = bieDocumentation.getPropertyTermName();
            if (StringUtils.hasLength(propertyTermName)) {
                Element ccts_PropertyTermName = new Element("ccts_PropertyTermName", namespace.getURI());
                documentation.addContent(ccts_PropertyTermName);
                ccts_PropertyTermName.setText(propertyTermName);
            }

            String representationTermName = bieDocumentation.getRepresentationTermName();
            if (StringUtils.hasLength(representationTermName)) {
                Element ccts_RepresentationTermName = new Element("ccts_RepresentationTermName", namespace.getURI());
                documentation.addContent(ccts_RepresentationTermName);
                ccts_RepresentationTermName.setText(representationTermName);
            }

            String dataTypeTermName = bieDocumentation.getDataTypeTermName();
            if (StringUtils.hasLength(dataTypeTermName)) {
                Element ccts_DataTypeTermName = new Element("ccts_DataTypeTermName", namespace.getURI());
                documentation.addContent(ccts_DataTypeTermName);
                ccts_DataTypeTermName.setText(dataTypeTermName);
            }

            String businessTerm = bieDocumentation.getBusinessTerm();
            if (StringUtils.hasLength(businessTerm)) {
                Element ccts_BusinessTerm = new Element("ccts_BusinessTerm", namespace.getURI());
                documentation.addContent(ccts_BusinessTerm);
                ccts_BusinessTerm.setText(businessTerm);
            }
        }

        if (bieOagiScoreMetaData) {
            String releaseNumber = bieDocumentation.getReleaseNumber();
            if (StringUtils.hasLength(releaseNumber)) {
                Element srt_BasedStandardReleaseNumber = new Element("srt_BasedStandardReleaseNumber", namespace.getURI());
                documentation.addContent(srt_BasedStandardReleaseNumber);
                srt_BasedStandardReleaseNumber.setText(releaseNumber);
            }

            String version = bieDocumentation.getVersion();
            if (StringUtils.hasLength(version)) {
                Element srt_Version = new Element("srt_Version", namespace.getURI());
                documentation.addContent(srt_Version);
                srt_Version.setText(version);
            }

            String stateCode = bieDocumentation.getStateCode();
            if (StringUtils.hasLength(stateCode)) {
                Element srt_StateCode = new Element("srt_StateCode", namespace.getURI());
                documentation.addContent(srt_StateCode);
                srt_StateCode.setText(stateCode);
            }

            String status = bieDocumentation.getStatus();
            if (StringUtils.hasLength(status)) {
                Element srt_Status = new Element("srt_Status", namespace.getURI());
                documentation.addContent(srt_Status);
                srt_Status.setText(status);
            }

            String remark = bieDocumentation.getRemark();
            if (StringUtils.hasLength(remark)) {
                Element srt_Remark = new Element("srt_Remark", namespace.getURI());
                documentation.addContent(srt_Remark);
                srt_Remark.setText(remark);
            }

            if (option.isIncludeWhoColumns()) {
                String ownerUserName = bieDocumentation.getOwnerUserName();
                if (StringUtils.hasLength(ownerUserName)) {
                    Element srt_OwnerUserName = new Element("srt_OwnerUserName", namespace.getURI());
                    documentation.addContent(srt_OwnerUserName);
                    srt_OwnerUserName.setText(ownerUserName);
                }

                String createdUserName = bieDocumentation.getCreatedUserName();
                if (StringUtils.hasLength(createdUserName)) {
                    Element srt_CreatedByUserName = new Element("srt_CreatedByUserName", namespace.getURI());
                    documentation.addContent(srt_CreatedByUserName);
                    srt_CreatedByUserName.setText(createdUserName);
                }

                String lastUpdatedUserName = bieDocumentation.getLastUpdatedUserName();
                if (StringUtils.hasLength(lastUpdatedUserName)) {
                    Element srt_LastUpdatedByUserName = new Element("srt_LastUpdatedByUserName", namespace.getURI());
                    documentation.addContent(srt_LastUpdatedByUserName);
                    srt_LastUpdatedByUserName.setText(lastUpdatedUserName);
                }

                Date creationTimestamp = bieDocumentation.getCreationTimestamp();
                if (creationTimestamp != null) {
                    Element srt_CreationTimestamp = new Element("srt_CreationTimestamp", namespace.getURI());
                    documentation.addContent(srt_CreationTimestamp);
                    srt_CreationTimestamp.setText(toZuluTimeString(creationTimestamp));
                }

                Date lastUpdatedTimestamp = bieDocumentation.getLastUpdatedTimestamp();
                if (lastUpdatedTimestamp != null) {
                    Element srt_LastUpdateTimestamp = new Element("srt_LastUpdateTimestamp", namespace.getURI());
                    documentation.addContent(srt_LastUpdateTimestamp);
                    srt_LastUpdateTimestamp.setText(toZuluTimeString(lastUpdatedTimestamp));
                }
            }
        }

        if (basedCcMetaData) {
            CcType ccType = bieDocumentation.ccType();

            String guid = bieDocumentation.guid();
            Element ccts_Based_GUID = new Element("ccts_Based" + ccType + "_GUID", namespace.getURI());
            documentation.addContent(ccts_Based_GUID);
            ccts_Based_GUID.setText(guid);

            int revisionNumber = bieDocumentation.getRevisionNumber();
            Element ccts_BasedRevisionNumber = new Element("ccts_Based" + ccType + "RevisionNumber", namespace.getURI());
            documentation.addContent(ccts_BasedRevisionNumber);
            ccts_BasedRevisionNumber.setText(Integer.toString(revisionNumber));

            for (Definition definition : bieDocumentation.getCoreComponentDefinitions()) {
                if (StringUtils.hasLength(definition.content())) {
                    Element ccts_BasedDefinition = new Element("ccts_Based" + ccType + "Definition", namespace.getURI());
                    documentation.addContent(ccts_BasedDefinition);
                    ccts_BasedDefinition.setText(definition.content());

                    if (StringUtils.hasLength(definition.source())) {
                        ccts_BasedDefinition.setAttribute("source", definition.source());
                    }
                }
            }
        }
    }

    public Element generateTopLevelASBIEP(AsbiepSummaryRecord asbiep, TopLevelAsbiepSummaryRecord topLevelAsbiep) {
        if (isProcessed(asbiep)) {
            return getProcessedElement(asbiep);
        }

        AsccpSummaryRecord asccp = generationContext.queryBasedASCCP(asbiep);
        Namespace namespace = getNamespace(generationContext, asccp.namespaceId());
        this.schemaNode.setAttribute("targetNamespace", namespace.getURI());
        this.schemaNode.addNamespaceDeclaration(namespace);
        Element rootEleNode = newElement("element");
        schemaNode.addContent(rootEleNode);

        rootEleNode.setAttribute("name", asccp.propertyTerm().replaceAll(" ", ""));
        if (option.isBieGuid()) {
            rootEleNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + asbiep.getGuid());
        }

        setDefinition(rootEleNode, asbiep.definition(), namespace);
        setBusinessContext(rootEleNode, topLevelAsbiep);
        setOptionalDocumentation(rootEleNode,
                new ASBIEPDocumentation(asbiep, asccp, topLevelAsbiep));

        setProcessedElement(asbiep, rootEleNode);

        return rootEleNode;
    }

    private Element generateABIE(AbieSummaryRecord abie, Element parentNode) {
        Element complexType = newElement("complexType");
        if (option.isBieGuid()) {
            complexType.setAttribute("id", ID_ATTRIBUTE_PREFIX + abie.getGuid());
        }
        parentNode.addContent(complexType);

        AccSummaryRecord acc = generationContext.queryBasedACC(abie);
        setDefinition(complexType, abie.definition(), getNamespace(generationContext, acc.namespaceId()));
        setOptionalDocumentation(complexType, abie, acc);

        Element element;
        if (OagisComponentType.Choice == acc.componentType()) {
            element = newElement("choice");
            complexType.addContent(element);
        } else {
            element = newElement("sequence");
            complexType.addContent(element);
        }

        return element;
    }

    public void generateBIEs(AbieSummaryRecord abie, Element parent) {
        List<BIE> childBIEs = generationContext.queryChildBIEs(abie);
        for (BIE bie : childBIEs) {
            if (bie instanceof BbieSummaryRecord) {
                BbieSummaryRecord bbie = (BbieSummaryRecord) bie;
                DtSummaryRecord bdt = generationContext.queryAssocBDT(bbie);
                generateBBIE(bbie, bdt, parent);
            } else {
                AsbieSummaryRecord asbie = (AsbieSummaryRecord) bie;

                if (Helper.isAnyProperty(asbie, generationContext)) {
                    generateAnyABIE(asbie, parent);
                } else {
                    Element node = generateASBIE(asbie, parent);
                    AsbiepSummaryRecord asbiep = generationContext.queryAssocToASBIEP(asbie);
                    Element asbiepNode = generateASBIEP(asbiep, node);
                    AbieSummaryRecord childAbie = generationContext.queryTargetABIE(asbiep);
                    Element compositorNode = generateABIE(childAbie, asbiepNode);

                    generationContext.referenceCounter().increase(asbiep)
                            .ifNotCircularReference(asbiep, () -> generateBIEs(childAbie, compositorNode))
                            .decrease(asbiep);

                    if (compositorNode.getChildren().isEmpty()) {
                        compositorNode.detach();
                    }
                }
            }
        }
    }

    private Element generateAnyABIE(AsbieSummaryRecord asbie, Element parent) {
        AsccSummaryRecord ascc = generationContext.queryBasedASCC(asbie);

        Element element = newElement("any");
        element.setAttribute("namespace", "##any");
        element.setAttribute("processContents", "strict");

        element.setAttribute("minOccurs", String.valueOf(asbie.cardinality().min()));
        if (asbie.cardinality().max() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(asbie.cardinality().max()));
        if (asbie.nillable())
            element.setAttribute("nillable", String.valueOf(asbie.nillable()));

        if (option.isBieGuid()) {
            element.setAttribute("id", ID_ATTRIBUTE_PREFIX + asbie.getGuid());
        }

        parent.addContent(element);
        return element;
    }

    public Element generateBDT(BbieSummaryRecord bbie, Element eNode, CodeListSummaryRecord codeList) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
        AccSummaryRecord fromAcc = generationContext.getAcc(bcc.fromAccManifestId());
        DtSummaryRecord bdt = generationContext.queryBDT(bbie);

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");

        if (option.isBieGuid()) {
            complexType.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
        }

        setDefinition(complexType, bbie.definition(), getNamespace(generationContext, fromAcc.namespaceId()));
        setOptionalDocumentation(complexType, bbie, bcc);

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
        String codeListTypeName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
        extNode.setAttribute("base", codeListTypeName);
        eNode.addContent(complexType);
        return eNode;
    }

    public String setBDTBase(DtSummaryRecord bdt) {
        DtAwdPriSummaryRecord dtAwdPri =
                generationContext.findDtAwdPriByDtManifestIdAndDefaultIsTrue(bdt.dtManifestId());
        XbtSummaryRecord xbt = Helper.getXbt(generationContext, dtAwdPri);
        addXbtSimpleType(xbt);
        return xbt.builtInType();
    }

    public String setBDTBase(BbieSummaryRecord bbie, DtSummaryRecord bdt) {
        XbtSummaryRecord xbt = generationContext.getXbt(bbie);
        String typeName = null;
        if (hasAnyValuesInFacets(bbie.facet())) {
            typeName = addXbtSimpleType(xbt, bbie.facet(), "type_" + bbie.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }
        return (typeName != null) ? typeName : xbt.builtInType();
    }

    public Element setBBIE_Attr_Type(DtSummaryRecord bdt, Element gNode) {
        DtAwdPriSummaryRecord dtAwdPri =
                generationContext.findDtAwdPriByDtManifestIdAndDefaultIsTrue(bdt.dtManifestId());
        XbtSummaryRecord xbt = Helper.getXbt(generationContext, dtAwdPri);
        if (xbt.builtInType() != null) {
            gNode.setAttribute("type", xbt.builtInType());
        }
        addXbtSimpleType(xbt);
        return gNode;
    }

    public Element setBBIE_Attr_Type(BbieSummaryRecord bbie, DtSummaryRecord bdt, Element gNode) {
        XbtSummaryRecord xbt = generationContext.getXbt(bbie.primitiveRestriction().xbtManifestId());

        String typeName = null;
        if (hasAnyValuesInFacets(bbie.facet())) {
            typeName = addXbtSimpleType(xbt, bbie.facet(), "type_" + bbie.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }

        gNode.setAttribute("type", (typeName != null) ? typeName : xbt.builtInType());

        return gNode;
    }

    public Element generateBDT(BbieSummaryRecord bbie, Element eNode) {
        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");

        if (option.isBieGuid()) {
            complexType.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
        }

        DtSummaryRecord bdt = generationContext.queryAssocBDT(bbie);
        setOptionalDocumentation(complexType, new BusinessDataTypeDocumentation(bdt));

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        if (bbie.primitiveRestriction().xbtManifestId() == null)
            extNode.setAttribute("base", setBDTBase(bdt));
        else {
            extNode.setAttribute("base", setBDTBase(bbie, bdt));
        }

        eNode.addContent(complexType);
        return eNode;
    }

    public Element generateASBIE(AsbieSummaryRecord asbie, Element parent) {
        Element element = newElement("element");
        if (option.isBieGuid()) {
            element.setAttribute("id", ID_ATTRIBUTE_PREFIX + asbie.getGuid());
        }
        element.setAttribute("minOccurs", String.valueOf(asbie.cardinality().min()));
        if (asbie.cardinality().max() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(asbie.cardinality().max()));
        if (asbie.nillable())
            element.setAttribute("nillable", String.valueOf(asbie.nillable()));

        while (!parent.getName().equals("sequence") && !parent.getName().equals("choice")) {
            parent = parent.getParentElement();
        }

        AsccSummaryRecord ascc = generationContext.queryBasedASCC(asbie);
        AccSummaryRecord fromAcc = generationContext.getAcc(ascc.fromAccManifestId());
        setDefinition(element, asbie.definition(), getNamespace(generationContext, fromAcc.namespaceId()));
        setOptionalDocumentation(element, asbie, ascc);

        parent.addContent(element);
        return element;
    }

    public Element generateASBIEP(AsbiepSummaryRecord asbiep, Element parent) {
        AsccpSummaryRecord asccp = generationContext.getAsccp(asbiep.basedAsccpManifestId());
        parent.setAttribute("name", Utility.first(asccp.den(), true));

        setDefinition(parent, asbiep.definition(), getNamespace(generationContext, asccp.namespaceId()));
        setOptionalDocumentation(parent, asbiep, asccp);

        return parent;
    }

    public Element handleElementBBIE(BbieSummaryRecord bbie, Element eNode) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.den(), true));
        if (option.isBieGuid()) {
            eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + bbie.getGuid());
        }

        if (bbie.nillable()) {
            eNode.setAttribute("nillable", "true");
        }
        if (bbie.valueConstraint() != null) {
            if (hasLength(bbie.valueConstraint().fixedValue())) {
                eNode.setAttribute("fixed", bbie.valueConstraint().fixedValue());
            } else if (hasLength(bbie.valueConstraint().defaultValue())) {
                eNode.setAttribute("default", bbie.valueConstraint().defaultValue());
            }
        }

        eNode.setAttribute("minOccurs", String.valueOf(bbie.cardinality().min()));
        if (bbie.cardinality().max() == -1)
            eNode.setAttribute("maxOccurs", "unbounded");
        else
            eNode.setAttribute("maxOccurs", String.valueOf(bbie.cardinality().max()));
        if (bbie.nillable())
            eNode.setAttribute("nillable", String.valueOf(bbie.nillable()));

        setDocumentation(eNode, bbie);

        return eNode;
    }

    public Element handleAttributeBBIE(BbieSummaryRecord bbie, Element eNode) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.den(), false));
        if (option.isBieGuid()) {
            eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + bbie.getGuid());
        }

        // Attribute 'nillable' is not allowed in element <xsd:attribute>
//        if (bbie.nillable()) {
//            eNode.setAttribute("nillable", "true");
//        }
        if (bbie.valueConstraint() != null) {
            if (hasLength(bbie.valueConstraint().fixedValue())) {
                eNode.setAttribute("fixed", bbie.valueConstraint().fixedValue());
            } else if (hasLength(bbie.valueConstraint().defaultValue())) {
                eNode.setAttribute("default", bbie.valueConstraint().defaultValue());
            }
        }
        if (bbie.cardinality().min() >= 1)
            eNode.setAttribute("use", "required");
        else
            eNode.setAttribute("use", "optional");

        setDocumentation(eNode, bbie);

        return eNode;
    }

    private void setDocumentation(Element node, BbieSummaryRecord bbie) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);
        AccSummaryRecord fromAcc = generationContext.getAcc(bcc.fromAccManifestId());

        setDefinition(node, bbie.definition(), getNamespace(generationContext, fromAcc.namespaceId()));
        setOptionalDocumentation(node, bbie, bcc);

        TopLevelAsbiepSummaryRecord topLevelAsbiep = generationContext.findTopLevelAsbiep(bbie.ownerTopLevelAsbiepId());
        BbiepSummaryRecord bbiep = generationContext.findBBIEP(bbie.toBbiepId(), topLevelAsbiep);
        BccpSummaryRecord bccp = generationContext.getBccp(bbiep.basedBccpManifestId());

        setDefinition(node, bbiep.definition(), getNamespace(generationContext, bccp.namespaceId()));
        setOptionalDocumentation(node, bbiep, bccp);
    }

    public Element setBBIEType(BbieSummaryRecord bbie, DtSummaryRecord bdt, Element gNode) {
        XbtSummaryRecord xbt = generationContext.getXbt(bbie);
        String typeName = null;
        if (hasAnyValuesInFacets(bbie.facet())) {
            typeName = addXbtSimpleType(xbt, bbie.facet(), "type_" + bbie.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }

        gNode.setAttribute("type", (typeName != null) ? typeName : xbt.builtInType());

        return gNode;
    }

    public String addXbtSimpleType(XbtSummaryRecord xbt, Facet facet, String name) {
        if (xbt == null) {
            return null;
        }
        if (rootElementNode == null) {
            return null;
        }

        Element xbtNode = newElement("simpleType");
        xbtNode.setAttribute("name", name);
        if (option.isBieGuid()) {
            xbtNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
        }

        Element restrictionNode = newElement("restriction");
        String builtInType = xbt.builtInType();
        if (!builtInType.startsWith(XSD_NAMESPACE.getPrefix())) {
            builtInType = XSD_NAMESPACE.getPrefix() + ":" + "token";
        }
        restrictionNode.setAttribute("base", builtInType);
        xbtNode.addContent(restrictionNode);

        if (facet.minLength() != null) {
            Element minLengthNode = newElement("minLength");
            minLengthNode.setAttribute("value", facet.minLength().toString());
            restrictionNode.addContent(minLengthNode);
        }
        if (facet.maxLength() != null) {
            Element maxLengthNode = newElement("maxLength");
            maxLengthNode.setAttribute("value", facet.maxLength().toString());
            restrictionNode.addContent(maxLengthNode);
        }
        if (StringUtils.hasLength(facet.pattern())) {
            Element patternNode = newElement("pattern");
            patternNode.setAttribute("value", facet.pattern());
            restrictionNode.addContent(patternNode);
        }

        schemaNode.addContent(xbtNode);

        return name;
    }

    public void addXbtSimpleType(XbtSummaryRecord xbt) {
        /*
         * Issue #521
         * If XBT has a value of schema content, it is not XML Schema Built-in Type.
         * It should be generated as the XML Schema simple type at the global level.
         */
        if (xbt == null || !StringUtils.hasLength(xbt.schemaDefinition())) {
            return;
        }
        if (rootElementNode == null) {
            return;
        }
        if (isProcessed(xbt)) {
            return;
        }

        String name = xbt.builtInType();

        Element xbtNode = newElement("simpleType");
        xbtNode.setAttribute("name", name);
        if (option.isBieGuid()) {
            xbtNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
        }

        try {
            StringBuilder sb = new StringBuilder();
            // To read the content, it must has defined 'xsd:schema' as a parent.
            sb.append("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "xmlns=\"http://www.openapplications.org/oagis/10\">");
            sb.append(xbt.schemaDefinition());
            sb.append("</xsd:schema>");

            Element content = new SAXBuilder().build(
                    new StringReader(sb.toString())).getRootElement();
            for (Content child : content.removeContent()) {
                xbtNode.addContent(child);
            }

            schemaNode.addContent(xbtNode);
        } catch (JDOMException | IOException e) {
            throw new IllegalStateException("Error occurs while the schema content loads for " + name, e);
        }

        setProcessedElement(xbt, xbtNode);
    }

    public Element generateBBIE(BbieSummaryRecord bbie, DtSummaryRecord bdt, Element parent) {
        BccSummaryRecord bcc = generationContext.queryBasedBCC(bbie);

        Element eNode = newElement("element");
        eNode = handleElementBBIE(bbie, eNode);

        if (bcc.entityType() == EntityType.Element) {
            while (!parent.getName().equals("sequence") && !parent.getName().equals("choice")) {
                parent = parent.getParentElement();
            }
            parent.addContent(eNode);

            List<BbieScSummaryRecord> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeListSummaryRecord codeList = generationContext.getCodeList(bbie.primitiveRestriction().codeListManifestId());
            if (codeList == null) {
                AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbie.primitiveRestriction().agencyIdListManifestId());
                if (agencyIdList != null) {
                    if (option.isBieGuid()) {
                        eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
                    }

                    AgencyIdListValueSummaryRecord agencyIdListValue =
                            generationContext.findAgencyIdListValue(agencyIdList.agencyIdListValueManifestId());
                    String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);

                    generateAgencyList(agencyIdList, agencyListTypeName);
                    if (StringUtils.hasLength(agencyListTypeName)) {
                        eNode.setAttribute("type", agencyListTypeName);
                    }
                } else {
                    if (bbie.primitiveRestriction().xbtManifestId() == null) {
                        if (bbieScList.isEmpty()) {
                            eNode = setBBIEType(bbie, bdt, eNode);
                        } else {
                            eNode = generateBDT(bbie, eNode);
                            eNode = generateSCs(bbie, eNode, bbieScList);
                        }
                    } else {
                        if (bbieScList.isEmpty()) {
                            eNode = setBBIEType(bbie, bdt, eNode);
                        } else {
                            eNode = generateBDT(bbie, eNode);
                            eNode = generateSCs(bbie, eNode, bbieScList);
                        }
                    }
                }
            } else {
                if (option.isBieGuid()) {
                    eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
                }
                generateCodeList(codeList, bbie);

                if (bbieScList.isEmpty()) {
                    AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
                    eNode.setAttribute("type", Helper.getCodeListTypeName(codeList, agencyIdListValue));
                } else {
                    eNode = generateBDT(bbie, eNode, codeList);
                    eNode = generateSCs(bbie, eNode, bbieScList);
                }
            }
        } else {
            List<BbieScSummaryRecord> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeListSummaryRecord codeList = generationContext.getCodeList(bbie.primitiveRestriction().codeListManifestId());
            if (codeList == null) {
                AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbie);
                if (agencyIdList != null) {
                    eNode = createAttributeNodeForBBIE(bbie, parent);

                    if (option.isBieGuid()) {
                        eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
                    }

                    AgencyIdListValueSummaryRecord agencyIdListValue =
                            generationContext.findAgencyIdListValue(agencyIdList.agencyIdListValueManifestId());
                    String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);

                    generateAgencyList(agencyIdList, agencyListTypeName);
                    if (StringUtils.hasLength(agencyListTypeName)) {
                        eNode.setAttribute("type", agencyListTypeName);
                    }
                } else {
                    if (bbie.primitiveRestriction().xbtManifestId() == null) {
                        if (bbieScList.isEmpty()) {
                            eNode = createAttributeNodeForBBIE(bbie, parent);
                            eNode = setBBIE_Attr_Type(bdt, eNode);
                        } else {
                            eNode = generateBDT(bbie, eNode);
                        }
                    } else {
                        if (bbieScList.isEmpty()) {
                            eNode = createAttributeNodeForBBIE(bbie, parent);
                            eNode = setBBIE_Attr_Type(bbie, bdt, eNode);
                        } else {
                            eNode = generateBDT(bbie, eNode);
                        }
                    }
                }
            } else {
                eNode = createAttributeNodeForBBIE(bbie, parent);

                if (option.isBieGuid()) {
                    eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
                }
                generateCodeList(codeList, bbie);

                if (bbieScList.isEmpty()) {
                    AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
                    String codeListTypeName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
                    if (StringUtils.hasLength(codeListTypeName)) {
                        eNode.setAttribute("type", codeListTypeName);
                    }
                    return eNode;
                } else {
                    if (bbie.primitiveRestriction().xbtManifestId() == null) {
                        eNode = setBBIE_Attr_Type(bdt, eNode);
                    } else {
                        AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
                        String codeListTypeName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
                        if (StringUtils.hasLength(codeListTypeName)) {
                            eNode.setAttribute("type", codeListTypeName);
                        }
                    }
                }
            }
        }

        return eNode;
    }

    private Element createAttributeNodeForBBIE(BbieSummaryRecord bbie, Element parent) {
        Element eNode = newElement("attribute");
        eNode = handleAttributeBBIE(bbie, eNode);

        while (!parent.getName().equals("complexType")) {
            parent = parent.getParentElement();
        }

        parent.addContent(eNode);

        return eNode;
    }

    public String setCodeListRestrictionAttr(BbieSummaryRecord bbie) {
        DtAwdPriSummaryRecord dtAwdPri =
                generationContext.findDtAwdPriByBbieAndDefaultIsTrue(bbie);
        if (dtAwdPri.codeListManifestId() != null) {
            return "xsd:token";
        } else {
            XbtSummaryRecord xbt = generationContext.getXbt(dtAwdPri.xbtManifestId());
            addXbtSimpleType(xbt);
            return xbt.builtInType();
        }
    }

    public String setCodeListRestrictionAttr(BbieScSummaryRecord bbieSc) {
        DtScAwdPriSummaryRecord dtScAwdPri =
                generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);
        if (dtScAwdPri.codeListManifestId() != null) {
            return "xsd:token";
        } else {
            XbtSummaryRecord xbt = generationContext.getXbt(dtScAwdPri.xbtManifestId());
            addXbtSimpleType(xbt);
            return xbt.builtInType();
        }
    }

    public Element generateCodeList(CodeListSummaryRecord codeList, BbieSummaryRecord bbie) {
        return generateCodeList(codeList, setCodeListRestrictionAttr(bbie));
    }

    public Element generateCodeList(CodeListSummaryRecord codeList, BbieScSummaryRecord bbieSc) {
        return generateCodeList(codeList, setCodeListRestrictionAttr(bbieSc));
    }

    private Element generateCodeList(CodeListSummaryRecord codeList, String codeListRestrictionAttr) {
        if (isProcessed(codeList)) {
            return getProcessedElement(codeList);
        }

        Element stNode = newElement("simpleType");

        AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
        stNode.setAttribute("name", Helper.getCodeListTypeName(codeList, agencyIdListValue));
        if (option.isBieGuid()) {
            stNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + codeList.guid());
        }

        Element rtNode = newElement("restriction");
        rtNode.setAttribute("base", codeListRestrictionAttr);
        stNode.addContent(rtNode);

        for (CodeListValueSummaryRecord codeListValue : generationContext.getCodeListValues(codeList)) {
            Element enumeration = newElement("enumeration");
            enumeration.setAttribute("value", codeListValue.value());
            rtNode.addContent(enumeration);
        }

        schemaNode.addContent(stNode);
        setProcessedElement(codeList, stNode);

        return stNode;
    }

    public Element handleBBIESCvalue(BbieScSummaryRecord bbieSc, Element aNode) {
        //Handle gSC[i]
        if (bbieSc.valueConstraint() != null) {
            if (hasLength(bbieSc.valueConstraint().fixedValue())) {
                aNode.setAttribute("fixed", bbieSc.valueConstraint().fixedValue());
            } else if (hasLength(bbieSc.valueConstraint().defaultValue())) {
                aNode.setAttribute("default", bbieSc.valueConstraint().defaultValue());
            }
        }
        // Generate a DOM Attribute node
        /*
         * Section 3.8.1.22 GenerateSCs #2
         */
        DtScSummaryRecord dtSc = generationContext.getDtSc(bbieSc.basedDtScManifestId());
        DtSummaryRecord dt = generationContext.getDt(dtSc.ownerDtManifestId());
        String representationTerm = dtSc.representationTerm();
        String propertyTerm = dtSc.propertyTerm();
        aNode.setAttribute("name", toName(propertyTerm, representationTerm, rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            if ("Identifier".equals(rt)) {
                return "ID";
            }
            return rt;
        }, false));

        if (bbieSc.cardinality().min() >= 1) {
            aNode.setAttribute("use", "required");
        } else {
            aNode.setAttribute("use", "optional");
        }

        if (option.isBieGuid()) {
            aNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + bbieSc.getGuid());
        }
        setDefinition(aNode, bbieSc.definition(), getNamespace(generationContext, dt.namespaceId()));

        BBIESCDocumentation bieDocumentation =
                new BBIESCDocumentation(bbieSc, dtSc, dt);
        setOptionalDocumentation(aNode, bieDocumentation);

        return aNode;
    }

    public Element setBBIESCType(BbieScSummaryRecord bbieSc, DtScSummaryRecord bdtSc, Element gNode) {
        DtScSummaryRecord dtSc = generationContext.getDtSc(bbieSc.basedDtScManifestId());
        if (dtSc != null) {
            DtScAwdPriSummaryRecord dtScAwdPri =
                    generationContext.findDtScAwdPriByBbieScAndDefaultIsTrue(bbieSc);
            if (dtScAwdPri != null) {
                if (dtScAwdPri.xbtManifestId() != null) {
                    XbtSummaryRecord xbt = generationContext.getXbt(dtScAwdPri.xbtManifestId());

                    String typeName = null;
                    if (hasAnyValuesInFacets(bbieSc.facet())) {
                        typeName = addXbtSimpleType(xbt, bbieSc.facet(), "type_" + bbieSc.getGuid());
                    } else {
                        addXbtSimpleType(xbt);
                    }

                    gNode.setAttribute("type", (typeName != null) ? typeName : xbt.builtInType());
                }
            }
        }
        return gNode;
    }

    public Element setBBIESCType2(BbieScSummaryRecord bbieSc, DtScSummaryRecord bdtSc, Element gNode) {
        XbtSummaryRecord xbt = generationContext.getXbt(bbieSc.primitiveRestriction().xbtManifestId());

        String typeName = null;
        if (hasAnyValuesInFacets(bbieSc.facet())) {
            typeName = addXbtSimpleType(xbt, bbieSc.facet(), "type_" + bbieSc.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }

        gNode.setAttribute("type", (typeName != null) ? typeName : xbt.builtInType());

        return gNode;

    }

    public Element generateSCs(BbieSummaryRecord bbie, Element bbieElement,
                               List<BbieScSummaryRecord> bbieScList) {
        Element tNode = bbieElement;
        while (true) {
            if (tNode.getName().equals("simpleType") || tNode.getName().equals("complexType"))
                break;
            List<Element> children = tNode.getChildren();
            tNode = children.get(children.size() - 1);
        }
        List<Element> children = tNode.getChildren();
        for (Element child : children) {
            if (child.getName().equals("simpleContent")) {
                tNode = child.getChildren().get(0);
                break;
            }
        }

        for (int i = 0; i < bbieScList.size(); i++) {
            BbieScSummaryRecord bbieSc = bbieScList.get(i);
            if (bbieSc.cardinality().max() == 0)
                continue;
            Element aNode = newElement("attribute");
            aNode = handleBBIESCvalue(bbieSc, aNode); //Generate a DOM Element Node, handle values

            //Get a code list object
            CodeListSummaryRecord codeList = generationContext.getCodeList(bbieSc);
            if (codeList == null) {
                AgencyIdListSummaryRecord agencyIdList = generationContext.getAgencyIdList(bbieSc);

                if (agencyIdList == null) {
                    DtScSummaryRecord dtSc = generationContext.getDtSc(bbieSc.basedDtScManifestId());

                    if (bbieSc.primitiveRestriction().xbtManifestId() == null)
                        aNode = setBBIESCType(bbieSc, dtSc, aNode);
                    else
                        aNode = setBBIESCType2(bbieSc, dtSc, aNode);
                } else {
                    if (option.isBieGuid()) {
                        aNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
                    }

                    AgencyIdListValueSummaryRecord agencyIdListValue =
                            generationContext.findAgencyIdListValue(agencyIdList.agencyIdListValueManifestId());
                    String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);

                    generateAgencyList(agencyIdList, agencyListTypeName);
                    if (StringUtils.hasLength(agencyListTypeName)) {
                        aNode.setAttribute("type", agencyListTypeName);
                    }
                }
            } else {
                if (option.isBieGuid()) {
                    aNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuidUtils.randomGuid());
                }

                generateCodeList(codeList, bbieSc);

                AgencyIdListValueSummaryRecord agencyIdListValue = generationContext.findAgencyIdListValue(codeList.agencyIdListValueManifestId());
                String codeListTypeName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
                if (StringUtils.hasLength(codeListTypeName)) {
                    aNode.setAttribute("type", codeListTypeName);
                }
            }

            tNode.addContent(aNode);
        }
        return tNode;
    }

    public Element generateAgencyList(AgencyIdListSummaryRecord agencyIdList, String agencyListTypeName) {
        if (isProcessed(agencyIdList)) {
            return getProcessedElement(agencyIdList);
        }

        Element stNode = newElement("simpleType");

        stNode.setAttribute("name", agencyListTypeName);
        if (option.isBieGuid()) {
            stNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + agencyIdList.guid());
        }

        Element rtNode = newElement("restriction");

        rtNode.setAttribute("base", "xsd:token");
        stNode.addContent(rtNode);

        List<AgencyIdListValueSummaryRecord> agencyIdListValues =
                generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.agencyIdListManifestId());

        Namespace namespace = getNamespace(generationContext, agencyIdList.namespaceId());

        for (AgencyIdListValueSummaryRecord agencyIdListValue : agencyIdListValues) {
            Element enumeration = newElement("enumeration");
            rtNode.addContent(enumeration);
            enumeration.setAttribute("value", agencyIdListValue.value());

            Element annotation = newElement("annotation");
            Element documentation = newElement("documentation");
            documentation.setAttribute("source", namespace.getURI());

            Element cctsName = new Element("ccts_Name", namespace);
            String name = agencyIdListValue.name();
            cctsName.setText(name);
            documentation.addContent(cctsName);
            if (agencyIdListValue.definition() != null) {
                Element cctsDefinition = new Element("ccts_Definition", namespace);
                cctsDefinition.setText(agencyIdListValue.definition().content());
                documentation.addContent(cctsDefinition);
            }

            annotation.addContent(documentation);
            enumeration.addContent(annotation);
        }

        schemaNode.addContent(stNode);
        setProcessedElement(agencyIdList, stNode);
        return stNode;
    }

    private interface BIEMetaData {
        String getEntityTypeCode();

        String getDictionaryEntryName();

        Collection<Definition> getDefinitions();

        String getObjectClassTermName();

        String getPropertyTermName();

        String getRepresentationTermName();

        String getDataTypeTermName();

        String getBusinessTerm();
    }

    private interface SRTMetaData {
        String getReleaseNumber();

        String getVersion();

        String getStateCode();

        String getStatus();

        String getRemark();

        String getCreatedUserName();

        String getLastUpdatedUserName();

        String getOwnerUserName();

        Date getCreationTimestamp();

        Date getLastUpdatedTimestamp();
    }

    private interface CCMetaData {
        CcType ccType();

        String guid();

        int getRevisionNumber();

        NamespaceId getNamespaceId();

        Collection<Definition> getCoreComponentDefinitions();
    }

    private abstract class AbstractBIEDocumentation implements BIEMetaData, SRTMetaData, CCMetaData {

        @Override
        public String getDictionaryEntryName() {
            return null;
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public String getObjectClassTermName() {
            return null;
        }

        @Override
        public String getPropertyTermName() {
            return null;
        }

        @Override
        public String getRepresentationTermName() {
            return null;
        }

        @Override
        public String getDataTypeTermName() {
            return null;
        }

        @Override
        public String getBusinessTerm() {
            return null;
        }

        @Override
        public String getReleaseNumber() {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public String getStateCode() {
            return null;
        }

        @Override
        public String getStatus() {
            return null;
        }

        @Override
        public String getRemark() {
            return null;
        }

        @Override
        public String getCreatedUserName() {
            return null;
        }

        @Override
        public String getLastUpdatedUserName() {
            return null;
        }

        @Override
        public String getOwnerUserName() {
            return null;
        }

        @Override
        public Date getCreationTimestamp() {
            return null;
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return null;
        }

        @Override
        public String guid() {
            return null;
        }

        @Override
        public int getRevisionNumber() {
            return 0;
        }

        @Override
        public NamespaceId getNamespaceId() {
            return null;
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Collections.emptyList();
        }
    }

    private class ABIEDocumentation extends AbstractBIEDocumentation {
        private final TopLevelAsbiepSummaryRecord topLevelAsbiep;
        private final AbieSummaryRecord abie;
        private final AccSummaryRecord acc;

        public ABIEDocumentation(
                TopLevelAsbiepSummaryRecord topLevelAsbiep,
                AbieSummaryRecord abie,
                AccSummaryRecord acc) {
            this.topLevelAsbiep = topLevelAsbiep;
            this.abie = abie;
            this.acc = acc;
        }

        @Override
        public String getEntityTypeCode() {
            return "ABIE";
        }

        @Override
        public String getDictionaryEntryName() {
            return acc.den();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(abie.definition(), null));
        }

        @Override
        public String getObjectClassTermName() {
            return acc.objectClassTerm();
        }

        @Override
        public String getVersion() {
            return topLevelAsbiep.version();
        }

        @Override
        public String getStatus() {
            return topLevelAsbiep.status();
        }

        @Override
        public String getBusinessTerm() {
            return abie.bizTerm();
        }

        @Override
        public String getRemark() {
            return abie.remark();
        }

        @Override
        public String getCreatedUserName() {
            return abie.created().who().loginId();
        }

        @Override
        public String getLastUpdatedUserName() {
            return abie.lastUpdated().who().loginId();
        }

        @Override
        public Date getCreationTimestamp() {
            return abie.created().when();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return abie.lastUpdated().when();
        }

        @Override
        public CcType ccType() {
            return CcType.ACC;
        }

        @Override
        public String guid() {
            return acc.guid().value();
        }

        @Override
        public int getRevisionNumber() {
            return acc.revisionNum();
        }

        @Override
        public NamespaceId getNamespaceId() {
            return acc.namespaceId();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return (acc.definition() != null) ? Arrays.asList(acc.definition()) : Collections.emptyList();
        }
    }

    private class ASBIEDocumentation extends AbstractBIEDocumentation {
        private final AsbieSummaryRecord asbie;
        private final AsccSummaryRecord ascc;
        private final AccSummaryRecord fromAcc;

        public ASBIEDocumentation(
                AsbieSummaryRecord asbie,
                AsccSummaryRecord ascc,
                AccSummaryRecord fromAcc) {
            this.asbie = asbie;
            this.ascc = ascc;
            this.fromAcc = fromAcc;
        }

        @Override
        public String getEntityTypeCode() {
            return "ASBIE";
        }

        @Override
        public String getDictionaryEntryName() {
            return ascc.den();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(asbie.definition(), null));
        }

        @Override
        public String getRemark() {
            return asbie.remark();
        }

        @Override
        public String getCreatedUserName() {
            return asbie.created().who().loginId();
        }

        @Override
        public String getLastUpdatedUserName() {
            return asbie.lastUpdated().who().loginId();
        }

        @Override
        public Date getCreationTimestamp() {
            return asbie.created().when();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return asbie.lastUpdated().when();
        }

        @Override
        public CcType ccType() {
            return CcType.ASCC;
        }

        @Override
        public String guid() {
            return ascc.guid().value();
        }

        @Override
        public int getRevisionNumber() {
            return ascc.revisionNum();
        }

        @Override
        public NamespaceId getNamespaceId() {
            return fromAcc.namespaceId();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return (ascc.definition() != null) ? Arrays.asList(ascc.definition()) : Collections.emptyList();
        }
    }

    private class ASBIEPDocumentation extends AbstractBIEDocumentation {
        private final AsbiepSummaryRecord asbiep;
        private final AsccpSummaryRecord asccp;
        private final TopLevelAsbiepSummaryRecord topLevelAsbiep;

        public ASBIEPDocumentation(
                AsbiepSummaryRecord asbiep,
                AsccpSummaryRecord asccp) {
            this(asbiep, asccp, null);
        }

        public ASBIEPDocumentation(
                AsbiepSummaryRecord asbiep,
                AsccpSummaryRecord asccp,
                TopLevelAsbiepSummaryRecord topLevelAsbiep) {
            this.asbiep = asbiep;
            this.asccp = asccp;
            this.topLevelAsbiep = topLevelAsbiep;
        }

        @Override
        public String getEntityTypeCode() {
            return "ASBIEP";
        }

        @Override
        public String getDictionaryEntryName() {
            return asccp.den();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(asbiep.definition(), null));
        }

        @Override
        public String getPropertyTermName() {
            return asccp.propertyTerm();
        }

        @Override
        public String getReleaseNumber() {
            if (topLevelAsbiep != null) {
                return generationContext.findReleaseNumber(topLevelAsbiep.release().releaseId());
            }
            return null;
        }

        @Override
        public String getStateCode() {
            if (topLevelAsbiep != null) {
                return topLevelAsbiep.state().name();
            }
            return null;
        }

        @Override
        public String getOwnerUserName() {
            if (topLevelAsbiep != null) {
                return generationContext.findUserName(topLevelAsbiep.owner().userId());
            }
            return null;
        }

        @Override
        public String getRemark() {
            return asbiep.remark();
        }

        @Override
        public String getCreatedUserName() {
            return asbiep.created().who().loginId();
        }

        @Override
        public String getLastUpdatedUserName() {
            return asbiep.lastUpdated().who().loginId();
        }

        @Override
        public Date getCreationTimestamp() {
            return asbiep.created().when();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return asbiep.lastUpdated().when();
        }

        @Override
        public CcType ccType() {
            return CcType.ASCCP;
        }

        @Override
        public String guid() {
            return asccp.guid().value();
        }

        @Override
        public int getRevisionNumber() {
            return asccp.revisionNum();
        }

        @Override
        public NamespaceId getNamespaceId() {
            return asccp.namespaceId();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return (asccp.definition() != null) ? Arrays.asList(asccp.definition()) : Collections.emptyList();
        }
    }

    private class BBIEDocumentation extends AbstractBIEDocumentation {
        private final BbieSummaryRecord bbie;
        private final BccSummaryRecord bcc;
        private final AccSummaryRecord fromAcc;

        public BBIEDocumentation(
                BbieSummaryRecord bbie,
                BccSummaryRecord bcc,
                AccSummaryRecord fromAcc) {
            this.bbie = bbie;
            this.bcc = bcc;
            this.fromAcc = fromAcc;
        }

        @Override
        public String getEntityTypeCode() {
            return "BBIE";
        }

        @Override
        public String getDictionaryEntryName() {
            return bcc.den();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(bbie.definition(), null));
        }

        @Override
        public String getRemark() {
            return bbie.remark();
        }

        @Override
        public String getCreatedUserName() {
            return bbie.created().who().loginId();
        }

        @Override
        public String getLastUpdatedUserName() {
            return bbie.lastUpdated().who().loginId();
        }

        @Override
        public Date getCreationTimestamp() {
            return bbie.created().when();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return bbie.lastUpdated().when();
        }

        @Override
        public CcType ccType() {
            return CcType.BCC;
        }

        @Override
        public String guid() {
            return bcc.guid().value();
        }

        @Override
        public int getRevisionNumber() {
            return bcc.revisionNum();
        }

        @Override
        public NamespaceId getNamespaceId() {
            return fromAcc.namespaceId();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return (bcc.definition() != null) ? Arrays.asList(bcc.definition()) : Collections.emptyList();
        }
    }

    private class BBIEPDocumentation extends AbstractBIEDocumentation {
        private final BbiepSummaryRecord bbiep;
        private final BccpSummaryRecord bccp;

        public BBIEPDocumentation(
                BbiepSummaryRecord bbiep,
                BccpSummaryRecord bccp) {
            this.bbiep = bbiep;
            this.bccp = bccp;
        }

        @Override
        public String getEntityTypeCode() {
            return "BBIEP";
        }

        @Override
        public String getDictionaryEntryName() {
            return bccp.den();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(bbiep.definition(), null));
        }

        @Override
        public String getPropertyTermName() {
            return bccp.propertyTerm();
        }

        @Override
        public String getRepresentationTermName() {
            return bccp.representationTerm();
        }

        @Override
        public String getRemark() {
            return bbiep.remark();
        }

        @Override
        public String getCreatedUserName() {
            return bbiep.created().who().loginId();
        }

        @Override
        public String getLastUpdatedUserName() {
            return bbiep.lastUpdated().who().loginId();
        }

        @Override
        public Date getCreationTimestamp() {
            return bbiep.created().when();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return bbiep.lastUpdated().when();
        }

        @Override
        public CcType ccType() {
            return CcType.BCCP;
        }

        @Override
        public String guid() {
            return bccp.guid().value();
        }

        @Override
        public int getRevisionNumber() {
            return bccp.revisionNum();
        }

        @Override
        public NamespaceId getNamespaceId() {
            return bccp.namespaceId();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return (bccp.definition() != null) ? Arrays.asList(bccp.definition()) : Collections.emptyList();
        }
    }

    private class BBIESCDocumentation extends AbstractBIEDocumentation {
        private final BbieScSummaryRecord bbieSc;
        private final DtScSummaryRecord dtSc;
        private final DtSummaryRecord dt;

        public BBIESCDocumentation(
                BbieScSummaryRecord bbieSc,
                DtScSummaryRecord dtSc,
                DtSummaryRecord dt) {
            this.bbieSc = bbieSc;
            this.dtSc = dtSc;
            this.dt = dt;
        }

        @Override
        public String getEntityTypeCode() {
            return "SC";
        }

        @Override
        public String getDictionaryEntryName() {
            return dtSc.den();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(bbieSc.definition(), null));
        }

        @Override
        public String getPropertyTermName() {
            return dtSc.propertyTerm();
        }

        @Override
        public String getRepresentationTermName() {
            return dtSc.representationTerm();
        }

        @Override
        public String getRemark() {
            return bbieSc.remark();
        }

        @Override
        public CcType ccType() {
            return CcType.DT_SC;
        }

        @Override
        public String guid() {
            return dtSc.guid().value();
        }

        @Override
        public int getRevisionNumber() {
            return dtSc.revisionNum();
        }

        @Override
        public NamespaceId getNamespaceId() {
            return dt.namespaceId();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return (dtSc.definition() != null) ? Arrays.asList(dtSc.definition()) : Collections.emptyList();
        }
    }

    private class BusinessDataTypeDocumentation extends AbstractBIEDocumentation {
        private final DtSummaryRecord dt;

        public BusinessDataTypeDocumentation(DtSummaryRecord dt) {
            this.dt = dt;
        }

        @Override
        public String getEntityTypeCode() {
            return "BDT";
        }

        @Override
        public String getDictionaryEntryName() {
            return dt.den();
        }

        @Override
        public String getDataTypeTermName() {
            return dt.dataTypeTerm();
        }

        @Override
        public CcType ccType() {
            return CcType.DT;
        }

        @Override
        public String guid() {
            return dt.guid().value();
        }

        @Override
        public int getRevisionNumber() {
            return dt.revisionNum();
        }

        @Override
        public NamespaceId getNamespaceId() {
            return dt.namespaceId();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return (dt.definition() != null) ? Arrays.asList(dt.definition()) : Collections.emptyList();
        }
    }
}
