package wanganxin.com.poker.GameLogic.GameProcess;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.utilities.Constants;

//一轮游戏结束做最后的处理
public class EndGameProcess {
    //获取当前进行的游戏
    private LandlordActivity game;

    public EndGameProcess(LandlordActivity game) {
        this.game = game;
    }

    public void startEndGame(boolean isCallScoreRestart) {
        //将底分删除
        game.lbl_bottomscore.setText("");
        //将倍数删除
        game.lbl_multiple.setText("");

        //如果当前处于托管状态，取消托管
        if (game.isHosting == true) {
            CardAnimator.alphaGoneRun(game.robots[0], Constants.LIGHT_DURATION_TIME);
            game.isHosting = false;
        }

        //如果当前处于明牌模式，取消明牌模式
        if (game.isBrightCard == true) {
            game.isBrightCard = false;
            game.brightCard.setBackground(game.getResources().getDrawable(R.mipmap.brightcard, game.getTheme()));
        }

        //初始化所有的变量,并移除所有的牌和底牌
        game.clearAll();

        //一分按钮回归
        game.callScoreFourbtn[0].setBackground(game.getDrawable(R.mipmap.onescore_button));
        game.callScoreFourbtn[0].setEnabled(true);

        //两分按钮回归
        game.callScoreFourbtn[1].setBackground(game.getDrawable(R.mipmap.twoscore_button));
        game.callScoreFourbtn[1].setEnabled(true);

        //下一步的状态转移
        game.process.gameProcessChange();

        //如果现在是叫分重启状态
        if (isCallScoreRestart) {
            //如果为单机模式，且当前为叫分重开状态，自动开始
            if (game.isNetWork == false) {
                game.start_button_Click();
            }
        }
    }
}
