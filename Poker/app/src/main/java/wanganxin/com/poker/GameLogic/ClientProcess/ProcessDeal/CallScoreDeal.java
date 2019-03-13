package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wanganxin.com.poker.GameLogic.GameProcess.CallScore.NetCallScore;
import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;


public class CallScoreDeal {

    //对叫分处理返回的结果
    public enum CallScoreResult {
        NONE,

        //继续叫分
        CONTINUE,

        //叫分成功
        SUCCESS,

        //没人叫分
        NO_SCORE,
    }

    public static int callScore;

    public static String getCallScoreMsg() {
        //发消息给服务器叫分信息
        JsonObject jsonObject = new JsonObject();

        //开始进入游戏大厅
        jsonObject.addProperty("callScore", callScore);

        return new Gson().toJson(jsonObject);
    }

    //接受服务器返回的结果
    public static CallScoreResult dealCallScoreResult(String receiveMsg, LandlordActivity game) {
        JsonParser parser = new JsonParser();
        NetCallScore netCallScore = (NetCallScore)game.process.callScoreProcess;

        CallScoreResult callScoreResult = CallScoreResult.CONTINUE;

        //如果是object对象（获取第一个叫分的人）
        if (parser.parse(receiveMsg).isJsonObject()) {
            JsonObject people = parser.parse(receiveMsg).getAsJsonObject();

            if (people.has("firstCallScore")) {
                netCallScore.firstPeople = GlobalValue.getDisplayIndex(people.get("firstCallScore").getAsInt());

                //游戏显示进程切换（开始发牌）
                game.process.gameProcessChange();

                //继续叫分
                callScoreResult = CallScoreResult.CONTINUE;
            }
            //如果三次都没人叫分，三次之后直接由服务器指定
            else if (people.has("whoIsLand")) {
                game.whosLand = GlobalValue.getDisplayIndex(people.get("whoIsLand").getAsInt());
                game.integration[0] = 1;

                //游戏显示进程切换（开始发地主的牌）
                game.process.gameProcessChange();

                callScoreResult = CallScoreResult.SUCCESS;
            }
        }
        //如果是其他人的叫分情况
        else{
            JsonArray callScoreObject = parser.parse(receiveMsg).getAsJsonArray();

            //获取叫分的人
            JsonObject callPeople = callScoreObject.get(0).getAsJsonObject();
            int playerIndex = GlobalValue.getDisplayIndex(callPeople.get("playerIndex").getAsInt());

            //获取所叫的分数
            int callScore = callScoreObject.get(1).getAsJsonObject().get("callScore").getAsInt();

            //如果客户端和服务端信息不一致，将playerIndex前的玩家直接设置为不叫，和服务器同步
//            for (int i = 0; (i + netCallScore.begin_pos) % 4 != playerIndex; i++) {
//                netCallScore.playersCallScore[(i + netCallScore.begin_pos) % 4] = 0;
//            }

            //设置当前人的叫分
            netCallScore.playersCallScore[playerIndex] = callScore;
            if (callScore > game.integration[0]) {
                game.integration[0] = callScore;
                game.whosLand = playerIndex;
            }

            //继续叫分
            callScoreResult = CallScoreResult.CONTINUE;

            //如果叫了三分或者每个人都叫完了
            if ((playerIndex + 1) % 4 == netCallScore.firstPeople) {
                //如果没人叫分
                if (callScore == 0) {
                    if (game.process.reCallScore < Constants.MAX_RE_CALLSCROE_NUM - 1) {
                        callScoreResult = CallScoreResult.NO_SCORE;
                    }
                } else {
                    callScoreResult = CallScoreResult.SUCCESS;
                }
            }
            //或者有人直接叫了三分，叫分也结束
            else if (callScore >= 3) {
                callScoreResult = CallScoreResult.SUCCESS;
            }
        }

        return callScoreResult;
    }
}
