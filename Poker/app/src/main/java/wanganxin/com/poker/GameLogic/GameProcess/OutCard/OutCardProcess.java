package wanganxin.com.poker.GameLogic.GameProcess.OutCard;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import wanganxin.com.poker.GameAnimation.PokeEffect.DeckOperator;
import wanganxin.com.poker.GameAnimation.PokeEffect.PokeOperator;
import wanganxin.com.poker.GameLogic.GameProcess.GameProcessEnum;
import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameAnimation.GUI.CardPosition;
import wanganxin.com.poker.GameLogic.OutCard.OutCardStyleEnum;
import wanganxin.com.poker.GameLogic.entity.CardOrderMode;
import wanganxin.com.poker.GameLogic.Operator.LandLord_GameMode;
import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.entity.PeopleActionEnum;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

//打牌阶段流程
public abstract class OutCardProcess {

    protected LandlordActivity game;

    //设置卡牌图片
    private PokeOperator pokeOperator;
    //卡牌平移之类的操作
    private DeckOperator deckOperator;
    public Boolean isCanRuse;       //判断下家是否可以不出牌
    //判断是否能托管(出牌)1、这局不处于完结未关闭中2、不能在别人出牌中
    protected Boolean isCanRobotOutCard = false;
    //当前是哪个人出的牌
    public int whoOut;
    //选择希望打出的牌数组
    public List<Card>[] toOutCard = new ArrayList[4];

    //初始化需要操作的卡牌
    public OutCardProcess(LandlordActivity game) {
        //获取当前游戏单例
        this.game = game;

        pokeOperator = game.pokeOperator;
        deckOperator = game.deckOperator;
    }

    //为出牌前做准备
    public abstract void startOutCard();

    //电脑出牌，判断出牌逻辑（单机，网络）
    protected void OutCard(final int who) {

        //将每个玩家的卡牌index和大小打印出来（测试）
        for (int i = 0; i < 4; i++) {
            Log.e("1111", "people: " + i);
            String msg = "";
            for (Card card : game.peoples[i].deck) {
                msg += card.cardIndex + "\\";
                switch (card.cardSize) {
                    case 17: msg += "大王"; break;
                    case 16: msg += "小王"; break;
                    case 15: msg += "2"; break;
                    case 14: msg += "A"; break;
                    case 13: msg += "K"; break;
                    case 12: msg += "Q"; break;
                    case 11: msg += "J"; break;
                    default: msg += card.cardSize; break;
                }
                msg += "  ";
            }
            Log.e("1111", "people: " + msg);
        }

        //电脑出牌时不能有托管
        isCanRobotOutCard = false;

        //移除当前已出牌
        for (int j = 0; j < toOutCard[who].size(); j++) {
            CardAnimator.alphaGoneRun(game.poke[toOutCard[who].get(j).cardIndex], Constants.LIGHT_DURATION_TIME, game.landlord_layout);
        }
        //移除当前家的动作信息（“不出”图片）
        game.peoples[who].actionAlphaGoneRun();

        //单机版和网络版不同的等待别人出牌
        OutCardMeth(who);
    }
    //单机版和网络版不同的等待别人出牌
    protected abstract void OutCardMeth(final int who);

    //玩家出牌前的初始化
    protected void prePlayerOutCard() {
        //其他三家出完牌，允许下家出牌
        game.outCardThreebtnVisibility(true);

        //如果当前为下家必须出牌状态
        //  1.玩家当地主必须出牌
        //  2.玩家出牌没人要必须出牌
        if (whoOut == 0) {
            //设置不能点不出按钮
            isCanRuse = false;
            game.outCardThreebtn[1].setBackground(game.getDrawable(R.mipmap.refuse_gray));
            game.outCardThreebtn[1].setEnabled(false);
        }
        else {
            //设置可以点不出按钮
            isCanRuse = true;
            game.outCardThreebtn[1].setBackground(game.getDrawable(R.mipmap.refuse));
            game.outCardThreebtn[1].setEnabled(true);
        }
        //更新玩家提示
        game.peoples[0].UpdateHintResult();

        //玩家出牌时能托管出牌
        isCanRobotOutCard = true;
    }

    //显示四人出牌（更新UI界面）
    protected void displayPlayerOutCard(int who) {
        //判断游戏是否结束
        boolean isGameOver = false;

        //如果牌可以出
        if (toOutCard[who].size() != 0) {
            //出牌的处理
            isGameOver = outCardManage(who);
        }
        //如果该家不出牌
        else {
            //设置图片为不出（淡入）（音效）
            game.peoples[who].setAction(PeopleActionEnum.REFUSED);
        }// if-else（三家是否能出牌）

        //进行下一轮出牌
        if (isGameOver == false) {
            OutCard((who + 1) % 4);
        }
    }

