package org.oagi.srt.persistence.populate.script.oracle;

import com.google.common.base.Splitter;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.*;
import org.hibernate.type.descriptor.converter.AttributeConverterTypeAdapter;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.oagi.srt.persistence.populate.script.oracle.OracleDataImportScriptPrinter.print;

public class OracleEntityUpdateInterceptor extends EmptyInterceptor {

    private String getTableName(Object entity) {
        Class<?> clazz = entity.getClass();
        Table tableAnnotation = clazz.getDeclaredAnnotation(Table.class);
        return tableAnnotation.name().toUpperCase();
    }

    private String getIdentifierName(Object entity) {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getDeclaredAnnotation(Id.class) != null) {
                return convertPropertyNameForDB(field.getName()).toUpperCase();
            }
        }
        return getSequenceName(entity).replace("_SEQ", "");
    }

    private String getSequenceName(Object entity) {
        Class<?> clazz = entity.getClass();
        try {
            Field field = clazz.getDeclaredField("SEQUENCE_NAME");
            return ((String) field.get(entity));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Field getDeclaredField(Class<?> clazz, String propertyName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(propertyName);
        } catch (NoSuchFieldException e) {
            clazz = clazz.getSuperclass();
            return clazz.getDeclaredField(propertyName);
        }
    }

    private String getPropertyName(Object entity, String propertyName) {
        Class<?> clazz = entity.getClass();
        try {
            Field field = getDeclaredField(clazz, propertyName);
            Column column = field.getDeclaredAnnotation(Column.class);
            if (column == null) {
                return propertyName;
            }

            String name = column.name();
            return (StringUtils.isEmpty(name)) ? propertyName : name;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String convertPropertyNameForDB(String propertyName) {
        StringBuilder sb = new StringBuilder();
        for (char ch : propertyName.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                sb.append("_");
            }
            sb.append(ch);
        }
        return sb.toString().toLowerCase();
    }

    private boolean isEntity(Object object) {
        if (object == null) {
            return false;
        }
        Class<?> clazz = object.getClass();
        Entity entity = clazz.getDeclaredAnnotation(Entity.class);
        return (entity != null) ? true : false;
    }

    private Long getId(Object entity) {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getDeclaredAnnotation(Id.class) != null) {
                try {
                    field.setAccessible(true);
                    try {
                        Long id = (Long) field.get(entity);
                        return id;
                    } finally {
                        field.setAccessible(false);
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        throw new IllegalStateException();
    }

    private Integer getEnumValue(Object enumObj) {
        if (enumObj == null) {
            return null;
        }
        Class<?> clazz = enumObj.getClass();
        try {
            Method getValueMethod = clazz.getDeclaredMethod("getValue", new Class[0]);
            return (Integer) getValueMethod.invoke(enumObj, new Object[0]);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    private String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private File getFile(Object entity) {
        Class<?> clazz = entity.getClass();
        try {
            Field fileField = clazz.getDeclaredField("file");
            try {
                fileField.setAccessible(true);
                return (File) fileField.get(entity);
            } finally {
                fileField.setAccessible(false);
            }
        } catch (NoSuchFieldException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String getRelativePath(File file) {
        try {
            Path pathAbsolute = Paths.get(file.getCanonicalPath());
            Path pathBase = Paths.get(new File(System.getProperty("user.dir")).getCanonicalPath());
            Path pathRelative = pathBase.relativize(pathAbsolute);

            return FilenameUtils.separatorsToUnix(pathRelative.toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String escape(String value) {
        return value.replace("'", "''");
    }

    private void propertyNameSet(Object entity, String[] propertyNames, Type[] types, StringBuilder sb, int i) {
        sb.append(getColumnName(entity, propertyNames, types, i));
    }

    private String getColumnName(Object entity, String[] propertyNames, Type[] types, int i) {
        String propertyName = convertPropertyNameForDB(getPropertyName(entity, propertyNames[i])).toUpperCase();
        Type type = types[i];
        if (type.isAssociationType()) {
            return propertyName + "_ID";
        } else {
            return propertyName;
        }
    }

    private String valueSet(
            Object entity,
            Object[] state,
            Type[] types,
            StringBuilder sb,
            int i) {
        Object value = state[i];
        Type type = types[i];
        if (type == BooleanType.INSTANCE) {
            boolean booleanValue = ((Boolean) value).booleanValue();
            sb.append((booleanValue) ? "1" : "0");
        } else if (type == StringType.INSTANCE || type == ClobType.INSTANCE || type == MaterializedClobType.INSTANCE) {
            if (StringUtils.isEmpty(value)) {
                sb.append("null");
            } else {
                String str = (String) value;
                if (str.length() > 4000) {
                    sb.append("EMPTY_CLOB()");
                    return escape(str);
                } else {
                    sb.append("'").append(escape(str)).append("'");
                }
            }
        } else if (type == LongType.INSTANCE || type == IntegerType.INSTANCE) {
            sb.append(value);
        } else if (type == TimestampType.INSTANCE) {
            sb.append("CURRENT_TIMESTAMP");
        } else if (type == BinaryType.INSTANCE || type == BlobType.INSTANCE || type == MaterializedBlobType.INSTANCE) {
            sb.append("EMPTY_BLOB()");
            return toHex((byte[]) value);
        } else if (type.getClass() == AttributeConverterTypeAdapter.class) {
            sb.append(getEnumValue(value));
        } else if (type.getClass() == CustomType.class) {  // EnumType
            sb.append("'").append(value).append("'");
        } else {
            if (isEntity(value)) {
                sb.append(getId(value));
            } else {
                sb.append(value);
            }
        }
        return null;
    }

    private class LobValueResolver {
        private String dbType;
        private String lobValue;
        private String lobColumnName;
        private Object entity;
        private Serializable id;

        public LobValueResolver(String lobType,
                                String lobValue,
                                String lobColumnName,
                                Object entity,
                                Serializable id) {
            this.dbType = lobType;
            this.lobValue = lobValue;
            this.lobColumnName = lobColumnName;
            this.entity = entity;
            this.id = id;
        }

        public void resolve() {
            StringBuilder sb = new StringBuilder();
            sb.append("DECLARE\n" +
                    "  buf " + dbType + "; \n" +
                    "BEGIN\n" +
                    "  dbms_lob.createtemporary(buf, FALSE);\n");

            for (String splittedValue : Splitter.fixedLength(2000).split(lobValue)) {
                switch (dbType.toUpperCase()) {
                    case "BLOB":
                        sb.append("  dbms_lob.append(buf, HEXTORAW('" + splittedValue + "'));\n");
                        break;
                    case "CLOB":
                        sb.append("  dbms_lob.append(buf, '" + splittedValue + "');\n");
                        break;
                }

            }

            sb.append("  UPDATE " + getTableName(entity) + "\n" +
                    "     SET " + lobColumnName + " = buf\n" +
                    "   WHERE " + getIdentifierName(entity) + " = " + id + ";\n" +
                    "END;\n" +
                    "/");

            print(sb.toString());
        }
    }

    @Override
    public boolean onSave(
            Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) {

        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(getTableName(entity));
        sb.append(" (").append(getIdentifierName(entity)).append(", ");
        for (int i = 0, len = propertyNames.length; i < len; ++i) {
            propertyNameSet(entity, propertyNames, types, sb, i);

            if (i + 1 == len) {
                sb.append(") VALUES (");
            } else {
                sb.append(", ");
            }
        }

        sb.append(id).append(", ");

        List<LobValueResolver> lobValueResolverList = new ArrayList();
        for (int i = 0, len = state.length; i < len; ++i) {
            String lobValue = valueSet(entity, state, types, sb, i);
            if (lobValue != null) {
                String lobColumnName = getColumnName(entity, propertyNames, types, i);
                Type type = types[i];
                String lobType;
                if (type == BinaryType.INSTANCE || type == BlobType.INSTANCE || type == MaterializedBlobType.INSTANCE) {
                    lobType = "BLOB";
                } else {
                    lobType = "CLOB";
                }
                lobValueResolverList.add(new LobValueResolver(lobType, lobValue, lobColumnName, entity, id));
            }

            if (i + 1 == len) {
                sb.append(");");
            } else {
                sb.append(", ");
            }
        }

        print(sb.toString());

        for (LobValueResolver lobValueResolver : lobValueResolverList) {
            lobValueResolver.resolve();
        }

        return false;
    }

    @Override
    public boolean onFlushDirty(
            Object entity,
            Serializable id,
            Object[] currentState,
            Object[] previousState,
            String[] propertyNames,
            Type[] types) {

        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(getTableName(entity));
        sb.append(" SET ");

        for (int i = 0, len = propertyNames.length; i < len; ++i) {
            Object previous = previousState[i];
            Object current = currentState[i];

            boolean update = false;
            if (current != null && !current.equals(previous)) {
                update = true;
            } else if (previous != null && !previous.equals(current)) {
                update = true;
            }

            if (update) {
                propertyNameSet(entity, propertyNames, types, sb, i);
                sb.append(" = ");
                valueSet(entity, currentState, types, sb, i);

                if (i + 1 == len) {
                    sb.append(" ");
                } else {
                    sb.append(", ");
                }
            }
        }

        sb.append("WHERE ").append(getIdentifierName(entity)).append(" = ").append(id).append(";");

        print(sb.toString().replace(", WHERE", " WHERE"));

        return false;
    }
}
