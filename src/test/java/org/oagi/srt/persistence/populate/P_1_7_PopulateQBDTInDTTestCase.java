package org.oagi.srt.persistence.populate;

import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
@Transactional(readOnly = true)
public class P_1_7_PopulateQBDTInDTTestCase extends AbstractTransactionalJUnit4SpringContextTests {

}
