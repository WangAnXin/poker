package wanganxin.com.poker.GameLogic.GameProcess.CallScore;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.JsonArray;

import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.CallScoreDeal;
import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.ReconnectDeal;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;

//网络叫分环节
public class NetCallScore extends CallScoreProcess {

    //从服务器传来的信息，谁第一个叫分，每个人的叫分情况
    public Integer firstPeople;
    public Integer[] playersCallScore;

    public NetCallScore(LandlordActivity game) {
        super(game);
        //初始化叫分的信息
        firstPeople = null;
        playersCallScore = new Integer[4];
    }

    public void startCallScore() {
        //初始化每个人的叫分情况
        for (int i = 0; i < 4; i++) {
            playersCallScore[i] = null;
        }

        //如果为断线重连阶段
        if (game.reconMode > 0) {
            JsonArray jsonArray = ReconnectDeal.reconJsonArray;
            //获取开始读取数据的index
            int index = ReconnectDeal.extraIndex;
            firstPeople = GlobalValue.getDisplayIndex(
                    jsonArray.get(index++).getAsJsonObject().get("firstCallScore").getAsInt());
            int num = 0;
            while (jsonArray.get(index).getAsJsonObject().has("callScore") == true) {
                int people = (firstPeople + num) % 4;
                playersCallScore[people] =
                        jsonArray.get(index++).getAsJsonObject().get("callScore").getAsInt();
                //在UI上显示其他人叫分的结果
                otherPlayerCallScoreDisplay(playersCallScore[people], people);
                num++;
            }

//            //获取当前已经有多少人叫分了
//            int callPeopleNum = jsonArray.get(index++).getAsJsonObject().get("callPeopleNum").getAsInt();
//            //将所获取得分传入数组中
//            int num = 0;
//            while (num < callPeopleNum) {
//                int people = (firstPeople + num) % 4;
//                playersCallScore[people] =
//                        jsonArray.get(index++).getAsJsonObject().get("callScore").getAsInt();
//                //在UI上显示其他人叫分的结果
//                otherPlayerCallScoreDisplay(playersCallScore[people], people);
//                num++;
//            }

            //获取当前时钟的时间，因为有动画，所以传的时间有延迟
            ReconnectDeal.clockTime = jsonArray.get(index++).getAsJsonObject().get("lastTime").getAsInt() + 2;
            //获取当前已经重新开始了多少局
            game.process.reCallScore = jsonArray.get(index++).getAsJsonObject().get("reCallScore").getAsInt();
            //恢复发牌的速度
            game.backUpAnimator();

            begin_pos = firstPeople - 1 + num;
            end_pos = firstPeople - 1 + 4;
        }
        //如果为正常的网络状态
        else {
            begin_pos = firstPeople - 1;
            end_pos = begin_pos + 4;
        }

        //第一个人开始叫分
        showCallResult();
    }
    //网络版，如果玩家不叫分，15s后自动认为不叫
    @Override
    protected void playerAutoCallScore() {
        //开机时钟倒计时，12s后自动认为不叫
        Message mesAction = new Message();
        mesAction.what = CALL_SCORE_0_BUTTON;

        game.clockManage.setClock(Constants.CALL_SCORE_CLOCK, 0, handler, mesAction, game);
    }

    //网络版发送消息给服务器，等待其他人叫分
    @Override
    public void playerScoreMeth(int score) {
        //在12s内叫过分了，停止计时
        game.clockManage.clockStopMes(0, game);

        //发送叫分信息
        CallScoreDeal.callScore = score;
        game.process.socketClient
                .sendDeal.processSendUpdate();

        //开始对下一个叫分
        showCallResult();
    }

    private final int UPDATE_ACTION = 1;
    private final int SHOW_CALL_RESULT = 2;
    private final int CALL_SCORE_0_BUTTON = 3;
    private final int NONE = 4;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ACTION:
                    //显示叫分的动画
                    otherPlayerCallScoreDisplay(msg.arg1, msg.arg2);
                    break;

                case SHOW_CALL_RESULT:
                    //下一位开始叫分
                    showCallResult();
                    break;
                case CALL_SCORE_0_BUTTON:
                    //自动认为不叫
                    score_button_Click(0);
                    break;
            }
        }
    };

    //其他人开始叫分，等待别人叫分
    @Override
    protected void othersCallScore(final int who) {
        //显示时钟计时，时间到了不做任何操作，等待服务器处理，服务器一定会发消息，除非服务器挂了
        Message mesAction = new Message();
        mesAction.what = NONE;

        Log.e("1111", "setClock: "+ who);

        game.clockManage.setClock(Constants.CALL_SCORE_CLOCK, who, handler, mesAction, game);

        //开启一个子线程，循环等待其他玩家将消息传入
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //间隔多少秒接受一次消息
                    try {
                        Thread.sleep(Constants.WAIT_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (playersCallScore[who] != null) {
                        Message mesAction = new Message();
                        mesAction.what = UPDATE_ACTION;
                        mesAction.arg1 = playersCallScore[who];
                        mesAction.arg2 = who;

                        //显示叫分的动画
                        handler.sendMessage(mesAction);

                        //下一位开始叫分
                        Message mesCall = new Message();
                        mesCall.what = SHOW_CALL_RESULT;
                        handler.sendMessage(mesCall);

                        //将时钟关闭
                        game.clockManage.clockStopMes(who, game);

                        //跳出循环
                        break;
                    }
                }
            }
        }).start();

    }

    //网络部分不调用，等待网络环节调用，避免网络接受顺序和画面执行顺序的不一致
    //@Override
    //protected void gameProcessChange() { }
}
