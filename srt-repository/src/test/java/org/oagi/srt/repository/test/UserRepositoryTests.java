package org.oagi.srt.repository.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.repository.UserRepository;
import org.oagi.srt.repository.config.Config;
import org.oagi.srt.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {Config.class})
@ActiveProfiles("test")
@Transactional
@Rollback
@RunWith(SpringJUnit4ClassRunner.class)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void insertUserTest() {
        User user = new User();
        String expectedLoginId = "test-user";
        user.setLoginId(expectedLoginId);
        String expectedName = "test user";
        user.setName(expectedName);
        boolean expectedOagisDeveloperIndicator = false;
        user.setOagisDeveloperIndicator(expectedOagisDeveloperIndicator);
        String expectedOrganization = "test organization";
        user.setOrganization(expectedOrganization);
        String expectedPassword = "test";
        user.setPassword(expectedPassword);

        userRepository.saveAndFlush(user);
        Long appUserId = user.getAppUserId();

        user = userRepository.findById(appUserId).get();
        assertThat(user).isNotNull();

        assertThat(user.getLoginId()).isEqualTo(expectedLoginId);
        assertThat(user.getName()).isEqualTo(expectedName);
        assertThat(user.isOagisDeveloperIndicator()).isEqualTo(expectedOagisDeveloperIndicator);
        assertThat(user.getOrganization()).isEqualTo(expectedOrganization);
        assertThat(user.getPassword()).isEqualTo(expectedPassword);
    }

}
