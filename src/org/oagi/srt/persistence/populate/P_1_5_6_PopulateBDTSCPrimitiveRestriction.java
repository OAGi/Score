package org.oagi.srt.persistence.populate;

import java.sql.Connection;
import java.util.ArrayList;

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
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version 1.0
 * @author Yunsu Lee
 */
public class P_1_5_6_PopulateBDTSCPrimitiveRestriction {
	
	private static Connection conn = null;
	
	public int getAgencyListID() throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("AgencyIDList");
    	QueryCondition qc = new QueryCondition();
		qc.add("name", "Agency Identification");
		AgencyIDListVO agencyidlistVO = (AgencyIDListVO)dao.findObject(qc);
		return agencyidlistVO.getAgencyIDListID();
	}

	public void run() throws Exception {
		System.out.println("### 1.5.6 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
		SRTDAO aBDTSCPrimitiveRestrictionDAO = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		SRTDAO aCodeListDAO = df.getDAO("CodeList");
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		SRTDAO aXBTDAO = df.getDAO("XSDBuiltInType");
		SRTDAO aDTDAO = df.getDAO("DT");
		
		XPathHandler xh = new XPathHandler(SRTConstants.BUSINESS_DATA_TYPE_XSD_FILE_PATH);

		ArrayList<SRTObject> al = dao.findObjects(conn);
		for(SRTObject aSRTObject : al) {
			DTSCVO aDTSCVO = (DTSCVO)aSRTObject;
			String tmp_guid = null;
			if(aDTSCVO.getBasedDTSCID() != 0) {
				
				Node result = xh.getNode("//xsd:attribute[@id='" + aDTSCVO.getDTSCGUID() + "']");
				tmp_guid = aDTSCVO.getDTSCGUID();
				if(result == null) { // if result is null, then look up its based default BDT and get guid
					DTSCVO dtscVO = getDTSC(aDTSCVO.getBasedDTSCID());
					result = xh.getNode("//xsd:attribute[@id='" + dtscVO.getDTSCGUID() + "']");
					tmp_guid = dtscVO.getDTSCGUID();
				}
				if(result == null){
					System.out.println(aDTSCVO.getDTSCGUID()+" is inherited bdt from cdt");
					continue;
				}
				else
					System.out.println("BDT SC Primitive restriction for "+aDTSCVO.getDTSCGUID()+" is populated");
				Element ele = (Element)result;
				QueryCondition qc_00 = new QueryCondition();
				
				qc_00.add("name", ele.getAttribute("type").replaceAll("ContentType", ""));
				int codeListId = ((CodeListVO)aCodeListDAO.findObject(qc_00, conn)).getCodeListID();
				
				if((aDTSCVO.getRepresentationTerm().contains("Code") && codeListId > 0) || ele.getAttribute("name").contains("AgencyID")) { // && dtVO.getDataTypeTerm().contains("Code")) {
					
					BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
					bVO.setBDTSCID(aDTSCVO.getDTSCID());
					bVO.setisDefault(false);
					
					if(ele.getAttribute("name").contains("AgencyID")) {
						bVO.setAgencyIDListID(getAgencyListID());
					} else {
						bVO.setCodeListID(codeListId);
					}
					aBDTSCPrimitiveRestrictionDAO.insertObject(bVO);
					
					
					QueryCondition qc01 = new QueryCondition();
					qc01.add("name", "Token");
					int CDT_Primitive_id = ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc01, conn)).getCDTPrimitiveID();
					
					QueryCondition qc02 = new QueryCondition();
					qc02.add("builtin_type", "xsd:token");
					int xbt_id = ((XSDBuiltInTypeVO)aXBTDAO.findObject(qc02, conn)).getXSDBuiltInTypeID();
					
					
					QueryCondition qc021 = new QueryCondition();
					qc021.add("dt_sc_id", aDTSCVO.getBasedDTSCID());
					DTSCVO stscVO = (DTSCVO)dao.findObject(qc021, conn);
					int cdt_id = 0;
					if(stscVO.getBasedDTSCID() == 0)
						cdt_id = aDTSCVO.getBasedDTSCID();
					else
						cdt_id = stscVO.getBasedDTSCID();
					
					QueryCondition qc03 = new QueryCondition();
					qc03.add("cdt_sc_id", cdt_id); 
					qc03.add("CDT_Pri_id", CDT_Primitive_id);
					int cdt_sc_awd_pri_id = ((CDTSCAllowedPrimitiveVO)aCDTSCAllowedPrimitiveDAO.findObject(qc03, conn)).getCDTSCAllowedPrimitiveID();
					
					QueryCondition qc04 = new QueryCondition();
					qc04.add("CDT_SC_awd_pri", cdt_sc_awd_pri_id);
					qc04.add("xbt_id", xbt_id);
					int CDTAllowedPrimitiveExpressionTypeMapID = ((CDTSCAllowedPrimitiveExpressionTypeMapVO)aCDTSCAllowedPrimitiveExpressionTypeMapDAO.findObject(qc04, conn)).getCTSCAllowedPrimitiveExpressionTypeMapID();
					
					BDTSCPrimitiveRestrictionVO bVO1 = new BDTSCPrimitiveRestrictionVO();
					bVO1.setBDTSCID(aDTSCVO.getDTSCID());
					
					bVO1.setCDTSCAllowedPrimitiveExpressionTypeMapID(CDTAllowedPrimitiveExpressionTypeMapID);
					bVO1.setisDefault(true); 
					
					aBDTSCPrimitiveRestrictionDAO.insertObject(bVO1);
				} else {
					QueryCondition qc = new QueryCondition();
					qc.add("cdt_sc_id", aDTSCVO.getBasedDTSCID());
					
					ArrayList<SRTObject> al3 = aCDTSCAllowedPrimitiveDAO.findObjects(qc, conn);
					if(al3.size() < 1) {
						QueryCondition qc4 = new QueryCondition();
						qc4.add("dt_sc_id", aDTSCVO.getBasedDTSCID());;
						
						qc = new QueryCondition();
						qc.add("cdt_sc_id", ((DTSCVO)dao.findObject(qc4, conn)).getBasedDTSCID());
						
						al3 = aCDTSCAllowedPrimitiveDAO.findObjects(qc, conn);
					}
					
					for(SRTObject aSRTObject3 : al3) {
						CDTSCAllowedPrimitiveVO aCDTSCAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO)aSRTObject3;
						
						QueryCondition qc1 = new QueryCondition();
						qc1.add("cdt_sc_awd_pri", aCDTSCAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID());
						ArrayList<SRTObject> al4 = aCDTSCAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc1, conn);
						for(SRTObject aSRTObject4 : al4) {
							CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTSCAllowedPrimitiveExVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO)aSRTObject4;
							
							BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
							bVO.setBDTSCID(aDTSCVO.getDTSCID());
							
							bVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(aCDTSCAllowedPrimitiveExVO.getCTSCAllowedPrimitiveExpressionTypeMapID());
							
//							QueryCondition qc5 = new QueryCondition();
//							qc5.add("cdt_allowed_primitive", aCDTSCAllowedPrimitiveExVO.getCDTSCAllowedPrimitive());
//							qc5.add("xsd_builtin_type_id", aCDTSCAllowedPrimitiveExVO.getXSDBuiltInTypeID());
//							aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc5, conn);
							
							XSDBuiltInTypeVO xbtVO = getXSDBuiltInTypeID(aCDTSCAllowedPrimitiveExVO.getXSDBuiltInTypeID());
							String xdtName = xbtVO.getBuiltInType();
							
//							String representationTerm = aDTSCVO.getRepresentationTerm();
							int cdtPrimitiveId = aCDTSCAllowedPrimitiveVO.getCDTPrimitiveID();
							
							String representationTerm = aDTSCVO.getRepresentationTerm();
							if(representationTerm.equalsIgnoreCase("Code") && xdtName.equalsIgnoreCase("xsd:token") && "Token".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Identifier") && xdtName.equalsIgnoreCase("xsd:token") && "Token".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Name") && xdtName.equalsIgnoreCase("xsd:token") && "Token".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Indicator") && xdtName.equalsIgnoreCase("xsd:boolean") && "Boolean".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Value") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Text") && xdtName.equalsIgnoreCase("xsd:string") && "Text".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Number") && xdtName.equalsIgnoreCase("xsd:decimal") && "Decimal".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else if(representationTerm.equalsIgnoreCase("Date Time") && xdtName.equalsIgnoreCase("xsd:token") && "Timepoint".equalsIgnoreCase(getCDTPrimitiveName(cdtPrimitiveId))) {
								bVO.setisDefault(true); 
							} else {
								bVO.setisDefault(false); 
							}
							aBDTSCPrimitiveRestrictionDAO.insertObject(bVO);
						}
					}
				}
			}
		}
		
		tx.close();
		conn.close();
		System.out.println("### 1.5.6 End");
	}
	
	public DTSCVO getDTSC(int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aDTDAO = df.getDAO("DTSC");
    	QueryCondition qc = new QueryCondition();
		qc.add("dt_sc_id",  id);
		return (DTSCVO)aDTDAO.findObject(qc, conn);
	}
	
	public String getCDTPrimitiveName(int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
    	QueryCondition qc = new QueryCondition();
		qc.add("cdt_pri_id",  id);
		return ((CDTPrimitiveVO)aCDTPrimitiveDAO.findObject(qc)).getName();
	}
	
	public XSDBuiltInTypeVO getXSDBuiltInTypeID(int id) throws SRTDAOException{
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aXSDBuiltInTypeDAO = df.getDAO("XSDBuiltInType");
    	QueryCondition qc = new QueryCondition();
		qc.add("xbt_id", id);
		return (XSDBuiltInTypeVO)aXSDBuiltInTypeDAO.findObject(qc);
	}

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		P_1_5_6_PopulateBDTSCPrimitiveRestriction p = new P_1_5_6_PopulateBDTSCPrimitiveRestriction();
		p.run();
	}
}