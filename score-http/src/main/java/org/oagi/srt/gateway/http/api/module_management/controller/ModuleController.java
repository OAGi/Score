package org.oagi.srt.gateway.http.api.module_management.controller;

import org.oagi.srt.gateway.http.api.module_management.data.Module;
import org.oagi.srt.gateway.http.api.module_management.data.ModuleList;
import org.oagi.srt.gateway.http.api.module_management.data.SimpleModule;
import org.oagi.srt.gateway.http.api.module_management.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ModuleController {

    @Autowired
    private ModuleService moduleService;

    @RequestMapping(value = "/simple_modules", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<SimpleModule> getSimpleModules() {
        return moduleService.getSimpleModules();
    }

    @RequestMapping(value = "/module_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ModuleList> getModuleList(@AuthenticationPrincipal User user) {
        return moduleService.getModuleList(user);
    }

    @RequestMapping(value = "/module/{id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Module getModule(@AuthenticationPrincipal User user,
                            @PathVariable("id") long moduleId) {
        return moduleService.getModule(user, moduleId);
    }
}
