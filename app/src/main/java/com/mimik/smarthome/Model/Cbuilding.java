package com.mimik.smarthome.Model;

public class Cbuilding {


    // fields
    private int BuildingID;
    private String TypeOfBuilding;//Resendtial Commercial others
    private String BuildingNum;
    private int AdminID;
    private String BuildingManagerID;
    private String Location; //included lat lon


    // constructors

    public Cbuilding() { }

    public void setAdminID(int adminID) {
        AdminID = adminID;
    }

    public int getAdminID() {
        return AdminID;
    }

    public int getBuildingID() {
        return BuildingID;
    }

    public void setBuildingID(int buildingID) {
        BuildingID = buildingID;
    }

    public String getBuildingManagerID() {
        return BuildingManagerID;
    }

    public String getBuildingNum() {
        return BuildingNum;
    }

    public String getLocation() {
        return Location;
    }

    public String getTypeOfBuilding() {
        return TypeOfBuilding;
    }

    public void setBuildingManagerID(String buildingManagerID) {
        BuildingManagerID = buildingManagerID;
    }

    public void setBuildingNum(String buildingNum) {
        BuildingNum = buildingNum;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public void setTypeOfBuilding(String typeOfBuilding) {
        TypeOfBuilding = typeOfBuilding;
    }
}
