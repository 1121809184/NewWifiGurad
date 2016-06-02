package com.sharedream.wifiguard.sqlite;


public class TableUser {
    public static final String TABLE_NAME = "user";
    public static final String COLUMN_USER_ID = "userid";
    public static final String COLUMN_PASSWD = "passwd";
    public static final String COLUMN_MODE = "mode";

    private static final String[] COLUMN_TITLES = {
            COLUMN_USER_ID,
            COLUMN_PASSWD,
            COLUMN_MODE
    };

    private static final String[] COLUMN_TYPES = {
            "VARCHAR",
            "VARCHAR",
            "int"
    };

    public static String getCreateTableSQL() {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(TABLE_NAME);
        sql.append("(");
        for (int i = 0; i < COLUMN_TITLES.length; i++) {
            sql.append(COLUMN_TITLES[i]);
            sql.append(" ");
            sql.append(COLUMN_TYPES[i]);
            sql.append(",");
        }
        sql.deleteCharAt(sql.lastIndexOf(","));
        sql.append(");");
        return sql.toString();
    }
}
