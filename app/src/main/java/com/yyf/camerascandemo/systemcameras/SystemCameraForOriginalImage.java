package com.yyf.camerascandemo.systemcameras;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

public class SystemCameraForOriginalImage extends AppCompatActivity {

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
        setContentView(R.layout.activity_system_camera_for_original_image);
        imageView = (ImageView) findViewById(R.id.imageview);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        filepath = getPhotopath();
        File f1 = new File(filepath);
        Uri uri = Uri.fromFile(f1);
        // 指定存储路径，这样就可以保存原图了
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 1);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (resultCode == Activity.RESULT_OK) {

                String sdStatus = Environment.getExternalStorageState();
                if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ���sd�Ƿ����
                    Toast.makeText(getApplicationContext(), "SD卡不存在", Toast.LENGTH_SHORT).show();
                    return;
                }

                FileOutputStream b = null;
                Bitmap bitmap = null;
                try {
                    // 获取输入流
                    is = new FileInputStream(filepath);
                    // 把流解析成bitmap
                     bitmap = BitmapFactory.decodeStream(is);
                    b = new FileOutputStream(filepath);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        b.flush();
                        b.close();
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                imageView.setImageBitmap(bitmap);
        }
    }
    }
}
