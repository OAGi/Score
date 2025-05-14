package org.oagi.score.gateway.http.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.oagi.score.gateway.http.common.model.PageRequest;
import org.oagi.score.gateway.http.common.model.Sort;
import org.oagi.score.gateway.http.common.model.SortDirection;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Collections;

import static java.util.stream.Collectors.toList;
import static org.oagi.score.gateway.http.common.model.PageRequest.DEFAULT_PAGE_INDEX;
import static org.oagi.score.gateway.http.common.model.PageRequest.DEFAULT_PAGE_SIZE;
import static org.oagi.score.gateway.http.common.util.StringUtils.hasLength;
import static org.oagi.score.gateway.http.common.util.Utility.separate;

public class ControllerUtils {

    private ControllerUtils() {
    }

    public static PageRequest pageRequest(Integer pageIndex,
                                          Integer pageSize,
                                          String orderBy) {
        return new PageRequest(
                (pageIndex != null) ? pageIndex : DEFAULT_PAGE_INDEX,
                (pageSize != null) ? pageSize : DEFAULT_PAGE_SIZE,
                (org.springframework.util.StringUtils.hasLength(orderBy)) ? separate(orderBy).map(e -> {
                    char ch = e.charAt(0);
                    if (ch == '-') {
                        return new Sort(e.substring(1), SortDirection.DESC);
                    } else {
                        return new Sort((ch == '+') ? e.substring(1) : e, SortDirection.ASC);
                    }
                }).collect(toList()) : Collections.emptyList());
    }

    public static PageRequest pageRequest(Integer pageIndex,
                                          Integer pageSize,
                                          String sortActive,
                                          String sortDirection) {
        return new PageRequest(
                (pageIndex != null) ? pageIndex : DEFAULT_PAGE_INDEX,
                (pageSize != null) ? pageSize : DEFAULT_PAGE_SIZE,
                (org.springframework.util.StringUtils.hasLength(sortActive)) ?
                        Arrays.asList(new Sort(sortActive, org.springframework.util.StringUtils.hasLength(sortDirection) ?
                                SortDirection.valueOf(sortDirection.toUpperCase()) :
                                SortDirection.ASC)) : Collections.emptyList());
    }

    public static String getRequestScheme(HttpServletRequest request) {
        String proto = request.getHeader("X-Forwarded-Proto");
        return hasLength(proto) ? proto : request.getScheme();
    }

    public static String getRequestHostname(HttpServletRequest request) {
        String host = request.getHeader("X-Forwarded-Host");
        return hasLength(host) ? host : request.getHeader(HttpHeaders.HOST);
    }

}
