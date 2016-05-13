package org.oagi.srt.persistence.populate;

import org.oagi.srt.common.util.Utility;

public class RunAll {

	public static void main(String args[]) throws Exception {
		Utility.dbSetup();

		P_1_3_PopulateAgencyIDList p1 = new P_1_3_PopulateAgencyIDList();
//		p1.run();
		
		P_1_4_PopulateCodeList p2 = new P_1_4_PopulateCodeList();
//		p2.run();
		
		P_1_5_1_to_2_PopulateBDTsInDT p3 = new P_1_5_1_to_2_PopulateBDTsInDT();
//		p3.run();
		
		P_1_5_3_to_5_PopulateSCInDTSC p4 = new P_1_5_3_to_5_PopulateSCInDTSC();
		p4.run();
		
		P_1_5_6_PopulateBDTSCPrimitiveRestriction p6 = new P_1_5_6_PopulateBDTSCPrimitiveRestriction();
//		p6.run();
		
		P_1_6_1_to_2_PopulateDTFromMetaXSD p7 = new P_1_6_1_to_2_PopulateDTFromMetaXSD();
//		p7.run();

		/*this part is not used at all
		P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD p9 = new P_1_6_3_to_6_PopulateSCInDTSCFromMetaXSD();
		p9.run();*/
		
		P_1_7_PopulateQBDTInDT p13 = new P_1_7_PopulateQBDTInDT();
//		p13.run();
		
		P_1_8_PopulateAccAsccpBccAscc p14 = new P_1_8_PopulateAccAsccpBccAscc();
//		p14.run();
		
//		p1.validate_P_1_3_PopulateAgencyIDList();
//		p2.validate_P_1_4_PopulateCodeList();
//		p3.validate_P_1_5_1_to_2_PopulateBDTsInDT();
		
	}
}
