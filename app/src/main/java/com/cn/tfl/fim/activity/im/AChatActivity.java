package com.cn.tfl.fim.activity.im;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.cn.tfl.fim.activity.ActivitySupport;
import com.cn.tfl.fim.comm.Constant;
import com.cn.tfl.fim.manager.MessageManager;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.model.IMMessage;
import com.cn.tfl.fim.utils.DateUtil;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by tfl on 2016/5/23.
 */
public abstract class AChatActivity extends ActivitySupport {
    private Chat chat = null;
    protected String to;// 聊天人
    private List<IMMessage> message_pool = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        to = getIntent().getStringExtra("to");
        if (to == null)
            return;
        chat = ChatManager.getInstanceFor(XmppConnectionManager.getInstance().getConnection()).createChat(to);
    }

    @Override
    protected void onResume() {
        message_pool = MessageManager.getInstance()
                .getMessageListByFrom(to);
        if (null != message_pool && message_pool.size() > 0) {
            Collections.sort(message_pool);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constant.NEW_MESSAGE_ACTION);
            registerReceiver(receiver, filter);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.NEW_MESSAGE_ACTION.equals(action)) {
                IMMessage message = intent
                        .getParcelableExtra(IMMessage.IMMESSAGE_KEY);
                message_pool.add(message);
                receiveNewMessage(message);
                refreshMessage(message_pool);
            }
        }
    };


    protected abstract void receiveNewMessage(IMMessage message);

    protected abstract void refreshMessage(List<IMMessage> messages);

    protected List<IMMessage> getMessages() {
        return message_pool;
    }

    protected void sendMessage(String messageContent) throws Exception {
        String time = DateUtil.date2Str(Calendar.getInstance(),
                Constant.MS_FORMART);
        Message message = new Message();
        message.setBody(messageContent);
        chat.sendMessage(message);
        IMMessage newMessage = new IMMessage();
        newMessage.setMsgType(1);
        newMessage.setFromSubJid(chat.getParticipant());
        newMessage.setContent(messageContent);
        newMessage.setTime(time);
        MessageManager.getInstance().saveMessage(newMessage);
        if (message_pool != null) {
            message_pool.add(newMessage);
        }
        // 刷新视图
        refreshMessage(message_pool);
    }


    protected void resh() {
        // 刷新视图
        refreshMessage(message_pool);
    }
}
