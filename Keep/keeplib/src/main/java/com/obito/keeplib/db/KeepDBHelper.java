package com.obito.keeplib.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KeepDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "keep.db";
    private static final String TASK_TABLE_NAME = "keep_task";

    interface Properties {
        String ID = "_id";
        String URL = "_url";
        String LOCAL_PATH = "_local_path";
        String FILE_TOTAL_SIZE = "_file_total_size";
        String DOWNLOADED_SIZE = "_downloaded_size";
        String FILE_TYPE = "_file_type";
        String STATUS = "_status";
        String PRIORITY = "_priority";
        String PROGRESS = "_progress";
    }

    private static final String CREATE_TASK_DB = "CREATE TABLE " + TASK_TABLE_NAME + " ( "
            + Properties.ID + " INTEGER PRIMARY KEY NOT NULL ,"
            + Properties.URL + " TEXT,"
            + Properties.LOCAL_PATH + " TEXT,"
            + Properties.FILE_TOTAL_SIZE + " BIGINT DEFAULT 0,"
            + Properties.DOWNLOADED_SIZE + " BIGINT DEFAULT 0,"
            + Properties.FILE_TYPE + " INT DEFAULT 0, "
            + Properties.STATUS + " INT DEFAULT 0, "
            + Properties.PRIORITY + " INT DEFAULT 0, "
            + Properties.PROGRESS + " INT DEFAULT 0 "
            + ")";

    private static final String CREATE_TASK_INDEX = "CREATE INDEX IDX_URL_STATUS ON " + TASK_TABLE_NAME
            + " (" + Properties.URL + ", " + Properties.STATUS + ")";

    public KeepDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version, DatabaseErrorHandler errorHandler) {
        super(context, DB_NAME, factory, DB_VERSION, errorHandler);
    }

    public KeepDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(CREATE_TASK_DB);
            sqLiteDatabase.execSQL(CREATE_TASK_INDEX);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
