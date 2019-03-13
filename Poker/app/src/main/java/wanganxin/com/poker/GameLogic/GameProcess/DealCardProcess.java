package wanganxin.com.poker.GameLogic.GameProcess;

import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.PokeEffect.DeckOperator;
import wanganxin.com.poker.GameAnimation.PokeEffect.PokeOperator;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameAnimation.GUI.CardPosition;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;


//发牌动画流程类//
public class DealCardProcess {
    protected LandlordActivity game;

    //设置卡牌图片
    private PokeOperator stp;
    //卡牌平移之类的操作
    private DeckOperator deckOperator;

    //四家起始位置
    private int []position = new int[4];
    //是否改变位置
    private boolean []isChangePosition = new boolean[4];

    //初始化需要操作的卡牌
    public DealCardProcess(LandlordActivity game) {
        //获取当前游戏单例
        this.game = game;

        stp = game.pokeOperator;
        deckOperator = game.deckOperator;

        initDealCardAnimator();
    }

    //下一轮开始进行初始化
    public void initDealCardAnimator() {
        //一开始发牌动画的位置没有改变
        for (int i = 0; i < 4; i++) {
            isChangePosition[i] = false;
        }
        //在发牌前删除牌面上所有的扑克牌
        game.removeAllPoke();
    }

