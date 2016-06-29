package com.yyf.camerascandemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.yyf.camerascandemo.countdownpictures.CountdownPictures;
import com.yyf.camerascandemo.photoscan.PhotoScanActivity;
import com.yyf.camerascandemo.scanphoto.ScanPhotoActivity;
import com.yyf.camerascandemo.systemcameras.SystemCameraForOriginalImage;
import com.yyf.camerascandemo.systemcameras.SystemCameraForThumbnails;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button1,button2,button3,button4,button5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setLinstener();
    }

    private void setLinstener() {
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
    }

    private void initView() {
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.button1:
                intent = new Intent(MainActivity.this, SystemCameraForThumbnails.class);
                startActivity(intent);
                break;
            case R.id.button2:
                intent = new Intent(MainActivity.this, SystemCameraForOriginalImage.class);
                startActivity(intent);
                break;
            case R.id.button3:
                intent = new Intent(MainActivity.this, CountdownPictures.class);
                startActivity(intent);
                break;
            case R.id.button4:
                 intent = new Intent(MainActivity.this, ScanPhotoActivity.class);
                startActivity(intent);
                break;
            case R.id.button5:
                 intent = new Intent(MainActivity.this, PhotoScanActivity.class);
                startActivity(intent);
                break;
        }

    }
}
