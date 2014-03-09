package jp.clockup.ir;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

//チャンネルリスト
public class ChannelList {
	public ArrayList<Channel> m_list = new ArrayList<Channel>();

	public ChannelList() {
	}

	public ChannelList(String jsonString) throws Exception {
		JSONObject json = new JSONObject(jsonString);
		JSONArray data = json.getJSONArray("data");
		for (int i = 0; i < data.length(); i++) {
			try {
				Channel channel = new Channel(data.getJSONObject(i));
				m_list.add(channel);
			} catch (Exception ex) {
			}
		}
	}

	public Channel getChannel(int index) {
		if (index >= 0 && index < m_list.size()) {
			return m_list.get(index);
		}
		return null;
	}
}
