package org.oagi.srt.gateway.http.configuration.security;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;
}
