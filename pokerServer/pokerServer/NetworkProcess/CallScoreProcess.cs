using Newtonsoft.Json.Linq;
using pokerServer.Helper;
using pokerServer.NetworkProcess.Entity;

namespace pokerServer.NetworkProcess {
    //叫分进程
    class CallScoreProcess {
        public static void receiveScoreMsg(string msg, ref Player player) {
            JObject callScoreResult = JObject.Parse(msg);

            //是否叫分结束
            do {
                //如果消息不匹配
                if (callScoreResult.ContainsKey("callScore") == false) {
                    break;
                }

                //获取当前的叫分情况
                //player.gameProcess.fourPeopleIsCalled[player.lobbyIndex] = true;
                int score = (int)callScoreResult.GetValue("callScore");
                if (player.gameProcess.autoCallScoreThread[player.lobbyIndex] != null) {
                    player.gameProcess.autoCallScoreThread[player.lobbyIndex].Interrupt();
                }

                //实现叫分
                callScore(score, ref player);

            } while (false);

            return ;
        }

        //根据所叫的分数，发给玩家，并判断是否进行状态转移
        public static void callScore(int score, ref Player player) {
            //如果当前积分正常（修改积分和地主的情况）
            if (score <= 3 && score > player.gameProcess.intergation || score == 0) {
                //保存当前玩家所叫的分
                player.gameProcess.fourCallScore[player.lobbyIndex] = score;

                //如果不是不叫
                if (score != 0) {
                    player.gameProcess.intergation = score;
                    player.gameProcess.whoIsLand = player.lobbyIndex;
                }

                //发叫分的人和叫了多少分（发叫分的人是为了同步信息，如果客户端那边有误，将前面的人都置成不叫）
                string sendMsg = "[";
                sendMsg += JsonHelper.jsonObjectInt("playerIndex", player.lobbyIndex) + ",";
                sendMsg += JsonHelper.jsonObjectInt("callScore", score) + "]";

                //发送给除该玩家外的其他玩家叫分信息
                player.lobby.sendMesToAllPlayers(sendMsg);

                //如果得分大于等于3，或者1轮叫完结束
                if (score >= 3 || (player.gameProcess.firstCallScore + 3) % 4 == player.lobbyIndex) {
                    player.gameProcess.processUpdate();
                    //执行下一步
                    //如果游戏重新开始，则自动发牌
                    //如果是开始出牌，则开始计时玩家出牌，玩家未出牌自动为其出牌
                    player.gameProcess.processExecute();
                } 
                //执行下一次叫分
                else {
                    player.gameProcess.processExecute();
                }
            }
        }

        //强制要求一个人当地主
        public static void setLandlord(int landlord, ref GameLobby lobby) {
            string sendMsg = JsonHelper.jsonObjectInt("whoIsLand", landlord);
            lobby.sendMesToAllPlayers(sendMsg);
        }

    }
}
