package com.sharedream.wifiguard.sqlite;


import android.content.ContentValues;
import android.database.Cursor;

import com.sharedream.wifiguard.cmdws.CmdShopCategory;
import com.sharedream.wifiguard.version.FileDownload;
import com.sharedream.wifiguard.vo.UserVo;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    public static long insertUser(String userid, String passwd, int mode) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableUser.COLUMN_USER_ID, userid);
        contentValues.put(TableUser.COLUMN_PASSWD, passwd);
        contentValues.put(TableUser.COLUMN_MODE, mode);
        return DatabaseHelper.getInstance().getDatabase().insert(TableUser.TABLE_NAME, null, contentValues);
    }

    public static UserVo queryUser() {
        Cursor cursor = DatabaseHelper.getInstance().getDatabase().query(TableUser.TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            UserVo userVo = new UserVo();
            if (cursor.moveToFirst()) {
                String userid = cursor.getString(cursor.getColumnIndex(TableUser.COLUMN_USER_ID));
                String pwd = cursor.getString(cursor.getColumnIndex(TableUser.COLUMN_PASSWD));
                int mode = cursor.getInt(cursor.getColumnIndex(TableUser.COLUMN_MODE));
                userVo.userid = userid;
                userVo.passwd = pwd;
                userVo.mode = mode;
                cursor.close();
                return userVo;
            }
        }
        return null;
    }

    public static boolean logout() {
        int line = DatabaseHelper.getInstance().getDatabase().delete(TableUser.TABLE_NAME, null, null);
        return line > 0;
    }

    public static long insertBigScene(int id, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableBigScene.COLUMN_SCENE_ID, id);
        contentValues.put(TableBigScene.COLUMN_SCENE_NAME, name);
        return DatabaseHelper.getInstance().getDatabase().insert(TableBigScene.TABLE_NAME, null, contentValues);
    }

    public static long insertSmallScene(int id, String name, int parentId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableSmallScene.COLUMN_SCENE_ID, id);
        contentValues.put(TableSmallScene.COLUMN_SCENE_NAME, name);
        contentValues.put(TableSmallScene.COLUMN_SCENE_PARENT_ID, parentId);
        return DatabaseHelper.getInstance().getDatabase().insert(TableSmallScene.TABLE_NAME, null, contentValues);
    }

    public static boolean deleteAllBigScene() {
        int line = DatabaseHelper.getInstance().getDatabase().delete(TableBigScene.TABLE_NAME, null, null);
        return line > 0;
    }

    public static boolean deleteAllSmallScene() {
        int line = DatabaseHelper.getInstance().getDatabase().delete(TableSmallScene.TABLE_NAME, null, null);
        return line > 0;
    }

    public static List<CmdShopCategory.BigCategory> queryAllBigScene() {
        Cursor cursor = DatabaseHelper.getInstance().getDatabase().query(TableBigScene.TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            List<CmdShopCategory.BigCategory> bigScenes = new ArrayList<CmdShopCategory.BigCategory>();
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(TableBigScene.COLUMN_SCENE_ID));
                String name = cursor.getString(cursor.getColumnIndex(TableBigScene.COLUMN_SCENE_NAME));
                CmdShopCategory.BigCategory bigScene = new CmdShopCategory.BigCategory();
                bigScene.id = Integer.parseInt(id);
                bigScene.name = name;
                bigScenes.add(bigScene);
            }
            cursor.close();
            return bigScenes;
        }
        return null;
    }

    public static List<CmdShopCategory.SmallCategory> queryAllSmallScene(int bigSceneId) {
        String selection = TableSmallScene.COLUMN_SCENE_PARENT_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(bigSceneId)};
        Cursor cursor = DatabaseHelper.getInstance().getDatabase().query(TableSmallScene.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            List<CmdShopCategory.SmallCategory> smallSceneList = new ArrayList<CmdShopCategory.SmallCategory>();
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(TableSmallScene.COLUMN_SCENE_ID));
                String name = cursor.getString(cursor.getColumnIndex(TableSmallScene.COLUMN_SCENE_NAME));
                CmdShopCategory.SmallCategory smallScene = new CmdShopCategory.SmallCategory();
                smallScene.id = Integer.parseInt(id);
                smallScene.name = name;
                smallSceneList.add(smallScene);
            }
            cursor.close();
            return smallSceneList;
        }
        return null;
    }

    public static String findBigSceneNameById(String categoryId) {

        String selection = TableBigScene.COLUMN_SCENE_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(categoryId)};
        Cursor cursor = DatabaseHelper.getInstance().getDatabase().query(TableBigScene.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToFirst()) {
                String bigSceneName = cursor.getString(cursor.getColumnIndex(TableBigScene.COLUMN_SCENE_NAME));
                cursor.close();
                return bigSceneName;
            }
        }
        return null;
    }

    public static String[] findSceneNameById(int categoryId) {
        String[] categoryGroup = new String[4];
        String selection = TableSmallScene.COLUMN_SCENE_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(categoryId)};
        Cursor cursor = DatabaseHelper.getInstance().getDatabase().query(TableSmallScene.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                String smallSceneName = cursor.getString(cursor.getColumnIndex(TableSmallScene.COLUMN_SCENE_NAME));
                categoryGroup[0] = String.valueOf(categoryId);
                categoryGroup[1] = smallSceneName;
                String bigSceneId = cursor.getString(cursor.getColumnIndex(TableSmallScene.COLUMN_SCENE_PARENT_ID));
                String bigSceneName = findBigSceneNameById(bigSceneId);
                categoryGroup[2] = bigSceneId;
                categoryGroup[3] = bigSceneName;
                cursor.close();
                return categoryGroup;
            }
        }
        return null;
    }

    public static void updateFileDownloadData(String url, String finishFlag, long readLength) {
        ContentValues values = new ContentValues();
        values.put(TableFileDownload.COLUMN_INT_READ_LENGTH, readLength);
        values.put(TableFileDownload.COLUMN_VC2_FINISH_FLAG, finishFlag);
        values.put(TableFileDownload.COLUMN_LONG_DOWNLOAD_LAST_TIME, System.currentTimeMillis());
        String where = TableFileDownload.COLUMN_VC2_URL + " = ?";
        String[] whereArgs = {
                url
        };
        DatabaseHelper.getInstance().getDatabase().update(TableFileDownload.TABLE_NAME, values, where, whereArgs);
    }

    public static void insertFileDownloadData(String url, String filename, String savePath, String finishFlag, long readLength, long totalLength) {
        String where = TableFileDownload.COLUMN_VC2_URL + " = ?";
        String[] whereArgs = {
                url
        };
        DatabaseHelper.getInstance().getDatabase().delete(TableFileDownload.TABLE_NAME, where, whereArgs);

        ContentValues values = new ContentValues();
        values.put(TableFileDownload.COLUMN_VC2_URL, url);
        values.put(TableFileDownload.COLUMN_VC2_FILENAME, filename);
        values.put(TableFileDownload.COLUMN_VC2_SAVE_PATH, savePath);
        values.put(TableFileDownload.COLUMN_VC2_FINISH_FLAG, finishFlag);
        values.put(TableFileDownload.COLUMN_INT_READ_LENGTH, readLength);
        values.put(TableFileDownload.COLUMN_INT_TOTAL_LENGTH, totalLength);
        values.put(TableFileDownload.COLUMN_LONG_DOWNLOAD_FIRST_TIME, System.currentTimeMillis());
        values.put(TableFileDownload.COLUMN_LONG_DOWNLOAD_LAST_TIME, System.currentTimeMillis());
        DatabaseHelper.getInstance().getDatabase().insert(TableFileDownload.TABLE_NAME, null, values);
    }

    public static FileDownload queryFileDownloadDataByUrl(String url) {
        final long twoDayTime = 1000 * 60 * 60 * 48;
        final long curTime = System.currentTimeMillis();
        final long time48HourBefore = curTime - twoDayTime;

        FileDownload data = null;
        String[] whereArgs = {
                url
        };

        Cursor cursor = DatabaseHelper.getInstance().getDatabase().rawQuery("select * from " + TableFileDownload.TABLE_NAME + " where "
                + TableFileDownload.COLUMN_VC2_URL + " = ? and " + TableFileDownload.COLUMN_LONG_DOWNLOAD_FIRST_TIME + " > " + time48HourBefore, whereArgs);
        if (cursor.moveToFirst()) {

            data = new FileDownload();
            data.setUrl(cursor.getString(cursor.getColumnIndex(TableFileDownload.COLUMN_VC2_URL)));
            data.setFilename(cursor.getString(cursor.getColumnIndex(TableFileDownload.COLUMN_VC2_FILENAME)));
            data.setSavePath(cursor.getString(cursor.getColumnIndex(TableFileDownload.COLUMN_VC2_SAVE_PATH)));
            data.setFinishFlag(cursor.getString(cursor.getColumnIndex(TableFileDownload.COLUMN_VC2_FINISH_FLAG)));
            data.setReadLength(cursor.getLong(cursor.getColumnIndex(TableFileDownload.COLUMN_INT_READ_LENGTH)));
            data.setTotalLength(cursor.getLong(cursor.getColumnIndex(TableFileDownload.COLUMN_INT_TOTAL_LENGTH)));
            data.setFirstTime(cursor.getLong(cursor.getColumnIndex(TableFileDownload.COLUMN_LONG_DOWNLOAD_FIRST_TIME)));
            data.setLastTime(cursor.getLong(cursor.getColumnIndex(TableFileDownload.COLUMN_LONG_DOWNLOAD_LAST_TIME)));
        }
        cursor.close();
        return data;
    }
}
