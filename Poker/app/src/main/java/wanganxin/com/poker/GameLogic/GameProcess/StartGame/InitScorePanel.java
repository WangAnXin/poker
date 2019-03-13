package wanganxin.com.poker.GameLogic.GameProcess.StartGame;

import android.graphics.Point;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;
import wanganxin.com.poker.GameLogic.utilities.DensityUtil;

public class InitScorePanel {
//    private static InitScorePanel initScorePanel = null;
//
//    //游戏最后结算设置单例模式
//    public static InitScorePanel getInstance() {
//        if (initScorePanel == null) {
//            initScorePanel = new InitScorePanel();
//        }
//        initScorePanel.game = LandlordActivity.getInstance();
//        return initScorePanel;
//    }

    //获取当前进行的游戏
    private LandlordActivity game;

    public InitScorePanel(LandlordActivity game) {
        this.game = game;
    }

    //初始化积分榜
    public void InitalScore_panel() {
        //设置行，高度按比例
        TableRow row = new TableRow(game.getApplicationContext());
        row.setLayoutParams(
                new TableRow.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1.0f));

        row.addView(new TextView(game.getApplicationContext()));

        //先添加一个空行
        game.score_panel_table.addView(row);

        //添加剩余四行的文本控件
        for (int rowi = 0; rowi < 4; rowi++) {
            //创建一个新的行
            row = new TableRow(game.getApplicationContext());
            row.setLayoutParams(
                    new TableRow.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1.0f));
            game.score_panel_score[rowi] = new TextView[3];

            //每行放入三个信息，姓名，上局得分，总分数
            for (int columni = 0; columni < 3; columni++) {
                game.score_panel_score[rowi][columni] = new TextView(game.getApplicationContext());
                game.score_panel_score[rowi][columni].setLayoutParams(
                        new TableRow.LayoutParams(
                                0,
                                TableLayout.LayoutParams.MATCH_PARENT,
                                1.0f));

                //设置文本居中
                game.score_panel_score[rowi][columni].setGravity(Gravity.CENTER);
                //设置文本颜色为白色
                game.score_panel_score[rowi][columni].setTextColor(game.getResources().getColor(R.color.white));
                //设置文本颜色为白色
                game.score_panel_score[rowi][columni].setTextSize(10f);
                row.addView(game.score_panel_score[rowi][columni]);
            }
            //添加一个新行，有三个文本信息，姓名，上局得分，总分数
            game.score_panel_table.addView(row);
        }
        //初始化积分榜为向上隐藏的状态
        game.isScoreUp = true;
        int x = DensityUtil.dip2px(game.getApplicationContext(), 82);
        game.score_button.setTranslationY(-x);
        game.score_panel_table.setTranslationY(-x);
        game.score_panel.setTranslationY(-x);
    }

    //初始化结束时的积分榜
    public void InitEnd_Score_panel() {
        for (int rowi = 0; rowi < 4; rowi++) {  //添加剩余四行的文本控件
            game.end_panel_score[rowi] = new TextView[3];
            for (int columni = 0; columni < 3; columni++) {     //每行放入三个信息，姓名，上局得分，总分数
                game.end_panel_score[rowi][columni] = new TextView(game.getApplicationContext());
                game.end_panel_score[rowi][columni].setLayoutParams(
                        new TableRow.LayoutParams(
                                0,
                                TableLayout.LayoutParams.MATCH_PARENT,
                                1.0f));
                game.end_panel_score[rowi][columni].setGravity(Gravity.CENTER);    //设置文本居中
                game.end_panel_score[rowi][columni].setTextColor(game.resources.getColor(R.color.white));  //设置文本颜色为白色
                game.end_panel_score[rowi][columni].setTextSize(11f);  //设置文本颜色为白色
                game.end_panel_score[rowi][columni].getPaint().setFakeBoldText(true);
                game.end_panel_row[rowi].addView(game.end_panel_score[rowi][columni]);
            }
        }

        //实现结算榜的拖拽
        game.end_score_panel.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;
            Point p = new Point();
            RelativeLayout.LayoutParams layoutParams;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:   //手指第一次触摸到屏幕
                        game.end_score_panel.setAlpha(0.5f);
                        startX = (int)event.getRawX();
                        startY = (int)event.getRawY();
                        game.d.getRealSize(p);
                        break;
                    case MotionEvent.ACTION_MOVE:   //手指移动
                        int newX = (int)event.getRawX();
                        int newY = (int)event.getRawY();
                        int dx = newX - startX;
                        int dy = newY - startY;

                        //计算出控件原来的位置
                        int l = game.end_score_panel.getLeft();
                        int r = game.end_score_panel.getRight();
                        int t = game.end_score_panel.getTop();
                        int b = game.end_score_panel.getBottom();

                        int newt = t + dy;
                        int newb = b + dy;
                        int newl = l + dx;
                        int newr = r + dx;

                        if (newl < 0 || newt < 0 || newr > p.x || newb > p.y)
                            break;
                        game.end_score_panel.layout(newl, newt, newr, newb);
                        startX = (int)event.getRawX();
                        startY = (int)event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:     //松开手让它回到远处
                        game.end_score_panel.setAlpha(1.0f);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(game.end_score_panel.getLayoutParams());
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        game.end_score_panel.setLayoutParams(layoutParams);
                        break;
                }
                return true;
            }
        });
    }
}
