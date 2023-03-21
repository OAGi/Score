package org.oagi.score.repo.api.businessterm.model;

import org.oagi.score.repo.api.base.Request;

import java.util.List;

public class ConfirmAsbieBbieListRequest extends Request {

    private List<BieToAssign> biesToAssign;

    public ConfirmAsbieBbieListRequest() {
    }

    public ConfirmAsbieBbieListRequest(List<BieToAssign> biesToAssign) {
        this.biesToAssign = biesToAssign;
    }

    public List<BieToAssign> getBiesToAssign() {
        return biesToAssign;
    }

    public void setBiesToAssign(List<BieToAssign> biesToAssign) {
        this.biesToAssign = biesToAssign;
    }

}
