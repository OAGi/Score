package org.oagi.srt.persistence.populate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.UserVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
*
* @author Nasif Sikder
* @author Jaehun Lee
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_5_1_to_2_PopulateBDTsInDT {
	
	
	public DTVO insertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition, String ccDefinition, String id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoUser = df.getDAO("User");
		
		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", dataTypeTerm);
		qc.add("type", new Integer(0));
		int basedDTID = ((DTVO)dao.findObject(qc, conn)).getDTID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", id);
		
		if(dao.findObject(qc1, conn) == null) {
	
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(id);
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			//dtVO.setRevisionType(0);
			dtVO.setDataTypeTerm(dataTypeTerm);
			dtVO.setBasedDTID(basedDTID);
			dtVO.setDEN(typeName + ". Type");
			dtVO.setContentComponentDEN(typeName + ". Content");
			dtVO.setDefinition(definition);
			dtVO.setContentComponentDefinition(ccDefinition);
			dtVO.setState(3);
		    QueryCondition qc_02 = new QueryCondition();
		    qc_02.add("login_id", "oagis");
			int userId = ((UserVO)daoUser.findObject(qc_02, conn)).getUserID();
			
			dtVO.setCreatedByUserId(userId);
			dtVO.setLastUpdatedByUserId(userId);
			dtVO.setOwnerUserId(userId);
			dtVO.setRevisionDocumentation("");
			dtVO.setRevisionNum(0);
			dtVO.setRevisionTrackingNum(0);
			dtVO.setRevisionAction(true);
			dtVO.setIs_deprecated(false);
	
			dao.insertObject(dtVO);
		}
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("guid", id);
		return (DTVO)dao.findObject(qc2, conn);
	}
	
	public void populateAdditionalDefault_BDTStatement(XPathHandler filename) throws SRTDAOException, XPathExpressionException, FileNotFoundException, ParserConfigurationException, SAXException, IOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		NodeList xsd_node = filename.getNodeList("//xsd:attribute");
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
		SRTDAO daoXSD = df.getDAO("XSDBuiltInType");
		for(int i = 0; i < xsd_node.getLength(); i++) {
			Element tmp = (Element)xsd_node.item(i);
			String typeName = tmp.getAttribute("type");
			
			String den = tmp.getAttribute("type").replaceAll("Type", "") + ". Type";
			
			QueryCondition qc1 = new QueryCondition();
			qc1.add("DEN", den);
			if(dao.findObject(qc1, conn) == null) { // && duplicate_check == false) {
				XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
				String type = "complex";
				String xsdTypeName = typeName;
				String dataTypeTerm;
				
				Node aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
				if(aNodeBDT == null) {
					type = "simple";
					aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
				}
				Element aElementBDT = (Element)aNodeBDT;
				
				if(aElementBDT != null) {
					//Data Type Term
					Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
					
					Element dataTypeTermElement = (Element)dataTypeTermNode;
					if(dataTypeTermElement != null) {
						dataTypeTerm = dataTypeTermElement.getTextContent();
						if (dataTypeTerm.length() > 5) if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
							dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
						//dataTypeTerm = dataTypeTerm.replaceAll(" Object", "");
					} else {
						dataTypeTerm = typeName.substring(0, typeName.indexOf("Type"));
					}
									
					//Definitions
					Node definitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
					Element definitionElement = (Element)definitionNode;
					Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
					if (ccDefinitionNode == null)
						ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
					if (ccDefinitionNode == null)
						ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
					Element ccDefinitionElement = (Element)ccDefinitionNode;
					
					Node union = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union");
					int defaultId = -1;
					
					if(union != null) {
						QueryCondition qc3 = new QueryCondition();
						qc3.add("name", "token");
						defaultId = ((XSDBuiltInTypeVO)daoXSD.findObject(qc3, conn)).getXSDBuiltInTypeID();
					} else {
						Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:extension");
						if(xsdTypeNameNode == null)
							xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:restriction");
						if(xsdTypeNameNode != null) {
							Element xsdTypeNameElement = (Element)xsdTypeNameNode;
							xsdTypeName = xsdTypeNameElement.getAttribute("base");	
							
							QueryCondition qc3 = new QueryCondition();
							qc3.add("builtin_type", xsdTypeName);
							defaultId = ((XSDBuiltInTypeVO)daoXSD.findObject(qc3, conn)).getXSDBuiltInTypeID();
							if(defaultId == 0) {
								Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
								QueryCondition qc4 = new QueryCondition();
								qc4.add("builtin_type", ((Element)xbtNode).getAttribute("base"));
								defaultId = ((XSDBuiltInTypeVO)daoXSD.findObject(qc4, conn)).getXSDBuiltInTypeID();
							}
						} 
					}
					
					//typeName = typeName.replaceAll("Type", "");
					
					typeName = tmp.getAttribute("type").replaceAll("Type", "");
						
					DTVO dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, (definitionElement != null) ? definitionElement.getTextContent() : null, (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
					
					insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId); // here --> modify
				}
			}
		}
	}
	
	public DTVO insertUnqualified_BDTStatement(String typeName, String dataTypeTerm, String id, String defaultGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoUser = df.getDAO("User");
		QueryCondition qc = new QueryCondition();
		qc.add("guid", defaultGUID);
		int basedDTID = ((DTVO)dao.findObject(qc, conn)).getDTID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", id);
		
		if(dao.findObject(qc1, conn) == null) {
				
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(id);
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			//dtVO.setRevisionType(0);
			dtVO.setDataTypeTerm(dataTypeTerm);
			dtVO.setBasedDTID(basedDTID);
			dtVO.setDEN(typeName + ". Type");
			dtVO.setContentComponentDEN(typeName + ". Content");
			dtVO.setState(3);
			QueryCondition qc_02 = new QueryCondition();
			qc_02.add("login_id", "oagis");
			int userId = ((UserVO)daoUser.findObject(qc_02, conn)).getUserID();
			dtVO.setCreatedByUserId(userId);
			dtVO.setLastUpdatedByUserId(userId);
			dtVO.setOwnerUserId(userId);
			dtVO.setRevisionDocumentation("");
			dtVO.setRevisionNum(0);
			dtVO.setRevisionTrackingNum(0);
			dtVO.setRevisionAction(true);
			dtVO.setIs_deprecated(false);
			dao.insertObject(dtVO);
		}
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("guid", id);
		return (DTVO)dao.findObject(qc2, conn);
	}
	
	private void importDataTypeList(String dataType) throws Exception {
		String typeName;
		String xsdTypeName;
		String dataTypeTerm;
		
		String type = "complex";
		
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
		
		//Type Name
		Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension");
		if (typeNameNode == null){
			type = "simple";
			typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
		} 
		Element typeNameElement = (Element)typeNameNode;
		typeName = typeNameElement.getAttribute("base");	
		
		Node aNodeTN = fields_xsd.getNode("//xsd:"+type+"Type[@name = '" + dataType + "']");
		Element aElementTN = (Element)aNodeTN;
		
		//Data Type Term
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		
		if(dataTypeTermNode == null && type.equals("simple")) {
			type = "complex";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		} else if(dataTypeTermNode == null && type.equals("complex")) {
			type = "simple";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		}
		Element dataTypeTermElement = (Element)dataTypeTermNode;
		dataTypeTerm = dataTypeTermElement.getTextContent();
		if (dataTypeTerm.length() > 5) if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
			dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
		//dataTypeTerm = dataTypeTerm.replaceAll(" Object", "");
						
		//Definitions
		Node definitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
		Element definitionElement = (Element)definitionNode;
		Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		Element ccDefinitionElement = (Element)ccDefinitionNode;				
		
		Node aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
		Element aElementBDT = (Element)aNodeBDT;
		
		Node union = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union");
		int defaultId = -1;
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSDBuiltInType");
		if(union != null) {
			QueryCondition qc3 = new QueryCondition();
			qc3.add("name", "token");
			defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc3, conn)).getXSDBuiltInTypeID();
		} else {
			Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:extension");
			if(xsdTypeNameNode == null)
				xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:restriction");
			if(xsdTypeNameNode != null) {
				Element xsdTypeNameElement = (Element)xsdTypeNameNode;
				xsdTypeName = xsdTypeNameElement.getAttribute("base");	
				
				QueryCondition qc3 = new QueryCondition();
				qc3.add("builtin_type", xsdTypeName);
				defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc3, conn)).getXSDBuiltInTypeID();
				if(defaultId < 1) {
					Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
					QueryCondition qc4 = new QueryCondition();
					System.out.println(xsdTypeName);
					qc4.add("builtin_type", ((Element)xbtNode).getAttribute("base"));
					defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc4, conn)).getXSDBuiltInTypeID();
				}
			} 
		}
		
		typeName = typeName.replaceAll("Type", "");
		
		DTVO dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
		
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		
		DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private void importExceptionalDataTypeList(String dataType) throws Exception {
		String typeName;
		String xsdTypeName;
		String dataTypeTerm;
		
//		String type = "complex";
//		
//		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
//		
//		//Type Name
//		Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension");
//		if (typeNameNode == null){
//			type = "simple";
//			typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
//		} 
//		Element typeNameElement = (Element)typeNameNode;
//		typeName = typeNameElement.getAttribute("base");	
//		
//		Node aNodeTN = fields_xsd.getNode("//xsd:"+type+"Type[@name = '" + dataType + "']");
//		Element aElementTN = (Element)aNodeTN;
		
		//Data Type Term
		
		typeName = "ValueType_039C44";
		String type = "simple";
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		
		Element dataTypeTermElement = (Element)dataTypeTermNode;
		dataTypeTerm = dataTypeTermElement.getTextContent();
		if (dataTypeTerm.length() > 5) if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
			dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
		//dataTypeTerm = dataTypeTerm.replaceAll(" Object", "");
						
		//Definitions
		Node definitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
		Element definitionElement = (Element)definitionNode;
		Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		Element ccDefinitionElement = (Element)ccDefinitionNode;				
		
		Node aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
		Element aElementBDT = (Element)aNodeBDT;
		
		Node union = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union");
		int defaultId = -1;
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("XSDBuiltInType");
		if(union != null) {
			QueryCondition qc3 = new QueryCondition();
			qc3.add("name", "token");
			defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc3, conn)).getXSDBuiltInTypeID();
		} else {
			Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:extension");
			if(xsdTypeNameNode == null)
				xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']//xsd:restriction");
			if(xsdTypeNameNode != null) {
				Element xsdTypeNameElement = (Element)xsdTypeNameNode;
				xsdTypeName = xsdTypeNameElement.getAttribute("base");	
				
				QueryCondition qc3 = new QueryCondition();
				qc3.add("builtin_type", xsdTypeName);
				defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc3, conn)).getXSDBuiltInTypeID();
				if(defaultId == 0) {
					Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
					QueryCondition qc4 = new QueryCondition();
					qc4.add("builtin_type", ((Element)xbtNode).getAttribute("base"));
					defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc4, conn)).getXSDBuiltInTypeID();
				}
			} 
		}
		
		typeName = typeName.replaceAll("Type", "");
		
		DTVO dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
		
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		
		DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementBDT.getAttribute("id"), aElementBDT.getAttribute("id"));
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private void insertBDTPrimitiveRestriction(int cdtID, int bdtID, int defaultId) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		
		QueryCondition qc3 = new QueryCondition();
		qc3.add("cdt_id", cdtID);
		ArrayList<SRTObject> al3 = aCDTAllowedPrimitiveDAO.findObjects(qc3);
		
		for(SRTObject aSRTObject3 : al3) {
			
			CDTAllowedPrimitiveVO aCDTAllowedPrimitiveVO = (CDTAllowedPrimitiveVO)aSRTObject3;
			
			QueryCondition qc4 = new QueryCondition();
			qc4.add("cdt_awd_pri_id", aCDTAllowedPrimitiveVO.getCDTAllowedPrimitiveID());
			ArrayList<SRTObject> al4 = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4);
			
			for(SRTObject aSRTObject4 : al4) {
				CDTAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExpressionTypeMapVO = (CDTAllowedPrimitiveExpressionTypeMapVO)aSRTObject4;
				// create insert statement
				BDTPrimitiveRestrictionVO aBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
				aBDT_Primitive_RestrictionVO.setBDTID(bdtID);
				aBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
				
				if(defaultId == aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID())
					aBDT_Primitive_RestrictionVO.setisDefault(true);
				else
					aBDT_Primitive_RestrictionVO.setisDefault(false);
				
				aBDTPrimitiveRestrictionDAO.insertObject(aBDT_Primitive_RestrictionVO);
			}
		}
	}
	
	private static Connection conn = null;
	
	public void run() throws Exception {
		System.out.println("### 1.5.1-2 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("User");
		QueryCondition qc = new QueryCondition();
		qc.add("login_id", "oagis");
		int userId = ((UserVO)dao.findObject(qc, conn)).getUserID();
		
		XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		XPathHandler components_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		
		File f = new File(SRTConstants.NOUNS_FILE_PATH);
		File[] listOfFiles = f.listFiles();
			
		for (int i = 0; i < Types.dataTypeList.length; i++){
			importDataTypeList(Types.dataTypeList[i]);
		}
		
//		for (int i = 0; i < Types.simpleTypeList.length; i++){
//			importDataTypeList(Types.simpleTypeList[i]);
//		}
		
		populateAdditionalDefault_BDTStatement(fields_xsd);
		populateAdditionalDefault_BDTStatement(meta_xsd);
		populateAdditionalDefault_BDTStatement(components_xsd);
		for (File file : listOfFiles) {
		    if (file.isFile()) {
		    	XPathHandler nouns_xsd = new XPathHandler(SRTConstants.NOUNS_FILE_PATH + file.getName());
		    	populateAdditionalDefault_BDTStatement(nouns_xsd);
		    }
		}
		importExceptionalDataTypeList("ValueType_039C44");
		tx.close();
		conn.close();
		System.out.println("### 1.5.1-2 End");
	}
	
	public static void main(String[] args) throws Exception {
		Utility.dbSetup();
		P_1_5_1_to_2_PopulateBDTsInDT p = new P_1_5_1_to_2_PopulateBDTsInDT();
		p.run();
	}
}
