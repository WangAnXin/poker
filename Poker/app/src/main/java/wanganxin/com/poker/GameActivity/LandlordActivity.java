package wanganxin.com.poker.GameActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.ReconnectDeal;
import wanganxin.com.poker.GameLogic.GameProcess.GameProcess;
import wanganxin.com.poker.GameLogic.GameProcess.GameProcessEnum;
import wanganxin.com.poker.GameLogic.entity.Clock;
import wanganxin.com.poker.GameLogic.Operator.ShowDialog;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.LoginRegisterDeal;
import wanganxin.com.poker.GameAnimation.PokeEffect.DeckOperator;
import wanganxin.com.poker.GameAnimation.PokeEffect.PokeOperator;
import wanganxin.com.poker.GameLogic.GameProcess.StartGame.InitScorePanel;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.Sound.BackgroundMusic;
import wanganxin.com.poker.GameLogic.Operator.LandLord_GameMode;
import wanganxin.com.poker.GameLogic.Operator.PeopleOperator;
import wanganxin.com.poker.Sound.GameEffectPlay;
import wanganxin.com.poker.GameLogic.utilities.RandomName;
import wanganxin.com.poker.GameLogic.entity.Card;
import wanganxin.com.poker.GameLogic.entity.People;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;
import wanganxin.com.poker.GameLogic.utilities.GlobalValue;
import wanganxin.com.poker.GameLogic.utilities.HomeWatcher;

public class LandlordActivity extends AppCompatActivity {

    //region 变量命名处
    public PeopleOperator peopleOperator;   //业务类，对四人队列进行操作
    public People[] peoples = new People[4];        //四个人的队列
    public List<Card> cardpile = new ArrayList<Card>();                 //剩余的卡组
    public int whosLand;        //谁是地主

    public int[] integration = new int[1];     //当前局数的积分，Integer不能传引用
    public Integer multiple;        //当前局数的倍数
    public Random ran;     //随机函数

    public CardView[] poke = new CardView[109];       //108张卡
    public CardView[] downpoke = new CardView[8];     //8张底牌
    public TextView[] temptPoke = new TextView[109];       //108张卡（保存位置）

    public TextView[] mes = new TextView[4];          //四个人的信息
    public TextView[] four_action_pic = new TextView[4];     //显示图片（1分、不叫）
    public TextView breakrule;     //规则不符合
    public TextView cannotOutHint;     //不能出牌
    public CardView start_button;  //开始按钮
    public CardView [] outCardThreebtn = new CardView[3];     //出牌,不出,提示
    public CardView [] callScoreFourbtn = new CardView[4];     //叫分四按钮，一、二、三分、不叫
    public TextView [] fourLandPicbtn = new TextView[4];     //四个人头上的地主图标
    public TextView [] fourFunbtn = new TextView[4];     //四个功能按钮图标
    public TextView[] robots = new TextView[4];     //托管后的各图标
    public TextView[] room_master = new TextView[4];     //四人房主的图标
    private TextView[] fourchat = new TextView[8];//创建四人说话
    public TextView[] peoplesImage = new TextView[4];//创建四人说话
    public TextView brightCard;   //明牌模式
    public TextView lbl_bottomscore;//底分
    public TextView lbl_multiple;    //倍数
    public TextView[][] score_panel_score = new TextView[4][];  //计分按钮
    public CardView score_button;  //右上角积分移动
    public CardView score_panel;  //右上角积分移动
    private CardView undercard_panel;  //左上角的底牌
    public TableLayout score_panel_table;

    public TextView[][] end_panel_score = new TextView[4][]; //结束计分按钮
    private TextView end_panel_close; //结束的积分榜的差
    private LinearLayout end_panel_table; //结束的积分榜上的表格
    public CardView end_score_panel; //结束的积分榜上的表格
    public TableRow [] end_panel_row = new TableRow[4]; //结束的积分榜上的表格的对应行

    public Boolean isHosting;     //是否处于托管状态
    public Boolean isBackgroundOpen; //是否可以播放背景音乐
    public Boolean isSoundEffectOpen; //是否可以播放音效
    public Boolean isBrightCard = false;      //判断是否为明牌模式，true为是，false为否
    public Boolean isScoreUp;  //判断右上角积分榜是不是在上面

    public GameEffectPlay soundEffect;  //音效播放
    public BackgroundMusic backgroundMusic;     //背景音乐播放
    public RelativeLayout landlord_layout; //landlord相对布局

    public Display d;  //保存当前系统的长宽px
    public Resources resources;  //获取当前系统的资源
    public int card_interval;  //card_interval的px版
    public int card_small_interval;  //CARD_SMALL_INTERVAL的px版

