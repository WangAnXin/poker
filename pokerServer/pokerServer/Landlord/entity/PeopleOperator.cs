using pokerServer.Landlord.OutCard;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.Landlord.entity {
    public class PeopleOperator {
        private Landlord_GameMode gamemode;

        //构造操作人卡组队列的类
        public PeopleOperator(Landlord_GameMode gamemode) {
            this.gamemode = gamemode;
        }

        //更新人当前手牌中每个牌的数量(保证原来的排序)
        private void updateCardNum(People people) {
            //更新卡组中牌的数量（便于排序）
            updateCardNum(people.deck);
        }

        //更新卡牌队列中的牌的数量
        public static void updateCardNum(List<Card> cards) {
            //按牌的大小排序
            cards.Sort(Card.CompareModeCardSize);
            //改变每个牌的数量
            for (int i = 0; i < cards.Count; i++) {
                int cardnum = 1, k = i;
                //找到相似的卡
                while (i < cards.Count - 1 && cards[i].cardSize == cards[i + 1].cardSize) {
                    cardnum++;
                    i++;
                }
                //给每张卡设置cardNum
                for (; k <= i; k++) {
                    cards[k].sameCardNum = cardnum;
                }
            }
        }

        //给四个玩家发牌，剩余的八张牌放在牌堆中
        public void shuffle(People[] people, List<Card> cardPile) {
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
            for (int j = 0; j < cardPile.Count; j++) {
                people[whosland].deck.Add(cardPile[j]);
            }

            //更新地主卡牌的属性
            updateCardNum(people[whosland]);

            //改变四个人的地主状态
            people[whosland].isLandlord = true;
        }

        //判断是否能出牌     
        public bool canOutCard(List<Card> prevCard, List<Card> myCard) {
            //先更新sameCardNum
            updateCardNum(myCard);

            //返回能否出牌
            return gamemode.canOutCard(prevCard, myCard);
        }

        //减卡并计分，如果获胜返回true，如果没有返回false，并播放倍数翻倍和提示牌没有的音效
        public bool outCard(People people, List<Card> card, ref int integration) {
            //删除牌
            for (int i = 0; i < card.Count; i++) {
                for (int j = 0; j < people.deck.Count; j++) {
                    if (card[i].cardIndex == people.deck[j].cardIndex) {
                        people.deck.Remove(people.deck[j]);
                        break;
                    }
                }
            }
            //更新卡组数量
            updateCardNum(people);

            //玩家出牌的次数加一
            people.outCardNum++;

            //如果出的是炸弹且是6个头或7个头，积分翻倍
            if (card[0].cardSize == card[card.Count - 1].cardSize && card.Count >= 6 && card.Count <= 7) {
                integration *= 2;
            }
            //如果出的牌是8个头或者是四大天王，积分翻三倍
            else if (card[0].cardSize == card[card.Count - 1].cardSize && card.Count == 8
                    || OutCardStyle.isFourGhost(card)) {
                integration *= 3;
            }

            //判断牌是否出光
            if (people.deck.Count == 0) {
                people.isWin = true;
                return true;
            }
            return false;
        }

        //机器人（托管）出卡
        public List<Card> AIOutCard(People[] people, int landlord, int whoOut, int nowPerson, List<Card> prevCard) {
            List<Card>[] TCard = new List<Card>[4];
            for (int j = 0; j < 4; j++) {
                TCard[j] = Card.CopyListCard(people[j].deck);
            }
            List<Card> resCards = ComputerRemindCard.AIOutCard(TCard, landlord, whoOut, nowPerson, prevCard);
            //更新出牌的卡牌数量后返回
            updateCardNum(resCards);
            return resCards;
        }

        //结算分数  
        public void settleScore(People[] peoples, int integration) {
            //判断地主和农民谁获胜（false是地主赢，true是农民赢）
            bool isFarmerWin = false;

            //找到出完牌的那个人，判断他是不是地主
            int peopleNum = peoples.Length;
            foreach (People people in peoples) {
                if (people.isWin == true) {
                    isFarmerWin = !people.isLandlord;
                    break;
                }
            }

            //地主获胜(如果农民一张牌没出，则积分翻倍）
            if (isFarmerWin == false) {
                bool isFarmerOutCard = false;
                foreach (People people in peoples) {
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
                bool isLandlordOutCardNumLessTwo = false;
                foreach (People people in peoples) {
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
                } else {
                    peoples[i].integration += integration;
                }
            }
        }
    }
}
