package org.oagi.score.gateway.http.api.cc_management.model.dt;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #808 and Issue #1879: the primitive that a BBIE starts with.
 */
class DefaultAwdPriRuleTest {

    private static DtAwdPriSummaryRecord pri(String xbtName, boolean isDefault) {
        XbtManifestId xbt = (xbtName != null) ? XbtManifestId.from(BigInteger.valueOf(Math.abs(xbtName.hashCode()) + 1L)) : null;
        return new DtAwdPriSummaryRecord(null, null, null, null, xbt, xbtName, null, null, null, null, isDefault);
    }

    private static DtAwdPriSummaryRecord codeListPri(boolean isDefault) {
        return new DtAwdPriSummaryRecord(null, null, null, null, null, null,
                CodeListManifestId.from(BigInteger.TWO), "a code list", null, null, isDefault);
    }

    private static DtAwdPriSummaryRecord agencyIdListPri(boolean isDefault) {
        return new DtAwdPriSummaryRecord(null, null, null, null, null, null,
                null, null, AgencyIdListManifestId.from(BigInteger.TEN), "an agency id list", isDefault);
    }

    @Test
    void dateTimeDateAndTimeStartWithThePrimitiveOfTheSameNameNotWithTheDefault() {
        List<DtAwdPriSummaryRecord> list = List.of(
                pri("token", true), pri("date time", false), pri("date", false), pri("time", false));

        assertEquals("date time", DefaultAwdPriRule.select("Date Time", list).xbtName());
        assertEquals("date", DefaultAwdPriRule.select("Date", list).xbtName());
        assertEquals("time", DefaultAwdPriRule.select("Time", list).xbtName());
    }

    @Test
    void aNumericDateStillStartsWithTheDatePrimitiveWhenItIsAllowed() {
        List<DtAwdPriSummaryRecord> list = List.of(pri("int", true), pri("date", false));

        assertEquals("date", DefaultAwdPriRule.select("Date", list).xbtName());
    }

    @Test
    void otherTermsStartWithTheDefault() {
        List<DtAwdPriSummaryRecord> list = List.of(pri("token", false), pri("string", true));

        assertEquals("string", DefaultAwdPriRule.select("Text", list).xbtName());
    }

    @Test
    void aDefaultThatIsACodeListOrAnAgencyIdListIsTheDefault() {
        DtAwdPriSummaryRecord codeList = codeListPri(true);
        DtAwdPriSummaryRecord agencyIdList = agencyIdListPri(true);

        assertEquals(codeList, DefaultAwdPriRule.select("Code", List.of(pri("token", false), codeList)));
        assertEquals(agencyIdList, DefaultAwdPriRule.select("Identifier", List.of(pri("token", false), agencyIdList)));
    }

    @Test
    void aDateTimeThatDoesNotAllowThePrimitiveOfItsNameBreaksTheRule() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> DefaultAwdPriRule.select("Date Time", List.of(pri("token", true))));

        assertTrue(e.getMessage().contains("date time"), e.getMessage());
    }

    @Test
    void aDataTypeWithoutADefaultBreaksTheRule() {
        assertThrows(IllegalStateException.class,
                () -> DefaultAwdPriRule.select("Text", List.of(pri("token", false))));
    }

    @Test
    void aSupplementaryComponentFollowsTheSameRule() {
        List<DtScAwdPriSummaryRecord> list = List.of(
                new DtScAwdPriSummaryRecord(null, null, null, null, XbtManifestId.from(BigInteger.ONE), "token", null, null, null, null, true),
                new DtScAwdPriSummaryRecord(null, null, null, null, XbtManifestId.from(BigInteger.TWO), "date time", null, null, null, null, false));

        assertEquals("date time", DefaultAwdPriRule.selectForSc("Date Time", list).xbtName());
        assertEquals("token", DefaultAwdPriRule.selectForSc("Code", list).xbtName());
    }
}
