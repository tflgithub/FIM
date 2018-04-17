package com.cn.tfl.fim.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.cn.tfl.fim.FimApplication;
import com.cn.tfl.fim.R;
import com.cn.tfl.fim.comm.Constant;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.model.LoginConfig;

import org.xutils.x;

/**
 * Created by tfl on 2016/5/19.
 */
public class ActivitySupport extends AppCompatActivity implements IActivitySupport {
    protected Context context = null;
    protected SharedPreferences preferences;
    protected FimApplication fimApplication;
    protected ProgressDialog pg = null;
    protected NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        preferences = getSharedPreferences(Constant.LOGIN_SET, 0);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        pg = new ProgressDialog(context);
        fimApplication = (FimApplication) getApplication();
        fimApplication.addActivity(this);
    }

    @Override
    public FimApplication getFimApplication() {
        return fimApplication;
    }

    @Override
    public void startService() {

    }

    @Override
    public void stopService() {

    }

    @Override
    public boolean validateInternet() {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            openWirelessSet();
            return false;
        } else {
            NetworkInfo[] info = manager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        openWirelessSet();
        return false;
    }


    public void openWirelessSet() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder
                .setTitle(R.string.prompt)
                .setMessage(context.getString(R.string.check_connection))
                .setPositiveButton(R.string.menu_settings,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                Intent intent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                context.startActivity(intent);
                            }
                        })
                .setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.cancel();
                            }
                        });
        dialogBuilder.show();
    }


    @Override
    public boolean hasInternetConnected() {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo network = manager.getActiveNetworkInfo();
            if (network != null && network.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void isExit() {
        new AlertDialog.Builder(context).setTitle("确定退出吗?")
                .setNeutralButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopService();
                        fimApplication.exit();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public SharedPreferences getLoginUserSharedPre() {
        return preferences;
    }

    @Override
    public void saveLoginConfig(LoginConfig loginConfig) {
        preferences.edit()
                .putString(Constant.XMPP_HOST, loginConfig.xmppHost)
                .commit();
        preferences.edit()
                .putInt(Constant.XMPP_PORT, loginConfig.xmppPort).commit();
        preferences
                .edit()
                .putString(Constant.XMPP_SEIVICE_NAME,
                        loginConfig.xmppServiceName).commit();
        preferences.edit()
                .putString(Constant.USERNAME, loginConfig.username)
                .commit();
        preferences.edit()
                .putString(Constant.PASSWORD, loginConfig.password)
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_AUTOLOGIN, loginConfig.isAutoLogin)
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_NOVISIBLE, loginConfig.isNovisible)
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_REMEMBER, loginConfig.isRemember)
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_ONLINE, loginConfig.isOnline)
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_FIRSTSTART, loginConfig.isFirstStart)
                .commit();
    }

    @Override
    public LoginConfig getLoginConfig() {
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.xmppHost = preferences.getString(Constant.XMPP_HOST,
                getResources().getString(R.string.xmpp_host));
        loginConfig.xmppPort = preferences.getInt(Constant.XMPP_PORT,
                getResources().getInteger(R.integer.xmpp_port));
        loginConfig.username = preferences.getString(Constant.USERNAME, null);
        loginConfig.password = preferences.getString(Constant.PASSWORD, null);
        loginConfig.xmppServiceName = preferences.getString(
                Constant.XMPP_SEIVICE_NAME,
                getResources().getString(R.string.xmpp_service_name));
        loginConfig.isAutoLogin = preferences.getBoolean(Constant.IS_AUTOLOGIN,
                getResources().getBoolean(R.bool.is_autologin));
        loginConfig.isNovisible = preferences.getBoolean(Constant.IS_NOVISIBLE,
                getResources().getBoolean(R.bool.is_novisible));
        loginConfig.isRemember = preferences.getBoolean(Constant.IS_REMEMBER,
                getResources().getBoolean(R.bool.is_remember));
        loginConfig.isFirstStart = preferences.getBoolean(
                Constant.IS_FIRSTSTART, true);
        return loginConfig;
    }

    @Override
    public boolean getUserOnlineState() {
        return preferences.getBoolean(Constant.IS_ONLINE, true);
    }

    @Override
    public void setUserOnlineState(boolean isOnline) {
        preferences.edit().putBoolean(Constant.IS_ONLINE, isOnline).commit();
    }


    /**
     * 关闭键盘事件
     */
    public void closeInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public ProgressDialog getProgressDialog() {
        return pg;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        XmppConnectionManager.getInstance().disconnectConnection();
        super.onDestroy();
    }
}
