package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import wanganxin.com.poker.GameActivity.GameStartActivity;
import wanganxin.com.poker.GameLogic.utilities.Constants;

/**
 *心跳包处理
 */
public class HeartbeatDeal {
    //进行心跳模拟
    public static void startHeartBeatSend() {
        // 启动线程模拟加载
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //每隔一段时间发送心跳包
                        Thread.sleep(Constants.HEART_BEAT_TIMESPAN);
                        GameStartActivity.getInstance().send("1");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
