package org.oagi.srt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.oagi.srt.common.SRTConstants.OAGI_NS;

@Component
public class ProfileBODGenerateService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static org.jdom2.Namespace XSD_NAMESPACE = org.jdom2.Namespace.getNamespace("xsd", "http://www.w3.org/2001/XMLSchema");

    private static final String CODE_LIST_NAME_PREFIX = "cl";

    private static final String AGENCY_ID_LIST_NAME_PREFIX = "il";

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

    public Element generateSchema(Document doc) {
        Element schemaNode = newElement("schema");
        schemaNode.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("", OAGI_NS));
        schemaNode.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace"));
        schemaNode.setAttribute("targetNamespace", OAGI_NS);
        schemaNode.setAttribute("elementFormDefault", "qualified");
        schemaNode.setAttribute("attributeFormDefault", "unqualified");
        doc.addContent(schemaNode);
        return schemaNode;
    }

    public Document generateTopLevelABIE(AssociationBusinessInformationEntityProperty asbiep,
                                         Document tlABIEDOM, Element schemaNode,
                                         GenerationContext generationContext) {
        Element rootElementNode = generateTopLevelASBIEP(asbiep, schemaNode, generationContext);
        AggregateBusinessInformationEntity abie = generationContext.queryTargetABIE(asbiep);
        Element rootSeqNode = generateABIE(abie, rootElementNode, schemaNode, generationContext);
        schemaNode = generateBIEs(abie, rootSeqNode, schemaNode, generationContext);
        return tlABIEDOM;
    }

    private Element newElement(String localName) {
        return new Element(localName, XSD_NAMESPACE);
    }

    public Element generateTopLevelASBIEP(AssociationBusinessInformationEntityProperty asbiep,
                                          Element gSchemaNode,
                                          GenerationContext generationContext) {

        AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);

        //serm: What does this do?
        if (generationContext.isCCStored(asbiep.getGuid()))
            return gSchemaNode;

        Element rootEleNode = newElement("element");
        gSchemaNode.addContent(rootEleNode);
        rootEleNode.setAttribute("name", asccp.getPropertyTerm().replaceAll(" ", ""));
        rootEleNode.setAttribute("id", asbiep.getGuid()); //rootEleNode.setAttribute("id", asccpVO.getASCCPGuid());
        //rootEleNode.setAttribute("type", Utility.second(asccpVO.getDen()).replaceAll(" ", "")+"Type");
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", OAGI_NS);
        //documentation.setTextContent(asccpVO.getDefinition());
        rootEleNode.addContent(annotation);
        annotation.addContent(documentation);

        //serm: what does this do?
        generationContext.addCCGuidIntoStoredCC(asbiep.getGuid());

        return rootEleNode;
    }

    public Element generateABIE(AggregateBusinessInformationEntity abie, Element gElementNode,
                                Element gSchemaNode, GenerationContext generationContext) {
        //AggregateCoreComponent gACC = queryBasedACC(gABIE);

        if (generationContext.isCCStored(abie.getGuid()))
            return gElementNode;
        Element complexType = newElement("complexType");
        complexType.setAttribute("id", abie.getGuid());
        gElementNode.addContent(complexType);
        //serm: why is this one called generateACC - the function name is not sensible.
        Element PNode = generateACC(abie, complexType, gElementNode, generationContext);
        return PNode;
    }

    public Element generateACC(AggregateBusinessInformationEntity abie, Element complexType,
                               Element gElementNode, GenerationContext generationContext) {

        AggregateCoreComponent acc = generationContext.queryBasedACC(abie);
        Element PNode = newElement("sequence");
        //***complexType.setAttribute("id", Utility.generateGUID()); 		
        generationContext.addCCGuidIntoStoredCC(acc.getGuid());
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", OAGI_NS + "/platform/2");
        //documentation.setTextContent(gACC.getDefinition());
        complexType.addContent(annotation);
        annotation.addContent(documentation);
        complexType.addContent(PNode);

        return PNode;
    }

    public Element generateBIEs(AggregateBusinessInformationEntity abie, Element gPNode,
                                Element gSchemaNode, GenerationContext generationContext) {

        List<BusinessInformationEntity> childBIEs = generationContext.queryChildBIEs(abie);
        for (BusinessInformationEntity bie : childBIEs) {
            if (bie instanceof BasicBusinessInformationEntity) {
                BasicBusinessInformationEntity childBIE = (BasicBusinessInformationEntity) bie;
                DataType bdt = generationContext.queryAssocBDT(childBIE);
                generateBBIE(childBIE, bdt, gPNode, gSchemaNode, generationContext);
            } else {
                AssociationBusinessInformationEntity childBIE = (AssociationBusinessInformationEntity) bie;

                if (isAnyProperty(childBIE, generationContext)) {
                    generateAnyABIE(childBIE, gPNode, generationContext);
                } else {
                    Element node = generateASBIE(childBIE, gPNode, generationContext);
                    AssociationBusinessInformationEntityProperty anASBIEP = generationContext.queryAssocToASBIEP(childBIE);
                    node = generateASBIEP(generationContext, anASBIEP, node);
                    AggregateBusinessInformationEntity anABIE = generationContext.queryTargetABIE2(anASBIEP);
                    node = generateABIE(anABIE, node, gSchemaNode, generationContext);
                    node = generateBIEs(anABIE, node, gSchemaNode, generationContext);
                }
            }

        }

        return gSchemaNode;
    }

    private boolean isAnyProperty(AssociationBusinessInformationEntity asbie,
                                  GenerationContext generationContext) {
        AssociationBusinessInformationEntityProperty asbiep = generationContext.queryAssocToASBIEP(asbie);
        AssociationCoreComponentProperty asccp = generationContext.findASCCP(asbiep.getBasedAsccpId());
        if (!"AnyProperty".equals(Utility.first(asccp.getDen(), true))) {
            return false;
        }

        AggregateBusinessInformationEntity abie = generationContext.queryTargetABIE2(asbiep);
        AggregateCoreComponent acc = generationContext.queryBasedACC(abie);
        return OagisComponentType.Embedded == acc.getOagisComponentType();
    }

    private Element generateAnyABIE(AssociationBusinessInformationEntity gASBIE,
                                    Element gPNode, GenerationContext generationContext) {
        AssociationCoreComponent gASCC = generationContext.queryBasedASCC(gASBIE);

        Element element = newElement("any");
        element.setAttribute("namespace", "##any");
        element.setAttribute("processContents", "strict");

        element.setAttribute("minOccurs", String.valueOf(gASBIE.getCardinalityMin()));
        if (gASBIE.getCardinalityMax() == -1)
            element.setAttribute("maxOccurs", "unbounded");
        else
            element.setAttribute("maxOccurs", String.valueOf(gASBIE.getCardinalityMax()));
        if (gASBIE.isNillable())
            element.setAttribute("nillable", String.valueOf(gASBIE.isNillable()));

        element.setAttribute("id", gASBIE.getGuid());

        gPNode.addContent(element);
        generationContext.addCCGuidIntoStoredCC(gASCC.getGuid());//check

        return element;
    }

    public Element generateBDT(BasicBusinessInformationEntity bbie, Element eNode, Element gSchemaNode,
                               CodeList codeList, GenerationContext generationContext) {

        DataType bdt = generationContext.queryBDT(bbie);
//		if(isCCStored(bDT.getGuid()))
//			return eNode;

        Element complexType = newElement("complexType");
        Element simpleContent = newElement("simpleContent");
        Element extNode = newElement("extension");
        //complexType.setAttribute("name", Utility.DenToName(bDT.getDen())); 
        complexType.setAttribute("id", Utility.generateGUID()); //complexType.setAttribute("id", bDT.getGuid());
        if (bdt.getDefinition() != null) {
            Element annotation = newElement("annotation");
            Element documentation = newElement("documentation");
            documentation.setAttribute("source", OAGI_NS);
            //documentation.setTextContent(bDT.getDefinition());
            complexType.addContent(annotation);
        }
        generationContext.addCCGuidIntoStoredCC(bdt.getGuid());

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        extNode.setAttribute("base", getCodeListTypeName(codeList));
        eNode.addContent(complexType);
        return eNode;
    }

    public String setBDTBase(GenerationContext generationContext, DataType bdt) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        return xbt.getBuiltInType();
    }

    public String setBDTBase(GenerationContext generationContext, BasicBusinessInformationEntity bbie) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        return xbt.getBuiltInType();
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
        BusinessDataTypePrimitiveRestriction aBDTPrimitiveRestriction =
                generationContext.findBdtPriRestri(gBBIE.getBdtPriRestriId());
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
            documentation.setAttribute("source", OAGI_NS);
            //documentation.setTextContent(bDT.getDefinition());
            complexType.addContent(annotation);
        }
        generationContext.addCCGuidIntoStoredCC(bDT.getGuid());

        complexType.addContent(simpleContent);
        simpleContent.addContent(extNode);

        DataType gBDT = generationContext.queryAssocBDT(gBBIE);

        if (gBBIE.getBdtPriRestriId() == 0)
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
            gPNode = gPNode.getParentElement();
        }
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", OAGI_NS + "/platform/2");
        //documentation.setTextContent(gASBIE.getDefinition());
        annotation.addContent(documentation);
        element.addContent(annotation);
        gPNode.addContent(element);
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
        documentation.setAttribute("source", OAGI_NS + "/platform/2");
        //documentation.setTextContent(gBBIE.getDefinition());
        annotation.addContent(documentation);
        eNode.addContent(annotation);

        return eNode;
    }

    public Element handleBBIE_Attributevalue(BasicBusinessInformationEntity bbie,
                                             Element eNode, GenerationContext generationContext) {

        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        eNode.setAttribute("name", Utility.second(bcc.getDen(), false));
        eNode.setAttribute("id", bbie.getGuid()); //eNode.setAttribute("id", bcc.getGuid());
        generationContext.addCCGuidIntoStoredCC(bcc.getGuid());
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
        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", OAGI_NS + "/platform/2");
        //documentation.setTextContent(bbie.getDefinition());
        annotation.addContent(documentation);
        eNode.addContent(annotation);
        return eNode;
    }

    public CodeList getCodeList(GenerationContext generationContext, BasicBusinessInformationEntity bbie, DataType bdt) {
        CodeList codeList = null;

        if (bbie.getCodeListId() != 0) {
            codeList = generationContext.findCodeList(bbie.getCodeListId());
        }

        if (codeList == null) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
            if (bdtPriRestri != null && bdtPriRestri.getCodeListId() != 0) {
                codeList = generationContext.findCodeList(bdtPriRestri.getCodeListId());
            }
        }

        if (codeList == null) {
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());

            if (bdtPriRestri.getCodeListId() != 0)
                codeList = generationContext.findCodeList(bdtPriRestri.getCodeListId());
            else
                codeList = null;
        }
        return codeList;
    }

    public XSDBuiltInType getXbt(GenerationContext generationContext,
                                 BusinessDataTypePrimitiveRestriction bdtPriRestri) {
        CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
        XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtAwdPriXpsTypeMap.getXbtId());
        return xbt;
    }

    public Element setBBIEType(GenerationContext generationContext, DataType bdt, Element gNode) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        if (xbt.getBuiltInType() != null)
            gNode.setAttribute("type", xbt.getBuiltInType());

        return gNode;
    }

    public Element setBBIEType(GenerationContext generationContext, BasicBusinessInformationEntity bbie, Element gNode) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri =
                generationContext.findBdtPriRestri(bbie.getBdtPriRestriId());
        XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);
        if (xbt.getBuiltInType() != null)
            gNode.setAttribute("type", xbt.getBuiltInType());

        return gNode;
    }

    public Element generateBBIE(BasicBusinessInformationEntity bbie, DataType bdt, Element gPNode,
                                Element gSchemaNode, GenerationContext generationContext) {

        BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
        Element eNode;
        eNode = newElement("element");
        eNode = handleBBIE_Elementvalue(bbie, eNode, generationContext);
        if (bcc.getEntityType() == BasicCoreComponentEntityType.Element) {
            while (!gPNode.getName().equals("sequence")) {
                gPNode = gPNode.getParentElement();
            }

            gPNode.addContent(eNode);

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);

            CodeList aCL = getCodeList(generationContext, bbie, bdt);

            if (aCL == null) {
                if (bbie.getBdtPriRestriId() == 0) {
                    if (bbieScList.isEmpty()) {
                        eNode = setBBIEType(generationContext, bdt, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(bbie, eNode, generationContext);
                        eNode = generateSCs(bbie, eNode, bbieScList, gSchemaNode, generationContext);
                        return eNode;
                    }
                } else {
                    if (bbieScList.isEmpty()) {
                        eNode = setBBIEType(generationContext, bbie, eNode);
                        return eNode;
                    } else {
                        eNode = generateBDT(bbie, eNode, generationContext);
                        eNode = generateSCs(bbie, eNode, bbieScList, gSchemaNode, generationContext);
                        return eNode;
                    }
                }
            } else { //is aCL null?
                if (!generationContext.isCodeListGenerated(aCL)) {
                    generateCodeList(aCL, bdt, gSchemaNode, generationContext);
                }
                if (bbieScList.isEmpty()) {
                    eNode.setAttribute("type", getCodeListTypeName(aCL));
                    return eNode;
                } else {
                    eNode = generateBDT(bbie, eNode, gSchemaNode, aCL, generationContext);
                    eNode = generateSCs(bbie, eNode, bbieScList, gSchemaNode, generationContext);
                    return eNode;
                }
            }
        } else {
            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeList aCL = getCodeList(generationContext, bbie, bdt);
            if (aCL == null) {
                if (bbie.getBdtPriRestriId() == 0) {
                    if (bbieScList.isEmpty()) {
                        eNode = newElement("attribute");
                        eNode = handleBBIE_Attributevalue(bbie, eNode, generationContext);

                        while (!gPNode.getName().equals("complexType")) {
                            gPNode = (Element) gPNode.getParentElement();
                        }

                        gPNode.addContent(eNode);
                        eNode = setBBIE_Attr_Type(generationContext, bdt, eNode);
                        return eNode;
                    } else {
                        //eNode = setBBIE_Attr_Type(gBBIE, eNode);
                        eNode = generateBDT(bbie, eNode, generationContext);
                        return eNode;
                    }
                } else {
                    if (bbieScList.isEmpty()) {
                        eNode = newElement("attribute");
                        eNode = handleBBIE_Attributevalue(bbie, eNode, generationContext);

                        while (!gPNode.getName().equals("complexType")) {
                            gPNode = (Element) gPNode.getParentElement();
                        }

                        gPNode.addContent(eNode);
                        eNode = setBBIE_Attr_Type(generationContext, bbie, eNode);
                        return eNode;
                    } else {
                        //eNode = setBBIE_Attr_Type(gBBIE, eNode);
                        eNode = generateBDT(bbie, eNode, generationContext);
                        return eNode;
                    }
                }
            } else { //is aCL null?
                eNode = newElement("attribute");
                eNode = handleBBIE_Attributevalue(bbie, eNode, generationContext);

                while (!gPNode.getName().equals("complexType")) {
                    gPNode = (Element) gPNode.getParentElement();
                }

                gPNode.addContent(eNode);

                if (!generationContext.isCodeListGenerated(aCL)) {
                    generateCodeList(aCL, bdt, gSchemaNode, generationContext);
                }
                if (bbieScList.isEmpty()) {
                    if (getCodeListTypeName(aCL) != null) {
                        eNode.setAttribute("type", getCodeListTypeName(aCL));
                    }
                    return eNode;
                } else {
                    if (bbie.getBdtPriRestriId() == 0) {
                        eNode = setBBIE_Attr_Type(generationContext, bdt, eNode);
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

    public String getCodeListTypeName(CodeList codeList) {
        StringBuilder sb = new StringBuilder();

        sb.append(CODE_LIST_NAME_PREFIX);
        sb.append(codeList.getAgencyId()).append('_');
        sb.append(codeList.getVersionId()).append('_');
        String name = codeList.getName();
        if (!StringUtils.isEmpty(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(codeList.getListId());

        return sb.toString();
    }

    public String setCodeListRestrictionAttr(GenerationContext generationContext, DataType bdt) {
        BusinessDataTypePrimitiveRestriction bdtPriRestri = generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
        if (bdtPriRestri.getCodeListId() != 0) {
            return "xsd:token";
        } else {
            CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                    generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
            XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtAwdPriXpsTypeMap.getXbtId());
            return xbt.getBuiltInType();
        }
    }

    public String setCodeListRestrictionAttr(GenerationContext generationContext, DataTypeSupplementaryComponent dtSc) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(dtSc.getDtScId());
        if (bdtScPriRestri.getCodeListId() != 0) {
            return "xsd:token";
        } else {
            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                    generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
            XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
            return xbt.getBuiltInType();
        }
    }

    public Element generateCodeList(CodeList codeList, DataType bdt,
                                    Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = newElement("simpleType");
        stNode.setAttribute("name", getCodeListTypeName(codeList));

        stNode.setAttribute("id", codeList.getGuid());

        Element rtNode = newElement("restriction");
        rtNode.setAttribute("base", setCodeListRestrictionAttr(generationContext, bdt));
        stNode.addContent(rtNode);

        List<CodeListValue> codeListValues = generationContext.getCodeListValues(codeList);
        for (int i = 0; i < codeListValues.size(); i++) {
            CodeListValue codeListValue = codeListValues.get(i);
            Element enumeration = newElement("enumeration");
            enumeration.setAttribute("value", codeListValue.getValue());
            rtNode.addContent(enumeration);
        }
        generationContext.addGuidIntoGuidArrayList(codeList.getGuid());
        gSchemaNode.addContent(stNode);
        return stNode;
    }

    public Element generateCodeList(CodeList codeList, DataTypeSupplementaryComponent dtSc,
                                    Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = newElement("simpleType");
        stNode.setAttribute("name", getCodeListTypeName(codeList));

        stNode.setAttribute("id", codeList.getGuid());

        Element rtNode = newElement("restriction");

        rtNode.setAttribute("base", setCodeListRestrictionAttr(generationContext, dtSc));
        stNode.addContent(rtNode);

        List<CodeListValue> gCLVs = generationContext.getCodeListValues(codeList);
        for (int i = 0; i < gCLVs.size(); i++) {
            CodeListValue bCodeListValue = gCLVs.get(i);
            Element enumeration = newElement("enumeration");
            enumeration.setAttribute("value", bCodeListValue.getValue());
            rtNode.addContent(enumeration);
        }
        generationContext.addGuidIntoGuidArrayList(codeList.getGuid());
        gSchemaNode.addContent(stNode);
        return stNode;
    }

    public Element handleBBIESCvalue(GenerationContext generationContext,
                                     BasicBusinessInformationEntitySupplementaryComponent bbieSc, Element aNode) {
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

        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", OAGI_NS + "/platform/2");
        //documentation.setTextContent(aBBIESC.getDefinition());
        annotation.addContent(documentation);
        aNode.addContent(annotation);

        return aNode;
    }

    public Element setBBIESCType(GenerationContext generationContext,
                                 BasicBusinessInformationEntitySupplementaryComponent bbieSc, Element gNode) {
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
                }
            }
        }
        return gNode;
    }

    public Element setBBIESCType2(GenerationContext generationContext,
                                  BasicBusinessInformationEntitySupplementaryComponent bbieSc, Element gNode) {
        BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
        XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
        if (xbt.getBuiltInType() != null) {
            gNode.setAttribute("type", xbt.getBuiltInType());
        }
        return gNode;

    }

    public Element generateSCs(BasicBusinessInformationEntity bbie, Element gBBIENode,
                               List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList,
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
        for (int i = 0; i < bbieScList.size(); i++) {
            BasicBusinessInformationEntitySupplementaryComponent bbieSc = bbieScList.get(i);
            if (bbieSc.getCardinalityMax() == 0)
                continue;
            Element aNode = newElement("attribute");
            aNode = handleBBIESCvalue(generationContext, bbieSc, aNode); //Generate a DOM Element Node, handle values

            //Get a code list object
            CodeList codeList = generationContext.getCodeList(bbieSc);
            if (codeList != null) {
                aNode.setAttribute("id", Utility.generateGUID());
            }

            AgencyIdList agencyIdList = new AgencyIdList();

            if (codeList == null) { //aCL = null?
                agencyIdList = generationContext.getAgencyIdList(bbieSc);

                if (agencyIdList != null) {
                    aNode.setAttribute("id", agencyIdList.getGuid());
                }

                if (agencyIdList == null) { //aAL = null?
                    long primRestriction = bbieSc.getDtScPriRestriId();
                    if (primRestriction == 0L)
                        aNode = setBBIESCType(generationContext, bbieSc, aNode);
                    else
                        aNode = setBBIESCType2(generationContext, bbieSc, aNode);
                } else { //aAL = null?
                    if (!generationContext.isAgencyListGenerated(agencyIdList)) { //isAgencyListGenerated(aAL)?
                        generateAgencyList(agencyIdList, bbieSc, gSchemaNode, generationContext);
                    }

                    String agencyListTypeName = getAgencyListTypeName(agencyIdList, generationContext);
                    if (!StringUtils.isEmpty(agencyListTypeName)) {
                        aNode.setAttribute("type", agencyListTypeName);
                    }
                }
            } else { //aCL = null?
                if (!generationContext.isCodeListGenerated(codeList)) {
                    DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
                    generateCodeList(codeList, dtSc, gSchemaNode, generationContext);
                }
                if (getCodeListTypeName(codeList) != null) {
                    aNode.setAttribute("type", getCodeListTypeName(codeList));
                }
            }
//			if(isCCStored(aNode.getAttribute("id")))
//				continue;
//			storedCC.add(aNode.getAttribute("id"));
            tNode.addContent(aNode);
        }
        return tNode;
    }

    public String getAgencyListTypeName(AgencyIdList agencyIdList, GenerationContext generationContext) {
        AgencyIdListValue agencyIdListValue =
                generationContext.findAgencyIdListValue(agencyIdList.getAgencyIdListValueId());

        StringBuilder sb = new StringBuilder();

        sb.append(AGENCY_ID_LIST_NAME_PREFIX);
        sb.append(agencyIdListValue.getValue());
        sb.append(agencyIdList.getVersionId()).append('_');
        String name = agencyIdList.getName();
        if (!StringUtils.isEmpty(name)) {
            sb.append(Utility.toCamelCase(name)).append("ContentType").append('_');
        }
        sb.append(agencyIdList.getListId());

        return sb.toString();
    }

    public Element generateAgencyList(AgencyIdList gAL, BasicBusinessInformationEntitySupplementaryComponent gSC,
                                      Element gSchemaNode, GenerationContext generationContext) {
        Element stNode = newElement("simpleType");

        stNode.setAttribute("name", getAgencyListTypeName(gAL, generationContext));
        stNode.setAttribute("id", gAL.getGuid());

        Element rtNode = newElement("restriction");

        rtNode.setAttribute("base", "xsd:token");
        stNode.addContent(rtNode);

        List<AgencyIdListValue> gALVs = generationContext.findAgencyIdListValueByOwnerListId(gAL.getAgencyIdListId());

        for (int i = 0; i < gALVs.size(); i++) {
            AgencyIdListValue aAgencyIdListValue = gALVs.get(i);
            Element enumeration = newElement("enumeration");
            rtNode.addContent(enumeration);
            enumeration.setAttribute("value", aAgencyIdListValue.getValue());

            Element annotation = newElement("annotation");
            Element documentation = newElement("documentation");
            documentation.setAttribute("source", OAGI_NS);

            Element cctsName = new Element("ccts_Name", OAGI_NS);
            String name = aAgencyIdListValue.getName();
            cctsName.setText(name);
            documentation.addContent(cctsName);
            Element cctsDefinition = new Element("ccts_Definition", OAGI_NS);
            String definition = aAgencyIdListValue.getDefinition();
            cctsDefinition.setText(definition);
            documentation.addContent(cctsDefinition);

            annotation.addContent(documentation);
            enumeration.addContent(annotation);
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
            findAgencyIdListValueMap = agencyIdListValues.stream()
                    .collect(Collectors.toMap(e -> e.getAgencyIdListValueId(), Function.identity()));
            findAgencyIdListValueByOwnerListIdMap = agencyIdListValues.stream()
                    .collect(Collectors.groupingBy(e -> e.getOwnerListId()));

            List<AggregateBusinessInformationEntity> abieList = abieRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            findAbieMap = abieList.stream()
                    .collect(Collectors.toMap(e -> e.getAbieId(), Function.identity()));

            List<BasicBusinessInformationEntity> bbieList =
                    bbieRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
            findBbieByFromAbieIdAndUsedIsTrueMap = bbieList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList =
                    bbieScRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
            findBbieScByBbieIdAndUsedIsTrueMap = bbieScList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getBbieId()));

            List<AssociationBusinessInformationEntity> asbieList =
                    asbieRepository.findByOwnerTopLevelAbieIdAndUsedIsTrue(topLevelAbie.getTopLevelAbieId());
            findAsbieByFromAbieIdAndUsedIsTrueMap = asbieList.stream()
                    .filter(e -> e.isUsed())
                    .collect(Collectors.groupingBy(e -> e.getFromAbieId()));

            List<AssociationBusinessInformationEntityProperty> asbiepList =
                    asbiepRepository.findByOwnerTopLevelAbieId(topLevelAbie.getTopLevelAbieId());
            findASBIEPMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getAsbiepId(), Function.identity()));
            findAsbiepByRoleOfAbieIdMap = asbiepList.stream()
                    .collect(Collectors.toMap(e -> e.getRoleOfAbieId(), Function.identity()));
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

        private Map<Long, AgencyIdListValue> findAgencyIdListValueMap;
        private Map<Long, List<AgencyIdListValue>> findAgencyIdListValueByOwnerListIdMap;

        public AgencyIdListValue findAgencyIdListValue(long agencyIdListValueId) {
            return findAgencyIdListValueMap.get(agencyIdListValueId);
        }

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

        public boolean isCodeListGenerated(CodeList codeList) {
            for (int i = 0; i < guidArrayList.size(); i++) {
                if (codeList.getGuid().equals(guidArrayList.get(i)))
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
            long basedAccId = gABIE.getBasedAccId();
            return findACC(basedAccId);
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
        public List<BasicBusinessInformationEntitySupplementaryComponent> queryBBIESCs(BasicBusinessInformationEntity bbie) {
            long bbieId = bbie.getBbieId();
            return findBbieScByBbieIdAndUsedIsTrue(bbieId);
        }

        public AssociationBusinessInformationEntityProperty receiveASBIEP(long abieId) {
            return findAsbiepByRoleOfAbieId(abieId);
        }

        public DataType queryBDT(BasicBusinessInformationEntity bbie) {
            BasicCoreComponent bcc = findBCC(bbie.getBasedBccId());
            BasicCoreComponentProperty bccp = findBCCP(bcc.getToBccpId());
            return queryBDT(bccp);
        }

        public DataType queryBDT(BasicCoreComponentProperty bccp) {
            DataType bdt = findDT(bccp.getBdtId());
            return bdt;
        }

        public AssociationCoreComponentProperty queryBasedASCCP(AssociationBusinessInformationEntityProperty asbiep) {
            AssociationCoreComponentProperty asccp = findASCCP(asbiep.getBasedAsccpId());
            return asccp;
        }

        public AssociationCoreComponent queryBasedASCC(AssociationBusinessInformationEntity asbie) {
            AssociationCoreComponent ascc = findASCC(asbie.getBasedAsccId());
            return ascc;
        }

        public AggregateBusinessInformationEntity queryTargetABIE(AssociationBusinessInformationEntityProperty asbiep) {
            AggregateBusinessInformationEntity abie = findAbie(asbiep.getRoleOfAbieId());
            return abie;
        }

        public AggregateCoreComponent queryTargetACC(AssociationBusinessInformationEntityProperty asbiep) {
            AggregateBusinessInformationEntity abie = findAbie(asbiep.getRoleOfAbieId());

            AggregateCoreComponent acc = findACC(abie.getBasedAccId());
            return acc;
        }

        public AggregateBusinessInformationEntity queryTargetABIE2(AssociationBusinessInformationEntityProperty asbiep) {
            AggregateBusinessInformationEntity abie = findAbie(asbiep.getRoleOfAbieId());
            return abie;
        }

        public BasicCoreComponent queryBasedBCC(BasicBusinessInformationEntity bbie) {
            BasicCoreComponent bcc = findBCC(bbie.getBasedBccId());
            return bcc;
        }

        public BasicCoreComponentProperty queryToBCCP(BasicCoreComponent bcc) {
            return findBCCP(bcc.getToBccpId());
        }

        public CodeList getCodeList(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
            CodeList codeList = findCodeList(bbieSc.getCodeListId());
            if (codeList != null) {
                return codeList;
            }

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    findBdtScPriRestri(bbieSc.getDtScPriRestriId());
            if (bdtScPriRestri != null) {
                return findCodeList(bdtScPriRestri.getCodeListId());
            } else {
                DataTypeSupplementaryComponent gDTSC = findDtSc(bbieSc.getDtScId());
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bBDTSCPrimitiveRestriction =
                        findBdtScPriRestriByBdtScIdAndDefaultIsTrue(gDTSC.getDtScId());
                if (bBDTSCPrimitiveRestriction != null) {
                    codeList = findCodeList(bBDTSCPrimitiveRestriction.getCodeListId());
                }
            }

            return codeList;
        }

        public AgencyIdList getAgencyIdList(BasicBusinessInformationEntity bbie) {
            AgencyIdList agencyIdList = findAgencyIdList(bbie.getAgencyIdListId());
            if (agencyIdList != null) {
                return agencyIdList;
            }

            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    findBdtPriRestri(bbie.getBdtPriRestriId());
            if (bdtPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListId());
            }

            if (agencyIdList == null) {
                DataType bdt = queryAssocBDT(bbie);
                bdtPriRestri = findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
                if (bdtPriRestri != null) {
                    agencyIdList = findAgencyIdList(bdtPriRestri.getAgencyIdListId());
                }
            }
            return agencyIdList;
        }

        public AgencyIdList getAgencyIdList(BasicBusinessInformationEntitySupplementaryComponent bbieSc) {
            AgencyIdList agencyIdList = findAgencyIdList(bbieSc.getAgencyIdListId());
            if (agencyIdList != null) {
                return agencyIdList;
            }

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    findBdtScPriRestri(bbieSc.getDtScPriRestriId());
            if (bdtScPriRestri != null) {
                agencyIdList = findAgencyIdList(bdtScPriRestri.getAgencyIdListId());
            }

            if (agencyIdList == null) {
                DataTypeSupplementaryComponent gDTSC = findDtSc(bbieSc.getDtScId());
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

        public AssociationBusinessInformationEntityProperty queryAssocToASBIEP(AssociationBusinessInformationEntity asbie) {
            AssociationBusinessInformationEntityProperty asbiepVO = findASBIEP(asbie.getToAsbiepId());
            return asbiepVO;
        }

        public DataType queryAssocBDT(BasicBusinessInformationEntity bbie) {
            BasicCoreComponent bcc = findBCC(bbie.getBasedBccId());
            BasicCoreComponentProperty bccp = findBCCP(bcc.getToBccpId());
            return queryBDT(bccp);
        }
    }

    public File generateSchema(List<Long> topLevelAbieIds, ProfileBODGenerationOption option) throws Exception {
        if (topLevelAbieIds == null || topLevelAbieIds.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (option == null) {
            throw new IllegalArgumentException();
        }

        switch (option.getSchemaPackage()) {
            case All:
                return generateSchemaForAll(topLevelAbieIds, option);

            case Each:
                return generateSchemaForEach(topLevelAbieIds, option);

            default:
                throw new IllegalStateException();
        }
    }

    private File generateSchemaForAll(List<Long> topLevelAbieIds, ProfileBODGenerationOption option) throws Exception {
        SchemaExpressionGenerator schemaExpressionGenerator = createSchemaExpressionGenerator(option);

        for (long topLevelAbieId : topLevelAbieIds) {
            TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
            schemaExpressionGenerator.generate(topLevelAbie, option);
        }

        File schemaExpressionFile = schemaExpressionGenerator.asFile(Utility.generateGUID() + "_standalone");
        return schemaExpressionFile;
    }

    private File generateSchemaForEach(List<Long> topLevelAbieIds, ProfileBODGenerationOption option) throws Exception {
        List<File> targetFiles = new ArrayList();
        for (long topLevelAbieId : topLevelAbieIds) {
            SchemaExpressionGenerator schemaExpressionGenerator = createSchemaExpressionGenerator(option);

            TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
            schemaExpressionGenerator.generate(topLevelAbie, option);

            File schemaExpressionFile = schemaExpressionGenerator.asFile(topLevelAbie.getAbie().getGuid());
            targetFiles.add(schemaExpressionFile);
        }

        return Zip.compression(targetFiles, Utility.generateGUID());
    }

    private SchemaExpressionGenerator createSchemaExpressionGenerator(ProfileBODGenerationOption option) {
        switch (option.getSchemaExpression()) {
            case XML:
                return new XMLSchemaExpressionGenerator();
            case JSON:
                return new JSONSchemaExpressionGenerator();
            default:
                throw new UnsupportedOperationException();
        }
    }

    private interface SchemaExpressionGenerator {

        void generate(TopLevelAbie topLevelAbie, ProfileBODGenerationOption option) throws JsonProcessingException;

        File asFile(String filename) throws IOException;

    }

    private class XMLSchemaExpressionGenerator implements SchemaExpressionGenerator {

        private Document document;
        private Element schemaNode;

        public XMLSchemaExpressionGenerator() {
            this.document = new Document();
            this.schemaNode = generateSchema(document);
        }

        @Override
        public void generate(TopLevelAbie topLevelAbie, ProfileBODGenerationOption option) {
            GenerationContext generationContext = new GenerationContext(topLevelAbie);
            AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();
            AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie.getAbieId());
            logger.debug("Generating Top Level ABIE w/ given AssociationBusinessInformationEntityProperty Id: " + asbiep.getAsbiepId());
            this.document = generateTopLevelABIE(asbiep, document, schemaNode, generationContext);
        }

        @Override
        public File asFile(String filename) throws IOException {
            File tempFile = File.createTempFile(Utility.generateGUID(), null);
            tempFile = new File(tempFile.getParentFile(), filename + ".xml");

            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                outputter.output(this.document, outputStream);
                outputStream.flush();
            }

            logger.info("XML Schema is generated: " + tempFile);

            return tempFile;
        }
    }

    private class JSONSchemaExpressionGenerator implements SchemaExpressionGenerator {

        // In schema version draft-04, it used "id" for dereferencing.
        // However, in draft-06, it changes to "$id".
        private static final String ID_KEYWORD = "id";

        private ObjectMapper mapper;
        private Map<String, Object> root;

        public JSONSchemaExpressionGenerator() {
            mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        @Override
        public void generate(TopLevelAbie topLevelAbie, ProfileBODGenerationOption option) {
            GenerationContext generationContext = new GenerationContext(topLevelAbie);
            AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();

            AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie.getAbieId());
            AggregateBusinessInformationEntity typeAbie = generationContext.queryTargetABIE(asbiep);

            Map<String, Object> definitions;
            if (root == null) {
                root = new LinkedHashMap();
                root.put("$schema", "http://json-schema.org/draft-04/schema#");
                root.put(ID_KEYWORD, "http://www.openapplications.org/oagis/10/");

                root.put("required", new ArrayList());
                root.put("additionalProperties", false);

                Map<String, Object> properties = new LinkedHashMap();
                root.put("properties", properties);
                definitions = new LinkedHashMap();
                root.put("definitions", definitions);
            } else {
                definitions = (Map<String, Object>) root.get("definitions");
            }

            fillProperties(root, definitions, asbiep, typeAbie, generationContext, option);
        }

        private String camelCase(String... terms) {
            String term = Arrays.stream(terms).filter(e -> !StringUtils.isEmpty(e))
                    .collect(Collectors.joining());
            if (StringUtils.isEmpty(term)) {
                throw new IllegalArgumentException();
            }
            String s = term.replaceAll(" ", "");

            if (s.length() > 3 &&
                    Character.isUpperCase(s.charAt(0)) &&
                    Character.isUpperCase(s.charAt(1)) &&
                    Character.isUpperCase(s.charAt(2))) {
                return s;
            }

            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }

        private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                    AssociationBusinessInformationEntity asbie,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {
            AssociationBusinessInformationEntityProperty asbiep = generationContext.queryAssocToASBIEP(asbie);
            AggregateBusinessInformationEntity typeAbie = generationContext.queryTargetABIE2(asbiep);

            AssociationCoreComponent ascc = generationContext.queryBasedASCC(asbie);
            boolean isArray = (ascc.getCardinalityMax() != 1);
            int minVal = asbie.getCardinalityMin();
            int maxVal = asbie.getCardinalityMax();
            boolean isNillable = asbie.isNillable();

            AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
            String name = camelCase(asccp.getPropertyTerm());
            if (minVal > 0) {
                List<String> parentRequired = (List<String>) parent.get("required");
                if (parentRequired == null) {
                    throw new IllegalStateException();
                }
                parentRequired.add(name);
            }

            Map<String, Object> properties = new LinkedHashMap();
            if (!parent.containsKey("properties")) {
                parent.put("properties", new LinkedHashMap<String, Object>());
            }
            ((Map<String, Object>) parent.get("properties")).put(name, properties);

            String definition = asbie.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                properties.put("description", definition);
            }

            if (isNillable) {
                properties.put("type", Arrays.asList(isArray ? "array" : "object", "null"));
            } else {
                properties.put("type", isArray ? "array" : "object");
            }

            if (isArray) {
                if (minVal > 0) {
                    properties.put("minItems", minVal);
                }
                if (maxVal > 0) {
                    properties.put("maxItems", maxVal);
                }
            }

            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);

            fillProperties(properties, definitions, typeAbie, generationContext, option);

            if (((List) properties.get("required")).isEmpty()) {
                properties.remove("required");
            }
        }

        private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                    AssociationBusinessInformationEntityProperty asbiep,
                                    AggregateBusinessInformationEntity abie,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {

            AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
            String name = camelCase(asccp.getPropertyTerm());

            List<String> parentRequired = (List<String>) parent.get("required");
            parentRequired.add(name);

            Map<String, Object> properties = new LinkedHashMap();
            if (!parent.containsKey("properties")) {
                parent.put("properties", new LinkedHashMap<String, Object>());
            }
            ((Map<String, Object>) parent.get("properties")).put(name, properties);

            properties.put("type", "object");
            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);

            fillProperties(properties, definitions, abie, generationContext, option);

            if (((List) properties.get("required")).isEmpty()) {
                properties.remove("required");
            }
        }

        private Map<String, Object> toProperties(XSDBuiltInType xbt) {
            String jbtDraft05Map = xbt.getJbtDraft05Map();
            try {
                return mapper.readValue(jbtDraft05Map, LinkedHashMap.class);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private String fillDefinitions(Map<String, Object> definitions,
                                       XSDBuiltInType xbt,
                                       GenerationContext generationContext) {
            String builtInType = xbt.getBuiltInType();
            if (builtInType.startsWith("xsd:")) {
                builtInType = builtInType.substring(4);
            }
            if (!definitions.containsKey(builtInType)) {
                Map<String, Object> content = toProperties(xbt);
                definitions.put(builtInType, content);
            }

            return "#/definitions/" + builtInType;
        }

        private String fillDefinitions(Map<String, Object> definitions,
                                       BasicBusinessInformationEntity bbie,
                                       CodeList codeList,
                                       GenerationContext generationContext) {
            DataType bdt = generationContext.queryAssocBDT(bbie);
            BusinessDataTypePrimitiveRestriction bdtPriRestri =
                    generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());

            Map<String, Object> properties;
            if (bdtPriRestri.getCodeListId() != 0) {
                properties = new LinkedHashMap();
                properties.put("type", "string");
            } else {
                CoreDataTypeAllowedPrimitiveExpressionTypeMap cdtAwdPriXpsTypeMap =
                        generationContext.findCdtAwdPriXpsTypeMap(bdtPriRestri.getCdtAwdPriXpsTypeMapId());
                XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtAwdPriXpsTypeMap.getXbtId());
                properties = toProperties(xbt);
            }

            return fillDefinitions(properties, definitions, codeList, generationContext);
        }

        private String fillDefinitions(Map<String, Object> definitions,
                                       BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                                       CodeList codeList,
                                       GenerationContext generationContext) {
            DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                    generationContext.findBdtScPriRestriByBdtScIdAndDefaultIsTrue(dtSc.getDtScId());

            Map<String, Object> properties;
            if (bdtScPriRestri.getCodeListId() != 0) {
                properties = new LinkedHashMap();
                properties.put("type", "string");
            } else {
                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                        generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
                properties = toProperties(xbt);
            }

            return fillDefinitions(properties, definitions, codeList, generationContext);
        }

        private String fillDefinitions(Map<String, Object> properties,
                                       Map<String, Object> definitions,
                                       CodeList codeList,
                                       GenerationContext generationContext) {
            String codeListName = camelCase(getCodeListTypeName(codeList));
            if (!definitions.containsKey(codeListName)) {
                List<CodeListValue> codeListValues = generationContext.getCodeListValues(codeList);
                List<String> enumerations = codeListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
                properties.put("enum", enumerations);

                definitions.put(codeListName, properties);
            }

            return "#/definitions/" + codeListName;
        }

        private String fillDefinitions(Map<String, Object> definitions,
                                       AgencyIdList agencyIdList,
                                       GenerationContext generationContext) {
            String agencyListTypeName = camelCase(getAgencyListTypeName(agencyIdList, generationContext));
            if (!definitions.containsKey(agencyListTypeName)) {
                Map<String, Object> properties = new LinkedHashMap();
                properties.put("type", "string");

                List<AgencyIdListValue> agencyIdListValues =
                        generationContext.findAgencyIdListValueByOwnerListId(agencyIdList.getAgencyIdListId());
                List<String> enumerations = agencyIdListValues.stream().map(e -> e.getValue()).collect(Collectors.toList());
                properties.put("enum", enumerations);

                definitions.put(agencyListTypeName, properties);
            }

            return "#/definitions/" + agencyListTypeName;
        }

        private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                    AggregateBusinessInformationEntity abie,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {

            List<BusinessInformationEntity> children = generationContext.queryChildBIEs(abie);
            for (BusinessInformationEntity bie : children) {
                if (bie instanceof BasicBusinessInformationEntity) {
                    BasicBusinessInformationEntity bbie = (BasicBusinessInformationEntity) bie;
                    fillProperties(parent, definitions, bbie, generationContext, option);
                } else {
                    AssociationBusinessInformationEntity asbie = (AssociationBusinessInformationEntity) bie;
                    if (isAnyProperty(asbie, generationContext)) {
                        parent.put("additionalProperties", true);
                    } else {
                        fillProperties(parent, definitions, asbie, generationContext, option);
                    }
                }
            }
        }

        private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                    BasicBusinessInformationEntity bbie,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {
            BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
            BasicCoreComponentProperty bccp = generationContext.queryToBCCP(bcc);
            DataType bdt = generationContext.queryBDT(bccp);

            boolean isArray = (bcc.getCardinalityMax() != 1);
            int minVal = bbie.getCardinalityMin();
            int maxVal = bbie.getCardinalityMax();
            boolean isNillable = bbie.isNillable();

            String name = camelCase(bccp.getPropertyTerm());

            Map<String, Object> properties = new LinkedHashMap();
            if (!parent.containsKey("properties")) {
                parent.put("properties", new LinkedHashMap<String, Object>());
            }
            ((Map<String, Object>) parent.get("properties")).put(name, properties);

            if (minVal > 0) {
                List<String> parentRequired = (List<String>) parent.get("required");
                if (parentRequired == null) {
                    throw new IllegalStateException();
                }
                parentRequired.add(name);
            }

            String definition = bbie.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                properties.put("description", definition);
            }

            if (isNillable) {
                properties.put("type", Arrays.asList(isArray ? "array" : "object", "null"));
            } else {
                properties.put("type", isArray ? "array" : "object");
            }

            if (isArray) {
                if (minVal > 0) {
                    properties.put("minItems", minVal);
                }
                if (maxVal > 0) {
                    properties.put("maxItems", maxVal);
                }
            }

            properties.put("required", new ArrayList());
            properties.put("additionalProperties", false);
            properties.put("properties", new LinkedHashMap<String, Object>());

            CodeList codeList = getCodeList(generationContext, bbie, bdt);
            String ref;
            if (codeList != null) {
                ref = fillDefinitions(definitions, bbie, codeList, generationContext);
            } else {
                AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbie);
                if (agencyIdList != null) {
                    ref = fillDefinitions(definitions, agencyIdList, generationContext);
                } else {
                    BusinessDataTypePrimitiveRestriction bdtPriRestri =
                            generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
                    XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);

                    ref = fillDefinitions(definitions, xbt, generationContext);
                }
            }

            ((List<String>) properties.get("required")).add("content");
            ((Map<String, Object>) properties.get("properties"))
                    .put("content", ImmutableMap.<String, Object>builder()
                            .put("$ref", ref)
                            .build());

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
            if (!bbieScList.isEmpty()) {
                for (BasicBusinessInformationEntitySupplementaryComponent bbieSc : bbieScList) {
                    fillProperties(properties, definitions, bbieSc, generationContext, option);
                }
            }
        }

        private void fillProperties(Map<String, Object> parent, Map<String, Object> definitions,
                                    BasicBusinessInformationEntitySupplementaryComponent bbieSc,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {
            int minVal = bbieSc.getCardinalityMin();
            int maxVal = bbieSc.getCardinalityMax();
            if (maxVal == 0) {
                return;
            }

            DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(bbieSc.getDtScId());
            String name = camelCase(dtSc.getPropertyTerm(), dtSc.getRepresentationTerm());
            Map<String, Object> properties = new LinkedHashMap();
            ((Map<String, Object>) parent.get("properties")).put(name, properties);

            if (minVal > 0) {
                ((List<String>) parent.get("required")).add(name);
            }

            String definition = bbieSc.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                parent.put("description", definition);
            }

            CodeList codeList = generationContext.getCodeList(bbieSc);
            String ref;
            if (codeList != null) {
                ref = fillDefinitions(definitions, bbieSc, codeList, generationContext);
            } else {
                AgencyIdList agencyIdList = generationContext.getAgencyIdList(bbieSc);
                if (agencyIdList != null) {
                    ref = fillDefinitions(definitions, agencyIdList, generationContext);
                } else {
                    BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtScPriRestri =
                            generationContext.findBdtScPriRestri(bbieSc.getDtScPriRestriId());
                    CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap cdtScAwdPriXpsTypeMap =
                            generationContext.findCdtScAwdPriXpsTypeMap(bdtScPriRestri.getCdtScAwdPriXpsTypeMapId());
                    XSDBuiltInType xbt = generationContext.findXSDBuiltInType(cdtScAwdPriXpsTypeMap.getXbtId());
                    ref = fillDefinitions(definitions, xbt, generationContext);
                }
            }

            properties.put("$ref", ref);
        }

        private void ensureRoot() {
            if (root == null) {
                throw new IllegalStateException();
            }

            // The "required" property for the root element of schema should has only one child.
            if (((List<String>) root.get("required")).size() > 1) {
                root.remove("required");
            }

            //
            Map<String, Object> properties = (Map<String, Object>) root.get("properties");
            for (String key : properties.keySet()) {
                Map<String, Object> copied = new LinkedHashMap();
                copied.put(ID_KEYWORD, "#" + key);
                copied.putAll(((Map<String, Object>) properties.get(key)));
                properties.put(key, copied);
            }
        }

        @Override
        public File asFile(String filename) throws IOException {
            ensureRoot();

            File tempFile = File.createTempFile(Utility.generateGUID(), null);
            tempFile = new File(tempFile.getParentFile(), filename + ".json");

            mapper.writeValue(tempFile, root);
            logger.info("JSON Schema is generated: " + tempFile);

            return tempFile;
        }
    }
}
