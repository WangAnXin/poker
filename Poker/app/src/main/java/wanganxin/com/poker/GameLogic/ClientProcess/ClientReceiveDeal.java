package wanganxin.com.poker.GameLogic.ClientProcess;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wanganxin.com.poker.GameActivity.GameStartActivity;
import wanganxin.com.poker.GameActivity.LoginActivity;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.CallScoreDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.CardReceiveDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.LoginRegisterDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.MatchDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.OutCardDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.PrepareGameDeal;
import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;
import wanganxin.com.poker.GameLogic.entity.Player;

public class ClientReceiveDeal {
    public static String TAG = "1111";
    //获取客户端
    GameStartActivity client = null;
    public ClientReceiveDeal(GameStartActivity client) {
        this.client = client;
    }

    //当前玩家的信息
    public static Player player;
    //处理从服务器中读取的信息
    public String receiveMsg = null;
    //判断此次的消息处理是否完成，完成了一条，才能执行下一条
    public boolean isDealFinish = true;
    //设置当前游戏的进程
    public LandlordActivity game = null;

    //当前客户端读取消息处于什么状态
    ClientReceiveEnum step = ClientReceiveEnum.NONE;
    public ClientReceiveEnum nextStep = ClientReceiveEnum.NONE;

    //初始化当前游戏的进程
    public void initLandlordActivity(LandlordActivity game) {
        this.game = game;
    }

    //接受一些额外信息（托管\离线信息）
    public boolean receiveExtraMes() {
        boolean isMesReceive = false;
        switch (step) {
            case PREPARE_RECEIVE_PROCESS:
            case CARDS_RECEIVE_PROCESS:
            case CALL_SCORE_RECEIVE_PROCESS:
            case OUT_CARD_RECEIVE_PROCESS:
                //如果不是json数组返回错误
                do {
                    JsonParser parser = new JsonParser();
                    if (parser.parse(receiveMsg).isJsonArray() == false) {
                        break;
                    }

                    //如果是别人的出牌信息
                    JsonArray extraArray = parser.parse(receiveMsg).getAsJsonArray();

                    //获取发出动作的人
                    JsonObject indexObject = extraArray.get(0).getAsJsonObject();
                    if (indexObject.has("playerIndex") == false) {
                        break;
                    }
                    int playerIndex = GlobalValue.getDisplayIndex(indexObject.get("playerIndex").getAsInt());

                    //获取卡组信息或者是托管信息
                    JsonObject infoObject = extraArray.get(1).getAsJsonObject();

                    //如果是别人的离开信息
                    if (infoObject.has("isLeaveInfo") == true) {
                        //如果不是玩家自身的话（自己的托管信息已经设置过了）
                        if (playerIndex != 0) {
                            boolean isRobort = infoObject.get("isLeaveInfo").getAsBoolean();
                            game.peoples[playerIndex].setRobot(isRobort);
                        }

                        isMesReceive = true;
                        break;
                    }
                    //如果是别人的托管信息
                    else if (infoObject.has("isRobortInfo") == true) {
                        //如果不是玩家自身的话（自己的托管信息已经设置过了）
                        if (playerIndex != 0) {
                            if (infoObject.get("isRobortInfo").getAsBoolean() == true) {
                                CardAnimator.alphaRun(game.robots[playerIndex], Constants.LIGHT_DURATION_TIME);
                            } else {
                                CardAnimator.alphaGoneRun(game.robots[playerIndex], Constants.LIGHT_DURATION_TIME);
                            }
                        }
                        isMesReceive = true;
                        break;
                    }

                } while (false);
        }

        return isMesReceive;
    }

