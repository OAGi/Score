package org.oagi.srt.standalone;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StandaloneXMLSchema {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static org.jdom2.Namespace XSD_NAMESPACE = org.jdom2.Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private AgencyIdListValueRepository agencyIdListValueRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private CodeListValueRepository codeListValueRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private BasicCoreComponentPropertyRepository bccpRepository;

    @Autowired
    private AggregateCoreComponentRepository accRepository;

    @Autowired
    private AssociationCoreComponentPropertyRepository asccpRepository;

    @Autowired
    private BasicCoreComponentRepository bccRepository;

    @Autowired
    private AssociationCoreComponentRepository asccRepository;

    @Autowired
    private AssociationBusinessInformationEntityPropertyRepository asbiepRepository;

    @Autowired
    private AggregateBusinessInformationEntityRepository abieRepository;

    @Autowired
    private AssociationBusinessInformationEntityRepository asbieRepository;

    @Autowired
    private BasicBusinessInformationEntityRepository bbieRepository;

    @Autowired
    private BasicBusinessInformationEntitySupplementaryComponentRepository bbieScRepository;

    @Autowired
    private TopLevelAbieRepository topLevelAbieRepository;

    public String writeXSDFile(Document doc, String filename) throws IOException {
        String filepath = SRTConstants.BOD_FILE_PATH + filename + ".xsd";
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(filepath)))) {
            outputter.output(doc, outputStream);
            outputStream.flush();
        }

        System.out.println(filepath + " is generated");
        return filepath;
    }

    public Element generateSchema(Document doc) {
        Element schemaNode = newElement("schema");
        schemaNode.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("", "http://www.openapplications.org/oagis/10"));
        schemaNode.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace"));
        schemaNode.setAttribute("targetNamespace", "http://www.openapplications.org/oagis/10");
        schemaNode.setAttribute("elementFormDefault", "qualified");
        schemaNode.setAttribute("attributeFormDefault", "unqualified");
        doc.addContent(schemaNode);
        return schemaNode;
    }

    public Document generateTopLevelABIE(AssociationBusinessInformationEntityProperty tlASBIEP,
                                         Document tlABIEDOM, Element schemaNode,
                                         GenerationContext generationContext) {
        Element rootEleNode = generateTopLevelASBIEP(tlASBIEP, schemaNode, generationContext);
        AggregateBusinessInformationEntity aABIE = generationContext.queryTargetABIE(tlASBIEP);
        Element rootSeqNode = generateABIE(aABIE, rootEleNode, schemaNode, generationContext);
        schemaNode = generateBIEs(aABIE, rootSeqNode, schemaNode, generationContext);
        return tlABIEDOM;
    }

    private Element newElement(String localName) {
        return new Element(localName, XSD_NAMESPACE);
    }

    public Element generateTopLevelASBIEP(AssociationBusinessInformationEntityProperty tlASBIEP,
                                          Element gSchemaNode,
                                          GenerationContext generationContext) {

        AssociationCoreComponentProperty asccpVO = generationContext.queryBasedASCCP(tlASBIEP);

        //serm: What does this do?
        if (generationContext.isCCStored(tlASBIEP.getGuid()))
            return gSchemaNode;

        Element rootEleNode = newElement("element");
        gSchemaNode.addContent(rootEleNode);
        rootEleNode.setAttribute("name", asccpVO.getPropertyTerm().replaceAll(" ", ""));
        rootEleNode.setAttribute("id", tlASBIEP.getGuid()); //rootEleNode.setAttribute("id", asccpVO.getASCCPGuid());
        //rootEleNode.setAttribute("type", Utility.second(asccpVO.getDen()).replaceAll(" ", "")+"Type");
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
        //documentation.setTextContent(asccpVO.getDefinition());
        rootEleNode.addContent(annotation);
        annotation.addContent(documentation);

        //serm: what does this do?
        generationContext.addCCGuidIntoStoredCC(tlASBIEP.getGuid());

        return rootEleNode;
    }

    public Element generateABIE(AggregateBusinessInformationEntity gABIE, Element gElementNode,
                                Element gSchemaNode, GenerationContext generationContext) {
        //AggregateCoreComponent gACC = queryBasedACC(gABIE);

        if (generationContext.isCCStored(gABIE.getGuid()))
            return gElementNode;
        Element complexType = newElement("complexType");
        complexType.setAttribute("id", gABIE.getGuid());
        gElementNode.addContent(complexType);
        //serm: why is this one called generateACC - the function name is not sensible.
        Element PNode = generateACC(gABIE, complexType, gElementNode, generationContext);
        return PNode;
    }

    public Element generateACC(AggregateBusinessInformationEntity gABIE, Element complexType,
                               Element gElementNode, GenerationContext generationContext) {

        AggregateCoreComponent gACC = generationContext.queryBasedACC(gABIE);
        Element PNode = newElement("sequence");
        //***complexType.setAttribute("id", Utility.generateGUID()); 		
        generationContext.addCCGuidIntoStoredCC(gACC.getGuid());
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gACC.getDefinition());
        complexType.addContent(annotation);
        annotation.addContent(documentation);
        complexType.addContent(PNode);

        return PNode;
    }

    public Element generateBIEs(AggregateBusinessInformationEntity gABIE, Element gPNode,
                                Element gSchemaNode, GenerationContext generationContext) {
        AggregateCoreComponent gACC = generationContext.queryBasedACC(gABIE);
        String accDen = gACC.getDen();
        if (accDen.equalsIgnoreCase("Any User Area. Details") || accDen.equalsIgnoreCase("Signature. Details")) {
            Element any = newElement("any");
            any.setAttribute("namespace", "##any");
            gPNode.addContent(any);
        }

        List<BusinessInformationEntity> childBIEs = generationContext.queryChildBIEs(gABIE);
        for (BusinessInformationEntity aSRTObject : childBIEs) {
            if (aSRTObject instanceof BasicBusinessInformationEntity) {
                BasicBusinessInformationEntity childBIE = (BasicBusinessInformationEntity) aSRTObject;
                DataType aBDT = generationContext.queryAssocBDT(childBIE);
                generateBBIE(childBIE, aBDT, gPNode, gSchemaNode, generationContext);
            } else {
                AssociationBusinessInformationEntity childBIE = (AssociationBusinessInformationEntity) aSRTObject;
                Element node = generateASBIE(childBIE, gPNode, generationContext);
                AssociationBusinessInformationEntityProperty anASBIEP = generationContext.queryAssocToASBIEP(childBIE);
                node = generateASBIEP(generationContext, anASBIEP, node);
                AggregateBusinessInformationEntity anABIE = generationContext.queryTargetABIE2(anASBIEP);
                node = generateABIE(anABIE, node, gSchemaNode, generationContext);
                node = generateBIEs(anABIE, node, gSchemaNode, generationContext);
            }

        }
        return gSchemaNode;
    }

    public Element generateBDT(BasicBusinessInformationEntity gBBIE, Element eNode, Element gSchemaNode,
                               CodeList gCL, GenerationContext generationContext) {

        DataType bDT = generationContext.queryBDT(gBBIE);
//		if(isCCStored(bDT.getGuid()))
//			return eNode;

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");
        //complexType.setAttribute("name", Utility.DenToName(bDT.getDen())); 
        complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", bDT.getGuid());
        if (bDT.getDefinition() != null) {
            Element annotation = newElement("annotation");
            Element documentation = newElement("documentation");
            documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
            //documentation.setTextContent(bDT.getDefinition());
            complexType.addContent(annotation);
        }
        generationContext.addCCGuidIntoStoredCC(bDT.getGuid());

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        extNode.setAttribute("base", getCodeListTypeName(gCL));
        eNode.addContent(complexType);
        return eNode;
    }


    public String setBDTBase(GenerationContext generationContext, DataType gBDT) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());

        return aXSDBuiltInType.getBuiltInType();
    }

    public String setBDTBase(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction = gBBIE.getBdtPriRestri();
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());

        return aXSDBuiltInType.getBuiltInType();
    }

    public Element setBBIE_Attr_Type(GenerationContext generationContext, DataType gBDT, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getBuiltInType() != null) {
            gNode.setAttribute("type", aXSDBuiltInType.getBuiltInType());
        }
        return gNode;
    }

    public Element setBBIE_Attr_Type(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction = gBBIE.getBdtPriRestri();
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getBuiltInType() != null) {
            gNode.setAttribute("type", aXSDBuiltInType.getBuiltInType());
        }
        return gNode;
    }

    public Element generateBDT(BasicBusinessInformationEntity gBBIE,
                               Element eNode, GenerationContext generationContext) {

        DataType bDT = generationContext.queryBDT(gBBIE);
//		if(isCCStored(bDT.getGuid()))
//			return eNode;

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");
        //complexType.setAttribute("name", Utility.DenToName(bDT.getDen())); 
        complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", bDT.getGuid());
        if (bDT.getDefinition() != null) {
            Element annotation = newElement("annotation");
            Element documentation = newElement("documentation");
            documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
            //documentation.setTextContent(bDT.getDefinition());
            complexType.addContent(annotation);
        }
        generationContext.addCCGuidIntoStoredCC(bDT.getGuid());

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        DataType gBDT = generationContext.queryAssocBDT(gBBIE);

        if (gBBIE.getBdtPriRestri() == null)
            extNode.setAttribute("base", setBDTBase(generationContext, gBDT));
        else {
            extNode.setAttribute("base", setBDTBase(generationContext, gBBIE));
        }

        eNode.addContent(complexType);
        return eNode;
    }

    public Element generateASBIE(AssociationBusinessInformationEntity gASBIE,
                                 Element gPNode, GenerationContext generationContext) {

        AssociationCoreComponent gASCC = generationContext.queryBasedASCC(gASBIE);

        Element element = newElement("element");
        element.setAttribute("id", gASBIE.getGuid()); //element.setAttribute("id", gASCC.getASCCGuid());
        element.setAttribute("minOccurs", String.valueOf(gASBIE.getCardinalityMin()));
        if (gASBIE.getCardinalityMax() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(gASBIE.getCardinalityMax()));
        if (gASBIE.isNillable())
            element.setAttribute("nillable", String.valueOf(gASBIE.isNillable()));

        while (!gPNode.getName().equals("sequence")) {
            gPNode = (Element) gPNode.getParentElement();
        }
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gASBIE.getDefinition());
        annotation.addContent(documentation);
        element.addContent(annotation);
        gPNode.addContent(element);
        generationContext.addCCGuidIntoStoredCC(gASCC.getGuid());//check
        return element;
    }

    public Element generateASBIEP(GenerationContext generationContext,
                                  AssociationBusinessInformationEntityProperty gASBIEP, Element gElementNode) {
        AssociationCoreComponentProperty asccp = gASBIEP.getBasedAsccp();
        gElementNode.setAttribute("name", Utility.first(asccp.getDen(), true));
        //gElementNode.setAttribute("type", Utility.second(asccp.getDen())+"Type");
        return gElementNode;
    }

    public Element handleBBIE_Elementvalue(BasicBusinessInformationEntity gBBIE,
                                           Element eNode, GenerationContext generationContext) {

        BasicCoreComponent bccVO = generationContext.queryBasedBCC(gBBIE);
        eNode.setAttribute("name", Utility.second(bccVO.getDen(), true));
        eNode.setAttribute("id", gBBIE.getGuid()); //eNode.setAttribute("id", bccVO.getGuid());
        generationContext.addCCGuidIntoStoredCC(bccVO.getGuid());
        if (gBBIE.getDefaultValue() != null && gBBIE.getFixedValue() != null) {
            System.out.println("Error");
        }
        if (gBBIE.isNillable()) {
            eNode.setAttribute("nillable", "true");
        }
        if (gBBIE.getDefaultValue() != null && gBBIE.getDefaultValue().length() != 0) {
            eNode.setAttribute("default", gBBIE.getDefaultValue());
        }
        if (gBBIE.getFixedValue() != null && gBBIE.getFixedValue().length() != 0) {
            eNode.setAttribute("fixed", gBBIE.getFixedValue());
        }

        eNode.setAttribute("minOccurs", String.valueOf(gBBIE.getCardinalityMin()));
        if (gBBIE.getCardinalityMax() == -1)
            eNode.setAttribute("maxOccurs", "unbounded");
        else
            eNode.setAttribute("maxOccurs", String.valueOf(gBBIE.getCardinalityMax()));
        if (gBBIE.isNillable())
            eNode.setAttribute("nillable", String.valueOf(gBBIE.isNillable()));

        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gBBIE.getDefinition());
        annotation.addContent(documentation);
        eNode.addContent(annotation);

        return eNode;
    }

    public Element handleBBIE_Attributevalue(BasicBusinessInformationEntity gBBIE,
                                             Element eNode, GenerationContext generationContext) {

        BasicCoreComponent bccVO = generationContext.queryBasedBCC(gBBIE);
        eNode.setAttribute("name", Utility.second(bccVO.getDen(), false));
        eNode.setAttribute("id", gBBIE.getGuid()); //eNode.setAttribute("id", bccVO.getGuid());
        generationContext.addCCGuidIntoStoredCC(bccVO.getGuid());
        if (gBBIE.getDefaultValue() != null && gBBIE.getFixedValue() != null) {
            System.out.println("Error");
        }
        if (gBBIE.isNillable()) {
            eNode.setAttribute("nillable", "true");
        }
        if (gBBIE.getDefaultValue() != null && gBBIE.getDefaultValue().length() != 0) {
            eNode.setAttribute("default", gBBIE.getDefaultValue());
        }
        if (gBBIE.getFixedValue() != null && gBBIE.getFixedValue().length() != 0) {
            eNode.setAttribute("fixed", gBBIE.getFixedValue());
        }
        if (gBBIE.getCardinalityMin() >= 1)
            eNode.setAttribute("use", "required");
        else
            eNode.setAttribute("use", "optional");
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gBBIE.getDefinition());
        annotation.addContent(documentation);
        eNode.addContent(annotation);
        return eNode;
    }

    public CodeList getCodeList(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE, DataType gBDT) {
        CodeList aCL = null;

        if (gBBIE.getCodeList() != null) {
            aCL = gBBIE.getCodeList();
        }

        if (aCL == null) {
            BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction = gBBIE.getBdtPriRestri();
            if (aBDTPrimitiveRestriction.getCodeListId() != 0) {
                aCL = generationContext.findCodeList(aBDTPrimitiveRestriction.getCodeListId());
            }
        }

        if (aCL == null) {
            BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                    generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());

            if (aBDTPrimitiveRestriction.getCodeListId() != 0)
                aCL = generationContext.findCodeList(aBDTPrimitiveRestriction.getCodeListId());
            else
                aCL = null;
        }
        return aCL;
    }

    public Element setBBIEType(GenerationContext generationContext, DataType gBDT, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getBuiltInType() != null)
            gNode.setAttribute("type", aXSDBuiltInType.getBuiltInType());

        return gNode;
    }

    public Element setBBIEType(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction = gBBIE.getBdtPriRestri();
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());

        if (aXSDBuiltInType.getBuiltInType() != null)
            gNode.setAttribute("type", aXSDBuiltInType.getBuiltInType());

        return gNode;
    }

    public Element generateBBIE(BasicBusinessInformationEntity gBBIE, DataType gBDT, Element gPNode,
                                Element gSchemaNode, GenerationContext generationContext) {

        BasicCoreComponent gBCC = generationContext.queryBasedBCC(gBBIE);
        Element eNode = null;
        eNode = newElement("element");
        eNode = handleBBIE_Elementvalue(gBBIE, eNode, generationContext);
        if (gBCC.getEntityType() == 1) {
            while (!gPNode.getName().equals("sequence")) {
                gPNode = (Element) gPNode.getParentElement();
            }

            gPNode.addContent(eNode);

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(gBBIE);

            CodeList aCL = getCodeList(generationContext, gBBIE, gBDT);

            if (aCL == null) {
                if (gBBIE.getBdtPriRestri() == null) {
                    if (bbieScList.isEmpty()) {
                        eNode = setBBIEType(generationContext, gBDT, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(gBBIE, eNode, generationContext);
                        eNode = generateSCs(gBBIE, eNode, bbieScList, gSchemaNode, generationContext);
                        return eNode;
                    }
                } else {
                    if (bbieScList.isEmpty()) {
                        eNode = setBBIEType(generationContext, gBBIE, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(gBBIE, eNode, generationContext);
                        eNode = generateSCs(gBBIE, eNode, bbieScList, gSchemaNode, generationContext);
                        return eNode;
                    }
                }
            } else { //is aCL null?
                if (!generationContext.isCodeListGenerated(aCL)) {
                    generateCodeList(aCL, gBDT, gSchemaNode, generationContext);
                }
                if (bbieScList.isEmpty()) {
                    eNode.setAttribute("type", getCodeListTypeName(aCL));
                    return eNode;
                } else {
                    eNode = generateBDT(gBBIE, eNode, gSchemaNode, aCL, generationContext);
                    eNode = generateSCs(gBBIE, eNode, bbieScList, gSchemaNode, generationContext);
                    return eNode;
                }
            }


        } else {
            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(gBBIE);
            CodeList aCL = getCodeList(generationContext, gBBIE, gBDT);
            if (aCL == null) {
                if (gBBIE.getBdtPriRestri() == null) {
                    if (bbieScList.isEmpty()) {
                        eNode = newElement("attribute");
                        eNode = handleBBIE_Attributevalue(gBBIE, eNode, generationContext);

                        while (!gPNode.getName().equals("complexType")) {
                            gPNode = (Element) gPNode.getParentElement();
                        }

                        gPNode.addContent(eNode);
                        eNode = setBBIE_Attr_Type(generationContext, gBDT, eNode);
                        return eNode;
                    } else {
                        //eNode = setBBIE_Attr_Type(gBBIE, eNode);
                        eNode = generateBDT(gBBIE, eNode, generationContext);
                        return eNode;
                    }
                } else {
                    if (bbieScList.isEmpty()) {
                        eNode = newElement("attribute");
                        eNode = handleBBIE_Attributevalue(gBBIE, eNode, generationContext);

                        while (!gPNode.getName().equals("complexType")) {
                            gPNode = (Element) gPNode.getParentElement();
                        }

                        gPNode.addContent(eNode);
                        eNode = setBBIE_Attr_Type(generationContext, gBBIE, eNode);
                        return eNode;
                    } else {
                        //eNode = setBBIE_Attr_Type(gBBIE, eNode);
                        eNode = generateBDT(gBBIE, eNode, generationContext);
                        return eNode;
                    }
                }
            } else { //is aCL null?
                eNode = newElement("attribute");
                eNode = handleBBIE_Attributevalue(gBBIE, eNode, generationContext);

                while (!gPNode.getName().equals("complexType")) {
                    gPNode = (Element) gPNode.getParentElement();
                }

                gPNode.addContent(eNode);

                if (!generationContext.isCodeListGenerated(aCL)) {
                    generateCodeList(aCL, gBDT, gSchemaNode, generationContext);
                }
                if (bbieScList.isEmpty()) {
                    if (getCodeListTypeName(aCL) != null) {
                        eNode.setAttribute("type", getCodeListTypeName(aCL));
                    }
                    return eNode;
                } else {
                    if (gBBIE.getBdtPriRestri() == null) {
                        eNode = setBBIE_Attr_Type(generationContext, gBDT, eNode);
                        return eNode;
                    } else {
                        if (getCodeListTypeName(aCL) != null) {
                            eNode.setAttribute("type", getCodeListTypeName(aCL));
                        }
                        return eNode;
                    }
                }
            }
        }
    }

    public String getCodeListTypeName(CodeList gCL) { //confirm
        //String CodeListTypeName ="xsd:string";
        String CodeListTypeName = gCL.getName() + (gCL.getName().endsWith("Code") == true ? "" : "Code") + "ContentType";
        CodeListTypeName = CodeListTypeName.replaceAll(" ", "").replaceAll("oacl_", "");
        return CodeListTypeName;
    }

    public String setCodeListRestrictionAttr(GenerationContext generationContext, DataType gBDT) {
        BusinessDataTypePrimitiveRestriction dPrim = generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        if (dPrim.getCodeListId() != 0) {
            return "xsd:token";
        } else {
            CoreDataTypeAllowedPrimitiveExpressionTypeMap aCDTAllowedPrimitiveExpressionTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(dPrim.getCdtAwdPriXpsTypeMapId());
            XSDBuiltInType aXSDBuiltInType =
                    generationContext.findXSDBuiltInType(aCDTAllowedPrimitiveExpressionTypeMap.getXbtId());
            return aXSDBuiltInType.getBuiltInType();
        }
    }

    public String setCodeListRestrictionAttr(GenerationContext generationContext, DataTypeSupplementaryComponent gSC) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction dPrim =
                generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gSC.getDtScId());
        if (dPrim.getCodeListId() != 0) {
            return "xsd:token";
        } else {
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCAllowedPrimitiveExpressionTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(dPrim.getCdtScAwdPriXpsTypeMapId());
            XSDBuiltInType aXSDBuiltInType = generationContext.findXSDBuiltInType(aCDTSCAllowedPrimitiveExpressionTypeMap.getXbtId());
            return aXSDBuiltInType.getBuiltInType();
        }
    }

    public Element generateCodeList(CodeList gCL, DataType gBDT,
                                    Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = newElement("simpleType");
        stNode.setAttribute("name", getCodeListTypeName(gCL));

        stNode.setAttribute("id", gCL.getGuid());

        Element rtNode = newElement("restriction");
        rtNode.setAttribute("base", setCodeListRestrictionAttr(generationContext, gBDT));
        stNode.addContent(rtNode);

        List<CodeListValue> gCLVs = generationContext.getCodeListValues(gCL);
        for (int i = 0; i < gCLVs.size(); i++) {
            CodeListValue bCodeListValue = gCLVs.get(i);
            Element enumeration = newElement("enumeration");
            enumeration.setAttribute("value", bCodeListValue.getValue());
            rtNode.addContent(enumeration);
        }
        generationContext.addGuidIntoGuidArrayList(gCL.getGuid());
        gSchemaNode.addContent(stNode);
        return stNode;
    }

    public Element generateCodeList(CodeList gCL, DataTypeSupplementaryComponent gSC,
                                    Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = newElement("simpleType");
        stNode.setAttribute("name", getCodeListTypeName(gCL));

        stNode.setAttribute("id", gCL.getGuid());

        Element rtNode = newElement("restriction");

        rtNode.setAttribute("base", setCodeListRestrictionAttr(generationContext, gSC));
        stNode.addContent(rtNode);

        List<CodeListValue> gCLVs = generationContext.getCodeListValues(gCL);
        for (int i = 0; i < gCLVs.size(); i++) {
            CodeListValue bCodeListValue = gCLVs.get(i);
            Element enumeration = newElement("enumeration");
            enumeration.setAttribute("value", bCodeListValue.getValue());
            rtNode.addContent(enumeration);
        }
        generationContext.addGuidIntoGuidArrayList(gCL.getGuid());
        gSchemaNode.addContent(stNode);
        return stNode;
    }

    public Element handleBBIESCvalue(GenerationContext generationContext,
                                     BasicBusinessInformationEntitySupplementaryComponent aBBIESC, Element aNode) {
        //Handle gSC[i]
        if (aBBIESC.getDefaultValue() != null && aBBIESC.getFixedValue() != null) {
            System.out.println("default and fixed value options handling error");
        } else if (aBBIESC.getDefaultValue() != null && aBBIESC.getDefaultValue().length() != 0) {
            aNode.setAttribute("default", aBBIESC.getDefaultValue());
        } else if (aBBIESC.getFixedValue() != null && aBBIESC.getFixedValue().length() != 0) {
            aNode.setAttribute("fixed", aBBIESC.getFixedValue());
        }
        // Generate a DOM Attribute node
        /*
         * Section 3.8.1.22 GenerateSCs #2
         */
        DataTypeSupplementaryComponent dtSc = aBBIESC.getDtSc();
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


        if (aBBIESC.getCardinalityMin() >= 1) {
            aNode.setAttribute("use", "required");
        } else {
            aNode.setAttribute("use", "optional");
        }

        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(aBBIESC.getDefinition());
        annotation.addContent(documentation);
        aNode.addContent(annotation);

        return aNode;
    }

    public Element setBBIESCType(GenerationContext generationContext,
                                 BasicBusinessInformationEntitySupplementaryComponent gBBIESC, Element gNode) {
        DataTypeSupplementaryComponent gDTSC = gBBIESC.getDtSc();
        if (gDTSC != null) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId());
            if (bdtScPriRestri != null) {
                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                if (cdtScAwdPriXpsTypeMap != null) {
                    XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
                    if (xbt != null && xbt.getBuiltInType() != null) {
                        gNode.setAttribute("type", xbt.getBuiltInType());
                    }
                }
            }
        }
        return gNode;
    }

    public Element setBBIESCType2(GenerationContext generationContext,
                                  BasicBusinessInformationEntitySupplementaryComponent gBBIESC, Element gNode) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction aBDTSCPrimitiveRestriction = gBBIESC.getDtScPriRestri();
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCAllowedPrimitiveExpressionTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(aBDTSCPrimitiveRestriction.getCdtScAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType = generationContext.findXSDBuiltInType(aCDTSCAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getBuiltInType() != null) {
            gNode.setAttribute("type", aXSDBuiltInType.getBuiltInType());
        }
        return gNode;

    }

    public Element generateSCs(BasicBusinessInformationEntity gBBIE, Element gBBIENode,
                               List<BasicBusinessInformationEntitySupplementaryComponent> gSCs,
                               Element gSchemaNode, GenerationContext generationContext) {
        Element tNode = gBBIENode;
        while (true) {
            if (tNode.getName().equals("simpleType") || tNode.getName().equals("complexType"))
                break;
            List<Element> children = tNode.getChildren();
            tNode = children.get(children.size() - 1);
            //tNode = (Element) tNode.getParentElement();
        }
        List<Element> children = tNode.getChildren();
        for (Element child : children) {
            if (child.getName().equals("simpleContent")) {
                tNode = child.getChildren().get(0);
                break;
            }
        }

//		if(tNode.getFirstChild().getName().equals("simpleContent"))
//			tNode = (Element) tNode.getFirstChild().getFirstChild();
        //here
        for (int i = 0; i < gSCs.size(); i++) {
            BasicBusinessInformationEntitySupplementaryComponent aBBIESC = gSCs.get(i);
            if (aBBIESC.getCardinalityMax() == 0)
                continue;
            Element aNode = newElement("attribute");
            aNode = handleBBIESCvalue(generationContext, aBBIESC, aNode); //Generate a DOM Element Node, handle values

            //Get a code list object
            CodeList aCL = generationContext.getCodeList(aBBIESC);
            if (aCL != null) {
                aNode.setAttribute("id", Utility.generateGUID());
            }

            AgencyIdList aAL = new AgencyIdList();

            if (aCL == null) { //aCL = null?
                aAL = generationContext.getAgencyIdList(aBBIESC);

                if (aAL != null) {
                    aNode.setAttribute("id", aAL.getGuid());
                }

                if (aAL == null) { //aAL = null?
                    BusinessDataTypeSupplementaryComponentPrimitiveRestriction primRestriction = aBBIESC.getDtScPriRestri();
                    if (primRestriction == null)
                        aNode = setBBIESCType(generationContext, aBBIESC, aNode);
                    else
                        aNode = setBBIESCType2(generationContext, aBBIESC, aNode);
                } else { //aAL = null?
                    if (!generationContext.isAgencyListGenerated(aAL))  //isAgencyListGenerated(aAL)?
                        generateAgencyList(aAL, aBBIESC, gSchemaNode, generationContext);
                    if (getAgencyListTypeName(aAL) != null) {
                        aNode.setAttribute("type", getAgencyListTypeName(aAL));
                    }
                }
            } else { //aCL = null?
                if (!generationContext.isCodeListGenerated(aCL)) {
                    DataTypeSupplementaryComponent aDTSC = aBBIESC.getDtSc();
                    generateCodeList(aCL, aDTSC, gSchemaNode, generationContext);
                }
                if (getCodeListTypeName(aCL) != null) {
                    aNode.setAttribute("type", getCodeListTypeName(aCL));
                }
            }
//			if(isCCStored(aNode.getAttribute("id")))
//				continue;
//			storedCC.add(aNode.getAttribute("id"));
            tNode.addContent(aNode);
        }
        return tNode;
    }

    public String getAgencyListTypeName(AgencyIdList gAL) {
        String AgencyListTypeName = "clm" + gAL.getAgencyIdListValueId() + gAL.getListId() + gAL.getVersionId() + "_" + Utility.toCamelCase(gAL.getName()) + "ContentType";
        AgencyListTypeName = AgencyListTypeName.replaceAll(" ", "");
        return AgencyListTypeName;
    }

    public Element generateAgencyList(AgencyIdList gAL, BasicBusinessInformationEntitySupplementaryComponent gSC,
                                      Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = newElement("simpleType");

        stNode.setAttribute("name", getAgencyListTypeName(gAL));
        stNode.setAttribute("id", gAL.getGuid());

        Element rtNode = newElement("restriction");

        rtNode.setAttribute("base", "xsd:token");
        stNode.addContent(rtNode);

        List<AgencyIdListValue> gALVs = generationContext.findAgencyIdListValueByOwnerListId(gAL.getAgencyIdListId());

        for (int i = 0; i < gALVs.size(); i++) {
            AgencyIdListValue aAgencyIdListValue = gALVs.get(i);
            Element enumeration = newElement("enumeration");
            stNode.addContent(enumeration);
            stNode.setAttribute("value", aAgencyIdListValue.getValue());
        }
        gSchemaNode.addContent(stNode);
        generationContext.addGuidIntoGuidArrayList(gAL.getGuid());
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

    class ValueComparator implements Comparator<BusinessInformationEntity> {

        Map<BusinessInformationEntity, Double> base;

        public ValueComparator(Map<BusinessInformationEntity, Double> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(BusinessInformationEntity a, BusinessInformationEntity b) {
            if (base.get(a) <= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }

    private class GenerationContext {
        private List<Integer> abieIds = new ArrayList();
        private List<String> storedCC = new ArrayList();
        private List<String> guidArrayList = new ArrayList();

        public GenerationContext(TopLevelAbie topLevelAbie) {
            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList = bdtPriRestriRepository.findAll();
            findBdtPriRestriByBdtIdAndDefaultIsTrueMap = bdtPriRestriList.stream()
                    .filter(e -> e.isDefault())
                    .collect(Collectors.toMap(e -> e.getBdtId(), Function.identity()));
            findBdtPriRestriMap = bdtPriRestriList.stream()
                    .collect(Collectors.toMap(e -> e.getBdtPriRestriId(), Function.identity()));

            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestriList = bdtScPriRestriRepository.findAll();
            findBdtScPriRestriByBdtIdAndDefaultIsTrueMap = bdtScPriRestriList.stream()
                    .filter(e -> e.isDefault())
                    .collect(Collectors.toMap(e -> e.getBdtScId(), Function.identity()));
            findBdtScPriRestriMap = bdtScPriRestriList.stream()
                    .collect(Collectors.toMap(e -> e.getBdtScPriRestriId(), Function.identity()));

            List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAwdPriXpsTypeMapList = cdtAwdPriXpsTypeMapRepository.findAll();
            findCdtAwdPriXpsTypeMapMap = cdtAwdPriXpsTypeMapList.stream()
                    .collect(Collectors.toMap(e -> e.getCdtAwdPriXpsTypeMapId(), Function.identity()));

            List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtScAwdPriXpsTypeMapList = cdtScAwdPriXpsTypeMapRepository.findAll();
            findCdtScAwdPriXpsTypeMapMap = cdtScAwdPriXpsTypeMapList.stream()
                    .collect(Collectors.toMap(e -> e.getCdtScAwdPriXpsTypeMapId(), Function.identity()));

            List<XSDBuiltInType> xbtList = xbtRepository.findAll();
            findXSDBuiltInTypeMap = xbtList.stream()
                    .collect(Collectors.toMap(e -> e.getXbtId(), Function.identity()));

            List<CodeList> codeLists = codeListRepository.findAll();
            findCodeListMap = codeLists.stream()
                    .collect(Collectors.toMap(e -> e.getCodeListId(), Function.identity()));

            List<CodeListValue> codeListValues = codeListValueRepository.findAll();
            findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap = codeListValues.stream()
                    .filter(e -> e.isUsedIndicator())
                    .collect(Collectors.groupingBy(e -> e.getCodeListId()));

            List<AggregateCoreComponent> accList = accRepository.findAll();
            findACCMap = accList.stream()
                    .collect(Collectors.toMap(e -> e.getAccId(), Function.identity()));

            List<BasicCoreComponent> bccList = bccRepository.findAll();
            findBCCMap = bccList.stream()
                    .collect(Collectors.toMap(e -> e.getBccId(), Function.identity()));

            List<BasicCoreComponentProperty> bccpList = bccpRepository.findAll();
            findBCCPMap = bccpList.stream()
                    .collect(Collectors.toMap(e -> e.getBccpId(), Function.identity()));

            List<AssociationCoreComponent> asccList = asccRepository.findAll();
            findASCCMap = asccList.stream()
                    .collect(Collectors.toMap(e -> e.getAsccId(), Function.identity()));

            List<AssociationCoreComponentProperty> asccpList = asccpRepository.findAll();
            findASCCPMap = asccpList.stream()
                    .collect(Collectors.toMap(e -> e.getAsccpId(), Function.identity()));

            List<DataType> dataTypeList = dataTypeRepository.findAll();
            findDTMap = dataTypeList.stream()
                    .collect(Collectors.toMap(e -> e.getDtId(), Function.identity()));

            List<DataTypeSupplementaryComponent> dtScList = dtScRepository.findAll();
            findDtScMap = dtScList.stream()
                    .collect(Collectors.toMap(e -> e.getDtScId(), Function.identity()));

            List<AgencyIdList> agencyIdLists = agencyIdListRepository.findAll();
            findAgencyIdListMap = agencyIdLists.stream()
                    .collect(Collectors.toMap(e -> e.getAgencyIdListId(), Function.identity()));

            List<AgencyIdListValue> agencyIdListValues = agencyIdListValueRepository.findAll();
            findAgencyIdListValueByOwnerListIdMap = agencyIdListValues.stream()
                    .collect(Collectors.groupingBy(e -> e.getOwnerListId()));

            List<AggregateBusinessInformationEntity> abieList = abieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            findAbieMap = abieList.stream()
                    .collect(Collectors.toMap(e -> e.getAbieId(), Function.identity()));

            List<BasicBusinessInformationEntity> bbieList =
                    bbieRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
            findBbieByFromAbieIdAndUsedIsTrueMap = bbieList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getFromAbie().getAbieId()));

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList =
                    bbieScRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
            findBbieScByBbieIdAndUsedIsTrueMap = bbieScList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getBbie().getBbieId()));

            List<AssociationBusinessInformationEntity> asbieList =
                    asbieRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
            findAsbieByFromAbieIdAndUsedIsTrueMap = asbieList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getFromAbie().getAbieId()));

            List<AssociationBusinessInformationEntityProperty> asbiepList =
                    asbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            findASBIEPMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getAsbiepId(), Function.identity()));
            findAsbiepByRoleOfAbieIdMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getRoleOfAbie().getAbieId(), Function.identity()));
        }

        // Prepared Datas
        private Map<Long, BusinessDataTypePrimitiveRestriction> findBdtPriRestriByBdtIdAndDefaultIsTrueMap;

        public BusinessDataTypePrimitiveRestriction findBdtPriRestriByBdtIdAndDefaultIsTrue(long bdtId) {
            return findBdtPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtId);
        }

        private Map<Long, BusinessDataTypePrimitiveRestriction> findBdtPriRestriMap;

        public BusinessDataTypePrimitiveRestriction findBdtPriRestri(long bdtPriRestriId) {
            return findBdtPriRestriMap.get(bdtPriRestriId);
        }

        private Map<Long, CoreDataTypeAllowedPrimitiveExpressionTypeMap> findCdtAwdPriXpsTypeMapMap;

        public CoreDataTypeAllowedPrimitiveExpressionTypeMap findCdtAwdPriXpsTypeMap(long cdtAwdPriXpsTypeMapId) {
            return findCdtAwdPriXpsTypeMapMap.get(cdtAwdPriXpsTypeMapId);
        }

        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriByBdtIdAndDefaultIsTrueMap;

        public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findBdtScPriRestriByBdtScIdAndDefaultIsTrue(long bdtScId) {
            return findBdtScPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtScId);
        }

        private Map<Long, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriMap;

        public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findBdtScPriRestri(long bdtScPriRestriId) {
            return findBdtScPriRestriMap.get(bdtScPriRestriId);
        }

        private Map<Long, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findCdtScAwdPriXpsTypeMapMap;

        public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findCdtScAwdPriXpsTypeMap(long cdtScAwdPriXpsTypeMapId) {
            return findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId);
        }

        private Map<Long, XSDBuiltInType> findXSDBuiltInTypeMap;

        public XSDBuiltInType findXSDBuiltInType(long xbtId) {
            return findXSDBuiltInTypeMap.get(xbtId);
        }

        private Map<Long, CodeList> findCodeListMap;

        public CodeList findCodeList(long codeListId) {
            return findCodeListMap.get(codeListId);
        }

        private Map<Long, List<CodeListValue>> findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap;

        public List<CodeListValue> findCodeListValueByCodeListIdAndUsedIndicatorIsTrue(long codeListId) {
            return findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.containsKey(codeListId) ?
                    findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.get(codeListId) :
                    Collections.emptyList();
        }

        private Map<Long, AggregateCoreComponent> findACCMap;

        public AggregateCoreComponent findACC(long accId) {
            return findACCMap.get(accId);
        }

        private Map<Long, BasicCoreComponent> findBCCMap;

        public BasicCoreComponent findBCC(long bccId) {
            return findBCCMap.get(bccId);
        }

        private Map<Long, BasicCoreComponentProperty> findBCCPMap;

        public BasicCoreComponentProperty findBCCP(long bccpId) {
            return findBCCPMap.get(bccpId);
        }

        private Map<Long, AssociationCoreComponent> findASCCMap;

        public AssociationCoreComponent findASCC(long asccId) {
            return findASCCMap.get(asccId);
        }

        private Map<Long, AssociationCoreComponentProperty> findASCCPMap;

        public AssociationCoreComponentProperty findASCCP(long asccpId) {
            return findASCCPMap.get(asccpId);
        }

        private Map<Long, DataType> findDTMap;

        public DataType findDT(long dtId) {
            return findDTMap.get(dtId);
        }

        private Map<Long, DataTypeSupplementaryComponent> findDtScMap;

        public DataTypeSupplementaryComponent findDtSc(long dtScId) {
            return findDtScMap.get(dtScId);
        }

        private Map<Long, AgencyIdList> findAgencyIdListMap;

        public AgencyIdList findAgencyIdList(long agencyIdListId) {
            return findAgencyIdListMap.get(agencyIdListId);
        }

        private Map<Long, List<AgencyIdListValue>> findAgencyIdListValueByOwnerListIdMap;

        public List<AgencyIdListValue> findAgencyIdListValueByOwnerListId(long ownerListId) {
            return findAgencyIdListValueByOwnerListIdMap.containsKey(ownerListId) ?
                    findAgencyIdListValueByOwnerListIdMap.get(ownerListId) :
                    Collections.emptyList();
        }

        private Map<Long, AggregateBusinessInformationEntity> findAbieMap;

        public AggregateBusinessInformationEntity findAbie(long abieId) {
            return findAbieMap.get(abieId);
        }

        private Map<Long, List<BasicBusinessInformationEntity>> findBbieByFromAbieIdAndUsedIsTrueMap;

        public List<BasicBusinessInformationEntity> findBbieByFromAbieIdAndUsedIsTrue(long fromAbieId) {
            return findBbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                    findBbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                    Collections.emptyList();
        }

        private Map<Long, List<BasicBusinessInformationEntitySupplementaryComponent>>
                findBbieScByBbieIdAndUsedIsTrueMap;

        public List<BasicBusinessInformationEntitySupplementaryComponent> findBbieScByBbieIdAndUsedIsTrue(long bbieId) {
            return findBbieScByBbieIdAndUsedIsTrueMap.containsKey(bbieId) ?
                    findBbieScByBbieIdAndUsedIsTrueMap.get(bbieId) :
                    Collections.emptyList();
        }

        private Map<Long, List<AssociationBusinessInformationEntity>> findAsbieByFromAbieIdAndUsedIsTrueMap;

        public List<AssociationBusinessInformationEntity> findAsbieByFromAbieIdAndUsedIsTrue(long fromAbieId) {
            return findAsbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                    findAsbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                    Collections.emptyList();
        }

        private Map<Long, AssociationBusinessInformationEntityProperty> findASBIEPMap;

        public AssociationBusinessInformationEntityProperty findASBIEP(long asbiepId) {
            return findASBIEPMap.get(asbiepId);
        }

        private Map<Long, AssociationBusinessInformationEntityProperty> findAsbiepByRoleOfAbieIdMap;

        public AssociationBusinessInformationEntityProperty findAsbiepByRoleOfAbieId(long roleOfAbieId) {
            return findAsbiepByRoleOfAbieIdMap.get(roleOfAbieId);
        }

        // Prepared Datas end

        public void addCCGuidIntoStoredCC(String asbiepGuid) {
            storedCC.add(asbiepGuid);
        }

        public void addGuidIntoGuidArrayList(String agencyIdListGuid) {
            guidArrayList.add(agencyIdListGuid);
        }

        public boolean isCodeListGenerated(CodeList gCL) {
            for (int i = 0; i < guidArrayList.size(); i++) {
                if (gCL.getGuid().equals(guidArrayList.get(i)))
                    return true;
            }
            return false;
        }

        public boolean isCCStored(String id) {
            for (int i = 0; i < storedCC.size(); i++) {
                if (storedCC.get(i).equals(id)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isAgencyListGenerated(AgencyIdList gAL) {
            for (int i = 0; i < guidArrayList.size(); i++) {
                if (gAL.getGuid().equals(guidArrayList.get(i)))
                    return true;
            }
            return false;
        }

        public AggregateCoreComponent queryBasedACC(AggregateBusinessInformationEntity gABIE) {
            return gABIE.getBasedAcc();
        }

        // Get only Child BIEs whose is_used flag is true
        public List<BusinessInformationEntity> queryChildBIEs(AggregateBusinessInformationEntity gABIE) {
            List<BusinessInformationEntity> result;
            Map<BusinessInformationEntity, Double> sequence = new HashMap();
            ValueComparator bvc = new ValueComparator(sequence);
            Map<BusinessInformationEntity, Double> ordered_sequence = new TreeMap(bvc);

            List<AssociationBusinessInformationEntity> asbievo = findAsbieByFromAbieIdAndUsedIsTrue(gABIE.getAbieId());
            List<BasicBusinessInformationEntity> bbievo = findBbieByFromAbieIdAndUsedIsTrue(gABIE.getAbieId());

            for (BasicBusinessInformationEntity aBasicBusinessInformationEntity : bbievo) {
                if (aBasicBusinessInformationEntity.getCardinalityMax() != 0) //modify
                    sequence.put(aBasicBusinessInformationEntity, aBasicBusinessInformationEntity.getSeqKey());
            }

            for (AssociationBusinessInformationEntity aAssociationBusinessInformationEntity : asbievo) {
                if (aAssociationBusinessInformationEntity.getCardinalityMax() != 0)
                    sequence.put(aAssociationBusinessInformationEntity, aAssociationBusinessInformationEntity.getSeqKey());
            }

            ordered_sequence.putAll(sequence);
            Set set = ordered_sequence.entrySet();
            Iterator i = set.iterator();
            result = new ArrayList();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                result.add((BusinessInformationEntity) me.getKey());
            }

            return result;
        }

        // Get only SCs whose is_used is true.
        public List<BasicBusinessInformationEntitySupplementaryComponent> queryBBIESCs(BasicBusinessInformationEntity gBBIE) {
            long bbieId = gBBIE.getBbieId();
            return findBbieScByBbieIdAndUsedIsTrue(bbieId);
        }

        public AssociationBusinessInformationEntityProperty receiveASBIEP(long abieId) {
            return findAsbiepByRoleOfAbieId(abieId);
        }

        public DataType queryBDT(BasicBusinessInformationEntity gBBIE) {
            BasicCoreComponent gBCC = findBCC(gBBIE.getBasedBccId());
            BasicCoreComponentProperty aBasicCoreComponentProperty = findBCCP(gBCC.getToBccpId());
            DataType bDT = findDT(aBasicCoreComponentProperty.getBdtId());
            return bDT;
        }

        public AssociationCoreComponentProperty queryBasedASCCP(AssociationBusinessInformationEntityProperty gASBIEP) {
            AssociationCoreComponentProperty asccpVO = gASBIEP.getBasedAsccp();
            return asccpVO;
        }

        public AssociationCoreComponent queryBasedASCC(AssociationBusinessInformationEntity gASBIE) {
            AssociationCoreComponent gASCC = gASBIE.getBasedAscc();
            return gASCC;
        }

        public AggregateBusinessInformationEntity queryTargetABIE(AssociationBusinessInformationEntityProperty gASBIEP) {
            AggregateBusinessInformationEntity abievo = gASBIEP.getRoleOfAbie();
            return abievo;
        }

        public AggregateCoreComponent queryTargetACC(AssociationBusinessInformationEntityProperty gASBIEP) {
            AggregateBusinessInformationEntity abievo = gASBIEP.getRoleOfAbie();

            AggregateCoreComponent aAggregateCoreComponent = abievo.getBasedAcc();
            return aAggregateCoreComponent;
        }

        public AggregateBusinessInformationEntity queryTargetABIE2(AssociationBusinessInformationEntityProperty gASBIEP) {
            AggregateBusinessInformationEntity abieVO = gASBIEP.getRoleOfAbie();
            return abieVO;
        }

        public BasicCoreComponent queryBasedBCC(BasicBusinessInformationEntity gBBIE) {
            BasicCoreComponent bccVO = findBCC(gBBIE.getBasedBccId());
            return bccVO;
        }

        public CodeList getCodeList(BasicBusinessInformationEntitySupplementaryComponent gBBIESC) {
            CodeList codeList = gBBIESC.getCodeList();
            if (codeList != null) {
                return codeList;
            }

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri = gBBIESC.getDtScPriRestri();
            if (bdtScPriRestri != null) {
                return findCodeList(bdtScPriRestri.getCodeListId());
            } else {
                DataTypeSupplementaryComponent gDTSC = gBBIESC.getDtSc();
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bBDTSCPrimitiveRestriction =
                        findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId());
                if (bBDTSCPrimitiveRestriction != null) {
                    codeList = findCodeList(bBDTSCPrimitiveRestriction.getCodeListId());
                }
            }

            return codeList;
        }

        public AgencyIdList getAgencyIdList(BasicBusinessInformationEntitySupplementaryComponent gBBIESC) {
            AgencyIdList agencyIdList = findAgencyIdList(gBBIESC.getAgencyIdListId());
            if (agencyIdList != null) {
                return agencyIdList;
            }

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri = gBBIESC.getDtScPriRestri();
            if (bdtScPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
            }

            if (agencyIdList == null) {
                DataTypeSupplementaryComponent gDTSC = gBBIESC.getDtSc();
                bdtScPriRestri = findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId());
                if (bdtScPriRestri != null) {
                    agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
                }
            }
            return agencyIdList;
        }

        public List<CodeListValue> getCodeListValues(CodeList gCL) {
            return findCodeListValueByCodeListIdAndUsedIndicatorIsTrue(gCL.getCodeListId());
        }

        public AssociationBusinessInformationEntityProperty queryAssocToASBIEP(AssociationBusinessInformationEntity gASBIE) {
            AssociationBusinessInformationEntityProperty asbiepVO = gASBIE.getToAsbiep();
            return asbiepVO;
        }

        public DataType queryAssocBDT(BasicBusinessInformationEntity gBBIE) {
            BasicCoreComponent bccVO = findBCC(gBBIE.getBasedBccId());
            BasicCoreComponentProperty bccpVO = findBCCP(bccVO.getToBccpId());
            DataType aBDT = findDT(bccpVO.getBdtId());
            return aBDT;
        }
    }

    public String generateXMLSchema(List<Long> topLevelAbieIds, boolean schema_package_flag) throws Exception {
        String filepath = null;
        for (long topLevelAbieId : topLevelAbieIds) {
            TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
            GenerationContext generationContext = new GenerationContext(topLevelAbie);
            AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();

            Document doc = new Document();

            Element schemaNode = generateSchema(doc);
            String filename = null;
            if (schema_package_flag == true) {
                AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie.getAbieId());
                System.out.println("Generating Top Level ABIE w/ given AssociationBusinessInformationEntityProperty Id: " + asbiep.getAsbiepId());
                doc = generateTopLevelABIE(asbiep, doc, schemaNode, generationContext);
                AssociationCoreComponentProperty asccpvo = asbiep.getBasedAsccp();
                filename = asccpvo.getPropertyTerm().replaceAll(" ", "");
                filepath = writeXSDFile(doc, filename + "_standalone");
            } else {
                AssociationBusinessInformationEntityProperty asbiep =
                        generationContext.receiveASBIEP(abie.getAbieId());
                doc = new Document();
                schemaNode = generateSchema(doc);
                doc = generateTopLevelABIE(asbiep, doc, schemaNode, generationContext);
                writeXSDFile(doc, "Package/" + abie.getGuid());
                filepath = Zip.compression(Utility.generateGUID());
            }
        }

        return filepath;
    }
}