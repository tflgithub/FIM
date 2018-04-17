package com.cn.tfl.fim.manager;

import android.content.Context;

import com.cn.tfl.fim.comm.Constant;
import com.cn.tfl.fim.model.User;
import com.cn.tfl.fim.utils.StringUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tfl on 2016/5/20.
 */
public class ContacterManager {
    /**
     * 保存着所有的联系人信息
     */
    public Map<String, User> contacters = null;

    private ContacterManager() {

    }

    private static ContacterManager contacterManager;

    public static ContacterManager getInstance() {
        if (contacterManager == null) {
            contacterManager = new ContacterManager();
        }
        return contacterManager;
    }

    public void init(XMPPConnection connection) {
        contacters = new HashMap<>();
        Roster roster = Roster.getInstanceFor(connection);
        for (RosterEntry entry : roster.getEntries()) {
            contacters.put(entry.getUser(),
                    transEntryToUser(entry, roster));
        }
    }

    public void destroy() {
        contacters = null;
    }

    /**
     * 获得所有的联系人列表
     *
     * @return
     */
    public List<User> getContacterList() {
        if (contacters == null)
            throw new RuntimeException("contacters is null");
        List<User> userList = new ArrayList<User>();
        for (String key : contacters.keySet())
            userList.add(contacters.get(key));
        return userList;
    }

    /**
     * 获得所有未分组的联系人列表
     *
     * @return
     */
    public List<User> getNoGroupUserList(Roster roster) {
        List<User> userList = new ArrayList<User>();
        // 服务器的用户信息改变后，不会通知到unfiledEntries
        for (RosterEntry entry : roster.getUnfiledEntries()) {
            userList.add(contacters.get(entry.getUser()).clone());
        }
        return userList;
    }

    /**
     * 获得所有分组联系人
     *
     * @return
     */
    public List<MRosterGroup> getGroups(Roster roster) {
        if (contacters == null)
            throw new RuntimeException("contacters is null");
        List<MRosterGroup> groups = new ArrayList<ContacterManager.MRosterGroup>();
        groups.add(new MRosterGroup(Constant.ALL_FRIEND, getContacterList()));
        for (RosterGroup group : roster.getGroups()) {
            List<User> groupUsers = new ArrayList<User>();
            for (RosterEntry entry : group.getEntries()) {
                groupUsers.add(contacters.get(entry.getUser()));
            }
            groups.add(new MRosterGroup(group.getName(), groupUsers));
        }
        groups.add(new MRosterGroup(Constant.NO_GROUP_FRIEND,
                getNoGroupUserList(roster)));
        return groups;
    }

    /**
     * 根据RosterEntry创建一个User
     *
     * @param entry
     * @return
     */
    public User transEntryToUser(RosterEntry entry, Roster roster) {
        User user = new User();
        if (entry.getName() == null) {
            user.setName(StringUtil.getUserNameByJid(entry.getUser()));
        } else {
            user.setName(entry.getName());
        }
        user.setJID(entry.getUser());
        System.out.println(entry.getUser());
        Presence presence = roster.getPresence(entry.getUser());
        user.setFrom(presence.getFrom());
        user.setStatus(presence.getStatus());
        user.setSize(entry.getGroups().size());
        user.setAvailable(presence.isAvailable());
        user.setType(entry.getType());
        return user;
    }

    /**
     * 修改这个好友的昵称
     *
     * @param user
     * @param nickname
     */
    public void setNickname(User user, String nickname,
                            XMPPConnection connection) throws SmackException.NotConnectedException, XMPPException.XMPPErrorException, SmackException.NoResponseException {
        RosterEntry entry = Roster.getInstanceFor(connection).getEntry(user.getJID());
        entry.setName(nickname);
    }

