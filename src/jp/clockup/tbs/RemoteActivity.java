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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sony.remotecontrol.ir.Device;
import com.sony.remotecontrol.ir.DeviceCategory;
import com.sony.remotecontrol.ir.IntentParams;
import com.sony.remotecontrol.ir.IrManager;
import com.sony.remotecontrol.ir.IrManagerFactory;
import com.sony.remotecontrol.ir.Key;
import com.sony.remotecontrol.ir.Status;

public class RemoteActivity extends ListActivity {

    public static final String EXTRA_DEVICE_ID = "device_id";

    private static final String TAG = "RemoteActivity";

    private Switch mRepeatSwitch;

    private Button mCancelRepeatButton;

    private Button mShowControlPannelButton;

    private int mDeviceId;

    private Device mDevice;

    private ArrayList<String> mKeyLabels = new ArrayList<String>();

    private ArrayList<Key> mKeys = new ArrayList<Key>();

    private Handler mMainThread = new Handler();

    private IrManager mIrMgr;

    private IrManager.Listener mMgrListener = new IrManager.Listener() {

        @Override
        public void onActivated(IrManager irMgr) {
            Log.d(TAG, "onActivated");

            final IrManager mgr = irMgr;

            mMainThread.post(new Runnable() {
                public void run() {
                    List<Device> devices = mgr.getRegisteredDevices();
                    for (Device device : devices) {
                        if (device.getInstanceId() == mDeviceId) {
                            mDevice = device;
                            mShowControlPannelButton.setEnabled(true);
                            break;
                        }
                    }
                    showDeviceInfo();
                    setupKeyList();
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


        mDeviceId = getIntent().getIntExtra(EXTRA_DEVICE_ID,
                                            Device.INVALID_DEVICE_ID);
        if (mDeviceId == Device.INVALID_DEVICE_ID) {
            finish();
        }

        setContentView(R.layout.remote_view);

        // repeat switch
        mRepeatSwitch = (Switch) findViewById(R.id.switch_repeat);

        // cancel repeat button
        mCancelRepeatButton = (Button) findViewById(R.id.button_cancel_repeat);
        mCancelRepeatButton.setEnabled(true);
        mCancelRepeatButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mDevice.stopRepeatKey();
            }
        });

        // show control pannel button
        mShowControlPannelButton = (Button) findViewById(R.id.button_show_control_panel);
        mShowControlPannelButton.setEnabled(false);
        mShowControlPannelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showControlPannel();
            }
        });


        ListAdapter adapter =
            new ArrayAdapter<String>(this,
                                     android.R.layout.simple_list_item_1,
                                     mKeyLabels);
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

        mIrMgr.activate(this, mMgrListener);
    }


    @Override
    protected void onPause() {

        Log.d(TAG, "onPause");

        super.onPause();

        clearData();
        onContentChanged();
        mShowControlPannelButton.setEnabled(false);
        mIrMgr.inactivate();
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        Log.d(TAG, "onListItemClick " + position);

        boolean repeat = mRepeatSwitch.isChecked();

        boolean success;
        if (repeat) {
            success = mDevice.startRepeatKey(mKeys.get(position));
        } else {
            success = mDevice.sendKey(mKeys.get(position));
        }

        String methodName = repeat ? "startRepeatKey" : "sendKey";
        String result = success ? "Success !" : "Failed !";
        Toast toast = Toast.makeText(getApplicationContext(),
                                     methodName + ": " + result,
                                     Toast.LENGTH_SHORT);
        toast.show();
    }



    private void showDeviceInfo() {

        // instance id
        TextView instanceIdView = (TextView) findViewById(R.id.text_instance_id);
        Integer instanceId = mDeviceId;
        instanceIdView.setText(instanceId.toString());


        // name
        TextView nameView = (TextView) findViewById(R.id.text_device_name);
        String name;
        if (mDevice != null) {
            name = mDevice.getDeviceName();
            if (name == null) {
                name = "Error: getDeviceName";
            }
        } else {
            name = "Invalid device";
        }
        nameView.setText(name);


        // category
        TextView categoryView = (TextView) findViewById(R.id.text_category);
        String category;
        if (mDevice != null) {
            DeviceCategory c = mDevice.getCategory();
            if (c != null) {
                category = c.toString();
            } else {
                category = "Error: getCategory";
            }
        } else {
            category = "Invalid device";
        }
        categoryView.setText(category);


        // manufacturer
        TextView manufacturerView = (TextView) findViewById(R.id.text_manufacturer);
        String manufacturer;
        if (mDevice != null) {
            manufacturer = mDevice.getManufacturerName();
            if (manufacturer == null) {
                manufacturer = "Error: getManufacturerName";
            }
        } else {
            manufacturer = "Invalid device";
        }
        manufacturerView.setText(manufacturer);
    }

    private void setupKeyList() {

        List<Key> keyList;
        if (mDevice != null) {
            keyList = mDevice.getKeyList();
        } else {
            keyList = new ArrayList<Key>();
        }

        mKeys.clear();
        mKeyLabels.clear();
        for (Key key : keyList) {

            String label = key.getUserLabel();
            if (label == null) {
                label = "-----";
            }
            boolean learnt = key.isLearnt();

            mKeyLabels.add("type: " + key.getType() + ", label: " + label + ", learnt: " + learnt);
            mKeys.add(key);
        }
    }

    private void clearData() {
        mDevice = null;
        mKeyLabels.clear();
        mKeys.clear();
    }

    private void showControlPannel() {
        Intent intent = new Intent(IntentParams.ACTION_SHOW_CONTROL_PANEL);
        intent.putExtra(IntentParams.EXTRA_DEVICE_ID, mDeviceId);
        startActivity(intent);
    }
}
