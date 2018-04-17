package com.cn.tfl.fim.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.cn.tfl.fim.FimApplication;
import com.cn.tfl.fim.model.LoginConfig;

/**
 * Created by tfl on 2016/5/19.
 * Activity帮助支持类接口.
 */

public interface IActivitySupport {

    /**
     * 获取fimApplication
     */
    FimApplication getFimApplication();

    /**
     * 开启服务
     */
    void startService();


    /**
     * 停止服务
     */
    void stopService();

    /**
     * 检查网络
     * 校验网络-如果没有网络就弹出设置,并返回true.
     *
     * @return
     */
    boolean validateInternet();

    /**
     * 判断网络连接
     *
     * @return
     */
    boolean hasInternetConnected();

    /**
     * 退出应用
     */
    void isExit();

    /**
     * 获取上下文
     *
     * @return
     */
    Context getContext();

    /**
     * 获取登录配置
     *
     * @return
     */
    SharedPreferences getLoginUserSharedPre();

    /**
     * 保存登录信息
     *
     * @param loginConfig
     */
    void saveLoginConfig(LoginConfig loginConfig);

    /**
     * 获取登录信息
     * @return
     */
    LoginConfig getLoginConfig();

    boolean getUserOnlineState();

    /**
     * 设置用户在线状态 true 在线 false 不在线
     *
     * @param isOnline
     */
    void setUserOnlineState(boolean isOnline);


    /**
     * 吐司
     * @param text
     */
    void showToast(String text);

    /**
     * 进度条
     * @return
     */
    ProgressDialog getProgressDialog();
}
