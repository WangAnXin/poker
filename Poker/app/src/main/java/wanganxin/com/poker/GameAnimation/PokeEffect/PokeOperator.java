package wanganxin.com.poker.GameAnimation.PokeEffect;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.GameLogic.GameProcess.GameProcessEnum;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameAnimation.GUI.CardAnimator;
import wanganxin.com.poker.GameLogic.utilities.Constants;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

public class PokeOperator implements View.OnTouchListener {
    //传主页面
    private LandlordActivity game;

    public PokeOperator(LandlordActivity game) {
        this.game = game;
    }

    //通过滑动来选择牌的效果（变为阴影，上移）
    private int startX;
    private int startY;
    private Point p = new Point();
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (game.process.step.compareTo(GameProcessEnum.DEAL_CARD_PROCESS) > 0
                && game.process.step.compareTo(GameProcessEnum.SCORE_SETTLE_PROCESS) < 0) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:   //手指第一次触摸到屏幕
                    startX = (int)event.getRawX();
                    startY = (int)event.getRawY();
                    game.d.getRealSize(p);
                    break;
                case MotionEvent.ACTION_MOVE:   //手指移动
                    //鼠标在手牌有效范围内
                    int num2;
                    Point mouseOff1 = new Point(startX, startY);
                    Point mouseOff2 = new Point((int)event.getRawX(), (int)event.getRawY());
                    if (mouseOff1.x > mouseOff2.x)
                    {
                        Point temp = mouseOff1;
                        mouseOff1 = mouseOff2;
                        mouseOff2 = temp;
                    }
                    for (int i = game.peoples[0].deck.size() - 1; i >= 0; i--) {
                        num2 = game.peoples[0].deck.get(i).cardIndex;
                        if (game.poke[num2].getLeft() < mouseOff2.x
                                && game.poke[num2].getLeft() > mouseOff1.x
                                || game.poke[num2].getLeft() < mouseOff1.x
                                && mouseOff1.x - game.poke[num2].getLeft() < Constants.CARD_INTERVAL)
                        {
                            setShadowPokePicture(num2);
                        }
                        else
                        {
                            setPokePicture(num2, false, false);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:  //松开手让牌变成原本的模样
                    for (int i = 0; i < game.peoples[0].deck.size(); i++) {
                        setPokePicture(game.peoples[0].deck.get(i).cardIndex, false, false);
                    }

                    int num;
                    mouseOff1 = new Point(startX, startY);
                    mouseOff2 = new Point((int)event.getRawX(), (int)event.getRawY());
                    if (mouseOff1.x > mouseOff2.x)
                    {
                        Point temp = mouseOff1;
                        mouseOff1 = mouseOff2;
                        mouseOff2 = temp;
                    }
                    for (int i = game.peoples[0].deck.size() - 1; i >= 0; i--) {
                        num = game.peoples[0].deck.get(i).cardIndex;
                        if (game.poke[num].getLeft() < mouseOff2.x
                                && game.poke[num].getLeft() > mouseOff1.x
                                || game.poke[num].getLeft() < mouseOff1.x
                                && mouseOff1.x - game.poke[num].getLeft() < Constants.CARD_INTERVAL)
                        {
                            pokeClick(game.poke[num]);
                        }
                    }
                    break;
            }
        }
        return false;
    }

    //设置扑克牌的图片，num为哪张扑克牌，isDownPoke为是不是底牌
    public void setPokePicture(int num, Boolean isCardBack, Boolean isDownPoke) {
        Field field;
        int id = 0;
        if (isCardBack == true) //如果是卡牌背面
        {
            id = R.mipmap.cardback;
        }
        else//如果不是卡牌背面，则用相应的牌
        {
            int curPokeNum;         //扑克牌图片渲染编号
            if (isDownPoke == false) {
                curPokeNum = num;
            } else {
                curPokeNum = game.cardpile.get(num).cardIndex;
            }
            try {
                field = R.mipmap.class.getField("poke_" + Integer.toString(curPokeNum > 54 ? curPokeNum - 54 : curPokeNum));
                id = field.getInt(new R.mipmap());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Drawable btnDrawable = game.resources.getDrawable(id, game.getTheme());
        if (isDownPoke == false) {
            game.poke[num].setAlpha(1.0f);   //消除淡出效果
            game.poke[num].setRotationY(0f); //消除旋转效果
            game.poke[num].setVisibility(View.VISIBLE);
            game.poke[num].setBackground(btnDrawable);
        }
        else {
            game.downpoke[num].setBackground(btnDrawable);
        }
    }

    //设置扑克牌的翻转图片
    public void setConversePokePicture(int num, Boolean isDownPoke) {
        Field field;
        int id = 0;
        try {
            int curPokeNum;     //扑克牌图片渲染编号
            if (isDownPoke == false) {
                curPokeNum = num;
            } else {
                curPokeNum = game.cardpile.get(num).cardIndex;
            }
            field = R.mipmap.class.getField("converse_poke_" + Integer.toString(curPokeNum > 54 ? curPokeNum - 54 : curPokeNum));
            id = field.getInt(new R.mipmap());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Drawable btnDrawable = game.resources.getDrawable(id, game.getTheme());
        if (isDownPoke == false) {
            game.poke[num].setVisibility(View.VISIBLE);
            game.poke[num].setBackground(btnDrawable);
        }
        else {
            game.downpoke[num].setBackground(btnDrawable);
        }
    }

    //设置扑克牌的阴影图片
    public void setShadowPokePicture(int num) {
        Field field;
        int id = 0;
        try {
            field = R.mipmap.class.getField("poke_shadow_" + Integer.toString(num > 54 ? num - 54 : num));
            id = field.getInt(new R.mipmap());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Drawable btnDrawable = game.resources.getDrawable(id, game.getTheme());
        game.poke[num].setBackground(btnDrawable);
    }

    //对牌的点击事件
    public void pokeClick(View v) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

        if (layoutParams.bottomMargin == DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_ADD_BOTTOM)) {
            //将扑克下提
            pokeUpDown(v, false);
        }
        else if(layoutParams.bottomMargin == DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_ORIGN_BOTTOM)) {
            //将扑克上提
            pokeUpDown(v, true);
        }

        v.setLayoutParams(layoutParams);
    }

    //将View选中的扑克上提或下提，isUp==true就是上提
    public void pokeUpDown(View v, Boolean isUp) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();

        if (isUp == true) {
            //将牌上提的动画
            CardAnimator.verticalOutRun(v,
                    0,
                    DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_ADD_BOTTOM) - layoutParams.bottomMargin
                    , Constants.CLICKPOKE_DURATION_TIME);
        } else {
            //将牌下题的动画
            CardAnimator.verticalOutRun(v,
                    0,
                    DensityUtil.dip2px(game.getApplicationContext(), Constants.CARD_ORIGN_BOTTOM) - layoutParams.bottomMargin
                    , Constants.CLICKPOKE_DURATION_TIME);
        }
    }
}
