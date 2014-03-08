package jp.clockup.hue;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import jp.clockup.tbs.R;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ListView;

import com.philips.lighting.data.AccessPointListAdapter;
import com.philips.lighting.data.HueSharedPreferences;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;
import com.philips.lighting.quickstart.PHHomeActivity;
import com.philips.lighting.quickstart.PHPushlinkActivity;
import com.philips.lighting.quickstart.PHWizardAlertDialog;

public class HueUtil {
	// ログ定数
	public static final String TAG = "QuickStart";

	// Hue用定数
	private PHHueSDK m_phHueSDK;
	private static final int MAX_HUE = 65535;

	// Hue用変数
	private HueSharedPreferences m_prefs;
	private boolean m_connect_ok = false;

	public boolean onCreate(Context context) {
		// Gets an instance of the Hue SDK.
		m_phHueSDK = PHHueSDK.create();

		// Set the Device Name (name of your app). This will be stored in your
		// bridge whitelist entry.
		m_phHueSDK.setDeviceName("QuickStartApp");

		// Register the PHSDKListener to receive callbacks from the bridge.
		m_phHueSDK.getNotificationManager().registerSDKListener(m_listener_setup);

		// Try to automatically connect to the last known bridge. For first time
		// use this will be empty so a bridge search is automatically started.
		m_prefs = HueSharedPreferences.getInstance(context.getApplicationContext());
		String lastIpAddress = m_prefs.getLastConnectedIPAddress();
		String lastUsername = m_prefs.getUsername();

		// Automatically try to connect to the last connected IP Address. For
		// multiple bridge support a different implementation is required.
		if (lastIpAddress != null && !lastIpAddress.equals("")) {
			PHAccessPoint lastAccessPoint = new PHAccessPoint();
			lastAccessPoint.setIpAddress(lastIpAddress);
			lastAccessPoint.setUsername(lastUsername);

			if (!m_phHueSDK.isAccessPointConnected(lastAccessPoint)) {
				// 接続中表示
				PHWizardAlertDialog.getInstance().showProgressDialog(
						R.string.connecting, context);
				m_phHueSDK.connect(lastAccessPoint);
			}
			return true;
		} else {
			// 設定が見つからなかった（要設定）
			return false;
		}
	}

	public void onDestroy() {
		// Hue破棄処理
		PHBridge bridge = m_phHueSDK.getSelectedBridge();
		if (bridge != null) {
			if (m_phHueSDK.isHeartbeatEnabled(bridge)) {
				m_phHueSDK.disableHeartbeat(bridge);
			}
			m_phHueSDK.disconnect(bridge);
		}
	}

	// Local SDK Listener private PHSDKListener listener_setup = new
	// Local SDK Listener
	private PHSDKListener m_listener_setup = new PHSDKListener() {
		@Override
		public void onAccessPointsFound(List<PHAccessPoint> accessPoint) {
		}
		@Override
		public void onCacheUpdated(int flags, PHBridge bridge) {
		}
		@Override
		public void onBridgeConnected(PHBridge b) {
			m_phHueSDK.setSelectedBridge(b);
			m_phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
			m_phHueSDK.getLastHeartbeat().put(
					b.getResourceCache().getBridgeConfiguration()
							.getIpAddress(), System.currentTimeMillis());
			m_prefs.setLastConnectedIPAddress(b.getResourceCache()
					.getBridgeConfiguration().getIpAddress());
			m_prefs.setUsername(m_prefs.getUsername());
			PHWizardAlertDialog.getInstance().closeProgressDialog();
			//
			m_connect_ok = true;
			//startMainActivity();
		}

		@Override
		public void onAuthenticationRequired(PHAccessPoint accessPoint) {
		}

		@Override
		public void onConnectionResumed(PHBridge bridge) {
			Log.v(TAG, "onConnectionResumed");
			/*
			if (PHHomeActivity.this.isFinishing())
				return;

			Log.v(TAG, "onConnectionResumed"
					+ bridge.getResourceCache().getBridgeConfiguration()
							.getIpAddress());
			m_phHueSDK.getLastHeartbeat().put(
					bridge.getResourceCache().getBridgeConfiguration()
							.getIpAddress(), System.currentTimeMillis());
			for (int i = 0; i < m_phHueSDK.getDisconnectedAccessPoint().size(); i++) {

				if (m_phHueSDK
						.getDisconnectedAccessPoint()
						.get(i)
						.getIpAddress()
						.equals(bridge.getResourceCache()
								.getBridgeConfiguration().getIpAddress())) {
					m_phHueSDK.getDisconnectedAccessPoint().remove(i);
				}
			}
			*/
		}

		@Override
		public void onConnectionLost(PHAccessPoint accessPoint) {
			Log.v(TAG, "onConnectionLost : " + accessPoint.getIpAddress());
			/*
			if (!m_phHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
				m_phHueSDK.getDisconnectedAccessPoint().add(accessPoint);
			}
			*/
		}

		@Override
		public void onError(int code, final String message) {
			Log.e(TAG, "on Error Called : " + code + ":" + message);
			/*
			if (code == PHHueError.NO_CONNECTION) {
				Log.w(TAG, "On No Connection");
			} else if (code == PHHueError.AUTHENTICATION_FAILED || code == 1158) {
				PHWizardAlertDialog.getInstance().closeProgressDialog();
			} else if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
				Log.w(TAG, "Bridge Not Responding . . . ");
				PHWizardAlertDialog.getInstance().closeProgressDialog();
				PHHomeActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						PHWizardAlertDialog.showErrorDialog(
								PHHomeActivity.this, message, R.string.btn_ok);
					}
				});

			} else if (code == PHMessageType.BRIDGE_NOT_FOUND) {
				PHWizardAlertDialog.getInstance().closeProgressDialog();

				PHHomeActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						PHWizardAlertDialog.showErrorDialog(
								PHHomeActivity.this, message, R.string.btn_ok);
					}
				});
			}
			*/
		}
	};

