package com.example.Link_Eye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import myUtils.BitmapProcess;
import myUtils.ToBase64;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.alibaba.fastjson.*;

import static android.content.Context.VIBRATOR_SERVICE;
import static myUtils.myToast.showMyToast;


public class FaceTask extends AsyncTask {
    private static final String myUrl = "http://82.157.104.222:5000/blinds";
    public static boolean check = false;
    private static final String TAG = "CameraTAG";

    private byte[] mData;
    private Bitmap raw_bitmap;
    private String base64_global;
    private Camera mCamera;

    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(1000, TimeUnit.SECONDS)
            .readTimeout(1000, TimeUnit.SECONDS)
            .writeTimeout(1000, TimeUnit.SECONDS)
            .build();

    public FaceTask() {

    }

    public FaceTask(byte[] data, Camera camera) {
        this.mData = data;
        this.mCamera = camera;
        // okhttp 默认允许一个host最多发起 5 个请求，所以需要修改。
        //自定义上限
        okHttpClient.dispatcher().setMaxRequests(3000000);
        //自定义上限
        okHttpClient.dispatcher().setMaxRequestsPerHost(1000000);
    }

    public void setDataAndCamera(byte[] data, Camera camera) {
        this.mData = data;
        this.mCamera = camera;
    }

    /*关于内存泄漏问题 可尝试在handler以及一些内部类上做手脚 ～ 弱引用handler和static静态内部类结合（并分析内部静态变量是否持有外部类的引用） 12号之后我也来就内存泄漏问题进行一些研究 和大家共同探讨*/


    @Override
    protected Object doInBackground(Object[] objects) {

        Camera.Parameters parameters = mCamera.getParameters();
        int imageFormat = parameters.getPreviewFormat();
        int w = parameters.getPreviewSize().width;
        int h = parameters.getPreviewSize().height;

        // TODO: Arguments?
        Rect rect = new Rect(0, 0, w, h);
        YuvImage yuvImage = new YuvImage(mData, imageFormat, w, h, null);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(rect, 100, outputStream);
            raw_bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size());

            Log.d("Length_before", String.valueOf(ToBase64.bitmapToBase64(raw_bitmap).length()));
            raw_bitmap = BitmapProcess.compressImage(raw_bitmap);
            raw_bitmap = BitmapProcess.compressImage(raw_bitmap);
            raw_bitmap = BitmapProcess.compressImage(raw_bitmap);
            Log.d("Length_after", String.valueOf(ToBase64.bitmapToBase64(raw_bitmap).length()));

            base64_global = ToBase64.bitmapToBase64(raw_bitmap);

            login(myUrl, base64_global, new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    Log.d(TAG + "Failure", "onFailure:err");
                    e.printStackTrace();
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    assert response.body() != null;
                    String result = response.body().string();
                    Log.d(TAG + "Response", "out result===" + result);
                    Log.d(TAG + "Response", response.code() + "");
                    getMethod(JudgeAns(result));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onPreviewFrame: Fail" + e.getLocalizedMessage());
        }
        return null;
    }


    private boolean JudgeAns(String json) {
        boolean ans = true;
        JSONObject jsonObject = JSON.parseObject(json);
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            if (entry.getKey().equals("data")) {
                ans = (entry.getValue().equals("blinds"));
            } else if (entry.getKey().equals("msg") || entry.getKey().equals("status")) {
                if (!entry.getValue().equals("success")) return false;
            }
        }
        return ans;
    }

    private void login(String url, String base64, Callback callback) {
        Log.d("login", "loginSucceed");

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("image", base64);
        MultipartBody multipartBody = builder.build();
        Request request = new Request.Builder().url(url).post(multipartBody).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    private static final String appearContent = "盲道出现";
    private static final String disappearContent = "盲道消失";
    private static Toast toast = Toast.makeText(SurfaceViewCallback.getInstance().getContext(), "", Toast.LENGTH_LONG);
    private static Vibrator v = (Vibrator) SurfaceViewCallback.getInstance().getContext().getSystemService(VIBRATOR_SERVICE);
    private static TextToSpeech textToSpeech = new TextToSpeech(SurfaceViewCallback.getInstance().getContext(), new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.CHINESE);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(SurfaceViewCallback.getInstance().getContext(), "This language is not supported", Toast.LENGTH_LONG).show();

                } else {
                    textToSpeech.setPitch(0.6f);
                    textToSpeech.setSpeechRate(1.3f);
                }
            }
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getMethod(boolean appear) {
        switch (MainActivity.getMethodNum()) {
            case ToastMessage: {
                if (appear) {
                    Log.e("Response", "Blind");
                    if (!check) {
                        Looper.prepare();
                        toast.setText(appearContent);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        showMyToast(toast, 500);
                        check = true;
                        Looper.loop();
                    }
                } else {
                    Log.e("Response", "Others");
                    if (check) {
                        Looper.prepare();
                        toast.setText(disappearContent);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        showMyToast(toast, 500);
                        check = false;
                        Looper.loop();
                    }
                }
                break;
            }

            case VideoMessage: {
                if (appear) {
                    Log.e("Response", "Blind");
                    if (!check) {
                        textToSpeech.speak(appearContent, TextToSpeech.QUEUE_ADD, null);
                        check = true;
                    }
                } else {
                    Log.e("Response", "Others");
                    if (check) {
                        textToSpeech.speak(disappearContent, TextToSpeech.QUEUE_ADD, null);
                        check = false;
                    }
                }

                break;
            }

            case VibratorMessage: {
                if (appear) {
                    Log.e("Response", "Blind");
                    if (!check) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        check = true;
                    }
                } else {
                    Log.e("Response", "Others");
                    if (check) {
                        v.vibrate(VibrationEffect.createOneShot(500, 255));
                        check = false;
                    }
                }

                break;
            }
        }
    }
}
