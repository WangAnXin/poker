package wanganxin.com.poker.GameLogic.Operator;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import wanganxin.com.poker.GameLogic.OutCard.OutCardStyle;
import wanganxin.com.poker.GameLogic.OutCard.OutCardStyleEnum;
import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.entity.CardOrderMode;
import wanganxin.com.poker.GameLogic.entity.People;

/**
 * Created by Administrator on 2017/4/3.
 */

public class LandLord_GameMode{

    //将卡的编号转化为Card类型
    public static Card ConvertCard(int num) {
        int x = (num > 54 ? num - 54 : num);

        //如果是大小王大小为16,17；普通的卡大小为1~13，再把A,2变为14,15，牌的大小范围为3~17
        int size = (x > 52 ? x - 37 : (x - 1) / 4 + 1);   //refInt - 52 + 13 + 2

        //如果是1和2，加13；如果是大王小王，再加2
        if (size <= 2)  {
            size += 13;
        }

        //返回卡牌的类型
        return new Card(size, (x - 1) % 4, num, 0);
    }

    //发牌的流程
    public void Shuffle(People[] pl, List<Card> cp) {
        int[] num = new int[108];

        //生成108张牌
        for (int i = 0; i < 108; i++) {
            num[i] = i + 1;
        }

        //生成随机种子
        Random random = new Random();

        //随机打乱牌序(Fisher-Yates算法)
        for (int i = 107; i >= 0; i--) {
            int randNum = random.nextInt(i + 1);
            int temp = num[i];
            num[i] = num[randNum];
            num[randNum] = temp;
        }

        //发牌
        for (int i = 0; i < 108; i++) {
            Card card = ConvertCard(num[i]);
            if (i < 100) {
                //对应人的队列增加相应卡牌
                pl[i % 4].deck.add(card);
            } else {
                //底牌先放到牌堆中
                cp.add(card);
            }
        }

        //底牌按照牌的大小显示
        changeOrder(cp, CardOrderMode.CARD_SIZE);
    }

    //改变牌的顺序，0是让牌按牌的大小排，1是让牌按的数量排
    public static void changeOrder(List<Card> deck, CardOrderMode orderMode) {
        switch (orderMode) {
            case CARD_NUM:
                Collections.sort(deck, Card.CompareModeCardNum);
                break;
            case CARD_SIZE:
                Collections.sort(deck, Card.CompareModeCardSize);
                break;
            case CARD_DISPLAY:
                Collections.sort(deck, Card.CompareModeDisplayCard);
                break;
        }
    }

    //判断出牌是否合法
    public boolean canOutCard(List<Card> prevCard, List<Card> myCard) {
        //获取自家的CardStyle
        OutCardStyle myOutCardStyle = OutCardStyle.judgeCardStyle(myCard);

        //如果自家出牌不合法，返回false
        if (myOutCardStyle.outCardStyleEnum == OutCardStyleEnum.CANT_OUT) {
            return false;
        }

        //如果上次出牌的是自己，且自家牌合法，则可以打出
        if (prevCard == null || prevCard.size() == 0) {
            return true;
        }

        //获取上家的cardStyle
        OutCardStyle preOutCardStyle = OutCardStyle.judgeCardStyle(prevCard);

        //如果上家或自家出四大天王，如果是自家出的返回true，否则false
        if (preOutCardStyle.outCardStyleEnum == OutCardStyleEnum.FOUR_GHOST
                || myOutCardStyle.outCardStyleEnum == OutCardStyleEnum.FOUR_GHOST) {
            return myOutCardStyle.outCardStyleEnum == OutCardStyleEnum.FOUR_GHOST;
        }
        //上家出炸弹
        else if (preOutCardStyle.outCardStyleEnum == OutCardStyleEnum.BOMB) {
            //必须自家也是炸弹，比它长，或者相同长度比它大
            return myOutCardStyle.outCardStyleEnum == OutCardStyleEnum.BOMB
                    || preOutCardStyle.cardLength < myOutCardStyle.cardLength
                    || preOutCardStyle.cardLength == myOutCardStyle.cardLength
                    && preOutCardStyle.firstCardSize < myOutCardStyle.firstCardSize;

        }
        //如果上家出其他类型的牌（自家能出炸弹，或者是必须为相同类型，相同长度且要比它大
        else {
            return myOutCardStyle.outCardStyleEnum == OutCardStyleEnum.BOMB
                    || preOutCardStyle.outCardStyleEnum == myOutCardStyle.outCardStyleEnum
                    && preOutCardStyle.cardLength == myOutCardStyle.cardLength
                    && preOutCardStyle.firstCardSize < myOutCardStyle.firstCardSize;
        }
    }

}
