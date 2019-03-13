package wanganxin.com.poker.GameLogic.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Card {
    public int cardSize;       //牌的大小
    public int color;     //牌的花色
    public int cardIndex;       //对应标签编号
    public int sameCardNum;     //相同牌的个数

    //按牌的大小从大到小排，再按牌的花色从小到大排
    public static Comparator<Card> CompareModeCardSize = new Comparator<Card>(){
        @Override
        public int compare(Card c1, Card c2) {
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
    };

    //按牌的数量从大到小牌，再按牌的大小从大到小排，再按牌的花色从小到大排
    public static Comparator<Card> CompareModeCardNum = new Comparator<Card>(){
        //按牌的大小从大到小排，再按牌的花色排
        @Override
        public int compare(Card c1, Card c2) {
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
    };

    //按牌的数量从大到小牌，再按牌的大小从小到大排，再按牌的花色从小到大排
    public static Comparator<Card> CompareModeDisplayCard = new Comparator<Card>(){
        //按牌的大小从大到小排，再按牌的花色排
        @Override
        public int compare(Card c1, Card c2) {
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
    };

    public Card(int cardSize, int color, int cardIndex, int sameCardNum)
    {
        this.cardSize = cardSize;
        this.color = color;
        this.cardIndex = cardIndex;
        this.sameCardNum = sameCardNum;
    }

    public Card() { }

    //重写equals方法，否则会调用父类方法只删除同一对象的元素
    @Override
    public boolean equals(Object obj) {
        Card card = (Card)obj;
        return this.cardIndex == card.cardIndex;
                //&& this.sameCardNum == card.sameCardNum
                //&& this.cardSize == card.cardSize
                //&& this.color == card.color;
    }

    //重载hashCode方法。否则用Card对象作为Key放到HashMap中时，还会出现问题。
    @Override
    public int hashCode() {
        return cardIndex;
    }

    //复制一个新的card链表
    public static List<Card> CopyListCard(List<Card> people) {
        if (people == null) {
            return null;
        }

        List<Card> TCard = new ArrayList<Card>();
        for (int i = 0; i < people.size(); i++) {
            Card tcard = new Card(people.get(i).cardSize, people.get(i).color, people.get(i).cardIndex, people.get(i).sameCardNum);
            TCard.add(tcard);
        }
        return TCard;
    }
}
