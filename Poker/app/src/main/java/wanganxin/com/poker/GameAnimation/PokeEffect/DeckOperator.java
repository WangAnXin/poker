package wanganxin.com.poker.GameAnimation.PokeEffect;

import android.os.Handler;
import android.widget.RelativeLayout;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameAnimation.GUI.CardPosition;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

public class DeckOperator {

    private LandlordActivity game;

    //初始化
    public DeckOperator(LandlordActivity game) {
        this.game = game;
    }

    //刷新玩家卡组的阴影
    public void freshPlayerDeck() {
        for (int i = 0; i < game.peoples[0].deck.size(); i++) {
            int num = game.peoples[0].deck.get(i).cardIndex;
            game.landlord_layout.removeView(game.poke[num]);
            game.landlord_layout.addView(game.poke[num]);
            //设置叠加顺序
            game.poke[num].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP));
        }
    }

    //刷新其余三家卡组的阴影
    public void freshOthersDeck(int who){
        int num;
        int shaderNum = 1;
        int len = game.peoples[who].deck.size() - 1 < Constants.excursion_num
                ? game.peoples[who].deck.size() - 1 : Constants.excursion_num;

        //对于其余三家的卡牌，如果分为两层，先放下层的卡牌
        for (int i = len; i >= 0; i--) {
            num = game.peoples[who].deck.get(i).cardIndex;
            game.landlord_layout.removeView(game.poke[num]);
            game.landlord_layout.addView(game.poke[num]);
            //设置叠加顺序
            game.poke[num].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP) + shaderNum++);
        }

        for (int i = game.peoples[who].deck.size() - 1; i > Constants.excursion_num; i--) {
            num = game.peoples[who].deck.get(i).cardIndex;
            game.landlord_layout.removeView(game.poke[num]);
            game.landlord_layout.addView(game.poke[num]);
            //设置叠加顺序
            game.poke[num].setElevation(DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP) + shaderNum++);
        }
    }

    //水平或垂直移动界面的卡组(重新显示卡组)
    public void movePeopleDeck(final int who) {
        final int []pokeShader = new int[33];
        if (who == 0) {

            //计算卡组当前所在的位置保存到tempPoke中
            countPlayerCardPosition();

        }
        else {
            //计算其余三家显示位置
            countOthersCardPosition(who);

            //计算其余三家卡牌的阴影
            if (game.isBrightCard == false) {
                int curShader = 1;
                int len = game.peoples[who].deck.size() - 1 < Constants.excursion_num
                        ? game.peoples[who].deck.size() - 1 : Constants.excursion_num;
                for (int i = len; i >= 0; i--) {
                    pokeShader[i] = curShader++;
                }
                for (int i = game.peoples[who].deck.size() - 1; i > Constants.excursion_num; i--) {
                    pokeShader[i] = curShader++;
                }
            }
        }

        //对卡组进行动画渲染
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //对当前玩家的每一张牌进行移动
                for (int i = 0; i < game.peoples[who].deck.size(); i++) {
                    //获取当前卡牌的标号
                    final int num = game.peoples[who].deck.get(i).cardIndex;

                    //刷新显示顺序
                    game.landlord_layout.removeView(game.poke[num]);
                    game.landlord_layout.addView(game.poke[num]);

                    //设置卡牌的叠加顺序
                    if (game.isBrightCard == true || who == 0) {
                        game.poke[num].setCardElevation
                                (DensityUtil.dip2px(game.getApplicationContext(), Constants.ElEVATION_DP) + i);
                    } else {
                        game.poke[num].setCardElevation
                                (DensityUtil.dip2px(game.getApplicationContext(), Constants.ElEVATION_DP) + pokeShader[i]);
                    }

                    //下家和左家为向左移动
                    if (who == 0 || who == 3) {
                        //将牌水平移动（向左）
                        CardAnimator.horizentalRun(game.poke[num],
                                0, game.temptPoke[num].getLeft() - game.poke[num].getLeft()
                                ,Constants.POKE_HORIZENTAL_DURATION, true);
                    }
                    else {    //上家和右家为向右移动
                        //将牌水平移动（向右）
                        CardAnimator.horizentalRun(game.poke[num],
                                0, game.poke[num].getRight() - game.temptPoke[num].getRight()
                                ,Constants.POKE_HORIZENTAL_DURATION, false);
                    }

                    //如果不是下家，有可能是垂直移动（因为有可能为两层）
                    if (who != 0) {
                        if (who == 2) {
                            //将牌垂直移动
                            CardAnimator.verticalOutRunTopMargin(game.poke[num],
                                    0, game.poke[num].getTop() - game.temptPoke[num].getTop()
                                    ,Constants.POKE_HORIZENTAL_DURATION);
                        }
                        else {
                            //将牌垂直移动
                            CardAnimator.verticalOutRun(game.poke[num],
                                    0, game.poke[num].getTop() - game.temptPoke[num].getTop()
                                    ,Constants.POKE_HORIZENTAL_DURATION);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                game.poke[num].setLayoutParams(game.temptPoke[num].getLayoutParams());
                            }
                        }, Constants.POKE_HORIZENTAL_DURATION);
                    }


                    if (i == game.peoples[who].deck.size() - 1 && who == 0) {
                        freshPlayerDeck();
                    }
                }

                //如果是左家或右家是地主盖住了，要重新刷出来
                if (who == 1 && game.whosLand == 1 || who == 3 && game.whosLand == 3) {
                    game.landlord_layout.removeView(game.fourLandPicbtn[game.whosLand]);
                    game.landlord_layout.addView(game.fourLandPicbtn[game.whosLand]);
                }
            }
        }, Constants.WAIT_FOR_COUNT);
    }

    //计算下家卡牌应该存在的位置
    private void countPlayerCardPosition() {
        //用temptPoke计算该有的位置
        RelativeLayout.LayoutParams layoutParams;

        int pos = CardPosition.getDownPokePosition(game.getApplicationContext(), game.d, game.peoples[0].deck.size(), true);

        for (int i = 0; i < game.peoples[0].deck.size(); i++) {
            int num = game.peoples[0].deck.get(i).cardIndex;
            layoutParams = CardPosition.getDownPokeLayoutParams(game.getApplicationContext());
            layoutParams.leftMargin = pos;
            game.landlord_layout.removeView(game.temptPoke[num]);
            game.landlord_layout.addView(game.temptPoke[num], layoutParams);
            pos += game.card_interval;
        }
    }

    //计算其余三家卡牌应该存在的位置
    private void countOthersCardPosition(int who) {
        RelativeLayout.LayoutParams layoutParams;
        int num;
        int len = game.peoples[who].deck.size() - 1 < Constants.excursion_num
                ? game.peoples[who].deck.size() - 1 : Constants.excursion_num;

        int position = CardPosition.getTurnOverPokePosition(game.getApplicationContext(), who, game.d, len + 1);

        for (int i = len; i >= 0; i--) {
            layoutParams = CardPosition.getTurnOverPokeLayoutParams(game.getApplicationContext(), who);
            if (len == game.peoples[who].deck.size() - 1) {
                if (who == 2) {
                    layoutParams.topMargin = 0;
                }
            }
            else {
                if (who == 2) {
                    layoutParams.topMargin = 0;
                }
                else {
                    layoutParams.bottomMargin = 0;
                }
            }
            if (who == 3) {//如果是左家为靠左
                layoutParams.leftMargin = position;
                position -= game.card_small_interval;
            }
            else {//上家和右家为靠右
                layoutParams.rightMargin = position;
                position += game.card_small_interval;
            }
            num = game.peoples[who].deck.get(i).cardIndex;
            game.landlord_layout.removeView(game.temptPoke[num]);
            game.landlord_layout.addView(game.temptPoke[num], layoutParams);    //只用来计算temptPoke坐标
        }

        if (len == Constants.excursion_num) {
            position = CardPosition.getTurnOverPokePosition(game.getApplicationContext(), who, game.d, game.peoples[who].deck.size() - 1 - Constants.excursion_num);
            for (int ii = game.peoples[who].deck.size() - 1; ii > Constants.excursion_num; ii--) {
                layoutParams = CardPosition.getTurnOverPokeLayoutParams(game.getApplicationContext(), who);
                if (who == 3) {//如果是左家为靠左
                    layoutParams.leftMargin = position;
                    position -= game.card_small_interval;
                } else {//上家和右家为靠右
                    layoutParams.rightMargin = position;
                    position += game.card_small_interval;
                }
                num = game.peoples[who].deck.get(ii).cardIndex;
                game.landlord_layout.removeView(game.temptPoke[num]);
                game.landlord_layout.addView(game.temptPoke[num], layoutParams);    //只用来计算temptPoke坐标
            }
        }
    }

}
