package com.bdlions.trustedload.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bdlions.trustedload.bean.OperatorInfo;
import com.bdlions.trustedload.bean.PackageInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
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
        try
        {
            db.execSQL(DBQuery.CREATE_TABLE_OPCODE);
            db.execSQL(DBQuery.CREATE_TABLE_USERS);
            db.execSQL(DBQuery.CREATE_TABLE_SERVICES);
            db.execSQL(DBQuery.CREATE_TABLE_OPERATORS);
            db.execSQL(DBQuery.CREATE_TABLE_PACKAGES);
            db.execSQL(DBQuery.CREATE_TABLE_CONTACTS);
        }
        catch(Exception ex)
        {
            System.out.println(ex.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

    /**
     * This method will delete opcode
     * @author nazmul hasan on 24th February 2017
     * */
    public void deleteOPCode()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(QueryField.TABLE_OPCODE, null, null);
        db.close();
    }
    /**
     * This method will insert opcode
     * @param opcode, opcode
     * @return boolean
     * @author nazmul hasan on 24th February 2017
     * */
    public boolean addOpcode(String opcode)
    {
        this.deleteOPCode();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(QueryField.OP_CODE, opcode);
        long result = db.insert(QueryField.TABLE_OPCODE, null, contentValues);
        db.close();
        if(result == -1)
            return false;
        else
            return true;
    }

    /**
     * This method will return opcode
     * @author nazmul hasan on 24th February 2017
     * */
    public String getOPCode()
    {
        String opCode = "";

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_OPCODE;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    opCode = cursor.getString(cursor.getColumnIndex(QueryField.OP_CODE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return opCode;
    }

    /**
     * This method will check whether a user exists or not into the database
     * @author nazmul hasan on 22nd december 2016
     * */
    public boolean isUserExists()
    {
        boolean isExists = true;
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_USERS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery(selectQuery, null);

        if (!(mCursor.moveToFirst()) || mCursor.getCount() == 0){
            isExists = false;
        }
        mCursor.close();
        return isExists;
    }

    /**
     * This method will insert a new user
     * @param userId, user id
     * @param userName, user name
     * @param password, password
     * @param baseUrl, base url
     * @param sessionId, session id
     * @param balance, balance
     * @param companyName, company name
     * @return boolean
     * @author nazmul hasan on 22nd december 2016
     * */
    public boolean createUser(int userId, String userName, String password, String pinCode, String baseUrl, String sessionId, double balance, String companyName)
    {
        //checking whether user exists or not before creating a new user
        if(this.isUserExists())
        {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(QueryField.USER_ID,userId);
        contentValues.put(QueryField.SESSION_ID, sessionId);
        contentValues.put(QueryField.USER_NMAE,userName);
        contentValues.put(QueryField.BASE_URL,baseUrl);
        contentValues.put(QueryField.PIN_CODE, pinCode);
        contentValues.put(QueryField.PASSWORD, password);
        contentValues.put(QueryField.BALANCE, balance);
        contentValues.put(QueryField.COMPANY_NAME, companyName);
        long result = db.insert(QueryField.TABLE_USERS, null, contentValues);
        db.close();
        if(result == -1)
            return false;
        else
            return true;
    }

    /**
     * This method will return user info
     * @reutnr JSONObject, user info
     * @author nazmul hasan on 22nd december 2016
     * */
    public JSONObject getUserInfo()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_USERS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        JSONObject userInfo = new JSONObject();
        if (cursor.moveToFirst()) {
            do {
                try {
                    userInfo.put("userId", cursor.getInt(cursor.getColumnIndex(QueryField.USER_ID)));
                    userInfo.put("sessionId", cursor.getString(cursor.getColumnIndex(QueryField.SESSION_ID)));
                    userInfo.put("userName", cursor.getString(cursor.getColumnIndex(QueryField.USER_NMAE)));
                    userInfo.put("baseUrl", cursor.getString(cursor.getColumnIndex(QueryField.BASE_URL)));
                    userInfo.put("password", cursor.getString(cursor.getColumnIndex(QueryField.PASSWORD)));
                    userInfo.put("pinCode", cursor.getString(cursor.getColumnIndex(QueryField.PIN_CODE)));
                    userInfo.put("balance", cursor.getDouble(cursor.getColumnIndex(QueryField.BALANCE)));
                    userInfo.put("companyName", cursor.getString(cursor.getColumnIndex(QueryField.COMPANY_NAME)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return userInfo;
    }

    /**
     * This method will update user name
     * @param userId, user id
     * @param userName, user name
     * @author nazmul hasan on 22nd december 2016
     * */
    public int updateUserName(int userId, String userName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateUserInfo = new ContentValues();
        updateUserInfo.put(QueryField.USER_NMAE, userName);
        // updating row
        return db.update(QueryField.TABLE_USERS, updateUserInfo, QueryField.USER_ID + " = ?",
                new String[] { String.valueOf(userId) });
    }

    /**
     * This method will update base url
     * @param userId, user id
     * @param baseURL, base url
     * @author nazmul hasan on 22nd december 2016
     * */
    public int updateBaseURL(int userId, String baseURL)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateUserInfo = new ContentValues();
        updateUserInfo.put(QueryField.BASE_URL, baseURL);
        // updating row
        return db.update(QueryField.TABLE_USERS, updateUserInfo, QueryField.USER_ID + " = ?",
                new String[] { String.valueOf(userId) });
    }

    /**
     * This method will update user pin
     * @param userId, user id
     * @param pinCode, pin
     * @author nazmul hasan on 22nd december 2016
     * */
    public int updatePinCode(int userId, String pinCode)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateUserInfo = new ContentValues();
        updateUserInfo.put(QueryField.PIN_CODE, pinCode);
        // updating row
        return db.update(QueryField.TABLE_USERS, updateUserInfo, QueryField.USER_ID + " = ?",
                new String[] { String.valueOf(userId) });
    }

    /**
     * This method will update user pin
     * @param userId, user id
     * @param balance, balance
     * @author nazmul hasan on 22nd december 2016
     * */
    public int updateBalance(int userId, double balance)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateUserInfo = new ContentValues();
        updateUserInfo.put(QueryField.BALANCE, balance);
        // updating row
        return db.update(QueryField.TABLE_USERS, updateUserInfo, QueryField.USER_ID + " = ?",
                new String[] { String.valueOf(userId) });
    }

    /**
     * This method will update session id
     * @param userId, user id
     @param sessionId, session id
     * @author nazmul hasan on 22nd december 2016
     * */
    public int updateSessionId(int userId, String sessionId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues updateUserInfo = new ContentValues();
        updateUserInfo.put(QueryField.SESSION_ID, sessionId);
        // updating row
        return db.update(QueryField.TABLE_USERS, updateUserInfo, QueryField.USER_ID + " = ?",
                new String[] { String.valueOf(userId) });
    }

    /**
     * This method will delete user info
     * @author nazmul hasan on 22nd december 2016
     * */
    public void deleteUserInfo()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(QueryField.TABLE_USERS, null, null);
        db.close();
    }


    public void addServices(int userId, int[] serviceIdList)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < serviceIdList.length; i++)
            {
                int serviceId = serviceIdList[i];
                values.put(QueryField.USER_ID, userId);
                values.put(QueryField.SERVICE_ID, serviceId);
                db.insert(QueryField.TABLE_SERVICES, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Integer> getAllServices()
    {
        List<Integer> serviceIdList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_SERVICES;
        Cursor cursor = db.rawQuery(selectQuery, null);
        JSONObject userInfo = new JSONObject();
        if (cursor.moveToFirst()) {
            do {
                try {
                    int serviceId = cursor.getInt(cursor.getColumnIndex(QueryField.SERVICE_ID));
                    serviceIdList.add(serviceId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return serviceIdList;
    }

    public void deleteAllServices()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(QueryField.TABLE_SERVICES, null, null);
        db.close();
    }

    /**
    * This method will insert operator list
    * @param operatorList operator list in json array
    * @author nazmul hasan on 17th february 2017
    */
    public void addOperators(JSONArray operatorList)
    {
        //at first deleting existing entries
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(QueryField.TABLE_OPERATORS, null, null);
        db.close();

        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < operatorList.length(); i++)
            {
                JSONObject operatorInfo = operatorList.getJSONObject(i);
                values.put(QueryField.ID, Integer.parseInt((String)operatorInfo.get("id")));
                values.put(QueryField.TITLE, (String)operatorInfo.get("title"));
                db.insert(QueryField.TABLE_OPERATORS, null, values);
            }
            db.setTransactionSuccessful();
        }
        catch(Exception ex)
        {
            //handle exception
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * This method will insert package list
     * @param packageList package list in json array
     * @author nazmul hasan on 17th february 2017
     */
    public void addPackages(JSONArray packageList)
    {
        //at first deleting existing entries
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(QueryField.TABLE_PACKAGES, null, null);
        db.close();

        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (int i = 0; i < packageList.length(); i++)
            {
                JSONObject packageInfo = packageList.getJSONObject(i);
                values.put(QueryField.ID, Integer.parseInt((String)packageInfo.get("id")));
                values.put(QueryField.TITLE, (String)packageInfo.get("title"));
                values.put(QueryField.OPERATOR_ID, Integer.parseInt((String)packageInfo.get("operator_id")));
                values.put(QueryField.AMOUNT, Double.parseDouble((String)packageInfo.get("amount")));
                db.insert(QueryField.TABLE_PACKAGES, null, values);
            }
            db.setTransactionSuccessful();
        }
        catch(Exception ex)
        {
            //handle exception
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * This method will return operator list
     * @return ArrayList operator list
     * @author nazmul hasan on 17th february 2017
     */
    public ArrayList<OperatorInfo> getAllOperators()
    {
        ArrayList<OperatorInfo> operatorList = new ArrayList<>();
        operatorList.add(new OperatorInfo(0, "Select"));

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_OPERATORS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndex(QueryField.ID));
                    String title = cursor.getString(cursor.getColumnIndex(QueryField.TITLE));
                    operatorList.add(new OperatorInfo(id, title));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return operatorList;
    }

    /**
     * This method will return package list
     * @return ArrayList package list
     * @author nazmul hasan on 17th february 2017
     */
    public ArrayList<PackageInfo> getAllPackages(int operatorId)
    {
        ArrayList<PackageInfo> packageList = new ArrayList<>();
        packageList.add(new PackageInfo(0, 0, "Select", 0));
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_PACKAGES + " where "+QueryField.OPERATOR_ID + " = "+operatorId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndex(QueryField.ID));
                    String title = cursor.getString(cursor.getColumnIndex(QueryField.TITLE));
                    double amount = cursor.getDouble(cursor.getColumnIndex(QueryField.AMOUNT));
                    packageList.add(new PackageInfo(id, operatorId, title, amount));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return packageList;
    }

    public boolean isContactExists(String title)
    {
        boolean isExists = true;
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_CONTACTS + " where title = '" + title + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery(selectQuery, null);
        if (!(mCursor.moveToFirst()) || mCursor.getCount() == 0){
            isExists = false;
        }
        mCursor.close();
        return isExists;
    }

    public boolean addContact(String title)
    {
        if(this.isContactExists(title))
        {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(QueryField.TITLE,title);
        long result = db.insert(QueryField.TABLE_CONTACTS, null, contentValues);
        db.close();
        if(result == -1)
            return false;
        else
            return true;
    }

    public ArrayList<String> getAllContacts()
    {
        ArrayList<String> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  * FROM " + QueryField.TABLE_CONTACTS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    String title = cursor.getString(cursor.getColumnIndex(QueryField.TITLE));
                    if(!contactList.contains(title))
                    {
                        contactList.add(title);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contactList;
    }

    public int getTotalContacts()
    {
        int totalContacts = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT  count(*) as total_contacts FROM " + QueryField.TABLE_CONTACTS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                try
                {
                    totalContacts = cursor.getInt(cursor.getColumnIndex("total_contacts"));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return totalContacts;
    }
}
