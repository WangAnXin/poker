package wanganxin.com.poker.GameActivity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wanganxin.com.poker.GameLogic.ClientProcess.ClientReceiveDeal;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.MatchDeal;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.Operator.ShowDialog;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.entity.Player;

public class PlayerInfoActivity extends AppCompatActivity {

    @BindView(R.id.player_name)
    EditText playerName;            //玩家名
    @BindView(R.id.player_pic)
    TextView playerPic;            //玩家的图片
    @BindView(R.id.player_sex_rg)
    RadioGroup playerSex;          //玩家的性别
    @BindView(R.id.player_score)
    TextView playerScore;          //玩家的积分

    Fade fade;      //设置淡入淡出的效果
    private int curImageIndex = 0;      //当前玩家图片的编号

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

        setContentView(R.layout.activity_playerinfo);
        ButterKnife.bind(this);

        //设置淡入的进场效果
        fade = new Fade();
        fade.setDuration(Constants.LIGHT_DURATION_TIME);
        getWindow().setEnterTransition(fade);

        initPlayerDisplay();
    }

    //初始化玩家的显示信息
    private void initPlayerDisplay() {
        //获取登录后玩家的信息
        Player player = ClientReceiveDeal.player;

        //初始化玩家的信息
        playerName.setText(player.name);
        playerScore.setText(String.valueOf(player.score));
        playerSex.check(player.sex ? R.id.player_isMale : R.id.player_isFemale);
        curImageIndex = player.image - 1;
        playerPicChange();
    }

    //玩家图片更换（点击翻转）（翻转到一半时更换图片）
    @OnClick(R.id.player_pic)
    public void playerPicChange() {
        curImageIndex = (curImageIndex + 1) % 6;

        Field field = null;
        int id = 0;
        try {
            field = R.mipmap.class.getField("man" + Integer.toString(curImageIndex));
            id = field.getInt(new R.mipmap());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        final int duration = (int)(Constants.PEOPLE_DURATION_TIME);
        CardAnimator.rotationYRun(playerPic, duration / 2, -180, -90, getApplicationContext());
        final int finalId = id;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                playerPic.setBackground(getDrawable(finalId));
                CardAnimator.rotationYRun(playerPic, duration / 2, -90, 0, getApplicationContext());
            }
        }, (long) duration / 2);
    }

    //点击保存，发送数据给服务器，并改变目前player的状态
    @OnClick(R.id.bt_save)
    public void saveChange() {
        ClientReceiveDeal.player.name = playerName.getText().toString();
        ClientReceiveDeal.player.sex = playerSex.getCheckedRadioButtonId() == R.id.player_isMale;
        ClientReceiveDeal.player.image = curImageIndex;

        String msg = MatchDeal.getChangePlayerInfoMsg(ClientReceiveDeal.player);
        GameStartActivity client = GameStartActivity.getInstance();
        client.send(msg);

        Log.e("1111", "CHANGE_INFO_PROGRESS：发消息成功！-" + msg);

        //显示修改成功
        ShowDialog.showSuccessDialog(this, "保存成功!");
        //等待一段时间，显示登录成功的框
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ShowDialog.hideGameStartActionBar();
                getWindow().setExitTransition(fade);
                PlayerInfoActivity.super.onBackPressed();
            }
        }, Constants.DIALOG_DURATION_TIME);
    }
}
