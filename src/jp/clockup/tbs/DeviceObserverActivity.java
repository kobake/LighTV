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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.sony.remotecontrol.ir.Device;
import com.sony.remotecontrol.ir.IrManager;
import com.sony.remotecontrol.ir.IrManagerFactory;
import com.sony.remotecontrol.ir.Status;

public class DeviceObserverActivity extends Activity {

    private static final String TAG = "DeviceObserverActivity";

    private TextView mContets;

    private IrManager mIrMgr;

    private IrManager.Listener mListener = new IrManager.Listener() {

        @Override
        public void onActivated(IrManager irMgr) {
            Log.d(TAG, "onActivated");
        }

        @Override
        public void onError(Status reason) {
            String message = "onError: " + reason;
            Log.d(TAG, message);
            mContets.append("\n" + message);
        }

        @Override
        public void onDeviceUnregistered(Device device) {
            String message = "onDeviceUnregistered: " + device.getInstanceId() + " " + device.getDeviceName();
            Log.d(TAG, message);
            mContets.append("\n" + message);
        }

        @Override
        public void onDeviceRegistered(Device device) {
            String message = "onDeviceRegistered: " + device.getInstanceId() + " " + device.getDeviceName();
            Log.d(TAG, message);
            mContets.append("\n" + message);
        }

        @Override
        public void onDeviceAttributeChanged(Device device) {
            String message = "onDeviceAttributeChanged: " + device.getInstanceId() + " " + device.getDeviceName();
            Log.d(TAG, message);
            mContets.append("\n" + message);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_observer_view);

        mContets = (TextView)findViewById(R.id.device_observer_contents);

        mIrMgr = IrManagerFactory.getNewInstance();
        mIrMgr.activate(this, mListener);
    }


    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");

        mIrMgr.inactivate();

        super.onDestroy();
    }


}
