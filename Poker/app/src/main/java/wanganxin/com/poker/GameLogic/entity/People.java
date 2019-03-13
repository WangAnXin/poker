package wanganxin.com.poker.GameLogic.entity;

import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.Operator.PeopleOperator;
import wanganxin.com.poker.GameLogic.OutCard.OutCardStyleEnum;
import wanganxin.com.poker.GameLogic.utilities.Constants;

//人手中牌的类
public class People {
    public String name = null;    //玩家姓名
    public boolean sex;       //玩家性别
    public int image;      //玩家图片
    public int netIndex;       //当前玩家在网络的位置（客户端中当前玩家永远为下家）
    public int playIndex = -1;       //当前玩家显示的位置

    public List<Card> deck = new ArrayList<Card>();    //玩家的卡组

    public int integration = 0;            //积分
    public CardOrderMode orderMode;    //牌序方式，0是从大到小排，1是按牌的个数排

    public boolean isRemind;           //是否点了提示
    public boolean isLandlord;         //是否是地主
    public boolean isWin;              //是否出完牌了
    public int outCardNum;          //出过牌的次数

    public List<Card> htCards = new ArrayList<Card>();       //提示能出的牌
    public OutCardStyleEnum htStyle;
    public boolean isOutByMy;        //提示的时候是不是自己出牌
    public boolean canOutCard;      //记录是否可以出牌
    public boolean isRobot = false;         //记录当前玩家是否为电脑

    LandlordActivity game = null;          //绑定对应的游戏activity

    //初始化
    public People(LandlordActivity landlordActivity) {
        game = landlordActivity;
    }

    public People() {
        Init_AfterTurn();
    }

    //一局以后清空的函数
    public void Init_AfterTurn() {
        //deck.clear();
        htStyle = OutCardStyleEnum.CANT_OUT;
        htCards.clear();
        orderMode = CardOrderMode.CARD_SIZE;
        isRemind = false;
        isLandlord = false;          //当前为农民
        outCardNum = 0;           //初始化没出过牌
        isWin = false;               //初始化为输
        canOutCard = false;         //初始化为不能
    }

    //更改hintResult
    public void UpdateHintResult() {
        isRemind = false;
        htCards.clear();
        htStyle = OutCardStyleEnum.CANT_OUT;
        canOutCard = false;
    }

    //设置当前玩家是否处于托管状态
    public void setRobot(boolean isRobot) {
        //获取当前游戏显示
        if (isRobot == true) {
            //显示托管的图标（淡入）
            CardAnimator.alphaRun(game.robots[playIndex], Constants.LIGHT_DURATION_TIME);
            this.isRobot = true;
        } else {
            CardAnimator.alphaGoneRun(game.robots[playIndex], Constants.LIGHT_DURATION_TIME);
            this.isRobot = false;
        }
    }

    //设置当前玩家的图片
    public void setImage(int image) {
        //如果图片超出范围限制为1
        if (image < Constants.MIN_IMAGE || image > Constants.MAX_IMAGE) {
            image = Constants.MIN_IMAGE;
        }

        this.image = image;

        //发送消息给显示界面，设置玩家的图片
        Message mes = game.handler.obtainMessage(LandlordActivity.SET_ROBOT);
        mes.arg1 = playIndex;
        mes.arg2 = image;
        mes.sendToTarget();
    }

    //设置玩家的姓名
    public void setName(String name) {
        this.name = name;

        game.mes[playIndex].setText(name);

        //同时设置积分面板和结束面板上的姓名
        game.score_panel_score[playIndex][0].setText(name);
        game.end_panel_score[playIndex][0].setText(name);
    }

    //设置玩家的积分
    public void setScore(int score) {
        this.integration = score;

        //更新上局积分（一开始上局积分为0）
        game.score_panel_score[playIndex][1].setText(Integer.toString(0));
        //更新当前总积分
        game.score_panel_score[playIndex][2].setText(Integer.toString(integration));
    }

