package wanganxin.com.poker.Sound;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.lang.reflect.Field;

import wanganxin.com.poker.GameActivity.LandlordActivity;
import wanganxin.com.poker.R;

public class BackgroundMusic {
    private LandlordActivity game = null;

    //初始化需要操作的卡牌
    public BackgroundMusic(LandlordActivity game) {
        this.game = game;
    }

    private MediaPlayer background_music = null;    //背景音乐播放

    //初始化背景音乐
    public void initBackgroundMusic() {
        game.isBackgroundOpen = true; //初始化背景音乐一开始处于播放状态
        game.isSoundEffectOpen = true;//初始化音效一开始处于播放状态
        // 设定调整音量为媒体音量,当暂停播放的时候调整音量就不会再默认调整铃声音量了
        game.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        background_music = new MediaPlayer();
        background_music = MediaPlayer.create(game.getApplicationContext(), randomBackgroundMusciPlay());
        background_music.start();
        //播放结束之后弹出提示
        background_music.setOnCompletionListener(new MediaListenter());
    }

    //暂停背景音乐
    public void pause() {
        if (background_music != null) {
            background_music.pause();
        }
    }

    //开启背景音乐
    public void start() {
        if (background_music != null) {
            background_music.start();
        }
    }

    //设置背景音乐的监听事件，结束播放时随机播放下一首
    public class MediaListenter implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            background_music.release();
            background_music = MediaPlayer.create(game.getApplicationContext(), randomBackgroundMusciPlay());
            background_music.start();
            background_music.setOnCompletionListener(new MediaListenter());
        }
    }
    //从res中随机播放背景音乐（1~8）
    public int randomBackgroundMusciPlay() {
        int num = game.ran.nextInt(7);
        Field field = null;
        int id = 0;
        try {
            field = R.raw.class.getField("background" + Integer.toString(num + 1));
            id = field.getInt(new R.raw());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void background_music_btn_Click() {
        if (game.isBackgroundOpen == true) {
            background_music.pause();
            game.isBackgroundOpen = false;
            game.fourFunbtn[2].setBackground(game.resources.getDrawable(
                    R.mipmap.background_music_not_btn, game.getTheme()));
        }
        else {
            background_music.start();
            game.isBackgroundOpen = true;
            game.fourFunbtn[2].setBackground(game.resources.getDrawable(
                    R.mipmap.background_music_btn, game.getTheme()));
        }
    }

    public void backgroundMusicShutDown() {
        if (background_music != null) {
            background_music.stop();
            background_music.release();
            background_music = null;
        }
    }
}
