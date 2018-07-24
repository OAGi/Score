package org.oagi.srt.persistence.populate;

import org.oagi.srt.ImportApplication;
import org.oagi.srt.persistence.populate.script.DataImportScriptRunner;
import org.oagi.srt.persistence.populate.script.DataImportScriptRunnerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.persistence.EntityManager;

public class RunAll {

    public static void main(String args[]) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ImportApplication.class, args)) {
            DataImportScriptRunner importScriptRunner =
                    DataImportScriptRunnerFactory.loadDataImportScriptRunner(ctx);
            importScriptRunner.printHeader();
            importScriptRunner.printSettings();

            P_1_1_PopulateCommonData p1 = ctx.getBean(P_1_1_PopulateCommonData.class);
            p1.run(ctx);

            PopulateBlobContents populateBlobContents = ctx.getBean(PopulateBlobContents.class);
            populateBlobContents.run(ctx);

            P_1_2_PopulateCDTandCDTSC p2 = ctx.getBean(P_1_2_PopulateCDTandCDTSC.class);
            p2.run(ctx);

            P_1_3_PopulateAgencyIDList p3 = ctx.getBean(P_1_3_PopulateAgencyIDList.class);
            p3.run(ctx);

            P_1_4_PopulateCodeList p4 = ctx.getBean(P_1_4_PopulateCodeList.class);
            p4.run(ctx);

            P_1_5_1_PopulateDefaultAndUnqualifiedBDT p5 = ctx.getBean(P_1_5_1_PopulateDefaultAndUnqualifiedBDT.class);
            p5.run(ctx);

            P_1_6_PopulateDTFromMeta p6 = ctx.getBean(P_1_6_PopulateDTFromMeta.class);
            p6.run(ctx);

            P_1_7_PopulateQBDTInDT p7 = ctx.getBean(P_1_7_PopulateQBDTInDT.class);
            p7.run(ctx);

            P_1_8_1_PopulateAccAsccpBccAscc p8 = ctx.getBean(P_1_8_1_PopulateAccAsccpBccAscc.class);
            p8.run(ctx);

            P_1_8_2_PopulateOAGISType p8_2 = ctx.getBean(P_1_8_2_PopulateOAGISType.class);
            p8_2.run(ctx);

            EntityManager entityManager = ctx.getBean(EntityManager.class);
            importScriptRunner.printResetSequences(entityManager);
        }
    }
}