    //设置当前玩家的动作（开始，一分，二分，三分，不叫，不出）
    // 淡入
    // 配音
    public void setAction(PeopleActionEnum peopleActionEnum) {
        Drawable btnDrawable = null;

        //根据玩家的动作挑选图片
        switch (peopleActionEnum) {
            case PREAPARE:
                btnDrawable = game.getResources().getDrawable(R.mipmap.start_pic, game.getTheme());
                break;

            case ONE_SCORE:
                btnDrawable = game.getResources().getDrawable(R.mipmap.onescore_pic, game.getTheme());
                //播放叫分的声音（叫分或抢地主）
                game.soundEffect.CallScore_RobLandlord(game.integration[0], sex);
                break;

            case TWO_SCORE:
                btnDrawable = game.getResources().getDrawable(R.mipmap.twoscore_pic, game.getTheme());
                //播放叫分的声音（叫分或抢地主）
                game.soundEffect.CallScore_RobLandlord(game.integration[0], sex);
                break;

            case THREE_SCORE:
                btnDrawable = game.getResources().getDrawable(R.mipmap.threescore_pic, game.getTheme());
                //播放叫分的声音（叫分或抢地主）
                game.soundEffect.CallScore_RobLandlord(game.integration[0], sex);
                break;

            case NO_SCORE:
                btnDrawable = game.getResources().getDrawable(R.mipmap.noscore_pic, game.getTheme());
                //播放不叫的声音
                game.soundEffect.NoCallScore_NoRobLandlord(game.integration[0], sex);
                break;

            case REFUSED:
                btnDrawable = game.getResources().getDrawable(R.mipmap.refuse_pic, game.getTheme());
                //播放不出的音效
                game.soundEffect.Player_Refuse_Voice(sex);
                break;
        }

        //设置图片并将其淡入
        game.four_action_pic[playIndex].setBackground(btnDrawable);
        game.four_action_pic[playIndex].setVisibility(View.VISIBLE);
        CardAnimator.alphaRun(game.four_action_pic[playIndex], Constants.LIGHT_DURATION_TIME);
    }

    //将当前的动作显示淡出
    public synchronized void actionAlphaGoneRun() {
        CardAnimator.alphaGoneRun(game.four_action_pic[playIndex], Constants.LIGHT_DURATION_TIME);
    }

    //玩家离开（清空姓名，积分，图片，准备动画）
    public void leave() {
        String nullString = "";

        //设置玩家姓名为空
        name = nullString;

        game.mes[playIndex].setText(nullString);

        //移除积分面板和结束面板上的姓名
        game.score_panel_score[playIndex][0].setText(nullString);
        game.end_panel_score[playIndex][0].setText(nullString);

        //移除上局积分（一开始上局积分为0）
        game.score_panel_score[playIndex][1].setText(nullString);
        //移除当前总积分
        game.score_panel_score[playIndex][2].setText(nullString);

        //移除玩家图片
        //game.peoplesImage[playIndex].setBackground(null);
        //发送消息给显示界面，设置玩家的图片
        Message mes = game.handler.obtainMessage(LandlordActivity.SET_ROBOT);
        mes.arg1 = playIndex;
        mes.arg2 = Constants.MAX_IMAGE + 1;
        mes.sendToTarget();

        //移除准备信息
        actionAlphaGoneRun();

        //移除托管信息
        setRobot(false);
    }

    //设置玩家的信息和准备信息
    public void setPlayerInfo(Player player, boolean isReady) {
        setName(player.name);
        setScore(player.score);
        setImage(player.image);
        sex = player.sex;
        setRobot(player.isRobot);

        //如果准备了，设置相应图片
        if (isReady == true) {
            setAction(PeopleActionEnum.PREAPARE);
        } else {
            actionAlphaGoneRun();
        }
    }

    //设置玩家的卡组（初始化设置，排序会变成按照牌的大小排序）
    public void setPlayerDeck(List<Card> cards) {
        deck = cards;
        PeopleOperator.updateCardNum(deck);
    }
}
