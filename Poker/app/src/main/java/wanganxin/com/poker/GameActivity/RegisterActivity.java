package wanganxin.com.poker.GameActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveEnum;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.LoginRegisterDeal;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameLogic.Operator.ShowDialog;
import wanganxin.com.poker.GameLogic.utilities.Constants;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton fab;       //回归登录的按钮
    @BindView(R.id.cv_add)
    CardView cvAdd;                 //注册整体的按钮
    @BindView(R.id.et_register_username)
    EditText etUsername;            //账户
    @BindView(R.id.et_register_password)
    EditText etPassword;            //密码
    @BindView(R.id.et_repeatpassword)
    EditText etRepeatpassword;       //重复输入的密码
    @BindView(R.id.bt_go)
    Button btRegister;              //点击注册的按钮

    public static RegisterActivity registerActivity = null;
    public static RegisterActivity getInstance() {return registerActivity;}

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

        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        registerActivity = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ShowEnterAnimation();
        }
    }

    @OnClick({R.id.bt_go, R.id.fab})
    public void onClick(View view) {
        switch (view.getId()) {
            //如果点击返回按钮
            case R.id.fab:
                animateRevealClose();
                break;

            //如果点击注册按钮
            case R.id.bt_go:
                startRegister();
                break;
        }
    }

    //开始注册
    private void startRegister() {
        //如果输入不合法，返回
        if (isInputIegal() == false) {
            return;
        }

        String username;        //注册用的用户名
        String password;        //注册用的密码

        username = etUsername.getText().toString();
        password = etPassword.getText().toString();

        //发送注册消息
        String msg = LoginRegisterDeal.startRegister(username, password);
        GameStartActivity client = GameStartActivity.getInstance();
        client.send(msg);

        //等待消息结果
        client.receiveDeal.nextStep = ClientReceiveEnum.LOGIN_RECEIVE_PROCESS;
        client.receiveDeal.initProcessReceiveUpdate();

        Log.e("1111", "REGISTER_SEND_PROCESS：发消息成功！-" + msg);
    }

    //判断输入是否合法
    private boolean isInputIegal() {
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
            //如果密码和重复输入的密码不一致
            else if (!etPassword.getText().toString().equals(etRepeatpassword.getText().toString())) {
                msg = "两次输入的密码不一致！";
                break;
            }
            //如果密码长度小于6位
            else if (etPassword.getText().toString().length() < 6) {
                msg = "密码长度不能小于六位！";
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

    //进入注册界面的动画
    private void ShowEnterAnimation() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.fabtransition);
        getWindow().setSharedElementEnterTransition(transition);

        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                cvAdd.setVisibility(View.GONE);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                animateRevealShow();
            }
            @Override
            public void onTransitionCancel(Transition transition) {}
            @Override
            public void onTransitionPause(Transition transition) {}
            @Override
            public void onTransitionResume(Transition transition) {}
        });
    }

    public void animateRevealShow() {
        Animator mAnimator = ViewAnimationUtils.createCircularReveal(
                cvAdd, cvAdd.getWidth()/2,0, fab.getWidth() / 2, cvAdd.getHeight());
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                cvAdd.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }
        });
        mAnimator.start();
    }

    //返回登录界面的动画
    public void animateRevealClose() {
        Animator mAnimator = ViewAnimationUtils.
                createCircularReveal(cvAdd,cvAdd.getWidth()/2,0, cvAdd.getHeight(), fab.getWidth() / 2);
        mAnimator.setDuration(500);
        mAnimator.setInterpolator(new AccelerateInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                cvAdd.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
                fab.setImageResource(R.drawable.plus);
                //关闭状态栏和虚拟按键
                ShowDialog.hideGameStartActionBar();
                RegisterActivity.super.onBackPressed();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        mAnimator.start();
    }
    @Override
    public void onBackPressed() {
        animateRevealClose();
    }

    public static final int REGISTER_SUCCESS = 0;
    public static final int USERNAME_EXIST = 1;

    //发送回调函数给注册
    public Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REGISTER_SUCCESS:
                    registerSuccess();
                    break;

                case USERNAME_EXIST:
                    usernameExist();
                    break;
            }
        };
    };

    //注册成功
    private void registerSuccess() {
        //显示注册成功
        ShowDialog.showSuccessDialog(this, "注册成功!");

        //将登录的账户名和密码写入登录窗口
        LoginActivity.getInstance().etUsername.setText(etUsername.getText().toString());
        LoginActivity.getInstance().etPassword.setText(etPassword.getText().toString());

        //返回原窗口
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animateRevealClose();
            }
        }, Constants.DIALOG_DURATION_TIME);
    }

    //用户名已经存在
    private void usernameExist() {
        ShowDialog.showErrorDialog(this, "当前用户名已存在!");
    }
}
