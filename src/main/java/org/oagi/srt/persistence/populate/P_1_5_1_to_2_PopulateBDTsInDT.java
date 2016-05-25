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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nasif Sikder
 * @author Jaehun Lee
 * @author Yunsu Lee
 * @version 1.0
 */
@Component
public class P_1_5_1_to_2_PopulateBDTsInDT {

    @Autowired
    private RepositoryFactory repositoryFactory;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    public static void main(String[] args) throws Exception {
        try (AbstractApplicationContext ctx = (AbstractApplicationContext)
                SpringApplication.run(Application.class, args);) {
            P_1_5_1_to_2_PopulateBDTsInDT populateBDTsInDT = ctx.getBean(P_1_5_1_to_2_PopulateBDTsInDT.class);
            populateBDTsInDT.run(ctx);
            //populateBDTsInDT.validate();
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### 1.5.1-2 Start");

        int userId = getUserID("oagis");

        XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
        XPathHandler components_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);
        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);

        File f = new File(SRTConstants.NOUNS_FILE_PATH);
        File[] listOfFiles = f.listFiles();

        for (int i = 0; i < Types.dataTypeList.length; i++) {
            importDataTypeList(Types.dataTypeList[i]);
        }

        importExceptionalDataTypeList();
        importExceptionalDataTypeList2("ValueType_039C44");

