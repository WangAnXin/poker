package wanganxin.com.poker.GameLogic.GameProcess.StartGame;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameLogic.entity.PeopleActionEnum;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;

public class NetStartGame extends StartGameProcess {
    public NetStartGame(LandlordActivity game) {
        super(game);
    }

    @Override
    public void initGame() {
        //按照当前玩家的位置，初始化peoples
        for (int i = GlobalValue.playerIndex, j = 0; j < 4; i = (i + 1) % 4, j++) {
            //初始化当前逻辑对
            game.peoples[j].netIndex = i;
            //记录当前的index
            game.peoples[j].playIndex = j;
            if (GlobalValue.players[i] != null) {
                //初始化姓名，图片，性别，积分
                game.peoples[j].setPlayerInfo(GlobalValue.players[i], GlobalValue.playersIsReady[i]);
            } else {
                //消去开始按钮
                game.peoples[j].actionAlphaGoneRun();
                //设置机器人图片
                game.peoplesImage[j].setBackground(game.resources.getDrawable(R.mipmap.big_robot, game.getTheme()));
            }

            //初始化网络和本地位置的对应信息
            GlobalValue.playersIndex[i] = j;
        }
        //game.room_master[0].setVisibility(View.VISIBLE);    //显示房主的图标
    }

    //自己开始准备并发送信息给服务器
    @Override
    public void prepareGameMeth() {
        //发送准备的信息
        game.process.socketClient.sendDeal.processSendUpdate();
        //设置自己准备
        game.peoples[0].setAction(PeopleActionEnum.PREAPARE);
    }
}
