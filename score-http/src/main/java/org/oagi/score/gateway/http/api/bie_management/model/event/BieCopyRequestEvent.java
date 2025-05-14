package org.oagi.score.gateway.http.api.bie_management.model.event;

import org.oagi.score.gateway.http.api.account_management.model.UserId;
import org.oagi.score.gateway.http.api.bie_management.model.TopLevelAsbiepId;
import org.oagi.score.gateway.http.api.context_management.business_context.model.BusinessContextId;
import org.oagi.score.gateway.http.common.model.event.Event;

import java.util.List;

/**
 * Represents an event for a BIE (Business Information Entity) copy request.
 * This event encapsulates the information necessary to request a copy of a BIE, including
 * the user making the request, the source and copied BIE IDs, and the list of business contexts
 * associated with the request.
 * <p>
 * This class implements the {@link Event} interface.
 * </p>
 *
 * <p><strong>Note:</strong> Do not switch this class to a Java record.
 * {@code GenericJackson2JsonRedisSerializer} cannot handle Java records properly at the time of writing.</p> *
 *
 * <p>
 * The event is initialized with:
 * <ul>
 *     <li>{@link UserId} - the user who made the copy request.</li>
 *     <li>{@link TopLevelAsbiepId} - the source BIE ID from which the copy is made.</li>
 *     <li>{@link TopLevelAsbiepId} - the copied BIE ID that represents the new entity.</li>
 *     <li>{@link List<BusinessContextId>} - a list of business context IDs associated with the event.</li>
 * </ul>
 * </p>
 */
public class BieCopyRequestEvent implements Event {

    private UserId userId;
    private TopLevelAsbiepId sourceTopLevelAsbiepId;
    private TopLevelAsbiepId copiedTopLevelAsbiepId;
    private List<BusinessContextId> bizCtxIdList;

    /**
     * Default constructor.
     */
    public BieCopyRequestEvent() {
    }

    /**
     * Constructs a new BieCopyRequestEvent with the specified parameters.
     *
     * @param userId                 the user making the copy request.
     * @param sourceTopLevelAsbiepId the source BIE ID for the copy.
     * @param copiedTopLevelAsbiepId the copied BIE ID.
     * @param bizCtxIdList           the list of business context IDs associated with the request.
     */
    public BieCopyRequestEvent(UserId userId,
                               TopLevelAsbiepId sourceTopLevelAsbiepId,
                               TopLevelAsbiepId copiedTopLevelAsbiepId,
                               List<BusinessContextId> bizCtxIdList) {
        this.userId = userId;
        this.sourceTopLevelAsbiepId = sourceTopLevelAsbiepId;
        this.copiedTopLevelAsbiepId = copiedTopLevelAsbiepId;
        this.bizCtxIdList = bizCtxIdList;
    }

    /**
     * Gets the user ID associated with the copy request.
     *
     * @return the user ID.
     */
    public UserId getUserId() {
        return userId;
    }

    /**
     * Sets the user ID for the copy request.
     *
     * @param userId the user ID to set.
     */
    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    /**
     * Gets the source top-level ASBIEP ID.
     *
     * @return the source top-level ASBIEP ID.
     */
    public TopLevelAsbiepId getSourceTopLevelAsbiepId() {
        return sourceTopLevelAsbiepId;
    }

    /**
     * Sets the source top-level ASBIEP ID.
     *
     * @param sourceTopLevelAsbiepId the source ASBIEP ID to set.
     */
    public void setSourceTopLevelAsbiepId(TopLevelAsbiepId sourceTopLevelAsbiepId) {
        this.sourceTopLevelAsbiepId = sourceTopLevelAsbiepId;
    }

    /**
     * Gets the copied top-level ASBIEP ID.
     *
     * @return the copied top-level ASBIEP ID.
     */
    public TopLevelAsbiepId getCopiedTopLevelAsbiepId() {
        return copiedTopLevelAsbiepId;
    }

    /**
     * Sets the copied top-level ASBIEP ID.
     *
     * @param copiedTopLevelAsbiepId the copied ASBIEP ID to set.
     */
    public void setCopiedTopLevelAsbiepId(TopLevelAsbiepId copiedTopLevelAsbiepId) {
        this.copiedTopLevelAsbiepId = copiedTopLevelAsbiepId;
    }

    /**
     * Gets the list of business context IDs associated with the copy request.
     *
     * @return the list of business context IDs.
     */
    public List<BusinessContextId> getBizCtxIdList() {
        return bizCtxIdList;
    }

    /**
     * Sets the list of business context IDs associated with the copy request.
     *
     * @param bizCtxIdList the list of business context IDs to set.
     */
    public void setBizCtxIdList(List<BusinessContextId> bizCtxIdList) {
        this.bizCtxIdList = bizCtxIdList;
    }

}