    /*
    //发牌动画流程//
    1.计算四个玩家第一张牌的初始位置（发牌预处理）
    2.四个玩家开始发牌（卡牌平移淡入）
    3.做发完牌的处理，和底牌的预处理
        3.1.刷新卡牌阴影
        3.2.更新卡牌数量信息
        3.3.计算底牌第一张牌的位置
    4.下一步动画
        4.1.将下家玩家（主玩家）卡牌翻转（显示卡牌）
        4.2.发底牌（底牌平移淡入）
    5.开始进入叫分环节
     */
    //显示发牌的动画
    //发牌动画的初始化处理，计算起始坐标并加入第一张牌
    public void startDealCard() {
        //音效开始游戏
        game.soundEffect.StartGame();

        //更新四个人的动作显示，去掉开始图片
        for (int i = 0; i < 4; i++) {
            game.peoples[i].actionAlphaGoneRun();
        }

        //播放开始发牌的音效
        game.soundEffect.DealCard();

        //计算下家第一张牌的布局
        Log.e("1111", String.format("startDealCard: %d", game.peoples[0].deck.size() - 1));

        int curCardNum = game.peoples[0].deck.get(game.peoples[0].deck.size() - 1).cardIndex;
        //初始化下家显示牌的起始位置
        position[0] = CardPosition.getDownPokePosition(game.getApplicationContext(), game.d, game.peoples[0].deck.size(), false);
        RelativeLayout.LayoutParams layoutParams = CardPosition.getDownPokeLayoutParams(game.getApplicationContext());
        layoutParams.leftMargin = position[0];
        game.landlord_layout.addView(game.poke[curCardNum], layoutParams);

        //设置卡牌的图片，一开始下家为背面
        stp.setPokePicture(curCardNum, true, false);
        //第一张牌设置淡入效果
        CardAnimator.alphaRun(game.poke[curCardNum], Constants.DEALCARD_DURATION_TIME);
        //设置叠加顺序
        game.poke[curCardNum].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP) + 1);

        //记录当前牌的数量
        final int[] fourCardNum = new int[4];
        for (int i = 0; i < 4; i++) {
            fourCardNum[i] = game.peoples[i].deck.size() - 2;
        }

        //初始化其余人的初始位置
        for (int i = 1; i < 4; i++) {
            //获取其余三人的第一张牌
            curCardNum = game.peoples[i].deck.get(game.peoples[i].deck.size() - 1).cardIndex;
            //设置卡牌的图片
            stp.setPokePicture(curCardNum, !game.isBrightCard, false);

            //设置layout
            layoutParams = CardPosition.getTurnOverPokeLayoutParams(game.getApplicationContext(), i);
            //计算其余三家牌的位置
            //在17~24，8张牌的时候换行
            if (fourCardNum[i] < Constants.excursion_num + 2 && !isChangePosition[i]) {
                position[i] = CardPosition.getTurnOverPokePosition(game.getApplicationContext(), i, game.d, fourCardNum[i] + 1);
                isChangePosition[i] = true;
                if (i == 2) {
                    //上家是改topMargin
                    layoutParams.topMargin = 0;
                } else {
                    //左右两家是改bottomMargin
                    layoutParams.bottomMargin = 0;
                }
                game.poke[curCardNum].setElevation(game.poke[curCardNum].getCardElevation() - 25);   //设置叠加顺序
            } else {
                position[i] = CardPosition.getTurnOverPokePosition(game.getApplicationContext(), i, game.d
                        , game.peoples[i].deck.size() - 1 - Constants.excursion_num);
            }

            //如果是左家为靠左
            if (i == 3) {
                layoutParams.leftMargin = position[i];
            }else {
                //上家和右家为靠右
                layoutParams.rightMargin = position[i];
            }

            game.landlord_layout.addView(game.poke[curCardNum], layoutParams);

            //第一张牌设置淡入效果
            CardAnimator.alphaRun(game.poke[curCardNum], Constants.DEALCARD_DURATION_TIME);
            //设置叠加顺序
            game.poke[curCardNum].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP) + 1 + 25);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //四家的发牌效果
                show_DealCard(fourCardNum);
            }
        }, (long) Constants.DEALCARD_DURATION_TIME);
    }

    //发牌动画(淡入平移动画)
    private void show_DealCard(final int[] fourCardNum) {
        //如果下家还有牌
        if (fourCardNum[0] >= 0) {
            //发下家的牌
            int curCardNum = game.peoples[0].deck.get(fourCardNum[0]).cardIndex;
            RelativeLayout.LayoutParams layoutParams = CardPosition.getDownPokeLayoutParams(game.getApplicationContext());
            layoutParams.leftMargin = position[0];

            //设置卡牌的图片，一开始下家为背面
            stp.setPokePicture(curCardNum, true, false);
            game.landlord_layout.addView(game.poke[curCardNum], layoutParams);
            CardAnimator.horizentalRun(game.poke[curCardNum], 0, -game.card_interval, Constants.DEALCARD_DURATION_TIME, true);
            position[0] -= game.card_interval;
            //设置叠加顺序
            game.poke[curCardNum].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP)
                    + game.peoples[0].deck.size() - fourCardNum[0]);
        }

        //发三家的牌
        for (int j = 1; j < 4; j++) {
            //如果当前家没牌了，跳过
            if (fourCardNum[j] < 0) {
                continue;
            }

            int curCardNum = game.peoples[j].deck.get(fourCardNum[j]).cardIndex;
            //设置卡牌的图片
            stp.setPokePicture(curCardNum, !game.isBrightCard, false);

            //设置layout对齐
            RelativeLayout.LayoutParams layoutParams = CardPosition.getTurnOverPokeLayoutParams(game.getApplicationContext(), j);
            game.poke[curCardNum].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP)
                    + game.peoples[j].deck.size() - fourCardNum[j] + 25);   //设置叠加顺序

            //在17~24，8张牌的时候换行
            if (fourCardNum[j] <= Constants.excursion_num && !isChangePosition[j]) {
                position[j] = CardPosition.getTurnOverPokePosition(game.getApplicationContext(), j, game.d, fourCardNum[j] + 1);
                //到最后一家时再改变
                isChangePosition[j] = true;
                if (j != 3) {
                    position[j] -= game.card_small_interval;
                }
            }

            //其余三家一行牌放不下，给牌换行
            if (isChangePosition[j]) {
                if (j == 2) {
                    //上家是改topMargin
                    layoutParams.topMargin = 0;
                } else {
                    //左右两家是改bottomMargin
                    layoutParams.bottomMargin = 0;
                }
                game.poke[curCardNum].setElevation(game.poke[curCardNum].getCardElevation() - 25);   //设置叠加顺序
            }
            game.landlord_layout.addView(game.poke[curCardNum], layoutParams);


            if (j == 3) {
                //如果是左家为靠左
                layoutParams.leftMargin = position[j];
                CardAnimator.horizentalRun(game.poke[curCardNum], 0, -game.card_small_interval, Constants.DEALCARD_DURATION_TIME, true);
                position[j] -= game.card_small_interval;
            } else {
                //上家和右家为靠右
                layoutParams.rightMargin = position[j];
                CardAnimator.horizentalRun(game.poke[curCardNum], 0, game.card_small_interval, Constants.DEALCARD_DURATION_TIME, false);
                position[j] += game.card_small_interval;
            }
        }

        //递归调用，发下一张牌
        boolean isStillCards = false;
        for (int i = 0; i < 4; i++) {
            fourCardNum[i]--;
            if (fourCardNum[i] >= 0) {
                isStillCards = true;
            }
        }
        if (isStillCards == true) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    show_DealCard(fourCardNum);
                }
            }, (long) Constants.DEALCARD_DURATION_TIME);
        }
        else {  //如果发完了
            //如果处于断线重连模式，有可能画面显示有错误，先将四人的卡组重新刷一遍
            if (game.reconMode > 0) {
                deckOperator.freshPlayerDeck();
            }
            //底牌动画预处理
            preOperShowUnderCard();
        }
    }

    //发完牌的处理，底牌动画预处理
    private void preOperShowUnderCard() {
        //刷牌，使所有牌的阴影相同
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int j = 1; j < 4; j++) {
                    deckOperator.freshOthersDeck(j);
                }
            }
        }, (long) Constants.DEALCARD_DURATION_TIME);

        //更新卡组信息
        for (int cardnum = 0; cardnum < 4; cardnum++) {
            game.update_mes_cardnum(cardnum);
        }
        //将下家的牌翻转过来
        showAnimator_OverTurnDown(game.peoples[0].deck.size() - 1);

        //初始化发底牌的动画效果
        //获取底牌的初始位置
        position[0] = CardPosition.getUnderPokePosition(game.getApplication());
        //加入第一张底牌
        RelativeLayout.LayoutParams layoutParams = CardPosition.getUnderPokeLayoutParams(game.getApplicationContext());
        layoutParams.rightMargin = position[0];

        //设置底牌为不可见状态
        stp.setPokePicture(7, true, true);
        game.landlord_layout.addView(game.downpoke[7], layoutParams);

        //第一张牌设置淡入效果
        CardAnimator.alphaRun(game.downpoke[7], Constants.DEALCARD_DURATION_TIME);

        //设置叠加顺序
        game.downpoke[7].setElevation(game.downpoke[7].getCardElevation() + 1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //开始发底牌
                showUnderCard(6);
            }
        }, (long) Constants.DEALCARD_DURATION_TIME);
    }

    //设置下家卡牌动画翻转效果
    private void showAnimator_OverTurnDown(final int i) {
        final float start;
        final float end1;
        final float end2;
        if (i < game.peoples[0].deck.size()) {
            final int num = game.peoples[0].deck.get(i).cardIndex;
            start = 180.0F;
            end1 = start / Constants.beishu * (Constants.beishu - 1);
            end2 = 0f;
            final int duration = (int)(Constants.UNDERPOKE_DURATION_TIME);//设置翻转时间
            CardAnimator.rotationYRun(game.poke[num], (long)(duration / Constants.beishu), start, end1);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stp.setPokePicture(num, false, false); //再从背面转换为正面
                    game.poke[num].setElevation(i * 2 + game.poke[num].getElevation());
                }
            },duration / 2 + Constants.WAIT_FOR_COUNT);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CardAnimator.rotationYRun(game.poke[num], (long)(duration * (Constants.beishu - 1) / Constants.beishu), end1, end2);
                    if (i > 0) {
                        //从右至左
                        showAnimator_OverTurnDown(i - 1);
                    }
                    else if (i == 0) {
                        //全部翻转完后，将阴影相同，刷牌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < game.peoples[0].deck.size(); i++) {
                                    int num = game.peoples[0].deck.get(i).cardIndex;
                                    game.landlord_layout.removeView(game.poke[num]);
                                    game.landlord_layout.addView(game.poke[num]);
                                    game.poke[num].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP));
                                }
                            }
                        }, (long)(duration * (Constants.beishu - 1) / Constants.beishu));
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stp.setPokePicture(game.peoples[0].deck.get(i).cardIndex, false, false);
                        }
                    }, (long)(duration * (Constants.beishu - 1) / Constants.beishu) + Constants.WAIT_FOR_COUNT);
                }
            }, (long) (duration / Constants.beishu));
        }
    }

    //底牌动画显示(淡入平移动画)
    private void showUnderCard(final int curDownCardNum) {
        //获取底牌卡的布局
        RelativeLayout.LayoutParams layoutParams = CardPosition.getUnderPokeLayoutParams(game.getApplicationContext());
        layoutParams.rightMargin = position[0];
        //设置底牌为不可见状态
        stp.setPokePicture(curDownCardNum, true, true);
        game.landlord_layout.addView(game.downpoke[curDownCardNum], layoutParams);
        CardAnimator.horizentalRun(game.downpoke[curDownCardNum], 0, game.card_small_interval, Constants.DEALCARD_DURATION_TIME, false);
        position[0] += game.card_small_interval;
        //设置叠加顺序
        game.downpoke[curDownCardNum].setElevation(game.downpoke[curDownCardNum].getCardElevation() + 8 - curDownCardNum);
        if (curDownCardNum > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showUnderCard(curDownCardNum - 1);
                }
            }, (long) Constants.DEALCARD_DURATION_TIME);
        }
        //四个人的牌都发完了，开始发底牌
        else {
            //进行下一步状态转移
            game.process.gameProcessChange();
            //nextStepChange();
        }
    }

    //进行下一步状态转移（单机，网络）
    //protected abstract void nextStepChange();

}
