package wanganxin.com.poker.GameLogic.entity;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.ReconnectDeal;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

//控制时钟行为的类
public class Clock {
    //设置时钟开始叫分
    public void setClock(int action, int who, Handler actionHandler, Message mes, LandlordActivity game) {
        //准备闹钟的布局
        prepareClock(action, who, game);
        //开启时钟
        startClock(action, actionHandler, mes, who, game);
    }
    //准备闹钟的布局
    private void prepareClock(int action, int who, LandlordActivity game) {
        //设置时钟的位置
        setClockLayout(action, who, game);

        //设置可见
        game.fourClock[who].setVisibility(View.VISIBLE);
        CardAnimator.alphaRun(game.fourClock[who], Constants.LIGHT_DURATION_TIME);

        //设置时钟的图片
        switch (action) {
            case Constants.CALL_SCORE_CLOCK: {
                game.fourClock[who].setBackground(game.resources.getDrawable(R.mipmap.clock_callscore_1, game.getTheme()));
            }
            break;

            case Constants.OUT_CARD_CLOCK: {
                game.fourClock[who].setBackground(game.resources.getDrawable(R.mipmap.clock_outcard_1, game.getTheme()));
            }
            break;
        }
    }

    //如果是当前玩家要按照出牌和叫分来改变位置
    private void setClockLayout(int action, int who, LandlordActivity game) {

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(game.fourClock[who].getLayoutParams());
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.start);
        layoutParams.bottomMargin = DensityUtil.dip2px(game.getApplicationContext(), -10);
        switch (action) {
            case Constants.CALL_SCORE_CLOCK: {
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.onescore);
                layoutParams.rightMargin = DensityUtil.dip2px(game.getApplicationContext(), 16);
            }
            break;

            case Constants.OUT_CARD_CLOCK: {
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.outcard);
                layoutParams.rightMargin = DensityUtil.dip2px(game.getApplicationContext(), 25);
            }
            break;
        }

        //如果是当前玩家要按照出牌和叫分来改变位置
        if (who == 0) {
            game.fourClock[who].setLayoutParams(layoutParams);
        }
    }

    public boolean[] isClockWork = new boolean[4];       //设置四个时钟是否处于开启中
    private Thread[] clockThread = new Thread[4];        //设置四个时钟线程防止意外关错
    private void startClock(final int action, final Handler actionHandler, final Message mes, final int who, final LandlordActivity game) {
        //如果指定时间了，按照指定的时间来，否则按照正常的时间
        final int circleTime = action == Constants.CALL_SCORE_CLOCK ? 12 : 36;
        //设置起始时间(如果是重连则设置指定的时间否则)
        if (game.reconMode > 0) {
            game.reconMode--;
            clockTime = ReconnectDeal.clockTime;
        } else {
            clockTime = 1;
        }
        //设置闹钟开始
        isClockWork[who] = true;

        clockThread[who] = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isClockWork[who]) {
                    try {
                        //如果是首次要将淡入的时间减去
                        if (clockTime == 0) {
                            Thread.sleep(1000 - Constants.LIGHT_DURATION_TIME);
                        } else {
                            Thread.sleep(1000);
                        }

                        if (clockTime + 4 > circleTime) {
                            game.soundEffect.clockRemind();
                        }
                        //执行传递过来的任务
                        if (clockTime >= circleTime) {
                            //发送消息，使时钟暂停
                            actionHandler.sendMessage(mes);
                            //让1显示1s，消去时钟
                            Thread.sleep(500);
                            clockStopMes(who, game);
                            break;
                        }
                        Message mesAction = new Message();
                        mesAction.what = UPDATE_CLOCK;
                        mesAction.obj = new ClockMsg(game, clockTime + 1, action, who);
                        clockTime++;

                        handler.sendMessage(mesAction);
                        //Log.e("1111", "clock: " + "who" + who +", clockTime:" + clockTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        clockThread[who].start();

        return ;
    }

    //Clock和handler之间传递的信息
    class ClockMsg {
        public LandlordActivity game;
        public int clockTime;
        public int action;
        public int who;

        ClockMsg(LandlordActivity game, int clockTime, int action, int who) {
            this.game = game;
            this.clockTime = clockTime;
            this.action = action;
            this.who = who;
        }
    }

    //让时钟暂停的handler处理方法
    public void clockStopMes(int who, LandlordActivity game) {
        Message mesGone = new Message();
        mesGone.what = CLOCK_GONE;
        mesGone.arg1 = who;
        mesGone.obj = game;
        handler.sendMessage(mesGone);
    }

    //让时钟暂停显示
    private void stopClock(int who, LandlordActivity game) {
        //停止时钟显示
        isClockWork[who] = false;
        CardAnimator.alphaGoneRun(game.fourClock[who], Constants.LIGHT_DURATION_TIME);
        while (clockThread[who] != null && clockThread[who].isAlive() == true) {
            clockThread[who].interrupt();
        }
    }

    //让时钟没过一秒开始计时
    private int clockTime = 0;
    private static final int UPDATE_CLOCK = 1;
    private static final int CLOCK_GONE = 2;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CLOCK: {
                    //获取ClockMsg中的信息
                    ClockMsg clockMsg = (ClockMsg)msg.obj;
                    int clockTime = clockMsg.clockTime;
                    LandlordActivity game = clockMsg.game;
                    int who = clockMsg.who;
                    int action = clockMsg.action;

                    //更改闹钟的图片
                    int id = 0;
                    try {
                        Field field = R.mipmap.class.getField(
                                (action == Constants.CALL_SCORE_CLOCK ? "clock_callscore_" : "clock_outcard_")
                                        + Integer.toString(clockTime));
                        id = field.getInt(new R.mipmap());
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    Drawable btnDrawable = game.resources.getDrawable(id, game.getTheme());
                    game.fourClock[who].setBackground(btnDrawable);
                }
                break;

                case CLOCK_GONE: {
                    stopClock(msg.arg1, (LandlordActivity)msg.obj);
                }
                break;
            }
        }
    };
}
