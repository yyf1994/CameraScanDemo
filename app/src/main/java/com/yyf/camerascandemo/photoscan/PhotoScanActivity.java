package com.yyf.camerascandemo.photoscan;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.yyf.camerascandemo.R;
import com.yyf.camerascandemo.util.CameraUtils;
import com.yyf.camerascandemo.util.FileHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
/**
 * 先拍照后解码 兼容性问题比较难，还没有很好的解决。
 * 由于设备硬件不同，并且对条码要求比较高，会有一定的失败率
 * 拍完照之后将图片保存在SD卡中的MyImage文件夹中，解码成功，修改文件名称
 * **/
public class PhotoScanActivity extends AppCompatActivity implements View.OnClickListener{

    private Camera camera;
    private Button snap;
    private SurfaceView surfaceView;
    int camera_id = 0;
    private IOrientationEventListener iOriListener;
    final int SUCCESS = 233;
    private SnapHandler handler = new SnapHandler();
    // 格式化时间
    private SimpleDateFormat dateFormat;
    // 日期对象
    private Date date;
    // 文件名字的带的时间戳
    private String timeString,filenameString;
    //保存的文件名称
    private String fileName;
    //sd卡的路径
    private String SDPATH;
    private String url;

    //文件路径
    private String filepath;
    //新文件路径
    private String newfilepath;
    private String sdStatus = Environment.getExternalStorageState();

    private FileHelper fileHelper;
    private String neirong;

    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 显示界面
        setContentView(R.layout.activity_photo_scan);
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        fileHelper = new FileHelper(getApplicationContext());
        snap = (Button) this.findViewById(R.id.snap);
        snap.setOnClickListener(this);
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

    //拍照
    private void takePicture() {

        camera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // TODO Auto-generated method stub
                final byte[] tempdata = data;
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        filepath = getFilePath();

                        File f1 = new File(filepath);
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

    private String getFilePath() {

        // 获取时间
        timeString = formatDate();
        filenameString = formatDate1();
        if (fileHelper.hasSD()) {
            //文件夹的路径
            SDPATH = Environment.getExternalStorageDirectory().getPath();
        } else {
            SDPATH = Environment.getExternalStorageDirectory().getPath();
        }

        url = SDPATH + "/myImage/" + timeString;
        File file = new File(url);
        if (!file.exists()) {
            file.mkdirs();//创建目录
        }
        fileName = url + "/" + filenameString + ".jpg";
        return fileName;
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
        this.iOriListener.disable();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.snap){
            takePicture();
        }
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
        setDispaly(parameters, camera);//设置角度旋转90度

        //先判断是否支持，否则会报错
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
        camera.setDisplayOrientation(90);//竖屏旋转90度
//        camera.setDisplayOrientation(0);//横屏为0
        camera.setParameters(parameters);
    }

    class SnapHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                String result = success();
                //重新写入文件名称 filepath为原文件名 newfilepath 为新文件名

                String path = url + "/" + result + ".jpg";
                File f = new File(path);
                if (!f.exists()) {
                    newfilepath = url + "/" + result + ".jpg";
                } else {
                    newfilepath = url + "/" + result + "_.jpg";
                }
                File oldfile = new File(filepath);
                File newfile = new File(newfilepath);
                oldfile.renameTo(newfile);

                Toast.makeText(PhotoScanActivity.this, result, Toast.LENGTH_SHORT).show();
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
    private String success() {
        MultiFormatReader multiFormatReader = new MultiFormatReader();

        // 解码的参数
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(
                2);
        // 可以解析的编码类型
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        if (decodeFormats == null || decodeFormats.isEmpty()) {
            decodeFormats = new Vector<BarcodeFormat>();
            // 这里设置可扫描的类型，我这里选择了都支持
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        }
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        // 设置继续的字符编码格式为UTF8
        // hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

        // 设置解析配置参数
        multiFormatReader.setHints(hints);

        // 开始对图像资源解码
        Result rawResult = null;
        Bitmap bitmap1;
        try {

            bitmap1 = BitmapFactory.decodeFile(filepath, null);
            double width = bitmap1.getWidth();
            double height = bitmap1.getHeight();
            int i = getBitmapSize(bitmap1);
            Log.d("bitmap","size:"+i+"width:"+width+"height:"+height);

//                if(i>1024){
////                    bitmap1 = getBitmapFromUrl(filepath, 480.0, 800.0);
//                }

            int jiaodu = getBitmapDegree(filepath);
            Log.d("jiaodu",jiaodu+"");
            bitmap = rotateBitmapByDegree(bitmap1,90);
            bitmap = loadBitmap(filepath, true);
            setPictureDegreeZero(filepath);
//                int i = getBitmapSize(bitmap);
//                Log.d("bitmap",i+"");

            BitmapLuminanceSource source = new BitmapLuminanceSource(bitmap);
            HybridBinarizer hybridBinarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(hybridBinarizer);
            rawResult = multiFormatReader.decodeWithState(binaryBitmap);

        } catch (NotFoundException e) {
            e.printStackTrace();
        }

        if (rawResult == null) {
            neirong = "解码失败,请重新拍照";
        } else {
            neirong = rawResult.getText();
        }
        return neirong;
    }

