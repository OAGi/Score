package org.oagi.srt.persistence.validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.persistence.populate.Types;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DataTypeTest {
	
	public DTVO validateInsertDefault_BDTStatement(String typeName, String dataTypeTerm, String id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", dataTypeTerm);
		qc.add("type", new Integer(0));
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", id);
		
		if(dao.findObject(qc1, conn) == null)
			System.out.println("Error! "+ id);
		else
			;//System.out.println("Success!!");
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("guid", id);
		return (DTVO)dao.findObject(qc2, conn);
	}
	
	public void validatePopulateAdditionalDefault_BDTStatement(XPathHandler filename) throws SRTDAOException, XPathExpressionException, FileNotFoundException, ParserConfigurationException, SAXException, IOException{
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
			if(dao.findObject(qc1, conn) == null) { 
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
					
					typeName = tmp.getAttribute("type").replaceAll("Type", "");
						
					DTVO dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));
					
					validateInsertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId); 
				}
			}
		}
	}
	
	public DTVO validateInsertUnqualified_BDTStatement(String typeName, String dataTypeTerm, String id, String defaultGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("guid", defaultGUID);
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", id);
		
		if(dao.findObject(qc1, conn) == null)
			System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
		else
			;//System.out.println("Success!!");
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("guid", id);
		return (DTVO)dao.findObject(qc2, conn);
	}
	
	private void validateDefaultImportDataTypeList(String dataType) throws Exception {
		String typeName = null;
		String xsdTypeName;
		String dataTypeTerm;
		
		String type = "complex";
		
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
		

		for(int i = 0; i < Types.defaultDataTypeList.length ; i++){
			if(Types.defaultDataTypeList[i].startsWith(dataType))
				typeName = Types.defaultDataTypeList[i];
		}
		
		//typeName = dataType;
		Node aNodeTN = fields_xsd.getNode("//xsd:"+type+"Type[@name = '" + dataType + "']");
		Element aElementTN = (Element)aNodeTN;
		
		//Data Type Term
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		
		dataTypeTerm = typeName.substring(0, typeName.indexOf("Type"));
		
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
		
		DTVO dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));
		System.out.println(dVO1.getBasedDTID()+"  "+ dVO1.getDTID()+"  "+ defaultId);
		validateInsertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		
		DTVO dVO2 = validateInsertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
		System.out.println(dVO1.getBasedDTID()+"  "+ dVO2.getDTID()+"  "+ defaultId);
		validateInsertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private void validateImportDataTypeList(String dataType) throws Exception {
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
				if(defaultId == 0) {
					Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
					QueryCondition qc4 = new QueryCondition();
					qc4.add("builtin_type", ((Element)xbtNode).getAttribute("base"));
					defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc4, conn)).getXSDBuiltInTypeID();
				}
			} 
		}
		
		typeName = typeName.replaceAll("Type", "");
		
		DTVO dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));
		
		validateInsertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		
		DTVO dVO2 = validateInsertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
		validateInsertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private void validateImportExceptionalDataTypeList(String dataType) throws Exception {
		String typeName;
		String xsdTypeName;
		String dataTypeTerm;
		
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
		
		typeName = "ValueType_039C44";
		String type = "simple";
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		
		Element dataTypeTermElement = (Element)dataTypeTermNode;
		dataTypeTerm = dataTypeTermElement.getTextContent();
		if (dataTypeTerm.length() > 5) if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
			dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
						
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
		
		DTVO dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, aElementBDT.getAttribute("id"));
		
		validateInsertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		
		DTVO dVO2 = validateInsertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementBDT.getAttribute("id"), aElementBDT.getAttribute("id"));
		validateInsertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private void validateInsertBDTPrimitiveRestriction(int cdtID, int bdtID, int defaultId) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		SRTDAO aBDTAllowedPrimitiveDAO = df.getDAO("BDTPrimitiveRestriction");
		
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
				// create insert statement -->modify
				BDTPrimitiveRestrictionVO aBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
				aBDT_Primitive_RestrictionVO.setBDTID(bdtID);
				aBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
				QueryCondition qc5 = new QueryCondition();
				qc5.add("bdt_id", bdtID);
				qc5.add("cdt_awd_pri_xps_type_map_id", aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
				
				if(aBDTAllowedPrimitiveDAO.findObject(qc5, conn) == null)
					System.out.println("Error!  bdt_id = "+bdtID+"   cdt_awd_pri_xps_type_map_id = "+aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
				else
					;//System.out.println("Success!!");
			}
		}
	}
	
	public void validateImportAdditionalBDT() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		XPathHandler xh = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		NodeList result = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");
	    
		for(int i = 0; i < result.getLength(); i++) {
		    Element ele = (Element)result.item(i);
		    String name = ele.getAttribute("name");
		    String guid = ele.getAttribute("id");
		    String den = name.substring(0, name.lastIndexOf("Type")) + ". Type";
		    QueryCondition qc01 = new QueryCondition();
			qc01.add("guid", guid);
			qc01.add("den", den);
			DTVO dtVO = (DTVO)dao.findObject(qc01, conn);
			if(dtVO == null)
				System.out.println("Error in line # "+new Exception().getStackTrace()[0].getLineNumber());
			else {
				;//System.out.println("Success!!");
			    // BDT_Primitive_Restriction
			    QueryCondition qc2 = new QueryCondition();
				qc2.add("guid", dtVO.getDTGUID());
				validateInsertBDTPrimitiveRestriction(dtVO.getDTID(), ((DTVO)dao.findObject(qc2, conn)).getDTID());
			}
	    }
	}
	
	private void validateInsertBDTPrimitiveRestriction(int basedBdtId, int bdtId) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		
		QueryCondition qc = new QueryCondition();
		qc.add("bdt_id", basedBdtId);
		ArrayList<SRTObject> al = aBDTPrimitiveRestrictionDAO.findObjects(qc, conn);
		
		for(SRTObject aSRTObject : al) {
			BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
			QueryCondition qc2 = new QueryCondition();
			if(aBDTPrimitiveRestrictionVO.getCDTPrimitiveExpressionTypeMapID() !=0)
				qc2.add("cdt_awd_pri_xps_type_map_id", aBDTPrimitiveRestrictionVO.getCDTPrimitiveExpressionTypeMapID());
			if(aBDTPrimitiveRestrictionVO.getCodeListID() != 0)
				qc2.add("code_list_id", aBDTPrimitiveRestrictionVO.getCodeListID());
			if(aBDTPrimitiveRestrictionDAO.findObject(qc2, conn) == null)
				System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
			else
				;//System.out.println("Success!!");
		}
	}
	
	
	private void validateImportQBDT(XPathHandler fields_xsd) throws XPathExpressionException, SRTDAOException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		NodeList elementsFromFieldsXSD = fields_xsd.getNodeList("/xsd:schema/xsd:element");
		NodeList simpleTypesFromFieldXSD = fields_xsd.getNodeList("//xsd:simpleType");
		validateInsertCodeContentQBDT(simpleTypesFromFieldXSD, fields_xsd, 0);
		validateInsertIDContentQBDT(simpleTypesFromFieldXSD, fields_xsd, 0);
		
		validateInsertOtherQBDT(elementsFromFieldsXSD, fields_xsd, 0);
	}
	
	private void validateInsertCodeContentQBDT(NodeList simpleTypesFromFieldXSD, XPathHandler org_xHandler, int xsdType) throws XPathExpressionException, SRTDAOException {
		XPathHandler xHandler = org_xHandler;
		
		//specify Code Content Type QBDT
		//the xsd:simpleType whose names end with ‘CodeContentType’, except the ‘CodeContentType’ itself. 
		
		for(int i = 0; i < simpleTypesFromFieldXSD.getLength(); i++) {
			xHandler = org_xHandler;
			String name = ((Element)simpleTypesFromFieldXSD.item(i)).getAttribute("name");
			
			if(!name.equals("CodeContentType") && name.endsWith("CodeContentType")){
				Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");

				if(simpleType != null) {	
					Node typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");
					
					DAOFactory df;
					df = DAOFactory.getDAOFactory();
					SRTDAO aDTDAO = df.getDAO("DT");
					QueryCondition qc = new QueryCondition();
					String typeGuid = ((Element)typeNode).getAttribute("id");
					qc.add("guid", typeGuid);
					DTVO dtVO = (DTVO)aDTDAO.findObject(qc, conn);

					if(dtVO != null) {
						DTVO dVO = validateAddToDTForCodeContentQBDT(dtVO, name, typeNode, xHandler);
						if(dVO != null)
							validateAddToDTSCForContentType(xHandler, name, dVO);
					} else {
						System.out.println("Error! Qualified Code Content Type: " + name +" is not exist in Database");
					}
				}
			}
		}
	}
	
	private void validateInsertIDContentQBDT(NodeList simpleTypesFromFieldXSD, XPathHandler org_xHandler, int xsdType) throws XPathExpressionException, SRTDAOException {
		XPathHandler xHandler = org_xHandler;
		
		//specify ID Content Type QBDT
		//the xsd:simpleType whose names end with ‘IDContentType’ 
		
		for(int i = 0; i < simpleTypesFromFieldXSD.getLength(); i++) {
			xHandler = org_xHandler;
			String name = ((Element)simpleTypesFromFieldXSD.item(i)).getAttribute("name");
			
			if(!name.equals("IDContentType") && name.endsWith("IDContentType")){//Actually Only AgencyIDContentType..
				Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");

				if(simpleType != null) {	
					Node typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + name + "']");
					
					DAOFactory df;
					df = DAOFactory.getDAOFactory();
					SRTDAO aDTDAO = df.getDAO("DT");
					QueryCondition qc = new QueryCondition();
					String typeGuid = ((Element)typeNode).getAttribute("id");
					qc.add("guid", typeGuid);
					DTVO dtVO = (DTVO)aDTDAO.findObject(qc, conn);

					if(dtVO != null) {
						DTVO dVO = validateAddToDTForIDContentQBDT(dtVO, name, typeNode, xHandler);
						if(dVO != null)
							validateAddToDTSCForContentType(xHandler, name, dVO);
					} else {
						System.out.println("Error! Qualified ID Content Type: " + name +" is not exist in Database");
					}
				}
			}
		}
	}
	
	private void validateInsertOtherQBDT(NodeList elementsFromXSD, XPathHandler org_xHandler, int xsdType) throws XPathExpressionException, SRTDAOException {
		XPathHandler xHandler = org_xHandler;
		for(int i = 0; i < elementsFromXSD.getLength(); i++) {
			xHandler = org_xHandler;
			String bccp = ((Element)elementsFromXSD.item(i)).getAttribute("name");
			String type = ((Element)elementsFromXSD.item(i)).getAttribute("type");
			Node simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
			Node simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");

			simpleContent = xHandler.getNode("//xsd:complexType[@name = '" + type + "']/xsd:simpleContent");
			simpleType = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
			
			if(simpleContent != null || simpleType != null) {
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
				if(typeNode == null) {
					typeNode = xHandler.getNode("//xsd:simpleType[@name = '" + type + "']");
				}
				
				DAOFactory df;
				df = DAOFactory.getDAOFactory();
				SRTDAO aDTDAO = df.getDAO("DT");
				QueryCondition qc = new QueryCondition();
				String typeGuid = ((Element)typeNode).getAttribute("id");
				qc.add("guid", typeGuid);
				DTVO dtVO = (DTVO)aDTDAO.findObject(qc, conn);
				
				if(dtVO != null ) {
					
					if(dtVO.getQualifier()!=null){
						QueryCondition qc2 = new QueryCondition();
						qc2.add("dt_id", dtVO.getBasedDTID());
						
						DTVO basedtVO = (DTVO)aDTDAO.findObject(qc2, conn);
						boolean checkAlready = false;
						
						if(basedtVO.getDEN().equals("Code Content. Type") || basedtVO.getDEN().equals("Identifier Content. Type")){
							checkAlready = true;
						}
						
						if(!checkAlready){
							DTVO dVO = validateAddToDTForOtherQBDT(dtVO, type, typeNode, xHandler);
							if(dVO != null){
								validateAddToDTSC(xHandler, type, dVO);
							}
						}
					}
				} else {
					System.out.println("Error! OtherQBDT is not in database. Check guid: "+ typeGuid);
				}
			}
		}
	}
	
	private List<SRTObject> getBDTSCPrimitiveRestriction(DTSCVO dtscVO) throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO bdtSCPRDAO = df.getDAO("BDTSCPrimitiveRestriction");
		QueryCondition qc_00 = new QueryCondition();
		qc_00.add("bdt_sc_id", dtscVO.getBasedDTSCID());
		List<SRTObject> bdtscs = bdtSCPRDAO.findObjects(qc_00, conn);
		return bdtscs;
	}
	
	private void validateInsertBDTSCPrimitiveRestriction(DTSCVO dtscVO, int mode, String name, String type)  throws XPathExpressionException, SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO bdtSCPRDAO = df.getDAO("BDTSCPrimitiveRestriction");
		List<SRTObject> cdtscallowedprimitivelist = new ArrayList<SRTObject>();
		if(mode == 1) {//inherited
			List<SRTObject> bdtscs = getBDTSCPrimitiveRestriction(dtscVO);
			for(SRTObject obj : bdtscs) {
				BDTSCPrimitiveRestrictionVO parent = (BDTSCPrimitiveRestrictionVO)obj;
				
				QueryCondition qc1 = new QueryCondition();
				qc1.add("bdt_sc_id", dtscVO.getDTSCID());
				qc1.add("cdt_sc_awd_pri_xps_type_map_id", parent.getCDTSCAllowedPrimitiveExpressionTypeMapID());
				qc1.add("code_list_id", parent.getCodeListID());
				qc1.add("agency_id_list_id", parent.getAgencyIDListID());
				if(bdtSCPRDAO.findObject(qc1, conn) == null)
					System.out.println("#######BDT_SC_PRIMITIVE_RESTRICTION FROM BASE IS NOT POPULATED!! Check DT_SC_ID: "+dtscVO.getDTSCID());
				else
					;//System.out.println("Success!!");
			}
		
		} else { // else if (new SC)
			QueryCondition qc1 = new QueryCondition();
			qc1.add("bdt_sc_id", dtscVO.getDTSCID());

			cdtscallowedprimitivelist = getCdtSCAllowedPrimitiveID(dtscVO.getDTSCID());
			for(SRTObject dvo : cdtscallowedprimitivelist) {
				CDTSCAllowedPrimitiveVO svo = (CDTSCAllowedPrimitiveVO) dvo;
				List<SRTObject> maps = getCdtSCAPMap(svo.getCDTSCAllowedPrimitiveID());
				for(SRTObject mapVO : maps) {
					CDTSCAllowedPrimitiveExpressionTypeMapVO vo = (CDTSCAllowedPrimitiveExpressionTypeMapVO)mapVO;
					qc1.add("cdt_sc_awd_pri_xps_type_map_id", vo.getCTSCAllowedPrimitiveExpressionTypeMapID());
					if(bdtSCPRDAO.findObject(qc1, conn) == null)
						System.out.println("#######BDT_SC_PRIMITIVE_RESTRICTION FROM ATTRIBUTES IS NOT POPULATED!! Check DT_SC_ID: "+dtscVO.getDTSCID());
					else
						;//System.out.println("Success!!");
					
				}
			}
			
			if(type.contains("CodeContentType")){
				QueryCondition qc2 = new QueryCondition();
				qc2.add("bdt_sc_id", dtscVO.getDTSCID());
				qc2.add("code_list_id", getCodeListID(type.substring(0, type.indexOf("CodeContentType"))));
				if(bdtSCPRDAO.findObject(qc2, conn) == null)
					System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
				else
					;//System.out.println("Success!!");
				
			} 
			if(name.equalsIgnoreCase("listAgencyID")){
				QueryCondition qc2 = new QueryCondition();
				qc2.add("bdt_sc_id", dtscVO.getDTSCID());
				qc2.add("agency_id_list_id", getAgencyListID());
				if(bdtSCPRDAO.findObject(qc2, conn) == null)
					System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
				else
					;//System.out.println("Success!!");
			}
		}		
	}
	
	private DTVO validateAddToDTForCodeContentQBDT(DTVO queriedQBDTVO, String type, Node typeNode, XPathHandler xHandler) throws XPathExpressionException, SRTDAOException {		
		
		String fromDTVO = "";
		String fromXSD = "";
		
		Node nodeForDef =  xHandler.getNode("//xsd:simpleType[@name='"+type+"']/xsd:annotation/xsd:documentation");
		
		String definitionFromXSD =null;
		if(nodeForDef!=null){
			definitionFromXSD = nodeForDef.getTextContent();
		}
				
		fromXSD = fromXSD + "type=1 version_number=1.0 previous_version_dt_id=0 data_type_term=Code den="+type
				+ " baseTypeGuid=oagis-id-5646bf52a97b48adb50ded6ff8c38354 definition="+definitionFromXSD+
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
		
		String denFromDTVO = queriedQBDTVO.getContentComponentDEN();
		
		denFromDTVO=denFromDTVO.replaceAll("_", "");
		
		denFromDTVO=denFromDTVO.replaceAll(" ", "");
		
		denFromDTVO=denFromDTVO.replace(".Content", "Type");
		
		
		
		//To validate bdt_pri_restri,
		//check it inherit base's bdt_pri_restri successfully (except id itself)
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("dt_id", queriedQBDTVO.getBasedDTID());
		DTVO baseDTVO = (DTVO) aDTDAO.findObject(qc);
		
		fromDTVO = fromDTVO + "type=" +queriedQBDTVO.getDTType()
				+" version_number=" +queriedQBDTVO.getVersionNumber()
				+" previous_version_dt_id=" + queriedQBDTVO.getPreviousVersionDTID()
				+" data_type_term=" + queriedQBDTVO.getDataTypeTerm()
				+" den=" + denFromDTVO
				+ " baseTypeGuid=" + baseDTVO.getDTGUID()
				+ " definition=" + queriedQBDTVO.getDefinition()
				+ " content_component_definition=" + queriedQBDTVO.getContentComponentDefinition()
				+" revision_doc=" + queriedQBDTVO.getRevisionDocumentation()
				+" state=" + queriedQBDTVO.getState()
				+ " created_by=" + queriedQBDTVO.getCreatedByUserId()
				+ " owner_user_id="  + queriedQBDTVO.getOwnerUserId()
				+ " last_updated_by="  + queriedQBDTVO.getLastUpdatedByUserId()
				+" revision_num=" + queriedQBDTVO.getRevisionNum()
				+ " revision_tracking_num=" + queriedQBDTVO.getRevisionTrackingNum()
				+ " revision_action_id=" + queriedQBDTVO.getRevisionAction()
				+ " release_id=" + queriedQBDTVO.getReleaseId()
				+ " current_bdt_id=" + queriedQBDTVO.getCurrentBdtId()
				+ " is_deprecated=" + queriedQBDTVO.getIs_deprecated();
		
		
		if(!fromXSD.equals(fromDTVO)){
			System.out.println("########CodeContentType QBDT is not valid in some fields!!###########");
			System.out.println(" XSD: "+fromXSD);
			System.out.println("DTVO: "+fromDTVO);
		}
		
		validateInsertQBDTPrimitiveRestriction(queriedQBDTVO, "CodeContentType");
		
		return queriedQBDTVO;
	}
	
	private DTVO validateAddToDTForIDContentQBDT(DTVO queriedQBDTVO, String type, Node typeNode, XPathHandler xHandler) throws XPathExpressionException, SRTDAOException {		
		
		String fromDTVO = "";
		String fromXSD = "";
		
		Node nodeForDef =  xHandler.getNode("//xsd:simpleType[@name='"+type+"']/xsd:annotation/xsd:documentation");
		
		String definitionFromXSD =null;
		if(nodeForDef!=null){
			definitionFromXSD = nodeForDef.getTextContent();
		}
				
		fromXSD = fromXSD + "type=1 version_number=1.0 previous_version_dt_id=0 data_type_term=Identifier den="+type
				+ " baseTypeGuid=oagis-id-08d6ade226fd42488b53c0815664e246 definition="+definitionFromXSD+
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
		
		String denFromDTVO = queriedQBDTVO.getContentComponentDEN();
		denFromDTVO=denFromDTVO.replaceAll("_", "");
		denFromDTVO=denFromDTVO.replaceAll("Identifier", "ID");
		denFromDTVO=denFromDTVO.replaceAll(" ", "");
		denFromDTVO=denFromDTVO.replace(".Content", "Type");
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("dt_id", queriedQBDTVO.getBasedDTID());
		DTVO baseDTVO = (DTVO) aDTDAO.findObject(qc);
		
		fromDTVO = fromDTVO + "type=" +queriedQBDTVO.getDTType()
				+" version_number=" +queriedQBDTVO.getVersionNumber()
				+" previous_version_dt_id=" + queriedQBDTVO.getPreviousVersionDTID()
				+" data_type_term=" + queriedQBDTVO.getDataTypeTerm()
				+" den=" + denFromDTVO
				+ " baseTypeGuid=" + baseDTVO.getDTGUID()
				+ " definition=" + queriedQBDTVO.getDefinition()
				+ " content_component_definition=" + queriedQBDTVO.getContentComponentDefinition()
				+" revision_doc=" + queriedQBDTVO.getRevisionDocumentation()
				+" state=" + queriedQBDTVO.getState()
				+ " created_by=" + queriedQBDTVO.getCreatedByUserId()
				+ " owner_user_id="  + queriedQBDTVO.getOwnerUserId()
				+ " last_updated_by="  + queriedQBDTVO.getLastUpdatedByUserId()
				+" revision_num=" + queriedQBDTVO.getRevisionNum()
				+ " revision_tracking_num=" + queriedQBDTVO.getRevisionTrackingNum()
				+ " revision_action_id=" + queriedQBDTVO.getRevisionAction()
				+ " release_id=" + queriedQBDTVO.getReleaseId()
				+ " current_bdt_id=" + queriedQBDTVO.getCurrentBdtId()
				+ " is_deprecated=" + queriedQBDTVO.getIs_deprecated();
		
		
		if(!fromXSD.equals(fromDTVO)){
			System.out.println("########IDContentType QBDT is not valid in some fields!!###########");
			System.out.println(" XSD: "+fromXSD);
			System.out.println("DTVO: "+fromDTVO);
		}
		
		validateInsertQBDTPrimitiveRestriction(queriedQBDTVO, "IDContentType");
		
		return queriedQBDTVO;
	}
		
	private DTVO validateAddToDTForOtherQBDT(DTVO queriedQBDTVO, String type, Node typeNode, XPathHandler xHandler) throws XPathExpressionException, SRTDAOException {
			
//			Element extension = (Element)((Element)typeNode).getElementsByTagName("xsd:extension").item(0);
//			if(extension == null || extension.getAttribute("base") == null)
//				return null;
//			String base = extension.getAttribute("base");
//			
//			if(type.endsWith("CodeContentType")) {
//				dVO = getDTVOWithDEN("Code. Type");
//			} else {
//				String den = Utility.typeToDen(type);
//				dVO = getDTVOWithDEN(den);
//				
//				// QBDT is based on another QBDT
//				if(dVO != null) {
//
//					System.out.println("DTT: "+dVO.getDataTypeTerm() + "   Qualifier: "+dVO.getQualifier() + "   den:" + dVO.getDEN());
//					;//System.out.println("Success!!");
//				}
//				else {
//					System.out.println("Add to DT Error! DTT: "+dVO.getDataTypeTerm() + "   Qualifier: "+dVO.getQualifier() + "   den:" + dVO.getDEN());
//					return null;
//				}
//			}
			
			
			String fromDTVO = "";
			String fromXSD = "";
			
			Node nodeForDef =  xHandler.getNode("//xsd:simpleType[@name='"+type+"']/xsd:annotation/xsd:documentation");
			if(nodeForDef==null){
				nodeForDef = xHandler.getNode("//xsd:complexType[@name='"+type+"']/xsd:annotation/xsd:documentation");
			}
			
			String definitionFromXSD =null;
			
			
//			if(nodeForDef!=null){
//				definitionFromXSD = nodeForDef.getTextContent();
//			}
//			//Most QBDT has no definition. 
//			//Later on we want to come back and modify this to take definition from an element with the same name. 
//			//Right now let’s leave it blank so that it is easier to validate the import.
			
			String baseTypeGUIDFromXSD = "";
			String basdTypeDataTypeTerm = "";
			
			Node baseNameNode =  xHandler.getNode("//xsd:simpleType[@name='"+type+"']//@base");
			if(baseNameNode==null){
				baseNameNode = xHandler.getNode("//xsd:complexType[@name='"+type+"']//@base");
			}
			String baseTypeNodeName = baseNameNode.getTextContent();
			String dataTypeTerm = "";
			if(baseTypeNodeName.contains("Code")){
				dataTypeTerm = "Code";
			}
			else if(baseTypeNodeName.contains("String")){
				dataTypeTerm = "Text";
			}
			else {
				dataTypeTerm = Utility.spaceSeparatorBeforeStr(baseTypeNodeName, "Type");
			}
			
			
			
//			Node baseTypeNode = xHandler.getNode("//xsd:simpleType[@name='"+baseTypeNodeName+"']");
//			if(baseTypeNode==null){
//				baseTypeNode = xHandler.getNode("//xsd:complexType[@name='"+baseTypeNodeName+"']");
//			}
//			
//			baseTypeGUIDFromXSD = ((Element)baseTypeNode).getAttribute("id");
			
			if(baseTypeNodeName.endsWith("CodeContentType")){
				baseTypeNodeName="CodeType";
			}
					
			fromXSD = fromXSD + "type=1 version_number=1.0 previous_version_dt_id=0 data_type_term="+dataTypeTerm
					+" den="+type
					+ " baseTypeName="+baseTypeNodeName+ " definition="+definitionFromXSD+
					" content_component_definition=null revision_doc=null state=3 created_by=1 owner_user_id=1" +
					" last_updated_by=1 revision_num=0 revision_tracking_num=0 revision_action_id=0 release_id=0 current_bdt_id=0 is_deprecated=false";
			
			//0 in id value means null in database
					
			String denFromDTVO = queriedQBDTVO.getContentComponentDEN();
			denFromDTVO=denFromDTVO.replaceAll("_", "");
			denFromDTVO=denFromDTVO.replaceAll("Identifier", "ID");
			denFromDTVO=denFromDTVO.replaceAll(" ", "");
			denFromDTVO=denFromDTVO.replace(".Content", "Type");
			
			if(!denFromDTVO.equals("OpenTextType")){
				denFromDTVO=denFromDTVO.replaceFirst("Text", "");
			}
			if(denFromDTVO.contains("String")){
				denFromDTVO=denFromDTVO.replace("String", "");
			}
			
			DAOFactory df = DAOFactory.getDAOFactory();
			SRTDAO aDTDAO = df.getDAO("DT");
			QueryCondition qc = new QueryCondition();
			qc.add("dt_id", queriedQBDTVO.getBasedDTID());
			DTVO baseDTVO = (DTVO) aDTDAO.findObject(qc);
			String baseTypeName = "";
			baseTypeName = baseDTVO.getContentComponentDEN();
			baseTypeName = baseTypeName.replaceAll("_", "");
			baseTypeName=baseTypeName.replaceAll("Identifier", "ID");
			baseTypeName=baseTypeName.replaceAll(" ", "");
			baseTypeName=baseTypeName.replace(".Content", "Type");
			
			fromDTVO = fromDTVO + "type=" +queriedQBDTVO.getDTType()
					+" version_number=" +queriedQBDTVO.getVersionNumber()
					+" previous_version_dt_id=" + queriedQBDTVO.getPreviousVersionDTID()
					+" data_type_term=" + queriedQBDTVO.getDataTypeTerm()
					+" den=" + denFromDTVO
					+ " baseTypeName=" + baseTypeName
					+ " definition=" + queriedQBDTVO.getDefinition()
					+ " content_component_definition=" + queriedQBDTVO.getContentComponentDefinition()
					+" revision_doc=" + queriedQBDTVO.getRevisionDocumentation()
					+" state=" + queriedQBDTVO.getState()
					+ " created_by=" + queriedQBDTVO.getCreatedByUserId()
					+ " owner_user_id="  + queriedQBDTVO.getOwnerUserId()
					+ " last_updated_by="  + queriedQBDTVO.getLastUpdatedByUserId()
					+" revision_num=" + queriedQBDTVO.getRevisionNum()
					+ " revision_tracking_num=" + queriedQBDTVO.getRevisionTrackingNum()
					+ " revision_action_id=" + queriedQBDTVO.getRevisionAction()
					+ " release_id=" + queriedQBDTVO.getReleaseId()
					+ " current_bdt_id=" + queriedQBDTVO.getCurrentBdtId()
					+ " is_deprecated=" + queriedQBDTVO.getIs_deprecated();
			
			
			if(!fromXSD.equals(fromDTVO)){
				System.out.println("########Other QBDT is not valid in some fields!!###########");
				System.out.println(" XSD: "+fromXSD);
				System.out.println("DTVO: "+fromDTVO);
			}
			
			DTVO res = (DTVO)queriedQBDTVO;
			validateInsertQBDTPrimitiveRestriction(res, baseTypeName);
			return res;
			
	}
	
	private void validateInsertQBDTPrimitiveRestriction(DTVO dVO, String base) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO BDTPriRestrisVO = df.getDAO("BDTPrimitiveRestriction");
		
		//To validate bdt_pri_restri,
		//check it inherit base's bdt_pri_restri successfully (except id itself)
		
		QueryCondition qc4base = new QueryCondition();
		qc4base.add("bdt_id", dVO.getBasedDTID());
		ArrayList<SRTObject> baseBDTPriRestriList = BDTPriRestrisVO.findObjects(qc4base, conn);
		//get base's bdt_pri_restri records
		
		QueryCondition qc4this = new QueryCondition();
		qc4this.add("bdt_id", dVO.getDTID());
		ArrayList<SRTObject> thisBDTPriRestriList = BDTPriRestrisVO.findObjects(qc4this, conn);
		//get this's bdt_pri_restri records
		
		//if CodeContentType QBDT-->inherit from CodeContentType & add codeListID  
		//if IDContentType QBDT-->inherit from IDContentType & add agencyIDListID 
		//Assume there is no qualified CodeContentType is based on another qualified CodeContentType.
		
		if(dVO.getDataTypeTerm().equals("Code")
		&& base.equals("CodeContentType")){
			for(int i=0; i<baseBDTPriRestriList.size(); i++){
				BDTPrimitiveRestrictionVO baseBDT_pri_restriVO = new BDTPrimitiveRestrictionVO();
				baseBDT_pri_restriVO = (BDTPrimitiveRestrictionVO) baseBDTPriRestriList.get(i);
				boolean exist = false;
				int duplicated = -1;
				int addedIndex = -1;
				int isDefaultCnt = 0;
				for(int j=0; j<thisBDTPriRestriList.size(); j++){
					BDTPrimitiveRestrictionVO BDT_pri_restriVO = new BDTPrimitiveRestrictionVO();
					BDT_pri_restriVO = (BDTPrimitiveRestrictionVO) thisBDTPriRestriList.get(j);
					
					if(baseBDT_pri_restriVO.getCDTPrimitiveExpressionTypeMapID()==BDT_pri_restriVO.getCDTPrimitiveExpressionTypeMapID()
					&&	baseBDT_pri_restriVO.getCodeListID() == BDT_pri_restriVO.getCodeListID()
					&& baseBDT_pri_restriVO.getAgencyIDListID() == BDT_pri_restriVO.getAgencyIDListID()
					&& baseBDT_pri_restriVO.getisDefault() == BDT_pri_restriVO.getisDefault()){
						exist = true;
						duplicated ++;
						if(BDT_pri_restriVO.getisDefault()){
							isDefaultCnt++;
						}
						thisBDTPriRestriList.remove(j);
						j=0;
					}
				}
				
				if(!exist){
					System.out.println("Not Inherited from Base. Check DT_ID:"+dVO.getDTID());
				}
				if(duplicated > 0){
					System.out.println("Duplicated. Check DT_ID:"+dVO.getDTID());
				}
				if(isDefaultCnt > 1){
					System.out.println("There are "+isDefaultCnt+ " defaults. Check DT_ID:"+dVO.getDTID());
				}
			}
			if(thisBDTPriRestriList.size() > 0){					
				System.out.println("CodeContentType QBDT: "+dVO.getDTID()+" has added record in bdt_pri_restri");
				for(int j=0; j<thisBDTPriRestriList.size(); j++){
					BDTPrimitiveRestrictionVO addedRecord = new BDTPrimitiveRestrictionVO();
					addedRecord = (BDTPrimitiveRestrictionVO) thisBDTPriRestriList.get(j);
					System.out.println(addedRecord.getCDTPrimitiveExpressionTypeMapID()+" "
									+addedRecord.getCodeListID() +" "
									+addedRecord.getAgencyIDListID() + " "
									+addedRecord.getisDefault());
				}
			}
		}
		else if(dVO.getDataTypeTerm().equals("Identifier")
		&& base.equals("IDContentType")){
			for(int i=0; i<baseBDTPriRestriList.size(); i++){
				BDTPrimitiveRestrictionVO baseBDT_pri_restriVO = new BDTPrimitiveRestrictionVO();
				baseBDT_pri_restriVO = (BDTPrimitiveRestrictionVO) baseBDTPriRestriList.get(i);
				boolean exist = false;
				int duplicated = -1;
				int addedIndex = -1;
				int isDefaultCnt = 0;
				for(int j=0; j<thisBDTPriRestriList.size(); j++){
					BDTPrimitiveRestrictionVO BDT_pri_restriVO = new BDTPrimitiveRestrictionVO();
					BDT_pri_restriVO = (BDTPrimitiveRestrictionVO) thisBDTPriRestriList.get(j);
					
					if(baseBDT_pri_restriVO.getCDTPrimitiveExpressionTypeMapID()==BDT_pri_restriVO.getCDTPrimitiveExpressionTypeMapID()
					&&	baseBDT_pri_restriVO.getCodeListID() == BDT_pri_restriVO.getCodeListID()
					&& baseBDT_pri_restriVO.getAgencyIDListID() == BDT_pri_restriVO.getAgencyIDListID()
					&& baseBDT_pri_restriVO.getisDefault() == BDT_pri_restriVO.getisDefault()){
						exist = true;
						duplicated ++;
						if(BDT_pri_restriVO.getisDefault()){
							isDefaultCnt++;
						}
						thisBDTPriRestriList.remove(j);
						j=0;
					}
				}
				
				if(!exist){
					System.out.println("Not Inherited from Base. Check DT_ID:"+dVO.getDTID());
				}
				if(duplicated > 0){
					System.out.println("Duplicated. Check DT_ID:"+dVO.getDTID());
				}
				if(isDefaultCnt > 1){
					System.out.println("There are "+isDefaultCnt+ " defaults. Check DT_ID:"+dVO.getDTID());
				}
			}
			if(thisBDTPriRestriList.size() > 0){					
				System.out.println("IDContentType QBDT: "+dVO.getDTID()+" has added record in bdt_pri_restri");
				for(int j=0; j<thisBDTPriRestriList.size(); j++){
					BDTPrimitiveRestrictionVO addedRecord = new BDTPrimitiveRestrictionVO();
					addedRecord = (BDTPrimitiveRestrictionVO) thisBDTPriRestriList.get(j);
					System.out.println(addedRecord.getCDTPrimitiveExpressionTypeMapID()+" "
									+addedRecord.getCodeListID() +" "
									+addedRecord.getAgencyIDListID() + " "
									+addedRecord.getisDefault());
				}
			}
		}
		else {
			for(int i=0; i<baseBDTPriRestriList.size(); i++){
				BDTPrimitiveRestrictionVO baseBDT_pri_restriVO = new BDTPrimitiveRestrictionVO();
				baseBDT_pri_restriVO = (BDTPrimitiveRestrictionVO) baseBDTPriRestriList.get(i);
				boolean exist = false;
				int duplicated = -1;
				int addedIndex = -1;
				int isDefaultCnt = 0;
				for(int j=0; j<thisBDTPriRestriList.size(); j++){
					BDTPrimitiveRestrictionVO BDT_pri_restriVO = new BDTPrimitiveRestrictionVO();
					BDT_pri_restriVO = (BDTPrimitiveRestrictionVO) thisBDTPriRestriList.get(j);
					
					if(baseBDT_pri_restriVO.getCDTPrimitiveExpressionTypeMapID()==BDT_pri_restriVO.getCDTPrimitiveExpressionTypeMapID()
					&&	baseBDT_pri_restriVO.getCodeListID() == BDT_pri_restriVO.getCodeListID()
					&& baseBDT_pri_restriVO.getAgencyIDListID() == BDT_pri_restriVO.getAgencyIDListID()
					&& baseBDT_pri_restriVO.getisDefault() == BDT_pri_restriVO.getisDefault()){
						exist = true;
						duplicated ++;
						if(BDT_pri_restriVO.getisDefault()){
							isDefaultCnt++;
						}
						thisBDTPriRestriList.remove(j);
						j=0;
					}
				}
				
				if(!exist){
					System.out.println("Not Inherited from Base. Check DT_ID:"+dVO.getDTID());
				}
				if(duplicated > 0){
					System.out.println("Duplicated. Check DT_ID:"+dVO.getDTID());
				}
				if(isDefaultCnt > 1){
					System.out.println("There are "+isDefaultCnt+ " defaults. Check DT_ID:"+dVO.getDTID());
				}
			}
			if(thisBDTPriRestriList.size() > 0){					
				System.out.println("Other QBDT: "+dVO.getDTID()+" has added record in bdt_pri_restri");
				for(int j=0; j<thisBDTPriRestriList.size(); j++){
					BDTPrimitiveRestrictionVO addedRecord = new BDTPrimitiveRestrictionVO();
					addedRecord = (BDTPrimitiveRestrictionVO) thisBDTPriRestriList.get(j);
					System.out.println(addedRecord.getCDTPrimitiveExpressionTypeMapID()+" "
									+addedRecord.getCodeListID() +" "
									+addedRecord.getAgencyIDListID() + " "
									+addedRecord.getisDefault());
				}
			}
		}
		
