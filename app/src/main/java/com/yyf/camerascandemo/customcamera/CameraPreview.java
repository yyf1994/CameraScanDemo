package com.yyf.camerascandemo.customcamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import java.io.IOException;

public class CameraPreview extends TextureView implements TextureView.SurfaceTextureListener {

    private TextureView textureView;
    private Camera mCamera;

    public CameraPreview(Context context , Camera camera) {
        super(context);
        mCamera = camera;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
       /* mCamera = Camera.open();
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        textureView.setLayoutParams(new FrameLayout.LayoutParams(
                previewSize.width, previewSize.height, Gravity.CENTER));
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException t) {
        }
        mCamera.startPreview();
        textureView.setAlpha(1.0f);
        textureView.setRotation(90.0f);*/

        try {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {

        mCamera.release();
        mCamera.stopPreview();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
