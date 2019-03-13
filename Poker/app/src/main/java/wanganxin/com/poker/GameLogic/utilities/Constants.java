package wanganxin.com.poker.GameLogic.utilities;

/**
 * 常量类
 * 大小单位都是dp，时间为毫秒
 */

public class Constants
{
    public static String serverIp = "119.23.242.175";       //服务器的ip地址
    public static int serverPort = 80;                //服务器的端口号
    public static int HEART_BEAT_TIMESPAN = 5000;       //每隔多久发送心跳包
    public static int HEART_BEAT_TIMEOUT = 10000;       //多长时间没接收到心跳包，认为与服务器断开连接
    public static int CONNECTED_TIMEOUT = 6000;       //与服务器连接超时的时间

    //扑克牌宽/长比约为0.7065，原本是65-92
    public static int CARD_WIDTH = 65;
    public static int CARD_HEIGHT = 92;
    public static int CARD_INTERVAL = 14;   //大卡之间的距离

    public static int CARD_SMALL_WIDTH = 38;
    public static int CARD_SMALL_HEIGHT = 51;
    public static int CARD_SMALL_INTERVAL = 8;  //小卡之间的距离
    public static int CARD_VERTICAL_DIVERSION = 16;   //小卡上下之间的距离

    public static int UNDERCARD_BOTTOM = 5;    //底牌距离底牌框的距离
    public static int UNDERCARD_LEFT = 5;    //底牌距离底牌框的距离
    public static int CARD_ORIGN_BOTTOM = 2;    //下家卡牌原本距离下方的距离
    public static int CARD_ADD_BOTTOM = 15;     //下家的卡牌点击后距离下方的距离
    public static int CARD_AADD_BOTTOM = 20;     //下家的卡牌点击后距离下方的距离
    public static int OUTCARD_HORIZENTAL_BOTTOM = 10;   //出牌时与人物之间的间距
    public static int ElEVATION_DP = 5;   //setElevation上浮DP

    public static int OUTCARD_VERTICAL_BOTTOM = 2;   //出牌时与人物之间的间距

    public static Boolean soundEffectMusicFlag = true;  //是否播放音效
    public static int RandomCallLandlord = 5;     //多大可能性叫地主
    public static Boolean IsAutoTest = false;   //是否为自动测试状态（网络版断线重连恢复可用）

    public static long DEALCARD_DURATION_TIME = 120;    //发牌时间
    public static long CLICKPOKE_DURATION_TIME = 300;    //点击牌上移时间
    public static long UNDERPOKE_DURATION_TIME = 400;  //底牌翻转时间
    public static long PEOPLE_DURATION_TIME = 400;  //人物和姓名翻转时间
    public static long POKE_HORIZENTAL_DURATION = 400;  //牌水平移动的时间
    //public static long POKE_OUTCARD_DURATION = 300;  //出牌的时间
    public static long WAIT_FOR_COUNT = 100;  //等待计算坐标的事件
    public static long LIGHT_DURATION_TIME = 300;  //淡入淡出的时间
    public static long COMPUTER_THINK_TIME = 1500;  //电脑思考时间
    public static long BREAKRULE_TIME = 1500; //规则有问题时间
    public static long DIALOG_DURATION_TIME = 1200;  //淡入淡出的时间

    public static int excursion_num = 16;   //发牌动画(淡入平移动画)
    public static float beishu = 15; //从百分之多少时，底牌开始翻转

    public static final int CALL_SCORE_CLOCK = 1;     //叫分计时的闹钟
    public static final int OUT_CARD_CLOCK = 2;       //出牌计时的闹钟

    public static long WAIT_OTHER_CALL_SCORE = 10000;   //等待别人的叫分时间（15s）
    public static long WAIT_OTHER_OUT_CARD = 30000;   //等待别人的出牌时间（30s）

    public static long WAIT_INTERVAL = 300;     //每间隔多长时间接受一次网络的消息

    public static int MIN_IMAGE = 0;         //人物图片最小的编号
    public static int MAX_IMAGE = 5;         //人物图片最打的编号

    public static int MAX_RE_CALLSCROE_NUM = 3;     //最多可以重新叫分多少局
}
