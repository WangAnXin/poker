package wanganxin.com.poker.GameLogic.GameProcess.StartGame;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.utilities.RandomName;
import wanganxin.com.poker.GameLogic.entity.PeopleActionEnum;

public class SingleStartGame extends StartGameProcess {

    public SingleStartGame(LandlordActivity game) {
        super(game);
    }

    //初始化游（单机是随机取名字）
    public void initGame() {
        //姓名初始化//四个人的姓名
        RandomName randomName = new RandomName();
        //人物当前是哪个图片
        int[] peoplePicture = new int[] {3, 2, 0, 1};
        //初始化四人的性别，性别true为男
        boolean[] fourPeopleSex = new boolean[] { true, true, true, false };

        for (int i = 0, j; i < 4; i++) {
            String name;
            do {
                name = randomName.getRandomName();
                for (j = 0; j < i; j++) {
                    if (name == game.peoples[j].name) {
                        break;
                    }
                }
            } while (j != i);   //不允许四人有重复的名字
            //记录当前的index
            game.peoples[i].playIndex = j;
            //初始化姓名，图片，性别，积分
            game.peoples[i].setName(name);
            game.peoples[i].setImage(peoplePicture[i]);
            game.peoples[i].setScore(0);
            game.peoples[i].sex = fourPeopleSex[i];
            game.peoples[i].setAction(PeopleActionEnum.PREAPARE);
        }
    }

    //点击开始，对游戏做准备，开始游戏
    @Override
    public void prepareGameMeth() {
        //如果在托管状态且不是自动测试状态
        if (game.isHosting == true) {
            //关闭托管
            game.robot_Click();
        }

        //卡组洗牌
        game.peopleOperator.shuffle(game.peoples, game.cardpile);

        //游戏准备好，进行下一步
        game.process.gameProcessChange();
    }
}
