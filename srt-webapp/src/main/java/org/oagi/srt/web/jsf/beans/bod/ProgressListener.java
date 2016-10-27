package org.oagi.srt.web.jsf.beans.bod;

import org.oagi.srt.repository.entity.listener.PersistEventListener;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressListener implements PersistEventListener {
    private int maxCount = 0;
    private AtomicInteger currentCount = new AtomicInteger();
    private String status = "Initializing";

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    @Override
    public void onPrePersist(Object object) {
    }

    @Override
    public void onPostPersist(Object object) {
//            if (object instanceof AggregateBusinessInformationEntity) {
//                setProgressStatus("Updating ABIE");
//            } else if (object instanceof AssociationBusinessInformationEntity) {
//                setProgressStatus("Updating ASBIE");
//            } else if (object instanceof AssociationBusinessInformationEntityProperty) {
//                setProgressStatus("Updating ASBIEP");
//            } else if (object instanceof BasicBusinessInformationEntity) {
//                setProgressStatus("Updating BBIE");
//            } else if (object instanceof BasicBusinessInformationEntityProperty) {
//                setProgressStatus("Updating BBIEP");
//            } else if (object instanceof BasicBusinessInformationEntitySupplementaryComponent) {
//                setProgressStatus("Updating BBIESC");
//            }

        if (currentCount.incrementAndGet() == maxCount) {
            setProgressStatus("Completed");
        }
    }

    public int getProgress() {
        long progress = Math.round((currentCount.get() / (double) maxCount) * 100);
        return (int) progress;
    }

    public synchronized void setProgressStatus(String status) {
        this.status = status;
    }

    public synchronized String getProgressStatus() {
        return status;
    }
}