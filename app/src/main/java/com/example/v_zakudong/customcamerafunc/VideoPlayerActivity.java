package com.example.v_zakudong.customcamerafunc;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.v_zakudong.customcamerafunc.view.MyVideoView;

public class VideoPlayerActivity extends AppCompatActivity {

    private MyVideoView video_player;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        path = getIntent().getStringExtra("path");
        video_player = (MyVideoView) findViewById(R.id.video_player);
        video_player.setVideoPath(path);
        /*RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        video_player.setLayoutParams(params);*/
        video_player.start();
        video_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                finish();
                VideoPlayerActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }
}
