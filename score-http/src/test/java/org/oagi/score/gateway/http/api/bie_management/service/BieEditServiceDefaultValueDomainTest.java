package org.oagi.score.gateway.http.api.bie_management.service;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.api.bie_management.model.PrimitiveRestriction;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie.BbieId;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScDetailsRecord;
import org.oagi.score.gateway.http.api.bie_management.model.bbie_sc.BbieScId;
import org.oagi.score.gateway.http.api.bie_management.repository.BbieQueryRepository;
import org.oagi.score.gateway.http.api.bie_management.repository.BbieScQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.model.bcc.BccSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.bccp.BccpSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt.DtManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScAwdPriSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScManifestId;
import org.oagi.score.gateway.http.api.cc_management.model.dt_sc.DtScSummaryRecord;
import org.oagi.score.gateway.http.api.cc_management.repository.BccpQueryRepository;
import org.oagi.score.gateway.http.api.cc_management.repository.DtQueryRepository;
import org.oagi.score.gateway.http.api.code_list_management.model.CodeListManifestId;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.oagi.score.gateway.http.common.repository.jooq.RepositoryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A BBIE or BBIE supplementary component saved without a value domain is read with the default of its Data
 * Type, so the BIE page shows a Code List default instead of an empty Primitive.
 */
class BieEditServiceDefaultValueDomainTest {

    private final CodeListManifestId codeList = new CodeListManifestId(BigInteger.valueOf(7));
    private final ScoreUser requester = mock(ScoreUser.class);
    private final RepositoryFactory repositoryFactory = mock(RepositoryFactory.class);
    private final DtQueryRepository dtQuery = mock(DtQueryRepository.class);
    private final BieEditService service = new BieEditService();

    BieEditServiceDefaultValueDomainTest() {
        ReflectionTestUtils.setField(service, "repositoryFactory", repositoryFactory);
        when(repositoryFactory.dtQueryRepository(requester)).thenReturn(dtQuery);
    }

    private DtAwdPriSummaryRecord defaultCodeList() {
        return new DtAwdPriSummaryRecord(null, null, null, "Token", null, null, codeList, "List", null, null, true);
    }

    private BbieDetailsRecord bbie(PrimitiveRestriction restriction) {
        BccSummaryRecord bcc = mock(BccSummaryRecord.class);
        when(bcc.toBccpManifestId()).thenReturn(new BccpManifestId(BigInteger.ONE));
        return BbieDetailsRecord.builder().basedBcc(bcc).primitiveRestriction(restriction).build();
    }

    private BbieDetailsRecord read(BbieDetailsRecord stored) {
        var bbieQuery = mock(BbieQueryRepository.class);
        BbieId bbieId = new BbieId(BigInteger.TEN);
        when(repositoryFactory.bbieQueryRepository(requester)).thenReturn(bbieQuery);
        when(bbieQuery.getBbieDetails(bbieId)).thenReturn(stored);
        return service.getBbieDetails(requester, bbieId);
    }

    @Test
    void an_empty_bbie_is_read_with_the_default_code_list() {
        var bccpQuery = mock(BccpQueryRepository.class);
        BccpSummaryRecord bccp = mock(BccpSummaryRecord.class);
        DtManifestId dtManifestId = new DtManifestId(BigInteger.TWO);
        when(repositoryFactory.bccpQueryRepository(requester)).thenReturn(bccpQuery);
        when(bccpQuery.getBccpSummary(any())).thenReturn(bccp);
        when(bccp.dtManifestId()).thenReturn(dtManifestId);
        when(dtQuery.getDefaultDtAwdPriSummary(dtManifestId)).thenReturn(defaultCodeList());

        BbieDetailsRecord result = read(bbie(new PrimitiveRestriction(null, null, null)));

        assertEquals(codeList, result.primitiveRestriction().codeListManifestId());
    }

    @Test
    void a_bbie_with_its_own_value_domain_keeps_it() {
        BbieDetailsRecord stored = bbie(new PrimitiveRestriction(null, new CodeListManifestId(BigInteger.valueOf(9)), null));

        assertSame(stored, read(stored));
        verify(dtQuery, never()).getDefaultDtAwdPriSummary(any());
    }

    @Test
    void an_empty_bbie_sc_is_read_with_the_default_code_list() {
        var bbieScQuery = mock(BbieScQueryRepository.class);
        BbieScId bbieScId = new BbieScId(BigInteger.TEN);
        DtScManifestId dtScManifestId = new DtScManifestId(BigInteger.valueOf(3));
        DtScSummaryRecord dtSc = mock(DtScSummaryRecord.class);
        when(dtSc.dtScManifestId()).thenReturn(dtScManifestId);
        BbieScDetailsRecord stored = BbieScDetailsRecord.builder().basedDtSc(dtSc)
                .primitiveRestriction(new PrimitiveRestriction(null, null, null)).build();
        when(repositoryFactory.bbieScQueryRepository(requester)).thenReturn(bbieScQuery);
        when(bbieScQuery.getBbieScDetails(bbieScId)).thenReturn(stored);
        when(dtQuery.getDefaultDtScAwdPriSummary(dtScManifestId)).thenReturn(
                new DtScAwdPriSummaryRecord(null, null, null, "Token", null, null, codeList, "List", null, null, true));

        assertEquals(codeList, service.getBbieScDetails(requester, bbieScId).primitiveRestriction().codeListManifestId());
    }
}
