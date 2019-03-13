package wanganxin.com.poker.GameLogic.GameProcess;

import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.ReconnectDeal;
import wanganxin.com.poker.GameActivity.GameStartActivity;
import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.GameProcess.CallScore.CallScoreProcess;
import wanganxin.com.poker.GameLogic.GameProcess.CallScore.NetCallScore;
import wanganxin.com.poker.GameLogic.GameProcess.CallScore.SingleCallScore;
import wanganxin.com.poker.GameLogic.GameProcess.OutCard.NetOutCard;
import wanganxin.com.poker.GameLogic.GameProcess.OutCard.OutCardProcess;
import wanganxin.com.poker.GameLogic.GameProcess.OutCard.SingleOutCard;
import wanganxin.com.poker.GameLogic.GameProcess.StartGame.NetStartGame;
import wanganxin.com.poker.GameLogic.GameProcess.StartGame.SingleStartGame;
import wanganxin.com.poker.GameLogic.GameProcess.StartGame.StartGameProcess;

public class GameProcess {

    private LandlordActivity game;

    public StartGameProcess startGameProcess;  //准备阶段的流程
    public DealCardProcess dealCardProcess;        //发牌阶段的流程
    public CallScoreProcess callScoreProcess;//叫地主流程函数
    public DealLandlordCardProcess dealLandlordCardProcess;  //发地主牌阶段的流程
    public OutCardProcess outCardProcess;      //出牌阶段的流程
    public ScoreSettleProcess scoreSettleProcess;      //结算积分的流程
    public EndGameProcess endGameProcess;      //结束游戏的流程

    public GameStartActivity socketClient;      //获取服务器获取
    private boolean isNetWork;              //是否为网络模式
    private boolean isCallScoreRestart;     //判断是不是因为叫分都是不叫而重新开始的游戏
    public int reCallScore;    //设置叫分阶段重开了多少次

    //初始化需要操作的卡牌
    public GameProcess(boolean isNetWork, LandlordActivity game) {
        //获取当前游戏单例
        this.game = game;
        //设置是否为网络模式
        this.isNetWork = isNetWork;
        //当前不是由于叫分而重新开始
        isCallScoreRestart = false;
        //设置叫分阶段重开了多少次
        reCallScore = 0;

        //如果是联网模式
        if (isNetWork == true) {
            socketClient = GameStartActivity.getInstance();
            startGameProcess = new NetStartGame(game);
            callScoreProcess = new NetCallScore(game);
            outCardProcess = new NetOutCard(game);
        }
        //如果是单机模式
        else {
            startGameProcess = new SingleStartGame(game);
            callScoreProcess = new SingleCallScore(game);
            outCardProcess = new SingleOutCard(game);
        }

        //初始流程的类
        dealCardProcess = new DealCardProcess(game);
        dealLandlordCardProcess = new DealLandlordCardProcess(game);
        scoreSettleProcess = new ScoreSettleProcess(game);
        endGameProcess = new EndGameProcess(game);
    }

    //当前状态的步骤
    public GameProcessEnum step = GameProcessEnum.PREPARE_GAME_PROCESS;
    //下一阶段的状态步骤
    public GameProcessEnum next_step = GameProcessEnum.NONE;


    //游戏进程状态转移(网络模式开始是从CallScoreDeal获取第一个叫分的人开始的)
    public void gameProcessChange() {
        switch (step) {

            //游戏准备开始阶段
            case PREPARE_GAME_PROCESS: {
                next_step = GameProcessEnum.DEAL_CARD_PROCESS;
            }
            break;

            //出牌阶段的流程（发完牌自动跳转）
            case DEAL_CARD_PROCESS: {
                //如果处于断线重连出牌阶段的模式，直接跳到出牌阶段
                if (game.reconMode == ReconnectDeal.reconOutCardMode) {
                    next_step = GameProcessEnum.OUT_CARD_PROCESS;
                } else {
                    next_step = GameProcessEnum.CALL_SCORE_PROCESS;
                }
            }
            break;

            //发牌阶段流程
            case CALL_SCORE_PROCESS: {
                //如果没人叫地主，则游戏重新开始，否则进入发地主卡牌的阶段
                if (game.whosLand == -1) {
                    reCallScore++;
                    isCallScoreRestart = true;
                    next_step = GameProcessEnum.End_GAME_PROCESS;
                } else {
                    next_step = GameProcessEnum.DEAL_LANDLORD_CARD_PROCESS;
                }
            }
            break;

            //发地主牌的流程
            case DEAL_LANDLORD_CARD_PROCESS: {
                next_step = GameProcessEnum.OUT_CARD_PROCESS;
            }
            break;

            //出牌阶段的流程
            case OUT_CARD_PROCESS: {
                next_step = GameProcessEnum.SCORE_SETTLE_PROCESS;
            }
            break;

            //积分结算阶段的流程
            case SCORE_SETTLE_PROCESS: {
                next_step = GameProcessEnum.End_GAME_PROCESS;
            }
            break;

            //游戏结束流程
            case End_GAME_PROCESS: {
                next_step = GameProcessEnum.PREPARE_GAME_PROCESS;
            }
            break;
        }

        //开始初始化状态的转移
        initGameProcessUpdate();
    }

    //游戏进程转换前的初始化
    private void initGameProcessUpdate() {
        //跳转到下一个阶段前的初始化
        if (next_step == GameProcessEnum.NONE) {
            return ;
        }

        switch (next_step) {

            //发牌前进行初始化
            case DEAL_CARD_PROCESS: {
                dealCardProcess.initDealCardAnimator();
            }
            break;

            //如果处于断线重连阶段，叫分和出牌阶段要进行初始化
            case CALL_SCORE_PROCESS: {

            }
            break;

            //对发地主牌进行初始化
            case DEAL_LANDLORD_CARD_PROCESS: {
                //如果有人叫分
                //if (game.whosLand != -1) {
                    //保存当前的积分
                    game.multiple = game.integration[0];
                    //更新当前的底分和倍数
                    game.countBottomscoreMultiple();
                //}
            }
            break;
        }

        step = next_step;

        //进行下一步
        gameProcessUpdate();
    }

    //游戏进程的状态转换
    private void gameProcessUpdate() {
        switch (step) {

            //出牌阶段的流程
            case DEAL_CARD_PROCESS: {
                dealCardProcess.startDealCard();
            }
            break;

            //发牌阶段流程
            case CALL_SCORE_PROCESS: {
                callScoreProcess.startCallScore();
            }
            break;

            //发地主牌的流程
            case DEAL_LANDLORD_CARD_PROCESS: {
                dealLandlordCardProcess.startDealLandlordCard();
            }
            break;

            //出牌阶段的流程
            case OUT_CARD_PROCESS: {
                outCardProcess.startOutCard();
            }
            break;

            //积分结算阶段的流程
            case SCORE_SETTLE_PROCESS: {
                scoreSettleProcess.startSettleScore();
            }
            break;

            //游戏结束的流程
            case End_GAME_PROCESS: {
                //将当前状态置为false
                boolean curIsCallScoreRestart = isCallScoreRestart;
                isCallScoreRestart = false;
                endGameProcess.startEndGame(curIsCallScoreRestart);
            }
            break;
        }
    }
}
