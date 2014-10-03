package org.oagi.srt.persistent.populate;

import java.util.ArrayList;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dto.BDT_Primitive_RestrictionVO;
import org.oagi.srt.persistence.dto.CDT_Allowed_PrimitiveVO;
import org.oagi.srt.persistence.dto.CDT_Allowed_Primitive_Expression_Type_MapVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
 * @version 1.0
 * @author Yunsu Lee
 */
public class PopulateBDTPrimitiveRestriction {
	
	//TODO MUST fix the value in 'isDefault' field

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDT_Primitive_Restriction");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDT_Allowed_Primitive_Expression_Type_Map");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDT_Allowed_Primitive");

		QueryCondition qc = new QueryCondition();
		qc.add("DT_Type", new Integer(1));
		ArrayList<SRTObject> al =  dao.findObjects(qc);
		for(SRTObject aSRTObject : al) {
			DTVO aDTVO = (DTVO)aSRTObject;
			
			QueryCondition qc1 = new QueryCondition();
			qc1.add("dt_id", aDTVO.getBasedDTID());
			DTVO parentDT = (DTVO)dao.findObject(qc1);
			if(parentDT.getDTType() == 1) {
				QueryCondition qc11 = new QueryCondition();
				qc11.add("dt_id", parentDT.getBasedDTID());
				DTVO aDTVO11 = (DTVO)dao.findObject(qc11);
				aDTVO.setBasedDTID(aDTVO11.getBasedDTID());
			}
				
			QueryCondition qc3 = new QueryCondition();
			qc3.add("cdt_id", aDTVO.getBasedDTID());
			ArrayList<SRTObject> al3 = aCDTAllowedPrimitiveDAO.findObjects(qc3);
			
			for(SRTObject aSRTObject3 : al3) {
				
				CDT_Allowed_PrimitiveVO aCDTAllowedPrimitiveVO = (CDT_Allowed_PrimitiveVO)aSRTObject3;
				
				QueryCondition qc4 = new QueryCondition();
				qc4.add("cdt_allowed_primitive_id", aCDTAllowedPrimitiveVO.getCDTAllowedPrimitiveID());
				ArrayList<SRTObject> al4 = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4);
				
				for(SRTObject aSRTObject4 : al4) {
					CDT_Allowed_Primitive_Expression_Type_MapVO aCDTAllowedPrimitiveExpressionTypeMapVO = (CDT_Allowed_Primitive_Expression_Type_MapVO)aSRTObject4;
					// create insert statement
					BDT_Primitive_RestrictionVO aBDT_Primitive_RestrictionVO = new BDT_Primitive_RestrictionVO();
					aBDT_Primitive_RestrictionVO.setBDTID(aDTVO.getDTID());
					aBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
					aBDT_Primitive_RestrictionVO.setisDefault(true);;
					
					aBDTPrimitiveRestrictionDAO.insertObject(aBDT_Primitive_RestrictionVO);
				}
			}
		}
	}
}
