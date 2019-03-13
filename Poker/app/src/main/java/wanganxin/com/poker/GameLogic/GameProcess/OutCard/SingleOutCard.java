package wanganxin.com.poker.GameLogic.GameProcess.OutCard;

import android.os.Handler;
import android.view.View;

import java.util.ArrayList;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.utilities.Constants;

//单机版出卡
public class SingleOutCard extends OutCardProcess {

    public SingleOutCard(LandlordActivity game) {
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

        //显示地主的图标（淡入）
        game.fourLandPicbtn[game.whosLand].setVisibility(View.VISIBLE);
        CardAnimator.alphaRun(game.fourLandPicbtn[game.whosLand], Constants.LIGHT_DURATION_TIME);
        //更新地主的卡牌数量
        game.update_mes_cardnum(game.whosLand);

        //和给地主发牌动画保持一致
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //电脑先出牌
                OutCard(whoOut);
            }
        }, (long) Constants.COMPUTER_THINK_TIME);
    }

    //电脑出牌，判断出牌逻辑
    @Override
    protected void OutCardMeth(final int who) {
        //如果到玩家出牌，且玩家不为托管状态
        if (who == 0 && game.isHosting == false) {
            //玩家出牌前的准备
            prePlayerOutCard();
        }
        else {
            //获取电脑当前要出的牌
            toOutCard[who] = game.peopleOperator
                    .AIOutCard(game.peoples, game.whosLand, whoOut, who, whoOut == who ? null : toOutCard[whoOut]);

            //调用更新UI界面
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayPlayerOutCard(who);
                }
            }, Constants.COMPUTER_THINK_TIME);
        }
    }

    //将玩家出牌的信息发给服务器，单机版不动
    @Override
    protected void playerOutCardToServer() {}

    //单机版直接让电脑出牌
    @Override
    protected void robotOutCardMeth() {
        if (isCanRobotOutCard) {
            //电脑替玩家开始出牌
            OutCard(0);
        }
    }
}