//		
//		if(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
//			QueryCondition qc2 = new QueryCondition();
//
//			BDTPrimitiveRestrictionVO theBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
//			theBDT_Primitive_RestrictionVO.setBDTID(dVO.getDTID());
//			if(base.endsWith("CodeContentType")) {
//				qc2.add("code_list_id", getCodeListID(base.substring(0, base.indexOf("CodeContentType"))));
//			} else {
//				for(SRTObject aSRTObject : al) {
//					BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
//					if(aBDTPrimitiveRestrictionVO.getCodeListID() > 0) {
//						qc2.add("code_list_id", aBDTPrimitiveRestrictionVO.getCodeListID());
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
//				BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
//				QueryCondition qc2 = new QueryCondition();
//				qc2.add("bdt_id", dVO.getDTID());
//				qc2.add("cdt_awd_pri_xps_type_map_id", aBDTPrimitiveRestrictionVO.getCDTPrimitiveExpressionTypeMapID());
//
//				if(BDTPriRestrisVOofBase.findObject(qc2, conn) == null)
//					System.out.println("Error! BDT Primitive Restriction doesn't exist!"+new Exception().getStackTrace()[0].getLineNumber());
//				else
//					;//System.out.println("Success!!");
//			}
//		}
	}
	
//	
//	private void validateInsertQBDTPrimitiveRestriction(DTVO dVO, String base) throws SRTDAOException {
//		DAOFactory df = DAOFactory.getDAOFactory();
//		SRTDAO BDTPriRestrisVO = df.getDAO("BDTPrimitiveRestriction");
//		
//		//To validate bdt_pri_restri,
//		//check it inherit base's bdt_pri_restri successfully (except id itself)
//		
//		QueryCondition qc = new QueryCondition();
//		qc.add("bdt_id", dVO.getBasedDTID());
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
//			BDTPrimitiveRestrictionVO theBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
//			theBDT_Primitive_RestrictionVO.setBDTID(dVO.getDTID());
//			if(base.endsWith("CodeContentType")) {
//				qc2.add("code_list_id", getCodeListID(base.substring(0, base.indexOf("CodeContentType"))));
//			} else {
//				for(SRTObject aSRTObject : baseBDTPriRestriList) {
//					BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
//					if(aBDTPrimitiveRestrictionVO.getCodeListID() > 0) {
//						qc2.add("code_list_id", aBDTPrimitiveRestrictionVO.getCodeListID());
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
//				BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
//				QueryCondition qc2 = new QueryCondition();
//				qc2.add("bdt_id", dVO.getDTID());
//				qc2.add("cdt_awd_pri_xps_type_map_id", aBDTPrimitiveRestrictionVO.getCDTPrimitiveExpressionTypeMapID());
//
//				if(BDTPriRestrisVO.findObject(qc2, conn) == null)
//					System.out.println("Error! BDT Primitive Restriction doesn't exist!"+new Exception().getStackTrace()[0].getLineNumber());
//				else
//					;//System.out.println("Success!!");
//			}
//		}
//	}
	
	private void validateAddToDTSCForContentType(XPathHandler xHandler, String typeName, DTVO qbdtVO) throws SRTDAOException, XPathExpressionException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		
		
		//if QBDT is based on CodeContentType or IDContentType, check inherit sc of base & sc max card = 0
		
		if(!qbdtVO.getDEN().contains("_"))
			qbdtVO = getDTVO(qbdtVO.getBasedDTID());
		
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
		SRTDAO cdtSCAPMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		QueryCondition qc = new QueryCondition();
		qc.add("owner_dt_iD", qbdtVO.getBasedDTID());
		int owner_dT_iD = qbdtVO.getDTID();
		
		ArrayList<SRTObject> basedtsc_vos = aDTSCDAO.findObjects(qc, conn);
		for(SRTObject sObj : basedtsc_vos) {
			DTSCVO basedtsc_vo = (DTSCVO)sObj;
			
			QueryCondition qc2 = new QueryCondition();
			qc2.add("owner_dt_iD", qbdtVO.getDTID());	
			qc2.add("Base_dt_sc_id", basedtsc_vo.getDTSCID());
			DTSCVO thisdtsc_vo = (DTSCVO) aDTSCDAO.findObject(qc2, conn);
			
			String fromBaseDTSCStr="";
			String thisDTSCStr="";
			
			fromBaseDTSCStr = fromBaseDTSCStr+basedtsc_vo.getDTSCGUID()+basedtsc_vo.getPropertyTerm()
			+basedtsc_vo.getRepresentationTerm()+basedtsc_vo.getDefinition()+basedtsc_vo.getMinCardinality()+"0"//max card = 0 for contentType
			+basedtsc_vo.getDTSCID();//Based_dt_sc_id is dt_sc_id of base 
			
			thisDTSCStr=thisDTSCStr+thisdtsc_vo.getDTSCGUID()+thisdtsc_vo.getPropertyTerm()
			+thisdtsc_vo.getRepresentationTerm()+thisdtsc_vo.getDefinition()+thisdtsc_vo.getMinCardinality()+thisdtsc_vo.getMaxCardinality()
			+thisdtsc_vo.getBasedDTSCID();
			
			if(!fromBaseDTSCStr.equals(thisDTSCStr)){
				System.out.println("############DTSC IS NOT GENERATED! Check DT_ID: "+thisdtsc_vo.getOwnerDTID()+" & its base DT_SC_ID: "+ thisdtsc_vo.getBasedDTSCID());;
			}
			
			validateInsertBDTSCPrimitiveRestriction(getDTSCVO(basedtsc_vo.getDTSCGUID(), owner_dT_iD), 1, "", "");
			
		}
	}
	
	private void validateAddToDTSC(XPathHandler xHandler, String typeName, DTVO qbdtVO) throws SRTDAOException, XPathExpressionException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		
		
		//check inherit sc of base & added from attributes
			if(!qbdtVO.getDEN().contains("_"))
			qbdtVO = getDTVO(qbdtVO.getBasedDTID());
		
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
		SRTDAO cdtSCAPMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		QueryCondition qc = new QueryCondition();
		qc.add("owner_dt_iD", qbdtVO.getBasedDTID());
		int owner_dT_iD = qbdtVO.getDTID();
		
		ArrayList<SRTObject> dtsc_vos = aDTSCDAO.findObjects(qc, conn);
		for(SRTObject sObj : dtsc_vos) {
			DTSCVO dtsc_vo = (DTSCVO)sObj;
			
			QueryCondition qc2 = new QueryCondition();
			qc2.add("guid", dtsc_vo.getDTSCGUID());
			qc2.add("owner_dt_id", owner_dT_iD);
			validateInsertBDTSCPrimitiveRestriction(getDTSCVO(dtsc_vo.getDTSCGUID(), owner_dT_iD), 1, "", "");
		}
		
		NodeList attributeList = xHandler.getNodeList("//xsd:complexType[@id = '" + qbdtVO.getDTGUID() + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
		
		if(attributeList == null || attributeList.getLength() == 0) {
		} else {
			String dt_sc_guid = "";
			String property_term = "";
			String representation_term = "";
			
			String definition;
			int min_cardinality = 0;
			int max_cardinality = 0;
			
			for(int i = 0; i < attributeList.getLength(); i++) {
				Node attribute = attributeList.item(i);
				Element attrElement = (Element)attribute;
				dt_sc_guid = attrElement.getAttribute("id");

				if(attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
					min_cardinality = 0;
				else if(attrElement.getAttribute("use").equalsIgnoreCase("required"))
					min_cardinality = 1;
				
				if(attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("required"))
					max_cardinality = 1;
				else if(attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
					max_cardinality = 0;
				
				String attrName = attrElement.getAttribute("name");
				
				// Property Term
				if(attrName.endsWith("Code"))  
					property_term = attrName.substring(0, attrName.indexOf("Code"));
				else if(attrName.endsWith("ID")) 
					property_term = attrName.substring(0, attrName.indexOf("ID"));
				else if(attrName.endsWith("Value"))	
					property_term = attrName.substring(0, attrName.indexOf("Value"));
				else
					property_term = Utility.spaceSeparator(attrName);
				
				if(property_term.trim().length() == 0)
					property_term = attrName;
				
				property_term = Utility.firstToUpperCase(property_term);
				
				// Representation Term
				if(attrName.endsWith("Code") || attrName.endsWith("code")) {
					representation_term = "Code";
				} else if(attrName.endsWith("Number")) {
					representation_term = "Number";
				} else if(attrName.endsWith("ID")) {
					representation_term = "Identifier";
				} else if(attrName.endsWith("DateTime")) {
					representation_term = "Date Time";
				} else if(attrName.endsWith("Value")) {
					representation_term = "Value";
				} else if(attrName.endsWith("Name") || attrName.endsWith("name")) {
					representation_term = "Name";
				} else {
					String attrType = attrElement.getAttribute("type");
					if(attrType.equals("StringType") || attrType.equals("NormalizedStringType"))
						representation_term = "Text";
					else if(attrType.equals("IndicatorType"))
						representation_term = "Indicator";
				}
					
				Node documentationNode = xHandler.getNode("//xsd:complexType[@id = '" + qbdtVO.getDTGUID() + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation");
				if(documentationNode != null) {
					Node documentationFromCCTS = xHandler.getNode("//xsd:complexType[@id = '" + qbdtVO.getDTGUID() + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
					if(documentationFromCCTS != null)
						definition = ((Element)documentationFromCCTS).getTextContent();
					else
						definition = ((Element)documentationNode).getTextContent();
				} else {
					definition = null;
				}
					
				DTSCVO vo = new DTSCVO();
				vo.setDTSCGUID(dt_sc_guid);
				vo.setPropertyTerm(Utility.spaceSeparator(property_term));
				vo.setRepresentationTerm(representation_term);
				vo.setDefinition(definition);
				vo.setOwnerDTID(owner_dT_iD);
				
				vo.setMinCardinality(min_cardinality);
				vo.setMaxCardinality(max_cardinality);
				
				DTSCVO duplicate = checkDuplicate(vo);
				if(duplicate != null) {
					
					QueryCondition qc_01 = new QueryCondition();
					qc_01.add("owner_dt_iD", vo.getOwnerDTID());
					qc_01.add("guid", vo.getDTSCGUID());
					
					DTSCVO dtscVO = (DTSCVO)aDTSCDAO.findObject(qc_01, conn);
					String representationTerm = dtscVO.getRepresentationTerm();
					DTVO dtVO = getDTVOWithRepresentationTerm(representationTerm);
					
					CDTSCAllowedPrimitiveVO cdtSCAllowedVO = new CDTSCAllowedPrimitiveVO();
					cdtSCAllowedVO.setCDTSCID(dtscVO.getDTSCID());
					ArrayList<SRTObject> cdtallowedprimitivelist = getCDTAllowedPrimitiveIDs(dtVO.getDTID());
					
					SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
					for(SRTObject dvo : cdtallowedprimitivelist){
						CDTAllowedPrimitiveVO svo = (CDTAllowedPrimitiveVO) dvo;
						cdtSCAllowedVO.setCDTPrimitiveID(svo.getCDTPrimitiveID());
						QueryCondition qc_02 = new QueryCondition();
						qc_02.add("cdt_pri_id", svo.getCDTPrimitiveID());

						if(aCDTSCAllowedPrimitiveDAO.findObject(qc_02, conn) == null)
							System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
						else
							;//System.out.println("Success!!");
						
						QueryCondition qc_03 = new QueryCondition();
						qc_03.add("cdt_sc_id", cdtSCAllowedVO.getCDTSCID());
						qc_03.add("cdt_pri_id", cdtSCAllowedVO.getCDTPrimitiveID());
						int cdtSCAllowedPrimitiveId = ((CDTSCAllowedPrimitiveVO)aCDTSCAllowedPrimitiveDAO.findObject(qc_03, conn)).getCDTSCAllowedPrimitiveID();
						List<String> xsdbs = Types.getCorrespondingXSDBuiltType(getPrimitiveName(cdtSCAllowedVO.getCDTPrimitiveID()));
						for(String xbt : xsdbs) {
							QueryCondition qc_04 = new QueryCondition();
							qc_04.add("builtin_type", xbt);
							int xdtBuiltTypeId = ((XSDBuiltInTypeVO)aXSDBuiltInTypeDAO.findObject(qc_04, conn)).getXSDBuiltInTypeID();
							
							QueryCondition qc_05 = new QueryCondition();
							qc_05.add("cdt_sc_awd_pri", cdtSCAllowedPrimitiveId);
							qc_05.add("xbt_id", xdtBuiltTypeId);
							if(cdtSCAPMapDAO.findObject(qc_05, conn) == null)
								System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
							else
								;//System.out.println("Success!!");
						}
					}
					
					validateInsertBDTSCPrimitiveRestriction(getDTSCVO(dt_sc_guid), 0, attrElement.getAttribute("name"), attrElement.getAttribute("type"));
				} else {
					;//System.out.println("No prob!!");
				}
			}
		}
	}
	
	private List<SRTObject> getCdtSCAPMap(int cdtSCAllowedPrimitiveId) throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO cdtSCAPMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		QueryCondition qc = new QueryCondition();
		qc.add("cdt_sc_awd_pri", cdtSCAllowedPrimitiveId);
		return cdtSCAPMapDAO.findObjects(qc, conn);
	}
	
	private DTSCVO checkDuplicate(DTSCVO dtVO) throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("representation_term", dtVO.getRepresentationTerm());
		qc.add("property_term", dtVO.getPropertyTerm());
		qc.add("owner_dt_id", dtVO.getOwnerDTID());
		return (DTSCVO)aDTSCDAO.findObject(qc, conn);
	}
	
	public int getCodeListID(String codeName) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCodeListDAO = df.getDAO("CodeList");
		QueryCondition qc = new QueryCondition();
		qc.addLikeClause("Name", "%" + codeName.trim() + "%");
		CodeListVO codelistVO = (CodeListVO)aCodeListDAO.findObject(qc, conn);
		return codelistVO.getCodeListID();
	}
	
	public int getAgencyListID() throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDList");
    	QueryCondition qc = new QueryCondition();
		qc.add("name", "Agency Identification");
		AgencyIDListVO agencyidlistVO = (AgencyIDListVO)dao.findObject(qc);
		return agencyidlistVO.getAgencyIDListID();
	}
	
	public DTSCVO getDTSCVO(String guid) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("GUID", guid);
		return (DTSCVO)aDTSCDAO.findObject(qc);
	}
	
	public DTSCVO getDTSCVO(String guid, int ownerId) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("GUID", guid);
		qc.add("owner_dt_id", ownerId);
		return (DTSCVO)aDTSCDAO.findObject(qc);
	}
	
	public int getDTID(String DataTypeTerm) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", new String(DataTypeTerm));
		qc.add("Type", 0);
		DTVO dtVO = (DTVO)aDTDAO.findObject(qc);		
		int id = dtVO.getDTID();
		return id;
	}
	
	public int getCDTSCID(int DTSCID) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_ID", DTSCID);
		CDTSCAllowedPrimitiveVO cdtVO = (CDTSCAllowedPrimitiveVO)aCDTSCAllowedPrimitiveDAO.findObject(qc);
		int id = cdtVO.getCDTSCID();
		return id;
	}
	
	public String getRepresentationTerm(String DTSCGUID) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("GUID", new String(DTSCGUID));
		DTSCVO dtscVO = (DTSCVO)aDTSCDAO.findObject(qc);
		String term = dtscVO.getRepresentationTerm();
		return term;
	}
	
	public String getPrimitiveName(int CDTPrimitiveID) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_Pri_ID", CDTPrimitiveID);
		return ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc)).getName();
	}
	
	
	public int getCDTPrimitiveID(String name) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("Name",  name);
		return ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc)).getCDTPrimitiveID();
	}
	
	public ArrayList<SRTObject> getCDTAllowedPrimitiveIDs(int cdt_id) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_ID", cdt_id);
		return aCDTAllowedPrimitiveDAO.findObjects(qc, conn);
	}
	
	public List<SRTObject> getCdtSCAllowedPrimitiveID(int dt_sc_id) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		QueryCondition qc = new QueryCondition();
		qc.add("CDT_SC_ID", dt_sc_id);
		List<SRTObject> res = aCDTSCAllowedPrimitiveDAO.findObjects(qc, conn);
		if(res.size() < 1) {
			QueryCondition qc_01 = new QueryCondition();
			qc_01.add("DT_SC_ID", dt_sc_id);
			DTSCVO dtscVO = (DTSCVO)aDTSCDAO.findObject(qc_01);
			res = getCdtSCAllowedPrimitiveID(dtscVO.getBasedDTSCID());
		}
		return res;
	}
	
	public int getXSDBuiltInTypeID(String BuiltIntype) throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
    	QueryCondition qc = new QueryCondition();
		qc.add("BuiltIn_Type", new String(BuiltIntype));
		return ((XSDBuiltInTypeVO)aXSDBuiltInTypeDAO.findObject(qc)).getXSDBuiltInTypeID();
	}
	
	private DTVO getDTVOWithDEN(String den) throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		if(den.equalsIgnoreCase("MatchCode. Type"))
			den = "Match_ Code. Type";
		else if(den.equalsIgnoreCase("Description. Type"))
			den = "Description_ Text. Type";
		qc.add("den", den);
		qc.add("type", 1);
		
		return (DTVO)aDTDAO.findObject(qc, conn);
	}
	
	private DTVO getDTVOWithRepresentationTerm(String representationTerm) throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("data_type_term", representationTerm);
		qc.add("type", 0);
		return (DTVO)aDTDAO.findObject(qc, conn);
	}
	
	private DTVO getDTVO(int dtid) throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("dt_id", dtid);
		return (DTVO)aDTDAO.findObject(qc, conn);
	}
	
	private void validate_bdt_pri_resti(String datatype) throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("den", Utility.typeToDen(datatype));
		int bdt_id = ((DTVO)aDTDAO.findObject(qc, conn)).getDTID();
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		QueryCondition qc2 = new QueryCondition();
		qc2.add("bdt_id", bdt_id);

		List<SRTObject> bdt_pri_resti_list = aBDTPrimitiveRestrictionDAO.findObjects(qc2, conn);

		ArrayList<String> bdt_pri_list = new ArrayList<String>();

		if(((DTVO)aDTDAO.findObject(qc, conn)).getDEN().equals("Amount_0723C8. Type")){
			bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeDecimalxsd:decimal");
			bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeDoublexsd:double");
			bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeDoublexsd:float");
			bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeFloatxsd:float");
			bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeIntegerxsd:integer");
			bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeIntegerxsd:nonNegativeInteger");
			bdt_pri_list.add("Amount_0723C8. TypeAmount. TypeIntegerxsd:positiveInteger");
		}
		
		else if(((DTVO)aDTDAO.findObject(qc, conn)).getDEN().equals("Code_1DEB05. Type")){
			bdt_pri_list.add("Code_1DEB05. TypeCode. TypeNormalizedStringxsd:normalizedString");
			bdt_pri_list.add("Code_1DEB05. TypeCode. TypeStringxsd:string");
			bdt_pri_list.add("Code_1DEB05. TypeCode. TypeTokenxsd:token");
		}
		

		ArrayList<String> bdt_pri_fromDB = new ArrayList<String>();
		
		for(SRTObject bdtpriresti : bdt_pri_resti_list) {
			BdtPriResti aBdtPriResti = new BdtPriResti(((BDTPrimitiveRestrictionVO)bdtpriresti).getBDTPrimitiveRestrictionID());
			if(bdt_pri_list.indexOf(aBdtPriResti.getData()) == -1) 
				System.out.println("Data error in bdt_pri_resti. Data is " + aBdtPriResti.getData());
			bdt_pri_fromDB.add(aBdtPriResti.getData());
		}
		
		for(String bdtpri : bdt_pri_list){
				if(bdt_pri_fromDB.indexOf(bdtpri) == -1 ) System.out.println("Data may be missing in the bdt_pri_resti table. Data is " + bdtpri);
		}
	}
	
	private void validate_default_bdt() throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		ArrayList<String> default_bdt = new ArrayList<String>();
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
	
		for(int i = 0 ; i < default_bdt.size(); i ++) {
			qc.add("den", default_bdt.get(i));
			if(aDTDAO.findObject(qc, conn) == null)
				System.out.println("There is no default bdt in dt table : "+default_bdt.get(i));
			qc = new QueryCondition();
		}
	}
	class BdtPriResti {
		int id;
		String bdtDen;
		String cdtDen;
		String cdtPriTerm;
		String xsdBuiltInType;
		
		public BdtPriResti(int id) throws SRTDAOException {
			DAOFactory df;
			df = DAOFactory.getDAOFactory();

			SRTDAO aBDTPrimitiveRestriction = df.getDAO("BDTPrimitiveRestriction");
			
			QueryCondition qc = new QueryCondition();
			qc.add("bdt_pri_restri_id", id);
			BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO= (BDTPrimitiveRestrictionVO)aBDTPrimitiveRestriction.findObject(qc, conn);
			int bdt_id;
			bdt_id = aBDTPrimitiveRestrictionVO.getBDTID();
			
			SRTDAO aDT = df.getDAO("DT");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("dt_id", bdt_id);
			DTVO aDTVO= (DTVO)aDT.findObject(qc2, conn);
			
			bdtDen = aDTVO.getDEN();
			
			int cdt_awd_pri_xps_type_map_id = aBDTPrimitiveRestrictionVO.getCDTPrimitiveExpressionTypeMapID();
			
			SRTDAO aCDTAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
			QueryCondition qc3 = new QueryCondition();
			qc3.add("cdt_awd_pri_xps_type_map_id", cdt_awd_pri_xps_type_map_id);
			CDTAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExpressionTypeMapVO= (CDTAllowedPrimitiveExpressionTypeMapVO)aCDTAllowedPrimitiveExpressionTypeMap.findObject(qc3, conn);
			
			int cdt_awd_pri_id = aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTAllowedPrimitiveID();
			int xbt_id = aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID();
			
			SRTDAO aCDTAllowedPrimitive = df.getDAO("CDTAllowedPrimitive");
			QueryCondition qc4 = new QueryCondition();
			qc4.add("cdt_awd_pri_id", cdt_awd_pri_id);
			CDTAllowedPrimitiveVO cdtAllowedPrimitiveVO = (CDTAllowedPrimitiveVO)aCDTAllowedPrimitive.findObject(qc4, conn);
			
			SRTDAO aCDTPrimitive = df.getDAO("CDTPrimitive");
			QueryCondition qc5 = new QueryCondition();
			qc5.add("cdt_pri_id", cdtAllowedPrimitiveVO.getCDTPrimitiveID());
			CDTPrimitiveVO cdtPrimitiveVO = (CDTPrimitiveVO) aCDTPrimitive.findObject(qc5, conn);
			cdtPriTerm = cdtPrimitiveVO.getName();
			
			QueryCondition qc6 = new QueryCondition();
			qc6.add("dt_id", cdtAllowedPrimitiveVO.getCDTID());
			DTVO cdtVO = (DTVO) aDT.findObject(qc6, conn);
			cdtDen = cdtVO.getDEN();
			
			SRTDAO aXbt = df.getDAO("XSDBuiltInType");
			QueryCondition qc7 = new QueryCondition();
			qc7.add("xbt_id", xbt_id);
			XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO) aXbt.findObject(qc7, conn);
			xsdBuiltInType = xsdBuiltInTypeVO.getBuiltInType();

		}
		
		public String getData() {
			return this.bdtDen + this.cdtDen + this.cdtPriTerm + this.xsdBuiltInType;
		}
		
	}
	
	private void validteDTSC() throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		
		ArrayList<SRTObject> DTSCList = aDTSCDAO.findObjects();
		HashMap <SRTObject, Boolean> map = new HashMap <SRTObject, Boolean>();
		for(SRTObject DTSC : DTSCList)
			map.put(DTSC, false);
		
		for(int i = 0; i < DTSCList.size(); i++){
			SRTObject DTSC = DTSCList.get(i);
			if(map.get(DTSC))
				continue;
			DTSCVO dtscvo = (DTSCVO) DTSC;
			if(dtscvo.getBasedDTSCID() == 0)
				continue;
			int dt_id = dtscvo.getOwnerDTID();
			QueryCondition qc = new QueryCondition();
			qc.add("dt_id", dt_id);
			DTVO ownerDT = (DTVO) aDTDAO.findObject(qc, conn);
			if(ownerDT.getDEN().contains("_")) {// if BDT is default BDT
				QueryCondition qc2 = new QueryCondition();
				qc2.add("dt_sc_id", dtscvo.getBasedDTSCID());
				DTSCVO basedDTscvo = (DTSCVO) aDTSCDAO.findObject(qc2, conn);
				if(basedDTscvo.getOwnerDTID() != ownerDT.getBasedDTID())
					System.out.println("DTSC based on default BDT is wrong when dt_sc_id = "+dtscvo.getDTSCID());
				else
					System.out.println("good when dt_sc_id = "+dtscvo.getDTSCID());
				map.put(DTSC, true);
				
			}
			else { // if BDT is qualified BDT
				
			}
		}
		
	}
	private static Connection conn = null;
	
	public void run() throws Exception {
		System.out.println("### DataType Validation Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();

		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
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
		tx.close();
		conn.close();
		System.out.println("### DataType Validation End");
	}
	
	public static void main(String[] args) throws Exception {
		Utility.dbSetup();
		DataTypeTest p = new DataTypeTest();
		p.run();
	}
}
