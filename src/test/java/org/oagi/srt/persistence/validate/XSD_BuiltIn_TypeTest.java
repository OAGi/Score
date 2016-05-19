package org.oagi.srt.persistence.validate;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oagi.srt.common.util.Utility;
import org.oagi.srt.persistence.validate.data.TableData;
import org.oagi.srt.repository.RepositoryFactory;
import org.oagi.srt.repository.XSDBuiltInTypeRepository;
import org.oagi.srt.repository.entity.XSDBuiltInType;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author Yunsu Lee
 * @version 1.0
 */

public class XSD_BuiltIn_TypeTest {

    @Autowired
    private RepositoryFactory repositoryFactory;
    private XSDBuiltInTypeRepository dao;

    @Before
    public void connectDB() throws Exception {
        dao = repositoryFactory.xsdBuiltInTypeRepository();
    }

    @Test
    public void testNumberOfData() {
        assertEquals(TableData.XDT_BUILT_IN_TYPE.length, dao.findAll().size());
    }

    @Test
    public void testName() {
        for (int i = 0; i < TableData.XDT_BUILT_IN_TYPE.length; i++) {
            XSDBuiltInType xVO = dao.findOneByName(TableData.XDT_BUILT_IN_TYPE[i][0]);
            if (xVO.getBuiltInType() == null)
                fail("No such type with the name, '" + TableData.XDT_BUILT_IN_TYPE[i][0] + "'r.");
        }
    }

    @Test
    public void testBuiltinType() {
        for (int i = 0; i < TableData.XDT_BUILT_IN_TYPE.length; i++) {
            XSDBuiltInType xVO = dao.findOneByName(TableData.XDT_BUILT_IN_TYPE[i][0]);
            if (xVO.getBuiltInType() == null)
                fail("No such type with the name, '" + TableData.XDT_BUILT_IN_TYPE[i][0] + "'r.");

            assertEquals(TableData.XDT_BUILT_IN_TYPE[i][1], xVO.getBuiltInType());
        }
    }

    @Test
    public void testTypeHierarchy() {
        for (int i = 0; i < TableData.XDT_BUILT_IN_TYPE.length; i++) {
            XSDBuiltInType xVO = dao.findOneByName(TableData.XDT_BUILT_IN_TYPE[i][0]);
            if (xVO.getBuiltInType() == null)
                fail("No such type with the name, '" + TableData.XDT_BUILT_IN_TYPE[i][0] + "'r.");

            XSDBuiltInType xVO1 = dao.findOneByXbtId(xVO.getSubtypeOfXbtId());

            assertEquals(TableData.XDT_BUILT_IN_TYPE[i][2], xVO1.getBuiltInType());
        }
    }
}
