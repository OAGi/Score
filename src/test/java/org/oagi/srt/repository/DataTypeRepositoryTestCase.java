package org.oagi.srt.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.Application;
import org.oagi.srt.repository.entity.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.oagi.srt.common.util.Utility.generateGUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class DataTypeRepositoryTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private DataTypeRepository dataTypeRepository;

    @Test
    public void test_findOneByDtId() {
        int dtId = 1;
        String expectedGuid = "oagis-id-3bfbbc07cffc47a886496961b0f6b292";
        DataType dataType = dataTypeRepository.findOne(dtId);
        String actualGuid = dataType.getGuid();

        assertEquals(expectedGuid, actualGuid);
    }

    @Test
    public void test_save() {
        DataType expectedDataType = new DataType();
        expectedDataType.setGuid(generateGUID());
        expectedDataType.setVersionNum("1.0");
        expectedDataType.setDen("Test. Type");
        expectedDataType.setModule("Model\\Platform\\2_1\\ReadMe.txt");

        dataTypeRepository.save(expectedDataType);

        DataType actualDataType = dataTypeRepository.findOne(expectedDataType.getDtId());

        assertEquals(expectedDataType.getGuid(), actualDataType.getGuid());
        assertEquals(expectedDataType.getVersionNum(), actualDataType.getVersionNum());
        assertEquals(expectedDataType.getDen(), actualDataType.getDen());
        assertEquals(expectedDataType.getModule(), actualDataType.getModule());
    }
}
