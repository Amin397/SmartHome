package com.mimik.smarthome.edgeSDK;

import java.util.List;

public class McmData {
    public List<Data> data;
    public static class Env {
        public String AUTHORIZATION_KEY;
    }
    public static class Data {
        public String name;
        public Env env;
    }
}
