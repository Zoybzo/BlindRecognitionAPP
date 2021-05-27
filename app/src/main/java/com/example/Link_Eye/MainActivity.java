package com.example.Link_Eye;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import myUtils.Enums;

public class MainActivity extends AppCompatActivity {
    private static Enums.Method methodNum = Enums.Method.ToastMessage;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String NORMAL_USE = "需要允许授权才可正常使用";
    private static final String PERMISSION_GET = "授权";
    private static final String PERMISSION_DENY = "暂不授权";

    private SharedPreferences settings;

    private Context context = MainActivity.this;
    private SurfaceView surfaceView;
    private CameraSurfaceHolder mCameraSurfaceHolder = new CameraSurfaceHolder();
    private FrameLayout container;
    private AlertDialog mDialog;
    private AlertDialog dialog;

    Enums.Method[] methodNames = {Enums.Method.ToastMessage, Enums.Method.VibratorMessage, Enums.Method.VideoMessage};
    private int methodId = R.id.ToastMessage;

    public Context getContext() {
        return this.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPermissionList = new ArrayList<>();

        super.onCreate(savedInstanceState);
        setContentView(container = new FrameLayout(this));

        settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)) {
            Log.d("Comments", "First time");
            getPrivacy();
        } else {
            getPermission();
            initView();
        }
    }

    void getPrivacy() {
        dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        dialog.setCancelable(false);

        String str = "欢迎使用 Link-Eye 灵眸！我们非常重视您的个人信息和隐私保护。" +
                "为了更好地保证您的个人权益，在您使用我们的产品前，请务必审慎阅读《隐私政策》内的所有条款。" +
                "您点击\"同意并继续\"，即表示您已阅读并同意以上条款。Link-Eye 灵眸将权力保证您的合法权益与信息安全，并持续为您提供更优质的服务。";


        final Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_intimate);
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置属性
        final WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.dimAmount = 0.5f;
        window.setAttributes(params);

        TextView textView = window.findViewById(R.id.tv_1);
        TextView tvCancel = window.findViewById(R.id.tv_cancel);
        TextView tvAgree = window.findViewById(R.id.tv_agree);
        tvCancel.setOnClickListener(v -> {
            dialog.cancel();
            System.exit(0);
        });
        tvAgree.setOnClickListener(v -> {
            settings.edit().putBoolean("my_first_time", false).apply();
            dialog.cancel();
            getPermission();
            initView();
        });
        textView.setText(str);

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(str);
        final int start = str.indexOf("《");//第一个出现的位置
        ssb.setSpan(new URLSpan("http://82.157.104.222:5000/privacy"), start, start + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //网络

        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(ssb, TextView.BufferType.SPANNABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.methods, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        methodId = item.getItemId();
        Log.e("methodId", String.valueOf(methodId));
        switch (methodId) {
            case R.id.ToastMessage:
                methodNum = methodNames[0];
                Toast.makeText(context, "文本框模式启动", Toast.LENGTH_SHORT).show();
                break;
            case R.id.VibratorMessage:
                Toast.makeText(context, "震动模式启动", Toast.LENGTH_SHORT).show();
                methodNum = methodNames[1];
                break;
            case R.id.VideoMessage:
                Toast.makeText(context, "语音模式启动", Toast.LENGTH_SHORT).show();
                methodNum = methodNames[2];
                break;
        }
        return true;
    }

    public static Enums.Method getMethodNum() {
        return methodNum;
    }


    public void initView() {
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView1);
        mCameraSurfaceHolder.setCameraSurfaceHolder(context, surfaceView);
    }


    private final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private List<String> mPermissionList = null;


    /**
     * Get the permission the APP will need
     **/
    private void getPermission() {
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        //未授予的权限为空，表示都授予了

        if (mPermissionList.isEmpty()) {

            Log.d("Permission", "Successful");

        } else {//请求权限方法

            Log.d("Permission", "Fail");

            String[] permissions = mPermissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                //已授权
                if (grantResults[i] == 0) {
                    continue;
                }

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    //选择禁止
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(PERMISSION_GET);
                    builder.setMessage(NORMAL_USE);
                    String str = permissions[i];
                    builder.setPositiveButton(PERMISSION_GET, (dialog, id) -> {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{str}, 1);
                    });
                    builder.setNegativeButton(PERMISSION_DENY, (dialog, id) -> {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    });
                    mDialog = builder.create();
//                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                } else {
                    //选择禁止并勾选禁止后不再询问
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(PERMISSION_GET);
                    builder.setMessage(NORMAL_USE);
                    builder.setPositiveButton(PERMISSION_GET, (dialog, id) -> {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        //调起应用设置页面
                        startActivityForResult(intent, 2);
                    });
                    builder.setNegativeButton(PERMISSION_DENY, (dialog, id) -> {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                    });
                    mDialog = builder.create();
//                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                }
            }
        }
    }

}