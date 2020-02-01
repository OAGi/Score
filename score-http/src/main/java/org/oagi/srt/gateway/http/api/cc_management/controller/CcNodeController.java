package org.oagi.srt.gateway.http.api.cc_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.srt.gateway.http.api.cc_management.data.CcActionRequest;
import org.oagi.srt.gateway.http.api.cc_management.data.CcEditUpdateRequest;
import org.oagi.srt.gateway.http.api.cc_management.data.CcEditUpdateResponse;
import org.oagi.srt.gateway.http.api.cc_management.data.node.*;
import org.oagi.srt.gateway.http.api.cc_management.service.CcNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class CcNodeController {

    @Autowired
    private CcNodeService service;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/core_component/node/{type}/{releaseId:[\\d]+}/{id:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNode getCcNode(@AuthenticationPrincipal User user,
                            @PathVariable("type") String type,
                            @PathVariable("releaseId") long releaseId,
                            @PathVariable("id") long ccId) {
        switch (type) {
            case "acc":
                return getAccNode(user, ccId, (releaseId == 0L) ? null : releaseId);
            case "asccp":
                return getAsccpNode(user, ccId, (releaseId == 0L) ? null : releaseId);
            case "bccp":
                return getBccpNode(user, ccId, (releaseId == 0L) ? null : releaseId);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private CcAccNode getAccNode(User user, long accId, Long releaseId) {
        return service.getAccNode(user, accId, releaseId);
    }

    private CcAsccpNode getAsccpNode(User user, long asccpId, Long releaseId) {
        return service.getAsccpNode(user, asccpId, releaseId);
    }

    private CcBccpNode getBccpNode(User user, long bccpId, Long releaseId) {
        return service.getBccpNode(user, bccpId, releaseId);
    }

    @RequestMapping(value = "/core_component/acc/{id}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void update(
            @AuthenticationPrincipal User user,
            @PathVariable("id") long id,
            @RequestBody CcAccNode ccAccNode) {
        ccAccNode.setAccId(id);
        service.updateAcc(user, ccAccNode);
    }

    @RequestMapping(value = "/core_component/asccp/{id}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateAsccp(
            @AuthenticationPrincipal User user,
            @PathVariable("id") long id,
            @RequestBody CcAsccpNodeDetail.Asccp ccAsccpNodeDetail) {
        service.updateAsccp(user, ccAsccpNodeDetail, id);
    }

    @RequestMapping(value = "/core_component/asccp_id", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public long getLastAsccp() {
        return service.getLastAsccp().value1().longValue();
    }

    @RequestMapping(value = "/core_component/bccp_id", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public long getLastBccp() {
        return service.getLastBccp().value1().longValue();
    }

    @RequestMapping(value = "/core_component/asccp/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcAsccpNodeDetail.Asccp getAsccp(@PathVariable("id") long id) {
        return service.getAsccp(id);
    }

    @RequestMapping(value = "/core_component/acc", method = RequestMethod.PUT)
    public CcAccNode createAcc(@AuthenticationPrincipal User user) {
        return service.createAcc(user);
    }

    @RequestMapping(value = "/core_component/node/children/{type}/{releaseId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<? extends CcNode> getNodeChildren(@AuthenticationPrincipal User user,
                                                  @PathVariable("type") String type,
                                                  @PathVariable("releaseId") long releaseId,
                                                  @RequestParam("data") String data) {

        switch (type) {
            case "acc":
                CcAccNode accNode = convertValue(data, CcAccNode.class);
                accNode.setReleaseId((releaseId == 0L) ? null : releaseId);
                return service.getDescendants(user, accNode);
            case "asccp":
                CcAsccpNode asccpNode = convertValue(data, CcAsccpNode.class);
                asccpNode.setReleaseId((releaseId == 0L) ? null : releaseId);
                return service.getDescendants(user, asccpNode);
            case "bccp":
                CcBccpNode bccpNode = convertValue(data, CcBccpNode.class);
                bccpNode.setReleaseId((releaseId == 0L) ? null : releaseId);
                return service.getDescendants(user, bccpNode);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private <T> T convertValue(String data, Class<T> clazz) {
        Map<String, Object> params = new HashMap();
        Arrays.stream(new String(Base64.getDecoder().decode(data)).split("&")).forEach(e -> {
            String[] keyValue = e.split("=");
            Object value = keyValue[1];
            if (!"null".equals(value) && value != null) {
                params.put(keyValue[0], value);
            }
        });
        return objectMapper.convertValue(params, clazz);
    }

    @RequestMapping(value = "/core_component/node/detail/{type}/{releaseId:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNodeDetail getNodeDetail(@AuthenticationPrincipal User user,
                                      @PathVariable("type") String type,
                                      @PathVariable("releaseId") long releaseId,
                                      @RequestParam("data") String data) {
        switch (type) {
            case "acc":
                CcAccNode accNode = convertValue(data, CcAccNode.class);
                accNode.setReleaseId((releaseId == 0L) ? null : releaseId);
                return service.getAccNodeDetail(user, accNode);
            case "asccp":
                CcAsccpNode asccpNode = convertValue(data, CcAsccpNode.class);
                asccpNode.setReleaseId((releaseId == 0L) ? null : releaseId);
                return service.getAsccpNodeDetail(user, asccpNode);
            case "bccp":
                CcBccpNode bccpNode = convertValue(data, CcBccpNode.class);
                bccpNode.setReleaseId((releaseId == 0L) ? null : releaseId);
                return service.getBccpNodeDetail(user, bccpNode);
            case "bdt_sc":
                CcBdtScNode bdtScNode = convertValue(data, CcBdtScNode.class);
                bdtScNode.setReleaseId((releaseId == 0L) ? null : releaseId);
                return service.getBdtScNodeDetail(user, bdtScNode);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @RequestMapping(value = "/core_component/node/detail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcEditUpdateResponse updateDetails(@AuthenticationPrincipal User user,
                                              @RequestBody CcEditUpdateRequest request) {
        return service.updateDetails(user, request);
    }

    @RequestMapping(value = "/core_component/ascc/{releaseId:[\\d]+}/{id:[\\d]+}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity doAsccAction(@AuthenticationPrincipal User user,
                                       @PathVariable("releaseId") long releaseId,
                                       @PathVariable("id") long accId,
                                       @RequestBody CcActionRequest actionRequest) {

        switch (actionRequest.getAction()) {
            case "append":
                service.appendAscc(user, accId, releaseId, actionRequest.getId());
                break;

            case "discard":
                service.discardAscc(user, accId, releaseId, actionRequest.getId());
                break;
        }

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/core_component/asccp/create", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createAsccp(
            @AuthenticationPrincipal User user,
            @RequestBody CcAsccpNode ccAsccpNode) {
        service.createAsccp(user, ccAsccpNode);
        return ResponseEntity.noContent().build();
    }
}