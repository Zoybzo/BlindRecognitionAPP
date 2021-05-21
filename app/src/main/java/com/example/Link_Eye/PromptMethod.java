package com.example.Link_Eye;

import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import myUtils.Enums;

import static myUtils.myToast.showMyToast;

public class PromptMethod {

    private static PromptMethod instance = null;
    private Enums.Method methodNum = Enums.Method.ToastMessage;
    private final Toast toast = Toast.makeText(SurfaceViewCallback.getInstance().getContext(), "", Toast.LENGTH_LONG);

    private PromptMethod() {

    }

    public static PromptMethod getInstance() {
        if (instance == null) instance = new PromptMethod();
        return instance;
    }

    public void getMethod(boolean appear) {
        switch (methodNum) {
            case ToastMessage: {
                Looper.prepare();

                if (appear) toast.setText("盲道出现");
                else toast.setText("盲道消失");

                toast.setGravity(Gravity.CENTER, 0, 0);
                showMyToast(toast, 500);
                Looper.loop();

                break;
            }

            case VibratorMessage: {

                break;
            }

            case VideoMessage: {
                break;
            }
        }
    }

    public Enums.Method getMethodNum() {
        return methodNum;
    }

    public void setMethodNum(Enums.Method methodNum) {
        this.methodNum = methodNum;
    }

}
