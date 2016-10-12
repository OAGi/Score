package org.oagi.srt.web.jsf.component.treetable;

import java.util.List;

public interface Node {

    public String getType();

    public String getName();

    public <T extends Node> void addChild(T child);

    public List<? extends Node> getChildren();

}
