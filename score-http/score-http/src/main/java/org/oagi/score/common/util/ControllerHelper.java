package org.oagi.score.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

import static org.oagi.score.repo.api.impl.utils.StringUtils.hasLength;

public class ControllerHelper {

    private ControllerHelper() {
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
