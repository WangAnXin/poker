package wanganxin.com.poker.GameActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveEnum;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.LoginRegisterDeal;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameLogic.Operator.ShowDialog;
import wanganxin.com.poker.GameLogic.utilities.Constants;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.et_username)
    EditText etUsername;            //账户
    @BindView(R.id.et_password)
    EditText etPassword;            //密码
    @BindView(R.id.ck_remberPass)
    CheckBox ck_remberPass;         //是否记住密码
    @BindView(R.id.ck_autoLogin)
    CheckBox ck_autoLogin;         //是否自动登录
    @BindView(R.id.bt_go)
    Button btGo;                    //点击登录的按钮
    @BindView(R.id.cv)
    CardView cv;                    //整个登录的背景
    @BindView(R.id.fab)
    FloatingActionButton fab;       //开启注册浮动的按钮

    Fade fade;      //设置淡入淡出的效果
    private static LoginActivity loginActivity = null;
    public static LoginActivity getInstance() {return loginActivity;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //设置淡入的进场效果
        fade = new Fade();
        fade.setDuration(Constants.LIGHT_DURATION_TIME);
        getWindow().setEnterTransition(fade);

        loginActivity = this;

        //如果该用户从前登录过，则从sharedPreferences中读取用户名和密码
        SharedPreferences pref = getSharedPreferences("loginData", MODE_PRIVATE);
        etUsername.setText(pref.getString("username", ""));
        etPassword.setText(pref.getString("password", ""));
        if (pref.getBoolean("autoLogin", false) == true) {
            startLogin();
        }
    }

    @OnClick({R.id.bt_go, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            //如果点击注册按钮
            case R.id.fab:
                startRegister();
                break;

            //如果点击登录按钮
            case R.id.bt_go:
                startLogin();
                break;
        }
    }

    //点击注册按钮
    private void startRegister() {
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this, fab, fab.getTransitionName());
            startActivity(new Intent(this, RegisterActivity.class), options.toBundle());
        } else {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    //点击登录按钮
    private void startLogin() {
        //如果输入不合法，返回
        if (isInputIegal() == false) {
            return;
        }

        //登录用的用户名
        String username = etUsername.getText().toString();
        //登录用的密码
        String password = etPassword.getText().toString();

        //发送登录消息
        String msg = LoginRegisterDeal.startLogin(username, password);
        GameStartActivity client = GameStartActivity.getInstance();
        client.send(msg);

        //等待消息结果
        client.receiveDeal.nextStep = ClientReceiveEnum.LOGIN_RECEIVE_PROCESS;
        client.receiveDeal.initProcessReceiveUpdate();

        Log.e("1111", "LOGIN_SEND_PROCESS：发消息成功！-" + msg);
    }

    //判断输入是否合法
    public boolean isInputIegal() {
        boolean isLegal = false;     //判断用户名和密码输入是否合法
        String msg = null;          //显示不合法的信息
        do {
            //如果用户名为空
            if (etUsername.getText().toString().equals("")) {
                msg = "用户名不能为空！";
                break;
            }
            //如果密码为空
            else if (etPassword.getText().toString().equals("")) {
                msg = "密码不能为空！";
                break;
            }
            //输入合法
            isLegal = true;
        } while (false);


        //如何当前输入不合法，提示错误，返回
        if (!isLegal) {
            ShowDialog.showErrorDialog(this, msg);
        }

        return isLegal;
    }

    public static final int NONE = -1;
    public static final int LOGIN_SUCCESS = 0;
    public static final int USER_NOT_EXIST = 1;
    public static final int USER_ALREADY_LOGIN = 2;
    public static final int PASSWORD_NOT_CORRECT = 3;

    //发送回调函数给登录
    public Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            //处理相应的结果
            switch (msg.what) {
                case LOGIN_SUCCESS: {
                    loginSuccess();
                }
                break;

                case USER_NOT_EXIST:
                    userNotExist();
                    break;

                case USER_ALREADY_LOGIN:
                    userAlreadyLogin();
                    break;

                case PASSWORD_NOT_CORRECT:
                    passwordNotCorrect();
                    break;
            }
        };
    };

    //登录窗口的弹框
    private SweetAlertDialog loginDialog = null;

    //登录成功
    private void loginSuccess() {
        //如果用户选择了保存密码，将用户名和密码保存到sharedPreferences中
        if (ck_remberPass.isChecked() == true
                && getSharedPreferences("loginData", MODE_PRIVATE).getBoolean("autoLogin", false) == false) {
            SharedPreferences.Editor editor = getSharedPreferences("loginData", MODE_PRIVATE).edit();
            editor.putBoolean("autoLogin", ck_autoLogin.isChecked());
            editor.putString("username", etUsername.getText().toString());
            editor.putString("password", etPassword.getText().toString());
            editor.apply();
        }

        loginDialog = ShowDialog.showSuccessDialog(this, "登录成功!");
        //如果是断线重连模式，则不显示登录成功的弹框
        if (GameStartActivity.getInstance().isReconnected == false) {
            //等待一段时间，显示登录成功的框
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ShowDialog.hideGameStartActionBar();
                    getWindow().setExitTransition(fade);
                    LoginActivity.super.onBackPressed();

                    //等待登录界面淡化动画效果结束后
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //返回登录成功的消息
                            GameStartActivity.getInstance().handler
                                    .obtainMessage(GameStartActivity.LOGIN_SUCCESS)
                                    .sendToTarget();
                        }
                    }, Constants.LIGHT_DURATION_TIME);
                }
            }, Constants.DIALOG_DURATION_TIME);
        }
    }
    //用户已经登录
    private void userAlreadyLogin() {
        ShowDialog.showErrorDialog(this, "用户已经登录!");
    }
    //用户不存在
    private void userNotExist() {
        ShowDialog.showErrorDialog(this, "用户不存在!");
    }
    //密码不正确
    private void passwordNotCorrect() {
        ShowDialog.showErrorDialog(this, "密码不正确!");
    }

    @Override
    public void finish() {
        //如果弹框在窗口销毁前未关闭，将其关闭
        if (loginDialog != null) {
            loginDialog.dismiss();
        }
        super.finish();
    }
}
