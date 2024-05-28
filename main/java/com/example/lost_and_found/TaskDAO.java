package com.example.lost_and_found;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    private SQLiteDatabase db;

    public TaskDAO(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long addTask(TaskClass taskClass) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, taskClass.getName());
        values.put(DBHelper.COLUMN_PHONE, taskClass.getPhone());
        values.put(DBHelper.COLUMN_DESCRIPTION, taskClass.getDescription());
        values.put(DBHelper.COLUMN_DATE, taskClass.getDate());
        values.put(DBHelper.COLUMN_LOCATION, taskClass.getLocation());
        values.put(DBHelper.COLUMN_TYPE, taskClass.getType());
        values.put(DBHelper.COLUMN_LATITUDE, taskClass.getLatitude());
        values.put(DBHelper.COLUMN_LONGITUDE, taskClass.getLongitude());

        long id = db.insert(DBHelper.TABLE_NAME, null, values);
        Log.d("TaskDAO", "Inserted Task with ID: " + id + " and LatLng: (" + taskClass.getLatitude() + ", " + taskClass.getLongitude() + ")");
        return id;
    }

    @SuppressLint("Range")
    public List<TaskClass> getAllTasks() {
        List<TaskClass> taskClasses = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TaskClass taskClass = new TaskClass();
                taskClass.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
                taskClass.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
                taskClass.setPhone(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PHONE)));
                taskClass.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));
                taskClass.setDate(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE)));
                taskClass.setLocation(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION)));
                taskClass.setType(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TYPE)));
                taskClass.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LATITUDE)));
                taskClass.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LONGITUDE)));

                Log.d("TaskDAO", "Retrieved Task: " + taskClass.toString());
                taskClasses.add(taskClass);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return taskClasses;
    }

    @SuppressLint("Range")
    public TaskClass getTask(int id) {
        Cursor cursor = db.query(DBHelper.TABLE_NAME, null, DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            TaskClass taskClass = new TaskClass();
            taskClass.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID)));
            taskClass.setName(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
            taskClass.setPhone(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_PHONE)));
            taskClass.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_DESCRIPTION)));
            taskClass.setDate(cursor.getLong(cursor.getColumnIndex(DBHelper.COLUMN_DATE)));
            taskClass.setLocation(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_LOCATION)));
            taskClass.setType(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_TYPE)));
            taskClass.setLatitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LATITUDE)));
            taskClass.setLongitude(cursor.getDouble(cursor.getColumnIndex(DBHelper.COLUMN_LONGITUDE)));

            cursor.close();
            return taskClass;
        }
        return null;
    }

    public void deleteTask(int id) {
        db.delete(DBHelper.TABLE_NAME, DBHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
