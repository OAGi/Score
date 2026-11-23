package org.oagi.score.gateway.http.common.repository.jooq;

import org.junit.jupiter.api.Test;
import org.oagi.score.gateway.http.common.model.base.DataValueTooLongException;
import org.oagi.score.gateway.http.common.model.base.DuplicateValueException;
import org.oagi.score.gateway.http.common.model.base.ReferencedRecordException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataIntegrityExceptionTranslatorTest {

    @Test
    void valueLongerThanColumnIsToLongWhateverTheDatabaseCode() {
        var ex = new DataIntegrityViolationException("x", new SQLException("too long", "22001", 12345));
        assertInstanceOf(DataValueTooLongException.class, DataIntegrityExceptionTranslator.translate(ex));
    }

    @Test
    void duplicateKeyIsDuplicate() {
        assertInstanceOf(DuplicateValueException.class,
                DataIntegrityExceptionTranslator.translate(new DuplicateKeyException("x")));
    }

    @Test
    void otherIntegrityViolationIsReferenced() {
        var ex = new DataIntegrityViolationException("x", new SQLException("fk", "23000", 1451));
        assertInstanceOf(ReferencedRecordException.class, DataIntegrityExceptionTranslator.translate(ex));
    }

    @Test
    void unrelatedExceptionIsLeftAlone() {
        assertNull(DataIntegrityExceptionTranslator.translate(new QueryTimeoutException("x")));
    }
}
