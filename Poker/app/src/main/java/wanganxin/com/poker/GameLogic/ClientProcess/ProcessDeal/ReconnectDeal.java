package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import com.google.gson.JsonArray;

import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveEnum;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientSendEnum;
import wanganxin.com.poker.GameActivity.GameStartActivity;
import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.utilities.Constants;


//处理断线重连的类
public class ReconnectDeal {
    static final int playerInfoIndex = 1;
    //playerInfoIndex会读取1个playerIndex和4个player信息和4个player的ready信息
    static final int cardInfoIndex = playerInfoIndex + 9;
    //cardInfoIndex会读取4个卡组信息和1个底牌信息
    public static int extraIndex = cardInfoIndex + 5;

    //是否处于断线重连状态，-1不是，1是重连叫分阶段，2是重连出牌阶段
    private static int reconMode = -1;
    public static final int reconCallScoreMode = 1;
    public static final int reconOutCardMode = 2;

    //获取时钟最后剩余的时间（Clock类再获取后会终止重连，将reconMode置为-1）
    public static int clockTime;

    //存储断线重连中JsonArray的值
    public static JsonArray reconJsonArray;

    //进行断线重连的第一步，获取状态
    public static boolean firstDealReconnected(JsonArray reconArray) {
        //如果存在gameProcess这个项则是断线重连
        if (reconArray.get(0).getAsJsonObject().has("gameProcess") == false) {
            return false;
        }

        //设置gameStartActivity的断线重连模式，保证在game创建之后接受消息
        GameStartActivity.getInstance().isReconnected = true;
        //获取当前应返回的状态
        reconMode = reconArray.get(0).getAsJsonObject().get("gameProcess").getAsInt();
        //将可以用来恢复的array赋给全局，供其他地方使用
        reconJsonArray = reconArray;

        //最快的速度进行状态转移，接受数据
        if (reconMode == reconCallScoreMode) {
            GameStartActivity.getInstance().sendDeal.nextStep = ClientSendEnum.CALL_SCORE_PROCESS;
            GameStartActivity.getInstance().receiveDeal.nextStep = ClientReceiveEnum.CALL_SCORE_RECEIVE_PROCESS;
        } else if (reconMode == reconOutCardMode) {
            GameStartActivity.getInstance().sendDeal.nextStep = ClientSendEnum.OUT_CARD_PROCESS;
            GameStartActivity.getInstance().receiveDeal.nextStep = ClientReceiveEnum.OUT_CARD_RECEIVE_PROCESS;
        }
        GameStartActivity.getInstance().sendDeal.initProcessSendUpdate();
        GameStartActivity.getInstance().receiveDeal.initProcessReceiveUpdate();

        //启动游戏
        GameStartActivity.getInstance().handler.obtainMessage(GameStartActivity.START_GAME, reconMode).sendToTarget();

        return true;
    }

    //读取其他玩家的信息
    public static void getMatchInfo() {
        //从jsonArray指定的index获取玩家当前的位置信息和其余玩家的信息
        MatchDeal.getPlayerIndexInfo(reconJsonArray, playerInfoIndex);
    }

    //读取卡组的信息
    public static void getCardInfo(LandlordActivity game) {
        //获取四个人的卡组信息和底牌信息
        CardReceiveDeal.getCardInfo(reconJsonArray, cardInfoIndex, game);
        //移除开始按钮（淡出）
        CardAnimator.alphaGoneRun(game.start_button, Constants.LIGHT_DURATION_TIME);
    }
}
