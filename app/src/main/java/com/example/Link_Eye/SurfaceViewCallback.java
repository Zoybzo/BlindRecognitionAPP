package com.example.Link_Eye;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import java.io.IOException;

public class SurfaceViewCallback implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Context context;
    private static final String TAG = "Camera";
    private FrontCamera mFrontCamera = null;
    private boolean previewing = false;
    private Camera mCamera;
    private static FaceTask mFaceTask;
    public static SurfaceViewCallback _instance = null;

    public SurfaceViewCallback() {
        super();
        mFrontCamera = new FrontCamera();
        previewing = mFrontCamera.getPreviewing();
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 相机实时数据的回调
     *
     * @param data   相机获取的数据，格式是YUV
     * @param camera 相应相机的对象
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mFaceTask != null) {
            switch (mFaceTask.getStatus()) {
                case RUNNING:
                    return;
                case PENDING:
                    mFaceTask.cancel(false);
                    mFaceTask = null;
                    break;
            }
        }
        mFaceTask = new FaceTask(data, camera);
        mFaceTask.execute((Void) null);
    }

    public static SurfaceViewCallback getInstance() {
        if (_instance == null) {
            _instance = new SurfaceViewCallback();
        }
        return _instance;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mFrontCamera.setCamera(mCamera);
        mCamera = mFrontCamera.initCamera();
        mCamera.setPreviewCallback(this);

        FrontCamera.setCameraDisplayOrientation((Activity) context, mFrontCamera.getCurrentCamIndex(), mCamera);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (previewing) {
            mCamera.stopPreview();
            Log.i(TAG, "Stop preview");
        }

        try {
            // notice the order there!
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            Log.i(TAG, "Begin preview");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mFrontCamera.StopCamera(mCamera);
    }
}
