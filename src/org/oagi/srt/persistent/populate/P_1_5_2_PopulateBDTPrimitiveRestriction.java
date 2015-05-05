package org.oagi.srt.persistent.populate;

import java.util.ArrayList;

import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dto.BDTPrimitiveRestrictionVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.DTVO;

/**
 * @version 1.0
 * @author Yunsu Lee
 */
public class P_1_5_2_PopulateBDTPrimitiveRestriction {
	
	//TODO MUST fix the value in 'isDefault' field
	
	public void run() throws Exception {
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO dao = df.getDAO("DT");
		SRTDAO aBDTPrimitiveRestrictionDAO = df.getDAO("BDTPrimitiveRestriction");
		SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");

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
				
				CDTAllowedPrimitiveVO aCDTAllowedPrimitiveVO = (CDTAllowedPrimitiveVO)aSRTObject3;
				
				QueryCondition qc4 = new QueryCondition();
				qc4.add("cdt_allowed_primitive_id", aCDTAllowedPrimitiveVO.getCDTAllowedPrimitiveID());
				ArrayList<SRTObject> al4 = aCDTAllowedPrimitiveExpressionTypeMapDAO.findObjects(qc4);
				
				for(SRTObject aSRTObject4 : al4) {
					CDTAllowedPrimitiveExpressionTypeMapVO aCDTAllowedPrimitiveExpressionTypeMapVO = (CDTAllowedPrimitiveExpressionTypeMapVO)aSRTObject4;
					// create insert statement
					BDTPrimitiveRestrictionVO aBDT_Primitive_RestrictionVO = new BDTPrimitiveRestrictionVO();
					aBDT_Primitive_RestrictionVO.setBDTID(aDTVO.getDTID());
					aBDT_Primitive_RestrictionVO.setCDTPrimitiveExpressionTypeMapID(aCDTAllowedPrimitiveExpressionTypeMapVO.getCDTPrimitiveExpressionTypeMapID());
					aBDT_Primitive_RestrictionVO.setisDefault(true);;
					
					aBDTPrimitiveRestrictionDAO.insertObject(aBDT_Primitive_RestrictionVO);
				}
			}
		}
	}

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		P_1_5_2_PopulateBDTPrimitiveRestriction p = new P_1_5_2_PopulateBDTPrimitiveRestriction();
		p.run();
	}
}
