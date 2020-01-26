package com.mimik.smarthome.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mimik.smarthome.Model.CAdmin;
import com.mimik.smarthome.Model.Cbuilding;

import java.util.ArrayList;

import static com.mimik.smarthome.dataAccess.Database.AD_BuildingID;
import static com.mimik.smarthome.dataAccess.Database.AD_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.AD_KEY_NAME;
import static com.mimik.smarthome.dataAccess.Database.AD_KEY_PASS;
import static com.mimik.smarthome.dataAccess.Database.AD_Permission;
import static com.mimik.smarthome.dataAccess.Database.BD_AdminID;
import static com.mimik.smarthome.dataAccess.Database.BD_BuildingManagerID;
import static com.mimik.smarthome.dataAccess.Database.BD_BuildingNum;
import static com.mimik.smarthome.dataAccess.Database.BD_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.BD_Location;
import static com.mimik.smarthome.dataAccess.Database.BD_TypeOfBuilding;
import static com.mimik.smarthome.dataAccess.Database.BM_BuildingID;
import static com.mimik.smarthome.dataAccess.Database.BM_Email;
import static com.mimik.smarthome.dataAccess.Database.BM_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.BM_Name;
import static com.mimik.smarthome.dataAccess.Database.BM_Tel;
import static com.mimik.smarthome.dataAccess.Database.FB_ActivationDate;
import static com.mimik.smarthome.dataAccess.Database.FB_CreatedDate;
import static com.mimik.smarthome.dataAccess.Database.FB_ExpireDate;
import static com.mimik.smarthome.dataAccess.Database.FB_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.FB_USER_ID;
import static com.mimik.smarthome.dataAccess.Database.HP_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.HP_NODEID;
import static com.mimik.smarthome.dataAccess.Database.HP_UNITID;
import static com.mimik.smarthome.dataAccess.Database.TABLE_Admins;
import static com.mimik.smarthome.dataAccess.Database.TABLE_BUILDING;
import static com.mimik.smarthome.dataAccess.Database.TABLE_BUILDING_Manager;
import static com.mimik.smarthome.dataAccess.Database.TABLE_Door;
import static com.mimik.smarthome.dataAccess.Database.TABLE_FOBs;
import static com.mimik.smarthome.dataAccess.Database.TABLE_HomePanel;
import static com.mimik.smarthome.dataAccess.Database.TABLE_UNIT;
import static com.mimik.smarthome.dataAccess.Database.TABLE_USERS;
import static com.mimik.smarthome.dataAccess.Database.TABLE_VKEYs;
import static com.mimik.smarthome.dataAccess.Database.TABLE_VP_HP;
import static com.mimik.smarthome.dataAccess.Database.TABLE_VisitorPanel;
import static com.mimik.smarthome.dataAccess.Database.TD_BuildingID;
import static com.mimik.smarthome.dataAccess.Database.TD_Door_ID;
import static com.mimik.smarthome.dataAccess.Database.TD_VP_ID;
import static com.mimik.smarthome.dataAccess.Database.UN_BUZZNUMBER;
import static com.mimik.smarthome.dataAccess.Database.UN_BuildingID;
import static com.mimik.smarthome.dataAccess.Database.UN_FLOORNUMBER;
import static com.mimik.smarthome.dataAccess.Database.UN_ID;
import static com.mimik.smarthome.dataAccess.Database.UN_UnitNumber;
import static com.mimik.smarthome.dataAccess.Database.USER_KEY_EMAIL;
import static com.mimik.smarthome.dataAccess.Database.USER_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.USER_KEY_NAME;
import static com.mimik.smarthome.dataAccess.Database.USER_KEY_PASS;
import static com.mimik.smarthome.dataAccess.Database.USER_KEY_TEL1;
import static com.mimik.smarthome.dataAccess.Database.USER_KEY_TEL2;
import static com.mimik.smarthome.dataAccess.Database.USER_TYPE;
import static com.mimik.smarthome.dataAccess.Database.USER_UNITID;
import static com.mimik.smarthome.dataAccess.Database.USER_UserEnabled;
import static com.mimik.smarthome.dataAccess.Database.VK_ActivationDate;
import static com.mimik.smarthome.dataAccess.Database.VK_Authority_Level;
import static com.mimik.smarthome.dataAccess.Database.VK_CreatedDate;
import static com.mimik.smarthome.dataAccess.Database.VK_ExpireDate;
import static com.mimik.smarthome.dataAccess.Database.VK_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.VK_NumberOfUsage;
import static com.mimik.smarthome.dataAccess.Database.VK_Type;
import static com.mimik.smarthome.dataAccess.Database.VK_UnitID;
import static com.mimik.smarthome.dataAccess.Database.VP_BUILDINGID;
import static com.mimik.smarthome.dataAccess.Database.VP_HP_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.VP_KEY_ID;
import static com.mimik.smarthome.dataAccess.Database.VP_NODEID;

