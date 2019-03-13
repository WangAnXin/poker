package wanganxin.com.poker.GameLogic.GameProcess.CallScore;

import android.os.Handler;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameLogic.entity.PeopleActionEnum;
import wanganxin.com.poker.GameLogic.utilities.Constants;

//叫分确定地主
public abstract class CallScoreProcess {
    //获取当前进行的游戏
    protected LandlordActivity game;

    //起始位子
    public int begin_pos;
    //终止位子
    public int end_pos;

    public CallScoreProcess(LandlordActivity game) {
        this.game = game;
    }

    //开始叫分（叫分前的初始化）
    //（当前玩家叫前，为第一轮叫分，当前玩家叫后，为第二轮叫分）
    public abstract void startCallScore();

    //显示叫分的结果
    protected void showCallResult() {
        //当前叫分人数加一
        this.begin_pos++;

        //如果已经四人结束或是已经有了地主则叫分完成
        if (begin_pos > end_pos || game.integration[0] >= 3) {
            //调用结束按钮
            endCallScore();
        }
        //如果轮到玩家出牌且不处于测试状态
        else if (begin_pos % 4 == 0 && Constants.IsAutoTest == false) {
            //当前玩家叫分过程
            playerCallScore();
        }//如果是电脑，随机叫分
        else {
            //其余玩家叫分过程
            othersCallScore(begin_pos % 4);
        }
    }

    //当前玩家叫分过程
    protected void playerCallScore() {
        //(如果当前分数大于1分，则将1分显示为灰色，不让再叫1分）
        if (game.integration[0] >= 1) {
            game.callScoreFourbtn[0].setBackground(game.getDrawable(R.mipmap.onescore_button_gray));
            game.callScoreFourbtn[0].setEnabled(false);
        }

        //(如果当前分数大于2分，则将2分显示为灰色，不让再叫2分）
        if (game.integration[0] >= 2) {
            game.callScoreFourbtn[1].setBackground(game.getDrawable(R.mipmap.twoscore_button_gray));
            game.callScoreFourbtn[1].setEnabled(false);
        }

        //显示叫分按钮
        game.callScoreFourbtnVisibility(true);

        //自动叫分
        playerAutoCallScore();
    }
    //单机版一直等待玩家叫分，网络版过15s不叫分，自动认为不叫上传到服务器
    protected abstract void playerAutoCallScore();

    //当前玩家点击叫分的四按钮操作(0~3)0分为不叫
    public void score_button_Click(int score) {
        if (score == 0) {
            setPeopleAction(score, 0);
        }
        else if (score > game.integration[0]) {
            setPeopleAction(score, 0);

            //设置当前最高分
            game.integration[0] = score;

            //将当前玩家设置为地主
            game.whosLand = 0;
        }

        //如果第二轮叫分，先隐藏叫分四按钮
        game.callScoreFourbtnVisibility(false);

        //玩家叫分的下一步动作
        playerScoreMeth(score);
    }
    //玩家叫地主下一步的动作（单机，网络）
    protected abstract void playerScoreMeth(int score);

    //其余玩家叫分过程（单机，网络）
    protected abstract void othersCallScore(int who);
    //玩家叫地主下一步的动作
    protected void otherPlayerCallScoreDisplay(int curScore, int who) {
        //根据叫分显示图片
        setPeopleAction(curScore, who);

        //如果为4分以上，则为不叫
        if (curScore >= 4) {
            //当前叫分为0分
            curScore = 0;
        }

        //如果牌比当前大，则目前他是地主
        if (curScore > game.integration[0]) {
            game.whosLand = who;
            game.integration[0] = curScore;
        }
    }

    //根据叫分显示图片
    //设置显示所叫分数的图片（一分、两分、三分）（淡入）
    private void setPeopleAction(int score, int who) {
        switch (score) {
            case 1:
                game.peoples[who].setAction(PeopleActionEnum.ONE_SCORE);
                break;
            case 2:
                game.peoples[who].setAction(PeopleActionEnum.TWO_SCORE);
                break;
            case 3:
                game.peoples[who].setAction(PeopleActionEnum.THREE_SCORE);
                break;
            default:
                game.peoples[who].setAction(PeopleActionEnum.NO_SCORE);
        }
    }

    //叫分结束,移除四张图片，叫分四按钮隐藏，开始游戏
    protected void endCallScore() {
        //将玩家的叫分按钮隐藏
        game.callScoreFourbtnVisibility(false);

        //等待其显示完隐藏
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //先移除四人的动作图片（所叫的分）
                for (int i = 0; i < 4; i++) {
                    game.peoples[i].actionAlphaGoneRun();
                }
            }
        }, (long) Constants.COMPUTER_THINK_TIME);

        //单机，网络是否进入下一环节
        //单机自己调用
        //网络由clientReceiveDeal调用，避免进程顺序不一致产生错误(发牌与接收消息的不一致)
        if (game.isNetWork == false
                || game.process.reCallScore < Constants.MAX_RE_CALLSCROE_NUM - 1
                || game.whosLand != -1) {
            game.process.gameProcessChange();
        } else if (game.process.reCallScore == Constants.MAX_RE_CALLSCROE_NUM - 1) {
            game.process.reCallScore = 0;
        }
    }
    //单机，网络是否进入下一环节
    //protected abstract void gameProcessChange();

}
