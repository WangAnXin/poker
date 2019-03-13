using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.Landlord.entity {

    public enum CardOrderMode {
        //按牌的大小从大到小排，再按牌的花色从小到大排
        CARD_SIZE,

        //按牌的数量从大到小牌，再按牌的大小从大到小排，再按牌的花色从小到大排
        CARD_NUM,

        //按牌的数量从大到小牌，再按牌的大小从小到大排，再按牌的花色从小到大排
        CARD_DISPLAY,
    }

    public class Card {
        public int cardSize;       //牌的大小
        public int color;     //牌的花色
        public int cardIndex;       //对应标签编号
        public int sameCardNum;     //相同牌的个数

        //按牌的大小从大到小排，再按牌的花色从小到大排
        public static int CompareModeCardSize(Card c1, Card c2) {
            if (c1.cardSize == c2.cardSize) {
                if (c1.color == c2.color) {
                    return c1.cardIndex > c2.cardIndex ? 1 : -1;
                } else {
                    return c1.color > c2.color ? 1 : -1;
                }
            } else {
                return c1.cardSize < c2.cardSize ? 1 : -1;
            }
        }

        //按牌的数量从大到小牌，再按牌的大小从大到小排，再按牌的花色从小到大排
        public static int CompareModeCardNum(Card c1, Card c2) {
            if (c1.sameCardNum == c2.sameCardNum) {
                if (c1.cardSize == c2.cardSize) {
                    if (c1.color == c2.color) {
                        return c1.cardIndex > c2.cardIndex ? 1 : -1;
                    } else {
                        return c1.color > c2.color ? 1 : -1;
                    }
                } else {
                    return c1.cardSize < c2.cardSize ? 1 : -1;
                }
            } else {
                return c1.sameCardNum < c2.sameCardNum ? 1 : -1;
            }
        }

        //按牌的数量从大到小牌，再按牌的大小从小到大排，再按牌的花色从小到大排
        public static int CompareModeDisplayCard(Card c1, Card c2) {
            if (c1.sameCardNum == c2.sameCardNum) {
                if (c1.cardSize == c2.cardSize) {
                    if (c1.color == c2.color) {
                        return c1.cardIndex > c2.cardIndex ? 1 : -1;
                    } else {
                        return c1.color > c2.color ? 1 : -1;
                    }
                } else {
                    return c1.cardSize > c2.cardSize ? 1 : -1;
                }
            } else {
                return c1.sameCardNum < c2.sameCardNum ? 1 : -1;
            }
        }

        public Card(int cardSize, int color, int cardIndex, int sameCardNum) {
            this.cardSize = cardSize;
            this.color = color;
            this.cardIndex = cardIndex;
            this.sameCardNum = sameCardNum;
        }

        public Card() { }

        //重写equals方法，否则会调用父类方法只删除同一对象的元素
        public override bool Equals(object obj) {
            Card card = (Card)obj;
            return this.cardIndex == card.cardIndex;
        }

        //重载hashCode方法。否则用Card对象作为Key放到HashMap中时，还会出现问题。
        public override int GetHashCode() {
            return cardIndex;
        }


        //复制一个新的card链表
        public static List<Card> CopyListCard(List<Card> people) {
            if (people == null) {
                return null;
            }

            List<Card> TCard = new List<Card>();
            for (int i = 0; i < people.Count; i++) {
                Card tcard = new Card(people[i].cardSize, people[i].color, people[i].cardIndex, people[i].sameCardNum);
                TCard.Add(tcard);
            }
            return TCard;
        }

        public static implicit operator Card(List<Card> v) {
            throw new NotImplementedException();
        }
    }
}
