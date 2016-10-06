package org.oagi.srt.service;

import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findByAuthentication(Authentication authentication) {
        return userRepository.findOneByLoginId(authentication.getName());
    }

    public boolean exists(String username) {
        return (userRepository.findOneByName(username) != null);
    }

    public boolean matchPassword(Authentication authentication, String oldPassword) {
        User user = userRepository.findOneByLoginId(authentication.getName());
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Transactional
    public void updatePassword(Authentication authentication, String newPassword) {
        User user = userRepository.findOneByLoginId(authentication.getName());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void register(String username, String rawPassword) {
        User user = new User();
        user.setLoginId(username);
        user.setName(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setOagisDeveloperIndicator(false);
        user.setOrganization(null);
        userRepository.save(user);
    }
}
