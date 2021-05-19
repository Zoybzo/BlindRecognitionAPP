package com.example.cameraapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Context context = MainActivity.this;
    SurfaceView surfaceView;
    CameraSurfaceHolder mCameraSurfaceHolder = new CameraSurfaceHolder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPermissionList = new ArrayList<>();
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        getPermission();
        initView();
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

    private AlertDialog mDialog;
    private static final int PERMISSION_REQUEST_CODE = 200;

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSION_REQUEST_CODE:
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 &&
//                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission is granted. Continue the action or workflow
//                    // in your app.
//                } else {
//                    // Explain to the user that the feature is unavailable because
//                    // the features requires a permission that the user has denied.
//                    // At the same time, respect the user's decision. Don't link to
//                    // system settings in an effort to convince the user to change
//                    // their decision.
//                }
//                return;
//        }
//        // Other 'case' lines to check for other
//        // permissions this app might request.
//    }
//}


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        if (requestCode == 1) {
//            int cnt = 0;
//            String[] str = new String[(int) permissions.length];
//            for (int i = 0; i < permissions.length; i++) {
//                if (grantResults[i] != 0) str[cnt++] = permissions[i];
//            }
//
//            if (cnt != 0) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("授权");
//                builder.setMessage("需要允许授权才可使用");
//                builder.setPositiveButton("去允许", (dialog, id) -> {
//                    if (mDialog != null && mDialog.isShowing()) {
//                        mDialog.dismiss();
//                    }
//                    ActivityCompat.requestPermissions(MainActivity.this,
//                            str, 1);
//                });
//                mDialog = builder.create();
//                mDialog.setCanceledOnTouchOutside(false);
//                mDialog.show();
//            } else {
//                // 重启
//            }
//        }

        //TODO: A new method to request permissions.





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