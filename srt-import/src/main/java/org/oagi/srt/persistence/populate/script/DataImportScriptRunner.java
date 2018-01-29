package org.oagi.srt.persistence.populate.script;

import javax.persistence.EntityManager;

public interface DataImportScriptRunner {

    public void printHeader();

    public void printSettings();

    public void printResetSequences(EntityManager entityManager);

}
