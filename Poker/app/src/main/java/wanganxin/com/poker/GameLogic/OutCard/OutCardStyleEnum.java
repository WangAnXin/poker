package wanganxin.com.poker.GameLogic.OutCard;

//android枚举影响性能
//出牌的方式
//0不能出，1单支，2对子，3三不带
//4三带，5连对，6顺子，7飞机不带
//8飞机带两对，9炸弹，10四大天王
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
