package wanganxin.com.poker.GameLogic.GameProcess;

import android.os.Handler;
import android.view.View;

import java.util.Random;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.PokeEffect.DeckOperator;
import wanganxin.com.poker.GameAnimation.PokeEffect.PokeOperator;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.entity.PeopleActionEnum;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

//一轮结束后结算积分
public class ScoreSettleProcess  {

    //获取当前进行的游戏
    private LandlordActivity game;

    //对扑克操作
    private DeckOperator deckOperator;
    //设置卡牌图片
    private PokeOperator stp;

    //初始化随机种子
    private Random ran;
    //最终得分
    private int[] lastScore = new int[4];

    public ScoreSettleProcess(LandlordActivity game) {
        this.game = game;
        ran = new Random();

        stp = game.pokeOperator;
        deckOperator = game.deckOperator;
    }

    //获胜结算，显示结束计分板
    public void startSettleScore() {
        //计算最终计分
        countFourPeopleScore();

        //播放胜利或者失败的音效
        game.soundEffect.Player_EndGame_Voice(lastScore[0] > 0 ? true : false);

        //设置电脑说的话
        String[] sad = new String[]{ "( ≧Д≦)", "o(╥﹏╥)o", "o(〒﹏〒)o" };
        String[] happy = new String[] { "<(￣︶￣)>", "~(￣▽￣)~", "≧▽≦" };
        for (int i = 1; i < 4; i++) {
            //播放获胜或失败时说的话
            if (lastScore[i] > 0) {
                game.show_fourpeoplechat(i, "哈哈还是我最厉害" +"\r\n"+ happy[ran.nextInt(3)]);
            }
            else {
                game.show_fourpeoplechat(i, "呜呜呜" + "\r\n" + sad[ran.nextInt(3)]);
            }
        }

        //移除显示的四个图标
        for (int i = 0; i < 4; i++) {
            game.peoples[i].actionAlphaGoneRun();
        }

        //调用结束的积分界面
        UpdateEnd_Score_panel();

        //如果不是明牌模式将其余三家剩余的牌翻转
        if (game.isBrightCard == false) {
            for (int i = 1; i < 4; i++) {
                //有剩余牌
                if (game.peoples[i].deck.size() > 0) {
                    showAnimator_OverTurnThreeBright(i, game.peoples[i].deck.size() - 1);
                }
            }
        }

//        //如果在自动测试状态，直接自动开始（测试使用）
//        if (Constants.IsAutoTest == true) {
//            start_new_turn_click();
//            game.start_button_Click();
//        }
    }

