package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;

public class CardReceiveDeal {
    //获取四个人的卡组信息和底牌信息
    public static void getCardInfo(JsonArray jsonArray, int startIndex, LandlordActivity game) {
        //读取4个人的卡组信息
        for (int i = 0; i < 4; i++) {
            JsonObject cardsObject = jsonArray.get(i + startIndex).getAsJsonObject();
            game.peoples[GlobalValue.getDisplayIndex(i)].setPlayerDeck(
                    GlobalValue.jsonToCards(cardsObject.get("deck" + i).getAsString()) );
        }

        //读取底牌信息
        JsonObject cardPileObject = jsonArray.get(startIndex + 4).getAsJsonObject();
        game.cardpile = GlobalValue.jsonToCards(cardPileObject.get("cardPile").getAsString());
    }
}
