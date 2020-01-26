package com.mimik.smarthome.Model;

public class CVirtualKey {

    // fields
    private String VKID;
    private int UnitID;
    private String Authority_Level; //(Owner, Guest)
    private String ActivationDate;
    private String ExpireDate;
    private String CreatedDate;
    private String NumberOfUsage;
    private String Type;  //Password or QRCode

    // constructors
    public CVirtualKey() {
    }

}
