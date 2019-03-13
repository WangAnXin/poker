package wanganxin.com.poker.GameLogic.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wanganxin.com.poker.GameLogic.OutCard.ComputerRemindCard;
import wanganxin.com.poker.GameLogic.OutCard.OutCardStyle;
import wanganxin.com.poker.GameLogic.OutCard.PlayerRemindCard;
import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.entity.CardOrderMode;
import wanganxin.com.poker.GameLogic.entity.People;

/**
 * Created by Administrator on 2017/4/3.
 * 直接操作四人斗地主的业务类
 */

public class PeopleOperator {
    private LandLord_GameMode gamemode;

    //构造操作人卡组队列的类
    public PeopleOperator(LandLord_GameMode gamemode) {
        this.gamemode = gamemode;
    }

    //更新人当前手牌中每个牌的数量(保证原来的排序)
    private void updateCardNum(People people) {
        //更新卡组中牌的数量（便于排序）
        updateCardNum(people.deck);

        //如果当前是按个数排则重新排序
        if (people.orderMode == CardOrderMode.CARD_NUM) {
            gamemode.changeOrder(people.deck, CardOrderMode.CARD_NUM);
        }
    }

    //更新卡牌队列中的牌的数量
    public static void updateCardNum(List<Card> cards) {
        //按牌的大小排序
        Collections.sort(cards, Card.CompareModeCardSize);
        //改变每个牌的数量
        for (int i = 0; i < cards.size(); i++) {
            int cardnum = 1, k = i;
            //找到相似的卡
            while (i < cards.size() - 1 && cards.get(i).cardSize == cards.get(i + 1).cardSize) {
                cardnum++;
                i++;
            }
            //给每张卡设置cardNum
            for (; k <= i; k++) {
                cards.get(k).sameCardNum = cardnum;
            }
        }
    }

    //给四个玩家发牌，剩余的八张牌放在牌堆中
    public void shuffle(People[] people, List<Card> cardPile) {
        //发牌前先把牌清空
        for (int i = 0; i < people.length; i++) {
            people[i].deck.clear();
        }
        //给玩家发牌，并留8张牌作为底牌
        gamemode.Shuffle(people, cardPile);

        //更新玩家卡组中卡的属性（数目）
        for (int i = 0; i < 4; i++) {
            updateCardNum(people[i]);
        }
    }

    //给地主8张牌，并且更新地主状态
    public void dealCardToLandlord(People[] people, List<Card> cardPile, int whosland) {
        //给地主发牌
        for (int j = 0; j < cardPile.size(); j++) {
            people[whosland].deck.add(cardPile.get(j));
        }

        //更新地主卡牌的属性
        updateCardNum(people[whosland]);

        //改变四个人的地主状态
        people[whosland].isLandlord = true;
    }

    //判断是否能出牌     
    public boolean canOutCard(List<Card> prevCard, List<Card> myCard) {
        //先更新sameCardNum
        updateCardNum(myCard);

        //返回能否出牌
        return gamemode.canOutCard(prevCard, myCard);
    }

    //减卡并计分，如果获胜返回true，如果没有返回false，并播放倍数翻倍和提示牌没有的音效
    public boolean outCard(People people, List<Card> card, int[] integration) {
        //删除牌
        for (int i = 0; i < card.size(); i++) {
            for (int j = 0; j < people.deck.size(); j++) {
                if (card.get(i).cardIndex == people.deck.get(j).cardIndex) {
                    people.deck.remove(people.deck.get(j));
                    break;
                }
            }
        }
        //更新卡组数量
        updateCardNum(people);

        //玩家出牌的次数加一
        people.outCardNum++;

        //如果出的是炸弹且是6个头或7个头，积分翻倍
        if (card.get(0).cardSize == card.get(card.size() - 1).cardSize && card.size() >= 6 && card.size() <= 7) {
            integration[0] *= 2;
        }
        //如果出的牌是8个头或者是四大天王，积分翻三倍
        else if (card.get(0).cardSize == card.get(card.size() - 1).cardSize && card.size() == 8
                || OutCardStyle.isFourGhost(card)) {
            integration[0] *= 3;
        }

        //判断牌是否出光
        if (people.deck.size() == 0) {
            people.isWin = true;
            return true;
        }
        return false;
    }

