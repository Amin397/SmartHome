package com.mimik.smarthome.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Database {

    protected static final String TAG = "DataAdapter";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "SmartHomedb.db";

    //________________ Visitor Panel Admin Table______________
    public static final String TABLE_Admins = "TAdmin";

    //--Fields --------------------------
    public static final String AD_KEY_ID = "ADid";
    public static final String AD_KEY_NAME = "Name";
    public static final String AD_KEY_PASS = "Pass";
    public static final String AD_Permission = "Permission";
    public static final String AD_BuildingID = "BuildingID";

    //________________USER Table______________
    public static final String TABLE_USERS = "TUser";
    //--Fields --------------------------
    public static final String USER_KEY_ID = "Userid";
    public static final String USER_KEY_NAME = "name";
    public static final String USER_KEY_PASS = "pass";
    //public static final String HP_KEY_ID = "HPid";
    public static final String USER_KEY_EMAIL = "email";
    public static final String USER_KEY_TEL1 = "tel1";
    public static final String USER_KEY_TEL2 = "tel2";
    public static final String USER_UNITID = "unitid";
    public static final String USER_TYPE = "usertype";
    public static final String USER_UserEnabled = "UserEnabled";



    //________________FOB Table______________
    public static final String TABLE_FOBs = "TFOBs";
    //--Fields --------------------------
    public static final String FB_KEY_ID = "RFid";
    public static final String FB_USER_ID = "userId";
    public static final String FB_ActivationDate = "ActivationDate";
    public static final String FB_ExpireDate = "ExpireDate";
    public static final String FB_CreatedDate = "CreatedDate";

    //________________VKeys Table______________
    public static final String TABLE_VKEYs = "VKEYs";
    //--Fields --------------------------
    public static final String VK_KEY_ID = "VKid";
    public static final String VK_UnitID = "VK_UnitID";
    public static final String VK_Authority_Level = "VK_Authority_Level";
    public static final String VK_ActivationDate = "VK_ActivationDate";
    public static final String VK_ExpireDate = "VK_ExpireDate";
    public static final String VK_CreatedDate = "VK_CreatedDate";
    public static final String VK_NumberOfUsage = "VK_NumberOfUsage";
    public static final String VK_Type = "VK_Type";


    //________________ Visitor Panel  Table______________
    public static final String TABLE_VisitorPanel = "TVisitorPanel"; //Table name
    //--Fields --------------------------
    public static final String VP_KEY_ID = "VPid";
    public static final String VP_BUILDINGID = "buildingID";
    public static final String VP_NODEID = "NodeID";
    //________________ Door  Table______________
    public static final String TABLE_Door = "TDoor"; //Table name
    //--Fields --------------------------
    public static final String TD_Door_ID = "doorID";
    public static final String TD_VP_ID = "VPid";
    public static final String TD_BuildingID = "BuildingID";

    //________________ Unit  Table______________
    public static final String TABLE_UNIT = "TUNIT"; //Table name
    //--Fields --------------------------
    public static final String UN_ID = "unitid";
    public static final String UN_BuildingID = "BuildingID";
    public static final String UN_UnitNumber = "unitNumber";
    public static final String UN_FLOORNUMBER = "floorNumber";
    public static final String UN_BUZZNUMBER = "buzzNumber";

    //________________ Building   Table______________

    public static final String TABLE_BUILDING = "TBUILDING"; //Table name
    //--Fields --------------------------
    public static final String BD_KEY_ID = "buildingId";
    public static final String BD_TypeOfBuilding = "TypeOfBuilding";
    public static final String BD_BuildingNum = "BuildingNum";
    public static final String BD_AdminID = "AdminId";
    public static final String BD_BuildingManagerID = "BuildingManagerID";
    public static final String BD_Location = "Location";

    //________________ Building  Manager Table______________

    public static final String TABLE_BUILDING_Manager = "TBUILDINGManager"; //Table name
    //--Fields --------------------------
    public static final String BM_KEY_ID = "BuildingManagerID";
    public static final String BM_BuildingID = "BuildingID";
    public static final String BM_Name = "Name";
    public static final String BM_Email = "Email";
    public static final String BM_Tel = "Tel";
    //________________ Home Panel  Table______________
    public static final String TABLE_HomePanel = "THomePanel"; //Table name
    //--Fields --------------------------
    public static final String HP_KEY_ID = "HPid"; //HP Node ID
    public static final String HP_UNITID = "UnitID";
    public static final String HP_NODEID = "NodeID";
    //________________ Home & Visitor Panel  Table______________

    public static final String TABLE_VP_HP = "T_VP_HP"; //Table name
    //--Fields --------------------------
    public static final String VP_HP_KEY_ID = "id";
    //    public static final String HP_KEY_ID = "HPid"; //Home Panle ID
    //    public static final String VP_KEY_ID = "VPid"; //Visitor Panel ID


    SQLiteDatabase sqLiteDatabase;
    SQLiteHelper sqLiteHelper;
    Context context;



    public Database(Context c){
        context = c;
        sqLiteHelper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);
        sqLiteDatabase=sqLiteHelper.getWritableDatabase();

    }

    public void openToRead() throws android.database.SQLException {
        if(sqLiteHelper==null)
            sqLiteHelper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);

        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
    }

    public void openToWrite() throws android.database.SQLException {
        if(sqLiteHelper==null) sqLiteHelper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);

        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
    }

    public void close(){
        sqLiteHelper.close();
    }

    public boolean Insert(String Table_Name, ContentValues cvalue)
    {

        sqLiteDatabase.insert(Table_Name,null,cvalue);
        return true;

    }



}
