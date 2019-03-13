package wanganxin.com.poker.GameAnimation.GUI;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.widget.RelativeLayout;

import wanganxin.com.poker.R;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

/**
 * Created by Administrator on 2017/4/5.
 * 计算各个控件在界面的位置
 */

public class CardPosition
{
    //获取底牌的初始位置
    public static int getUnderPokePosition(Context context) {
        return DensityUtil.dip2px(context, Constants.UNDERCARD_LEFT);
    }
    //获取底牌卡的布局
    public static RelativeLayout.LayoutParams getUnderPokeLayoutParams(Context context) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                DensityUtil.dip2px(context, Constants.CARD_SMALL_WIDTH),
                DensityUtil.dip2px(context, Constants.CARD_SMALL_HEIGHT));
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.undercard);   //在底牌的框里面
        layoutParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.undercard);
        layoutParams.bottomMargin = DensityUtil.dip2px(context, Constants.UNDERCARD_BOTTOM);
        return layoutParams;
    }
    //获取下家牌的初始位置,如果isLeft == true算下家距离左边距离的位置，返回为位置，否则算最右边的距离
    public static int getDownPokePosition(Context context, Display d, int num, Boolean isLeft) {
        Point p = new Point();
        d.getRealSize(p);
        int card_weight = DensityUtil.dip2px(context, Constants.CARD_WIDTH);
        int card_interval = DensityUtil.dip2px(context, Constants.CARD_INTERVAL);
        int totalW = card_weight + (num - 1) * card_interval;
        if (isLeft == true) {
            return (int)Math.round(p.x / 2.0 - totalW / 2.0);
        }
        else {
            return (int)Math.round(p.x / 2.0 + totalW / 2.0 - card_weight);
        }
    }
    //获取下家牌的布局
    public static RelativeLayout.LayoutParams getDownPokeLayoutParams(Context context) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                DensityUtil.dip2px(context, Constants.CARD_WIDTH),
                DensityUtil.dip2px(context, Constants.CARD_HEIGHT));
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.multiple);   //在down人物信息的上方
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.bottomMargin = DensityUtil.dip2px(context, Constants.CARD_ORIGN_BOTTOM);
        return layoutParams;
    }

    //获取中间牌堆的初始位置,算中间牌堆距离上边距离的位置，返回为位置
    public static int getCentrePokePosition(Context context, Display d, int num) {
        Point p = new Point();
        d.getRealSize(p);
        int card_height = DensityUtil.dip2px(context, Constants.CARD_SMALL_HEIGHT);
        int card_interval = 1;
        int totalH = card_height + (num - 1) * card_interval;
        return (int)Math.round(p.y / 2.0 + totalH / 2.0 - card_height);
    }
    //获取中间牌堆的布局
    public static RelativeLayout.LayoutParams getCentrePokeLayoutParams(Context context, int num, Boolean isPoke) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                DensityUtil.dip2px(context, Constants.CARD_SMALL_WIDTH),
                DensityUtil.dip2px(context, Constants.CARD_SMALL_HEIGHT));
        if (isPoke == false) {
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);   //在水平正中间
            layoutParams.bottomMargin = num / 2;
        }
        else {
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);   //在正中间
        }
        return layoutParams;
    }

    //获取四家出牌的初始位置
    public static int getOutPokePosition(Context context, int who, Display d, int num) {
        Point pos = new Point();
        d.getRealSize(pos);
        int card_weight = DensityUtil.dip2px(context, Constants.CARD_SMALL_WIDTH);
        int card_interval = DensityUtil.dip2px(context, Constants.CARD_SMALL_INTERVAL);
        int totalW = card_weight + (num - 1) * card_interval;
        int position = 0;
        switch (who) {
            case 0:
                position = (int)Math.round(pos.x / 2.0 - totalW / 2.0) - card_interval;
                break;
            case 1:
                position = totalW + DensityUtil.dip2px(context, Constants.OUTCARD_HORIZENTAL_BOTTOM) - card_weight; //朝右的距离不包括它本身
                break;
            case 2:
                position = (int)Math.round(pos.x / 2.0 - totalW / 2.0);
                break;
            case 3:
                position = DensityUtil.dip2px(context, Constants.OUTCARD_HORIZENTAL_BOTTOM);
                break;
        }
        return position;
    }
    //获取四家出牌的布局
    public static RelativeLayout.LayoutParams getOutPokeLayoutParams(Context context, int who) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                DensityUtil.dip2px(context, Constants.CARD_SMALL_WIDTH),   //出的牌为小牌的尺寸
                DensityUtil.dip2px(context, Constants.CARD_SMALL_HEIGHT));
        switch (who) {
            case 0:
                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.start);   //与start信息低端对齐
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT); //距离父窗体的左边
                layoutParams.bottomMargin = DensityUtil.dip2px(context, Constants.OUTCARD_HORIZENTAL_BOTTOM + 2);
                break;
            case 1:
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);   //垂直居中
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.right_people); //在右侧人物的左边
                break;
            case 2:
                layoutParams.addRule(RelativeLayout.BELOW, R.id.up_mes);   //与上方人物的下侧
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT); //距离父窗体的左边
                layoutParams.topMargin = DensityUtil.dip2px(context, Constants.OUTCARD_HORIZENTAL_BOTTOM);
                break;
            case 3:
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);   //垂直居中
                layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.left_people); //在左侧人物的右边
                break;
        }
        return layoutParams;
    }

    //获取其余三家明牌的初始位置
    public static int getTurnOverPokePosition(Context context, int who, Display d, int num) {
        Point p = new Point();
        d.getRealSize(p);
        int card_weight = DensityUtil.dip2px(context, Constants.CARD_SMALL_WIDTH);
        int card_interval = DensityUtil.dip2px(context, Constants.CARD_SMALL_INTERVAL);
        int totalW = card_weight + (num - 1) * card_interval;
        int position = 0;
        switch (who) {
            case 1:
                position = 0; //朝右的距离不包括它本身
                break;
            case 2:
                position = card_interval + card_interval; //朝右的距离不包括它本身
                break;
            case 3:
                position = totalW - card_weight;
                break;
        }
        return position;
    }
    //获取其余三家明牌的布局
    public static RelativeLayout.LayoutParams getTurnOverPokeLayoutParams(Context context, int who) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                DensityUtil.dip2px(context, Constants.CARD_SMALL_WIDTH),   //出的牌为小牌的尺寸
                DensityUtil.dip2px(context, Constants.CARD_SMALL_HEIGHT));
        switch (who) {
            case 1:
                layoutParams.addRule(RelativeLayout.ABOVE, R.id.right_land); //在右侧地主上
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT); //靠近右侧屏幕
                layoutParams.bottomMargin = -DensityUtil.dip2px(context, Constants.CARD_VERTICAL_DIVERSION);
                break;
            case 2:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);   //靠近上侧屏幕
                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.up_people); //在上方人物的左侧
                layoutParams.topMargin = DensityUtil.dip2px(context, Constants.CARD_VERTICAL_DIVERSION);
                break;
            case 3:
                layoutParams.addRule(RelativeLayout.ABOVE, R.id.left_land); //在左侧地主上
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT); //靠近左侧屏幕
                layoutParams.bottomMargin = -DensityUtil.dip2px(context, Constants.CARD_VERTICAL_DIVERSION);
                break;
        }
        return layoutParams;
    }
}
