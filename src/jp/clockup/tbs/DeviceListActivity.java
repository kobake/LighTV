/*
 * Copyright 2011, 2012 Sony Corporation
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.clockup.tbs;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sony.remotecontrol.ir.Device;
import com.sony.remotecontrol.ir.IntentParams;
import com.sony.remotecontrol.ir.IrManager;
import com.sony.remotecontrol.ir.IrManagerFactory;
import com.sony.remotecontrol.ir.Status;

public class DeviceListActivity extends ListActivity {

    private static final String TAG = "DeviceListActivity";

    private ArrayList<String> mDeviceNames = new ArrayList<String>();

    private ArrayList<Integer> mDeviceIds = new ArrayList<Integer>();

    private Handler mMainThread = new Handler();

    private IrManager mIrMgr;

    private IrManager.Listener mListener = new IrManager.Listener() {

        @Override
        public void onActivated(IrManager irMgr) {
            Log.d(TAG, "onActivated");

            final IrManager mgr = irMgr;

            mMainThread.post(new Runnable() {
                public void run() {
                    List<Device> devices = mgr.getRegisteredDevices();
                    clearDeviceData();
                    for (Device device : devices) {
                        String name = device.getDeviceName();
                        int instanceId = device.getInstanceId();
                        if (name != null && instanceId != Device.INVALID_DEVICE_ID) {
                            addDeviceData(name ,instanceId);
                        } else {
                            // error
                            clearDeviceData();
                        }
                    }
                    onContentChanged();
                }
            });
        }

        @Override
        public void onError(Status reason) {
        }

        @Override
        public void onDeviceUnregistered(Device device) {
        }

        @Override
        public void onDeviceRegistered(Device device) {
        }

        @Override
        public void onDeviceAttributeChanged(Device device) {
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_list_view);

        Button registerDeviceButton = (Button) findViewById(R.id.button_register_device);
        registerDeviceButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                launchDeviceEditor();
            }
        });

        Button observingDeviceButton = (Button) findViewById(R.id.button_observing_device);
        observingDeviceButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent  = new Intent(DeviceListActivity.this, DeviceObserverActivity.class);
                startActivity(intent);
            }
        });


        ListAdapter adapter =
            new ArrayAdapter<String>(this,
                                     android.R.layout.simple_list_item_1,
                                     mDeviceNames);
        setListAdapter(adapter);

        mIrMgr = IrManagerFactory.getNewInstance();
    }


    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");

        super.onDestroy();

    }


    @Override
    protected void onResume() {

        Log.d(TAG, "onResume");

        super.onResume();

        mIrMgr.activate(this, mListener);
    }


    @Override
    protected void onPause() {

        Log.d(TAG, "onPause");

        super.onPause();

        mIrMgr.inactivate();
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Log.d(TAG, "onListItemClick " + position);

        Intent intent  = new Intent(this, RemoteActivity.class);
        intent.putExtra(RemoteActivity.EXTRA_DEVICE_ID, mDeviceIds.get(position));
        startActivity(intent);
    }



    private void clearDeviceData() {
        mDeviceNames.clear();
        mDeviceIds.clear();
    }

    private void addDeviceData(String name, int instanceId) {
        mDeviceNames.add(name);
        mDeviceIds.add(instanceId);
    }

    private void launchDeviceEditor() {
        Intent intent = new Intent(IntentParams.ACTION_REGISTER_DEVICE);
        startActivity(intent);
    }
}
