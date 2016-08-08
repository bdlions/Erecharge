package com.bdlions.sampanit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by sampanit on 14/06/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper sInstance = null;
    private Context context;

    public static DatabaseHelper getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return sInstance;
    }
    private DatabaseHelper(Context context) {
        super(context, QueryField.DATABASE_NAME, null, QueryField.DATABASE_VERSION);
        this.context = context;
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBQuery.create_user);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
    public boolean createUser(int userId, String userNmae, String password, String opcode, String baseUrl){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(QueryField.USER_ID,userId);
        contentValues.put(QueryField.USER_NMAE,userNmae);
        contentValues.put(QueryField.PASSWORD,password);
        contentValues.put(QueryField.OP_CODE,opcode);
        long result = db.insert(QueryField.TABLE_USERS, null, contentValues);
        db.close();
        if(result == -1)
            return false;
        else
            return true;
    }

    public JSONObject getUserInfo() {
        int localUserId = 0;
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
      /*  if(cursor.moveToFirst()){
            while (cursor.moveToFirst()){

            }
        }*/
        JSONObject userInfo = new JSONObject();
        if (cursor.moveToFirst()) {
            do {
                try {
                    userInfo.put("userId", cursor.getInt(1));
                    userInfo.put("userName", cursor.getString(2));
                    userInfo.put("opCode", cursor.getString(3));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userInfo;
    }

    public boolean checkLogin(){

        // Check login status
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery(selectQuery, null);

        if (!(mCursor.moveToFirst()) || mCursor.getCount() ==0){
            return false;
            //cursor is empty
        }
        mCursor.close();

        return true;
    }


   /* public void addServiceList(){
        String insertQuery = "INSERT INTO mytable (col1, col2, col3) VALUES (1, 2, 'abc'),(2, 4, 'xyz')";
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(insertQuery);

    }*/






}
