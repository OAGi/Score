package org.oagi.srt.gateway.http.api.info.controller;

import org.oagi.srt.gateway.http.api.info.data.ProductInfo;
import org.oagi.srt.gateway.http.api.info.service.ProductInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ProductInfoController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductInfoService service;

    @RequestMapping(value = "/info/products", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductInfo> getProductInfos() {
        List<ProductInfo> productInfos = new ArrayList();
        productInfos.add(service.gatewayMetadata());
        productInfos.add(service.databaseMetadata());
        productInfos.add(service.redisMetadata());
        return productInfos;
    }
}
