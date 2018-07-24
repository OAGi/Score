package org.oagi.srt.persistence.populate.script.hsqldb;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import java.io.*;
import java.math.BigDecimal;

public class HsqldbDataImportScriptPrinter {

    private static PrintWriter printWriter;

    public static void turnOn() {
        if (printWriter != null) {
            turnOff();
        }

        try {
            printWriter = new PrintWriter(
                    new OutputStreamWriter(
                            new BufferedOutputStream(
                                    new FileOutputStream("./srt-import/src/main/resources/data-hsqldb.sql"))));
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
            printWriter.flush();
        }
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
