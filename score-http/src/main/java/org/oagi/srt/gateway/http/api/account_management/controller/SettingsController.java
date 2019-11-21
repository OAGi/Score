package org.oagi.srt.gateway.http.api.account_management.controller;

import org.oagi.srt.gateway.http.api.account_management.data.UpdatePasswordRequest;
import org.oagi.srt.gateway.http.api.account_management.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SettingsController {

    @Autowired
    private SettingsService service;

    @RequestMapping(value = "/settings/password", method = RequestMethod.POST)
    public ResponseEntity updatePassword(@AuthenticationPrincipal User user,
                                         @RequestBody UpdatePasswordRequest request) {
        service.updatePassword(user, request);
        return ResponseEntity.accepted().build();
    }

    @RequestMapping(value = "/account/password", method = RequestMethod.POST)
    public ResponseEntity updatePasswordAccount(@RequestBody UpdatePasswordRequest request) {
        service.updatePasswordAccount(request);
        return ResponseEntity.accepted().build();
    }

}
