package wanganxin.com.poker.GameLogic.GameProcess;

import android.os.Handler;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.PokeEffect.DeckOperator;
import wanganxin.com.poker.GameAnimation.PokeEffect.PokeOperator;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

//发地主牌的进程
public class DealLandlordCardProcess {

    private LandlordActivity game;

    //设置卡牌图片
    private PokeOperator pokeOperator;
    //卡牌平移之类的操作
    private DeckOperator deckOperator;

    //初始化需要操作的卡牌
    public DealLandlordCardProcess(LandlordActivity game) {
        //获取当前游戏单例
        this.game = game;

        pokeOperator = game.pokeOperator;
        deckOperator = game.deckOperator;
    }

    //给玩家或电脑发地主的8张牌
    public void startDealLandlordCard() {
        //给逻辑地主发牌
        game.peopleOperator.dealCardToLandlord(game.peoples, game.cardpile, game.whosLand);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //给地主8张卡
                GiveLandCard();
            }
        }, Constants.WAIT_FOR_COUNT);

        //将底牌进行显示，然后玩家或AI出牌
        turnOverDownPoke(7);
    }

    //给玩家或电脑发地主的8张牌(地主牌向上提再向下移)
    private void GiveLandCard() {
        //将地主的八张牌向上提
        final List<Integer> eightCard = new ArrayList<Integer>();
        //计算阴影值
        int []shaderNum = new int[33];
        if (game.isBrightCard == false) {
            int curShader = 1;
            int len = game.peoples[game.whosLand].deck.size() - 1 < Constants.excursion_num
                    ? game.peoples[game.whosLand].deck.size() - 1 : Constants.excursion_num;
            for (int i = len; i >= 0; i--) {
                shaderNum[i] = curShader++;
            }
            for (int i = game.peoples[game.whosLand].deck.size() - 1; i > Constants.excursion_num; i--) {
                shaderNum[i] = curShader++;
            }
        }

        //移动更新四家卡组
        deckOperator.movePeopleDeck(game.whosLand);

        //显示为地主发8张牌
        for (int i = 0; i < game.peoples[game.whosLand].deck.size(); i++) {
            //获取当前的牌
            int num = game.peoples[game.whosLand].deck.get(i).cardIndex;
            //让它显示在最上方
            game.landlord_layout.removeView(game.poke[num]);
            game.landlord_layout.addView(game.poke[num]);
            //如果是下家或是其余三家是明牌模式则设置卡显示
            if (game.whosLand == 0 || game.isBrightCard == true) {
                game.poke[num].setCardElevation(DensityUtil.dip2px(game.getApplicationContext(), Constants.ElEVATION_DP) + i);
            } else {
                game.poke[num].setCardElevation(DensityUtil.dip2px(game.getApplicationContext(), Constants.ElEVATION_DP) + shaderNum[i]);
            }

            //找到peoplelist[whosLand]中加底牌的位置，将底牌上移
            boolean isLandlordCard = false;
            for (int j = 0; j < game.cardpile.size(); j++) {
                if (game.cardpile.get(j).cardIndex == game.peoples[game.whosLand].deck.get(i).cardIndex) {
                    if (game.whosLand == 0 || game.isBrightCard == true) {
                        pokeOperator.setPokePicture(num, false, false);
                    }
                    else {
                        pokeOperator.setPokePicture(num, true, false);
                    }
                    isLandlordCard = true;
                    break;
                }
            }

            //如果当前牌是地主牌，则地主牌下降
            if (isLandlordCard == true) {
                eightCard.add(i);
                game.poke[num].setLayoutParams(game.temptPoke[num].getLayoutParams());
                CardAnimator.verticalRun(game.poke[num],
                        -DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_AADD_BOTTOM), 0
                        , 1500);    //地主牌下降
            }
            else {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) game.poke[num].getLayoutParams();
                if (layoutParams.bottomMargin == DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_ADD_BOTTOM)) {
                    pokeOperator.pokeUpDown(game.poke[num], false);   //将下家在上面的牌放下来
                }
            }

        }

    }

    //判断是不是断线重连模式，如果是断线重连模式，翻转完底牌后，不自动更新游戏状态
    public boolean isReconnected = false;

    //底牌翻转的动画
    public void turnOverDownPoke(final int i) {
        //end1从多少角度开始翻转
        final float start = 0f;
        final float end1 = (180.0F) / Constants.beishu;
        final float end2 = 180.0F;

        //翻转总共所需的时间
        final int duration = (int)Constants.UNDERPOKE_DURATION_TIME;

        //对这张牌进行翻转
        CardAnimator.rotationYRun(game.downpoke[i], (long)(duration / Constants.beishu), start, end1);

        //当翻转到一半时设置当前牌的状态
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //将翻转的牌从背面转换为正面
                pokeOperator.setConversePokePicture(i, true);
                //设置阴影值
                game.downpoke[i].setElevation(game.downpoke[i].getElevation() + 2 * i);
            }
        },duration / 2);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //进行第二部分翻转
                CardAnimator.rotationYRun(game.downpoke[i], (long)(duration / Constants.beishu * (Constants.beishu - 1)), end1, end2);

                if (i > 0) {
                    //开始下一张牌的翻转
                    turnOverDownPoke(i - 1);
                }
                else {
                    //全部翻转完后，将阴影相同，刷牌
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < 8; i++) {
                                //更新底牌的显示顺序
                                game.landlord_layout.removeView(game.downpoke[i]);
                                game.landlord_layout.addView(game.downpoke[i]);
                                //设置底牌的阴影
                                game.downpoke[i].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP));
                                //初始化底牌的y轴
                                game.downpoke[i].setRotationY(0.0f);
                                pokeOperator.setPokePicture(i, false, true);
                            }
                        }
                    }, (long)(Constants.UNDERPOKE_DURATION_TIME * (Constants.beishu - 1) / Constants.beishu) + Constants.WAIT_FOR_COUNT);

                    //底牌翻转完后，为出牌前做准备(如果不是断线重连模式)
                    if (isReconnected == false) {
                        game.process.gameProcessChange();
                    } else {
                        isReconnected = false;
                    }
                }
            }
        }, (long) (duration / Constants.beishu));
    }

}
