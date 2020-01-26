package com.mimik.smarthome.userinterface.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mimik.smarthome.R;
import com.mimik.smarthome.edgeSDK.Device;
import com.mimik.smarthome.edgeSDK.DriveFile;
import com.mimik.smarthome.edgeSDK.DriveFiles;
import com.mimik.smarthome.edgeSDK.DriveProvider;
import com.mimik.smarthome.edgeSDK.FileAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class MediaListActivity extends Activity implements FileAdapter.IFileSelectCallback {
    private static final String TAG = "MediaListActivity";

    private Device mShareDevice;
    private String mEdgeAccessToken;
    private List<DriveFile> mFileList = new ArrayList<>();

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_list);

        mRecyclerView = findViewById(R.id.filesView);


        List<DriveFile> emptyFileList = new ArrayList<>();
        mAdapter = new FileAdapter(emptyFileList);
        if (mAdapter instanceof FileAdapter) {
            ((FileAdapter) mAdapter).registerCallback(MediaListActivity.this);
        }
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent.hasExtra("token")) {
            mEdgeAccessToken = (String) intent.getSerializableExtra("token");
        }
        if (intent.hasExtra("shareDevice")) {
            mShareDevice = (Device) intent.getSerializableExtra("shareDevice");
            new GetFileList().execute();
        }
    }

    @Override
    public void selectedFile(DriveFile file) {
        // File to open
        String type = file.mimeType;

        if (type == null)
            type = "*/*";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse(DriveProvider.mRemoteUrl + "/drive/v1/files/" + file.id + "?alt=media");

        intent.setDataAndType(data, type);

        startActivity(intent);
    }

    private class GetFileList extends AsyncTask<Void, Void, DriveFiles> {
        @Override
        protected DriveFiles doInBackground(Void... voids) {
            DriveFiles ret = null;
            DriveProvider driveProvider = DriveProvider.instance(mShareDevice, mEdgeAccessToken);

            try {
                Response<DriveFiles> response = driveProvider.getFiles().execute();
                if (response.isSuccessful() && response.body() != null) {
                    ret = response.body();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }

        @Override
        protected void onPostExecute(final DriveFiles files) {
            mFileList.clear();
            for (DriveFile file: files.files) {
                Log.d(TAG, "files: " + file.name + " -- " + file.mimeType);
                if (!file.name.startsWith("v1.0-") && !file.mimeType.equalsIgnoreCase("application/json")) {
                    mFileList.add(file);
                }
            }
            if (mAdapter instanceof FileAdapter) {
                ((FileAdapter) mAdapter).swap(mFileList);
            }
        }
    }

}
