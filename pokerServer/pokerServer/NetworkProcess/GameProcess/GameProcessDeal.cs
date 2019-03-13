using pokerServer.Helper;
using pokerServer.Landlord.entity;
using pokerServer.NetworkProcess.Entity;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading;
using System.Timers;

namespace pokerServer.NetworkProcess {
    //游戏进程
    public enum GameProcessEnum {
        NONE = -1,

        //游戏开始（给四个人发牌）
        GAME_START,

        //游戏开始叫分
        CALL_SCORE,

        //游戏出卡环节
        OUT_CARD,
    }

    public class GameProcess {
        private GameLobby lobby;                    //游戏房间类，对四个人发消息
        public People[] peoples = new People[4];    //四个人的类，记录卡组，积分信息
        public PeopleOperator peopleOperator;      //游戏操作类，对游戏逻辑的操作
        public List<Card> cardPile;                //底牌
        private Random random;                      //初始化随机种子

        private Stopwatch timeCount;      //计算叫分或者出牌所花的时间

        public int firstCallScore;          //第一个叫分的人
        public int curCallScore;           //当前叫分的人
        public int[] fourCallScore;        //记录叫分的情况（断线重连使用）
        public int reCallScoreNum;          //重新叫分的次数

        public int curOutCard;           //当前出牌的人
        public int intergation;             //本局游戏的积分（当前叫的最高分）
        public int whoIsLand;               //谁是地主
        public int whoOut;                  //当前谁出牌
        public List<Card> []outCards = new List<Card>[4];    //当前四人的出牌
        public const int MAX_RE_CALLSCORE_NUM = 3;      //最大可以重新开始叫分的次数

        //对游戏大厅做销毁处理
        public void destroyGameProcessDeal() {
            lobby = null;
            for (int i = 0; i < 4; i++) {
                peoples[i] = null;
                outCards = null;
            }
            peopleOperator = null;
            cardPile = null;
            random = null;
            timeCount = null;
            fourCallScore = null;
        }

        //将游戏大厅传入，因为游戏是对四个人通信
        public GameProcess(GameLobby lobby) {
            this.lobby = lobby;
            random = new Random();
            peopleOperator = new PeopleOperator(new Landlord_GameMode());
            firstCallScore = -1;
            intergation = 0;
            whoIsLand = -1;
            timeCount = new Stopwatch();
            fourCallScore = new int[4] { -1, -1, -1, -1 };
            startCallScore = false;
            startOutCard = false;
            reCallScoreNum = 0;
        }

        //当前服务器处于什么状态
        public GameProcessEnum step = GameProcessEnum.GAME_START;
        public GameProcessEnum next_step = GameProcessEnum.NONE;

        //设置随机叫分的线程
        public Thread []autoCallScoreThread = new Thread[4];
        public Thread []autoOutCardThread = new Thread[4];
        //public bool[] fourPeopleIsCalled = new bool[4];     //判断四个人是否已经叫分或者出牌了

        public bool startCallScore = false;         //开始叫分
        public bool startOutCard = false;           //开始出牌
        private bool isProcessExecute = false;      //判断当前processExecute是否在执行，执行完了才能再次执行

        public void processExecute() {
            while (isProcessExecute == true)
                ;
            isProcessExecute = true;
            executeProcess();
            isProcessExecute = false;
        }

