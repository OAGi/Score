package org.oagi.srt.repository;

import org.oagi.srt.repository.entity.CodeListValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CodeListValueRepository extends JpaRepository<CodeListValue, Integer> {

    @Query("select c from CodeListValue c where c.codeListId = ?1")
    public List<CodeListValue> findByCodeListId(int codeListId);

    @Query("select c from CodeListValue c where c.codeListId = ?1 and c.value = ?2")
    public CodeListValue findOneByCodeListIdAndValue(int codeListId, String value);

    @Query("update CodeListValue c set c.codeListId = ?1 where codeListValueId = ?2")
    public void updateCodeListIdByCodeListValueId(int codeListId, int codeListValueId);

}
