package jp.clockup.ir;

import java.util.ArrayList;
import java.util.List;

import jp.clockup.tbs.MainActivity;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sony.remotecontrol.ir.Device;
import com.sony.remotecontrol.ir.IrManager;
import com.sony.remotecontrol.ir.IrManagerFactory;
import com.sony.remotecontrol.ir.Key;
import com.sony.remotecontrol.ir.Status;

public class IrController {
    private static final String TAG = "RemoteActivity";
    private MainActivity m_parent;
    
    private int mDeviceId;
    private Device mDevice;
    private ArrayList<String> mKeyLabels = new ArrayList<String>();
    private ArrayList<Key> mKeys = new ArrayList<Key>();
    private Handler mMainThread = new Handler();
    private IrManager mIrMgr;
    
    public IrController(MainActivity parent) {
    	m_parent = parent;
	}

    public void controlTV(int cannnel) {
        Log.d("IR contorol", "select channnel " + cannnel);
        mDevice.sendKey(mKeys.get(52 + cannnel));// sonytablet:55-63,
                                                 // xperiatablet:53-61
        //Toast.makeText(this, "select channnel " + cannnel, Toast.LENGTH_SHORT).show();
    }

	public void onCreate(Context context){
        mDeviceId = 92550614; // sonytablet:63156005, xperiatablet:92550614
        mIrMgr = IrManagerFactory.getNewInstance();
        mIrMgr.activate(context, mMgrListener);
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
                            Log.v(TAG, "device " + device);
                            // mShowControlPannelButton.setEnabled(true);
                            break;
                        }
                    }
                    // showDeviceInfo();
                    setupKeyList();
                    m_parent.onContentChanged();
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

}
