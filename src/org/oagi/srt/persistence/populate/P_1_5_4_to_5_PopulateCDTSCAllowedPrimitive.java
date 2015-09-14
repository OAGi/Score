package org.oagi.srt.persistence.populate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import org.chanchan.common.persistence.db.DBAgent;
import org.oagi.srt.common.QueryCondition;
import org.oagi.srt.common.SRTObject;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.dao.DAOFactory;
import org.oagi.srt.persistence.dao.SRTDAO;
import org.oagi.srt.persistence.dao.SRTDAOException;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTPrimitiveVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveExpressionTypeMapVO;
import org.oagi.srt.persistence.dto.CDTSCAllowedPrimitiveVO;
import org.oagi.srt.persistence.dto.DTSCVO;
import org.oagi.srt.persistence.dto.DTVO;
import org.oagi.srt.persistence.dto.XSDBuiltInTypeVO;

public class P_1_5_4_to_5_PopulateCDTSCAllowedPrimitive {
	private static Connection conn = null;
	
	public void run() throws Exception {
		System.out.println("### 1.5.4-5 Start");
		
		DBAgent tx = new DBAgent();
		conn = tx.open();
		
		DAOFactory df = DAOFactory.getDAOFactory();
		SRTDAO aDTSCDAO = df.getDAO("DTSC");
		//SRTDAO aBDTSCPrimitiveRestrictionDAO = df.getDAO("BDTSCPrimitiveRestriction");
		SRTDAO aCDTSCAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTSCAllowedPrimitiveExpressionTypeMap");
		//SRTDAO aCDTAllowedPrimitiveExpressionTypeMapDAO = df.getDAO("CDTAllowedPrimitiveExpressionTypeMap");
		SRTDAO aCDTSCAllowedPrimitiveDAO = df.getDAO("CDTSCAllowedPrimitive");
		SRTDAO aCDTAllowedPrimitiveDAO = df.getDAO("CDTAllowedPrimitive");
		SRTDAO aCDTPrimitiveDAO = df.getDAO("CDTPrimitive");
		SRTDAO aXBTDAO = df.getDAO("XSDBuiltInType");
		SRTDAO aDTDAO = df.getDAO("DT");
		
		
		QueryCondition qc_00 = new QueryCondition();
		qc_00.add("DT_Type", 0);
		qc_00.add("Data_Type_Term", "Name");
		DTVO aDTVO = (DTVO)aDTDAO.findObject(qc_00); 
		int nametypecdtid = aDTVO.getDTID();
		
		QueryCondition qc_01 = new QueryCondition();
		qc_01.add("Based_DT_ID", nametypecdtid);
		DTVO bDTVO = (DTVO)aDTDAO.findObject(qc_01);
		int basedbdtid = bDTVO.getDTID();
		ArrayList<SRTObject> dtlist = new ArrayList();
		dtlist.add(aDTDAO.findObject(qc_01));
		ArrayList<SRTObject> dtlist_check = new ArrayList();
		dtlist_check.add(aDTDAO.findObject(qc_01));
		while(dtlist_check.size() > 0) {
			bDTVO = (DTVO) dtlist_check.get(0);
			dtlist_check.remove(0);
			QueryCondition qc_02 = new QueryCondition();
			qc_02.add("Based_DT_ID", bDTVO.getDTID());
			dtlist.addAll(aDTDAO.findObjects(qc_02));
			dtlist_check.addAll(aDTDAO.findObjects(qc_02));
		}
		
		for(int i = 0 ; i < dtlist.size() ; i++){
			DTVO cDTVO = (DTVO)(dtlist.get(i));
			QueryCondition qc_03 = new QueryCondition();
			qc_03.add("Owner_DT_ID", cDTVO.getDTID());
			ArrayList<SRTObject> dtsclist = aDTSCDAO.findObjects(qc_03);
			for(int j = 0 ; j < dtsclist.size() ; j++) {
				DTSCVO aDTSCVO = (DTSCVO)dtsclist.get(j);
				QueryCondition qc_04 = new QueryCondition();
				qc_04.add("Data_Type_Term",aDTSCVO.getRepresentationTerm());
				ArrayList<SRTObject> dt_datatypelist = aDTDAO.findObjects(qc_04);
				for(int k = 0 ; k < dt_datatypelist.size(); k++) {
					DTVO tDTVO = (DTVO)dt_datatypelist.get(k);
					QueryCondition qc_05 = new QueryCondition();
					qc_05.add("CDT_ID", tDTVO.getDTID());
					CDTAllowedPrimitiveVO aCDTAllowedPrimitiveVO = (CDTAllowedPrimitiveVO) aCDTAllowedPrimitiveDAO.findObject(qc_05);
					if(aCDTAllowedPrimitiveVO.getCDTPrimitiveID() != 0) {
						CDTSCAllowedPrimitiveVO aVO = new CDTSCAllowedPrimitiveVO();
						aVO.setCDTSCID(aDTSCVO.getDTSCID());
						aVO.setCDTPrimitiveID(aCDTAllowedPrimitiveVO.getCDTPrimitiveID());
						aVO.setisDefault(aCDTAllowedPrimitiveVO.getisDefault());
						aCDTSCAllowedPrimitiveDAO.insertObject(aVO);
						
						QueryCondition qc_051 = new QueryCondition();
						qc_051.add("CDT_SC_ID", aVO.getCDTSCID());
						qc_051.add("CDT_Primitive_ID", aVO.getCDTPrimitiveID());
						CDTSCAllowedPrimitiveVO aCDTSCAllowedPrimitiveVO = (CDTSCAllowedPrimitiveVO) aCDTSCAllowedPrimitiveDAO.findObject(qc_051);
						int cdtscallowedprimitiveid = aCDTSCAllowedPrimitiveVO.getCDTSCAllowedPrimitiveID();
						
						QueryCondition qc_06 = new QueryCondition();
						qc_06.add("CDT_Primitive_ID", aVO.getCDTPrimitiveID());
						CDTPrimitiveVO aCDTPrimitiveVO = (CDTPrimitiveVO) aCDTPrimitiveDAO.findObject(qc_06);
						String cdt_primitive_name = aCDTPrimitiveVO.getName();
						ArrayList<String> xsd_builtin_type = new ArrayList<String>();

						if(cdt_primitive_name.equals("Binary")){
							xsd_builtin_type.add("xsd:base64Binary");
							xsd_builtin_type.add("xsd:hexBinary");
						}
						else if(cdt_primitive_name.equals("Boolean")){
							xsd_builtin_type.add("xsd:Boolean");
						}
						else if(cdt_primitive_name.equals("Decimal")){
							xsd_builtin_type.add("xsd:decimal");
						}
						else if(cdt_primitive_name.equals("Double")){
							xsd_builtin_type.add("xsd:double");
							xsd_builtin_type.add("xsd:float");
						}
						else if(cdt_primitive_name.equals("Float")){
							xsd_builtin_type.add("xsd:float");
						}
						else if(cdt_primitive_name.equals("Integer")){
							xsd_builtin_type.add("xsd:integer");
							xsd_builtin_type.add("xsd:nonNegativeInteger");
							xsd_builtin_type.add("xsd:positiveInteger");
						}
						else if(cdt_primitive_name.equals("NormalizedString")){
							xsd_builtin_type.add("xsd:normalizedString");
						}
						else if(cdt_primitive_name.equals("String")){
							xsd_builtin_type.add("xsd:string");
						}
						else if(cdt_primitive_name.equals("TimeDuration")){
							xsd_builtin_type.add("xsd:token");
							xsd_builtin_type.add("xsd:duration");
						}
						else if(cdt_primitive_name.equals("TimePoint")){
							xsd_builtin_type.add("xsd:token");
							xsd_builtin_type.add("xsd:dateTime");
							xsd_builtin_type.add("xsd:date");
							xsd_builtin_type.add("xsd:time");
							xsd_builtin_type.add("xsd:gYearMonth");
							xsd_builtin_type.add("xsd:gYear");
							xsd_builtin_type.add("xsd:gMonthDay");
							xsd_builtin_type.add("xsd:gDay");
							xsd_builtin_type.add("xsd:gMonth");
						}
						else if(cdt_primitive_name.equals("Token")){
							xsd_builtin_type.add("xsd:token");
						}
							
						for(int l = 0 ; l < xsd_builtin_type.size(); l++){
							CDTSCAllowedPrimitiveExpressionTypeMapVO bVO = new CDTSCAllowedPrimitiveExpressionTypeMapVO();
							bVO.setCDTSCAllowedPrimitive(cdtscallowedprimitiveid);
							QueryCondition qc_07 = new QueryCondition();
							qc_07.add("BuiltIn_Type", xsd_builtin_type.get(l));
							XSDBuiltInTypeVO cVO = (XSDBuiltInTypeVO) aXBTDAO.findObject(qc_07);
							bVO.setXSDBuiltInTypeID(cVO.getXSDBuiltInTypeID());
							aCDTSCAllowedPrimitiveExpressionTypeMapDAO.insertObject(bVO);
						}					
					}
				}
			}
		}
		System.out.println("### 1.5.4-5 End");
	}
	
	
	public static void main(String args[]) throws Exception {
		Utility.dbSetup();
		P_1_5_4_to_5_PopulateCDTSCAllowedPrimitive p = new P_1_5_4_to_5_PopulateCDTSCAllowedPrimitive();
		p.run();
	}
	
}

