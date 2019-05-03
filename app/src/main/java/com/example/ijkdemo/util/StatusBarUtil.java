package com.example.ijkdemo.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

/**
 * 设置状态栏为透明色及测量状态蓝高度
 */

public class StatusBarUtil {
    /**
     * 设置状态栏
     */
    public static void setStatusBar(Activity activity) {
        // 设置状态栏t透明颜色ss
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置状态栏去除朦胧遮罩
     */
    public static void setStatus(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            // window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 设置状态栏所有属性去除朦胧遮罩
     */
    public static void setStatusBarAll(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 去除状态栏朦胧阴影
     */
    public static void delStatusBg(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
                Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
                field.setAccessible(true);
                field.setInt(activity.getWindow().getDecorView(), Color.TRANSPARENT);  //改为透明
            } catch (Exception e) {
            }
        }
    }

    /**
     * 设置状态栏
     * 这里我们用到了getIdentifier()的方法来获取资源的ID，
     * 其中第一个参数是要获取资源对象的名称，比如我们要获取状态栏的相关内容，
     * 这里填入"status_bar_height"；
     * 第二个参数是我们要获取什么属性，我们要获取高度内容，所以填入"dimen"；
     * 第三个是包名，状态栏是系统内容，故填入“android”。
     * 另外一个用到的办法是getDimensionPixelSize() ，由函数名就能知道是根据资源ID获得资源像素尺寸，
     * 这里就直接获得状态栏的高度。
     */
    public static void setStatusBarHight(Context context, View v) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) v.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height = getStatusBarHeight(context);// 获取状态栏高度
        //linearParams.width = 30;// 控件的宽强制设成30
        v.setLayoutParams(linearParams); //使设置好的布局参数应用到控件
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