        System.out.println("### 1.5.1-2 End");
    }

    private int getUserID(String userName) {
        return userRepository.findOneByLoginId(userName).getAppUserId();
    }

    private void importDataTypeList(String dataType) throws Exception {
        System.out.println("Importing " + dataType + " now");
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String type = "complex";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        //Type Name
        Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension");
        if (typeNameNode == null) {
            type = "simple";
            typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        }
        Element typeNameElement = (Element) typeNameNode;
        typeName = typeNameElement.getAttribute("base");
        System.out.println("!! typeName = " + typeName);
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
        System.out.println("### " + dataTypeTermElement.getTextContent());
        try {
            dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
            if (dataTypeTerm == "")
                System.out.println("Error getting the data type term for the unqualified BDT: " + dataType);
        } catch (Exception e) {
            System.out.println("Error getting the data type term for the unqualified BDT: " + dataType + " Stacktrace:" + e.getMessage());
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
            System.out.println("Error getting the default BDT primitive restriction for the default BDT: " + typeName);
        System.out.println("data type term = " + dataTypeTerm);
        DataType dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definition, ccDefinition, aElementBDT.getAttribute("id"));
        System.out.println("Inserting bdt primitive restriction for default bdt");
        insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);

        //Unqualified Type Name
        String unQualifiedTypeName = dataType;

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = dataTypeTerm;
        DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
        System.out.println("Inserting bdt primitive restriction for unqualfieid bdt");
        insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
    }

    public DataType insertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition, String ccDefinition, String id) throws Exception {
        int basedDTID = dataTypeRepository.findOneByDataTypeTermAndType(dataTypeTerm, 0).getDtId();
        DataType dtVO = dataTypeRepository.findOneByGuid(id);
        if (dtVO == null) {
            System.out.println("Inserting default bdt whose name is " + typeName);

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

            int userId = getUserID("oagis");

            dtVO.setCreatedBy(userId);
            dtVO.setLastUpdatedBy(userId);
            dtVO.setOwnerUserId(userId);
            dtVO.setRevisionDoc(null);
            dtVO.setRevisionNum(0);
            dtVO.setRevisionTrackingNum(0);
            dtVO.setDeprecated(false);

            dtVO = dataTypeRepository.saveAndFlush(dtVO);
        }

        return dtVO;
    }

    private void insertBDTPrimitiveRestriction(int cdtId, int bdtID, int defaultId) throws Exception {
        BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository =
                repositoryFactory.businessDataTypePrimitiveRestrictionRepository();
        CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository =
                repositoryFactory.coreDataTypeAllowedPrimitiveRepository();
        CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository =
                repositoryFactory.coreDataTypeAllowedPrimitiveExpressionTypeMapRepository();

        List<CoreDataTypeAllowedPrimitive> al3 = cdtAwdPriRepository.findByCdtId(cdtId);

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
                System.out.println("Inserting allowed primitive expression type map with XSD built-in type " +
                        getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = " + isDefault);
                bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);
            }
        }
    }

    private String getXsdBuiltinType(int id) {
        return xbtRepository.findOne(id).getBuiltInType();
    }

    public DataType insertUnqualified_BDTStatement(String typeName, String dataTypeTerm, String id, String defaultGUID) throws Exception {
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

            int userId = getUserID("oagis");

            dtVO.setCreatedBy(userId);
            dtVO.setLastUpdatedBy(userId);
            dtVO.setOwnerUserId(userId);
            dtVO.setRevisionDoc(null);
            dtVO.setRevisionNum(0);
            dtVO.setRevisionTrackingNum(0);
            dtVO.setDeprecated(false);
            dtVO = dataTypeRepository.saveAndFlush(dtVO);
        }

        return dtVO;
    }

    public void validate() throws Exception {
        System.out.println("### 1.5.1-2 Start Validation");

        int userId = getUserID("oagis");

        XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
        XPathHandler components_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);
        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);

        File f = new File(SRTConstants.NOUNS_FILE_PATH);
        File[] listOfFiles = f.listFiles();

        for (int i = 0; i < Types.dataTypeList.length; i++) {
            validateImportDataTypeList(Types.dataTypeList[i]);
        }

        validateImportExceptionalDataTypeList();

        System.out.println("### 1.5.1-2 Validation End");
    }

    public boolean check_BDT(String id) {
        return (dataTypeRepository.findOneByGuid(id) == null) ? false : true;
    }

    public void populateAdditionalDefault_BDTStatement(XPathHandler filename) throws Exception {
        NodeList xsd_node = filename.getNodeList("//xsd:attribute");
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
        for (int i = 0; i < xsd_node.getLength(); i++) {
            Element tmp = (Element) xsd_node.item(i);
            String typeName = tmp.getAttribute("type");
            System.out.println(typeName);
            String den = Utility.typeToDen(tmp.getAttribute("type"));

            if (dataTypeRepository.findOneByDen(den) == null) { // && duplicate_check == false) {
                XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
                String type = "complex";
                String xsdTypeName = typeName;
                String dataTypeTerm;

                Node aNodeBDT = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']");
                if (aNodeBDT == null) {
                    type = "simple";
                    aNodeBDT = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']");
                }
                Element aElementBDT = (Element) aNodeBDT;

                if (aElementBDT != null) {
                    //Data Type Term
                    Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

                    Element dataTypeTermElement = (Element) dataTypeTermNode;
                    if (dataTypeTermElement != null) {
                        dataTypeTerm = dataTypeTermElement.getTextContent();
                        if (dataTypeTerm.length() > 5)
                            if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
                                dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
                        //dataTypeTerm = dataTypeTerm.replaceAll(" Object", "");
                    } else {
                        dataTypeTerm = typeName.substring(0, typeName.indexOf("Type"));
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
                        System.out.println("Error getting the default BDT primitive restriction for the default BDT: " + typeName);

                    //typeName = typeName.replaceAll("Type", "");

                    typeName = tmp.getAttribute("type"); //here

                    DataType dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, (definitionElement != null) ? definitionElement.getTextContent() : null, (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));

                    insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId); // here --> modify
                }
            }
        }
    }


    private void validateImportDataTypeList(String dataType) throws Exception {
        //System.out.println("Validating Import "+dataType+" now");
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String type = "complex";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        String defaultFromXSD = "";
        String defaultFromDB = "";

        String unqualifiedFromXSD = "";
        String unqualifiedFromDB = "";

        //Type Name
        Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension");
        if (typeNameNode == null) {
            type = "simple";
            typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        }
        Element typeNameElement = (Element) typeNameNode;
        typeName = typeNameElement.getAttribute("base");
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
        try {
            dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
            if (dataTypeTerm == "")
                System.out.println("Error getting the data type term for the unqualified BDT: " + dataType);
        } catch (Exception e) {
            System.out.println("Error getting the data type term for the unqualified BDT: " + dataType + " Stacktrace:" + e.getMessage());
        }

        Node aNodeBDT = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']");
        Element aElementBDT = (Element) aNodeBDT;

        defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("id");//guid
        defaultFromXSD = defaultFromXSD + "1";//type
        defaultFromXSD = defaultFromXSD + "1.0";//versionNum
        defaultFromXSD = defaultFromXSD + "0";//PreviousVersionDT_ID 0 means null
        defaultFromXSD = defaultFromXSD + dataTypeTerm;//data type term
        defaultFromXSD = defaultFromXSD + "null";//qualifier
        String baseCDTDen = "";
        baseCDTDen = Utility.denWithoutUUID(typeName);
        baseCDTDen = baseCDTDen.replace(". Type", "");

        defaultFromXSD = defaultFromXSD + baseCDTDen;//base cdt den instead of base dt id
        defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("name");//type name instead of den

        Node definitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
        Element definitionElement = (Element) definitionNode;
        Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        Element ccDefinitionElement = (Element) ccDefinitionNode;

        String definition = "";
        if (definitionNode != null) {
            definition = definitionNode.getTextContent();
        }
        if (definition.equals("")) {
            definition = "null";
        }

        String ccdefinition = "";
        if (ccDefinitionNode != null) {
            ccdefinition = ccDefinitionNode.getTextContent();
        }
        if (ccdefinition.equals("")) {
            ccdefinition = "null";
        }

        defaultFromXSD = defaultFromXSD + definition;//definition
        defaultFromXSD = defaultFromXSD + ccdefinition;//content component definition
        defaultFromXSD = defaultFromXSD + "null";//revision_doc
        defaultFromXSD = defaultFromXSD + "3";//state

        defaultFromXSD = defaultFromXSD + "0";//revisionNum
        defaultFromXSD = defaultFromXSD + "0";//revisionTrackingNum
        defaultFromXSD = defaultFromXSD + "0";//revisionAction   0 means null
        defaultFromXSD = defaultFromXSD + "0";//releaseID   0 means null
        defaultFromXSD = defaultFromXSD + "0";//currentBDTID   0 means null
        defaultFromXSD = defaultFromXSD + "false";//is_deprecated

        defaultFromXSD = defaultFromXSD.replace("ID", "Identifier");

        Node union = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:union");
        boolean unionExist = false;
        if (union != null) {
            unionExist = true;
        }

        Node base = businessDataType_xsd.getNode("//xsd:complexType[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/@base");
        if (base == null) {
            base = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']/xsd:restriction/@base");
        }
        String baseName = "";
        if (base != null) {
            baseName = base.getTextContent();
        }

        DataType dtvoFromDB;
        try {
            dtvoFromDB = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));

            defaultFromDB = "";
            defaultFromDB = defaultFromDB + dtvoFromDB.getGuid();//guid
            defaultFromDB = defaultFromDB + dtvoFromDB.getType();//type
            defaultFromDB = defaultFromDB + dtvoFromDB.getVersionNum();//versionNum
            defaultFromDB = defaultFromDB + dtvoFromDB.getPreviousVersionDtId();//PreviousVersionDT_ID
            defaultFromDB = defaultFromDB + dtvoFromDB.getDataTypeTerm();//data type term
            defaultFromDB = defaultFromDB + dtvoFromDB.getQualifier();//qualifier

            DataType baseDataType = dataTypeRepository.findOne(dtvoFromDB.getBasedDtId());

            defaultFromDB = defaultFromDB + Utility.denToTypeName(baseDataType.getDen());//base cdt den instead of base dt id
            defaultFromDB = defaultFromDB + Utility.denToTypeName(dtvoFromDB.getDen());//type name instead of den
            defaultFromDB = defaultFromDB + dtvoFromDB.getDefinition();//definition
            defaultFromDB = defaultFromDB + dtvoFromDB.getContentComponentDefinition();//content component definition
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionDoc();//revision_doc
            defaultFromDB = defaultFromDB + dtvoFromDB.getState();//state
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionNum();//revisionNum
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionTrackingNum();//revisionTrackingNum
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionAction();//revisionAction
            defaultFromDB = defaultFromDB + dtvoFromDB.getReleaseId();//releaseID
            defaultFromDB = defaultFromDB + dtvoFromDB.getCurrentBdtId();//currentBDTID
            defaultFromDB = defaultFromDB + dtvoFromDB.isDeprecated();//is_deprecated

            if (!defaultFromDB.equals(defaultFromXSD)) {
                System.err.println("@@@@ Default BDT has different values! Check: " + dtvoFromDB.getGuid());
                System.err.println("     FromXSD: " + defaultFromXSD);
                System.err.println("      FromDB: " + defaultFromDB);
            } else {
                System.out.println("# # # Default BDT " + typeName + " is Valid");
                if (union != null) {

                }

                validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDtId(), dtvoFromDB.getDtId(), unionExist, baseName);
            }
        } catch (EmptyResultDataAccessException e) {
            System.err.println("@@@@ Default BDT is not imported! Check: " + aElementBDT.getAttribute("id"));
        }


        unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("id");//guid
        unqualifiedFromXSD = unqualifiedFromXSD + "1";//type
        unqualifiedFromXSD = unqualifiedFromXSD + "1.0";//versionNum
        unqualifiedFromXSD = unqualifiedFromXSD + "0";//PreviousVersionDT_ID 0 means null
        unqualifiedFromXSD = unqualifiedFromXSD + dataTypeTerm;//data type term
        unqualifiedFromXSD = unqualifiedFromXSD + "null";//qualifier
        String baseDTName = "";
        if (type.equals("complex")) {
            Node aBaseNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension/@base");
            baseDTName = aBaseNode.getTextContent();
        } else {
            Node aBaseNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction/@base");
            baseDTName = aBaseNode.getTextContent();
        }
        unqualifiedFromXSD = unqualifiedFromXSD + baseDTName;//base dt den instead of base dt id
        unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("name");//type name instead of den

        String unqualifiedDefinition = "null";

        String unqualifiedCCDefinition = "null";

        unqualifiedFromXSD = unqualifiedFromXSD + "null";//unqualified BDT definition
        unqualifiedFromXSD = unqualifiedFromXSD + "null";//unqualified BDT content component definition
        unqualifiedFromXSD = unqualifiedFromXSD + "null";//revision_doc
        unqualifiedFromXSD = unqualifiedFromXSD + "3";//state

        unqualifiedFromXSD = unqualifiedFromXSD + "0";//revisionNum
        unqualifiedFromXSD = unqualifiedFromXSD + "0";//revisionTrackingNum
        unqualifiedFromXSD = unqualifiedFromXSD + "0";//revisionAction   0 means null
        unqualifiedFromXSD = unqualifiedFromXSD + "0";//releaseID   0 means null
        unqualifiedFromXSD = unqualifiedFromXSD + "0";//currentBDTID   0 means null
        unqualifiedFromXSD = unqualifiedFromXSD + "false";//is_deprecated

        unqualifiedFromXSD = unqualifiedFromXSD.replace("ID", "Identifier");

        try {
            dtvoFromDB = dataTypeRepository.findOneByGuid(aElementTN.getAttribute("id"));

            unqualifiedFromDB = "";
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getGuid();//guid
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getType();//type
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getVersionNum();//versionNum
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getPreviousVersionDtId();//PreviousVersionDT_ID
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getDataTypeTerm();//data type term
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getQualifier();//qualifier

            DataType baseDataType = dataTypeRepository.findOne(dtvoFromDB.getBasedDtId());

            unqualifiedFromDB = unqualifiedFromDB + Utility.denToTypeName(baseDataType.getDen());//base dt name instead of base dt id
            unqualifiedFromDB = unqualifiedFromDB + Utility.denToTypeName(dtvoFromDB.getDen());//type name instead of den
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getDefinition();//definition
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getContentComponentDefinition();//content component definition
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionDoc();//revision_doc
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getState();//state
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionNum();//revisionNum
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionTrackingNum();//revisionTrackingNum
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionAction();//revisionAction
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getReleaseId();//releaseID
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getCurrentBdtId();//currentBDTID
            unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.isDeprecated();//is_deprecated

            if (!unqualifiedFromDB.equals(unqualifiedFromXSD)) {
                System.err.println("@@@@ Unqualified BDT has different values! Check: " + dtvoFromDB.getGuid());
                System.err.println("     FromXSD: " + unqualifiedFromXSD);
                System.err.println("      FromDB: " + unqualifiedFromDB);
            } else {
                System.out.println("$ $ $ Unqaulified BDT " + dataType + " is Valid");
                validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDtId(), dtvoFromDB.getDtId(), false, baseName);
            }
        } catch (EmptyResultDataAccessException e) {
            System.err.println("@@@@ Unqaulified BDT is not imported! Check: " + aElementTN.getAttribute("id"));
        }


        ///////////////////////


        ///////////


