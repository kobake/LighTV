package jp.clockup.tvzin;

import java.io.IOException;

import jp.clockup.ir.ChannelList;
import jp.clockup.tbs.MainActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class Tvzin {
	// 結果受け取り用インターフェース
	public interface Listener{
		public void onGotChannelList(ChannelList channelList);
	}
	
	Listener m_listener;
	public void request(Listener listener){
        // 通信テスト
		m_listener = listener;
        new NetworkTask().execute("http://api2.tvz.in/1/program/current");
	}

    class NetworkTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                HttpGet req = new HttpGet(url);
                DefaultHttpClient client = new DefaultHttpClient();
                HttpResponse res = client.execute(req);
                String s = EntityUtils.toString(res.getEntity());
                return s;
            } catch (IOException ex) {
                return "Error: " + ex.toString() + ", " + ex.getMessage();
            } catch (Exception ex) {
                return "Error: " + ex.toString() + ", " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.startsWith("Error")) {
            	m_listener.onGotChannelList(null);
            }
            // JSON解釈
            try {
                ChannelList channelList = new ChannelList(result);
            	m_listener.onGotChannelList(channelList);
            } catch (Exception ex) {
            }
        }
    }
}
