using System;
using System.Data;
using System.IO;
using System.Windows.Forms;
using Newtonsoft.Json;
using pokerServer.Helper;

namespace pokerServer.NetworkProcess.Entity {
    //当前玩家的类型
    public enum PlayerEnum {
        NONE = -1,

        //当前玩家在游戏中
        PLAYING,

        //设置玩家处于在线状态
        ONLINE,

        //当前玩家处于游戏状态但离线
        OFFLINE,

        //当前玩家是个机器人
        ROBOT
    }
    public class Player {
        public string username;  //玩家用户名（做主键）
        public string name;    //玩家姓名
        public bool sex;       //玩家性别
        public int image;      //玩家图片
        public int score;      //玩家当前的积分

        public PlayerEnum playerEnum = PlayerEnum.NONE;
        public bool isReady = false;        //玩家是否准备
        //public bool isRobot = false;        //玩家是否离线（游戏开始时，玩家若离开则服务器帮其托管，直至玩家上线）
        public int lobbyIndex;     //玩家所在游戏房间的位置
        public ServerDeal curServerDeal = null;    //当前玩家服务器处理的进程
        public GameLobby lobby = null;         //玩家所在的游戏房间
        public GameProcess gameProcess = null;  //玩家所对应的游戏进程

        //消除电脑玩家（消除玩家的所有引用）
        public void destroyAIPlayer() {
            curServerDeal = null;
            lobby = null;
            gameProcess = null;
        }

        //获取一个电脑版的Player
        public static Player getRobotPlayer(GameLobby lobby, int lobbyIndex) {
            Player player = new Player();
            //随机生成玩家的姓名，图片，分数和性别
            RandomName randomName = new RandomName();
            player.name = randomName.getRandomName();
            Random rand = new Random();
            player.image = rand.Next(6);
            player.sex = (player.image != 1);
            player.score = 0;

            //设置玩家所在的大厅及其编号
            player.lobby = lobby;
            player.lobbyIndex = lobbyIndex;

            //设置当前玩家为机器人
            player.playerEnum = PlayerEnum.ROBOT;

            return player;
        }

        //从datarow得到Player的信息
        public bool getPlayerInfo(DataRow player) {
            try {
                //传给玩家用户名（做主键）
                username = player["username"].ToString();

                //如果服务器中姓名为空，则随机给他一个姓名
                if (player["name"] == null || player["name"].ToString() == "") {
                    RandomName randomName = new RandomName();
                    name = randomName.getRandomName();

                    //将随机的名字写入数据库
                    int result = SqlDbHelper.ExecuteNonQuery("UPDATE USER_TABLE SET NAME = '" + name
                        + "' WHERE username = '"+ player["username"].ToString() + "'");

                    if (result == 0) {
                        MessageBox.Show("修改玩家姓名失败！");
                    }
                } else {
                    name = player["name"].ToString();
                }
                
                //传给玩家性别
                sex = (bool)player["sex"];
                //传给玩家积分
                score = (int)player["score"];
                //传给玩家图片
                image = (int)player["image"];
                //设置准备状态为false
                isReady = false;
                //设置玩家的状态为在线状态
                playerEnum = PlayerEnum.ONLINE;

                return true;
            } catch (Exception e) {
                MessageBox.Show(e.ToString());
                return false;
            }
        }

        //通过jsonWriter写入玩家的信息
        public string writePlayerInfo() {
            //创建发送的消息
            StringWriter sw = new StringWriter();
            JsonWriter jsonWriter = new JsonTextWriter(sw);
            jsonWriter.WriteStartObject();

            //写入用户名姓名
            jsonWriter.WritePropertyName("name");
            jsonWriter.WriteValue(name);
            //性别
            jsonWriter.WritePropertyName("sex");
            jsonWriter.WriteValue(sex);
            //积分
            jsonWriter.WritePropertyName("score");
            jsonWriter.WriteValue(score);
            //积分
            jsonWriter.WritePropertyName("image");
            jsonWriter.WriteValue(image);
            //是否为机器人
            jsonWriter.WritePropertyName("isRobot");
            jsonWriter.WriteValue(playerEnum == PlayerEnum.ROBOT);

            jsonWriter.WriteEndObject();

            //清空流
            jsonWriter.Flush();

            //发送卡组信息
            return sw.GetStringBuilder().ToString();
        }

        //给客户端发送消息（只有玩家在线时才发送）
        public void sendMessange(string msg) {
            if (playerEnum != PlayerEnum.ROBOT && playerEnum != PlayerEnum.OFFLINE) {
                curServerDeal.sendMessange(msg);
            }
        }

