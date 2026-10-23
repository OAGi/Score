package org.oagi.score.gateway.http.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DenUtilsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void noQualifierMeansNoPrefix(String qualifier) {
        assertThat(DenUtils.qualifiedTerm(qualifier, "Date Time")).isEqualTo("Date Time");
        assertThat(DenUtils.dtDen(qualifier, "Date Time")).isEqualTo("Date Time. Type");
        assertThat(DenUtils.bccpDen("Date Time", qualifier, "Date Time")).isEqualTo("Date Time. Date Time");
    }

    @Test
    void qualifierIsPutInFrontOfTheTerm() {
        assertThat(DenUtils.qualifiedTerm("Open", "Number")).isEqualTo("Open_ Number");
        assertThat(DenUtils.dtDen("Open", "Number")).isEqualTo("Open_ Number. Type");
        assertThat(DenUtils.bccpDen("Frequency Number", "Open", "Number")).isEqualTo("Frequency Number. Open_ Number");
    }

    @Test
    void anEmptyQualifierNeverLeavesAnUnderscoreBehind() {
        for (String qualifier : new String[]{null, "", " "}) {
            assertThat(DenUtils.dtDen(qualifier, "Duration")).doesNotContain("_");
            assertThat(DenUtils.bccpDen("Forecast Horizon", qualifier, "Duration")).doesNotContain("_");
        }
    }

    @Test
    void onlyTheTypeEndingIsRemoved() {
        assertThat(DenUtils.withoutTypeSuffix("Date Time. Type")).isEqualTo("Date Time");
        assertThat(DenUtils.withoutTypeSuffix("Open_ Number. Type")).isEqualTo("Open_ Number");
        assertThat(DenUtils.withoutTypeSuffix("Number")).isEqualTo("Number");
        assertThat(DenUtils.withoutTypeSuffix(null)).isNull();
    }

    /**
     * The regular expression ". Type" matches any character in front of " Type", so it used to cut the last letter of the
     * word before it; these are the DENs that came out of it ("Line Ite_ Code", "Zon_ Code", ...).
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "Line Item Type_ Code. Type",
            "Purchase Order Type Code. Type",
            "Zone Type_ Code. Type",
            "Shipment Stop Type_ Code. Type",
            "Allowance Charge Type_ Code. Type"
    })
    void aWordEndingBeforeTypeKeepsItsLastLetter(String dtDen) {
        String withoutSuffix = DenUtils.withoutTypeSuffix(dtDen);

        assertThat(withoutSuffix).isEqualTo(dtDen.substring(0, dtDen.length() - ". Type".length()));
        assertThat(withoutSuffix).contains("Type");
        assertThat(dtDen.replaceAll(". Type", "")).isNotEqualTo(withoutSuffix);
    }

    @Test
    void theDenOfABccpIsBuiltFromItsDataTypeWithoutCuttingIt() {
        String dtDen = DenUtils.dtDen("Line Item Type", "Code");

        assertThat(dtDen).isEqualTo("Line Item Type_ Code. Type");
        assertThat("Line Item Type" + ". " + DenUtils.withoutTypeSuffix(dtDen))
                .isEqualTo("Line Item Type. Line Item Type_ Code");
    }


    @Test
    void bccpDenFromTheDtDenDropsTheTypeEnding() {
        assertThat(DenUtils.bccpDen("Frequency Number", "Open_ Number. Type")).isEqualTo("Frequency Number. Open_ Number");
    }

    @Test
    void componentDens() {
        assertThat(DenUtils.accDen("Invoice")).isEqualTo("Invoice. Details");
        assertThat(DenUtils.asccpDen("Party", "Party Details")).isEqualTo("Party. Party Details");
        assertThat(DenUtils.asccDen("Invoice", "Party. Party Details")).isEqualTo("Invoice. Party. Party Details");
        assertThat(DenUtils.bccDen("Invoice", "Amount. Amount")).isEqualTo("Invoice. Amount. Amount");
    }

    @Test
    void asccpDenIsNotCutAtTheLongestTerms() {
        String den = DenUtils.asccpDen("P".repeat(100), "O".repeat(100));
        assertThat(den).hasSize(202).startsWith("P").endsWith("O");
    }

    @Test
    void dtScUniquenessDenDropsTheRepresentationTermFromThePropertyTerm() {
        assertThat(DenUtils.dtScUniquenessDen("Amount. Type", "Currency Code", "Code")).isEqualTo("Amount. Type. Currency. Code");
        assertThat(DenUtils.dtScUniquenessDen("Amount. Type", null, "Code")).isEqualTo("Amount. Type. Code");
    }
}
