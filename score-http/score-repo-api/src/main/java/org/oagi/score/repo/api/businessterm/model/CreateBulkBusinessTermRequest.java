package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;
import org.oagi.score.repo.api.user.model.ScoreUser;

import java.io.InputStream;
import java.util.List;

public class CreateBulkBusinessTermRequest extends Request {

    private InputStream inputStream;

    private List<BusinessTerm> businessTermList;

    public CreateBulkBusinessTermRequest(ScoreUser requester) {
        super(requester);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public List<BusinessTerm> getBusinessTermList() {
        return businessTermList;
    }

    public void setBusinessTermList(List<BusinessTerm> businessTermList) {
        this.businessTermList = businessTermList;
    }
}
