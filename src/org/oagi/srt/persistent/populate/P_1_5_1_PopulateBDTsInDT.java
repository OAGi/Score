package org.oagi.srt.persistent.populate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.chanchan.common.persistence.db.ConnectionPoolManager;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.OAGiNamespaceContext;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.startup.SRTInitializer;
import org.oagi.srt.startup.SRTInitializerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.UUID;

/**
*
* @author Nasif Sikder
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_5_1_PopulateBDTsInDT {
	
	private XPathHandler fields_xsd;
	private XPathHandler businessDataType_xsd;
	
	public P_1_5_1_PopulateBDTsInDT() throws Exception {
		fields_xsd = new XPathHandler("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/Common/Components/Fields_modified.xsd");
		businessDataType_xsd = new XPathHandler("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/Common/DataTypes/BusinessDataType_1_modified.xsd");
	}
	
	public static void insertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition, String ccDefinition, String id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", dataTypeTerm);
		qc.add("DT_Type", new Integer(0));
		int basedDTID = ((DTVO)dao.findObject(qc)).getDTID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("DT_GUID", id);
		System.out.println("### id: " + id);
		System.out.println("### basedDTID: " + basedDTID);
		System.out.println("### dataTypeTerm: " + dataTypeTerm);
		
		if(dao.findObject(qc1) == null) {
	
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(id);
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			dtVO.setRevisionType(0);
			dtVO.setDataTypeTerm(dataTypeTerm);
			dtVO.setBasedDTID(basedDTID);
			dtVO.setDEN(typeName + ". Type");
			dtVO.setContentComponentDEN(typeName + ". Content");
			dtVO.setDefinition(definition);
			dtVO.setContentComponentDefinition(ccDefinition);
			dtVO.setRevisionState(1);
			dtVO.setCreatedByUserId(1);
			dtVO.setLastUpdatedByUserId(1);
	
			dao.insertObject(dtVO);
		}
	}
	
	public static void insertUnqualified_BDTStatement(String typeName, String dataTypeTerm, String id, String defaultGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("DT_GUID", defaultGUID);
		int basedDTID = ((DTVO)dao.findObject(qc)).getDTID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("DT_GUID", id);
		
		if(dao.findObject(qc1) == null) {
				
			DTVO dtVO = new DTVO();
			
			dtVO.setDTGUID(id);
			dtVO.setDTType(1);
			dtVO.setVersionNumber("1.0");
			dtVO.setRevisionType(0);
			dtVO.setDataTypeTerm(dataTypeTerm);
			dtVO.setBasedDTID(basedDTID);
			dtVO.setDEN(typeName + ". Type");
			dtVO.setContentComponentDEN(typeName + ". Content");
			dtVO.setRevisionState(1);
			dtVO.setCreatedByUserId(1);
			dtVO.setLastUpdatedByUserId(1);
			dao.insertObject(dtVO);
		}
		
	}
	
	private void importDataTypeList(String dataType) throws Exception {
		String typeName;
		String dataTypeTerm;
		
		String type = "complex";
		
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
		
		System.out.println("### " + dataType + " - " + typeName);
		
		//Data Type Term
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:DictionaryEntryName");
		if(dataTypeTermNode == null && type.equals("simple")) {
			type = "complex";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:DictionaryEntryName");
		} else if(dataTypeTermNode == null && type.equals("complex")) {
			type = "simple";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:DictionaryEntryName");
		}
		
		System.out.println("### " + type + " - " + dataTypeTermNode);
			
		Element dataTypeTermElement = (Element)dataTypeTermNode;
		dataTypeTerm = dataTypeTermElement.getTextContent();
		if (dataTypeTerm.length() > 5) if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
			dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
		//dataTypeTerm = dataTypeTerm.replaceAll(" Object", "");
						
		//Definitions
		Node definitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:Definition");
		Element definitionElement = (Element)definitionNode;
		Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation/ccts:ContentComponentValueDomain/ccts:Definition");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation/ccts:ContentComponentValueDomain/ccts:Definition");
		if (ccDefinitionNode == null)
			ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation/ccts:ContentComponentValueDomain/ccts:Definition");
		Element ccDefinitionElement = (Element)ccDefinitionNode;				
		
		Node aNodeBDT = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']");
		Element aElementBDT = (Element)aNodeBDT;
		
		typeName = typeName.replaceAll("Type", "");
		insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		
		insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
			
	}
	
	public static void main(String[] args) throws Exception{
		Utility.dbSetup();
		P_1_5_1_PopulateBDTsInDT p = new P_1_5_1_PopulateBDTsInDT();
		for (int i = 0; i < Types.dataTypeList.length; i++){
			p.importDataTypeList(Types.dataTypeList[i]);
		}
		for (int i = 0; i < Types.simpleTypeList.length; i++){
			p.importDataTypeList(Types.simpleTypeList[i]);
		}
	}
}
