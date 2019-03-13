package wanganxin.com.poker.GameLogic.GameProcess;

//游戏进程的状态
public enum GameProcessEnum {
    NONE,

    //准备游戏开始阶段
    PREPARE_GAME_PROCESS,

    //发牌阶段
    DEAL_CARD_PROCESS,

    //叫分（叫地主）阶段
    CALL_SCORE_PROCESS,

    //发地主牌阶段
    DEAL_LANDLORD_CARD_PROCESS,

    //出牌（打牌）阶段
    OUT_CARD_PROCESS,

    //积分结算阶段
    SCORE_SETTLE_PROCESS,

    //游戏结束，清空画面
    End_GAME_PROCESS,

    NUM
};
