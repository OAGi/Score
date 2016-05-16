package org.oagi.srt.repository.mapper;

import org.oagi.srt.repository.entity.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {

    public static UserMapper INSTANCE = new UserMapper();

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setAppUserId(rs.getInt("app_user_id"));
        user.setLoginId(rs.getString("login_id"));
        user.setPassword(rs.getString("password"));
        user.setName(rs.getString("name"));
        user.setOrganization(rs.getString("organization"));
        user.setOagisDeveloperIndicator(rs.getInt("oagis_developer_indicator") == 1 ? true : false);
        return user;
    }
}
