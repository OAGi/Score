package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Auditable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class CreateBulkBusinessTermResponse extends Auditable {

    private final List<BigInteger> businessTermIds;
    private List<String> formatCheckExceptions = Collections.emptyList();

    public CreateBulkBusinessTermResponse(List<BigInteger> businessTermIds) {
        this.businessTermIds = businessTermIds;
    }

    public List<BigInteger> getBusinessTermIds() {
        return businessTermIds;
    }

    public List<String> getFormatCheckExceptions() {
        return formatCheckExceptions;
    }
    public void setFormatCheckExceptions(List<String> formatCheckExceptions) {
        this.formatCheckExceptions = formatCheckExceptions;
    }
}
