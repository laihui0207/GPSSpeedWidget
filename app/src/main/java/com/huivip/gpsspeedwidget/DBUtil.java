package com.huivip.gpsspeedwidget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sunlaihui
 */
public class DBUtil extends SQLiteOpenHelper {
    private static final String dbName = "GPSWidget.db";

    private static final int version = 1;
    private static final String tableName="GPSHistory";
    public DBUtil(Context context){
        super(context, dbName,null,version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+" (id integer primary key autoincrement," +
                " lng varchar(20), lat varchar(20),createTime integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insert(String lng, String lat, Date date) {
        ContentValues cv = new ContentValues();
        cv.put("lng", lng);
        cv.put("lat", lat);
        cv.put("createTime", date.getTime());
        SQLiteDatabase db = getWritableDatabase();
        db.insertOrThrow(tableName, null, cv);
        db.close();
    }

    public void delete(Date fromDate){
        String sql="delete from "+tableName+" where createTime <"+fromDate.getTime();
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }
    public List<LocationVO> getFromDate(Date fromDate){
        Cursor cursor=null;
        SQLiteDatabase db = getReadableDatabase();
        List<LocationVO> list = new ArrayList<>();
        try {
            cursor=db.query(tableName, new String[]{"lng", "lat", "createTime"}, "createTime<?",
                    new String[]{String.valueOf(fromDate.getTime())}, null, null, "createTime");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    LocationVO vo = new LocationVO();
                    vo.setLng(cursor.getString(0));
                    vo.setLat(cursor.getString(1));
                    vo.setCreateTime(cursor.getLong(2));
                    list.add(vo);
                }
            }
        } catch(Exception e){

        } finally {
            if(null!=cursor){
                cursor.close();
            }
            db.close();
        }
        return list;
    }
}