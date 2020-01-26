package com.mimik.smarthome.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mimik.smarthome.Model.CAdmin;

import java.util.ArrayList;
import java.util.HashMap;


    public class MDBHandler extends SQLiteOpenHelper {

        protected static final String TAG = "DataAdapter";
        private static final int DB_VERSION = 1;
        private static final String DB_NAME = "SmartHomedbTest";
        private static final String TABLE_Admins = "AdminInfo";

        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";
        public static final String KEY_PASS = "pass";

        public MDBHandler(Context context){
            super(context,DB_NAME, null, DB_VERSION);

        }
        @Override
        public void onCreate(SQLiteDatabase db){
            String CREATE_TABLE = "CREATE TABLE " + TABLE_Admins + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_NAME + " TEXT,"
                    + KEY_PASS + " TEXT"+ ")";
            db.execSQL(CREATE_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            // Drop older table if exist
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_Admins);
            // Create tables again
            onCreate(db);
        }
        // **** CRUD (Create, Read, Update, Delete) Operations ***** //

        // Adding new Admin Details
        public void  insertAdminDetails(CAdmin AdminInfo){
            //Get the Data Repository in write mode
            SQLiteDatabase db = this.getWritableDatabase();
            //Create a new map of values, where column names are the keys
            ContentValues cValues = new ContentValues();
            cValues.put(KEY_NAME, AdminInfo.getName());
            cValues.put(KEY_PASS, AdminInfo.getPass());
            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(TABLE_Admins,null, cValues);
            db.close();
        }
        // Get Admin Details
        public ArrayList<HashMap<String, String>> GetAdmins(){
            SQLiteDatabase db = this.getWritableDatabase();
            ArrayList<HashMap<String, String>> AdminList = new ArrayList<>();
            String query = "SELECT name, pass FROM "+ TABLE_Admins;
            Cursor cursor = db.rawQuery(query,null);
            while (cursor.moveToNext()){
                HashMap<String,String> Admin = new HashMap<>();
                Admin.put("name",cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                Admin.put("pass",cursor.getString(cursor.getColumnIndex(KEY_PASS)));
                AdminList.add(Admin);
            }
            return  AdminList;
        }
        // Get Admin Details based on Adminid
        public ArrayList<HashMap<String, String>> GetAdminByAdminId(int Adminid){
            SQLiteDatabase db = this.getWritableDatabase();
            ArrayList<HashMap<String, String>> AdminList = new ArrayList<>();
            String query = "SELECT name, pass FROM "+ TABLE_Admins;
            Cursor cursor = db.query(TABLE_Admins, new String[]{KEY_NAME, KEY_PASS}, KEY_ID+ "=?",new String[]{String.valueOf(Adminid)},null, null, null, null);
            if (cursor.moveToNext()){
                HashMap<String,String> Admin = new HashMap<>();
                Admin.put("name",cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                Admin.put("pass",cursor.getString(cursor.getColumnIndex(KEY_PASS)));
                AdminList.add(Admin);
            }
            return  AdminList;
        }
        // Delete Admin Details
        public void DeleteAdmin(int Adminid){
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_Admins, KEY_ID+" = ?",new String[]{String.valueOf(Adminid)});
            db.close();
        }
        // Update Admin Details
        public int UpdateAdminDetails(String name, String pass, int id){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cVals = new ContentValues();
            cVals.put(KEY_PASS, pass);
            cVals.put(KEY_NAME,name);
            int count = db.update(TABLE_Admins, cVals, KEY_ID+" = ?",new String[]{String.valueOf(id)});
            return  count;
        }
    }