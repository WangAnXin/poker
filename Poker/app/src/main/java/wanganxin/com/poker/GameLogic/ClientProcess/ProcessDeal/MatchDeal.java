package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wanganxin.com.poker.GameLogic.utilities.GlobalValue;
import wanganxin.com.poker.GameLogic.entity.Player;

//进入游戏大厅的过程
public class MatchDeal {

    private static String startMatch = "startMatch";
    private static String changeInfo = "changeInfo";

    //准备进入游戏大厅
    public static String getMatchGameMsg() {
        //发消息给服务器开始匹配
        JsonObject jsonObject = new JsonObject();

        //开始进入游戏大厅
        jsonObject.addProperty(startMatch, true);

        //将结果返回
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }

    //发送玩家个人信息的修改
    public static String getChangePlayerInfoMsg(Player player) {
        //发消息给服务器要修改信息
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(changeInfo, true);
        jsonObject.addProperty("playerName", player.name);
        jsonObject.addProperty("playerSex", player.sex);
        jsonObject.addProperty("playerImage", player.image);

        //将结果返回
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }

    //接受服务器返回的结果
    public static boolean dealMatchResult(String jsonResult) {
        //读取jsonResult
        //Json的解析类对象
        JsonParser parser = new JsonParser();

        //如果是不是array数组，返回错误
        if (!parser.parse(jsonResult).isJsonArray()) {
            return false;
        }

        JsonArray jsonArray = parser.parse(jsonResult).getAsJsonArray();

        //如果返回的是断线重连的信息，则进行断线重连的处理
        if (ReconnectDeal.firstDealReconnected(jsonArray)) {
            return false;
        }

        //当前array中读取的index
        int arrayIndex = 0;
        //获取返回结果
        JsonObject result = jsonArray.get(arrayIndex++).getAsJsonObject();
        //保存匹配是否成功
        boolean isMatched = true;
        do {
            if (result.has("matchResult") == false
                    || result.get("matchResult").getAsBoolean() == false) {

                //如果匹配不成功
                isMatched = false;
                break;
            }
            //从jsonArray指定的index获取玩家当前的位置信息和其余玩家的信息
            getPlayerIndexInfo(jsonArray, arrayIndex);

        } while (false);

        //返回匹配结果
        return isMatched;
    }

    //从jsonArray指定的index获取玩家当前的位置信息和其余玩家的信息
    public static void getPlayerIndexInfo(JsonArray jsonArray, int arrayIndex) {
        //如果为匹配返回结果且返回成功
        //1.获取当前玩家所在位置
        JsonObject playerIndex = jsonArray.get(arrayIndex++).getAsJsonObject();
        GlobalValue.playerIndex = playerIndex.get("playerIndex").getAsInt();

        //2.获取玩家信息和玩家的准备信息
        Gson gson = new Gson();
        for (int i = 0; i < 4; i++) {
            //获取玩家的信息
            GlobalValue.players[i] = gson.fromJson(jsonArray.get(arrayIndex++), Player.class);

            //如果玩家的姓名信息不为空（fromJson成功与否都会创建一个对象，但是name会为空）获取玩家的准备情况
            if (GlobalValue.players[i].name != null) {
                JsonObject isReady = jsonArray.get(arrayIndex++).getAsJsonObject();
                GlobalValue.playersIsReady[i] = isReady.get("isReady").getAsBoolean();
            } else {
                GlobalValue.players[i] = null;
            }
        }
    }
}
