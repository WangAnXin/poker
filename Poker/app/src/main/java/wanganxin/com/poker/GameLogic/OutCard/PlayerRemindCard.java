package wanganxin.com.poker.GameLogic.OutCard;

import java.util.ArrayList;
import java.util.List;

import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.entity.People;

//提示出牌
public class PlayerRemindCard {

    //玩家任意出牌的游戏提示，记录上次找的牌
    public static void remindCard(People people, List<Card> prevCard, boolean canOutBoom) {
        //判断上家的卡组样式
        OutCardStyle preOutCardStyle = OutCardStyle.judgeCardStyle(prevCard);
        //如果想出什么牌就出什么牌
        if (preOutCardStyle.outCardStyleEnum == OutCardStyleEnum.CANT_OUT) {
            people.htCards = new ArrayList<Card>();
            peopleFirstHint(people, true);
        }
        //打上一家出的牌（如果是打自己出的牌，canOutBoom=false）
        else {
            people.htCards = CrushPreCard.crushPreCard(people.deck, preOutCardStyle, canOutBoom, true);
        }
    }

    //玩家想出什么牌出什么牌的提示
    // 按各种可能性提示，第i+1次提示比第i次提示要不同类型大，要不提示下一种类型
    // 算法思想，伪造一个cardStyle，如果当前玩家能打，则可以出
    public static void peopleFirstHint(People people, boolean canSplit) {
        List<Card> deck = people.deck;
        int length = people.deck.size();
        OutCardStyle createOutCardStyle;    //伪造一个outCardStyle，判断其能否出相应的牌

        switch (people.htStyle) {
            //如果是第一次提示（大的开始提示）
            case CANT_OUT:

            case PLANE_TAKE_TWO:
                //飞机带几对（按最长的长度，最小的（不存在）构造，再依次降低长度，直到找到）
                if (length >= 10) {
                    for (int cardLen = length / 5 * 5; cardLen >= 10 && people.htCards.size() == 0; cardLen -= 5) {
                        createOutCardStyle = new OutCardStyle(OutCardStyleEnum.PLANE_TAKE_TWO, 0, cardLen);
                        people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    }
                }
                people.htStyle = OutCardStyleEnum.PLANE;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case PLANE:
                //飞机不带
                for (int cardLen = length / 3 * 3; cardLen >= 6 && people.htCards.size() == 0; cardLen -= 3) {
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.PLANE, 0, cardLen);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                }
                people.htStyle = OutCardStyleEnum.NEXT_TWO;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case NEXT_TWO:
                //连对，对3-对A共24张
                for (int i = Math.min(24, length / 2 * 2); i >= 6 && people.htCards.size() == 0; i -= 2) {
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.NEXT_TWO, 0, i);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                }
                people.htStyle = OutCardStyleEnum.STRAIGHT;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case STRAIGHT:
                //顺子，3-A共12张
                for (int cardLen = Math.min(12, length); cardLen >= 5 && people.htCards.size() == 0; cardLen--) {
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.STRAIGHT, 0, cardLen);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                }
                people.htStyle = OutCardStyleEnum.THREE_TAKE_TWO;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case THREE_TAKE_TWO:
                createOutCardStyle = new OutCardStyle(OutCardStyleEnum.THREE_TAKE_TWO, 0, 5);
                people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                people.htStyle = OutCardStyleEnum.THREE;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case THREE:
                createOutCardStyle = new OutCardStyle(OutCardStyleEnum.THREE, 0, 3);
                people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                people.htStyle = OutCardStyleEnum.TWO;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case TWO:
                createOutCardStyle = new OutCardStyle(OutCardStyleEnum.TWO, 0, 2);
                people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                people.htStyle = OutCardStyleEnum.ONE;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case ONE:
                createOutCardStyle = new OutCardStyle(OutCardStyleEnum.ONE, 0, 1);
                people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                people.htStyle = OutCardStyleEnum.BOMB;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }

            case BOMB:
                for (int i = 8; i >= 4; i--) {
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.BOMB, 0, i);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                }
                people.htStyle = OutCardStyleEnum.FOUR_GHOST;
                //如果找到则跳出
                if (people.htCards.size() != 0) {
                    break;
                }
            case FOUR_GHOST:
                if (OutCardStyle.isFourGhost(people.deck) == true) {
                    people.htCards = people.deck;
                }
                people.htStyle = OutCardStyleEnum.CANT_OUT;
        }
    }


}
