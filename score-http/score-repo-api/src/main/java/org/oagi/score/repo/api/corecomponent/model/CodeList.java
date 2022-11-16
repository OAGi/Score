package org.oagi.score.repo.api.corecomponent.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

public class CodeList implements CoreComponent, Serializable {

    private BigInteger codeListManifestId;

    private BigInteger codeListId;

    private String guid;

    private String name;

    private String versionId;

    private BigInteger basedCodeListId;

    private String listId;

    private BigInteger agencyIdListValueManifestId;

    private String agencyName;

    private BigInteger prevCodeListId;

    private BigInteger nextCodeListId;

    public BigInteger getCodeListManifestId() {
        return codeListManifestId;
    }

    public void setCodeListManifestId(BigInteger codeListManifestId) {
        this.codeListManifestId = codeListManifestId;
    }

    public BigInteger getBasedCodeListId() {
        return basedCodeListId;
    }

    public void setBasedCodeListId(BigInteger basedCodeListId) {
        this.basedCodeListId = basedCodeListId;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public BigInteger getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(BigInteger codeListId) {
        this.codeListId = codeListId;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public BigInteger getAgencyIdListValueManifestId() {
        return agencyIdListValueManifestId;
    }

    public void setAgencyIdListValueManifestId(BigInteger agencyIdListValueManifestId) {
        this.agencyIdListValueManifestId = agencyIdListValueManifestId;
    }

    public BigInteger getPrevCodeListId() {
        return prevCodeListId;
    }

    public void setPrevCodeListId(BigInteger prevCodeListId) {
        this.prevCodeListId = prevCodeListId;
    }

    public BigInteger getNextCodeListId() {
        return nextCodeListId;
    }

    public void setNextCodeListId(BigInteger nextCodeListId) {
        this.nextCodeListId = nextCodeListId;
    }

    @Override
    public BigInteger getId() {
        return getCodeListId();
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeList that = (CodeList) o;
        return Objects.equals(codeListId, that.codeListId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeListId);
    }
}
