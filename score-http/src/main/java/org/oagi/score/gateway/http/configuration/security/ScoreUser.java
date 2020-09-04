package org.oagi.score.gateway.http.configuration.security;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class ScoreUser extends User implements AuthenticatedPrincipal  {

    public ScoreUser(String username, String password,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public ScoreUser(String username, String password,
                     boolean enabled, boolean accountNonExpired,
                     boolean credentialsNonExpired, boolean accountNonLocked,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    @Override
    public String getName() {
        return getUsername();
    }

}
