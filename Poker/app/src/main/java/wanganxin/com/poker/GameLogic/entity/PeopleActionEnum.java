package wanganxin.com.poker.GameLogic.entity;

/**
 * Created by Administrator on 2017/4/3.
 * 人卡组
 */

//玩家的动作（开始，一分，二分，三分，不叫，不出）
public enum PeopleActionEnum {
    //准备
    PREAPARE,

    //一分
    ONE_SCORE,

    //二分
    TWO_SCORE,

    //三分
    THREE_SCORE,

    //不叫
    NO_SCORE,

    //不出
    REFUSED,
}
