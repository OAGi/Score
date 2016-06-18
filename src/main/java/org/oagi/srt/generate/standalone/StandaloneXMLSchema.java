package org.oagi.srt.generate.standalone;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StandaloneXMLSchema {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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

    public String writeXSDFile(Document doc, String filename) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);

        String filepath = SRTConstants.BOD_FILE_PATH + filename + ".xsd";
        StreamResult result = new StreamResult(new File(filepath));
        transformer.transform(source, result);
        System.out.println(filepath + " is generated");
        return filepath;
    }

    public Element generateSchema(Document doc) {
        Element schemaNode = doc.createElement("xsd:schema");
        schemaNode.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
        schemaNode.setAttribute("xmlns", "http://www.openapplications.org/oagis/10");
        schemaNode.setAttribute("xmlns:xml", "http://www.w3.org/XML/1998/namespace");
        schemaNode.setAttribute("targetNamespace", "http://www.openapplications.org/oagis/10");
        schemaNode.setAttribute("elementFormDefault", "qualified");
        schemaNode.setAttribute("attributeFormDefault", "unqualified");
        doc.appendChild(schemaNode);
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

    public Element generateTopLevelASBIEP(AssociationBusinessInformationEntityProperty tlASBIEP,
                                          Element gSchemaNode,
                                          GenerationContext generationContext) {

        AssociationCoreComponentProperty asccpVO = generationContext.queryBasedASCCP(tlASBIEP);

        //serm: What does this do?
        if (generationContext.isCCStored(tlASBIEP.getGuid()))
            return gSchemaNode;

        Element rootEleNode = gSchemaNode.getOwnerDocument().createElement("xsd:element");
        gSchemaNode.appendChild(rootEleNode);
        rootEleNode.setAttribute("name", asccpVO.getPropertyTerm().replaceAll(" ", ""));
        rootEleNode.setAttribute("id", tlASBIEP.getGuid()); //rootEleNode.setAttribute("id", asccpVO.getASCCPGuid());
        //rootEleNode.setAttribute("type", Utility.second(asccpVO.getDen()).replaceAll(" ", "")+"Type");
        Element annotation = gSchemaNode.getOwnerDocument().createElement("xsd:annotation");
        Element documentation = gSchemaNode.getOwnerDocument().createElement("xsd:documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
        //documentation.setTextContent(asccpVO.getDefinition());
        rootEleNode.appendChild(annotation);
        annotation.appendChild(documentation);

        //serm: what does this do?
        generationContext.addCCGuidIntoStoredCC(tlASBIEP.getGuid());

        return rootEleNode;
    }

    public Element generateABIE(AggregateBusinessInformationEntity gABIE, Element gElementNode,
                                Element gSchemaNode, GenerationContext generationContext) {
        //AggregateCoreComponent gACC = queryBasedACC(gABIE);

        if (generationContext.isCCStored(gABIE.getGuid()))
            return gElementNode;
        Element complexType = gElementNode.getOwnerDocument().createElement("xsd:complexType");
        complexType.setAttribute("id", gABIE.getGuid());
        gElementNode.appendChild(complexType);
        //serm: why is this one called generateACC - the function name is not sensible.
        Element PNode = generateACC(gABIE, complexType, gElementNode, generationContext);
        return PNode;
    }

    public Element generateACC(AggregateBusinessInformationEntity gABIE, Element complexType,
                               Element gElementNode, GenerationContext generationContext) {

        AggregateCoreComponent gACC = generationContext.queryBasedACC(gABIE);
        Element PNode = complexType.getOwnerDocument().createElement("xsd:sequence");
        //***complexType.setAttribute("id", Utility.generateGUID()); 		
        generationContext.addCCGuidIntoStoredCC(gACC.getGuid());
        Element annotation = gElementNode.getOwnerDocument().createElement("xsd:annotation");
        Element documentation = gElementNode.getOwnerDocument().createElement("xsd:documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gACC.getDefinition());
        complexType.appendChild(annotation);
        annotation.appendChild(documentation);
        complexType.appendChild(PNode);

        return PNode;
    }

    public Element generateBIEs(AggregateBusinessInformationEntity gABIE, Element gPNode,
                                Element gSchemaNode, GenerationContext generationContext) {
        AggregateCoreComponent gACC = generationContext.queryBasedACC(gABIE);
        String accDen = gACC.getDen();
        if (accDen.equalsIgnoreCase("Any User Area. Details") || accDen.equalsIgnoreCase("Signature. Details")) {
            Element any = gPNode.getOwnerDocument().createElement("xsd:any");
            any.setAttribute("namespace", "##any");
            gPNode.appendChild(any);
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

        Document ownerDocument = eNode.getOwnerDocument();
        Element complexType = ownerDocument.createElement("xsd:complexType");
        Element simpleContent = ownerDocument.createElement("xsd:simpleContent");
        Element extNode = ownerDocument.createElement("xsd:extension");
        //complexType.setAttribute("name", Utility.DenToName(bDT.getDen())); 
        complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", bDT.getGuid());
        if (bDT.getDefinition() != null) {
            Element annotation = ownerDocument.createElement("xsd:annotation");
            Element documentation = ownerDocument.createElement("xsd:documentation");
            documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
            //documentation.setTextContent(bDT.getDefinition());
            complexType.appendChild(annotation);
        }
        generationContext.addCCGuidIntoStoredCC(bDT.getGuid());

        complexType.appendChild(simpleContent);
        simpleContent.appendChild(extNode);

        Attr base = extNode.getOwnerDocument().createAttribute("base");
        base.setNodeValue(getCodeListTypeName(gCL));
        extNode.setAttributeNode(base);
        eNode.appendChild(complexType);
        return eNode;
    }


    public Attr setBDTBase(GenerationContext generationContext, DataType gBDT, Attr base) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        base.setValue(aXSDBuiltInType.getBuiltInType());

        return base;
    }

    public Attr setBDTBase(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE, Attr base) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestri(gBBIE.getBdtPriRestriId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());

        base.setValue(aXSDBuiltInType.getBuiltInType());

        return base;
    }

    public Element setBBIE_Attr_Type(GenerationContext generationContext, DataType gBDT, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getBuiltInType() != null) {
            Attr type = gNode.getOwnerDocument().createAttribute("type");
            type.setValue(aXSDBuiltInType.getBuiltInType()); //type.setValue(Utility.toCamelCase(aXSDBuiltInType.getName())+"Type");
            gNode.setAttributeNode(type);
        }
        return gNode;
    }

    public Element setBBIE_Attr_Type(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE, Element gNode) {
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestri(gBBIE.getBdtPriRestriId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getBuiltInType() != null) {
            Attr type = gNode.getOwnerDocument().createAttribute("type");
            type.setValue(aXSDBuiltInType.getBuiltInType()); //type.setValue(Utility.toCamelCase(aXSDBuiltInType.getName())+"Type");
            gNode.setAttributeNode(type);
        }
        return gNode;
    }

    public Element generateBDT(BasicBusinessInformationEntity gBBIE,
                               Element eNode, GenerationContext generationContext) {

        DataType bDT = generationContext.queryBDT(gBBIE);
//		if(isCCStored(bDT.getGuid()))
//			return eNode;

        Document ownerDocument = eNode.getOwnerDocument();
        Element complexType = ownerDocument.createElement("xsd:complexType");
        Element simpleContent = ownerDocument.createElement("xsd:simpleContent");
        Element extNode = ownerDocument.createElement("xsd:extension");
        //complexType.setAttribute("name", Utility.DenToName(bDT.getDen())); 
        complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", bDT.getGuid());
        if (bDT.getDefinition() != null) {
            Element annotation = ownerDocument.createElement("xsd:annotation");
            Element documentation = ownerDocument.createElement("xsd:documentation");
            documentation.setAttribute("source", "http://www.openapplications.org/oagis/10");
            //documentation.setTextContent(bDT.getDefinition());
            complexType.appendChild(annotation);
        }
        generationContext.addCCGuidIntoStoredCC(bDT.getGuid());

        complexType.appendChild(simpleContent);
        simpleContent.appendChild(extNode);

        DataType gBDT = generationContext.queryAssocBDT(gBBIE);

        Attr base = extNode.getOwnerDocument().createAttribute("base");

        if (gBBIE.getBdtPriRestriId() == 0)
            base = setBDTBase(generationContext, gBDT, base);
        else {
            base = setBDTBase(generationContext, gBBIE, base);
        }
        extNode.setAttributeNode(base);
        eNode.appendChild(complexType);
        return eNode;
    }

    public Element generateASBIE(AssociationBusinessInformationEntity gASBIE,
                                 Element gPNode, GenerationContext generationContext) {

        AssociationCoreComponent gASCC = generationContext.queryBasedASCC(gASBIE);

        Element element = gPNode.getOwnerDocument().createElement("xsd:element");
        element.setAttribute("id", gASBIE.getGuid()); //element.setAttribute("id", gASCC.getASCCGuid());
        element.setAttribute("minOccurs", String.valueOf(gASBIE.getCardinalityMin()));
        if (gASBIE.getCardinalityMax() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(gASBIE.getCardinalityMax()));
        if (gASBIE.isNillable())
            element.setAttribute("nillable", String.valueOf(gASBIE.isNillable()));

        while (!gPNode.getNodeName().equals("xsd:sequence")) {
            gPNode = (Element) gPNode.getParentNode();
        }
        Element annotation = element.getOwnerDocument().createElement("xsd:annotation");
        Element documentation = element.getOwnerDocument().createElement("xsd:documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gASBIE.getDefinition());
        annotation.appendChild(documentation);
        element.appendChild(annotation);
        gPNode.appendChild(element);
        generationContext.addCCGuidIntoStoredCC(gASCC.getGuid());//check
        return element;
    }

    public Element generateASBIEP(GenerationContext generationContext,
                                  AssociationBusinessInformationEntityProperty gASBIEP, Element gElementNode) {
        AssociationCoreComponentProperty asccp = generationContext.findASCCP(gASBIEP.getBasedAsccpId());
        gElementNode.setAttribute("name", Utility.first(asccp.getDen(), true));
        //gElementNode.setAttribute("type", Utility.second(asccp.getDen())+"Type");
        return gElementNode;
    }

    public Element handleBBIE_Elementvalue(BasicBusinessInformationEntity gBBIE,
                                           Element eNode, GenerationContext generationContext) {

        BasicCoreComponent bccVO = generationContext.queryBasedBCC(gBBIE);
        Attr nameANode = eNode.getOwnerDocument().createAttribute("name");
        nameANode.setValue(Utility.second(bccVO.getDen(), true));
        eNode.setAttributeNode(nameANode);
        eNode.setAttribute("id", gBBIE.getGuid()); //eNode.setAttribute("id", bccVO.getGuid());
        generationContext.addCCGuidIntoStoredCC(bccVO.getGuid());
        if (gBBIE.getDefaultValue() != null && gBBIE.getFixedValue() != null) {
            System.out.println("Error");
        }
        if (gBBIE.isNillable()) {
            Attr nillable = eNode.getOwnerDocument().createAttribute("nillable");
            nillable.setValue("true");
            eNode.setAttributeNode(nillable);
        }
        if (gBBIE.getDefaultValue() != null && gBBIE.getDefaultValue().length() != 0) {
            Attr defaulta = eNode.getOwnerDocument().createAttribute("default");
            defaulta.setValue(gBBIE.getDefaultValue());
            eNode.setAttributeNode(defaulta);
        }
        if (gBBIE.getFixedValue() != null && gBBIE.getFixedValue().length() != 0) {
            Attr fixedvalue = eNode.getOwnerDocument().createAttribute("fixed");
            fixedvalue.setValue(gBBIE.getFixedValue());
            eNode.setAttributeNode(fixedvalue);
        }

        eNode.setAttribute("minOccurs", String.valueOf(gBBIE.getCardinalityMin()));
        if (gBBIE.getCardinalityMax() == -1)
            eNode.setAttribute("maxOccurs", "unbounded");
        else
            eNode.setAttribute("maxOccurs", String.valueOf(gBBIE.getCardinalityMax()));
        if (gBBIE.isNillable())
            eNode.setAttribute("nillable", String.valueOf(gBBIE.isNillable()));

        Element annotation = eNode.getOwnerDocument().createElement("xsd:annotation");
        Element documentation = eNode.getOwnerDocument().createElement("xsd:documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gBBIE.getDefinition());
        annotation.appendChild(documentation);
        eNode.appendChild(annotation);

        return eNode;
    }

    public Element handleBBIE_Attributevalue(BasicBusinessInformationEntity gBBIE,
                                             Element eNode, GenerationContext generationContext) {

        BasicCoreComponent bccVO = generationContext.queryBasedBCC(gBBIE);
        Attr nameANode = eNode.getOwnerDocument().createAttribute("name");
        nameANode.setValue(Utility.second(bccVO.getDen(), false));
        eNode.setAttributeNode(nameANode);
        eNode.setAttribute("id", gBBIE.getGuid()); //eNode.setAttribute("id", bccVO.getGuid());
        generationContext.addCCGuidIntoStoredCC(bccVO.getGuid());
        if (gBBIE.getDefaultValue() != null && gBBIE.getFixedValue() != null) {
            System.out.println("Error");
        }
        if (gBBIE.isNillable()) {
            Attr nillable = eNode.getOwnerDocument().createAttribute("nillable");
            nillable.setValue("true");
            eNode.setAttributeNode(nillable);
        }
        if (gBBIE.getDefaultValue() != null && gBBIE.getDefaultValue().length() != 0) {
            Attr defaulta = eNode.getOwnerDocument().createAttribute("default");
            defaulta.setValue(gBBIE.getDefaultValue());
            eNode.setAttributeNode(defaulta);
        }
        if (gBBIE.getFixedValue() != null && gBBIE.getFixedValue().length() != 0) {
            Attr fixedvalue = eNode.getOwnerDocument().createAttribute("fixed");
            fixedvalue.setValue(gBBIE.getFixedValue());
            eNode.setAttributeNode(fixedvalue);
        }
        if (gBBIE.getCardinalityMin() >= 1)
            eNode.setAttribute("use", "required");
        else
            eNode.setAttribute("use", "optional");
        Element annotation = eNode.getOwnerDocument().createElement("xsd:annotation");
        Element documentation = eNode.getOwnerDocument().createElement("xsd:documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(gBBIE.getDefinition());
        annotation.appendChild(documentation);
        eNode.appendChild(annotation);
        return eNode;
    }

    public CodeList getCodeList(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE, DataType gBDT) {
        CodeList aCL = null;

        if (gBBIE.getCodeListId() != 0) {
            aCL = generationContext.findCodeList(gBBIE.getCodeListId());
        }

        if (aCL == null) {
            BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                    generationContext.findBdtPriRestri(gBBIE.getBdtPriRestriId());
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
        Attr tNode = gNode.getOwnerDocument().createAttribute("type");

        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());
        tNode.setValue(aXSDBuiltInType.getBuiltInType());
        if (aXSDBuiltInType.getBuiltInType() != null)
            gNode.setAttributeNode(tNode);

        return gNode;
    }

    public Element setBBIEType(GenerationContext generationContext, BasicBusinessInformationEntity gBBIE, Element gNode) {
        Attr tNode = gNode.getOwnerDocument().createAttribute("type");

        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestri(gBBIE.getBdtPriRestriId());
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aDTAllowedPrimitiveExpressionTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(aBDTPrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType =
                generationContext.findXSDBuiltInType(aDTAllowedPrimitiveExpressionTypeMap.getXbtId());

        tNode.setValue(aXSDBuiltInType.getBuiltInType());
        if (aXSDBuiltInType.getBuiltInType() != null)
            gNode.setAttributeNode(tNode);

        return gNode;
    }

    public Element generateBBIE(BasicBusinessInformationEntity gBBIE, DataType gBDT, Element gPNode,
                                Element gSchemaNode, GenerationContext generationContext) {

        BasicCoreComponent gBCC = generationContext.queryBasedBCC(gBBIE);
        Element eNode = null;
        eNode = gPNode.getOwnerDocument().createElement("xsd:element");
        eNode = handleBBIE_Elementvalue(gBBIE, eNode, generationContext);
        if (gBCC.getEntityType() == 1) {
            while (!gPNode.getNodeName().equals("xsd:sequence")) {
                gPNode = (Element) gPNode.getParentNode();
            }

            gPNode.appendChild(eNode);

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(gBBIE);

            CodeList aCL = getCodeList(generationContext, gBBIE, gBDT);

            if (aCL == null) {
                if (gBBIE.getBdtPriRestriId() == 0) {
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
                    Attr tNode = eNode.getOwnerDocument().createAttribute("type");
                    tNode.setNodeValue(getCodeListTypeName(aCL));
                    eNode.setAttributeNode(tNode);
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
                if (gBBIE.getBdtPriRestriId() == 0) {
                    if (bbieScList.isEmpty()) {
                        eNode = gPNode.getOwnerDocument().createElement("xsd:attribute");
                        eNode = handleBBIE_Attributevalue(gBBIE, eNode, generationContext);

                        while (!gPNode.getNodeName().equals("xsd:complexType")) {
                            gPNode = (Element) gPNode.getParentNode();
                        }

                        gPNode.appendChild(eNode);
                        eNode = setBBIE_Attr_Type(generationContext, gBDT, eNode);
                        return eNode;
                    } else {
                        //eNode = setBBIE_Attr_Type(gBBIE, eNode);
                        eNode = generateBDT(gBBIE, eNode, generationContext);
                        return eNode;
                    }
                } else {
                    if (bbieScList.isEmpty()) {
                        eNode = gPNode.getOwnerDocument().createElement("xsd:attribute");
                        eNode = handleBBIE_Attributevalue(gBBIE, eNode, generationContext);

                        while (!gPNode.getNodeName().equals("xsd:complexType")) {
                            gPNode = (Element) gPNode.getParentNode();
                        }

                        gPNode.appendChild(eNode);
                        eNode = setBBIE_Attr_Type(generationContext, gBBIE, eNode);
                        return eNode;
                    } else {
                        //eNode = setBBIE_Attr_Type(gBBIE, eNode);
                        eNode = generateBDT(gBBIE, eNode, generationContext);
                        return eNode;
                    }
                }
            } else { //is aCL null?
                eNode = gPNode.getOwnerDocument().createElement("xsd:attribute");
                eNode = handleBBIE_Attributevalue(gBBIE, eNode, generationContext);

                while (!gPNode.getNodeName().equals("xsd:complexType")) {
                    gPNode = (Element) gPNode.getParentNode();
                }

                gPNode.appendChild(eNode);

                if (!generationContext.isCodeListGenerated(aCL)) {
                    generateCodeList(aCL, gBDT, gSchemaNode, generationContext);
                }
                if (bbieScList.isEmpty()) {
                    if (getCodeListTypeName(aCL) != null) {
                        Attr tNode = eNode.getOwnerDocument().createAttribute("type");
                        tNode.setNodeValue(getCodeListTypeName(aCL));
                        eNode.setAttributeNode(tNode);
                    }
                    return eNode;
                } else {
                    if (gBBIE.getBdtPriRestriId() == 0) {
                        eNode = setBBIE_Attr_Type(generationContext, gBDT, eNode);
                        return eNode;
                    } else {
                        if (getCodeListTypeName(aCL) != null) {
                            Attr tNode = eNode.getOwnerDocument().createAttribute("type");
                            tNode.setNodeValue(getCodeListTypeName(aCL));
                            eNode.setAttributeNode(tNode);
                        }
                        return eNode;
                    }
                }
            }
        }
    }

    public String getCodeListTypeName(CodeList gCL) { //confirm
        //String CodeListTypeName ="xsd:string";
        String CodeListTypeName = gCL.getName() + (gCL.getName().endsWith("Code") == true ? "" : "Code") + "ContentType" + "_" + gCL.getAgencyId() + "_" + gCL.getListId() + "_" + gCL.getVersionId();
        CodeListTypeName = CodeListTypeName.replaceAll(" ", "");
        return CodeListTypeName;
    }

    public Attr setCodeListRestrictionAttr(GenerationContext generationContext, DataType gBDT, Attr base) {
        BusinessDataTypePrimitiveRestriction dPrim = generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(gBDT.getDtId());
        if (dPrim.getCodeListId() != 0) {
            base.setNodeValue("xsd:token");
        } else {
            CoreDataTypeAllowedPrimitiveExpressionTypeMap aCDTAllowedPrimitiveExpressionTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(dPrim.getCdtAwdPriXpsTypeMapId());
            XSDBuiltInType aXSDBuiltInType =
                    generationContext.findXSDBuiltInType(aCDTAllowedPrimitiveExpressionTypeMap.getXbtId());
            base.setNodeValue(aXSDBuiltInType.getBuiltInType());
        }
        return base;
    }

    public Attr setCodeListRestrictionAttr(GenerationContext generationContext, DataTypeSupplementaryComponent gSC, Attr base) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction dPrim =
                generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gSC.getDtScId());
        if (dPrim.getCodeListId() != 0) {
            base.setNodeValue("xsd:token");
        } else {
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCAllowedPrimitiveExpressionTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(dPrim.getCdtScAwdPriXpsTypeMapId());
            XSDBuiltInType aXSDBuiltInType = generationContext.findXSDBuiltInType(aCDTSCAllowedPrimitiveExpressionTypeMap.getXbtId());
            base.setNodeValue(aXSDBuiltInType.getBuiltInType());
        }
        return base;
    }

    public Element generateCodeList(CodeList gCL, DataType gBDT,
                                    Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
        Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
        stNameNode.setValue(getCodeListTypeName(gCL));
        stNode.setAttributeNode(stNameNode);

        Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
        stIdNode.setValue(gCL.getGuid());
        stNode.setAttributeNode(stIdNode);

        Element rtNode = stNode.getOwnerDocument().createElement("xsd:restriction");
        Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
        base = setCodeListRestrictionAttr(generationContext, gBDT, base);
        rtNode.setAttributeNode(base);
        stNode.appendChild(rtNode);

        List<CodeListValue> gCLVs = generationContext.getCodeListValues(gCL);
        for (int i = 0; i < gCLVs.size(); i++) {
            CodeListValue bCodeListValue = gCLVs.get(i);
            Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
            Attr value = stNode.getOwnerDocument().createAttribute("value");
            value.setNodeValue(bCodeListValue.getValue());
            enumeration.setAttributeNode(value);
            rtNode.appendChild(enumeration);
        }
        generationContext.addGuidIntoGuidArrayList(gCL.getGuid());
        gSchemaNode.appendChild(stNode);
        return stNode;
    }

    public Element generateCodeList(CodeList gCL, DataTypeSupplementaryComponent gSC,
                                    Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");
        Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
        stNameNode.setValue(getCodeListTypeName(gCL));
        stNode.setAttributeNode(stNameNode);

        Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
        stIdNode.setValue(gCL.getGuid());
        stNode.setAttributeNode(stIdNode);

        Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");

        Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
        base = setCodeListRestrictionAttr(generationContext, gSC, base);
        rtNode.setAttributeNode(base);
        stNode.appendChild(rtNode);

        List<CodeListValue> gCLVs = generationContext.getCodeListValues(gCL);
        for (int i = 0; i < gCLVs.size(); i++) {
            CodeListValue bCodeListValue = gCLVs.get(i);
            Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
            Attr value = stNode.getOwnerDocument().createAttribute("value");
            value.setNodeValue(bCodeListValue.getValue());
            enumeration.setAttributeNode(value);
            rtNode.appendChild(enumeration);
        }
        generationContext.addGuidIntoGuidArrayList(gCL.getGuid());
        gSchemaNode.appendChild(stNode);
        return stNode;
    }

    public Element handleBBIESCvalue(GenerationContext generationContext,
                                     BasicBusinessInformationEntitySupplementaryComponent aBBIESC, Element aNode) {
        //Handle gSC[i]
        if (aBBIESC.getDefaultValue() != null && aBBIESC.getFixedValue() != null) {
            System.out.println("default and fixed value options handling error");
        } else if (aBBIESC.getDefaultValue() != null && aBBIESC.getDefaultValue().length() != 0) {
            Attr default_att = aNode.getOwnerDocument().createAttribute("default");
            default_att.setNodeValue(aBBIESC.getDefaultValue());
            aNode.setAttributeNode(default_att);
        } else if (aBBIESC.getFixedValue() != null && aBBIESC.getFixedValue().length() != 0) {
            Attr fixed_att = aNode.getOwnerDocument().createAttribute("fixed");
            fixed_att.setNodeValue(aBBIESC.getFixedValue());
            aNode.setAttributeNode(fixed_att);
        }
        // Generate a DOM Attribute node
        /*
         * Section 3.8.1.22 GenerateSCs #2
         */
        Attr aNameNode = aNode.getOwnerDocument().createAttribute("name");

        DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(aBBIESC.getDtScId());
        String representationTerm = dtSc.getRepresentationTerm();
        String propertyTerm = dtSc.getPropertyTerm();
        if ("Text".equals(representationTerm) ||
            "Indicator".equals(representationTerm) && "Preferred".equals(propertyTerm) ||
            propertyTerm.contains(representationTerm)) {
            aNameNode.setNodeValue(Utility.toLowerCamelCase(propertyTerm));
        } else if ("Identifier".equals(representationTerm)) {
            aNameNode.setNodeValue(Utility.toLowerCamelCase(propertyTerm).concat("ID"));
        } else {
            aNameNode.setNodeValue(Utility.toLowerCamelCase(propertyTerm) + Utility.toCamelCase(representationTerm));
        }

        aNode.setAttributeNode(aNameNode);
        if (aBBIESC.getMinCardinality() >= 1) {
            aNode.setAttribute("use", "required");
        } else {
            aNode.setAttribute("use", "optional");
        }

        Element annotation = aNode.getOwnerDocument().createElement("xsd:annotation");
        Element documentation = aNode.getOwnerDocument().createElement("xsd:documentation");
        documentation.setAttribute("source", "http://www.openapplications.org/oagis/10/platform/2");
        //documentation.setTextContent(aBBIESC.getDefinition());
        annotation.appendChild(documentation);
        aNode.appendChild(annotation);

        return aNode;
    }

    public Element setBBIESCType(GenerationContext generationContext,
                                 BasicBusinessInformationEntitySupplementaryComponent gBBIESC, Element gNode) {
        DataTypeSupplementaryComponent gDTSC = generationContext.findDtSc(gBBIESC.getDtScId());
        if (gDTSC != null) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId());
            if (bdtScPriRestri != null) {
                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                if (cdtScAwdPriXpsTypeMap != null) {
                    XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
                    if (xbt != null && xbt.getBuiltInType() != null) {
                        Attr aTypeNode = gNode.getOwnerDocument().createAttribute("type");
                        aTypeNode.setNodeValue(xbt.getBuiltInType());
                        gNode.setAttributeNode(aTypeNode);
                    }
                }
            }
        }
        return gNode;
    }

    public Element setBBIESCType2(GenerationContext generationContext,
                                  BasicBusinessInformationEntitySupplementaryComponent gBBIESC, Element gNode) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction aBDTSCPrimitiveRestriction =
                generationContext.findBdtScPriRestri(gBBIESC.getDtScPriRestriId());
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCAllowedPrimitiveExpressionTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(aBDTSCPrimitiveRestriction.getCdtScAwdPriXpsTypeMapId());
        XSDBuiltInType aXSDBuiltInType = generationContext.findXSDBuiltInType(aCDTSCAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getBuiltInType() != null) {
            Attr aTypeNode = gNode.getOwnerDocument().createAttribute("type");
            aTypeNode.setNodeValue(aXSDBuiltInType.getBuiltInType());
            gNode.setAttributeNode(aTypeNode);
        }
        return gNode;

    }

    public Element generateSCs(BasicBusinessInformationEntity gBBIE, Element gBBIENode,
                               List<BasicBusinessInformationEntitySupplementaryComponent> gSCs,
                               Element gSchemaNode, GenerationContext generationContext) {
        Element tNode = gBBIENode;
        while (true) {
            if (tNode.getNodeName().equals("xsd:simpleType") || tNode.getNodeName().equals("xsd:complexType"))
                break;
            tNode = (Element) tNode.getLastChild();
            //tNode = (Element) tNode.getParentNode();
        }
        Node tmp = tNode.getFirstChild();
        do {
            if (tmp.getNodeName().equals("xsd:simpleContent")) {
                tNode = (Element) tmp.getFirstChild();
                break;
            }
            if (tmp.getNextSibling() != null)
                tmp = tmp.getNextSibling();
        } while (tmp != null);

//		if(tNode.getFirstChild().getNodeName().equals("xsd:simpleContent"))
//			tNode = (Element) tNode.getFirstChild().getFirstChild();
        //here
        for (int i = 0; i < gSCs.size(); i++) {
            BasicBusinessInformationEntitySupplementaryComponent aBBIESC = gSCs.get(i);
            if (aBBIESC.getMaxCardinality() == 0)
                continue;
            Element aNode = tNode.getOwnerDocument().createElement("xsd:attribute");
            aNode = handleBBIESCvalue(generationContext, aBBIESC, aNode); //Generate a DOM Element Node, handle values

            //Get a code list object
            CodeList aCL = generationContext.getCodeList(aBBIESC);
            if (aCL != null) {
                Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
                stIdNode.setNodeValue(Utility.generateGUID());
                aNode.setAttributeNode(stIdNode);
            }

            AgencyIdList aAL = new AgencyIdList();

            if (aCL == null) { //aCL = null?
                aAL = generationContext.getAgencyIdList(aBBIESC);

                if (aAL != null) {
                    Attr stIdNode = aNode.getOwnerDocument().createAttribute("id");
                    stIdNode.setNodeValue(aAL.getGuid());
                    aNode.setAttributeNode(stIdNode);
                }

                if (aAL == null) { //aAL = null?
                    int primRestriction = aBBIESC.getDtScPriRestriId();
                    if (primRestriction == 0)
                        aNode = setBBIESCType(generationContext, aBBIESC, aNode);
                    else
                        aNode = setBBIESCType2(generationContext, aBBIESC, aNode);
                } else { //aAL = null?
                    if (!generationContext.isAgencyListGenerated(aAL))  //isAgencyListGenerated(aAL)?
                        generateAgencyList(aAL, aBBIESC, gSchemaNode, generationContext);
                    if (getAgencyListTypeName(aAL) != null) {
                        Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
                        aTypeNode.setNodeValue(getAgencyListTypeName(aAL));
                        aNode.setAttributeNode(aTypeNode);
                    }
                }
            } else { //aCL = null?
                if (!generationContext.isCodeListGenerated(aCL)) {
                    DataTypeSupplementaryComponent aDTSC = generationContext.findDtSc(aBBIESC.getDtScId());
                    generateCodeList(aCL, aDTSC, gSchemaNode, generationContext);
                }
                if (getCodeListTypeName(aCL) != null) {
                    Attr aTypeNode = aNode.getOwnerDocument().createAttribute("type");
                    aTypeNode.setNodeValue(getCodeListTypeName(aCL));
                    aNode.setAttributeNode(aTypeNode);
                }
            }
//			if(isCCStored(aNode.getAttribute("id")))
//				continue;
//			storedCC.add(aNode.getAttribute("id"));
            tNode.appendChild(aNode);
        }
        return tNode;
    }

    public String getAgencyListTypeName(AgencyIdList gAL) {
        String AgencyListTypeName = "clm" + gAL.getAgencyId() + gAL.getListId() + gAL.getVersionId() + "_" + Utility.toCamelCase(gAL.getName()) + "ContentType";
        AgencyListTypeName = AgencyListTypeName.replaceAll(" ", "");
        return AgencyListTypeName;
    }

    public Element generateAgencyList(AgencyIdList gAL, BasicBusinessInformationEntitySupplementaryComponent gSC,
                                      Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = gSchemaNode.getOwnerDocument().createElement("xsd:simpleType");

        Attr stNameNode = gSchemaNode.getOwnerDocument().createAttribute("name");
        stNameNode.setNodeValue(getAgencyListTypeName(gAL));
        stNode.setAttributeNode(stNameNode);

        Attr stIdNode = gSchemaNode.getOwnerDocument().createAttribute("id");
        stIdNode.setNodeValue(gAL.getGuid());
        stNode.setAttributeNode(stIdNode);

        Element rtNode = gSchemaNode.getOwnerDocument().createElement("xsd:restriction");

        Attr base = gSchemaNode.getOwnerDocument().createAttribute("base");
        base.setNodeValue("xsd:token");
        stNode.appendChild(rtNode);
        rtNode.setAttributeNode(base);

        List<AgencyIdListValue> gALVs = generationContext.findAgencyIdListValueByOwnerListId(gAL.getAgencyIdListId());

        for (int i = 0; i < gALVs.size(); i++) {
            AgencyIdListValue aAgencyIdListValue = gALVs.get(i);
            Element enumeration = stNode.getOwnerDocument().createElement("xsd:enumeration");
            stNode.appendChild(enumeration);
            Attr value = stNode.getOwnerDocument().createAttribute("value");
            value.setNodeValue(aAgencyIdListValue.getValue());
            stNode.setAttributeNode(value);
        }
        gSchemaNode.appendChild(stNode);
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

        Map<BusinessInformationEntity, Integer> base;

        public ValueComparator(Map<BusinessInformationEntity, Integer> base) {
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

        public GenerationContext() {
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

            List<AggregateBusinessInformationEntity> abieList = abieRepository.findAll();
            findAbieMap = abieList.stream()
                    .collect(Collectors.toMap(e -> e.getAbieId(), Function.identity()));

            List<BasicBusinessInformationEntity> bbieList = bbieRepository.findAll();
            findBbieByFromAbieIdAndUsedIsTrueMap = bbieList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = bbieScRepository.findAll();
            findBbieScByBbieIdAndUsedIsTrueMap = bbieScList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getBbieId()));

            List<AssociationBusinessInformationEntity> asbieList = asbieRepository.findAll();
            findAsbieByFromAbieIdAndUsedIsTrueMap = asbieList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

            List<AssociationBusinessInformationEntityProperty> asbiepList = asbiepRepository.findAll();
            findASBIEPMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getAsbiepId(), Function.identity()));
            findAsbiepByRoleOfAbieIdMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getRoleOfAbieId(), Function.identity()));
        }

        // Prepared Datas
        private Map<Integer, BusinessDataTypePrimitiveRestriction> findBdtPriRestriByBdtIdAndDefaultIsTrueMap;

        public BusinessDataTypePrimitiveRestriction findBdtPriRestriByBdtIdAndDefaultIsTrue(int bdtId) {
            return findBdtPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtId);
        }

        private Map<Integer, BusinessDataTypePrimitiveRestriction> findBdtPriRestriMap;

        public BusinessDataTypePrimitiveRestriction findBdtPriRestri(int bdtPriRestriId) {
            return findBdtPriRestriMap.get(bdtPriRestriId);
        }

        private Map<Integer, CoreDataTypeAllowedPrimitiveExpressionTypeMap> findCdtAwdPriXpsTypeMapMap;

        public CoreDataTypeAllowedPrimitiveExpressionTypeMap findCdtAwdPriXpsTypeMap(int cdtAwdPriXpsTypeMapId) {
            return findCdtAwdPriXpsTypeMapMap.get(cdtAwdPriXpsTypeMapId);
        }

        private Map<Integer, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriByBdtIdAndDefaultIsTrueMap;

        public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findBdtScPriRestriByBdtScIdAndDefaultIsTrue(int bdtScId) {
            return findBdtScPriRestriByBdtIdAndDefaultIsTrueMap.get(bdtScId);
        }

        private Map<Integer, BusinessDataTypeSupplementaryComponentPrimitiveRestriction> findBdtScPriRestriMap;

        public BusinessDataTypeSupplementaryComponentPrimitiveRestriction findBdtScPriRestri(int bdtScPriRestriId) {
            return findBdtScPriRestriMap.get(bdtScPriRestriId);
        }

        private Map<Integer, CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> findCdtScAwdPriXpsTypeMapMap;

        public CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap findCdtScAwdPriXpsTypeMap(int cdtScAwdPriXpsTypeMapId) {
            return findCdtScAwdPriXpsTypeMapMap.get(cdtScAwdPriXpsTypeMapId);
        }

        private Map<Integer, XSDBuiltInType> findXSDBuiltInTypeMap;

        public XSDBuiltInType findXSDBuiltInType(int xbtId) {
            return findXSDBuiltInTypeMap.get(xbtId);
        }

        private Map<Integer, CodeList> findCodeListMap;

        public CodeList findCodeList(int codeListId) {
            return findCodeListMap.get(codeListId);
        }

        private Map<Integer, List<CodeListValue>> findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap;

        public List<CodeListValue> findCodeListValueByCodeListIdAndUsedIndicatorIsTrue(int codeListId) {
            return findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.containsKey(codeListId) ?
                    findCodeListValueByCodeListIdAndUsedIndicatorIsTrueMap.get(codeListId) :
                    Collections.emptyList();
        }

        private Map<Integer, AggregateCoreComponent> findACCMap;

        public AggregateCoreComponent findACC(int accId) {
            return findACCMap.get(accId);
        }

        private Map<Integer, BasicCoreComponent> findBCCMap;

        public BasicCoreComponent findBCC(int bccId) {
            return findBCCMap.get(bccId);
        }

        private Map<Integer, BasicCoreComponentProperty> findBCCPMap;

        public BasicCoreComponentProperty findBCCP(int bccpId) {
            return findBCCPMap.get(bccpId);
        }

        private Map<Integer, AssociationCoreComponent> findASCCMap;

        public AssociationCoreComponent findASCC(int asccId) {
            return findASCCMap.get(asccId);
        }

        private Map<Integer, AssociationCoreComponentProperty> findASCCPMap;

        public AssociationCoreComponentProperty findASCCP(int asccpId) {
            return findASCCPMap.get(asccpId);
        }

        private Map<Integer, DataType> findDTMap;

        public DataType findDT(int dtId) {
            return findDTMap.get(dtId);
        }

        private Map<Integer, DataTypeSupplementaryComponent> findDtScMap;

        public DataTypeSupplementaryComponent findDtSc(int dtScId) {
            return findDtScMap.get(dtScId);
        }

        private Map<Integer, AgencyIdList> findAgencyIdListMap;

        public AgencyIdList findAgencyIdList(int agencyIdListId) {
            return findAgencyIdListMap.get(agencyIdListId);
        }

        private Map<Integer, List<AgencyIdListValue>> findAgencyIdListValueByOwnerListIdMap;

        public List<AgencyIdListValue> findAgencyIdListValueByOwnerListId(int ownerListId) {
            return findAgencyIdListValueByOwnerListIdMap.containsKey(ownerListId) ?
                    findAgencyIdListValueByOwnerListIdMap.get(ownerListId) :
                    Collections.emptyList();
        }

        private Map<Integer, AggregateBusinessInformationEntity> findAbieMap;

        public AggregateBusinessInformationEntity findAbie(int abieId) {
            return findAbieMap.get(abieId);
        }

        private Map<Integer, List<BasicBusinessInformationEntity>> findBbieByFromAbieIdAndUsedIsTrueMap;

        public List<BasicBusinessInformationEntity> findBbieByFromAbieIdAndUsedIsTrue(int fromAbieId) {
            return findBbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                    findBbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                    Collections.emptyList();
        }

        private Map<Integer, List<BasicBusinessInformationEntitySupplementaryComponent>>
                findBbieScByBbieIdAndUsedIsTrueMap;

        public List<BasicBusinessInformationEntitySupplementaryComponent> findBbieScByBbieIdAndUsedIsTrue(int bbieId) {
            return findBbieScByBbieIdAndUsedIsTrueMap.containsKey(bbieId) ?
                    findBbieScByBbieIdAndUsedIsTrueMap.get(bbieId) :
                    Collections.emptyList();
        }

        private Map<Integer, List<AssociationBusinessInformationEntity>> findAsbieByFromAbieIdAndUsedIsTrueMap;

        public List<AssociationBusinessInformationEntity> findAsbieByFromAbieIdAndUsedIsTrue(int fromAbieId) {
            return findAsbieByFromAbieIdAndUsedIsTrueMap.containsKey(fromAbieId) ?
                    findAsbieByFromAbieIdAndUsedIsTrueMap.get(fromAbieId) :
                    Collections.emptyList();
        }

        private Map<Integer, AssociationBusinessInformationEntityProperty> findASBIEPMap;

        public AssociationBusinessInformationEntityProperty findASBIEP(int asbiepId) {
            return findASBIEPMap.get(asbiepId);
        }

        private Map<Integer, AssociationBusinessInformationEntityProperty> findAsbiepByRoleOfAbieIdMap;

        public AssociationBusinessInformationEntityProperty findAsbiepByRoleOfAbieId(int roleOfAbieId) {
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
            int basedAccId = gABIE.getBasedAccId();
            return findACC(basedAccId);
        }

        // Get only Child BIEs whose is_used flag is true
        public List<BusinessInformationEntity> queryChildBIEs(AggregateBusinessInformationEntity gABIE) {
            List<BusinessInformationEntity> result;
            Map<BusinessInformationEntity, Integer> sequence = new HashMap();
            ValueComparator bvc = new ValueComparator(sequence);
            Map<BusinessInformationEntity, Integer> ordered_sequence = new TreeMap(bvc);

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
            int bbieId = gBBIE.getBbieId();
            return findBbieScByBbieIdAndUsedIsTrue(bbieId);
        }

        public AssociationBusinessInformationEntityProperty receiveASBIEP(int abieId) {
            return findAsbiepByRoleOfAbieId(abieId);
        }

        public DataType queryBDT(BasicBusinessInformationEntity gBBIE) {
            BasicCoreComponent gBCC = findBCC(gBBIE.getBasedBccId());
            BasicCoreComponentProperty aBasicCoreComponentProperty = findBCCP(gBCC.getToBccpId());
            DataType bDT = findDT(aBasicCoreComponentProperty.getBdtId());
            return bDT;
        }

        public AssociationCoreComponentProperty queryBasedASCCP(AssociationBusinessInformationEntityProperty gASBIEP) {
            AssociationCoreComponentProperty asccpVO = findASCCP(gASBIEP.getBasedAsccpId());
            return asccpVO;
        }

        public AssociationCoreComponent queryBasedASCC(AssociationBusinessInformationEntity gASBIE) {
            AssociationCoreComponent gASCC = findASCC(gASBIE.getBasedAscc());
            return gASCC;
        }

        public AggregateBusinessInformationEntity queryTargetABIE(AssociationBusinessInformationEntityProperty gASBIEP) {
            AggregateBusinessInformationEntity abievo = findAbie(gASBIEP.getRoleOfAbieId());
            return abievo;
        }

        public AggregateCoreComponent queryTargetACC(AssociationBusinessInformationEntityProperty gASBIEP) {
            AggregateBusinessInformationEntity abievo = findAbie(gASBIEP.getRoleOfAbieId());

            AggregateCoreComponent aAggregateCoreComponent = findACC(abievo.getBasedAccId());
            return aAggregateCoreComponent;
        }

        public AggregateBusinessInformationEntity queryTargetABIE2(AssociationBusinessInformationEntityProperty gASBIEP) {
            AggregateBusinessInformationEntity abieVO = findAbie(gASBIEP.getRoleOfAbieId());
            return abieVO;
        }

        public BasicCoreComponent queryBasedBCC(BasicBusinessInformationEntity gBBIE) {
            BasicCoreComponent bccVO = findBCC(gBBIE.getBasedBccId());
            return bccVO;
        }

        public CodeList getCodeList(BasicBusinessInformationEntitySupplementaryComponent gBBIESC) {
            CodeList codeList = findCodeList(gBBIESC.getCodeListId());
            if (codeList != null) {
                return codeList;
            }

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    findBdtScPriRestri(gBBIESC.getDtScPriRestriId());
            if (bdtScPriRestri != null) {
                return findCodeList(bdtScPriRestri.getCodeListId());
            } else {
                DataTypeSupplementaryComponent gDTSC = findDtSc(gBBIESC.getDtScId());
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

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    findBdtScPriRestri(gBBIESC.getDtScPriRestriId());
            if (bdtScPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
            }

            if (agencyIdList == null) {
                DataTypeSupplementaryComponent gDTSC = findDtSc(gBBIESC.getDtScId());
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
            AssociationBusinessInformationEntityProperty asbiepVO = findASBIEP(gASBIE.getToAsbiepId());
            return asbiepVO;
        }

        public DataType queryAssocBDT(BasicBusinessInformationEntity gBBIE) {
            BasicCoreComponent bccVO = findBCC(gBBIE.getBasedBccId());
            BasicCoreComponentProperty bccpVO = findBCCP(bccVO.getToBccpId());
            DataType aBDT = findDT(bccpVO.getBdtId());
            return aBDT;
        }
    }

    public String generateXMLSchema(List<Integer> abie_ids, boolean schema_package_flag) throws Exception {
        GenerationContext generationContext = new GenerationContext();

        List<AggregateBusinessInformationEntity> gABIE = receiveABIE(generationContext, abie_ids);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element schemaNode = generateSchema(doc);
        String filepath = null, filename = null;
        if (schema_package_flag == true) {
            for (AggregateBusinessInformationEntity abie : gABIE) {
                AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie.getAbieId());
                System.out.println("Generating Top Level ABIE w/ given AssociationBusinessInformationEntityProperty Id: " + asbiep.getAsbiepId());
                doc = generateTopLevelABIE(asbiep, doc, schemaNode, generationContext);
                AssociationCoreComponentProperty asccpvo = generationContext.findASCCP(asbiep.getBasedAsccpId());
                filename = asccpvo.getPropertyTerm().replaceAll(" ", "");
            }
            filepath = writeXSDFile(doc, filename + "_standalone");
        } else {
            for (AggregateBusinessInformationEntity aAggregateBusinessInformationEntity : gABIE) {
                AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(aAggregateBusinessInformationEntity.getAbieId());
                doc = docBuilder.newDocument();
                schemaNode = generateSchema(doc);
                doc = generateTopLevelABIE(asbiep, doc, schemaNode, generationContext);
                writeXSDFile(doc, "Package/" + aAggregateBusinessInformationEntity.getGuid());
            }
            filepath = Zip.compression(Utility.generateGUID());
        }

        return filepath;
    }
}