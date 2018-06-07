package org.oagi.srt.service.expression;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
        AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie.getAbieId());
        logger.debug("Generating Top Level ABIE w/ given AssociationBusinessInformationEntityProperty Id: " + asbiep.getAsbiepId());
        generateTopLevelABIE(asbiep);
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

    public Document generateTopLevelABIE(AssociationBusinessInformationEntityProperty asbiep) {
        rootElementNode = generateTopLevelASBIEP(asbiep);
        AggregateBusinessInformationEntity abie = generationContext.queryTargetABIE(asbiep);
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

    private void setDefinition(Element node, String contextDefinition, String componentDefinition) {
        ProfileBODGenerationOption.BIEDocumentationOption bieDocumentationOption = option.getBieDocumentationOption();
        if (bieDocumentationOption.isBieDefinition()) {
            String definition = contextDefinition;
            if (StringUtils.isEmpty(definition) && bieDocumentationOption.isInheritIfEmpty()) {
                definition = componentDefinition;
            }

            if (StringUtils.isEmpty(definition)) {
                return;
            }

            Element annotation = newElement("annotation");
            Element documentation = newElement("documentation");
            documentation.setAttribute("source", OAGI_NS);
            documentation.setText(definition);

            node.addContent(annotation);
            annotation.addContent(documentation);
        }
    }

    public Element generateTopLevelASBIEP(AssociationBusinessInformationEntityProperty asbiep) {
        if (isProcessed(asbiep)) {
            return getProcessedElement(asbiep);
        }

        AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
        Element rootEleNode = newElement("element");
        schemaNode.addContent(rootEleNode);

        rootEleNode.setAttribute("name", asccp.getPropertyTerm().replaceAll(" ", ""));
        rootEleNode.setAttribute("id", asbiep.getGuid());
        setDefinition(rootEleNode, asbiep.getDefinition(), asccp.getDefinition());

        setProcessedElement(asbiep, rootEleNode);

        return rootEleNode;
    }

    public Element generateABIE(AggregateBusinessInformationEntity abie, Element parentNode) {
        if (isProcessed(abie)) {
            return parentNode;
        }

        Element complexType = newElement("complexType");
        complexType.setAttribute("id", abie.getGuid());
        parentNode.addContent(complexType);

        //serm: why is this one called generateACC - the function name is not sensible.
        Element sequenceElement = generateACC(abie, complexType);
        setProcessedElement(abie, sequenceElement);

        return sequenceElement;
    }

    public Element generateACC(AggregateBusinessInformationEntity abie, Element complexType) {
        AggregateCoreComponent acc = generationContext.queryBasedACC(abie);
        Element sequenceElement = newElement("sequence");
        complexType.addContent(sequenceElement);

        setDefinition(complexType, abie.getDefinition(), acc.getDefinition());

        return sequenceElement;
    }

    public void generateBIEs(AggregateBusinessInformationEntity abie, Element gPNode) {

        List<BusinessInformationEntity> childBIEs = generationContext.queryChildBIEs(abie);
        for (BusinessInformationEntity bie : childBIEs) {
            if (bie instanceof BasicBusinessInformationEntity) {
                BasicBusinessInformationEntity childBIE = (BasicBusinessInformationEntity) bie;
                DataType bdt = generationContext.queryAssocBDT(childBIE);
                generateBBIE(childBIE, bdt, gPNode);
            } else {
                AssociationBusinessInformationEntity childBIE = (AssociationBusinessInformationEntity) bie;

                if (isAnyProperty(childBIE, generationContext)) {
                    generateAnyABIE(childBIE, gPNode);
                } else {
                    Element node = generateASBIE(childBIE, gPNode);
                    AssociationBusinessInformationEntityProperty anASBIEP = generationContext.queryAssocToASBIEP(childBIE);
                    node = generateASBIEP(anASBIEP, node);
                    AggregateBusinessInformationEntity anABIE = generationContext.queryTargetABIE2(anASBIEP);
                    node = generateABIE(anABIE, node);
                    generateBIEs(anABIE, node);
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

        element.setAttribute("id", asbie.getGuid());

        parent.addContent(element);
        return element;
    }

    public Element generateBDT(BasicBusinessInformationEntity bbie, Element eNode, CodeList codeList) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        DataType bdt = generationContext.queryBDT(bbie);

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");
        complexType.setAttribute("id", Utility.generateGUID());

        setDefinition(complexType, bbie.getDefinition(), bcc.getDefinition());

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
        complexType.setAttribute("id", Utility.generateGUID());
        setDefinition(complexType, bbie.getDefinition(), bcc.getDefinition());

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

    public Element generateASBIE(AssociationBusinessInformationEntity asbie, Element gPNode) {
        Element element = newElement("element");
        element.setAttribute("id", asbie.getGuid());
        element.setAttribute("minOccurs", String.valueOf(asbie.getCardinalityMin()));
        if (asbie.getCardinalityMax() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(asbie.getCardinalityMax()));
        if (asbie.isNillable())
            element.setAttribute("nillable", String.valueOf(asbie.isNillable()));

        while (!gPNode.getName().equals("sequence")) {
            gPNode = gPNode.getParentElement();
        }

        AssociationCoreComponent ascc = generationContext.queryBasedASCC(asbie);
        setDefinition(element, asbie.getDefinition(), ascc.getDefinition());

        gPNode.addContent(element);
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
        eNode.setAttribute("id", bbie.getGuid());

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

        setDefinition(eNode, bbie.getDefinition(), bcc.getDefinition());

        return eNode;
    }

    public Element handleAttributeBBIE(BasicBusinessInformationEntity bbie, Element eNode) {
        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.getDen(), false));
        eNode.setAttribute("id", bbie.getGuid());

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

        setDefinition(eNode, bbie.getDefinition(), bcc.getDefinition());

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
        xbtNode.setAttribute("id", Utility.generateGUID());

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
        stNode.setAttribute("id", codeList.getGuid());

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

        aNode.setAttribute("id", bbieSc.getGuid());
        setDefinition(aNode, bbieSc.getDefinition(), dtSc.getDefinition());

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
                aNode.setAttribute("id", Utility.generateGUID());
            }

            if (codeList == null) {
                AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);

                if (agencyIdList != null) {
                    aNode.setAttribute("id", agencyIdList.getGuid());
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
        stNode.setAttribute("id", agencyIdList.getGuid());

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

    public List<AggregateBusinessInformationEntity> receiveABIE(GenerationContext generationContext, List<Integer> abieIds) {
        List<AggregateBusinessInformationEntity> abieList = new ArrayList();
        for (int abieId : abieIds) {
            AggregateBusinessInformationEntity abie = generationContext.findAbie(abieId);
            if (abie != null) {
                abieList.add(abie);
            }
        }
        return abieList;
    }
}
