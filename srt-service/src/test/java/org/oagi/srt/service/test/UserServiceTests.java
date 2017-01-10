package org.oagi.srt.service.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.repository.entity.User;
import org.oagi.srt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Test
    public void findUserTest() {
        User oagisUser = userService.findByUserId(1L);
        assertThat(oagisUser).isNotNull();
        assertThat(oagisUser.getLoginId()).isEqualTo("oagis");
    }

    @Test
    public void addUserTest() {
        String loginId = "test-user";
        String password = "test-password";

        long userId = userService.register(loginId, password);
        assertThat(userId).isGreaterThan(1L);

        User user = userService.findByUserId(userId);
        assertThat(user).isNotNull();

        assertThat(user.getLoginId()).isEqualTo(loginId);
    }
}
