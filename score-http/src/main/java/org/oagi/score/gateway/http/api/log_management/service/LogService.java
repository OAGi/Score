package org.oagi.score.gateway.http.api.log_management.service;

import org.oagi.score.gateway.http.api.log_management.controller.payload.LogListRequest;
import org.oagi.score.gateway.http.api.log_management.model.Log;
import org.oagi.score.gateway.http.api.log_management.repository.LogRepository;
import org.oagi.score.gateway.http.common.model.PageResponse;
import org.oagi.score.gateway.http.common.model.ScoreUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
@Transactional(readOnly = true)
public class LogService {

    @Autowired
    private LogRepository repository;

    public PageResponse<Log> getLogByReference(LogListRequest request) {
        return repository.getLogByReference(request);
    }

    public String getSnapshotById(ScoreUser requester, BigInteger logId) {
        return repository.getSnapshotById(requester, logId);
    }
}
