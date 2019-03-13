using pokerServer.Landlord.entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.Landlord.OutCard {
    class PlayerRemindCard {
        //玩家任意出牌的游戏提示，记录上次找的牌
        public static void remindCard(People people, List<Card> prevCard, bool canOutBoom) {
            //判断上家的卡组样式
            OutCardStyle preOutCardStyle = OutCardStyle.judgeCardStyle(prevCard);
            //如果想出什么牌就出什么牌
            if (preOutCardStyle.outCardStyleEnum == OutCardStyleEnum.CANT_OUT) {
                people.htCards = new List<Card>();
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
        public static void peopleFirstHint(People people, bool canSplit) {
            List<Card> deck = people.deck;
            int length = people.deck.Count;
            OutCardStyle createOutCardStyle;    //伪造一个outCardStyle，判断其能否出相应的牌

             do {
                switch (people.htStyle) {
                    //如果是第一次提示（大的开始提示）
                    case OutCardStyleEnum.CANT_OUT:

                    case OutCardStyleEnum.PLANE_TAKE_TWO:
                    //飞机带几对（按最长的长度，最小的（不存在）构造，再依次降低长度，直到找到）
                    if (length >= 10) {
                        for (int cardLen = length / 5 * 5; cardLen >= 10 && people.htCards.Count == 0; cardLen -= 5) {
                            createOutCardStyle = new OutCardStyle(OutCardStyleEnum.PLANE_TAKE_TWO, 0, cardLen);
                            people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                        }
                    }
                    people.htStyle = OutCardStyleEnum.PLANE;
                    break;

                    case OutCardStyleEnum.PLANE:
                    //飞机不带
                    for (int cardLen = length / 3 * 3; cardLen >= 6 && people.htCards.Count == 0; cardLen -= 3) {
                        createOutCardStyle = new OutCardStyle(OutCardStyleEnum.PLANE, 0, cardLen);
                        people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    }
                    people.htStyle = OutCardStyleEnum.NEXT_TWO;
                    break;

                    case OutCardStyleEnum.NEXT_TWO:
                    //连对，对3-对A共24张
                    for (int i = Math.Min(24, length / 2 * 2); i >= 6 && people.htCards.Count == 0; i -= 2) {
                        createOutCardStyle = new OutCardStyle(OutCardStyleEnum.NEXT_TWO, 0, i);
                        people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    }
                    people.htStyle = OutCardStyleEnum.STRAIGHT;
                    break;

                    case OutCardStyleEnum.STRAIGHT:
                    //顺子，3-A共12张
                    for (int cardLen = Math.Min(12, length); cardLen >= 5 && people.htCards.Count == 0; cardLen--) {
                        createOutCardStyle = new OutCardStyle(OutCardStyleEnum.STRAIGHT, 0, cardLen);
                        people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    }
                    people.htStyle = OutCardStyleEnum.THREE_TAKE_TWO;
                    break;

                    case OutCardStyleEnum.THREE_TAKE_TWO:
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.THREE_TAKE_TWO, 0, 5);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    people.htStyle = OutCardStyleEnum.THREE;
                    break;

                    case OutCardStyleEnum.THREE:
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.THREE, 0, 3);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    people.htStyle = OutCardStyleEnum.TWO;
                    break;

                    case OutCardStyleEnum.TWO:
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.TWO, 0, 2);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    people.htStyle = OutCardStyleEnum.ONE;
                    break;

                    case OutCardStyleEnum.ONE:
                    createOutCardStyle = new OutCardStyle(OutCardStyleEnum.ONE, 0, 1);
                    people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    people.htStyle = OutCardStyleEnum.BOMB;
                    break;

                    case OutCardStyleEnum.BOMB:
                    for (int i = 8; i >= 4; i--) {
                        createOutCardStyle = new OutCardStyle(OutCardStyleEnum.BOMB, 0, i);
                        people.htCards = CrushPreCard.crushPreCard(deck, createOutCardStyle, false, canSplit);
                    }
                    people.htStyle = OutCardStyleEnum.FOUR_GHOST;
                    break;

                    case OutCardStyleEnum.FOUR_GHOST:
                    if (OutCardStyle.isFourGhost(people.deck) == true) {
                        people.htCards = people.deck;
                    }
                    people.htStyle = OutCardStyleEnum.CANT_OUT;
                    break;
                }
            } while (people.htCards.Count == 0 && people.htStyle != OutCardStyleEnum.CANT_OUT) ;

        }
    }
}
