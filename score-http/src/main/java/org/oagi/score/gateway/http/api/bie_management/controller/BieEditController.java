package org.oagi.score.gateway.http.api.bie_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oagi.score.data.BieState;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.*;
import org.oagi.score.gateway.http.api.bie_management.data.bie_edit.tree.*;
import org.oagi.score.gateway.http.api.bie_management.service.BieEditService;
import org.oagi.score.gateway.http.api.bie_management.service.BieMakeReusableBieService;
import org.oagi.score.gateway.http.api.common.data.AccessPrivilege;
import org.oagi.score.gateway.http.api.context_management.service.BusinessContextService;
import org.oagi.score.gateway.http.configuration.security.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.*;

import static org.oagi.score.gateway.http.api.common.data.AccessPrivilege.*;

@RestController
public class BieEditController {

    @Autowired
    private BieEditService service;

    @Autowired
    private BieMakeReusableBieService makeReusableBieService;

    @Autowired
    private BusinessContextService businessContextService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/profile_bie/node/root/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditNode getRootNode(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                   @PathVariable("id") long topLevelAsbiepId) {
        BieEditAbieNode rootNode = service.getRootNode(user, topLevelAsbiepId);
        BieState state = BieState.valueOf((Integer) rootNode.getTopLevelAsbiepState());
        rootNode.setTopLevelAsbiepState(state.toString());

        long userId = sessionService.userId(user);
        AccessPrivilege accessPrivilege = Prohibited;
        switch (state) {
            case Initiating:
                accessPrivilege = Unprepared;
                break;

            case Editing:
                if (userId == rootNode.getOwnerUserId()) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = Prohibited;
                }
                break;

            case Candidate:
                if (userId == rootNode.getOwnerUserId()) {
                    accessPrivilege = CanEdit;
                } else {
                    accessPrivilege = CanView;
                }

                break;

            case Published:
                accessPrivilege = CanView;
                break;

        }

        rootNode.setAccess(accessPrivilege.name());

