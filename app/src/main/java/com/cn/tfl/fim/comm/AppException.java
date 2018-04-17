package com.cn.tfl.fim.comm;

/**
 * Created by Administrator on 2016/5/19.
 */
public class AppException implements Thread.UncaughtExceptionHandler {

    private static AppException instance;
    private AppException(){}
    public static AppException getInstance(){
        if(null == instance)
            instance = new AppException();
        return instance;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
