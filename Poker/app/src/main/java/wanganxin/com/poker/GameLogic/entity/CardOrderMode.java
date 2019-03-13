package wanganxin.com.poker.GameLogic.entity;

public enum CardOrderMode {
    //按牌的大小从大到小排，再按牌的花色从小到大排
    CARD_SIZE,

    //按牌的数量从大到小牌，再按牌的大小从大到小排，再按牌的花色从小到大排
    CARD_NUM,

    //按牌的数量从大到小牌，再按牌的大小从小到大排，再按牌的花色从小到大排
    CARD_DISPLAY,
}
