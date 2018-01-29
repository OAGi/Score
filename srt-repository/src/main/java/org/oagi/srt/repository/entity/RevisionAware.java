package org.oagi.srt.repository.entity;

public interface RevisionAware extends TimestampAware {
    int getRevisionNum();

    void setRevisionNum(int revisionNum);

    int getRevisionTrackingNum();

    void setRevisionTrackingNum(int revisionTrackingNum);

    RevisionAction getRevisionAction();

    void setRevisionAction(RevisionAction revisionAction);
}
