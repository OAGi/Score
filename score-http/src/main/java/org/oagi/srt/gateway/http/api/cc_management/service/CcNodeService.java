package org.oagi.srt.gateway.http.api.cc_management.service;

import org.jooq.Record1;
import org.jooq.types.ULong;
import org.oagi.srt.entity.jooq.tables.records.AccRecord;
import org.oagi.srt.gateway.http.api.cc_management.data.CcEditUpdateRequest;
import org.oagi.srt.gateway.http.api.cc_management.data.CcEditUpdateResponse;
import org.oagi.srt.gateway.http.api.cc_management.data.node.*;
import org.oagi.srt.gateway.http.api.cc_management.repository.CcNodeRepository;
import org.oagi.srt.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CcNodeService {

    @Autowired
    private CcNodeRepository repository;

    @Autowired
    private SessionService sessionService;

    public CcAccNode getAccNode(User user, long accId, Long releaseId) {
        return repository.getAccNodeByAccId(accId, releaseId);
    }

    public CcAsccpNode getAsccpNode(User user, long asccpId, Long releaseId) {
        return repository.getAsccpNodeByAsccpId(asccpId, releaseId);
    }

    public CcBccpNode getBccpNode(User user, long bccpId, Long releaseId) {
        return repository.getBccpNodeByBccpId(bccpId, releaseId);
    }

    public List<? extends CcNode> getDescendants(User user, CcAccNode accNode) {
        return repository.getDescendants(user, accNode);
    }

    public List<? extends CcNode> getDescendants(User user, CcAsccpNode asccpNode) {
        return repository.getDescendants(user, asccpNode);
    }

    public List<? extends CcNode> getDescendants(User user, CcBccpNode bccpNode) {
        return repository.getDescendants(user, bccpNode);
    }

    public CcAccNodeDetail getAccNodeDetail(User user, CcAccNode accNode) {
        return repository.getAccNodeDetail(user, accNode);
    }

    public CcAsccpNodeDetail getAsccpNodeDetail(User user, CcAsccpNode asccpNode) {
        return repository.getAsccpNodeDetail(user, asccpNode);
    }

    public CcBccpNodeDetail getBccpNodeDetail(User user, CcBccpNode bccpNode) {
        return repository.getBccpNodeDetail(user, bccpNode);
    }

    public CcBdtScNodeDetail getBdtScNodeDetail(User user, CcBdtScNode bdtScNode) {
        return repository.getBdtScNodeDetail(user, bdtScNode);
    }

    public CcAsccpNodeDetail.Asccp getAsccp(long asccpId) {
        return repository.getAsccp(asccpId);
    }

    @Transactional
    public CcAccNode createAcc(User user) {
        long userId = sessionService.userId(user);
        AccRecord accRecord = repository.createAcc(userId);
        return getAccNode(user, accRecord.getAccId().longValue(), 0L);
    }

    @Transactional
    public void updateAcc(User user, CcAccNode ccAccNode) {
        repository.updateAcc(user, ccAccNode);
    }

    @Transactional
    public void updateAsccp(User user, CcAsccpNodeDetail.Asccp asccpNodeDetail, long id) {
        repository.updateAsccp(user, asccpNodeDetail, id);
    }

    @Transactional
    public void appendAscc(User user, long accId, long releaseId, long asccId) {
        repository.createAscc(user, accId, releaseId, asccId);
    }

    @Transactional
    public void discardAscc(User user, long extensionId, Long releaseId, long accId) {
        // repository method discard specific id
    }

    @Transactional
    public Record1<ULong> getLastAsccp() {
        return repository.getLastAsccpId();
    }

    @Transactional
    public Record1<ULong> getLastBccp() {
        return repository.getLastBccpId();
    }

    @Transactional
    public void createAsccp(User user, CcAsccpNode ccAsccpNode) {
        repository.createAsccp(user, ccAsccpNode);
    }

    @Transactional
    public CcEditUpdateResponse updateDetails(User user, CcEditUpdateRequest request) {
        long accId = request.getAccId();

        CcEditUpdateResponse response = new CcEditUpdateResponse();

        return response;
    }

}

