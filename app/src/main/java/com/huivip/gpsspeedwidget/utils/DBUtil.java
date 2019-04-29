package com.huivip.gpsspeedwidget.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.huivip.gpsspeedwidget.beans.LocationVO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sunlaihui
 */
public class DBUtil extends SQLiteOpenHelper {
    private static final String dbName = "GPSHistory.db";

    private static final int version = 1;
    private static final String tableName="GPS";
    public DBUtil(Context context){
        super(context, dbName,null,version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+tableName+" (id integer primary key autoincrement," +
                "deviceId varchar(50), lng varchar(20), lat varchar(20),speed varchar(10),speedValue REAL," +
                "bearingValue REAL, createTime integer,lineId integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insert(String deviceId,String lng, String lat,String speed,double speedValue,double bearingValue, Date date,long lineId) {
        SQLiteDatabase db=null;
        try {
            ContentValues cv = new ContentValues();
            cv.put("deviceId", deviceId);
            cv.put("lng", lng);
            cv.put("lat", lat);
            cv.put("speed", speed);
            cv.put("speedValue", speedValue);
            cv.put("bearingValue", bearingValue);
            cv.put("createTime", date.getTime());
            cv.put("lineId", lineId);
            db = getWritableDatabase();
            db.insertOrThrow(tableName, null, cv);
        }catch (Exception e){
            Log.d("huivip","insert data failed");
        }finally {
            if(db!=null){
                db.close();
            }
        }
    }

    public void delete(Date fromDate){
        SQLiteDatabase db=null;
        try {
            String sql = "delete from " + tableName + " where createTime <" + fromDate.getTime();
            db = getWritableDatabase();
            db.execSQL(sql);
            sql = "VACUUM";
            db.execSQL(sql);
        }catch (Exception e){
            Log.d("huivip","delete history data failed");
        }finally {
            if(db!=null){
                db.close();
            }
        }
    }
    public List<LocationVO> getLastedData(String limitNumber){
        List<LocationVO> list=new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor=null;
        try {
            cursor=db.query(tableName, new String[]{"lng", "lat","speed","speedValue","bearingValue","createTime","lineId"},
                    null,null, null, null, "createTime DESC", limitNumber);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    LocationVO vo = new LocationVO();
                    vo.setLng(cursor.getString(0));
                    vo.setLat(cursor.getString(1));
                    vo.setSpeed(cursor.getString(2));
                    vo.setSpeedValue(cursor.getDouble(3));
                    vo.setBearingValue(cursor.getFloat(4));
                    vo.setCreateTime(cursor.getLong(5));
                    vo.setLineId(cursor.getLong(6));
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
    public List<LocationVO> getBetweenDate(Date fromDate,Date toDate){
        Cursor cursor=null;
        SQLiteDatabase db = getReadableDatabase();
        List<LocationVO> list = new ArrayList<>();
        try {
            cursor=db.query(tableName, new String[]{"lng", "lat","speed","speedValue","bearingValue","createTime","lineId"}, "createTime>? and createTime<?",
                    new String[]{String.valueOf(fromDate.getTime()),String.valueOf(toDate.getTime())}, null, null, "createTime");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    LocationVO vo = new LocationVO();
                    vo.setLng(cursor.getString(0));
                    vo.setLat(cursor.getString(1));
                    vo.setSpeed(cursor.getString(2));
                    vo.setSpeedValue(cursor.getDouble(3));
                    vo.setBearingValue(cursor.getFloat(4));
                    vo.setCreateTime(cursor.getLong(5));
                    vo.setLineId(cursor.getLong(6));
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
    public List<LocationVO> getFromDate(Date fromDate){
        Cursor cursor=null;
        SQLiteDatabase db = getReadableDatabase();
        List<LocationVO> list = new ArrayList<>();
        try {
            cursor=db.query(tableName, new String[]{"lng", "lat","speed","speedValue","bearingValue","createTime","lineId"}, "createTime<?",
                    new String[]{String.valueOf(fromDate.getTime())}, null, null, "createTime");
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    LocationVO vo = new LocationVO();
                    vo.setLng(cursor.getString(0));
                    vo.setLat(cursor.getString(1));
                    vo.setSpeed(cursor.getString(2));
                    vo.setSpeedValue(cursor.getDouble(3));
                    vo.setBearingValue(cursor.getFloat(4));
                    vo.setCreateTime(cursor.getLong(5));
                    vo.setLineId(cursor.getLong(6));
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
