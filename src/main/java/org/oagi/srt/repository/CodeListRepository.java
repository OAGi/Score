package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CodeList;

import java.util.List;

public interface CodeListRepository {

    public List<CodeList> findAll();

    public List<CodeList> findByNameContaining(String name);

    public List<CodeList> findByNameContainingAndStateIsPublishedAndExtensibleIndicatorIsTrue(String name);

    public CodeList findOneByCodeListId(int codeListId);

    public CodeList findOneByGuidAndEnumTypeGuidAndNameAndDefinition(
            String guid, String enumTypeGuid, String name, String definition
    );

    public CodeList findOneByGuidAndEnumTypeGuidAndCodeListIdAndNameAndDefinition(
            String guid, String enumTypeGuid, int codeListId, String name, String definition
    );

    public CodeList findOneByGuid(String guid);

    public void update(CodeList codeList);

    public void updateStateByCodeListId(String state, int codeListId);

    public void save(CodeList codeList);

}