        //游戏进程执行
        private void executeProcess() {
            switch(step) {
                //游戏开始
                case GameProcessEnum.GAME_START: {
                    //初始化peoples和cardPile
                    for (int i = 0; i < 4; i++) {
                        peoples[i] = new People();
                    }
                    cardPile = new List<Card>();

                    //洗牌，将卡发给四个人，留8张底牌
                    peopleOperator.shuffle(peoples, cardPile);
                    //给每个人发卡组信息和底牌信息
                    string msg = "[";
                    for (int i = 0; i < 4; i++) {
                        msg += JsonHelper.sendCards("deck" + i, peoples[i].deck) + ",";
                    }
                    msg += JsonHelper.sendCards("cardPile", cardPile) + "]";
                    lobby.sendMesToAllPlayers(msg);

                    //随机叫分的第一人
                    firstCallScore = random.Next(4);
                    curCallScore = firstCallScore;
                    //将消息发给四个人
                    lobby.sendMesToAllPlayers(JsonHelper.jsonObjectInt("firstCallScore", firstCallScore));
                }
                break;              

                //进入叫分环节（叫分阶段结束）
                case GameProcessEnum.CALL_SCORE: {
                    startCallScore = true;
                    int nowPeople = curCallScore;       //记录当前的玩家
                    curCallScore = (curCallScore + 1) % 4;      //轮到下一位玩家

                    //计算当前人叫分的时间（断线重连使用）
                    timeCount.Restart();

                    //如果当前玩家处于断线状态或它是机器人，则自动认为不叫(等待一段时间)
                    if (lobby.players[nowPeople] == null || lobby.players[nowPeople].playerEnum != PlayerEnum.PLAYING) {
                        System.Timers.Timer t = new System.Timers.Timer(Constants.WAIT_AUTO_CALLSCORE_OUTCARD);
                        t.AutoReset = false;
                        t.Enabled = true;
                        t.Elapsed += new ElapsedEventHandler(delegate (object sender, ElapsedEventArgs e) {
                            //在其他地方已经把process给desroy了
                            if (lobby != null) {
                                if (lobby.players[nowPeople] == null || lobby.players[nowPeople].playerEnum != PlayerEnum.PLAYING) {
                                    //如果是电脑可以不只是叫0分
                                    int callScore =
                                    lobby.players[nowPeople] != null && lobby.players[nowPeople].playerEnum == PlayerEnum.ROBOT
                                    ? random.Next(4 - intergation) + intergation + 1 : 0;
                                    callScore = callScore > 3 ? 0 : callScore;
                                    CallScoreProcess.callScore(callScore, ref lobby.players[nowPeople]);
                                } else {
                                    autoCallScoreThread[nowPeople] = new Thread(waitPeopleCallScore);
                                    timeCount.Stop();
                                    ThreadMsg threadMsg = new ThreadMsg(nowPeople, Constants.WAIT_PEOPLE_CALL_SCORE - (int)timeCount.ElapsedMilliseconds / 1000);
                                    timeCount.Start();
                                    autoCallScoreThread[nowPeople].Start(threadMsg);
                                }
                            }
                        });
                    } 
                    //否则开启一个线程，如果玩家没能在指定时间内叫分，则自动叫分
                    else {
                        autoCallScoreThread[nowPeople] = new Thread(waitPeopleCallScore);
                        ThreadMsg threadMsg = new ThreadMsg(nowPeople, Constants.WAIT_PEOPLE_CALL_SCORE);
                        autoCallScoreThread[nowPeople].Start(threadMsg);
                    }
                }
                break;

                //进入出牌阶段（出牌阶段结束）
                case GameProcessEnum.OUT_CARD: {
                    startOutCard = true;
                    int nowPeople = curOutCard;       //记录当前的玩家
                    curOutCard = (curOutCard + 1) % 4;  //轮到下一位玩家

                    //计算当前人出牌的时间（断线重连使用）
                    timeCount.Restart();

                    //如果当前玩家处于断线状态，则自动出牌
                    if (lobby.players[nowPeople] == null || lobby.players[nowPeople].playerEnum != PlayerEnum.PLAYING) {
                        System.Timers.Timer t = new System.Timers.Timer(Constants.WAIT_AUTO_CALLSCORE_OUTCARD);
                        t.AutoReset = false;
                        t.Enabled = true;
                        t.Elapsed += new ElapsedEventHandler(delegate (object sender, ElapsedEventArgs e) {
                            //在其他地方已经把process给desroy了
                            if (lobby != null) {
                                if (lobby.players[nowPeople] == null || lobby.players[nowPeople].playerEnum != PlayerEnum.PLAYING) {
                                    AIoutCard(nowPeople);
                                } else {
                                    autoOutCardThread[nowPeople] = new Thread(waitPeopleOutCard);
                                    timeCount.Stop();
                                    ThreadMsg threadMsg = new ThreadMsg(nowPeople, Constants.WAIT_PEOPLE_OUT_CARD - (int)timeCount.ElapsedMilliseconds / 1000);
                                    timeCount.Start();
                                    autoOutCardThread[nowPeople].Start(threadMsg);
                                }
                            }
                        });
                    }
                    //否则开启一个线程，如果玩家没能在指定时间内出牌，则自动为其出牌，并设置托管
                    else {
                        autoOutCardThread[nowPeople] = new Thread(waitPeopleOutCard);
                        ThreadMsg threadMsg = new ThreadMsg(nowPeople, Constants.WAIT_PEOPLE_OUT_CARD);
                        autoOutCardThread[nowPeople].Start(threadMsg);
                    }
                }
                break;
            }

            //如果是游戏开始状态，执行一次直接进入下一状态
            if (step == GameProcessEnum.GAME_START) {
                processUpdate();
            }
        }