//		Node union = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union");
//		int defaultId = -1;
//		DAOFactory df3 = DAOFactory.getDAOFactory();
//		SRTDAO dao3 = df3.getDAO("XSDBuiltInType");
//		if(union != null) {
//			QueryCondition qc3 = new QueryCondition();
//			qc3.add("name", "token");
//			defaultId = ((XSDBuiltInType)dao3.findObject(qc3, conn)).getXSDBuiltInTypeID();
//		} else {
//			Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:extension");
//			if(xsdTypeNameNode == null)
//				xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:restriction");
//			if(xsdTypeNameNode != null) {
//				Element xsdTypeNameElement = (Element)xsdTypeNameNode;
//				xsdTypeName = xsdTypeNameElement.getAttribute("base");
//
//				QueryCondition qc3 = new QueryCondition();
//				qc3.add("builtIn_type", xsdTypeName);
//				defaultId = ((XSDBuiltInType)dao3.findObject(qc3, conn)).getXSDBuiltInTypeID();
//				if(defaultId < 1) {
//					Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
//					QueryCondition qc4 = new QueryCondition();
//					System.out.println(xsdTypeName);
//					qc4.add("builtIn_type", ((Element)xbtNode).getAttribute("base"));
//					defaultId = ((XSDBuiltInType)dao3.findObject(qc4, conn)).getXSDBuiltInTypeID();
//				}
//			}
//		}
//
        //Let's start validating BDT_PRI_RESTRIs!
        //Get owner bdt, and


        //if (defaultId == -1 || defaultId == 0) System.out.println("Error getting the default BDT primitive restriction for the default BDT: " + typeName);
        //System.out.println("data type term = "+dataTypeTerm);
        //DataType dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
        //System.out.println("Inserting bdt primitive restriction for default bdt");
        //insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

        //Unqualified Type Name
        //String unQualifiedTypeName = dataType;

        //Unqualified Data Type Term
        //String unQualifiedDataTypeTerm = dataTypeTerm;
        //DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
        //System.out.println("Inserting bdt primitive restriction for unqualfieid bdt");
        //insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
    }

    private void importExceptionalDataTypeList2(String dataType) throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

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
            System.out.println("Default BDT is already existing");
            dVO1 = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));
        } else {
            dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
            System.out.println("Inserting bdt primitive restriction for exceptional default bdt");
            insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);
        }

        if (check_BDT(aElementBDT.getAttribute("id")))
            System.out.println("Unqualified BDT is already existing");
        else {
            //Unqualified Type Name
            String unQualifiedTypeName = dataType;

            //Unqualified Data Type Term
            String unQualifiedDataTypeTerm = dataTypeTerm;
            DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementBDT.getAttribute("id"), aElementBDT.getAttribute("id"));
            System.out.println("Inserting bdt primitive restriction for exceptional unqualified bdt");
            insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
        }
    }

    private void importExceptionalDataTypeList() throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String type = "simple";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

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
            System.out.println("Importing " + dataType + " now in the exception ");
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

            if (dataType.equals("DayOfWeekHourMinuteUTCType")) {
                int a = 0;
            }

            if (check_BDT(aElementBDT.getAttribute("id"))) {
                System.out.println("Default BDT is already existing");
                dVO1 = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));
            } else {
                dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
                System.out.println("Inserting bdt primitive restriction for exceptional default bdt");
                insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);
            }

            if (check_BDT(aElementTN.getAttribute("id")))
                System.out.println("Unqualified BDT is already existing");
            else {
                //Unqualified Type Name
                String unQualifiedTypeName = dataType;

                //Unqualified Data Type Term
                String unQualifiedDataTypeTerm = dataTypeTerm;
                DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
                System.out.println("Inserting bdt primitive restriction for exceptional unqualified bdt");
                insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
            }
        }
    }

    private void insertBDTPrimitiveRestrictionForExceptionalBDT(int cdtID, int bdtID, int defaultId) throws Exception {
        BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository =
                repositoryFactory.businessDataTypePrimitiveRestrictionRepository();
        CoreDataTypePrimitiveRepository cdtPriRepository =
                repositoryFactory.coreDataTypePrimitiveRepository();
        CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository =
                repositoryFactory.coreDataTypeAllowedPrimitiveRepository();
        CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository =
                repositoryFactory.coreDataTypeAllowedPrimitiveExpressionTypeMapRepository();

        List<CoreDataTypeAllowedPrimitive> al3 = cdtAwdPriRepository.findByCdtId(cdtID);
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
                    System.out.println("Inserting allowed primitive expression type map with XSD built-in type " +
                            getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = true");
                    bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);
                } else {
                    BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                    aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
                    aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aCDTAllowedPrimitiveExpressionTypeMapVO.getCdtAwdPriXpsTypeMapId());
                    aBDT_Primitive_RestrictionVO.setDefault(false);
                    System.out.println("Inserting allowed primitive expression type map with XSD built-in type " +
                            getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXbtId()) + ": default = false");
                    bdtPriRestriRepository.save(aBDT_Primitive_RestrictionVO);

                }
            }
        }
    }

    private void validateImportExceptionalDataTypeList() throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm = "";

        String defaultFromXSD = "";
        String defaultFromDB = "";

        String unqualifiedFromXSD = "";
        String unqualifiedFromDB = "";

        String Value_039C44FromXSD = "";
        String Value_039C44FromDB = "";

        String type = "simple";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

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

            defaultFromXSD = "";
            defaultFromDB = "";

            unqualifiedFromXSD = "";
            unqualifiedFromDB = "";

            Node typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
            //System.out.println("Validating "+dataType+" now in the exception ");
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
                    System.out.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType);
            } catch (Exception e) {
                System.out.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType + " Stacktrace:" + e.getMessage());
            }

            Node base = businessDataType_xsd.getNode("//xsd:complexType[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/@base");
            if (base == null) {
                base = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']/xsd:restriction/@base");
            }
            String baseName = "";
            if (base != null) {
                baseName = base.getTextContent();
            }
            if (baseName.startsWith("xbt_")) {
                Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + baseName + "']/xsd:restriction");
                baseName = ((Element) xbtNode).getAttribute("base");
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


            defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("id");
            defaultFromXSD = defaultFromXSD + "1";//type
            defaultFromXSD = defaultFromXSD + "1.0";//versionNum
            defaultFromXSD = defaultFromXSD + "0";//PreviousVersionDT_ID 0 means null
            defaultFromXSD = defaultFromXSD + dataTypeTerm;//data type term
            defaultFromXSD = defaultFromXSD + "null";//qualifier
            String baseCDTDen = "";
            baseCDTDen = Utility.denWithoutUUID(typeName);
            baseCDTDen = baseCDTDen.replace(". Type", "");

            defaultFromXSD = defaultFromXSD + baseCDTDen;//base cdt den instead of base dt id
            defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("name");//type name instead of den
            String definition = "";
            if (definitionNode != null) {
                definition = definitionNode.getTextContent();
            }
            if (definition.equals("")) {
                definition = "null";
            }

            String ccdefinition = "";
            if (ccDefinitionNode != null) {
                ccdefinition = ccDefinitionNode.getTextContent();
            }
            if (ccdefinition.equals("")) {
                ccdefinition = "null";
            }

            defaultFromXSD = defaultFromXSD + definition;//definition
            defaultFromXSD = defaultFromXSD + ccdefinition;//content component definition
            defaultFromXSD = defaultFromXSD + "null";//revision_doc
            defaultFromXSD = defaultFromXSD + "3";//state

            defaultFromXSD = defaultFromXSD + "0";//revisionNum
            defaultFromXSD = defaultFromXSD + "0";//revisionTrackingNum
            defaultFromXSD = defaultFromXSD + "0";//revisionAction   0 means null
            defaultFromXSD = defaultFromXSD + "0";//releaseID   0 means null
            defaultFromXSD = defaultFromXSD + "0";//currentBDTID   0 means null
            defaultFromXSD = defaultFromXSD + "false";//is_deprecated

            defaultFromXSD = defaultFromXSD.replace("ID", "Identifier");

            DataType dtvoFromDB = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));

            if (dtvoFromDB == null) {
                System.out.println("@@@@ Default BDT is not imported! Check: " + aElementBDT.getAttribute("id"));
            } else {
                defaultFromDB = "";
                defaultFromDB = defaultFromDB + dtvoFromDB.getGuid();//guid
                defaultFromDB = defaultFromDB + dtvoFromDB.getType();//type
                defaultFromDB = defaultFromDB + dtvoFromDB.getVersionNum();//versionNum
                defaultFromDB = defaultFromDB + dtvoFromDB.getPreviousVersionDtId();//PreviousVersionDT_ID
                defaultFromDB = defaultFromDB + dtvoFromDB.getDataTypeTerm();//data type term
                defaultFromDB = defaultFromDB + dtvoFromDB.getQualifier();//qualifier

                DataType baseDataType = dataTypeRepository.findOne(dtvoFromDB.getBasedDtId());

                defaultFromDB = defaultFromDB + Utility.denToTypeName(baseDataType.getDen());//base cdt den instead of base dt id
                defaultFromDB = defaultFromDB + Utility.denToTypeName(dtvoFromDB.getDen());//type name instead of den
                defaultFromDB = defaultFromDB + dtvoFromDB.getDefinition();//definition
                defaultFromDB = defaultFromDB + dtvoFromDB.getContentComponentDefinition();//content component definition
                defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionDoc();//revision_doc
                defaultFromDB = defaultFromDB + dtvoFromDB.getState();//state
                defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionNum();//revisionNum
                defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionTrackingNum();//revisionTrackingNum
                defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionAction();//revisionAction
                defaultFromDB = defaultFromDB + dtvoFromDB.getReleaseId();//releaseID
                defaultFromDB = defaultFromDB + dtvoFromDB.getCurrentBdtId();//currentBDTID
                defaultFromDB = defaultFromDB + dtvoFromDB.isDeprecated();//is_deprecated
            }

            if (!defaultFromDB.equals(defaultFromXSD)) {
                System.err.println("@@@@ Default BDT has different values! Check: " + dtvoFromDB.getGuid());
                System.err.println("     FromXSD: " + defaultFromXSD);
                System.err.println("      FromDB: " + defaultFromDB);
            } else {
                System.out.println("# # # Default BDT " + typeName + " (Exceptional BDT) is Valid");
                int checkInserted = 0;
                List<DataType> DTList = dataTypeRepository.findByDataTypeTerm(dtvoFromDB.getDataTypeTerm());
                for (int i = 0; i < DTList.size(); i++) {
                    DataType aDT = DTList.get(i);
                    if (aDT.getBasedDtId() == dtvoFromDB.getDtId()) {
                        checkInserted++;
                    }
                }

                if (checkInserted <= 1) {
                    validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDtId(), dtvoFromDB.getDtId(), false, baseName);
                } else {
                    System.out.println("      BDT Pri restri of Default BDT " + typeName + " is Checked! already");
                }
            }

            unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("id");//guid
            unqualifiedFromXSD = unqualifiedFromXSD + "1";//type
            unqualifiedFromXSD = unqualifiedFromXSD + "1.0";//versionNum
            unqualifiedFromXSD = unqualifiedFromXSD + "0";//PreviousVersionDT_ID 0 means null
            unqualifiedFromXSD = unqualifiedFromXSD + dataTypeTerm;//data type term
            unqualifiedFromXSD = unqualifiedFromXSD + "null";//qualifier
            String baseDTName = "";
            if (type.equals("complex")) {
                Node aBaseNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension/@base");
                baseDTName = aBaseNode.getTextContent();
            } else {
                Node aBaseNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction/@base");
                baseDTName = aBaseNode.getTextContent();
            }
            unqualifiedFromXSD = unqualifiedFromXSD + baseDTName;//base dt den instead of base dt id
            unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("name");//type name instead of den

            String unqualifiedDefinition = "null";

            String unqualifiedCCDefinition = "null";

            unqualifiedFromXSD = unqualifiedFromXSD + "null";//unqualified BDT definition
            unqualifiedFromXSD = unqualifiedFromXSD + "null";//unqualified BDT content component definition
            unqualifiedFromXSD = unqualifiedFromXSD + "null";//revision_doc
            unqualifiedFromXSD = unqualifiedFromXSD + "3";//state

            unqualifiedFromXSD = unqualifiedFromXSD + "0";//revisionNum
            unqualifiedFromXSD = unqualifiedFromXSD + "0";//revisionTrackingNum
            unqualifiedFromXSD = unqualifiedFromXSD + "0";//revisionAction   0 means null
            unqualifiedFromXSD = unqualifiedFromXSD + "0";//releaseID   0 means null
            unqualifiedFromXSD = unqualifiedFromXSD + "0";//currentBDTID   0 means null
            unqualifiedFromXSD = unqualifiedFromXSD + "false";//is_deprecated

            unqualifiedFromXSD = unqualifiedFromXSD.replace("ID", "Identifier");

            dtvoFromDB = dataTypeRepository.findOneByGuid(aElementTN.getAttribute("id"));
            if (dtvoFromDB == null) {
                System.err.println("@@@@ Unqaulified BDT is not imported! Check: " + aElementTN.getAttribute("id"));
            } else {
                unqualifiedFromDB = "";
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getGuid();//guid
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getType();//type
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getVersionNum();//versionNum
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getPreviousVersionDtId();//PreviousVersionDT_ID
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getDataTypeTerm();//data type term
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getQualifier();//qualifier

                DataType baseDataType = dataTypeRepository.findOne(dtvoFromDB.getBasedDtId());

                unqualifiedFromDB = unqualifiedFromDB + Utility.denToTypeName(baseDataType.getDen());//base dt name instead of base dt id
                unqualifiedFromDB = unqualifiedFromDB + Utility.denToTypeName(dtvoFromDB.getDen());//type name instead of den
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getDefinition();//definition
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getContentComponentDefinition();//content component definition
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionDoc();//revision_doc
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getState();//state
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionNum();//revisionNum
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionTrackingNum();//revisionTrackingNum
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getRevisionAction();//revisionAction
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getReleaseId();//releaseID
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.getCurrentBdtId();//currentBDTID
                unqualifiedFromDB = unqualifiedFromDB + dtvoFromDB.isDeprecated();//is_deprecated
            }

            if (!unqualifiedFromDB.equals(unqualifiedFromXSD)) {
                System.err.println("@@@@ Unqualified BDT has different values! Check: " + dtvoFromDB.getGuid());
                System.err.println("     FromXSD: " + unqualifiedFromXSD);
                System.err.println("      FromDB: " + unqualifiedFromDB);
            } else {
                System.out.println("$ $ $ Unqaulified BDT " + dataType + " (Exceptional BDT) is Valid");
                validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDtId(), dtvoFromDB.getDtId(), false, baseName);
            }

        }


        //Check ValueType_039C44

        defaultFromXSD = "";
        defaultFromDB = "";
        //Data Type Term
        Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

        Element dataTypeTermElement = (Element) dataTypeTermNode;
        try {
            dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
            if (dataTypeTerm == "")
                System.err.println("Error getting the data type term for the unqualified BDT in the exception: ValueType_039C44");
        } catch (Exception e) {
            System.err.println("Error getting the data type term for the unqualified BDT in the exception: ValueType_039C44 Stacktrace:" + e.getMessage());
        }

        //Definitions
        Node definitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
        Element definitionElement = (Element) definitionNode;
        Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        if (ccDefinitionNode == null)
            ccDefinitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
        Element ccDefinitionElement = (Element) ccDefinitionNode;

        Node aNodeBDT = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']");
        Element aElementBDT = (Element) aNodeBDT;


        defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("id");
        defaultFromXSD = defaultFromXSD + "1";//type
        defaultFromXSD = defaultFromXSD + "1.0";//versionNum
        defaultFromXSD = defaultFromXSD + "0";//PreviousVersionDT_ID 0 means null
        defaultFromXSD = defaultFromXSD + dataTypeTerm;//data type term
        defaultFromXSD = defaultFromXSD + "null";//qualifier
        String baseCDTDen = "";
        baseCDTDen = Utility.denWithoutUUID("ValueType_039C44");
        baseCDTDen = baseCDTDen.replace(". Type", "");

        defaultFromXSD = defaultFromXSD + baseCDTDen;//base cdt den instead of base dt id
        defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("name");//type name instead of den
        String definition = "";
        if (definitionNode != null) {
            definition = definitionNode.getTextContent();
        }
        if (definition.equals("")) {
            definition = "null";
        }

        String ccdefinition = "";
        if (ccDefinitionNode != null) {
            ccdefinition = ccDefinitionNode.getTextContent();
        }
        if (ccdefinition.equals("")) {
            ccdefinition = "null";
        }

        defaultFromXSD = defaultFromXSD + definition;//definition
        defaultFromXSD = defaultFromXSD + ccdefinition;//content component definition
        defaultFromXSD = defaultFromXSD + "null";//revision_doc
        defaultFromXSD = defaultFromXSD + "3";//state

        defaultFromXSD = defaultFromXSD + "0";//revisionNum
        defaultFromXSD = defaultFromXSD + "0";//revisionTrackingNum
        defaultFromXSD = defaultFromXSD + "0";//revisionAction   0 means null
        defaultFromXSD = defaultFromXSD + "0";//releaseID   0 means null
        defaultFromXSD = defaultFromXSD + "0";//currentBDTID   0 means null
        defaultFromXSD = defaultFromXSD + "false";//is_deprecated

        defaultFromXSD = defaultFromXSD.replace("ID", "Identifier");
        DataType dtvoFromDB;
        try {
            dtvoFromDB = dataTypeRepository.findOneByGuid(aElementBDT.getAttribute("id"));

            defaultFromDB = "";
            defaultFromDB = defaultFromDB + dtvoFromDB.getGuid();//guid
            defaultFromDB = defaultFromDB + dtvoFromDB.getType();//type
            defaultFromDB = defaultFromDB + dtvoFromDB.getVersionNum();//versionNum
            defaultFromDB = defaultFromDB + dtvoFromDB.getPreviousVersionDtId();//PreviousVersionDT_ID
            defaultFromDB = defaultFromDB + dtvoFromDB.getDataTypeTerm();//data type term
            defaultFromDB = defaultFromDB + dtvoFromDB.getQualifier();//qualifier

            DataType baseDataType = dataTypeRepository.findOne(dtvoFromDB.getBasedDtId());

            defaultFromDB = defaultFromDB + Utility.denToTypeName(baseDataType.getDen());//base cdt den instead of base dt id
            defaultFromDB = defaultFromDB + Utility.denToTypeName(dtvoFromDB.getDen());//type name instead of den
            defaultFromDB = defaultFromDB + dtvoFromDB.getDefinition();//definition
            defaultFromDB = defaultFromDB + dtvoFromDB.getContentComponentDefinition();//content component definition
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionDoc();//revision_doc
            defaultFromDB = defaultFromDB + dtvoFromDB.getState();//state
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionNum();//revisionNum
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionTrackingNum();//revisionTrackingNum
            defaultFromDB = defaultFromDB + dtvoFromDB.getRevisionAction();//revisionAction
            defaultFromDB = defaultFromDB + dtvoFromDB.getReleaseId();//releaseID
            defaultFromDB = defaultFromDB + dtvoFromDB.getCurrentBdtId();//currentBDTID
            defaultFromDB = defaultFromDB + dtvoFromDB.isDeprecated();//is_deprecated

            if (!defaultFromDB.equals(defaultFromXSD)) {
                System.err.println("@@@@ Default BDT ValueType_039C44 has different values! ");
                System.err.println("     FromXSD: " + defaultFromXSD);
                System.err.println("      FromDB: " + defaultFromDB);
            } else {
                System.out.println("# # # Default BDT ValueType_039C44 (Exceptional BDT) is Valid");

                validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDtId(), dtvoFromDB.getDtId(), false, "xsd:integer");
            }
        } catch (EmptyResultDataAccessException e) {
            System.err.println("@@@@ Default BDT is not imported! Check: " + aElementBDT.getAttribute("id"));
        }
    }

    private void validateInsertBDTPrimitiveRestriction(int basedtID, int bdtId, boolean unionExist, String baseName) throws Exception {
        BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository =
                repositoryFactory.businessDataTypePrimitiveRestrictionRepository();
        CoreDataTypePrimitiveRepository cdtPriRepository =
                repositoryFactory.coreDataTypePrimitiveRepository();
        CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository =
                repositoryFactory.coreDataTypeAllowedPrimitiveRepository();
        CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository =
                repositoryFactory.coreDataTypeAllowedPrimitiveExpressionTypeMapRepository();

        DataType baseDataType = dataTypeRepository.findOne(basedtID);
        if (baseDataType.getType() == 0) {//if the base is CDT and this is default

            List<CoreDataTypeAllowedPrimitive> cdtAwdPriList = cdtAwdPriRepository.findByCdtId(basedtID);
            List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> cdtAwdPriXpsTypeMapList =
                    new ArrayList(cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId((cdtAwdPriList.get(0).getCdtAwdPriId())));
            for (int i = 1; i < cdtAwdPriList.size(); i++) {
                List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> listAdded =
                        new ArrayList(cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId((cdtAwdPriList.get(i).getCdtAwdPriId())));
                cdtAwdPriXpsTypeMapList.addAll(listAdded);
            }

            List<BusinessDataTypePrimitiveRestriction> defaultBDTPri = new ArrayList(bdtPriRestriRepository.findByBdtId(bdtId));

            int defaultCount = 0;

            if (cdtAwdPriXpsTypeMapList.size() != defaultBDTPri.size()) {
                System.out.println("@@@@ Default BDT Pri is not properly imported! Check BDT: " + bdtId);
            } else {
                for (int i = cdtAwdPriXpsTypeMapList.size() - 1; i > -1; i--) {
                    for (int j = defaultBDTPri.size() - 1; j > -1; j--) {
                        int thisCDTAwdPriXpsTypeMapId = defaultBDTPri.get(j).getCdtAwdPriXpsTypeMapId();
                        if (cdtAwdPriXpsTypeMapList.get(i).getCdtAwdPriXpsTypeMapId() == thisCDTAwdPriXpsTypeMapId) {

                            if (defaultBDTPri.get(j).isDefault()) {
                                defaultCount++;
                                CoreDataTypeAllowedPrimitiveExpressionTypeMap mapFromBDT =
                                        cdtAwdPriXpsTypeMapRepository.findOneByCdtAwdPriXpsTypeMapId(thisCDTAwdPriXpsTypeMapId);
                                CoreDataTypeAllowedPrimitive aCDTAP = cdtAwdPriRepository.findOneByCdtAwdPriId(mapFromBDT.getCdtAwdPriId());
                                CoreDataTypePrimitive aCDTP = cdtPriRepository.findOneByCdtPriId(aCDTAP.getCdtPriId());
                                XSDBuiltInType xbt = xbtRepository.findOne(mapFromBDT.getXbtId());

                                if (unionExist) {
                                    if (!xbt.getBuiltInType().equals("xsd:token")) {//xsd:token but is not default
                                        System.err.println("@@@@ xsd:token should be Default! Check DT_ID:" + bdtId);
                                    }
                                } else {
                                    if (!xbt.getBuiltInType().equals(baseName)) {
                                        System.err.println("@@@@ " + baseName + " should be Default! Check DT_ID:" + bdtId);
                                    }
                                }
                            }

                            cdtAwdPriXpsTypeMapList.remove(i);
                            defaultBDTPri.remove(j);
                            break;
                        }
                    }
                }
                if (defaultCount != 1) {
                    System.out.println("@@@@ Default BDT Pri has multiple or zero isDefault! Check BDT: " + bdtId);
                }
            }
        } else {//if the base is Default and this is unqualified bdt

            List<BusinessDataTypePrimitiveRestriction> defaultBDTPriList = new ArrayList(bdtPriRestriRepository.findByBdtId(basedtID));
            List<BusinessDataTypePrimitiveRestriction> unqualifiedBDTPriList = new ArrayList(bdtPriRestriRepository.findByBdtId(bdtId));
            int defaultCount = 0;
            if (defaultBDTPriList.size() != unqualifiedBDTPriList.size()) {
                System.out.println("@@@@ Unqualified BDT Pri is not properly inherited! Check BDT: " + bdtId);
            } else {
                for (int i = defaultBDTPriList.size() - 1; i > -1; i--) {
                    for (int j = unqualifiedBDTPriList.size() - 1; j > -1; j--) {
                        BusinessDataTypePrimitiveRestriction defaultBDTPri = defaultBDTPriList.get(i);
                        BusinessDataTypePrimitiveRestriction unqualifiedBDTPri = unqualifiedBDTPriList.get(j);
                        if (defaultBDTPri.getCdtAwdPriXpsTypeMapId() == unqualifiedBDTPri.getCdtAwdPriXpsTypeMapId()
                                && defaultBDTPri.isDefault() == unqualifiedBDTPri.isDefault()) {
                            defaultBDTPriList.remove(i);
                            unqualifiedBDTPriList.remove(j);
                            if (unqualifiedBDTPri.isDefault()) {
                                defaultCount++;
                            }
                            break;
                        }
                    }
                }
                if (defaultBDTPriList.size() > 0 || unqualifiedBDTPriList.size() > 0) {
                    System.out.println("@@@@ Unqualified BDT Pri is not properly inherited! Check BDT: " + bdtId);
                }
                if (defaultCount != 1) {
                    System.out.println("@@@@ Unqaulified BDT Pri has multiple or zero isDefault! Check BDT: " + bdtId);
                }
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

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);


        Node aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']");
        Element aElementTN = (Element) aNodeTN;
        id = aElementTN.getAttribute("id");
        aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        aElementTN = (Element) aNodeTN;
        typeName = aElementTN.getAttribute("base");

        String den = Utility.typeToDen(typeName);
        DataType dVO1 = dataTypeRepository.findOneByDen(den);
        baseDataTypeTerm = dVO1.getDataTypeTerm();
        baseGUID = dVO1.getGuid();


        //Unqualified Type Name
        String unQualifiedTypeName = dataType.replaceAll("Type", "");

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

        DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, id, baseGUID);
        insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
    }

    private void importIDContentType() throws Exception {

        String dataType = "IDContentType";

        String typeName;
        String xsdTypeName;
        String baseDataTypeTerm;
        String baseGUID;
        String id;
        int defaultId = -1;

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);


        Node aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']");
        Element aElementTN = (Element) aNodeTN;
        id = aElementTN.getAttribute("id");
        aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
        aElementTN = (Element) aNodeTN;
        typeName = aElementTN.getAttribute("base");

        String den = Utility.typeToDen(typeName);
        DataType dVO1 = dataTypeRepository.findOneByDen(den);
        baseDataTypeTerm = dVO1.getDataTypeTerm();
        baseGUID = dVO1.getGuid();


        //Unqualified Type Name
        String unQualifiedTypeName = dataType.replaceAll("Type", "");

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
        DataType dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, id, baseGUID);
        insertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
    }

    private int getXSDBuiltInTypeId(String xsd_buitintype) {
        XSDBuiltInType xbt = xbtRepository.findOneByBuiltInType(xsd_buitintype);
        return (xbt != null) ? xbt.getXbtId() : 0;
    }
}
