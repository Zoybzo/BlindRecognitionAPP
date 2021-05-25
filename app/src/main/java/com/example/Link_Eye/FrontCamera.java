package com.example.Link_Eye;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

public class FrontCamera {
    static final String TAG = "Camera";
    Camera mCamera;
    int mCurrentCameraIndex = 0;
    boolean previewing;

    public void setCamera(Camera camera) {
        this.mCamera = camera;
    }

    public int getCurrentCamIndex() {
        return this.mCurrentCameraIndex;
    }

    public boolean getPreviewing() {
        return this.previewing;
    }

    // Find the target Camera and return it.
    public Camera initCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        previewing = true;

        for (int camInx = 0; camInx < cameraCount; camInx++) {
            Camera.getCameraInfo(camInx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // For the normal use of Camera, it is necessary to surround open() with try/catch.
                try {
                    Log.d(TAG, "Camera Got");
                    cam = Camera.open(camInx);
                    mCurrentCameraIndex = camInx;
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open:" + e.getLocalizedMessage());
                }
            }
        }

        Log.d(TAG, "Camera is " + (cam == null));
        return cam;
    }

    public void StopCamera(Camera mCamera) {
        // The callback faction must be null before the Camera be released.
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        previewing = false;
    }

    /**
     * 旋转屏幕后自动适配（若只用到竖的，也可不要）
     * 已经在manifests中让此Activity只能竖屏了
     *
     * @param activity 相机显示在的Activity
     * @param cameraId 相机的ID
     * @param camera   相机对象
     */
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }






    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };

    Camera.PictureCallback rawPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };

    Camera.PictureCallback jpegPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Log.i(TAG, "已经获取了bitmap:" + bitmap.toString());
            previewing = false;
            //需要保存可执行下面
/*            new Thread(new Runnable() {
                @Override
                public void run() {
                    String filePath = ImageUtil.getSaveImgePath();
                    File file = new File(filePath);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file, true);
                        fos.write(data);
                        ImageUtil.saveImage(file, data, filePath);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();*/
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();//重新开启预览 ，不然不能继续拍照
            previewing = true;
        }


    };

}
