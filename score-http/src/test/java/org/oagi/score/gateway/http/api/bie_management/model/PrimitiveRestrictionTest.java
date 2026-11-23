package org.oagi.score.gateway.http.api.bie_management.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.agency_id_management.model.AgencyIdListManifestId;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.api.xbt_management.model.XbtManifestId;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Data Type default value domain may be a primitive, a code list, or an agency ID list (Issue #1879).
 */
class PrimitiveRestrictionTest {

    private static final XbtManifestId XBT = XbtManifestId.from(BigInteger.ONE);
    private static final CodeListManifestId CODE_LIST = CodeListManifestId.from(BigInteger.TWO);
    private static final AgencyIdListManifestId AGENCY_ID_LIST = AgencyIdListManifestId.from(BigInteger.TEN);

    @Test
    void primitiveDefaultIsNeitherEmptyNorCodeValued() {
        PrimitiveRestriction r = new PrimitiveRestriction(XBT, null, null);
        assertFalse(r.isEmpty());
        assertFalse(r.isCodeListOrAgencyIdList());
    }

    @Test
    void codeListDefaultIsCodeValued() {
        PrimitiveRestriction r = new PrimitiveRestriction(null, CODE_LIST, null);
        assertFalse(r.isEmpty());
        assertTrue(r.isCodeListOrAgencyIdList());
    }

    @Test
    void agencyIdListDefaultIsCodeValued() {
        PrimitiveRestriction r = new PrimitiveRestriction(null, null, AGENCY_ID_LIST);
        assertFalse(r.isEmpty());
        assertTrue(r.isCodeListOrAgencyIdList());
    }

    @Test
    void nothingAssignedIsEmpty() {
        PrimitiveRestriction r = new PrimitiveRestriction(null, null, null);
        assertTrue(r.isEmpty());
        assertFalse(r.isCodeListOrAgencyIdList());
    }
}
