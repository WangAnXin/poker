using Newtonsoft.Json.Linq;
using pokerServer.Helper;
using pokerServer.Landlord.entity;
using pokerServer.NetworkProcess.Entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.NetworkProcess {
    //处理出牌的信息
    class OutCardProcess {
        //给玩家发消息的时候会把OutCard最后一个字符去掉，因为客户端发来的最后一个字符为\n
        public static bool receiveOutCardMsg(string msg, ref Player player) {
            JObject outCardResult = JObject.Parse(msg);
            bool isGameEnd = false;
            GameProcess gameProcess = player.gameProcess;

            //游戏是否结束（有人出完牌）
            do {
                //获取托管信息或者是出牌信息
                if (outCardResult.ContainsKey("isRobortInfo") == true) {
                    //托管服务器设置其未离开的状态，发消息给其他玩家托管信息
                    //如果玩家点击托管，则由玩家客户端AI自行出牌，服务器不帮其出牌，但要通知其他玩家其进行托管
                    player.playerEnum = PlayerEnum.PLAYING;
                } else if (outCardResult.ContainsKey("outCard") == true) {
                    //获取当前的出牌情况
                    string outCardMsg = (string)outCardResult.GetValue("outCard");
                    //将当前玩家的出牌队列赋给它
                    gameProcess.outCards[player.lobbyIndex] = JsonHelper.jsonToCards(outCardMsg);

                    //在客户端已经判断过出牌是否合法了（除非作弊，不考虑）
                    //更新玩家的出牌，并判断游戏是否结束
                    if (gameProcess.outCards[player.lobbyIndex].Count > 0) {
                        try {
                            isGameEnd = gameProcess.peopleOperator.outCard(gameProcess.peoples[player.lobbyIndex],
                            gameProcess.outCards[player.lobbyIndex], ref gameProcess.intergation);
                        } catch (Exception e) {
                            serverForm.server.ShowMsg(e.ToString());
                        }
                        //是当前玩家出的牌
                        gameProcess.whoOut = player.lobbyIndex;
                    }

                    if (gameProcess.autoOutCardThread[player.lobbyIndex] != null) {
                        gameProcess.autoOutCardThread[player.lobbyIndex].Interrupt();
                    }

                } else {
                    break;
                }

                //向其它玩家发送托管和出牌的情况
                string sendMsg = "[";
                sendMsg += JsonHelper.jsonObjectInt("playerIndex", player.lobbyIndex) + ",";
                //如果最后一个字符为\n，将其去掉
                if (msg[msg.Length - 1] == '\n') {
                    msg = msg.Remove(msg.Length - 1);
                }
                sendMsg += msg + "]";

                //发送给除该玩家外的其他玩家叫分信息
                player.lobby.sendMesToAllPlayers(sendMsg);

                //如果有人出完牌，进入积分结算环节
                if (isGameEnd == true) {
                    //更新状态进行积分结算
                    gameProcess.processUpdate();
                } else if (outCardResult.ContainsKey("outCard") == true) {
                    //进行下一步出牌
                    gameProcess.processExecute();
                }

            } while (false);

            return isGameEnd;
        }
    }
}
