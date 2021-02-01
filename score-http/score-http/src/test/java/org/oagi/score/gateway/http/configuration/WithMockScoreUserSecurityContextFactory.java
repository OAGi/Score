package org.oagi.score.gateway.http.configuration;

import org.oagi.score.gateway.http.configuration.security.ScoreUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;

public class WithMockScoreUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockScoreUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockScoreUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User principal = new ScoreUser(annotation.username(), annotation.password(),
                Arrays.asList(new SimpleGrantedAuthority(annotation.role())));
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }
}
