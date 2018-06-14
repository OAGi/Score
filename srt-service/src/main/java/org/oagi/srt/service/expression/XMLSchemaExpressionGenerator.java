package org.oagi.srt.service.expression;

import org.apache.commons.io.FileUtils;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.ServiceApplication;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

import static org.oagi.srt.common.SRTConstants.OAGI_NS;
import static org.oagi.srt.service.expression.Helper.*;

class XMLSchemaExpressionGenerator implements SchemaExpressionGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static org.jdom2.Namespace XSD_NAMESPACE = org.jdom2.Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    private Document document;
    private Element schemaNode;
    private Element rootElementNode;
    private GenerationContext generationContext;
    private ProfileBODGenerationOption option;

    private Map<String, Element> processedElements = new HashMap();

    public XMLSchemaExpressionGenerator() {
        this.document = new Document();
        this.schemaNode = generateSchema(document);
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
    public void generate(GenerationContext generationContext, TopLevelAbie topLevelAbie, ProfileBODGenerationOption option) {
        this.generationContext = generationContext;
        this.option = option;

        generateTopLevelABIE(topLevelAbie);
    }

    @Override
    public File asFile(String filename) throws IOException {
        File tempFile = File.createTempFile(Utility.generateGUID(), null);
        tempFile = new File(tempFile.getParentFile(), filename + ".xsd");

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            outputter.output(this.document, outputStream);
            outputStream.flush();
        }

        logger.info("XML Schema is generated: " + tempFile);

        return tempFile;
    }

    public Document generateTopLevelABIE(TopLevelAbie topLevelAbie) {
        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie);
        logger.debug("Generating Top Level ABIE w/ given AssociationBusinessInformationEntityProperty Id: " + asbiep.getAsbiepId());

        rootElementNode = generateTopLevelASBIEP(asbiep, topLevelAbie);

        abie = generationContext.queryTargetABIE(asbiep);
        Element rootSeqNode = generateABIE(abie, rootElementNode);
        generateBIEs(abie, rootSeqNode);

        return document;
    }

    private String key(BusinessInformationEntity bie) {
        return bie.getClass().getSimpleName() + bie.getGuid();
    }

    private String key(XSDBuiltInType xbt) {
        return "XBT" + xbt.getName();
    }

    private String key(CodeList codeList) {
        return "CodeList" + codeList.getGuid();
    }

    private String key(AgencyIdList agencyIdList) {
        return "AgencyIdList" + agencyIdList.getGuid();
    }

    private boolean isProcessed(BusinessInformationEntity bie) {
        return processedElements.containsKey(key(bie));
    }

    private boolean isProcessed(XSDBuiltInType xbt) {
        return processedElements.containsKey(key(xbt));
    }

    private boolean isProcessed(CodeList codeList) {
        return processedElements.containsKey(key(codeList));
    }

    private boolean isProcessed(AgencyIdList agencyIdList) {
        return processedElements.containsKey(key(agencyIdList));
    }

    private Element getProcessedElement(BusinessInformationEntity bie) {
        return processedElements.get(key(bie));
    }

    private Element getProcessedElement(CodeList codeList) {
        return processedElements.get(key(codeList));
    }

    private Element getProcessedElement(AgencyIdList agencyIdList) {
        return processedElements.get(key(agencyIdList));
    }

    private Element setProcessedElement(BusinessInformationEntity bie, Element element) {
        processedElements.put(key(bie), element);
        return element;
    }

    private Element setProcessedElement(XSDBuiltInType xbt, Element element) {
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
        if (StringUtils.isEmpty(definition)) {
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

    private void setBusinessContext(Element node, TopLevelAbie topLevelAbie) {
        if (!option.isBusinessContext()) {
            return;
        }

        Element annotation = newElement("annotation");
        node.addContent(annotation);

        Element documentation = newElement("documentation");
        annotation.addContent(documentation);

        Element ccts_BusinessContext = new Element("ccts_BusinessContext", OAGI_NS);
        documentation.addContent(ccts_BusinessContext);

        BusinessContext businessContext = generationContext.findBusinessContext(topLevelAbie);

        Element ccts_BusinessContextGUID = new Element("ccts_GUID", OAGI_NS);
        ccts_BusinessContext.addContent(ccts_BusinessContextGUID);
        ccts_BusinessContextGUID.setText(businessContext.getGuid());

        Element ccts_BusinessContextName = new Element("ccts_Name", OAGI_NS);
        ccts_BusinessContext.addContent(ccts_BusinessContextName);
        ccts_BusinessContextName.setText(businessContext.getName());

        List<ContextSchemeValue> contextSchemeValues = generationContext.findContextSchemeValue(businessContext);
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

            ContextScheme contextScheme = contextSchemeValue.getContextScheme();

            Element ccts_ClassificationScheme = new Element("ccts_ClassificationScheme", OAGI_NS);
            ccts_ContextValue.addContent(ccts_ClassificationScheme);

            Element ccts_ClassificationSchemeGUID = new Element("ccts_GUID", OAGI_NS);
            ccts_ClassificationScheme.addContent(ccts_ClassificationSchemeGUID);
            ccts_ClassificationSchemeGUID.setText(contextScheme.getGuid());

            {
                ContextCategory contextCategory = contextScheme.getContextCategory();

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

    private interface BIEMetaData {
        String getEntityTypeCode();
        String getDictionaryEntryName();
        String getDefinition();
        String getDefinitionSource();
        String getObjectClassTermName();
        String getPropertyTermName();
        String getRepresentationTermName();
        String getDataTypeTermName();
        String getBusinessTerm();
    }

    private interface SRTMetaData {
        String getVersion();
        String getStatus();
        String getState();
        String getRemark();
        String getCreatedUserName();
        String getLastUpdatedUserName();
        String getOwnerUserName();
        Date getCreationTimestamp();
        Date getLastUpdatedTimestamp();
    }

    private abstract class AbstractBIEDocumentation implements BIEMetaData, SRTMetaData {
        @Override
        public String getDictionaryEntryName() {
            return null;
        }
        @Override
        public String getDefinition() {
            return null;
        }
        @Override
        public String getDefinitionSource() {
            return null;
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
        public String getVersion() {
            return null;
        }
        @Override
        public String getStatus() {
            return null;
        }
        @Override
        public String getState() {
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
    }

    private class AggregateBusinessInformationEntityDocumentation extends AbstractBIEDocumentation {
        private final AggregateBusinessInformationEntity abie;
        private final AggregateCoreComponent acc;
        private TopLevelAbie topLevelAbie;

        public AggregateBusinessInformationEntityDocumentation(
                AggregateBusinessInformationEntity abie,
                AggregateCoreComponent acc) {
            this(abie, acc, null);
        }

        public AggregateBusinessInformationEntityDocumentation(
                AggregateBusinessInformationEntity abie,
                AggregateCoreComponent acc, TopLevelAbie topLevelAbie) {
            this.abie = abie;
            this.acc = acc;
            this.topLevelAbie = topLevelAbie;
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
        public String getDefinition() {
            return abie.getDefinition();
        }
        @Override
        public String getObjectClassTermName() {
            return acc.getObjectClassTerm();
        }
        @Override
        public String getBusinessTerm() {
            return abie.getBizTerm();
        }

        @Override
        public String getVersion() {
            if (topLevelAbie != null) {
                return abie.getVersion();
            }
            return null;
        }
        @Override
        public String getStatus() {
            if (topLevelAbie != null) {
                return abie.getStatus();
            }
            return null;
        }
        @Override
        public String getState() {
            if (topLevelAbie != null) {
                return topLevelAbie.getState().toString();
            }
            return null;
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
        public String getOwnerUserName() {
            if (topLevelAbie != null) {
                return generationContext.findUserName(topLevelAbie.getOwnerUserId());
            }
            return null;
        }
        @Override
        public Date getCreationTimestamp() {
            return abie.getCreationTimestamp();
        }
        @Override
        public Date getLastUpdatedTimestamp() {
            return abie.getLastUpdateTimestamp();
        }
    }

    private class AssociationBusinessInformationEntityDocumentation extends AbstractBIEDocumentation {
        private final AssociationBusinessInformationEntity asbie;
        private final AssociationCoreComponent ascc;
        public AssociationBusinessInformationEntityDocumentation(
                AssociationBusinessInformationEntity asbie,
                AssociationCoreComponent ascc) {
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
        public String getDefinition() {
            return asbie.getDefinition();
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
    }

    private class AssociationBusinessInformationEntityPropertyDocumentation extends AbstractBIEDocumentation {
        private final AssociationBusinessInformationEntityProperty asbiep;
        private final AssociationCoreComponentProperty asccp;
        public AssociationBusinessInformationEntityPropertyDocumentation(
                AssociationBusinessInformationEntityProperty asbiep,
                AssociationCoreComponentProperty asccp) {
            this.asbiep = asbiep;
            this.asccp = asccp;
        }

        @Override
        public String getEntityTypeCode() {
            return "ASIEP";
        }
        @Override
        public String getDictionaryEntryName() {
            return asccp.getDen();
        }
        @Override
        public String getDefinition() {
            return asbiep.getDefinition();
        }
        @Override
        public String getPropertyTermName() {
            return asccp.getPropertyTerm();
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
    }

    private class BasicBusinessInformationEntityDocumentation extends AbstractBIEDocumentation {
        private final BasicBusinessInformationEntity bbie;
        private final BasicCoreComponent bcc;
        public BasicBusinessInformationEntityDocumentation(
                BasicBusinessInformationEntity bbie,
                BasicCoreComponent bcc) {
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
        public String getDefinition() {
            return bbie.getDefinition();
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
    }

    private class BasicBusinessInformationEntityPropertyDocumentation extends AbstractBIEDocumentation {
        private final BasicBusinessInformationEntityProperty bbiep;
        private final BasicCoreComponentProperty bccp;
        public BasicBusinessInformationEntityPropertyDocumentation(
                BasicBusinessInformationEntityProperty bbiep,
                BasicCoreComponentProperty bccp) {
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
        public String getDefinition() {
            return bbiep.getDefinition();
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
    }

    private class BasicBusinessInformationEntitySupplementaryComponentDocumentation extends AbstractBIEDocumentation {
        private final BasicBusinessInformationEntitySupplementaryComponent bbieSc;
        private final DataTypeSupplementaryComponent dtSc;
        public BasicBusinessInformationEntitySupplementaryComponentDocumentation(
                BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                DataTypeSupplementaryComponent dtSc) {
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
        public String getDefinition() {
            return bbieSc.getDefinition();
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
    }

    private class BusinessDataTypeDocumentation extends AbstractBIEDocumentation {
        private final DataType bdt;
        public BusinessDataTypeDocumentation(DataType bdt) {
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
    }

    private BIEMetaData getBIEDocumentation(BusinessInformationEntity bie, CoreComponent cc) {
        if (bie instanceof AggregateBusinessInformationEntity && cc instanceof AggregateCoreComponent) {
            return new AggregateBusinessInformationEntityDocumentation(
                    (AggregateBusinessInformationEntity) bie,
                    (AggregateCoreComponent) cc);
        }

        if (bie instanceof AssociationBusinessInformationEntity && cc instanceof AssociationCoreComponent) {
            return new AssociationBusinessInformationEntityDocumentation(
                    (AssociationBusinessInformationEntity) bie, (AssociationCoreComponent) cc);
        }

        if (bie instanceof AssociationBusinessInformationEntityProperty && cc instanceof AssociationCoreComponentProperty) {
            return new AssociationBusinessInformationEntityPropertyDocumentation(
                    (AssociationBusinessInformationEntityProperty) bie,
                    (AssociationCoreComponentProperty) cc);
        }

        if (bie instanceof BasicBusinessInformationEntity && cc instanceof BasicCoreComponent) {
            return new BasicBusinessInformationEntityDocumentation(
                    (BasicBusinessInformationEntity) bie, (BasicCoreComponent) cc);
        }

        if (bie instanceof BasicBusinessInformationEntityProperty && cc instanceof BasicCoreComponentProperty) {
            return new BasicBusinessInformationEntityPropertyDocumentation(
                    (BasicBusinessInformationEntityProperty) bie,
                    (BasicCoreComponentProperty) cc);
        }

        throw new IllegalArgumentException();
    }

    private void setOptionalDocumentation(Element node, BusinessInformationEntity bie, CoreComponent cc) {
        boolean bieCctsMetaData = option.isBieCctsMetaData();
        boolean bieOagiSrtMetaData = option.isBieOagiSrtMetaData();
        boolean basedCcMetaData = option.isBasedCcMetaData();

        if (!basedCcMetaData && !bieOagiSrtMetaData && !basedCcMetaData) {
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

        BIEMetaData bieDocumentation = getBIEDocumentation(bie, cc);
        if (bieCctsMetaData) {
            String entityTypeCode = bieDocumentation.getEntityTypeCode();
            Element ccts_BIEEntityTypeCode = new Element("ccts_BIEEntityTypeCode", OAGI_NS);
            documentation.addContent(ccts_BIEEntityTypeCode);
            ccts_BIEEntityTypeCode.setText(entityTypeCode);

            String dictionaryEntryName = bieDocumentation.getDictionaryEntryName();
            if (!StringUtils.isEmpty(dictionaryEntryName)) {
                Element ccts_DictionaryEntryName = new Element("ccts_DictionaryEntryName", OAGI_NS);
                documentation.addContent(ccts_DictionaryEntryName);
                ccts_DictionaryEntryName.setText(dictionaryEntryName);
            }

            if (option.isIncludeCctsDefinitionTag()) {
                String definition = bieDocumentation.getDefinition();
                if (!StringUtils.isEmpty(definition)) {
                    Element ccts_Definition = new Element("ccts_Definition", OAGI_NS);
                    documentation.addContent(ccts_Definition);
                    ccts_Definition.setText(definition);

                    String definitionSource = bieDocumentation.getDefinitionSource();
                    if (!StringUtils.isEmpty(definitionSource)) {
                        ccts_Definition.setAttribute("source", definitionSource);
                    }
                }
            }

            String objectClassTermName = bieDocumentation.getObjectClassTermName();
            if (!StringUtils.isEmpty(objectClassTermName)) {
                Element ccts_ObjectClassTermName = new Element("ccts_ObjectClassTermName", OAGI_NS);
                documentation.addContent(ccts_ObjectClassTermName);
                ccts_ObjectClassTermName.setText(objectClassTermName);
            }

            String propertyTermName = bieDocumentation.getPropertyTermName();
            if (!StringUtils.isEmpty(propertyTermName)) {
                Element ccts_PropertyTermName = new Element("ccts_PropertyTermName", OAGI_NS);
                documentation.addContent(ccts_PropertyTermName);
                ccts_PropertyTermName.setText(propertyTermName);
            }

            String representationTermName = bieDocumentation.getRepresentationTermName();
            if (!StringUtils.isEmpty(representationTermName)) {
                Element ccts_RepresentationTermName = new Element("ccts_RepresentationTermName", OAGI_NS);
                documentation.addContent(ccts_RepresentationTermName);
                ccts_RepresentationTermName.setText(representationTermName);
            }

            String dataTypeTermName = bieDocumentation.getDataTypeTermName();
            if (!StringUtils.isEmpty(dataTypeTermName)) {
                Element ccts_DataTypeTermName = new Element("ccts_DataTypeTermName", OAGI_NS);
                documentation.addContent(ccts_DataTypeTermName);
                ccts_DataTypeTermName.setText(dataTypeTermName);
            }

            String businessTerm = bieDocumentation.getBusinessTerm();
            if (!StringUtils.isEmpty(businessTerm)) {
                Element ccts_BusinessTerm = new Element("ccts_BusinessTerm", OAGI_NS);
                documentation.addContent(ccts_BusinessTerm);
                ccts_BusinessTerm.setText(businessTerm);
            }
        }

        if (bieOagiSrtMetaData) {
            Element srt_Version = new Element("srt_Version", OAGI_NS);
            documentation.addContent(srt_Version);

            Element srt_Status = new Element("srt_Status", OAGI_NS);
            documentation.addContent(srt_Status);

            Element srt_Remark = new Element("srt_Remark", OAGI_NS);
            documentation.addContent(srt_Remark);

            Element srt_CreatedByUserName = new Element("srt_CreatedByUserName", OAGI_NS);
            documentation.addContent(srt_CreatedByUserName);

            Element srt_LastUpdatedByUserName = new Element("srt_LastUpdatedByUserName", OAGI_NS);
            documentation.addContent(srt_LastUpdatedByUserName);

            Element srt_CreationTimestamp = new Element("srt_CreationTimestamp", OAGI_NS);
            documentation.addContent(srt_CreationTimestamp);

            Element srt_LastUpdateTimestamp = new Element("srt_LastUpdateTimestamp", OAGI_NS);
            documentation.addContent(srt_LastUpdateTimestamp);
        }

        if (basedCcMetaData) {

        }

    }

    public Element generateTopLevelASBIEP(AssociationBusinessInformationEntityProperty asbiep, TopLevelAbie topLevelAbie) {
        if (isProcessed(asbiep)) {
            return getProcessedElement(asbiep);
        }

        AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
        Element rootEleNode = newElement("element");
        schemaNode.addContent(rootEleNode);

        rootEleNode.setAttribute("name", asccp.getPropertyTerm().replaceAll(" ", ""));
        if (option.isBieGuid()) {
            rootEleNode.setAttribute("id", asbiep.getGuid());
        }

        setDefinition(rootEleNode, asbiep.getDefinition());
        setBusinessContext(rootEleNode, topLevelAbie);
        setOptionalDocumentation(rootEleNode, asbiep, asccp);

        setProcessedElement(asbiep, rootEleNode);

        return rootEleNode;
    }

    public Element generateABIE(AggregateBusinessInformationEntity abie, Element parentNode) {
        if (isProcessed(abie)) {
            return parentNode;
        }

        Element complexType = newElement("complexType");
        if (option.isBieGuid()) {
            complexType.setAttribute("id", abie.getGuid());
        }
        parentNode.addContent(complexType);

        AggregateCoreComponent acc = generationContext.queryBasedACC(abie);
        setDefinition(complexType, abie.getDefinition());

        Element sequenceElement = newElement("sequence");
        complexType.addContent(sequenceElement);
        setProcessedElement(abie, sequenceElement);

        return sequenceElement;
    }

    public void generateBIEs(AggregateBusinessInformationEntity abie, Element parent) {

        List<BusinessInformationEntity> childBIEs = generationContext.queryChildBIEs(abie);
        for (BusinessInformationEntity bie : childBIEs) {
            if (bie instanceof BasicBusinessInformationEntity) {
                BasicBusinessInformationEntity bbie = (BasicBusinessInformationEntity) bie;
                DataType bdt = generationContext.queryAssocBDT(bbie);
                generateBBIE(bbie, bdt, parent);
            } else {
                AssociationBusinessInformationEntity asbie = (AssociationBusinessInformationEntity) bie;

                if (isAnyProperty(asbie, generationContext)) {
                    generateAnyABIE(asbie, parent);
                } else {
                    Element node = generateASBIE(asbie, parent);
                    AssociationBusinessInformationEntityProperty asbiep = generationContext.queryAssocToASBIEP(asbie);
                    Element asbiepNode = generateASBIEP(asbiep, node);
                    AggregateBusinessInformationEntity childAbie = generationContext.queryTargetABIE2(asbiep);
                    Element sequenceNode = generateABIE(childAbie, asbiepNode);
                    generateBIEs(childAbie, sequenceNode);
                    if (sequenceNode.getChildren().isEmpty()) {
                        sequenceNode.detach();
                    }
                }
            }
        }
    }

    private Element generateAnyABIE(AssociationBusinessInformationEntity asbie, Element parent) {
        AssociationCoreComponent ascc = generationContext.queryBasedASCC(asbie);

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
            element.setAttribute("id", asbie.getGuid());
        }

        parent.addContent(element);
        return element;
    }

    public Element generateBDT(BasicBusinessInformationEntity bbie, Element eNode, CodeList codeList) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        DataType bdt = generationContext.queryBDT(bbie);

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");

        if (option.isBieGuid()) {
            complexType.setAttribute("id", Utility.generateGUID());
        }

        setDefinition(complexType, bbie.getDefinition());

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        extNode.setAttribute("base", getCodeListTypeName(codeList));
        eNode.addContent(complexType);
        return eNode;
    }

    public String setBDTBase(DataType bdt) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        addXbtSimpleType(xbt);
        return xbt.getBuiltInType();
    }

    public String setBDTBase(BasicBusinessInformationEntity bbie) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        addXbtSimpleType(xbt);
        return xbt.getBuiltInType();
    }

    public Element setBBIE_Attr_Type(DataType gBDT, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType xbt =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (xbt.getBuiltInType() != null) {
            gNode.setAttribute("type", xbt.getBuiltInType());
        }
        addXbtSimpleType(xbt);
        return gNode;
    }

    public Element setBBIE_Attr_Type(BasicBusinessInformationEntity gBBIE, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestri(gBBIE.getBdtPriRestriId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType xbt =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (xbt.getBuiltInType() != null) {
            gNode.setAttribute("type", xbt.getBuiltInType());
        }
        addXbtSimpleType(xbt);
        return gNode;
    }

    public Element generateBDT(BasicBusinessInformationEntity bbie, Element eNode) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");

        if (option.isBieGuid()) {
            complexType.setAttribute("id", Utility.generateGUID());
        }

        setDefinition(complexType, bbie.getDefinition());

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        DataType gBDT = generationContext.queryAssocBDT(bbie);

        if (bbie.getBdtPriRestriId() == 0)
            extNode.setAttribute("base", setBDTBase(gBDT));
        else {
            extNode.setAttribute("base", setBDTBase(bbie));
        }

        eNode.addContent(complexType);
        return eNode;
    }

    public Element generateASBIE(AssociationBusinessInformationEntity asbie, Element parent) {
        Element element = newElement("element");
        if (option.isBieGuid()) {
            element.setAttribute("id", asbie.getGuid());
        }
        element.setAttribute("minOccurs", String.valueOf(asbie.getCardinalityMin()));
        if (asbie.getCardinalityMax() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(asbie.getCardinalityMax()));
        if (asbie.isNillable())
            element.setAttribute("nillable", String.valueOf(asbie.isNillable()));

        while (!parent.getName().equals("sequence")) {
            parent = parent.getParentElement();
        }

        AssociationCoreComponent ascc = generationContext.queryBasedASCC(asbie);
        setDefinition(element, asbie.getDefinition());

        parent.addContent(element);
        return element;
    }

    public Element generateASBIEP(AssociationBusinessInformationEntityProperty asbiep, Element parent) {
        AssociationCoreComponentProperty asccp = generationContext.findASCCP(asbiep.getBasedAsccpId());
        parent.setAttribute("name", Utility.first(asccp.getDen(), true));
        return parent;
    }

    public Element handleElementBBIE(BasicBusinessInformationEntity bbie, Element eNode) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.getDen(), true));
        if (option.isBieGuid()) {
            eNode.setAttribute("id", bbie.getGuid());
        }

        if (bbie.getDefaultValue() != null && bbie.getFixedValue() != null) {
            System.out.println("Error");
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

        setDefinition(eNode, bbie.getDefinition());

        return eNode;
    }

    public Element handleAttributeBBIE(BasicBusinessInformationEntity bbie, Element eNode) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.getDen(), false));
        if (option.isBieGuid()) {
            eNode.setAttribute("id", bbie.getGuid());
        }

        if (bbie.getDefaultValue() != null && bbie.getFixedValue() != null) {
            System.out.println("Error");
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
        if (bbie.getCardinalityMin() >= 1)
            eNode.setAttribute("use", "required");
        else
            eNode.setAttribute("use", "optional");

        setDefinition(eNode, bbie.getDefinition());

        return eNode;
    }

    public Element setBBIEType(DataType bdt, Element gNode) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        if (xbt.getBuiltInType() != null) {
            gNode.setAttribute("type", xbt.getBuiltInType());
        }
        addXbtSimpleType(xbt);

        return gNode;
    }

    public Element setBBIEType(BasicBusinessInformationEntity bbie, Element gNode) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        if (xbt.getBuiltInType() != null) {
            gNode.setAttribute("type", xbt.getBuiltInType());
        }
        addXbtSimpleType(xbt);

        return gNode;
    }

    public void addXbtSimpleType(XSDBuiltInType xbt) {
        /*
         * Issue #521
         * If XBT has a value of schema definition, it is not XML Schema Built-in Type.
         * It should generated as the XML Schema simple type at the global level.
         */
        if (xbt == null || StringUtils.isEmpty(xbt.getSchemaDefinition())) {
            return;
        }
        if (rootElementNode == null) {
            return;
        }
        if (isProcessed(xbt)) {
            return;
        }

        String name = xbt.getBuiltInType();

        Element xbtNode = newElement("simpleType");
        xbtNode.setAttribute("name", name);
        if (option.isBieGuid()) {
            xbtNode.setAttribute("id", Utility.generateGUID());
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

            rootElementNode.addContent(xbtNode);
        } catch (JDOMException | IOException e) {
            throw new IllegalStateException("Error occurs while the schema definition loads for " + name, e);
        }

        setProcessedElement(xbt, xbtNode);
    }

    public Element generateBBIE(BasicBusinessInformationEntity bbie, DataType bdt, Element parent) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);

        Element eNode = newElement("element");
        eNode = handleElementBBIE(bbie, eNode);
        if (bcc.getEntityType() == BasicCoreComponentEntityType.Element) {
            while (!parent.getName().equals("sequence")) {
                parent = parent.getParentElement();
            }
            parent.addContent(eNode);

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeList codeList = getCodeList(generationContext, bbie, bdt);

            if (codeList == null) {
                if (bbie.getBdtPriRestriId() == 0) {
                    if (bbieScList.isEmpty()) {
                        eNode = setBBIEType(bdt, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(bbie, eNode);
                        eNode = generateSCs(bbie, eNode, bbieScList);
                        return eNode;
                    }
                } else {
                    if (bbieScList.isEmpty()) {
                        eNode = setBBIEType(bbie, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(bbie, eNode);
                        eNode = generateSCs(bbie, eNode, bbieScList);
                        return eNode;
                    }
                }
            } else {
                generateCodeList(codeList, bdt);

                if (bbieScList.isEmpty()) {
                    eNode.setAttribute("type", getCodeListTypeName(codeList));
                    return eNode;
                } else {
                    eNode = generateBDT(bbie, eNode, codeList);
                    eNode = generateSCs(bbie, eNode, bbieScList);
                    return eNode;
                }
            }
        } else {
            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeList codeList = getCodeList(generationContext, bbie, bdt);
            if (codeList == null) {
                if (bbie.getBdtPriRestriId() == 0) {
                    if (bbieScList.isEmpty()) {
                        eNode = newElement("attribute");
                        eNode = handleAttributeBBIE(bbie, eNode);

                        while (!parent.getName().equals("complexType")) {
                            parent = parent.getParentElement();
                        }

                        parent.addContent(eNode);
                        eNode = setBBIE_Attr_Type(bdt, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(bbie, eNode);
                        return eNode;
                    }
                } else {
                    if (bbieScList.isEmpty()) {
                        eNode = newElement("attribute");
                        eNode = handleAttributeBBIE(bbie, eNode);

                        while (!parent.getName().equals("complexType")) {
                            parent = parent.getParentElement();
                        }

                        parent.addContent(eNode);
                        eNode = setBBIE_Attr_Type(bbie, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(bbie, eNode);
                        return eNode;
                    }
                }
            } else {
                eNode = newElement("attribute");
                eNode = handleAttributeBBIE(bbie, eNode);

                while (!parent.getName().equals("complexType")) {
                    parent = parent.getParentElement();
                }

                parent.addContent(eNode);
                generateCodeList(codeList, bdt);

                if (bbieScList.isEmpty()) {
                    if (getCodeListTypeName(codeList) != null) {
                        eNode.setAttribute("type", getCodeListTypeName(codeList));
                    }
                    return eNode;
                } else {
                    if (bbie.getBdtPriRestriId() == 0) {
                        eNode = setBBIE_Attr_Type(bdt, eNode);
                        return eNode;
                    } else {
                        if (getCodeListTypeName(codeList) != null) {
                            eNode.setAttribute("type", getCodeListTypeName(codeList));
                        }
                        return eNode;
                    }
                }
            }
        }
    }

    public String setCodeListRestrictionAttr(DataType bdt) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
        if (bdtPriRestri.getCodeListId() != 0) {
            return "xsd:token";
        } else {
            CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtAwdPriXpsTypeMap.getXbtId());
            addXbtSimpleType(xbt);
            return xbt.getBuiltInType();
        }
    }

    public String setCodeListRestrictionAttr(DataTypeSupplementaryComponent dtSc) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(dtSc.getDtScId());
        if (bdtScPriRestri.getCodeListId() != 0) {
            return "xsd:token";
        } else {
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
            addXbtSimpleType(xbt);
            return xbt.getBuiltInType();
        }
    }

    public Element generateCodeList(CodeList codeList, DataType bdt) {
        return generateCodeList(codeList, setCodeListRestrictionAttr(bdt));
    }

    public Element generateCodeList(CodeList codeList, DataTypeSupplementaryComponent dtSc) {
        return generateCodeList(codeList, setCodeListRestrictionAttr(dtSc));
    }

    private Element generateCodeList(CodeList codeList, String codeListRestrictionAttr) {
        if (isProcessed(codeList)) {
            return getProcessedElement(codeList);
        }

        Element stNode = newElement("simpleType");

        stNode.setAttribute("name", getCodeListTypeName(codeList));
        if (option.isBieGuid()) {
            stNode.setAttribute("id", codeList.getGuid());
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

    public Element handleBBIESCvalue(BasicBusinessInformationEntitySupplementaryComponent bbieSc, Element aNode) {
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
        DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
        String representationTerm = dtSc.getRepresentationTerm();
        String propertyTerm = dtSc.getPropertyTerm();
        if ("Text".equals(representationTerm) ||
                "Indicator".equals(representationTerm) && "Preferred".equals(propertyTerm) ||
                propertyTerm.contains(representationTerm)) {
            aNode.setAttribute("name", Utility.toLowerCamelCase(propertyTerm));
        } else if ("Identifier".equals(representationTerm)) {
            aNode.setAttribute("name", Utility.toLowerCamelCase(propertyTerm).concat("ID"));
        } else {
            aNode.setAttribute("name", Utility.toLowerCamelCase(propertyTerm) + Utility.toCamelCase(representationTerm));
        }

        if (bbieSc.getCardinalityMin() >= 1) {
            aNode.setAttribute("use", "required");
        } else {
            aNode.setAttribute("use", "optional");
        }

        if (option.isBieGuid()) {
            aNode.setAttribute("id", bbieSc.getGuid());
        }
        setDefinition(aNode, bbieSc.getDefinition());

        return aNode;
    }

    public Element setBBIESCType(BasicBusinessInformationEntitySupplementaryComponent bbieSc, Element gNode) {
        DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
        if (dtSc != null) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(dtSc.getDtScId());
            if (bdtScPriRestri != null) {
                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                if (cdtScAwdPriXpsTypeMap != null) {
                    XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
                    if (xbt != null && xbt.getBuiltInType() != null) {
                        gNode.setAttribute("type", xbt.getBuiltInType());
                    }
                    addXbtSimpleType(xbt);
                }
            }
        }
        return gNode;
    }

    public Element setBBIESCType2(BasicBusinessInformationEntitySupplementaryComponent bbieSc, Element gNode) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
        XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
        if (xbt.getBuiltInType() != null) {
            gNode.setAttribute("type", xbt.getBuiltInType());
        }
        addXbtSimpleType(xbt);
        return gNode;

    }

    public Element generateSCs(BasicBusinessInformationEntity bbie, Element bbieElement,
                               List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList) {
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
            BasicBusinessInformationEntitySupplementaryComponent bbieSc = bbieScList.get(i);
            if (bbieSc.getCardinalityMax() == 0)
                continue;
            Element aNode = newElement("attribute");
            aNode = handleBBIESCvalue(bbieSc, aNode); //Generate a DOM Element Node, handle values

            //Get a code list object
            CodeList codeList = generationContext.getCodeList(bbieSc);
            if (codeList != null) {
                if (option.isBieGuid()) {
                    aNode.setAttribute("id", Utility.generateGUID());
                }
            }

            if (codeList == null) {
                AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);

                if (agencyIdList != null) {
                    if (option.isBieGuid()) {
                        aNode.setAttribute("id", agencyIdList.getGuid());
                    }
                }

                if (agencyIdList == null) {
                    long primRestriction = bbieSc.getDtScPriRestriId();
                    if (primRestriction == 0L)
                        aNode = setBBIESCType(bbieSc, aNode);
                    else
                        aNode = setBBIESCType2(bbieSc, aNode);
                } else {
                    AgencyIdListValue agencyIdListValue =
                            generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueId());
                    String agencyListTypeName = getAgencyListTypeName(agencyIdList, agencyIdListValue);

                    generateAgencyList(agencyIdList, agencyListTypeName);
                    if (!StringUtils.isEmpty(agencyListTypeName)) {
                        aNode.setAttribute("type", agencyListTypeName);
                    }
                }
            } else {
                DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
                generateCodeList(codeList, dtSc);

                if (getCodeListTypeName(codeList) != null) {
                    aNode.setAttribute("type", getCodeListTypeName(codeList));
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
            stNode.setAttribute("id", agencyIdList.getGuid());
        }

        Element rtNode = newElement("restriction");

        rtNode.setAttribute("base", "xsd:token");
        stNode.addContent(rtNode);

        List<AgencyIdListValue> agencyIdListValues =
                generationContext.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());

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

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ServiceApplication.class, args)) {
            ProfileBIEGenerateService profileBIEGenerateService = ctx.getBean(ProfileBIEGenerateService.class);
            ProfileBODGenerationOption option = new ProfileBODGenerationOption();
            option.setSchemaExpression(ProfileBODGenerationOption.SchemaExpression.XML);
            option.setSchemaPackage(ProfileBODGenerationOption.SchemaPackage.All);
            option.setBusinessContext(true);

            File schemaFile = profileBIEGenerateService.generateSchema(Arrays.asList(2L), option);
            for (String line : FileUtils.readLines(schemaFile)) {
                System.out.println(line);
            }

            FileUtils.deleteQuietly(schemaFile);
        }
    }
}
