package wanganxin.com.poker.GameLogic.Operator;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cn.pedant.SweetAlert.SweetAlertDialog;
import wanganxin.com.poker.GameLogic.ClientProcess.ProcessDeal.PrepareGameDeal;
import wanganxin.com.poker.GameActivity.GameStartActivity;
import wanganxin.com.poker.GameActivity.LandlordActivity;

//将Sweet-alert-dialog进一步封装
public class ShowDialog {

    //发送错误的弹框
    public static void showErrorDialog(final AppCompatActivity activity, String msg) {
        //发送错误的弹框
        new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("错误...")
                .setContentText(msg)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        //隐藏标题栏和虚拟按键
                        hideGameStartActionBar();
                    }
                })
                .show();
    }

    //发送成功的弹框
    public static SweetAlertDialog showSuccessDialog(final AppCompatActivity activity, String msg) {
        //发送错误的弹框
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("成功")
                .setContentText(msg)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        //隐藏标题栏和虚拟按键
                        hideGameStartActionBar();
                    }
                });
        sweetAlertDialog.show();

        return sweetAlertDialog;
    }

    //发送是否确定退出的弹框（结束后调用）
    public static void showConfirmDialog(final AppCompatActivity activity, final LandlordActivity game) {
        new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("确定要退出吗?")
                .setContentText("将退出当前的游戏!")
                .setCancelText("不退出了")
                .setConfirmText("是的!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        // reuse previous dialog instance, keep widget user state, reset them if you need
                        sDialog.setTitleText("继续游戏!")
                                .setContentText("祝你本局游戏愉快 :)")
                                .setConfirmText("好的")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        hideLandlordActionBar(game);
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        hideLandlordActionBar(game);
                        sDialog.cancel();
                        game.handler.obtainMessage(LandlordActivity.BACK_PRESSED).sendToTarget();
                    }
                })
                .show();
    }

    //发送是否确定设置为电脑的弹框
    public static void showSetRobotDialog(final AppCompatActivity activity, final int who, final boolean isRobot, final LandlordActivity game) {
        String title = isRobot ? "要添加电脑吗?" : "要取消设置电脑吗?";
        new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("设置")
                .setContentText(title)
                .setCancelText("取消")
                .setConfirmText("是的")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        hideLandlordActionBar(game);
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        //发送消息给服务器，设置当前玩家为电脑，或者取消电脑
                        GameStartActivity.getInstance().send(PrepareGameDeal.setPlayer2Robot(who, isRobot));
                        hideLandlordActionBar(game);
                        sDialog.cancel();
                    }
                })
                .show();
    }

    //隐藏LandlordActivity标题栏和虚拟按键
    public static void hideLandlordActionBar(LandlordActivity game) {
        game.getSupportActionBar().hide();
        //隐藏虚拟按键
        View decorView = game.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    //隐藏GameStartActivity标题栏和虚拟按键
    public static void hideGameStartActionBar() {
        GameStartActivity.getInstance().getSupportActionBar().hide();
        //隐藏虚拟按键
        View decorView = GameStartActivity.getInstance().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