    /**
     * 把一个好友添加到一个组中
     *
     * @param user
     * @param groupName
     */
    public void addUserToGroup(final User user, final String groupName,
                               final XMPPConnection connection) {
        if (groupName == null || user == null)
            return;
        // 将一个rosterEntry添加到group中是PacketCollector，会阻塞线程
        new Thread() {
            public void run() {
                RosterGroup group = Roster.getInstanceFor(connection).getGroup(groupName);
                // 这个组已经存在就添加到这个组，不存在创建一个组
                RosterEntry entry = Roster.getInstanceFor(connection).getEntry(
                        user.getJID());
                try {
                    if (group != null) {
                        if (entry != null)
                            group.addEntry(entry);
                    } else {
                        RosterGroup newGroup = Roster.getInstanceFor(connection)
                                .createGroup(groupName);
                        if (entry != null)
                            newGroup.addEntry(entry);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 把一个好友从组中删除
     *
     * @param user
     * @param groupName
     */
    public void removeUserFromGroup(final User user,
                                    final String groupName, final XMPPConnection connection) {
        if (groupName == null || user == null)
            return;
        new Thread() {
            public void run() {
                RosterGroup group = Roster.getInstanceFor(connection).getGroup(groupName);
                if (group != null) {
                    try {
                        System.out.println(user.getJID() + "----------------");
                        RosterEntry entry = Roster.getInstanceFor(connection).getEntry(
                                user.getJID());
                        if (entry != null)
                            try {
                                group.removeEntry(entry);
                            } catch (SmackException.NoResponseException e) {
                                e.printStackTrace();
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static class MRosterGroup {
        private String name;
        private List<User> users;

        public MRosterGroup(String name, List<User> users) {
            this.name = name;
            this.users = users;
        }

        public int getCount() {
            if (users != null)
                return users.size();
            return 0;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }

    }

    /**
     * 根据jid获得用户昵称
     *
     * @param Jid
     * @param connection
     * @return
     */
    public User getNickname(String Jid, XMPPConnection connection) {
        Roster roster = Roster.getInstanceFor(connection);
        for (RosterEntry entry : roster.getEntries()) {
            String params = entry.getUser();
            if (params.split("/")[0].equals(Jid)) {
                return transEntryToUser(entry, roster);
            }
        }
        return null;

    }

    /**
     * 添加分组 .
     *
     * @param groupName
     * @param connection
     */
    public void addGroup(final String groupName,
                         final XMPPConnection connection) {
        if (StringUtil.empty(groupName)) {
            return;
        }

        // 将一个rosterEntry添加到group中是PacketCollector，会阻塞线程
        new Thread() {
            public void run() {
                try {
                    RosterGroup g = Roster.getInstanceFor(connection).getGroup(groupName);
                    if (g != null) {
                        return;
                    }
                    Roster.getInstanceFor(connection).createGroup(groupName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 获得所有组名
     *
     * @return
     */
    public List<String> getGroupNames(Roster roster) {
        List<String> groupNames = new ArrayList<String>();
        for (RosterGroup group : roster.getGroups()) {
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    /**
     * 从花名册中删除用户.
     *
     * @param userJid
     * @throws XMPPException
     */
    public void deleteUser(String userJid) throws XMPPException, SmackException.NotLoggedInException, SmackException.NotConnectedException, SmackException.NoResponseException {
        Roster roster = Roster.getInstanceFor(XmppConnectionManager.getInstance().getConnection());
        RosterEntry entry = roster.getEntry(userJid);
        roster.removeEntry(entry);
    }

    /**
     * 根据用户jid得到用户
     */
    public static User getByUserJid(String userJId, XMPPConnection connection) {
        Roster roster = Roster.getInstanceFor(connection);
        RosterEntry entry = roster.getEntry(userJId);
        if (null == entry) {
            return null;
        }
        User user = new User();
        if (entry.getName() == null) {
            user.setName(StringUtil.getUserNameByJid(entry.getUser()));
        } else {
            user.setName(entry.getName());
        }
        user.setJID(entry.getUser());
        System.out.println(entry.getUser());
        Presence presence = roster.getPresence(entry.getUser());
        user.setFrom(presence.getFrom());
        user.setStatus(presence.getStatus());
        user.setSize(entry.getGroups().size());
        user.setAvailable(presence.isAvailable());
        user.setType(entry.getType());
        return user;
    }
}
