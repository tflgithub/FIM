package com.cn.tfl.fim.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.cn.tfl.fim.MainActivity;
import com.cn.tfl.fim.R;
import com.cn.tfl.fim.comm.Constant;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.model.LoginConfig;
import com.cn.tfl.fim.utils.ValidateUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActivitySupport {
    private EditText edt_username, edt_pwd;
    private CheckBox rememberCb, autologinCb, novisibleCb;
    private Button btn_login = null;
    private LoginConfig loginConfig;
    private VCard vc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检测网络和版本
        validateInternet();
    }


    private void init() {
        vc = new VCard();
        loginConfig = getLoginConfig();
        // 如果为自动登录
        if (loginConfig.isAutoLogin) {
            UserLoginTask loginTask = new UserLoginTask(loginConfig);
            loginTask.execute();
        }
        edt_username = (EditText) findViewById(R.id.ui_username_input);
        edt_pwd = (EditText) findViewById(R.id.ui_password_input);
        rememberCb = (CheckBox) findViewById(R.id.remember);
        autologinCb = (CheckBox) findViewById(R.id.autologin);
        novisibleCb = (CheckBox) findViewById(R.id.novisible);
        btn_login = (Button) findViewById(R.id.ui_login_btn);

        // 初始化各组件的默认状态
        edt_username.setText(loginConfig.username);
        edt_pwd.setText(loginConfig.password);
        rememberCb.setChecked(loginConfig.isRemember);
        rememberCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (!isChecked)
                    autologinCb.setChecked(false);
            }
        });
        autologinCb.setChecked(loginConfig.isAutoLogin);
        novisibleCb.setChecked(loginConfig.isNovisible);
        findViewById(R.id.ui_register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getProgressDialog().isShowing()) {
                    return;
                }
                if (checkData() && validateInternet()) {
                    String username = edt_username.getText().toString();
                    String password = edt_pwd.getText().toString();
                    vc.setFirstName(edt_username.getText().toString());
                    // 先记录下各组件的目前状态,登录成功后才保存
                    loginConfig.password = password;
                    loginConfig.username = username;
                    loginConfig.isRemember = rememberCb.isChecked();
                    loginConfig.isAutoLogin = autologinCb.isChecked();
                    loginConfig.isNovisible = novisibleCb.isChecked();
                    UserLoginTask loginTask = new UserLoginTask(loginConfig);
                    loginTask.execute();
                }
            }
        });
    }


    /**
     * 登录校验.
     *
     * @return
     * @author shimiso
     * @update 2012-5-16 上午9:12:37
     */
    private boolean checkData() {
        boolean checked = false;
        checked = (!ValidateUtil.isEmpty(edt_username, "登录名") && !ValidateUtil
                .isEmpty(edt_pwd, "密码"));
        return checked;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Integer, Integer> {

        private LoginConfig loginConfig;

        public UserLoginTask(LoginConfig loginConfig) {
            this.loginConfig = loginConfig;
        }

        @Override
        protected Integer doInBackground(String... params) {
            return login();
        }

        @Override
        protected void onPreExecute() {
            getProgressDialog().setTitle("请稍等");
            getProgressDialog().setMessage("正在登录...");
            getProgressDialog().show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Integer result) {
            getProgressDialog().dismiss();
            switch (result) {
                case Constant.LOGIN_SECCESS: // 登录成功
                    XMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
                    if (connection.isConnected() && connection.isAuthenticated()) {
                        Toast.makeText(context, "登陆成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        if (loginConfig.isFirstStart) {// 如果是首次启动
                            intent.setClass(context, GuideViewActivity.class);
                            loginConfig.isFirstStart = false;
                        } else {
                            intent.setClass(context, MainActivity.class);
                        }
                        saveLoginConfig(loginConfig);// 保存用户配置信息
                        context.startActivity(intent);
                        finish();
                    }
                    break;
                case Constant.LOGIN_ERROR_ACCOUNT_PASS:// 账户或者密码错误
                    Toast.makeText(
                            context,
                            context.getResources().getString(
                                    R.string.message_invalid_username_password),
                            Toast.LENGTH_SHORT).show();
                    break;
                case Constant.SERVER_UNAVAILABLE:// 服务器连接失败
                    Toast.makeText(
                            context,
                            context.getResources().getString(
                                    R.string.message_server_unavailable),
                            Toast.LENGTH_SHORT).show();
                    break;
                case Constant.LOGIN_ERROR:// 未知异常
                    Toast.makeText(
                            context,
                            context.getResources().getString(
                                    R.string.unrecoverable_error), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            super.onPostExecute(result);
        }
    }

    /**
     * 登录
     *
     * @return
     */
    private Integer login() {
        try {
            String username = loginConfig.username;
            String password = loginConfig.password;
            XmppConnectionManager xmppConnectionManager = XmppConnectionManager.getInstance();
            xmppConnectionManager.init(loginConfig);
            xmppConnectionManager.connectConnection();
            xmppConnectionManager.login(username, password);
            loginConfig.username = username;
            if (loginConfig.isRemember) {// 保存密码
                loginConfig.password = password;
            } else {
                loginConfig.password = "";
            }
            loginConfig.isOnline = true;
            return Constant.LOGIN_SECCESS;
        } catch (Exception xee) {
            if (xee instanceof SmackException) {
                return Constant.SERVER_UNAVAILABLE;
            } else if (xee instanceof XMPPException) {
                return Constant.LOGIN_ERROR_ACCOUNT_PASS;
            } else {
                return Constant.LOGIN_ERROR;
            }
        }
    }
}

