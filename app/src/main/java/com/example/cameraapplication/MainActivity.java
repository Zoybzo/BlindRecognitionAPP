package com.example.cameraapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private static Enums.Method methodNum = Enums.Method.ToastMessage;

    private Context context = MainActivity.this;
    private SurfaceView surfaceView;
    private CameraSurfaceHolder mCameraSurfaceHolder = new CameraSurfaceHolder();
    private FrameLayout container;
    private AlertDialog mDialog;

    Enums.Method[] methodNames = {Enums.Method.ToastMessage, Enums.Method.VibratorMessage, Enums.Method.VideoMessage};
    final Integer[] methods = {R.id.ToastMessage, R.id.VibratorMessage, R.id.VideoMessage};
    private int methodId = R.id.ToastMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPermissionList = new ArrayList<>();

        super.onCreate(savedInstanceState);
        setContentView(container = new FrameLayout(this));

        getPermission();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.methods, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        methodId = item.getItemId();
        Log.e("methodId", String.valueOf(methodId));
        switch (methodId) {
            case R.id.ToastMessage:
                methodNum = methodNames[0];
                break;
            case R.id.VibratorMessage:
                methodNum = methodNames[1];
                break;
            case R.id.VideoMessage:
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
                    builder.setTitle("授权");
                    builder.setMessage("需要允许授权才可使用");
                    String str = permissions[i];
                    builder.setPositiveButton("去允许", (dialog, id) -> {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{str}, 1);
                    });
                    mDialog = builder.create();
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                } else {
                    //选择禁止并勾选禁止后不再询问
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("授权");
                    builder.setMessage("需要允许授权才可使用");
                    builder.setPositiveButton("去授权", (dialog, id) -> {
                        if (mDialog != null && mDialog.isShowing()) {
                            mDialog.dismiss();
                        }
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        //调起应用设置页面
                        startActivityForResult(intent, 2);
                    });
                    mDialog = builder.create();
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                }
            }
        }
    }

}