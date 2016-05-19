package org.oagi.srt.repository.entity;

import java.io.Serializable;

public class XSDBuiltInType implements Serializable {

    private int xbtId;
    private String name;
    private String builtInType;
    private int subtypeOfXbtId;

    public int getXbtId() {
        return xbtId;
    }

    public void setXbtId(int xbtId) {
        this.xbtId = xbtId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuiltInType() {
        return builtInType;
    }

    public void setBuiltInType(String builtInType) {
        this.builtInType = builtInType;
    }

    public int getSubtypeOfXbtId() {
        return subtypeOfXbtId;
    }

    public void setSubtypeOfXbtId(int subtypeOfXbtId) {
        this.subtypeOfXbtId = subtypeOfXbtId;
    }

    @Override
    public String toString() {
        return "XSDBuiltInType{" +
                "xbtId=" + xbtId +
                ", name='" + name + '\'' +
                ", builtInType='" + builtInType + '\'' +
                ", subtypeOfXbtId=" + subtypeOfXbtId +
                '}';
    }
}
