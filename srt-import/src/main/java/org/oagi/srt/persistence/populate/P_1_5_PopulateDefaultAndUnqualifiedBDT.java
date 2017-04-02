package org.oagi.srt.persistence.populate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.common.ImportConstants;
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
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static org.oagi.srt.common.ImportConstants.AGENCY_IDENTIFICATION_NAME;
import static org.oagi.srt.persistence.populate.DataImportScriptPrinter.printTitle;

/**
 * Created by tnk11 on 6/24/2016.
 */
@Component
public class P_1_5_PopulateDefaultAndUnqualifiedBDT {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private ImportUtil importUtil;

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            P_1_5_PopulateDefaultAndUnqualifiedBDT populateBDTsInDT = ctx.getBean(P_1_5_PopulateDefaultAndUnqualifiedBDT.class);
            populateBDTsInDT.run(ctx);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        logger.info("### 1.5 Start");
        printTitle("Populate default and unqualified BDTs");

        for (int i = 0; i < Types.dataTypeList.length; i++) {
            importDataTypeList(Types.dataTypeList[i]);
        }

        importExceptionalDataTypeList();
        importExceptionalDataTypeList2("CodeType_1E7368");
        importExceptionalDataTypeList2("ValueType_039C44");

        importCodeContentType();
        importIDContentType();

        logger.info("### 1.5 End");
    }

    private void importDataTypeList(String dataType) throws Exception {
        logger.debug("Importing " + dataType + " now");
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String type = "complex";

        XPathHandler fields_xsd = new XPathHandler(ImportConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(ImportConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(ImportConstants.FIELDS_XSD_FILE_PATH));

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
        if (dataTypeTermElement == null) {
            throw new IllegalStateException();
        }
        logger.debug("### " + dataTypeTermElement.getTextContent());
        try {
            dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
            if (dataTypeTerm == "")
                logger.error("Error getting the data type term for the unqualified BDT: " + dataType);
        } catch (Exception e) {
            logger.error("Error getting the data type term for the unqualified BDT: " + dataType, e);
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
        long defaultId = -1L;
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
        populateDTSCforDefaultBDT(dVO1, fields_xsd, businessDataType_xsd);

        //Unqualified Type Name
        String unQualifiedTypeName = dataType;

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = dataTypeTerm;
        DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm,
                aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"), module);
        logger.debug("Inserting bdt primitive restriction for unqualfieid bdt");
        insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
        populateDTSCforUnqualifiedBDT(dVO2, fields_xsd, businessDataType_xsd);
    }

    public DataType insertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition,
                                               String ccDefinition, String id, Module module) throws Exception {
        DataType basedDT = dataTypeRepository.findOneByDataTypeTermAndType(dataTypeTerm, 0);
        if (basedDT == null) {
            throw new IllegalStateException("Can't find based CDT by '" + dataTypeTerm + "'");
        }
        long basedDTID = basedDT.getDtId();
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
            dtVO.setState(CoreComponentState.Published);
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

    private void insertBDTPrimitiveRestriction(long cdtId, long bdtID, long defaultId) throws Exception {
        List<CoreDataTypeAllowedPrimitive> al3 = cdtAwdPriRepository.findByCdtId(cdtId);
        DataType bdt = dataTypeRepository.findOne(bdtID);

        if (bdt.getBasedDtId() == cdtId) {//if default BDT
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
                    bdtPriRestriRepository.saveAndFlush(aBDT_Primitive_RestrictionVO);
                }
            }
        } else {
            List<BusinessDataTypePrimitiveRestriction> defaultBDTPri = bdtPriRestriRepository.findByBdtId(bdt.getBasedDtId());
            for (int i = 0; i < defaultBDTPri.size(); i++) {
                BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
                aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(defaultBDTPri.get(i).getCdtAwdPriXpsTypeMapId());
                aBDT_Primitive_RestrictionVO.setDefault(defaultBDTPri.get(i).isDefault());
                logger.debug("Inherit allowed primitive expression type map with XSD built-in type in unqualified BDT");
                bdtPriRestriRepository.saveAndFlush(aBDT_Primitive_RestrictionVO);
            }
        }

    }

    private String getXsdBuiltinType(long id) {
        return xbtRepository.findOne(id).getBuiltInType();
    }

    public DataType insertUnqualified_BDTStatement(String typeName, String dataTypeTerm,
                                                   String id, String defaultGUID, Module module) throws Exception {
        long basedDTID = dataTypeRepository.findOneByGuid(defaultGUID).getDtId();

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
            dtVO.setState(CoreComponentState.Published);
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

        XPathHandler businessDataType_xsd = new XPathHandler(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(ImportConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH));

        typeName = dataType;
        String type = "simple";
        String expressionPrefix = "//xsd:" + type + "Type[@name = '" + typeName + "']";
        Node dataTypeTermNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

        Element dataTypeTermElement = (Element) dataTypeTermNode;
        dataTypeTerm = dataTypeTermElement.getTextContent();
        if (dataTypeTerm.length() > 5)
            if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
                dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);

        //Definitions
        Node definitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
        Element definitionElement = (Element) definitionNode;
        Node ccDefinitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        Element ccDefinitionElement = (Element) ccDefinitionNode;

        Node aNodeBDT = businessDataType_xsd.getNode(expressionPrefix);
        Element aElementBDT = (Element) aNodeBDT;

        Node union = businessDataType_xsd.getNode(expressionPrefix + "/xsd:union");
        long defaultId = -1L;
        if (union != null) {
            defaultId = xbtRepository.findOneByName("token").getXbtId();
        } else {
            Node xsdTypeNameNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:extension");
            if (xsdTypeNameNode == null)
                xsdTypeNameNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:restriction");
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
            populateDTSCforDefaultBDT(dVO1, null, businessDataType_xsd);
        }

