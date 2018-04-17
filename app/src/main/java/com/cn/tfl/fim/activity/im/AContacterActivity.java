package com.cn.tfl.fim.activity.im;

import android.content.Intent;
import android.os.Bundle;

import com.cn.tfl.fim.activity.ActivitySupport;
import com.cn.tfl.fim.comm.Constant;
import com.cn.tfl.fim.manager.ContacterManager;
import com.cn.tfl.fim.manager.XmppConnectionManager;
import com.cn.tfl.fim.model.User;
import com.cn.tfl.fim.utils.StringUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;

import java.util.List;

/**
 * Created by Administrator on 2016/5/20.
 */
public abstract class AContacterActivity extends ActivitySupport {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContacterManager.getInstance().init(XmppConnectionManager.getInstance().getConnection());
    }

    /**
     * roster添加了一个subcriber
     *
     * @param user
     */
    protected abstract void addUserReceive(User user);

    /**
     * roster删除了一个subscriber
     *
     * @param user
     */
    protected abstract void deleteUserReceive(User user);

    /**
     * roster中的一个subscriber的状态信息信息发生了改变
     *
     * @param user
     */
    protected abstract void changePresenceReceive(User user);

    /**
     * roster中的一个subscriber信息更新了
     *
     * @param user
     */
    protected abstract void updateUserReceive(User user);

    /**
     * 收到一个好友添加请求
     *
     * @param subFrom
     */
    protected abstract void subscripUserReceive(String subFrom);


    /**
     * 回复一个presence信息给用户
     *
     * @param type
     * @param to
     */
    protected void sendSubscribe(Presence.Type type, String to) throws SmackException.NotConnectedException {
        Presence presence = new Presence(type);
        presence.setTo(to);
        XmppConnectionManager.getInstance().getConnection()
                .sendPacket(presence);
    }

    /**
     * 修改这个好友的昵称
     *
     * @param user
     * @param nickname
     */
    protected void setNickname(User user, String nickname) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        ContacterManager.getInstance().setNickname(user, nickname, XmppConnectionManager
                .getInstance().getConnection());
    }

    /**
     * 把一个好友添加到一个组中 先移除当前分组，然后添加到新分组
     *
     * @param user
     * @param groupName
     */
    protected void addUserToGroup(final User user, final String groupName) {

        if (null == user) {
            return;
        }
        if (StringUtil.notEmpty(groupName) && Constant.ALL_FRIEND != groupName
                && Constant.NO_GROUP_FRIEND != groupName) {
            ContacterManager.getInstance().addUserToGroup(user, groupName,
                    XmppConnectionManager.getInstance().getConnection());
        }
    }

    /**
     * 把一个好友从组中删除
     *
     * @param user
     * @param groupName
     */
    protected void removeUserFromGroup(User user, String groupName) {

        if (null == user) {
            return;
        }
        if (StringUtil.notEmpty(groupName)
                && !Constant.ALL_FRIEND.equals(groupName)
                && !Constant.NO_GROUP_FRIEND.equals(groupName))
            ContacterManager.getInstance().removeUserFromGroup(user, groupName,
                    XmppConnectionManager.getInstance().getConnection());

    }

    /**
     * 添加一个联系人
     *
     * @param userJid  联系人JID
     * @param nickname 联系人昵称
     * @param groups   联系人添加到哪些组
     * @throws XMPPException
     */
    protected void createSubscriber(String userJid, String nickname,
                                    String[] groups) throws XMPPException, SmackException.NotLoggedInException, SmackException.NotConnectedException, SmackException.NoResponseException {
        XMPPConnection connection = XmppConnectionManager.getInstance().getConnection();
        if (connection.isConnected() && connection.isAuthenticated()) {
            Roster.getInstanceFor(connection)
                    .createEntry(userJid, nickname, groups);
        }
    }

    /**
     * 删除一个联系人
     *
     * @param userJid 联系人的JID
     * @throws XMPPException
     */
    protected void removeSubscriber(String userJid) throws XMPPException, SmackException.NotLoggedInException, SmackException.NotConnectedException, SmackException.NoResponseException {
        ContacterManager.getInstance().deleteUser(userJid);

    }

    /**
     * 修改一个组的组名
     */
    protected void updateGroupName(String oldGroupName, String newGroupName) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        Roster.getInstanceFor(XmppConnectionManager.getInstance().getConnection())
                .getGroup(oldGroupName).setName(newGroupName);
    }

    /**
     * 这添加分组.
     */
    protected void addGroup(String newGroupName) {
        ContacterManager.getInstance().addGroup(newGroupName, XmppConnectionManager
                .getInstance().getConnection());

    }

    /**
     * 创建一个聊天
     *
     * @param user
     */
    protected void createChat(User user) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("to", user.getJID());
        startActivity(intent);
    }

    /**
     * 冲连接返回
     *
     * @param isSuccess
     */
    protected abstract void handReConnect(boolean isSuccess);

    /**
     * 判断用户名是否存在
     *
     * @param groups
     * @return
     */
    protected boolean isExitJid(String userJid, List<ContacterManager.MRosterGroup> groups) {
        for (ContacterManager.MRosterGroup g : groups) {
            List<User> users = g.getUsers();
            if (users != null && users.size() > 0) {
                for (User u : users) {
                    if (u.getJID().equals(userJid)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
