using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using pokerServer.Helper;
using pokerServer.NetworkProcess.Entity;
using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace pokerServer.NetworkProcess {

    //登录返回的结果
    public enum LoginResult {
        //返回错误
        NONE = -1,

        //登录成功
        LOGIN_SUCCESS,

        //用户不存在
        USER_NOT_EXIST,

        //用户已经登录
        USER_ALREADY_LOGIN,

        //密码不正确
        PASSWORD_NOT_CORRECT,

        //断线重连
        RECONNECTED,

        NUM
    }

    //注册返回的结果
    public enum RegisterResult {
        //返回错误
        NONE = -1,

        //注册成功
        REGISTER_SUCCESS,

        //用户已经存在
        USER_EXIST,

        NUM
    }

    class LoginProcess {

        private static DataRow loginDeal(ref JObject loginRegisterMsg, ref LoginResult loginResult, out Player player, ref bool isReconnected) {
            string username = (string)loginRegisterMsg.GetValue("username");
            string password = (string)loginRegisterMsg.GetValue("password");

            //初始设置玩家信息为null
            player = null;
            //根据是否能找到该用户，返回状态参数
            DataRow playerInfo = null;     //玩家的信息
            do {
                //从数据库中获取用户名
                DataTable dataTable = SqlDbHelper.ExecuteDataTable
                    ("select * from user_table where username = '" + username + "'");

                //如果不存在当前用户名
                if (dataTable.Rows.Count == 0) {
                    loginResult = LoginResult.USER_NOT_EXIST;
                    break;
                }

                //如果密码不正确
                playerInfo = dataTable.Rows[0];
                if ((string)playerInfo["password"] != password) {
                    loginResult = LoginResult.PASSWORD_NOT_CORRECT;
                    break;
                }

                //如果当前登录的用户名已登录
                GameLobby lobby = null;
                isReconnected = false;
                if (serverForm.server.players.TryGetValue(username, out lobby)) {
                    //如果当前存在该玩家，但该玩家不在房间中，则移除该玩家
                    if (lobby == null) {
                        serverForm.server.players.TryRemove(username, out lobby);
                        break;
                    }
                    //判断当前用户是否处于游戏中
                    if (lobby.getPlayer(username, out player) == true) {
                        //如果正处在掉线的状态，进行断线重连
                        if (player.playerEnum == PlayerEnum.OFFLINE) {
                            isReconnected = true;
                        }
                    }

                    if (isReconnected == false) {
                        loginResult = LoginResult.USER_ALREADY_LOGIN;
                        break;
                    }
                }

                //如果用户名和密码都匹配，登录成功
                loginResult = LoginResult.LOGIN_SUCCESS;
            } while (false);

            //返回的结果
            return playerInfo;
        }

        private static void registerDeal(ref JObject loginRegisterMsg, ref RegisterResult registerResult) {
            string username = (string)loginRegisterMsg.GetValue("username");
            string password = (string)loginRegisterMsg.GetValue("password");

            //根据是否能找到该用户，返回状态参数
            do {
                //从数据库中获取用户名
                DataTable dataTable = SqlDbHelper.ExecuteDataTable
                    ("select * from user_table where username = '" + username + "'");

                //如果当前用户名已经存在
                if (dataTable.Rows.Count > 0) {
                    registerResult = RegisterResult.USER_EXIST;
                    break;
                }

                //如果合法，注册成功
                registerResult = RegisterResult.REGISTER_SUCCESS;
            } while (false);

            //如果注册结果成功，则将用户信息加入数据库中
            if (registerResult == RegisterResult.REGISTER_SUCCESS) {
                //将用户名和密码数据写入
                SqlDbHelper.ExecuteNonQuery
                    ("insert into user_table(username, password) values('" + username + "','" + password + "')");
            }
        }

        public static string startLogin(string msg, ref LoginResult loginResult, ref Player player, ref bool isReconnected) {
            //将其转换为loginReceive
            JObject loginRegisterMsg = JObject.Parse(msg);
            bool isLoginMsg = true;
            RegisterResult registerResult = RegisterResult.NONE;

            //判断是不是登录信息，返回NONE
            DataRow playerInfo = null;     //玩家的信息
            if (loginRegisterMsg.ContainsKey("startLogin")) {
                playerInfo = loginDeal(ref loginRegisterMsg, ref loginResult, out player, ref isReconnected);
                isLoginMsg = true;
            }
            //判断是不是注册信息，返回NONE
            else if (loginRegisterMsg.ContainsKey("startRegister")) {
                registerDeal(ref loginRegisterMsg, ref registerResult);
                isLoginMsg = false;
            }

            string sendMsg = "[";
            sendMsg += JsonHelper.jsonObjectInt(isLoginMsg ? "loginResult" : "registerResult",
                isLoginMsg ? (int)loginResult : (int)registerResult);
            //如果是登录结果，且登录结果为正常，则把用户信息返回给它
            if (loginResult == LoginResult.LOGIN_SUCCESS) {
                if (player == null) {
                    player = new Player();
                }
                player.getPlayerInfo(playerInfo);
                //写入玩家的信息
                sendMsg += "," + player.writePlayerInfo();
            }

            //如果为断线重连状态，发送恢复的状态消息
            if (isReconnected == true) {
                //给自己发断线重连的消息，给其他人发不是托管的消息
                sendMsg += "," + player.reConnected();
                loginResult = LoginResult.NONE;
            }

            sendMsg += "]";

            return sendMsg;
        }
    }
}
