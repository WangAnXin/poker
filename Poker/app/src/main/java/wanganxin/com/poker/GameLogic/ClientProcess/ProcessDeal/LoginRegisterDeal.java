package wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveEnum;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientSendEnum;
import wanganxin.com.poker.GameActivity.GameStartActivity;
import wanganxin.com.poker.GameActivity.LoginActivity;
import wanganxin.com.poker.GameActivity.RegisterActivity;
import wanganxin.com.poker.GameLogic.entity.Player;

public class LoginRegisterDeal {
    //发送登录消息
    public static String startLogin(String username, String password) {
        //发消息给服务器要登录
        JsonObject jsonObject = new JsonObject();

        //发送登录信息
        jsonObject.addProperty("startLogin", true);
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", password);

        return jsonObject.toString();
    }

    //发送注册消息
    public static String startRegister(String username, String password) {
        //发消息给服务器要登录
        JsonObject jsonObject = new JsonObject();

        //发送登录信息
        jsonObject.addProperty("startRegister", true);
        jsonObject.addProperty("username", username);
        jsonObject.addProperty("password", password);

        return jsonObject.toString();
    }

    //发送离开消息，改变发送和接受的状态
    public static void leaveGame() {
        GameStartActivity gameStartActivity = GameStartActivity.getInstance();

        //将接受消息的状态和发送消息的状态改变
        gameStartActivity.sendDeal.nextStep = ClientSendEnum.MATCH_PREPARE_PROCESS;
        gameStartActivity.sendDeal.initProcessSendUpdate();

        gameStartActivity.receiveDeal.nextStep = ClientReceiveEnum.MATCH_RECEIVE_PROCESS;
        gameStartActivity.receiveDeal.initProcessReceiveUpdate();

        //发消息给服务器要离开
        JsonObject jsonObject = new JsonObject();
        //发送登录信息
        jsonObject.addProperty("leaveGame", true);
        gameStartActivity.send(jsonObject.toString());

        return;
    }

    //处理服务器返回的登录结果
    public static int dealLoginResult(String jsonResult) {
        Gson gson = new Gson();

        //读取jsonResult
        //Json的解析类对象
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(jsonResult).getAsJsonArray();

        //获取返回结果
        JsonObject result = jsonArray.get(0).getAsJsonObject();

        //如果是登录的返回结果
        if (result.has("loginResult") == true) {
            //获取返回的结果
            int loginResult = result.get("loginResult").getAsInt();
            //发送消息给对应的activity处理相应的结果
            LoginActivity loginActivity = LoginActivity.getInstance();
            loginActivity.handler.obtainMessage(loginResult).sendToTarget();
            if (loginResult == LoginActivity.LOGIN_SUCCESS) {
                //获取用户信息
                JsonElement playerInfo = jsonArray.get(1);
                ClientReceiveDeal.player = gson.fromJson(playerInfo, Player.class);
            }

            //如果登录信息中含有断线重连的信息（调用断线重连类，进行断线重连）
            if (jsonArray.size() == 3 && ReconnectDeal.firstDealReconnected(jsonArray.get(2).getAsJsonArray())) {
                return LoginActivity.NONE;
            }
        }
        //如果是注册的返回结果
        else if (result.has("registerResult") == true) {
            //获取返回的结果
            int registerResult = result.get("registerResult").getAsInt();
            //发送消息给对应的activity处理相应的结果
            RegisterActivity registerActivity = RegisterActivity.getInstance();
            registerActivity.handler.obtainMessage(registerResult).sendToTarget();
            return LoginActivity.NONE;
        }

        //返回是否登录成功
        return LoginActivity.LOGIN_SUCCESS;
    }
}
