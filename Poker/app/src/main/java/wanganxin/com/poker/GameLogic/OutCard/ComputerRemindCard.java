package wanganxin.com.poker.GameLogic.OutCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.entity.People;
import wanganxin.com.poker.GameLogic.Operator.PeopleOperator;

import static wanganxin.com.poker.GameLogic.entity.Card.CompareModeCardSize;

public class ComputerRemindCard {
    //玩家任意出牌的游戏提示，记录上次找的牌
    private static List<Card> remindCard(List<Card> deck, List<Card> prevCard) {
        //判断上家的卡组样式
        OutCardStyle preOutCardStyle = OutCardStyle.judgeCardStyle(prevCard);
        //如果想出什么牌就出什么牌
        if (preOutCardStyle.outCardStyleEnum == OutCardStyleEnum.CANT_OUT) {
            return Robort_Free_Remind(deck);
        }
        //要不住，提示炸弹
        else {
            return CrushPreCard.crushPreCard(deck, preOutCardStyle, true, false);
        }
    }

    //电脑想出什么牌就出什么牌
    private static List<Card> Robort_Free_Remind(List<Card> deck) {
        //找同类型自己能出的最小的牌（不能随便拆）
        People people = new People();
        people.deck = deck;
        people.htStyle = OutCardStyleEnum.CANT_OUT;
        //利用玩家的提示
        PlayerRemindCard.peopleFirstHint(people, false);
        List<Card> resCards = people.htCards;

        //因为playerRemind提示后htStyle会到下一个，所以按照下一个处理
        switch (people.htStyle) {
            //如果提示出三带二，三不带，对子
            case THREE:
                //如果带了对二及以上，只出三个头
                if (resCards.get(resCards.size() - 1).cardSize >= 15 && afterThisOut_CanOutAllCard(deck, resCards) == false) {
                    resCards.clear();
                    //必须出牌，肯定还剩牌，找对子和单支
                    people.htStyle = OutCardStyleEnum.THREE;
                    PlayerRemindCard.peopleFirstHint(people, false);
                    resCards = people.htCards;
                }
                break;
            case TWO:
            case ONE:
                //不出3个2，不带对2，对小鬼，对老鬼//除非能直接出完或者出完这些牌可以一次性出完其他牌，否则不允许出
                if (resCards.get(0).cardSize >= 15 && afterThisOut_CanOutAllCard(deck, resCards) == false) {
                    resCards.clear();
                    //必须出牌，肯定还剩牌，找对子和单支
                    people.htStyle = OutCardStyleEnum.TWO;
                    PlayerRemindCard.peopleFirstHint(people, false);
                    resCards = people.htCards;
                }
                break;
        }

        return resCards;
    }

    //记录农民提示出牌情况，0代表当前出牌的人，1代表下一个
    private static List<Card> [] nextRemindCard = new ArrayList[3];
    //机器人智能出牌，包含各种情况
    public static List<Card> AIOutCard(List<Card>[] peoplesCard, int whosland, int whoOut, int nowPerson, List<Card> preCard) {
        //landlord：谁是地主    nowPerson：谁要出牌     whoOut：上家出牌的是谁
        List<Card> curOutCards;

        //如果是地主出牌
        if (nowPerson == whosland){
            //不出3个2，对小鬼，对老鬼，对2 //打单支可拆2，A，小鬼，老鬼
            curOutCards = remindCard(peoplesCard[nowPerson], preCard);
        }
        //如果是农民
        else {
            curOutCards = farmerAIOutCard(peoplesCard, whosland, whoOut, nowPerson, preCard);
        }
        return curOutCards;
    }

