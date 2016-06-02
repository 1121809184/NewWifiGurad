package com.sharedream.wifiguard.sqlite;


public class TableBigScene {

    public static final String TABLE_NAME = "BigScene";
    public static final String COLUMN_SCENE_ID = "sceneId";
    public static final String COLUMN_SCENE_NAME = "sceneName";

    private static final String[] COLUMN_TITLES = {
            COLUMN_SCENE_ID,
            COLUMN_SCENE_NAME
    };

    private static final String[] COLUMN_TYPES = {
            "VARCHAR",
            "VARCHAR"
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
