using Newtonsoft.Json.Linq;
using pokerServer.NetworkProcess.Entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.NetworkProcess {
    //游戏准备阶段流程
    class PrepareGameProcess {
        //判断玩家是否开始
        public static void startPrepareGame(string msg, ref Player player) {
            //判断是不是设置电脑
            if (isSetRobot(msg, ref player) == true) {
                return ;
            }

            JObject prepareResult = JObject.Parse(msg);
            bool isPrepareSuccess = false;

            //是否准备成功
            do {
                //如果消息不匹配
                if (prepareResult.ContainsKey("startReady") == false) {
                    break;
                }

                //玩家开始准备
                GameLobby gameLobby = player.lobby;
                isPrepareSuccess = true;
                //返回准备是否成功
                player.curServerDeal.sendMessange(@"{""readySuccess"":" + isPrepareSuccess + @"}");

                gameLobby.playerReady(player.lobbyIndex);
                
            } while (false);
        }

        //判断是不是设置电脑的
        public static bool isSetRobot(string msg, ref Player player) {
            //如果是设置电脑的
            JObject setRobotMsg = JObject.Parse(msg);
            if (!setRobotMsg.ContainsKey("changeRobot")) {
                return false;
            }

            //获取修改的电脑信息
            bool setRobot = (bool)setRobotMsg.GetValue("changeRobot");
            int playerIndex = (int)setRobotMsg.GetValue("playerIndex");

            //如果返回的消息为错误(如果成功就已经在大厅的时候给其他玩家发送过消息)
            player.lobby.setPlayer2Robot(playerIndex, setRobot, msg);

            return true;
        }
    }
}
