package com.mimik.smarthome.Model;

public class CAdmin {

        // fields
        private int AdminID;
        private String Name;
        private String Pass;
        private String Permission;
        private int BuildingID;

        // constructors

        public CAdmin() {
            this.Name="MimikAdmin";
            this.Pass="Mimik";
        }


        // properties


    public int getAdminID() {
        return AdminID;
    }

    public void setAdminID(int adminID) {
        AdminID = adminID;
    }

    public void setName(String name) {
            this.Name = name;
        }
        public String getName() {
            return this.Name;
        }
        public void setPass(String pass) {
            this.Pass = pass;
        }
        public String getPass() {
            return this.Pass;
        }

    public int getBuildingID() {
        return BuildingID;
    }

    public String getPermission() {
        return Permission;
    }

    public void setBuildingID(int buildingID) {
        BuildingID = buildingID;
    }

    public void setPermission(String permission) {
        Permission = permission;
    }
}