    public PokeOperator pokeOperator;//设置卡牌图片
    public DeckOperator deckOperator;//对卡牌的操作类
    public GameProcess process; //游戏的流程
    private HomeWatcher mHomeWatcher;   //监听home键

    public boolean isNetWork;   //单机模式还是网络模式
    public int reconMode;       //判断是不是断线重连的模式

    public CardView[] fourClock = new CardView[4];     //每位玩家对应的计时工具
    public Clock clockManage;           //控制时钟的类
    public boolean isSpeedUp;           //动画是否处于加速中
    //endregion

    //region 初始化窗体，包括各个控件和变量的初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landlord);

        //将socket于当前游戏的activity绑定
        GameStartActivity.getInstance().receiveDeal.initLandlordActivity(this);
        GameStartActivity.getInstance().sendDeal.initLandlordActivity(this);

        //隐藏状态栏和虚拟按键
        hideActionBar();

        //获取是单机模式还是网络模式
        Intent intent = getIntent();
        this.isNetWork = intent.getBooleanExtra("isNetWork", false);
        this.reconMode = intent.getIntExtra("isReconnected", -1);

        //判断是不是重连，如果处于重连读取基本信息
        if (reconMode > 0) {
            ReconnectDeal.getMatchInfo();
        }

        initGameLandLord();  //初始化界面各控件，各变量，各信息
    }

    //隐藏状态栏和虚拟按键
    private void hideActionBar() {
        //隐藏顶部的标题栏
        this.getSupportActionBar().hide();
        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
    //初始化界面各控件，各变量，各信息
    private void initGameLandLord() {
        //初始化对应各控件
        initFindWidget();

        //刚进入游戏后初始化所有变量
        initVariable();
        InitScorePanel initScorePanel = new InitScorePanel(this);
        initScorePanel.InitalScore_panel();    //初始化积分榜
        initScorePanel.InitEnd_Score_panel();  //初始化结束时的积分榜
        backgroundMusic.initBackgroundMusic(); //初始化背景音乐
        InitalPoke(); //初始化108张牌的标签
        inithomeWatcher();      //初始化home键的监听事件，按下后声音关闭

        //初始化刚进入游戏的显示状态
        initViewDisplay();

        //对玩家信息的初始化
        process.startGameProcess.initGame();

        //设置长按将牌放下的监听事件
        landlord_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //只有在发完牌阶段且在结束游戏阶段之前
                if (process.step.compareTo(GameProcessEnum.DEAL_CARD_PROCESS) > 0
                        && process.step.compareTo(GameProcessEnum.End_GAME_PROCESS) < 0) {
                    //长按将牌放下
                    putDownPoke();
                }
                return true;
            }
        });

        //如果处于重连状态，第一步先发牌，加快发牌的速度（快速回复牌局）
        if (reconMode > 0) {
            ReconnectDeal.getCardInfo(this);
            speedUpAnimator();
            process.gameProcessChange();
        }

        //LandlordActivity创建完成后置为false
        GameStartActivity.getInstance().isReconnected = false;
    }

    //对游戏动画进行加速（断线重连使用）
    public void speedUpAnimator() {
        isSpeedUp = true;
        Constants.DEALCARD_DURATION_TIME /= 10;
        Constants.UNDERPOKE_DURATION_TIME /= 10;
    }
    //对游戏动画进行复原
    public void backUpAnimator() {
        isSpeedUp = false;
        Constants.DEALCARD_DURATION_TIME *= 10;
        Constants.UNDERPOKE_DURATION_TIME *= 10;
    }

    //初始化elevation
    private void initEvelation() {
        //初始化elevation
        undercard_panel.setCardElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP));
        end_score_panel.setCardElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 54));
        end_panel_table.setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 54));
        end_panel_close.setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 54));
        score_panel.setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 50));
        score_button.setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 50));
        score_panel_table.setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 50));
        for (int i = 0; i < 4; i++) {
            fourLandPicbtn[i].setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 50));
            robots[i].setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 50));
            fourClock[i].setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 50));
        }
    }

    //初始化，find对应各控件
    private void initFindWidget() {
        //初始化landlord相对布局
        landlord_layout = (RelativeLayout) findViewById(R.id.landlord);
        //初始化四人的信息
        mes[0] = (TextView) findViewById(R.id.down_mes);
        mes[1] = (TextView) findViewById(R.id.right_mes);
        mes[2] = (TextView) findViewById(R.id.up_mes);
        mes[3] = (TextView) findViewById(R.id.left_mes);
        //初始化四人的显示信息
        four_action_pic[0] = (TextView) findViewById(R.id.down_score);
        four_action_pic[1] = (TextView) findViewById(R.id.right_score);
        four_action_pic[2] = (TextView) findViewById(R.id.up_score);
        four_action_pic[3] = (TextView) findViewById(R.id.left_score);
        //初始化机器人的信息
        robots[0] = (TextView) findViewById(R.id.down_robot);
        robots[1] = (TextView) findViewById(R.id.right_robot);
        robots[2] = (TextView) findViewById(R.id.up_robot);
        robots[3] = (TextView) findViewById(R.id.left_robot);
        //初始化不能出，出错信息
        breakrule = (TextView) findViewById(R.id.breakrule);
        cannotOutHint = (TextView) findViewById(R.id.cannotout);
        //初始化出牌三按钮
        outCardThreebtn[0] = (CardView) findViewById(R.id.outcard);
        outCardThreebtn[1] = (CardView) findViewById(R.id.refuse);
        outCardThreebtn[2] = (CardView) findViewById(R.id.hint);
        //初始化叫分四按钮
        callScoreFourbtn[0] = (CardView) findViewById(R.id.onescore);
        callScoreFourbtn[1] = (CardView) findViewById(R.id.twoscore);
        callScoreFourbtn[2] = (CardView) findViewById(R.id.threescore);
        callScoreFourbtn[3] = (CardView) findViewById(R.id.noscore);
        //初始化地主的四个头像
        fourLandPicbtn[0] = (TextView) findViewById(R.id.down_land);
        fourLandPicbtn[1] = (TextView) findViewById(R.id.right_land);
        fourLandPicbtn[2] = (TextView) findViewById(R.id.up_land);
        fourLandPicbtn[3] = (TextView) findViewById(R.id.left_land);
        //初始化四个人的图片
        peoplesImage[0] = (TextView)findViewById(R.id.down_people);
        peoplesImage[1] = (TextView)findViewById(R.id.right_people);
        peoplesImage[2] = (TextView)findViewById(R.id.up_people);
        peoplesImage[3] = (TextView)findViewById(R.id.left_people);
        //初始化四个人的时钟
        fourClock[0] = (CardView)findViewById(R.id.down_clock);
        fourClock[1] = (CardView)findViewById(R.id.right_clock);
        fourClock[2] = (CardView)findViewById(R.id.up_clock);
        fourClock[3] = (CardView)findViewById(R.id.left_clock);
        //初始化四个人的房主图标
        room_master[0] = (TextView)findViewById(R.id.down_room_master);
        room_master[1] = (TextView)findViewById(R.id.right_room_master);
        room_master[2] = (TextView)findViewById(R.id.up_room_master);
        room_master[3] = (TextView)findViewById(R.id.left_room_master);
        //初始化积分榜
        score_panel_table = (TableLayout)findViewById(R.id.score_panel_table);
        //初始化四个人说话的气泡
        String[] people = new String[]{"down", "right", "up", "left"};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                Field field = null;
                int id = 0;
                try {
                    field = R.id.class.getField(people[i] + "_" + Integer.toString(j + 1) + "_chat");
                    id = field.getInt(new R.id());
                    fourchat[i + j * 4] = (TextView) findViewById(id);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                fourchat[i + j * 4].setElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP + 50));
            }
        }
        //初始化开始按钮
        start_button = (CardView) findViewById(R.id.start);
        //初始化四个功能按钮和明牌的图标
        fourFunbtn[0] = (TextView) findViewById(R.id.robot);
        fourFunbtn[1] = (TextView) findViewById(R.id.order);
        fourFunbtn[2] = (TextView) findViewById(R.id.background_music);
        fourFunbtn[3] = (TextView) findViewById(R.id.soundeffect);
        brightCard = (TextView)findViewById(R.id.brightcard);
        //displayCard = (TextView)findViewById(R.id.displaycard);
        //初始化底分和倍数
        lbl_bottomscore = (TextView) findViewById(R.id.bottom);
        lbl_multiple = (TextView) findViewById(R.id.multiple);
        //初始化左上角底牌的图标
        undercard_panel = (CardView)findViewById(R.id.undercard);
        //初始化积分榜的各图标
        score_panel = (CardView)findViewById(R.id.score_panel);
        score_button = (CardView)findViewById(R.id.score_button);
        //初始化结束时积分榜的各图标
        end_score_panel = (CardView)findViewById(R.id.end_score_panel);
        end_panel_close = (TextView)findViewById(R.id.close);
        end_panel_table = (LinearLayout)findViewById(R.id.end_score_panel_table);
        end_panel_row[0] = (TableRow)findViewById(R.id.end_table_row1);
        end_panel_row[1] = (TableRow)findViewById(R.id.end_table_row2);
        end_panel_row[2] = (TableRow)findViewById(R.id.end_table_row3);
        end_panel_row[3] = (TableRow)findViewById(R.id.end_table_row4);
        //初始化各按钮的触摸变透明的监听事件
        TouchListener touchListener = new TouchListener();
        start_button.setOnTouchListener(touchListener);
        end_panel_close.setOnTouchListener(touchListener);
        brightCard.setOnTouchListener(touchListener);
        score_button.setOnTouchListener(touchListener);
        for (int i = 0; i < 4; i++) {
            fourFunbtn[i].setOnTouchListener(touchListener);
            callScoreFourbtn[i].setOnTouchListener(touchListener);
            if (i < 3) {
                outCardThreebtn[i].setOnTouchListener(touchListener);
            }
            peoplesImage[i].setOnTouchListener(touchListener);
            mes[i].setOnTouchListener(touchListener);
        }

        start_button.setBackground(getDrawable(R.mipmap.start_button));
        callScoreFourbtn[0].setBackground(getDrawable(R.mipmap.onescore_button));
        callScoreFourbtn[1].setBackground(getDrawable(R.mipmap.twoscore_button));
        callScoreFourbtn[2].setBackground(getDrawable(R.mipmap.threescore_button));
        callScoreFourbtn[3].setBackground(getDrawable(R.mipmap.noscore_button));
        outCardThreebtn[0].setBackground(getDrawable(R.mipmap.outcard_button));
        outCardThreebtn[1].setBackground(getDrawable(R.mipmap.refuse));
        outCardThreebtn[2].setBackground(getDrawable(R.mipmap.hint_button));
    }

    //刚进入游戏后初始化所有变量
    private void initVariable() {
        //初始化随机种子
        ran = new Random();
        //初始化音效
        soundEffect = GameStartActivity.getInstance().soundEffect;
        backgroundMusic = new BackgroundMusic(this);
        //初始化环境资源
        resources = getApplicationContext().getResources();
        //获取当前系统
        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        //初始化当前手机的长宽比
        d = wm.getDefaultDisplay();
        //初始化px版的card_interval
        card_interval = DensityUtil.dip2px(getApplicationContext(), Constants.CARD_INTERVAL);
        card_small_interval = DensityUtil.dip2px(getApplicationContext(), Constants.CARD_SMALL_INTERVAL);

        //初始化卡牌效果类
        pokeOperator = new PokeOperator(this);
        deckOperator = new DeckOperator(this);
        //初始化游戏的流程
        process = new GameProcess(isNetWork, this);

        //初始化时钟类
        clockManage = new Clock();
        //动画不处于加速中
        isSpeedUp = false;

        //初始化四人、牌堆、出牌的队列
        peopleOperator = new PeopleOperator(new LandLord_GameMode());
        //对玩家信息进行初始化
        for (int i = 0; i < 4; i++) {
            peoples[i] = new People(this);
        }

        //游戏重新开始时初始化所有的变量
        refreshAllVariable();

        //初始化elevation
        initEvelation();
    }
    //游戏重新开始时初始化所有的变量
    private void refreshAllVariable() {
        isHosting = false;    //机器人托管初始化
        whosLand = -1;       //初始化地主没有人
        multiple = 1;        //初始化当前局数的倍数
        integration[0] = 0;     //初始化当前局数的积分
        //初始化每个人的信息
        for (int i = 0; i < 4; i++) {
            mes[i].setText(peoples[i].name);
            peoples[i].Init_AfterTurn();//对4个队列进行初始化操作
        }
        cardpile.clear();  //清空逻辑底牌
    }
    //初始化刚进入游戏的显示状态
    private void initViewDisplay() {
        for (int i = 0; i < 8; i++) {
            landlord_layout.removeView(fourchat[i]); //四人说话窗体隐藏
        }
        process.scoreSettleProcess.endPanelScoreVisibility(false); //隐藏结束时的积分榜
        breakrule.setVisibility(View.INVISIBLE);//提示有问题隐藏
        cannotOutHint.setVisibility(View.INVISIBLE);//提示不能出隐藏
        for (int i = 0; i < 4; i++) {
            if (i < 3) {
                outCardThreebtn[i].setVisibility(View.GONE);//出牌三按钮已经隐藏
            }
            callScoreFourbtn[i].setVisibility(View.GONE);//叫分四按钮隐藏
            room_master[i].setVisibility(View.GONE);//房主的图标隐藏
            isDisplayChat[i] = false;//初始化气泡一开始都不在显示状态中
            robots[i].setVisibility(View.GONE);//初始化机器人
        }
        fourLandPicbtnVisibility(false);//四个地主的头像隐藏
        //如果是联网模式，关闭明牌
        if (isNetWork == true) {
            CardView brightCard2 = findViewById(R.id.brightcard2);
            brightCard2.setVisibility(View.GONE);
            brightCard.setVisibility(View.GONE);
        }
    }

    //按钮触摸监听
    // 实现点击后半透明，手指移开后恢复
    // 判断手指移开后，是否停留在原按钮上，没有停留则不相当于按
    class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:   //手指第一次触摸到屏幕
                    //第一次触碰透明度减半
                    v.setAlpha(0.5f);
                    break;
                case MotionEvent.ACTION_UP:     //放开后透明度还原，同时触发点击事件
                    v.setAlpha(1.0f);
                    Point mouseOff2 = new Point((int)event.getRawX(), (int)event.getRawY());
                    int []location = new int[2];
                    v.getLocationInWindow(location);
                    if (location[0] <= mouseOff2.x
                            && location[1] <= mouseOff2.y
                            && location[0] + v.getWidth() >= mouseOff2.x
                            && location[1] + v.getHeight() >= mouseOff2.y) {
                        switch (v.getId()) {
                            case R.id.robot:
                                robot_Click();
                                break;
                            case R.id.order:
                                order_Click();
                                break;
                            case R.id.background_music:
                                background_music_btn_Click();
                                break;
                            case R.id.soundeffect:
                                sound_effect_btn_Click();
                                break;
                            case R.id.start:
                                start_button_Click();
                                break;
                            case R.id.outcard:
                                outcard_Click();
                                break;
                            case R.id.refuse:
                                refuse_button_Click();
                                break;
                            case R.id.hint:
                                hint_button_Click();
                                break;
                            case R.id.onescore:
                                onescore_button_Click();
                                break;
                            case R.id.twoscore:
                                twoscore_button_Click();
                                break;
                            case R.id.threescore:
                                threescore_button_Click();
                                break;
                            case R.id.noscore:
                                noscore_button_Click();
                                break;
                            case R.id.close:
                                start_new_turn_click();
                                break;
                            case R.id.down_people:
                                fourPeopleClick(0);
                                break;
                            case R.id.right_people:
                                fourPeopleClick(1);
                                break;
                            case R.id.up_people:
                                fourPeopleClick(2);
                                break;
                            case R.id.left_people:
                                fourPeopleClick(3);
                                break;
                            case R.id.down_mes:
                                fourMesClick(0);
                                break;
                            case R.id.right_mes:
                                fourMesClick(1);
                                break;
                            case R.id.up_mes:
                                fourMesClick(2);
                                break;
                            case R.id.left_mes:
                                fourMesClick(3);
                                break;
                            case R.id.brightcard:
                                brightCardClick();
                                break;
                            case R.id.score_button:
                                score_button_click();
                                break;
                        }
                    }
                    break;
            }
            return true;
        }
    }

    //右上角积分按钮缩放的点击事件
    // 实现点击下移和上移的效果
    private void score_button_click() {
        int x = DensityUtil.dip2px(getApplicationContext(), 82);
        int start = isScoreUp ? -x : 0;
        int end = isScoreUp ? 0 : -x;
        //scorePanelFresh();  //使积分榜在控件前方
        CardAnimator.verticalRun(score_button, start, end, 1000);
        CardAnimator.verticalRun(score_panel_table, start, end, 1000);
        CardAnimator.verticalRun(score_panel, start, end, 1000);
        isScoreUp = !isScoreUp;
    }
    //初始化108张牌，和33张用来保存扑克位置信息的无图扑克
    private void InitalPoke() {
        //初始化108个标签
        pokeClickListener listener = new pokeClickListener();
        for (int i = 1; i < 109; i++) {
            poke[i] = new CardView(getApplicationContext());
            poke[i].setOnClickListener(listener);
            //设置最小长宽
            poke[i].setMinimumWidth(DensityUtil.dip2px(getApplicationContext(), Constants.CARD_SMALL_WIDTH));
            poke[i].setMinimumHeight(DensityUtil.dip2px(getApplicationContext(), Constants.CARD_SMALL_HEIGHT));
            poke[i].setCardElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP));

            //设置通过滑动来选择牌的效果（变为阴影，上移）
            poke[i].setOnTouchListener(pokeOperator);
        }
        for (int i = 1; i < 109; i++) {
            temptPoke[i] = new TextView(getApplicationContext());
            temptPoke[i].setWidth(DensityUtil.dip2px(this, Constants.CARD_WIDTH));
            temptPoke[i].setHeight(DensityUtil.dip2px(this, Constants.CARD_HEIGHT));
        }
        //初始化八张底牌
        for (int i = 0; i < 8; i++) {
            downpoke[i] = new CardView(getApplicationContext());
            downpoke[i].setCardElevation(DensityUtil.dip2px(getApplication(), Constants.ElEVATION_DP));
        }
    }
    //为牌设置监听事件
    private class pokeClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //将扑克牌放下或上提
            pokeOperator.pokeClick(v);
        }
    }
    //endregion

    //region 玩家出牌和电脑出牌、玩家按钮事件实现处
    //开始按钮的事件
    public void start_button_Click()  {
        process.startGameProcess.startPrepareGame();
    }
    //叫1分、2分、3分、不叫按钮的事件
    private void onescore_button_Click() {
        process.callScoreProcess.score_button_Click(1);
    }
    private void twoscore_button_Click() {
        process.callScoreProcess.score_button_Click(2);
    }
    private void threescore_button_Click() {
        process.callScoreProcess.score_button_Click(3);
    }
    private void noscore_button_Click() {
        process.callScoreProcess.score_button_Click(0);
    }

    //down玩家出牌
    private void outcard_Click() {
        process.outCardProcess.playerOutCard();
    }
    //将下家在上面的牌放下来
    private void putDownPoke() {
        process.outCardProcess.putDownPoke();
    }
    //提示出牌按钮
    private void hint_button_Click() {
        process.outCardProcess.playerHint();
    }
    //不出的按钮
    private void refuse_button_Click() {
        process.outCardProcess.playerRefuse();
    }

    //结束积分“差”按钮的事件，删除结束的积分界面，初始化各种变量，显示开始按钮
    private void start_new_turn_click() {
        process.scoreSettleProcess.start_new_turn_click();
    }
    //endregion

    //region 六个功能按钮和聊天显示处
    //托管的点击事件
    public void robot_Click() {
        process.outCardProcess.playerRobot();
    }
    //排序按钮的点击事件
    private void order_Click() {
        //若牌没发完，不能排序
        if (process.step.compareTo(GameProcessEnum.DEAL_CARD_PROCESS) > 0
                && process.step.compareTo(GameProcessEnum.SCORE_SETTLE_PROCESS) < 0) {

            peopleOperator.changeOrder(peoples[0]);
            deckOperator.movePeopleDeck(0);  //重新显示下家的牌
        }
        else {
            Toast.makeText(getApplicationContext(), "牌还未发完，不能更换排序！", Toast.LENGTH_SHORT).show();
        }
    }
    //背景音乐播放的点击事件
    private void background_music_btn_Click() {
        backgroundMusic.background_music_btn_Click();
    }
    //音效播放的点击事件
    private void sound_effect_btn_Click() {
        if (Constants.soundEffectMusicFlag == true) {
            isSoundEffectOpen = false;
            Constants.soundEffectMusicFlag = false;
            fourFunbtn[3].setBackground(resources.getDrawable(
                    R.mipmap.sound_effect_not_btn, getTheme()));
        } else {
            isSoundEffectOpen = true;
            Constants.soundEffectMusicFlag = true;
            fourFunbtn[3].setBackground(resources.getDrawable(
                    R.mipmap.sound_effect_btn, getTheme()));
        }
    }
    //明牌的点击事件
    private void brightCardClick() {
        if (isDisplayChat[0] == true) {    //只有当下家处在不说话中才能开启明牌
            Toast.makeText(getApplicationContext(), "说话过于频繁！请稍后重试！", Toast.LENGTH_SHORT).show();
        }
        else if (process.step == GameProcessEnum.DEAL_CARD_PROCESS) { //不能在发牌中开启明牌
            Toast.makeText(getApplicationContext(), "未发牌不能开启明牌模式！", Toast.LENGTH_SHORT).show();
        }
        else {
            if (isBrightCard == true)//如果为true则关闭明牌模式
            {
                isBrightCard = false;
                for (int i = 1; i < 4; i++) {
                    process.scoreSettleProcess.showAnimator_OverTurnThreeDark(i, 0);
                }
                show_fourpeoplechat(0, "关闭明牌！");
                show_fourpeoplechat(ran.nextInt(3) + 1, "这才对嘛！");
                brightCard.setBackground(resources.getDrawable(
                        R.mipmap.brightcard, getTheme()));
            }
            else    //如果为false则开启明牌模式
            {
                 isBrightCard = true;
                 for (int i = 1; i < 4; i++)
                     process.scoreSettleProcess.showAnimator_OverTurnThreeBright(i, peoples[i].deck.size() - 1);
                 soundEffect.Player_BrightCard_Voice(peoples[0].sex);
                 show_fourpeoplechat(0, "开启明牌！");
                 show_fourpeoplechat(ran.nextInt(3) + 1, "你竟然耍赖啊！");
                 brightCard.setBackground(resources.getDrawable(R.mipmap.blackcard, getTheme()));
            }
        }
    }
    //显示四人聊天
    Boolean []isDisplayChat = new Boolean[4];  //是否正处在显示气泡中
    public void show_fourpeoplechat(final int wh, String st) {
        //显示说话气泡
        fourchat[wh].setText(st);
        if (isDisplayChat[wh] == false) {   //如果气泡不处在显示中，则可以显示
            isDisplayChat[wh] = true;
            landlord_layout.addView(fourchat[wh]);
            landlord_layout.addView(fourchat[wh + 4]);
            CardAnimator.alphaRun(fourchat[wh], Constants.LIGHT_DURATION_TIME);
            CardAnimator.alphaRun(fourchat[wh + 4], Constants.LIGHT_DURATION_TIME);
        }
        int time_of_duration = st.length() * 200 > 2000 ? st.length() * 200 : 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CardAnimator.alphaGoneRun(fourchat[wh], Constants.LIGHT_DURATION_TIME, landlord_layout);
                CardAnimator.alphaGoneRun(fourchat[wh + 4], Constants.LIGHT_DURATION_TIME, landlord_layout);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isDisplayChat[wh] = false;
                    }
                }, Constants.LIGHT_DURATION_TIME);
            }
        }, time_of_duration);
    }
    //endregion

    //region 斗地主功能函数实现处
    //更新人的卡组信息
    public void update_mes_cardnum(int who) {
        //更新电脑卡组的数量
        mes[who].setText(peoples[who].name + "\n剩余牌：" + Integer.toString(peoples[who].deck.size()));
    }
    //计算当前的底分和倍数
    public void countBottomscoreMultiple() {
        lbl_bottomscore.setText("底分：" + Integer.toString(integration[0]));
        lbl_multiple.setText("倍数：" + Integer.toString((integration[0] / multiple)));
    }
    //清空所有变量和扑克牌和底牌，并进行初始化
    public void clearAll() {
        refreshAllVariable();
        removeAllPoke();
    }
    public void removeAllPoke() {
        //移除108张poke牌
        for (int i = 1; i < 109; i++) {
            if (poke[i] != null) {
                poke[i].setRotationY(0.0f);     //初始化有可能的翻转
                poke[i].setTranslationX(0.0f);
                poke[i].setTranslationY(0.0f);
                poke[i].setScaleX(1.0f);
                poke[i].setScaleY(1.0f);
                landlord_layout.removeView(poke[i]);
            }
        }
        //移除8张底牌
        for (int i = 0; i < 8; i++) {
            if (downpoke[i] != null) {
                landlord_layout.removeView(downpoke[i]);
            }
        }
    }
    //对四人地主的头像进行隐藏或者显示（true为显示，false为隐藏）
    private void fourLandPicbtnVisibility(boolean b) {
        if (b == true) {
            for (int i = 0; i < 4; i++) {
                fourLandPicbtn[i].setVisibility(View.VISIBLE);
                CardAnimator.alphaRun(fourLandPicbtn[i], Constants.LIGHT_DURATION_TIME);
            }
        }
        else {
            for (int i = 0; i < 4; i++) {
                CardAnimator.alphaGoneRun(fourLandPicbtn[i], Constants.LIGHT_DURATION_TIME);
            }
        }
    }
    //对叫分四按钮进行隐藏或者显示（true为显示，false为隐藏）
    public void callScoreFourbtnVisibility(boolean b) {
        if (b == true) {
            for (int i = 0; i < 4; i++) {
                callScoreFourbtn[i].setVisibility(View.VISIBLE);
                CardAnimator.alphaRun(callScoreFourbtn[i], Constants.LIGHT_DURATION_TIME);
            }
        } else {
            for (int i = 0; i < 4; i++) {
                CardAnimator.alphaGoneRun(callScoreFourbtn[i], Constants.LIGHT_DURATION_TIME);
            }
        }
    }
    //对出牌三按钮进行隐藏或者显示（true为显示，false为隐藏）
    public void outCardThreebtnVisibility(boolean b) {
        if (b == true) {
            for (int i = 0; i < 3; i++) {
                outCardThreebtn[i].setVisibility(View.VISIBLE);
                CardAnimator.alphaRun(outCardThreebtn[i], Constants.LIGHT_DURATION_TIME);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                CardAnimator.alphaGoneRun(outCardThreebtn[i], Constants.LIGHT_DURATION_TIME);
            }
        }
    }

    //监听home键，后台时关闭音乐和音效
    private void inithomeWatcher() {
        //监听home键，
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                backgroundMusic.pause();
                Constants.soundEffectMusicFlag = false;
            }

            @Override
            public void onHomeLongPressed() {
            }
        });
        mHomeWatcher.startWatch();
    }

    //四个人的图片更换（点击翻转）（翻转到一半时更换图片）
    private void fourPeopleClick(final int who) {
        if (!isNetWork) {
            int image;
            do {
                image = ran.nextInt(Constants.MAX_IMAGE + 1);
            } while (peoples[who].image == image);  //更换时跟上次不同
            peoples[who].image = image;
            if (image == Constants.MIN_IMAGE + 1) {
                peoples[who].sex = false;
            }
            else {
                peoples[who].sex = true;
            }
            //设置人物图片的更换
            rotateChangePeopleImage(who, image);
        }
        //网络模式下，只能在游戏准备时，设置电脑
        else if (process.step == GameProcessEnum.PREPARE_GAME_PROCESS) {
            //如果当前没有玩家，可以将其设置为电脑
            if (GlobalValue.players[peoples[who].netIndex] == null){
                ShowDialog.showSetRobotDialog(this, peoples[who].netIndex, true, this);
            }
            //或者取消将其设置为电脑
            else if (peoples[who].isRobot == true) {
                ShowDialog.showSetRobotDialog(this, peoples[who].netIndex, false, this);
            }
        }
    }

    //设置人物图片的更换
    private void rotateChangePeopleImage(final int who, int image) {
        Field field = null;
        int id = 0;
        try {
            //如果图片不合法，设置为机器人图片
            if (image > Constants.MAX_IMAGE || image < Constants.MIN_IMAGE) {
                id = R.mipmap.big_robot;
            } else {
                field = R.mipmap.class.getField("man" + Integer.toString(image));
                id = field.getInt(new R.mipmap());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        final int duration = (int)(Constants.PEOPLE_DURATION_TIME);
        CardAnimator.rotationYRun(peoplesImage[who], duration / 2, -180, -90, getApplicationContext());
        final int finalId = id;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                peoplesImage[who].setBackground(resources.getDrawable(finalId, getTheme()));
                CardAnimator.rotationYRun(peoplesImage[who], duration / 2, -90, 0, getApplicationContext());
            }
        }, (long) duration / 2);
    }

    //只有单机模式才能进行四个人的姓名更换
    private void fourMesClick(final int who) {
        if (!isNetWork) {
            //更换时跟上次不同
            String s;
            do {
                s = RandomName.getRandomName();
            } while (s == peoples[who].name);
            peoples[who].name = s;

            //将姓名180°旋转
            final int duration = (int)(Constants.PEOPLE_DURATION_TIME);
            CardAnimator.rotationYRun(mes[who], duration / 2, 180, 90, getApplicationContext());

            //旋转到一半更换姓名
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (process.step == GameProcessEnum.DEAL_CARD_PROCESS) {
                        update_mes_cardnum(who);
                    }
                    else {
                        mes[who].setText(peoples[who].name);
                    }
                    score_panel_score[who][0].setText(peoples[who].name);
                    end_panel_score[who][0].setText(peoples[who].name);
                    CardAnimator.rotationYRun(mes[who], duration / 2, 90, 0, getApplicationContext());
                }
            }, (long) duration / 2);
        }
    }
    //endregion

    //关闭窗体事件，将背景音乐和音效播放关闭
    @Override
    protected void onDestroy() {
        destroyLandlord();
        super.onDestroy();
    }
    //游戏结束后，对游戏进行处理
    private void destroyLandlord() {
        backgroundMusic.backgroundMusicShutDown();
        //如果游戏动画处于加速过程，要将其复原
        if (isSpeedUp) {
            backUpAnimator();
        }
        //如果当前时钟没关，将其关闭
        for (int i = 0; i < 4; i++) {
            clockManage.clockStopMes(i, this);
        }
    }

    //窗体回归事件，将背景音乐和音效播放恢复
    @Override
    protected void onResume() {
        if (isBackgroundOpen == true) {
            backgroundMusic.start();
        }
        if (isSoundEffectOpen == true) {
            Constants.soundEffectMusicFlag = true;
        }
        hideActionBar();
        super.onResume();
    }

    //监听返回按键
    boolean isTrueBack = false;
    @Override
    public void onBackPressed() {
        //先弹出框，等确定后，再进行返回
        if (isTrueBack == true) {
            isTrueBack = false;
            //取消监听home键
            mHomeWatcher.stopWatch();
            //如果是网络状态，退出需要向服务器发送消息，并且关闭socket连接
            if (isNetWork == true) {
                LoginRegisterDeal.leaveGame();
                destroyLandlord();
            }
            super.onBackPressed();
            return;
        } else {
            ShowDialog.showConfirmDialog(this, this);
        }
    }


    public final static int BACK_PRESSED = 0;
    public final static int SET_ROBOT = 1;
    public Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BACK_PRESSED:
                    isTrueBack = true;
                    onBackPressed();
                    break;

                case SET_ROBOT:
                    rotateChangePeopleImage(msg.arg1, msg.arg2);
                    break;
            }
        };
    };

}
