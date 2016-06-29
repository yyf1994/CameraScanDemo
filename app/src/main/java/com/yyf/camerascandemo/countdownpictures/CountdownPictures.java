package com.yyf.camerascandemo.countdownpictures;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yyf.camerascandemo.R;
import com.yyf.camerascandemo.util.CameraUtils;
import com.yyf.camerascandemo.util.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 倒计时拍照
 **/
public class CountdownPictures extends AppCompatActivity implements Runnable {
    //    setContentView(R.layout.activity_countdown_pictures);
    Camera camera;
    SurfaceView surfaceView;
    int camera_id = 0;
    IOrientationEventListener iOriListener;

    final int SUCCESS = 233;
    SnapHandler handler = new SnapHandler();

    // 格式化时间
    private SimpleDateFormat dateFormat;
    // 日期对象
    private Date date;
    // 文件名字的带的时间戳
    private String timeString;
    //保存的文件名称
    private String fileName;

    private String sdStatus = Environment.getExternalStorageState();
    /**
     * SD卡的路径
     **/
    private String SDPATH;

    private FileHelper fileHelper;

    private String resultString ;

    // 倒计时拍摄
    private int cameratime = 5;
    // 控制线程
    boolean stopThread = false;

    private TextView tv_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 显示界面
        setContentView(R.layout.activity_countdown_pictures);
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        tv_time = (TextView) findViewById(R.id.tv_time);
        fileHelper = new FileHelper(getApplicationContext());

        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                int mNumberOfCameras = Camera.getNumberOfCameras();

                // Find the ID of the default camera
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < mNumberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        camera_id = i;
                    }
                }
                camera = Camera.open(camera_id);
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview(); // 开始预览

                    iOriListener.enable();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                // TODO Auto-generated method stub
                setCameraAndDisplay(width, height);

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                if (null != camera) {
                    camera.release();
                    camera = null;
                }

            }

        });//为SurfaceView的句柄添加一个回调函数

        iOriListener = new IOrientationEventListener(this);
    }

    // 格式化系统的时间
    public String formatDate() {
        date = new Date(System.currentTimeMillis());
        // 日期格式
        dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date);
    }

    // 格式化系统的时间
    public String formatDate1() {
        date = new Date(System.currentTimeMillis());
        // 日期格式
        dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        return dateFormat.format(date);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != camera) {
            camera.release();
            camera = null;
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopThread = true;
        this.iOriListener.disable();
    }

    @Override
    public void run() {
        while (!stopThread) {
            try {
                //按秒数倒计时
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            cameratime--;
            mHandler.sendEmptyMessage(222);
            if (cameratime <= 0) {
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开启线程
        new Thread(this).start();
    }

    public class IOrientationEventListener extends OrientationEventListener {

        public IOrientationEventListener(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }


        @Override
        public void onOrientationChanged(int orientation) {
            // TODO Auto-generated method stub
            if (ORIENTATION_UNKNOWN == orientation) {
                return;
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(camera_id, info);
            orientation = (orientation + 45) / 90 * 90;
            int rotation = 0;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
            if (null != camera) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setRotation(rotation);
                camera.setParameters(parameters);
            }

        }

    }

    public void setCameraAndDisplay(int width, int height) {
        Camera.Parameters parameters = camera.getParameters();
        /*获取摄像头支持的PictureSize列表*/
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        /*从列表中选取合适的分辨率*/
        Camera.Size picSize = CameraUtils.getProperSize(pictureSizeList, ((float) width) / height);
        if (null != picSize) {
            parameters.setPictureSize(picSize.width, picSize.height);
        } else {
            picSize = parameters.getPictureSize();
        }
        /*获取摄像头支持的PreviewSize列表*/
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Camera.Size preSize = CameraUtils.getProperSize(previewSizeList, ((float) width) / height);
        if (null != preSize) {
            Log.v("TestCameraActivityTag", preSize.width + "," + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        /*根据选出的PictureSize重新设置SurfaceView大小*/
        float w = picSize.width;
        float h = picSize.height;
        surfaceView.setLayoutParams(new RelativeLayout.LayoutParams((int) (height * (w / h)), height));

        parameters.setJpegQuality(100); // 设置照片质量

        //先判断是否支持，否则会报错
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
        camera.setDisplayOrientation(0);
        camera.setParameters(parameters);
    }

    class SnapHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                CountdownPictures.this.finish();
            }
            try {
                camera.setPreviewDisplay(surfaceView.getHolder());
                camera.startPreview();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 222:
                    tv_time.setText("" + cameratime);
                    if ("0".equals(tv_time.getText().toString())) {
                        tv_time.setText("拍摄成功！");
                        takePhoto();
                    }
                    break;

            }
        }
    };

    private void takePhoto() {
        camera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // TODO Auto-generated method stub
                final byte[] tempdata = data;
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // 获取时间
                        timeString = formatDate();
                        resultString = formatDate1();
                        if (fileHelper.hasSD()) {
                            //文件夹的路径
                            SDPATH = Environment.getExternalStorageDirectory().getPath();
                        } else {
                            SDPATH = Environment.getExternalStorageDirectory().getPath();
                        }

                        String url = SDPATH + "/myImage/" + timeString;
                        File file = new File(url);
                        if (!file.exists()) {
                            file.mkdirs();//创建目录
                        }
                        //得到此目录下的文件路径
                        String path = url + "/" + resultString + ".jpg";
                        File f = new File(path);
                        if (!f.exists()) {
                            fileName = url + "/" + resultString + ".jpg";
                        } else {
                            fileName = url + "/" + resultString + "_.jpg";
                        }
                        File f1 = new File(fileName);
                        try {
                            f1.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                            Toast.makeText(getApplicationContext(), "SD卡不存在", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FileOutputStream outputStream;
                        try {
                            outputStream = new FileOutputStream(f1);
                            outputStream.write(tempdata); // 写入sd卡中
                            outputStream.close(); // 关闭输出流
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(SUCCESS);
                    }

                });
                //启动存储照片的线程
                thread.start();
            }

        });

    }
}
