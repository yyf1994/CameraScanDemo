package com.yyf.camerascandemo.systemcameras;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.yyf.camerascandemo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 调用系统相机并保存缩略图
 *
 * */
public class SystemCameraForThumbnails extends AppCompatActivity {

    private ImageView imageView;
    //文件路径
    private String filepath;
    private String resultString;
    // 格式化时间
    private SimpleDateFormat dateFormat;
    // 日期对象
    private Date date;
    private FileInputStream is = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_camera_for_thumbnails);
        imageView = (ImageView) findViewById(R.id.imageview);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
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
    /**
     * 获取原图片存储路径
     *
     * @return
     */
    private String getPhotopath() {
        // 文件名字的带的时间戳
        String timeString;
        String fileName;
        /** SD卡的路径 **/
        String SDPATH;
        // 获取时间
        timeString = formatDate();
        resultString = formatDate1();
        //文件夹的路径
        SDPATH = Environment.getExternalStorageDirectory().getPath();
        String url = SDPATH + "/myImage/" + timeString;
        Log.d("url", "url" + url);
        File file = new File(url);
        file.mkdirs();//创建目录
        //得到此目录下的文件路径
        String path = url + "/" + resultString + ".jpg";
        File f = new File(path);
        if (!f.exists()) {
            fileName = url + "/" + resultString + ".jpg";
        } else {
            fileName = url + "/" + resultString + "_.jpg";
        }
        return fileName;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            String sdStatus = Environment.getExternalStorageState();
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { //判断SD卡是否存在
                Toast.makeText(SystemCameraForThumbnails.this,"SD卡不存在",Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            FileOutputStream b = null;
            String fileName = getPhotopath();

            try {
                b = new FileOutputStream(fileName);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// ������д���ļ�
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    b.flush();
                    b.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            imageView.setImageBitmap(bitmap);
        }
    }
}