    //农民出牌的策略
    //  1.自己出牌的情况
    //      1.1.如果自己能出完牌，自己打
    //      1.2.如果自己要不到上面的牌，不出
    //  2.如果后面有农民能一次性出完牌
    //      2.1.如果自己必须出牌
    //          2.1.1.如果能放他走(则按下列出)
    //              2.1.1.1 后面是炸弹自己随便出
    //              2.1.1.2 后面是单支或对子（自己任意拆）
    //              2.1.1.3 其他类型则出同类型最小的牌
    //          2.1.2.如果不能放他走(按常规的出)
    //      2.2.如果不是必须出牌，则不出牌，放它走
    //  3.如果后面没有农民能一次性出完牌
    //      3.1.上次出牌的是农民
    //          3.1.1.当前出牌农民出完牌后可以一次走，就不打
    //          3.1.2.不用2，小王，大王打农民(除非打完可以一次走)
    //          3.1.3.不用炸弹打农民
    //      3.2.上次出牌的是地主
    //          如果是地主的上家，单牌出J守门，不让地主出
    //      3.3.如果后面有农民可以要到这次牌，则不出炸弹
    private static List<Card> farmerAIOutCard(List<Card>[] peoplesCard, int whosland, int whoOut, int nowPerson, List<Card> preCard) {
        //landlord：谁是地主    nowPerson：谁要出牌     whoOut：上家出牌的是谁
        //先判断自己能不能出完，在判断后面的农民能不能出完
        boolean canNextPeopleOutAllCard = false;
        int i, j;
        //循环后面的农民，遇到地主就停
        for (i = nowPerson, j = 0; i != whosland; i = (i + 1) % 4, j++) {
            //将当前出牌者和地主前的农民要出的牌保存起来
            nextRemindCard[j] = remindCard(peoplesCard[i], preCard);
            //如果有人能出完（跳出）
            if (nextRemindCard[j].size() == peoplesCard[i].size())
                break;
        }

        List<Card> curOutCards = new ArrayList<>();
        //如果自己能出完或者自己什么牌都出不了（或者自己出完这次牌后，下一次能一次出完）
        if (i == nowPerson || nextRemindCard[0].size() == 0
                || afterThisOut_CanOutAllCard(peoplesCard[nowPerson], nextRemindCard[0])) {
            //出完自己的牌
            curOutCards = nextRemindCard[0];
        }
        //如果后面有农民能出完
        else if (i != whosland) {
            curOutCards = letFarmerGo(peoplesCard, whoOut, nowPerson, j);
        }
        //如果没有人能出完
        else {
            //农民自由出牌（必须得出）
            if (whoOut == nowPerson) {
                //放人走 > 卡位
                curOutCards = farmerFreeOutCard(peoplesCard, nowPerson);
            }
            //农民打农民或农民打地主
            else {
                //打农民不炸 > 单牌守门 > 后面的能不能要，能要不出炸弹
                if (whoOut != whosland) {
                    //如果打农民时，当前出牌农民出完牌后可以一次走，就不打
                    boolean canOutAllCard = afterThisOut_CanOutAllCard(peoplesCard[whoOut], preCard);
                    if (canOutAllCard == true) {
                        canNextPeopleOutAllCard = true;
                    }
                    //如果用2，大小王打其他农民,前面已经判断，不能一次性出完
                    else if (nextRemindCard[0].get(0).cardSize >= 15) {
                        canNextPeopleOutAllCard = true;
                    }
                }

                //如果没有其它玩家可以出完牌（该怎么打怎么打）
                if (canNextPeopleOutAllCard != true) {
                    curOutCards = farmerCrushCard(peoplesCard, whosland, whoOut, nowPerson, preCard);
                }
            }
        }

        return curOutCards;
    }

    //让后面的农民跑
    private static List<Card> letFarmerGo(List<Card>[] peoplesCard, int whoOut, int nowPerson, int whoCanOut) {
        List<Card> curOutCards = nextRemindCard[0];
        //如果是自己出牌（必须要出），想办法放农民跑
        if (whoOut == nowPerson) {
            OutCardStyle nextCardStyle = OutCardStyle.judgeCardStyle(nextRemindCard[whoCanOut]);

            //如果后面的农民出的是炸弹或者是四大天王，自己就随便出
            if (nextCardStyle.outCardStyleEnum == OutCardStyleEnum.BOMB
                    || nextCardStyle.outCardStyleEnum == OutCardStyleEnum.FOUR_GHOST) {
            }
            //如果是单支或者是对子，拆任意牌，放农民走
            else if (nextCardStyle.outCardStyleEnum == OutCardStyleEnum.ONE
                    || nextCardStyle.outCardStyleEnum == OutCardStyleEnum.TWO) {
                List<Card> tempOutCards = new ArrayList<Card>();
                switch (nextCardStyle.outCardStyleEnum) {
                    case ONE:
                        tempOutCards = CrushPreCard.findMinOneTwo(peoplesCard[nowPerson], 1);
                        break;
                    case TWO:
                        tempOutCards = CrushPreCard.findMinOneTwo(peoplesCard[nowPerson], 2);
                        break;
                }
                if (tempOutCards.size() > 0 && tempOutCards.get(0).cardSize < nextCardStyle.firstCardSize) {
                    curOutCards = tempOutCards;
                }
            }
            //如果是其他类型的牌
            else {
                //找同类型自己能出的最小的牌（可以拆）（利用玩家的提示）
                People people = new People();
                people.deck = peoplesCard[nowPerson];
                people.htStyle = nextCardStyle.outCardStyleEnum;
                PlayerRemindCard.peopleFirstHint(people, true);

                //判断当前出了以后，后面的农民能不能跑
                OutCardStyle myCardOutStyle = OutCardStyle.judgeCardStyle(people.htCards);
                //不能出证明放不了，就随便出
                if (myCardOutStyle.outCardStyleEnum == nextCardStyle.outCardStyleEnum
                        && myCardOutStyle.firstCardSize < nextCardStyle.firstCardSize) {
                    curOutCards = people.htCards;
                }
            }
        }
        //如果可以不出，则不出
        else {
            curOutCards.clear();
        }
        return curOutCards;
    }

