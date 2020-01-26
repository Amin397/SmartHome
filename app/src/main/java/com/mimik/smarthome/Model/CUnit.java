package com.mimik.smarthome.Model;

public class CUnit {

    private String          BuzzNumber;
    private String          UnitNumber;
    private int          UnitID;
    private String          FloorNo;
    private int          BuildingID;


    public void CUnit(){}

    public void setBuildingID(int buildingID) {
        BuildingID = buildingID;
    }

    public int getBuildingID() {
        return BuildingID;
    }

    public String getFloorNo() {
        return FloorNo;
    }

    public int getUnitID() {
        return UnitID;
    }

    public void setFloorNo(String floorNo) {
        FloorNo = floorNo;
    }

    public void setUnitID(int unitID) {
        UnitID = unitID;
    }

    public void setUnitNumber(String unitNumber) {
        UnitNumber = unitNumber;
    }

    public String getUnitNumber() {
        return UnitNumber;
    }

    public void setBuzzNumber(String buzzNumber) {
        BuzzNumber = buzzNumber;
    }

    public String getBuzzNumber() {
        return BuzzNumber;
    }
}