    //出牌的处理
    //  1.显示出牌动画
    //  2.播放出牌音效
    //  3.更新当前的卡组（逻辑，移动，卡组数量）
    //  4.更新底分和倍数
    //  5.播放更新积分和卡牌剩余提醒
    //  6.判断是否出完牌
    protected void diplayUIOutCard(int who) {
        //对OutCard的sameCardNum进行更新
        game.peopleOperator.updateCardNum(toOutCard[who]);
        //让出牌的显示规范
        LandLord_GameMode.changeOrder(toOutCard[who], CardOrderMode.CARD_DISPLAY);
        //移动出牌的位置
        movePeopleOutCard(who);

        Log.e("1111", "outCardManage: whoOut" + who);
        for (int i = 0; i < toOutCard[who].size(); i++) {
            Log.e("1111", "outCard" + i + ": " + toOutCard[who].get(i).cardSize);
        }
    }
    protected boolean outCardManage(int who) {
        //显示出牌的动画
        diplayUIOutCard(who);

        //出牌的声音，判断是出牌还是抢牌还是炸弹
        game.soundEffect.Player_OutorFollowCard_Voice(whoOut, who, toOutCard, game.peoples[who].sex);

        int orign_integration = game.integration[0];

        //逻辑更新牌，检查是否获胜，并更新积分
        Boolean isGameOver = game.peopleOperator.outCard(game.peoples[who], toOutCard[who], game.integration);

        //移动其它家的手牌
        deckOperator.movePeopleDeck(who);
        //现在为该玩家出的牌
        whoOut = who;

        //更新卡组数量
        game.update_mes_cardnum(whoOut);

        //更新当前的底分和倍数
        game.countBottomscoreMultiple();

        //播放更新积分和卡组数量
        if (orign_integration != game.integration[0]) {
            game.soundEffect.update_Integration();
        }
        if (game.peoples[who].deck.size() < 3) {
            game.soundEffect.update_DeckCount(game.peoples[who].deck.size(), game.peoples[who].sex);
        }

        //如果出完牌，进入结算画面
        if (isGameOver == true) {
            //隐藏出牌三按钮
            game.outCardThreebtnVisibility(false);

            //游戏已经结束，单击托管会无效
            isCanRobotOutCard = false;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //进行状态转移
                    game.process.gameProcessChange();
                }
            }, Constants.POKE_HORIZENTAL_DURATION * 2);
        }

        return isGameOver;
    }

    //down玩家出牌
    public void playerOutCard() {
        //清空上一轮所出的牌
        toOutCard[0].clear();

        //找在目前移动的牌
        for (int i = 0; i < game.peoples[0].deck.size(); i++) {
            int num = game.peoples[0].deck.get(i).cardIndex;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) game.poke[num].getLayoutParams();
            //如果牌被玩家点击，计算上浮的卡，将其加入出牌队列中
            if (layoutParams.bottomMargin == DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_ADD_BOTTOM)) {
                toOutCard[0].add(LandLord_GameMode.ConvertCard(num));
            }
        }

        //如果牌可以出
        if (game.peopleOperator.canOutCard(whoOut == 0 ? null : toOutCard[whoOut], toOutCard[0]) == true)  {
            //更新出的牌
            boolean isGameOver = outCardManage(0);

            //将出牌的消息发给服务器（单机不动）
            playerOutCardToServer();

            //如果游戏没有结束，则下一位玩家出牌
            if (isGameOver == false) {
                //其他三家出牌期间，下家不允许出牌
                game.outCardThreebtnVisibility(false);

                //出牌成功到下一位出牌
                OutCard(1);
            }
        }
        else {
            //如果当前牌不能出，显示出牌不符合规则
            //如果当前不能出的状态不在显示
            if (game.breakrule.getVisibility() == View.INVISIBLE) {

                //提示没有比当前大的牌（淡入）
                game.breakrule.setVisibility(View.VISIBLE);
                CardAnimator.alphaRun(game.breakrule, Constants.LIGHT_DURATION_TIME);

                //过一段时间淡出
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //设置淡出
                        CardAnimator.alphaGoneRun(game.breakrule, Constants.LIGHT_DURATION_TIME);
                    }
                }, Constants.BREAKRULE_TIME);
            }

            //播放不能出的音效
            game.soundEffect.BreakRule();
        }
    }
    //玩家出牌成功后的方法（单机不做事）（网络将消息发给服务器）
    protected abstract void playerOutCardToServer();

    //down玩家提示
    public void playerHint() {
        //所有的牌都拉下来，显示下家的牌
        putDownPoke();

        //根据上家打出的牌做提示
        List<Card> preOutCard = Card.CopyListCard(whoOut == 0 ? null : toOutCard[whoOut]);
        List<Card> TCard = game.peopleOperator.remind(game.peoples[0], preOutCard);

        //如果当前有牌出
        if (TCard.size() != 0) {
            //移动出的牌位置
            for (int j = 0; j < TCard.size(); j++) {
                pokeOperator.pokeUpDown(game.poke[TCard.get(j).cardIndex], true);
            }
        }
        else {
            //如果当前没牌可出
            if (game.peoples[0].canOutCard == false
                    && game.cannotOutHint.getVisibility() == View.INVISIBLE
                    && game.peoples[0].htStyle == OutCardStyleEnum.CANT_OUT) {

                //提示没有比当前大的牌（淡入）
                game.cannotOutHint.setVisibility(View.VISIBLE);
                CardAnimator.alphaRun(game.cannotOutHint, Constants.LIGHT_DURATION_TIME);

                // 过两秒钟隐藏
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //设置淡出
                        CardAnimator.alphaGoneRun(game.cannotOutHint, Constants.LIGHT_DURATION_TIME);
                    }
                }, Constants.BREAKRULE_TIME);
            }

            //播放不能出的音效
            game.soundEffect.BreakRule();
        }
    }

    //将下家在上面的牌放下来
    public void putDownPoke() {
        for (int i = 0; i < game.peoples[0].deck.size(); i++) {
            int num = game.peoples[0].deck.get(i).cardIndex;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) game.poke[num].getLayoutParams();
            //如果当前排为在上
            if (layoutParams.bottomMargin == DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_ADD_BOTTOM)) {
                //将下家在上面的牌放下来
                pokeOperator.pokeUpDown(game.poke[num], false);
            }
        }
    }

    //down玩家不出
    public void playerRefuse() {
        //如果玩家可以点击不出
        if (isCanRuse == true) {
            //设置不出的图片（淡入）
            game.peoples[0].setAction(PeopleActionEnum.REFUSED);
            //把玩家的牌回归原样
            putDownPoke();
            //设置淡出
            CardAnimator.alphaGoneRun(game.breakrule, Constants.LIGHT_DURATION_TIME);
            //因为是不出，把当前玩家的牌清空
            toOutCard[0].clear();
            //其他三家出牌期间，下家不允许出牌
            game.outCardThreebtnVisibility(false);
            //将不出的消息发给服务器（单机不动）
            playerOutCardToServer();
            //下一家出牌
            OutCard(1);
        }
    }

    //down玩家按托管
    public void playerRobot() {
        //只有在出牌阶段才可以托管
        if (game.process.step == GameProcessEnum.OUT_CARD_PROCESS) {
            //如果当前没有托管
            if (game.isHosting == false) {
                //显示托管的图标（淡入）
                CardAnimator.alphaRun(game.robots[0], Constants.LIGHT_DURATION_TIME);
                //设置托管中
                game.isHosting = true;
                //显示正在托管
                game.show_fourpeoplechat(0, "开始托管");

                //不处于最后的完结关闭中
                if (isCanRobotOutCard == true) {
                    //隐藏出牌三按钮
                    game.outCardThreebtnVisibility(false);
                    //将牌下移
                    putDownPoke();
                }
            }
            else {
                CardAnimator.alphaGoneRun(game.robots[0], Constants.LIGHT_DURATION_TIME);
                game.isHosting = false;
            }

            //让AI帮玩家出牌，发送托管信息给服务器
            robotOutCardMeth();
        }
        else {
            Toast.makeText(game.getApplicationContext(), "游戏尚未开始，不能托管！", Toast.LENGTH_SHORT).show();
        }
    }
    //托管的出牌方式
    protected abstract void robotOutCardMeth();

    //移动四个玩家出牌
    private void movePeopleOutCard(final int who) {
        //计算移动四个玩家出牌的位置
        countOutCardPosition(who);

        //等待计算完移动牌
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int j = 0; j < toOutCard[who].size(); j++)  {
                    final int num = toOutCard[who].get(j).cardIndex;
                    if (who != 0) {
                        //如果不是下家则要显示牌的图片
                        pokeOperator.setPokePicture(num, false, false);
                        //移动牌
                        game.poke[num].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP));
                    }

                    //动画出牌效果（平移，缩放）
                    CardAnimator.OutCardRun(game.poke[num], game.temptPoke[num], Constants.POKE_HORIZENTAL_DURATION, who);
                }
            }
        }, Constants.WAIT_FOR_COUNT);
    }

    //计算移动四个玩家出牌的位置
    private void countOutCardPosition(final int who) {
        //更新出的牌
        RelativeLayout.LayoutParams layoutParams;
        //获取当前玩家第一张牌的位置
        int downPosition = CardPosition.getOutPokePosition(game.getApplicationContext(), who, game.d, toOutCard[who].size());

        //移动出的牌位置
        for (int j = 0; j < toOutCard[who].size(); j++)  {
            int num = toOutCard[who].get(j).cardIndex;
            //先删除原本的卡
            game.landlord_layout.removeView(game.temptPoke[num]);
            //获取各家layout规则
            layoutParams = CardPosition.getOutPokeLayoutParams(game.getApplicationContext(), who);

            //只有右家需要是从右开始的
            //右家距离是越来越近
            if (who != 1) {
                layoutParams.leftMargin = downPosition;
                downPosition += game.card_small_interval;
            }
            else {
                layoutParams.rightMargin = downPosition;
                downPosition -= game.card_small_interval;
            }

            //增加修改后的卡
            game.landlord_layout.addView(game.temptPoke[num], layoutParams);
        }

        //刷新卡组的更新顺序
        for (int j = 0; j < toOutCard[who].size(); j++) {
            final int num = toOutCard[who].get(j).cardIndex;
            game.landlord_layout.removeView(game.poke[num]);
            game.landlord_layout.addView(game.poke[num]);
        }
    }
}
