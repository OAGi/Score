package org.oagi.srt.persistence.populate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
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
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.UserVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
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

public class P_1_6_1_to_2_PopulateDTFromMetaXSD {
	
	private static Connection conn = null;
	
	public void importAdditionalBDT() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
		XPathHandler xh = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO daoUser = df.getDAO("User");
		DTVO dtVO = new DTVO();

		NodeList result = xh.getNodeList("//xsd:complexType[@name='ExpressionType' or @name='ActionExpressionType' or @name='ResponseExpressionType']");
	    
		for(int i = 0; i < result.getLength(); i++) {
		    Element ele = (Element)result.item(i);
		    String name = ele.getAttribute("name");
		    
		    dtVO.setDTGUID(ele.getAttribute("id"));
		    dtVO.setDTType(1);
		    dtVO.setVersionNumber("1.0");
		    dtVO.setRevisionType(0);
		    
		    QueryCondition qc = new QueryCondition();
			qc.add("DT_GUID", "oagis-id-d5cb8551edf041389893fee25a496395");
			DTVO dtVO_01 = (DTVO)dao.findObject(qc, conn);
		    
		    
		    dtVO.setBasedDTID(dtVO_01.getDTID());
		    dtVO.setDataTypeTerm(dtVO_01.getDataTypeTerm());
		    
		    String den = name.substring(0, name.lastIndexOf("Type")) + ". Type";
		    if(den.contains("ID")){
		    	den = den.replace("ID", "Identifier");
		    }
		    dtVO.setDEN(den);
		    dtVO.setContentComponentDEN(den + ". Content");
		    
		    Element definition = (Element)ele.getElementsByTagName("xsd:documentation").item(0);
		    if(definition != null) 
			    dtVO.setDefinition(definition.getTextContent());
		    else 
		    	dtVO.setDefinition(null);
		    
		    dtVO.setContentComponentDefinition(null);
		    dtVO.setRevisionDocumentation(null);
		    dtVO.setRevisionState(1);
		    
		    QueryCondition qc_02 = new QueryCondition();
			qc.add("User_Name", "oagis");
			int userId = ((UserVO)daoUser.findObject(qc_02, conn)).getUserID();
			dtVO.setCreatedByUserId(userId);
			dtVO.setLastUpdatedByUserId(userId);
			dtVO.setRevisionDocumentation("");
			
		    dao.insertObject(dtVO);
		    
		    // BDT_Primitive_Restriction
		    QueryCondition qc2 = new QueryCondition();
			qc2.add("DT_GUID", dtVO.getDTGUID());
			
			insertBDTPrimitiveRestriction(dtVO_01.getDTID(), ((DTVO)dao.findObject(qc2, conn)).getDTID());
	    }
	}
	
	private void insertBDTPrimitiveRestriction(int basedBdtId, int bdtId) throws SRTDAOException {
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		
		QueryCondition qc = new QueryCondition();
		qc.add("bdt_id", basedBdtId);
		ArrayList<SRTObject> al = aBDTPrimitiveRestrictionDAO.findObjects(qc, conn);
		
		for(SRTObject aSRTObject : al) {
			BDTPrimitiveRestrictionVO aBDTPrimitiveRestrictionVO = (BDTPrimitiveRestrictionVO)aSRTObject;
			BDTPrimitiveRestrictionVO theBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
			theBDT_Primitive_RestrictionVO.setBDTID(bdtId);
			theBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aBDTPrimitiveRestrictionVO.getCDTPrimitiveExpressionTypeMapID());
			theBDT_Primitive_RestrictionVO.setisDefault(aBDTPrimitiveRestrictionVO.getisDefault());
			
			aBDTPrimitiveRestrictionDAO.insertObject(theBDT_Primitive_RestrictionVO);
		}
	}
	
	public void run() throws Exception {
		System.out.println("### 1.6.1-2 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		importAdditionalBDT();
		
		tx.close();
		conn.close();
		System.out.println("### 1.6.1-2 End");
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_1_to_2_PopulateDTFromMetaXSD dt = new P_1_6_1_to_2_PopulateDTFromMetaXSD();
		dt.run();
	}
}
