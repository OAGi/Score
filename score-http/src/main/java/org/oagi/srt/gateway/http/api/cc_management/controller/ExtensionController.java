package org.oagi.srt.gateway.http.api.cc_management.controller;

import org.oagi.srt.gateway.http.api.cc_management.data.*;
import org.oagi.srt.gateway.http.api.cc_management.data.node.CcNode;
import org.oagi.srt.gateway.http.api.cc_management.service.ExtensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ExtensionController {

    @Autowired
    private ExtensionService service;

    @RequestMapping(value = "/core_component/node/extension/{releaseId:[\\d]+}/{id:[\\d]+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNode getCcNode(@AuthenticationPrincipal User user,
                            @PathVariable("releaseId") long releaseId,
                            @PathVariable("id") long extensionId) {
        return service.getExtensionNode(user, extensionId, releaseId);
    }

    @RequestMapping(value = "/core_component/extension/{releaseId:[\\d]+}/{id:[\\d]+}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity doExtensionAction(@AuthenticationPrincipal User user,
                                            @PathVariable("releaseId") long releaseId,
                                            @PathVariable("id") long extensionId,
                                            @RequestBody CcActionRequest actionRequest) {

        switch (actionRequest.getAction()) {
            case "append":
                switch (actionRequest.getType()) {
                    case "asccp":
                        service.appendAsccp(user, extensionId, releaseId, actionRequest.getId());
                        break;

                    case "bccp":
                        service.appendBccp(user, extensionId, releaseId, actionRequest.getId());
                        break;
                }

                break;

            case "discard":
                switch (actionRequest.getType()) {
                    case "ascc":
                        service.discardAscc(user, extensionId, releaseId, actionRequest.getId());
                        break;

                    case "bcc":
                        service.discardBcc(user, extensionId, releaseId, actionRequest.getId());
                        break;
                }

                break;
        }

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/core_component/extension/{releaseId:[\\d]+}/{id:[\\d]+}/state",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateExtensionState(@AuthenticationPrincipal User user,
                                               @PathVariable("releaseId") long releaseId,
                                               @PathVariable("id") long extensionId,
                                               @RequestBody Map<String, Object> body) {
        CcState state = CcState.valueOf((String) body.get("state"));
        service.updateState(user, extensionId, releaseId, state);

        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/core_component/extension/{releaseId:[\\d]+}/{id:[\\d]+}/detail",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExtensionUpdateResponse updateDetails(@AuthenticationPrincipal User user,
                                                 @PathVariable("releaseId") long releaseId,
                                                 @PathVariable("id") long extensionId,
                                                 @RequestBody ExtensionUpdateRequest request) {
        request.setExtensionId(extensionId);
        request.setReleaseId(releaseId);
        return service.updateDetails(user, request);
    }

    @RequestMapping(value = "/core_component/extension/{releaseId:[\\d]+}/{type}/{id:[\\d]+}/reivision",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CcNode getLastCcNode(@AuthenticationPrincipal User user,
                                @PathVariable("releaseId") long releaseId,
                                @PathVariable("type") String type,
                                @PathVariable("id") long CcId) {


        return service.getLastRevisionCc(user, type, CcId);
    }
}
