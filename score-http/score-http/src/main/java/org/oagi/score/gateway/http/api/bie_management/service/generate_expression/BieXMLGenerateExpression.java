package org.oagi.score.gateway.http.api.bie_management.service.generate_expression;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.score.common.util.OagisComponentType;
import org.oagi.score.data.*;
import org.oagi.score.gateway.http.api.bie_management.data.expression.GenerateExpressionOption;
import org.oagi.score.gateway.http.api.cc_management.data.CcType;
import org.oagi.score.gateway.http.api.namespace_management.data.NamespaceList;
import org.oagi.score.gateway.http.helper.ScoreGuid;
import org.oagi.score.gateway.http.helper.Utility;
import org.oagi.score.service.common.data.BCCEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.*;

import static org.oagi.score.gateway.http.helper.Utility.toZuluTimeString;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class BieXMLGenerateExpression implements BieGenerateExpression, InitializingBean {

    private static final String OAGI_NS = "http://www.openapplications.org/oagis/10";
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
        this.schemaNode = generateSchema(document);
    }

    private Namespace getNamespace(GenerationContext generationContext, BigInteger namespaceId) {
        NamespaceList namespaceList = generationContext.findNamespace(namespaceId);
        if (namespaceList == null) {
            return null;
        }
        return org.jdom2.Namespace.getNamespace(namespaceList.getPrefix(), namespaceList.getUri());
    }

    private Element generateSchema(Document doc) {
        Element schemaNode = newElement("schema");
        schemaNode.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("", OAGI_NS));
        schemaNode.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace"));
        schemaNode.setAttribute("targetNamespace", OAGI_NS);
        schemaNode.setAttribute("elementFormDefault", "qualified");
        schemaNode.setAttribute("attributeFormDefault", "unqualified");
        doc.addContent(schemaNode);
        return schemaNode;
    }

    private Element newElement(String localName) {
        return new Element(localName, XSD_NAMESPACE);
    }

    @Override
    public GenerationContext generateContext(List<TopLevelAsbiep> topLevelAsbieps, GenerateExpressionOption option) {
        return applicationContext.getBean(GenerationContext.class, topLevelAsbieps);
    }

    @Override
    public void reset() {
        this.afterPropertiesSet();
    }

    @Override
    public void generate(TopLevelAsbiep topLevelAsbiep,
                         GenerationContext generationContext,
                         GenerateExpressionOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelAsbiep(topLevelAsbiep);
    }

    @Override
    public File asFile(String filename) throws IOException {
        File tempFile = File.createTempFile(ScoreGuid.randomGuid(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".xsd");

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            outputter.output(this.document, outputStream);
            outputStream.flush();
        }

        logger.info("XML Schema is generated: " + tempFile);

        return tempFile;
    }

    private void generateTopLevelAsbiep(TopLevelAsbiep topLevelAsbiep) {
        ASBIEP asbiep = generationContext.findASBIEP(topLevelAsbiep.getAsbiepId(), topLevelAsbiep);
        generationContext.referenceCounter().increase(asbiep);
        try {
            logger.debug("Generating Top Level ABIE w/ given ASBIEP Id: " + asbiep.getAsbiepId());

            rootElementNode = generateTopLevelASBIEP(asbiep, topLevelAsbiep);

            ABIE abie = generationContext.queryTargetABIE(asbiep);
            Element rootSeqNode = generateABIE(abie, rootElementNode);
            generateBIEs(abie, rootSeqNode);
            if (rootSeqNode.getChildren().isEmpty()) {
                rootSeqNode.detach();
            }
        } finally {
            generationContext.referenceCounter().decrease(asbiep);
        }
    }

    private String key(BIE bie) {
        return bie.getClass().getSimpleName() + ID_ATTRIBUTE_PREFIX + bie.getGuid();
    }

    private String key(Xbt xbt) {
        return "XBT" + xbt.getName();
    }

    private String key(CodeList codeList) {
        return "CodeList" + ID_ATTRIBUTE_PREFIX + codeList.getGuid();
    }

    private String key(AgencyIdList agencyIdList) {
        return "AgencyIdList" + ID_ATTRIBUTE_PREFIX + agencyIdList.getGuid();
    }

    private boolean isProcessed(BIE bie) {
        return processedElements.containsKey(key(bie));
    }

    private boolean isProcessed(Xbt xbt) {
        return processedElements.containsKey(key(xbt));
    }

    private boolean isProcessed(CodeList codeList) {
        return processedElements.containsKey(key(codeList));
    }

    private boolean isProcessed(AgencyIdList agencyIdList) {
        return processedElements.containsKey(key(agencyIdList));
    }

    private Element getProcessedElement(BIE bie) {
        return processedElements.get(key(bie));
    }

    private Element getProcessedElement(CodeList codeList) {
        return processedElements.get(key(codeList));
    }

    private Element getProcessedElement(AgencyIdList agencyIdList) {
        return processedElements.get(key(agencyIdList));
    }

    private Element setProcessedElement(BIE bie, Element element) {
        processedElements.put(key(bie), element);
        return element;
    }

    private Element setProcessedElement(Xbt xbt, Element element) {
        processedElements.put(key(xbt), element);
        return element;
    }

    private Element setProcessedElement(CodeList codeList, Element element) {
        processedElements.put(key(codeList), element);
        return element;
    }

    private Element setProcessedElement(AgencyIdList agencyIdList, Element element) {
        processedElements.put(key(agencyIdList), element);
        return element;
    }

    private void setDefinition(Element node, String contextDefinition) {
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
        documentation.setAttribute("source", OAGI_NS);
        documentation.setText(definition);

        annotation.addContent(documentation);
    }

    private void setBusinessContext(Element node, TopLevelAsbiep topLevelAsbiep) {
        if (!option.isBusinessContext()) {
            return;
        }

        Element annotation = newElement("annotation");
        node.addContent(annotation);

        Element documentation = newElement("documentation");
        annotation.addContent(documentation);
        List<BizCtx> bizCtxList = generationContext.findBusinessContexts(topLevelAsbiep);

        for (BizCtx bizCtx : bizCtxList) {
            Element ccts_BusinessContext = new Element("ccts_BusinessContext", OAGI_NS);
            documentation.addContent(ccts_BusinessContext);

            Element ccts_BusinessContextGUID = new Element("ccts_GUID", OAGI_NS);
            ccts_BusinessContext.addContent(ccts_BusinessContextGUID);
            ccts_BusinessContextGUID.setText(bizCtx.getGuid());

            Element ccts_BusinessContextName = new Element("ccts_Name", OAGI_NS);
            ccts_BusinessContext.addContent(ccts_BusinessContextName);
            ccts_BusinessContextName.setText(bizCtx.getName());

            List<ContextSchemeValue> contextSchemeValues = generationContext.findContextSchemeValue(bizCtx);
            for (ContextSchemeValue contextSchemeValue : contextSchemeValues) {
                Element ccts_ContextValue = new Element("ccts_ContextValue", OAGI_NS);
                ccts_BusinessContext.addContent(ccts_ContextValue);

                Element ccts_ContextSchemeValueGUID = new Element("ccts_GUID", OAGI_NS);
                ccts_ContextValue.addContent(ccts_ContextSchemeValueGUID);
                ccts_ContextSchemeValueGUID.setText(contextSchemeValue.getGuid());

                Element ccts_ContextSchemeValue = new Element("ccts_Value", OAGI_NS);
                ccts_ContextValue.addContent(ccts_ContextSchemeValue);
                ccts_ContextSchemeValue.setText(contextSchemeValue.getValue());

                Element ccts_ContextSchemeValueMeaning = new Element("ccts_Meaning", OAGI_NS);
                ccts_ContextValue.addContent(ccts_ContextSchemeValueMeaning);
                ccts_ContextSchemeValueMeaning.setText(contextSchemeValue.getMeaning());

                ContextScheme contextScheme = generationContext.findContextScheme(contextSchemeValue.getOwnerCtxSchemeId());

                Element ccts_ClassificationScheme = new Element("ccts_ClassificationScheme", OAGI_NS);
                ccts_ContextValue.addContent(ccts_ClassificationScheme);

                Element ccts_ClassificationSchemeGUID = new Element("ccts_GUID", OAGI_NS);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeGUID);
                ccts_ClassificationSchemeGUID.setText(contextScheme.getGuid());

                {
                    ContextCategory contextCategory = generationContext.findContextCategory(contextScheme.getCtxCategoryId());

                    Element ccts_ContextCategory = new Element("ccts_ContextCategory", OAGI_NS);
                    ccts_ClassificationScheme.addContent(ccts_ContextCategory);

                    Element ccts_ContextCategoryGUID = new Element("ccts_GUID", OAGI_NS);
                    ccts_ContextCategory.addContent(ccts_ContextCategoryGUID);
                    ccts_ContextCategoryGUID.setText(contextCategory.getGuid());

                    Element ccts_ContextCategoryName = new Element("ccts_Name", OAGI_NS);
                    ccts_ContextCategory.addContent(ccts_ContextCategoryName);
                    ccts_ContextCategoryName.setText(contextCategory.getName());

                    Element ccts_ContextCategoryDefinition = new Element("ccts_Definition", OAGI_NS);
                    ccts_ContextCategory.addContent(ccts_ContextCategoryDefinition);
                    ccts_ContextCategoryDefinition.setText(contextCategory.getDescription());
                }

                Element ccts_ClassificationSchemeId = new Element("ccts_ID", OAGI_NS);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeId);
                ccts_ClassificationSchemeId.setText(contextScheme.getSchemeId());

                Element ccts_ClassificationSchemeName = new Element("ccts_Name", OAGI_NS);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeName);
                ccts_ClassificationSchemeName.setText(contextScheme.getSchemeName());

                Element ccts_ClassificationSchemeAgencyID = new Element("ccts_AgencyID", OAGI_NS);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeAgencyID);
                ccts_ClassificationSchemeAgencyID.setText(contextScheme.getSchemeAgencyId());

                Element ccts_ClassificationSchemeVersionID = new Element("ccts_VersionID", OAGI_NS);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeVersionID);
                ccts_ClassificationSchemeVersionID.setText(contextScheme.getSchemeVersionId());

                Element ccts_ClassificationSchemeDefinition = new Element("ccts_Definition", OAGI_NS);
                ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeDefinition);
                ccts_ClassificationSchemeDefinition.setText(contextScheme.getDescription());
            }
        }
    }

    private AbstractBIEDocumentation getBIEDocumentation(BIE bie, CoreComponent cc) {
        if (bie instanceof ABIE && cc instanceof ACC) {
            return new ABIEDocumentation(
                    (ABIE) bie,
                    (ACC) cc);
        }

        if (bie instanceof ASBIE && cc instanceof ASCC) {
            return new ASBIEDocumentation(
                    (ASBIE) bie, (ASCC) cc);
        }

        if (bie instanceof ASBIEP && cc instanceof ASCCP) {
            return new ASBIEPDocumentation(
                    (ASBIEP) bie,
                    (ASCCP) cc);
        }

        if (bie instanceof BBIE && cc instanceof BCC) {
            return new BBIEDocumentation(
                    (BBIE) bie, (BCC) cc);
        }

        if (bie instanceof BBIEP && cc instanceof BCCP) {
            return new BBIEPDocumentation(
                    (BBIEP) bie,
                    (BCCP) cc);
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

        if (bieCctsMetaData) {
            String entityTypeCode = bieDocumentation.getEntityTypeCode();
            Element ccts_BIEEntityTypeCode = new Element("ccts_BIEEntityTypeCode", OAGI_NS);
            documentation.addContent(ccts_BIEEntityTypeCode);
            ccts_BIEEntityTypeCode.setText(entityTypeCode);

            String dictionaryEntryName = bieDocumentation.getDictionaryEntryName();
            if (StringUtils.hasLength(dictionaryEntryName)) {
                Element ccts_DictionaryEntryName = new Element("ccts_DictionaryEntryName", OAGI_NS);
                documentation.addContent(ccts_DictionaryEntryName);
                ccts_DictionaryEntryName.setText(dictionaryEntryName);
            }

            if (option.isIncludeCctsDefinitionTag()) {
                for (Definition definition : bieDocumentation.getDefinitions()) {
                    if (StringUtils.hasLength(definition.getDefinition())) {
                        Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
                        documentation.addContent(ccts_Definition);
                        ccts_Definition.setText(definition.getDefinition());

                        if (StringUtils.hasLength(definition.getDefinitionSource())) {
                            ccts_Definition.setAttribute("source", definition.getDefinitionSource());
                        }
                    }
                }
            }

            String objectClassTermName = bieDocumentation.getObjectClassTermName();
            if (StringUtils.hasLength(objectClassTermName)) {
                Element ccts_ObjectClassTermName = new Element("ccts_ObjectClassTermName", OAGI_NS);
                documentation.addContent(ccts_ObjectClassTermName);
                ccts_ObjectClassTermName.setText(objectClassTermName);
            }

            String propertyTermName = bieDocumentation.getPropertyTermName();
            if (StringUtils.hasLength(propertyTermName)) {
                Element ccts_PropertyTermName = new Element("ccts_PropertyTermName", OAGI_NS);
                documentation.addContent(ccts_PropertyTermName);
                ccts_PropertyTermName.setText(propertyTermName);
            }

            String representationTermName = bieDocumentation.getRepresentationTermName();
            if (StringUtils.hasLength(representationTermName)) {
                Element ccts_RepresentationTermName = new Element("ccts_RepresentationTermName", OAGI_NS);
                documentation.addContent(ccts_RepresentationTermName);
                ccts_RepresentationTermName.setText(representationTermName);
            }

            String dataTypeTermName = bieDocumentation.getDataTypeTermName();
            if (StringUtils.hasLength(dataTypeTermName)) {
                Element ccts_DataTypeTermName = new Element("ccts_DataTypeTermName", OAGI_NS);
                documentation.addContent(ccts_DataTypeTermName);
                ccts_DataTypeTermName.setText(dataTypeTermName);
            }

            String businessTerm = bieDocumentation.getBusinessTerm();
            if (StringUtils.hasLength(businessTerm)) {
                Element ccts_BusinessTerm = new Element("ccts_BusinessTerm", OAGI_NS);
                documentation.addContent(ccts_BusinessTerm);
                ccts_BusinessTerm.setText(businessTerm);
            }
        }

        if (bieOagiScoreMetaData) {
            String releaseNumber = bieDocumentation.getReleaseNumber();
            if (StringUtils.hasLength(releaseNumber)) {
                Element srt_BasedStandardReleaseNumber = new Element("srt_BasedStandardReleaseNumber", OAGI_NS);
                documentation.addContent(srt_BasedStandardReleaseNumber);
                srt_BasedStandardReleaseNumber.setText(releaseNumber);
            }

            String version = bieDocumentation.getVersion();
            if (StringUtils.hasLength(version)) {
                Element srt_Version = new Element("srt_Version", OAGI_NS);
                documentation.addContent(srt_Version);
                srt_Version.setText(version);
            }

            String stateCode = bieDocumentation.getStateCode();
            if (StringUtils.hasLength(stateCode)) {
                Element srt_StateCode = new Element("srt_StateCode", OAGI_NS);
                documentation.addContent(srt_StateCode);
                srt_StateCode.setText(stateCode);
            }

            String status = bieDocumentation.getStatus();
            if (StringUtils.hasLength(status)) {
                Element srt_Status = new Element("srt_Status", OAGI_NS);
                documentation.addContent(srt_Status);
                srt_Status.setText(status);
            }

            String remark = bieDocumentation.getRemark();
            if (StringUtils.hasLength(remark)) {
                Element srt_Remark = new Element("srt_Remark", OAGI_NS);
                documentation.addContent(srt_Remark);
                srt_Remark.setText(remark);
            }

            if (option.isIncludeWhoColumns()) {
                String ownerUserName = bieDocumentation.getOwnerUserName();
                if (StringUtils.hasLength(ownerUserName)) {
                    Element srt_OwnerUserName = new Element("srt_OwnerUserName", OAGI_NS);
                    documentation.addContent(srt_OwnerUserName);
                    srt_OwnerUserName.setText(ownerUserName);
                }

                String createdUserName = bieDocumentation.getCreatedUserName();
                if (StringUtils.hasLength(createdUserName)) {
                    Element srt_CreatedByUserName = new Element("srt_CreatedByUserName", OAGI_NS);
                    documentation.addContent(srt_CreatedByUserName);
                    srt_CreatedByUserName.setText(createdUserName);
                }

                String lastUpdatedUserName = bieDocumentation.getLastUpdatedUserName();
                if (StringUtils.hasLength(lastUpdatedUserName)) {
                    Element srt_LastUpdatedByUserName = new Element("srt_LastUpdatedByUserName", OAGI_NS);
                    documentation.addContent(srt_LastUpdatedByUserName);
                    srt_LastUpdatedByUserName.setText(lastUpdatedUserName);
                }

                Date creationTimestamp = bieDocumentation.getCreationTimestamp();
                if (creationTimestamp != null) {
                    Element srt_CreationTimestamp = new Element("srt_CreationTimestamp", OAGI_NS);
                    documentation.addContent(srt_CreationTimestamp);
                    srt_CreationTimestamp.setText(toZuluTimeString(creationTimestamp));
                }

                Date lastUpdatedTimestamp = bieDocumentation.getLastUpdatedTimestamp();
                if (lastUpdatedTimestamp != null) {
                    Element srt_LastUpdateTimestamp = new Element("srt_LastUpdateTimestamp", OAGI_NS);
                    documentation.addContent(srt_LastUpdateTimestamp);
                    srt_LastUpdateTimestamp.setText(toZuluTimeString(lastUpdatedTimestamp));
                }
            }
        }

        if (basedCcMetaData) {
            CcType ccType = bieDocumentation.ccType();

            String guid = bieDocumentation.getGuid();
            Element ccts_Based_GUID = new Element("ccts_Based" + ccType + "_GUID", OAGI_NS);
            documentation.addContent(ccts_Based_GUID);
            ccts_Based_GUID.setText(guid);

            int revisionNumber = bieDocumentation.getRevisionNumber();
            Element ccts_BasedRevisionNumber = new Element("ccts_Based" + ccType + "RevisionNumber", OAGI_NS);
            documentation.addContent(ccts_BasedRevisionNumber);
            ccts_BasedRevisionNumber.setText(Integer.toString(revisionNumber));

            for (Definition definition : bieDocumentation.getCoreComponentDefinitions()) {
                if (StringUtils.hasLength(definition.getDefinition())) {
                    Element ccts_BasedDefinition = new Element("ccts_Based" + ccType + "Definition", OAGI_NS);
                    documentation.addContent(ccts_BasedDefinition);
                    ccts_BasedDefinition.setText(definition.getDefinition());

                    if (StringUtils.hasLength(definition.getDefinitionSource())) {
                        ccts_BasedDefinition.setAttribute("source", definition.getDefinitionSource());
                    }
                }
            }
        }
    }

    public Element generateTopLevelASBIEP(ASBIEP asbiep, TopLevelAsbiep topLevelAsbiep) {
        if (isProcessed(asbiep)) {
            return getProcessedElement(asbiep);
        }

        ASCCP asccp = generationContext.queryBasedASCCP(asbiep);
        Namespace namespace = getNamespace(generationContext, asccp.getNamespaceId());
        this.schemaNode.setAttribute("targetNamespace", namespace.getURI());
        this.schemaNode.addNamespaceDeclaration(namespace);
        Element rootEleNode = newElement("element");
        schemaNode.addContent(rootEleNode);

        rootEleNode.setAttribute("name", asccp.getPropertyTerm().replaceAll(" ", ""));
        if (option.isBieGuid()) {
            rootEleNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + asbiep.getGuid());
        }

        setDefinition(rootEleNode, asbiep.getDefinition());
        setBusinessContext(rootEleNode, topLevelAsbiep);
        setOptionalDocumentation(rootEleNode,
                new ASBIEPDocumentation(asbiep, asccp, topLevelAsbiep));

        setProcessedElement(asbiep, rootEleNode);

        return rootEleNode;
    }

    private Element generateABIE(ABIE abie, Element parentNode) {
        Element complexType = newElement("complexType");
        if (option.isBieGuid()) {
            complexType.setAttribute("id", ID_ATTRIBUTE_PREFIX + abie.getGuid());
        }
        parentNode.addContent(complexType);

        ACC acc = generationContext.queryBasedACC(abie);
        setDefinition(complexType, abie.getDefinition());
        setOptionalDocumentation(complexType, abie, acc);

        Element element;
        if (OagisComponentType.Choice.getValue() == acc.getOagisComponentType()) {
            element = newElement("choice");
            complexType.addContent(element);
        } else {
            element = newElement("sequence");
            complexType.addContent(element);
        }

        return element;
    }

    public void generateBIEs(ABIE abie, Element parent) {
        List<BIE> childBIEs = generationContext.queryChildBIEs(abie);
        for (BIE bie : childBIEs) {
            if (bie instanceof BBIE) {
                BBIE bbie = (BBIE) bie;
                DT bdt = generationContext.queryAssocBDT(bbie);
                generateBBIE(bbie, bdt, parent);
            } else {
                ASBIE asbie = (ASBIE) bie;

                if (Helper.isAnyProperty(asbie, generationContext)) {
                    generateAnyABIE(asbie, parent);
                } else {
                    Element node = generateASBIE(asbie, parent);
                    ASBIEP asbiep = generationContext.queryAssocToASBIEP(asbie);
                    Element asbiepNode = generateASBIEP(asbiep, node);
                    ABIE childAbie = generationContext.queryTargetABIE(asbiep);
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

    private Element generateAnyABIE(ASBIE asbie, Element parent) {
        ASCC ascc = generationContext.queryBasedASCC(asbie);

        Element element = newElement("any");
        element.setAttribute("namespace", "##any");
        element.setAttribute("processContents", "strict");

        element.setAttribute("minOccurs", String.valueOf(asbie.getCardinalityMin()));
        if (asbie.getCardinalityMax() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(asbie.getCardinalityMax()));
        if (asbie.isNillable())
            element.setAttribute("nillable", String.valueOf(asbie.isNillable()));

        if (option.isBieGuid()) {
            element.setAttribute("id", ID_ATTRIBUTE_PREFIX + asbie.getGuid());
        }

        parent.addContent(element);
        return element;
    }

    public Element generateBDT(BBIE bbie, Element eNode, CodeList codeList) {
        BCC bcc = generationContext.queryBasedBCC(bbie);
        DT bdt = generationContext.queryBDT(bbie);

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");

        if (option.isBieGuid()) {
            complexType.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
        }

        setDefinition(complexType, bbie.getDefinition());
        setOptionalDocumentation(complexType, bbie, bcc);

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
        String codeListTypeName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
        extNode.setAttribute("base", codeListTypeName);
        eNode.addContent(complexType);
        return eNode;
    }

    public String setBDTBase(DT bdt) {
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestriByBdtManifestIdAndDefaultIsTrue(bdt.getDtId());
        Xbt xbt = Helper.getXbt(generationContext, bdtPriRestri);
        addXbtSimpleType(xbt);
        return xbt.getBuiltinType();
    }

    public String setBDTBase(BBIE bbie, DT bdt) {
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
        Xbt xbt = Helper.getXbt(generationContext, bdtPriRestri);
        String typeName = null;
        if (bbie.getFacetMinLength() != null || bbie.getFacetMaxLength() != null || StringUtils.hasLength(bbie.getFacetPattern())) {
            typeName = addXbtSimpleType(xbt, bbie, "type_" + bbie.getGuid());
        } else if (bdt.getFacetMinLength() != null || bdt.getFacetMaxLength() != null || StringUtils.hasLength(bdt.getFacetPattern())) {
            typeName = addXbtSimpleType(xbt, bdt, "type_" + bdt.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }
        return (typeName != null) ? typeName : xbt.getBuiltinType();
    }

    public Element setBBIE_Attr_Type(DT bdt, Element gNode) {
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestriByBdtManifestIdAndDefaultIsTrue(bdt.getDtId());
        CdtAwdPriXpsTypeMap cdtAwdPriXpsTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
        Xbt xbt =
                generationContext.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
        if (xbt.getBuiltinType() != null) {
            gNode.setAttribute("type", xbt.getBuiltinType());
        }
        addXbtSimpleType(xbt);
        return gNode;
    }

    public Element setBBIE_Attr_Type(BBIE bbie, DT bdt, Element gNode) {
        BdtPriRestri aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
        CdtAwdPriXpsTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        Xbt xbt =
                generationContext.findXbt(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());

        String typeName = null;
        if (bbie.getFacetMinLength() != null || bbie.getFacetMaxLength() != null || StringUtils.hasLength(bbie.getFacetPattern())) {
            typeName = addXbtSimpleType(xbt, bbie, "type_" + bbie.getGuid());
        } else if (bdt.getFacetMinLength() != null || bdt.getFacetMaxLength() != null || StringUtils.hasLength(bdt.getFacetPattern())) {
            typeName = addXbtSimpleType(xbt, bdt, "type_" + bdt.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }

        gNode.setAttribute("type", (typeName != null) ? typeName : xbt.getBuiltinType());

        return gNode;
    }

    public Element generateBDT(BBIE bbie, Element eNode) {
        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");

        if (option.isBieGuid()) {
            complexType.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
        }

        DT bdt = generationContext.queryAssocBDT(bbie);
        setOptionalDocumentation(complexType, new BusinessDataTypeDocumentation(bdt));

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        if (bbie.getBdtPriRestriId() == null)
            extNode.setAttribute("base", setBDTBase(bdt));
        else {
            extNode.setAttribute("base", setBDTBase(bbie, bdt));
        }

        eNode.addContent(complexType);
        return eNode;
    }

    public Element generateASBIE(ASBIE asbie, Element parent) {
        Element element = newElement("element");
        if (option.isBieGuid()) {
            element.setAttribute("id", ID_ATTRIBUTE_PREFIX + asbie.getGuid());
        }
        element.setAttribute("minOccurs", String.valueOf(asbie.getCardinalityMin()));
        if (asbie.getCardinalityMax() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(asbie.getCardinalityMax()));
        if (asbie.isNillable())
            element.setAttribute("nillable", String.valueOf(asbie.isNillable()));

        while (!parent.getName().equals("sequence") && !parent.getName().equals("choice")) {
            parent = parent.getParentElement();
        }

        ASCC ascc = generationContext.queryBasedASCC(asbie);
        setDefinition(element, asbie.getDefinition());
        setOptionalDocumentation(element, asbie, ascc);

        parent.addContent(element);
        return element;
    }

    public Element generateASBIEP(ASBIEP asbiep, Element parent) {
        ASCCP asccp = generationContext.findASCCP(asbiep.getBasedAsccpManifestId());
        parent.setAttribute("name", Utility.first(asccp.getDen(), true));

        setDefinition(parent, asbiep.getDefinition());
        setOptionalDocumentation(parent, asbiep, asccp);

        return parent;
    }

    public Element handleElementBBIE(BBIE bbie, Element eNode) {
        BCC bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.getDen(), true));
        if (option.isBieGuid()) {
            eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + bbie.getGuid());
        }

        if (bbie.isNillable()) {
            eNode.setAttribute("nillable", "true");
        }
        if (bbie.getDefaultValue() != null && bbie.getDefaultValue().length() != 0) {
            eNode.setAttribute("default", bbie.getDefaultValue());
        }
        if (bbie.getFixedValue() != null && bbie.getFixedValue().length() != 0) {
            eNode.setAttribute("fixed", bbie.getFixedValue());
        }

        eNode.setAttribute("minOccurs", String.valueOf(bbie.getCardinalityMin()));
        if (bbie.getCardinalityMax() == -1)
            eNode.setAttribute("maxOccurs", "unbounded");
        else
            eNode.setAttribute("maxOccurs", String.valueOf(bbie.getCardinalityMax()));
        if (bbie.isNillable())
            eNode.setAttribute("nillable", String.valueOf(bbie.isNillable()));

        setDocumentation(eNode, bbie);

        return eNode;
    }

    public Element handleAttributeBBIE(BBIE bbie, Element eNode) {
        BCC bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.getDen(), false));
        if (option.isBieGuid()) {
            eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + bbie.getGuid());
        }

        // Attribute 'nillable' is not allowed in element <xsd:attribute>
//        if (bbie.isNillable()) {
//            eNode.setAttribute("nillable", "true");
//        }
        if (bbie.getDefaultValue() != null && bbie.getDefaultValue().length() != 0) {
            eNode.setAttribute("default", bbie.getDefaultValue());
        }
        if (bbie.getFixedValue() != null && bbie.getFixedValue().length() != 0) {
            eNode.setAttribute("fixed", bbie.getFixedValue());
        }
        if (bbie.getCardinalityMin() >= 1)
            eNode.setAttribute("use", "required");
        else
            eNode.setAttribute("use", "optional");

        setDocumentation(eNode, bbie);

        return eNode;
    }

    private void setDocumentation(Element node, BBIE bbie) {
        BCC bcc = generationContext.queryBasedBCC(bbie);

        setDefinition(node, bbie.getDefinition());
        setOptionalDocumentation(node, bbie, bcc);

        TopLevelAsbiep topLevelAsbiep = generationContext.findTopLevelAsbiep(bbie.getOwnerTopLevelAsbiepId());
        BBIEP bbiep = generationContext.findBBIEP(bbie.getToBbiepId(), topLevelAsbiep);
        BCCP bccp = generationContext.findBCCP(bbiep.getBasedBccpManifestId());

        setDefinition(node, bbiep.getDefinition());
        setOptionalDocumentation(node, bbiep, bccp);
    }

    public Element setBBIEType(BBIE bbie, DT bdt, Element gNode) {
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
        Xbt xbt = Helper.getXbt(generationContext, bdtPriRestri);
        String typeName = null;
        if (bbie.getFacetMinLength() != null || bbie.getFacetMaxLength() != null || StringUtils.hasLength(bbie.getFacetPattern())) {
            typeName = addXbtSimpleType(xbt, bbie, "type_" + bbie.getGuid());
        } else if (bdt.getFacetMinLength() != null || bdt.getFacetMaxLength() != null || StringUtils.hasLength(bdt.getFacetPattern())) {
            typeName = addXbtSimpleType(xbt, bdt, "type_" + bdt.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }

        gNode.setAttribute("type", (typeName != null) ? typeName : xbt.getBuiltinType());

        return gNode;
    }

    public String addXbtSimpleType(Xbt xbt, FacetRestrictionsAware facetRestri, String name) {
        if (xbt == null) {
            return null;
        }
        if (rootElementNode == null) {
            return null;
        }

        Element xbtNode = newElement("simpleType");
        xbtNode.setAttribute("name", name);
        if (option.isBieGuid()) {
            xbtNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
        }

        Element restrictionNode = newElement("restriction");
        String builtinType = xbt.getBuiltinType();
        if (!builtinType.startsWith(XSD_NAMESPACE.getPrefix())) {
            builtinType = XSD_NAMESPACE.getPrefix() + ":" + "token";
        }
        restrictionNode.setAttribute("base", builtinType);
        xbtNode.addContent(restrictionNode);

        if (facetRestri.getFacetMinLength() != null) {
            Element minLengthNode = newElement("minLength");
            minLengthNode.setAttribute("value", facetRestri.getFacetMinLength().toString());
            restrictionNode.addContent(minLengthNode);
        }
        if (facetRestri.getFacetMaxLength() != null) {
            Element maxLengthNode = newElement("maxLength");
            maxLengthNode.setAttribute("value", facetRestri.getFacetMaxLength().toString());
            restrictionNode.addContent(maxLengthNode);
        }
        if (facetRestri.getFacetPattern() != null) {
            Element patternNode = newElement("pattern");
            patternNode.setAttribute("value", facetRestri.getFacetPattern().toString());
            restrictionNode.addContent(patternNode);
        }

        schemaNode.addContent(xbtNode);

        return name;
    }

    public void addXbtSimpleType(Xbt xbt) {
        /*
         * Issue #521
         * If XBT has a value of schema definition, it is not XML Schema Built-in Type.
         * It should be generated as the XML Schema simple type at the global level.
         */
        if (xbt == null || !StringUtils.hasLength(xbt.getSchemaDefinition())) {
            return;
        }
        if (rootElementNode == null) {
            return;
        }
        if (isProcessed(xbt)) {
            return;
        }

        String name = xbt.getBuiltinType();

        Element xbtNode = newElement("simpleType");
        xbtNode.setAttribute("name", name);
        if (option.isBieGuid()) {
            xbtNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
        }

        try {
            StringBuilder sb = new StringBuilder();
            // To read the definition, it must has defined 'xsd:schema' as a parent.
            sb.append("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "xmlns=\"http://www.openapplications.org/oagis/10\">");
            sb.append(xbt.getSchemaDefinition());
            sb.append("</xsd:schema>");

            Element content = new SAXBuilder().build(
                    new StringReader(sb.toString())).getRootElement();
            for (Content child : content.removeContent()) {
                xbtNode.addContent(child);
            }

            schemaNode.addContent(xbtNode);
        } catch (JDOMException | IOException e) {
            throw new IllegalStateException("Error occurs while the schema definition loads for " + name, e);
        }

        setProcessedElement(xbt, xbtNode);
    }

    public Element generateBBIE(BBIE bbie, DT bdt, Element parent) {
        BCC bcc = generationContext.queryBasedBCC(bbie);

        Element eNode = newElement("element");
        eNode = handleElementBBIE(bbie, eNode);

        if (bcc.getEntityType() == BCCEntityType.Element.getValue()) {
            while (!parent.getName().equals("sequence") && !parent.getName().equals("choice")) {
                parent = parent.getParentElement();
            }
            parent.addContent(eNode);

            List<BBIESC> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeList codeList = generationContext.findCodeList(bbie.getCodeListManifestId());
            if (codeList == null) {
                AgencyIdList agencyIdList = generationContext.findAgencyIdList(bbie.getAgencyIdListManifestId());
                if (agencyIdList != null) {
                    if (option.isBieGuid()) {
                        eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
                    }

                    AgencyIdListValue agencyIdListValue =
                            generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueManifestId());
                    String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);

                    generateAgencyList(agencyIdList, agencyListTypeName);
                    if (StringUtils.hasLength(agencyListTypeName)) {
                        eNode.setAttribute("type", agencyListTypeName);
                    }
                } else {
                    if (bbie.getBdtPriRestriId() == null) {
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
                    eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
                }
                generateCodeList(codeList, bbie);

                if (bbieScList.isEmpty()) {
                    AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
                    eNode.setAttribute("type", Helper.getCodeListTypeName(codeList, agencyIdListValue));
                } else {
                    eNode = generateBDT(bbie, eNode, codeList);
                    eNode = generateSCs(bbie, eNode, bbieScList);
                }
            }
        } else {
            List<BBIESC> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeList codeList = generationContext.findCodeList(bbie.getCodeListManifestId());
            if (codeList == null) {
                AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbie);
                if (agencyIdList != null) {
                    eNode = createAttributeNodeForBBIE(bbie, parent);

                    if (option.isBieGuid()) {
                        eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
                    }

                    AgencyIdListValue agencyIdListValue =
                            generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueManifestId());
                    String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);

                    generateAgencyList(agencyIdList, agencyListTypeName);
                    if (StringUtils.hasLength(agencyListTypeName)) {
                        eNode.setAttribute("type", agencyListTypeName);
                    }
                } else {
                    if (bbie.getBdtPriRestriId() == null) {
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
                    eNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
                }
                generateCodeList(codeList, bbie);

                if (bbieScList.isEmpty()) {
                    AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
                    String codeListTypeName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
                    if (StringUtils.hasLength(codeListTypeName)) {
                        eNode.setAttribute("type", codeListTypeName);
                    }
                    return eNode;
                } else {
                    if (bbie.getBdtPriRestriId() == null) {
                        eNode = setBBIE_Attr_Type(bdt, eNode);
                    } else {
                        AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
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

    private Element createAttributeNodeForBBIE(BBIE bbie, Element parent) {
        Element eNode = newElement("attribute");
        eNode = handleAttributeBBIE(bbie, eNode);

        while (!parent.getName().equals("complexType")) {
            parent = parent.getParentElement();
        }

        parent.addContent(eNode);

        return eNode;
    }

    public String setCodeListRestrictionAttr(BBIE bbie) {
        BdtPriRestri bdtPriRestri =
                generationContext.findBdtPriRestriByBbieAndDefaultIsTrue(bbie);
        if (bdtPriRestri.getCodeListManifestId() != null) {
            return "xsd:token";
        } else {
            CdtAwdPriXpsTypeMap cdtAwdPriXpsTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtAwdPriXpsTypeMap.getXbtId());
            addXbtSimpleType(xbt);
            return xbt.getBuiltinType();
        }
    }

    public String setCodeListRestrictionAttr(BBIESC bbieSc) {
        BdtScPriRestri bdtScPriRestri =
                generationContext.findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc);
        if (bdtScPriRestri.getCodeListManifestId() != null) {
            return "xsd:token";
        } else {
            CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            Xbt xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());
            addXbtSimpleType(xbt);
            return xbt.getBuiltinType();
        }
    }

    public Element generateCodeList(CodeList codeList, BBIE bbie) {
        return generateCodeList(codeList, setCodeListRestrictionAttr(bbie));
    }

    public Element generateCodeList(CodeList codeList, BBIESC bbieSc) {
        return generateCodeList(codeList, setCodeListRestrictionAttr(bbieSc));
    }

    private Element generateCodeList(CodeList codeList, String codeListRestrictionAttr) {
        if (isProcessed(codeList)) {
            return getProcessedElement(codeList);
        }

        Element stNode = newElement("simpleType");

        AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
        stNode.setAttribute("name", Helper.getCodeListTypeName(codeList, agencyIdListValue));
        if (option.isBieGuid()) {
            stNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + codeList.getGuid());
        }

        Element rtNode = newElement("restriction");
        rtNode.setAttribute("base", codeListRestrictionAttr);
        stNode.addContent(rtNode);

        for (CodeListValue codeListValue : generationContext.getCodeListValues(codeList)) {
            Element enumeration = newElement("enumeration");
            enumeration.setAttribute("value", codeListValue.getValue());
            rtNode.addContent(enumeration);
        }

        schemaNode.addContent(stNode);
        setProcessedElement(codeList, stNode);

        return stNode;
    }

    public Element handleBBIESCvalue(BBIESC bbieSc, Element aNode) {
        //Handle gSC[i]
        if (bbieSc.getDefaultValue() != null && bbieSc.getFixedValue() != null) {
            System.out.println("default and fixed value options handling error");
        } else if (bbieSc.getDefaultValue() != null && bbieSc.getDefaultValue().length() != 0) {
            aNode.setAttribute("default", bbieSc.getDefaultValue());
        } else if (bbieSc.getFixedValue() != null && bbieSc.getFixedValue().length() != 0) {
            aNode.setAttribute("fixed", bbieSc.getFixedValue());
        }
        // Generate a DOM Attribute node
        /*
         * Section 3.8.1.22 GenerateSCs #2
         */
        DTSC dtSc = generationContext.findDtSc(bbieSc.getBasedDtScManifestId());
        String representationTerm = dtSc.getRepresentationTerm();
        String propertyTerm = dtSc.getPropertyTerm();
        aNode.setAttribute("name", toName(propertyTerm, representationTerm, rt -> {
            if ("Text".equals(rt)) {
                return "";
            }
            if ("Identifier".equals(rt)) {
                return "ID";
            }
            return rt;
        }, false));

        if (bbieSc.getCardinalityMin() >= 1) {
            aNode.setAttribute("use", "required");
        } else {
            aNode.setAttribute("use", "optional");
        }

        if (option.isBieGuid()) {
            aNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + bbieSc.getGuid());
        }
        setDefinition(aNode, bbieSc.getDefinition());

        DTSC bdtSc = generationContext.findDtSc(bbieSc.getBasedDtScManifestId());
        BBIESCDocumentation bieDocumentation =
                new BBIESCDocumentation(bbieSc, bdtSc);
        setOptionalDocumentation(aNode, bieDocumentation);

        return aNode;
    }

    public Element setBBIESCType(BBIESC bbieSc, DTSC bdtSc, Element gNode) {
        DTSC dtSc = generationContext.findDtSc(bbieSc.getBasedDtScManifestId());
        if (dtSc != null) {
            BdtScPriRestri bdtScPriRestri =
                    generationContext.findBdtScPriRestriByBbieScAndDefaultIsTrue(bbieSc);
            if (bdtScPriRestri != null) {
                CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                if (cdtScAwdPriXpsTypeMap != null) {
                    Xbt xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());

                    String typeName = null;
                    if (bbieSc.getFacetMinLength() != null || bbieSc.getFacetMaxLength() != null || bbieSc.getFacetPattern() != null) {
                        typeName = addXbtSimpleType(xbt, bbieSc, "type_" + bbieSc.getGuid());
                    } else if (bdtSc.getFacetMinLength() != null || bdtSc.getFacetMaxLength() != null || StringUtils.hasLength(bdtSc.getFacetPattern())) {
                        typeName = addXbtSimpleType(xbt, bdtSc, "type_" + bdtSc.getGuid());
                    } else {
                        addXbtSimpleType(xbt);
                    }

                    gNode.setAttribute("type", (typeName != null) ? typeName : xbt.getBuiltinType());
                }
            }
        }
        return gNode;
    }

    public Element setBBIESCType2(BBIESC bbieSc, DTSC bdtSc, Element gNode) {
        BdtScPriRestri bdtScPriRestri =
                generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        CdtScAwdPriXpsTypeMap cdtScAwdPriXpsTypeMap =
                generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
        Xbt xbt = generationContext.findXbt(cdtScAwdPriXpsTypeMap.getXbtId());

        String typeName = null;
        if (bbieSc.getFacetMinLength() != null || bbieSc.getFacetMaxLength() != null || bbieSc.getFacetPattern() != null) {
            typeName = addXbtSimpleType(xbt, bbieSc, "type_" + bbieSc.getGuid());
        } else if (bdtSc.getFacetMinLength() != null || bdtSc.getFacetMaxLength() != null || StringUtils.hasLength(bdtSc.getFacetPattern())) {
            typeName = addXbtSimpleType(xbt, bdtSc, "type_" + bdtSc.getGuid());
        } else {
            addXbtSimpleType(xbt);
        }

        gNode.setAttribute("type", (typeName != null) ? typeName : xbt.getBuiltinType());

        return gNode;

    }

    public Element generateSCs(BBIE bbie, Element bbieElement,
                               List<BBIESC> bbieScList) {
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
            BBIESC bbieSc = bbieScList.get(i);
            if (bbieSc.getCardinalityMax() == 0)
                continue;
            Element aNode = newElement("attribute");
            aNode = handleBBIESCvalue(bbieSc, aNode); //Generate a DOM Element Node, handle values

            //Get a code list object
            CodeList codeList = generationContext.getCodeList(bbieSc);
            if (codeList == null) {
                AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);

                if (agencyIdList == null) {
                    DTSC dtSc = generationContext.findDtSc(bbieSc.getBasedDtScManifestId());

                    if (bbieSc.getDtScPriRestriId() == null)
                        aNode = setBBIESCType(bbieSc, dtSc, aNode);
                    else
                        aNode = setBBIESCType2(bbieSc, dtSc, aNode);
                } else {
                    if (option.isBieGuid()) {
                        aNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
                    }

                    AgencyIdListValue agencyIdListValue =
                            generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueManifestId());
                    String agencyListTypeName = Helper.getAgencyListTypeName(agencyIdList, agencyIdListValue);

                    generateAgencyList(agencyIdList, agencyListTypeName);
                    if (StringUtils.hasLength(agencyListTypeName)) {
                        aNode.setAttribute("type", agencyListTypeName);
                    }
                }
            } else {
                if (option.isBieGuid()) {
                    aNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + ScoreGuid.randomGuid());
                }

                generateCodeList(codeList, bbieSc);

                AgencyIdListValue agencyIdListValue = generationContext.findAgencyIdListValue(codeList.getAgencyIdListValueManifestId());
                String codeListTypeName = Helper.getCodeListTypeName(codeList, agencyIdListValue);
                if (StringUtils.hasLength(codeListTypeName)) {
                    aNode.setAttribute("type", codeListTypeName);
                }
            }

            tNode.addContent(aNode);
        }
        return tNode;
    }

    public Element generateAgencyList(AgencyIdList agencyIdList, String agencyListTypeName) {
        if (isProcessed(agencyIdList)) {
            return getProcessedElement(agencyIdList);
        }

        Element stNode = newElement("simpleType");

        stNode.setAttribute("name", agencyListTypeName);
        if (option.isBieGuid()) {
            stNode.setAttribute("id", ID_ATTRIBUTE_PREFIX + agencyIdList.getGuid());
        }

        Element rtNode = newElement("restriction");

        rtNode.setAttribute("base", "xsd:token");
        stNode.addContent(rtNode);

        List<AgencyIdListValue> agencyIdListValues =
                generationContext.findAgencyIdListValueByAgencyIdListManifestId(agencyIdList.getAgencyIdListManifestId());

        for (AgencyIdListValue agencyIdListValue : agencyIdListValues) {
            Element enumeration = newElement("enumeration");
            rtNode.addContent(enumeration);
            enumeration.setAttribute("value", agencyIdListValue.getValue());

            Element annotation = newElement("annotation");
            Element documentation = newElement("documentation");
            documentation.setAttribute("source", OAGI_NS);

            Element cctsName = new Element("ccts_Name", OAGI_NS);
            String name = agencyIdListValue.getName();
            cctsName.setText(name);
            documentation.addContent(cctsName);
            Element cctsDefinition = new Element("ccts_Definition", OAGI_NS);
            String definition = agencyIdListValue.getDefinition();
            cctsDefinition.setText(definition);
            documentation.addContent(cctsDefinition);

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

        String getGuid();

        int getRevisionNumber();

        Collection<Definition> getCoreComponentDefinitions();
    }

    private class Definition {
        private final String definition;
        private String definitionSource;

        public Definition(String definition) {
            this.definition = definition;
        }

        public Definition(String definition, String definitionSource) {
            this.definition = definition;
            this.definitionSource = definitionSource;
        }

        public String getDefinition() {
            return definition;
        }

        public String getDefinitionSource() {
            return definitionSource;
        }
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
        public String getGuid() {
            return null;
        }

        @Override
        public int getRevisionNumber() {
            return 0;
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Collections.emptyList();
        }
    }

    private class ABIEDocumentation extends AbstractBIEDocumentation {
        private final ABIE abie;
        private final ACC acc;

        public ABIEDocumentation(
                ABIE abie,
                ACC acc) {
            this.abie = abie;
            this.acc = acc;
        }

        @Override
        public String getEntityTypeCode() {
            return "ABIE";
        }

        @Override
        public String getDictionaryEntryName() {
            return acc.getDen();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(abie.getDefinition()));
        }

        @Override
        public String getObjectClassTermName() {
            return acc.getObjectClassTerm();
        }

        @Override
        public String getVersion() {
            return abie.getVersion();
        }

        @Override
        public String getStatus() {
            return abie.getStatus();
        }

        @Override
        public String getBusinessTerm() {
            return abie.getBizTerm();
        }

        @Override
        public String getRemark() {
            return abie.getRemark();
        }

        @Override
        public String getCreatedUserName() {
            return generationContext.findUserName(abie.getCreatedBy());
        }

        @Override
        public String getLastUpdatedUserName() {
            return generationContext.findUserName(abie.getLastUpdatedBy());
        }

        @Override
        public Date getCreationTimestamp() {
            return abie.getCreationTimestamp();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return abie.getLastUpdateTimestamp();
        }

        @Override
        public CcType ccType() {
            return CcType.ACC;
        }

        @Override
        public String getGuid() {
            return acc.getGuid();
        }

        @Override
        public int getRevisionNumber() {
            return acc.getRevisionNum();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Arrays.asList(new Definition(acc.getDefinition(), acc.getDefinitionSource()));
        }
    }

    private class ASBIEDocumentation extends AbstractBIEDocumentation {
        private final ASBIE asbie;
        private final ASCC ascc;

        public ASBIEDocumentation(
                ASBIE asbie,
                ASCC ascc) {
            this.asbie = asbie;
            this.ascc = ascc;
        }

        @Override
        public String getEntityTypeCode() {
            return "ASBIE";
        }

        @Override
        public String getDictionaryEntryName() {
            return ascc.getDen();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(asbie.getDefinition()));
        }

        @Override
        public String getRemark() {
            return asbie.getRemark();
        }

        @Override
        public String getCreatedUserName() {
            return generationContext.findUserName(asbie.getCreatedBy());
        }

        @Override
        public String getLastUpdatedUserName() {
            return generationContext.findUserName(asbie.getLastUpdatedBy());
        }

        @Override
        public Date getCreationTimestamp() {
            return asbie.getCreationTimestamp();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return asbie.getLastUpdateTimestamp();
        }

        @Override
        public CcType ccType() {
            return CcType.ASCC;
        }

        @Override
        public String getGuid() {
            return ascc.getGuid();
        }

        @Override
        public int getRevisionNumber() {
            return ascc.getRevisionNum();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Arrays.asList(new Definition(ascc.getDefinition(), ascc.getDefinitionSource()));
        }
    }

    private class ASBIEPDocumentation extends AbstractBIEDocumentation {
        private final ASBIEP asbiep;
        private final ASCCP asccp;
        private final TopLevelAsbiep topLevelAsbiep;

        public ASBIEPDocumentation(
                ASBIEP asbiep,
                ASCCP asccp) {
            this(asbiep, asccp, null);
        }

        public ASBIEPDocumentation(
                ASBIEP asbiep,
                ASCCP asccp,
                TopLevelAsbiep topLevelAsbiep) {
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
            return asccp.getDen();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(asbiep.getDefinition()));
        }

        @Override
        public String getPropertyTermName() {
            return asccp.getPropertyTerm();
        }

        @Override
        public String getReleaseNumber() {
            if (topLevelAsbiep != null) {
                return generationContext.findReleaseNumber(topLevelAsbiep.getReleaseId());
            }
            return null;
        }

        @Override
        public String getStateCode() {
            if (topLevelAsbiep != null) {
                return topLevelAsbiep.getState().name();
            }
            return null;
        }

        @Override
        public String getOwnerUserName() {
            if (topLevelAsbiep != null) {
                return generationContext.findUserName(topLevelAsbiep.getOwnerUserId());
            }
            return null;
        }

        @Override
        public String getRemark() {
            return asbiep.getRemark();
        }

        @Override
        public String getCreatedUserName() {
            return generationContext.findUserName(asbiep.getCreatedBy());
        }

        @Override
        public String getLastUpdatedUserName() {
            return generationContext.findUserName(asbiep.getLastUpdatedBy());
        }

        @Override
        public Date getCreationTimestamp() {
            return asbiep.getCreationTimestamp();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return asbiep.getLastUpdateTimestamp();
        }

        @Override
        public CcType ccType() {
            return CcType.ASCCP;
        }

        @Override
        public String getGuid() {
            return asccp.getGuid();
        }

        @Override
        public int getRevisionNumber() {
            return asccp.getRevisionNum();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Arrays.asList(new Definition(asccp.getDefinition(), asccp.getDefinitionSource()));
        }
    }

    private class BBIEDocumentation extends AbstractBIEDocumentation {
        private final BBIE bbie;
        private final BCC bcc;

        public BBIEDocumentation(
                BBIE bbie,
                BCC bcc) {
            this.bbie = bbie;
            this.bcc = bcc;
        }

        @Override
        public String getEntityTypeCode() {
            return "BBIE";
        }

        @Override
        public String getDictionaryEntryName() {
            return bcc.getDen();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(bbie.getDefinition()));
        }

        @Override
        public String getRemark() {
            return bbie.getRemark();
        }

        @Override
        public String getCreatedUserName() {
            return generationContext.findUserName(bbie.getCreatedBy());
        }

        @Override
        public String getLastUpdatedUserName() {
            return generationContext.findUserName(bbie.getLastUpdatedBy());
        }

        @Override
        public Date getCreationTimestamp() {
            return bbie.getCreationTimestamp();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return bbie.getLastUpdateTimestamp();
        }

        @Override
        public CcType ccType() {
            return CcType.BCC;
        }

        @Override
        public String getGuid() {
            return bcc.getGuid();
        }

        @Override
        public int getRevisionNumber() {
            return bcc.getRevisionNum();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Arrays.asList(new Definition(bcc.getDefinition(), bcc.getDefinitionSource()));
        }
    }

    private class BBIEPDocumentation extends AbstractBIEDocumentation {
        private final BBIEP bbiep;
        private final BCCP bccp;

        public BBIEPDocumentation(
                BBIEP bbiep,
                BCCP bccp) {
            this.bbiep = bbiep;
            this.bccp = bccp;
        }

        @Override
        public String getEntityTypeCode() {
            return "BBIEP";
        }

        @Override
        public String getDictionaryEntryName() {
            return bccp.getDen();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(bbiep.getDefinition()));
        }

        @Override
        public String getPropertyTermName() {
            return bccp.getPropertyTerm();
        }

        @Override
        public String getRepresentationTermName() {
            return bccp.getRepresentationTerm();
        }

        @Override
        public String getRemark() {
            return bbiep.getRemark();
        }

        @Override
        public String getCreatedUserName() {
            return generationContext.findUserName(bbiep.getCreatedBy());
        }

        @Override
        public String getLastUpdatedUserName() {
            return generationContext.findUserName(bbiep.getLastUpdatedBy());
        }

        @Override
        public Date getCreationTimestamp() {
            return bbiep.getCreationTimestamp();
        }

        @Override
        public Date getLastUpdatedTimestamp() {
            return bbiep.getLastUpdateTimestamp();
        }

        @Override
        public CcType ccType() {
            return CcType.BCCP;
        }

        @Override
        public String getGuid() {
            return bccp.getGuid();
        }

        @Override
        public int getRevisionNumber() {
            return bccp.getRevisionNum();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Arrays.asList(new Definition(bccp.getDefinition(), bccp.getDefinitionSource()));
        }
    }

    private class BBIESCDocumentation extends AbstractBIEDocumentation {
        private final BBIESC bbieSc;
        private final DTSC dtSc;

        public BBIESCDocumentation(
                BBIESC bbieSc,
                DTSC dtSc) {
            this.bbieSc = bbieSc;
            this.dtSc = dtSc;
        }

        @Override
        public String getEntityTypeCode() {
            return "SC";
        }

        @Override
        public String getDictionaryEntryName() {
            return dtSc.getDen();
        }

        @Override
        public Collection<Definition> getDefinitions() {
            return Arrays.asList(new Definition(bbieSc.getDefinition()));
        }

        @Override
        public String getPropertyTermName() {
            return dtSc.getPropertyTerm();
        }

        @Override
        public String getRepresentationTermName() {
            return dtSc.getRepresentationTerm();
        }

        @Override
        public String getRemark() {
            return bbieSc.getRemark();
        }

        @Override
        public CcType ccType() {
            return CcType.DT_SC;
        }

        @Override
        public String getGuid() {
            return dtSc.getGuid();
        }

        @Override
        public int getRevisionNumber() {
            return dtSc.getRevisionNum();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Arrays.asList(new Definition(dtSc.getDefinition(), dtSc.getDefinitionSource()));
        }
    }

    private class BusinessDataTypeDocumentation extends AbstractBIEDocumentation {
        private final DT bdt;

        public BusinessDataTypeDocumentation(DT bdt) {
            this.bdt = bdt;
        }

        @Override
        public String getEntityTypeCode() {
            return "BDT";
        }

        @Override
        public String getDictionaryEntryName() {
            return bdt.getDen();
        }

        @Override
        public String getDataTypeTermName() {
            return bdt.getDataTypeTerm();
        }

        @Override
        public CcType ccType() {
            return CcType.DT;
        }

        @Override
        public String getGuid() {
            return bdt.getGuid();
        }

        @Override
        public int getRevisionNumber() {
            return bdt.getRevisionNum();
        }

        @Override
        public Collection<Definition> getCoreComponentDefinitions() {
            return Arrays.asList(new Definition(bdt.getDefinition(), bdt.getDefinitionSource()));
        }
    }
}
