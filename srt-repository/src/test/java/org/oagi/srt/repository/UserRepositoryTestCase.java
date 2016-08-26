package org.oagi.srt.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.config.TestRepositoryConfig;
import org.oagi.srt.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class UserRepositoryTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUp() {
        User testUser = new User();
        testUser.setLoginId("oagis");
        testUser.setPassword("oagis");
        testUser.setName("Open Applications Group Developer");

        userRepository.save(testUser);
    }

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
