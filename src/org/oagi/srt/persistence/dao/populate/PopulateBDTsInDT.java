package org.oagi.srt.persistence.dao.populate;

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
* @version 1.0
*
*What's left?
* There are additional default BDTs and unqualified BDTs that needs to be imported. These are listed in the exceptions section
*/

public class PopulateBDTsInDT {
	
	private static boolean printSQL = true;
	private static boolean insertSQL = false;

	
	private Document xmlDocument;
	private DocumentBuilder builder;
	private XPath xPath;
	
	public static void insertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition, String ccDefinition) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", dataTypeTerm);
		qc.add("DT_Type", new Integer(0));
		int basedDTID = ((DTVO)dao.findObject(qc)).getDTID();

		DTVO dtVO = new DTVO();
		
		dtVO.setDTGUID("oagis-id-" + UUID.randomUUID().toString().replaceAll("-", ""));
		dtVO.setDTType(1);
		dtVO.setVersionNumber("1.0");
		dtVO.setRevisionType(0);
		dtVO.setDataTypeTerm(dataTypeTerm);
		dtVO.setBasedDTID(basedDTID);
		dtVO.setDEN(typeName + ". Type");
		dtVO.setContentComponentDEN(typeName + ". Amount");
		dtVO.setDefinition(definition);
		dtVO.setContentComponentDefinition(ccDefinition);
		dtVO.setRevisionState(1);
		dtVO.setCreatedByUserId(1);
		dtVO.setLastUpdatedByUserId(1);

		if (insertSQL) dao.insertObject(dtVO);
	}
	
	public static void insertUnqualified_BDTStatement(String typeName, String dataTypeTerm) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", dataTypeTerm);
		qc.add("DT_Type", new Integer(1));
		int basedDTID = ((DTVO)dao.findObject(qc)).getDTID();
		
		DTVO dtVO = new DTVO();
		
		dtVO.setDTGUID("oagis-id-" + UUID.randomUUID().toString().replaceAll("-", ""));
		dtVO.setDTType(1);
		dtVO.setVersionNumber("1.0");
		dtVO.setRevisionType(0);
		dtVO.setDataTypeTerm(dataTypeTerm);
		dtVO.setBasedDTID(basedDTID);
		dtVO.setDEN(typeName + ". Type");
		dtVO.setContentComponentDEN(typeName + ". Amount");
		dtVO.setRevisionState(1);
		dtVO.setCreatedByUserId(1);
		dtVO.setLastUpdatedByUserId(1);
		
		if (insertSQL) dao.insertObject(dtVO);
	}
	
	public PopulateBDTsInDT(String filePath) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		
		builder = builderFactory.newDocumentBuilder();
		xmlDocument = builder.parse(new FileInputStream(filePath));
		xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new OAGiNamespaceContext());
	}
	
	private static void setup() throws SRTInitializerException {
		ServerProperties props = ServerProperties.getInstance();
		String _propFile = "/" + SRTConstants.SRT_PROPERTIES_FILE_NAME;
		try {
			InputStream is = SRTInitializer.class.getResourceAsStream(_propFile);
			if (is == null) {
				throw new SRTInitializerException(_propFile + " not found!");
			}
			try {
				props.load(is, true);
			} catch (IOException e) {
				throw new SRTInitializerException(_propFile + " cannot be read...");
			}
		} catch (Exception e) {
			System.out.println("[SRTInitializer] Fail to Getting "
					+ SRTConstants.SRT_PROPERTIES_FILE_NAME + " URL : "
					+ e.toString());
		}
		try {
			ConnectionPoolManager cpm = ConnectionPoolManager.getInstance();
			String poolName = cpm.getDefaultPoolName();
			System.out.println("DefaultPoolName:" + poolName);
			Connection dbConnection = cpm.getConnection(poolName);
			dbConnection.close();
			System.out.println("DB Connection Pool initialized...");
			cpm.release();
		} catch (Exception e) {
			System.out.println("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString());
			e.printStackTrace();
			throw new SRTInitializerException("[SRTInitializer] Fail to Creating Connection Pool : "
					+ e.toString());
		}
	}
	
	public NodeList getNodeList(String xPathExpression) throws XPathExpressionException {
		return (NodeList)xPath.compile(xPathExpression).evaluate(xmlDocument, XPathConstants.NODESET);
	}
	
	public Node getNode(String xPathExpression) throws XPathExpressionException {
		return (Node)xPath.compile(xPathExpression).evaluate(xmlDocument, XPathConstants.NODE);
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, SRTInitializerException, XPathExpressionException, SRTDAOException{
		setup();
		PopulateBDTsInDT fields_xsd = new PopulateBDTsInDT("C:/Users/nfs/Documents/Fields.xsd");
		PopulateBDTsInDT businessDataType_xsd = new PopulateBDTsInDT("C:/Users/nfs/Documents/BusinessDataType_1.xsd");
		
		for (int i = 0; i < Types.typeList.size(); i++){
			String typeName;
			String dataTypeTerm;
			String definition;
			String ccDefinition;
			
			String type = "complex";
			
			//Type Name
			Node typeNameNode = fields_xsd.getNode("//xsd:complexType[@name = '" + Types.typeList.get(i) + "']/xsd:simpleContent/xsd:extension");
			if (typeNameNode == null){
				type = "simple";
				typeNameNode = fields_xsd.getNode("//xsd:simpleType[@name = '" + Types.typeList.get(i) + "']/xsd:restriction");
			}
			Element typeNameElement = (Element)typeNameNode;
			typeName = typeNameElement.getAttribute("base");			
			
			//Data Type Term
			Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:DictionaryEntryName");
			Element dataTypeTermElement = (Element)dataTypeTermNode;
			dataTypeTerm = dataTypeTermElement.getTextContent();
			if (dataTypeTerm.length() > 5) if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
				dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.length() - 6);
			dataTypeTerm = dataTypeTerm.replaceAll(" Object", "");
							
			//Definitions
			Node definitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:Definition");
			Element definitionElement = (Element)definitionNode;
			definition = definitionElement.getTextContent();
			Node ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:annotation/xsd:documentation/ccts:ContentComponentValueDomain/ccts:Definition");
			if (ccDefinitionNode == null)
				ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:restriction/xsd:annotation/xsd:documentation/ccts:ContentComponentValueDomain/ccts:Definition");
			if (ccDefinitionNode == null)
				ccDefinitionNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:union/xsd:annotation/xsd:documentation/ccts:ContentComponentValueDomain/ccts:Definition");
			Element ccDefinitionElement = (Element)ccDefinitionNode;				
			ccDefinition = ccDefinitionElement.getTextContent();
			
			
			typeName = typeName.replaceAll("Type", "");
			insertDefault_BDTStatement(typeName, dataTypeTerm, definition, ccDefinition);

			if (printSQL){
				//Default BDT
				String insertDefault_BDTStatement = "INSERT INTO dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, "
				+ "Based_DT_ID, DEN, Content_Component_DEN, Definition, Content_Component_Definition, Revision_State, "
				+ "Created_By_User_ID, Last_Updated_By_User_ID, Creation_Timestamp, Last_Update_Timestamp) VALUES "
				+ "(CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 1, \"10.0\", 0, \"" + dataTypeTerm 
				+ "\", (SELECT DT_ID from dt WHERE Data_Type_Term = \"" + dataTypeTerm 	+ "\" AND DT_Type = 0),"
				+ " CONCAT(\"" + typeName + "\", \". Type\"), CONCAT(\"" + typeName + "\", \". Content\"), \"" 
				+ definition + "\", \"" + ccDefinition + "\", 1, \"oagis\", \"oagis\", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);\n";
	
				System.out.println("-- " + typeNameElement.getAttribute("base"));
				System.out.println(insertDefault_BDTStatement);
			}
			
			//Unqualified Type Name
			String unQualifiedTypeName = typeName.substring(0, typeName.length()-7);

			//Unqualified Data Type Term
			String unQualifiedDataTypeTerm = dataTypeTerm;
			
			insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm);
			
			if (printSQL){
				 //Unqualified BDT
				String insertUnqualified_BDTStatement = "INSERT INTO dt (DT_GUID, DT_Type, Version_Number, Revision_Type, Data_Type_Term, "
				+ "Based_DT_ID, DEN, Content_Component_DEN, Revision_State, Created_By_User_ID, Last_Updated_By_User_ID,"
				+ " Creation_Timestamp, Last_Update_Timestamp) VALUES (CONCAT('oagis-id-', REPLACE(UUID(),'-','')), 1, "
				+ "\"10.0\", 0, \"" + dataTypeTerm + "\", (SELECT DT_ID FROM dt WHERE Data_Type_Term = \"" + dataTypeTerm 
				+ "\" AND DT_Type = 1), CONCAT(\"" + unQualifiedTypeName + "\", \". Type\"), CONCAT(\"" + unQualifiedTypeName 
				+ "\", \". Content\"), 1, \"oagis\", \"oagis\", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);\n";
				
				System.out.println("-- " + unQualifiedTypeName);
				System.out.println(insertUnqualified_BDTStatement);
			}
		}


		
	}
	
}