        //设置玩家离线，给其他玩家发送该玩家托管的信息
        public void setLeaveState() {
            //如果玩家处于游戏状态，设置OFFLINE，等待断线重连；如果不是，则为ONLINE状态
            playerEnum = PlayerEnum.OFFLINE;
            string msg = "[" + JsonHelper.jsonObjectInt("playerIndex", lobbyIndex) + ","
                + JsonHelper.jsonObjectBool("isLeaveInfo", true) + "]";
            lobby.sendMesToOtherPlayers(lobbyIndex, msg);
        }

        //设置玩家重连，给其他玩家发送该玩家取消托管的信息
        public void setReconnectState() {
            //将其取消托管状态
            playerEnum = PlayerEnum.PLAYING;
            //设置准备
            isReady = true;
            string msg = "[" + JsonHelper.jsonObjectInt("playerIndex", lobbyIndex) + ","
                + JsonHelper.jsonObjectBool("isLeaveInfo", false) + "]";
            lobby.sendMesToOtherPlayers(lobbyIndex, msg);
        }

        //进行断线重连(叫分和出牌阶段)
        public string reConnected() {
            //发当前每个人的卡组和底牌状况
            string msg = "[";

            //等待游戏进入叫分阶段或者出牌阶段
            while (gameProcess.step < GameProcessEnum.CALL_SCORE) ;

            //赋予当前游戏的进程
            msg += JsonHelper.jsonObjectInt("gameProcess", (int)gameProcess.step);
            //写玩家当前在房间的位置
            msg += "," + JsonHelper.jsonObjectInt("playerIndex", lobbyIndex);
            //写该房间所有人的信息
            msg += "," + lobby.writeExistPlayerInfo();
            //写卡组和底牌的信息
            for (int i = 0; i < 4; i++) {
                msg += "," + JsonHelper.sendCards("deck" + i, gameProcess.peoples[i].deck);
            }
            msg += "," + JsonHelper.sendCards("cardPile", gameProcess.cardPile);

            switch (gameProcess.step) {
                //如果在叫分阶段
                case GameProcessEnum.CALL_SCORE: {
                    //从第一个人开始发，发每个人叫的分
                    int callScorePeople = gameProcess.firstCallScore;
                    msg += "," + JsonHelper.jsonObjectInt("firstCallScore", gameProcess.firstCallScore);
                    
                    for (int j = 0; j < 4; j++) {
                        if (gameProcess.fourCallScore[(callScorePeople + j) % 4] != -1) {
                            msg += "," + JsonHelper.jsonObjectInt("callScore", gameProcess.fourCallScore[(callScorePeople + j) % 4]);
                        } else {
                            break;
                        }
                    }

                    //发当前人叫分已经过去多长的时间
                    msg += "," + JsonHelper.jsonObjectInt("lastTime", gameProcess.getTime());
                    //发当前已经重新开始叫了多少局
                    msg += "," + JsonHelper.jsonObjectInt("reCallScore", gameProcess.reCallScoreNum);
                }
                break;

                //如果在出牌阶段
                case GameProcessEnum.OUT_CARD: {
                    //谁是地主
                    msg += "," + JsonHelper.jsonObjectInt("whoIsLand", gameProcess.whoIsLand);
                    //当前游戏的积分
                    msg += "," + JsonHelper.jsonObjectInt("intergation", gameProcess.intergation);
                    //当前是谁在出牌状态(上一个人)
                    msg += "," + JsonHelper.jsonObjectInt("whoOut", gameProcess.whoOut);
                    //当前出牌的人
                    int curOutCard = gameProcess.startOutCard ? (gameProcess.curOutCard + 3) % 4 : gameProcess.curOutCard;
                    msg += "," + JsonHelper.jsonObjectInt("curOutCard", curOutCard);
                    //历史出的牌
                    for (int i = 0; i < 4; i++) {
                        if (i != curOutCard) {
                            if (gameProcess.outCards[i] == null) {
                                msg += "," + JsonHelper.jsonObjectBool("hasOutCard", false);
                            } else {
                                msg += "," + JsonHelper.jsonObjectBool("hasOutCard", true);
                                msg += "," + JsonHelper.sendCards("outCards", gameProcess.outCards[i]);
                            }
                        }
                    }

                    //发当前人叫分已经过去多长的时间
                    msg += "," + JsonHelper.jsonObjectInt("lastTime", gameProcess.getTime());
                }
                break;
            }

            //设置结尾符
            msg += "]";

            //设置玩家的状态不处于状态重连的阶段
            setReconnectState();

            return msg;
        }
    }
}
