package com.obito.keeplib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.obito.keeplib.KeepTask;

import java.util.ArrayList;
import java.util.List;

public class KeepTaskDao {
    private SQLiteDatabase database;
    private KeepDBHelper dbHelper;

    public KeepTaskDao(Context context) {
        dbHelper = new KeepDBHelper(context);
        database = dbHelper.getWritableDatabase();
    }


    public void addTask(KeepTask task) {
        if (task == null) return;
        insertOrReplaceTask(task);
    }

    public void deleteTask(KeepTask task) {
        if (task == null) return;
        database.delete(KeepDBHelper.TASK_TABLE_NAME,KeepDBHelper.Properties.URL + "=?",new String[]{task.getUrl()});
    }

    public List<KeepTask> getAllTask() {
        List<KeepTask> list = new ArrayList<>();
        Cursor cursor = database.query(KeepDBHelper.TASK_TABLE_NAME,null,null,null,null,null,null);
        try {
            if (cursor != null && cursor.moveToFirst()){
                do {
                    KeepTask task = new KeepTask();
                    task.readEntity(cursor);
                    list.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e){

        } finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return list;
    }

    public void updateTask(KeepTask task) {
        if (task == null) return;
        insertOrReplaceTask(task);
    }


    private void insertOrReplaceTask(KeepTask task) {
        Cursor cursor = database.query(KeepDBHelper.TASK_TABLE_NAME, null, KeepDBHelper.Properties.URL + "=?"
                , new String[]{task.getUrl()}, null, null, null);
        boolean needInsert = true;
        ContentValues cv = task.getContentValues();
        try {
            if (cursor != null && cursor.getCount() > 0) {
                int row = database.update(KeepDBHelper.TASK_TABLE_NAME, cv, KeepDBHelper.Properties.URL + "=?", new String[]{task.getUrl()});
                if (row > 0) {
                    needInsert = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }

        if (needInsert){
            database.insert(KeepDBHelper.TASK_TABLE_NAME,null,cv);
        }
    }


}