        return rootNode;
    }

    @RequestMapping(value = "/profile_bie/node/root/{id}/state", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateState(@AuthenticationPrincipal AuthenticatedPrincipal user,
                            @PathVariable("id") long topLevelAsbiepId,
                            @RequestBody Map<String, Object> body) {
        BieState state = BieState.valueOf((String) body.get("state"));
        service.updateState(user, topLevelAsbiepId, state);
    }

    @RequestMapping(value = "/profile_bie/node/root/bcc/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BccForBie getBcc(@AuthenticationPrincipal AuthenticatedPrincipal user,
                            @PathVariable("id") long bccId) {
        return service.getBcc(user, bccId);
    }

    @RequestMapping(value = "/profile_bie/node/children/abie", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieEditNode> getAbieChildren(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @RequestParam("data") String data,
                                             @RequestParam(value = "hideUnused", required = false) Boolean hideUnused) {
        BieEditAbieNode abieNode = convertValue(data, BieEditAbieNode.class);
        return service.getDescendants(user, abieNode, hideUnused != null && hideUnused);
    }

    @RequestMapping(value = "/profile_bie/node/detail/abie", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditNodeDetail getAbieDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                           @RequestParam("data") String data) {
        BieEditAbieNode abieNode = convertValue(data, BieEditAbieNode.class);
        return service.getDetail(user, abieNode);
    }

    @RequestMapping(value = "/profile_bie/node/children/asbiep", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieEditNode> getAsbiepChildren(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @RequestParam("data") String data,
                                               @RequestParam(value = "hideUnused", required = false) Boolean hideUnused) {
        BieEditAsbiepNode asbiepNode = convertValue(data, BieEditAsbiepNode.class);
        return service.getDescendants(user, asbiepNode, hideUnused != null && hideUnused);
    }

    @RequestMapping(value = "/profile_bie/node/detail/asbiep", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditNodeDetail getAsbiepDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @RequestParam("data") String data) {
        BieEditAsbiepNode asbiepNode = convertValue(data, BieEditAsbiepNode.class);
        return service.getDetail(user, asbiepNode);
    }

    @RequestMapping(value = "/profile_bie/node/children/bbiep", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BieEditNode> getBbiepChildren(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                              @RequestParam("data") String data,
                                              @RequestParam(value = "hideUnused", required = false) Boolean hideUnused) {
        BieEditBbiepNode bbiepNode = convertValue(data, BieEditBbiepNode.class);
        return service.getDescendants(user, bbiepNode, hideUnused != null && hideUnused);
    }

    @RequestMapping(value = "/profile_bie/node/detail/bbiep", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditNodeDetail getBbiepDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                            @RequestParam("data") String data) {
        BieEditBbiepNode bbiepNode = convertValue(data, BieEditBbiepNode.class);
        return service.getDetail(user, bbiepNode);
    }

    @RequestMapping(value = "/profile_bie/node/detail/bbie_sc", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditNodeDetail getBbieScDetail(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                             @RequestParam("data") String data) {
        BieEditBbieScNode bbieScNode = convertValue(data, BieEditBbieScNode.class);
        return service.getDetail(user, bbieScNode);
    }

    private <T> T convertValue(String data, Class<T> clazz) {
        Map<String, Object> params = new HashMap();
        Arrays.stream(new String(Base64.getDecoder().decode(data)).split("&")).forEach(e -> {
            String[] keyValue = e.split("=");
            if (keyValue.length > 1) {
                params.put(keyValue[0], keyValue[1]);
            } else {
                params.put(keyValue[0], "");
            }

        });
        return objectMapper.convertValue(params, clazz);
    }

    @RequestMapping(value = "/profile_bie/node/detail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BieEditUpdateResponse updateDetails(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                               @RequestBody BieEditUpdateRequest request) {

        return service.updateDetails(user, request);
    }

    @RequestMapping(value = "/profile_bie/node/extension/local", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateExtensionResponse createLocalAbieExtension(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                            @RequestBody BieEditAsbiepNode extensionNode) {
        CreateExtensionResponse response = service.createLocalAbieExtension(user, extensionNode);
        return response;
    }

    @RequestMapping(value = "/profile_bie/node/extension/global", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateExtensionResponse createGlobalAbieExtension(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                                             @RequestBody BieEditAsbiepNode extensionNode) {
        CreateExtensionResponse response = service.createGlobalAbieExtension(user, extensionNode);
        return response;
    }

    /* Reuse BIE */

    @RequestMapping(value = "/profile_bie/{topLevelAsbiepId}/asbie/{asbieId}/reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void reuseBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                         @PathVariable("topLevelAsbiepId") BigInteger topLevelAsbiepId,
                         @PathVariable("asbieId") BigInteger asbieId,
                         @RequestBody ReuseBIERequest reuseBIERequest) {

        reuseBIERequest.setTopLevelAsbiepId(topLevelAsbiepId);
        reuseBIERequest.setAsbieId(asbieId);
        service.reuseBIE(user, reuseBIERequest);
    }

    @RequestMapping(value = "/profile_bie/node/asbie/{asbieId}/remove_reuse",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeReusedBIE(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                @PathVariable("asbieId") BigInteger asbieId) {

        RemoveReusedBIERequest removeReusedBIERequest = new RemoveReusedBIERequest();
        removeReusedBIERequest.setAsbieId(asbieId);
        service.removeReusedBIE(user, removeReusedBIERequest);
    }

    @RequestMapping(value = "/profile_bie/node/asbie/{asbieId}/make_bie_reusable",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void makeBieReusable(@AuthenticationPrincipal AuthenticatedPrincipal user,
                                @PathVariable("asbieId") BigInteger asbieId) {

        MakeReusableBieRequest request = new MakeReusableBieRequest();
        request.setAsbieId(asbieId);

        makeReusableBieService.makeReusableBie(user, request);
    }

}
