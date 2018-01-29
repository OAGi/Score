package org.oagi.srt.service;

import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByAuthentication(Authentication authentication) {
        return userRepository.findOneByLoginId(authentication.getName());
    }

    public User findByUserId(long userId) {
        return userRepository.findOne(userId);
    }

    public User findByLoginId(String loginId) {
        return userRepository.findOneByLoginId(loginId);
    }

    public Map<Long, User> findByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> userList = userRepository.findAllByUserIds(userIds);
        return userList.stream().collect(Collectors.toMap(e -> e.getAppUserId(), Function.identity()));
    }

    public boolean exists(String username) {
        return (userRepository.findOneByName(username) != null);
    }

    public boolean matchPassword(Authentication authentication, String oldPassword) {
        User user = findByAuthentication(authentication);
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Transactional
    public void updatePassword(Authentication authentication, String newPassword) {
        User user = findByAuthentication(authentication);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public long register(String username, String rawPassword) {
        User user = new User();
        user.setLoginId(username);
        user.setPassword(rawPassword);
        user.setName(username);
        user.setOagisDeveloperIndicator(false);
        user.setOrganization(null);
        user = update(user);
        return user.getAppUserId();
    }

    @Transactional
    public User update(User user) {
        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }
}
