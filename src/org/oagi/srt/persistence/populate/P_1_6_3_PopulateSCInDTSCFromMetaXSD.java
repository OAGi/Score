package org.oagi.srt.persistence.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTConstants;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.common.util.XPathHandler;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.web.startup.SRTInitializerException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
*
* @author Jaehun Lee
* @author Yunsu Lee
* @version 1.0
*
*/
public class P_1_6_3_PopulateSCInDTSCFromMetaXSD {
	
	private static Connection conn = null;
	
	public void importDTSCFromMeta() throws SRTDAOException, FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dtscDao = df.getDAO("DTSC");
		SRTDAO dtDao = df.getDAO("DT");
		
		// inherit all values from default Text BDT with two exceptions (max cardinality and based DTSC Id)
		QueryCondition qc_01 = new QueryCondition();
		qc_01.add("DT_GUID", "oagis-id-d5cb8551edf041389893fee25a496394");
		DTVO dtVO_01 = (DTVO)dtDao.findObject(qc_01, conn);
		
		QueryCondition qc_02 = new QueryCondition();
		qc_02.add("owner_dt_id", dtVO_01.getDTID());
		DTSCVO dtscVO_01 = (DTSCVO)dtscDao.findObject(qc_02, conn);
		
		DTSCVO dtscVO_02 = new DTSCVO();
		dtscVO_02.setBasedDTSCID(dtscVO_01.getDTSCID());
		dtscVO_02.setDefinition(dtscVO_01.getDefinition());
		dtscVO_02.setDTSCGUID(dtscVO_01.getDTSCGUID());
		dtscVO_02.setMaxCardinality(0);
		dtscVO_02.setMinCardinality(dtscVO_01.getMinCardinality());
		dtscVO_02.setOwnerDTID(dtscVO_01.getOwnerDTID());
		dtscVO_02.setPropertyTerm(dtscVO_01.getPropertyTerm());
		dtscVO_02.setRepresentationTerm(dtscVO_01.getRepresentationTerm());
		
		dtscDao.insertObject(dtscVO_02);
		
		// populate using attributes
		XPathHandler xh = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		NodeList complexTypes = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");
		for(int i = 0; i < complexTypes.getLength(); i++) {
		    Element ele = (Element)complexTypes.item(i);
		    String eleGuid = ele.getAttribute("id");
		    
		    QueryCondition qc_03 = new QueryCondition();
		    qc_03.add("dt_guid", eleGuid);
			DTVO dtVO_02 = (DTVO)dtDao.findObject(qc_03, conn);
			
		    NodeList attributes = ele.getElementsByTagName("xsd:attribute");
		    for(int j = 0; j < attributes.getLength(); j++) {
		    	Element attr = (Element)attributes.item(j);
		    	String attrName = attr.getAttribute("name");
		    	
		    	DTSCVO dtscVO_03 = new DTSCVO();
		    	dtscVO_03.setDTSCGUID(attr.getAttribute("id"));
		    	
		    	String use = attr.getAttribute("use");
		    	int minCardinality = 0;
		    	int maxCardinality = 1;
		    	minCardinality = (use == null) ? 0 : (use.equalsIgnoreCase("required")) ? 1: 0;
		    	maxCardinality = (use == null) ? 1 : (use.equalsIgnoreCase("prohibited")) ? 0: 1;
		    	dtscVO_03.setMaxCardinality(maxCardinality);
		    	dtscVO_03.setMinCardinality(minCardinality);
		    	
		    	dtscVO_03.setOwnerDTID(dtVO_02.getDTID());
		    	dtscVO_03.setPropertyTerm(Utility.spaceSeparator(attrName));
		    	dtscVO_03.setRepresentationTerm((attrName.equalsIgnoreCase("expressionLanguage")) ? "Text" : "Code");
				
				dtscDao.insertObject(dtscVO_03);
		    }
		}
	}
	
	public void run() throws Exception {
		System.out.println("### 1.6.3 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		importDTSCFromMeta();
		
		tx.close();
		conn.close();
		System.out.println("### 1.6.3 End");
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_3_PopulateSCInDTSCFromMetaXSD scindt_sc = new P_1_6_3_PopulateSCInDTSCFromMetaXSD();
		scindt_sc.run();
	}
}
