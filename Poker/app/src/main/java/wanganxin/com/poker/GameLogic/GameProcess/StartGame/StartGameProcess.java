package wanganxin.com.poker.GameLogic.GameProcess.StartGame;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.utilities.Constants;

//玩家准备游戏过程
public abstract class StartGameProcess {
    //获取当前进行的游戏
    protected LandlordActivity game;

    public StartGameProcess(LandlordActivity game) {
        this.game = game;
    }

    //初始化游戏
    public abstract void initGame();

    //点击开始，对游戏做准备，开始游戏
    public void startPrepareGame() {
        //移除开始按钮（淡出）
        CardAnimator.alphaGoneRun(game.start_button, Constants.LIGHT_DURATION_TIME);
        //调用每个子类不同的方法
        prepareGameMeth();
    }

    public abstract void prepareGameMeth();

}