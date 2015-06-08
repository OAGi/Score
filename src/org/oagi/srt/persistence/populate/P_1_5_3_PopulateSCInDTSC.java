package org.oagi.srt.persistence.populate;

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
import org.chanchan.common.persistence.db.DBAgent;
import org.chanchan.common.util.ServerProperties;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.OAGiNamespaceContext;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.oagi.srt.web.startup.SRTInitializer;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;
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
		fields_xsd = new XPathHandler(SRTConstants.FILEDS_XSD_FILE_PATH);
		businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
	}
	
	private static Connection conn = null;
	
	private void populateDTSC() throws SRTDAOException, XPathExpressionException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoDTSC = df.getDAO("DTSC");
		QueryCondition qc = new QueryCondition();
		qc.add("dt_type", 1);
		List<SRTObject> srtObjects = dao.findObjects(qc, conn);
		for(SRTObject srtObject :  srtObjects) {
			DTVO dtVO = (DTVO)srtObject;
			qc = new QueryCondition();
			qc.add("dt_id", dtVO.getBasedDTID());
			DTVO dtVO2 = (DTVO)dao.findObject(qc, conn);
			
			
			// default BDT
			if(dtVO2.getDTType() == 0) {
				String denType = Utility.denToTypeName(dtVO.getDEN());
				
				NodeList attributeList = businessDataType_xsd.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
				if(attributeList == null || attributeList.getLength() == 0) {
					//System.out.println("##### no attribute: " + dtVO.getDTGUID());
				} else {
					
					int min_cardinality = 0;
					int max_cardinality = 1;
					for(int i = 0; i < attributeList.getLength(); i++) {
						Node attribute = attributeList.item(i);
						Element attrElement = (Element)attribute;
						
						if(attrElement.getAttribute("use") == null || attrElement.getAttribute("use").equalsIgnoreCase("optional") || attrElement.getAttribute("use").equalsIgnoreCase("prohibited"))
							min_cardinality = 0;
						else if(attrElement.getAttribute("use").equalsIgnoreCase("required"))
							min_cardinality = 1;
						
						DTSCVO vo = new DTSCVO();
						vo.setDTSCGUID(attrElement.getAttribute("id"));
						
						Node propertyTermNode = businessDataType_xsd.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@name = '" + attrElement.getAttribute("name") + "']/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_PropertyTermName\"]");
						vo.setPropertyTerm(propertyTermNode.getTextContent());
						
						Node representationTermNode = businessDataType_xsd.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@name = '" + attrElement.getAttribute("name") + "']/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_RepresentationTermName\"]");
						vo.setRepresentationTerm(representationTermNode.getTextContent());
						
						Node definitionNode = businessDataType_xsd.getNode("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute[@name = '" + attrElement.getAttribute("name") + "']/xsd:annotation/xsd:documentation//*[local-name()=\"ccts_Definition\"]");
						
						vo.setDefinition(definitionNode.getTextContent());
						vo.setOwnerDTID(dtVO.getDTID());
						
						vo.setMinCardinality(min_cardinality);
						
						if(denType.equals("NormalizedStringType") || denType.equals("TokenType") || denType.equals("StringType"))
							max_cardinality = 0;
						vo.setMaxCardinality(max_cardinality);
						
						qc = new QueryCondition();
						qc.add("owner_dt_id", dtVO2.getDTID());
						DTSCVO dtVO3 = (DTSCVO)daoDTSC.findObject(qc, conn);
						vo.setBasedDTSCID(dtVO3.getDTSCID());
						
						daoDTSC.insertObject(vo);
						
					}
				}
				
			// unqualified BDT
			} else {
				qc = new QueryCondition();
				qc.add("owner_dt_id", dtVO.getBasedDTID());
				List<SRTObject> dtscVOs = daoDTSC.findObjects(qc, conn);
				for(SRTObject dtscObject: dtscVOs) {
					DTSCVO dtscVO = (DTSCVO)dtscObject;
					DTSCVO vo = new DTSCVO();
					
					vo.setDTSCGUID(Utility.generateGUID());  // set new GUID
					vo.setPropertyTerm(dtscVO.getPropertyTerm());
					vo.setRepresentationTerm(dtscVO.getRepresentationTerm());
					vo.setOwnerDTID(dtVO.getDTID());
					vo.setMinCardinality(dtscVO.getMinCardinality());
					vo.setMaxCardinality(dtscVO.getMaxCardinality());
					vo.setBasedDTSCID(dtscVO.getDTSCID());
					
					daoDTSC.insertObject(vo);
				}
				
				// for the attribute in unqualified BDT... exec Xpath using GUID e.g., something has XXX guid in the field.xsd
//				NodeList attributeList = fields_xsd.getNodeList("//xsd:complexType[@name = '" + denType + "']/xsd:simpleContent/xsd:extension/xsd:attribute");
//				if(attributeList != null && attributeList.getLength() > 0) {
//					System.out.println("##### no attribute: " + dtVO.getDTGUID());
//				} 
			}
			
		}
	}
	
	public void run() throws Exception {
		System.out.println("### 1.5.3 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		populateDTSC();
		
		tx.close();
		conn.close();
		System.out.println("### 1.5.3 End");
		
	}
	
	public static void main(String[] args) throws Exception{
		Utility.dbSetup();
		P_1_5_3_PopulateSCInDTSC p = new P_1_5_3_PopulateSCInDTSC();
		p.run();
	}
}