public class SQLiteHelper extends  SQLiteOpenHelper {


    //_________________________________________________________


    public SQLiteHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    //-------------------------------------------------------------------------------
    @Override
    public void onCreate(SQLiteDatabase db){



        // Create Admin table-------------------------
        String CREATE_TABLE = "CREATE TABLE " + TABLE_Admins + "("
                + AD_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AD_KEY_NAME + " TEXT,"
                + AD_KEY_PASS + " TEXT,"
                + AD_Permission + " TEXT,"
                + AD_BuildingID + " TEXT"

                + ")";
        db.execSQL(CREATE_TABLE);

       /* CAdmin admin=new CAdmin();
        admin.setName("Admin");
        admin.setPass("Mimik");
        admin.setPermission("Config");
        admin.setBuildingID("All");

        AdminRepository dbadmin=new AdminRepository();
        dbadmin.insertAdminDetails(admin);*/
        //---------------------------------------------------
        // Create Visitor Panel table-------------------------

        //________________ Door  Table______________

        CREATE_TABLE = "CREATE TABLE " + TABLE_Door + "("
                + TD_Door_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + TD_VP_ID+ " TEXT,"
                + TD_BuildingID+ " TEXT"
                +")";
        db.execSQL(CREATE_TABLE);


        //________________ TABLE_VisitorPanel______________

         CREATE_TABLE = "CREATE TABLE " + TABLE_VisitorPanel + "("
                 + VP_KEY_ID + " TEXT PRIMARY KEY ,"
                 + VP_BUILDINGID+ " TEXT,"
                 + VP_NODEID+ " TEXT"
                 +")";
        db.execSQL(CREATE_TABLE);
        //________________ Unit  Table______________

        CREATE_TABLE = "CREATE TABLE " + TABLE_UNIT + "("
                + UN_ID + " INTEGER PRIMARY KEY ,"
                + UN_BuildingID+ " INTEGER,"
                + UN_UnitNumber+ " TEXT ,"
                + UN_FLOORNUMBER+ " TEXT ,"
                + UN_BUZZNUMBER+ " TEXT "
                +")";
        db.execSQL(CREATE_TABLE);
        //________________ Home Panel  Table______________
        CREATE_TABLE = "CREATE TABLE " + TABLE_HomePanel + "("
                + HP_KEY_ID + " INTEGER PRIMARY KEY ,"
                + HP_UNITID+ " INTEGER,"
                + HP_NODEID+ " TEXT"
                +")";
        db.execSQL(CREATE_TABLE);
       //________________ Home & Visitor Panel  Table______________
        CREATE_TABLE = "CREATE TABLE " + TABLE_VP_HP + "("
                + VP_HP_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + HP_KEY_ID+ " INTEGER,"
                + VP_KEY_ID+ " INTEGER"
                +")";
        //db.execSQL(CREATE_TABLE);


        //________________USER Table______________

        CREATE_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + USER_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + USER_KEY_NAME+ " TEXT,"
                + USER_KEY_PASS+ " TEXT,"
                + HP_KEY_ID + " INTEGER,"
                + USER_KEY_EMAIL + " TEXT,"
                + USER_KEY_TEL1 + " TEXT,"
                + USER_KEY_TEL2 + " TEXT,"
                + USER_UNITID + " INTEGER,"
                + USER_UserEnabled+ " TEXT ,"
                + USER_TYPE + " TEXT"
                +")";
        db.execSQL(CREATE_TABLE);

        //________________FOB Table______________

        CREATE_TABLE = "CREATE TABLE " + TABLE_FOBs + "("
                + FB_KEY_ID + " TEXT PRIMARY KEY ,"
                + FB_USER_ID+ " INTEGER,"
                + FB_ActivationDate+ " TEXT,"
                + FB_ExpireDate + " TEXT,"
                + FB_CreatedDate + " TEXT"
                +")";
        db.execSQL(CREATE_TABLE);

        //________________VKeys Table______________

        CREATE_TABLE = "CREATE TABLE " + TABLE_VKEYs + "("
                + VK_KEY_ID + " TEXT PRIMARY KEY  ,"
                + VK_UnitID+ " INTEGER,"
                + VK_Authority_Level+ " TEXT,"
                + VK_ActivationDate + " TEXT,"
                + VK_ExpireDate + " TEXT,"
                + VK_CreatedDate + " TEXT,"
                + VK_NumberOfUsage + " TEXT,"
                + VK_Type + " TEXT"
                +")";
        db.execSQL(CREATE_TABLE);

        //________________ Building   Table______________
        CREATE_TABLE = "CREATE TABLE " + TABLE_BUILDING + "("
                + BD_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + BD_TypeOfBuilding+ " TEXT,"
                + BD_BuildingNum+ " TEXT,"
                + BD_AdminID + " INTEGER,"
                + BD_BuildingManagerID + " INTEGER,"
                + BD_Location + " TEXT"
                +")";

        //________________ Building  Manager Table______________

        CREATE_TABLE = "CREATE TABLE " + TABLE_BUILDING_Manager + "("
                + BM_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + BM_BuildingID+ " INTEGER,"
                + BM_Name+ " TEXT,"
                + BM_Email + " TEXT,"
                + BM_Tel + " TEXT"
                +")";

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Admins);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VisitorPanel);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HomePanel);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUILDING_Manager);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOBs);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VKEYs);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UNIT);
        // Create tables again
        onCreate(db);
    }
    // **** CRUD (Create, Read, Update, Delete) Operations ***** //
 /*
         This Area use to manage the Administrator Database
         Created : 29-11-2019 11:20am
         M.Esfandiari
     */

    // Adding new Building Details
    public void  insertBuildingDetails(Cbuilding building){
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(BD_AdminID, building.getAdminID());
        cValues.put(BD_BuildingManagerID, building.getBuildingManagerID());
        cValues.put(BD_BuildingNum,building.getBuildingNum());
        cValues.put(BD_Location,building.getLocation());
        cValues.put(BD_TypeOfBuilding,building.getTypeOfBuilding());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_BUILDING,null, cValues);
        db.close();
    }
    // Get Admin Details
    public ArrayList<Cbuilding> GetBuildins(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Cbuilding> BuildingList = new ArrayList<Cbuilding>();
        String query = "SELECT "+ BD_AdminID +"," +BD_BuildingManagerID +"," +BD_BuildingNum +"," + BD_KEY_ID +"," + BD_Location + "," + BD_TypeOfBuilding + "FROM "+ TABLE_BUILDING;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            Cbuilding Building = new Cbuilding();
            Building.setAdminID(cursor.getInt(cursor.getColumnIndex(BD_AdminID)));
            Building.setBuildingManagerID(cursor.getString(cursor.getColumnIndex(BD_BuildingManagerID)));
            Building.setBuildingNum(cursor.getString(cursor.getColumnIndex(BD_BuildingNum)));
            Building.setBuildingID(cursor.getInt(cursor.getColumnIndex(BD_KEY_ID)));
            Building.setLocation(cursor.getString(cursor.getColumnIndex(BD_Location)));
            Building.setTypeOfBuilding(cursor.getString(cursor.getColumnIndex(BD_TypeOfBuilding)));

            BuildingList.add(Building);
        }
        return  BuildingList;
    }
    // Get Building Details based on BuildingID
    public ArrayList<Cbuilding> GetBuildingByBuildingId(int BuildingID){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Cbuilding> BuildingList = new ArrayList<Cbuilding>();

        Cursor cursor = db.query(TABLE_BUILDING, new String[]{BD_AdminID ,BD_BuildingManagerID,BD_BuildingNum,BD_KEY_ID ,BD_Location , BD_TypeOfBuilding}, BD_KEY_ID+ "=?",new String[]{String.valueOf(BuildingID)},null, null, null, null);
        if (cursor.moveToNext()){
            Cbuilding Building = new Cbuilding();
            Building.setAdminID(cursor.getInt(cursor.getColumnIndex(BD_AdminID)));
            Building.setBuildingManagerID(cursor.getString(cursor.getColumnIndex(BD_BuildingManagerID)));
            Building.setBuildingNum(cursor.getString(cursor.getColumnIndex(BD_BuildingNum)));
            Building.setBuildingID(cursor.getInt(cursor.getColumnIndex(BD_KEY_ID)));
            Building.setLocation(cursor.getString(cursor.getColumnIndex(BD_Location)));
            Building.setTypeOfBuilding(cursor.getString(cursor.getColumnIndex(BD_TypeOfBuilding)));

            BuildingList.add(Building);
        }
        return  BuildingList;
    }
    // Delete Building Details
    public void DeleteBuilding(int BuildingID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BUILDING, BD_KEY_ID+" = ?",new String[]{String.valueOf(BuildingID)});
        db.close();
    }
    // Update Admin Details
    public int UpdateBuildingDetails(Cbuilding building){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(BD_AdminID, building.getAdminID());
        cValues.put(BD_BuildingManagerID, building.getBuildingManagerID());
        cValues.put(BD_BuildingNum,building.getBuildingNum());
        cValues.put(BD_Location,building.getLocation());
        cValues.put(BD_TypeOfBuilding,building.getTypeOfBuilding());
        // int count = db.update(TABLE_Admins, cVals, AD_KEY_ID+" = ?",new String[]{String.valueOf(admin.getID())});
        int count = db.update(TABLE_BUILDING, cValues, BD_KEY_ID+" = ?",new String[]{String.valueOf(building.getBuildingID())});
        return  count;
    }



}
