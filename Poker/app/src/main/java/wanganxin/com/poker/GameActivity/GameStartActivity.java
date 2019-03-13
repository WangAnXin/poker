package wanganxin.com.poker.GameActivity;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientSendDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.LoginRegisterDeal;
import wanganxin.com.poker.GameLogic.Operator.ShowDialog;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;
import wanganxin.com.poker.R;
import wanganxin.com.poker.Sound.GameEffectPlay;

public class GameStartActivity extends AppCompatActivity {
    CardView netModeCV;           //可选择联网模式的扑克
    CardView singleModeCV;        //可选择单机模式的扑克
    CardView matchCV;           //网络模式下开始匹配的扑克
    CardView infoCV;            //个人信息的的扑克
    TextView palm;              //握住扑克的手掌
    TextView finger;            //握住扑克的手指
    CardView shady;             //选择结束时的黑幕
    TextView leftButterfly;     //左边的蝴蝶
    TextView rightButterfly;    //右边的蝴蝶
    TextView pokerLogo;         //poker的Log

    //String ip = "10.82.197.132";   //设置当前连接的ip
    String ip = Constants.serverIp;     //腾讯云服务器
    int port = Constants.serverPort;             //设置当前连接的段可好

    //游戏播放的音效
    public GameEffectPlay soundEffect;

    //public ReconnectDeal reconnectDeal = null;      //断线重连的类
    boolean isNetWork = false;      //当前是否为联网模式
    private int baseElevation = 10;     //基础的elevation
    private static GameStartActivity gameStartActivity = null;

    public static GameStartActivity getInstance() {
        return gameStartActivity;
    }

    //记录是不是断线重连，如果是，等到建立LandlordActivity结束后，才可以接受消息
    public boolean isReconnected;

    //隐藏标题栏和虚拟按键
    private void hideActionBarAndDecorView() {
        //隐藏顶部的标题栏
        this.getSupportActionBar().hide();
        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onResume() {
        //隐藏标题栏和虚拟按键
        hideActionBarAndDecorView();
        super.onResume();
    }

    //初始化各控件
    private void initFindWidget() {
        //关联控件
        singleModeCV = (CardView)findViewById(R.id.left_select_cv);
        infoCV = singleModeCV;
        netModeCV = (CardView)findViewById(R.id.right_select_cv);
        matchCV = netModeCV;
        palm = (TextView)findViewById(R.id.palm);
        finger = (TextView)findViewById(R.id.finger);
        leftButterfly = (TextView)findViewById(R.id.left_butterfly);
        rightButterfly = (TextView)findViewById(R.id.right_butterfly);
        pokerLogo = (TextView)findViewById(R.id.poker_logo);
        //shady = (CardView)findViewById(R.id.shady);

        //设置控件图片和z轴位置
        singleModeCV.setBackground(getDrawable(R.mipmap.single));
        netModeCV.setBackground(getDrawable(R.mipmap.network));

        singleModeCV.setCardElevation(DensityUtil.dip2px(getApplication(), baseElevation + 3));
        netModeCV.setCardElevation(DensityUtil.dip2px(getApplication(), baseElevation + 2));
        palm.setElevation(DensityUtil.dip2px(getApplication(), baseElevation + 1));
        finger.setElevation(DensityUtil.dip2px(getApplication(), baseElevation + 4));
    }

    //region 初始化窗体，包括各个控件和变量的初始化
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏和虚拟按键
        hideActionBarAndDecorView();
        setContentView(R.layout.game_login_layout);

        //初始化各控件
        initFindWidget();

        //设置监听事件
        TouchListener touchListener = new TouchListener();
        netModeCV.setOnTouchListener(touchListener);
        singleModeCV.setOnTouchListener(touchListener);

        //初始化客户端发送给服务器和接受服务器信息的状态机
        gameStartActivity = this;
        sendDeal = new ClientSendDeal(gameStartActivity);
        receiveDeal = new ClientReceiveDeal(gameStartActivity);
        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.login_select_layout);
        soundEffect = new GameEffectPlay(getApplicationContext());
        isReconnected = false;      //当前不是断线重连模式

