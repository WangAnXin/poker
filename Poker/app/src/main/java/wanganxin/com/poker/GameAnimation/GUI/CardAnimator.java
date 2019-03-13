package wanganxin.com.poker.GameAnimation.GUI;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;


/**
 * Created by Administrator on 2017/4/19.
 * 四人斗地主游戏动画设计
 */

public class CardAnimator {
    /**
     * 地主发八张牌的动画
     * @param view
     */
    //垂直移动的动画
    public static void verticalRun(final View view, float start, float end, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setTarget(view);
        animator.setDuration(duration).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationY((Float) animation.getAnimatedValue());
            }
        });
    }
    //水平移动的动画
    public static void horizentalRun(final View view, float start, float end, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setTarget(view);
        animator.setDuration(duration).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setTranslationX((Float) animation.getAnimatedValue());
            }
        });
    }

    //发牌时平移的效果
    public static void horizentalRun(final View view, float start, float end, long duration, final Boolean isLeftMarign) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end); //设置平移效果
        animator.setTarget(view);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        final int orignMarign = isLeftMarign ? layoutParams.leftMargin : layoutParams.rightMargin;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                if (isLeftMarign) {
                    layoutParams.leftMargin = (int)(orignMarign + (Float)animation.getAnimatedValue());
                }
                else {
                    layoutParams.rightMargin = (int)(orignMarign + (Float)animation.getAnimatedValue());
                }
                view.setLayoutParams(layoutParams);
            }
        });
        AnimatorSet animSet = new AnimatorSet();    //将效果同时使用
        animSet.playTogether(animator);
        animSet.setDuration(duration);
        animSet.start();
    }
    //单击牌上移和下移的效果
    public static void verticalOutRun(final View view, float start, float end, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end); //设置平移效果
        animator.setTarget(view);
        animator.setDuration(duration);
        animator.start();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        final int orignMarign = layoutParams.bottomMargin;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.bottomMargin = (int)(orignMarign + (Float)animation.getAnimatedValue());
                view.setLayoutParams(layoutParams);
            }
        });
    }
    public static void verticalOutRunTopMargin(final View view, float start, float end, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end); //设置平移效果
        animator.setTarget(view);
        animator.setDuration(duration);
        animator.start();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        final int topMargin = layoutParams.topMargin;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                layoutParams.topMargin = (int)(topMargin - (Float)animation.getAnimatedValue());
                view.setLayoutParams(layoutParams);
            }
        });
    }
    //淡入的效果
    public static void alphaRun(final View view, long duration) {
        float orignAlpha = 0f;    //设置淡入的效果
        view.setVisibility(View.VISIBLE);
        view.setAlpha(orignAlpha);
        ValueAnimator animator2 = ValueAnimator.ofFloat(orignAlpha, 1.0f);
        animator2.setDuration(duration);
        animator2.setTarget(view);
        animator2.start();
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((Float)animation.getAnimatedValue());
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);//设置淡入效果
                view.setEnabled(true);
            }
        }, duration);
    }
    //淡出的效果(最终会被layoutRemove掉)
    public static void alphaGoneRun(final View view, long duration, final RelativeLayout layout) {
        view.setEnabled(false);//淡出中不能点击
        ValueAnimator animator2 = ValueAnimator.ofFloat(1.0f, 0f);
        animator2.setDuration(duration);
        animator2.setTarget(view);
        animator2.start();
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                view.setAlpha((Float)animation.getAnimatedValue());
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layout.removeView(view);//设置淡出效果
                view.setEnabled(true);
            }
        }, duration);
    }
    //淡出的效果(最终会被设置为不可见)
    public static void alphaGoneRun(final View view, long duration) {
        view.setEnabled(false); //淡出中不能点击
        ValueAnimator animator2 = ValueAnimator.ofFloat(1.0f, 0f);
        animator2.setDuration(duration);
        animator2.setTarget(view);
        animator2.start();
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setAlpha((Float)animation.getAnimatedValue());
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.INVISIBLE);//设置淡出效果
                view.setEnabled(true);
            }
        }, duration);
    }


    //横向翻转的效果
    public static void rotationYRun(final View view, long duration, float start, float end) {
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(view, "rotationY", start,  end)
                .setDuration(duration);
        animator.start();
        view.setElevation(view.getElevation() + 1);
        view.setCameraDistance(90 * 30 * 3);
    }
    public static void rotationYRun(final View view, long duration, float start, float end, final Context context) {
        ValueAnimator animator2 = ValueAnimator.ofFloat(start, end);
        animator2.setDuration(duration);
        animator2.setTarget(view);
        animator2.start();
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                view.setRotationY((Float)animation.getAnimatedValue());
            }
        });
        float distance = 30000.0f;
        float scale = context.getResources().getDisplayMetrics().density * distance;
        view.setCameraDistance(scale);
    }

    //出牌的效果
    public static void OutCardRun(final View fromView, final View toView, long duration, int who) {
        //设置平移效果
        ObjectAnimator animatorH = ObjectAnimator
                .ofFloat(fromView, "translationX", 0F,  (float)(toView.getLeft() - fromView.getLeft()))
                .setDuration(duration);
        //设置竖直移动效果
        ValueAnimator animator = ValueAnimator.ofFloat(0F, (float)(fromView.getTop() - toView.getTop())); //设置平移效果
        animator.setTarget(fromView);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fromView.getLayoutParams();
        final int orignMarign = layoutParams.bottomMargin;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) fromView.getLayoutParams();
                layoutParams.bottomMargin = (int)(orignMarign + (Float)animation.getAnimatedValue());
                fromView.setLayoutParams(layoutParams);
            }
        });
        ObjectAnimator animatorV = ObjectAnimator
                .ofFloat(fromView, "translationY", 0F,  (float)(toView.getTop() - fromView.getTop()))
                .setDuration(duration);
        //设置水平缩小
        ObjectAnimator animX = ObjectAnimator
                .ofFloat(fromView, "scaleX", 1.0F,  (float)(1.0 * toView.getWidth() / fromView.getWidth()))
                .setDuration(duration);
        //设置竖直缩小
        ObjectAnimator animY = ObjectAnimator
                .ofFloat(fromView, "scaleY", 1.0F,  (float)(1.0 * toView.getHeight() / fromView.getHeight()))
                .setDuration(duration);
        AnimatorSet animSet = new AnimatorSet();    //将效果同时使用
        if (who == 0) {
            animSet.playTogether(animator, animatorH, animX, animY);
        }
        else {
            animSet.playTogether(animatorV, animatorH, animX, animY);
        }
        animSet.setDuration(duration);
        animSet.start();
    }
}
