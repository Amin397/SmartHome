package com.mimik.smarthome.edgeSDK;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mimik.smarthome.R;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private static final String TAG = "DeviceAdapter";
    private List<Device> mDataset;
    private IDeviceSelectCallback mDeviceSelectCallback;
    private Boolean isShareDeviceSelected = false;

    public void registerCallback(IDeviceSelectCallback callback) {
        mDeviceSelectCallback = callback;
    }

    private void callDeviceSelectCallback(Device device) {
        mDeviceSelectCallback.selectDevice(device);
    }

    private void callShareDeviceSelectCallback(Device device) {
        mDeviceSelectCallback.selectShareDevice(device);
    }

    public interface IDeviceSelectCallback {
        void selectDevice(Device device);
        void selectShareDevice(Device device);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mName, mOs;
        ImageButton mShare, mCall;
        RelativeLayout mCell;

        public ViewHolder(View v) {
            super(v);
            mShare = v.findViewById(R.id.share);
            mName = v.findViewById(R.id.name);
            mOs = v.findViewById(R.id.os);
//            mUrl = v.findViewById(R.id.url);
            mCall = v.findViewById(R.id.call);
            mCell = v.findViewById(R.id.cell);
            Log.d(TAG, "ViewHolder: ");
        }
    }

    public DeviceAdapter(List<Device> data) {
        this.mDataset = data;
        setShareDeviceStatus();
    }

    public void swap(List<Device> data) {
        if(data == null || data.size()==0)
            return;
        if (mDataset != null && mDataset.size()>0)
            mDataset.clear();

        if (mDataset != null) {
            mDataset.addAll(data);
        }
        setShareDeviceStatus();
        notifyDataSetChanged();
    }

    private void setShareDeviceStatus() {
        this.isShareDeviceSelected = true;
//        for (Device device : mDataset) {
//            if (device.isShareDevice) {
//                this.isShareDeviceSelected = device.isShareDevice;
//                break;
//            }
//        }
    }

    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Device device = mDataset.get(position);
        Log.d("onBindViewHolder", device.toString());
        holder.mShare.setImageResource(R.drawable.ic_dvr_24dp);
        holder.mShare.setBackgroundResource(R.drawable.roundcorner);
        holder.mShare.setVisibility(View.VISIBLE);
        holder.mName.setText(device.name);
        holder.mOs.setText(device.os);
        holder.mCall.setImageResource(R.drawable.ic_call_24dp);

        if (this.isShareDeviceSelected) {
            holder.mCall.setVisibility(View.VISIBLE);
            holder.mCall.setOnClickListener(view -> callDeviceSelectCallback(device));
            if (device.isShareDevice) {
                holder.mShare.setBackgroundResource(0);
                holder.mCall.setVisibility(View.VISIBLE);
            } else {
                holder.mShare.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.mCall.setVisibility(View.VISIBLE);
            holder.mShare.setOnClickListener(view -> {
                device.isShareDevice = true;
                callShareDeviceSelectCallback(device);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
