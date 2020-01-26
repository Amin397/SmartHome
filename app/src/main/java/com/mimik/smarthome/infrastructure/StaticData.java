package com.mimik.smarthome.infrastructure;
import android.content.Context;


public class StaticData {
    private static Context _context;
    public static Context getContext() {
        return _context;
    }
    public static void setContext(Context context) {
        _context = context;
    }

}