    // If you want to handle the response from the bridge, create a PHLightListener object.
    PHLightListener m_listener_app = new PHLightListener() {
        @Override
        public void onSuccess() {  
        }
        @Override
        public void onStateUpdate(Hashtable<String, String> arg0, List<PHHueError> arg1) {
           Log.w(TAG, "Light has updated");
        }
        @Override
        public void onError(int arg0, String arg1) {  
            Log.e(TAG, "app onError: " + arg1);
        }
    };

    public boolean isConnected(){
    	return m_connect_ok;
    }
    
	public void random() {
		// 繋がってないとダメだよ!!
		if(!m_connect_ok){
			return;
		}
		
        PHBridge bridge = m_phHueSDK.getSelectedBridge();

        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        Random rand = new Random();
        
        for (PHLight light : allLights) {
            PHLightState lightState = new PHLightState();
            lightState.setHue(rand.nextInt(MAX_HUE));
            lightState.setSaturation(254);
            lightState.setBrightness(254);
            
            // To validate your lightstate is valid (before sending to the bridge) you can use:  
            // String validState = lightState.validateState();
            bridge.updateLightState(light, lightState, m_listener_app);
            //  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
        }
	}
	
	Rainbow m_rainbow = null;
	public void random2() {
		// 繋がってないとダメだよ!!
		if(!m_connect_ok){
			return;
		}
		
		// 既に実行中なら何もしない
		if(m_rainbow != null){
			return;
		}
		
		m_rainbow = new Rainbow();
		m_rainbow.execute("");
       
	}

	// タイムライン通りにライトを点灯させる
	public void timeline(){
		
	}
	
	class Rainbow extends AsyncTask<String, Integer, String>{
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			m_rainbow = null;
			super.onPostExecute(result);
		}

		@Override
		protected String doInBackground(String... params) {
	        PHBridge bridge = m_phHueSDK.getSelectedBridge();
	        List<PHLight> allLights = bridge.getResourceCache().getAllLights();
        	int hue = 270; // 0～360
			for(int i = 0; i < 100; i++){
	           Log.w(TAG, "hue is " + hue);
		        for (PHLight light : allLights) {
		            PHLightState lightState = new PHLightState();
		            lightState.setOn(true);
		            lightState.setHue((int)(hue / 360.0f * MAX_HUE));// 色相  rand.nextInt(MAX_HUE));
		            lightState.setSaturation(254); // 彩度 0～254
		            lightState.setBrightness(100); // 明度 0～254
		            
		            hue += 10;
		            if(hue > 360)hue -= 360;
		            
		            // To validate your lightstate is valid (before sending to the bridge) you can use:  
		            // String validState = lightState.validateState();
		            bridge.updateLightState(light, lightState, m_listener_app);
		            //  bridge.updateLightState(light, lightState);   // If no bridge response is required then use this simpler form.
		        }
		        try{
		        	Thread.sleep(100);
		        }
		        catch(InterruptedException ex){
		        }
	        }
           Log.w(TAG, "thread done");
			return null;
		}
	}
}
