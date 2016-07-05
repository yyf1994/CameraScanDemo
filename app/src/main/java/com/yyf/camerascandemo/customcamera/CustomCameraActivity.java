package com.yyf.camerascandemo.customcamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yyf.camerascandemo.R;
import com.yyf.camerascandemo.util.CameraUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CustomCameraActivity extends Activity implements TextureView.SurfaceTextureListener,View.OnClickListener{

    private AutoFitTextureView cameraPreview;
    private Camera mCamera;
    private Button captureButton;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String TAG = "ERROR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        cameraPreview = (AutoFitTextureView) findViewById(R.id.cameraPreview);
        // 在Capture按钮中加入listener
        captureButton = (Button) findViewById(R.id.button_capture);
        cameraPreview.setSurfaceTextureListener(CustomCameraActivity.this);
        captureButton.setOnClickListener(this);
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG,
                        "Error creating media file, check storage permissions: "
                                + "e.getMessage()");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    /** 为保存图片或视频创建File */
    private static File getOutputMediaFile(int type) {
        // 安全起见，在使用前应该
        // 用Environment.getExternalStorageState()检查SD卡是否已装入
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        // 如果期望图片在应用程序卸载后还存在、且能被其它应用程序共享，
        // 则此保存位置最合适
        // 如果不存在的话，则创建存储目录
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
            Log.d("MyCameraApp", "failed to create directory");
        }
        // 创建媒体文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }  else {
            return null;
        }
        return mediaFile;
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // 试图获取Camera实例
        }
        catch (Exception e) {
            // 摄像头不可用（正被占用或不存在）
        }
        return c; // 不可用则返回null
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // 捕获的图像保存到Intent指定的fileUri
                Toast.makeText(this, "Image saved to:\n" + data.getData(),
                        Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // 用户取消了图像捕获
            } else {
                // 图像捕获失败，提示用户
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera(); // 在暂停事件中立即释放摄像头
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // 为其它应用释放摄像头
            mCamera = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        // 当TextureView可用时，打开摄像头
        mCamera = Camera.open();
//        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
//        cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(
//                previewSize.width, previewSize.height, Gravity.CENTER));
//        setCameraAndDisplay();
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException t) {
        }
        mCamera.startPreview();
        cameraPreview.setAlpha(1.0f);
        cameraPreview.setRotation(90.0f);
//        openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        setCameraAndDisplay(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
       return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onClick(View view) {
        // 从摄像头获取图片
        mCamera.takePicture(null, null, mPicture);
    }

    public void setCameraAndDisplay(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();
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
        cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (height * (w / h)), height, Gravity.CENTER));

        parameters.setJpegQuality(100); // 设置照片质量
//        setDispaly(parameters, camera);//设置角度旋转90度

        //先判断是否支持，否则会报错
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
        mCamera.setDisplayOrientation(90);//竖屏旋转90度
        mCamera.setParameters(parameters);
    }

}
