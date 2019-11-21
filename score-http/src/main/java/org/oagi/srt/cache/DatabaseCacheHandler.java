package org.oagi.srt.cache;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
public class DatabaseCacheHandler<T> implements InitializingBean {

    private final String tableName;
    private final Class<T> mappedClass;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private String camelCasePriKeyName;
    private String underscorePriKeyName;

    public DatabaseCacheHandler(String tableName, Class<T> mappedClass) {
        this.tableName = tableName;
        this.mappedClass = mappedClass;

        setPrimaryKeyName(this.tableName + "_id");
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.underscorePriKeyName = primaryKeyName;
        this.camelCasePriKeyName = underscoreToCamelCase(primaryKeyName);
    }

    public String underscoreToCamelCase(String string) {
        String str = Arrays.asList(string.split("_")).stream()
                .map(e -> Character.toUpperCase(e.charAt(0)) + e.substring(1).toLowerCase())
                .collect(Collectors.joining(""));
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public String getTableName() {
        return this.tableName;
    }

    public Class<T> getMappedClass() {
        return mappedClass;
    }

    public String getCamelCasePriKeyName() {
        return camelCasePriKeyName;
    }

    public void setCamelCasePriKeyName(String camelCasePriKeyName) {
        this.camelCasePriKeyName = camelCasePriKeyName;
    }

    public String getUnderscorePriKeyName() {
        return underscorePriKeyName;
    }

    public void setUnderscorePriKeyName(String underscorePriKeyName) {
        this.underscorePriKeyName = underscorePriKeyName;
    }

    @Override
    public void afterPropertiesSet() {
    }

    private List<String> loadFields(String tableName) {
        List<String> fields = new ArrayList();
        jdbcTemplate.query("DESCRIBE `" + tableName + "`", rch -> {
            String field = rch.getString("Field");
            fields.add(field);
        });
        return fields;
    }

    public String getChecksumQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ").append(this.underscorePriKeyName)
                .append(", sha1(concat_ws(`").append(String.join("`,`", loadFields(this.tableName)))
                .append("`)) `checksum` FROM ").append(this.tableName);
        return query.toString();
    }

    public String getChecksumByIdQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT sha1(concat_ws(`").append(String.join("`,`", loadFields(this.tableName)))
                .append("`)) `checksum` FROM ").append(this.tableName).append(" WHERE ")
                .append(this.underscorePriKeyName).append(" = :id");
        return query.toString();
    }

}
