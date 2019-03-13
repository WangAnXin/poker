package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.entity.PeopleActionEnum;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;
import wanganxin.com.poker.GameLogic.entity.Player;

//玩家动作的返回结果
enum PlayerAction {
    //玩家进入该房间
    PLAYER_ENTER(0),

    //玩家离开该房间
    PLAYER_LEAVE(1),

    //玩家准备
    PLAYER_PREPARE(2),

    NUM(3);

    //设置int和LoginResult之间的转换
    private int value;
    private static Map<Integer, PlayerAction> map = new HashMap<Integer, PlayerAction>();
    static {
        for (PlayerAction legEnum : PlayerAction.values()) {
            map.put(legEnum.value, legEnum);
        }
    }
    private PlayerAction(final int value) { this.value = value; }
    public static PlayerAction valueOf(int value) {
        return map.get(value);
    }
}

//游戏准备阶段
public class PrepareGameDeal {

    private static String startReady = "startReady";

    //点击准备
    public static String getPrepareGameMsg() {
        //发消息给服务器要登录
        JsonObject jsonObject = new JsonObject();

        //开始进入游戏大厅
        jsonObject.addProperty(startReady, true);

        //将结果返回
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }

    //发送想要设置电脑
    public static String setPlayer2Robot(int playerIndex, boolean isRobot) {
        //发送消息给服务器想要设置电脑
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("changeRobot", isRobot);
        jsonObject.addProperty("playerIndex", playerIndex);

        //将结果返回
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }

    //判断是不是游戏开始的消息
    private static boolean judgeIsGameStart(String jsonResult) {
        //如果是玩家的准备回复信息或者是开始游戏的消息
        if (new JsonParser().parse(jsonResult).isJsonObject()) {
            JsonObject jsonObject = new JsonParser().parse(jsonResult).getAsJsonObject();

            //如果是玩家的准备回复消息
            if (jsonObject.has("readySuccess") == true) {

            }
            //如果是游戏开始的消息
            else if (jsonObject.has("gameStart") == true) {
                return true;
            }
        }

        return false;
    }

    //判断是不是玩家设置电脑的消息
    private static boolean judgeIsRobotSet(String jsonResult, LandlordActivity game) {
        //Json的解析类对象,读取jsonResult
        JsonParser parser = new JsonParser();

        //如果是其他玩家的信息
        if (parser.parse(jsonResult).isJsonObject()) {
            JsonObject jsonObject = parser.parse(jsonResult).getAsJsonObject();
            //如果是设置电脑的消息
            if (jsonObject.has("changeRobot") == true) {
                int playerIndex = jsonObject.get("playerIndex").getAsInt();

                //根据消息设置电脑(取消电脑的设置，相当于玩家离开)
                peopleLeave(playerIndex, game);
                return true;
            }
        } else if (parser.parse(jsonResult).isJsonArray()){
            JsonArray jsonArray = parser.parse(jsonResult).getAsJsonArray();
            JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
            if (jsonObject.has("changeRobot") == true) {
                int playerIndex = jsonObject.get("playerIndex").getAsInt();

                //获取电脑玩家的基本信息
                Player player = new Gson().fromJson(jsonArray.get(1), Player.class);
                //根据消息设置电脑
                peopleEnter(playerIndex, player, true, game);

                return true;
            }
        }

        return false;
    }

    //接受准备消息（或者是其他玩家的动作消息）（或者是游戏开始的消息）
    public static boolean dealPrepareResult(String jsonResult, LandlordActivity game) {
        //判断是不是游戏开始的消息
        if (judgeIsGameStart(jsonResult) == true) {
            return true;
        }

        //判断是不是玩家设置电脑的消息
        if (judgeIsRobotSet(jsonResult, game) == true) {
            return false;
        }

        //Json的解析类对象,读取jsonResult
        JsonParser parser = new JsonParser();

        //如果是其他玩家的信息
        if (parser.parse(jsonResult).isJsonArray()) {
            JsonArray jsonArray = parser.parse(jsonResult).getAsJsonArray();

            //获取是哪一位玩家
            JsonObject getIndex = jsonArray.get(0).getAsJsonObject();
            int playerIndex = getIndex.get("playerIndex").getAsInt();
            //获取玩家的动作信息
            JsonObject getAction = jsonArray.get(1).getAsJsonObject();
            int actionReuslt = getAction.get("playerAction").getAsInt();
            //获取玩家动作
            PlayerAction playerAction = PlayerAction.valueOf(actionReuslt);

            switch(playerAction) {
                case PLAYER_ENTER: {
                    peopleEnter(playerIndex, new Gson().fromJson(jsonArray.get(2), Player.class), false, game);
                }
                break;

                case PLAYER_LEAVE: {
                    peopleLeave(playerIndex, game);
                }
                break;

                case PLAYER_PREPARE: {
                    //将玩家设置为准备
                    GlobalValue.playersIsReady[playerIndex] = true;
                    game.peoples[GlobalValue.getDisplayIndex(playerIndex)]
                            .setAction(PeopleActionEnum.PREAPARE);
                }
                break;
            }
        }

        return false;
    }

    private static void peopleEnter(int playerIndex, Player player, Boolean isReady, LandlordActivity game) {
        //获取玩家的信息
        GlobalValue.players[playerIndex] = player;
        game.peoples[GlobalValue.getDisplayIndex(playerIndex)]
                .setPlayerInfo(GlobalValue.players[playerIndex], isReady);
    }

    private static void peopleLeave(int playerIndex, LandlordActivity game) {
        //将玩家信息删除
        GlobalValue.players[playerIndex] = null;
        GlobalValue.playersIsReady[playerIndex] = false;
        game.peoples[GlobalValue.getDisplayIndex(playerIndex)].leave();
    }
}
