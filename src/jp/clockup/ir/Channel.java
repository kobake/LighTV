package jp.clockup.ir;

import org.json.JSONObject;

//チャンネル
public class Channel {
	// 番組情報
	public String m_title = "";

	// 局情報
	public String m_chName = "";
	public int m_chNumber = 0;
	public String m_chHash = "";
	public String m_chLogo = "";

	// アクティブ情報
	public int m_log = 0;

	public Channel(JSONObject obj) throws Exception {
		// 番組情報
		obj.getString("description");
		m_title = obj.getString("title");
		// 局
		JSONObject station = obj.getJSONObject("station");
		m_chName = station.getString("name"); // 局の名前
		m_chNumber = station.getInt("number"); // チャンネル？
		m_chHash = station.getString("hashtag"); // ハッシュタグ
		m_chLogo = station.getString("logo_url"); // ロゴURL
		// 盛り上がり
		m_log = obj.getInt("log");
	}

	public String toString() {
		return String.format("%d:%s:%s\n", m_chNumber, m_chName, m_title);
	}
}
