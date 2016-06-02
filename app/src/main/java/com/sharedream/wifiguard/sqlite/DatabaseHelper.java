package com.sharedream.wifiguard.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sharedream.wifiguard.app.AppContext;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "wifiguard.db";
    public static final int DB_VERSION = 1;

    private static DatabaseHelper instance;
    private static SQLiteDatabase db;

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(AppContext.getContext());
                    db = instance.getWritableDatabase();
                }
            }
        }
        return instance;
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(TableBigScene.getCreateTableSQL());
        db.execSQL(TableSmallScene.getCreateTableSQL());
        db.execSQL(TableUser.getCreateTableSQL());
        db.execSQL(TableFileDownload.getCreateTableSQL());
    }
}
