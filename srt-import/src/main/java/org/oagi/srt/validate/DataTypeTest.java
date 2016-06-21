package org.oagi.srt.validate;

import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.config.ImportConfig;
import org.oagi.srt.config.RepositoryConfig;
import org.oagi.srt.persistence.populate.Types;
import org.oagi.srt.repository.*;
import org.oagi.srt.repository.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataTypeTest {

    @Autowired
    private AgencyIdListRepository agencyIdListRepository;

    @Autowired
    private CodeListRepository codeListRepository;

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Autowired
    private DataTypeSupplementaryComponentRepository dtScRepository;

    @Autowired
    private XSDBuiltInTypeRepository xbtRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveExpressionTypeMapRepository cdtAwdPriXpsTypeMapRepository;

    @Autowired
    private CoreDataTypeAllowedPrimitiveRepository cdtAwdPriRepository;

    @Autowired
    private BusinessDataTypePrimitiveRestrictionRepository bdtPriRestriRepository;

    @Autowired
    private CoreDataTypePrimitiveRepository cdtPriRepository;

    @Autowired
    private BusinessDataTypeSupplementaryComponentPrimitiveRestrictionRepository bdtScPriRestriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveRepository cdtScAwdPriRepository;

    @Autowired
    private CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMapRepository cdtScAwdPriXpsTypeMapRepository;

    public DataType validateInsertDefault_BDTStatement(String typeName, String dataTypeTerm, String id) {
        try {
            return dataTypeRepository.findOneByGuid(id);
            // System.out.println("Success!!");
        } catch (EmptyResultDataAccessException e) {
            System.err.println("Error! " + id);
            return null;
        }
    }

    public void validatePopulateAdditionalDefault_BDTStatement(XPathHandler filename) throws Exception {
        NodeList xsd_node = filename.getNodeList("//xsd:attribute");
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
        for (int i = 0; i < xsd_node.getLength(); i++) {
            Element tmp = (Element) xsd_node.item(i);
            String typeName = tmp.getAttribute("type");

            String den = tmp.getAttribute("type").replaceAll("Type", "") + ". Type";

            if (dataTypeRepository.findByDen(den) == null) {
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

                            defaultId = xbtRepository.findOneByBuiltInType(xsdTypeName).getXbtId();
                            if (defaultId == 0) {
                                Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                                defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
                            }
                        }
                    }

                    typeName = tmp.getAttribute("type").replaceAll("Type", "");

                    DataType dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));

                    validateInsertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);
                }
            }
        }
    }

    public DataType validateInsertUnqualified_BDTStatement(String typeName, String dataTypeTerm, String id, String defaultGUID) {
        try {
            return dataTypeRepository.findOneByGuid(id);
        } catch (EmptyResultDataAccessException e) {
            System.err.println("Error!" + e.getStackTrace()[0].getLineNumber());
            return null;
        }
    }

    private void validateDefaultImportDataTypeList(String dataType) throws Exception {
        String typeName = null;
        String xsdTypeName;
        String dataTypeTerm;

        String type = "complex";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);


        for (int i = 0; i < Types.defaultDataTypeList.length; i++) {
            if (Types.defaultDataTypeList[i].startsWith(dataType))
                typeName = Types.defaultDataTypeList[i];
        }

        //typeName = dataType;
        Node aNodeTN = fields_xsd.getNode("//xsd:" + type + "Type[@name = '" + dataType + "']");
        Element aElementTN = (Element) aNodeTN;

        //Data Type Term
        Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:" + type + "Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

        dataTypeTerm = typeName.substring(0, typeName.indexOf("Type"));

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

                defaultId = xbtRepository.findOneByBuiltInType(xsdTypeName).getXbtId();
                if (defaultId == 0) {
                    Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                    defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
                }
            }
        }

        typeName = typeName.replaceAll("Type", "");

        DataType dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));
        System.out.println(dVO1.getBasedDtId() + "  " + dVO1.getDtId() + "  " + defaultId);
        validateInsertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);

        //Unqualified Type Name
        String unQualifiedTypeName = dataType.replaceAll("Type", "");

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = dataTypeTerm;

        DataType dVO2 = validateInsertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
        if (dVO2 != null) {
            System.out.println(dVO1.getBasedDtId() + "  " + dVO2.getDtId() + "  " + defaultId);
            validateInsertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
        }
    }

    private void validateImportDataTypeList(String dataType) throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm;

        String type = "complex";

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
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
        dataTypeTerm = dataTypeTermElement.getTextContent();
        if (dataTypeTerm.length() > 5)
            if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
                dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
        //dataTypeTerm = dataTypeTerm.replaceAll(" Object", "");
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

                defaultId = xbtRepository.findOneByBuiltInType(xsdTypeName).getXbtId();
                if (defaultId == 0) {
                    Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                    defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
                }
            }
        }

        typeName = typeName.replaceAll("Type", "");

        DataType dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));

        validateInsertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);

        //Unqualified Type Name
        String unQualifiedTypeName = dataType.replaceAll("Type", "");

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = dataTypeTerm;

        DataType dVO2 = validateInsertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
        if (dVO2 != null) {
            validateInsertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
        }
    }

    private void validateImportExceptionalDataTypeList(String dataType) throws Exception {
        String typeName;
        String xsdTypeName;
        String dataTypeTerm;

        XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
        XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

        typeName = "ValueType_039C44";
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

                defaultId = xbtRepository.findOneByBuiltInType(xsdTypeName).getXbtId();
                if (defaultId == 0) {
                    Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
                    defaultId = xbtRepository.findOneByBuiltInType(((Element) xbtNode).getAttribute("base")).getXbtId();
                }
            }
        }

        typeName = typeName.replaceAll("Type", "");

        DataType dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));

        validateInsertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO1.getDtId(), defaultId);

        //Unqualified Type Name
        String unQualifiedTypeName = dataType.replaceAll("Type", "");

        //Unqualified Data Type Term
        String unQualifiedDataTypeTerm = dataTypeTerm;

        DataType dVO2 = validateInsertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementBDT.getAttribute("id"), aElementBDT.getAttribute("id"));
        if (dVO2 != null) {
            validateInsertBDTPrimitiveRestriction(dVO1.getBasedDtId(), dVO2.getDtId(), defaultId);
        }
    }

    private void validateInsertBDTPrimitiveRestriction(int cdtID, int bdtID, int defaultId) {
        List<CoreDataTypeAllowedPrimitive> al3 = cdtAwdPriRepository.findByCdtId(cdtID);

        for (CoreDataTypeAllowedPrimitive aCoreDataTypeAllowedPrimitive : al3) {

            List<CoreDataTypeAllowedPrimitiveExpressionTypeMap> al4 = cdtAwdPriXpsTypeMapRepository.findByCdtAwdPriId(
                    aCoreDataTypeAllowedPrimitive.getCdtAwdPriId());

            for (CoreDataTypeAllowedPrimitiveExpressionTypeMap aCoreDataTypeAllowedPrimitiveExpressionTypeMap : al4) {
                // create insert statement -->modify
                BusinessDataTypePrimitiveRestriction aBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
                aBDT_Primitive_RestrictionVO.setBdtId(bdtID);
                aBDT_Primitive_RestrictionVO.setCdtAwdPriXpsTypeMapId(aCoreDataTypeAllowedPrimitiveExpressionTypeMap.getCdtAwdPriXpsTypeMapId());

                try {
                    bdtPriRestriRepository.findOneByBdtIdAndCdtAwdPriXpsTypeMapId(bdtID, aCoreDataTypeAllowedPrimitiveExpressionTypeMap.getCdtAwdPriXpsTypeMapId());
                } catch (EmptyResultDataAccessException e) {
                    System.err.println("Error!  bdt_id = " + bdtID + "   cdt_awd_pri_xps_type_map_id = " + aCoreDataTypeAllowedPrimitiveExpressionTypeMap.getCdtAwdPriXpsTypeMapId());
                }
            }
        }
    }

    public void validateImportAdditionalBDT() throws Exception {
        XPathHandler xh = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);

        NodeList result = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");

        for (int i = 0; i < result.getLength(); i++) {
            Element ele = (Element) result.item(i);
            String name = ele.getAttribute("name");
            String guid = ele.getAttribute("id");
            String den = name.substring(0, name.lastIndexOf("Type")) + ". Type";
            DataType dtVO;
            try {
                dtVO = dataTypeRepository.findOneByGuidAndDen(guid, den);
                // BDT_Primitive_Restriction
                validateInsertBDTPrimitiveRestriction(dtVO.getDtId(), dataTypeRepository.findOneByGuid(dtVO.getGuid()).getDtId());
            } catch (EmptyResultDataAccessException e) {
                System.out.println("Error in line # " + new Exception().getStackTrace()[0].getLineNumber());
            }
        }
    }

    private void validateInsertBDTPrimitiveRestriction(int basedBdtId, int bdtId) {
        List<BusinessDataTypePrimitiveRestriction> al = bdtPriRestriRepository.findByBdtId(basedBdtId);

        for (BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction : al) {
            int cdtAwdPriXpsTypeMapId = aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId();
            int codeListId = aBusinessDataTypePrimitiveRestriction.getCodeListId();

            Object result = null;
            if (cdtAwdPriXpsTypeMapId != 0 && codeListId != 0) {
                result = bdtPriRestriRepository.findOneByCodeListIdAndCdtAwdPriXpsTypeMapId(codeListId, cdtAwdPriXpsTypeMapId);
            } else if (cdtAwdPriXpsTypeMapId != 0) {
                List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                        bdtPriRestriRepository.findByCdtAwdPriXpsTypeMapId(cdtAwdPriXpsTypeMapId);
                result = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);
            } else if (codeListId != 0) {
                result = bdtPriRestriRepository.findOneByCodeListId(codeListId);
            }
            if (result == null) {
                System.err.println("Error!" + new Exception().getStackTrace()[0].getLineNumber());
            }
        }
    }


    private void validateImportQBDT(XPathHandler fields_xsd) throws Exception {
        NodeList elementsFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:element");
        NodeList simpleTypesFromFieldXSD = fields_xsd.getNodeList("//xsd:simpleType");
        validateInsertCodeContentQBDT(simpleTypesFromFieldXSD, fields_xsd, 0);
        validateInsertIDContentQBDT(simpleTypesFromFieldXSD, fields_xsd, 0);

        validateInsertOtherQBDT(elementsFromFieldsXSD, fields_xsd, 0);
    }

    private void validateInsertCodeContentQBDT(NodeList simpleTypesFromFieldXSD, XPathHandler org_xHandler, int xsdType) throws Exception {
        XPathHandler xHandler = org_xHandler;

        //specify Code Content Type QBDT
        //the xsd:simpleType whose names end with �CodeContentType�, except the �CodeContentType� itself. 

        for (int i = 0; i < simpleTypesFromFieldXSD.getLength(); i++) {
            xHandler = org_xHandler;
            String name = ((Element) simpleTypesFromFieldXSD.item(i)).getAttribute("name");

            if (!name.equals("CodeContentType") && name.endsWith("CodeContentType")) {
                Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");

                if (simpleType != null) {
                    Node typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");

                    String typeGuid = ((Element) typeNode).getAttribute("id");
                    DataType dtVO = dataTypeRepository.findOneByGuid(typeGuid);

                    if (dtVO != null) {
                        DataType dVO = validateAddToDTForCodeContentQBDT(dtVO, name, typeNode, xHandler);
                        if (dVO != null)
                            validateAddToDTSCForContentType(xHandler, name, dVO);
                    } else {
                        System.out.println("Error! Qualified Code Content Type: " + name + " is not exist in Database");
                    }
                }
            }
        }
    }

    private void validateInsertIDContentQBDT(NodeList simpleTypesFromFieldXSD, XPathHandler org_xHandler, int xsdType) throws Exception {
        XPathHandler xHandler = org_xHandler;

        //specify ID Content Type QBDT
        //the xsd:simpleType whose names end with �IDContentType� 

        for (int i = 0; i < simpleTypesFromFieldXSD.getLength(); i++) {
            xHandler = org_xHandler;
            String name = ((Element) simpleTypesFromFieldXSD.item(i)).getAttribute("name");

            if (!name.equals("IDContentType") && name.endsWith("IDContentType")) {//Actually Only AgencyIDContentType..
                Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");

                if (simpleType != null) {
                    Node typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");

                    String typeGuid = ((Element) typeNode).getAttribute("id");
                    DataType dtVO = dataTypeRepository.findOneByGuid(typeGuid);

                    if (dtVO != null) {
                        DataType dVO = validateAddToDTForIDContentQBDT(dtVO, name, typeNode, xHandler);
                        if (dVO != null)
                            validateAddToDTSCForContentType(xHandler, name, dVO);
                    } else {
                        System.out.println("Error! Qualified ID Content Type: " + name + " is not exist in Database");
                    }
                }
            }
        }
    }

    private void validateInsertOtherQBDT(NodeList elementsFromXSD, XPathHandler org_xHandler, int xsdType) throws Exception {
        XPathHandler xHandler = org_xHandler;
        for (int i = 0; i < elementsFromXSD.getLength(); i++) {
            xHandler = org_xHandler;
            String bccp = ((Element) elementsFromXSD.item(i)).getAttribute("name");
            String type = ((Element) elementsFromXSD.item(i)).getAttribute("type");
            Node simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
            Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");

            simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
            simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");

            if (simpleContent != null || simpleType != null) {
//				Node documentationFromXSD = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation");
//				String definition = "";
//				if(documentationFromXSD != null) {
//					Node documentationFromCCTS = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
//					if(documentationFromCCTS != null)
//						definition = ((Element)documentationFromCCTS).getTextContent();
//					else
//						definition = ((Element)documentationFromXSD).getTextContent();
//				}

                Node typeNode = xHandler.getNode("//xsd:complexType[@name = '" + type + "']");
                if (typeNode == null) {
                    typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
                }

                String typeGuid = ((Element) typeNode).getAttribute("id");
                DataType dtVO = dataTypeRepository.findOneByGuid(typeGuid);

                if (dtVO != null) {

                    if (dtVO.getQualifier() != null) {
                        DataType basedtVO = dataTypeRepository.findOne(dtVO.getBasedDtId());
                        boolean checkAlready = false;

                        if (basedtVO.getDen().equals("Code Content. Type") || basedtVO.getDen().equals("Identifier Content. Type")) {
                            checkAlready = true;
                        }

                        if (!checkAlready) {
                            DataType dVO = validateAddToDTForOtherQBDT(dtVO, type, typeNode, xHandler);
                            if (dVO != null) {
                                validateAddToDTSC(xHandler, type, dVO);
                            }
                        }
                    }
                } else {
                    System.out.println("Error! OtherQBDT is not in database. Check guid: " + typeGuid);
                }
            }
        }
    }

    private List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> getBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtscVO) {
        return bdtScPriRestriRepository.findByBdtScId(dtscVO.getBasedDtScId());
    }

    private void validateInsertBDTSCPrimitiveRestriction(DataTypeSupplementaryComponent dtscVO, int mode, String name, String type) throws Exception {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> cdtscallowedprimitivelist = new ArrayList();
        if (mode == 1) {//inherited
            List<BusinessDataTypeSupplementaryComponentPrimitiveRestriction> bdtscs = getBDTSCPrimitiveRestriction(dtscVO);
            for (BusinessDataTypeSupplementaryComponentPrimitiveRestriction parent : bdtscs) {

                try {
                    bdtScPriRestriRepository.findOneByBdtScIdAndCdtScAwdPriXpsTypeMapIdAndCodeListIdAndAgencyIdListId(
                            dtscVO.getDtScId(),
                            parent.getCdtScAwdPriXpsTypeMapId(),
                            parent.getCodeListId(),
                            parent.getAgencyIdListId()
                    );
                } catch (EmptyResultDataAccessException e) {
                    System.err.println("#######BDT_SC_PRIMITIVE_RESTRICTION FROM BASE IS NOT POPULATED!! Check DT_SC_ID: " + dtscVO.getDtScId());
                }
            }

        } else { // else if (new SC)
            cdtscallowedprimitivelist = getCdtSCAllowedPrimitiveID(dtscVO.getDtScId());
            for (CoreDataTypeSupplementaryComponentAllowedPrimitive svo : cdtscallowedprimitivelist) {
                List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> maps = getCdtSCAPMap(svo.getCdtScAwdPriId());
                for (CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap vo : maps) {

                    try {
                        bdtScPriRestriRepository.findOneByBdtScIdAndCdtScAwdPriXpsTypeMapId(dtscVO.getDtScId(), vo.getCdtScAwdPriXpsTypeMapId());
                    } catch (EmptyResultDataAccessException e) {
                        System.err.println("#######BDT_SC_PRIMITIVE_RESTRICTION FROM ATTRIBUTES IS NOT POPULATED!! Check DT_SC_ID: " + dtscVO.getDtScId());
                    }
                }
            }

            if (type.contains("CodeContentType")) {
                try {
                    bdtScPriRestriRepository.findOneByBdtScIdAndCodeListId(
                            dtscVO.getDtScId(),
                            getCodeListId(type.substring(0, type.indexOf("CodeContentType"))));
                } catch (EmptyResultDataAccessException e) {
                    System.err.println("Error!" + e.getStackTrace()[0].getLineNumber());
                }
            }
            if (name.equalsIgnoreCase("listAgencyID")) {
                try {
                    bdtScPriRestriRepository.findOneByBdtScIdAndAgencyIdListId(
                            dtscVO.getDtScId(),
                            getAgencyListID());
                } catch (EmptyResultDataAccessException e) {
                    System.err.println("Error!" + e.getStackTrace()[0].getLineNumber());
                }
            }
        }
    }

    private DataType validateAddToDTForCodeContentQBDT(DataType queriedQBDataType, String type, Node typeNode, XPathHandler xHandler) throws Exception {

        String fromDataType = "";
        String fromXSD = "";

        Node nodeForDef = xHandler.getNode("//xsd:simpleType[@name='" + type + "']/xsd:annotation/xsd:documentation");

        String definitionFromXSD = null;
        if (nodeForDef != null) {
            definitionFromXSD = nodeForDef.getTextContent();
        }

        fromXSD = fromXSD + "type=1 version_number=1.0 previous_version_dt_id=0 data_type_term=Code den=" + type
                + " baseTypeGuid=oagis-id-5646bf52a97b48adb50ded6ff8c38354 definition=" + definitionFromXSD +
                " content_component_definition=null revision_doc=null state=3 created_by=1 owner_user_id=1" +
                " last_updated_by=1 revision_num=0 revision_tracking_num=0 revision_action_id=0 release_id=0 current_bdt_id=0 is_deprecated=false";
        //0 in id value means null in database

        //get from the VO
        //create a whole string
        //type = 1
        //version_number = 1.0
        //previous_version_dt_id = null
        //data type term = Code
        //qualifier = 
        //guid = oagis-id-5646bf52a97b48adb50ded6ff8c38354
        //den = 
        //content_component_den = 
        //

        String denFromDataType = queriedQBDataType.getContentComponentDen();

        denFromDataType = denFromDataType.replaceAll("_", "");

        denFromDataType = denFromDataType.replaceAll(" ", "");

        denFromDataType = denFromDataType.replace(".Content", "Type");


        //To validate bdt_pri_restri,
        //check it inherit base's bdt_pri_restri successfully (except id itself)
        DataType baseDataType = dataTypeRepository.findOne(queriedQBDataType.getBasedDtId());

        fromDataType = fromDataType + "type=" + queriedQBDataType.getType()
                + " version_number=" + queriedQBDataType.getVersionNum()
                + " previous_version_dt_id=" + queriedQBDataType.getPreviousVersionDtId()
                + " data_type_term=" + queriedQBDataType.getDataTypeTerm()
                + " den=" + denFromDataType
                + " baseTypeGuid=" + baseDataType.getGuid()
                + " definition=" + queriedQBDataType.getDefinition()
                + " content_component_definition=" + queriedQBDataType.getContentComponentDefinition()
                + " revision_doc=" + queriedQBDataType.getRevisionDoc()
                + " state=" + queriedQBDataType.getState()
                + " created_by=" + queriedQBDataType.getCreatedBy()
                + " owner_user_id=" + queriedQBDataType.getOwnerUserId()
                + " last_updated_by=" + queriedQBDataType.getLastUpdatedBy()
                + " revision_num=" + queriedQBDataType.getRevisionNum()
                + " revision_tracking_num=" + queriedQBDataType.getRevisionTrackingNum()
                + " revision_action_id=" + queriedQBDataType.getRevisionAction()
                + " release_id=" + queriedQBDataType.getReleaseId()
                + " current_bdt_id=" + queriedQBDataType.getCurrentBdtId()
                + " is_deprecated=" + queriedQBDataType.isDeprecated();


        if (!fromXSD.equals(fromDataType)) {
            System.out.println("########CodeContentType QBDT is not valid in some fields!!###########");
            System.out.println(" XSD: " + fromXSD);
            System.out.println("DataType: " + fromDataType);
        }

        validateInsertQBDTPrimitiveRestriction(queriedQBDataType, "CodeContentType");

        return queriedQBDataType;
    }

    private DataType validateAddToDTForIDContentQBDT(DataType queriedQBDataType, String type, Node typeNode, XPathHandler xHandler) throws Exception {

        String fromDataType = "";
        String fromXSD = "";

        Node nodeForDef = xHandler.getNode("//xsd:simpleType[@name='" + type + "']/xsd:annotation/xsd:documentation");

        String definitionFromXSD = null;
        if (nodeForDef != null) {
            definitionFromXSD = nodeForDef.getTextContent();
        }

        fromXSD = fromXSD + "type=1 version_number=1.0 previous_version_dt_id=0 data_type_term=Identifier den=" + type
                + " baseTypeGuid=oagis-id-08d6ade226fd42488b53c0815664e246 definition=" + definitionFromXSD +
                " content_component_definition=null revision_doc=null state=3 created_by=1 owner_user_id=1" +
                " last_updated_by=1 revision_num=0 revision_tracking_num=0 revision_action_id=0 release_id=0 current_bdt_id=0 is_deprecated=false";
        //0 in id value means null in database

        //get from the VO
        //create a whole string
        //type = 1
        //version_number = 1.0
        //previous_version_dt_id = null
        //data type term = Identifier
        //qualifier = 
        //guid = oagis-id-08d6ade226fd42488b53c0815664e246
        //den = 
        //content_component_den = 
        //

        String denFromDataType = queriedQBDataType.getContentComponentDen();
        denFromDataType = denFromDataType.replaceAll("_", "");
        denFromDataType = denFromDataType.replaceAll("Identifier", "ID");
        denFromDataType = denFromDataType.replaceAll(" ", "");
        denFromDataType = denFromDataType.replace(".Content", "Type");

        DataType baseDataType = dataTypeRepository.findOne(queriedQBDataType.getBasedDtId());

        fromDataType = fromDataType + "type=" + queriedQBDataType.getType()
                + " version_number=" + queriedQBDataType.getVersionNum()
                + " previous_version_dt_id=" + queriedQBDataType.getPreviousVersionDtId()
                + " data_type_term=" + queriedQBDataType.getDataTypeTerm()
                + " den=" + denFromDataType
                + " baseTypeGuid=" + baseDataType.getGuid()
                + " definition=" + queriedQBDataType.getDefinition()
                + " content_component_definition=" + queriedQBDataType.getContentComponentDefinition()
                + " revision_doc=" + queriedQBDataType.getRevisionDoc()
                + " state=" + queriedQBDataType.getState()
                + " created_by=" + queriedQBDataType.getCreatedBy()
                + " owner_user_id=" + queriedQBDataType.getOwnerUserId()
                + " last_updated_by=" + queriedQBDataType.getLastUpdatedBy()
                + " revision_num=" + queriedQBDataType.getRevisionNum()
                + " revision_tracking_num=" + queriedQBDataType.getRevisionTrackingNum()
                + " revision_action_id=" + queriedQBDataType.getRevisionAction()
                + " release_id=" + queriedQBDataType.getReleaseId()
                + " current_bdt_id=" + queriedQBDataType.getCurrentBdtId()
                + " is_deprecated=" + queriedQBDataType.isDeprecated();


        if (!fromXSD.equals(fromDataType)) {
            System.out.println("########IDContentType QBDT is not valid in some fields!!###########");
            System.out.println(" XSD: " + fromXSD);
            System.out.println("DataType: " + fromDataType);
        }

        validateInsertQBDTPrimitiveRestriction(queriedQBDataType, "IDContentType");

        return queriedQBDataType;
    }

    private DataType validateAddToDTForOtherQBDT(DataType queriedQBDataType, String type, Node typeNode, XPathHandler xHandler) throws Exception {

//			Element extension = (Element)((Element)typeNode).getElementsByTagName("xsd:extension").item(0);
//			if(extension == null || extension.getAttribute("base") == null)
//				return null;
//			String base = extension.getAttribute("base");
//			
//			if(type.endsWith("CodeContentType")) {
//				dVO = getDataTypeWithDen("Code. Type");
//			} else {
//				String den = Utility.typeToDen(type);
//				dVO = getDataTypeWithDen(den);
//				
//				// QBDT is based on another QBDT
//				if(dVO != null) {
//
//					System.out.println("DTT: "+dVO.getDataTypeTerm() + "   Qualifier: "+dVO.getQualifier() + "   den:" + dVO.getDen());
//					;//System.out.println("Success!!");
//				}
//				else {
//					System.out.println("Add to DT Error! DTT: "+dVO.getDataTypeTerm() + "   Qualifier: "+dVO.getQualifier() + "   den:" + dVO.getDen());
//					return null;
//				}
//			}


        String fromDataType = "";
        String fromXSD = "";

        Node nodeForDef = xHandler.getNode("//xsd:simpleType[@name='" + type + "']/xsd:annotation/xsd:documentation");
        if (nodeForDef == null) {
            nodeForDef = xHandler.getNode("//xsd:complexType[@name='" + type + "']/xsd:annotation/xsd:documentation");
        }

        String definitionFromXSD = null;


//			if(nodeForDef!=null){
//				definitionFromXSD = nodeForDef.getTextContent();
//			}
//			//Most QBDT has no definition. 
//			//Later on we want to come back and modify this to take definition from an element with the same name. 
//			//Right now let�s leave it blank so that it is easier to validate the import.

        String baseTypeGUIDFromXSD = "";
        String basdTypeDataTypeTerm = "";

        Node baseNameNode = xHandler.getNode("//xsd:simpleType[@name='" + type + "']//@base");
        if (baseNameNode == null) {
            baseNameNode = xHandler.getNode("//xsd:complexType[@name='" + type + "']//@base");
        }
        String baseTypeNodeName = baseNameNode.getTextContent();
        String dataTypeTerm = "";
        if (baseTypeNodeName.contains("Code")) {
            dataTypeTerm = "Code";
        } else if (baseTypeNodeName.contains("String")) {
            dataTypeTerm = "Text";
        } else {
            dataTypeTerm = Utility.spaceSeparatorBeforeStr(baseTypeNodeName, "Type");
        }


//			Node baseTypeNode = xHandler.getNode("//xsd:simpleType[@name='"+baseTypeNodeName+"']");
//			if(baseTypeNode==null){
//				baseTypeNode = xHandler.getNode("//xsd:complexType[@name='"+baseTypeNodeName+"']");
//			}
//			
//			baseTypeGUIDFromXSD = ((Element)baseTypeNode).getAttribute("id");

        if (baseTypeNodeName.endsWith("CodeContentType")) {
            baseTypeNodeName = "CodeType";
        }

        fromXSD = fromXSD + "type=1 version_number=1.0 previous_version_dt_id=0 data_type_term=" + dataTypeTerm
                + " den=" + type
                + " baseTypeName=" + baseTypeNodeName + " definition=" + definitionFromXSD +
                " content_component_definition=null revision_doc=null state=3 created_by=1 owner_user_id=1" +
                " last_updated_by=1 revision_num=0 revision_tracking_num=0 revision_action_id=0 release_id=0 current_bdt_id=0 is_deprecated=false";

        //0 in id value means null in database

        String denFromDataType = queriedQBDataType.getContentComponentDen();
        denFromDataType = denFromDataType.replaceAll("_", "");
        denFromDataType = denFromDataType.replaceAll("Identifier", "ID");
        denFromDataType = denFromDataType.replaceAll(" ", "");
        denFromDataType = denFromDataType.replace(".Content", "Type");

        if (!denFromDataType.equals("OpenTextType")) {
            denFromDataType = denFromDataType.replaceFirst("Text", "");
        }
        if (denFromDataType.contains("String")) {
            denFromDataType = denFromDataType.replace("String", "");
        }

        DataType baseDataType = dataTypeRepository.findOne(queriedQBDataType.getBasedDtId());
        String baseTypeName = "";
        baseTypeName = baseDataType.getContentComponentDen();
        baseTypeName = baseTypeName.replaceAll("_", "");
        baseTypeName = baseTypeName.replaceAll("Identifier", "ID");
        baseTypeName = baseTypeName.replaceAll(" ", "");
        baseTypeName = baseTypeName.replace(".Content", "Type");

        fromDataType = fromDataType + "type=" + queriedQBDataType.getType()
                + " version_number=" + queriedQBDataType.getVersionNum()
                + " previous_version_dt_id=" + queriedQBDataType.getPreviousVersionDtId()
                + " data_type_term=" + queriedQBDataType.getDataTypeTerm()
                + " den=" + denFromDataType
                + " baseTypeName=" + baseTypeName
                + " definition=" + queriedQBDataType.getDefinition()
                + " content_component_definition=" + queriedQBDataType.getContentComponentDefinition()
                + " revision_doc=" + queriedQBDataType.getRevisionDoc()
                + " state=" + queriedQBDataType.getState()
                + " created_by=" + queriedQBDataType.getCreatedBy()
                + " owner_user_id=" + queriedQBDataType.getOwnerUserId()
                + " last_updated_by=" + queriedQBDataType.getLastUpdatedBy()
                + " revision_num=" + queriedQBDataType.getRevisionNum()
                + " revision_tracking_num=" + queriedQBDataType.getRevisionTrackingNum()
                + " revision_action_id=" + queriedQBDataType.getRevisionAction()
                + " release_id=" + queriedQBDataType.getReleaseId()
                + " current_bdt_id=" + queriedQBDataType.getCurrentBdtId()
                + " is_deprecated=" + queriedQBDataType.isDeprecated();


        if (!fromXSD.equals(fromDataType)) {
            System.out.println("########Other QBDT is not valid in some fields!!###########");
            System.out.println(" XSD: " + fromXSD);
            System.out.println("DataType: " + fromDataType);
        }

        DataType res = queriedQBDataType;
        validateInsertQBDTPrimitiveRestriction(res, baseTypeName);
        return res;

    }

    private void validateInsertQBDTPrimitiveRestriction(DataType dVO, String base) {
        //To validate bdt_pri_restri,
        //check it inherit base's bdt_pri_restri successfully (except id itself)

        List<BusinessDataTypePrimitiveRestriction> baseBDTPriRestriList = bdtPriRestriRepository.findByBdtId(dVO.getBasedDtId());
        //get base's bdt_pri_restri records

        List<BusinessDataTypePrimitiveRestriction> thisBDTPriRestriList = bdtPriRestriRepository.findByBdtId(dVO.getDtId());
        //get this's bdt_pri_restri records

        //if CodeContentType QBDT-->inherit from CodeContentType & add codeListID  
        //if IDContentType QBDT-->inherit from IDContentType & add agencyIDListID 
        //Assume there is no qualified CodeContentType is based on another qualified CodeContentType.

        if (dVO.getDataTypeTerm().equals("Code")
                && base.equals("CodeContentType")) {
            for (int i = 0; i < baseBDTPriRestriList.size(); i++) {
                BusinessDataTypePrimitiveRestriction baseBDT_pri_restriVO = new BusinessDataTypePrimitiveRestriction();
                baseBDT_pri_restriVO = baseBDTPriRestriList.get(i);
                boolean exist = false;
                int duplicated = -1;
                int addedIndex = -1;
                int isDefaultCnt = 0;
                for (int j = 0; j < thisBDTPriRestriList.size(); j++) {
                    BusinessDataTypePrimitiveRestriction BDT_pri_restriVO = new BusinessDataTypePrimitiveRestriction();
                    BDT_pri_restriVO = (BusinessDataTypePrimitiveRestriction) thisBDTPriRestriList.get(j);

                    if (baseBDT_pri_restriVO.getCdtAwdPriXpsTypeMapId() == BDT_pri_restriVO.getCdtAwdPriXpsTypeMapId()
                            && baseBDT_pri_restriVO.getCodeListId() == BDT_pri_restriVO.getCodeListId()
                            && baseBDT_pri_restriVO.getAgencyIdListId() == BDT_pri_restriVO.getAgencyIdListId()
                            && baseBDT_pri_restriVO.isDefault() == BDT_pri_restriVO.isDefault()) {
                        exist = true;
                        duplicated++;
                        if (BDT_pri_restriVO.isDefault()) {
                            isDefaultCnt++;
                        }
                        thisBDTPriRestriList.remove(j);
                        j = 0;
                    }
                }

                if (!exist) {
                    System.out.println("Not Inherited from Base. Check DT_ID:" + dVO.getDtId());
                }
                if (duplicated > 0) {
                    System.out.println("Duplicated. Check DT_ID:" + dVO.getDtId());
                }
                if (isDefaultCnt > 1) {
                    System.out.println("There are " + isDefaultCnt + " defaults. Check DT_ID:" + dVO.getDtId());
                }
            }
            if (thisBDTPriRestriList.size() > 0) {
                System.out.println("CodeContentType QBDT: " + dVO.getDtId() + " has added record in bdt_pri_restri");
                for (int j = 0; j < thisBDTPriRestriList.size(); j++) {
                    BusinessDataTypePrimitiveRestriction addedRecord = new BusinessDataTypePrimitiveRestriction();
                    addedRecord = (BusinessDataTypePrimitiveRestriction) thisBDTPriRestriList.get(j);
                    System.out.println(addedRecord.getCdtAwdPriXpsTypeMapId() + " "
                            + addedRecord.getCodeListId() + " "
                            + addedRecord.getAgencyIdListId() + " "
                            + addedRecord.isDefault());
                }
            }
        } else if (dVO.getDataTypeTerm().equals("Identifier")
                && base.equals("IDContentType")) {
            for (int i = 0; i < baseBDTPriRestriList.size(); i++) {
                BusinessDataTypePrimitiveRestriction baseBDT_pri_restriVO = new BusinessDataTypePrimitiveRestriction();
                baseBDT_pri_restriVO = (BusinessDataTypePrimitiveRestriction) baseBDTPriRestriList.get(i);
                boolean exist = false;
                int duplicated = -1;
                int addedIndex = -1;
                int isDefaultCnt = 0;
                for (int j = 0; j < thisBDTPriRestriList.size(); j++) {
                    BusinessDataTypePrimitiveRestriction BDT_pri_restriVO = new BusinessDataTypePrimitiveRestriction();
                    BDT_pri_restriVO = (BusinessDataTypePrimitiveRestriction) thisBDTPriRestriList.get(j);

                    if (baseBDT_pri_restriVO.getCdtAwdPriXpsTypeMapId() == BDT_pri_restriVO.getCdtAwdPriXpsTypeMapId()
                            && baseBDT_pri_restriVO.getCodeListId() == BDT_pri_restriVO.getCodeListId()
                            && baseBDT_pri_restriVO.getAgencyIdListId() == BDT_pri_restriVO.getAgencyIdListId()
                            && baseBDT_pri_restriVO.isDefault() == BDT_pri_restriVO.isDefault()) {
                        exist = true;
                        duplicated++;
                        if (BDT_pri_restriVO.isDefault()) {
                            isDefaultCnt++;
                        }
                        thisBDTPriRestriList.remove(j);
                        j = 0;
                    }
                }

                if (!exist) {
                    System.out.println("Not Inherited from Base. Check DT_ID:" + dVO.getDtId());
                }
                if (duplicated > 0) {
                    System.out.println("Duplicated. Check DT_ID:" + dVO.getDtId());
                }
                if (isDefaultCnt > 1) {
                    System.out.println("There are " + isDefaultCnt + " defaults. Check DT_ID:" + dVO.getDtId());
                }
            }
            if (thisBDTPriRestriList.size() > 0) {
                System.out.println("IDContentType QBDT: " + dVO.getDtId() + " has added record in bdt_pri_restri");
                for (int j = 0; j < thisBDTPriRestriList.size(); j++) {
                    BusinessDataTypePrimitiveRestriction addedRecord = new BusinessDataTypePrimitiveRestriction();
                    addedRecord = (BusinessDataTypePrimitiveRestriction) thisBDTPriRestriList.get(j);
                    System.out.println(addedRecord.getCdtAwdPriXpsTypeMapId() + " "
                            + addedRecord.getCodeListId() + " "
                            + addedRecord.getAgencyIdListId() + " "
                            + addedRecord.isDefault());
                }
            }
        } else {
            for (int i = 0; i < baseBDTPriRestriList.size(); i++) {
                BusinessDataTypePrimitiveRestriction baseBDT_pri_restriVO = new BusinessDataTypePrimitiveRestriction();
                baseBDT_pri_restriVO = (BusinessDataTypePrimitiveRestriction) baseBDTPriRestriList.get(i);
                boolean exist = false;
                int duplicated = -1;
                int addedIndex = -1;
                int isDefaultCnt = 0;
                for (int j = 0; j < thisBDTPriRestriList.size(); j++) {
                    BusinessDataTypePrimitiveRestriction BDT_pri_restriVO = new BusinessDataTypePrimitiveRestriction();
                    BDT_pri_restriVO = (BusinessDataTypePrimitiveRestriction) thisBDTPriRestriList.get(j);

                    if (baseBDT_pri_restriVO.getCdtAwdPriXpsTypeMapId() == BDT_pri_restriVO.getCdtAwdPriXpsTypeMapId()
                            && baseBDT_pri_restriVO.getCodeListId() == BDT_pri_restriVO.getCodeListId()
                            && baseBDT_pri_restriVO.getAgencyIdListId() == BDT_pri_restriVO.getAgencyIdListId()
                            && baseBDT_pri_restriVO.isDefault() == BDT_pri_restriVO.isDefault()) {
                        exist = true;
                        duplicated++;
                        if (BDT_pri_restriVO.isDefault()) {
                            isDefaultCnt++;
                        }
                        thisBDTPriRestriList.remove(j);
                        j = 0;
                    }
                }

                if (!exist) {
                    System.out.println("Not Inherited from Base. Check DT_ID:" + dVO.getDtId());
                }
                if (duplicated > 0) {
                    System.out.println("Duplicated. Check DT_ID:" + dVO.getDtId());
                }
                if (isDefaultCnt > 1) {
                    System.out.println("There are " + isDefaultCnt + " defaults. Check DT_ID:" + dVO.getDtId());
                }
            }
            if (thisBDTPriRestriList.size() > 0) {
                System.out.println("Other QBDT: " + dVO.getDtId() + " has added record in bdt_pri_restri");
                for (int j = 0; j < thisBDTPriRestriList.size(); j++) {
                    BusinessDataTypePrimitiveRestriction addedRecord = new BusinessDataTypePrimitiveRestriction();
                    addedRecord = (BusinessDataTypePrimitiveRestriction) thisBDTPriRestriList.get(j);
                    System.out.println(addedRecord.getCdtAwdPriXpsTypeMapId() + " "
                            + addedRecord.getCodeListId() + " "
                            + addedRecord.getAgencyIdListId() + " "
                            + addedRecord.isDefault());
                }
            }
        }

//		
//		if(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
//			QueryCondition qc2 = new QueryCondition();
//
//			BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
//			theBDT_Primitive_RestrictionVO.setBDtId(dVO.getDtId());
//			if(base.endsWith("CodeContentType")) {
//				qc2.add("code_list_id", getCodeListId(base.substring(0, base.indexOf("CodeContentType"))));
//			} else {
//				for(SRTObject aSRTObject : al) {
//					BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction = (BusinessDataTypePrimitiveRestriction)aSRTObject;
//					if(aBusinessDataTypePrimitiveRestriction.getCodeListId() > 0) {
//						qc2.add("code_list_id", aBusinessDataTypePrimitiveRestriction.getCodeListId());
//						break;
//					}
//				}
//			}
//			if(BDTPriRestrisVOofBase.findObject(qc2, conn) == null)
//				System.out.println("Error! BDT Primitive Restriction doesn't exist!"+new Exception().getStackTrace()[0].getLineNumber());
//			else
//				;//System.out.println("Success!!");
//		} 
//		
//		if(!dVO.getDataTypeTerm().equalsIgnoreCase("Code") || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType")) || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.endsWith("CodeContentType"))){
//			for(SRTObject aSRTObject : al) {
//				BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction = (BusinessDataTypePrimitiveRestriction)aSRTObject;
//				QueryCondition qc2 = new QueryCondition();
//				qc2.add("bdt_id", dVO.getDtId());
//				qc2.add("cdt_awd_pri_xps_type_map_id", aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
//
//				if(BDTPriRestrisVOofBase.findObject(qc2, conn) == null)
//					System.out.println("Error! BDT Primitive Restriction doesn't exist!"+new Exception().getStackTrace()[0].getLineNumber());
//				else
//					;//System.out.println("Success!!");
//			}
//		}
    }

