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

    public boolean matchPassword(Authentication authentication, String oldPassword) {
        User user = userRepository.findOneByLoginId(authentication.getName());
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Transactional(readOnly = false)
    public void updatePassword(Authentication authentication, String newPassword) {
        User user = userRepository.findOneByLoginId(authentication.getName());
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
