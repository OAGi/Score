package org.oagi.score.gateway.http.api.oas_management.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAPIGenerateFileNameTest {

    @Test
    void replacesCharactersThatAreInvalidInAFileName() {
        assertEquals("bot-scan oas A ___ _q_ é", OpenAPIGenerateService.toFileNamePart("bot-scan oas A <&> \"q\" é"));
        assertEquals("a_b_c_d_e_f_g_h", OpenAPIGenerateService.toFileNamePart("a:b/c\\d?e*f|g<h"));
    }

    @Test
    void keepsAPlainTitleAndVersion() {
        assertEquals("My API_v2", OpenAPIGenerateService.toFileNamePart("My API_v2"));
        assertEquals("1.0.3", OpenAPIGenerateService.toFileNamePart("1.0.3"));
    }

    @Test
    void cannotLeaveTheTempDirectory() {
        assertEquals(".._.._etc", OpenAPIGenerateService.toFileNamePart("../../etc"));
    }

    @Test
    void fallsBackWhenNothingIsLeft() {
        assertEquals("openapi", OpenAPIGenerateService.toFileNamePart(null));
        assertEquals("openapi", OpenAPIGenerateService.toFileNamePart("  "));
        assertEquals("openapi", OpenAPIGenerateService.toFileNamePart(".."));
    }
}
