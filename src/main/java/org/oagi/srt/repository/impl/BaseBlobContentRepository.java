package org.oagi.srt.repository.impl;

import org.oagi.srt.repository.BlobContentRepository;
import org.oagi.srt.repository.entity.BlobContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Types;

@Repository
public class BaseBlobContentRepository extends NamedParameterJdbcDaoSupport implements BlobContentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initialize() {
        setJdbcTemplate(jdbcTemplate);
    }

    private final String SAVE_STATEMENT = "INSERT INTO blob_content (" +
            "content, release_id, module) VALUES (" +
            ":content, :release_id, :module)";

    @Override
    public void save(BlobContent blobContent) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("content", new SqlLobValue(blobContent.getContent()), Types.BLOB)
                .addValue("release_id", blobContent.getReleaseId())
                .addValue("module", blobContent.getModule());

        int blobContentId = doSave(namedParameters, blobContent);
        blobContent.setBlobContentId(blobContentId);
    }

    protected int doSave(MapSqlParameterSource namedParameters,
                         BlobContent blobContent) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getNamedParameterJdbcTemplate().update(SAVE_STATEMENT, namedParameters, keyHolder, new String[]{"blob_content_id"});
        return keyHolder.getKey().intValue();
    }
}
