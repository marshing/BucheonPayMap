package com.malangyee.bucheonpaymap;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class DBManager {

    public static final String ROOT_DIR = "/data/data/com.malangyee.bucheonpaymap/";
    private static final String DATABASE_NAME = "testdb.db";
    public static final String TABLE_NAME = "testdb";
    private static final String COLUMN_KEY_ID = "_id";
    private static final String COLUMN_NAME = "name";

    private Context context;
    private SQLiteDatabase mDatabase;

    public DBManager(Context context) {
        this.context = context;
        setDB(context);
    }


    public static void setDB(Context context) {
        File folder = new File(ROOT_DIR + "databases");
        if (folder.exists()) {
        } else {
            folder.mkdirs();
        }
        Log.d(TAG, "setDB: "+context.getFilesDir().getPath() + "/" + DATABASE_NAME);
        File outfile = new File(context.getFilesDir().getPath() + "/" + DATABASE_NAME);
        if (outfile.length() <= 0) {
            AssetManager assetManager = context.getResources().getAssets();
            try {
                InputStream is = assetManager.open(DATABASE_NAME, AssetManager.ACCESS_BUFFER);
                long filesize = is.available();
                byte[] tempdate = new byte[(int) filesize];
                is.read(tempdate);
                is.close();

                outfile.createNewFile();
                FileOutputStream fo = new FileOutputStream(outfile);
                fo.write(tempdate);
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DBManager open() {
        if(mDatabase == null){
            File databaseFile = new File(context.getFilesDir().getPath() + "/" + DATABASE_NAME);
            mDatabase = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        }
        return this;
    }

    public void close(){
        if(mDatabase !=null && mDatabase.isOpen()){
            mDatabase.close();
        }
    }

    public List<Location> getNearByLocation(double myLat, double myLng, double distance){
        List<Location> resultList = new ArrayList<>();
        String selectQuery = "SELECT *"
                            + ", " + buildDistanceQuery(myLat, myLng)
                            + " AS distance"
                            + " FROM " + TABLE_NAME
                            + " WHERE distance > "  + Math.cos(distance/6371);
//        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        Log.d(TAG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@sql : " + selectQuery);
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                Location location = new Location();
                location.set_no(cursor.getInt(0));
                location.setName(cursor.getString(1));
                location.setDong(cursor.getString(2));
                location.setAddress(cursor.getString(3));
                location.setLng(cursor.getDouble(4));
                location.setLat(cursor.getDouble(5));
                resultList.add(location);
            } while(cursor.moveToNext());
        }

        Log.d(TAG, "getNearByLocation: LENGTH : " + resultList.size());

        return resultList;
    }

    public static String buildDistanceQuery(double lat, double lng){
        final double coslat = Math.cos(deg2rad(lat));
        final double sinlat = Math.sin(deg2rad(lat));
        final double coslng = Math.cos(deg2rad(lng));
        final double sinlng = Math.sin(deg2rad(lng));

        return "(" + coslat + "*COS_LAT"
                + "*(COS_LON*(" + coslng + ")"
                + "+SIN_LON" + "*" +sinlng
                + ")+" + sinlat + "*SIN_LAT"
                + ")" ;

    }

    public static double deg2rad(double deg){
        return (double)(deg*Math.PI / (double)180d);
    }

//
//    public void setAdapter(ListView listview){
//
//        Cursor cursor = null;
//        final CursorAdapter adapter;
//        String[] columns = new String[] {COLUMN_NAME};
//
//        cursor = mDatabase.query(TABLE_NAME, columns, null, null,  null,  null,  null,  null);
//        adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor, new String[] {COLUMN_NAME}, new int[] {android.R.id.text1});
//        listview.setAdapter(adapter);
//    }


    //이곳에 public으로 쿼리코드 생성
}
