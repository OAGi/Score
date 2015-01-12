package org.oagi.srt.persistent.populate;

import java.util.ArrayList;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.BDTSCPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CodeListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DTSCVO;

/**
 * @version 1.0
 * @author Yunsu Lee
 */
public class P_1_5_4_PopulateBDTSCPrimitiveRestriction {
	
	//TODO MUST fix the value in 'isDefault' field

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DTSC");
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO aCodeListDAO = df.getDAO("CodeList");

		ArrayList<SRTObject> al =  dao.findObjects();
		for(SRTObject aSRTObject : al) {
			DTSCVO aDTSCVO = (DTSCVO)aSRTObject;
			if(aDTSCVO.getBasedDTSCID() != 0) {
				
				if(aDTSCVO.getRepresentationTerm().contains("Code")) {
					System.out.println("### " + aDTSCVO.getDTSCID());
					BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
					bVO.setBDTSCID(aDTSCVO.getDTSCID());
					QueryCondition qc = new QueryCondition();
					qc.addLikeClause("name", "%" + aDTSCVO.getPropertyTerm() + "%");
					System.out.println("###1 " + aDTSCVO.getPropertyTerm());
					bVO.setCodeListID(((CodeListVO)aCodeListDAO.findObject(qc)).getCodeListID());
					bVO.setisDefault(true);
					bVO.setAgencyIDListID(0);
					
					aBDTPrimitiveRestrictionDAO.insertObject(bVO);
				} else {
					int dt_sc_id = aDTSCVO.getBasedDTSCID();
					
					QueryCondition qc = new QueryCondition();
					qc.add("cdt_sc_id", aDTSCVO.getBasedDTSCID());
					
					ArrayList<SRTObject> al3 = aCDTAllowedPrimitiveDAO.findObjects(qc);
					//System.out.println("###1 " + aDTSCVO.getBasedDTSCID());
					for(SRTObject aSRTObject3 : al3) {
						
						
						CDTSCAllowedPrimitiveVO aCDTAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO)aSRTObject3;
						//System.out.println("### " + aCDTAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID());
						
						QueryCondition qc1 = new QueryCondition();
						qc1.add("cdt_sc_allowed_primitive", aCDTAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID());
						ArrayList<SRTObject> al4 = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc1);
						for(SRTObject aSRTObject4 : al4) {
							CDTSCAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExVO = (CDTSCAllowedPrimitiveExpressionTypeMapVO)aSRTObject4;
							
							BDTSCPrimitiveRestrictionVO bVO = new BDTSCPrimitiveRestrictionVO();
							bVO.setBDTSCID(aDTSCVO.getDTSCID());
							
							System.out.println("###22 " + aCDTAllowedPrimitiveExVO.getCTSCAllowedPrimitiveExpressionTypeMapID());
							
							
							bVO.setCDTSCAllowedPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExVO.getCTSCAllowedPrimitiveExpressionTypeMapID());
							bVO.setisDefault(true); // TODO get the default value correctly
							
							aBDTPrimitiveRestrictionDAO.insertObject(bVO);
						}
					}
					
				}
				
			}
			
		}
	}
}