//
//        if (check_BDT(aElementBDT.getAttribute("id")))
//            logger.debug("Unqualified BDT is already existing");
//        else {
//            //Unqualified Type Name
//            String unQualifiedTypeName = dataType;
//
//            //Unqualified Data Type Term
//            String unQualifiedDataTypeTerm = dataTypeTerm;
//            DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm,
//                    aElementBDT.getAttribute("id"), aElementBDT.getAttribute("id"), module);
//            logger.debug("Inserting bdt primitive restriction for exceptional unqualified bdt");
//            insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
//            populateDTSCforUnqualifiedBDT(dVO2, null, businessDataType_xsd);
//        }
    }

    private void importExceptionalDataTypeList() throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String type = "simple";

        XPathHandler fields_xsd = new XPathHandler(ImportConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(ImportConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(ImportConstants.FIELDS_XSD_FILE_PATH));

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
            /*
             * In Fields.xsd of OAGIS 10.2 have two exceptional cases, 'OperatorCodeType' and 'TestSampleHandlingCodeType'.
             * These elements should be ignored at this time and have 'xsd:enumeration' as a children.
             */
            NodeList nodeList = fields_xsd.getNodeList(typeNameNode, ".//xsd:enumeration");
            if (nodeList.getLength() > 0) {
                continue;
            }
            logger.debug("Importing " + dataType + " now in the exception ");
            Element typeNameElement = (Element) typeNameNode;
            typeName = typeNameElement.getAttribute("base");

            Node aNodeTN = fields_xsd.getNode("//xsd:" + type + "Type[@name = '" + dataType + "']");
            Element aElementTN = (Element) aNodeTN;

            // Data Type Term
            String expressionPrefix = "//xsd:" + type + "Type[@name = '" + typeName + "']";
            Node dataTypeTermNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

            Element dataTypeTermElement = (Element) dataTypeTermNode;
            try {
                dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
                if (StringUtils.isEmpty(dataTypeTerm))
                    logger.error("Error getting the data type term for the unqualified BDT in the exception: " + dataType);
            } catch (Exception e) {
                logger.error("Error getting the data type term for the unqualified BDT in the exception: " + dataType, e);
            }

            // Definitions
            Node definitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
            Element definitionElement = (Element) definitionNode;
            Node ccDefinitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
            if (ccDefinitionNode == null)
                ccDefinitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
            if (ccDefinitionNode == null)
                ccDefinitionNode = businessDataType_xsd.getNode(expressionPrefix + "/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
            Element ccDefinitionElement = (Element) ccDefinitionNode;

            Node aNodeBDT = businessDataType_xsd.getNode(expressionPrefix);
            if (aNodeBDT == null) {
                logger.error("Can't find node using the following XPATH expression in " +
                        ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH + ": \"" + expressionPrefix + "\"");
                continue;
            }
            Element aElementBDT = (Element) aNodeBDT;

            Node union = businessDataType_xsd.getNode(expressionPrefix + "/xsd:union");
            long defaultId = -1L;

            if (union != null) {
                defaultId = xbtRepository.findOneByName("token").getXbtId();
            } else {
                Node xsdTypeNameNode = businessDataType_xsd.getNode(expressionPrefix + "//xsd:extension");
                if (xsdTypeNameNode == null)
                    xsdTypeNameNode = businessDataType_xsd.getNode(expressionPrefix + "//xsd:restriction");
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

            DataType dVO1;

            if (check_BDT(aElementBDT.getAttribute("id"))) {
                logger.debug("Default BDT is already existing");
                dVO1 = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));
            } else {
                dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm,
                        (definitionElement != null) ? definitionElement.getTextContent() : null,
                        (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"), module);
                logger.debug("Inserting bdt primitive restriction for exceptional default bdt");
                insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);
                populateDTSCforDefaultBDT(dVO1, fields_xsd, businessDataType_xsd);
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
                populateDTSCforUnqualifiedBDT(dVO2, fields_xsd, businessDataType_xsd);
            }
        }
    }

    private void insertBDTPrimitiveRestrictionForExceptionalBDT(long cdtID, long bdtID, long defaultId) throws Exception {
        List<CoreDataTypeAllowedPrimitive> al3 = cdtAwdPriRepository.findByCdtId(cdtID);

        DataType bdt = dataTypeRepository.findOne(bdtID);
        if (bdt.getBasedDtId() == cdtID) {
            boolean isTimePoint = false;
            if (bdt.getDen().equals(Utility.typeToDen("DateType_DB95C8"))        //DayDateType
                    || bdt.getDen().equals(Utility.typeToDen("DateType_0C267D"))        //MonthDateType
                    || bdt.getDen().equals(Utility.typeToDen("DateType_5B057B"))        //MonthDayDateType
                    || bdt.getDen().equals(Utility.typeToDen("DateType_57D5E1"))        //YearDateType
                    || bdt.getDen().equals(Utility.typeToDen("DateType_BBCC14"))) {        //YearMonthDateType
                isTimePoint = true;
            }

            for (CoreDataTypeAllowedPrimitive aCDTAllowedPrimitiveVO : al3) {

                List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> al4 =
                        cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId(aCDTAllowedPrimitiveVO.getCdtAwdPriId());

                for (CoreDataTypeAllowedPrimitiveExpressionTypeMap aCDTAllowedPrimitiveExpressionTypeMapVO : al4) {
                    long idOfXsdToken = getXSDBuiltInTypeId("xsd:token");

                    if (defaultId == aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) { // default
                        BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                        aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
                        aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aCDTAllowedPrimitiveExpressionTypeMapVO.getCdtAwdPriXpsTypeMapId());
                        aBDT_Primitive_RestrictionVO.setDefault(true);
                        logger.debug("Inserting allowed primitive expression type map with XSD built-in type " +
                                getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = true");
                        bdtPriRestriRepository.saveAndFlush(aBDT_Primitive_RestrictionVO);
                    }

                    if (isTimePoint && idOfXsdToken == aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) {
                        BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                        aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
                        aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aCDTAllowedPrimitiveExpressionTypeMapVO.getCdtAwdPriXpsTypeMapId());
                        aBDT_Primitive_RestrictionVO.setDefault(false);
                        logger.debug("Inserting allowed primitive expression type map with XSD built-in type " +
                                getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = false");
                        bdtPriRestriRepository.saveAndFlush(aBDT_Primitive_RestrictionVO);
                    }

                    //TODO: logic for default BDTs which don't have base type and only have union
                }
            }
        } else {
            List<BusinessDataTypePrimitiveRestriction> defaultBDTPri = bdtPriRestriRepository.findByBdtId(bdt.getBasedDtId());
            for (int i = 0; i < defaultBDTPri.size(); i++) {
                BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
                aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(defaultBDTPri.get(i).getCdtAwdPriXpsTypeMapId());
                aBDT_Primitive_RestrictionVO.setDefault(defaultBDTPri.get(i).isDefault());
                logger.debug("Inherit allowed primitive expression type map with XSD built-in type in unqualified BDT");
                bdtPriRestriRepository.saveAndFlush(aBDT_Primitive_RestrictionVO);
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
        long defaultId = -1L;

        XPathHandler fields_xsd = new XPathHandler(ImportConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(ImportConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(ImportConstants.FIELDS_XSD_FILE_PATH));

        Node aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']");
        Element aElementTN = (Element) aNodeTN;
        id = aElementTN.getAttribute("id");
        aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        aElementTN = (Element) aNodeTN;
        typeName = aElementTN.getAttribute("base");

        String den = Utility.typeToDen(typeName);
        List<DataType> dataTypesByDen = dataTypeRepository.findByDen(den);
        if (dataTypesByDen.isEmpty()) {
            throw new IllegalStateException("Can't find data type by DEN: " + den);
        }
        DataType dVO1 = dataTypesByDen.get(0);
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
        populateDTSCforUnqualifiedBDT(dVO2, fields_xsd, businessDataType_xsd);
    }

    private void importIDContentType() throws Exception {

        String dataType = "IDContentType";

        String typeName;
        String xsdTypeName;
        String baseDataTypeTerm;
        String baseGUID;
        String id;
        long defaultId = -1L;

        XPathHandler fields_xsd = new XPathHandler(ImportConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(ImportConstants.XBT_FILE_PATH);

        Module module = moduleRepository.findByModule(Utility.extractModuleName(ImportConstants.FIELDS_XSD_FILE_PATH));

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
        populateDTSCforUnqualifiedBDT(dVO2, fields_xsd, businessDataType_xsd);
    }

    private void populateDTSCforDefaultBDT(DataType dt, XPathHandler xh, XPathHandler xh2) throws Exception {

        DataType baseCDT = dataTypeRepository.findOne(dt.getBasedDtId());
        logger.debug("Popuating SCs for default BDT with type = " + Utility.denToTypeName(dt.getDen()));
        List<DataTypeSupplementaryComponent> cdtSCList = dtScRepository.findByOwnerDtId(baseCDT.getDtId());

        NodeList attributeNodeList = xh2.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
        if (attributeNodeList == null || attributeNodeList.getLength() < 1 && xh != null) {
            attributeNodeList = xh.getNodeList("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
        }
        for (int i = 0; i < attributeNodeList.getLength(); i++) {
            //New or Re-defined attribute
            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            int min_cardinality = -1;
            int max_cardinality = -1;
            Element attrElement = (Element) attributeNodeList.item(i);
            String attribute_name = attrElement.getAttribute("name");
            String attrubute_type = attrElement.getAttribute("type");
            String attribute_id = attrElement.getAttribute("id");

            vo.setGuid(attribute_id);
            vo.setOwnerDtId(dt.getDtId());

            if (attrElement.getAttribute("use") == null) {
                min_cardinality = 0;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("optional")) {
                min_cardinality = 0;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("required")) {
                min_cardinality = 1;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited")) {
                min_cardinality = 0;
                max_cardinality = 0;
            }

            vo.setCardinalityMin(min_cardinality);
            vo.setCardinalityMax(max_cardinality);

            int baseInd = -1;
            for (int j = 0; j < cdtSCList.size(); j++) {

                DataTypeSupplementaryComponent baseCDTSC = cdtSCList.get(j);
                String basePropertyTerm = baseCDTSC.getPropertyTerm();
                String baseRepresentationTerm = baseCDTSC.getRepresentationTerm();
                String baseStr = basePropertyTerm + " " + baseRepresentationTerm;
                String thisStr = Utility.spaceSeparator(attribute_name);

                logger.debug(baseStr + " vs " + thisStr);
                if (baseStr.equals(thisStr)) {
                    baseInd = j;
                    vo.setPropertyTerm(baseCDTSC.getPropertyTerm());
                    vo.setRepresentationTerm(baseCDTSC.getRepresentationTerm());
                    vo.setDefinition(baseCDTSC.getDefinition());
                    vo.setBasedDtScId(baseCDTSC.getDtScId());
                    break;
                }
            }
            if (baseInd > -1) {
                cdtSCList.remove(baseInd);
                logger.debug("~~~ " + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This sc has corresponding base!");
                dtScRepository.saveAndFlush(vo);
                populateBDTSCPrimitiveRestrictionWithAttribute(attrElement, vo);
            } else {
                String propertyTerm = "";
                String representationTerm = "";
                String definition = "";

                propertyTerm = Utility.spaceSeparator(attribute_name);
                representationTerm = Utility.getRepresentationTerm(attribute_name);

                Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation/ccts_Definition");
                if (defNode == null) {
                    defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation");
                }
                if (defNode == null) {
                    defNode = xh.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation/ccts_Definition");
                }
                if (defNode != null) {
                    definition = defNode.getTextContent();
                }

                vo.setPropertyTerm(propertyTerm);
                vo.setRepresentationTerm(representationTerm);
                vo.setDefinition(definition);
                logger.debug("~~~ " + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by default BDT is new from Attribute!");
                dtScRepository.saveAndFlush(vo);
                populateBDTSCPrimitiveRestrictionWithAttribute(attrElement, vo);
            }


        }

        //Inherit From the remain SCs based on CDT because they don't have corresponding attributes
        //Just copy and get the values from remain cdtSCList
        for (int i = 0; i < cdtSCList.size(); i++) {
            DataTypeSupplementaryComponent baseCDTSC = cdtSCList.get(i);
            DataTypeSupplementaryComponent dtSc = new DataTypeSupplementaryComponent();

            dtSc.setOwnerDtId(dt.getDtId());
            dtSc.setGuid(Utility.generateGUID());
            dtSc.setPropertyTerm(baseCDTSC.getPropertyTerm());
            dtSc.setRepresentationTerm(baseCDTSC.getRepresentationTerm());
            dtSc.setDefinition(baseCDTSC.getDefinition());

            //we already know it doesn't have attributes
            //so according to design doc,
            //min_cardinality = 0, max_cardinality = 0
            dtSc.setCardinalityMin(0);
            dtSc.setCardinalityMax(0);
            dtSc.setBasedDtScId(baseCDTSC.getDtScId());
            logger.debug("~~~ " + baseCDTSC.getPropertyTerm() + " " + baseCDTSC.getRepresentationTerm() + ". This SC owned by default BDT is inherited from Base!");
            dtScRepository.saveAndFlush(dtSc);
            inheritCDTSCAllowedPrimitiveRestriction(dtSc);
        }
    }

    public void populateDTSCforUnqualifiedBDT(DataType dt, XPathHandler xh, XPathHandler xh2) throws Exception {

        //inheritance
        String denType = Utility.denToTypeName(dt.getDen());
        logger.debug("Popuating SCs for unqualified bdt with type = " + denType);
        Node extensionNode = xh2.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension");
        if (extensionNode == null) {
            extensionNode = xh2.getNode("//xsd:simpleType[@name = '" + denType + "']/xsd:restriction");
        }
        if (extensionNode == null) {
            extensionNode = xh.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension");
        }
        if (extensionNode == null) {
            extensionNode = xh.getNode("//xsd:simpleType[@name = '" + denType + "']/xsd:restriction");
        }

        String base = ((Element) extensionNode).getAttribute("base");
        Node baseNode = xh2.getNode("//xsd:complexType[@name = '" + base + "']");
        if (baseNode == null)
            baseNode = xh.getNode("//xsd:complexType[@name = '" + base + "']");
        if (baseNode == null)
            baseNode = xh2.getNode("//xsd:simpleType[@name = '" + base + "']");
        if (baseNode == null)
            baseNode = xh.getNode("//xsd:simpleType[@name = '" + base + "']");

        DataType basedDt = dataTypeRepository.findOneByGuid(((Element) baseNode).getAttribute("id"));
        long based_dt_id = basedDt.getDtId();
        List<DataTypeSupplementaryComponent> baseDefaultDTSCs = dtScRepository.findByOwnerDtId(based_dt_id);

        //adding additional SCs for attributes
        NodeList attributeList = xh2.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
        if (attributeList == null || attributeList.getLength() < 1 && xh != null) {
            attributeList = xh.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
        }
        int min_cardinality = 0;
        int max_cardinality = 1;

        for (int i = 0; i < attributeList.getLength(); i++) {
            Element attrElement = (Element) attributeList.item(i);
            String attribute_name = attrElement.getAttribute("name");
            String attribute_id = attrElement.getAttribute("id");

            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            vo.setOwnerDtId(dt.getDtId());
            vo.setGuid(attribute_id);
            if (attrElement.getAttribute("use") == null) {
                min_cardinality = 0;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("optional")) {
                min_cardinality = 0;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("required")) {
                min_cardinality = 1;
                max_cardinality = 1;
            } else if (attrElement.getAttribute("use").equalsIgnoreCase("prohibited")) {
                min_cardinality = 0;
                max_cardinality = 0;
            }

            vo.setCardinalityMin(min_cardinality);
            vo.setCardinalityMax(max_cardinality);

            int baseInd = -1;

            for (int j = 0; j < baseDefaultDTSCs.size(); j++) {
                DataTypeSupplementaryComponent baseDefaultBDTSC = baseDefaultDTSCs.get(j);
                String basePropertyTerm = baseDefaultBDTSC.getPropertyTerm();
                String baseRepresentationTerm = baseDefaultBDTSC.getRepresentationTerm();
                String baseStr = basePropertyTerm + " " + baseRepresentationTerm;
                String thisStr = Utility.spaceSeparator(attribute_name);

                logger.debug(baseStr + " vs " + thisStr);
                if (baseStr.equals(thisStr)) {
                    baseInd = j;
                    vo.setPropertyTerm(baseDefaultBDTSC.getPropertyTerm());
                    vo.setRepresentationTerm(baseDefaultBDTSC.getRepresentationTerm());
                    vo.setDefinition(baseDefaultBDTSC.getDefinition());
                    vo.setBasedDtScId(baseDefaultBDTSC.getDtScId());
                    break;
                }
            }

            if (baseInd > -1) {
                baseDefaultDTSCs.remove(baseInd);
                logger.debug("~~~" + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by unqualified BDT has corresponding base!");
                dtScRepository.saveAndFlush(vo);
                populateBDTSCPrimitiveRestrictionWithAttribute(attrElement, vo);
            } else {

                String propertyTerm = "";
                String representationTerm = "";
                String definition = "";

                propertyTerm = Utility.spaceSeparator(attribute_name);

                representationTerm = Utility.getRepresentationTerm(attribute_name);

                Node defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
                if (defNode == null) {
                    defNode = xh2.getNode("//xsd:complexType[@name = '" + Utility.denToTypeName(dt.getDen()) + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@id='" + attribute_id + "']/xsd:annotation/xsd:documentation");
                }
                if (defNode != null) {
                    definition = defNode.getTextContent();
                }

                vo.setPropertyTerm(propertyTerm);
                vo.setRepresentationTerm(representationTerm);
                vo.setDefinition(definition);
                logger.debug("~~~" + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by unqualified BDT is new from Attribute!");

                dtScRepository.saveAndFlush(vo);


                //if it has new attribute extension,
                //it needs to have records in cdt_sc_awd_pri and cdt_sc_awd_pri_xps_type_map
                DataTypeSupplementaryComponent insertedSC = dtScRepository.findOneByGuid(vo.getGuid());

                List<DataType> DataTypewDataTypeTerm = dataTypeRepository.findByDataTypeTerm(vo.getRepresentationTerm());

                for (int j = 0; j < DataTypewDataTypeTerm.size(); j++) {
                    DataType aDataType = DataTypewDataTypeTerm.get(j);
                    List<CoreDataTypeAllowedPrimitive> CDTAwdPris = cdtAwdPriRepository.findByCdtId(aDataType.getDtId());

                    if (CDTAwdPris.size() > 0 && !CDTAwdPris.isEmpty()) {
                        for (int k = 0; k < CDTAwdPris.size(); k++) {
                            CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAP = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                            CoreDataTypeAllowedPrimitive cdtAP = CDTAwdPris.get(k);
                            cdtSCAP.setCdtScId(insertedSC.getDtScId());
                            cdtSCAP.setCdtPriId(cdtAP.getCdtPriId());
                            CoreDataTypePrimitive tmpPri = cdtPriRepository.findOne(cdtAP.getCdtPriId());

                            cdtSCAP.setDefault(cdtAP.isDefault());

                            if (cdtSCAP.isDefault()) {
                                logger.debug(" and it's Default!");
                            } else {
                                logger.debug("");
                            }

                            cdtScAwdPriRepository.saveAndFlush(cdtSCAP);

                            CoreDataTypeSupplementaryComponentAllowedPrimitive insertedCDTSCAP =
                                    cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(cdtSCAP.getCdtScId(), cdtSCAP.getCdtPriId());

                            List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAPXTMs =
                                    cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId(cdtAP.getCdtAwdPriId());
                            for (int m = 0; m < cdtAPXTMs.size(); m++) {
                                CoreDataTypeAllowedPrimitiveExpressionTypeMap thisAPXTmap = cdtAPXTMs.get(m);
                                CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap tmp = new CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap();
                                tmp.setXbtId(thisAPXTmap.getXbtId());
                                tmp.setCdtScAwdPriId(insertedCDTSCAP.getCdtScAwdPriId());
                                cdtScAwdPriXpsTypeMapRepository.saveAndFlush(tmp);
                            }
                        }
                        break;//if this is hit, that means dt sc is mapped to cdt sc
                    }
                }
                populateBDTSCPrimitiveRestrictionWithAttribute(attrElement, vo);
            }
        }

        //Inherit From the remain SCs based on default bdt because they don't have corresponding attributes
        //Just copy and get the values from remain baseDefaultDTSCs
        for (int i = 0; i < baseDefaultDTSCs.size(); i++) {
            DataTypeSupplementaryComponent baseDefaultBDTSC = (DataTypeSupplementaryComponent) baseDefaultDTSCs.get(i);
            DataTypeSupplementaryComponent vo = new DataTypeSupplementaryComponent();
            vo.setOwnerDtId(dt.getDtId());
            vo.setGuid(Utility.generateGUID());
            vo.setPropertyTerm(baseDefaultBDTSC.getPropertyTerm());
            vo.setRepresentationTerm(baseDefaultBDTSC.getRepresentationTerm());
            vo.setDefinition(baseDefaultBDTSC.getDefinition());

            //we already know it doesn't have attributes
            //so according to design doc,
            //inherit the values of default BDT sc's min_cardinality, max_cardinality
            vo.setCardinalityMin(baseDefaultBDTSC.getCardinalityMin());
            vo.setCardinalityMax(baseDefaultBDTSC.getCardinalityMax());
            vo.setBasedDtScId(baseDefaultBDTSC.getDtScId());
            logger.debug("~~~" + vo.getPropertyTerm() + " " + vo.getRepresentationTerm() + ". This SC owned by unqualified BDT is inherited from Base!");
            dtScRepository.saveAndFlush(vo);
            inheritBDTSCPrimitiveRestriction(vo, xh, xh2);
        }
    }

    public void populateBDTSCPrimitiveRestrictionWithAttribute(DataTypeSupplementaryComponent dtSc, String type) throws Exception {
        // Ensure whether the type is primitive or not.
        type = findPrimitiveTypeName(type);

        CodeList isCodeList = codeListRepository.findOneByName(type.replace("CodeContentType", "Code"));
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestiList = new ArrayList();

        if (isCodeList != null) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtSc.getDtScId());
            bdtSCPri.setDefault(false);
            bdtSCPri.setCodeListId(isCodeList.getCodeListId());
            bdtSCPri = bdtScPriRestriRepository.saveAndFlush(bdtSCPri);
            bdtScPriRestiList.add(bdtSCPri);

            bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtSc.getDtScId());
            long mapId = getCDTSCAllowedExpressionTypeMapByCDTPrimitiveAndBuiltInTypeAndCDTId("Token", "xsd:token", dtSc);
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(mapId);
            bdtSCPri.setDefault(true);
            bdtSCPri = bdtScPriRestriRepository.saveAndFlush(bdtSCPri);
            bdtScPriRestiList.add(bdtSCPri);

        } else if (type.equals(AGENCY_IDENTIFICATION_NAME + "ContentType")) {
            AgencyIdList agencyIdList = agencyIdListRepository.findOneByName(AGENCY_IDENTIFICATION_NAME);

            BusinessDataTypeSupplementaryComponentPrimitiveRestriction bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtSc.getDtScId());
            bdtSCPri.setDefault(false);
            bdtSCPri.setAgencyIdListId(agencyIdList.getAgencyIdListId());
            bdtSCPri = bdtScPriRestriRepository.saveAndFlush(bdtSCPri);
            bdtScPriRestiList.add(bdtSCPri);

            bdtSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
            bdtSCPri.setBdtScId(dtSc.getDtScId());
            long mapId = getCDTSCAllowedExpressionTypeMapByCDTPrimitiveAndBuiltInTypeAndCDTId("Token", "xsd:token", dtSc);
            bdtSCPri.setCdtScAwdPriXpsTypeMapId(mapId);
            bdtSCPri.setDefault(true);
            bdtSCPri = bdtScPriRestriRepository.saveAndFlush(bdtSCPri);
            bdtScPriRestiList.add(bdtSCPri);
        } else {
            List<CoreDataTypeSupplementaryComponentAllowedPrimitive> al3 = cdtScAwdPriRepository.findByCdtScId(getCDTSCAncestor(dtSc));

            for (CoreDataTypeSupplementaryComponentAllowedPrimitive aCDTSCAllowedPrimitiveVO : al3) {//Loop retrieved cdt_sc_awd_pri\
                List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> al4 =
                        cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriId(aCDTSCAllowedPrimitiveVO.getCdtScAwdPriId());
                for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap aCDTSCAllowedPrimitiveExVO : al4) {
                    BusinessDataTypeSupplementaryComponentPrimitiveRestriction bVO = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                    bVO.setBdtScId(dtSc.getDtScId());

                    bVO.setCdtScAwdPriXpsTypeMapId(aCDTSCAllowedPrimitiveExVO.getCdtScAwdPriXpsTypeMapId());
                    XSDBuiltInType xbtVO = xbtRepository.findOne(aCDTSCAllowedPrimitiveExVO.getXbtId());

                    if (type.equals(xbtVO.getBuiltInType())) {
                        bVO.setDefault(true);
                    } else {
                        bVO.setDefault(false);
                    }

                    bVO = bdtScPriRestriRepository.saveAndFlush(bVO);
                    bdtScPriRestiList.add(bVO);
                }
            }

            if (bdtScPriRestiList.stream().mapToInt(e -> e.isDefault() ? 1 : 0).sum() != 1) {
                throw new IllegalStateException("BDT_SC_ID['" + dtSc.getDtScId() + "'] has incorrect 'is_default' value in BDT_SC_PRI_RESTRI.");
            }
        }
    }

    public String findPrimitiveTypeName(String type) throws Exception {
        while (xbtRepository.findOneByBuiltInType(type) == null) {
            if (codeListRepository.findOneByName(type.replace("CodeContentType", "Code")) != null) {
                break;
            }
            if (type.equals(AGENCY_IDENTIFICATION_NAME + "ContentType")) {
                break;
            }

            XPathHandler businessDataType_xsd = new XPathHandler(ImportConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);

            // Type Name
            Node typeNameNode = businessDataType_xsd.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent/xsd:extension");
            if (typeNameNode == null) {
                typeNameNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + type + "']/xsd:restriction");
            }

            if (typeNameNode == null) {
                XPathHandler fields_xsd = new XPathHandler(ImportConstants.FIELDS_XSD_FILE_PATH);
                typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent/xsd:extension");
                if (typeNameNode == null) {
                    typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + type + "']/xsd:restriction");
                }
            }

            if (typeNameNode == null) {
                throw new IllegalStateException("Can't find type['" + type + "'].");
            }

            Element typeNameElement = (Element) typeNameNode;
            String baseType = typeNameElement.getAttribute("base");
            if (baseType == null) {
                throw new IllegalStateException("Can't find primitive type from type name['" + type + "'].");
            }

            type = baseType;
        }

        return type;
    }

    public void populateBDTSCPrimitiveRestrictionWithAttribute(
            Element attrElement, DataTypeSupplementaryComponent dtSc) throws Exception {
        String type = attrElement.getAttribute("type");
        populateBDTSCPrimitiveRestrictionWithAttribute(dtSc, type);
    }

    public void inheritCDTSCAllowedPrimitiveRestriction(DataTypeSupplementaryComponent dtSc) {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> baseCDTSCPriList =
                cdtScAwdPriRepository.findByCdtScId(dtSc.getBasedDtScId());
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestiList = new ArrayList();

        boolean isDefaultCDTPri;
        for (int i = 0; i < baseCDTSCPriList.size(); i++) {
            if (baseCDTSCPriList.get(i).isDefault()) {
                isDefaultCDTPri = true;
            } else {
                isDefaultCDTPri = false;
            }

            List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> cdtScAwdPriXpsTypeMapList =
                    cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPriId(baseCDTSCPriList.get(i).getCdtScAwdPriId());

            for (int j = 0; j < cdtScAwdPriXpsTypeMapList.size(); j++) {
                BusinessDataTypeSupplementaryComponentPrimitiveRestriction inputBDTSCPri = new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();
                inputBDTSCPri.setBdtScId(dtSc.getDtScId());
                inputBDTSCPri.setCdtScAwdPriXpsTypeMapId(cdtScAwdPriXpsTypeMapList.get(j).getCdtScAwdPriXpsTypeMapId());
                inputBDTSCPri.setCodeListId(0);
                inputBDTSCPri.setAgencyIdListId(0);
                if (isDefaultCDTPri && j == 0) {
                    inputBDTSCPri.setDefault(true);
                } else {
                    inputBDTSCPri.setDefault(false);
                }
                inputBDTSCPri = bdtScPriRestriRepository.saveAndFlush(inputBDTSCPri);
                bdtScPriRestiList.add(inputBDTSCPri);
            }
        }

        if (bdtScPriRestiList.stream().mapToInt(e -> e.isDefault() ? 1 : 0).sum() != 1) {
            throw new IllegalStateException("BDT_SC_ID['" + dtSc.getDtScId() + "'] has incorrect 'is_default' value in BDT_SC_PRI_RESTRI.");
        }
    }

    public void inheritBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtSc, XPathHandler xh, XPathHandler xh2) {
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> baseBDTSCPriList =
                bdtScPriRestriRepository.findByBdtScId(dtSc.getBasedDtScId());
        List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtScPriRestiList = new ArrayList();

        for (int i = 0; i < baseBDTSCPriList.size(); i++) {
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction baseBDTSCPri = baseBDTSCPriList.get(i);
            BusinessDataTypeSupplementaryComponentPrimitiveRestriction inputBDTSCPri =
                    new BusinessDataTypeSupplementaryComponentPrimitiveRestriction();

            inputBDTSCPri.setBdtScId(dtSc.getDtScId());
            inputBDTSCPri.setDefault(baseBDTSCPri.isDefault());
            inputBDTSCPri.setAgencyIdListId(baseBDTSCPri.getAgencyIdListId());
            inputBDTSCPri.setCodeListId(baseBDTSCPri.getCodeListId());
            inputBDTSCPri.setCdtScAwdPriXpsTypeMapId(baseBDTSCPri.getCdtScAwdPriXpsTypeMapId());
            inputBDTSCPri = bdtScPriRestriRepository.saveAndFlush(inputBDTSCPri);
            bdtScPriRestiList.add(inputBDTSCPri);
        }

        if (bdtScPriRestiList.stream().mapToInt(e -> e.isDefault() ? 1 : 0).sum() != 1) {
            throw new IllegalStateException("BDT_SC_ID['" + dtSc.getDtScId() + "'] has incorrect 'is_default' value in BDT_SC_PRI_RESTRI.");
        }
    }


    public long getCDTSCAllowedExpressionTypeMapByCDTPrimitiveAndBuiltInTypeAndCDTId(
            String cdtPrimitiveName, String builtInType, DataTypeSupplementaryComponent dtsc) {

        CoreDataTypePrimitive cdtPri = cdtPriRepository.findOneByName(cdtPrimitiveName);
        long cdtSCId = getCDTSCAncestor(dtsc);
        XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(builtInType);
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAwdPri =
                cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(cdtSCId, cdtPri.getCdtPriId());
        CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap map =
                cdtScAwdPriXpsTypeMapRepository.findOneByCdtScAwdPriIdAndXbtId(cdtSCAwdPri.getCdtScAwdPriId(), xbt.getXbtId());

        return map.getCdtScAwdPriXpsTypeMapId();
    }

    public long getCDTSCAncestor(DataTypeSupplementaryComponent dtsc) {
        if (dtsc.getBasedDtScId() > 0L) {
            DataTypeSupplementaryComponent baseDTSC = dtScRepository.findOne(dtsc.getBasedDtScId());
            return getCDTSCAncestor(baseDTSC);
        }
        return dtsc.getDtScId();
    }

    private long getXSDBuiltInTypeId(String xsd_buitintype) {
        XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsd_buitintype);
        return (xbt != null) ? xbt.getXbtId() : 0L;
    }

}