//	
//	private void validateInsertQBDTPrimitiveRestriction(DataType dVO, String base)  {
//		DAOFactory df = DAOFactory.getDAOFactory();
//		SRTDAO BDTPriRestrisVO = df.getDAO("BDTPrimitiveRestriction");
//		
//		//To validate bdt_pri_restri,
//		//check it inherit base's bdt_pri_restri successfully (except id itself)
//		
//		QueryCondition qc = new QueryCondition();
//		qc.add("bdt_id", dVO.getBasedDtId());
//		ArrayList<SRTObject> baseBDTPriRestriList = BDTPriRestrisVO.findObjects(qc, conn);
//		//get base's bdt_pri_restri records
//		
//		
//		//if CodeContentType QBDT-->inherit from CodeContentType & add codeListID  
//		//if IDContentType QBDT-->inherit from IDContentType & add agencyIDListID 
//		//Assume there is no qualified CodeContentType is based on another qualifed CodeContentType.
//		
//		
//		if(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
//			QueryCondition qc2 = new QueryCondition();
//
//			BusinessDataTypePrimitiveRestriction theBDT_Primitive_RestrictionVO = new BusinessDataTypePrimitiveRestriction();
//			theBDT_Primitive_RestrictionVO.setBDtId(dVO.getDtId());
//			if(base.endsWith("CodeContentType")) {
//				qc2.add("code_list_id", getCodeListId(base.substring(0, base.indexOf("CodeContentType"))));
//			} else {
//				for(SRTObject aSRTObject : baseBDTPriRestriList) {
//					BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction = (BusinessDataTypePrimitiveRestriction)aSRTObject;
//					if(aBusinessDataTypePrimitiveRestriction.getCodeListId() > 0) {
//						qc2.add("code_list_id", aBusinessDataTypePrimitiveRestriction.getCodeListId());
//						break;
//					}
//				}
//			}
//			if(BDTPriRestrisVO.findObject(qc2, conn) == null)
//				System.out.println("Error! BDT Primitive Restriction doesn't exist!"+new Exception().getStackTrace()[0].getLineNumber());
//			else
//				;//System.out.println("Success!!");
//		} 
//		
//		if(!dVO.getDataTypeTerm().equalsIgnoreCase("Code") || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType")) || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.endsWith("CodeContentType"))){
//			for(SRTObject aSRTObject : baseBDTPriRestriList) {
//				BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction = (BusinessDataTypePrimitiveRestriction)aSRTObject;
//				QueryCondition qc2 = new QueryCondition();
//				qc2.add("bdt_id", dVO.getDtId());
//				qc2.add("cdt_awd_pri_xps_type_map_id", aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId());
//
//				if(BDTPriRestrisVO.findObject(qc2, conn) == null)
//					System.out.println("Error! BDT Primitive Restriction doesn't exist!"+new Exception().getStackTrace()[0].getLineNumber());
//				else
//					;//System.out.println("Success!!");
//			}
//		}
//	}

    private void validateAddToDTSCForContentType(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {
        //if QBDT is based on CodeContentType or IDContentType, check inherit sc of base & sc max card = 0
        if (!qbdtVO.getDen().contains("_"))
            qbdtVO = getDataType(qbdtVO.getBasedDtId());

        int owner_dT_iD = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> basedtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());
        for (DataTypeSupplementaryComponent basedtsc_vo : basedtsc_vos) {
            DataTypeSupplementaryComponent thisdtsc_vo = dtScRepository.findOneByOwnerDtIdAndBasedDtScId(qbdtVO.getDtId(), basedtsc_vo.getDtScId());

            String fromBaseDTSCStr = "";
            String thisDTSCStr = "";

            fromBaseDTSCStr = fromBaseDTSCStr + basedtsc_vo.getGuid() + basedtsc_vo.getPropertyTerm()
                    + basedtsc_vo.getRepresentationTerm() + basedtsc_vo.getDefinition() + basedtsc_vo.getMinCardinality() + "0"//max card = 0 for contentType
                    + basedtsc_vo.getDtScId();//Based_dt_sc_id is dt_sc_id of base 

            thisDTSCStr = thisDTSCStr + thisdtsc_vo.getGuid() + thisdtsc_vo.getPropertyTerm()
                    + thisdtsc_vo.getRepresentationTerm() + thisdtsc_vo.getDefinition() + thisdtsc_vo.getMinCardinality() + thisdtsc_vo.getMaxCardinality()
                    + thisdtsc_vo.getBasedDtScId();

            if (!fromBaseDTSCStr.equals(thisDTSCStr)) {
                System.out.println("############DTSC IS NOT GENERATED! Check DT_ID: " + thisdtsc_vo.getOwnerDtId() + " & its base DT_SC_ID: " + thisdtsc_vo.getBasedDtScId());
                ;
            }

            validateInsertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(basedtsc_vo.getGuid(), owner_dT_iD), 1, "", "");

        }
    }

    private void validateAddToDTSC(XPathHandler xHandler, String typeName, DataType qbdtVO) throws Exception {
        if (!qbdtVO.getDen().contains("_"))
            qbdtVO = getDataType(qbdtVO.getBasedDtId());

        int owner_dT_iD = qbdtVO.getDtId();

        List<DataTypeSupplementaryComponent> dtsc_vos = dtScRepository.findByOwnerDtId(qbdtVO.getBasedDtId());
        for (DataTypeSupplementaryComponent dtsc_vo : dtsc_vos) {
            validateInsertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dtsc_vo.getGuid(), owner_dT_iD), 1, "", "");
        }

        NodeList attributeList = xHandler.getNodeList("//xsd:complexType[@id = '" + qbdtVO.getGuid() + "']/xsd:simpleContent/xsd:extension/xsd:attribute");

        if (attributeList == null || attributeList.getLength() == 0) {
        } else {
            String dt_sc_guid = "";
            String property_term = "";
            String representation_term = "";

            String definition;
            int min_cardinality = 0;
            int max_cardinality = 0;

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

                DataTypeSupplementaryComponent duplicate = checkDuplicate(vo);
                if (duplicate != null) {
                    DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByGuidAndOwnerDtId(vo.getGuid(), vo.getOwnerDtId());
                    String representationTerm = dtscVO.getRepresentationTerm();
                    DataType dtVO = getDataTypeWithRepresentationTerm(representationTerm);

                    CoreDataTypeSupplementaryComponentAllowedPrimitive cdtSCAllowedVO = new CoreDataTypeSupplementaryComponentAllowedPrimitive();
                    cdtSCAllowedVO.setCdtScId(dtscVO.getDtScId());
                    List<CoreDataTypeAllowedPrimitive> cdtallowedprimitivelist = getCDTAllowedPrimitiveIDs(dtVO.getDtId());

                    for (CoreDataTypeAllowedPrimitive svo : cdtallowedprimitivelist) {
                        cdtSCAllowedVO.setCdtPriId(svo.getCdtPriId());

                        if (cdtScAwdPriRepository.findByCdtPriId(svo.getCdtPriId()).isEmpty())
                            System.err.println("Error!" + new Exception().getStackTrace()[0].getLineNumber());
                        else
                            ;//System.out.println("Success!!");

                        int cdtSCAllowedPrimitiveId = cdtScAwdPriRepository.findOneByCdtScIdAndCdtPriId(cdtSCAllowedVO.getCdtScId(), cdtSCAllowedVO.getCdtPriId()).getCdtScAwdPriId();
                        List<String> xsdbs = Types.getCorrespondingXSDBuiltType(getPrimitiveName(cdtSCAllowedVO.getCdtPriId()));
                        for (String xbt : xsdbs) {
                            int xdtBuiltTypeId = xbtRepository.findOneByBuiltInType(xbt).getXbtId();

                            try {
                                cdtScAwdPriXpsTypeMapRepository.findOneByCdtScAwdPriAndXbtId(cdtSCAllowedPrimitiveId, xdtBuiltTypeId);
                            } catch (EmptyResultDataAccessException e) {
                                System.err.println("Error!" + new Exception().getStackTrace()[0].getLineNumber());
                            }
                        }
                    }

                    validateInsertBDTSCPrimitiveRestriction(getDataTypeSupplementaryComponent(dt_sc_guid), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
                } else {
                    ;//System.out.println("No prob!!");
                }
            }
        }
    }

    private List<CoreDataTypeSupplementaryComponentAllowedPrimitiveExpressionTypeMap> getCdtSCAPMap(int cdtSCAllowedPrimitiveId) {
        return cdtScAwdPriXpsTypeMapRepository.findByCdtScAwdPri(cdtSCAllowedPrimitiveId);
    }

    private DataTypeSupplementaryComponent checkDuplicate(DataTypeSupplementaryComponent dtVO) {
        return dtScRepository.findOneByOwnerDtIdAndPropertyTermAndRepresentationTerm(dtVO.getOwnerDtId(), dtVO.getPropertyTerm(), dtVO.getRepresentationTerm());
    }

    public int getCodeListId(String codeName) {
        CodeList codelistVO = codeListRepository.findByNameContaining(codeName.trim()).get(0);
        return codelistVO.getCodeListId();
    }

    public int getAgencyListID() {
        AgencyIdList agencyidlistVO = agencyIdListRepository.findOneByName("Agency Identification");
        return agencyidlistVO.getAgencyIdListId();
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid) {
        return dtScRepository.findOneByGuid(guid);
    }

    public DataTypeSupplementaryComponent getDataTypeSupplementaryComponent(String guid, int ownerId) {
        return dtScRepository.findOneByGuidAndOwnerDtId(guid, ownerId);
    }

    public int getDtId(String DataTypeTerm) {
        DataType dtVO = dataTypeRepository.findOneByDataTypeTermAndType(DataTypeTerm, 0);
        int id = dtVO.getDtId();
        return id;
    }

    public int getCdtScId(int DtScId) {
        CoreDataTypeSupplementaryComponentAllowedPrimitive cdtVO =
                cdtScAwdPriRepository.findByCdtScId(DtScId).get(0);
        int id = cdtVO.getCdtScId();
        return id;
    }

    public String getRepresentationTerm(String Guid) {
        DataTypeSupplementaryComponent dtscVO = dtScRepository.findOneByGuid(Guid);
        String term = dtscVO.getRepresentationTerm();
        return term;
    }

    public String getPrimitiveName(int CDTPrimitiveID) {
        return cdtPriRepository.findOne(CDTPrimitiveID).getName();
    }

    public List<CoreDataTypeAllowedPrimitive> getCDTAllowedPrimitiveIDs(int cdt_id) {
        return cdtAwdPriRepository.findByCdtId(cdt_id);
    }

    public List<CoreDataTypeSupplementaryComponentAllowedPrimitive> getCdtSCAllowedPrimitiveID(int dt_sc_id) {
        List<CoreDataTypeSupplementaryComponentAllowedPrimitive> res = cdtScAwdPriRepository.findByCdtScId(dt_sc_id);
        if (res.isEmpty()) {
            DataTypeSupplementaryComponent dtscVO = dtScRepository.findOne(dt_sc_id);
            res = getCdtSCAllowedPrimitiveID(dtscVO.getBasedDtScId());
        }
        return res;
    }

    public int getXSDBuiltInTypeID(String BuiltIntype) {
        return xbtRepository.findOneByBuiltInType(BuiltIntype).getXbtId();
    }

    private DataType getDataTypeWithDen(String den) {
        if (den.equalsIgnoreCase("MatchCode. Type"))
            den = "Match_ Code. Type";
        else if (den.equalsIgnoreCase("Description. Type"))
            den = "Description_ Text. Type";

        return dataTypeRepository.findOneByTypeAndDen(1, den);
    }

    private DataType getDataTypeWithRepresentationTerm(String representationTerm) {
        return dataTypeRepository.findOneByDataTypeTermAndType(representationTerm, 0);
    }

    private DataType getDataType(int dtid) {
        return dataTypeRepository.findOne(dtid);
    }

    private void validate_bdt_pri_resti(String datatype) {
        DataType dataType = dataTypeRepository.findByDen(Utility.typeToDen(datatype)).get(0);
        int bdt_id = dataType.getDtId();

        List<BusinessDataTypePrimitiveRestriction> bdt_pri_resti_list = bdtPriRestriRepository.findByBdtId(bdt_id);

        List<String> bdt_pri_list = new ArrayList<String>();

        if (dataType.getDen().equals("Amount_0723C8. Type")) {
            bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeDecimalxsd:decimal");
            bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeDoublexsd:double");
            bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeDoublexsd:float");
            bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeFloatxsd:float");
            bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeIntegerxsd:integer");
            bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeIntegerxsd:nonNegativeInteger");
            bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeIntegerxsd:positiveInteger");
        } else if (dataType.getDen().equals("Code_1DEB05. Type")) {
            bdt_pri_list.add("Code_1DEB05. TypeCode. TypeNormalizedStringxsd:normalizedString");
            bdt_pri_list.add("Code_1DEB05. TypeCode. TypeStringxsd:string");
            bdt_pri_list.add("Code_1DEB05. TypeCode. TypeTokenxsd:token");
        }


        List<String> bdt_pri_fromDB = new ArrayList<String>();

        for (BusinessDataTypePrimitiveRestriction bdtpriresti : bdt_pri_resti_list) {
            BdtPriResti aBdtPriResti = new BdtPriResti(bdtpriresti.getBdtPriRestriId());
            if (bdt_pri_list.indexOf(aBdtPriResti.getData()) == -1)
                System.out.println("Data error in bdt_pri_resti. Data is " + aBdtPriResti.getData());
            bdt_pri_fromDB.add(aBdtPriResti.getData());
        }

        for (String bdtpri : bdt_pri_list) {
            if (bdt_pri_fromDB.indexOf(bdtpri) == -1)
                System.out.println("Data may be missing in the bdt_pri_resti table. Data is " + bdtpri);
        }
    }

    private void validate_default_bdt() {
        List<String> default_bdt = new ArrayList();
        default_bdt.add(Utility.typeToDen("AmountType_0723C8"));
        default_bdt.add(Utility.typeToDen("BinaryObjectType_290B4F"));
        default_bdt.add(Utility.typeToDen("GraphicType_CM6785"));
        default_bdt.add(Utility.typeToDen("SoundType_697AE6"));
        default_bdt.add(Utility.typeToDen("VideoType_539B44"));
        default_bdt.add(Utility.typeToDen("CodeType_1DEB05"));
        default_bdt.add(Utility.typeToDen("DateType_238C51"));
        default_bdt.add(Utility.typeToDen("DateTimeType_AD9DD9"));
        default_bdt.add(Utility.typeToDen("DurationType_JJ5401"));
        default_bdt.add(Utility.typeToDen("IDType_D995CD"));
        default_bdt.add(Utility.typeToDen("IndicatorType_CVW231"));
        default_bdt.add(Utility.typeToDen("MeasureType_671290"));
        default_bdt.add(Utility.typeToDen("NameType_02FC2Z"));
        default_bdt.add(Utility.typeToDen("NumberType_BE4776"));
        default_bdt.add(Utility.typeToDen("OrdinalType_PQALZM"));
        default_bdt.add(Utility.typeToDen("PercentType_481002"));
        default_bdt.add(Utility.typeToDen("QuantityType_201330"));
        default_bdt.add(Utility.typeToDen("TextType_62S0B4"));
        default_bdt.add(Utility.typeToDen("TimeType_100DCA"));
        default_bdt.add(Utility.typeToDen("ValueType_D19E7B"));
        default_bdt.add(Utility.typeToDen("CodeType_1E7368"));
        default_bdt.add(Utility.typeToDen("IDType_B3F14E"));
        default_bdt.add(Utility.typeToDen("ValueType_039C44"));
        default_bdt.add(Utility.typeToDen("DateType_DB95C8"));
        default_bdt.add(Utility.typeToDen("DateType_0C267D"));
        default_bdt.add(Utility.typeToDen("DateType_5B057B"));
        default_bdt.add(Utility.typeToDen("DateType_57D5E1"));
        default_bdt.add(Utility.typeToDen("DateType_BBCC14"));
        default_bdt.add(Utility.typeToDen("NumberType_B98233"));
        default_bdt.add(Utility.typeToDen("NumberType_201301"));
        default_bdt.add(Utility.typeToDen("TextType_62S0C1"));
        default_bdt.add(Utility.typeToDen("TextType_0VCBZ5"));
        default_bdt.add(Utility.typeToDen("TextType_0F0ZX1"));

        for (int i = 0; i < default_bdt.size(); i++) {
            if (dataTypeRepository.findByDen(default_bdt.get(i)).isEmpty()) {
                System.err.println("There is no default bdt in dt table : " + default_bdt.get(i));
            }
        }
    }

    class BdtPriResti {
        int id;
        String bdtDen;
        String cdtDen;
        String cdtPriTerm;
        String xsdBuiltInType;

        public BdtPriResti(int id) {
            List<BusinessDataTypePrimitiveRestriction> bdtPriRestriList =
                    bdtPriRestriRepository.findByCdtAwdPriXpsTypeMapId(id);
            BusinessDataTypePrimitiveRestriction aBusinessDataTypePrimitiveRestriction = (bdtPriRestriList.isEmpty()) ? null : bdtPriRestriList.get(0);
            int bdt_id = aBusinessDataTypePrimitiveRestriction.getBdtId();

            DataType aDataType = dataTypeRepository.findOne(bdt_id);

            bdtDen = aDataType.getDen();

            int cdt_awd_pri_xps_type_map_id = aBusinessDataTypePrimitiveRestriction.getCdtAwdPriXpsTypeMapId();

            CoreDataTypeAllowedPrimitiveExpressionTypeMap aCoreDataTypeAllowedPrimitiveExpressionTypeMap =
                    cdtAwdPriXpsTypeMapRepository.findOne(cdt_awd_pri_xps_type_map_id);

            int cdt_awd_pri_id = aCoreDataTypeAllowedPrimitiveExpressionTypeMap.getCdtAwdPriId();
            int xbt_id = aCoreDataTypeAllowedPrimitiveExpressionTypeMap.getXbtId();

            CoreDataTypeAllowedPrimitive cdtAllowedPrimitiveVO = cdtAwdPriRepository.findOne(cdt_awd_pri_id);

            CoreDataTypePrimitive cdtPrimitiveVO = cdtPriRepository.findOne(cdtAllowedPrimitiveVO.getCdtPriId());
            cdtPriTerm = cdtPrimitiveVO.getName();

            DataType cdtVO = dataTypeRepository.findOne(cdtAllowedPrimitiveVO.getCdtId());
            cdtDen = cdtVO.getDen();

            XSDBuiltInType xsdBuiltInTypeVO = xbtRepository.findOne(xbt_id);
            xsdBuiltInType = xsdBuiltInTypeVO.getBuiltInType();

        }

        public String getData() {
            return this.bdtDen + this.cdtDen + this.cdtPriTerm + this.xsdBuiltInType;
        }

    }

    private void validteDTSC() {
        List<DataTypeSupplementaryComponent> DTSCList = dtScRepository.findAll();
        Map<DataTypeSupplementaryComponent, Boolean> map = new HashMap();
        for (DataTypeSupplementaryComponent DTSC : DTSCList)
            map.put(DTSC, false);

        for (int i = 0; i < DTSCList.size(); i++) {
            DataTypeSupplementaryComponent DTSC = DTSCList.get(i);
            if (map.get(DTSC))
                continue;
            DataTypeSupplementaryComponent dtscvo = DTSC;
            if (dtscvo.getBasedDtScId() == 0)
                continue;
            int dt_id = dtscvo.getOwnerDtId();

            DataType ownerDT = dataTypeRepository.findOne(dt_id);
            if (ownerDT.getDen().contains("_")) {// if BDT is default BDT
                DataTypeSupplementaryComponent basedDTscvo = dtScRepository.findOne(dtscvo.getBasedDtScId());
                if (basedDTscvo.getOwnerDtId() != ownerDT.getBasedDtId())
                    System.out.println("DTSC based on default BDT is wrong when dt_sc_id = " + dtscvo.getDtScId());
                else
                    System.out.println("good when dt_sc_id = " + dtscvo.getDtScId());
                map.put(DTSC, true);

            } else { // if BDT is qualified BDT

            }
        }

    }

    public void run(ApplicationContext applicationContext) throws Exception {
        System.out.println("### DataType Validation Start");

        XPathHandler fields_xsd = new XPathHandler(SRTConstants.FIELDS_XSD_FILE_PATH);
        XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
        XPathHandler components_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);

