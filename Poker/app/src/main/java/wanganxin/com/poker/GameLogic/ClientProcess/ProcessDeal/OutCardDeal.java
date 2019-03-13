package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.GameProcess.OutCard.NetOutCard;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;

//发送出牌信息和处理别人的出牌信息
public class OutCardDeal {
    public static boolean isRobortInfo = true;
    //给服务器发送玩家的出牌信息
    public static String getOutCardMsg(LandlordActivity game) {
        //发消息给服务器出牌信息
        JsonObject jsonObject = new JsonObject();

        //如果是发送托管信息
        if (isRobortInfo == true) {
            jsonObject.addProperty("isRobortInfo", game.isHosting);
        } else {
            //将玩家的出牌发给服务器
            jsonObject.addProperty("outCard",
                    GlobalValue.cardsToJson(game.process.outCardProcess.toOutCard[0]));
        }

        return new Gson().toJson(jsonObject);
    }

    //接受服务器返回的结果
    public static boolean dealOutCardResult(String receiveMsg, LandlordActivity game) {
        boolean isGameEnd = false;
        //如果是别人的出牌信息
        JsonArray outCardArray = new JsonParser().parse(receiveMsg).getAsJsonArray();

        //获取发出动作的人
        JsonObject indexObject = outCardArray.get(0).getAsJsonObject();
        int playerIndex = GlobalValue.getDisplayIndex(indexObject.get("playerIndex").getAsInt());

        //获取卡组信息或者是托管信息
        JsonObject infoObject = outCardArray.get(1).getAsJsonObject();

        //如果是别人的出牌信息
        if (infoObject.has("outCard")) {
            //获取所叫的分数
            String outCard = infoObject.get("outCard").getAsString();

            NetOutCard netOutCard = (NetOutCard)game.process.outCardProcess;
            //获取所出的牌
            netOutCard.toOutCard[playerIndex] = GlobalValue.jsonToCards(outCard);

            //如果当前玩家出完牌，游戏结束
            //如果是当前客户端的玩家，是客户端卡组先减完牌再发消息给服务器，客户端收到服务器端卡组的减牌消息
            if (playerIndex == 0 && game.peoples[playerIndex].deck.size() == 0) {
                isGameEnd = true;
            } else if (playerIndex != 0
                    && game.peoples[playerIndex].deck.size()
                    == netOutCard.toOutCard[playerIndex].size()) {
                isGameEnd = true;
            }

            //设置当前人已经出牌，客户端动画显示
            netOutCard.isPlayerOutCard[playerIndex] = true;
        }

        return isGameEnd;
    }
}
