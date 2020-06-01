package org.oagi.srt.gateway.http.api.info.controller;

import org.oagi.srt.gateway.http.api.info.data.ProductInfo;
import org.oagi.srt.gateway.http.api.info.data.SummaryBieInfo;
import org.oagi.srt.gateway.http.api.info.data.SummaryCcExtInfo;
import org.oagi.srt.gateway.http.api.info.service.BieInfoService;
import org.oagi.srt.gateway.http.api.info.service.CcInfoService;
import org.oagi.srt.gateway.http.api.info.service.ProductInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class InfoController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private BieInfoService bieInfoService;

    @Autowired
    private CcInfoService ccInfoService;

    @RequestMapping(value = "/info/products", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductInfo> getProductInfos() {
        List<ProductInfo> productInfos = new ArrayList();
        productInfos.add(productInfoService.gatewayMetadata());
        productInfos.add(productInfoService.databaseMetadata());
        productInfos.add(productInfoService.redisMetadata());
        return productInfos;
    }

    @RequestMapping(value = "/info/bie_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryBieInfo getSummaryBieInfo(@AuthenticationPrincipal User user) {
        return bieInfoService.getSummaryBieInfo(user);
    }

    @RequestMapping(value = "/info/cc_ext_summary",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SummaryCcExtInfo getSummaryCcExtInfo(@AuthenticationPrincipal User user) {
        return ccInfoService.getSummaryCcExtInfo(user);
    }
}
