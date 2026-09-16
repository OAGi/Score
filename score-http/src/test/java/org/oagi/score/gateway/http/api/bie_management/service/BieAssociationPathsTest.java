package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.acc.AccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.ascc.AsccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.AsccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BieAssociationPathsTest {
    private final CcDocument document = mock(CcDocument.class);
    private final AccSummaryRecord extension = mock(AccSummaryRecord.class);
    private final AsccSummaryRecord association = mock(AsccSummaryRecord.class);
    private final AsccpSummaryRecord property = mock(AsccpSummaryRecord.class);
    private final BccSummaryRecord sibling = mock(BccSummaryRecord.class);
    private final AccManifestId roleId = new AccManifestId(BigInteger.TWO);
    private final AsccpManifestId propertyId = new AsccpManifestId(BigInteger.TWO);

    private void setUpExtension() {
        when(extension.accManifestId()).thenReturn(new AccManifestId(BigInteger.ONE));
        when(document.getAssociations(extension)).thenReturn(List.of(association, sibling));
        when(association.isAscc()).thenReturn(true);
        when(association.asccManifestId()).thenReturn(new AsccManifestId(BigInteger.TWO));
        when(association.toAsccpManifestId()).thenReturn(propertyId);
        when(document.getAsccp(propertyId)).thenReturn(property);
        when(property.asccpManifestId()).thenReturn(propertyId);
        when(property.roleOfAccManifestId()).thenReturn(roleId);
        when(sibling.bccManifestId()).thenReturn(new BccManifestId(BigInteger.TEN));
    }

    @Test
    void skipsDeletedExtensionAccAndPreservesSiblingPath() {
        setUpExtension();
        assertThat(BieAssociationPaths.getAssociationsRegardingBases("ASCCP-1", document, extension))
                .extracting(BieAssociationPaths.Association::getPath)
                .containsExactly("ASCCP-1>ACC-1>BCC-10");
    }

    @Test
    void skipsDeletedAsccpAndPreservesSiblingPath() {
        setUpExtension();
        when(document.getAsccp(propertyId)).thenReturn(null);
        assertThat(BieAssociationPaths.getAssociationsRegardingBases("ASCCP-1", document, extension))
                .extracting(BieAssociationPaths.Association::getPath)
                .containsExactly("ASCCP-1>ACC-1>BCC-10");
    }

    @Test
    void flattensExistingGroupAndRetainsOrdinaryAssociation() {
        setUpExtension();
        AccSummaryRecord role = mock(AccSummaryRecord.class);
        when(document.getAcc(roleId)).thenReturn(role);
        when(role.accManifestId()).thenReturn(roleId);
        when(role.isGroup()).thenReturn(true);
        when(document.getAssociations(role)).thenReturn(List.of(sibling));
        assertThat(BieAssociationPaths.getAssociationsRegardingBases("ASCCP-1", document, extension))
                .extracting(BieAssociationPaths.Association::getPath)
                .containsExactly("ASCCP-1>ACC-1>ASCC-2>ASCCP-2>ACC-2>BCC-10", "ASCCP-1>ACC-1>BCC-10");

        when(role.isGroup()).thenReturn(false);
        assertThat(BieAssociationPaths.getAssociationsRegardingBases("ASCCP-1", document, extension))
                .extracting(BieAssociationPaths.Association::getPath)
                .containsExactly("ASCCP-1>ACC-1>ASCC-2", "ASCCP-1>ACC-1>BCC-10");
    }
}
