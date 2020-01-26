package com.mimik.smarthome.Model;

public class CBuildingManager {

    private int BuildingManagerID;
    private int BuildingID;
    private String Name;
    private String Email;
    private String Tel;

    public void setBuildingManagerID(int buildingManagerID) {
        BuildingManagerID = buildingManagerID;
    }

    public int getBuildingManagerID() {
        return BuildingManagerID;
    }

    public void setBuildingID(int buildingID) {
        BuildingID = buildingID;
    }

    public int getBuildingID() {
        return BuildingID;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getEmail() {
        return Email;
    }

    public String getName() {
        return Name;
    }

    public String getTel() {
        return Tel;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setTel(String tel) {
        Tel = tel;
    }
}
