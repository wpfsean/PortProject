package com.zhketech.mstapp.client.port.project.base;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by Root on 2018/6/28.
 */

public class ActivityManager {

    private static Stack<Activity> activityStack;
    private static ActivityManager instance;

    private ActivityManager() {
    }

    public synchronized static ActivityManager getActivityStackManager() {
        if (instance == null) {
            instance = new ActivityManager();
        }
        return instance;
    }

    /**
     * 关闭activity
     * finish the activity and remove it from stack.
     *
     * @param activity
     */
    public void finishActivity(Activity activity) {
        if (activityStack == null) return;
        if (activity != null) {
            activity.finish();
            activity.overridePendingTransition(0, 0);
            activityStack.remove(activity);
            activity = null;
        }
    }

    /**
     * 获取当前的Activity
     * get the current activity.
     *
     * @return
     */
    public Activity getCurrentActivity() {
        if (activityStack == null || activityStack.isEmpty()) return null;
        Activity activity = (Activity) activityStack.lastElement();
        return activity;
    }

    /**
     * 获取最后一个的Activity
     * get the first activity in the stack.
     *
     * @return
     */
    public Activity firstActivity() {
        if (activityStack == null || activityStack.isEmpty()) return null;
        Activity activity = (Activity) activityStack.firstElement();
        return activity;
    }


    /**
     * 添加activity到Stack
     * add the activity to the stack.
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    /**
     * remove所有Activity
     * remove all activity.
     */
    public void removeAllActivity() {
        if (activityStack == null) return;
        while (true) {
            if (activityStack.empty()) {
                break;
            }
            Activity activity = getCurrentActivity();
            finishActivity(activity);
        }
    }

    /**
     * remove所有Activity但保持目前的Activity
     * remove all activity but keep the current activity.
     */
    public void removeAllActivityWithOutCurrent() {
        Activity activity = getCurrentActivity();
        while (true) {
            if (activityStack.size() == 1) {
                break;
            }
            if (firstActivity() == activity) {
                break;
            } else {
                finishActivity(firstActivity());
            }
        }
    }
}
