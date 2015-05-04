package org.oagi.srt.persistent.populate;

import java.sql.Connection;

import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.AgencyIDListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
*
* @author Nasif Sikder
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_5_1_PopulateBDTsInDT {
	
	private static XPathHandler fields_xsd;
	private static XPathHandler businessDataType_xsd;
	private static XPathHandler meta_xsd;
	private static XPathHandler components_xsd;
	private static XPathHandler nouns_bod_xsd;
	private static XPathHandler nouns_field_xsd;
	private static XPathHandler nouns_table_xsd;
	private static XPathHandler nouns_uomgroup_xsd;

	
	public P_1_5_1_PopulateBDTsInDT() throws Exception {
		fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		components_xsd = new XPathHandler(SRTConstants.COMPONENTS_XSD_FILE_PATH);
		nouns_bod_xsd = new XPathHandler(SRTConstants.NOUNS_BOD_XSD_FILE_PATH);
		nouns_field_xsd = new XPathHandler(SRTConstants.NOUNS_FIELD_XSD_FILE_PATH);
		nouns_table_xsd = new XPathHandler(SRTConstants.NOUNS_TABLE_XSD_FILE_PATH);
		nouns_uomgroup_xsd = new XPathHandler(SRTConstants.NOUNS_UOMGROUP_XSD_FILE_PATH);
	}
	
	public static void insertDefault_BDTStatement(String typeName, String dataTypeTerm, String definition, String ccDefinition, String id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("Data_Type_Term", dataTypeTerm);
		qc.add("DT_Type", new Integer(0));
		int basedDTID = ((DTVO)dao.findObject(qc, conn)).getDTID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("DT_GUID", id);
		System.out.println("### id: " + id);
		System.out.println("### basedDTID: " + basedDTID);
		System.out.println("### dataTypeTerm: " + dataTypeTerm);
		
		if(dao.findObject(qc1, conn) == null) {
	
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
			dtVO.setRevisionDocumentation("");
	
			dao.insertObject(dtVO);
		}
	}
	
	public static void populateAdditionalDefault_BDTStatement(XPathHandler filename, String dataType) throws SRTDAOException, XPathExpressionException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		NodeList xsd_node = filename.getNodeList("//xsd:attribute");
		for(int i = 0; i < xsd_node.getLength(); i++) {
			Element tmp = (Element)xsd_node.item(i);
			String typeName = tmp.getAttribute("type").replaceAll("Type", "");
			String DEN = tmp.getAttribute("type").replaceAll("Type", "")+ ". Type";
			
			QueryCondition qc1 = new QueryCondition();
			qc1.add("DEN", DEN);
			//distinct-values(typeName);
			
			boolean duplicate_check = false;
			for(int j = 0; j < i; j++) {
				Element tmp2 = (Element)xsd_node.item(j);
				String tmp_typeName = tmp2.getAttribute("type").replaceAll("Type", "")+ ". Type";
				if(DEN.equals(tmp_typeName))
					duplicate_check = true;
			}
			
			if(dao.findObject(qc1, conn) == null && duplicate_check == false) {
				System.out.println("Should be Added!:   "+i+"###############"+DEN);
								
				//Data Type Term
				Node dataTypeTermNode = filename.getNode("//xsd:attribute[@type = '" + tmp.getAttribute("type") + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
				if(dataTypeTermNode == null)
					System.out.println("#####null;;");
				else {
					Element dataTypeTermElement = (Element)dataTypeTermNode;
					String dataTypeTerm = dataTypeTermElement.getTextContent();
					//if (dataTypeTerm.length() > 5) if (dataTypeTerm.substring(dataTypeTerm.length() - 6, dataTypeTerm.length()).equals(". Type"))
						dataTypeTerm = dataTypeTerm.substring(0, dataTypeTerm.indexOf("."));
					System.out.println("!!!!DatatypeTerm:   "+dataTypeTerm);
					//Definitions
					Node definitionNode = filename.getNode("//xsd:attribute[@type = '" + tmp.getAttribute("type") + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_Definition\"]");
					Element definitionElement = (Element)definitionNode;
					
					Node aNodeBDT = filename.getNode("//xsd:attribute[@type = '" + tmp.getAttribute("type")+ "']");
					Element aElementBDT = (Element)aNodeBDT;
					//System.out.println("@@@ typeName = " + typeName + "  dataTypeTerm == " + dataTypeTerm + "  definitionElement = " + definitionElement.getTextContent() + "  definitionElement = " + definitionElement.getTextContent() + "  id" + aElementBDT.getAttribute("id") + "...");
					insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), definitionElement.getTextContent(), aElementBDT.getAttribute("id"));
				}
			}
		}
	}
	
	public void insertUnqualified_BDTStatement(String typeName, String dataTypeTerm, String id, String defaultGUID) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");

		QueryCondition qc = new QueryCondition();
		qc.add("DT_GUID", defaultGUID);
		int basedDTID = ((DTVO)dao.findObject(qc, conn)).getDTID();
		
		QueryCondition qc1 = new QueryCondition();
		qc1.add("DT_GUID", id);
		
		if(dao.findObject(qc1, conn) == null) {
				
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
			dtVO.setRevisionDocumentation("");
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
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		if(dataTypeTermNode == null && type.equals("simple")) {
			type = "complex";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		} else if(dataTypeTermNode == null && type.equals("complex")) {
			type = "simple";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/*[local-name()=\"ccts_DictionaryEntryName\"]");
		}
		
		System.out.println("### " + type + " - " + dataTypeTermNode);
			
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
		
		typeName = typeName.replaceAll("Type", "");
		insertDefault_BDTStatement(typeName, dataTypeTerm, definitionElement.getTextContent(), (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null, aElementBDT.getAttribute("id"));

		//Unqualified Type Name
		String unQualifiedTypeName = dataType.replaceAll("Type", "");

		//Unqualified Data Type Term
		String unQualifiedDataTypeTerm = dataTypeTerm;
		
		insertUnqualified_BDTStatement(unQualifiedTypeName, unQualifiedDataTypeTerm, aElementTN.getAttribute("id"), aElementBDT.getAttribute("id"));
			
	}
	
	private static Connection conn = null;
	
	public static void main(String[] args) throws Exception {
		Utility.dbSetup();
		DBAgent tx = new DBAgent();
		conn = tx.open();
			
		P_1_5_1_PopulateBDTsInDT p = new P_1_5_1_PopulateBDTsInDT();
		for (int i = 0; i < Types.dataTypeList.length; i++){
			p.importDataTypeList(Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(fields_xsd, Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(businessDataType_xsd, Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(meta_xsd, Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(components_xsd, Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_bod_xsd, Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_field_xsd, Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_table_xsd, Types.dataTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_uomgroup_xsd, Types.dataTypeList[i]);

		}
		for (int i = 0; i < Types.simpleTypeList.length; i++){
			p.importDataTypeList(Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(fields_xsd, Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(businessDataType_xsd, Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(meta_xsd, Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(components_xsd, Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_bod_xsd, Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_field_xsd, Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_table_xsd, Types.simpleTypeList[i]);
			populateAdditionalDefault_BDTStatement(nouns_uomgroup_xsd, Types.simpleTypeList[i]);
		}
		
		tx.close();
		conn.close();
		System.out.println("END");
		
	}
}
