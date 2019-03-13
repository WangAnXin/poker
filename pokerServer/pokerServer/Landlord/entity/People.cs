using pokerServer.Landlord.OutCard;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.Landlord.entity {
    //人手中牌的类
    public class People {
        public List<Card> deck = new List<Card>();    //玩家的卡组

        public int integration;            //积分

        public bool isLandlord;         //是否是地主
        public bool isWin;              //是否出完牌了
        public int outCardNum;          //出过牌的次数
        public List<Card> htCards = new List<Card>();       //提示能出的牌
        public OutCardStyleEnum htStyle;

        //初始化
        public People() {
            integration = 0;
            Init_AfterTurn();
        }

        //一局以后清空的函数
        public void Init_AfterTurn() {
            htStyle = OutCardStyleEnum.CANT_OUT;
            htCards.Clear();
            isLandlord = false;          //当前为农民
            outCardNum = 0;           //初始化没出过牌
            isWin = false;               //初始化为输
            this.deck.Clear();          //清空当前卡组
        }
    }
}