    //获取bitmap的大小
    public int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }


    /**
     * 读取图片的旋转的角度， 某些机型此方法无效
     *
     * @param path
     *            图片绝对路径
     * @return 图片的旋转角度
     */
    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm
     *            需要旋转的图片
     * @param degree
     *            旋转角度
     * @return 旋转后的图片
     */
    public  Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    // 控制图像的正确显示方向
    public  void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
            setDisplayOrientation(camera,90);
        } else {
            parameters.setRotation(90);
        }

    }

    // 实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod(
                    "setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[] { i });
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }

    /** 从给定路径加载图片 */
    public Bitmap loadBitmap(String imgpath) {
        return BitmapFactory.decodeFile(imgpath);
    }

    /** 从给定的路径加载图片，并指定是否自动旋转方向 */
    public Bitmap loadBitmap(String imgpath, boolean adjustOritation) {
        if (!adjustOritation) {
            return loadBitmap(imgpath);
        } else {
            Bitmap bm = loadBitmap(imgpath);
            int digree = 0;
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imgpath);
            } catch (IOException e) {
                e.printStackTrace();
                exif = null;
            }
            if (exif != null) {
                // 读取图片中相机方向信息
                // int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                // ExifInterface.ORIENTATION_NORMAL);
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_FLIP_VERTICAL);
                // 计算旋转角度
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        digree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        digree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        digree = 270;
                        break;
                    default:
                        digree = 0;
                        break;
                }
            }
            if (digree != 0) {
                // 旋转图片
                Matrix m = new Matrix();
                m.postRotate(digree);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                        bm.getHeight(), m, true);
            }
            return bm;
        }
    }

    /**
     * 将图片的旋转角度置为0  ，此方法可以解决某些机型拍照后图像，出现了旋转情况
     *
     * @Title: setPictureDegreeZero
     * @param path
     * @return void
     * @date 2012-12-10 上午10:54:46
     */
    private void setPictureDegreeZero(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
            // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
            exifInterface.saveAttributes();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 根据路径获取图片并压缩返回bitmap用于显示
     *
     * @param context
     * @param id
     * @return
     */

    private  Bitmap getSmallBitmap(Context context, int id,int width,int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), id, options);
        // 计算 缩略图大小为原始图片大小的几分之一 inSampleSize:缩略图大小为原始图片大小的几分之一
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(context.getResources(), id, options);
    }

    /**
     * 计算图片的缩放值
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private  int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

        }
        return inSampleSize;

    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public  int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public  int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据路径获取图片资源（已缩放）
     *
     * @param url    图片存储路径
     * @param width  缩放的宽度
     * @param height 缩放的高度
     * @return
     */
    private Bitmap getBitmapFromUrl(String url, double width, double height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
        Bitmap bitmap = BitmapFactory.decodeFile(url);
        // 防止OOM发生
        options.inJustDecodeBounds = false;
        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = 1;
        float scaleHeight = 1;
//        try {
//            ExifInterface exif = new ExifInterface(url);
//            String model = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        // 按照固定宽高进行缩放
        // 这里希望知道照片是横屏拍摄还是竖屏拍摄
        // 因为两种方式宽高不同，缩放效果就会不同
        // 这里用了比较笨的方式
        if (mWidth <= mHeight) {
            scaleWidth = (float) (width / mWidth);
            scaleHeight = (float) (height / mHeight);
        } else {
            scaleWidth = (float) (height / mWidth);
            scaleHeight = (float) (width / mHeight);
        }
//        matrix.postRotate(90); /* 翻转90度 */
        // 按照固定大小对图片进行缩放
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight, matrix, true);
        // 用完了记得回收
        bitmap.recycle();
        return newBitmap;
    }
}
