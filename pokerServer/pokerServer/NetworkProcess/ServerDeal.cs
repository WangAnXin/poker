using pokerServer.Helper;
using pokerServer.NetworkProcess.Entity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace pokerServer.NetworkProcess {

    //服务器的进程
    public enum ServerProcess {
        NONE = -1,

        //登录游戏阶段
        LOGIN_PROCESS,

        //游戏大厅匹配阶段
        MATCH_PROCESS,

        //游戏房间准备阶段
        PREPARE_GAME_PROCESS,

        //接受玩家叫分阶段
        CALL_SCORE_PROCESS,

        //接受玩家出牌的阶段
        OUT_CARD_PROCESS,

        NUM
    };

    public class ServerDeal {
        //当前客户端连接的玩家
        Player player;
        //通过房间四人都准备了，房间通知游戏是否开始
        public bool isGameStart = false;
        //当前连接的socket
        public Socket socket;
        //public bool isConnected = true;
        //public int heartBeatNum = Constants.MAX_HEART_BEAT_NUM;

        //当前服务器处于什么状态
        public ServerProcess step = ServerProcess.LOGIN_PROCESS;
        public ServerProcess next_step = ServerProcess.NONE;

        //传给服务器的数据
        public string msg = null;

        //销毁ServerDeal
        void destoryServerDeal() {
            player = null;
            socket = null;
        }

        //服务器进程更新
        public void processUpdate() {
            //判断是不是心跳信息
            if (JudgeOtherInfo.judgeHeartBeatMsg(msg) == true) {
                return;
            }
            //判断是否为玩家离开的消息
            else if (JudgeOtherInfo.judgeLeaveMsg(msg) == true) {
                clientBreak(false);
                return;
            } 

            switch (step) {
                //如果是登录状态（注册）（断线重连）
                case ServerProcess.LOGIN_PROCESS: {
                    //登录验证阶段(分析登录是否成功，返回登录结果）
                    LoginResult loginResult = LoginResult.NONE;
                    bool isReconnected = false;
                    string senderLogin = LoginProcess.startLogin(msg, ref loginResult, ref player, ref isReconnected);

                    //向客户端发送登录是否成功
                    sendMessange(senderLogin);

                    //如果登录成功(如果为断线重连状态，由player中的函数进行状态跳转)
                    if (loginResult == LoginResult.LOGIN_SUCCESS) {
                        //将已登录的用户存入到players中
                        serverForm.server.players.TryAdd(player.username, null);
                        //将当前进程赋给玩家
                        player.curServerDeal = this;
                        //下一步进入游戏大厅匹配环节
                        next_step = ServerProcess.MATCH_PROCESS;
                    } else if (isReconnected) {
                        //将当前进程赋给玩家
                        player.curServerDeal = this;
                        //将自身接受的消息改为对应的状态
                        if (player.gameProcess.step == GameProcessEnum.CALL_SCORE) {
                            next_step = ServerProcess.CALL_SCORE_PROCESS;
                        } else if (player.gameProcess.step == GameProcessEnum.OUT_CARD) {
                            next_step = ServerProcess.OUT_CARD_PROCESS;
                        }
                    }
                }
                break;

                //如果是游戏大厅匹配阶段(个人信息修改)（断线重连）
                case ServerProcess.MATCH_PROCESS: {
                    bool isMatched = false;
                    bool isReconnected = false;
                    //创建匹配处理的进程
                    string matchResult = MatchProcess.startMatch(msg, ref player, out isMatched, ref isReconnected);

                    //向客户端发送匹配结果（成功，返回当前房间的状态）
                    sendMessange(matchResult);

                    //如果匹配成功(进入房间)(如果为断线重连状态，由player中的函数进行状态跳转)
                    if (isMatched == true) {
                        next_step = ServerProcess.PREPARE_GAME_PROCESS;
                    } else if (isReconnected) {
                        //将当前进程赋给玩家
                        player.curServerDeal = this;
                        //将自身接受的消息改为对应的状态
                        if (player.gameProcess.step == GameProcessEnum.CALL_SCORE) {
                            next_step = ServerProcess.CALL_SCORE_PROCESS;
                        } else if (player.gameProcess.step == GameProcessEnum.OUT_CARD) {
                            next_step = ServerProcess.OUT_CARD_PROCESS;
                        }
                    }
                }
                break;

                //如果是游戏房间准备阶段
                case ServerProcess.PREPARE_GAME_PROCESS: {
                    //创建准备处理的进程
                    PrepareGameProcess.startPrepareGame(msg, ref player);
                    //向客户端发送匹配结果
                    //sendMessange(prepareResult);
                }
                break;

                //游戏开始，接受叫分的信息（卡组信息已在GameProcess中发过）
                case ServerProcess.CALL_SCORE_PROCESS: {
                    CallScoreProcess.receiveScoreMsg(msg, ref player);                 
                }
                break;

                //接受玩家的出牌信息(这局结束后由GameProcess进行步骤跳转，跳到PREPARE_GAME_PROCESS)
                case ServerProcess.OUT_CARD_PROCESS: {
                    OutCardProcess.receiveOutCardMsg(msg, ref player);
                }
                break;
            }

            //改变状态前的初始化(跳入下一阶段)
            initProcessUpdate();
        }

        //改变状态前的初始化
        public void initProcessUpdate() {
            //每次将信息清除
            msg = null;

            if (next_step == ServerProcess.NONE) {
                return;
            }

            switch (next_step) {

            }

            step = next_step;
        }

        //客户端断开(isBreakUp==true玩家断线，isBreakUp==false玩家自己离开)
        public void clientBreak(bool isBreakUp) {
            if (player == null) {
                return;
            }

            //根据客户端当前的状态
            GameLobby gameLobby = player.lobby;
            //保存一下当前状态，因为后面会修改且要判定
            ServerProcess orignStep = step;

            //如果玩家处于游戏状态，改变游戏状态
            //设置玩家离线，告诉其他玩家，该玩家托管
            if (player.playerEnum == PlayerEnum.PLAYING) {
                player.setLeaveState();
            }

            switch (step) {              
                case ServerProcess.LOGIN_PROCESS:
                    break;

                //如果当前是登录状态(和匹配阶段操作相同）
                case ServerProcess.MATCH_PROCESS: 
                case ServerProcess.PREPARE_GAME_PROCESS:{
                    //如果玩家在断线状态，不做操作，只将其状态重置
                    if (player.playerEnum == PlayerEnum.OFFLINE) {
                        if (step == ServerProcess.PREPARE_GAME_PROCESS) {
                            //将玩家设置成游戏准备阶段
                            next_step = ServerProcess.MATCH_PROCESS;
                            initProcessUpdate();
                        }
                        break;
                    }

                    //如果游戏大厅不为空（从大厅中将该玩家移除，并发送消息通知其他玩家）(离线状态不删除)
                    if (player.playerEnum == PlayerEnum.OFFLINE) {
                        //如果是玩家自己退出的，则将玩家状态重新设置成游戏匹配状态
                        next_step = ServerProcess.MATCH_PROCESS;
                        initProcessUpdate();
                    } else {
                        //如果游戏大厅不为空（从大厅中将该玩家移除，并发送消息通知其他玩家）
                        if (gameLobby != null) {
                            gameLobby.removeThisPlayer(player.lobbyIndex);
                            //检查是否有玩家了，没玩家将房间从大厅移除
                            if (gameLobby.playersNum == 3) {
                                serverForm.server.gameLobbies.Add(gameLobby);
                            }
                        }
                        //如果玩家断线，将玩家从登录成功的人删除
                        if (isBreakUp || step == ServerProcess.MATCH_PROCESS) {
                            serverForm.server.players.TryRemove(player.username, out gameLobby);
                        } else if (step == ServerProcess.PREPARE_GAME_PROCESS) {
                            //删去对应的gameLobby
                            serverForm.server.players.TryUpdate(player.username, null, gameLobby);
                            //将玩家设置成游戏准备阶段
                            next_step = ServerProcess.MATCH_PROCESS;
                            initProcessUpdate();
                        }
                        player.lobby = null;
                    }
                }
                break;

                //如果游戏已经开始，玩家再退出，保存玩家所对应的游戏房间，游戏房间将其设置托管，直至其上线
                case ServerProcess.CALL_SCORE_PROCESS:
                case ServerProcess.OUT_CARD_PROCESS: {
                    //如果是玩家自己点返回键的，则将玩家置入匹配状态
                    if (isBreakUp == false) {
                        //将玩家状态重新设置成游戏匹配状态
                        next_step = ServerProcess.MATCH_PROCESS;
                        initProcessUpdate();
                    }
                }
                break;
            }

            //如果玩家断线销毁ServerDeal
            if (isBreakUp == true || orignStep == ServerProcess.MATCH_PROCESS) {
                destoryServerDeal();
            }
        }

        //给当前连接的客户端发送消息
        public void sendMessange(string msg) {
            //在屏幕上显示发送的消息（测试）
            if (socket != null) {
                serverForm.server.ShowMsg(socket.RemoteEndPoint.ToString() + " send " + msg);        
                serverForm.Send(msg, socket);
            }
        }
    }
}
