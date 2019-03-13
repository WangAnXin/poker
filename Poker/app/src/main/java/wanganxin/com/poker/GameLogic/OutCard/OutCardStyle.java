package wanganxin.com.poker.GameLogic.OutCard;

import java.util.Collections;
import java.util.List;

import wanganxin.com.poker.GameLogic.entity.Card;

public class OutCardStyle {
    public OutCardStyleEnum outCardStyleEnum;      //出牌形式
    public int firstCardSize;       //相同牌的第一张
    public int cardLength;          //出牌的数量

    //构造函数
    public OutCardStyle(OutCardStyleEnum outCardStyleEnum, int firstCardSize, int cardLength) {
        this.outCardStyleEnum = outCardStyleEnum;
        this.firstCardSize = firstCardSize;
        this.cardLength = cardLength;
    }
    private OutCardStyle() {
        outCardStyleEnum = OutCardStyleEnum.CANT_OUT;
        firstCardSize = 0;
        cardLength = 0;
    }

    //判断是不是对子，返回对子的第一张牌
    private static int isTwo(List<Card> cards) {
        //如果两张牌不相等不是
        if (cards.get(0).sameCardNum == 2) {
            return cards.get(0).cardSize;
        } else {
            return 0;
        }
    }

    //判断是不是三不带，返回第一张牌
    private static int isThree(List<Card> cards) {
        if (cards.get(0).sameCardNum == 3) {
            return cards.get(0).cardSize;
        } else {
            return 0;
        }
    }

    //判断是不是三带二，返回三个头的第一张牌(不能按照牌的数量排序)
    private static int isThreeTakeTwo(List<Card> cards) {
        //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
        Collections.sort(cards, Card.CompareModeCardNum);

        //如果不是5张牌，返回错误
        if (cards.size() != 5
                || cards.get(0).sameCardNum != 3
                || cards.get(cards.size() - 1).sameCardNum != 2) {
            return 0;
        }

        return cards.get(0).cardSize;
    }


    //判断是不是连对，返回第一个对子的第一张牌(最小的)
    public static int isNextTwo(List<Card> cards) {
        //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
        Collections.sort(cards, Card.CompareModeCardNum);

        int length = cards.size();
        //连对至少3对，且连对最大不能是2，不是都是对子，不是连的
        if (length < 6 || length % 2 != 0 || cards.get(0).cardSize > 14
                || cards.get(0).sameCardNum != 2
                || cards.get(0).sameCardNum != cards.get(length - 1).sameCardNum
                || (cards.get(0).cardSize - cards.get(length - 1).cardSize + 1) * 2 != length) {
            return 0;
        }

        return cards.get(length - 1).cardSize;
    }

    //判断是不是顺子，返回顺子的第一张牌
    public static int isStraight(List<Card> cards) {
        //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
        Collections.sort(cards, Card.CompareModeCardNum);

        int length = cards.size();
        //顺子最少5张，顺子最大不能是2，不是都是单支，不是连的
        if (length < 5 || cards.get(0).cardSize > 14
                || cards.get(0).sameCardNum != 1
                || cards.get(0).sameCardNum != cards.get(length - 1).sameCardNum
                || cards.get(0).cardSize - cards.get(length - 1).cardSize + 1 != length) {
            return 0;
        }

        return cards.get(length - 1).cardSize;
    }

    //判断是不是飞机不带(三个头连对)，返回飞机的第一张牌
    public static int isPlane(List<Card> cards) {
        //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
        Collections.sort(cards, Card.CompareModeCardNum);

        int length = cards.size();
        //最少两个三个头，三连带最大不能是2，不是都是三个头，不是连的
        if (length % 3 != 0 || length < 6 || cards.get(0).cardSize > 14
                || cards.get(0).sameCardNum != 3
                || cards.get(0).sameCardNum != cards.get(length - 1).sameCardNum
                || (cards.get(0).cardSize - cards.get(length - 1).cardSize + 1) * 3 != length) {
            return 0;
        }

        return cards.get(length - 1).cardSize;
    }

