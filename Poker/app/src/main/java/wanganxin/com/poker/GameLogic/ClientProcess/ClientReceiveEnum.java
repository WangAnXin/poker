package wanganxin.com.poker.GameLogic.ClientProcess;

//客户端接受服务器端的进程
public enum ClientReceiveEnum {
    NONE,

    //获取登录结果(成功，用户名不存在，密码错误)
    LOGIN_RECEIVE_PROCESS,

    //接受匹配结果阶段
    MATCH_RECEIVE_PROCESS,

    //房间准备阶段(等待四人准备)
    PREPARE_RECEIVE_PROCESS,

    //接受卡组
    CARDS_RECEIVE_PROCESS,

    //接受叫分的信息
    CALL_SCORE_RECEIVE_PROCESS,

    //接受出牌的信息
    OUT_CARD_RECEIVE_PROCESS,

    NUM
};
