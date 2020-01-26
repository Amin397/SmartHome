package com.mimik.smarthome.Model;

public class CDevice {


    private String   NodeID;
    private String   accountId;
    private String   Name;
    private String   OS;
    private String   URL;
    private String   DeviceID; //UnitdID or VP ID
    // constructors
    public CDevice() {}
    public CDevice(String NodeID,String accountId, String Name, String OS, String URL,String DeviceID) {
        this.NodeID=NodeID;
        this.accountId = accountId;
        this.Name = Name;
        this.OS=OS;
        this.URL=URL;
        this.DeviceID=DeviceID;

    }
    // properties

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getDeviceID() {
        return DeviceID;
    }

    public void setDeviceID(String deviceID) {
        DeviceID = deviceID;
    }

    public void setNodeID(String NodeID) {
        this.NodeID = NodeID;
    }
    public String getNodeID() {
        return this.NodeID;
    }
    //////////////////////////////////////
    public String getaccountId() {
        return this.accountId;
    }

    public void setaccountId(String accountId) {
        this.accountId = accountId;
    }
    //////////////////////////////////////
    public void setName(String Name) {
        this.Name = Name;
    }
    public String getName() {
        return this.Name;
    }
    //////////////////////////////////////
    public void setOS(String OS) {
        this.OS = OS;
    }
    public String getOS() {
        return this.OS;
    }
    //////////////////////////////////////
    public void setURL(String URL) {
        this.URL = URL;
    }
    public String getURL() {
        return this.URL;
    }
    //////////////////////////////////////
}
