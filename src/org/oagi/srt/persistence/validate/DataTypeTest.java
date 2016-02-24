package org.oagi.srt.persistence.validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
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
	
	public DTVO validateInsertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition, String ccDefinition, String id) throws SRTDAOException{
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
						
					DTVO dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, (definitionElement != null) ? definitionElement.getTextContent() : null, (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
					
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
		
		DTVO dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
		
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
		
		DTVO dVO1 = validateInsertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
		
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
				System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
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
		validateInsertDT(elementsFromFieldsXSD, fields_xsd, 0);
	}
	
	
	private void validateInsertDT(NodeList elementsFromXSD, XPathHandler org_xHandler, int xsdType) throws XPathExpressionException, SRTDAOException {
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
				Node documentationFromXSD = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation");
				String definition = "";
				if(documentationFromXSD != null) {
					Node documentationFromCCTS = xHandler.getNode("/xsd:schema/xsd:element[@name = '" + bccp + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
					if(documentationFromCCTS != null)
						definition = ((Element)documentationFromCCTS).getTextContent();
					else
						definition = ((Element)documentationFromXSD).getTextContent();
				}
				
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
				
				QueryCondition qc2 = new QueryCondition();
				qc2.add("GUID", typeGuid);
				if(dtVO != null) {
					DTVO dVO = validateAddToDT(typeGuid, type, typeNode, xHandler);
					if(dVO != null)
						validateAddToDTSC(xHandler, type, dVO);
				} else {
					System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
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
		if(mode == 1) {
			List<SRTObject> bdtscs = getBDTSCPrimitiveRestriction(dtscVO);
			for(SRTObject obj : bdtscs) {
				BDTSCPrimitiveRestrictionVO parent = (BDTSCPrimitiveRestrictionVO)obj;
				
				QueryCondition qc1 = new QueryCondition();
				qc1.add("bdt_sc_id", dtscVO.getDTSCID());
				qc1.add("cdt_sc_awd_pri_xps_type_map_id", parent.getCDTSCAllowedPrimitiveExpressionTypeMapID());
				qc1.add("code_list_id", parent.getCodeListID());
				qc1.add("agency_id_list_id", parent.getAgencyIDListID());
				if(bdtSCPRDAO.findObject(qc1, conn) == null)
					System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
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
						System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
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
		
	private DTVO validateAddToDT(String guid, String type, Node typeNode, XPathHandler xHandler) throws XPathExpressionException, SRTDAOException {
			DTVO dVO = new DTVO();
			
			Element extension = (Element)((Element)typeNode).getElementsByTagName("xsd:extension").item(0);
			if(extension == null || extension.getAttribute("base") == null)
				return null;
			String base = extension.getAttribute("base");
			
			if(base.endsWith("CodeContentType")) {
				dVO = getDTVOWithDEN("Code. Type");
			} else {
				String den = Utility.createDENFormat(base);
				dVO = getDTVOWithDEN(den);
				
				// QBDT is based on another QBDT
				if(dVO != null) {
					;//System.out.println("Success!!");
				}
				else {
					System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber()+"  den = "+den);
					return null;
				}
			}
			DAOFactory df;
			df = DAOFactory.getDAOFactory();
			SRTDAO aDTDAO = df.getDAO("DT");
			QueryCondition qc1 = new QueryCondition();
			qc1.add("guid", dVO.getDTGUID());
			if(aDTDAO.findObject(qc1, conn) == null){
				System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
				return null;
			}
			
			else {
				QueryCondition qc2 = new QueryCondition();
				qc2.add("guid", guid);
				DTVO res = (DTVO)aDTDAO.findObject(qc2, conn);
				validateInsertBDTPrimitiveRestriction(res, base);
				return res;
			}
	}
	
	private void validateInsertBDTPrimitiveRestriction(DTVO dVO, String base) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		
		QueryCondition qc = new QueryCondition();
		qc.add("bdt_id", dVO.getBasedDTID());
		ArrayList<SRTObject> al = aBDTPrimitiveRestrictionDAO.findObjects(qc, conn);
		
		if(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && !(dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType"))) {
			QueryCondition qc2 = new QueryCondition();

			BDTPrimitiveRestrictionVO theBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
			theBDT_Primitive_RestrictionVO.setBDTID(dVO.getDTID());
			if(base.endsWith("CodeContentType")) {
				qc2.add("code_list_id", getCodeListID(base.substring(0, base.indexOf("CodeContentType"))));
			} else {
				for(SRTObject aSRTObject : al) {
					BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
					if(aBDTPrimitiveRestrictionVO.getCodeListID() > 0) {
						qc2.add("code_list_id", aBDTPrimitiveRestrictionVO.getCodeListID());
						break;
					}
				}
			}
			if(aBDTPrimitiveRestrictionDAO.findObject(qc2, conn) == null)
				System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
			else
				;//System.out.println("Success!!");
		} 
		
		if(!dVO.getDataTypeTerm().equalsIgnoreCase("Code") || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.equalsIgnoreCase("CodeType")) || (dVO.getDataTypeTerm().equalsIgnoreCase("Code") && base.endsWith("CodeContentType"))){
			for(SRTObject aSRTObject : al) {
				BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
				QueryCondition qc2 = new QueryCondition();
				qc2.add("bdt_id", dVO.getDTID());
				qc2.add("cdt_awd_pri_xps_type_map_id", aBDTPrimitiveRestrictionVO.getCDTPrimitiveExpressionTypeMapID());

				if(aBDTPrimitiveRestrictionDAO.findObject(qc2, conn) == null)
					System.out.println("Error!"+new Exception().getStackTrace()[0].getLineNumber());
				else
					;//System.out.println("Success!!");
			}
		}
	}
	
	private void validateAddToDTSC(XPathHandler xHandler, String typeName, DTVO qbdtVO) throws SRTDAOException, XPathExpressionException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		
		
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
		
		// new SC
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
	
	private void check_number_of_CDT() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 0);
		List<SRTObject> cdt = aDTDAO.findObjects(qc, conn);
		System.out.println("# of CDTs in catalog : "+cdt.size());
		if(cdt.size() == 23)
			System.out.println("Validated");
	}
	
	private void check_number_of_XBT() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
		List<SRTObject> xsd = aXSDBuiltInTypeDAO.findObjects();
		System.out.println("# of XBTs in catalog : "+xsd.size());
		for(int i = 0 ; i < xsd.size(); i++){
			for(int j = i+1 ; j < xsd.size(); j++){
				if(((XSDBuiltInTypeVO)(xsd.get(i))).getName().equalsIgnoreCase((((XSDBuiltInTypeVO)(xsd.get(j))).getName())))
						System.out.println("Strange data; see the "+i+"th data and "+j+" th data");
			}
		}
		if(xsd.size() == 24)
			System.out.println("Validated");
	}
	
	private void validate_cdt_sc() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		qc.add("type", 0);
		List<SRTObject> cdtlist = aDTDAO.findObjects(qc, conn);
		ArrayList<String> cdt_sc_list = new ArrayList<String>();
		
		cdt_sc_list.add("AmountCurrencyCode01");
		cdt_sc_list.add("Binary ObjectMIMECode01");
		cdt_sc_list.add("Binary ObjectCharacter SetCode01");
		cdt_sc_list.add("Binary ObjectFilenameName01");
		cdt_sc_list.add("CodeListIdentifier01");
		cdt_sc_list.add("CodeList AgencyIdentifier01");
		cdt_sc_list.add("CodeList VersionIdentifier01");
		cdt_sc_list.add("Date TimeTime ZoneCode01");
		cdt_sc_list.add("Date TimeDaylight SavingIndicator01");
		cdt_sc_list.add("GraphicMIMECode01");
		cdt_sc_list.add("GraphicCharacter SetCode01");
		cdt_sc_list.add("GraphicFilenameName01");
		cdt_sc_list.add("IdentifierSchemeIdentifier01");
		cdt_sc_list.add("IdentifierScheme VersionIdentifier01");
		cdt_sc_list.add("IdentifierScheme AgencyIdentifier01");
		cdt_sc_list.add("MeasureUnitCode01");
		cdt_sc_list.add("NameLanguageCode01");
		cdt_sc_list.add("PictureMIMECode01");
		cdt_sc_list.add("PictureCharacter SetCode01");
		cdt_sc_list.add("PictureFilenameName01");
		cdt_sc_list.add("QuantityUnitCode01");
		cdt_sc_list.add("RateMultiplierValue01");
		cdt_sc_list.add("RateUnitCode01");
		cdt_sc_list.add("RateCurrencyCode01");
		cdt_sc_list.add("RateBase MultiplierValue01");
		cdt_sc_list.add("RateBase UnitCode01");
		cdt_sc_list.add("RateBase CurrencyCode01");
		
		cdt_sc_list.add("SoundMIMECode01");
		cdt_sc_list.add("SoundCharacter SetCode01");
		cdt_sc_list.add("SoundFilenameName01");
		cdt_sc_list.add("TextLanguageCode01");
		cdt_sc_list.add("VideoMIMECode01");
		cdt_sc_list.add("VideoCharacter SetCode01");
		cdt_sc_list.add("VideoFilenameName01");
		
		
		ArrayList<String> cdtscFromDB = new ArrayList<String>();
		ArrayList<SRTObject> cdtsclistfromDB = new ArrayList<SRTObject>();
		for(SRTObject cdt : cdtlist){
			SRTDAO aDTSCDAO = df.getDAO("DTSC");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("owner_dt_id", ((DTVO)cdt).getDTID() );
			cdtsclistfromDB.addAll(aDTSCDAO.findObjects(qc2, conn));
		}
		
		for(SRTObject cdtsc : cdtsclistfromDB) {
			CDT aCDT = new CDT(((DTSCVO)cdtsc).getDTSCID());
			if(cdt_sc_list.indexOf(aCDT.getCDTData()) == -1) 
				System.out.println("Data error in dt. Data is " + aCDT.getCDTData());
			cdtscFromDB.add(aCDT.getCDTData());
		}
		
		for(String cdtsc : cdt_sc_list){
				if(cdtscFromDB.indexOf(cdtsc) == -1 ) System.out.println("Data may be missing in the dt_sc table. Data is " + cdtsc);
		}
		
	}
		
	private void validate_cdt_sc_awd_pri_xps_type_map() throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		
		ArrayList<String> cdtscxpsMapData = new ArrayList<String>(); 
		
		cdtscxpsMapData.add("AmountCurrencyCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("AmountCurrencyCodeStringxsd:string");
		cdtscxpsMapData.add("AmountCurrencyCodeTokenxsd:token");
		cdtscxpsMapData.add("Binary ObjectMIMECodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("Binary ObjectMIMECodeStringxsd:string");
		cdtscxpsMapData.add("Binary ObjectMIMECodeTokenxsd:token");
		cdtscxpsMapData.add("Binary ObjectCharacter SetCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("Binary ObjectCharacter SetCodeStringxsd:string");
		cdtscxpsMapData.add("Binary ObjectCharacter SetCodeTokenxsd:token");
		cdtscxpsMapData.add("Binary ObjectFilenameNameNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("Binary ObjectFilenameNameStringxsd:string");
		cdtscxpsMapData.add("Binary ObjectFilenameNameTokenxsd:token");
		cdtscxpsMapData.add("CodeListIdentifierNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("CodeListIdentifierStringxsd:string");
		cdtscxpsMapData.add("CodeListIdentifierTokenxsd:token");
		cdtscxpsMapData.add("CodeList AgencyIdentifierNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("CodeList AgencyIdentifierStringxsd:string");
		cdtscxpsMapData.add("CodeList AgencyIdentifierTokenxsd:token");
		cdtscxpsMapData.add("CodeList VersionIdentifierNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("CodeList VersionIdentifierStringxsd:string");
		cdtscxpsMapData.add("CodeList VersionIdentifierTokenxsd:token");
		
		cdtscxpsMapData.add("Date TimeTime ZoneCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("Date TimeTime ZoneCodeStringxsd:string");
		cdtscxpsMapData.add("Date TimeTime ZoneCodeTokenxsd:token");
		
		cdtscxpsMapData.add("Date TimeDaylight SavingIndicatorBooleanxsd:boolean");

		cdtscxpsMapData.add("GraphicMIMECodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("GraphicMIMECodeStringxsd:string");
		cdtscxpsMapData.add("GraphicMIMECodeTokenxsd:token");
		cdtscxpsMapData.add("GraphicCharacter SetCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("GraphicCharacter SetCodeStringxsd:string");
		cdtscxpsMapData.add("GraphicCharacter SetCodeTokenxsd:token");
		cdtscxpsMapData.add("GraphicFilenameNameNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("GraphicFilenameNameStringxsd:string");
		cdtscxpsMapData.add("GraphicFilenameNameTokenxsd:token");

		cdtscxpsMapData.add("IdentifierSchemeIdentifierNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("IdentifierSchemeIdentifierStringxsd:string");
		cdtscxpsMapData.add("IdentifierSchemeIdentifierTokenxsd:token");
		cdtscxpsMapData.add("IdentifierScheme VersionIdentifierNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("IdentifierScheme VersionIdentifierStringxsd:string");
		cdtscxpsMapData.add("IdentifierScheme VersionIdentifierTokenxsd:token");
		cdtscxpsMapData.add("IdentifierScheme AgencyIdentifierNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("IdentifierScheme AgencyIdentifierStringxsd:string");
		cdtscxpsMapData.add("IdentifierScheme AgencyIdentifierTokenxsd:token");
		
		cdtscxpsMapData.add("MeasureUnitCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("MeasureUnitCodeStringxsd:string");
		cdtscxpsMapData.add("MeasureUnitCodeTokenxsd:token");
		
		cdtscxpsMapData.add("NameLanguageCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("NameLanguageCodeStringxsd:string");
		cdtscxpsMapData.add("NameLanguageCodeTokenxsd:token");
		
		cdtscxpsMapData.add("PictureMIMECodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("PictureMIMECodeStringxsd:string");
		cdtscxpsMapData.add("PictureMIMECodeTokenxsd:token");
		cdtscxpsMapData.add("PictureCharacter SetCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("PictureCharacter SetCodeStringxsd:string");
		cdtscxpsMapData.add("PictureCharacter SetCodeTokenxsd:token");
		cdtscxpsMapData.add("PictureFilenameNameNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("PictureFilenameNameStringxsd:string");
		cdtscxpsMapData.add("PictureFilenameNameTokenxsd:token");
		
		cdtscxpsMapData.add("QuantityUnitCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("QuantityUnitCodeStringxsd:string");
		cdtscxpsMapData.add("QuantityUnitCodeTokenxsd:token");
		
		cdtscxpsMapData.add("RateMultiplierValueDecimalxsd:decimal");
		cdtscxpsMapData.add("RateMultiplierValueDoublexsd:double");
		cdtscxpsMapData.add("RateMultiplierValueDoublexsd:float");
		cdtscxpsMapData.add("RateMultiplierValueFloatxsd:float");
		cdtscxpsMapData.add("RateMultiplierValueIntegerxsd:integer");
		cdtscxpsMapData.add("RateMultiplierValueIntegerxsd:nonNegativeInteger");
		cdtscxpsMapData.add("RateMultiplierValueIntegerxsd:positiveInteger");
		
		cdtscxpsMapData.add("RateUnitCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("RateUnitCodeStringxsd:string");
		cdtscxpsMapData.add("RateUnitCodeTokenxsd:token");
		
		cdtscxpsMapData.add("RateCurrencyCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("RateCurrencyCodeStringxsd:string");
		cdtscxpsMapData.add("RateCurrencyCodeTokenxsd:token");
		
		
		cdtscxpsMapData.add("RateBase MultiplierValueDecimalxsd:decimal");
		cdtscxpsMapData.add("RateBase MultiplierValueDoublexsd:double");
		cdtscxpsMapData.add("RateBase MultiplierValueDoublexsd:float");
		cdtscxpsMapData.add("RateBase MultiplierValueFloatxsd:float");
		cdtscxpsMapData.add("RateBase MultiplierValueIntegerxsd:integer");
		cdtscxpsMapData.add("RateBase MultiplierValueIntegerxsd:nonNegativeInteger");
		cdtscxpsMapData.add("RateBase MultiplierValueIntegerxsd:positiveInteger");
		
		cdtscxpsMapData.add("RateBase UnitCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("RateBase UnitCodeStringxsd:string");
		cdtscxpsMapData.add("RateBase UnitCodeTokenxsd:token");
		
		cdtscxpsMapData.add("RateBase CurrencyCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("RateBase CurrencyCodeStringxsd:string");
		cdtscxpsMapData.add("RateBase CurrencyCodeTokenxsd:token");
		
		cdtscxpsMapData.add("SoundMIMECodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("SoundMIMECodeStringxsd:string");
		cdtscxpsMapData.add("SoundMIMECodeTokenxsd:token");
		cdtscxpsMapData.add("SoundCharacter SetCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("SoundCharacter SetCodeStringxsd:string");
		cdtscxpsMapData.add("SoundCharacter SetCodeTokenxsd:token");
		cdtscxpsMapData.add("SoundFilenameNameNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("SoundFilenameNameStringxsd:string");
		cdtscxpsMapData.add("SoundFilenameNameTokenxsd:token");
		
		cdtscxpsMapData.add("TextLanguageCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("TextLanguageCodeStringxsd:string");
		cdtscxpsMapData.add("TextLanguageCodeTokenxsd:token");
		
		cdtscxpsMapData.add("VideoMIMECodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("VideoMIMECodeStringxsd:string");
		cdtscxpsMapData.add("VideoMIMECodeTokenxsd:token");
		cdtscxpsMapData.add("VideoCharacter SetCodeNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("VideoCharacter SetCodeStringxsd:string");
		cdtscxpsMapData.add("VideoCharacter SetCodeTokenxsd:token");
		cdtscxpsMapData.add("VideoFilenameNameNormalizedStringxsd:normalizedString");
		cdtscxpsMapData.add("VideoFilenameNameStringxsd:string");
		cdtscxpsMapData.add("VideoFilenameNameTokenxsd:token");
		
		SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		List<SRTObject> CDTSCAllowedPrimitiveExpressionTypeMapList = aCDTSCAllowedPrimitiveExpressionTypeMap.findObjects();
		ArrayList<String> cdtscxpsMapDataFromDB = new ArrayList<String>();
		for(SRTObject cdt_sc_allowed_primitive_expression_type_map : CDTSCAllowedPrimitiveExpressionTypeMapList){
				CDTSCExpressionTypeMap xpsMap = new CDTSCExpressionTypeMap(((CDTSCAllowedPrimitiveExpressionTypeMapVO)cdt_sc_allowed_primitive_expression_type_map).getCTSCAllowedPrimitiveExpressionTypeMapID());
				if (cdtscxpsMapData.indexOf(xpsMap.getXpsMapData()) == -1) System.out.println("Data error in cdt_sc_awd_pri_xps_map. Data is " + xpsMap.getXpsMapData());
				cdtscxpsMapDataFromDB.add(xpsMap.getXpsMapData());
		}
		
		for (String xpsMap : cdtscxpsMapData){
			if(cdtscxpsMapDataFromDB.indexOf(xpsMap) == -1) System.out.println("Data may be missing in the cdt_sc_awd_pri_xps_map table. Data is " + xpsMap);
		}	
	}
	
	private void validate_cdt_awd_pri_xps_type_map() throws SRTDAOException {
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		
		ArrayList<String> xpsMapData = new ArrayList<String>(); 
		xpsMapData.add("AmountDecimalxsd:decimal");
		xpsMapData.add("AmountDoublexsd:double");
		xpsMapData.add("AmountDoublexsd:float");
		xpsMapData.add("AmountFloatxsd:float");	
		xpsMapData.add("AmountIntegerxsd:integer");
		xpsMapData.add("AmountIntegerxsd:nonNegativeInteger");
		xpsMapData.add("AmountIntegerxsd:positiveInteger");
		
		xpsMapData.add("Binary ObjectBinaryxsd:base64Binary");
		xpsMapData.add("Binary ObjectBinaryxsd:hexBinary");
		
		xpsMapData.add("CodeNormalizedStringxsd:normalizedString");
		xpsMapData.add("CodeStringxsd:string");
		xpsMapData.add("CodeTokenxsd:token");
		xpsMapData.add("DateTimePointxsd:token");
		xpsMapData.add("DateTimePointxsd:date");
		xpsMapData.add("DateTimePointxsd:time");
		xpsMapData.add("DateTimePointxsd:gYearMonth");
		xpsMapData.add("DateTimePointxsd:gYear");
		xpsMapData.add("DateTimePointxsd:gMonthDay");
		xpsMapData.add("DateTimePointxsd:gDay");
		xpsMapData.add("DateTimePointxsd:gMonth");
		
		xpsMapData.add("Date TimeTimePointxsd:token");
		xpsMapData.add("Date TimeTimePointxsd:dateTime");
		xpsMapData.add("Date TimeTimePointxsd:date");
		xpsMapData.add("Date TimeTimePointxsd:time");
		xpsMapData.add("Date TimeTimePointxsd:gYearMonth");
		xpsMapData.add("Date TimeTimePointxsd:gYear");
		xpsMapData.add("Date TimeTimePointxsd:gMonthDay");
		xpsMapData.add("Date TimeTimePointxsd:gDay");
		xpsMapData.add("Date TimeTimePointxsd:gMonth");
		
		xpsMapData.add("DurationTimeDurationxsd:duration");
		xpsMapData.add("DurationTimeDurationxsd:token");
		
		xpsMapData.add("GraphicBinaryxsd:base64Binary");
		xpsMapData.add("GraphicBinaryxsd:hexBinary");
		
		xpsMapData.add("IdentifierNormalizedStringxsd:normalizedString");
		xpsMapData.add("IdentifierStringxsd:string");
		xpsMapData.add("IdentifierTokenxsd:token");
		
		xpsMapData.add("IndicatorBooleanxsd:boolean");
		
		xpsMapData.add("MeasureDecimalxsd:decimal");
		xpsMapData.add("MeasureDoublexsd:double");
		xpsMapData.add("MeasureDoublexsd:float");
		xpsMapData.add("MeasureFloatxsd:float");
		xpsMapData.add("MeasureIntegerxsd:integer");
		xpsMapData.add("MeasureIntegerxsd:nonNegativeInteger");
		xpsMapData.add("MeasureIntegerxsd:positiveInteger");
		
		xpsMapData.add("NameNormalizedStringxsd:normalizedString");
		xpsMapData.add("NameStringxsd:string");
		xpsMapData.add("NameTokenxsd:token");
		
		xpsMapData.add("NumberDecimalxsd:decimal");
		xpsMapData.add("NumberDoublexsd:double");
		xpsMapData.add("NumberDoublexsd:float");
		xpsMapData.add("NumberFloatxsd:float");
		xpsMapData.add("NumberIntegerxsd:integer");
		xpsMapData.add("NumberIntegerxsd:nonNegativeInteger");
		xpsMapData.add("NumberIntegerxsd:positiveInteger");
		
		xpsMapData.add("OrdinalIntegerxsd:integer");
		xpsMapData.add("OrdinalIntegerxsd:nonNegativeInteger");
		xpsMapData.add("OrdinalIntegerxsd:positiveInteger");
		
		xpsMapData.add("PercentDecimalxsd:decimal");
		xpsMapData.add("PercentDoublexsd:double");
		xpsMapData.add("PercentDoublexsd:float");
		xpsMapData.add("PercentFloatxsd:float");
		xpsMapData.add("PercentIntegerxsd:integer");
		xpsMapData.add("PercentIntegerxsd:nonNegativeInteger");
		xpsMapData.add("PercentIntegerxsd:positiveInteger");
		
		xpsMapData.add("PictureBinaryxsd:base64Binary");
		xpsMapData.add("PictureBinaryxsd:hexBinary");
		
		xpsMapData.add("QuantityDecimalxsd:decimal");
		xpsMapData.add("QuantityDoublexsd:double");
		xpsMapData.add("QuantityDoublexsd:float");
		xpsMapData.add("QuantityFloatxsd:float");
		xpsMapData.add("QuantityIntegerxsd:integer");
		xpsMapData.add("QuantityIntegerxsd:nonNegativeInteger");
		xpsMapData.add("QuantityIntegerxsd:positiveInteger");
		
		xpsMapData.add("RateDecimalxsd:decimal");
		xpsMapData.add("RateDoublexsd:double");
		xpsMapData.add("RateDoublexsd:float");
		xpsMapData.add("RateFloatxsd:float");
		xpsMapData.add("RateIntegerxsd:integer");
		xpsMapData.add("RateIntegerxsd:nonNegativeInteger");
		xpsMapData.add("RateIntegerxsd:positiveInteger");
		
		xpsMapData.add("RatioDecimalxsd:decimal");
		xpsMapData.add("RatioDoublexsd:double");
		xpsMapData.add("RatioDoublexsd:float");
		xpsMapData.add("RatioFloatxsd:float");
		xpsMapData.add("RatioIntegerxsd:integer");
		xpsMapData.add("RatioIntegerxsd:nonNegativeInteger");
		xpsMapData.add("RatioIntegerxsd:positiveInteger");
		xpsMapData.add("RatioStringxsd:string");

		xpsMapData.add("SoundBinaryxsd:base64Binary");
		xpsMapData.add("SoundBinaryxsd:hexBinary");
		
		xpsMapData.add("TextNormalizedStringxsd:normalizedString");
		xpsMapData.add("TextStringxsd:string");
		xpsMapData.add("TextTokenxsd:token");
		
		xpsMapData.add("TimeTimePointxsd:token");
		xpsMapData.add("TimeTimePointxsd:time");
		
		xpsMapData.add("ValueDecimalxsd:decimal");
		xpsMapData.add("ValueDoublexsd:double");
		xpsMapData.add("ValueDoublexsd:float");
		xpsMapData.add("ValueFloatxsd:float");
		xpsMapData.add("ValueIntegerxsd:integer");
		xpsMapData.add("ValueIntegerxsd:nonNegativeInteger");
		xpsMapData.add("ValueIntegerxsd:positiveInteger");
		xpsMapData.add("ValueNormalizedStringxsd:normalizedString");
		xpsMapData.add("ValueTokenxsd:token");
		xpsMapData.add("ValueStringxsd:string");
		
		xpsMapData.add("VideoBinaryxsd:base64Binary");
		xpsMapData.add("VideoBinaryxsd:hexBinary");
		
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		List<SRTObject> CDTAllowedPrimitiveExpressionTypeMapList = aCDTAllowedPrimitiveExpressionTypeMap.findObjects();
		ArrayList<String> xpsMapDataFromDB = new ArrayList<String>();
		for(SRTObject cdt_allowed_primitive_expression_type_map : CDTAllowedPrimitiveExpressionTypeMapList){
				CDTPrimitiveExpressionTypeMap xpsMap = new CDTPrimitiveExpressionTypeMap(((CDTAllowedPrimitiveExpressionTypeMapVO)cdt_allowed_primitive_expression_type_map).getCDTPrimitiveExpressionTypeMapID());
				if (xpsMapData.indexOf(xpsMap.getXpsMapData()) == -1) System.out.println("Data error in cdt_awd_pri_xps_map. Data is " + xpsMap.getXpsMapData());
				xpsMapDataFromDB.add(xpsMap.getXpsMapData());
		}
		
		for (String xpsMap : xpsMapData){
			if(xpsMapDataFromDB.indexOf(xpsMap) == -1) System.out.println("Data may be missing in the cdt_awd_pri_xps_map table. Data is " + xpsMap);
		}		
			
	}
	
	private void validate_agency_id_list() throws SRTDAOException{
		DAOFactory df;
		df = DAOFactory.getDAOFactory();
		SRTDAO aAgencyIDListValue = df.getDAO("AgencyIDListValue");
		List<SRTObject> AgencyIDListValueList = aAgencyIDListValue.findObjects();
		if(AgencyIDListValueList.size() == 395)
			System.out.println("# of Agency ID List Values is correct");
	}
	
	class CDT {
		int id;
		String cdtDTTerm;
		String scPropertyTerm;
		String scRepresentationTerm;
		int minCardinality;
		int maxCardinality;
		
		public CDT(int id) throws SRTDAOException {
			DAOFactory df;
			df = DAOFactory.getDAOFactory();

			SRTDAO aDT = df.getDAO("DT");
			SRTDAO aDTSC = df.getDAO("DTSC");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("dt_sc_id", id);
			DTSCVO aDTSCVO = (DTSCVO) aDTSC.findObject(qc2, conn);
			int dt_id = aDTSCVO.getOwnerDTID();
			
			QueryCondition qc = new QueryCondition();
			qc.add("dt_id", dt_id);
			DTVO aDTVO= (DTVO)aDT.findObject(qc, conn);
			
			cdtDTTerm = aDTVO.getDataTypeTerm();
			
			scPropertyTerm = aDTSCVO.getPropertyTerm();
			scRepresentationTerm = aDTSCVO.getRepresentationTerm();
			minCardinality = aDTSCVO.getMinCardinality();
			maxCardinality = aDTSCVO.getMaxCardinality();
		}
		
		public String getCDTData() {
			return this.cdtDTTerm + this.scPropertyTerm + this.scRepresentationTerm + this.minCardinality + this.maxCardinality;
		}
		
	}
	class CDTPrimitiveExpressionTypeMap {
		int id;
		String cdtDTTerm;
		String primitiveName;
		String xsdBuiltInType;
		
		public CDTPrimitiveExpressionTypeMap(int id) throws SRTDAOException {
			DAOFactory df;
			df = DAOFactory.getDAOFactory();
			SRTDAO aCDTAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
			QueryCondition qc = new QueryCondition();
			qc.add("cdt_awd_pri_xps_type_map_id", id);
			CDTAllowedPrimitiveExpressionTypeMapVO cdtAllowedPrimitiveExpressionTypeMapVO = (CDTAllowedPrimitiveExpressionTypeMapVO) aCDTAllowedPrimitiveExpressionTypeMap.findObject(qc, conn);
			int awdPriId = cdtAllowedPrimitiveExpressionTypeMapVO.getCDTAllowedPrimitiveID();
			int xbtId = cdtAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID();
			
			SRTDAO aCDTAllowedPrimitive = df.getDAO("CDTAllowedPrimitive");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("cdt_awd_pri_id", awdPriId);
			CDTAllowedPrimitiveVO cdtAllowedPrimitiveVO = (CDTAllowedPrimitiveVO)aCDTAllowedPrimitive.findObject(qc2, conn);
			
			SRTDAO aCDTPrimitive = df.getDAO("CDTPrimitive");
			QueryCondition qc3 = new QueryCondition();
			qc3.add("cdt_pri_id", cdtAllowedPrimitiveVO.getCDTPrimitiveID());
			CDTPrimitiveVO cdtPrimitiveVO = (CDTPrimitiveVO) aCDTPrimitive.findObject(qc3, conn);
			primitiveName = cdtPrimitiveVO.getName();
			
			SRTDAO aDT = df.getDAO("DT");
			QueryCondition qc4 = new QueryCondition();
			qc4.add("dt_id", cdtAllowedPrimitiveVO.getCDTID());
			DTVO cdtVO = (DTVO) aDT.findObject(qc4, conn);
			cdtDTTerm = cdtVO.getDataTypeTerm();
			
			SRTDAO aXbt = df.getDAO("XSDBuiltInType");
			QueryCondition qc5 = new QueryCondition();
			qc5.add("xbt_id", xbtId);
			XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO) aXbt.findObject(qc5, conn);
			xsdBuiltInType = xsdBuiltInTypeVO.getBuiltInType();
			
		}
		
		public String getXpsMapData() {
			return this.cdtDTTerm + this.primitiveName + this.xsdBuiltInType;
		}
	}
	
	class CDTSCExpressionTypeMap {
		int id;
		String cdtDTTerm;
		String primitiveName;
		String xsdBuiltInType;
		String scPropertyTerm;
		String scRepresentationTerm;
		
		public CDTSCExpressionTypeMap(int id) throws SRTDAOException {
			DAOFactory df;
			df = DAOFactory.getDAOFactory();
			SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMap = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
			QueryCondition qc = new QueryCondition();
			qc.add("cdt_sc_awd_pri_xps_type_map_id", id);
			CDTSCAllowedPrimitiveExpressionTypeMapVO cdtscAllowedPrimitiveExpressionTypeMapVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO) aCDTSCAllowedPrimitiveExpressionTypeMap.findObject(qc, conn);
			int cdtScAwdPriId = cdtscAllowedPrimitiveExpressionTypeMapVO.getCDTSCAllowedPrimitive();
			int xbtId = cdtscAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID();
			
			SRTDAO aCDTSCAllowedPrimitive = df.getDAO("CDTSCAllowedPrimitive");
			QueryCondition qc2 = new QueryCondition();
			qc2.add("cdt_sc_awd_pri_id", cdtScAwdPriId);
			CDTSCAllowedPrimitiveVO cdtscAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO)aCDTSCAllowedPrimitive.findObject(qc2, conn);
			
			SRTDAO aCDTPrimitive = df.getDAO("CDTPrimitive");
			QueryCondition qc3 = new QueryCondition();
			qc3.add("cdt_pri_id", cdtscAllowedPrimitiveVO.getCDTPrimitiveID());
			CDTPrimitiveVO cdtPrimitiveVO = (CDTPrimitiveVO) aCDTPrimitive.findObject(qc3, conn);
			primitiveName = cdtPrimitiveVO.getName();
			
			SRTDAO aDTSC = df.getDAO("DTSC");
			QueryCondition qc4 = new QueryCondition();
			qc4.add("dt_sc_id", cdtscAllowedPrimitiveVO.getCDTSCID());
			DTSCVO cdtscVO = (DTSCVO) aDTSC.findObject(qc4, conn);
			scPropertyTerm = cdtscVO.getPropertyTerm();
			scRepresentationTerm = cdtscVO.getRepresentationTerm();
			
			SRTDAO aDT = df.getDAO("DT");
			QueryCondition qc5 = new QueryCondition();
			qc5.add("dt_id", cdtscVO.getOwnerDTID());
			DTVO cdtVO = (DTVO) aDT.findObject(qc5, conn);
			cdtDTTerm = cdtVO.getDataTypeTerm();
			
			SRTDAO aXbt = df.getDAO("XSDBuiltInType");
			QueryCondition qc6 = new QueryCondition();
			qc6.add("xbt_id", xbtId);
			XSDBuiltInTypeVO xsdBuiltInTypeVO = (XSDBuiltInTypeVO) aXbt.findObject(qc6, conn);
			xsdBuiltInType = xsdBuiltInTypeVO.getBuiltInType();
			
		}

		public String getXpsMapData() {
			return this.cdtDTTerm + this.scPropertyTerm + this.scRepresentationTerm + this.primitiveName + this.xsdBuiltInType;
		}
		
	}
	
	private static Connection conn = null;
	
	public void run() throws Exception {
		System.out.println("### DataType Validation Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		//check_number_of_CDT();
		//check_number_of_XBT();
		//validate_cdt_awd_pri_xps_type_map();
		//validate_cdt_sc();
		//validate_cdt_sc_awd_pri_xps_type_map();
		validate_agency_id_list();
//		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
//		XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
//		XPathHandler components_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);
//			
//		for (int i = 0; i < Types.dataTypeList.length; i++){
//			validateImportDataTypeList(Types.dataTypeList[i]);
//		}
//		
//		for (int i = 0; i < Types.simpleTypeList.length; i++){
//			validateImportDataTypeList(Types.simpleTypeList[i]);
//		}
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
//		validateImportQBDT(fields_xsd);
//		tx.close();
//		conn.close();
		System.out.println("### DataType Validation End");
	}
	
	public static void main(String[] args) throws Exception {
		Utility.dbSetup();
		DataTypeTest p = new DataTypeTest();
		p.run();
	}
}
