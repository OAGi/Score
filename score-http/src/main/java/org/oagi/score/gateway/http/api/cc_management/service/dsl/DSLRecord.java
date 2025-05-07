package org.oagi.score.gateway.http.api.cc_management.service.dsl;

public interface DSLRecord {

    boolean contains(String keyword);

    boolean isChildOf(String keyword);

    int getChildrenCount();

    String name();

}
