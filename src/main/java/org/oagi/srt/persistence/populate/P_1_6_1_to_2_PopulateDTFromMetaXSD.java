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
import org.oagi.srt.persistence.populate.P_1_5_3_to_5_PopulateSCInDTSC;
import org.oagi.srt.persistence.populate.P_1_5_6_PopulateBDTSCPrimitiveRestriction;

/**
*
* @author Jaehun Lee
* @author Yunsu Lee
* @version 1.0
*
*/

public class P_1_6_1_to_2_PopulateDTFromMetaXSD {
	
	private static Connection conn = null;
	
	public void importAdditionalBDT(XPathHandler xh) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, XPathExpressionException, SRTInitializerException, SRTDAOException {
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
		    //dtVO.setRevisionType(0);
		    
		    Node extension = xh.getNode("//xsd:complexType[@name = '" + name + "']/xsd:simpleContent/xsd:extension");
		    String base = Utility.typeToDen(((Element)extension).getAttribute("base"));
		    QueryCondition qc = new QueryCondition();
			qc.add("den", base);
			DTVO dtVO_01 = (DTVO)dao.findObject(qc, conn);
		    
		    
		    dtVO.setBasedDTID(dtVO_01.getDTID());
		    dtVO.setDataTypeTerm(dtVO_01.getDataTypeTerm());
		    
		    dtVO.setDEN(Utility.typeToDen(name));
		    dtVO.setContentComponentDEN(Utility.typeToContent(name));
		    
		    Element definition = (Element)ele.getElementsByTagName("xsd:documentation").item(0);
		    if(definition != null) 
			    dtVO.setDefinition(definition.getTextContent());
		    else 
		    	dtVO.setDefinition(null);
		    
		    dtVO.setContentComponentDefinition(null);
		    dtVO.setRevisionDocumentation(null);
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
			System.out.println("Populating additonal BDTs from meta whose name is "+ name);
		    dao.insertObject(dtVO);
		    
		    // BDT_Primitive_Restriction
		    QueryCondition qc2 = new QueryCondition();
			qc2.add("guid", dtVO.getDTGUID());
			
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
			System.out.println("Populating BDT Primitive Restriction for bdt id = " + bdtId+ " cdt primitive expression type map = "+theBDT_Primitive_RestrictionVO.getCDTPrimitiveExpressionTypeMapID()+" is_default = " + theBDT_Primitive_RestrictionVO.getisDefault());
			aBDTPrimitiveRestrictionDAO.insertObject(theBDT_Primitive_RestrictionVO);
		}
	}
	
	public void run() throws Exception {
		System.out.println("### 1.6. Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		XPathHandler businessDataType_xsd = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);
		XPathHandler meta_xsd = new XPathHandler(SRTConstants.META_XSD_FILE_PATH);
		//importAdditionalBDT(meta_xsd);
		
		P_1_5_3_to_5_PopulateSCInDTSC dtsc = new P_1_5_3_to_5_PopulateSCInDTSC();
		//dtsc.populateDTSCforUnqualifiedBDT(businessDataType_xsd, meta_xsd, conn, false);
		
		P_1_5_6_PopulateBDTSCPrimitiveRestriction bdtscpri = new P_1_5_6_PopulateBDTSCPrimitiveRestriction();
		bdtscpri.populateBDTSCPrimitiveRestriction(businessDataType_xsd, meta_xsd, false);
		
		tx.close();
		conn.close();
		System.out.println("### 1.6. End");
	}
	
	public static void main (String args[]) throws Exception {
		Utility.dbSetup();
		P_1_6_1_to_2_PopulateDTFromMetaXSD dt = new P_1_6_1_to_2_PopulateDTFromMetaXSD();
		dt.run();
	}
}
