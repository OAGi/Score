package org.oagi.srt.persistent.populate;

import java.util.ArrayList;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dto.BDT_Primitive_RestrictionVO;
import org.oagi.srt.persistence.dto.BDT_SC_Primitive_RestrictionVO;
import org.oagi.srt.persistence.dto.CDT_Allowed_PrimitiveVO;
import org.oagi.srt.persistence.dto.CDT_Allowed_Primitive_Expression_Type_MapVO;
import org.oagi.srt.persistence.dto.CDT_SC_Allowed_PrimitiveVO;
import org.oagi.srt.persistence.dto.CDT_SC_Allowed_Primitive_Expression_Type_MapVO;
import org.oagi.srt.persistence.dto.Code_ListVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.DT_SCVO;

/**
 * @version 1.0
 * @author Yunsu Lee
 */
public class PopulateBDT_SC_Primitive_Restriction {
	
	//TODO MUST fix the value in 'isDefault' field

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT_SC");
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDT_SC_Primitive_Restriction");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDT_SC_Allowed_Primitive_Expression_Type_Map");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDT_SC_Allowed_Primitive");
		SRTDAO aCodeListDAO = df.getDAO("Code_List");

		ArrayList<SRTObject> al =  dao.findObjects();
		for(SRTObject aSRTObject : al) {
			DT_SCVO aDTSCVO = (DT_SCVO)aSRTObject;
			if(aDTSCVO.getBasedDTSCID() != 0) {
				
				if(aDTSCVO.getRepresentationTerm().contains("Code")) {
					System.out.println("### " + aDTSCVO.getDTSCID());
					BDT_SC_Primitive_RestrictionVO bVO = new BDT_SC_Primitive_RestrictionVO();
					bVO.setBDTSCID(aDTSCVO.getDTSCID());
					QueryCondition qc = new QueryCondition();
					qc.addLikeClause("name", "%" + aDTSCVO.getPropertyTerm() + "%");
					System.out.println("###1 " + aDTSCVO.getPropertyTerm());
					bVO.setCodeListID(((Code_ListVO)aCodeListDAO.findObject(qc)).getCodeListID());
					bVO.setisDefault(true);
					
					aBDTPrimitiveRestrictionDAO.insertObject(bVO);
				} else {
					int dt_sc_id = aDTSCVO.getBasedDTSCID();
					
					QueryCondition qc = new QueryCondition();
					qc.add("cdt_sc_id", aDTSCVO.getBasedDTSCID());
					
					ArrayList<SRTObject> al3 = aCDTAllowedPrimitiveDAO.findObjects(qc);
					//System.out.println("###1 " + aDTSCVO.getBasedDTSCID());
					for(SRTObject aSRTObject3 : al3) {
						
						
						CDT_SC_Allowed_PrimitiveVO aCDTAllowedPrimitiveVO = (CDT_SC_Allowed_PrimitiveVO)aSRTObject3;
						//System.out.println("### " + aCDTAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID());
						
						QueryCondition qc1 = new QueryCondition();
						qc1.add("cdt_sc_allowed_primitive", aCDTAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID());
						ArrayList<SRTObject> al4 = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc1);
						for(SRTObject aSRTObject4 : al4) {
							CDT_SC_Allowed_Primitive_Expression_Type_MapVO aCDTAllowedPrimitiveExVO = (CDT_SC_Allowed_Primitive_Expression_Type_MapVO)aSRTObject4;
							
							BDT_SC_Primitive_RestrictionVO bVO = new BDT_SC_Primitive_RestrictionVO();
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
