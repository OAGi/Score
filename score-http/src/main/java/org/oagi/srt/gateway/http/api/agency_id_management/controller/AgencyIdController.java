package org.oagi.srt.gateway.http.api.agency_id_management.controller;

import org.oagi.srt.gateway.http.api.agency_id_management.data.SimpleAgencyIdListValue;
import org.oagi.srt.gateway.http.api.agency_id_management.service.AgencyIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AgencyIdController {

    @Autowired
    private AgencyIdService service;

    @RequestMapping(value = "/simple_agency_id_list_values", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<SimpleAgencyIdListValue> getSimpleAgencyIdListValues() {
        return service.getSimpleAgencyIdListValues();
    }
}
