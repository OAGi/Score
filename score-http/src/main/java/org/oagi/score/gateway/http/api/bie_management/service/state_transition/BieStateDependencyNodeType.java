package org.oagi.score.gateway.http.api.bie_management.service.state_transition;

/**
 * Node kinds that can appear in the BIE state dependency dialog.
 */
public enum BieStateDependencyNodeType {
    /**
     * Row represents a top-level BIE.
     */
    BIE,
    /**
     * Row represents a code list assigned somewhere inside a BIE.
     */
    CODE_LIST
}
