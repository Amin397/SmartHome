package com.mimik.smarthome.edgeSDK;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mimik.smarthome.R;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private static final String TAG = "FileAdapter";
    private List<DriveFile> mDataset;
    private IFileSelectCallback mFileSelectCallback;

    public void registerCallback(IFileSelectCallback callback) {
        mFileSelectCallback = callback;
    }

    private void callFileSelectCallback(DriveFile file) {
        mFileSelectCallback.selectedFile(file);
    }

    public interface IFileSelectCallback {
        void selectedFile(DriveFile file);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mName, mType;
        ImageButton mOpen;

        ViewHolder(View v) {
            super(v);
            mName = v.findViewById(R.id.name);
            mType = v.findViewById(R.id.type);
            mOpen = v.findViewById(R.id.open);
        }
    }

    public FileAdapter(List<DriveFile> files) {
        this.mDataset = files;
        Log.d(TAG, "FileAdapter: ");
    }

    public void swap(List<DriveFile> newFiles) {
        if(newFiles == null || newFiles.size() == 0)
            return;
        if (mDataset != null && mDataset.size()>0)
            mDataset.clear();

        if (mDataset != null) {
            mDataset.addAll(newFiles);
        }
        notifyDataSetChanged();
    }

    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_list_item, parent, false);
        Log.d(TAG, "onCreateViewHolder: ");
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DriveFile file = mDataset.get(position);
        Log.d(TAG,"onBindViewHolder"+ file.toString());
        holder.mName.setText(file.name);
        holder.mType.setText(file.mimeType);
        holder.mOpen.setImageResource(R.drawable.ic_open_in_new_24dp);
        holder.mOpen.setOnClickListener(view -> {
            callFileSelectCallback(file);
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