        //游戏进程的更新
        public void processUpdate() {
            switch (step) {
                //游戏开始
                case GameProcessEnum.GAME_START: {
                    //进入下一步的叫分环节
                    next_step = GameProcessEnum.CALL_SCORE;

                    //四名玩家同时进入叫分阶段
                    for (int i = 0; i < 4; i++) {
                        //如果该玩家不是机器人(离线的玩家也要对其状态进行更新)
                        if (lobby.players[i].playerEnum != PlayerEnum.ROBOT && lobby.players[i].playerEnum != PlayerEnum.OFFLINE) {
                            lobby.players[i].curServerDeal.next_step = ServerProcess.CALL_SCORE_PROCESS;
                            lobby.players[i].curServerDeal.initProcessUpdate();
                        }
                    }
                }
                break;

                //进入叫分环节（叫分阶段结束）
                case GameProcessEnum.CALL_SCORE: {
                    if (intergation >= 3 || curCallScore % 4 == firstCallScore || reCallScoreNum == MAX_RE_CALLSCORE_NUM - 1) {
                        //停止计时
                        timeCount.Stop();

                        //已经连续三局没人叫地主了，强制要求一个人当地主
                        if (reCallScoreNum == MAX_RE_CALLSCORE_NUM - 1 && intergation == 0) {
                            reCallScoreNum = 0;
                            //随机指定一个地主，叫分为1
                            intergation = 1;
                            whoIsLand = random.Next(4);
                            CallScoreProcess.setLandlord(whoIsLand, ref lobby);
                        }

                        //如果当前有人叫分
                        if (intergation > 0) {
                            next_step = GameProcessEnum.OUT_CARD;
                            //四名玩家同时进入出牌阶段(离线的玩家也要对其状态进行更新)
                            for (int i = 0; i < 4; i++) {
                                if (lobby.players[i].playerEnum != PlayerEnum.ROBOT && lobby.players[i].playerEnum != PlayerEnum.OFFLINE) {
                                    lobby.players[i].curServerDeal.next_step = ServerProcess.OUT_CARD_PROCESS;
                                    lobby.players[i].curServerDeal.initProcessUpdate();
                                }
                            }
                        }
                        //如果当前没人叫分，则重新开始（三局之内）
                        else if (reCallScoreNum < MAX_RE_CALLSCORE_NUM - 1) {
                            //当前叫分轮数加一
                            reCallScoreNum++;
                            next_step = GameProcessEnum.GAME_START;
                        }
                    }
                }
                break;

                //进入出牌阶段（出牌阶段结束）
                case GameProcessEnum.OUT_CARD: {
                    //停止计时
                    timeCount.Stop();

                    //进行积分结算
                    peopleOperator.settleScore(peoples, intergation);

                    //如果玩家不为机器人，将结算完的积分写入数据库
                    for (int i = 0; i < 4; i++) {
                        if (lobby.players[i] != null && lobby.players[i].playerEnum != PlayerEnum.ROBOT) {
                            string username = lobby.players[i].username;
                            int score = peoples[i].integration;
                            SqlDbHelper.ExecuteNonQuery
                            ("update user_table set score = " + score + " where username = '" + username + "'");
                        }
                    }

                    //房间对剩余玩家进行处理，如果还剩玩家，将其放入大厅中，如果不剩，做销毁处理
                    if (lobby.dealTurnGame() == false) {
                        destroyGameProcessDeal();
                        return;
                    }

                    //积分结算完等待下一局游戏开始
                    next_step = GameProcessEnum.GAME_START;
                }
                break;
            }

            //改变状态前的初始化
            initProcessUpdate();
        }

