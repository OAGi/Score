package org.oagi.srt.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.oagi.srt.config.TestRepositoryConfig;
import org.oagi.srt.repository.entity.DataType;
import org.oagi.srt.repository.entity.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
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
        expectedDataType.setGuid(UUID.randomUUID().toString());
        expectedDataType.setVersionNum("1.0");
        expectedDataType.setDen("Test. Type");

        Module module = new Module();
        module.setModuleId(1);
        module.setModule("Model\\BODs\\AcknowledgeAllocateResource.xsd");
        module.setNamespaceId(1);
        module.setReleaseId(1);
        expectedDataType.setModule(module);

        dataTypeRepository.save(expectedDataType);

        DataType actualDataType = dataTypeRepository.findOne(expectedDataType.getDtId());

        assertEquals(expectedDataType.getGuid(), actualDataType.getGuid());
        assertEquals(expectedDataType.getVersionNum(), actualDataType.getVersionNum());
        assertEquals(expectedDataType.getDen(), actualDataType.getDen());
        assertEquals(expectedDataType.getModule(), actualDataType.getModule());
    }
}
