package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpliftBieRequest;
import org.oagi.score.gateway.http.api.bie_management.controller.payload.UpliftValidationRequest;
import org.oagi.score.gateway.http.api.bie_management.model.BieUpliftingMapping;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.release_management.model.ReleaseId;
import org.oagi.score.gateway.http.api.cc_management.service.CcMatchingService;

import java.math.BigInteger;
import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BieUpliftingRequestValidationTest {

    private final BieUpliftingService service = new BieUpliftingService(new CcMatchingService());

    @Test
    void createRejectsMissingSourceAndTarget() {
        assertThatThrownBy(() -> service.upliftBie(null, new UpliftBieRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source BIE");
    }

    @Test
    void validationRejectsNullMappingList() {
        UpliftValidationRequest request = validValidationRequest();
        request.setMappingList(null);

        assertThatThrownBy(() -> service.validateBieUplifting(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mapping list");
    }

    @Test
    void validationRejectsNullBieType() {
        UpliftValidationRequest request = validValidationRequest();
        BieUpliftingMapping mapping = new BieUpliftingMapping();
        mapping.setSourcePath("ASCCP-1>ACC-2>BCC-3");
        request.setMappingList(List.of(mapping));

        assertThatThrownBy(() -> service.validateBieUplifting(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BIE type");
    }

    @Test
    void validationRejectsUnsupportedBieType() {
        UpliftValidationRequest request = validValidationRequest();
        BieUpliftingMapping mapping = new BieUpliftingMapping();
        mapping.setBieId(BigInteger.valueOf(7));
        mapping.setBieType("UNKNOWN");
        request.setMappingList(List.of(mapping));

        assertThatThrownBy(() -> service.validateBieUplifting(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported BIE mapping type");
    }

    @Test
    void validationRejectsNullMappingEntry() {
        UpliftValidationRequest request = validValidationRequest();
        request.setMappingList(Arrays.asList((BieUpliftingMapping) null));

        assertThatThrownBy(() -> service.validateBieUplifting(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BIE type");
    }

    @Test
    void validationRejectsMappedTargetWithoutManifestId() {
        UpliftValidationRequest request = validValidationRequest();
        BieUpliftingMapping mapping = new BieUpliftingMapping();
        mapping.setBieId(BigInteger.valueOf(7));
        mapping.setBieType("BBIE");
        mapping.setSourcePath("ASCCP-1>ACC-2>BCC-3");
        mapping.setTargetPath("ASCCP-10>ACC-20>BCC-30");
        request.setMappingList(List.of(mapping));

        assertThatThrownBy(() -> service.validateBieUplifting(null, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("target manifest ID");
    }

    @Test
    void analysisRejectsMissingTargetRelease() {
        assertThatThrownBy(() -> service.analysisBieUplifting(null,
                new TopLevelAsbiepId(BigInteger.ONE), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("target release");
    }

    @Test
    void analysisRejectsMissingSourceBie() {
        assertThatThrownBy(() -> service.analysisBieUplifting(null, null,
                new ReleaseId(BigInteger.TWO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source BIE");
    }

    @Test
    void acceptsRootAbieWithoutTargetManifestId() {
        BieUpliftingMapping root = new BieUpliftingMapping();
        root.setBieType("ABIE");
        root.setSourcePath("ASCCP-1>ACC-2");
        root.setTargetPath("ASCCP-10>ACC-20");
        org.assertj.core.api.Assertions.assertThatCode(() ->
                BieUpliftingService.validateTargetManifestIds(List.of(root))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ASBIE", "BBIE", "BBIE_SC"})
    void rejectsMissingAssociationManifestBesideRoot(String type) {
        BieUpliftingMapping root = new BieUpliftingMapping();
        root.setBieType("ABIE");
        root.setTargetPath("ASCCP-10>ACC-20");
        BieUpliftingMapping association = new BieUpliftingMapping();
        association.setBieType(type);
        association.setTargetPath("ASCCP-10>ACC-20>" +
                (type.equals("ASBIE") ? "ASCC-30" : type.equals("BBIE") ? "BCC-30" : "BCC-30>DT-40>DT_SC-50"));
        assertThatThrownBy(() -> BieUpliftingService.validateTargetManifestIds(List.of(root, association)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("target manifest ID");
        association.setTargetManifestId(BigInteger.valueOf(30));
        org.assertj.core.api.Assertions.assertThatCode(() ->
                BieUpliftingService.validateTargetManifestIds(List.of(root, association))).doesNotThrowAnyException();
    }

    private static UpliftValidationRequest validValidationRequest() {
        UpliftValidationRequest request = new UpliftValidationRequest();
        request.setTopLevelAsbiepId(new TopLevelAsbiepId(BigInteger.ONE));
        request.setTargetReleaseId(new ReleaseId(BigInteger.TWO));
        request.setMappingList(List.of());
        return request;
    }
}
