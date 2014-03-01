package jp.clockup.tbs;

public class TvThread extends Thread{
	boolean m_stop = false;
	
	public void triggerStop(){
		m_stop = true;
		try{
			this.join();
		}
		catch(InterruptedException ex){
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//super.run();
		// ひたすらＴＶ情報をとってくる
		while(!m_stop){
			
		}
	}
}
