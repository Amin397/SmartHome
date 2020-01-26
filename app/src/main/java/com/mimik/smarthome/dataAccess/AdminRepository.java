package com.mimik.smarthome.dataAccess;

import android.content.ContentValues;

import android.database.Cursor;

import com.mimik.smarthome.Model.CAdmin;
import com.mimik.smarthome.infrastructure.StaticData;
import static com.mimik.smarthome.dataAccess.Database.AD_BuildingID;
import static com.mimik.smarthome.dataAccess.Database.AD_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.AD_KEY_NAME;
import static com.mimik.smarthome.dataAccess.Database.AD_KEY_PASS;
import static com.mimik.smarthome.dataAccess.Database.AD_Permission;
import static com.mimik.smarthome.dataAccess.Database.TABLE_Admins;




public class AdminRepository {

    private Database db = new Database(StaticData.getContext());

    // Adding new Admin Details
    public void  insertAdminDetails(CAdmin admin){
        //Get the Data Repository in write mode


        db.openToWrite();

       // SQLiteDatabase db = _dbHelper.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(AD_KEY_NAME, admin.getName());
        cValues.put(AD_KEY_PASS, admin.getPass());
        cValues.put(AD_BuildingID,admin.getBuildingID());
        cValues.put(AD_Permission,admin.getPermission());
        // Insert the new row, returning the primary key value of the new row
        boolean newRowId = db.Insert(TABLE_Admins, cValues);
        db.close();
    }
//    // Get Admin Details
//    public ArrayList<CAdmin> GetAdmins(){
//
//        SQLiteDatabase db = this.getWritableDatabase();
//        ArrayList<CAdmin> AdminList = new ArrayList<CAdmin>();
//        String query = "SELECT "+ AD_KEY_NAME +"," +AD_BuildingID +"," +AD_Permission +"," + AD_KEY_PASS + "FROM "+ TABLE_Admins;
//        Cursor cursor = db.rawQuery(query,null);
//        while (cursor.moveToNext()){
//            CAdmin Admin = new CAdmin();
//            // Admin.setID(cursor.getString(cursor.getColumnIndex(AD_KEY_ID)));
//            Admin.setName(cursor.getString(cursor.getColumnIndex(AD_KEY_NAME)));
//            Admin.setPass(cursor.getString(cursor.getColumnIndex(AD_KEY_PASS)));
//            Admin.setBuildingID(cursor.getString(cursor.getColumnIndex(AD_BuildingID)));
//            Admin.setPermission(cursor.getString(cursor.getColumnIndex(AD_Permission)));
//            AdminList.add(Admin);
//        }
//        db.close();
//        cursor.close();
//        return  AdminList;
//    }
//    // Get Admin Details based on Adminid
//    public ArrayList<CAdmin> GetAdminByAdminId(int Adminid){
//        SQLiteDatabase db = _dbHelper.getWritableDatabase();
//        ArrayList<CAdmin> AdminList = new ArrayList<CAdmin>();
//
//        Cursor cursor = db.query(TABLE_Admins, new String[]{AD_KEY_ID,AD_KEY_NAME, AD_KEY_PASS,AD_BuildingID,AD_Permission}, AD_KEY_ID+ "=?",new String[]{String.valueOf(Adminid)},null, null, null, null);
//        if (cursor.moveToNext()){
//            CAdmin Admin = new CAdmin();
//            // Admin.setID(cursor.getString(cursor.getColumnIndex(AD_KEY_ID)));
//            Admin.setName(cursor.getString(cursor.getColumnIndex(AD_KEY_NAME)));
//            Admin.setPass(cursor.getString(cursor.getColumnIndex(AD_KEY_PASS)));
//            Admin.setBuildingID(cursor.getString(cursor.getColumnIndex(AD_BuildingID)));
//            Admin.setPermission(cursor.getString(cursor.getColumnIndex(AD_Permission)));
//            AdminList.add(Admin);
//        }
//        db.close();
//        cursor.close();
//        return  AdminList;
//    }
    public CAdmin GetAdminByNamePass(String Name,String Pass){
        CAdmin Admin = new CAdmin();

        db.openToWrite();
        Admin.setName("Admin");
        Cursor cursor = db.sqLiteDatabase.query(TABLE_Admins, new String[]{AD_KEY_ID,AD_KEY_NAME, AD_KEY_PASS,AD_BuildingID,AD_Permission}, AD_KEY_NAME+ "=?  and "+AD_KEY_PASS +" =?" ,new String[]{Name,Pass},null, null, null, null);
        if (cursor.moveToNext()){

            //Admin.(cursor.getString(cursor.getColumnIndex(AD_KEY_ID)));
            Admin.setName(cursor.getString(cursor.getColumnIndex(AD_KEY_NAME)));
            Admin.setPass(cursor.getString(cursor.getColumnIndex(AD_KEY_PASS)));
            Admin.setBuildingID(cursor.getInt(cursor.getColumnIndex(AD_BuildingID)));
            Admin.setPermission(cursor.getString(cursor.getColumnIndex(AD_Permission)));

        }
        db.close();
        cursor.close();
        return  Admin;
    }
//    // Delete Admin Details
//    public void DeleteAdmin(int Adminid){
//        SQLiteDatabase db  = _dbHelper.getReadableDatabase();
//        db.delete(TABLE_Admins, AD_KEY_ID+" = ?",new String[]{String.valueOf(Adminid)});
//        db.close();
//    }
//    // Update Admin Details
//    public int UpdateAdminDetails(CAdmin admin){
//        SQLiteDatabase db  = _dbHelper.getReadableDatabase();
//        ContentValues cVals = new ContentValues();
//        cVals.put(AD_KEY_PASS, admin.getPass());
//        cVals.put(AD_KEY_NAME,admin.getName());
//        cVals.put(AD_BuildingID,admin.getBuildingID());
//        cVals.put(AD_Permission,admin.getPermission());
//        int count = db.update(TABLE_Admins, cVals, AD_KEY_ID+" = ?",new String[]{String.valueOf(admin.getAdminID())});
//        return  count;
//    }


}
