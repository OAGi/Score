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
import org.oagi.srt.persistence.dto.DTSCVO;
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
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_5_3_PopulateSCInDTSC {
	
	private XPathHandler fields_xsd;
	private XPathHandler businessDataType_xsd;
	
	public P_1_5_3_PopulateSCInDTSC() throws Exception {
		fields_xsd = new XPathHandler("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/Common/Components/Fields_modified.xsd");
		businessDataType_xsd = new XPathHandler("/Users/yslee/Work/Project/OAG/Development/OAGIS_10_EnterpriseEdition/OAGi-BPI-Platform/org_openapplications_oagis/10_0/Model/Platform/2_0/Common/DataTypes/BusinessDataType_1_modified.xsd");
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
		
		//System.out.println("### " + dataType + " - " + typeName);
		
		//Data Type Term
		Node dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:DictionaryEntryName");
		if(dataTypeTermNode == null && type.equals("simple")) {
			type = "complex";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:DictionaryEntryName");
		} else if(dataTypeTermNode == null && type.equals("complex")) {
			type = "simple";
			dataTypeTermNode = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:annotation/xsd:documentation/ccts:DictionaryEntryName");
		}
		
		//System.out.println("### " + type + " - " + dataTypeTermNode);
			
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
		
		
		String dt_sc_guid = "";
		String property_term = "";
		String representation_term = "";
		int owner_dT_iD = 0;
		String definition = (ccDefinitionElement != null) ? ccDefinitionElement.getTextContent() : null;
		int min_cardinality = 0;
		int max_cardinality = 0;
		
		
		int based_dT_sc_id; 
		
		NodeList attributeList = businessDataType_xsd.getNodeList("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
		if(attributeList == null || attributeList.getLength() == 0) {
			//System.out.println("##### " + "//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
		} else {
			
			for(int i = 0; i < attributeList.getLength(); i++) {
				Node attribute = attributeList.item(i);
				Element attrElement = (Element)attribute;
				dt_sc_guid = attrElement.getAttribute("id");
				
				if(attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
					min_cardinality = 0;
				else if(attrElement.getAttribute("use").equalsIgnoreCase("required"))
					min_cardinality = 1;
				
				String typeName1 = typeName.replaceAll("Type", "");
				DAOFactory df = DAOFactory.getDAOFactory();
				SRTDAO dao = df.getDAO("DT");
				QueryCondition qc = new QueryCondition();
				qc.add("data_type_term", dataTypeTerm);
				qc.add("den", typeName1 + ". Type");
				qc.add("dt_type", 1);
				DTVO dt_vo = ((DTVO)dao.findObject(qc));
				owner_dT_iD = dt_vo.getDTID();
				
				QueryCondition qc1 = new QueryCondition();
				qc1.add("owner_dt_iD", dt_vo.getBasedDTID());
				
				SRTDAO dao1 = df.getDAO("DTSC");
				
				DTSCVO dtsc_vo = (DTSCVO)dao1.findObject(qc1);
				
				based_dT_sc_id = dtsc_vo.getDTSCID();
				
				if(based_dT_sc_id != 0) {
				
					System.out.println("##### based_dT_sc_id = " + based_dT_sc_id);
					System.out.println("##### owner_dt_iD = " + dt_vo.getBasedDTID());
					
					Node propertyTermName = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation/ccts:PropertyTermName");
					if(propertyTermName != null) {
						property_term = ((Element)propertyTermName).getTextContent();
					} else {
						property_term = dtsc_vo.getPropertyTerm();
					}
						
					
					Node representationTermName = businessDataType_xsd.getNode("//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute/xsd:annotation/xsd:documentation/ccts:RepresentationTermName");
					if(representationTermName != null) {
						representation_term = ((Element)representationTermName).getTextContent();
					} else {
						representation_term = dtsc_vo.getRepresentationTerm();
					}
					
					System.out.println("##### " + "//xsd:"+type+"Type[@name = '" + typeName + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
					System.out.println("##### property_term = " + property_term);
					System.out.println("##### representation_term = " + representation_term);
		
					
					
					if(typeName1.equalsIgnoreCase("NormalizedString") || typeName1.equalsIgnoreCase("Token") || typeName1.equalsIgnoreCase("String"))
						max_cardinality = 0;
					else
						max_cardinality = 1;
					
					DTSCVO vo = new DTSCVO();
					vo.setDTSCGUID(dt_sc_guid);
					vo.setPropertyTerm(property_term);
					vo.setRepresentationTerm(representation_term);
					vo.setDefinition(definition);
					vo.setOwnerDTID(owner_dT_iD);
					
					vo.setMinCardinality(min_cardinality);
					vo.setMaxCardinality(max_cardinality);
					vo.setBasedDTSCID(based_dT_sc_id);
					
					
					dao1.insertObject(vo);		
				}
			}
		}
			
	}
	
	public static void main(String[] args) throws Exception{
		Utility.dbSetup();
		P_1_5_3_PopulateSCInDTSC p = new P_1_5_3_PopulateSCInDTSC();
		for (int i = 0; i < Types.dataTypeList.length; i++){
			p.importDataTypeList(Types.dataTypeList[i]);
		}
		for (int i = 0; i < Types.simpleTypeList.length; i++){
			p.importDataTypeList(Types.simpleTypeList[i]);
		}
	}
}