    //更新和显示结束的积分界面
    private void UpdateEnd_Score_panel() {
        //显示结束时的积分榜
        endPanelScoreVisibility(true);
        //将最终的积分榜淡入
        CardAnimator.alphaRun(game.end_score_panel, 500);

        //设置积分界面
        //  1.第一列为用户姓名
        //  2.第二列为胜利或是失败的图标
        //  3.第三列为当前局所获得的积分
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 4; i++) {
                if (j == 0) {
                    //设置用户姓名
                    game.end_panel_score[i][j].setText(game.peoples[i].name);
                } else if (j == 1) {
                    //判断胜利还是失败的图标
                    if (lastScore[i] > 0) {
                        game.end_panel_score[i][j].setBackground(game.getResources().getDrawable(R.mipmap.win, game.getTheme()));
                    } else {
                        game.end_panel_score[i][j].setBackground(game.getResources().getDrawable(R.mipmap.lose, game.getTheme()));
                    }
                } else {
                    //赋给对应获得的积分
                    game.end_panel_score[i][j].setText(Integer.toString(lastScore[i]));
                }
            }
        }
    }

    //计算四个人最终得分，并将得分存放到lastScore数组里面
    private void countFourPeopleScore() {
        int[] OrignIntegration = new int[4];

        //保存上一局四人原本的积分
        for (int i = 0; i < 4; i++) {
            OrignIntegration[i] = game.peoples[i].integration;
        }

        //结算上一局最终的积分
        game.peopleOperator.settleScore(game.peoples, game.integration[0]);

        //将最终积分保存到lastScore中
        for (int i = 0; i < 4; i++) {
            lastScore[i] = game.peoples[i].integration - OrignIntegration[i];
        }
    }

    //结束积分“差”按钮的事件，删除结束的积分界面，初始化各种变量，显示开始按钮
    public void start_new_turn_click() {
        //设置积分结算界面的淡出效果
        CardAnimator.alphaGoneRun(game.end_score_panel, Constants.LIGHT_DURATION_TIME, game.landlord_layout);

        //对积分榜进行更新
        updateScorePanel();

        //隐藏地主头像（未选地主更新会出错）
        CardAnimator.alphaGoneRun(game.fourLandPicbtn[game.whosLand], Constants.LIGHT_DURATION_TIME);

        //显示开始按钮
        game.start_button.setVisibility(View.VISIBLE);
        //显示开始按钮
        CardAnimator.alphaRun(game.start_button, Constants.LIGHT_DURATION_TIME);

        //如果是网络状态将其余家的托管状态取消
        if (game.isNetWork) {
            for (int i = 1; i < 4; i++) {
                if (game.peoples[i].isRobot) {
                    //如果其他玩家为机器人，初始化开始图片
                    game.peoples[i].setAction(PeopleActionEnum.PREAPARE);
                } else {
                    //如果是玩家，取消托管信息
                    CardAnimator.alphaGoneRun(game.robots[i], Constants.LIGHT_DURATION_TIME);
                }
            }
        }
        //如果是单机状态，其他三家自动准备
        else {
            //初始化四个人的开始图片
            for (int i = 1; i < 4; i++) {
                //添加score图片（叫分和开始的图片）（淡入）
                game.peoples[i].setAction(PeopleActionEnum.PREAPARE);
            }
        }

        //积分结算结束，进入状态转移
        game.process.gameProcessChange();
    }

    //对最后积分表加入和删除
    public void endPanelScoreVisibility(boolean b) {
        if (b == true) {
            //显示积分版上的表格
            game.landlord_layout.addView(game.end_score_panel);
        }
        else {
            //隐藏积分版上的表格
            game.landlord_layout.removeView(game.end_score_panel);
        }
    }

    //更新积分榜
    public void updateScorePanel() {
        for (int i = 0; i < 4; i++) {
            //更新姓名
            game.score_panel_score[i][0].setText(game.peoples[i].name);
            //更新上局积分
            game.score_panel_score[i][1].setText(Integer.toString(lastScore[i]));
            //更新当前总积分
            game.score_panel_score[i][2].setText(Integer.toString(game.peoples[i].integration));
        }
    }

    //将其余三家翻转变明
    //setElevation(i * 2 + game.poke[cardIndex].getElevation())是为了在翻转过程中能保持相对高度不变
    public void showAnimator_OverTurnThreeBright(final int who, final int i) {
        final float start = 0f;
        final float end1 = (180.0F) / Constants.beishu;
        final float end2 = 180.0F;
        if (i >= 0 && i < game.peoples[who].deck.size()) {
            final int num = game.peoples[who].deck.get(i).cardIndex;

            //设置翻转时间
            final int duration = (int)(Constants.UNDERPOKE_DURATION_TIME);
            CardAnimator.rotationYRun(game.poke[num], (long)(duration / Constants.beishu), start, end1);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //再从背面转换为正面
                    stp.setConversePokePicture(num, false);
                    game.poke[num].setElevation(i * 2 + game.poke[num].getElevation());
                }
            }, duration / 2 + Constants.WAIT_FOR_COUNT);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CardAnimator.rotationYRun(game.poke[num], (long)(duration * (Constants.beishu - 1) / Constants.beishu), end1, end2);
                    if (i > 0) {
                        //从右至左
                        showAnimator_OverTurnThreeBright(who, i - 1);
                    } else if (i == 0) {
                        //全部翻转完后，将阴影相同，刷牌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < game.peoples[who].deck.size(); i++) {
                                    int num = game.peoples[who].deck.get(i).cardIndex;
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
                            if (i >= 0 && i < game.peoples[who].deck.size()) {
                                stp.setPokePicture(game.peoples[who].deck.get(i).cardIndex, false, false);
                            }
                        }
                    }, (long)(duration * (Constants.beishu - 1) / Constants.beishu) + Constants.WAIT_FOR_COUNT);
                }
            }, (long)(duration / Constants.beishu));
        }
    }

    //将其余三家翻转变暗
    public void showAnimator_OverTurnThreeDark(final int who, final int i) {
        final float start;
        final float end1;
        final float end2;
        if (i >= 0 && i < game.peoples[who].deck.size()) {
            final int num = game.peoples[who].deck.get(i).cardIndex;
            start = 180.0F;
            end1 = start / Constants.beishu * (Constants.beishu - 1);
            end2 = 0f;
            final int duration = (int)(Constants.UNDERPOKE_DURATION_TIME);//设置翻转时间
            CardAnimator.rotationYRun(game.poke[num], (long)(duration / Constants.beishu), start, end1);
            if (i >= Constants.excursion_num + 1) {
                game.poke[num].setElevation((game.peoples[who].deck.size() - 3 - i) + 25 + DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP));
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stp.setPokePicture(num, true, false); //再从背面转换为正面
                    if (i < Constants.excursion_num + 1) {   //在0~17，18张牌的时候换行
                        game.poke[num].setElevation((game.peoples[who].deck.size() - 3 - i) + DensityUtil.dip2px(game.getApplication(), Constants.ElEVATION_DP));
                    }
                }
            },duration / 2 + Constants.WAIT_FOR_COUNT);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CardAnimator.rotationYRun(game.poke[num], (long)(duration * (Constants.beishu - 1) / Constants.beishu), end1, end2);
                    if (i < game.peoples[who].deck.size() - 1) {
                        //从左至右
                        showAnimator_OverTurnThreeDark(who, i + 1);
                    }
                    else if (i == game.peoples[who].deck.size() - 1) {
                        //全部翻转完后，将阴影相同，刷牌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deckOperator.freshOthersDeck(who);
                            }
                        }, (long)(duration * (Constants.beishu - 1) / Constants.beishu));
                    }
                }
            }, (long) (duration / Constants.beishu));
        }
    }

}
