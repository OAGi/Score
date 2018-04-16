package org.oagi.srt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.oagi.srt.ServiceApplication;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.Zip;
import org.oagi.srt.model.bod.ProfileBODGenerationOption;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
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

    public File writeXSDFile(Document doc, String filename) throws IOException {
        File file = File.createTempFile("oagis-", null);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputter.output(doc, outputStream);
            outputStream.flush();
        }

        File renamedFile = new File(file.getParentFile(), filename + ".xsd");
        if (file.renameTo(renamedFile)) {
            FileUtils.deleteQuietly(file);
            file = renamedFile;
        }

        logger.info(file + " is generated");
        return file;
    }

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
                DataType aBDT = generationContext.queryAssocBDT(childBIE);
                generateBBIE(childBIE, aBDT, gPNode, gSchemaNode, generationContext);
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

    private boolean isAnyProperty(AssociationBusinessInformationEntity gASBIE,
                                  GenerationContext generationContext) {
        AssociationBusinessInformationEntityProperty gASBIEP = generationContext.queryAssocToASBIEP(gASBIE);
        AssociationCoreComponentProperty asccp = generationContext.findASCCP(gASBIEP.getBasedAsccpId());
        if (!"AnyProperty".equals(Utility.first(asccp.getDen(), true))) {
            return false;
        }

        AggregateBusinessInformationEntity gABIE = generationContext.queryTargetABIE2(gASBIEP);
        AggregateCoreComponent gACC = generationContext.queryBasedACC(gABIE);
        return OagisComponentType.Embedded == gACC.getOagisComponentType();
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
        DataTypeSupplementaryComponent dtSc = generationContext.findDtSc(aBBIESC.getDtScId());
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

        aNode.setAttribute("id", aBBIESC.getGuid());

        Element annotation = newElement("annotation");
        Element documentation = newElement("documentation");
        documentation.setAttribute("source", OAGI_NS + "/platform/2");
        //documentation.setTextContent(aBBIESC.getDefinition());
        annotation.addContent(documentation);
        aNode.addContent(annotation);

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
                        gNode.setAttribute("type", xbt.getBuiltInType());
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
                    long primRestriction = aBBIESC.getDtScPriRestriId();
                    if (primRestriction == 0L)
                        aNode = setBBIESCType(generationContext, aBBIESC, aNode);
                    else
                        aNode = setBBIESCType2(generationContext, aBBIESC, aNode);
                } else { //aAL = null?
                    if (!generationContext.isAgencyListGenerated(aAL)) { //isAgencyListGenerated(aAL)?
                        generateAgencyList(aAL, aBBIESC, gSchemaNode, generationContext);
                    }

                    String agencyListTypeName = getAgencyListTypeName(aAL, generationContext);
                    if (!StringUtils.isEmpty(agencyListTypeName)) {
                        aNode.setAttribute("type", agencyListTypeName);
                    }
                }
            } else { //aCL = null?
                if (!generationContext.isCodeListGenerated(aCL)) {
                    DataTypeSupplementaryComponent aDTSC = generationContext.findDtSc(aBBIESC.getDtScId());
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

        public AssociationCoreComponentProperty queryBasedASCCP(AssociationBusinessInformationEntityProperty gASBIEP) {
            AssociationCoreComponentProperty asccpVO = findASCCP(gASBIEP.getBasedAsccpId());
            return asccpVO;
        }

        public AssociationCoreComponent queryBasedASCC(AssociationBusinessInformationEntity gASBIE) {
            AssociationCoreComponent gASCC = findASCC(gASBIE.getBasedAsccId());
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

        public BasicCoreComponentProperty queryToBCCP(BasicCoreComponent bcc) {
            return findBCCP(bcc.getToBccpId());
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

    public File generateXMLSchema(List<Long> topLevelAbieIds, ProfileBODGenerationOption option) throws Exception {
        if (topLevelAbieIds == null || topLevelAbieIds.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (option == null) {
            throw new IllegalArgumentException();
        }

        switch (option.getSchemaPackage()) {
            case All:
                return generateXMLSchemaForAll(topLevelAbieIds, option);

            case Each:
                return generateXMLSchemaForEach(topLevelAbieIds, option);

            default:
                throw new IllegalStateException();
        }
    }

    private File generateXMLSchemaForAll(List<Long> topLevelAbieIds, ProfileBODGenerationOption option) throws Exception {
        SchemaExpressionGenerator schemaExpressionGenerator = createSchemaExpressionGenerator(option);

        for (long topLevelAbieId : topLevelAbieIds) {
            TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
            schemaExpressionGenerator.generate(topLevelAbie, option);
        }

        File schemaExpressionFile = File.createTempFile(Utility.generateGUID() + "_standalone", null);
        schemaExpressionGenerator.writeTo(schemaExpressionFile);
        return schemaExpressionFile;
    }

    private File generateXMLSchemaForEach(List<Long> topLevelAbieIds, ProfileBODGenerationOption option) throws Exception {
        List<File> targetFiles = new ArrayList();
        for (long topLevelAbieId : topLevelAbieIds) {
            SchemaExpressionGenerator schemaExpressionGenerator = createSchemaExpressionGenerator(option);

            TopLevelAbie topLevelAbie = topLevelAbieRepository.findOne(topLevelAbieId);
            schemaExpressionGenerator.generate(topLevelAbie, option);

            File schemaExpressionFile = File.createTempFile(topLevelAbie.getAbie().getGuid(), null);
            schemaExpressionGenerator.writeTo(schemaExpressionFile);
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

        void writeTo(File file) throws IOException;

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
        public void writeTo(File file) throws IOException {
            File tempFile = File.createTempFile("oagis-", null);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat().setIndent("\t"));
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                outputter.output(this.document, outputStream);
                outputStream.flush();
            }

            if (!tempFile.renameTo(file)) {
                throw new IOException();
            }

            logger.info(file + " is generated");
        }
    }

    private class JSONSchemaExpressionGenerator implements SchemaExpressionGenerator {

        private ObjectMapper mapper;

        public JSONSchemaExpressionGenerator() {
            mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }

        @Override
        public void generate(TopLevelAbie topLevelAbie, ProfileBODGenerationOption option) {
            Map<String, Object> root = new LinkedHashMap();
            root.put("$schema", "http://json-schema.org/draft-06/schema#");
            root.put("$id", "http://www.openapplications.org/oagis/10/");

            GenerationContext generationContext = new GenerationContext(topLevelAbie);
            AggregateBusinessInformationEntity abie = topLevelAbie.getAbie();

            AssociationBusinessInformationEntityProperty asbiep = generationContext.receiveASBIEP(abie.getAbieId());
            AggregateBusinessInformationEntity typeAbie = generationContext.queryTargetABIE(asbiep);

            AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
            root.put("required", Arrays.asList(camelCase(asccp.getPropertyTerm())));
            root.put("additionalProperties", false);

            Map<String, Object> properties = new LinkedHashMap();
            root.put("properties", properties);

            fillProperties(properties, asbiep, typeAbie, generationContext, option);

            try {
                String str = mapper.writeValueAsString(root);
                System.out.println(str);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }

        private String camelCase(String... terms) {
            String term = Arrays.stream(terms).filter(e -> !StringUtils.isEmpty(e))
                    .collect(Collectors.joining());
            if (StringUtils.isEmpty(term)) {
                throw new IllegalArgumentException();
            }
            String s = term.replaceAll(" ", "");
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }

        private void fillProperties(Map<String, Object> parent,
                                    AssociationBusinessInformationEntityProperty asbiep,
                                    AggregateBusinessInformationEntity abie,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {
            AssociationCoreComponentProperty asccp = generationContext.queryBasedASCCP(asbiep);
            String name = camelCase(asccp.getPropertyTerm());

            Map<String, Object> properties = new LinkedHashMap();
            parent.put(name, properties);

            properties.put("type", "object");
            properties.put("additionalProperties", false);

            fillProperties(properties, abie, generationContext, option);
        }

        private void fillProperties(Map<String, Object> parent,
                                    AggregateBusinessInformationEntity abie,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {
            AggregateCoreComponent acc = generationContext.queryBasedACC(abie);

            Map<String, Object> properties = new LinkedHashMap();
            parent.put("properties", properties);

            List<BusinessInformationEntity> childBIEs = generationContext.queryChildBIEs(abie);
            for (BusinessInformationEntity bie : childBIEs) {

                if (bie instanceof BasicBusinessInformationEntity) {
                    BasicBusinessInformationEntity bbie = (BasicBusinessInformationEntity) bie;
                    fillProperties(properties, bbie, generationContext, option);
                } else {
                    AssociationBusinessInformationEntity asbie = (AssociationBusinessInformationEntity) bie;

                    if (isAnyProperty(asbie, generationContext)) {
                        // generateAnyABIE(childBIE, gPNode, generationContext);
                    } else {
                        AssociationBusinessInformationEntityProperty asbiep = generationContext.queryAssocToASBIEP(asbie);
                        AggregateBusinessInformationEntity typeAbie = generationContext.queryTargetABIE2(asbiep);

                        fillProperties(properties, asbiep, typeAbie, generationContext, option);
//                        Element node = generateASBIE(childBIE, gPNode, generationContext);
//                        AssociationBusinessInformationEntityProperty anASBIEP = generationContext.queryAssocToASBIEP(childBIE);
//                        node = generateASBIEP(generationContext, anASBIEP, node);
//                        AggregateBusinessInformationEntity anABIE = generationContext.queryTargetABIE2(anASBIEP);
//                        node = generateABIE(anABIE, node, gSchemaNode, generationContext);
//                        node = generateBIEs(anABIE, node, gSchemaNode, generationContext);
                    }
                }
            }
        }

        private void fillProperties(Map<String, Object> parent,
                                    BasicBusinessInformationEntity bbie,
                                    GenerationContext generationContext,
                                    ProfileBODGenerationOption option) {
            BasicCoreComponent bcc = generationContext.queryBasedBCC(bbie);
            BasicCoreComponentProperty bccp = generationContext.queryToBCCP(bcc);
            DataType bdt = generationContext.queryBDT(bccp);

            String name = camelCase(bccp.getPropertyTerm());
            Map<String, Object> properties = new LinkedHashMap();
            parent.put(name, properties);

            String definition = bbie.getDefinition();
            if (!StringUtils.isEmpty(definition)) {
                properties.put("definition", definition);
            }
            properties.put("type", "object");
            properties.put("additionalProperties", false);

            if (bcc.getEntityType() == BasicCoreComponentEntityType.Element) {

            } else {

            }

            List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
            CodeList codeList = getCodeList(generationContext, bbie, bdt);
            if (codeList == null) {
                BusinessDataTypePrimitiveRestriction bdtPriRestri =
                        generationContext.findBdtPriRestriByBdtIdAndDefaultIsTrue(bdt.getDtId());
                XSDBuiltInType xbt = getXbt(generationContext, bdtPriRestri);

            } else {

            }

//            Element eNode;
//            eNode = newElement("element");
//            eNode = handleBBIE_Elementvalue(bbie, eNode, generationContext);
//            if (bcc.getEntityType() == BasicCoreComponentEntityType.Element) {
//                while (!gPNode.getName().equals("sequence")) {
//                    gPNode = gPNode.getParentElement();
//                }
//
//                gPNode.addContent(eNode);
//
//                List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
//
//                CodeList aCL = getCodeList(generationContext, bbie, bdt);
//
//                if (aCL == null) {
//                    if (bbie.getBdtPriRestriId() == 0) {
//                        if (bbieScList.isEmpty()) {
//                            eNode = setBBIEType(generationContext, bdt, eNode);
//                            return eNode;
//                        } else {
//                            eNode = generateBDT(bbie, eNode, generationContext);
//                            eNode = generateSCs(bbie, eNode, bbieScList, gSchemaNode, generationContext);
//                            return eNode;
//                        }
//                    } else {
//                        if (bbieScList.isEmpty()) {
//                            eNode = setBBIEType(generationContext, bbie, eNode);
//                            return eNode;
//                        } else {
//                            eNode = generateBDT(bbie, eNode, generationContext);
//                            eNode = generateSCs(bbie, eNode, bbieScList, gSchemaNode, generationContext);
//                            return eNode;
//                        }
//                    }
//                } else { //is aCL null?
//                    if (!generationContext.isCodeListGenerated(aCL)) {
//                        generateCodeList(aCL, bdt, gSchemaNode, generationContext);
//                    }
//                    if (bbieScList.isEmpty()) {
//                        eNode.setAttribute("type", getCodeListTypeName(aCL));
//                        return eNode;
//                    } else {
//                        eNode = generateBDT(bbie, eNode, gSchemaNode, aCL, generationContext);
//                        eNode = generateSCs(bbie, eNode, bbieScList, gSchemaNode, generationContext);
//                        return eNode;
//                    }
//                }
//
//
//            } else {
//                List<BasicBusinessInformationEntitySupplementaryComponent> bbieScList = generationContext.queryBBIESCs(bbie);
//                CodeList aCL = getCodeList(generationContext, bbie, bdt);
//                if (aCL == null) {
//                    if (bbie.getBdtPriRestriId() == 0) {
//                        if (bbieScList.isEmpty()) {
//                            eNode = newElement("attribute");
//                            eNode = handleBBIE_Attributevalue(bbie, eNode, generationContext);
//
//                            while (!gPNode.getName().equals("complexType")) {
//                                gPNode = (Element) gPNode.getParentElement();
//                            }
//
//                            gPNode.addContent(eNode);
//                            eNode = setBBIE_Attr_Type(generationContext, bdt, eNode);
//                            return eNode;
//                        } else {
//                            //eNode = setBBIE_Attr_Type(gBBIE, eNode);
//                            eNode = generateBDT(bbie, eNode, generationContext);
//                            return eNode;
//                        }
//                    } else {
//                        if (bbieScList.isEmpty()) {
//                            eNode = newElement("attribute");
//                            eNode = handleBBIE_Attributevalue(bbie, eNode, generationContext);
//
//                            while (!gPNode.getName().equals("complexType")) {
//                                gPNode = (Element) gPNode.getParentElement();
//                            }
//
//                            gPNode.addContent(eNode);
//                            eNode = setBBIE_Attr_Type(generationContext, bbie, eNode);
//                            return eNode;
//                        } else {
//                            //eNode = setBBIE_Attr_Type(gBBIE, eNode);
//                            eNode = generateBDT(bbie, eNode, generationContext);
//                            return eNode;
//                        }
//                    }
//                } else { //is aCL null?
//                    eNode = newElement("attribute");
//                    eNode = handleBBIE_Attributevalue(bbie, eNode, generationContext);
//
//                    while (!gPNode.getName().equals("complexType")) {
//                        gPNode = (Element) gPNode.getParentElement();
//                    }
//
//                    gPNode.addContent(eNode);
//
//                    if (!generationContext.isCodeListGenerated(aCL)) {
//                        generateCodeList(aCL, bdt, gSchemaNode, generationContext);
//                    }
//                    if (bbieScList.isEmpty()) {
//                        if (getCodeListTypeName(aCL) != null) {
//                            eNode.setAttribute("type", getCodeListTypeName(aCL));
//                        }
//                        return eNode;
//                    } else {
//                        if (bbie.getBdtPriRestriId() == 0) {
//                            eNode = setBBIE_Attr_Type(generationContext, bdt, eNode);
//                            return eNode;
//                        } else {
//                            if (getCodeListTypeName(aCL) != null) {
//                                eNode.setAttribute("type", getCodeListTypeName(aCL));
//                            }
//                            return eNode;
//                        }
//                    }
//                }
//            }

        }

        @Override
        public void writeTo(File file) throws IOException {

        }
    }

    public static void main(String[] args) throws Throwable {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ServiceApplication.class, args)) {
            ProfileBODGenerateService profileBODGenerateService = ctx.getBean(ProfileBODGenerateService.class);

            ProfileBODGenerationOption option = new ProfileBODGenerationOption();
            option.setSchemaExpression(ProfileBODGenerationOption.SchemaExpression.JSON);
            profileBODGenerateService.generateXMLSchemaForAll(Arrays.asList(1L), option);
//            File generatedSchemaExpression = profileBODGenerateService.generateXMLSchemaForAll(Arrays.asList(1L), option);
//
//            for (String line : FileUtils.readLines(generatedSchemaExpression)) {
//                System.out.println(line);
//            }
        }
    }
}