        final int duration = (int)(Constants.LIGHT_DURATION_TIME * 6);
        //手的动画
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(relativeLayout, "rotation", -60, 0)
                .setDuration(duration);
        animator.start();
        //蝴蝶淡入的动画
        CardAnimator.alphaRun(leftButterfly, duration * 3 / 2);
        CardAnimator.alphaRun(rightButterfly, duration * 3 / 2);
    }

    //还原原本图片
    private void backupSelected() {
        if (!isNetWork) {
            singleModeCV.setBackground(getDrawable(R.mipmap.single));
            netModeCV.setBackground(getDrawable(R.mipmap.network));
        } else {
            infoCV.setBackground(getDrawable(R.mipmap.info));
            matchCV.setBackground(getDrawable(R.mipmap.match));
        }
    }

    //按钮触摸监听
    // 实现点击后图片改变，手指移开后恢复
    // 判断手指移开后，是否停留在原按钮上，没有停留则不相当于按
    class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                //手指第一次触摸到屏幕
                case MotionEvent.ACTION_DOWN:
                    //播放按钮音效
                    soundEffect.pressButton();
                    //第一次触碰图片改变
                    switch (v.getId()) {
                        //设置右边选项被选中的效果
                        case R.id.right_select_cv:
                            if (!isNetWork) {
                                netModeCV.setBackground(getDrawable(R.mipmap.network_shader));
                            } else {
                                matchCV.setBackground(getDrawable(R.mipmap.match_shader));
                            }
                            break;
                        //设置左边选项被选中的效果
                        case R.id.left_select_cv:
                            if (!isNetWork) {
                                singleModeCV.setBackground(getDrawable(R.mipmap.single_shader));
                            } else {
                                infoCV.setBackground(getDrawable(R.mipmap.info_shader));
                            }
                            break;
                    }
                    break;

                //放开后透明度还原，同时触发点击事件
                case MotionEvent.ACTION_UP:
                    //还原图片
                    backupSelected();

                    //判断手指是否还在图标范围内
                    Point mouseOff2 = new Point((int)event.getRawX(), (int)event.getRawY());
                    int[] location = new int[2];
                    v.getLocationInWindow(location);
                    if (location[0] <= mouseOff2.x
                            && location[1] <= mouseOff2.y
                            && location[0] + v.getWidth() >= mouseOff2.x
                            && location[1] + v.getHeight() >= mouseOff2.y) {

                        switch (v.getId()) {
                            case R.id.right_select_cv:
                                if (!isNetWork) {
                                    //如果当前不是联网状态
                                    //初始化socket连接，连接成功后显示登录的画面，否则弹出错误
                                    initSocket();
                                } else {
                                    //如果为联网状态
                                    //开始匹配
                                    sendDeal.processSendUpdate();
                                }

                                break;

                            case R.id.left_select_cv:
                                if (!isNetWork) {
                                    //如果当前不是联网状态
                                    //单机模式直接从登录页面跳转到游戏页面
                                    Intent intent = new Intent(getApplicationContext(), LandlordActivity.class);
                                    intent.putExtra("isNetWork", false);
                                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(gameStartActivity).toBundle());
                                } else {
                                    //如果为联网状态
                                    //打开个人信息
                                    Intent intent = new Intent(getApplicationContext(), PlayerInfoActivity.class);
                                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(gameStartActivity).toBundle());
                                }
                                break;
                        }

                    }
                    break;
            }
            return true;
        }
    }

    public Socket clientSocket;        //与服务器连接的socket
    private boolean isReceivingMsgReady;        //是否准备接受数据
    private BufferedReader mReader;         //读数据流
    public BufferedWriter mWriter;         //写数据流
    //客户端发送流程
    public ClientSendDeal sendDeal;
    //客户端接收流程
    public ClientReceiveDeal receiveDeal;
    final static int READ = 0;
    final static int WRITE = 1;
    final static int LOGIN_ACTIVITY = 2;
    final static int CONNECT_FAIL = 3;
    final static int LOGIN_SUCCESS = 4;
    public final static int START_GAME = 5;

    //初始化socket连接，连接成功后显示登录的画面，否则弹出错误
    private void initSocket() {
        //开始心跳包的发送与检测
        //HeartbeatDeal.startHeartBeatSend();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //准备好接受消息
                    isReceivingMsgReady = true;

                    //在子线程中初始化Socket对象(设置5秒的连接超时时间)
                    clientSocket = new Socket();
                    SocketAddress socAddress = new InetSocketAddress(ip, port);
                    clientSocket.connect(socAddress, Constants.CONNECTED_TIMEOUT);

                    //根据clientSocket.getInputStream得到BufferedReader对象，从而从输入流中获取数据
                    mReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "utf-8"));
                    //根据clientSocket.getOutputStream得到BufferedWriter对象，从而从输出流中获取数据
                    mWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "utf-8"));

                    //连接成功后显示登录的画面
                    handler.obtainMessage(LOGIN_ACTIVITY).sendToTarget();

                    //接受服务器端的数据
                    while (isReceivingMsgReady) {
                        if (mReader.ready() == true) {
                            //上条消息没处理完，等待
                            //等待断线重连UI结束，开始，且不能在发牌阶段进行处理
                            while (receiveDeal.isDealFinish == false
                                    || isReconnected == true
                                    || receiveDeal.game != null && receiveDeal.game.reconMode > 0)
                                ;
                            //先将这条消息是否处理完设置成false
                            receiveDeal.isDealFinish = false;

                            String msg = mReader.readLine();

                            Log.e("1111", " reconMode " + (receiveDeal.game != null ?
                                    receiveDeal.game.reconMode + " step " + receiveDeal.game.process.step : "game空") +
                                    " msg " + msg);

                            //开始处理这条消息
                            handler.obtainMessage(READ, msg).sendToTarget();
                        }
                    }

                    //如果连接失败关闭写入和读入的流
                    mWriter.close();
                    mReader.close();
                    clientSocket.close();
                } catch (Exception e) {
                    //发送错误的弹框
                    handler.obtainMessage(CONNECT_FAIL).sendToTarget();
                    //e.printStackTrace();
                }

            }
        }).start();
    }

    public Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case READ:
                    //获取服务器传输的数据
                    receiveDeal.receiveMsg = (String)msg.obj;
                    //处理登录返回的结果
                    receiveDeal.processReceiveUpdate();
                    break;

                case WRITE:
                    Log.e("1111", "handleMessage: 写数据" + msg.obj);
                    break;

                case LOGIN_ACTIVITY:
                    //显示登录的画面
                    getWindow().setExitTransition(null);
                    getWindow().setEnterTransition(null);

                    startActivity(new Intent(getApplicationContext(), LoginActivity.class),
                            ActivityOptions.makeSceneTransitionAnimation(gameStartActivity).toBundle());
                    break;

                case CONNECT_FAIL:
                    //发送错误的弹框
                    ShowDialog.showErrorDialog(gameStartActivity, "连接失败");
                    break;

                case LOGIN_SUCCESS:
                    //登录成功后，更换选项（开始匹配，个人信息）
                    changeSelect();
                    break;

                case START_GAME:
                    //判断登录界面有没有关，如果没有关，先将其关闭
                    if (LoginActivity.getInstance() != null) {
                        LoginActivity.getInstance().finish();
                    }
                    //游戏开始
                    //从登录页面跳转到游戏页面
                    Intent intent = new Intent(gameStartActivity, LandlordActivity.class);
                    intent.putExtra("isNetWork", true);
                    if (msg.obj != null) {
                        intent.putExtra("isReconnected", (int)msg.obj);
                    }
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(gameStartActivity).toBundle());
                    //isReconnected = false;      //记录断线重连模式取消，证明LandlordActivity已创建
            }
        }

        ;
    };

    //通过动画，更换选项
    private void changeSelect() {
        //当前为联网模式
        isNetWork = true;
        final int duration = (int)(Constants.LIGHT_DURATION_TIME * 3);

        //更换角度
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(singleModeCV, "rotation", -10, 10)
                .setDuration(duration);
        animator.start();

        ObjectAnimator animator2 = ObjectAnimator
                .ofFloat(netModeCV, "rotation", 10, -10)
                .setDuration(duration);
        animator2.start();

        //更换水平的位置
        int end = DensityUtil.dip2px(getApplicationContext(), 144);
        CardAnimator.horizentalRun(singleModeCV, 0, end, duration);
        CardAnimator.horizentalRun(netModeCV, 0, -end, duration);

        //更换垂直的距离和更换显示
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                infoCV.setBackground(getDrawable(R.mipmap.info));
                matchCV.setBackground(getDrawable(R.mipmap.match));
                infoCV.setCardElevation(DensityUtil.dip2px(getApplication(), baseElevation + 2));
                matchCV.setCardElevation(DensityUtil.dip2px(getApplication(), baseElevation + 3));
            }
        }, duration / 2);
    }

    //向服务器发送消息
    protected void sendMsg(String msg) {
        if (mWriter != null) {
            try {
                //通过BufferedWriter对象向服务器写数据
                mWriter.write(msg + "\n");
                //一定要调用flush将缓存中的数据写到服务器
                mWriter.flush();
                //将发送的数据写入Log
                String str = "\n" + "我:" + msg + "   " + getTime(System.currentTimeMillis()) + "\n";
                handler.obtainMessage(WRITE, str).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //向服务器段发送数据
    public void send(final String msg) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                sendMsg(msg);
                return null;
            }
        }.execute();
    }

    /**
     * 得到自己定义的时间格式的样式
     *
     * @param millTime
     * @return
     */
    private String getTime(long millTime) {
        Date d = new Date(millTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(d));
        return sdf.format(d);
    }

    @Override
    public void onBackPressed() {
        //如果是网络模式，按下返回键之前应该先告诉服务器取消消息
        if (clientSocket != null && clientSocket.isConnected()) {
            LoginRegisterDeal.leaveGame();
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        soundEffect.releaseSoundPool();
        super.onBackPressed();
    }
}