        //改变状态前的初始化
        private void initProcessUpdate() {
            switch (next_step) {
                //游戏开始阶段的初始化
                case GameProcessEnum.GAME_START: {
                    firstCallScore = -1;
                    intergation = 0;
                    whoIsLand = -1;
                    startCallScore = false;
                    startOutCard = false;
                }
                break;

                case GameProcessEnum.CALL_SCORE: {
                    for (int i = 0; i < 4; i++) {
                        fourCallScore[i] = -1;
                    }
                }
                break;

                //出牌阶段前的初始化
                case GameProcessEnum.OUT_CARD: {
                    //地主先出牌
                    whoOut = whoIsLand;
                    curOutCard = whoOut;
                    //初始化四人的出牌队列
                    for (int i = 0; i < 4; i++) {
                        outCards[i] = null;
                    }
                    //给地主8张牌
                    peopleOperator.dealCardToLandlord(peoples, cardPile, whoIsLand);
                }
                break;
            }

            if (next_step != GameProcessEnum.NONE) {
                step = next_step;
            }
            //如果下一步为叫分环节，等待客户端发牌动画结束，开始计时叫分
            if (next_step == GameProcessEnum.CALL_SCORE) {
                System.Timers.Timer t = new System.Timers.Timer(Constants.DIDPLAY_DEAL_CARD);
                t.AutoReset = false;
                t.Enabled = true;
                t.Elapsed += new ElapsedEventHandler(delegate (object sender, ElapsedEventArgs e) {
                    //执行叫分环节
                    processExecute();
                });
            }
        }

        //传递给线程的消息类
        private class ThreadMsg {
            public int curPeople;
            public int waitTime;

            public ThreadMsg(int curPeople, int waitTime) {
                this.curPeople = curPeople;
                this.waitTime = waitTime;
            }
        };

        // 定义线程方法:等待人的叫分信息
        private void waitPeopleCallScore(object msg) {
            try {
                //等待玩家叫分的时间，如果玩家在指定时间内没有叫分，认为玩家那方出现问题，设置玩家离线
                ThreadMsg threadMsg = (ThreadMsg)msg;
                Thread.Sleep(threadMsg.waitTime);

                lobby.players[threadMsg.curPeople].setLeaveState();
                //设置随机的叫分
                CallScoreProcess.callScore(0, ref lobby.players[threadMsg.curPeople]);
            }
            catch (Exception e) {

            }
        }

        // 定义线程方法:等待人的出牌信息
        private void waitPeopleOutCard(object msg) {
            try {
                //等待玩家出牌的时间，如果玩家在指定时间内没有出牌，认为玩家那方出现问题，设置玩家离线
                ThreadMsg threadMsg = (ThreadMsg)msg;
                Thread.Sleep(threadMsg.waitTime);

                lobby.players[threadMsg.curPeople].setLeaveState();
                //设置AI为其出牌
                AIoutCard(threadMsg.curPeople);
            }
            catch (Exception e) {

            }
        }

        //让AI帮其出牌
        private void AIoutCard(int nowPeople) {
            try {
                //设置AI为其出牌
                outCards[nowPeople] = peopleOperator.AIOutCard
                (peoples, whoIsLand, whoOut, nowPeople, whoOut == nowPeople ? null : outCards[whoOut]);
                string msg = JsonHelper.sendCards("outCard", outCards[nowPeople]);
                //模仿玩家发送消息
                OutCardProcess.receiveOutCardMsg(msg, ref lobby.players[nowPeople]);
            } catch (Exception e) {
                serverForm.server.ShowMsg(e.ToString());
            }
        }

        //获取已经过去了多少时间
        public int getTime() {
            timeCount.Stop();
            TimeSpan ts = timeCount.Elapsed;
            timeCount.Start();
            return ts.Seconds;
        }
    }
}
