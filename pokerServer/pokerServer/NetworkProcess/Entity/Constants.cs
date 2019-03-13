using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.NetworkProcess.Entity {
    public class Constants {
        public static int DIDPLAY_DEAL_CARD = 4000;     //等待发牌动画的时间
        public static int RTT = 3000;       //网络来回所需时间
        public static int WAIT_PEOPLE_CALL_SCORE = 12000 + RTT;   //等待叫分的时间
        public static int WAIT_PEOPLE_OUT_CARD = 36000 + RTT;        //等待出牌的时间

        public static int WAIT_AUTO_CALLSCORE_OUTCARD = 2000;       //等待自动出牌或叫分的时间

        public static int HEART_BEAT_TIME = 10000 + RTT;      //心跳包未发送的超时时间
    }
}
