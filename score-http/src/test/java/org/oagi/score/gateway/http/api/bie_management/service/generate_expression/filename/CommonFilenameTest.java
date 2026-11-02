package org.oagi.score.gateway.http.api.bie_management.service.generate_expression.filename;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.bie_package.BiePackageSummaryRecord;
import org.oagi.score.gateway.http.api.bie_management.model.expression.GenerateExpressionOption;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonFilenameTest {

    private static BiePackageSummaryRecord biePackage(String name, String versionId) {
        return new BiePackageSummaryRecord(null, null, null, name, versionId, null, null, null,
                null, null, null, null);
    }

    @Test
    void aCommonFileOfAPackageIsNamedAfterThePackage() {
        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setBiePackage(biePackage("New BIE Package", "v1.0"));

        assertEquals("NewBIEPackage-v1.0_CommonCodeList",
                ExpressionBasedFilenameStrategy.from(null, option).buildCommonFilename("CommonCodeList"));
    }

    @Test
    void thePackageNameIsMadeSafeForAFilename() {
        GenerateExpressionOption option = new GenerateExpressionOption();
        option.setBiePackage(biePackage("A/B:C", null));

        assertEquals("ABC_CommonDataType",
                ExpressionBasedFilenameStrategy.from(null, option).buildCommonFilename("CommonDataType"));
    }

    @Test
    void aCommonFileOutsideAPackageKeepsItsName() {
        assertEquals("CommonAgencyIdList", ExpressionBasedFilenameStrategy
                .from(null, new GenerateExpressionOption()).buildCommonFilename("CommonAgencyIdList"));
    }
}
