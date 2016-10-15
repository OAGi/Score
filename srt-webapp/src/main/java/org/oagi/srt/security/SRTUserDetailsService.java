package org.oagi.srt.security;

import org.oagi.srt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service("userDetailsService")
public class SRTUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.oagi.srt.repository.entity.User user = userRepository.findOneByLoginId(username);
        if (user == null) {
            throw new UsernameNotFoundException("'" + username + "' doesn't exist");
        }
        return buildUser(user, Collections.emptyList());
    }

    private UserDetails buildUser(org.oagi.srt.repository.entity.User user,
                                  List<GrantedAuthority> authorities) {
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        return new User(user.getLoginId(), user.getPassword(),
                enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

}
