using Newtonsoft.Json;
using pokerServer.Helper;
using pokerServer.Landlord.entity;
using System;
using System.Collections.Generic;
using System.IO;

namespace pokerServer.NetworkProcess.Entity {
    public enum PlayerAction {
        NONE = -1,

        //玩家进入该房间
        PLAYER_ENTER,

        //玩家离开该房间
        PLAYER_LEAVE,

        //玩家准备
        PLAYER_PREPARE,

        NUM
    }

    public class GameLobbySort : IComparer<GameLobby> {
        //按每个房间的玩家数量比较（从小到大排序）
        public int Compare(GameLobby lobby1, GameLobby lobby2) {
            return lobby1.playersNum - lobby2.playersNum;
        }
    }

    //游戏房间的类(因为一个房间涉及4个线程，应对玩家（进，出，准备）上锁)
    public class GameLobby {
        public Player[] players;   //当前房间的玩家
        public int playersNum;      //当前房间玩家的数量
        public GameProcess gameProcess;    //游戏的进程

        //对房间初始化
        public GameLobby() {
            players = new Player[4];
            playersNum = 0;
        }
        //销毁该房间
        private void destoryGameLobby() {
            for (int i = 0; i < 4; i++) {
                players[i] = null;
            }
            gameProcess = null;
        }

        //在lobby中找到对应的玩家
        public bool getPlayer(string username, out Player player) {
            bool isGetPlayer = false;
            player = null;
            for (int i = 0; i < 4; i++) {
                if (players[i].username == username) {
                    player = players[i];
                    isGetPlayer = true;
                    break;
                }
            }
            return isGetPlayer;
        }

        //将玩家放入该房间中
        public int SetPlayer(Player player) {
            lock (this) {
                if (playersNum >= 4) {
                    return -1;
                }

                //玩家人数增长
                playersNum++;
                //将玩家放入(找到空位加入)
                int playerIndex = -1;
                for (int i = 0; i < 4; i++) {
                    if (players[i] == null) {
                        players[i] = player;
                        playerIndex = i;
                        players[i].isReady = false;
                        break;
                    }
                }

                //将玩家进入该房间的消息发送给其它玩家
                sendMesToPlayer(playerIndex, PlayerAction.PLAYER_ENTER);

                return playerIndex;
            }
        }

        //通过jsonWriter写入大厅里的玩家的信息
        public string writeExistPlayerInfo() {
            string sendMsg = "";
            //如果存在当前位置存在player写入player信息，没有则写入空的object
            bool isFirst = true;    //第一个不写","
            for (int i = 0; i < 4; i++) {
                if (players[i] != null) {
                    if (isFirst == true) {
                        isFirst = false;
                    } else {
                        sendMsg += ",";
                    }
                    sendMsg += players[i].writePlayerInfo();
                    //写入当前玩家是否准备
                    sendMsg += "," + JsonHelper.jsonObjectBool("isReady", players[i].isReady);
                } else {
                    sendMsg += ",{}";
                }
            }

            return sendMsg;
        }

        //将玩家从该房间移除
        public void removeThisPlayer(int playerIndex) {
            lock(this) {
                //删除当前位置的玩家
                players[playerIndex] = null;
                //玩家数量减少
                playersNum--;
                //向其他玩家发送消息
                sendMesToPlayer(playerIndex, PlayerAction.PLAYER_LEAVE);
            }
        }

        //检查是否还有玩家了，没有玩家，将该房间从大厅中消除
        public bool checkIsStillPlayer() {
            bool hasPlayer = false;
            for (int i = 0; i < 4; i++) {
                if (players[i] != null && players[i].playerEnum != PlayerEnum.ROBOT) {
                    hasPlayer = true;
                    break;
                }
            }

            //如果不剩玩家了，将该房间从大厅移除，并销毁自身
            if (hasPlayer == false) {
                serverForm.server.gameLobbies.Remove(this);
                destoryGameLobby();
            }

            return hasPlayer;
        }

