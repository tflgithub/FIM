package com.cn.tfl.fim.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.cn.tfl.fim.R;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.utils.ValidateUtil;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ContentView(R.layout.activity_register)
public class RegisterActivity extends ActivitySupport {

    @ViewInject(R.id.ui_username_input)
    private EditText edt_username;
    @ViewInject(R.id.ui_password_input)
    private EditText edt_pwd;
    @ViewInject(R.id.ui_name_input)
    private EditText edt_name;
    @ViewInject(R.id.ui_email_input)
    private EditText edt_email;
    private AbstractXMPPConnection connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        findViewById(R.id.ui_register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkData()) {
                    new RegisterTask().execute();
                }
            }
        });
    }


    public class RegisterTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return register();
        }

        @Override
        protected void onPreExecute() {
            getProgressDialog().setTitle("请稍等");
            getProgressDialog().setMessage("正在注册...");
            getProgressDialog().show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Integer result) {
            super.onPostExecute(result);
            getProgressDialog().dismiss();
            switch (result) {
                case 1:
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    showToast("恭喜你，注册成功");
                    finish();
                    break;
                case 2:
                    showToast("注册失败");
                    break;
                case 3:
                    showToast("服务器无法连接");
                    break;
                case 4:
                    showToast("服务器无响应");
                    break;
            }
        }
    }


    private boolean checkData() {
        boolean checked;
        checked = (!ValidateUtil.isEmpty(edt_username, "用户名") && !ValidateUtil
                .isEmpty(edt_pwd, "密码"));
        return checked;
    }


    public static final Integer SUCCESS = 1;//注册成功
    public static final Integer FAILURE = 2;//注册失败
    public static final Integer ERROR = 3;//服务器无法连接
    public static final Integer NORESPONE = 4;//服务器无响应

    private Integer register() {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setServiceName(getResources().getString(R.string.xmpp_service_name));
        configBuilder.setHost(getResources().getString(R.string.xmpp_host));
        configBuilder.setPort(getResources().getInteger(R.integer.xmpp_port));
        configBuilder.setSendPresence(false);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        try {
            connect = new XMPPTCPConnection(configBuilder.build());
            try {
                connect.connect();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            }
            ProviderManager providerManager=new ProviderManager();
            XmppConnectionManager.getInstance().configure(providerManager);

            AccountManager amgr = AccountManager.getInstance(connect);
            amgr.sensitiveOperationOverInsecureConnection(true);
            Map<String, String> map = new HashMap<>();
            map.put("name", edt_name.getText().toString());
            map.put("email", edt_email.getText().toString());
            map.put("creationDate", "" + System.currentTimeMillis() / 1000L);
            amgr.createAccount(edt_username.getText().toString(), edt_pwd.getText().toString(), map);
            return SUCCESS;
        } catch (SmackException.NoResponseException e) {
            Log.e("register", e.getMessage());
            return NORESPONE;
        } catch (XMPPException.XMPPErrorException e) {
            Log.e("register", e.toString());
            return FAILURE;
        } catch (SmackException.NotConnectedException e) {
            Log.e("register", e.getMessage());
            return ERROR;
        }
    }
}
