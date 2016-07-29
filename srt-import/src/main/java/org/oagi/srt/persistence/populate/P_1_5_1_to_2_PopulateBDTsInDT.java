package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

import static org.oagi.srt.common.SRTConstants.OAGIS_VERSION;

/**
 * @author Nasif Sikder
 * @author Jaehun Lee
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_5_1_to_2_PopulateBDTsInDT {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ImportUtil importUtil;

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_5_1_to_2_PopulateBDTsInDT populateBDTsInDT = ctx.getBean(P_1_5_1_to_2_PopulateBDTsInDT.class);
            populateBDTsInDT.run(ctx);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.5.1-2 Start");

        for (int i = 0; i < Types.dataTypeList.length; i++) {
            importDataTypeList(Types.dataTypeList[i]);
        }

        importExceptionalDataTypeList();
        importExceptionalDataTypeList2("CodeType_1E7368");
        importExceptionalDataTypeList2("ValueType_039C44");
        
        importCodeContentType();
        importIDContentType();

        logger.info("### 1.5.1-2 End");
    }

    private void importDataTypeList(String dataType) throws Exception {
        logger.debug("Importing " + dataType + " now");
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String type = "complex";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(SRTConstants.FIELDS_XSD_FILE_PATH));

        //Type Name
        Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension");
        if (typeNameNode == null) {
            type = "simple";
            typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        }
        Element typeNameElement = (Element) typeNameNode;
        typeName = typeNameElement.getAttribute("base");
        logger.debug("!! typeName = " + typeName);
        Node aNodeTN = fields_xsd.getNode("//xsd:" + type + "Type[@name = '" + dataType + "']");
        Element aElementTN = (Element) aNodeTN;

        //Data Type Term
        Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

        if (dataTypeTermNode == null && type.equals("simple")) {
            type = "complex";
            dataTypeTermNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
        } else if (dataTypeTermNode == null && type.equals("complex")) {
            type = "simple";
            dataTypeTermNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
        }
        Element dataTypeTermElement = (Element) dataTypeTermNode;
        logger.debug("### " + dataTypeTermElement.getTextContent());
        try {
            dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
            if (dataTypeTerm == "")
                logger.debug("Error getting the data type term for the unqualified BDT: " + dataType);
        } catch (Exception e) {
            logger.debug("Error getting the data type term for the unqualified BDT: " + dataType + " Stacktrace:" + e.getMessage());
        }

        //Definitions
        Node definitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
        Element definitionElement = (Element) definitionNode;
        Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        Element ccDefinitionElement = (Element) ccDefinitionNode;

        String definition = "";
        String ccDefinition = "";

        definition = definitionElement.getTextContent();

        if (ccDefinitionElement != null) {
            ccDefinition = ccDefinitionElement.getTextContent();
        } else {
            ccDefinition = null;
        }

        //This is the default BDT type definition node
        Node aNodeBDT = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']");
        Element aElementBDT = (Element) aNodeBDT;

        Node union = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union");
        int defaultId = -1;
        if (union != null) {
            defaultId = xbtRepository.findOneByName("token").getXbtId();
        } else {
            Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']//xsd:extension");
            if (xsdTypeNameNode == null)
                xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']//xsd:restriction");
            if (xsdTypeNameNode != null) {
                Element xsdTypeNameElement = (Element) xsdTypeNameNode;
                xsdTypeName = xsdTypeNameElement.getAttribute("base");

                XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsdTypeName);
                if (xbt != null) {
                    defaultId = xbt.getXbtId();
                } else {
                    Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                    defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
                }
            }
        }

        if (defaultId == -1 || defaultId == 0)
            logger.debug("Error getting the default BDT primitive restriction for the default BDT: " + typeName);
        logger.debug("data type term = " + dataTypeTerm);

        DataType dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definition,
                ccDefinition, aElementBDT.getAttribute("id"), module);
        logger.debug("Inserting bdt primitive restriction for default bdt");
        insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);

        //Unqualified Type Name
        String unQualifiedTypeName = dataType;

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = dataTypeTerm;
        DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm,
                aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"), module);
        logger.debug("Inserting bdt primitive restriction for unqualfieid bdt");
        insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
    }

    public DataType insertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition,
                                               String ccDefinition, String id, Module module) throws Exception {
        int basedDTID = dataTypeRepository.findOneByDataTypeTermAndType(dataTypeTerm, 0).getDtId();
        DataType dtVO = dataTypeRepository.findOneByGuid(id);
        if (dtVO == null) {
            logger.debug("Inserting default bdt whose name is " + typeName);

            dtVO = new DataType();

            dtVO.setGuid(id);
            dtVO.setType(1);
            dtVO.setVersionNum("1.0");
            //dtVO.setRevisionType(0);
            dtVO.setDataTypeTerm(dataTypeTerm);
            dtVO.setBasedDtId(basedDTID);
            dtVO.setDen(Utility.typeToDen(typeName));
            dtVO.setContentComponentDen(Utility.typeToContent(typeName));
            dtVO.setDefinition(definition);
            dtVO.setContentComponentDefinition(ccDefinition);
            dtVO.setState(3);
            dtVO.setCreatedBy(importUtil.getUserId());
            dtVO.setLastUpdatedBy(importUtil.getUserId());
            dtVO.setOwnerUserId(importUtil.getUserId());
            dtVO.setRevisionDoc(null);
            dtVO.setRevisionNum(0);
            dtVO.setRevisionTrackingNum(0);
            dtVO.setDeprecated(false);
            dtVO.setReleaseId(importUtil.getReleaseId());
            dtVO.setModule(module);

            dtVO = dataTypeRepository.saveAndFlush(dtVO);
        }

        return dtVO;
    }

    private void insertBDTPrimitiveRestriction(int cdtId, int bdtID, int defaultId) throws Exception {
        List<CoreDataTypeAllowedPrimitive> al3 = cdtAwdPriRepository.findByCdtId(cdtId);
        DataType bdt = dataTypeRepository.findOne(bdtID);
        
        if(bdt.getBasedDtId()==cdtId) {//if default BDT
        	for (CoreDataTypeAllowedPrimitive aCDTAllowedPrimitiveVO : al3) {

                List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> al4 =
                        cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId(aCDTAllowedPrimitiveVO.getCdtAwdPriId());

                for (CoreDataTypeAllowedPrimitiveExpressionTypeMap aCDTAllowedPrimitiveExpressionTypeMapVO : al4) {
                    // create insert statement
                    BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                    aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
                    aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aCDTAllowedPrimitiveExpressionTypeMapVO.getCdtAwdPriXpsTypeMapId());
                    boolean isDefault = false;
                    if (defaultId == aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId())
                        isDefault = true;
                    aBDT_Primitive_RestrictionVO.setDefault(isDefault);
                    logger.debug("Inserting allowed primitive expression type map with XSD built-in type in DefaultBDT" +
                            getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = " + isDefault);
                    bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);
                }
            }
        }
        else {
        	List<BusinessDataTypePrimitiveRestriction> defaultBDTPri = bdtPriRestriRepository.findByBdtId(bdt.getBasedDtId());
        	for(int i=0; i<defaultBDTPri.size(); i++){
        		BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
        		aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
        		aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(defaultBDTPri.get(i).getCdtAwdPriXpsTypeMapId());
        		aBDT_Primitive_RestrictionVO.setDefault(defaultBDTPri.get(i).isDefault());
        		logger.debug("Inherit allowed primitive expression type map with XSD built-in type in unqualified BDT");
                bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);
        	}
        }
        
    }

    private String getXsdBuiltinType(int id) {
        return xbtRepository.findOne(id).getBuiltInType();
    }

    public DataType insertUnqualified_BDTStatement(String typeName, String dataTypeTerm,
                                                   String id, String defaultGUID, Module module) throws Exception {
        int basedDTID = dataTypeRepository.findOneByGuid(defaultGUID).getDtId();

        DataType dtVO = dataTypeRepository.findOneByGuid(id);
        if (dtVO == null) {
            dtVO = new DataType();

            dtVO.setGuid(id);
            dtVO.setType(1);
            dtVO.setVersionNum("1.0");
            //dtVO.setRevisionType(0);
            dtVO.setDataTypeTerm(dataTypeTerm);
            dtVO.setBasedDtId(basedDTID);
            dtVO.setDen(Utility.typeToDen(typeName));
            dtVO.setContentComponentDen(Utility.typeToContent(typeName));
            dtVO.setState(3);
            dtVO.setCreatedBy(importUtil.getUserId());
            dtVO.setLastUpdatedBy(importUtil.getUserId());
            dtVO.setOwnerUserId(importUtil.getUserId());
            dtVO.setRevisionDoc(null);
            dtVO.setRevisionNum(0);
            dtVO.setRevisionTrackingNum(0);
            dtVO.setDeprecated(false);
            dtVO.setReleaseId(importUtil.getReleaseId());
            dtVO.setModule(module);
            dtVO = dataTypeRepository.saveAndFlush(dtVO);
        }

        return dtVO;
    }

    public boolean check_BDT(String id) {
        return (dataTypeRepository.findOneByGuid(id) == null) ? false : true;
    }

    private void importExceptionalDataTypeList2(String dataType) throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH));

        typeName = dataType;
        String type = "simple";
        Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

        Element dataTypeTermElement = (Element) dataTypeTermNode;
        dataTypeTerm = dataTypeTermElement.getTextContent();
        if (dataTypeTerm.length() > 5)
            if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
                dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);

        //Definitions
        Node definitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
        Element definitionElement = (Element) definitionNode;
        Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        Element ccDefinitionElement = (Element) ccDefinitionNode;

        Node aNodeBDT = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']");
        Element aElementBDT = (Element) aNodeBDT;

        Node union = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union");
        int defaultId = -1;
        if (union != null) {
            defaultId = xbtRepository.findOneByName("token").getXbtId();
        } else {
            Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']//xsd:extension");
            if (xsdTypeNameNode == null)
                xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']//xsd:restriction");
            if (xsdTypeNameNode != null) {
                Element xsdTypeNameElement = (Element) xsdTypeNameNode;
                xsdTypeName = xsdTypeNameElement.getAttribute("base");

                XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsdTypeName);
                if (xbt != null) {
                    defaultId = xbt.getXbtId();
                } else {
                    Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                    defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
                }
            }
        }

        DataType dVO1 = new DataType();

        if (check_BDT(aElementBDT.getAttribute("id"))) {
            logger.debug("Default BDT is already existing");
            dVO1 = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));
        } else {
            dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(),
                    (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"), module);
            logger.debug("Inserting bdt primitive restriction for exceptional default bdt");
            insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);
        }

        if (check_BDT(aElementBDT.getAttribute("id")))
            logger.debug("Unqualified BDT is already existing");
        else {
            //Unqualified Type Name
            String unQualifiedTypeName = dataType;

            //Unqualified Data Type Term
            String unQualifiedDataTypeTerm = dataTypeTerm;
            DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm,
                    aElementBDT.getAttribute("id"), aElementBDT.getAttribute("id"), module);
            logger.debug("Inserting bdt primitive restriction for exceptional unqualified bdt");
            insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
        }
    }

    private void importExceptionalDataTypeList() throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String type = "simple";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(SRTConstants.FIELDS_XSD_FILE_PATH));

        //Type Name
        NodeList simpleTypeNodeList = fields_xsd.getNodeList("//xsd:simpleType");
        for (int k = 0; k < simpleTypeNodeList.getLength(); k++) {
            Node simpleTypeNode = simpleTypeNodeList.item(k);
            String dataType = ((Element) simpleTypeNode).getAttribute("name");
            if (dataType.endsWith("CodeContentType") || dataType.endsWith("IDContentType"))
                continue;

            boolean isIntheList = false;
            for (int i = 0; i < Types.dataTypeList.length; i++) {
                if (Types.dataTypeList[i].equals(dataType)) {
                    isIntheList = true;
                    break;
                }
            }
            if (isIntheList)
                continue;

            Node typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
            logger.debug("Importing " + dataType + " now in the exception ");
            Element typeNameElement = (Element) typeNameNode;
            typeName = typeNameElement.getAttribute("base");

            Node aNodeTN = fields_xsd.getNode("//xsd:" + type + "Type[@name = '" + dataType + "']");
            Element aElementTN = (Element) aNodeTN;

            //Data Type Term

            Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

            Element dataTypeTermElement = (Element) dataTypeTermNode;
            try {
                dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
                if (dataTypeTerm == "")
                    System.err.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType);
            } catch (Exception e) {
                System.err.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType + " Stacktrace:" + e.getMessage());
            }

            //Definitions
            Node definitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
            Element definitionElement = (Element) definitionNode;
            Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
            if (ccDefinitionNode == null)
                ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
            if (ccDefinitionNode == null)
                ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
            Element ccDefinitionElement = (Element) ccDefinitionNode;

            Node aNodeBDT = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']");
            Element aElementBDT = (Element) aNodeBDT;

            Node union = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union");
            int defaultId = -1;

            if (union != null) {
                defaultId = xbtRepository.findOneByName("token").getXbtId();
            } else {
                Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']//xsd:extension");
                if (xsdTypeNameNode == null)
                    xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']//xsd:restriction");
                if (xsdTypeNameNode != null) {
                    Element xsdTypeNameElement = (Element) xsdTypeNameNode;
                    xsdTypeName = xsdTypeNameElement.getAttribute("base");

                    XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsdTypeName);
                    if (xbt != null) {
                        defaultId = xbt.getXbtId();
                    } else {
                        Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                        defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
                    }
                }
            }

            DataType dVO1 = new DataType();

            if (check_BDT(aElementBDT.getAttribute("id"))) {
                logger.debug("Default BDT is already existing");
                dVO1 = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));
            } else {
                dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(),
                        (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"), module);
                logger.debug("Inserting bdt primitive restriction for exceptional default bdt");
                insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);
            }

            if (check_BDT(aElementTN.getAttribute("id")))
                logger.debug("Unqualified BDT is already existing");
            else {
                //Unqualified Type Name
                String unQualifiedTypeName = dataType;

                //Unqualified Data Type Term
                String unQualifiedDataTypeTerm = dataTypeTerm;
                DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm,
                        aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"), module);
                logger.debug("Inserting bdt primitive restriction for exceptional unqualified bdt");
                insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
            }
        }
    }

    private void insertBDTPrimitiveRestrictionForExceptionalBDT(int cdtID, int bdtID, int defaultId) throws Exception {
        List<CoreDataTypeAllowedPrimitive> al3 = cdtAwdPriRepository.findByCdtId(cdtID);
        
        DataType bdt = dataTypeRepository.findOne(bdtID);
        if(bdt.getBasedDtId()==cdtID){
	        boolean isTimePoint = false;
	        if(bdt.getDen().equals(Utility.typeToDen("DateType_DB95C8")) 		//DayDateType
	        || bdt.getDen().equals(Utility.typeToDen("DateType_0C267D"))		//MonthDateType
	        || bdt.getDen().equals(Utility.typeToDen("DateType_5B057B"))		//MonthDayDateType
	        || bdt.getDen().equals(Utility.typeToDen("DateType_57D5E1"))		//YearDateType
	        || bdt.getDen().equals(Utility.typeToDen("DateType_BBCC14"))){		//YearMonthDateType
	        	isTimePoint=true;
	        }
	        
	        for (CoreDataTypeAllowedPrimitive aCDTAllowedPrimitiveVO : al3) {
	
	            List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> al4 =
	                    cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId(aCDTAllowedPrimitiveVO.getCdtAwdPriId());
	
	            for (CoreDataTypeAllowedPrimitiveExpressionTypeMap aCDTAllowedPrimitiveExpressionTypeMapVO : al4) {
	                int idOfXsdToken = getXSDBuiltInTypeId("xsd:token");
	
	                if (defaultId == aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) { // default
	                    BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
	                    aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
	                    aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aCDTAllowedPrimitiveExpressionTypeMapVO.getCdtAwdPriXpsTypeMapId());
	                    aBDT_Primitive_RestrictionVO.setDefault(true);
	                    logger.debug("Inserting allowed primitive expression type map with XSD built-in type " +
	                            getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = true");
	                    bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);
	                } 
	                
	                if(isTimePoint && idOfXsdToken==aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()){
	                    BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
	                    aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
	                    aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aCDTAllowedPrimitiveExpressionTypeMapVO.getCdtAwdPriXpsTypeMapId());
	                    aBDT_Primitive_RestrictionVO.setDefault(false);
	                    logger.debug("Inserting allowed primitive expression type map with XSD built-in type " +
	                            getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = false");
	                    bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);
	                }
	                
	                //TODO: logic for default BDTs which don't have base type and only have union 
	            }
	        }
        }
        else {
        	List<BusinessDataTypePrimitiveRestriction> defaultBDTPri = bdtPriRestriRepository.findByBdtId(bdt.getBasedDtId());
        	for(int i=0; i<defaultBDTPri.size(); i++){
        		BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
        		aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
        		aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(defaultBDTPri.get(i).getCdtAwdPriXpsTypeMapId());
        		aBDT_Primitive_RestrictionVO.setDefault(defaultBDTPri.get(i).isDefault());
        		logger.debug("Inherit allowed primitive expression type map with XSD built-in type in unqualified BDT");
                bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);
        	}
        }
    }

    private void importCodeContentType() throws Exception {
        String dataType = "CodeContentType";

        String typeName;
        String xsdTypeName;
        String baseDataTypeTerm;
        String baseGUID;
        String id;
        int defaultId = -1;

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(SRTConstants.FIELDS_XSD_FILE_PATH));

        Node aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']");
        Element aElementTN = (Element) aNodeTN;
        id = aElementTN.getAttribute("id");
        aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        aElementTN = (Element) aNodeTN;
        typeName = aElementTN.getAttribute("base");

        String den = Utility.typeToDen(typeName);
        DataType dVO1 = dataTypeRepository.findByDen(den).get(0);
        baseDataTypeTerm = dVO1.getDataTypeTerm();
        baseGUID = dVO1.getGuid();


        //Unqualified Type Name
        String unQualifiedTypeName = dataType;

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = baseDataTypeTerm;

        Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']//xsd:restriction");
        if (xsdTypeNameNode != null) {
            Element xsdTypeNameElement = (Element) xsdTypeNameNode;
            xsdTypeName = xsdTypeNameElement.getAttribute("base");

            XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsdTypeName);
            if (xbt != null) {
                defaultId = xbt.getXbtId();
            } else {
                Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
            }
        }

        DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, id, baseGUID, module);
        insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
    }

    private void importIDContentType() throws Exception {

        String dataType = "IDContentType";

        String typeName;
        String xsdTypeName;
        String baseDataTypeTerm;
        String baseGUID;
        String id;
        int defaultId = -1;

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(SRTConstants.FIELDS_XSD_FILE_PATH));

        Node aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']");
        Element aElementTN = (Element) aNodeTN;
        id = aElementTN.getAttribute("id");
        aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        aElementTN = (Element) aNodeTN;
        typeName = aElementTN.getAttribute("base");

        String den = Utility.typeToDen(typeName);
        DataType dVO1 = dataTypeRepository.findByDen(den).get(0);
        baseDataTypeTerm = dVO1.getDataTypeTerm();
        baseGUID = dVO1.getGuid();


        //Unqualified Type Name
        String unQualifiedTypeName = dataType;

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = baseDataTypeTerm;

        Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']//xsd:extension");
        if (xsdTypeNameNode == null)
            xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']//xsd:restriction");
        if (xsdTypeNameNode != null) {
            Element xsdTypeNameElement = (Element) xsdTypeNameNode;
            xsdTypeName = xsdTypeNameElement.getAttribute("base");

            XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsdTypeName);
            if (xbt != null) {
                defaultId = xbt.getXbtId();
            } else {
                Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
            }
        }
        DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, id, baseGUID, module);
        insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
    }

    private int getXSDBuiltInTypeId(String xsd_buitintype) {
        XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsd_buitintype);
        return (xbt != null) ? xbt.getXbtId() : 0;
    }
}
