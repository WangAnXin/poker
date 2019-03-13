using pokerServer.Landlord.entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.Landlord.OutCard {
    //出牌的方式
    //-1表示当前没有牌，0不能出，1单支，2对子，3三不带，4三带，5连对，6顺子，7飞机不带，8飞机带两对，9炸弹，10四大天王
    public enum OutCardStyleEnum {
        CANT_OUT,
        ONE,
        TWO,
        THREE,
        THREE_TAKE_TWO,
        NEXT_TWO,
        STRAIGHT,
        PLANE,
        PLANE_TAKE_TWO,
        BOMB,
        FOUR_GHOST
    }

    public class OutCardStyle {

        public OutCardStyleEnum outCardStyleEnum;      //出牌形式
        public int firstCardSize;       //相同牌的第一张
        public int cardlength;          //出牌的数量

        //构造函数
        public OutCardStyle(OutCardStyleEnum outCardStyleEnum, int firstCardSize, int cardlength) {
            this.outCardStyleEnum = outCardStyleEnum;
            this.firstCardSize = firstCardSize;
            this.cardlength = cardlength;
        }
        public OutCardStyle() {
            outCardStyleEnum = OutCardStyleEnum.CANT_OUT;
            firstCardSize = 0;
            cardlength = 0;
        }

        //判断是不是对子，返回对子的第一张牌
        public static int isTwo(List<Card> cards) {
            //如果两张牌不相等不是
            if (cards[0].sameCardNum == 2) {
                return cards[0].cardSize;
            } else {
                return 0;
            }
        }

        //判断是不是三不带，返回第一张牌
        public static int isThree(List<Card> cards) {
            if (cards[0].sameCardNum == 3) {
                return cards[0].cardSize;
            } else {
                return 0;
            }
        }

        //判断是不是三带二，返回三个头的第一张牌(不能按照牌的数量排序)
        public static int isThreeTakeTwo(List<Card> cards) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            cards.Sort(Card.CompareModeCardNum);

            //如果不是5张牌，返回错误
            if (cards.Count != 5 || cards[0].sameCardNum != 3 || cards[cards.Count - 1].sameCardNum != 2) {
                return 0;
            }

            return cards[0].cardSize;
        }

        //判断是不是连对，返回第一个对子的第一张牌(最小的)
        public static int isNextTwo(List<Card> cards) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            cards.Sort(Card.CompareModeCardNum);

            int length = cards.Count;
            //连对至少3对，且连对最大不能是2，不是都是对子，不是连的
            if (length < 6 || length % 2 != 0 || cards[0].cardSize > 14
                    || cards[0].sameCardNum != 2
                    || cards[0].sameCardNum != cards[length - 1].sameCardNum
                    || (cards[0].cardSize - cards[length - 1].cardSize + 1) * 2 != length) {
                return 0;
            }

            return cards[length - 1].cardSize;
        }

        //判断是不是顺子，返回顺子的第一张牌
        public static int isStraight(List<Card> cards) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            cards.Sort(Card.CompareModeCardNum);

            int length = cards.Count;
            //顺子最少5张，顺子最大不能是2，不是都是单支，不是连的
            if (length < 5 || cards[0].cardSize > 14
                    || cards[0].sameCardNum != 1
                    || cards[0].sameCardNum != cards[length - 1].sameCardNum
                    || cards[0].cardSize - cards[length - 1].cardSize + 1 != length) {
                return 0;
            }

            return cards[length - 1].cardSize;
        }

        //判断是不是飞机不带(三个头连对)，返回飞机的第一张牌
        public static int isPlane(List<Card> cards) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            cards.Sort(Card.CompareModeCardNum);

            int length = cards.Count;
            //最少两个三个头，三连带最大不能是2，不是都是三个头，不是连的
            if (length % 3 != 0 || length < 6 || cards[0].cardSize > 14
                    || cards[0].sameCardNum != 3
                    || cards[0].sameCardNum != cards[length - 1].sameCardNum
                    || (cards[0].cardSize - cards[length - 1].cardSize + 1) * 3 != length) {
                return 0;
            }

            return cards[length - 1].cardSize;
        }

        //判断是不是飞机带对子，返回第一个三个头的第一张牌
        public static int isPlaneWithTwo(List<Card> cards) {
            //按牌相同的数量排序（从大到小）再按牌的大小排序（从大到小）
            cards.Sort(Card.CompareModeCardNum);

            int length = cards.Count;
            //获取最小的三个头的位置
            int minThreeIndex = (length / 5 - 1) * 3;
            //最少两个三个头，三连带最大不能是2，不是都是三个头，不是连的，因为后面的sameCardNum一定比它小
            if (length < 10 || length % 5 != 0 || cards[0].cardSize > 14
                    || cards[0].sameCardNum != 3
                    || cards[0].sameCardNum != cards[minThreeIndex].sameCardNum
                    || cards[minThreeIndex + 3].sameCardNum != 2
                    || (cards[0].cardSize - cards[minThreeIndex].cardSize + 1) * 5 != length) {
                return 0;
            }
            //判断带的是不是都是对子
            for (int i = length - 1, limit = length / 5 * 3; i > limit; i -= cards[i].sameCardNum) {
                if (cards[i].sameCardNum != 2) {
                    return 0;
                }
            }
            return cards[minThreeIndex].cardSize;
        }

        //判断是不是炸弹，返回炸弹的第一张牌
        public static int isBomb(List<Card> cards) {
            if (cards.Count < 4 || cards[0].sameCardNum != cards.Count) {
                return 0;
            } else {
                return cards[0].cardSize;
            }
        }

        //判断是不是四大天王
        public static bool isFourGhost(List<Card> cards) {
            //按牌的大小排序
            cards.Sort(Card.CompareModeCardSize);
            if (cards.Count == 4 && cards[0].cardSize == 17 && cards[3].cardSize == 16) {
                return true;
            } else {
                return false;
            }
        }

        //判断出牌的种类
        public static OutCardStyle judgeCardStyle(List<Card> cards) {
            //0出错，1单支，2对子，3三不带，4三带二，5连对，6顺子，7飞机不带，8飞机带两对，9炸弹，10四大天王
            OutCardStyle cardstyle = new OutCardStyle();
            if (cards == null || cards.Count == 0) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.CANT_OUT;
                return cardstyle;
            }

            //保存出的牌的长度
            cardstyle.cardlength = cards.Count;
            //先根据牌的数量判断单支，对子，三个头
            switch (cards.Count) {
                case 1:
                cardstyle.outCardStyleEnum = OutCardStyleEnum.ONE;
                cardstyle.firstCardSize = cards[0].cardSize;
                return cardstyle;
                case 2:
                cardstyle.firstCardSize = isTwo(cards);
                if (cardstyle.firstCardSize > 0) {
                    cardstyle.outCardStyleEnum = OutCardStyleEnum.TWO;
                }
                return cardstyle;
                case 3:
                cardstyle.firstCardSize = isThree(cards);
                if (cardstyle.firstCardSize > 0) {
                    cardstyle.outCardStyleEnum = OutCardStyleEnum.THREE;
                }
                return cardstyle;
            }
            //判断三带二
            cardstyle.firstCardSize = isThreeTakeTwo(cards);
            if (cardstyle.firstCardSize > 0) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.THREE_TAKE_TWO;
                return cardstyle;
            }

            //判断连对
            cardstyle.firstCardSize = isNextTwo(cards);
            if (cardstyle.firstCardSize > 0) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.NEXT_TWO;
                return cardstyle;
            }

            //判断顺子
            cardstyle.firstCardSize = isStraight(cards);
            if (cardstyle.firstCardSize > 0) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.STRAIGHT;
                return cardstyle;
            }

            //判断飞机不带
            cardstyle.firstCardSize = isPlane(cards);
            if (cardstyle.firstCardSize > 0) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.PLANE;
                return cardstyle;
            }

            //判断飞机带两对
            cardstyle.firstCardSize = isPlaneWithTwo(cards);
            if (cardstyle.firstCardSize > 0) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.PLANE_TAKE_TWO;
                return cardstyle;
            }

            //判断炸弹
            cardstyle.firstCardSize = isBomb(cards);
            if (cardstyle.firstCardSize > 0) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.BOMB;
                return cardstyle;
            }

            //判断四大天王
            if (isFourGhost(cards)) {
                cardstyle.outCardStyleEnum = OutCardStyleEnum.FOUR_GHOST;
                return cardstyle;
            }

            //什么类型都不是
            return cardstyle;
        }
    }
}
