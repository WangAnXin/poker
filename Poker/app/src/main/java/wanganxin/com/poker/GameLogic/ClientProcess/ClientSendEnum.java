package wanganxin.com.poker.GameLogic.ClientProcess;

//游戏进程的状态
public enum ClientSendEnum {
    NONE,

    //发送登录消息(用户名，密码)
    LOGIN_SEND_PROCESS,

    //游戏大厅匹配阶段
    MATCH_PREPARE_PROCESS,

    //房间准备阶段(发送准备信息)
    PREPARE_GAME_PROCESS,

    //发送叫分信息
    CALL_SCORE_PROCESS,

    //发送出牌的信息
    OUT_CARD_PROCESS,

    NUM
}