//		for (int i = 0; i < Types.dataTypeList.length; i++){
//			validateImportDataTypeList(Types.dataTypeList[i]);
//		}
//		for (int i = 0; i < Types.dataTypeList.length; i++){
//			validateImportDataTypeList(Types.dataTypeList[i]);
//		}
//		
//		for (int i = 0; i < Types.defaultDataTypeList.length; i++){
//			validate_bdt_pri_resti(Types.defaultDataTypeList[i]);
//		}
//		
//		validate_default_bdt();
//		
//
//		validatePopulateAdditionalDefault_BDTStatement(fields_xsd);
//		validatePopulateAdditionalDefault_BDTStatement(meta_xsd);
//		validatePopulateAdditionalDefault_BDTStatement(components_xsd);
//
//		File f = new File(SRTConstants.NOUNS_FILE_PATH);
//		File[] listOfFiles = f.listFiles();
//		for (File file : listOfFiles) {
//		    if (file.isFile()) {
//		    	XPathHandler nouns_xsd = new XPathHandler(SRTConstants.NOUNS_FILE_PATH + file.getName());
//		    	validatePopulateAdditionalDefault_BDTStatement(nouns_xsd);
//		    }
//		}
//		validateImportExceptionalDataTypeList("ValueType_039C44");
//		validateImportAdditionalBDT();
        validateImportQBDT(fields_xsd);
//		validteDTSC();

        System.out.println("### DataType Validation End");
    }

    public static void main(String[] args) throws Exception {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(RepositoryConfig.class, ImportConfig.class)) {
            DataTypeTest dataTypeTest = ctx.getBean(DataTypeTest.class);
            dataTypeTest.run(ctx);
        }
    }
}