    //农民自由出牌
    private static List<Card> farmerFreeOutCard(List<Card>[] people, int nowPerson) {
        return Robort_Free_Remind(people[nowPerson]);
    }

    //农民打别人的牌//打农民不炸 > 单牌守门 > 后面的能不能要，能要不出炸弹
    private static List<Card> farmerCrushCard(List<Card>[] people, int whosland, int whoOut, int nowPerson, List<Card> prevCard) {
        //landlord：谁是地主    nowPerson：谁要出牌     whoOut：上家出牌的是谁
        //上家出牌的是自己人而且我只能出炸弹，则不出牌
        if (OutCardStyle.isBomb(nextRemindCard[0]) != 0 && whoOut != whosland)
            return new ArrayList<Card>();   //返回空值

        //如果下家是地主，且上家出的牌是单牌，则需要守门
        if ((nowPerson + 1) % 4 == whosland) {
            if (prevCard.size() == 1) {
                return robortSingleDefend(people[nowPerson], prevCard);
            }
        }

        //判断自己是否一定要出炸弹
        int i, j;
        for (i = nowPerson, j = 0; i != whosland; i = (i + 1) % 4, j++) {
            //如果该家出的是普通牌
            if (OutCardStyle.isBomb(nextRemindCard[j]) == 0 && nextRemindCard[j].size() != 0)
                break;
        }
        //如果我家出的是普通牌，或者没有一家出的不是普通牌
        if (i == nowPerson || i == whosland) {
            return nextRemindCard[0];
        }
        //后面有农民可以要住，我就不用炸
        else {
            return new ArrayList<Card>();   //返回空值
        }
    }

    //如果下家是地主，且上家出的牌是单牌，则需要守门
    private static List<Card> robortSingleDefend(List<Card> deck, List<Card> prevCard) {
        List<Card> resCards = new ArrayList<Card>();

        //按照牌的大小排序
        Collections.sort(deck, CompareModeCardSize);
        int i;
        //如果没找到能出的单支（出J以上，不拆炸弹，大于当前的牌）
        for (i = deck.size() - 1; i >= 0; i--) {
            if (deck.get(i).cardSize < 11 || deck.get(i).sameCardNum > 3 || deck.get(i).cardSize <= prevCard.get(0).cardSize)
                continue;
            resCards.add(deck.get(i));
            return resCards;
        }
        //如果封不住，出原来的牌
        return nextRemindCard[0];
    }

    //判断出完这次牌后，下一次能不能一次出完
    private static boolean afterThisOut_CanOutAllCard(List<Card> people, List<Card> thisCard) {
        //如果当前已经可以打完了，返回true
        if (people.size() == thisCard.size()) {
            return true;
        }
        //保存当前的卡组
        List<Card> tDeck = Card.CopyListCard(people);
        //一出当前要出的牌
        for (int i = 0; i < thisCard.size(); i++)
            tDeck.remove(thisCard.get(i));

        //更新卡组的数量
        PeopleOperator.updateCardNum(tDeck);

        //判断下一次出牌
        //找同类型自己能出的最小的牌（不能随便拆）
        People p = new People();
        p.deck = tDeck;
        p.htStyle = OutCardStyleEnum.CANT_OUT;
        //利用玩家的提示
        PlayerRemindCard.peopleFirstHint(p, false);

        //如果该玩家提示的牌可全部出完
        return p.htCards.size() == tDeck.size();
    }
}
