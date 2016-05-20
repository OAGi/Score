package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.BlobContent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BlobContentMapper implements RowMapper<BlobContent> {

    public static BlobContentMapper INSTANCE = new BlobContentMapper();

    @Override
    public BlobContent mapRow(ResultSet rs, int rowNum) throws SQLException {
        BlobContent blobContent = new BlobContent();
        
        return blobContent;
    }
}
