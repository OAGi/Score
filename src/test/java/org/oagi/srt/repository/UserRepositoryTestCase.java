package org.oagi.srt.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class UserRepositoryTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void test_findOneByLoginId() {
        String expectedName = "Open Applications Group Developer";
        User user = userRepository.findOneByLoginId("oagis");

        assertEquals(expectedName, user.getName());
    }

    @Test
    public void test_findOneByName() {
        String expectedLoginId = "oagis";
        User user = userRepository.findOneByName("Open Applications Group Developer");

        assertEquals(expectedLoginId, user.getLoginId());
    }
}
