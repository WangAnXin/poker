package wanganxin.com.poker.GameLogic.GameProcess.CallScore;

import android.os.Handler;

import java.util.Random;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.utilities.Constants;

public class SingleCallScore extends CallScoreProcess {
    //随机函数
    private Random ran;

    public SingleCallScore(LandlordActivity game) {
        super(game);
    }

    //（当前玩家叫前，为第一轮叫分，当前玩家叫后，为第二轮叫分）
    @Override
    public void startCallScore() {
        ran = new Random();
        //从哪个人开始叫分，ran.Next随机0~3的数
        begin_pos = ran.nextInt(4);
        end_pos = begin_pos + 4;

        //第一个人开始叫分
        showCallResult();
    }

    //单机版持续等待，直至玩家叫分
    @Override
    protected void playerAutoCallScore() {}

    //单机版直接进行下一个叫分
    @Override
    public void playerScoreMeth(int score) {
        //开始对下一个进行叫分
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showCallResult();
            }
        }, (long) Constants.COMPUTER_THINK_TIME);
    }

    //其他玩家叫分的重写
    @Override
    protected void othersCallScore(int who) {
        //随机叫几分，随机[integration[0]+1,4)的数，4为不叫
        int curScore = ran.nextInt(Constants.RandomCallLandlord - (game.integration[0] + 1)) + game.integration[0] + 1;

        //显示叫分的动画
        otherPlayerCallScoreDisplay(curScore, who);

        //等待一段时间下一位开始叫分
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //下一位开始叫分
                showCallResult();

            }
        }, (long) Constants.COMPUTER_THINK_TIME);
    }

    //单机，结束直接进入下一环节
    //@Override
    //protected void gameProcessChange() {
        ////进行游戏状态的切换
        //game.process.gameProcessChange();
    //}
}