    //判断是不是飞机带对子，返回第一个三个头的第一张牌
    public static int isPlaneWithTwo(List<Card> cards) {
        //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
        Collections.sort(cards, Card.CompareModeCardNum);

        int length = cards.size();
        //获取最小的三个头的位置
        int minThreeIndex = (length / 5 - 1) * 3;
        //最少两个三个头，三连带最大不能是2，不是都是三个头，不是连的，因为后面的sameCardNum一定比它小
        if (length < 10 || length % 5 != 0 || cards.get(0).cardSize > 14
                || cards.get(0).sameCardNum != 3
                || cards.get(0).sameCardNum != cards.get(minThreeIndex).sameCardNum
                || cards.get(minThreeIndex + 3).sameCardNum != 2
                || (cards.get(0).cardSize - cards.get(minThreeIndex).cardSize + 1) * 5 != length) {
            return 0;
        }
        //判断带的是不是都是对子
        for (int i = length - 1, limit = length / 5 * 3; i > limit; i -= cards.get(i).sameCardNum) {
            if (cards.get(i).sameCardNum != 2) {
                return 0;
            }
        }
        return cards.get(minThreeIndex).cardSize;
    }

    //判断是不是炸弹，返回炸弹的第一张牌
    public static int isBomb(List<Card> cards) {
        if (cards.size() < 4 || cards.get(0).sameCardNum != cards.size()) {
            return 0;
        } else {
            return cards.get(0).cardSize;
        }
    }

    //判断是不是四大天王
    public static boolean isFourGhost(List<Card> cards) {
        //按牌的大小排序
        Collections.sort(cards, Card.CompareModeCardSize);
        return cards.size() == 4 && cards.get(0).cardSize == 17 && cards.get(3).cardSize == 16;
    }

    //判断出牌的种类
    public static OutCardStyle judgeCardStyle(List<Card> cards){
        //0出错，1单支，2对子，3三不带，4三带二，5连对，6顺子，7飞机不带，8飞机带两对，9炸弹，10四大天王
        OutCardStyle cardStyle = new OutCardStyle();
        if (cards == null || cards.size() == 0) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.CANT_OUT;
            return cardStyle;
        }

        //保存出的牌的长度
        cardStyle.cardLength = cards.size();
        //先根据牌的数量判断单支，对子，三个头
        switch (cards.size()) {
            case 1:
                cardStyle.outCardStyleEnum = OutCardStyleEnum.ONE;
                cardStyle.firstCardSize = cards.get(0).cardSize;
                return cardStyle;
            case 2:
                cardStyle.firstCardSize = isTwo(cards);
                if (cardStyle.firstCardSize > 0) {
                    cardStyle.outCardStyleEnum = OutCardStyleEnum.TWO;
                }
                return cardStyle;
            case 3:
                cardStyle.firstCardSize = isThree(cards);
                if (cardStyle.firstCardSize > 0) {
                    cardStyle.outCardStyleEnum = OutCardStyleEnum.THREE;
                }
                return cardStyle;
        }
        //判断三带二
        cardStyle.firstCardSize = isThreeTakeTwo(cards);
        if (cardStyle.firstCardSize > 0) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.THREE_TAKE_TWO;
            return cardStyle;
        }

        //判断连对
        cardStyle.firstCardSize = isNextTwo(cards);
        if (cardStyle.firstCardSize > 0) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.NEXT_TWO;
            return cardStyle;
        }

        //判断顺子
        cardStyle.firstCardSize = isStraight(cards);
        if (cardStyle.firstCardSize > 0) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.STRAIGHT;
            return cardStyle;
        }

        //判断飞机不带
        cardStyle.firstCardSize = isPlane(cards);
        if (cardStyle.firstCardSize > 0) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.PLANE;
            return cardStyle;
        }

        //判断飞机带两对
        cardStyle.firstCardSize = isPlaneWithTwo(cards);
        if (cardStyle.firstCardSize > 0) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.PLANE_TAKE_TWO;
            return cardStyle;
        }

        //判断炸弹
        cardStyle.firstCardSize = isBomb(cards);
        if (cardStyle.firstCardSize > 0) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.BOMB;
            return cardStyle;
        }

        //判断四大天王
        if (isFourGhost(cards)) {
            cardStyle.outCardStyleEnum = OutCardStyleEnum.FOUR_GHOST;
            return cardStyle;
        }

        //什么类型都不是
        return cardStyle;
    }
}
