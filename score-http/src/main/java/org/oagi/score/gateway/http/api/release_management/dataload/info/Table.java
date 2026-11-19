package org.oagi.score.gateway.http.api.release_management.dataload.info;

public class Table {

    private String tableName;
    private long maxId;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }
}