    //玩家提示出卡（
    public List<Card> remind(People people, List<Card> prevCard) {
        if (people.isRemind == false) {
            people.htCards = prevCard;
            if (prevCard == null) {
                people.isOutByMy = true;
            } else {
                people.isOutByMy = false;
            }
        }

        //如果是别人出牌，就可以出炸弹
        if (people.isOutByMy == false) {
            PlayerRemindCard.remindCard(people, people.htCards, true);
        }
        //如果是自己想怎么出怎么出
        else {
            PlayerRemindCard.remindCard(people, people.htCards, false);
            //如果没找到，递归调用，因为htCardStyle已经改变了
            if (people.htCards.size() == 0) {
                remind(people, prevCard);
                return people.htCards;
            }
        }

        List<Card> resCards = new ArrayList<>();
        //如果能提示牌，记录有牌可出，并将其返回
        if (people.htCards.size() > 0) {
            people.canOutCard = true;
            resCards = people.htCards;
        }
        //如果当前不能出牌，则重新提示
        else {
            people.htCards = prevCard;
        }

        //已经提醒过了
        people.isRemind = true;
        //改回人的卡组排序
        gamemode.changeOrder(people.deck, people.orderMode);

        return resCards;
    }

    //机器人（托管）出卡
    public List<Card> AIOutCard(People[] people, int landlord, int whoOut, int nowPerson, List<Card> prevCard) {
        List<Card> []TCard = new ArrayList[4];
        for (int j = 0; j < 4; j++) {
            TCard[j] = Card.CopyListCard(people[j].deck);
        }
        List<Card> resCards = ComputerRemindCard.AIOutCard(TCard, landlord, whoOut, nowPerson, prevCard);
        //更新出牌的卡牌数量后返回
        updateCardNum(resCards);
        return resCards;
    }

    //结算分数  
    public void settleScore(People[] peoples, Integer integration) {
        //判断地主和农民谁获胜（false是地主赢，true是农民赢）
        boolean isFarmerWin = false;

        //找到出完牌的那个人，判断他是不是地主
        int peopleNum = peoples.length;
        for (People people : peoples) {
            if (people.isWin == true) {
                isFarmerWin = !people.isLandlord;
                break;
            }
        }

        //地主获胜(如果农民一张牌没出，则积分翻倍）
        if (isFarmerWin == false) {
            boolean isFarmerOutCard = false;
            for (People people : peoples) {
                //是农民且出过一次牌
                if (people.isLandlord == false && people.outCardNum > 0) {
                    isFarmerOutCard = true;
                    break;
                }
            }
            //没有一个农民出过牌，底分翻倍
            if (isFarmerOutCard == false) {
                integration *= 2;
            }
        }
        //农民获胜(地主只出过一手牌或不出牌，底分翻倍)
        else {
            boolean isLandlordOutCardNumLessTwo = false;
            for (People people : peoples) {
                //地主只出过一手牌或不出牌
                if (people.isLandlord == true && people.outCardNum <= 1) {
                    isLandlordOutCardNumLessTwo = true;
                    break;
                }
            }
            //地主只出过一手牌或不出牌，底分翻倍
            if (isLandlordOutCardNumLessTwo == true) {
                integration *= 2;
            }
        }

        //每家结算分数
        //农民获胜，地主减分，农民加分，地主获胜则相反
        if (isFarmerWin == false) {
            integration *= -1;
        }
        for (int i = 0; i < peopleNum; i++) {
            //地主获胜
            if (peoples[i].isLandlord == true) {
                peoples[i].integration -= 3 * integration;
            }
            else {
                peoples[i].integration += integration;
            }
        }
    }

    //改变牌序（按钮）（改变成相反的排序）
    public void changeOrder(People people) {
        //将玩家当前排序模式转变（按牌大小排，按牌的数量排）
        switch (people.orderMode) {
            case CARD_NUM:
                people.orderMode = CardOrderMode.CARD_SIZE;
                break;
            case CARD_SIZE:
                people.orderMode = CardOrderMode.CARD_NUM;
                break;
        }
        //改变玩家的排序显示
        gamemode.changeOrder(people.deck, people.orderMode);
    }
}
