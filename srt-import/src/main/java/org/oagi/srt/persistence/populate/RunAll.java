package org.oagi.srt.persistence.populate;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class RunAll {

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {

            P_1_1_PopulateCommonData p1 = ctx.getBean(P_1_1_PopulateCommonData.class);
            p1.run(ctx);

            PopulateModules populateModules = ctx.getBean(PopulateModules.class);
            populateModules.run(ctx);

            PopulateBlobContents populateBlobContents = ctx.getBean(PopulateBlobContents.class);
            populateBlobContents.run(ctx);

            P_1_2_PopulateCDTandCDTSC p2 = ctx.getBean(P_1_2_PopulateCDTandCDTSC.class);
            p2.run(ctx);

            P_1_3_PopulateAgencyIDList p3 = ctx.getBean(P_1_3_PopulateAgencyIDList.class);
            p3.run(ctx);

            P_1_4_PopulateCodeList p4 = ctx.getBean(P_1_4_PopulateCodeList.class);
            p4.run(ctx);

            P_1_5_1_to_2_PopulateBDTsInDT p5 = ctx.getBean(P_1_5_1_to_2_PopulateBDTsInDT.class);
            p5.run(ctx);

            P_1_5_3_to_5_PopulateSCInDTSC p6 = ctx.getBean(P_1_5_3_to_5_PopulateSCInDTSC.class);
            p6.run(ctx);

            P_1_5_6_PopulateBDTSCPrimitiveRestriction p7 = ctx.getBean(P_1_5_6_PopulateBDTSCPrimitiveRestriction.class);
            p7.run(ctx);

            P_1_6_1_to_2_PopulateDTFromMetaXSD p8 = ctx.getBean(P_1_6_1_to_2_PopulateDTFromMetaXSD.class);
            p8.run(ctx);

            P_1_7_PopulateQBDTInDT p9 = ctx.getBean(P_1_7_PopulateQBDTInDT.class);
            p9.run(ctx);

            P_1_8_1_PopulateAccAsccpBccAscc p10 = ctx.getBean(P_1_8_1_PopulateAccAsccpBccAscc.class);
            p10.run(ctx);
        }
    }
}