        //玩家准备（判断游戏是否开始，如果开始，给客户端发消息，服务器端所有玩家游戏开始）
        public bool playerReady(int playerIndex) {
            lock(this) {
                //将当前玩家设置为准备
                players[playerIndex].isReady = true;

                //向其他玩家发送消息
                sendMesToPlayer(playerIndex, PlayerAction.PLAYER_PREPARE);

                if (isGameStart()) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        //检查游戏是否开始
        private bool isGameStart() {
            //检测是否可以开始游戏了
            if (playersNum == 4) {
                //判断是不是四个人都准备了
                bool isCanStart = true;
                for (int i = 0; i < 4; i++) {
                    if (players[i].isReady == false) {
                        isCanStart = false;
                        break;
                    }
                }

                //如果都准备了，向四个玩家发送游戏开始
                if (isCanStart == true) {
                    //初始化游戏的进程（开始游戏）
                    gameProcess = new GameProcess(this);

                    string msg = @"{""gameStart"":true}";
                    for (int i = 0; i < 4; i++) {
                        //当前玩家处于游戏状态
                        if (players[i].playerEnum != PlayerEnum.ROBOT) {
                            players[i].playerEnum = PlayerEnum.PLAYING;
                        }
                        players[i].sendMessange(msg);
                        //每个玩家赋予该游戏进程
                        players[i].gameProcess = gameProcess;
                    }
                    //向每个玩家发送卡组信息，和首位叫分的信息，接受信息改为叫分阶段
                    gameProcess.processExecute();

                    //返回游戏已开始
                    return true;
                }
            }

            return false;
        }

        //处理玩家的动作并向其它玩家发送消息(playerIndex:当前动作的玩家，playerState:玩家的动作)
        public void sendMesToPlayer(int playerIndex, PlayerAction playerAction) {
            //创建发送的消息
            string msg = "[";
            //写入玩家编号
            msg += JsonHelper.jsonObjectInt("playerIndex", playerIndex);
            //写入玩家动作信息
            msg += "," + JsonHelper.jsonObjectInt("playerAction", (int)playerAction);
            //如果是有玩家进入，则将玩家信息发给其他玩家
            if (playerAction == PlayerAction.PLAYER_ENTER) {
                msg += "," + players[playerIndex].writePlayerInfo();
            }
            msg += "]";

            for (int i = 0; i < 4; i++) {
                //如果不是当前玩家，且位子上有人，发送相应的消息
                if (i != playerIndex && players[i] != null) {
                    players[i].sendMessange(msg);
                }
            }
        }

        //发送消息给指定的玩家
        public void sendMesToPlayer(int playerIndex, string mes) {
            players[playerIndex].sendMessange(mes);
        }
        //发送消息给所有的玩家
        public void sendMesToAllPlayers(string mes) {
            for (int i = 0; i < 4; i++) {
                if (players[i] != null) {
                    players[i].sendMessange(mes);
                }
            }
        }
        //发送消息给除了playerIndex以外的所有的玩家
        public void sendMesToOtherPlayers(int playerIndex, string mes) {
            for (int i = 0; i < 4; i++) {
                if (players[i] != null && i != playerIndex) {
                    players[i].sendMessange(mes);
                }
            }
        }

        //设置对应位置的玩家成机器人
        public bool setPlayer2Robot(int playerIndex, bool isRobot, string msg) {
            lock (this) {
                //如果是想取消机器人的设置
                if (isRobot == false) {
                    //如果当前玩家为空则错误
                    if (players[playerIndex] == null || players[playerIndex].playerEnum == PlayerEnum.ONLINE) {
                        return false;
                    } else {
                        players[playerIndex] = null;
                        playersNum--;

                        //因为只有在匹配模式下，才可以设置，所以可在减少时添加
                        if (playersNum == 3) {
                            serverForm.server.gameLobbies.Add(this);
                        }
                        
                        //发送消息给所有玩家
                        sendMesToAllPlayers(msg);
                    }
                } else {
                    //如果当前玩家不为空，则错误
                    if (players[playerIndex] != null) {
                        return false;
                    } else {
                        //如果最后一个字符为\n，将其去掉
                        if (msg[msg.Length - 1] == '\n') {
                            msg = msg.Remove(msg.Length - 1);
                        }
                        players[playerIndex] = Player.getRobotPlayer(this, playerIndex);
                        players[playerIndex].isReady = true;
                        playersNum++;

                        //因为只有在匹配模式下，才可以设置，所以可在减少时添加
                        if (playersNum == 4) {
                            serverForm.server.gameLobbies.Remove(this);
                        }

                        //发送消息给其他玩家
                        string reMsg = "[" + msg + "," + players[playerIndex].writePlayerInfo();
                        reMsg += "]";
                        //发送消息给所有玩家
                        sendMesToAllPlayers(reMsg);
                        
                        //判断游戏是否开始
                        isGameStart();
                    }
                }
            }

            return true;
        }

        //一轮游戏结束后，对房间的玩家和游戏进程进行处理，如果没有玩家了，将其清空
        public bool dealTurnGame() {
            //记录是否还有玩家了
            bool hasPlayer = false;

            //四名玩家同时进入游戏准备阶段（将四名玩家置为不准备）
            for (int i = 0; i < 4; i++) {
                //如果当前玩家处在掉线中，游戏结束后将其踢出（从服务器和房间踢出）
                if (players[i].playerEnum == PlayerEnum.OFFLINE || players[i].playerEnum == PlayerEnum.ONLINE) {
                    //将其从游戏中删除
                    serverForm.server.players.TryUpdate(players[i].username, null, this);
                    players[i].playerEnum = PlayerEnum.ONLINE;
                    removeThisPlayer(i);
                    continue;
                }
                //如果玩家处于在线状态，将其设置为不准备状态
                if (players[i] != null && players[i].playerEnum == PlayerEnum.PLAYING) {
                    players[i].playerEnum = PlayerEnum.ONLINE;
                    players[i].isReady = false;
                    players[i].curServerDeal.next_step = ServerProcess.PREPARE_GAME_PROCESS;
                    players[i].curServerDeal.initProcessUpdate();
                    hasPlayer = true;
                }
            }

            //如果当前还有玩家
            if (hasPlayer) {
                //游戏结束后，如果游戏人数少于4，将该房间放入游戏大厅中
                if (playersNum < 4) {
                    serverForm.server.gameLobbies.Add(this);
                }
            } 
            //如果没有玩家了，进行清空
            else {
                for (int i = 0; i < 4; i++) {
                    if (players[i] != null) {
                        players[i].destroyAIPlayer();
                        players[i] = null;
                    }
                }
                gameProcess = null;
            }

            return hasPlayer;
        }
    }
}
