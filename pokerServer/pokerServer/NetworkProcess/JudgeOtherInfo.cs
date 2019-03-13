using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.NetworkProcess {
    class JudgeOtherInfo {
        //判断是否为玩家退出
        public static bool judgeLeaveMsg(string msg) {
            return JObject.Parse(msg).ContainsKey("leaveGame");
        }

        //判断是不是心跳包(失效)
        public static bool judgeHeartBeatMsg(string msg) {
            //return msg.Equals("1");
            return false;
        }
    }
}
