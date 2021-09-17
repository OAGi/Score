package org.oagi.score.repo.api.module;

import org.oagi.score.repo.api.base.ScoreDataAccessException;
import org.oagi.score.repo.api.module.model.*;

import java.math.BigInteger;
import java.util.List;

public interface ModuleSetReleaseReadRepository {

    GetModuleSetReleaseResponse getModuleSetRelease(
            GetModuleSetReleaseRequest request) throws ScoreDataAccessException;

    GetModuleSetReleaseListResponse getModuleSetReleaseList(
            GetModuleSetReleaseListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignableACCByModuleSetReleaseId(
            GetAssignableCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignedACCByModuleSetReleaseId(
            GetAssignedCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignableASCCPByModuleSetReleaseId(
            GetAssignableCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignedASCCPByModuleSetReleaseId(
            GetAssignedCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignableBCCPByModuleSetReleaseId(
            GetAssignableCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignedBCCPByModuleSetReleaseId(
            GetAssignedCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignableDTByModuleSetReleaseId(
            GetAssignableCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignedDTByModuleSetReleaseId(
            GetAssignedCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignableCodeListByModuleSetReleaseId(
            GetAssignableCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignedCodeListByModuleSetReleaseId(
            GetAssignedCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignableAgencyIdListByModuleSetReleaseId(
            GetAssignableCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignedAgencyIdListByModuleSetReleaseId(
            GetAssignedCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignableXBTByModuleSetReleaseId(
            GetAssignableCCListRequest request) throws ScoreDataAccessException;

    List<AssignableNode> getAssignedXBTByModuleSetReleaseId(
            GetAssignedCCListRequest request) throws ScoreDataAccessException;
}
