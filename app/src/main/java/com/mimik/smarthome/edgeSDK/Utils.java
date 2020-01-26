package com.mimik.smarthome.edgeSDK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.mimik.smarthome.BuildConfig;
import com.mimik.smarthome.R;

public final class Utils {
    public static final String TAG = "Util";

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getApplicationInfo(packageName, 0).enabled;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void showPackagePrompt(Activity parentActivity, Class cancelActivityClass) {
        Log.d(TAG, "showPackagePrompt");
        AlertDialog.Builder builder =
                new AlertDialog.Builder(parentActivity);
        builder.setTitle(parentActivity.getResources().getString(R.string.install_service_required_title));
        builder.setMessage(parentActivity.getResources().getString(R.string.install_service_required));
        builder.setPositiveButton(
                R.string.install_service_button,
                (dialog, which) -> {
                    Log.d(TAG, "onPositiveButton");
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(BuildConfig.EDGE_MARKET_URL));
                    try {
                        parentActivity.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        // Activity disappeared (app closed while popup shown? Play store not present?)
                        ex.printStackTrace();
                        System.exit(0);
                    }
                });
        builder.setOnCancelListener((di) -> {
            Log.d(TAG, "onCancelListener");
            parentActivity.finish();
            System.exit(0);
        });
        builder.show();
    }
}
