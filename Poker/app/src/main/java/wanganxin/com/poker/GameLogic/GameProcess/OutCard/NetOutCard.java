package wanganxin.com.poker.GameLogic.GameProcess.OutCard;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.google.gson.JsonArray;

import java.util.ArrayList;

import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.OutCardDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.ReconnectDeal;
import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;

//网络版出卡
public class NetOutCard extends OutCardProcess {
    public boolean []isPlayerOutCard = new boolean[4];    //获取玩家们是否出牌

    public NetOutCard(LandlordActivity game) {
        super(game);
    }

    //为出牌前做准备
    @Override
    public void startOutCard() {
        //初始化出牌队列
        for (int i = 0; i < 4; i++) {
            toOutCard[i] = new ArrayList<Card>();
        }

        //目前先出牌的是地主
        whoOut = game.whosLand;
        //如果是断线重连模式
        int curOutCard = -1;
        if (game.reconMode > 0) {
            JsonArray jsonArray = ReconnectDeal.reconJsonArray;
            //获取开始读取数据的index
            int index = ReconnectDeal.extraIndex;
            //读取谁是地主
            game.whosLand = GlobalValue.getDisplayIndex(
                    jsonArray.get(index++).getAsJsonObject().get("whoIsLand").getAsInt());
            //改变四个人的地主状态
            game.peoples[game.whosLand].isLandlord = true;

            //获取游戏的积分
            game.integration[0] = jsonArray.get(index++).getAsJsonObject().get("intergation").getAsInt();
            //保存当前的积分
            game.multiple = game.integration[0];
            //更新当前的底分和倍数
            game.countBottomscoreMultiple();

            //将底牌翻转
            game.process.dealLandlordCardProcess.isReconnected = true;
            game.process.dealLandlordCardProcess.turnOverDownPoke(7);

            //读取当前是哪个人出的牌
            whoOut = GlobalValue.getDisplayIndex(jsonArray.get(index++).getAsJsonObject().get("whoOut").getAsInt());
            //读取当前出牌的玩家
            curOutCard = GlobalValue.getDisplayIndex(
                    jsonArray.get(index++).getAsJsonObject().get("curOutCard").getAsInt());;
            //获取历史出牌的人
            for (int i = 0; i < 4; i++) {
                int playerIndex = GlobalValue.playersIndex[i];
                if (i != curOutCard) {
                    //先判断当前玩家是否出牌
                    if (jsonArray.get(index++).getAsJsonObject().get("hasOutCard").getAsBoolean() == true) {
                        toOutCard[playerIndex] = GlobalValue.jsonToCards(
                                jsonArray.get(index++).getAsJsonObject().get("outCards").getAsString());

                        //如果是不出，显示不出的动画
                        if (toOutCard[playerIndex].size() == 0) {
                            game.four_action_pic[playerIndex].setBackground(game.getDrawable(R.mipmap.refuse_pic));
                            game.four_action_pic[playerIndex].setVisibility(View.VISIBLE);
                            CardAnimator.alphaRun(game.four_action_pic[playerIndex], Constants.LIGHT_DURATION_TIME);
                        } else {
                            diplayUIOutCard(playerIndex);
                        }
                    }
                }
            }

            //获取当前时钟的时间，因为有动画，所以传的时间有延迟
            ReconnectDeal.clockTime = jsonArray.get(index++).getAsJsonObject().get("lastTime").getAsInt() + 2;
            //恢复发牌的速度
            game.backUpAnimator();
        }

        //显示地主的图标（淡入）
        game.fourLandPicbtn[game.whosLand].setVisibility(View.VISIBLE);
        CardAnimator.alphaRun(game.fourLandPicbtn[game.whosLand], Constants.LIGHT_DURATION_TIME);
        //更新地主的卡牌数量
        game.update_mes_cardnum(game.whosLand);

        //如果当前是断线重连状态，恢复当前出牌的人
        if (game.reconMode > 0) {
            OutCard(curOutCard);
            game.reconMode--;
        } else {
            //和给地主发牌动画保持一致
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //电脑先出牌
                    OutCard(whoOut);
                }
            }, (long) Constants.COMPUTER_THINK_TIME);
        }
    }

    //玩家出牌
    @Override
    protected void OutCardMeth(int who) {
        //当前玩家没有出牌
        isPlayerOutCard[who] = false;

        //如果到玩家出牌，且玩家不为托管状态
        if (who == 0 && game.isHosting == false) {
            //设置过30s没出牌，玩家自动出牌
            playerAutoRobort();

            //玩家出牌前的准备
            prePlayerOutCard();
        }
        //如果为玩家托管状态，自动帮其出牌
        else if (who == 0 && game.isHosting == true) {
            //设置AI出牌
            playerAIAutoOutCard();
        }
        else {
            //等待其他玩家出牌
            waitOtherPlayerOutCard(who);
        }
    }

    private final int DISPLAY_OUT_CARD = 1;
    private final int PLAY_ROBORT = 2;
    private final int NONE = 3;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISPLAY_OUT_CARD:
                    //显示出牌的动画
                    displayPlayerOutCard(msg.arg1);
                    break;
                case PLAY_ROBORT:
                    //如果玩家30s没出牌，则点击托管按钮，让其托管
                    playerRobot();
                    break;
            }
        }
    };

    //等待其他玩家出牌
    private void waitOtherPlayerOutCard(final int who) {
        //开始计时
        Message mesAction = new Message();
        mesAction.what = NONE;
        game.clockManage.setClock(Constants.OUT_CARD_CLOCK, who, handler, mesAction, game);

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

                    if (isPlayerOutCard[who] == true) {
                        Message mesAction = new Message();
                        mesAction.what = DISPLAY_OUT_CARD;
                        mesAction.arg1 = who;

                        //显示出牌的画面和逻辑处理和下一人出牌
                        handler.sendMessage(mesAction);

                        //将时钟关闭
                        game.clockManage.clockStopMes(who, game);

                        //跳出循环
                        break;
                    }
                }
            }
        }).start();
    }

    protected void playerAutoRobort() {
        //开启时钟倒计时，36s后自动托管
        Message mesAction = new Message();
        mesAction.what = PLAY_ROBORT;

        game.clockManage.setClock(Constants.OUT_CARD_CLOCK, 0, handler, mesAction, game);
    }

    //将玩家出牌的信息发给服务器
    @Override
    protected void playerOutCardToServer() {
        //在36s内出过牌了，停止计时
        game.clockManage.clockStopMes(0, game);

        //发送出牌信息
        OutCardDeal.isRobortInfo = false;
        game.process.socketClient
                .sendDeal.processSendUpdate();
    }

    //网络版得设置让AI出牌
    @Override
    protected void robotOutCardMeth() {
        //将玩家托管的信息发给服务器
        OutCardDeal.isRobortInfo = true;
        game.process.socketClient
                .sendDeal.processSendUpdate();

        //如果当前托管能出牌，让AI出牌，发给服务器
        if (game.isHosting && isCanRobotOutCard) {
            playerAIAutoOutCard();
        }
    }

    //玩家自动出牌
    private void playerAIAutoOutCard() {
        //移除当前已出牌
        for (int j = 0; j < toOutCard[0].size(); j++) {
            CardAnimator.alphaGoneRun(game.poke[toOutCard[0].get(j).cardIndex], Constants.LIGHT_DURATION_TIME, game.landlord_layout);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //设置AI出牌
                toOutCard[0] = game.peopleOperator
                        .AIOutCard(game.peoples, game.whosLand, whoOut, 0, whoOut == 0 ? null : toOutCard[whoOut]);

                //如果当前托管能出牌，让AI出牌，发给服务器
                //显示出牌画面
                displayPlayerOutCard(0);

                //将玩家出牌的信息发给服务器
                playerOutCardToServer();
            }
        }, Constants.COMPUTER_THINK_TIME);
    }
}
