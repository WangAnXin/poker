using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using pokerServer.Helper;
using pokerServer.NetworkProcess.Entity;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.NetworkProcess {
    //玩家进入游戏大厅的处理流程
    class MatchProcess {
        //接受Login的数据，用户名和密码
        class MatchReceive {
            public string startMatch = null;
        }

        //开始进入游戏大厅匹配（如果消息匹配，进入大厅）（不匹配，消息错误）
        public static string startMatch(string msg, ref Player player, out bool isMatched, ref bool isReconnected) {
            //先初始化匹配失败
            isMatched = false;

            //判断是不是修改用户个人信息的
            if (isPlayerInfoChange(msg, ref player)) {
                //如果是，返回用户信息修改成功（现在客户端并未处理此信息）
                isMatched = false;
                return @"{""playerChangeSuccess"":true}";
            }

            //将其转换为matchReceive
            MatchReceive matchReceive = JsonConvert.DeserializeObject<MatchReceive>(msg);
            
            //创建一空房间
            GameLobby gameLobby = null;
            //保存玩家的位置
            int playerIndex = -1;

            do {
                //如果该信息不符合返回错误
                if (matchReceive.startMatch == null) {
                    serverForm.server.ShowMsg("游戏大厅消息不对");
                    break;
                }

                //判断该玩家是否处于断线重连的状态，如果是返回状态重连的信息
                serverForm.server.players.TryGetValue(player.username, out gameLobby);
                if (gameLobby != null) {
                    isReconnected = true;
                    return player.reConnected();
                }

                //找到人最多的大厅(上锁)
                lock (serverForm.server.gameLobbies) {
                    //如果当前没有房间
                    if (serverForm.server.gameLobbies.Count == 0) {
                        //创一个空的房间
                        gameLobby = new GameLobby();
                    } else {
                        //找到玩家人数最多的房间
                        gameLobby = serverForm.server.gameLobbies.Max;
                        //将该游戏大厅移除
                        serverForm.server.gameLobbies.Remove(gameLobby);
                    }

                    //将玩家放入该大厅中，返回当前位置
                    playerIndex = gameLobby.SetPlayer(player);
                    player.lobbyIndex = playerIndex;
                    player.lobby = gameLobby;

                    //如果为-1，房间数量异常（测试）
                    if (playerIndex == -1) {
                        serverForm.server.ShowMsg("房间数量异常");
                        break;
                    }

                    //如果游戏人数少于4，将该房间放入游戏大厅中
                    if (gameLobby.playersNum < 4) {
                        serverForm.server.gameLobbies.Add(gameLobby);
                    }

                    //设置匹配成功
                    isMatched = true;
                }
                //设置玩家对应的游戏大厅
                serverForm.server.players[player.username] = gameLobby;

            } while (false);

            string sendMsg = "[";
            sendMsg += JsonHelper.jsonObjectBool("matchResult", isMatched);
            //如果匹配成功，返回玩家位置信息和其余玩家的信息
            sendMsg += "," + JsonHelper.jsonObjectInt("playerIndex", playerIndex);
            sendMsg += "," + gameLobby.writeExistPlayerInfo();
            sendMsg += "]";

            return sendMsg;
        }

        //判断是不是修改个人信息的
        public static bool isPlayerInfoChange(string msg, ref Player player) {
            //如果是修改个人信息的
            JObject changeInfoMsg = JObject.Parse(msg);
            if (!changeInfoMsg.ContainsKey("changeInfo")) {
                return false;
            }

            //获取所修改的玩家信息
            player.name = (string)changeInfoMsg.GetValue("playerName");
            player.sex = (bool)changeInfoMsg.GetValue("playerSex");
            player.image = (int)changeInfoMsg.GetValue("playerImage");

            //更新数据库对应的数据
            SqlDbHelper.ExecuteNonQuery("update user_table set name='"+ player.name +"'," +
                    " sex="+ (player.sex ? 1 : 0) +", image="+ player.image +"" +
                    " where username='"+ player.username +"'");

            string s = "update user_table set name='" + player.name + "'," +
                    " sex=" + player.sex + ", image=" + player.image + "" +
                    " where username='" + player.username + "'";

            return true;
        }
    }
}
