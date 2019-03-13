using pokerServer.Landlord.entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.Landlord.OutCard {
    class CrushPreCard {
        public static List<Card> crushPreCard(List<Card> deck, OutCardStyle preOutCardStyle, bool canOutBoom, bool canSplit) {
            List<Card> reminderList = new List<Card>();

            //如果卡组比本身的牌少，不考虑
            if (deck.Count < preOutCardStyle.cardlength) {
                return reminderList;
            }

            //上家出什么调用相应的提示
            switch (preOutCardStyle.outCardStyleEnum) {
                case OutCardStyleEnum.FOUR_GHOST://上家出四大天王
                break;
                case OutCardStyleEnum.BOMB://上家出炸弹
                reminderList = findBomb(deck, preOutCardStyle);
                break;
                case OutCardStyleEnum.ONE://上家出单牌
                reminderList = findOneTwoThree(deck, preOutCardStyle, canOutBoom, canSplit, 1);
                break;
                case OutCardStyleEnum.TWO://上家出对子
                reminderList = findOneTwoThree(deck, preOutCardStyle, canOutBoom, canSplit, 2);
                break;
                case OutCardStyleEnum.THREE://上家出三不带
                reminderList = findOneTwoThree(deck, preOutCardStyle, canOutBoom, canSplit, 3);
                break;
                case OutCardStyleEnum.THREE_TAKE_TWO://上家出三带二
                reminderList = findThreeWithTwo(deck, preOutCardStyle, canOutBoom, canSplit);
                break;
                case OutCardStyleEnum.NEXT_TWO://上家出连对
                //如果是玩家自己出牌的提示，不拆
                if (canSplit == true && canOutBoom == false)
                    reminderList = findStraghitOrNextTwoCanSplit(deck, preOutCardStyle, canOutBoom, 2, false);
                else
                    reminderList = findStraghitOrNextTwoCanSplit(deck, preOutCardStyle, canOutBoom, 2, canSplit);
                break;
                case OutCardStyleEnum.STRAIGHT://上家出顺子
                //如果是玩家自己出牌的提示，不拆
                if (canSplit == true && canOutBoom == false)
                    reminderList = findStraghitOrNextTwoCanSplit(deck, preOutCardStyle, canOutBoom, 1, false);
                else
                    reminderList = findStraghitOrNextTwoCanSplit(deck, preOutCardStyle, canOutBoom, 1, canSplit);
                break;
                case OutCardStyleEnum.PLANE://上家出飞机不带
                reminderList = findStraightNextTwoPlane(deck, preOutCardStyle, canOutBoom, 3);
                break;
                case OutCardStyleEnum.PLANE_TAKE_TWO://上家出飞机带两对
                reminderList = findPlaneWithTwo(deck, preOutCardStyle, canOutBoom);
                break;
            }
            return reminderList;
        }

        //判断能不能出炸弹和四王（必须保证已经按照牌的数量排序）（尽量找最小）
        public static List<Card> findBomb(List<Card> deck, int pos) {
            List<Card> resCards = new List<Card>();
            for (int i = pos; i >= 0; i -= deck[i].sameCardNum) {
                //如果找到炸弹
                if (deck[i].sameCardNum >= 4) {
                    int cardNum = deck[i].sameCardNum;
                    while (cardNum-- > 0) {
                        resCards.Add(deck[i--]);
                    }
                    return resCards;
                }
            }
            //找四大天王
            return findFourGoust(deck);
        }

        //判断能不能找到四大天王（必须保证已经按照牌的数量排序）
        public static List<Card> findFourGoust(List<Card> deck) {
            //按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardSize);

            List<Card> resCards = new List<Card>();
            //如果找到四大天王，将其加入
            if (deck.Count >= 4
                    && deck[0].cardSize == 17 && deck[0].sameCardNum == 2
                    && deck[2].cardSize == 16 && deck[2].sameCardNum == 2) {
                resCards.Add(deck[0]);
                resCards.Add(deck[1]);
                resCards.Add(deck[2]);
                resCards.Add(deck[3]);
            }

            return resCards;
        }

        //出炸弹的提示（打别人的炸弹）（尽量找最小）
        public static List<Card> findBomb(List<Card> deck, OutCardStyle preCardStyle) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardNum);

            List<Card> resCards = new List<Card>();
            for (int i = deck.Count - 1; i >= 0; i -= deck[i].sameCardNum) {
                //我出炸弹
                if (deck[i].sameCardNum == preCardStyle.cardlength && deck[i].cardSize > preCardStyle.firstCardSize
                        || deck[i].sameCardNum > preCardStyle.cardlength) {
                    int cardNum = deck[i].sameCardNum;
                    while (cardNum-- > 0) {
                        resCards.Add(deck[i--]);
                    }
                    //如果发现则返回
                    return resCards;
                }
            }

            return findFourGoust(deck);
        }

        //找最小的单支，对子，可以任意拆
        public static List<Card> findMinOneTwo(List<Card> deck, int cardNum) {
            List<Card> resCards = new List<Card>();
            int minIndex = -1;
            for (int i = 0; i < deck.Count; i += deck[i].sameCardNum) {
                if (deck[i].sameCardNum >= cardNum) {
                    if (minIndex == -1 || deck[minIndex].cardSize > deck[i].cardSize) {
                        minIndex = i;
                    }
                }
            }
            //如果没找到，自己家没对子了
            if (minIndex == -1) {
                while (cardNum-- > 0) {
                    resCards.Add(deck[minIndex++]);
                }
            }

            return resCards;
        }

        //出单支，对子，三不带的提示，不可以拆炸弹，但可以拆2，玩家出牌可以拆任意的牌（尽量找最小）
        public static List<Card> findOneTwoThree(List<Card> deck, OutCardStyle preCardStyle, bool canOutBoom, bool canSplit, int cardNum) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardNum);
            List<Card> resCards = new List<Card>();

            //先从对子里面找，根据情况能不能拆，不拆4个头，（拆不是4个头的炸还是炸）
            int pos;    //保存最后找的位置
            for (pos = deck.Count - 1; pos >= 0 && deck[pos].sameCardNum < 4; pos -= deck[pos].sameCardNum) {
                //找到对子则返回
                if (deck[pos].cardSize > preCardStyle.firstCardSize) {
                    if (deck[pos].sameCardNum == cardNum) {
                        while (cardNum-- > 0) {
                            resCards.Add(deck[pos--]);
                        }
                        return resCards;
                    } else if (deck[pos].sameCardNum > cardNum) {
                        //如果处于自己出牌的状态（不拆其他牌）
                        if (canOutBoom == false) {
                            break;
                        }
                        //如果处于打别人牌状态（可以拆分）
                        else {
                            //如果可以拆分（玩家提示，无论拆什么都行（不拆炸弹））
                            // 如果是电脑出牌，找2，小王，大王拆
                            // 或者是打单支只剩对子，打对子只剩三个头
                            if (canSplit == true || deck[pos].cardSize >= 15 || deck.Count <= cardNum + 1) {
                                while (cardNum-- > 0) {
                                    resCards.Add(deck[pos--]);
                                }
                                return resCards;
                            }
                        }
                    }
                }
            }
            return canOutBoom == true ? findBomb(deck, pos) : resCards;
        }

        //出三带二的提示//先塞3个，再塞2个//如果没有对子，可以从比当前三个头小的对子中拆对子出
        public static List<Card> findThreeWithTwo(List<Card> deck, OutCardStyle preCardStyle, bool canOutBoom, bool canSplit) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardNum);
            List<Card> resCards = new List<Card>();

            int twoPos, threePos, twoInThreePos;
            bool isFindTwo = false;  //在对子中有没有找到对子
            bool isInThreeFindTwo = false;  //在三个个头中有没有找到对子

            //先找对子
            for (twoPos = deck.Count - 1; twoPos >= 0; twoPos--) {
                if (deck[twoPos].sameCardNum < 2) continue;
                else if (deck[twoPos].sameCardNum > 2) break;
                else {
                    //找到对子
                    isFindTwo = true;
                    break;
                }
            }

            twoInThreePos = twoPos;//保存未找到或找到的最后的位置
                                   //如果没有找到正好为两张的对子，就在三张里面找对子
            if (canSplit == true && !isFindTwo) {
                for (; twoInThreePos >= 0; twoInThreePos -= deck[twoInThreePos].sameCardNum) {
                    if (deck[twoInThreePos].sameCardNum < 3) continue;
                    else if (deck[twoInThreePos].sameCardNum > 3) break;
                    else {
                        //找到对子
                        isInThreeFindTwo = true;
                        break;
                    }
                }
            }

            threePos = twoInThreePos;  //保存未找到或找到的最后的位置
                                       //找对应的三个头
            if (isFindTwo || isInThreeFindTwo) {
                for (; threePos >= 0; threePos -= deck[threePos].sameCardNum) {
                    if (deck[threePos].sameCardNum < 3) continue;
                    if (deck[threePos].sameCardNum > 3) break;
                    //找到三个头
                    if (deck[threePos].cardSize > preCardStyle.firstCardSize) {
                        for (int k = 0; k < 3; k++) resCards.Add(deck[threePos--]);
                        for (int k = 0; k < 2; k++) resCards.Add(deck[twoPos--]);
                        return resCards;
                    }
                }
            }
            return canOutBoom == true ? findBomb(deck, threePos) : resCards;
        }

        //出顺子，连对，飞机不带的提示（不能拆的版本）
        public static List<Card> findStraightNextTwoPlane(List<Card> deck, OutCardStyle preCardStyle, bool canOutBoom, int cardNum) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardNum);

            List<Card> resCards = new List<Card>();
            int pos;    //保存最后找的位置
            int length = preCardStyle.cardlength;
            for (pos = deck.Count - 1; pos >= 0 && deck[pos].sameCardNum <= cardNum; pos -= deck[pos].sameCardNum) {
                if (deck[pos].sameCardNum < cardNum) continue;
                //找到连对：先比它大、它长度最后是对子且是连对且符合要求（小于等于A）
                if (deck[pos].cardSize > preCardStyle.firstCardSize
                        && pos - length + 1 >= 0
                        && deck[pos - length + 1].sameCardNum == cardNum
                        && deck[pos - length + 1].cardSize == deck[pos].cardSize + length / cardNum - 1
                        && deck[pos - length + 1].cardSize <= 14) {
                    while (length-- > 0) {
                        resCards.Add(deck[pos--]);
                    }
                    return resCards;
                }
            }
            return canOutBoom == true ? findBomb(deck, pos) : resCards;
        }

        //出飞机连对的提示
        public static List<Card> findPlaneWithTwo(List<Card> deck, OutCardStyle preCardStyle, bool canOutBoom) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardNum);

            List<Card> resCards = new List<Card>();
            int i;
            bool isFindTwo = false;
            int length = preCardStyle.cardlength;
            int len = length / 5 * 3;
            int len2 = length / 5 * 2;
            //先找到足够的对子后，
            for (i = deck.Count - 1; i >= 0 && deck[i].sameCardNum < 3; i--) {
                if (deck[i].sameCardNum < 2) continue;
                else if (i - len2 + 1 >= 0 && deck[i - len2 + 1].sameCardNum == 2) {
                    isFindTwo = true;
                    break;
                }
            }
            int j = i;
            //如果找到对应个数的对子，找大于它的飞机
            if (isFindTwo == true) {
                for (; j >= 0 && deck[j].sameCardNum < 4; j -= deck[j].sameCardNum) {
                    if (deck[j].sameCardNum < 3) continue;

                    //找到飞机不带：先比它大、它长度最后是三个头且是飞机且符合要求（小于等于A）
                    if (deck[j].cardSize > preCardStyle.firstCardSize
                            && j - len + 1 >= 0
                            && deck[j - len + 1].sameCardNum == 3
                            && deck[j - len + 1].cardSize == deck[j].cardSize + len / 3 - 1
                            && deck[j - len + 1].cardSize <= 14) {

                        for (int k = 0; k < len; k++) resCards.Add(deck[j--]);
                        for (int k = 0; k < len2; k++) resCards.Add(deck[i--]);

                        //返回结果
                        return resCards;
                    }
                }
            }
            return canOutBoom == true ? findBomb(deck, j) : resCards;
        }

        //出顺子或连对的提示（可拆版本）//必须先按牌从大到小排序
        public static List<Card> findStraghitOrNextTwoCanSplit(List<Card> deck, OutCardStyle preCardStyle, bool canOutBoom, int oneOrTwo, bool canSplit) {
            //按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardSize);

            List<Card> resCards = new List<Card>();
            int length = preCardStyle.cardlength / oneOrTwo; //对应要出的牌的长度
            int i = deck.Count - 1;

            for (; i >= 0; i -= deck[i].sameCardNum) {
                if (deck[i].cardSize > preCardStyle.firstCardSize && deck[i].sameCardNum >= oneOrTwo) {
                    int j = i, nextJ = i;
                    int splitNum = 0;   //拆成单支的个数
                    int curLen = 0; //当前长度
                    do {
                        //判断有没有拆炸弹（如果拆的炸弹，拆完之后还是炸弹不算）
                        if (deck[nextJ].sameCardNum >= 4 && deck[nextJ].sameCardNum - oneOrTwo < 4) {
                            break;
                        }

                        //判断是否拆了不是顺子或三个（拆了之后变成单支了）
                        if (deck[nextJ].sameCardNum == oneOrTwo + 1) {
                            splitNum++;
                        }

                        //将结果保存起来
                        for (int k = 0; k < oneOrTwo; k++)
                            resCards.Add(deck[nextJ - k]);

                        curLen++;       //长度增加
                        j = nextJ;      //找下一个
                        nextJ = j - deck[j].sameCardNum;
                        //找到足够数量的顺子或连对，数量超过cardnum，长度要够上一家出的牌，要连在一起，最高是A
                    } while (nextJ >= 0
                            && deck[nextJ].sameCardNum >= oneOrTwo
                            && curLen < length
                            && deck[j].cardSize == deck[nextJ].cardSize - 1
                            && deck[nextJ].cardSize <= 14);

                    //如果找到
                    if (curLen == length) {
                        //根据拆的个数的长度，判断是否让拆
                        //如果len=5,最多可以拆2个，len=6,可拆2，len=7,可拆3
                        //如果是可以随便拆，或者电脑可以拆的版本跳出，否则找下一种（电脑可拆）
                        if (length >= splitNum * 2 + 1 || canSplit == true)
                            break;
                    }

                    //如果没找到，i直接从j开始，因为它不连续，或者中间有炸弹
                    i = j;
                    resCards.Clear();
                }
            }

            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            deck.Sort(Card.CompareModeCardNum);
            return canOutBoom == true ? findBomb(deck, deck.Count - 1) : resCards;
        }
    }
}
