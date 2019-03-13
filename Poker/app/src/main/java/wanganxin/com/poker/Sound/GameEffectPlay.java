package wanganxin.com.poker.Sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import wanganxin.com.poker.R;
import wanganxin.com.poker.GameLogic.OutCard.OutCardStyleEnum;
import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.OutCard.OutCardStyle;
import wanganxin.com.poker.GameLogic.utilities.Constants;

/**
 * Created by Administrator on 2017/4/4.
 */

public class GameEffectPlay {
    private Random ran;
    private SoundPool soundPool;
    private Map<String, Integer> poolMap;
    private Context context;

    //初始化构造函数
    public GameEffectPlay(Context context) {
        this.context = context;
        ran = new Random();
        poolMap = new HashMap<String, Integer>();
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) // 设置音效使用场景
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)// 设置音效的类型
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr) // 设置音效池的属性
                .setMaxStreams(3) // 设置最多可容纳1个音频流
                .build();  //
        try{
            poolMap.put("pressButton", soundPool.load(context, R.raw.press_button, 1));
            poolMap.put("breakrule", soundPool.load(context, R.raw.breakrule, 1));
            poolMap.put("dealcard", soundPool.load(context, R.raw.dealcard, 1));
            poolMap.put("victory_0", soundPool.load(context, R.raw.victory_0, 1));
            poolMap.put("victory_1", soundPool.load(context, R.raw.victory_1, 1));
            poolMap.put("lose", soundPool.load(context, R.raw.lose, 1));
            poolMap.put("planevoice", soundPool.load(context, R.raw.planevoice, 1));
            poolMap.put("bombvoice", soundPool.load(context, R.raw.bombvoice, 1));
            poolMap.put("mutiple_score", soundPool.load(context, R.raw.mutiple_score, 1));
            poolMap.put("outcard", soundPool.load(context, R.raw.outcard, 1));
            poolMap.put("special_remind", soundPool.load(context, R.raw.special_remind, 1));
            poolMap.put("startgame", soundPool.load(context, R.raw.startgame, 1));
            poolMap.put("timeremind", soundPool.load(context, R.raw.timeremind, 1));
            poolMap.put("nexttwostraightvoice", soundPool.load(context, R.raw.nexttwostraightvoice, 1));
            poolMap.put("clock", soundPool.load(context, R.raw.clock, 1));
            poolMap.put("clock_remind", soundPool.load(context, R.raw.clock_remind, 1));

            //加入男女出牌，叫分等等的音效
            String []outCardStyle = new String[]{
                    "bomb",
                    "callrob",
                    "kingbomb",
                    "nexttwo",
                    "nocall",
                    "norob",
                    "onlyone",
                    "onlytwo",
                    "plane",
                    "straight",
                    "threetoone",
                    "threetotwo"
                    };
            //加入出牌音效（男女）
            for (int i = 0; i < 2; i++) {
                String sex = (i == 0 ? "man" : "woman") + "_";
                String size = "";
                String style = "";
                Field field;
                int id;

                //3~大王
                for (int j = 1; j <= 15; j++) {
                    switch (j) {
                        case 1: size = "a"; break;
                        case 11: size = "j"; break;
                        case 12: size = "q"; break;
                        case 13: size = "k"; break;
                        case 14: size = "smallking"; break;
                        case 15: size = "largeking"; break;
                        default: size = Integer.toString(j); break;
                    }
                    //单支，对子，三不带
                    for (int k = 0; k < 3; k++) {
                        switch (k) {
                            case 0: style = ""; break;
                            case 1: style = "double"; break;
                            case 2: style = "trible"; break;
                        }
                        //没有三个小鬼和三个老鬼
                        if (k == 2 && j >= 14)
                            continue;
                        field = R.raw.class.getField(sex + style + size + "_0");
                        id = field.getInt(new R.raw());
                        poolMap.put(sex + style + size + "_0", soundPool.load(context, id, 1));
                    }
                }

                for (int jj = 0; jj < outCardStyle.length; jj++) {
                    field = R.raw.class.getField(sex + outCardStyle[jj] + "_0");
                    id = field.getInt(new R.raw());
                    poolMap.put(sex + outCardStyle[jj] + "_0", soundPool.load(context, id, 1));
                }

                for (int outcardnum = 0; outcardnum < 3; outcardnum++) {
                    field = R.raw.class.getField(sex + "outcard_" + Integer.toString(outcardnum));
                    id = field.getInt(new R.raw());
                    poolMap.put(sex + "outcard_" + Integer.toString(outcardnum), soundPool.load(context, id, 1));
                }

                for (int roblandnum = 0; roblandnum < 3; roblandnum++) {
                    field = R.raw.class.getField(sex + "robland_" + Integer.toString(roblandnum));
                    id = field.getInt(new R.raw());
                    poolMap.put(sex + "robland_" + Integer.toString(roblandnum), soundPool.load(context, id, 1));
                }

                for (int refusenum = 0; refusenum < 4; refusenum++) {
                    field = R.raw.class.getField(sex + "refuse_" + Integer.toString(refusenum));
                    id = field.getInt(new R.raw());
                    poolMap.put(sex + "refuse_" + Integer.toString(refusenum), soundPool.load(context, id, 1));
                }

                field = R.raw.class.getField(sex + "brightcard");
                id = field.getInt(new R.raw());
                poolMap.put(sex + "brightcard", soundPool.load(context, id, 1));
            }

        }catch(Exception e){
        }

    }

    //播放音效的函数
    private void landlordMusicPlay(String s) {
        if (Constants.soundEffectMusicFlag == true && soundPool != null) {
            try {
                int x = soundPool.play(poolMap.get(s), 1.0f, 1.0f, 0, 0, 1.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Toast.makeText(context, Integer.toString(refInt), Toast.LENGTH_SHORT).show();
        }

    }
    //销毁soundPool
    public void releaseSoundPool() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    //胜利
    private void VictoryVoice() {
        landlordMusicPlay("victory_" + Integer.toString(ran.nextInt(2)));
    }
    //输牌
    private void LoseGame()
    {
        landlordMusicPlay("lose");
    }

    //飞机音
    private void PlaneVoice()
    {
        landlordMusicPlay("planevoice");
    }
    //炸弹音
    private void BombVoice()
    {
        landlordMusicPlay("bombvoice");
    }
    //连对音
    private void NextTwoStraightVoice()
    {
        landlordMusicPlay("nexttwostraightvoice");
    }

    //玩家出牌的声音
    private void Player_OutCard_Voice(Boolean sex, List<Card> card) {
        OutCardStyle cs = OutCardStyle.judgeCardStyle(card);
        String news = "";
        //判断牌的大小
        String size = "";
        if (cs.firstCardSize < 11)
            size = Integer.toString(cs.firstCardSize);
        switch (cs.firstCardSize)
        {
            case 11: size = "j"; break;
            case 12: size = "q"; break;
            case 13: size = "k"; break;
            case 14: size = "a"; break;
            case 15: size = "2"; break;
            case 16: size = "smallking"; break;
            case 17: size = "largeking"; break;
        }
        switch (cs.outCardStyleEnum)
        {
            case ONE: news = size + "_0"; break;
            case TWO: news = "double" + size + "_0"; break;
            case THREE: news = "trible" + size + "_0"; break;
            case THREE_TAKE_TWO: news = "threetotwo" + "_0"; break;
            case NEXT_TWO: news = "nexttwo" + "_0"; break;
            case STRAIGHT: news = "straight" + "_0"; break;
            case PLANE: news = "plane" + "_0"; break;
            case PLANE_TAKE_TWO: news = "plane" + "_0"; break;
            case BOMB: news = "bomb" + "_0"; break;
            case FOUR_GHOST: news = "kingbomb" + "_0"; break;
        }

        //防止游戏崩溃
        if (news == null) {
            return;
        }

        String s = (sex == true ? "man" : "woman") + "_" + news;
        landlordMusicPlay(s);
        if (cs.outCardStyleEnum == OutCardStyleEnum.PLANE || cs.outCardStyleEnum == OutCardStyleEnum.PLANE_TAKE_TWO)
        {
            PlaneVoice();
        }
        else if (cs.outCardStyleEnum == OutCardStyleEnum.BOMB || cs.outCardStyleEnum == OutCardStyleEnum.FOUR_GHOST)
        {
            BombVoice();
        }
        else if (cs.outCardStyleEnum == OutCardStyleEnum.NEXT_TWO || cs.outCardStyleEnum == OutCardStyleEnum.STRAIGHT)
        {
            NextTwoStraightVoice();
        }
        else {
            OutCard();  //配上出牌的声音
        }
    }
    //玩家抢牌的声音
    private void Player_FollowOutCard_Voice(Boolean sex) {
        String s = (sex == true ? "man" : "woman") + "_" + "outcard_" + ran.nextInt(3);
        landlordMusicPlay(s);
    }

    //玩家叫地主的声音
    private void Player_CallLandlord_Voice(Boolean sex) {
        String s = (sex == true ? "man" : "woman") + "_" + "callrob_0";
        landlordMusicPlay(s);
    }
    //玩家抢地主的声音
    private void Player_RobLandlord_Voice(Boolean sex) {
        String s = (sex == true ? "man" : "woman") + "_" + "robland_" + ran.nextInt(3);
        landlordMusicPlay(s);
    }

    //玩家不叫的声音
    private void Player_NotScodeCall_Voice(Boolean sex) {
        String s = (sex == true ? "man" : "woman") + "_" + "nocall_0";
        landlordMusicPlay(s);
    }
    //玩家不抢的声音
    private void Player_NotRobLandlord_Voice(Boolean sex) {
        String s = (sex == true ? "man" : "woman") + "_" + "norob_0";
        landlordMusicPlay(s);
    }

    //倍数翻倍的时候
    public void MultipleScore()
    {
        landlordMusicPlay("mutiple_score");
    }
    //剩两张牌
    private void Player_SurplusTwoCard_Voice(final Boolean sex) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String s = (sex == true ? "man" : "woman") + "_" + "onlytwo_0";
                landlordMusicPlay(s);
                SpecialRemind();
            }
        }, 1000);
    }
    //剩一张牌
    private void Player_SurplusOneCard_Voice(final Boolean sex) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String s = (sex == true ? "man" : "woman") + "_" + "onlyone_0";
                landlordMusicPlay(s);
                SpecialRemind();
            }
        }, 1000);
    }
    //出牌
    private void OutCard() {
        landlordMusicPlay("outcard");
    }
    //违反规则的时候
    public void BreakRule()
    {
        landlordMusicPlay("breakrule");
    }
    //特别提醒的时候
    public void SpecialRemind()
    {
        landlordMusicPlay("special_remind");
    }
    //发牌音效
    public void DealCard()
    {
        landlordMusicPlay("dealcard");
    }
    //开始游戏
    public void StartGame()
    {
        landlordMusicPlay("startgame");
    }
    //时钟的音效
    public void clockEffect() {
        landlordMusicPlay("clock");
    }
    //时钟提醒的音效
    public void clockRemind() {
        landlordMusicPlay("clock_remind");
    }

    //不叫地主或不抢地主
    public void NoCallScore_NoRobLandlord(int integration, Boolean sex) {
        if (integration == 0) {
            //不叫地主（如果当前没人叫分）
            Player_NotScodeCall_Voice(sex);
        } else {
            //不抢地主
            Player_NotRobLandlord_Voice(sex);
        }
    }

    //叫分或抢地主
    public void CallScore_RobLandlord(int integration, Boolean sex) {
        if (integration == 0) {
            //（如果当前没人叫分）叫地主
            Player_CallLandlord_Voice(sex);
        } else {
            //抢地主
            Player_RobLandlord_Voice(sex);
        }
    }

    //播放更新积分
    public void update_Integration() {
        //倍数翻倍音效
        MultipleScore();
    }

    //播放卡组剩余数量
    public void update_DeckCount(int deckCount, Boolean sex) {
        if (deckCount == 2) {
            //提示就最后两张牌
            Player_SurplusTwoCard_Voice(sex);
        }
        else if (deckCount == 1) {
            //提示就最后一张牌
            Player_SurplusOneCard_Voice(sex);
        }
    }

    //出牌的声音，判断是出牌还是抢牌还是炸弹
    public void Player_OutorFollowCard_Voice(int who_out, int nowPerson, List<Card> []toOutCard, Boolean sex) {
        //出牌声音
        if (who_out == nowPerson || toOutCard[who_out].size() == 0) {
            Player_OutCard_Voice(sex, toOutCard[nowPerson]);
        }
        //抢牌声音
        else {
            //如果是炸弹
            if (OutCardStyle.isBomb(toOutCard[nowPerson]) != 0 || OutCardStyle.isFourGhost(toOutCard[nowPerson]) == true) {
                BombVoice();
            }
            else {
                Player_FollowOutCard_Voice(sex);
                if (OutCardStyle.isPlaneWithTwo(toOutCard[nowPerson]) != 0 || OutCardStyle.isPlane(toOutCard[nowPerson]) != 0) {
                    //如果是飞机
                    PlaneVoice();
                } else if (OutCardStyle.isStraight(toOutCard[nowPerson]) != 0 || OutCardStyle.isNextTwo(toOutCard[nowPerson]) != 0) {
                    //如果是连对
                    NextTwoStraightVoice();
                } else {
                    //配上出牌的声音
                    OutCard();
                }
            }
        }
    }

    //玩家不出的声音
    public void Player_Refuse_Voice(Boolean sex) {
        String s = (sex == true ? "man" : "woman") + "_" + "refuse_" + ran.nextInt(4);
        landlordMusicPlay(s);
    }

    //播放最终胜利或者是失败的音效，true为胜利，false为失败
    public void Player_EndGame_Voice(final Boolean winFlag) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (winFlag == true) {
                    //如果玩家加分，则玩家获胜
                    VictoryVoice();
                } else {
                    //否则玩家失败
                    LoseGame();
                }
            }
        }, 500);
    }

    //玩家开启明牌的声音
    public void Player_BrightCard_Voice(Boolean sex) {
        landlordMusicPlay((sex == true ? "man" : "woman") + "_brightcard");
    }

    //按按钮的声音
    public void pressButton() {
        landlordMusicPlay("pressButton");
    }
}
