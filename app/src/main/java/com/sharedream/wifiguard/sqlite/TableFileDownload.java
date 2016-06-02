package com.sharedream.wifiguard.sqlite;

/**
 * 文件下载记录表
 */
public class TableFileDownload {
	public static final String TABLE_NAME = "Mappush_File_Download";	// 表名
	public static final String COLUMN_VC2_URL = "vc2Url"; // 下载路径
	public static final String COLUMN_VC2_FILENAME = "vc2Filename"; // 文件名
	public static final String COLUMN_VC2_SAVE_PATH = "vc2SavePath"; // 文件的存放路径
	public static final String COLUMN_VC2_FINISH_FLAG = "vc2FinishFlag"; // 文件是否下载完成，"Y"代表完成，"N"代表未完成
	public static final String COLUMN_INT_READ_LENGTH = "intReadLength"; // 已读取的文件长度
	public static final String COLUMN_INT_TOTAL_LENGTH = "intTotalLength"; // 文件的总长度
	public static final String COLUMN_LONG_DOWNLOAD_FIRST_TIME = "longDownloadFirstTime"; // 第一次下载的时间
	public static final String COLUMN_LONG_DOWNLOAD_LAST_TIME = "longDownloadLastTime"; // 最近的下载时间
	
	private static final String [] COLUMNS_TITLE = {
		COLUMN_VC2_URL,
		COLUMN_VC2_FILENAME,
		COLUMN_VC2_SAVE_PATH,
		COLUMN_VC2_FINISH_FLAG,
		COLUMN_INT_READ_LENGTH,
		COLUMN_INT_TOTAL_LENGTH,
		COLUMN_LONG_DOWNLOAD_FIRST_TIME,
		COLUMN_LONG_DOWNLOAD_LAST_TIME
	};
	private static final String [] COLUMES_TYPE = {
		"varchar",
		"varchar", 
		"varchar",
		"varchar", 
		"INTEGER",
		"INTEGER",
		"LONG",
		"LONG"
	};
	
	public static String getCreateTableSQL() {
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS ");
		sql.append(TABLE_NAME);
		sql.append(" ( ");
		
		for (int k = 0; k < COLUMNS_TITLE.length; k++) {
			sql.append(COLUMNS_TITLE[k]);
			sql.append(" ");
			sql.append(COLUMES_TYPE[k]);
			sql.append(",");
		}
		
		sql.deleteCharAt(sql.lastIndexOf(","));
		sql.append(" ); ");
		
		return sql.toString();
	}

	public static String getAlterTableSQL(int oldVersion) {
		switch (oldVersion) {
		case 1:
			break;
		}
		
		return null;
	}

}
