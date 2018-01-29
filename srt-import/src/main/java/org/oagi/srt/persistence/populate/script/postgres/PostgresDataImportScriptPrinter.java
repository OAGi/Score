package org.oagi.srt.persistence.populate.script.postgres;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collection;

public class PostgresDataImportScriptPrinter {

    private static PrintWriter printWriter;

    public static void turnOn() {
        if (printWriter != null) {
            turnOff();
        }

        try {
            printWriter = new PrintWriter(
                    new OutputStreamWriter(
                            new BufferedOutputStream(
                                    new FileOutputStream("./srt-import/src/main/resources/data-postgres.sql"))));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failure to open file", e);
        }
    }

    public static void printTitle(String title) {
        if (printWriter == null) {
            return;
        }

        StringBuilder straightLine = new StringBuilder();
        for (int i = 0, len = title.length() + 3; i < len; ++i)
            straightLine.append("-");

        print("\n");
        print("-- " + straightLine.toString());
        print("-- " + title + " --");
        print("-- " + straightLine.toString());
        print("");
    }

    public static void print(String message) {
        if (printWriter != null) {
            printWriter.println(message);
        }
    }

    public static void resetSequence(EntityManager manager, Collection<Class<?>> entityClasses) {
        for (Class<?> entityClass : entityClasses) {
            resetSequence(manager, entityClass);
        }
    }

    public static void resetSequence(EntityManager manager, Class<?> entityClass) {
        String sequenceName;
        try {
            Field sequenceNameField = entityClass.getDeclaredField("SEQUENCE_NAME");
            sequenceName = (String) sequenceNameField.get(null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        long count = getEntityCount(manager, entityClass);
        print("ALTER SEQUENCE " + sequenceName + " START WITH " + (count + 1));
    }

    public static long getEntityCount(EntityManager manager, Class<?> entityClass) {
        Table table = entityClass.getDeclaredAnnotation(Table.class);
        Query query = manager.createNativeQuery("SELECT COUNT(*) FROM " + table.name().toUpperCase());
        Object result = query.getSingleResult();
        return ((BigDecimal) result).longValue();
    }

    public static void turnOff() {
        if (printWriter != null) {
            printWriter.flush();
            printWriter.close();
            printWriter = null;
        }
    }
}
