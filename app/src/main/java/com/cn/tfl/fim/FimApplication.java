package com.cn.tfl.fim;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.cn.tfl.fim.comm.AppException;
import com.morgoo.droidplugin.PluginHelper;

import org.xutils.x;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tfl on 2016/5/19.
 */
public class FimApplication extends Application {

    private List<Activity> activityList = new LinkedList<>();

    private static FimApplication instance;

    public static FimApplication getInstance() {
        return instance;
    }

    private ExecutorService es;

    public void execRunnable(Runnable r) {
        es.execute(r);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
       // Thread.setDefaultUncaughtExceptionHandler(AppException.getInstance());
       // es = Executors.newFixedThreadPool(3);
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
        PluginHelper.getInstance().applicationOnCreate(getBaseContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        PluginHelper.getInstance().applicationAttachBaseContext(base);
        super.attachBaseContext(base);
    }

    // 添加Activity到容器中
    public void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public void exit() {
//        XmppConnectionManager.getInstance().disconnect();
//        stopService(new Intent(this, PresenceService.class));
        for (Activity activity : activityList) {
            activity.finish();

        }
    }
}
