package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.Release;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReleaseMapper implements RowMapper<Release> {

    public static ReleaseMapper INSTANCE = new ReleaseMapper();

    @Override
    public Release mapRow(ResultSet rs, int rowNum) throws SQLException {
        Release release = new Release();
        release.setReleaseId(rs.getInt("release_id"));
        release.setReleaseNum(rs.getString("release_num"));
        release.setReleaseNote(rs.getString("release_note"));
        release.setNamespaceId(rs.getInt("namespace_id"));
        return release;
    }
}