    //客户端接受进程的更新（上锁避免状态每更换就进入）
    public void processReceiveUpdate() {
        //打印接受信息的log
        Log.e(TAG, "receiveMsg: " + receiveMsg + " ," + "curStep" + step);

        //先接受额外的信息，如托管信息之内的
        do {
            boolean isExtraMes = receiveExtraMes();
            if (isExtraMes == true) {
                break;
            }

            switch (step) {
                //如果收到登录返回的结果(断线重连的状态跳转由ReconnectDeal完成)
                case LOGIN_RECEIVE_PROCESS: {
                    //创建登录处理的进程
                    int loginResult = LoginRegisterDeal.dealLoginResult(receiveMsg);

                    //如果登录成功，跳转到登录大厅阶段
                    if (loginResult == LoginActivity.LOGIN_SUCCESS) {
                        client.sendDeal.nextStep = ClientSendEnum.MATCH_PREPARE_PROCESS;
                        //等待消息结果
                        client.receiveDeal.nextStep = ClientReceiveEnum.MATCH_RECEIVE_PROCESS;
                    }
                }
                break;

                //准备进入匹配结果接受阶段
                case MATCH_RECEIVE_PROCESS: {
                    //创建匹配处理的流程
                    boolean isMatched = MatchDeal.dealMatchResult(receiveMsg);

                    //跳转到准备比赛结果阶段
                    if (isMatched == true) {
                        client.sendDeal.nextStep = ClientSendEnum.PREPARE_GAME_PROCESS;
                        nextStep = ClientReceiveEnum.PREPARE_RECEIVE_PROCESS;

                        //从登录页面跳转到游戏页面
                        GameStartActivity.getInstance().handler
                                .obtainMessage(GameStartActivity.START_GAME)
                                .sendToTarget();
                    }
                }
                break;

                //接受房间中玩家准备的结果
                case PREPARE_RECEIVE_PROCESS: {
                    //创建匹配处理的流程
                    boolean isGameStart = PrepareGameDeal.dealPrepareResult(receiveMsg, game);

                    //跳转到准备比赛结果阶段
                    if (isGameStart == true) {
                        nextStep = ClientReceiveEnum.CARDS_RECEIVE_PROCESS;
                    }
                }
                break;

                //接受卡组信息
                case CARDS_RECEIVE_PROCESS: {
                    //获取游戏画面的单例
                    //game = LandlordActivity.getInstance();
                    //获取四个人的卡组信息和底牌信息
                    if (new JsonParser().parse(receiveMsg).isJsonArray()
                            && new JsonParser().parse(receiveMsg).getAsJsonArray().get(0).getAsJsonObject().has("deck0")) {
                        CardReceiveDeal.getCardInfo(new JsonParser().parse(receiveMsg).getAsJsonArray(), 0, game);
                    }

                    nextStep = ClientReceiveEnum.CALL_SCORE_RECEIVE_PROCESS;
                    client.sendDeal.nextStep = ClientSendEnum.CALL_SCORE_PROCESS;
                }
                break;

                //接受叫分的信息
                case CALL_SCORE_RECEIVE_PROCESS: {

                    //创建处理叫分的流程（收到首位叫分人的时候开始发牌动画）
                    CallScoreDeal.CallScoreResult callScoreResult = CallScoreDeal.dealCallScoreResult(receiveMsg, game);

                    switch (callScoreResult) {
                        //如果有人叫分且叫分结束，跳转到下一步
                        case SUCCESS: {
                            nextStep = ClientReceiveEnum.OUT_CARD_RECEIVE_PROCESS;
                            client.sendDeal.nextStep = ClientSendEnum.OUT_CARD_PROCESS;
                        }
                        break;
                        //如果没人叫分叫分结束，重新发牌
                        case NO_SCORE: {
                            nextStep = ClientReceiveEnum.CARDS_RECEIVE_PROCESS;
                        }
                        break;
                    }
                }
                break;

                //接受出牌的信息
                case OUT_CARD_RECEIVE_PROCESS: {
                    //创建处理出牌的流程
                    boolean isGameEnd = OutCardDeal.dealOutCardResult(receiveMsg, game);

                    if (isGameEnd == true) {
                        //如果游戏结束重新进入游戏准备阶段
                        nextStep = ClientReceiveEnum.PREPARE_RECEIVE_PROCESS;
                        client.sendDeal.nextStep = ClientSendEnum.PREPARE_GAME_PROCESS;
                    }
                }
                break;
            }
        } while (false);

        //当前消息处理完成
        isDealFinish = true;

        //下一个发送哪个阶段
        client.sendDeal.initProcessSendUpdate();
        initProcessReceiveUpdate();
    }

    //改变状态前的初始化
    public void initProcessReceiveUpdate() {
        //每次将信息清除
        receiveMsg = null;

        if (nextStep == ClientReceiveEnum.NONE || step == nextStep) {
            return;
        }

        switch (nextStep) {

        }

        step = nextStep;
    }
}
