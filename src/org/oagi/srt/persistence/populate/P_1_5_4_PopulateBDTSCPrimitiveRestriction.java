package org.oagi.srt.persistence.populate;

import java.sql.Connection;
import java.util.ArrayList;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
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

/**
 * @version 1.0
 * @author Yunsu Lee
 */
public class P_1_5_4_PopulateBDTSCPrimitiveRestriction {
	
	private static Connection conn = null;

	public void run() throws Exception {
		System.out.println("### 1.5.4 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
		SRTDAO aBDTSCPrimitiveRestrictionDAO = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO aCodeListDAO = df.getDAO("CodeList");
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		SRTDAO aXBTDAO = df.getDAO("XSDBuiltInType");
		SRTDAO aDTDAO = df.getDAO("DT");

		ArrayList<SRTObject> al =  dao.findObjects(conn);
		for(SRTObject aSRTObject : al) {
			DTSCVO aDTSCVO = (DTSCVO)aSRTObject;
			if(aDTSCVO.getBasedDTSCID() != 0) {
				
				QueryCondition qc00 = new QueryCondition();
				qc00.add("dt_id", aDTSCVO.getOwnerDTID());
				DTVO dtVO = (DTVO)aDTDAO.findObject(qc00, conn);
				
				if(aDTSCVO.getRepresentationTerm().contains("Code")) { // && dtVO.getDataTypeTerm().contains("Code")) {
					BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
					bVO.setBDTSCID(aDTSCVO.getDTSCID());
					QueryCondition qc = new QueryCondition();
					qc.addLikeClause("name", "%" + aDTSCVO.getPropertyTerm().replaceAll(" ", "") + "%");
					bVO.setCodeListID(((CodeListVO)aCodeListDAO.findObject(qc, conn)).getCodeListID());
					bVO.setisDefault(true);
					bVO.setAgencyIDListID(0);
					
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
					qc03.add("CDT_Primitive_id", CDT_Primitive_id);
					int cdt_sc_allowed_primitive_id = ((CDTSCAllowedPrimitiveVO)aCDTAllowedPrimitiveDAO.findObject(qc03, conn)).getCDTSCAllowedPrimitiveID();
					
					QueryCondition qc04 = new QueryCondition();
					qc04.add("CDT_SC_Allowed_Primitive", cdt_sc_allowed_primitive_id);
					qc04.add("xsd_builtin_type_id", xbt_id);
					int CDTAllowedPrimitiveExpressionTypeMapID = ((CDTSCAllowedPrimitiveExpressionTypeMapVO)aCDTAllowedPrimitiveExpressionTypeMapDAO.findObject(qc04, conn)).getCTSCAllowedPrimitiveExpressionTypeMapID();
					
					BDTSCPrimitiveRestrictionVO bVO1 = new BDTSCPrimitiveRestrictionVO();
					bVO1.setBDTSCID(aDTSCVO.getDTSCID());
					
					bVO1.setCDTSCAllowedPrimitiveExpressionTypeMapID(CDTAllowedPrimitiveExpressionTypeMapID);
					bVO1.setisDefault(true); // TODO get the default value correctly
					
					aBDTSCPrimitiveRestrictionDAO.insertObject(bVO1);
					
					
				} else {
					QueryCondition qc = new QueryCondition();
					qc.add("cdt_sc_id", aDTSCVO.getBasedDTSCID());
					
					ArrayList<SRTObject> al3 = aCDTAllowedPrimitiveDAO.findObjects(qc, conn);
					if(al3.size() < 1) {
						QueryCondition qc4 = new QueryCondition();
						qc4.add("dt_sc_id", aDTSCVO.getBasedDTSCID());;
						
						qc = new QueryCondition();
						qc.add("cdt_sc_id", ((DTSCVO)dao.findObject(qc4, conn)).getBasedDTSCID());
						
						al3 = aCDTAllowedPrimitiveDAO.findObjects(qc, conn);
					}
					
					for(SRTObject aSRTObject3 : al3) {
						CDTSCAllowedPrimitiveVO aCDTAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO)aSRTObject3;
						
						QueryCondition qc1 = new QueryCondition();
						qc1.add("cdt_sc_allowed_primitive", aCDTAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID());
						ArrayList<SRTObject> al4 = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc1, conn);
						for(SRTObject aSRTObject4 : al4) {
							CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO)aSRTObject4;
							
							BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
							bVO.setBDTSCID(aDTSCVO.getDTSCID());
							
							bVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExVO.getCTSCAllowedPrimitiveExpressionTypeMapID());
							bVO.setisDefault(true); // TODO get the default value correctly
							
							aBDTSCPrimitiveRestrictionDAO.insertObject(bVO);
						}
					}
					
				}
				
			}
		}
		
		tx.close();
		conn.close();
		System.out.println("### 1.5.4 End");
	}

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		P_1_5_4_PopulateBDTSCPrimitiveRestriction p = new P_1_5_4_PopulateBDTSCPrimitiveRestriction();
		p.run();
	}
}