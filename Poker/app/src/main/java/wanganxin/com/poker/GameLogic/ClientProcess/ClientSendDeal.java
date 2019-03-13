package wanganxin.com.poker.GameLogic.ClientProcess;

import android.util.Log;

import wanganxin.com.poker.GameActivity.GameStartActivity;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.CallScoreDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.MatchDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.OutCardDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.PrepareGameDeal;
import wanganxin.com.poker.GameActivity.LandlordActivity;

public class ClientSendDeal {

    public static String TAG = "1111";

    //获取客户端
    GameStartActivity client = null;
    //设置当前游戏的进程
    LandlordActivity game = null;
    //当前客户端发送消息处于什么状态
    ClientSendEnum step = ClientSendEnum.LOGIN_SEND_PROCESS;
    public ClientSendEnum nextStep = ClientSendEnum.NONE;

    public ClientSendDeal(GameStartActivity client) {
        this.client = client;
    }

    //初始化当前游戏的进程
    public void initLandlordActivity(LandlordActivity game) {
        this.game = game;
    }

    //客户端发送进程更新（上锁避免状态每更换就进入）
    public void processSendUpdate() {
        switch(step) {
            //如果是登录状态
            case LOGIN_SEND_PROCESS: {
                //分登录消息发送
                //注册消息发送
            }
            break;

            //准备进入匹配准备阶段
            case MATCH_PREPARE_PROCESS: {
                String msg = MatchDeal.getMatchGameMsg();

                //发送请求匹配
                client.send(msg);

                Log.e(TAG, "MATCH_PREPARE_PROCESS: " + msg);
            }
            break;

            //进入大厅准备阶段
            case PREPARE_GAME_PROCESS: {
                String msg = PrepareGameDeal.getPrepareGameMsg();

                //发送请求匹配
                client.send(msg);

                //等待消息结果
                client.receiveDeal.nextStep = ClientReceiveEnum.PREPARE_RECEIVE_PROCESS;

                Log.e(TAG, "MATCH_PREPARE_PROCESS: " + msg);
            }
            break;

            //进入叫分阶段
            case CALL_SCORE_PROCESS: {
                String msg = CallScoreDeal.getCallScoreMsg();

                //发送叫分信息
                client.send(msg);

                Log.e(TAG, "CALL_SCORE_PROCESS: " + msg);
            }
            break;

            //进入出牌阶段
            case OUT_CARD_PROCESS: {
                String msg = OutCardDeal.getOutCardMsg(game);

                //发送出牌的信息
                client.send(msg);

                Log.e(TAG, "OUT_CARD_PROCESS: " + msg);
            }
            break;
        }

        initProcessSendUpdate();
        client.receiveDeal.initProcessReceiveUpdate();
    }

    //改变状态前的初始化
    public void initProcessSendUpdate() {
        if (nextStep == ClientSendEnum.NONE || step == nextStep) {
            return;
        }

        switch (nextStep) {

        }

        step = nextStep;
    }
}
