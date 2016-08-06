package com.bdlions.sampanit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



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
  public boolean createUser(String userId, String userNmae, String password, String opcode){
     SQLiteDatabase db = this.getWritableDatabase();
     ContentValues contentValues = new ContentValues();
     contentValues.put(QueryField.USER_ID,userId);
     contentValues.put(QueryField.USER_NMAE,userNmae);
     contentValues.put(QueryField.PASSWORD,password);
     contentValues.put(QueryField.OP_CODE,opcode);
     long result = db.insert(QueryField.TABLE_USERS, null ,contentValues);
      db.close();
      if(result == -1)
          return false;
      else
          return true;
  }
    public Cursor getUserInfo() {
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        db.close();
        return cursor;
    }






}
