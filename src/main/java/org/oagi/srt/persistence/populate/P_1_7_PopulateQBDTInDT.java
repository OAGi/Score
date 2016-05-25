package org.oagi.srt.persistence.populate;

import org.oagi.srt.Application;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_7_PopulateQBDTInDT {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    private XPathHandler fields_xsd;
    private XPathHandler meta_xsd;
    private XPathHandler businessdatatype_xsd;
    private XPathHandler component_xsd;
    private File f1;

    private BasicCoreComponentPropertyRepository bccpDAO;
    private BusinessDataTypePrimitiveRestrictionRepository aBDTPrimitiveRestrictionDAO;
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository aCDTAllowedPrimitiveExpressionTypeMapDAO;
    private CoreDataTypeAllowedPrimitiveRepository aCDTAllowedPrimitiveDAO;
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository aCDTSCAllowedPrimitiveDAO;
    private XSDBuiltInTypeRepository aXSDBuiltInTypeDAO;
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtSCAPMapDAO;
    private CoreDataTypePrimitiveRepository aCDTPrimitiveDAO;
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtSCPRDAO;

    @PostConstruct
    public void init() throws Exception {
        bccpDAO = repositoryFactory.basicCoreComponentPropertyRepository();
        bdtSCPRDAO = repositoryFactory.businessDataTypeSupplementaryComponentPrimitiveRestrictionRepository();
        aBDTPrimitiveRestrictionDAO = repositoryFactory.businessDataTypePrimitiveRestrictionRepository();
        aCDTAllowedPrimitiveExpressionTypeMapDAO = repositoryFactory.coreDataTypeAllowedPrimitiveExpressionTypeMapRepository();
        aCDTAllowedPrimitiveDAO = repositoryFactory.coreDataTypeAllowedPrimitiveRepository();
        aCDTSCAllowedPrimitiveDAO = repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveRepository();
        aXSDBuiltInTypeDAO = repositoryFactory.xsdBuiltInTypeRepository();
        cdtSCAPMapDAO = repositoryFactory.coreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository();
        aCDTPrimitiveDAO = repositoryFactory.coreDataTypePrimitiveRepository();

        fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
        businessdatatype_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        component_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);

        f1 = new File(SRTConstants.CODE_LIST_FILE_PATH);
    }

    private File[] getBODs(File f) {
        return f.listFiles((dir, name) -> {
            return name.matches(".*.xsd");
        });
    }

    private void populate() throws Exception {
        NodeList elementsFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:element");
        NodeList elementsFromMetaXSD = meta_xsd.getNodeList("/xsd:schema/xsd:element");
        NodeList elementsFromComponentsXSD = component_xsd.getNodeList("/xsd:schema/xsd:element");

        insertCodeContentTypeDT();
        insertIDContentTypeDT();

        insertDTAndBCCP(elementsFromFieldsXSD, fields_xsd, 0);
        insertDTAndBCCP(elementsFromMetaXSD, meta_xsd, 1); // found that no QBDT from Meta.xsd, maybe because already imported in additional BDT
        insertDTAndBCCP(elementsFromComponentsXSD, component_xsd, 2);

		/*File[] listOfF1 = getBODs(f1);

		for (File file : listOfF1) {
			System.out.println(file.getName()+" ing...");
			XPathHandler codelist_xsd = new XPathHandler(SRTConstants.CODE_LIST_FILE_PATH + file.getName());
			NodeList elementsFromCodeListXSD = codelist_xsd.getNodeList("/xsd:schema/xsd:element");
			insertDTAndBCCP(elementsFromCodeListXSD, codelist_xsd, 3);
		}//this part is not used at all*/
        insertDTwithoutElement();

    }

    private List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> getCdtSCAPMap(int cdtSCAllowedPrimitiveId) throws Exception {
        return cdtSCAPMapDAO.findByCdtScAwdPri(cdtSCAllowedPrimitiveId);
    }

    private void insertDTwithoutElement() throws Exception {
        NodeList complexTypesFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:complexType");
        for (int i = 0; i < complexTypesFromFieldsXSD.getLength(); i++) {
            Node typeNode = complexTypesFromFieldsXSD.item(i);
            String type = ((Element) typeNode).getAttribute("name");
            String typeGuid = ((Element) typeNode).getAttribute("id");
            Node simpleContent = fields_xsd.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
            if (simpleContent != null) {
                try {
                    DataType dtVO = dataTypeRepository.findOneByGuid(typeGuid);
                } catch (EmptyResultDataAccessException e) {
                    // add new QBDT
                    DataType dVO = addToDT(typeGuid, type, typeNode, fields_xsd);

                    // add DT_SC
                    addToDTSC(fields_xsd, type, dVO);
                }
            }

        }
    }

    private void insertDTAndBCCP(NodeList elementsFromXSD, XPathHandler org_xHandler, int xsdType) throws Exception {
        XPathHandler xHandler = org_xHandler;
        for (int i = 0; i < elementsFromXSD.getLength(); i++) {//ElementsFromXSD don't have CodeContentType, IDContentType
            xHandler = org_xHandler;
            String bccp = ((Element) elementsFromXSD.item(i)).getAttribute("name");
            String guid = ((Element) elementsFromXSD.item(i)).getAttribute("id");
            String type = ((Element) elementsFromXSD.item(i)).getAttribute("type");
            //String den = Utility.createDenFormat(type);
            Node simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
            Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
            if (simpleContent == null && simpleType == null) {
                if (xsdType == 1)
                    xHandler = fields_xsd;
                else
                    xHandler = meta_xsd;
            }
            simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
            simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");

            if (simpleContent == null && simpleType == null) {
                if (xsdType == 2) {
                    xHandler = fields_xsd;
                    simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
                    simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
                }
            }
            if (simpleContent == null && simpleType == null)
                xHandler = businessdatatype_xsd;
            simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
            simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");

            if (simpleContent != null || simpleType != null) {
                Node documentationFromXSD = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation");
                String definition = "";
                if (documentationFromXSD != null) {
                    Node documentationFromCCTS = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
                    if (documentationFromCCTS != null)
                        definition = ((Element) documentationFromCCTS).getTextContent();
                    else
                        definition = ((Element) documentationFromXSD).getTextContent();
                }

                Node typeNode = xHandler.getNode("//xsd:complexType[@name = '" + type + "']");
                if (typeNode == null) {
                    typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
                }
                String typeGuid = ((Element) typeNode).getAttribute("id");
                try {
                    DataType dtVO = dataTypeRepository.findOneByGuid(typeGuid);

                    // add BCCP
                    addToBCCP(guid, bccp, dtVO, definition);
                } catch (EmptyResultDataAccessException e) {
                    // add new QBDT
                    DataType dVO = addToDT(typeGuid, type, typeNode, xHandler);

                    // add DT_SC
                    addToDTSC(xHandler, type, dVO);

                    // add BCCP
                    addToBCCP(guid, bccp, dVO, definition);
                }
            }
        }
    }

    private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> getBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtscVO) throws Exception {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtscs = bdtSCPRDAO.findByBdtScId(dtscVO.getBasedDtScId());
        if (bdtscs.isEmpty()) {
            if (dtscVO.getBasedDtScId() == 0) {
                return Collections.emptyList();
            }
            DataTypeSupplementaryComponent vo = dtScRepository.findOne(dtscVO.getBasedDtScId());
            bdtscs = getBDTSCPrimitiveRestriction(vo);
        }
        return bdtscs;
    }

    private void insertBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtscVO, int mode, String name, String type) throws Exception {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtscallowedprimitivelist = new ArrayList();
        // if (SC = inherit from the base BDT)
        if (mode == 1) {
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtscs = getBDTSCPrimitiveRestriction(dtscVO);
            for (BusinessDataTypeSupplementaryComponentPrimitiveRestriction parent : bdtscs) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPRVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtSCPRVO.setBdtScId(dtscVO.getDtScId());
                bdtSCPRVO.setCdtScAwdPriXpsTypeMapId(parent.getCdtScAwdPriXpsTypeMapId());
                bdtSCPRVO.setCodeListId(parent.getCodeListId());
                bdtSCPRVO.setDefault(parent.isDefault());
                bdtSCPRVO.setAgencyIdListId(parent.getAgencyIdListId());
                bdtSCPRDAO.save(bdtSCPRVO);
            }

        } else { // else if (new SC)
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtscprimitiverestionvo;

            cdtscallowedprimitivelist = getCdtSCAllowedPrimitiveID(dtscVO.getDtScId());
            for (CoreDataTypeSupplementaryComponentAllowedPrimitive svo : cdtscallowedprimitivelist) {
                List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> maps = getCdtSCAPMap(svo.getCdtScAwdPriId());
                for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap vo : maps) {
                    bdtscprimitiverestionvo = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                    bdtscprimitiverestionvo.setBdtScId(dtscVO.getDtScId());
                    bdtscprimitiverestionvo.setCdtScAwdPriXpsTypeMapId(vo.getCdtScAwdPriXpsTypeMapId());

                    if (type.equalsIgnoreCase("NumberType_B98233")) {
                        if (svo.getCdtPriId() == getCdtPriId("Decimal") && vo.getXbtId() == getXbtId("xsd:decimal"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("CodeType_1E7368") || type.equalsIgnoreCase("CodeContentType") || name.equalsIgnoreCase("countryCode")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("StringType")) {
                        if (svo.getCdtPriId() == getCdtPriId("String") && vo.getXbtId() == getXbtId("xsd:string"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("NormalizedStringType")) {
                        if (svo.getCdtPriId() == getCdtPriId("String") && vo.getXbtId() == getXbtId("xsd:string"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (name.equalsIgnoreCase("listID") || name.equalsIgnoreCase("listVersionID") || name.equalsIgnoreCase("unitCodeListVersionID")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("DateTimeType")) {
                        if (svo.getCdtPriId() == getCdtPriId("TimePoint") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("IndicatorType")) {
                        if (svo.getCdtPriId() == getCdtPriId("Boolean") && vo.getXbtId() == getXbtId("xsd:boolean"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.equalsIgnoreCase("ValueType_E7171E")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (name.equalsIgnoreCase("name")) {
                        if (svo.getCdtPriId() == getCdtPriId("NormalizedString") && vo.getXbtId() == getXbtId("xsd:normalizedString"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);

                    } else if (type.contains("CodeContentType")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);
                        //bdtSCPRDAO.save(bdtscprimitiverestionvo);

//						// add code_list id for this case
//						bdtscprimitiverestionvo.setCodeListId(getCodeListId(type.substring(0, type.indexOf("CodeContentType"))));
//						bdtscprimitiverestionvo.setDefault(false);
//						bdtSCPRDAO.save(bdtscprimitiverestionvo);
//						continue;

                    } else if (name.equalsIgnoreCase("listAgencyID")) {
                        if (svo.getCdtPriId() == getCdtPriId("Token") && vo.getXbtId() == getXbtId("xsd:token"))
                            bdtscprimitiverestionvo.setDefault(true);
                        else
                            bdtscprimitiverestionvo.setDefault(false);
                        //bdtSCPRDAO.save(bdtscprimitiverestionvo);

//						// add agency_id_list id for this case
//						bdtscprimitiverestionvo.setAgencyIdListId(getAgencyListID());
//						bdtscprimitiverestionvo.setDefault(false);
//						bdtSCPRDAO.save(bdtscprimitiverestionvo);
//						continue;
                    }

                    bdtSCPRDAO.save(bdtscprimitiverestionvo);

                }
            }

            if (type.contains("CodeContentType")) {
                // add code_list id for this case
                bdtscprimitiverestionvo = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtscprimitiverestionvo.setBdtScId(dtscVO.getDtScId());
                bdtscprimitiverestionvo.setCodeListId(getCodeListId(type.substring(0, type.indexOf("CodeContentType"))));
                bdtscprimitiverestionvo.setDefault(false);
                bdtSCPRDAO.save(bdtscprimitiverestionvo);
            }

            if (name.equalsIgnoreCase("listAgencyID")) {
                // add agency_id_list id for this case
                bdtscprimitiverestionvo = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                bdtscprimitiverestionvo.setBdtScId(dtscVO.getDtScId());
                bdtscprimitiverestionvo.setAgencyIdListId(getAgencyListID());
                bdtscprimitiverestionvo.setDefault(false);
                bdtSCPRDAO.save(bdtscprimitiverestionvo);
            }
        }
    }

    private DataType addToDT(String guid, String type, Node typeNode, XPathHandler xHandler) throws Exception {
        DataType dVO = new DataType();

        DataType dtVO = new DataType();
        dtVO.setGuid(guid);
        dtVO.setType(1);
        dtVO.setVersionNum("1.0");
        //dtVO.setRevisionType(0);

        //System.out.println("### type: " + type);

        Element extension = (Element) ((Element) typeNode).getElementsByTagName("xsd:extension").item(0);
        String base = extension.getAttribute("base");

        if (base.endsWith("CodeContentType")) {
            dVO = getDataTypeWithDen("Code. Type");
        } else {
            String den = Utility.typeToDen(base);
            dVO = getDataTypeWithDen(den);

            // QBDT is based on another QBDT
            if (dVO == null) {
                Node newTypeNode = xHandler.getNode("//xsd:complexType[@name = '" + base + "']");
                if (newTypeNode == null) {
                    newTypeNode = xHandler.getNode("//xsd:simpleType[@name = '" + base + "']");
                }
                Element newType = (Element) newTypeNode;
                String newGuid = newType.getAttribute("id");
                dVO = getDataTypeWithGUID(newGuid);
                if (dVO == null)
                    dVO = addToDT(newGuid, base, newTypeNode, xHandler);
            }
        }

        dtVO.setBasedDtId(dVO.getDtId());
        dtVO.setDataTypeTerm(dVO.getDataTypeTerm());

        String qualifier = Utility.qualifier(type, dVO);
        if (qualifier.length() == 0 || qualifier.isEmpty() || qualifier == null) {
            System.out.println("!!Null Qualifier Detected During Import QBDT " + type + " based on Den:" + dVO.getDen());
        }
        dtVO.setQualifier(qualifier);
        String den = Utility.denWithQualifier(qualifier, dVO.getDen());
        dtVO.setDen(den);
        dtVO.setContentComponentDen(den.substring(0, den.indexOf(".")) + ". Content");
        Node definitionNode = xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation");
        String definition = null;
        if (((Element) definitionNode) != null)
            definition = ((Element) definitionNode).getTextContent();
        else if (xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]") != null) {
            definitionNode = xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
            definition = ((Element) definitionNode).getTextContent();
        } else
            definition = null;
        dtVO.setDefinition(definition);
        dtVO.setContentComponentDefinition(null);
        dtVO.setState(3);
        int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
        dtVO.setCreatedBy(userId);
        dtVO.setLastUpdatedBy(userId);
        dtVO.setOwnerUserId(userId);
        dtVO.setRevisionDoc("");
        dtVO.setRevisionNum(0);
        dtVO.setRevisionTrackingNum(0);
        dtVO.setDeprecated(false);
        dataTypeRepository.save(dtVO);

        DataType res = dataTypeRepository.findOneByGuid(guid);
        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(res, base);

        return res;
    }

    private void insertBDTPrimitiveRestriction(DataType dVO, String base) throws Exception {
        List<BusinessDataTypePrimitiveRestriction> al = aBDTPrimitiveRestrictionDAO.findByBdtId(dVO.getBasedDtId());

//		//the previous condition below cannot classify the cases correctly.
//		//we need 3 cases : CodeContentQBDTs, IDContentQBDT, and other QBDTs
//		if(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
        if (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
            //same as (DataTypeTerm = "Code") & (base != "CodeType")
            BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
            theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
            if (base.endsWith("CodeContentType")) {
                theBDT_Primitive_RestrictionVO.setCodeListId(getCodeListId(base.substring(0, base.indexOf("CodeContentType"))));
            } else {//MatchCodeType, ResponseCodeType
                for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
                    if (aBusinessDataTypePrimitiveRestriction.getCodeListId() > 0) {
                        theBDT_Primitive_RestrictionVO.setCodeListId(aBusinessDataTypePrimitiveRestriction.getCodeListId());
                        break;
                    }
                }
            }
            theBDT_Primitive_RestrictionVO.setDefault(false);
            aBDTPrimitiveRestrictionDAO.save(theBDT_Primitive_RestrictionVO);
        }

        if (dVO.getDataTypeTerm().equalsIgnoreCase("Identifier") && base.endsWith("IDContentType")) {
            BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
            theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
            theBDT_Primitive_RestrictionVO.setAgencyIdListId(getAgencyListID());
            theBDT_Primitive_RestrictionVO.setDefault(false);
            aBDTPrimitiveRestrictionDAO.save(theBDT_Primitive_RestrictionVO);
        }

        if (!dVO.getDataTypeTerm().equalsIgnoreCase("Code") || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.endsWith("CodeType")) || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.endsWith("CodeContentType"))) {
            //
            //third or condition is not fine because we might apply this code to base = "CodeContentType" not only end-with "CodeContentType"
            for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
                BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
                theBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());

//				//Previous Code re-assign isDefault value, but design-doc said just copy it's base DT's
                //So i make change
//				if(base.endsWith("CodeContentType") && checkTokenofXBT(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId()))
//					theBDT_Primitive_RestrictionVO.setDefault(true);
//				else if(base.endsWith("CodeContentType") && !checkTokenofXBT(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId()))
//					theBDT_Primitive_RestrictionVO.setDefault(false);
//				else
//					theBDT_Primitive_RestrictionVO.setDefault(aBusinessDataTypePrimitiveRestriction.isDefault());
//				aBDTPrimitiveRestrictionDAO.save(theBDT_Primitive_RestrictionVO);

                theBDT_Primitive_RestrictionVO.setDefault(aBusinessDataTypePrimitiveRestriction.isDefault());
                aBDTPrimitiveRestrictionDAO.save(theBDT_Primitive_RestrictionVO);

            }
        }

//		if(!dVO.getDataTypeTerm().equalsIgnoreCase("Code") || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
//			for(SRTObject aSRTObject : al) {
//				BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction = (BusinessDataTypePrimitiveRestriction)aSRTObject;
//				BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
//				theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
//				theBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
//				theBDT_Primitive_RestrictionVO.setDefault(aBusinessDataTypePrimitiveRestriction.isDefault());
//				
//				aBDTPrimitiveRestrictionDAO.save(theBDT_Primitive_RestrictionVO);
//			}
//		} else {
//			BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
//			theBDT_Primitive_RestrictionVO.setBdtId(dVO.getDtId());
//			if(base.endsWith("CodeContentType")) {
//				theBDT_Primitive_RestrictionVO.setCodeListId(getCodeListId(base.substring(0, base.indexOf("CodeContentType"))));
//			} else {
//				for(SRTObject aSRTObject : al) {
//					BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction = (BusinessDataTypePrimitiveRestriction)aSRTObject;
//					if(aBusinessDataTypePrimitiveRestriction.getCodeListId() > 0) {
//						theBDT_Primitive_RestrictionVO.setCodeListId(aBusinessDataTypePrimitiveRestriction.getCodeListId());
//						break;
//					}
//				}
//			}
//			theBDT_Primitive_RestrictionVO.setDefault(true);
//			aBDTPrimitiveRestrictionDAO.save(theBDT_Primitive_RestrictionVO);
//		}
    }

    private void addToBCCP(String guid, String bccp, DataType dtVO, String definition) throws Exception {

        BasicCoreComponentProperty bccpVO = new BasicCoreComponentProperty();
        bccpVO.setGuid(guid);

        String propertyTerm = Utility.spaceSeparator(bccp.replaceAll("ID", "Identifier"));
        bccpVO.setPropertyTerm(propertyTerm);
        bccpVO.setRepresentationTerm(dtVO.getDataTypeTerm());
        bccpVO.setBdtId(dtVO.getDtId());
        bccpVO.setDen(Utility.firstToUpperCase(propertyTerm) + ". " + dtVO.getDataTypeTerm());
        bccpVO.setDefinition(definition);
        bccpVO.setState(3);
        int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
        dtVO.setCreatedBy(userId);
        dtVO.setLastUpdatedBy(userId);
        dtVO.setOwnerUserId(userId);
        bccpDAO.save(bccpVO);

    }

    private void addToDTSC(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {

        // inherit from the base BDT
        int owner_dT_iD = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> dtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());
        for (DataTypeSupplementaryComponent dtsc_vo : dtsc_vos) {
            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            vo.setGuid(dtsc_vo.getGuid());
            vo.setPropertyTerm(dtsc_vo.getPropertyTerm());
            vo.setRepresentationTerm(dtsc_vo.getRepresentationTerm());
            vo.setOwnerDtId(owner_dT_iD);

            vo.setMinCardinality(dtsc_vo.getMinCardinality());
            vo.setMaxCardinality(dtsc_vo.getMaxCardinality());
            vo.setBasedDtScId(dtsc_vo.getDtScId());

            dtScRepository.save(vo);

            insertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dtsc_vo.getGuid(), owner_dT_iD), 1, "", "");
        }

        // new SC
        NodeList attributeList = xHandler.getNodeList("//xsd:complexType[@id = '" + qbdtVO.getGuid() + "']/xsd:simpleContent/xsd:extension/xsd:attribute");

        if (attributeList == null || attributeList.getLength() == 0) {
            //System.out.println("##### " + "//xsd:"+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
        } else {
            String dt_sc_guid = "";
            String property_term = "";
            String representation_term = "";

            String definition;
            int min_cardinality = 0;
            int max_cardinality = 1;

            for (int i = 0; i < attributeList.getLength(); i++) {
                Node attribute = attributeList.item(i);
                Element attrElement = (Element) attribute;
                dt_sc_guid = attrElement.getAttribute("id");

                if (attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
                    min_cardinality = 0;
                else if (attrElement.getAttribute("use").equalsIgnoreCase("required"))
                    min_cardinality = 1;

                if (attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("required"))
                    max_cardinality = 1;
                else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
                    max_cardinality = 0;

                String attrName = attrElement.getAttribute("name");

                // Property Term
                if (attrName.endsWith("Code"))
                    property_term = attrName.substring(0, attrName.indexOf("Code"));
                else if (attrName.endsWith("ID"))
                    property_term = attrName.substring(0, attrName.indexOf("ID"));
                else if (attrName.endsWith("Value"))
                    property_term = attrName.substring(0, attrName.indexOf("Value"));
                else
                    property_term = Utility.spaceSeparator(attrName);

                if (property_term.trim().length() == 0)
                    property_term = attrName;

                property_term = Utility.firstToUpperCase(property_term);

                // Representation Term
                if (attrName.endsWith("Code") || attrName.endsWith("code")) {
                    representation_term = "Code";
                } else if (attrName.endsWith("Number")) {
                    representation_term = "Number";
                } else if (attrName.endsWith("ID")) {
                    representation_term = "Identifier";
                } else if (attrName.endsWith("DateTime")) {
                    representation_term = "Date Time";
                } else if (attrName.endsWith("Value")) {
                    representation_term = "Value";
                } else if (attrName.endsWith("Name") || attrName.endsWith("name")) {
                    representation_term = "Name";
                } else {
                    String attrType = attrElement.getAttribute("type");
                    if (attrType.equals("StringType") || attrType.equals("NormalizedStringType"))
                        representation_term = "Text";
                    else if (attrType.equals("IndicatorType"))
                        representation_term = "Indicator";
                }

                Node documentationNode = xHandler.getNode("//xsd:complexType[@id = '" + qbdtVO.getGuid() + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation");
                if (documentationNode != null) {
                    Node documentationFromCCTS = xHandler.getNode("//xsd:complexType[@id = '" + qbdtVO.getGuid() + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
                    if (documentationFromCCTS != null)
                        definition = ((Element) documentationFromCCTS).getTextContent();
                    else
                        definition = ((Element) documentationNode).getTextContent();
                } else {
                    definition = null;
                }

                DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
                vo.setGuid(dt_sc_guid);
                vo.setPropertyTerm(Utility.spaceSeparator(property_term));
                vo.setRepresentationTerm(representation_term);
                vo.setDefinition(definition);
                vo.setOwnerDtId(owner_dT_iD);

                vo.setMinCardinality(min_cardinality);
                vo.setMaxCardinality(max_cardinality);

//				Both based dtsc and target dtsc have listAgencyID
//				target dtsc inherits all attrs from the base
//				since the attr name is the same, it just update the guid
//				in this case, the target dtsc is new? or not?

                DataTypeSupplementaryComponent duplicate = checkDuplicate(vo);
                if (duplicate == null) {
                    dtScRepository.save(vo);

                    // populate CDT_SC_Allowed_Primitives
                    DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByGuidAndOwnerDtId(vo.getGuid(), vo.getOwnerDtId());
                    String representationTerm = dtscVO.getRepresentationTerm();
                    DataType dtVO = getDataTypeWithRepresentationTerm(representationTerm);

                    CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAllowedVO = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                    cdtSCAllowedVO.setCdtScId(dtscVO.getDtScId());
                    List<CoreDataTypeAllowedPrimitive> cdtallowedprimitivelist = getCDTAllowedPrimitiveIDs(dtVO.getDtId());
                    for (CoreDataTypeAllowedPrimitive svo : cdtallowedprimitivelist) {
                        cdtSCAllowedVO.setCdtPriId(svo.getCdtPriId());
                        cdtSCAllowedVO.setDefault(svo.isDefault());
                        aCDTSCAllowedPrimitiveDAO.save(cdtSCAllowedVO);

                        // populate CDT_SC_Allowed_Primitive_Expression_Type_Map
                        int cdtSCAllowedPrimitiveId =
                                aCDTSCAllowedPrimitiveDAO
                                        .findOneByCdtScIdAndCdtPriId(cdtSCAllowedVO.getCdtScId(), cdtSCAllowedVO.getCdtPriId())
                                        .getCdtScAwdPriId();

                        List<String> xsdbs = Types.getCorrespondingXSDBuiltType(getPrimitiveName(cdtSCAllowedVO.getCdtPriId()));
                        for (String xbt : xsdbs) {
                            CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap mapVO =
                                    new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                            mapVO.setCdtScAwdPri(cdtSCAllowedPrimitiveId);
                            int xdtBuiltTypeId = aXSDBuiltInTypeDAO.findOneByBuiltInType(xbt).getXbtId();
                            mapVO.setXbtId(xdtBuiltTypeId);
                            cdtSCAPMapDAO.save(mapVO);
                        }
                    }

                    insertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dt_sc_guid, owner_dT_iD), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
                } else {
                    vo.setDtScId(duplicate.getDtScId());
                    vo.setBasedDtScId(duplicate.getBasedDtScId());
                    dtScRepository.save(vo);
                }
            }
        }
    }

    private DataTypeSupplementaryComponent checkDuplicate(DataTypeSupplementaryComponent dtVO) throws Exception {
        try {
            return dtScRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(
                    dtVO.getOwnerDtId(), dtVO.getPropertyTerm(), dtVO.getRepresentationTerm());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public int getCodeListId(String codeName) throws Exception {
        List<CodeList> al = codeListRepository.findByNameContaining(codeName.trim());
        int minStrLen = Integer.MAX_VALUE;
        int minInd = -1;
        for (CodeList codelistVO : al) {
            if (minStrLen > codelistVO.getName().length()) {
                minStrLen = codelistVO.getName().length();
                minInd = codelistVO.getCodeListId();
            }
        }
        return minInd;
    }

    public int getAgencyListID() throws Exception {
        return agencyIdListRepository.findOneByName("Agency Identification").getAgencyIdListId();
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid) throws Exception {
        return dtScRepository.findOneByGuid(guid);
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid, int ownerId) throws Exception {
        return dtScRepository.findOneByGuidAndOwnerDtId(guid, ownerId);
    }

    public int getDtId(String DataTypeTerm) throws Exception {
        DataType dtVO = dataTypeRepository.findOneByDataTypeTermAndType(DataTypeTerm, 0);
        int id = dtVO.getDtId();
        return id;
    }

    public String getRepresentationTerm(String Guid) throws Exception {
        DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByGuid(Guid);
        String term = dtscVO.getRepresentationTerm();
        return term;
    }

    public String getPrimitiveName(int CdtPriId) throws Exception {
        return aCDTPrimitiveDAO.findOneByCdtPriId(CdtPriId).getName();
    }


    public int getCdtPriId(String name) throws Exception {
        return aCDTPrimitiveDAO.findOneByName(name).getCdtPriId();
    }

    public List<CoreDataTypeAllowedPrimitive> getCDTAllowedPrimitiveIDs(int cdt_id) throws Exception {
        return aCDTAllowedPrimitiveDAO.findByCdtId(cdt_id);
    }

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> getCdtSCAllowedPrimitiveID(int dt_sc_id) throws Exception {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> res = aCDTSCAllowedPrimitiveDAO.findByCdtScId(dt_sc_id);
        if (res.isEmpty()) {
            DataTypeSupplementaryComponent dtscVO = dtScRepository.findOne(dt_sc_id);
            res = getCdtSCAllowedPrimitiveID(dtscVO.getBasedDtScId());
        }
        return res;
    }

    public int getXbtId(String BuiltIntype) throws Exception {
        return aXSDBuiltInTypeDAO.findOneByBuiltInType(BuiltIntype).getXbtId();
    }

    public boolean checkTokenofXBT(int cdt_awd_pri_xps_type_map_id) throws Exception {
        CoreDataTypeAllowedPrimitiveExpressionTypeMap aCoreDataTypeAllowedPrimitiveExpressionTypeMap =
                aCDTAllowedPrimitiveExpressionTypeMapDAO.findOneByCdtAwdPriXpsTypeMapId(cdt_awd_pri_xps_type_map_id);
        XSDBuiltInType aXSDBuiltInType = aXSDBuiltInTypeDAO.findOneByXbtId(
                aCoreDataTypeAllowedPrimitiveExpressionTypeMap.getXbtId());
        if (aXSDBuiltInType.getName().equalsIgnoreCase("token"))
            return true;
        else
            return false;
    }

    private DataType getDataTypeWithDen(String den) throws Exception {
        try {
            return dataTypeRepository.findOneByTypeAndDen(1, den);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private DataType getDataTypeWithRepresentationTerm(String representationTerm) throws Exception {
        return dataTypeRepository.findOneByDataTypeTermAndType(representationTerm, 0);
    }

    private DataType getDataTypeWithGUID(String guid) throws Exception {
        try {
            return dataTypeRepository.findOneByGuid(guid);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void insertCodeContentTypeDT() throws Exception {
        String dataType = "CodeContentType";
        NodeList simpleTypesFromFieldsXSD = fields_xsd.getNodeList("//xsd:simpleType");

        for (int i = 0; i < simpleTypesFromFieldsXSD.getLength(); i++) {
            Node typeNode = simpleTypesFromFieldsXSD.item(i);
            String type = ((Element) typeNode).getAttribute("name");
            if (type.endsWith(dataType) && !type.equals(dataType)) {
                String typeGuid = ((Element) typeNode).getAttribute("id");
                try {
                    DataType dtVO = dataTypeRepository.findOneByGuid(typeGuid);
                } catch (EmptyResultDataAccessException e) {
                    // add new QBDT
                    DataType dVO = addToDTForContentType(typeGuid, type, typeNode, fields_xsd);

                    // add DT_SC
                    addToDTSCForContentType(fields_xsd, type, dVO);
                }
            }
        }
    }

    private void insertIDContentTypeDT() throws Exception {
        String dataType = "IDContentType";
        NodeList simpleTypesFromFieldsXSD = fields_xsd.getNodeList("//xsd:simpleType");

        for (int i = 0; i < simpleTypesFromFieldsXSD.getLength(); i++) {
            Node typeNode = simpleTypesFromFieldsXSD.item(i);
            String type = ((Element) typeNode).getAttribute("name");
            if (type.endsWith(dataType) && !type.equals(dataType)) {
                String typeGuid = ((Element) typeNode).getAttribute("id");
                try {
                    DataType dtVO = dataTypeRepository.findOneByGuid(typeGuid);
                } catch (EmptyResultDataAccessException e) {
                    // add new QBDT
                    DataType dVO = addToDTForContentType(typeGuid, type, typeNode, fields_xsd);

                    // add DT_SC
                    addToDTSCForContentType(fields_xsd, type, dVO);
                }
            }
        }
    }

    private DataType addToDTForContentType(String guid, String type, Node typeNode, XPathHandler xHandler) throws Exception {
        DataType dVO = new DataType();

        DataType dtVO = new DataType();
        dtVO.setGuid(guid);
        dtVO.setType(1);
        dtVO.setVersionNum("1.0");
        //dtVO.setRevisionType(0);

        //System.out.println("### type: " + type);

        Element extension = (Element) ((Element) typeNode).getElementsByTagName("xsd:restriction").item(0);
        String base = extension.getAttribute("base");

        if (base.endsWith("CodeContentType")) {
            dVO = getDataTypeWithDen("Code Content. Type");
        } else { //else if (base.endsWith("IDContentType")){
            dVO = getDataTypeWithDen("Identifier Content. Type");
            base = "IDContentType";
        }

        dtVO.setBasedDtId(dVO.getDtId());
        dtVO.setDataTypeTerm(dVO.getDataTypeTerm());

        String qualifier = Utility.qualifier(type, dVO);
        if (qualifier.length() == 0 || qualifier.isEmpty() || qualifier == null) {
            System.out.println("!!Null Qualifier Detected During Import QBDT " + type + " based on Den: " + dVO.getDen());
        }

        dtVO.setQualifier(qualifier);
        String den = Utility.denWithQualifier(qualifier, dVO.getDen());
        dtVO.setDen(den);
        dtVO.setContentComponentDen(den.substring(0, den.indexOf(".")) + ". Content");
        String definition = null;
        Node definitionNode = xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation");
        if (((Element) definitionNode) != null)
            definition = ((Element) definitionNode).getTextContent();
        else if (xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]") != null) {
            definitionNode = xHandler.getNode("//xsd:simpleType[@name = '" + base + "']//xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
            definition = ((Element) definitionNode).getTextContent();
        } else
            definition = null;
        dtVO.setDefinition(definition);
        dtVO.setContentComponentDefinition(null);
        dtVO.setState(3);
        int userId = userRepository.findOneByLoginId("oagis").getAppUserId();
        dtVO.setCreatedBy(userId);
        dtVO.setLastUpdatedBy(userId);
        dtVO.setOwnerUserId(userId);
        dtVO.setRevisionDoc("");
        dtVO.setRevisionNum(0);
        dtVO.setRevisionTrackingNum(0);
        dtVO.setDeprecated(false);
        dataTypeRepository.save(dtVO);

        DataType res = dataTypeRepository.findOneByGuid(guid);
        // add to BDTPrimitiveRestriction
        insertBDTPrimitiveRestriction(res, base);

        return res;
    }

    private void addToDTSCForContentType(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {

        // inherit from the base BDT
        int owner_dT_iD = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> dtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());
        for (DataTypeSupplementaryComponent dtsc_vo : dtsc_vos) {
            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            vo.setGuid(dtsc_vo.getGuid());
            vo.setPropertyTerm(dtsc_vo.getPropertyTerm());
            vo.setRepresentationTerm(dtsc_vo.getRepresentationTerm());
            vo.setOwnerDtId(owner_dT_iD);

            vo.setMinCardinality(dtsc_vo.getMinCardinality());
            //vo.setMaxCardinality(dtsc_vo.getMaxCardinality());
            vo.setMaxCardinality(0);
            vo.setBasedDtScId(dtsc_vo.getDtScId());

            dtScRepository.save(vo);

            insertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dtsc_vo.getGuid(), owner_dT_iD), 1, "", "");

        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.7 Start");

        populate();

        System.out.println("### 1.7 End");
    }

    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_7_PopulateQBDTInDT populateQBDTInDT = ctx.getBean(P_1_7_PopulateQBDTInDT.class);
            populateQBDTInDT.run(ctx);
        }
    }
}
