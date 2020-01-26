package com.mimik.smarthome.edgeSDK;

import java.io.Serializable;

 public class Device implements Serializable {
    public String id;
    public String accountId;
    public String name;
    public String os;
    public String url;
    public Routing routing;
    public Boolean isShareDevice = false;
}

