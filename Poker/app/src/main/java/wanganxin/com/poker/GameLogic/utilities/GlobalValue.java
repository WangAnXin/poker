package wanganxin.com.poker.GameLogic.utilities;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.Operator.LandLord_GameMode;
import wanganxin.com.poker.GameLogic.entity.Player;

//全局变量
public class GlobalValue {
    //四个玩家的信息(和服务器上的players的顺序一致)
    public static Player[] players = new Player[4];
    //玩家当前所在的位置
    public static int playerIndex;
    //玩家是否准备
    public static boolean[] playersIsReady = {false, false, false, false};
    //网络位置对应于游戏中的位置
    public static int[] playersIndex = new int[4];

    //获取游戏中的位置
    public static int getDisplayIndex(int netIndex) {
        return playersIndex[netIndex];
    }

    //将json字符串转换为card数组
    //将cards先按UTF8解析为字节（1个字节0~255，cardIndex最大为108），再将字节转换为List<Card>
    public static List<Card> jsonToCards(String cards) {
        List<Card> resCards = new ArrayList<Card>();

        try {
            //转换为
            byte[] byteCards = cards.getBytes("UTF8");
            for (int i = 0; i < cards.length(); i++) {
                resCards.add(LandLord_GameMode.ConvertCard(byteCards[i]));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return resCards;
    }

    //将card数组转换为Json字符串发送
    public static String cardsToJson(List<Card> cards) {
        //因为卡最多108张，一个byte8位0~255，足够放了
        byte[] buffer = new byte[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            buffer[i] = (byte)cards.get(i).cardIndex;
        }

        String cardsMsg = null;

        try {
            //按UTF8转换
            cardsMsg = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return cardsMsg;
    }
}
