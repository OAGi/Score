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
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
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
			System.out.println("Inserting default bdt whose name is "+typeName);
	
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(id);
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			//dtVO.setRevisionType(0);
			dtVO.setDataTypeTerm(dataTypeTerm);
			dtVO.setBasedDTID(basedDTID);
			dtVO.setDEN(Utility.typeToDen(typeName));
			dtVO.setContentComponentDEN(Utility.typeToContent(typeName));
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
			dtVO.setIs_deprecated(false);
	
			dao.insertObject(dtVO);
		}
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("guid", id);
		return (DTVO)dao.findObject(qc2, conn);
	}
	
	public boolean check_BDT(String id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		QueryCondition qc1 = new QueryCondition();
		qc1.add("guid", id);
		if(dao.findObject(qc1, conn) == null)
			return false;
		else
			return true;
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
			System.out.println(typeName);
			String den = Utility.typeToDen(tmp.getAttribute("type"));
			
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
					
					if (defaultId == -1 || defaultId == 0) System.out.println("Error getting the default BDT primitive restriction for the default BDT: " + typeName);
					
					//typeName = typeName.replaceAll("Type", "");
					
					typeName = tmp.getAttribute("type"); //here
						
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
			dtVO.setDEN(Utility.typeToDen(typeName));
			dtVO.setContentComponentDEN(Utility.typeToContent(typeName));
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
			dtVO.setIs_deprecated(false);
			dao.insertObject(dtVO);
		}
		
		QueryCondition qc2 = new QueryCondition();
		qc2.add("guid", id);
		return (DTVO)dao.findObject(qc2, conn);
	}
	
	private void importDataTypeList(String dataType) throws Exception {
		System.out.println("Importing "+dataType+" now");
		String typeName;
		String xsdTypeName;
		String dataTypeTerm="";
		
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
		System.out.println("!! typeName = "+typeName);
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
		System.out.println("### "+dataTypeTermElement.getTextContent());
		try {
			dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
			if (dataTypeTerm == "") System.out.println("Error getting the data type term for the unqualified BDT: " + dataType);
		} catch (Exception e){
			System.out.println("Error getting the data type term for the unqualified BDT: " + dataType + " Stacktrace:" + e.getMessage());
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
		
		String definition = "";
		String ccDefinition = "";
		
		definition = definitionElement.getTextContent();
		
		if(ccDefinitionElement!=null){
			ccDefinition = ccDefinitionElement.getTextContent();
		}
		else {
			ccDefinition = null;
		}
		
		//This is the default BDT type definition node
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
				qc3.add("builtIn_type", xsdTypeName);
				defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc3, conn)).getXSDBuiltInTypeID();
				if(defaultId < 1) {
					Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
					QueryCondition qc4 = new QueryCondition();
					System.out.println(xsdTypeName);
					qc4.add("builtIn_type", ((Element)xbtNode).getAttribute("base"));
					defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc4, conn)).getXSDBuiltInTypeID();
				}
			} 
		}
		
		if (defaultId == -1 || defaultId == 0) System.out.println("Error getting the default BDT primitive restriction for the default BDT: " + typeName);
		System.out.println("data type term = "+dataTypeTerm);
		DTVO dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definition, ccDefinition, aElementBDT.getAttribute("id"));
		System.out.println("Inserting bdt primitive restriction for default bdt");
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

		//Unqualified Type Name
		String unQualifiedTypeName = dataType;

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
		System.out.println("Inserting bdt primitive restriction for unqualfieid bdt");
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	
	
	private void validateImportDataTypeList(String dataType) throws Exception {
		//System.out.println("Validating Import "+dataType+" now");
		String typeName;
		String xsdTypeName;
		String dataTypeTerm="";
		
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
		try {
			dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
			if (dataTypeTerm == "") System.out.println("Error getting the data type term for the unqualified BDT: " + dataType);
		} catch (Exception e){
			System.out.println("Error getting the data type term for the unqualified BDT: " + dataType + " Stacktrace:" + e.getMessage());
		}
		
		Node aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
		Element aElementBDT = (Element)aNodeBDT;
		
		defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("id");//guid
		defaultFromXSD = defaultFromXSD + "1";//type
		defaultFromXSD = defaultFromXSD + "1.0";//versionNum
		defaultFromXSD = defaultFromXSD + "0";//PreviousVersionDT_ID 0 means null
		defaultFromXSD = defaultFromXSD + dataTypeTerm;//data type term
		defaultFromXSD = defaultFromXSD + "null";//qualifier
		String baseCDTDen="";
		baseCDTDen = Utility.denWithoutUUID(typeName);
		baseCDTDen = baseCDTDen.replace(". Type", "");
		
		defaultFromXSD = defaultFromXSD + baseCDTDen;//base cdt den instead of base dt id
		defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("name");//type name instead of den
		
		Node definitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
		Element definitionElement = (Element)definitionNode;
		Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		Element ccDefinitionElement = (Element)ccDefinitionNode;				
		
		String definition = "";
		if(definitionNode!=null){
			definition = definitionNode.getTextContent();
		}
		if(definition.equals("")){
			definition = "null";
		}
		
		String ccdefinition="";
		if(ccDefinitionNode!=null){
			ccdefinition = ccDefinitionNode.getTextContent();
		}
		if(ccdefinition.equals("")){
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
		
		Node union = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union");
		boolean unionExist = false;
		if(union!=null){
			unionExist =true;
		}
		
		QueryCondition qc4defaultbdt = new QueryCondition();
		qc4defaultbdt.add("guid", aElementBDT.getAttribute("id"));
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		
		DTVO dtvoFromDB = (DTVO) dao.findObject(qc4defaultbdt);
		
		if(dtvoFromDB==null){
			System.out.println("@@@@ Default BDT is not imported! Check: "+aElementBDT.getAttribute("id"));
		}
		else {
			defaultFromDB = "";
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDTGUID()  ;//guid
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDTType() ;//type
			defaultFromDB = defaultFromDB+  dtvoFromDB.getVersionNumber()  ;//versionNum
			defaultFromDB = defaultFromDB+  dtvoFromDB.getPreviousVersionDTID()  ;//PreviousVersionDT_ID
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDataTypeTerm()  ;//data type term
			defaultFromDB = defaultFromDB+  dtvoFromDB.getQualifier()  ;//qualifier
			
			QueryCondition qc4basedt = new QueryCondition();
			qc4basedt.add("dt_id", dtvoFromDB.getBasedDTID());
			DTVO baseDTVO = (DTVO) dao.findObject(qc4basedt);
			
			defaultFromDB = defaultFromDB+  Utility.denToTypeName(baseDTVO.getDEN())  ;//base cdt den instead of base dt id
			defaultFromDB = defaultFromDB+  Utility.denToTypeName(dtvoFromDB.getDEN())  ;//type name instead of den
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDefinition()  ;//definition
			defaultFromDB = defaultFromDB+  dtvoFromDB.getContentComponentDefinition()  ;//content component definition
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionDocumentation()  ;//revision_doc	
			defaultFromDB = defaultFromDB+  dtvoFromDB.getState()  ;//state
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionNum()  ;//revisionNum
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionTrackingNum()  ;//revisionTrackingNum
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionAction()  ;//revisionAction
			defaultFromDB = defaultFromDB+  dtvoFromDB.getReleaseId()  ;//releaseID
			defaultFromDB = defaultFromDB+  dtvoFromDB.getCurrentBdtId()  ;//currentBDTID
			defaultFromDB = defaultFromDB+  dtvoFromDB.getIs_deprecated()  ;//is_deprecated	
		}
		
		if(!defaultFromDB.equals(defaultFromXSD)){
			System.out.println("@@@@ Default BDT has different values! Check: "+dtvoFromDB.getDTGUID());
			System.out.println("     FromXSD: "+defaultFromXSD);
			System.out.println("      FromDB: "+defaultFromDB);
		}
		else {
			System.out.println("# # # Default BDT "+typeName+ " is Valid");
			if(union!=null){
				
			}
			
			validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDTID(), dtvoFromDB.getDTID(), unionExist);
		}		
		
		
		
		unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("id");//guid
		unqualifiedFromXSD = unqualifiedFromXSD + "1";//type
		unqualifiedFromXSD = unqualifiedFromXSD + "1.0";//versionNum
		unqualifiedFromXSD = unqualifiedFromXSD + "0";//PreviousVersionDT_ID 0 means null
		unqualifiedFromXSD = unqualifiedFromXSD + dataTypeTerm;//data type term
		unqualifiedFromXSD = unqualifiedFromXSD + "null";//qualifier
		String baseDTName="";
		if(type.equals("complex")){
			Node aBaseNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension/@base");
			baseDTName = aBaseNode.getTextContent();
		}
		else {
			Node aBaseNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction/@base");
			baseDTName = aBaseNode.getTextContent();
		}
		unqualifiedFromXSD = unqualifiedFromXSD + baseDTName;//base dt den instead of base dt id
		unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("name");//type name instead of den		
		
		String unqualifiedDefinition = "null";

		String unqualifiedCCDefinition="null";

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

		
		QueryCondition qc4dt = new QueryCondition();
		qc4dt.add("guid", aElementTN.getAttribute("id"));
		
		dtvoFromDB = (DTVO) dao.findObject(qc4dt);
		if(dtvoFromDB==null){
			System.out.println("@@@@ Unqaulified BDT is not imported! Check: "+aElementTN.getAttribute("id"));
		}
		else {
			unqualifiedFromDB = "";
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDTGUID()  ;//guid
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDTType() ;//type
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getVersionNumber()  ;//versionNum
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getPreviousVersionDTID()  ;//PreviousVersionDT_ID
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDataTypeTerm()  ;//data type term
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getQualifier()  ;//qualifier
			
			QueryCondition qc4basedt = new QueryCondition();
			qc4basedt.add("dt_id", dtvoFromDB.getBasedDTID());
			DTVO baseDTVO = (DTVO) dao.findObject(qc4basedt);
			
			unqualifiedFromDB = unqualifiedFromDB+  Utility.denToTypeName(baseDTVO.getDEN())  ;//base dt name instead of base dt id
			unqualifiedFromDB = unqualifiedFromDB+  Utility.denToTypeName(dtvoFromDB.getDEN())  ;//type name instead of den
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDefinition()  ;//definition
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getContentComponentDefinition()  ;//content component definition
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionDocumentation()  ;//revision_doc	
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getState()  ;//state
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionNum()  ;//revisionNum
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionTrackingNum()  ;//revisionTrackingNum
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionAction()  ;//revisionAction
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getReleaseId()  ;//releaseID
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getCurrentBdtId()  ;//currentBDTID
			unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getIs_deprecated()  ;//is_deprecated	
		}
		
		if(!unqualifiedFromDB.equals(unqualifiedFromXSD)){
			System.out.println("@@@@ Unqualified BDT has different values! Check: "+dtvoFromDB.getDTGUID());
			System.out.println("     FromXSD: "+unqualifiedFromXSD);
			System.out.println("      FromDB: "+unqualifiedFromDB);
		}
		else {
			System.out.println("$ $ $ Unqaulified BDT "+dataType+" is Valid");
			validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDTID(), dtvoFromDB.getDTID(), false);
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
//			defaultId = ((XSDBuiltInTypeVO)dao3.findObject(qc3, conn)).getXSDBuiltInTypeID();
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
//				defaultId = ((XSDBuiltInTypeVO)dao3.findObject(qc3, conn)).getXSDBuiltInTypeID();
//				if(defaultId < 1) {
//					Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
//					QueryCondition qc4 = new QueryCondition();
//					System.out.println(xsdTypeName);
//					qc4.add("builtIn_type", ((Element)xbtNode).getAttribute("base"));
//					defaultId = ((XSDBuiltInTypeVO)dao3.findObject(qc4, conn)).getXSDBuiltInTypeID();
//				}
//			} 
//		}
//		
		//Let's start validating BDT_PRI_RESTRIs!
		//Get owner bdt, and 
		
		
		//if (defaultId == -1 || defaultId == 0) System.out.println("Error getting the default BDT primitive restriction for the default BDT: " + typeName);
		//System.out.println("data type term = "+dataTypeTerm);
		//DTVO dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
		//System.out.println("Inserting bdt primitive restriction for default bdt");
		//insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);

		//Unqualified Type Name
		//String unQualifiedTypeName = dataType;

		//Unqualified Data Type Term
		//String unQualifiedDataTypeTerm = dataTypeTerm;
		//DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
		//System.out.println("Inserting bdt primitive restriction for unqualfieid bdt");
		//insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private void importExceptionalDataTypeList2(String dataType) throws Exception {
		String typeName;
		String xsdTypeName;
		String dataTypeTerm="";

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
				qc3.add("builtIn_type", xsdTypeName);
				defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc3, conn)).getXSDBuiltInTypeID();
				if(defaultId == 0) {
					Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
					QueryCondition qc4 = new QueryCondition();
					qc4.add("builtIn_type", ((Element)xbtNode).getAttribute("base"));
					defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc4, conn)).getXSDBuiltInTypeID();
				}
			} 
		}
		
		DTVO dVO1 = new DTVO();
		
		if(check_BDT(aElementBDT.getAttribute("id"))){
			System.out.println("Default BDT is already existing");
			QueryCondition qc = new QueryCondition();
			qc.add("guid", aElementBDT.getAttribute("id"));
			SRTDAO dao2 = df.getDAO("DT");
			dVO1 = (DTVO)dao2.findObject(qc, conn);
		}
		else {
			dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
			System.out.println("Inserting bdt primitive restriction for exceptional default bdt");
			insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);
		}
		
		if(check_BDT(aElementBDT.getAttribute("id")))
			System.out.println("Unqualified BDT is already existing");
		else {
				//Unqualified Type Name
				String unQualifiedTypeName = dataType;
		
				//Unqualified Data Type Term
				String unQualifiedDataTypeTerm = dataTypeTerm;
				DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementBDT.getAttribute("id"), aElementBDT.getAttribute("id"));
				System.out.println("Inserting bdt primitive restriction for exceptional unqualified bdt");
				insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
		}
	}
	
	private void importExceptionalDataTypeList() throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO DTDao = df.getDAO("DT");
		SRTDAO dao = df.getDAO("XSDBuiltInType");
		String typeName;
		String xsdTypeName;
		String dataTypeTerm="";
		
		String type = "simple";
		
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

		//Type Name
		NodeList simpleTypeNodeList = fields_xsd.getNodeList("//xsd:simpleType");
		for(int k = 0; k < simpleTypeNodeList.getLength(); k++) {
			Node simpleTypeNode = simpleTypeNodeList.item(k);
			String dataType = ((Element)simpleTypeNode).getAttribute("name");
			if(dataType.endsWith("CodeContentType") || dataType.endsWith("IDContentType") )
				continue;
			
			boolean isIntheList = false;
			for(int i = 0 ; i < Types.dataTypeList.length ; i++){
				if(Types.dataTypeList[i].equals(dataType)){
					isIntheList = true;
					break;
				}
			}
			if(isIntheList)
				continue;
			
			Node typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
			System.out.println("Importing "+dataType+" now in the exception ");
			Element typeNameElement = (Element)typeNameNode;
			typeName = typeNameElement.getAttribute("base");	
			
			Node aNodeTN = fields_xsd.getNode("//xsd:"+type+"Type[@name = '" + dataType + "']");
			Element aElementTN = (Element)aNodeTN;
			
			//Data Type Term
			
			Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

			Element dataTypeTermElement = (Element)dataTypeTermNode;
			try {
				dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
				if (dataTypeTerm == "") System.out.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType);
			} catch (Exception e){
				System.out.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType + " Stacktrace:" + e.getMessage());
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
			
			Node aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
			Element aElementBDT = (Element)aNodeBDT;
			
			Node union = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union");
			int defaultId = -1;

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
			
			DTVO dVO1 = new DTVO();
			
			if(check_BDT(aElementBDT.getAttribute("id"))){
				System.out.println("Default BDT is already existing");
				QueryCondition qc = new QueryCondition();
				qc.add("guid", aElementBDT.getAttribute("id"));
				dVO1 = (DTVO)DTDao.findObject(qc, conn);
			}
			else {
				dVO1 = insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));
				System.out.println("Inserting bdt primitive restriction for exceptional default bdt");
				insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDTID(), dVO1.getDTID(), defaultId);
			}
			
			if(check_BDT(aElementTN.getAttribute("id")))
				System.out.println("Unqualified BDT is already existing");
			else {
					//Unqualified Type Name
					String unQualifiedTypeName = dataType;
			
					//Unqualified Data Type Term
					String unQualifiedDataTypeTerm = dataTypeTerm;
					DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
					System.out.println("Inserting bdt primitive restriction for exceptional unqualified bdt");
					insertBDTPrimitiveRestrictionForExceptionalBDT(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
			}
		}
	}
	
	
	
	
	private void validateImportExceptionalDataTypeList() throws Exception {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO DTDao = df.getDAO("DT");
		SRTDAO dao = df.getDAO("XSDBuiltInType");
		String typeName;
		String xsdTypeName;
		String dataTypeTerm="";
		
		String defaultFromXSD="";
		String defaultFromDB="";
		
		String unqualifiedFromXSD="";
		String unqualifiedFromDB="";
		
		String Value_039C44FromXSD="";
		String Value_039C44FromDB="";
		
		String type = "simple";
		
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);

		//Type Name
		NodeList simpleTypeNodeList = fields_xsd.getNodeList("//xsd:simpleType");
		for(int k = 0; k < simpleTypeNodeList.getLength(); k++) {
			Node simpleTypeNode = simpleTypeNodeList.item(k);
			String dataType = ((Element)simpleTypeNode).getAttribute("name");
			if(dataType.endsWith("CodeContentType") || dataType.endsWith("IDContentType") )
				continue;
			
			boolean isIntheList = false;
			for(int i = 0 ; i < Types.dataTypeList.length ; i++){
				if(Types.dataTypeList[i].equals(dataType)){
					isIntheList = true;
					break;
				}
			}
			if(isIntheList)
				continue;
			
			defaultFromXSD = "";
			defaultFromDB = "";
			
			unqualifiedFromXSD="";
			unqualifiedFromDB="";
			
			Node typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
			//System.out.println("Validating "+dataType+" now in the exception ");
			Element typeNameElement = (Element)typeNameNode;
			typeName = typeNameElement.getAttribute("base");	
			
			Node aNodeTN = fields_xsd.getNode("//xsd:"+type+"Type[@name = '" + dataType + "']");
			Element aElementTN = (Element)aNodeTN;
			
			//Data Type Term
			
			Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

			Element dataTypeTermElement = (Element)dataTypeTermNode;
			try {
				dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
				if (dataTypeTerm == "") System.out.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType);
			} catch (Exception e){
				System.out.println("Error getting the data type term for the unqualified BDT in the exception: " + dataType + " Stacktrace:" + e.getMessage());
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
			
			Node aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
			Element aElementBDT = (Element)aNodeBDT;
			
			
			defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("id");
			defaultFromXSD = defaultFromXSD + "1";//type
			defaultFromXSD = defaultFromXSD + "1.0";//versionNum
			defaultFromXSD = defaultFromXSD + "0";//PreviousVersionDT_ID 0 means null
			defaultFromXSD = defaultFromXSD + dataTypeTerm;//data type term
			defaultFromXSD = defaultFromXSD + "null";//qualifier
			String baseCDTDen="";
			baseCDTDen = Utility.denWithoutUUID(typeName);
			baseCDTDen = baseCDTDen.replace(". Type", "");
			
			defaultFromXSD = defaultFromXSD + baseCDTDen;//base cdt den instead of base dt id
			defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("name");//type name instead of den
			String definition = "";
			if(definitionNode!=null){
				definition = definitionNode.getTextContent();
			}
			if(definition.equals("")){
				definition = "null";
			}
			
			String ccdefinition="";
			if(ccDefinitionNode!=null){
				ccdefinition = ccDefinitionNode.getTextContent();
			}
			if(ccdefinition.equals("")){
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
			
			QueryCondition qc4defaultbdt = new QueryCondition();
			qc4defaultbdt.add("guid", aElementBDT.getAttribute("id"));
			df = DAOFactory.getDAOFactory();
			dao = df.getDAO("DT");
			
			DTVO dtvoFromDB = (DTVO) dao.findObject(qc4defaultbdt);
			
			if(dtvoFromDB==null){
				System.out.println("@@@@ Default BDT is not imported! Check: "+aElementBDT.getAttribute("id"));
			}
			else {
				defaultFromDB = "";
				defaultFromDB = defaultFromDB+  dtvoFromDB.getDTGUID()  ;//guid
				defaultFromDB = defaultFromDB+  dtvoFromDB.getDTType() ;//type
				defaultFromDB = defaultFromDB+  dtvoFromDB.getVersionNumber()  ;//versionNum
				defaultFromDB = defaultFromDB+  dtvoFromDB.getPreviousVersionDTID()  ;//PreviousVersionDT_ID
				defaultFromDB = defaultFromDB+  dtvoFromDB.getDataTypeTerm()  ;//data type term
				defaultFromDB = defaultFromDB+  dtvoFromDB.getQualifier()  ;//qualifier
				
				QueryCondition qc4basedt = new QueryCondition();
				qc4basedt.add("dt_id", dtvoFromDB.getBasedDTID());
				DTVO baseDTVO = (DTVO) dao.findObject(qc4basedt);
				
				defaultFromDB = defaultFromDB+  Utility.denToTypeName(baseDTVO.getDEN())  ;//base cdt den instead of base dt id
				defaultFromDB = defaultFromDB+  Utility.denToTypeName(dtvoFromDB.getDEN())  ;//type name instead of den
				defaultFromDB = defaultFromDB+  dtvoFromDB.getDefinition()  ;//definition
				defaultFromDB = defaultFromDB+  dtvoFromDB.getContentComponentDefinition()  ;//content component definition
				defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionDocumentation()  ;//revision_doc	
				defaultFromDB = defaultFromDB+  dtvoFromDB.getState()  ;//state
				defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionNum()  ;//revisionNum
				defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionTrackingNum()  ;//revisionTrackingNum
				defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionAction()  ;//revisionAction
				defaultFromDB = defaultFromDB+  dtvoFromDB.getReleaseId()  ;//releaseID
				defaultFromDB = defaultFromDB+  dtvoFromDB.getCurrentBdtId()  ;//currentBDTID
				defaultFromDB = defaultFromDB+  dtvoFromDB.getIs_deprecated()  ;//is_deprecated	
			}
			
			if(!defaultFromDB.equals(defaultFromXSD)){
				System.out.println("@@@@ Default BDT has different values! Check: "+dtvoFromDB.getDTGUID());
				System.out.println("     FromXSD: "+defaultFromXSD);
				System.out.println("      FromDB: "+defaultFromDB);
			}
			else {
				System.out.println("# # # Default BDT "+typeName+ " (Exceptional BDT) is Valid");
				
				validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDTID(), dtvoFromDB.getDTID(), false);
			}		
			
			unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("id");//guid
			unqualifiedFromXSD = unqualifiedFromXSD + "1";//type
			unqualifiedFromXSD = unqualifiedFromXSD + "1.0";//versionNum
			unqualifiedFromXSD = unqualifiedFromXSD + "0";//PreviousVersionDT_ID 0 means null
			unqualifiedFromXSD = unqualifiedFromXSD + dataTypeTerm;//data type term
			unqualifiedFromXSD = unqualifiedFromXSD + "null";//qualifier
			String baseDTName="";
			if(type.equals("complex")){
				Node aBaseNode = fields_xsd.getNode("//xsd:complexType[@name = '" + dataType + "']/xsd:simpleContent/xsd:extension/@base");
				baseDTName = aBaseNode.getTextContent();
			}
			else {
				Node aBaseNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction/@base");
				baseDTName = aBaseNode.getTextContent();
			}
			unqualifiedFromXSD = unqualifiedFromXSD + baseDTName;//base dt den instead of base dt id
			unqualifiedFromXSD = unqualifiedFromXSD + aElementTN.getAttribute("name");//type name instead of den		
			
			String unqualifiedDefinition = "null";

			String unqualifiedCCDefinition="null";

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

			
			QueryCondition qc4dt = new QueryCondition();
			qc4dt.add("guid", aElementTN.getAttribute("id"));
			
			dtvoFromDB = (DTVO) dao.findObject(qc4dt);
			if(dtvoFromDB==null){
				System.out.println("@@@@ Unqaulified BDT is not imported! Check: "+aElementTN.getAttribute("id"));
			}
			else {
				unqualifiedFromDB = "";
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDTGUID()  ;//guid
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDTType() ;//type
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getVersionNumber()  ;//versionNum
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getPreviousVersionDTID()  ;//PreviousVersionDT_ID
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDataTypeTerm()  ;//data type term
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getQualifier()  ;//qualifier
				
				QueryCondition qc4basedt = new QueryCondition();
				qc4basedt.add("dt_id", dtvoFromDB.getBasedDTID());
				DTVO baseDTVO = (DTVO) dao.findObject(qc4basedt);
				
				unqualifiedFromDB = unqualifiedFromDB+  Utility.denToTypeName(baseDTVO.getDEN())  ;//base dt name instead of base dt id
				unqualifiedFromDB = unqualifiedFromDB+  Utility.denToTypeName(dtvoFromDB.getDEN())  ;//type name instead of den
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getDefinition()  ;//definition
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getContentComponentDefinition()  ;//content component definition
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionDocumentation()  ;//revision_doc	
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getState()  ;//state
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionNum()  ;//revisionNum
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionTrackingNum()  ;//revisionTrackingNum
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getRevisionAction()  ;//revisionAction
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getReleaseId()  ;//releaseID
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getCurrentBdtId()  ;//currentBDTID
				unqualifiedFromDB = unqualifiedFromDB+  dtvoFromDB.getIs_deprecated()  ;//is_deprecated	
			}
			
			if(!unqualifiedFromDB.equals(unqualifiedFromXSD)){
				System.out.println("@@@@ Unqualified BDT has different values! Check: "+dtvoFromDB.getDTGUID());
				System.out.println("     FromXSD: "+unqualifiedFromXSD);
				System.out.println("      FromDB: "+unqualifiedFromDB);
			}
			else {
				System.out.println("$ $ $ Unqaulified BDT "+dataType+" (Exceptional BDT) is Valid");
				validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDTID(), dtvoFromDB.getDTID(), false);
			}
			
		}
		
		
		//Check ValueType_039C44

		defaultFromXSD ="";
		defaultFromDB ="";
		//Data Type Term
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");

		Element dataTypeTermElement = (Element)dataTypeTermNode;
		try {
			dataTypeTerm = dataTypeTermElement.getTextContent().substring(0, dataTypeTermElement.getTextContent().indexOf(". Type"));
			if (dataTypeTerm == "") System.out.println("Error getting the data type term for the unqualified BDT in the exception: ValueType_039C44" );
		} catch (Exception e){
			System.out.println("Error getting the data type term for the unqualified BDT in the exception: ValueType_039C44 Stacktrace:" + e.getMessage());
		}
		
		//Definitions
		Node definitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
		Element definitionElement = (Element)definitionNode;
		Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:restriction/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']/xsd:union/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
		Element ccDefinitionElement = (Element)ccDefinitionNode;				
		
		Node aNodeBDT = businessDataType_xsd.getNode("//xsd:simpleType[@name = 'ValueType_039C44']");
		Element aElementBDT = (Element)aNodeBDT;
		
		
		defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("id");
		defaultFromXSD = defaultFromXSD + "1";//type
		defaultFromXSD = defaultFromXSD + "1.0";//versionNum
		defaultFromXSD = defaultFromXSD + "0";//PreviousVersionDT_ID 0 means null
		defaultFromXSD = defaultFromXSD + dataTypeTerm;//data type term
		defaultFromXSD = defaultFromXSD + "null";//qualifier
		String baseCDTDen="";
		baseCDTDen = Utility.denWithoutUUID("ValueType_039C44");
		baseCDTDen = baseCDTDen.replace(". Type", "");
		
		defaultFromXSD = defaultFromXSD + baseCDTDen;//base cdt den instead of base dt id
		defaultFromXSD = defaultFromXSD + aElementBDT.getAttribute("name");//type name instead of den
		String definition = "";
		if(definitionNode!=null){
			definition = definitionNode.getTextContent();
		}
		if(definition.equals("")){
			definition = "null";
		}
		
		String ccdefinition="";
		if(ccDefinitionNode!=null){
			ccdefinition = ccDefinitionNode.getTextContent();
		}
		if(ccdefinition.equals("")){
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
		
		QueryCondition qc4defaultbdt = new QueryCondition();
		qc4defaultbdt.add("guid", aElementBDT.getAttribute("id"));
		df = DAOFactory.getDAOFactory();
		dao = df.getDAO("DT");
		
		DTVO dtvoFromDB = (DTVO) dao.findObject(qc4defaultbdt);
		
		if(dtvoFromDB==null){
			System.out.println("@@@@ Default BDT is not imported! Check: "+aElementBDT.getAttribute("id"));
		}
		else {
			defaultFromDB = "";
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDTGUID()  ;//guid
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDTType() ;//type
			defaultFromDB = defaultFromDB+  dtvoFromDB.getVersionNumber()  ;//versionNum
			defaultFromDB = defaultFromDB+  dtvoFromDB.getPreviousVersionDTID()  ;//PreviousVersionDT_ID
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDataTypeTerm()  ;//data type term
			defaultFromDB = defaultFromDB+  dtvoFromDB.getQualifier()  ;//qualifier
			
			QueryCondition qc4basedt = new QueryCondition();
			qc4basedt.add("dt_id", dtvoFromDB.getBasedDTID());
			DTVO baseDTVO = (DTVO) dao.findObject(qc4basedt);
			
			defaultFromDB = defaultFromDB+  Utility.denToTypeName(baseDTVO.getDEN())  ;//base cdt den instead of base dt id
			defaultFromDB = defaultFromDB+  Utility.denToTypeName(dtvoFromDB.getDEN())  ;//type name instead of den
			defaultFromDB = defaultFromDB+  dtvoFromDB.getDefinition()  ;//definition
			defaultFromDB = defaultFromDB+  dtvoFromDB.getContentComponentDefinition()  ;//content component definition
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionDocumentation()  ;//revision_doc	
			defaultFromDB = defaultFromDB+  dtvoFromDB.getState()  ;//state
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionNum()  ;//revisionNum
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionTrackingNum()  ;//revisionTrackingNum
			defaultFromDB = defaultFromDB+  dtvoFromDB.getRevisionAction()  ;//revisionAction
			defaultFromDB = defaultFromDB+  dtvoFromDB.getReleaseId()  ;//releaseID
			defaultFromDB = defaultFromDB+  dtvoFromDB.getCurrentBdtId()  ;//currentBDTID
			defaultFromDB = defaultFromDB+  dtvoFromDB.getIs_deprecated()  ;//is_deprecated	
		}
		
		if(!defaultFromDB.equals(defaultFromXSD)){
			System.out.println("@@@@ Default BDT ValueType_039C44 has different values! ");
			System.out.println("     FromXSD: "+defaultFromXSD);
			System.out.println("      FromDB: "+defaultFromDB);
		}
		else {
			System.out.println("# # # Default BDT ValueType_039C44 (Exceptional BDT) is Valid");
			
			validateInsertBDTPrimitiveRestriction(dtvoFromDB.getBasedDTID(), dtvoFromDB.getDTID(), false);
		}		
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
				boolean isDefault = false;
				if(defaultId == aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID())
					isDefault = true;
				aBDT_Primitive_RestrictionVO.setisDefault(isDefault);
				System.out.println("Inserting allowed primitive expression type map with XSD built-in type " +
						getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID()) + ": default = " + isDefault);				
				aBDTPrimitiveRestrictionDAO.insertObject(aBDT_Primitive_RestrictionVO);
			}
		}
	}
	
	private void validateInsertBDTPrimitiveRestriction(int cdtID, int bdtID, boolean unionExist) throws SRTDAOException {
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		
		QueryCondition qc4BDT = new QueryCondition();
		qc4BDT.add("BDT_ID", bdtID);
		ArrayList<SRTObject> thisBDTPris = aBDTPrimitiveRestrictionDAO.findObjects(qc4BDT);
		int defaultCnt = 0;
		
		for(int i=0; i<thisBDTPris.size(); i++){
			
			BDTPrimitiveRestrictionVO aBDTVO = (BDTPrimitiveRestrictionVO) thisBDTPris.get(i);
			
			QueryCondition qc4CDTAPXTM = new QueryCondition();
			qc4CDTAPXTM.add("CDT_awd_pri_xps_type_map_id", aBDTVO.getCDTPrimitiveExpressionTypeMapID());
			ArrayList<SRTObject> thisCDTAPXTMaps = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4CDTAPXTM);
			
			for(int j=0; j<thisCDTAPXTMaps.size(); j++){
				
				CDTAllowedPrimitiveExpressionTypeMapVO aCDTAPXTM = (CDTAllowedPrimitiveExpressionTypeMapVO) thisCDTAPXTMaps.get(j);
				//Now We get CDT_AWD_PRI and XBT
				QueryCondition qc4CDTAP = new QueryCondition();
				qc4CDTAP.add("CDT_awd_pri_id", aCDTAPXTM.getCDTAllowedPrimitiveID());
				CDTAllowedPrimitiveVO aCDTAP = (CDTAllowedPrimitiveVO) aCDTAllowedPrimitiveDAO.findObject(qc4CDTAP);
				QueryCondition qc4CDTPri = new QueryCondition();
				qc4CDTPri.add("CDT_Pri_id", aCDTAP.getCDTPrimitiveID());
				CDTPrimitiveVO aCDTP = (CDTPrimitiveVO) aCDTPrimitiveDAO.findObject(qc4CDTPri);
				
				QueryCondition qc4xbt = new QueryCondition();
				qc4xbt.add("xbt_id", aCDTAPXTM.getXSDBuiltInTypeID());
				XSDBuiltInTypeVO axbt = (XSDBuiltInTypeVO) aXSDBuiltInTypeDAO.findObject(qc4xbt);
				
				boolean correspond = Utility.checkCorrespondingTypes(axbt.getBuiltInType(), aCDTP.getName());
				
				if(!correspond){
					System.out.println("@@@@ Check XBT and CDT Pri! XBT and CDT Pri are not Corresponding!");
					System.out.println("         XBT: "+axbt.getBuiltInType());
					System.out.println("     CDT_Pri: "+aCDTP.getName());
				}
				
				if(aBDTVO.getisDefault()){
					defaultCnt ++;
				}
			}	
		}
		if(defaultCnt !=1 ){
			System.out.println("@@@@ Check BDT Pri! Default is invalid!");
		}
	}
	
	
	
	private void insertBDTPrimitiveRestrictionForExceptionalBDT(int cdtID, int bdtID, int defaultId) throws SRTDAOException {
		
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
				int idOfXsdToken = getXSDBuiltIntypeId("xsd:token");
				
				if(defaultId == aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID()) { // default
					BDTPrimitiveRestrictionVO aBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
					aBDT_Primitive_RestrictionVO.setBDTID(bdtID);
					aBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
					aBDT_Primitive_RestrictionVO.setisDefault(true);
					System.out.println("Inserting allowed primitive expression type map with XSD built-in type " +
							getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID()) + ": default = true");				
					aBDTPrimitiveRestrictionDAO.insertObject(aBDT_Primitive_RestrictionVO);
				}
				if (idOfXsdToken != defaultId && idOfXsdToken == aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID()) {
					BDTPrimitiveRestrictionVO aBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
					aBDT_Primitive_RestrictionVO.setBDTID(bdtID);
					aBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
					aBDT_Primitive_RestrictionVO.setisDefault(false);
					System.out.println("Inserting allowed primitive expression type map with XSD built-in type " +
							getXsdBuiltinType(aCDTAllowedPrimitiveExpressionTypeMapVO.getXSDBuiltInTypeID()) + ": default = false");			
					aBDTPrimitiveRestrictionDAO.insertObject(aBDT_Primitive_RestrictionVO);
					
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
		int defaultId=-1;
	
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
		
		
		Node aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']");
		Element aElementTN = (Element)aNodeTN;
		id = aElementTN.getAttribute("id");
		aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
		aElementTN = (Element)aNodeTN;
		typeName = aElementTN.getAttribute("base");
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		QueryCondition qc5 = new QueryCondition();
		String den = Utility.typeToDen(typeName);
		qc5.add("den", den);
		DTVO dVO1 = (DTVO) dao.findObject(qc5, conn);
		baseDataTypeTerm = dVO1.getDataTypeTerm();
		baseGUID = dVO1.getDTGUID();
		
		
		
		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = baseDataTypeTerm;
			
		dao = df.getDAO("XSDBuiltInType");
		
		Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']//xsd:restriction");
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
		
		DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, id, baseGUID);
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private void importIDContentType() throws Exception {

		String dataType = "IDContentType";
		
		String typeName;
		String xsdTypeName;
		String baseDataTypeTerm;
		String baseGUID;
		String id;
		int defaultId=-1;
	
		XPathHandler fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler xbt_xsd = new XPathHandler(SRTConstants.XBT_FILE_PATH);
		
		
		Node aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']");
		Element aElementTN = (Element)aNodeTN;
		id = aElementTN.getAttribute("id");
		aNodeTN = fields_xsd.getNode("//xsd:simpleType[@name = '" + dataType + "']/xsd:restriction");
		aElementTN = (Element)aNodeTN;
		typeName = aElementTN.getAttribute("base");
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		QueryCondition qc = new QueryCondition();
		String den = Utility.typeToDen(typeName);
		qc.add("den", den);
		DTVO dVO1 = (DTVO) dao.findObject(qc, conn);
		baseDataTypeTerm = dVO1.getDataTypeTerm();
		baseGUID = dVO1.getDTGUID();
		

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = baseDataTypeTerm;
			
		dao = df.getDAO("XSDBuiltInType");
		
		Node xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']//xsd:extension");
		if(xsdTypeNameNode == null)
			xsdTypeNameNode = businessDataType_xsd.getNode("//xsd:simpleType[@name = '" + typeName + "']//xsd:restriction");
		if(xsdTypeNameNode != null) {
			Element xsdTypeNameElement = (Element)xsdTypeNameNode;
			xsdTypeName = xsdTypeNameElement.getAttribute("base");	
			
			QueryCondition qc2 = new QueryCondition();
			qc2.add("builtin_type", xsdTypeName);
			defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc2, conn)).getXSDBuiltInTypeID();
			if(defaultId < 1) {
				Node xbtNode = xbt_xsd.getNode("//xsd:simpleType[@name = '" + xsdTypeName + "']/xsd:restriction");
				QueryCondition qc3 = new QueryCondition();
				System.out.println(xsdTypeName);
				qc3.add("builtin_type", ((Element)xbtNode).getAttribute("base"));
				defaultId = ((XSDBuiltInTypeVO)dao.findObject(qc3, conn)).getXSDBuiltInTypeID();
			}
		} 		
		DTVO dVO2 = insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, id, baseGUID);
		insertBDTPrimitiveRestriction(dVO1.getBasedDTID(), dVO2.getDTID(), defaultId);
	}
	
	private String getXsdBuiltinType(int id) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO daoXSD = df.getDAO("XSDBuiltInType");
		QueryCondition qc = new QueryCondition();
		qc.add("xbt_id", id);
		return ((XSDBuiltInTypeVO)(daoXSD.findObject(qc, conn))).getBuiltInType();		
	}
	
	private int getXSDBuiltIntypeId(String xsd_buitintype) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO daoXSD = df.getDAO("XSDBuiltInType");
		QueryCondition qc = new QueryCondition();
		qc.add("builtin_type", xsd_buitintype);
		return ((XSDBuiltInTypeVO)(daoXSD.findObject(qc, conn))).getXSDBuiltInTypeID();		
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
		
		importExceptionalDataTypeList();
		importExceptionalDataTypeList2("ValueType_039C44");
		
		tx.close();
		conn.close();
		System.out.println("### 1.5.1-2 End");
	}
	
	public void validate_P_1_5_1_to_2_PopulateBDTsInDT() throws Exception {
		System.out.println("### 1.5.1-2 Start Validation");
		
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
			validateImportDataTypeList(Types.dataTypeList[i]);
		}
		
		validateImportExceptionalDataTypeList();
		
		tx.close();
		conn.close();
		System.out.println("### 1.5.1-2 Validation End");
	}
	
	public static void main(String[] args) throws Exception {
		Utility.dbSetup();
		P_1_5_1_to_2_PopulateBDTsInDT p = new P_1_5_1_to_2_PopulateBDTsInDT();
		p.run();
	}
}
