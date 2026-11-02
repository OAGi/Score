package org.oagi.score.gateway.http.api.graph.model;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.cc_management.model.CcDocument;
import org.oagi.score.gateway.http.api.cc_management.model.CcState;
import org.oagi.score.gateway.http.api.cc_management.model.Cardinality;
import org.oagi.score.gateway.http.api.cc_management.model.acc.*;
import org.oagi.score.gateway.http.api.cc_management.model.asccp.*;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.*;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.common.model.Guid;

import java.math.BigInteger;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreComponentGraphContextIdentityTest {

    private final CcDocument ccDocument = mock(CcDocument.class);
    private final CoreComponentGraphContext context = new CoreComponentGraphContext(ccDocument);

    @Test
    void exposesAccIdentityUsedByRoleAccMappings() {
        AccSummaryRecord acc = mock(AccSummaryRecord.class);
        when(acc.accManifestId()).thenReturn(AccManifestId.from("11"));
        when(acc.accId()).thenReturn(AccId.from("101"));
        when(acc.guid()).thenReturn(guid("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        when(acc.state()).thenReturn(CcState.Published);
        when(acc.componentType()).thenReturn(OagisComponentType.Base);
        when(ccDocument.getTagListByAccManifestId(any())).thenReturn(Collections.emptyList());

        Node node = context.toNode(acc);

        assertThat(node.getProperties()).containsEntry("componentId", BigInteger.valueOf(101))
                .containsEntry("guid", guid("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    void exposesDtIdentityUsedByBbieMappings() {
        DtSummaryRecord dt = mock(DtSummaryRecord.class);
        when(dt.dtManifestId()).thenReturn(DtManifestId.from("21"));
        when(dt.dtId()).thenReturn(DtId.from("201"));
        when(dt.guid()).thenReturn(guid("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));
        when(dt.state()).thenReturn(CcState.Published);
        when(ccDocument.getTagListByDtManifestId(any())).thenReturn(Collections.emptyList());

        Node node = context.toNode(dt);

        assertThat(node.getProperties()).containsEntry("componentId", BigInteger.valueOf(201))
                .containsEntry("guid", guid("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));
    }

    @Test
    void exposesDtScIdentityUsedByBbieScMappings() {
        DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
        when(dtSc.dtScManifestId()).thenReturn(DtScManifestId.from("31"));
        when(dtSc.dtScId()).thenReturn(DtScId.from("301"));
        when(dtSc.guid()).thenReturn(guid("cccccccccccccccccccccccccccccccc"));
        when(dtSc.state()).thenReturn(CcState.Published);
        when(dtSc.cardinality()).thenReturn(new Cardinality(0, 1));

        Node node = context.toNode(dtSc);

        assertThat(node.getProperties()).containsEntry("componentId", BigInteger.valueOf(301))
                .containsEntry("guid", guid("cccccccccccccccccccccccccccccccc"));
    }

    @Test
    void exposesAsccpAndBccpIdentityForGraphConsumers() {
        AsccpSummaryRecord asccp = mock(AsccpSummaryRecord.class);
        when(asccp.asccpManifestId()).thenReturn(AsccpManifestId.from("41"));
        when(asccp.asccpId()).thenReturn(AsccpId.from("401"));
        when(asccp.guid()).thenReturn(guid("dddddddddddddddddddddddddddddddd"));
        when(asccp.state()).thenReturn(CcState.Published);

        BccpSummaryRecord bccp = mock(BccpSummaryRecord.class);
        when(bccp.bccpManifestId()).thenReturn(BccpManifestId.from("51"));
        when(bccp.bccpId()).thenReturn(BccpId.from("501"));
        when(bccp.guid()).thenReturn(guid("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"));
        when(bccp.state()).thenReturn(CcState.Published);

        Node asccpNode = context.toNode(asccp);
        Node bccpNode = context.toNode(bccp);

        assertThat(asccpNode.getProperties()).containsEntry("componentId", BigInteger.valueOf(401))
                .containsEntry("guid", guid("dddddddddddddddddddddddddddddddd"));
        assertThat(bccpNode.getProperties()).containsEntry("componentId", BigInteger.valueOf(501))
                .containsEntry("guid", guid("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"));
    }

    private static Guid guid(String value) {
        return new Guid(value);
    }
}
